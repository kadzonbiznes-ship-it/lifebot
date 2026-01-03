/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import jdk.internal.logger.DefaultLoggerFinder;
import jdk.internal.logger.LazyLoggers;
import jdk.internal.logger.SurrogateLogger;
import jdk.internal.misc.InnocuousThread;
import jdk.internal.misc.VM;
import sun.util.logging.PlatformLogger;

public final class BootstrapLogger
implements System.Logger,
PlatformLogger.Bridge,
PlatformLogger.ConfigurableBridge {
    final LazyLoggers.LazyLoggerAccessor holder;
    final BooleanSupplier isLoadingThread;
    private static volatile BooleanSupplier isBooted;
    private static volatile boolean logManagerConfigured;

    boolean isLoadingThread() {
        return this.isLoadingThread != null && this.isLoadingThread.getAsBoolean();
    }

    BootstrapLogger(LazyLoggers.LazyLoggerAccessor holder, BooleanSupplier isLoadingThread) {
        this.holder = holder;
        this.isLoadingThread = isLoadingThread;
    }

    void push(LogEvent log) {
        BootstrapExecutors.enqueue(log);
        this.checkBootstrapping();
    }

    void flush(LogEvent event) {
        assert (event.bootstrap == this);
        if (event.platformLevel != null) {
            PlatformLogger.Bridge concrete = this.holder.getConcretePlatformLogger(this);
            LogEvent.log(event, concrete);
        } else {
            System.Logger concrete = this.holder.getConcreteLogger(this);
            LogEvent.log(event, concrete);
        }
    }

    @Override
    public String getName() {
        return this.holder.name;
    }

    boolean checkBootstrapping() {
        if (BootstrapLogger.isBooted() && !this.isLoadingThread()) {
            BootstrapExecutors.flush();
            this.holder.getConcreteLogger(this);
            return false;
        }
        return true;
    }

    @Override
    public boolean isLoggable(System.Logger.Level level) {
        if (this.checkBootstrapping()) {
            return level.getSeverity() >= System.Logger.Level.INFO.getSeverity();
        }
        System.Logger spi = this.holder.wrapped();
        return spi.isLoggable(level);
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, bundle, key, thrown));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, bundle, key, thrown);
        }
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle bundle, String format, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, bundle, format, params));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, bundle, format, params);
        }
    }

    @Override
    public void log(System.Logger.Level level, String msg, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, null, msg, thrown));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, msg, thrown);
        }
    }

    @Override
    public void log(System.Logger.Level level, String format, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, null, format, params));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, format, params);
        }
    }

    @Override
    public void log(System.Logger.Level level, Supplier<String> msgSupplier) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msgSupplier));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, msgSupplier);
        }
    }

    @Override
    public void log(System.Logger.Level level, Object obj) {
        if (this.checkBootstrapping()) {
            System.Logger.super.log(level, obj);
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, obj);
        }
    }

    @Override
    public void log(System.Logger.Level level, String msg) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, null, msg, (Object[])null));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, msg);
        }
    }

    @Override
    public void log(System.Logger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msgSupplier, thrown));
        } else {
            System.Logger spi = this.holder.wrapped();
            spi.log(level, msgSupplier, thrown);
        }
    }

    @Override
    public boolean isLoggable(PlatformLogger.Level level) {
        if (this.checkBootstrapping()) {
            return level.intValue() >= PlatformLogger.Level.INFO.intValue();
        }
        PlatformLogger.Bridge spi = this.holder.platform();
        return spi.isLoggable(level);
    }

    @Override
    public boolean isEnabled() {
        if (this.checkBootstrapping()) {
            return true;
        }
        PlatformLogger.Bridge spi = this.holder.platform();
        return spi.isEnabled();
    }

    @Override
    public void log(PlatformLogger.Level level, String msg) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msg));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.log(level, msg);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, String msg, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msg, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.log(level, msg, thrown);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, String msg, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msg, params));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.log(level, msg, params);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, Supplier<String> msgSupplier) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, msgSupplier));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.log(level, msgSupplier);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.vaueOf(this, level, msgSupplier, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.log(level, thrown, msgSupplier);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, null, msg, (Object[])null));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logp(level, sourceClass, sourceMethod, msg);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, msgSupplier, null));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logp(level, sourceClass, sourceMethod, msgSupplier);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, null, msg, params));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logp(level, sourceClass, sourceMethod, msg, params);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, null, msg, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logp(level, sourceClass, sourceMethod, msg, thrown);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, msgSupplier, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logp(level, sourceClass, sourceMethod, thrown, msgSupplier);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, bundle, msg, params));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logrb(level, sourceClass, sourceMethod, bundle, msg, params);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, sourceClass, sourceMethod, bundle, msg, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logrb(level, sourceClass, sourceMethod, bundle, msg, thrown);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String msg, Object ... params) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, null, null, bundle, msg, params));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logrb(level, bundle, msg, params);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        if (this.checkBootstrapping()) {
            this.push(LogEvent.valueOf(this, level, null, null, bundle, msg, thrown));
        } else {
            PlatformLogger.Bridge spi = this.holder.platform();
            spi.logrb(level, bundle, msg, thrown);
        }
    }

    @Override
    public PlatformLogger.ConfigurableBridge.LoggerConfiguration getLoggerConfiguration() {
        if (this.checkBootstrapping()) {
            return PlatformLogger.ConfigurableBridge.super.getLoggerConfiguration();
        }
        PlatformLogger.Bridge spi = this.holder.platform();
        return PlatformLogger.ConfigurableBridge.getLoggerConfiguration(spi);
    }

    public static boolean isBooted() {
        if (isBooted != null) {
            return isBooted.getAsBoolean();
        }
        return VM.isBooted();
    }

    private static boolean useSurrogateLoggers() {
        if (!BootstrapLogger.isBooted()) {
            return true;
        }
        return DetectBackend.detectedBackend == LoggingBackend.JUL_DEFAULT && !logManagerConfigured;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean useLazyLoggers() {
        if (!BootstrapLogger.isBooted() || DetectBackend.detectedBackend == LoggingBackend.CUSTOM) {
            return true;
        }
        Class<BootstrapLogger> clazz = BootstrapLogger.class;
        synchronized (BootstrapLogger.class) {
            // ** MonitorExit[var0] (shouldn't be in output)
            return BootstrapLogger.useSurrogateLoggers();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static System.Logger getLogger(LazyLoggers.LazyLoggerAccessor accessor, BooleanSupplier isLoading) {
        if (!BootstrapLogger.isBooted() || isLoading != null && isLoading.getAsBoolean()) {
            return new BootstrapLogger(accessor, isLoading);
        }
        if (!BootstrapLogger.useSurrogateLoggers()) return accessor.createLogger();
        Class<BootstrapLogger> clazz = BootstrapLogger.class;
        synchronized (BootstrapLogger.class) {
            if (!BootstrapLogger.useSurrogateLoggers()) return accessor.createLogger();
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return BootstrapLogger.createSurrogateLogger(accessor);
        }
    }

    static void ensureBackendDetected() {
        assert (VM.isBooted()) : "VM is not booted";
        LoggingBackend backend = DetectBackend.detectedBackend;
    }

    static synchronized System.Logger createSurrogateLogger(LazyLoggers.LazyLoggerAccessor a) {
        return RedirectedLoggers.INSTANCE.get(a);
    }

    private static synchronized Map<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> releaseSurrogateLoggers() {
        boolean releaseSurrogateLoggers = BootstrapLogger.useSurrogateLoggers();
        logManagerConfigured = true;
        if (releaseSurrogateLoggers) {
            return RedirectedLoggers.INSTANCE.drainLoggersMap();
        }
        return null;
    }

    public static void redirectTemporaryLoggers() {
        Map<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> accessors = BootstrapLogger.releaseSurrogateLoggers();
        if (accessors != null) {
            RedirectedLoggers.replaceSurrogateLoggers(accessors);
        }
        BootstrapExecutors.flush();
    }

    static void awaitPendingTasks() {
        BootstrapExecutors.awaitPendingTasks();
    }

    static boolean isAlive() {
        return BootstrapExecutors.isAlive();
    }

    private static class BootstrapExecutors
    implements ThreadFactory {
        static final long KEEP_EXECUTOR_ALIVE_SECONDS = 30L;
        private static volatile WeakReference<ExecutorService> executorRef;
        static LogEvent head;
        static LogEvent tail;

        private BootstrapExecutors() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static ExecutorService getExecutor() {
            ExecutorService executor;
            WeakReference<ExecutorService> ref = executorRef;
            ExecutorService executorService = executor = ref == null ? null : (ExecutorService)ref.get();
            if (executor != null) {
                return executor;
            }
            Class<BootstrapExecutors> clazz = BootstrapExecutors.class;
            synchronized (BootstrapExecutors.class) {
                ref = executorRef;
                ExecutorService executorService2 = executor = ref == null ? null : (ExecutorService)ref.get();
                if (executor == null) {
                    executor = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new BootstrapExecutors());
                }
                executorRef = new WeakReference<ExecutorService>(executor);
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return (ExecutorService)executorRef.get();
            }
        }

        @Override
        public Thread newThread(final Runnable r) {
            final ExecutorService owner = BootstrapExecutors.getExecutor();
            Thread thread = AccessController.doPrivileged(new PrivilegedAction<Thread>(){

                @Override
                public Thread run() {
                    Thread t = InnocuousThread.newThread(new BootstrapMessageLoggerTask(owner, r));
                    t.setName("BootstrapMessageLoggerTask-" + t.getName());
                    return t;
                }
            }, null, new RuntimePermission("enableContextClassLoaderOverride"));
            thread.setDaemon(true);
            return thread;
        }

        static void submit(Runnable r) {
            BootstrapExecutors.getExecutor().execute(r);
        }

        static void join(Runnable r) {
            try {
                BootstrapExecutors.getExecutor().submit(r).get();
            }
            catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         * Converted monitor instructions to comments
         * Lifted jumps to return sites
         */
        static void awaitPendingTasks() {
            ExecutorService executor;
            WeakReference<ExecutorService> ref = executorRef;
            ExecutorService executorService = executor = ref == null ? null : (ExecutorService)ref.get();
            if (ref == null) {
                Class<BootstrapExecutors> clazz = BootstrapExecutors.class;
                // MONITORENTER : jdk.internal.logger.BootstrapLogger$BootstrapExecutors.class
                ref = executorRef;
                executor = ref == null ? null : (ExecutorService)ref.get();
                // MONITOREXIT : clazz
            }
            if (executor == null) return;
            BootstrapExecutors.join(() -> {});
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static boolean isAlive() {
            WeakReference<ExecutorService> ref = executorRef;
            if (ref != null && !ref.refersTo(null)) {
                return true;
            }
            Class<BootstrapExecutors> clazz = BootstrapExecutors.class;
            synchronized (BootstrapExecutors.class) {
                ref = executorRef;
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return ref != null && !ref.refersTo(null);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static void enqueue(LogEvent event) {
            if (event.next != null) {
                return;
            }
            Class<BootstrapExecutors> clazz = BootstrapExecutors.class;
            synchronized (BootstrapExecutors.class) {
                if (event.next != null) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return;
                }
                event.next = event;
                if (tail == null) {
                    head = tail = event;
                } else {
                    BootstrapExecutors.tail.next = event;
                    tail = event;
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         * Converted monitor instructions to comments
         * Lifted jumps to return sites
         */
        static void flush() {
            Class<BootstrapExecutors> clazz = BootstrapExecutors.class;
            // MONITORENTER : jdk.internal.logger.BootstrapLogger$BootstrapExecutors.class
            LogEvent event = head;
            tail = null;
            head = null;
            // MONITOREXIT : clazz
            while (event != null) {
                LogEvent.log(event);
                clazz = BootstrapExecutors.class;
                // MONITORENTER : jdk.internal.logger.BootstrapLogger$BootstrapExecutors.class
                LogEvent prev = event;
                event = event.next == event ? null : event.next;
                prev.next = null;
                // MONITOREXIT : clazz
            }
        }

        private static class BootstrapMessageLoggerTask
        implements Runnable {
            ExecutorService owner;
            Runnable run;

            public BootstrapMessageLoggerTask(ExecutorService owner, Runnable r) {
                this.owner = owner;
                this.run = r;
            }

            @Override
            public void run() {
                try {
                    this.run.run();
                }
                finally {
                    this.owner = null;
                }
            }
        }
    }

    static final class LogEvent {
        final System.Logger.Level level;
        final PlatformLogger.Level platformLevel;
        final BootstrapLogger bootstrap;
        final ResourceBundle bundle;
        final String msg;
        final Throwable thrown;
        final Object[] params;
        final Supplier<String> msgSupplier;
        final String sourceClass;
        final String sourceMethod;
        final long timeMillis;
        final long nanoAdjustment;
        final AccessControlContext acc = AccessController.getContext();
        LogEvent next;

        private LogEvent(BootstrapLogger bootstrap, System.Logger.Level level, ResourceBundle bundle, String msg, Throwable thrown, Object[] params) {
            this.timeMillis = System.currentTimeMillis();
            this.nanoAdjustment = VM.getNanoTimeAdjustment(this.timeMillis);
            this.level = level;
            this.platformLevel = null;
            this.bundle = bundle;
            this.msg = msg;
            this.msgSupplier = null;
            this.thrown = thrown;
            this.params = params;
            this.sourceClass = null;
            this.sourceMethod = null;
            this.bootstrap = bootstrap;
        }

        private LogEvent(BootstrapLogger bootstrap, System.Logger.Level level, Supplier<String> msgSupplier, Throwable thrown, Object[] params) {
            this.timeMillis = System.currentTimeMillis();
            this.nanoAdjustment = VM.getNanoTimeAdjustment(this.timeMillis);
            this.level = level;
            this.platformLevel = null;
            this.bundle = null;
            this.msg = null;
            this.msgSupplier = msgSupplier;
            this.thrown = thrown;
            this.params = params;
            this.sourceClass = null;
            this.sourceMethod = null;
            this.bootstrap = bootstrap;
        }

        private LogEvent(BootstrapLogger bootstrap, PlatformLogger.Level platformLevel, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown, Object[] params) {
            this.timeMillis = System.currentTimeMillis();
            this.nanoAdjustment = VM.getNanoTimeAdjustment(this.timeMillis);
            this.level = null;
            this.platformLevel = platformLevel;
            this.bundle = bundle;
            this.msg = msg;
            this.msgSupplier = null;
            this.thrown = thrown;
            this.params = params;
            this.sourceClass = sourceClass;
            this.sourceMethod = sourceMethod;
            this.bootstrap = bootstrap;
        }

        private LogEvent(BootstrapLogger bootstrap, PlatformLogger.Level platformLevel, String sourceClass, String sourceMethod, Supplier<String> msgSupplier, Throwable thrown, Object[] params) {
            this.timeMillis = System.currentTimeMillis();
            this.nanoAdjustment = VM.getNanoTimeAdjustment(this.timeMillis);
            this.level = null;
            this.platformLevel = platformLevel;
            this.bundle = null;
            this.msg = null;
            this.msgSupplier = msgSupplier;
            this.thrown = thrown;
            this.params = params;
            this.sourceClass = sourceClass;
            this.sourceMethod = sourceMethod;
            this.bootstrap = bootstrap;
        }

        private void log(System.Logger logger) {
            assert (this.platformLevel == null && this.level != null);
            if (this.msgSupplier != null) {
                if (this.thrown != null) {
                    logger.log(this.level, this.msgSupplier, this.thrown);
                } else {
                    logger.log(this.level, this.msgSupplier);
                }
            } else if (this.thrown != null) {
                logger.log(this.level, this.bundle, this.msg, this.thrown);
            } else {
                logger.log(this.level, this.bundle, this.msg, this.params);
            }
        }

        private void log(PlatformLogger.Bridge logger) {
            assert (this.platformLevel != null && this.level == null);
            if (this.sourceClass == null) {
                if (this.msgSupplier != null) {
                    if (this.thrown != null) {
                        logger.log(this.platformLevel, this.thrown, this.msgSupplier);
                    } else {
                        logger.log(this.platformLevel, this.msgSupplier);
                    }
                } else if (this.thrown != null) {
                    logger.logrb(this.platformLevel, this.bundle, this.msg, this.thrown);
                } else {
                    logger.logrb(this.platformLevel, this.bundle, this.msg, this.params);
                }
            } else if (this.msgSupplier != null) {
                if (this.thrown != null) {
                    logger.logp(this.platformLevel, this.sourceClass, this.sourceMethod, this.thrown, this.msgSupplier);
                } else {
                    logger.logp(this.platformLevel, this.sourceClass, this.sourceMethod, this.msgSupplier);
                }
            } else if (this.thrown != null) {
                logger.logrb(this.platformLevel, this.sourceClass, this.sourceMethod, this.bundle, this.msg, this.thrown);
            } else {
                logger.logrb(this.platformLevel, this.sourceClass, this.sourceMethod, this.bundle, this.msg, this.params);
            }
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, System.Logger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), bundle, key, thrown, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, System.Logger.Level level, ResourceBundle bundle, String format, Object[] params) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), bundle, format, null, params);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, System.Logger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), Objects.requireNonNull(msgSupplier), thrown, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, System.Logger.Level level, Supplier<String> msgSupplier) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), Objects.requireNonNull(msgSupplier), null, null);
        }

        static void log(LogEvent log, System.Logger logger) {
            SecurityManager sm = System.getSecurityManager();
            if (sm == null || log.acc == null) {
                BootstrapExecutors.submit(() -> log.log(logger));
            } else {
                BootstrapExecutors.submit(() -> AccessController.doPrivileged(() -> {
                    log.log(logger);
                    return null;
                }, log.acc));
            }
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String msg) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), null, null, null, msg, null, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String msg, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), null, null, null, msg, thrown, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String msg, Object[] params) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), null, null, null, msg, null, params);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, Supplier<String> msgSupplier) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), null, null, msgSupplier, null, null);
        }

        static LogEvent vaueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), null, null, msgSupplier, thrown, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object[] params) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), sourceClass, sourceMethod, bundle, msg, null, params);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), sourceClass, sourceMethod, bundle, msg, thrown, null);
        }

        static LogEvent valueOf(BootstrapLogger bootstrap, PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier, Throwable thrown) {
            return new LogEvent(Objects.requireNonNull(bootstrap), Objects.requireNonNull(level), sourceClass, sourceMethod, msgSupplier, thrown, null);
        }

        static void log(LogEvent log, PlatformLogger.Bridge logger) {
            SecurityManager sm = System.getSecurityManager();
            if (sm == null || log.acc == null) {
                BootstrapExecutors.submit(() -> log.log(logger));
            } else {
                BootstrapExecutors.submit(() -> AccessController.doPrivileged(() -> {
                    log.log(logger);
                    return null;
                }, log.acc));
            }
        }

        static void log(LogEvent event) {
            event.bootstrap.flush(event);
        }
    }

    private static final class DetectBackend {
        static final LoggingBackend detectedBackend = AccessController.doPrivileged(new PrivilegedAction<LoggingBackend>(){

            @Override
            public LoggingBackend run() {
                Iterator<System.LoggerFinder> iterator = ServiceLoader.load(System.LoggerFinder.class, ClassLoader.getSystemClassLoader()).iterator();
                if (iterator.hasNext()) {
                    return LoggingBackend.CUSTOM;
                }
                Iterator<DefaultLoggerFinder> iterator2 = ServiceLoader.loadInstalled(DefaultLoggerFinder.class).iterator();
                if (iterator2.hasNext()) {
                    String cname = System.getProperty("java.util.logging.config.class");
                    String fname = System.getProperty("java.util.logging.config.file");
                    return cname != null || fname != null ? LoggingBackend.JUL_WITH_CONFIG : LoggingBackend.JUL_DEFAULT;
                }
                return LoggingBackend.NONE;
            }
        });

        private DetectBackend() {
        }
    }

    private static enum LoggingBackend {
        NONE(true),
        JUL_DEFAULT(false),
        JUL_WITH_CONFIG(true),
        CUSTOM(true);

        final boolean useLoggerFinder;

        private LoggingBackend(boolean useLoggerFinder) {
            this.useLoggerFinder = useLoggerFinder;
        }
    }

    static final class RedirectedLoggers
    implements Function<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> {
        final Map<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> redirectedLoggers = new HashMap<LazyLoggers.LazyLoggerAccessor, SurrogateLogger>();
        boolean cleared;
        static final RedirectedLoggers INSTANCE = new RedirectedLoggers();

        RedirectedLoggers() {
        }

        @Override
        public SurrogateLogger apply(LazyLoggers.LazyLoggerAccessor t) {
            if (this.cleared) {
                throw new IllegalStateException("LoggerFinder already initialized");
            }
            return SurrogateLogger.makeSurrogateLogger(t.getLoggerName());
        }

        SurrogateLogger get(LazyLoggers.LazyLoggerAccessor a) {
            if (this.cleared) {
                throw new IllegalStateException("LoggerFinder already initialized");
            }
            return this.redirectedLoggers.computeIfAbsent(a, this);
        }

        Map<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> drainLoggersMap() {
            if (this.redirectedLoggers.isEmpty()) {
                return null;
            }
            if (this.cleared) {
                throw new IllegalStateException("LoggerFinder already initialized");
            }
            HashMap<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> accessors = new HashMap<LazyLoggers.LazyLoggerAccessor, SurrogateLogger>(this.redirectedLoggers);
            this.redirectedLoggers.clear();
            this.cleared = true;
            return accessors;
        }

        static void replaceSurrogateLoggers(Map<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> accessors) {
            LoggingBackend detectedBackend = DetectBackend.detectedBackend;
            boolean lazy = detectedBackend != LoggingBackend.JUL_DEFAULT && detectedBackend != LoggingBackend.JUL_WITH_CONFIG;
            for (Map.Entry<LazyLoggers.LazyLoggerAccessor, SurrogateLogger> a : accessors.entrySet()) {
                a.getKey().release(a.getValue(), !lazy);
            }
        }
    }
}

