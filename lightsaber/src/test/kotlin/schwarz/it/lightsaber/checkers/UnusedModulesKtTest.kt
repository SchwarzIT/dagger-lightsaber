package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.compile
import schwarz.it.lightsaber.utils.createKotlinCompiler

class UnusedModulesKtTest {

    private val compiler = createKotlinCompiler(checkUnusedModules = true)

    private val module = createSource(
        """
            package test

            import dagger.Module
            import dagger.Provides

            @Module
            class MyModule {
                @Provides
                fun dependency(): String {
                    return "string"
                }
            }
        """.trimIndent(),
    )

    @Test
    fun moduleUsedOnComponent() {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [MyModule::class])
                interface MyComponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertNoFindings()
    }

    @Test
    fun moduleNotUsedOnSubcomponent() {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [MyModule::class])
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
                interface MySubcomponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 6,
            column = 29,
        )
    }

    @Test
    fun moduleUsedOnSubcomponent() {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [MyModule::class])
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

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertNoFindings()
    }

    @Test
    fun moduleUnusedOnSubcomponent2() {
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

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 6,
            column = 32,
            fileName = "test/MySubcomponent.java",
        )
    }

    @Test
    fun moduleUsedOnSubcomponent2() {
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

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertNoFindings()
    }

    @Test
    fun includeModules0() {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [MyModule::class])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 6,
            column = 29,
        )
    }

    @Test
    fun includeModules1() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleB {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used but its child `test.ModuleB` is used.",
            line = 6,
            column = 29,
        )
    }

    @Test
    fun includeModules2() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleB
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)
        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used.",
            line = 6,
            column = 27,
            fileName = "test/ModuleA.java",
        )
    }

    @Test
    fun includeModules3() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleC::class])
                class ModuleB {
                    @Provides
                    fun intDependency(): Int {
                        return 0
                    }
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleC
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency(): String
                    fun intDependency(): Int
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleC` included by `test.ModuleA → test.ModuleB` is not used.",
            line = 6,
            column = 27,
            fileName = "test/ModuleB.java",
        )
    }

    @Test
    fun includeModules4() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA {
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleB
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used.",
            line = 6,
            column = 29,
        )
    }

    @Test
    fun includeModules5() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class, ModuleC::class])
                class ModuleA
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleB {
                    @Provides
                    fun dependency(): Int {
                        return 0
                    }
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleC {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency1(): String
                    fun dependency2(): Int
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used but its children `test.ModuleB`, `test.ModuleC` are used.",
            line = 6,
            column = 29,
        )
    }

    @Test
    fun includeModules6() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA {
                    @Provides
                    fun dependency(): Int {
                        return 0
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes=[ModuleC::class])
                class ModuleB
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleC {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency1(): String
                    fun dependency2(): Int
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used but its child `test.ModuleC` is used.",
            line = 6,
            column = 27,
            fileName = "test/ModuleA.java",
        )
    }

    @Test
    fun includeModules7() {
        val moduleA = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes = [ModuleB::class])
                class ModuleA {
                    @Provides
                    fun dependency(): Int {
                        return 0
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module(includes=[ModuleC::class, ModuleD::class])
                class ModuleB
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleC {
                    @Provides
                    fun dependency(): String {
                        return "string"
                    }
                }
            """.trimIndent(),
        )
        val moduleD = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class ModuleD {
                    @Provides
                    fun dependency(): Boolean {
                        return true
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test

                import dagger.Component

                @Component(modules = [ModuleA::class])
                interface MyComponent {
                    fun dependency1(): String
                    fun dependency2(): Int
                    fun dependency3(): Boolean
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC, moduleD)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used but its children `test.ModuleC`, `test.ModuleD` are used.",
            line = 6,
            column = 27,
            fileName = "test/ModuleA.java",
        )
    }
}

private fun CompilationResult.assertUnusedModules(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyComponent.java",
) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedModules",
        fileName = sourcesDir.resolve(fileName).toString(),
    )
}
