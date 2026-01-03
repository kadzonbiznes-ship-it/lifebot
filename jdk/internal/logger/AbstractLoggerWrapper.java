/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.logger;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import sun.util.logging.PlatformLogger;

abstract class AbstractLoggerWrapper<L extends System.Logger>
implements System.Logger,
PlatformLogger.Bridge,
PlatformLogger.ConfigurableBridge {
    AbstractLoggerWrapper() {
    }

    abstract L wrapped();

    abstract PlatformLogger.Bridge platformProxy();

    L getWrapped() {
        return this.wrapped();
    }

    @Override
    public final String getName() {
        return this.wrapped().getName();
    }

    @Override
    public boolean isLoggable(System.Logger.Level level) {
        return this.wrapped().isLoggable(level);
    }

    @Override
    public void log(System.Logger.Level level, String msg) {
        this.wrapped().log(level, msg);
    }

    @Override
    public void log(System.Logger.Level level, Supplier<String> msgSupplier) {
        this.wrapped().log(level, msgSupplier);
    }

    @Override
    public void log(System.Logger.Level level, Object obj) {
        this.wrapped().log(level, obj);
    }

    @Override
    public void log(System.Logger.Level level, String msg, Throwable thrown) {
        this.wrapped().log(level, msg, thrown);
    }

    @Override
    public void log(System.Logger.Level level, Supplier<String> msgSupplier, Throwable thrown) {
        this.wrapped().log(level, msgSupplier, thrown);
    }

    @Override
    public void log(System.Logger.Level level, String format, Object ... params) {
        this.wrapped().log(level, format, params);
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
        this.wrapped().log(level, bundle, key, thrown);
    }

    @Override
    public void log(System.Logger.Level level, ResourceBundle bundle, String format, Object ... params) {
        this.wrapped().log(level, bundle, format, params);
    }

    @Override
    public boolean isLoggable(PlatformLogger.Level level) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            return this.isLoggable(level.systemLevel());
        }
        return platformProxy.isLoggable(level);
    }

    @Override
    public boolean isEnabled() {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        return platformProxy == null || platformProxy.isEnabled();
    }

    @Override
    public void log(PlatformLogger.Level level, String msg) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), msg);
        } else {
            platformProxy.log(level, msg);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, String msg, Throwable thrown) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), msg, thrown);
        } else {
            platformProxy.log(level, msg, thrown);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, String msg, Object ... params) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), msg, params);
        } else {
            platformProxy.log(level, msg, params);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, Supplier<String> msgSupplier) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), msgSupplier);
        } else {
            platformProxy.log(level, msgSupplier);
        }
    }

    @Override
    public void log(PlatformLogger.Level level, Throwable thrown, Supplier<String> msgSupplier) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), msgSupplier, thrown);
        } else {
            platformProxy.log(level, thrown, msgSupplier);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), msg);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    sourceClass = sourceClass == null ? "" : sourceClass;
                    sourceMethod = sourceMethod == null ? "" : sourceMethod;
                    msg = msg == null ? "" : msg;
                    wrapped.log(systemLevel, String.format("[%s %s] %s", sourceClass, sourceMethod, msg));
                }
            }
        } else {
            platformProxy.logp(level, sourceClass, sourceMethod, msg);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), msgSupplier);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    String sClass = sourceClass == null ? "" : sourceClass;
                    String sMethod = sourceMethod == null ? "" : sourceMethod;
                    wrapped.log(systemLevel, () -> String.format("[%s %s] %s", sClass, sMethod, msgSupplier.get()));
                }
            }
        } else {
            platformProxy.logp(level, sourceClass, sourceMethod, msgSupplier);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Object ... params) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), msg, params);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    sourceClass = sourceClass == null ? "" : sourceClass;
                    sourceMethod = sourceMethod == null ? "" : sourceMethod;
                    msg = msg == null ? "" : msg;
                    wrapped.log(systemLevel, String.format("[%s %s] %s", sourceClass, sourceMethod, msg), params);
                }
            }
        } else {
            platformProxy.logp(level, sourceClass, sourceMethod, msg, params);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), msg, thrown);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    sourceClass = sourceClass == null ? "" : sourceClass;
                    sourceMethod = sourceMethod == null ? "" : sourceMethod;
                    msg = msg == null ? "" : msg;
                    wrapped.log(systemLevel, String.format("[%s %s] %s", sourceClass, sourceMethod, msg), thrown);
                }
            }
        } else {
            platformProxy.logp(level, sourceClass, sourceMethod, msg, thrown);
        }
    }

    @Override
    public void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), msgSupplier, thrown);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    String sClass = sourceClass == null ? "" : sourceClass;
                    String sMethod = sourceMethod == null ? "" : sourceMethod;
                    wrapped.log(systemLevel, () -> String.format("[%s %s] %s", sClass, sMethod, msgSupplier.get()), thrown);
                }
            }
        } else {
            platformProxy.logp(level, sourceClass, sourceMethod, thrown, msgSupplier);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object ... params) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (bundle != null || sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), bundle, msg, params);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    sourceClass = sourceClass == null ? "" : sourceClass;
                    sourceMethod = sourceMethod == null ? "" : sourceMethod;
                    msg = msg == null ? "" : msg;
                    wrapped.log(systemLevel, bundle, String.format("[%s %s] %s", sourceClass, sourceMethod, msg), params);
                }
            }
        } else {
            platformProxy.logrb(level, sourceClass, sourceMethod, bundle, msg, params);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            if (bundle != null || sourceClass == null && sourceMethod == null) {
                this.wrapped().log(level.systemLevel(), bundle, msg, thrown);
            } else {
                System.Logger.Level systemLevel = level.systemLevel();
                L wrapped = this.wrapped();
                if (wrapped.isLoggable(systemLevel)) {
                    sourceClass = sourceClass == null ? "" : sourceClass;
                    sourceMethod = sourceMethod == null ? "" : sourceMethod;
                    msg = msg == null ? "" : msg;
                    wrapped.log(systemLevel, bundle, String.format("[%s %s] %s", sourceClass, sourceMethod, msg), thrown);
                }
            }
        } else {
            platformProxy.logrb(level, sourceClass, sourceMethod, bundle, msg, thrown);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), bundle, msg, thrown);
        } else {
            platformProxy.logrb(level, bundle, msg, thrown);
        }
    }

    @Override
    public void logrb(PlatformLogger.Level level, ResourceBundle bundle, String msg, Object ... params) {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        if (platformProxy == null) {
            this.wrapped().log(level.systemLevel(), bundle, msg, params);
        } else {
            platformProxy.logrb(level, bundle, msg, params);
        }
    }

    @Override
    public PlatformLogger.ConfigurableBridge.LoggerConfiguration getLoggerConfiguration() {
        PlatformLogger.Bridge platformProxy = this.platformProxy();
        return platformProxy == null ? null : PlatformLogger.ConfigurableBridge.getLoggerConfiguration(platformProxy);
    }
}

