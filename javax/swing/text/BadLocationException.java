/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

public class BadLocationException
extends Exception {
    private int offs;

    public BadLocationException(String s, int offs) {
        super(s);
        this.offs = offs;
    }

    public int offsetRequested() {
        return this.offs;
    }
}

