/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 *  org.apache.commons.io.IOUtils
 *  org.jboss.vfs.VFS
 *  org.jboss.vfs.VirtualFile
 */
package net.sourceforge.tess4j.util;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.util.LoggHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadLibs {
    private static final String VFS_PROTOCOL = "vfs";
    private static final String JNA_LIBRARY_PATH = "jna.library.path";
    public static final String TESS4J_TEMP_DIR;
    public static final String LIB_NAME = "libtesseract551";
    public static final String LIB_NAME_NON_WIN = "tesseract";
    private static final Logger logger;

    public static TessAPI getTessAPIInstance() {
        return Native.load(LoadLibs.getTesseractLibName(), TessAPI.class);
    }

    public static String getTesseractLibName() {
        return Platform.isWindows() ? LIB_NAME : LIB_NAME_NON_WIN;
    }

    public static synchronized File extractTessResources(String resourceName) {
        File targetPath = null;
        try {
            targetPath = new File(TESS4J_TEMP_DIR, resourceName);
            Enumeration<URL> resources = LoadLibs.class.getClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL resourceUrl = resources.nextElement();
                LoadLibs.copyResources(resourceUrl, targetPath);
            }
        }
        catch (IOException | URISyntaxException e) {
            logger.warn(e.getMessage(), e);
        }
        return targetPath;
    }

    static void copyResources(URL resourceUrl, File targetPath) throws IOException, URISyntaxException {
        if (resourceUrl == null) {
            return;
        }
        URLConnection urlConnection = resourceUrl.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            LoadLibs.copyJarResourceToPath((JarURLConnection)urlConnection, targetPath);
        } else if (VFS_PROTOCOL.equals(resourceUrl.getProtocol())) {
            VirtualFile virtualFileOrFolder = VFS.getChild((URI)resourceUrl.toURI());
            LoadLibs.copyFromWarToFolder(virtualFileOrFolder, targetPath);
        } else {
            File file = new File(resourceUrl.getPath());
            if (file.isDirectory()) {
                for (File resourceFile : FileUtils.listFiles((File)file, null, (boolean)true)) {
                    int index = resourceFile.getPath().lastIndexOf(targetPath.getName()) + targetPath.getName().length();
                    File targetFile = new File(targetPath, resourceFile.getPath().substring(index));
                    if (targetFile.exists() && targetFile.length() == resourceFile.length() && targetFile.lastModified() == resourceFile.lastModified() || !resourceFile.isFile()) continue;
                    FileUtils.copyFile((File)resourceFile, (File)targetFile, (boolean)true);
                }
            } else if (!targetPath.exists() || targetPath.length() != file.length() || targetPath.lastModified() != file.lastModified()) {
                FileUtils.copyFile((File)file, (File)targetPath, (boolean)true);
            }
        }
    }

    static void copyJarResourceToPath(JarURLConnection jarConnection, File destPath) {
        try (JarFile jarFile = jarConnection.getJarFile();){
            String jarConnectionEntryName = jarConnection.getEntryName();
            if (!jarConnectionEntryName.endsWith("/")) {
                jarConnectionEntryName = jarConnectionEntryName + "/";
            }
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                String jarEntryName = jarEntry.getName();
                if (!jarEntryName.startsWith(jarConnectionEntryName)) continue;
                String filename = jarEntryName.substring(jarConnectionEntryName.length());
                File targetFile = new File(destPath, filename);
                if (jarEntry.isDirectory()) {
                    targetFile.mkdirs();
                    continue;
                }
                if (targetFile.exists() && targetFile.length() == jarEntry.getSize()) continue;
                InputStream is = jarFile.getInputStream(jarEntry);
                try {
                    FileOutputStream out = FileUtils.openOutputStream((File)targetFile);
                    try {
                        IOUtils.copy((InputStream)is, (OutputStream)out);
                    }
                    finally {
                        if (out == null) continue;
                        ((OutputStream)out).close();
                    }
                }
                finally {
                    if (is == null) continue;
                    is.close();
                }
            }
        }
        catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    static void copyFromWarToFolder(VirtualFile virtualFileOrFolder, File targetFolder) throws IOException {
        if (virtualFileOrFolder.isDirectory() && !virtualFileOrFolder.getName().contains(".")) {
            if (targetFolder.getName().equalsIgnoreCase(virtualFileOrFolder.getName())) {
                for (VirtualFile innerFileOrFolder : virtualFileOrFolder.getChildren()) {
                    LoadLibs.copyFromWarToFolder(innerFileOrFolder, targetFolder);
                }
            } else {
                File innerTargetFolder = new File(targetFolder, virtualFileOrFolder.getName());
                innerTargetFolder.mkdir();
                for (VirtualFile innerFileOrFolder : virtualFileOrFolder.getChildren()) {
                    LoadLibs.copyFromWarToFolder(innerFileOrFolder, innerTargetFolder);
                }
            }
        } else {
            File targetFile = new File(targetFolder, virtualFileOrFolder.getName());
            if (!targetFile.exists() || targetFile.length() != virtualFileOrFolder.getSize()) {
                FileUtils.copyURLToFile((URL)virtualFileOrFolder.asFileURL(), (File)targetFile);
            }
        }
    }

    static {
        String userCustomizedPath;
        TESS4J_TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "tess4j").getPath();
        logger = LoggerFactory.getLogger(new LoggHelper().toString());
        System.setProperty("jna.encoding", "UTF8");
        String resourcePrefix = Platform.RESOURCE_PREFIX;
        File targetTempFolder = LoadLibs.extractTessResources(resourcePrefix);
        if (targetTempFolder != null && targetTempFolder.exists()) {
            userCustomizedPath = System.getProperty(JNA_LIBRARY_PATH);
            if (null == userCustomizedPath || userCustomizedPath.isEmpty()) {
                System.setProperty(JNA_LIBRARY_PATH, targetTempFolder.getPath());
            } else {
                System.setProperty(JNA_LIBRARY_PATH, userCustomizedPath + File.pathSeparator + targetTempFolder.getPath());
            }
        }
        if (Platform.isMac() && Platform.isARM()) {
            userCustomizedPath = System.getProperty(JNA_LIBRARY_PATH);
            if (null == userCustomizedPath || userCustomizedPath.isEmpty()) {
                System.setProperty(JNA_LIBRARY_PATH, "/opt/homebrew/lib");
            } else {
                System.setProperty(JNA_LIBRARY_PATH, userCustomizedPath + File.pathSeparator + "/opt/homebrew/lib");
            }
        }
    }
}

