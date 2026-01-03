/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.core.spi.ConfigurationEvent
 *  ch.qos.logback.core.spi.ConfigurationEventListener
 *  ch.qos.logback.core.spi.SequenceNumberGenerator
 */
package ch.qos.logback.core;

import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEventListener;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.status.StatusManager;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public interface Context
extends PropertyContainer {
    public StatusManager getStatusManager();

    public Object getObject(String var1);

    public void putObject(String var1, Object var2);

    @Override
    public String getProperty(String var1);

    public void putProperty(String var1, String var2);

    @Override
    public Map<String, String> getCopyOfPropertyMap();

    public String getName();

    public void setName(String var1);

    public long getBirthTime();

    public Object getConfigurationLock();

    public ScheduledExecutorService getScheduledExecutorService();

    public ExecutorService getExecutorService();

    default public ExecutorService getAlternateExecutorService() {
        return this.getExecutorService();
    }

    public void register(LifeCycle var1);

    public void addScheduledFuture(ScheduledFuture<?> var1);

    public SequenceNumberGenerator getSequenceNumberGenerator();

    public void setSequenceNumberGenerator(SequenceNumberGenerator var1);

    public void addConfigurationEventListener(ConfigurationEventListener var1);

    public void fireConfigurationEvent(ConfigurationEvent var1);
}

