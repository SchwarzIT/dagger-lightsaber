package schwarz.it.lightsaber.utils

import dagger.Binds
import dagger.Provides
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

@JvmInline
value class Module(private val value: TypeElement) {
    override fun toString(): String {
        return value.toString()
    }

    fun getBindings(): List<Binding> {
        return value.enclosedElements.filter { it.isABinding() }.map { Binding(it) }
    }

    fun getIncludedModules(types: Types): List<Module> {
        return value.getAnnotation(dagger.Module::class.java)
            .getTypesMirrorsFromClass { includes }
            .map { Module(types.asElement(it) as TypeElement) }
    }

    @JvmInline
    value class Binding(private val value: Element) {
        override fun toString(): String {
            return "@${bindingAnnotations.first { value.isAnnotatedWith(it) }.simpleName} `${value.simpleName}`"
        }
    }
}

private fun Element.isABinding(): Boolean {
    return bindingAnnotations.any { isAnnotatedWith(it) }
}

private val bindingAnnotations = listOf(Binds::class, Provides::class)
