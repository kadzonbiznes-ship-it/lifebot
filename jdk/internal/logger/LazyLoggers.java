/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import jdk.internal.logger.AbstractLoggerWrapper;
import jdk.internal.logger.BootstrapLogger;
import jdk.internal.logger.DefaultLoggerFinder;
import jdk.internal.logger.LoggerFinderLoader;
import jdk.internal.logger.SimpleConsoleLogger;
import jdk.internal.misc.VM;
import sun.util.logging.PlatformLogger;

public final class LazyLoggers {
    static final RuntimePermission LOGGERFINDER_PERMISSION = new RuntimePermission("loggerFinder");
    private static volatile System.LoggerFinder provider;
    private static final BiFunction<String, Module, System.Logger> loggerSupplier;
    private static final LazyLoggerFactories<System.Logger> factories;

    private LazyLoggers() {
        throw new InternalError();
    }

    private static System.LoggerFinder accessLoggerFinder() {
        System.LoggerFinder prov = provider;
        if (prov == null) {
            SecurityManager sm = System.getSecurityManager();
            System.LoggerFinder loggerFinder = prov = sm == null ? System.LoggerFinder.getLoggerFinder() : AccessController.doPrivileged(System.LoggerFinder::getLoggerFinder);
            if (prov instanceof LoggerFinderLoader.TemporaryLoggerFinder) {
                return prov;
            }
            provider = prov;
        }
        return prov;
    }

    static System.Logger makeLazyLogger(String name, Module module, BooleanSupplier isLoading) {
        LazyLoggerAccessor holder = new LazyLoggerAccessor(name, factories, module, isLoading);
        return new JdkLazyLogger(holder, null);
    }

