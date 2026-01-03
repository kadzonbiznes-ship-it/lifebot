/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import sun.awt.NativeLibLoader;
import sun.security.action.GetPropertyAction;

public class FontDescriptor
implements Cloneable {
    String nativeName;
    public CharsetEncoder encoder;
    String charsetName;
    private int[] exclusionRanges;
    public CharsetEncoder unicodeEncoder;
    boolean useUnicode;
    static boolean isLE;

    public FontDescriptor(String nativeName, CharsetEncoder encoder, int[] exclusionRanges) {
        this.nativeName = nativeName;
        this.encoder = encoder;
        this.exclusionRanges = exclusionRanges;
        this.useUnicode = false;
        Charset cs = encoder.charset();
        try {
            OutputStreamWriter osw = new OutputStreamWriter((OutputStream)new ByteArrayOutputStream(), cs);
            this.charsetName = osw.getEncoding();
            osw.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public String getNativeName() {
        return this.nativeName;
    }

    public CharsetEncoder getFontCharsetEncoder() {
        return this.encoder;
    }

    public String getFontCharsetName() {
        return this.charsetName;
    }

    public int[] getExclusionRanges() {
        return this.exclusionRanges;
    }

    public boolean isExcluded(char ch) {
        int i = 0;
        while (i < this.exclusionRanges.length) {
            int lo = this.exclusionRanges[i++];
            int up = this.exclusionRanges[i++];
            if (ch < lo || ch > up) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        return super.toString() + " [" + this.nativeName + "|" + String.valueOf(this.encoder) + "]";
    }

    private static native void initIDs();

    public boolean useUnicode() {
        if (this.useUnicode && this.unicodeEncoder == null) {
            try {
                this.unicodeEncoder = isLE ? StandardCharsets.UTF_16LE.newEncoder() : StandardCharsets.UTF_16BE.newEncoder();
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return this.useUnicode;
    }

    static {
        NativeLibLoader.loadLibraries();
        FontDescriptor.initIDs();
        String enc = AccessController.doPrivileged(new GetPropertyAction("sun.io.unicode.encoding", "UnicodeBig"));
        isLE = !"UnicodeBig".equals(enc);
    }
}

