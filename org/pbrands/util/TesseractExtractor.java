/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseractExtractor {
    private static final Logger logger = LoggerFactory.getLogger(TesseractExtractor.class);
    private static final String VERSION_MARKER = ".tesseract_version";
    private static final String CURRENT_VERSION = "5.5.1-lept1860";

    public static boolean extractIfNeeded(File targetDir) {
        try {
            File versionFile = new File(targetDir, VERSION_MARKER);
            if (versionFile.exists()) {
                String existingVersion = Files.readString(versionFile.toPath()).trim();
                if (CURRENT_VERSION.equals(existingVersion)) {
                    logger.debug("Tesseract resources already extracted (version {})", (Object)CURRENT_VERSION);
                    return true;
                }
                logger.info("Tesseract version mismatch ({} vs {}), re-extracting...", (Object)existingVersion, (Object)CURRENT_VERSION);
            }
            targetDir.mkdirs();
            File tessDataDir = new File(targetDir, "tessdata");
            tessDataDir.mkdirs();
            int extractedCount = 0;
            extractedCount += TesseractExtractor.extractResourceDir("win32-x86-64", targetDir);
            Files.writeString(versionFile.toPath(), (CharSequence)CURRENT_VERSION, new OpenOption[0]);
            logger.info("Extracted {} Tesseract resources to {}", (Object)(extractedCount += TesseractExtractor.extractResourceDir("tessdata", tessDataDir)), (Object)targetDir.getAbsolutePath());
            return true;
        }
        catch (Exception e) {
            logger.error("Failed to extract Tesseract resources: {}", (Object)e.getMessage(), (Object)e);
            return false;
        }
    }

    private static int extractResourceDir(String resourcePath, File targetDir) throws IOException {
        int count = 0;
        Enumeration<URL> resources = TesseractExtractor.class.getClassLoader().getResources(resourcePath);
        while (resources.hasMoreElements()) {
            URL resourceUrl = resources.nextElement();
            logger.debug("Found resource: {}", (Object)resourceUrl);
            if (resourceUrl.getProtocol().equals("jar")) {
                count += TesseractExtractor.extractFromJar(resourceUrl, resourcePath, targetDir);
                continue;
            }
            if (!resourceUrl.getProtocol().equals("file")) continue;
            count += TesseractExtractor.extractFromFileSystem(resourceUrl, targetDir);
        }
        return count;
    }

    private static int extractFromJar(URL jarUrl, String resourcePath, File targetDir) throws IOException {
        int count = 0;
        JarURLConnection jarConnection = (JarURLConnection)jarUrl.openConnection();
        JarFile jarFile = jarConnection.getJarFile();
        Object prefix = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            String relativePath;
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.startsWith((String)prefix) || entry.isDirectory() || (relativePath = entryName.substring(((String)prefix).length())).isEmpty()) continue;
            File targetFile = new File(targetDir, relativePath);
            if (targetFile.exists() && targetFile.length() == entry.getSize()) {
                logger.debug("Skipping {} (already exists)", (Object)relativePath);
                continue;
            }
            targetFile.getParentFile().mkdirs();
            try (InputStream is = jarFile.getInputStream(entry);
                 FileOutputStream os = new FileOutputStream(targetFile);){
                is.transferTo(os);
            }
            logger.debug("Extracted: {}", (Object)relativePath);
            ++count;
        }
        return count;
    }

    private static int extractFromFileSystem(URL fileUrl, File targetDir) throws IOException {
        int count = 0;
        File sourceDir = new File(fileUrl.getPath());
        if (sourceDir.isDirectory()) {
            for (File file : sourceDir.listFiles()) {
                if (file.isFile()) {
                    File targetFile = new File(targetDir, file.getName());
                    if (targetFile.exists() && targetFile.length() == file.length()) continue;
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Copied: {}", (Object)file.getName());
                    ++count;
                    continue;
                }
                if (!file.isDirectory()) continue;
                File subTargetDir = new File(targetDir, file.getName());
                subTargetDir.mkdirs();
                count += TesseractExtractor.extractFromFileSystem(file.toURI().toURL(), subTargetDir);
            }
        }
        return count;
    }
}

