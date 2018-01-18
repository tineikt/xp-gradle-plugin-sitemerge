package no.tine.gradle.xp

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.GradleException
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
		final GPathResult original = new XmlSlurper().parse(new File(siteXml))

		def siteFiles = []

		include.each { def jar ->
			siteFiles = findSiteXmlFiles(jar, project)

			siteFiles.forEach { def siteFile ->
				appendToOriginal(siteFile, original)
			}
		}

		write(original, project, siteXml)
	}

	/**
	 * Writes the result of the merge to target folder and site.xml folder in src.
	 *
	 * @param original content that is modified.
	 * @param project
	 */
	static void write(final GPathResult original, final Project project, final String siteXml) {
		final def target = extension.getTarget() ? extension.getTarget() : target
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
	 */
	static void appendToOriginal(final File file, final GPathResult original) {

		GPathResult siteLib = new XmlSlurper().parse(file)

		def configName = getConfigName(file, File.separator)

		removeOldMerges(original)

		appendToConfig(siteLib, original, configName)

		appendXData(siteLib, original, configName)
	}

	static String getConfigName(File file, String separator) {
		return file.toString().tokenize(separator).last()[0..-5]
	}

	/**
	 * Removed old merge result from the original so it it not duplicated.
	 *
	 * @return a clean original
	 */
	static def removeOldMerges(final GPathResult original) {
		original.config.children().findAll { it.@merged == mergedAttribute }.each { it.replaceNode {} }
		original.children().findAll { it.attributes().get(mergedAttributeName) == mergedAttribute }.each { it.replaceNode {} }
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
	 * Append other site.xml config from 'include' dependencies to the original <config></config>
	 *
	 * @param siteLib is the site.xml from the included jar.
	 * @param original that will be added to.
	 * @param configName do be used as label.
	 */
	static void appendToConfig(final GPathResult siteLib, final GPathResult original, final configName) {
		siteLib.config.children().each{ def toBeAdded ->
			if(toBeAdded.label) {
				toBeAdded.label.replaceBody (toBeAdded.label.toString() + ' (' + configName + ')')
			}
			toBeAdded.@merged = mergedAttribute

			if (original.config.findAll{ it.@name == toBeAdded.@name}.size() > 0) {
				throw new GradleException("Duplicate config with same name found. Name was " + toBeAdded.@name )
			}

			original.config.appendNode(toBeAdded)
		}
	}

	/**
	 * Add mixing's and other x-data elements to the original site.xml.
	 *
	 * @param siteLib  is the site.xml from the included jar.
	 * @param original that will be appended too.
	 * @param configName to be added as attribute 'lib-src'
	 */
	static void appendXData(final GPathResult siteLib, final GPathResult original, final configName) {
		siteLib['x-data'].each { def toBeAdded ->
			toBeAdded.attributes()['lib-src'] = configName
			toBeAdded.@merged = mergedAttribute

			(original << toBeAdded)
		}
	}
}
