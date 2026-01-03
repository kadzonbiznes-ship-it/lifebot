/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm.lcms;

import java.awt.color.CMMException;
import java.awt.color.ICC_Profile;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.StampedLock;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.lcms.LCMSProfile;
import sun.java2d.cmm.lcms.LCMSTransform;

final class LCMS
implements PCMM {
    private static final StampedLock lock = new StampedLock();
    private static LCMS theLcms = null;

    @Override
    public Profile loadProfile(byte[] data) {
        Object disposerRef = new Object();
        long ptr = LCMS.loadProfileNative(data, disposerRef);
        if (ptr != 0L) {
            return new LCMSProfile(ptr, disposerRef);
        }
        return null;
    }

    static LCMSProfile getLcmsProfile(Profile p) {
        if (p instanceof LCMSProfile) {
            return (LCMSProfile)p;
        }
        throw new CMMException("Invalid profile: " + String.valueOf(p));
    }

    static native void setTagDataNative(long var0, int var2, byte[] var3);

    static native byte[] getProfileDataNative(long var0);

    static native byte[] getTagNative(long var0, int var2);

    private static native long loadProfileNative(byte[] var0, Object var1);

    @Override
    public byte[] getProfileData(Profile p) {
        return LCMS.getLcmsProfile(p).getProfileData();
    }

    @Override
    public byte[] getTagData(Profile p, int tagSignature) {
        return LCMS.getLcmsProfile(p).getTag(tagSignature);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTagData(Profile p, int tagSignature, byte[] data) {
        long stamp = lock.writeLock();
        try {
            LCMS.getLcmsProfile(p).setTag(tagSignature, data);
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long createTransform(LCMSProfile[] profiles, int renderingIntent, int inFormatter, int outFormatter, Object disposerRef) {
        long[] ptrs = new long[profiles.length];
        long stamp = lock.readLock();
        try {
            for (int i = 0; i < profiles.length; ++i) {
                if (profiles[i] == null) {
                    throw new CMMException("Unknown profile ID");
                }
                ptrs[i] = profiles[i].getLcmsPtr();
            }
            long l = LCMS.createNativeTransform(ptrs, renderingIntent, inFormatter, outFormatter, disposerRef);
            return l;
        }
        finally {
            lock.unlockRead(stamp);
        }
    }

    private static native long createNativeTransform(long[] var0, int var1, int var2, int var3, Object var4);

    @Override
    public ColorTransform createTransform(int renderingIntent, ICC_Profile ... profiles) {
        return new LCMSTransform(renderingIntent, profiles);
    }

    static native void colorConvert(long var0, int var2, int var3, int var4, int var5, int var6, int var7, Object var8, Object var9, int var10, int var11);

    private LCMS() {
    }

    static synchronized PCMM getModule() {
        if (theLcms != null) {
            return theLcms;
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                System.loadLibrary("awt");
                System.loadLibrary("lcms");
                return null;
            }
        });
        theLcms = new LCMS();
        return theLcms;
    }
}

