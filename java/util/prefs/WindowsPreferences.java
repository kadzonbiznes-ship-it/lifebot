/*
 * Decompiled with CFR 0.152.
 */
package java.util.prefs;

import java.io.ByteArrayOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Base64;
import java.util.prefs.Preferences;
import sun.util.logging.PlatformLogger;

class WindowsPreferences
extends AbstractPreferences {
    private static PlatformLogger logger;
    private static final byte[] WINDOWS_ROOT_PATH;
    private static final int HKEY_CURRENT_USER = -2147483647;
    private static final int HKEY_LOCAL_MACHINE = -2147483646;
    private static final int USER_ROOT_NATIVE_HANDLE = -2147483647;
    private static final int SYSTEM_ROOT_NATIVE_HANDLE = -2147483646;
    private static final int MAX_WINDOWS_PATH_LENGTH = 256;
    private static volatile Preferences userRoot;
    private static volatile Preferences systemRoot;
    private static final int ERROR_SUCCESS = 0;
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_ACCESS_DENIED = 5;
    private static final int NATIVE_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    private static final int VALUES_NUMBER = 2;
    private static final int MAX_KEY_LENGTH = 3;
    private static final int MAX_VALUE_NAME_LENGTH = 4;
    private static final int DISPOSITION = 2;
    private static final int REG_CREATED_NEW_KEY = 1;
    private static final int REG_OPENED_EXISTING_KEY = 2;
    private static final int NULL_NATIVE_HANDLE = 0;
    private static final int DELETE = 65536;
    private static final int KEY_QUERY_VALUE = 1;
    private static final int KEY_SET_VALUE = 2;
    private static final int KEY_CREATE_SUB_KEY = 4;
    private static final int KEY_ENUMERATE_SUB_KEYS = 8;
    private static final int KEY_READ = 131097;
    private static final int KEY_WRITE = 131078;
    private static final int KEY_ALL_ACCESS = 983103;
    private static int INIT_SLEEP_TIME;
    private static int MAX_ATTEMPTS;
    private boolean isBackingStoreAvailable = true;

    private static void loadPrefsLib() {
        PrivilegedAction<Void> load = () -> {
            System.loadLibrary("prefs");
            return null;
        };
        AccessController.doPrivileged(load);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static Preferences getUserRoot() {
        Preferences root = userRoot;
        if (root != null) return root;
        Class<WindowsPreferences> clazz = WindowsPreferences.class;
        synchronized (WindowsPreferences.class) {
            root = userRoot;
            if (root != null) return root;
            userRoot = root = new WindowsPreferences(-2147483647L, WINDOWS_ROOT_PATH);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return root;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static Preferences getSystemRoot() {
        Preferences root = systemRoot;
        if (root != null) return root;
        Class<WindowsPreferences> clazz = WindowsPreferences.class;
        synchronized (WindowsPreferences.class) {
            root = systemRoot;
            if (root != null) return root;
            systemRoot = root = new WindowsPreferences(-2147483646L, WINDOWS_ROOT_PATH);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return root;
        }
    }

    private static native long[] WindowsRegOpenKey(long var0, byte[] var2, int var3);

    private static long[] WindowsRegOpenKey1(long hKey, byte[] subKey, int securityMask) {
        long[] result = WindowsPreferences.WindowsRegOpenKey(hKey, subKey, securityMask);
        if (result[1] == 0L) {
            return result;
        }
        if (result[1] == 2L) {
            WindowsPreferences.logger().warning("Trying to recreate Windows registry node " + WindowsPreferences.byteArrayToString(subKey) + " at root 0x" + Long.toHexString(hKey) + ".");
            long handle = WindowsPreferences.WindowsRegCreateKeyEx(hKey, subKey)[0];
            WindowsPreferences.WindowsRegCloseKey(handle);
            return WindowsPreferences.WindowsRegOpenKey(hKey, subKey, securityMask);
        }
        if (result[1] != 5L) {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; ++i) {
                try {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                    return result;
                }
                sleepTime *= 2L;
                result = WindowsPreferences.WindowsRegOpenKey(hKey, subKey, securityMask);
                if (result[1] != 0L) continue;
                return result;
            }
        }
        return result;
    }

    private static native int WindowsRegCloseKey(long var0);

    private static native long[] WindowsRegCreateKeyEx(long var0, byte[] var2);

    private static long[] WindowsRegCreateKeyEx1(long hKey, byte[] subKey) {
        long[] result = WindowsPreferences.WindowsRegCreateKeyEx(hKey, subKey);
        if (result[1] == 0L) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegCreateKeyEx(hKey, subKey);
            if (result[1] != 0L) continue;
            return result;
        }
        return result;
    }

    private static native int WindowsRegDeleteKey(long var0, byte[] var2);

    private static native int WindowsRegFlushKey(long var0);

    private static int WindowsRegFlushKey1(long hKey) {
        int result = WindowsPreferences.WindowsRegFlushKey(hKey);
        if (result == 0) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegFlushKey(hKey);
            if (result != 0) continue;
            return result;
        }
        return result;
    }

    private static native byte[] WindowsRegQueryValueEx(long var0, byte[] var2);

    private static native int WindowsRegSetValueEx(long var0, byte[] var2, byte[] var3);

    private static int WindowsRegSetValueEx1(long hKey, byte[] valueName, byte[] value) {
        int result = WindowsPreferences.WindowsRegSetValueEx(hKey, valueName, value);
        if (result == 0) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegSetValueEx(hKey, valueName, value);
            if (result != 0) continue;
            return result;
        }
        return result;
    }

    private static native int WindowsRegDeleteValue(long var0, byte[] var2);

    private static native long[] WindowsRegQueryInfoKey(long var0);

    private static long[] WindowsRegQueryInfoKey1(long hKey) {
        long[] result = WindowsPreferences.WindowsRegQueryInfoKey(hKey);
        if (result[1] == 0L) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegQueryInfoKey(hKey);
            if (result[1] != 0L) continue;
            return result;
        }
        return result;
    }

    private static native byte[] WindowsRegEnumKeyEx(long var0, int var2, int var3);

    private static byte[] WindowsRegEnumKeyEx1(long hKey, int subKeyIndex, int maxKeyLength) {
        byte[] result = WindowsPreferences.WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
        if (result != null) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
            if (result == null) continue;
            return result;
        }
        return result;
    }

    private static native byte[] WindowsRegEnumValue(long var0, int var2, int var3);

    private static byte[] WindowsRegEnumValue1(long hKey, int valueIndex, int maxValueNameLength) {
        byte[] result = WindowsPreferences.WindowsRegEnumValue(hKey, valueIndex, maxValueNameLength);
        if (result != null) {
            return result;
        }
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return result;
            }
            sleepTime *= 2L;
            result = WindowsPreferences.WindowsRegEnumValue(hKey, valueIndex, maxValueNameLength);
            if (result == null) continue;
            return result;
        }
        return result;
    }

    private WindowsPreferences(WindowsPreferences parent, String name) {
        super(parent, name);
        long parentNativeHandle = parent.openKey(4, 131097);
        if (parentNativeHandle == 0L) {
            this.isBackingStoreAvailable = false;
            return;
        }
        long[] result = WindowsPreferences.WindowsRegCreateKeyEx1(parentNativeHandle, WindowsPreferences.toWindowsName(name));
        if (result[1] != 0L) {
            WindowsPreferences.logger().warning("Could not create windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(parentNativeHandle) + ". Windows RegCreateKeyEx(...) returned error code " + result[1] + ".");
            this.isBackingStoreAvailable = false;
            return;
        }
        this.newNode = result[2] == 1L;
        this.closeKey(parentNativeHandle);
        this.closeKey(result[0]);
    }

    private WindowsPreferences(long rootNativeHandle, byte[] rootDirectory) {
        super(null, "");
        long[] result = WindowsPreferences.WindowsRegCreateKeyEx1(rootNativeHandle, rootDirectory);
        if (result[1] != 0L) {
            WindowsPreferences.logger().warning("Could not open/create prefs root node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(rootNativeHandle) + ". Windows RegCreateKeyEx(...) returned error code " + result[1] + ".");
            this.isBackingStoreAvailable = false;
            return;
        }
        this.newNode = result[2] == 1L;
        this.closeKey(result[0]);
    }

    private byte[] windowsAbsolutePath() {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        bstream.write(WINDOWS_ROOT_PATH, 0, WINDOWS_ROOT_PATH.length - 1);
        StringTokenizer tokenizer = new StringTokenizer(this.absolutePath(), "/");
        while (tokenizer.hasMoreTokens()) {
            bstream.write(92);
            String nextName = tokenizer.nextToken();
            byte[] windowsNextName = WindowsPreferences.toWindowsName(nextName);
            bstream.write(windowsNextName, 0, windowsNextName.length - 1);
        }
        bstream.write(0);
        return bstream.toByteArray();
    }

    private long openKey(int securityMask) {
        return this.openKey(securityMask, securityMask);
    }

    private long openKey(int mask1, int mask2) {
        return this.openKey(this.windowsAbsolutePath(), mask1, mask2);
    }

    private long openKey(byte[] windowsAbsolutePath, int mask1, int mask2) {
        if (windowsAbsolutePath.length <= 257) {
            long[] result = WindowsPreferences.WindowsRegOpenKey1(this.rootNativeHandle(), windowsAbsolutePath, mask1);
            if (result[1] == 5L && mask2 != mask1) {
                result = WindowsPreferences.WindowsRegOpenKey1(this.rootNativeHandle(), windowsAbsolutePath, mask2);
            }
            if (result[1] != 0L) {
                WindowsPreferences.logger().warning("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegOpenKey(...) returned error code " + result[1] + ".");
                result[0] = 0L;
                if (result[1] == 5L) {
                    throw new SecurityException("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ": Access denied");
                }
            }
            return result[0];
        }
        return this.openKey(this.rootNativeHandle(), windowsAbsolutePath, mask1, mask2);
    }

    private long openKey(long nativeHandle, byte[] windowsRelativePath, int mask1, int mask2) {
        if (windowsRelativePath.length <= 257) {
            long[] result = WindowsPreferences.WindowsRegOpenKey1(nativeHandle, windowsRelativePath, mask1);
            if (result[1] == 5L && mask2 != mask1) {
                result = WindowsPreferences.WindowsRegOpenKey1(nativeHandle, windowsRelativePath, mask2);
            }
            if (result[1] != 0L) {
                WindowsPreferences.logger().warning("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(nativeHandle) + ". Windows RegOpenKey(...) returned error code " + result[1] + ".");
                result[0] = 0L;
            }
            return result[0];
        }
        int separatorPosition = -1;
        for (int i = 256; i > 0; --i) {
            if (windowsRelativePath[i] != 92) continue;
            separatorPosition = i;
            break;
        }
        byte[] nextRelativeRoot = new byte[separatorPosition + 1];
        System.arraycopy(windowsRelativePath, 0, nextRelativeRoot, 0, separatorPosition);
        nextRelativeRoot[separatorPosition] = 0;
        byte[] nextRelativePath = new byte[windowsRelativePath.length - separatorPosition - 1];
        System.arraycopy(windowsRelativePath, separatorPosition + 1, nextRelativePath, 0, nextRelativePath.length);
        long nextNativeHandle = this.openKey(nativeHandle, nextRelativeRoot, mask1, mask2);
        if (nextNativeHandle == 0L) {
            return 0L;
        }
        long result = this.openKey(nextNativeHandle, nextRelativePath, mask1, mask2);
        this.closeKey(nextNativeHandle);
        return result;
    }

    private void closeKey(long nativeHandle) {
        int result = WindowsPreferences.WindowsRegCloseKey(nativeHandle);
        if (result != 0) {
            WindowsPreferences.logger().warning("Could not close windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegCloseKey(...) returned error code " + result + ".");
        }
    }

    @Override
    protected void putSpi(String javaName, String value) {
        long nativeHandle = this.openKey(2);
        if (nativeHandle == 0L) {
            this.isBackingStoreAvailable = false;
            return;
        }
        int result = WindowsPreferences.WindowsRegSetValueEx1(nativeHandle, WindowsPreferences.toWindowsName(javaName), WindowsPreferences.toWindowsValueString(value));
        if (result != 0) {
            WindowsPreferences.logger().warning("Could not assign value to key " + WindowsPreferences.byteArrayToString(WindowsPreferences.toWindowsName(javaName)) + " at Windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegSetValueEx(...) returned error code " + result + ".");
            this.isBackingStoreAvailable = false;
        }
        this.closeKey(nativeHandle);
    }

    @Override
    protected String getSpi(String javaName) {
        long nativeHandle = this.openKey(1);
        if (nativeHandle == 0L) {
            return null;
        }
        byte[] resultObject = WindowsPreferences.WindowsRegQueryValueEx(nativeHandle, WindowsPreferences.toWindowsName(javaName));
        if (resultObject == null) {
            this.closeKey(nativeHandle);
            return null;
        }
        this.closeKey(nativeHandle);
        return WindowsPreferences.toJavaValueString(resultObject);
    }

    @Override
    protected void removeSpi(String key) {
        long nativeHandle = this.openKey(2);
        if (nativeHandle == 0L) {
            return;
        }
        int result = WindowsPreferences.WindowsRegDeleteValue(nativeHandle, WindowsPreferences.toWindowsName(key));
        if (result != 0 && result != 2) {
            WindowsPreferences.logger().warning("Could not delete windows registry value " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + "\\" + String.valueOf(WindowsPreferences.toWindowsName(key)) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegDeleteValue(...) returned error code " + result + ".");
            this.isBackingStoreAvailable = false;
        }
        this.closeKey(nativeHandle);
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        long nativeHandle = this.openKey(1);
        if (nativeHandle == 0L) {
            throw new BackingStoreException("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ".");
        }
        long[] result = WindowsPreferences.WindowsRegQueryInfoKey1(nativeHandle);
        if (result[1] != 0L) {
            String info = "Could not query windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegQueryInfoKeyEx(...) returned error code " + result[1] + ".";
            WindowsPreferences.logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxValueNameLength = (int)result[4];
        int valuesNumber = (int)result[2];
        if (valuesNumber == 0) {
            this.closeKey(nativeHandle);
            return new String[0];
        }
        String[] valueNames = new String[valuesNumber];
        for (int i = 0; i < valuesNumber; ++i) {
            byte[] windowsName = WindowsPreferences.WindowsRegEnumValue1(nativeHandle, i, maxValueNameLength + 1);
            if (windowsName == null) {
                String info = "Could not enumerate value #" + i + "  of windows node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ".";
                WindowsPreferences.logger().warning(info);
                throw new BackingStoreException(info);
            }
            valueNames[i] = WindowsPreferences.toJavaName(windowsName);
        }
        this.closeKey(nativeHandle);
        return valueNames;
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        long nativeHandle = this.openKey(9);
        if (nativeHandle == 0L) {
            throw new BackingStoreException("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ".");
        }
        long[] result = WindowsPreferences.WindowsRegQueryInfoKey1(nativeHandle);
        if (result[1] != 0L) {
            String info = "Could not query windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegQueryInfoKeyEx(...) returned error code " + result[1] + ".";
            WindowsPreferences.logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxKeyLength = (int)result[3];
        int subKeysNumber = (int)result[0];
        if (subKeysNumber == 0) {
            this.closeKey(nativeHandle);
            return new String[0];
        }
        String[] children = new String[subKeysNumber];
        for (int i = 0; i < subKeysNumber; ++i) {
            String javaName;
            byte[] windowsName = WindowsPreferences.WindowsRegEnumKeyEx1(nativeHandle, i, maxKeyLength + 1);
            if (windowsName == null) {
                String info = "Could not enumerate key #" + i + "  of windows node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". ";
                WindowsPreferences.logger().warning(info);
                throw new BackingStoreException(info);
            }
            children[i] = javaName = WindowsPreferences.toJavaName(windowsName);
        }
        this.closeKey(nativeHandle);
        return children;
    }

    @Override
    public void flush() throws BackingStoreException {
        if (this.isRemoved()) {
            this.parent.flush();
            return;
        }
        if (!this.isBackingStoreAvailable) {
            throw new BackingStoreException("flush(): Backing store not available.");
        }
        long nativeHandle = this.openKey(131097);
        if (nativeHandle == 0L) {
            throw new BackingStoreException("Could not open windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ".");
        }
        int result = WindowsPreferences.WindowsRegFlushKey1(nativeHandle);
        if (result != 0) {
            String info = "Could not flush windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegFlushKey(...) returned error code " + result + ".";
            WindowsPreferences.logger().warning(info);
            throw new BackingStoreException(info);
        }
        this.closeKey(nativeHandle);
    }

    @Override
    public void sync() throws BackingStoreException {
        if (this.isRemoved()) {
            throw new IllegalStateException("Node has been removed");
        }
        this.flush();
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        return new WindowsPreferences(this, name);
    }

    @Override
    public void removeNodeSpi() throws BackingStoreException {
        long parentNativeHandle = ((WindowsPreferences)this.parent()).openKey(65536);
        if (parentNativeHandle == 0L) {
            throw new BackingStoreException("Could not open parent windows registry node of " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ".");
        }
        int result = WindowsPreferences.WindowsRegDeleteKey(parentNativeHandle, WindowsPreferences.toWindowsName(this.name()));
        if (result != 0) {
            String info = "Could not delete windows registry node " + WindowsPreferences.byteArrayToString(this.windowsAbsolutePath()) + " at root 0x" + Long.toHexString(this.rootNativeHandle()) + ". Windows RegDeleteKeyEx(...) returned error code " + result + ".";
            WindowsPreferences.logger().warning(info);
            throw new BackingStoreException(info);
        }
        this.closeKey(parentNativeHandle);
    }

    /*
     * Unable to fully structure code
     */
    private static String toJavaName(byte[] windowsNameArray) {
        windowsName = WindowsPreferences.byteArrayToString(windowsNameArray);
        if (windowsName.startsWith("/!")) {
            return WindowsPreferences.toJavaAlt64Name(windowsName);
        }
        javaName = new StringBuilder();
        for (i = 0; i < windowsName.length(); ++i) {
            block7: {
                block6: {
                    ch = windowsName.charAt(i);
                    if (ch != 47) break block6;
                    next = 32;
                    if (windowsName.length() <= i + 1) ** GOTO lbl-1000
                    v0 = windowsName.charAt(i + 1);
                    next = v0;
                    if (v0 >= 'A' && next <= 90) {
                        ch = next;
                        ++i;
                    } else if (windowsName.length() > i + 1 && next == 47) {
                        ch = 92;
                        ++i;
                    }
                    break block7;
                }
                if (ch == 92) {
                    ch = 47;
                }
            }
            javaName.append((char)ch);
        }
        return javaName.toString();
    }

    private static String toJavaAlt64Name(String windowsName) {
        byte[] byteBuffer = Base64.altBase64ToByteArray(windowsName.substring(2));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < byteBuffer.length; ++i) {
            int firstbyte = byteBuffer[i++] & 0xFF;
            int secondbyte = byteBuffer[i] & 0xFF;
            result.append((char)((firstbyte << 8) + secondbyte));
        }
        return result.toString();
    }

    private static byte[] toWindowsName(String javaName) {
        StringBuilder windowsName = new StringBuilder();
        for (int i = 0; i < javaName.length(); ++i) {
            char ch = javaName.charAt(i);
            if (ch < ' ' || ch > '\u007f') {
                return WindowsPreferences.toWindowsAlt64Name(javaName);
            }
            if (ch == '\\') {
                windowsName.append("//");
                continue;
            }
            if (ch == '/') {
                windowsName.append('\\');
                continue;
            }
            if (ch >= 'A' && ch <= 'Z') {
                windowsName.append('/').append(ch);
                continue;
            }
            windowsName.append(ch);
        }
        return WindowsPreferences.stringToByteArray(windowsName.toString());
    }

    private static byte[] toWindowsAlt64Name(String javaName) {
        byte[] javaNameArray = new byte[2 * javaName.length()];
        int counter = 0;
        for (int i = 0; i < javaName.length(); ++i) {
            char ch = javaName.charAt(i);
            javaNameArray[counter++] = (byte)(ch >>> 8);
            javaNameArray[counter++] = (byte)ch;
        }
        return WindowsPreferences.stringToByteArray("/!" + Base64.byteArrayToAltBase64(javaNameArray));
    }

    /*
     * Enabled aggressive block sorting
     */
    private static String toJavaValueString(byte[] windowsNameArray) {
        String windowsName = WindowsPreferences.byteArrayToString(windowsNameArray);
        StringBuilder javaName = new StringBuilder();
        int i = 0;
        while (i < windowsName.length()) {
            int ch;
            block8: {
                block6: {
                    int next;
                    block7: {
                        ch = windowsName.charAt(i);
                        if (ch != 47) break block6;
                        next = 32;
                        if (windowsName.length() <= i + 1) break block7;
                        char c = windowsName.charAt(i + 1);
                        next = c;
                        if (c != 'u') break block7;
                        if (windowsName.length() < i + 6) {
                            return javaName.toString();
                        }
                        ch = (char)Integer.parseInt(windowsName.substring(i + 2, i + 6), 16);
                        i += 5;
                        break block8;
                    }
                    if (windowsName.length() > i + 1 && windowsName.charAt(i + 1) >= 'A' && next <= 90) {
                        ch = next;
                        ++i;
                        break block8;
                    } else if (windowsName.length() > i + 1 && next == 47) {
                        ch = 92;
                        ++i;
                    }
                    break block8;
                }
                if (ch == 92) {
                    ch = 47;
                }
            }
            javaName.append((char)ch);
            ++i;
        }
        return javaName.toString();
    }

    private static byte[] toWindowsValueString(String javaName) {
        StringBuilder windowsName = new StringBuilder();
        for (int i = 0; i < javaName.length(); ++i) {
            char ch = javaName.charAt(i);
            if (ch < ' ' || ch > '\u007f') {
                int j;
                windowsName.append("/u");
                String hex = Long.toHexString(javaName.charAt(i));
                StringBuilder hex4 = new StringBuilder(hex);
                hex4.reverse();
                int len = 4 - hex4.length();
                for (j = 0; j < len; ++j) {
                    hex4.append('0');
                }
                for (j = 0; j < 4; ++j) {
                    windowsName.append(hex4.charAt(3 - j));
                }
                continue;
            }
            if (ch == '\\') {
                windowsName.append("//");
                continue;
            }
            if (ch == '/') {
                windowsName.append('\\');
                continue;
            }
            if (ch >= 'A' && ch <= 'Z') {
                windowsName.append('/').append(ch);
                continue;
            }
            windowsName.append(ch);
        }
        return WindowsPreferences.stringToByteArray(windowsName.toString());
    }

    private long rootNativeHandle() {
        return this.isUserNode() ? -2147483647 : -2147483646;
    }

    private static byte[] stringToByteArray(String str) {
        byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); ++i) {
            result[i] = (byte)str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }

    private static String byteArrayToString(byte[] array) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length - 1; ++i) {
            result.append((char)array[i]);
        }
        return result.toString();
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
    }

    private static synchronized PlatformLogger logger() {
        if (logger == null) {
            logger = PlatformLogger.getLogger("java.util.prefs");
        }
        return logger;
    }

    static {
        WindowsPreferences.loadPrefsLib();
        WINDOWS_ROOT_PATH = WindowsPreferences.stringToByteArray("Software\\JavaSoft\\Prefs");
        INIT_SLEEP_TIME = 50;
        MAX_ATTEMPTS = 5;
    }
}

