/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.logic.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.Generated;
import org.pbrands.logic.checks.CheckSound;
import org.pbrands.logic.settings.DetectionActionSettings;
import org.pbrands.logic.settings.DetectionType;
import org.pbrands.logic.settings.SoundMode;
import org.pbrands.sound.Sound;
import org.pbrands.util.Log;
import org.pbrands.util.ObfuscatedStorage;

public class BotSettings {
    private static final String CONFIG_FILE_NAME = "settings.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File dataFolder = null;
    private static final BotSettings instance = new BotSettings();
    private transient CopyOnWriteArrayList<Consumer<BotSettings>> changeListeners = new CopyOnWriteArrayList();
    private boolean detectionEnabled = true;
    private String playerNickname = "";
    private int playerId = 0;
    private boolean detectAdminByColor = true;
    private boolean detectAdminByOCR = false;
    private boolean detectAdminWarningPopup = true;
    private boolean pauseOnAdminDetected = true;
    private boolean alertOnAdminDetected = true;
    private boolean ignoreAdminDetection = false;
    private boolean detectFullInventory = true;
    private boolean pauseOnFullInventory = true;
    private boolean alertOnFullInventory = true;
    private boolean ignoreFullInventory = false;
    private boolean detectPrivateMessage = true;
    private boolean detectMention = true;
    private boolean pauseOnPM = false;
    private boolean alertOnPM = true;
    private boolean ignoreWarnings = false;
    private Map<DetectionType, DetectionActionSettings> detectionActions = new EnumMap<DetectionType, DetectionActionSettings>(DetectionType.class);
    private int notificationVolume = 100;
    private boolean soundEnabled = true;
    private Set<CheckSound> enabledSounds = EnumSet.allOf(CheckSound.class);
    private Map<CheckSound, String> soundMap = new EnumMap<CheckSound, String>(CheckSound.class);
    private boolean antiCaptureEnabled = false;
    private boolean motoBoostEnabled = false;
    private boolean hideBlackChatWarning = false;
    private boolean tutorialCompleted = false;
    private boolean useWindowCapture = true;
    private int toggleKeyCode = 3663;
    private int overlayKeyCode = 45;
    private List<String> activeWidgets = new ArrayList<String>(Arrays.asList("LEARNING", "SEPARATOR", "STATUS", "SEPARATOR", "RECOGNITION", "SEPARATOR", "LEVELS", "SEPARATOR", "ACCURACY", "SEPARATOR", "COAL_PRICE", "SEPARATOR", "CONTROLS"));

    public static void initialize(File folder) {
        dataFolder = folder;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        Log.info("BotSettings initialized with folder: {}", dataFolder.getAbsolutePath());
    }

    private static File getConfigFile() {
        if (dataFolder == null) {
            return new File(CONFIG_FILE_NAME);
        }
        return new File(dataFolder, CONFIG_FILE_NAME);
    }

    private BotSettings() {
        for (CheckSound cs : CheckSound.values()) {
            this.soundMap.put(cs, Sound.NOTIFICATION.name());
        }
        this.initDefaultDetectionActions();
    }

