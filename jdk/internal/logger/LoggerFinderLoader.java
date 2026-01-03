/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.io.FilePermission;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.BooleanSupplier;
import jdk.internal.logger.BootstrapLogger;
import jdk.internal.logger.DefaultLoggerFinder;
import jdk.internal.logger.LazyLoggers;
import jdk.internal.logger.SimpleConsoleLogger;
import jdk.internal.vm.annotation.Stable;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;

public final class LoggerFinderLoader {
    private static volatile System.LoggerFinder service;
    private static final Object lock;
    static final Permission CLASSLOADER_PERMISSION;
    static final Permission READ_PERMISSION;
    public static final RuntimePermission LOGGERFINDER_PERMISSION;
    static volatile Thread loadingThread;

    private LoggerFinderLoader() {
        throw new InternalError("LoggerFinderLoader cannot be instantiated");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static System.LoggerFinder service() {
        if (service != null) {
            return service;
        }
        BootstrapLogger.ensureBackendDetected();
        Object object = lock;
        synchronized (object) {
            if (service != null) {
                return service;
            }
            Thread currentThread = Thread.currentThread();
            if (loadingThread == currentThread) {
                return TemporaryLoggerFinder.INSTANCE;
            }
            loadingThread = currentThread;
            try {
                service = LoggerFinderLoader.loadLoggerFinder();
            }
            finally {
                loadingThread = null;
            }
        }
        BootstrapLogger.redirectTemporaryLoggers();
        return service;
    }

    static boolean isLoadingThread() {
        return loadingThread != null && loadingThread == Thread.currentThread();
    }

    private static ErrorPolicy configurationErrorPolicy() {
        String errorPolicy = GetPropertyAction.privilegedGetProperty("jdk.logger.finder.error");
        if (errorPolicy == null || errorPolicy.isEmpty()) {
            return ErrorPolicy.WARNING;
        }
        try {
            return ErrorPolicy.valueOf(errorPolicy.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException x) {
            return ErrorPolicy.WARNING;
        }
    }

    private static boolean ensureSingletonProvider() {
        return GetBooleanAction.privilegedGetProperty("jdk.logger.finder.singleton");
    }

    private static Iterator<System.LoggerFinder> findLoggerFinderProviders() {
        Iterator iterator;
        if (System.getSecurityManager() == null) {
            iterator = ServiceLoader.load(System.LoggerFinder.class, ClassLoader.getSystemClassLoader()).iterator();
        } else {
            PrivilegedAction<Iterator> pa = () -> ServiceLoader.load(System.LoggerFinder.class, ClassLoader.getSystemClassLoader()).iterator();
            iterator = AccessController.doPrivileged(pa, null, LOGGERFINDER_PERMISSION, CLASSLOADER_PERMISSION, READ_PERMISSION);
        }
        return iterator;
    }

    private static System.LoggerFinder loadLoggerFinder() {
        System.LoggerFinder result;
        block7: {
            try {
                Iterator<System.LoggerFinder> iterator = LoggerFinderLoader.findLoggerFinderProviders();
                if (iterator.hasNext()) {
                    result = iterator.next();
                    if (iterator.hasNext() && LoggerFinderLoader.ensureSingletonProvider()) {
                        throw new ServiceConfigurationError("More than one LoggerFinder implementation");
                    }
                } else {
                    result = LoggerFinderLoader.loadDefaultImplementation();
                }
            }
            catch (Error | RuntimeException x) {
                service = result = new DefaultLoggerFinder();
                ErrorPolicy errorPolicy = LoggerFinderLoader.configurationErrorPolicy();
                if (errorPolicy == ErrorPolicy.ERROR) {
                    if (x instanceof Error) {
                        throw x;
                    }
                    throw new ServiceConfigurationError("Failed to instantiate LoggerFinder provider; Using default.", x);
                }
                if (errorPolicy == ErrorPolicy.QUIET) break block7;
                SimpleConsoleLogger logger = new SimpleConsoleLogger("jdk.internal.logger", false);
                logger.log(System.Logger.Level.WARNING, "Failed to instantiate LoggerFinder provider; Using default.");
                if (errorPolicy != ErrorPolicy.DEBUG) break block7;
                logger.log(System.Logger.Level.WARNING, "Exception raised trying to instantiate LoggerFinder", x);
            }
        }
        return result;
    }

    private static System.LoggerFinder loadDefaultImplementation() {
        Iterator iterator;
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            iterator = ServiceLoader.loadInstalled(DefaultLoggerFinder.class).iterator();
        } else {
            PrivilegedAction<Iterator> pa = () -> ServiceLoader.loadInstalled(DefaultLoggerFinder.class).iterator();
            iterator = AccessController.doPrivileged(pa, null, LOGGERFINDER_PERMISSION, CLASSLOADER_PERMISSION, READ_PERMISSION);
        }
        DefaultLoggerFinder result = null;
        try {
            if (iterator.hasNext()) {
                result = iterator.next();
            }
        }
        catch (RuntimeException x) {
            throw new ServiceConfigurationError("Failed to instantiate default LoggerFinder", x);
        }
        if (result == null) {
            result = new DefaultLoggerFinder();
        }
        return result;
    }

    public static System.LoggerFinder getLoggerFinder() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(LOGGERFINDER_PERMISSION);
        }
        return LoggerFinderLoader.service();
    }

    static {
        lock = new int[0];
        CLASSLOADER_PERMISSION = SecurityConstants.GET_CLASSLOADER_PERMISSION;
        READ_PERMISSION = new FilePermission("<<ALL FILES>>", "read");
        LOGGERFINDER_PERMISSION = new RuntimePermission("loggerFinder");
    }

    public static final class TemporaryLoggerFinder
    extends System.LoggerFinder {
        @Stable
        private System.LoggerFinder loadedService;
        private static final BooleanSupplier isLoadingThread = new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return LoggerFinderLoader.isLoadingThread();
            }
        };
        private static final TemporaryLoggerFinder INSTANCE = new TemporaryLoggerFinder();

        private TemporaryLoggerFinder() {
        }

        @Override
        public System.Logger getLogger(String name, Module module) {
            if (this.loadedService == null) {
                this.loadedService = service;
                if (this.loadedService == null) {
                    return LazyLoggers.makeLazyLogger(name, module, isLoadingThread);
                }
            }
            assert (this.loadedService != null);
            assert (!LoggerFinderLoader.isLoadingThread());
            assert (this.loadedService != this);
            return LazyLoggers.getLogger(name, module);
        }
    }

    private static enum ErrorPolicy {
        ERROR,
        WARNING,
        DEBUG,
        QUIET;

    }
}

