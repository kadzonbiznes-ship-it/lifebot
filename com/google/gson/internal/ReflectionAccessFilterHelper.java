/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.ReflectionAccessFilter
 *  com.google.gson.internal.ReflectionAccessFilterHelper$AccessChecker
 */
package com.google.gson.internal;

import com.google.gson.ReflectionAccessFilter;
import com.google.gson.internal.ReflectionAccessFilterHelper;
import java.lang.reflect.AccessibleObject;
import java.util.List;

public class ReflectionAccessFilterHelper {
    private ReflectionAccessFilterHelper() {
    }

    public static boolean isJavaType(Class<?> c) {
        return ReflectionAccessFilterHelper.isJavaType(c.getName());
    }

    private static boolean isJavaType(String className) {
        return className.startsWith("java.") || className.startsWith("javax.");
    }

    public static boolean isAndroidType(Class<?> c) {
        return ReflectionAccessFilterHelper.isAndroidType(c.getName());
    }

    private static boolean isAndroidType(String className) {
        return className.startsWith("android.") || className.startsWith("androidx.") || ReflectionAccessFilterHelper.isJavaType(className);
    }

    public static boolean isAnyPlatformType(Class<?> c) {
        String className = c.getName();
        return ReflectionAccessFilterHelper.isAndroidType(className) || className.startsWith("kotlin.") || className.startsWith("kotlinx.") || className.startsWith("scala.");
    }

    public static ReflectionAccessFilter.FilterResult getFilterResult(List<ReflectionAccessFilter> reflectionFilters, Class<?> c) {
        for (ReflectionAccessFilter filter : reflectionFilters) {
            ReflectionAccessFilter.FilterResult result = filter.check(c);
            if (result == ReflectionAccessFilter.FilterResult.INDECISIVE) continue;
            return result;
        }
        return ReflectionAccessFilter.FilterResult.ALLOW;
    }

    public static boolean canAccess(AccessibleObject accessibleObject, Object object) {
        return AccessChecker.INSTANCE.canAccess(accessibleObject, object);
    }
}

