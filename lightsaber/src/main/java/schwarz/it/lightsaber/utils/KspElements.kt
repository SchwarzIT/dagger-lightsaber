package schwarz.it.lightsaber.utils

import java.io.Writer
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

object KspElements : Elements {
    override fun getPackageElement(name: CharSequence?): PackageElement {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getTypeElement(name: CharSequence?): TypeElement {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getElementValuesWithDefaults(a: AnnotationMirror?): MutableMap<out ExecutableElement, out AnnotationValue> {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getDocComment(e: Element?): String {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isDeprecated(e: Element?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getBinaryName(type: TypeElement?): Name {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getPackageOf(e: Element?): PackageElement {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getAllMembers(type: TypeElement?): MutableList<out Element> {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getAllAnnotationMirrors(e: Element?): MutableList<out AnnotationMirror> {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun hides(hider: Element?, hidden: Element?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun overrides(
        overrider: ExecutableElement?,
        overridden: ExecutableElement?,
        type: TypeElement?,
    ): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getConstantExpression(value: Any?): String {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun printElements(w: Writer?, vararg elements: Element?) {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getName(cs: CharSequence?): Name {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isFunctionalInterface(type: TypeElement?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }
}
