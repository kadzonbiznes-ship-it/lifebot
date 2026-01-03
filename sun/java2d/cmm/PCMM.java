/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.Profile;

public interface PCMM {
    public Profile loadProfile(byte[] var1);

    public byte[] getProfileData(Profile var1);

    public byte[] getTagData(Profile var1, int var2);

    public void setTagData(Profile var1, int var2, byte[] var3);

    public ColorTransform createTransform(int var1, ICC_Profile ... var2);
}

