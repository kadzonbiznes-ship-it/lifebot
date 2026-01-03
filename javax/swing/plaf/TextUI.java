/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

public abstract class TextUI
extends ComponentUI {
    protected TextUI() {
    }

    @Deprecated(since="9")
    public abstract Rectangle modelToView(JTextComponent var1, int var2) throws BadLocationException;

    @Deprecated(since="9")
    public abstract Rectangle modelToView(JTextComponent var1, int var2, Position.Bias var3) throws BadLocationException;

    public Rectangle2D modelToView2D(JTextComponent t, int pos, Position.Bias bias) throws BadLocationException {
        return this.modelToView(t, pos, bias);
    }

    @Deprecated(since="9")
    public abstract int viewToModel(JTextComponent var1, Point var2);

    @Deprecated(since="9")
    public abstract int viewToModel(JTextComponent var1, Point var2, Position.Bias[] var3);

    public int viewToModel2D(JTextComponent t, Point2D pt, Position.Bias[] biasReturn) {
        return this.viewToModel(t, new Point((int)pt.getX(), (int)pt.getY()), biasReturn);
    }

    public abstract int getNextVisualPositionFrom(JTextComponent var1, int var2, Position.Bias var3, int var4, Position.Bias[] var5) throws BadLocationException;

    public abstract void damageRange(JTextComponent var1, int var2, int var3);

    public abstract void damageRange(JTextComponent var1, int var2, int var3, Position.Bias var4, Position.Bias var5);

    public abstract EditorKit getEditorKit(JTextComponent var1);

    public abstract View getRootView(JTextComponent var1);

    @Deprecated(since="9")
    public String getToolTipText(JTextComponent t, Point pt) {
        return null;
    }

    public String getToolTipText2D(JTextComponent t, Point2D pt) {
        return this.getToolTipText(t, new Point((int)pt.getX(), (int)pt.getY()));
    }
}

