package schwarz.it.lightsaber.utils

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

fun KSClassDeclaration.getCompanion(): KSClassDeclaration? {
    return this.declarations.singleOrNull { (it as? KSClassDeclaration)?.isCompanionObject == true } as KSClassDeclaration?
}
