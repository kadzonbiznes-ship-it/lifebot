/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.TimeUnit;
import org.pbrands.Startup;
import org.pbrands.netty.NettyClient;
import org.pbrands.util.Log;

public class NativeLibraryLoader {
    public static final String CAPTURE_LIB = "d3drender.dll";
    private static final String CAPTURE_LIB_SERVER = "WindowCapture.dll";
    private static Path nativeDir;
    private static boolean initialized;
    private static NettyClient nettyClient;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            nativeDir = Startup.LIFEBOT_FOLDER.toPath().resolve("native");
            Files.createDirectories(nativeDir, new FileAttribute[0]);
            String libPath = System.getProperty("java.library.path");
            String nativePath = nativeDir.toAbsolutePath().toString();
            if (libPath == null || libPath.isEmpty()) {
                System.setProperty("java.library.path", nativePath);
            } else if (!libPath.contains(nativePath)) {
                System.setProperty("java.library.path", nativePath + File.pathSeparator + libPath);
            }
            initialized = true;
            Log.info("Native library directory initialized: {}", nativeDir);
        }
        catch (IOException e) {
            Log.error("Failed to initialize native library directory", e);
        }
    }

    public static void setNettyClient(NettyClient client) {
        nettyClient = client;
    }

    public static Path ensureLibrary(String localName, String serverName) {
        NativeLibraryLoader.initialize();
        Path libPath = nativeDir.resolve(localName);
        if (Files.exists(libPath, new LinkOption[0])) {
            Log.debug("Native library already exists: {}", libPath);
            return libPath;
        }
        if (nettyClient == null) {
            Log.error("NettyClient not set, cannot download native library: {}", serverName);
            return null;
        }
        try {
            NativeLibraryLoader.downloadLibrary(serverName, libPath);
            return libPath;
        }
        catch (Exception e) {
            Log.error("Failed to download native library: {}", serverName, e);
            return null;
        }
    }

    public static Path ensureCaptureLibrary() {
        return NativeLibraryLoader.ensureLibrary(CAPTURE_LIB, CAPTURE_LIB_SERVER);
    }

    private static void downloadLibrary(String serverName, Path destination) throws Exception {
        Log.info("Downloading native library via Netty: {}", serverName);
        byte[] bytes = (byte[])nettyClient.downloadNative(serverName).get(30L, TimeUnit.SECONDS);
        if (bytes == null || bytes.length == 0) {
            throw new IOException("Server returned empty or null response for: " + serverName);
        }
        Path tempFile = destination.resolveSibling(String.valueOf(destination.getFileName()) + ".tmp");
        Files.write(tempFile, bytes, new OpenOption[0]);
        Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        Log.info("Downloaded native library to: {} ({} bytes)", destination, bytes.length);
    }

    public static Path getNativeDir() {
        NativeLibraryLoader.initialize();
        return nativeDir;
    }

    public static String getLibraryPath(String libName) {
        NativeLibraryLoader.initialize();
        return nativeDir.resolve(libName).toAbsolutePath().toString();
    }

    static {
        initialized = false;
    }
}

