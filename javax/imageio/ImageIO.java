/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import sun.awt.AppContext;
import sun.security.action.GetPropertyAction;

public final class ImageIO {
    private static final IIORegistry theRegistry = IIORegistry.getDefaultInstance();
    private static Method readerFormatNamesMethod;
    private static Method readerFileSuffixesMethod;
    private static Method readerMIMETypesMethod;
    private static Method writerFormatNamesMethod;
    private static Method writerFileSuffixesMethod;
    private static Method writerMIMETypesMethod;

    private ImageIO() {
    }

    public static void scanForPlugins() {
        theRegistry.registerApplicationClasspathSpis();
    }

    private static synchronized CacheInfo getCacheInfo() {
        AppContext context = AppContext.getAppContext();
        CacheInfo info = (CacheInfo)context.get(CacheInfo.class);
        if (info == null) {
            info = new CacheInfo();
            context.put(CacheInfo.class, info);
        }
        return info;
    }

    private static String getTempDir() {
        GetPropertyAction a = new GetPropertyAction("java.io.tmpdir");
        return AccessController.doPrivileged(a);
    }

    private static boolean hasCachePermission() {
        Boolean hasPermission = ImageIO.getCacheInfo().getHasPermission();
        if (hasPermission != null) {
            return hasPermission;
        }
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                String cachepath;
                File cachedir = ImageIO.getCacheDirectory();
                if (cachedir != null) {
                    cachepath = cachedir.getPath();
                } else {
                    cachepath = ImageIO.getTempDir();
                    if (cachepath == null || cachepath.isEmpty()) {
                        ImageIO.getCacheInfo().setHasPermission(Boolean.FALSE);
                        return false;
                    }
                }
                Object filepath = cachepath;
                if (!((String)filepath).endsWith(File.separator)) {
                    filepath = (String)filepath + File.separator;
                }
                filepath = (String)filepath + "*";
                security.checkPermission(new FilePermission((String)filepath, "read, write, delete"));
            }
        }
        catch (SecurityException e) {
            ImageIO.getCacheInfo().setHasPermission(Boolean.FALSE);
            return false;
        }
        ImageIO.getCacheInfo().setHasPermission(Boolean.TRUE);
        return true;
    }

    public static void setUseCache(boolean useCache) {
        ImageIO.getCacheInfo().setUseCache(useCache);
    }

    public static boolean getUseCache() {
        return ImageIO.getCacheInfo().getUseCache();
    }

    public static void setCacheDirectory(File cacheDirectory) {
        if (cacheDirectory != null && !cacheDirectory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory!");
        }
        ImageIO.getCacheInfo().setCacheDirectory(cacheDirectory);
        ImageIO.getCacheInfo().setHasPermission(null);
    }

    public static File getCacheDirectory() {
        return ImageIO.getCacheInfo().getCacheDirectory();
    }

    public static ImageInputStream createImageInputStream(Object input) throws IOException {
        boolean usecache;
        Iterator<ImageInputStreamSpi> iter;
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageInputStreamSpi.class, true);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        boolean bl = usecache = ImageIO.getUseCache() && ImageIO.hasCachePermission();
        while (iter.hasNext()) {
            ImageInputStreamSpi spi = iter.next();
            if (!spi.getInputClass().isInstance(input)) continue;
            try {
                return spi.createInputStreamInstance(input, usecache, ImageIO.getCacheDirectory());
            }
            catch (IOException e) {
                throw new IIOException("Can't create cache file!", e);
            }
        }
        return null;
    }

    public static ImageOutputStream createImageOutputStream(Object output) throws IOException {
        boolean usecache;
        Iterator<ImageOutputStreamSpi> iter;
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageOutputStreamSpi.class, true);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        boolean bl = usecache = ImageIO.getUseCache() && ImageIO.hasCachePermission();
        while (iter.hasNext()) {
            ImageOutputStreamSpi spi = iter.next();
            if (!spi.getOutputClass().isInstance(output)) continue;
            try {
                return spi.createOutputStreamInstance(output, usecache, ImageIO.getCacheDirectory());
            }
            catch (IOException e) {
                throw new IIOException("Can't create cache file!", e);
            }
        }
        return null;
    }

    private static <S extends ImageReaderWriterSpi> String[] getReaderWriterInfo(Class<S> spiClass, SpiInfo spiInfo) {
        Iterator<S> iter;
        try {
            iter = theRegistry.getServiceProviders(spiClass, true);
        }
        catch (IllegalArgumentException e) {
            return new String[0];
        }
        HashSet s = new HashSet();
        while (iter.hasNext()) {
            ImageReaderWriterSpi spi = (ImageReaderWriterSpi)iter.next();
            String[] info = spiInfo.info(spi);
            if (info == null) continue;
            Collections.addAll(s, info);
        }
        return s.toArray(new String[s.size()]);
    }

    public static String[] getReaderFormatNames() {
        return ImageIO.getReaderWriterInfo(ImageReaderSpi.class, SpiInfo.FORMAT_NAMES);
    }

    public static String[] getReaderMIMETypes() {
        return ImageIO.getReaderWriterInfo(ImageReaderSpi.class, SpiInfo.MIME_TYPES);
    }

    public static String[] getReaderFileSuffixes() {
        return ImageIO.getReaderWriterInfo(ImageReaderSpi.class, SpiInfo.FILE_SUFFIXES);
    }

    public static Iterator<ImageReader> getImageReaders(Object input) {
        Iterator<ImageReaderSpi> iter;
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class, new CanDecodeInputFilter(input), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }

    public static Iterator<ImageReader> getImageReadersByFormatName(String formatName) {
        Iterator<ImageReaderSpi> iter;
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class, new ContainsFilter(readerFormatNamesMethod, formatName), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }

    public static Iterator<ImageReader> getImageReadersBySuffix(String fileSuffix) {
        Iterator<ImageReaderSpi> iter;
        if (fileSuffix == null) {
            throw new IllegalArgumentException("fileSuffix == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class, new ContainsFilter(readerFileSuffixesMethod, fileSuffix), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }

    public static Iterator<ImageReader> getImageReadersByMIMEType(String MIMEType) {
        Iterator<ImageReaderSpi> iter;
        if (MIMEType == null) {
            throw new IllegalArgumentException("MIMEType == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageReaderSpi.class, new ContainsFilter(readerMIMETypesMethod, MIMEType), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageReaderIterator(iter);
    }

    public static String[] getWriterFormatNames() {
        return ImageIO.getReaderWriterInfo(ImageWriterSpi.class, SpiInfo.FORMAT_NAMES);
    }

    public static String[] getWriterMIMETypes() {
        return ImageIO.getReaderWriterInfo(ImageWriterSpi.class, SpiInfo.MIME_TYPES);
    }

    public static String[] getWriterFileSuffixes() {
        return ImageIO.getReaderWriterInfo(ImageWriterSpi.class, SpiInfo.FILE_SUFFIXES);
    }

    private static boolean contains(String[] names, String name) {
        for (int i = 0; i < names.length; ++i) {
            if (!name.equalsIgnoreCase(names[i])) continue;
            return true;
        }
        return false;
    }

    public static Iterator<ImageWriter> getImageWritersByFormatName(String formatName) {
        Iterator<ImageWriterSpi> iter;
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class, new ContainsFilter(writerFormatNamesMethod, formatName), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }

    public static Iterator<ImageWriter> getImageWritersBySuffix(String fileSuffix) {
        Iterator<ImageWriterSpi> iter;
        if (fileSuffix == null) {
            throw new IllegalArgumentException("fileSuffix == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class, new ContainsFilter(writerFileSuffixesMethod, fileSuffix), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }

    public static Iterator<ImageWriter> getImageWritersByMIMEType(String MIMEType) {
        Iterator<ImageWriterSpi> iter;
        if (MIMEType == null) {
            throw new IllegalArgumentException("MIMEType == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class, new ContainsFilter(writerMIMETypesMethod, MIMEType), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }

    public static ImageWriter getImageWriter(ImageReader reader) {
        String[] writerNames;
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        ImageReaderSpi readerSpi = reader.getOriginatingProvider();
        if (readerSpi == null) {
            Iterator<ImageReaderSpi> readerSpiIter;
            try {
                readerSpiIter = theRegistry.getServiceProviders(ImageReaderSpi.class, false);
            }
            catch (IllegalArgumentException e) {
                return null;
            }
            while (readerSpiIter.hasNext()) {
                ImageReaderSpi temp = readerSpiIter.next();
                if (!temp.isOwnReader(reader)) continue;
                readerSpi = temp;
                break;
            }
            if (readerSpi == null) {
                return null;
            }
        }
        if ((writerNames = readerSpi.getImageWriterSpiNames()) == null) {
            return null;
        }
        Class<?> writerSpiClass = null;
        try {
            writerSpiClass = Class.forName(writerNames[0], true, ClassLoader.getSystemClassLoader());
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        ImageWriterSpi writerSpi = (ImageWriterSpi)theRegistry.getServiceProviderByClass(writerSpiClass);
        if (writerSpi == null) {
            return null;
        }
        try {
            return writerSpi.createWriterInstance();
        }
        catch (IOException e) {
            theRegistry.deregisterServiceProvider(writerSpi, ImageWriterSpi.class);
            return null;
        }
    }

    public static ImageReader getImageReader(ImageWriter writer) {
        String[] readerNames;
        if (writer == null) {
            throw new IllegalArgumentException("writer == null!");
        }
        ImageWriterSpi writerSpi = writer.getOriginatingProvider();
        if (writerSpi == null) {
            Iterator<ImageWriterSpi> writerSpiIter;
            try {
                writerSpiIter = theRegistry.getServiceProviders(ImageWriterSpi.class, false);
            }
            catch (IllegalArgumentException e) {
                return null;
            }
            while (writerSpiIter.hasNext()) {
                ImageWriterSpi temp = writerSpiIter.next();
                if (!temp.isOwnWriter(writer)) continue;
                writerSpi = temp;
                break;
            }
            if (writerSpi == null) {
                return null;
            }
        }
        if ((readerNames = writerSpi.getImageReaderSpiNames()) == null) {
            return null;
        }
        Class<?> readerSpiClass = null;
        try {
            readerSpiClass = Class.forName(readerNames[0], true, ClassLoader.getSystemClassLoader());
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        ImageReaderSpi readerSpi = (ImageReaderSpi)theRegistry.getServiceProviderByClass(readerSpiClass);
        if (readerSpi == null) {
            return null;
        }
        try {
            return readerSpi.createReaderInstance();
        }
        catch (IOException e) {
            theRegistry.deregisterServiceProvider(readerSpi, ImageReaderSpi.class);
            return null;
        }
    }

    public static Iterator<ImageWriter> getImageWriters(ImageTypeSpecifier type, String formatName) {
        Iterator<ImageWriterSpi> iter;
        if (type == null) {
            throw new IllegalArgumentException("type == null!");
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        try {
            iter = theRegistry.getServiceProviders(ImageWriterSpi.class, new CanEncodeImageAndFormatFilter(type, formatName), true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageWriterIterator(iter);
    }

    public static Iterator<ImageTranscoder> getImageTranscoders(ImageReader reader, ImageWriter writer) {
        Iterator<ImageTranscoderSpi> iter;
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer == null!");
        }
        ImageReaderSpi readerSpi = reader.getOriginatingProvider();
        ImageWriterSpi writerSpi = writer.getOriginatingProvider();
        TranscoderFilter filter = new TranscoderFilter(readerSpi, writerSpi);
        try {
            iter = theRegistry.getServiceProviders(ImageTranscoderSpi.class, filter, true);
        }
        catch (IllegalArgumentException e) {
            return Collections.emptyIterator();
        }
        return new ImageTranscoderIterator(iter);
    }

    public static BufferedImage read(File input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        if (!input.canRead()) {
            throw new IIOException("Can't read input file!");
        }
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        if (stream == null) {
            throw new IIOException("Can't create an ImageInputStream!");
        }
        BufferedImage bi = ImageIO.read(stream);
        if (bi == null) {
            stream.close();
        }
        return bi;
    }

    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        if (stream == null) {
            throw new IIOException("Can't create an ImageInputStream!");
        }
        BufferedImage bi = ImageIO.read(stream);
        if (bi == null) {
            stream.close();
        }
        return bi;
    }

    public static BufferedImage read(URL input) throws IOException {
        BufferedImage bi;
        InputStream istream;
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        try {
            istream = input.openStream();
        }
        catch (IOException e) {
            throw new IIOException("Can't get input stream from URL!", e);
        }
        ImageInputStream stream = ImageIO.createImageInputStream(istream);
        if (stream == null) {
            istream.close();
            throw new IIOException("Can't create an ImageInputStream!");
        }
        try (InputStream inputStream = istream;){
            bi = ImageIO.read(stream);
            if (bi == null) {
                stream.close();
            }
        }
        return bi;
    }

    public static BufferedImage read(ImageInputStream stream) throws IOException {
        BufferedImage bi;
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext()) {
            return null;
        }
        ImageReader reader = iter.next();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);
        try (ImageInputStream imageInputStream = stream;){
            bi = reader.read(0, param);
        }
        catch (RuntimeException e) {
            throw new IIOException(e.toString(), e);
        }
        finally {
            reader.dispose();
        }
        return bi;
    }

    public static boolean write(RenderedImage im, String formatName, ImageOutputStream output) throws IOException {
        if (im == null) {
            throw new IllegalArgumentException("im == null!");
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        return ImageIO.doWrite(im, ImageIO.getWriter(im, formatName), output);
    }

    public static boolean write(RenderedImage im, String formatName, File output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        ImageWriter writer = ImageIO.getWriter(im, formatName);
        if (writer == null) {
            return false;
        }
        output.delete();
        ImageOutputStream stream = ImageIO.createImageOutputStream(output);
        if (stream == null) {
            throw new IIOException("Can't create an ImageOutputStream!");
        }
        try (ImageOutputStream imageOutputStream = stream;){
            boolean bl = ImageIO.doWrite(im, writer, stream);
            return bl;
        }
    }

    public static boolean write(RenderedImage im, String formatName, OutputStream output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        ImageOutputStream stream = ImageIO.createImageOutputStream(output);
        if (stream == null) {
            throw new IIOException("Can't create an ImageOutputStream!");
        }
        try (ImageOutputStream imageOutputStream = stream;){
            boolean bl = ImageIO.doWrite(im, ImageIO.getWriter(im, formatName), stream);
            return bl;
        }
    }

    private static ImageWriter getWriter(RenderedImage im, String formatName) {
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(im);
        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, formatName);
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    private static boolean doWrite(RenderedImage im, ImageWriter writer, ImageOutputStream output) throws IOException {
        if (writer == null) {
            return false;
        }
        writer.setOutput(output);
        try {
            writer.write(im);
        }
        finally {
            writer.dispose();
            output.flush();
        }
        return true;
    }

    static {
        try {
            readerFormatNamesMethod = ImageReaderSpi.class.getMethod("getFormatNames", new Class[0]);
            readerFileSuffixesMethod = ImageReaderSpi.class.getMethod("getFileSuffixes", new Class[0]);
            readerMIMETypesMethod = ImageReaderSpi.class.getMethod("getMIMETypes", new Class[0]);
            writerFormatNamesMethod = ImageWriterSpi.class.getMethod("getFormatNames", new Class[0]);
            writerFileSuffixesMethod = ImageWriterSpi.class.getMethod("getFileSuffixes", new Class[0]);
            writerMIMETypesMethod = ImageWriterSpi.class.getMethod("getMIMETypes", new Class[0]);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    static class CacheInfo {
        boolean useCache = true;
        File cacheDirectory = null;
        Boolean hasPermission = null;

        public boolean getUseCache() {
            return this.useCache;
        }

        public void setUseCache(boolean useCache) {
            this.useCache = useCache;
        }

        public File getCacheDirectory() {
            return this.cacheDirectory;
        }

        public void setCacheDirectory(File cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
        }

        public Boolean getHasPermission() {
            return this.hasPermission;
        }

        public void setHasPermission(Boolean hasPermission) {
            this.hasPermission = hasPermission;
        }
    }

    private static enum SpiInfo {
        FORMAT_NAMES{

            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getFormatNames();
            }
        }
        ,
        MIME_TYPES{

            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getMIMETypes();
            }
        }
        ,
        FILE_SUFFIXES{

            @Override
            String[] info(ImageReaderWriterSpi spi) {
                return spi.getFileSuffixes();
            }
        };


        abstract String[] info(ImageReaderWriterSpi var1);
    }

    static class CanDecodeInputFilter
    implements ServiceRegistry.Filter {
        Object input;

        public CanDecodeInputFilter(Object input) {
            this.input = input;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean filter(Object elt) {
            try {
                ImageReaderSpi spi = (ImageReaderSpi)elt;
                ImageInputStream stream = null;
                if (this.input instanceof ImageInputStream) {
                    stream = (ImageInputStream)this.input;
                }
                boolean canDecode = false;
                if (stream != null) {
                    stream.mark();
                }
                try {
                    canDecode = spi.canDecodeInput(this.input);
                }
                finally {
                    if (stream != null) {
                        stream.reset();
                    }
                }
                return canDecode;
            }
            catch (IOException e) {
                return false;
            }
        }
    }

    static class ImageReaderIterator
    implements Iterator<ImageReader> {
        private Iterator<ImageReaderSpi> iter;

        public ImageReaderIterator(Iterator<ImageReaderSpi> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return this.iter.hasNext();
        }

        @Override
        public ImageReader next() {
            ImageReaderSpi spi = null;
            try {
                spi = this.iter.next();
                return spi.createReaderInstance();
            }
            catch (IOException e) {
                theRegistry.deregisterServiceProvider(spi, ImageReaderSpi.class);
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class ContainsFilter
    implements ServiceRegistry.Filter {
        Method method;
        String name;

        public ContainsFilter(Method method, String name) {
            this.method = method;
            this.name = name;
        }

        @Override
        public boolean filter(Object elt) {
            try {
                return ImageIO.contains((String[])this.method.invoke(elt, new Object[0]), this.name);
            }
            catch (Exception e) {
                return false;
            }
        }
    }

    static class ImageWriterIterator
    implements Iterator<ImageWriter> {
        private Iterator<ImageWriterSpi> iter;

        public ImageWriterIterator(Iterator<ImageWriterSpi> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return this.iter.hasNext();
        }

        @Override
        public ImageWriter next() {
            ImageWriterSpi spi = null;
            try {
                spi = this.iter.next();
                return spi.createWriterInstance();
            }
            catch (IOException e) {
                theRegistry.deregisterServiceProvider(spi, ImageWriterSpi.class);
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class CanEncodeImageAndFormatFilter
    implements ServiceRegistry.Filter {
        ImageTypeSpecifier type;
        String formatName;

        public CanEncodeImageAndFormatFilter(ImageTypeSpecifier type, String formatName) {
            this.type = type;
            this.formatName = formatName;
        }

        @Override
        public boolean filter(Object elt) {
            ImageWriterSpi spi = (ImageWriterSpi)elt;
            return Arrays.asList(spi.getFormatNames()).contains(this.formatName) && spi.canEncodeImage(this.type);
        }
    }

    static class TranscoderFilter
    implements ServiceRegistry.Filter {
        String readerSpiName;
        String writerSpiName;

        public TranscoderFilter(ImageReaderSpi readerSpi, ImageWriterSpi writerSpi) {
            this.readerSpiName = readerSpi.getClass().getName();
            this.writerSpiName = writerSpi.getClass().getName();
        }

        @Override
        public boolean filter(Object elt) {
            ImageTranscoderSpi spi = (ImageTranscoderSpi)elt;
            String readerName = spi.getReaderServiceProviderName();
            String writerName = spi.getWriterServiceProviderName();
            return readerName.equals(this.readerSpiName) && writerName.equals(this.writerSpiName);
        }
    }

    static class ImageTranscoderIterator
    implements Iterator<ImageTranscoder> {
        public Iterator<ImageTranscoderSpi> iter;

        public ImageTranscoderIterator(Iterator<ImageTranscoderSpi> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return this.iter.hasNext();
        }

        @Override
        public ImageTranscoder next() {
            ImageTranscoderSpi spi = null;
            spi = this.iter.next();
            return spi.createTranscoderInstance();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

