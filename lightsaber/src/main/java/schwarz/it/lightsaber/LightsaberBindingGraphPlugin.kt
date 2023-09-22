@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package schwarz.it.lightsaber

import com.google.auto.service.AutoService
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.util.DiagnosticSource
import dagger.model.BindingGraph
import dagger.spi.BindingGraphPlugin
import dagger.spi.DiagnosticReporter
import schwarz.it.lightsaber.checkers.checkUnusedBindInstance
import schwarz.it.lightsaber.checkers.checkUnusedBindsAndProvides
import schwarz.it.lightsaber.checkers.checkUnusedDependencies
import schwarz.it.lightsaber.checkers.checkUnusedModules
import java.io.PrintWriter
import javax.annotation.processing.Filer
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
            runRule(config.checkUnusedDependencies, "UnusedDependencies") {
                checkUnusedDependencies(bindingGraph, types)
            },
            runRule(config.checkUnusedModules, "UnusedModules") { checkUnusedModules(bindingGraph, types) },
            runRule(config.checkUnusedBindInstance, "UnusedBindInstance") { checkUnusedBindInstance(bindingGraph) },
            runRule(config.checkUnusedBindsAndProvides, "UnusedBindsAndProvides") {
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
            checkUnusedBindInstance = options["Lightsaber.CheckUnusedBindInstance"] != "false",
            checkUnusedBindsAndProvides = options["Lightsaber.CheckUnusedBindsAndProvides"] != "false",
            checkUnusedDependencies = options["Lightsaber.CheckUnusedDependencies"] != "false",
            checkUnusedModules = options["Lightsaber.CheckUnusedModules"] != "false",
        )
    }

    override fun supportedOptions(): Set<String> {
        return setOf(
            "Lightsaber.CheckUnusedBindInstance",
            "Lightsaber.CheckUnusedBindsAndProvides",
            "Lightsaber.CheckUnusedDependencies",
            "Lightsaber.CheckUnusedModules",
        )
    }

    private fun CodePosition.getLocation(): String {
        val pair = (elements as JavacElements).getTreeAndTopLevel(this.element, this.annotationMirror, this.annotationValue)
        val sourceFile = (pair.snd as JCTree.JCCompilationUnit).sourcefile
        val diagnosticSource = DiagnosticSource(sourceFile, null)
        val line = diagnosticSource.getLineNumber(pair.fst.pos)
        val column = diagnosticSource.getColumnNumber(pair.fst.pos, true) + errorOffset
        return "${sourceFile.name}:$line:$column"
    }

    private fun Issue.getMessage(): String {
        return "${codePosition.getLocation()}: $message [$rule]"
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
