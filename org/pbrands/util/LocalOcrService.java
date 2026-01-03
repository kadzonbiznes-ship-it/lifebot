/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.pbrands.Startup;
import org.pbrands.util.Log;

public class LocalOcrService {
    private static final LocalOcrService INSTANCE = new LocalOcrService();
    private static final int POOL_SIZE = 2;
    private static final long ACQUIRE_TIMEOUT_MS = 500L;
    private final BlockingQueue<Tesseract> pool = new ArrayBlockingQueue<Tesseract>(2);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ExecutorService ocrExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OCR-Worker");
        t.setDaemon(true);
        return t;
    });
    private String tessDataPath;

    private LocalOcrService() {
    }

    public static LocalOcrService getInstance() {
        return INSTANCE;
    }

    public synchronized boolean initialize() {
        if (this.initialized.get()) {
            return true;
        }
        Log.info("[LocalOCR] Initializing service...");
        File tesseractDir = new File(Startup.LIFEBOT_FOLDER, "tesseract");
        File tessDataDir = new File(tesseractDir, "tessdata");
        if (!tessDataDir.exists() || !tessDataDir.isDirectory()) {
            Log.warn("[LocalOCR] Tessdata not found at {}, local OCR disabled", tessDataDir.getAbsolutePath());
            return false;
        }
        this.tessDataPath = tessDataDir.getAbsolutePath();
        Log.info("[LocalOCR] Found tessdata at: {}", this.tessDataPath);
        Log.info("[LocalOCR] jna.library.path = {}", System.getProperty("jna.library.path", "NOT SET"));
        int created = 0;
        for (int i = 0; i < 2; ++i) {
            Tesseract tesseract = this.createInstance();
            if (tesseract == null) continue;
            this.pool.offer(tesseract);
            ++created;
        }
        if (created > 0) {
            this.initialized.set(true);
            Log.info("[LocalOCR] Service initialized with {} Tesseract instances", created);
            return true;
        }
        Log.error("[LocalOCR] Failed to create any Tesseract instances");
        return false;
    }

    public boolean isAvailable() {
        return this.initialized.get() && !this.pool.isEmpty();
    }

    private Tesseract createInstance() {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(this.tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(7);
            tesseract.setVariable("tessedit_char_whitelist", "0123456789.");
            tesseract.setVariable("user_defined_dpi", "150");
            tesseract.setVariable("debug_file", System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null");
            BufferedImage testImg = new BufferedImage(10, 10, 1);
            tesseract.doOCR(testImg);
            Log.debug("[LocalOCR] Created Tesseract instance");
            return tesseract;
        }
        catch (Exception e) {
            Log.error("[LocalOCR] Failed to create Tesseract: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public double recognizeDigit(BufferedImage image) {
        if (!this.initialized.get() || image == null) {
            return -1.0;
        }
        Tesseract tesseract = null;
        try {
            tesseract = this.pool.poll(500L, TimeUnit.MILLISECONDS);
            if (tesseract == null) {
                Log.debug("[LocalOCR] Could not acquire Tesseract instance (pool busy)");
                double d = -1.0;
                return d;
            }
            String result = tesseract.doOCR(image).trim();
            if (result.isEmpty()) {
                double d = -1.0;
                return d;
            }
            if ((result = result.replaceAll("[^0-9.]", "")).isEmpty()) {
                double d = -1.0;
                return d;
            }
            double d = Double.parseDouble(result);
            return d;
        }
        catch (TesseractException e) {
            Log.debug("[LocalOCR] OCR failed: {}", e.getMessage());
            double d = -1.0;
            return d;
        }
        catch (NumberFormatException e) {
            Log.debug("[LocalOCR] Could not parse OCR result as number");
            double d = -1.0;
            return d;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            double d = -1.0;
            return d;
        }
        finally {
            if (tesseract != null) {
                this.pool.offer(tesseract);
            }
        }
    }

    public CompletableFuture<Double> recognizeDigitAsync(BufferedImage image) {
        return CompletableFuture.supplyAsync(() -> this.recognizeDigit(image), this.ocrExecutor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String recognizePolishText(BufferedImage image) {
        if (!this.initialized.get() || image == null) {
            return "";
        }
        Tesseract tesseract = null;
        try {
            tesseract = this.pool.poll(500L, TimeUnit.MILLISECONDS);
            if (tesseract == null) {
                String string = "";
                return string;
            }
            tesseract.setLanguage("pol");
            tesseract.setVariable("tessedit_char_whitelist", "");
            String result = tesseract.doOCR(image).trim();
            tesseract.setLanguage("eng");
            tesseract.setVariable("tessedit_char_whitelist", "0123456789.");
            String string = result;
            return string;
        }
        catch (Exception e) {
            Log.debug("[LocalOCR] Polish OCR failed: {}", e.getMessage());
            String string = "";
            return string;
        }
        finally {
            if (tesseract != null) {
                this.pool.offer(tesseract);
            }
        }
    }

    public void shutdown() {
        this.ocrExecutor.shutdown();
        this.pool.clear();
        this.initialized.set(false);
        Log.info("[LocalOCR] Service shut down");
    }
}

