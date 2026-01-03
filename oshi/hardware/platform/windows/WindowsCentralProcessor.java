/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.driver.windows.perfmon.LoadAverage
 *  oshi.driver.windows.perfmon.ProcessorInformation
 *  oshi.driver.windows.perfmon.ProcessorInformation$InterruptsProperty
 *  oshi.driver.windows.perfmon.ProcessorInformation$ProcessorFrequencyProperty
 *  oshi.driver.windows.perfmon.ProcessorInformation$ProcessorTickCountProperty
 *  oshi.driver.windows.perfmon.ProcessorInformation$ProcessorUtilityTickCountProperty
 *  oshi.driver.windows.perfmon.ProcessorInformation$SystemTickCountProperty
 *  oshi.driver.windows.perfmon.SystemInformation
 *  oshi.driver.windows.perfmon.SystemInformation$ContextSwitchProperty
 *  oshi.hardware.CentralProcessor$TickType
 *  oshi.jna.platform.windows.PowrProf
 *  oshi.jna.platform.windows.PowrProf$ProcessorPowerInformation
 */
package oshi.hardware.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinReg;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.LogicalProcessorInformation;
import oshi.driver.windows.perfmon.LoadAverage;
import oshi.driver.windows.perfmon.ProcessorInformation;
import oshi.driver.windows.perfmon.SystemInformation;
import oshi.driver.windows.wmi.Win32Processor;
import oshi.hardware.CentralProcessor;
import oshi.hardware.common.AbstractCentralProcessor;
import oshi.jna.Struct;
import oshi.jna.platform.windows.Kernel32;
import oshi.jna.platform.windows.PowrProf;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Quartet;
import oshi.util.tuples.Triplet;

