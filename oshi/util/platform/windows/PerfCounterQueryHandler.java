/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.NotThreadSafe
 *  oshi.util.FormatUtil
 */
package oshi.util.platform.windows;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.NotThreadSafe;
import oshi.jna.ByRef;
import oshi.util.FormatUtil;
import oshi.util.platform.windows.PerfDataUtil;

@NotThreadSafe
public final class PerfCounterQueryHandler
implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PerfCounterQueryHandler.class);
    private Map<PerfDataUtil.PerfCounter, ByRef.CloseableHANDLEByReference> counterHandleMap = new HashMap<PerfDataUtil.PerfCounter, ByRef.CloseableHANDLEByReference>();
    private ByRef.CloseableHANDLEByReference queryHandle = null;

    public boolean addCounterToQuery(PerfDataUtil.PerfCounter counter) {
        if (this.queryHandle == null) {
            this.queryHandle = new ByRef.CloseableHANDLEByReference();
            if (!PerfDataUtil.openQuery(this.queryHandle)) {
                LOG.warn("Failed to open a query for PDH counter: {}", (Object)counter.getCounterPath());
                this.queryHandle.close();
                this.queryHandle = null;
                return false;
            }
        }
        ByRef.CloseableHANDLEByReference p = new ByRef.CloseableHANDLEByReference();
        if (!PerfDataUtil.addCounter(this.queryHandle, counter.getCounterPath(), p)) {
            LOG.warn("Failed to add counter for PDH counter: {}", (Object)counter.getCounterPath());
            p.close();
            return false;
        }
        this.counterHandleMap.put(counter, p);
        return true;
    }

    public boolean removeCounterFromQuery(PerfDataUtil.PerfCounter counter) {
        boolean success = false;
        try (ByRef.CloseableHANDLEByReference href = this.counterHandleMap.remove(counter);){
            if (href != null) {
                success = PerfDataUtil.removeCounter(href);
            }
        }
        if (this.counterHandleMap.isEmpty()) {
            PerfDataUtil.closeQuery(this.queryHandle);
            this.queryHandle.close();
            this.queryHandle = null;
        }
        return success;
    }

    public void removeAllCounters() {
        for (ByRef.CloseableHANDLEByReference href : this.counterHandleMap.values()) {
            PerfDataUtil.removeCounter(href);
            href.close();
        }
        this.counterHandleMap.clear();
        if (this.queryHandle != null) {
            PerfDataUtil.closeQuery(this.queryHandle);
            this.queryHandle.close();
            this.queryHandle = null;
        }
    }

    public long updateQuery() {
        if (this.queryHandle == null) {
            LOG.warn("Query does not exist to update.");
            return 0L;
        }
        return PerfDataUtil.updateQueryTimestamp(this.queryHandle);
    }

    public long queryCounter(PerfDataUtil.PerfCounter counter) {
        long value;
        if (!this.counterHandleMap.containsKey(counter)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Counter {} does not exist to query.", (Object)counter.getCounterPath());
            }
            return 0L;
        }
        long l = value = counter.isBaseCounter() ? PerfDataUtil.querySecondCounter(this.counterHandleMap.get(counter)) : PerfDataUtil.queryCounter(this.counterHandleMap.get(counter));
        if (value < 0L) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Error querying counter {}: {}", (Object)counter.getCounterPath(), (Object)String.format(Locale.ROOT, FormatUtil.formatError((int)((int)value)), new Object[0]));
            }
            return 0L;
        }
        return value;
    }

    @Override
    public void close() {
        this.removeAllCounters();
    }
}

