/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class ParameterizedTypeImpl
implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private final Type ownerType;

    private ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType != null ? ownerType : rawType.getDeclaringClass();
        this.validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable<Class<?>>[] formals = this.rawType.getTypeParameters();
        if (formals.length != this.actualTypeArguments.length) {
            throw new MalformedParameterizedTypeException(String.format("Mismatch of count of formal and actual type arguments in constructor of %s: %d formal argument(s) %d actual argument(s)", this.rawType.getName(), formals.length, this.actualTypeArguments.length));
        }
        for (int i = 0; i < this.actualTypeArguments.length; ++i) {
        }
    }

    public static ParameterizedTypeImpl make(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return (Type[])this.actualTypeArguments.clone();
    }

    @Override
    public Class<?> getRawType() {
        return this.rawType;
    }

    @Override
    public Type getOwnerType() {
        return this.ownerType;
    }

    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            ParameterizedType that = (ParameterizedType)o;
            if (this == that) {
                return true;
            }
            Type thatOwner = that.getOwnerType();
            Type thatRawType = that.getRawType();
            return Objects.equals(this.ownerType, thatOwner) && Objects.equals(this.rawType, thatRawType) && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.ownerType != null) {
            sb.append(this.ownerType.getTypeName());
            sb.append("$");
            if (this.ownerType instanceof ParameterizedTypeImpl) {
                sb.append(this.rawType.getName().replace(((ParameterizedTypeImpl)this.ownerType).rawType.getName() + "$", ""));
            } else {
                sb.append(this.rawType.getSimpleName());
            }
        } else {
            sb.append(this.rawType.getName());
        }
        if (this.actualTypeArguments != null) {
            StringJoiner sj = new StringJoiner(", ", "<", ">");
            sj.setEmptyValue("");
            for (Type t : this.actualTypeArguments) {
                sj.add(t.getTypeName());
            }
            sb.append(sj.toString());
        }
        return sb.toString();
    }
}

