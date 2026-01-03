/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.glfw.ImGuiImplGlfw$MapAnalog
 *  imgui.glfw.ImGuiImplGlfw$MapButton
 *  imgui.glfw.ImGuiImplGlfw$ViewportData
 *  imgui.lwjgl3.glfw.ImGuiImplGlfwNative
 *  org.lwjgl.glfw.GLFWGamepadState
 *  org.lwjgl.glfw.GLFWNativeCocoa
 */
package imgui.glfw;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiPlatformIO;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.callback.ImPlatformFuncViewport;
import imgui.callback.ImPlatformFuncViewportFloat;
import imgui.callback.ImPlatformFuncViewportImVec2;
import imgui.callback.ImPlatformFuncViewportString;
import imgui.callback.ImPlatformFuncViewportSuppBoolean;
import imgui.callback.ImPlatformFuncViewportSuppImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.glfw.ImGuiImplGlfw;
import imgui.lwjgl3.glfw.ImGuiImplGlfwNative;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

public class ImGuiImplGlfw {
    protected static final String OS = System.getProperty("os.name", "generic").toLowerCase();
    protected static final boolean IS_WINDOWS = OS.contains("win");
    protected static final boolean IS_APPLE = OS.contains("mac") || OS.contains("darwin");
    protected Data data = null;
    private final Properties props = new Properties();
    protected static final int glfwVersionCombined = 3400;
    protected static final boolean glfwHawWindowTopmost = true;
    protected static final boolean glfwHasWindowHovered = true;
    protected static final boolean glfwHasWindowAlpha = true;
    protected static final boolean glfwHasPerMonitorDpi = true;
    protected static final boolean glfwHasFocusWindow = true;
    protected static final boolean glfwHasFocusOnShow = true;
    protected static final boolean glfwHasMonitorWorkArea = true;
    protected static final boolean glfwHasOsxWindowPosFix = true;
    protected static final boolean glfwHasNewCursors = true;
    protected static final boolean glfwHasMousePassthrough = true;
    protected static final boolean glfwHasGamepadApi = true;
    protected static final boolean glfwHasGetKeyName = true;
    protected static final boolean glfwHasGetError = true;

    protected ImStrSupplier getClipboardTextFn() {
        return new ImStrSupplier(){

            @Override
            public String get() {
                String clipboardString = GLFW.glfwGetClipboardString(ImGuiImplGlfw.this.data.window);
                return clipboardString != null ? clipboardString : "";
            }
        };
    }

    protected ImStrConsumer setClipboardTextFn() {
        return new ImStrConsumer(){

            @Override
            public void accept(String text) {
                GLFW.glfwSetClipboardString(ImGuiImplGlfw.this.data.window, text);
            }
        };
    }

    protected int glfwKeyToImGuiKey(int glfwKey) {
        switch (glfwKey) {
            case 258: {
                return 512;
            }
            case 263: {
                return 513;
            }
            case 262: {
                return 514;
            }
            case 265: {
                return 515;
            }
            case 264: {
                return 516;
            }
            case 266: {
                return 517;
            }
            case 267: {
                return 518;
            }
            case 268: {
                return 519;
            }
            case 269: {
                return 520;
            }
            case 260: {
                return 521;
            }
            case 261: {
                return 522;
            }
            case 259: {
                return 523;
            }
            case 32: {
                return 524;
            }
            case 257: {
                return 525;
            }
            case 256: {
                return 526;
            }
            case 39: {
                return 584;
            }
            case 44: {
                return 585;
            }
            case 45: {
                return 586;
            }
            case 46: {
                return 587;
            }
            case 47: {
                return 588;
            }
            case 59: {
                return 589;
            }
            case 61: {
                return 590;
            }
            case 91: {
                return 591;
            }
            case 92: {
                return 592;
            }
            case 93: {
                return 593;
            }
            case 96: {
                return 594;
            }
            case 280: {
                return 595;
            }
            case 281: {
                return 596;
            }
            case 282: {
                return 597;
            }
            case 283: {
                return 598;
            }
            case 284: {
                return 599;
            }
            case 320: {
                return 600;
            }
            case 321: {
                return 601;
            }
            case 322: {
                return 602;
            }
            case 323: {
                return 603;
            }
            case 324: {
                return 604;
            }
            case 325: {
                return 605;
            }
            case 326: {
                return 606;
            }
            case 327: {
                return 607;
            }
            case 328: {
                return 608;
            }
            case 329: {
                return 609;
            }
            case 330: {
                return 610;
            }
            case 331: {
                return 611;
            }
            case 332: {
                return 612;
            }
            case 333: {
                return 613;
            }
            case 334: {
                return 614;
            }
            case 335: {
                return 615;
            }
            case 336: {
                return 616;
            }
            case 340: {
                return 528;
            }
            case 341: {
                return 527;
            }
            case 342: {
                return 529;
            }
            case 343: {
                return 530;
            }
            case 344: {
                return 532;
            }
            case 345: {
                return 531;
            }
            case 346: {
                return 533;
            }
            case 347: {
                return 534;
            }
            case 348: {
                return 535;
            }
            case 48: {
                return 536;
            }
            case 49: {
                return 537;
            }
            case 50: {
                return 538;
            }
            case 51: {
                return 539;
            }
            case 52: {
                return 540;
            }
            case 53: {
                return 541;
            }
            case 54: {
                return 542;
            }
            case 55: {
                return 543;
            }
            case 56: {
                return 544;
            }
            case 57: {
                return 545;
            }
            case 65: {
                return 546;
            }
            case 66: {
                return 547;
            }
            case 67: {
                return 548;
            }
            case 68: {
                return 549;
            }
            case 69: {
                return 550;
            }
            case 70: {
                return 551;
            }
            case 71: {
                return 552;
            }
            case 72: {
                return 553;
            }
            case 73: {
                return 554;
            }
            case 74: {
                return 555;
            }
            case 75: {
                return 556;
            }
            case 76: {
                return 557;
            }
            case 77: {
                return 558;
            }
            case 78: {
                return 559;
            }
            case 79: {
                return 560;
            }
            case 80: {
                return 561;
            }
            case 81: {
                return 562;
            }
            case 82: {
                return 563;
            }
            case 83: {
                return 564;
            }
            case 84: {
                return 565;
            }
            case 85: {
                return 566;
            }
            case 86: {
                return 567;
            }
            case 87: {
                return 568;
            }
            case 88: {
                return 569;
            }
            case 89: {
                return 570;
            }
            case 90: {
                return 571;
            }
            case 290: {
                return 572;
            }
            case 291: {
                return 573;
            }
            case 292: {
                return 574;
            }
            case 293: {
                return 575;
            }
            case 294: {
                return 576;
            }
            case 295: {
                return 577;
            }
            case 296: {
                return 578;
            }
            case 297: {
                return 579;
            }
            case 298: {
                return 580;
            }
            case 299: {
                return 581;
            }
            case 300: {
                return 582;
            }
            case 301: {
                return 583;
            }
        }
        return 0;
    }

