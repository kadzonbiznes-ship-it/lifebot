/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Objects;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

public final class ToolTipManager
extends MouseAdapter
implements MouseMotionListener {
    Timer enterTimer = new Timer(750, new insideTimerAction());
    Timer exitTimer;
    Timer insideTimer;
    String toolTipText;
    Point preferredLocation;
    JComponent insideComponent;
    MouseEvent mouseEvent;
    boolean showImmediately;
    private static final Object TOOL_TIP_MANAGER_KEY = new Object();
    transient Popup tipWindow;
    private Window window;
    JToolTip tip;
    private Rectangle popupRect = null;
    private Rectangle popupFrameRect = null;
    boolean enabled = true;
    private boolean tipShowing = false;
    private FocusListener focusChangeListener = null;
    private MouseMotionListener moveBeforeEnterListener = null;
    private KeyListener accessibilityKeyListener = null;
    private KeyStroke postTip;
    private KeyStroke hideTip;
    protected boolean lightWeightPopupEnabled = true;
    protected boolean heavyWeightPopupEnabled = false;

    ToolTipManager() {
        this.enterTimer.setRepeats(false);
        this.exitTimer = new Timer(500, new outsideTimerAction());
        this.exitTimer.setRepeats(false);
        this.insideTimer = new Timer(4000, new stillInsideTimerAction());
        this.insideTimer.setRepeats(false);
        this.moveBeforeEnterListener = new MoveBeforeEnterListener();
        this.accessibilityKeyListener = new AccessibilityKeyListener();
        this.postTip = KeyStroke.getKeyStroke(112, 2);
        this.hideTip = KeyStroke.getKeyStroke(27, 0);
    }

    public void setEnabled(boolean flag) {
        this.enabled = flag;
        if (!flag) {
            this.hideTipWindow();
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setLightWeightPopupEnabled(boolean aFlag) {
        this.lightWeightPopupEnabled = aFlag;
    }

    public boolean isLightWeightPopupEnabled() {
        return this.lightWeightPopupEnabled;
    }

    public void setInitialDelay(int milliseconds) {
        this.enterTimer.setInitialDelay(milliseconds);
    }

    public int getInitialDelay() {
        return this.enterTimer.getInitialDelay();
    }

    public void setDismissDelay(int milliseconds) {
        this.insideTimer.setInitialDelay(milliseconds);
    }

    public int getDismissDelay() {
        return this.insideTimer.getInitialDelay();
    }

    public void setReshowDelay(int milliseconds) {
        this.exitTimer.setInitialDelay(milliseconds);
    }

    public int getReshowDelay() {
        return this.exitTimer.getInitialDelay();
    }

    private GraphicsConfiguration getDrawingGC(Point toFind) {
        GraphicsDevice[] devices;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : devices = env.getScreenDevices()) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle rect = config.getBounds();
            if (!rect.contains(toFind)) continue;
            return config;
        }
        return null;
    }

    void showTipWindow() {
        KeyboardFocusManager kfm;
        if (this.insideComponent == null || !this.insideComponent.isShowing()) {
            return;
        }
        String mode = UIManager.getString("ToolTipManager.enableToolTipMode");
        if ("activeApplication".equals(mode) && (kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager()).getFocusedWindow() == null) {
            return;
        }
        if (this.enabled) {
            Point location;
            Point screenLocation = this.insideComponent.getLocationOnScreen();
            Point toFind = this.preferredLocation != null ? new Point(screenLocation.x + this.preferredLocation.x, screenLocation.y + this.preferredLocation.y) : (this.mouseEvent != null ? this.mouseEvent.getLocationOnScreen() : screenLocation);
            GraphicsConfiguration gc = this.getDrawingGC(toFind);
            if (gc == null) {
                if (this.mouseEvent != null) {
                    toFind = this.mouseEvent.getLocationOnScreen();
                    gc = this.getDrawingGC(toFind);
                }
                if (gc == null) {
                    gc = this.insideComponent.getGraphicsConfiguration();
                }
            }
            Rectangle sBounds = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            sBounds.x += screenInsets.left;
            sBounds.y += screenInsets.top;
            sBounds.width -= screenInsets.left + screenInsets.right;
            sBounds.height -= screenInsets.top + screenInsets.bottom;
            boolean leftToRight = SwingUtilities.isLeftToRight(this.insideComponent);
            this.hideTipWindow();
            this.tip = this.insideComponent.createToolTip();
            this.tip.setTipText(this.toolTipText);
            Dimension size = this.tip.getPreferredSize();
            if (this.preferredLocation != null) {
                location = toFind;
                if (!leftToRight) {
                    location.x -= size.width;
                }
            } else {
                location = this.mouseEvent != null ? new Point(screenLocation.x + this.mouseEvent.getX(), screenLocation.y + this.mouseEvent.getY() + 20) : screenLocation;
                if (!leftToRight && location.x - size.width >= 0) {
                    location.x -= size.width;
                }
            }
            if (this.popupRect == null) {
                this.popupRect = new Rectangle();
            }
            this.popupRect.setBounds(location.x, location.y, size.width, size.height);
            if (location.x < sBounds.x) {
                location.x = sBounds.x;
            } else if (location.x - sBounds.x + size.width > sBounds.width) {
                location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
            }
            if (location.y < sBounds.y) {
                location.y = sBounds.y;
            } else if (location.y - sBounds.y + size.height > sBounds.height) {
                location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
            }
            PopupFactory popupFactory = PopupFactory.getSharedInstance();
            if (this.lightWeightPopupEnabled) {
                int y = this.getPopupFitHeight(this.popupRect, this.insideComponent);
                int x = this.getPopupFitWidth(this.popupRect, this.insideComponent);
                if (x > 0 || y > 0) {
                    popupFactory.setPopupType(1);
                } else {
                    popupFactory.setPopupType(0);
                }
            } else {
                popupFactory.setPopupType(1);
            }
            this.tipWindow = popupFactory.getPopup(this.insideComponent, this.tip, location.x, location.y);
            popupFactory.setPopupType(0);
            this.tipWindow.show();
            Window componentWindow = SwingUtilities.windowForComponent(this.insideComponent);
            this.window = SwingUtilities.windowForComponent(this.tip);
            if (this.window != null && this.window != componentWindow) {
                this.window.addMouseListener(this);
            } else {
                this.window = null;
            }
            this.insideTimer.start();
            this.tipShowing = true;
        }
    }

    void hideTipWindow() {
        if (this.tipWindow != null) {
            if (this.window != null) {
                this.window.removeMouseListener(this);
                this.window = null;
            }
            this.tipWindow.hide();
            this.tipWindow = null;
            this.tipShowing = false;
            this.tip = null;
            this.insideTimer.stop();
        }
    }

    public static ToolTipManager sharedInstance() {
        Object value = SwingUtilities.appContextGet(TOOL_TIP_MANAGER_KEY);
        if (value instanceof ToolTipManager) {
            return (ToolTipManager)value;
        }
        ToolTipManager manager = new ToolTipManager();
        SwingUtilities.appContextPut(TOOL_TIP_MANAGER_KEY, manager);
        return manager;
    }

    public void registerComponent(JComponent component) {
        component.removeMouseListener(this);
        component.addMouseListener(this);
        component.removeMouseMotionListener(this.moveBeforeEnterListener);
        component.addMouseMotionListener(this.moveBeforeEnterListener);
        if (component instanceof JMenuItem) {
            ((JMenuItem)component).removeMenuKeyListener((MenuKeyListener)((Object)this.accessibilityKeyListener));
            ((JMenuItem)component).addMenuKeyListener((MenuKeyListener)((Object)this.accessibilityKeyListener));
        } else {
            component.removeKeyListener(this.accessibilityKeyListener);
            component.addKeyListener(this.accessibilityKeyListener);
        }
    }

    public void unregisterComponent(JComponent component) {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this.moveBeforeEnterListener);
        if (component instanceof JMenuItem) {
            ((JMenuItem)component).removeMenuKeyListener((MenuKeyListener)((Object)this.accessibilityKeyListener));
        } else {
            component.removeKeyListener(this.accessibilityKeyListener);
        }
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        this.initiateToolTip(event);
    }

    private void initiateToolTip(MouseEvent event) {
        if (event.getSource() == this.window) {
            return;
        }
        JComponent component = (JComponent)event.getSource();
        component.removeMouseMotionListener(this.moveBeforeEnterListener);
        this.exitTimer.stop();
        Point location = event.getPoint();
        if (location.x < 0 || location.x >= component.getWidth() || location.y < 0 || location.y >= component.getHeight()) {
            return;
        }
        if (this.insideComponent != null) {
            this.enterTimer.stop();
        }
        component.removeMouseMotionListener(this);
        component.addMouseMotionListener(this);
        boolean sameComponent = this.insideComponent == component;
        this.insideComponent = component;
        if (this.tipWindow != null) {
            this.mouseEvent = event;
            if (this.showImmediately) {
                boolean sameLoc;
                String newToolTipText = component.getToolTipText(event);
                Point newPreferredLocation = component.getToolTipLocation(event);
                boolean bl = this.preferredLocation != null ? this.preferredLocation.equals(newPreferredLocation) : (sameLoc = newPreferredLocation == null);
                if (!(sameComponent && Objects.equals(this.toolTipText, newToolTipText) && sameLoc)) {
                    this.toolTipText = newToolTipText;
                    this.preferredLocation = newPreferredLocation;
                    this.showTipWindow();
                }
            } else {
                this.enterTimer.start();
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent event) {
        Window win;
        boolean shouldHide = true;
        if (this.insideComponent == null) {
            // empty if block
        }
        if (this.window != null && event.getSource() == this.window && this.insideComponent != null) {
            Container insideComponentWindow = this.insideComponent.getTopLevelAncestor();
            if (insideComponentWindow != null) {
                Point location = event.getPoint();
                SwingUtilities.convertPointToScreen(location, this.window);
                location.x -= insideComponentWindow.getX();
                location.y -= insideComponentWindow.getY();
                location = SwingUtilities.convertPoint(null, location, this.insideComponent);
                shouldHide = location.x < 0 || location.x >= this.insideComponent.getWidth() || location.y < 0 || location.y >= this.insideComponent.getHeight();
            }
        } else if (event.getSource() == this.insideComponent && this.tipWindow != null && (win = SwingUtilities.getWindowAncestor(this.insideComponent)) != null) {
            Point location = SwingUtilities.convertPoint(this.insideComponent, event.getPoint(), win);
            Rectangle bounds = this.insideComponent.getTopLevelAncestor().getBounds();
            location.x += bounds.x;
            location.y += bounds.y;
            Point loc = new Point(0, 0);
            SwingUtilities.convertPointToScreen(loc, this.tip);
            bounds.x = loc.x;
            bounds.y = loc.y;
            bounds.width = this.tip.getWidth();
            bounds.height = this.tip.getHeight();
            shouldHide = location.x < bounds.x || location.x >= bounds.x + bounds.width || location.y < bounds.y || location.y >= bounds.y + bounds.height;
        }
        if (shouldHide) {
            this.enterTimer.stop();
            if (this.insideComponent != null) {
                this.insideComponent.removeMouseMotionListener(this);
            }
            this.insideComponent = null;
            this.toolTipText = null;
            this.mouseEvent = null;
            this.hideTipWindow();
            this.exitTimer.restart();
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        this.hideTipWindow();
        this.enterTimer.stop();
        this.showImmediately = false;
        this.insideComponent = null;
        this.mouseEvent = null;
    }

    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        if (this.tipShowing) {
            this.checkForTipChange(event);
        } else if (this.showImmediately) {
            JComponent component = (JComponent)event.getSource();
            this.toolTipText = component.getToolTipText(event);
            if (this.toolTipText != null) {
                this.preferredLocation = component.getToolTipLocation(event);
                this.mouseEvent = event;
                this.insideComponent = component;
                this.exitTimer.stop();
                this.showTipWindow();
            }
        } else {
            this.insideComponent = (JComponent)event.getSource();
            this.mouseEvent = event;
            this.toolTipText = null;
            this.enterTimer.restart();
        }
    }

    private void checkForTipChange(MouseEvent event) {
        JComponent component = (JComponent)event.getSource();
        String newText = component.getToolTipText(event);
        Point newPreferredLocation = component.getToolTipLocation(event);
        if (newText != null || newPreferredLocation != null) {
            this.mouseEvent = event;
            if ((newText != null && newText.equals(this.toolTipText) || newText == null) && (newPreferredLocation != null && newPreferredLocation.equals(this.preferredLocation) || newPreferredLocation == null)) {
                if (this.tipWindow != null) {
                    this.insideTimer.restart();
                } else {
                    this.enterTimer.restart();
                }
            } else {
                this.toolTipText = newText;
                this.preferredLocation = newPreferredLocation;
                if (this.showImmediately) {
                    this.hideTipWindow();
                    this.showTipWindow();
                    this.exitTimer.stop();
                } else {
                    this.enterTimer.restart();
                }
            }
        } else {
            this.toolTipText = null;
            this.preferredLocation = null;
            this.mouseEvent = null;
            this.insideComponent = null;
            this.hideTipWindow();
            this.enterTimer.stop();
            this.exitTimer.restart();
        }
    }

    static Frame frameForComponent(Component component) {
        while (!(component instanceof Frame)) {
            component = component.getParent();
        }
        return (Frame)component;
    }

    private FocusListener createFocusChangeListener() {
        return new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent evt) {
                ToolTipManager.this.hideTipWindow();
                ToolTipManager.this.insideComponent = null;
                JComponent c = (JComponent)evt.getSource();
                c.removeFocusListener(ToolTipManager.this.focusChangeListener);
            }
        };
    }

    private int getPopupFitWidth(Rectangle popupRectInScreen, Component invoker) {
        if (invoker != null) {
            for (Container parent = invoker.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof JFrame || parent instanceof JDialog || parent instanceof JWindow) {
                    return this.getWidthAdjust(parent.getBounds(), popupRectInScreen);
                }
                if (!(parent instanceof JApplet) && !(parent instanceof JInternalFrame)) continue;
                if (this.popupFrameRect == null) {
                    this.popupFrameRect = new Rectangle();
                }
                Point p = parent.getLocationOnScreen();
                this.popupFrameRect.setBounds(p.x, p.y, parent.getBounds().width, parent.getBounds().height);
                return this.getWidthAdjust(this.popupFrameRect, popupRectInScreen);
            }
        }
        return 0;
    }

    private int getPopupFitHeight(Rectangle popupRectInScreen, Component invoker) {
        if (invoker != null) {
            for (Container parent = invoker.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof JFrame || parent instanceof JDialog || parent instanceof JWindow) {
                    return this.getHeightAdjust(parent.getBounds(), popupRectInScreen);
                }
                if (!(parent instanceof JApplet) && !(parent instanceof JInternalFrame)) continue;
                if (this.popupFrameRect == null) {
                    this.popupFrameRect = new Rectangle();
                }
                Point p = parent.getLocationOnScreen();
                this.popupFrameRect.setBounds(p.x, p.y, parent.getBounds().width, parent.getBounds().height);
                return this.getHeightAdjust(this.popupFrameRect, popupRectInScreen);
            }
        }
        return 0;
    }

    private int getHeightAdjust(Rectangle a, Rectangle b) {
        if (b.y >= a.y && b.y + b.height <= a.y + a.height) {
            return 0;
        }
        return b.y + b.height - (a.y + a.height) + 5;
    }

    private int getWidthAdjust(Rectangle a, Rectangle b) {
        if (b.x >= a.x && b.x + b.width <= a.x + a.width) {
            return 0;
        }
        return b.x + b.width - (a.x + a.width) + 5;
    }

    private void show(JComponent source) {
        if (this.tipWindow != null) {
            this.hideTipWindow();
            this.insideComponent = null;
        } else {
            this.hideTipWindow();
            this.enterTimer.stop();
            this.exitTimer.stop();
            this.insideTimer.stop();
            this.insideComponent = source;
            if (this.insideComponent != null) {
                this.toolTipText = this.insideComponent.getToolTipText();
                this.preferredLocation = new Point(10, this.insideComponent.getHeight() + 10);
                this.showTipWindow();
                if (this.focusChangeListener == null) {
                    this.focusChangeListener = this.createFocusChangeListener();
                }
                this.insideComponent.addFocusListener(this.focusChangeListener);
            }
        }
    }

    private void hide(JComponent source) {
        this.hideTipWindow();
        source.removeFocusListener(this.focusChangeListener);
        this.preferredLocation = null;
        this.insideComponent = null;
    }

    protected class insideTimerAction
    implements ActionListener {
        protected insideTimerAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ToolTipManager.this.insideComponent != null && ToolTipManager.this.insideComponent.isShowing()) {
                if (ToolTipManager.this.toolTipText == null && ToolTipManager.this.mouseEvent != null) {
                    ToolTipManager.this.toolTipText = ToolTipManager.this.insideComponent.getToolTipText(ToolTipManager.this.mouseEvent);
                    ToolTipManager.this.preferredLocation = ToolTipManager.this.insideComponent.getToolTipLocation(ToolTipManager.this.mouseEvent);
                }
                if (ToolTipManager.this.toolTipText != null) {
                    ToolTipManager.this.showImmediately = true;
                    ToolTipManager.this.showTipWindow();
                } else {
                    ToolTipManager.this.insideComponent = null;
                    ToolTipManager.this.toolTipText = null;
                    ToolTipManager.this.preferredLocation = null;
                    ToolTipManager.this.mouseEvent = null;
                    ToolTipManager.this.hideTipWindow();
                }
            }
        }
    }

    protected class outsideTimerAction
    implements ActionListener {
        protected outsideTimerAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ToolTipManager.this.showImmediately = false;
        }
    }

    protected class stillInsideTimerAction
    implements ActionListener {
        protected stillInsideTimerAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ToolTipManager.this.hideTipWindow();
            ToolTipManager.this.enterTimer.stop();
            ToolTipManager.this.showImmediately = false;
            ToolTipManager.this.insideComponent = null;
            ToolTipManager.this.mouseEvent = null;
        }
    }

    private class MoveBeforeEnterListener
    extends MouseMotionAdapter {
        private MoveBeforeEnterListener() {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            ToolTipManager.this.initiateToolTip(e);
        }
    }

    private class AccessibilityKeyListener
    extends KeyAdapter
    implements MenuKeyListener {
        private AccessibilityKeyListener() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                JComponent source = (JComponent)e.getComponent();
                KeyStroke keyStrokeForEvent = KeyStroke.getKeyStrokeForEvent(e);
                if (ToolTipManager.this.hideTip.equals(keyStrokeForEvent)) {
                    if (ToolTipManager.this.tipWindow != null) {
                        ToolTipManager.this.hide(source);
                        e.consume();
                    }
                } else if (ToolTipManager.this.postTip.equals(keyStrokeForEvent)) {
                    ToolTipManager.this.show(source);
                    e.consume();
                }
            }
        }

        @Override
        public void menuKeyTyped(MenuKeyEvent e) {
        }

        @Override
        public void menuKeyPressed(MenuKeyEvent e) {
            MenuSelectionManager msm;
            MenuElement[] selectedPath;
            MenuElement selectedElement;
            MenuElement[] path;
            MenuElement element;
            if (ToolTipManager.this.postTip.equals(KeyStroke.getKeyStrokeForEvent(e)) && (element = (path = e.getPath())[path.length - 1]).equals(selectedElement = (selectedPath = (msm = e.getMenuSelectionManager()).getSelectedPath())[selectedPath.length - 1])) {
                JComponent source = (JComponent)element.getComponent();
                ToolTipManager.this.show(source);
                e.consume();
            }
        }

        @Override
        public void menuKeyReleased(MenuKeyEvent e) {
        }
    }
}