    private void initDefaultDetectionActions() {
        DetectionActionSettings adminSettings = new DetectionActionSettings();
        adminSettings.setPauseBot(true);
        adminSettings.setShowNotification(true);
        adminSettings.setPlaySound(true);
        adminSettings.setSoundMode(SoundMode.REPEATING);
        adminSettings.setRepeatIntervalMs(3000);
        adminSettings.setSelectedSound("builtin:NOTIFICATION");
        this.detectionActions.put(DetectionType.ADMIN, adminSettings);
        DetectionActionSettings adminWarnSettings = new DetectionActionSettings();
        adminWarnSettings.setPauseBot(true);
        adminWarnSettings.setShowNotification(true);
        adminWarnSettings.setPlaySound(true);
        adminWarnSettings.setSoundMode(SoundMode.REPEATING);
        adminWarnSettings.setRepeatIntervalMs(2000);
        adminWarnSettings.setSelectedSound("builtin:CARTOON_GAME");
        this.detectionActions.put(DetectionType.ADMIN_WARNING, adminWarnSettings);
        DetectionActionSettings invSettings = new DetectionActionSettings();
        invSettings.setPauseBot(true);
        invSettings.setShowNotification(true);
        invSettings.setPlaySound(true);
        invSettings.setSoundMode(SoundMode.SINGLE);
        invSettings.setSelectedSound("builtin:NOTIFICATION");
        this.detectionActions.put(DetectionType.FULL_INVENTORY, invSettings);
        DetectionActionSettings pmSettings = new DetectionActionSettings();
        pmSettings.setPauseBot(false);
        pmSettings.setShowNotification(true);
        pmSettings.setPlaySound(true);
        pmSettings.setSoundMode(SoundMode.SINGLE);
        pmSettings.setSelectedSound("builtin:PHONE_NOTIFICATION_BELL");
        this.detectionActions.put(DetectionType.PRIVATE_MESSAGE, pmSettings);
        DetectionActionSettings mentionSettings = new DetectionActionSettings();
        mentionSettings.setPauseBot(false);
        mentionSettings.setShowNotification(true);
        mentionSettings.setPlaySound(true);
        mentionSettings.setSoundMode(SoundMode.SINGLE);
        mentionSettings.setSelectedSound("builtin:HELLO_BELLS");
        this.detectionActions.put(DetectionType.MENTION, mentionSettings);
        DetectionActionSettings coalEmptySettings = new DetectionActionSettings();
        coalEmptySettings.setPauseBot(true);
        coalEmptySettings.setShowNotification(true);
        coalEmptySettings.setPlaySound(true);
        coalEmptySettings.setSoundMode(SoundMode.SINGLE);
        coalEmptySettings.setSelectedSound("builtin:NOTIFICATION");
        this.detectionActions.put(DetectionType.COAL_EMPTY, coalEmptySettings);
        DetectionActionSettings digHighSettings = new DetectionActionSettings();
        digHighSettings.setPauseBot(true);
        digHighSettings.setShowNotification(true);
        digHighSettings.setPlaySound(true);
        digHighSettings.setSoundMode(SoundMode.SINGLE);
        digHighSettings.setSelectedSound("builtin:NOTIFICATION");
        this.detectionActions.put(DetectionType.DIG_LEVEL_TOO_HIGH, digHighSettings);
        DetectionActionSettings digChangeSettings = new DetectionActionSettings();
        digChangeSettings.setPauseBot(false);
        digChangeSettings.setShowNotification(true);
        digChangeSettings.setPlaySound(true);
        digChangeSettings.setSoundMode(SoundMode.SINGLE);
        digChangeSettings.setSelectedSound("builtin:NOTIFICATION_DIGITAL_STRUM");
        this.detectionActions.put(DetectionType.DIG_LEVEL_CHANGE, digChangeSettings);
    }

    public DetectionActionSettings getDetectionAction(DetectionType type) {
        DetectionActionSettings settings = this.detectionActions.get((Object)type);
        if (settings == null) {
            settings = new DetectionActionSettings();
            this.detectionActions.put(type, settings);
        }
        return settings;
    }

    public void setDetectionAction(DetectionType type, DetectionActionSettings settings) {
        this.detectionActions.put(type, settings);
        this.notifyListeners();
    }

    public boolean isCheckSoundEnabled(CheckSound checkSound) {
        if (!this.soundEnabled) {
            return false;
        }
        return this.enabledSounds.contains((Object)checkSound);
    }

    public void setCheckSoundEnabled(CheckSound checkSound, boolean enabled) {
        if (enabled) {
            this.enabledSounds.add(checkSound);
        } else {
            this.enabledSounds.remove((Object)checkSound);
        }
        this.notifyListeners();
    }

