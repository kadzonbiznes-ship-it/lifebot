/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import com.sun.imageio.plugins.bmp.BMPImageReaderSpi;
import com.sun.imageio.plugins.bmp.BMPImageWriterSpi;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.sun.imageio.plugins.png.PNGImageReaderSpi;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import com.sun.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.sun.imageio.plugins.tiff.TIFFImageWriterSpi;
import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;
import com.sun.imageio.plugins.wbmp.WBMPImageWriterSpi;
import com.sun.imageio.spi.FileImageInputStreamSpi;
import com.sun.imageio.spi.FileImageOutputStreamSpi;
import com.sun.imageio.spi.InputStreamImageInputStreamSpi;
import com.sun.imageio.spi.OutputStreamImageOutputStreamSpi;
import com.sun.imageio.spi.RAFImageInputStreamSpi;
import com.sun.imageio.spi.RAFImageOutputStreamSpi;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import sun.awt.AppContext;

public final class IIORegistry
extends ServiceRegistry {
    private static final List<Class<?>> initialCategories = List.of(ImageReaderSpi.class, ImageWriterSpi.class, ImageTranscoderSpi.class, ImageInputStreamSpi.class, ImageOutputStreamSpi.class);

    private IIORegistry() {
        super(initialCategories.iterator());
        this.registerStandardSpis();
        this.registerApplicationClasspathSpis();
    }

    public static IIORegistry getDefaultInstance() {
        AppContext context = AppContext.getAppContext();
        IIORegistry registry = (IIORegistry)context.get(IIORegistry.class);
        if (registry == null) {
            registry = new IIORegistry();
            context.put(IIORegistry.class, registry);
        }
        return registry;
    }

    private void registerStandardSpis() {
        this.registerServiceProvider(new GIFImageReaderSpi());
        this.registerServiceProvider(new GIFImageWriterSpi());
        this.registerServiceProvider(new BMPImageReaderSpi());
        this.registerServiceProvider(new BMPImageWriterSpi());
        this.registerServiceProvider(new WBMPImageReaderSpi());
        this.registerServiceProvider(new WBMPImageWriterSpi());
        this.registerServiceProvider(new TIFFImageReaderSpi());
        this.registerServiceProvider(new TIFFImageWriterSpi());
        this.registerServiceProvider(new PNGImageReaderSpi());
        this.registerServiceProvider(new PNGImageWriterSpi());
        this.registerServiceProvider(new JPEGImageReaderSpi());
        this.registerServiceProvider(new JPEGImageWriterSpi());
        this.registerServiceProvider(new FileImageInputStreamSpi());
        this.registerServiceProvider(new FileImageOutputStreamSpi());
        this.registerServiceProvider(new InputStreamImageInputStreamSpi());
        this.registerServiceProvider(new OutputStreamImageOutputStreamSpi());
        this.registerServiceProvider(new RAFImageInputStreamSpi());
        this.registerServiceProvider(new RAFImageOutputStreamSpi());
        this.registerInstalledProviders();
    }

    public void registerApplicationClasspathSpis() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Iterator<Class<?>> categories = this.getCategories();
        while (categories.hasNext()) {
            Class<?> c = categories.next();
            Iterator<?> riter = ServiceLoader.load(c, loader).iterator();
            while (riter.hasNext()) {
                try {
                    IIOServiceProvider r = (IIOServiceProvider)riter.next();
                    this.registerServiceProvider(r);
                }
                catch (ServiceConfigurationError err) {
                    if (System.getSecurityManager() != null) {
                        err.printStackTrace();
                        continue;
                    }
                    throw err;
                }
            }
        }
    }

    private void registerInstalledProviders() {
        PrivilegedAction<Object> doRegistration = new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                Iterator<Class<?>> categories = IIORegistry.this.getCategories();
                while (categories.hasNext()) {
                    Class<?> c = categories.next();
                    for (IIOServiceProvider p : ServiceLoader.loadInstalled(c)) {
                        IIORegistry.this.registerServiceProvider(p);
                    }
                }
                return this;
            }
        };
        AccessController.doPrivileged(doRegistration);
    }
}

