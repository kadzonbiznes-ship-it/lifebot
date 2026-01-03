/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.pbrands.Startup;
import org.pbrands.model.Product;
import org.pbrands.model.StartupParams;
import org.pbrands.netty.NettyClient;
import org.pbrands.ui.main.LoaderWindow;
import org.pbrands.util.EncryptedJarClassLoader;
import org.pbrands.util.JarCrypto;
import org.pbrands.util.RegistryUtil;
import org.pbrands.util.TesseractExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapLoader {
    private static final Logger logger = LoggerFactory.getLogger(BootstrapLoader.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final LoaderWindow loaderWindow;

    public BootstrapLoader(LoaderWindow loaderWindow) {
        this.loaderWindow = loaderWindow;
    }

    public void downloadAndRunEncrypted(LoaderWindow loaderWindow, StartupParams startupParams, Product product) {
        this.executorService.submit(() -> {
            try {
                int productId = product.getId();
                logger.info("Downloading encrypted JAR for product: {}", (Object)product.getName());
                NettyClient.EncryptedProductResult result = (NettyClient.EncryptedProductResult)loaderWindow.getNettyClient().downloadEncryptedProduct(productId).get(60L, TimeUnit.SECONDS);
                logger.info("Downloaded encrypted JAR: {} bytes, key: {}...", (Object)result.encryptedJar.length, (Object)result.sessionKey.substring(0, 8));
                byte[] decryptedJar = JarCrypto.decrypt(result.encryptedJar, result.sessionKey);
                logger.info("Decrypted JAR: {} bytes", (Object)decryptedJar.length);
                this.saveToRegistry(startupParams);
                this.extractJNativeHookLibrary();
                this.runJarInMemory(decryptedJar, startupParams, loaderWindow);
            }
            catch (Exception e) {
                logger.error("B\u0142\u0105d podczas pobierania lub uruchamiania programu", e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loaderWindow, "Nie uda\u0142o si\u0119 pobra\u0107 lub uruchomi\u0107 programu.\n" + e.getMessage(), "B\u0142\u0105d", 0));
            }
        });
    }

    private void saveToRegistry(StartupParams startupParams) {
        RegistryUtil.setAuthToken(startupParams.token);
        RegistryUtil.setDeviceType(startupParams.device);
        RegistryUtil.setFolderName(startupParams.folderName);
        logger.info("Saved token, device and folderName to registry");
    }

    private void extractJNativeHookLibrary() {
        File nativeDir = new File(System.getenv("LOCALAPPDATA"), "LifeBot/native");
        nativeDir.mkdirs();
        String targetName = "JNativeHook.dll";
        File targetFile = new File(nativeDir, targetName);
        String resourcePath = "/windows/x86_64/JNativeHook.dll";
        try (InputStream is = this.getClass().getResourceAsStream(resourcePath);){
            if (is == null) {
                logger.warn("JNativeHook library not found in loader resources: {}", (Object)resourcePath);
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile);){
                int read;
                byte[] buffer = new byte[8192];
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            logger.info("Extracted JNativeHook library to: {}", (Object)targetFile.getAbsolutePath());
        }
        catch (Exception e) {
            logger.warn("Failed to extract JNativeHook library: {}", (Object)e.getMessage());
        }
    }

    private void runJarInMemory(byte[] jarBytes, StartupParams startupParams, LoaderWindow loaderWindow) {
        try {
            String[] clientArgs;
            EncryptedJarClassLoader classLoader = new EncryptedJarClassLoader(jarBytes, this.getClass().getClassLoader());
            String mainClassName = classLoader.getMainClass();
            if (mainClassName == null) {
                throw new RuntimeException("No Main-Class found in JAR manifest");
            }
            logger.info("Loading main class: {} ({} classes, {} resources)", mainClassName, classLoader.getClassCount(), classLoader.getResourceCount());
            Thread.currentThread().setContextClassLoader(classLoader);
            System.setProperty("jnativehook.lib.locator", "org.pbrands.util.JNativeHookLibraryLocator");
            logger.info("Set JNativeHook library locator");
            File lifebotDir = new File(System.getenv("LOCALAPPDATA"), "LifeBot");
            File tesseractDir = new File(lifebotDir, "tesseract");
            if (!TesseractExtractor.extractIfNeeded(tesseractDir)) {
                logger.warn("Failed to extract Tesseract resources - OCR may not work!");
            }
            if (tesseractDir.exists() && tesseractDir.isDirectory()) {
                String tesseractPath;
                String currentPath = System.getProperty("jna.library.path", "");
                if (!currentPath.contains(tesseractPath = tesseractDir.getAbsolutePath())) {
                    String newPath = currentPath.isEmpty() ? tesseractPath : tesseractPath + File.pathSeparator + currentPath;
                    System.setProperty("jna.library.path", newPath);
                    logger.info("Set jna.library.path to: {}", (Object)newPath);
                }
            } else {
                logger.warn("Tesseract folder not found: {} - OCR will not work!", (Object)tesseractDir);
            }
            Class<?> mainClass = classLoader.loadClass(mainClassName);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            loaderWindow.hideLoadingSpinner();
            Startup.setLoaded((boolean)true);
            if (startupParams.debug) {
                clientArgs = new String[]{"--dev"};
                logger.info("Invoking main method with --dev flag...");
            } else {
                clientArgs = new String[]{};
                logger.info("Invoking main method (production mode)...");
            }
            mainMethod.invoke(null, new Object[]{clientArgs});
        }
        catch (Exception e) {
            Throwable cause = e;
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                cause = e.getCause();
            }
            if (Startup.isLoaded()) {
                logger.warn("Exception after app loaded (ignoring): {}", (Object)cause.getMessage());
                return;
            }
            logger.error("Failed to run JAR in memory", cause);
            Object errorMessage = cause.getMessage();
            if (errorMessage == null || ((String)errorMessage).isEmpty()) {
                errorMessage = cause.getClass().getSimpleName();
                if (cause.getStackTrace().length > 0) {
                    StackTraceElement ste = cause.getStackTrace()[0];
                    errorMessage = (String)errorMessage + " at " + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber();
                }
            }
            String finalMessage = errorMessage;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loaderWindow, "Nie uda\u0142o si\u0119 uruchomi\u0107 programu.\n" + finalMessage, "B\u0142\u0105d", 0));
        }
    }

    public void terminateExternalProcess() {
        logger.info("Terminating application...");
        System.exit(0);
    }
}

