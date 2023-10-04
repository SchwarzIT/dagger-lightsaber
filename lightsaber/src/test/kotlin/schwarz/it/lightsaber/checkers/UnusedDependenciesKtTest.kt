package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings

class UnusedDependenciesKtTest {

    private val compiler = KaptKotlinCompiler(Rule.UnusedDependencies)

    private val dependency = createSource(
        """
            package test

            interface Dependency {
                fun dependency(): String
            }
        """.trimIndent(),
    )

    @Test
    fun dependencyNotUsed() {
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
            message = "The dependency `test.Dependency` is not used.",
            line = 6,
            column = 34,
        )
    }

    @Test
    fun dependencyUsedOnComponent() {
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

    @Test
    fun dependencyUsedOnSubcomponent() {
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
}

private fun CompilationResult.assertUnusedDependencies(message: String, line: Int, column: Int) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedDependencies",
        fileName = sourcesDir.resolve("test/MyComponent.java").toString(),
    )
}
