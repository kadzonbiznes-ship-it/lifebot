/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.zone.TzdbZoneRulesProvider;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ZoneRulesProvider {
    private static final CopyOnWriteArrayList<ZoneRulesProvider> PROVIDERS = new CopyOnWriteArrayList();
    private static final ConcurrentMap<String, ZoneRulesProvider> ZONES = new ConcurrentHashMap<String, ZoneRulesProvider>(512, 0.75f, 2);
    private static volatile Set<String> ZONE_IDS;

    public static Set<String> getAvailableZoneIds() {
        return ZONE_IDS;
    }

    public static ZoneRules getRules(String zoneId, boolean forCaching) {
        Objects.requireNonNull(zoneId, "zoneId");
        return ZoneRulesProvider.getProvider(zoneId).provideRules(zoneId, forCaching);
    }

    public static NavigableMap<String, ZoneRules> getVersions(String zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        return ZoneRulesProvider.getProvider(zoneId).provideVersions(zoneId);
    }

    private static ZoneRulesProvider getProvider(String zoneId) {
        ZoneRulesProvider provider = (ZoneRulesProvider)ZONES.get(zoneId);
        if (provider == null) {
            if (ZONES.isEmpty()) {
                throw new ZoneRulesException("No time-zone data files registered");
            }
            throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
        }
        return provider;
    }

    public static void registerProvider(ZoneRulesProvider provider) {
        Objects.requireNonNull(provider, "provider");
        ZoneRulesProvider.registerProvider0(provider);
        PROVIDERS.add(provider);
    }

    private static synchronized void registerProvider0(ZoneRulesProvider provider) {
        for (String zoneId : provider.provideZoneIds()) {
            Objects.requireNonNull(zoneId, "zoneId");
            ZoneRulesProvider old = ZONES.putIfAbsent(zoneId, provider);
            if (old == null) continue;
            if (!old.equals(provider)) {
                ZONES.put(zoneId, old);
                provider.provideZoneIds().forEach(id -> ZONES.remove(id, provider));
            }
            throw new ZoneRulesException("Unable to register zone as one already registered with that ID: " + zoneId + ", currently loading from provider: " + provider);
        }
        HashSet combinedSet = new HashSet(ZONES.keySet());
        ZONE_IDS = Collections.unmodifiableSet(combinedSet);
    }

    public static boolean refresh() {
        boolean changed = false;
        for (ZoneRulesProvider provider : PROVIDERS) {
            changed |= provider.provideRefresh();
        }
        return changed;
    }

    protected ZoneRulesProvider() {
    }

    protected abstract Set<String> provideZoneIds();

    protected abstract ZoneRules provideRules(String var1, boolean var2);

    protected abstract NavigableMap<String, ZoneRules> provideVersions(String var1);

    protected boolean provideRefresh() {
        return false;
    }

    static {
        List<ZoneRulesProvider> loaded = AccessController.doPrivileged(new PrivilegedAction<List<ZoneRulesProvider>>(){

            @Override
            public List<ZoneRulesProvider> run() {
                ArrayList<ZoneRulesProvider> result = new ArrayList<ZoneRulesProvider>();
                String prop = System.getProperty("java.time.zone.DefaultZoneRulesProvider");
                if (prop != null) {
                    try {
                        Class<?> c = Class.forName(prop, true, ClassLoader.getSystemClassLoader());
                        ZoneRulesProvider provider = (ZoneRulesProvider)ZoneRulesProvider.class.cast(c.newInstance());
                        ZoneRulesProvider.registerProvider(provider);
                        result.add(provider);
                    }
                    catch (Exception x) {
                        throw new Error(x);
                    }
                } else {
                    ZoneRulesProvider.registerProvider(new TzdbZoneRulesProvider());
                }
                return result;
            }
        });
        ServiceLoader<ZoneRulesProvider> sl = ServiceLoader.load(ZoneRulesProvider.class, ClassLoader.getSystemClassLoader());
        Iterator<ZoneRulesProvider> it = sl.iterator();
        while (it.hasNext()) {
            ZoneRulesProvider provider;
            try {
                provider = it.next();
            }
            catch (ServiceConfigurationError ex) {
                if (ex.getCause() instanceof SecurityException) continue;
                throw ex;
            }
            boolean found = false;
            for (ZoneRulesProvider p : loaded) {
                if (p.getClass() != provider.getClass()) continue;
                found = true;
            }
            if (found) continue;
            ZoneRulesProvider.registerProvider0(provider);
            loaded.add(provider);
        }
        PROVIDERS.addAll(loaded);
    }
}

