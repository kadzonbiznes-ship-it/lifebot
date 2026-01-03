/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.plugins.tiff;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFTag {
    public static final int TIFF_BYTE = 1;
    public static final int TIFF_ASCII = 2;
    public static final int TIFF_SHORT = 3;
    public static final int TIFF_LONG = 4;
    public static final int TIFF_RATIONAL = 5;
    public static final int TIFF_SBYTE = 6;
    public static final int TIFF_UNDEFINED = 7;
    public static final int TIFF_SSHORT = 8;
    public static final int TIFF_SLONG = 9;
    public static final int TIFF_SRATIONAL = 10;
    public static final int TIFF_FLOAT = 11;
    public static final int TIFF_DOUBLE = 12;
    public static final int TIFF_IFD_POINTER = 13;
    public static final int MIN_DATATYPE = 1;
    public static final int MAX_DATATYPE = 13;
    public static final String UNKNOWN_TAG_NAME = "UnknownTag";
    private static final int DISALLOWED_DATATYPES_MASK = -16384;
    private static final int[] SIZE_OF_TYPE = new int[]{0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 4};
    private int number;
    private String name;
    private int dataTypes;
    private int count;
    private TIFFTagSet tagSet = null;
    private SortedMap<Integer, String> valueNames = null;

    public TIFFTag(String name, int number, int dataTypes, int count) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (number < 0) {
            throw new IllegalArgumentException("number (" + number + ") < 0");
        }
        if (dataTypes < 0 || (dataTypes & 0xFFFFC000) != 0) {
            throw new IllegalArgumentException("dataTypes out of range");
        }
        this.name = name;
        this.number = number;
        this.dataTypes = dataTypes;
        this.count = count;
    }

    public TIFFTag(String name, int number, TIFFTagSet tagSet) {
        this(name, number, 8208, 1);
        if (tagSet == null) {
            throw new NullPointerException("tagSet == null");
        }
        this.tagSet = tagSet;
    }

    public TIFFTag(String name, int number, int dataTypes) {
        this(name, number, dataTypes, -1);
    }

    public static int getSizeOfType(int dataType) {
        if (dataType < 1 || dataType > 13) {
            throw new IllegalArgumentException("dataType out of range!");
        }
        return SIZE_OF_TYPE[dataType];
    }

    public String getName() {
        return this.name;
    }

    public int getNumber() {
        return this.number;
    }

    public int getDataTypes() {
        return this.dataTypes;
    }

    public int getCount() {
        return this.count;
    }

    public boolean isDataTypeOK(int dataType) {
        if (dataType < 1 || dataType > 13) {
            throw new IllegalArgumentException("datatype not in range!");
        }
        return (this.dataTypes & 1 << dataType) != 0;
    }

    public TIFFTagSet getTagSet() {
        return this.tagSet;
    }

    public boolean isIFDPointer() {
        return this.tagSet != null || this.isDataTypeOK(13);
    }

    public boolean hasValueNames() {
        return this.valueNames != null;
    }

    protected void addValueName(int value, String name) {
        if (this.valueNames == null) {
            this.valueNames = new TreeMap<Integer, String>();
        }
        this.valueNames.put(value, name);
    }

    public String getValueName(int value) {
        if (this.valueNames == null) {
            return null;
        }
        return (String)this.valueNames.get(value);
    }

    public int[] getNamedValues() {
        int[] intValues = null;
        if (this.valueNames != null) {
            Set<Integer> values = this.valueNames.keySet();
            intValues = new int[values.size()];
            int i = 0;
            for (int value : values) {
                intValues[i++] = value;
            }
        }
        return intValues;
    }
}

