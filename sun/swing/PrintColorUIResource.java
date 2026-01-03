/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import java.awt.Color;
import javax.swing.plaf.ColorUIResource;

public class PrintColorUIResource
extends ColorUIResource {
    private Color printColor;

    public PrintColorUIResource(int rgb, Color printColor) {
        super(rgb);
        this.printColor = printColor;
    }

    public Color getPrintColor() {
        return this.printColor != null ? this.printColor : this;
    }

    private Object writeReplace() {
        return new ColorUIResource(this);
    }
}