    protected void updateKeyModifiers(long window) {
        ImGuiIO io = ImGui.getIO();
        io.addKeyEvent(4096, GLFW.glfwGetKey(window, 341) == 1 || GLFW.glfwGetKey(window, 345) == 1);
        io.addKeyEvent(8192, GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1);
        io.addKeyEvent(16384, GLFW.glfwGetKey(window, 342) == 1 || GLFW.glfwGetKey(window, 346) == 1);
        io.addKeyEvent(32768, GLFW.glfwGetKey(window, 343) == 1 || GLFW.glfwGetKey(window, 347) == 1);
    }

    protected boolean shouldChainCallback(long window) {
        return this.data.callbacksChainForAllWindows ? true : window == this.data.window;
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
        if (this.data.prevUserCallbackMousebutton != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackMousebutton.invoke(window, button, action, mods);
        }
        this.updateKeyModifiers(window);
        ImGuiIO io = ImGui.getIO();
        if (button >= 0 && button < 5) {
            io.addMouseButtonEvent(button, action == 1);
        }
    }

    public void scrollCallback(long window, double xOffset, double yOffset) {
        if (this.data.prevUserCallbackScroll != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackScroll.invoke(window, xOffset, yOffset);
        }
        ImGuiIO io = ImGui.getIO();
        io.addMouseWheelEvent((float)xOffset, (float)yOffset);
    }

    protected int translateUntranslatedKey(int key, int scancode) {
        if (key >= 320 && key <= 336) {
            return key;
        }
        int resultKey = key;
        String keyName = GLFW.glfwGetKeyName(key, scancode);
        this.eatErrors();
        if (keyName != null && keyName.length() > 2 && keyName.charAt(0) != '\u0000' && keyName.charAt(1) == '\u0000') {
            if (keyName.charAt(0) >= '0' && keyName.charAt(0) <= '9') {
                resultKey = 48 + (keyName.charAt(0) - 48);
            } else if (keyName.charAt(0) >= 'A' && keyName.charAt(0) <= 'Z') {
                resultKey = 65 + (keyName.charAt(0) - 65);
            } else if (keyName.charAt(0) >= 'a' && keyName.charAt(0) <= 'z') {
                resultKey = 65 + (keyName.charAt(0) - 97);
            } else {
                int index = "`-=[]\\,;'./".indexOf(keyName.charAt(0));
                if (index != -1) {
                    resultKey = this.props.charKeys[index];
                }
            }
        }
        return resultKey;
    }

    protected void eatErrors() {
        PointerBuffer pb = MemoryUtil.memAllocPointer(1);
        GLFW.glfwGetError(pb);
        MemoryUtil.memFree(pb);
    }

