package schwarz.it.lightsaber.sample;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = {MyModule.class})
public interface MyComponent {
    Integer myInt();
}
