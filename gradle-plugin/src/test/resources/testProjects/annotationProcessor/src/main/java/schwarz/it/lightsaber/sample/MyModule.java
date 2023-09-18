package schwarz.it.lightsaber.sample;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MyModule {

    @Provides
    static Integer myInt() {
        return 42;
    }

    @Provides
    static Long myLong() {
        return 42L;
    }
}
