package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class LightsaberSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LightsaberSymbolProcessor(environment.codeGenerator, LightsaberConfig2(true))
    }
}

data class LightsaberConfig2(
    val checkUnusedInject: Boolean,
)
