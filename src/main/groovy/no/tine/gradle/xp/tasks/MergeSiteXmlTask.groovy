package no.tine.gradle.xp.tasks

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class MergeSiteXmlTask extends DefaultTask {

	@Internal
	def siteXml = "src/main/resources/site/site.xml"

	@Internal
	def target = "build/resources/main/site/site.xml"

	@TaskAction
	def mergeSiteXml() {
		if (project.configurations.include != null) {
			this.merge(project.configurations.include)
		}
	}

	protected void merge(def include) {
		final  def original = new XmlSlurper().parse(new File(siteXml))
		def siteFiles = []

		include.each {
			siteFiles = this.findSiteXmlFiles(it)

			siteFiles.forEach({
				appendToOriginal(it, original)
			})
		}

		def writer = new FileWriter(target)
		XmlUtil.serialize(original, writer)
	}

	protected void appendToOriginal(it, original) {

		def siteLib = new XmlSlurper().parse(it)
		def configName = it.toString().tokenize('/').last()[0..-5]

		this.appendToConfig(siteLib, original, configName)

		this.appendXData(siteLib, original, configName);

	}

	protected findSiteXmlFiles(def file) {
		return project.zipTree(file).matching({
			include 'site/site.xml'
		}).collect()
	}

	protected void appendToConfig(GPathResult siteLib, original, configName) {
		siteLib.config.children().each{
			if(it.label) {
				it.label.replaceBody (it.label.toString() + ' (' + configName + ')')
			}
			original.config.appendNode(it)
		}
	}

	protected void appendXData(GPathResult siteLib, original, configName) {
		siteLib['x-data'].each {
			it.attributes()['lib-src'] = configName
			original.appendNode(it)
		}
	}
}
