/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.module;

import java.util.Set;

public final class Checks {
    private static final Set<String> RESERVED = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "_");

    private Checks() {
    }

    public static String requireModuleName(String name) {
        int next;
        if (name == null) {
            throw new IllegalArgumentException("Null module name");
        }
        int off = 0;
        while ((next = name.indexOf(46, off)) != -1) {
            String id = name.substring(off, next);
            if (!Checks.isJavaIdentifier(id)) {
                throw new IllegalArgumentException(name + ": Invalid module name: '" + id + "' is not a Java identifier");
            }
            off = next + 1;
        }
        String last = name.substring(off);
        if (!Checks.isJavaIdentifier(last)) {
            throw new IllegalArgumentException(name + ": Invalid module name: '" + last + "' is not a Java identifier");
        }
        return name;
    }

    public static String requirePackageName(String name) {
        return Checks.requireTypeName("package name", name);
    }

    public static boolean isPackageName(String name) {
        return Checks.isTypeName(name);
    }

    public static String requireServiceTypeName(String name) {
        return Checks.requireQualifiedClassName("service type name", name);
    }

    public static String requireServiceProviderName(String name) {
        return Checks.requireQualifiedClassName("service provider name", name);
    }

    public static String requireQualifiedClassName(String what, String name) {
        Checks.requireTypeName(what, name);
        if (name.indexOf(46) == -1) {
            throw new IllegalArgumentException(name + ": is not a qualified name of a Java class in a named package");
        }
        return name;
    }

    public static boolean isClassName(String name) {
        return Checks.isTypeName(name);
    }

    private static boolean isTypeName(String name) {
        int next;
        int off = 0;
        while ((next = name.indexOf(46, off)) != -1) {
            String id = name.substring(off, next);
            if (!Checks.isJavaIdentifier(id)) {
                return false;
            }
            off = next + 1;
        }
        String last = name.substring(off);
        return Checks.isJavaIdentifier(last);
    }

    private static String requireTypeName(String what, String name) {
        int next;
        if (name == null) {
            throw new IllegalArgumentException("Null " + what);
        }
        int off = 0;
        while ((next = name.indexOf(46, off)) != -1) {
            String id = name.substring(off, next);
            if (!Checks.isJavaIdentifier(id)) {
                throw new IllegalArgumentException(name + ": Invalid " + what + ": '" + id + "' is not a Java identifier");
            }
            off = next + 1;
        }
        String last = name.substring(off);
        if (!Checks.isJavaIdentifier(last)) {
            throw new IllegalArgumentException(name + ": Invalid " + what + ": '" + last + "' is not a Java identifier");
        }
        return name;
    }

    public static boolean isJavaIdentifier(String str) {
        int cp;
        if (str.isEmpty() || RESERVED.contains(str)) {
            return false;
        }
        int first = Character.codePointAt(str, 0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }
        for (int i = Character.charCount(first); i < str.length(); i += Character.charCount(cp)) {
            cp = Character.codePointAt(str, i);
            if (Character.isJavaIdentifierPart(cp)) continue;
            return false;
        }
        return true;
    }
}

