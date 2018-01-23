package no.tine.gradle.xp

import groovy.util.slurpersupport.GPathResult
import spock.lang.Shared
import spock.lang.Specification

class SiteMergeModuleTest extends Specification {

	@Shared
	def original = '<site><config></config><x-data mixin="tags1" lib-src="site"/></site>'

	@Shared
	def siteXml =
			'<site>' +
			' <config>' +
			'   <input name="key" merged="xp-gradle-plugin-sitemerge" type="TextLine">' +
			'     <label>API key (site)</label>' +
			'     <occurrences maximum="1" minimum="1"/>' +
			'   </input>' +
			' </config>' +
			' <x-data mixin="tags2" lib-src="site"/>' +
			'</site>'

	def setup() {
	}

	def "AppendXData"() {
		setup:
		GPathResult originalFile = new XmlSlurper().parseText(original)
		GPathResult siteLib = new XmlSlurper().parseText(siteXml)

		when:
		SiteMergeModule.appendXData(siteLib, originalFile, 'xp-lib', 'site')

		then:
		//println XmlUtil.serialize(originalFile)
		originalFile[0].children.size() == 3
	}

	def "Get config name on windows"() {
		setup:
		File file = new File("C:\\temp\\site\\site.xml")

		when:
		String name = SiteMergeModule.getConfigName(file, "\\")

		then:
		name == "site"
	}

	def "Get config name on linux"() {
		setup:
		File file = new File("/temp/test/site/site.xml")

		when:
		String name = SiteMergeModule.getConfigName(file, "/")

		then:
		name == "site"
	}

	def "Get file name on linux"() {
		setup:
		File file = new File("/temp/test/site/test.jar")

		when:
		String name = SiteMergeModule.getJarName(file)

		then:
		name == "test"
	}


}
