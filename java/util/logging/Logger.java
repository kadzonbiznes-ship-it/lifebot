/*
 * Decompiled with CFR 0.152.
 */
package java.util.logging;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import jdk.internal.access.JavaUtilResourceBundleAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.logger.DefaultLoggerFinder;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.CallerSensitiveAdapter;
import jdk.internal.reflect.Reflection;

public class Logger {
    private static final Handler[] emptyHandlers = new Handler[0];
    private static final int offValue = Level.OFF.intValue();
    static final String SYSTEM_LOGGER_RB_NAME = "sun.util.logging.resources.logging";
    private static final LoggerBundle SYSTEM_BUNDLE = new LoggerBundle("sun.util.logging.resources.logging", null);
    private static final LoggerBundle NO_RESOURCE_BUNDLE = new LoggerBundle(null, null);
    private volatile ConfigurationData config;
    private volatile LogManager manager;
    private String name;
    private volatile LoggerBundle loggerBundle = NO_RESOURCE_BUNDLE;
    private boolean anonymous;
    private WeakReference<ResourceBundle> catalogRef;
    private String catalogName;
    private Locale catalogLocale;
    private static final Object treeLock = new Object();
    private volatile Logger parent;
    private ArrayList<LogManager.LoggerWeakRef> kids;
    private WeakReference<Module> callerModuleRef;
    private final boolean isSystemLogger;
    public static final String GLOBAL_LOGGER_NAME = "global";
    @Deprecated
    public static final Logger global = new Logger("global");

    public static final Logger getGlobal() {
        LogManager.getLogManager();
        return global;
    }

    protected Logger(String name, String resourceBundleName) {
        this(name, resourceBundleName, null, LogManager.getLogManager(), false);
    }

    Logger(String name, String resourceBundleName, Module caller, LogManager manager, boolean isSystemLogger) {
        this.manager = manager;
        this.isSystemLogger = isSystemLogger;
        this.config = new ConfigurationData();
        this.name = name;
        this.setupResourceInfo(resourceBundleName, caller);
    }

    final void mergeWithSystemLogger(Logger system) {
        if (!system.isSystemLogger || this.anonymous || this.name == null || !this.name.equals(system.name)) {
            throw new InternalError("invalid logger merge");
        }
        this.checkPermission();
        ConfigurationData cfg = this.config;
        if (cfg != system.config) {
            this.config = cfg.merge(system);
        }
    }

    private void setCallerModuleRef(Module callerModule) {
        if (callerModule != null) {
            this.callerModuleRef = new WeakReference<Module>(callerModule);
        }
    }

    private Module getCallerModule() {
        return this.callerModuleRef != null ? (Module)this.callerModuleRef.get() : null;
    }

    private Logger(String name) {
        this.name = name;
        this.isSystemLogger = true;
        this.config = new ConfigurationData();
    }

    void setLogManager(LogManager manager) {
        this.manager = manager;
    }

    private void checkPermission() throws SecurityException {
        if (!this.anonymous) {
            if (this.manager == null) {
                this.manager = LogManager.getLogManager();
            }
            this.manager.checkPermission();
        }
    }

