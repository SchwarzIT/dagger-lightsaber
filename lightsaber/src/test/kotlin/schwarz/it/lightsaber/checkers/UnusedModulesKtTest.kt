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

internal class UnusedModulesKtTest {

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

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun moduleUsedOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    @ParameterizedTest
    @CsvSource("kapt,3,29", "ksp,5,")
    fun moduleNotUsedOnSubcomponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun moduleUsedOnSubcomponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    @ParameterizedTest
    @CsvSource("kapt,3,32", "ksp,5,")
    fun moduleUnusedOnSubcomponent2(
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

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = line,
            column = column,
            fileName = "test/MySubcomponent",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun moduleUsedOnSubcomponent2(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
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

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent {
                    fun dependency(): String
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,3,29", "ksp,5,")
    fun includeModules0(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,4,29")
    fun anvil(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test

                import com.squareup.anvil.annotations.MergeComponent
                import javax.inject.Singleton

                @MergeComponent(Singleton::class, modules = [MyModule::class])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,29", "ksp,5,")
    fun includeModules1(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,27", "ksp,7,")
    fun includeModules2(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
            fileName = "test/ModuleA",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,27", "ksp,7,")
    fun includeModules3(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
            fileName = "test/ModuleB",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,29", "ksp,5,")
    fun includeModules4(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,29", "ksp,5,")
    fun includeModules5(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,27", "ksp,7,")
    fun includeModules6(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
            fileName = "test/ModuleA",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,27", "ksp,7,")
    fun includeModules7(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
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
            line = line,
            column = column,
            fileName = "test/ModuleA",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun SuppressedUnusedModulesDoesNotReportError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test

                import dagger.Component

                @Suppress("UnusedModules")
                @Component(modules = [MyModule::class])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : AbstractCompilerArgumentConverter(Rule.UnusedModules)
}

private fun CompilationResult.assertUnusedModules(
    message: String,
    line: Int,
    column: Int?,
    fileName: String = "test/MyComponent",
) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedModules",
        fileName = sourcesDir.resolve("$fileName.${type.extension}").toString(),
    )
}