    public void keyCallback(long window, int keycode, int scancode, int action, int mods) {
        if (this.data.prevUserCallbackKey != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackKey.invoke(window, keycode, scancode, action, mods);
        }
        if (action != 1 && action != 0) {
            return;
        }
        this.updateKeyModifiers(window);
        if (keycode >= 0 && keycode < this.data.keyOwnerWindows.length) {
            this.data.keyOwnerWindows[keycode] = action == 1 ? window : -1L;
        }
        int key = this.translateUntranslatedKey(keycode, scancode);
        ImGuiIO io = ImGui.getIO();
        int imguiKey = this.glfwKeyToImGuiKey(key);
        io.addKeyEvent(imguiKey, action == 1);
        io.setKeyEventNativeData(imguiKey, key, scancode);
    }

    public void windowFocusCallback(long window, boolean focused) {
        if (this.data.prevUserCallbackWindowFocus != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackWindowFocus.invoke(window, focused);
        }
        ImGui.getIO().addFocusEvent(focused);
    }

    public void cursorPosCallback(long window, double x, double y) {
        if (this.data.prevUserCallbackCursorPos != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackCursorPos.invoke(window, x, y);
        }
        float posX = (float)x;
        float posY = (float)y;
        ImGuiIO io = ImGui.getIO();
        if (io.hasConfigFlags(1024)) {
            GLFW.glfwGetWindowPos(window, this.props.windowX, this.props.windowY);
            posX += (float)this.props.windowX[0];
            posY += (float)this.props.windowY[0];
        }
        io.addMousePosEvent(posX, posY);
        this.data.lastValidMousePos.set(posX, posY);
    }

    public void cursorEnterCallback(long window, boolean entered) {
        if (this.data.prevUserCallbackCursorEnter != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackCursorEnter.invoke(window, entered);
        }
        ImGuiIO io = ImGui.getIO();
        if (entered) {
            this.data.mouseWindow = window;
            io.addMousePosEvent(this.data.lastValidMousePos.x, this.data.lastValidMousePos.y);
        } else if (this.data.mouseWindow == window) {
            io.getMousePos(this.data.lastValidMousePos);
            this.data.mouseWindow = -1L;
            io.addMousePosEvent(Float.MIN_VALUE, Float.MIN_VALUE);
        }
    }

    public void charCallback(long window, int c) {
        if (this.data.prevUserCallbackChar != null && this.shouldChainCallback(window)) {
            this.data.prevUserCallbackChar.invoke(window, c);
        }
        ImGui.getIO().addInputCharacter(c);
    }

    public void monitorCallback(long window, int event) {
        this.data.wantUpdateMonitors = true;
    }

    public void installCallbacks(long window) {
        this.data.prevUserCallbackWindowFocus = GLFW.glfwSetWindowFocusCallback(window, this::windowFocusCallback);
        this.data.prevUserCallbackCursorEnter = GLFW.glfwSetCursorEnterCallback(window, this::cursorEnterCallback);
        this.data.prevUserCallbackCursorPos = GLFW.glfwSetCursorPosCallback(window, this::cursorPosCallback);
        this.data.prevUserCallbackMousebutton = GLFW.glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        this.data.prevUserCallbackScroll = GLFW.glfwSetScrollCallback(window, this::scrollCallback);
        this.data.prevUserCallbackKey = GLFW.glfwSetKeyCallback(window, this::keyCallback);
        this.data.prevUserCallbackChar = GLFW.glfwSetCharCallback(window, this::charCallback);
        this.data.prevUserCallbackMonitor = GLFW.glfwSetMonitorCallback(this::monitorCallback);
        this.data.installedCallbacks = true;
    }

    protected void freeCallback(Callback cb) {
        if (cb != null) {
            cb.free();
        }
    }

    public void restoreCallbacks(long window) {
        this.freeCallback(GLFW.glfwSetWindowFocusCallback(window, this.data.prevUserCallbackWindowFocus));
        this.freeCallback(GLFW.glfwSetCursorEnterCallback(window, this.data.prevUserCallbackCursorEnter));
        this.freeCallback(GLFW.glfwSetCursorPosCallback(window, this.data.prevUserCallbackCursorPos));
        this.freeCallback(GLFW.glfwSetMouseButtonCallback(window, this.data.prevUserCallbackMousebutton));
        this.freeCallback(GLFW.glfwSetScrollCallback(window, this.data.prevUserCallbackScroll));
        this.freeCallback(GLFW.glfwSetKeyCallback(window, this.data.prevUserCallbackKey));
        this.freeCallback(GLFW.glfwSetCharCallback(window, this.data.prevUserCallbackChar));
        this.freeCallback(GLFW.glfwSetMonitorCallback(this.data.prevUserCallbackMonitor));
        this.data.installedCallbacks = false;
        this.data.prevUserCallbackWindowFocus = null;
        this.data.prevUserCallbackCursorEnter = null;
        this.data.prevUserCallbackCursorPos = null;
        this.data.prevUserCallbackMousebutton = null;
        this.data.prevUserCallbackScroll = null;
        this.data.prevUserCallbackKey = null;
        this.data.prevUserCallbackChar = null;
        this.data.prevUserCallbackMonitor = null;
    }

