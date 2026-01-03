/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Toolkit;
import java.awt.VKCollection;
import java.awt.event.KeyEvent;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import sun.awt.AppContext;
import sun.swing.SwingAccessor;

public class AWTKeyStroke
implements Serializable {
    private static final long serialVersionUID = -6430539691155161871L;
    private static Map<String, Integer> modifierKeywords;
    private static VKCollection vks;
    private static Object APP_CONTEXT_CACHE_KEY;
    private static AWTKeyStroke APP_CONTEXT_KEYSTROKE_KEY;
    private char keyChar = (char)65535;
    private int keyCode = 0;
    private int modifiers;
    private boolean onKeyRelease;

    protected AWTKeyStroke() {
    }

    protected AWTKeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
        this.keyChar = keyChar;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.onKeyRelease = onKeyRelease;
    }

    @Deprecated
    protected static void registerSubclass(Class<?> subclass) {
    }

    private static synchronized AWTKeyStroke getCachedStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
        HashMap<AWTKeyStroke, AWTKeyStroke> cache = (HashMap<AWTKeyStroke, AWTKeyStroke>)AppContext.getAppContext().get(APP_CONTEXT_CACHE_KEY);
        AWTKeyStroke cacheKey = (AWTKeyStroke)AppContext.getAppContext().get(APP_CONTEXT_KEYSTROKE_KEY);
        if (cache == null) {
            cache = new HashMap<AWTKeyStroke, AWTKeyStroke>();
            AppContext.getAppContext().put(APP_CONTEXT_CACHE_KEY, cache);
        }
        if (cacheKey == null) {
            cacheKey = SwingAccessor.getKeyStrokeAccessor().create();
            AppContext.getAppContext().put(APP_CONTEXT_KEYSTROKE_KEY, cacheKey);
        }
        cacheKey.keyChar = keyChar;
        cacheKey.keyCode = keyCode;
        cacheKey.modifiers = AWTKeyStroke.mapNewModifiers(AWTKeyStroke.mapOldModifiers(modifiers));
        cacheKey.onKeyRelease = onKeyRelease;
        AWTKeyStroke stroke = (AWTKeyStroke)cache.get(cacheKey);
        if (stroke == null) {
            stroke = cacheKey;
            cache.put(stroke, stroke);
            AppContext.getAppContext().remove(APP_CONTEXT_KEYSTROKE_KEY);
        }
        return stroke;
    }

    public static AWTKeyStroke getAWTKeyStroke(char keyChar) {
        return AWTKeyStroke.getCachedStroke(keyChar, 0, 0, false);
    }

    public static AWTKeyStroke getAWTKeyStroke(Character keyChar, int modifiers) {
        if (keyChar == null) {
            throw new IllegalArgumentException("keyChar cannot be null");
        }
        return AWTKeyStroke.getCachedStroke(keyChar.charValue(), 0, modifiers, false);
    }

    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
        return AWTKeyStroke.getCachedStroke('\uffff', keyCode, modifiers, onKeyRelease);
    }

    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers) {
        return AWTKeyStroke.getCachedStroke('\uffff', keyCode, modifiers, false);
    }

    public static AWTKeyStroke getAWTKeyStrokeForEvent(KeyEvent anEvent) {
        int id = anEvent.getID();
        switch (id) {
            case 401: 
            case 402: {
                return AWTKeyStroke.getCachedStroke('\uffff', anEvent.getKeyCode(), anEvent.getModifiers(), id == 402);
            }
            case 400: {
                return AWTKeyStroke.getCachedStroke(anEvent.getKeyChar(), 0, anEvent.getModifiers(), false);
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static AWTKeyStroke getAWTKeyStroke(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        String errmsg = "String formatted incorrectly";
        StringTokenizer st = new StringTokenizer(s, " ");
        int mask = 0;
        boolean released = false;
        boolean typed = false;
        boolean pressed = false;
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            if (modifierKeywords == null) {
                HashMap<String, Integer> uninitializedMap = new HashMap<String, Integer>(8, 1.0f);
                uninitializedMap.put("shift", 65);
                uninitializedMap.put("control", 130);
                uninitializedMap.put("ctrl", 130);
                uninitializedMap.put("meta", 260);
                uninitializedMap.put("alt", 520);
                uninitializedMap.put("altGraph", 8224);
                uninitializedMap.put("button1", 1024);
                uninitializedMap.put("button2", 2048);
                uninitializedMap.put("button3", 4096);
                modifierKeywords = Collections.synchronizedMap(uninitializedMap);
            }
            // ** MonitorExit[var7_7] (shouldn't be in output)
            int count = st.countTokens();
            for (int i = 1; i <= count; ++i) {
                String token = st.nextToken();
                if (typed) {
                    if (token.length() != 1 || i != count) {
                        throw new IllegalArgumentException("String formatted incorrectly");
                    }
                    return AWTKeyStroke.getCachedStroke(token.charAt(0), 0, mask, false);
                }
                if (pressed || released || i == count) {
                    if (i != count) {
                        throw new IllegalArgumentException("String formatted incorrectly");
                    }
                    String keyCodeName = "VK_" + token;
                    int keyCode = AWTKeyStroke.getVKValue(keyCodeName);
                    return AWTKeyStroke.getCachedStroke('\uffff', keyCode, mask, released);
                }
                if (token.equals("released")) {
                    released = true;
                    continue;
                }
                if (token.equals("pressed")) {
                    pressed = true;
                    continue;
                }
                if (token.equals("typed")) {
                    typed = true;
                    continue;
                }
                Integer tokenMask = modifierKeywords.get(token);
                if (tokenMask != null) {
                    mask |= tokenMask.intValue();
                    continue;
                }
                throw new IllegalArgumentException("String formatted incorrectly");
            }
            throw new IllegalArgumentException("String formatted incorrectly");
        }
    }

    private static VKCollection getVKCollection() {
        if (vks == null) {
            vks = new VKCollection();
        }
        return vks;
    }

    private static int getVKValue(String key) {
        VKCollection vkCollect = AWTKeyStroke.getVKCollection();
        Integer value = vkCollect.findCode(key);
        if (value == null) {
            int keyCode = 0;
            String errmsg = "String formatted incorrectly";
            try {
                keyCode = KeyEvent.class.getField(key).getInt(KeyEvent.class);
            }
            catch (IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalArgumentException("String formatted incorrectly");
            }
            value = keyCode;
            vkCollect.put(key, value);
        }
        return value;
    }

    public final char getKeyChar() {
        return this.keyChar;
    }

    public final int getKeyCode() {
        return this.keyCode;
    }

    public final int getModifiers() {
        return this.modifiers;
    }

    public final boolean isOnKeyRelease() {
        return this.onKeyRelease;
    }

    public final int getKeyEventType() {
        if (this.keyCode == 0) {
            return 400;
        }
        return this.onKeyRelease ? 402 : 401;
    }

    public int hashCode() {
        return (this.keyChar + '\u0001') * (2 * (this.keyCode + 1)) * (this.modifiers + 1) + (this.onKeyRelease ? 1 : 2);
    }

    public final boolean equals(Object anObject) {
        if (anObject instanceof AWTKeyStroke) {
            AWTKeyStroke ks = (AWTKeyStroke)anObject;
            return ks.keyChar == this.keyChar && ks.keyCode == this.keyCode && ks.onKeyRelease == this.onKeyRelease && ks.modifiers == this.modifiers;
        }
        return false;
    }

    public String toString() {
        if (this.keyCode == 0) {
            return AWTKeyStroke.getModifiersText(this.modifiers) + "typed " + this.keyChar;
        }
        return AWTKeyStroke.getModifiersText(this.modifiers) + (this.onKeyRelease ? "released" : "pressed") + " " + AWTKeyStroke.getVKText(this.keyCode);
    }

    static String getModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & 0x40) != 0) {
            buf.append("shift ");
        }
        if ((modifiers & 0x80) != 0) {
            buf.append("ctrl ");
        }
        if ((modifiers & 0x100) != 0) {
            buf.append("meta ");
        }
        if ((modifiers & 0x200) != 0) {
            buf.append("alt ");
        }
        if ((modifiers & 0x2000) != 0) {
            buf.append("altGraph ");
        }
        if ((modifiers & 0x400) != 0) {
            buf.append("button1 ");
        }
        if ((modifiers & 0x800) != 0) {
            buf.append("button2 ");
        }
        if ((modifiers & 0x1000) != 0) {
            buf.append("button3 ");
        }
        return buf.toString();
    }

    static String getVKText(int keyCode) {
        Integer key;
        VKCollection vkCollect = AWTKeyStroke.getVKCollection();
        String name = vkCollect.findName(key = Integer.valueOf(keyCode));
        if (name != null) {
            return name.substring(3);
        }
        int expected_modifiers = 25;
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            try {
                if (fields[i].getModifiers() != expected_modifiers || fields[i].getType() != Integer.TYPE || !fields[i].getName().startsWith("VK_") || fields[i].getInt(KeyEvent.class) != keyCode) continue;
                name = fields[i].getName();
                vkCollect.put(name, key);
                return name.substring(3);
            }
            catch (IllegalAccessException e) {
                assert (false);
                continue;
            }
        }
        return "UNKNOWN";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Object readResolve() throws ObjectStreamException {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return AWTKeyStroke.getCachedStroke(this.keyChar, this.keyCode, this.modifiers, this.onKeyRelease);
        }
    }

    private static int mapOldModifiers(int modifiers) {
        if ((modifiers & 1) != 0) {
            modifiers |= 0x40;
        }
        if ((modifiers & 8) != 0) {
            modifiers |= 0x200;
        }
        if ((modifiers & 0x20) != 0) {
            modifiers |= 0x2000;
        }
        if ((modifiers & 2) != 0) {
            modifiers |= 0x80;
        }
        if ((modifiers & 4) != 0) {
            modifiers |= 0x100;
        }
        return modifiers &= 0x3FC0;
    }

    private static int mapNewModifiers(int modifiers) {
        if ((modifiers & 0x40) != 0) {
            modifiers |= 1;
        }
        if ((modifiers & 0x200) != 0) {
            modifiers |= 8;
        }
        if ((modifiers & 0x2000) != 0) {
            modifiers |= 0x20;
        }
        if ((modifiers & 0x80) != 0) {
            modifiers |= 2;
        }
        if ((modifiers & 0x100) != 0) {
            modifiers |= 4;
        }
        return modifiers;
    }

    static {
        APP_CONTEXT_CACHE_KEY = new Object();
        APP_CONTEXT_KEYSTROKE_KEY = new AWTKeyStroke();
        Toolkit.loadLibraries();
    }
}