    private static Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        LogManager manager = LogManager.getLogManager();
        if (!SystemLoggerHelper.disableCallerCheck && DefaultLoggerFinder.isSystem(caller.getModule())) {
            return manager.demandSystemLogger(name, resourceBundleName, caller);
        }
        return manager.demandLogger(name, resourceBundleName, caller);
    }

    @CallerSensitive
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, Reflection.getCallerClass());
    }

    @CallerSensitiveAdapter
    private static Logger getLogger(String name, Class<?> callerClass) {
        return Logger.demandLogger(name, null, callerClass);
    }

    @CallerSensitive
    public static Logger getLogger(String name, String resourceBundleName) {
        return Logger.getLogger(name, resourceBundleName, Reflection.getCallerClass());
    }

    @CallerSensitiveAdapter
    private static Logger getLogger(String name, String resourceBundleName, Class<?> callerClass) {
        Logger result = Logger.demandLogger(name, resourceBundleName, callerClass);
        result.setupResourceInfo(resourceBundleName, callerClass);
        return result;
    }

    static Logger getPlatformLogger(String name) {
        LogManager manager = LogManager.getLogManager();
        Logger result = manager.demandSystemLogger(name, SYSTEM_LOGGER_RB_NAME, (Module)null);
        return result;
    }

    public static Logger getAnonymousLogger() {
        return Logger.getAnonymousLogger(null);
    }

    @CallerSensitive
    public static Logger getAnonymousLogger(String resourceBundleName) {
        LogManager manager = LogManager.getLogManager();
        manager.drainLoggerRefQueueBounded();
        Class<?> callerClass = Reflection.getCallerClass();
        Module module = callerClass.getModule();
        Logger result = new Logger(null, resourceBundleName, module, manager, false);
        result.anonymous = true;
        Logger root = manager.getLogger("");
        result.doSetParent(root);
        return result;
    }

    public ResourceBundle getResourceBundle() {
        return this.findResourceBundle(this.getResourceBundleName(), true);
    }

    public String getResourceBundleName() {
        return this.loggerBundle.resourceBundleName;
    }

    public void setFilter(Filter newFilter) throws SecurityException {
        this.checkPermission();
        this.config.setFilter(newFilter);
    }

    public Filter getFilter() {
        return this.config.filter;
    }

    public void log(LogRecord record) {
        if (!this.isLoggable(record.getLevel())) {
            return;
        }
        Filter theFilter = this.config.filter;
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return;
        }
        Logger logger = this;
        while (logger != null) {
            boolean useParentHdls;
            Handler[] loggerHandlers;
            for (Handler handler : loggerHandlers = this.isSystemLogger ? logger.accessCheckedHandlers() : logger.getHandlers()) {
                handler.publish(record);
            }
            boolean bl = useParentHdls = this.isSystemLogger ? logger.config.useParentHandlers : logger.getUseParentHandlers();
            if (!useParentHdls) break;
            logger = this.isSystemLogger ? logger.parent : logger.getParent();
        }
    }

    private void doLog(LogRecord lr) {
        lr.setLoggerName(this.name);
        LoggerBundle lb = this.getEffectiveLoggerBundle();
        ResourceBundle bundle = lb.userBundle;
        String ebname = lb.resourceBundleName;
        if (ebname != null && bundle != null) {
            lr.setResourceBundleName(ebname);
            lr.setResourceBundle(bundle);
        }
        this.log(lr);
    }

    public void log(Level level, String msg) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        this.doLog(lr);
    }

    public void log(Level level, Supplier<String> msgSupplier) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        this.doLog(lr);
    }

    public void log(Level level, String msg, Object param1) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        Object[] params = new Object[]{param1};
        lr.setParameters(params);
        this.doLog(lr);
    }

    public void log(Level level, String msg, Object[] params) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setParameters(params);
        this.doLog(lr);
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        this.doLog(lr);
    }

    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setThrown(thrown);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object[] params = new Object[]{param1};
        lr.setParameters(params);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        this.doLog(lr);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        this.doLog(lr);
    }

    private void doLog(LogRecord lr, String rbname) {
        lr.setLoggerName(this.name);
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(this.findResourceBundle(rbname, false));
        }
        this.log(lr);
    }

    private void doLog(LogRecord lr, ResourceBundle rb) {
        lr.setLoggerName(this.name);
        if (rb != null) {
            lr.setResourceBundleName(rb.getBaseBundleName());
            lr.setResourceBundle(rb);
        }
        this.log(lr);
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        this.doLog(lr, bundleName);
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object[] params = new Object[]{param1};
        lr.setParameters(params);
        this.doLog(lr, bundleName);
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        this.doLog(lr, bundleName);
    }

    public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object ... params) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        if (params != null && params.length != 0) {
            lr.setParameters(params);
        }
        this.doLog(lr, bundle);
    }

    public void logrb(Level level, ResourceBundle bundle, String msg, Object ... params) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        if (params != null && params.length != 0) {
            lr.setParameters(params);
        }
        this.doLog(lr, bundle);
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        this.doLog(lr, bundleName);
    }

    public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        this.doLog(lr, bundle);
    }

    public void logrb(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        if (!this.isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        this.doLog(lr, bundle);
    }

    public void entering(String sourceClass, String sourceMethod) {
        this.logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
    }

    public void entering(String sourceClass, String sourceMethod, Object param1) {
        this.logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param1);
    }

    public void entering(String sourceClass, String sourceMethod, Object[] params) {
        String msg = "ENTRY";
        if (params == null) {
            this.logp(Level.FINER, sourceClass, sourceMethod, msg);
            return;
        }
        if (!this.isLoggable(Level.FINER)) {
            return;
        }
        if (params.length > 0) {
            StringBuilder b = new StringBuilder(msg);
            for (int i = 0; i < params.length; ++i) {
                b.append(' ').append('{').append(i).append('}');
            }
            msg = b.toString();
        }
        this.logp(Level.FINER, sourceClass, sourceMethod, msg, params);
    }

    public void exiting(String sourceClass, String sourceMethod) {
        this.logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
    }

    public void exiting(String sourceClass, String sourceMethod, Object result) {
        this.logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
    }

    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (!this.isLoggable(Level.FINER)) {
            return;
        }
        LogRecord lr = new LogRecord(Level.FINER, "THROW");
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        this.doLog(lr);
    }

    public void severe(String msg) {
        this.log(Level.SEVERE, msg);
    }

    public void warning(String msg) {
        this.log(Level.WARNING, msg);
    }

    public void info(String msg) {
        this.log(Level.INFO, msg);
    }

    public void config(String msg) {
        this.log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        this.log(Level.FINE, msg);
    }

    public void finer(String msg) {
        this.log(Level.FINER, msg);
    }

    public void finest(String msg) {
        this.log(Level.FINEST, msg);
    }

    public void severe(Supplier<String> msgSupplier) {
        this.log(Level.SEVERE, msgSupplier);
    }

    public void warning(Supplier<String> msgSupplier) {
        this.log(Level.WARNING, msgSupplier);
    }

    public void info(Supplier<String> msgSupplier) {
        this.log(Level.INFO, msgSupplier);
    }

    public void config(Supplier<String> msgSupplier) {
        this.log(Level.CONFIG, msgSupplier);
    }

    public void fine(Supplier<String> msgSupplier) {
        this.log(Level.FINE, msgSupplier);
    }

    public void finer(Supplier<String> msgSupplier) {
        this.log(Level.FINER, msgSupplier);
    }

    public void finest(Supplier<String> msgSupplier) {
        this.log(Level.FINEST, msgSupplier);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLevel(Level newLevel) throws SecurityException {
        this.checkPermission();
        Object object = treeLock;
        synchronized (object) {
            this.config.setLevelObject(newLevel);
            this.updateEffectiveLevel();
        }
    }

    final boolean isLevelInitialized() {
        return this.config.levelObject != null;
    }

    public Level getLevel() {
        return this.config.levelObject;
    }

    public boolean isLoggable(Level level) {
        int levelValue = this.config.levelValue;
        return level.intValue() >= levelValue && levelValue != offValue;
    }

    public String getName() {
        return this.name;
    }

    public void addHandler(Handler handler) throws SecurityException {
        Objects.requireNonNull(handler);
        this.checkPermission();
        this.config.addHandler(handler);
    }

    public void removeHandler(Handler handler) throws SecurityException {
        this.checkPermission();
        if (handler == null) {
            return;
        }
        this.config.removeHandler(handler);
    }

    public Handler[] getHandlers() {
        return this.accessCheckedHandlers();
    }

    Handler[] accessCheckedHandlers() {
        return this.config.handlers.toArray(emptyHandlers);
    }

    public void setUseParentHandlers(boolean useParentHandlers) {
        this.checkPermission();
        this.config.setUseParentHandlers(useParentHandlers);
    }

    public boolean getUseParentHandlers() {
        return this.config.useParentHandlers;
    }

    private ResourceBundle catalog() {
        WeakReference<ResourceBundle> ref = this.catalogRef;
        return ref == null ? null : (ResourceBundle)ref.get();
    }

    private synchronized ResourceBundle findResourceBundle(String name, boolean useCallersModule) {
        if (name == null) {
            return null;
        }
        Locale currentLocale = Locale.getDefault();
        LoggerBundle lb = this.loggerBundle;
        ResourceBundle catalog = this.catalog();
        if (lb.userBundle != null && name.equals(lb.resourceBundleName)) {
            return lb.userBundle;
        }
        if (catalog != null && currentLocale.equals(this.catalogLocale) && name.equals(this.catalogName)) {
            return catalog;
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        Module callerModule = this.getCallerModule();
        if (!useCallersModule || callerModule == null || !callerModule.isNamed()) {
            try {
                Module mod = cl.getUnnamedModule();
                catalog = RbAccess.RB_ACCESS.getBundle(name, currentLocale, mod);
                this.catalogRef = new WeakReference<ResourceBundle>(catalog);
                this.catalogName = name;
                this.catalogLocale = currentLocale;
                return catalog;
            }
            catch (MissingResourceException ex) {
                if (useCallersModule && callerModule != null) {
                    try {
                        PrivilegedAction<ClassLoader> getModuleClassLoader = () -> callerModule.getClassLoader();
                        ClassLoader moduleCL = AccessController.doPrivileged(getModuleClassLoader);
                        if (moduleCL == cl || moduleCL == null) {
                            return null;
                        }
                        catalog = ResourceBundle.getBundle(name, currentLocale, moduleCL);
                        this.catalogRef = new WeakReference<ResourceBundle>(catalog);
                        this.catalogName = name;
                        this.catalogLocale = currentLocale;
                        return catalog;
                    }
                    catch (MissingResourceException x) {
                        return null;
                    }
                }
                return null;
            }
        }
        try {
            catalog = RbAccess.RB_ACCESS.getBundle(name, currentLocale, callerModule);
            this.catalogRef = new WeakReference<ResourceBundle>(catalog);
            this.catalogName = name;
            this.catalogLocale = currentLocale;
            return catalog;
        }
        catch (MissingResourceException ex) {
            return null;
        }
    }

    private void setupResourceInfo(String name, Class<?> caller) {
        Module module = caller == null ? null : caller.getModule();
        this.setupResourceInfo(name, module);
    }

    private synchronized void setupResourceInfo(String name, Module callerModule) {
        LoggerBundle lb = this.loggerBundle;
        if (lb.resourceBundleName != null) {
            if (lb.resourceBundleName.equals(name)) {
                return;
            }
            throw new IllegalArgumentException(lb.resourceBundleName + " != " + name);
        }
        if (name == null) {
            return;
        }
        this.setCallerModuleRef(callerModule);
        if (this.isSystemLogger && callerModule != null && !DefaultLoggerFinder.isSystem(callerModule)) {
            this.checkPermission();
        }
        if (name.equals(SYSTEM_LOGGER_RB_NAME)) {
            this.loggerBundle = SYSTEM_BUNDLE;
        } else {
            ResourceBundle bundle = this.findResourceBundle(name, true);
            if (bundle == null) {
                this.callerModuleRef = null;
                throw new MissingResourceException("Can't find " + name + " bundle from ", name, "");
            }
            this.loggerBundle = LoggerBundle.get(name, null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setResourceBundle(ResourceBundle bundle) {
        this.checkPermission();
        String baseName = bundle.getBaseBundleName();
        if (baseName == null || baseName.isEmpty()) {
            throw new IllegalArgumentException("resource bundle must have a name");
        }
        Logger logger = this;
        synchronized (logger) {
            boolean canReplaceResourceBundle;
            LoggerBundle lb = this.loggerBundle;
            boolean bl = canReplaceResourceBundle = lb.resourceBundleName == null || lb.resourceBundleName.equals(baseName);
            if (!canReplaceResourceBundle) {
                throw new IllegalArgumentException("can't replace resource bundle");
            }
            this.loggerBundle = LoggerBundle.get(baseName, bundle);
        }
    }

    public Logger getParent() {
        return this.parent;
    }

    public void setParent(Logger parent) {
        if (parent == null) {
            throw new NullPointerException();
        }
        if (this.manager == null) {
            this.manager = LogManager.getLogManager();
        }
        this.manager.checkPermission();
        this.doSetParent(parent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doSetParent(Logger newParent) {
        Object object = treeLock;
        synchronized (object) {
            LogManager.LoggerWeakRef ref = null;
            if (this.parent != null) {
                Iterator<LogManager.LoggerWeakRef> iter = this.parent.kids.iterator();
                while (iter.hasNext()) {
                    ref = iter.next();
                    if (ref.refersTo(this)) {
                        iter.remove();
                        break;
                    }
                    ref = null;
                }
            }
            this.parent = newParent;
            if (this.parent.kids == null) {
                this.parent.kids = new ArrayList(2);
            }
            if (ref == null) {
                LogManager logManager = this.manager;
                Objects.requireNonNull(logManager);
                ref = new LogManager.LoggerWeakRef(logManager, this);
            }
            ref.setParentRef(new WeakReference<Logger>(this.parent));
            this.parent.kids.add(ref);
            this.updateEffectiveLevel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void removeChildLogger(LogManager.LoggerWeakRef child) {
        Object object = treeLock;
        synchronized (object) {
            Iterator<LogManager.LoggerWeakRef> iter = this.kids.iterator();
            while (iter.hasNext()) {
                LogManager.LoggerWeakRef ref = iter.next();
                if (ref != child) continue;
                iter.remove();
                return;
            }
        }
    }

    private void updateEffectiveLevel() {
        ConfigurationData cfg = this.config;
        Level levelObject = cfg.levelObject;
        int newLevelValue = levelObject != null ? levelObject.intValue() : (this.parent != null ? this.parent.config.levelValue : Level.INFO.intValue());
        if (cfg.levelValue == newLevelValue) {
            return;
        }
        cfg.setLevelValue(newLevelValue);
        if (this.kids != null) {
            for (LogManager.LoggerWeakRef ref : this.kids) {
                Logger kid = (Logger)ref.get();
                if (kid == null) continue;
                kid.updateEffectiveLevel();
            }
        }
    }

    private LoggerBundle getEffectiveLoggerBundle() {
        LoggerBundle lb = this.loggerBundle;
        if (lb.isSystemBundle()) {
            return SYSTEM_BUNDLE;
        }
        ResourceBundle b = this.getResourceBundle();
        if (b != null && b == lb.userBundle) {
            return lb;
        }
        if (b != null) {
            String rbName = this.getResourceBundleName();
            return LoggerBundle.get(rbName, b);
        }
        Logger target = this.parent;
        while (target != null) {
            String rbName;
            LoggerBundle trb = target.loggerBundle;
            if (trb.isSystemBundle()) {
                return SYSTEM_BUNDLE;
            }
            if (trb.userBundle != null) {
                return trb;
            }
            String string = this.isSystemLogger ? (target.isSystemLogger ? trb.resourceBundleName : null) : (rbName = target.getResourceBundleName());
            if (rbName != null) {
                return LoggerBundle.get(rbName, this.findResourceBundle(rbName, true));
            }
            target = this.isSystemLogger ? target.parent : target.getParent();
        }
        return NO_RESOURCE_BUNDLE;
    }

    private static final class LoggerBundle {
        final String resourceBundleName;
        final ResourceBundle userBundle;

        private LoggerBundle(String resourceBundleName, ResourceBundle bundle) {
            this.resourceBundleName = resourceBundleName;
            this.userBundle = bundle;
        }

        boolean isSystemBundle() {
            return Logger.SYSTEM_LOGGER_RB_NAME.equals(this.resourceBundleName);
        }

        static LoggerBundle get(String name, ResourceBundle bundle) {
            if (name == null && bundle == null) {
                return NO_RESOURCE_BUNDLE;
            }
            if (Logger.SYSTEM_LOGGER_RB_NAME.equals(name) && bundle == null) {
                return SYSTEM_BUNDLE;
            }
            return new LoggerBundle(name, bundle);
        }
    }

    private static final class ConfigurationData {
        private volatile ConfigurationData delegate;
        volatile boolean useParentHandlers = true;
        volatile Filter filter;
        volatile Level levelObject;
        volatile int levelValue;
        final CopyOnWriteArrayList<Handler> handlers = new CopyOnWriteArrayList();

        ConfigurationData() {
            this.delegate = this;
            this.levelValue = Level.INFO.intValue();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setUseParentHandlers(boolean flag) {
            this.useParentHandlers = flag;
            if (this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.useParentHandlers = this.useParentHandlers;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setFilter(Filter f) {
            this.filter = f;
            if (this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.filter = this.filter;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setLevelObject(Level l) {
            this.levelObject = l;
            if (this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.levelObject = this.levelObject;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setLevelValue(int v) {
            this.levelValue = v;
            if (this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.levelValue = this.levelValue;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void addHandler(Handler h) {
            if (this.handlers.add(h) && this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.handlers.addIfAbsent(h);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void removeHandler(Handler h) {
            if (this.handlers.remove(h) && this.delegate != this) {
                ConfigurationData system;
                ConfigurationData configurationData = system = this.delegate;
                synchronized (configurationData) {
                    system.handlers.remove(h);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ConfigurationData merge(Logger systemPeer) {
            if (!systemPeer.isSystemLogger) {
                throw new InternalError("not a system logger");
            }
            ConfigurationData system = systemPeer.config;
            if (system == this) {
                return system;
            }
            Object object = system;
            synchronized (object) {
                if (this.delegate == system) {
                    return system;
                }
                this.delegate = system;
                system.useParentHandlers = this.useParentHandlers;
                system.filter = this.filter;
                system.levelObject = this.levelObject;
                system.levelValue = this.levelValue;
                for (Handler h : this.handlers) {
                    if (system.handlers.contains(h)) continue;
                    systemPeer.addHandler(h);
                }
                system.handlers.retainAll(this.handlers);
                system.handlers.addAllAbsent(this.handlers);
            }
            object = treeLock;
            synchronized (object) {
                systemPeer.updateEffectiveLevel();
            }
            return system;
        }
    }

    private static class SystemLoggerHelper {
        static boolean disableCallerCheck = SystemLoggerHelper.getBooleanProperty("sun.util.logging.disableCallerCheck");

        private SystemLoggerHelper() {
        }

        private static boolean getBooleanProperty(final String key) {
            String s = AccessController.doPrivileged(new PrivilegedAction<String>(){

                @Override
                public String run() {
                    return System.getProperty(key);
                }
            });
            return Boolean.parseBoolean(s);
        }
    }

    private static final class RbAccess {
        static final JavaUtilResourceBundleAccess RB_ACCESS = SharedSecrets.getJavaUtilResourceBundleAccess();

        private RbAccess() {
        }
    }
}

