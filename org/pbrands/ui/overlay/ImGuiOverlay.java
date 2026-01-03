/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.sound.SoundUtils
 *  org.pbrands.ui.overlay.ImGuiOverlay$KeyEvent
 */
package org.pbrands.ui.overlay;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import imgui.ImDrawList;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.pbrands.netty.handler.MiningBotClientHandler;
import org.pbrands.sound.Sound;
import org.pbrands.sound.SoundUtils;
import org.pbrands.ui.overlay.ImGuiNotifications;
import org.pbrands.ui.overlay.ImGuiOverlay;
import org.pbrands.ui.overlay.LifebotImGuiUI;
import org.pbrands.ui.overlay.MiniGameWindow;
import org.pbrands.util.FontAwesomeIcons;
import org.pbrands.util.ObfuscatedStorage;

public class ImGuiOverlay {
    private long windowPtr;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    public static final String targetWindowTitle = "MTA: San Andreas";
    private WinDef.HWND targetHwnd = null;
    private static String iniFilePath = "imgui_overlay.ini";
    private WinDef.HWND overlayHwnd = null;
    private int lastX;
    private int lastY;
    private int lastW;
    private int lastH;
    private boolean overlayVisible = false;
    private boolean userHidden = false;
    private long lastVisibleTime = 0L;
    private long lastFrameTime = System.nanoTime();
    private long lastSaveTime = 0L;
    private static final long MIN_VISIBLE_TIME_FOR_SAVE = 500L;
    private static final long MIN_SAVE_INTERVAL = 2000L;
    private final LifebotImGuiUI lifebotUI = new LifebotImGuiUI();
    private volatile boolean loadingMode = false;
    private volatile String loadingStatus = "Inicjalizacja...";
    private float loadingAnimTime = 0.0f;
    private float loadingSpinnerAngle = 0.0f;
    private float loadingPulsePhase = 0.0f;
    private float loadingFadeAlpha = 0.0f;
    private static final int LOADING_WINDOW_WIDTH = 420;
    private static final int LOADING_WINDOW_HEIGHT = 180;
    private boolean showDemoWindow = false;
    private boolean showDebugWindow = false;
    private float[] testColor = new float[]{0.4f, 0.7f, 0.0f, 1.0f};
    private int clickCount = 0;
    private ImString textInputBuffer = new ImString(256);
    private boolean isCursorOverImGui = false;
    private final ConcurrentLinkedQueue<KeyEvent> keyEventQueue = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<Character> charInputQueue = new ConcurrentLinkedQueue();
    private volatile boolean shiftDown = false;
    private volatile boolean ctrlDown = false;
    private volatile boolean altDown = false;
    private WinUser.HHOOK keyboardHook;
    private WinUser.LowLevelKeyboardProc keyboardProc;
    private Thread keyboardHookThread;
    private final AtomicBoolean wantTextInput = new AtomicBoolean(false);
    private final AtomicBoolean blockHotkeys = new AtomicBoolean(true);
    private static final int VK_END_KEY = 35;
    private static final int VK_INSERT_KEY = 45;
    private HotkeyListener hotkeyListener;
    private static final int WS_EX_TRANSPARENT = 32;
    private static final int WS_EX_LAYERED = 524288;
    private static final int WS_EX_TOPMOST = 8;
    private static final int WS_EX_TOOLWINDOW = 128;
    private static final int WS_EX_APPWINDOW = 262144;
    private static final int WS_EX_NOACTIVATE = 0x8000000;
    private static final int GWL_EXSTYLE = -20;
    private static final WinDef.HWND HWND_TOPMOST = new WinDef.HWND(new Pointer(-1L));
    private static final int SWP_NOSIZE = 1;
    private static final int SWP_NOMOVE = 2;
    private static final int SWP_NOACTIVATE = 16;
    private static final int SWP_SHOWWINDOW = 64;
    private static final int SWP_HIDEWINDOW = 128;
    private boolean initialAntiCapture = false;
    private boolean antiCaptureEnabled = false;
    private static final int VK_TAB = 9;
    private static final int VK_CAPITAL = 20;
    private static final int VK_LEFT = 37;
    private static final int VK_UP = 38;
    private static final int VK_RIGHT = 39;
    private static final int VK_DOWN = 40;
    private static final int VK_PRIOR = 33;
    private static final int VK_NEXT = 34;
    private static final int VK_HOME = 36;
    private static final int VK_END = 35;
    private static final int VK_INSERT = 45;
    private static final int VK_DELETE = 46;
    private static final int VK_BACK = 8;
    private static final int VK_LSHIFT = 160;
    private static final int VK_RSHIFT = 161;
    private static final int VK_LCONTROL = 162;
    private static final int VK_RCONTROL = 163;
    private static final int VK_LMENU = 164;
    private static final int VK_RMENU = 165;
    private static final int WDA_NONE = 0;
    private static final int WDA_MONITOR = 1;
    private static final int WDA_EXCLUDEFROMCAPTURE = 17;
    private static final int VK_SPACE = 32;
    private static final int VK_RETURN = 13;
    private static final int VK_ESCAPE = 27;
    private static final int VK_SHIFT = 16;
    private static final int VK_CONTROL = 17;
    private static final int VK_MENU = 18;
    private static final File CUSTOM_SOUNDS_DIR = new File(System.getProperty("user.home"), ".lifebot/sounds");
    private final CopyOnWriteArrayList<Clip> customPlayingClips = new CopyOnWriteArrayList();
    private static final File SOUND_NAMES_FILE = new File(CUSTOM_SOUNDS_DIR, "sound_names.properties");
    private final Properties soundNamesConfig = new Properties();

    public void setHotkeyListener(HotkeyListener listener) {
        this.hotkeyListener = listener;
    }

    public ImGuiOverlay() {
        this(false);
    }

    public ImGuiOverlay(boolean initialAntiCapture) {
        this.initialAntiCapture = initialAntiCapture;
        this.antiCaptureEnabled = initialAntiCapture;
    }

