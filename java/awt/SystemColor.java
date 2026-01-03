/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.ObjectStreamException;
import java.io.Serializable;
import sun.awt.AWTAccessor;

public final class SystemColor
extends Color
implements Serializable {
    public static final int DESKTOP = 0;
    public static final int ACTIVE_CAPTION = 1;
    public static final int ACTIVE_CAPTION_TEXT = 2;
    public static final int ACTIVE_CAPTION_BORDER = 3;
    public static final int INACTIVE_CAPTION = 4;
    public static final int INACTIVE_CAPTION_TEXT = 5;
    public static final int INACTIVE_CAPTION_BORDER = 6;
    public static final int WINDOW = 7;
    public static final int WINDOW_BORDER = 8;
    public static final int WINDOW_TEXT = 9;
    public static final int MENU = 10;
    public static final int MENU_TEXT = 11;
    public static final int TEXT = 12;
    public static final int TEXT_TEXT = 13;
    public static final int TEXT_HIGHLIGHT = 14;
    public static final int TEXT_HIGHLIGHT_TEXT = 15;
    public static final int TEXT_INACTIVE_TEXT = 16;
    public static final int CONTROL = 17;
    public static final int CONTROL_TEXT = 18;
    public static final int CONTROL_HIGHLIGHT = 19;
    public static final int CONTROL_LT_HIGHLIGHT = 20;
    public static final int CONTROL_SHADOW = 21;
    public static final int CONTROL_DK_SHADOW = 22;
    public static final int SCROLLBAR = 23;
    public static final int INFO = 24;
    public static final int INFO_TEXT = 25;
    public static final int NUM_COLORS = 26;
    private static int[] systemColors = new int[]{-16753572, -16777088, -1, -4144960, -8355712, -4144960, -4144960, -1, -16777216, -16777216, -4144960, -16777216, -4144960, -16777216, -16777088, -1, -8355712, -4144960, -16777216, -1, -2039584, -8355712, -16777216, -2039584, -2039808, -16777216};
    public static final SystemColor desktop = new SystemColor(0);
    public static final SystemColor activeCaption = new SystemColor(1);
    public static final SystemColor activeCaptionText = new SystemColor(2);
    public static final SystemColor activeCaptionBorder = new SystemColor(3);
    public static final SystemColor inactiveCaption = new SystemColor(4);
    public static final SystemColor inactiveCaptionText = new SystemColor(5);
    public static final SystemColor inactiveCaptionBorder = new SystemColor(6);
    public static final SystemColor window = new SystemColor(7);
    public static final SystemColor windowBorder = new SystemColor(8);
    public static final SystemColor windowText = new SystemColor(9);
    public static final SystemColor menu = new SystemColor(10);
    public static final SystemColor menuText = new SystemColor(11);
    public static final SystemColor text = new SystemColor(12);
    public static final SystemColor textText = new SystemColor(13);
    public static final SystemColor textHighlight = new SystemColor(14);
    public static final SystemColor textHighlightText = new SystemColor(15);
    public static final SystemColor textInactiveText = new SystemColor(16);
    public static final SystemColor control = new SystemColor(17);
    public static final SystemColor controlText = new SystemColor(18);
    public static final SystemColor controlHighlight = new SystemColor(19);
    public static final SystemColor controlLtHighlight = new SystemColor(20);
    public static final SystemColor controlShadow = new SystemColor(21);
    public static final SystemColor controlDkShadow = new SystemColor(22);
    public static final SystemColor scrollbar = new SystemColor(23);
    public static final SystemColor info = new SystemColor(24);
    public static final SystemColor infoText = new SystemColor(25);
    private static final long serialVersionUID = 4503142729533789064L;
    private transient int index;
    private static SystemColor[] systemColorObjects = new SystemColor[]{desktop, activeCaption, activeCaptionText, activeCaptionBorder, inactiveCaption, inactiveCaptionText, inactiveCaptionBorder, window, windowBorder, windowText, menu, menuText, text, textText, textHighlight, textHighlightText, textInactiveText, control, controlText, controlHighlight, controlLtHighlight, controlShadow, controlDkShadow, scrollbar, info, infoText};

    private static void updateSystemColors() {
        if (!GraphicsEnvironment.isHeadless()) {
            Toolkit.getDefaultToolkit().loadSystemColors(systemColors);
        }
        for (int i = 0; i < systemColors.length; ++i) {
            SystemColor.systemColorObjects[i].value = systemColors[i];
        }
    }

    private SystemColor(byte index) {
        super(systemColors[index]);
        this.index = index;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[i=" + this.index + "]";
    }

    private Object readResolve() {
        return systemColorObjects[this.value];
    }

    private Object writeReplace() throws ObjectStreamException {
        SystemColor color = new SystemColor((byte)this.index);
        color.value = this.index;
        return color;
    }

    static {
        AWTAccessor.setSystemColorAccessor(SystemColor::updateSystemColors);
        SystemColor.updateSystemColors();
    }
}

