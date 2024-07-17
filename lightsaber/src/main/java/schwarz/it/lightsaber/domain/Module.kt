package schwarz.it.lightsaber.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.Binds
import dagger.Provides
import dagger.spi.model.DaggerElement
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.findAnnotationMirrors
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getAnnotationValue
import schwarz.it.lightsaber.utils.getCompanion
import schwarz.it.lightsaber.utils.getDeclaredArguments
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getTypes
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

interface Module {
    fun getIncludedModules(daggerProcessingEnv: DaggerProcessingEnv): List<Module>
    fun getIncludesCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition
    fun getBindings(): List<Binding>

    companion object {
        operator fun invoke(
            daggerProcessingEnv: DaggerProcessingEnv,
            typeMirror: TypeMirror,
        ): Module {
            return ModuleJavac(daggerProcessingEnv, typeMirror)
        }

        operator fun invoke(typeElement: TypeElement): Module {
            return ModuleJavac(typeElement)
        }

        operator fun invoke(classDeclaration: KSClassDeclaration): Module {
            return ModuleKsp(classDeclaration)
        }
    }

    interface Binding : Suppression {
        override fun toString(): String
        fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition

        companion object {
            operator fun invoke(element: DaggerElement): Binding {
                return element.fold(ModuleJavac::Binding, ModuleKsp::Binding)
            }
        }
    }
}

@JvmInline
private value class ModuleJavac(private val value: TypeElement) : Module {

    constructor(daggerProcessingEnv: DaggerProcessingEnv, typeMirror: TypeMirror) : this(
        daggerProcessingEnv.getTypes().asElement(typeMirror) as TypeElement,
    )

    override fun toString(): String {
        return value.toString()
    }

    override fun getBindings(): List<Module.Binding> {
        return value.getBindings() + value.getCompanion()?.getBindings().orEmpty()
    }

    override fun getIncludedModules(daggerProcessingEnv: DaggerProcessingEnv): List<Module> {
        return value.getAnnotation(dagger.Module::class.java)
            .getTypesMirrorsFromClass { includes }
            .map { ModuleJavac(daggerProcessingEnv, it) }
    }

    override fun getIncludesCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
        val annotationMirror = value.findAnnotationMirrors(dagger.Module::class.qualifiedName!!)!!
        return daggerProcessingEnv.getElements().getCodePosition(
            value,
            annotationMirror,
            annotationMirror.getAnnotationValue("includes"),
        )
    }

    @JvmInline
    value class Binding(private val value: Element) : Module.Binding {
        override fun toString(): String {
            return "@${bindingAnnotations.first { value.isAnnotatedWith(it) }.simpleName} `${value.simpleName}`"
        }

        override fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
            return daggerProcessingEnv.getElements().getCodePosition(value)
        }

        override fun hasSuppress(key: String): Boolean {
            return value.hasSuppress(key)
        }
    }
}

@JvmInline
private value class ModuleKsp(private val value: KSClassDeclaration) : Module {

    override fun toString(): String {
        return value.qualifiedName!!.asString()
    }

    @OptIn(KspExperimental::class)
    override fun getBindings(): List<Module.Binding> {
        val allFunctions = value.getAllFunctions() + value.getCompanion()?.getAllFunctions().orEmpty()

        return allFunctions
            .filter { func -> bindingAnnotations.any { func.isAnnotationPresent(it) } }
            .map { Binding(it) }
            .toList()
    }

    override fun getIncludedModules(daggerProcessingEnv: DaggerProcessingEnv): List<Module> {
        return value
            .getDeclaredArguments(dagger.Module::class, "includes")
            .map { Module(it.declaration as KSClassDeclaration) }
    }

    override fun getIncludesCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
        return value.location.toCodePosition()
    }

    @JvmInline
    value class Binding(private val value: KSAnnotated) : Module.Binding {
        @OptIn(KspExperimental::class)
        override fun toString(): String {
            return "@${bindingAnnotations.first { value.isAnnotationPresent(it) }.simpleName} `$value`"
        }

        override fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
            return value.location.toCodePosition()
        }

        override fun hasSuppress(key: String): Boolean {
            return value.hasSuppress(key)
        }
    }
}

private fun TypeElement.getCompanion(): Element? {
    return enclosedElements.find { it.simpleName.toString() == "Companion" }
}

private fun Element.getBindings(): List<Module.Binding> {
    return this.enclosedElements.filter { it.isABinding() }.map { ModuleJavac.Binding(it) }
}

private fun Element.isABinding(): Boolean {
    return bindingAnnotations.any { isAnnotatedWith(it) }
}

private val bindingAnnotations = listOf(Binds::class, Provides::class)
