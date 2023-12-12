package schwarz.it.lightsaber.utils

import dagger.spi.model.DaggerTypeElement

internal fun DaggerTypeElement.isSuppressedBy(rule: String): Boolean {
    return fold(
        { element -> element.isSuppressedBy(rule) },
        { classDeclaration -> classDeclaration.isSuppressedBy(rule) },
    )
}
