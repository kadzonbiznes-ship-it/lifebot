/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import javax.swing.Icon;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.StrokeBorder;
import javax.swing.border.TitledBorder;

public class BorderFactory {
    static final Border sharedRaisedBevel = new BevelBorder(0);
    static final Border sharedLoweredBevel = new BevelBorder(1);
    private static Border sharedSoftRaisedBevel;
    private static Border sharedSoftLoweredBevel;
    static final Border sharedEtchedBorder;
    private static Border sharedRaisedEtchedBorder;
    static final Border emptyBorder;
    private static Border sharedDashedBorder;

    private BorderFactory() {
    }

    public static Border createLineBorder(Color color) {
        return new LineBorder(color, 1);
    }

    public static Border createLineBorder(Color color, int thickness) {
        return new LineBorder(color, thickness);
    }

    public static Border createLineBorder(Color color, int thickness, boolean rounded) {
        return new LineBorder(color, thickness, rounded);
    }

    public static Border createRaisedBevelBorder() {
        return BorderFactory.createSharedBevel(0);
    }

    public static Border createLoweredBevelBorder() {
        return BorderFactory.createSharedBevel(1);
    }

    public static Border createBevelBorder(int type) {
        return BorderFactory.createSharedBevel(type);
    }

    public static Border createBevelBorder(int type, Color highlight, Color shadow) {
        return new BevelBorder(type, highlight, shadow);
    }

    public static Border createBevelBorder(int type, Color highlightOuter, Color highlightInner, Color shadowOuter, Color shadowInner) {
        return new BevelBorder(type, highlightOuter, highlightInner, shadowOuter, shadowInner);
    }

    static Border createSharedBevel(int type) {
        if (type == 0) {
            return sharedRaisedBevel;
        }
        if (type == 1) {
            return sharedLoweredBevel;
        }
        return null;
    }

    public static Border createRaisedSoftBevelBorder() {
        if (sharedSoftRaisedBevel == null) {
            sharedSoftRaisedBevel = new SoftBevelBorder(0);
        }
        return sharedSoftRaisedBevel;
    }

    public static Border createLoweredSoftBevelBorder() {
        if (sharedSoftLoweredBevel == null) {
            sharedSoftLoweredBevel = new SoftBevelBorder(1);
        }
        return sharedSoftLoweredBevel;
    }

    public static Border createSoftBevelBorder(int type) {
        if (type == 0) {
            return BorderFactory.createRaisedSoftBevelBorder();
        }
        if (type == 1) {
            return BorderFactory.createLoweredSoftBevelBorder();
        }
        return null;
    }

    public static Border createSoftBevelBorder(int type, Color highlight, Color shadow) {
        return new SoftBevelBorder(type, highlight, shadow);
    }

    public static Border createSoftBevelBorder(int type, Color highlightOuter, Color highlightInner, Color shadowOuter, Color shadowInner) {
        return new SoftBevelBorder(type, highlightOuter, highlightInner, shadowOuter, shadowInner);
    }

    public static Border createEtchedBorder() {
        return sharedEtchedBorder;
    }

    public static Border createEtchedBorder(Color highlight, Color shadow) {
        return new EtchedBorder(highlight, shadow);
    }

    public static Border createEtchedBorder(int type) {
        switch (type) {
            case 0: {
                if (sharedRaisedEtchedBorder == null) {
                    sharedRaisedEtchedBorder = new EtchedBorder(0);
                }
                return sharedRaisedEtchedBorder;
            }
            case 1: {
                return sharedEtchedBorder;
            }
        }
        throw new IllegalArgumentException("type must be one of EtchedBorder.RAISED or EtchedBorder.LOWERED");
    }

    public static Border createEtchedBorder(int type, Color highlight, Color shadow) {
        return new EtchedBorder(type, highlight, shadow);
    }

    public static TitledBorder createTitledBorder(String title) {
        return new TitledBorder(title);
    }

    public static TitledBorder createTitledBorder(Border border) {
        return new TitledBorder(border);
    }

    public static TitledBorder createTitledBorder(Border border, String title) {
        return new TitledBorder(border, title);
    }

    public static TitledBorder createTitledBorder(Border border, String title, int titleJustification, int titlePosition) {
        return new TitledBorder(border, title, titleJustification, titlePosition);
    }

    public static TitledBorder createTitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont) {
        return new TitledBorder(border, title, titleJustification, titlePosition, titleFont);
    }

    public static TitledBorder createTitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont, Color titleColor) {
        return new TitledBorder(border, title, titleJustification, titlePosition, titleFont, titleColor);
    }

    public static Border createEmptyBorder() {
        return emptyBorder;
    }

    public static Border createEmptyBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static CompoundBorder createCompoundBorder() {
        return new CompoundBorder();
    }

    public static CompoundBorder createCompoundBorder(Border outsideBorder, Border insideBorder) {
        return new CompoundBorder(outsideBorder, insideBorder);
    }

    public static MatteBorder createMatteBorder(int top, int left, int bottom, int right, Color color) {
        return new MatteBorder(top, left, bottom, right, color);
    }

    public static MatteBorder createMatteBorder(int top, int left, int bottom, int right, Icon tileIcon) {
        return new MatteBorder(top, left, bottom, right, tileIcon);
    }

    public static Border createStrokeBorder(BasicStroke stroke) {
        return new StrokeBorder(stroke);
    }

    public static Border createStrokeBorder(BasicStroke stroke, Paint paint) {
        return new StrokeBorder(stroke, paint);
    }

    public static Border createDashedBorder(Paint paint) {
        return BorderFactory.createDashedBorder(paint, 1.0f, 1.0f, 1.0f, false);
    }

    public static Border createDashedBorder(Paint paint, float length, float spacing) {
        return BorderFactory.createDashedBorder(paint, 1.0f, length, spacing, false);
    }

    public static Border createDashedBorder(Paint paint, float thickness, float length, float spacing, boolean rounded) {
        boolean shared;
        boolean bl = shared = !rounded && paint == null && thickness == 1.0f && length == 1.0f && spacing == 1.0f;
        if (shared && sharedDashedBorder != null) {
            return sharedDashedBorder;
        }
        if (thickness < 1.0f) {
            throw new IllegalArgumentException("thickness is less than 1");
        }
        if (length < 1.0f) {
            throw new IllegalArgumentException("length is less than 1");
        }
        if (spacing < 0.0f) {
            throw new IllegalArgumentException("spacing is less than 0");
        }
        int cap = rounded ? 1 : 2;
        int join = rounded ? 1 : 0;
        float[] array = new float[]{thickness * (length - 1.0f), thickness * (spacing + 1.0f)};
        Border border = BorderFactory.createStrokeBorder(new BasicStroke(thickness, cap, join, thickness * 2.0f, array, 0.0f), paint);
        if (shared) {
            sharedDashedBorder = border;
        }
        return border;
    }

    static {
        sharedEtchedBorder = new EtchedBorder();
        emptyBorder = new EmptyBorder(0, 0, 0, 0);
    }
}