    public Sound getSoundForFeature(CheckSound feature) {
        String soundName = this.soundMap.get((Object)feature);
        if (soundName == null) {
            return Sound.NOTIFICATION;
        }
        try {
            return Sound.valueOf(soundName);
        }
        catch (IllegalArgumentException e) {
            return Sound.NOTIFICATION;
        }
    }

    public void setSoundForFeature(CheckSound feature, Sound sound) {
        this.soundMap.put(feature, sound.name());
        this.notifyListeners();
    }

    public void save() {
        File configFile = ObfuscatedStorage.isInitialized() ? ObfuscatedStorage.getObfuscatedFile(dataFolder, "preferences.dat") : BotSettings.getConfigFile();
        try {
            String json = GSON.toJson(this);
            if (ObfuscatedStorage.isInitialized()) {
                ObfuscatedStorage.writeEncrypted(configFile, json);
            } else {
                try (FileWriter writer = new FileWriter(configFile);){
                    writer.write(json);
                }
            }
            Log.info("Settings saved to {}", configFile.getAbsolutePath());
        }
        catch (IOException e) {
            Log.error("Failed to save settings to {}", configFile.getAbsolutePath(), e);
        }
    }

    public void load() {
        BotSettings loaded;
        File encryptedFile = ObfuscatedStorage.isInitialized() ? ObfuscatedStorage.getObfuscatedFile(dataFolder, "preferences.dat") : null;
        File legacyFile = BotSettings.getConfigFile();
        if (encryptedFile != null && encryptedFile.exists()) {
            try {
                String json = ObfuscatedStorage.readEncrypted(encryptedFile);
                if (json != null && (loaded = GSON.fromJson(json, BotSettings.class)) != null) {
                    this.copyFrom(loaded);
                    Log.info("Settings loaded from encrypted {}", encryptedFile.getAbsolutePath());
                    return;
                }
            }
            catch (IOException e) {
                Log.warn("Failed to load encrypted settings, trying legacy file: {}", e.getMessage());
            }
        }
        if (!legacyFile.exists()) {
            Log.info("No settings file found, using defaults and saving");
            this.save();
            return;
        }
        try (FileReader reader = new FileReader(legacyFile);){
            loaded = GSON.fromJson((Reader)reader, BotSettings.class);
            if (loaded != null) {
                this.copyFrom(loaded);
                Log.info("Settings loaded from legacy {}", legacyFile.getAbsolutePath());
                if (ObfuscatedStorage.isInitialized()) {
                    this.save();
                    legacyFile.delete();
                    Log.info("Migrated settings to encrypted format");
                }
            }
        }
        catch (IOException e) {
            Log.error("Failed to load settings from {}", legacyFile.getAbsolutePath(), e);
        }
    }

