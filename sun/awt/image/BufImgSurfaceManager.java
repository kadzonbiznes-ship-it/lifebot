/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.BufferedImage;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;

public class BufImgSurfaceManager
extends SurfaceManager {
    protected BufferedImage bImg;
    protected SurfaceData sdDefault;

    public BufImgSurfaceManager(BufferedImage bImg) {
        this.bImg = bImg;
        this.sdDefault = BufImgSurfaceData.createData(bImg);
    }

    @Override
    public SurfaceData getPrimarySurfaceData() {
        return this.sdDefault;
    }

    @Override
    public SurfaceData restoreContents() {
        return this.sdDefault;
    }
}

