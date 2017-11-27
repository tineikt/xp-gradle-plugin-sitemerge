import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.*
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.*

class SiteMergeModuleTest {

	@Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder()

	private File build_gradle

	@Before
	public void setup() {
		// Prepare build.gradle
		build_gradle = testProjectDir.newFile('build.gradle')
		build_gradle << 'plugins { id "no.tine.gradle.xp.SiteMerge" }\n'
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
	void helloWorld_standard() {
		def result = gradle('helloWorld')
		assert result.task(":helloWorld").outcome == SUCCESS
		assert result.output.contains("Hello, world!")
	}
}
