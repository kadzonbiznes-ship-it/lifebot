/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.event.ChangeListener;

public interface BoundedRangeModel {
    public int getMinimum();

    public void setMinimum(int var1);

    public int getMaximum();

    public void setMaximum(int var1);

    public int getValue();

    public void setValue(int var1);

    public void setValueIsAdjusting(boolean var1);

    public boolean getValueIsAdjusting();

    public int getExtent();

    public void setExtent(int var1);

    public void setRangeProperties(int var1, int var2, int var3, int var4, boolean var5);

    public void addChangeListener(ChangeListener var1);

    public void removeChangeListener(ChangeListener var1);
}

