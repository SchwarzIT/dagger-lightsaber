package schwarz.it.lightsaber.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import dagger.spi.model.DaggerElement
import dagger.spi.model.DaggerTypeElement
import schwarz.it.lightsaber.utils.fold
import javax.lang.model.element.Element
import kotlin.reflect.KClass

fun interface Suppression {
    fun hasSuppress(key: String): Boolean
}

internal fun DaggerTypeElement.hasSuppress(key: String): Boolean {
    return fold(
        { element -> element.hasSuppress(key) },
        { classDeclaration -> classDeclaration.hasSuppress(key) },
    )
}

internal fun DaggerElement.hasSuppress(key: String): Boolean {
    return fold(
        { element -> element.hasSuppress(key) },
        { classDeclaration -> classDeclaration.hasSuppress(key) },
    )
}

internal fun Element.hasSuppress(key: String): Boolean {
    val a = getAnnotation(Suppress::class.java)
    return a?.names?.any { key in it } ?: false
}

@OptIn(KspExperimental::class)
internal fun KSAnnotated.hasSuppress(key: String): Boolean {
    if (!this.isAnnotationPresent(Suppress::class)) return false
    return getDeclaredStringArguments(Suppress::class, "names").any { key in it }
}

private fun KSAnnotated.getDeclaredStringArguments(kClass: KClass<*>, argument: String): List<String> {
    val modules = annotations
        .single { it.shortName.asString() == kClass.simpleName }
        .arguments
        .singleOrNull { it.name?.asString() == argument }
        ?.value as? List<*>

    return modules
        .orEmpty()
        .map { it as String }
}
