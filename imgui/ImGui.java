/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.ImGuiStorage
 *  imgui.ImGuiTableSortSpecs
 *  imgui.ImGuiWindowClass
 *  imgui.type.ImDouble
 *  imgui.type.ImLong
 *  imgui.type.ImShort
 */
package imgui;

import imgui.ImDrawData;
import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGuiIO;
import imgui.ImGuiPlatformIO;
import imgui.ImGuiStorage;
import imgui.ImGuiStyle;
import imgui.ImGuiTableSortSpecs;
import imgui.ImGuiViewport;
import imgui.ImGuiWindowClass;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.assertion.ImAssertCallback;
import imgui.callback.ImGuiInputTextCallback;
import imgui.internal.ImGuiContext;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImLong;
import imgui.type.ImShort;
import imgui.type.ImString;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.Properties;

public class ImGui {
    private static final String LIB_PATH_PROP = "imgui.library.path";
    private static final String LIB_NAME_PROP = "imgui.library.name";
    private static final String LIB_NAME_DEFAULT = "imgui-java64";
    private static final String LIB_TMP_DIR_PREFIX = "imgui-java-natives";
    private static final ImGuiIO _GETIO_1;
    private static final ImGuiStyle _GETSTYLE_1;
    private static final ImDrawData _GETDRAWDATA_1;
    private static final ImFont _GETFONT_1;
    private static WeakReference<Object> payloadRef;
    private static final byte[] PAYLOAD_PLACEHOLDER_DATA;
    private static final ImGuiViewport _GETMAINVIEWPORT_1;
    private static final ImGuiPlatformIO _GETPLATFORMIO_1;

    private static String resolveFullLibName() {
        String libSuffix;
        String libPrefix;
        boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        if (isWin) {
            libPrefix = "";
            libSuffix = ".dll";
        } else if (isMac) {
            libPrefix = "lib";
            libSuffix = ".dylib";
        } else {
            libPrefix = "lib";
            libSuffix = ".so";
        }
        return System.getProperty(LIB_NAME_PROP, libPrefix + LIB_NAME_DEFAULT + libSuffix);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String tryLoadFromClasspath(String fullLibName) {
        try (InputStream is = ImGui.class.getClassLoader().getResourceAsStream("io/imgui/java/native-bin/" + fullLibName);){
            Path libBin;
            block21: {
                if (is == null) {
                    String string = null;
                    return string;
                }
                String version = ImGui.getVersionString().orElse("undefined");
                Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), new String[0]).resolve(LIB_TMP_DIR_PREFIX).resolve(version);
                if (!Files.exists(tmpDir, new LinkOption[0])) {
                    Files.createDirectories(tmpDir, new FileAttribute[0]);
                }
                libBin = tmpDir.resolve(fullLibName);
                try {
                    Files.copy(is, libBin, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (AccessDeniedException e) {
                    if (Files.exists(libBin, new LinkOption[0])) break block21;
                    throw e;
                }
            }
            String string = libBin.toAbsolutePath().toString();
            return string;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Optional<String> getVersionString() {
        Properties properties = new Properties();
        try (InputStream is = ImGui.class.getResourceAsStream("/imgui/imgui-java.properties");){
            if (is == null) return Optional.empty();
            properties.load(is);
            Optional<String> optional = Optional.of(properties.get("imgui.java.version").toString());
            return optional;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void init() {
    }

    private static native void nInitJni();

    public static native void setAssertCallback(ImAssertCallback var0);

    public static ImGuiContext createContext() {
        return new ImGuiContext(ImGui.nCreateContext());
    }

    public static ImGuiContext createContext(ImFontAtlas sharedFontAtlas) {
        return new ImGuiContext(ImGui.nCreateContext(sharedFontAtlas.ptr));
    }

    private static native long nCreateContext();

    private static native long nCreateContext(long var0);

    public static void destroyContext() {
        ImGui.nDestroyContext();
    }

    public static void destroyContext(ImGuiContext ctx) {
        ImGui.nDestroyContext(ctx.ptr);
    }

    private static native void nDestroyContext();

    private static native void nDestroyContext(long var0);

    public static ImGuiContext getCurrentContext() {
        return new ImGuiContext(ImGui.nGetCurrentContext());
    }

    private static native long nGetCurrentContext();

    public static void setCurrentContext(ImGuiContext ctx) {
        ImGui.nSetCurrentContext(ctx.ptr);
    }

    private static native void nSetCurrentContext(long var0);

    public static ImGuiIO getIO() {
        ImGui._GETIO_1.ptr = ImGui.nGetIO();
        return _GETIO_1;
    }

    private static native long nGetIO();

    public static ImGuiStyle getStyle() {
        ImGui._GETSTYLE_1.ptr = ImGui.nGetStyle();
        return _GETSTYLE_1;
    }

    private static native long nGetStyle();

    public static void newFrame() {
        ImGui.nNewFrame();
    }

    private static native void nNewFrame();

    public static void endFrame() {
        ImGui.nEndFrame();
    }

    private static native void nEndFrame();

    public static void render() {
        ImGui.nRender();
    }

    private static native void nRender();

    public static ImDrawData getDrawData() {
        ImGui._GETDRAWDATA_1.ptr = ImGui.nGetDrawData();
        return _GETDRAWDATA_1;
    }

    private static native long nGetDrawData();

    public static void showDemoWindow() {
        ImGui.nShowDemoWindow();
    }

    public static void showDemoWindow(ImBoolean pOpen) {
        ImGui.nShowDemoWindow(pOpen != null ? pOpen.getData() : null);
    }

    private static native void nShowDemoWindow();

    private static native void nShowDemoWindow(boolean[] var0);

    public static void showMetricsWindow() {
        ImGui.nShowMetricsWindow();
    }

    public static void showMetricsWindow(ImBoolean pOpen) {
        ImGui.nShowMetricsWindow(pOpen != null ? pOpen.getData() : null);
    }

    private static native void nShowMetricsWindow();

    private static native void nShowMetricsWindow(boolean[] var0);

    public static void showDebugLogWindow() {
        ImGui.nShowDebugLogWindow();
    }

    public static void showDebugLogWindow(ImBoolean pOpen) {
        ImGui.nShowDebugLogWindow(pOpen != null ? pOpen.getData() : null);
    }

    private static native void nShowDebugLogWindow();

    private static native void nShowDebugLogWindow(boolean[] var0);

    public static void showStackToolWindow() {
        ImGui.nShowStackToolWindow();
    }

    public static void showStackToolWindow(ImBoolean pOpen) {
        ImGui.nShowStackToolWindow(pOpen != null ? pOpen.getData() : null);
    }

    private static native void nShowStackToolWindow();

    private static native void nShowStackToolWindow(boolean[] var0);

    public static void showAboutWindow() {
        ImGui.nShowAboutWindow();
    }

    public static void showAboutWindow(ImBoolean pOpen) {
        ImGui.nShowAboutWindow(pOpen != null ? pOpen.getData() : null);
    }

    private static native void nShowAboutWindow();

    private static native void nShowAboutWindow(boolean[] var0);

    public static void showStyleEditor() {
        ImGui.nShowStyleEditor();
    }

    public static void showStyleEditor(ImGuiStyle ref) {
        ImGui.nShowStyleEditor(ref.ptr);
    }

    private static native void nShowStyleEditor();

    private static native void nShowStyleEditor(long var0);

    public static boolean showStyleSelector(String label) {
        return ImGui.nShowStyleSelector(label);
    }

    private static native boolean nShowStyleSelector(String var0);

    public static void showFontSelector(String label) {
        ImGui.nShowFontSelector(label);
    }

    private static native void nShowFontSelector(String var0);

    public static void showUserGuide() {
        ImGui.nShowUserGuide();
    }

    private static native void nShowUserGuide();

    public static String getVersion() {
        return ImGui.nGetVersion();
    }

    private static native String nGetVersion();

    public static void styleColorsDark() {
        ImGui.nStyleColorsDark();
    }

    public static void styleColorsDark(ImGuiStyle style) {
        ImGui.nStyleColorsDark(style.ptr);
    }

    private static native void nStyleColorsDark();

    private static native void nStyleColorsDark(long var0);

    public static void styleColorsLight() {
        ImGui.nStyleColorsLight();
    }

    public static void styleColorsLight(ImGuiStyle style) {
        ImGui.nStyleColorsLight(style.ptr);
    }

    private static native void nStyleColorsLight();

    private static native void nStyleColorsLight(long var0);

    public static void styleColorsClassic() {
        ImGui.nStyleColorsClassic();
    }

    public static void styleColorsClassic(ImGuiStyle style) {
        ImGui.nStyleColorsClassic(style.ptr);
    }

    private static native void nStyleColorsClassic();

    private static native void nStyleColorsClassic(long var0);

    public static boolean begin(String title) {
        return ImGui.nBegin(title);
    }

    public static boolean begin(String title, ImBoolean pOpen) {
        return ImGui.nBegin(title, pOpen != null ? pOpen.getData() : null);
    }

    public static boolean begin(String title, ImBoolean pOpen, int imGuiWindowFlags) {
        return ImGui.nBegin(title, pOpen != null ? pOpen.getData() : null, imGuiWindowFlags);
    }

    public static boolean begin(String title, int imGuiWindowFlags) {
        return ImGui.nBegin(title, imGuiWindowFlags);
    }

    private static native boolean nBegin(String var0);

    private static native boolean nBegin(String var0, boolean[] var1);

    private static native boolean nBegin(String var0, boolean[] var1, int var2);

    private static native boolean nBegin(String var0, int var1);

    public static void end() {
        ImGui.nEnd();
    }

    private static native void nEnd();

    public static boolean beginChild(String strId) {
        return ImGui.nBeginChild(strId);
    }

    public static boolean beginChild(String strId, ImVec2 size) {
        return ImGui.nBeginChild(strId, size.x, size.y);
    }

    public static boolean beginChild(String strId, float sizeX, float sizeY) {
        return ImGui.nBeginChild(strId, sizeX, sizeY);
    }

    public static boolean beginChild(String strId, ImVec2 size, boolean border) {
        return ImGui.nBeginChild(strId, size.x, size.y, border);
    }

    public static boolean beginChild(String strId, float sizeX, float sizeY, boolean border) {
        return ImGui.nBeginChild(strId, sizeX, sizeY, border);
    }

    public static boolean beginChild(String strId, ImVec2 size, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, size.x, size.y, border, imGuiWindowFlags);
    }

    public static boolean beginChild(String strId, float sizeX, float sizeY, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, sizeX, sizeY, border, imGuiWindowFlags);
    }

    public static boolean beginChild(String strId, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, border, imGuiWindowFlags);
    }

    public static boolean beginChild(String strId, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, imGuiWindowFlags);
    }

    public static boolean beginChild(String strId, ImVec2 size, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, size.x, size.y, imGuiWindowFlags);
    }

    public static boolean beginChild(String strId, float sizeX, float sizeY, int imGuiWindowFlags) {
        return ImGui.nBeginChild(strId, sizeX, sizeY, imGuiWindowFlags);
    }

    private static native boolean nBeginChild(String var0);

    private static native boolean nBeginChild(String var0, float var1, float var2);

    private static native boolean nBeginChild(String var0, float var1, float var2, boolean var3);

    private static native boolean nBeginChild(String var0, float var1, float var2, boolean var3, int var4);

    private static native boolean nBeginChild(String var0, boolean var1, int var2);

    private static native boolean nBeginChild(String var0, int var1);

    private static native boolean nBeginChild(String var0, float var1, float var2, int var3);

    public static boolean beginChild(int imGuiID) {
        return ImGui.nBeginChild(imGuiID);
    }

    public static boolean beginChild(int imGuiID, ImVec2 size) {
        return ImGui.nBeginChild(imGuiID, size.x, size.y);
    }

    public static boolean beginChild(int imGuiID, float sizeX, float sizeY) {
        return ImGui.nBeginChild(imGuiID, sizeX, sizeY);
    }

    public static boolean beginChild(int imGuiID, ImVec2 size, boolean border) {
        return ImGui.nBeginChild(imGuiID, size.x, size.y, border);
    }

    public static boolean beginChild(int imGuiID, float sizeX, float sizeY, boolean border) {
        return ImGui.nBeginChild(imGuiID, sizeX, sizeY, border);
    }

