package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

internal fun KSClassDeclaration.getDeclaredArguments(kClass: KClass<*>, argument: String): List<KSType> {
    val modules = annotations
        .single { it.shortName.asString() == kClass.simpleName }
        .arguments
        .singleOrNull { it.name?.asString() == argument }
        ?.value as? List<*>

    return modules
        .orEmpty()
        .map { it as KSType }
}

internal fun KSClassDeclaration.getDeclaredStringArguments(kClass: KClass<*>, argument: String): List<String> {
    val modules = annotations
        .single { it.shortName.asString() == kClass.simpleName }
        .arguments
        .singleOrNull { it.name?.asString() == argument }
        ?.value as? List<*>

    return modules
        .orEmpty()
        .map { it as String }
}

fun KSClassDeclaration.getCompanion(): KSClassDeclaration? {
    return this.declarations.singleOrNull { (it as? KSClassDeclaration)?.isCompanionObject == true } as KSClassDeclaration?
}

@OptIn(KspExperimental::class)
internal fun KSClassDeclaration.hasSuppress(key: String): Boolean {
    if (!this.isAnnotationPresent(Suppress::class)) return false
    return getDeclaredStringArguments(Suppress::class, "names").any { key in it }
}
