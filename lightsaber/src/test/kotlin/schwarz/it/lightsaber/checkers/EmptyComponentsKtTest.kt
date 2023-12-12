package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Nested
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

internal class EmptyComponentsKtTest {

    @Nested
    inner class ComponentInheritance {

        @ParameterizedTest
        @CsvSource("kapt,5,17", "ksp,6,")
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
                    interface MyComponent : MyComponentInterface

                    interface MyComponentInterface
                """.trimIndent(),
            )

            val compilation = compiler.compile(component)

            compilation.assertEmptyComponents(
                message = "The @Component `test.MyComponent` is empty and could be removed.",
                line = line,
                column = column,
            )
        }

        @ParameterizedTest
        @CsvSource("kapt", "ksp")
        fun emptyComponent_suppress_doesNotReportsError(
            @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        ) {
            val component = createSource(
                """
                    package test

                    import dagger.Component
                
                    @Suppress("EmptyComponents")
                    @Component
                    interface MyComponent : MyComponentInterface

                    interface MyComponentInterface
                """.trimIndent(),
            )

            val compilation = compiler.compile(component)

            compilation.assertNoFindings()
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
                    interface MyComponent : MyComponentInterface {
                        @Component.Factory
                        interface Factory {
                            fun create(
                                @BindsInstance myInt: Int,
                            ): MyComponent
                        }
                    }

                    interface MyComponentInterface {
                        fun myInt(): Int
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
                    interface MyComponent : MyComponentInterface {
                        @Component.Factory
                        interface Factory {
                            fun create(
                                @BindsInstance myInt: Int,
                            ): MyComponent
                        }
                    }

                    interface MyComponentInterface {
                        val myInt: Int
                    }
                """.trimIndent(),
            )

            val compilation = compiler.compile(component)

            compilation.assertNoFindings()
        }
    }

    @ParameterizedTest
    @CsvSource("kapt,5,17", "ksp,6,")
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

        compilation.assertEmptyComponents(
            message = "The @Component `test.MyComponent` is empty and could be removed.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,5,17", "ksp,6,")
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

        compilation.assertEmptyComponents(
            message = "The @Subcomponent `test.MySubcomponent` is empty and could be removed.",
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
    @CsvSource("kapt,5,17", "ksp,7,")
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

        compilation.assertEmptyComponents(
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
                "kapt" -> KaptKotlinCompiler(Rule.EmptyComponents)
                "ksp" -> KspKotlinCompiler(Rule.EmptyComponents)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertEmptyComponents(
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
        ruleName = "EmptyComponents",
    )
}
