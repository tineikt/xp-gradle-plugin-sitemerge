package no.tine.gradle.xp

import org.gradle.api.Plugin
import org.gradle.api.Project

class SiteMerge implements Plugin<Project> {

	private Project project

    void apply(final Project project) {
		SiteMergeModule.load(project)
		this.project = project
		this.add()
    }

	private void add() {
		this.project.getConfigurations().create("mergeSitesXml")
	}

}
