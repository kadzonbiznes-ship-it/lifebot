/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.event.AdjustmentListener;

public interface Adjustable {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int NO_ORIENTATION = 2;

    public int getOrientation();

    public void setMinimum(int var1);

    public int getMinimum();

    public void setMaximum(int var1);

    public int getMaximum();

    public void setUnitIncrement(int var1);

    public int getUnitIncrement();

    public void setBlockIncrement(int var1);

    public int getBlockIncrement();

    public void setVisibleAmount(int var1);

    public int getVisibleAmount();

    public void setValue(int var1);

    public int getValue();

    public void addAdjustmentListener(AdjustmentListener var1);

    public void removeAdjustmentListener(AdjustmentListener var1);
}

