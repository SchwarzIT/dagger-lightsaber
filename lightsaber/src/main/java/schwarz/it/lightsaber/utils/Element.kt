package schwarz.it.lightsaber.utils

import javax.lang.model.element.Element
import kotlin.reflect.KClass

internal fun <T : Annotation> Element.isAnnotatedWith(klass: KClass<T>): Boolean {
    return getAnnotation(klass.java) != null
}
