package schwarz.it.lightsaber.domain

import dagger.Binds
import dagger.Provides
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DaggerTypeElement
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.findAnnotationMirrors
import schwarz.it.lightsaber.utils.getAnnotationValue
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

interface Module {
    fun getIncludedModules(types: Types): List<Module>
    fun getIncludesCodePosition(): CodePosition
    fun getBindings(): List<Binding>

    interface Binding {
        companion object {
            operator fun invoke(element: Element): Binding {
                return ModuleJavac.Binding(element)
            }
        }

        override fun toString(): String
        fun getCodePosition(): CodePosition
    }

    companion object {
        operator fun invoke(daggerTypeElement: DaggerTypeElement): Module {
            return when (daggerTypeElement.backend()!!) {
                DaggerProcessingEnv.Backend.JAVAC -> ModuleJavac(daggerTypeElement.javac())
                DaggerProcessingEnv.Backend.KSP -> TODO()
            }
        }

        operator fun invoke(typeElement: TypeElement): Module {
            return ModuleJavac(typeElement)
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

    override fun getIncludesCodePosition(): CodePosition {
        val annotationMirror = value.findAnnotationMirrors("Module")!!
        return CodePosition(
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

        override fun getCodePosition(): CodePosition {
            return value.toCodePosition()
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
