/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.sound.SoundManager$SoundEntry
 *  org.pbrands.sound.SoundUtils
 */
package org.pbrands.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import lombok.Generated;
import org.pbrands.sound.Sound;
import org.pbrands.sound.SoundEvent;
import org.pbrands.sound.SoundManager;
import org.pbrands.sound.SoundUtils;

public class SoundManager {
    private static SoundManager instance;
    private final Path customSoundsDir;
    private final Map<String, Path> customSounds = new LinkedHashMap<String, Path>();
    private final Map<SoundEvent, String> eventSounds = new EnumMap<SoundEvent, String>(SoundEvent.class);
    private final List<Clip> activeClips = new ArrayList<Clip>();

    private SoundManager() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) {
            localAppData = System.getProperty("user.home");
        }
        this.customSoundsDir = Paths.get(localAppData, "LifeBot", "sounds");
        try {
            Files.createDirectories(this.customSoundsDir, new FileAttribute[0]);
        }
        catch (IOException e) {
            System.err.println("Could not create custom sounds directory: " + e.getMessage());
        }
        this.loadCustomSounds();
        this.setDefaultEventSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadCustomSounds() {
        this.customSounds.clear();
        try {
            if (Files.exists(this.customSoundsDir, new LinkOption[0])) {
                Files.list(this.customSoundsDir).filter(p -> p.toString().toLowerCase().endsWith(".wav")).forEach(p -> {
                    String name = p.getFileName().toString();
                    name = name.substring(0, name.lastIndexOf(46));
                    this.customSounds.put(name, (Path)p);
                });
            }
        }
        catch (IOException e) {
            System.err.println("Error loading custom sounds: " + e.getMessage());
        }
    }

    private void setDefaultEventSounds() {
        this.eventSounds.put(SoundEvent.ADMIN_DETECTED, "builtin:" + Sound.NOTIFICATION.name());
        this.eventSounds.put(SoundEvent.FULL_INVENTORY, "builtin:" + Sound.NOTIFICATION.name());
        this.eventSounds.put(SoundEvent.PRIVATE_MESSAGE, "builtin:" + Sound.PHONE_NOTIFICATION_BELL.name());
        this.eventSounds.put(SoundEvent.MENTION, "builtin:" + Sound.NOTIFICATION_DIGITAL_STRUM.name());
        this.eventSounds.put(SoundEvent.AFK_WARNING, "builtin:" + Sound.CARTOON_GAME.name());
        this.eventSounds.put(SoundEvent.BOT_STARTED, "builtin:" + Sound.HELLO_BELLS.name());
        this.eventSounds.put(SoundEvent.BOT_STOPPED, "builtin:" + Sound.NOTIFICATION.name());
        this.eventSounds.put(SoundEvent.ERROR, "builtin:" + Sound.NOTIFICATION_DIGITAL_STRUM.name());
    }

    public String importSound(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        String filename = sourceFile.getName();
        if (!filename.toLowerCase().endsWith(".wav")) {
            System.err.println("Only .wav files are supported");
            return null;
        }
        try {
            Path destPath = this.customSoundsDir.resolve(filename);
            int counter = 1;
            String baseName = filename.substring(0, filename.lastIndexOf(46));
            while (Files.exists(destPath, new LinkOption[0])) {
                destPath = this.customSoundsDir.resolve(baseName + "_" + counter + ".wav");
                ++counter;
            }
            Files.copy(sourceFile.toPath(), destPath, new CopyOption[0]);
            String name = destPath.getFileName().toString();
            name = name.substring(0, name.lastIndexOf(46));
            this.customSounds.put(name, destPath);
            return name;
        }
        catch (IOException e) {
            System.err.println("Error importing sound: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteCustomSound(String name) {
        Path path = this.customSounds.get(name);
        if (path == null) {
            return false;
        }
        try {
            Files.deleteIfExists(path);
            this.customSounds.remove(name);
            this.eventSounds.entrySet().removeIf(e -> ((String)e.getValue()).equals("custom:" + name));
            return true;
        }
        catch (IOException e2) {
            System.err.println("Error deleting sound: " + e2.getMessage());
            return false;
        }
    }

    public List<SoundEntry> getAllSounds() {
        ArrayList<SoundEntry> sounds = new ArrayList<SoundEntry>();
        for (Sound sound : Sound.values()) {
            if (!sound.isUserSelectable()) continue;
            sounds.add(new SoundEntry("builtin:" + sound.name(), sound.getName(), true));
        }
        for (String name : this.customSounds.keySet()) {
            sounds.add(new SoundEntry("custom:" + name, name, false));
        }
        return sounds;
    }

    public void setEventSound(SoundEvent event, String soundId) {
        this.eventSounds.put(event, soundId);
    }

    public void playSound(String soundId, int volume) {
        if (soundId == null || soundId.isEmpty()) {
            return;
        }
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream;
                if (soundId.startsWith("builtin:")) {
                    name = soundId.substring("builtin:".length());
                    Sound sound = Sound.valueOf(name);
                    InputStream audioSrc = SoundUtils.class.getResourceAsStream("/sounds/" + sound.getFilename());
                    if (audioSrc == null) {
                        System.err.println("Built-in sound not found: " + sound.getFilename());
                        return;
                    }
                    audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
                } else if (soundId.startsWith("custom:")) {
                    name = soundId.substring("custom:".length());
                    Path path = this.customSounds.get(name);
                    if (path == null || !Files.exists(path, new LinkOption[0])) {
                        System.err.println("Custom sound not found: " + name);
                        return;
                    }
                    audioInputStream = AudioSystem.getAudioInputStream(path.toFile());
                } else {
                    System.err.println("Invalid sound ID: " + soundId);
                    return;
                }
                AudioFormat originalFormat = audioInputStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16, originalFormat.getChannels(), originalFormat.getChannels() * 2, originalFormat.getSampleRate(), false);
                if (originalFormat.getSampleSizeInBits() != 16) {
                    audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                }
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                double vol = (double)Math.max(0, Math.min(100, volume)) / 100.0;
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float)(Math.log(vol) / Math.log(10.0) * 20.0);
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }
                List<Clip> list = this.activeClips;
                synchronized (list) {
                    this.activeClips.add(clip);
                }
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        List<Clip> list = this.activeClips;
                        synchronized (list) {
                            this.activeClips.remove(clip);
                        }
                    }
                });
                clip.start();
            }
            catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }).start();
    }

    public void playEventSound(SoundEvent event, int volume) {
        String soundId = this.eventSounds.get((Object)event);
        if (soundId != null) {
            this.playSound(soundId, volume);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void stopAllSounds() {
        List<Clip> list = this.activeClips;
        synchronized (list) {
            for (Clip clip : new ArrayList<Clip>(this.activeClips)) {
                clip.stop();
                clip.close();
            }
            this.activeClips.clear();
        }
    }

    public void refreshCustomSounds() {
        this.loadCustomSounds();
    }

    public Path getCustomSoundsDirectory() {
        return this.customSoundsDir;
    }

    @Generated
    public Map<String, Path> getCustomSounds() {
        return this.customSounds;
    }

    @Generated
    public Map<SoundEvent, String> getEventSounds() {
        return this.eventSounds;
    }
}

