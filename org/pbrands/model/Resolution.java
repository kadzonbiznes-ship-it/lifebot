/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import java.awt.Dimension;
import lombok.Generated;

public class Resolution {
    private final int width;
    private final int height;

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isAtLeastFHD() {
        return this.width >= 1920 && this.height >= 1080;
    }

    public Dimension getSize() {
        return new Dimension(this.width, this.height);
    }

    public String toString() {
        return this.width + "x" + this.height;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }
}

