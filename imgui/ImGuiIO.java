/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.ImGuiKeyData
 */
package imgui;

import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGuiKeyData;
import imgui.ImVec2;
import imgui.binding.ImGuiStruct;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.internal.ImGuiContext;

public final class ImGuiIO
extends ImGuiStruct {
    private static final ImFontAtlas _GETFONTS_1 = new ImFontAtlas(0L);
    private static final ImGuiContext _GETCTX_1 = new ImGuiContext(0L);

    public ImGuiIO(long ptr) {
        super(ptr);
    }

    public int getConfigFlags() {
        return this.nGetConfigFlags();
    }

    public void setConfigFlags(int value) {
        this.nSetConfigFlags(value);
    }

    public void addConfigFlags(int flags) {
        this.setConfigFlags(this.getConfigFlags() | flags);
    }

    public void removeConfigFlags(int flags) {
        this.setConfigFlags(this.getConfigFlags() & ~flags);
    }

    public boolean hasConfigFlags(int flags) {
        return (this.getConfigFlags() & flags) != 0;
    }

    private native int nGetConfigFlags();

    private native void nSetConfigFlags(int var1);

    public int getBackendFlags() {
        return this.nGetBackendFlags();
    }

    public void setBackendFlags(int value) {
        this.nSetBackendFlags(value);
    }

    public void addBackendFlags(int flags) {
        this.setBackendFlags(this.getBackendFlags() | flags);
    }

    public void removeBackendFlags(int flags) {
        this.setBackendFlags(this.getBackendFlags() & ~flags);
    }

    public boolean hasBackendFlags(int flags) {
        return (this.getBackendFlags() & flags) != 0;
    }

    private native int nGetBackendFlags();

    private native void nSetBackendFlags(int var1);

    public ImVec2 getDisplaySize() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplaySize(dst);
        return dst;
    }

    public float getDisplaySizeX() {
        return this.nGetDisplaySizeX();
    }

    public float getDisplaySizeY() {
        return this.nGetDisplaySizeY();
    }

    public void getDisplaySize(ImVec2 dst) {
        this.nGetDisplaySize(dst);
    }

    public void setDisplaySize(ImVec2 value) {
        this.nSetDisplaySize(value.x, value.y);
    }

    public void setDisplaySize(float valueX, float valueY) {
        this.nSetDisplaySize(valueX, valueY);
    }

    private native void nGetDisplaySize(ImVec2 var1);

    private native float nGetDisplaySizeX();

    private native float nGetDisplaySizeY();

    private native void nSetDisplaySize(float var1, float var2);

    public float getDeltaTime() {
        return this.nGetDeltaTime();
    }

    public void setDeltaTime(float value) {
        this.nSetDeltaTime(value);
    }

    private native float nGetDeltaTime();

    private native void nSetDeltaTime(float var1);

    public float getIniSavingRate() {
        return this.nGetIniSavingRate();
    }

    public void setIniSavingRate(float value) {
        this.nSetIniSavingRate(value);
    }

    private native float nGetIniSavingRate();

    private native void nSetIniSavingRate(float var1);

    public String getIniFilename() {
        return this.nGetIniFilename();
    }

    public void setIniFilename(String value) {
        this.nSetIniFilename(value);
    }

    private native String nGetIniFilename();

    private native void nSetIniFilename(String var1);

    public String getLogFilename() {
        return this.nGetLogFilename();
    }

    public void setLogFilename(String value) {
        this.nSetLogFilename(value);
    }

    private native String nGetLogFilename();

    private native void nSetLogFilename(String var1);

    public ImFontAtlas getFonts() {
        ImGuiIO._GETFONTS_1.ptr = this.nGetFonts();
        return _GETFONTS_1;
    }

    public void setFonts(ImFontAtlas value) {
        this.nSetFonts(value.ptr);
    }

    private native long nGetFonts();

    private native void nSetFonts(long var1);

    public float getFontGlobalScale() {
        return this.nGetFontGlobalScale();
    }

    public void setFontGlobalScale(float value) {
        this.nSetFontGlobalScale(value);
    }

    private native float nGetFontGlobalScale();

    private native void nSetFontGlobalScale(float var1);

    public boolean getFontAllowUserScaling() {
        return this.nGetFontAllowUserScaling();
    }

    public void setFontAllowUserScaling(boolean value) {
        this.nSetFontAllowUserScaling(value);
    }

    private native boolean nGetFontAllowUserScaling();

    private native void nSetFontAllowUserScaling(boolean var1);

    public ImFont getFontDefault() {
        return new ImFont(this.nGetFontDefault());
    }

    public void setFontDefault(ImFont value) {
        this.nSetFontDefault(value.ptr);
    }

    private native long nGetFontDefault();

    private native void nSetFontDefault(long var1);

    public ImVec2 getDisplayFramebufferScale() {
        ImVec2 dst = new ImVec2();
        this.nGetDisplayFramebufferScale(dst);
        return dst;
    }

    public float getDisplayFramebufferScaleX() {
        return this.nGetDisplayFramebufferScaleX();
    }

    public float getDisplayFramebufferScaleY() {
        return this.nGetDisplayFramebufferScaleY();
    }

    public void getDisplayFramebufferScale(ImVec2 dst) {
        this.nGetDisplayFramebufferScale(dst);
    }

    public void setDisplayFramebufferScale(ImVec2 value) {
        this.nSetDisplayFramebufferScale(value.x, value.y);
    }

    public void setDisplayFramebufferScale(float valueX, float valueY) {
        this.nSetDisplayFramebufferScale(valueX, valueY);
    }

    private native void nGetDisplayFramebufferScale(ImVec2 var1);

    private native float nGetDisplayFramebufferScaleX();

    private native float nGetDisplayFramebufferScaleY();

    private native void nSetDisplayFramebufferScale(float var1, float var2);

    public boolean getConfigDockingNoSplit() {
        return this.nGetConfigDockingNoSplit();
    }

    public void setConfigDockingNoSplit(boolean value) {
        this.nSetConfigDockingNoSplit(value);
    }

    private native boolean nGetConfigDockingNoSplit();

    private native void nSetConfigDockingNoSplit(boolean var1);

    public boolean getConfigDockingWithShift() {
        return this.nGetConfigDockingWithShift();
    }

    public void setConfigDockingWithShift(boolean value) {
        this.nSetConfigDockingWithShift(value);
    }

    private native boolean nGetConfigDockingWithShift();

    private native void nSetConfigDockingWithShift(boolean var1);

    public boolean getConfigDockingAlwaysTabBar() {
        return this.nGetConfigDockingAlwaysTabBar();
    }

    public void setConfigDockingAlwaysTabBar(boolean value) {
        this.nSetConfigDockingAlwaysTabBar(value);
    }

    private native boolean nGetConfigDockingAlwaysTabBar();

    private native void nSetConfigDockingAlwaysTabBar(boolean var1);

    public boolean getConfigDockingTransparentPayload() {
        return this.nGetConfigDockingTransparentPayload();
    }

    public void setConfigDockingTransparentPayload(boolean value) {
        this.nSetConfigDockingTransparentPayload(value);
    }

    private native boolean nGetConfigDockingTransparentPayload();

    private native void nSetConfigDockingTransparentPayload(boolean var1);

    public boolean getConfigViewportsNoAutoMerge() {
        return this.nGetConfigViewportsNoAutoMerge();
    }

    public void setConfigViewportsNoAutoMerge(boolean value) {
        this.nSetConfigViewportsNoAutoMerge(value);
    }

    private native boolean nGetConfigViewportsNoAutoMerge();

    private native void nSetConfigViewportsNoAutoMerge(boolean var1);

    public boolean getConfigViewportsNoTaskBarIcon() {
        return this.nGetConfigViewportsNoTaskBarIcon();
    }

    public void setConfigViewportsNoTaskBarIcon(boolean value) {
        this.nSetConfigViewportsNoTaskBarIcon(value);
    }

    private native boolean nGetConfigViewportsNoTaskBarIcon();

    private native void nSetConfigViewportsNoTaskBarIcon(boolean var1);

    public boolean getConfigViewportsNoDecoration() {
        return this.nGetConfigViewportsNoDecoration();
    }

    public void setConfigViewportsNoDecoration(boolean value) {
        this.nSetConfigViewportsNoDecoration(value);
    }

    private native boolean nGetConfigViewportsNoDecoration();

    private native void nSetConfigViewportsNoDecoration(boolean var1);

    public boolean getConfigViewportsNoDefaultParent() {
        return this.nGetConfigViewportsNoDefaultParent();
    }

    public void setConfigViewportsNoDefaultParent(boolean value) {
        this.nSetConfigViewportsNoDefaultParent(value);
    }

    private native boolean nGetConfigViewportsNoDefaultParent();

    private native void nSetConfigViewportsNoDefaultParent(boolean var1);

    public boolean getMouseDrawCursor() {
        return this.nGetMouseDrawCursor();
    }

    public void setMouseDrawCursor(boolean value) {
        this.nSetMouseDrawCursor(value);
    }

    private native boolean nGetMouseDrawCursor();

    private native void nSetMouseDrawCursor(boolean var1);

    public boolean getConfigMacOSXBehaviors() {
        return this.nGetConfigMacOSXBehaviors();
    }

    public void setConfigMacOSXBehaviors(boolean value) {
        this.nSetConfigMacOSXBehaviors(value);
    }

    private native boolean nGetConfigMacOSXBehaviors();

    private native void nSetConfigMacOSXBehaviors(boolean var1);

    public boolean getConfigInputTrickleEventQueue() {
        return this.nGetConfigInputTrickleEventQueue();
    }

    public void setConfigInputTrickleEventQueue(boolean value) {
        this.nSetConfigInputTrickleEventQueue(value);
    }

    private native boolean nGetConfigInputTrickleEventQueue();

    private native void nSetConfigInputTrickleEventQueue(boolean var1);

    public boolean getConfigInputTextCursorBlink() {
        return this.nGetConfigInputTextCursorBlink();
    }

    public void setConfigInputTextCursorBlink(boolean value) {
        this.nSetConfigInputTextCursorBlink(value);
    }

    private native boolean nGetConfigInputTextCursorBlink();

    private native void nSetConfigInputTextCursorBlink(boolean var1);

    public boolean getConfigInputTextEnterKeepActive() {
        return this.nGetConfigInputTextEnterKeepActive();
    }

    public void setConfigInputTextEnterKeepActive(boolean value) {
        this.nSetConfigInputTextEnterKeepActive(value);
    }

    private native boolean nGetConfigInputTextEnterKeepActive();

    private native void nSetConfigInputTextEnterKeepActive(boolean var1);

    public boolean getConfigDragClickToInputText() {
        return this.nGetConfigDragClickToInputText();
    }

    public void setConfigDragClickToInputText(boolean value) {
        this.nSetConfigDragClickToInputText(value);
    }

    private native boolean nGetConfigDragClickToInputText();

    private native void nSetConfigDragClickToInputText(boolean var1);

    public boolean getConfigWindowsResizeFromEdges() {
        return this.nGetConfigWindowsResizeFromEdges();
    }

    public void setConfigWindowsResizeFromEdges(boolean value) {
        this.nSetConfigWindowsResizeFromEdges(value);
    }

    private native boolean nGetConfigWindowsResizeFromEdges();

    private native void nSetConfigWindowsResizeFromEdges(boolean var1);

    public boolean getConfigWindowsMoveFromTitleBarOnly() {
        return this.nGetConfigWindowsMoveFromTitleBarOnly();
    }

    public void setConfigWindowsMoveFromTitleBarOnly(boolean value) {
        this.nSetConfigWindowsMoveFromTitleBarOnly(value);
    }

    private native boolean nGetConfigWindowsMoveFromTitleBarOnly();

    private native void nSetConfigWindowsMoveFromTitleBarOnly(boolean var1);

    public boolean getConfigMemoryCompactTimer() {
        return this.nGetConfigMemoryCompactTimer();
    }

    public void setConfigMemoryCompactTimer(boolean value) {
        this.nSetConfigMemoryCompactTimer(value);
    }

    private native boolean nGetConfigMemoryCompactTimer();

    private native void nSetConfigMemoryCompactTimer(boolean var1);

    public float getMouseDoubleClickTime() {
        return this.nGetMouseDoubleClickTime();
    }

    public void setMouseDoubleClickTime(float value) {
        this.nSetMouseDoubleClickTime(value);
    }

    private native float nGetMouseDoubleClickTime();

    private native void nSetMouseDoubleClickTime(float var1);

    public float getMouseDoubleClickMaxDist() {
        return this.nGetMouseDoubleClickMaxDist();
    }

    public void setMouseDoubleClickMaxDist(float value) {
        this.nSetMouseDoubleClickMaxDist(value);
    }

    private native float nGetMouseDoubleClickMaxDist();

    private native void nSetMouseDoubleClickMaxDist(float var1);

    public float getMouseDragThreshold() {
        return this.nGetMouseDragThreshold();
    }

    public void setMouseDragThreshold(float value) {
        this.nSetMouseDragThreshold(value);
    }

    private native float nGetMouseDragThreshold();

    private native void nSetMouseDragThreshold(float var1);

    public float getKeyRepeatDelay() {
        return this.nGetKeyRepeatDelay();
    }

    public void setKeyRepeatDelay(float value) {
        this.nSetKeyRepeatDelay(value);
    }

    private native float nGetKeyRepeatDelay();

    private native void nSetKeyRepeatDelay(float var1);

    public float getKeyRepeatRate() {
        return this.nGetKeyRepeatRate();
    }

    public void setKeyRepeatRate(float value) {
        this.nSetKeyRepeatRate(value);
    }

    private native float nGetKeyRepeatRate();

    private native void nSetKeyRepeatRate(float var1);

    public boolean getConfigDebugBeginReturnValueOnce() {
        return this.nGetConfigDebugBeginReturnValueOnce();
    }

    public void setConfigDebugBeginReturnValueOnce(boolean value) {
        this.nSetConfigDebugBeginReturnValueOnce(value);
    }

    private native boolean nGetConfigDebugBeginReturnValueOnce();

    private native void nSetConfigDebugBeginReturnValueOnce(boolean var1);

    public boolean getConfigDebugBeginReturnValueLoop() {
        return this.nGetConfigDebugBeginReturnValueLoop();
    }

    public void setConfigDebugBeginReturnValueLoop(boolean value) {
        this.nSetConfigDebugBeginReturnValueLoop(value);
    }

    private native boolean nGetConfigDebugBeginReturnValueLoop();

    private native void nSetConfigDebugBeginReturnValueLoop(boolean var1);

    public boolean getConfigDebugIgnoreFocusLoss() {
        return this.nGetConfigDebugIgnoreFocusLoss();
    }

    public void setConfigDebugIgnoreFocusLoss(boolean value) {
        this.nSetConfigDebugIgnoreFocusLoss(value);
    }

    private native boolean nGetConfigDebugIgnoreFocusLoss();

    private native void nSetConfigDebugIgnoreFocusLoss(boolean var1);

    public boolean getConfigDebugIniSettings() {
        return this.nGetConfigDebugIniSettings();
    }

    public void setConfigDebugIniSettings(boolean value) {
        this.nSetConfigDebugIniSettings(value);
    }

    private native boolean nGetConfigDebugIniSettings();

    private native void nSetConfigDebugIniSettings(boolean var1);

    public String getBackendPlatformName() {
        return this.nGetBackendPlatformName();
    }

    public void setBackendPlatformName(String value) {
        this.nSetBackendPlatformName(value);
    }

    private native String nGetBackendPlatformName();

    private native void nSetBackendPlatformName(String var1);

    public String getBackendRendererName() {
        return this.nGetBackendRendererName();
    }

    public void setBackendRendererName(String value) {
        this.nSetBackendRendererName(value);
    }

    private native String nGetBackendRendererName();

    private native void nSetBackendRendererName(String var1);

    public native void setSetClipboardTextFn(ImStrConsumer var1);

    public native void setGetClipboardTextFn(ImStrSupplier var1);

    public short getPlatformLocaleDecimalPoint() {
        return this.nGetPlatformLocaleDecimalPoint();
    }

    public void setPlatformLocaleDecimalPoint(short value) {
        this.nSetPlatformLocaleDecimalPoint(value);
    }

    private native short nGetPlatformLocaleDecimalPoint();

    private native void nSetPlatformLocaleDecimalPoint(short var1);

    public void addKeyEvent(int key, boolean down) {
        this.nAddKeyEvent(key, down);
    }

    private native void nAddKeyEvent(int var1, boolean var2);

    public void addKeyAnalogEvent(int key, boolean down, float v) {
        this.nAddKeyAnalogEvent(key, down, v);
    }

    private native void nAddKeyAnalogEvent(int var1, boolean var2, float var3);

    public void addMousePosEvent(float x, float y) {
        this.nAddMousePosEvent(x, y);
    }

    private native void nAddMousePosEvent(float var1, float var2);

    public void addMouseButtonEvent(int button, boolean down) {
        this.nAddMouseButtonEvent(button, down);
    }

    private native void nAddMouseButtonEvent(int var1, boolean var2);

    public void addMouseWheelEvent(float whX, float whY) {
        this.nAddMouseWheelEvent(whX, whY);
    }

    private native void nAddMouseWheelEvent(float var1, float var2);

    public void addMouseSourceEvent(int source) {
        this.nAddMouseSourceEvent(source);
    }

    private native void nAddMouseSourceEvent(int var1);

    public void addMouseViewportEvent(int id) {
        this.nAddMouseViewportEvent(id);
    }

    private native void nAddMouseViewportEvent(int var1);

    public void addFocusEvent(boolean focused) {
        this.nAddFocusEvent(focused);
    }

    private native void nAddFocusEvent(boolean var1);

    public void addInputCharacter(int c) {
        this.nAddInputCharacter(c);
    }

    private native void nAddInputCharacter(int var1);

    public void addInputCharacterUTF16(short c) {
        this.nAddInputCharacterUTF16(c);
    }

    private native void nAddInputCharacterUTF16(short var1);

    public void addInputCharactersUTF8(String str) {
        this.nAddInputCharactersUTF8(str);
    }

    private native void nAddInputCharactersUTF8(String var1);

    public void setKeyEventNativeData(int key, int nativeKeycode, int nativeScancode) {
        this.nSetKeyEventNativeData(key, nativeKeycode, nativeScancode);
    }

    public void setKeyEventNativeData(int key, int nativeKeycode, int nativeScancode, int nativeLegacyIndex) {
        this.nSetKeyEventNativeData(key, nativeKeycode, nativeScancode, nativeLegacyIndex);
    }

    private native void nSetKeyEventNativeData(int var1, int var2, int var3);

    private native void nSetKeyEventNativeData(int var1, int var2, int var3, int var4);

    public void setAppAcceptingEvents(boolean acceptingEvents) {
        this.nSetAppAcceptingEvents(acceptingEvents);
    }

    private native void nSetAppAcceptingEvents(boolean var1);

    public void clearEventsQueue() {
        this.nClearEventsQueue();
    }

    private native void nClearEventsQueue();

    public boolean getWantCaptureMouse() {
        return this.nGetWantCaptureMouse();
    }

    public void setWantCaptureMouse(boolean value) {
        this.nSetWantCaptureMouse(value);
    }

    private native boolean nGetWantCaptureMouse();

    private native void nSetWantCaptureMouse(boolean var1);

    public boolean getWantCaptureKeyboard() {
        return this.nGetWantCaptureKeyboard();
    }

    public void setWantCaptureKeyboard(boolean value) {
        this.nSetWantCaptureKeyboard(value);
    }

    private native boolean nGetWantCaptureKeyboard();

    private native void nSetWantCaptureKeyboard(boolean var1);

    public boolean getWantTextInput() {
        return this.nGetWantTextInput();
    }

    public void setWantTextInput(boolean value) {
        this.nSetWantTextInput(value);
    }

    private native boolean nGetWantTextInput();

    private native void nSetWantTextInput(boolean var1);

    public boolean getWantSetMousePos() {
        return this.nGetWantSetMousePos();
    }

    public void setWantSetMousePos(boolean value) {
        this.nSetWantSetMousePos(value);
    }

    private native boolean nGetWantSetMousePos();

    private native void nSetWantSetMousePos(boolean var1);

    public boolean getWantSaveIniSettings() {
        return this.nGetWantSaveIniSettings();
    }

    public void setWantSaveIniSettings(boolean value) {
        this.nSetWantSaveIniSettings(value);
    }

    private native boolean nGetWantSaveIniSettings();

    private native void nSetWantSaveIniSettings(boolean var1);

    public boolean getNavActive() {
        return this.nGetNavActive();
    }

    public void setNavActive(boolean value) {
        this.nSetNavActive(value);
    }

    private native boolean nGetNavActive();

    private native void nSetNavActive(boolean var1);

    public boolean getNavVisible() {
        return this.nGetNavVisible();
    }

    public void setNavVisible(boolean value) {
        this.nSetNavVisible(value);
    }

    private native boolean nGetNavVisible();

    private native void nSetNavVisible(boolean var1);

    public float getFramerate() {
        return this.nGetFramerate();
    }

    public void setFramerate(float value) {
        this.nSetFramerate(value);
    }

    private native float nGetFramerate();

    private native void nSetFramerate(float var1);

    public int getMetricsRenderVertices() {
        return this.nGetMetricsRenderVertices();
    }

    public void setMetricsRenderVertices(int value) {
        this.nSetMetricsRenderVertices(value);
    }

    private native int nGetMetricsRenderVertices();

    private native void nSetMetricsRenderVertices(int var1);

    public int getMetricsRenderIndices() {
        return this.nGetMetricsRenderIndices();
    }

    public void setMetricsRenderIndices(int value) {
        this.nSetMetricsRenderIndices(value);
    }

    private native int nGetMetricsRenderIndices();

    private native void nSetMetricsRenderIndices(int var1);

    public int getMetricsRenderWindows() {
        return this.nGetMetricsRenderWindows();
    }

    public void setMetricsRenderWindows(int value) {
        this.nSetMetricsRenderWindows(value);
    }

    private native int nGetMetricsRenderWindows();

    private native void nSetMetricsRenderWindows(int var1);

    public int getMetricsActiveWindows() {
        return this.nGetMetricsActiveWindows();
    }

    public void setMetricsActiveWindows(int value) {
        this.nSetMetricsActiveWindows(value);
    }

    private native int nGetMetricsActiveWindows();

    private native void nSetMetricsActiveWindows(int var1);

    public int getMetricsActiveAllocations() {
        return this.nGetMetricsActiveAllocations();
    }

    public void setMetricsActiveAllocations(int value) {
        this.nSetMetricsActiveAllocations(value);
    }

    private native int nGetMetricsActiveAllocations();

    private native void nSetMetricsActiveAllocations(int var1);

    public ImVec2 getMouseDelta() {
        ImVec2 dst = new ImVec2();
        this.nGetMouseDelta(dst);
        return dst;
    }

    public float getMouseDeltaX() {
        return this.nGetMouseDeltaX();
    }

    public float getMouseDeltaY() {
        return this.nGetMouseDeltaY();
    }

    public void getMouseDelta(ImVec2 dst) {
        this.nGetMouseDelta(dst);
    }

    public void setMouseDelta(ImVec2 value) {
        this.nSetMouseDelta(value.x, value.y);
    }

    public void setMouseDelta(float valueX, float valueY) {
        this.nSetMouseDelta(valueX, valueY);
    }

    private native void nGetMouseDelta(ImVec2 var1);

    private native float nGetMouseDeltaX();

    private native float nGetMouseDeltaY();

    private native void nSetMouseDelta(float var1, float var2);

    @Deprecated
    public int[] getKeyMap() {
        return this.nGetKeyMap();
    }

    @Deprecated
    public int getKeyMap(int idx) {
        return this.nGetKeyMap(idx);
    }

    @Deprecated
    public void setKeyMap(int[] value) {
        this.nSetKeyMap(value);
    }

    @Deprecated
    public void setKeyMap(int idx, int value) {
        this.nSetKeyMap(idx, value);
    }

    @Deprecated
    private native int[] nGetKeyMap();

    @Deprecated
    private native int nGetKeyMap(int var1);

    @Deprecated
    private native void nSetKeyMap(int[] var1);

    @Deprecated
    private native void nSetKeyMap(int var1, int var2);

    @Deprecated
    public boolean[] getKeysDown() {
        return this.nGetKeysDown();
    }

    @Deprecated
    public boolean getKeysDown(int idx) {
        return this.nGetKeysDown(idx);
    }

    @Deprecated
    public void setKeysDown(boolean[] value) {
        this.nSetKeysDown(value);
    }

    @Deprecated
    public void setKeysDown(int idx, boolean value) {
        this.nSetKeysDown(idx, value);
    }

    @Deprecated
    private native boolean[] nGetKeysDown();

    @Deprecated
    private native boolean nGetKeysDown(int var1);

    @Deprecated
    private native void nSetKeysDown(boolean[] var1);

    @Deprecated
    private native void nSetKeysDown(int var1, boolean var2);

    public float[] getNavInputs() {
        return this.nGetNavInputs();
    }

    public float getNavInputs(int idx) {
        return this.nGetNavInputs(idx);
    }

    public void setNavInputs(float[] value) {
        this.nSetNavInputs(value);
    }

    public void setNavInputs(int idx, float value) {
        this.nSetNavInputs(idx, value);
    }

    private native float[] nGetNavInputs();

    private native float nGetNavInputs(int var1);

    private native void nSetNavInputs(float[] var1);

    private native void nSetNavInputs(int var1, float var2);

    public ImGuiContext getCtx() {
        ImGuiIO._GETCTX_1.ptr = this.nGetCtx();
        return _GETCTX_1;
    }

    public void setCtx(ImGuiContext value) {
        this.nSetCtx(value.ptr);
    }

    private native long nGetCtx();

    private native void nSetCtx(long var1);

    public ImVec2 getMousePos() {
        ImVec2 dst = new ImVec2();
        this.nGetMousePos(dst);
        return dst;
    }

    public float getMousePosX() {
        return this.nGetMousePosX();
    }

    public float getMousePosY() {
        return this.nGetMousePosY();
    }

    public void getMousePos(ImVec2 dst) {
        this.nGetMousePos(dst);
    }

    public void setMousePos(ImVec2 value) {
        this.nSetMousePos(value.x, value.y);
    }

    public void setMousePos(float valueX, float valueY) {
        this.nSetMousePos(valueX, valueY);
    }

    private native void nGetMousePos(ImVec2 var1);

    private native float nGetMousePosX();

    private native float nGetMousePosY();

    private native void nSetMousePos(float var1, float var2);

    public boolean[] getMouseDown() {
        return this.nGetMouseDown();
    }

    public boolean getMouseDown(int idx) {
        return this.nGetMouseDown(idx);
    }

    public void setMouseDown(boolean[] value) {
        this.nSetMouseDown(value);
    }

    public void setMouseDown(int idx, boolean value) {
        this.nSetMouseDown(idx, value);
    }

    private native boolean[] nGetMouseDown();

    private native boolean nGetMouseDown(int var1);

    private native void nSetMouseDown(boolean[] var1);

    private native void nSetMouseDown(int var1, boolean var2);

    public float getMouseWheel() {
        return this.nGetMouseWheel();
    }

    public void setMouseWheel(float value) {
        this.nSetMouseWheel(value);
    }

    private native float nGetMouseWheel();

    private native void nSetMouseWheel(float var1);

    public float getMouseWheelH() {
        return this.nGetMouseWheelH();
    }

    public void setMouseWheelH(float value) {
        this.nSetMouseWheelH(value);
    }

    private native float nGetMouseWheelH();

    private native void nSetMouseWheelH(float var1);

    public int getMouseHoveredViewport() {
        return this.nGetMouseHoveredViewport();
    }

    public void setMouseHoveredViewport(int value) {
        this.nSetMouseHoveredViewport(value);
    }

    private native int nGetMouseHoveredViewport();

    private native void nSetMouseHoveredViewport(int var1);

    public boolean getKeyCtrl() {
        return this.nGetKeyCtrl();
    }

    public void setKeyCtrl(boolean value) {
        this.nSetKeyCtrl(value);
    }

    private native boolean nGetKeyCtrl();

    private native void nSetKeyCtrl(boolean var1);

    public boolean getKeyShift() {
        return this.nGetKeyShift();
    }

    public void setKeyShift(boolean value) {
        this.nSetKeyShift(value);
    }

    private native boolean nGetKeyShift();

    private native void nSetKeyShift(boolean var1);

    public boolean getKeyAlt() {
        return this.nGetKeyAlt();
    }

    public void setKeyAlt(boolean value) {
        this.nSetKeyAlt(value);
    }

    private native boolean nGetKeyAlt();

    private native void nSetKeyAlt(boolean var1);

    public boolean getKeySuper() {
        return this.nGetKeySuper();
    }

    public void setKeySuper(boolean value) {
        this.nSetKeySuper(value);
    }

    private native boolean nGetKeySuper();

    private native void nSetKeySuper(boolean var1);

    public int getKeyMods() {
        return this.nGetKeyMods();
    }

    public void setKeyMods(int value) {
        this.nSetKeyMods(value);
    }

    private native int nGetKeyMods();

    private native void nSetKeyMods(int var1);

    public ImGuiKeyData[] getKeysData() {
        return this.nGetKeysData();
    }

    public void setKeysData(ImGuiKeyData[] value) {
        this.nSetKeysData(value);
    }

    private native ImGuiKeyData[] nGetKeysData();

    private native void nSetKeysData(ImGuiKeyData[] var1);

    public boolean getWantCaptureMouseUnlessPopupClose() {
        return this.nGetWantCaptureMouseUnlessPopupClose();
    }

    public void setWantCaptureMouseUnlessPopupClose(boolean value) {
        this.nSetWantCaptureMouseUnlessPopupClose(value);
    }

    private native boolean nGetWantCaptureMouseUnlessPopupClose();

    private native void nSetWantCaptureMouseUnlessPopupClose(boolean var1);

    public ImVec2 getMousePosPrev() {
        ImVec2 dst = new ImVec2();
        this.nGetMousePosPrev(dst);
        return dst;
    }

    public float getMousePosPrevX() {
        return this.nGetMousePosPrevX();
    }

    public float getMousePosPrevY() {
        return this.nGetMousePosPrevY();
    }

    public void getMousePosPrev(ImVec2 dst) {
        this.nGetMousePosPrev(dst);
    }

    public void setMousePosPrev(ImVec2 value) {
        this.nSetMousePosPrev(value.x, value.y);
    }

    public void setMousePosPrev(float valueX, float valueY) {
        this.nSetMousePosPrev(valueX, valueY);
    }

    private native void nGetMousePosPrev(ImVec2 var1);

    private native float nGetMousePosPrevX();

    private native float nGetMousePosPrevY();

    private native void nSetMousePosPrev(float var1, float var2);

    public ImVec2[] getMouseClickedPos() {
        return this.nGetMouseClickedPos();
    }

    public void setMouseClickedPos(ImVec2[] value) {
        this.nSetMouseClickedPos(value);
    }

    private native ImVec2[] nGetMouseClickedPos();

    private native void nSetMouseClickedPos(ImVec2[] var1);

    public double[] getMouseClickedTime() {
        return this.nGetMouseClickedTime();
    }

    public double getMouseClickedTime(int idx) {
        return this.nGetMouseClickedTime(idx);
    }

    public void setMouseClickedTime(double[] value) {
        this.nSetMouseClickedTime(value);
    }

    public void setMouseClickedTime(int idx, double value) {
        this.nSetMouseClickedTime(idx, value);
    }

    private native double[] nGetMouseClickedTime();

    private native double nGetMouseClickedTime(int var1);

    private native void nSetMouseClickedTime(double[] var1);

    private native void nSetMouseClickedTime(int var1, double var2);

    public boolean[] getMouseClicked() {
        return this.nGetMouseClicked();
    }

    public boolean getMouseClicked(int idx) {
        return this.nGetMouseClicked(idx);
    }

    public void setMouseClicked(boolean[] value) {
        this.nSetMouseClicked(value);
    }

    public void setMouseClicked(int idx, boolean value) {
        this.nSetMouseClicked(idx, value);
    }

    private native boolean[] nGetMouseClicked();

    private native boolean nGetMouseClicked(int var1);

    private native void nSetMouseClicked(boolean[] var1);

    private native void nSetMouseClicked(int var1, boolean var2);

    public boolean[] getMouseDoubleClicked() {
        return this.nGetMouseDoubleClicked();
    }

    public boolean getMouseDoubleClicked(int idx) {
        return this.nGetMouseDoubleClicked(idx);
    }

    public void setMouseDoubleClicked(boolean[] value) {
        this.nSetMouseDoubleClicked(value);
    }

    public void setMouseDoubleClicked(int idx, boolean value) {
        this.nSetMouseDoubleClicked(idx, value);
    }

    private native boolean[] nGetMouseDoubleClicked();

    private native boolean nGetMouseDoubleClicked(int var1);

    private native void nSetMouseDoubleClicked(boolean[] var1);

    private native void nSetMouseDoubleClicked(int var1, boolean var2);

    public int[] getMouseClickedCount() {
        return this.nGetMouseClickedCount();
    }

    public int getMouseClickedCount(int idx) {
        return this.nGetMouseClickedCount(idx);
    }

    public void setMouseClickedCount(int[] value) {
        this.nSetMouseClickedCount(value);
    }

    public void setMouseClickedCount(int idx, int value) {
        this.nSetMouseClickedCount(idx, value);
    }

    private native int[] nGetMouseClickedCount();

    private native int nGetMouseClickedCount(int var1);

    private native void nSetMouseClickedCount(int[] var1);

    private native void nSetMouseClickedCount(int var1, int var2);

    public int[] getMouseClickedLastCount() {
        return this.nGetMouseClickedLastCount();
    }

    public int getMouseClickedLastCount(int idx) {
        return this.nGetMouseClickedLastCount(idx);
    }

    public void setMouseClickedLastCount(int[] value) {
        this.nSetMouseClickedLastCount(value);
    }

    public void setMouseClickedLastCount(int idx, int value) {
        this.nSetMouseClickedLastCount(idx, value);
    }

    private native int[] nGetMouseClickedLastCount();

    private native int nGetMouseClickedLastCount(int var1);

    private native void nSetMouseClickedLastCount(int[] var1);

    private native void nSetMouseClickedLastCount(int var1, int var2);

    public boolean[] getMouseReleased() {
        return this.nGetMouseReleased();
    }

    public boolean getMouseReleased(int idx) {
        return this.nGetMouseReleased(idx);
    }

    public void setMouseReleased(boolean[] value) {
        this.nSetMouseReleased(value);
    }

    public void setMouseReleased(int idx, boolean value) {
        this.nSetMouseReleased(idx, value);
    }

    private native boolean[] nGetMouseReleased();

    private native boolean nGetMouseReleased(int var1);

    private native void nSetMouseReleased(boolean[] var1);

    private native void nSetMouseReleased(int var1, boolean var2);

    public boolean[] getMouseDownOwned() {
        return this.nGetMouseDownOwned();
    }

    public boolean getMouseDownOwned(int idx) {
        return this.nGetMouseDownOwned(idx);
    }

    public void setMouseDownOwned(boolean[] value) {
        this.nSetMouseDownOwned(value);
    }

    public void setMouseDownOwned(int idx, boolean value) {
        this.nSetMouseDownOwned(idx, value);
    }

    private native boolean[] nGetMouseDownOwned();

    private native boolean nGetMouseDownOwned(int var1);

    private native void nSetMouseDownOwned(boolean[] var1);

    private native void nSetMouseDownOwned(int var1, boolean var2);

    public boolean[] getMouseDownOwnedUnlessPopupClose() {
        return this.nGetMouseDownOwnedUnlessPopupClose();
    }

    public boolean getMouseDownOwnedUnlessPopupClose(int idx) {
        return this.nGetMouseDownOwnedUnlessPopupClose(idx);
    }

    public void setMouseDownOwnedUnlessPopupClose(boolean[] value) {
        this.nSetMouseDownOwnedUnlessPopupClose(value);
    }

    public void setMouseDownOwnedUnlessPopupClose(int idx, boolean value) {
        this.nSetMouseDownOwnedUnlessPopupClose(idx, value);
    }

    private native boolean[] nGetMouseDownOwnedUnlessPopupClose();

    private native boolean nGetMouseDownOwnedUnlessPopupClose(int var1);

    private native void nSetMouseDownOwnedUnlessPopupClose(boolean[] var1);

    private native void nSetMouseDownOwnedUnlessPopupClose(int var1, boolean var2);

    public boolean getMouseWheelRequestAxisSwap() {
        return this.nGetMouseWheelRequestAxisSwap();
    }

    public void setMouseWheelRequestAxisSwap(boolean value) {
        this.nSetMouseWheelRequestAxisSwap(value);
    }

    private native boolean nGetMouseWheelRequestAxisSwap();

    private native void nSetMouseWheelRequestAxisSwap(boolean var1);

    public float[] getMouseDownDuration() {
        return this.nGetMouseDownDuration();
    }

    public float getMouseDownDuration(int idx) {
        return this.nGetMouseDownDuration(idx);
    }

    public void setMouseDownDuration(float[] value) {
        this.nSetMouseDownDuration(value);
    }

    public void setMouseDownDuration(int idx, float value) {
        this.nSetMouseDownDuration(idx, value);
    }

    private native float[] nGetMouseDownDuration();

    private native float nGetMouseDownDuration(int var1);

    private native void nSetMouseDownDuration(float[] var1);

    private native void nSetMouseDownDuration(int var1, float var2);

    public float[] getMouseDownDurationPrev() {
        return this.nGetMouseDownDurationPrev();
    }

    public float getMouseDownDurationPrev(int idx) {
        return this.nGetMouseDownDurationPrev(idx);
    }

    public void setMouseDownDurationPrev(float[] value) {
        this.nSetMouseDownDurationPrev(value);
    }

    public void setMouseDownDurationPrev(int idx, float value) {
        this.nSetMouseDownDurationPrev(idx, value);
    }

    private native float[] nGetMouseDownDurationPrev();

    private native float nGetMouseDownDurationPrev(int var1);

    private native void nSetMouseDownDurationPrev(float[] var1);

    private native void nSetMouseDownDurationPrev(int var1, float var2);

    public ImVec2[] getMouseDragMaxDistanceAbs() {
        return this.nGetMouseDragMaxDistanceAbs();
    }

    public void setMouseDragMaxDistanceAbs(ImVec2[] value) {
        this.nSetMouseDragMaxDistanceAbs(value);
    }

    private native ImVec2[] nGetMouseDragMaxDistanceAbs();

    private native void nSetMouseDragMaxDistanceAbs(ImVec2[] var1);

    public float[] getMouseDragMaxDistanceSqr() {
        return this.nGetMouseDragMaxDistanceSqr();
    }

    public float getMouseDragMaxDistanceSqr(int idx) {
        return this.nGetMouseDragMaxDistanceSqr(idx);
    }

    public void setMouseDragMaxDistanceSqr(float[] value) {
        this.nSetMouseDragMaxDistanceSqr(value);
    }

    public void setMouseDragMaxDistanceSqr(int idx, float value) {
        this.nSetMouseDragMaxDistanceSqr(idx, value);
    }

    private native float[] nGetMouseDragMaxDistanceSqr();

    private native float nGetMouseDragMaxDistanceSqr(int var1);

    private native void nSetMouseDragMaxDistanceSqr(float[] var1);

    private native void nSetMouseDragMaxDistanceSqr(int var1, float var2);

    public float getPenPressure() {
        return this.nGetPenPressure();
    }

    public void setPenPressure(float value) {
        this.nSetPenPressure(value);
    }

    private native float nGetPenPressure();

    private native void nSetPenPressure(float var1);

    public boolean getAppFocusLost() {
        return this.nGetAppFocusLost();
    }

    private native boolean nGetAppFocusLost();

    public boolean getAppAcceptingEvents() {
        return this.nGetAppAcceptingEvents();
    }

    private native boolean nGetAppAcceptingEvents();

    public short getBackendUsingLegacyKeyArrays() {
        return this.nGetBackendUsingLegacyKeyArrays();
    }

    public void setBackendUsingLegacyKeyArrays(short value) {
        this.nSetBackendUsingLegacyKeyArrays(value);
    }

    private native short nGetBackendUsingLegacyKeyArrays();

    private native void nSetBackendUsingLegacyKeyArrays(short var1);

    public boolean getBackendUsingLegacyNavInputArray() {
        return this.nGetBackendUsingLegacyNavInputArray();
    }

    public void setBackendUsingLegacyNavInputArray(boolean value) {
        this.nSetBackendUsingLegacyNavInputArray(value);
    }

    private native boolean nGetBackendUsingLegacyNavInputArray();

    private native void nSetBackendUsingLegacyNavInputArray(boolean var1);

    public short getInputQueueSurrogate() {
        return this.nGetInputQueueSurrogate();
    }

    public void setInputQueueSurrogate(short value) {
        this.nSetInputQueueSurrogate(value);
    }

    private native short nGetInputQueueSurrogate();

    private native void nSetInputQueueSurrogate(short var1);
}

