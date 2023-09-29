package schwarz.it.lightsaber.domain

import dagger.spi.model.DaggerElement
import dagger.spi.model.DaggerTypeElement
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

interface FactoryOrBuilder {
    fun getBindInstance(): List<BindsInstance>
    override fun toString(): String

    companion object {
        operator fun invoke(element: DaggerTypeElement): FactoryOrBuilder {
            return element.fold(::FactoryOrBuilderJavac, { TODO("ksp is not supported yet") })
        }

        operator fun invoke(element: Element): FactoryOrBuilder {
            return FactoryOrBuilderJavac(element)
        }
    }

    interface BindsInstance {
        fun getCodePosition(): CodePosition
        override fun toString(): String

        companion object {
            operator fun invoke(element: DaggerElement): BindsInstance {
                return element.fold(FactoryOrBuilderJavac::BindsInstance, { TODO("ksp is not supported yet") })
            }
        }
    }
}

@JvmInline
private value class FactoryOrBuilderJavac(private val value: Element) : FactoryOrBuilder {
    override fun getBindInstance(): List<BindsInstance> {
        return value.getMethods()
            .flatMap { it.parameters }
            .filter { it.isAnnotatedWith(dagger.BindsInstance::class) }
            .map { BindsInstance(it) }
    }

    override fun toString(): String {
        return value.toString()
    }

    @JvmInline
    value class BindsInstance(private val value: Element) : FactoryOrBuilder.BindsInstance {
        override fun getCodePosition(): CodePosition {
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
