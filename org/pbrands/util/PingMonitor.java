/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pbrands.util.Log;

public class PingMonitor {
    private static final String PING_HOST = "51.68.143.118";
    private static final int DEFAULT_PING = 50;
    private static final int MIN_PING = 5;
    private static final int MAX_PING = 250;
    private static final int SAMPLE_COUNT = 5;
    private final AtomicInteger currentPing = new AtomicInteger(50);
    private final ScheduledExecutorService executor;
    private volatile boolean running = false;
    private final LinkedList<Integer> pingSamples = new LinkedList();
    private static final Pattern PING_PATTERN = Pattern.compile("time[=<](\\d+)ms");

    public PingMonitor() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PingMonitor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(int intervalSeconds) {
        if (this.running) {
            return;
        }
        this.running = true;
        this.executor.submit(this::measurePing);
        this.executor.scheduleAtFixedRate(this::measurePing, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        Log.info("PingMonitor started, checking every {}s (averaging {} samples)", intervalSeconds, 5);
    }

    public void stop() {
        this.running = false;
        this.executor.shutdownNow();
        Log.info("PingMonitor stopped");
    }

    public int getPing() {
        return this.currentPing.get();
    }

    public int getHalfPing() {
        return this.currentPing.get() / 2;
    }

    private void measurePing() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ping", "-n", "1", "-w", "1000", PING_HOST);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));){
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PING_PATTERN.matcher(line);
                    if (!matcher.find()) continue;
                    int ping = Integer.parseInt(matcher.group(1));
                    if (ping >= 5 && ping <= 250) {
                        this.updateRollingAverage(ping);
                    }
                    break;
                }
            }
            process.waitFor(2L, TimeUnit.SECONDS);
            process.destroyForcibly();
        }
        catch (Exception e) {
            Log.warn("Failed to measure ping: {}", e.getMessage());
        }
    }

    private synchronized void updateRollingAverage(int newPing) {
        this.pingSamples.addLast(newPing);
        while (this.pingSamples.size() > 5) {
            this.pingSamples.removeFirst();
        }
        int sum = 0;
        Iterator iterator = this.pingSamples.iterator();
        while (iterator.hasNext()) {
            int sample = (Integer)iterator.next();
            sum += sample;
        }
        int average = sum / this.pingSamples.size();
        int oldPing = this.currentPing.getAndSet(average);
        if (Math.abs(oldPing - average) > 15) {
            Log.debug("Ping average: {}ms (samples: {})", average, this.pingSamples.size());
        }
    }
}

