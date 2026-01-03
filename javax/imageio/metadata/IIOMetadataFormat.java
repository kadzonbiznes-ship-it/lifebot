/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.metadata;

import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;

public interface IIOMetadataFormat {
    public static final int CHILD_POLICY_EMPTY = 0;
    public static final int CHILD_POLICY_ALL = 1;
    public static final int CHILD_POLICY_SOME = 2;
    public static final int CHILD_POLICY_CHOICE = 3;
    public static final int CHILD_POLICY_SEQUENCE = 4;
    public static final int CHILD_POLICY_REPEAT = 5;
    public static final int CHILD_POLICY_MAX = 5;
    public static final int VALUE_NONE = 0;
    public static final int VALUE_ARBITRARY = 1;
    public static final int VALUE_RANGE = 2;
    public static final int VALUE_RANGE_MIN_INCLUSIVE_MASK = 4;
    public static final int VALUE_RANGE_MAX_INCLUSIVE_MASK = 8;
    public static final int VALUE_RANGE_MIN_INCLUSIVE = 6;
    public static final int VALUE_RANGE_MAX_INCLUSIVE = 10;
    public static final int VALUE_RANGE_MIN_MAX_INCLUSIVE = 14;
    public static final int VALUE_ENUMERATION = 16;
    public static final int VALUE_LIST = 32;
    public static final int DATATYPE_STRING = 0;
    public static final int DATATYPE_BOOLEAN = 1;
    public static final int DATATYPE_INTEGER = 2;
    public static final int DATATYPE_FLOAT = 3;
    public static final int DATATYPE_DOUBLE = 4;

    public String getRootName();

    public boolean canNodeAppear(String var1, ImageTypeSpecifier var2);

    public int getElementMinChildren(String var1);

    public int getElementMaxChildren(String var1);

    public String getElementDescription(String var1, Locale var2);

    public int getChildPolicy(String var1);

    public String[] getChildNames(String var1);

    public String[] getAttributeNames(String var1);

    public int getAttributeValueType(String var1, String var2);

    public int getAttributeDataType(String var1, String var2);

    public boolean isAttributeRequired(String var1, String var2);

    public String getAttributeDefaultValue(String var1, String var2);

    public String[] getAttributeEnumerations(String var1, String var2);

    public String getAttributeMinValue(String var1, String var2);

    public String getAttributeMaxValue(String var1, String var2);

    public int getAttributeListMinLength(String var1, String var2);

    public int getAttributeListMaxLength(String var1, String var2);

    public String getAttributeDescription(String var1, String var2, Locale var3);

    public int getObjectValueType(String var1);

    public Class<?> getObjectClass(String var1);

    public Object getObjectDefaultValue(String var1);

    public Object[] getObjectEnumerations(String var1);

    public Comparable<?> getObjectMinValue(String var1);

    public Comparable<?> getObjectMaxValue(String var1);

    public int getObjectArrayMinLength(String var1);

    public int getObjectArrayMaxLength(String var1);
}

