/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.logic.FishingColor
 *  org.pbrands.logic.FishingState
 *  org.pbrands.sound.SoundUtils
 *  raven.toast.Notifications
 *  raven.toast.Notifications$Location
 *  raven.toast.Notifications$Type
 */
package org.pbrands.logic;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.platform.win32.WinDef;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import lombok.Generated;
import org.pbrands.hid.HIDSimulator;
import org.pbrands.logic.FishingColor;
import org.pbrands.logic.FishingState;
import org.pbrands.logic.ScreenHelper;
import org.pbrands.logic.Sizes;
import org.pbrands.model.BotStatus;
import org.pbrands.model.Resolution;
import org.pbrands.model.StartupParams;
import org.pbrands.netty.ConnectionStatus;
import org.pbrands.netty.NettyClient;
import org.pbrands.settings.SettingsWindow;
import org.pbrands.sound.Sound;
import org.pbrands.sound.SoundUtils;
import org.pbrands.ui.AppUI;
import org.pbrands.ui.OverlayWindow;
import org.pbrands.ui.ProgressWindow;
import org.pbrands.util.CoreConstants;
import org.pbrands.util.ResolutionScaler;
import org.pbrands.util.WindowsUtil;
import org.pbrands.windows.User32Extended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raven.toast.Notifications;

