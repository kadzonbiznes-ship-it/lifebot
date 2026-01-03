/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.spi.PartiallyOrderedSet;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

class SubRegistry {
    ServiceRegistry registry;
    Class<?> category;
    final PartiallyOrderedSet<Object> poset = new PartiallyOrderedSet();
    final Map<Class<?>, Object> map = new HashMap();
    final Map<Class<?>, AccessControlContext> accMap = new HashMap();

    public SubRegistry(ServiceRegistry registry, Class<?> category) {
        this.registry = registry;
        this.category = category;
    }

    public synchronized boolean registerServiceProvider(Object provider) {
        boolean present;
        Object oprovider = this.map.get(provider.getClass());
        boolean bl = present = oprovider != null;
        if (present) {
            this.deregisterServiceProvider(oprovider);
        }
        this.map.put(provider.getClass(), provider);
        this.accMap.put(provider.getClass(), AccessController.getContext());
        this.poset.add(provider);
        if (provider instanceof RegisterableService) {
            RegisterableService rs = (RegisterableService)provider;
            try {
                rs.onRegistration(this.registry, this.category);
            }
            catch (Throwable t) {
                System.err.println("Caught and handled this exception :");
                t.printStackTrace();
            }
        }
        return !present;
    }

    public synchronized boolean deregisterServiceProvider(Object provider) {
        Object oprovider = this.map.get(provider.getClass());
        if (provider == oprovider) {
            this.map.remove(provider.getClass());
            this.accMap.remove(provider.getClass());
            this.poset.remove(provider);
            if (provider instanceof RegisterableService) {
                RegisterableService rs = (RegisterableService)provider;
                rs.onDeregistration(this.registry, this.category);
            }
            return true;
        }
        return false;
    }

    public synchronized boolean contains(Object provider) {
        Object oprovider = this.map.get(provider.getClass());
        return oprovider == provider;
    }

    public synchronized boolean setOrdering(Object firstProvider, Object secondProvider) {
        return this.poset.setOrdering(firstProvider, secondProvider);
    }

    public synchronized boolean unsetOrdering(Object firstProvider, Object secondProvider) {
        return this.poset.unsetOrdering(firstProvider, secondProvider);
    }

    public synchronized Iterator<Object> getServiceProviders(boolean useOrdering) {
        if (useOrdering) {
            return this.poset.iterator();
        }
        return this.map.values().iterator();
    }

    public synchronized <T> T getServiceProviderByClass(Class<T> providerClass) {
        return (T)this.map.get(providerClass);
    }

    public synchronized void clear() {
        Iterator<Object> iter = this.map.values().iterator();
        while (iter.hasNext()) {
            Object provider = iter.next();
            iter.remove();
            if (!(provider instanceof RegisterableService)) continue;
            RegisterableService rs = (RegisterableService)provider;
            AccessControlContext acc = this.accMap.get(provider.getClass());
            if (acc == null && System.getSecurityManager() != null) continue;
            AccessController.doPrivileged(() -> {
                rs.onDeregistration(this.registry, this.category);
                return null;
            }, acc);
        }
        this.poset.clear();
        this.accMap.clear();
    }

    public synchronized void finalize() {
        this.clear();
    }
}

