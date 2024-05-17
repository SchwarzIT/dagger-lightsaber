package schwarz.it.lightsaber

import dagger.Provides
import java.io.PrintWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.inject.Inject
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
class LightsaberProcessor : AbstractProcessor() {
    private val injects: MutableList<ExecutableElement> = mutableListOf()
    private val provides: MutableSet<TypeMirror> = mutableSetOf()
    private lateinit var elements: Elements
    private lateinit var filer: Filer

    override fun init(processingEnv: ProcessingEnvironment) {
        elements = processingEnv.elementUtils
        filer = processingEnv.filer
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        injects.addAll(
            roundEnv.getElementsAnnotatedWith(Inject::class.java)
                .filterIsInstance<ExecutableElement>(),
        )

        provides.addAll(
            roundEnv.getElementsAnnotatedWith(Provides::class.java)
                .filterIsInstance<ExecutableElement>()
                .map { it.returnType }
        )

        if (roundEnv.processingOver()) {
            val issues = computeFindings().map { Issue(it.codePosition, it.message, "UnusedInject") }

            if (issues.isNotEmpty()) {
                filer.createResource(StandardLocation.CLASS_OUTPUT, "", "processor.lightsaber")
                    .openOutputStream()
                    .let(::PrintWriter)
                    .use { writer ->
                        issues.forEach { writer.println(it.getMessage()) }
                    }
            }
        }

        return false
    }

    private fun computeFindings(): List<Finding> {
        return injects
            .filter { it.enclosingElement.asType() in provides }
            .map { Finding("This Inject is unused", elements.getCodePosition(it.enclosingElement)) }
    }
}
