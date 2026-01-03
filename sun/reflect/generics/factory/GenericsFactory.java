/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.factory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import sun.reflect.generics.tree.FieldTypeSignature;

public interface GenericsFactory {
    public TypeVariable<?> makeTypeVariable(String var1, FieldTypeSignature[] var2);

    public ParameterizedType makeParameterizedType(Type var1, Type[] var2, Type var3);

    public TypeVariable<?> findTypeVariable(String var1);

    public WildcardType makeWildcard(FieldTypeSignature[] var1, FieldTypeSignature[] var2);

    public Type makeNamedType(String var1);

    public Type makeArrayType(Type var1);

    public Type makeByte();

    public Type makeBool();

    public Type makeShort();

    public Type makeChar();

    public Type makeInt();

    public Type makeLong();

    public Type makeFloat();

    public Type makeDouble();

    public Type makeVoid();
}

