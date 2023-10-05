package schwarz.it.lightsaber.domain

import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.spi.model.DaggerElement
import schwarz.it.lightsaber.utils.fold
import javax.lang.model.element.Element

interface Dependency {
    override fun toString(): String

    companion object {
        operator fun invoke(element: DaggerElement): Dependency {
            return element.fold(::DependencyJavac, { DependencyKsp(it as KSDeclaration) })
        }

        operator fun invoke(ksDeclaration: KSDeclaration): Dependency {
            return DependencyKsp(ksDeclaration)
        }

        operator fun invoke(element: Element): Dependency {
            return DependencyJavac(element)
        }
    }
}

@JvmInline
private value class DependencyJavac(private val element: Element) : Dependency {
    override fun toString(): String {
        return element.toString()
    }
}

@JvmInline
private value class DependencyKsp(private val ksDeclaration: KSDeclaration) : Dependency {
    override fun toString(): String {
        return ksDeclaration.qualifiedName!!.asString()
    }
}
