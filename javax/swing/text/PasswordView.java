/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import javax.swing.JPasswordField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.FieldView;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Utilities;
import sun.swing.SwingUtilities2;

public class PasswordView
extends FieldView {
    static char[] ONE = new char[1];
    private final boolean drawEchoCharacterOverridden = PasswordView.getFPMethodOverridden(this.getClass(), "drawEchoCharacter", PlainView.FPMethodArgs.GNNC);

    public PasswordView(Element elem) {
        super(elem);
    }

    @Override
    @Deprecated(since="9")
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)this.drawUnselectedTextImpl(g, x, y, p0, p1, false);
    }

    @Override
    protected float drawUnselectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawUnselectedTextImpl(g, x, y, p0, p1, true);
    }

    private float drawUnselectedTextImpl(Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        Container c = this.getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField)c;
            if (!f.echoCharIsSet()) {
                boolean useDrawUnselectedFPAPI = useFPAPI && this.drawUnselectedTextOverridden && g instanceof Graphics2D;
                return useDrawUnselectedFPAPI ? super.drawUnselectedText((Graphics2D)g, x, y, p0, p1) : (float)super.drawUnselectedText(g, (int)x, (int)y, p0, p1);
            }
            if (f.isEnabled()) {
                g.setColor(f.getForeground());
            } else {
                g.setColor(f.getDisabledTextColor());
            }
            char echoChar = f.getEchoChar();
            int n = p1 - p0;
            boolean useEchoCharFPAPI = useFPAPI && this.drawEchoCharacterOverridden && g instanceof Graphics2D;
            for (int i = 0; i < n; ++i) {
                x = useEchoCharFPAPI ? this.drawEchoCharacter((Graphics2D)g, x, y, echoChar) : (float)this.drawEchoCharacter(g, (int)x, (int)y, echoChar);
            }
        }
        return x;
    }

    @Override
    @Deprecated(since="9")
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)this.drawSelectedTextImpl(g, x, y, p0, p1, false);
    }

    @Override
    protected float drawSelectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawSelectedTextImpl(g, x, y, p0, p1, true);
    }

    private float drawSelectedTextImpl(Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        g.setColor(this.selected);
        Container c = this.getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField)c;
            if (!f.echoCharIsSet()) {
                boolean useDrawUnselectedFPAPI = useFPAPI && this.drawSelectedTextOverridden && g instanceof Graphics2D;
                return useFPAPI ? super.drawSelectedText((Graphics2D)g, x, y, p0, p1) : (float)super.drawSelectedText(g, (int)x, (int)y, p0, p1);
            }
            char echoChar = f.getEchoChar();
            int n = p1 - p0;
            boolean useEchoCharFPAPI = useFPAPI && this.drawEchoCharacterOverridden && g instanceof Graphics2D;
            for (int i = 0; i < n; ++i) {
                x = useEchoCharFPAPI ? this.drawEchoCharacter((Graphics2D)g, x, y, echoChar) : (float)this.drawEchoCharacter(g, (int)x, (int)y, echoChar);
            }
        }
        return x;
    }

    @Deprecated(since="9")
    protected int drawEchoCharacter(Graphics g, int x, int y, char c) {
        return (int)this.drawEchoCharacterImpl(g, x, y, c, false);
    }

    protected float drawEchoCharacter(Graphics2D g, float x, float y, char c) {
        return this.drawEchoCharacterImpl(g, x, y, c, true);
    }

    private float drawEchoCharacterImpl(Graphics g, float x, float y, char c, boolean useFPAPI) {
        PasswordView.ONE[0] = c;
        SwingUtilities2.drawChars(Utilities.getJComponent(this), g, ONE, 0, 1, x, y);
        if (useFPAPI) {
            return x + (float)g.getFontMetrics().charWidth(c);
        }
        FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
        return x + (float)g.getFont().getStringBounds(ONE, 0, 1, frc).getWidth();
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Container c = this.getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField)c;
            if (!f.echoCharIsSet()) {
                return super.modelToView(pos, a, b);
            }
            char echoChar = f.getEchoChar();
            FontMetrics m = f.getFontMetrics(f.getFont());
            Rectangle alloc = this.adjustAllocation(a).getBounds();
            int dx = (pos - this.getStartOffset()) * m.charWidth(echoChar);
            alloc.x += dx;
            alloc.width = 1;
            return alloc;
        }
        return null;
    }

    @Override
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        bias[0] = Position.Bias.Forward;
        int n = 0;
        Container c = this.getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField)c;
            if (!f.echoCharIsSet()) {
                return super.viewToModel(fx, fy, a, bias);
            }
            char echoChar = f.getEchoChar();
            int charWidth = f.getFontMetrics(f.getFont()).charWidth(echoChar);
            Rectangle alloc = (a = this.adjustAllocation(a)) instanceof Rectangle ? (Rectangle)a : a.getBounds();
            int n2 = n = charWidth > 0 ? ((int)fx - alloc.x) / charWidth : Integer.MAX_VALUE;
            if (n < 0) {
                n = 0;
            } else if (n > this.getStartOffset() + this.getDocument().getLength()) {
                n = this.getDocument().getLength() - this.getStartOffset();
            }
        }
        return this.getStartOffset() + n;
    }

    @Override
    public float getPreferredSpan(int axis) {
        switch (axis) {
            case 0: {
                JPasswordField f;
                Container c = this.getContainer();
                if (!(c instanceof JPasswordField) || !(f = (JPasswordField)c).echoCharIsSet()) break;
                char echoChar = f.getEchoChar();
                FontMetrics m = f.getFontMetrics(f.getFont());
                Document doc = this.getDocument();
                return m.charWidth(echoChar) * this.getDocument().getLength();
            }
        }
        return super.getPreferredSpan(axis);
    }
}