    public static boolean beginChild(int imGuiID, ImVec2 size, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, size.x, size.y, border, imGuiWindowFlags);
    }

    public static boolean beginChild(int imGuiID, float sizeX, float sizeY, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, sizeX, sizeY, border, imGuiWindowFlags);
    }

    public static boolean beginChild(int imGuiID, boolean border, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, border, imGuiWindowFlags);
    }

    public static boolean beginChild(int imGuiID, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, imGuiWindowFlags);
    }

    public static boolean beginChild(int imGuiID, ImVec2 size, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, size.x, size.y, imGuiWindowFlags);
    }

    public static boolean beginChild(int imGuiID, float sizeX, float sizeY, int imGuiWindowFlags) {
        return ImGui.nBeginChild(imGuiID, sizeX, sizeY, imGuiWindowFlags);
    }

    private static native boolean nBeginChild(int var0);

    private static native boolean nBeginChild(int var0, float var1, float var2);

    private static native boolean nBeginChild(int var0, float var1, float var2, boolean var3);

    private static native boolean nBeginChild(int var0, float var1, float var2, boolean var3, int var4);

    private static native boolean nBeginChild(int var0, boolean var1, int var2);

    private static native boolean nBeginChild(int var0, int var1);

    private static native boolean nBeginChild(int var0, float var1, float var2, int var3);

    public static void endChild() {
        ImGui.nEndChild();
    }

    private static native void nEndChild();

    public static boolean isWindowAppearing() {
        return ImGui.nIsWindowAppearing();
    }

    private static native boolean nIsWindowAppearing();

    public static boolean isWindowCollapsed() {
        return ImGui.nIsWindowCollapsed();
    }

    private static native boolean nIsWindowCollapsed();

    public static boolean isWindowFocused() {
        return ImGui.nIsWindowFocused();
    }

    public static boolean isWindowFocused(int imGuiFocusedFlags) {
        return ImGui.nIsWindowFocused(imGuiFocusedFlags);
    }

    private static native boolean nIsWindowFocused();

    private static native boolean nIsWindowFocused(int var0);

    public static boolean isWindowHovered() {
        return ImGui.nIsWindowHovered();
    }

    public static boolean isWindowHovered(int imGuiHoveredFlags) {
        return ImGui.nIsWindowHovered(imGuiHoveredFlags);
    }

    private static native boolean nIsWindowHovered();

    private static native boolean nIsWindowHovered(int var0);

    public static ImDrawList getWindowDrawList() {
        return new ImDrawList(ImGui.nGetWindowDrawList());
    }

    private static native long nGetWindowDrawList();

    public static float getWindowDpiScale() {
        return ImGui.nGetWindowDpiScale();
    }

    private static native float nGetWindowDpiScale();

    public static ImVec2 getWindowPos() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetWindowPos(dst);
        return dst;
    }

    public static float getWindowPosX() {
        return ImGui.nGetWindowPosX();
    }

    public static float getWindowPosY() {
        return ImGui.nGetWindowPosY();
    }

    public static void getWindowPos(ImVec2 dst) {
        ImGui.nGetWindowPos(dst);
    }

    private static native void nGetWindowPos(ImVec2 var0);

    private static native float nGetWindowPosX();

    private static native float nGetWindowPosY();

    public static ImVec2 getWindowSize() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetWindowSize(dst);
        return dst;
    }

    public static float getWindowSizeX() {
        return ImGui.nGetWindowSizeX();
    }

    public static float getWindowSizeY() {
        return ImGui.nGetWindowSizeY();
    }

    public static void getWindowSize(ImVec2 dst) {
        ImGui.nGetWindowSize(dst);
    }

    private static native void nGetWindowSize(ImVec2 var0);

    private static native float nGetWindowSizeX();

    private static native float nGetWindowSizeY();

    public static float getWindowWidth() {
        return ImGui.nGetWindowWidth();
    }

    private static native float nGetWindowWidth();

    public static float getWindowHeight() {
        return ImGui.nGetWindowHeight();
    }

    private static native float nGetWindowHeight();

    public static ImGuiViewport getWindowViewport() {
        return new ImGuiViewport(ImGui.nGetWindowViewport());
    }

    private static native long nGetWindowViewport();

    public static void setNextWindowPos(ImVec2 pos) {
        ImGui.nSetNextWindowPos(pos.x, pos.y);
    }

    public static void setNextWindowPos(float posX, float posY) {
        ImGui.nSetNextWindowPos(posX, posY);
    }

    public static void setNextWindowPos(ImVec2 pos, int cond) {
        ImGui.nSetNextWindowPos(pos.x, pos.y, cond);
    }

    public static void setNextWindowPos(float posX, float posY, int cond) {
        ImGui.nSetNextWindowPos(posX, posY, cond);
    }

    public static void setNextWindowPos(ImVec2 pos, int cond, ImVec2 pivot) {
        ImGui.nSetNextWindowPos(pos.x, pos.y, cond, pivot.x, pivot.y);
    }

    public static void setNextWindowPos(float posX, float posY, int cond, float pivotX, float pivotY) {
        ImGui.nSetNextWindowPos(posX, posY, cond, pivotX, pivotY);
    }

    public static void setNextWindowPos(ImVec2 pos, ImVec2 pivot) {
        ImGui.nSetNextWindowPos(pos.x, pos.y, pivot.x, pivot.y);
    }

    public static void setNextWindowPos(float posX, float posY, float pivotX, float pivotY) {
        ImGui.nSetNextWindowPos(posX, posY, pivotX, pivotY);
    }

    private static native void nSetNextWindowPos(float var0, float var1);

    private static native void nSetNextWindowPos(float var0, float var1, int var2);

    private static native void nSetNextWindowPos(float var0, float var1, int var2, float var3, float var4);

    private static native void nSetNextWindowPos(float var0, float var1, float var2, float var3);

    public static void setNextWindowSize(ImVec2 size) {
        ImGui.nSetNextWindowSize(size.x, size.y);
    }

    public static void setNextWindowSize(float sizeX, float sizeY) {
        ImGui.nSetNextWindowSize(sizeX, sizeY);
    }

    public static void setNextWindowSize(ImVec2 size, int cond) {
        ImGui.nSetNextWindowSize(size.x, size.y, cond);
    }

    public static void setNextWindowSize(float sizeX, float sizeY, int cond) {
        ImGui.nSetNextWindowSize(sizeX, sizeY, cond);
    }

    private static native void nSetNextWindowSize(float var0, float var1);

    private static native void nSetNextWindowSize(float var0, float var1, int var2);

    public static void setNextWindowSizeConstraints(ImVec2 sizeMin, ImVec2 sizeMax) {
        ImGui.nSetNextWindowSizeConstraints(sizeMin.x, sizeMin.y, sizeMax.x, sizeMax.y);
    }

    public static void setNextWindowSizeConstraints(float sizeMinX, float sizeMinY, float sizeMaxX, float sizeMaxY) {
        ImGui.nSetNextWindowSizeConstraints(sizeMinX, sizeMinY, sizeMaxX, sizeMaxY);
    }

    private static native void nSetNextWindowSizeConstraints(float var0, float var1, float var2, float var3);

    public static void setNextWindowContentSize(ImVec2 size) {
        ImGui.nSetNextWindowContentSize(size.x, size.y);
    }

    public static void setNextWindowContentSize(float sizeX, float sizeY) {
        ImGui.nSetNextWindowContentSize(sizeX, sizeY);
    }

    private static native void nSetNextWindowContentSize(float var0, float var1);

    public static void setNextWindowCollapsed(boolean collapsed) {
        ImGui.nSetNextWindowCollapsed(collapsed);
    }

    public static void setNextWindowCollapsed(boolean collapsed, int cond) {
        ImGui.nSetNextWindowCollapsed(collapsed, cond);
    }

    private static native void nSetNextWindowCollapsed(boolean var0);

    private static native void nSetNextWindowCollapsed(boolean var0, int var1);

    public static void setNextWindowFocus() {
        ImGui.nSetNextWindowFocus();
    }

    private static native void nSetNextWindowFocus();

    public static void setNextWindowScroll(ImVec2 scroll) {
        ImGui.nSetNextWindowScroll(scroll.x, scroll.y);
    }

    public static void setNextWindowScroll(float scrollX, float scrollY) {
        ImGui.nSetNextWindowScroll(scrollX, scrollY);
    }

    private static native void nSetNextWindowScroll(float var0, float var1);

    public static void setNextWindowBgAlpha(float alpha) {
        ImGui.nSetNextWindowBgAlpha(alpha);
    }

    private static native void nSetNextWindowBgAlpha(float var0);

    public static void setNextWindowViewport(int viewportId) {
        ImGui.nSetNextWindowViewport(viewportId);
    }

    private static native void nSetNextWindowViewport(int var0);

    public static void setWindowPos(ImVec2 pos) {
        ImGui.nSetWindowPos(pos.x, pos.y);
    }

    public static void setWindowPos(float posX, float posY) {
        ImGui.nSetWindowPos(posX, posY);
    }

    public static void setWindowPos(ImVec2 pos, int cond) {
        ImGui.nSetWindowPos(pos.x, pos.y, cond);
    }

    public static void setWindowPos(float posX, float posY, int cond) {
        ImGui.nSetWindowPos(posX, posY, cond);
    }

    private static native void nSetWindowPos(float var0, float var1);

    private static native void nSetWindowPos(float var0, float var1, int var2);

    public static void setWindowSize(ImVec2 size) {
        ImGui.nSetWindowSize(size.x, size.y);
    }

    public static void setWindowSize(float sizeX, float sizeY) {
        ImGui.nSetWindowSize(sizeX, sizeY);
    }

    public static void setWindowSize(ImVec2 size, int cond) {
        ImGui.nSetWindowSize(size.x, size.y, cond);
    }

    public static void setWindowSize(float sizeX, float sizeY, int cond) {
        ImGui.nSetWindowSize(sizeX, sizeY, cond);
    }

    private static native void nSetWindowSize(float var0, float var1);

    private static native void nSetWindowSize(float var0, float var1, int var2);

    public static void setWindowCollapsed(boolean collapsed) {
        ImGui.nSetWindowCollapsed(collapsed);
    }

    public static void setWindowCollapsed(boolean collapsed, int cond) {
        ImGui.nSetWindowCollapsed(collapsed, cond);
    }

    private static native void nSetWindowCollapsed(boolean var0);

    private static native void nSetWindowCollapsed(boolean var0, int var1);

    public static void setWindowFocus() {
        ImGui.nSetWindowFocus();
    }

    private static native void nSetWindowFocus();

    public static void setWindowFontScale(float scale) {
        ImGui.nSetWindowFontScale(scale);
    }

    private static native void nSetWindowFontScale(float var0);

    public static void setWindowPos(String name, ImVec2 pos) {
        ImGui.nSetWindowPos(name, pos.x, pos.y);
    }

    public static void setWindowPos(String name, float posX, float posY) {
        ImGui.nSetWindowPos(name, posX, posY);
    }

    public static void setWindowPos(String name, ImVec2 pos, int cond) {
        ImGui.nSetWindowPos(name, pos.x, pos.y, cond);
    }

    public static void setWindowPos(String name, float posX, float posY, int cond) {
        ImGui.nSetWindowPos(name, posX, posY, cond);
    }

    private static native void nSetWindowPos(String var0, float var1, float var2);

    private static native void nSetWindowPos(String var0, float var1, float var2, int var3);

    public static void setWindowSize(String name, ImVec2 size) {
        ImGui.nSetWindowSize(name, size.x, size.y);
    }

    public static void setWindowSize(String name, float sizeX, float sizeY) {
        ImGui.nSetWindowSize(name, sizeX, sizeY);
    }

    public static void setWindowSize(String name, ImVec2 size, int cond) {
        ImGui.nSetWindowSize(name, size.x, size.y, cond);
    }

    public static void setWindowSize(String name, float sizeX, float sizeY, int cond) {
        ImGui.nSetWindowSize(name, sizeX, sizeY, cond);
    }

    private static native void nSetWindowSize(String var0, float var1, float var2);

    private static native void nSetWindowSize(String var0, float var1, float var2, int var3);

    public static void setWindowCollapsed(String name, boolean collapsed) {
        ImGui.nSetWindowCollapsed(name, collapsed);
    }

    public static void setWindowCollapsed(String name, boolean collapsed, int cond) {
        ImGui.nSetWindowCollapsed(name, collapsed, cond);
    }

    private static native void nSetWindowCollapsed(String var0, boolean var1);

    private static native void nSetWindowCollapsed(String var0, boolean var1, int var2);

    public static void setWindowFocus(String name) {
        ImGui.nSetWindowFocus(name);
    }

    private static native void nSetWindowFocus(String var0);

    public static ImVec2 getContentRegionAvail() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetContentRegionAvail(dst);
        return dst;
    }

    public static float getContentRegionAvailX() {
        return ImGui.nGetContentRegionAvailX();
    }

    public static float getContentRegionAvailY() {
        return ImGui.nGetContentRegionAvailY();
    }

    public static void getContentRegionAvail(ImVec2 dst) {
        ImGui.nGetContentRegionAvail(dst);
    }

    private static native void nGetContentRegionAvail(ImVec2 var0);

    private static native float nGetContentRegionAvailX();

    private static native float nGetContentRegionAvailY();

    public static ImVec2 getContentRegionMax() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetContentRegionMax(dst);
        return dst;
    }

    public static float getContentRegionMaxX() {
        return ImGui.nGetContentRegionMaxX();
    }

    public static float getContentRegionMaxY() {
        return ImGui.nGetContentRegionMaxY();
    }

    public static void getContentRegionMax(ImVec2 dst) {
        ImGui.nGetContentRegionMax(dst);
    }

    private static native void nGetContentRegionMax(ImVec2 var0);

    private static native float nGetContentRegionMaxX();

    private static native float nGetContentRegionMaxY();

    public static ImVec2 getWindowContentRegionMin() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetWindowContentRegionMin(dst);
        return dst;
    }

    public static float getWindowContentRegionMinX() {
        return ImGui.nGetWindowContentRegionMinX();
    }

    public static float getWindowContentRegionMinY() {
        return ImGui.nGetWindowContentRegionMinY();
    }

    public static void getWindowContentRegionMin(ImVec2 dst) {
        ImGui.nGetWindowContentRegionMin(dst);
    }

    private static native void nGetWindowContentRegionMin(ImVec2 var0);

    private static native float nGetWindowContentRegionMinX();

    private static native float nGetWindowContentRegionMinY();

    public static ImVec2 getWindowContentRegionMax() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetWindowContentRegionMax(dst);
        return dst;
    }

    public static float getWindowContentRegionMaxX() {
        return ImGui.nGetWindowContentRegionMaxX();
    }

    public static float getWindowContentRegionMaxY() {
        return ImGui.nGetWindowContentRegionMaxY();
    }

    public static void getWindowContentRegionMax(ImVec2 dst) {
        ImGui.nGetWindowContentRegionMax(dst);
    }

    private static native void nGetWindowContentRegionMax(ImVec2 var0);

    private static native float nGetWindowContentRegionMaxX();

    private static native float nGetWindowContentRegionMaxY();

    public static float getScrollX() {
        return ImGui.nGetScrollX();
    }

    private static native float nGetScrollX();

    public static float getScrollY() {
        return ImGui.nGetScrollY();
    }

    private static native float nGetScrollY();

    public static void setScrollX(float scrollX) {
        ImGui.nSetScrollX(scrollX);
    }

    private static native void nSetScrollX(float var0);

    public static void setScrollY(float scrollY) {
        ImGui.nSetScrollY(scrollY);
    }

    private static native void nSetScrollY(float var0);

    public static float getScrollMaxX() {
        return ImGui.nGetScrollMaxX();
    }

    private static native float nGetScrollMaxX();

    public static float getScrollMaxY() {
        return ImGui.nGetScrollMaxY();
    }

    private static native float nGetScrollMaxY();

    public static void setScrollHereX() {
        ImGui.nSetScrollHereX();
    }

    public static void setScrollHereX(float centerXRatio) {
        ImGui.nSetScrollHereX(centerXRatio);
    }

    private static native void nSetScrollHereX();

    private static native void nSetScrollHereX(float var0);

    public static void setScrollHereY() {
        ImGui.nSetScrollHereY();
    }

    public static void setScrollHereY(float centerYRatio) {
        ImGui.nSetScrollHereY(centerYRatio);
    }

    private static native void nSetScrollHereY();

    private static native void nSetScrollHereY(float var0);

    public static void setScrollFromPosX(float localX) {
        ImGui.nSetScrollFromPosX(localX);
    }

    public static void setScrollFromPosX(float localX, float centerXRatio) {
        ImGui.nSetScrollFromPosX(localX, centerXRatio);
    }

    private static native void nSetScrollFromPosX(float var0);

    private static native void nSetScrollFromPosX(float var0, float var1);

    public static void setScrollFromPosY(float localY) {
        ImGui.nSetScrollFromPosY(localY);
    }

    public static void setScrollFromPosY(float localY, float centerYRatio) {
        ImGui.nSetScrollFromPosY(localY, centerYRatio);
    }

    private static native void nSetScrollFromPosY(float var0);

    private static native void nSetScrollFromPosY(float var0, float var1);

    public static void pushFont(ImFont font) {
        ImGui.nPushFont(font.ptr);
    }

    private static native void nPushFont(long var0);

    public static void popFont() {
        ImGui.nPopFont();
    }

    private static native void nPopFont();

    public static native void pushStyleColor(int var0, int var1, int var2, int var3, int var4);

    public static void pushStyleColor(int imGuiCol, ImVec4 col) {
        ImGui.nPushStyleColor(imGuiCol, col.x, col.y, col.z, col.w);
    }

    public static void pushStyleColor(int imGuiCol, float colX, float colY, float colZ, float colW) {
        ImGui.nPushStyleColor(imGuiCol, colX, colY, colZ, colW);
    }

    private static native void nPushStyleColor(int var0, float var1, float var2, float var3, float var4);

    public static void pushStyleColor(int imGuiCol, int col) {
        ImGui.nPushStyleColor(imGuiCol, col);
    }

    private static native void nPushStyleColor(int var0, int var1);

    public static void popStyleColor() {
        ImGui.nPopStyleColor();
    }

    public static void popStyleColor(int count) {
        ImGui.nPopStyleColor(count);
    }

    private static native void nPopStyleColor();

    private static native void nPopStyleColor(int var0);

    public static void pushStyleVar(int imGuiStyleVar, float val) {
        ImGui.nPushStyleVar(imGuiStyleVar, val);
    }

    private static native void nPushStyleVar(int var0, float var1);

    public static void pushStyleVar(int imGuiStyleVar, ImVec2 val) {
        ImGui.nPushStyleVar(imGuiStyleVar, val.x, val.y);
    }

    public static void pushStyleVar(int imGuiStyleVar, float valX, float valY) {
        ImGui.nPushStyleVar(imGuiStyleVar, valX, valY);
    }

    private static native void nPushStyleVar(int var0, float var1, float var2);

    public static void popStyleVar() {
        ImGui.nPopStyleVar();
    }

    public static void popStyleVar(int count) {
        ImGui.nPopStyleVar(count);
    }

    private static native void nPopStyleVar();

    private static native void nPopStyleVar(int var0);

    public static void pushTabStop(boolean tabStop) {
        ImGui.nPushTabStop(tabStop);
    }

    private static native void nPushTabStop(boolean var0);

    public static void popTabStop() {
        ImGui.nPopTabStop();
    }

    private static native void nPopTabStop();

    public static void pushButtonRepeat(boolean repeat) {
        ImGui.nPushButtonRepeat(repeat);
    }

    private static native void nPushButtonRepeat(boolean var0);

    public static void popButtonRepeat() {
        ImGui.nPopButtonRepeat();
    }

    private static native void nPopButtonRepeat();

    public static void pushItemWidth(float itemWidth) {
        ImGui.nPushItemWidth(itemWidth);
    }

    private static native void nPushItemWidth(float var0);

    public static void popItemWidth() {
        ImGui.nPopItemWidth();
    }

    private static native void nPopItemWidth();

    public static void setNextItemWidth(float itemWidth) {
        ImGui.nSetNextItemWidth(itemWidth);
    }

    private static native void nSetNextItemWidth(float var0);

    public static float calcItemWidth() {
        return ImGui.nCalcItemWidth();
    }

    private static native float nCalcItemWidth();

    public static void pushTextWrapPos() {
        ImGui.nPushTextWrapPos();
    }

    public static void pushTextWrapPos(float wrapLocalPosX) {
        ImGui.nPushTextWrapPos(wrapLocalPosX);
    }

    private static native void nPushTextWrapPos();

    private static native void nPushTextWrapPos(float var0);

    public static void popTextWrapPos() {
        ImGui.nPopTextWrapPos();
    }

    private static native void nPopTextWrapPos();

    public static ImFont getFont() {
        ImGui._GETFONT_1.ptr = ImGui.nGetFont();
        return _GETFONT_1;
    }

    private static native long nGetFont();

    public static int getFontSize() {
        return ImGui.nGetFontSize();
    }

    private static native int nGetFontSize();

    public static ImVec2 getFontTexUvWhitePixel() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetFontTexUvWhitePixel(dst);
        return dst;
    }

    public static float getFontTexUvWhitePixelX() {
        return ImGui.nGetFontTexUvWhitePixelX();
    }

    public static float getFontTexUvWhitePixelY() {
        return ImGui.nGetFontTexUvWhitePixelY();
    }

    public static void getFontTexUvWhitePixel(ImVec2 dst) {
        ImGui.nGetFontTexUvWhitePixel(dst);
    }

    private static native void nGetFontTexUvWhitePixel(ImVec2 var0);

    private static native float nGetFontTexUvWhitePixelX();

    private static native float nGetFontTexUvWhitePixelY();

    public static int getColorU32(int idx) {
        return ImGui.nGetColorU32(idx);
    }

    public static int getColorU32(int idx, float alphaMul) {
        return ImGui.nGetColorU32(idx, alphaMul);
    }

    private static native int nGetColorU32(int var0);

    private static native int nGetColorU32(int var0, float var1);

    public static int getColorU32(ImVec4 col) {
        return ImGui.nGetColorU32(col.x, col.y, col.z, col.w);
    }

    public static int getColorU32(float colX, float colY, float colZ, float colW) {
        return ImGui.nGetColorU32(colX, colY, colZ, colW);
    }

    private static native int nGetColorU32(float var0, float var1, float var2, float var3);

    public static int getColorU32i(int col) {
        return ImGui.nGetColorU32i(col);
    }

    private static native int nGetColorU32i(int var0);

    public static ImVec4 getStyleColorVec4(int imGuiColIdx) {
        ImVec4 dst = new ImVec4();
        ImGui.nGetStyleColorVec4(dst, imGuiColIdx);
        return dst;
    }

    public static float getStyleColorVec4X(int imGuiColIdx) {
        return ImGui.nGetStyleColorVec4X(imGuiColIdx);
    }

    public static float getStyleColorVec4Y(int imGuiColIdx) {
        return ImGui.nGetStyleColorVec4Y(imGuiColIdx);
    }

    public static float getStyleColorVec4Z(int imGuiColIdx) {
        return ImGui.nGetStyleColorVec4Z(imGuiColIdx);
    }

    public static float getStyleColorVec4W(int imGuiColIdx) {
        return ImGui.nGetStyleColorVec4W(imGuiColIdx);
    }

    public static void getStyleColorVec4(ImVec4 dst, int imGuiColIdx) {
        ImGui.nGetStyleColorVec4(dst, imGuiColIdx);
    }

    private static native void nGetStyleColorVec4(ImVec4 var0, int var1);

    private static native float nGetStyleColorVec4X(int var0);

    private static native float nGetStyleColorVec4Y(int var0);

    private static native float nGetStyleColorVec4Z(int var0);

    private static native float nGetStyleColorVec4W(int var0);

    public static void separator() {
        ImGui.nSeparator();
    }

    private static native void nSeparator();

    public static void sameLine() {
        ImGui.nSameLine();
    }

    public static void sameLine(float offsetFromStartX) {
        ImGui.nSameLine(offsetFromStartX);
    }

    public static void sameLine(float offsetFromStartX, float spacing) {
        ImGui.nSameLine(offsetFromStartX, spacing);
    }

    private static native void nSameLine();

    private static native void nSameLine(float var0);

    private static native void nSameLine(float var0, float var1);

    public static void newLine() {
        ImGui.nNewLine();
    }

    private static native void nNewLine();

    public static void spacing() {
        ImGui.nSpacing();
    }

    private static native void nSpacing();

    public static void dummy(ImVec2 size) {
        ImGui.nDummy(size.x, size.y);
    }

    public static void dummy(float sizeX, float sizeY) {
        ImGui.nDummy(sizeX, sizeY);
    }

    private static native void nDummy(float var0, float var1);

    public static void indent() {
        ImGui.nIndent();
    }

    public static void indent(float indentW) {
        ImGui.nIndent(indentW);
    }

    private static native void nIndent();

    private static native void nIndent(float var0);

    public static void unindent() {
        ImGui.nUnindent();
    }

    public static void unindent(float indentW) {
        ImGui.nUnindent(indentW);
    }

    private static native void nUnindent();

    private static native void nUnindent(float var0);

    public static void beginGroup() {
        ImGui.nBeginGroup();
    }

    private static native void nBeginGroup();

    public static void endGroup() {
        ImGui.nEndGroup();
    }

    private static native void nEndGroup();

    public static ImVec2 getCursorPos() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetCursorPos(dst);
        return dst;
    }

    public static float getCursorPosX() {
        return ImGui.nGetCursorPosX();
    }

    public static float getCursorPosY() {
        return ImGui.nGetCursorPosY();
    }

    public static void getCursorPos(ImVec2 dst) {
        ImGui.nGetCursorPos(dst);
    }

    private static native void nGetCursorPos(ImVec2 var0);

    private static native float nGetCursorPosX();

    private static native float nGetCursorPosY();

    public static void setCursorPos(ImVec2 pos) {
        ImGui.nSetCursorPos(pos.x, pos.y);
    }

    public static void setCursorPos(float posX, float posY) {
        ImGui.nSetCursorPos(posX, posY);
    }

    private static native void nSetCursorPos(float var0, float var1);

    public static void setCursorPosX(float x) {
        ImGui.nSetCursorPosX(x);
    }

    private static native void nSetCursorPosX(float var0);

    public static void setCursorPosY(float y) {
        ImGui.nSetCursorPosY(y);
    }

    private static native void nSetCursorPosY(float var0);

    public static ImVec2 getCursorStartPos() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetCursorStartPos(dst);
        return dst;
    }

    public static float getCursorStartPosX() {
        return ImGui.nGetCursorStartPosX();
    }

    public static float getCursorStartPosY() {
        return ImGui.nGetCursorStartPosY();
    }

    public static void getCursorStartPos(ImVec2 dst) {
        ImGui.nGetCursorStartPos(dst);
    }

    private static native void nGetCursorStartPos(ImVec2 var0);

    private static native float nGetCursorStartPosX();

    private static native float nGetCursorStartPosY();

    public static ImVec2 getCursorScreenPos() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetCursorScreenPos(dst);
        return dst;
    }

    public static float getCursorScreenPosX() {
        return ImGui.nGetCursorScreenPosX();
    }

    public static float getCursorScreenPosY() {
        return ImGui.nGetCursorScreenPosY();
    }

    public static void getCursorScreenPos(ImVec2 dst) {
        ImGui.nGetCursorScreenPos(dst);
    }

    private static native void nGetCursorScreenPos(ImVec2 var0);

    private static native float nGetCursorScreenPosX();

    private static native float nGetCursorScreenPosY();

    public static void setCursorScreenPos(ImVec2 pos) {
        ImGui.nSetCursorScreenPos(pos.x, pos.y);
    }

    public static void setCursorScreenPos(float posX, float posY) {
        ImGui.nSetCursorScreenPos(posX, posY);
    }

    private static native void nSetCursorScreenPos(float var0, float var1);

    public static void alignTextToFramePadding() {
        ImGui.nAlignTextToFramePadding();
    }

    private static native void nAlignTextToFramePadding();

    public static float getTextLineHeight() {
        return ImGui.nGetTextLineHeight();
    }

    private static native float nGetTextLineHeight();

    public static float getTextLineHeightWithSpacing() {
        return ImGui.nGetTextLineHeightWithSpacing();
    }

    private static native float nGetTextLineHeightWithSpacing();

    public static float getFrameHeight() {
        return ImGui.nGetFrameHeight();
    }

    private static native float nGetFrameHeight();

    public static float getFrameHeightWithSpacing() {
        return ImGui.nGetFrameHeightWithSpacing();
    }

    private static native float nGetFrameHeightWithSpacing();

    public static void pushID(String strId) {
        ImGui.nPushID(strId);
    }

    private static native void nPushID(String var0);

    public static void pushID(String strIdBegin, String strIdEnd) {
        ImGui.nPushID(strIdBegin, strIdEnd);
    }

    private static native void nPushID(String var0, String var1);

    public static void pushID(long ptrId) {
        ImGui.nPushID(ptrId);
    }

    private static native void nPushID(long var0);

    public static void pushID(int intId) {
        ImGui.nPushID(intId);
    }

    private static native void nPushID(int var0);

    public static void popID() {
        ImGui.nPopID();
    }

    private static native void nPopID();

    public static int getID(String strId) {
        return ImGui.nGetID(strId);
    }

    private static native int nGetID(String var0);

    public static int getID(String strIdBegin, String strIdEnd) {
        return ImGui.nGetID(strIdBegin, strIdEnd);
    }

    private static native int nGetID(String var0, String var1);

    public static int getID(long ptrId) {
        return ImGui.nGetID(ptrId);
    }

    private static native int nGetID(long var0);

    public static void textUnformatted(String text) {
        ImGui.nTextUnformatted(text);
    }

    public static void textUnformatted(String text, String textEnd) {
        ImGui.nTextUnformatted(text, textEnd);
    }

    private static native void nTextUnformatted(String var0);

    private static native void nTextUnformatted(String var0, String var1);

    public static void text(String text) {
        ImGui.nText(text);
    }

    private static native void nText(String var0);

    public static void textColored(ImVec4 col, String text) {
        ImGui.nTextColored(col.x, col.y, col.z, col.w, text);
    }

    public static void textColored(float colX, float colY, float colZ, float colW, String text) {
        ImGui.nTextColored(colX, colY, colZ, colW, text);
    }

    private static native void nTextColored(float var0, float var1, float var2, float var3, String var4);

    public static native void textColored(int var0, int var1, int var2, int var3, String var4);

    public static native void textColored(int var0, String var1);

    public static void textDisabled(String text) {
        ImGui.nTextDisabled(text);
    }

    private static native void nTextDisabled(String var0);

    public static void textWrapped(String text) {
        ImGui.nTextWrapped(text);
    }

    private static native void nTextWrapped(String var0);

    public static void labelText(String label, String text) {
        ImGui.nLabelText(label, text);
    }

    private static native void nLabelText(String var0, String var1);

    public static void bulletText(String text) {
        ImGui.nBulletText(text);
    }

    private static native void nBulletText(String var0);

    public static void separatorText(String label) {
        ImGui.nSeparatorText(label);
    }

    private static native void nSeparatorText(String var0);

    public static boolean button(String label) {
        return ImGui.nButton(label);
    }

    public static boolean button(String label, ImVec2 size) {
        return ImGui.nButton(label, size.x, size.y);
    }

    public static boolean button(String label, float sizeX, float sizeY) {
        return ImGui.nButton(label, sizeX, sizeY);
    }

    private static native boolean nButton(String var0);

    private static native boolean nButton(String var0, float var1, float var2);

    public static boolean smallButton(String label) {
        return ImGui.nSmallButton(label);
    }

    private static native boolean nSmallButton(String var0);

    public static boolean invisibleButton(String strId, ImVec2 size) {
        return ImGui.nInvisibleButton(strId, size.x, size.y);
    }

    public static boolean invisibleButton(String strId, float sizeX, float sizeY) {
        return ImGui.nInvisibleButton(strId, sizeX, sizeY);
    }

    public static boolean invisibleButton(String strId, ImVec2 size, int imGuiButtonFlags) {
        return ImGui.nInvisibleButton(strId, size.x, size.y, imGuiButtonFlags);
    }

    public static boolean invisibleButton(String strId, float sizeX, float sizeY, int imGuiButtonFlags) {
        return ImGui.nInvisibleButton(strId, sizeX, sizeY, imGuiButtonFlags);
    }

    private static native boolean nInvisibleButton(String var0, float var1, float var2);

    private static native boolean nInvisibleButton(String var0, float var1, float var2, int var3);

    public static boolean arrowButton(String strId, int dir) {
        return ImGui.nArrowButton(strId, dir);
    }

    private static native boolean nArrowButton(String var0, int var1);

    public static boolean checkbox(String label, boolean active) {
        return ImGui.nCheckbox(label, active);
    }

    private static native boolean nCheckbox(String var0, boolean var1);

    public static boolean checkbox(String label, ImBoolean data) {
        return ImGui.nCheckbox(label, data != null ? data.getData() : null);
    }

    private static native boolean nCheckbox(String var0, boolean[] var1);

    public static boolean checkboxFlags(String label, ImInt flags, int flagsValue) {
        return ImGui.nCheckboxFlags(label, flags != null ? flags.getData() : null, flagsValue);
    }

    private static native boolean nCheckboxFlags(String var0, int[] var1, int var2);

    public static boolean radioButton(String label, boolean active) {
        return ImGui.nRadioButton(label, active);
    }

    private static native boolean nRadioButton(String var0, boolean var1);

    public static boolean radioButton(String label, ImInt v, int vButton) {
        return ImGui.nRadioButton(label, v != null ? v.getData() : null, vButton);
    }

    private static native boolean nRadioButton(String var0, int[] var1, int var2);

    public static void progressBar(float fraction) {
        ImGui.nProgressBar(fraction);
    }

    public static void progressBar(float fraction, ImVec2 size) {
        ImGui.nProgressBar(fraction, size.x, size.y);
    }

    public static void progressBar(float fraction, float sizeX, float sizeY) {
        ImGui.nProgressBar(fraction, sizeX, sizeY);
    }

    public static void progressBar(float fraction, ImVec2 size, String overlay) {
        ImGui.nProgressBar(fraction, size.x, size.y, overlay);
    }

    public static void progressBar(float fraction, float sizeX, float sizeY, String overlay) {
        ImGui.nProgressBar(fraction, sizeX, sizeY, overlay);
    }

    public static void progressBar(float fraction, String overlay) {
        ImGui.nProgressBar(fraction, overlay);
    }

    private static native void nProgressBar(float var0);

    private static native void nProgressBar(float var0, float var1, float var2);

    private static native void nProgressBar(float var0, float var1, float var2, String var3);

    private static native void nProgressBar(float var0, String var1);

    public static void bullet() {
        ImGui.nBullet();
    }

    private static native void nBullet();

    public static void image(long userTextureId, ImVec2 size) {
        ImGui.nImage(userTextureId, size.x, size.y);
    }

    public static void image(long userTextureId, float sizeX, float sizeY) {
        ImGui.nImage(userTextureId, sizeX, sizeY);
    }

    public static void image(long userTextureId, ImVec2 size, ImVec2 uv0) {
        ImGui.nImage(userTextureId, size.x, size.y, uv0.x, uv0.y);
    }

    public static void image(long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y) {
        ImGui.nImage(userTextureId, sizeX, sizeY, uv0X, uv0Y);
    }

    public static void image(long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1) {
        ImGui.nImage(userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y);
    }

    public static void image(long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y) {
        ImGui.nImage(userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y);
    }

    public static void image(long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1, ImVec4 tintCol) {
        ImGui.nImage(userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, tintCol.x, tintCol.y, tintCol.z, tintCol.w);
    }

    public static void image(long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y, float tintColX, float tintColY, float tintColZ, float tintColW) {
        ImGui.nImage(userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y, tintColX, tintColY, tintColZ, tintColW);
    }

    public static void image(long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1, ImVec4 tintCol, ImVec4 borderCol) {
        ImGui.nImage(userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, tintCol.x, tintCol.y, tintCol.z, tintCol.w, borderCol.x, borderCol.y, borderCol.z, borderCol.w);
    }

    public static void image(long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y, float tintColX, float tintColY, float tintColZ, float tintColW, float borderColX, float borderColY, float borderColZ, float borderColW) {
        ImGui.nImage(userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y, tintColX, tintColY, tintColZ, tintColW, borderColX, borderColY, borderColZ, borderColW);
    }

    private static native void nImage(long var0, float var2, float var3);

    private static native void nImage(long var0, float var2, float var3, float var4, float var5);

    private static native void nImage(long var0, float var2, float var3, float var4, float var5, float var6, float var7);

    private static native void nImage(long var0, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11);

    private static native void nImage(long var0, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15);

    public static boolean imageButton(String strId, long userTextureId, ImVec2 size) {
        return ImGui.nImageButton(strId, userTextureId, size.x, size.y);
    }

    public static boolean imageButton(String strId, long userTextureId, float sizeX, float sizeY) {
        return ImGui.nImageButton(strId, userTextureId, sizeX, sizeY);
    }

    public static boolean imageButton(String strId, long userTextureId, ImVec2 size, ImVec2 uv0) {
        return ImGui.nImageButton(strId, userTextureId, size.x, size.y, uv0.x, uv0.y);
    }

    public static boolean imageButton(String strId, long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y) {
        return ImGui.nImageButton(strId, userTextureId, sizeX, sizeY, uv0X, uv0Y);
    }

    public static boolean imageButton(String strId, long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1) {
        return ImGui.nImageButton(strId, userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y);
    }

    public static boolean imageButton(String strId, long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y) {
        return ImGui.nImageButton(strId, userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y);
    }

    public static boolean imageButton(String strId, long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1, ImVec4 bgCol) {
        return ImGui.nImageButton(strId, userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, bgCol.x, bgCol.y, bgCol.z, bgCol.w);
    }

    public static boolean imageButton(String strId, long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y, float bgColX, float bgColY, float bgColZ, float bgColW) {
        return ImGui.nImageButton(strId, userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y, bgColX, bgColY, bgColZ, bgColW);
    }

    public static boolean imageButton(String strId, long userTextureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1, ImVec4 bgCol, ImVec4 tintCol) {
        return ImGui.nImageButton(strId, userTextureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, bgCol.x, bgCol.y, bgCol.z, bgCol.w, tintCol.x, tintCol.y, tintCol.z, tintCol.w);
    }

    public static boolean imageButton(String strId, long userTextureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y, float bgColX, float bgColY, float bgColZ, float bgColW, float tintColX, float tintColY, float tintColZ, float tintColW) {
        return ImGui.nImageButton(strId, userTextureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y, bgColX, bgColY, bgColZ, bgColW, tintColX, tintColY, tintColZ, tintColW);
    }

    private static native boolean nImageButton(String var0, long var1, float var3, float var4);

    private static native boolean nImageButton(String var0, long var1, float var3, float var4, float var5, float var6);

    private static native boolean nImageButton(String var0, long var1, float var3, float var4, float var5, float var6, float var7, float var8);

    private static native boolean nImageButton(String var0, long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12);

    private static native boolean nImageButton(String var0, long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16);

    public static boolean beginCombo(String label, String previewValue) {
        return ImGui.nBeginCombo(label, previewValue);
    }

    public static boolean beginCombo(String label, String previewValue, int imGuiComboFlags) {
        return ImGui.nBeginCombo(label, previewValue, imGuiComboFlags);
    }

    private static native boolean nBeginCombo(String var0, String var1);

    private static native boolean nBeginCombo(String var0, String var1, int var2);

    public static void endCombo() {
        ImGui.nEndCombo();
    }

    private static native void nEndCombo();

    public static boolean combo(String label, ImInt currentItem, String[] items) {
        return ImGui.nCombo(label, currentItem != null ? currentItem.getData() : null, items, items.length);
    }

    public static boolean combo(String label, ImInt currentItem, String[] items, int popupMaxHeightInItems) {
        return ImGui.nCombo(label, currentItem != null ? currentItem.getData() : null, items, items.length, popupMaxHeightInItems);
    }

    private static native boolean nCombo(String var0, int[] var1, String[] var2, int var3);

    private static native boolean nCombo(String var0, int[] var1, String[] var2, int var3, int var4);

    public static boolean combo(String label, ImInt currentItem, String itemsSeparatedByZeros) {
        return ImGui.nCombo(label, currentItem != null ? currentItem.getData() : null, itemsSeparatedByZeros);
    }

    public static boolean combo(String label, ImInt currentItem, String itemsSeparatedByZeros, int popupMaxHeightInItems) {
        return ImGui.nCombo(label, currentItem != null ? currentItem.getData() : null, itemsSeparatedByZeros, popupMaxHeightInItems);
    }

    private static native boolean nCombo(String var0, int[] var1, String var2);

    private static native boolean nCombo(String var0, int[] var1, String var2, int var3);

    public static boolean dragFloat(String label, float[] v) {
        return ImGui.nDragFloat(label, v);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed) {
        return ImGui.nDragFloat(label, v, vSpeed);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed, float vMin) {
        return ImGui.nDragFloat(label, v, vSpeed, vMin);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed, float vMin, float vMax) {
        return ImGui.nDragFloat(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed, float vMin, float vMax, String format) {
        return ImGui.nDragFloat(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragFloat(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragFloat(String label, float[] v, float vSpeed, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nDragFloat(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragFloat(String var0, float[] var1);

    private static native boolean nDragFloat(String var0, float[] var1, float var2);

    private static native boolean nDragFloat(String var0, float[] var1, float var2, float var3);

    private static native boolean nDragFloat(String var0, float[] var1, float var2, float var3, float var4);

    private static native boolean nDragFloat(String var0, float[] var1, float var2, float var3, float var4, String var5);

    private static native boolean nDragFloat(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    private static native boolean nDragFloat(String var0, float[] var1, float var2, float var3, float var4, int var5);

    public static boolean dragFloat2(String label, float[] v) {
        return ImGui.nDragFloat2(label, v);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed) {
        return ImGui.nDragFloat2(label, v, vSpeed);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed, float vMin) {
        return ImGui.nDragFloat2(label, v, vSpeed, vMin);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed, float vMin, float vMax) {
        return ImGui.nDragFloat2(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed, float vMin, float vMax, String format) {
        return ImGui.nDragFloat2(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragFloat2(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragFloat2(String label, float[] v, float vSpeed, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nDragFloat2(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragFloat2(String var0, float[] var1);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2, float var3);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2, float var3, float var4);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2, float var3, float var4, String var5);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    private static native boolean nDragFloat2(String var0, float[] var1, float var2, float var3, float var4, int var5);

    public static boolean dragFloat3(String label, float[] v) {
        return ImGui.nDragFloat3(label, v);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed) {
        return ImGui.nDragFloat3(label, v, vSpeed);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed, float vMin) {
        return ImGui.nDragFloat3(label, v, vSpeed, vMin);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed, float vMin, float vMax) {
        return ImGui.nDragFloat3(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed, float vMin, float vMax, String format) {
        return ImGui.nDragFloat3(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragFloat3(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragFloat3(String label, float[] v, float vSpeed, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nDragFloat3(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragFloat3(String var0, float[] var1);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2, float var3);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2, float var3, float var4);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2, float var3, float var4, String var5);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    private static native boolean nDragFloat3(String var0, float[] var1, float var2, float var3, float var4, int var5);

    public static boolean dragFloat4(String label, float[] v) {
        return ImGui.nDragFloat4(label, v);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed) {
        return ImGui.nDragFloat4(label, v, vSpeed);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed, float vMin) {
        return ImGui.nDragFloat4(label, v, vSpeed, vMin);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed, float vMin, float vMax) {
        return ImGui.nDragFloat4(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed, float vMin, float vMax, String format) {
        return ImGui.nDragFloat4(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragFloat4(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragFloat4(String label, float[] v, float vSpeed, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nDragFloat4(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragFloat4(String var0, float[] var1);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2, float var3);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2, float var3, float var4);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2, float var3, float var4, String var5);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    private static native boolean nDragFloat4(String var0, float[] var1, float var2, float var3, float var4, int var5);

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed);
    }

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed, float vMin) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin);
    }

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed, float vMin, float vMax) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax);
    }

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed, float vMin, float vMax, String format) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format);
    }

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed, float vMin, float vMax, String format, String formatMax) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax);
    }

    public static boolean dragFloatRange2(String label, float[] vCurrentMin, float[] vCurrentMax, float vSpeed, float vMin, float vMax, String format, String formatMax, int imGuiSliderFlags) {
        return ImGui.nDragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax, imGuiSliderFlags);
    }

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3);

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4);

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5);

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6);

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6, String var7);

    private static native boolean nDragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6, String var7, int var8);

    public static boolean dragInt(String label, int[] v) {
        return ImGui.nDragInt(label, v);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed) {
        return ImGui.nDragInt(label, v, vSpeed);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed, int vMin) {
        return ImGui.nDragInt(label, v, vSpeed, vMin);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed, int vMin, int vMax) {
        return ImGui.nDragInt(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed, int vMin, int vMax, String format) {
        return ImGui.nDragInt(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragInt(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragInt(String label, int[] v, float vSpeed, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nDragInt(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragInt(String var0, int[] var1);

    private static native boolean nDragInt(String var0, int[] var1, float var2);

    private static native boolean nDragInt(String var0, int[] var1, float var2, int var3);

    private static native boolean nDragInt(String var0, int[] var1, float var2, int var3, int var4);

    private static native boolean nDragInt(String var0, int[] var1, float var2, int var3, int var4, String var5);

    private static native boolean nDragInt(String var0, int[] var1, float var2, int var3, int var4, String var5, int var6);

    private static native boolean nDragInt(String var0, int[] var1, float var2, int var3, int var4, int var5);

    public static boolean dragInt2(String label, int[] v) {
        return ImGui.nDragInt2(label, v);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed) {
        return ImGui.nDragInt2(label, v, vSpeed);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed, int vMin) {
        return ImGui.nDragInt2(label, v, vSpeed, vMin);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed, int vMin, int vMax) {
        return ImGui.nDragInt2(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed, int vMin, int vMax, String format) {
        return ImGui.nDragInt2(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragInt2(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragInt2(String label, int[] v, float vSpeed, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nDragInt2(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragInt2(String var0, int[] var1);

    private static native boolean nDragInt2(String var0, int[] var1, float var2);

    private static native boolean nDragInt2(String var0, int[] var1, float var2, int var3);

    private static native boolean nDragInt2(String var0, int[] var1, float var2, int var3, int var4);

    private static native boolean nDragInt2(String var0, int[] var1, float var2, int var3, int var4, String var5);

    private static native boolean nDragInt2(String var0, int[] var1, float var2, int var3, int var4, String var5, int var6);

    private static native boolean nDragInt2(String var0, int[] var1, float var2, int var3, int var4, int var5);

    public static boolean dragInt3(String label, int[] v) {
        return ImGui.nDragInt3(label, v);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed) {
        return ImGui.nDragInt3(label, v, vSpeed);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed, int vMin) {
        return ImGui.nDragInt3(label, v, vSpeed, vMin);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed, int vMin, int vMax) {
        return ImGui.nDragInt3(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed, int vMin, int vMax, String format) {
        return ImGui.nDragInt3(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragInt3(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragInt3(String label, int[] v, float vSpeed, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nDragInt3(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragInt3(String var0, int[] var1);

    private static native boolean nDragInt3(String var0, int[] var1, float var2);

    private static native boolean nDragInt3(String var0, int[] var1, float var2, int var3);

    private static native boolean nDragInt3(String var0, int[] var1, float var2, int var3, int var4);

    private static native boolean nDragInt3(String var0, int[] var1, float var2, int var3, int var4, String var5);

    private static native boolean nDragInt3(String var0, int[] var1, float var2, int var3, int var4, String var5, int var6);

    private static native boolean nDragInt3(String var0, int[] var1, float var2, int var3, int var4, int var5);

    public static boolean dragInt4(String label, int[] v) {
        return ImGui.nDragInt4(label, v);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed) {
        return ImGui.nDragInt4(label, v, vSpeed);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed, int vMin) {
        return ImGui.nDragInt4(label, v, vSpeed, vMin);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed, int vMin, int vMax) {
        return ImGui.nDragInt4(label, v, vSpeed, vMin, vMax);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed, int vMin, int vMax, String format) {
        return ImGui.nDragInt4(label, v, vSpeed, vMin, vMax, format);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragInt4(label, v, vSpeed, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean dragInt4(String label, int[] v, float vSpeed, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nDragInt4(label, v, vSpeed, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nDragInt4(String var0, int[] var1);

    private static native boolean nDragInt4(String var0, int[] var1, float var2);

    private static native boolean nDragInt4(String var0, int[] var1, float var2, int var3);

    private static native boolean nDragInt4(String var0, int[] var1, float var2, int var3, int var4);

    private static native boolean nDragInt4(String var0, int[] var1, float var2, int var3, int var4, String var5);

    private static native boolean nDragInt4(String var0, int[] var1, float var2, int var3, int var4, String var5, int var6);

    private static native boolean nDragInt4(String var0, int[] var1, float var2, int var3, int var4, int var5);

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed, int vMin) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed, int vMin, int vMax) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed, int vMin, int vMax, String format) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed, int vMin, int vMax, String format, String formatMax) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax);
    }

    public static boolean dragIntRange2(String label, int[] vCurrentMin, int[] vCurrentMax, float vSpeed, int vMin, int vMax, String format, String formatMax, int imGuiSliderFlags) {
        return ImGui.nDragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax, imGuiSliderFlags);
    }

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3, int var4);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3, int var4, int var5);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3, int var4, int var5, String var6);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3, int var4, int var5, String var6, String var7);

    private static native boolean nDragIntRange2(String var0, int[] var1, int[] var2, float var3, int var4, int var5, String var6, String var7, int var8);

    public static boolean dragScalar(String label, short[] pData) {
        return ImGui.nDragScalar(label, pData);
    }

    public static boolean dragScalar(String label, short[] pData, float vSpeed) {
        return ImGui.nDragScalar(label, pData, vSpeed);
    }

    public static boolean dragScalar(String label, short[] pData, float vSpeed, short pMin) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin);
    }

    public static boolean dragScalar(String label, short[] pData, float vSpeed, short pMin, short pMax) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax);
    }

    public static boolean dragScalar(String label, short[] pData, float vSpeed, short pMin, short pMax, String format) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalar(String label, short[] pData, float vSpeed, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, short[] var1);

    private static native boolean nDragScalar(String var0, short[] var1, float var2);

    private static native boolean nDragScalar(String var0, short[] var1, float var2, short var3);

    private static native boolean nDragScalar(String var0, short[] var1, float var2, short var3, short var4);

    private static native boolean nDragScalar(String var0, short[] var1, float var2, short var3, short var4, String var5);

    private static native boolean nDragScalar(String var0, short[] var1, float var2, short var3, short var4, String var5, int var6);

    public static boolean dragScalar(String label, int[] pData) {
        return ImGui.nDragScalar(label, pData);
    }

    public static boolean dragScalar(String label, int[] pData, float vSpeed) {
        return ImGui.nDragScalar(label, pData, vSpeed);
    }

    public static boolean dragScalar(String label, int[] pData, float vSpeed, int pMin) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin);
    }

    public static boolean dragScalar(String label, int[] pData, float vSpeed, int pMin, int pMax) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax);
    }

    public static boolean dragScalar(String label, int[] pData, float vSpeed, int pMin, int pMax, String format) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalar(String label, int[] pData, float vSpeed, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int[] var1);

    private static native boolean nDragScalar(String var0, int[] var1, float var2);

    private static native boolean nDragScalar(String var0, int[] var1, float var2, int var3);

    private static native boolean nDragScalar(String var0, int[] var1, float var2, int var3, int var4);

    private static native boolean nDragScalar(String var0, int[] var1, float var2, int var3, int var4, String var5);

    private static native boolean nDragScalar(String var0, int[] var1, float var2, int var3, int var4, String var5, int var6);

    public static boolean dragScalar(String label, long[] pData) {
        return ImGui.nDragScalar(label, pData);
    }

    public static boolean dragScalar(String label, long[] pData, float vSpeed) {
        return ImGui.nDragScalar(label, pData, vSpeed);
    }

    public static boolean dragScalar(String label, long[] pData, float vSpeed, long pMin) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin);
    }

    public static boolean dragScalar(String label, long[] pData, float vSpeed, long pMin, long pMax) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax);
    }

    public static boolean dragScalar(String label, long[] pData, float vSpeed, long pMin, long pMax, String format) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalar(String label, long[] pData, float vSpeed, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, long[] var1);

    private static native boolean nDragScalar(String var0, long[] var1, float var2);

    private static native boolean nDragScalar(String var0, long[] var1, float var2, long var3);

    private static native boolean nDragScalar(String var0, long[] var1, float var2, long var3, long var5);

    private static native boolean nDragScalar(String var0, long[] var1, float var2, long var3, long var5, String var7);

    private static native boolean nDragScalar(String var0, long[] var1, float var2, long var3, long var5, String var7, int var8);

    public static boolean dragScalar(String label, float[] pData) {
        return ImGui.nDragScalar(label, pData);
    }

    public static boolean dragScalar(String label, float[] pData, float vSpeed) {
        return ImGui.nDragScalar(label, pData, vSpeed);
    }

    public static boolean dragScalar(String label, float[] pData, float vSpeed, float pMin) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin);
    }

    public static boolean dragScalar(String label, float[] pData, float vSpeed, float pMin, float pMax) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax);
    }

    public static boolean dragScalar(String label, float[] pData, float vSpeed, float pMin, float pMax, String format) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalar(String label, float[] pData, float vSpeed, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, float[] var1);

    private static native boolean nDragScalar(String var0, float[] var1, float var2);

    private static native boolean nDragScalar(String var0, float[] var1, float var2, float var3);

    private static native boolean nDragScalar(String var0, float[] var1, float var2, float var3, float var4);

    private static native boolean nDragScalar(String var0, float[] var1, float var2, float var3, float var4, String var5);

    private static native boolean nDragScalar(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    public static boolean dragScalar(String label, double[] pData) {
        return ImGui.nDragScalar(label, pData);
    }

    public static boolean dragScalar(String label, double[] pData, float vSpeed) {
        return ImGui.nDragScalar(label, pData, vSpeed);
    }

    public static boolean dragScalar(String label, double[] pData, float vSpeed, double pMin) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin);
    }

    public static boolean dragScalar(String label, double[] pData, float vSpeed, double pMin, double pMax) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax);
    }

    public static boolean dragScalar(String label, double[] pData, float vSpeed, double pMin, double pMax, String format) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalar(String label, double[] pData, float vSpeed, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, pData, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, double[] var1);

    private static native boolean nDragScalar(String var0, double[] var1, float var2);

    private static native boolean nDragScalar(String var0, double[] var1, float var2, double var3);

    private static native boolean nDragScalar(String var0, double[] var1, float var2, double var3, double var5);

    private static native boolean nDragScalar(String var0, double[] var1, float var2, double var3, double var5, String var7);

    private static native boolean nDragScalar(String var0, double[] var1, float var2, double var3, double var5, String var7, int var8);

    public static boolean dragScalarN(String label, short[] pData, int components) {
        return ImGui.nDragScalarN(label, pData, components);
    }

    public static boolean dragScalarN(String label, short[] pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed);
    }

    public static boolean dragScalarN(String label, short[] pData, int components, float vSpeed, short pMin) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin);
    }

    public static boolean dragScalarN(String label, short[] pData, int components, float vSpeed, short pMin, short pMax) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax);
    }

    public static boolean dragScalarN(String label, short[] pData, int components, float vSpeed, short pMin, short pMax, String format) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalarN(String label, short[] pData, int components, float vSpeed, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, short[] var1, int var2);

    private static native boolean nDragScalarN(String var0, short[] var1, int var2, float var3);

    private static native boolean nDragScalarN(String var0, short[] var1, int var2, float var3, short var4);

    private static native boolean nDragScalarN(String var0, short[] var1, int var2, float var3, short var4, short var5);

    private static native boolean nDragScalarN(String var0, short[] var1, int var2, float var3, short var4, short var5, String var6);

    private static native boolean nDragScalarN(String var0, short[] var1, int var2, float var3, short var4, short var5, String var6, int var7);

    public static boolean dragScalarN(String label, int[] pData, int components) {
        return ImGui.nDragScalarN(label, pData, components);
    }

    public static boolean dragScalarN(String label, int[] pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed);
    }

    public static boolean dragScalarN(String label, int[] pData, int components, float vSpeed, int pMin) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin);
    }

    public static boolean dragScalarN(String label, int[] pData, int components, float vSpeed, int pMin, int pMax) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax);
    }

    public static boolean dragScalarN(String label, int[] pData, int components, float vSpeed, int pMin, int pMax, String format) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalarN(String label, int[] pData, int components, float vSpeed, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int[] var1, int var2);

    private static native boolean nDragScalarN(String var0, int[] var1, int var2, float var3);

    private static native boolean nDragScalarN(String var0, int[] var1, int var2, float var3, int var4);

    private static native boolean nDragScalarN(String var0, int[] var1, int var2, float var3, int var4, int var5);

    private static native boolean nDragScalarN(String var0, int[] var1, int var2, float var3, int var4, int var5, String var6);

    private static native boolean nDragScalarN(String var0, int[] var1, int var2, float var3, int var4, int var5, String var6, int var7);

    public static boolean dragScalarN(String label, long[] pData, int components) {
        return ImGui.nDragScalarN(label, pData, components);
    }

    public static boolean dragScalarN(String label, long[] pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed);
    }

    public static boolean dragScalarN(String label, long[] pData, int components, float vSpeed, long pMin) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin);
    }

    public static boolean dragScalarN(String label, long[] pData, int components, float vSpeed, long pMin, long pMax) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax);
    }

    public static boolean dragScalarN(String label, long[] pData, int components, float vSpeed, long pMin, long pMax, String format) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalarN(String label, long[] pData, int components, float vSpeed, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, long[] var1, int var2);

    private static native boolean nDragScalarN(String var0, long[] var1, int var2, float var3);

    private static native boolean nDragScalarN(String var0, long[] var1, int var2, float var3, long var4);

    private static native boolean nDragScalarN(String var0, long[] var1, int var2, float var3, long var4, long var6);

    private static native boolean nDragScalarN(String var0, long[] var1, int var2, float var3, long var4, long var6, String var8);

    private static native boolean nDragScalarN(String var0, long[] var1, int var2, float var3, long var4, long var6, String var8, int var9);

    public static boolean dragScalarN(String label, float[] pData, int components) {
        return ImGui.nDragScalarN(label, pData, components);
    }

    public static boolean dragScalarN(String label, float[] pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed);
    }

    public static boolean dragScalarN(String label, float[] pData, int components, float vSpeed, float pMin) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin);
    }

    public static boolean dragScalarN(String label, float[] pData, int components, float vSpeed, float pMin, float pMax) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax);
    }

    public static boolean dragScalarN(String label, float[] pData, int components, float vSpeed, float pMin, float pMax, String format) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalarN(String label, float[] pData, int components, float vSpeed, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, float[] var1, int var2);

    private static native boolean nDragScalarN(String var0, float[] var1, int var2, float var3);

    private static native boolean nDragScalarN(String var0, float[] var1, int var2, float var3, float var4);

    private static native boolean nDragScalarN(String var0, float[] var1, int var2, float var3, float var4, float var5);

    private static native boolean nDragScalarN(String var0, float[] var1, int var2, float var3, float var4, float var5, String var6);

    private static native boolean nDragScalarN(String var0, float[] var1, int var2, float var3, float var4, float var5, String var6, int var7);

    public static boolean dragScalarN(String label, double[] pData, int components) {
        return ImGui.nDragScalarN(label, pData, components);
    }

    public static boolean dragScalarN(String label, double[] pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed);
    }

    public static boolean dragScalarN(String label, double[] pData, int components, float vSpeed, double pMin) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin);
    }

    public static boolean dragScalarN(String label, double[] pData, int components, float vSpeed, double pMin, double pMax) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax);
    }

    public static boolean dragScalarN(String label, double[] pData, int components, float vSpeed, double pMin, double pMax, String format) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format);
    }

    public static boolean dragScalarN(String label, double[] pData, int components, float vSpeed, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, pData, components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, double[] var1, int var2);

    private static native boolean nDragScalarN(String var0, double[] var1, int var2, float var3);

    private static native boolean nDragScalarN(String var0, double[] var1, int var2, float var3, double var4);

    private static native boolean nDragScalarN(String var0, double[] var1, int var2, float var3, double var4, double var6);

    private static native boolean nDragScalarN(String var0, double[] var1, int var2, float var3, double var4, double var6, String var8);

    private static native boolean nDragScalarN(String var0, double[] var1, int var2, float var3, double var4, double var6, String var8, int var9);

    public static boolean sliderFloat(String label, float[] v, float vMin, float vMax) {
        return ImGui.nSliderFloat(label, v, vMin, vMax);
    }

    public static boolean sliderFloat(String label, float[] v, float vMin, float vMax, String format) {
        return ImGui.nSliderFloat(label, v, vMin, vMax, format);
    }

    public static boolean sliderFloat(String label, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderFloat(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderFloat(String label, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nSliderFloat(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderFloat(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderFloat(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderFloat(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nSliderFloat(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean sliderFloat2(String label, float[] v, float vMin, float vMax) {
        return ImGui.nSliderFloat2(label, v, vMin, vMax);
    }

    public static boolean sliderFloat2(String label, float[] v, float vMin, float vMax, String format) {
        return ImGui.nSliderFloat2(label, v, vMin, vMax, format);
    }

    public static boolean sliderFloat2(String label, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderFloat2(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderFloat2(String label, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nSliderFloat2(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderFloat2(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderFloat2(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderFloat2(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nSliderFloat2(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean sliderFloat3(String label, float[] v, float vMin, float vMax) {
        return ImGui.nSliderFloat3(label, v, vMin, vMax);
    }

    public static boolean sliderFloat3(String label, float[] v, float vMin, float vMax, String format) {
        return ImGui.nSliderFloat3(label, v, vMin, vMax, format);
    }

    public static boolean sliderFloat3(String label, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderFloat3(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderFloat3(String label, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nSliderFloat3(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderFloat3(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderFloat3(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderFloat3(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nSliderFloat3(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean sliderFloat4(String label, float[] v, float vMin, float vMax) {
        return ImGui.nSliderFloat4(label, v, vMin, vMax);
    }

    public static boolean sliderFloat4(String label, float[] v, float vMin, float vMax, String format) {
        return ImGui.nSliderFloat4(label, v, vMin, vMax, format);
    }

    public static boolean sliderFloat4(String label, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderFloat4(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderFloat4(String label, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nSliderFloat4(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderFloat4(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderFloat4(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderFloat4(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nSliderFloat4(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean sliderAngle(String label, float[] vRad) {
        return ImGui.nSliderAngle(label, vRad);
    }

    public static boolean sliderAngle(String label, float[] vRad, float vDegreesMin) {
        return ImGui.nSliderAngle(label, vRad, vDegreesMin);
    }

    public static boolean sliderAngle(String label, float[] vRad, float vDegreesMin, float vDegreesMax) {
        return ImGui.nSliderAngle(label, vRad, vDegreesMin, vDegreesMax);
    }

    public static boolean sliderAngle(String label, float[] vRad, float vDegreesMin, float vDegreesMax, String format) {
        return ImGui.nSliderAngle(label, vRad, vDegreesMin, vDegreesMax, format);
    }

    public static boolean sliderAngle(String label, float[] vRad, float vDegreesMin, float vDegreesMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderAngle(label, vRad, vDegreesMin, vDegreesMax, format, imGuiSliderFlags);
    }

    public static boolean sliderAngle(String label, float[] vRad, float vDegreesMin, float vDegreesMax, int imGuiSliderFlags) {
        return ImGui.nSliderAngle(label, vRad, vDegreesMin, vDegreesMax, imGuiSliderFlags);
    }

    private static native boolean nSliderAngle(String var0, float[] var1);

    private static native boolean nSliderAngle(String var0, float[] var1, float var2);

    private static native boolean nSliderAngle(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderAngle(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderAngle(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nSliderAngle(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean sliderInt(String label, int[] v, int vMin, int vMax) {
        return ImGui.nSliderInt(label, v, vMin, vMax);
    }

    public static boolean sliderInt(String label, int[] v, int vMin, int vMax, String format) {
        return ImGui.nSliderInt(label, v, vMin, vMax, format);
    }

    public static boolean sliderInt(String label, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderInt(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderInt(String label, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nSliderInt(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderInt(String var0, int[] var1, int var2, int var3);

    private static native boolean nSliderInt(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nSliderInt(String var0, int[] var1, int var2, int var3, String var4, int var5);

    private static native boolean nSliderInt(String var0, int[] var1, int var2, int var3, int var4);

    public static boolean sliderInt2(String label, int[] v, int vMin, int vMax) {
        return ImGui.nSliderInt2(label, v, vMin, vMax);
    }

    public static boolean sliderInt2(String label, int[] v, int vMin, int vMax, String format) {
        return ImGui.nSliderInt2(label, v, vMin, vMax, format);
    }

    public static boolean sliderInt2(String label, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderInt2(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderInt2(String label, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nSliderInt2(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderInt2(String var0, int[] var1, int var2, int var3);

    private static native boolean nSliderInt2(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nSliderInt2(String var0, int[] var1, int var2, int var3, String var4, int var5);

    private static native boolean nSliderInt2(String var0, int[] var1, int var2, int var3, int var4);

    public static boolean sliderInt3(String label, int[] v, int vMin, int vMax) {
        return ImGui.nSliderInt3(label, v, vMin, vMax);
    }

    public static boolean sliderInt3(String label, int[] v, int vMin, int vMax, String format) {
        return ImGui.nSliderInt3(label, v, vMin, vMax, format);
    }

    public static boolean sliderInt3(String label, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderInt3(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderInt3(String label, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nSliderInt3(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderInt3(String var0, int[] var1, int var2, int var3);

    private static native boolean nSliderInt3(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nSliderInt3(String var0, int[] var1, int var2, int var3, String var4, int var5);

    private static native boolean nSliderInt3(String var0, int[] var1, int var2, int var3, int var4);

    public static boolean sliderInt4(String label, int[] v, int vMin, int vMax) {
        return ImGui.nSliderInt4(label, v, vMin, vMax);
    }

    public static boolean sliderInt4(String label, int[] v, int vMin, int vMax, String format) {
        return ImGui.nSliderInt4(label, v, vMin, vMax, format);
    }

    public static boolean sliderInt4(String label, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderInt4(label, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean sliderInt4(String label, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nSliderInt4(label, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nSliderInt4(String var0, int[] var1, int var2, int var3);

    private static native boolean nSliderInt4(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nSliderInt4(String var0, int[] var1, int var2, int var3, String var4, int var5);

    private static native boolean nSliderInt4(String var0, int[] var1, int var2, int var3, int var4);

    public static boolean sliderScalar(String label, short[] pData, short pMin, short pMax) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax);
    }

    public static boolean sliderScalar(String label, short[] pData, short pMin, short pMax, String format) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format);
    }

    public static boolean sliderScalar(String label, short[] pData, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, short[] var1, short var2, short var3);

    private static native boolean nSliderScalar(String var0, short[] var1, short var2, short var3, String var4);

    private static native boolean nSliderScalar(String var0, short[] var1, short var2, short var3, String var4, int var5);

    public static boolean sliderScalar(String label, int[] pData, int pMin, int pMax) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax);
    }

    public static boolean sliderScalar(String label, int[] pData, int pMin, int pMax, String format) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format);
    }

    public static boolean sliderScalar(String label, int[] pData, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int[] var1, int var2, int var3);

    private static native boolean nSliderScalar(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nSliderScalar(String var0, int[] var1, int var2, int var3, String var4, int var5);

    public static boolean sliderScalar(String label, long[] pData, long pMin, long pMax) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax);
    }

    public static boolean sliderScalar(String label, long[] pData, long pMin, long pMax, String format) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format);
    }

    public static boolean sliderScalar(String label, long[] pData, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, long[] var1, long var2, long var4);

    private static native boolean nSliderScalar(String var0, long[] var1, long var2, long var4, String var6);

    private static native boolean nSliderScalar(String var0, long[] var1, long var2, long var4, String var6, int var7);

    public static boolean sliderScalar(String label, float[] pData, float pMin, float pMax) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax);
    }

    public static boolean sliderScalar(String label, float[] pData, float pMin, float pMax, String format) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format);
    }

    public static boolean sliderScalar(String label, float[] pData, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, float[] var1, float var2, float var3);

    private static native boolean nSliderScalar(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nSliderScalar(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static boolean sliderScalar(String label, double[] pData, double pMin, double pMax) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax);
    }

    public static boolean sliderScalar(String label, double[] pData, double pMin, double pMax, String format) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format);
    }

    public static boolean sliderScalar(String label, double[] pData, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, double[] var1, double var2, double var4);

    private static native boolean nSliderScalar(String var0, double[] var1, double var2, double var4, String var6);

    private static native boolean nSliderScalar(String var0, double[] var1, double var2, double var4, String var6, int var7);

    public static boolean sliderScalarN(String label, short[] pData, int components, short pMin, short pMax) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax);
    }

    public static boolean sliderScalarN(String label, short[] pData, int components, short pMin, short pMax, String format) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format);
    }

    public static boolean sliderScalarN(String label, short[] pData, int components, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, short[] var1, int var2, short var3, short var4);

    private static native boolean nSliderScalarN(String var0, short[] var1, int var2, short var3, short var4, String var5);

    private static native boolean nSliderScalarN(String var0, short[] var1, int var2, short var3, short var4, String var5, int var6);

    public static boolean sliderScalarN(String label, int[] pData, int components, int pMin, int pMax) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax);
    }

    public static boolean sliderScalarN(String label, int[] pData, int components, int pMin, int pMax, String format) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format);
    }

    public static boolean sliderScalarN(String label, int[] pData, int components, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int[] var1, int var2, int var3, int var4);

    private static native boolean nSliderScalarN(String var0, int[] var1, int var2, int var3, int var4, String var5);

    private static native boolean nSliderScalarN(String var0, int[] var1, int var2, int var3, int var4, String var5, int var6);

    public static boolean sliderScalarN(String label, long[] pData, int components, long pMin, long pMax) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax);
    }

    public static boolean sliderScalarN(String label, long[] pData, int components, long pMin, long pMax, String format) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format);
    }

    public static boolean sliderScalarN(String label, long[] pData, int components, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, long[] var1, int var2, long var3, long var5);

    private static native boolean nSliderScalarN(String var0, long[] var1, int var2, long var3, long var5, String var7);

    private static native boolean nSliderScalarN(String var0, long[] var1, int var2, long var3, long var5, String var7, int var8);

    public static boolean sliderScalarN(String label, float[] pData, int components, float pMin, float pMax) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax);
    }

    public static boolean sliderScalarN(String label, float[] pData, int components, float pMin, float pMax, String format) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format);
    }

    public static boolean sliderScalarN(String label, float[] pData, int components, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, float[] var1, int var2, float var3, float var4);

    private static native boolean nSliderScalarN(String var0, float[] var1, int var2, float var3, float var4, String var5);

    private static native boolean nSliderScalarN(String var0, float[] var1, int var2, float var3, float var4, String var5, int var6);

    public static boolean sliderScalarN(String label, double[] pData, int components, double pMin, double pMax) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax);
    }

    public static boolean sliderScalarN(String label, double[] pData, int components, double pMin, double pMax, String format) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format);
    }

    public static boolean sliderScalarN(String label, double[] pData, int components, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, pData, components, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, double[] var1, int var2, double var3, double var5);

    private static native boolean nSliderScalarN(String var0, double[] var1, int var2, double var3, double var5, String var7);

    private static native boolean nSliderScalarN(String var0, double[] var1, int var2, double var3, double var5, String var7, int var8);

    public static boolean vSliderFloat(String label, ImVec2 size, float[] v, float vMin, float vMax) {
        return ImGui.nVSliderFloat(label, size.x, size.y, v, vMin, vMax);
    }

    public static boolean vSliderFloat(String label, float sizeX, float sizeY, float[] v, float vMin, float vMax) {
        return ImGui.nVSliderFloat(label, sizeX, sizeY, v, vMin, vMax);
    }

    public static boolean vSliderFloat(String label, ImVec2 size, float[] v, float vMin, float vMax, String format) {
        return ImGui.nVSliderFloat(label, size.x, size.y, v, vMin, vMax, format);
    }

    public static boolean vSliderFloat(String label, float sizeX, float sizeY, float[] v, float vMin, float vMax, String format) {
        return ImGui.nVSliderFloat(label, sizeX, sizeY, v, vMin, vMax, format);
    }

    public static boolean vSliderFloat(String label, ImVec2 size, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderFloat(label, size.x, size.y, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderFloat(String label, float sizeX, float sizeY, float[] v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderFloat(label, sizeX, sizeY, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderFloat(String label, ImVec2 size, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nVSliderFloat(label, size.x, size.y, v, vMin, vMax, imGuiSliderFlags);
    }

    public static boolean vSliderFloat(String label, float sizeX, float sizeY, float[] v, float vMin, float vMax, int imGuiSliderFlags) {
        return ImGui.nVSliderFloat(label, sizeX, sizeY, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nVSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5);

    private static native boolean nVSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6);

    private static native boolean nVSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6, int var7);

    private static native boolean nVSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5, int var6);

    public static boolean vSliderInt(String label, ImVec2 size, int[] v, int vMin, int vMax) {
        return ImGui.nVSliderInt(label, size.x, size.y, v, vMin, vMax);
    }

    public static boolean vSliderInt(String label, float sizeX, float sizeY, int[] v, int vMin, int vMax) {
        return ImGui.nVSliderInt(label, sizeX, sizeY, v, vMin, vMax);
    }

    public static boolean vSliderInt(String label, ImVec2 size, int[] v, int vMin, int vMax, String format) {
        return ImGui.nVSliderInt(label, size.x, size.y, v, vMin, vMax, format);
    }

    public static boolean vSliderInt(String label, float sizeX, float sizeY, int[] v, int vMin, int vMax, String format) {
        return ImGui.nVSliderInt(label, sizeX, sizeY, v, vMin, vMax, format);
    }

    public static boolean vSliderInt(String label, ImVec2 size, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderInt(label, size.x, size.y, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderInt(String label, float sizeX, float sizeY, int[] v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderInt(label, sizeX, sizeY, v, vMin, vMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderInt(String label, ImVec2 size, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nVSliderInt(label, size.x, size.y, v, vMin, vMax, imGuiSliderFlags);
    }

    public static boolean vSliderInt(String label, float sizeX, float sizeY, int[] v, int vMin, int vMax, int imGuiSliderFlags) {
        return ImGui.nVSliderInt(label, sizeX, sizeY, v, vMin, vMax, imGuiSliderFlags);
    }

    private static native boolean nVSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5);

    private static native boolean nVSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5, String var6);

    private static native boolean nVSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5, String var6, int var7);

    private static native boolean nVSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5, int var6);

    public static boolean vSliderScalar(String label, ImVec2 size, short[] pData, short pMin, short pMax) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, short[] pData, short pMin, short pMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, short[] pData, short pMin, short pMax, String format) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, short[] pData, short pMin, short pMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, short[] pData, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, short[] pData, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, short[] var3, short var4, short var5);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, short[] var3, short var4, short var5, String var6);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, short[] var3, short var4, short var5, String var6, int var7);

    public static boolean vSliderScalar(String label, ImVec2 size, int[] pData, int pMin, int pMax) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int[] pData, int pMin, int pMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, int[] pData, int pMin, int pMax, String format) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int[] pData, int pMin, int pMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, int[] pData, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int[] pData, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int[] var3, int var4, int var5);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int[] var3, int var4, int var5, String var6);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int[] var3, int var4, int var5, String var6, int var7);

    public static boolean vSliderScalar(String label, ImVec2 size, long[] pData, long pMin, long pMax) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, long[] pData, long pMin, long pMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, long[] pData, long pMin, long pMax, String format) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, long[] pData, long pMin, long pMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, long[] pData, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, long[] pData, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, long[] var3, long var4, long var6);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, long[] var3, long var4, long var6, String var8);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, long[] var3, long var4, long var6, String var8, int var9);

    public static boolean vSliderScalar(String label, ImVec2 size, float[] pData, float pMin, float pMax) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, float[] pData, float pMin, float pMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, float[] pData, float pMin, float pMax, String format) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, float[] pData, float pMin, float pMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, float[] pData, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, float[] pData, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, float[] var3, float var4, float var5);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6, int var7);

    public static boolean vSliderScalar(String label, ImVec2 size, double[] pData, double pMin, double pMax) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, double[] pData, double pMin, double pMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, double[] pData, double pMin, double pMax, String format) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, double[] pData, double pMin, double pMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format);
    }

    public static boolean vSliderScalar(String label, ImVec2 size, double[] pData, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, size.x, size.y, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, double[] pData, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, pData, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, double[] var3, double var4, double var6);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, double[] var3, double var4, double var6, String var8);

    private static native boolean nVSliderScalar(String var0, float var1, float var2, double[] var3, double var4, double var6, String var8, int var9);

    private static native void nInitInputTextData();

    public static boolean inputText(String label, ImString text) {
        return ImGui.preInputText(false, label, null, text);
    }

    public static boolean inputText(String label, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(false, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputText(String label, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(false, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextMultiline(String label, ImString text) {
        return ImGui.preInputText(true, label, null, text);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height) {
        return ImGui.preInputText(true, label, null, text, width, height);
    }

    public static boolean inputTextMultiline(String label, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(true, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(true, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height, int imGuiInputTextFlags) {
        return ImGui.preInputText(true, label, null, text, width, height, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(true, label, null, text, width, height, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text) {
        return ImGui.preInputText(false, label, hint, text);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(false, label, hint, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(false, label, hint, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text) {
        return ImGui.preInputText(multiline, label, hint, text, 0.0f, 0.0f);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height) {
        return ImGui.preInputText(multiline, label, hint, text, width, height, 0);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height, int flags) {
        return ImGui.preInputText(multiline, label, hint, text, width, height, flags, null);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height, int flags, ImGuiInputTextCallback callback) {
        ImString.InputData inputData = text.inputData;
        String hintLabel = hint;
        if (hintLabel == null) {
            hintLabel = "";
        }
        return ImGui.nInputText(multiline, hint != null, label, hintLabel, text, text.getData(), text.getData().length, width, height, flags, inputData, inputData.allowedChars, inputData.isResizable, callback);
    }

    private static native boolean nInputText(boolean var0, boolean var1, String var2, String var3, ImString var4, byte[] var5, int var6, float var7, float var8, int var9, ImString.InputData var10, String var11, boolean var12, ImGuiInputTextCallback var13);

    public static boolean inputFloat(String label, ImFloat v) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null);
    }

    public static boolean inputFloat(String label, ImFloat v, float step) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null, step);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null, step, stepFast);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast, String format) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null, step, stepFast, format);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null, step, stepFast, format, imGuiInputTextFlags);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast, int imGuiInputTextFlags) {
        return ImGui.nInputFloat(label, v != null ? v.getData() : null, step, stepFast, imGuiInputTextFlags);
    }

    private static native boolean nInputFloat(String var0, float[] var1);

    private static native boolean nInputFloat(String var0, float[] var1, float var2);

    private static native boolean nInputFloat(String var0, float[] var1, float var2, float var3);

    private static native boolean nInputFloat(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nInputFloat(String var0, float[] var1, float var2, float var3, String var4, int var5);

    private static native boolean nInputFloat(String var0, float[] var1, float var2, float var3, int var4);

    public static boolean inputFloat2(String label, float[] v) {
        return ImGui.nInputFloat2(label, v);
    }

    public static boolean inputFloat2(String label, float[] v, String format) {
        return ImGui.nInputFloat2(label, v, format);
    }

    public static boolean inputFloat2(String label, float[] v, String format, int imGuiInputTextFlags) {
        return ImGui.nInputFloat2(label, v, format, imGuiInputTextFlags);
    }

    public static boolean inputFloat2(String label, float[] v, int imGuiInputTextFlags) {
        return ImGui.nInputFloat2(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputFloat2(String var0, float[] var1);

    private static native boolean nInputFloat2(String var0, float[] var1, String var2);

    private static native boolean nInputFloat2(String var0, float[] var1, String var2, int var3);

    private static native boolean nInputFloat2(String var0, float[] var1, int var2);

    public static boolean inputFloat3(String label, float[] v) {
        return ImGui.nInputFloat3(label, v);
    }

    public static boolean inputFloat3(String label, float[] v, String format) {
        return ImGui.nInputFloat3(label, v, format);
    }

    public static boolean inputFloat3(String label, float[] v, String format, int imGuiInputTextFlags) {
        return ImGui.nInputFloat3(label, v, format, imGuiInputTextFlags);
    }

    public static boolean inputFloat3(String label, float[] v, int imGuiInputTextFlags) {
        return ImGui.nInputFloat3(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputFloat3(String var0, float[] var1);

    private static native boolean nInputFloat3(String var0, float[] var1, String var2);

    private static native boolean nInputFloat3(String var0, float[] var1, String var2, int var3);

    private static native boolean nInputFloat3(String var0, float[] var1, int var2);

    public static boolean inputFloat4(String label, float[] v) {
        return ImGui.nInputFloat4(label, v);
    }

    public static boolean inputFloat4(String label, float[] v, String format) {
        return ImGui.nInputFloat4(label, v, format);
    }

    public static boolean inputFloat4(String label, float[] v, String format, int imGuiInputTextFlags) {
        return ImGui.nInputFloat4(label, v, format, imGuiInputTextFlags);
    }

    public static boolean inputFloat4(String label, float[] v, int imGuiInputTextFlags) {
        return ImGui.nInputFloat4(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputFloat4(String var0, float[] var1);

    private static native boolean nInputFloat4(String var0, float[] var1, String var2);

    private static native boolean nInputFloat4(String var0, float[] var1, String var2, int var3);

    private static native boolean nInputFloat4(String var0, float[] var1, int var2);

    public static boolean inputInt(String label, ImInt v) {
        return ImGui.nInputInt(label, v != null ? v.getData() : null);
    }

    public static boolean inputInt(String label, ImInt v, int step) {
        return ImGui.nInputInt(label, v != null ? v.getData() : null, step);
    }

    public static boolean inputInt(String label, ImInt v, int step, int stepFast) {
        return ImGui.nInputInt(label, v != null ? v.getData() : null, step, stepFast);
    }

    public static boolean inputInt(String label, ImInt v, int step, int stepFast, int imGuiInputTextFlags) {
        return ImGui.nInputInt(label, v != null ? v.getData() : null, step, stepFast, imGuiInputTextFlags);
    }

    private static native boolean nInputInt(String var0, int[] var1);

    private static native boolean nInputInt(String var0, int[] var1, int var2);

    private static native boolean nInputInt(String var0, int[] var1, int var2, int var3);

    private static native boolean nInputInt(String var0, int[] var1, int var2, int var3, int var4);

    public static boolean inputInt2(String label, int[] v) {
        return ImGui.nInputInt2(label, v);
    }

    public static boolean inputInt2(String label, int[] v, int imGuiInputTextFlags) {
        return ImGui.nInputInt2(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputInt2(String var0, int[] var1);

    private static native boolean nInputInt2(String var0, int[] var1, int var2);

    public static boolean inputInt3(String label, int[] v) {
        return ImGui.nInputInt3(label, v);
    }

    public static boolean inputInt3(String label, int[] v, int imGuiInputTextFlags) {
        return ImGui.nInputInt3(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputInt3(String var0, int[] var1);

    private static native boolean nInputInt3(String var0, int[] var1, int var2);

    public static boolean inputInt4(String label, int[] v) {
        return ImGui.nInputInt4(label, v);
    }

    public static boolean inputInt4(String label, int[] v, int imGuiInputTextFlags) {
        return ImGui.nInputInt4(label, v, imGuiInputTextFlags);
    }

    private static native boolean nInputInt4(String var0, int[] var1);

    private static native boolean nInputInt4(String var0, int[] var1, int var2);

    public static boolean inputDouble(String label, ImDouble v) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null);
    }

    public static boolean inputDouble(String label, ImDouble v, double step) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null, step);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null, step, stepFast);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast, String format) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null, step, stepFast, format);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null, step, stepFast, format, imGuiInputTextFlags);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast, int imGuiInputTextFlags) {
        return ImGui.nInputDouble(label, v != null ? v.getData() : null, step, stepFast, imGuiInputTextFlags);
    }

    private static native boolean nInputDouble(String var0, double[] var1);

    private static native boolean nInputDouble(String var0, double[] var1, double var2);

    private static native boolean nInputDouble(String var0, double[] var1, double var2, double var4);

    private static native boolean nInputDouble(String var0, double[] var1, double var2, double var4, String var6);

    private static native boolean nInputDouble(String var0, double[] var1, double var2, double var4, String var6, int var7);

    private static native boolean nInputDouble(String var0, double[] var1, double var2, double var4, int var6);

    public static boolean inputScalar(String label, ImShort pData) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, ImShort pData, short pStep) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, ImShort pData, short pStep, short pStepFast) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, ImShort pData, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, ImShort pData, short pStep, short pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, short[] var1);

    private static native boolean nInputScalar(String var0, short[] var1, short var2);

    private static native boolean nInputScalar(String var0, short[] var1, short var2, short var3);

    private static native boolean nInputScalar(String var0, short[] var1, short var2, short var3, String var4);

    private static native boolean nInputScalar(String var0, short[] var1, short var2, short var3, String var4, int var5);

    public static boolean inputScalar(String label, ImInt pData) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, ImInt pData, int pStep) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, ImInt pData, int pStep, int pStepFast) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, ImInt pData, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, ImInt pData, int pStep, int pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int[] var1);

    private static native boolean nInputScalar(String var0, int[] var1, int var2);

    private static native boolean nInputScalar(String var0, int[] var1, int var2, int var3);

    private static native boolean nInputScalar(String var0, int[] var1, int var2, int var3, String var4);

    private static native boolean nInputScalar(String var0, int[] var1, int var2, int var3, String var4, int var5);

    public static boolean inputScalar(String label, ImLong pData) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, ImLong pData, long pStep) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, ImLong pData, long pStep, long pStepFast) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, ImLong pData, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, ImLong pData, long pStep, long pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, long[] var1);

    private static native boolean nInputScalar(String var0, long[] var1, long var2);

    private static native boolean nInputScalar(String var0, long[] var1, long var2, long var4);

    private static native boolean nInputScalar(String var0, long[] var1, long var2, long var4, String var6);

    private static native boolean nInputScalar(String var0, long[] var1, long var2, long var4, String var6, int var7);

    public static boolean inputScalar(String label, ImFloat pData) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, ImFloat pData, float pStep) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, ImFloat pData, float pStep, float pStepFast) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, ImFloat pData, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, ImFloat pData, float pStep, float pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, float[] var1);

    private static native boolean nInputScalar(String var0, float[] var1, float var2);

    private static native boolean nInputScalar(String var0, float[] var1, float var2, float var3);

    private static native boolean nInputScalar(String var0, float[] var1, float var2, float var3, String var4);

    private static native boolean nInputScalar(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static boolean inputScalar(String label, ImDouble pData) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, ImDouble pData, double pStep) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, ImDouble pData, double pStep, double pStepFast) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, ImDouble pData, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, ImDouble pData, double pStep, double pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, double[] var1);

    private static native boolean nInputScalar(String var0, double[] var1, double var2);

    private static native boolean nInputScalar(String var0, double[] var1, double var2, double var4);

    private static native boolean nInputScalar(String var0, double[] var1, double var2, double var4, String var6);

    private static native boolean nInputScalar(String var0, double[] var1, double var2, double var4, String var6, int var7);

    public static boolean inputScalar(String label, int dataType, ImShort pData) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2);

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3);

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4);

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4, String var5);

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4, String var5, int var6);

    public static boolean inputScalar(String label, int dataType, ImInt pData) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2);

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3);

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4);

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4, String var5);

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4, String var5, int var6);

    public static boolean inputScalar(String label, int dataType, ImLong pData) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2);

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3);

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5);

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5, String var7);

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5, String var7, int var8);

    public static boolean inputScalar(String label, int dataType, ImFloat pData) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2);

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3);

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4);

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4, String var5);

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4, String var5, int var6);

    public static boolean inputScalar(String label, int dataType, ImDouble pData) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null);
    }

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep);
    }

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast);
    }

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format);
    }

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalar(label, dataType, pData != null ? pData.getData() : null, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2);

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3);

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5);

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5, String var7);

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5, String var7, int var8);

    public static boolean inputScalarN(String label, short[] pData, int components) {
        return ImGui.nInputScalarN(label, pData, components);
    }

    public static boolean inputScalarN(String label, short[] pData, int components, short pStep) {
        return ImGui.nInputScalarN(label, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, short[] pData, int components, short pStep, short pStepFast) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, short[] pData, int components, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, short[] pData, int components, short pStep, short pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, short[] var1, int var2);

    private static native boolean nInputScalarN(String var0, short[] var1, int var2, short var3);

    private static native boolean nInputScalarN(String var0, short[] var1, int var2, short var3, short var4);

    private static native boolean nInputScalarN(String var0, short[] var1, int var2, short var3, short var4, String var5);

    private static native boolean nInputScalarN(String var0, short[] var1, int var2, short var3, short var4, String var5, int var6);

    public static boolean inputScalarN(String label, int[] pData, int components) {
        return ImGui.nInputScalarN(label, pData, components);
    }

    public static boolean inputScalarN(String label, int[] pData, int components, int pStep) {
        return ImGui.nInputScalarN(label, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int[] pData, int components, int pStep, int pStepFast) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int[] pData, int components, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int[] pData, int components, int pStep, int pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int[] var1, int var2);

    private static native boolean nInputScalarN(String var0, int[] var1, int var2, int var3);

    private static native boolean nInputScalarN(String var0, int[] var1, int var2, int var3, int var4);

    private static native boolean nInputScalarN(String var0, int[] var1, int var2, int var3, int var4, String var5);

    private static native boolean nInputScalarN(String var0, int[] var1, int var2, int var3, int var4, String var5, int var6);

    public static boolean inputScalarN(String label, long[] pData, int components) {
        return ImGui.nInputScalarN(label, pData, components);
    }

    public static boolean inputScalarN(String label, long[] pData, int components, long pStep) {
        return ImGui.nInputScalarN(label, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, long[] pData, int components, long pStep, long pStepFast) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, long[] pData, int components, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, long[] pData, int components, long pStep, long pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, long[] var1, int var2);

    private static native boolean nInputScalarN(String var0, long[] var1, int var2, long var3);

    private static native boolean nInputScalarN(String var0, long[] var1, int var2, long var3, long var5);

    private static native boolean nInputScalarN(String var0, long[] var1, int var2, long var3, long var5, String var7);

    private static native boolean nInputScalarN(String var0, long[] var1, int var2, long var3, long var5, String var7, int var8);

    public static boolean inputScalarN(String label, float[] pData, int components) {
        return ImGui.nInputScalarN(label, pData, components);
    }

    public static boolean inputScalarN(String label, float[] pData, int components, float pStep) {
        return ImGui.nInputScalarN(label, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, float[] pData, int components, float pStep, float pStepFast) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, float[] pData, int components, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, float[] pData, int components, float pStep, float pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, float[] var1, int var2);

    private static native boolean nInputScalarN(String var0, float[] var1, int var2, float var3);

    private static native boolean nInputScalarN(String var0, float[] var1, int var2, float var3, float var4);

    private static native boolean nInputScalarN(String var0, float[] var1, int var2, float var3, float var4, String var5);

    private static native boolean nInputScalarN(String var0, float[] var1, int var2, float var3, float var4, String var5, int var6);

    public static boolean inputScalarN(String label, double[] pData, int components) {
        return ImGui.nInputScalarN(label, pData, components);
    }

    public static boolean inputScalarN(String label, double[] pData, int components, double pStep) {
        return ImGui.nInputScalarN(label, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, double[] pData, int components, double pStep, double pStepFast) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, double[] pData, int components, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, double[] pData, int components, double pStep, double pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, double[] var1, int var2);

    private static native boolean nInputScalarN(String var0, double[] var1, int var2, double var3);

    private static native boolean nInputScalarN(String var0, double[] var1, int var2, double var3, double var5);

    private static native boolean nInputScalarN(String var0, double[] var1, int var2, double var3, double var5, String var7);

    private static native boolean nInputScalarN(String var0, double[] var1, int var2, double var3, double var5, String var7, int var8);

    public static boolean inputScalarN(String label, int dataType, short[] pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData, components);
    }

    public static boolean inputScalarN(String label, int dataType, short[] pData, int components, short pStep) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int dataType, short[] pData, int components, short pStep, short pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int dataType, short[] pData, int components, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int dataType, short[] pData, int components, short pStep, short pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3);

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4);

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5);

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5, String var6);

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5, String var6, int var7);

    public static boolean inputScalarN(String label, int dataType, int[] pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData, components);
    }

    public static boolean inputScalarN(String label, int dataType, int[] pData, int components, int pStep) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int dataType, int[] pData, int components, int pStep, int pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int dataType, int[] pData, int components, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int dataType, int[] pData, int components, int pStep, int pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3);

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4);

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5);

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5, String var6);

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5, String var6, int var7);

    public static boolean inputScalarN(String label, int dataType, long[] pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData, components);
    }

    public static boolean inputScalarN(String label, int dataType, long[] pData, int components, long pStep) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int dataType, long[] pData, int components, long pStep, long pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int dataType, long[] pData, int components, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int dataType, long[] pData, int components, long pStep, long pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3);

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4);

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6);

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6, String var8);

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6, String var8, int var9);

    public static boolean inputScalarN(String label, int dataType, float[] pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData, components);
    }

    public static boolean inputScalarN(String label, int dataType, float[] pData, int components, float pStep) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int dataType, float[] pData, int components, float pStep, float pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int dataType, float[] pData, int components, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int dataType, float[] pData, int components, float pStep, float pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3);

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4);

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5);

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, String var6);

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, String var6, int var7);

    public static boolean inputScalarN(String label, int dataType, double[] pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData, components);
    }

    public static boolean inputScalarN(String label, int dataType, double[] pData, int components, double pStep) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep);
    }

    public static boolean inputScalarN(String label, int dataType, double[] pData, int components, double pStep, double pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast);
    }

    public static boolean inputScalarN(String label, int dataType, double[] pData, int components, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format);
    }

    public static boolean inputScalarN(String label, int dataType, double[] pData, int components, double pStep, double pStepFast, String format, int imGuiSliderFlags) {
        return ImGui.nInputScalarN(label, dataType, pData, components, pStep, pStepFast, format, imGuiSliderFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3);

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4);

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6);

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6, String var8);

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6, String var8, int var9);

    public static boolean colorEdit3(String label, float[] col) {
        return ImGui.nColorEdit3(label, col);
    }

    public static boolean colorEdit3(String label, float[] col, int imGuiColorEditFlags) {
        return ImGui.nColorEdit3(label, col, imGuiColorEditFlags);
    }

    private static native boolean nColorEdit3(String var0, float[] var1);

    private static native boolean nColorEdit3(String var0, float[] var1, int var2);

    public static boolean colorEdit4(String label, float[] col) {
        return ImGui.nColorEdit4(label, col);
    }

    public static boolean colorEdit4(String label, float[] col, int imGuiColorEditFlags) {
        return ImGui.nColorEdit4(label, col, imGuiColorEditFlags);
    }

    private static native boolean nColorEdit4(String var0, float[] var1);

    private static native boolean nColorEdit4(String var0, float[] var1, int var2);

    public static boolean colorPicker3(String label, float[] col) {
        return ImGui.nColorPicker3(label, col);
    }

    public static boolean colorPicker3(String label, float[] col, int imGuiColorEditFlags) {
        return ImGui.nColorPicker3(label, col, imGuiColorEditFlags);
    }

    private static native boolean nColorPicker3(String var0, float[] var1);

    private static native boolean nColorPicker3(String var0, float[] var1, int var2);

    public static boolean colorPicker4(String label, float[] col) {
        return ImGui.nColorPicker4(label, col);
    }

    public static boolean colorPicker4(String label, float[] col, int imGuiColorEditFlags) {
        return ImGui.nColorPicker4(label, col, imGuiColorEditFlags);
    }

    public static boolean colorPicker4(String label, float[] col, int imGuiColorEditFlags, float[] refCol) {
        return ImGui.nColorPicker4(label, col, imGuiColorEditFlags, refCol);
    }

    public static boolean colorPicker4(String label, float[] col, float[] refCol) {
        return ImGui.nColorPicker4(label, col, refCol);
    }

    private static native boolean nColorPicker4(String var0, float[] var1);

    private static native boolean nColorPicker4(String var0, float[] var1, int var2);

    private static native boolean nColorPicker4(String var0, float[] var1, int var2, float[] var3);

    private static native boolean nColorPicker4(String var0, float[] var1, float[] var2);

    public static boolean colorButton(String descId, ImVec4 col) {
        return ImGui.nColorButton(descId, col.x, col.y, col.z, col.w);
    }

    public static boolean colorButton(String descId, float colX, float colY, float colZ, float colW) {
        return ImGui.nColorButton(descId, colX, colY, colZ, colW);
    }

    public static boolean colorButton(String descId, ImVec4 col, int imGuiColorEditFlags) {
        return ImGui.nColorButton(descId, col.x, col.y, col.z, col.w, imGuiColorEditFlags);
    }

    public static boolean colorButton(String descId, float colX, float colY, float colZ, float colW, int imGuiColorEditFlags) {
        return ImGui.nColorButton(descId, colX, colY, colZ, colW, imGuiColorEditFlags);
    }

    public static boolean colorButton(String descId, ImVec4 col, int imGuiColorEditFlags, ImVec2 size) {
        return ImGui.nColorButton(descId, col.x, col.y, col.z, col.w, imGuiColorEditFlags, size.x, size.y);
    }

    public static boolean colorButton(String descId, float colX, float colY, float colZ, float colW, int imGuiColorEditFlags, float sizeX, float sizeY) {
        return ImGui.nColorButton(descId, colX, colY, colZ, colW, imGuiColorEditFlags, sizeX, sizeY);
    }

    public static boolean colorButton(String descId, ImVec4 col, ImVec2 size) {
        return ImGui.nColorButton(descId, col.x, col.y, col.z, col.w, size.x, size.y);
    }

    public static boolean colorButton(String descId, float colX, float colY, float colZ, float colW, float sizeX, float sizeY) {
        return ImGui.nColorButton(descId, colX, colY, colZ, colW, sizeX, sizeY);
    }

    private static native boolean nColorButton(String var0, float var1, float var2, float var3, float var4);

    private static native boolean nColorButton(String var0, float var1, float var2, float var3, float var4, int var5);

    private static native boolean nColorButton(String var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7);

    private static native boolean nColorButton(String var0, float var1, float var2, float var3, float var4, float var5, float var6);

    @Deprecated
    public static boolean colorButton(String descId, float[] col) {
        return ImGui.nColorButton(descId, col);
    }

    @Deprecated
    public static boolean colorButton(String descId, float[] col, int imGuiColorEditFlags) {
        return ImGui.nColorButton(descId, col, imGuiColorEditFlags);
    }

    @Deprecated
    public static boolean colorButton(String descId, float[] col, int imGuiColorEditFlags, ImVec2 size) {
        return ImGui.nColorButton(descId, col, imGuiColorEditFlags, size.x, size.y);
    }

    @Deprecated
    public static boolean colorButton(String descId, float[] col, int imGuiColorEditFlags, float sizeX, float sizeY) {
        return ImGui.nColorButton(descId, col, imGuiColorEditFlags, sizeX, sizeY);
    }

    @Deprecated
    public static boolean colorButton(String descId, float[] col, ImVec2 size) {
        return ImGui.nColorButton(descId, col, size.x, size.y);
    }

    @Deprecated
    public static boolean colorButton(String descId, float[] col, float sizeX, float sizeY) {
        return ImGui.nColorButton(descId, col, sizeX, sizeY);
    }

    private static native boolean nColorButton(String var0, float[] var1);

    private static native boolean nColorButton(String var0, float[] var1, int var2);

    private static native boolean nColorButton(String var0, float[] var1, int var2, float var3, float var4);

    private static native boolean nColorButton(String var0, float[] var1, float var2, float var3);

    public static void setColorEditOptions(int imGuiColorEditFlags) {
        ImGui.nSetColorEditOptions(imGuiColorEditFlags);
    }

    private static native void nSetColorEditOptions(int var0);

    public static boolean treeNode(String label) {
        return ImGui.nTreeNode(label);
    }

    private static native boolean nTreeNode(String var0);

    public static boolean treeNode(String strId, String label) {
        return ImGui.nTreeNode(strId, label);
    }

    private static native boolean nTreeNode(String var0, String var1);

    public static boolean treeNode(long ptrId, String label) {
        return ImGui.nTreeNode(ptrId, label);
    }

    private static native boolean nTreeNode(long var0, String var2);

    public static boolean treeNodeEx(String label) {
        return ImGui.nTreeNodeEx(label);
    }

    public static boolean treeNodeEx(String label, int flags) {
        return ImGui.nTreeNodeEx(label, flags);
    }

    private static native boolean nTreeNodeEx(String var0);

    private static native boolean nTreeNodeEx(String var0, int var1);

    public static boolean treeNodeEx(String strId, int flags, String label) {
        return ImGui.nTreeNodeEx(strId, flags, label);
    }

    private static native boolean nTreeNodeEx(String var0, int var1, String var2);

    public static boolean treeNodeEx(long ptrId, int flags, String label) {
        return ImGui.nTreeNodeEx(ptrId, flags, label);
    }

    private static native boolean nTreeNodeEx(long var0, int var2, String var3);

    public static void treePush(String strId) {
        ImGui.nTreePush(strId);
    }

    private static native void nTreePush(String var0);

    public static void treePush(long ptrId) {
        ImGui.nTreePush(ptrId);
    }

    private static native void nTreePush(long var0);

    public static void treePop() {
        ImGui.nTreePop();
    }

    private static native void nTreePop();

    public static float getTreeNodeToLabelSpacing() {
        return ImGui.nGetTreeNodeToLabelSpacing();
    }

    private static native float nGetTreeNodeToLabelSpacing();

    public static boolean collapsingHeader(String label) {
        return ImGui.nCollapsingHeader(label);
    }

    public static boolean collapsingHeader(String label, int imGuiTreeNodeFlags) {
        return ImGui.nCollapsingHeader(label, imGuiTreeNodeFlags);
    }

    private static native boolean nCollapsingHeader(String var0);

    private static native boolean nCollapsingHeader(String var0, int var1);

    public static boolean collapsingHeader(String label, ImBoolean pVisible) {
        return ImGui.nCollapsingHeader(label, pVisible != null ? pVisible.getData() : null);
    }

    public static boolean collapsingHeader(String label, ImBoolean pVisible, int imGuiTreeNodeFlags) {
        return ImGui.nCollapsingHeader(label, pVisible != null ? pVisible.getData() : null, imGuiTreeNodeFlags);
    }

    private static native boolean nCollapsingHeader(String var0, boolean[] var1);

    private static native boolean nCollapsingHeader(String var0, boolean[] var1, int var2);

    public static void setNextItemOpen(boolean isOpen) {
        ImGui.nSetNextItemOpen(isOpen);
    }

    public static void setNextItemOpen(boolean isOpen, int cond) {
        ImGui.nSetNextItemOpen(isOpen, cond);
    }

    private static native void nSetNextItemOpen(boolean var0);

    private static native void nSetNextItemOpen(boolean var0, int var1);

    public static boolean selectable(String label) {
        return ImGui.nSelectable(label);
    }

    public static boolean selectable(String label, boolean selected) {
        return ImGui.nSelectable(label, selected);
    }

    public static boolean selectable(String label, boolean selected, int imGuiSelectableFlags) {
        return ImGui.nSelectable(label, selected, imGuiSelectableFlags);
    }

    public static boolean selectable(String label, boolean selected, int imGuiSelectableFlags, ImVec2 size) {
        return ImGui.nSelectable(label, selected, imGuiSelectableFlags, size.x, size.y);
    }

    public static boolean selectable(String label, boolean selected, int imGuiSelectableFlags, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, selected, imGuiSelectableFlags, sizeX, sizeY);
    }

    public static boolean selectable(String label, int imGuiSelectableFlags, ImVec2 size) {
        return ImGui.nSelectable(label, imGuiSelectableFlags, size.x, size.y);
    }

    public static boolean selectable(String label, int imGuiSelectableFlags, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, imGuiSelectableFlags, sizeX, sizeY);
    }

    public static boolean selectable(String label, ImVec2 size) {
        return ImGui.nSelectable(label, size.x, size.y);
    }

    public static boolean selectable(String label, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, sizeX, sizeY);
    }

    public static boolean selectable(String label, boolean selected, ImVec2 size) {
        return ImGui.nSelectable(label, selected, size.x, size.y);
    }

    public static boolean selectable(String label, boolean selected, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, selected, sizeX, sizeY);
    }

    private static native boolean nSelectable(String var0);

    private static native boolean nSelectable(String var0, boolean var1);

    private static native boolean nSelectable(String var0, boolean var1, int var2);

    private static native boolean nSelectable(String var0, boolean var1, int var2, float var3, float var4);

    private static native boolean nSelectable(String var0, int var1, float var2, float var3);

    private static native boolean nSelectable(String var0, float var1, float var2);

    private static native boolean nSelectable(String var0, boolean var1, float var2, float var3);

    public static boolean selectable(String label, ImBoolean pSelected) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null);
    }

    public static boolean selectable(String label, ImBoolean pSelected, int imGuiSelectableFlags) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null, imGuiSelectableFlags);
    }

    public static boolean selectable(String label, ImBoolean pSelected, int imGuiSelectableFlags, ImVec2 size) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null, imGuiSelectableFlags, size.x, size.y);
    }

    public static boolean selectable(String label, ImBoolean pSelected, int imGuiSelectableFlags, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null, imGuiSelectableFlags, sizeX, sizeY);
    }

    public static boolean selectable(String label, ImBoolean pSelected, ImVec2 size) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null, size.x, size.y);
    }

    public static boolean selectable(String label, ImBoolean pSelected, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, pSelected != null ? pSelected.getData() : null, sizeX, sizeY);
    }

    private static native boolean nSelectable(String var0, boolean[] var1);

    private static native boolean nSelectable(String var0, boolean[] var1, int var2);

    private static native boolean nSelectable(String var0, boolean[] var1, int var2, float var3, float var4);

    private static native boolean nSelectable(String var0, boolean[] var1, float var2, float var3);

    public static boolean beginListBox(String label) {
        return ImGui.nBeginListBox(label);
    }

    public static boolean beginListBox(String label, ImVec2 size) {
        return ImGui.nBeginListBox(label, size.x, size.y);
    }

    public static boolean beginListBox(String label, float sizeX, float sizeY) {
        return ImGui.nBeginListBox(label, sizeX, sizeY);
    }

    private static native boolean nBeginListBox(String var0);

    private static native boolean nBeginListBox(String var0, float var1, float var2);

    public static void endListBox() {
        ImGui.nEndListBox();
    }

    private static native void nEndListBox();

    public static void listBox(String label, ImInt currentItem, String[] items) {
        ImGui.nListBox(label, currentItem != null ? currentItem.getData() : null, items, items.length);
    }

    public static void listBox(String label, ImInt currentItem, String[] items, int heightInItems) {
        ImGui.nListBox(label, currentItem != null ? currentItem.getData() : null, items, items.length, heightInItems);
    }

    private static native void nListBox(String var0, int[] var1, String[] var2, int var3);

    private static native void nListBox(String var0, int[] var1, String[] var2, int var3, int var4);

    public static void plotLines(String label, float[] values, int valuesCount) {
        ImGui.nPlotLines(label, values, valuesCount);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotLines(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, int stride) {
        ImGui.nPlotLines(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, stride);
    }

    private static native void nPlotLines(String var0, float[] var1, int var2);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4, float var5);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8, int var9);

    private static native void nPlotLines(String var0, float[] var1, int var2, String var3, float var4, float var5, float var6, float var7, int var8);

    private static native void nPlotLines(String var0, float[] var1, int var2, float var3, float var4, float var5, float var6, int var7);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, float var4, float var5, float var6, float var7, int var8);

    private static native void nPlotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, int var7);

    public static void plotHistogram(String label, float[] values, int valuesCount) {
        ImGui.nPlotHistogram(label, values, valuesCount);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, String overlayText, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, overlayText, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, String overlayText, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, overlayText, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, float scaleMin, float scaleMax, ImVec2 graphSize, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, scaleMin, scaleMax, graphSize.x, graphSize.y, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, float scaleMin, float scaleMax, float graphSizeX, float graphSizeY, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, scaleMin, scaleMax, graphSizeX, graphSizeY, stride);
    }

    public static void plotHistogram(String label, float[] values, int valuesCount, int valuesOffset, String overlayText, float scaleMin, float scaleMax, int stride) {
        ImGui.nPlotHistogram(label, values, valuesCount, valuesOffset, overlayText, scaleMin, scaleMax, stride);
    }

    private static native void nPlotHistogram(String var0, float[] var1, int var2);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8, int var9);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, String var3, float var4, float var5, float var6, float var7, int var8);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, float var3, float var4, float var5, float var6, int var7);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, float var4, float var5, float var6, float var7, int var8);

    private static native void nPlotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, int var7);

    public static void value(String prefix, Number value) {
        ImGui.nValue(prefix, value.toString());
    }

    public static void value(String prefix, float value, String floatFormat) {
        ImGui.nValue(prefix, String.format(floatFormat, Float.valueOf(value)));
    }

    private static native void nValue(String var0, String var1);

    public static boolean beginMenuBar() {
        return ImGui.nBeginMenuBar();
    }

    private static native boolean nBeginMenuBar();

    public static void endMenuBar() {
        ImGui.nEndMenuBar();
    }

    private static native void nEndMenuBar();

    public static boolean beginMainMenuBar() {
        return ImGui.nBeginMainMenuBar();
    }

    private static native boolean nBeginMainMenuBar();

    public static void endMainMenuBar() {
        ImGui.nEndMainMenuBar();
    }

    private static native void nEndMainMenuBar();

    public static boolean beginMenu(String label) {
        return ImGui.nBeginMenu(label);
    }

    public static boolean beginMenu(String label, boolean enabled) {
        return ImGui.nBeginMenu(label, enabled);
    }

    private static native boolean nBeginMenu(String var0);

    private static native boolean nBeginMenu(String var0, boolean var1);

    public static void endMenu() {
        ImGui.nEndMenu();
    }

    private static native void nEndMenu();

    public static boolean menuItem(String label) {
        return ImGui.nMenuItem(label);
    }

    public static boolean menuItem(String label, boolean selected) {
        return ImGui.nMenuItem(label, selected);
    }

    public static boolean menuItem(String label, boolean selected, boolean enabled) {
        return ImGui.nMenuItem(label, selected, enabled);
    }

    private static native boolean nMenuItem(String var0);

    private static native boolean nMenuItem(String var0, boolean var1);

    private static native boolean nMenuItem(String var0, boolean var1, boolean var2);

    public static boolean menuItem(String label, String shortcut) {
        return ImGui.nMenuItem(label, shortcut);
    }

    public static boolean menuItem(String label, String shortcut, boolean selected) {
        return ImGui.nMenuItem(label, shortcut, selected);
    }

    public static boolean menuItem(String label, String shortcut, boolean selected, boolean enabled) {
        return ImGui.nMenuItem(label, shortcut, selected, enabled);
    }

    private static native boolean nMenuItem(String var0, String var1);

    private static native boolean nMenuItem(String var0, String var1, boolean var2);

    private static native boolean nMenuItem(String var0, String var1, boolean var2, boolean var3);

    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected) {
        return ImGui.nMenuItem(label, shortcut, pSelected != null ? pSelected.getData() : null);
    }

    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected, boolean enabled) {
        return ImGui.nMenuItem(label, shortcut, pSelected != null ? pSelected.getData() : null, enabled);
    }

    private static native boolean nMenuItem(String var0, String var1, boolean[] var2);

    private static native boolean nMenuItem(String var0, String var1, boolean[] var2, boolean var3);

    public static void beginTooltip() {
        ImGui.nBeginTooltip();
    }

    private static native void nBeginTooltip();

    public static void endTooltip() {
        ImGui.nEndTooltip();
    }

    private static native void nEndTooltip();

    public static void setTooltip(String text) {
        ImGui.nSetTooltip(text);
    }

    private static native void nSetTooltip(String var0);

    public static boolean beginItemTooltip() {
        return ImGui.nBeginItemTooltip();
    }

    private static native boolean nBeginItemTooltip();

    public static void setItemTooltip(String text) {
        ImGui.nSetItemTooltip(text);
    }

    private static native void nSetItemTooltip(String var0);

    public static boolean beginPopup(String strId) {
        return ImGui.nBeginPopup(strId);
    }

    public static boolean beginPopup(String strId, int imGuiWindowFlags) {
        return ImGui.nBeginPopup(strId, imGuiWindowFlags);
    }

    private static native boolean nBeginPopup(String var0);

    private static native boolean nBeginPopup(String var0, int var1);

    public static boolean beginPopupModal(String name) {
        return ImGui.nBeginPopupModal(name);
    }

    public static boolean beginPopupModal(String name, ImBoolean pOpen) {
        return ImGui.nBeginPopupModal(name, pOpen != null ? pOpen.getData() : null);
    }

    public static boolean beginPopupModal(String name, ImBoolean pOpen, int imGuiWindowFlags) {
        return ImGui.nBeginPopupModal(name, pOpen != null ? pOpen.getData() : null, imGuiWindowFlags);
    }

    public static boolean beginPopupModal(String name, int imGuiWindowFlags) {
        return ImGui.nBeginPopupModal(name, imGuiWindowFlags);
    }

    private static native boolean nBeginPopupModal(String var0);

    private static native boolean nBeginPopupModal(String var0, boolean[] var1);

    private static native boolean nBeginPopupModal(String var0, boolean[] var1, int var2);

    private static native boolean nBeginPopupModal(String var0, int var1);

    public static void endPopup() {
        ImGui.nEndPopup();
    }

    private static native void nEndPopup();

    public static void openPopup(String strId) {
        ImGui.nOpenPopup(strId);
    }

    public static void openPopup(String strId, int imGuiPopupFlags) {
        ImGui.nOpenPopup(strId, imGuiPopupFlags);
    }

    private static native void nOpenPopup(String var0);

    private static native void nOpenPopup(String var0, int var1);

    public static void openPopup(int id) {
        ImGui.nOpenPopup(id);
    }

    public static void openPopup(int id, int imGuiPopupFlags) {
        ImGui.nOpenPopup(id, imGuiPopupFlags);
    }

    private static native void nOpenPopup(int var0);

    private static native void nOpenPopup(int var0, int var1);

    public static void openPopupOnItemClick() {
        ImGui.nOpenPopupOnItemClick();
    }

    public static void openPopupOnItemClick(String strId) {
        ImGui.nOpenPopupOnItemClick(strId);
    }

    public static void openPopupOnItemClick(String strId, int imGuiPopupFlags) {
        ImGui.nOpenPopupOnItemClick(strId, imGuiPopupFlags);
    }

    public static void openPopupOnItemClick(int imGuiPopupFlags) {
        ImGui.nOpenPopupOnItemClick(imGuiPopupFlags);
    }

    private static native void nOpenPopupOnItemClick();

    private static native void nOpenPopupOnItemClick(String var0);

    private static native void nOpenPopupOnItemClick(String var0, int var1);

    private static native void nOpenPopupOnItemClick(int var0);

    public static void closeCurrentPopup() {
        ImGui.nCloseCurrentPopup();
    }

    private static native void nCloseCurrentPopup();

    public static boolean beginPopupContextItem() {
        return ImGui.nBeginPopupContextItem();
    }

    public static boolean beginPopupContextItem(String strId) {
        return ImGui.nBeginPopupContextItem(strId);
    }

    public static boolean beginPopupContextItem(String strId, int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextItem(strId, imGuiPopupFlags);
    }

    public static boolean beginPopupContextItem(int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextItem(imGuiPopupFlags);
    }

    private static native boolean nBeginPopupContextItem();

    private static native boolean nBeginPopupContextItem(String var0);

    private static native boolean nBeginPopupContextItem(String var0, int var1);

    private static native boolean nBeginPopupContextItem(int var0);

    public static boolean beginPopupContextWindow() {
        return ImGui.nBeginPopupContextWindow();
    }

    public static boolean beginPopupContextWindow(String strId) {
        return ImGui.nBeginPopupContextWindow(strId);
    }

    public static boolean beginPopupContextWindow(String strId, int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextWindow(strId, imGuiPopupFlags);
    }

    public static boolean beginPopupContextWindow(int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextWindow(imGuiPopupFlags);
    }

    private static native boolean nBeginPopupContextWindow();

    private static native boolean nBeginPopupContextWindow(String var0);

    private static native boolean nBeginPopupContextWindow(String var0, int var1);

    private static native boolean nBeginPopupContextWindow(int var0);

    public static boolean beginPopupContextVoid() {
        return ImGui.nBeginPopupContextVoid();
    }

    public static boolean beginPopupContextVoid(String strId) {
        return ImGui.nBeginPopupContextVoid(strId);
    }

    public static boolean beginPopupContextVoid(String strId, int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextVoid(strId, imGuiPopupFlags);
    }

    public static boolean beginPopupContextVoid(int imGuiPopupFlags) {
        return ImGui.nBeginPopupContextVoid(imGuiPopupFlags);
    }

    private static native boolean nBeginPopupContextVoid();

    private static native boolean nBeginPopupContextVoid(String var0);

    private static native boolean nBeginPopupContextVoid(String var0, int var1);

    private static native boolean nBeginPopupContextVoid(int var0);

    public static boolean isPopupOpen(String strId) {
        return ImGui.nIsPopupOpen(strId);
    }

    public static boolean isPopupOpen(String strId, int imGuiPopupFlags) {
        return ImGui.nIsPopupOpen(strId, imGuiPopupFlags);
    }

    private static native boolean nIsPopupOpen(String var0);

    private static native boolean nIsPopupOpen(String var0, int var1);

    public static boolean beginTable(String id, int column) {
        return ImGui.nBeginTable(id, column);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags, ImVec2 outerSize) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags, outerSize.x, outerSize.y);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags, float outerSizeX, float outerSizeY) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags, outerSizeX, outerSizeY);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags, ImVec2 outerSize, float innerWidth) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags, outerSize.x, outerSize.y, innerWidth);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags, float outerSizeX, float outerSizeY, float innerWidth) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags, outerSizeX, outerSizeY, innerWidth);
    }

    public static boolean beginTable(String id, int column, ImVec2 outerSize, float innerWidth) {
        return ImGui.nBeginTable(id, column, outerSize.x, outerSize.y, innerWidth);
    }

    public static boolean beginTable(String id, int column, float outerSizeX, float outerSizeY, float innerWidth) {
        return ImGui.nBeginTable(id, column, outerSizeX, outerSizeY, innerWidth);
    }

    public static boolean beginTable(String id, int column, float innerWidth) {
        return ImGui.nBeginTable(id, column, innerWidth);
    }

    public static boolean beginTable(String id, int column, int imGuiTableFlags, float innerWidth) {
        return ImGui.nBeginTable(id, column, imGuiTableFlags, innerWidth);
    }

    private static native boolean nBeginTable(String var0, int var1);

    private static native boolean nBeginTable(String var0, int var1, int var2);

    private static native boolean nBeginTable(String var0, int var1, int var2, float var3, float var4);

    private static native boolean nBeginTable(String var0, int var1, int var2, float var3, float var4, float var5);

    private static native boolean nBeginTable(String var0, int var1, float var2, float var3, float var4);

    private static native boolean nBeginTable(String var0, int var1, float var2);

    private static native boolean nBeginTable(String var0, int var1, int var2, float var3);

    public static void endTable() {
        ImGui.nEndTable();
    }

    private static native void nEndTable();

    public static void tableNextRow() {
        ImGui.nTableNextRow();
    }

    public static void tableNextRow(int imGuiTableRowFlags) {
        ImGui.nTableNextRow(imGuiTableRowFlags);
    }

    public static void tableNextRow(int imGuiTableRowFlags, float minRowHeight) {
        ImGui.nTableNextRow(imGuiTableRowFlags, minRowHeight);
    }

    public static void tableNextRow(float minRowHeight) {
        ImGui.nTableNextRow(minRowHeight);
    }

    private static native void nTableNextRow();

    private static native void nTableNextRow(int var0);

    private static native void nTableNextRow(int var0, float var1);

    private static native void nTableNextRow(float var0);

    public static boolean tableNextColumn() {
        return ImGui.nTableNextColumn();
    }

    private static native boolean nTableNextColumn();

    public static boolean tableSetColumnIndex(int columnN) {
        return ImGui.nTableSetColumnIndex(columnN);
    }

    private static native boolean nTableSetColumnIndex(int var0);

    public static void tableSetupColumn(String label) {
        ImGui.nTableSetupColumn(label);
    }

    public static void tableSetupColumn(String label, int imGuiTableColumnFlags) {
        ImGui.nTableSetupColumn(label, imGuiTableColumnFlags);
    }

    public static void tableSetupColumn(String label, int imGuiTableColumnFlags, float initWidthOrWeight) {
        ImGui.nTableSetupColumn(label, imGuiTableColumnFlags, initWidthOrWeight);
    }

    public static void tableSetupColumn(String label, int imGuiTableColumnFlags, float initWidthOrWeight, int userId) {
        ImGui.nTableSetupColumn(label, imGuiTableColumnFlags, initWidthOrWeight, userId);
    }

    public static void tableSetupColumn(String label, float initWidthOrWeight, int userId) {
        ImGui.nTableSetupColumn(label, initWidthOrWeight, userId);
    }

    public static void tableSetupColumn(String label, int imGuiTableColumnFlags, int userId) {
        ImGui.nTableSetupColumn(label, imGuiTableColumnFlags, userId);
    }

    private static native void nTableSetupColumn(String var0);

    private static native void nTableSetupColumn(String var0, int var1);

    private static native void nTableSetupColumn(String var0, int var1, float var2);

    private static native void nTableSetupColumn(String var0, int var1, float var2, int var3);

    private static native void nTableSetupColumn(String var0, float var1, int var2);

    private static native void nTableSetupColumn(String var0, int var1, int var2);

    public static void tableSetupScrollFreeze(int cols, int rows) {
        ImGui.nTableSetupScrollFreeze(cols, rows);
    }

    private static native void nTableSetupScrollFreeze(int var0, int var1);

    public static void tableHeadersRow() {
        ImGui.nTableHeadersRow();
    }

    private static native void nTableHeadersRow();

    public static void tableHeader(String label) {
        ImGui.nTableHeader(label);
    }

    private static native void nTableHeader(String var0);

    public static ImGuiTableSortSpecs tableGetSortSpecs() {
        return new ImGuiTableSortSpecs(ImGui.nTableGetSortSpecs());
    }

    private static native long nTableGetSortSpecs();

    public static int tableGetColumnCount() {
        return ImGui.nTableGetColumnCount();
    }

    private static native int nTableGetColumnCount();

    public static int tableGetColumnIndex() {
        return ImGui.nTableGetColumnIndex();
    }

    private static native int nTableGetColumnIndex();

    public static int tableGetRowIndex() {
        return ImGui.nTableGetRowIndex();
    }

    private static native int nTableGetRowIndex();

    public static String tableGetColumnName() {
        return ImGui.nTableGetColumnName();
    }

    public static String tableGetColumnName(int columnN) {
        return ImGui.nTableGetColumnName(columnN);
    }

    private static native String nTableGetColumnName();

    private static native String nTableGetColumnName(int var0);

    public static int tableGetColumnFlags() {
        return ImGui.nTableGetColumnFlags();
    }

    public static int tableGetColumnFlags(int columnN) {
        return ImGui.nTableGetColumnFlags(columnN);
    }

    private static native int nTableGetColumnFlags();

    private static native int nTableGetColumnFlags(int var0);

    public static void tableSetColumnEnabled(int columnN, boolean value) {
        ImGui.nTableSetColumnEnabled(columnN, value);
    }

    private static native void nTableSetColumnEnabled(int var0, boolean var1);

    public static void tableSetBgColor(int imGuiTableBgTarget, int color) {
        ImGui.nTableSetBgColor(imGuiTableBgTarget, color);
    }

    public static void tableSetBgColor(int imGuiTableBgTarget, int color, int columnN) {
        ImGui.nTableSetBgColor(imGuiTableBgTarget, color, columnN);
    }

    private static native void nTableSetBgColor(int var0, int var1);

    private static native void nTableSetBgColor(int var0, int var1, int var2);

    public static void columns() {
        ImGui.nColumns();
    }

    public static void columns(int count) {
        ImGui.nColumns(count);
    }

    public static void columns(int count, String id) {
        ImGui.nColumns(count, id);
    }

    public static void columns(int count, String id, boolean border) {
        ImGui.nColumns(count, id, border);
    }

    public static void columns(String id, boolean border) {
        ImGui.nColumns(id, border);
    }

    public static void columns(boolean border) {
        ImGui.nColumns(border);
    }

    public static void columns(int count, boolean border) {
        ImGui.nColumns(count, border);
    }

    private static native void nColumns();

    private static native void nColumns(int var0);

    private static native void nColumns(int var0, String var1);

    private static native void nColumns(int var0, String var1, boolean var2);

    private static native void nColumns(String var0, boolean var1);

    private static native void nColumns(boolean var0);

    private static native void nColumns(int var0, boolean var1);

    public static void nextColumn() {
        ImGui.nNextColumn();
    }

    private static native void nNextColumn();

    public static int getColumnIndex() {
        return ImGui.nGetColumnIndex();
    }

    private static native int nGetColumnIndex();

    public static float getColumnWidth() {
        return ImGui.nGetColumnWidth();
    }

    public static float getColumnWidth(int columnIndex) {
        return ImGui.nGetColumnWidth(columnIndex);
    }

    private static native float nGetColumnWidth();

    private static native float nGetColumnWidth(int var0);

    public static void setColumnWidth(int columnIndex, float width) {
        ImGui.nSetColumnWidth(columnIndex, width);
    }

    private static native void nSetColumnWidth(int var0, float var1);

    public static float getColumnOffset() {
        return ImGui.nGetColumnOffset();
    }

    public static float getColumnOffset(int columnIndex) {
        return ImGui.nGetColumnOffset(columnIndex);
    }

    private static native float nGetColumnOffset();

    private static native float nGetColumnOffset(int var0);

    public static void setColumnOffset(int columnIndex, float offsetX) {
        ImGui.nSetColumnOffset(columnIndex, offsetX);
    }

    private static native void nSetColumnOffset(int var0, float var1);

    public static int getColumnsCount() {
        return ImGui.nGetColumnsCount();
    }

    private static native int nGetColumnsCount();

    public static boolean beginTabBar(String strId) {
        return ImGui.nBeginTabBar(strId);
    }

    public static boolean beginTabBar(String strId, int imGuiTabBarFlags) {
        return ImGui.nBeginTabBar(strId, imGuiTabBarFlags);
    }

    private static native boolean nBeginTabBar(String var0);

    private static native boolean nBeginTabBar(String var0, int var1);

    public static void endTabBar() {
        ImGui.nEndTabBar();
    }

    private static native void nEndTabBar();

    public static boolean beginTabItem(String label) {
        return ImGui.nBeginTabItem(label);
    }

    public static boolean beginTabItem(String label, ImBoolean pOpen) {
        return ImGui.nBeginTabItem(label, pOpen != null ? pOpen.getData() : null);
    }

    public static boolean beginTabItem(String label, ImBoolean pOpen, int imGuiTabItemFlags) {
        return ImGui.nBeginTabItem(label, pOpen != null ? pOpen.getData() : null, imGuiTabItemFlags);
    }

    public static boolean beginTabItem(String label, int imGuiTabItemFlags) {
        return ImGui.nBeginTabItem(label, imGuiTabItemFlags);
    }

    private static native boolean nBeginTabItem(String var0);

    private static native boolean nBeginTabItem(String var0, boolean[] var1);

    private static native boolean nBeginTabItem(String var0, boolean[] var1, int var2);

    private static native boolean nBeginTabItem(String var0, int var1);

    public static void endTabItem() {
        ImGui.nEndTabItem();
    }

    private static native void nEndTabItem();

    public static boolean tabItemButton(String label) {
        return ImGui.nTabItemButton(label);
    }

    public static boolean tabItemButton(String label, int imGuiTabItemFlags) {
        return ImGui.nTabItemButton(label, imGuiTabItemFlags);
    }

    private static native boolean nTabItemButton(String var0);

    private static native boolean nTabItemButton(String var0, int var1);

    public static void setTabItemClosed(String tabOrDockedWindowLabel) {
        ImGui.nSetTabItemClosed(tabOrDockedWindowLabel);
    }

    private static native void nSetTabItemClosed(String var0);

    public static int dockSpace(int imGuiID) {
        return ImGui.nDockSpace(imGuiID);
    }

    public static int dockSpace(int imGuiID, ImVec2 size) {
        return ImGui.nDockSpace(imGuiID, size.x, size.y);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY);
    }

    public static int dockSpace(int imGuiID, ImVec2 size, int imGuiDockNodeFlags) {
        return ImGui.nDockSpace(imGuiID, size.x, size.y, imGuiDockNodeFlags);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags);
    }

    public static int dockSpace(int imGuiID, ImVec2 size, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, size.x, size.y, imGuiDockNodeFlags, windowClass.ptr);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags, windowClass.ptr);
    }

    public static int dockSpace(int imGuiID, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, imGuiDockNodeFlags, windowClass.ptr);
    }

    public static int dockSpace(int imGuiID, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, windowClass.ptr);
    }

    public static int dockSpace(int imGuiID, ImVec2 size, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, size.x, size.y, windowClass.ptr);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, windowClass.ptr);
    }

    private static native int nDockSpace(int var0);

    private static native int nDockSpace(int var0, float var1, float var2);

    private static native int nDockSpace(int var0, float var1, float var2, int var3);

    private static native int nDockSpace(int var0, float var1, float var2, int var3, long var4);

    private static native int nDockSpace(int var0, int var1, long var2);

    private static native int nDockSpace(int var0, long var1);

    private static native int nDockSpace(int var0, float var1, float var2, long var3);

    public static int dockSpaceOverViewport() {
        return ImGui.nDockSpaceOverViewport();
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags, windowClass.ptr);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, windowClass.ptr);
    }

    private static native int nDockSpaceOverViewport();

    private static native int nDockSpaceOverViewport(long var0);

    private static native int nDockSpaceOverViewport(long var0, int var2);

    private static native int nDockSpaceOverViewport(long var0, int var2, long var3);

    private static native int nDockSpaceOverViewport(long var0, long var2);

    public static void setNextWindowDockID(int dockId) {
        ImGui.nSetNextWindowDockID(dockId);
    }

    public static void setNextWindowDockID(int dockId, int imGuiCond) {
        ImGui.nSetNextWindowDockID(dockId, imGuiCond);
    }

    private static native void nSetNextWindowDockID(int var0);

    private static native void nSetNextWindowDockID(int var0, int var1);

    public static void setNextWindowClass(ImGuiWindowClass windowClass) {
        ImGui.nSetNextWindowClass(windowClass.ptr);
    }

    private static native void nSetNextWindowClass(long var0);

    public static int getWindowDockID() {
        return ImGui.nGetWindowDockID();
    }

    private static native int nGetWindowDockID();

    public static boolean isWindowDocked() {
        return ImGui.nIsWindowDocked();
    }

    private static native boolean nIsWindowDocked();

    public static void logToTTY() {
        ImGui.nLogToTTY();
    }

    public static void logToTTY(int autoOpenDepth) {
        ImGui.nLogToTTY(autoOpenDepth);
    }

    private static native void nLogToTTY();

    private static native void nLogToTTY(int var0);

    public static void logToFile() {
        ImGui.nLogToFile();
    }

    public static void logToFile(int autoOpenDepth) {
        ImGui.nLogToFile(autoOpenDepth);
    }

    public static void logToFile(int autoOpenDepth, String filename) {
        ImGui.nLogToFile(autoOpenDepth, filename);
    }

    public static void logToFile(String filename) {
        ImGui.nLogToFile(filename);
    }

    private static native void nLogToFile();

    private static native void nLogToFile(int var0);

    private static native void nLogToFile(int var0, String var1);

    private static native void nLogToFile(String var0);

    public static void logToClipboard() {
        ImGui.nLogToClipboard();
    }

    public static void logToClipboard(int autoOpenDepth) {
        ImGui.nLogToClipboard(autoOpenDepth);
    }

    private static native void nLogToClipboard();

    private static native void nLogToClipboard(int var0);

    public static void logFinish() {
        ImGui.nLogFinish();
    }

    private static native void nLogFinish();

    public static void logButtons() {
        ImGui.nLogButtons();
    }

    private static native void nLogButtons();

    public static void logText(String text) {
        ImGui.nLogText(text);
    }

    private static native void nLogText(String var0);

    public static boolean beginDragDropSource() {
        return ImGui.nBeginDragDropSource();
    }

    public static boolean beginDragDropSource(int imGuiDragDropFlags) {
        return ImGui.nBeginDragDropSource(imGuiDragDropFlags);
    }

    private static native boolean nBeginDragDropSource();

    private static native boolean nBeginDragDropSource(int var0);

    public static boolean setDragDropPayload(String dataType, Object payload) {
        return ImGui.setDragDropPayload(dataType, payload, 0);
    }

    public static boolean setDragDropPayload(String dataType, Object payload, int imGuiCond) {
        if (payloadRef == null || payloadRef.get() != payload) {
            payloadRef = new WeakReference<Object>(payload);
        }
        return ImGui.nSetDragDropPayload(dataType, PAYLOAD_PLACEHOLDER_DATA, 1, imGuiCond);
    }

    public static boolean setDragDropPayload(Object payload) {
        return ImGui.setDragDropPayload(payload, 0);
    }

    public static boolean setDragDropPayload(Object payload, int imGuiCond) {
        return ImGui.setDragDropPayload(String.valueOf(payload.getClass().hashCode()), payload, imGuiCond);
    }

    private static native boolean nSetDragDropPayload(String var0, byte[] var1, int var2, int var3);

    public static void endDragDropSource() {
        ImGui.nEndDragDropSource();
    }

    private static native void nEndDragDropSource();

    public static boolean beginDragDropTarget() {
        return ImGui.nBeginDragDropTarget();
    }

    private static native boolean nBeginDragDropTarget();

    public static <T> T acceptDragDropPayload(String dataType) {
        return ImGui.acceptDragDropPayload(dataType, 0);
    }

    public static <T> T acceptDragDropPayload(String dataType, Class<T> aClass) {
        return ImGui.acceptDragDropPayload(dataType, 0, aClass);
    }

    public static <T> T acceptDragDropPayload(String dataType, int imGuiDragDropFlags) {
        return ImGui.acceptDragDropPayload(dataType, imGuiDragDropFlags, null);
    }

    public static <T> T acceptDragDropPayload(String dataType, int imGuiDragDropFlags, Class<T> aClass) {
        Object rawPayload;
        if (payloadRef != null && ImGui.nAcceptDragDropPayload(dataType, imGuiDragDropFlags) && (rawPayload = payloadRef.get()) != null && (aClass == null || rawPayload.getClass().isAssignableFrom(aClass))) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T acceptDragDropPayload(Class<T> aClass) {
        return ImGui.acceptDragDropPayload(String.valueOf(aClass.hashCode()), 0, aClass);
    }

    public static <T> T acceptDragDropPayload(Class<T> aClass, int imGuiDragDropFlags) {
        return ImGui.acceptDragDropPayload(String.valueOf(aClass.hashCode()), imGuiDragDropFlags, aClass);
    }

    private static native boolean nAcceptDragDropPayload(String var0, int var1);

    public static void endDragDropTarget() {
        ImGui.nEndDragDropTarget();
    }

    private static native void nEndDragDropTarget();

    public static <T> T getDragDropPayload() {
        Object rawPayload;
        if (payloadRef != null && ImGui.nHasDragDropPayload() && (rawPayload = payloadRef.get()) != null) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T getDragDropPayload(String dataType) {
        Object rawPayload;
        if (payloadRef != null && ImGui.nHasDragDropPayload(dataType) && (rawPayload = payloadRef.get()) != null) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T getDragDropPayload(Class<T> aClass) {
        return ImGui.getDragDropPayload(String.valueOf(aClass.hashCode()));
    }

    private static native boolean nHasDragDropPayload();

    private static native boolean nHasDragDropPayload(String var0);

    public static void beginDisabled() {
        ImGui.nBeginDisabled();
    }

    public static void beginDisabled(boolean disabled) {
        ImGui.nBeginDisabled(disabled);
    }

    private static native void nBeginDisabled();

    private static native void nBeginDisabled(boolean var0);

    public static void endDisabled() {
        ImGui.nEndDisabled();
    }

    private static native void nEndDisabled();

    public static void pushClipRect(ImVec2 clipRectMin, ImVec2 clipRectMax, boolean intersectWithCurrentClipRect) {
        ImGui.nPushClipRect(clipRectMin.x, clipRectMin.y, clipRectMax.x, clipRectMax.y, intersectWithCurrentClipRect);
    }

    public static void pushClipRect(float clipRectMinX, float clipRectMinY, float clipRectMaxX, float clipRectMaxY, boolean intersectWithCurrentClipRect) {
        ImGui.nPushClipRect(clipRectMinX, clipRectMinY, clipRectMaxX, clipRectMaxY, intersectWithCurrentClipRect);
    }

    private static native void nPushClipRect(float var0, float var1, float var2, float var3, boolean var4);

    public static void popClipRect() {
        ImGui.nPopClipRect();
    }

    private static native void nPopClipRect();

    public static void setItemDefaultFocus() {
        ImGui.nSetItemDefaultFocus();
    }

    private static native void nSetItemDefaultFocus();

    public static void setKeyboardFocusHere() {
        ImGui.nSetKeyboardFocusHere();
    }

    public static void setKeyboardFocusHere(int offset) {
        ImGui.nSetKeyboardFocusHere(offset);
    }

    private static native void nSetKeyboardFocusHere();

    private static native void nSetKeyboardFocusHere(int var0);

    public static void setNextItemAllowOverlap() {
        ImGui.nSetNextItemAllowOverlap();
    }

    private static native void nSetNextItemAllowOverlap();

    public static boolean isItemHovered() {
        return ImGui.nIsItemHovered();
    }

    public static boolean isItemHovered(int imGuiHoveredFlags) {
        return ImGui.nIsItemHovered(imGuiHoveredFlags);
    }

    private static native boolean nIsItemHovered();

    private static native boolean nIsItemHovered(int var0);

    public static boolean isItemActive() {
        return ImGui.nIsItemActive();
    }

    private static native boolean nIsItemActive();

    public static boolean isItemFocused() {
        return ImGui.nIsItemFocused();
    }

    private static native boolean nIsItemFocused();

    public static boolean isItemClicked() {
        return ImGui.nIsItemClicked();
    }

    public static boolean isItemClicked(int mouseButton) {
        return ImGui.nIsItemClicked(mouseButton);
    }

    private static native boolean nIsItemClicked();

    private static native boolean nIsItemClicked(int var0);

    public static boolean isItemVisible() {
        return ImGui.nIsItemVisible();
    }

    private static native boolean nIsItemVisible();

    public static boolean isItemEdited() {
        return ImGui.nIsItemEdited();
    }

    private static native boolean nIsItemEdited();

    public static boolean isItemActivated() {
        return ImGui.nIsItemActivated();
    }

    private static native boolean nIsItemActivated();

    public static boolean isItemDeactivated() {
        return ImGui.nIsItemDeactivated();
    }

    private static native boolean nIsItemDeactivated();

    public static boolean isItemDeactivatedAfterEdit() {
        return ImGui.nIsItemDeactivatedAfterEdit();
    }

    private static native boolean nIsItemDeactivatedAfterEdit();

    public static boolean isItemToggledOpen() {
        return ImGui.nIsItemToggledOpen();
    }

    private static native boolean nIsItemToggledOpen();

    public static boolean isAnyItemHovered() {
        return ImGui.nIsAnyItemHovered();
    }

    private static native boolean nIsAnyItemHovered();

    public static boolean isAnyItemActive() {
        return ImGui.nIsAnyItemActive();
    }

    private static native boolean nIsAnyItemActive();

    public static boolean isAnyItemFocused() {
        return ImGui.nIsAnyItemFocused();
    }

    private static native boolean nIsAnyItemFocused();

    public static int getItemID() {
        return ImGui.nGetItemID();
    }

    private static native int nGetItemID();

    public static ImVec2 getItemRectMin() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetItemRectMin(dst);
        return dst;
    }

    public static float getItemRectMinX() {
        return ImGui.nGetItemRectMinX();
    }

    public static float getItemRectMinY() {
        return ImGui.nGetItemRectMinY();
    }

    public static void getItemRectMin(ImVec2 dst) {
        ImGui.nGetItemRectMin(dst);
    }

    private static native void nGetItemRectMin(ImVec2 var0);

    private static native float nGetItemRectMinX();

    private static native float nGetItemRectMinY();

    public static ImVec2 getItemRectMax() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetItemRectMax(dst);
        return dst;
    }

    public static float getItemRectMaxX() {
        return ImGui.nGetItemRectMaxX();
    }

    public static float getItemRectMaxY() {
        return ImGui.nGetItemRectMaxY();
    }

    public static void getItemRectMax(ImVec2 dst) {
        ImGui.nGetItemRectMax(dst);
    }

    private static native void nGetItemRectMax(ImVec2 var0);

    private static native float nGetItemRectMaxX();

    private static native float nGetItemRectMaxY();

    public static ImVec2 getItemRectSize() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetItemRectSize(dst);
        return dst;
    }

    public static float getItemRectSizeX() {
        return ImGui.nGetItemRectSizeX();
    }

    public static float getItemRectSizeY() {
        return ImGui.nGetItemRectSizeY();
    }

    public static void getItemRectSize(ImVec2 dst) {
        ImGui.nGetItemRectSize(dst);
    }

    private static native void nGetItemRectSize(ImVec2 var0);

    private static native float nGetItemRectSizeX();

    private static native float nGetItemRectSizeY();

    public static ImGuiViewport getMainViewport() {
        ImGui._GETMAINVIEWPORT_1.ptr = ImGui.nGetMainViewport();
        return _GETMAINVIEWPORT_1;
    }

    private static native long nGetMainViewport();

    public static ImDrawList getBackgroundDrawList() {
        return new ImDrawList(ImGui.nGetBackgroundDrawList());
    }

    public static ImDrawList getBackgroundDrawList(ImGuiViewport viewport) {
        return new ImDrawList(ImGui.nGetBackgroundDrawList(viewport.ptr));
    }

    private static native long nGetBackgroundDrawList();

    private static native long nGetBackgroundDrawList(long var0);

    public static ImDrawList getForegroundDrawList() {
        return new ImDrawList(ImGui.nGetForegroundDrawList());
    }

    public static ImDrawList getForegroundDrawList(ImGuiViewport viewport) {
        return new ImDrawList(ImGui.nGetForegroundDrawList(viewport.ptr));
    }

    private static native long nGetForegroundDrawList();

    private static native long nGetForegroundDrawList(long var0);

    public static boolean isRectVisible(ImVec2 size) {
        return ImGui.nIsRectVisible(size.x, size.y);
    }

    public static boolean isRectVisible(float sizeX, float sizeY) {
        return ImGui.nIsRectVisible(sizeX, sizeY);
    }

    private static native boolean nIsRectVisible(float var0, float var1);

    public static boolean isRectVisible(ImVec2 rectMin, ImVec2 rectMax) {
        return ImGui.nIsRectVisible(rectMin.x, rectMin.y, rectMax.x, rectMax.y);
    }

    public static boolean isRectVisible(float rectMinX, float rectMinY, float rectMaxX, float rectMaxY) {
        return ImGui.nIsRectVisible(rectMinX, rectMinY, rectMaxX, rectMaxY);
    }

    private static native boolean nIsRectVisible(float var0, float var1, float var2, float var3);

    public static double getTime() {
        return ImGui.nGetTime();
    }

    private static native double nGetTime();

    public static int getFrameCount() {
        return ImGui.nGetFrameCount();
    }

    private static native int nGetFrameCount();

    public static String getStyleColorName(int imGuiColIdx) {
        return ImGui.nGetStyleColorName(imGuiColIdx);
    }

    private static native String nGetStyleColorName(int var0);

    public static void setStateStorage(ImGuiStorage storage) {
        ImGui.nSetStateStorage(storage.ptr);
    }

    private static native void nSetStateStorage(long var0);

    public static ImGuiStorage getStateStorage() {
        return new ImGuiStorage(ImGui.nGetStateStorage());
    }

    private static native long nGetStateStorage();

    public static boolean beginChildFrame(int id, ImVec2 size) {
        return ImGui.nBeginChildFrame(id, size.x, size.y);
    }

    public static boolean beginChildFrame(int id, float sizeX, float sizeY) {
        return ImGui.nBeginChildFrame(id, sizeX, sizeY);
    }

    public static boolean beginChildFrame(int id, ImVec2 size, int imGuiWindowFlags) {
        return ImGui.nBeginChildFrame(id, size.x, size.y, imGuiWindowFlags);
    }

    public static boolean beginChildFrame(int id, float sizeX, float sizeY, int imGuiWindowFlags) {
        return ImGui.nBeginChildFrame(id, sizeX, sizeY, imGuiWindowFlags);
    }

    private static native boolean nBeginChildFrame(int var0, float var1, float var2);

    private static native boolean nBeginChildFrame(int var0, float var1, float var2, int var3);

    public static void endChildFrame() {
        ImGui.nEndChildFrame();
    }

    private static native void nEndChildFrame();

    public static ImVec2 calcTextSize(String text) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcTextSize(dst, text);
        return dst;
    }

    public static float calcTextSizeX(String text) {
        return ImGui.nCalcTextSizeX(text);
    }

    public static float calcTextSizeY(String text) {
        return ImGui.nCalcTextSizeY(text);
    }

    public static void calcTextSize(ImVec2 dst, String text) {
        ImGui.nCalcTextSize(dst, text);
    }

    public static ImVec2 calcTextSize(String text, boolean hideTextAfterDoubleHash) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcTextSize(dst, text, hideTextAfterDoubleHash);
        return dst;
    }

    public static float calcTextSizeX(String text, boolean hideTextAfterDoubleHash) {
        return ImGui.nCalcTextSizeX(text, hideTextAfterDoubleHash);
    }

    public static float calcTextSizeY(String text, boolean hideTextAfterDoubleHash) {
        return ImGui.nCalcTextSizeY(text, hideTextAfterDoubleHash);
    }

    public static void calcTextSize(ImVec2 dst, String text, boolean hideTextAfterDoubleHash) {
        ImGui.nCalcTextSize(dst, text, hideTextAfterDoubleHash);
    }

    public static ImVec2 calcTextSize(String text, boolean hideTextAfterDoubleHash, float wrapWidth) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcTextSize(dst, text, hideTextAfterDoubleHash, wrapWidth);
        return dst;
    }

    public static float calcTextSizeX(String text, boolean hideTextAfterDoubleHash, float wrapWidth) {
        return ImGui.nCalcTextSizeX(text, hideTextAfterDoubleHash, wrapWidth);
    }

    public static float calcTextSizeY(String text, boolean hideTextAfterDoubleHash, float wrapWidth) {
        return ImGui.nCalcTextSizeY(text, hideTextAfterDoubleHash, wrapWidth);
    }

    public static void calcTextSize(ImVec2 dst, String text, boolean hideTextAfterDoubleHash, float wrapWidth) {
        ImGui.nCalcTextSize(dst, text, hideTextAfterDoubleHash, wrapWidth);
    }

    public static ImVec2 calcTextSize(String text, float wrapWidth) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcTextSize(dst, text, wrapWidth);
        return dst;
    }

    public static float calcTextSizeX(String text, float wrapWidth) {
        return ImGui.nCalcTextSizeX(text, wrapWidth);
    }

    public static float calcTextSizeY(String text, float wrapWidth) {
        return ImGui.nCalcTextSizeY(text, wrapWidth);
    }

    public static void calcTextSize(ImVec2 dst, String text, float wrapWidth) {
        ImGui.nCalcTextSize(dst, text, wrapWidth);
    }

    private static native void nCalcTextSize(ImVec2 var0, String var1);

    private static native float nCalcTextSizeX(String var0);

    private static native float nCalcTextSizeY(String var0);

    private static native void nCalcTextSize(ImVec2 var0, String var1, boolean var2);

    private static native float nCalcTextSizeX(String var0, boolean var1);

    private static native float nCalcTextSizeY(String var0, boolean var1);

    private static native void nCalcTextSize(ImVec2 var0, String var1, boolean var2, float var3);

    private static native float nCalcTextSizeX(String var0, boolean var1, float var2);

    private static native float nCalcTextSizeY(String var0, boolean var1, float var2);

    private static native void nCalcTextSize(ImVec2 var0, String var1, float var2);

    private static native float nCalcTextSizeX(String var0, float var1);

    private static native float nCalcTextSizeY(String var0, float var1);

    public static ImVec4 colorConvertU32ToFloat4(int in) {
        ImVec4 dst = new ImVec4();
        ImGui.nColorConvertU32ToFloat4(dst, in);
        return dst;
    }

    public static float colorConvertU32ToFloat4X(int in) {
        return ImGui.nColorConvertU32ToFloat4X(in);
    }

    public static float colorConvertU32ToFloat4Y(int in) {
        return ImGui.nColorConvertU32ToFloat4Y(in);
    }

    public static float colorConvertU32ToFloat4Z(int in) {
        return ImGui.nColorConvertU32ToFloat4Z(in);
    }

    public static float colorConvertU32ToFloat4W(int in) {
        return ImGui.nColorConvertU32ToFloat4W(in);
    }

    public static void colorConvertU32ToFloat4(ImVec4 dst, int in) {
        ImGui.nColorConvertU32ToFloat4(dst, in);
    }

    private static native void nColorConvertU32ToFloat4(ImVec4 var0, int var1);

    private static native float nColorConvertU32ToFloat4X(int var0);

    private static native float nColorConvertU32ToFloat4Y(int var0);

    private static native float nColorConvertU32ToFloat4Z(int var0);

    private static native float nColorConvertU32ToFloat4W(int var0);

    public static int colorConvertFloat4ToU32(ImVec4 in) {
        return ImGui.nColorConvertFloat4ToU32(in.x, in.y, in.z, in.w);
    }

    public static int colorConvertFloat4ToU32(float inX, float inY, float inZ, float inW) {
        return ImGui.nColorConvertFloat4ToU32(inX, inY, inZ, inW);
    }

    private static native int nColorConvertFloat4ToU32(float var0, float var1, float var2, float var3);

    public static void colorConvertRGBtoHSV(float[] rgb, float[] hsv) {
        ImGui.nColorConvertRGBtoHSV(rgb, hsv);
    }

    public static native void nColorConvertRGBtoHSV(float[] var0, float[] var1);

    public static void colorConvertHSVtoRGB(float[] hsv, float[] rgb) {
        ImGui.nColorConvertHSVtoRGB(hsv, rgb);
    }

    public static native void nColorConvertHSVtoRGB(float[] var0, float[] var1);

    @Deprecated
    public static int getKeyIndex(int key) {
        return ImGui.nGetKeyIndex(key);
    }

    private static native int nGetKeyIndex(int var0);

    public static boolean isKeyDown(int key) {
        return ImGui.nIsKeyDown(key);
    }

    private static native boolean nIsKeyDown(int var0);

    public static boolean isKeyPressed(int key) {
        return ImGui.nIsKeyPressed(key);
    }

    public static boolean isKeyPressed(int key, boolean repeat) {
        return ImGui.nIsKeyPressed(key, repeat);
    }

    private static native boolean nIsKeyPressed(int var0);

    private static native boolean nIsKeyPressed(int var0, boolean var1);

    public static boolean isKeyReleased(int key) {
        return ImGui.nIsKeyReleased(key);
    }

    private static native boolean nIsKeyReleased(int var0);

    public static boolean getKeyPressedAmount(int key, float repeatDelay, float rate) {
        return ImGui.nGetKeyPressedAmount(key, repeatDelay, rate);
    }

    private static native boolean nGetKeyPressedAmount(int var0, float var1, float var2);

    public static String getKeyName(int key) {
        return ImGui.nGetKeyName(key);
    }

    private static native String nGetKeyName(int var0);

    public static void setNextFrameWantCaptureKeyboard(boolean wantCaptureKeyboard) {
        ImGui.nSetNextFrameWantCaptureKeyboard(wantCaptureKeyboard);
    }

    private static native void nSetNextFrameWantCaptureKeyboard(boolean var0);

    public static boolean isMouseDown(int button) {
        return ImGui.nIsMouseDown(button);
    }

    private static native boolean nIsMouseDown(int var0);

    public static boolean isMouseClicked(int button) {
        return ImGui.nIsMouseClicked(button);
    }

    public static boolean isMouseClicked(int button, boolean repeat) {
        return ImGui.nIsMouseClicked(button, repeat);
    }

    private static native boolean nIsMouseClicked(int var0);

    private static native boolean nIsMouseClicked(int var0, boolean var1);

    public static boolean isMouseReleased(int button) {
        return ImGui.nIsMouseReleased(button);
    }

    private static native boolean nIsMouseReleased(int var0);

    public static boolean isMouseDoubleClicked(int button) {
        return ImGui.nIsMouseDoubleClicked(button);
    }

    private static native boolean nIsMouseDoubleClicked(int var0);

    public static int getMouseClickedCount(int button) {
        return ImGui.nGetMouseClickedCount(button);
    }

    private static native int nGetMouseClickedCount(int var0);

    public static boolean isMouseHoveringRect(ImVec2 rMin, ImVec2 rMax) {
        return ImGui.nIsMouseHoveringRect(rMin.x, rMin.y, rMax.x, rMax.y);
    }

    public static boolean isMouseHoveringRect(float rMinX, float rMinY, float rMaxX, float rMaxY) {
        return ImGui.nIsMouseHoveringRect(rMinX, rMinY, rMaxX, rMaxY);
    }

    public static boolean isMouseHoveringRect(ImVec2 rMin, ImVec2 rMax, boolean clip) {
        return ImGui.nIsMouseHoveringRect(rMin.x, rMin.y, rMax.x, rMax.y, clip);
    }

    public static boolean isMouseHoveringRect(float rMinX, float rMinY, float rMaxX, float rMaxY, boolean clip) {
        return ImGui.nIsMouseHoveringRect(rMinX, rMinY, rMaxX, rMaxY, clip);
    }

    private static native boolean nIsMouseHoveringRect(float var0, float var1, float var2, float var3);

    private static native boolean nIsMouseHoveringRect(float var0, float var1, float var2, float var3, boolean var4);

    public static boolean isMousePosValid() {
        return ImGui.nIsMousePosValid();
    }

    public static boolean isMousePosValid(ImVec2 mousePos) {
        return ImGui.nIsMousePosValid(mousePos.x, mousePos.y);
    }

    public static boolean isMousePosValid(float mousePosX, float mousePosY) {
        return ImGui.nIsMousePosValid(mousePosX, mousePosY);
    }

    private static native boolean nIsMousePosValid();

    private static native boolean nIsMousePosValid(float var0, float var1);

    public static boolean isAnyMouseDown() {
        return ImGui.nIsAnyMouseDown();
    }

    private static native boolean nIsAnyMouseDown();

    public static ImVec2 getMousePos() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetMousePos(dst);
        return dst;
    }

    public static float getMousePosX() {
        return ImGui.nGetMousePosX();
    }

    public static float getMousePosY() {
        return ImGui.nGetMousePosY();
    }

    public static void getMousePos(ImVec2 dst) {
        ImGui.nGetMousePos(dst);
    }

    private static native void nGetMousePos(ImVec2 var0);

    private static native float nGetMousePosX();

    private static native float nGetMousePosY();

    public static ImVec2 getMousePosOnOpeningCurrentPopup() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetMousePosOnOpeningCurrentPopup(dst);
        return dst;
    }

    public static float getMousePosOnOpeningCurrentPopupX() {
        return ImGui.nGetMousePosOnOpeningCurrentPopupX();
    }

    public static float getMousePosOnOpeningCurrentPopupY() {
        return ImGui.nGetMousePosOnOpeningCurrentPopupY();
    }

    public static void getMousePosOnOpeningCurrentPopup(ImVec2 dst) {
        ImGui.nGetMousePosOnOpeningCurrentPopup(dst);
    }

    private static native void nGetMousePosOnOpeningCurrentPopup(ImVec2 var0);

    private static native float nGetMousePosOnOpeningCurrentPopupX();

    private static native float nGetMousePosOnOpeningCurrentPopupY();

    public static boolean isMouseDragging(int button) {
        return ImGui.nIsMouseDragging(button);
    }

    public static boolean isMouseDragging(int button, float lockThreshold) {
        return ImGui.nIsMouseDragging(button, lockThreshold);
    }

    private static native boolean nIsMouseDragging(int var0);

    private static native boolean nIsMouseDragging(int var0, float var1);

    public static ImVec2 getMouseDragDelta() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetMouseDragDelta(dst);
        return dst;
    }

    public static float getMouseDragDeltaX() {
        return ImGui.nGetMouseDragDeltaX();
    }

    public static float getMouseDragDeltaY() {
        return ImGui.nGetMouseDragDeltaY();
    }

    public static void getMouseDragDelta(ImVec2 dst) {
        ImGui.nGetMouseDragDelta(dst);
    }

    public static ImVec2 getMouseDragDelta(int button) {
        ImVec2 dst = new ImVec2();
        ImGui.nGetMouseDragDelta(dst, button);
        return dst;
    }

    public static float getMouseDragDeltaX(int button) {
        return ImGui.nGetMouseDragDeltaX(button);
    }

    public static float getMouseDragDeltaY(int button) {
        return ImGui.nGetMouseDragDeltaY(button);
    }

    public static void getMouseDragDelta(ImVec2 dst, int button) {
        ImGui.nGetMouseDragDelta(dst, button);
    }

    public static ImVec2 getMouseDragDelta(int button, float lockThreshold) {
        ImVec2 dst = new ImVec2();
        ImGui.nGetMouseDragDelta(dst, button, lockThreshold);
        return dst;
    }

    public static float getMouseDragDeltaX(int button, float lockThreshold) {
        return ImGui.nGetMouseDragDeltaX(button, lockThreshold);
    }

    public static float getMouseDragDeltaY(int button, float lockThreshold) {
        return ImGui.nGetMouseDragDeltaY(button, lockThreshold);
    }

    public static void getMouseDragDelta(ImVec2 dst, int button, float lockThreshold) {
        ImGui.nGetMouseDragDelta(dst, button, lockThreshold);
    }

    private static native void nGetMouseDragDelta(ImVec2 var0);

    private static native float nGetMouseDragDeltaX();

    private static native float nGetMouseDragDeltaY();

    private static native void nGetMouseDragDelta(ImVec2 var0, int var1);

    private static native float nGetMouseDragDeltaX(int var0);

    private static native float nGetMouseDragDeltaY(int var0);

    private static native void nGetMouseDragDelta(ImVec2 var0, int var1, float var2);

    private static native float nGetMouseDragDeltaX(int var0, float var1);

    private static native float nGetMouseDragDeltaY(int var0, float var1);

    public static void resetMouseDragDelta() {
        ImGui.nResetMouseDragDelta();
    }

    public static void resetMouseDragDelta(int button) {
        ImGui.nResetMouseDragDelta(button);
    }

    private static native void nResetMouseDragDelta();

    private static native void nResetMouseDragDelta(int var0);

    public static int getMouseCursor() {
        return ImGui.nGetMouseCursor();
    }

    private static native int nGetMouseCursor();

    public static void setMouseCursor(int type) {
        ImGui.nSetMouseCursor(type);
    }

    private static native void nSetMouseCursor(int var0);

    public static void setNextFrameWantCaptureMouse(boolean wantCaptureMouse) {
        ImGui.nSetNextFrameWantCaptureMouse(wantCaptureMouse);
    }

    private static native void nSetNextFrameWantCaptureMouse(boolean var0);

    public static String getClipboardText() {
        return ImGui.nGetClipboardText();
    }

    private static native String nGetClipboardText();

    public static void setClipboardText(String text) {
        ImGui.nSetClipboardText(text);
    }

    private static native void nSetClipboardText(String var0);

    public static void loadIniSettingsFromDisk(String iniFilename) {
        ImGui.nLoadIniSettingsFromDisk(iniFilename);
    }

    private static native void nLoadIniSettingsFromDisk(String var0);

    public static void loadIniSettingsFromMemory(String iniData) {
        ImGui.nLoadIniSettingsFromMemory(iniData);
    }

    public static void loadIniSettingsFromMemory(String iniData, int iniSize) {
        ImGui.nLoadIniSettingsFromMemory(iniData, iniSize);
    }

    private static native void nLoadIniSettingsFromMemory(String var0);

    private static native void nLoadIniSettingsFromMemory(String var0, int var1);

    public static void saveIniSettingsToDisk(String iniFilename) {
        ImGui.nSaveIniSettingsToDisk(iniFilename);
    }

    private static native void nSaveIniSettingsToDisk(String var0);

    public static String saveIniSettingsToMemory() {
        return ImGui.nSaveIniSettingsToMemory();
    }

    public static String saveIniSettingsToMemory(long outIniSize) {
        return ImGui.nSaveIniSettingsToMemory(outIniSize);
    }

    private static native String nSaveIniSettingsToMemory();

    private static native String nSaveIniSettingsToMemory(long var0);

    public static void debugTextEncoding(String text) {
        ImGui.nDebugTextEncoding(text);
    }

    private static native void nDebugTextEncoding(String var0);

    public static boolean debugCheckVersionAndDataLayout(String versionStr, int szIo, int szStyle, int szVec2, int szVec4, int szDrawVert, int szDrawIdx) {
        return ImGui.nDebugCheckVersionAndDataLayout(versionStr, szIo, szStyle, szVec2, szVec4, szDrawVert, szDrawIdx);
    }

    private static native boolean nDebugCheckVersionAndDataLayout(String var0, int var1, int var2, int var3, int var4, int var5, int var6);

    public static ImGuiPlatformIO getPlatformIO() {
        ImGui._GETPLATFORMIO_1.ptr = ImGui.nGetPlatformIO();
        return _GETPLATFORMIO_1;
    }

    private static native long nGetPlatformIO();

    public static void updatePlatformWindows() {
        ImGui.nUpdatePlatformWindows();
    }

    private static native void nUpdatePlatformWindows();

    public static void renderPlatformWindowsDefault() {
        ImGui.nRenderPlatformWindowsDefault();
    }

    private static native void nRenderPlatformWindowsDefault();

    public static void destroyPlatformWindows() {
        ImGui.nDestroyPlatformWindows();
    }

    private static native void nDestroyPlatformWindows();

    public static ImGuiViewport findViewportByID(int imGuiID) {
        return new ImGuiViewport(ImGui.nFindViewportByID(imGuiID));
    }

    private static native long nFindViewportByID(int var0);

    public static ImGuiViewport findViewportByPlatformHandle(long platformHandle) {
        return new ImGuiViewport(ImGui.nFindViewportByPlatformHandle(platformHandle));
    }

    private static native long nFindViewportByPlatformHandle(long var0);

    static {
        String libPath = System.getProperty(LIB_PATH_PROP);
        String libName = System.getProperty(LIB_NAME_PROP, LIB_NAME_DEFAULT);
        String fullLibName = ImGui.resolveFullLibName();
        if (libPath != null) {
            System.load(Paths.get(libPath, new String[0]).resolve(fullLibName).toAbsolutePath().toString());
        } else {
            try {
                System.loadLibrary(libName);
            }
            catch (Error | Exception e) {
                String extractedLibAbsPath = ImGui.tryLoadFromClasspath(fullLibName);
                if (extractedLibAbsPath != null) {
                    System.load(extractedLibAbsPath);
                }
                throw e;
            }
        }
        ImGui.nInitJni();
        ImFontAtlas.nInit();
        ImGuiPlatformIO.init();
        ImGui.nInitInputTextData();
        ImGui.setAssertCallback(new ImAssertCallback(){

            @Override
            public void imAssertCallback(String assertion, int line, String file) {
                System.err.println("Dear ImGui Assertion Failed: " + assertion);
                System.err.println("Assertion Located At: " + file + ":" + line);
                Thread.dumpStack();
            }
        });
        _GETIO_1 = new ImGuiIO(0L);
        _GETSTYLE_1 = new ImGuiStyle(0L);
        _GETDRAWDATA_1 = new ImDrawData(0L);
        _GETFONT_1 = new ImFont(0L);
        payloadRef = null;
        PAYLOAD_PLACEHOLDER_DATA = new byte[1];
        _GETMAINVIEWPORT_1 = new ImGuiViewport(0L);
        _GETPLATFORMIO_1 = new ImGuiPlatformIO(0L);
    }
}

