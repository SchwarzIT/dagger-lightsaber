package schwarz.it.lightsaber.gradle

import java.util.Locale

fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