    private void copyFrom(BotSettings other) {
        this.detectionEnabled = other.detectionEnabled;
        this.playerNickname = other.playerNickname;
        this.playerId = other.playerId;
        this.detectAdminByColor = other.detectAdminByColor;
        this.detectAdminByOCR = other.detectAdminByOCR;
        this.detectAdminWarningPopup = other.detectAdminWarningPopup;
        this.pauseOnAdminDetected = other.pauseOnAdminDetected;
        this.alertOnAdminDetected = other.alertOnAdminDetected;
        this.ignoreAdminDetection = other.ignoreAdminDetection;
        this.detectFullInventory = other.detectFullInventory;
        this.pauseOnFullInventory = other.pauseOnFullInventory;
        this.alertOnFullInventory = other.alertOnFullInventory;
        this.ignoreFullInventory = other.ignoreFullInventory;
        this.detectPrivateMessage = other.detectPrivateMessage;
        this.detectMention = other.detectMention;
        this.pauseOnPM = other.pauseOnPM;
        this.alertOnPM = other.alertOnPM;
        this.ignoreWarnings = other.ignoreWarnings;
        this.notificationVolume = other.notificationVolume;
        this.soundEnabled = other.soundEnabled;
        if (other.enabledSounds != null) {
            this.enabledSounds = EnumSet.copyOf(other.enabledSounds);
        }
        if (other.soundMap != null) {
            this.soundMap = new EnumMap<CheckSound, String>(other.soundMap);
        }
        this.antiCaptureEnabled = other.antiCaptureEnabled;
        this.motoBoostEnabled = other.motoBoostEnabled;
        this.hideBlackChatWarning = other.hideBlackChatWarning;
        this.tutorialCompleted = other.tutorialCompleted;
        this.useWindowCapture = other.useWindowCapture;
        this.toggleKeyCode = other.toggleKeyCode;
        this.overlayKeyCode = other.overlayKeyCode;
        if (other.activeWidgets != null && !other.activeWidgets.isEmpty()) {
            this.activeWidgets = new ArrayList<String>(other.activeWidgets);
        }
        if (other.detectionActions != null && !other.detectionActions.isEmpty()) {
            for (Map.Entry<DetectionType, DetectionActionSettings> entry : other.detectionActions.entrySet()) {
                DetectionType key = entry.getKey();
                if (key == null) continue;
                DetectionActionSettings existing = this.detectionActions.get((Object)key);
                if (existing == null) {
                    existing = new DetectionActionSettings();
                    this.detectionActions.put(key, existing);
                }
                existing.copyFrom(entry.getValue());
            }
        }
        this.notifyListeners();
    }

    public void addChangeListener(Consumer<BotSettings> listener) {
        if (this.changeListeners == null) {
            this.changeListeners = new CopyOnWriteArrayList();
        }
        this.changeListeners.add(listener);
    }

    public void removeChangeListener(Consumer<BotSettings> listener) {
        if (this.changeListeners != null) {
            this.changeListeners.remove(listener);
        }
    }

    public void notifyListeners() {
        if (this.changeListeners != null) {
            for (Consumer<BotSettings> listener : this.changeListeners) {
                try {
                    listener.accept(this);
                }
                catch (Exception e) {
                    Log.error("Error notifying settings listener", e);
                }
            }
        }
    }

    @Generated
    public CopyOnWriteArrayList<Consumer<BotSettings>> getChangeListeners() {
        return this.changeListeners;
    }

    @Generated
    public boolean isDetectionEnabled() {
        return this.detectionEnabled;
    }

    @Generated
    public String getPlayerNickname() {
        return this.playerNickname;
    }

    @Generated
    public int getPlayerId() {
        return this.playerId;
    }

    @Generated
    public boolean isDetectAdminByColor() {
        return this.detectAdminByColor;
    }

    @Generated
    public boolean isDetectAdminByOCR() {
        return this.detectAdminByOCR;
    }

    @Generated
    public boolean isDetectAdminWarningPopup() {
        return this.detectAdminWarningPopup;
    }

    @Generated
    public boolean isPauseOnAdminDetected() {
        return this.pauseOnAdminDetected;
    }

    @Generated
    public boolean isAlertOnAdminDetected() {
        return this.alertOnAdminDetected;
    }

    @Generated
    public boolean isIgnoreAdminDetection() {
        return this.ignoreAdminDetection;
    }

    @Generated
    public boolean isDetectFullInventory() {
        return this.detectFullInventory;
    }

    @Generated
    public boolean isPauseOnFullInventory() {
        return this.pauseOnFullInventory;
    }

    @Generated
    public boolean isAlertOnFullInventory() {
        return this.alertOnFullInventory;
    }

    @Generated
    public boolean isIgnoreFullInventory() {
        return this.ignoreFullInventory;
    }

    @Generated
    public boolean isDetectPrivateMessage() {
        return this.detectPrivateMessage;
    }

