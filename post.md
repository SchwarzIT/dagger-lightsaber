# Erase una vez
La aplicación de Lidl Plus está compuesta por unos 250 modulos los cuales usan dagger para la inyección interna y también para conectarse entre ellos. Por como es dagger, es muy fácil olvidarte de eliminar un `@Provides` en un `@Module` cuando deja de ser usado, y dagger nunca se queja de ello.

Esto hace que tengamos codigo muerto en nuestro proyecto. Ninguna de las herramientas que usamos nos detecta este problema (ktlint, detekt, gradle dependency analysis y los checks del propio IntelliJ). Además como el `@Provides` apunta a un Caso de Uso este tampoco es detectado como código muerto lo cual nos genera tener "arboles" de código muerto. Y, por otro lado, como utilizamos dagger para conectar modulos entre si muchas veces estos `@Provides` hacen que parezca que nos hace falta depender entre dos modulos que realmente no son necesarios.

Queremos buscarle solución a estos problemas. Nos pusismos a buscar diferentes opciones pero no encontramos ninguna herramienta que nos ayudara a detectarlos. Por esto, decidimos implementarla nosotros mismos. En SCRM tenemos los viernes para poder hacer investigación y mejoras tech así que nos pusimos manos a la obra. Investigar las tripas de Dagger, crear un Plugin de Gradle, conectarse a las diferentes plugins de compilación (ksp, kapt and javac annotation processor). Era un reto muy interesante.

Y así nació lightsaber

## Lightsaber
Si conoces Dagger2 sabras que diferentes herramientas alrededor de ella usan nombres "relacionados" con "dagger": `Anvil`, `scabbard`, etc. Por lo que una herramienta que encuentra código muerto ¿Cómo se podría llamar? Una herramienta que arroja luz al código muerto... luz... lighsaber! DONE! ya tenemos un nombre, una de las cosas más dificiles de hacer en informatica 🎉.

Lightsaber is a Dagger 2 plugin that detects unused code in your Modules, Components and Subcomponents.

Actualmente tiene 6 reglas que detectan los siguientes problemas
- ...
- ...

La mayor parte del tiempo de desarrollo lo hemos dedicado a conseguir que los mensajes de error sean muy claros (aunque comparado con dagger es un reto muy facil de conseguir).

## Como usuarlo en tu proyecto

Es MUY fácil. Lightsaber viene con un plugin de gradle que se encarga de configurar todo. Simplemente tienes que añadir este plugin a cada modulo donde quieras usar lightsaber:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.schwarzit.lightsaber") version "<version>"
}
```

Y ejecutar la tarea `./gradlew lightsaberCheck`.

Cuando lo ejecutes, si lighsaber detecta algún tipo de código muerto en tu configuración de dagger la tarea te fallará mostrandote un error como el siguiente:

```
/path/module/com/example/MyComponent.java:6:8: e: The @BindsInstance `myInt` declared in `test.MyComponent` is not used. [UnusedBindInstance]
```

En este ejemplo nos indica que ha detectado un `@BindsInstance` en la linea 6, columna 8 del archivo `MyComponent.kt` que no se usa y por lo tanto se podría eliminar.

A día de hoy, lightsaber no tiene la opción de arreglar automaticamente los errores, simplemente genera reportes con los errores detectados.

Además, si alguna de las reglas no te gusta o prefieres que se trate como un warning en lugar de un error, puedes hacerlo:

```
// build.gradle.kts
lightsaber {
  emptyComponent = Severity.Error // Warning or Ignore
  unusedBindInstance = Severity.Error
  unusedBindsAndProvides = Severity.Error
  unusedDependencies = Severity.Error
  unusedMembersInjection = Severity.Error
  unusedModules = Severity.Error
}
```
Por defecto todas las reglas están activadas y en modo error.

## Resultados
Tras aplicar lightsaber en nuestro proyecto conseguimos los siguientes resultados:

- ~2.000 lineas código eliminadas directamente
- ~5.000 lineas de código detectadas después de eliminar todo el código muerto detectado por lightsaber
- ~70 dependencias entre modulos eliminadas (Esto nos ha ayudado mucho a mejorar la paralelización del proyecto y reducir las recompilaciones innecesarias)
- Herramienta añadida a nuestro `CI` para evitar que nuevos casos vuelvan a aparecer.
- Esta es una herramienta que ayuda a borrar código. A todos nos encanta borrar código :)
