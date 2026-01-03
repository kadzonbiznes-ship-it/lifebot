/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.LazyActionMap;
import sun.swing.DefaultLookup;
import sun.swing.UIAction;

public class BasicScrollPaneUI
extends ScrollPaneUI
implements ScrollPaneConstants {
    protected JScrollPane scrollpane;
    protected ChangeListener vsbChangeListener;
    protected ChangeListener hsbChangeListener;
    protected ChangeListener viewportChangeListener;
    protected PropertyChangeListener spPropertyChangeListener;
    private MouseWheelListener mouseScrollListener;
    private int oldExtent = Integer.MIN_VALUE;
    private PropertyChangeListener vsbPropertyChangeListener;
    private PropertyChangeListener hsbPropertyChangeListener;
    private Handler handler;
    private boolean setValueCalled = false;

    public static ComponentUI createUI(JComponent x) {
        return new BasicScrollPaneUI();
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("scrollUp"));
        map.put(new Actions("scrollDown"));
        map.put(new Actions("scrollHome"));
        map.put(new Actions("scrollEnd"));
        map.put(new Actions("unitScrollUp"));
        map.put(new Actions("unitScrollDown"));
        map.put(new Actions("scrollLeft"));
        map.put(new Actions("scrollRight"));
        map.put(new Actions("unitScrollRight"));
        map.put(new Actions("unitScrollLeft"));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Border vpBorder = this.scrollpane.getViewportBorder();
        if (vpBorder != null) {
            Rectangle r = this.scrollpane.getViewportBorderBounds();
            vpBorder.paintBorder(this.scrollpane, g, r.x, r.y, r.width, r.height);
        }
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    protected void installDefaults(JScrollPane scrollpane) {
        LookAndFeel.installBorder(scrollpane, "ScrollPane.border");
        LookAndFeel.installColorsAndFont(scrollpane, "ScrollPane.background", "ScrollPane.foreground", "ScrollPane.font");
        Border vpBorder = scrollpane.getViewportBorder();
        if (vpBorder == null || vpBorder instanceof UIResource) {
            vpBorder = UIManager.getBorder("ScrollPane.viewportBorder");
            scrollpane.setViewportBorder(vpBorder);
        }
        LookAndFeel.installProperty(scrollpane, "opaque", Boolean.TRUE);
    }

    protected void installListeners(JScrollPane c) {
        this.vsbChangeListener = this.createVSBChangeListener();
        this.vsbPropertyChangeListener = this.createVSBPropertyChangeListener();
        this.hsbChangeListener = this.createHSBChangeListener();
        this.hsbPropertyChangeListener = this.createHSBPropertyChangeListener();
        this.viewportChangeListener = this.createViewportChangeListener();
        this.spPropertyChangeListener = this.createPropertyChangeListener();
        JViewport viewport = this.scrollpane.getViewport();
        JScrollBar vsb = this.scrollpane.getVerticalScrollBar();
        JScrollBar hsb = this.scrollpane.getHorizontalScrollBar();
        if (viewport != null) {
            viewport.addChangeListener(this.viewportChangeListener);
        }
        if (vsb != null) {
            vsb.getModel().addChangeListener(this.vsbChangeListener);
            vsb.addPropertyChangeListener(this.vsbPropertyChangeListener);
        }
        if (hsb != null) {
            hsb.getModel().addChangeListener(this.hsbChangeListener);
            hsb.addPropertyChangeListener(this.hsbPropertyChangeListener);
        }
        this.scrollpane.addPropertyChangeListener(this.spPropertyChangeListener);
        this.mouseScrollListener = this.createMouseWheelListener();
        this.scrollpane.addMouseWheelListener(this.mouseScrollListener);
    }

    protected void installKeyboardActions(JScrollPane c) {
        InputMap inputMap = this.getInputMap(1);
        SwingUtilities.replaceUIInputMap(c, 1, inputMap);
        LazyActionMap.installLazyActionMap(c, BasicScrollPaneUI.class, "ScrollPane.actionMap");
    }

    InputMap getInputMap(int condition) {
        if (condition == 1) {
            InputMap rtlKeyMap;
            InputMap keyMap = (InputMap)DefaultLookup.get(this.scrollpane, this, "ScrollPane.ancestorInputMap");
            if (this.scrollpane.getComponentOrientation().isLeftToRight() || (rtlKeyMap = (InputMap)DefaultLookup.get(this.scrollpane, this, "ScrollPane.ancestorInputMap.RightToLeft")) == null) {
                return keyMap;
            }
            rtlKeyMap.setParent(keyMap);
            return rtlKeyMap;
        }
        return null;
    }

    @Override
    public void installUI(JComponent x) {
        this.scrollpane = (JScrollPane)x;
        this.installDefaults(this.scrollpane);
        this.installListeners(this.scrollpane);
        this.installKeyboardActions(this.scrollpane);
    }

    protected void uninstallDefaults(JScrollPane c) {
        LookAndFeel.uninstallBorder(this.scrollpane);
        if (this.scrollpane.getViewportBorder() instanceof UIResource) {
            this.scrollpane.setViewportBorder(null);
        }
    }

    protected void uninstallListeners(JComponent c) {
        JViewport viewport = this.scrollpane.getViewport();
        JScrollBar vsb = this.scrollpane.getVerticalScrollBar();
        JScrollBar hsb = this.scrollpane.getHorizontalScrollBar();
        if (viewport != null) {
            viewport.removeChangeListener(this.viewportChangeListener);
        }
        if (vsb != null) {
            vsb.getModel().removeChangeListener(this.vsbChangeListener);
            vsb.removePropertyChangeListener(this.vsbPropertyChangeListener);
        }
        if (hsb != null) {
            hsb.getModel().removeChangeListener(this.hsbChangeListener);
            hsb.removePropertyChangeListener(this.hsbPropertyChangeListener);
        }
        this.scrollpane.removePropertyChangeListener(this.spPropertyChangeListener);
        if (this.mouseScrollListener != null) {
            this.scrollpane.removeMouseWheelListener(this.mouseScrollListener);
        }
        this.vsbChangeListener = null;
        this.hsbChangeListener = null;
        this.viewportChangeListener = null;
        this.spPropertyChangeListener = null;
        this.mouseScrollListener = null;
        this.handler = null;
    }

    protected void uninstallKeyboardActions(JScrollPane c) {
        SwingUtilities.replaceUIActionMap(c, null);
        SwingUtilities.replaceUIInputMap(c, 1, null);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallDefaults(this.scrollpane);
        this.uninstallListeners(this.scrollpane);
        this.uninstallKeyboardActions(this.scrollpane);
        this.scrollpane = null;
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void syncScrollPaneWithViewport() {
        JViewport viewport = this.scrollpane.getViewport();
        JScrollBar vsb = this.scrollpane.getVerticalScrollBar();
        JScrollBar hsb = this.scrollpane.getHorizontalScrollBar();
        JViewport rowHead = this.scrollpane.getRowHeader();
        JViewport colHead = this.scrollpane.getColumnHeader();
        boolean ltr = this.scrollpane.getComponentOrientation().isLeftToRight();
        if (viewport != null) {
            int value;
            int max;
            int extent;
            Dimension extentSize = viewport.getExtentSize();
            Dimension viewSize = viewport.getViewSize();
            Point viewPosition = viewport.getViewPosition();
            if (vsb != null) {
                extent = extentSize.height;
                max = viewSize.height;
                value = Math.max(0, Math.min(viewPosition.y, max - extent));
                vsb.setValues(value, extent, 0, max);
            }
            if (hsb != null) {
                extent = extentSize.width;
                max = viewSize.width;
                if (ltr) {
                    value = Math.max(0, Math.min(viewPosition.x, max - extent));
                } else {
                    int currentValue = hsb.getValue();
                    if (this.setValueCalled && max - currentValue == viewPosition.x) {
                        value = Math.max(0, Math.min(max - extent, currentValue));
                        if (extent != 0) {
                            this.setValueCalled = false;
                        }
                    } else if (extent > max) {
                        viewPosition.x = max - extent;
                        viewport.setViewPosition(viewPosition);
                        value = 0;
                    } else {
                        value = Math.max(0, Math.min(max - extent, max - extent - viewPosition.x));
                        if (this.oldExtent > extent) {
                            value -= this.oldExtent - extent;
                        }
                    }
                }
                this.oldExtent = extent;
                hsb.setValues(value, extent, 0, max);
            }
            if (rowHead != null) {
                Point p = rowHead.getViewPosition();
                p.y = viewport.getViewPosition().y;
                p.x = 0;
                rowHead.setViewPosition(p);
            }
            if (colHead != null) {
                Point p = colHead.getViewPosition();
                p.x = ltr ? viewport.getViewPosition().x : Math.max(0, viewport.getViewPosition().x);
                p.y = 0;
                colHead.setViewPosition(p);
            }
        }
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        Component view;
        int baseline;
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height must be >= 0");
        }
        JViewport viewport = this.scrollpane.getViewport();
        Insets spInsets = this.scrollpane.getInsets();
        int y = spInsets.top;
        height = height - spInsets.top - spInsets.bottom;
        width = width - spInsets.left - spInsets.right;
        JViewport columnHeader = this.scrollpane.getColumnHeader();
        if (columnHeader != null && columnHeader.isVisible()) {
            Component header = columnHeader.getView();
            if (header != null && header.isVisible()) {
                Dimension headerPref = header.getPreferredSize();
                baseline = header.getBaseline(headerPref.width, headerPref.height);
                if (baseline >= 0) {
                    return y + baseline;
                }
            }
            Dimension columnPref = columnHeader.getPreferredSize();
            height -= columnPref.height;
            y += columnPref.height;
        }
        Component component = view = viewport == null ? null : viewport.getView();
        if (view != null && view.isVisible() && view.getBaselineResizeBehavior() == Component.BaselineResizeBehavior.CONSTANT_ASCENT) {
            Border viewportBorder = this.scrollpane.getViewportBorder();
            if (viewportBorder != null) {
                Insets vpbInsets = viewportBorder.getBorderInsets(this.scrollpane);
                y += vpbInsets.top;
                height = height - vpbInsets.top - vpbInsets.bottom;
                width = width - vpbInsets.left - vpbInsets.right;
            }
            if (view.getWidth() > 0 && view.getHeight() > 0) {
                Dimension min = view.getMinimumSize();
                width = Math.max(min.width, view.getWidth());
                height = Math.max(min.height, view.getHeight());
            }
            if (width > 0 && height > 0 && (baseline = view.getBaseline(width, height)) > 0) {
                return y + baseline;
            }
        }
        return -1;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    protected ChangeListener createViewportChangeListener() {
        return this.getHandler();
    }

    private PropertyChangeListener createHSBPropertyChangeListener() {
        return this.getHandler();
    }

    protected ChangeListener createHSBChangeListener() {
        return this.getHandler();
    }

    private PropertyChangeListener createVSBPropertyChangeListener() {
        return this.getHandler();
    }

    protected ChangeListener createVSBChangeListener() {
        return this.getHandler();
    }

    protected MouseWheelListener createMouseWheelListener() {
        return this.getHandler();
    }

    protected void updateScrollBarDisplayPolicy(PropertyChangeEvent e) {
        this.scrollpane.revalidate();
        this.scrollpane.repaint();
    }

    protected void updateViewport(PropertyChangeEvent e) {
        JViewport oldViewport = (JViewport)e.getOldValue();
        JViewport newViewport = (JViewport)e.getNewValue();
        if (oldViewport != null) {
            oldViewport.removeChangeListener(this.viewportChangeListener);
        }
        if (newViewport != null) {
            int max;
            int extent;
            Point p = newViewport.getViewPosition();
            p.x = this.scrollpane.getComponentOrientation().isLeftToRight() ? Math.max(p.x, 0) : ((extent = newViewport.getExtentSize().width) > (max = newViewport.getViewSize().width) ? max - extent : Math.max(0, Math.min(max - extent, p.x)));
            p.y = Math.max(p.y, 0);
            newViewport.setViewPosition(p);
            newViewport.addChangeListener(this.viewportChangeListener);
        }
    }

    protected void updateRowHeader(PropertyChangeEvent e) {
        JViewport newRowHead = (JViewport)e.getNewValue();
        if (newRowHead != null) {
            JViewport viewport = this.scrollpane.getViewport();
            Point p = newRowHead.getViewPosition();
            p.y = viewport != null ? viewport.getViewPosition().y : 0;
            newRowHead.setViewPosition(p);
        }
    }

    protected void updateColumnHeader(PropertyChangeEvent e) {
        JViewport newColHead = (JViewport)e.getNewValue();
        if (newColHead != null) {
            JViewport viewport = this.scrollpane.getViewport();
            Point p = newColHead.getViewPosition();
            p.x = viewport == null ? 0 : (this.scrollpane.getComponentOrientation().isLeftToRight() ? viewport.getViewPosition().x : Math.max(0, viewport.getViewPosition().x));
            newColHead.setViewPosition(p);
            this.scrollpane.add((Component)newColHead, "COLUMN_HEADER");
        }
    }

    private void updateHorizontalScrollBar(PropertyChangeEvent pce) {
        this.updateScrollBar(pce, this.hsbChangeListener, this.hsbPropertyChangeListener);
    }

    private void updateVerticalScrollBar(PropertyChangeEvent pce) {
        this.updateScrollBar(pce, this.vsbChangeListener, this.vsbPropertyChangeListener);
    }

    private void updateScrollBar(PropertyChangeEvent pce, ChangeListener cl, PropertyChangeListener pcl) {
        JScrollBar sb = (JScrollBar)pce.getOldValue();
        if (sb != null) {
            if (cl != null) {
                sb.getModel().removeChangeListener(cl);
            }
            if (pcl != null) {
                sb.removePropertyChangeListener(pcl);
            }
        }
        if ((sb = (JScrollBar)pce.getNewValue()) != null) {
            if (cl != null) {
                sb.getModel().addChangeListener(cl);
            }
            if (pcl != null) {
                sb.addPropertyChangeListener(pcl);
            }
        }
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    private static class Actions
    extends UIAction {
        private static final String SCROLL_UP = "scrollUp";
        private static final String SCROLL_DOWN = "scrollDown";
        private static final String SCROLL_HOME = "scrollHome";
        private static final String SCROLL_END = "scrollEnd";
        private static final String UNIT_SCROLL_UP = "unitScrollUp";
        private static final String UNIT_SCROLL_DOWN = "unitScrollDown";
        private static final String SCROLL_LEFT = "scrollLeft";
        private static final String SCROLL_RIGHT = "scrollRight";
        private static final String UNIT_SCROLL_LEFT = "unitScrollLeft";
        private static final String UNIT_SCROLL_RIGHT = "unitScrollRight";

        Actions(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JScrollPane scrollPane = (JScrollPane)e.getSource();
            boolean ltr = scrollPane.getComponentOrientation().isLeftToRight();
            String key = this.getName();
            if (key == SCROLL_UP) {
                this.scroll(scrollPane, 1, -1, true);
            } else if (key == SCROLL_DOWN) {
                this.scroll(scrollPane, 1, 1, true);
            } else if (key == SCROLL_HOME) {
                this.scrollHome(scrollPane);
            } else if (key == SCROLL_END) {
                this.scrollEnd(scrollPane);
            } else if (key == UNIT_SCROLL_UP) {
                this.scroll(scrollPane, 1, -1, false);
            } else if (key == UNIT_SCROLL_DOWN) {
                this.scroll(scrollPane, 1, 1, false);
            } else if (key == SCROLL_LEFT) {
                this.scroll(scrollPane, 0, ltr ? -1 : 1, true);
            } else if (key == SCROLL_RIGHT) {
                this.scroll(scrollPane, 0, ltr ? 1 : -1, true);
            } else if (key == UNIT_SCROLL_LEFT) {
                this.scroll(scrollPane, 0, ltr ? -1 : 1, false);
            } else if (key == UNIT_SCROLL_RIGHT) {
                this.scroll(scrollPane, 0, ltr ? 1 : -1, false);
            }
        }

        private void scrollEnd(JScrollPane scrollpane) {
            Component view;
            JViewport vp = scrollpane.getViewport();
            if (vp != null && (view = vp.getView()) != null) {
                Rectangle visRect = vp.getViewRect();
                Rectangle bounds = view.getBounds();
                if (scrollpane.getComponentOrientation().isLeftToRight()) {
                    vp.setViewPosition(new Point(bounds.width - visRect.width, bounds.height - visRect.height));
                } else {
                    vp.setViewPosition(new Point(0, bounds.height - visRect.height));
                }
            }
        }

        private void scrollHome(JScrollPane scrollpane) {
            Component view;
            JViewport vp = scrollpane.getViewport();
            if (vp != null && (view = vp.getView()) != null) {
                if (scrollpane.getComponentOrientation().isLeftToRight()) {
                    vp.setViewPosition(new Point(0, 0));
                } else {
                    Rectangle visRect = vp.getViewRect();
                    Rectangle bounds = view.getBounds();
                    vp.setViewPosition(new Point(bounds.width - visRect.width, 0));
                }
            }
        }

        private void scroll(JScrollPane scrollpane, int orientation, int direction, boolean block) {
            Component view;
            JViewport vp = scrollpane.getViewport();
            if (vp != null && (view = vp.getView()) != null) {
                Rectangle visRect = vp.getViewRect();
                Dimension vSize = view.getSize();
                int amount = view instanceof Scrollable ? (block ? ((Scrollable)((Object)view)).getScrollableBlockIncrement(visRect, orientation, direction) : ((Scrollable)((Object)view)).getScrollableUnitIncrement(visRect, orientation, direction)) : (block ? (orientation == 1 ? visRect.height : visRect.width) : 10);
                if (orientation == 1) {
                    visRect.y += amount * direction;
                    if (visRect.y + visRect.height > vSize.height) {
                        visRect.y = Math.max(0, vSize.height - visRect.height);
                    } else if (visRect.y < 0) {
                        visRect.y = 0;
                    }
                } else if (scrollpane.getComponentOrientation().isLeftToRight()) {
                    visRect.x += amount * direction;
                    if (visRect.x + visRect.width > vSize.width) {
                        visRect.x = Math.max(0, vSize.width - visRect.width);
                    } else if (visRect.x < 0) {
                        visRect.x = 0;
                    }
                } else {
                    visRect.x -= amount * direction;
                    visRect.x = visRect.width > vSize.width ? vSize.width - visRect.width : Math.max(0, Math.min(vSize.width - visRect.width, visRect.x));
                }
                vp.setViewPosition(visRect.getLocation());
            }
        }
    }

    class Handler
    implements ChangeListener,
    PropertyChangeListener,
    MouseWheelListener {
        Handler() {
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (BasicScrollPaneUI.this.scrollpane.isWheelScrollingEnabled() && e.getWheelRotation() != 0) {
                JScrollBar toScroll = BasicScrollPaneUI.this.scrollpane.getVerticalScrollBar();
                int direction = e.getWheelRotation() < 0 ? -1 : 1;
                int orientation = 1;
                if (toScroll == null || !toScroll.isVisible() || e.isShiftDown()) {
                    JScrollBar hScroll = BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar();
                    if (hScroll == null) {
                        return;
                    }
                    if (hScroll.isVisible()) {
                        toScroll = hScroll;
                        orientation = 0;
                    } else if (!hScroll.isVisible()) {
                        if (e.isShiftDown()) {
                            return;
                        }
                        orientation = 1;
                    }
                }
                e.consume();
                if (e.getScrollType() == 0) {
                    JViewport vp = BasicScrollPaneUI.this.scrollpane.getViewport();
                    if (vp == null) {
                        return;
                    }
                    Component comp = vp.getView();
                    int units = Math.abs(e.getUnitsToScroll());
                    boolean limitScroll = Math.abs(e.getWheelRotation()) == 1;
                    Object fastWheelScroll = toScroll.getClientProperty("JScrollBar.fastWheelScrolling");
                    if (Boolean.TRUE == fastWheelScroll && comp instanceof Scrollable) {
                        Scrollable scrollComp = (Scrollable)((Object)comp);
                        Rectangle viewRect = vp.getViewRect();
                        int startingX = viewRect.x;
                        boolean leftToRight = comp.getComponentOrientation().isLeftToRight();
                        int scrollMin = toScroll.getMinimum();
                        int scrollMax = toScroll.getMaximum() - toScroll.getModel().getExtent();
                        if (limitScroll) {
                            int blockIncr = scrollComp.getScrollableBlockIncrement(viewRect, orientation, direction);
                            if (direction < 0) {
                                scrollMin = Math.max(scrollMin, toScroll.getValue() - blockIncr);
                            } else {
                                scrollMax = Math.min(scrollMax, toScroll.getValue() + blockIncr);
                            }
                        }
                        for (int i = 0; i < units; ++i) {
                            int unitIncr = scrollComp.getScrollableUnitIncrement(viewRect, orientation, direction);
                            if (orientation == 1) {
                                if (direction < 0) {
                                    viewRect.y -= unitIncr;
                                    if (viewRect.y > scrollMin) continue;
                                    viewRect.y = scrollMin;
                                    break;
                                }
                                viewRect.y += unitIncr;
                                if (viewRect.y < scrollMax) continue;
                                viewRect.y = scrollMax;
                                break;
                            }
                            if (leftToRight && direction < 0 || !leftToRight && direction > 0) {
                                viewRect.x -= unitIncr;
                                if (!leftToRight || viewRect.x >= scrollMin) continue;
                                viewRect.x = scrollMin;
                                break;
                            }
                            if (leftToRight && direction > 0 || !leftToRight && direction < 0) {
                                viewRect.x += unitIncr;
                                if (!leftToRight || viewRect.x <= scrollMax) continue;
                                viewRect.x = scrollMax;
                                break;
                            }
                            assert (false) : "Non-sensical ComponentOrientation / scroll direction";
                        }
                        if (orientation == 1) {
                            toScroll.setValue(viewRect.y);
                        } else if (leftToRight) {
                            toScroll.setValue(viewRect.x);
                        } else {
                            int newPos = toScroll.getValue() - (viewRect.x - startingX);
                            if (newPos < scrollMin) {
                                newPos = scrollMin;
                            } else if (newPos > scrollMax) {
                                newPos = scrollMax;
                            }
                            toScroll.setValue(newPos);
                        }
                    } else {
                        BasicScrollBarUI.scrollByUnits(toScroll, direction, units, limitScroll);
                    }
                } else if (e.getScrollType() == 1) {
                    BasicScrollBarUI.scrollByBlock(toScroll, direction);
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JViewport viewport = BasicScrollPaneUI.this.scrollpane.getViewport();
            if (viewport != null) {
                if (e.getSource() == viewport) {
                    BasicScrollPaneUI.this.syncScrollPaneWithViewport();
                } else {
                    JScrollBar hsb = BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar();
                    if (hsb != null && e.getSource() == hsb.getModel()) {
                        this.hsbStateChanged(viewport, e);
                    } else {
                        JScrollBar vsb = BasicScrollPaneUI.this.scrollpane.getVerticalScrollBar();
                        if (vsb != null && e.getSource() == vsb.getModel()) {
                            this.vsbStateChanged(viewport, e);
                        }
                    }
                }
            }
        }

        private void vsbStateChanged(JViewport viewport, ChangeEvent e) {
            BoundedRangeModel model = (BoundedRangeModel)e.getSource();
            Point p = viewport.getViewPosition();
            p.y = model.getValue();
            viewport.setViewPosition(p);
        }

        private void hsbStateChanged(JViewport viewport, ChangeEvent e) {
            BoundedRangeModel model = (BoundedRangeModel)e.getSource();
            Point p = viewport.getViewPosition();
            int value = model.getValue();
            if (BasicScrollPaneUI.this.scrollpane.getComponentOrientation().isLeftToRight()) {
                p.x = value;
            } else {
                int max = viewport.getViewSize().width;
                int extent = viewport.getExtentSize().width;
                int oldX = p.x;
                p.x = max - extent - value;
                if (extent == 0 && value != 0 && oldX == max) {
                    BasicScrollPaneUI.this.setValueCalled = true;
                } else if (extent != 0 && oldX < 0 && p.x == 0) {
                    p.x += value;
                }
            }
            viewport.setViewPosition(p);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getSource() == BasicScrollPaneUI.this.scrollpane) {
                this.scrollPanePropertyChange(e);
            } else {
                this.sbPropertyChange(e);
            }
        }

        private void scrollPanePropertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "verticalScrollBarDisplayPolicy") {
                BasicScrollPaneUI.this.updateScrollBarDisplayPolicy(e);
            } else if (propertyName == "horizontalScrollBarDisplayPolicy") {
                BasicScrollPaneUI.this.updateScrollBarDisplayPolicy(e);
            } else if (propertyName == "viewport") {
                BasicScrollPaneUI.this.updateViewport(e);
            } else if (propertyName == "rowHeader") {
                BasicScrollPaneUI.this.updateRowHeader(e);
            } else if (propertyName == "columnHeader") {
                BasicScrollPaneUI.this.updateColumnHeader(e);
            } else if (propertyName == "verticalScrollBar") {
                BasicScrollPaneUI.this.updateVerticalScrollBar(e);
            } else if (propertyName == "horizontalScrollBar") {
                BasicScrollPaneUI.this.updateHorizontalScrollBar(e);
            } else if (propertyName == "componentOrientation") {
                BasicScrollPaneUI.this.scrollpane.revalidate();
                BasicScrollPaneUI.this.scrollpane.repaint();
            }
        }

        private void sbPropertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            Object source = e.getSource();
            if ("model" == propertyName) {
                JScrollBar sb = BasicScrollPaneUI.this.scrollpane.getVerticalScrollBar();
                BoundedRangeModel oldModel = (BoundedRangeModel)e.getOldValue();
                ChangeListener cl = null;
                if (source == sb) {
                    cl = BasicScrollPaneUI.this.vsbChangeListener;
                } else if (source == BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar()) {
                    sb = BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar();
                    cl = BasicScrollPaneUI.this.hsbChangeListener;
                }
                if (cl != null) {
                    if (oldModel != null) {
                        oldModel.removeChangeListener(cl);
                    }
                    if (sb.getModel() != null) {
                        sb.getModel().addChangeListener(cl);
                    }
                }
            } else if ("componentOrientation" == propertyName && source == BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar()) {
                JScrollBar hsb = BasicScrollPaneUI.this.scrollpane.getHorizontalScrollBar();
                JViewport viewport = BasicScrollPaneUI.this.scrollpane.getViewport();
                Point p = viewport.getViewPosition();
                p.x = BasicScrollPaneUI.this.scrollpane.getComponentOrientation().isLeftToRight() ? hsb.getValue() : viewport.getViewSize().width - viewport.getExtentSize().width - hsb.getValue();
                viewport.setViewPosition(p);
            }
        }
    }

    @Deprecated(since="17", forRemoval=true)
    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicScrollPaneUI.this.getHandler().propertyChange(e);
        }
    }

    protected class MouseWheelHandler
    implements MouseWheelListener {
        protected MouseWheelHandler() {
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            BasicScrollPaneUI.this.getHandler().mouseWheelMoved(e);
        }
    }

    @Deprecated(since="17", forRemoval=true)
    public class VSBChangeListener
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicScrollPaneUI.this.getHandler().stateChanged(e);
        }
    }

    @Deprecated(since="17", forRemoval=true)
    public class HSBChangeListener
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicScrollPaneUI.this.getHandler().stateChanged(e);
        }
    }

    @Deprecated(since="17", forRemoval=true)
    public class ViewportChangeHandler
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicScrollPaneUI.this.getHandler().stateChanged(e);
        }
    }
}

