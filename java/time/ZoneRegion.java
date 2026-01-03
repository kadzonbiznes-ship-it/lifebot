/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

final class ZoneRegion
extends ZoneId
implements Serializable {
    private static final long serialVersionUID = 8386373296231747096L;
    private final String id;
    private final transient ZoneRules rules;

    static ZoneRegion ofId(String zoneId, boolean checkAvailable) {
        ZoneRules rules;
        block2: {
            Objects.requireNonNull(zoneId, "zoneId");
            ZoneRegion.checkName(zoneId);
            rules = null;
            try {
                rules = ZoneRulesProvider.getRules(zoneId, true);
            }
            catch (ZoneRulesException ex) {
                if (!checkAvailable) break block2;
                throw ex;
            }
        }
        return new ZoneRegion(zoneId, rules);
    }

    private static void checkName(String zoneId) {
        int n = zoneId.length();
        if (n < 2) {
            throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
        }
        for (int i = 0; i < n; ++i) {
            char c = zoneId.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '/' && i != 0 || c >= '0' && c <= '9' && i != 0 || c == '~' && i != 0 || c == '.' && i != 0 || c == '_' && i != 0 || c == '+' && i != 0 || c == '-' && i != 0) continue;
            throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
        }
    }

    ZoneRegion(String id, ZoneRules rules) {
        this.id = id;
        this.rules = rules;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ZoneRules getRules() {
        return this.rules != null ? this.rules : ZoneRulesProvider.getRules(this.id, false);
    }

    @Override
    ZoneOffset getOffset(long epochSecond) {
        return this.getRules().getOffset(Instant.ofEpochSecond(epochSecond));
    }

    private Object writeReplace() {
        return new Ser(7, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    @Override
    void write(DataOutput out) throws IOException {
        out.writeByte(7);
        this.writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(this.id);
    }

    static ZoneId readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return ZoneId.of(id, false);
    }
}

