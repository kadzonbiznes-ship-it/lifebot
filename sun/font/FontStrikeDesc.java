/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import sun.awt.SunHints;
import sun.font.Font2D;
import sun.font.FontUtilities;

public class FontStrikeDesc {
    static final int AA_ON = 16;
    static final int AA_LCD_H = 32;
    static final int AA_LCD_V = 64;
    static final int FRAC_METRICS_ON = 256;
    static final int FRAC_METRICS_SP = 512;
    AffineTransform devTx;
    AffineTransform glyphTx;
    int style;
    int aaHint;
    int fmHint;
    private int hashCode;
    private int valuemask;

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = this.glyphTx.hashCode() + this.devTx.hashCode() + this.valuemask;
        }
        return this.hashCode;
    }

    public boolean equals(Object obj) {
        try {
            FontStrikeDesc desc = (FontStrikeDesc)obj;
            return desc.valuemask == this.valuemask && desc.glyphTx.equals(this.glyphTx) && desc.devTx.equals(this.devTx);
        }
        catch (Exception e) {
            return false;
        }
    }

    FontStrikeDesc() {
    }

    public static int getAAHintIntVal(Object aa, Font2D font2D, int ptSize) {
        if (FontUtilities.isMacOSX14 && (aa == SunHints.VALUE_TEXT_ANTIALIAS_OFF || aa == SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT || aa == SunHints.VALUE_TEXT_ANTIALIAS_ON || aa == SunHints.VALUE_TEXT_ANTIALIAS_GASP)) {
            return 2;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_OFF || aa == SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
            return 1;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_ON) {
            return 2;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_GASP) {
            if (font2D.useAAForPtSize(ptSize)) {
                return 2;
            }
            return 1;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB || aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR) {
            return 4;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB || aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR) {
            return 6;
        }
        return 1;
    }

    public static int getAAHintIntVal(Font2D font2D, Font font, FontRenderContext frc) {
        Object aa = frc.getAntiAliasingHint();
        if (FontUtilities.isMacOSX14 && (aa == SunHints.VALUE_TEXT_ANTIALIAS_OFF || aa == SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT || aa == SunHints.VALUE_TEXT_ANTIALIAS_ON || aa == SunHints.VALUE_TEXT_ANTIALIAS_GASP)) {
            return 2;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_OFF || aa == SunHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
            return 1;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_ON) {
            return 2;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_GASP) {
            int ptSize;
            AffineTransform tx = frc.getTransform();
            if (tx.isIdentity() && !font.isTransformed()) {
                ptSize = font.getSize();
            } else {
                float size = font.getSize2D();
                if (tx.isIdentity()) {
                    tx = font.getTransform();
                    tx.scale(size, size);
                } else {
                    tx.scale(size, size);
                    if (font.isTransformed()) {
                        tx.concatenate(font.getTransform());
                    }
                }
                double shearx = tx.getShearX();
                double scaley = tx.getScaleY();
                if (shearx != 0.0) {
                    scaley = Math.sqrt(shearx * shearx + scaley * scaley);
                }
                ptSize = (int)(Math.abs(scaley) + 0.5);
            }
            if (font2D.useAAForPtSize(ptSize)) {
                return 2;
            }
            return 1;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB || aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR) {
            return 4;
        }
        if (aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB || aa == SunHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR) {
            return 6;
        }
        return 1;
    }

    public static int getFMHintIntVal(Object fm) {
        if (fm == SunHints.VALUE_FRACTIONALMETRICS_OFF || fm == SunHints.VALUE_FRACTIONALMETRICS_DEFAULT) {
            return 1;
        }
        return 2;
    }

    public FontStrikeDesc(AffineTransform devAt, AffineTransform at, int fStyle, int aa, int fm) {
        this.devTx = devAt;
        this.glyphTx = at;
        this.style = fStyle;
        this.aaHint = aa;
        this.fmHint = fm;
        this.valuemask = fStyle;
        switch (aa) {
            case 1: {
                break;
            }
            case 2: {
                this.valuemask |= 0x10;
                break;
            }
            case 4: 
            case 5: {
                this.valuemask |= 0x20;
                break;
            }
            case 6: 
            case 7: {
                this.valuemask |= 0x40;
                break;
            }
        }
        if (fm == 2) {
            this.valuemask |= 0x100;
        }
    }

    FontStrikeDesc(FontStrikeDesc desc) {
        this.devTx = desc.devTx;
        this.glyphTx = (AffineTransform)desc.glyphTx.clone();
        this.style = desc.style;
        this.aaHint = desc.aaHint;
        this.fmHint = desc.fmHint;
        this.hashCode = desc.hashCode;
        this.valuemask = desc.valuemask;
    }

    public String toString() {
        return "FontStrikeDesc: Style=" + this.style + " AA=" + this.aaHint + " FM=" + this.fmHint + " devTx=" + String.valueOf(this.devTx) + " devTx.FontTx.ptSize=" + String.valueOf(this.glyphTx);
    }
}

