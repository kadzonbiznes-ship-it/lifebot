/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm.lcms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.lcms.LCMS;

final class LCMSProfile
extends Profile {
    private final Object disposerReferent;
    private final Map<Integer, byte[]> tags = new ConcurrentHashMap<Integer, byte[]>();
    private final StampedLock lock = new StampedLock();

    LCMSProfile(long ptr, Object ref) {
        super(ptr);
        this.disposerReferent = ref;
    }

    long getLcmsPtr() {
        return this.getNativePtr();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    byte[] getProfileData() {
        long stamp = this.lock.readLock();
        try {
            byte[] byArray = LCMS.getProfileDataNative(this.getNativePtr());
            return byArray;
        }
        finally {
            this.lock.unlockRead(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    byte[] getTag(int sig) {
        byte[] t = this.tags.get(sig);
        if (t != null) {
            return t;
        }
        long stamp = this.lock.readLock();
        try {
            byte[] byArray = this.tags.computeIfAbsent(sig, key -> LCMS.getTagNative(this.getNativePtr(), key));
            return byArray;
        }
        finally {
            this.lock.unlockRead(stamp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setTag(int tagSignature, byte[] data) {
        long stamp = this.lock.writeLock();
        try {
            this.tags.clear();
            LCMS.setTagDataNative(this.getNativePtr(), tagSignature, data);
        }
        finally {
            this.lock.unlockWrite(stamp);
        }
    }
}

