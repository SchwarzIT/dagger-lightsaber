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
            runRule(config.unusedDependencies, "UnusedDependencies") { checkUnusedDependencies(bindingGraph, types) },
            runRule(config.unusedModules, "UnusedModules") { checkUnusedModules(bindingGraph, types) },
            runRule(config.unusedBindInstance, "UnusedBindInstance") { checkUnusedBindInstance(bindingGraph) },
            runRule(config.unusedBindsAndProvides, "UnusedBindsAndProvides") {
                checkUnusedBindsAndProvides(bindingGraph, types)
            },
        )
            .flatten()
            .ifEmpty { return }

        val fileObject = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            "schwarz.it.lightsaber",
            "${bindingGraph.rootComponentNode().componentPath().currentComponent().qualifiedName}.lightsaber",
        )

        PrintWriter(fileObject.openWriter()).use { writer ->
            issues.forEach { writer.println(it.getMessage()) }
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
        val line = diagnosticSource.getLineNumber(pair.fst.pos)
        val column = diagnosticSource.getColumnNumber(pair.fst.pos, true)
        return "${sourceFile.name}:$line:$column"
    }

    private fun ComponentNode.getLocation(): String {
        return this.componentPath().currentComponent().getLocation()
    }

    private fun Issue.getMessage(): String {
        return if (element != null) {
            "${element.getLocation()}: $message [$rule]"
        } else {
            "${component.getLocation()}: $message [$rule]"
        }
    }
}

private fun runRule(reportType: ReportType, ruleName: String, rule: () -> List<Finding>): List<Issue> {
    if (reportType == ReportType.Ignore) return emptyList()

    return rule().map { Issue(it.component, it.message, reportType, ruleName, it.element) }
}

private data class Issue(
    val component: ComponentNode,
    val message: String,
    val reportType: ReportType,
    val rule: String,
    val element: Element? = null,
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
