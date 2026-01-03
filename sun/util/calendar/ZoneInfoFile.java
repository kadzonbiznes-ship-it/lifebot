/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import jdk.internal.util.StaticProperty;
import sun.security.action.GetPropertyAction;
import sun.util.calendar.ZoneInfo;

public final class ZoneInfoFile {
    private static String versionId;
    private static final Map<String, ZoneInfo> zones;
    private static Map<String, String> aliases;
    private static byte[][] ruleArray;
    private static String[] regions;
    private static int[] indices;
    private static final boolean USE_OLDMAPPING;
    private static String[][] oldMappings;
    private static final long UTC1900 = -2208988800L;
    private static final long UTC2037 = 2145916799L;
    private static final long LDT2037 = 2114380800L;
    private static final long CURRT;
    static final int SECONDS_PER_DAY = 86400;
    static final int DAYS_PER_CYCLE = 146097;
    static final long DAYS_0000_TO_1970 = 719528L;
    private static final int[] toCalendarDOW;
    private static final int[] toSTZTime;
    private static final long OFFSET_MASK = 15L;
    private static final long DST_MASK = 240L;
    private static final int DST_NSHIFT = 4;
    private static final int TRANSITION_NSHIFT = 12;
    private static final int LASTYEAR = 2037;

    public static String[] getZoneIds() {
        int len = regions.length + oldMappings.length;
        if (!USE_OLDMAPPING) {
            len += 3;
        }
        String[] ids = Arrays.copyOf(regions, len);
        int i = regions.length;
        if (!USE_OLDMAPPING) {
            ids[i++] = "EST";
            ids[i++] = "HST";
            ids[i++] = "MST";
        }
        for (int j = 0; j < oldMappings.length; ++j) {
            ids[i++] = oldMappings[j][0];
        }
        return ids;
    }

    public static String[] getZoneIds(int rawOffset) {
        ArrayList<String> ids = new ArrayList<String>();
        for (String id : ZoneInfoFile.getZoneIds()) {
            ZoneInfo zi = ZoneInfoFile.getZoneInfo(id);
            if (zi.getRawOffset() != rawOffset) continue;
            ids.add(id);
        }
        Object[] list = ids.toArray(new String[ids.size()]);
        Arrays.sort(list);
        return list;
    }

    public static ZoneInfo getZoneInfo(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        ZoneInfo zi = ZoneInfoFile.getZoneInfo0(zoneId);
        if (zi != null) {
            zi = (ZoneInfo)zi.clone();
            zi.setID(zoneId);
        }
        return zi;
    }

    private static ZoneInfo getZoneInfo0(String zoneId) {
        try {
            ZoneInfo zi = zones.get(zoneId);
            if (zi != null) {
                return zi;
            }
            String zid = aliases.getOrDefault(zoneId, zoneId);
            int index = Arrays.binarySearch(regions, zid);
            if (index < 0) {
                return null;
            }
            byte[] bytes = ruleArray[indices[index]];
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
            zi = ZoneInfoFile.getZoneInfo(dis, zid);
            zones.put(zoneId, zi);
            return zi;
        }
        catch (Exception ex) {
            throw new RuntimeException("Invalid binary time-zone data: TZDB:" + zoneId + ", version: " + versionId, ex);
        }
    }

    public static Map<String, String> getAliasMap() {
        return Collections.unmodifiableMap(aliases);
    }

    public static String getVersion() {
        return versionId;
    }

    public static ZoneInfo getCustomTimeZone(String originalId, int gmtOffset) {
        String id = ZoneInfoFile.toCustomID(gmtOffset);
        return new ZoneInfo(id, gmtOffset);
    }

