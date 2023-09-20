package schwarz.it.lightsaber.checkers

import com.google.testing.compile.Compilation
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.ReportType
import schwarz.it.lightsaber.assertHasFinding
import schwarz.it.lightsaber.assertNoFindings
import schwarz.it.lightsaber.createCompiler
import schwarz.it.lightsaber.createSource

class UnusedModulesKtTest {

    private val compiler = createCompiler(unusedModules = ReportType.Error)

    private val module = createSource(
        """
            package test;

            import dagger.Module;
            import dagger.Provides;

            @Module
            public class MyModule {
                @Provides
                String dependency() {
                    return "string";
                }
            }
        """.trimIndent(),
    )

    @Test
    fun moduleUsedOnComponent() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    String dependency();
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
                package test;

                import dagger.Component;

                @Component(modules = {MyModule.class})
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
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 5,
            column = 22,
        )
    }

    @Test
    fun moduleUsedOnSubcomponent() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {MyModule.class})
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

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertNoFindings()
    }

    @Test
    fun moduleUnusedOnSubcomponent2() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component
                public interface MyComponent {
                    MySubcomponent subcomponent();
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test;

                import dagger.Subcomponent;

                @Subcomponent(modules = {MyModule.class})
                public interface MySubcomponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 5,
            column = 25,
            fileName = "test/MySubcomponent.java",
        )
    }

    @Test
    fun moduleUsedOnSubcomponent2() {
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component
                public interface MyComponent {
                    MySubcomponent subcomponent();
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test;

                import dagger.Subcomponent;

                @Subcomponent(modules = {MyModule.class})
                public interface MySubcomponent {
                    String dependency();
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
                package test;

                import dagger.Component;

                @Component(modules = {MyModule.class})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedModules(
            message = "The @Module `test.MyModule` is not used.",
            line = 5,
            column = 22,
        )
    }

    @Test
    fun includeModules1() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleB {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used but its child `test.ModuleB` is used.",
            line = 5,
            column = 22,
        )
    }

    @Test
    fun includeModules2() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleB {
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)
        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used.",
            line = 6,
            column = 20,
            fileName = "test/ModuleA.java"
        )
    }

    @Test
    fun includeModules3() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleC.class})
                public class ModuleB {
                    @Provides
                    Integer intDependency() {
                        return 0;
                    }
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleC {
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency();
                    Integer intDependency();
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleC` included by `test.ModuleA → test.ModuleB` is not used.",
            line = 6,
            column = 20,
            fileName = "test/ModuleB.java"
        )
    }

    @Test
    fun includeModules4() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleB {
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used.",
            line = 5,
            column = 22,
        )
    }

    @Test
    fun includeModules5() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class, ModuleC.class})
                public class ModuleA {
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleB {
                    @Provides
                    int dependency() {
                        return 0;
                    }
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleC {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency1();
                    int dependency2();
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleA` is not used but its children `test.ModuleB`, `test.ModuleC` are used.",
            line = 5,
            column = 22,
        )
    }

    @Test
    fun includeModules6() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                    @Provides
                    int dependency() {
                        return 0;
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes={ModuleC.class})
                public class ModuleB {
                    
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleC {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency1();
                    int dependency2();
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used but its child `test.ModuleC` is used.",
            line = 6,
            column = 20,
            fileName = "test/ModuleA.java"
        )
    }

    @Test
    fun includeModules7() {
        val moduleA = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes = {ModuleB.class})
                public class ModuleA {
                    @Provides
                    int dependency() {
                        return 0;
                    }
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module(includes={ModuleC.class, ModuleD.class})
                public class ModuleB {
                    
                }
            """.trimIndent(),
        )
        val moduleC = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleC {
                    @Provides
                    String dependency() {
                        return "string";
                    }
                }
            """.trimIndent(),
        )
        val moduleD = createSource(
            """
                package test;

                import dagger.Module;
                import dagger.Provides;

                @Module
                public class ModuleD {
                    @Provides
                    Boolean dependency() {
                        return true;
                    }
                }
            """.trimIndent(),
        )
        val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {ModuleA.class})
                public interface MyComponent {
                    String dependency1();
                    int dependency2();
                    Boolean dependency3();
                    
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB, moduleC, moduleD)

        compilation.assertUnusedModules(
            message = "The @Module `test.ModuleB` included by `test.ModuleA` is not used but its children `test.ModuleC`, `test.ModuleD` are used.",
            line = 6,
            column = 20,
            fileName = "test/ModuleA.java"
        )
    }
}

private fun Compilation.assertUnusedModules(
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
        fileName = fileName,
    )
}
