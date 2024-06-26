package schwarz.it.lightsaber.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.spi.model.DaggerElement
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getMethods
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element

interface FactoryOrBuilder {
    fun getBindsInstance(): List<BindsInstance>
    override fun toString(): String

    companion object {
        operator fun invoke(ksClassDeclaration: KSClassDeclaration): FactoryOrBuilder {
            return FactoryOrBuilderKsp(ksClassDeclaration)
        }

        operator fun invoke(element: Element): FactoryOrBuilder {
            return FactoryOrBuilderJavac(element)
        }
    }

    interface BindsInstance : Suppression {
        fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition
        override fun toString(): String

        companion object {
            operator fun invoke(element: DaggerElement): BindsInstance {
                return element.fold(
                    FactoryOrBuilderJavac::BindsInstance,
                    FactoryOrBuilderKsp::BindsInstance,
                )
            }
        }
    }
}

@JvmInline
private value class FactoryOrBuilderJavac(private val value: Element) : FactoryOrBuilder {
    override fun getBindsInstance(): List<BindsInstance> {
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
        override fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
            return daggerProcessingEnv.getElements().getCodePosition(value)
        }

        override fun toString(): String {
            return value.toString()
        }

        override fun hasSuppress(key: String): Boolean {
            return value.hasSuppress(key)
        }
    }
}

@JvmInline
private value class FactoryOrBuilderKsp(private val value: KSClassDeclaration) : FactoryOrBuilder {
    @OptIn(KspExperimental::class)
    override fun getBindsInstance(): List<BindsInstance> {
        return value.getAllFunctions()
            .flatMap { it.parameters }
            .filter { it.isAnnotationPresent(dagger.BindsInstance::class) }
            .map { BindsInstance(it) }
            .toList()
    }

    override fun toString(): String {
        return value.toString()
    }

    @JvmInline
    value class BindsInstance(private val value: KSAnnotated) : FactoryOrBuilder.BindsInstance {
        override fun getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
            return value.location.toCodePosition()
        }

        override fun toString(): String {
            return value.toString()
        }

        override fun hasSuppress(key: String): Boolean {
            return value.hasSuppress(key)
        }
    }
}
