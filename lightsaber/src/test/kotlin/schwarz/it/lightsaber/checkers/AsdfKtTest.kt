package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.KspKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.extension

internal class AsdfKtTest {

    @ParameterizedTest
    @CsvSource("kapt,7,17", "ksp,6,")
    fun asdfReportsError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
            package test

            import dagger.Component

            @Component
            interface MyComponent {
                fun inject(str: String)
            }

            class Foo()
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertAsdf(
            message = "The @Component `test.MyComponent` is not used and could be removed.",
            line = line,
            column = column,
        )
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.Asdf)
                "ksp" -> KspKotlinCompiler(Rule.Asdf)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertAsdf(
    message: String,
    line: Int,
    column: Int?,
    fileName: String = "test/MyComponent",
) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        fileName = sourcesDir.resolve("$fileName.${type.extension}").toString(),
        ruleName = "Asdf",
    )
}


