/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.LazyActionMap;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicScrollBarUI
extends ScrollBarUI
implements LayoutManager,
SwingConstants {
    private static final int POSITIVE_SCROLL = 1;
    private static final int NEGATIVE_SCROLL = -1;
    private static final int MIN_SCROLL = 2;
    private static final int MAX_SCROLL = 3;
    protected Dimension minimumThumbSize;
    protected Dimension maximumThumbSize;
    protected Color thumbHighlightColor;
    protected Color thumbLightShadowColor;
    protected Color thumbDarkShadowColor;
    protected Color thumbColor;
    protected Color trackColor;
    protected Color trackHighlightColor;
    protected JScrollBar scrollbar;
    protected JButton incrButton;
    protected JButton decrButton;
    protected boolean isDragging;
    protected TrackListener trackListener;
    protected ArrowButtonListener buttonListener;
    protected ModelListener modelListener;
    protected Rectangle thumbRect;
    protected Rectangle trackRect;
    protected int trackHighlight;
    protected static final int NO_HIGHLIGHT = 0;
    protected static final int DECREASE_HIGHLIGHT = 1;
    protected static final int INCREASE_HIGHLIGHT = 2;
    protected ScrollListener scrollListener;
    protected PropertyChangeListener propertyChangeListener;
    protected Timer scrollTimer;
    private static final int scrollSpeedThrottle = 60;
    private boolean supportsAbsolutePositioning;
    protected int scrollBarWidth;
    private Handler handler;
    private boolean thumbActive;
    private boolean useCachedValue = false;
    private int scrollBarValue;
    protected int incrGap;
    protected int decrGap;

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("positiveUnitIncrement"));
        map.put(new Actions("positiveBlockIncrement"));
        map.put(new Actions("negativeUnitIncrement"));
        map.put(new Actions("negativeBlockIncrement"));
        map.put(new Actions("minScroll"));
        map.put(new Actions("maxScroll"));
    }

    public static ComponentUI createUI(JComponent c) {
        return new BasicScrollBarUI();
    }

    protected void configureScrollBarColors() {
        LookAndFeel.installColors(this.scrollbar, "ScrollBar.background", "ScrollBar.foreground");
        this.thumbHighlightColor = UIManager.getColor("ScrollBar.thumbHighlight");
        this.thumbLightShadowColor = UIManager.getColor("ScrollBar.thumbShadow");
        this.thumbDarkShadowColor = UIManager.getColor("ScrollBar.thumbDarkShadow");
        this.thumbColor = UIManager.getColor("ScrollBar.thumb");
        this.trackColor = UIManager.getColor("ScrollBar.track");
        this.trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
    }

    @Override
    public void installUI(JComponent c) {
        this.scrollbar = (JScrollBar)c;
        this.thumbRect = new Rectangle(0, 0, 0, 0);
        this.trackRect = new Rectangle(0, 0, 0, 0);
        this.installDefaults();
        this.installComponents();
        this.installListeners();
        this.installKeyboardActions();
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.scrollbar = (JScrollBar)c;
        this.uninstallListeners();
        this.uninstallDefaults();
        this.uninstallComponents();
        this.uninstallKeyboardActions();
        this.thumbRect = null;
        this.scrollbar = null;
        this.incrButton = null;
        this.decrButton = null;
    }

    protected void installDefaults() {
        this.scrollBarWidth = UIManager.getInt("ScrollBar.width");
        if (this.scrollBarWidth <= 0) {
            this.scrollBarWidth = 16;
        }
        this.minimumThumbSize = (Dimension)UIManager.get("ScrollBar.minimumThumbSize");
        this.maximumThumbSize = (Dimension)UIManager.get("ScrollBar.maximumThumbSize");
        Boolean absB = (Boolean)UIManager.get("ScrollBar.allowsAbsolutePositioning");
        this.supportsAbsolutePositioning = absB != null ? absB : false;
        this.trackHighlight = 0;
        if (this.scrollbar.getLayout() == null || this.scrollbar.getLayout() instanceof UIResource) {
            this.scrollbar.setLayout(this);
        }
        this.configureScrollBarColors();
        LookAndFeel.installBorder(this.scrollbar, "ScrollBar.border");
        LookAndFeel.installProperty(this.scrollbar, "opaque", Boolean.TRUE);
        this.scrollBarValue = this.scrollbar.getValue();
        this.incrGap = UIManager.getInt("ScrollBar.incrementButtonGap");
        this.decrGap = UIManager.getInt("ScrollBar.decrementButtonGap");
        String scaleKey = (String)this.scrollbar.getClientProperty("JComponent.sizeVariant");
        if (scaleKey != null) {
            if ("large".equals(scaleKey)) {
                this.scrollBarWidth = (int)((double)this.scrollBarWidth * 1.15);
                this.incrGap = (int)((double)this.incrGap * 1.15);
                this.decrGap = (int)((double)this.decrGap * 1.15);
            } else if ("small".equals(scaleKey)) {
                this.scrollBarWidth = (int)((double)this.scrollBarWidth * 0.857);
                this.incrGap = (int)((double)this.incrGap * 0.857);
                this.decrGap = (int)((double)this.decrGap * 0.857);
            } else if ("mini".equals(scaleKey)) {
                this.scrollBarWidth = (int)((double)this.scrollBarWidth * 0.714);
                this.incrGap = (int)((double)this.incrGap * 0.714);
                this.decrGap = (int)((double)this.decrGap * 0.714);
            }
        }
    }

    protected void installComponents() {
        switch (this.scrollbar.getOrientation()) {
            case 1: {
                this.incrButton = this.createIncreaseButton(5);
                this.decrButton = this.createDecreaseButton(1);
                break;
            }
            case 0: {
                if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
                    this.incrButton = this.createIncreaseButton(3);
                    this.decrButton = this.createDecreaseButton(7);
                    break;
                }
                this.incrButton = this.createIncreaseButton(7);
                this.decrButton = this.createDecreaseButton(3);
            }
        }
        this.scrollbar.add(this.incrButton);
        this.scrollbar.add(this.decrButton);
        this.scrollbar.setEnabled(this.scrollbar.isEnabled());
    }

    protected void uninstallComponents() {
        this.scrollbar.remove(this.incrButton);
        this.scrollbar.remove(this.decrButton);
    }

    protected void installListeners() {
        this.trackListener = this.createTrackListener();
        this.buttonListener = this.createArrowButtonListener();
        this.modelListener = this.createModelListener();
        this.propertyChangeListener = this.createPropertyChangeListener();
        this.scrollbar.addMouseListener(this.trackListener);
        this.scrollbar.addMouseMotionListener(this.trackListener);
        this.scrollbar.getModel().addChangeListener(this.modelListener);
        this.scrollbar.addPropertyChangeListener(this.propertyChangeListener);
        this.scrollbar.addFocusListener(this.getHandler());
        if (this.incrButton != null) {
            this.incrButton.addMouseListener(this.buttonListener);
        }
        if (this.decrButton != null) {
            this.decrButton.addMouseListener(this.buttonListener);
        }
        this.scrollListener = this.createScrollListener();
        this.scrollTimer = new Timer(60, this.scrollListener);
        this.scrollTimer.setInitialDelay(300);
    }

    protected void installKeyboardActions() {
        LazyActionMap.installLazyActionMap(this.scrollbar, BasicScrollBarUI.class, "ScrollBar.actionMap");
        InputMap inputMap = this.getInputMap(0);
        SwingUtilities.replaceUIInputMap(this.scrollbar, 0, inputMap);
        inputMap = this.getInputMap(1);
        SwingUtilities.replaceUIInputMap(this.scrollbar, 1, inputMap);
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(this.scrollbar, 0, null);
        SwingUtilities.replaceUIActionMap(this.scrollbar, null);
    }

    private InputMap getInputMap(int condition) {
        if (condition == 0) {
            InputMap rtlKeyMap;
            InputMap keyMap = (InputMap)DefaultLookup.get(this.scrollbar, this, "ScrollBar.focusInputMap");
            if (this.scrollbar.getComponentOrientation().isLeftToRight() || (rtlKeyMap = (InputMap)DefaultLookup.get(this.scrollbar, this, "ScrollBar.focusInputMap.RightToLeft")) == null) {
                return keyMap;
            }
            rtlKeyMap.setParent(keyMap);
            return rtlKeyMap;
        }
        if (condition == 1) {
            InputMap rtlKeyMap;
            InputMap keyMap = (InputMap)DefaultLookup.get(this.scrollbar, this, "ScrollBar.ancestorInputMap");
            if (this.scrollbar.getComponentOrientation().isLeftToRight() || (rtlKeyMap = (InputMap)DefaultLookup.get(this.scrollbar, this, "ScrollBar.ancestorInputMap.RightToLeft")) == null) {
                return keyMap;
            }
            rtlKeyMap.setParent(keyMap);
            return rtlKeyMap;
        }
        return null;
    }

    protected void uninstallListeners() {
        this.scrollTimer.stop();
        this.scrollTimer = null;
        if (this.decrButton != null) {
            this.decrButton.removeMouseListener(this.buttonListener);
        }
        if (this.incrButton != null) {
            this.incrButton.removeMouseListener(this.buttonListener);
        }
        this.scrollbar.getModel().removeChangeListener(this.modelListener);
        this.scrollbar.removeMouseListener(this.trackListener);
        this.scrollbar.removeMouseMotionListener(this.trackListener);
        this.scrollbar.removePropertyChangeListener(this.propertyChangeListener);
        this.scrollbar.removeFocusListener(this.getHandler());
        this.handler = null;
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(this.scrollbar);
        if (this.scrollbar.getLayout() == this) {
            this.scrollbar.setLayout(null);
        }
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected TrackListener createTrackListener() {
        return new TrackListener();
    }

    protected ArrowButtonListener createArrowButtonListener() {
        return new ArrowButtonListener();
    }

    protected ModelListener createModelListener() {
        return new ModelListener();
    }

    protected ScrollListener createScrollListener() {
        return new ScrollListener();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    private void updateThumbState(int x, int y) {
        Rectangle rect = this.getThumbBounds();
        this.setThumbRollover(rect.contains(x, y));
    }

    protected void setThumbRollover(boolean active) {
        if (this.thumbActive != active) {
            this.thumbActive = active;
            this.scrollbar.repaint(this.getThumbBounds());
        }
    }

    public boolean isThumbRollover() {
        return this.thumbActive;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        this.paintTrack(g, c, this.getTrackBounds());
        Rectangle thumbBounds = this.getThumbBounds();
        if (thumbBounds.intersects(g.getClipBounds())) {
            this.paintThumb(g, c, thumbBounds);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return this.scrollbar.getOrientation() == 1 ? new Dimension(this.scrollBarWidth, 48) : new Dimension(48, this.scrollBarWidth);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    protected JButton createDecreaseButton(int orientation) {
        return new BasicArrowButton(orientation, UIManager.getColor("ScrollBar.thumb"), UIManager.getColor("ScrollBar.thumbShadow"), UIManager.getColor("ScrollBar.thumbDarkShadow"), UIManager.getColor("ScrollBar.thumbHighlight"));
    }

    protected JButton createIncreaseButton(int orientation) {
        return new BasicArrowButton(orientation, UIManager.getColor("ScrollBar.thumb"), UIManager.getColor("ScrollBar.thumbShadow"), UIManager.getColor("ScrollBar.thumbDarkShadow"), UIManager.getColor("ScrollBar.thumbHighlight"));
    }

    protected void paintDecreaseHighlight(Graphics g) {
        Insets insets = this.scrollbar.getInsets();
        Rectangle thumbR = this.getThumbBounds();
        g.setColor(this.trackHighlightColor);
        if (this.scrollbar.getOrientation() == 1) {
            int x = insets.left;
            int y = this.trackRect.y;
            int w = this.scrollbar.getWidth() - (insets.left + insets.right);
            int h = thumbR.y - y;
            g.fillRect(x, y, w, h);
        } else {
            int w;
            int x;
            if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
                x = this.trackRect.x;
                w = thumbR.x - x;
            } else {
                x = thumbR.x + thumbR.width;
                w = this.trackRect.x + this.trackRect.width - x;
            }
            int y = insets.top;
            int h = this.scrollbar.getHeight() - (insets.top + insets.bottom);
            g.fillRect(x, y, w, h);
        }
    }

    protected void paintIncreaseHighlight(Graphics g) {
        Insets insets = this.scrollbar.getInsets();
        Rectangle thumbR = this.getThumbBounds();
        g.setColor(this.trackHighlightColor);
        if (this.scrollbar.getOrientation() == 1) {
            int x = insets.left;
            int y = thumbR.y + thumbR.height;
            int w = this.scrollbar.getWidth() - (insets.left + insets.right);
            int h = this.trackRect.y + this.trackRect.height - y;
            g.fillRect(x, y, w, h);
        } else {
            int w;
            int x;
            if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
                x = thumbR.x + thumbR.width;
                w = this.trackRect.x + this.trackRect.width - x;
            } else {
                x = this.trackRect.x;
                w = thumbR.x - x;
            }
            int y = insets.top;
            int h = this.scrollbar.getHeight() - (insets.top + insets.bottom);
            g.fillRect(x, y, w, h);
        }
    }

    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(this.trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        if (this.trackHighlight == 1) {
            this.paintDecreaseHighlight(g);
        } else if (this.trackHighlight == 2) {
            this.paintIncreaseHighlight(g);
        }
    }

    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !this.scrollbar.isEnabled()) {
            return;
        }
        int w = thumbBounds.width;
        int h = thumbBounds.height;
        g.translate(thumbBounds.x, thumbBounds.y);
        g.setColor(this.thumbDarkShadowColor);
        SwingUtilities2.drawRect(g, 0, 0, w - 1, h - 1);
        g.setColor(this.thumbColor);
        g.fillRect(0, 0, w - 1, h - 1);
        g.setColor(this.thumbHighlightColor);
        SwingUtilities2.drawVLine(g, 1, 1, h - 2);
        SwingUtilities2.drawHLine(g, 2, w - 3, 1);
        g.setColor(this.thumbLightShadowColor);
        SwingUtilities2.drawHLine(g, 2, w - 2, h - 2);
        SwingUtilities2.drawVLine(g, w - 2, 1, h - 3);
        g.translate(-thumbBounds.x, -thumbBounds.y);
    }

    protected Dimension getMinimumThumbSize() {
        return this.minimumThumbSize;
    }

    protected Dimension getMaximumThumbSize() {
        return this.maximumThumbSize;
    }

    @Override
    public void addLayoutComponent(String name, Component child) {
    }

    @Override
    public void removeLayoutComponent(Component child) {
    }

    @Override
    public Dimension preferredLayoutSize(Container scrollbarContainer) {
        return this.getPreferredSize((JComponent)scrollbarContainer);
    }

    @Override
    public Dimension minimumLayoutSize(Container scrollbarContainer) {
        return this.getMinimumSize((JComponent)scrollbarContainer);
    }

    private int getValue(JScrollBar sb) {
        return this.useCachedValue ? this.scrollBarValue : sb.getValue();
    }

    protected void layoutVScrollbar(JScrollBar sb) {
        int sbAvailButtonH;
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();
        int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
        int itemX = sbInsets.left;
        boolean squareButtons = DefaultLookup.getBoolean(this.scrollbar, this, "ScrollBar.squareButtons", false);
        int decrButtonH = squareButtons ? itemW : this.decrButton.getPreferredSize().height;
        int decrButtonY = sbInsets.top;
        int incrButtonH = squareButtons ? itemW : this.incrButton.getPreferredSize().height;
        int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;
        int gaps = this.decrGap + this.incrGap;
        float trackH = sbSize.height - (sbInsetsH + sbButtonsH) - gaps;
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = (float)sb.getMaximum() - min;
        float value = this.getValue(sb);
        int thumbH = range <= 0.0f ? this.getMaximumThumbSize().height : (int)(trackH * (extent / range));
        thumbH = Math.max(thumbH, this.getMinimumThumbSize().height);
        thumbH = Math.min(thumbH, this.getMaximumThumbSize().height);
        int thumbY = incrButtonY - this.incrGap - thumbH;
        if (value < (float)(sb.getMaximum() - sb.getVisibleAmount())) {
            float thumbRange = trackH - (float)thumbH;
            thumbY = (int)(0.5f + thumbRange * ((value - min) / (range - extent)));
            thumbY += decrButtonY + decrButtonH + this.decrGap;
        }
        if ((sbAvailButtonH = sbSize.height - sbInsetsH) < sbButtonsH) {
            incrButtonH = decrButtonH = sbAvailButtonH / 2;
            incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        }
        this.decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        this.incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);
        int itrackY = decrButtonY + decrButtonH + this.decrGap;
        int itrackH = incrButtonY - this.incrGap - itrackY;
        this.trackRect.setBounds(itemX, itrackY, itemW, itrackH);
        if (thumbH >= (int)trackH) {
            if (UIManager.getBoolean("ScrollBar.alwaysShowThumb")) {
                this.setThumbBounds(itemX, itrackY, itemW, itrackH);
            } else {
                this.setThumbBounds(0, 0, 0, 0);
            }
        } else {
            if (thumbY + thumbH > incrButtonY - this.incrGap) {
                thumbY = incrButtonY - this.incrGap - thumbH;
            }
            if (thumbY < decrButtonY + decrButtonH + this.decrGap) {
                thumbY = decrButtonY + decrButtonH + this.decrGap + 1;
            }
            this.setThumbBounds(itemX, thumbY, itemW, thumbH);
        }
    }

    protected void layoutHScrollbar(JScrollBar sb) {
        int sbAvailButtonW;
        int thumbX;
        int rightButtonW;
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();
        int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
        int itemY = sbInsets.top;
        boolean ltr = sb.getComponentOrientation().isLeftToRight();
        boolean squareButtons = DefaultLookup.getBoolean(this.scrollbar, this, "ScrollBar.squareButtons", false);
        int leftButtonW = squareButtons ? itemH : this.decrButton.getPreferredSize().width;
        int n = rightButtonW = squareButtons ? itemH : this.incrButton.getPreferredSize().width;
        if (!ltr) {
            int temp = leftButtonW;
            leftButtonW = rightButtonW;
            rightButtonW = temp;
        }
        int leftButtonX = sbInsets.left;
        int rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);
        int leftGap = ltr ? this.decrGap : this.incrGap;
        int rightGap = ltr ? this.incrGap : this.decrGap;
        int sbInsetsW = sbInsets.left + sbInsets.right;
        int sbButtonsW = leftButtonW + rightButtonW;
        float trackW = sbSize.width - (sbInsetsW + sbButtonsW) - (leftGap + rightGap);
        float min = sb.getMinimum();
        float max = sb.getMaximum();
        float extent = sb.getVisibleAmount();
        float range = max - min;
        float value = this.getValue(sb);
        int thumbW = range <= 0.0f ? this.getMaximumThumbSize().width : (int)(trackW * (extent / range));
        thumbW = Math.max(thumbW, this.getMinimumThumbSize().width);
        thumbW = Math.min(thumbW, this.getMaximumThumbSize().width);
        int n2 = thumbX = ltr ? rightButtonX - rightGap - thumbW : leftButtonX + leftButtonW + leftGap;
        if (value < max - (float)sb.getVisibleAmount()) {
            float thumbRange = trackW - (float)thumbW;
            thumbX = ltr ? (int)(0.5f + thumbRange * ((value - min) / (range - extent))) : (int)(0.5f + thumbRange * ((max - extent - value) / (range - extent)));
            thumbX += leftButtonX + leftButtonW + leftGap;
        }
        if ((sbAvailButtonW = sbSize.width - sbInsetsW) < sbButtonsW) {
            rightButtonW = leftButtonW = sbAvailButtonW / 2;
            rightButtonX = sbSize.width - (sbInsets.right + rightButtonW + rightGap);
        }
        (ltr ? this.decrButton : this.incrButton).setBounds(leftButtonX, itemY, leftButtonW, itemH);
        (ltr ? this.incrButton : this.decrButton).setBounds(rightButtonX, itemY, rightButtonW, itemH);
        int itrackX = leftButtonX + leftButtonW + leftGap;
        int itrackW = rightButtonX - rightGap - itrackX;
        this.trackRect.setBounds(itrackX, itemY, itrackW, itemH);
        if (thumbW >= (int)trackW) {
            if (UIManager.getBoolean("ScrollBar.alwaysShowThumb")) {
                this.setThumbBounds(itrackX, itemY, itrackW, itemH);
            } else {
                this.setThumbBounds(0, 0, 0, 0);
            }
        } else {
            if (thumbX + thumbW > rightButtonX - rightGap) {
                thumbX = rightButtonX - rightGap - thumbW;
            }
            if (thumbX < leftButtonX + leftButtonW + leftGap) {
                thumbX = leftButtonX + leftButtonW + leftGap + 1;
            }
            this.setThumbBounds(thumbX, itemY, thumbW, itemH);
        }
    }

    @Override
    public void layoutContainer(Container scrollbarContainer) {
        if (this.isDragging) {
            return;
        }
        JScrollBar scrollbar = (JScrollBar)scrollbarContainer;
        switch (scrollbar.getOrientation()) {
            case 1: {
                this.layoutVScrollbar(scrollbar);
                break;
            }
            case 0: {
                this.layoutHScrollbar(scrollbar);
            }
        }
    }

    protected void setThumbBounds(int x, int y, int width, int height) {
        if (this.thumbRect.x == x && this.thumbRect.y == y && this.thumbRect.width == width && this.thumbRect.height == height) {
            return;
        }
        int minX = Math.min(x, this.thumbRect.x);
        int minY = Math.min(y, this.thumbRect.y);
        int maxX = Math.max(x + width, this.thumbRect.x + this.thumbRect.width);
        int maxY = Math.max(y + height, this.thumbRect.y + this.thumbRect.height);
        this.thumbRect.setBounds(x, y, width, height);
        this.scrollbar.repaint(minX, minY, maxX - minX, maxY - minY);
        this.setThumbRollover(false);
    }

    protected Rectangle getThumbBounds() {
        return this.thumbRect;
    }

    protected Rectangle getTrackBounds() {
        return this.trackRect;
    }

    static void scrollByBlock(JScrollBar scrollbar, int direction) {
        int oldValue = scrollbar.getValue();
        int blockIncrement = scrollbar.getBlockIncrement(direction);
        int delta = blockIncrement * (direction > 0 ? 1 : -1);
        int newValue = oldValue + delta;
        if (delta > 0 && newValue < oldValue) {
            newValue = scrollbar.getMaximum();
        } else if (delta < 0 && newValue > oldValue) {
            newValue = scrollbar.getMinimum();
        }
        scrollbar.setValue(newValue);
    }

    protected void scrollByBlock(int direction) {
        BasicScrollBarUI.scrollByBlock(this.scrollbar, direction);
        this.trackHighlight = direction > 0 ? 2 : 1;
        Rectangle dirtyRect = this.getTrackBounds();
        this.scrollbar.repaint(dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
    }

    static void scrollByUnits(JScrollBar scrollbar, int direction, int units, boolean limitToBlock) {
        int limit = -1;
        if (limitToBlock) {
            limit = direction < 0 ? scrollbar.getValue() - scrollbar.getBlockIncrement(direction) : scrollbar.getValue() + scrollbar.getBlockIncrement(direction);
        }
        for (int i = 0; i < units; ++i) {
            int delta = direction > 0 ? scrollbar.getUnitIncrement(direction) : -scrollbar.getUnitIncrement(direction);
            int oldValue = scrollbar.getValue();
            int newValue = oldValue + delta;
            if (delta > 0 && newValue < oldValue) {
                newValue = scrollbar.getMaximum();
            } else if (delta < 0 && newValue > oldValue) {
                newValue = scrollbar.getMinimum();
            }
            if (oldValue == newValue) break;
            if (limitToBlock && i > 0) {
                assert (limit != -1);
                if (direction < 0 && newValue < limit || direction > 0 && newValue > limit) break;
            }
            scrollbar.setValue(newValue);
        }
    }

    protected void scrollByUnit(int direction) {
        BasicScrollBarUI.scrollByUnits(this.scrollbar, direction, 1, false);
    }

    public boolean getSupportsAbsolutePositioning() {
        return this.supportsAbsolutePositioning;
    }

    private boolean isMouseLeftOfThumb() {
        return this.trackListener.currentMouseX < this.getThumbBounds().x;
    }

    private boolean isMouseRightOfThumb() {
        Rectangle tb = this.getThumbBounds();
        return this.trackListener.currentMouseX > tb.x + tb.width;
    }

    private boolean isMouseBeforeThumb() {
        return this.scrollbar.getComponentOrientation().isLeftToRight() ? this.isMouseLeftOfThumb() : this.isMouseRightOfThumb();
    }

    private boolean isMouseAfterThumb() {
        return this.scrollbar.getComponentOrientation().isLeftToRight() ? this.isMouseRightOfThumb() : this.isMouseLeftOfThumb();
    }

    private void updateButtonDirections() {
        int orient = this.scrollbar.getOrientation();
        if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
            if (this.incrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)this.incrButton).setDirection(orient == 0 ? 3 : 5);
            }
            if (this.decrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)this.decrButton).setDirection(orient == 0 ? 7 : 1);
            }
        } else {
            if (this.incrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)this.incrButton).setDirection(orient == 0 ? 7 : 5);
            }
            if (this.decrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)this.decrButton).setDirection(orient == 0 ? 3 : 1);
            }
        }
    }

    private void setDragging(boolean dragging) {
        this.isDragging = dragging;
        this.scrollbar.repaint(this.getThumbBounds());
    }

    private static class Actions
    extends UIAction {
        private static final String POSITIVE_UNIT_INCREMENT = "positiveUnitIncrement";
        private static final String POSITIVE_BLOCK_INCREMENT = "positiveBlockIncrement";
        private static final String NEGATIVE_UNIT_INCREMENT = "negativeUnitIncrement";
        private static final String NEGATIVE_BLOCK_INCREMENT = "negativeBlockIncrement";
        private static final String MIN_SCROLL = "minScroll";
        private static final String MAX_SCROLL = "maxScroll";

        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JScrollBar scrollBar = (JScrollBar)e.getSource();
            String key = this.getName();
            if (key == POSITIVE_UNIT_INCREMENT) {
                this.scroll(scrollBar, 1, false);
            } else if (key == POSITIVE_BLOCK_INCREMENT) {
                this.scroll(scrollBar, 1, true);
            } else if (key == NEGATIVE_UNIT_INCREMENT) {
                this.scroll(scrollBar, -1, false);
            } else if (key == NEGATIVE_BLOCK_INCREMENT) {
                this.scroll(scrollBar, -1, true);
            } else if (key == MIN_SCROLL) {
                this.scroll(scrollBar, 2, true);
            } else if (key == MAX_SCROLL) {
                this.scroll(scrollBar, 3, true);
            }
        }

        private void scroll(JScrollBar scrollBar, int dir, boolean block) {
            if (dir == -1 || dir == 1) {
                int amount = block ? (dir == -1 ? -1 * scrollBar.getBlockIncrement(-1) : scrollBar.getBlockIncrement(1)) : (dir == -1 ? -1 * scrollBar.getUnitIncrement(-1) : scrollBar.getUnitIncrement(1));
                scrollBar.setValue(scrollBar.getValue() + amount);
            } else if (dir == 2) {
                scrollBar.setValue(scrollBar.getMinimum());
            } else if (dir == 3) {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        }
    }

    protected class TrackListener
    extends MouseAdapter
    implements MouseMotionListener {
        protected transient int offset;
        protected transient int currentMouseX;
        protected transient int currentMouseY;
        private transient int direction = 1;

        protected TrackListener() {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (BasicScrollBarUI.this.isDragging) {
                BasicScrollBarUI.this.updateThumbState(e.getX(), e.getY());
            }
            if (SwingUtilities.isRightMouseButton(e) || !BasicScrollBarUI.this.getSupportsAbsolutePositioning() && SwingUtilities.isMiddleMouseButton(e)) {
                return;
            }
            if (!BasicScrollBarUI.this.scrollbar.isEnabled()) {
                return;
            }
            Rectangle r = BasicScrollBarUI.this.getTrackBounds();
            BasicScrollBarUI.this.scrollbar.repaint(r.x, r.y, r.width, r.height);
            BasicScrollBarUI.this.trackHighlight = 0;
            BasicScrollBarUI.this.setDragging(false);
            this.offset = 0;
            BasicScrollBarUI.this.scrollTimer.stop();
            BasicScrollBarUI.this.useCachedValue = true;
            BasicScrollBarUI.this.scrollbar.setValueIsAdjusting(false);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) || !BasicScrollBarUI.this.getSupportsAbsolutePositioning() && SwingUtilities.isMiddleMouseButton(e)) {
                return;
            }
            if (!BasicScrollBarUI.this.scrollbar.isEnabled()) {
                return;
            }
            if (!BasicScrollBarUI.this.scrollbar.hasFocus() && BasicScrollBarUI.this.scrollbar.isRequestFocusEnabled()) {
                BasicScrollBarUI.this.scrollbar.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
            BasicScrollBarUI.this.useCachedValue = true;
            BasicScrollBarUI.this.scrollbar.setValueIsAdjusting(true);
            this.currentMouseX = e.getX();
            this.currentMouseY = e.getY();
            if (BasicScrollBarUI.this.getThumbBounds().contains(this.currentMouseX, this.currentMouseY)) {
                switch (BasicScrollBarUI.this.scrollbar.getOrientation()) {
                    case 1: {
                        this.offset = this.currentMouseY - BasicScrollBarUI.this.getThumbBounds().y;
                        break;
                    }
                    case 0: {
                        this.offset = this.currentMouseX - BasicScrollBarUI.this.getThumbBounds().x;
                    }
                }
                BasicScrollBarUI.this.setDragging(true);
                return;
            }
            if (BasicScrollBarUI.this.getSupportsAbsolutePositioning() && SwingUtilities.isMiddleMouseButton(e)) {
                switch (BasicScrollBarUI.this.scrollbar.getOrientation()) {
                    case 1: {
                        this.offset = BasicScrollBarUI.this.getThumbBounds().height / 2;
                        break;
                    }
                    case 0: {
                        this.offset = BasicScrollBarUI.this.getThumbBounds().width / 2;
                    }
                }
                BasicScrollBarUI.this.setDragging(true);
                this.setValueFrom(e);
                return;
            }
            BasicScrollBarUI.this.setDragging(false);
            Dimension sbSize = BasicScrollBarUI.this.scrollbar.getSize();
            this.direction = 1;
            switch (BasicScrollBarUI.this.scrollbar.getOrientation()) {
                case 1: {
                    if (BasicScrollBarUI.this.getThumbBounds().isEmpty()) {
                        int scrollbarCenter = sbSize.height / 2;
                        this.direction = this.currentMouseY < scrollbarCenter ? -1 : 1;
                        break;
                    }
                    int thumbY = BasicScrollBarUI.this.getThumbBounds().y;
                    this.direction = this.currentMouseY < thumbY ? -1 : 1;
                    break;
                }
                case 0: {
                    if (BasicScrollBarUI.this.getThumbBounds().isEmpty()) {
                        int scrollbarCenter = sbSize.width / 2;
                        this.direction = this.currentMouseX < scrollbarCenter ? -1 : 1;
                    } else {
                        int thumbX = BasicScrollBarUI.this.getThumbBounds().x;
                        int n = this.direction = this.currentMouseX < thumbX ? -1 : 1;
                    }
                    if (BasicScrollBarUI.this.scrollbar.getComponentOrientation().isLeftToRight()) break;
                    this.direction = -this.direction;
                }
            }
            BasicScrollBarUI.this.scrollByBlock(this.direction);
            BasicScrollBarUI.this.scrollTimer.stop();
            BasicScrollBarUI.this.scrollListener.setDirection(this.direction);
            BasicScrollBarUI.this.scrollListener.setScrollByBlock(true);
            this.startScrollTimerIfNecessary();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) || !BasicScrollBarUI.this.getSupportsAbsolutePositioning() && SwingUtilities.isMiddleMouseButton(e)) {
                return;
            }
            if (!BasicScrollBarUI.this.scrollbar.isEnabled() || BasicScrollBarUI.this.getThumbBounds().isEmpty()) {
                return;
            }
            if (BasicScrollBarUI.this.isDragging) {
                this.setValueFrom(e);
            } else {
                this.currentMouseX = e.getX();
                this.currentMouseY = e.getY();
                BasicScrollBarUI.this.updateThumbState(this.currentMouseX, this.currentMouseY);
                this.startScrollTimerIfNecessary();
            }
        }

        private void setValueFrom(MouseEvent e) {
            int thumbPos;
            int thumbMax;
            int thumbMin;
            boolean active = BasicScrollBarUI.this.isThumbRollover();
            BoundedRangeModel model = BasicScrollBarUI.this.scrollbar.getModel();
            Rectangle thumbR = BasicScrollBarUI.this.getThumbBounds();
            if (BasicScrollBarUI.this.scrollbar.getOrientation() == 1) {
                thumbMin = BasicScrollBarUI.this.trackRect.y;
                thumbMax = BasicScrollBarUI.this.trackRect.y + BasicScrollBarUI.this.trackRect.height - thumbR.height;
                thumbPos = Math.min(thumbMax, Math.max(thumbMin, e.getY() - this.offset));
                BasicScrollBarUI.this.setThumbBounds(thumbR.x, thumbPos, thumbR.width, thumbR.height);
                float trackLength = BasicScrollBarUI.this.getTrackBounds().height;
            } else {
                thumbMin = BasicScrollBarUI.this.trackRect.x;
                thumbMax = BasicScrollBarUI.this.trackRect.x + BasicScrollBarUI.this.trackRect.width - thumbR.width;
                thumbPos = Math.min(thumbMax, Math.max(thumbMin, e.getX() - this.offset));
                BasicScrollBarUI.this.setThumbBounds(thumbPos, thumbR.y, thumbR.width, thumbR.height);
                float trackLength = BasicScrollBarUI.this.getTrackBounds().width;
            }
            if (thumbPos == thumbMax) {
                if (BasicScrollBarUI.this.scrollbar.getOrientation() == 1 || BasicScrollBarUI.this.scrollbar.getComponentOrientation().isLeftToRight()) {
                    BasicScrollBarUI.this.scrollbar.setValue(model.getMaximum() - model.getExtent());
                } else {
                    BasicScrollBarUI.this.scrollbar.setValue(model.getMinimum());
                }
            } else {
                float valueMax = model.getMaximum() - model.getExtent();
                float valueRange = valueMax - (float)model.getMinimum();
                float thumbValue = thumbPos - thumbMin;
                float thumbRange = thumbMax - thumbMin;
                int value = BasicScrollBarUI.this.scrollbar.getOrientation() == 1 || BasicScrollBarUI.this.scrollbar.getComponentOrientation().isLeftToRight() ? (int)(0.5 + (double)(thumbValue / thumbRange * valueRange)) : (int)(0.5 + (double)((float)(thumbMax - thumbPos) / thumbRange * valueRange));
                BasicScrollBarUI.this.useCachedValue = true;
                BasicScrollBarUI.this.scrollBarValue = value + model.getMinimum();
                BasicScrollBarUI.this.scrollbar.setValue(this.adjustValueIfNecessary(BasicScrollBarUI.this.scrollBarValue));
            }
            BasicScrollBarUI.this.setThumbRollover(active);
        }

        private int adjustValueIfNecessary(int value) {
            JList list;
            JScrollPane scrollpane;
            JViewport viewport;
            Component view;
            if (BasicScrollBarUI.this.scrollbar.getParent() instanceof JScrollPane && (view = (viewport = (scrollpane = (JScrollPane)BasicScrollBarUI.this.scrollbar.getParent()).getViewport()).getView()) instanceof JList && DefaultLookup.getBoolean(list = (JList)view, list.getUI(), "List.lockToPositionOnScroll", false)) {
                int index;
                Rectangle rect;
                int adjustedValue = value;
                int mode = list.getLayoutOrientation();
                int orientation = BasicScrollBarUI.this.scrollbar.getOrientation();
                if (orientation == 1 && mode == 0 && (rect = list.getCellBounds(index = list.locationToIndex(new Point(0, value)), index)) != null) {
                    adjustedValue = rect.y;
                }
                if (orientation == 0 && (mode == 1 || mode == 2)) {
                    if (scrollpane.getComponentOrientation().isLeftToRight()) {
                        index = list.locationToIndex(new Point(value, 0));
                        rect = list.getCellBounds(index, index);
                        if (rect != null) {
                            adjustedValue = rect.x;
                        }
                    } else {
                        Point loc = new Point(value, 0);
                        int extent = viewport.getExtentSize().width;
                        loc.x += extent - 1;
                        int index2 = list.locationToIndex(loc);
                        Rectangle rect2 = list.getCellBounds(index2, index2);
                        if (rect2 != null) {
                            adjustedValue = rect2.x + rect2.width - extent;
                        }
                    }
                }
                value = adjustedValue;
            }
            return value;
        }

        private void startScrollTimerIfNecessary() {
            if (BasicScrollBarUI.this.scrollTimer.isRunning()) {
                return;
            }
            Rectangle tb = BasicScrollBarUI.this.getThumbBounds();
            switch (BasicScrollBarUI.this.scrollbar.getOrientation()) {
                case 1: {
                    if (this.direction > 0) {
                        if (tb.y + tb.height >= BasicScrollBarUI.this.trackListener.currentMouseY) break;
                        BasicScrollBarUI.this.scrollTimer.start();
                        break;
                    }
                    if (tb.y <= BasicScrollBarUI.this.trackListener.currentMouseY) break;
                    BasicScrollBarUI.this.scrollTimer.start();
                    break;
                }
                case 0: {
                    if ((this.direction <= 0 || !BasicScrollBarUI.this.isMouseAfterThumb()) && (this.direction >= 0 || !BasicScrollBarUI.this.isMouseBeforeThumb())) break;
                    BasicScrollBarUI.this.scrollTimer.start();
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!BasicScrollBarUI.this.isDragging) {
                BasicScrollBarUI.this.updateThumbState(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!BasicScrollBarUI.this.isDragging) {
                BasicScrollBarUI.this.setThumbRollover(false);
            }
        }
    }

    protected class ArrowButtonListener
    extends MouseAdapter {
        boolean handledEvent;

        protected ArrowButtonListener() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!BasicScrollBarUI.this.scrollbar.isEnabled()) {
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            int direction = e.getSource() == BasicScrollBarUI.this.incrButton ? 1 : -1;
            BasicScrollBarUI.this.scrollByUnit(direction);
            BasicScrollBarUI.this.scrollTimer.stop();
            BasicScrollBarUI.this.scrollListener.setDirection(direction);
            BasicScrollBarUI.this.scrollListener.setScrollByBlock(false);
            BasicScrollBarUI.this.scrollTimer.start();
            this.handledEvent = true;
            if (!BasicScrollBarUI.this.scrollbar.hasFocus() && BasicScrollBarUI.this.scrollbar.isRequestFocusEnabled()) {
                BasicScrollBarUI.this.scrollbar.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            BasicScrollBarUI.this.scrollTimer.stop();
            this.handledEvent = false;
            BasicScrollBarUI.this.scrollbar.setValueIsAdjusting(false);
        }
    }

    protected class ModelListener
    implements ChangeListener {
        protected ModelListener() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!BasicScrollBarUI.this.useCachedValue) {
                BasicScrollBarUI.this.scrollBarValue = BasicScrollBarUI.this.scrollbar.getValue();
            }
            BasicScrollBarUI.this.layoutContainer(BasicScrollBarUI.this.scrollbar);
            BasicScrollBarUI.this.useCachedValue = false;
        }
    }

    private class Handler
    implements FocusListener,
    PropertyChangeListener {
        private Handler() {
        }

        @Override
        public void focusGained(FocusEvent e) {
            BasicScrollBarUI.this.scrollbar.repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicScrollBarUI.this.scrollbar.repaint();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ("model" == propertyName) {
                BoundedRangeModel oldModel = (BoundedRangeModel)e.getOldValue();
                BoundedRangeModel newModel = (BoundedRangeModel)e.getNewValue();
                oldModel.removeChangeListener(BasicScrollBarUI.this.modelListener);
                newModel.addChangeListener(BasicScrollBarUI.this.modelListener);
                BasicScrollBarUI.this.scrollBarValue = BasicScrollBarUI.this.scrollbar.getValue();
                BasicScrollBarUI.this.scrollbar.repaint();
                BasicScrollBarUI.this.scrollbar.revalidate();
            } else if ("orientation" == propertyName) {
                BasicScrollBarUI.this.updateButtonDirections();
            } else if ("componentOrientation" == propertyName) {
                BasicScrollBarUI.this.updateButtonDirections();
                InputMap inputMap = BasicScrollBarUI.this.getInputMap(0);
                SwingUtilities.replaceUIInputMap(BasicScrollBarUI.this.scrollbar, 0, inputMap);
            }
        }
    }

    protected class ScrollListener
    implements ActionListener {
        int direction = 1;
        boolean useBlockIncrement;

        public ScrollListener() {
            this.direction = 1;
            this.useBlockIncrement = false;
        }

        public ScrollListener(int dir, boolean block) {
            this.direction = dir;
            this.useBlockIncrement = block;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public void setScrollByBlock(boolean block) {
            this.useBlockIncrement = block;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.useBlockIncrement) {
                BasicScrollBarUI.this.scrollByBlock(this.direction);
                if (BasicScrollBarUI.this.scrollbar.getOrientation() == 1) {
                    if (this.direction > 0) {
                        if (BasicScrollBarUI.this.getThumbBounds().y + BasicScrollBarUI.this.getThumbBounds().height >= BasicScrollBarUI.this.trackListener.currentMouseY) {
                            ((Timer)e.getSource()).stop();
                        }
                    } else if (BasicScrollBarUI.this.getThumbBounds().y <= BasicScrollBarUI.this.trackListener.currentMouseY) {
                        ((Timer)e.getSource()).stop();
                    }
                } else if (this.direction > 0 && !BasicScrollBarUI.this.isMouseAfterThumb() || this.direction < 0 && !BasicScrollBarUI.this.isMouseBeforeThumb()) {
                    ((Timer)e.getSource()).stop();
                }
            } else {
                BasicScrollBarUI.this.scrollByUnit(this.direction);
            }
            if (this.direction > 0 && BasicScrollBarUI.this.scrollbar.getValue() + BasicScrollBarUI.this.scrollbar.getVisibleAmount() >= BasicScrollBarUI.this.scrollbar.getMaximum()) {
                ((Timer)e.getSource()).stop();
            } else if (this.direction < 0 && BasicScrollBarUI.this.scrollbar.getValue() <= BasicScrollBarUI.this.scrollbar.getMinimum()) {
                ((Timer)e.getSource()).stop();
            }
        }
    }

    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicScrollBarUI.this.getHandler().propertyChange(e);
        }
    }
}