    public static String toCustomID(int gmtOffset) {
        char sign;
        int offset = gmtOffset / 1000;
        if (offset >= 0) {
            sign = '+';
        } else {
            sign = '-';
            offset = -offset;
        }
        int hh = offset / 3600;
        int mm = offset % 3600 / 60;
        int ss = offset % 60;
        char[] buf = new char[]{'G', 'M', 'T', sign, '0', '0', ':', '0', '0'};
        if (hh >= 10) {
            buf[4] = (char)(buf[4] + (char)(hh / 10));
        }
        buf[5] = (char)(buf[5] + (char)(hh % 10));
        if (mm != 0) {
            buf[7] = (char)(buf[7] + (char)(mm / 10));
            buf[8] = (char)(buf[8] + (char)(mm % 10));
        }
        String id = new String(buf);
        if (ss != 0) {
            buf[7] = (char)(48 + ss / 10);
            buf[8] = (char)(48 + ss % 10);
            id = id + new String(buf, 6, 3);
        }
        return id;
    }

    private ZoneInfoFile() {
    }

    private static void loadTZDB() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                try {
                    String libDir = StaticProperty.javaHome() + File.separator + "lib";
                    try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(libDir, "tzdb.dat"))));){
                        ZoneInfoFile.load(dis);
                    }
                }
                catch (Exception x) {
                    throw new Error(x);
                }
                return null;
            }
        });
    }

    private static void addOldMapping() {
        for (String[] alias : oldMappings) {
            aliases.put(alias[0], alias[1]);
        }
        if (USE_OLDMAPPING) {
            aliases.put("EST", "America/New_York");
            aliases.put("MST", "America/Denver");
            aliases.put("HST", "Pacific/Honolulu");
        } else {
            zones.put("EST", new ZoneInfo("EST", -18000000));
            zones.put("MST", new ZoneInfo("MST", -25200000));
            zones.put("HST", new ZoneInfo("HST", -36000000));
        }
    }

    public static boolean useOldMapping() {
        return USE_OLDMAPPING;
    }

    private static void load(DataInputStream dis) throws IOException {
        int i;
        if (dis.readByte() != 1) {
            throw new StreamCorruptedException("File format not recognised");
        }
        String groupId = dis.readUTF();
        if (!"TZDB".equals(groupId)) {
            throw new StreamCorruptedException("File format not recognised");
        }
        int versionCount = dis.readShort();
        for (int i2 = 0; i2 < versionCount; ++i2) {
            versionId = dis.readUTF();
        }
        int regionCount = dis.readShort();
        String[] regionArray = new String[regionCount];
        for (int i3 = 0; i3 < regionCount; ++i3) {
            regionArray[i3] = dis.readUTF();
        }
        int ruleCount = dis.readShort();
        ruleArray = new byte[ruleCount][];
        for (i = 0; i < ruleCount; ++i) {
            byte[] bytes = new byte[dis.readShort()];
            dis.readFully(bytes);
            ZoneInfoFile.ruleArray[i] = bytes;
        }
        for (i = 0; i < versionCount; ++i) {
            regionCount = dis.readShort();
            regions = new String[regionCount];
            indices = new int[regionCount];
            for (int j = 0; j < regionCount; ++j) {
                ZoneInfoFile.regions[j] = regionArray[dis.readShort()];
                ZoneInfoFile.indices[j] = dis.readShort();
            }
        }
        zones.remove("ROC");
        for (i = 0; i < versionCount; ++i) {
            int aliasCount = dis.readShort();
            aliases.clear();
            for (int j = 0; j < aliasCount; ++j) {
                String alias = regionArray[dis.readShort()];
                String region = regionArray[dis.readShort()];
                aliases.put(alias, region);
            }
        }
        ZoneInfoFile.addOldMapping();
    }

    public static ZoneInfo getZoneInfo(DataInput in, String zoneId) throws Exception {
        byte type = in.readByte();
        int stdSize = in.readInt();
        long[] stdTrans = new long[stdSize];
        for (int i = 0; i < stdSize; ++i) {
            stdTrans[i] = ZoneInfoFile.readEpochSec(in);
        }
        int[] stdOffsets = new int[stdSize + 1];
        for (int i = 0; i < stdOffsets.length; ++i) {
            stdOffsets[i] = ZoneInfoFile.readOffset(in);
        }
        int savSize = in.readInt();
        long[] savTrans = new long[savSize];
        for (int i = 0; i < savSize; ++i) {
            savTrans[i] = ZoneInfoFile.readEpochSec(in);
        }
        int[] savOffsets = new int[savSize + 1];
        for (int i = 0; i < savOffsets.length; ++i) {
            savOffsets[i] = ZoneInfoFile.readOffset(in);
        }
        int ruleSize = in.readByte();
        ZoneOffsetTransitionRule[] rules = new ZoneOffsetTransitionRule[ruleSize];
        for (int i = 0; i < ruleSize; ++i) {
            rules[i] = new ZoneOffsetTransitionRule(in);
        }
        return ZoneInfoFile.getZoneInfo(zoneId, stdTrans, stdOffsets, savTrans, savOffsets, rules);
    }

    public static int readOffset(DataInput in) throws IOException {
        byte offsetByte = in.readByte();
        return offsetByte == 127 ? in.readInt() : offsetByte * 900;
    }

    static long readEpochSec(DataInput in) throws IOException {
        int hiByte = in.readByte() & 0xFF;
        if (hiByte == 255) {
            return in.readLong();
        }
        int midByte = in.readByte() & 0xFF;
        int loByte = in.readByte() & 0xFF;
        long tot = (hiByte << 16) + (midByte << 8) + loByte;
        return tot * 900L - 4575744000L;
    }

    private static ZoneInfo getZoneInfo(String zoneId, long[] standardTransitions, int[] standardOffsets, long[] savingsInstantTransitions, int[] wallOffsets, ZoneOffsetTransitionRule[] lastRules) {
        int rawOffset = 0;
        int dstSavings = 0;
        int checksum = 0;
        int[] params = null;
        boolean willGMTOffsetChange = false;
        if (standardTransitions.length > 0) {
            rawOffset = standardOffsets[standardOffsets.length - 1] * 1000;
            willGMTOffsetChange = standardTransitions[standardTransitions.length - 1] > CURRT;
        } else {
            rawOffset = standardOffsets[0] * 1000;
        }
        Object transitions = null;
        Object offsets = null;
        int nOffsets = 0;
        int nTrans = 0;
        if (savingsInstantTransitions.length != 0) {
            long trans;
            int i;
            transitions = new long[250];
            offsets = new int[100];
            int lastyear = ZoneInfoFile.getYear(savingsInstantTransitions[savingsInstantTransitions.length - 1], wallOffsets[savingsInstantTransitions.length - 1]);
            int k = 1;
            for (i = 0; i < savingsInstantTransitions.length && savingsInstantTransitions[i] < -2208988800L; ++i) {
            }
            if (i < savingsInstantTransitions.length) {
                if (i < savingsInstantTransitions.length) {
                    offsets[0] = standardOffsets[standardOffsets.length - 1] * 1000;
                    nOffsets = 1;
                }
                nOffsets = ZoneInfoFile.addTrans(transitions, nTrans++, offsets, nOffsets, -2208988800L, wallOffsets[i], ZoneInfoFile.getStandardOffset(standardTransitions, standardOffsets, -2208988800L));
            }
            while (i < savingsInstantTransitions.length) {
                trans = savingsInstantTransitions[i];
                if (trans > 2145916799L) {
                    lastyear = 2037;
                    break;
                }
                while (k < standardTransitions.length) {
                    long trans_s = standardTransitions[k];
                    if (trans_s >= -2208988800L) {
                        if (trans_s > trans) break;
                        if (trans_s < trans) {
                            if (nOffsets + 2 >= ((int[])offsets).length) {
                                offsets = Arrays.copyOf(offsets, ((int[])offsets).length + 100);
                            }
                            if (nTrans + 1 >= ((long[])transitions).length) {
                                transitions = Arrays.copyOf(transitions, ((long[])transitions).length + 100);
                            }
                            nOffsets = ZoneInfoFile.addTrans(transitions, nTrans++, offsets, nOffsets, trans_s, wallOffsets[i], standardOffsets[k + 1]);
                        }
                    }
                    ++k;
                }
                if (nOffsets + 2 >= ((int[])offsets).length) {
                    offsets = Arrays.copyOf(offsets, ((int[])offsets).length + 100);
                }
                if (nTrans + 1 >= ((long[])transitions).length) {
                    transitions = Arrays.copyOf(transitions, ((long[])transitions).length + 100);
                }
                nOffsets = ZoneInfoFile.addTrans(transitions, nTrans++, offsets, nOffsets, trans, wallOffsets[i + 1], ZoneInfoFile.getStandardOffset(standardTransitions, standardOffsets, trans));
                ++i;
            }
            while (k < standardTransitions.length) {
                trans = standardTransitions[k];
                if (trans >= -2208988800L) {
                    int offset = wallOffsets[i];
                    int offsetIndex = ZoneInfoFile.indexOf(offsets, 0, nOffsets, offset);
                    if (offsetIndex == nOffsets) {
                        ++nOffsets;
                    }
                    transitions[nTrans++] = trans * 1000L << 12 | (long)offsetIndex & 0xFL;
                }
                ++k;
            }
            if (lastRules.length > 1) {
                while (lastyear++ < 2037) {
                    for (ZoneOffsetTransitionRule zotr : lastRules) {
                        long trans2 = zotr.getTransitionEpochSecond(lastyear);
                        if (nOffsets + 2 >= ((int[])offsets).length) {
                            offsets = Arrays.copyOf(offsets, ((int[])offsets).length + 100);
                        }
                        if (nTrans + 1 >= ((long[])transitions).length) {
                            transitions = Arrays.copyOf(transitions, ((long[])transitions).length + 100);
                        }
                        nOffsets = ZoneInfoFile.addTrans(transitions, nTrans++, offsets, nOffsets, trans2, zotr.offsetAfter, zotr.standardOffset);
                    }
                }
                ZoneOffsetTransitionRule startRule = lastRules[lastRules.length - 2];
                ZoneOffsetTransitionRule endRule = lastRules[lastRules.length - 1];
                params = new int[10];
                if (startRule.offsetAfter - startRule.offsetBefore < 0 && endRule.offsetAfter - endRule.offsetBefore > 0) {
                    ZoneOffsetTransitionRule tmp = startRule;
                    startRule = endRule;
                    endRule = tmp;
                }
                params[0] = startRule.month - 1;
                byte dom = startRule.dom;
                int dow = startRule.dow;
                if (dow == -1) {
                    params[1] = dom;
                    params[2] = 0;
                } else if (dom < 0 || dom >= 24) {
                    params[1] = -1;
                    params[2] = toCalendarDOW[dow];
                } else {
                    params[1] = dom;
                    params[2] = -toCalendarDOW[dow];
                }
                params[3] = startRule.secondOfDay * 1000;
                params[4] = toSTZTime[startRule.timeDefinition];
                params[5] = endRule.month - 1;
                dom = endRule.dom;
                dow = endRule.dow;
                if (dow == -1) {
                    params[6] = dom;
                    params[7] = 0;
                } else if (dom < 0 || dom >= 24) {
                    params[6] = -1;
                    params[7] = toCalendarDOW[dow];
                } else {
                    params[6] = dom;
                    params[7] = -toCalendarDOW[dow];
                }
                params[8] = endRule.secondOfDay * 1000;
                params[9] = toSTZTime[endRule.timeDefinition];
                dstSavings = (startRule.offsetAfter - startRule.offsetBefore) * 1000;
                if (zoneId.equals("Africa/Cairo") && params[7] == 6 && params[8] == 0) {
                    params[7] = 5;
                    params[8] = 86400000;
                }
            } else if (nTrans > 0) {
                if (lastyear < 2037) {
                    trans = 2114380800L - (long)(rawOffset / 1000);
                    int offsetIndex = ZoneInfoFile.indexOf(offsets, 0, nOffsets, rawOffset / 1000);
                    if (offsetIndex == nOffsets) {
                        ++nOffsets;
                    }
                    transitions[nTrans++] = trans * 1000L << 12 | (long)offsetIndex & 0xFL;
                } else if (savingsInstantTransitions.length > 2) {
                    int m = savingsInstantTransitions.length;
                    long startTrans = savingsInstantTransitions[m - 2];
                    int startOffset = wallOffsets[m - 2 + 1];
                    int startStd = ZoneInfoFile.getStandardOffset(standardTransitions, standardOffsets, startTrans);
                    long endTrans = savingsInstantTransitions[m - 1];
                    int endOffset = wallOffsets[m - 1 + 1];
                    int endStd = ZoneInfoFile.getStandardOffset(standardTransitions, standardOffsets, endTrans);
                    if (startOffset > startStd && endOffset == endStd) {
                        m = savingsInstantTransitions.length - 2;
                        ZoneOffset before = ZoneOffset.ofTotalSeconds(wallOffsets[m]);
                        ZoneOffset after = ZoneOffset.ofTotalSeconds(wallOffsets[m + 1]);
                        LocalDateTime ldt = LocalDateTime.ofEpochSecond(savingsInstantTransitions[m], 0, before);
                        LocalDateTime startLDT = after.getTotalSeconds() > before.getTotalSeconds() ? ldt : ldt.plusSeconds(wallOffsets[m + 1] - wallOffsets[m]);
                        m = savingsInstantTransitions.length - 1;
                        before = ZoneOffset.ofTotalSeconds(wallOffsets[m]);
                        after = ZoneOffset.ofTotalSeconds(wallOffsets[m + 1]);
                        ldt = LocalDateTime.ofEpochSecond(savingsInstantTransitions[m], 0, before);
                        LocalDateTime endLDT = after.getTotalSeconds() > before.getTotalSeconds() ? ldt.plusSeconds(wallOffsets[m + 1] - wallOffsets[m]) : ldt;
                        params = new int[]{startLDT.getMonthValue() - 1, startLDT.getDayOfMonth(), 0, startLDT.toLocalTime().toSecondOfDay() * 1000, 0, endLDT.getMonthValue() - 1, endLDT.getDayOfMonth(), 0, endLDT.toLocalTime().toSecondOfDay() * 1000, 0};
                        dstSavings = (startOffset - startStd) * 1000;
                    }
                }
            }
            if (transitions != null && ((long[])transitions).length != nTrans) {
                transitions = nTrans == 0 ? null : Arrays.copyOf(transitions, nTrans);
            }
            if (offsets != null && ((int[])offsets).length != nOffsets) {
                offsets = nOffsets == 0 ? null : Arrays.copyOf(offsets, nOffsets);
            }
            if (transitions != null) {
                Checksum sum = new Checksum();
                for (i = 0; i < ((long[])transitions).length; ++i) {
                    long val = transitions[i];
                    int dst = (int)(val >>> 4 & 0xFL);
                    int index = (int)(val & 0xFL);
                    long second = val >> 12;
                    sum.update(second + (long)index);
                    sum.update(index);
                    sum.update(dst == 0 ? -1 : dst);
                }
                checksum = (int)sum.getValue();
            }
        }
        return new ZoneInfo(zoneId, rawOffset, dstSavings, checksum, (long[])transitions, (int[])offsets, params, willGMTOffsetChange);
    }

    private static int getStandardOffset(long[] standardTransitions, int[] standardOffsets, long epochSec) {
        int index;
        for (index = 0; index < standardTransitions.length && epochSec >= standardTransitions[index]; ++index) {
        }
        return standardOffsets[index];
    }

    private static int getYear(long epochSecond, int offset) {
        long yearEst;
        long doyEst;
        long second = epochSecond + (long)offset;
        long epochDay = Math.floorDiv(second, 86400);
        long zeroDay = epochDay + 719528L;
        long adjust = 0L;
        if ((zeroDay -= 60L) < 0L) {
            long adjustCycles = (zeroDay + 1L) / 146097L - 1L;
            adjust = adjustCycles * 400L;
            zeroDay += -adjustCycles * 146097L;
        }
        if ((doyEst = zeroDay - (365L * (yearEst = (400L * zeroDay + 591L) / 146097L) + yearEst / 4L - yearEst / 100L + yearEst / 400L)) < 0L) {
            doyEst = zeroDay - (365L * --yearEst + yearEst / 4L - yearEst / 100L + yearEst / 400L);
        }
        yearEst += adjust;
        int marchDoy0 = (int)doyEst;
        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        return (int)(yearEst += (long)(marchMonth0 / 10));
    }

    private static int indexOf(int[] offsets, int from, int nOffsets, int offset) {
        offset *= 1000;
        while (from < nOffsets) {
            if (offsets[from] == offset) {
                return from;
            }
            ++from;
        }
        offsets[from] = offset;
        return from;
    }

    private static int addTrans(long[] transitions, int nTrans, int[] offsets, int nOffsets, long trans, int offset, int stdOffset) {
        int offsetIndex = ZoneInfoFile.indexOf(offsets, 0, nOffsets, offset);
        if (offsetIndex == nOffsets) {
            ++nOffsets;
        }
        int dstIndex = 0;
        if (offset != stdOffset && (dstIndex = ZoneInfoFile.indexOf(offsets, 1, nOffsets, offset - stdOffset)) == nOffsets) {
            ++nOffsets;
        }
        transitions[nTrans] = trans * 1000L << 12 | (long)(dstIndex << 4) & 0xF0L | (long)offsetIndex & 0xFL;
        return nOffsets;
    }

    static {
        zones = new ConcurrentHashMap<String, ZoneInfo>();
        aliases = new HashMap<String, String>();
        oldMappings = new String[][]{{"ACT", "Australia/Darwin"}, {"AET", "Australia/Sydney"}, {"AGT", "America/Argentina/Buenos_Aires"}, {"ART", "Africa/Cairo"}, {"AST", "America/Anchorage"}, {"BET", "America/Sao_Paulo"}, {"BST", "Asia/Dhaka"}, {"CAT", "Africa/Harare"}, {"CNT", "America/St_Johns"}, {"CST", "America/Chicago"}, {"CTT", "Asia/Shanghai"}, {"EAT", "Africa/Addis_Ababa"}, {"ECT", "Europe/Paris"}, {"IET", "America/Indiana/Indianapolis"}, {"IST", "Asia/Kolkata"}, {"JST", "Asia/Tokyo"}, {"MIT", "Pacific/Apia"}, {"NET", "Asia/Yerevan"}, {"NST", "Pacific/Auckland"}, {"PLT", "Asia/Karachi"}, {"PNT", "America/Phoenix"}, {"PRT", "America/Puerto_Rico"}, {"PST", "America/Los_Angeles"}, {"SST", "Pacific/Guadalcanal"}, {"VST", "Asia/Ho_Chi_Minh"}};
        String oldmapping = GetPropertyAction.privilegedGetProperty("sun.timezone.ids.oldmapping", "false").toLowerCase(Locale.ROOT);
        USE_OLDMAPPING = oldmapping.equals("yes") || oldmapping.equals("true");
        ZoneInfoFile.loadTZDB();
        CURRT = System.currentTimeMillis() / 1000L;
        toCalendarDOW = new int[]{-1, 2, 3, 4, 5, 6, 7, 1};
        toSTZTime = new int[]{2, 0, 1};
    }

    private static class ZoneOffsetTransitionRule {
        private final int month;
        private final byte dom;
        private final int dow;
        private final int secondOfDay;
        private final int timeDefinition;
        private final int standardOffset;
        private final int offsetBefore;
        private final int offsetAfter;

        ZoneOffsetTransitionRule(DataInput in) throws IOException {
            int data = in.readInt();
            int dowByte = (data & 0x380000) >>> 19;
            int timeByte = (data & 0x7C000) >>> 14;
            int stdByte = (data & 0xFF0) >>> 4;
            int beforeByte = (data & 0xC) >>> 2;
            int afterByte = data & 3;
            this.month = data >>> 28;
            this.dom = (byte)(((data & 0xFC00000) >>> 22) - 32);
            this.dow = dowByte == 0 ? -1 : dowByte;
            this.secondOfDay = timeByte == 31 ? in.readInt() : timeByte * 3600;
            this.timeDefinition = (data & 0x3000) >>> 12;
            this.standardOffset = stdByte == 255 ? in.readInt() : (stdByte - 128) * 900;
            this.offsetBefore = beforeByte == 3 ? in.readInt() : this.standardOffset + beforeByte * 1800;
            this.offsetAfter = afterByte == 3 ? in.readInt() : this.standardOffset + afterByte * 1800;
        }

        long getTransitionEpochSecond(int year) {
            long epochDay = 0L;
            if (this.dom < 0) {
                epochDay = ZoneOffsetTransitionRule.toEpochDay(year, this.month, ZoneOffsetTransitionRule.lengthOfMonth(year, this.month) + 1 + this.dom);
                if (this.dow != -1) {
                    epochDay = ZoneOffsetTransitionRule.previousOrSame(epochDay, this.dow);
                }
            } else {
                epochDay = ZoneOffsetTransitionRule.toEpochDay(year, this.month, this.dom);
                if (this.dow != -1) {
                    epochDay = ZoneOffsetTransitionRule.nextOrSame(epochDay, this.dow);
                }
            }
            int difference = 0;
            switch (this.timeDefinition) {
                case 0: {
                    difference = 0;
                    break;
                }
                case 1: {
                    difference = -this.offsetBefore;
                    break;
                }
                case 2: {
                    difference = -this.standardOffset;
                }
            }
            return epochDay * 86400L + (long)this.secondOfDay + (long)difference;
        }

        static final boolean isLeapYear(int year) {
            return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
        }

        static final int lengthOfMonth(int year, int month) {
            switch (month) {
                case 2: {
                    return ZoneOffsetTransitionRule.isLeapYear(year) ? 29 : 28;
                }
                case 4: 
                case 6: 
                case 9: 
                case 11: {
                    return 30;
                }
            }
            return 31;
        }

        static final long toEpochDay(int year, int month, int day) {
            long y = year;
            long m = month;
            long total = 0L;
            total += 365L * y;
            total = y >= 0L ? (total += (y + 3L) / 4L - (y + 99L) / 100L + (y + 399L) / 400L) : (total -= y / -4L - y / -100L + y / -400L);
            total += (367L * m - 362L) / 12L;
            total += (long)(day - 1);
            if (m > 2L) {
                --total;
                if (!ZoneOffsetTransitionRule.isLeapYear(year)) {
                    --total;
                }
            }
            return total - 719528L;
        }

        static final long previousOrSame(long epochDay, int dayOfWeek) {
            return ZoneOffsetTransitionRule.adjust(epochDay, dayOfWeek, 1);
        }

        static final long nextOrSame(long epochDay, int dayOfWeek) {
            return ZoneOffsetTransitionRule.adjust(epochDay, dayOfWeek, 0);
        }

        static final long adjust(long epochDay, int dow, int relative) {
            int calDow = (int)Math.floorMod(epochDay + 3L, 7L) + 1;
            if (relative < 2 && calDow == dow) {
                return epochDay;
            }
            if ((relative & 1) == 0) {
                int daysDiff = calDow - dow;
                return epochDay + (long)(daysDiff >= 0 ? 7 - daysDiff : -daysDiff);
            }
            int daysDiff = dow - calDow;
            return epochDay - (long)(daysDiff >= 0 ? 7 - daysDiff : -daysDiff);
        }
    }

    private static class Checksum
    extends CRC32 {
        private Checksum() {
        }

        @Override
        public void update(int val) {
            byte[] b = new byte[]{(byte)(val >>> 24), (byte)(val >>> 16), (byte)(val >>> 8), (byte)val};
            this.update(b);
        }

        void update(long val) {
            byte[] b = new byte[]{(byte)(val >>> 56), (byte)(val >>> 48), (byte)(val >>> 40), (byte)(val >>> 32), (byte)(val >>> 24), (byte)(val >>> 16), (byte)(val >>> 8), (byte)val};
            this.update(b);
        }
    }
}

