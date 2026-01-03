/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.driver.unix.Who
 *  oshi.driver.unix.Xwininfo
 *  oshi.software.os.OSDesktopWindow
 *  oshi.software.os.OSProcess
 *  oshi.software.os.OSService
 *  oshi.software.os.OSSession
 *  oshi.software.os.OSThread
 *  oshi.software.os.OperatingSystem$OSVersionInfo
 *  oshi.software.os.OperatingSystem$ProcessFiltering
 *  oshi.util.UserGroupInfo
 */
package oshi.software.os;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.unix.Who;
import oshi.driver.unix.Xwininfo;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSSession;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.util.UserGroupInfo;

@ThreadSafe
public interface OperatingSystem {
    public String getFamily();

    public String getManufacturer();

    public OSVersionInfo getVersionInfo();

    public FileSystem getFileSystem();

    public InternetProtocolStats getInternetProtocolStats();

    default public List<OSProcess> getProcesses() {
        return this.getProcesses(null, null, 0);
    }

    public List<OSProcess> getProcesses(Predicate<OSProcess> var1, Comparator<OSProcess> var2, int var3);

    default public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return pids.stream().map(this::getProcess).filter(Objects::nonNull).filter(ProcessFiltering.VALID_PROCESS).collect(Collectors.toList());
    }

    public OSProcess getProcess(int var1);

    public List<OSProcess> getChildProcesses(int var1, Predicate<OSProcess> var2, Comparator<OSProcess> var3, int var4);

    public List<OSProcess> getDescendantProcesses(int var1, Predicate<OSProcess> var2, Comparator<OSProcess> var3, int var4);

    public int getProcessId();

    default public OSProcess getCurrentProcess() {
        return this.getProcess(this.getProcessId());
    }

    public int getProcessCount();

    public int getThreadId();

    public OSThread getCurrentThread();

    public int getThreadCount();

    public int getBitness();

    public long getSystemUptime();

    public long getSystemBootTime();

    default public boolean isElevated() {
        return UserGroupInfo.isElevated();
    }

    public NetworkParams getNetworkParams();

    default public List<OSService> getServices() {
        return new ArrayList<OSService>();
    }

    default public List<OSSession> getSessions() {
        return Who.queryWho();
    }

    default public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return Xwininfo.queryXWindows((boolean)visibleOnly);
    }
}