    public void run() {
        this.init();
        this.loop();
        this.destroy();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(131076, 0);
        GLFW.glfwWindowHint(131075, 0);
        GLFW.glfwWindowHint(131077, 0);
        GLFW.glfwWindowHint(131079, 1);
        GLFW.glfwWindowHint(131082, 1);
        this.windowPtr = GLFW.glfwCreateWindow(800, 600, "Overlay", 0L, 0L);
        if (this.windowPtr == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        long hwndPtr = GLFWNativeWin32.glfwGetWin32Window(this.windowPtr);
        this.overlayHwnd = new WinDef.HWND(new Pointer(hwndPtr));
        int exStyle = User32.INSTANCE.GetWindowLong(this.overlayHwnd, -20);
        exStyle &= 0xFFFBFFFF;
        User32.INSTANCE.SetWindowLong(this.overlayHwnd, -20, exStyle |= 0x8080080);
        this.setClickThrough(true);
        if (this.initialAntiCapture) {
            int affinity = 17;
            boolean success = User32Ext.INSTANCE.SetWindowDisplayAffinity(this.overlayHwnd, affinity);
            System.out.println("Initial anti-capture applied (success: " + success + ")");
        }
        User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 19);
        GLFW.glfwMakeContextCurrent(this.windowPtr);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        this.loadEncryptedIni();
        io.getFonts().setFreeTypeRenderer(true);
        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(new short[]{32, 255, 256, 591, 8192, 8303, 8448, 8527, 8592, 8703, 9472, 9599, 9600, 9631, 9632, 9727, 9728, 9983, 9984, 10175, 10240, 10495, 12288, 12351, 0});
        rangesBuilder.addRanges(FontAwesomeIcons._IconRange);
        short[] glyphRanges = rangesBuilder.buildRanges();
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(true);
        byte[] fontData = this.loadFontFromResources("/fonts/Nunito-Black.ttf");
        if (fontData != null) {
            byte[] faSolid;
            byte[] faRegular;
            io.getFonts().addFontFromMemoryTTF(fontData, 18.0f, glyphRanges);
            System.out.println("Loaded Nunito-Black font from resources");
            byte[] emojiFont = this.loadFontFromResources("/fonts/NotoEmoji-Regular.ttf");
            if (emojiFont != null) {
                io.getFonts().addFontFromMemoryTTF(emojiFont, 16.0f, fontConfig, glyphRanges);
                System.out.println("Loaded NotoEmoji font for emoji support");
            }
            if ((faRegular = this.loadFontFromResources("/fonts/fa-regular-400.ttf")) != null) {
                io.getFonts().addFontFromMemoryTTF(faRegular, 16.0f, fontConfig, glyphRanges);
                System.out.println("Loaded Font Awesome Regular");
            }
            if ((faSolid = this.loadFontFromResources("/fonts/fa-solid-900.ttf")) != null) {
                io.getFonts().addFontFromMemoryTTF(faSolid, 16.0f, fontConfig, glyphRanges);
                System.out.println("Loaded Font Awesome Solid");
            }
        } else {
            String fontPath = "C:\\Windows\\Fonts\\segoeui.ttf";
            if (!new File(fontPath).exists()) {
                fontPath = "C:\\Windows\\Fonts\\arial.ttf";
            }
            if (new File(fontPath).exists()) {
                io.getFonts().addFontFromFileTTF(fontPath, 18.0f, glyphRanges);
            }
        }
        io.getFonts().build();
        fontConfig.destroy();
        int logoTextureId = this.loadTextureFromResources("/images/logo.png");
        if (logoTextureId > 0) {
            this.lifebotUI.setLogoTexture(logoTextureId, 2000, 400);
        }
        this.lifebotUI.applyTheme();
        this.imGuiGlfw.init(this.windowPtr, true);
        this.imGuiGl3.init("#version 130");
        this.updateCustomSoundsList();
        this.setupKeyboardHook();
        if (this.loadingMode) {
            this.updateLoadingWindowPosition();
        }
    }

