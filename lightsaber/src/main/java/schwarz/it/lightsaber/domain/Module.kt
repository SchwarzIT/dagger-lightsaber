package schwarz.it.lightsaber.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.Binds
import dagger.Provides
import dagger.spi.model.DaggerElement
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.findAnnotationMirrors
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getAnnotationValue
import schwarz.it.lightsaber.utils.getDeclaredArguments
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

interface Module {
    fun getIncludedModules(types: Types): List<Module>
    fun getIncludesCodePosition(elements: Elements): CodePosition
    fun getBindings(): List<Binding>

    companion object {
        operator fun invoke(typeElement: TypeElement): Module {
            return ModuleJavac(typeElement)
        }

        operator fun invoke(classDeclaration: KSClassDeclaration): Module {
            return ModuleKsp(classDeclaration)
        }
    }

    interface Binding {
        override fun toString(): String
        fun getCodePosition(elements: Elements): CodePosition

        companion object {
            operator fun invoke(element: DaggerElement): Binding {
                return element.fold(ModuleJavac::Binding, ModuleKsp::Binding)
            }
        }
    }
}

@JvmInline
private value class ModuleJavac(private val value: TypeElement) : Module {

    override fun toString(): String {
        return value.toString()
    }

    override fun getBindings(): List<Module.Binding> {
        return value.getBindings() + value.getCompanion()?.getBindings().orEmpty()
    }

    override fun getIncludedModules(types: Types): List<Module> {
        return value.getAnnotation(dagger.Module::class.java)
            .getTypesMirrorsFromClass { includes }
            .map { ModuleJavac(types.asElement(it) as TypeElement) }
    }

    override fun getIncludesCodePosition(elements: Elements): CodePosition {
        val annotationMirror = value.findAnnotationMirrors("Module")!!
        return elements.getCodePosition(
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

        override fun getCodePosition(elements: Elements): CodePosition {
            return elements.getCodePosition(value)
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
        return value.getAllFunctions()
            .filter { func -> bindingAnnotations.any { func.isAnnotationPresent(it) } }
            .map { Binding(it) }
            .toList()
    }

    override fun getIncludedModules(types: Types): List<Module> {
        return value
            .getDeclaredArguments(dagger.Module::class, "includes")
            .map { Module(it.declaration as KSClassDeclaration) }
    }

    override fun getIncludesCodePosition(elements: Elements): CodePosition {
        return value.location.toCodePosition()
    }

    @JvmInline
    value class Binding(private val value: KSAnnotated) : Module.Binding {
        @OptIn(KspExperimental::class)
        override fun toString(): String {
            return "@${bindingAnnotations.first { value.isAnnotationPresent(it) }.simpleName} `$value`"
        }

        override fun getCodePosition(elements: Elements): CodePosition {
            return value.location.toCodePosition()
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
