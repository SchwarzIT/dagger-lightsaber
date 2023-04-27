package es.lidlplus.libs.lightsaber.checkers

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import dagger.internal.codegen.ComponentProcessor
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.createCompiler
import es.lidlplus.libs.lightsaber.createSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.MyModule` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.MyModule` is not used. [test.MyComponent → test.MySubcomponent]")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.MyModule` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleA` is not used but its child `test.ModuleB` is used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleB` included by `test.ModuleA` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleC` included by `test.ModuleA → test.ModuleB` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleA` is not used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleA` is not used but its children `test.ModuleB`, `test.ModuleC` are used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleB` included by `test.ModuleA` is not used but its child `test.ModuleC` is used.")
            .inFile(component)
            .onLineContaining("MyComponent")
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

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Module `test.ModuleB` included by `test.ModuleA` is not used but its children `test.ModuleC`, `test.ModuleD` are used.")
            .inFile(component)
            .onLineContaining("MyComponent")
    }

    @Nested
    internal inner class ReportTypes {
        private val component = createSource(
            """
                package test;

                import dagger.Component;

                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    MySubcomponent subcomponent();
                }
            """.trimIndent(),
        )
        private val subcomponent = createSource(
            """
                package test;

                import dagger.Subcomponent;

                @Subcomponent
                public interface MySubcomponent {
                }
            """.trimIndent(),
        )

        @Test
        fun testError() {
            val compilation = createCompiler(unusedModules = ReportType.Error)
                .compile(component, subcomponent, module)

            CompilationSubject.assertThat(compilation)
                .hadErrorCount(1)
        }

        @Test
        fun testWarning() {
            val compilation = createCompiler(unusedModules = ReportType.Warning)
                .compile(component, subcomponent, module)

            CompilationSubject.assertThat(compilation)
                .hadWarningCount(1)
        }

        @Test
        fun testIgnore() {
            val compilation = createCompiler(unusedModules = ReportType.Ignore)
                .compile(component, subcomponent, module)

            CompilationSubject.assertThat(compilation)
                .succeededWithoutWarnings()
        }
    }
}
