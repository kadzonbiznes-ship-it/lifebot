/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.annotation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import jdk.internal.misc.Unsafe;
import sun.reflect.annotation.AnnotationType;
import sun.reflect.annotation.AnnotationTypeMismatchExceptionProxy;
import sun.reflect.annotation.ExceptionProxy;

class AnnotationInvocationHandler
implements InvocationHandler,
Serializable {
    private static final long serialVersionUID = 6182022883658399397L;
    private final Class<? extends Annotation> type;
    private final Map<String, Object> memberValues;
    private volatile transient Method[] memberMethods;

    AnnotationInvocationHandler(Class<? extends Annotation> type, Map<String, Object> memberValues) {
        Class<?>[] superInterfaces = type.getInterfaces();
        if (!type.isAnnotation() || superInterfaces.length != 1 || superInterfaces[0] != Annotation.class) {
            throw new AnnotationFormatError("Attempt to create proxy for a non-annotation type: " + type.getName());
        }
        this.type = type;
        this.memberValues = memberValues;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String member = method.getName();
        int parameterCount = method.getParameterCount();
        if (parameterCount == 1 && member == "equals" && method.getParameterTypes()[0] == Object.class) {
            return this.equalsImpl(proxy, args[0]);
        }
        if (parameterCount != 0) {
            throw new AssertionError((Object)"Too many parameters for an annotation method");
        }
        if (member == "toString") {
            return this.toStringImpl();
        }
        if (member == "hashCode") {
            return this.hashCodeImpl();
        }
        if (member == "annotationType") {
            return this.type;
        }
        Object result = this.memberValues.get(member);
        if (result == null) {
            throw new IncompleteAnnotationException(this.type, member);
        }
        if (result instanceof ExceptionProxy) {
            ExceptionProxy exceptProxy = (ExceptionProxy)result;
            throw exceptProxy.generateException();
        }
        if (result.getClass().isArray() && Array.getLength(result) != 0) {
            result = this.cloneArray(result);
        }
        return result;
    }

    private Object cloneArray(Object array) {
        Class<?> type = array.getClass();
        if (type == byte[].class) {
            byte[] byteArray = (byte[])array;
            return byteArray.clone();
        }
        if (type == char[].class) {
            char[] charArray = (char[])array;
            return charArray.clone();
        }
        if (type == double[].class) {
            double[] doubleArray = (double[])array;
            return doubleArray.clone();
        }
        if (type == float[].class) {
            float[] floatArray = (float[])array;
            return floatArray.clone();
        }
        if (type == int[].class) {
            int[] intArray = (int[])array;
            return intArray.clone();
        }
        if (type == long[].class) {
            long[] longArray = (long[])array;
            return longArray.clone();
        }
        if (type == short[].class) {
            short[] shortArray = (short[])array;
            return shortArray.clone();
        }
        if (type == boolean[].class) {
            boolean[] booleanArray = (boolean[])array;
            return booleanArray.clone();
        }
        Object[] objectArray = (Object[])array;
        return objectArray.clone();
    }

    private String toStringImpl() {
        StringBuilder result = new StringBuilder(128);
        result.append('@');
        result.append(Objects.toString(this.type.getCanonicalName(), "<no canonical name>"));
        result.append('(');
        boolean firstMember = true;
        Set<Map.Entry<String, Object>> entries = this.memberValues.entrySet();
        boolean loneValue = entries.size() == 1;
        for (Map.Entry<String, Object> e : entries) {
            if (firstMember) {
                firstMember = false;
            } else {
                result.append(", ");
            }
            String key = e.getKey();
            if (!loneValue || !"value".equals(key)) {
                result.append(key);
                result.append('=');
            }
            loneValue = false;
            result.append(AnnotationInvocationHandler.memberValueToString(e.getValue()));
        }
        result.append(')');
        return result.toString();
    }

    private static String memberValueToString(Object value) {
        Class<?> type = value.getClass();
        if (!type.isArray()) {
            if (type == Class.class) {
                return AnnotationInvocationHandler.toSourceString((Class)value);
            }
            if (type == String.class) {
                return AnnotationInvocationHandler.toSourceString((String)value);
            }
            if (type == Character.class) {
                return AnnotationInvocationHandler.toSourceString(((Character)value).charValue());
            }
            if (type == Double.class) {
                return AnnotationInvocationHandler.toSourceString((Double)value);
            }
            if (type == Float.class) {
                return AnnotationInvocationHandler.toSourceString(((Float)value).floatValue());
            }
            if (type == Long.class) {
                return AnnotationInvocationHandler.toSourceString((Long)value);
            }
            if (type == Byte.class) {
                return AnnotationInvocationHandler.toSourceString((Byte)value);
            }
            if (value instanceof Enum) {
                Enum v = (Enum)value;
                return AnnotationInvocationHandler.toSourceString(v);
            }
            return value.toString();
        }
        Stream<String> stringStream = type == byte[].class ? AnnotationInvocationHandler.convert((byte[])value) : (type == char[].class ? AnnotationInvocationHandler.convert((char[])value) : (type == double[].class ? DoubleStream.of((double[])value).mapToObj(AnnotationInvocationHandler::toSourceString) : (type == float[].class ? AnnotationInvocationHandler.convert((float[])value) : (type == int[].class ? IntStream.of((int[])value).mapToObj(String::valueOf) : (type == long[].class ? LongStream.of((long[])value).mapToObj(AnnotationInvocationHandler::toSourceString) : (type == short[].class ? AnnotationInvocationHandler.convert((short[])value) : (type == boolean[].class ? AnnotationInvocationHandler.convert((boolean[])value) : (type == Class[].class ? Arrays.stream((Class[])value).map(AnnotationInvocationHandler::toSourceString) : (type == String[].class ? Arrays.stream((String[])value).map(AnnotationInvocationHandler::toSourceString) : (type.getComponentType().isEnum() ? Arrays.stream((Enum[])value).map(AnnotationInvocationHandler::toSourceString) : Arrays.stream((Object[])value).map(Objects::toString)))))))))));
        return AnnotationInvocationHandler.stringStreamToString(stringStream);
    }

    private static String toSourceString(Class<?> clazz) {
        return Objects.toString(clazz.getCanonicalName(), "<no canonical name>") + ".class";
    }

    private static String toSourceString(float f) {
        if (Float.isFinite(f)) {
            return Float.toString(f) + "f";
        }
        if (Float.isInfinite(f)) {
            return f < 0.0f ? "-1.0f/0.0f" : "1.0f/0.0f";
        }
        return "0.0f/0.0f";
    }

    private static String toSourceString(double d) {
        if (Double.isFinite(d)) {
            return Double.toString(d);
        }
        if (Double.isInfinite(d)) {
            return d < 0.0 ? "-1.0/0.0" : "1.0/0.0";
        }
        return "0.0/0.0";
    }

    private static String toSourceString(char c) {
        StringBuilder sb = new StringBuilder(4);
        sb.append('\'');
        sb.append(AnnotationInvocationHandler.quote(c));
        return sb.append('\'').toString();
    }

    private static String quote(char ch) {
        switch (ch) {
            case '\b': {
                return "\\b";
            }
            case '\f': {
                return "\\f";
            }
            case '\n': {
                return "\\n";
            }
            case '\r': {
                return "\\r";
            }
            case '\t': {
                return "\\t";
            }
            case '\'': {
                return "\\'";
            }
            case '\"': {
                return "\\\"";
            }
            case '\\': {
                return "\\\\";
            }
        }
        return AnnotationInvocationHandler.isPrintableAscii(ch) ? String.valueOf(ch) : String.format("\\u%04x", ch);
    }

    private static boolean isPrintableAscii(char ch) {
        return ch >= ' ' && ch <= '~';
    }

    private static String toSourceString(byte b) {
        return String.format("(byte)0x%02x", b);
    }

    private static String toSourceString(long ell) {
        return String.valueOf(ell) + "L";
    }

    private static String toSourceString(Enum<?> enumConstant) {
        return enumConstant.name();
    }

    private static String toSourceString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        for (int i = 0; i < s.length(); ++i) {
            sb.append(AnnotationInvocationHandler.quote(s.charAt(i)));
        }
        sb.append('\"');
        return sb.toString();
    }

    private static Stream<String> convert(byte[] values) {
        ArrayList<String> list = new ArrayList<String>(values.length);
        for (byte b : values) {
            list.add(AnnotationInvocationHandler.toSourceString(b));
        }
        return list.stream();
    }

    private static Stream<String> convert(char[] values) {
        ArrayList<String> list = new ArrayList<String>(values.length);
        for (char c : values) {
            list.add(AnnotationInvocationHandler.toSourceString(c));
        }
        return list.stream();
    }

    private static Stream<String> convert(float[] values) {
        ArrayList<String> list = new ArrayList<String>(values.length);
        for (float f : values) {
            list.add(AnnotationInvocationHandler.toSourceString(f));
        }
        return list.stream();
    }

    private static Stream<String> convert(short[] values) {
        ArrayList<String> list = new ArrayList<String>(values.length);
        for (short s : values) {
            list.add(Short.toString(s));
        }
        return list.stream();
    }

    private static Stream<String> convert(boolean[] values) {
        ArrayList<String> list = new ArrayList<String>(values.length);
        for (boolean b : values) {
            list.add(Boolean.toString(b));
        }
        return list.stream();
    }

    private static String stringStreamToString(Stream<String> stream) {
        return stream.collect(Collectors.joining(", ", "{", "}"));
    }

    private Boolean equalsImpl(Object proxy, Object o) {
        if (o == proxy) {
            return true;
        }
        if (!this.type.isInstance(o)) {
            return false;
        }
        for (Method memberMethod : this.getMemberMethods()) {
            if (memberMethod.isSynthetic()) continue;
            String member = memberMethod.getName();
            Object ourValue = this.memberValues.get(member);
            Object hisValue = null;
            AnnotationInvocationHandler hisHandler = this.asOneOfUs(o);
            if (hisHandler != null) {
                hisValue = hisHandler.memberValues.get(member);
            } else {
                try {
                    hisValue = memberMethod.invoke(o, new Object[0]);
                }
                catch (InvocationTargetException e) {
                    return false;
                }
                catch (IllegalAccessException e) {
                    throw new AssertionError((Object)e);
                }
            }
            if (AnnotationInvocationHandler.memberValueEquals(ourValue, hisValue)) continue;
            return false;
        }
        return true;
    }

    private AnnotationInvocationHandler asOneOfUs(Object o) {
        InvocationHandler handler;
        if (Proxy.isProxyClass(o.getClass()) && (handler = Proxy.getInvocationHandler(o)) instanceof AnnotationInvocationHandler) {
            AnnotationInvocationHandler annotationHandler = (AnnotationInvocationHandler)handler;
            return annotationHandler;
        }
        return null;
    }

    private static boolean memberValueEquals(Object v1, Object v2) {
        Class<?> type = v1.getClass();
        if (!type.isArray()) {
            return v1.equals(v2);
        }
        if (v1 instanceof Object[] && v2 instanceof Object[]) {
            return Arrays.equals((Object[])v1, (Object[])v2);
        }
        if (v2.getClass() != type) {
            return false;
        }
        if (type == byte[].class) {
            return Arrays.equals((byte[])v1, (byte[])v2);
        }
        if (type == char[].class) {
            return Arrays.equals((char[])v1, (char[])v2);
        }
        if (type == double[].class) {
            return Arrays.equals((double[])v1, (double[])v2);
        }
        if (type == float[].class) {
            return Arrays.equals((float[])v1, (float[])v2);
        }
        if (type == int[].class) {
            return Arrays.equals((int[])v1, (int[])v2);
        }
        if (type == long[].class) {
            return Arrays.equals((long[])v1, (long[])v2);
        }
        if (type == short[].class) {
            return Arrays.equals((short[])v1, (short[])v2);
        }
        assert (type == boolean[].class);
        return Arrays.equals((boolean[])v1, (boolean[])v2);
    }

    private Method[] getMemberMethods() {
        Method[] value = this.memberMethods;
        if (value == null) {
            value = this.computeMemberMethods();
            this.memberMethods = value;
        }
        return value;
    }

    private Method[] computeMemberMethods() {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

            @Override
            public Method[] run() {
                AccessibleObject[] methods = AnnotationInvocationHandler.this.type.getDeclaredMethods();
                AnnotationInvocationHandler.this.validateAnnotationMethods((Method[])methods);
                AccessibleObject.setAccessible(methods, true);
                return methods;
            }
        });
    }

    private void validateAnnotationMethods(Method[] memberMethods) {
        boolean valid = true;
        Method currentMethod = null;
        Method[] methodArray = memberMethods;
        int n = methodArray.length;
        for (int i = 0; i < n; ++i) {
            Method method;
            currentMethod = method = methodArray[i];
            int modifiers = method.getModifiers();
            if (method.isSynthetic() && (modifiers & 0xA) != 0 && method.getParameterCount() == 0) continue;
            if (modifiers != 1025 || method.isDefault() || method.getParameterCount() != 0 || method.getExceptionTypes().length != 0) {
                valid = false;
                break;
            }
            Class<?> returnType = method.getReturnType();
            if (returnType.isArray() && (returnType = returnType.getComponentType()).isArray()) {
                valid = false;
                break;
            }
            if (!(returnType.isPrimitive() && returnType != Void.TYPE || returnType == String.class || returnType == Class.class || returnType.isEnum() || returnType.isAnnotation())) {
                valid = false;
                break;
            }
            String methodName = method.getName();
            if (!(methodName.equals("toString") && returnType == String.class || methodName.equals("hashCode") && returnType == Integer.TYPE) && (!methodName.equals("annotationType") || returnType != Class.class)) continue;
            valid = false;
            break;
        }
        if (valid) {
            return;
        }
        throw new AnnotationFormatError("Malformed method on an annotation type: " + currentMethod.toString());
    }

    private int hashCodeImpl() {
        int result = 0;
        for (Map.Entry<String, Object> e : this.memberValues.entrySet()) {
            result += 127 * e.getKey().hashCode() ^ AnnotationInvocationHandler.memberValueHashCode(e.getValue());
        }
        return result;
    }

    private static int memberValueHashCode(Object value) {
        Class<?> type = value.getClass();
        if (!type.isArray()) {
            return value.hashCode();
        }
        if (type == byte[].class) {
            return Arrays.hashCode((byte[])value);
        }
        if (type == char[].class) {
            return Arrays.hashCode((char[])value);
        }
        if (type == double[].class) {
            return Arrays.hashCode((double[])value);
        }
        if (type == float[].class) {
            return Arrays.hashCode((float[])value);
        }
        if (type == int[].class) {
            return Arrays.hashCode((int[])value);
        }
        if (type == long[].class) {
            return Arrays.hashCode((long[])value);
        }
        if (type == short[].class) {
            return Arrays.hashCode((short[])value);
        }
        if (type == boolean[].class) {
            return Arrays.hashCode((boolean[])value);
        }
        return Arrays.hashCode((Object[])value);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        Class t = (Class)fields.get("type", null);
        Map streamVals = (Map)fields.get("memberValues", null);
        AnnotationType annotationType = null;
        try {
            annotationType = AnnotationType.getInstance(t);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidObjectException("Non-annotation type in annotation serial stream");
        }
        Map<String, Class<?>> memberTypes = annotationType.memberTypes();
        LinkedHashMap<String, Object> mv = new LinkedHashMap<String, Object>();
        for (Map.Entry memberValue : streamVals.entrySet()) {
            String name = (String)memberValue.getKey();
            AnnotationTypeMismatchExceptionProxy value = null;
            Class<?> memberType = memberTypes.get(name);
            if (memberType != null && !memberType.isInstance(value = (AnnotationTypeMismatchExceptionProxy)memberValue.getValue()) && !(value instanceof ExceptionProxy)) {
                value = new AnnotationTypeMismatchExceptionProxy(Objects.toIdentityString(value)).setMember(annotationType.members().get(name));
            }
            mv.put(name, value);
        }
        UnsafeAccessor.setType(this, t);
        UnsafeAccessor.setMemberValues(this, mv);
    }

    Map<String, Object> memberValues() {
        return Collections.unmodifiableMap(this.memberValues);
    }

    private static class UnsafeAccessor {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private static final long typeOffset = unsafe.objectFieldOffset(AnnotationInvocationHandler.class, "type");
        private static final long memberValuesOffset = unsafe.objectFieldOffset(AnnotationInvocationHandler.class, "memberValues");

        private UnsafeAccessor() {
        }

        static void setType(AnnotationInvocationHandler o, Class<? extends Annotation> type) {
            unsafe.putReference(o, typeOffset, type);
        }

        static void setMemberValues(AnnotationInvocationHandler o, Map<String, Object> memberValues) {
            unsafe.putReference(o, memberValuesOffset, memberValues);
        }
    }
}

