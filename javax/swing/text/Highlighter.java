/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

public interface Highlighter {
    public void install(JTextComponent var1);

    public void deinstall(JTextComponent var1);

    public void paint(Graphics var1);

    public Object addHighlight(int var1, int var2, HighlightPainter var3) throws BadLocationException;

    public void removeHighlight(Object var1);

    public void removeAllHighlights();

    public void changeHighlight(Object var1, int var2, int var3) throws BadLocationException;

    public Highlight[] getHighlights();

    public static interface Highlight {
        public int getStartOffset();

        public int getEndOffset();

        public HighlightPainter getPainter();
    }

    public static interface HighlightPainter {
        public void paint(Graphics var1, int var2, int var3, Shape var4, JTextComponent var5);
    }
}

