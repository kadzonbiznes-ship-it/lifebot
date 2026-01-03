/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import sun.java2d.SunCompositeContext;

public final class AlphaComposite
implements Composite {
    public static final int CLEAR = 1;
    public static final int SRC = 2;
    public static final int DST = 9;
    public static final int SRC_OVER = 3;
    public static final int DST_OVER = 4;
    public static final int SRC_IN = 5;
    public static final int DST_IN = 6;
    public static final int SRC_OUT = 7;
    public static final int DST_OUT = 8;
    public static final int SRC_ATOP = 10;
    public static final int DST_ATOP = 11;
    public static final int XOR = 12;
    public static final AlphaComposite Clear = new AlphaComposite(1);
    public static final AlphaComposite Src = new AlphaComposite(2);
    public static final AlphaComposite Dst = new AlphaComposite(9);
    public static final AlphaComposite SrcOver = new AlphaComposite(3);
    public static final AlphaComposite DstOver = new AlphaComposite(4);
    public static final AlphaComposite SrcIn = new AlphaComposite(5);
    public static final AlphaComposite DstIn = new AlphaComposite(6);
    public static final AlphaComposite SrcOut = new AlphaComposite(7);
    public static final AlphaComposite DstOut = new AlphaComposite(8);
    public static final AlphaComposite SrcAtop = new AlphaComposite(10);
    public static final AlphaComposite DstAtop = new AlphaComposite(11);
    public static final AlphaComposite Xor = new AlphaComposite(12);
    private static final int MIN_RULE = 1;
    private static final int MAX_RULE = 12;
    float extraAlpha;
    int rule;

    private AlphaComposite(int rule) {
        this(rule, 1.0f);
    }

    private AlphaComposite(int rule, float alpha) {
        if (rule < 1 || rule > 12) {
            throw new IllegalArgumentException("unknown composite rule");
        }
        if (!(alpha >= 0.0f) || !(alpha <= 1.0f)) {
            throw new IllegalArgumentException("alpha value out of range");
        }
        this.rule = rule;
        this.extraAlpha = alpha;
    }

    public static AlphaComposite getInstance(int rule) {
        switch (rule) {
            case 1: {
                return Clear;
            }
            case 2: {
                return Src;
            }
            case 9: {
                return Dst;
            }
            case 3: {
                return SrcOver;
            }
            case 4: {
                return DstOver;
            }
            case 5: {
                return SrcIn;
            }
            case 6: {
                return DstIn;
            }
            case 7: {
                return SrcOut;
            }
            case 8: {
                return DstOut;
            }
            case 10: {
                return SrcAtop;
            }
            case 11: {
                return DstAtop;
            }
            case 12: {
                return Xor;
            }
        }
        throw new IllegalArgumentException("unknown composite rule");
    }

    public static AlphaComposite getInstance(int rule, float alpha) {
        if (alpha == 1.0f) {
            return AlphaComposite.getInstance(rule);
        }
        return new AlphaComposite(rule, alpha);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new SunCompositeContext(this, srcColorModel, dstColorModel);
    }

    public float getAlpha() {
        return this.extraAlpha;
    }

    public int getRule() {
        return this.rule;
    }

    public AlphaComposite derive(int rule) {
        return this.rule == rule ? this : AlphaComposite.getInstance(rule, this.extraAlpha);
    }

    public AlphaComposite derive(float alpha) {
        return this.extraAlpha == alpha ? this : AlphaComposite.getInstance(this.rule, alpha);
    }

    public int hashCode() {
        return Float.floatToIntBits(this.extraAlpha) * 31 + this.rule;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AlphaComposite)) {
            return false;
        }
        AlphaComposite ac = (AlphaComposite)obj;
        if (this.rule != ac.rule) {
            return false;
        }
        return this.extraAlpha == ac.extraAlpha;
    }
}

