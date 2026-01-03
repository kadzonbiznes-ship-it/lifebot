/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.unix.LibCAPI$size_t
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.util;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ParseUtil;

@ThreadSafe
public final class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);
    private static final String READING_LOG = "Reading file {}";
    private static final String READ_LOG = "Read {}";
    private static final int BUFFER_SIZE = 1024;

    private FileUtil() {
    }

    public static List<String> readFile(String filename) {
        return FileUtil.readFile(filename, true);
    }

    public static List<String> readFile(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(READING_LOG, (Object)filename);
            }
            try {
                return Files.readAllLines(Paths.get(filename, new String[0]), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                if (reportError) {
                    LOG.error("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
                } else {
                    LOG.debug("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
                }
            }
        } else if (reportError) {
            LOG.warn("File not found or not readable: {}", (Object)filename);
        }
        return Collections.emptyList();
    }

    public static List<String> readLines(String filename, int count) {
        return FileUtil.readLines(filename, count, true);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static List<String> readLines(String filename, int count, boolean reportError) {
        Path file = Paths.get(filename, new String[0]);
        if (Files.isReadable(file)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(READING_LOG, (Object)filename);
            }
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(file, new OpenOption[0]), decoder);){
                ArrayList<String> arrayList;
                try (BufferedReader reader = new BufferedReader(isr, 1024);){
                    String line;
                    ArrayList<String> lines = new ArrayList<String>(count);
                    for (int i = 0; i < count && (line = reader.readLine()) != null; ++i) {
                        lines.add(line);
                    }
                    arrayList = lines;
                }
                return arrayList;
            }
            catch (IOException e) {
                if (reportError) {
                    LOG.error("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
                }
                LOG.debug("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
            }
        } else if (reportError) {
            LOG.warn("File not found or not readable: {}", (Object)filename);
        }
        return Collections.emptyList();
    }

    public static byte[] readAllBytes(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(READING_LOG, (Object)filename);
            }
            try {
                return Files.readAllBytes(Paths.get(filename, new String[0]));
            }
            catch (IOException e) {
                if (reportError) {
                    LOG.error("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
                } else {
                    LOG.debug("Error reading file {}. {}", (Object)filename, (Object)e.getMessage());
                }
            }
        } else if (reportError) {
            LOG.warn("File not found or not readable: {}", (Object)filename);
        }
        return new byte[0];
    }

    public static ByteBuffer readAllBytesAsBuffer(String filename) {
        byte[] bytes = FileUtil.readAllBytes(filename, false);
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.order(ByteOrder.nativeOrder());
        for (byte b : bytes) {
            buff.put(b);
        }
        buff.flip();
        return buff;
    }

    public static byte readByteFromBuffer(ByteBuffer buff) {
        if (buff.position() < buff.limit()) {
            return buff.get();
        }
        return 0;
    }

    public static short readShortFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 2) {
            return buff.getShort();
        }
        return 0;
    }

    public static int readIntFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 4) {
            return buff.getInt();
        }
        return 0;
    }

    public static long readLongFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 8) {
            return buff.getLong();
        }
        return 0L;
    }

    public static NativeLong readNativeLongFromBuffer(ByteBuffer buff) {
        return new NativeLong(Native.LONG_SIZE == 4 ? (long)FileUtil.readIntFromBuffer(buff) : FileUtil.readLongFromBuffer(buff));
    }

    public static LibCAPI.size_t readSizeTFromBuffer(ByteBuffer buff) {
        return new LibCAPI.size_t(Native.SIZE_T_SIZE == 4 ? (long)FileUtil.readIntFromBuffer(buff) : FileUtil.readLongFromBuffer(buff));
    }

    public static void readByteArrayFromBuffer(ByteBuffer buff, byte[] array) {
        if (buff.position() <= buff.limit() - array.length) {
            buff.get(array);
        }
    }

    public static Pointer readPointerFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - Native.POINTER_SIZE) {
            return Native.POINTER_SIZE == 4 ? new Pointer(buff.getInt()) : new Pointer(buff.getLong());
        }
        return Pointer.NULL;
    }

    public static long getLongFromFile(String filename) {
        List<String> read;
        if (LOG.isDebugEnabled()) {
            LOG.debug(READING_LOG, (Object)filename);
        }
        if (!(read = FileUtil.readLines(filename, 1, false)).isEmpty()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(READ_LOG, (Object)read.get(0));
            }
            return ParseUtil.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    public static long getUnsignedLongFromFile(String filename) {
        List<String> read;
        if (LOG.isDebugEnabled()) {
            LOG.debug(READING_LOG, (Object)filename);
        }
        if (!(read = FileUtil.readLines(filename, 1, false)).isEmpty()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(READ_LOG, (Object)read.get(0));
            }
            return ParseUtil.parseUnsignedLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    public static int getIntFromFile(String filename) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(READING_LOG, (Object)filename);
        }
        try {
            List<String> read = FileUtil.readLines(filename, 1, false);
            if (!read.isEmpty()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(READ_LOG, (Object)read.get(0));
                }
                return ParseUtil.parseIntOrDefault(read.get(0), 0);
            }
        }
        catch (NumberFormatException ex) {
            LOG.warn("Unable to read value from {}. {}", (Object)filename, (Object)ex.getMessage());
        }
        return 0;
    }

    public static String getStringFromFile(String filename) {
        List<String> read;
        if (LOG.isDebugEnabled()) {
            LOG.debug(READING_LOG, (Object)filename);
        }
        if (!(read = FileUtil.readLines(filename, 1, false)).isEmpty()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(READ_LOG, (Object)read.get(0));
            }
            return read.get(0);
        }
        return "";
    }

    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (LOG.isDebugEnabled()) {
            LOG.debug(READING_LOG, (Object)filename);
        }
        List<String> lines = FileUtil.readFile(filename, false);
        for (String line : lines) {
            String[] parts = line.split(separator);
            if (parts.length != 2) continue;
            map.put(parts[0], parts[1].trim());
        }
        return map;
    }

    public static Properties readPropertiesFromFilename(String propsFilename) {
        Properties archProps = new Properties();
        for (ClassLoader loader : Stream.of(Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader(), FileUtil.class.getClassLoader()).collect(Collectors.toCollection(LinkedHashSet::new))) {
            if (!FileUtil.readPropertiesFromClassLoader(propsFilename, archProps, loader)) continue;
            return archProps;
        }
        LOG.warn("Failed to load configuration file from classloader: {}", (Object)propsFilename);
        return archProps;
    }

    private static boolean readPropertiesFromClassLoader(String propsFilename, Properties archProps, ClassLoader loader) {
        if (loader == null) {
            return false;
        }
        try {
            ArrayList<URL> resources = Collections.list(loader.getResources(propsFilename));
            if (resources.isEmpty()) {
                LOG.debug("No {} file found from ClassLoader {}", (Object)propsFilename, (Object)loader);
                return false;
            }
            if (resources.size() > 1) {
                byte[] propsAsBytes = FileUtil.readFileAsBytes((URL)resources.get(0));
                for (int i = 1; i < resources.size(); ++i) {
                    if (Arrays.equals(propsAsBytes, FileUtil.readFileAsBytes((URL)resources.get(i)))) continue;
                    LOG.warn("Configuration conflict: there is more than one {} file on the classpath: {}", (Object)propsFilename, (Object)resources);
                    break;
                }
            }
            try (InputStream in = ((URL)resources.get(0)).openStream();){
                if (in != null) {
                    archProps.load(in);
                }
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static byte[] readFileAsBytes(URL url) throws IOException {
        try (InputStream in = url.openStream();){
            int bytesRead;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                buf.write(data, 0, bytesRead);
            }
            buf.flush();
            byte[] byArray = buf.toByteArray();
            return byArray;
        }
    }

    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath(), new String[0])).toString();
        }
        catch (IOException e) {
            return null;
        }
    }
}

