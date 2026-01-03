/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.util.platform.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.PdhUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.GlobalConfig;
import oshi.util.Util;
import oshi.util.platform.windows.PerfCounterQuery;
import oshi.util.platform.windows.PerfCounterQueryHandler;
import oshi.util.platform.windows.PerfDataUtil;
import oshi.util.platform.windows.WmiQueryHandler;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class PerfCounterWildcardQuery {
    private static final Logger LOG = LoggerFactory.getLogger(PerfCounterWildcardQuery.class);
    private static final boolean PERF_DISABLE_ALL_ON_FAILURE = GlobalConfig.get("oshi.os.windows.perf.disable.all.on.failure", false);
    private static final Set<String> FAILED_QUERY_CACHE = ConcurrentHashMap.newKeySet();

    private PerfCounterWildcardQuery() {
    }

    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(Class<T> propertyEnum, String perfObject, String perfWmiClass) {
        return PerfCounterWildcardQuery.queryInstancesAndValues(propertyEnum, perfObject, perfWmiClass, null);
    }

    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(Class<T> propertyEnum, String perfObject, String perfWmiClass, String customFilter) {
        if (FAILED_QUERY_CACHE.isEmpty() || !PERF_DISABLE_ALL_ON_FAILURE && !FAILED_QUERY_CACHE.contains(perfObject)) {
            Pair<List<String>, Map<T, List<Long>>> instancesAndValuesMap = PerfCounterWildcardQuery.queryInstancesAndValuesFromPDH(propertyEnum, perfObject, customFilter);
            if (!instancesAndValuesMap.getA().isEmpty()) {
                return instancesAndValuesMap;
            }
            if (Util.isBlank(customFilter)) {
                if (PERF_DISABLE_ALL_ON_FAILURE) {
                    LOG.info("Disabling further attempts to query performance counters.");
                } else {
                    LOG.info("Disabling further attempts to query {}.", (Object)perfObject);
                }
                FAILED_QUERY_CACHE.add(perfObject);
            }
        }
        return PerfCounterWildcardQuery.queryInstancesAndValuesFromWMI(propertyEnum, perfWmiClass);
    }

    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(Class<T> propertyEnum, String perfObject) {
        return PerfCounterWildcardQuery.queryInstancesAndValuesFromPDH(propertyEnum, perfObject, null);
    }

    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(Class<T> propertyEnum, String perfObject, String customFilter) {
        Enum[] props = (Enum[])propertyEnum.getEnumConstants();
        if (props.length < 2) {
            throw new IllegalArgumentException("Enum " + propertyEnum.getName() + " must have at least two elements, an instance filter and a counter.");
        }
        String instanceFilter = Util.isBlank(customFilter) ? ((PdhCounterWildcardProperty)((Object)((Enum[])propertyEnum.getEnumConstants())[0])).getCounter().toLowerCase(Locale.ROOT) : customFilter;
        String perfObjectLocalized = PerfCounterQuery.localizeIfNeeded(perfObject, true);
        PdhUtil.PdhEnumObjectItems objectItems = null;
        try {
            objectItems = PdhUtil.PdhEnumObjectItems(null, null, perfObjectLocalized, 100);
        }
        catch (PdhUtil.PdhException e) {
            LOG.warn("Failed to locate performance object for {} in the registry. Performance counters may be corrupt. {}", (Object)perfObjectLocalized, (Object)e.getMessage());
        }
        if (objectItems == null) {
            return new Pair<List<String>, Map<T, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        List<String> instances = objectItems.getInstances();
        instances.removeIf(i -> !Util.wildcardMatch(i.toLowerCase(Locale.ROOT), instanceFilter));
        EnumMap valuesMap = new EnumMap(propertyEnum);
        try (PerfCounterQueryHandler pdhQueryHandler = new PerfCounterQueryHandler();){
            Enum prop;
            int i2;
            EnumMap counterListMap = new EnumMap(propertyEnum);
            for (i2 = 1; i2 < props.length; ++i2) {
                prop = props[i2];
                ArrayList<PerfDataUtil.PerfCounter> counterList = new ArrayList<PerfDataUtil.PerfCounter>(instances.size());
                for (String instance : instances) {
                    PerfDataUtil.PerfCounter counter = PerfDataUtil.createCounter(perfObject, instance, ((PdhCounterWildcardProperty)((Object)prop)).getCounter());
                    if (!pdhQueryHandler.addCounterToQuery(counter)) {
                        Pair<List<String>, Map<T, List<Long>>> pair = new Pair<List<String>, Map<T, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
                        return pair;
                    }
                    counterList.add(counter);
                }
                counterListMap.put(prop, counterList);
            }
            if (0L < pdhQueryHandler.updateQuery()) {
                for (i2 = 1; i2 < props.length; ++i2) {
                    prop = props[i2];
                    ArrayList<Long> values = new ArrayList<Long>();
                    for (PerfDataUtil.PerfCounter counter : (List)counterListMap.get(prop)) {
                        values.add(pdhQueryHandler.queryCounter(counter));
                    }
                    valuesMap.put(prop, values);
                }
            }
        }
        return new Pair<List<String>, Map<T, List<Long>>>(instances, valuesMap);
    }

    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromWMI(Class<T> propertyEnum, String wmiClass) {
        ArrayList<String> instances = new ArrayList<String>();
        EnumMap valuesMap = new EnumMap(propertyEnum);
        WbemcliUtil.WmiQuery<T> query = new WbemcliUtil.WmiQuery<T>(wmiClass, propertyEnum);
        WbemcliUtil.WmiResult<Enum> result = Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(query);
        if (result.getResultCount() > 0) {
            for (Enum prop : (Enum[])propertyEnum.getEnumConstants()) {
                if (prop.ordinal() == 0) {
                    for (int i = 0; i < result.getResultCount(); ++i) {
                        instances.add(WmiUtil.getString(result, prop, i));
                    }
                    continue;
                }
                ArrayList<Long> values = new ArrayList<Long>();
                block8: for (int i = 0; i < result.getResultCount(); ++i) {
                    switch (result.getCIMType(prop)) {
                        case 18: {
                            values.add(Long.valueOf(WmiUtil.getUint16(result, prop, i)));
                            continue block8;
                        }
                        case 19: {
                            values.add(WmiUtil.getUint32asLong(result, prop, i));
                            continue block8;
                        }
                        case 21: {
                            values.add(WmiUtil.getUint64(result, prop, i));
                            continue block8;
                        }
                        case 101: {
                            values.add(WmiUtil.getDateTime(result, prop, i).toInstant().toEpochMilli());
                            continue block8;
                        }
                        default: {
                            throw new ClassCastException("Unimplemented CIM Type Mapping.");
                        }
                    }
                }
                valuesMap.put(prop, values);
            }
        }
        return new Pair<List<String>, Map<T, List<Long>>>(instances, valuesMap);
    }

    public static interface PdhCounterWildcardProperty {
        public String getCounter();
    }
}

