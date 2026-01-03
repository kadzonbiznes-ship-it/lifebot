/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.internal.ImGuiDockNode
 *  imgui.internal.ImGuiWindow
 */
package imgui.internal;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGuiPlatformMonitor;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.internal.ImGuiDockNode;
import imgui.internal.ImGuiWindow;
import imgui.internal.ImRect;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;

public final class ImGui
extends imgui.ImGui {
    public static void init() {
    }

    private static native void nInit();

    public static ImGuiWindow getCurrentWindowRead() {
        return new ImGuiWindow(ImGui.nGetCurrentWindowRead());
    }

    private static native long nGetCurrentWindowRead();

    public static ImGuiWindow getCurrentWindow() {
        return new ImGuiWindow(ImGui.nGetCurrentWindow());
    }

    private static native long nGetCurrentWindow();

    public static ImGuiWindow findWindowByID(int id) {
        return new ImGuiWindow(ImGui.nFindWindowByID(id));
    }

    private static native long nFindWindowByID(int var0);

    public static ImGuiWindow findWindowByName(String name) {
        return new ImGuiWindow(ImGui.nFindWindowByName(name));
    }

    private static native long nFindWindowByName(String var0);

    public static void updateWindowParentAndRootLinks(ImGuiWindow window, int flags, ImGuiWindow parentWindow) {
        ImGui.nUpdateWindowParentAndRootLinks(window.ptr, flags, parentWindow.ptr);
    }

    private static native void nUpdateWindowParentAndRootLinks(long var0, int var2, long var3);

    public static ImVec2 calcWindowNextAutoFitSize(ImGuiWindow window) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcWindowNextAutoFitSize(dst, window.ptr);
        return dst;
    }

    public static float calcWindowNextAutoFitSizeX(ImGuiWindow window) {
        return ImGui.nCalcWindowNextAutoFitSizeX(window.ptr);
    }

    public static float calcWindowNextAutoFitSizeY(ImGuiWindow window) {
        return ImGui.nCalcWindowNextAutoFitSizeY(window.ptr);
    }

    public static void calcWindowNextAutoFitSize(ImVec2 dst, ImGuiWindow window) {
        ImGui.nCalcWindowNextAutoFitSize(dst, window.ptr);
    }

    private static native void nCalcWindowNextAutoFitSize(ImVec2 var0, long var1);

    private static native float nCalcWindowNextAutoFitSizeX(long var0);

    private static native float nCalcWindowNextAutoFitSizeY(long var0);

    public static boolean isWindowChildOf(ImGuiWindow window, ImGuiWindow potentialParent, boolean popupHierarchy, boolean dockHierarchy) {
        return ImGui.nIsWindowChildOf(window.ptr, potentialParent.ptr, popupHierarchy, dockHierarchy);
    }

    private static native boolean nIsWindowChildOf(long var0, long var2, boolean var4, boolean var5);

    public static boolean isWindowWithinBeginStackOf(ImGuiWindow window, ImGuiWindow potentialParent) {
        return ImGui.nIsWindowWithinBeginStackOf(window.ptr, potentialParent.ptr);
    }

    private static native boolean nIsWindowWithinBeginStackOf(long var0, long var2);

    public static boolean isWindowAbove(ImGuiWindow potentialAbove, ImGuiWindow potentialBelow) {
        return ImGui.nIsWindowAbove(potentialAbove.ptr, potentialBelow.ptr);
    }

    private static native boolean nIsWindowAbove(long var0, long var2);

    public static boolean isWindowNavFocusable(ImGuiWindow window) {
        return ImGui.nIsWindowNavFocusable(window.ptr);
    }

    private static native boolean nIsWindowNavFocusable(long var0);

    public static void setWindowPos(ImGuiWindow window, ImVec2 pos) {
        ImGui.nSetWindowPos(window.ptr, pos.x, pos.y);
    }

    public static void setWindowPos(ImGuiWindow window, float posX, float posY) {
        ImGui.nSetWindowPos(window.ptr, posX, posY);
    }

    public static void setWindowPos(ImGuiWindow window, ImVec2 pos, int cond) {
        ImGui.nSetWindowPos(window.ptr, pos.x, pos.y, cond);
    }

    public static void setWindowPos(ImGuiWindow window, float posX, float posY, int cond) {
        ImGui.nSetWindowPos(window.ptr, posX, posY, cond);
    }

    private static native void nSetWindowPos(long var0, float var2, float var3);

    private static native void nSetWindowPos(long var0, float var2, float var3, int var4);

    public static void setWindowSize(ImGuiWindow window, ImVec2 size) {
        ImGui.nSetWindowSize(window.ptr, size.x, size.y);
    }

    public static void setWindowSize(ImGuiWindow window, float sizeX, float sizeY) {
        ImGui.nSetWindowSize(window.ptr, sizeX, sizeY);
    }

    public static void setWindowSize(ImGuiWindow window, ImVec2 size, int cond) {
        ImGui.nSetWindowSize(window.ptr, size.x, size.y, cond);
    }

    public static void setWindowSize(ImGuiWindow window, float sizeX, float sizeY, int cond) {
        ImGui.nSetWindowSize(window.ptr, sizeX, sizeY, cond);
    }

    private static native void nSetWindowSize(long var0, float var2, float var3);

    private static native void nSetWindowSize(long var0, float var2, float var3, int var4);

    public static void setWindowCollapsed(ImGuiWindow window, boolean collapsed) {
        ImGui.nSetWindowCollapsed(window.ptr, collapsed);
    }

    public static void setWindowCollapsed(ImGuiWindow window, boolean collapsed, int cond) {
        ImGui.nSetWindowCollapsed(window.ptr, collapsed, cond);
    }

    private static native void nSetWindowCollapsed(long var0, boolean var2);

    private static native void nSetWindowCollapsed(long var0, boolean var2, int var3);

    public static void setWindowHitTestHole(ImGuiWindow window, ImVec2 pos, ImVec2 size) {
        ImGui.nSetWindowHitTestHole(window.ptr, pos.x, pos.y, size.x, size.y);
    }

    public static void setWindowHitTestHole(ImGuiWindow window, float posX, float posY, float sizeX, float sizeY) {
        ImGui.nSetWindowHitTestHole(window.ptr, posX, posY, sizeX, sizeY);
    }

    private static native void nSetWindowHitTestHole(long var0, float var2, float var3, float var4, float var5);

    public static ImRect windowRectAbsToRel(ImGuiWindow window, ImRect r) {
        ImRect dst = new ImRect();
        ImGui.nWindowRectAbsToRel(dst, window.ptr, r.min.x, r.min.y, r.max.x, r.max.y);
        return dst;
    }

    public static ImRect windowRectAbsToRel(ImGuiWindow window, float rMinX, float rMinY, float rMaxX, float rMaxY) {
        ImRect dst = new ImRect();
        ImGui.nWindowRectAbsToRel(dst, window.ptr, rMinX, rMinY, rMaxX, rMaxY);
        return dst;
    }

    public static void windowRectAbsToRel(ImRect dst, ImGuiWindow window, ImRect r) {
        ImGui.nWindowRectAbsToRel(dst, window.ptr, r.min.x, r.min.y, r.max.x, r.max.y);
    }

    public static void windowRectAbsToRel(ImRect dst, ImGuiWindow window, float rMinX, float rMinY, float rMaxX, float rMaxY) {
        ImGui.nWindowRectAbsToRel(dst, window.ptr, rMinX, rMinY, rMaxX, rMaxY);
    }

    private static native void nWindowRectAbsToRel(ImRect var0, long var1, float var3, float var4, float var5, float var6);

    public static ImRect windowRectRelToAbs(ImGuiWindow window, ImRect r) {
        ImRect dst = new ImRect();
        ImGui.nWindowRectRelToAbs(dst, window.ptr, r.min.x, r.min.y, r.max.x, r.max.y);
        return dst;
    }

    public static ImRect windowRectRelToAbs(ImGuiWindow window, float rMinX, float rMinY, float rMaxX, float rMaxY) {
        ImRect dst = new ImRect();
        ImGui.nWindowRectRelToAbs(dst, window.ptr, rMinX, rMinY, rMaxX, rMaxY);
        return dst;
    }

    public static void windowRectRelToAbs(ImRect dst, ImGuiWindow window, ImRect r) {
        ImGui.nWindowRectRelToAbs(dst, window.ptr, r.min.x, r.min.y, r.max.x, r.max.y);
    }

    public static void windowRectRelToAbs(ImRect dst, ImGuiWindow window, float rMinX, float rMinY, float rMaxX, float rMaxY) {
        ImGui.nWindowRectRelToAbs(dst, window.ptr, rMinX, rMinY, rMaxX, rMaxY);
    }

    private static native void nWindowRectRelToAbs(ImRect var0, long var1, float var3, float var4, float var5, float var6);

    public static void focusWindow(ImGuiWindow window) {
        ImGui.nFocusWindow(window.ptr);
    }

    public static void focusWindow(ImGuiWindow window, int flags) {
        ImGui.nFocusWindow(window.ptr, flags);
    }

    private static native void nFocusWindow(long var0);

    private static native void nFocusWindow(long var0, int var2);

    public static void focusTopMostWindowUnderOne(ImGuiWindow underThisWindow, ImGuiWindow ignoreWindow, ImGuiViewport filterViewport, int flags) {
        ImGui.nFocusTopMostWindowUnderOne(underThisWindow.ptr, ignoreWindow.ptr, filterViewport.ptr, flags);
    }

    private static native void nFocusTopMostWindowUnderOne(long var0, long var2, long var4, int var6);

    public static void bringWindowToFocusFront(ImGuiWindow window) {
        ImGui.nBringWindowToFocusFront(window.ptr);
    }

    private static native void nBringWindowToFocusFront(long var0);

    public static void bringWindowToDisplayFront(ImGuiWindow window) {
        ImGui.nBringWindowToDisplayFront(window.ptr);
    }

    private static native void nBringWindowToDisplayFront(long var0);

    public static void bringWindowToDisplayBack(ImGuiWindow window) {
        ImGui.nBringWindowToDisplayBack(window.ptr);
    }

    private static native void nBringWindowToDisplayBack(long var0);

    public static void bringWindowToDisplayBehind(ImGuiWindow window, ImGuiWindow aboveWindow) {
        ImGui.nBringWindowToDisplayBehind(window.ptr, aboveWindow.ptr);
    }

    private static native void nBringWindowToDisplayBehind(long var0, long var2);

    public static int findWindowDisplayIndex(ImGuiWindow window) {
        return ImGui.nFindWindowDisplayIndex(window.ptr);
    }

    private static native int nFindWindowDisplayIndex(long var0);

    public static ImGuiWindow findBottomMostVisibleWindowWithinBeginStack(ImGuiWindow window) {
        return new ImGuiWindow(ImGui.nFindBottomMostVisibleWindowWithinBeginStack(window.ptr));
    }

    private static native long nFindBottomMostVisibleWindowWithinBeginStack(long var0);

    public static void setCurrentFont(ImFont font) {
        ImGui.nSetCurrentFont(font.ptr);
    }

    private static native void nSetCurrentFont(long var0);

    public static ImFont getDefaultFont() {
        return new ImFont(ImGui.nGetDefaultFont());
    }

    private static native long nGetDefaultFont();

    public static ImDrawList getForegroundDrawList(ImGuiWindow window) {
        return new ImDrawList(ImGui.nGetForegroundDrawList(window.ptr));
    }

    private static native long nGetForegroundDrawList(long var0);

    public static void initialize() {
        ImGui.nInitialize();
    }

    private static native void nInitialize();

    public static void shutdown() {
        ImGui.nShutdown();
    }

    private static native void nShutdown();

    public static void updateInputEvents(boolean trickleFastInputs) {
        ImGui.nUpdateInputEvents(trickleFastInputs);
    }

    private static native void nUpdateInputEvents(boolean var0);

    public static void updateHoveredWindowAndCaptureFlags() {
        ImGui.nUpdateHoveredWindowAndCaptureFlags();
    }

    private static native void nUpdateHoveredWindowAndCaptureFlags();

    public static void startMouseMovingWindow(ImGuiWindow window) {
        ImGui.nStartMouseMovingWindow(window.ptr);
    }

    private static native void nStartMouseMovingWindow(long var0);

    public static void startMouseMovingWindowOrNode(ImGuiWindow window, ImGuiDockNode node, boolean undockFloatingNode) {
        ImGui.nStartMouseMovingWindowOrNode(window.ptr, node.ptr, undockFloatingNode);
    }

    private static native void nStartMouseMovingWindowOrNode(long var0, long var2, boolean var4);

    public static void updateMouseMovingWindowNewFrame() {
        ImGui.nUpdateMouseMovingWindowNewFrame();
    }

    private static native void nUpdateMouseMovingWindowNewFrame();

    public static void updateMouseMovingWindowEndFrame() {
        ImGui.nUpdateMouseMovingWindowEndFrame();
    }

    private static native void nUpdateMouseMovingWindowEndFrame();

    public static ImGuiPlatformMonitor getViewportPlatformMonitor(ImGuiViewport viewport) {
        return new ImGuiPlatformMonitor(ImGui.nGetViewportPlatformMonitor(viewport.ptr));
    }

    private static native long nGetViewportPlatformMonitor(long var0);

    public static void setNextWindowScroll(ImVec2 scroll) {
        ImGui.nSetNextWindowScroll(scroll.x, scroll.y);
    }

    public static void setNextWindowScroll(float scrollX, float scrollY) {
        ImGui.nSetNextWindowScroll(scrollX, scrollY);
    }

    private static native void nSetNextWindowScroll(float var0, float var1);

    public static void setScrollX(ImGuiWindow window, float scrollX) {
        ImGui.nSetScrollX(window.ptr, scrollX);
    }

    private static native void nSetScrollX(long var0, float var2);

    public static void setScrollY(ImGuiWindow window, float scrollY) {
        ImGui.nSetScrollY(window.ptr, scrollY);
    }

    private static native void nSetScrollY(long var0, float var2);

    public static void setScrollFromPosX(ImGuiWindow window, float localX, float centerXRatio) {
        ImGui.nSetScrollFromPosX(window.ptr, localX, centerXRatio);
    }

    private static native void nSetScrollFromPosX(long var0, float var2, float var3);

    public static void setScrollFromPosY(ImGuiWindow window, float localY, float centerYRatio) {
        ImGui.nSetScrollFromPosY(window.ptr, localY, centerYRatio);
    }

    private static native void nSetScrollFromPosY(long var0, float var2, float var3);

    public static void scrollToItem() {
        ImGui.nScrollToItem();
    }

    public static void scrollToItem(int flags) {
        ImGui.nScrollToItem(flags);
    }

    private static native void nScrollToItem();

    private static native void nScrollToItem(int var0);

    public static void scrollToRect(ImGuiWindow window, ImRect rect) {
        ImGui.nScrollToRect(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y);
    }

    public static void scrollToRect(ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY) {
        ImGui.nScrollToRect(window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY);
    }

    public static void scrollToRect(ImGuiWindow window, ImRect rect, int flags) {
        ImGui.nScrollToRect(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y, flags);
    }

    public static void scrollToRect(ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, int flags) {
        ImGui.nScrollToRect(window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY, flags);
    }

    private static native void nScrollToRect(long var0, float var2, float var3, float var4, float var5);

    private static native void nScrollToRect(long var0, float var2, float var3, float var4, float var5, int var6);

    public static ImVec2 scrollToRectEx(ImGuiWindow window, ImRect rect) {
        ImVec2 dst = new ImVec2();
        ImGui.nScrollToRectEx(dst, window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y);
        return dst;
    }

    public static ImVec2 scrollToRectEx(ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY) {
        ImVec2 dst = new ImVec2();
        ImGui.nScrollToRectEx(dst, window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY);
        return dst;
    }

    public static float scrollToRectExX(ImGuiWindow window, ImRect rect) {
        return ImGui.nScrollToRectExX(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y);
    }

    public static float scrollToRectExY(ImGuiWindow window, ImRect rect) {
        return ImGui.nScrollToRectExY(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y);
    }

    public static void scrollToRectEx(ImVec2 dst, ImGuiWindow window, ImRect rect) {
        ImGui.nScrollToRectEx(dst, window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y);
    }

    public static void scrollToRectEx(ImVec2 dst, ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY) {
        ImGui.nScrollToRectEx(dst, window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY);
    }

    public static ImVec2 scrollToRectEx(ImGuiWindow window, ImRect rect, int flags) {
        ImVec2 dst = new ImVec2();
        ImGui.nScrollToRectEx(dst, window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y, flags);
        return dst;
    }

    public static ImVec2 scrollToRectEx(ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, int flags) {
        ImVec2 dst = new ImVec2();
        ImGui.nScrollToRectEx(dst, window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY, flags);
        return dst;
    }

    public static float scrollToRectExX(ImGuiWindow window, ImRect rect, int flags) {
        return ImGui.nScrollToRectExX(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y, flags);
    }

    public static float scrollToRectExY(ImGuiWindow window, ImRect rect, int flags) {
        return ImGui.nScrollToRectExY(window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y, flags);
    }

    public static void scrollToRectEx(ImVec2 dst, ImGuiWindow window, ImRect rect, int flags) {
        ImGui.nScrollToRectEx(dst, window.ptr, rect.min.x, rect.min.y, rect.max.x, rect.max.y, flags);
    }

    public static void scrollToRectEx(ImVec2 dst, ImGuiWindow window, float rectMinX, float rectMinY, float rectMaxX, float rectMaxY, int flags) {
        ImGui.nScrollToRectEx(dst, window.ptr, rectMinX, rectMinY, rectMaxX, rectMaxY, flags);
    }

    private static native void nScrollToRectEx(ImVec2 var0, long var1, float var3, float var4, float var5, float var6);

    private static native float nScrollToRectExX(long var0, float var2, float var3, float var4, float var5);

    private static native float nScrollToRectExY(long var0, float var2, float var3, float var4, float var5);

    private static native void nScrollToRectEx(ImVec2 var0, long var1, float var3, float var4, float var5, float var6, int var7);

    private static native float nScrollToRectExX(long var0, float var2, float var3, float var4, float var5, int var6);

    private static native float nScrollToRectExY(long var0, float var2, float var3, float var4, float var5, int var6);

    public static int getItemStatusFlags() {
        return ImGui.nGetItemStatusFlags();
    }

    private static native int nGetItemStatusFlags();

    public static int getItemFlags() {
        return ImGui.nGetItemFlags();
    }

    private static native int nGetItemFlags();

    public static int getActiveID() {
        return ImGui.nGetActiveID();
    }

    private static native int nGetActiveID();

    public static int getFocusID() {
        return ImGui.nGetFocusID();
    }

    private static native int nGetFocusID();

    public static void setActiveID(int id, ImGuiWindow window) {
        ImGui.nSetActiveID(id, window.ptr);
    }

    private static native void nSetActiveID(int var0, long var1);

    public static void setFocusID(int id, ImGuiWindow window) {
        ImGui.nSetFocusID(id, window.ptr);
    }

    private static native void nSetFocusID(int var0, long var1);

    public static void clearActiveID() {
        ImGui.nClearActiveID();
    }

    private static native void nClearActiveID();

    public static int getHoveredID() {
        return ImGui.nGetHoveredID();
    }

    private static native int nGetHoveredID();

    public static void setHoveredID(int id) {
        ImGui.nSetHoveredID(id);
    }

    private static native void nSetHoveredID(int var0);

    public static void keepAliveID(int id) {
        ImGui.nKeepAliveID(id);
    }

    private static native void nKeepAliveID(int var0);

    public static void markItemEdited(int id) {
        ImGui.nMarkItemEdited(id);
    }

    private static native void nMarkItemEdited(int var0);

    public static void pushOverrideID(int id) {
        ImGui.nPushOverrideID(id);
    }

    private static native void nPushOverrideID(int var0);

    public static int getIDWithSeed(String strIdBegin, String strIdEnd, int seed) {
        return ImGui.nGetIDWithSeed(strIdBegin, strIdEnd, seed);
    }

    private static native int nGetIDWithSeed(String var0, String var1, int var2);

    public static void itemSize(ImVec2 size) {
        ImGui.nItemSize(size.x, size.y);
    }

    public static void itemSize(float sizeX, float sizeY) {
        ImGui.nItemSize(sizeX, sizeY);
    }

    public static void itemSize(ImVec2 size, float textBaselineY) {
        ImGui.nItemSize(size.x, size.y, textBaselineY);
    }

    public static void itemSize(float sizeX, float sizeY, float textBaselineY) {
        ImGui.nItemSize(sizeX, sizeY, textBaselineY);
    }

    private static native void nItemSize(float var0, float var1);

    private static native void nItemSize(float var0, float var1, float var2);

    public static void itemSize(ImRect bb) {
        ImGui.nItemSize(bb.min.x, bb.min.y, bb.max.x, bb.max.y);
    }

    public static void itemSize(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY) {
        ImGui.nItemSize(bbMinX, bbMinY, bbMaxX, bbMaxY);
    }

    public static void itemSize(ImRect bb, float textBaselineY) {
        ImGui.nItemSize(bb.min.x, bb.min.y, bb.max.x, bb.max.y, textBaselineY);
    }

    public static void itemSize(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, float textBaselineY) {
        ImGui.nItemSize(bbMinX, bbMinY, bbMaxX, bbMaxY, textBaselineY);
    }

    private static native void nItemSize(float var0, float var1, float var2, float var3);

    private static native void nItemSize(float var0, float var1, float var2, float var3, float var4);

    public static boolean itemHoverable(ImRect bb, int id, int itemFlags) {
        return ImGui.nItemHoverable(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, itemFlags);
    }

    public static boolean itemHoverable(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int itemFlags) {
        return ImGui.nItemHoverable(bbMinX, bbMinY, bbMaxX, bbMaxY, id, itemFlags);
    }

    private static native boolean nItemHoverable(float var0, float var1, float var2, float var3, int var4, int var5);

    public static boolean isWindowContentHoverable(ImGuiWindow window, int flags) {
        return ImGui.nIsWindowContentHoverable(window.ptr, flags);
    }

    private static native boolean nIsWindowContentHoverable(long var0, int var2);

    public static boolean isClippedEx(ImRect bb, int id) {
        return ImGui.nIsClippedEx(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id);
    }

    public static boolean isClippedEx(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id) {
        return ImGui.nIsClippedEx(bbMinX, bbMinY, bbMaxX, bbMaxY, id);
    }

    private static native boolean nIsClippedEx(float var0, float var1, float var2, float var3, int var4);

    public static void setLastItemData(int itemId, int inFlags, int statusFlags, ImRect itemRect) {
        ImGui.nSetLastItemData(itemId, inFlags, statusFlags, itemRect.min.x, itemRect.min.y, itemRect.max.x, itemRect.max.y);
    }

    public static void setLastItemData(int itemId, int inFlags, int statusFlags, float itemRectMinX, float itemRectMinY, float itemRectMaxX, float itemRectMaxY) {
        ImGui.nSetLastItemData(itemId, inFlags, statusFlags, itemRectMinX, itemRectMinY, itemRectMaxX, itemRectMaxY);
    }

    private static native void nSetLastItemData(int var0, int var1, int var2, float var3, float var4, float var5, float var6);

    public static ImVec2 calcItemSize(ImVec2 size, float defaultW, float defaultH) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcItemSize(dst, size.x, size.y, defaultW, defaultH);
        return dst;
    }

    public static ImVec2 calcItemSize(float sizeX, float sizeY, float defaultW, float defaultH) {
        ImVec2 dst = new ImVec2();
        ImGui.nCalcItemSize(dst, sizeX, sizeY, defaultW, defaultH);
        return dst;
    }

    public static float calcItemSizeX(ImVec2 size, float defaultW, float defaultH) {
        return ImGui.nCalcItemSizeX(size.x, size.y, defaultW, defaultH);
    }

    public static float calcItemSizeY(ImVec2 size, float defaultW, float defaultH) {
        return ImGui.nCalcItemSizeY(size.x, size.y, defaultW, defaultH);
    }

    public static void calcItemSize(ImVec2 dst, ImVec2 size, float defaultW, float defaultH) {
        ImGui.nCalcItemSize(dst, size.x, size.y, defaultW, defaultH);
    }

    public static void calcItemSize(ImVec2 dst, float sizeX, float sizeY, float defaultW, float defaultH) {
        ImGui.nCalcItemSize(dst, sizeX, sizeY, defaultW, defaultH);
    }

    private static native void nCalcItemSize(ImVec2 var0, float var1, float var2, float var3, float var4);

    private static native float nCalcItemSizeX(float var0, float var1, float var2, float var3);

    private static native float nCalcItemSizeY(float var0, float var1, float var2, float var3);

    public static float calcWrapWidthForPos(ImVec2 pos, float wrapPosX) {
        return ImGui.nCalcWrapWidthForPos(pos.x, pos.y, wrapPosX);
    }

    public static float calcWrapWidthForPos(float posX, float posY, float wrapPosX) {
        return ImGui.nCalcWrapWidthForPos(posX, posY, wrapPosX);
    }

    private static native float nCalcWrapWidthForPos(float var0, float var1, float var2);

    public static void pushMultiItemsWidths(int components, float widthFull) {
        ImGui.nPushMultiItemsWidths(components, widthFull);
    }

    private static native void nPushMultiItemsWidths(int var0, float var1);

    public static boolean isItemToggledSelection() {
        return ImGui.nIsItemToggledSelection();
    }

    private static native boolean nIsItemToggledSelection();

    public static ImVec2 getContentRegionMaxAbs() {
        ImVec2 dst = new ImVec2();
        ImGui.nGetContentRegionMaxAbs(dst);
        return dst;
    }

    public static float getContentRegionMaxAbsX() {
        return ImGui.nGetContentRegionMaxAbsX();
    }

    public static float getContentRegionMaxAbsY() {
        return ImGui.nGetContentRegionMaxAbsY();
    }

    public static void getContentRegionMaxAbs(ImVec2 dst) {
        ImGui.nGetContentRegionMaxAbs(dst);
    }

    private static native void nGetContentRegionMaxAbs(ImVec2 var0);

    private static native float nGetContentRegionMaxAbsX();

    private static native float nGetContentRegionMaxAbsY();

    public static void pushItemFlag(int option, boolean enabled) {
        ImGui.nPushItemFlag(option, enabled);
    }

    private static native void nPushItemFlag(int var0, boolean var1);

    public static void popItemFlag() {
        ImGui.nPopItemFlag();
    }

    private static native void nPopItemFlag();

    public static void dockBuilderDockWindow(String windowName, int nodeId) {
        ImGui.nDockBuilderDockWindow(windowName, nodeId);
    }

    private static native void nDockBuilderDockWindow(String var0, int var1);

    public static ImGuiDockNode dockBuilderGetNode(int nodeId) {
        return new ImGuiDockNode(ImGui.nDockBuilderGetNode(nodeId));
    }

    private static native long nDockBuilderGetNode(int var0);

    public static ImGuiDockNode dockBuilderGetCentralNode(int nodeId) {
        return new ImGuiDockNode(ImGui.nDockBuilderGetCentralNode(nodeId));
    }

    private static native long nDockBuilderGetCentralNode(int var0);

    public static int dockBuilderAddNode() {
        return ImGui.nDockBuilderAddNode();
    }

    public static int dockBuilderAddNode(int nodeId) {
        return ImGui.nDockBuilderAddNode(nodeId);
    }

    public static int dockBuilderAddNode(int nodeId, int flags) {
        return ImGui.nDockBuilderAddNode(nodeId, flags);
    }

    private static native int nDockBuilderAddNode();

    private static native int nDockBuilderAddNode(int var0);

    private static native int nDockBuilderAddNode(int var0, int var1);

    public static void dockBuilderRemoveNode(int nodeId) {
        ImGui.nDockBuilderRemoveNode(nodeId);
    }

    private static native void nDockBuilderRemoveNode(int var0);

    public static void dockBuilderRemoveNodeDockedWindows(int nodeId) {
        ImGui.nDockBuilderRemoveNodeDockedWindows(nodeId);
    }

    public static void dockBuilderRemoveNodeDockedWindows(int nodeId, boolean clearSettingsRefs) {
        ImGui.nDockBuilderRemoveNodeDockedWindows(nodeId, clearSettingsRefs);
    }

    private static native void nDockBuilderRemoveNodeDockedWindows(int var0);

    private static native void nDockBuilderRemoveNodeDockedWindows(int var0, boolean var1);

    public static void dockBuilderRemoveNodeChildNodes(int nodeId) {
        ImGui.nDockBuilderRemoveNodeChildNodes(nodeId);
    }

    private static native void nDockBuilderRemoveNodeChildNodes(int var0);

    public static void dockBuilderSetNodePos(int nodeId, ImVec2 pos) {
        ImGui.nDockBuilderSetNodePos(nodeId, pos.x, pos.y);
    }

    public static void dockBuilderSetNodePos(int nodeId, float posX, float posY) {
        ImGui.nDockBuilderSetNodePos(nodeId, posX, posY);
    }

    private static native void nDockBuilderSetNodePos(int var0, float var1, float var2);

    public static void dockBuilderSetNodeSize(int nodeId, ImVec2 size) {
        ImGui.nDockBuilderSetNodeSize(nodeId, size.x, size.y);
    }

    public static void dockBuilderSetNodeSize(int nodeId, float sizeX, float sizeY) {
        ImGui.nDockBuilderSetNodeSize(nodeId, sizeX, sizeY);
    }

    private static native void nDockBuilderSetNodeSize(int var0, float var1, float var2);

    public static int dockBuilderSplitNode(int nodeId, int splitDir, float sizeRatioForNodeAtDir, ImInt outIdAtDir, ImInt outIdAtOppositeDir) {
        return ImGui.nDockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir, outIdAtDir != null ? outIdAtDir.getData() : null, outIdAtOppositeDir != null ? outIdAtOppositeDir.getData() : null);
    }

    private static native int nDockBuilderSplitNode(int var0, int var1, float var2, int[] var3, int[] var4);

    public static void dockBuilderCopyWindowSettings(String srcName, String dstName) {
        ImGui.nDockBuilderCopyWindowSettings(srcName, dstName);
    }

    private static native void nDockBuilderCopyWindowSettings(String var0, String var1);

    public static void dockBuilderFinish(int nodeId) {
        ImGui.nDockBuilderFinish(nodeId);
    }

    private static native void nDockBuilderFinish(int var0);

    public static void tableOpenContextMenu() {
        ImGui.nTableOpenContextMenu();
    }

    public static void tableOpenContextMenu(int columnN) {
        ImGui.nTableOpenContextMenu(columnN);
    }

    private static native void nTableOpenContextMenu();

    private static native void nTableOpenContextMenu(int var0);

    public static void tableSetColumnWidth(int columnN, float width) {
        ImGui.nTableSetColumnWidth(columnN, width);
    }

    private static native void nTableSetColumnWidth(int var0, float var1);

    public static void tableSetColumnSortDirection(int columnN, int sortDirection, boolean appendToSortSpecs) {
        ImGui.nTableSetColumnSortDirection(columnN, sortDirection, appendToSortSpecs);
    }

    private static native void nTableSetColumnSortDirection(int var0, int var1, boolean var2);

    public static int tableGetHoveredColumn() {
        return ImGui.nTableGetHoveredColumn();
    }

    private static native int nTableGetHoveredColumn();

    public static int tableGetHoveredRow() {
        return ImGui.nTableGetHoveredRow();
    }

    private static native int nTableGetHoveredRow();

    public static float tableGetHeaderRowHeight() {
        return ImGui.nTableGetHeaderRowHeight();
    }

    private static native float nTableGetHeaderRowHeight();

    public static void tablePushBackgroundChannel() {
        ImGui.nTablePushBackgroundChannel();
    }

    private static native void nTablePushBackgroundChannel();

    public static void tablePopBackgroundChannel() {
        ImGui.nTablePopBackgroundChannel();
    }

    private static native void nTablePopBackgroundChannel();

    public static void textEx(String beginText) {
        ImGui.nTextEx(beginText);
    }

    public static void textEx(String beginText, String endText) {
        ImGui.nTextEx(beginText, endText);
    }

    public static void textEx(String beginText, String endText, int flags) {
        ImGui.nTextEx(beginText, endText, flags);
    }

    private static native void nTextEx(String var0);

    private static native void nTextEx(String var0, String var1);

    private static native void nTextEx(String var0, String var1, int var2);

    public static boolean buttonEx(String label) {
        return ImGui.nButtonEx(label);
    }

    public static boolean buttonEx(String label, ImVec2 size) {
        return ImGui.nButtonEx(label, size.x, size.y);
    }

    public static boolean buttonEx(String label, float sizeX, float sizeY) {
        return ImGui.nButtonEx(label, sizeX, sizeY);
    }

    public static boolean buttonEx(String label, ImVec2 size, int flags) {
        return ImGui.nButtonEx(label, size.x, size.y, flags);
    }

    public static boolean buttonEx(String label, float sizeX, float sizeY, int flags) {
        return ImGui.nButtonEx(label, sizeX, sizeY, flags);
    }

    public static boolean buttonEx(String label, int flags) {
        return ImGui.nButtonEx(label, flags);
    }

    private static native boolean nButtonEx(String var0);

    private static native boolean nButtonEx(String var0, float var1, float var2);

    private static native boolean nButtonEx(String var0, float var1, float var2, int var3);

    private static native boolean nButtonEx(String var0, int var1);

    public static boolean arrowButtonEx(String strId, int dir, ImVec2 size) {
        return ImGui.nArrowButtonEx(strId, dir, size.x, size.y);
    }

    public static boolean arrowButtonEx(String strId, int dir, float sizeX, float sizeY) {
        return ImGui.nArrowButtonEx(strId, dir, sizeX, sizeY);
    }

    public static boolean arrowButtonEx(String strId, int dir, ImVec2 size, int flags) {
        return ImGui.nArrowButtonEx(strId, dir, size.x, size.y, flags);
    }

    public static boolean arrowButtonEx(String strId, int dir, float sizeX, float sizeY, int flags) {
        return ImGui.nArrowButtonEx(strId, dir, sizeX, sizeY, flags);
    }

    private static native boolean nArrowButtonEx(String var0, int var1, float var2, float var3);

    private static native boolean nArrowButtonEx(String var0, int var1, float var2, float var3, int var4);

    public static boolean imageButtonEx(int id, long textureId, ImVec2 size, ImVec2 uv0, ImVec2 uv1, ImVec4 bgCol, ImVec4 tintCol, int flags) {
        return ImGui.nImageButtonEx(id, textureId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, bgCol.x, bgCol.y, bgCol.z, bgCol.w, tintCol.x, tintCol.y, tintCol.z, tintCol.w, flags);
    }

    public static boolean imageButtonEx(int id, long textureId, float sizeX, float sizeY, float uv0X, float uv0Y, float uv1X, float uv1Y, float bgColX, float bgColY, float bgColZ, float bgColW, float tintColX, float tintColY, float tintColZ, float tintColW, int flags) {
        return ImGui.nImageButtonEx(id, textureId, sizeX, sizeY, uv0X, uv0Y, uv1X, uv1Y, bgColX, bgColY, bgColZ, bgColW, tintColX, tintColY, tintColZ, tintColW, flags);
    }

    private static native boolean nImageButtonEx(int var0, long var1, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16, int var17);

    public static void separatorEx(int flags) {
        ImGui.nSeparatorEx(flags);
    }

    public static void separatorEx(int flags, float thickness) {
        ImGui.nSeparatorEx(flags, thickness);
    }

    private static native void nSeparatorEx(int var0);

    private static native void nSeparatorEx(int var0, float var1);

    public static void separatorTextEx(int id, String label, String labelEnd, float extraWidth) {
        ImGui.nSeparatorTextEx(id, label, labelEnd, extraWidth);
    }

    private static native void nSeparatorTextEx(int var0, String var1, String var2, float var3);

    public static boolean closeButton(int id, ImVec2 pos) {
        return ImGui.nCloseButton(id, pos.x, pos.y);
    }

    public static boolean closeButton(int id, float posX, float posY) {
        return ImGui.nCloseButton(id, posX, posY);
    }

    private static native boolean nCloseButton(int var0, float var1, float var2);

    public static boolean collapseButton(int id, ImVec2 pos, ImGuiDockNode dockNode) {
        return ImGui.nCollapseButton(id, pos.x, pos.y, dockNode.ptr);
    }

    public static boolean collapseButton(int id, float posX, float posY, ImGuiDockNode dockNode) {
        return ImGui.nCollapseButton(id, posX, posY, dockNode.ptr);
    }

    private static native boolean nCollapseButton(int var0, float var1, float var2, long var3);

    public static void scrollbar(int axis) {
        ImGui.nScrollbar(axis);
    }

    private static native void nScrollbar(int var0);

    public static ImRect getWindowScrollbarRect(ImGuiWindow imGuiWindow, int axis) {
        ImRect dst = new ImRect();
        ImGui.nGetWindowScrollbarRect(dst, imGuiWindow.ptr, axis);
        return dst;
    }

    public static void getWindowScrollbarRect(ImRect dst, ImGuiWindow imGuiWindow, int axis) {
        ImGui.nGetWindowScrollbarRect(dst, imGuiWindow.ptr, axis);
    }

    private static native void nGetWindowScrollbarRect(ImRect var0, long var1, int var3);

    public static int getWindowScrollbarID(ImGuiWindow window, int axis) {
        return ImGui.nGetWindowScrollbarID(window.ptr, axis);
    }

    private static native int nGetWindowScrollbarID(long var0, int var2);

    public static int getWindowResizeCornerID(ImGuiWindow window, int n) {
        return ImGui.nGetWindowResizeCornerID(window.ptr, n);
    }

    private static native int nGetWindowResizeCornerID(long var0, int var2);

    public static int getWindowResizeBorderID(ImGuiWindow window, int dir) {
        return ImGui.nGetWindowResizeBorderID(window.ptr, dir);
    }

    private static native int nGetWindowResizeBorderID(long var0, int var2);

    public static boolean buttonBehavior(ImRect bb, int id, ImBoolean outHovered, ImBoolean outHeld) {
        return ImGui.nButtonBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, outHovered != null ? outHovered.getData() : null, outHeld != null ? outHeld.getData() : null);
    }

    public static boolean buttonBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, ImBoolean outHovered, ImBoolean outHeld) {
        return ImGui.nButtonBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, outHovered != null ? outHovered.getData() : null, outHeld != null ? outHeld.getData() : null);
    }

    public static boolean buttonBehavior(ImRect bb, int id, ImBoolean outHovered, ImBoolean outHeld, int imGuiButtonFlags) {
        return ImGui.nButtonBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, outHovered != null ? outHovered.getData() : null, outHeld != null ? outHeld.getData() : null, imGuiButtonFlags);
    }

    public static boolean buttonBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, ImBoolean outHovered, ImBoolean outHeld, int imGuiButtonFlags) {
        return ImGui.nButtonBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, outHovered != null ? outHovered.getData() : null, outHeld != null ? outHeld.getData() : null, imGuiButtonFlags);
    }

    private static native boolean nButtonBehavior(float var0, float var1, float var2, float var3, int var4, boolean[] var5, boolean[] var6);

    private static native boolean nButtonBehavior(float var0, float var1, float var2, float var3, int var4, boolean[] var5, boolean[] var6, int var7);

    public static boolean splitterBehavior(ImRect bb, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2) {
        return ImGui.nSplitterBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2) {
        return ImGui.nSplitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2);
    }

    public static boolean splitterBehavior(ImRect bb, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend) {
        return ImGui.nSplitterBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend) {
        return ImGui.nSplitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend);
    }

    public static boolean splitterBehavior(ImRect bb, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend, float hoverVisibilityDelay) {
        return ImGui.nSplitterBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend, hoverVisibilityDelay);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend, float hoverVisibilityDelay) {
        return ImGui.nSplitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend, hoverVisibilityDelay);
    }

    public static boolean splitterBehavior(ImRect bb, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend, float hoverVisibilityDelay, int bgCol) {
        return ImGui.nSplitterBehavior(bb.min.x, bb.min.y, bb.max.x, bb.max.y, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend, hoverVisibilityDelay, bgCol);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int axis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend, float hoverVisibilityDelay, int bgCol) {
        return ImGui.nSplitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, axis, size1 != null ? size1.getData() : null, size2 != null ? size2.getData() : null, minSize1, minSize2, hoverExtend, hoverVisibilityDelay, bgCol);
    }

    private static native boolean nSplitterBehavior(float var0, float var1, float var2, float var3, int var4, int var5, float[] var6, float[] var7, float var8, float var9);

    private static native boolean nSplitterBehavior(float var0, float var1, float var2, float var3, int var4, int var5, float[] var6, float[] var7, float var8, float var9, float var10);

    private static native boolean nSplitterBehavior(float var0, float var1, float var2, float var3, int var4, int var5, float[] var6, float[] var7, float var8, float var9, float var10, float var11);

    private static native boolean nSplitterBehavior(float var0, float var1, float var2, float var3, int var4, int var5, float[] var6, float[] var7, float var8, float var9, float var10, float var11, int var12);

    static {
        ImGui.nInit();
    }
}

