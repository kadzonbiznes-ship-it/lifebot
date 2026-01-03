/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.logic.checks.AdminWarningChecker
 *  org.pbrands.ui.overlay.DebugCaptures
 *  org.pbrands.ui.overlay.ImGuiNotifications$Type
 *  org.pbrands.ui.overlay.MiniGameWindow$GameType
 */
package org.pbrands.ui.overlay;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec4;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.lwjgl.opengl.GL11;
import org.pbrands.logic.checks.AdminWarningChecker;
import org.pbrands.logic.settings.BotSettings;
import org.pbrands.logic.settings.DetectionActionSettings;
import org.pbrands.logic.settings.DetectionType;
import org.pbrands.logic.settings.SoundMode;
import org.pbrands.ui.overlay.DebugCaptures;
import org.pbrands.ui.overlay.ImGuiNotifications;
import org.pbrands.ui.overlay.LifebotTheme;
import org.pbrands.ui.overlay.MiniGameWindow;
import org.pbrands.ui.overlay.TutorialWindow;

public class LifebotImGuiUI {
    private static final ImVec4 COLOR_BG_DARK = LifebotTheme.BG_DARK;
    private static final ImVec4 COLOR_BG_MEDIUM = LifebotTheme.BG_MEDIUM;
    private static final ImVec4 COLOR_BG_LIGHT = LifebotTheme.BG_LIGHT;
    private static final ImVec4 COLOR_ACCENT_PRIMARY = LifebotTheme.ACCENT_PRIMARY;
    private static final ImVec4 COLOR_ACCENT_SECONDARY = LifebotTheme.ACCENT_SECONDARY;
    private static final ImVec4 COLOR_ACCENT_HOVER = LifebotTheme.ACCENT_PRIMARY_HOVER;
    private static final ImVec4 COLOR_ACCENT_GREEN = LifebotTheme.SUCCESS;
    private static final ImVec4 COLOR_ACCENT_RED = LifebotTheme.ERROR;
    private static final ImVec4 COLOR_ACCENT_YELLOW = LifebotTheme.WARNING;
    private static final ImVec4 COLOR_ACCENT_PURPLE = LifebotTheme.PURPLE;
    private static final ImVec4 COLOR_ACCENT_CYAN = LifebotTheme.ACCENT_SECONDARY;
    private static final ImVec4 COLOR_TEXT_PRIMARY = LifebotTheme.TEXT_PRIMARY;
    private static final ImVec4 COLOR_TEXT_SECONDARY = LifebotTheme.TEXT_SECONDARY;
    private static final ImVec4 COLOR_TEXT_DISABLED = LifebotTheme.TEXT_DISABLED;
    private boolean botRunning = false;
    private String statusText = "Zatrzymany";
    private ImVec4 statusColor = COLOR_ACCENT_RED;
    private char[] recognizedLetters = new char[0];
    private boolean[] recognizedCorrect = new boolean[0];
    private String yourLevel = "N/A";
    private ImVec4 yourLevelColor = COLOR_TEXT_SECONDARY;
    private String requiredLevel = "N/A";
    private ImVec4 requiredLevelColor = COLOR_TEXT_SECONDARY;
    private String accuracy = "N/A";
    private String coalPrice = "N/A";
    private double coalPriceValue = 0.0;
    private int collectedSamples = 0;
    private int requiredSamples = 10;
    private boolean isLearningReady = false;
    private double[] learnedDelayMeans = new double[3];
    private double[] learnedDelayStdDevs = new double[3];
    private double[] learnedDurationMeans = new double[3];
    private double[] learnedDurationStdDevs = new double[3];
    private final ImInt notificationVolume = new ImInt(100);
    private final ImBoolean antiCapture = new ImBoolean(false);
    private final ImBoolean soundEnabled = new ImBoolean(true);
    private final ImBoolean passthroughHotkeys = new ImBoolean(false);
    private final ImBoolean hideBlackChatWarning = new ImBoolean(false);
    private final ImBoolean useWindowCapture = new ImBoolean(true);
    private final ImFloat windowOpacity = new ImFloat(0.95f);
    private final ImBoolean detectionEnabled = new ImBoolean(true);
    private final ImString playerNickname = new ImString(64);
    private final ImInt playerId = new ImInt(0);
    private final ImBoolean detectFullInventory = new ImBoolean(true);
    private final ImBoolean pauseOnFullInventory = new ImBoolean(true);
    private final ImBoolean alertOnFullInventory = new ImBoolean(true);
    private final ImBoolean detectAdminByColor = new ImBoolean(true);
    private final ImBoolean detectAdminByOCR = new ImBoolean(false);
    private final ImBoolean detectAdminWarningPopup = new ImBoolean(true);
    private final ImBoolean pauseOnAdminDetected = new ImBoolean(true);
    private final ImBoolean alertOnAdminDetected = new ImBoolean(true);
    private final ImBoolean detectPrivateMessage = new ImBoolean(true);
    private final ImBoolean detectMention = new ImBoolean(true);
    private final ImBoolean pauseOnPM = new ImBoolean(false);
    private final ImBoolean alertOnPM = new ImBoolean(true);
    private boolean showSettings = false;
    private final ImBoolean showSettingsPtr = new ImBoolean(false);
    private boolean showDebug = false;
    private boolean showChatBlockingPopup = false;
    private boolean showBlackBackgroundWarning = false;
    private boolean mainWindowPositionSet = false;
    private volatile boolean serverConnected = true;
    private volatile String serverConnectionStatus = "";
    private volatile boolean isBanned = false;
    private volatile String banReason = "";
    private volatile int banExitCode = 0;
    private Runnable shutdownCallback = null;
    private boolean chatHasBlackBackground = false;
    private int currentSettingsTab = 0;
    private boolean uiVisible = true;
    private float uiAlpha = 1.0f;
    private static final float FADE_SPEED = 8.0f;
    private int currentDetectionSubTab = 0;
    private final Map<String, Integer> debugTextureCache = new HashMap<String, Integer>();
    private final Map<String, Long> debugTextureTimestamps = new HashMap<String, Long>();
    private static final String[] SETTINGS_TABS = new String[]{"Widgety", "Uczenie", "Detekcja", "Dzwieki", "Dodatkowe", "Minigry"};
    private static final String[] SETTINGS_TAB_ICONS = new String[]{"\uf009", "\uf19d", "\uf3ed", "\uf028", "\uf013", "\uf11b"};
    private static final String[] SETTINGS_TAB_DESCRIPTIONS = new String[]{"Konfiguruj wyglad panelu", "Status i reset uczenia", "Wykrywanie zagrozen", "Powiadomienia i alarmy", "Opcje dodatkowe", "Ustawienia mini-gier"};
    private String[] DETECTION_SUBTABS;
    private static final float SIDEBAR_WIDTH = 140.0f;
    private int logoTextureId = 0;
    private int logoWidth = 24;
    private int logoHeight = 24;
    private float settingsRotation = 0.0f;
    private float settingsScale = 1.0f;
    private float settingsTargetRotation = 0.0f;
    private float settingsTargetScale = 1.0f;
    private static final float SETTINGS_ANIM_SPEED = 10.0f;
    private float stopButtonScale = 1.0f;
    private float stopButtonTargetScale = 1.0f;
    private Map<String, Float> playButtonScales = new HashMap<String, Float>();
    private Map<String, Float> playButtonTargetScales = new HashMap<String, Float>();
    private static final float BUTTON_ANIM_SPEED = 12.0f;
    private float animatedTabIndex = 0.0f;
    private float animatedDetectionTabIndex = 0.0f;
    private static final float TAB_ANIM_SPEED = 14.0f;
    private final MiniGameWindow miniGameWindow = new MiniGameWindow();
    private final TutorialWindow tutorialWindow = new TutorialWindow();
    private final List<WidgetType> activeWidgets = new ArrayList<WidgetType>(Arrays.asList(WidgetType.LEARNING, WidgetType.SEPARATOR, WidgetType.STATUS, WidgetType.SEPARATOR, WidgetType.RECOGNITION, WidgetType.SEPARATOR, WidgetType.LEVELS, WidgetType.SEPARATOR, WidgetType.ACCURACY, WidgetType.SEPARATOR, WidgetType.COAL_PRICE, WidgetType.SEPARATOR, WidgetType.CONTROLS));
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private SettingsCallback callback;
    private int draggedWidgetIndex = -1;
    private WidgetType draggedWidget = null;
    private final Map<DetectionType, DetectionActionSettings> detectionActionCache = new HashMap<DetectionType, DetectionActionSettings>();
    private final Map<DetectionType, ImBoolean> miningEventToggles = new HashMap<DetectionType, ImBoolean>();
    private List<CustomSoundInfo> customSounds = new ArrayList<CustomSoundInfo>();
    private final Map<String, ImString> renamingBuffers = new HashMap<String, ImString>();
    private int editingSoundIndex = -1;
    private int justStartedEditing = -1;
    private String[] customSoundNames = new String[0];

    public void showChatBlockingWarning() {
        this.showChatBlockingPopup = true;
    }

    public void hideChatBlockingWarning() {
        this.showChatBlockingPopup = false;
    }

    public void showBlackBackgroundWarning() {
        this.showBlackBackgroundWarning = true;
    }

    public void hideBlackBackgroundWarning() {
        this.showBlackBackgroundWarning = false;
    }

    public void setChatHasBlackBackground(boolean hasBlack) {
        this.chatHasBlackBackground = hasBlack;
        this.showBlackBackgroundWarning = !hasBlack;
    }

    public void setServerConnectionStatus(boolean connected, String status) {
        this.serverConnected = connected;
        this.serverConnectionStatus = status != null ? status : "";
    }

    public void setShutdownCallback(Runnable callback) {
        this.shutdownCallback = callback;
    }

    public void showBannedDialog(String reason, int exitCode) {
        this.banReason = reason != null ? reason : "Nieznany pow\u00f3d";
        this.banExitCode = exitCode;
        this.isBanned = true;
        if (this.shutdownCallback != null) {
            this.shutdownCallback.run();
        }
    }

    public void setLogoTexture(int textureId, int width, int height) {
        this.logoTextureId = textureId;
        this.logoWidth = width;
        this.logoHeight = height;
    }

