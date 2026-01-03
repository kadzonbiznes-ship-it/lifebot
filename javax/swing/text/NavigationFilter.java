/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public class NavigationFilter {
    public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
        fb.setDot(dot, bias);
    }

    public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
        fb.moveDot(dot, bias);
    }

    public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction, Position.Bias[] biasRet) throws BadLocationException {
        return text.getUI().getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
    }

    public static abstract class FilterBypass {
        protected FilterBypass() {
        }

        public abstract Caret getCaret();

        public abstract void setDot(int var1, Position.Bias var2);

        public abstract void moveDot(int var1, Position.Bias var2);
    }
}

