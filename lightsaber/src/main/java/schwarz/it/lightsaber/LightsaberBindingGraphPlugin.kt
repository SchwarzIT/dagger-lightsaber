package schwarz.it.lightsaber

import com.google.auto.service.AutoService
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraphPlugin
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DiagnosticReporter
import schwarz.it.lightsaber.checkers.checkUnusedBindInstance
import schwarz.it.lightsaber.checkers.checkUnusedBindsAndProvides
import schwarz.it.lightsaber.checkers.checkUnusedDependencies
import schwarz.it.lightsaber.checkers.checkUnusedModules
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.fold
import java.io.PrintWriter
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(BindingGraphPlugin::class)
public class LightsaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "Lightsaber"
    }

    private lateinit var types: Types
    private lateinit var filer: FileGenerator
    private lateinit var elements: Elements
    private lateinit var config: LightsaberConfig

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        val issues = listOf(
            runRule(config.checkUnusedDependencies, "UnusedDependencies") {
                checkUnusedDependencies(bindingGraph, types, elements)
            },
            runRule(config.checkUnusedModules, "UnusedModules") {
                checkUnusedModules(bindingGraph, types, elements)
            },
            runRule(config.checkUnusedBindInstance, "UnusedBindInstance") {
                checkUnusedBindInstance(bindingGraph, elements)
            },
            runRule(config.checkUnusedBindsAndProvides, "UnusedBindsAndProvides") {
                checkUnusedBindsAndProvides(bindingGraph, types, elements)
            },
        )
            .flatten()
            .ifEmpty { return }

        val qualifiedName = bindingGraph.rootComponentNode().componentPath().currentComponent()
            .fold(
                { it.qualifiedName.toString() },
                { TODO("ksp is not supported yet") },
            )

        filer.createFile("schwarz.it.lightsaber", qualifiedName, "lightsaber")
            .let(::PrintWriter)
            .use { writer ->
                issues.forEach { writer.println(it.getMessage()) }
            }
    }

    override fun init(processingEnv: DaggerProcessingEnv, options: MutableMap<String, String>) {
        this.config = LightsaberConfig(
            checkUnusedBindInstance = options["Lightsaber.CheckUnusedBindInstance"] != "false",
            checkUnusedBindsAndProvides = options["Lightsaber.CheckUnusedBindsAndProvides"] != "false",
            checkUnusedDependencies = options["Lightsaber.CheckUnusedDependencies"] != "false",
            checkUnusedModules = options["Lightsaber.CheckUnusedModules"] != "false",
        )
        this.filer = FileGenerator(processingEnv)
        this.elements = processingEnv.fold({ it.elementUtils }, { TODO("ksp is not supported yet") })
        this.types = processingEnv.fold({ it.typeUtils }, { TODO("ksp is not supported yet") })
    }

    override fun supportedOptions(): Set<String> {
        return setOf(
            "Lightsaber.CheckUnusedBindInstance",
            "Lightsaber.CheckUnusedBindsAndProvides",
            "Lightsaber.CheckUnusedDependencies",
            "Lightsaber.CheckUnusedModules",
        )
    }

    private fun Issue.getMessage(): String {
        return "$codePosition: $message [$rule]"
    }
}

private fun runRule(check: Boolean, ruleName: String, rule: () -> List<Finding>): List<Issue> {
    if (!check) return emptyList()

    return rule().map { Issue(it.codePosition, it.message, ruleName) }
}

private data class Issue(
    val codePosition: CodePosition,
    val message: String,
    val rule: String,
)

internal data class LightsaberConfig(
    val checkUnusedBindInstance: Boolean,
    val checkUnusedBindsAndProvides: Boolean,
    val checkUnusedDependencies: Boolean,
    val checkUnusedModules: Boolean,
)
