/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.W32ServiceManager
 *  com.sun.jna.platform.win32.Winsvc$ENUM_SERVICE_STATUS_PROCESS
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.driver.windows.EnumWindows
 *  oshi.driver.windows.registry.HkeyUserData
 *  oshi.driver.windows.registry.NetSessionData
 *  oshi.driver.windows.registry.ProcessPerformanceData
 *  oshi.driver.windows.registry.ProcessPerformanceData$PerfCounterBlock
 *  oshi.driver.windows.registry.ProcessWtsData
 *  oshi.driver.windows.registry.SessionWtsData
 *  oshi.driver.windows.registry.ThreadPerformanceData
 *  oshi.driver.windows.registry.ThreadPerformanceData$PerfCounterBlock
 *  oshi.driver.windows.wmi.Win32OperatingSystem
 *  oshi.software.os.OSDesktopWindow
 *  oshi.software.os.OSProcess
 *  oshi.software.os.OSService
 *  oshi.software.os.OSService$State
 *  oshi.software.os.OSSession
 *  oshi.software.os.OSThread
 *  oshi.software.os.OperatingSystem$OSVersionInfo
 *  oshi.software.os.OperatingSystem$ProcessFiltering
 *  oshi.software.os.windows.WindowsFileSystem
 *  oshi.software.os.windows.WindowsInternetProtocolStats
 *  oshi.software.os.windows.WindowsNetworkParams
 *  oshi.software.os.windows.WindowsOSProcess
 *  oshi.software.os.windows.WindowsOSThread
 */
