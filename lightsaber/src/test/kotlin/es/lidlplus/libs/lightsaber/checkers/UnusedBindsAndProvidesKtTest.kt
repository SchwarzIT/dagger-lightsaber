package es.lidlplus.libs.lightsaber.checkers

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import dagger.internal.codegen.ComponentProcessor
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.createCompiler
import es.lidlplus.libs.lightsaber.createSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnusedBindsAndProvidesKtTest {

    private val compiler = createCompiler(unusedBindsAndProvides = ReportType.Error)
    
    @Test
    fun bindsNotUsed() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                import java.util.ArrayList;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Binds
                    abstract List<Integer> bindsMyInts(ArrayList<Integer> impl);
                    
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Binds `bindsMyInts` declared on `test.MyModule` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
    }

    @Test
    fun providesNotUsed() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                import java.util.ArrayList;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                    
                    @Provides
                    static String providesMyString() {
                        return "Hello there!";
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Provides `providesMyString` declared on `test.MyModule` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
    }

    @Test
    fun providesNotUsedReportedOnSubcomponent() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
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
                import java.util.ArrayList;

                @Subcomponent(modules = {MyModule.class})
                public interface MySubcomponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                    
                    @Provides
                    static String providesMyString() {
                        return "Hello there!";
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Provides `providesMyString` declared on `test.MyModule` is not used. [test.MyComponent → test.MySubcomponent]")
            .inFile(component)
    }

    @Test
    fun providesNotUsedReportedOnSubcomponent2() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
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
                import java.util.ArrayList;

                @Subcomponent(modules = {MyModule.class})
                public interface MySubcomponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        val component2 = createSource(
            """
                package test;
                
                import dagger.Component;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent2 { 
                    String myString();
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                    
                    @Provides
                    static String providesMyString() {
                        return "Hello there!";
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, component2, module)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(2)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Provides `providesMyString` declared on `test.MyModule` is not used. [test.MyComponent → test.MySubcomponent]")
            .inFile(component)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Provides `providesMyInts` declared on `test.MyModule` is not used.")
            .inFile(component2)
    }

    @Test
    fun componentWithInterface() {
        val component = createSource(
            """
                package test;
                
                import dagger.Component;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent extends MyComponentInterface {
                }
            """.trimIndent(),
        )

        val componentInterface = createSource(
            """
                package test;
                
                import java.util.ArrayList;
                
                public interface MyComponentInterface {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )

        val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }                
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, componentInterface, module)

        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings()
    }

    @Test
    fun bindsNotUsedInAChildModule() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                import java.util.ArrayList;
                
                @Component(modules = {MyModuleA.class})
                public interface MyComponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModuleB {
                    @Binds
                    abstract List<Integer> bindsMyInts(ArrayList<Integer> impl);
                    
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                }
            """.trimIndent(),
        )

        val moduleA = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module(includes = { MyModuleB.class })
                public abstract class MyModuleA {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        CompilationSubject.assertThat(compilation)
            .hadErrorCount(1)
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("The @Binds `bindsMyInts` declared on `test.MyModuleB` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
    }

    @Nested
    internal inner class ReportTypes {
        private val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                import java.util.ArrayList;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    ArrayList<Integer> myInts();
                }
            """.trimIndent(),
        )
        private val module = createSource(
            """
                package test;
                
                import dagger.Binds;
                import dagger.Module;
                import dagger.Provides;
                import java.util.ArrayList;
                import java.util.List;
                
                @Module
                public abstract class MyModule {
                    @Binds
                    abstract List<Integer> bindsMyInts(ArrayList<Integer> impl);
                    
                    @Provides
                    static ArrayList<Integer> providesMyInts() {
                        return new ArrayList<>();
                    }
                }
            """.trimIndent(),
        )

        @Test
        fun testError() {
            val compilation = createCompiler(unusedBindsAndProvides = ReportType.Error)
                .compile(component, module)

            CompilationSubject.assertThat(compilation)
                .hadErrorCount(1)
        }

        @Test
        fun testWarning() {
            val compilation = createCompiler(unusedBindsAndProvides = ReportType.Warning)
                .compile(component, module)

            CompilationSubject.assertThat(compilation)
                .hadWarningCount(1)
        }

        @Test
        fun testIgnore() {
            val compilation = createCompiler(unusedBindsAndProvides = ReportType.Ignore)
                .compile(component, module)

            CompilationSubject.assertThat(compilation)
                .succeededWithoutWarnings()
        }
    }
}
