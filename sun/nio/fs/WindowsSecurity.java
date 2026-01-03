/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import jdk.internal.misc.PreviewFeatures;
import jdk.internal.vm.Continuation;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsNativeDispatcher;

class WindowsSecurity {
    static final long processTokenWithDuplicateAccess = WindowsSecurity.openProcessToken(2);
    static final long processTokenWithQueryAccess = WindowsSecurity.openProcessToken(8);

    private WindowsSecurity() {
    }

    private static long openProcessToken(int access) {
        try {
            return WindowsNativeDispatcher.OpenProcessToken(WindowsNativeDispatcher.GetCurrentProcess(), access);
        }
        catch (WindowsException x) {
            return 0L;
        }
    }

    static Privilege enablePrivilege(String priv) {
        long pLuid;
        try {
            pLuid = WindowsNativeDispatcher.LookupPrivilegeValue(priv);
        }
        catch (WindowsException x) {
            throw new AssertionError((Object)x);
        }
        long hToken = 0L;
        boolean impersontating = false;
        boolean elevated = false;
        try {
            hToken = WindowsNativeDispatcher.OpenThreadToken(WindowsNativeDispatcher.GetCurrentThread(), 32, false);
            if (hToken == 0L && processTokenWithDuplicateAccess != 0L) {
                hToken = WindowsNativeDispatcher.DuplicateTokenEx(processTokenWithDuplicateAccess, 36);
                WindowsNativeDispatcher.SetThreadToken(0L, hToken);
                impersontating = true;
            }
            if (hToken != 0L) {
                WindowsNativeDispatcher.AdjustTokenPrivileges(hToken, pLuid, 2);
                elevated = true;
            }
        }
        catch (WindowsException windowsException) {
            // empty catch block
        }
        long token = hToken;
        boolean stopImpersontating = impersontating;
        boolean needToRevert = elevated;
        if (PreviewFeatures.isEnabled()) {
            Continuation.pin();
        }
        return () -> {
            block12: {
                try {
                    if (token == 0L) break block12;
                    try {
                        if (stopImpersontating) {
                            WindowsNativeDispatcher.SetThreadToken(0L, 0L);
                        } else if (needToRevert) {
                            WindowsNativeDispatcher.AdjustTokenPrivileges(token, pLuid, 0);
                        }
                    }
                    catch (WindowsException x) {
                        throw new AssertionError((Object)x);
                    }
                    finally {
                        WindowsNativeDispatcher.CloseHandle(token);
                    }
                }
                finally {
                    WindowsNativeDispatcher.LocalFree(pLuid);
                    if (PreviewFeatures.isEnabled()) {
                        Continuation.unpin();
                    }
                }
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean checkAccessMask(long securityInfo, int accessMask, int genericRead, int genericWrite, int genericExecute, int genericAll) throws WindowsException {
        int privileges = 8;
        long hToken = WindowsNativeDispatcher.OpenThreadToken(WindowsNativeDispatcher.GetCurrentThread(), privileges, false);
        if (hToken == 0L && processTokenWithDuplicateAccess != 0L) {
            hToken = WindowsNativeDispatcher.DuplicateTokenEx(processTokenWithDuplicateAccess, privileges);
        }
        boolean hasRight = false;
        if (hToken != 0L) {
            try {
                hasRight = WindowsNativeDispatcher.AccessCheck(hToken, securityInfo, accessMask, genericRead, genericWrite, genericExecute, genericAll);
            }
            finally {
                WindowsNativeDispatcher.CloseHandle(hToken);
            }
        }
        return hasRight;
    }

    static interface Privilege {
        public void drop();
    }
}

