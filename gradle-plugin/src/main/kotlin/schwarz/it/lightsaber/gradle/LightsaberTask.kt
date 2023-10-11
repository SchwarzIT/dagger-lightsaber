package schwarz.it.lightsaber.gradle

import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class LightsaberTask : SourceTask(), VerificationTask {
    init {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check for unused dagger code"
    }

    @get:Input
    abstract val severities: MapProperty<Rule, Severity>

    @TaskAction
    fun invoke() {
        checkIssues(source.flatMap { it.readLines() }, severities.get(), ignoreFailures)
    }
}

internal fun checkIssues(lines: List<String>, severities: Map<Rule, Severity>, ignoreFailures: Boolean) {
    require(lines.isNotEmpty()) { "The .lightsaber files should never be empty" }
    val issues = lines
        .mapNotNull { it.toIssue(severities) }
        .onEach { println(it) }

    val errorCount = issues.count { it is Error }
    if (!ignoreFailures && errorCount > 0) {
        val message = if (issues.count() == errorCount) {
            if (errorCount == 1) {
                "Analysis failed with 1 error"
            } else {
                "Analysis failed with $errorCount errors"
            }
        } else {
            if (errorCount == 1) {
                "Analysis failed with ${issues.count()} issues (1 was an error)"
            } else {
                "Analysis failed with ${issues.count()} issues ($errorCount were errors)"
            }
        }
        throw GradleException(message)
    }
}