    public void applyTheme() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(10.0f);
        style.setFrameRounding(6.0f);
        style.setGrabRounding(6.0f);
        style.setTabRounding(6.0f);
        style.setChildRounding(6.0f);
        style.setPopupRounding(8.0f);
        style.setScrollbarRounding(6.0f);
        style.setWindowPadding(14.0f, 14.0f);
        style.setFramePadding(10.0f, 5.0f);
        style.setItemSpacing(10.0f, 7.0f);
        style.setItemInnerSpacing(8.0f, 5.0f);
        style.setWindowBorderSize(1.0f);
        style.setFrameBorderSize(0.0f);
        style.setPopupBorderSize(1.0f);
        style.setColor(0, LifebotImGuiUI.COLOR_TEXT_PRIMARY.x, LifebotImGuiUI.COLOR_TEXT_PRIMARY.y, LifebotImGuiUI.COLOR_TEXT_PRIMARY.z, LifebotImGuiUI.COLOR_TEXT_PRIMARY.w);
        style.setColor(1, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, LifebotImGuiUI.COLOR_TEXT_DISABLED.w);
        style.setColor(2, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 1.0f);
        style.setColor(3, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.4f);
        style.setColor(4, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 1.0f);
        style.setColor(5, 0.3f, 0.3f, 0.3f, 0.6f);
        style.setColor(6, 0, 0, 0, 0);
        style.setColor(7, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
        style.setColor(8, 0.25f, 0.25f, 0.25f, 1.0f);
        style.setColor(9, 0.3f, 0.3f, 0.3f, 1.0f);
        style.setColor(10, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 0.95f);
        style.setColor(11, 0.2f, 0.2f, 0.2f, 0.95f);
        style.setColor(12, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 0.75f);
        style.setColor(13, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.95f);
        style.setColor(14, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 0.4f);
        style.setColor(15, 0.35f, 0.35f, 0.35f, 0.8f);
        style.setColor(16, 0.45f, 0.45f, 0.45f, 1.0f);
        style.setColor(17, 0.55f, 0.55f, 0.55f, 1.0f);
        style.setColor(18, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        style.setColor(19, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 0.9f);
        style.setColor(20, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        style.setColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
        style.setColor(22, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.z * 0.6f, 1.0f);
        style.setColor(23, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.5f, 1.0f);
        style.setColor(24, 0.12f, 0.18f, 0.28f, 1.0f);
        style.setColor(25, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.x * 0.35f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.z * 0.5f, 1.0f);
        style.setColor(26, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.4f, 1.0f);
        style.setColor(27, 0.35f, 0.35f, 0.35f, 0.8f);
        style.setColor(28, 0.5f, 0.5f, 0.5f, 0.9f);
        style.setColor(29, 0.65f, 0.65f, 0.65f, 1.0f);
        style.setColor(33, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 1.0f);
        style.setColor(34, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.z * 0.7f, 1.0f);
        style.setColor(35, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.x * 0.35f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_SECONDARY.z * 0.5f, 1.0f);
        style.setColor(36, LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, 1.0f);
        style.setColor(37, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 1.0f);
        style.setColor(42, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        style.setColor(43, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 0.85f);
    }

    public void toggleVisibility() {
        this.uiVisible = !this.uiVisible;
    }

    public boolean shouldRender() {
        return this.uiAlpha > 0.01f;
    }

    public void updateFade(float deltaTime) {
        float targetAlpha;
        float f = targetAlpha = this.uiVisible ? 1.0f : 0.0f;
        if (this.uiAlpha < targetAlpha) {
            this.uiAlpha = Math.min(this.uiAlpha + 8.0f * deltaTime, 1.0f);
        } else if (this.uiAlpha > targetAlpha) {
            this.uiAlpha = Math.max(this.uiAlpha - 8.0f * deltaTime, 0.0f);
        }
    }

    public void renderMainWindow() {
        if (this.isBanned) {
            this.renderBannedDialog();
            return;
        }
        if (this.tutorialWindow.isVisible()) {
            this.tutorialWindow.render();
            return;
        }
        float effectiveAlpha = this.uiAlpha * this.windowOpacity.get();
        ImGui.pushStyleVar(0, effectiveAlpha);
        float mainWindowWidth = 300.0f;
        float mainWindowHeight = 485.0f;
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        if (!this.mainWindowPositionSet && displayWidth >= 800.0f && displayHeight >= 600.0f) {
            float windowY = (displayHeight - mainWindowHeight) / 2.0f;
            ImGui.setNextWindowPos(10.0f, windowY, 1);
            this.mainWindowPositionSet = true;
            System.out.println("[UI DEBUG] Main window position SET - displaySize: " + displayWidth + "x" + displayHeight + ", windowY: " + windowY);
        } else if (!this.mainWindowPositionSet) {
            System.out.println("[UI DEBUG] Waiting for proper display size - current: " + displayWidth + "x" + displayHeight);
        }
        ImGui.setNextWindowSize(mainWindowWidth, mainWindowHeight, 4);
        int windowFlags = 41;
        if (ImGui.begin("##lifebot_main", windowFlags)) {
            this.renderCustomTitleBar();
            ImGui.spacing();
            for (WidgetType widget : this.activeWidgets) {
                this.renderWidget(widget);
            }
        }
        ImGui.end();
        if (this.showSettings) {
            this.renderSettingsWindow();
        }
        if (this.showDebug) {
            this.renderDebugWindow();
        }
        if (this.showChatBlockingPopup) {
            this.renderChatBlockingWatermark();
        }
        if (this.showBlackBackgroundWarning && !this.showChatBlockingPopup && !this.hideBlackChatWarning.get()) {
            this.renderBlackBackgroundWatermark();
        }
        if (!this.serverConnected) {
            this.renderServerDisconnectedBanner();
        }
        ImGui.popStyleVar();
    }

    private void renderChatBlockingWatermark() {
        float marginX = 10.0f;
        float marginY = 40.0f;
        float windowWidth = 460.0f;
        float windowHeight = 335.0f;
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() - windowWidth - marginX, marginY, 1);
        ImGui.setNextWindowSize(windowWidth, windowHeight, 1);
        int flags = 62;
        ImGui.pushStyleColor(2, 0.12f, 0.12f, 0.15f, 0.98f);
        ImGui.pushStyleColor(5, 1.0f, 0.3f, 0.3f, 1.0f);
        ImGui.pushStyleColor(10, 0.6f, 0.1f, 0.1f, 1.0f);
        ImGui.pushStyleColor(11, 0.7f, 0.15f, 0.15f, 1.0f);
        ImGui.pushStyleVar(4, 2.0f);
        ImGui.pushStyleVar(3, 6.0f);
        ImGui.pushStyleVar(2, 15.0f, 12.0f);
        if (ImGui.begin("[!] Chat zas\u0142ania ilo\u015b\u0107 w\u0119gla", flags)) {
            ImGui.pushStyleColor(0, 1.0f, 0.85f, 0.4f, 1.0f);
            ImGui.text("PROBLEM:");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, 0.9f, 0.9f, 0.9f, 1.0f);
            ImGui.textWrapped("Chat zas\u0142ania miejsce gdzie wy\u015bwietlana jest ilo\u015b\u0107 w\u0119gla. Bot zosta\u0142 wstrzymany.");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.4f, 0.9f, 0.4f, 1.0f);
            ImGui.text("JAK NAPRAWI\u0106:");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.85f, 0.85f, 0.85f, 1.0f);
            ImGui.bulletText("Wci\u015bnij ESC w grze");
            ImGui.bulletText("Wejd\u017a w: Ustawienia > Interface > Chat");
            ImGui.bulletText("Wybierz zak\u0142adk\u0119 'Layout'");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, 1.0f, 0.85f, 0.4f, 1.0f);
            ImGui.bulletText("Zmniejsz 'Scale'/'Width' lub zwi\u0119ksz 'Y-Offset'");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, 0.85f, 0.85f, 0.85f, 1.0f);
            ImGui.bulletText("Zamknij ustawienia MTA");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.4f, 0.9f, 0.4f, 1.0f);
            ImGui.textWrapped("Bot automatycznie wykryje zmian\u0119 i wznowi prac\u0119!");
            ImGui.popStyleColor();
        }
        ImGui.end();
        ImGui.popStyleVar(3);
        ImGui.popStyleColor(4);
    }

    private void renderBlackBackgroundWatermark() {
        float marginX = 10.0f;
        float marginY = 40.0f;
        float windowWidth = 480.0f;
        float windowHeight = 485.0f;
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() - windowWidth - marginX, marginY, 1);
        ImGui.setNextWindowSize(windowWidth, windowHeight, 1);
        int flags = 62;
        ImGui.pushStyleColor(2, 0.12f, 0.12f, 0.15f, 0.98f);
        ImGui.pushStyleColor(5, 1.0f, 0.5f, 0.0f, 1.0f);
        ImGui.pushStyleColor(10, 0.6f, 0.3f, 0.0f, 1.0f);
        ImGui.pushStyleColor(11, 0.7f, 0.35f, 0.0f, 1.0f);
        ImGui.pushStyleVar(4, 2.0f);
        ImGui.pushStyleVar(3, 6.0f);
        ImGui.pushStyleVar(2, 15.0f, 12.0f);
        if (ImGui.begin("[!] Czarny chat wymagany", flags)) {
            ImGui.pushStyleColor(0, 1.0f, 0.85f, 0.4f, 1.0f);
            ImGui.text("WYMAGANA KONFIGURACJA:");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, 0.9f, 0.9f, 0.9f, 1.0f);
            ImGui.textWrapped("Czarne tlo chatu jest wymagane do prawidlowego dzialania wszystkich detekcji opartych na chacie:");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f);
            ImGui.bulletText("Detekcja admina po nicku");
            ImGui.bulletText("Detekcja admina po kolorze nicku");
            ImGui.bulletText("Detekcja prywatnych wiadomosci (PM)");
            ImGui.bulletText("Detekcja wzmianek twojego nicku");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.4f, 0.9f, 0.4f, 1.0f);
            ImGui.text("JAK NAPRAWIC:");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.85f, 0.85f, 0.85f, 1.0f);
            ImGui.bulletText("Wcisnij ESC w grze");
            ImGui.bulletText("Wejdz w: Ustawienia > Interfejs > Kolory");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, 1.0f, 0.85f, 0.4f, 1.0f);
            ImGui.bulletText("Tlo czatu -> Przezroczystosc [255]");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.spacing();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.textWrapped("Detekcje oparte na chacie sa wylaczone do czasu ustawienia czarnego tla.");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.4f, 0.9f, 0.4f, 1.0f);
            ImGui.textWrapped("Bot automatycznie wykryje zmiane po ustawieniu.");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.textWrapped("\uf05a Mozesz wylaczyc to ostrzezenie w: Ustawienia > Dodatkowe");
            ImGui.popStyleColor();
        }
        ImGui.end();
        ImGui.popStyleVar(3);
        ImGui.popStyleColor(4);
    }

    private void renderServerDisconnectedBanner() {
        float bannerWidth = 450.0f;
        float bannerHeight = 80.0f;
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        ImGui.setNextWindowPos((displayWidth - bannerWidth) / 2.0f, 15.0f, 1);
        ImGui.setNextWindowSize(bannerWidth, bannerHeight, 1);
        int flags = 4159;
        ImGui.pushStyleColor(2, 0.35f, 0.08f, 0.08f, 0.98f);
        ImGui.pushStyleColor(5, 1.0f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleVar(4, 3.0f);
        ImGui.pushStyleVar(3, 8.0f);
        ImGui.pushStyleVar(2, 20.0f, 15.0f);
        if (ImGui.begin("##server_disconnected", flags)) {
            float time = (float)(System.currentTimeMillis() % 2000L) / 2000.0f;
            float pulse = (float)((double)0.7f + (double)0.3f * Math.sin((double)time * Math.PI * 2.0));
            ImGui.pushStyleColor(0, 1.0f * pulse, 0.3f * pulse, 0.3f * pulse, 1.0f);
            float textWidth = ImGui.calcTextSize((String)"\uf071 BRAK POLACZENIA Z SERWEREM").x;
            ImGui.setCursorPosX((bannerWidth - textWidth) / 2.0f - 10.0f);
            ImGui.text("\uf071 BRAK POLACZENIA Z SERWEREM");
            ImGui.popStyleColor();
            String statusText = this.serverConnectionStatus.isEmpty() ? "Trwa proba ponownego polaczenia..." : this.serverConnectionStatus;
            ImGui.pushStyleColor(0, 0.9f, 0.9f, 0.9f, 1.0f);
            float statusWidth = ImGui.calcTextSize((String)statusText).x;
            ImGui.setCursorPosX((bannerWidth - statusWidth) / 2.0f - 10.0f);
            ImGui.text(statusText);
            ImGui.popStyleColor();
        }
        ImGui.end();
        ImGui.popStyleVar(3);
        ImGui.popStyleColor(2);
    }

    private void renderBannedDialog() {
        float dialogWidth = 500.0f;
        float dialogHeight = 280.0f;
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        ImGui.getBackgroundDrawList().addRectFilled(0.0f, 0.0f, displayWidth, displayHeight, ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.85f));
        ImGui.setNextWindowPos((displayWidth - dialogWidth) / 2.0f, (displayHeight - dialogHeight) / 2.0f, 1);
        ImGui.setNextWindowSize(dialogWidth, dialogHeight, 1);
        int flags = 63;
        ImGui.pushStyleColor(2, 0.12f, 0.06f, 0.06f, 0.98f);
        ImGui.pushStyleColor(5, 0.9f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleVar(4, 3.0f);
        ImGui.pushStyleVar(3, 12.0f);
        ImGui.pushStyleVar(2, 30.0f, 25.0f);
        if (ImGui.begin("##banned_dialog", flags)) {
            float availWidth = ImGui.getContentRegionAvailX();
            ImGui.pushStyleColor(0, 1.0f, 0.25f, 0.25f, 1.0f);
            String title = "\uf05e  KONTO ZABLOKOWANE";
            float titleWidth = ImGui.calcTextSize((String)title).x;
            ImGui.setCursorPosX((availWidth - titleWidth) / 2.0f + ImGui.getStyle().getWindowPaddingX());
            ImGui.text(title);
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.spacing();
            ImDrawList drawList = ImGui.getWindowDrawList();
            float sepY = ImGui.getCursorScreenPosY();
            float sepX = ImGui.getWindowPosX() + ImGui.getStyle().getWindowPaddingX();
            drawList.addLine(sepX, sepY, sepX + availWidth, sepY, ImGui.colorConvertFloat4ToU32(0.5f, 0.15f, 0.15f, 1.0f), 1.0f);
            ImGui.spacing();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.95f, 0.95f, 0.95f, 1.0f);
            String reasonText = "Powod: " + this.banReason;
            float reasonWidth = ImGui.calcTextSize((String)reasonText).x;
            ImGui.setCursorPosX((availWidth - reasonWidth) / 2.0f + ImGui.getStyle().getWindowPaddingX());
            ImGui.text(reasonText);
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.7f, 0.7f, 0.7f, 1.0f);
            String infoLine1 = "Jesli uwazasz, ze to blad, utworz zgloszenie:";
            float info1Width = ImGui.calcTextSize((String)infoLine1).x;
            ImGui.setCursorPosX((availWidth - info1Width) / 2.0f + ImGui.getStyle().getWindowPaddingX());
            ImGui.text(infoLine1);
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, 0.4f, 0.7f, 1.0f, 1.0f);
            String url = "https://life-bot.pl/tickets";
            float urlWidth = ImGui.calcTextSize((String)url).x;
            ImGui.setCursorPosX((availWidth - urlWidth) / 2.0f + ImGui.getStyle().getWindowPaddingX());
            ImGui.text(url);
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.spacing();
            ImGui.spacing();
            float buttonWidth = 140.0f;
            float buttonHeight = 38.0f;
            ImGui.setCursorPosX((availWidth - buttonWidth) / 2.0f + ImGui.getStyle().getWindowPaddingX());
            ImGui.pushStyleColor(21, 0.7f, 0.15f, 0.15f, 1.0f);
            ImGui.pushStyleColor(22, 0.85f, 0.25f, 0.25f, 1.0f);
            ImGui.pushStyleColor(23, 0.55f, 0.1f, 0.1f, 1.0f);
            ImGui.pushStyleVar(12, 8.0f);
            if (ImGui.button("Zamknij##ban_ok", buttonWidth, buttonHeight)) {
                System.exit(this.banExitCode);
            }
            ImGui.popStyleVar();
            ImGui.popStyleColor(3);
        }
        ImGui.end();
        ImGui.popStyleVar(3);
        ImGui.popStyleColor(2);
    }

    private void renderWidget(WidgetType type) {
        switch (type.ordinal()) {
            case 0: {
                this.renderLearningSection();
                break;
            }
            case 1: {
                this.renderStatusSection();
                break;
            }
            case 2: {
                this.renderRecognitionSection();
                break;
            }
            case 3: {
                this.renderLevelsSection();
                break;
            }
            case 4: {
                this.renderAccuracySection();
                break;
            }
            case 5: {
                this.renderCoalPriceSection();
                break;
            }
            case 6: {
                this.renderSeparatorWidget();
                break;
            }
            case 7: {
                this.renderControlButtons();
            }
        }
    }

    private void renderCustomTitleBar() {
        float windowWidth = ImGui.getWindowWidth();
        float titleBarHeight = 48.0f;
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowPosX = ImGui.getWindowPosX();
        float windowPosY = ImGui.getWindowPosY();
        float startY = ImGui.getCursorPosY();
        float deltaTime = ImGui.getIO().getDeltaTime();
        float logoDisplayHeight = 32.0f;
        float logoDisplayWidth = logoDisplayHeight * ((float)this.logoWidth / (float)this.logoHeight);
        float logoCenterY = startY + titleBarHeight / 2.0f;
        if (this.logoTextureId > 0) {
            ImGui.setCursorPosX(10.0f);
            ImGui.setCursorPosY(logoCenterY - logoDisplayHeight / 2.0f);
            ImGui.image((long)this.logoTextureId, logoDisplayWidth, logoDisplayHeight);
        }
        float buttonBaseSize = 24.0f;
        float buttonSize = buttonBaseSize * this.settingsScale;
        float buttonCenterX = windowWidth - 40.0f;
        float buttonX = buttonCenterX - buttonSize / 2.0f;
        float buttonY = logoCenterY - buttonSize / 2.0f;
        ImGui.setCursorPos(buttonX, buttonY);
        ImGui.pushID("settings_btn");
        ImGui.invisibleButton("##settings_invisible", buttonSize, buttonSize);
        boolean isHovered = ImGui.isItemHovered();
        boolean clicked = ImGui.isItemClicked();
        ImGui.popID();
        if (isHovered) {
            this.settingsTargetScale = 1.3f;
            this.settingsTargetRotation += deltaTime * 3.0f;
        } else {
            this.settingsTargetScale = 1.0f;
            this.settingsTargetRotation = 0.0f;
        }
        this.settingsScale += (this.settingsTargetScale - this.settingsScale) * 10.0f * deltaTime;
        this.settingsRotation += (this.settingsTargetRotation - this.settingsRotation) * 10.0f * deltaTime;
        float centerX = windowPosX + buttonCenterX;
        float centerY = windowPosY + logoCenterY;
        float size = buttonBaseSize * this.settingsScale * 0.5f;
        float iconAlpha = isHovered ? 1.0f : 0.7f;
        float finalAlpha = iconAlpha * this.getEffectiveAlpha();
        float bodyR = size * 0.65f;
        float toothW = size * 0.22f;
        float toothH = size * 0.12f;
        int numTeeth = 10;
        int iconColor = ImGui.colorConvertFloat4ToU32(1.0f, 1.0f, 1.0f, finalAlpha);
        int bgCol = ImGui.colorConvertFloat4ToU32(LifebotImGuiUI.COLOR_BG_DARK.x, LifebotImGuiUI.COLOR_BG_DARK.y, LifebotImGuiUI.COLOR_BG_DARK.z, this.getEffectiveAlpha());
        for (int i = 0; i < numTeeth; ++i) {
            float angle = this.settingsRotation + (float)((double)i * Math.PI * 2.0 / (double)numTeeth);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float perpX = -sin;
            float perpY = cos;
            float inner = bodyR - 1.0f;
            float outer = bodyR + toothH;
            float hw = toothW / 2.0f;
            drawList.addQuadFilled(centerX + cos * inner + perpX * hw, centerY + sin * inner + perpY * hw, centerX + cos * inner - perpX * hw, centerY + sin * inner - perpY * hw, centerX + cos * outer - perpX * hw, centerY + sin * outer - perpY * hw, centerX + cos * outer + perpX * hw, centerY + sin * outer + perpY * hw, iconColor);
            float tipX = centerX + cos * outer;
            float tipY = centerY + sin * outer;
            drawList.addCircleFilled(tipX, tipY, hw, iconColor, 8);
        }
        drawList.addCircleFilled(centerX, centerY, bodyR, iconColor, 24);
        drawList.addCircleFilled(centerX, centerY, size * 0.28f, bgCol, 16);
        if (clicked) {
            this.showSettings = !this.showSettings;
            this.showSettingsPtr.set(this.showSettings);
        }
        if (isHovered) {
            ImGui.setTooltip("Ustawienia");
        }
        ImGui.setCursorPosY(startY + titleBarHeight + 8.0f);
    }

    private void renderHeader() {
    }

    private void renderLearningSection() {
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\uf19d Uczenie");
        ImGui.popStyleColor();
        ImGui.spacing();
        float progress = (float)this.collectedSamples / (float)this.requiredSamples;
        if (this.isLearningReady) {
            ImGui.pushStyleColor(42, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.progressBar(1.0f, -1.0f, 20.0f, "Gotowy!");
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text("\uf058 Mozesz uruchomic bota");
            ImGui.popStyleColor();
        } else {
            ImGui.pushStyleColor(42, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
            ImGui.progressBar(progress, -1.0f, 20.0f, String.format("%d/%d", this.collectedSamples, this.requiredSamples));
            ImGui.popStyleColor();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text("\uf11c ALT - nagrywanie");
            ImGui.popStyleColor();
        }
    }

    private void renderStatusSection() {
        ImGui.text("Status:");
        ImGui.sameLine();
        ImGui.pushStyleColor(0, this.statusColor.x, this.statusColor.y, this.statusColor.z, this.statusColor.w);
        ImGui.text(this.statusText);
        ImGui.popStyleColor();
    }

    private void renderRecognitionSection() {
        ImGui.alignTextToFramePadding();
        ImGui.text("Wykryte:");
        ImGui.sameLine();
        for (int i = 0; i < 3; ++i) {
            if (i > 0) {
                ImGui.sameLine();
            }
            if (i < this.recognizedLetters.length && i < this.recognizedCorrect.length) {
                ImVec4 color = this.recognizedCorrect[i] ? COLOR_ACCENT_GREEN : COLOR_ACCENT_RED;
                ImGui.pushStyleColor(0, color.x, color.y, color.z, color.w);
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
                String letter = String.valueOf(this.recognizedLetters[i]);
                ImGui.button(letter, 30.0f, 30.0f);
                ImGui.popStyleColor(2);
                continue;
            }
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 0.5f);
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.5f);
            ImGui.button("-", 30.0f, 30.0f);
            ImGui.popStyleColor(2);
        }
    }

    private void renderLevelsSection() {
        ImGui.text("Twoj stopien:");
        ImGui.sameLine();
        ImGui.pushStyleColor(0, this.yourLevelColor.x, this.yourLevelColor.y, this.yourLevelColor.z, this.yourLevelColor.w);
        ImGui.text(this.yourLevel);
        ImGui.popStyleColor();
        ImGui.text("Wymagany stopien:");
        ImGui.sameLine();
        ImGui.pushStyleColor(0, this.requiredLevelColor.x, this.requiredLevelColor.y, this.requiredLevelColor.z, this.requiredLevelColor.w);
        ImGui.text(this.requiredLevel);
        ImGui.popStyleColor();
    }

    private void renderAccuracySection() {
        ImGui.text("Precyzja:");
        ImGui.sameLine();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PURPLE.x, LifebotImGuiUI.COLOR_ACCENT_PURPLE.y, LifebotImGuiUI.COLOR_ACCENT_PURPLE.z, 1.0f);
        ImGui.text(this.accuracy);
        ImGui.popStyleColor();
    }

    private void renderCoalPriceSection() {
        ImGui.text("Cena wegla:");
        ImGui.sameLine();
        if (this.coalPriceValue >= 11.5) {
            ImGui.pushStyleColor(0, 1.0f, 0.84f, 0.0f, 1.0f);
            ImGui.text("\u2605 " + this.coalPrice + " \u2605");
            ImGui.popStyleColor();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Swietna cena wegla! Warto kopac!");
            }
        } else if (this.coalPriceValue >= 11.4) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text(this.coalPrice);
            ImGui.popStyleColor();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Dobra cena wegla!");
            }
        } else {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text(this.coalPrice);
            ImGui.popStyleColor();
        }
    }

    private void renderSeparatorWidget() {
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
    }

    private void renderControlButtons() {
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
        this.centerText("Wcisnij END aby wlaczyc/wylaczyc bota");
        ImGui.popStyleColor();
    }

    private void renderSettingsWindow() {
        float settingsWidth = 780.0f;
        float settingsHeight = 640.0f;
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        float windowX = 100.0f;
        float windowY = 100.0f;
        if (displayWidth > 100.0f && displayHeight > 100.0f) {
            windowX = (displayWidth - settingsWidth) / 2.0f;
            windowY = (displayHeight - settingsHeight) / 2.0f;
        }
        ImGui.setNextWindowPos(windowX, windowY, 4);
        ImGui.setNextWindowSize(settingsWidth, settingsHeight, 4);
        if (ImGui.begin("Ustawienia", this.showSettingsPtr, 56)) {
            this.showSettings = this.showSettingsPtr.get();
            float sidebarWidth = 140.0f;
            float availHeight = ImGui.getContentRegionAvailY();
            ImGui.beginGroup();
            float exitButtonHeight = 28.0f;
            float exitButtonPadding = 8.0f;
            float sidebarHeight = availHeight - exitButtonHeight - exitButtonPadding;
            ImGui.pushStyleColor(3, LifebotImGuiUI.COLOR_BG_DARK.x * 0.85f, LifebotImGuiUI.COLOR_BG_DARK.y * 0.85f, LifebotImGuiUI.COLOR_BG_DARK.z * 0.85f, 1.0f);
            ImGui.pushStyleVar(2, 4.0f, 8.0f);
            ImGui.pushStyleVar(14, 0.0f, 2.0f);
            ImGui.beginChild("##sidebar", sidebarWidth, sidebarHeight, true, 8);
            float deltaTime = ImGui.getIO().getDeltaTime();
            this.animatedTabIndex += ((float)this.currentSettingsTab - this.animatedTabIndex) * 14.0f * deltaTime;
            float itemHeight = 36.0f;
            float buttonWidth = ImGui.getContentRegionAvailX();
            for (int i = 0; i < SETTINGS_TABS.length; ++i) {
                boolean isActive;
                boolean bl = isActive = this.currentSettingsTab == i;
                if (isActive) {
                    ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 0.2f);
                    ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 0.25f);
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
                } else {
                    ImGui.pushStyleColor(21, 0, 0, 0, 0);
                    ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.3f);
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
                }
                ImGui.pushStyleVar(23, 0.1f, 0.5f);
                ImGui.pushStyleVar(12, 4.0f);
                String buttonLabel = SETTINGS_TAB_ICONS[i] + "  " + SETTINGS_TABS[i] + "##tab" + i;
                if (ImGui.button(buttonLabel, buttonWidth, itemHeight)) {
                    this.currentSettingsTab = i;
                }
                ImGui.popStyleVar(2);
                ImGui.popStyleColor(3);
            }
            ImGui.endChild();
            ImGui.popStyleVar(2);
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(21, 0.5f, 0.15f, 0.15f, 1.0f);
            ImGui.pushStyleColor(22, 0.65f, 0.2f, 0.2f, 1.0f);
            ImGui.pushStyleColor(23, 0.4f, 0.1f, 0.1f, 1.0f);
            ImGui.pushStyleVar(23, 0.5f, 0.5f);
            ImGui.pushStyleVar(12, 4.0f);
            if (ImGui.button("\uf011  Zamknij##exit", sidebarWidth, exitButtonHeight)) {
                System.exit(0);
            }
            ImGui.popStyleVar(2);
            ImGui.popStyleColor(3);
            ImGui.endGroup();
            ImGui.sameLine(0.0f, 8.0f);
            float contentWidth = ImGui.getContentRegionAvailX();
            ImGui.pushStyleVar(2, 12.0f, 12.0f);
            ImGui.pushStyleColor(3, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.3f);
            ImGui.beginChild("##tab_content", contentWidth, availHeight, true, 0);
            switch (this.currentSettingsTab) {
                case 0: {
                    this.renderWidgetsTab();
                    break;
                }
                case 1: {
                    this.renderLearningTab();
                    break;
                }
                case 2: {
                    this.renderDetectionTab();
                    break;
                }
                case 3: {
                    this.renderSoundsTab();
                    break;
                }
                case 4: {
                    this.renderAdditionalTab();
                    break;
                }
                case 5: {
                    this.renderGameTab();
                }
            }
            ImGui.endChild();
            ImGui.popStyleColor();
            ImGui.popStyleVar();
        }
        ImGui.end();
    }

    private void renderWidgetsTab() {
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("Przeciagnij elementy aby zmienic kolejnosc. Dodaj nowe z prawej strony.");
        ImGui.popStyleColor();
        ImGui.spacing();
        float availWidth = ImGui.getContentRegionAvailX();
        float columnWidth = (availWidth - 10.0f) / 2.0f;
        ImGui.beginGroup();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("Aktywne:");
        ImGui.popStyleColor();
        ImGui.spacing();
        int indexToRemove = -1;
        float itemHeight = 26.0f;
        ImDrawList drawList = ImGui.getWindowDrawList();
        int swapTarget = -1;
        for (int i = 0; i < this.activeWidgets.size(); ++i) {
            boolean mouseOverItem;
            WidgetType widget = this.activeWidgets.get(i);
            boolean isBeingDragged = widget == this.draggedWidget;
            ImGui.pushID(i);
            float startX = ImGui.getCursorScreenPosX();
            float startY = ImGui.getCursorScreenPosY();
            float mouseX = ImGui.getMousePosX();
            float mouseY = ImGui.getMousePosY();
            float xBtnLeft = startX + columnWidth - 24.0f;
            boolean mouseOverX = mouseX >= xBtnLeft && mouseX <= startX + columnWidth && mouseY >= startY && mouseY <= startY + itemHeight;
            boolean bl = mouseOverItem = mouseX >= startX && mouseX <= startX + columnWidth && mouseY >= startY && mouseY <= startY + itemHeight;
            int bgColor = isBeingDragged ? this.colorU32(LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.35f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.35f, 1.0f) : (mouseOverItem && this.draggedWidget == null ? this.colorU32(COLOR_BG_LIGHT, 0.6f) : this.colorU32(COLOR_BG_MEDIUM, 0.4f));
            drawList.addRectFilled(startX, startY, startX + columnWidth, startY + itemHeight, bgColor, 4.0f);
            ImGui.pushStyleColor(24, 0, 0, 0, 0);
            ImGui.pushStyleColor(25, 0, 0, 0, 0);
            ImGui.pushStyleColor(26, 0, 0, 0, 0);
            boolean clicked = ImGui.selectable("##item", false, 0, columnWidth, itemHeight);
            ImGui.popStyleColor(3);
            boolean isHovered = ImGui.isItemHovered();
            if (clicked && mouseOverX) {
                indexToRemove = i;
            }
            float handleX = startX + 4.0f;
            float handleY = startY + (itemHeight - ImGui.getTextLineHeight()) / 2.0f;
            int handleColor = isBeingDragged ? this.colorU32(COLOR_ACCENT_PRIMARY) : this.colorU32(COLOR_TEXT_SECONDARY, isHovered ? 0.7f : 0.4f);
            drawList.addText(handleX, handleY, handleColor, "\uf58e");
            float textY = startY + (itemHeight - ImGui.getTextLineHeight()) / 2.0f;
            int textColor = this.colorU32(COLOR_TEXT_PRIMARY);
            String shortName = widget.displayName.length() > 18 ? widget.displayName.substring(0, 17) + ".." : widget.displayName;
            drawList.addText(startX + 20.0f, textY, textColor, shortName);
            float xCenterX = startX + columnWidth - 14.0f;
            float xCenterY = startY + (itemHeight - ImGui.getTextLineHeight()) / 2.0f;
            int xColor = mouseOverX ? this.colorU32(COLOR_ACCENT_RED) : this.colorU32(COLOR_TEXT_DISABLED, 0.5f);
            drawList.addText(xCenterX, xCenterY, xColor, "\uf00d");
            if (isHovered && !mouseOverX && ImGui.isMouseClicked(0)) {
                this.draggedWidgetIndex = i;
                this.draggedWidget = widget;
            }
            if (this.draggedWidgetIndex != -1 && this.draggedWidgetIndex != i && mouseOverItem && ImGui.isMouseDown(0)) {
                swapTarget = i;
            }
            if (isHovered && !mouseOverX && this.draggedWidgetIndex == -1) {
                ImGui.setTooltip(widget.displayName);
            }
            ImGui.popID();
        }
        if (swapTarget != -1 && this.draggedWidgetIndex != -1) {
            WidgetType tmp = this.activeWidgets.get(this.draggedWidgetIndex);
            this.activeWidgets.set(this.draggedWidgetIndex, this.activeWidgets.get(swapTarget));
            this.activeWidgets.set(swapTarget, tmp);
            this.draggedWidgetIndex = swapTarget;
            if (this.callback != null) {
                this.callback.onSettingChanged("widgetOrder", this.activeWidgets);
            }
        }
        if (!ImGui.isMouseDown(0)) {
            this.draggedWidgetIndex = -1;
            this.draggedWidget = null;
        }
        if (indexToRemove != -1) {
            this.activeWidgets.remove(indexToRemove);
            if (this.callback != null) {
                this.callback.onSettingChanged("widgetOrder", this.activeWidgets);
            }
        }
        ImGui.endGroup();
        ImGui.sameLine(columnWidth + 15.0f);
        ImGui.beginGroup();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("Dostepne:");
        ImGui.popStyleColor();
        ImGui.spacing();
        int nonSeparatorCount = 0;
        int separatorCount = 0;
        for (WidgetType w : this.activeWidgets) {
            if (w == WidgetType.SEPARATOR) {
                ++separatorCount;
                continue;
            }
            ++nonSeparatorCount;
        }
        int maxSeparators = Math.max(0, nonSeparatorCount - 1);
        for (WidgetType widget : WidgetType.values()) {
            String buttonLabel;
            boolean canAdd = false;
            Object tooltip = widget.description;
            if (widget == WidgetType.SEPARATOR) {
                if (separatorCount >= maxSeparators) continue;
                canAdd = true;
                buttonLabel = "\uf067 Separator (" + separatorCount + "/" + maxSeparators + ")##add_sep";
                tooltip = widget.description + " (max: " + maxSeparators + ")";
            } else {
                if (this.activeWidgets.contains((Object)widget)) continue;
                canAdd = true;
                String shortName = widget.displayName.length() > 15 ? widget.displayName.substring(0, 14) + ".." : widget.displayName;
                buttonLabel = "\uf067 " + shortName + "##add_" + widget.name();
            }
            if (!canAdd) continue;
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.2f, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.4f, 1.0f);
            ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.6f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.6f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.6f, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            if (ImGui.button(buttonLabel, columnWidth, 24.0f)) {
                this.activeWidgets.add(widget);
                if (this.callback != null) {
                    this.callback.onSettingChanged("widgetOrder", this.activeWidgets);
                }
            }
            ImGui.popStyleColor(4);
            if (!ImGui.isItemHovered()) continue;
            ImGui.setTooltip((String)tooltip);
        }
        ImGui.endGroup();
        ImGui.spacing();
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.3f, 1.0f);
        if (ImGui.button("Przywroc domyslne##reset", -1.0f, 26.0f)) {
            this.activeWidgets.clear();
            this.activeWidgets.addAll(Arrays.asList(WidgetType.LEARNING, WidgetType.STATUS, WidgetType.RECOGNITION, WidgetType.LEVELS, WidgetType.ACCURACY, WidgetType.COAL_PRICE, WidgetType.CONTROLS));
            if (this.callback != null) {
                this.callback.onSettingChanged("widgetOrder", this.activeWidgets);
            }
        }
        ImGui.popStyleColor(2);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Przywroc domyslna konfiguracje panelu");
        }
    }

    private void renderLearningTab() {
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\uf19d System uczenia");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("Bot uczy sie z Twojego stylu kopania. Nie musisz ustawiac zadnych opoznien - bot nauczy sie ich automatycznie obserwujac jak kopiesz.");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.text("\uf201 Status uczenia:");
        ImGui.sameLine();
        if (this.isLearningReady) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text("\uf058 Gotowy");
            ImGui.popStyleColor();
        } else {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
            ImGui.text("\uf110 Uczenie " + this.collectedSamples + "/" + this.requiredSamples);
            ImGui.popStyleColor();
        }
        ImGui.spacing();
        float progress = (float)this.collectedSamples / (float)this.requiredSamples;
        ImGui.progressBar(progress, -1.0f, 0.0f, "");
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\uf05a Jak uczyc bota:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.text("  \uf11c  Kliknij ALT aby rozpoczac kopanie");
        ImGui.text("  \uf25a  Klikaj Q/E jak normalnie kopiesz");
        ImGui.text("  \uf03d  Bot nagrywa Twoje czasy");
        ImGui.text("  \uf560  Po " + this.requiredSamples + " sesjach bot jest gotowy");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.7f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.7f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.7f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.85f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.85f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.85f, 1.0f);
        ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f);
        if (ImGui.button("\uf01e Resetuj uczenie", -1.0f, 30.0f) && this.callback != null) {
            this.callback.onResetLearning();
        }
        ImGui.popStyleColor(3);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Wyczysc nauczone dane i zacznij od nowa");
        }
    }

    private void renderDetectionTab() {
        boolean chatBasedDisabled;
        float currentY;
        ImGui.spacing();
        if (this.DETECTION_SUBTABS == null) {
            this.DETECTION_SUBTABS = new String[]{"\uf505 Admin", "\uf071 Warn", "\uf466 Ekwip.", "\uf086 Chat", "\uf807 Kopanie", "\uf05a Info"};
        }
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\uf3ed System detekcji");
        ImGui.popStyleColor();
        ImGui.spacing();
        if (ImGui.checkbox("Wlacz detekcje##master_toggle", this.detectionEnabled) && this.callback != null) {
            this.callback.onSettingChanged("detectionEnabled", this.detectionEnabled.get());
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        float tabAreaWidth = ImGui.getContentRegionAvailX();
        float tabWidth = tabAreaWidth / (float)this.DETECTION_SUBTABS.length;
        float tabHeight = 28.0f;
        float tabBarX = ImGui.getCursorScreenPosX();
        float tabBarY = ImGui.getCursorScreenPosY();
        float deltaTime = ImGui.getIO().getDeltaTime();
        ImDrawList drawList = ImGui.getWindowDrawList();
        this.animatedDetectionTabIndex += ((float)this.currentDetectionSubTab - this.animatedDetectionTabIndex) * 14.0f * deltaTime;
        drawList.addRectFilled(tabBarX, tabBarY, tabBarX + tabAreaWidth, tabBarY + tabHeight, this.colorU32(COLOR_BG_DARK), 6.0f);
        float pillX = this.animatedDetectionTabIndex * tabWidth + tabBarX;
        float pillPadding = 3.0f;
        drawList.addRectFilled(pillX + pillPadding, tabBarY + pillPadding, pillX + tabWidth - pillPadding, tabBarY + tabHeight - pillPadding, this.colorU32(COLOR_BG_LIGHT), 4.0f);
        for (int i = 0; i < this.DETECTION_SUBTABS.length; ++i) {
            float startX = tabBarX + (float)i * tabWidth;
            float textWidth = ImGui.calcTextSize((String)this.DETECTION_SUBTABS[i]).x;
            float textX = startX + (tabWidth - textWidth) / 2.0f;
            float textY = tabBarY + (tabHeight - ImGui.getTextLineHeight()) / 2.0f;
            boolean isActive = this.currentDetectionSubTab == i;
            int textColor = isActive ? this.colorU32(COLOR_ACCENT_PRIMARY) : this.colorU32(COLOR_TEXT_SECONDARY);
            drawList.addText(textX, textY, textColor, this.DETECTION_SUBTABS[i]);
            ImGui.setCursorScreenPos(startX, tabBarY);
            if (!ImGui.invisibleButton("dtab_" + i, tabWidth, tabHeight)) continue;
            this.currentDetectionSubTab = i;
        }
        float newY = tabBarY - ImGui.getWindowPosY() + tabHeight + 8.0f + ImGui.getScrollY();
        if (newY > (currentY = ImGui.getCursorPosY())) {
            ImGui.dummy(0.0f, newY - currentY);
        }
        ImGui.spacing();
        if (!this.detectionEnabled.get()) {
            ImGui.beginDisabled();
        }
        boolean bl = chatBasedDisabled = !this.chatHasBlackBackground && (this.currentDetectionSubTab == 0 || this.currentDetectionSubTab == 3);
        if (chatBasedDisabled) {
            this.renderChatRequiresBlackBackgroundWarning();
            ImGui.beginDisabled();
        }
        switch (this.currentDetectionSubTab) {
            case 0: {
                this.renderAdminDetectionPanel();
                break;
            }
            case 1: {
                this.renderAdminWarningPanel();
                break;
            }
            case 2: {
                this.renderInventoryDetectionPanel();
                break;
            }
            case 3: {
                this.renderChatDetectionPanel();
                break;
            }
            case 4: {
                this.renderMiningDetectionPanel();
                break;
            }
            case 5: {
                this.renderDetectionInfoPanel();
            }
        }
        if (chatBasedDisabled) {
            ImGui.endDisabled();
        }
        if (!this.detectionEnabled.get()) {
            ImGui.endDisabled();
        }
    }

    private void renderChatRequiresBlackBackgroundWarning() {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float startX = ImGui.getCursorScreenPosX();
        float startY = ImGui.getCursorScreenPosY();
        float width = ImGui.getContentRegionAvailX();
        float padding = 10.0f;
        drawList.addRectFilled(startX, startY, startX + width, startY + 60.0f, this.colorU32(0.6f, 0.3f, 0.0f, 0.3f), 6.0f);
        drawList.addRect(startX, startY, startX + width, startY + 60.0f, this.colorU32(1.0f, 0.5f, 0.0f, 0.8f), 6.0f, 0, 2.0f);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + padding);
        ImGui.indent(padding);
        ImGui.pushStyleColor(0, 1.0f, 0.7f, 0.3f, 1.0f);
        ImGui.text("\uf071 Wymagany czarny chat");
        ImGui.popStyleColor();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("Te detekcje sa wylaczone. Ustaw czarne tlo chatu w MTA.");
        ImGui.popStyleColor();
        ImGui.unindent(padding);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + padding + 5.0f);
        ImGui.spacing();
    }

    private void renderDetectionInfoPanel() {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        this.renderInfoCard("\uf06e", "Jak dziala detekcja?", COLOR_ACCENT_PRIMARY, new String[]{"Bot skanuje ekran gry w poszukiwaniu roznych zdarzen.", "Mozesz ustawic reakcje: zatrzymanie bota, alert lub dzwiek.", "Kazda detekcja ma wlasne ustawienia - wlacz co potrzebujesz."});
        ImGui.spacing();
        this.renderInfoCard("\uf0e7", "Dostepne reakcje", COLOR_ACCENT_GREEN, new String[]{"\uf04c  Stop - zatrzymuje bota do recznego wznowienia", "\uf0f3  Alert - pokazuje powiadomienie na ekranie", "\uf028  Dzwiek - odtwarza wybrany dzwiek"});
        ImGui.spacing();
        this.renderInfoCard("\uf001", "Tryby dzwiekow", COLOR_ACCENT_PURPLE, new String[]{"\uf04b  Pojedynczy - gra dzwiek raz", "\uf01e  Powtarzajacy - gra co X sekund", "\uf2f1  Auto - gra az ruszysz mysza/klawiatura"});
        ImGui.spacing();
        this.renderInfoCard("\uf071", "Uwaga!", COLOR_ACCENT_YELLOW, new String[]{"Dzwieki (GLOSNE!) moga byc bardzo glosne!", "OCR moze obciazac procesor.", "Ustaw nizszy volume dla glosnych dzwiekow."});
    }

    private void renderInfoCard(String icon, String title, ImVec4 accentColor, String[] lines) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        float cardStartY = ImGui.getCursorScreenPosY();
        float cardStartX = ImGui.getCursorScreenPosX();
        float lineHeight = 22.0f;
        float cardHeight = 38.0f + (float)lines.length * lineHeight;
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + windowWidth, cardStartY + cardHeight, this.colorU32(COLOR_BG_LIGHT, 0.4f), 8.0f);
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + 4.0f, cardStartY + cardHeight, this.colorU32(accentColor), 4.0f);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 8.0f);
        ImGui.pushStyleColor(0, accentColor.x, accentColor.y, accentColor.z, 1.0f);
        ImGui.text(icon + " " + title);
        ImGui.popStyleColor();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        for (String line : lines) {
            ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
            ImGui.text(line);
        }
        ImGui.popStyleColor();
        float newY = cardStartY - ImGui.getWindowPosY() + cardHeight + 5.0f + ImGui.getScrollY();
        float currentY = ImGui.getCursorPosY();
        if (newY > currentY) {
            ImGui.dummy(0.0f, newY - currentY);
        }
    }

    private boolean renderCheckboxOption(String label, ImBoolean value, String settingKey) {
        if (ImGui.checkbox(label + "##" + settingKey, value)) {
            if (this.callback != null) {
                this.callback.onSettingChanged(settingKey, value.get());
            }
            return true;
        }
        return false;
    }

    private DetectionActionSettings getDetectionActionUI(DetectionType type) {
        return this.detectionActionCache.computeIfAbsent(type, k -> new DetectionActionSettings());
    }

    public void setDetectionActionUI(DetectionType type, DetectionActionSettings settings) {
        DetectionActionSettings cached = this.getDetectionActionUI(type);
        cached.copyFrom(settings);
    }

    private void renderDetectionActionSection(DetectionType type, boolean showPauseOption) {
        boolean showNotif;
        DetectionActionSettings settings = this.getDetectionActionUI(type);
        String prefix = type.name().toLowerCase();
        ImDrawList drawList = ImGui.getWindowDrawList();
        ImGui.spacing();
        ImGui.pushStyleVar(12, 6.0f);
        ImGui.pushStyleVar(14, 8.0f, 4.0f);
        ImGui.pushStyleVar(23, 0.5f, 0.5f);
        ImGui.pushStyleVar(11, 12.0f, 8.0f);
        if (showPauseOption) {
            boolean pauseBot = settings.isPauseBot();
            if (pauseBot) {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.2f, 1.0f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            } else {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.7f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.9f);
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
            }
            if (ImGui.button("\uf04c  Stop##" + prefix)) {
                settings.setPauseBot(!pauseBot);
                this.notifyDetectionActionChanged(type, settings);
            }
            ImGui.popStyleColor(3);
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Zatrzymaj bota przy wykryciu");
            }
            ImGui.sameLine();
        }
        if (showNotif = settings.isShowNotification()) {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.2f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.2f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z * 0.2f, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z * 0.3f, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
        } else {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.7f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.9f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
        }
        if (ImGui.button("\uf0f3  Alert##" + prefix)) {
            settings.setShowNotification(!showNotif);
            this.notifyDetectionActionChanged(type, settings);
        }
        ImGui.popStyleColor(3);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Pokaz powiadomienie");
        }
        ImGui.sameLine();
        boolean playSound = settings.isPlaySound();
        if (playSound) {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.2f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.2f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.2f, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.3f, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        } else {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 0.7f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.9f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
        }
        if (ImGui.button("\uf028  Dzwiek##" + prefix)) {
            settings.setPlaySound(!playSound);
            this.notifyDetectionActionChanged(type, settings);
        }
        ImGui.popStyleColor(3);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Odtworz dzwiek");
        }
        ImGui.popStyleVar(4);
        if (playSound) {
            ImGui.spacing();
            float availWidth = ImGui.getContentRegionAvailX();
            ImGui.alignTextToFramePadding();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text("\uf001 Dzwiek:");
            ImGui.popStyleColor();
            ImGui.sameLine(100.0f);
            ImGui.setNextItemWidth(availWidth - 105.0f);
            String currentSound = settings.getSelectedSound();
            String displayName = this.getSoundDisplayName(currentSound);
            if (ImGui.beginCombo("##sound_" + prefix, displayName)) {
                this.renderSoundSelectionList(type, settings);
                ImGui.endCombo();
            }
            boolean useGlobal = settings.isUseGlobalVolume();
            ImGui.alignTextToFramePadding();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text("\uf028 Glosnosc:");
            ImGui.popStyleColor();
            ImGui.sameLine(110.0f);
            if (ImGui.checkbox("Globalna##globalvol_" + prefix, useGlobal)) {
                settings.setUseGlobalVolume(!useGlobal);
                this.notifyDetectionActionChanged(type, settings);
            }
            if (!useGlobal) {
                ImGui.sameLine(200.0f);
                ImGui.setNextItemWidth(availWidth - 220.0f);
                int[] volArr = new int[]{settings.getVolume()};
                if (ImGui.sliderInt("##vol_" + prefix, volArr, 0, 100, "%d%%")) {
                    settings.setVolume(volArr[0]);
                    this.notifyDetectionActionChanged(type, settings);
                }
            } else {
                ImGui.sameLine();
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
                ImGui.text("(" + this.notificationVolume.get() + "%%)");
                ImGui.popStyleColor();
            }
            ImGui.alignTextToFramePadding();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text("\uf021 Tryb:");
            ImGui.popStyleColor();
            ImGui.sameLine(100.0f);
            SoundMode currentMode = settings.getSoundMode();
            if (currentMode == SoundMode.REPEATING) {
                ImGui.setNextItemWidth(120.0f);
            } else {
                ImGui.setNextItemWidth(availWidth - 105.0f);
            }
            if (ImGui.beginCombo("##mode_" + prefix, currentMode.getDisplayName())) {
                for (SoundMode mode : SoundMode.values()) {
                    boolean isSelected;
                    boolean bl = isSelected = mode == currentMode;
                    if (ImGui.selectable(mode.getDisplayName(), isSelected)) {
                        settings.setSoundMode(mode);
                        this.notifyDetectionActionChanged(type, settings);
                    }
                    if (!isSelected) continue;
                    ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }
            if (currentMode == SoundMode.REPEATING) {
                ImGui.sameLine();
                ImGui.alignTextToFramePadding();
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
                ImGui.text("co:");
                ImGui.popStyleColor();
                ImGui.sameLine();
                float intervalStartX = ImGui.getCursorPosX();
                ImGui.setNextItemWidth(availWidth - intervalStartX - 5.0f);
                float intervalSec = (float)settings.getRepeatIntervalMs() / 1000.0f;
                float[] intervalArr = new float[]{intervalSec};
                if (ImGui.sliderFloat("##interval_" + prefix, intervalArr, 0.5f, 30.0f, "%.1f s")) {
                    settings.setRepeatIntervalMs((int)(intervalArr[0] * 1000.0f));
                    this.notifyDetectionActionChanged(type, settings);
                }
            }
        }
        ImGui.spacing();
    }

    private void renderSoundSelectionList(DetectionType type, DetectionActionSettings settings) {
        boolean isNone;
        int i;
        String currentSound = settings.getSelectedSound();
        String prefix = type.name().toLowerCase();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("-- Wbudowane --");
        ImGui.popStyleColor();
        String[] builtInSounds = new String[]{"NOTIFICATION", "NOTIFICATION_DIGITAL_STRUM", "HELLO_BELLS", "CARTOON_GAME", "SWIPE_WOOSH_DING_BETACUT", "PHONE_NOTIFICATION_BELL", "NUCLEAR_ALARM", "BAZA_AVAST"};
        String[] builtInNames = new String[]{"Default", "Digital Strum", "Screenshot", "Hello Bells", "Cartoon Game", "Cash Register", "Swipe Woosh", "Twinkle", "Phone Bell", "Pop Bottle", "Nuclear Alarm (G\u0141O\u015aNE!)", "Baza (G\u0141O\u015aNE!)"};
        for (i = 0; i < builtInSounds.length; ++i) {
            String soundId = "builtin:" + builtInSounds[i];
            boolean isSelected = soundId.equals(currentSound);
            if (ImGui.selectable(builtInNames[i] + "##builtin_" + prefix + i, isSelected)) {
                settings.setSelectedSound(soundId);
                this.notifyDetectionActionChanged(type, settings);
                if (this.callback != null) {
                    int vol = settings.isUseGlobalVolume() ? this.notificationVolume.get() : settings.getVolume();
                    this.callback.onPlaySound(soundId, vol);
                }
            }
            if (!isSelected) continue;
            ImGui.setItemDefaultFocus();
        }
        if (!this.customSounds.isEmpty()) {
            ImGui.separator();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PURPLE.x, LifebotImGuiUI.COLOR_ACCENT_PURPLE.y, LifebotImGuiUI.COLOR_ACCENT_PURPLE.z, 1.0f);
            ImGui.text("-- W\u0142asne --");
            ImGui.popStyleColor();
            for (i = 0; i < this.customSounds.size(); ++i) {
                CustomSoundInfo sound = this.customSounds.get(i);
                String soundId = "custom:" + sound.fileName;
                boolean isSelected = soundId.equals(currentSound);
                String soundLabel = sound.getDisplayNameOrFileName();
                if (ImGui.selectable(soundLabel + "##custom_" + prefix + i, isSelected)) {
                    settings.setSelectedSound(soundId);
                    this.notifyDetectionActionChanged(type, settings);
                    if (this.callback != null) {
                        int vol = settings.isUseGlobalVolume() ? this.notificationVolume.get() : settings.getVolume();
                        this.callback.onPlaySound(soundId, vol);
                    }
                }
                if (!isSelected) continue;
                ImGui.setItemDefaultFocus();
            }
        }
        ImGui.separator();
        boolean bl = isNone = currentSound == null || currentSound.isEmpty() || currentSound.equals("none");
        if (ImGui.selectable("(Brak)##none_" + prefix, isNone)) {
            settings.setSelectedSound("none");
            this.notifyDetectionActionChanged(type, settings);
        }
    }

    private void notifyDetectionActionChanged(DetectionType type, DetectionActionSettings settings) {
        if (this.callback != null) {
            this.callback.onSettingChanged("detectionAction_" + type.name(), settings);
        }
    }

    private void renderAdminDetectionPanel() {
        if (this.renderToggleCard("\uf505", "Wykrywanie admina", "Wykrywa adminow na chacie", this.detectAdminByColor, "detectAdminByColor", COLOR_ACCENT_RED)) {
            ImGui.indent(12.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_PRIMARY.x, LifebotImGuiUI.COLOR_TEXT_PRIMARY.y, LifebotImGuiUI.COLOR_TEXT_PRIMARY.z, 1.0f);
            ImGui.text("\uf53f Metoda: kolor nicku");
            ImGui.popStyleColor();
            ImDrawList drawList = ImGui.getWindowDrawList();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            ImGui.text("Wykrywane:");
            ImGui.popStyleColor();
            ImGui.sameLine();
            float boxSize = 12.0f;
            float gap = 3.0f;
            float colorBoxX = ImGui.getCursorScreenPosX();
            float colorBoxY = ImGui.getCursorScreenPosY() + 2.0f;
            drawList.addRectFilled(colorBoxX, colorBoxY, colorBoxX + boxSize, colorBoxY + boxSize, this.colorU32(0.5019608f, 0.0f, 0.0f, 1.0f), 2);
            drawList.addRectFilled(colorBoxX += boxSize + gap, colorBoxY, colorBoxX + boxSize, colorBoxY + boxSize, this.colorU32(1.0f, 0.27058825f, 0.0f, 1.0f), 2);
            drawList.addRectFilled(colorBoxX += boxSize + gap, colorBoxY, colorBoxX + boxSize, colorBoxY + boxSize, this.colorU32(1.0f, 0.0f, 0.0f, 1.0f), 2);
            drawList.addRectFilled(colorBoxX += boxSize + gap, colorBoxY, colorBoxX + boxSize, colorBoxY + boxSize, this.colorU32(0.0f, 0.7176471f, 0.92156863f, 1.0f), 2);
            drawList.addRectFilled(colorBoxX += boxSize + gap, colorBoxY, colorBoxX + boxSize, colorBoxY + boxSize, this.colorU32(0.0f, 0.5019608f, 0.0f, 1.0f), 2);
            ImGui.invisibleButton("##color_legend", (boxSize + gap) * 5.0f, boxSize + 4.0f);
            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.pushStyleColor(0, 0.5019608f, 0.0f, 0.0f, 1.0f);
                ImGui.text("RCON");
                ImGui.popStyleColor();
                ImGui.pushStyleColor(0, 1.0f, 0.27058825f, 0.0f, 1.0f);
                ImGui.text("Starszy Admin");
                ImGui.popStyleColor();
                ImGui.pushStyleColor(0, 1.0f, 0.0f, 0.0f, 1.0f);
                ImGui.text("Administrator");
                ImGui.popStyleColor();
                ImGui.pushStyleColor(0, 0.0f, 0.7176471f, 0.92156863f, 1.0f);
                ImGui.text("Moderator");
                ImGui.popStyleColor();
                ImGui.pushStyleColor(0, 0.0f, 0.5019608f, 0.0f, 1.0f);
                ImGui.text("Support");
                ImGui.popStyleColor();
                ImGui.endTooltip();
            }
            ImGui.spacing();
            boolean ocrEnabled = this.detectAdminByOCR.get();
            if (ocrEnabled) {
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
            } else {
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
            }
            if (ImGui.checkbox("\uf06e OCR (skanowanie tekstu)##ocr_toggle", this.detectAdminByOCR) && this.callback != null) {
                this.callback.onSettingChanged("detectAdminByOCR", this.detectAdminByOCR.get());
            }
            ImGui.popStyleColor();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Dodatkowa metoda - skanuje tekst szukajac nickow adminow. Moze obciazac CPU!");
            }
            ImGui.unindent(12.0f);
            ImGui.spacing();
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.ADMIN, true);
            ImGui.unindent(8.0f);
        }
    }

    private void renderAdminWarningPanel() {
        if (this.renderToggleCard("\uf071", "Ostrzezenie od admina", "Wykrywa czerwony popup ostrzezenia", this.detectAdminWarningPopup, "detectAdminWarningPopup", COLOR_ACCENT_RED)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.ADMIN_WARNING, true);
            ImGui.unindent(8.0f);
        }
    }

    private void renderDetectionCard(String icon, String title, String description, ImBoolean toggleValue, String settingKey, ImVec4 accentColor) {
        this.renderDetectionCard(icon, title, description, toggleValue, settingKey, accentColor, null);
    }

    private void renderDetectionCard(String icon, String title, String description, ImBoolean toggleValue, String settingKey, ImVec4 accentColor, String warningLabel) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        float cardStartY = ImGui.getCursorScreenPosY();
        float cardStartX = ImGui.getCursorScreenPosX();
        float cardHeight = 70.0f;
        boolean enabled = toggleValue.get();
        ImVec4 cardAccent = enabled ? COLOR_ACCENT_GREEN : accentColor;
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + windowWidth, cardStartY + cardHeight, this.colorU32(COLOR_BG_LIGHT, 0.5f), 8.0f);
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + 4.0f, cardStartY + cardHeight, this.colorU32(cardAccent), 4.0f);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 12.0f);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text(icon + " " + title);
        ImGui.popStyleColor();
        if (warningLabel != null && !warningLabel.isEmpty()) {
            ImGui.sameLine();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
            ImGui.text(warningLabel);
            ImGui.popStyleColor();
        }
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.text(description);
        ImGui.popStyleColor();
        float btnY = cardStartY + (cardHeight - 30.0f) / 2.0f;
        ImGui.setCursorPos(windowWidth - 62.0f, btnY - ImGui.getCursorScreenPosY() + ImGui.getCursorPosY() + cardHeight / 2.0f - 3.0f);
        if (enabled) {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.4f, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
        } else {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        }
        if (ImGui.button(enabled ? "ON##" + settingKey : "OFF##" + settingKey, 50.0f, 26.0f)) {
            toggleValue.set(!enabled);
            if (this.callback != null) {
                this.callback.onSettingChanged(settingKey, toggleValue.get());
            }
        }
        ImGui.popStyleColor(3);
        float newY = cardStartY - ImGui.getWindowPosY() + cardHeight + 5.0f + ImGui.getScrollY();
        float currentY = ImGui.getCursorPosY();
        if (newY > currentY) {
            ImGui.dummy(0.0f, newY - currentY);
        }
    }

    private void renderInventoryDetectionPanel() {
        if (this.renderToggleCard("\uf466", "Pelny ekwipunek", "Wykrywa gdy masz pelny plecak wegla", this.detectFullInventory, "detectFullInventory", COLOR_ACCENT_PRIMARY)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.FULL_INVENTORY, true);
            ImGui.unindent(8.0f);
        }
    }

    private void renderSectionHeader(String title) {
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text(title);
        ImGui.popStyleColor();
        ImGui.spacing();
    }

    private boolean renderToggleCard(String icon, String title, String description, ImBoolean toggleValue, String settingKey, ImVec4 accentColor) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        float cardStartY = ImGui.getCursorScreenPosY();
        float cardStartX = ImGui.getCursorScreenPosX();
        float cardHeight = 70.0f;
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + windowWidth, cardStartY + cardHeight, this.colorU32(COLOR_BG_LIGHT, 0.5f), 8.0f);
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + 4.0f, cardStartY + cardHeight, this.colorU32(accentColor), 4.0f);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 12.0f);
        ImGui.pushStyleColor(0, accentColor.x, accentColor.y, accentColor.z, 1.0f);
        ImGui.text(icon + " " + title);
        ImGui.popStyleColor();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.text(description);
        ImGui.popStyleColor();
        float buttonWidth = 50.0f;
        float buttonHeight = 26.0f;
        float buttonX = windowWidth - buttonWidth - 12.0f;
        float buttonY = cardStartY - ImGui.getWindowPosY() + (cardHeight - buttonHeight) / 2.0f + ImGui.getScrollY();
        ImGui.setCursorPos(buttonX, buttonY);
        boolean enabled = toggleValue.get();
        if (enabled) {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.4f, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
        } else {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_MEDIUM.x, LifebotImGuiUI.COLOR_BG_MEDIUM.y, LifebotImGuiUI.COLOR_BG_MEDIUM.z, 1.0f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
        }
        ImGui.pushStyleVar(12, 6.0f);
        ImGui.pushStyleVar(23, 0.5f, 0.5f);
        if (ImGui.button(enabled ? "ON##" + settingKey : "OFF##" + settingKey, buttonWidth, buttonHeight)) {
            toggleValue.set(!enabled);
            if (this.callback != null) {
                this.callback.onSettingChanged(settingKey, toggleValue.get());
            }
        }
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);
        float targetY = cardStartY - ImGui.getCursorScreenPosY() + ImGui.getCursorPosY() + cardHeight + 8.0f;
        float currentY = ImGui.getCursorPosY();
        if (targetY > currentY) {
            ImGui.dummy(0.0f, targetY - currentY);
        }
        return enabled;
    }

    private void renderChatDetectionPanel() {
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        this.renderSectionHeader("\uf007 Twoj nick");
        ImGui.indent(8.0f);
        String nick = this.playerNickname.get();
        boolean nickTooShortForPM = nick.length() < 3;
        boolean nickTooShortForMention = nick.length() < 5;
        ImGui.setNextItemWidth(180.0f);
        if (ImGui.inputText("##nick_chat", this.playerNickname) && this.callback != null) {
            this.callback.onSettingChanged("playerNickname", this.playerNickname.get());
        }
        ImGui.sameLine();
        if (nick.isEmpty()) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
            ImGui.text("\uf06a Wymagany");
            ImGui.popStyleColor();
        } else if (nickTooShortForPM) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f);
            ImGui.text("\uf00d Min 3 znaki");
            ImGui.popStyleColor();
        } else if (nickTooShortForMention) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 1.0f);
            ImGui.text("\uf071 Min 5 dla wzmianek");
            ImGui.popStyleColor();
        } else {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text("\uf00c OK");
            ImGui.popStyleColor();
        }
        ImGui.unindent(8.0f);
        ImGui.spacing();
        if (nickTooShortForPM) {
            ImGui.beginDisabled();
        }
        if (this.renderToggleCard("\uf0e0", "Prywatne wiadomosci (PM)", "Wykrywa gdy ktos wysle Ci PW", this.detectPrivateMessage, "detectPrivateMessage", COLOR_ACCENT_PRIMARY)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.PRIVATE_MESSAGE, true);
            ImGui.unindent(8.0f);
        }
        if (nickTooShortForPM) {
            ImGui.endDisabled();
        }
        ImGui.spacing();
        if (nickTooShortForMention) {
            ImGui.beginDisabled();
        }
        if (this.renderToggleCard("\uf1fa", "Wzmianki twojego nicku", "Wykrywa gdy ktos napisze Twoj nick na chacie", this.detectMention, "detectMention", COLOR_ACCENT_PURPLE)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.MENTION, true);
            ImGui.unindent(8.0f);
        }
        if (nickTooShortForMention) {
            ImGui.endDisabled();
        }
    }

    private void renderMiningDetectionPanel() {
        if (this.renderToggleCard("\uf057", "Pusta sciana", "Nie ma co kopac - sciana pusta", this.getMiningEventToggle(DetectionType.COAL_EMPTY), "miningEvent_COAL_EMPTY", COLOR_ACCENT_RED)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.COAL_EMPTY, true);
            ImGui.unindent(8.0f);
        }
        ImGui.spacing();
        if (this.renderToggleCard("\uf3bf", "Za wysoki level sciany", "Level sciany wyzszy niz Twoj level", this.getMiningEventToggle(DetectionType.DIG_LEVEL_TOO_HIGH), "miningEvent_DIG_LEVEL_TOO_HIGH", COLOR_ACCENT_YELLOW)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.DIG_LEVEL_TOO_HIGH, true);
            ImGui.unindent(8.0f);
        }
        ImGui.spacing();
        if (this.renderToggleCard("\uf362", "Zmiana levelu sciany", "Ktos zajal Twoja sciane", this.getMiningEventToggle(DetectionType.DIG_LEVEL_CHANGE), "miningEvent_DIG_LEVEL_CHANGE", COLOR_ACCENT_PURPLE)) {
            ImGui.indent(8.0f);
            this.renderDetectionActionSection(DetectionType.DIG_LEVEL_CHANGE, false);
            ImGui.unindent(8.0f);
        }
    }

    private ImBoolean getMiningEventToggle(DetectionType type) {
        return this.miningEventToggles.computeIfAbsent(type, k -> new ImBoolean(true));
    }

    public void setMiningEventEnabled(DetectionType type, boolean enabled) {
        this.getMiningEventToggle(type).set(enabled);
    }

    private void renderMiningEventCard(String icon, String title, String description, DetectionType type, ImVec4 accentColor, boolean showPause) {
        boolean bell;
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowWidth = ImGui.getContentRegionAvailX();
        float cardStartY = ImGui.getCursorScreenPosY();
        float cardStartX = ImGui.getCursorScreenPosX();
        float cardHeight = 56.0f;
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + windowWidth, cardStartY + cardHeight, this.colorU32(COLOR_BG_LIGHT, 0.4f), 8.0f);
        drawList.addRectFilled(cardStartX, cardStartY, cardStartX + 4.0f, cardStartY + cardHeight, this.colorU32(accentColor), 4.0f);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 8.0f);
        ImGui.pushStyleColor(0, accentColor.x, accentColor.y, accentColor.z, 1.0f);
        ImGui.text(icon + " " + title);
        ImGui.popStyleColor();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 12.0f);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.text(description);
        ImGui.popStyleColor();
        float targetY = cardStartY - ImGui.getCursorScreenPosY() + ImGui.getCursorPosY() + cardHeight + 4.0f;
        float currentY = ImGui.getCursorPosY();
        if (targetY > currentY) {
            ImGui.dummy(0.0f, targetY - currentY);
        }
        ImGui.indent(12.0f);
        DetectionActionSettings settings = this.getDetectionActionUI(type);
        String prefix = type.name().toLowerCase();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        if (showPause) {
            boolean pause = settings.isPauseBot();
            if (pause) {
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            }
            if (ImGui.checkbox("\uf04c Stop##" + prefix, pause)) {
                settings.setPauseBot(!pause);
                this.notifyDetectionActionChanged(type, settings);
            }
            if (pause) {
                ImGui.popStyleColor();
            }
            ImGui.sameLine();
        }
        if (bell = settings.isShowNotification()) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
        }
        if (ImGui.checkbox("\uf0f3 Alert##" + prefix, bell)) {
            settings.setShowNotification(!bell);
            this.notifyDetectionActionChanged(type, settings);
        }
        if (bell) {
            ImGui.popStyleColor();
        }
        ImGui.sameLine();
        boolean sound = settings.isPlaySound();
        if (sound) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
        }
        if (ImGui.checkbox("\uf028 Dzwiek##" + prefix, sound)) {
            settings.setPlaySound(!sound);
            this.notifyDetectionActionChanged(type, settings);
        }
        if (sound) {
            ImGui.popStyleColor();
        }
        ImGui.popStyleColor();
        ImGui.unindent(12.0f);
        ImGui.spacing();
    }

    private void renderIdentitySection() {
    }

    private void renderAdminDetectionSection() {
    }

    private void renderInventoryDetectionSection() {
    }

    private void renderChatDetectionSection() {
    }

    public boolean isDetectionEnabled() {
        return this.detectionEnabled.get();
    }

    public boolean isDetectAdminByColor() {
        return this.detectAdminByColor.get();
    }

    public boolean isDetectAdminByOCR() {
        return this.detectAdminByOCR.get();
    }

    public boolean isDetectAdminWarningPopup() {
        return this.detectAdminWarningPopup.get();
    }

    public boolean isPauseOnAdminDetected() {
        return this.pauseOnAdminDetected.get();
    }

    public boolean isAlertOnAdminDetected() {
        return this.alertOnAdminDetected.get();
    }

    public boolean isDetectFullInventory() {
        return this.detectFullInventory.get();
    }

    public boolean isPauseOnFullInventory() {
        return this.pauseOnFullInventory.get();
    }

    public boolean isAlertOnFullInventory() {
        return this.alertOnFullInventory.get();
    }

    public boolean isDetectPrivateMessage() {
        return this.detectPrivateMessage.get();
    }

    public boolean isDetectMention() {
        return this.detectMention.get();
    }

    public boolean isPauseOnPM() {
        return this.pauseOnPM.get();
    }

    public boolean isAlertOnPM() {
        return this.alertOnPM.get();
    }

    public String getPlayerNickname() {
        return this.playerNickname.get();
    }

    public int getPlayerId() {
        return this.playerId.get();
    }

    public DetectionActionSettings getDetectionActionSettings(DetectionType type) {
        return this.getDetectionActionUI(type);
    }

    private void renderSoundsTab() {
        int i;
        float deltaTime = ImGui.getIO().getDeltaTime();
        this.stopButtonScale += (this.stopButtonTargetScale - this.stopButtonScale) * 12.0f * deltaTime;
        for (String key : this.playButtonTargetScales.keySet()) {
            float current = this.playButtonScales.getOrDefault(key, Float.valueOf(1.0f)).floatValue();
            float target = this.playButtonTargetScales.get(key).floatValue();
            this.playButtonScales.put(key, Float.valueOf(current + (target - current) * 12.0f * deltaTime));
        }
        ImGui.spacing();
        this.renderSectionHeader("\uf028  Glowne ustawienia");
        if (ImGui.checkbox("Wlacz dzwieki", this.soundEnabled) && this.callback != null) {
            this.callback.onSettingChanged("soundEnabled", this.soundEnabled.get());
        }
        float sliderWidth = 150.0f;
        float labelWidth = ImGui.calcTextSize((String)"Glosnosc:").x;
        ImGui.sameLine(ImGui.getContentRegionAvailX() - sliderWidth - labelWidth - 40.0f);
        ImGui.text("Glosnosc:");
        ImGui.sameLine();
        ImGui.setNextItemWidth(sliderWidth);
        ImGui.pushStyleColor(19, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.pushStyleColor(20, LifebotImGuiUI.COLOR_ACCENT_HOVER.x, LifebotImGuiUI.COLOR_ACCENT_HOVER.y, LifebotImGuiUI.COLOR_ACCENT_HOVER.z, 1.0f);
        ImGui.sliderInt("##volume", this.notificationVolume.getData(), 0, 100, "%d%%");
        ImGui.popStyleColor(2);
        if (ImGui.isItemDeactivatedAfterEdit() && this.callback != null) {
            this.callback.onSettingChanged("notificationVolume", this.notificationVolume.get());
        }
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
        ImGui.text("\uf05a Dzwieki dla zdarzen konfigurujesz w zakladce Detekcja");
        ImGui.popStyleColor();
        if (!this.soundEnabled.get()) {
            ImGui.beginDisabled();
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf001  Wbudowane dzwieki");
        String[] builtInSounds = new String[]{"NOTIFICATION:Default", "NOTIFICATION_DIGITAL_STRUM:Digital Strum", "HELLO_BELLS:Hello Bells", "CARTOON_GAME:Cartoon Game", "SWIPE_WOOSH_DING_BETACUT:Swipe Woosh", "PHONE_NOTIFICATION_BELL:Phone Bell", "NUCLEAR_ALARM:Nuclear Alarm", "BAZA_AVAST:Baza"};
        float contentWidth = ImGui.getContentRegionAvailX();
        int columns = 3;
        float gap = 6.0f;
        float buttonWidth = (contentWidth - gap * (float)(columns - 1)) / (float)columns;
        float buttonHeight = 26.0f;
        for (i = 0; i < builtInSounds.length; ++i) {
            Object buttonLabel;
            boolean isLoud;
            String[] parts = builtInSounds[i].split(":");
            String soundEnum = parts[0];
            String displayName = parts[1];
            boolean bl = isLoud = soundEnum.equals("NUCLEAR_ALARM") || soundEnum.equals("BAZA_AVAST");
            if (i % columns != 0) {
                ImGui.sameLine(0.0f, gap);
            }
            ImGui.pushID("builtin_" + i);
            if (isLoud) {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.25f, 0.05f, 1.0f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.4f, 0.1f, 1.0f);
                ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.6f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.5f, 0.15f, 1.0f);
            } else {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.35f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.45f, 1.0f);
                ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.6f, 1.0f);
            }
            Object object = buttonLabel = isLoud ? "\uf071 " + displayName : displayName;
            if (ImGui.button((String)buttonLabel, buttonWidth, buttonHeight) && this.callback != null) {
                this.callback.onPlaySound("builtin:" + soundEnum, this.notificationVolume.get());
            }
            if (isLoud && ImGui.isItemHovered()) {
                ImGui.setTooltip("Uwaga: Glosny dzwiek!");
            }
            ImGui.popStyleColor(3);
            ImGui.popID();
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf07c  Wlasne dzwieki");
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.4f, 1.0f);
        if (ImGui.button("\uf07c Otworz folder", 135.0f, 26.0f) && this.callback != null) {
            this.callback.onOpenSoundsFolder();
        }
        ImGui.popStyleColor(2);
        ImGui.sameLine();
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.6f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.7f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.7f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.8f, 1.0f);
        if (ImGui.button("\uf021 Odswiez", 100.0f, 26.0f) && this.callback != null) {
            this.callback.onRefreshSounds();
        }
        ImGui.popStyleColor(2);
        ImGui.spacing();
        if (!this.customSounds.isEmpty()) {
            for (i = 0; i < this.customSounds.size(); ++i) {
                CustomSoundInfo sound = this.customSounds.get(i);
                boolean isEditing = this.editingSoundIndex == i;
                boolean hasDisplayName = sound.displayName != null && !sound.displayName.isEmpty();
                ImGui.pushID("csnd_" + i);
                if (i > 0) {
                    ImGui.pushStyleColor(27, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.5f);
                    ImGui.separator();
                    ImGui.popStyleColor();
                    ImGui.spacing();
                }
                ImGui.pushStyleColor(21, 0.0f, 0.0f, 0.0f, 0.0f);
                ImGui.pushStyleColor(22, 0.0f, 0.0f, 0.0f, 0.0f);
                ImGui.pushStyleColor(23, 0.0f, 0.0f, 0.0f, 0.0f);
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
                if (ImGui.button("\uf04b##play", 24.0f, 24.0f) && this.callback != null) {
                    this.callback.onPlaySound("custom:" + sound.fileName, this.notificationVolume.get());
                }
                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip("Odtworz: " + sound.fileName);
                }
                ImGui.popStyleColor(4);
                ImGui.sameLine();
                if (!hasDisplayName) {
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
                    ImGui.text("[NOWY]");
                    ImGui.popStyleColor();
                    ImGui.sameLine();
                }
                if (hasDisplayName) {
                    ImGui.text(sound.displayName);
                    ImGui.sameLine();
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
                    ImGui.text("(" + sound.fileName + ")");
                    ImGui.popStyleColor();
                } else {
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
                    ImGui.text(sound.fileName);
                    ImGui.popStyleColor();
                }
                ImGui.sameLine(ImGui.getWindowWidth() - 95.0f);
                if (isEditing) {
                    ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.5f, 0.1f, 0.1f, 1.0f);
                    ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.7f, 0.15f, 0.15f, 1.0f);
                    if (ImGui.button("\uf00d Anuluj", 80.0f, 24.0f)) {
                        this.editingSoundIndex = -1;
                    }
                    ImGui.popStyleColor(2);
                } else {
                    ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
                    ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.4f, 1.0f);
                    if (ImGui.button("\uf044 Edytuj", 80.0f, 24.0f)) {
                        this.editingSoundIndex = i;
                        this.justStartedEditing = i;
                        this.renamingBuffers.put(sound.fileName, new ImString(sound.displayName != null ? sound.displayName : "", 64));
                    }
                    ImGui.popStyleColor(2);
                }
                if (isEditing) {
                    String newName;
                    ImGui.spacing();
                    ImGui.indent(30.0f);
                    ImGui.alignTextToFramePadding();
                    ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
                    ImGui.text("Nazwa:");
                    ImGui.popStyleColor();
                    ImGui.sameLine();
                    ImGui.setNextItemWidth(150.0f);
                    ImString nameBuffer = this.renamingBuffers.get(sound.fileName);
                    if (nameBuffer == null) {
                        nameBuffer = new ImString(sound.displayName != null ? sound.displayName : "", 64);
                        this.renamingBuffers.put(sound.fileName, nameBuffer);
                    }
                    if (this.justStartedEditing == i) {
                        ImGui.setKeyboardFocusHere();
                        this.justStartedEditing = -1;
                    }
                    float inputHeight = ImGui.getFrameHeight();
                    if (ImGui.inputText("##edit", nameBuffer, 32)) {
                        newName = nameBuffer.get().trim();
                        if (this.callback != null) {
                            this.callback.onRenameSound(sound.fileName, newName);
                        }
                        this.editingSoundIndex = -1;
                    }
                    ImGui.sameLine();
                    ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
                    ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.7f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.7f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.5f, 1.0f);
                    if (ImGui.button("\uf00c Zapisz", 85.0f, inputHeight)) {
                        newName = nameBuffer.get().trim();
                        if (this.callback != null) {
                            this.callback.onRenameSound(sound.fileName, newName);
                        }
                        this.editingSoundIndex = -1;
                    }
                    ImGui.popStyleColor(2);
                    ImGui.unindent(30.0f);
                }
                ImGui.spacing();
                ImGui.popID();
            }
        } else {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
            ImGui.text("Brak wlasnych dzwiekow");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
            ImGui.textWrapped("Kliknij 'Otworz folder' i wrzuc pliki .wav, nastepnie 'Odswiez'");
            ImGui.popStyleColor();
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf0f3  Test powiadomienia");
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
        ImGui.textWrapped("Sprawdz jak wygladaja powiadomienia w grze. Kliknij przycisk aby wyslac testowe powiadomienie.");
        ImGui.popStyleColor();
        ImGui.spacing();
        float testBtnWidth = (ImGui.getContentRegionAvailX() - 8.0f) / 2.0f;
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.3f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.6f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.4f, 1.0f);
        ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.35f, 1.0f);
        if (ImGui.button("\uf505 Admin wykryty", testBtnWidth, 30.0f) && this.callback != null) {
            this.callback.onTestNotification("admin");
        }
        ImGui.popStyleColor(3);
        ImGui.sameLine(0.0f, 8.0f);
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.35f, 0.1f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.55f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.5f, 0.15f, 1.0f);
        ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x * 0.45f, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y * 0.4f, 0.12f, 1.0f);
        if (ImGui.button("\uf49e Pelny ekwipunek", testBtnWidth, 30.0f) && this.callback != null) {
            this.callback.onTestNotification("inventory");
        }
        ImGui.popStyleColor(3);
        ImGui.spacing();
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.5f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.55f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.55f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.65f, 1.0f);
        ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.45f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.45f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.55f, 1.0f);
        if (ImGui.button("\uf0e0 Prywatna wiadomosc", testBtnWidth, 30.0f) && this.callback != null) {
            this.callback.onTestNotification("pm");
        }
        ImGui.popStyleColor(3);
        ImGui.sameLine(0.0f, 8.0f);
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.35f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.35f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.25f, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.4f, 1.0f);
        ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.4f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
        if (ImGui.button("\uf1fa Wzmianka na chacie", testBtnWidth, 30.0f) && this.callback != null) {
            this.callback.onTestNotification("mention");
        }
        ImGui.popStyleColor(3);
        ImGui.spacing();
        if (!this.soundEnabled.get()) {
            ImGui.endDisabled();
        }
    }

    private String getSoundDisplayName(String soundId) {
        if (soundId == null || soundId.isEmpty() || soundId.equals("none")) {
            return "(Brak)";
        }
        if (soundId.startsWith("builtin:")) {
            String name;
            switch (name = soundId.substring("builtin:".length())) {
                case "NOTIFICATION": {
                    return "Default";
                }
                case "NOTIFICATION_DIGITAL_STRUM": {
                    return "Digital Strum";
                }
                case "HELLO_BELLS": {
                    return "Hello Bells";
                }
                case "CARTOON_GAME": {
                    return "Cartoon Game";
                }
                case "SWIPE_WOOSH_DING_BETACUT": {
                    return "Swipe Woosh";
                }
                case "PHONE_NOTIFICATION_BELL": {
                    return "Phone Bell";
                }
                case "NUCLEAR_ALARM": {
                    return "Nuclear Alarm (GLOSNE!)";
                }
                case "BAZA_AVAST": {
                    return "Baza (GLOSNE!)";
                }
            }
            return name;
        }
        if (soundId.startsWith("custom:")) {
            String fileName = soundId.substring("custom:".length());
            for (CustomSoundInfo info : this.customSounds) {
                if (!info.fileName.equals(fileName)) continue;
                return info.displayName != null && !info.displayName.isEmpty() ? info.displayName : fileName;
            }
            return fileName;
        }
        return soundId;
    }

    public void setCustomSounds(List<CustomSoundInfo> sounds) {
        this.customSounds = sounds != null ? sounds : new ArrayList();
        this.editingSoundIndex = -1;
        this.justStartedEditing = -1;
    }

    public void setCustomSoundNames(String[] names) {
        this.customSoundNames = names != null ? names : new String[]{};
    }

    private void renderAdditionalTab() {
        ImGui.spacing();
        this.renderSectionHeader("\uf070  Prywatnosc");
        if (ImGui.checkbox("\uf3ed Anti-Capture (anty-screen)", this.antiCapture) && this.callback != null) {
            this.callback.onAntiCaptureChanged(this.antiCapture.get());
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Ukryj overlay przed nagrywaniem ekranu i screenshotami");
        }
        if (ImGui.checkbox("\uf030 Windows Capture API", this.useWindowCapture) && this.callback != null) {
            this.callback.onSettingChanged("useWindowCapture", this.useWindowCapture.get());
        }
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Uzywaj Windows Graphics Capture API do robienia zrzutow ekranu.");
            ImGui.text("Wylacz jesli obraz MTA mruga/migota podczas dzialania bota.");
            ImGui.textColored(1.0f, 0.8f, 0.0f, 1.0f, "Wymaga restartu bota po zmianie!");
            ImGui.endTooltip();
        }
        ImGui.spacing();
        this.renderSectionHeader("\uf042  Przezroczystosc okien");
        ImGui.setNextItemWidth(200.0f);
        if (ImGui.sliderFloat("##windowOpacity", this.windowOpacity.getData(), 0.1f, 1.0f, String.format("%.0f%%%%", Float.valueOf(this.windowOpacity.get() * 100.0f))) && this.callback != null) {
            this.callback.onSettingChanged("windowOpacity", Float.valueOf(this.windowOpacity.get()));
        }
        ImGui.sameLine();
        ImGui.textColored(0.6f, 0.6f, 0.6f, 1.0f, String.format("%.0f%%", Float.valueOf(this.windowOpacity.get() * 100.0f)));
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Ustaw przezroczystosc okien ImGui (10-100%%)");
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf030  Zrzuty ekranu");
        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 1.0f);
        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.4f, 1.0f);
        if (ImGui.button("\uf07c Otworz folder", 140.0f, 26.0f) && this.callback != null) {
            this.callback.onOpenScreenshotsFolder();
        }
        ImGui.popStyleColor(2);
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Otworz folder ze zrzutami ekranu w eksploratorze plikow");
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf11c  Skroty klawiszowe");
        float keyWidth = 80.0f;
        float descStartX = keyWidth + 20.0f;
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("END");
        ImGui.popStyleColor();
        ImGui.sameLine(descStartX);
        ImGui.text("Wlacz/wylacz bota");
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("INSERT");
        ImGui.popStyleColor();
        ImGui.sameLine(descStartX);
        ImGui.text("Pokaz/ukryj overlay");
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\\");
        ImGui.popStyleColor();
        ImGui.sameLine(descStartX);
        ImGui.text("Zrzut ekranu");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f);
        if (ImGui.checkbox("\uf071 Przepuszczaj END/INSERT do MTA", this.passthroughHotkeys)) {
            if (this.callback != null) {
                this.callback.onSettingChanged("passthroughHotkeys", this.passthroughHotkeys.get());
            }
            if (this.passthroughHotkeys.get() && this.botRunning && this.callback != null) {
                this.callback.onBotToggle(false);
            }
        }
        ImGui.popStyleColor();
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Gdy wlaczone, END i INSERT beda dzialac normalnie w MTA.\nUzywaj gdy admin kaze Ci kliknac END aby sprawdzic czy masz bota!");
        }
        ImGui.spacing();
        if (this.passthroughHotkeys.get()) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 0.9f);
            ImGui.text("\uf06a UWAGA: END i INSERT dzialaja normalnie w MTA!");
            ImGui.popStyleColor();
        } else {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
            ImGui.text("\uf05a Skroty END i INSERT nie sa wysylane do MTA");
            ImGui.popStyleColor();
        }
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        this.renderSectionHeader("\uf086  Ostrzezenia chatu");
        if (ImGui.checkbox("\uf1f6 Ukryj ostrzezenie o czarnym chacie", this.hideBlackChatWarning) && this.callback != null) {
            this.callback.onSettingChanged("hideBlackChatWarning", this.hideBlackChatWarning.get());
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Ukrywa ostrzezenie wymagajace czarnego tla chatu.\\nUWAGA: Detekcje oparte na chacie nie beda dzialac!");
        }
        if (this.hideBlackChatWarning.get()) {
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, 0.9f);
            ImGui.textWrapped("\uf071 Detekcje oparte na chacie (admin, PM, wzmianki) sa wylaczone.");
            ImGui.popStyleColor();
        }
        ImGui.spacing();
    }

    private void renderGameTab() {
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("\uf11b");
        ImGui.popStyleColor();
        ImGui.sameLine();
        ImGui.text("Minigry");
        ImGui.spacing();
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
        ImGui.textWrapped("Wybierz minigre aby zagrac podczas kopania! Gry nie wplywaja na dzialanie bota.");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        if (this.miniGameWindow.isOpen()) {
            ImGui.pushStyleColor(3, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.2f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.2f, 1.0f);
            ImGui.beginChild("##game_status", ImGui.getContentRegionAvailX(), 40.0f, true);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text("\uf04b Aktywna: " + this.miniGameWindow.getCurrentGame().displayName);
            ImGui.popStyleColor();
            if (this.miniGameWindow.isFocused()) {
                ImGui.sameLine();
                ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 1.0f);
                ImGui.text("| \uf11c Klawisze przechwycone");
                ImGui.popStyleColor();
            }
            ImGui.endChild();
            ImGui.popStyleColor();
            ImGui.spacing();
        }
        float availWidth = ImGui.getContentRegionAvailX();
        float cardWidth = (availWidth - 8.0f) / 2.0f;
        float cardHeight = 100.0f;
        MiniGameWindow.GameType[] games = MiniGameWindow.GameType.values();
        for (int i = 0; i < games.length; ++i) {
            if (i % 2 == 1) {
                ImGui.sameLine(0.0f, 8.0f);
            }
            this.renderGameCard(games[i], cardWidth, cardHeight);
            if (i % 2 != 1 && i != games.length - 1) continue;
            ImGui.spacing();
        }
    }

    private void renderGameCard(MiniGameWindow.GameType game, float width, float height) {
        boolean isAvailable;
        boolean isCurrentGame = this.miniGameWindow.isOpen() && this.miniGameWindow.getCurrentGame() == game;
        boolean bl = isAvailable = game == MiniGameWindow.GameType.SNAKE || game == MiniGameWindow.GameType.PONG;
        if (isCurrentGame) {
            ImGui.pushStyleColor(3, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.15f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.15f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.15f, 1.0f);
        } else {
            ImGui.pushStyleColor(3, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.5f);
        }
        ImGui.pushStyleVar(7, 6.0f);
        ImGui.pushStyleVar(2, 10.0f, 8.0f);
        ImGui.beginChild("##game_" + game.name(), width, height, true);
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text(game.displayName);
        ImGui.popStyleColor();
        if (isCurrentGame) {
            ImGui.sameLine();
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.text("\uf058");
            ImGui.popStyleColor();
        }
        ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, 0.8f);
        ImGui.textWrapped(game.description);
        ImGui.popStyleColor();
        float buttonY = height - 38.0f;
        ImGui.setCursorPosY(buttonY);
        float buttonWidth = ImGui.getContentRegionAvailX();
        if (isAvailable) {
            if (isCurrentGame) {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.7f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.7f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.7f, 1.0f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f);
                ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_RED.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_RED.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_RED.z * 0.5f, 1.0f);
                if (ImGui.button("\uf04d Zamknij##" + game.name(), buttonWidth, 26.0f)) {
                    this.miniGameWindow.close();
                }
                ImGui.popStyleColor(3);
            } else {
                ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, 0.8f);
                ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_HOVER.x, LifebotImGuiUI.COLOR_ACCENT_HOVER.y, LifebotImGuiUI.COLOR_ACCENT_HOVER.z, 1.0f);
                ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x * 0.7f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y * 0.7f, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z * 0.7f, 1.0f);
                if (ImGui.button("\uf04b Graj##" + game.name(), buttonWidth, 26.0f)) {
                    this.miniGameWindow.openGame(game);
                }
                ImGui.popStyleColor(3);
            }
        } else {
            ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.3f);
            ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.3f);
            ImGui.pushStyleColor(23, LifebotImGuiUI.COLOR_BG_LIGHT.x, LifebotImGuiUI.COLOR_BG_LIGHT.y, LifebotImGuiUI.COLOR_BG_LIGHT.z, 0.3f);
            ImGui.pushStyleColor(0, LifebotImGuiUI.COLOR_TEXT_DISABLED.x, LifebotImGuiUI.COLOR_TEXT_DISABLED.y, LifebotImGuiUI.COLOR_TEXT_DISABLED.z, 1.0f);
            ImGui.button("\uf017 Wkrotce##" + game.name(), buttonWidth, 26.0f);
            ImGui.popStyleColor(4);
        }
        ImGui.endChild();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor();
    }

    public MiniGameWindow getMiniGameWindow() {
        return this.miniGameWindow;
    }

    private float getEffectiveAlpha() {
        return this.uiAlpha * this.windowOpacity.get();
    }

    private int colorU32(float r, float g, float b, float a) {
        return ImGui.colorConvertFloat4ToU32(r, g, b, a * this.getEffectiveAlpha());
    }

    private int colorU32(ImVec4 color) {
        return ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, color.w * this.getEffectiveAlpha());
    }

    private int colorU32(ImVec4 color, float alpha) {
        return ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, alpha * this.getEffectiveAlpha());
    }

    private void centerText(String text) {
        float windowWidth = ImGui.getWindowWidth();
        float textWidth = ImGui.calcTextSize((String)text).x;
        ImGui.setCursorPosX((windowWidth - textWidth) / 2.0f);
        ImGui.text(text);
    }

    public void setBotRunning(boolean running) {
        this.botRunning = running;
        if (running) {
            this.statusText = "Uruchomiony";
            this.statusColor = COLOR_ACCENT_GREEN;
        } else {
            this.statusText = "Zatrzymany";
            this.statusColor = COLOR_ACCENT_RED;
            this.clearRecognizedLetters();
        }
    }

    public void setStatus(String text, ImVec4 color) {
        this.statusText = text;
        this.statusColor = color;
    }

    public void setStatusLearning(int samples, int required) {
        this.collectedSamples = samples;
        this.requiredSamples = required;
        boolean bl = this.isLearningReady = samples >= required;
        if (!this.isLearningReady) {
            this.statusText = "Uczenie...";
            this.statusColor = COLOR_ACCENT_PRIMARY;
        }
    }

    public void setLearningReady(boolean ready) {
        this.isLearningReady = ready;
        if (ready && !this.botRunning) {
            this.statusText = "Gotowy";
            this.statusColor = COLOR_ACCENT_GREEN;
        }
    }

    public void setLearnedStatistics(double[] delayMeans, double[] delayStdDevs, double[] durationMeans, double[] durationStdDevs) {
        System.arraycopy(delayMeans, 0, this.learnedDelayMeans, 0, Math.min(3, delayMeans.length));
        System.arraycopy(delayStdDevs, 0, this.learnedDelayStdDevs, 0, Math.min(3, delayStdDevs.length));
        System.arraycopy(durationMeans, 0, this.learnedDurationMeans, 0, Math.min(3, durationMeans.length));
        System.arraycopy(durationStdDevs, 0, this.learnedDurationStdDevs, 0, Math.min(3, durationStdDevs.length));
    }

    public void setRecognizedLetters(char[] letters, boolean[] correct) {
        int len = Math.min(3, Math.min(letters.length, correct.length));
        this.recognizedLetters = new char[len];
        this.recognizedCorrect = new boolean[len];
        System.arraycopy(letters, 0, this.recognizedLetters, 0, len);
        System.arraycopy(correct, 0, this.recognizedCorrect, 0, len);
    }

    public void clearRecognizedLetters() {
        this.recognizedLetters = new char[0];
        this.recognizedCorrect = new boolean[0];
    }

    public void setYourLevel(String level, ImVec4 color) {
        this.yourLevel = level;
        this.yourLevelColor = color;
    }

    public void setRequiredLevel(String level, ImVec4 color) {
        this.requiredLevel = level;
        this.requiredLevelColor = color;
    }

    public void setCoalPrice(double price) {
        this.coalPriceValue = price;
        this.coalPrice = df.format(price) + " PLN";
    }

    public boolean isSettingsOpen() {
        return this.showSettings;
    }

    public void setSettingsOpen(boolean open) {
        this.showSettings = open;
        this.showSettingsPtr.set(open);
    }

    public boolean isLearningReady() {
        return this.isLearningReady;
    }

    public int getNotificationVolume() {
        return this.notificationVolume.get();
    }

    public boolean isAntiCapture() {
        return this.antiCapture.get();
    }

    public boolean isSoundEnabled() {
        return this.soundEnabled.get();
    }

    public boolean isPassthroughHotkeys() {
        return this.passthroughHotkeys.get();
    }

    public float getWindowOpacity() {
        return this.windowOpacity.get();
    }

    public void setNotificationVolume(int value) {
        this.notificationVolume.set(value);
    }

    public void setAntiCapture(boolean value) {
        this.antiCapture.set(value);
        if (this.callback != null) {
            this.callback.onAntiCaptureChanged(value);
        }
    }

    public void setSoundEnabled(boolean value) {
        this.soundEnabled.set(value);
    }

    public void setWindowOpacity(float value) {
        this.windowOpacity.set(Math.max(0.1f, Math.min(1.0f, value)));
    }

    public void setDetectionEnabled(boolean value) {
        this.detectionEnabled.set(value);
    }

    public void setDetectAdminByColor(boolean value) {
        this.detectAdminByColor.set(value);
    }

    public void setDetectAdminByOCR(boolean value) {
        this.detectAdminByOCR.set(value);
    }

    public void setDetectAdminWarningPopup(boolean value) {
        this.detectAdminWarningPopup.set(value);
    }

    public void setPauseOnAdminDetected(boolean value) {
        this.pauseOnAdminDetected.set(value);
    }

    public void setAlertOnAdminDetected(boolean value) {
        this.alertOnAdminDetected.set(value);
    }

    public void setDetectFullInventory(boolean value) {
        this.detectFullInventory.set(value);
    }

    public void setPauseOnFullInventory(boolean value) {
        this.pauseOnFullInventory.set(value);
    }

    public void setAlertOnFullInventory(boolean value) {
        this.alertOnFullInventory.set(value);
    }

    public void setDetectPrivateMessage(boolean value) {
        this.detectPrivateMessage.set(value);
    }

    public void setDetectMention(boolean value) {
        this.detectMention.set(value);
    }

    public void setPauseOnPM(boolean value) {
        this.pauseOnPM.set(value);
    }

    public void setAlertOnPM(boolean value) {
        this.alertOnPM.set(value);
    }

    public void setPlayerNickname(String value) {
        this.playerNickname.set(value);
    }

    public void setPlayerId(int value) {
        this.playerId.set(value);
    }

    public void syncFromBotSettings(BotSettings settings) {
        this.notificationVolume.set(settings.getNotificationVolume());
        this.soundEnabled.set(settings.isSoundEnabled());
        this.antiCapture.set(settings.isAntiCaptureEnabled());
        this.hideBlackChatWarning.set(settings.isHideBlackChatWarning());
        this.useWindowCapture.set(settings.isUseWindowCapture());
        this.detectionEnabled.set(settings.isDetectionEnabled());
        this.playerNickname.set(settings.getPlayerNickname() != null ? settings.getPlayerNickname() : "");
        this.playerId.set(settings.getPlayerId());
        this.detectAdminByColor.set(settings.isDetectAdminByColor());
        this.detectAdminByOCR.set(settings.isDetectAdminByOCR());
        this.detectAdminWarningPopup.set(settings.isDetectAdminWarningPopup());
        this.pauseOnAdminDetected.set(settings.isPauseOnAdminDetected());
        this.alertOnAdminDetected.set(settings.isAlertOnAdminDetected());
        this.detectFullInventory.set(settings.isDetectFullInventory());
        this.pauseOnFullInventory.set(settings.isPauseOnFullInventory());
        this.alertOnFullInventory.set(settings.isAlertOnFullInventory());
        this.detectPrivateMessage.set(settings.isDetectPrivateMessage());
        this.detectMention.set(settings.isDetectMention());
        this.pauseOnPM.set(settings.isPauseOnPM());
        this.alertOnPM.set(settings.isAlertOnPM());
        for (DetectionType type : DetectionType.values()) {
            DetectionActionSettings saved = settings.getDetectionAction(type);
            this.setDetectionActionUI(type, saved);
        }
        List<String> widgetNames = settings.getActiveWidgets();
        if (widgetNames != null && !widgetNames.isEmpty()) {
            this.activeWidgets.clear();
            for (String name : widgetNames) {
                try {
                    if ("STATS".equals(name)) {
                        this.activeWidgets.add(WidgetType.ACCURACY);
                        this.activeWidgets.add(WidgetType.COAL_PRICE);
                        continue;
                    }
                    this.activeWidgets.add(WidgetType.valueOf(name));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
    }

    public void syncToBotSettings(BotSettings settings) {
        settings.setNotificationVolume(this.notificationVolume.get());
        settings.setSoundEnabled(this.soundEnabled.get());
        settings.setAntiCaptureEnabled(this.antiCapture.get());
        settings.setHideBlackChatWarning(this.hideBlackChatWarning.get());
        settings.setUseWindowCapture(this.useWindowCapture.get());
        settings.setDetectionEnabled(this.detectionEnabled.get());
        settings.setPlayerNickname(this.playerNickname.get());
        settings.setPlayerId(this.playerId.get());
        settings.setDetectAdminByColor(this.detectAdminByColor.get());
        settings.setDetectAdminByOCR(this.detectAdminByOCR.get());
        settings.setDetectAdminWarningPopup(this.detectAdminWarningPopup.get());
        settings.setPauseOnAdminDetected(this.pauseOnAdminDetected.get());
        settings.setAlertOnAdminDetected(this.alertOnAdminDetected.get());
        settings.setIgnoreAdminDetection(!this.pauseOnAdminDetected.get() && !this.alertOnAdminDetected.get());
        settings.setDetectFullInventory(this.detectFullInventory.get());
        settings.setPauseOnFullInventory(this.pauseOnFullInventory.get());
        settings.setAlertOnFullInventory(this.alertOnFullInventory.get());
        settings.setIgnoreFullInventory(!this.pauseOnFullInventory.get() && !this.alertOnFullInventory.get());
        settings.setDetectPrivateMessage(this.detectPrivateMessage.get());
        settings.setDetectMention(this.detectMention.get());
        settings.setPauseOnPM(this.pauseOnPM.get());
        settings.setAlertOnPM(this.alertOnPM.get());
        for (DetectionType type : DetectionType.values()) {
            DetectionActionSettings uiSettings = this.detectionActionCache.get((Object)type);
            if (uiSettings == null) continue;
            settings.getDetectionAction(type).copyFrom(uiSettings);
        }
        ArrayList<String> widgetNames = new ArrayList<String>();
        for (WidgetType w : this.activeWidgets) {
            widgetNames.add(w.name());
        }
        settings.setActiveWidgets(widgetNames);
        settings.notifyListeners();
        settings.save();
    }

    private void renderDebugWindow() {
        ImGui.setNextWindowSize(600.0f, 500.0f, 4);
        if (ImGui.begin("Debug - Podglad regionow", 0)) {
            DebugCaptures captures = DebugCaptures.getInstance();
            String[] names = captures.getCaptureNames();
            if (ImGui.beginTabBar("DebugTabs")) {
                if (ImGui.beginTabItem("Status")) {
                    ImGui.spacing();
                    String lastStopReason = captures.getLastStopReason();
                    long lastStopTime = captures.getLastStopTime();
                    if (lastStopReason != null && !lastStopReason.isEmpty()) {
                        ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, LifebotImGuiUI.COLOR_ACCENT_RED.w, "Ostatnie zatrzymanie:");
                        ImGui.spacing();
                        long timeSinceStop = System.currentTimeMillis() - lastStopTime;
                        String timeAgo = timeSinceStop < 60000L ? timeSinceStop / 1000L + " sek temu" : (timeSinceStop < 3600000L ? timeSinceStop / 60000L + " min temu" : timeSinceStop / 3600000L + " godz temu");
                        ImGui.bulletText("Powod: " + lastStopReason);
                        ImGui.bulletText("Czas: " + timeAgo);
                    } else {
                        ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, "Bot nie byl jeszcze zatrzymany");
                    }
                    ImGui.spacing();
                    ImGui.separator();
                    ImGui.spacing();
                    ImGui.text("Ilosc wegla: ");
                    ImGui.sameLine();
                    ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, LifebotImGuiUI.COLOR_ACCENT_GREEN.w, captures.getRecognizedCoalAmount());
                    ImGui.spacing();
                    ImGui.separator();
                    ImGui.spacing();
                    ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_PRIMARY.x, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.y, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.z, LifebotImGuiUI.COLOR_ACCENT_PRIMARY.w, "Diagnostyka window capture:");
                    ImGui.spacing();
                    String captureInfo = captures.getCaptureInfo();
                    if (captureInfo != null && !captureInfo.isEmpty()) {
                        ImGui.bulletText(captureInfo);
                    } else {
                        ImGui.bulletText("Brak danych");
                    }
                    int[] borders = captures.getWindowBorders();
                    if (borders != null && borders.length == 4) {
                        ImGui.bulletText(String.format("Obramowanie: L=%d T=%d R=%d B=%d", borders[0], borders[1], borders[2], borders[3]));
                    }
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Klawisze")) {
                    ImGui.spacing();
                    ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, LifebotImGuiUI.COLOR_ACCENT_GREEN.w, "Nacisnij dowolny klawisz aby zobaczyc jego ID");
                    ImGui.spacing();
                    ImGui.separator();
                    ImGui.spacing();
                    int lastKeyCode = captures.getLastKeyCode();
                    String lastKeyName = captures.getLastKeyName();
                    long lastKeyTime = captures.getLastKeyTime();
                    if (lastKeyCode != 0) {
                        ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_CYAN.x, LifebotImGuiUI.COLOR_ACCENT_CYAN.y, LifebotImGuiUI.COLOR_ACCENT_CYAN.z, LifebotImGuiUI.COLOR_ACCENT_CYAN.w, "Kod klawisza:");
                        ImGui.sameLine();
                        ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_YELLOW.x, LifebotImGuiUI.COLOR_ACCENT_YELLOW.y, LifebotImGuiUI.COLOR_ACCENT_YELLOW.z, LifebotImGuiUI.COLOR_ACCENT_YELLOW.w, String.valueOf(lastKeyCode));
                        ImGui.spacing();
                        ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_CYAN.x, LifebotImGuiUI.COLOR_ACCENT_CYAN.y, LifebotImGuiUI.COLOR_ACCENT_CYAN.z, LifebotImGuiUI.COLOR_ACCENT_CYAN.w, "Nazwa klawisza:");
                        ImGui.sameLine();
                        ImGui.text(lastKeyName);
                        ImGui.spacing();
                        long ago = System.currentTimeMillis() - lastKeyTime;
                        String timeStr = ago < 1000L ? "teraz" : (ago < 60000L ? String.format("%.0f sek temu", (double)ago / 1000.0) : String.format("%.1f min temu", (double)ago / 60000.0));
                        ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, "Czas: " + timeStr);
                        ImGui.spacing();
                        ImGui.separator();
                        ImGui.spacing();
                        ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_CYAN.x, LifebotImGuiUI.COLOR_ACCENT_CYAN.y, LifebotImGuiUI.COLOR_ACCENT_CYAN.z, LifebotImGuiUI.COLOR_ACCENT_CYAN.w, "Popularne kody klawiszy:");
                        ImGui.spacing();
                        ImGui.bulletText("END = 3663");
                        ImGui.bulletText("INSERT = 3666");
                        ImGui.bulletText("HOME = 3665");
                        ImGui.bulletText("DELETE = 3667");
                        ImGui.bulletText("PAGE UP = 3664");
                        ImGui.bulletText("PAGE DOWN = 3668");
                    } else {
                        ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, "Oczekiwanie na nacisniecie klawisza...");
                    }
                    ImGui.endTabItem();
                }
                for (String name : names) {
                    if (!ImGui.beginTabItem(name)) continue;
                    BufferedImage image = captures.getCapture(name);
                    ImGui.spacing();
                    if (image != null) {
                        ImGui.pushStyleColor(21, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.3f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.3f, 1.0f);
                        ImGui.pushStyleColor(22, LifebotImGuiUI.COLOR_ACCENT_GREEN.x * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.y * 0.5f, LifebotImGuiUI.COLOR_ACCENT_GREEN.z * 0.5f, 1.0f);
                        if (ImGui.button("\uf0c7 Save##" + name, 80.0f, 24.0f)) {
                            this.saveDebugRegion(name, image);
                        }
                        ImGui.popStyleColor(2);
                        if (ImGui.isItemHovered()) {
                            ImGui.setTooltip("Zapisz aktualny stan regionu do pliku");
                        }
                    }
                    if (name.equals("Chat")) {
                        ImGui.sameLine();
                        if (ImGui.button("Force Capture##chat", 110.0f, 24.0f) && this.callback != null) {
                            this.callback.onForceChatCapture();
                        }
                    }
                    ImGui.separator();
                    if (image != null) {
                        int textureId = this.getOrCreateDebugTexture(name, image);
                        if (textureId > 0) {
                            BufferedImage ocrImage;
                            float availWidth = ImGui.getContentRegionAvailX();
                            float availHeight = ImGui.getContentRegionAvailY() - 50.0f;
                            float imgWidth = image.getWidth();
                            float imgHeight = image.getHeight();
                            float scaleX = availWidth / imgWidth;
                            float scaleY = availHeight / imgHeight;
                            float scale = Math.min(Math.min(scaleX, scaleY), 4.0f);
                            scale = Math.max(scale, 1.0f);
                            float displayWidth = imgWidth * scale;
                            float displayHeight = imgHeight * scale;
                            ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, String.format("Rozmiar: %dx%d px | Skala: %.1fx", image.getWidth(), image.getHeight(), Float.valueOf(scale)));
                            if (name.equals("Ilosc wegla")) {
                                ImGui.sameLine();
                                ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_GREEN.x, LifebotImGuiUI.COLOR_ACCENT_GREEN.y, LifebotImGuiUI.COLOR_ACCENT_GREEN.z, LifebotImGuiUI.COLOR_ACCENT_GREEN.w, " | Rozpoznano: " + captures.getRecognizedCoalAmount());
                                ImGui.sameLine();
                                ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_CYAN.x, LifebotImGuiUI.COLOR_ACCENT_CYAN.y, LifebotImGuiUI.COLOR_ACCENT_CYAN.z, LifebotImGuiUI.COLOR_ACCENT_CYAN.w, " | OCR: " + captures.getOcrTimeMs() + "ms");
                            }
                            ImGui.separator();
                            ImGui.image((long)textureId, displayWidth, displayHeight);
                            if (name.equals("Ilosc wegla") && (ocrImage = captures.getOcrPreprocessedImage()) != null) {
                                ImGui.spacing();
                                ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_CYAN.x, LifebotImGuiUI.COLOR_ACCENT_CYAN.y, LifebotImGuiUI.COLOR_ACCENT_CYAN.z, LifebotImGuiUI.COLOR_ACCENT_CYAN.w, "Obraz po preprocessingu (co idzie do OCR):");
                                int ocrTexId = this.getOrCreateDebugTexture("ocr_preprocessed", ocrImage);
                                if (ocrTexId > 0) {
                                    ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, String.format("Rozmiar: %dx%d px", ocrImage.getWidth(), ocrImage.getHeight()));
                                    ImGui.image((long)ocrTexId, ocrImage.getWidth(), ocrImage.getHeight());
                                }
                            }
                        } else {
                            ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, LifebotImGuiUI.COLOR_ACCENT_RED.w, "Blad tworzenia tekstury");
                        }
                    } else {
                        ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, "Brak obrazu");
                    }
                    ImGui.endTabItem();
                }
                if (ImGui.beginTabItem("Admin Warn")) {
                    this.renderAdminWarningDebugTab(captures);
                    ImGui.endTabItem();
                }
                ImGui.endTabBar();
            }
            ImGui.separator();
            if (ImGui.button("Zamknij", 100.0f, 25.0f)) {
                this.showDebug = false;
            }
            ImGui.sameLine();
            if (ImGui.button("Odswiez tekstury", 130.0f, 25.0f)) {
                this.clearDebugTextures();
            }
            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();
            ImGui.textColored(LifebotImGuiUI.COLOR_TEXT_SECONDARY.x, LifebotImGuiUI.COLOR_TEXT_SECONDARY.y, LifebotImGuiUI.COLOR_TEXT_SECONDARY.z, LifebotImGuiUI.COLOR_TEXT_SECONDARY.w, "Zapisz wzorzec:");
            ImGui.sameLine();
            if (ImGui.button("50kg", 50.0f, 25.0f)) {
                this.saveInventoryPattern(50);
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Zapisz wzorzec pelnego ekwipunku 50kg");
            }
            ImGui.sameLine();
            if (ImGui.button("60kg", 50.0f, 25.0f)) {
                this.saveInventoryPattern(60);
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Zapisz wzorzec pelnego ekwipunku 60kg");
            }
            ImGui.sameLine();
            if (ImGui.button("75kg", 50.0f, 25.0f)) {
                this.saveInventoryPattern(75);
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Zapisz wzorzec pelnego ekwipunku 75kg");
            }
        }
        ImGui.end();
    }

    private void renderAdminWarningDebugTab(DebugCaptures captures) {
        AdminWarningChecker checker = captures.getAdminWarningChecker();
        if (checker == null) {
            ImGui.textColored(LifebotImGuiUI.COLOR_ACCENT_RED.x, LifebotImGuiUI.COLOR_ACCENT_RED.y, LifebotImGuiUI.COLOR_ACCENT_RED.z, 1.0f, "AdminWarningChecker nie zosta\u0142 zainicjalizowany");
            return;
        }
        ImGui.spacing();
        int matches = checker.getLastMatches();
        int minRequired = checker.getMinMatches();
        int total = checker.getSamplePoints().length;
        boolean detected = matches >= minRequired;
        ImGui.text("Status: ");
        ImGui.sameLine();
        if (detected) {
            ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, "WYKRYTO WARNING!");
        } else {
            ImGui.textColored(0.3f, 0.9f, 0.3f, 1.0f, "Brak warninga");
        }
        ImGui.spacing();
        ImGui.text(String.format("Dopasowania: %d / %d (wymagane: %d)", matches, total, minRequired));
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.text("Kryteria detekcji (czerwonawy kolor):");
        ImGui.text(String.format("  Min R: %d", checker.getMinRed()));
        ImGui.text(String.format("  R - G >= %d", checker.getRedDominance()));
        ImGui.text(String.format("  R - B >= %d", checker.getRedDominance()));
        ImGui.text(String.format("  Max G/B: %d", checker.getMaxGreenBlue()));
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        ImGui.text("Punkty pr\u00f3bkowania:");
        ImGui.spacing();
        ImDrawList drawList = ImGui.getWindowDrawList();
        Point[] points = checker.getSamplePoints();
        Color[] colors = checker.getLastColors();
        boolean[] matchResults = checker.getLastMatchResults();
        String[] pointNames = new String[]{"Lewy-g\u00f3rny r\u00f3g", "Prawy-g\u00f3rny r\u00f3g", "Lewy-dolny r\u00f3g", "Prawy-dolny r\u00f3g", "Lewy \u015brodek", "Prawy \u015brodek", "G\u00f3ra \u015brodek", "D\u00f3\u0142 \u015brodek", "Wewn. lewy-g\u00f3rny"};
        for (int i = 0; i < points.length && i < pointNames.length; ++i) {
            boolean isMatch;
            Point p = points[i];
            Color c = colors != null && i < colors.length ? colors[i] : null;
            boolean bl = isMatch = matchResults != null && i < matchResults.length && matchResults[i];
            if (isMatch) {
                ImGui.textColored(0.3f, 0.9f, 0.3f, 1.0f, "\u2713");
            } else {
                ImGui.textColored(0.9f, 0.3f, 0.3f, 1.0f, "\u2717");
            }
            ImGui.sameLine();
            ImGui.text(String.format("%s (%d,%d): ", pointNames[i], p.x, p.y));
            ImGui.sameLine();
            if (c != null) {
                float cbX = ImGui.getCursorScreenPosX();
                float cbY = ImGui.getCursorScreenPosY();
                int rgb = this.colorU32((float)c.getRed() / 255.0f, (float)c.getGreen() / 255.0f, (float)c.getBlue() / 255.0f, 1.0f);
                drawList.addRectFilled(cbX, cbY, cbX + 16.0f, cbY + 14.0f, rgb);
                drawList.addRect(cbX, cbY, cbX + 16.0f, cbY + 14.0f, this.colorU32(1.0f, 1.0f, 1.0f, 0.3f));
                ImGui.dummy(18.0f, 14.0f);
                ImGui.sameLine();
                ImGui.text(String.format("RGB(%d,%d,%d)", c.getRed(), c.getGreen(), c.getBlue()));
                continue;
            }
            ImGui.textColored(0.5f, 0.5f, 0.5f, 1.0f, "brak danych");
        }
    }

    private void saveDebugRegion(String regionName, BufferedImage image) {
        if (this.callback != null && image != null) {
            this.callback.onSaveDebugRegion(regionName, image);
            String fileName = "debug_" + regionName.replace(" ", "_").toLowerCase() + "_" + System.currentTimeMillis() + ".png";
            ImGuiNotifications.getInstance().show(ImGuiNotifications.Type.SUCCESS, ImGuiNotifications.Location.BOTTOM_CENTER, 3000L, "Zapisano: " + fileName);
        }
    }

    private void saveInventoryPattern(int limitKg) {
        boolean saved;
        if (this.callback != null && (saved = this.callback.onSaveInventoryPattern(limitKg))) {
            ImGuiNotifications.getInstance().show(ImGuiNotifications.Type.SUCCESS, ImGuiNotifications.Location.BOTTOM_CENTER, 3000L, "Zapisano wzorzec: full_" + limitKg + ".png");
        }
    }

    private int getOrCreateDebugTexture(String name, BufferedImage image) {
        boolean needsUpdate;
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = this.debugTextureTimestamps.get(name);
        Integer existingId = this.debugTextureCache.get(name);
        boolean bl = needsUpdate = existingId == null || lastUpdate == null || currentTime - lastUpdate > 500L;
        if (needsUpdate) {
            if (existingId != null && existingId > 0) {
                GL11.glDeleteTextures(existingId);
            }
            int textureId = this.createTextureFromImage(image);
            this.debugTextureCache.put(name, textureId);
            this.debugTextureTimestamps.put(name, currentTime);
            return textureId;
        }
        return existingId != null ? existingId : 0;
    }

    private int createTextureFromImage(BufferedImage image) {
        if (image == null) {
            return 0;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage argbImage = image;
        if (image.getType() != 2) {
            argbImage = new BufferedImage(width, height, 2);
            Graphics2D g = argbImage.createGraphics();
            g.drawImage((Image)image, 0, 0, null);
            g.dispose();
        }
        int[] pixels = new int[width * height];
        argbImage.getRGB(0, 0, width, height, pixels, 0, width);
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int pixel = pixels[y * width + x];
                buffer.put((byte)(pixel >> 16 & 0xFF));
                buffer.put((byte)(pixel >> 8 & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)(pixel >> 24 & 0xFF));
            }
        }
        buffer.flip();
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(3553, textureId);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, buffer);
        return textureId;
    }

    private void clearDebugTextures() {
        for (Integer textureId : this.debugTextureCache.values()) {
            if (textureId == null || textureId <= 0) continue;
            GL11.glDeleteTextures(textureId);
        }
        this.debugTextureCache.clear();
        this.debugTextureTimestamps.clear();
    }

    public void toggleDebugWindow() {
        this.showDebug = !this.showDebug;
    }

    public boolean isDebugWindowVisible() {
        return this.showDebug;
    }

    @Generated
    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    @Generated
    public boolean isChatHasBlackBackground() {
        return this.chatHasBlackBackground;
    }

    @Generated
    public boolean isUiVisible() {
        return this.uiVisible;
    }

    @Generated
    public float getUiAlpha() {
        return this.uiAlpha;
    }

    @Generated
    public TutorialWindow getTutorialWindow() {
        return this.tutorialWindow;
    }

    @Generated
    public void setCallback(SettingsCallback callback) {
        this.callback = callback;
    }

    public static enum WidgetType {
        LEARNING("\uf19d Uczenie", "Pasek postepu uczenia i status nagrywania"),
        STATUS("\uf1ce Status", "Aktualny status bota (wlaczony/wylaczony)"),
        RECOGNITION("\uf06e Litery", "Wykryte litery z minigry"),
        LEVELS("\uf5fd Poziomy", "Twoj poziom i wymagany poziom"),
        ACCURACY("\uf05b Precyzja", "Aktualna precyzja rozpoznawania liter"),
        COAL_PRICE("\uf155 Cena wegla", "Aktualna cena wegla na gieldzie"),
        SEPARATOR("\uf7a4 Separator", "Pozioma linia rozdzielajaca sekcje"),
        CONTROLS("\uf11b Sterowanie", "Przycisk start/stop bota");

        public final String displayName;
        public final String description;

        private WidgetType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }

    public static interface SettingsCallback {
        public void onBotToggle(boolean var1);

        public void onSettingChanged(String var1, Object var2);

        public void onAntiCaptureChanged(boolean var1);

        public void onResetLearning();

        default public void onPlaySound(String soundId, int volume) {
        }

        default public void onStopAllSounds() {
        }

        default public boolean isSoundPlaying() {
            return false;
        }

        default public void onRefreshSounds() {
        }

        default public void onRenameSound(String fileName, String newDisplayName) {
        }

        default public void onOpenSoundsFolder() {
        }

        default public void onTestNotification(String type) {
        }

        default public boolean onSaveInventoryPattern(int limitKg) {
            return false;
        }

        default public void onSaveDebugRegion(String regionName, BufferedImage image) {
        }

        default public void onForceChatCapture() {
        }

        default public void onOpenScreenshotsFolder() {
        }
    }

    public static class CustomSoundInfo {
        public String fileName;
        public String displayName;

        public CustomSoundInfo(String fileName, String displayName) {
            this.fileName = fileName;
            this.displayName = displayName;
        }

        public String getDisplayNameOrFileName() {
            return this.displayName != null && !this.displayName.isEmpty() ? this.displayName : this.fileName;
        }

        public boolean hasDisplayName() {
            return this.displayName != null && !this.displayName.isEmpty();
        }
    }
}

