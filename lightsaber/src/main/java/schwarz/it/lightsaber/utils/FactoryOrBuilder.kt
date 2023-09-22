package schwarz.it.lightsaber.utils

import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

@JvmInline
value class FactoryOrBuilder(private val value: Element) {
    fun getBindInstance(): List<BindsInstance> {
        return value.getMethods()
            .flatMap { it.parameters }
            .filter { it.isAnnotatedWith(dagger.BindsInstance::class) }
            .map { BindsInstance(it) }
    }

    override fun toString(): String {
        return value.toString()
    }

    @JvmInline
    value class BindsInstance(private val value: Element) {
        fun getCodePosition(): CodePosition {
            return value.toCodePosition()
        }

        override fun toString(): String {
            return value.toString()
        }
    }
}

private fun Element.getMethods(): List<ExecutableElement> {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }
        .mapNotNull { it as? ExecutableElement }
}
