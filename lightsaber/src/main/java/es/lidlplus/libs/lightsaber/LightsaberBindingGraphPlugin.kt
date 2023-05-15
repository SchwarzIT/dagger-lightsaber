package es.lidlplus.libs.lightsaber

import com.google.auto.service.AutoService
import dagger.model.BindingGraph
import dagger.spi.BindingGraphPlugin
import dagger.spi.DiagnosticReporter
import es.lidlplus.libs.lightsaber.checkers.checkUnusedBindInstance
import es.lidlplus.libs.lightsaber.checkers.checkUnusedBindsAndProvides
import es.lidlplus.libs.lightsaber.checkers.checkUnusedDependencies
import es.lidlplus.libs.lightsaber.checkers.checkUnusedModules
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
        checkUnusedDependencies(bindingGraph, diagnosticReporter, types, config.unusedDependencies)
        checkUnusedModules(bindingGraph, diagnosticReporter, types, config.unusedModules)
        checkUnusedBindInstance(bindingGraph, diagnosticReporter, config.unusedBindInstance)
        checkUnusedBindsAndProvides(bindingGraph, diagnosticReporter, types, config.unusedBindsAndProvides)
    }

    override fun initTypes(types: Types) {
        this.types = types
    }

    override fun initOptions(options: MutableMap<String, String>) {
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
        "warn" -> ReportType.Warning
        "error" -> ReportType.Error
        "ignore" -> ReportType.Ignore
        null -> ReportType.Error
        else -> error("Unknown type $this")
    }
}

internal fun ReportType.toKind(): Diagnostic.Kind {
    return when (this) {
        ReportType.Ignore -> error("WTF!")
        ReportType.Warning -> Diagnostic.Kind.WARNING
        ReportType.Error -> Diagnostic.Kind.ERROR
    }
}
