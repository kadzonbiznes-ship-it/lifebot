/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ComponentOrientation
implements Serializable {
    private static final long serialVersionUID = -4113291392143563828L;
    private static final int UNK_BIT = 1;
    private static final int HORIZ_BIT = 2;
    private static final int LTR_BIT = 4;
    public static final ComponentOrientation LEFT_TO_RIGHT = new ComponentOrientation(6);
    public static final ComponentOrientation RIGHT_TO_LEFT = new ComponentOrientation(2);
    public static final ComponentOrientation UNKNOWN = new ComponentOrientation(7);
    private int orientation;

    public boolean isHorizontal() {
        return (this.orientation & 2) != 0;
    }

    public boolean isLeftToRight() {
        return (this.orientation & 4) != 0;
    }

    public static ComponentOrientation getOrientation(Locale locale) {
        return switch (locale.getLanguage()) {
            case "ar", "fa", "he", "iw", "ji", "ur", "yi" -> RIGHT_TO_LEFT;
            default -> LEFT_TO_RIGHT;
        };
    }

    @Deprecated
    public static ComponentOrientation getOrientation(ResourceBundle bdl) {
        ComponentOrientation result = null;
        try {
            result = (ComponentOrientation)bdl.getObject("Orientation");
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (result == null) {
            result = ComponentOrientation.getOrientation(bdl.getLocale());
        }
        if (result == null) {
            result = ComponentOrientation.getOrientation(Locale.getDefault());
        }
        return result;
    }

    private ComponentOrientation(int value) {
        this.orientation = value;
    }
}

