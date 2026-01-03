/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import jdk.internal.logger.SimpleConsoleLogger;
import jdk.internal.misc.VM;

public class DefaultLoggerFinder
extends System.LoggerFinder {
    static final RuntimePermission LOGGERFINDER_PERMISSION = new RuntimePermission("loggerFinder");

    protected DefaultLoggerFinder() {
        this(DefaultLoggerFinder.checkPermission());
    }

    private DefaultLoggerFinder(Void unused) {
    }

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(LOGGERFINDER_PERMISSION);
        }
        return null;
    }

    public static boolean isSystem(final Module m) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return VM.isSystemDomainLoader(m.getClassLoader());
            }
        });
    }

    @Override
    public final System.Logger getLogger(String name, Module module) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(module, "module");
        DefaultLoggerFinder.checkPermission();
        return this.demandLoggerFor(name, module);
    }

    @Override
    public final System.Logger getLocalizedLogger(String name, ResourceBundle bundle, Module module) {
        return super.getLocalizedLogger(name, bundle, module);
    }

    protected System.Logger demandLoggerFor(String name, Module module) {
        DefaultLoggerFinder.checkPermission();
        if (DefaultLoggerFinder.isSystem(module)) {
            return SharedLoggers.system.get(SimpleConsoleLogger::makeSimpleLogger, name);
        }
        return SharedLoggers.application.get(SimpleConsoleLogger::makeSimpleLogger, name);
    }

    static final class SharedLoggers {
        private final Map<String, Reference<System.Logger>> loggers = new HashMap<String, Reference<System.Logger>>();
        private final ReferenceQueue<System.Logger> queue = new ReferenceQueue();
        static final SharedLoggers system = new SharedLoggers();
        static final SharedLoggers application = new SharedLoggers();

        SharedLoggers() {
        }

        synchronized System.Logger get(Function<String, System.Logger> loggerSupplier, String name) {
            System.Logger w;
            Reference<System.Logger> ref = this.loggers.get(name);
            System.Logger logger = w = ref == null ? null : ref.get();
            if (w == null) {
                w = loggerSupplier.apply(name);
                this.loggers.put(name, new WeakReference<System.Logger>(w, this.queue));
            }
            Collection<Reference<System.Logger>> values = null;
            while ((ref = this.queue.poll()) != null) {
                if (values == null) {
                    values = this.loggers.values();
                }
                values.remove(ref);
            }
            return w;
        }
    }
}

