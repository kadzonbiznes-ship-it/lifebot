/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.event.MouseEvent;

public class MouseWheelEvent
extends MouseEvent {
    public static final int WHEEL_UNIT_SCROLL = 0;
    public static final int WHEEL_BLOCK_SCROLL = 1;
    int scrollType;
    int scrollAmount;
    int wheelRotation;
    double preciseWheelRotation;
    private static final long serialVersionUID = 6459879390515399677L;

    public MouseWheelEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation) {
        this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation);
    }

    public MouseWheelEvent(Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation) {
        this(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation, wheelRotation);
    }

    public MouseWheelEvent(Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation, double preciseWheelRotation) {
        super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, 0);
        this.scrollType = scrollType;
        this.scrollAmount = scrollAmount;
        this.wheelRotation = wheelRotation;
        this.preciseWheelRotation = preciseWheelRotation;
    }

    public int getScrollType() {
        return this.scrollType;
    }

    public int getScrollAmount() {
        return this.scrollAmount;
    }

    public int getWheelRotation() {
        return this.wheelRotation;
    }

    public double getPreciseWheelRotation() {
        return this.preciseWheelRotation;
    }

    public int getUnitsToScroll() {
        return this.scrollAmount * this.wheelRotation;
    }

    @Override
    public String paramString() {
        String scrollTypeStr = null;
        scrollTypeStr = this.getScrollType() == 0 ? "WHEEL_UNIT_SCROLL" : (this.getScrollType() == 1 ? "WHEEL_BLOCK_SCROLL" : "unknown scroll type");
        return super.paramString() + ",scrollType=" + scrollTypeStr + ",scrollAmount=" + this.getScrollAmount() + ",wheelRotation=" + this.getWheelRotation() + ",preciseWheelRotation=" + this.getPreciseWheelRotation();
    }
}

