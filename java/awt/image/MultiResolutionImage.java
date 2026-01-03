/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Image;
import java.util.List;

public interface MultiResolutionImage {
    public Image getResolutionVariant(double var1, double var3);

    public List<Image> getResolutionVariants();
}

