/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.event.ListSelectionListener;

public interface ListSelectionModel {
    public static final int SINGLE_SELECTION = 0;
    public static final int SINGLE_INTERVAL_SELECTION = 1;
    public static final int MULTIPLE_INTERVAL_SELECTION = 2;

    public void setSelectionInterval(int var1, int var2);

    public void addSelectionInterval(int var1, int var2);

    public void removeSelectionInterval(int var1, int var2);

    public int getMinSelectionIndex();

    public int getMaxSelectionIndex();

    public boolean isSelectedIndex(int var1);

    public int getAnchorSelectionIndex();

    public void setAnchorSelectionIndex(int var1);

    public int getLeadSelectionIndex();

    public void setLeadSelectionIndex(int var1);

    public void clearSelection();

    public boolean isSelectionEmpty();

    public void insertIndexInterval(int var1, int var2, boolean var3);

    public void removeIndexInterval(int var1, int var2);

    public void setValueIsAdjusting(boolean var1);

    public boolean getValueIsAdjusting();

    public void setSelectionMode(int var1);

    public int getSelectionMode();

    public void addListSelectionListener(ListSelectionListener var1);

    public void removeListSelectionListener(ListSelectionListener var1);

    default public int[] getSelectedIndices() {
        int iMin = this.getMinSelectionIndex();
        int iMax = this.getMaxSelectionIndex();
        if (iMin < 0 || iMax < 0) {
            return new int[0];
        }
        int[] rvTmp = new int[1 + (iMax - iMin)];
        int n = 0;
        for (int i = iMin; i <= iMax; ++i) {
            if (!this.isSelectedIndex(i)) continue;
            rvTmp[n++] = i;
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }

    default public int getSelectedItemsCount() {
        int iMin = this.getMinSelectionIndex();
        int iMax = this.getMaxSelectionIndex();
        int count = 0;
        for (int i = iMin; i <= iMax; ++i) {
            if (!this.isSelectedIndex(i)) continue;
            ++count;
        }
        return count;
    }
}

