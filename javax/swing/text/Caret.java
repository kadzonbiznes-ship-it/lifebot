/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Point;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

public interface Caret {
    public void install(JTextComponent var1);

    public void deinstall(JTextComponent var1);

    public void paint(Graphics var1);

    public void addChangeListener(ChangeListener var1);

    public void removeChangeListener(ChangeListener var1);

    public boolean isVisible();

    public void setVisible(boolean var1);

    public boolean isSelectionVisible();

    public void setSelectionVisible(boolean var1);

    public void setMagicCaretPosition(Point var1);

    public Point getMagicCaretPosition();

    public void setBlinkRate(int var1);

    public int getBlinkRate();

    public int getDot();

    public int getMark();

    public void setDot(int var1);

    public void moveDot(int var1);
}

