/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

public abstract class LayeredHighlighter
implements Highlighter {
    protected LayeredHighlighter() {
    }

    public abstract void paintLayeredHighlights(Graphics var1, int var2, int var3, Shape var4, JTextComponent var5, View var6);

    public static abstract class LayerPainter
    implements Highlighter.HighlightPainter {
        protected LayerPainter() {
        }

        public abstract Shape paintLayer(Graphics var1, int var2, int var3, Shape var4, JTextComponent var5, View var6);
    }
}

