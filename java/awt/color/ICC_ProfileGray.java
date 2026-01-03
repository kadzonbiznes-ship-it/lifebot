/*
 * Decompiled with CFR 0.152.
 */
package java.awt.color;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDeferralInfo;

public final class ICC_ProfileGray
extends ICC_Profile {
    private static final long serialVersionUID = -1124721290732002649L;

    ICC_ProfileGray(Profile p) {
        super(p);
    }

    ICC_ProfileGray(ProfileDeferralInfo pdi) {
        super(pdi);
    }

    @Override
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }

    public float getGamma() {
        return this.getGamma(1800688195);
    }

    public short[] getTRC() {
        return this.getTRC(1800688195);
    }
}

