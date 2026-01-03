/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AWTKeyStroke;
import java.awt.event.KeyEvent;
import sun.swing.SwingAccessor;

public class KeyStroke
extends AWTKeyStroke {
    private static final long serialVersionUID = -9060180771037902530L;

    private KeyStroke() {
    }

    private KeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
        super(keyChar, keyCode, modifiers, onKeyRelease);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStroke(char keyChar) {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return (KeyStroke)KeyStroke.getAWTKeyStroke(keyChar);
        }
    }

    @Deprecated
    public static KeyStroke getKeyStroke(char keyChar, boolean onKeyRelease) {
        return new KeyStroke(keyChar, 0, 0, onKeyRelease);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStroke(Character keyChar, int modifiers) {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return (KeyStroke)KeyStroke.getAWTKeyStroke(keyChar, modifiers);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return (KeyStroke)KeyStroke.getAWTKeyStroke(keyCode, modifiers, onKeyRelease);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStroke(int keyCode, int modifiers) {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return (KeyStroke)KeyStroke.getAWTKeyStroke(keyCode, modifiers);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStrokeForEvent(KeyEvent anEvent) {
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return (KeyStroke)KeyStroke.getAWTKeyStrokeForEvent(anEvent);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static KeyStroke getKeyStroke(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        Class<AWTKeyStroke> clazz = AWTKeyStroke.class;
        synchronized (AWTKeyStroke.class) {
            try {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return (KeyStroke)KeyStroke.getAWTKeyStroke(s);
            }
            catch (IllegalArgumentException e) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
        }
    }

    static {
        SwingAccessor.setKeyStrokeAccessor(new SwingAccessor.KeyStrokeAccessor(){

            @Override
            public KeyStroke create() {
                return new KeyStroke();
            }
        });
    }
}

