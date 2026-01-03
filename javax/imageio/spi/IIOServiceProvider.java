/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.util.Locale;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

public abstract class IIOServiceProvider
implements RegisterableService {
    protected String vendorName;
    protected String version;

    public IIOServiceProvider(String vendorName, String version) {
        if (vendorName == null) {
            throw new IllegalArgumentException("vendorName == null!");
        }
        if (version == null) {
            throw new IllegalArgumentException("version == null!");
        }
        this.vendorName = vendorName;
        this.version = version;
    }

    public IIOServiceProvider() {
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
    }

    @Override
    public void onDeregistration(ServiceRegistry registry, Class<?> category) {
    }

    public String getVendorName() {
        return this.vendorName;
    }

    public String getVersion() {
        return this.version;
    }

    public abstract String getDescription(Locale var1);
}

