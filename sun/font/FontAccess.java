/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.peer.FontPeer;
import sun.font.Font2D;
import sun.font.Font2DHandle;

public abstract class FontAccess {
    private static FontAccess access;

    public static synchronized void setFontAccess(FontAccess acc) {
        if (access != null) {
            throw new InternalError("Attempt to set FontAccessor twice");
        }
        access = acc;
    }

    public static synchronized FontAccess getFontAccess() {
        return access;
    }

    public abstract Font2D getFont2D(Font var1);

    public abstract void setFont2D(Font var1, Font2DHandle var2);

    public abstract void setCreatedFont(Font var1);

    public abstract boolean isCreatedFont(Font var1);

    public abstract FontPeer getFontPeer(Font var1);
}

