package no.tine.gradle.xp

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException

class SiteMergeModule {


	static def siteXml = "src/main/resources/site/site.xml"
	static def target  = "build/resources/main/site/site.xml"

	static def testSiteXml = "src/test/resources/site/site.xml"
	static def testTarget = "src/test/resources/build.xml"

	static void load(Project project) {
		/*
        * Register a 'siteMerge' extension
        */
		project.extensions.create("siteMerge", SiteMergeExtension)

		project.task('mergeSitesXml') {
			group = "MergeSite"
			description = "Merge site.xml from different projects onto one site.xml"

			dependsOn project.configurations.include

			doLast {
				merge(project)
			}
		}
	}

	static void merge(Project project) {
		final def include = project.configurations.include
		final def siteXml = project.hasProperty("junit.test") ? testSiteXml :siteXml
		final def original = new XmlSlurper().parse(new File(siteXml))
		def siteFiles = []

		include.each {
			siteFiles = findSiteXmlFiles(it, project)

			siteFiles.forEach({
				appendToOriginal(it, original)
			})
		}
		final def target = project.hasProperty("junit.test") ? testTarget : target

		def writer = new FileWriter(target)
		def originalWriter = new FileWriter(siteXml)

		XmlUtil.serialize(original, writer)
		XmlUtil.serialize(original, originalWriter)
	}

	static void appendToOriginal(file, original) {

		def siteLib = new XmlSlurper().parse(file)
		def configName = file.toString().tokenize('/').last()[0..-5]

		removeOldMerges(original)

		appendToConfig(siteLib, original, configName)

		appendXData(siteLib, original, configName)

	}

	static def removeOldMerges(original) {
		original.config.children().findAll { it.@merged=="xp-gradle-plugin-sitemerge" }.each { it.replaceNode {}  }
		original.children().findAll { it.attributes().get('merged') == "xp-gradle-plugin-sitemerge" }.each { it.replaceNode {}  }
	}

	static findSiteXmlFiles(def file, Project project) {
		return project.zipTree(file).matching({
			include 'site/site.xml'
		}).collect()
	}

	static void appendToConfig(GPathResult siteLib, original, configName) {
		siteLib.config.children().each{ def toBeAdded ->
			if(toBeAdded.label) {
				toBeAdded.label.replaceBody (toBeAdded.label.toString() + ' (' + configName + ')')
			}
			toBeAdded.@merged = "xp-gradle-plugin-sitemerge"

			if (original.config.findAll{ it.@name == toBeAdded.@name}.size() > 0) {
				throw new GradleException("Duplicate config with same name found. Name was " + toBeAdded.@name )
			}

			original.config.appendNode(toBeAdded)
		}
	}

	static void appendXData(GPathResult siteLib, original, configName) {
		siteLib['x-data'].each { def toBeAdded ->
			toBeAdded.attributes()['lib-src'] = configName
			toBeAdded.@merged = "xp-gradle-plugin-sitemerge"

//			if (original.children().findAll { def originalElements ->
//				originalElements.attributes().get('mixin') != null && originalElements.@mixin == toBeAdded.@mixin
//			}.size() > 0) {
//				throw new GradleException("Duplicate mixin's with same name found. Name was " + toBeAdded.@mixin )
//			}

			original.appendNode(toBeAdded)
		}
	}
}