    static System.Logger getLoggerFromFinder(String name, Module module) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return LazyLoggers.accessLoggerFinder().getLogger(name, module);
        }
        return AccessController.doPrivileged(() -> LazyLoggers.accessLoggerFinder().getLogger(name, module), null, LOGGERFINDER_PERMISSION);
    }

    public static final System.Logger getLogger(String name, Module module) {
        if (DefaultLoggerFinder.isSystem(module)) {
            return LazyLoggers.getLazyLogger(name, module);
        }
        return LazyLoggers.getLoggerFromFinder(name, module);
    }

    public static final System.Logger getLazyLogger(String name, Module module) {
        boolean useLazyLogger = BootstrapLogger.useLazyLoggers();
        if (useLazyLogger) {
            return new JdkLazyLogger(name, module);
        }
        return LazyLoggers.getLoggerFromFinder(name, module);
    }

    static {
        loggerSupplier = new BiFunction<String, Module, System.Logger>(){

            @Override
            public System.Logger apply(String name, Module module) {
                return LazyLoggers.getLoggerFromFinder(name, module);
            }
        };
        factories = new LazyLoggerFactories<System.Logger>(loggerSupplier);
    }

    static final class LazyLoggerAccessor
    implements LoggerAccessor {
        final LazyLoggerFactories<? extends System.Logger> factories;
        private final WeakReference<Module> moduleRef;
        private final BooleanSupplier isLoadingThread;
        final String name;
        private volatile System.Logger w;
        private volatile PlatformLogger.Bridge p;

        private LazyLoggerAccessor(String name, LazyLoggerFactories<? extends System.Logger> factories, Module module) {
            this(name, factories, module, null);
        }

        private LazyLoggerAccessor(String name, LazyLoggerFactories<? extends System.Logger> factories, Module module, BooleanSupplier isLoading) {
            this(Objects.requireNonNull(name), Objects.requireNonNull(factories), Objects.requireNonNull(module), isLoading, null);
        }

        private LazyLoggerAccessor(String name, LazyLoggerFactories<? extends System.Logger> factories, Module module, BooleanSupplier isLoading, Void unused) {
            this.name = name;
            this.factories = factories;
            this.moduleRef = new WeakReference<Module>(module);
            this.isLoadingThread = isLoading;
        }

        @Override
        public String getLoggerName() {
            return this.name;
        }

        private void setWrappedIfNotSet(System.Logger wrapped) {
            if (this.w == null) {
                this.w = wrapped;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public System.Logger wrapped() {
            System.Logger wrapped = this.w;
            if (wrapped != null) {
                return wrapped;
            }
            wrapped = BootstrapLogger.getLogger(this, this.isLoadingThread);
            LazyLoggerAccessor lazyLoggerAccessor = this;
            synchronized (lazyLoggerAccessor) {
                this.setWrappedIfNotSet(wrapped);
                return this.w;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public PlatformLogger.Bridge platform() {
            PlatformLogger.Bridge platform = this.p;
            if (platform != null) {
                return platform;
            }
            LazyLoggerAccessor lazyLoggerAccessor = this;
            synchronized (lazyLoggerAccessor) {
                if (this.w != null) {
                    if (this.p == null) {
                        this.p = PlatformLogger.Bridge.convert(this.w);
                    }
                    return this.p;
                }
            }
            System.Logger wrapped = BootstrapLogger.getLogger(this, this.isLoadingThread);
            LazyLoggerAccessor lazyLoggerAccessor2 = this;
            synchronized (lazyLoggerAccessor2) {
                this.setWrappedIfNotSet(wrapped);
                if (this.p == null) {
                    this.p = PlatformLogger.Bridge.convert(this.w);
                }
                return this.p;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void release(SimpleConsoleLogger temporary, boolean replace) {
            PlatformLogger.Bridge platform;
            PlatformLogger.ConfigurableBridge.LoggerConfiguration conf = PlatformLogger.ConfigurableBridge.getLoggerConfiguration(temporary);
            PlatformLogger.Level level = conf != null ? conf.getPlatformLevel() : null;
            LazyLoggerAccessor lazyLoggerAccessor = this;
            synchronized (lazyLoggerAccessor) {
                if (this.w == temporary) {
                    this.w = null;
                    this.p = null;
                }
            }
            PlatformLogger.Bridge bridge = platform = replace || level != null ? this.platform() : null;
            if (level != null) {
                PlatformLogger.ConfigurableBridge.LoggerConfiguration loggerConfiguration = conf = platform != null && platform != temporary ? PlatformLogger.ConfigurableBridge.getLoggerConfiguration(platform) : null;
                if (conf != null) {
                    conf.setPlatformLevel(level);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        System.Logger getConcreteLogger(BootstrapLogger bootstrap) {
            assert (VM.isBooted());
            LazyLoggerAccessor lazyLoggerAccessor = this;
            synchronized (lazyLoggerAccessor) {
                if (this.w == bootstrap) {
                    this.w = null;
                    this.p = null;
                }
            }
            return this.wrapped();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        PlatformLogger.Bridge getConcretePlatformLogger(BootstrapLogger bootstrap) {
            assert (VM.isBooted());
            LazyLoggerAccessor lazyLoggerAccessor = this;
            synchronized (lazyLoggerAccessor) {
                if (this.w == bootstrap) {
                    this.w = null;
                    this.p = null;
                }
            }
            return this.platform();
        }

        System.Logger createLogger() {
            Module module = (Module)this.moduleRef.get();
            if (module == null) {
                throw new IllegalStateException("The module for which this logger was created has been garbage collected");
            }
            return (System.Logger)this.factories.loggerSupplier.apply(this.name, module);
        }

        public static LazyLoggerAccessor makeAccessor(String name, LazyLoggerFactories<? extends System.Logger> factories, Module module) {
            return new LazyLoggerAccessor(name, factories, module);
        }
    }

    private static final class LazyLoggerFactories<L extends System.Logger> {
        final BiFunction<String, Module, L> loggerSupplier;

        public LazyLoggerFactories(BiFunction<String, Module, L> loggerSupplier) {
            this(Objects.requireNonNull(loggerSupplier), null);
        }

        private LazyLoggerFactories(BiFunction<String, Module, L> loggerSupplier, Void unused) {
            this.loggerSupplier = loggerSupplier;
        }
    }

    private static final class JdkLazyLogger
    extends LazyLoggerWrapper {
        JdkLazyLogger(String name, Module module) {
            this(LazyLoggerAccessor.makeAccessor(name, factories, module), (Void)null);
        }

        private JdkLazyLogger(LazyLoggerAccessor holder, Void unused) {
            super(holder);
        }
    }

    private static class LazyLoggerWrapper
    extends AbstractLoggerWrapper<System.Logger> {
        final LoggerAccessor loggerAccessor;

        public LazyLoggerWrapper(LazyLoggerAccessor loggerSinkSupplier) {
            this(Objects.requireNonNull(loggerSinkSupplier), null);
        }

        private LazyLoggerWrapper(LazyLoggerAccessor loggerSinkSupplier, Void unused) {
            this.loggerAccessor = loggerSinkSupplier;
        }

        @Override
        final System.Logger wrapped() {
            return this.loggerAccessor.wrapped();
        }

        @Override
        PlatformLogger.Bridge platformProxy() {
            return this.loggerAccessor.platform();
        }
    }

    static interface LoggerAccessor {
        public String getLoggerName();

        public System.Logger wrapped();

        public PlatformLogger.Bridge platform();
    }
}