@ThreadSafe
final class WindowsCentralProcessor
extends AbstractCentralProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsCentralProcessor.class);
    private Map<String, Integer> numaNodeProcToLogicalProcMap;
    private static final boolean USE_LEGACY_SYSTEM_COUNTERS = GlobalConfig.get("oshi.os.windows.legacy.system.counters", false);
    private static final boolean USE_LOAD_AVERAGE = GlobalConfig.get("oshi.os.windows.loadaverage", false);
    private static final boolean USE_CPU_UTILITY;
    private final Supplier<Pair<List<String>, Map<ProcessorInformation.ProcessorUtilityTickCountProperty, List<Long>>>> processorUtilityCounters = USE_CPU_UTILITY ? Memoizer.memoize(WindowsCentralProcessor::queryProcessorUtilityCounters, TimeUnit.MILLISECONDS.toNanos(300L)) : null;
    private Map<ProcessorInformation.ProcessorUtilityTickCountProperty, List<Long>> initialUtilityCounters = USE_CPU_UTILITY ? this.processorUtilityCounters.get().getB() : null;
    private Long utilityBaseMultiplier = null;

    WindowsCentralProcessor() {
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String processorID;
        String cpuVendor = "";
        String cpuName = "";
        String cpuIdentifier = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";
        long cpuVendorFreq = 0L;
        boolean cpu64bit = false;
        String cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\";
        String[] processorIds = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\");
        if (processorIds.length > 0) {
            String cpuRegistryPath = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\" + processorIds[0];
            cpuVendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "VendorIdentifier");
            cpuName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "ProcessorNameString");
            cpuIdentifier = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "Identifier");
            try {
                cpuVendorFreq = (long)Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "~MHz") * 1000000L;
            }
            catch (Win32Exception win32Exception) {
                // empty catch block
            }
        }
        if (!cpuIdentifier.isEmpty()) {
            cpuFamily = WindowsCentralProcessor.parseIdentifier(cpuIdentifier, "Family");
            cpuModel = WindowsCentralProcessor.parseIdentifier(cpuIdentifier, "Model");
            cpuStepping = WindowsCentralProcessor.parseIdentifier(cpuIdentifier, "Stepping");
        }
        try (Struct.CloseableSystemInfo sysinfo = new Struct.CloseableSystemInfo();){
            Kernel32.INSTANCE.GetNativeSystemInfo(sysinfo);
            int processorArchitecture = sysinfo.processorArchitecture.pi.wProcessorArchitecture.intValue();
            if (processorArchitecture == 9 || processorArchitecture == 12 || processorArchitecture == 6) {
                cpu64bit = true;
            }
        }
        WbemcliUtil.WmiResult<Win32Processor.ProcessorIdProperty> processorId = Win32Processor.queryProcessorId();
        if (processorId.getResultCount() > 0) {
            processorID = WmiUtil.getString(processorId, Win32Processor.ProcessorIdProperty.PROCESSORID, 0);
        } else {
            String[] stringArray;
            if (cpu64bit) {
                String[] stringArray2 = new String[1];
                stringArray = stringArray2;
                stringArray2[0] = "ia64";
            } else {
                stringArray = new String[]{};
            }
            processorID = WindowsCentralProcessor.createProcessorID(cpuStepping, cpuModel, cpuFamily, stringArray);
        }
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, cpuVendorFreq);
    }

    private static String parseIdentifier(String identifier, String key) {
        String[] idSplit = ParseUtil.whitespaces.split(identifier);
        boolean found = false;
        for (String s : idSplit) {
            if (found) {
                return s;
            }
            found = s.equals(key);
        }
        return "";
    }

    @Override
    protected Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>> initProcessorCounts() {
        Triplet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>> lpi;
        if (VersionHelpers.IsWindows7OrGreater()) {
            lpi = LogicalProcessorInformation.getLogicalProcessorInformationEx();
            int curNode = -1;
            int procNum = 0;
            int lp = 0;
            this.numaNodeProcToLogicalProcMap = new HashMap<String, Integer>();
            for (CentralProcessor.LogicalProcessor logProc : lpi.getA()) {
                int node = logProc.getNumaNode();
                if (node != curNode) {
                    curNode = node;
                    procNum = 0;
                }
                this.numaNodeProcToLogicalProcMap.put(String.format(Locale.ROOT, "%d,%d", logProc.getNumaNode(), procNum++), lp++);
            }
        } else {
            lpi = LogicalProcessorInformation.getLogicalProcessorInformation();
        }
        List featureFlags = Arrays.stream(Kernel32.ProcessorFeature.values()).filter(f -> Kernel32.INSTANCE.IsProcessorFeaturePresent(f.value())).map(Enum::name).collect(Collectors.toList());
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>>(lpi.getA(), lpi.getB(), lpi.getC(), featureFlags);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        if (USE_LEGACY_SYSTEM_COUNTERS) {
            WinBase.FILETIME lpIdleTime = new WinBase.FILETIME();
            WinBase.FILETIME lpKernelTime = new WinBase.FILETIME();
            WinBase.FILETIME lpUserTime = new WinBase.FILETIME();
            if (!Kernel32.INSTANCE.GetSystemTimes(lpIdleTime, lpKernelTime, lpUserTime)) {
                LOG.error("Failed to update system idle/kernel/user times. Error code: {}", (Object)Native.getLastError());
                return ticks;
            }
            Map valueMap = ProcessorInformation.querySystemCounters();
            ticks[CentralProcessor.TickType.IRQ.getIndex()] = valueMap.getOrDefault(ProcessorInformation.SystemTickCountProperty.PERCENTINTERRUPTTIME, 0L) / 10000L;
            ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] = valueMap.getOrDefault(ProcessorInformation.SystemTickCountProperty.PERCENTDPCTIME, 0L) / 10000L;
            ticks[CentralProcessor.TickType.IDLE.getIndex()] = lpIdleTime.toDWordLong().longValue() / 10000L;
            ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = lpKernelTime.toDWordLong().longValue() / 10000L - ticks[CentralProcessor.TickType.IDLE.getIndex()];
            ticks[CentralProcessor.TickType.USER.getIndex()] = lpUserTime.toDWordLong().longValue() / 10000L;
            int n = CentralProcessor.TickType.SYSTEM.getIndex();
            ticks[n] = ticks[n] - (ticks[CentralProcessor.TickType.IRQ.getIndex()] + ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]);
            return ticks;
        }
        long[][] procTicks = this.getProcessorCpuLoadTicks();
        for (int i = 0; i < ticks.length; ++i) {
            for (long[] procTick : procTicks) {
                int n = i;
                ticks[n] = ticks[n] + procTick[i];
            }
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        if (VersionHelpers.IsWindows7OrGreater()) {
            Pair instanceValuePair = ProcessorInformation.queryFrequencyCounters();
            List instances = (List)instanceValuePair.getA();
            Map valueMap = (Map)instanceValuePair.getB();
            List percentMaxList = (List)valueMap.get(ProcessorInformation.ProcessorFrequencyProperty.PERCENTOFMAXIMUMFREQUENCY);
            if (!instances.isEmpty()) {
                long maxFreq = this.getMaxFreq();
                long[] freqs = new long[this.getLogicalProcessorCount()];
                for (String instance : instances) {
                    int cpu;
                    int n = cpu = instance.contains(",") ? this.numaNodeProcToLogicalProcMap.getOrDefault(instance, 0) : ParseUtil.parseIntOrDefault(instance, 0);
                    if (cpu >= this.getLogicalProcessorCount()) continue;
                    freqs[cpu] = (Long)percentMaxList.get(cpu) * maxFreq / 100L;
                }
                return freqs;
            }
        }
        return this.queryNTPower(2);
    }

    @Override
    public long queryMaxFreq() {
        long[] freqs = this.queryNTPower(1);
        return Arrays.stream(freqs).max().orElse(-1L);
    }

    private long[] queryNTPower(int fieldIndex) {
        PowrProf.ProcessorPowerInformation ppi = new PowrProf.ProcessorPowerInformation();
        PowrProf.ProcessorPowerInformation[] ppiArray = (PowrProf.ProcessorPowerInformation[])ppi.toArray(this.getLogicalProcessorCount());
        long[] freqs = new long[this.getLogicalProcessorCount()];
        if (0 != PowrProf.INSTANCE.CallNtPowerInformation(11, null, 0, ppiArray[0].getPointer(), ppi.size() * ppiArray.length)) {
            LOG.error("Unable to get Processor Information");
            Arrays.fill(freqs, -1L);
            return freqs;
        }
        for (int i = 0; i < freqs.length; ++i) {
            freqs[i] = fieldIndex == 1 ? (long)ppiArray[i].maxMhz * 1000000L : (fieldIndex == 2 ? (long)ppiArray[i].currentMhz * 1000000L : -1L);
            if (freqs[i] != 0L) continue;
            freqs[i] = this.getProcessorIdentifier().getVendorFreq();
        }
        return freqs;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        return LoadAverage.queryLoadAverage((int)nelem);
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        List<Long> idleList;
        List<Long> softIrqList;
        List<Long> irqList;
        List<Long> userList;
        List<Long> systemList;
        Map<ProcessorInformation.ProcessorUtilityTickCountProperty, List<Long>> valueMap;
        List<String> instances;
        Pair<List<String>, Map<ProcessorInformation.ProcessorUtilityTickCountProperty, List<Long>>> instanceValuePair;
        List<Long> baseList = null;
        List<Long> systemUtility = null;
        List<Long> processorUtility = null;
        List<Long> processorUtilityBase = null;
        List<Long> initSystemList = null;
        List<Long> initUserList = null;
        List<Long> initBase = null;
        List<Long> initSystemUtility = null;
        List<Long> initProcessorUtility = null;
        List<Long> initProcessorUtilityBase = null;
        if (USE_CPU_UTILITY) {
            instanceValuePair = this.processorUtilityCounters.get();
            instances = instanceValuePair.getA();
            valueMap = instanceValuePair.getB();
            systemList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTDPCTIME);
            idleList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPROCESSORTIME);
            baseList = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.TIMESTAMP_SYS100NS);
            systemUtility = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            processorUtility = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY);
            processorUtilityBase = valueMap.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);
            initSystemList = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDTIME);
            initUserList = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTUSERTIME);
            initBase = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.TIMESTAMP_SYS100NS);
            initSystemUtility = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            initProcessorUtility = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY);
            initProcessorUtilityBase = this.initialUtilityCounters.get(ProcessorInformation.ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);
        } else {
            instanceValuePair = ProcessorInformation.queryProcessorCounters();
            instances = (List<String>)instanceValuePair.getA();
            valueMap = instanceValuePair.getB();
            systemList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTDPCTIME);
            idleList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTPROCESSORTIME);
        }
        int ncpu = this.getLogicalProcessorCount();
        long[][] ticks = new long[ncpu][CentralProcessor.TickType.values().length];
        if (instances.isEmpty() || systemList == null || userList == null || irqList == null || softIrqList == null || idleList == null || USE_CPU_UTILITY && (baseList == null || systemUtility == null || processorUtility == null || processorUtilityBase == null || initSystemList == null || initUserList == null || initBase == null || initSystemUtility == null || initProcessorUtility == null || initProcessorUtilityBase == null)) {
            return ticks;
        }
        for (String instance : instances) {
            long deltaBase;
            long multiplier;
            long deltaT;
            int cpu;
            int n = cpu = instance.contains(",") ? this.numaNodeProcToLogicalProcMap.getOrDefault(instance, 0) : ParseUtil.parseIntOrDefault(instance, 0);
            if (cpu >= ncpu) continue;
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = systemList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = userList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] = irqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()] = softIrqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = idleList.get(cpu);
            if (USE_CPU_UTILITY && (deltaT = baseList.get(cpu) - initBase.get(cpu)) > 0L && (multiplier = this.lazilyCalculateMultiplier(deltaBase = processorUtilityBase.get(cpu) - initProcessorUtilityBase.get(cpu), deltaT)) > 0L) {
                long deltaProc = processorUtility.get(cpu) - initProcessorUtility.get(cpu);
                long deltaSys = systemUtility.get(cpu) - initSystemUtility.get(cpu);
                long newUser = initUserList.get(cpu) + multiplier * (deltaProc - deltaSys) / 100L;
                long newSystem = initSystemList.get(cpu) + multiplier * deltaSys / 100L;
                long delta = newUser - ticks[cpu][CentralProcessor.TickType.USER.getIndex()];
                ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = newUser;
                ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = newSystem;
                long[] lArray = ticks[cpu];
                int n2 = CentralProcessor.TickType.IDLE.getIndex();
                lArray[n2] = lArray[n2] - (delta += newSystem - ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()]);
            }
            long[] lArray = ticks[cpu];
            int n3 = CentralProcessor.TickType.SYSTEM.getIndex();
            lArray[n3] = lArray[n3] - (ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] + ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()]);
            long[] lArray2 = ticks[cpu];
            int n4 = CentralProcessor.TickType.SYSTEM.getIndex();
            lArray2[n4] = lArray2[n4] / 10000L;
            long[] lArray3 = ticks[cpu];
            int n5 = CentralProcessor.TickType.USER.getIndex();
            lArray3[n5] = lArray3[n5] / 10000L;
            long[] lArray4 = ticks[cpu];
            int n6 = CentralProcessor.TickType.IRQ.getIndex();
            lArray4[n6] = lArray4[n6] / 10000L;
            long[] lArray5 = ticks[cpu];
            int n7 = CentralProcessor.TickType.SOFTIRQ.getIndex();
            lArray5[n7] = lArray5[n7] / 10000L;
            long[] lArray6 = ticks[cpu];
            int n8 = CentralProcessor.TickType.IDLE.getIndex();
            lArray6[n8] = lArray6[n8] / 10000L;
        }
        return ticks;
    }

    private synchronized long lazilyCalculateMultiplier(long deltaBase, long deltaT) {
        if (this.utilityBaseMultiplier == null) {
            if (deltaT >> 32 > 0L) {
                this.initialUtilityCounters = this.processorUtilityCounters.get().getB();
                return 0L;
            }
            if (deltaBase <= 0L) {
                deltaBase += 0x100000000L;
            }
            long multiplier = Math.round((double)deltaT / (double)deltaBase);
            if (deltaT < 50000000L) {
                return multiplier;
            }
            this.utilityBaseMultiplier = multiplier;
        }
        return this.utilityBaseMultiplier;
    }

    private static Pair<List<String>, Map<ProcessorInformation.ProcessorUtilityTickCountProperty, List<Long>>> queryProcessorUtilityCounters() {
        return ProcessorInformation.queryProcessorCapacityCounters();
    }

    @Override
    public long queryContextSwitches() {
        return SystemInformation.queryContextSwitchCounters().getOrDefault(SystemInformation.ContextSwitchProperty.CONTEXTSWITCHESPERSEC, 0L);
    }

    @Override
    public long queryInterrupts() {
        return ProcessorInformation.queryInterruptCounters().getOrDefault(ProcessorInformation.InterruptsProperty.INTERRUPTSPERSEC, 0L);
    }

    static {
        if (USE_LOAD_AVERAGE) {
            LoadAverage.startDaemon();
        }
        USE_CPU_UTILITY = VersionHelpers.IsWindows8OrGreater() && GlobalConfig.get("oshi.os.windows.cpu.utility", false);
    }
}

