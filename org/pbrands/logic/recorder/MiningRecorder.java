/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.logic.recorder.MiningRecorder$LearningData
 *  org.pbrands.logic.recorder.MiningRecorder$MiningSession
 */
package org.pbrands.logic.recorder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import lombok.Generated;
import org.pbrands.logic.recorder.MiningRecorder;
import org.pbrands.util.Log;
import org.pbrands.util.ObfuscatedStorage;

public class MiningRecorder {
    private static final String LEARNING_FILE_NAME = "mining_learning.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File dataFolder = null;
    private final List<MiningSession> sessions = new ArrayList<MiningSession>();
    private MiningSession currentSession;
    private long lastEventTime = 0L;
    private long keyPressTime = 0L;
    private static final int REQUIRED_SAMPLES = 50;
    private final Random random = new Random();
    private double[] delayMeans;
    private double[] delayStdDevs;
    private double[] durationMeans;
    private double[] durationStdDevs;
    private double startDelayMean;
    private double startDelayStdDev;
    private boolean trained = false;
    private volatile long miningFinishedTime = 0L;
    private volatile boolean waitingForMiningFinish = false;

    public static void initialize(File folder) {
        dataFolder = folder;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        Log.info("MiningRecorder initialized with folder: {}", dataFolder.getAbsolutePath());
    }

    private static File getLearningFile() {
        if (dataFolder == null) {
            return new File(LEARNING_FILE_NAME);
        }
        return new File(dataFolder, LEARNING_FILE_NAME);
    }

    public boolean isLearningActive() {
        return this.currentSession != null;
    }

    public void startWaitingForMiningFinish() {
        this.waitingForMiningFinish = true;
        this.miningFinishedTime = 0L;
        Log.debug("Started waiting for mining to finish");
    }

    public void onMiningFinished() {
        if (this.waitingForMiningFinish) {
            this.miningFinishedTime = System.currentTimeMillis();
            this.waitingForMiningFinish = false;
            Log.debug("Mining finished at {}", this.miningFinishedTime);
        }
    }

    public void onAltPressed(boolean miningStillInProgress) {
        long now = System.currentTimeMillis();
        if (this.currentSession != null && this.currentSession.delays.isEmpty() && now - this.lastEventTime < 500L) {
            return;
        }
        this.currentSession = new MiningSession();
        if (miningStillInProgress) {
            this.currentSession.startDelay = 0L;
            Log.debug("Mining still in progress - cannot measure reaction time");
        } else if (this.miningFinishedTime > 0L) {
            long startDelay = now - this.miningFinishedTime;
            if (startDelay <= 1500L) {
                this.currentSession.startDelay = startDelay;
                Log.info("Recorded reaction time: {}ms", startDelay);
            } else {
                this.currentSession.startDelay = 0L;
                Log.debug("Filtered reaction time: {}ms (> 1500ms = break)", startDelay);
            }
        } else {
            this.currentSession.startDelay = 0L;
            Log.debug("No miningFinishedTime recorded");
        }
        this.miningFinishedTime = 0L;
        this.lastEventTime = now;
    }

    public void onKeyPressed(int keyCode, long timestamp) {
        if (this.currentSession == null) {
            return;
        }
        long delay = timestamp - this.lastEventTime;
        this.currentSession.delays.add(delay);
    }

    public int getCurrentStepIndex() {
        return this.currentSession != null && !this.currentSession.delays.isEmpty() ? this.currentSession.delays.size() - 1 : -1;
    }

    public void updateLastEventTime(long timestamp) {
        this.lastEventTime = timestamp;
    }

    public boolean completeStep(boolean isCorrect, long duration) {
        if (this.currentSession == null) {
            return false;
        }
        this.currentSession.pressDurations.add(duration);
        if (!isCorrect) {
            this.currentSession = null;
            return false;
        }
        if (this.currentSession.delays.size() >= 3 && this.currentSession.pressDurations.size() >= 3) {
            this.sessions.add(this.currentSession);
            this.logSession(this.currentSession);
            this.startWaitingForMiningFinish();
            this.currentSession = null;
            if (this.sessions.size() >= 50 && !this.trained) {
                this.train();
            } else {
                this.save();
            }
            return true;
        }
        return false;
    }

