/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.generics.visitor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import sun.reflect.generics.factory.GenericsFactory;
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
import sun.reflect.generics.tree.TypeArgument;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;
import sun.reflect.generics.visitor.TypeTreeVisitor;

public class Reifier
implements TypeTreeVisitor<Type> {
    private Type resultType;
    private final GenericsFactory factory;

    private Reifier(GenericsFactory f) {
        this.factory = f;
    }

    private GenericsFactory getFactory() {
        return this.factory;
    }

    public static Reifier make(GenericsFactory f) {
        return new Reifier(f);
    }

    private Type[] reifyTypeArguments(TypeArgument[] tas) {
        Type[] ts = new Type[tas.length];
        for (int i = 0; i < tas.length; ++i) {
            tas[i].accept(this);
            ts[i] = this.resultType;
        }
        return ts;
    }

    @Override
    public Type getResult() {
        assert (this.resultType != null);
        return this.resultType;
    }

    @Override
    public void visitFormalTypeParameter(FormalTypeParameter ftp) {
        this.resultType = this.getFactory().makeTypeVariable(ftp.getName(), ftp.getBounds());
    }

    @Override
    public void visitClassTypeSignature(ClassTypeSignature ct) {
        List<SimpleClassTypeSignature> scts = ct.getPath();
        assert (!scts.isEmpty());
        Iterator<SimpleClassTypeSignature> iter = scts.iterator();
        SimpleClassTypeSignature sc = iter.next();
        StringBuilder n = new StringBuilder(sc.getName());
        boolean dollar = sc.getDollar();
        while (iter.hasNext() && sc.getTypeArguments().length == 0) {
            sc = iter.next();
            dollar = sc.getDollar();
            n.append(dollar ? "$" : ".").append(sc.getName());
        }
        assert (!iter.hasNext() || sc.getTypeArguments().length > 0);
        Type c = this.getFactory().makeNamedType(n.toString());
        if (sc.getTypeArguments().length == 0) {
            assert (!iter.hasNext());
            this.resultType = c;
        } else {
            assert (sc.getTypeArguments().length > 0);
            Type[] pts = this.reifyTypeArguments(sc.getTypeArguments());
            ParameterizedType owner = this.getFactory().makeParameterizedType(c, pts, null);
            dollar = false;
            while (iter.hasNext()) {
                sc = iter.next();
                dollar = sc.getDollar();
                n.append(dollar ? "$" : ".").append(sc.getName());
                c = this.getFactory().makeNamedType(n.toString());
                pts = this.reifyTypeArguments(sc.getTypeArguments());
                owner = this.getFactory().makeParameterizedType(c, pts, owner);
            }
            this.resultType = owner;
        }
    }

    @Override
    public void visitArrayTypeSignature(ArrayTypeSignature a) {
        a.getComponentType().accept(this);
        Type ct = this.resultType;
        this.resultType = this.getFactory().makeArrayType(ct);
    }

    @Override
    public void visitTypeVariableSignature(TypeVariableSignature tv) {
        this.resultType = this.getFactory().findTypeVariable(tv.getIdentifier());
    }

    @Override
    public void visitWildcard(Wildcard w) {
        this.resultType = this.getFactory().makeWildcard(w.getUpperBounds(), w.getLowerBounds());
    }

    @Override
    public void visitSimpleClassTypeSignature(SimpleClassTypeSignature sct) {
        this.resultType = this.getFactory().makeNamedType(sct.getName());
    }

    @Override
    public void visitBottomSignature(BottomSignature b) {
    }

    @Override
    public void visitByteSignature(ByteSignature b) {
        this.resultType = this.getFactory().makeByte();
    }

    @Override
    public void visitBooleanSignature(BooleanSignature b) {
        this.resultType = this.getFactory().makeBool();
    }

    @Override
    public void visitShortSignature(ShortSignature s) {
        this.resultType = this.getFactory().makeShort();
    }

    @Override
    public void visitCharSignature(CharSignature c) {
        this.resultType = this.getFactory().makeChar();
    }

    @Override
    public void visitIntSignature(IntSignature i) {
        this.resultType = this.getFactory().makeInt();
    }

    @Override
    public void visitLongSignature(LongSignature l) {
        this.resultType = this.getFactory().makeLong();
    }

    @Override
    public void visitFloatSignature(FloatSignature f) {
        this.resultType = this.getFactory().makeFloat();
    }

    @Override
    public void visitDoubleSignature(DoubleSignature d) {
        this.resultType = this.getFactory().makeDouble();
    }

    @Override
    public void visitVoidDescriptor(VoidDescriptor v) {
        this.resultType = this.getFactory().makeVoid();
    }
}

