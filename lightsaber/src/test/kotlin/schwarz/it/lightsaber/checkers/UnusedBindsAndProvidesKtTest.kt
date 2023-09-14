package schwarz.it.lightsaber.checkers

import com.google.testing.compile.Compilation
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.FindingInfo
import schwarz.it.lightsaber.assertHasFinding
import schwarz.it.lightsaber.assertHasFindings
import schwarz.it.lightsaber.assertNoFindings
import schwarz.it.lightsaber.createCompiler
import schwarz.it.lightsaber.createSource

class UnusedBindsAndProvidesKtTest {

    private val compiler = createCompiler(checkUnusedBindsAndProvides = true)

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

        compilation.assertUnusedBindsAndProvides(
            message = "The @Binds `bindsMyInts` declared on `test.MyModule` is not used.",
            line = 12,
            column = 28,
        )
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

        compilation.assertUnusedBindsAndProvides(
            message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
            line = 17,
            column = 19,
        )
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

        compilation.assertUnusedBindsAndProvides(
            message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
            line = 17,
            column = 19,
        )
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

        compilation.assertHasFindings(
            FindingInfo(
                message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
                line = 17,
                column = 19,
                ruleName = "UnusedBindsAndProvides",
                fileName = "test/MyModule.java",
            ),
            FindingInfo(
                message = "The @Provides `providesMyInts` declared on `test.MyModule` is not used.",
                line = 12,
                column = 31,
                ruleName = "UnusedBindsAndProvides",
                fileName = "test/MyModule.java",
            ),
        )
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

        compilation.assertNoFindings()
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

        compilation.assertUnusedBindsAndProvides(
            message = "The @Binds `bindsMyInts` declared on `test.MyModuleB` is not used.",
            line = 12,
            column = 28,
            fileName = "test/MyModuleB.java",
        )
    }

    @Test
    fun testMultibinding() {
        val component = createSource(
            """
                package test;
                
                import dagger.Component;
                import java.util.Set;
                
                @Component(modules = {MyModuleA.class})
                public interface MyComponent {
                    Set<Number> myInts();
                }
            """.trimIndent(),
        )
        val moduleA = createSource(
            """
                package test;
                
                import dagger.Binds;import dagger.Module;
                import dagger.Provides;
                import dagger.multibindings.IntoSet;
                                
                @Module
                public abstract class MyModuleA {
                        
                    @Binds
                    @IntoSet
                    public abstract Number bindLong(Integer impl);
                    
                    @Binds
                    @IntoSet
                    public abstract Number bindLong2(Long impl);
                    
                    @Provides
                    static Integer providesMyInt() {
                        return 1;
                    }
                    
                    @Provides
                    static Long providesMyLong2() {
                        return 2L;
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA)

        compilation.assertNoFindings()
    }

    @Test
    fun unusedInCompanion() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                import schwarz.it.lightsaber.sample.MyModule;
                
                @Component(modules = {MyModule.class})
                public interface MyComponent {
                    Integer myInt();
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package schwarz.it.lightsaber.sample;
                
                @dagger.Module
                @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\b\'\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lschwarz/it/lightsaber/sample/MyModule;", "", "()V", "Companion", "sample"})
                public abstract class MyModule {
                    @org.jetbrains.annotations.NotNull
                    public static final schwarz.it.lightsaber.sample.MyModule.Companion Companion = null;
                    
                    public MyModule() {
                        super();
                    }
                    
                    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u0007"}, d2 = {"Lschwarz/it/lightsaber/sample/MyModule${'$'}Companion;", "", "()V", "myInt", "", "myLong", "", "sample"})
                    public static final class Companion {
                        
                        private Companion() {
                            super();
                        }
                        
                        @dagger.Provides
                        public final int myInt() {
                            return 0;
                        }
                        
                        @dagger.Provides
                        public final long myLong() {
                            return 0L;
                        }
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedBindsAndProvides(
            message = "The @Provides `myLong` declared on `schwarz.it.lightsaber.sample.MyModule` is not used.",
            line = 26,
            column = 27,
            fileName = "schwarz/it/lightsaber/sample/MyModule.java",
        )
    }
}

private fun Compilation.assertUnusedBindsAndProvides(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyModule.java",
) {
    assertHasFinding(message, line, column, fileName, "UnusedBindsAndProvides")
}
