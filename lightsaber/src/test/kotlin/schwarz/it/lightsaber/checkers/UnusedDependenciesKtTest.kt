package schwarz.it.lightsaber.checkers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.AbstractCompilerArgumentConverter
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class UnusedDependenciesKtTest {

    private val dependency = createSource(
        """
            package test

            interface Dependency {
                fun dependency(): String
            }
        """.trimIndent(),
    )

    @ParameterizedTest
    @CsvSource("kapt,3,34", "ksp,5,")
    fun dependencyNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(dependencies = [Dependency::class])
                interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(component, dependency)

        compilation.assertUnusedDependencies(
            message = "The dependency `test.Dependency` declared in `test.MyComponent` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun dependencyNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Suppress("UnusedDependencies")
                @Component(dependencies = [Dependency::class])
                interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(component, dependency)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun dependencyUsedOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(dependencies = [Dependency::class])
                interface MyComponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, dependency)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun dependencyUsedOnSubcomponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(dependencies = [Dependency::class])
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
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, dependency)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : AbstractCompilerArgumentConverter(Rule.UnusedDependencies)
}

private fun CompilationResult.assertUnusedDependencies(message: String, line: Int, column: Int?) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedDependencies",
        fileName = sourcesDir.resolve("test/MyComponent.${type.extension}").toString(),
    )
}
