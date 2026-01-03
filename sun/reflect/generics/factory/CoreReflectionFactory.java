/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.factory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;
import sun.reflect.generics.scope.Scope;
import sun.reflect.generics.tree.FieldTypeSignature;

public class CoreReflectionFactory
implements GenericsFactory {
    private final GenericDeclaration decl;
    private final Scope scope;

    private CoreReflectionFactory(GenericDeclaration d, Scope s) {
        this.decl = d;
        this.scope = s;
    }

    private GenericDeclaration getDecl() {
        return this.decl;
    }

    private Scope getScope() {
        return this.scope;
    }

    private ClassLoader getDeclsLoader() {
        if (this.decl instanceof Class) {
            return ((Class)this.decl).getClassLoader();
        }
        if (this.decl instanceof Method) {
            return ((Method)this.decl).getDeclaringClass().getClassLoader();
        }
        assert (this.decl instanceof Constructor) : "Constructor expected";
        return ((Constructor)this.decl).getDeclaringClass().getClassLoader();
    }

    public static CoreReflectionFactory make(GenericDeclaration d, Scope s) {
        return new CoreReflectionFactory(d, s);
    }

    @Override
    public TypeVariable<?> makeTypeVariable(String name, FieldTypeSignature[] bounds) {
        return TypeVariableImpl.make(this.getDecl(), name, bounds, this);
    }

    @Override
    public WildcardType makeWildcard(FieldTypeSignature[] ubs, FieldTypeSignature[] lbs) {
        return WildcardTypeImpl.make(ubs, lbs, this);
    }

    @Override
    public ParameterizedType makeParameterizedType(Type declaration, Type[] typeArgs, Type owner) {
        return ParameterizedTypeImpl.make((Class)declaration, typeArgs, owner);
    }

    @Override
    public TypeVariable<?> findTypeVariable(String name) {
        return this.getScope().lookup(name);
    }

    @Override
    public Type makeNamedType(String name) {
        try {
            return Class.forName(name, false, this.getDeclsLoader());
        }
        catch (ClassNotFoundException c) {
            throw new TypeNotPresentException(name, c);
        }
    }

    @Override
    public Type makeArrayType(Type componentType) {
        if (componentType instanceof Class) {
            return Array.newInstance((Class)componentType, 0).getClass();
        }
        return GenericArrayTypeImpl.make(componentType);
    }

    @Override
    public Type makeByte() {
        return Byte.TYPE;
    }

    @Override
    public Type makeBool() {
        return Boolean.TYPE;
    }

    @Override
    public Type makeShort() {
        return Short.TYPE;
    }

    @Override
    public Type makeChar() {
        return Character.TYPE;
    }

    @Override
    public Type makeInt() {
        return Integer.TYPE;
    }

    @Override
    public Type makeLong() {
        return Long.TYPE;
    }

    @Override
    public Type makeFloat() {
        return Float.TYPE;
    }

    @Override
    public Type makeDouble() {
        return Double.TYPE;
    }

    @Override
    public Type makeVoid() {
        return Void.TYPE;
    }
}

