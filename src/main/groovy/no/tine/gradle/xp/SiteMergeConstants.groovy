package no.tine.gradle.xp

interface SiteMergeConstants {

	/** Name of the main task */
	def taskName = 'mergeSitesXml'

	/** Original site xml file. */
	def siteXml = "src/main/resources/site/site.xml"
	/** Target site.xml build file. */
	def target  = "resources/main/site/site.xml"

	/** Name of merged attribute */
	def mergedAttribute = "xp-gradle-plugin-sitemerge"
	def mergedAttributeName = "merged"
}
