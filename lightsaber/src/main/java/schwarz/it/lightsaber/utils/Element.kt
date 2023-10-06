package schwarz.it.lightsaber.utils

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

internal fun <T : Annotation> Element.isAnnotatedWith(klass: KClass<T>): Boolean {
    return getAnnotation(klass.java) != null
}

internal fun TypeElement.findAnnotationMirrors(annotationName: String): AnnotationMirror? {
    return annotationMirrors.singleOrNull { annotationMirror ->
        annotationMirror.annotationType.asElement().simpleName.toString() == annotationName
    }
}

internal fun AnnotationMirror.getAnnotationValue(key: String): AnnotationValue {
    return elementValues.toList().single { (it, _) -> it.simpleName.toString() == key }.second
}

internal fun Element.getMethods(): List<ExecutableElement> {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }
        .mapNotNull { it as? ExecutableElement }
}
