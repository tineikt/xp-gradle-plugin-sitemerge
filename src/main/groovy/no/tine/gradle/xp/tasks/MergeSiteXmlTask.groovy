package no.tine.gradle.xp.tasks

import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class MergeSiteXmlTask extends DefaultTask {

	@Internal
	def siteXml = "src/main/resources/site/site.xml"

	@Internal
	def target = "build/resources/main/site/site.xml"

	@TaskAction
	def mergeSiteXml() {

		def include = project.configurations.include

		merge(siteXml, target, include)
	}

	public static merge(String siteXmlFile, String targetFile, def include) {
		def original = new XmlSlurper().parse(new File(siteXmlFile))
		def siteFiles = []
		include.each {
			def configName = it.toString().tokenize('/').last()[0..-5]
			siteFiles = project.zipTree(it).matching({
				include 'site/site.xml'
			}).collect()

			siteFiles.forEach({
				def siteLib = new XmlSlurper().parse(it)
				siteLib.config.children().each{
					if(it.label) {
						it.label.replaceBody (it.label.toString() + ' (' + configName + ')')
					}
					original.config.appendNode(it)
				}
				siteLib['x-data'].each{
					it.attributes()['lib-src'] = configName
					original.appendNode(it)
				}
			})
		}
		def writer = new FileWriter(targetFile)
		XmlUtil.serialize(original, writer)
	}
}
