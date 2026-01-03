/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class SimpleConsoleLogger
extends PlatformLogger.ConfigurableBridge.LoggerConfiguration
implements System.Logger,
PlatformLogger.Bridge,
PlatformLogger.ConfigurableBridge {
    static final System.Logger.Level DEFAULT_LEVEL = SimpleConsoleLogger.getDefaultLevel();
    static final PlatformLogger.Level DEFAULT_PLATFORM_LEVEL = PlatformLogger.toPlatformLevel(DEFAULT_LEVEL);
    final String name;
    volatile PlatformLogger.Level level;
    final boolean usePlatformLevel;

    static System.Logger.Level getDefaultLevel() {
        String levelName = GetPropertyAction.privilegedGetProperty("jdk.system.logger.level", "INFO");
        try {
            return System.Logger.Level.valueOf(levelName);
        }
        catch (IllegalArgumentException iae) {
            return System.Logger.Level.INFO;
        }
    }

    SimpleConsoleLogger(String name, boolean usePlatformLevel) {
        this.name = name;
        this.usePlatformLevel = usePlatformLevel;
    }

    String getSimpleFormatString() {
        return Formatting.SIMPLE_CONSOLE_LOGGER_FORMAT;
    }

    PlatformLogger.Level defaultPlatformLevel() {
        return DEFAULT_PLATFORM_LEVEL;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    private Enum<?> logLevel(PlatformLogger.Level level) {
        return this.usePlatformLevel ? level : level.systemLevel();
    }

    private Enum<?> logLevel(System.Logger.Level level) {
        return this.usePlatformLevel ? PlatformLogger.toPlatformLevel(level) : level;
    }

    @Override
    public final boolean isLoggable(System.Logger.Level level) {
        return this.isLoggable(PlatformLogger.toPlatformLevel(level));
    }

    @Override
    public final void log(System.Logger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
        if (this.isLoggable(level)) {
            if (bundle != null) {
                key = SimpleConsoleLogger.getString(bundle, key);
            }
            this.publish(this.getCallerInfo(), this.logLevel(level), key, thrown);
        }
    }

    @Override
    public final void log(System.Logger.Level level, ResourceBundle bundle, String format, Object ... params) {
        if (this.isLoggable(level)) {
            if (bundle != null) {
                format = SimpleConsoleLogger.getString(bundle, format);
            }
            this.publish(this.getCallerInfo(), this.logLevel(level), format, params);
        }
    }

    @Override
    public final boolean isLoggable(PlatformLogger.Level level) {
        PlatformLogger.Level effectiveLevel = this.effectiveLevel();
        return level != PlatformLogger.Level.OFF && level.ordinal() >= effectiveLevel.ordinal();
    }

    @Override
    public final boolean isEnabled() {
        return this.level != PlatformLogger.Level.OFF;
    }

    @Override
    public final void log(PlatformLogger.Level level, String msg) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(), this.logLevel(level), msg);
        }
    }

    @Override
    public final void log(PlatformLogger.Level level, String msg, Throwable thrown) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(), this.logLevel(level), msg, thrown);
        }
    }

    @Override
    public final void log(PlatformLogger.Level level, String msg, Object ... params) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(), this.logLevel(level), msg, params);
        }
    }

    private PlatformLogger.Level effectiveLevel() {
        if (this.level == null) {
            return this.defaultPlatformLevel();
        }
        return this.level;
    }

    @Override
    public final PlatformLogger.Level getPlatformLevel() {
        return this.level;
    }

    @Override
    public final void setPlatformLevel(PlatformLogger.Level newLevel) {
        this.level = newLevel;
    }

    @Override
    public final PlatformLogger.ConfigurableBridge.LoggerConfiguration getLoggerConfiguration() {
        return this;
    }

    static PrintStream outputStream() {
        return System.err;
    }

    private String getCallerInfo() {
        Optional<StackWalker.StackFrame> frame = new CallerFinder().get();
        if (frame.isPresent()) {
            return frame.get().getClassName() + " " + frame.get().getMethodName();
        }
        return this.name;
    }

    private String getCallerInfo(String sourceClassName, String sourceMethodName) {
        if (sourceClassName == null) {
            return this.name;
        }
        if (sourceMethodName == null) {
            return sourceClassName;
        }
        return sourceClassName + " " + sourceMethodName;
    }

    private String toString(Throwable thrown) {
        String throwable = "";
        if (thrown != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            thrown.printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return throwable;
    }

    private synchronized String format(Enum<?> level, String msg, Throwable thrown, String callerInfo) {
        ZonedDateTime zdt = ZonedDateTime.now();
        String throwable = this.toString(thrown);
        return String.format(this.getSimpleFormatString(), zdt, callerInfo, this.name, level.name(), msg, throwable);
    }

    private void publish(String callerInfo, Enum<?> level, String msg) {
        SimpleConsoleLogger.outputStream().print(this.format(level, msg, null, callerInfo));
    }

    private void publish(String callerInfo, Enum<?> level, String msg, Throwable thrown) {
        SimpleConsoleLogger.outputStream().print(this.format(level, msg, thrown, callerInfo));
    }

    private void publish(String callerInfo, Enum<?> level, String msg, Object ... params) {
        msg = params == null || params.length == 0 ? msg : Formatting.formatMessage(msg, params);
        SimpleConsoleLogger.outputStream().print(this.format(level, msg, null, callerInfo));
    }

    public static SimpleConsoleLogger makeSimpleLogger(String name) {
        return new SimpleConsoleLogger(name, false);
    }

    @Override
    public final void log(PlatformLogger.Level level, Supplier<String> msgSupplier) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(), this.logLevel(level), msgSupplier.get());
        }
    }

    @Override
    public final void log(PlatformLogger.Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(), this.logLevel(level), msgSupplier.get(), thrown);
        }
    }

    @Override
    public final void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msg);
        }
    }

    @Override
    public final void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msgSupplier.get());
        }
    }

    @Override
    public final void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Object ... params) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msg, params);
        }
    }

    @Override
    public final void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msg, thrown);
        }
    }

    @Override
    public final void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        if (this.isLoggable(level)) {
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msgSupplier.get(), thrown);
        }
    }

    @Override
    public final void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String key, Object ... params) {
        if (this.isLoggable(level)) {
            String msg = bundle == null ? key : SimpleConsoleLogger.getString(bundle, key);
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msg, params);
        }
    }

    @Override
    public final void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String key, Throwable thrown) {
        if (this.isLoggable(level)) {
            String msg = bundle == null ? key : SimpleConsoleLogger.getString(bundle, key);
            this.publish(this.getCallerInfo(sourceClass, sourceMethod), this.logLevel(level), msg, thrown);
        }
    }

    @Override
    public final void logrb(PlatformLogger.Level level, ResourceBundle bundle, String key, Object ... params) {
        if (this.isLoggable(level)) {
            String msg = bundle == null ? key : SimpleConsoleLogger.getString(bundle, key);
            this.publish(this.getCallerInfo(), this.logLevel(level), msg, params);
        }
    }

    @Override
    public final void logrb(PlatformLogger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
        if (this.isLoggable(level)) {
            String msg = bundle == null ? key : SimpleConsoleLogger.getString(bundle, key);
            this.publish(this.getCallerInfo(), this.logLevel(level), msg, thrown);
        }
    }

    static String getString(ResourceBundle bundle, String key) {
        if (bundle == null || key == null) {
            return key;
        }
        try {
            return bundle.getString(key);
        }
        catch (MissingResourceException x) {
            return key;
        }
    }

    static final class Formatting {
        static final String DEFAULT_FORMAT = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
        static final String DEFAULT_FORMAT_PROP_KEY = "jdk.system.logger.format";
        static final String JUL_FORMAT_PROP_KEY = "java.util.logging.SimpleFormatter.format";
        static final String SIMPLE_CONSOLE_LOGGER_FORMAT = Formatting.getSimpleFormat("jdk.system.logger.format", null);
        private static final String[] skips;

        Formatting() {
        }

        static boolean isFilteredFrame(StackWalker.StackFrame st) {
            char c;
            if (System.Logger.class.isAssignableFrom(st.getDeclaringClass())) {
                return true;
            }
            String cname = st.getClassName();
            char c2 = c = cname.length() < 12 ? (char)'\u0000' : cname.charAt(0);
            if (c == 's') {
                if (cname.startsWith("sun.util.logging.")) {
                    return true;
                }
                if (cname.startsWith("sun.rmi.runtime.Log")) {
                    return true;
                }
            } else if (c == 'j') {
                if (cname.startsWith("jdk.internal.logger.BootstrapLogger$LogEvent")) {
                    return false;
                }
                if (cname.startsWith("jdk.internal.logger.")) {
                    return true;
                }
                if (cname.startsWith("java.util.logging.")) {
                    return true;
                }
                if (cname.startsWith("java.lang.invoke.MethodHandle")) {
                    return true;
                }
                if (cname.startsWith("java.security.AccessController")) {
                    return true;
                }
            }
            if (skips.length > 0) {
                for (int i = 0; i < skips.length; ++i) {
                    if (skips[i].isEmpty() || !cname.startsWith(skips[i])) continue;
                    return true;
                }
            }
            return false;
        }

        static String getSimpleFormat(String key, Function<String, String> defaultPropertyGetter) {
            if (!DEFAULT_FORMAT_PROP_KEY.equals(key) && !JUL_FORMAT_PROP_KEY.equals(key)) {
                throw new IllegalArgumentException("Invalid property name: " + key);
            }
            String format = GetPropertyAction.privilegedGetProperty(key);
            if (format == null && defaultPropertyGetter != null) {
                format = defaultPropertyGetter.apply(key);
            }
            if (format != null) {
                try {
                    String.format(format, ZonedDateTime.now(), "", "", "", "", "");
                }
                catch (IllegalArgumentException e) {
                    format = DEFAULT_FORMAT;
                }
            } else {
                format = DEFAULT_FORMAT;
            }
            return format;
        }

        static String formatMessage(String format, Object ... parameters) {
            try {
                if (parameters == null || parameters.length == 0) {
                    return format;
                }
                boolean isJavaTestFormat = false;
                int len = format.length();
                for (int i = 0; i < len - 2; ++i) {
                    char d;
                    char c = format.charAt(i);
                    if (c != '{' || (d = format.charAt(i + 1)) < '0' || d > '9') continue;
                    isJavaTestFormat = true;
                    break;
                }
                if (isJavaTestFormat) {
                    return MessageFormat.format(format, parameters);
                }
                return format;
            }
            catch (Exception ex) {
                return format;
            }
        }

        static {
            String additionalPkgs = GetPropertyAction.privilegedGetProperty("jdk.logger.packages");
            skips = additionalPkgs == null ? new String[]{} : additionalPkgs.split(",");
        }
    }

    static final class CallerFinder
    implements Predicate<StackWalker.StackFrame> {
        private static final StackWalker WALKER;
        private boolean lookingForLogger = true;

        CallerFinder() {
        }

        Optional<StackWalker.StackFrame> get() {
            return WALKER.walk(s -> s.filter(this).findFirst());
        }

        @Override
        public boolean test(StackWalker.StackFrame t) {
            String cname = t.getClassName();
            if (this.lookingForLogger) {
                this.lookingForLogger = !this.isLoggerImplFrame(cname);
                return false;
            }
            return !Formatting.isFilteredFrame(t);
        }

        private boolean isLoggerImplFrame(String cname) {
            return cname.equals("sun.util.logging.PlatformLogger") || cname.equals("jdk.internal.logger.SimpleConsoleLogger");
        }

        static {
            PrivilegedAction<StackWalker> action = new PrivilegedAction<StackWalker>(){

                @Override
                public StackWalker run() {
                    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
                }
            };
            WALKER = AccessController.doPrivileged(action);
        }
    }
}

