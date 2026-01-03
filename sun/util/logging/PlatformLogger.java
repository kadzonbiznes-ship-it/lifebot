/*
 * Decompiled with CFR 0.152.
 */
package sun.util.logging;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import jdk.internal.logger.LazyLoggers;
import jdk.internal.logger.LoggerWrapper;

public class PlatformLogger {
    private static final Map<String, WeakReference<PlatformLogger>> loggers = new HashMap<String, WeakReference<PlatformLogger>>();
    private final Bridge loggerProxy;
    private static final Level[] spi2platformLevelMapping = new Level[]{Level.ALL, Level.FINER, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF};

    public static synchronized PlatformLogger getLogger(String name) {
        PlatformLogger log = null;
        WeakReference<PlatformLogger> ref = loggers.get(name);
        if (ref != null) {
            log = (PlatformLogger)ref.get();
        }
        if (log == null) {
            log = new PlatformLogger(Bridge.convert(LazyLoggers.getLazyLogger(name, PlatformLogger.class.getModule())));
            loggers.put(name, new WeakReference<PlatformLogger>(log));
        }
        return log;
    }

    private PlatformLogger(Bridge loggerProxy) {
        this.loggerProxy = loggerProxy;
    }

    public boolean isEnabled() {
        return this.loggerProxy.isEnabled();
    }

    public String getName() {
        return this.loggerProxy.getName();
    }

    public boolean isLoggable(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        return this.loggerProxy.isLoggable(level);
    }

    public Level level() {
        ConfigurableBridge.LoggerConfiguration spi = ConfigurableBridge.getLoggerConfiguration(this.loggerProxy);
        return spi == null ? null : spi.getPlatformLevel();
    }

    @Deprecated
    public void setLevel(Level newLevel) {
        ConfigurableBridge.LoggerConfiguration spi = ConfigurableBridge.getLoggerConfiguration(this.loggerProxy);
        if (spi != null) {
            spi.setPlatformLevel(newLevel);
        }
    }

    public void severe(String msg) {
        this.loggerProxy.log(Level.SEVERE, msg, (Object[])null);
    }

    public void severe(String msg, Throwable t) {
        this.loggerProxy.log(Level.SEVERE, msg, t);
    }

    public void severe(String msg, Object ... params) {
        this.loggerProxy.log(Level.SEVERE, msg, params);
    }

    public void warning(String msg) {
        this.loggerProxy.log(Level.WARNING, msg, (Object[])null);
    }

    public void warning(String msg, Throwable t) {
        this.loggerProxy.log(Level.WARNING, msg, t);
    }

    public void warning(String msg, Object ... params) {
        this.loggerProxy.log(Level.WARNING, msg, params);
    }

    public void info(String msg) {
        this.loggerProxy.log(Level.INFO, msg, (Object[])null);
    }

    public void info(String msg, Throwable t) {
        this.loggerProxy.log(Level.INFO, msg, t);
    }

    public void info(String msg, Object ... params) {
        this.loggerProxy.log(Level.INFO, msg, params);
    }

    public void config(String msg) {
        this.loggerProxy.log(Level.CONFIG, msg, (Object[])null);
    }

    public void config(String msg, Throwable t) {
        this.loggerProxy.log(Level.CONFIG, msg, t);
    }

    public void config(String msg, Object ... params) {
        this.loggerProxy.log(Level.CONFIG, msg, params);
    }

    public void fine(String msg) {
        this.loggerProxy.log(Level.FINE, msg, (Object[])null);
    }

    public void fine(String msg, Throwable t) {
        this.loggerProxy.log(Level.FINE, msg, t);
    }

    public void fine(String msg, Object ... params) {
        this.loggerProxy.log(Level.FINE, msg, params);
    }

    public void finer(String msg) {
        this.loggerProxy.log(Level.FINER, msg, (Object[])null);
    }

    public void finer(String msg, Throwable t) {
        this.loggerProxy.log(Level.FINER, msg, t);
    }

    public void finer(String msg, Object ... params) {
        this.loggerProxy.log(Level.FINER, msg, params);
    }

    public void finest(String msg) {
        this.loggerProxy.log(Level.FINEST, msg, (Object[])null);
    }

    public void finest(String msg, Throwable t) {
        this.loggerProxy.log(Level.FINEST, msg, t);
    }

    public void finest(String msg, Object ... params) {
        this.loggerProxy.log(Level.FINEST, msg, params);
    }

