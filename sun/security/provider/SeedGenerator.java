/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import sun.security.provider.FileInputStreamPool;
import sun.security.provider.NativeSeedGenerator;
import sun.security.provider.SunEntries;
import sun.security.util.Debug;

abstract class SeedGenerator {
    private static SeedGenerator instance;
    private static final Debug debug;

    SeedGenerator() {
    }

    public static void generateSeed(byte[] result) {
        instance.getSeedBytes(result);
    }

    abstract void getSeedBytes(byte[] var1);

    static byte[] getSystemEntropy() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("internal error: SHA-1 not available.", nsae);
        }
        byte b = (byte)System.currentTimeMillis();
        md.update(b);
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                try {
                    Properties p = System.getProperties();
                    for (String s : p.stringPropertyNames()) {
                        md.update(s.getBytes());
                        md.update(p.getProperty(s).getBytes());
                    }
                    SeedGenerator.addNetworkAdapterInfo(md);
                    File f = new File(p.getProperty("java.io.tmpdir"));
                    int count = 0;
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(f.toPath());){
                        Random r = new Random();
                        for (Path entry : stream) {
                            if (count < 512 || r.nextBoolean()) {
                                md.update(entry.getFileName().toString().getBytes());
                            }
                            if (count++ <= 1024) continue;
                            break;
                        }
                    }
                }
                catch (Exception ex) {
                    md.update((byte)ex.hashCode());
                }
                Runtime rt = Runtime.getRuntime();
                byte[] memBytes = SeedGenerator.longToByteArray(rt.totalMemory());
                md.update(memBytes, 0, memBytes.length);
                memBytes = SeedGenerator.longToByteArray(rt.freeMemory());
                md.update(memBytes, 0, memBytes.length);
                return null;
            }
        });
        return md.digest();
    }

    private static void addNetworkAdapterInfo(MessageDigest md) {
        try {
            Enumeration<NetworkInterface> ifcs = NetworkInterface.getNetworkInterfaces();
            while (ifcs.hasMoreElements()) {
                byte[] bs;
                NetworkInterface ifc = ifcs.nextElement();
                md.update(ifc.toString().getBytes());
                if (ifc.isVirtual() || (bs = ifc.getHardwareAddress()) == null) continue;
                md.update(bs);
                break;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static byte[] longToByteArray(long l) {
        byte[] retVal = new byte[8];
        for (int i = 0; i < 8; ++i) {
            retVal[i] = (byte)l;
            l >>= 8;
        }
        return retVal;
    }

    static {
        block11: {
            debug = Debug.getInstance("provider");
            String egdSource = SunEntries.getSeedSource();
            if (egdSource.equals("file:/dev/random") || egdSource.equals("file:/dev/urandom")) {
                try {
                    instance = new NativeSeedGenerator(egdSource);
                    if (debug != null) {
                        debug.println("Using operating system seed generator" + egdSource);
                    }
                    break block11;
                }
                catch (IOException e) {
                    if (debug != null) {
                        debug.println("Failed to use operating system seed generator: " + e.toString());
                    }
                    break block11;
                }
            }
            if (!egdSource.isEmpty()) {
                try {
                    instance = new URLSeedGenerator(egdSource);
                    if (debug != null) {
                        debug.println("Using URL seed generator reading from " + egdSource);
                    }
                }
                catch (IOException e) {
                    if (debug == null) break block11;
                    debug.println("Failed to create seed generator with " + egdSource + ": " + e.toString());
                }
            }
        }
        if (instance == null) {
            if (debug != null) {
                debug.println("Using default threaded seed generator");
            }
            instance = new ThreadedSeedGenerator();
        }
    }

    static class URLSeedGenerator
    extends SeedGenerator {
        private final String deviceName;
        private InputStream seedStream;

        URLSeedGenerator(String egdurl) throws IOException {
            if (egdurl == null) {
                throw new IOException("No random source specified");
            }
            this.deviceName = egdurl;
            this.init();
        }

        private void init() throws IOException {
            final URL device = new URL(this.deviceName);
            try {
                this.seedStream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>(this){

                    @Override
                    public InputStream run() throws IOException {
                        if (device.getProtocol().equalsIgnoreCase("file")) {
                            File deviceFile = SunEntries.getDeviceFile(device);
                            return FileInputStreamPool.getInputStream(deviceFile);
                        }
                        return device.openStream();
                    }
                });
            }
            catch (Exception e) {
                throw new IOException("Failed to open " + this.deviceName, e.getCause());
            }
        }

        @Override
        void getSeedBytes(byte[] result) {
            int len = result.length;
            try {
                int count;
                for (int read = 0; read < len; read += count) {
                    count = this.seedStream.read(result, read, len - read);
                    if (count >= 0) continue;
                    throw new InternalError("URLSeedGenerator " + this.deviceName + " reached end of file");
                }
            }
            catch (IOException ioe) {
                throw new InternalError("URLSeedGenerator " + this.deviceName + " generated exception: " + ioe.getMessage(), ioe);
            }
        }
    }

    private static class ThreadedSeedGenerator
    extends SeedGenerator
    implements Runnable {
        private final byte[] pool = new byte[20];
        private int start = 0;
        private int end = 0;
        private int count;
        ThreadGroup seedGroup;
        private static final byte[] rndTab = new byte[]{56, 30, -107, -6, -86, 25, -83, 75, -12, -64, 5, -128, 78, 21, 16, 32, 70, -81, 37, -51, -43, -46, -108, 87, 29, 17, -55, 22, -11, -111, -115, 84, -100, 108, -45, -15, -98, 72, -33, -28, 31, -52, -37, -117, -97, -27, 93, -123, 47, 126, -80, -62, -93, -79, 61, -96, -65, -5, -47, -119, 14, 89, 81, -118, -88, 20, 67, -126, -113, 60, -102, 55, 110, 28, 85, 121, 122, -58, 2, 45, 43, 24, -9, 103, -13, 102, -68, -54, -101, -104, 19, 13, -39, -26, -103, 62, 77, 51, 44, 111, 73, 18, -127, -82, 4, -30, 11, -99, -74, 40, -89, 42, -76, -77, -94, -35, -69, 35, 120, 76, 33, -73, -7, 82, -25, -10, 88, 125, -112, 58, 83, 95, 6, 10, 98, -34, 80, 15, -91, 86, -19, 52, -17, 117, 49, -63, 118, -90, 36, -116, -40, -71, 97, -53, -109, -85, 109, -16, -3, 104, -95, 68, 54, 34, 26, 114, -1, 106, -121, 3, 66, 0, 100, -84, 57, 107, 119, -42, 112, -61, 1, 48, 38, 12, -56, -57, 39, -106, -72, 41, 7, 71, -29, -59, -8, -38, 79, -31, 124, -124, 8, 91, 116, 99, -4, 9, -36, -78, 63, -49, -67, -87, 59, 101, -32, 92, 94, 53, -41, 115, -66, -70, -122, 50, -50, -22, -20, -18, -21, 23, -2, -48, 96, 65, -105, 123, -14, -110, 69, -24, -120, -75, 74, 127, -60, 113, 90, -114, 105, 46, 27, -125, -23, -44, 64};

        ThreadedSeedGenerator() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException e) {
                throw new InternalError("internal error: SHA-1 not available.", e);
            }
            final ThreadGroup[] finalsg = new ThreadGroup[1];
            Thread t = AccessController.doPrivileged(new PrivilegedAction<Thread>(this){
                final /* synthetic */ ThreadedSeedGenerator this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Thread run() {
                    ThreadGroup parent;
                    ThreadGroup group = Thread.currentThread().getThreadGroup();
                    while ((parent = group.getParent()) != null) {
                        group = parent;
                    }
                    finalsg[0] = new ThreadGroup(group, "SeedGenerator ThreadGroup");
                    Thread newT = new Thread(finalsg[0], this.this$0, "SeedGenerator Thread", 0L, false);
                    newT.setPriority(1);
                    newT.setDaemon(true);
                    return newT;
                }
            });
            this.seedGroup = finalsg[0];
            t.start();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         * Converted monitor instructions to comments
         * Lifted jumps to return sites
         */
        @Override
        public final void run() {
            try {
                while (true) {
                    int latch;
                    ThreadedSeedGenerator threadedSeedGenerator = this;
                    // MONITORENTER : threadedSeedGenerator
                    while (this.count >= this.pool.length) {
                        this.wait();
                    }
                    // MONITOREXIT : threadedSeedGenerator
                    byte v = 0;
                    int quanta = 0;
                    for (int counter = 0; counter < 64000 && quanta < 6; counter += latch, ++quanta) {
                        try {
                            BogusThread bt = new BogusThread();
                            Thread t = new Thread(this.seedGroup, bt, "SeedGenerator Thread", 0L, false);
                            t.start();
                        }
                        catch (Exception e) {
                            throw new InternalError("internal error: SeedGenerator thread creation error.", e);
                        }
                        latch = 0;
                        long startTime = System.nanoTime();
                        while (System.nanoTime() - startTime < 250000000L) {
                            ThreadedSeedGenerator threadedSeedGenerator2 = this;
                            // MONITORENTER : threadedSeedGenerator2
                            // MONITOREXIT : threadedSeedGenerator2
                            latch = latch + 1 & 0x1FFFFFFF;
                        }
                        v = (byte)(v ^ rndTab[latch % 255]);
                    }
                    ThreadedSeedGenerator threadedSeedGenerator3 = this;
                    // MONITORENTER : threadedSeedGenerator3
                    this.pool[this.end] = v;
                    ++this.end;
                    ++this.count;
                    if (this.end >= this.pool.length) {
                        this.end = 0;
                    }
                    this.notifyAll();
                    // MONITOREXIT : threadedSeedGenerator3
                }
            }
            catch (Exception e) {
                throw new InternalError("internal error: SeedGenerator thread generated an exception.", e);
            }
        }

        @Override
        void getSeedBytes(byte[] result) {
            for (int i = 0; i < result.length; ++i) {
                result[i] = this.getSeedByte();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        byte getSeedByte() {
            byte b;
            ThreadedSeedGenerator threadedSeedGenerator;
            block10: {
                try {
                    threadedSeedGenerator = this;
                    synchronized (threadedSeedGenerator) {
                        while (this.count <= 0) {
                            this.wait();
                        }
                    }
                }
                catch (Exception e) {
                    if (this.count > 0) break block10;
                    throw new InternalError("internal error: SeedGenerator thread generated an exception.", e);
                }
            }
            threadedSeedGenerator = this;
            synchronized (threadedSeedGenerator) {
                b = this.pool[this.start];
                this.pool[this.start] = 0;
                ++this.start;
                --this.count;
                if (this.start == this.pool.length) {
                    this.start = 0;
                }
                this.notifyAll();
            }
            return b;
        }

        private static class BogusThread
        implements Runnable {
            private BogusThread() {
            }

            @Override
            public final void run() {
                try {
                    for (int i = 0; i < 5; ++i) {
                        Thread.sleep(50L);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }
}

