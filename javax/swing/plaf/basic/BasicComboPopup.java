/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import javax.accessibility.AccessibleContext;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.ComboPopup;
import sun.awt.AWTAccessor;

public class BasicComboPopup
extends JPopupMenu
implements ComboPopup {
    static final ListModel<Object> EmptyListModel = new EmptyListModelClass();
    private static Border LIST_BORDER = new LineBorder(Color.BLACK, 1);
    protected JComboBox<Object> comboBox;
    protected JList<Object> list;
    protected JScrollPane scroller;
    protected boolean valueIsAdjusting = false;
    private Handler handler;
    protected MouseMotionListener mouseMotionListener;
    protected MouseListener mouseListener;
    protected KeyListener keyListener;
    protected ListSelectionListener listSelectionListener;
    protected MouseListener listMouseListener;
    protected MouseMotionListener listMouseMotionListener;
    protected PropertyChangeListener propertyChangeListener;
    protected ListDataListener listDataListener;
    protected ItemListener itemListener;
    private MouseWheelListener scrollerMouseWheelListener;
    protected Timer autoscrollTimer;
    protected boolean hasEntered = false;
    protected boolean isAutoScrolling = false;
    protected int scrollDirection = 0;
    protected static final int SCROLL_UP = 0;
    protected static final int SCROLL_DOWN = 1;

    @Override
    public void show() {
        this.comboBox.firePopupMenuWillBecomeVisible();
        this.setListSelection(this.comboBox.getSelectedIndex());
        Point location = this.getPopupLocation();
        this.show(this.comboBox, location.x, location.y);
    }

    @Override
    public void hide() {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        MenuElement[] selection = manager.getSelectedPath();
        for (int i = 0; i < selection.length; ++i) {
            if (selection[i] != this) continue;
            manager.clearSelectedPath();
            break;
        }
        if (selection.length > 0) {
            this.comboBox.repaint();
        }
    }

    @Override
    public JList<Object> getList() {
        return this.list;
    }

    @Override
    public MouseListener getMouseListener() {
        if (this.mouseListener == null) {
            this.mouseListener = this.createMouseListener();
        }
        return this.mouseListener;
    }

    @Override
    public MouseMotionListener getMouseMotionListener() {
        if (this.mouseMotionListener == null) {
            this.mouseMotionListener = this.createMouseMotionListener();
        }
        return this.mouseMotionListener;
    }

    @Override
    public KeyListener getKeyListener() {
        if (this.keyListener == null) {
            this.keyListener = this.createKeyListener();
        }
        return this.keyListener;
    }

    @Override
    public void uninstallingUI() {
        if (this.propertyChangeListener != null) {
            this.comboBox.removePropertyChangeListener(this.propertyChangeListener);
        }
        if (this.itemListener != null) {
            this.comboBox.removeItemListener(this.itemListener);
        }
        this.uninstallComboBoxModelListeners(this.comboBox.getModel());
        this.uninstallKeyboardActions();
        this.uninstallListListeners();
        this.uninstallScrollerListeners();
        this.list.setModel(EmptyListModel);
    }

    protected void uninstallComboBoxModelListeners(ComboBoxModel<?> model) {
        if (model != null && this.listDataListener != null) {
            model.removeListDataListener(this.listDataListener);
        }
    }

    protected void uninstallKeyboardActions() {
    }

    public BasicComboPopup(JComboBox<Object> combo) {
        this.setName("ComboPopup.popup");
        this.comboBox = combo;
        this.setLightWeightPopupEnabled(this.comboBox.isLightWeightPopupEnabled());
        this.list = this.createList();
        this.list.setName("ComboBox.list");
        this.configureList();
        this.scroller = this.createScroller();
        this.scroller.setName("ComboBox.scrollPane");
        this.configureScroller();
        this.configurePopup();
        this.installComboBoxListeners();
        this.installKeyboardActions();
    }

    @Override
    protected void firePopupMenuWillBecomeVisible() {
        if (this.scrollerMouseWheelListener != null) {
            this.comboBox.addMouseWheelListener(this.scrollerMouseWheelListener);
        }
        super.firePopupMenuWillBecomeVisible();
    }

    @Override
    protected void firePopupMenuWillBecomeInvisible() {
        if (this.scrollerMouseWheelListener != null) {
            this.comboBox.removeMouseWheelListener(this.scrollerMouseWheelListener);
        }
        super.firePopupMenuWillBecomeInvisible();
        this.comboBox.firePopupMenuWillBecomeInvisible();
    }

    @Override
    protected void firePopupMenuCanceled() {
        if (this.scrollerMouseWheelListener != null) {
            this.comboBox.removeMouseWheelListener(this.scrollerMouseWheelListener);
        }
        super.firePopupMenuCanceled();
        this.comboBox.firePopupMenuCanceled();
    }

    protected MouseListener createMouseListener() {
        return this.getHandler();
    }

    protected MouseMotionListener createMouseMotionListener() {
        return this.getHandler();
    }

    protected KeyListener createKeyListener() {
        return null;
    }

    protected ListSelectionListener createListSelectionListener() {
        return null;
    }

    protected ListDataListener createListDataListener() {
        return null;
    }

    protected MouseListener createListMouseListener() {
        return this.getHandler();
    }

    protected MouseMotionListener createListMouseMotionListener() {
        return this.getHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    protected ItemListener createItemListener() {
        return this.getHandler();
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected JList<Object> createList() {
        return new JList<Object>(this.comboBox.getModel()){

            @Override
            public void processMouseEvent(MouseEvent e) {
                if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    MouseEvent newEvent = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx() ^ toolkit.getMenuShortcutKeyMaskEx(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), 0);
                    AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                    meAccessor.setCausedByTouchEvent(newEvent, meAccessor.isCausedByTouchEvent(e));
                    e = newEvent;
                }
                super.processMouseEvent(e);
            }
        };
    }

    protected void configureList() {
        this.list.setFont(this.comboBox.getFont());
        this.list.setForeground(this.comboBox.getForeground());
        this.list.setBackground(this.comboBox.getBackground());
        this.list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
        this.list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));
        this.list.setBorder(null);
        this.list.setCellRenderer(this.comboBox.getRenderer());
        this.list.setFocusable(false);
        this.list.setSelectionMode(0);
        this.setListSelection(this.comboBox.getSelectedIndex());
        this.installListListeners();
    }

    protected void installListListeners() {
        this.listMouseListener = this.createListMouseListener();
        if (this.listMouseListener != null) {
            this.list.addMouseListener(this.listMouseListener);
        }
        if ((this.listMouseMotionListener = this.createListMouseMotionListener()) != null) {
            this.list.addMouseMotionListener(this.listMouseMotionListener);
        }
        if ((this.listSelectionListener = this.createListSelectionListener()) != null) {
            this.list.addListSelectionListener(this.listSelectionListener);
        }
    }

    void uninstallListListeners() {
        if (this.listMouseListener != null) {
            this.list.removeMouseListener(this.listMouseListener);
            this.listMouseListener = null;
        }
        if (this.listMouseMotionListener != null) {
            this.list.removeMouseMotionListener(this.listMouseMotionListener);
            this.listMouseMotionListener = null;
        }
        if (this.listSelectionListener != null) {
            this.list.removeListSelectionListener(this.listSelectionListener);
            this.listSelectionListener = null;
        }
        this.handler = null;
    }

    protected JScrollPane createScroller() {
        JScrollPane sp = new JScrollPane(this.list, 20, 31);
        sp.setHorizontalScrollBar(null);
        return sp;
    }

    protected void configureScroller() {
        this.scroller.setFocusable(false);
        this.scroller.getVerticalScrollBar().setFocusable(false);
        this.scroller.setBorder(null);
        this.installScrollerListeners();
    }

    protected void configurePopup() {
        this.setLayout(new BoxLayout(this, 1));
        this.setBorderPainted(true);
        this.setBorder(LIST_BORDER);
        this.setOpaque(false);
        this.add(this.scroller);
        this.setDoubleBuffered(true);
        this.setFocusable(false);
    }

    private void installScrollerListeners() {
        this.scrollerMouseWheelListener = this.getHandler();
        if (this.scrollerMouseWheelListener != null) {
            this.scroller.addMouseWheelListener(this.scrollerMouseWheelListener);
        }
    }

    private void uninstallScrollerListeners() {
        if (this.scrollerMouseWheelListener != null) {
            this.scroller.removeMouseWheelListener(this.scrollerMouseWheelListener);
            this.scrollerMouseWheelListener = null;
        }
    }

    protected void installComboBoxListeners() {
        this.propertyChangeListener = this.createPropertyChangeListener();
        if (this.propertyChangeListener != null) {
            this.comboBox.addPropertyChangeListener(this.propertyChangeListener);
        }
        if ((this.itemListener = this.createItemListener()) != null) {
            this.comboBox.addItemListener(this.itemListener);
        }
        this.installComboBoxModelListeners(this.comboBox.getModel());
    }

    protected void installComboBoxModelListeners(ComboBoxModel<?> model) {
        if (model != null && (this.listDataListener = this.createListDataListener()) != null) {
            model.addListDataListener(this.listDataListener);
        }
    }

    protected void installKeyboardActions() {
    }

    @Override
    public boolean isFocusTraversable() {
        return false;
    }

    protected void startAutoScrolling(int direction) {
        if (this.isAutoScrolling) {
            this.autoscrollTimer.stop();
        }
        this.isAutoScrolling = true;
        if (direction == 0) {
            this.scrollDirection = 0;
            Point convertedPoint = SwingUtilities.convertPoint(this.scroller, new Point(1, 1), this.list);
            int top = this.list.locationToIndex(convertedPoint);
            this.list.setSelectedIndex(top);
            this.autoscrollTimer = new Timer(100, new AutoScrollActionHandler(0));
        } else if (direction == 1) {
            this.scrollDirection = 1;
            Dimension size = this.scroller.getSize();
            Point convertedPoint = SwingUtilities.convertPoint(this.scroller, new Point(1, size.height - 1 - 2), this.list);
            int bottom = this.list.locationToIndex(convertedPoint);
            this.list.setSelectedIndex(bottom);
            this.autoscrollTimer = new Timer(100, new AutoScrollActionHandler(1));
        }
        this.autoscrollTimer.start();
    }

    protected void stopAutoScrolling() {
        this.isAutoScrolling = false;
        if (this.autoscrollTimer != null) {
            this.autoscrollTimer.stop();
            this.autoscrollTimer = null;
        }
    }

    protected void autoScrollUp() {
        int index = this.list.getSelectedIndex();
        if (index > 0) {
            this.list.setSelectedIndex(index - 1);
            this.list.ensureIndexIsVisible(index - 1);
        }
    }

    protected void autoScrollDown() {
        int lastItem;
        int index = this.list.getSelectedIndex();
        if (index < (lastItem = this.list.getModel().getSize() - 1)) {
            this.list.setSelectedIndex(index + 1);
            this.list.ensureIndexIsVisible(index + 1);
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        AccessibleContext context = super.getAccessibleContext();
        context.setAccessibleParent(this.comboBox);
        return context;
    }

    protected void delegateFocus(MouseEvent e) {
        if (this.comboBox.isEditable()) {
            Component comp = this.comboBox.getEditor().getEditorComponent();
            if (!(comp instanceof JComponent) || ((JComponent)comp).isRequestFocusEnabled()) {
                if (e != null) {
                    comp.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
                } else {
                    comp.requestFocus();
                }
            }
        } else if (this.comboBox.isRequestFocusEnabled()) {
            if (e != null) {
                this.comboBox.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            } else {
                this.comboBox.requestFocus();
            }
        }
    }

    protected void togglePopup() {
        if (this.isVisible()) {
            this.hide();
        } else {
            this.show();
        }
    }

    private void setListSelection(int selectedIndex) {
        if (selectedIndex == -1) {
            this.list.clearSelection();
        } else {
            this.list.setSelectedIndex(selectedIndex);
            this.list.ensureIndexIsVisible(selectedIndex);
        }
    }

    protected MouseEvent convertMouseEvent(MouseEvent e) {
        Point convertedPoint = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), this.list);
        MouseEvent newEvent = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), convertedPoint.x, convertedPoint.y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), 0);
        AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
        meAccessor.setCausedByTouchEvent(newEvent, meAccessor.isCausedByTouchEvent(e));
        return newEvent;
    }

    protected int getPopupHeightForRowCount(int maxRowCount) {
        Insets insets;
        Border border;
        int minRowCount = Math.min(maxRowCount, this.comboBox.getItemCount());
        int height = 0;
        ListCellRenderer<Object> renderer = this.list.getCellRenderer();
        Object value = null;
        for (int i = 0; i < minRowCount; ++i) {
            value = this.list.getModel().getElementAt(i);
            Component c = renderer.getListCellRendererComponent(this.list, value, i, false, false);
            height += c.getPreferredSize().height;
        }
        if (height == 0) {
            height = this.comboBox.getHeight();
        }
        if ((border = this.scroller.getViewportBorder()) != null) {
            insets = border.getBorderInsets(null);
            height += insets.top + insets.bottom;
        }
        if ((border = this.scroller.getBorder()) != null) {
            insets = border.getBorderInsets(null);
            height += insets.top + insets.bottom;
        }
        return height;
    }

    protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
        Rectangle screenBounds;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsConfiguration gc = this.comboBox.getGraphicsConfiguration();
        Point p = new Point();
        SwingUtilities.convertPointFromScreen(p, this.comboBox);
        if (gc != null) {
            Insets screenInsets = toolkit.getScreenInsets(gc);
            screenBounds = gc.getBounds();
            screenBounds.width -= screenInsets.left + screenInsets.right;
            screenBounds.height -= screenInsets.top + screenInsets.bottom;
            screenBounds.x += p.x + screenInsets.left;
            screenBounds.y += p.y + screenInsets.top;
        } else {
            screenBounds = new Rectangle(p, toolkit.getScreenSize());
        }
        int borderHeight = 0;
        Border popupBorder = this.getBorder();
        if (popupBorder != null) {
            Insets borderInsets = popupBorder.getBorderInsets(this);
            borderHeight = borderInsets.top + borderInsets.bottom;
            screenBounds.width -= borderInsets.left + borderInsets.right;
            screenBounds.height -= borderHeight;
        }
        Rectangle rect = new Rectangle(px, py, pw, ph);
        if (py + ph > screenBounds.y + screenBounds.height) {
            if (ph <= -screenBounds.y - borderHeight) {
                rect.y = -ph - borderHeight;
            } else {
                rect.y = screenBounds.y + Math.max(0, (screenBounds.height - ph) / 2);
                rect.height = Math.min(screenBounds.height, ph);
            }
        }
        return rect;
    }

    private Point getPopupLocation() {
        Dimension popupSize = this.comboBox.getSize();
        Insets insets = this.getInsets();
        popupSize.setSize(popupSize.width - (insets.right + insets.left), this.getPopupHeightForRowCount(this.comboBox.getMaximumRowCount()));
        Rectangle popupBounds = this.computePopupBounds(0, this.comboBox.getBounds().height, popupSize.width, popupSize.height);
        Dimension scrollSize = popupBounds.getSize();
        Point popupLocation = popupBounds.getLocation();
        this.scroller.setMaximumSize(scrollSize);
        this.scroller.setPreferredSize(scrollSize);
        this.scroller.setMinimumSize(scrollSize);
        this.list.revalidate();
        return popupLocation;
    }

    protected void updateListBoxSelectionForEvent(MouseEvent anEvent, boolean shouldScroll) {
        Point location = anEvent.getPoint();
        if (this.list == null) {
            return;
        }
        int index = this.list.locationToIndex(location);
        if (index == -1) {
            index = location.y < 0 ? 0 : this.comboBox.getModel().getSize() - 1;
        }
        if (this.list.getSelectedIndex() != index) {
            this.list.setSelectedIndex(index);
            if (shouldScroll) {
                this.list.ensureIndexIsVisible(index);
            }
        }
    }

    private class Handler
    implements ItemListener,
    MouseListener,
    MouseMotionListener,
    MouseWheelListener,
    PropertyChangeListener,
    Serializable {
        private Handler() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == BasicComboPopup.this.list) {
                return;
            }
            if (!(SwingUtilities.isLeftMouseButton(e) && BasicComboPopup.this.comboBox.isEnabled() && BasicComboPopup.this.comboBox.isShowing())) {
                return;
            }
            if (BasicComboPopup.this.comboBox.isEditable()) {
                Component comp = BasicComboPopup.this.comboBox.getEditor().getEditorComponent();
                if (!(comp instanceof JComponent) || ((JComponent)comp).isRequestFocusEnabled()) {
                    comp.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
                }
            } else if (BasicComboPopup.this.comboBox.isRequestFocusEnabled()) {
                BasicComboPopup.this.comboBox.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
            BasicComboPopup.this.togglePopup();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == BasicComboPopup.this.list) {
                if (BasicComboPopup.this.list.getModel().getSize() > 0) {
                    if (BasicComboPopup.this.comboBox.getSelectedIndex() == BasicComboPopup.this.list.getSelectedIndex()) {
                        BasicComboPopup.this.comboBox.getEditor().setItem(BasicComboPopup.this.list.getSelectedValue());
                    }
                    BasicComboPopup.this.comboBox.setSelectedIndex(BasicComboPopup.this.list.getSelectedIndex());
                }
                BasicComboPopup.this.comboBox.setPopupVisible(false);
                if (BasicComboPopup.this.comboBox.isEditable() && BasicComboPopup.this.comboBox.getEditor() != null) {
                    BasicComboPopup.this.comboBox.configureEditor(BasicComboPopup.this.comboBox.getEditor(), BasicComboPopup.this.comboBox.getSelectedItem());
                }
                return;
            }
            Component source = (Component)e.getSource();
            Dimension size = source.getSize();
            Rectangle bounds = new Rectangle(0, 0, size.width, size.height);
            if (!bounds.contains(e.getPoint())) {
                MouseEvent newEvent = BasicComboPopup.this.convertMouseEvent(e);
                Point location = newEvent.getPoint();
                Rectangle r = new Rectangle();
                BasicComboPopup.this.list.computeVisibleRect(r);
                if (r.contains(location)) {
                    if (BasicComboPopup.this.comboBox.getSelectedIndex() == BasicComboPopup.this.list.getSelectedIndex()) {
                        BasicComboPopup.this.comboBox.getEditor().setItem(BasicComboPopup.this.list.getSelectedValue());
                    }
                    BasicComboPopup.this.comboBox.setSelectedIndex(BasicComboPopup.this.list.getSelectedIndex());
                }
                BasicComboPopup.this.comboBox.setPopupVisible(false);
            }
            BasicComboPopup.this.hasEntered = false;
            BasicComboPopup.this.stopAutoScrolling();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent anEvent) {
            if (anEvent.getSource() == BasicComboPopup.this.list) {
                Point location = anEvent.getPoint();
                Rectangle r = new Rectangle();
                BasicComboPopup.this.list.computeVisibleRect(r);
                if (r.contains(location)) {
                    BasicComboPopup.this.updateListBoxSelectionForEvent(anEvent, false);
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getSource() == BasicComboPopup.this.list) {
                return;
            }
            if (BasicComboPopup.this.isVisible()) {
                MouseEvent newEvent = BasicComboPopup.this.convertMouseEvent(e);
                Rectangle r = new Rectangle();
                BasicComboPopup.this.list.computeVisibleRect(r);
                if (newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1) {
                    Point location;
                    BasicComboPopup.this.hasEntered = true;
                    if (BasicComboPopup.this.isAutoScrolling) {
                        BasicComboPopup.this.stopAutoScrolling();
                    }
                    if (r.contains(location = newEvent.getPoint())) {
                        BasicComboPopup.this.updateListBoxSelectionForEvent(newEvent, false);
                    }
                } else if (BasicComboPopup.this.hasEntered) {
                    int directionToScroll;
                    int n = directionToScroll = newEvent.getPoint().y < r.y ? 0 : 1;
                    if (BasicComboPopup.this.isAutoScrolling && BasicComboPopup.this.scrollDirection != directionToScroll) {
                        BasicComboPopup.this.stopAutoScrolling();
                        BasicComboPopup.this.startAutoScrolling(directionToScroll);
                    } else if (!BasicComboPopup.this.isAutoScrolling) {
                        BasicComboPopup.this.startAutoScrolling(directionToScroll);
                    }
                } else if (e.getPoint().y < 0) {
                    BasicComboPopup.this.hasEntered = true;
                    BasicComboPopup.this.startAutoScrolling(0);
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            JComboBox comboBox = (JComboBox)e.getSource();
            String propertyName = e.getPropertyName();
            if (propertyName == "model") {
                ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
                ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
                BasicComboPopup.this.uninstallComboBoxModelListeners(oldModel);
                BasicComboPopup.this.installComboBoxModelListeners(newModel);
                BasicComboPopup.this.list.setModel(newModel);
                if (BasicComboPopup.this.isVisible()) {
                    BasicComboPopup.this.hide();
                }
            } else if (propertyName == "renderer") {
                BasicComboPopup.this.list.setCellRenderer(comboBox.getRenderer());
                if (BasicComboPopup.this.isVisible()) {
                    BasicComboPopup.this.hide();
                }
            } else if (propertyName == "componentOrientation") {
                ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                JList<Object> list = BasicComboPopup.this.getList();
                if (list != null && list.getComponentOrientation() != o) {
                    list.setComponentOrientation(o);
                }
                if (BasicComboPopup.this.scroller != null && BasicComboPopup.this.scroller.getComponentOrientation() != o) {
                    BasicComboPopup.this.scroller.setComponentOrientation(o);
                }
                if (o != BasicComboPopup.this.getComponentOrientation()) {
                    BasicComboPopup.this.setComponentOrientation(o);
                }
            } else if (propertyName == "lightWeightPopupEnabled") {
                BasicComboPopup.this.setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == 1) {
                JComboBox comboBox = (JComboBox)e.getSource();
                BasicComboPopup.this.setListSelection(comboBox.getSelectedIndex());
            } else {
                BasicComboPopup.this.setListSelection(-1);
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            e.consume();
        }
    }

    private class AutoScrollActionHandler
    implements ActionListener {
        private int direction;

        AutoScrollActionHandler(int direction) {
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.direction == 0) {
                BasicComboPopup.this.autoScrollUp();
            } else {
                BasicComboPopup.this.autoScrollDown();
            }
        }
    }

    private static class EmptyListModelClass
    implements ListModel<Object>,
    Serializable {
        private EmptyListModelClass() {
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Object getElementAt(int index) {
            return null;
        }

        @Override
        public void addListDataListener(ListDataListener l) {
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
        }
    }

    protected class PropertyChangeHandler
    implements PropertyChangeListener {
        protected PropertyChangeHandler() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicComboPopup.this.getHandler().propertyChange(e);
        }
    }

    protected class ItemHandler
    implements ItemListener {
        protected ItemHandler() {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            BasicComboPopup.this.getHandler().itemStateChanged(e);
        }
    }

    protected class ListMouseMotionHandler
    extends MouseMotionAdapter {
        protected ListMouseMotionHandler() {
        }

        @Override
        public void mouseMoved(MouseEvent anEvent) {
            BasicComboPopup.this.getHandler().mouseMoved(anEvent);
        }
    }

    protected class ListMouseHandler
    extends MouseAdapter {
        protected ListMouseHandler() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent anEvent) {
            BasicComboPopup.this.getHandler().mouseReleased(anEvent);
        }
    }

    public class ListDataHandler
    implements ListDataListener {
        public ListDataHandler(BasicComboPopup this$0) {
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
        }
    }

    protected class ListSelectionHandler
    implements ListSelectionListener {
        protected ListSelectionHandler(BasicComboPopup this$0) {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
        }
    }

    public class InvocationKeyHandler
    extends KeyAdapter {
        public InvocationKeyHandler(BasicComboPopup this$0) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    protected class InvocationMouseMotionHandler
    extends MouseMotionAdapter {
        protected InvocationMouseMotionHandler() {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            BasicComboPopup.this.getHandler().mouseDragged(e);
        }
    }

    protected class InvocationMouseHandler
    extends MouseAdapter {
        protected InvocationMouseHandler() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            BasicComboPopup.this.getHandler().mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            BasicComboPopup.this.getHandler().mouseReleased(e);
        }
    }
}

