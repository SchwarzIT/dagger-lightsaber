package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dagger.Provides
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberJavacRule
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

internal class UnusedInjectKsp : LightsaberKspRule {
    private val injects: MutableList<KSFunctionDeclaration> = mutableListOf()
    private val provides: MutableSet<KSType> = mutableSetOf()

    override fun process(resolver: Resolver) {
        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>(),
        )
        provides.addAll(
            resolver.getSymbolsWithAnnotation(Provides::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it.returnType!!.resolve() },
        )
    }

    override fun computeFindings(): List<Finding> {
        return injects
            .filter { it.returnType!!.resolve() in provides }
            .map { Finding("This Inject is unused", it.location.toCodePosition()) }
    }
}

internal class UnusedInjectJavac(
    private val elements: Elements,
) : LightsaberJavacRule {
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
