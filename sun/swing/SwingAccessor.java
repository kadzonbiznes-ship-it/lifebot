/*
 * Decompiled with CFR 0.152.
 */
package sun.swing;

import java.awt.Component;
import java.awt.Point;
import java.lang.invoke.MethodHandles;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RepaintManager;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.text.JTextComponent;
import sun.swing.JLightweightFrame;
import sun.swing.SwingUtilities2;

public final class SwingAccessor {
    private static JComponentAccessor jComponentAccessor;
    private static JTextComponentAccessor jtextComponentAccessor;
    private static JLightweightFrameAccessor jLightweightFrameAccessor;
    private static UIDefaultsAccessor uiDefaultsAccessor;
    private static RepaintManagerAccessor repaintManagerAccessor;
    private static PopupFactoryAccessor popupFactoryAccessor;
    private static KeyStrokeAccessor keyStrokeAccessor;
    private static AccessibleComponentAccessor accessibleComponentAccessor;
    private static ThreadLocal<Boolean> tlObj;

    private SwingAccessor() {
    }

    public static void setJComponentAccessor(JComponentAccessor jCompAccessor) {
        jComponentAccessor = jCompAccessor;
    }

    public static JComponentAccessor getJComponentAccessor() {
        JComponentAccessor access = jComponentAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(JComponent.class);
            access = jComponentAccessor;
        }
        return access;
    }

    public static void setJTextComponentAccessor(JTextComponentAccessor jtca) {
        jtextComponentAccessor = jtca;
    }

    public static JTextComponentAccessor getJTextComponentAccessor() {
        JTextComponentAccessor access = jtextComponentAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(JTextComponent.class);
            access = jtextComponentAccessor;
        }
        return access;
    }

    public static void setJLightweightFrameAccessor(JLightweightFrameAccessor accessor) {
        jLightweightFrameAccessor = accessor;
    }

    public static JLightweightFrameAccessor getJLightweightFrameAccessor() {
        JLightweightFrameAccessor access = jLightweightFrameAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(JLightweightFrame.class);
            access = jLightweightFrameAccessor;
        }
        return access;
    }

    public static void setUIDefaultsAccessor(UIDefaultsAccessor accessor) {
        uiDefaultsAccessor = accessor;
    }

    public static UIDefaultsAccessor getUIDefaultsAccessor() {
        UIDefaultsAccessor access = uiDefaultsAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(UIDefaults.class);
            access = uiDefaultsAccessor;
        }
        return access;
    }

    public static void setRepaintManagerAccessor(RepaintManagerAccessor accessor) {
        repaintManagerAccessor = accessor;
    }

    public static RepaintManagerAccessor getRepaintManagerAccessor() {
        RepaintManagerAccessor access = repaintManagerAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(RepaintManager.class);
            access = repaintManagerAccessor;
        }
        return access;
    }

    public static PopupFactoryAccessor getPopupFactoryAccessor() {
        PopupFactoryAccessor access = popupFactoryAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(PopupFactory.class);
            access = popupFactoryAccessor;
        }
        return access;
    }

    public static void setPopupFactoryAccessor(PopupFactoryAccessor popupFactoryAccessor) {
        SwingAccessor.popupFactoryAccessor = popupFactoryAccessor;
    }

    public static KeyStrokeAccessor getKeyStrokeAccessor() {
        KeyStrokeAccessor access = keyStrokeAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(KeyStroke.class);
            access = keyStrokeAccessor;
        }
        return access;
    }

    public static void setKeyStrokeAccessor(KeyStrokeAccessor accessor) {
        keyStrokeAccessor = accessor;
    }

    public static AccessibleComponentAccessor getAccessibleComponentAccessor() {
        AccessibleComponentAccessor access = accessibleComponentAccessor;
        if (access == null) {
            SwingAccessor.ensureClassInitialized(JTree.class);
            access = accessibleComponentAccessor;
        }
        return access;
    }

    public static void setAccessibleComponentAccessor(AccessibleComponentAccessor accessibleAccessor) {
        accessibleComponentAccessor = accessibleAccessor;
    }

    private static void ensureClassInitialized(Class<?> c) {
        try {
            MethodHandles.lookup().ensureInitialized(c);
        }
        catch (IllegalAccessException illegalAccessException) {
            // empty catch block
        }
    }

    public static Boolean getAllowHTMLObject() {
        Boolean b = tlObj.get();
        if (b == null) {
            return Boolean.TRUE;
        }
        return b;
    }

    public static void setAllowHTMLObject(Boolean val) {
        tlObj.set(val);
    }

    static {
        accessibleComponentAccessor = null;
        tlObj = new ThreadLocal();
    }

    public static interface JComponentAccessor {
        public boolean getFlag(JComponent var1, int var2);

        public void compWriteObjectNotify(JComponent var1);
    }

    public static interface JTextComponentAccessor {
        public TransferHandler.DropLocation dropLocationForPoint(JTextComponent var1, Point var2);

        public Object setDropLocation(JTextComponent var1, TransferHandler.DropLocation var2, Object var3, boolean var4);
    }

    public static interface JLightweightFrameAccessor {
        public void updateCursor(JLightweightFrame var1);
    }

    public static interface UIDefaultsAccessor {
        public void addInternalBundle(UIDefaults var1, String var2);
    }

    public static interface RepaintManagerAccessor {
        public void addRepaintListener(RepaintManager var1, SwingUtilities2.RepaintListener var2);

        public void removeRepaintListener(RepaintManager var1, SwingUtilities2.RepaintListener var2);
    }

    public static interface PopupFactoryAccessor {
        public Popup getHeavyWeightPopup(PopupFactory var1, Component var2, Component var3, int var4, int var5);
    }

    public static interface KeyStrokeAccessor {
        public KeyStroke create();
    }

    public static interface AccessibleComponentAccessor {
        public Accessible getCurrentAccessible(AccessibleContext var1);
    }
}

