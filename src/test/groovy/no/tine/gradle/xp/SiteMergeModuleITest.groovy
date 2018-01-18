import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SiteMergeModuleITest {

	@Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder(new File("build/"))

	private File build_gradle

	@Before
	void setup() {
		// Prepare build.gradle
		build_gradle = testProjectDir.newFile('build.gradle')
		build_gradle << 'plugins { id "com.enonic.xp.app" version "1.0.13" \n id "no.tine.gradle.xp.SiteMerge" }\n'
		build_gradle << 'siteMerge { siteXml = "src/test/resources/site/site.xml" \n target = "src/test/resources/build.xml" }\n'
		build_gradle << 'dependencies { include files("../resources/test/test.jar") }\n'
	}

	/**
	 * Helper method that runs a Gradle task in the testProjectDir
	 * @param arguments the task arguments to execute
	 * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
	 * @return the task's BuildResult
	 */
	private BuildResult gradle(boolean isSuccessExpected, String[] arguments = ['tasks']) {
		arguments += '--stacktrace'
		def runner = GradleRunner.create()
				.withArguments(arguments)
				.withProjectDir(testProjectDir.root)
				.withPluginClasspath()
				.withDebug(true)
		return isSuccessExpected ? runner.build() : runner.buildAndFail()
	}

	private BuildResult gradle(String[] arguments = ['tasks']) {
		gradle(true, arguments)
	}

	@Test
	void mergeSitesXml() {
		def result = gradle('mergeSitesXml', '-Pjunit.test=true')
		result.task(":mergeSitesXml") == 'SUCCESS'
	}

}
