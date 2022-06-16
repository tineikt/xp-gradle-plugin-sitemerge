package no.tine.gradle.xp

import groovy.util.slurpersupport.Attributes
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.Project

class SiteMergeModule implements SiteMergeConstants {

	private static SiteMergeExtension extension

	static void load(Project project) {

		/*
        * Register a 'siteMerge' extension
        */
		extension = project.extensions.create("siteMerge", SiteMergeExtension)

		/*
		* Register the actual task with name {@link SiteMergeConstants#taskName}. It will depends on jar.
		*/
		project.task(taskName) {
			group = "MergeSite"
			description = "Merge site.xml from different projects onto one site.xml"

			dependsOn project.configurations.include

			doLast {
				merge(project)
			}
		}
	}

	/**
	 * Performs a merge of site.xml from dependencies marked with include.
	 *
	 * @param project
	 */
	static void merge(final Project project) {
		final def include = project.configurations.include
		final def siteXml = extension.getSiteXml() ? extension.getSiteXml() : siteXml
		println("Original source site xml: " + siteXml)
		final GPathResult original = new XmlSlurper().parse(new File(siteXml))

		def siteFiles = []

		include.each { def jar ->
			String name = getJarName(jar)
			siteFiles = findSiteXmlFiles(jar, project)

			siteFiles.forEach { def siteFile ->
				appendToOriginal(siteFile, original, name)
				original = new XmlSlurper().parseText(XmlUtil.serialize(original))
			}

		}

		write(original, project, siteXml)
	}

	static String getMergedAttribute(final String name) {
		return mergedAttribute + "-" + name
	}

	static String getJarName(File file) {
		return file.name.lastIndexOf('.').with {it != -1 ? file.name[0..<it] : file.name}
	}

	/**
	 * Writes the result of the merge to target folder and site.xml folder in src.
	 *
	 * @param original content that is modified.
	 * @param project
	 */
	static void write(final GPathResult original, final Project project, final String siteXml) {
		final def target = extension.getTarget() ? extension.getTarget() : target
		println("Destination site xml: " + target)
		println(siteXml)
		write(original, target, siteXml)
	}


	static void write(final GPathResult original, String ...file) {
		file.each { def fileName ->
			new FileWriter(fileName).withWriter() {  writer ->
				XmlUtil.serialize(original, writer)
			}
		}
	}

	/**
	 * Perform removal of old merges. This merges is marked with attribute 'merged=xp-gradle-plugin-sitemerge'.
	 *
	 * @param file of type site.xml located inside a jar.
	 * @param original what is too be modified.
	 * @param name is the name of the jar.
	 */
	static void appendToOriginal(final File file, final GPathResult original, final String name) {

		GPathResult siteLib = new XmlSlurper().parse(file)

		def configName = getConfigName(file, File.separator)

		removeOldMerges(original, name)

		appendToForm(siteLib, original, configName, name)

		appendXData(siteLib, original, configName, name)

		appendMappings(siteLib, original, configName, name)

		appendProcessors(siteLib, original, configName, name)

	}

	static String getConfigName(File file, String separator) {
		return file.toString().tokenize(separator).last()[0..-5]
	}

	/**
	 * Removed old merge result from the original so it it not duplicated.
	 *
	 * @return a clean original
	 */
	static def removeOldMerges(GPathResult original, final String name) {
		String attributeName = getMergedAttribute(name)
		original.form.children().findAll { it.@merged == attributeName }.each { it.replaceNode {} }
		original.children().findAll { it.attributes().get(mergedAttributeName) == attributeName }.each { it.replaceNode {} }
		original.mappings.children().findAll { it.@merged == attributeName }.each { it.replaceNode {} }
	}

	/**
	 * @return all site.xmls inside the 'include' dependencies.
	 */
	static findSiteXmlFiles(final def file, final Project project) {
		return project.zipTree(file).matching({
			include 'site/site.xml'
		}).collect()
	}

	/**
	 * Append other site.xml form from 'include' dependencies to the original <form></form>
	 *
	 * @param siteLib is the site.xml from the included jar.
	 * @param original that will be added to.
	 * @param configName do be used as label.
	 */
	static void appendToForm(final GPathResult siteLib, final GPathResult original, final String configName, final String name) {
		siteLib.form.children().each{ def toBeAdded ->
			if(toBeAdded.label) {
				toBeAdded.label.replaceBody (toBeAdded.label.toString() + ' (' + configName + ')')
			}
			toBeAdded.@merged = getMergedAttribute(name)

			if (!isDuplicate(original, toBeAdded.@name)) {
				original.form.appendNode(toBeAdded)
			} else {
				println("Duplicate found: " + toBeAdded.@name)
			}

		}
	}

	/**
	 * Find if the key exits already.
	 */
	static boolean isDuplicate(GPathResult original, Attributes name) {
		def newXml = new XmlSlurper().parseText(XmlUtil.serialize(original))
		def result = newXml.form.children().any {
			el -> el.@name.text() == name.text()
		}
		return result
	}

	/**
	 * Add mixin's and other x-data elements to the original site.xml.
	 *
	 * @param siteLib  is the site.xml from the included jar.
	 * @param original that will be appended too.
	 * @param configName to be added as attribute 'lib-src'
	 */
	static void appendXData(final GPathResult siteLib, final GPathResult original, final String configName, final String name) {
		siteLib['x-data'].each { def toBeAdded ->
			toBeAdded.attributes()['lib-src'] = configName
			toBeAdded.@merged = getMergedAttribute(name)

			(original << toBeAdded)
		}
	}

	/**
	 * Add mappings to the original site.xml.
	 *
	 * @param siteLib  is the site.xml from the included jar.
	 * @param original that will be appended too.
	 * @param configName to be added as attribute 'lib-src'
	 */
	static void appendMappings(final GPathResult siteLib, final GPathResult original, final String configName, final String name) {
		siteLib.mappings['mapping'].each { def toBeAdded ->
			toBeAdded.attributes()['lib-src'] = configName
			toBeAdded.@merged = getMergedAttribute(name)

			(original.mappings << toBeAdded)
		}
	}

	/**
	 * Add processors to the original site.xml.
	 *
	 * @param siteLib  is the site.xml from the included jar.
	 * @param original that will be appended too.
	 * @param configName to be added as attribute 'lib-src'
	 */
	static void appendProcessors(final GPathResult siteLib, final GPathResult original, final String configName, final String name) {
		siteLib.processors['response-filter'].each { def toBeAdded ->
			toBeAdded.attributes()['lib-src'] = configName
			toBeAdded.@merged = getMergedAttribute(name)

			(original.processors << toBeAdded)
		}
	}

}
