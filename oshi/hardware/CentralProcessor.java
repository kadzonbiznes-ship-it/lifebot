/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.hardware;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.Immutable;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.Util;

@ThreadSafe
public interface CentralProcessor {
    public ProcessorIdentifier getProcessorIdentifier();

    public long getMaxFreq();

    public long[] getCurrentFreq();

    public List<LogicalProcessor> getLogicalProcessors();

    public List<PhysicalProcessor> getPhysicalProcessors();

    public List<ProcessorCache> getProcessorCaches();

    public List<String> getFeatureFlags();

    public double getSystemCpuLoadBetweenTicks(long[] var1);

    public long[] getSystemCpuLoadTicks();

    public double[] getSystemLoadAverage(int var1);

    default public double getSystemCpuLoad(long delay) {
        long start = System.nanoTime();
        long[] oldTicks = this.getSystemCpuLoadTicks();
        long toWait = delay - (System.nanoTime() - start) / 1000000L;
        if (toWait > 0L) {
            Util.sleep(delay);
        }
        return this.getSystemCpuLoadBetweenTicks(oldTicks);
    }

    default public double[] getProcessorCpuLoad(long delay) {
        long start = System.nanoTime();
        long[][] oldTicks = this.getProcessorCpuLoadTicks();
        long toWait = delay - (System.nanoTime() - start) / 1000000L;
        if (toWait > 0L) {
            Util.sleep(delay);
        }
        return this.getProcessorCpuLoadBetweenTicks(oldTicks);
    }

    public double[] getProcessorCpuLoadBetweenTicks(long[][] var1);

    public long[][] getProcessorCpuLoadTicks();

    public int getLogicalProcessorCount();

    public int getPhysicalProcessorCount();

    public int getPhysicalPackageCount();

    public long getContextSwitches();

    public long getInterrupts();

    @Immutable
    public static final class ProcessorIdentifier {
        private static final String OSHI_ARCHITECTURE_PROPERTIES = "oshi.architecture.properties";
        private final String cpuVendor;
        private final String cpuName;
        private final String cpuFamily;
        private final String cpuModel;
        private final String cpuStepping;
        private final String processorID;
        private final String cpuIdentifier;
        private final boolean cpu64bit;
        private final long cpuVendorFreq;
        private final Supplier<String> microArchictecture = Memoizer.memoize(this::queryMicroarchitecture);

        public ProcessorIdentifier(String cpuVendor, String cpuName, String cpuFamily, String cpuModel, String cpuStepping, String processorID, boolean cpu64bit) {
            this(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, -1L);
        }

        public ProcessorIdentifier(String cpuVendor, String cpuName, String cpuFamily, String cpuModel, String cpuStepping, String processorID, boolean cpu64bit, long vendorFreq) {
            this.cpuVendor = cpuVendor.startsWith("0x") ? this.queryVendorFromImplementer(cpuVendor) : cpuVendor;
            this.cpuName = cpuName;
            this.cpuFamily = cpuFamily;
            this.cpuModel = cpuModel;
            this.cpuStepping = cpuStepping;
            this.processorID = processorID;
            this.cpu64bit = cpu64bit;
            StringBuilder sb = new StringBuilder();
            if (cpuVendor.contentEquals("GenuineIntel")) {
                sb.append(cpu64bit ? "Intel64" : "x86");
            } else {
                sb.append(cpuVendor);
            }
            sb.append(" Family ").append(cpuFamily);
            sb.append(" Model ").append(cpuModel);
            sb.append(" Stepping ").append(cpuStepping);
            this.cpuIdentifier = sb.toString();
            if (vendorFreq > 0L) {
                this.cpuVendorFreq = vendorFreq;
            } else {
                Pattern pattern = Pattern.compile("@ (.*)$");
                Matcher matcher = pattern.matcher(cpuName);
                if (matcher.find()) {
                    String unit = matcher.group(1);
                    this.cpuVendorFreq = ParseUtil.parseHertz(unit);
                } else {
                    this.cpuVendorFreq = -1L;
                }
            }
        }

        public String getVendor() {
            return this.cpuVendor;
        }

        public String getName() {
            return this.cpuName;
        }

        public String getFamily() {
            return this.cpuFamily;
        }

        public String getModel() {
            return this.cpuModel;
        }

        public String getStepping() {
            return this.cpuStepping;
        }

        public String getProcessorID() {
            return this.processorID;
        }

        public String getIdentifier() {
            return this.cpuIdentifier;
        }

        public boolean isCpu64bit() {
            return this.cpu64bit;
        }

        public long getVendorFreq() {
            return this.cpuVendorFreq;
        }

        public String getMicroarchitecture() {
            return this.microArchictecture.get();
        }

        private String queryMicroarchitecture() {
            String arch = null;
            Properties archProps = FileUtil.readPropertiesFromFilename(OSHI_ARCHITECTURE_PROPERTIES);
            StringBuilder sb = new StringBuilder();
            String ucVendor = this.cpuVendor.toUpperCase(Locale.ROOT);
            if (ucVendor.contains("AMD")) {
                sb.append("amd.");
            } else if (ucVendor.contains("ARM")) {
                sb.append("arm.");
            } else if (ucVendor.contains("IBM")) {
                int powerIdx = this.cpuName.indexOf("_POWER");
                if (powerIdx > 0) {
                    arch = this.cpuName.substring(powerIdx + 1);
                }
            } else if (ucVendor.contains("APPLE")) {
                sb.append("apple.");
            }
            if (Util.isBlank(arch) && !sb.toString().equals("arm.")) {
                sb.append(this.cpuFamily);
                arch = archProps.getProperty(sb.toString());
            }
            if (Util.isBlank(arch)) {
                sb.append('.').append(this.cpuModel);
                arch = archProps.getProperty(sb.toString());
            }
            if (Util.isBlank(arch)) {
                sb.append('.').append(this.cpuStepping);
                arch = archProps.getProperty(sb.toString());
            }
            return Util.isBlank(arch) ? "unknown" : arch;
        }

