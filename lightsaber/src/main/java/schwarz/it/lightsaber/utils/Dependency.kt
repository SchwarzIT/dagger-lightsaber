package schwarz.it.lightsaber.utils

import javax.lang.model.element.Element

interface Dependency {
    override fun toString(): String

    companion object {
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
