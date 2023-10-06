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
import schwarz.it.lightsaber.utils.KspElements
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getQualifiedName
import java.io.PrintWriter
import javax.lang.model.util.Elements

@AutoService(BindingGraphPlugin::class)
public class LightsaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "Lightsaber"
    }

    private lateinit var daggerProcessingEnv: DaggerProcessingEnv
    private lateinit var filer: FileGenerator
    private lateinit var elements: Elements
    private lateinit var config: LightsaberConfig

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        val issues = listOf(
            runRule(config.checkUnusedDependencies, "UnusedDependencies") {
                checkUnusedDependencies(bindingGraph, daggerProcessingEnv, elements)
            },
            runRule(config.checkUnusedModules, "UnusedModules") {
                checkUnusedModules(bindingGraph, daggerProcessingEnv, elements)
            },
            runRule(config.checkUnusedBindInstance, "UnusedBindInstance") {
                checkUnusedBindInstance(bindingGraph, elements)
            },
            runRule(config.checkUnusedBindsAndProvides, "UnusedBindsAndProvides") {
                checkUnusedBindsAndProvides(bindingGraph, daggerProcessingEnv, elements)
            },
        )
            .flatten()
            .ifEmpty { return }

        filer.createFile("schwarz.it.lightsaber", bindingGraph.getQualifiedName(), "lightsaber")
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
        this.daggerProcessingEnv = processingEnv
        this.filer = FileGenerator(processingEnv)
        this.elements = processingEnv.fold({ it.elementUtils }, { KspElements })
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
