package schwarz.it.lightsaber.checkers

import com.google.testing.compile.Compilation
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.createCompiler

class UnusedDependenciesKtTest {

    private val compiler = createCompiler(checkUnusedDependencies = true)

    private val dependency = createSource(
        """
            package test;

            public interface Dependency {
                String dependency();
            }
        """.trimIndent(),
    )

    @Test
    fun dependencyNotUsed() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(component, dependency)

        compilation.assertUnusedDependencies(
            message = "The dependency `test.Dependency` is not used.",
            line = 5,
            column = 27,
        )
    }

    @Test
    fun dependencyUsedOnComponent() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                    String dependency();
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
                package test;

                import dagger.Component;

                @Component(dependencies = {Dependency.class})
                public interface MyComponent {
                    MySubcomponent subcomponent();
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test;

                import dagger.Subcomponent;

                @Subcomponent
                public interface MySubcomponent {
                    String dependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, dependency)

        compilation.assertNoFindings()
    }
}

private fun Compilation.assertUnusedDependencies(message: String, line: Int, column: Int) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedDependencies",
    )
}
