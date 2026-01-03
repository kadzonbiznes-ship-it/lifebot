/*
 * Decompiled with CFR 0.152.
 */
package sun.text;

public final class SupplementaryCharacterData
implements Cloneable {
    private static final byte IGNORE = -1;
    private int[] dataTable;

    public SupplementaryCharacterData(int[] table) {
        this.dataTable = table;
    }

    public int getValue(int index) {
        int k;
        assert (index >= 65536 && index <= 0x10FFFF) : "Invalid code point:" + Integer.toHexString(index);
        int i = 0;
        int j = this.dataTable.length - 1;
        while (true) {
            k = (i + j) / 2;
            int start = this.dataTable[k] >> 8;
            int end = this.dataTable[k + 1] >> 8;
            if (index < start) {
                j = k;
                continue;
            }
            if (index <= end - 1) break;
            i = k;
        }
        int v = this.dataTable[k] & 0xFF;
        return v == 255 ? -1 : v;
    }

    public int[] getArray() {
        return this.dataTable;
    }
}