    private void logSession(MiningSession session) {
        try (FileWriter fw = new FileWriter("mining_recorder_data.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);){
            out.println("Recorded Session [" + String.valueOf(new Date()) + "]");
            out.println("1. Time from Mining Finish to Next Start (ALT/LPM): " + session.startDelay + "ms");
            if (session.delays.size() > 0) {
                out.println("2. Time from Start (ALT/LPM) to 1st Key Press: " + String.valueOf(session.delays.get(0)) + "ms (Duration: " + String.valueOf(session.pressDurations.get(0)) + "ms)");
            }
            if (session.delays.size() > 1) {
                out.println("3. Time from 1st Key Release to 2nd Key Press: " + String.valueOf(session.delays.get(1)) + "ms (Duration: " + String.valueOf(session.pressDurations.get(1)) + "ms)");
            }
            if (session.delays.size() > 2) {
                out.println("4. Time from 2nd Key Release to 3rd Key Press: " + String.valueOf(session.delays.get(2)) + "ms (Duration: " + String.valueOf(session.pressDurations.get(2)) + "ms)");
            }
            out.println("--------------------------------------------------");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return this.trained;
    }

    public void reset() {
        this.sessions.clear();
        this.currentSession = null;
        this.lastEventTime = 0L;
        this.keyPressTime = 0L;
        this.miningFinishedTime = 0L;
        this.waitingForMiningFinish = false;
        this.trained = false;
        this.delayMeans = null;
        this.delayStdDevs = null;
        this.durationMeans = null;
        this.durationStdDevs = null;
        this.startDelayMean = 0.0;
        this.startDelayStdDev = 0.0;
        File learningFile = MiningRecorder.getLearningFile();
        if (learningFile.exists()) {
            if (learningFile.delete()) {
                Log.info("Learning file deleted: {}", learningFile.getAbsolutePath());
            } else {
                Log.warn("Failed to delete learning file: {}", learningFile.getAbsolutePath());
            }
        }
        Log.info("MiningRecorder fully reset - all learning data cleared");
    }

    public boolean hasActiveSession() {
        return this.currentSession != null;
    }

    public int getRequiredSamples() {
        return 50;
    }

    public int getCollectedSamples() {
        return this.sessions.size();
    }

    private void train() {
        this.delayMeans = new double[3];
        this.delayStdDevs = new double[3];
        this.durationMeans = new double[3];
        this.durationStdDevs = new double[3];
        ArrayList<Long> startDelays = new ArrayList<Long>();
        for (int i = 0; i < 3; ++i) {
            ArrayList stepDelays = new ArrayList();
            ArrayList<Long> stepDurations = new ArrayList<Long>();
            for (MiningSession miningSession : this.sessions) {
                if (miningSession.delays.size() > i) {
                    stepDelays.add((Long)miningSession.delays.get(i));
                }
                if (miningSession.pressDurations.size() > i) {
                    stepDurations.add((Long)miningSession.pressDurations.get(i));
                }
                if (i != 0 || miningSession.startDelay <= 0L) continue;
                startDelays.add(miningSession.startDelay);
            }
            this.calculateStats(stepDelays, i, this.delayMeans, this.delayStdDevs);
            this.calculateStats(stepDurations, i, this.durationMeans, this.durationStdDevs);
        }
        int filteredCount = 0;
        for (MiningSession s : this.sessions) {
            if (s.startDelay != 0L) continue;
            ++filteredCount;
        }
        if (!startDelays.isEmpty()) {
            List<Long> filteredReactions = this.filterOutliers(startDelays);
            if (filteredReactions.isEmpty()) {
                filteredReactions = startDelays;
            }
            double sum = 0.0;
            for (long v : filteredReactions) {
                sum += (double)v;
            }
            this.startDelayMean = sum / (double)filteredReactions.size();
            double d = 0.0;
            for (long v : filteredReactions) {
                d += Math.pow((double)v - this.startDelayMean, 2.0);
            }
            this.startDelayStdDev = Math.sqrt(d / (double)filteredReactions.size());
            Log.info("Reaction time stats: mean={}ms, stdDev={}ms (from {} samples, {} filtered as 0ms, {} outliers removed)", (int)this.startDelayMean, (int)this.startDelayStdDev, filteredReactions.size(), filteredCount, startDelays.size() - filteredReactions.size());
        } else {
            this.startDelayMean = 500.0;
            this.startDelayStdDev = 200.0;
            Log.warn("No valid reaction times recorded (all {} filtered). Using defaults: mean=500ms, stdDev=200ms", filteredCount);
        }
        this.trained = true;
        this.currentSession = null;
        this.save();
    }

    private void calculateStats(List<Long> values, int index, double[] means, double[] stdDevs) {
        if (values.isEmpty()) {
            return;
        }
        List<Long> filtered = this.filterOutliers(values);
        if (filtered.isEmpty()) {
            filtered = values;
        }
        double sum = 0.0;
        for (long v : filtered) {
            sum += (double)v;
        }
        double mean = sum / (double)filtered.size();
        double sumSqDiff = 0.0;
        for (long v : filtered) {
            sumSqDiff += Math.pow((double)v - mean, 2.0);
        }
        double stdDev = Math.sqrt(sumSqDiff / (double)filtered.size());
        means[index] = mean;
        stdDevs[index] = stdDev;
        if (filtered.size() < values.size()) {
            Log.info("Delay[{}]: filtered {} outliers, using {} samples. Mean={}ms, StdDev={}ms", index, values.size() - filtered.size(), filtered.size(), (int)mean, (int)stdDev);
        }
    }

    private List<Long> filterOutliers(List<Long> values) {
        if (values.size() < 4) {
            return values;
        }
        ArrayList<Long> sorted = new ArrayList<Long>(values);
        sorted.sort(Long::compareTo);
        int n = sorted.size();
        double q1 = ((Long)sorted.get(n / 4)).longValue();
        double q3 = ((Long)sorted.get(3 * n / 4)).longValue();
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;
        lowerBound = Math.max(lowerBound, 50.0);
        upperBound = Math.min(upperBound, 2000.0);
        ArrayList<Long> filtered = new ArrayList<Long>();
        for (long v : values) {
            if (!((double)v >= lowerBound) || !((double)v <= upperBound)) continue;
            filtered.add(v);
        }
        return filtered;
    }

    public MiningSession generateSession() {
        if (!this.trained) {
            if (!this.sessions.isEmpty()) {
                return this.sessions.get(this.random.nextInt(this.sessions.size()));
            }
            return null;
        }
        MiningSession session = new MiningSession();
        long startDelay = (long)(this.random.nextGaussian() * this.startDelayStdDev + this.startDelayMean);
        session.startDelay = Math.max(100L, startDelay);
        Log.debug("Generated startDelay: {}ms (mean={}, stdDev={})", session.startDelay, (int)this.startDelayMean, (int)this.startDelayStdDev);
        for (int i = 0; i < 3; ++i) {
            long delay = (long)(this.random.nextGaussian() * this.delayStdDevs[i] + this.delayMeans[i]);
            long duration = (long)(this.random.nextGaussian() * this.durationStdDevs[i] + this.durationMeans[i]);
            delay = Math.max(20L, delay);
            duration = Math.max(20L, duration);
            session.delays.add(delay);
            session.pressDurations.add(duration);
        }
        return session;
    }

    public void save() {
        File file = ObfuscatedStorage.isInitialized() ? ObfuscatedStorage.getObfuscatedFile(dataFolder, "profile.dat") : MiningRecorder.getLearningFile();
        try {
            LearningData data = new LearningData();
            data.sessions = new ArrayList<MiningSession>(this.sessions);
            data.delayMeans = this.delayMeans;
            data.delayStdDevs = this.delayStdDevs;
            data.durationMeans = this.durationMeans;
            data.durationStdDevs = this.durationStdDevs;
            data.startDelayMean = this.startDelayMean;
            data.startDelayStdDev = this.startDelayStdDev;
            data.trained = this.trained;
            String json = GSON.toJson(data);
            if (ObfuscatedStorage.isInitialized()) {
                ObfuscatedStorage.writeEncrypted(file, json);
            } else {
                try (FileWriter writer = new FileWriter(file);){
                    writer.write(json);
                }
            }
            Log.info("Learning data saved to {} ({} sessions, trained={})", file.getAbsolutePath(), this.sessions.size(), this.trained);
        }
        catch (IOException e) {
            Log.error("Failed to save learning data to {}", file.getAbsolutePath(), e);
        }
    }

    public void load() {
        LearningData data;
        File encryptedFile = ObfuscatedStorage.isInitialized() ? ObfuscatedStorage.getObfuscatedFile(dataFolder, "profile.dat") : null;
        File legacyFile = MiningRecorder.getLearningFile();
        if (encryptedFile != null && encryptedFile.exists()) {
            try {
                String json = ObfuscatedStorage.readEncrypted(encryptedFile);
                if (json != null && (data = GSON.fromJson(json, LearningData.class)) != null) {
                    this.loadFromData(data);
                    Log.info("Learning data loaded from encrypted {} ({} sessions, trained={})", encryptedFile.getAbsolutePath(), this.sessions.size(), this.trained);
                    return;
                }
            }
            catch (IOException e) {
                Log.warn("Failed to load encrypted learning data, trying legacy file: {}", e.getMessage());
            }
        }
        if (!legacyFile.exists()) {
            Log.info("No learning data file found, starting fresh");
            return;
        }
        try (FileReader reader = new FileReader(legacyFile);){
            data = GSON.fromJson((Reader)reader, LearningData.class);
            if (data != null) {
                this.loadFromData(data);
                Log.info("Learning data loaded from legacy {} ({} sessions, trained={})", legacyFile.getAbsolutePath(), this.sessions.size(), this.trained);
                if (ObfuscatedStorage.isInitialized()) {
                    this.save();
                    legacyFile.delete();
                    Log.info("Migrated learning data to encrypted format");
                }
            }
        }
        catch (IOException e) {
            Log.error("Failed to load learning data from {}", legacyFile.getAbsolutePath(), e);
        }
    }

    private void loadFromData(LearningData data) {
        this.sessions.clear();
        if (data.sessions != null) {
            this.sessions.addAll(data.sessions);
        }
        this.delayMeans = data.delayMeans;
        this.delayStdDevs = data.delayStdDevs;
        this.durationMeans = data.durationMeans;
        this.durationStdDevs = data.durationStdDevs;
        this.startDelayMean = data.startDelayMean;
        this.startDelayStdDev = data.startDelayStdDev;
        this.trained = data.trained;
    }

    @Generated
    public List<MiningSession> getSessions() {
        return this.sessions;
    }
}

