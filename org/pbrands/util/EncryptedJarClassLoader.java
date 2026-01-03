/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedJarClassLoader
extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(EncryptedJarClassLoader.class);
    private final Map<String, byte[]> classes = new ConcurrentHashMap<String, byte[]>();
    private final Map<String, byte[]> resources = new ConcurrentHashMap<String, byte[]>();
    private final Map<String, Path> extractedNatives = new ConcurrentHashMap<String, Path>();
    private Manifest manifest;
    private final Path nativeLibDir = Files.createTempDirectory("lifebot-native-", new FileAttribute[0]);

    public EncryptedJarClassLoader(byte[] decryptedJar, ClassLoader parent) throws IOException {
        super(parent);
        this.nativeLibDir.toFile().deleteOnExit();
        this.loadJarContents(decryptedJar);
        logger.info("EncryptedJarClassLoader initialized with {} classes and {} resources", (Object)this.classes.size(), (Object)this.resources.size());
    }

    private void loadJarContents(byte[] jarBytes) throws IOException {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes));){
            JarEntry entry;
            this.manifest = jis.getManifest();
            while ((entry = jis.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || this.shouldSkipEntry(name)) continue;
                byte[] content = this.readAllBytes(jis);
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    this.classes.put(className, content);
                    continue;
                }
                this.resources.put(name, content);
                if (!this.isNativeLibrary(name)) continue;
                this.extractNativeLibrary(name, content);
            }
        }
    }

    private boolean shouldSkipEntry(String name) {
        if (name.startsWith("org/slf4j/")) {
            return true;
        }
        if (name.startsWith("ch/qos/logback/")) {
            return true;
        }
        if (name.equals("META-INF/services/org.slf4j.spi.SLF4JServiceProvider")) {
            return true;
        }
        if (name.startsWith("META-INF/services/org.slf4j")) {
            return true;
        }
        return name.startsWith("META-INF/services/ch.qos.logback");
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        int bytesRead;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    private boolean isNativeLibrary(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".dll") || lower.endsWith(".so") || lower.endsWith(".dylib") || lower.endsWith(".jnilib");
    }

    private void extractNativeLibrary(String name, byte[] content) {
        try {
            String fileName = name;
            int lastSlash = name.lastIndexOf(47);
            if (lastSlash >= 0) {
                fileName = name.substring(lastSlash + 1);
            }
            Path libPath = this.nativeLibDir.resolve(fileName);
            Files.write(libPath, content, new OpenOption[0]);
            libPath.toFile().deleteOnExit();
            this.extractedNatives.put(fileName, libPath);
            logger.debug("Extracted native library: {} -> {}", (Object)name, (Object)libPath);
        }
        catch (IOException e) {
            logger.error("Failed to extract native library: {}", (Object)name, (Object)e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Object object = this.getClassLoadingLock(name);
        synchronized (object) {
            Class<?> c = this.findLoadedClass(name);
            if (c == null) {
                if (this.classes.containsKey(name)) {
                    try {
                        c = this.findClass(name);
                    }
                    catch (ClassNotFoundException classNotFoundException) {
                        // empty catch block
                    }
                }
                if (c == null) {
                    if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("jdk.") || name.startsWith("org.slf4j.") || name.startsWith("ch.qos.logback.")) {
                        c = this.getParent().loadClass(name);
                    } else {
                        try {
                            c = this.getParent().loadClass(name);
                        }
                        catch (ClassNotFoundException e) {
                            throw new ClassNotFoundException(name);
                        }
                    }
                }
            }
            if (resolve) {
                this.resolveClass(c);
            }
            return c;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = this.classes.get(name);
        if (classBytes == null) {
            throw new ClassNotFoundException(name);
        }
        return this.defineClass(name, classBytes, 0, classBytes.length);
    }

    @Override
    protected URL findResource(String name) {
        byte[] data = this.resources.get(name);
        if (data == null && name.startsWith("/")) {
            data = this.resources.get(name.substring(1));
        }
        if (data != null) {
            try {
                final byte[] resourceData = data;
                return new URL("memory", "", -1, "/" + name, new URLStreamHandler(this){

                    @Override
                    protected URLConnection openConnection(URL u) {
                        return new URLConnection(u){

                            @Override
                            public void connect() {
                            }

                            @Override
                            public InputStream getInputStream() {
                                return new ByteArrayInputStream(resourceData);
                            }

                            @Override
                            public int getContentLength() {
                                return resourceData.length;
                            }
                        };
                    }
                });
            }
            catch (Exception e) {
                logger.error("Failed to create resource URL for: {}", (Object)name, (Object)e);
            }
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] data = this.resources.get(name);
        if (data == null && name.startsWith("/")) {
            data = this.resources.get(name.substring(1));
        }
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        return super.getResourceAsStream(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
        URL url = this.findResource(name);
        if (url != null) {
            return Collections.enumeration(Collections.singletonList(url));
        }
        return Collections.emptyEnumeration();
    }

    @Override
    protected String findLibrary(String libname) {
        String[] possibleNames;
        for (String name : possibleNames = new String[]{libname, libname + ".dll", "lib" + libname + ".so", "lib" + libname + ".dylib", libname + ".so", libname + ".dylib"}) {
            Path path = this.extractedNatives.get(name);
            if (path == null || !Files.exists(path, new LinkOption[0])) continue;
            return path.toAbsolutePath().toString();
        }
        return null;
    }

    public String getMainClass() {
        if (this.manifest != null) {
            return this.manifest.getMainAttributes().getValue("Main-Class");
        }
        return null;
    }

    public Manifest getManifest() {
        return this.manifest;
    }

    public int getClassCount() {
        return this.classes.size();
    }

    public int getResourceCount() {
        return this.resources.size();
    }

    public void cleanup() {
        for (Path path : this.extractedNatives.values()) {
            try {
                Files.deleteIfExists(path);
            }
            catch (IOException iOException) {}
        }
        try {
            Files.deleteIfExists(this.nativeLibDir);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

