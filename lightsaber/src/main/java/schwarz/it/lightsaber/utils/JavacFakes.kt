package schwarz.it.lightsaber.utils

import java.io.Writer
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.NoType
import javax.lang.model.type.NullType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

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

object KspTypes : Types {
    override fun asElement(t: TypeMirror?): Element {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isSameType(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isSubtype(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isAssignable(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun contains(t1: TypeMirror?, t2: TypeMirror?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun isSubsignature(m1: ExecutableType?, m2: ExecutableType?): Boolean {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun directSupertypes(t: TypeMirror?): MutableList<out TypeMirror> {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun erasure(t: TypeMirror?): TypeMirror {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun boxedClass(p: PrimitiveType?): TypeElement {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun unboxedType(t: TypeMirror?): PrimitiveType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun capture(t: TypeMirror?): TypeMirror {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getPrimitiveType(kind: TypeKind?): PrimitiveType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getNullType(): NullType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getNoType(kind: TypeKind?): NoType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getArrayType(componentType: TypeMirror?): ArrayType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getWildcardType(extendsBound: TypeMirror?, superBound: TypeMirror?): WildcardType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getDeclaredType(typeElem: TypeElement?, vararg typeArgs: TypeMirror?): DeclaredType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun getDeclaredType(
        containing: DeclaredType?,
        typeElem: TypeElement?,
        vararg typeArgs: TypeMirror?,
    ): DeclaredType {
        error("You shouldn't be using this when the backend is ksp")
    }

    override fun asMemberOf(containing: DeclaredType?, element: Element?): TypeMirror {
        error("You shouldn't be using this when the backend is ksp")
    }
}
