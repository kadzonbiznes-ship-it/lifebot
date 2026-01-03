/*
 * Decompiled with CFR 0.152.
 */
package java.awt.font;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

public class FontRenderContext {
    private transient AffineTransform tx;
    private transient Object aaHintValue;
    private transient Object fmHintValue;
    private transient boolean defaulting;

    protected FontRenderContext() {
        this.aaHintValue = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        this.fmHintValue = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
        this.defaulting = true;
    }

    public FontRenderContext(AffineTransform tx, boolean isAntiAliased, boolean usesFractionalMetrics) {
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        this.aaHintValue = isAntiAliased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        this.fmHintValue = usesFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
    }

    public FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint) {
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        try {
            if (!RenderingHints.KEY_TEXT_ANTIALIASING.isCompatibleValue(aaHint)) {
                throw new IllegalArgumentException("AA hint:" + String.valueOf(aaHint));
            }
            this.aaHintValue = aaHint;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("AA hint:" + String.valueOf(aaHint));
        }
        try {
            if (!RenderingHints.KEY_FRACTIONALMETRICS.isCompatibleValue(fmHint)) {
                throw new IllegalArgumentException("FM hint:" + String.valueOf(fmHint));
            }
            this.fmHintValue = fmHint;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("FM hint:" + String.valueOf(fmHint));
        }
    }

    public boolean isTransformed() {
        if (!this.defaulting) {
            return this.tx != null;
        }
        return !this.getTransform().isIdentity();
    }

    public int getTransformType() {
        if (!this.defaulting) {
            if (this.tx == null) {
                return 0;
            }
            return this.tx.getType();
        }
        return this.getTransform().getType();
    }

    public AffineTransform getTransform() {
        return this.tx == null ? new AffineTransform() : new AffineTransform(this.tx);
    }

    public boolean isAntiAliased() {
        return this.aaHintValue != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF && this.aaHintValue != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    }

    public boolean usesFractionalMetrics() {
        return this.fmHintValue != RenderingHints.VALUE_FRACTIONALMETRICS_OFF && this.fmHintValue != RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
    }

    public Object getAntiAliasingHint() {
        if (this.defaulting) {
            if (this.isAntiAliased()) {
                return RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            }
            return RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        }
        return this.aaHintValue;
    }

    public Object getFractionalMetricsHint() {
        if (this.defaulting) {
            if (this.usesFractionalMetrics()) {
                return RenderingHints.VALUE_FRACTIONALMETRICS_ON;
            }
            return RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        }
        return this.fmHintValue;
    }

    public boolean equals(Object obj) {
        try {
            return this.equals((FontRenderContext)obj);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public boolean equals(FontRenderContext rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        if (!rhs.defaulting && !this.defaulting) {
            if (rhs.aaHintValue == this.aaHintValue && rhs.fmHintValue == this.fmHintValue) {
                return this.tx == null ? rhs.tx == null : this.tx.equals(rhs.tx);
            }
            return false;
        }
        return rhs.getAntiAliasingHint() == this.getAntiAliasingHint() && rhs.getFractionalMetricsHint() == this.getFractionalMetricsHint() && rhs.getTransform().equals(this.getTransform());
    }

    public int hashCode() {
        int hash;
        int n = hash = this.tx == null ? 0 : this.tx.hashCode();
        if (this.defaulting) {
            hash += this.getAntiAliasingHint().hashCode();
            hash += this.getFractionalMetricsHint().hashCode();
        } else {
            hash += this.aaHintValue.hashCode();
            hash += this.fmHintValue.hashCode();
        }
        return hash;
    }
}

