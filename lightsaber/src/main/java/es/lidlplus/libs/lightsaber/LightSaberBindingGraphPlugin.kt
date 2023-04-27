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
public class LightSaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "LightSaber"
    }

    private lateinit var types: Types
    private lateinit var config: LightSaberConfig

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
        this.config = LightSaberConfig(
            unusedBindInstance = options["LightSaber.UnusedBindInstance"].toReportType(),
            unusedBindsAndProvides = options["LightSaber.UnusedBindsAndProvides"].toReportType(),
            unusedDependencies = options["LightSaber.UnusedDependencies"].toReportType(),
            unusedModules = options["LightSaber.UnusedModules"].toReportType(),
        )
    }

    override fun supportedOptions(): Set<String> {
        return setOf(
            "LightSaber.UnusedBindInstance",
            "LightSaber.UnusedBindsAndProvides",
            "LightSaber.UnusedDependencies",
            "LightSaber.UnusedModules",
        )
    }
}

internal data class LightSaberConfig(
    val unusedBindInstance: ReportType,
    val unusedBindsAndProvides: ReportType,
    val unusedDependencies: ReportType,
    val unusedModules: ReportType,
)

internal enum class ReportType {
    Ignore,
    Warning,
    Error
}

private fun String?.toReportType(): ReportType {
    return when (this) {
        "warn" -> ReportType.Warning
        "error" -> ReportType.Error
        "ignore" -> ReportType.Ignore
        else -> error("Unknown type $this")
    }
}



internal fun ReportType.toKind(): Diagnostic.Kind {
    return when(this) {
        ReportType.Ignore -> error("WTF!")
        ReportType.Warning -> Diagnostic.Kind.WARNING
        ReportType.Error -> Diagnostic.Kind.ERROR
    }
}
