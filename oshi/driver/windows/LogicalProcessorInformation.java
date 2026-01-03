/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.driver.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.WinNT;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.wmi.Win32Processor;
import oshi.hardware.CentralProcessor;
import oshi.hardware.common.AbstractCentralProcessor;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class LogicalProcessorInformation {
    private static final boolean IS_WIN10_OR_GREATER = VersionHelpers.IsWindows10OrGreater();

    private LogicalProcessorInformation() {
    }

    public static Triplet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>> getLogicalProcessorInformationEx() {
        WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX[] procInfo = Kernel32Util.getLogicalProcessorInformationEx(65535);
        ArrayList<WinNT.GROUP_AFFINITY[]> packages = new ArrayList<WinNT.GROUP_AFFINITY[]>();
        HashSet<CentralProcessor.ProcessorCache> caches = new HashSet<CentralProcessor.ProcessorCache>();
        ArrayList<WinNT.GROUP_AFFINITY> cores = new ArrayList<WinNT.GROUP_AFFINITY>();
        ArrayList<WinNT.NUMA_NODE_RELATIONSHIP> numaNodes = new ArrayList<WinNT.NUMA_NODE_RELATIONSHIP>();
        HashMap<WinNT.GROUP_AFFINITY, Integer> coreEfficiencyMap = new HashMap<WinNT.GROUP_AFFINITY, Integer>();
        block6: for (WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX info : procInfo) {
            switch (info.relationship) {
                case 3: {
                    packages.add(((WinNT.PROCESSOR_RELATIONSHIP)info).groupMask);
                    continue block6;
                }
                case 2: {
                    WinNT.CACHE_RELATIONSHIP cache = (WinNT.CACHE_RELATIONSHIP)info;
                    caches.add(new CentralProcessor.ProcessorCache(cache.level, cache.associativity, cache.lineSize, cache.size, CentralProcessor.ProcessorCache.Type.values()[cache.type]));
                    continue block6;
                }
                case 0: {
                    WinNT.PROCESSOR_RELATIONSHIP core = (WinNT.PROCESSOR_RELATIONSHIP)info;
                    cores.add(core.groupMask[0]);
                    if (!IS_WIN10_OR_GREATER) continue block6;
                    coreEfficiencyMap.put(core.groupMask[0], Integer.valueOf(core.efficiencyClass));
                    continue block6;
                }
                case 1: {
                    numaNodes.add((WinNT.NUMA_NODE_RELATIONSHIP)info);
                    continue block6;
                }
            }
        }
        cores.sort(Comparator.comparing(c -> (long)c.group * 64L + (long)Long.numberOfTrailingZeros(c.mask.longValue())));
        packages.sort(Comparator.comparing(p -> (long)p[0].group * 64L + (long)Long.numberOfTrailingZeros(p[0].mask.longValue())));
        numaNodes.sort(Comparator.comparing(n -> n.nodeNumber));
        HashMap<Integer, String> processorIdMap = new HashMap<Integer, String>();
        WbemcliUtil.WmiResult<Win32Processor.ProcessorIdProperty> processorId = Win32Processor.queryProcessorId();
        for (int pkg = 0; pkg < processorId.getResultCount(); ++pkg) {
            processorIdMap.put(pkg, WmiUtil.getString(processorId, Win32Processor.ProcessorIdProperty.PROCESSORID, pkg));
        }
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
        HashMap<Integer, Integer> corePkgMap = new HashMap<Integer, Integer>();
        HashMap<Integer, String> pkgCpuidMap = new HashMap<Integer, String>();
        for (WinNT.NUMA_NODE_RELATIONSHIP node : numaNodes) {
            int nodeNum = node.nodeNumber;
            short group = node.groupMask.group;
            long mask = node.groupMask.mask.longValue();
            int lowBit = Long.numberOfTrailingZeros(mask);
            int hiBit = 63 - Long.numberOfLeadingZeros(mask);
            for (int lp = lowBit; lp <= hiBit; ++lp) {
                if ((mask & 1L << lp) == 0L) continue;
                int coreId = LogicalProcessorInformation.getMatchingCore(cores, group, lp);
                int pkgId = LogicalProcessorInformation.getMatchingPackage(packages, group, lp);
                corePkgMap.put(coreId, pkgId);
                pkgCpuidMap.put(coreId, processorIdMap.getOrDefault(pkgId, ""));
                CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(lp, coreId, pkgId, nodeNum, group);
                logProcs.add(logProc);
            }
        }
        List<CentralProcessor.PhysicalProcessor> physProcs = LogicalProcessorInformation.getPhysProcs(cores, coreEfficiencyMap, corePkgMap, pkgCpuidMap);
        return new Triplet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>>(logProcs, physProcs, AbstractCentralProcessor.orderedProcCaches(caches));
    }

    private static List<CentralProcessor.PhysicalProcessor> getPhysProcs(List<WinNT.GROUP_AFFINITY> cores, Map<WinNT.GROUP_AFFINITY, Integer> coreEfficiencyMap, Map<Integer, Integer> corePkgMap, Map<Integer, String> coreCpuidMap) {
        ArrayList<CentralProcessor.PhysicalProcessor> physProcs = new ArrayList<CentralProcessor.PhysicalProcessor>();
        for (int coreId = 0; coreId < cores.size(); ++coreId) {
            int efficiency = coreEfficiencyMap.getOrDefault(cores.get(coreId), 0);
            String cpuid = coreCpuidMap.getOrDefault(coreId, "");
            int pkgId = corePkgMap.getOrDefault(coreId, 0);
            physProcs.add(new CentralProcessor.PhysicalProcessor(pkgId, coreId, efficiency, cpuid));
        }
        return physProcs;
    }

    private static int getMatchingPackage(List<WinNT.GROUP_AFFINITY[]> packages, int g, int lp) {
        for (int i = 0; i < packages.size(); ++i) {
            for (int j = 0; j < packages.get(i).length; ++j) {
                if ((packages.get((int)i)[j].mask.longValue() & 1L << lp) == 0L || packages.get((int)i)[j].group != g) continue;
                return i;
            }
        }
        return 0;
    }

    private static int getMatchingCore(List<WinNT.GROUP_AFFINITY> cores, int g, int lp) {
        for (int j = 0; j < cores.size(); ++j) {
            if ((cores.get((int)j).mask.longValue() & 1L << lp) == 0L || cores.get((int)j).group != g) continue;
            return j;
        }
        return 0;
    }

    public static Triplet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>> getLogicalProcessorInformation() {
        WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION[] processors;
        ArrayList<Long> packageMaskList = new ArrayList<Long>();
        ArrayList<Long> coreMaskList = new ArrayList<Long>();
        for (WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION proc : processors = Kernel32Util.getLogicalProcessorInformation()) {
            if (proc.relationship == 3) {
                packageMaskList.add(proc.processorMask.longValue());
                continue;
            }
            if (proc.relationship != 0) continue;
            coreMaskList.add(proc.processorMask.longValue());
        }
        coreMaskList.sort(null);
        packageMaskList.sort(null);
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
        for (int core = 0; core < coreMaskList.size(); ++core) {
            long coreMask = (Long)coreMaskList.get(core);
            int lowBit = Long.numberOfTrailingZeros(coreMask);
            int hiBit = 63 - Long.numberOfLeadingZeros(coreMask);
            for (int i = lowBit; i <= hiBit; ++i) {
                if ((coreMask & 1L << i) == 0L) continue;
                CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(i, core, LogicalProcessorInformation.getBitMatchingPackageNumber(packageMaskList, i));
                logProcs.add(logProc);
            }
        }
        return new Triplet(logProcs, null, null);
    }

    private static int getBitMatchingPackageNumber(List<Long> packageMaskList, int logProc) {
        for (int i = 0; i < packageMaskList.size(); ++i) {
            if ((packageMaskList.get(i) & 1L << logProc) == 0L) continue;
            return i;
        }
        return 0;
    }
}

