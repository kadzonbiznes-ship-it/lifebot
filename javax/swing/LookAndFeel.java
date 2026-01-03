/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.ComponentInputMap;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import sun.awt.SunToolkit;
import sun.swing.DefaultLayoutStyle;
import sun.swing.ImageIconUIResource;
import sun.swing.SwingUtilities2;

public abstract class LookAndFeel {
    protected LookAndFeel() {
    }

    public static void installColors(JComponent c, String defaultBgName, String defaultFgName) {
        Color fg;
        Color bg = c.getBackground();
        if (bg == null || bg instanceof UIResource) {
            c.setBackground(UIManager.getColor(defaultBgName));
        }
        if ((fg = c.getForeground()) == null || fg instanceof UIResource) {
            c.setForeground(UIManager.getColor(defaultFgName));
        }
    }

    public static void installColorsAndFont(JComponent c, String defaultBgName, String defaultFgName, String defaultFontName) {
        Font f = c.getFont();
        if (f == null || f instanceof UIResource) {
            c.setFont(UIManager.getFont(defaultFontName));
        }
        LookAndFeel.installColors(c, defaultBgName, defaultFgName);
    }

    public static void installBorder(JComponent c, String defaultBorderName) {
        Border b = c.getBorder();
        if (b == null || b instanceof UIResource) {
            c.setBorder(UIManager.getBorder(defaultBorderName));
        }
    }

    public static void uninstallBorder(JComponent c) {
        if (c.getBorder() instanceof UIResource) {
            c.setBorder(null);
        }
    }

    public static void installProperty(JComponent c, String propertyName, Object propertyValue) {
        if (SunToolkit.isInstanceOf(c, "javax.swing.JPasswordField")) {
            if (!((JPasswordField)c).customSetUIProperty(propertyName, propertyValue)) {
                c.setUIProperty(propertyName, propertyValue);
            }
        } else {
            c.setUIProperty(propertyName, propertyValue);
        }
    }

    public static JTextComponent.KeyBinding[] makeKeyBindings(Object[] keyBindingList) {
        JTextComponent.KeyBinding[] rv = new JTextComponent.KeyBinding[keyBindingList.length / 2];
        for (int i = 0; i < rv.length; ++i) {
            Object o = keyBindingList[2 * i];
            KeyStroke keystroke = o instanceof KeyStroke ? (KeyStroke)o : KeyStroke.getKeyStroke((String)o);
            String action = (String)keyBindingList[2 * i + 1];
            rv[i] = new JTextComponent.KeyBinding(keystroke, action);
        }
        return rv;
    }

    public static InputMap makeInputMap(Object[] keys) {
        InputMapUIResource retMap = new InputMapUIResource();
        LookAndFeel.loadKeyBindings(retMap, keys);
        return retMap;
    }

    public static ComponentInputMap makeComponentInputMap(JComponent c, Object[] keys) {
        ComponentInputMapUIResource retMap = new ComponentInputMapUIResource(c);
        LookAndFeel.loadKeyBindings(retMap, keys);
        return retMap;
    }

    public static void loadKeyBindings(InputMap retMap, Object[] keys) {
        if (keys != null) {
            int maxCounter = keys.length;
            for (int counter = 0; counter < maxCounter; ++counter) {
                Object keyStrokeO;
                KeyStroke ks = (keyStrokeO = keys[counter++]) instanceof KeyStroke ? (KeyStroke)keyStrokeO : KeyStroke.getKeyStroke((String)keyStrokeO);
                retMap.put(ks, keys[counter]);
            }
        }
    }

    public static Object makeIcon(Class<?> baseClass, String gifFile) {
        return SwingUtilities2.makeIcon_Unprivileged(baseClass, baseClass, gifFile);
    }

    public LayoutStyle getLayoutStyle() {
        return DefaultLayoutStyle.getInstance();
    }

    public void provideErrorFeedback(Component component) {
        Toolkit toolkit = null;
        toolkit = component != null ? component.getToolkit() : Toolkit.getDefaultToolkit();
        toolkit.beep();
    }

    public static Object getDesktopPropertyValue(String systemPropertyName, Object fallbackValue) {
        Object value = Toolkit.getDefaultToolkit().getDesktopProperty(systemPropertyName);
        if (value == null) {
            return fallbackValue;
        }
        if (value instanceof Color) {
            return new ColorUIResource((Color)value);
        }
        if (value instanceof Font) {
            return new FontUIResource((Font)value);
        }
        return value;
    }

    public Icon getDisabledIcon(JComponent component, Icon icon) {
        if (icon instanceof ImageIcon) {
            return new ImageIconUIResource(GrayFilter.createDisabledImage(((ImageIcon)icon).getImage()));
        }
        return null;
    }

    public Icon getDisabledSelectedIcon(JComponent component, Icon icon) {
        return this.getDisabledIcon(component, icon);
    }

    public abstract String getName();

    public abstract String getID();

    public abstract String getDescription();

    public boolean getSupportsWindowDecorations() {
        return false;
    }

    public abstract boolean isNativeLookAndFeel();

    public abstract boolean isSupportedLookAndFeel();

    public void initialize() {
    }

    public void uninitialize() {
    }

    public UIDefaults getDefaults() {
        return null;
    }

    public String toString() {
        return "[" + this.getDescription() + " - " + this.getClass().getName() + "]";
    }
}

