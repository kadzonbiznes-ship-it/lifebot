/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import sun.awt.AppContext;

public class DefaultLookup {
    private static final Object DEFAULT_LOOKUP_KEY = new StringBuffer("DefaultLookup");
    private static Thread currentDefaultThread;
    private static DefaultLookup currentDefaultLookup;
    private static boolean isLookupSet;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setDefaultLookup(DefaultLookup lookup) {
        Class<DefaultLookup> clazz = DefaultLookup.class;
        synchronized (DefaultLookup.class) {
            if (!isLookupSet && lookup == null) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
            if (lookup == null) {
                lookup = new DefaultLookup();
            }
            isLookupSet = true;
            AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
            currentDefaultThread = Thread.currentThread();
            currentDefaultLookup = lookup;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object get(JComponent c, ComponentUI ui, String key) {
        Class<DefaultLookup> clazz = DefaultLookup.class;
        synchronized (DefaultLookup.class) {
            boolean lookupSet = isLookupSet;
            // ** MonitorExit[var4_3] (shouldn't be in output)
            if (!lookupSet) {
                return UIManager.get(key, c.getLocale());
            }
            Thread thisThread = Thread.currentThread();
            Class<DefaultLookup> clazz2 = DefaultLookup.class;
            synchronized (DefaultLookup.class) {
                DefaultLookup lookup;
                if (thisThread == currentDefaultThread) {
                    lookup = currentDefaultLookup;
                } else {
                    lookup = (DefaultLookup)AppContext.getAppContext().get(DEFAULT_LOOKUP_KEY);
                    if (lookup == null) {
                        lookup = new DefaultLookup();
                        AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
                    }
                    currentDefaultThread = thisThread;
                    currentDefaultLookup = lookup;
                }
                // ** MonitorExit[var6_7] (shouldn't be in output)
                return lookup.getDefault(c, ui, key);
            }
        }
    }

    public static int getInt(JComponent c, ComponentUI ui, String key, int defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Number) {
            Number number = (Number)iValue;
            return number.intValue();
        }
        return defaultValue;
    }

    public static int getInt(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getInt(c, ui, key, -1);
    }

    public static Insets getInsets(JComponent c, ComponentUI ui, String key, Insets defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Insets) {
            Insets insets = (Insets)iValue;
            return insets;
        }
        return defaultValue;
    }

    public static Insets getInsets(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getInsets(c, ui, key, null);
    }

    public static boolean getBoolean(JComponent c, ComponentUI ui, String key, boolean defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Boolean) {
            Boolean b = (Boolean)iValue;
            return b;
        }
        return defaultValue;
    }

    public static boolean getBoolean(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getBoolean(c, ui, key, false);
    }

    public static Color getColor(JComponent c, ComponentUI ui, String key, Color defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Color) {
            Color color = (Color)iValue;
            return color;
        }
        return defaultValue;
    }

    public static Color getColor(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getColor(c, ui, key, null);
    }

    public static Icon getIcon(JComponent c, ComponentUI ui, String key, Icon defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Icon) {
            Icon icon = (Icon)iValue;
            return icon;
        }
        return defaultValue;
    }

    public static Icon getIcon(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getIcon(c, ui, key, null);
    }

    public static Border getBorder(JComponent c, ComponentUI ui, String key, Border defaultValue) {
        Object iValue = DefaultLookup.get(c, ui, key);
        if (iValue instanceof Border) {
            Border border = (Border)iValue;
            return border;
        }
        return defaultValue;
    }

    public static Border getBorder(JComponent c, ComponentUI ui, String key) {
        return DefaultLookup.getBorder(c, ui, key, null);
    }

    public Object getDefault(JComponent c, ComponentUI ui, String key) {
        return UIManager.get(key, c.getLocale());
    }
}

