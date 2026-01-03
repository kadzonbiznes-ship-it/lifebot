/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;

public interface Element {
    public Document getDocument();

    public Element getParentElement();

    public String getName();

    public AttributeSet getAttributes();

    public int getStartOffset();

    public int getEndOffset();

    public int getElementIndex(int var1);

    public int getElementCount();

    public Element getElement(int var1);

    public boolean isLeaf();
}

