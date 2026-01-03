/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StreamCorruptedException;
import java.time.zone.Ser;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import jdk.internal.util.StaticProperty;

final class TzdbZoneRulesProvider
extends ZoneRulesProvider {
    private List<String> regionIds;
    private String versionId;
    private final Map<String, Object> regionToRules = new ConcurrentHashMap<String, Object>();

    public TzdbZoneRulesProvider() {
        try {
            String libDir = StaticProperty.javaHome() + File.separator + "lib";
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(libDir, "tzdb.dat"))));){
                this.load(dis);
            }
        }
        catch (Exception ex) {
            throw new ZoneRulesException("Unable to load TZDB time-zone rules", ex);
        }
    }

    @Override
    protected Set<String> provideZoneIds() {
        return new HashSet<String>(this.regionIds);
    }

    @Override
    protected ZoneRules provideRules(String zoneId, boolean forCaching) {
        Object obj = this.regionToRules.get(zoneId);
        if (obj == null) {
            throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
        }
        try {
            if (obj instanceof byte[]) {
                byte[] bytes = (byte[])obj;
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
                obj = Ser.read(dis);
                this.regionToRules.put(zoneId, obj);
            }
            return (ZoneRules)obj;
        }
        catch (Exception ex) {
            throw new ZoneRulesException("Invalid binary time-zone data: TZDB:" + zoneId + ", version: " + this.versionId, ex);
        }
    }

    @Override
    protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
        TreeMap<String, ZoneRules> map = new TreeMap<String, ZoneRules>();
        ZoneRules rules = TzdbZoneRulesProvider.getRules(zoneId, false);
        if (rules != null) {
            map.put(this.versionId, rules);
        }
        return map;
    }

    private void load(DataInputStream dis) throws Exception {
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
            this.versionId = dis.readUTF();
        }
        int regionCount = dis.readShort();
        String[] regionArray = new String[regionCount];
        for (int i3 = 0; i3 < regionCount; ++i3) {
            regionArray[i3] = dis.readUTF();
        }
        this.regionIds = Arrays.asList(regionArray);
        int ruleCount = dis.readShort();
        Object[] ruleArray = new Object[ruleCount];
        for (i = 0; i < ruleCount; ++i) {
            byte[] bytes = new byte[dis.readShort()];
            dis.readFully(bytes);
            ruleArray[i] = bytes;
        }
        for (i = 0; i < versionCount; ++i) {
            int versionRegionCount = dis.readShort();
            this.regionToRules.clear();
            for (int j = 0; j < versionRegionCount; ++j) {
                String region = regionArray[dis.readShort()];
                Object rule = ruleArray[dis.readShort() & 0xFFFF];
                this.regionToRules.put(region, rule);
            }
        }
    }

    public String toString() {
        return "TZDB[" + this.versionId + "]";
    }
}

