/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.Ser;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ZoneRules
implements Serializable {
    private static final long serialVersionUID = 3044319355680032515L;
    private static final int LAST_CACHED_YEAR = 2100;
    private final long[] standardTransitions;
    private final ZoneOffset[] standardOffsets;
    private final long[] savingsInstantTransitions;
    private final LocalDateTime[] savingsLocalTransitions;
    private final ZoneOffset[] wallOffsets;
    private final ZoneOffsetTransitionRule[] lastRules;
    private final transient ConcurrentMap<Integer, ZoneOffsetTransition[]> lastRulesCache;
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final ZoneOffsetTransitionRule[] EMPTY_LASTRULES = new ZoneOffsetTransitionRule[0];
    private static final LocalDateTime[] EMPTY_LDT_ARRAY = new LocalDateTime[0];
    private static final int DAYS_PER_CYCLE = 146097;
    private static final long DAYS_0000_TO_1970 = 719528L;

    public static ZoneRules of(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset, List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList, List<ZoneOffsetTransitionRule> lastRules) {
        Objects.requireNonNull(baseStandardOffset, "baseStandardOffset");
        Objects.requireNonNull(baseWallOffset, "baseWallOffset");
        Objects.requireNonNull(standardOffsetTransitionList, "standardOffsetTransitionList");
        Objects.requireNonNull(transitionList, "transitionList");
        Objects.requireNonNull(lastRules, "lastRules");
        return new ZoneRules(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules);
    }

    public static ZoneRules of(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        return new ZoneRules(offset);
    }

    ZoneRules(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset, List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList, List<ZoneOffsetTransitionRule> lastRules) {
        this.standardTransitions = new long[standardOffsetTransitionList.size()];
        this.standardOffsets = new ZoneOffset[standardOffsetTransitionList.size() + 1];
        this.standardOffsets[0] = baseStandardOffset;
        for (int i = 0; i < standardOffsetTransitionList.size(); ++i) {
            this.standardTransitions[i] = standardOffsetTransitionList.get(i).toEpochSecond();
            this.standardOffsets[i + 1] = standardOffsetTransitionList.get(i).getOffsetAfter();
        }
        ArrayList<LocalDateTime> localTransitionList = new ArrayList<LocalDateTime>();
        ArrayList<ZoneOffset> localTransitionOffsetList = new ArrayList<ZoneOffset>();
        localTransitionOffsetList.add(baseWallOffset);
        for (ZoneOffsetTransition trans : transitionList) {
            if (trans.isGap()) {
                localTransitionList.add(trans.getDateTimeBefore());
                localTransitionList.add(trans.getDateTimeAfter());
            } else {
                localTransitionList.add(trans.getDateTimeAfter());
                localTransitionList.add(trans.getDateTimeBefore());
            }
            localTransitionOffsetList.add(trans.getOffsetAfter());
        }
        this.savingsLocalTransitions = localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
        this.wallOffsets = localTransitionOffsetList.toArray(new ZoneOffset[localTransitionOffsetList.size()]);
        this.savingsInstantTransitions = new long[transitionList.size()];
        for (int i = 0; i < transitionList.size(); ++i) {
            this.savingsInstantTransitions[i] = transitionList.get(i).toEpochSecond();
        }
        if (lastRules.size() > 0) {
            Object[] temp = lastRules.toArray();
            ZoneOffsetTransitionRule[] rulesArray = (ZoneOffsetTransitionRule[])Arrays.copyOf(temp, temp.length, ZoneOffsetTransitionRule[].class);
            if (rulesArray.length > 16) {
                throw new IllegalArgumentException("Too many transition rules");
            }
            this.lastRules = rulesArray;
            this.lastRulesCache = new ConcurrentHashMap<Integer, ZoneOffsetTransition[]>();
        } else {
            this.lastRules = EMPTY_LASTRULES;
            this.lastRulesCache = null;
        }
    }

    private ZoneRules(long[] standardTransitions, ZoneOffset[] standardOffsets, long[] savingsInstantTransitions, ZoneOffset[] wallOffsets, ZoneOffsetTransitionRule[] lastRules) {
        this.standardTransitions = standardTransitions;
        this.standardOffsets = standardOffsets;
        this.savingsInstantTransitions = savingsInstantTransitions;
        this.wallOffsets = wallOffsets;
        this.lastRules = lastRules;
        ConcurrentHashMap concurrentHashMap = this.lastRulesCache = lastRules.length > 0 ? new ConcurrentHashMap() : null;
        if (savingsInstantTransitions.length == 0) {
            this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
        } else {
            ArrayList<LocalDateTime> localTransitionList = new ArrayList<LocalDateTime>();
            for (int i = 0; i < savingsInstantTransitions.length; ++i) {
                ZoneOffset before = wallOffsets[i];
                ZoneOffset after = wallOffsets[i + 1];
                ZoneOffsetTransition trans = new ZoneOffsetTransition(savingsInstantTransitions[i], before, after);
                if (trans.isGap()) {
                    localTransitionList.add(trans.getDateTimeBefore());
                    localTransitionList.add(trans.getDateTimeAfter());
                    continue;
                }
                localTransitionList.add(trans.getDateTimeAfter());
                localTransitionList.add(trans.getDateTimeBefore());
            }
            this.savingsLocalTransitions = localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
        }
    }

    private ZoneRules(ZoneOffset offset) {
        this.standardOffsets = new ZoneOffset[1];
        this.standardOffsets[0] = offset;
        this.standardTransitions = EMPTY_LONG_ARRAY;
        this.savingsInstantTransitions = EMPTY_LONG_ARRAY;
        this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
        this.wallOffsets = this.standardOffsets;
        this.lastRules = EMPTY_LASTRULES;
        this.lastRulesCache = null;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser(1, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.standardTransitions.length);
        for (long trans : this.standardTransitions) {
            Ser.writeEpochSec(trans, out);
        }
        for (ZoneOffset offset : this.standardOffsets) {
            Ser.writeOffset(offset, out);
        }
        out.writeInt(this.savingsInstantTransitions.length);
        for (long trans : this.savingsInstantTransitions) {
            Ser.writeEpochSec(trans, out);
        }
        for (ZoneOffset offset : this.wallOffsets) {
            Ser.writeOffset(offset, out);
        }
        out.writeByte(this.lastRules.length);
        for (ZoneOffsetTransitionRule rule : this.lastRules) {
            rule.writeExternal(out);
        }
    }

    static ZoneRules readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int stdSize = in.readInt();
        if (stdSize > 1024) {
            throw new InvalidObjectException("Too many transitions");
        }
        long[] stdTrans = stdSize == 0 ? EMPTY_LONG_ARRAY : new long[stdSize];
        for (int i = 0; i < stdSize; ++i) {
            stdTrans[i] = Ser.readEpochSec(in);
        }
        ZoneOffset[] stdOffsets = new ZoneOffset[stdSize + 1];
        for (int i = 0; i < stdOffsets.length; ++i) {
            stdOffsets[i] = Ser.readOffset(in);
        }
        int savSize = in.readInt();
        if (savSize > 1024) {
            throw new InvalidObjectException("Too many saving offsets");
        }
        long[] savTrans = savSize == 0 ? EMPTY_LONG_ARRAY : new long[savSize];
        for (int i = 0; i < savSize; ++i) {
            savTrans[i] = Ser.readEpochSec(in);
        }
        ZoneOffset[] savOffsets = new ZoneOffset[savSize + 1];
        for (int i = 0; i < savOffsets.length; ++i) {
            savOffsets[i] = Ser.readOffset(in);
        }
        int ruleSize = in.readByte();
        if (ruleSize > 16) {
            throw new InvalidObjectException("Too many transition rules");
        }
        ZoneOffsetTransitionRule[] rules = ruleSize == 0 ? EMPTY_LASTRULES : new ZoneOffsetTransitionRule[ruleSize];
        for (int i = 0; i < ruleSize; ++i) {
            rules[i] = ZoneOffsetTransitionRule.readExternal(in);
        }
        return new ZoneRules(stdTrans, stdOffsets, savTrans, savOffsets, rules);
    }

    public boolean isFixedOffset() {
        return this.standardOffsets[0].equals(this.wallOffsets[0]) && this.standardTransitions.length == 0 && this.savingsInstantTransitions.length == 0 && this.lastRules.length == 0;
    }

    public ZoneOffset getOffset(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return this.wallOffsets[0];
        }
        long epochSec = instant.getEpochSecond();
        if (this.lastRules.length > 0 && epochSec > this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1]) {
            int year = this.findYear(epochSec, this.wallOffsets[this.wallOffsets.length - 1]);
            ZoneOffsetTransition[] transArray = this.findTransitionArray(year);
            ZoneOffsetTransition trans = null;
            for (int i = 0; i < transArray.length; ++i) {
                trans = transArray[i];
                if (epochSec >= trans.toEpochSecond()) continue;
                return trans.getOffsetBefore();
            }
            return trans.getOffsetAfter();
        }
        int index = Arrays.binarySearch(this.savingsInstantTransitions, epochSec);
        if (index < 0) {
            index = -index - 2;
        }
        return this.wallOffsets[index + 1];
    }

    public ZoneOffset getOffset(LocalDateTime localDateTime) {
        Object info = this.getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition)info).getOffsetBefore();
        }
        return (ZoneOffset)info;
    }

    public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
        Object info = this.getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition)info).getValidOffsets();
        }
        return Collections.singletonList((ZoneOffset)info);
    }

    public ZoneOffsetTransition getTransition(LocalDateTime localDateTime) {
        Object info = this.getOffsetInfo(localDateTime);
        return info instanceof ZoneOffsetTransition ? (ZoneOffsetTransition)info : null;
    }

    private Object getOffsetInfo(LocalDateTime dt) {
        if (this.savingsLocalTransitions.length == 0) {
            return this.wallOffsets[0];
        }
        if (this.lastRules.length > 0 && dt.isAfter(this.savingsLocalTransitions[this.savingsLocalTransitions.length - 1])) {
            ZoneOffsetTransition[] transArray = this.findTransitionArray(dt.getYear());
            Object info = null;
            for (ZoneOffsetTransition trans : transArray) {
                info = this.findOffsetInfo(dt, trans);
                if (!(info instanceof ZoneOffsetTransition) && !info.equals(trans.getOffsetBefore())) continue;
                return info;
            }
            return info;
        }
        int index = Arrays.binarySearch(this.savingsLocalTransitions, dt);
        if (index == -1) {
            return this.wallOffsets[0];
        }
        if (index < 0) {
            index = -index - 2;
        } else if (index < this.savingsLocalTransitions.length - 1 && this.savingsLocalTransitions[index].equals(this.savingsLocalTransitions[index + 1])) {
            ++index;
        }
        if ((index & 1) == 0) {
            LocalDateTime dtBefore = this.savingsLocalTransitions[index];
            LocalDateTime dtAfter = this.savingsLocalTransitions[index + 1];
            ZoneOffset offsetBefore = this.wallOffsets[index / 2];
            ZoneOffset offsetAfter = this.wallOffsets[index / 2 + 1];
            if (offsetAfter.getTotalSeconds() > offsetBefore.getTotalSeconds()) {
                return new ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter);
            }
            return new ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter);
        }
        return this.wallOffsets[index / 2 + 1];
    }

    private Object findOffsetInfo(LocalDateTime dt, ZoneOffsetTransition trans) {
        LocalDateTime localTransition = trans.getDateTimeBefore();
        if (trans.isGap()) {
            if (dt.isBefore(localTransition)) {
                return trans.getOffsetBefore();
            }
            if (dt.isBefore(trans.getDateTimeAfter())) {
                return trans;
            }
            return trans.getOffsetAfter();
        }
        if (!dt.isBefore(localTransition)) {
            return trans.getOffsetAfter();
        }
        if (dt.isBefore(trans.getDateTimeAfter())) {
            return trans.getOffsetBefore();
        }
        return trans;
    }

    private ZoneOffsetTransition[] findTransitionArray(int year) {
        Integer yearObj = year;
        ZoneOffsetTransition[] transArray = (ZoneOffsetTransition[])this.lastRulesCache.get(yearObj);
        if (transArray != null) {
            return transArray;
        }
        ZoneOffsetTransitionRule[] ruleArray = this.lastRules;
        transArray = new ZoneOffsetTransition[ruleArray.length];
        for (int i = 0; i < ruleArray.length; ++i) {
            transArray[i] = ruleArray[i].createTransition(year);
        }
        if (year < 2100) {
            this.lastRulesCache.putIfAbsent(yearObj, transArray);
        }
        return transArray;
    }

    public ZoneOffset getStandardOffset(Instant instant) {
        if (this.standardTransitions.length == 0) {
            return this.standardOffsets[0];
        }
        long epochSec = instant.getEpochSecond();
        int index = Arrays.binarySearch(this.standardTransitions, epochSec);
        if (index < 0) {
            index = -index - 2;
        }
        return this.standardOffsets[index + 1];
    }

    public Duration getDaylightSavings(Instant instant) {
        if (this.isFixedOffset()) {
            return Duration.ZERO;
        }
        ZoneOffset standardOffset = this.getStandardOffset(instant);
        ZoneOffset actualOffset = this.getOffset(instant);
        return Duration.ofSeconds(actualOffset.getTotalSeconds() - standardOffset.getTotalSeconds());
    }

    public boolean isDaylightSavings(Instant instant) {
        return !this.getStandardOffset(instant).equals(this.getOffset(instant));
    }

    public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
        return this.getValidOffsets(localDateTime).contains(offset);
    }

    public ZoneOffsetTransition nextTransition(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        if (epochSec >= this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1]) {
            ZoneOffsetTransition[] transArray;
            if (this.lastRules.length == 0) {
                return null;
            }
            int year = this.findYear(epochSec, this.wallOffsets[this.wallOffsets.length - 1]);
            for (ZoneOffsetTransition trans : transArray = this.findTransitionArray(year)) {
                if (epochSec >= trans.toEpochSecond()) continue;
                return trans;
            }
            if (year < 999999999) {
                transArray = this.findTransitionArray(year + 1);
                return transArray[0];
            }
            return null;
        }
        int index = Arrays.binarySearch(this.savingsInstantTransitions, epochSec);
        index = index < 0 ? -index - 1 : ++index;
        return new ZoneOffsetTransition(this.savingsInstantTransitions[index], this.wallOffsets[index], this.wallOffsets[index + 1]);
    }

    public ZoneOffsetTransition previousTransition(Instant instant) {
        int index;
        if (this.savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        if (instant.getNano() > 0 && epochSec < Long.MAX_VALUE) {
            ++epochSec;
        }
        long lastHistoric = this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1];
        if (this.lastRules.length > 0 && epochSec > lastHistoric) {
            ZoneOffset lastHistoricOffset = this.wallOffsets[this.wallOffsets.length - 1];
            int year = this.findYear(epochSec, lastHistoricOffset);
            ZoneOffsetTransition[] transArray = this.findTransitionArray(year);
            for (int i = transArray.length - 1; i >= 0; --i) {
                if (epochSec <= transArray[i].toEpochSecond()) continue;
                return transArray[i];
            }
            int lastHistoricYear = this.findYear(lastHistoric, lastHistoricOffset);
            if (--year > lastHistoricYear) {
                transArray = this.findTransitionArray(year);
                return transArray[transArray.length - 1];
            }
        }
        if ((index = Arrays.binarySearch(this.savingsInstantTransitions, epochSec)) < 0) {
            index = -index - 1;
        }
        if (index <= 0) {
            return null;
        }
        return new ZoneOffsetTransition(this.savingsInstantTransitions[index - 1], this.wallOffsets[index - 1], this.wallOffsets[index]);
    }

    private int findYear(long epochSecond, ZoneOffset offset) {
        long yearEst;
        long doyEst;
        long localSecond = epochSecond + (long)offset.getTotalSeconds();
        long zeroDay = Math.floorDiv(localSecond, 86400) + 719528L;
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
        if (doyEst >= 306L) {
            ++yearEst;
        }
        return (int)Math.min(yearEst, 999999999L);
    }

    public List<ZoneOffsetTransition> getTransitions() {
        ArrayList<ZoneOffsetTransition> list = new ArrayList<ZoneOffsetTransition>();
        for (int i = 0; i < this.savingsInstantTransitions.length; ++i) {
            list.add(new ZoneOffsetTransition(this.savingsInstantTransitions[i], this.wallOffsets[i], this.wallOffsets[i + 1]));
        }
        return Collections.unmodifiableList(list);
    }

    public List<ZoneOffsetTransitionRule> getTransitionRules() {
        return List.of(this.lastRules);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object otherRules) {
        if (this == otherRules) {
            return true;
        }
        if (!(otherRules instanceof ZoneRules)) return false;
        ZoneRules other = (ZoneRules)otherRules;
        if (!Arrays.equals(this.standardTransitions, other.standardTransitions)) return false;
        if (!Arrays.equals(this.standardOffsets, other.standardOffsets)) return false;
        if (!Arrays.equals(this.savingsInstantTransitions, other.savingsInstantTransitions)) return false;
        if (!Arrays.equals(this.wallOffsets, other.wallOffsets)) return false;
        if (!Arrays.equals(this.lastRules, other.lastRules)) return false;
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(this.standardTransitions) ^ Arrays.hashCode(this.standardOffsets) ^ Arrays.hashCode(this.savingsInstantTransitions) ^ Arrays.hashCode(this.wallOffsets) ^ Arrays.hashCode(this.lastRules);
    }

    public String toString() {
        return "ZoneRules[currentStandardOffset=" + this.standardOffsets[this.standardOffsets.length - 1] + "]";
    }
}

