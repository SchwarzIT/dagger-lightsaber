package schwarz.it.lightsaber.utils

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.ArgumentConverter

internal open class AbstractCompilerArgumentConverter(private vararg val rule: Rule) : ArgumentConverter {
    final override fun convert(source: Any, context: ParameterContext): Any {
        source as String
        return when (source) {
            "kapt" -> KaptKotlinCompiler(*rule)
            "ksp" -> KspKotlinCompiler(*rule)
            else -> error("Unknown compiler of type $source")
        }
    }
}
