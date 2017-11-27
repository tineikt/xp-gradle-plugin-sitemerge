package no.tine.gradle.xp

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin

class SiteMerge implements Plugin<Project> {
    void apply(final Project project) {
		SiteMergeModule.load(project)
    }
}
