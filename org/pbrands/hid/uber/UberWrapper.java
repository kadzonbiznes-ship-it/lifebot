/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.hid.uber;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javax.crypto.SecretKey;
import org.pbrands.hid.uber.api.FortniteHook;
import org.pbrands.util.DllEncryptorUtil;
import org.pbrands.util.LoaderUtil;
import org.pbrands.windows.User32Extended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UberWrapper
implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(UberWrapper.class);
    private final FortniteHook lib = LoaderUtil.FORTNITE_INSTANCE;
    private final User32Extended user32 = User32Extended.INSTANCE;
    private final boolean useHook;
    public static final int MOUSEEVENTF_MOVE = 1;
    public static final int MOUSEEVENTF_LEFTDOWN = 2;
    public static final int MOUSEEVENTF_LEFTUP = 4;
    public static final int MOUSEEVENTF_RIGHTDOWN = 8;
    public static final int MOUSEEVENTF_RIGHTUP = 16;
    public static final int MOUSEEVENTF_ABSOLUTE = 32768;
    private static final int VK_RETURN = 13;

    public UberWrapper(byte[] encodedDll, SecretKey key, boolean useHook) {
        try {
            byte[] decrypt = DllEncryptorUtil.decrypt(encodedDll, key);
            byte load = this.lib.LoadUber(decrypt, decrypt.length);
            if (load == 1) {
                logger.debug("DLL injected (Code " + load + ")");
            } else {
                logger.error("Failed to inject DLL (Code " + load + ")");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.useHook = useHook;
        int rc = this.lib.UberSendInit(3, 0, null, 0);
        if (rc != 0) {
            throw new IllegalStateException("UberSendInit failed, code=" + rc);
        }
    }

    public boolean isDriverAvailable() {
        int rc = this.lib.UberSendInit(3, 0, null, 0);
        if (rc == 0) {
            this.lib.UberSendDestroy();
            return true;
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        this.lib.UberSendDestroy();
    }

    public void keyPress(int vk) {
        this.keyDown(vk);
        this.keyUp(vk);
    }

    public void keyDown(int vk) {
        int scanCode = this.getScanCode(vk);
        logger.info("[UberWrapper] keyDown: vk={}, scanCode={}", (Object)vk, (Object)scanCode);
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(vk, false, scanCode));
    }

    public void keyUp(int vk) {
        int scanCode = this.getScanCode(vk);
        logger.info("[UberWrapper] keyUp: vk={}, scanCode={}", (Object)vk, (Object)scanCode);
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(vk, true, scanCode));
    }

    public void pressChar(char c) {
        KeyMapping mapping = this.getKeyMapping(c);
        if (mapping.shiftRequired) {
            this.keyDown(16);
        }
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(mapping.vk, false, mapping.scanCode));
    }

    public void pressChar(char c, long delayMs) {
        logger.info("[UberWrapper] pressChar: char='{}', delayMs={}", (Object)Character.valueOf(c), (Object)delayMs);
        this.pressChar(c);
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.releaseChar(c);
        logger.info("[UberWrapper] pressChar: char='{}' completed", (Object)Character.valueOf(c));
    }

    public void releaseChar(char c) {
        KeyMapping mapping = this.getKeyMapping(c);
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(mapping.vk, true, mapping.scanCode));
        if (mapping.shiftRequired) {
            this.keyUp(16);
        }
    }

    public void releaseChar(char c, int delayMs) {
        this.releaseChar(c);
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void typeChar(char c) {
        KeyMapping mapping = this.getKeyMapping(c);
        if (mapping.shiftRequired) {
            this.keyDown(16);
        }
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(mapping.vk, false, mapping.scanCode));
        this.sendInputs(this.useHook, UberWrapper.createKeyEvent(mapping.vk, true, mapping.scanCode));
        if (mapping.shiftRequired) {
            this.keyUp(16);
        }
    }

    public void typeChar(char c, int delayMs) {
        this.typeChar(c);
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void typeString(String text) {
        for (char c : text.toCharArray()) {
            this.typeChar(c);
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void mouseMoveAbsolute(int x, int y) {
        boolean success = this.user32.SetCursorPos(x, y);
    }

    public void mouseMoveRelative(int dx, int dy) {
        this.sendInputs(false, UberWrapper.createMouseEvent(dx, dy, 1));
    }

    public void mouseMoveRelative(int dx, int dy, int delayMs) {
        this.mouseMoveRelative(dx, dy);
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void mouseDown(int button) {
        int flag = button == 1 ? 2 : 8;
        this.sendInputs(this.useHook, UberWrapper.createMouseEvent(0, 0, flag));
    }

    public void mouseUp(int button) {
        int flag = button == 1 ? 4 : 16;
        this.sendInputs(this.useHook, UberWrapper.createMouseEvent(0, 0, flag));
    }

    public void mouseClick(int button) {
        this.mouseClick(button, 50L);
    }

    public void mouseClick(int button, long delayMs) {
        this.mouseDown(button);
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.mouseUp(button);
    }

    private void sendInputs(boolean useHook, WinUser.INPUT ... inputs) {
        logger.info("[UberWrapper] sendInputs: useHook={}, inputCount={}", (Object)useHook, (Object)inputs.length);
        if (useHook) {
            logger.debug("[UberWrapper] Calling UberSendInputHook(1)");
            this.lib.UberSendInputHook(1);
        }
        WinDef.DWORD result = this.user32.SendInput(new WinDef.DWORD((long)inputs.length), inputs, inputs[0].size());
        logger.info("[UberWrapper] SendInput result: expected={}, got={}", (Object)inputs.length, (Object)result.longValue());
        if (useHook) {
            logger.debug("[UberWrapper] Calling UberSendInputHook(0)");
            this.lib.UberSendInputHook(0);
        }
        if (result.longValue() != (long)inputs.length) {
            logger.error("[UberWrapper] SendInput failed: expected {}, got {}", (Object)inputs.length, (Object)result.longValue());
        }
    }

    private static WinUser.INPUT createKeyEvent(int vk, boolean keyUp, int scanCode) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(1L);
        input.input.setType("ki");
        int winVk = vk == 10 ? 13 : vk;
        input.input.ki.wVk = new WinDef.WORD((long)winVk);
        input.input.ki.wScan = new WinDef.WORD((long)scanCode);
        input.input.ki.dwFlags = new WinDef.DWORD(keyUp ? 2L : 0L);
        if (scanCode != 0) {
            input.input.ki.dwFlags = new WinDef.DWORD(input.input.ki.dwFlags.longValue() | 8L);
        }
        input.input.ki.time = new WinDef.DWORD(0L);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0L);
        input.write();
        return input;
    }

    private static WinUser.INPUT createMouseEvent(int dx, int dy, int flags) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(0L);
        input.input.setType("mi");
        input.input.mi.dx = new WinDef.LONG((long)dx);
        input.input.mi.dy = new WinDef.LONG((long)dy);
        input.input.mi.dwFlags = new WinDef.DWORD((long)flags);
        input.input.mi.mouseData = new WinDef.DWORD(0L);
        input.input.mi.time = new WinDef.DWORD(0L);
        input.input.mi.dwExtraInfo = new BaseTSD.ULONG_PTR(0L);
        input.write();
        return input;
    }

    private KeyMapping getKeyMapping(char c) {
        short result = this.user32.VkKeyScanA(c);
        if (result == -1) {
            throw new IllegalArgumentException("Cannot find VK for character: " + c);
        }
        int vk = result & 0xFF;
        int shiftState = result >> 8 & 0xFF;
        boolean shiftRequired = (shiftState & 1) != 0;
        int scanCode = this.user32.MapVirtualKeyA(vk, 0);
        return new KeyMapping(vk, scanCode, shiftRequired);
    }

    private int getScanCode(int vk) {
        return this.user32.MapVirtualKeyA(vk, 0);
    }

    private static class KeyMapping {
        int vk;
        int scanCode;
        boolean shiftRequired;

        KeyMapping(int vk, int scanCode, boolean shiftRequired) {
            this.vk = vk;
            this.scanCode = scanCode;
            this.shiftRequired = shiftRequired;
        }
    }
}

