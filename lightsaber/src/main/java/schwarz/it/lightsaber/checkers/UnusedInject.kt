package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dagger.Provides
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberJavacRule
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.domain.Suppression
import schwarz.it.lightsaber.domain.getSuppression
import schwarz.it.lightsaber.domain.hasSuppress
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

internal class UnusedInjectKsp : LightsaberKspRule {
    private val injects: MutableList<InjectInfo> = mutableListOf()
    private val provides: MutableSet<Pair<KSFunctionDeclaration, String>> = mutableSetOf()

    override fun process(resolver: Resolver) {
        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map(::InjectInfo),
        )
        provides.addAll(
            resolver.getSymbolsWithAnnotation(Provides::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it to it.returnType!!.resolve().declaration.qualifiedName!!.asString() },
        )
    }

    override fun computeFindings(): List<Finding> {
        val providesKsTypes = provides.map { (_, type) -> type }.toSet()
        return injects
            .filter { it.type in providesKsTypes }
            .map {
                val provide = provides.first { (_, providesType) -> providesType == it.type }.first
                val parent = provide.parent as KSClassDeclaration
                val injectName = it.inject.parent as KSClassDeclaration
                Finding(
                    "The @Inject in `${injectName.qualifiedName!!.asString()}` constructor is unused because there is a @Provides defined in `${parent.qualifiedName!!.asString()}.${provide.simpleName.getShortName()}`.",
                    it.codePosition,
                    it.hasSuppress,
                )
            }
    }
}

private data class InjectInfo(
    val inject: KSFunctionDeclaration,
    val type: String,
    val codePosition: CodePosition,
    val hasSuppress: Suppression,
) {
    @OptIn(KspExperimental::class)
    constructor(inject: KSFunctionDeclaration) : this(
        inject = inject,
        type = inject.returnType!!.resolve().declaration.qualifiedName!!.asString(),
        codePosition = inject.annotations
            .single { it.annotationType.resolve().declaration.qualifiedName!!.asString() == Inject::class.qualifiedName!! }
            .location
            .toCodePosition(),
        hasSuppress = inject.getSuppression(),
    )
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
