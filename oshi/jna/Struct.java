/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna;

import com.sun.jna.platform.win32.Pdh;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinBase;
import oshi.util.Util;

public interface Struct {

    public static class CloseableSystemInfo
    extends WinBase.SYSTEM_INFO
    implements AutoCloseable {
        @Override
        public void close() {
            Util.freeMemory(this.getPointer());
        }
    }

    public static class CloseablePerformanceInformation
    extends Psapi.PERFORMANCE_INFORMATION
    implements AutoCloseable {
        @Override
        public void close() {
            Util.freeMemory(this.getPointer());
        }
    }

    public static class CloseablePdhRawCounter
    extends Pdh.PDH_RAW_COUNTER
    implements AutoCloseable {
        @Override
        public void close() {
            Util.freeMemory(this.getPointer());
        }
    }
}

