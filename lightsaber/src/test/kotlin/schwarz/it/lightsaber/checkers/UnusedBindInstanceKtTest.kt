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

internal class UnusedBindInstanceKtTest {

    @ParameterizedTest
    @CsvSource("kapt,15,13", "ksp,12,")
    fun bindInstanceNotUsed_Factory(
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
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` declared in `test.MyComponent` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,18,13", "ksp,12,")
    fun bindInstanceNotUsed_Builder(
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

                    @Component.Builder
                    interface Builder {
                        fun build(): MyComponent
                        fun myInt(@BindsInstance myInt: Int): Builder
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` declared in `test.MyComponent` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun bindInstanceIsUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.BindsInstance
                import dagger.Component

                @Component
                interface MyComponent {

                    fun dependency(): Int

                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,19,13", "ksp,16,")
    fun bindInstanceNamedIsNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test

                import javax.inject.Named
                import dagger.BindsInstance
                import dagger.Component

                @Component
                interface MyComponent {

                    fun dependency(): Int

                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int,
                            @BindsInstance @Named("secondInt") secondInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `secondInt` declared in `test.MyComponent` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun componentWithBindInstanceAndSubcomponentIsUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.BindsInstance
                import dagger.Component

                @Component
                interface MyComponent {

                    fun subcomponent(): MySubcomponent

                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test

                import dagger.Subcomponent

                @Subcomponent
                interface MySubcomponent {
                    fun dependency(): Int
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun bindInstanceInSubcomponentIsUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.BindsInstance
                import dagger.Component

                @Component
                interface MyComponent {

                    fun subcomponentFactory(): MySubcomponent.Factory
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
                    fun dependency(): Int

                    @Subcomponent.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MySubcomponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,15,13", "ksp,12,")
    fun bindInstanceInSubcomponentIsNoUsed(
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

                    fun subcomponentFactory(): MySubcomponent.Factory
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

                    @Subcomponent.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MySubcomponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` declared in `test.MySubcomponent` is not used.",
            line = line,
            column = column,
            fileName = "test/MySubcomponent",
        )
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.UnusedBindInstance)
                "ksp" -> KspKotlinCompiler(Rule.UnusedBindInstance)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertUnusedBindInstance(
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
        ruleName = "UnusedBindInstance",
    )
}
