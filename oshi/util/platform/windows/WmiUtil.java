/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.util.Constants
 */
package oshi.util.platform.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.time.OffsetDateTime;
import java.util.Locale;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.Constants;
import oshi.util.ParseUtil;

@ThreadSafe
public final class WmiUtil {
    public static final String OHM_NAMESPACE = "ROOT\\OpenHardwareMonitor";
    private static final String CLASS_CAST_MSG = "%s is not a %s type. CIM Type is %d and VT type is %d";

    private WmiUtil() {
    }

    public static <T extends Enum<T>> String queryToString(WbemcliUtil.WmiQuery<T> query) {
        Enum[] props = (Enum[])query.getPropertyEnum().getEnumConstants();
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(props[0].name());
        for (int i = 1; i < props.length; ++i) {
            sb.append(',').append(props[i].name());
        }
        sb.append(" FROM ").append(query.getWmiClassName());
        return sb.toString();
    }

    public static <T extends Enum<T>> String getString(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 8) {
            return WmiUtil.getStr(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "String", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> String getDateString(WbemcliUtil.WmiResult<T> result, T property, int index) {
        OffsetDateTime dateTime = WmiUtil.getDateTime(result, property, index);
        if (dateTime.equals(Constants.UNIX_EPOCH)) {
            return "";
        }
        return dateTime.toLocalDate().toString();
    }

    public static <T extends Enum<T>> OffsetDateTime getDateTime(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 101) {
            return ParseUtil.parseCimDateTimeToOffset(WmiUtil.getStr(result, property, index));
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "DateTime", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> String getRefString(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 102) {
            return WmiUtil.getStr(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "Reference", result.getCIMType(property), result.getVtType(property)));
    }

    private static <T extends Enum<T>> String getStr(WbemcliUtil.WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return "";
        }
        if (result.getVtType(property) == 8) {
            return (String)o;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "String-mapped", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> long getUint64(WbemcliUtil.WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0L;
        }
        if (result.getCIMType(property) == 21 && result.getVtType(property) == 8) {
            return ParseUtil.parseLongOrDefault((String)o, 0L);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT64", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> int getUint32(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 19) {
            return WmiUtil.getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT32", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> long getUint32asLong(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 19) {
            return (long)WmiUtil.getInt(result, property, index) & 0xFFFFFFFFL;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT32", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> int getSint32(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 3) {
            return WmiUtil.getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "SINT32", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> int getUint16(WbemcliUtil.WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == 18) {
            return WmiUtil.getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT16", result.getCIMType(property), result.getVtType(property)));
    }

    private static <T extends Enum<T>> int getInt(WbemcliUtil.WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0;
        }
        if (result.getVtType(property) == 3) {
            return (Integer)o;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "32-bit integer", result.getCIMType(property), result.getVtType(property)));
    }

    public static <T extends Enum<T>> float getFloat(WbemcliUtil.WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0.0f;
        }
        if (result.getCIMType(property) == 4 && result.getVtType(property) == 4) {
            return ((Float)o).floatValue();
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "Float", result.getCIMType(property), result.getVtType(property)));
    }
}

