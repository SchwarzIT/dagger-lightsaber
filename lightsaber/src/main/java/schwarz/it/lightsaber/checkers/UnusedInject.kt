package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dagger.Provides
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberJavacRule
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.domain.hasSuppress
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

internal class UnusedInjectKsp : LightsaberKspRule {
    private val injects: MutableList<KSFunctionDeclaration> = mutableListOf()
    private val provides: MutableSet<KSFunctionDeclaration> = mutableSetOf()

    override fun process(resolver: Resolver) {
        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>(),
        )
        provides.addAll(
            resolver.getSymbolsWithAnnotation(Provides::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>(),
        )
    }

    override fun computeFindings(): List<Finding> {
        val providesKsTypes = provides.map { it.returnType!!.resolve() }
        return injects
            .filter { it.returnType!!.resolve() in providesKsTypes }
            .map { inject ->
                val provide = provides.first { it.returnType!!.resolve() == inject.returnType!!.resolve() }
                val parent = provide.parent as KSClassDeclaration
                val injectName = inject.parent as KSClassDeclaration
                Finding(
                    "The @Inject in `${injectName.qualifiedName!!.asString()}` constructor is unused because there is a @Provides defined in `${parent.qualifiedName!!.asString()}.${provide.simpleName.getShortName()}`.",
                    inject.annotations
                        .single { it.annotationType.resolve().declaration.qualifiedName!!.asString() == Inject::class.qualifiedName!! }
                        .location
                        .toCodePosition(),
                    inject::hasSuppress,
                )
            }
    }
}

internal class UnusedInjectJavac(
    private val elements: Elements,
) : LightsaberJavacRule {
    private val injects: MutableList<ExecutableElement> = mutableListOf()
    private val provides: MutableSet<ExecutableElement> = mutableSetOf()

    override fun process(roundEnv: RoundEnvironment) {
        injects.addAll(
            roundEnv.getElementsAnnotatedWith(Inject::class.java)
                .filterIsInstance<ExecutableElement>(),
        )

        provides.addAll(
            roundEnv.getElementsAnnotatedWith(Provides::class.java)
                .filterIsInstance<ExecutableElement>(),
        )
    }

    override fun computeFindings(): List<Finding> {
        val providesReturnTypes = provides.map { it.returnType }
        return injects
            .filter { it.enclosingElement.asType() in providesReturnTypes }
            .map { inject ->
                val provide = provides.first { it.returnType == inject.enclosingElement.asType() }
                Finding(
                    "The @Inject in `${inject.enclosingElement.asType()}` constructor is unused because there is a @Provides defined in `${provide.enclosingElement.asType()}.${provide.simpleName}`.",
                    elements.getCodePosition(inject.enclosingElement),
                    inject::hasSuppress,
                )
            }
    }
}
