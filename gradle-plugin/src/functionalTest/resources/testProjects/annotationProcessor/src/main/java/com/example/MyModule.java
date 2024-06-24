package com.example;

import dagger.Module;
import dagger.Provides;

@Module
abstract class MyModule {

    @Provides
    static int myInt() {
        return 42;
    }

    @Provides
    static long myLong() {
        return 42L;
    }

    @Provides
    static Foo foo() {
        return new Foo();
    }
}