    @Generated
    public boolean isDetectMention() {
        return this.detectMention;
    }

    @Generated
    public boolean isPauseOnPM() {
        return this.pauseOnPM;
    }

    @Generated
    public boolean isAlertOnPM() {
        return this.alertOnPM;
    }

    @Generated
    public boolean isIgnoreWarnings() {
        return this.ignoreWarnings;
    }

    @Generated
    public Map<DetectionType, DetectionActionSettings> getDetectionActions() {
        return this.detectionActions;
    }

    @Generated
    public int getNotificationVolume() {
        return this.notificationVolume;
    }

    @Generated
    public boolean isSoundEnabled() {
        return this.soundEnabled;
    }

    @Generated
    public Set<CheckSound> getEnabledSounds() {
        return this.enabledSounds;
    }

    @Generated
    public Map<CheckSound, String> getSoundMap() {
        return this.soundMap;
    }

    @Generated
    public boolean isAntiCaptureEnabled() {
        return this.antiCaptureEnabled;
    }

    @Generated
    public boolean isMotoBoostEnabled() {
        return this.motoBoostEnabled;
    }

    @Generated
    public boolean isHideBlackChatWarning() {
        return this.hideBlackChatWarning;
    }

    @Generated
    public boolean isTutorialCompleted() {
        return this.tutorialCompleted;
    }

    @Generated
    public boolean isUseWindowCapture() {
        return this.useWindowCapture;
    }

    @Generated
    public int getToggleKeyCode() {
        return this.toggleKeyCode;
    }

    @Generated
    public int getOverlayKeyCode() {
        return this.overlayKeyCode;
    }

    @Generated
    public List<String> getActiveWidgets() {
        return this.activeWidgets;
    }

    @Generated
    public void setChangeListeners(CopyOnWriteArrayList<Consumer<BotSettings>> changeListeners) {
        this.changeListeners = changeListeners;
    }

    @Generated
    public void setDetectionEnabled(boolean detectionEnabled) {
        this.detectionEnabled = detectionEnabled;
    }

    @Generated
    public void setPlayerNickname(String playerNickname) {
        this.playerNickname = playerNickname;
    }

    @Generated
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    @Generated
    public void setDetectAdminByColor(boolean detectAdminByColor) {
        this.detectAdminByColor = detectAdminByColor;
    }

    @Generated
    public void setDetectAdminByOCR(boolean detectAdminByOCR) {
        this.detectAdminByOCR = detectAdminByOCR;
    }

    @Generated
    public void setDetectAdminWarningPopup(boolean detectAdminWarningPopup) {
        this.detectAdminWarningPopup = detectAdminWarningPopup;
    }

    @Generated
    public void setPauseOnAdminDetected(boolean pauseOnAdminDetected) {
        this.pauseOnAdminDetected = pauseOnAdminDetected;
    }

    @Generated
    public void setAlertOnAdminDetected(boolean alertOnAdminDetected) {
        this.alertOnAdminDetected = alertOnAdminDetected;
    }

    @Generated
    public void setIgnoreAdminDetection(boolean ignoreAdminDetection) {
        this.ignoreAdminDetection = ignoreAdminDetection;
    }

    @Generated
    public void setDetectFullInventory(boolean detectFullInventory) {
        this.detectFullInventory = detectFullInventory;
    }

    @Generated
    public void setPauseOnFullInventory(boolean pauseOnFullInventory) {
        this.pauseOnFullInventory = pauseOnFullInventory;
    }

    @Generated
    public void setAlertOnFullInventory(boolean alertOnFullInventory) {
        this.alertOnFullInventory = alertOnFullInventory;
    }

    @Generated
    public void setIgnoreFullInventory(boolean ignoreFullInventory) {
        this.ignoreFullInventory = ignoreFullInventory;
    }

    @Generated
    public void setDetectPrivateMessage(boolean detectPrivateMessage) {
        this.detectPrivateMessage = detectPrivateMessage;
    }