    public static Level toPlatformLevel(System.Logger.Level level) {
        if (level == null) {
            return null;
        }
        assert (level.ordinal() < spi2platformLevelMapping.length);
        return spi2platformLevelMapping[level.ordinal()];
    }

    public static interface Bridge {
        public String getName();

        public boolean isLoggable(Level var1);

        public boolean isEnabled();

        public void log(Level var1, String var2);

        public void log(Level var1, String var2, Throwable var3);

        public void log(Level var1, String var2, Object ... var3);

        public void log(Level var1, Supplier<String> var2);

        public void log(Level var1, Throwable var2, Supplier<String> var3);

        public void logp(Level var1, String var2, String var3, String var4);

        public void logp(Level var1, String var2, String var3, Supplier<String> var4);

        public void logp(Level var1, String var2, String var3, String var4, Object ... var5);

        public void logp(Level var1, String var2, String var3, String var4, Throwable var5);

        public void logp(Level var1, String var2, String var3, Throwable var4, Supplier<String> var5);

        public void logrb(Level var1, String var2, String var3, ResourceBundle var4, String var5, Object ... var6);

        public void logrb(Level var1, String var2, String var3, ResourceBundle var4, String var5, Throwable var6);

        public void logrb(Level var1, ResourceBundle var2, String var3, Object ... var4);

        public void logrb(Level var1, ResourceBundle var2, String var3, Throwable var4);

        public static Bridge convert(System.Logger logger) {
            if (logger instanceof Bridge) {
                return (Bridge)((Object)logger);
            }
            return new LoggerWrapper<System.Logger>(logger);
        }
    }

    public static enum Level {
        ALL(System.Logger.Level.ALL),
        FINEST(System.Logger.Level.TRACE),
        FINER(System.Logger.Level.TRACE),
        FINE(System.Logger.Level.DEBUG),
        CONFIG(System.Logger.Level.DEBUG),
        INFO(System.Logger.Level.INFO),
        WARNING(System.Logger.Level.WARNING),
        SEVERE(System.Logger.Level.ERROR),
        OFF(System.Logger.Level.OFF);

        final System.Logger.Level systemLevel;
        private static final int SEVERITY_OFF = Integer.MAX_VALUE;
        private static final int SEVERITY_SEVERE = 1000;
        private static final int SEVERITY_WARNING = 900;
        private static final int SEVERITY_INFO = 800;
        private static final int SEVERITY_CONFIG = 700;
        private static final int SEVERITY_FINE = 500;
        private static final int SEVERITY_FINER = 400;
        private static final int SEVERITY_FINEST = 300;
        private static final int SEVERITY_ALL = Integer.MIN_VALUE;
        private static final int[] LEVEL_VALUES;

        private Level(System.Logger.Level systemLevel) {
            this.systemLevel = systemLevel;
        }

        public System.Logger.Level systemLevel() {
            return this.systemLevel;
        }

        public int intValue() {
            return LEVEL_VALUES[this.ordinal()];
        }

        public static Level valueOf(int level) {
            switch (level) {
                case 300: {
                    return FINEST;
                }
                case 500: {
                    return FINE;
                }
                case 400: {
                    return FINER;
                }
                case 800: {
                    return INFO;
                }
                case 900: {
                    return WARNING;
                }
                case 700: {
                    return CONFIG;
                }
                case 1000: {
                    return SEVERE;
                }
                case 0x7FFFFFFF: {
                    return OFF;
                }
                case -2147483648: {
                    return ALL;
                }
            }
            int i = Arrays.binarySearch(LEVEL_VALUES, 0, LEVEL_VALUES.length - 2, level);
            return Level.values()[i >= 0 ? i : -i - 1];
        }

        static {
            LEVEL_VALUES = new int[]{Integer.MIN_VALUE, 300, 400, 500, 700, 800, 900, 1000, Integer.MAX_VALUE};
        }
    }

    public static interface ConfigurableBridge {
        default public LoggerConfiguration getLoggerConfiguration() {
            return null;
        }

        public static LoggerConfiguration getLoggerConfiguration(Bridge logger) {
            if (logger instanceof ConfigurableBridge) {
                return ((ConfigurableBridge)((Object)logger)).getLoggerConfiguration();
            }
            return null;
        }

        public static abstract class LoggerConfiguration {
            public abstract Level getPlatformLevel();

            public abstract void setPlatformLevel(Level var1);
        }
    }
}

