/*
 * Decompiled with CFR 0.152.
 */
package java.awt.color;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDeferralInfo;

public final class ICC_ProfileRGB
extends ICC_Profile {
    private static final long serialVersionUID = 8505067385152579334L;
    public static final int REDCOMPONENT = 0;
    public static final int GREENCOMPONENT = 1;
    public static final int BLUECOMPONENT = 2;

    ICC_ProfileRGB(Profile p) {
        super(p);
    }

    ICC_ProfileRGB(ProfileDeferralInfo pdi) {
        super(pdi);
    }

    @Override
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }

    public float[][] getMatrix() {
        float[] red = this.getXYZTag(1918392666);
        float[] green = this.getXYZTag(1733843290);
        float[] blue = this.getXYZTag(1649957210);
        return new float[][]{{red[0], green[0], blue[0]}, {red[1], green[1], blue[1]}, {red[2], green[2], blue[2]}};
    }

    @Override
    public float getGamma(int component) {
        return super.getGamma(ICC_ProfileRGB.toTag(component));
    }

    @Override
    public short[] getTRC(int component) {
        return super.getTRC(ICC_ProfileRGB.toTag(component));
    }

    private static int toTag(int component) {
        return switch (component) {
            case 0 -> 1918128707;
            case 1 -> 1733579331;
            case 2 -> 1649693251;
            default -> throw new IllegalArgumentException("Must be Red, Green, or Blue");
        };
    }
}