        private String queryVendorFromImplementer(String cpuVendor) {
            Properties archProps = FileUtil.readPropertiesFromFilename(OSHI_ARCHITECTURE_PROPERTIES);
            return archProps.getProperty("hw_impl." + cpuVendor, cpuVendor);
        }

        public String toString() {
            return this.getIdentifier();
        }
    }

    @Immutable
    public static class ProcessorCache {
        private final byte level;
        private final byte associativity;
        private final short lineSize;
        private final int cacheSize;
        private final Type type;

        public ProcessorCache(byte level, byte associativity, short lineSize, int cacheSize, Type type) {
            this.level = level;
            this.associativity = associativity;
            this.lineSize = lineSize;
            this.cacheSize = cacheSize;
            this.type = type;
        }

        public ProcessorCache(int level, int associativity, int lineSize, long cacheSize, Type type) {
            this((byte)level, (byte)associativity, (short)lineSize, (int)cacheSize, type);
        }

        public byte getLevel() {
            return this.level;
        }

        public byte getAssociativity() {
            return this.associativity;
        }

        public short getLineSize() {
            return this.lineSize;
        }

        public int getCacheSize() {
            return this.cacheSize;
        }

        public Type getType() {
            return this.type;
        }

        public String toString() {
            return "ProcessorCache [L" + this.level + " " + (Object)((Object)this.type) + ", cacheSize=" + this.cacheSize + ", " + (this.associativity > 0 ? this.associativity + "-way" : "unknown") + " associativity, lineSize=" + this.lineSize + "]";
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof ProcessorCache)) {
                return false;
            }
            ProcessorCache other = (ProcessorCache)obj;
            return this.associativity == other.associativity && this.cacheSize == other.cacheSize && this.level == other.level && this.lineSize == other.lineSize && this.type == other.type;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.associativity, this.cacheSize, this.level, this.lineSize, this.type});
        }

        public static enum Type {
            UNIFIED,
            INSTRUCTION,
            DATA,
            TRACE;


            public String toString() {
                return this.name().substring(0, 1) + this.name().substring(1).toLowerCase(Locale.ROOT);
            }
        }
    }

    @Immutable
    public static class PhysicalProcessor {
        private final int physicalPackageNumber;
        private final int physicalProcessorNumber;
        private final int efficiency;
        private final String idString;

        public PhysicalProcessor(int physicalPackageNumber, int physicalProcessorNumber) {
            this(physicalPackageNumber, physicalProcessorNumber, 0, "");
        }

        public PhysicalProcessor(int physicalPackageNumber, int physicalProcessorNumber, int efficiency, String idString) {
            this.physicalPackageNumber = physicalPackageNumber;
            this.physicalProcessorNumber = physicalProcessorNumber;
            this.efficiency = efficiency;
            this.idString = idString;
        }

        public int getPhysicalPackageNumber() {
            return this.physicalPackageNumber;
        }

        public int getPhysicalProcessorNumber() {
            return this.physicalProcessorNumber;
        }

        public int getEfficiency() {
            return this.efficiency;
        }

        public String getIdString() {
            return this.idString;
        }

        public String toString() {
            return "PhysicalProcessor [package/core=" + this.physicalPackageNumber + "/" + this.physicalProcessorNumber + ", efficiency=" + this.efficiency + ", idString=" + this.idString + "]";
        }
    }

    @Immutable
    public static class LogicalProcessor {
        private final int processorNumber;
        private final int physicalProcessorNumber;
        private final int physicalPackageNumber;
        private final int numaNode;
        private final int processorGroup;

        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber) {
            this(processorNumber, physicalProcessorNumber, physicalPackageNumber, 0, 0);
        }

        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber, int numaNode) {
            this(processorNumber, physicalProcessorNumber, physicalPackageNumber, numaNode, 0);
        }

        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber, int numaNode, int processorGroup) {
            this.processorNumber = processorNumber;
            this.physicalProcessorNumber = physicalProcessorNumber;
            this.physicalPackageNumber = physicalPackageNumber;
            this.numaNode = numaNode;
            this.processorGroup = processorGroup;
        }

        public int getProcessorNumber() {
            return this.processorNumber;
        }

        public int getPhysicalProcessorNumber() {
            return this.physicalProcessorNumber;
        }

        public int getPhysicalPackageNumber() {
            return this.physicalPackageNumber;
        }

        public int getNumaNode() {
            return this.numaNode;
        }

        public int getProcessorGroup() {
            return this.processorGroup;
        }

        public String toString() {
            return "LogicalProcessor [processorNumber=" + this.processorNumber + ", coreNumber=" + this.physicalProcessorNumber + ", packageNumber=" + this.physicalPackageNumber + ", numaNode=" + this.numaNode + ", processorGroup=" + this.processorGroup + "]";
        }
    }
}

