/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import sun.reflect.annotation.AnnotationParser;

public class AnnotationType {
    private final Map<String, Class<?>> memberTypes;
    private final Map<String, Object> memberDefaults;
    private final Map<String, Method> members;
    private final RetentionPolicy retention;
    private final boolean inherited;

    public static AnnotationType getInstance(Class<? extends Annotation> annotationClass) {
        JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
        AnnotationType result = jla.getAnnotationType(annotationClass);
        if (result == null && !jla.casAnnotationType(annotationClass, null, result = new AnnotationType(annotationClass))) {
            result = jla.getAnnotationType(annotationClass);
            assert (result != null);
        }
        return result;
    }

    private AnnotationType(final Class<? extends Annotation> annotationClass) {
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type");
        }
        Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>(this){

            @Override
            public Method[] run() {
                return annotationClass.getDeclaredMethods();
            }
        });
        this.memberTypes = new HashMap(methods.length + 1, 1.0f);
        this.memberDefaults = new HashMap<String, Object>(0);
        this.members = new HashMap<String, Method>(methods.length + 1, 1.0f);
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isAbstract(method.getModifiers()) || method.isSynthetic()) continue;
            if (method.getParameterCount() != 0) {
                throw new IllegalArgumentException(method + " has params");
            }
            String name = method.getName();
            Class<?> type = method.getReturnType();
            this.memberTypes.put(name, AnnotationType.invocationHandlerReturnType(type));
            this.members.put(name, method);
            Object defaultValue = method.getDefaultValue();
            if (defaultValue == null) continue;
            this.memberDefaults.put(name, defaultValue);
        }
        if (annotationClass != Retention.class && annotationClass != Inherited.class) {
            JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
            Map<Class<? extends Annotation>, Annotation> metaAnnotations = AnnotationParser.parseSelectAnnotations(jla.getRawClassAnnotations(annotationClass), jla.getConstantPool(annotationClass), annotationClass, Retention.class, Inherited.class);
            Retention ret = (Retention)metaAnnotations.get(Retention.class);
            this.retention = ret == null ? RetentionPolicy.CLASS : ret.value();
            this.inherited = metaAnnotations.containsKey(Inherited.class);
        } else {
            this.retention = RetentionPolicy.RUNTIME;
            this.inherited = false;
        }
    }

    public static Class<?> invocationHandlerReturnType(Class<?> type) {
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        return type;
    }

    public Map<String, Class<?>> memberTypes() {
        return this.memberTypes;
    }

    public Map<String, Method> members() {
        return this.members;
    }

    public Map<String, Object> memberDefaults() {
        return this.memberDefaults;
    }

    public RetentionPolicy retention() {
        return this.retention;
    }

    public boolean isInherited() {
        return this.inherited;
    }

    public String toString() {
        return "Annotation Type:\n   Member types: " + this.memberTypes + "\n   Member defaults: " + this.memberDefaults + "\n   Retention policy: " + (Object)((Object)this.retention) + "\n   Inherited: " + this.inherited;
    }
}

