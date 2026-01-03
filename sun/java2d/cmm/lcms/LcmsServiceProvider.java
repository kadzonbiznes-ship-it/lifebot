/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm.lcms;

import sun.java2d.cmm.CMMServiceProvider;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.lcms.LCMS;

public final class LcmsServiceProvider
extends CMMServiceProvider {
    @Override
    protected PCMM getModule() {
        return LCMS.getModule();
    }
}

