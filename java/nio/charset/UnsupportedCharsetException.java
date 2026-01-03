/*
 * Decompiled with CFR 0.152.
 */
package java.nio.charset;

public class UnsupportedCharsetException
extends IllegalArgumentException {
    private static final long serialVersionUID = 1490765524727386367L;
    private String charsetName;

    public UnsupportedCharsetException(String charsetName) {
        super(String.valueOf(charsetName));
        this.charsetName = charsetName;
    }

    public String getCharsetName() {
        return this.charsetName;
    }
}

