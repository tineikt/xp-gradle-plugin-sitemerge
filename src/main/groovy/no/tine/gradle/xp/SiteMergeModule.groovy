package no.tine.gradle.xp

import no.tine.gradle.xp.tasks.MergeSiteXmlTask
import org.gradle.api.Project

class SiteMergeModule {
	static void load(Project project) {
		/*
        * Register a 'siteMerge' extension
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

	}
}
