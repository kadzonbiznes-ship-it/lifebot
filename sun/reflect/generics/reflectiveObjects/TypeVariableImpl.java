/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.reflectiveObjects;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.AnnotationType;
import sun.reflect.annotation.TypeAnnotationParser;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.misc.ReflectUtil;

public class TypeVariableImpl<D extends GenericDeclaration>
extends LazyReflectiveObjectGenerator
implements TypeVariable<D> {
    private final D genericDeclaration;
    private final String name;
    private volatile Object[] bounds;
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private TypeVariableImpl(D decl, String n, FieldTypeSignature[] bs, GenericsFactory f) {
        super(f);
        this.genericDeclaration = decl;
        this.name = n;
        this.bounds = bs;
    }

    public static <T extends GenericDeclaration> TypeVariableImpl<T> make(T decl, String name, FieldTypeSignature[] bs, GenericsFactory f) {
        if (!(decl instanceof Class || decl instanceof Method || decl instanceof Constructor)) {
            throw new AssertionError((Object)("Unexpected kind of GenericDeclaration" + decl.getClass().toString()));
        }
        return new TypeVariableImpl<T>(decl, name, bs, f);
    }

    @Override
    public Type[] getBounds() {
        Object[] value = this.bounds;
        if (value instanceof FieldTypeSignature[]) {
            this.bounds = value = this.reifyBounds((FieldTypeSignature[])value);
        }
        return (Type[])value.clone();
    }

    @Override
    public D getGenericDeclaration() {
        if (this.genericDeclaration instanceof Class) {
            ReflectUtil.checkPackageAccess((Class)this.genericDeclaration);
        } else if (this.genericDeclaration instanceof Method || this.genericDeclaration instanceof Constructor) {
            ReflectUtil.conservativeCheckMemberAccess((Member)this.genericDeclaration);
        } else {
            throw new AssertionError((Object)"Unexpected kind of GenericDeclaration");
        }
        return this.genericDeclaration;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.getName();
    }

    public boolean equals(Object o) {
        if (o instanceof TypeVariable && o.getClass() == TypeVariableImpl.class) {
            TypeVariable that = (TypeVariable)o;
            Object thatDecl = that.getGenericDeclaration();
            String thatName = that.getName();
            return Objects.equals(this.genericDeclaration, thatDecl) && Objects.equals(this.name, thatName);
        }
        return false;
    }

    public int hashCode() {
        return this.genericDeclaration.hashCode() ^ this.name.hashCode();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return (T)TypeVariableImpl.mapAnnotations(this.getAnnotations()).get(annotationClass);
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return this.getAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.getDirectlyAndIndirectlyPresent(TypeVariableImpl.mapAnnotations(this.getAnnotations()), annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return this.getAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        int myIndex = this.typeVarIndex();
        if (myIndex < 0) {
            throw new AssertionError((Object)"Index must be non-negative.");
        }
        return TypeAnnotationParser.parseTypeVariableAnnotations(this.getGenericDeclaration(), myIndex);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.getAnnotations();
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
        return TypeAnnotationParser.parseAnnotatedBounds(this.getBounds(), this.getGenericDeclaration(), this.typeVarIndex());
    }

    private int typeVarIndex() {
        TypeVariable<?>[] tVars = this.getGenericDeclaration().getTypeParameters();
        int i = -1;
        for (TypeVariable<?> v : tVars) {
            ++i;
            if (!this.equals(v)) continue;
            return i;
        }
        return -1;
    }

    private static Map<Class<? extends Annotation>, Annotation> mapAnnotations(Annotation[] annos) {
        LinkedHashMap<Class<? extends Annotation>, Annotation> result = new LinkedHashMap<Class<? extends Annotation>, Annotation>();
        for (Annotation a : annos) {
            Class<? extends Annotation> klass = a.annotationType();
            AnnotationType type = AnnotationType.getInstance(klass);
            if (type.retention() != RetentionPolicy.RUNTIME || result.put(klass, a) == null) continue;
            throw new AnnotationFormatError("Duplicate annotation for class: " + klass + ": " + a);
        }
        return result;
    }
}

