@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package schwarz.it.lightsaber

import com.google.auto.service.AutoService
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.util.DiagnosticSource
import dagger.model.BindingGraph
import dagger.model.BindingGraph.ComponentNode
import dagger.spi.BindingGraphPlugin
import dagger.spi.DiagnosticReporter
import schwarz.it.lightsaber.checkers.checkUnusedBindInstance
import schwarz.it.lightsaber.checkers.checkUnusedBindsAndProvides
import schwarz.it.lightsaber.checkers.checkUnusedDependencies
import schwarz.it.lightsaber.checkers.checkUnusedModules
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(BindingGraphPlugin::class)
public class LightsaberBindingGraphPlugin : BindingGraphPlugin {
    override fun pluginName(): String {
        return "Lightsaber"
    }

    private lateinit var types: Types
    private lateinit var filer: Filer
    private lateinit var elements: Elements
    private lateinit var config: LightsaberConfig

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        val issues = listOf(
            runRule(config.unusedDependencies) { checkUnusedDependencies(bindingGraph, types) },
            runRule(config.unusedModules) { checkUnusedModules(bindingGraph, types) },
            runRule(config.unusedBindInstance) { checkUnusedBindInstance(bindingGraph) },
            runRule(config.unusedBindsAndProvides) { checkUnusedBindsAndProvides(bindingGraph, types) },
        ).flatten().ifEmpty { return }

        val fileObject = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            "schwarz.it.lightsaber",
            bindingGraph.rootComponentNode().componentPath().currentComponent().qualifiedName,
        )

        PrintWriter(fileObject.openWriter()).use {
            issues.forEach { issue -> it.println(issue.getMessage()) }
        }
    }

    override fun initFiler(filer: Filer) {
        this.filer = filer
    }

    override fun initElements(elements: Elements) {
        this.elements = elements
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

    private fun Element.getLocation(): String {
        val pair = (elements as JavacElements).getTreeAndTopLevel(this, null, null)
        val sourceFile = (pair.snd as JCTree.JCCompilationUnit).sourcefile
        val diagnosticSource = DiagnosticSource(sourceFile, null)
        return "${sourceFile.name}:${diagnosticSource.getLineNumber(pair.fst.pos)}:${
            diagnosticSource.getColumnNumber(
                pair.fst.pos,
                true,
            )
        }"
    }

    private fun Issue.getMessage(): String {
        return "${component.componentPath().currentComponent().getLocation()} - $message"
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
