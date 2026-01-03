/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.Toolkit;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.StateInvariantError;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabableView;
import javax.swing.text.ViewFactory;

public class LabelView
extends GlyphView
implements TabableView {
    private Font font;
    private Color fg;
    private Color bg;
    private boolean underline;
    private boolean strike;
    private boolean superscript;
    private boolean subscript;

    public LabelView(Element elem) {
        super(elem);
    }

    final void sync() {
        if (this.font == null) {
            this.setPropertiesFromAttributes();
        }
    }

    protected void setUnderline(boolean u) {
        this.underline = u;
    }

    protected void setStrikeThrough(boolean s) {
        this.strike = s;
    }

    protected void setSuperscript(boolean s) {
        this.superscript = s;
    }

    protected void setSubscript(boolean s) {
        this.subscript = s;
    }

    protected void setBackground(Color bg) {
        this.bg = bg;
    }

    protected void setPropertiesFromAttributes() {
        AttributeSet attr = this.getAttributes();
        if (attr != null) {
            Document d = this.getDocument();
            if (d instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)d;
                this.font = doc.getFont(attr);
                this.fg = doc.getForeground(attr);
                this.bg = attr.isDefined(StyleConstants.Background) ? doc.getBackground(attr) : null;
                this.setUnderline(StyleConstants.isUnderline(attr));
                this.setStrikeThrough(StyleConstants.isStrikeThrough(attr));
                this.setSuperscript(StyleConstants.isSuperscript(attr));
                this.setSubscript(StyleConstants.isSubscript(attr));
            } else {
                throw new StateInvariantError("LabelView needs StyledDocument");
            }
        }
    }

    @Deprecated
    protected FontMetrics getFontMetrics() {
        this.sync();
        Container c = this.getContainer();
        return c != null ? c.getFontMetrics(this.font) : Toolkit.getDefaultToolkit().getFontMetrics(this.font);
    }

    @Override
    public Color getBackground() {
        this.sync();
        return this.bg;
    }

    @Override
    public Color getForeground() {
        this.sync();
        return this.fg;
    }

    @Override
    public Font getFont() {
        this.sync();
        return this.font;
    }

    @Override
    public boolean isUnderline() {
        this.sync();
        return this.underline;
    }

    @Override
    public boolean isStrikeThrough() {
        this.sync();
        return this.strike;
    }

    @Override
    public boolean isSubscript() {
        this.sync();
        return this.subscript;
    }

    @Override
    public boolean isSuperscript() {
        this.sync();
        return this.superscript;
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.font = null;
        super.changedUpdate(e, a, f);
    }
}

