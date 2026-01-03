/*
 * Decompiled with CFR 0.152.
 */
package sun.util.logging.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;
import jdk.internal.logger.DefaultLoggerFinder;
import sun.util.logging.PlatformLogger;

public final class LoggingProviderImpl
extends DefaultLoggerFinder {
    static final RuntimePermission LOGGERFINDER_PERMISSION = new RuntimePermission("loggerFinder");
    private static final LoggingPermission LOGGING_CONTROL_PERMISSION = new LoggingPermission("control", null);
    private static volatile LogManagerAccess logManagerAccess;

    private static Logger demandJULLoggerFor(String name, Module module) {
        LogManager manager = LogManager.getLogManager();
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return logManagerAccess.demandLoggerFor(manager, name, module);
        }
        PrivilegedAction<Logger> pa = () -> logManagerAccess.demandLoggerFor(manager, name, module);
        return AccessController.doPrivileged(pa, null, LOGGING_CONTROL_PERMISSION);
    }

    @Override
    protected System.Logger demandLoggerFor(String name, Module module) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(LOGGERFINDER_PERMISSION);
        }
        return JULWrapper.of(LoggingProviderImpl.demandJULLoggerFor(name, module));
    }

    public static LogManagerAccess getLogManagerAccess() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(LOGGING_CONTROL_PERMISSION);
        }
        if (logManagerAccess == null) {
            LogManager.getLogManager();
        }
        return logManagerAccess;
    }

    public static void setLogManagerAccess(LogManagerAccess accesLoggers) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(LOGGING_CONTROL_PERMISSION);
        }
        logManagerAccess = accesLoggers;
    }

    public static interface LogManagerAccess {
        public Logger demandLoggerFor(LogManager var1, String var2, Module var3);
    }

    static final class JULWrapper
    extends PlatformLogger.ConfigurableBridge.LoggerConfiguration
    implements System.Logger,
    PlatformLogger.Bridge,
    PlatformLogger.ConfigurableBridge {
        private static final Level[] spi2JulLevelMapping = new Level[]{Level.ALL, Level.FINER, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF};
        private static final Level[] platform2JulLevelMapping = new Level[]{Level.ALL, Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF};
        private final Logger julLogger;

        private JULWrapper(Logger logger) {
            this.julLogger = logger;
        }

        @Override
        public String getName() {
            return this.julLogger.getName();
        }

        @Override
        public void log(PlatformLogger.Level level, String msg, Throwable throwable) {
            this.julLogger.log(JULWrapper.toJUL(level), msg, throwable);
        }

        @Override
        public void log(PlatformLogger.Level level, String format, Object ... params) {
            this.julLogger.log(JULWrapper.toJUL(level), format, params);
        }

        @Override
        public void log(PlatformLogger.Level level, String msg) {
            this.julLogger.log(JULWrapper.toJUL(level), msg);
        }

        @Override
        public void log(PlatformLogger.Level level, Supplier<String> msgSuppier) {
            this.julLogger.log(JULWrapper.toJUL(level), msgSuppier);
        }

        @Override
        public void log(PlatformLogger.Level level, Throwable thrown, Supplier<String> msgSuppier) {
            this.julLogger.log(JULWrapper.toJUL(level), thrown, msgSuppier);
        }

        @Override
        public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String key, Throwable throwable) {
            this.julLogger.logrb(JULWrapper.toJUL(level), bundle, key, throwable);
        }

        @Override
        public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String key, Object ... params) {
            this.julLogger.logrb(JULWrapper.toJUL(level), bundle, key, params);
        }

        @Override
        public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg) {
            this.julLogger.logp(JULWrapper.toJUL(level), sourceClass, sourceMethod, msg);
        }

        @Override
        public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
            this.julLogger.logp(JULWrapper.toJUL(level), sourceClass, sourceMethod, msgSupplier);
        }

        @Override
        public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Object ... params) {
            this.julLogger.logp(JULWrapper.toJUL(level), sourceClass, sourceMethod, msg, params);
        }

        @Override
        public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
            this.julLogger.logp(JULWrapper.toJUL(level), sourceClass, sourceMethod, msg, thrown);
        }

        @Override
        public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
            this.julLogger.logp(JULWrapper.toJUL(level), sourceClass, sourceMethod, thrown, msgSupplier);
        }

        @Override
        public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String key, Object ... params) {
            this.julLogger.logrb(JULWrapper.toJUL(level), sourceClass, sourceMethod, bundle, key, params);
        }

        @Override
        public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String key, Throwable thrown) {
            this.julLogger.logrb(JULWrapper.toJUL(level), sourceClass, sourceMethod, bundle, key, thrown);
        }

        @Override
        public boolean isLoggable(PlatformLogger.Level level) {
            return this.julLogger.isLoggable(JULWrapper.toJUL(level));
        }

        @Override
        public boolean isLoggable(System.Logger.Level level) {
            return this.julLogger.isLoggable(JULWrapper.toJUL(level));
        }

        @Override
        public void log(System.Logger.Level level, String msg) {
            this.julLogger.log(JULWrapper.toJUL(level), msg);
        }

        @Override
        public void log(System.Logger.Level level, Supplier<String> msgSupplier) {
            Objects.requireNonNull(msgSupplier);
            this.julLogger.log(JULWrapper.toJUL(level), msgSupplier);
        }

        @Override
        public void log(System.Logger.Level level, Object obj) {
            Objects.requireNonNull(obj);
            this.julLogger.log(JULWrapper.toJUL(level), () -> obj.toString());
        }

        @Override
        public void log(System.Logger.Level level, String msg, Throwable thrown) {
            this.julLogger.log(JULWrapper.toJUL(level), msg, thrown);
        }

        @Override
        public void log(System.Logger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
            Objects.requireNonNull(msgSupplier);
            this.julLogger.log(JULWrapper.toJUL(level), thrown, msgSupplier);
        }

        @Override
        public void log(System.Logger.Level level, String format, Object ... params) {
            this.julLogger.log(JULWrapper.toJUL(level), format, params);
        }

        @Override
        public void log(System.Logger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
            this.julLogger.logrb(JULWrapper.toJUL(level), bundle, key, thrown);
        }

        @Override
        public void log(System.Logger.Level level, ResourceBundle bundle, String format, Object ... params) {
            this.julLogger.logrb(JULWrapper.toJUL(level), bundle, format, params);
        }

        static Level toJUL(System.Logger.Level level) {
            if (level == null) {
                return null;
            }
            assert (level.ordinal() < spi2JulLevelMapping.length);
            return spi2JulLevelMapping[level.ordinal()];
        }

        @Override
        public boolean isEnabled() {
            return this.julLogger.getLevel() != Level.OFF;
        }

        @Override
        public PlatformLogger.Level getPlatformLevel() {
            Level javaLevel = this.julLogger.getLevel();
            if (javaLevel == null) {
                return null;
            }
            try {
                return PlatformLogger.Level.valueOf(javaLevel.getName());
            }
            catch (IllegalArgumentException e) {
                return PlatformLogger.Level.valueOf(javaLevel.intValue());
            }
        }

        @Override
        public void setPlatformLevel(PlatformLogger.Level level) {
            this.julLogger.setLevel(JULWrapper.toJUL(level));
        }

        @Override
        public PlatformLogger.ConfigurableBridge.LoggerConfiguration getLoggerConfiguration() {
            return this;
        }

        static Level toJUL(PlatformLogger.Level level) {
            if (level == null) {
                return null;
            }
            assert (level.ordinal() < platform2JulLevelMapping.length);
            return platform2JulLevelMapping[level.ordinal()];
        }

        public boolean equals(Object obj) {
            return obj instanceof JULWrapper && obj.getClass() == this.getClass() && ((JULWrapper)obj).julLogger == this.julLogger;
        }

        public int hashCode() {
            return this.julLogger.hashCode();
        }

        static JULWrapper of(Logger logger) {
            return new JULWrapper(logger);
        }
    }
}

