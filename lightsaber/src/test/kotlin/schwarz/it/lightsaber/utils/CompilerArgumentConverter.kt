package schwarz.it.lightsaber.utils

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.ArgumentConverter

internal open class AbstractCompilerArgumentConverter(private vararg val rule: Rule) : ArgumentConverter {
    final override fun convert(source: Any, context: ParameterContext): KotlinCompiler = when (source as? String) {
        "kapt" -> KaptKotlinCompiler(*rule)
        "ksp" -> KspKotlinCompiler(*rule)
        else -> throw IllegalArgumentException("Unknown compiler of type $source")
    }
}
