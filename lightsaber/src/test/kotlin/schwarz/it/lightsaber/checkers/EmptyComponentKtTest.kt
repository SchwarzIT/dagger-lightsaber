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
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class EmptyComponentKtTest {
    @ParameterizedTest
    @CsvSource("kapt,7,17", "ksp,6,")
    fun emptyComponentReportsError(
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
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertEmptyComponent(
            message = "The @Component `test.MyComponent` is empty and could be removed.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,7,17", "ksp,6,")
    fun emptySubcomponentReportsError(
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
                fun subcomponent(): MySubcomponent
            }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
            package test

            import dagger.Subcomponent

            @Subcomponent
            interface MySubcomponent {
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(subcomponent, component)

        compilation.assertEmptyComponent(
            message = "The @Subcomponent `test.MyComponent → test.MySubcomponent` is empty and could be removed.",
            line = line,
            column = column,
            fileName = "test/MySubcomponent",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noEmptyComponentDoesNotReportError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
            package test

            import dagger.BindsInstance
            import dagger.Component

            @Component
            interface MyComponent {    
                fun myInt(): Int

                @Component.Factory
                interface Factory {
                    fun create(
                        @BindsInstance myInt: Int,
                    ): MyComponent
                }
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noEmptyComponentDoesNotReportError_property(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
            package test

            import dagger.BindsInstance
            import dagger.Component

            @Component
            interface MyComponent {    
                val myInt: Int

                @Component.Factory
                interface Factory {
                    fun create(
                        @BindsInstance myInt: Int,
                    ): MyComponent
                }
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,7,17", "ksp,7,")
    fun emptyComponentWithFactoryButNoProvidesReportsError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
            package test

            import dagger.BindsInstance
            import dagger.Component

            @Component
            interface MyComponent {    
                @Component.Factory
                interface Factory {
                    fun create(
                        @BindsInstance myInt: Int,
                    ): MyComponent
                }
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertEmptyComponent(
            message = "The @Component `test.MyComponent` is empty and could be removed.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noEmptySubcomponentDoesNotReportError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
            package test

            import dagger.Component

            @Component
            interface MyComponent {
                fun subcomponent(): MySubcomponent.Factory
            }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
            package test

            import dagger.BindsInstance
            import dagger.Subcomponent

            @Subcomponent
            interface MySubcomponent {
                fun myInt(): Int

                @Subcomponent.Factory
                interface Factory {

                    fun create(
                        @BindsInstance myInt: Int,
                    ) : MySubcomponent
                }
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(subcomponent, component)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.EmptyComponent)
                "ksp" -> KspKotlinCompiler(Rule.EmptyComponent)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertEmptyComponent(
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
        ruleName = "EmptyComponent",
    )
}