    private void setupKeyboardHook() {
        this.keyboardProc = new WinUser.LowLevelKeyboardProc(){
            private boolean lShiftDown = false;
            private boolean rShiftDown = false;
            private boolean lCtrlDown = false;
            private boolean rCtrlDown = false;
            private boolean lAltDown = false;
            private boolean rAltDown = false;

            @Override
            public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
                if (nCode >= 0) {
                    boolean isUp;
                    int vkCode = lParam.vkCode;
                    int scanCode = lParam.scanCode;
                    boolean isDown = wParam.intValue() == 256 || wParam.intValue() == 260;
                    boolean bl = isUp = wParam.intValue() == 257 || wParam.intValue() == 261;
                    if (vkCode == 115 && isDown && (MiningBotClientHandler.isAdmin() || MiningBotClientHandler.isBetaTester())) {
                        ImGuiOverlay.this.lifebotUI.toggleDebugWindow();
                    }
                    switch (vkCode) {
                        case 160: {
                            this.lShiftDown = isDown;
                            break;
                        }
                        case 161: {
                            this.rShiftDown = isDown;
                            break;
                        }
                        case 162: {
                            this.lCtrlDown = isDown;
                            break;
                        }
                        case 163: {
                            this.rCtrlDown = isDown;
                            break;
                        }
                        case 164: {
                            this.lAltDown = isDown;
                            break;
                        }
                        case 165: {
                            this.rAltDown = isDown;
                            break;
                        }
                        case 16: {
                            this.lShiftDown = isDown;
                            this.rShiftDown = isDown;
                            break;
                        }
                        case 17: {
                            this.lCtrlDown = isDown;
                            this.rCtrlDown = isDown;
                            break;
                        }
                        case 18: {
                            this.lAltDown = isDown;
                            this.rAltDown = isDown;
                        }
                    }
                    ImGuiOverlay.this.shiftDown = this.lShiftDown || this.rShiftDown;
                    ImGuiOverlay.this.ctrlDown = this.lCtrlDown || this.rCtrlDown;
                    ImGuiOverlay.this.altDown = this.lAltDown || this.rAltDown;
                    MiniGameWindow miniGameWindow = ImGuiOverlay.this.lifebotUI.getMiniGameWindow();
                    if (miniGameWindow != null && miniGameWindow.isFocused() && miniGameWindow.shouldCaptureKey(vkCode)) {
                        boolean overlayActive;
                        WinDef.HWND fgHwnd = User32.INSTANCE.GetForegroundWindow();
                        boolean mtaActive = fgHwnd != null && fgHwnd.equals(ImGuiOverlay.this.targetHwnd);
                        boolean bl2 = overlayActive = fgHwnd != null && ImGuiOverlay.this.overlayHwnd != null && fgHwnd.equals(ImGuiOverlay.this.overlayHwnd);
                        if (mtaActive || overlayActive) {
                            if (isDown) {
                                miniGameWindow.handleKeyPress(vkCode);
                            } else if (isUp) {
                                miniGameWindow.handleRelease(vkCode);
                            }
                            return new WinDef.LRESULT(1L);
                        }
                    }
                    boolean passthroughEnabled = ImGuiOverlay.this.lifebotUI.isPassthroughHotkeys();
                    if (isDown) {
                        if (vkCode == 35) {
                            System.out.println("[HOOK] END key pressed! passthrough=" + passthroughEnabled + ", listener=" + (ImGuiOverlay.this.hotkeyListener != null));
                            if (!passthroughEnabled && ImGuiOverlay.this.hotkeyListener != null) {
                                ImGuiOverlay.this.hotkeyListener.onToggleBot();
                            }
                            if (!passthroughEnabled) {
                                return new WinDef.LRESULT(1L);
                            }
                        }
                        if (vkCode == 45) {
                            System.out.println("[HOOK] INSERT key pressed! passthrough=" + passthroughEnabled + ", listener=" + (ImGuiOverlay.this.hotkeyListener != null));
                            if (!passthroughEnabled) {
                                if (ImGuiOverlay.this.hotkeyListener != null) {
                                    ImGuiOverlay.this.hotkeyListener.onToggleOverlay();
                                }
                                ImGuiOverlay.this.toggleVisibility();
                                return new WinDef.LRESULT(1L);
                            }
                        }
                    }
                    if (ImGuiOverlay.this.wantTextInput.get()) {
                        boolean passToSystem = ImGuiOverlay.this.shouldPassKeyToSystem(vkCode, this.lAltDown || this.rAltDown, this.lCtrlDown || this.rCtrlDown);
                        int imGuiKey = ImGuiOverlay.this.mapVkCodeToImGui(vkCode);
                        if (imGuiKey != 0) {
                            ImGuiOverlay.this.keyEventQueue.add(new KeyEvent(imGuiKey, isDown));
                        }
                        if (isDown && !passToSystem) {
                            short capsState;
                            byte[] keyState = new byte[256];
                            if (this.lShiftDown || this.rShiftDown) {
                                keyState[16] = -128;
                            }
                            if (this.lCtrlDown || this.rCtrlDown) {
                                keyState[17] = -128;
                            }
                            if (this.lAltDown || this.rAltDown) {
                                keyState[18] = -128;
                            }
                            if (this.lShiftDown) {
                                keyState[160] = -128;
                            }
                            if (this.rShiftDown) {
                                keyState[161] = -128;
                            }
                            if (this.lCtrlDown) {
                                keyState[162] = -128;
                            }
                            if (this.rCtrlDown) {
                                keyState[163] = -128;
                            }
                            if (this.lAltDown) {
                                keyState[164] = -128;
                            }
                            if (this.rAltDown) {
                                keyState[165] = -128;
                            }
                            if (((capsState = User32Ext.INSTANCE.GetKeyState(20)) & 1) != 0) {
                                keyState[20] = 1;
                            }
                            keyState[vkCode] = -128;
                            char[] buffer = new char[2];
                            WinDef.HKL layout = User32Ext.INSTANCE.GetKeyboardLayout(0);
                            int result = User32Ext.INSTANCE.ToUnicodeEx(vkCode, scanCode, keyState, buffer, 2, 0, layout);
                            if (result == 1 && buffer[0] >= ' ') {
                                ImGuiOverlay.this.charInputQueue.add(Character.valueOf(buffer[0]));
                            }
                        }
                        if (!passToSystem) {
                            return new WinDef.LRESULT(1L);
                        }
                    }
                }
                return User32.INSTANCE.CallNextHookEx(ImGuiOverlay.this.keyboardHook, nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer())));
            }
        };
        this.keyboardHookThread = new Thread(() -> {
            WinDef.HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
            this.keyboardHook = User32.INSTANCE.SetWindowsHookEx(13, this.keyboardProc, hMod, 0);
            if (this.keyboardHook == null) {
                System.err.println("Failed to install keyboard hook: " + Kernel32.INSTANCE.GetLastError());
                return;
            }
            System.out.println("Keyboard hook installed successfully");
            WinUser.MSG msg = new WinUser.MSG();
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }, "KeyboardHookThread");
        this.keyboardHookThread.setDaemon(true);
        this.keyboardHookThread.start();
    }

    private boolean shouldPassKeyToSystem(int vkCode, boolean altDown, boolean ctrlDown) {
        if (vkCode == 16 || vkCode == 160 || vkCode == 161 || vkCode == 17 || vkCode == 162 || vkCode == 163 || vkCode == 18 || vkCode == 164 || vkCode == 165) {
            return true;
        }
        if (vkCode == 20 || vkCode == 144 || vkCode == 145) {
            return true;
        }
        if (vkCode == 91 || vkCode == 92) {
            return true;
        }
        if (altDown && (vkCode == 9 || vkCode == 27 || vkCode == 115)) {
            return true;
        }
        if (ctrlDown && altDown && vkCode == 46) {
            return true;
        }
        return vkCode == 44;
    }

    private int mapVkCodeToImGui(int vkCode) {
        return switch (vkCode) {
            case 9 -> 512;
            case 37 -> 513;
            case 39 -> 514;
            case 38 -> 515;
            case 40 -> 516;
            case 33 -> 517;
            case 34 -> 518;
            case 36 -> 519;
            case 35 -> 520;
            case 45 -> 521;
            case 46 -> 522;
            case 8 -> 523;
            case 32 -> 524;
            case 13 -> 525;
            case 27 -> 526;
            case 65 -> 546;
            case 66 -> 547;
            case 67 -> 548;
            case 68 -> 549;
            case 69 -> 550;
            case 70 -> 551;
            case 71 -> 552;
            case 72 -> 553;
            case 73 -> 554;
            case 74 -> 555;
            case 75 -> 556;
            case 76 -> 557;
            case 77 -> 558;
            case 78 -> 559;
            case 79 -> 560;
            case 80 -> 561;
            case 81 -> 562;
            case 82 -> 563;
            case 83 -> 564;
            case 84 -> 565;
            case 85 -> 566;
            case 86 -> 567;
            case 87 -> 568;
            case 88 -> 569;
            case 89 -> 570;
            case 90 -> 571;
            case 48 -> 536;
            case 49 -> 537;
            case 50 -> 538;
            case 51 -> 539;
            case 52 -> 540;
            case 53 -> 541;
            case 54 -> 542;
            case 55 -> 543;
            case 56 -> 544;
            case 57 -> 545;
            case 16, 160, 161 -> 528;
            case 17, 162, 163 -> 527;
            case 18, 164, 165 -> 529;
            default -> 0;
        };
    }

    private void processKeyEvent(int vkCode, boolean down) {
        int imGuiKey;
        if (vkCode == 16 || vkCode == 160 || vkCode == 161) {
            this.shiftDown = down;
        }
        if (vkCode == 17 || vkCode == 162 || vkCode == 163) {
            this.ctrlDown = down;
        }
        if (vkCode == 18 || vkCode == 164 || vkCode == 165) {
            this.altDown = down;
        }
        if ((imGuiKey = this.mapVkCodeToImGui(vkCode)) != 0) {
            this.keyEventQueue.add(new KeyEvent(imGuiKey, down));
        }
    }

    private void setClickThrough(boolean clickThrough) {
        int exStyle = User32.INSTANCE.GetWindowLong(this.overlayHwnd, -20);
        int baseFlags = 0x8080080;
        exStyle &= 0xFFFBFFFF;
        exStyle |= baseFlags;
        exStyle = clickThrough ? (exStyle |= 0x20) : (exStyle &= 0xFFFFFFDF);
        User32.INSTANCE.SetWindowLong(this.overlayHwnd, -20, exStyle);
        User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 19);
    }

    public void setAntiCapture(boolean enabled) {
        if (this.overlayHwnd == null) {
            return;
        }
        int affinity = enabled ? 17 : 0;
        boolean success = User32Ext.INSTANCE.SetWindowDisplayAffinity(this.overlayHwnd, affinity);
        System.out.println("Anti-capture " + (enabled ? "enabled" : "disabled") + " (success: " + success + ")");
    }

    public void setBlockHotkeys(boolean block) {
        this.blockHotkeys.set(block);
    }

    public boolean isBlockingHotkeys() {
        return this.blockHotkeys.get();
    }

    public void toggleVisibility() {
        this.userHidden = !this.userHidden;
        this.lifebotUI.toggleVisibility();
    }

    public boolean isUserHidden() {
        return this.userHidden;
    }

    private void loop() {
        System.out.println("Overlay loop started.");
        boolean wasLButtonDown = false;
        boolean wasRButtonDown = false;
        boolean wasMButtonDown = false;
        while (!GLFW.glfwWindowShouldClose(this.windowPtr)) {
            this.updateWindowPosition();
            GLFW.glfwPollEvents();
            WinDef.POINT cursorPos = new WinDef.POINT();
            User32.INSTANCE.GetCursorPos(cursorPos);
            WinDef.RECT windowRect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(this.overlayHwnd, windowRect);
            float clientX = cursorPos.x - windowRect.left;
            float clientY = cursorPos.y - windowRect.top;
            boolean lButtonDown = (User32.INSTANCE.GetAsyncKeyState(1) & 0x8000) != 0;
            boolean rButtonDown = (User32.INSTANCE.GetAsyncKeyState(2) & 0x8000) != 0;
            boolean mButtonDown = (User32.INSTANCE.GetAsyncKeyState(4) & 0x8000) != 0;
            this.imGuiGl3.newFrame();
            this.imGuiGlfw.newFrame();
            ImGui.newFrame();
            ImGuiIO io = ImGui.getIO();
            io.addMousePosEvent(clientX, clientY);
            if (lButtonDown != wasLButtonDown) {
                io.addMouseButtonEvent(0, lButtonDown);
                wasLButtonDown = lButtonDown;
            }
            if (rButtonDown != wasRButtonDown) {
                io.addMouseButtonEvent(1, rButtonDown);
                wasRButtonDown = rButtonDown;
            }
            if (mButtonDown != wasMButtonDown) {
                io.addMouseButtonEvent(2, mButtonDown);
                wasMButtonDown = mButtonDown;
            }
            this.processKeyboardInput(io);
            this.wantTextInput.set(io.getWantTextInput());
            this.isCursorOverImGui = this.checkCursorOverImGui();
            this.setClickThrough(!this.isCursorOverImGui);
            long currentTime = System.nanoTime();
            float deltaTime = (float)(currentTime - this.lastFrameTime) / 1.0E9f;
            this.lastFrameTime = currentTime;
            this.lifebotUI.updateFade(deltaTime);
            if (this.loadingMode) {
                this.loadingAnimTime += deltaTime;
                this.loadingSpinnerAngle += deltaTime * 180.0f;
                this.loadingPulsePhase += deltaTime * 3.0f;
                if (this.loadingFadeAlpha < 1.0f) {
                    this.loadingFadeAlpha = Math.min(1.0f, this.loadingFadeAlpha + deltaTime * 3.0f);
                }
                this.renderLoadingScreen();
            } else {
                this.renderUI();
            }
            ImGui.render();
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glClear(16384);
            this.imGuiGl3.renderDrawData(ImGui.getDrawData());
            GLFW.glfwSwapBuffers(this.windowPtr);
        }
    }

    private boolean checkCursorOverImGui() {
        ImGuiIO io = ImGui.getIO();
        if (io.getWantCaptureMouse()) {
            return true;
        }
        return ImGui.isWindowHovered(36);
    }

    private void processKeyboardInput(ImGuiIO io) {
        Character c;
        KeyEvent keyEvent;
        io.addKeyEvent(4096, this.ctrlDown);
        io.addKeyEvent(8192, this.shiftDown);
        io.addKeyEvent(16384, this.altDown);
        while ((keyEvent = this.keyEventQueue.poll()) != null) {
            io.addKeyEvent(keyEvent.imGuiKey, keyEvent.down);
        }
        while ((c = this.charInputQueue.poll()) != null) {
            io.addInputCharacter(c.charValue());
        }
    }

    private void updateWindowPosition() {
        boolean overlayIsActive;
        if (this.loadingMode) {
            this.updateLoadingWindowPosition();
            return;
        }
        this.targetHwnd = User32.INSTANCE.FindWindow(null, targetWindowTitle);
        if (this.targetHwnd == null) {
            if (this.overlayVisible) {
                this.saveIniIfAppropriate();
                User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 147);
                this.overlayVisible = false;
            }
            return;
        }
        if (User32Ext.INSTANCE.IsIconic(this.targetHwnd)) {
            if (this.overlayVisible) {
                this.saveIniIfAppropriate();
                User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 147);
                this.overlayVisible = false;
            }
            return;
        }
        WinDef.HWND foregroundHwnd = User32.INSTANCE.GetForegroundWindow();
        boolean mtaIsActive = foregroundHwnd != null && foregroundHwnd.equals(this.targetHwnd);
        boolean bl = overlayIsActive = foregroundHwnd != null && this.overlayHwnd != null && foregroundHwnd.equals(this.overlayHwnd);
        if (this.userHidden && !this.lifebotUI.shouldRender()) {
            if (this.overlayVisible) {
                this.saveIniIfAppropriate();
                User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 147);
                this.overlayVisible = false;
            }
            return;
        }
        if (!mtaIsActive && !overlayIsActive) {
            if (this.overlayVisible) {
                this.saveIniIfAppropriate();
                User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 147);
                this.overlayVisible = false;
            }
            return;
        }
        if (!this.overlayVisible) {
            User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 83);
            this.overlayVisible = true;
            this.lastVisibleTime = System.currentTimeMillis();
        }
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(this.targetHwnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        int x = rect.left;
        int y = rect.top;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (x != this.lastX || y != this.lastY || width != this.lastW || height != this.lastH) {
            GLFW.glfwSetWindowPos(this.windowPtr, x, y);
            GLFW.glfwSetWindowSize(this.windowPtr, width, height);
            this.lastX = x;
            this.lastY = y;
            this.lastW = width;
            this.lastH = height;
        }
        User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 19);
    }

    private void saveIniIfAppropriate() {
        long now = System.currentTimeMillis();
        long visibleDuration = now - this.lastVisibleTime;
        long timeSinceLastSave = now - this.lastSaveTime;
        if (visibleDuration >= 500L && timeSinceLastSave >= 2000L) {
            this.saveEncryptedIni();
            this.lastSaveTime = now;
            System.out.println("Saved layout (visible: " + visibleDuration + "ms, since last save: " + timeSinceLastSave + "ms)");
        }
    }

    private void loadEncryptedIni() {
        if (iniFilePath == null) {
            return;
        }
        File iniFile = new File(iniFilePath);
        if (!iniFile.exists()) {
            File oldFile = new File(iniFile.getParentFile(), "imgui_overlay.ini");
            if (oldFile.exists()) {
                try {
                    String content = new String(Files.readAllBytes(oldFile.toPath()), StandardCharsets.UTF_8);
                    ImGui.loadIniSettingsFromMemory(content);
                    System.out.println("Migrated old ini file to memory");
                }
                catch (Exception e) {
                    System.out.println("Failed to migrate old ini: " + e.getMessage());
                }
            }
            return;
        }
        try {
            String content = ObfuscatedStorage.readEncrypted(iniFile);
            if (content != null && !content.isEmpty()) {
                ImGui.loadIniSettingsFromMemory(content);
                System.out.println("Loaded encrypted layout settings");
            }
        }
        catch (Exception e) {
            System.out.println("Failed to load layout: " + e.getMessage());
        }
    }

    private void saveEncryptedIni() {
        if (iniFilePath == null) {
            return;
        }
        try {
            String iniContent = ImGui.saveIniSettingsToMemory();
            if (iniContent != null && !iniContent.isEmpty()) {
                File iniFile = new File(iniFilePath);
                ObfuscatedStorage.writeEncrypted(iniFile, iniContent);
            }
        }
        catch (Exception e) {
            System.out.println("Failed to save layout: " + e.getMessage());
        }
    }

    public void setLoadingMode(boolean enabled) {
        this.loadingMode = enabled;
        if (enabled) {
            this.loadingFadeAlpha = 0.0f;
            this.loadingAnimTime = 0.0f;
        }
    }

    public static void setIniFilePath(String path) {
        iniFilePath = path;
        System.out.println("ImGui ini path set to: " + path);
    }

    public void setLoadingStatus(String status) {
        this.loadingStatus = status;
    }

    public boolean isLoadingMode() {
        return this.loadingMode;
    }

    private void updateLoadingWindowPosition() {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) {
            return;
        }
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(monitor);
        if (vidmode == null) {
            return;
        }
        int screenW = vidmode.width();
        int screenH = vidmode.height();
        int x = (screenW - 420) / 2;
        int y = (screenH - 180) / 2;
        if (this.lastW != 420 || this.lastH != 180 || this.lastX != x || this.lastY != y) {
            GLFW.glfwSetWindowSize(this.windowPtr, 420, 180);
            GLFW.glfwSetWindowPos(this.windowPtr, x, y);
            this.lastW = 420;
            this.lastH = 180;
            this.lastX = x;
            this.lastY = y;
        }
        if (!this.overlayVisible) {
            User32.INSTANCE.SetWindowPos(this.overlayHwnd, HWND_TOPMOST, 0, 0, 0, 0, 83);
            this.overlayVisible = true;
        }
    }

    private void renderLoadingScreen() {
        boolean glitchActive;
        ImGui.setNextWindowPos(0.0f, 0.0f, 1);
        ImGui.setNextWindowSize(420.0f, 180.0f, 1);
        int windowFlags = 431;
        ImGui.begin("##LoadingScreen", windowFlags);
        ImDrawList drawList = ImGui.getWindowDrawList();
        float windowX = 0.0f;
        float windowY = 0.0f;
        float alpha = this.loadingFadeAlpha;
        int bgDarkest = ImGuiOverlay.rgbaToIntAlpha(20, 22, 25, (int)(250.0f * alpha));
        int bgDark = ImGuiOverlay.rgbaToIntAlpha(28, 31, 35, (int)(255.0f * alpha));
        int accent = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(255.0f * alpha));
        int accentBright = ImGuiOverlay.rgbaToIntAlpha(89, 165, 240, (int)(255.0f * alpha));
        int accentGlow = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(80.0f * alpha));
        int textDim = ImGuiOverlay.rgbaToIntAlpha(158, 166, 188, (int)(255.0f * alpha));
        int border = ImGuiOverlay.rgbaToIntAlpha(56, 62, 72, (int)(255.0f * alpha));
        int cyan = ImGuiOverlay.rgbaToIntAlpha(0, 191, 217, (int)(255.0f * alpha));
        int purple = ImGuiOverlay.rgbaToIntAlpha(147, 112, 219, (int)(255.0f * alpha));
        float rounding = 12.0f;
        float centerX = windowX + 210.0f;
        for (int i = 3; i >= 0; --i) {
            float offset = (float)i * 2.0f;
            drawList.addRectFilled(windowX - offset, windowY - offset, windowX + 420.0f + offset, windowY + 180.0f + offset, ImGuiOverlay.rgbaToIntAlpha(0, 0, 0, (int)((float)(15 + i * 10) * alpha)), rounding + offset);
        }
        drawList.addRectFilled(windowX, windowY, windowX + 420.0f, windowY + 180.0f, bgDarkest, rounding);
        float glowIntensity = 0.4f + 0.3f * (float)Math.sin(this.loadingPulsePhase * 0.7f);
        int borderGlow = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(80.0f * alpha * glowIntensity));
        drawList.addRect(windowX, windowY, windowX + 420.0f, windowY + 180.0f, borderGlow, rounding, 0, 2.0f);
        drawList.addRect(windowX + 1.0f, windowY + 1.0f, windowX + 420.0f - 1.0f, windowY + 180.0f - 1.0f, border, rounding - 1.0f, 0, 1.0f);
        float padding = 16.0f;
        drawList.addRectFilled(windowX + padding, windowY + padding, windowX + 420.0f - padding, windowY + 180.0f - padding, bgDark, rounding - 4.0f);
        float logoY = windowY + 50.0f;
        String title = "LIFEBOT";
        ImVec2 titleSize = new ImVec2();
        ImGui.calcTextSize(titleSize, title);
        float titleX = centerX - titleSize.x / 2.0f;
        float glitchTime = this.loadingAnimTime * 8.0f;
        boolean bl = glitchActive = (int)glitchTime % 20 < 2;
        if (glitchActive) {
            float glitchOffset = (float)(Math.sin(glitchTime * 50.0f) * 3.0);
            int redGlitch = ImGuiOverlay.rgbaToIntAlpha(255, 50, 50, (int)(150.0f * alpha));
            drawList.addText(titleX + glitchOffset, logoY - titleSize.y / 2.0f - 1.0f, redGlitch, title);
            int cyanGlitch = ImGuiOverlay.rgbaToIntAlpha(50, 255, 255, (int)(150.0f * alpha));
            drawList.addText(titleX - glitchOffset, logoY - titleSize.y / 2.0f + 1.0f, cyanGlitch, title);
        }
        float glowPulse = 0.5f + 0.5f * (float)Math.sin(this.loadingPulsePhase * 0.7f);
        for (int i = 3; i >= 1; --i) {
            int layerGlow = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(80.0f * alpha * glowPulse * (0.3f / (float)i)));
            drawList.addText(titleX - (float)i, logoY - titleSize.y / 2.0f, layerGlow, title);
            drawList.addText(titleX + (float)i, logoY - titleSize.y / 2.0f, layerGlow, title);
        }
        drawList.addText(titleX, logoY - titleSize.y / 2.0f, accentBright, title);
        String status = this.loadingStatus;
        float statusY = windowY + 85.0f;
        ImVec2 statusSize = new ImVec2();
        ImGui.calcTextSize(statusSize, status);
        drawList.addText(centerX - statusSize.x / 2.0f, statusY - statusSize.y / 2.0f, textDim, status);
        float loadingY = windowY + 125.0f;
        int numBars = 7;
        float barWidth = 8.0f;
        float barSpacing = 6.0f;
        float maxBarHeight = 30.0f;
        float totalWidth = (float)numBars * barWidth + (float)(numBars - 1) * barSpacing;
        float startX = centerX - totalWidth / 2.0f;
        for (int i = 0; i < numBars; ++i) {
            float phase = this.loadingAnimTime * 4.0f - (float)i * 0.4f;
            float heightFactor = 0.3f + 0.7f * (float)(Math.sin(phase) * 0.5 + 0.5);
            float barHeight = maxBarHeight * heightFactor;
            float barX = startX + (float)i * (barWidth + barSpacing);
            float barY = loadingY - barHeight / 2.0f;
            float colorPhase = (float)i / (float)(numBars - 1);
            int r = (int)(64.0f + 83.0f * colorPhase);
            int g = (int)(144.0f + -32.0f * colorPhase);
            int b = (int)(222.0f + -3.0f * colorPhase);
            int barColor = ImGuiOverlay.rgbaToIntAlpha(r, g, b, (int)(255.0f * alpha * (0.7f + 0.3f * heightFactor)));
            int barGlow = ImGuiOverlay.rgbaToIntAlpha(r, g, b, (int)(40.0f * alpha));
            drawList.addRectFilled(barX - 2.0f, barY - 2.0f, barX + barWidth + 2.0f, barY + barHeight + 2.0f, barGlow, 4.0f);
            drawList.addRectFilled(barX, barY, barX + barWidth, barY + barHeight, barColor, 3.0f);
            int highlight = ImGuiOverlay.rgbaToIntAlpha(255, 255, 255, (int)(50.0f * alpha * heightFactor));
            drawList.addRectFilled(barX + 1.0f, barY, barX + barWidth - 1.0f, barY + 3.0f, highlight, 2.0f);
        }
        int numParticles = 12;
        for (int i = 0; i < numParticles; ++i) {
            float particlePhase = (this.loadingAnimTime * 0.3f + (float)i * 0.37f) % 1.0f;
            float particleX = windowX + padding + 20.0f + (float)(i * 31) % (420.0f - padding * 2.0f - 40.0f);
            float particleY = windowY + 180.0f - padding - particlePhase * (180.0f - padding * 2.0f);
            float particleAlpha = (float)Math.sin((double)particlePhase * Math.PI) * 0.6f;
            float particleSize = 1.5f + (float)Math.sin((float)i * 1.7f) * 1.0f;
            int particleColor = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(255.0f * alpha * particleAlpha));
            drawList.addCircleFilled(particleX, particleY, particleSize, particleColor);
        }
        float cornerSize = 20.0f + 5.0f * (float)Math.sin(this.loadingPulsePhase);
        int cornerColor = ImGuiOverlay.rgbaToIntAlpha(64, 144, 222, (int)(60.0f * alpha));
        drawList.addLine(windowX + padding, windowY + padding, windowX + padding + cornerSize, windowY + padding, cornerColor, 2.0f);
        drawList.addLine(windowX + padding, windowY + padding, windowX + padding, windowY + padding + cornerSize, cornerColor, 2.0f);
        drawList.addLine(windowX + 420.0f - padding - cornerSize, windowY + padding, windowX + 420.0f - padding, windowY + padding, cornerColor, 2.0f);
        drawList.addLine(windowX + 420.0f - padding, windowY + padding, windowX + 420.0f - padding, windowY + padding + cornerSize, cornerColor, 2.0f);
        drawList.addLine(windowX + padding, windowY + 180.0f - padding, windowX + padding + cornerSize, windowY + 180.0f - padding, cornerColor, 2.0f);
        drawList.addLine(windowX + padding, windowY + 180.0f - padding - cornerSize, windowX + padding, windowY + 180.0f - padding, cornerColor, 2.0f);
        drawList.addLine(windowX + 420.0f - padding - cornerSize, windowY + 180.0f - padding, windowX + 420.0f - padding, windowY + 180.0f - padding, cornerColor, 2.0f);
        drawList.addLine(windowX + 420.0f - padding, windowY + 180.0f - padding - cornerSize, windowX + 420.0f - padding, windowY + 180.0f - padding, cornerColor, 2.0f);
        ImGui.end();
    }

    private static int rgbaToIntAlpha(int r, int g, int b, int a) {
        a = Math.max(0, Math.min(255, a));
        return a << 24 | b << 16 | g << 8 | r;
    }

    private void renderUI() {
        this.lifebotUI.renderMainWindow();
        float deltaTime = ImGui.getIO().getDeltaTime();
        MiniGameWindow miniGameWindow = this.lifebotUI.getMiniGameWindow();
        if (miniGameWindow != null && miniGameWindow.isOpen()) {
            miniGameWindow.render(deltaTime);
        }
        ImGuiNotifications.getInstance().setGlobalOpacity(this.lifebotUI.getWindowOpacity());
        ImGuiNotifications.getInstance().render();
        if (this.showDebugWindow) {
            this.renderDebugWindow();
        }
        if (this.showDemoWindow) {
            ImGui.showDemoWindow();
        }
    }

    private void renderDebugWindow() {
        ImGui.setNextWindowPos(400.0f, 50.0f, 4);
        ImGui.setNextWindowSize(350.0f, 300.0f, 4);
        if (ImGui.begin("Debug Window (F3)")) {
            ImGui.text("Target: MTA: San Andreas");
            if (this.targetHwnd != null) {
                ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, "Target Window Detected!");
            } else {
                ImGui.textColored(1.0f, 0.5f, 0.0f, 1.0f, "Target NOT Found");
            }
            ImGui.separator();
            if (ImGui.button("Click Me Test")) {
                ++this.clickCount;
            }
            ImGui.sameLine();
            ImGui.text("Clicks: " + this.clickCount);
            ImGui.text("Cursor Over ImGui: " + this.isCursorOverImGui);
            ImGui.separator();
            ImGui.text("Keyboard Input Test:");
            ImGui.inputText("##textInput", this.textInputBuffer);
            ImGui.text("WantTextInput: " + ImGui.getIO().getWantTextInput());
            ImGui.separator();
            ImGuiIO io = ImGui.getIO();
            ImGui.text("Mouse Pos: " + String.format("%.0f, %.0f", Float.valueOf(io.getMousePosX()), Float.valueOf(io.getMousePosY())));
            ImGui.text("Mouse Down[0]: " + io.getMouseDown(0));
            ImGui.text("FPS: " + String.format("%.1f", Float.valueOf(io.getFramerate())));
            ImGui.separator();
            if (ImGui.button("Toggle Demo Window")) {
                this.showDemoWindow = !this.showDemoWindow;
            }
            ImGui.sameLine();
            if (ImGui.button("Close Overlay")) {
                GLFW.glfwSetWindowShouldClose(this.windowPtr, true);
            }
            ImGui.separator();
            if (ImGui.checkbox("Anti-Screen Capture", this.antiCaptureEnabled)) {
                this.antiCaptureEnabled = !this.antiCaptureEnabled;
                this.setAntiCapture(this.antiCaptureEnabled);
            }
        }
        ImGui.end();
    }

    private void destroy() {
        if (this.keyboardHook != null) {
            User32.INSTANCE.UnhookWindowsHookEx(this.keyboardHook);
            this.keyboardHook = null;
        }
        if (this.keyboardHookThread != null) {
            this.keyboardHookThread.interrupt();
        }
        this.saveEncryptedIni();
        this.imGuiGl3.shutdown();
        this.imGuiGlfw.shutdown();
        ImGui.destroyContext();
        this.glfwFreeCallbacks(this.windowPtr);
        GLFW.glfwDestroyWindow(this.windowPtr);
        GLFW.glfwTerminate();
        Objects.requireNonNull(GLFW.glfwSetErrorCallback(null)).free();
    }

    private void glfwFreeCallbacks(long window) {
        Callbacks.glfwFreeCallbacks(window);
    }

    public LifebotImGuiUI getUI() {
        return this.lifebotUI;
    }

    private int loadTextureFromResources(String resourcePath) {
        try {
            InputStream is = this.getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Could not find resource: " + resourcePath);
                return 0;
            }
            BufferedImage image = ImageIO.read(is);
            is.close();
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
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
            GL11.glTexParameteri(3553, 10241, 9987);
            GL11.glTexParameteri(3553, 10240, 9729);
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
            GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, buffer);
            GL30.glGenerateMipmap(3553);
            GL11.glBindTexture(3553, 0);
            System.out.println("Loaded logo texture: " + resourcePath + " (" + width + "x" + height + ") ID=" + textureId);
            return textureId;
        }
        catch (Exception e) {
            System.err.println("Failed to load texture: " + resourcePath);
            e.printStackTrace();
            return 0;
        }
    }

    private byte[] loadFontFromResources(String resourcePath) {
        try {
            InputStream is = this.getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Could not find font resource: " + resourcePath);
                return null;
            }
            byte[] data = is.readAllBytes();
            is.close();
            System.out.println("Loaded font from resources: " + resourcePath + " (" + data.length + " bytes)");
            return data;
        }
        catch (Exception e) {
            System.err.println("Failed to load font: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    public void playSound(String soundId, int volume) {
        if (soundId == null || soundId.isEmpty() || soundId.equals("none")) {
            return;
        }
        new Thread(() -> {
            try {
                if (soundId.startsWith("builtin:")) {
                    String soundName = soundId.substring("builtin:".length());
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        SoundUtils.playSound((Sound)sound, (int)volume);
                    }
                    catch (IllegalArgumentException e) {
                        System.err.println("Unknown built-in sound: " + soundName);
                    }
                } else if (soundId.startsWith("custom:")) {
                    String fileName = soundId.substring("custom:".length());
                    this.playCustomSound(fileName, volume);
                }
            }
            catch (Exception e) {
                System.err.println("Error playing sound: " + soundId);
                e.printStackTrace();
            }
        }).start();
    }

    private void playCustomSound(String fileName, int volume) {
        File soundFile = new File(CUSTOM_SOUNDS_DIR, fileName);
        if (!soundFile.exists()) {
            System.err.println("Custom sound file not found: " + soundFile.getAbsolutePath());
            return;
        }
        try (FileInputStream fis = new FileInputStream(soundFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);){
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            double newVolume = Math.max(0.0, Math.min(1.0, (double)volume / 100.0));
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float)(Math.log(newVolume) / Math.log(10.0) * 20.0);
                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                gainControl.setValue(dB);
            }
            this.customPlayingClips.add(clip);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    this.customPlayingClips.remove(clip);
                    clip.close();
                }
            });
            clip.start();
        }
        catch (Exception e) {
            System.err.println("Error playing custom sound: " + fileName);
            e.printStackTrace();
        }
    }

    public void stopAllSounds() {
        SoundUtils.stopAllSounds();
        for (Clip clip : this.customPlayingClips) {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
            catch (Exception exception) {}
        }
        this.customPlayingClips.clear();
    }

    public boolean isSoundPlaying() {
        if (SoundUtils.isPlaying()) {
            return true;
        }
        this.customPlayingClips.removeIf(clip -> !clip.isOpen());
        return !this.customPlayingClips.isEmpty();
    }

    public void openSoundsFolder() {
        try {
            if (!CUSTOM_SOUNDS_DIR.exists()) {
                CUSTOM_SOUNDS_DIR.mkdirs();
            }
            Desktop.getDesktop().open(CUSTOM_SOUNDS_DIR);
        }
        catch (Exception e) {
            System.err.println("Failed to open sounds folder");
            e.printStackTrace();
        }
    }

    private void loadSoundNamesConfig() {
        if (SOUND_NAMES_FILE.exists()) {
            try (FileInputStream fis = new FileInputStream(SOUND_NAMES_FILE);){
                this.soundNamesConfig.load(fis);
            }
            catch (Exception e) {
                System.err.println("Failed to load sound names config");
                e.printStackTrace();
            }
        }
    }

    private void saveSoundNamesConfig() {
        try {
            if (!CUSTOM_SOUNDS_DIR.exists()) {
                CUSTOM_SOUNDS_DIR.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(SOUND_NAMES_FILE);){
                this.soundNamesConfig.store(fos, "Custom sound display names");
            }
        }
        catch (Exception e) {
            System.err.println("Failed to save sound names config");
            e.printStackTrace();
        }
    }

    public void renameSound(String fileName, String newDisplayName) {
        if (newDisplayName != null && !newDisplayName.trim().isEmpty()) {
            this.soundNamesConfig.setProperty(fileName, newDisplayName.trim());
        } else {
            this.soundNamesConfig.remove(fileName);
        }
        this.saveSoundNamesConfig();
        this.updateCustomSoundsList();
    }

    public void updateCustomSoundsList() {
        File[] files;
        this.loadSoundNamesConfig();
        ArrayList<LifebotImGuiUI.CustomSoundInfo> sounds = new ArrayList<LifebotImGuiUI.CustomSoundInfo>();
        if (CUSTOM_SOUNDS_DIR.exists() && (files = CUSTOM_SOUNDS_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"))) != null) {
            for (File file : files) {
                String fileName = file.getName();
                String displayName = this.soundNamesConfig.getProperty(fileName, "");
                sounds.add(new LifebotImGuiUI.CustomSoundInfo(fileName, displayName));
            }
        }
        sounds.sort((a, b) -> {
            boolean bHasName;
            boolean aHasName = a.displayName != null && !a.displayName.isEmpty();
            boolean bl = bHasName = b.displayName != null && !b.displayName.isEmpty();
            if (aHasName != bHasName) {
                return aHasName ? 1 : -1;
            }
            String aSort = aHasName ? a.displayName : a.fileName;
            String bSort = bHasName ? b.displayName : b.fileName;
            return aSort.compareToIgnoreCase(bSort);
        });
        this.lifebotUI.setCustomSounds(sounds);
    }

    public static void main(String[] args) {
        new ImGuiOverlay().run();
    }

    public static interface HotkeyListener {
        public void onToggleBot();

        public void onToggleOverlay();
    }

    public static interface User32Ext
    extends StdCallLibrary {
        public static final User32Ext INSTANCE = Native.load("user32", User32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

        public int ToUnicodeEx(int var1, int var2, byte[] var3, char[] var4, int var5, int var6, WinDef.HKL var7);

        public WinDef.HKL GetKeyboardLayout(int var1);

        public short GetKeyState(int var1);

        public boolean SetWindowDisplayAffinity(WinDef.HWND var1, int var2);

        public boolean IsIconic(WinDef.HWND var1);
    }
}

