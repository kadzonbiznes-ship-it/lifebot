/*
 * Decompiled with CFR 0.152.
 */
package javax.print.attribute;

import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;

public interface PrintRequestAttributeSet
extends AttributeSet {
    @Override
    public boolean add(Attribute var1);

    @Override
    public boolean addAll(AttributeSet var1);
}

