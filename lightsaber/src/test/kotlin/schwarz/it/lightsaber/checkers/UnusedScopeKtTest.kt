package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.*
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.KspKotlinCompiler

internal class UnusedScopeKtTest {

    @ParameterizedTest
    @CsvSource("kapt,4,14", "ksp,6,")
    fun scopeNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {

        val foo = createSource(
            """
                package test

                import javax.inject.Singleton

                @Singleton
                class Foo
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertUnusedScope(
            message = "The @Singleton scope is Unused.",
            line = line,
            column = column,
        )
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.UnusedScope)
                "ksp" -> KspKotlinCompiler(Rule.UnusedScope)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertUnusedScope(message: String, line: Int, column: Int?) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedScope",
        fileName = sourcesDir.resolve("test/Foo.${type.extension}").toString(),
    )
}
