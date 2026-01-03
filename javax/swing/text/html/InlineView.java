/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class InlineView
extends LabelView {
    private boolean nowrap;
    private AttributeSet attr;

    public InlineView(Element elem) {
        super(elem);
        StyleSheet sheet = this.getStyleSheet();
        this.attr = sheet.getViewAttributes(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        StyleSheet sheet = this.getStyleSheet();
        this.attr = sheet.getViewAttributes(this);
        this.preferenceChanged(null, true, true);
    }

    @Override
    public AttributeSet getAttributes() {
        return this.attr;
    }

    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        if (this.nowrap) {
            return 0;
        }
        return super.getBreakWeight(axis, pos, len);
    }

    @Override
    public View breakView(int axis, int offset, float pos, float len) {
        return super.breakView(axis, offset, pos, len);
    }

    @Override
    protected void setPropertiesFromAttributes() {
        super.setPropertiesFromAttributes();
        AttributeSet a = this.getAttributes();
        Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
        boolean u = decor != null ? decor.toString().contains("underline") : false;
        this.setUnderline(u);
        boolean s = decor != null ? decor.toString().contains("line-through") : false;
        this.setStrikeThrough(s);
        Object vAlign = a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
        s = vAlign != null ? vAlign.toString().contains("sup") : false;
        this.setSuperscript(s);
        s = vAlign != null ? vAlign.toString().contains("sub") : false;
        this.setSubscript(s);
        Object whitespace = a.getAttribute(CSS.Attribute.WHITE_SPACE);
        this.nowrap = whitespace != null && whitespace.equals("nowrap");
        HTMLDocument doc = (HTMLDocument)this.getDocument();
        Color bg = doc.getBackground(a);
        if (bg != null) {
            this.setBackground(bg);
        }
    }

    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument)this.getDocument();
        return doc.getStyleSheet();
    }
}

