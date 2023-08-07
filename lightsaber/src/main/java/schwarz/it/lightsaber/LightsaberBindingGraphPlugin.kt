package schwarz.it.lightsaber

import com.google.auto.service.AutoService
import dagger.model.BindingGraph
import dagger.model.BindingGraph.ComponentNode
import dagger.spi.BindingGraphPlugin
import dagger.spi.DiagnosticReporter
import schwarz.it.lightsaber.checkers.checkUnusedBindInstance
import schwarz.it.lightsaber.checkers.checkUnusedBindsAndProvides
import schwarz.it.lightsaber.checkers.checkUnusedDependencies
import schwarz.it.lightsaber.checkers.checkUnusedModules
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@AutoService(BindingGraphPlugin::class)
public class LightsaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "Lightsaber"
    }

    private lateinit var types: Types
    private lateinit var config: LightsaberConfig

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        listOf(
            runRule(config.unusedDependencies) { checkUnusedDependencies(bindingGraph, types) },
            runRule(config.unusedModules) { checkUnusedModules(bindingGraph, types) },
            runRule(config.unusedBindInstance) { checkUnusedBindInstance(bindingGraph) },
            runRule(config.unusedBindsAndProvides) { checkUnusedBindsAndProvides(bindingGraph, types) },
        )
            .flatten()
            .forEach { diagnosticReporter.reportComponent(it.reportType.toKind(), it.component, it.message) }
    }

    override fun initTypes(types: Types) {
        this.types = types
    }

    override fun initOptions(options: Map<String, String>) {
        this.config = LightsaberConfig(
            unusedBindInstance = options["Lightsaber.UnusedBindInstance"].toReportType(),
            unusedBindsAndProvides = options["Lightsaber.UnusedBindsAndProvides"].toReportType(),
            unusedDependencies = options["Lightsaber.UnusedDependencies"].toReportType(),
            unusedModules = options["Lightsaber.UnusedModules"].toReportType(),
        )
    }

    override fun supportedOptions(): Set<String> {
        return setOf(
            "Lightsaber.UnusedBindInstance",
            "Lightsaber.UnusedBindsAndProvides",
            "Lightsaber.UnusedDependencies",
            "Lightsaber.UnusedModules",
        )
    }
}

private fun runRule(reportType: ReportType, rule: () -> List<Finding>): List<Issue> {
    if (reportType == ReportType.Ignore) return emptyList()

    return rule().map { Issue(it.component, it.message, reportType) }
}

private data class Issue(
    val component: ComponentNode,
    val message: String,
    val reportType: ReportType,
)

internal data class LightsaberConfig(
    val unusedBindInstance: ReportType,
    val unusedBindsAndProvides: ReportType,
    val unusedDependencies: ReportType,
    val unusedModules: ReportType,
)

internal enum class ReportType {
    Ignore,
    Warning,
    Error,
}

private fun String?.toReportType(): ReportType {
    return when (this) {
        "warning" -> ReportType.Warning
        "error" -> ReportType.Error
        "ignore" -> ReportType.Ignore
        null -> ReportType.Error
        else -> error("Unknown type $this")
    }
}

private fun ReportType.toKind(): Diagnostic.Kind {
    return when (this) {
        ReportType.Ignore -> error("WTF!")
        ReportType.Warning -> Diagnostic.Kind.WARNING
        ReportType.Error -> Diagnostic.Kind.ERROR
    }
}
