package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import schwarz.it.lightsaber.domain.Module
import kotlin.reflect.KClass

internal fun KSClassDeclaration.getDeclaredModules(kClass: KClass<*>, argument: String): List<Module> {
    val modules = annotations
        .single { it.shortName.asString() == kClass.simpleName }
        .arguments
        .singleOrNull { it.name?.asString() == argument }
        ?.value as? List<*>

    return modules
        .orEmpty()
        .map { it as KSType }
        .map { Module(it.declaration as KSClassDeclaration) }
}
