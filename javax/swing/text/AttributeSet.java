/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.util.Enumeration;
import javax.swing.text.StyleConstants;

public interface AttributeSet {
    public static final Object NameAttribute = StyleConstants.NameAttribute;
    public static final Object ResolveAttribute = StyleConstants.ResolveAttribute;

    public int getAttributeCount();

    public boolean isDefined(Object var1);

    public boolean isEqual(AttributeSet var1);

    public AttributeSet copyAttributes();

    public Object getAttribute(Object var1);

    public Enumeration<?> getAttributeNames();

    public boolean containsAttribute(Object var1, Object var2);

    public boolean containsAttributes(AttributeSet var1);

    public AttributeSet getResolveParent();

    public static interface ParagraphAttribute {
    }

    public static interface CharacterAttribute {
    }

    public static interface ColorAttribute {
    }

    public static interface FontAttribute {
    }
}

