/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.imageio.spi.FilterIterator;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.SubRegistry;

public class ServiceRegistry {
    private Map<Class<?>, SubRegistry> categoryMap = new HashMap();

    public ServiceRegistry(Iterator<Class<?>> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("categories == null!");
        }
        while (categories.hasNext()) {
            Class<?> category = categories.next();
            ServiceRegistry.checkClassAllowed(category);
            SubRegistry reg = new SubRegistry(this, category);
            this.categoryMap.put(category, reg);
        }
    }

    public static <T> Iterator<T> lookupProviders(Class<T> providerClass, ClassLoader loader) {
        if (providerClass == null) {
            throw new IllegalArgumentException("providerClass == null!");
        }
        ServiceRegistry.checkClassAllowed(providerClass);
        return ServiceLoader.load(providerClass, loader).iterator();
    }

    public static <T> Iterator<T> lookupProviders(Class<T> providerClass) {
        if (providerClass == null) {
            throw new IllegalArgumentException("providerClass == null!");
        }
        ServiceRegistry.checkClassAllowed(providerClass);
        return ServiceLoader.load(providerClass).iterator();
    }

    public Iterator<Class<?>> getCategories() {
        Set<Class<?>> keySet = this.categoryMap.keySet();
        return keySet.iterator();
    }

    private Iterator<SubRegistry> getSubRegistries(Object provider) {
        ArrayList<SubRegistry> l = new ArrayList<SubRegistry>();
        for (Class<?> c : this.categoryMap.keySet()) {
            if (!c.isInstance(provider)) continue;
            l.add(this.categoryMap.get(c));
        }
        return l.iterator();
    }

    public <T> boolean registerServiceProvider(T provider, Class<T> category) {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        if (!category.isInstance(provider)) {
            throw new ClassCastException();
        }
        return reg.registerServiceProvider(provider);
    }

    public void registerServiceProvider(Object provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator<SubRegistry> regs = this.getSubRegistries(provider);
        while (regs.hasNext()) {
            SubRegistry reg = regs.next();
            reg.registerServiceProvider(provider);
        }
    }

    public void registerServiceProviders(Iterator<?> providers) {
        if (providers == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        while (providers.hasNext()) {
            this.registerServiceProvider(providers.next());
        }
    }

    public <T> boolean deregisterServiceProvider(T provider, Class<T> category) {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        if (!category.isInstance(provider)) {
            throw new ClassCastException();
        }
        return reg.deregisterServiceProvider(provider);
    }

    public void deregisterServiceProvider(Object provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator<SubRegistry> regs = this.getSubRegistries(provider);
        while (regs.hasNext()) {
            SubRegistry reg = regs.next();
            reg.deregisterServiceProvider(provider);
        }
    }

    public boolean contains(Object provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null!");
        }
        Iterator<SubRegistry> regs = this.getSubRegistries(provider);
        while (regs.hasNext()) {
            SubRegistry reg = regs.next();
            if (!reg.contains(provider)) continue;
            return true;
        }
        return false;
    }

    public <T> Iterator<T> getServiceProviders(Class<T> category, boolean useOrdering) {
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        Iterator<Object> it = reg.getServiceProviders(useOrdering);
        return it;
    }

    public <T> Iterator<T> getServiceProviders(Class<T> category, Filter filter, boolean useOrdering) {
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        Iterator<T> iter = this.getServiceProviders(category, useOrdering);
        return new FilterIterator<T>(iter, filter);
    }

    public <T> T getServiceProviderByClass(Class<T> providerClass) {
        if (providerClass == null) {
            throw new IllegalArgumentException("providerClass == null!");
        }
        for (Class<T> clazz : this.categoryMap.keySet()) {
            SubRegistry reg;
            T provider;
            if (!clazz.isAssignableFrom(providerClass) || (provider = (reg = this.categoryMap.get(clazz)).getServiceProviderByClass(providerClass)) == null) continue;
            return provider;
        }
        return null;
    }

    public <T> boolean setOrdering(Class<T> category, T firstProvider, T secondProvider) {
        if (firstProvider == null || secondProvider == null) {
            throw new IllegalArgumentException("provider is null!");
        }
        if (firstProvider == secondProvider) {
            throw new IllegalArgumentException("providers are the same!");
        }
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        if (reg.contains(firstProvider) && reg.contains(secondProvider)) {
            return reg.setOrdering(firstProvider, secondProvider);
        }
        return false;
    }

    public <T> boolean unsetOrdering(Class<T> category, T firstProvider, T secondProvider) {
        if (firstProvider == null || secondProvider == null) {
            throw new IllegalArgumentException("provider is null!");
        }
        if (firstProvider == secondProvider) {
            throw new IllegalArgumentException("providers are the same!");
        }
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        if (reg.contains(firstProvider) && reg.contains(secondProvider)) {
            return reg.unsetOrdering(firstProvider, secondProvider);
        }
        return false;
    }

    public void deregisterAll(Class<?> category) {
        SubRegistry reg = this.categoryMap.get(category);
        if (reg == null) {
            throw new IllegalArgumentException("category unknown!");
        }
        reg.clear();
    }

    public void deregisterAll() {
        for (SubRegistry reg : this.categoryMap.values()) {
            reg.clear();
        }
    }

    @Deprecated(since="9", forRemoval=true)
    public void finalize() throws Throwable {
        this.deregisterAll();
        super.finalize();
    }

    private static void checkClassAllowed(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class must not be null");
        }
        if (clazz != ImageInputStreamSpi.class && clazz != ImageOutputStreamSpi.class && clazz != ImageReaderSpi.class && clazz != ImageTranscoderSpi.class && clazz != ImageWriterSpi.class) {
            throw new IllegalArgumentException(clazz.getName() + " is not an ImageIO SPI class");
        }
    }

    public static interface Filter {
        public boolean filter(Object var1);
    }
}

