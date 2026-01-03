/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.visitor;

import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;

public interface TypeTreeVisitor<T> {
    public T getResult();

    public void visitFormalTypeParameter(FormalTypeParameter var1);

    public void visitClassTypeSignature(ClassTypeSignature var1);

    public void visitArrayTypeSignature(ArrayTypeSignature var1);

    public void visitTypeVariableSignature(TypeVariableSignature var1);

    public void visitWildcard(Wildcard var1);

    public void visitSimpleClassTypeSignature(SimpleClassTypeSignature var1);

    public void visitBottomSignature(BottomSignature var1);

    public void visitByteSignature(ByteSignature var1);

    public void visitBooleanSignature(BooleanSignature var1);

    public void visitShortSignature(ShortSignature var1);

    public void visitCharSignature(CharSignature var1);

    public void visitIntSignature(IntSignature var1);

    public void visitLongSignature(LongSignature var1);

    public void visitFloatSignature(FloatSignature var1);

    public void visitDoubleSignature(DoubleSignature var1);

    public void visitVoidDescriptor(VoidDescriptor var1);
}

