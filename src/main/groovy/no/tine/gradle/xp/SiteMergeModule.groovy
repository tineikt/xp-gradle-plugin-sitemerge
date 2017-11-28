package no.tine.gradle.xp

import groovy.xml.XmlUtil
import no.tine.gradle.xp.tasks.MergeSiteXmlTask
import org.gradle.api.Project

class SiteMergeModule {
	static void load(Project project) {
		/*
        * Register a 'siteMerge' extension, with the properties defined in GreetingExtension
        * Reference:
        * https://docs.gradle.org/3.5/userguide/custom_plugins.html#sec:getting_input_from_the_build
        * Example 41.2
        */
		project.extensions.create("siteMerge", SiteMergeExtension)

		/*
        * Clever trick so users don't have to reference a custom task class by its fully qualified name.
        * Reference:
        * https://discuss.gradle.org/t/how-to-create-custom-gradle-task-type-and-not-have-to-specify-full-path-to-type-in-build-gradle/6059/4
        */
		project.ext.SiteMerge = MergeSiteXmlTask


		/*
        * A task using a project property for configuration.
        * Reference:
        * https://docs.gradle.org/3.5/userguide/build_environment.html#sec:gradle_properties_and_system_properties
        * Example 12.1
        */
		project.task('merge') {
			group = "siteMerge"
			description = "Merge site.xmls."

			dependsOn project.configurations.include

			def siteXml = 'src/test/resources/site/site.xml'
			def target = 'src/test/resources/build.xml'

			doLast {
				MergeSiteXmlTask.merge(siteXml, target, project.configurations.include);
			}
		}
	}
}
