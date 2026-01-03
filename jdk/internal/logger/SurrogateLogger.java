/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.util.function.Function;
import jdk.internal.logger.SimpleConsoleLogger;
import sun.util.logging.PlatformLogger;

public final class SurrogateLogger
extends SimpleConsoleLogger {
    private static final PlatformLogger.Level JUL_DEFAULT_LEVEL = PlatformLogger.Level.INFO;
    private static volatile String simpleFormatString;

    SurrogateLogger(String name) {
        super(name, true);
    }

    @Override
    PlatformLogger.Level defaultPlatformLevel() {
        return JUL_DEFAULT_LEVEL;
    }

    @Override
    String getSimpleFormatString() {
        if (simpleFormatString == null) {
            simpleFormatString = SurrogateLogger.getSimpleFormat(null);
        }
        return simpleFormatString;
    }

    public static String getSimpleFormat(Function<String, String> defaultPropertyGetter) {
        return SimpleConsoleLogger.Formatting.getSimpleFormat("java.util.logging.SimpleFormatter.format", defaultPropertyGetter);
    }

    public static SurrogateLogger makeSurrogateLogger(String name) {
        return new SurrogateLogger(name);
    }

    public static boolean isFilteredFrame(StackWalker.StackFrame st) {
        return SimpleConsoleLogger.Formatting.isFilteredFrame(st);
    }
}

