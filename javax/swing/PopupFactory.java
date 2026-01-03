/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ClientPropertyKey;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.MenuElement;
import javax.swing.Popup;
import javax.swing.RootPaneContainer;
import javax.swing.SwingHeavyWeight;
import javax.swing.SwingUtilities;
import sun.awt.EmbeddedFrame;
import sun.awt.OSInfo;
import sun.swing.SwingAccessor;

public class PopupFactory {
    private static final Object SharedInstanceKey;
    private static final int MAX_CACHE_SIZE = 5;
    static final int LIGHT_WEIGHT_POPUP = 0;
    static final int MEDIUM_WEIGHT_POPUP = 1;
    static final int HEAVY_WEIGHT_POPUP = 2;
    private int popupType = 0;

    public static void setSharedInstance(PopupFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("PopupFactory can not be null");
        }
        SwingUtilities.appContextPut(SharedInstanceKey, factory);
    }

    public static PopupFactory getSharedInstance() {
        PopupFactory factory = (PopupFactory)SwingUtilities.appContextGet(SharedInstanceKey);
        if (factory == null) {
            factory = new PopupFactory();
            PopupFactory.setSharedInstance(factory);
        }
        return factory;
    }

    void setPopupType(int type) {
        this.popupType = type;
    }

    int getPopupType() {
        return this.popupType;
    }

    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        return this.getPopup(owner, contents, x, y, false);
    }

    protected Popup getPopup(Component owner, Component contents, int x, int y, boolean isHeavyWeightPopup) throws IllegalArgumentException {
        if (contents == null) {
            throw new IllegalArgumentException("Popup.getPopup must be passed non-null contents");
        }
        if (isHeavyWeightPopup) {
            return this.getPopup(owner, contents, x, y, 2);
        }
        int popupType = this.getPopupType(owner, contents, x, y);
        Popup popup = this.getPopup(owner, contents, x, y, popupType);
        if (popup == null) {
            popup = this.getPopup(owner, contents, x, y, 2);
        }
        return popup;
    }

    private int getPopupType(Component owner, Component contents, int ownerX, int ownerY) {
        int popupType = this.getPopupType();
        if (owner == null || this.invokerInHeavyWeightPopup(owner)) {
            popupType = 2;
        } else if (popupType == 0 && !(contents instanceof JToolTip) && !(contents instanceof JPopupMenu)) {
            popupType = 1;
        }
        for (Component c = owner; c != null; c = c.getParent()) {
            if (!(c instanceof JComponent) || ((JComponent)c).getClientProperty((Object)ClientPropertyKey.PopupFactory_FORCE_HEAVYWEIGHT_POPUP) != Boolean.TRUE) continue;
            popupType = 2;
            break;
        }
        return popupType;
    }

    private Popup getPopup(Component owner, Component contents, int ownerX, int ownerY, int popupType) {
        if (GraphicsEnvironment.isHeadless()) {
            return this.getHeadlessPopup(owner, contents, ownerX, ownerY);
        }
        switch (popupType) {
            case 0: {
                return this.getLightWeightPopup(owner, contents, ownerX, ownerY);
            }
            case 1: {
                return this.getMediumWeightPopup(owner, contents, ownerX, ownerY);
            }
            case 2: {
                Popup popup = this.getHeavyWeightPopup(owner, contents, ownerX, ownerY);
                if (OSInfo.getOSType() == OSInfo.OSType.MACOSX && owner != null && EmbeddedFrame.getAppletIfAncestorOf(owner) != null) {
                    ((HeavyWeightPopup)popup).setCacheEnabled(false);
                }
                return popup;
            }
        }
        return null;
    }

    private Popup getHeadlessPopup(Component owner, Component contents, int ownerX, int ownerY) {
        return HeadlessPopup.getHeadlessPopup(owner, contents, ownerX, ownerY);
    }

    private Popup getLightWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
        return LightWeightPopup.getLightWeightPopup(owner, contents, ownerX, ownerY);
    }

    private Popup getMediumWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
        return MediumWeightPopup.getMediumWeightPopup(owner, contents, ownerX, ownerY);
    }

    private Popup getHeavyWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
        if (GraphicsEnvironment.isHeadless()) {
            return this.getMediumWeightPopup(owner, contents, ownerX, ownerY);
        }
        return HeavyWeightPopup.getHeavyWeightPopup(owner, contents, ownerX, ownerY);
    }

    private boolean invokerInHeavyWeightPopup(Component i) {
        if (i != null) {
            for (Container parent = i.getParent(); parent != null; parent = parent.getParent()) {
                if (!(parent instanceof Popup.HeavyWeightWindow)) continue;
                return true;
            }
        }
        return false;
    }

    static {
        SwingAccessor.setPopupFactoryAccessor(new SwingAccessor.PopupFactoryAccessor(){

            @Override
            public Popup getHeavyWeightPopup(PopupFactory factory, Component owner, Component contents, int ownerX, int ownerY) {
                return factory.getPopup(owner, contents, ownerX, ownerY, 2);
            }
        });
        SharedInstanceKey = new StringBuffer("PopupFactory.SharedInstanceKey");
    }

    private static class HeavyWeightPopup
    extends Popup {
        private static final Object heavyWeightPopupCacheKey = new StringBuffer("PopupFactory.heavyWeightPopupCache");
        private volatile boolean isCacheEnabled = true;

        private HeavyWeightPopup() {
        }

        static Popup getHeavyWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
            Window window = owner != null ? SwingUtilities.getWindowAncestor(owner) : null;
            HeavyWeightPopup popup = null;
            if (window != null) {
                popup = HeavyWeightPopup.getRecycledHeavyWeightPopup(window);
            }
            boolean focusPopup = false;
            if (contents != null && contents.isFocusable() && contents instanceof JPopupMenu) {
                Component[] popComps;
                JPopupMenu jpm = (JPopupMenu)contents;
                for (Component popComp : popComps = jpm.getComponents()) {
                    if (popComp instanceof MenuElement || popComp instanceof JSeparator) continue;
                    focusPopup = true;
                    break;
                }
            }
            if (popup == null || ((JWindow)popup.getComponent()).getFocusableWindowState() != focusPopup) {
                if (popup != null) {
                    popup._dispose();
                }
                popup = new HeavyWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if (focusPopup) {
                JWindow wnd = (JWindow)popup.getComponent();
                wnd.setFocusableWindowState(true);
                wnd.setName("###focusableSwingPopup###");
            }
            return popup;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static HeavyWeightPopup getRecycledHeavyWeightPopup(Window w) {
            Class<HeavyWeightPopup> clazz = HeavyWeightPopup.class;
            synchronized (HeavyWeightPopup.class) {
                Map<Window, List<HeavyWeightPopup>> heavyPopupCache = HeavyWeightPopup.getHeavyWeightPopupCache();
                if (!heavyPopupCache.containsKey(w)) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return null;
                }
                List<HeavyWeightPopup> cache = heavyPopupCache.get(w);
                if (cache.size() > 0) {
                    HeavyWeightPopup r = cache.get(0);
                    cache.remove(0);
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return r;
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static Map<Window, List<HeavyWeightPopup>> getHeavyWeightPopupCache() {
            Class<HeavyWeightPopup> clazz = HeavyWeightPopup.class;
            synchronized (HeavyWeightPopup.class) {
                HashMap cache = (HashMap)SwingUtilities.appContextGet(heavyWeightPopupCacheKey);
                if (cache == null) {
                    cache = new HashMap(2);
                    SwingUtilities.appContextPut(heavyWeightPopupCacheKey, cache);
                }
                // ** MonitorExit[var0] (shouldn't be in output)
                return cache;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void recycleHeavyWeightPopup(HeavyWeightPopup popup) {
            Class<HeavyWeightPopup> clazz = HeavyWeightPopup.class;
            synchronized (HeavyWeightPopup.class) {
                List<Object> cache;
                Window window = SwingUtilities.getWindowAncestor(popup.getComponent());
                Map<Window, List<HeavyWeightPopup>> heavyPopupCache = HeavyWeightPopup.getHeavyWeightPopupCache();
                if (window instanceof Popup.DefaultFrame || !window.isVisible()) {
                    popup._dispose();
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return;
                }
                if (heavyPopupCache.containsKey(window)) {
                    cache = heavyPopupCache.get(window);
                } else {
                    cache = new ArrayList();
                    heavyPopupCache.put(window, cache);
                    final Window w = window;
                    w.addWindowListener(new WindowAdapter(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void windowClosed(WindowEvent e) {
                            Class<HeavyWeightPopup> clazz = HeavyWeightPopup.class;
                            synchronized (HeavyWeightPopup.class) {
                                Map<Window, List<HeavyWeightPopup>> heavyPopupCache2 = HeavyWeightPopup.getHeavyWeightPopupCache();
                                List<HeavyWeightPopup> popups = heavyPopupCache2.remove(w);
                                // ** MonitorExit[var3_2] (shouldn't be in output)
                                if (popups != null) {
                                    for (int counter = popups.size() - 1; counter >= 0; --counter) {
                                        popups.get(counter)._dispose();
                                    }
                                }
                                return;
                            }
                        }
                    });
                }
                if (cache.size() < 5) {
                    cache.add(popup);
                } else {
                    popup._dispose();
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        void setCacheEnabled(boolean enable) {
            this.isCacheEnabled = enable;
        }

        @Override
        public void hide() {
            super.hide();
            if (this.isCacheEnabled) {
                HeavyWeightPopup.recycleHeavyWeightPopup(this);
            } else {
                this._dispose();
            }
        }

        @Override
        void dispose() {
        }

        void _dispose() {
            super.dispose();
        }
    }

    private static class HeadlessPopup
    extends ContainerPopup {
        private HeadlessPopup() {
        }

        static Popup getHeadlessPopup(Component owner, Component contents, int ownerX, int ownerY) {
            HeadlessPopup popup = new HeadlessPopup();
            popup.reset(owner, contents, ownerX, ownerY);
            return popup;
        }

        @Override
        Component createComponent(Component owner) {
            return new Panel(new BorderLayout());
        }

        @Override
        public void show() {
        }

        @Override
        public void hide() {
        }
    }

    private static class LightWeightPopup
    extends ContainerPopup {
        private static final Object lightWeightPopupCacheKey = new StringBuffer("PopupFactory.lightPopupCache");

        private LightWeightPopup() {
        }

        static Popup getLightWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
            LightWeightPopup popup = LightWeightPopup.getRecycledLightWeightPopup();
            if (popup == null) {
                popup = new LightWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if (!popup.fitsOnScreen() || popup.overlappedByOwnedWindow()) {
                popup.hide();
                return null;
            }
            return popup;
        }

        private static List<LightWeightPopup> getLightWeightPopupCache() {
            ArrayList cache = (ArrayList)SwingUtilities.appContextGet(lightWeightPopupCacheKey);
            if (cache == null) {
                cache = new ArrayList();
                SwingUtilities.appContextPut(lightWeightPopupCacheKey, cache);
            }
            return cache;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void recycleLightWeightPopup(LightWeightPopup popup) {
            Class<LightWeightPopup> clazz = LightWeightPopup.class;
            synchronized (LightWeightPopup.class) {
                List<LightWeightPopup> lightPopupCache = LightWeightPopup.getLightWeightPopupCache();
                if (lightPopupCache.size() < 5) {
                    lightPopupCache.add(popup);
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static LightWeightPopup getRecycledLightWeightPopup() {
            Class<LightWeightPopup> clazz = LightWeightPopup.class;
            synchronized (LightWeightPopup.class) {
                List<LightWeightPopup> lightPopupCache = LightWeightPopup.getLightWeightPopupCache();
                if (lightPopupCache.size() > 0) {
                    LightWeightPopup r = lightPopupCache.get(0);
                    lightPopupCache.remove(0);
                    // ** MonitorExit[var0] (shouldn't be in output)
                    return r;
                }
                // ** MonitorExit[var0] (shouldn't be in output)
                return null;
            }
        }

        @Override
        public void hide() {
            super.hide();
            Container component = (Container)this.getComponent();
            component.removeAll();
            LightWeightPopup.recycleLightWeightPopup(this);
        }

        @Override
        public void show() {
            Serializable p;
            Serializable parent = null;
            if (this.owner != null) {
                parent = this.owner instanceof Container ? (Container)this.owner : this.owner.getParent();
            }
            for (p = parent; p != null; p = ((Component)p).getParent()) {
                if (p instanceof JRootPane) {
                    if (((Component)p).getParent() instanceof JInternalFrame) continue;
                    parent = ((JRootPane)p).getLayeredPane();
                    continue;
                }
                if (p instanceof Window) {
                    if (parent != null) break;
                    parent = p;
                    break;
                }
                if (p instanceof JApplet) break;
            }
            p = SwingUtilities.convertScreenLocationToParent((Container)parent, this.x, this.y);
            Component component = this.getComponent();
            component.setLocation(((Point)p).x, ((Point)p).y);
            if (parent instanceof JLayeredPane) {
                ((Container)parent).add(component, JLayeredPane.POPUP_LAYER, 0);
            } else {
                ((Container)parent).add(component);
            }
            this.pack();
            component.setVisible(true);
        }

        @Override
        Component createComponent(Component owner) {
            return new JPanel(new BorderLayout(), true);
        }

        @Override
        void reset(Component owner, Component contents, int ownerX, int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);
            JComponent component = (JComponent)this.getComponent();
            component.setVisible(false);
            component.setLocation(ownerX, ownerY);
            component.setOpaque(contents.isOpaque());
            component.add(contents, "Center");
            this.pack();
        }
    }

    private static class MediumWeightPopup
    extends ContainerPopup {
        private static final Object mediumWeightPopupCacheKey = new StringBuffer("PopupFactory.mediumPopupCache");
        private JRootPane rootPane;

        private MediumWeightPopup() {
        }

        static Popup getMediumWeightPopup(Component owner, Component contents, int ownerX, int ownerY) {
            MediumWeightPopup popup = MediumWeightPopup.getRecycledMediumWeightPopup();
            if (popup == null) {
                popup = new MediumWeightPopup();
            }
            popup.reset(owner, contents, ownerX, ownerY);
            if (!popup.fitsOnScreen() || popup.overlappedByOwnedWindow()) {
                popup.hide();
                return null;
            }
            return popup;
        }

        private static List<MediumWeightPopup> getMediumWeightPopupCache() {
            ArrayList cache = (ArrayList)SwingUtilities.appContextGet(mediumWeightPopupCacheKey);
            if (cache == null) {
                cache = new ArrayList();
                SwingUtilities.appContextPut(mediumWeightPopupCacheKey, cache);
            }
            return cache;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void recycleMediumWeightPopup(MediumWeightPopup popup) {
            Class<MediumWeightPopup> clazz = MediumWeightPopup.class;
            synchronized (MediumWeightPopup.class) {
                List<MediumWeightPopup> mediumPopupCache = MediumWeightPopup.getMediumWeightPopupCache();
                if (mediumPopupCache.size() < 5) {
                    mediumPopupCache.add(popup);
                }
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static MediumWeightPopup getRecycledMediumWeightPopup() {
            Class<MediumWeightPopup> clazz = MediumWeightPopup.class;
            synchronized (MediumWeightPopup.class) {
                List<MediumWeightPopup> mediumPopupCache = MediumWeightPopup.getMediumWeightPopupCache();
                if (mediumPopupCache.size() > 0) {
                    MediumWeightPopup r = mediumPopupCache.get(0);
                    mediumPopupCache.remove(0);
                    // ** MonitorExit[var0] (shouldn't be in output)
                    return r;
                }
                // ** MonitorExit[var0] (shouldn't be in output)
                return null;
            }
        }

        @Override
        public void hide() {
            super.hide();
            this.rootPane.getContentPane().removeAll();
            MediumWeightPopup.recycleMediumWeightPopup(this);
        }

        @Override
        public void show() {
            Component component = this.getComponent();
            Container parent = null;
            if (this.owner != null) {
                parent = this.owner.getParent();
            }
            while (!(parent instanceof Window) && !(parent instanceof Applet) && parent != null) {
                parent = parent.getParent();
            }
            if (parent instanceof RootPaneContainer) {
                parent = ((RootPaneContainer)((Object)parent)).getLayeredPane();
            }
            Point p = SwingUtilities.convertScreenLocationToParent(parent, this.x, this.y);
            component.setLocation(p.x, p.y);
            if (parent instanceof JLayeredPane) {
                parent.add(component, JLayeredPane.POPUP_LAYER, 0);
            } else {
                parent.add(component);
            }
            this.pack();
            component.setVisible(true);
            component.revalidate();
        }

        @Override
        Component createComponent(Component owner) {
            MediumWeightComponent component = new MediumWeightComponent();
            this.rootPane = new JRootPane();
            this.rootPane.setOpaque(true);
            component.add((Component)this.rootPane, "Center");
            return component;
        }

        @Override
        void reset(Component owner, Component contents, int ownerX, int ownerY) {
            super.reset(owner, contents, ownerX, ownerY);
            Component component = this.getComponent();
            component.setVisible(false);
            component.setLocation(ownerX, ownerY);
            this.rootPane.getContentPane().add(contents, "Center");
            this.pack();
        }

        private static class MediumWeightComponent
        extends Panel
        implements SwingHeavyWeight {
            MediumWeightComponent() {
                super(new BorderLayout());
            }
        }
    }

    private static class ContainerPopup
    extends Popup {
        Component owner;
        int x;
        int y;

        private ContainerPopup() {
        }

        @Override
        public void hide() {
            Container parent;
            Component component = this.getComponent();
            if (component != null && (parent = component.getParent()) != null) {
                Rectangle bounds = component.getBounds();
                parent.remove(component);
                parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            this.owner = null;
        }

        @Override
        public void pack() {
            Component component = this.getComponent();
            if (component != null) {
                component.setSize(component.getPreferredSize());
            }
        }

        @Override
        void reset(Component owner, Component contents, int ownerX, int ownerY) {
            if (owner instanceof JFrame || owner instanceof JDialog || owner instanceof JWindow) {
                owner = ((RootPaneContainer)((Object)owner)).getLayeredPane();
            }
            super.reset(owner, contents, ownerX, ownerY);
            this.x = ownerX;
            this.y = ownerY;
            this.owner = owner;
        }

        boolean overlappedByOwnedWindow() {
            Component component = this.getComponent();
            if (this.owner != null && component != null) {
                Window w = SwingUtilities.getWindowAncestor(this.owner);
                if (w == null) {
                    return false;
                }
                Window[] ownedWindows = w.getOwnedWindows();
                if (ownedWindows != null) {
                    Rectangle bnd = component.getBounds();
                    for (Window window : ownedWindows) {
                        if (!window.isVisible() || !bnd.intersects(window.getBounds())) continue;
                        return true;
                    }
                }
            }
            return false;
        }

        boolean fitsOnScreen() {
            boolean result = false;
            Component component = this.getComponent();
            if (this.owner != null && component != null) {
                int popupWidth = component.getWidth();
                int popupHeight = component.getHeight();
                Container parent = (Container)SwingUtilities.getRoot(this.owner);
                if (parent instanceof JFrame || parent instanceof JDialog || parent instanceof JWindow) {
                    Rectangle parentBounds = parent.getBounds();
                    Insets i = parent.getInsets();
                    parentBounds.x += i.left;
                    parentBounds.y += i.top;
                    parentBounds.width -= i.left + i.right;
                    parentBounds.height -= i.top + i.bottom;
                    if (JPopupMenu.canPopupOverlapTaskBar()) {
                        GraphicsConfiguration gc = parent.getGraphicsConfiguration();
                        Rectangle popupArea = this.getContainerPopupArea(gc);
                        result = parentBounds.intersection(popupArea).contains(this.x, this.y, popupWidth, popupHeight);
                    } else {
                        result = parentBounds.contains(this.x, this.y, popupWidth, popupHeight);
                    }
                } else if (parent instanceof JApplet) {
                    Rectangle parentBounds = parent.getBounds();
                    Point p = parent.getLocationOnScreen();
                    parentBounds.x = p.x;
                    parentBounds.y = p.y;
                    result = parentBounds.contains(this.x, this.y, popupWidth, popupHeight);
                }
            }
            return result;
        }

        Rectangle getContainerPopupArea(GraphicsConfiguration gc) {
            Insets insets;
            Rectangle screenBounds;
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (gc != null) {
                screenBounds = gc.getBounds();
                insets = toolkit.getScreenInsets(gc);
            } else {
                screenBounds = new Rectangle(toolkit.getScreenSize());
                insets = new Insets(0, 0, 0, 0);
            }
            screenBounds.x += insets.left;
            screenBounds.y += insets.top;
            screenBounds.width -= insets.left + insets.right;
            screenBounds.height -= insets.top + insets.bottom;
            return screenBounds;
        }
    }
}

