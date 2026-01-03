/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.logic.hid;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.pbrands.hid.HIDSimulator;
import org.pbrands.util.Log;

public class HIDDiagnostics {
    private static final int VK_F13 = 124;
    private static final int VK_F14 = 125;
    private static final int VK_F15 = 126;
    private final HIDSimulator hidSimulator;
    private static final boolean SIMULATE_FAILURE = false;

    public HIDDiagnostics(HIDSimulator hidSimulator) {
        this.hidSimulator = hidSimulator;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DiagnosticResult runTest() {
        long startTime = System.currentTimeMillis();
        int TEST_KEY_COUNT = 3;
        final AtomicInteger keysReceived = new AtomicInteger(0);
        AtomicBoolean hookInstalled = new AtomicBoolean(false);
        final CountDownLatch testComplete = new CountDownLatch(3);
        final WinUser.HHOOK[] hookHandle = new WinUser.HHOOK[1];
        WinUser.LowLevelKeyboardProc testHookProc = new WinUser.LowLevelKeyboardProc(){

            @Override
            public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
                if (nCode >= 0) {
                    boolean isKeyDown;
                    int vkCode = lParam.vkCode;
                    boolean bl = isKeyDown = wParam.intValue() == 256 || wParam.intValue() == 260;
                    if (isKeyDown && (vkCode == 81 || vkCode == 69)) {
                        keysReceived.incrementAndGet();
                        testComplete.countDown();
                        Log.debug("Diagnostic hook captured key: 0x{}", Integer.toHexString(vkCode));
                        return new WinDef.LRESULT(1L);
                    }
                }
                return User32.INSTANCE.CallNextHookEx(hookHandle[0], nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer())));
            }
        };
        Thread hookThread = new Thread(() -> {
            try {
                WinDef.HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
                hookHandle[0] = User32.INSTANCE.SetWindowsHookEx(13, testHookProc, hMod, 0);
                if (hookHandle[0] == null) {
                    Log.error("Failed to install diagnostic keyboard hook: {}", Kernel32.INSTANCE.GetLastError());
                    return;
                }
                hookInstalled.set(true);
                Log.debug("Diagnostic keyboard hook installed");
                WinUser.MSG msg = new WinUser.MSG();
                while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }
            }
            catch (Exception e) {
                Log.error("Error in diagnostic hook thread", e);
            }
        }, "HID-Diagnostic-Hook");
        hookThread.setDaemon(true);
        hookThread.start();
        try {
            for (int waitCount = 0; !hookInstalled.get() && waitCount < 50; ++waitCount) {
                Thread.sleep(10L);
            }
            if (!hookInstalled.get()) {
                DiagnosticResult diagnosticResult = new DiagnosticResult(false, 0, 0, "Hook installation timeout", System.currentTimeMillis() - startTime);
                return diagnosticResult;
            }
            Thread.sleep(50L);
            Log.info("Sending {} diagnostic keystrokes...", 3);
            for (int i = 0; i < 3; ++i) {
                char key = i % 2 == 0 ? (char)'q' : 'e';
                this.hidSimulator.sendKeystroke(key, 10L);
                Thread.sleep(30L);
            }
            boolean allReceived = testComplete.await(500L, TimeUnit.MILLISECONDS);
            long duration = System.currentTimeMillis() - startTime;
            int received = keysReceived.get();
            if (received >= 3) {
                Log.info("HID diagnostics PASSED: {}/{} keys captured in {}ms", received, 3, duration);
                DiagnosticResult diagnosticResult = new DiagnosticResult(true, 3, received, null, duration);
                return diagnosticResult;
            }
            if (received > 0) {
                String msg = String.format("Partial capture: only %d/%d keys", received, 3);
                Log.warn("HID diagnostics WARNING: {}", msg);
                DiagnosticResult diagnosticResult = new DiagnosticResult(received >= 1, 3, received, msg, duration);
                return diagnosticResult;
            }
            Log.error("HID diagnostics FAILED: No keys captured");
            DiagnosticResult diagnosticResult = new DiagnosticResult(false, 3, 0, "No keys captured - driver may not be loaded correctly", duration);
            return diagnosticResult;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DiagnosticResult diagnosticResult = new DiagnosticResult(false, 3, keysReceived.get(), "Test interrupted", System.currentTimeMillis() - startTime);
            return diagnosticResult;
        }
        finally {
            if (hookHandle[0] != null) {
                User32.INSTANCE.UnhookWindowsHookEx(hookHandle[0]);
                Log.debug("Diagnostic keyboard hook removed");
            }
            User32.INSTANCE.PostThreadMessage(Kernel32.INSTANCE.GetCurrentThreadId(), 18, null, null);
        }
    }

    public static class DiagnosticResult {
        public final boolean success;
        public final int keysSent;
        public final int keysReceived;
        public final String errorMessage;
        public final long testDurationMs;

        public DiagnosticResult(boolean success, int keysSent, int keysReceived, String errorMessage, long testDurationMs) {
            this.success = success;
            this.keysSent = keysSent;
            this.keysReceived = keysReceived;
            this.errorMessage = errorMessage;
            this.testDurationMs = testDurationMs;
        }

        public String toString() {
            if (this.success) {
                return String.format("HID OK: %d/%d keys captured in %dms", this.keysReceived, this.keysSent, this.testDurationMs);
            }
            return String.format("HID FAILED: %s (captured %d/%d)", this.errorMessage, this.keysReceived, this.keysSent);
        }
    }
}

