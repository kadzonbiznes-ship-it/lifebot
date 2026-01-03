/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.BufferedPaints;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.SpanIterator;
import sun.java2d.pipe.hw.AccelSurface;

public abstract class BufferedContext {
    public static final int NO_CONTEXT_FLAGS = 0;
    public static final int SRC_IS_OPAQUE = 1;
    public static final int USE_MASK = 2;
    private final RenderQueue rq;
    private final RenderBuffer buf;
    protected static BufferedContext currentContext;
    private Reference<AccelSurface> validSrcDataRef = new WeakReference<Object>(null);
    private Reference<AccelSurface> validDstDataRef = new WeakReference<Object>(null);
    private Reference<Region> validClipRef = new WeakReference<Object>(null);
    private Reference<Composite> validCompRef = new WeakReference<Object>(null);
    private Reference<Paint> validPaintRef = new WeakReference<Object>(null);
    private boolean isValidatedPaintJustAColor;
    private int validatedRGB;
    private int validatedFlags;
    private boolean xformInUse;
    private AffineTransform transform;

    protected BufferedContext(RenderQueue rq) {
        this.rq = rq;
        this.buf = rq.getBuffer();
    }

    public static void validateContext(AccelSurface srcData, AccelSurface dstData, Region clip, Composite comp, AffineTransform xform, Paint paint, SunGraphics2D sg2d, int flags) {
        BufferedContext context = dstData.getContext();
        context.validate(srcData, dstData, clip, comp, xform, paint, sg2d, flags);
    }

    public static void validateContext(AccelSurface surface) {
        BufferedContext.validateContext(surface, surface, null, null, null, null, null, 0);
    }

    private void validate(AccelSurface srcData, AccelSurface dstData, Region clip, Composite comp, AffineTransform xform, Paint paint, SunGraphics2D sg2d, int flags) {
        Region validatedClip;
        boolean updateClip = false;
        boolean updatePaint = false;
        if (!dstData.isValid() || dstData.isSurfaceLost() || srcData.isSurfaceLost()) {
            this.invalidateContext();
            throw new InvalidPipeException("bounds changed or surface lost");
        }
        if (paint instanceof Color) {
            int newRGB = ((Color)paint).getRGB();
            if (this.isValidatedPaintJustAColor) {
                if (newRGB != this.validatedRGB) {
                    this.validatedRGB = newRGB;
                    updatePaint = true;
                }
            } else {
                this.validatedRGB = newRGB;
                updatePaint = true;
                this.isValidatedPaintJustAColor = true;
            }
        } else if (this.validPaintRef.get() != paint) {
            updatePaint = true;
            this.isValidatedPaintJustAColor = false;
        }
        AccelSurface validatedSrcData = this.validSrcDataRef.get();
        AccelSurface validatedDstData = this.validDstDataRef.get();
        if (currentContext != this || srcData != validatedSrcData || dstData != validatedDstData) {
            if (dstData != validatedDstData) {
                updateClip = true;
            }
            if (paint == null) {
                updatePaint = true;
            }
            this.setSurfaces(srcData, dstData);
            currentContext = this;
            this.validSrcDataRef = new WeakReference<AccelSurface>(srcData);
            this.validDstDataRef = new WeakReference<AccelSurface>(dstData);
        }
        if (clip != (validatedClip = this.validClipRef.get()) || updateClip) {
            if (clip != null) {
                if (updateClip || validatedClip == null || !validatedClip.isRectangular() || !clip.isRectangular() || clip.getLoX() != validatedClip.getLoX() || clip.getLoY() != validatedClip.getLoY() || clip.getHiX() != validatedClip.getHiX() || clip.getHiY() != validatedClip.getHiY()) {
                    this.setClip(clip);
                }
            } else {
                this.resetClip();
            }
            this.validClipRef = new WeakReference<Region>(clip);
        }
        if (comp != this.validCompRef.get() || flags != this.validatedFlags) {
            if (comp != null) {
                this.setComposite(comp, flags);
            } else {
                this.resetComposite();
            }
            updatePaint = true;
            this.validCompRef = new WeakReference<Composite>(comp);
            this.validatedFlags = flags;
        }
        boolean txChanged = false;
        if (xform == null) {
            if (this.xformInUse) {
                this.resetTransform();
                this.xformInUse = false;
                txChanged = true;
            } else if (sg2d != null && !sg2d.transform.equals(this.transform)) {
                txChanged = true;
            }
            if (sg2d != null && txChanged) {
                this.transform = new AffineTransform(sg2d.transform);
            }
        } else {
            this.setTransform(xform);
            this.xformInUse = true;
            txChanged = true;
        }
        if (!this.isValidatedPaintJustAColor && txChanged) {
            updatePaint = true;
        }
        if (updatePaint) {
            if (paint != null) {
                BufferedPaints.setPaint(this.rq, sg2d, paint, flags);
            } else {
                BufferedPaints.resetPaint(this.rq);
            }
            this.validPaintRef = new WeakReference<Paint>(paint);
        }
        dstData.markDirty();
    }

