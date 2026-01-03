/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.imageio.IIOParam;
import javax.imageio.ImageTypeSpecifier;

public class ImageReadParam
extends IIOParam {
    protected boolean canSetSourceRenderSize = false;
    protected Dimension sourceRenderSize = null;
    protected BufferedImage destination = null;
    protected int[] destinationBands = null;
    protected int minProgressivePass = 0;
    protected int numProgressivePasses = Integer.MAX_VALUE;

    @Override
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        super.setDestinationType(destinationType);
        this.setDestination(null);
    }

    public void setDestination(BufferedImage destination) {
        this.destination = destination;
    }

    public BufferedImage getDestination() {
        return this.destination;
    }

    public void setDestinationBands(int[] destinationBands) {
        if (destinationBands == null) {
            this.destinationBands = null;
        } else {
            int numBands = destinationBands.length;
            for (int i = 0; i < numBands; ++i) {
                int band = destinationBands[i];
                if (band < 0) {
                    throw new IllegalArgumentException("Band value < 0!");
                }
                for (int j = i + 1; j < numBands; ++j) {
                    if (band != destinationBands[j]) continue;
                    throw new IllegalArgumentException("Duplicate band value!");
                }
            }
            this.destinationBands = (int[])destinationBands.clone();
        }
    }

    public int[] getDestinationBands() {
        if (this.destinationBands == null) {
            return null;
        }
        return (int[])this.destinationBands.clone();
    }

    public boolean canSetSourceRenderSize() {
        return this.canSetSourceRenderSize;
    }

    public void setSourceRenderSize(Dimension size) throws UnsupportedOperationException {
        if (!this.canSetSourceRenderSize()) {
            throw new UnsupportedOperationException("Can't set source render size!");
        }
        if (size == null) {
            this.sourceRenderSize = null;
        } else {
            if (size.width <= 0 || size.height <= 0) {
                throw new IllegalArgumentException("width or height <= 0!");
            }
            this.sourceRenderSize = (Dimension)size.clone();
        }
    }

    public Dimension getSourceRenderSize() {
        return this.sourceRenderSize == null ? null : (Dimension)this.sourceRenderSize.clone();
    }

    public void setSourceProgressivePasses(int minPass, int numPasses) {
        if (minPass < 0) {
            throw new IllegalArgumentException("minPass < 0!");
        }
        if (numPasses <= 0) {
            throw new IllegalArgumentException("numPasses <= 0!");
        }
        if (numPasses != Integer.MAX_VALUE && (minPass + numPasses - 1 & Integer.MIN_VALUE) != 0) {
            throw new IllegalArgumentException("minPass + numPasses - 1 > INTEGER.MAX_VALUE!");
        }
        this.minProgressivePass = minPass;
        this.numProgressivePasses = numPasses;
    }

    public int getSourceMinProgressivePass() {
        return this.minProgressivePass;
    }

    public int getSourceMaxProgressivePass() {
        if (this.numProgressivePasses == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return this.minProgressivePass + this.numProgressivePasses - 1;
    }

    public int getSourceNumProgressivePasses() {
        return this.numProgressivePasses;
    }
}