    @Generated
    public void setDetectMention(boolean detectMention) {
        this.detectMention = detectMention;
    }

    @Generated
    public void setPauseOnPM(boolean pauseOnPM) {
        this.pauseOnPM = pauseOnPM;
    }

    @Generated
    public void setAlertOnPM(boolean alertOnPM) {
        this.alertOnPM = alertOnPM;
    }

    @Generated
    public void setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }

    @Generated
    public void setDetectionActions(Map<DetectionType, DetectionActionSettings> detectionActions) {
        this.detectionActions = detectionActions;
    }

    @Generated
    public void setNotificationVolume(int notificationVolume) {
        this.notificationVolume = notificationVolume;
    }

    @Generated
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    @Generated
    public void setEnabledSounds(Set<CheckSound> enabledSounds) {
        this.enabledSounds = enabledSounds;
    }

    @Generated
    public void setSoundMap(Map<CheckSound, String> soundMap) {
        this.soundMap = soundMap;
    }

    @Generated
    public void setAntiCaptureEnabled(boolean antiCaptureEnabled) {
        this.antiCaptureEnabled = antiCaptureEnabled;
    }

    @Generated
    public void setMotoBoostEnabled(boolean motoBoostEnabled) {
        this.motoBoostEnabled = motoBoostEnabled;
    }

    @Generated
    public void setHideBlackChatWarning(boolean hideBlackChatWarning) {
        this.hideBlackChatWarning = hideBlackChatWarning;
    }

    @Generated
    public void setTutorialCompleted(boolean tutorialCompleted) {
        this.tutorialCompleted = tutorialCompleted;
    }

    @Generated
    public void setUseWindowCapture(boolean useWindowCapture) {
        this.useWindowCapture = useWindowCapture;
    }

    @Generated
    public void setToggleKeyCode(int toggleKeyCode) {
        this.toggleKeyCode = toggleKeyCode;
    }

    @Generated
    public void setOverlayKeyCode(int overlayKeyCode) {
        this.overlayKeyCode = overlayKeyCode;
    }

    @Generated
    public void setActiveWidgets(List<String> activeWidgets) {
        this.activeWidgets = activeWidgets;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BotSettings)) {
            return false;
        }
        BotSettings other = (BotSettings)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isDetectionEnabled() != other.isDetectionEnabled()) {
            return false;
        }
        if (this.getPlayerId() != other.getPlayerId()) {
            return false;
        }
        if (this.isDetectAdminByColor() != other.isDetectAdminByColor()) {
            return false;
        }
        if (this.isDetectAdminByOCR() != other.isDetectAdminByOCR()) {
            return false;
        }
        if (this.isDetectAdminWarningPopup() != other.isDetectAdminWarningPopup()) {
            return false;
        }
        if (this.isPauseOnAdminDetected() != other.isPauseOnAdminDetected()) {
            return false;
        }
        if (this.isAlertOnAdminDetected() != other.isAlertOnAdminDetected()) {
            return false;
        }
        if (this.isIgnoreAdminDetection() != other.isIgnoreAdminDetection()) {
            return false;
        }
        if (this.isDetectFullInventory() != other.isDetectFullInventory()) {
            return false;
        }
        if (this.isPauseOnFullInventory() != other.isPauseOnFullInventory()) {
            return false;
        }
        if (this.isAlertOnFullInventory() != other.isAlertOnFullInventory()) {
            return false;
        }
        if (this.isIgnoreFullInventory() != other.isIgnoreFullInventory()) {
            return false;
        }
        if (this.isDetectPrivateMessage() != other.isDetectPrivateMessage()) {
            return false;
        }
        if (this.isDetectMention() != other.isDetectMention()) {
            return false;
        }
        if (this.isPauseOnPM() != other.isPauseOnPM()) {
            return false;
        }
        if (this.isAlertOnPM() != other.isAlertOnPM()) {
            return false;
        }
        if (this.isIgnoreWarnings() != other.isIgnoreWarnings()) {
            return false;
        }
        if (this.getNotificationVolume() != other.getNotificationVolume()) {
            return false;
        }
        if (this.isSoundEnabled() != other.isSoundEnabled()) {
            return false;
        }
        if (this.isAntiCaptureEnabled() != other.isAntiCaptureEnabled()) {
            return false;
        }
        if (this.isMotoBoostEnabled() != other.isMotoBoostEnabled()) {
            return false;
        }
        if (this.isHideBlackChatWarning() != other.isHideBlackChatWarning()) {
            return false;
        }
        if (this.isTutorialCompleted() != other.isTutorialCompleted()) {
            return false;
        }
        if (this.isUseWindowCapture() != other.isUseWindowCapture()) {
            return false;
        }
        if (this.getToggleKeyCode() != other.getToggleKeyCode()) {
            return false;
        }
        if (this.getOverlayKeyCode() != other.getOverlayKeyCode()) {
            return false;
        }
        String this$playerNickname = this.getPlayerNickname();
        String other$playerNickname = other.getPlayerNickname();
        if (this$playerNickname == null ? other$playerNickname != null : !this$playerNickname.equals(other$playerNickname)) {
            return false;
        }
        Map<DetectionType, DetectionActionSettings> this$detectionActions = this.getDetectionActions();
        Map<DetectionType, DetectionActionSettings> other$detectionActions = other.getDetectionActions();
        if (this$detectionActions == null ? other$detectionActions != null : !((Object)this$detectionActions).equals(other$detectionActions)) {
            return false;
        }
        Set<CheckSound> this$enabledSounds = this.getEnabledSounds();
        Set<CheckSound> other$enabledSounds = other.getEnabledSounds();
        if (this$enabledSounds == null ? other$enabledSounds != null : !((Object)this$enabledSounds).equals(other$enabledSounds)) {
            return false;
        }
        Map<CheckSound, String> this$soundMap = this.getSoundMap();
        Map<CheckSound, String> other$soundMap = other.getSoundMap();
        if (this$soundMap == null ? other$soundMap != null : !((Object)this$soundMap).equals(other$soundMap)) {
            return false;
        }
        List<String> this$activeWidgets = this.getActiveWidgets();
        List<String> other$activeWidgets = other.getActiveWidgets();
        return !(this$activeWidgets == null ? other$activeWidgets != null : !((Object)this$activeWidgets).equals(other$activeWidgets));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof BotSettings;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isDetectionEnabled() ? 79 : 97);
        result = result * 59 + this.getPlayerId();
        result = result * 59 + (this.isDetectAdminByColor() ? 79 : 97);
        result = result * 59 + (this.isDetectAdminByOCR() ? 79 : 97);
        result = result * 59 + (this.isDetectAdminWarningPopup() ? 79 : 97);
        result = result * 59 + (this.isPauseOnAdminDetected() ? 79 : 97);
        result = result * 59 + (this.isAlertOnAdminDetected() ? 79 : 97);
        result = result * 59 + (this.isIgnoreAdminDetection() ? 79 : 97);
        result = result * 59 + (this.isDetectFullInventory() ? 79 : 97);
        result = result * 59 + (this.isPauseOnFullInventory() ? 79 : 97);
        result = result * 59 + (this.isAlertOnFullInventory() ? 79 : 97);
        result = result * 59 + (this.isIgnoreFullInventory() ? 79 : 97);
        result = result * 59 + (this.isDetectPrivateMessage() ? 79 : 97);
        result = result * 59 + (this.isDetectMention() ? 79 : 97);
        result = result * 59 + (this.isPauseOnPM() ? 79 : 97);
        result = result * 59 + (this.isAlertOnPM() ? 79 : 97);
        result = result * 59 + (this.isIgnoreWarnings() ? 79 : 97);
        result = result * 59 + this.getNotificationVolume();
        result = result * 59 + (this.isSoundEnabled() ? 79 : 97);
        result = result * 59 + (this.isAntiCaptureEnabled() ? 79 : 97);
        result = result * 59 + (this.isMotoBoostEnabled() ? 79 : 97);
        result = result * 59 + (this.isHideBlackChatWarning() ? 79 : 97);
        result = result * 59 + (this.isTutorialCompleted() ? 79 : 97);
        result = result * 59 + (this.isUseWindowCapture() ? 79 : 97);
        result = result * 59 + this.getToggleKeyCode();
        result = result * 59 + this.getOverlayKeyCode();
        String $playerNickname = this.getPlayerNickname();
        result = result * 59 + ($playerNickname == null ? 43 : $playerNickname.hashCode());
        Map<DetectionType, DetectionActionSettings> $detectionActions = this.getDetectionActions();
        result = result * 59 + ($detectionActions == null ? 43 : ((Object)$detectionActions).hashCode());
        Set<CheckSound> $enabledSounds = this.getEnabledSounds();
        result = result * 59 + ($enabledSounds == null ? 43 : ((Object)$enabledSounds).hashCode());
        Map<CheckSound, String> $soundMap = this.getSoundMap();
        result = result * 59 + ($soundMap == null ? 43 : ((Object)$soundMap).hashCode());
        List<String> $activeWidgets = this.getActiveWidgets();
        result = result * 59 + ($activeWidgets == null ? 43 : ((Object)$activeWidgets).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "BotSettings(changeListeners=" + String.valueOf(this.getChangeListeners()) + ", detectionEnabled=" + this.isDetectionEnabled() + ", playerNickname=" + this.getPlayerNickname() + ", playerId=" + this.getPlayerId() + ", detectAdminByColor=" + this.isDetectAdminByColor() + ", detectAdminByOCR=" + this.isDetectAdminByOCR() + ", detectAdminWarningPopup=" + this.isDetectAdminWarningPopup() + ", pauseOnAdminDetected=" + this.isPauseOnAdminDetected() + ", alertOnAdminDetected=" + this.isAlertOnAdminDetected() + ", ignoreAdminDetection=" + this.isIgnoreAdminDetection() + ", detectFullInventory=" + this.isDetectFullInventory() + ", pauseOnFullInventory=" + this.isPauseOnFullInventory() + ", alertOnFullInventory=" + this.isAlertOnFullInventory() + ", ignoreFullInventory=" + this.isIgnoreFullInventory() + ", detectPrivateMessage=" + this.isDetectPrivateMessage() + ", detectMention=" + this.isDetectMention() + ", pauseOnPM=" + this.isPauseOnPM() + ", alertOnPM=" + this.isAlertOnPM() + ", ignoreWarnings=" + this.isIgnoreWarnings() + ", detectionActions=" + String.valueOf(this.getDetectionActions()) + ", notificationVolume=" + this.getNotificationVolume() + ", soundEnabled=" + this.isSoundEnabled() + ", enabledSounds=" + String.valueOf(this.getEnabledSounds()) + ", soundMap=" + String.valueOf(this.getSoundMap()) + ", antiCaptureEnabled=" + this.isAntiCaptureEnabled() + ", motoBoostEnabled=" + this.isMotoBoostEnabled() + ", hideBlackChatWarning=" + this.isHideBlackChatWarning() + ", tutorialCompleted=" + this.isTutorialCompleted() + ", useWindowCapture=" + this.isUseWindowCapture() + ", toggleKeyCode=" + this.getToggleKeyCode() + ", overlayKeyCode=" + this.getOverlayKeyCode() + ", activeWidgets=" + String.valueOf(this.getActiveWidgets()) + ")";
    }

    @Generated
    public static BotSettings getInstance() {
        return instance;
    }
}

