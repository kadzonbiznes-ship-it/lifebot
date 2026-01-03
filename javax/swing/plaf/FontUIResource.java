/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf;

import java.awt.Font;
import javax.swing.plaf.UIResource;

public class FontUIResource
extends Font
implements UIResource {
    public FontUIResource(String name, int style, int size) {
        super(name, style, size);
    }

    public FontUIResource(Font font) {
        super(font);
    }
}

