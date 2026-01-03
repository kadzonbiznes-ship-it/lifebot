/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm;

import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.PCMM;

public abstract class CMMServiceProvider {
    public final PCMM getColorManagementModule() {
        if (CMSManager.canCreateModule()) {
            return this.getModule();
        }
        return null;
    }

    protected abstract PCMM getModule();
}

