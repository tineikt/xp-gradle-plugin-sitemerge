package no.tine.gradle.xp

import groovy.util.slurpersupport.GPathResult
import spock.lang.Shared
import spock.lang.Specification
import groovy.xml.XmlUtil

class SiteMergeModuleTest extends Specification {

	@Shared
	def original = '<site><form></form><x-data name="tags1" lib-src="site"/><mappings><mapping controller="default3.js" lib-src="site"></mapping></mappings><processors><response-filter name="default-response-filter2" order="20" lib-src="site" /></processors></site>'

	@Shared
	def siteXml =
			'<site>' +
			' <form>' +
			'   <input name="key" merged="xp-gradle-plugin-sitemerge" type="TextLine">' +
			'     <label>API key (site)</label>' +
			'     <occurrences maximum="1" minimum="1"/>' +
			'   </input>' +
			' </form>' +
			' <x-data name="tags2" lib-src="site"/>' +
			' <mappings>' +
			'   <mapping controller="default1.js" lib-src="site" merged="xp-gradle-plugin-sitemerge"></mapping>' +
			'   <mapping controller="default2.js" lib-src="site" merged="xp-gradle-plugin-sitemerge"></mapping>' +
			' </mappings>' +
			' <processors>' +
			'   <response-filter name="default-response-filter1" order="10" lib-src="site" merged="xp-gradle-plugin-sitemerge"></response-filter>' +
			' </processors>' +
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
			// println XmlUtil.serialize(originalFile)
			originalFile[0].children().findAll { it.name() == 'x-data' }.size() == 2
	}

	def "AppendMappings"() {
		setup:
			GPathResult originalFile = new XmlSlurper().parseText(original)
			GPathResult siteLib = new XmlSlurper().parseText(siteXml)

		when:
			SiteMergeModule.appendMappings(siteLib, originalFile, 'xp-lib', 'site')

		then:
			originalFile[0].children().findAll { it.name() == 'mappings' }[0].children().size() == 3
	}

	def "AppendProcessors"() {
		setup:
			GPathResult originalFile = new XmlSlurper().parseText(original)
			GPathResult siteLib = new XmlSlurper().parseText(siteXml)

		when:
			SiteMergeModule.appendProcessors(siteLib, originalFile, 'xp-lib', 'site')

		then:
			originalFile[0].children().findAll { it.name() == 'processors' }[0].children().size() == 2
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