    public void setCallbacksChainForAllWindows(boolean chainForAllWindows) {
        this.data.callbacksChainForAllWindows = chainForAllWindows;
    }

    protected Data newData() {
        return new Data();
    }

    public boolean init(long window, boolean installCallbacks) {
        ImGuiIO io = ImGui.getIO();
        io.setBackendPlatformName("imgui-java_impl_glfw");
        io.addBackendFlags(1030);
        io.addBackendFlags(2048);
        this.data = this.newData();
        this.data.window = window;
        this.data.time = 0.0;
        this.data.wantUpdateMonitors = true;
        io.setGetClipboardTextFn(this.getClipboardTextFn());
        io.setSetClipboardTextFn(this.setClipboardTextFn());
        GLFWErrorCallback prevErrorCallback = GLFW.glfwSetErrorCallback(null);
        this.data.mouseCursors[0] = GLFW.glfwCreateStandardCursor(221185);
        this.data.mouseCursors[1] = GLFW.glfwCreateStandardCursor(221186);
        this.data.mouseCursors[3] = GLFW.glfwCreateStandardCursor(221190);
        this.data.mouseCursors[4] = GLFW.glfwCreateStandardCursor(221189);
        this.data.mouseCursors[7] = GLFW.glfwCreateStandardCursor(221188);
        this.data.mouseCursors[2] = GLFW.glfwCreateStandardCursor(221193);
        this.data.mouseCursors[5] = GLFW.glfwCreateStandardCursor(221192);
        this.data.mouseCursors[6] = GLFW.glfwCreateStandardCursor(221191);
        this.data.mouseCursors[8] = GLFW.glfwCreateStandardCursor(221194);
        GLFW.glfwSetErrorCallback(prevErrorCallback);
        this.eatErrors();
        if (installCallbacks) {
            this.installCallbacks(window);
        }
        this.updateMonitors();
        GLFW.glfwSetMonitorCallback(this::monitorCallback);
        ImGuiViewport mainViewport = ImGui.getMainViewport();
        mainViewport.setPlatformHandle(window);
        if (IS_WINDOWS) {
            mainViewport.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(window));
        }
        if (IS_APPLE) {
            mainViewport.setPlatformHandleRaw(GLFWNativeCocoa.glfwGetCocoaWindow((long)window));
        }
        if (io.hasConfigFlags(1024)) {
            this.initPlatformInterface();
        }
        return true;
    }

    public void shutdown() {
        ImGuiIO io = ImGui.getIO();
        this.shutdownPlatformInterface();
        if (this.data.installedCallbacks) {
            this.restoreCallbacks(this.data.window);
        }
        for (int cursorN = 0; cursorN < 9; ++cursorN) {
            GLFW.glfwDestroyCursor(this.data.mouseCursors[cursorN]);
        }
        io.setBackendPlatformName(null);
        this.data = null;
        io.removeBackendFlags(3079);
    }

    protected void updateMouseData() {
        ImGuiIO io = ImGui.getIO();
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        int mouseViewportId = 0;
        io.getMousePos(this.props.mousePosPrev);
        for (int n = 0; n < platformIO.getViewportsSize(); ++n) {
            boolean windowNoInput;
            boolean isWindowFocused;
            ImGuiViewport viewport = platformIO.getViewports(n);
            long window = viewport.getPlatformHandle();
            boolean bl = isWindowFocused = GLFW.glfwGetWindowAttrib(window, 131073) != 0;
            if (isWindowFocused) {
                if (io.getWantSetMousePos()) {
                    GLFW.glfwSetCursorPos(window, ((Properties)this.props).mousePosPrev.x - viewport.getPosX(), ((Properties)this.props).mousePosPrev.y - viewport.getPosY());
                }
                if (this.data.mouseWindow == -1L) {
                    GLFW.glfwGetCursorPos(window, this.props.mouseX, this.props.mouseY);
                    double mouseX = this.props.mouseX[0];
                    double mouseY = this.props.mouseY[0];
                    if (io.hasConfigFlags(1024)) {
                        GLFW.glfwGetWindowPos(window, this.props.windowX, this.props.windowY);
                        mouseX += (double)this.props.windowX[0];
                        mouseY += (double)this.props.windowY[0];
                    }
                    this.data.lastValidMousePos.set((float)mouseX, (float)mouseY);
                    io.addMousePosEvent((float)mouseX, (float)mouseY);
                }
            }
            GLFW.glfwSetWindowAttrib(window, 131085, (windowNoInput = viewport.hasFlags(128)) ? 1 : 0);
            if (GLFW.glfwGetWindowAttrib(window, 131083) != 1 || windowNoInput) continue;
            mouseViewportId = viewport.getID();
        }
        if (io.hasBackendFlags(2048)) {
            io.addMouseViewportEvent(mouseViewportId);
        }
    }

    protected void updateMouseCursor() {
        ImGuiIO io = ImGui.getIO();
        if (io.hasConfigFlags(32) || GLFW.glfwGetInputMode(this.data.window, 208897) == 212995) {
            return;
        }
        int imguiCursor = ImGui.getMouseCursor();
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        for (int n = 0; n < platformIO.getViewportsSize(); ++n) {
            long windowPtr = platformIO.getViewports(n).getPlatformHandle();
            if (imguiCursor == -1 || io.getMouseDrawCursor()) {
                GLFW.glfwSetInputMode(windowPtr, 208897, 212994);
                continue;
            }
            GLFW.glfwSetCursor(windowPtr, this.data.mouseCursors[imguiCursor] != 0L ? this.data.mouseCursors[imguiCursor] : this.data.mouseCursors[0]);
            GLFW.glfwSetInputMode(windowPtr, 208897, 212993);
        }
    }

    private float saturate(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    protected void updateGamepads() {
        MapAnalog mapAnalog;
        MapButton mapButton;
        ImGuiIO io = ImGui.getIO();
        if (!io.hasConfigFlags(2)) {
            return;
        }
        io.removeBackendFlags(1);
        try (GLFWGamepadState gamepad = GLFWGamepadState.create();){
            if (!GLFW.glfwGetGamepadState(0, gamepad)) {
                return;
            }
            mapButton = (keyNo, buttonNo, _unused) -> io.addKeyEvent(keyNo, gamepad.buttons(buttonNo) != 0);
            mapAnalog = (keyNo, axisNo, _unused, v0, v1) -> {
                float v = gamepad.axes(axisNo);
                io.addKeyAnalogEvent(keyNo, (v = (v - v0) / (v1 - v0)) > 0.1f, this.saturate(v));
            };
        }
        io.addBackendFlags(1);
        mapButton.run(617, 7, 7);
        mapButton.run(618, 6, 6);
        mapButton.run(619, 2, 2);
        mapButton.run(620, 1, 1);
        mapButton.run(621, 3, 3);
        mapButton.run(622, 0, 0);
        mapButton.run(623, 14, 13);
        mapButton.run(624, 12, 11);
        mapButton.run(625, 11, 10);
        mapButton.run(626, 13, 12);
        mapButton.run(627, 4, 4);
        mapButton.run(628, 5, 5);
        mapAnalog.run(629, 4, 4, -0.75f, 1.0f);
        mapAnalog.run(630, 5, 5, -0.75f, 1.0f);
        mapButton.run(631, 9, 8);
        mapButton.run(632, 10, 9);
        mapAnalog.run(633, 0, 0, -0.25f, -1.0f);
        mapAnalog.run(634, 0, 0, 0.25f, 1.0f);
        mapAnalog.run(635, 1, 1, -0.25f, -1.0f);
        mapAnalog.run(636, 1, 1, 0.25f, 1.0f);
        mapAnalog.run(637, 2, 2, -0.25f, -1.0f);
        mapAnalog.run(638, 2, 2, 0.25f, 1.0f);
        mapAnalog.run(639, 3, 3, -0.25f, -1.0f);
        mapAnalog.run(640, 3, 3, 0.25f, 1.0f);
    }

    protected void updateMonitors() {
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        this.data.wantUpdateMonitors = false;
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null) {
            System.err.println("Unable to get monitors!");
            return;
        }
        if (monitors.limit() == 0) {
            return;
        }
        platformIO.resizeMonitors(0);
        for (int n = 0; n < monitors.limit(); ++n) {
            long monitor = monitors.get(n);
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            if (vidMode == null) continue;
            GLFW.glfwGetMonitorPos(monitor, this.props.monitorX, this.props.monitorY);
            float mainPosX = this.props.monitorX[0];
            float mainPosY = this.props.monitorY[0];
            float mainSizeX = vidMode.width();
            float mainSizeY = vidMode.height();
            float workPosX = 0.0f;
            float workPosY = 0.0f;
            float workSizeX = 0.0f;
            float workSizeY = 0.0f;
            GLFW.glfwGetMonitorWorkarea(monitor, this.props.monitorWorkAreaX, this.props.monitorWorkAreaY, this.props.monitorWorkAreaWidth, this.props.monitorWorkAreaHeight);
            if (this.props.monitorWorkAreaWidth[0] > 0 && this.props.monitorWorkAreaHeight[0] > 0) {
                workPosX = this.props.monitorWorkAreaX[0];
                workPosY = this.props.monitorWorkAreaY[0];
                workSizeX = this.props.monitorWorkAreaWidth[0];
                workSizeY = this.props.monitorWorkAreaHeight[0];
            }
            float dpiScale = 0.0f;
            GLFW.glfwGetMonitorContentScale(monitor, this.props.monitorContentScaleX, this.props.monitorContentScaleY);
            dpiScale = this.props.monitorContentScaleX[0];
            platformIO.pushMonitors(monitor, mainPosX, mainPosY, mainSizeX, mainSizeY, workPosX, workPosY, workSizeX, workSizeY, dpiScale);
        }
    }

    public void newFrame() {
        double currentTime;
        ImGuiIO io = ImGui.getIO();
        GLFW.glfwGetWindowSize(this.data.window, this.props.windowW, this.props.windowH);
        GLFW.glfwGetFramebufferSize(this.data.window, this.props.displayW, this.props.displayH);
        io.setDisplaySize(this.props.windowW[0], this.props.windowH[0]);
        if (this.props.windowW[0] > 0 && this.props.windowH[0] > 0) {
            float scaleX = (float)this.props.displayW[0] / (float)this.props.windowW[0];
            float scaleY = (float)this.props.displayH[0] / (float)this.props.windowH[0];
            io.setDisplayFramebufferScale(scaleX, scaleY);
        }
        if (this.data.wantUpdateMonitors) {
            this.updateMonitors();
        }
        if ((currentTime = GLFW.glfwGetTime()) <= this.data.time) {
            currentTime = this.data.time + (double)1.0E-5f;
        }
        io.setDeltaTime(this.data.time > 0.0 ? (float)(currentTime - this.data.time) : 0.016666668f);
        this.data.time = currentTime;
        this.updateMouseData();
        this.updateMouseCursor();
        this.updateGamepads();
    }

    private void windowCloseCallback(long windowId) {
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        if (vp.isValidPtr()) {
            vp.setPlatformRequestClose(true);
        }
    }

    private void windowPosCallback(long windowId, int xPos, int yPos) {
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        if (vp.isNotValidPtr()) {
            return;
        }
        ViewportData vd = (ViewportData)vp.getPlatformUserData();
        if (vd != null) {
            boolean ignoreEvent;
            boolean bl = ignoreEvent = ImGui.getFrameCount() <= vd.ignoreWindowPosEventFrame + 1;
            if (ignoreEvent) {
                return;
            }
        }
        vp.setPlatformRequestMove(true);
    }

    private void windowSizeCallback(long windowId, int width, int height) {
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        if (vp.isNotValidPtr()) {
            return;
        }
        ViewportData vd = (ViewportData)vp.getPlatformUserData();
        if (vd != null) {
            boolean ignoreEvent;
            boolean bl = ignoreEvent = ImGui.getFrameCount() <= vd.ignoreWindowSizeEventFrame + 1;
            if (ignoreEvent) {
                return;
            }
        }
        vp.setPlatformRequestResize(true);
    }

    protected void initPlatformInterface() {
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        platformIO.setPlatformCreateWindow(new CreateWindowFunction());
        platformIO.setPlatformDestroyWindow(new DestroyWindowFunction());
        platformIO.setPlatformShowWindow(new ShowWindowFunction());
        platformIO.setPlatformGetWindowPos(new GetWindowPosFunction());
        platformIO.setPlatformSetWindowPos(new SetWindowPosFunction());
        platformIO.setPlatformGetWindowSize(new GetWindowSizeFunction());
        platformIO.setPlatformSetWindowSize(new SetWindowSizeFunction());
        platformIO.setPlatformSetWindowTitle(new SetWindowTitleFunction());
        platformIO.setPlatformSetWindowFocus(new SetWindowFocusFunction());
        platformIO.setPlatformGetWindowFocus(new GetWindowFocusFunction());
        platformIO.setPlatformGetWindowMinimized(new GetWindowMinimizedFunction());
        platformIO.setPlatformSetWindowAlpha(new SetWindowAlphaFunction());
        platformIO.setPlatformRenderWindow(new RenderWindowFunction());
        platformIO.setPlatformSwapBuffers(new SwapBuffersFunction());
        ImGuiViewport mainViewport = ImGui.getMainViewport();
        ViewportData vd = new ViewportData(null);
        vd.window = this.data.window;
        vd.windowOwned = false;
        mainViewport.setPlatformUserData(vd);
        mainViewport.setPlatformHandle(this.data.window);
    }

    protected void shutdownPlatformInterface() {
        ImGui.destroyPlatformWindows();
    }

    private /* synthetic */ void lambda$updateGamepads$3(FloatBuffer axes, ImGuiIO io, int keyNo, int axisNo, int _unused, float v0, float v1) {
        float v = axes.limit() > axisNo ? axes.get(axisNo) : v0;
        v = (v - v0) / (v1 - v0);
        io.addKeyAnalogEvent(keyNo, v > 0.1f, this.saturate(v));
    }

    private static /* synthetic */ void lambda$updateGamepads$2(ImGuiIO io, ByteBuffer buttons, int keyNo, int buttonNo, int _unused) {
        io.addKeyEvent(keyNo, buttons.limit() > buttonNo && buttons.get(buttonNo) == 1);
    }

    private static final class SwapBuffersFunction
    extends ImPlatformFuncViewport {
        private SwapBuffersFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                GLFW.glfwMakeContextCurrent(vd.window);
                GLFW.glfwSwapBuffers(vd.window);
            }
        }
    }

    private static final class RenderWindowFunction
    extends ImPlatformFuncViewport {
        private RenderWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                GLFW.glfwMakeContextCurrent(vd.window);
            }
        }
    }

    private final class SetWindowAlphaFunction
    extends ImPlatformFuncViewportFloat {
        private SetWindowAlphaFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, float value) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                GLFW.glfwSetWindowOpacity(vd.window, value);
            }
        }
    }

    private static final class GetWindowMinimizedFunction
    extends ImPlatformFuncViewportSuppBoolean {
        private GetWindowMinimizedFunction() {
        }

        @Override
        public boolean get(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                return GLFW.glfwGetWindowAttrib(vd.window, 131074) != 0;
            }
            return false;
        }
    }

    private static final class GetWindowFocusFunction
    extends ImPlatformFuncViewportSuppBoolean {
        private GetWindowFocusFunction() {
        }

        @Override
        public boolean get(ImGuiViewport vp) {
            ViewportData data = (ViewportData)vp.getPlatformUserData();
            return GLFW.glfwGetWindowAttrib(data.window, 131073) != 0;
        }
    }

    private final class SetWindowFocusFunction
    extends ImPlatformFuncViewport {
        private SetWindowFocusFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                GLFW.glfwFocusWindow(vd.window);
            }
        }
    }

    private static final class SetWindowTitleFunction
    extends ImPlatformFuncViewportString {
        private SetWindowTitleFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, String value) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null) {
                GLFW.glfwSetWindowTitle(vd.window, value);
            }
        }
    }

    private final class SetWindowSizeFunction
    extends ImPlatformFuncViewportImVec2 {
        private final int[] x = new int[1];
        private final int[] y = new int[1];
        private final int[] width = new int[1];
        private final int[] height = new int[1];

        private SetWindowSizeFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, ImVec2 value) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd == null) {
                return;
            }
            if (IS_APPLE) {
                // empty if block
            }
            vd.ignoreWindowSizeEventFrame = ImGui.getFrameCount();
            GLFW.glfwSetWindowSize(vd.window, (int)value.x, (int)value.y);
        }
    }

    private static final class GetWindowSizeFunction
    extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] width = new int[1];
        private final int[] height = new int[1];

        private GetWindowSizeFunction() {
        }

        @Override
        public void get(ImGuiViewport vp, ImVec2 dst) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd == null) {
                return;
            }
            this.width[0] = 0;
            this.height[0] = 0;
            GLFW.glfwGetWindowSize(vd.window, this.width, this.height);
            dst.x = this.width[0];
            dst.y = this.height[0];
        }
    }

    private static final class SetWindowPosFunction
    extends ImPlatformFuncViewportImVec2 {
        private SetWindowPosFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, ImVec2 value) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd == null) {
                return;
            }
            vd.ignoreWindowPosEventFrame = ImGui.getFrameCount();
            GLFW.glfwSetWindowPos(vd.window, (int)value.x, (int)value.y);
        }
    }

    private static final class GetWindowPosFunction
    extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] posX = new int[1];
        private final int[] posY = new int[1];

        private GetWindowPosFunction() {
        }

        @Override
        public void get(ImGuiViewport vp, ImVec2 dst) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd == null) {
                return;
            }
            this.posX[0] = 0;
            this.posY[0] = 0;
            GLFW.glfwGetWindowPos(vd.window, this.posX, this.posY);
            dst.set(this.posX[0], this.posY[0]);
        }
    }

    private static final class ShowWindowFunction
    extends ImPlatformFuncViewport {
        private ShowWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd == null) {
                return;
            }
            if (IS_WINDOWS && vp.hasFlags(16)) {
                ImGuiImplGlfwNative.win32hideFromTaskBar((long)vp.getPlatformHandleRaw());
            }
            GLFW.glfwShowWindow(vd.window);
        }
    }

    private final class DestroyWindowFunction
    extends ImPlatformFuncViewport {
        private DestroyWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = (ViewportData)vp.getPlatformUserData();
            if (vd != null && vd.windowOwned) {
                for (int i = 0; i < ImGuiImplGlfw.this.data.keyOwnerWindows.length; ++i) {
                    if (ImGuiImplGlfw.this.data.keyOwnerWindows[i] != vd.window) continue;
                    ImGuiImplGlfw.this.keyCallback(vd.window, i, 0, 0, 0);
                }
                Callbacks.glfwFreeCallbacks(vd.window);
                GLFW.glfwDestroyWindow(vd.window);
                vd.window = -1L;
            }
            vp.setPlatformHandle(-1L);
            vp.setPlatformUserData(null);
        }
    }

    private final class CreateWindowFunction
    extends ImPlatformFuncViewport {
        private CreateWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ViewportData vd = new ViewportData(null);
            vp.setPlatformUserData(vd);
            GLFW.glfwWindowHint(131076, 0);
            GLFW.glfwWindowHint(131073, 0);
            GLFW.glfwWindowHint(131084, 0);
            GLFW.glfwWindowHint(131077, vp.hasFlags(8) ? 0 : 1);
            GLFW.glfwWindowHint(131079, vp.hasFlags(1024) ? 1 : 0);
            vd.window = GLFW.glfwCreateWindow((int)vp.getSizeX(), (int)vp.getSizeY(), "No Title Yet", 0L, ImGuiImplGlfw.this.data.window);
            vd.windowOwned = true;
            vp.setPlatformHandle(vd.window);
            if (IS_WINDOWS) {
                vp.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(vd.window));
            } else if (IS_APPLE) {
                vp.setPlatformHandleRaw(GLFWNativeCocoa.glfwGetCocoaWindow((long)vd.window));
            }
            GLFW.glfwSetWindowPos(vd.window, (int)vp.getPosX(), (int)vp.getPosY());
            GLFW.glfwSetWindowFocusCallback(vd.window, ImGuiImplGlfw.this::windowFocusCallback);
            GLFW.glfwSetCursorEnterCallback(vd.window, ImGuiImplGlfw.this::cursorEnterCallback);
            GLFW.glfwSetCursorPosCallback(vd.window, ImGuiImplGlfw.this::cursorPosCallback);
            GLFW.glfwSetMouseButtonCallback(vd.window, ImGuiImplGlfw.this::mouseButtonCallback);
            GLFW.glfwSetScrollCallback(vd.window, ImGuiImplGlfw.this::scrollCallback);
            GLFW.glfwSetKeyCallback(vd.window, ImGuiImplGlfw.this::keyCallback);
            GLFW.glfwSetCharCallback(vd.window, ImGuiImplGlfw.this::charCallback);
            GLFW.glfwSetWindowCloseCallback(vd.window, x$0 -> ImGuiImplGlfw.this.windowCloseCallback(x$0));
            GLFW.glfwSetWindowPosCallback(vd.window, (x$0, x$1, x$2) -> ImGuiImplGlfw.this.windowPosCallback(x$0, x$1, x$2));
            GLFW.glfwSetWindowSizeCallback(vd.window, (x$0, x$1, x$2) -> ImGuiImplGlfw.this.windowSizeCallback(x$0, x$1, x$2));
            GLFW.glfwMakeContextCurrent(vd.window);
            GLFW.glfwSwapInterval(0);
        }
    }

    private static final class Properties {
        private final int[] windowW = new int[1];
        private final int[] windowH = new int[1];
        private final int[] windowX = new int[1];
        private final int[] windowY = new int[1];
        private final int[] displayW = new int[1];
        private final int[] displayH = new int[1];
        private final ImVec2 mousePosPrev = new ImVec2();
        private final double[] mouseX = new double[1];
        private final double[] mouseY = new double[1];
        private final int[] monitorX = new int[1];
        private final int[] monitorY = new int[1];
        private final int[] monitorWorkAreaX = new int[1];
        private final int[] monitorWorkAreaY = new int[1];
        private final int[] monitorWorkAreaWidth = new int[1];
        private final int[] monitorWorkAreaHeight = new int[1];
        private final float[] monitorContentScaleX = new float[1];
        private final float[] monitorContentScaleY = new float[1];
        private final String charNames = "`-=[]\\,;'./";
        private final int[] charKeys = new int[]{96, 45, 61, 91, 93, 92, 44, 59, 39, 46, 47};

        private Properties() {
        }
    }

    protected static class Data {
        protected long window = -1L;
        protected double time = 0.0;
        protected long mouseWindow = -1L;
        protected long[] mouseCursors = new long[9];
        protected ImVec2 lastValidMousePos = new ImVec2();
        protected long[] keyOwnerWindows = new long[348];
        protected boolean installedCallbacks = false;
        protected boolean callbacksChainForAllWindows = false;
        protected boolean wantUpdateMonitors = true;
        protected GLFWWindowFocusCallback prevUserCallbackWindowFocus = null;
        protected GLFWCursorPosCallback prevUserCallbackCursorPos = null;
        protected GLFWCursorEnterCallback prevUserCallbackCursorEnter = null;
        protected GLFWMouseButtonCallback prevUserCallbackMousebutton = null;
        protected GLFWScrollCallback prevUserCallbackScroll = null;
        protected GLFWKeyCallback prevUserCallbackKey = null;
        protected GLFWCharCallback prevUserCallbackChar = null;
        protected GLFWMonitorCallback prevUserCallbackMonitor = null;

        protected Data() {
        }
    }
}

