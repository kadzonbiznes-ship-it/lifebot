/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.CharacterIterator;

public abstract class FontMetrics
implements Serializable {
    private static final FontRenderContext DEFAULT_FRC;
    protected Font font;
    private static final long serialVersionUID = 1681126225205050147L;

    protected FontMetrics(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return this.font;
    }

    public FontRenderContext getFontRenderContext() {
        return DEFAULT_FRC;
    }

    public int getLeading() {
        return 0;
    }

    public int getAscent() {
        return this.font.getSize();
    }

    public int getDescent() {
        return 0;
    }

    public int getHeight() {
        return this.getLeading() + this.getAscent() + this.getDescent();
    }

    public int getMaxAscent() {
        return this.getAscent();
    }

    public int getMaxDescent() {
        return this.getDescent();
    }

    @Deprecated
    public int getMaxDecent() {
        return this.getMaxDescent();
    }

    public int getMaxAdvance() {
        return -1;
    }

    public int charWidth(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            codePoint = 65535;
        }
        if (codePoint < 256) {
            return this.getWidths()[codePoint];
        }
        char[] buffer = new char[2];
        int len = Character.toChars(codePoint, buffer, 0);
        return this.charsWidth(buffer, 0, len);
    }

    public int charWidth(char ch) {
        if (ch < '\u0100') {
            return this.getWidths()[ch];
        }
        char[] data = new char[]{ch};
        return this.charsWidth(data, 0, 1);
    }

    public int stringWidth(String str) {
        int len = str.length();
        char[] data = new char[len];
        str.getChars(0, len, data, 0);
        return this.charsWidth(data, 0, len);
    }

    public int charsWidth(char[] data, int off, int len) {
        return this.stringWidth(new String(data, off, len));
    }

    public int bytesWidth(byte[] data, int off, int len) {
        return this.stringWidth(new String(data, 0, off, len));
    }

    public int[] getWidths() {
        int[] widths = new int[256];
        for (char ch = '\u0000'; ch < '\u0100'; ch = (char)(ch + '\u0001')) {
            widths[ch] = this.charWidth(ch);
        }
        return widths;
    }

    public boolean hasUniformLineMetrics() {
        return this.font.hasUniformLineMetrics();
    }

    public LineMetrics getLineMetrics(String str, Graphics context) {
        return this.font.getLineMetrics(str, this.myFRC(context));
    }

    public LineMetrics getLineMetrics(String str, int beginIndex, int limit, Graphics context) {
        return this.font.getLineMetrics(str, beginIndex, limit, this.myFRC(context));
    }

    public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, Graphics context) {
        return this.font.getLineMetrics(chars, beginIndex, limit, this.myFRC(context));
    }

    public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
        return this.font.getLineMetrics(ci, beginIndex, limit, this.myFRC(context));
    }

    public Rectangle2D getStringBounds(String str, Graphics context) {
        return this.font.getStringBounds(str, this.myFRC(context));
    }

    public Rectangle2D getStringBounds(String str, int beginIndex, int limit, Graphics context) {
        return this.font.getStringBounds(str, beginIndex, limit, this.myFRC(context));
    }

    public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, Graphics context) {
        return this.font.getStringBounds(chars, beginIndex, limit, this.myFRC(context));
    }

    public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
        return this.font.getStringBounds(ci, beginIndex, limit, this.myFRC(context));
    }

    public Rectangle2D getMaxCharBounds(Graphics context) {
        return this.font.getMaxCharBounds(this.myFRC(context));
    }

    private FontRenderContext myFRC(Graphics context) {
        if (context instanceof Graphics2D) {
            return ((Graphics2D)context).getFontRenderContext();
        }
        return DEFAULT_FRC;
    }

    public String toString() {
        return this.getClass().getName() + "[font=" + String.valueOf(this.getFont()) + "ascent=" + this.getAscent() + ", descent=" + this.getDescent() + ", height=" + this.getHeight() + "]";
    }

    private static native void initIDs();

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            FontMetrics.initIDs();
        }
        DEFAULT_FRC = new FontRenderContext(null, false, false);
    }
}

