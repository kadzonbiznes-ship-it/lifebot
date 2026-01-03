/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm;

public final class ProfileDeferralInfo {
    public final int colorSpaceType;
    public final int numComponents;
    public final int profileClass;
    public final String filename;

    public ProfileDeferralInfo(String fn, int type, int ncomp, int pclass) {
        this.filename = fn;
        this.colorSpaceType = type;
        this.numComponents = ncomp;
        this.profileClass = pclass;
    }
}

