/*
 * Decompiled with CFR 0.152.
 */
package java.util.logging;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import jdk.internal.access.JavaUtilResourceBundleAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.loader.ClassLoaderValue;

public class Level
implements Serializable {
    private static final String defaultBundle = "sun.util.logging.resources.logging";
    private final String name;
    private final int value;
    private final String resourceBundleName;
    private transient String localizedLevelName;
    private transient Locale cachedLocale;
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, "sun.util.logging.resources.logging");
    public static final Level SEVERE = new Level("SEVERE", 1000, "sun.util.logging.resources.logging");
    public static final Level WARNING = new Level("WARNING", 900, "sun.util.logging.resources.logging");
    public static final Level INFO = new Level("INFO", 800, "sun.util.logging.resources.logging");
    public static final Level CONFIG = new Level("CONFIG", 700, "sun.util.logging.resources.logging");
    public static final Level FINE = new Level("FINE", 500, "sun.util.logging.resources.logging");
    public static final Level FINER = new Level("FINER", 400, "sun.util.logging.resources.logging");
    public static final Level FINEST = new Level("FINEST", 300, "sun.util.logging.resources.logging");
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, "sun.util.logging.resources.logging");
    private static final Level[] standardLevels = new Level[]{OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL};
    private static final long serialVersionUID = -8176160795706313070L;

    protected Level(String name, int value) {
        this(name, value, null);
    }

    protected Level(String name, int value, String resourceBundleName) {
        this(name, value, resourceBundleName, true);
    }

    private Level(String name, int value, String resourceBundleName, boolean visible) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
        this.localizedLevelName = resourceBundleName == null ? name : null;
        this.cachedLocale = null;
        if (visible) {
            KnownLevel.add(this);
        }
    }

    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    public String getName() {
        return this.name;
    }

    public String getLocalizedName() {
        return this.getLocalizedLevelName();
    }

    final String getLevelName() {
        return this.name;
    }

    private String computeLocalizedLevelName(Locale newLocale) {
        Module module = this.getClass().getModule();
        ResourceBundle rb = RbAccess.RB_ACCESS.getBundle(this.resourceBundleName, newLocale, module);
        String localizedName = rb.getString(this.name);
        boolean isDefaultBundle = defaultBundle.equals(this.resourceBundleName);
        if (!isDefaultBundle) {
            return localizedName;
        }
        Locale rbLocale = rb.getLocale();
        Locale locale = Locale.ROOT.equals(rbLocale) || this.name.equals(localizedName.toUpperCase(Locale.ROOT)) ? Locale.ROOT : rbLocale;
        return Locale.ROOT.equals(locale) ? this.name : localizedName.toUpperCase(locale);
    }

    final String getCachedLocalizedLevelName() {
        if (this.localizedLevelName != null && this.cachedLocale != null && this.cachedLocale.equals(Locale.getDefault())) {
            return this.localizedLevelName;
        }
        if (this.resourceBundleName == null) {
            return this.name;
        }
        return null;
    }

    final synchronized String getLocalizedLevelName() {
        String cachedLocalizedName = this.getCachedLocalizedLevelName();
        if (cachedLocalizedName != null) {
            return cachedLocalizedName;
        }
        Locale newLocale = Locale.getDefault();
        try {
            this.localizedLevelName = this.computeLocalizedLevelName(newLocale);
        }
        catch (Exception ex) {
            this.localizedLevelName = this.name;
        }
        this.cachedLocale = newLocale;
        return this.localizedLevelName;
    }

    static Level findLevel(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        Optional<Level> level = KnownLevel.findByName(name, KnownLevel::mirrored);
        if (level.isPresent()) {
            return level.get();
        }
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x, KnownLevel::mirrored);
            if (level.isPresent()) {
                return level.get();
            }
            Level levelObject = new Level(name, x);
            return KnownLevel.findByValue(x, KnownLevel::mirrored).get();
        }
        catch (NumberFormatException numberFormatException) {
            level = KnownLevel.findByLocalizedLevelName(name, KnownLevel::mirrored);
            if (level.isPresent()) {
                return level.get();
            }
            return null;
        }
    }

    public final String toString() {
        return this.name;
    }

    public final int intValue() {
        return this.value;
    }

    private Object readResolve() {
        Optional<Level> level = KnownLevel.matches(this);
        if (level.isPresent()) {
            return level.get();
        }
        return new Level(this.name, this.value, this.resourceBundleName);
    }

    public static synchronized Level parse(String name) throws IllegalArgumentException {
        name.length();
        Optional<Level> level = KnownLevel.findByName(name, KnownLevel::referent);
        if (level.isPresent()) {
            return level.get();
        }
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x, KnownLevel::referent);
            if (level.isPresent()) {
                return level.get();
            }
            Level levelObject = new Level(name, x);
            return KnownLevel.findByValue(x, KnownLevel::referent).get();
        }
        catch (NumberFormatException numberFormatException) {
            level = KnownLevel.findByLocalizedLevelName(name, KnownLevel::referent);
            if (level.isPresent()) {
                return level.get();
            }
            throw new IllegalArgumentException("Bad level \"" + name + "\"");
        }
    }

    public boolean equals(Object ox) {
        try {
            Level lx = (Level)ox;
            return lx.value == this.value;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public int hashCode() {
        return this.value;
    }

    static final class KnownLevel
    extends WeakReference<Level> {
        private static Map<String, List<KnownLevel>> nameToLevels = new HashMap<String, List<KnownLevel>>();
        private static Map<Integer, List<KnownLevel>> intToLevels = new HashMap<Integer, List<KnownLevel>>();
        private static final ReferenceQueue<Level> QUEUE = new ReferenceQueue();
        private static final ClassLoaderValue<List<Level>> CUSTOM_LEVEL_CLV = new ClassLoaderValue();
        final Level mirroredLevel;

        KnownLevel(Level l) {
            super(l, QUEUE);
            this.mirroredLevel = l.getClass() == Level.class ? l : new Level(l.name, l.value, l.resourceBundleName, false);
        }

        Optional<Level> mirrored() {
            return Optional.of(this.mirroredLevel);
        }

        Optional<Level> referent() {
            return Optional.ofNullable((Level)this.get());
        }

        private void remove() {
            Optional.ofNullable(nameToLevels.get(this.mirroredLevel.name)).ifPresent(x -> x.remove(this));
            Optional.ofNullable(intToLevels.get(this.mirroredLevel.value)).ifPresent(x -> x.remove(this));
        }

        static synchronized void purge() {
            Reference<Level> ref;
            while ((ref = QUEUE.poll()) != null) {
                if (!(ref instanceof KnownLevel)) continue;
                ((KnownLevel)ref).remove();
            }
        }

        private static void registerWithClassLoader(Level customLevel) {
            PrivilegedAction<ClassLoader> pa = customLevel.getClass()::getClassLoader;
            ClassLoader cl = AccessController.doPrivileged(pa);
            CUSTOM_LEVEL_CLV.computeIfAbsent(cl, (c, v) -> new ArrayList()).add(customLevel);
        }

        static synchronized void add(Level l) {
            KnownLevel.purge();
            KnownLevel o = new KnownLevel(l);
            nameToLevels.computeIfAbsent(l.name, k -> new ArrayList()).add(o);
            intToLevels.computeIfAbsent(l.value, k -> new ArrayList()).add(o);
            if (o.mirroredLevel != l) {
                KnownLevel.registerWithClassLoader(l);
            }
        }

        static synchronized Optional<Level> findByName(String name, Function<KnownLevel, Optional<Level>> selector) {
            KnownLevel.purge();
            return nameToLevels.getOrDefault(name, Collections.emptyList()).stream().map(selector).flatMap(Optional::stream).findFirst();
        }

        static synchronized Optional<Level> findByValue(int value, Function<KnownLevel, Optional<Level>> selector) {
            KnownLevel.purge();
            return intToLevels.getOrDefault(value, Collections.emptyList()).stream().map(selector).flatMap(Optional::stream).findFirst();
        }

        static synchronized Optional<Level> findByLocalizedLevelName(String name, Function<KnownLevel, Optional<Level>> selector) {
            KnownLevel.purge();
            return nameToLevels.values().stream().flatMap(Collection::stream).map(selector).flatMap(Optional::stream).filter(l -> name.equals(l.getLocalizedLevelName())).findFirst();
        }

        static synchronized Optional<Level> matches(Level l) {
            KnownLevel.purge();
            List<KnownLevel> list = nameToLevels.get(l.name);
            if (list != null) {
                for (KnownLevel ref : list) {
                    Level levelObject = (Level)ref.get();
                    if (levelObject == null) continue;
                    Level other = ref.mirroredLevel;
                    Class<?> type = levelObject.getClass();
                    if (l.value != other.value || l.resourceBundleName != other.resourceBundleName && (l.resourceBundleName == null || !l.resourceBundleName.equals(other.resourceBundleName)) || type != l.getClass()) continue;
                    return Optional.of(levelObject);
                }
            }
            return Optional.empty();
        }
    }

    private static final class RbAccess {
        static final JavaUtilResourceBundleAccess RB_ACCESS = SharedSecrets.getJavaUtilResourceBundleAccess();

        private RbAccess() {
        }
    }
}

