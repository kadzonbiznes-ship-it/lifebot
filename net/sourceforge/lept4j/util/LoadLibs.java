/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.Leptonica
 *  org.apache.commons.io.FileUtils
 *  org.apache.commons.io.IOUtils
 */
package net.sourceforge.lept4j.util;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.lept4j.Leptonica;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class LoadLibs {
    private static final String JNA_LIBRARY_PATH = "jna.library.path";
    public static final String LEPT4J_TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "lept4j").getPath();
    public static final String LIB_NAME = "libleptonica1860";
    public static final String LIB_NAME_NON_WIN = "leptonica";
    private static final Logger logger = Logger.getLogger(LoadLibs.class.getName());

    public static Leptonica getLeptonicaInstance() {
        return Native.load(LoadLibs.getLeptonicaLibName(), Leptonica.class);
    }

    public static String getLeptonicaLibName() {
        return Platform.isWindows() ? LIB_NAME : LIB_NAME_NON_WIN;
    }

    public static File extractNativeResources(String string) {
        File file = null;
        try {
            file = new File(LEPT4J_TEMP_DIR, string);
            URL uRL = LoadLibs.class.getResource(string.startsWith("/") ? string : "/" + string);
            if (uRL == null) {
                return null;
            }
            URLConnection uRLConnection = uRL.openConnection();
            if (uRLConnection instanceof JarURLConnection) {
                LoadLibs.copyJarResourceToDirectory((JarURLConnection)uRLConnection, file);
            } else {
                FileUtils.copyDirectory((File)new File(uRL.getPath()), (File)file);
            }
        }
        catch (Exception exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
        }
        return file;
    }

    static void copyJarResourceToDirectory(JarURLConnection jarURLConnection, File file) {
        try {
            JarFile jarFile = jarURLConnection.getJarFile();
            String string = jarURLConnection.getEntryName();
            if (!string.endsWith("/")) {
                string = string + "/";
            }
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();
                String string2 = jarEntry.getName();
                if (!string2.startsWith(string)) continue;
                String string3 = string2.substring(string.length());
                File file2 = new File(file, string3);
                if (jarEntry.isDirectory()) {
                    file2.mkdirs();
                    continue;
                }
                if (file2.exists() && file2.length() == jarEntry.getSize()) continue;
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                Throwable throwable = null;
                try {
                    FileOutputStream fileOutputStream = FileUtils.openOutputStream((File)file2);
                    Throwable throwable2 = null;
                    try {
                        IOUtils.copy((InputStream)inputStream, (OutputStream)fileOutputStream);
                    }
                    catch (Throwable throwable3) {
                        throwable2 = throwable3;
                        throw throwable3;
                    }
                    finally {
                        if (fileOutputStream == null) continue;
                        if (throwable2 != null) {
                            try {
                                ((OutputStream)fileOutputStream).close();
                            }
                            catch (Throwable throwable4) {
                                throwable2.addSuppressed(throwable4);
                            }
                            continue;
                        }
                        ((OutputStream)fileOutputStream).close();
                    }
                }
                catch (Throwable throwable5) {
                    throwable = throwable5;
                    throw throwable5;
                }
                finally {
                    if (inputStream == null) continue;
                    if (throwable != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable6) {
                            throwable.addSuppressed(throwable6);
                        }
                        continue;
                    }
                    inputStream.close();
                }
            }
        }
        catch (IOException iOException) {
            logger.log(Level.WARNING, iOException.getMessage(), iOException);
        }
    }

    static {
        System.setProperty("jna.encoding", "UTF8");
        String string = Platform.RESOURCE_PREFIX;
        File file = LoadLibs.extractNativeResources(string);
        if (file != null && file.exists()) {
            String string2 = System.getProperty(JNA_LIBRARY_PATH);
            if (null == string2 || string2.isEmpty()) {
                System.setProperty(JNA_LIBRARY_PATH, file.getPath());
            } else {
                System.setProperty(JNA_LIBRARY_PATH, string2 + File.pathSeparator + file.getPath());
            }
        }
    }
}

