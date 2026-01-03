/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.pipe.BufferedContext;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.hw.ContextCapabilities;

final class D3DContext
extends BufferedContext {
    private final D3DGraphicsDevice device;

    D3DContext(RenderQueue rq, D3DGraphicsDevice device) {
        super(rq);
        this.device = device;
    }

    static void invalidateCurrentContext() {
        if (currentContext != null) {
            currentContext.invalidateContext();
            currentContext = null;
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.ensureCapacity(4);
        rq.getBuffer().putInt(75);
        rq.flushNow();
    }

    static void setScratchSurface(D3DContext d3dc) {
        if (d3dc != currentContext) {
            currentContext = null;
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacity(8);
        buf.putInt(71);
        buf.putInt(d3dc.getDevice().getScreen());
    }

    D3DGraphicsDevice getDevice() {
        return this.device;
    }

    static class D3DContextCaps
    extends ContextCapabilities {
        static final int CAPS_LCD_SHADER = 65536;
        static final int CAPS_BIOP_SHADER = 131072;
        static final int CAPS_DEVICE_OK = 262144;
        static final int CAPS_AA_SHADER = 524288;

        D3DContextCaps(int caps, String adapterId) {
            super(caps, adapterId);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(super.toString());
            if ((this.caps & 0x10000) != 0) {
                buf.append("CAPS_LCD_SHADER|");
            }
            if ((this.caps & 0x20000) != 0) {
                buf.append("CAPS_BIOP_SHADER|");
            }
            if ((this.caps & 0x80000) != 0) {
                buf.append("CAPS_AA_SHADER|");
            }
            if ((this.caps & 0x40000) != 0) {
                buf.append("CAPS_DEVICE_OK|");
            }
            return buf.toString();
        }
    }
}

