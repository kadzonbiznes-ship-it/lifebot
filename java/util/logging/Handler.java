/*
 * Decompiled with CFR 0.152.
 */
package java.util.logging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public abstract class Handler {
    private static final int offValue = Level.OFF.intValue();
    private final LogManager manager = LogManager.getLogManager();
    private volatile Filter filter;
    private volatile Formatter formatter;
    private volatile Level logLevel = Level.ALL;
    private volatile ErrorManager errorManager = new ErrorManager();
    private volatile String encoding;
    private final ReentrantLock lock = this.initLocking();

    protected Handler() {
    }

    private ReentrantLock initLocking() {
        Class<?> clazz = this.getClass();
        ClassLoader loader = clazz.getClassLoader();
        if (loader != null && loader != ClassLoader.getPlatformClassLoader()) {
            return null;
        }
        return new ReentrantLock();
    }

    Handler(Level defaultLevel, Formatter defaultFormatter, Formatter specifiedFormatter) {
        this();
        LogManager manager = LogManager.getLogManager();
        String cname = this.getClass().getName();
        final Level level = manager.getLevelProperty(cname + ".level", defaultLevel);
        final Filter filter = manager.getFilterProperty(cname + ".filter", null);
        final Formatter formatter = specifiedFormatter == null ? manager.getFormatterProperty(cname + ".formatter", defaultFormatter) : specifiedFormatter;
        final String encoding = manager.getStringProperty(cname + ".encoding", null);
        AccessController.doPrivileged(new PrivilegedAction<Void>(){
            final /* synthetic */ Handler this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public Void run() {
                this.this$0.setLevel(level);
                this.this$0.setFilter(filter);
                this.this$0.setFormatter(formatter);
                try {
                    this.this$0.setEncoding(encoding);
                }
                catch (Exception ex) {
                    try {
                        this.this$0.setEncoding(null);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                return null;
            }
        }, null, LogManager.controlPermission);
    }

    boolean tryUseLock() {
        if (this.lock == null) {
            return false;
        }
        this.lock.lock();
        return true;
    }

    void unlock() {
        this.lock.unlock();
    }

    public abstract void publish(LogRecord var1);

    public abstract void flush();

    public abstract void close() throws SecurityException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFormatter(Formatter newFormatter) throws SecurityException {
        if (this.tryUseLock()) {
            try {
                this.setFormatter0(newFormatter);
            }
            finally {
                this.unlock();
            }
        }
        Handler handler = this;
        synchronized (handler) {
            this.setFormatter0(newFormatter);
        }
    }

    private void setFormatter0(Formatter newFormatter) throws SecurityException {
        this.checkPermission();
        this.formatter = Objects.requireNonNull(newFormatter);
    }

    public Formatter getFormatter() {
        return this.formatter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        if (this.tryUseLock()) {
            try {
                this.setEncoding0(encoding);
            }
            finally {
                this.unlock();
            }
        }
        Handler handler = this;
        synchronized (handler) {
            this.setEncoding0(encoding);
        }
    }

    private void setEncoding0(String encoding) throws SecurityException, UnsupportedEncodingException {
        this.checkPermission();
        if (encoding != null) {
            try {
                if (!Charset.isSupported(encoding)) {
                    throw new UnsupportedEncodingException(encoding);
                }
            }
            catch (IllegalCharsetNameException e) {
                throw new UnsupportedEncodingException(encoding);
            }
        }
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFilter(Filter newFilter) throws SecurityException {
        if (this.tryUseLock()) {
            try {
                this.setFilter0(newFilter);
            }
            finally {
                this.unlock();
            }
        }
        Handler handler = this;
        synchronized (handler) {
            this.setFilter0(newFilter);
        }
    }

    private void setFilter0(Filter newFilter) throws SecurityException {
        this.checkPermission();
        this.filter = newFilter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setErrorManager(ErrorManager em) {
        if (this.tryUseLock()) {
            try {
                this.setErrorManager0(em);
            }
            finally {
                this.unlock();
            }
        }
        Handler handler = this;
        synchronized (handler) {
            this.setErrorManager0(em);
        }
    }

    private void setErrorManager0(ErrorManager em) {
        this.checkPermission();
        if (em == null) {
            throw new NullPointerException();
        }
        this.errorManager = em;
    }

    public ErrorManager getErrorManager() {
        this.checkPermission();
        return this.errorManager;
    }

    protected void reportError(String msg, Exception ex, int code) {
        try {
            this.errorManager.error(msg, ex, code);
        }
        catch (Exception ex2) {
            System.err.println("Handler.reportError caught:");
            ex2.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLevel(Level newLevel) throws SecurityException {
        if (this.tryUseLock()) {
            try {
                this.setLevel0(newLevel);
            }
            finally {
                this.unlock();
            }
        }
        Handler handler = this;
        synchronized (handler) {
            this.setLevel0(newLevel);
        }
    }

    private void setLevel0(Level newLevel) throws SecurityException {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        this.checkPermission();
        this.logLevel = newLevel;
    }

    public Level getLevel() {
        return this.logLevel;
    }

    public boolean isLoggable(LogRecord record) {
        int levelValue = this.getLevel().intValue();
        if (record == null) {
            return false;
        }
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        Filter filter = this.getFilter();
        if (filter == null) {
            return true;
        }
        return filter.isLoggable(record);
    }

    void checkPermission() throws SecurityException {
        this.manager.checkPermission();
    }
}

