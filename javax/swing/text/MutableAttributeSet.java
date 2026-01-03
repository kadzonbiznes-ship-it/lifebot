/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.util.Enumeration;
import javax.swing.text.AttributeSet;

public interface MutableAttributeSet
extends AttributeSet {
    public void addAttribute(Object var1, Object var2);

    public void addAttributes(AttributeSet var1);

    public void removeAttribute(Object var1);

    public void removeAttributes(Enumeration<?> var1);

    public void removeAttributes(AttributeSet var1);

    public void setResolveParent(AttributeSet var1);
}

