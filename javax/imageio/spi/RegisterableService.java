/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import javax.imageio.spi.ServiceRegistry;

public interface RegisterableService {
    public void onRegistration(ServiceRegistry var1, Class<?> var2);

    public void onDeregistration(ServiceRegistry var1, Class<?> var2);
}

