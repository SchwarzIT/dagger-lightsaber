package schwarz.it.lightsaber.domain

import dagger.spi.model.DaggerElement
import schwarz.it.lightsaber.utils.fold
import javax.lang.model.element.Element

interface Dependency {
    override fun toString(): String

    companion object {
        operator fun invoke(element: DaggerElement): Dependency {
            return element.fold(::DependencyJavac, { TODO("ksp is not supported yet") })
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