package oshi.software.os.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.W32ServiceManager;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winsvc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.EnumWindows;
import oshi.driver.windows.registry.HkeyUserData;
import oshi.driver.windows.registry.NetSessionData;
import oshi.driver.windows.registry.ProcessPerformanceData;
import oshi.driver.windows.registry.ProcessWtsData;
import oshi.driver.windows.registry.SessionWtsData;
import oshi.driver.windows.registry.ThreadPerformanceData;
import oshi.driver.windows.wmi.Win32OperatingSystem;
import oshi.driver.windows.wmi.Win32Processor;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.software.common.AbstractOperatingSystem;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OSService;
import oshi.software.os.OSSession;
import oshi.software.os.OSThread;
import oshi.software.os.OperatingSystem;
import oshi.software.os.windows.WindowsFileSystem;
import oshi.software.os.windows.WindowsInternetProtocolStats;
import oshi.software.os.windows.WindowsNetworkParams;
import oshi.software.os.windows.WindowsOSProcess;
import oshi.software.os.windows.WindowsOSThread;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public class WindowsOperatingSystem
extends AbstractOperatingSystem {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsOperatingSystem.class);
    private static final boolean USE_PROCSTATE_SUSPENDED = GlobalConfig.get("oshi.os.windows.procstate.suspended", false);
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static Supplier<String> systemLog = Memoizer.memoize(WindowsOperatingSystem::querySystemLog, TimeUnit.HOURS.toNanos(1L));
    private static final long BOOTTIME = WindowsOperatingSystem.querySystemBootTime();
    private static final boolean X86;
    private static final boolean WOW;
    private Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromRegistry = Memoizer.memoize(WindowsOperatingSystem::queryProcessMapFromRegistry, Memoizer.defaultExpiration());
    private Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromPerfCounters = Memoizer.memoize(WindowsOperatingSystem::queryProcessMapFromPerfCounters, Memoizer.defaultExpiration());
    private Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromRegistry = Memoizer.memoize(WindowsOperatingSystem::queryThreadMapFromRegistry, Memoizer.defaultExpiration());
    private Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromPerfCounters = Memoizer.memoize(WindowsOperatingSystem::queryThreadMapFromPerfCounters, Memoizer.defaultExpiration());

    @Override
    public String queryManufacturer() {
        return "Microsoft";
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        String version = System.getProperty("os.name");
        if (version.startsWith("Windows ")) {
            version = version.substring(8);
        }
        String sp = null;
        int suiteMask = 0;
        String buildNumber = "";
        WbemcliUtil.WmiResult versionInfo = Win32OperatingSystem.queryOsVersion();
        if (versionInfo.getResultCount() > 0) {
            sp = WmiUtil.getString(versionInfo, Win32OperatingSystem.OSVersionProperty.CSDVERSION, 0);
            if (!sp.isEmpty() && !"unknown".equals(sp)) {
                version = version + " " + sp.replace("Service Pack ", "SP");
            }
            suiteMask = WmiUtil.getUint32(versionInfo, Win32OperatingSystem.OSVersionProperty.SUITEMASK, 0);
            buildNumber = WmiUtil.getString(versionInfo, Win32OperatingSystem.OSVersionProperty.BUILDNUMBER, 0);
        }
        String codeName = WindowsOperatingSystem.parseCodeName(suiteMask);
        if ("10".equals(version) && buildNumber.compareTo("22000") >= 0) {
            version = "11";
        } else if ("Server 2019".equals(version) && buildNumber.compareTo("20347") > 0) {
            version = "Server 2022";
        }
        return new Pair<String, OperatingSystem.OSVersionInfo>("Windows", new OperatingSystem.OSVersionInfo(version, codeName, buildNumber));
    }

    private static String parseCodeName(int suiteMask) {
        ArrayList<String> suites = new ArrayList<String>();
        if ((suiteMask & 2) != 0) {
            suites.add("Enterprise");
        }
        if ((suiteMask & 4) != 0) {
            suites.add("BackOffice");
        }
        if ((suiteMask & 8) != 0) {
            suites.add("Communications Server");
        }
        if ((suiteMask & 0x80) != 0) {
            suites.add("Datacenter");
        }
        if ((suiteMask & 0x200) != 0) {
            suites.add("Home");
        }
        if ((suiteMask & 0x400) != 0) {
            suites.add("Web Server");
        }
        if ((suiteMask & 0x2000) != 0) {
            suites.add("Storage Server");
        }
        if ((suiteMask & 0x4000) != 0) {
            suites.add("Compute Cluster");
        }
        if ((suiteMask & 0x8000) != 0) {
            suites.add("Home Server");
        }
        return String.join((CharSequence)",", suites);
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        WbemcliUtil.WmiResult<Win32Processor.BitnessProperty> bitnessMap;
        if (jvmBitness < 64 && System.getenv("ProgramFiles(x86)") != null && IS_VISTA_OR_GREATER && (bitnessMap = Win32Processor.queryBitness()).getResultCount() > 0) {
            return WmiUtil.getUint16(bitnessMap, Win32Processor.BitnessProperty.ADDRESSWIDTH, 0);
        }
        return jvmBitness;
    }

    @Override
    public boolean isElevated() {
        return Advapi32Util.isCurrentProcessElevated();
    }

    @Override
    public FileSystem getFileSystem() {
        return new WindowsFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new WindowsInternetProtocolStats();
    }

    @Override
    public List<OSSession> getSessions() {
        List whoList = HkeyUserData.queryUserSessions();
        whoList.addAll(SessionWtsData.queryUserSessions());
        whoList.addAll(NetSessionData.queryUserSessions());
        return whoList;
    }

    @Override
    public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return this.processMapToList(pids);
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return this.processMapToList(null);
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        Set<Integer> descendantPids = WindowsOperatingSystem.getChildrenOrDescendants(WindowsOperatingSystem.getParentPidsFromSnapshot(), parentPid, false);
        return this.processMapToList(descendantPids);
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        Set<Integer> descendantPids = WindowsOperatingSystem.getChildrenOrDescendants(WindowsOperatingSystem.getParentPidsFromSnapshot(), parentPid, true);
        return this.processMapToList(descendantPids);
    }

    private static Map<Integer, Integer> getParentPidsFromSnapshot() {
        HashMap<Integer, Integer> parentPidMap = new HashMap<Integer, Integer>();
        try (ByRef.CloseablePROCESSENTRY32ByReference processEntry = new ByRef.CloseablePROCESSENTRY32ByReference();){
            WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0L));
            try {
                while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                    parentPidMap.put(processEntry.th32ProcessID.intValue(), processEntry.th32ParentProcessID.intValue());
                }
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(snapshot);
            }
        }
        return parentPidMap;
    }

    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procList = this.processMapToList(Arrays.asList(pid));
        return procList.isEmpty() ? null : procList.get(0);
    }

    private List<OSProcess> processMapToList(Collection<Integer> pids) {
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap = this.processMapFromRegistry.get();
        if (processMap == null || processMap.isEmpty()) {
            processMap = pids == null ? this.processMapFromPerfCounters.get() : ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap = null;
        if (USE_PROCSTATE_SUSPENDED && ((threadMap = this.threadMapFromRegistry.get()) == null || threadMap.isEmpty())) {
            threadMap = pids == null ? this.threadMapFromPerfCounters.get() : ThreadPerformanceData.buildThreadMapFromPerfCounters(pids);
        }
        Map processWtsMap = ProcessWtsData.queryProcessWtsMap(pids);
        HashSet mapKeys = new HashSet(processWtsMap.keySet());
        mapKeys.retainAll(processMap.keySet());
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> finalProcessMap = processMap;
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> finalThreadMap = threadMap;
        return ((Stream)mapKeys.stream().parallel()).map(pid -> new WindowsOSProcess(pid.intValue(), this, finalProcessMap, processWtsMap, finalThreadMap)).filter(OperatingSystem.ProcessFiltering.VALID_PROCESS).collect(Collectors.toList());
    }

    private static Map<Integer, ProcessPerformanceData.PerfCounterBlock> queryProcessMapFromRegistry() {
        return ProcessPerformanceData.buildProcessMapFromRegistry(null);
    }

    private static Map<Integer, ProcessPerformanceData.PerfCounterBlock> queryProcessMapFromPerfCounters() {
        return ProcessPerformanceData.buildProcessMapFromPerfCounters(null);
    }

    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromRegistry() {
        return ThreadPerformanceData.buildThreadMapFromRegistry(null);
    }

    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromPerfCounters() {
        return ThreadPerformanceData.buildThreadMapFromPerfCounters(null);
    }

    @Override
    public int getProcessId() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    @Override
    public int getProcessCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation();){
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                LOG.error("Failed to get Performance Info. Error code: {}", (Object)Kernel32.INSTANCE.GetLastError());
                int n = 0;
                return n;
            }
            int n = perfInfo.ProcessCount.intValue();
            return n;
        }
    }

    @Override
    public int getThreadId() {
        return Kernel32.INSTANCE.GetCurrentThreadId();
    }

    @Override
    public OSThread getCurrentThread() {
        OSProcess proc = this.getCurrentProcess();
        int tid = this.getThreadId();
        return proc.getThreadDetails().stream().filter(t -> t.getThreadId() == tid).findFirst().orElse((OSThread)new WindowsOSThread(proc.getProcessID(), tid, null, null));
    }

    @Override
    public int getThreadCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation();){
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                LOG.error("Failed to get Performance Info. Error code: {}", (Object)Kernel32.INSTANCE.GetLastError());
                int n = 0;
                return n;
            }
            int n = perfInfo.ThreadCount.intValue();
            return n;
        }
    }

    @Override
    public long getSystemUptime() {
        return WindowsOperatingSystem.querySystemUptime();
    }

    private static long querySystemUptime() {
        if (IS_VISTA_OR_GREATER) {
            return Kernel32.INSTANCE.GetTickCount64() / 1000L;
        }
        return (long)Kernel32.INSTANCE.GetTickCount() / 1000L;
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    private static long querySystemBootTime() {
        String eventLog = systemLog.get();
        if (eventLog != null) {
            try {
                Advapi32Util.EventLogIterator iter = new Advapi32Util.EventLogIterator(null, eventLog, 8);
                long event6005Time = 0L;
                while (iter.hasNext()) {
                    Advapi32Util.EventLogRecord logRecord = iter.next();
                    if (logRecord.getStatusCode() == 12) {
                        return logRecord.getRecord().TimeGenerated.longValue();
                    }
                    if (logRecord.getStatusCode() != 6005) continue;
                    if (event6005Time > 0L) {
                        return event6005Time;
                    }
                    event6005Time = logRecord.getRecord().TimeGenerated.longValue();
                }
                if (event6005Time > 0L) {
                    return event6005Time;
                }
            }
            catch (Win32Exception e) {
                LOG.warn("Can't open event log \"{}\".", (Object)eventLog);
            }
        }
        return System.currentTimeMillis() / 1000L - WindowsOperatingSystem.querySystemUptime();
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new WindowsNetworkParams();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static boolean enableDebugPrivilege() {
        try (ByRef.CloseableHANDLEByReference hToken = new ByRef.CloseableHANDLEByReference();){
            boolean success = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), 40, hToken);
            if (!success) {
                LOG.error("OpenProcessToken failed. Error: {}", (Object)Native.getLastError());
                boolean bl = false;
                return bl;
            }
            try {
                WinNT.LUID luid = new WinNT.LUID();
                success = Advapi32.INSTANCE.LookupPrivilegeValue(null, "SeDebugPrivilege", luid);
                if (!success) {
                    LOG.error("LookupPrivilegeValue failed. Error: {}", (Object)Native.getLastError());
                    boolean bl = false;
                    return bl;
                }
                WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
                tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new WinDef.DWORD(2L));
                success = Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
                int err = Native.getLastError();
                if (!success) {
                    LOG.error("AdjustTokenPrivileges failed. Error: {}", (Object)err);
                    boolean bl = false;
                    return bl;
                }
                if (err != 1300) return true;
                LOG.debug("Debug privileges not enabled.");
                boolean bl = false;
                return bl;
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            }
        }
    }

    @Override
    public List<OSService> getServices() {
        ArrayList<OSService> arrayList;
        W32ServiceManager sm = new W32ServiceManager();
        try {
            sm.open(4);
            Winsvc.ENUM_SERVICE_STATUS_PROCESS[] services = sm.enumServicesStatusExProcess(48, 3, null);
            ArrayList<OSService> svcArray = new ArrayList<OSService>();
            for (Winsvc.ENUM_SERVICE_STATUS_PROCESS service : services) {
                OSService.State state;
                switch (service.ServiceStatusProcess.dwCurrentState) {
                    case 1: {
                        state = OSService.State.STOPPED;
                        break;
                    }
                    case 4: {
                        state = OSService.State.RUNNING;
                        break;
                    }
                    default: {
                        state = OSService.State.OTHER;
                    }
                }
                svcArray.add(new OSService(service.lpDisplayName, service.ServiceStatusProcess.dwProcessId, state));
            }
            arrayList = svcArray;
        }
        catch (Throwable throwable) {
            try {
                try {
                    sm.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Win32Exception ex) {
                LOG.error("Win32Exception: {}", (Object)ex.getMessage());
                return Collections.emptyList();
            }
        }
        sm.close();
        return arrayList;
    }

    private static String querySystemLog() {
        String systemLog = GlobalConfig.get("oshi.os.windows.eventlog", "System");
        if (systemLog.isEmpty()) {
            return null;
        }
        WinNT.HANDLE h = Advapi32.INSTANCE.OpenEventLog(null, systemLog);
        if (h == null) {
            LOG.warn("Unable to open configured system Event log \"{}\". Calculating boot time from uptime.", (Object)systemLog);
            return null;
        }
        return systemLog;
    }

    @Override
    public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return EnumWindows.queryDesktopWindows((boolean)visibleOnly);
    }

    static boolean isX86() {
        return X86;
    }

    private static boolean isCurrentX86() {
        try (Struct.CloseableSystemInfo sysinfo = new Struct.CloseableSystemInfo();){
            Kernel32.INSTANCE.GetNativeSystemInfo(sysinfo);
            boolean bl = 0 == sysinfo.processorArchitecture.pi.wProcessorArchitecture.intValue();
            return bl;
        }
    }

    static boolean isWow() {
        return WOW;
    }

    static boolean isWow(WinNT.HANDLE h) {
        if (X86) {
            return true;
        }
        try (ByRef.CloseableIntByReference isWow = new ByRef.CloseableIntByReference();){
            Kernel32.INSTANCE.IsWow64Process(h, isWow);
            boolean bl = isWow.getValue() != 0;
            return bl;
        }
    }

    private static boolean isCurrentWow() {
        if (X86) {
            return true;
        }
        WinNT.HANDLE h = Kernel32.INSTANCE.GetCurrentProcess();
        return h == null ? false : WindowsOperatingSystem.isWow(h);
    }

    static {
        WindowsOperatingSystem.enableDebugPrivilege();
        X86 = WindowsOperatingSystem.isCurrentX86();
        WOW = WindowsOperatingSystem.isCurrentWow();
    }
}

