/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;

public interface StyledDocument
extends Document {
    public Style addStyle(String var1, Style var2);

    public void removeStyle(String var1);

    public Style getStyle(String var1);

    public void setCharacterAttributes(int var1, int var2, AttributeSet var3, boolean var4);

    public void setParagraphAttributes(int var1, int var2, AttributeSet var3, boolean var4);

    public void setLogicalStyle(int var1, Style var2);

    public Style getLogicalStyle(int var1);

    public Element getParagraphElement(int var1);

    public Element getCharacterElement(int var1);

    public Color getForeground(AttributeSet var1);

    public Color getBackground(AttributeSet var1);

    public Font getFont(AttributeSet var1);
}

