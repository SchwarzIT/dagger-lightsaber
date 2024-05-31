package schwarz.it.lightsaber.checkers

import dagger.Provides
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberProcessorRule
import schwarz.it.lightsaber.getCodePosition
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

internal class UnusedInject2(
    private val elements: Elements,
) : LightsaberProcessorRule {
    private val injects: MutableList<ExecutableElement> = mutableListOf()
    private val provides: MutableSet<TypeMirror> = mutableSetOf()

    override fun process(roundEnv: RoundEnvironment) {
        injects.addAll(
            roundEnv.getElementsAnnotatedWith(Inject::class.java)
                .filterIsInstance<ExecutableElement>(),
        )

        provides.addAll(
            roundEnv.getElementsAnnotatedWith(Provides::class.java)
                .filterIsInstance<ExecutableElement>()
                .map { it.returnType },
        )
    }

    override fun computeFindings(): List<Finding> {
        return injects
            .filter { it.enclosingElement.asType() in provides }
            .map { Finding("This Inject is unused", elements.getCodePosition(it.enclosingElement)) }
    }
}