public class Application
implements NativeKeyListener {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    protected static final long FISHING_BAR_TIMEOUT_MS = 15000L;
    protected final HIDSimulator hidSimulator;
    protected final StartupParams startupParams;
    protected final ProgressWindow progressWindow;
    protected final NettyClient nettyClient;
    protected AppUI appUI;
    protected boolean disabled = true;
    protected boolean visible = true;
    protected BotStatus status = BotStatus.STOPPED;
    protected final OverlayWindow overlayWindow;
    protected Resolution resolution;
    protected ResolutionScaler scaler;
    protected Sizes sizes;
    protected Thread fishingThread;
    protected final File screenshotFolder;
    private ScreenHelper screenHelper;
    private boolean forceStopping = false;

    public Application(NettyClient nettyClient, StartupParams startupParams, ProgressWindow progressWindow, HIDSimulator hidSimulator) {
        this.hidSimulator = hidSimulator;
        this.startupParams = startupParams;
        this.progressWindow = progressWindow;
        this.nettyClient = nettyClient;
        User32Extended.INSTANCE.SetProcessDPIAware();
        progressWindow.showSpinner();
        this.overlayWindow = new OverlayWindow();
        this.overlayWindow.updateStatus("Initializing...");
        Runtime.getRuntime().addShutdownHook(new Thread(this.overlayWindow::dispose));
        this.awaitMTAInitialization();
        this.initializeApplicationUI();
        this.initializeGlobalKeyListeners();
        this.startDisplayThread();
        this.startFishing();
        this.registerShutdownHook();
        this.screenshotFolder = CoreConstants.SCREENSHOTS_FOLDER;
        this.screenshotFolder.mkdirs();
    }

    private void startDisplayThread() {
        Thread displayThread = new Thread(() -> {
            try {
                while (true) {
                    SettingsWindow settingsWindow;
                    boolean b;
                    Thread.sleep(50L);
                    boolean display = WindowsUtil.isMtaSanAndreasFocused();
                    if (this.appUI.isVisible() != display) {
                        this.appUI.setVisible(display);
                    }
                    if ((b = display && settingsWindow.isEnabled()) == (settingsWindow = this.appUI.getSettingsWindow()).isVisible()) continue;
                    settingsWindow.setVisible(b);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        });
        displayThread.start();
        displayThread = new Thread(() -> {
            try {
                while (true) {
                    boolean display;
                    Thread.sleep(50L);
                    boolean bl = display = WindowsUtil.isMtaSanAndreasFocused() && this.visible;
                    if (this.appUI.isVisible() == display) continue;
                    this.appUI.setVisible(display);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        });
        displayThread.start();
    }

    public BufferedImage getLowerBarImage(Robot robot) {
        Rectangle screenRect = new Rectangle(0, this.resolution.getHeight() - this.sizes.getDarkBarSize(), this.resolution.getWidth(), this.sizes.getDarkBarSize());
        return robot.createScreenCapture(screenRect);
    }

    public void startFishing() {
        this.fishingThread = new Thread(() -> {
            try {
                Random random = new Random();
                block2: while (true) {
                    if (!WindowsUtil.isMtaSanAndreasFocused()) {
                        Thread.sleep(100L);
                        this.overlayWindow.updateStatus("1");
                        continue;
                    }
                    if (this.nettyClient.getConnectionStatus() != ConnectionStatus.CONNECTED) {
                        Thread.sleep(100L);
                        continue;
                    }
                    if (this.disabled) {
                        this.forceStopping = false;
                        this.overlayWindow.updateStatus("3");
                        this.setStatus(BotStatus.STOPPED);
                        Thread.sleep(100L);
                        continue;
                    }
                    if (this.forceStopping) {
                        this.forceStopping = false;
                        this.disabled = true;
                        this.overlayWindow.updateStatus("2");
                        if (this.hidSimulator.isMousePressed()) {
                            this.hidSimulator.sendMouseRelease();
                        }
                        this.setStatus(BotStatus.STOPPED);
                        continue;
                    }
                    this.hidSimulator.sendMouseRelease();
                    Thread.sleep(100L);
                    int pressTime = random.nextInt(30, 48);
                    this.hidSimulator.sendMouseClick(pressTime);
                    Robot robot = new Robot();
                    this.overlayWindow.updateStatus("awaiting..");
                    long startTime = System.currentTimeMillis();
                    while (this.screenHelper.getFishingColor(this.getLowerBarImage(robot)) == FishingColor.UNDEFINED || !this.screenHelper.isBlackCorners(robot)) {
                        if (this.forceStopping) {
                            this.forceStopping = false;
                            this.disabled = true;
                            this.overlayWindow.updateStatus("force stopped");
                            if (this.hidSimulator.isMousePressed()) {
                                this.hidSimulator.sendMouseRelease();
                            }
                            this.setStatus(BotStatus.STOPPED);
                            continue block2;
                        }
                        Thread.sleep(100L);
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed < 15000L) continue;
                        logger.error("Timeout: Fishing bar did not appear within 10 seconds.");
                        this.overlayWindow.updateStatus("Timeout: Bar Not Found");
                        continue block2;
                    }
                    logger.info("Started fishing");
                    this.overlayWindow.updateStatus("bar showed up");
                    Thread.sleep(this.appUI.getSettingsWindow().getFishingBeginDelaySlider().getValue() + ThreadLocalRandom.current().nextInt(this.appUI.getSettingsWindow().getFishingBeginRandomizerSlider().getValue()));
                    this.hidSimulator.sendMousePress();
                    this.overlayWindow.updateStatus("fishing started");
                    do {
                        int millis;
                        if (this.forceStopping) {
                            this.forceStopping = false;
                            this.disabled = true;
                            this.overlayWindow.updateStatus("4");
                            if (this.hidSimulator.isMousePressed()) {
                                this.hidSimulator.sendMouseRelease();
                            }
                            this.setStatus(BotStatus.STOPPED);
                            continue block2;
                        }
                        this.overlayWindow.updateStatus("5");
                        Thread.sleep(20L);
                        BufferedImage lowerBar = this.getLowerBarImage(robot);
                        FishingColor fishingColor = this.screenHelper.getFishingColor(lowerBar);
                        this.overlayWindow.updateStatus("fishing color: " + fishingColor.toString());
                        if (fishingColor == FishingColor.GRAY) {
                            if (this.hidSimulator.isMousePressed()) continue;
                            this.hidSimulator.sendMousePress();
                            this.overlayWindow.updateStatus("mouse pressed");
                            continue;
                        }
                        if (fishingColor == FishingColor.GREEN) {
                            if (!(ThreadLocalRandom.current().nextDouble() < 5.0E-4)) continue;
                            Thread.sleep(30 + ThreadLocalRandom.current().nextInt(15));
                            millis = 50 + ThreadLocalRandom.current().nextInt(120);
                            this.hidSimulator.sendMouseRelease();
                            Thread.sleep(millis);
                            this.overlayWindow.updateStatus("mouse released");
                            continue;
                        }
                        if (fishingColor != FishingColor.RED) continue;
                        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
                            millis = 140 + ThreadLocalRandom.current().nextInt(140);
                            Thread.sleep(millis);
                            this.hidSimulator.sendMouseRelease();
                            this.overlayWindow.updateStatus("mouse released");
                            Thread.sleep(40 + ThreadLocalRandom.current().nextInt(60));
                            continue;
                        }
                        if (this.screenHelper.getFishingState(lowerBar) == FishingState.LINE_BREAK) {
                            if (!this.hidSimulator.isMousePressed()) continue;
                            millis = this.appUI.getSettingsWindow().getLooseDelay() + ThreadLocalRandom.current().nextInt(this.appUI.getSettingsWindow().getLooseDelayRandomizer());
                            Thread.sleep(millis);
                            this.hidSimulator.sendMouseRelease();
                            this.overlayWindow.updateStatus("mouse released");
                            Thread.sleep(40 + ThreadLocalRandom.current().nextInt(60));
                            continue;
                        }
                        if (this.hidSimulator.isMousePressed()) continue;
                        this.hidSimulator.sendMousePress();
                        this.overlayWindow.updateStatus("mouse pressed");
                    } while (this.screenHelper.isBlackCorners(robot));
                    logger.info("Stopped fishing");
                    this.overlayWindow.updateStatus("fishing stopped");
                    if (this.forceStopping) {
                        this.overlayWindow.updateStatus("6");
                        this.forceStopping = false;
                        this.disabled = true;
                        if (this.hidSimulator.isMousePressed()) {
                            this.hidSimulator.sendMouseRelease();
                        }
                        this.setStatus(BotStatus.STOPPED);
                        continue;
                    }
                    if (this.status == BotStatus.STOPPING) {
                        this.disabled = true;
                        this.setStatus(BotStatus.STOPPED);
                    }
                    Thread.sleep(this.appUI.getSettingsWindow().getFishingStartDelay() + ThreadLocalRandom.current().nextInt(this.appUI.getSettingsWindow().getFishingStartRandomizerDelay()));
                }
            }
            catch (Exception e) {
                this.overlayWindow.updateStatus("error occurred");
                return;
            }
        });
        this.fishingThread.start();
    }

    private void initializeApplicationUI() {
        WinDef.RECT windowResolutionByTitle = WindowsUtil.getWindowResolutionByTitle("MTA: San Andreas");
        if (windowResolutionByTitle == null) {
            System.out.println("exit windowResolutionByTitle");
            System.exit(-1);
            return;
        }
        int targetWidth = windowResolutionByTitle.right - windowResolutionByTitle.left;
        int targetHeight = windowResolutionByTitle.bottom - windowResolutionByTitle.top;
        while (targetWidth < 1280 && targetHeight < 720) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            windowResolutionByTitle = WindowsUtil.getWindowResolutionByTitle("MTA: San Andreas");
            if (windowResolutionByTitle == null) {
                System.out.println("exit windowResolutionByTitle");
                System.exit(-1);
                return;
            }
            targetWidth = windowResolutionByTitle.right - windowResolutionByTitle.left;
            targetHeight = windowResolutionByTitle.bottom - windowResolutionByTitle.top;
        }
        logger.info("Detected window resolution: {}x{}", (Object)targetWidth, (Object)targetHeight);
        this.resolution = new Resolution(targetWidth, targetHeight);
        this.scaler = new ResolutionScaler(2560, 1440, targetWidth, targetHeight, false);
        this.sizes = new Sizes(this.scaler, this.resolution);
        this.screenHelper = new ScreenHelper(this);
        this.appUI = new AppUI(this, new ResolutionScaler(2560, 1440, targetWidth, targetHeight, true));
        SettingsWindow settingsWindow = this.appUI.getSettingsWindow();
        GlobalScreen.addNativeKeyListener(settingsWindow);
    }

    private void waitForMTA() throws InterruptedException {
        this.progressWindow.updateStatus("Oczekiwanie na proces MTA");
        while (!WindowsUtil.doesWindowExist("MTA: San Andreas")) {
            TimeUnit.MILLISECONDS.sleep(500L);
        }
    }

    private void awaitMTAInitialization() {
        this.progressWindow.setVisible(true);
        this.progressWindow.showSpinner();
        try {
            this.waitForMTA();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            while (WindowsUtil.doesWindowExist("MTA: San Andreas")) {
                try {
                    Thread.sleep(200L);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(1500L);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Exiting application: MTA window closed");
            System.exit(0);
        }).start();
        this.progressWindow.updateStatus("Oczekiwanie na okno MTA");
        while (!WindowsUtil.isMtaSanAndreasFocused()) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.progressWindow.setVisible(false);
        WinDef.RECT windowResolutionByTitle = WindowsUtil.getWindowResolutionByTitle("MTA: San Andreas");
        if (windowResolutionByTitle == null) {
            System.err.println("Failed to get window resolution. Exiting application.");
            System.exit(-1);
            return;
        }
        int targetWidth = windowResolutionByTitle.right - windowResolutionByTitle.left;
        int targetHeight = windowResolutionByTitle.bottom - windowResolutionByTitle.top;
        while (targetWidth < 1280 && targetHeight < 720) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            windowResolutionByTitle = WindowsUtil.getWindowResolutionByTitle("MTA: San Andreas");
            if (windowResolutionByTitle == null) {
                System.err.println("Failed to get window resolution. Exiting application.");
                System.exit(-1);
                return;
            }
            targetWidth = windowResolutionByTitle.right - windowResolutionByTitle.left;
            targetHeight = windowResolutionByTitle.bottom - windowResolutionByTitle.top;
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        int keyCode = event.getKeyCode();
        if (!WindowsUtil.isMtaSanAndreasFocused()) {
            return;
        }
        if (keyCode == this.appUI.getSettingsWindow().getFishingToggleKeyCode()) {
            switch (1.$SwitchMap$org$pbrands$model$BotStatus[this.status.ordinal()]) {
                case 1: {
                    this.pause();
                    break;
                }
                case 2: {
                    this.stop();
                    break;
                }
                case 3: {
                    this.resume();
                }
            }
        }
        if (keyCode == this.appUI.getSettingsWindow().getOverlayKeyCode()) {
            boolean bl = this.visible = !this.visible;
        }
        if (keyCode == 43) {
            Robot robot;
            try {
                robot = new Robot();
            }
            catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            String timestamp = LocalDateTime.now().toString().replaceAll(":", "").replace(".", "");
            File screenshotFile = new File(this.screenshotFolder, "screencapture_" + timestamp + ".png");
            try {
                ImageIO.write((RenderedImage)screenCapture, "png", screenshotFile);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            SoundUtils.playSound((Sound)Sound.SCREENSHOT, (int)100);
            new Thread(() -> Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, 4000L, "Zrzut ekranu zosta\u0142 zapisany w folderze '" + this.startupParams.folderName + File.separator + "screenshots'")).start();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    private void initializeGlobalKeyListeners() {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException e) {
            logger.error("Failed to register native hook.", e);
            e.printStackTrace();
            System.exit(-1);
        }
        GlobalScreen.addNativeKeyListener(this);
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GlobalScreen.unregisterNativeHook();
            }
            catch (NativeHookException e) {
                e.printStackTrace();
            }
            if (this.fishingThread != null && this.fishingThread.isAlive()) {
                this.fishingThread.interrupt();
            }
        }));
    }

    private void stop() {
        this.forceStopping = true;
        logger.info("Bot force stopped.");
        this.setStatus(BotStatus.STOPPED);
    }

    private void pause() {
        this.disabled = true;
        this.forceStopping = false;
        logger.info("Bot paused.");
        this.setStatus(BotStatus.STOPPING);
    }

    private void resume() {
        if (this.forceStopping) {
            return;
        }
        this.disabled = false;
        logger.info("Bot started/resumed.");
        this.setStatus(BotStatus.RUNNING);
    }

    public void setStatus(BotStatus status) {
        this.status = status;
        this.appUI.updateStatusLabel(status);
    }

    @Generated
    public StartupParams getStartupParams() {
        return this.startupParams;
    }

    @Generated
    public NettyClient getNettyClient() {
        return this.nettyClient;
    }

    @Generated
    public AppUI getAppUI() {
        return this.appUI;
    }
}

