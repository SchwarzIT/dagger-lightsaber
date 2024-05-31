package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class LightsaberSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = LightsaberConfig2(environment.options["Lightsaber.CheckUnusedInject"] != "false")
        return LightsaberSymbolProcessor(environment.codeGenerator, config)
    }
}

data class LightsaberConfig2(
    val checkUnusedInject: Boolean,
)
