package schwarz.it.lightsaber.utils

import dagger.spi.model.DaggerTypeElement

internal fun DaggerTypeElement.hasSuppress(key: String): Boolean {
    return fold(
        { element -> element.hasSuppress(key) },
        { classDeclaration -> classDeclaration.hasSuppress(key) },
    )
}