    private void setSurfaces(AccelSurface srcData, AccelSurface dstData) {
        this.rq.ensureCapacityAndAlignment(20, 4);
        this.buf.putInt(70);
        this.buf.putLong(srcData.getNativeOps());
        this.buf.putLong(dstData.getNativeOps());
    }

    private void resetClip() {
        this.rq.ensureCapacity(4);
        this.buf.putInt(55);
    }

    private void setClip(Region clip) {
        if (clip.isRectangular()) {
            this.rq.ensureCapacity(20);
            this.buf.putInt(51);
            this.buf.putInt(clip.getLoX()).putInt(clip.getLoY());
            this.buf.putInt(clip.getHiX()).putInt(clip.getHiY());
        } else {
            this.rq.ensureCapacity(28);
            this.buf.putInt(52);
            this.buf.putInt(53);
            int countIndex = this.buf.position();
            this.buf.putInt(0);
            int spanCount = 0;
            int remainingSpans = this.buf.remaining() / 16;
            int[] span = new int[4];
            SpanIterator si = clip.getSpanIterator();
            while (si.nextSpan(span)) {
                if (remainingSpans == 0) {
                    this.buf.putInt(countIndex, spanCount);
                    this.rq.flushNow();
                    this.buf.putInt(53);
                    countIndex = this.buf.position();
                    this.buf.putInt(0);
                    spanCount = 0;
                    remainingSpans = this.buf.remaining() / 16;
                }
                this.buf.putInt(span[0]);
                this.buf.putInt(span[1]);
                this.buf.putInt(span[2]);
                this.buf.putInt(span[3]);
                ++spanCount;
                --remainingSpans;
            }
            this.buf.putInt(countIndex, spanCount);
            this.rq.ensureCapacity(4);
            this.buf.putInt(54);
        }
    }

    private void resetComposite() {
        this.rq.ensureCapacity(4);
        this.buf.putInt(58);
    }

    private void setComposite(Composite comp, int flags) {
        if (comp instanceof AlphaComposite) {
            AlphaComposite ac = (AlphaComposite)comp;
            this.rq.ensureCapacity(16);
            this.buf.putInt(56);
            this.buf.putInt(ac.getRule());
            this.buf.putFloat(ac.getAlpha());
            this.buf.putInt(flags);
        } else if (comp instanceof XORComposite) {
            int xorPixel = ((XORComposite)comp).getXorPixel();
            this.rq.ensureCapacity(8);
            this.buf.putInt(57);
            this.buf.putInt(xorPixel);
        } else {
            throw new InternalError("not yet implemented");
        }
    }

    private void resetTransform() {
        this.rq.ensureCapacity(4);
        this.buf.putInt(60);
    }

    private void setTransform(AffineTransform xform) {
        this.rq.ensureCapacityAndAlignment(52, 4);
        this.buf.putInt(59);
        this.buf.putDouble(xform.getScaleX());
        this.buf.putDouble(xform.getShearY());
        this.buf.putDouble(xform.getShearX());
        this.buf.putDouble(xform.getScaleY());
        this.buf.putDouble(xform.getTranslateX());
        this.buf.putDouble(xform.getTranslateY());
    }

    public final void invalidateContext() {
        this.resetTransform();
        this.resetComposite();
        this.resetClip();
        BufferedPaints.resetPaint(this.rq);
        this.validSrcDataRef.clear();
        this.validDstDataRef.clear();
        this.validCompRef.clear();
        this.validClipRef.clear();
        this.validPaintRef.clear();
        this.isValidatedPaintJustAColor = false;
        this.xformInUse = false;
    }

    public final RenderQueue getRenderQueue() {
        return this.rq;
    }
}

