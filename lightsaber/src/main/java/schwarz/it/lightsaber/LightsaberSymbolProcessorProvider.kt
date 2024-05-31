package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import schwarz.it.lightsaber.utils.FileGenerator

class LightsaberSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = LightsaberConfig2(
            checkUnusedInject = environment.options["Lightsaber.CheckUnusedInject"] != "false",
        )
        return LightsaberSymbolProcessor(FileGenerator(environment.codeGenerator), config)
    }
}

data class LightsaberConfig2(
    val checkUnusedInject: Boolean,
)
