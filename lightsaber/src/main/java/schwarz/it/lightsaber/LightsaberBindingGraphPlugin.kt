package schwarz.it.lightsaber

import com.google.auto.service.AutoService
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraphPlugin
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DiagnosticReporter
import schwarz.it.lightsaber.checkers.checkEmptyComponents
import schwarz.it.lightsaber.checkers.checkUnusedBindsAndProvides
import schwarz.it.lightsaber.checkers.checkUnusedBindsInstances
import schwarz.it.lightsaber.checkers.checkUnusedDependencies
import schwarz.it.lightsaber.checkers.checkUnusedMembersInjectionMethods
import schwarz.it.lightsaber.checkers.checkUnusedModules
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.getQualifiedName
import java.io.PrintWriter

@AutoService(BindingGraphPlugin::class)
public class LightsaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "Lightsaber"
    }

    private lateinit var daggerProcessingEnv: DaggerProcessingEnv
    private lateinit var fileGenerator: FileGenerator
    private lateinit var config: LightsaberConfig

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        val issues = listOf(
            runRule(config.checkEmptyComponents, "EmptyComponents") {
                checkEmptyComponents(bindingGraph, daggerProcessingEnv)
            },
            runRule(config.checkUnusedBindsAndProvides, "UnusedBindsAndProvides") {
                checkUnusedBindsAndProvides(bindingGraph, daggerProcessingEnv)
            },
            runRule(config.checkUnusedBindsInstances, "UnusedBindsInstances") {
                checkUnusedBindsInstances(bindingGraph, daggerProcessingEnv)
            },
            runRule(config.checkUnusedDependencies, "UnusedDependencies") {
                checkUnusedDependencies(bindingGraph, daggerProcessingEnv)
            },
            runRule(config.checkUnusedMembersInjectionMethods, "UnusedMembersInjectionMethods") {
                checkUnusedMembersInjectionMethods(bindingGraph, daggerProcessingEnv)
            },
            runRule(config.checkUnusedModules, "UnusedModules") {
                checkUnusedModules(bindingGraph, daggerProcessingEnv)
            },
        )
            .flatten()
            .ifEmpty { return }

        fileGenerator.createFile("schwarz.it.lightsaber", bindingGraph.getQualifiedName(), "lightsaber")
            .let(::PrintWriter)
            .use { writer ->
                issues.forEach { writer.println(it.getMessage()) }
            }
    }

    override fun init(processingEnv: DaggerProcessingEnv, options: MutableMap<String, String>) {
        this.config = LightsaberConfig(
            checkEmptyComponents = options["Lightsaber.CheckEmptyComponents"] != "false",
            checkUnusedBindsInstances = options["Lightsaber.CheckUnusedBindsInstances"] != "false",
            checkUnusedBindsAndProvides = options["Lightsaber.CheckUnusedBindsAndProvides"] != "false",
            checkUnusedDependencies = options["Lightsaber.CheckUnusedDependencies"] != "false",
            checkUnusedMembersInjectionMethods = options["Lightsaber.CheckUnusedMembersInjectionMethods"] != "false",
            checkUnusedModules = options["Lightsaber.CheckUnusedModules"] != "false",
        )
        this.daggerProcessingEnv = processingEnv
        this.fileGenerator = FileGenerator(processingEnv)
    }

    override fun supportedOptions(): Set<String> {
        return setOf(
            "Lightsaber.CheckEmptyComponents",
            "Lightsaber.CheckUnusedBindsInstances",
            "Lightsaber.CheckUnusedBindsAndProvides",
            "Lightsaber.CheckUnusedDependencies",
            "Lightsaber.CheckUnusedMembersInjectionMethods",
            "Lightsaber.CheckUnusedModules",
        )
    }

    private fun Issue.getMessage(): String {
        return "$codePosition: $message [$rule]"
    }
}

private fun runRule(check: Boolean, ruleName: String, rule: () -> List<Finding>): List<Issue> {
    if (!check) return emptyList()

    return rule()
        .filterNot { it.suppression.hasSuppress(ruleName) }
        .map { Issue(it.codePosition, it.message, ruleName) }
}

private data class Issue(
    val codePosition: CodePosition,
    val message: String,
    val rule: String,
)

internal data class LightsaberConfig(
    val checkEmptyComponents: Boolean,
    val checkUnusedBindsInstances: Boolean,
    val checkUnusedBindsAndProvides: Boolean,
    val checkUnusedDependencies: Boolean,
    val checkUnusedMembersInjectionMethods: Boolean,
    val checkUnusedModules: Boolean,
)
