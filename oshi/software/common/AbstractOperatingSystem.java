/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.software.os.OSProcess
 *  oshi.software.os.OperatingSystem$OSVersionInfo
 *  oshi.software.os.OperatingSystem$ProcessFiltering
 *  oshi.software.os.OperatingSystem$ProcessSorting
 */
package oshi.software.common;

import com.sun.jna.Platform;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.GlobalConfig;
import oshi.util.Memoizer;
import oshi.util.tuples.Pair;

public abstract class AbstractOperatingSystem
implements OperatingSystem {
    protected static final boolean USE_WHO_COMMAND = GlobalConfig.get("oshi.os.unix.whoCommand", false);
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<Pair<String, OperatingSystem.OSVersionInfo>> familyVersionInfo = Memoizer.memoize(this::queryFamilyVersionInfo);
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryPlatformBitness);

    @Override
    public String getManufacturer() {
        return this.manufacturer.get();
    }

    protected abstract String queryManufacturer();

    @Override
    public String getFamily() {
        return this.familyVersionInfo.get().getA();
    }

    @Override
    public OperatingSystem.OSVersionInfo getVersionInfo() {
        return this.familyVersionInfo.get().getB();
    }

    protected abstract Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo();

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

    private int queryPlatformBitness() {
        if (Platform.is64Bit()) {
            return 64;
        }
        int jvmBitness = System.getProperty("os.arch").contains("64") ? 64 : 32;
        return this.queryBitness(jvmBitness);
    }

    protected abstract int queryBitness(int var1);

    @Override
    public List<OSProcess> getProcesses(Predicate<OSProcess> filter, Comparator<OSProcess> sort, int limit) {
        return this.queryAllProcesses().stream().filter(filter == null ? OperatingSystem.ProcessFiltering.ALL_PROCESSES : filter).sorted(sort == null ? OperatingSystem.ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? (long)limit : Long.MAX_VALUE).collect(Collectors.toList());
    }

    protected abstract List<OSProcess> queryAllProcesses();

    @Override
    public List<OSProcess> getChildProcesses(int parentPid, Predicate<OSProcess> filter, Comparator<OSProcess> sort, int limit) {
        List<OSProcess> childProcs = this.queryChildProcesses(parentPid);
        OSProcess parent = childProcs.stream().filter(p -> p.getProcessID() == parentPid).findAny().orElse(null);
        long parentStartTime = parent == null ? 0L : parent.getStartTime();
        return this.queryChildProcesses(parentPid).stream().filter(filter == null ? OperatingSystem.ProcessFiltering.ALL_PROCESSES : filter).filter(p -> p.getProcessID() != parentPid && p.getStartTime() >= parentStartTime).sorted(sort == null ? OperatingSystem.ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? (long)limit : Long.MAX_VALUE).collect(Collectors.toList());
    }

    protected abstract List<OSProcess> queryChildProcesses(int var1);

    @Override
    public List<OSProcess> getDescendantProcesses(int parentPid, Predicate<OSProcess> filter, Comparator<OSProcess> sort, int limit) {
        List<OSProcess> descendantProcs = this.queryDescendantProcesses(parentPid);
        OSProcess parent = descendantProcs.stream().filter(p -> p.getProcessID() == parentPid).findAny().orElse(null);
        long parentStartTime = parent == null ? 0L : parent.getStartTime();
        return this.queryDescendantProcesses(parentPid).stream().filter(filter == null ? OperatingSystem.ProcessFiltering.ALL_PROCESSES : filter).filter(p -> p.getProcessID() != parentPid && p.getStartTime() >= parentStartTime).sorted(sort == null ? OperatingSystem.ProcessSorting.NO_SORTING : sort).limit(limit > 0 ? (long)limit : Long.MAX_VALUE).collect(Collectors.toList());
    }

    protected abstract List<OSProcess> queryDescendantProcesses(int var1);

    protected static Set<Integer> getChildrenOrDescendants(Collection<OSProcess> allProcs, int parentPid, boolean allDescendants) {
        Map<Integer, Integer> parentPidMap = allProcs.stream().collect(Collectors.toMap(OSProcess::getProcessID, OSProcess::getParentProcessID));
        return AbstractOperatingSystem.getChildrenOrDescendants(parentPidMap, parentPid, allDescendants);
    }

    protected static Set<Integer> getChildrenOrDescendants(Map<Integer, Integer> parentPidMap, int parentPid, boolean allDescendants) {
        HashSet<Integer> descendantPids = new HashSet<Integer>();
        descendantPids.add(parentPid);
        ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
        queue.add(parentPid);
        do {
            for (int pid : AbstractOperatingSystem.getChildren(parentPidMap, (Integer)queue.poll())) {
                if (descendantPids.contains(pid)) continue;
                descendantPids.add(pid);
                queue.add(pid);
            }
        } while (allDescendants && !queue.isEmpty());
        return descendantPids;
    }

    private static Set<Integer> getChildren(Map<Integer, Integer> parentPidMap, int parentPid) {
        return parentPidMap.entrySet().stream().filter(e -> ((Integer)e.getValue()).equals(parentPid) && !((Integer)e.getKey()).equals(parentPid)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getManufacturer()).append(' ').append(this.getFamily()).append(' ').append(this.getVersionInfo());
        return sb.toString();
    }
}

