/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.logging.AbstractInternalLogger$1
 *  io.netty.util.internal.logging.InternalLogLevel
 */
package io.netty.util.internal.logging;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.AbstractInternalLogger;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.ObjectStreamException;
import java.io.Serializable;

public abstract class AbstractInternalLogger
implements InternalLogger,
Serializable {
    private static final long serialVersionUID = -6382972526573193470L;
    static final String EXCEPTION_MESSAGE = "Unexpected exception:";
    private final String name;

    protected AbstractInternalLogger(String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean isEnabled(InternalLogLevel level) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                return this.isTraceEnabled();
            }
            case 2: {
                return this.isDebugEnabled();
            }
            case 3: {
                return this.isInfoEnabled();
            }
            case 4: {
                return this.isWarnEnabled();
            }
            case 5: {
                return this.isErrorEnabled();
            }
        }
        throw new Error();
    }

    @Override
    public void trace(Throwable t) {
        this.trace(EXCEPTION_MESSAGE, t);
    }

    @Override
    public void debug(Throwable t) {
        this.debug(EXCEPTION_MESSAGE, t);
    }

    @Override
    public void info(Throwable t) {
        this.info(EXCEPTION_MESSAGE, t);
    }

    @Override
    public void warn(Throwable t) {
        this.warn(EXCEPTION_MESSAGE, t);
    }

    @Override
    public void error(Throwable t) {
        this.error(EXCEPTION_MESSAGE, t);
    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable cause) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(msg, cause);
                break;
            }
            case 2: {
                this.debug(msg, cause);
                break;
            }
            case 3: {
                this.info(msg, cause);
                break;
            }
            case 4: {
                this.warn(msg, cause);
                break;
            }
            case 5: {
                this.error(msg, cause);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    @Override
    public void log(InternalLogLevel level, Throwable cause) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(cause);
                break;
            }
            case 2: {
                this.debug(cause);
                break;
            }
            case 3: {
                this.info(cause);
                break;
            }
            case 4: {
                this.warn(cause);
                break;
            }
            case 5: {
                this.error(cause);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    @Override
    public void log(InternalLogLevel level, String msg) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(msg);
                break;
            }
            case 2: {
                this.debug(msg);
                break;
            }
            case 3: {
                this.info(msg);
                break;
            }
            case 4: {
                this.warn(msg);
                break;
            }
            case 5: {
                this.error(msg);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(format, arg);
                break;
            }
            case 2: {
                this.debug(format, arg);
                break;
            }
            case 3: {
                this.info(format, arg);
                break;
            }
            case 4: {
                this.warn(format, arg);
                break;
            }
            case 5: {
                this.error(format, arg);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(format, argA, argB);
                break;
            }
            case 2: {
                this.debug(format, argA, argB);
                break;
            }
            case 3: {
                this.info(format, argA, argB);
                break;
            }
            case 4: {
                this.warn(format, argA, argB);
                break;
            }
            case 5: {
                this.error(format, argA, argB);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object ... arguments) {
        switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()]) {
            case 1: {
                this.trace(format, arguments);
                break;
            }
            case 2: {
                this.debug(format, arguments);
                break;
            }
            case 3: {
                this.info(format, arguments);
                break;
            }
            case 4: {
                this.warn(format, arguments);
                break;
            }
            case 5: {
                this.error(format, arguments);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    protected Object readResolve() throws ObjectStreamException {
        return InternalLoggerFactory.getInstance(this.name());
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + this.name() + ')';
    }
}

