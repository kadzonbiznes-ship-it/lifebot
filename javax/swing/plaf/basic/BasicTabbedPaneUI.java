/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.LazyActionMap;
import javax.swing.text.View;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicTabbedPaneUI
extends TabbedPaneUI
implements SwingConstants {
    protected JTabbedPane tabPane;
    protected Color highlight;
    protected Color lightHighlight;
    protected Color shadow;
    protected Color darkShadow;
    protected Color focus;
    private Color selectedColor;
    protected int textIconGap;
    protected int tabRunOverlay;
    protected Insets tabInsets;
    protected Insets selectedTabPadInsets;
    protected Insets tabAreaInsets;
    protected Insets contentBorderInsets;
    private boolean tabsOverlapBorder;
    private boolean tabsOpaque = true;
    private boolean contentOpaque = true;
    @Deprecated
    protected KeyStroke upKey;
    @Deprecated
    protected KeyStroke downKey;
    @Deprecated
    protected KeyStroke leftKey;
    @Deprecated
    protected KeyStroke rightKey;
    protected int[] tabRuns = new int[10];
    protected int runCount = 0;
    protected int selectedRun = -1;
    protected Rectangle[] rects = new Rectangle[0];
    protected int maxTabHeight;
    protected int maxTabWidth;
    protected ChangeListener tabChangeListener;
    protected PropertyChangeListener propertyChangeListener;
    protected MouseListener mouseListener;
    protected FocusListener focusListener;
    private Insets currentPadInsets = new Insets(0, 0, 0, 0);
    private Insets currentTabAreaInsets = new Insets(0, 0, 0, 0);
    private Component visibleComponent;
    private Vector<View> htmlViews;
    private Hashtable<Integer, Integer> mnemonicToIndexMap;
    private InputMap mnemonicInputMap;
    private ScrollableTabSupport tabScroller;
    private TabContainer tabContainer;
    protected transient Rectangle calcRect = new Rectangle(0, 0, 0, 0);
    private int focusIndex;
    private Handler handler;
    private int rolloverTabIndex;
    private boolean isRunsDirty;
    private boolean calculatedBaseline;
    private int baseline;
    private static int[] xCropLen = new int[]{1, 1, 0, 0, 1, 1, 2, 2};
    private static int[] yCropLen = new int[]{0, 3, 3, 6, 6, 9, 9, 12};
    private static final int CROP_SEGMENT = 12;

    public static ComponentUI createUI(JComponent c) {
        return new BasicTabbedPaneUI();
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("navigateNext"));
        map.put(new Actions("navigatePrevious"));
        map.put(new Actions("navigateRight"));
        map.put(new Actions("navigateLeft"));
        map.put(new Actions("navigateUp"));
        map.put(new Actions("navigateDown"));
        map.put(new Actions("navigatePageUp"));
        map.put(new Actions("navigatePageDown"));
        map.put(new Actions("requestFocus"));
        map.put(new Actions("requestFocusForVisibleComponent"));
        map.put(new Actions("setSelectedIndex"));
        map.put(new Actions("selectTabWithFocus"));
        map.put(new Actions("scrollTabsForwardAction"));
        map.put(new Actions("scrollTabsBackwardAction"));
    }

    @Override
    public void installUI(JComponent c) {
        this.tabPane = (JTabbedPane)c;
        this.calculatedBaseline = false;
        this.rolloverTabIndex = -1;
        this.focusIndex = -1;
        c.setLayout(this.createLayoutManager());
        this.installComponents();
        this.installDefaults();
        this.installListeners();
        this.installKeyboardActions();
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallKeyboardActions();
        this.uninstallListeners();
        this.uninstallDefaults();
        this.uninstallComponents();
        c.setLayout(null);
        this.tabPane = null;
    }

    protected LayoutManager createLayoutManager() {
        if (this.tabPane.getTabLayoutPolicy() == 1) {
            return new TabbedPaneScrollLayout();
        }
        return new TabbedPaneLayout();
    }

    private boolean scrollableTabLayoutEnabled() {
        return this.tabPane.getLayout() instanceof TabbedPaneScrollLayout;
    }

    protected void installComponents() {
        if (this.scrollableTabLayoutEnabled() && this.tabScroller == null) {
            this.tabScroller = new ScrollableTabSupport(this.tabPane.getTabPlacement());
            this.tabPane.add(this.tabScroller.viewport);
        }
        this.installTabContainer();
    }

    private void installTabContainer() {
        for (int i = 0; i < this.tabPane.getTabCount(); ++i) {
            Component tabComponent = this.tabPane.getTabComponentAt(i);
            if (tabComponent == null) continue;
            if (this.tabContainer == null) {
                this.tabContainer = new TabContainer();
            }
            this.tabContainer.add(tabComponent);
        }
        if (this.tabContainer == null) {
            return;
        }
        if (this.scrollableTabLayoutEnabled()) {
            this.tabScroller.tabPanel.add(this.tabContainer);
        } else {
            this.tabPane.add(this.tabContainer);
        }
    }

    protected JButton createScrollButton(int direction) {
        if (direction != 5 && direction != 1 && direction != 3 && direction != 7) {
            throw new IllegalArgumentException("Direction must be one of: SOUTH, NORTH, EAST or WEST");
        }
        return new ScrollableTabButton(direction);
    }

    protected void uninstallComponents() {
        this.uninstallTabContainer();
        if (this.scrollableTabLayoutEnabled()) {
            this.tabPane.remove(this.tabScroller.viewport);
            this.tabPane.remove(this.tabScroller.scrollForwardButton);
            this.tabPane.remove(this.tabScroller.scrollBackwardButton);
            this.tabScroller = null;
        }
    }

    private void uninstallTabContainer() {
        if (this.tabContainer == null) {
            return;
        }
        this.tabContainer.notifyTabbedPane = false;
        this.tabContainer.removeAll();
        if (this.scrollableTabLayoutEnabled()) {
            this.tabContainer.remove(this.tabScroller.croppedEdge);
            this.tabScroller.tabPanel.remove(this.tabContainer);
        } else {
            this.tabPane.remove(this.tabContainer);
        }
        this.tabContainer = null;
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(this.tabPane, "TabbedPane.background", "TabbedPane.foreground", "TabbedPane.font");
        this.highlight = UIManager.getColor("TabbedPane.light");
        this.lightHighlight = UIManager.getColor("TabbedPane.highlight");
        this.shadow = UIManager.getColor("TabbedPane.shadow");
        this.darkShadow = UIManager.getColor("TabbedPane.darkShadow");
        this.focus = UIManager.getColor("TabbedPane.focus");
        this.selectedColor = UIManager.getColor("TabbedPane.selected");
        this.textIconGap = UIManager.getInt("TabbedPane.textIconGap");
        this.tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
        this.selectedTabPadInsets = UIManager.getInsets("TabbedPane.selectedTabPadInsets");
        this.tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        this.tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
        this.contentBorderInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
        this.tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");
        this.tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
        this.contentOpaque = UIManager.getBoolean("TabbedPane.contentOpaque");
        Object opaque = UIManager.get("TabbedPane.opaque");
        if (opaque == null) {
            opaque = Boolean.FALSE;
        }
        LookAndFeel.installProperty(this.tabPane, "opaque", opaque);
        if (this.tabInsets == null) {
            this.tabInsets = new Insets(0, 4, 1, 4);
        }
        if (this.selectedTabPadInsets == null) {
            this.selectedTabPadInsets = new Insets(2, 2, 2, 1);
        }
        if (this.tabAreaInsets == null) {
            this.tabAreaInsets = new Insets(3, 2, 0, 2);
        }
        if (this.contentBorderInsets == null) {
            this.contentBorderInsets = new Insets(2, 2, 3, 3);
        }
    }

    protected void uninstallDefaults() {
        this.highlight = null;
        this.lightHighlight = null;
        this.shadow = null;
        this.darkShadow = null;
        this.focus = null;
        this.tabInsets = null;
        this.selectedTabPadInsets = null;
        this.tabAreaInsets = null;
        this.contentBorderInsets = null;
    }

    protected void installListeners() {
        Boolean htmlDisabled;
        this.propertyChangeListener = this.createPropertyChangeListener();
        if (this.propertyChangeListener != null) {
            this.tabPane.addPropertyChangeListener(this.propertyChangeListener);
        }
        if ((this.tabChangeListener = this.createChangeListener()) != null) {
            this.tabPane.addChangeListener(this.tabChangeListener);
        }
        if ((this.mouseListener = this.createMouseListener()) != null) {
            this.tabPane.addMouseListener(this.mouseListener);
        }
        this.tabPane.addMouseMotionListener(this.getHandler());
        this.focusListener = this.createFocusListener();
        if (this.focusListener != null) {
            this.tabPane.addFocusListener(this.focusListener);
        }
        this.tabPane.addContainerListener(this.getHandler());
        if (this.tabPane.getTabCount() > 0 && !Boolean.TRUE.equals(htmlDisabled = (Boolean)this.tabPane.getClientProperty("html.disable"))) {
            this.htmlViews = this.createHTMLVector();
        }
    }

    protected void uninstallListeners() {
        if (this.mouseListener != null) {
            this.tabPane.removeMouseListener(this.mouseListener);
            this.mouseListener = null;
        }
        this.tabPane.removeMouseMotionListener(this.getHandler());
        if (this.focusListener != null) {
            this.tabPane.removeFocusListener(this.focusListener);
            this.focusListener = null;
        }
        this.tabPane.removeContainerListener(this.getHandler());
        if (this.htmlViews != null) {
            this.htmlViews.removeAllElements();
            this.htmlViews = null;
        }
        if (this.tabChangeListener != null) {
            this.tabPane.removeChangeListener(this.tabChangeListener);
            this.tabChangeListener = null;
        }
        if (this.propertyChangeListener != null) {
            this.tabPane.removePropertyChangeListener(this.propertyChangeListener);
            this.propertyChangeListener = null;
        }
        this.handler = null;
    }

    protected MouseListener createMouseListener() {
        return this.getHandler();
    }

    protected FocusListener createFocusListener() {
        return this.getHandler();
    }

    protected ChangeListener createChangeListener() {
        return this.getHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void installKeyboardActions() {
        InputMap km = this.getInputMap(1);
        SwingUtilities.replaceUIInputMap(this.tabPane, 1, km);
        km = this.getInputMap(0);
        SwingUtilities.replaceUIInputMap(this.tabPane, 0, km);
        LazyActionMap.installLazyActionMap(this.tabPane, BasicTabbedPaneUI.class, "TabbedPane.actionMap");
        this.updateMnemonics();
    }

    InputMap getInputMap(int condition) {
        if (condition == 1) {
            return (InputMap)DefaultLookup.get(this.tabPane, this, "TabbedPane.ancestorInputMap");
        }
        if (condition == 0) {
            return (InputMap)DefaultLookup.get(this.tabPane, this, "TabbedPane.focusInputMap");
        }
        return null;
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(this.tabPane, null);
        SwingUtilities.replaceUIInputMap(this.tabPane, 1, null);
        SwingUtilities.replaceUIInputMap(this.tabPane, 0, null);
        SwingUtilities.replaceUIInputMap(this.tabPane, 2, null);
        this.mnemonicToIndexMap = null;
        this.mnemonicInputMap = null;
    }

    private void updateMnemonics() {
        this.resetMnemonics();
        for (int counter = this.tabPane.getTabCount() - 1; counter >= 0; --counter) {
            int mnemonic = this.tabPane.getMnemonicAt(counter);
            if (mnemonic <= 0) continue;
            this.addMnemonic(counter, mnemonic);
        }
    }

    private void resetMnemonics() {
        if (this.mnemonicToIndexMap != null) {
            this.mnemonicToIndexMap.clear();
            this.mnemonicInputMap.clear();
        }
    }

    private void addMnemonic(int index, int mnemonic) {
        if (this.mnemonicToIndexMap == null) {
            this.initMnemonics();
        }
        this.mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, BasicLookAndFeel.getFocusAcceleratorKeyMask()), "setSelectedIndex");
        this.mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask())), "setSelectedIndex");
        this.mnemonicToIndexMap.put(mnemonic, index);
    }

    private void initMnemonics() {
        this.mnemonicToIndexMap = new Hashtable();
        this.mnemonicInputMap = new ComponentInputMapUIResource(this.tabPane);
        this.mnemonicInputMap.setParent(SwingUtilities.getUIInputMap(this.tabPane, 2));
        SwingUtilities.replaceUIInputMap(this.tabPane, 2, this.mnemonicInputMap);
    }

    private void setRolloverTab(int x, int y) {
        this.setRolloverTab(this.tabForCoordinate(this.tabPane, x, y, false));
    }

    protected void setRolloverTab(int index) {
        this.rolloverTabIndex = index;
    }

    protected int getRolloverTab() {
        return this.rolloverTabIndex;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return null;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return null;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = this.calculateBaselineIfNecessary();
        if (baseline != -1) {
            int placement = this.tabPane.getTabPlacement();
            Insets insets = this.tabPane.getInsets();
            Insets tabAreaInsets = this.getTabAreaInsets(placement);
            switch (placement) {
                case 1: {
                    return baseline += insets.top + tabAreaInsets.top;
                }
                case 3: {
                    baseline = height - insets.bottom - tabAreaInsets.bottom - this.maxTabHeight + baseline;
                    return baseline;
                }
                case 2: 
                case 4: {
                    return baseline += insets.top + tabAreaInsets.top;
                }
            }
        }
        return -1;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        switch (this.tabPane.getTabPlacement()) {
            case 1: 
            case 2: 
            case 4: {
                return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
            }
            case 3: {
                return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
            }
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    protected int getBaseline(int tab) {
        if (this.tabPane.getTabComponentAt(tab) != null) {
            int offset = this.getBaselineOffset();
            if (offset != 0) {
                return -1;
            }
            Component c = this.tabPane.getTabComponentAt(tab);
            Dimension pref = c.getPreferredSize();
            Insets tabInsets = this.getTabInsets(this.tabPane.getTabPlacement(), tab);
            int cellHeight = this.maxTabHeight - tabInsets.top - tabInsets.bottom;
            return c.getBaseline(pref.width, pref.height) + (cellHeight - pref.height) / 2 + tabInsets.top;
        }
        View view = this.getTextViewForTab(tab);
        if (view != null) {
            int viewHeight = (int)view.getPreferredSpan(1);
            int baseline = BasicHTML.getHTMLBaseline(view, (int)view.getPreferredSpan(0), viewHeight);
            if (baseline >= 0) {
                return this.maxTabHeight / 2 - viewHeight / 2 + baseline + this.getBaselineOffset();
            }
            return -1;
        }
        FontMetrics metrics = this.getFontMetrics();
        int fontHeight = metrics.getHeight();
        int fontBaseline = metrics.getAscent();
        return this.maxTabHeight / 2 - fontHeight / 2 + fontBaseline + this.getBaselineOffset();
    }

    protected int getBaselineOffset() {
        switch (this.tabPane.getTabPlacement()) {
            case 1: {
                if (this.tabPane.getTabCount() > 1) {
                    return 1;
                }
                return -1;
            }
            case 3: {
                if (this.tabPane.getTabCount() > 1) {
                    return -1;
                }
                return 1;
            }
        }
        return this.maxTabHeight % 2;
    }

    private int calculateBaselineIfNecessary() {
        if (!this.calculatedBaseline) {
            this.calculatedBaseline = true;
            this.baseline = -1;
            if (this.tabPane.getTabCount() > 0) {
                this.calculateBaseline();
            }
        }
        return this.baseline;
    }

    private void calculateBaseline() {
        int tabCount = this.tabPane.getTabCount();
        int tabPlacement = this.tabPane.getTabPlacement();
        this.maxTabHeight = this.calculateMaxTabHeight(tabPlacement);
        this.baseline = this.getBaseline(0);
        if (this.isHorizontalTabPlacement()) {
            for (int i = 1; i < tabCount; ++i) {
                if (this.getBaseline(i) == this.baseline) continue;
                this.baseline = -1;
                break;
            }
        } else {
            FontMetrics fontMetrics = this.getFontMetrics();
            int fontHeight = fontMetrics.getHeight();
            int height = this.calculateTabHeight(tabPlacement, 0, fontHeight);
            for (int i = 1; i < tabCount; ++i) {
                int newHeight = this.calculateTabHeight(tabPlacement, i, fontHeight);
                if (height == newHeight) continue;
                this.baseline = -1;
                break;
            }
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int selectedIndex = this.tabPane.getSelectedIndex();
        int tabPlacement = this.tabPane.getTabPlacement();
        this.ensureCurrentLayout();
        if (this.tabsOverlapBorder) {
            this.paintContentBorder(g, tabPlacement, selectedIndex);
        }
        if (!this.scrollableTabLayoutEnabled()) {
            this.paintTabArea(g, tabPlacement, selectedIndex);
        }
        if (!this.tabsOverlapBorder) {
            this.paintContentBorder(g, tabPlacement, selectedIndex);
        }
    }

    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        int tabCount = this.tabPane.getTabCount();
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();
        for (int i = this.runCount - 1; i >= 0; --i) {
            int start = this.tabRuns[i];
            int next = this.tabRuns[i == this.runCount - 1 ? 0 : i + 1];
            int end = next != 0 ? next - 1 : tabCount - 1;
            for (int j = start; j <= end; ++j) {
                if (j == selectedIndex || !this.rects[j].intersects(clipRect)) continue;
                this.paintTab(g, tabPlacement, this.rects, j, iconRect, textRect);
            }
        }
        if (selectedIndex >= 0 && this.rects[selectedIndex].intersects(clipRect)) {
            this.paintTab(g, tabPlacement, this.rects, selectedIndex, iconRect, textRect);
        }
    }

    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        boolean isSelected;
        Rectangle tabRect = rects[tabIndex];
        int selectedIndex = this.tabPane.getSelectedIndex();
        boolean bl = isSelected = selectedIndex == tabIndex;
        if (this.tabsOpaque || this.tabPane.isOpaque()) {
            this.paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);
        }
        this.paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);
        String title = this.tabPane.getTitleAt(tabIndex);
        Font font = this.tabPane.getFont();
        FontMetrics metrics = SwingUtilities2.getFontMetrics(this.tabPane, g, font);
        Icon icon = this.getIconForTab(tabIndex);
        this.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);
        if (this.tabPane.getTabComponentAt(tabIndex) == null) {
            String clippedTitle = title;
            if (this.scrollableTabLayoutEnabled() && this.tabScroller.croppedEdge.isParamsSet() && this.tabScroller.croppedEdge.getTabIndex() == tabIndex && this.isHorizontalTabPlacement()) {
                int availTextWidth = this.tabScroller.croppedEdge.getCropline() - (textRect.x - tabRect.x) - this.tabScroller.croppedEdge.getCroppedSideWidth();
                clippedTitle = SwingUtilities2.clipStringIfNecessary(null, metrics, title, availTextWidth);
            } else if (!this.scrollableTabLayoutEnabled() && this.isHorizontalTabPlacement()) {
                clippedTitle = SwingUtilities2.clipStringIfNecessary(null, metrics, title, textRect.width);
            }
            this.paintText(g, tabPlacement, font, metrics, tabIndex, clippedTitle, textRect, isSelected);
            this.paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
        }
        this.paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect, textRect, isSelected);
    }

    private boolean isHorizontalTabPlacement() {
        return this.tabPane.getTabPlacement() == 1 || this.tabPane.getTabPlacement() == 3;
    }

    private static Polygon createCroppedTabShape(int tabPlacement, Rectangle tabRect, int cropline) {
        int end;
        int start;
        int rlen;
        int ostart = switch (tabPlacement) {
            case 2, 4 -> {
                rlen = tabRect.width;
                start = tabRect.x;
                end = tabRect.x + tabRect.width;
                yield tabRect.y + tabRect.height;
            }
            default -> {
                rlen = tabRect.height;
                start = tabRect.y;
                end = tabRect.y + tabRect.height;
                yield tabRect.x + tabRect.width;
            }
        };
        int rcnt = rlen / 12;
        if (rlen % 12 > 0) {
            ++rcnt;
        }
        int npts = 2 + rcnt * 8;
        int[] xp = new int[npts];
        int[] yp = new int[npts];
        int pcnt = 0;
        xp[pcnt] = ostart;
        yp[pcnt++] = end;
        xp[pcnt] = ostart;
        yp[pcnt++] = start;
        block3: for (int i = 0; i < rcnt; ++i) {
            for (int j = 0; j < xCropLen.length; ++j) {
                xp[pcnt] = cropline - xCropLen[j];
                yp[pcnt] = start + i * 12 + yCropLen[j];
                if (yp[pcnt] >= end) {
                    yp[pcnt] = end;
                    ++pcnt;
                    continue block3;
                }
                ++pcnt;
            }
        }
        if (tabPlacement == 1 || tabPlacement == 3) {
            return new Polygon(xp, yp, pcnt);
        }
        return new Polygon(yp, xp, pcnt);
    }

    private void paintCroppedTabEdge(Graphics g) {
        int tabIndex = this.tabScroller.croppedEdge.getTabIndex();
        int cropline = this.tabScroller.croppedEdge.getCropline();
        switch (this.tabPane.getTabPlacement()) {
            case 2: 
            case 4: {
                int x = this.rects[tabIndex].x;
                int y = cropline;
                g.setColor(this.shadow);
                for (int xx = x; xx <= x + this.rects[tabIndex].width; xx += 12) {
                    for (int i = 0; i < xCropLen.length; i += 2) {
                        g.drawLine(xx + yCropLen[i], y - xCropLen[i], xx + yCropLen[i + 1] - 1, y - xCropLen[i + 1]);
                    }
                }
                break;
            }
            default: {
                int y;
                int x = cropline;
                g.setColor(this.shadow);
                for (int yy = y = this.rects[tabIndex].y; yy <= y + this.rects[tabIndex].height; yy += 12) {
                    for (int i = 0; i < xCropLen.length; i += 2) {
                        g.drawLine(x - xCropLen[i], yy + yCropLen[i], x - xCropLen[i + 1], yy + yCropLen[i + 1] - 1);
                    }
                }
            }
        }
    }

    protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        iconRect.y = 0;
        iconRect.x = 0;
        textRect.y = 0;
        textRect.x = 0;
        View v = this.getTextViewForTab(tabIndex);
        if (v != null) {
            this.tabPane.putClientProperty("html", v);
        }
        SwingUtilities.layoutCompoundLabel(this.tabPane, metrics, title, icon, 0, 0, 0, 11, tabRect, iconRect, textRect, this.textIconGap);
        this.tabPane.putClientProperty("html", null);
        int xNudge = this.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = this.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }

    protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect, boolean isSelected) {
        if (icon != null) {
            Shape oldClip = g.getClip();
            ((Graphics2D)g).clip(iconRect);
            icon.paintIcon(this.tabPane, g, iconRect.x, iconRect.y);
            g.setClip(oldClip);
        }
    }

    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        g.setFont(font);
        View v = this.getTextViewForTab(tabIndex);
        if (v != null) {
            v.paint(g, textRect);
        } else {
            int mnemIndex = this.tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            if (this.tabPane.isEnabled() && this.tabPane.isEnabledAt(tabIndex)) {
                Color selectedFG;
                Color fg = this.tabPane.getForegroundAt(tabIndex);
                if (isSelected && fg instanceof UIResource && (selectedFG = UIManager.getColor("TabbedPane.selectedForeground")) != null) {
                    fg = selectedFG;
                }
                g.setColor(fg);
                SwingUtilities2.drawStringUnderlineCharAt(this.tabPane, g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
            } else {
                g.setColor(this.tabPane.getBackgroundAt(tabIndex).brighter());
                SwingUtilities2.drawStringUnderlineCharAt(this.tabPane, g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
                g.setColor(this.tabPane.getBackgroundAt(tabIndex).darker());
                SwingUtilities2.drawStringUnderlineCharAt(this.tabPane, g, title, mnemIndex, textRect.x - 1, textRect.y + metrics.getAscent() - 1);
            }
        }
    }

    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        Rectangle tabRect = this.rects[tabIndex];
        String propKey = isSelected ? "selectedLabelShift" : "labelShift";
        int nudge = DefaultLookup.getInt(this.tabPane, this, "TabbedPane." + propKey, 1);
        switch (tabPlacement) {
            case 2: {
                return nudge;
            }
            case 4: {
                return -nudge;
            }
        }
        return tabRect.width % 2;
    }

    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        Rectangle tabRect = this.rects[tabIndex];
        int nudge = isSelected ? DefaultLookup.getInt(this.tabPane, this, "TabbedPane.selectedLabelShift", -1) : DefaultLookup.getInt(this.tabPane, this, "TabbedPane.labelShift", 1);
        switch (tabPlacement) {
            case 3: {
                return -nudge;
            }
            case 2: 
            case 4: {
                return tabRect.height % 2;
            }
        }
        return nudge;
    }

    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        Rectangle tabRect = rects[tabIndex];
        if (this.tabPane.hasFocus() && isSelected) {
            int w;
            int y;
            int x;
            g.setColor(this.focus);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, switch (tabPlacement) {
                case 2 -> {
                    x = tabRect.x + 3;
                    y = tabRect.y + 3;
                    w = tabRect.width - 5;
                    yield tabRect.height - 6;
                }
                case 4 -> {
                    x = tabRect.x + 2;
                    y = tabRect.y + 3;
                    w = tabRect.width - 5;
                    yield tabRect.height - 6;
                }
                case 3 -> {
                    x = tabRect.x + 3;
                    y = tabRect.y + 2;
                    w = tabRect.width - 6;
                    yield tabRect.height - 5;
                }
                default -> {
                    x = tabRect.x + 3;
                    y = tabRect.y + 3;
                    w = tabRect.width - 6;
                    yield tabRect.height - 5;
                }
            });
        }
    }

    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(this.lightHighlight);
        switch (tabPlacement) {
            case 2: {
                g.drawLine(x + 1, y + h - 2, x + 1, y + h - 2);
                g.drawLine(x, y + 2, x, y + h - 3);
                g.drawLine(x + 1, y + 1, x + 1, y + 1);
                g.drawLine(x + 2, y, x + w - 1, y);
                g.setColor(this.shadow);
                g.drawLine(x + 2, y + h - 2, x + w - 1, y + h - 2);
                g.setColor(this.darkShadow);
                g.drawLine(x + 2, y + h - 1, x + w - 1, y + h - 1);
                break;
            }
            case 4: {
                g.drawLine(x, y, x + w - 3, y);
                g.setColor(this.shadow);
                g.drawLine(x, y + h - 2, x + w - 3, y + h - 2);
                g.drawLine(x + w - 2, y + 2, x + w - 2, y + h - 3);
                g.setColor(this.darkShadow);
                g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1);
                g.drawLine(x + w - 2, y + h - 2, x + w - 2, y + h - 2);
                g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 3);
                g.drawLine(x, y + h - 1, x + w - 3, y + h - 1);
                break;
            }
            case 3: {
                g.drawLine(x, y, x, y + h - 3);
                g.drawLine(x + 1, y + h - 2, x + 1, y + h - 2);
                g.setColor(this.shadow);
                g.drawLine(x + 2, y + h - 2, x + w - 3, y + h - 2);
                g.drawLine(x + w - 2, y, x + w - 2, y + h - 3);
                g.setColor(this.darkShadow);
                g.drawLine(x + 2, y + h - 1, x + w - 3, y + h - 1);
                g.drawLine(x + w - 2, y + h - 2, x + w - 2, y + h - 2);
                g.drawLine(x + w - 1, y, x + w - 1, y + h - 3);
                break;
            }
            default: {
                g.drawLine(x, y + 2, x, y + h - 1);
                g.drawLine(x + 1, y + 1, x + 1, y + 1);
                g.drawLine(x + 2, y, x + w - 3, y);
                g.setColor(this.shadow);
                g.drawLine(x + w - 2, y + 2, x + w - 2, y + h - 1);
                g.setColor(this.darkShadow);
                g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 1);
                g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1);
            }
        }
    }

    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(!isSelected || this.selectedColor == null ? this.tabPane.getBackgroundAt(tabIndex) : this.selectedColor);
        switch (tabPlacement) {
            case 2: {
                g.fillRect(x + 1, y + 1, w - 1, h - 3);
                break;
            }
            case 4: {
                g.fillRect(x, y + 1, w - 2, h - 3);
                break;
            }
            case 3: {
                g.fillRect(x + 1, y, w - 3, h - 1);
                break;
            }
            default: {
                g.fillRect(x + 1, y + 1, w - 3, h - 1);
            }
        }
    }

    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int width = this.tabPane.getWidth();
        int height = this.tabPane.getHeight();
        Insets insets = this.tabPane.getInsets();
        Insets tabAreaInsets = this.getTabAreaInsets(tabPlacement);
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;
        switch (tabPlacement) {
            case 2: {
                x += this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth);
                if (this.tabsOverlapBorder) {
                    x -= tabAreaInsets.right;
                }
                w -= x - insets.left;
                break;
            }
            case 4: {
                w -= this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth);
                if (!this.tabsOverlapBorder) break;
                w += tabAreaInsets.left;
                break;
            }
            case 3: {
                h -= this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight);
                if (!this.tabsOverlapBorder) break;
                h += tabAreaInsets.top;
                break;
            }
            default: {
                y += this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight);
                if (this.tabsOverlapBorder) {
                    y -= tabAreaInsets.bottom;
                }
                h -= y - insets.top;
            }
        }
        if (this.tabPane.getTabCount() > 0 && (this.contentOpaque || this.tabPane.isOpaque())) {
            Color color = UIManager.getColor("TabbedPane.contentAreaColor");
            if (color != null) {
                g.setColor(color);
            } else if (this.selectedColor == null || selectedIndex == -1) {
                g.setColor(this.tabPane.getBackground());
            } else {
                g.setColor(this.selectedColor);
            }
            g.fillRect(x, y, w, h);
        }
        this.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        this.paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        this.paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        this.paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    }

    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0 ? null : this.getTabBounds(selectedIndex, this.calcRect);
        g.setColor(this.lightHighlight);
        if (tabPlacement != 1 || selectedIndex < 0 || selRect.y + selRect.height + 1 < y || selRect.x < x || selRect.x > x + w) {
            g.drawLine(x, y, x + w - 2, y);
        } else {
            g.drawLine(x, y, selRect.x - 1, y);
            if (selRect.x + selRect.width < x + w - 2) {
                g.drawLine(selRect.x + selRect.width, y, x + w - 2, y);
            } else {
                g.setColor(this.shadow);
                g.drawLine(x + w - 2, y, x + w - 2, y);
            }
        }
    }

    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0 ? null : this.getTabBounds(selectedIndex, this.calcRect);
        g.setColor(this.lightHighlight);
        if (tabPlacement != 2 || selectedIndex < 0 || selRect.x + selRect.width + 1 < x || selRect.y < y || selRect.y > y + h) {
            g.drawLine(x, y, x, y + h - 2);
        } else {
            g.drawLine(x, y, x, selRect.y - 1);
            if (selRect.y + selRect.height < y + h - 2) {
                g.drawLine(x, selRect.y + selRect.height, x, y + h - 2);
            }
        }
    }

    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0 ? null : this.getTabBounds(selectedIndex, this.calcRect);
        g.setColor(this.shadow);
        if (tabPlacement != 3 || selectedIndex < 0 || selRect.y - 1 > h || selRect.x < x || selRect.x > x + w) {
            g.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g.setColor(this.darkShadow);
            g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        } else {
            g.drawLine(x + 1, y + h - 2, selRect.x - 1, y + h - 2);
            g.setColor(this.darkShadow);
            g.drawLine(x, y + h - 1, selRect.x - 1, y + h - 1);
            if (selRect.x + selRect.width < x + w - 2) {
                g.setColor(this.shadow);
                g.drawLine(selRect.x + selRect.width, y + h - 2, x + w - 2, y + h - 2);
                g.setColor(this.darkShadow);
                g.drawLine(selRect.x + selRect.width, y + h - 1, x + w - 1, y + h - 1);
            }
        }
    }

    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0 ? null : this.getTabBounds(selectedIndex, this.calcRect);
        g.setColor(this.shadow);
        if (tabPlacement != 4 || selectedIndex < 0 || selRect.x - 1 > w || selRect.y < y || selRect.y > y + h) {
            g.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);
            g.setColor(this.darkShadow);
            g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
        } else {
            g.drawLine(x + w - 2, y + 1, x + w - 2, selRect.y - 1);
            g.setColor(this.darkShadow);
            g.drawLine(x + w - 1, y, x + w - 1, selRect.y - 1);
            if (selRect.y + selRect.height < y + h - 2) {
                g.setColor(this.shadow);
                g.drawLine(x + w - 2, selRect.y + selRect.height, x + w - 2, y + h - 2);
                g.setColor(this.darkShadow);
                g.drawLine(x + w - 1, selRect.y + selRect.height, x + w - 1, y + h - 2);
            }
        }
    }

    private void ensureCurrentLayout() {
        if (!this.tabPane.isValid()) {
            this.tabPane.validate();
        }
        if (!this.tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout)this.tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }

    @Override
    public Rectangle getTabBounds(JTabbedPane pane, int i) {
        this.ensureCurrentLayout();
        Rectangle tabRect = new Rectangle();
        return this.getTabBounds(i, tabRect);
    }

    @Override
    public int getTabRunCount(JTabbedPane pane) {
        this.ensureCurrentLayout();
        return this.runCount;
    }

    @Override
    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        return this.tabForCoordinate(pane, x, y, true);
    }

    private int tabForCoordinate(JTabbedPane pane, int x, int y, boolean validateIfNecessary) {
        if (validateIfNecessary) {
            this.ensureCurrentLayout();
        }
        if (this.isRunsDirty) {
            return -1;
        }
        Point p = new Point(x, y);
        if (this.scrollableTabLayoutEnabled()) {
            this.translatePointToTabPanel(x, y, p);
            Rectangle viewRect = this.tabScroller.viewport.getViewRect();
            if (!viewRect.contains(p)) {
                return -1;
            }
        }
        int tabCount = this.tabPane.getTabCount();
        for (int i = 0; i < tabCount; ++i) {
            if (!this.rects[i].contains(p.x, p.y)) continue;
            return i;
        }
        return -1;
    }

    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        dest.width = this.rects[tabIndex].width;
        dest.height = this.rects[tabIndex].height;
        if (this.scrollableTabLayoutEnabled()) {
            Point vpp = this.tabScroller.viewport.getLocation();
            Point viewp = this.tabScroller.viewport.getViewPosition();
            dest.x = this.rects[tabIndex].x + vpp.x - viewp.x;
            dest.y = this.rects[tabIndex].y + vpp.y - viewp.y;
        } else {
            dest.x = this.rects[tabIndex].x;
            dest.y = this.rects[tabIndex].y;
        }
        return dest;
    }

    private int getClosestTab(int x, int y) {
        int want;
        int tabCount;
        int min = 0;
        int max = tabCount = Math.min(this.rects.length, this.tabPane.getTabCount());
        int tabPlacement = this.tabPane.getTabPlacement();
        boolean useX = tabPlacement == 1 || tabPlacement == 3;
        int n = want = useX ? x : y;
        while (min != max) {
            int maxLoc;
            int minLoc;
            int current = (max + min) / 2;
            if (useX) {
                minLoc = this.rects[current].x;
                maxLoc = minLoc + this.rects[current].width;
            } else {
                minLoc = this.rects[current].y;
                maxLoc = minLoc + this.rects[current].height;
            }
            if (want < minLoc) {
                max = current;
                if (min != max) continue;
                return Math.max(0, current - 1);
            }
            if (want >= maxLoc) {
                min = current;
                if (max - min > 1) continue;
                return Math.max(current + 1, tabCount - 1);
            }
            return current;
        }
        return min;
    }

    private Point translatePointToTabPanel(int srcx, int srcy, Point dest) {
        Point vpp = this.tabScroller.viewport.getLocation();
        Point viewp = this.tabScroller.viewport.getViewPosition();
        dest.x = srcx - vpp.x + viewp.x;
        dest.y = srcy - vpp.y + viewp.y;
        return dest;
    }

    protected Component getVisibleComponent() {
        return this.visibleComponent;
    }

    protected void setVisibleComponent(Component component) {
        if (this.visibleComponent != null && this.visibleComponent != component && this.visibleComponent.getParent() == this.tabPane && this.visibleComponent.isVisible()) {
            this.visibleComponent.setVisible(false);
        }
        if (component != null && !component.isVisible()) {
            component.setVisible(true);
        }
        this.visibleComponent = component;
    }

    protected void assureRectsCreated(int tabCount) {
        int rectArrayLen = this.rects.length;
        if (tabCount != rectArrayLen) {
            Rectangle[] tempRectArray = new Rectangle[tabCount];
            System.arraycopy(this.rects, 0, tempRectArray, 0, Math.min(rectArrayLen, tabCount));
            this.rects = tempRectArray;
            for (int rectIndex = rectArrayLen; rectIndex < tabCount; ++rectIndex) {
                this.rects[rectIndex] = new Rectangle();
            }
        }
    }

    protected void expandTabRunsArray() {
        int rectLen = this.tabRuns.length;
        int[] newArray = new int[rectLen + 10];
        System.arraycopy(this.tabRuns, 0, newArray, 0, this.runCount);
        this.tabRuns = newArray;
    }

    protected int getRunForTab(int tabCount, int tabIndex) {
        for (int i = 0; i < this.runCount; ++i) {
            int first = this.tabRuns[i];
            int last = this.lastTabInRun(tabCount, i);
            if (tabIndex < first || tabIndex > last) continue;
            return i;
        }
        return 0;
    }

    protected int lastTabInRun(int tabCount, int run) {
        int nextRun;
        if (this.runCount == 1) {
            return tabCount - 1;
        }
        int n = nextRun = run == this.runCount - 1 ? 0 : run + 1;
        if (this.tabRuns[nextRun] == 0) {
            return tabCount - 1;
        }
        return this.tabRuns[nextRun] - 1;
    }

    protected int getTabRunOverlay(int tabPlacement) {
        return this.tabRunOverlay;
    }

    protected int getTabRunIndent(int tabPlacement, int run) {
        return 0;
    }

    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return this.runCount > 1;
    }

    protected boolean shouldRotateTabRuns(int tabPlacement) {
        return true;
    }

    protected Icon getIconForTab(int tabIndex) {
        return !this.tabPane.isEnabled() || !this.tabPane.isEnabledAt(tabIndex) ? this.tabPane.getDisabledIconAt(tabIndex) : this.tabPane.getIconAt(tabIndex);
    }

    protected View getTextViewForTab(int tabIndex) {
        if (this.htmlViews != null) {
            return this.htmlViews.elementAt(tabIndex);
        }
        return null;
    }

    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        int height = 0;
        Component c = this.tabPane.getTabComponentAt(tabIndex);
        if (c != null) {
            height = c.getPreferredSize().height;
        } else {
            View v = this.getTextViewForTab(tabIndex);
            height = v != null ? (height += (int)v.getPreferredSpan(1)) : (height += fontHeight);
            Icon icon = this.getIconForTab(tabIndex);
            if (icon != null) {
                height = Math.max(height, icon.getIconHeight());
            }
        }
        Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
        return height += tabInsets.top + tabInsets.bottom + 2;
    }

    protected int calculateMaxTabHeight(int tabPlacement) {
        FontMetrics metrics = this.getFontMetrics();
        int tabCount = this.tabPane.getTabCount();
        int result = 0;
        int fontHeight = metrics.getHeight();
        for (int i = 0; i < tabCount; ++i) {
            result = Math.max(this.calculateTabHeight(tabPlacement, i, fontHeight), result);
        }
        return result;
    }

    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        Insets tabInsets = this.getTabInsets(tabPlacement, tabIndex);
        int width = tabInsets.left + tabInsets.right + 3;
        Component tabComponent = this.tabPane.getTabComponentAt(tabIndex);
        if (tabComponent != null) {
            width += tabComponent.getPreferredSize().width;
        } else {
            View v;
            Icon icon = this.getIconForTab(tabIndex);
            if (icon != null) {
                width += icon.getIconWidth() + this.textIconGap;
            }
            if ((v = this.getTextViewForTab(tabIndex)) != null) {
                width += (int)v.getPreferredSpan(0);
            } else {
                String title = this.tabPane.getTitleAt(tabIndex);
                width += SwingUtilities2.stringWidth(this.tabPane, metrics, title);
            }
        }
        return width;
    }

    protected int calculateMaxTabWidth(int tabPlacement) {
        FontMetrics metrics = this.getFontMetrics();
        int tabCount = this.tabPane.getTabCount();
        int result = 0;
        for (int i = 0; i < tabCount; ++i) {
            result = Math.max(this.calculateTabWidth(tabPlacement, i, metrics), result);
        }
        return result;
    }

    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
        Insets tabAreaInsets = this.getTabAreaInsets(tabPlacement);
        int tabRunOverlay = this.getTabRunOverlay(tabPlacement);
        return horizRunCount > 0 ? horizRunCount * (maxTabHeight - tabRunOverlay) + tabRunOverlay + tabAreaInsets.top + tabAreaInsets.bottom : 0;
    }

    protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
        Insets tabAreaInsets = this.getTabAreaInsets(tabPlacement);
        int tabRunOverlay = this.getTabRunOverlay(tabPlacement);
        return vertRunCount > 0 ? vertRunCount * (maxTabWidth - tabRunOverlay) + tabRunOverlay + tabAreaInsets.left + tabAreaInsets.right : 0;
    }

    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return this.tabInsets;
    }

    protected Insets getSelectedTabPadInsets(int tabPlacement) {
        BasicTabbedPaneUI.rotateInsets(this.selectedTabPadInsets, this.currentPadInsets, tabPlacement);
        return this.currentPadInsets;
    }

    protected Insets getTabAreaInsets(int tabPlacement) {
        BasicTabbedPaneUI.rotateInsets(this.tabAreaInsets, this.currentTabAreaInsets, tabPlacement);
        return this.currentTabAreaInsets;
    }

    protected Insets getContentBorderInsets(int tabPlacement) {
        return this.contentBorderInsets;
    }

    protected FontMetrics getFontMetrics() {
        Font font = this.tabPane.getFont();
        return this.tabPane.getFontMetrics(font);
    }

    protected void navigateSelectedTab(int direction) {
        int tabPlacement = this.tabPane.getTabPlacement();
        int current = DefaultLookup.getBoolean(this.tabPane, this, "TabbedPane.selectionFollowsFocus", true) ? this.tabPane.getSelectedIndex() : this.getFocusIndex();
        int tabCount = this.tabPane.getTabCount();
        boolean leftToRight = BasicGraphicsUtils.isLeftToRight(this.tabPane);
        if (tabCount <= 0) {
            return;
        }
        block0 : switch (tabPlacement) {
            case 2: 
            case 4: {
                switch (direction) {
                    case 12: {
                        this.selectNextTab(current);
                        break block0;
                    }
                    case 13: {
                        this.selectPreviousTab(current);
                        break block0;
                    }
                    case 1: {
                        this.selectPreviousTabInRun(current);
                        break block0;
                    }
                    case 5: {
                        this.selectNextTabInRun(current);
                        break block0;
                    }
                    case 7: {
                        int offset = this.getTabRunOffset(tabPlacement, tabCount, current, false);
                        this.selectAdjacentRunTab(tabPlacement, current, offset);
                        break block0;
                    }
                    case 3: {
                        int offset = this.getTabRunOffset(tabPlacement, tabCount, current, true);
                        this.selectAdjacentRunTab(tabPlacement, current, offset);
                        break block0;
                    }
                }
                break;
            }
            default: {
                switch (direction) {
                    case 12: {
                        this.selectNextTab(current);
                        break block0;
                    }
                    case 13: {
                        this.selectPreviousTab(current);
                        break block0;
                    }
                    case 1: {
                        int offset = this.getTabRunOffset(tabPlacement, tabCount, current, false);
                        this.selectAdjacentRunTab(tabPlacement, current, offset);
                        break block0;
                    }
                    case 5: {
                        int offset = this.getTabRunOffset(tabPlacement, tabCount, current, true);
                        this.selectAdjacentRunTab(tabPlacement, current, offset);
                        break block0;
                    }
                    case 3: {
                        if (leftToRight) {
                            this.selectNextTabInRun(current);
                            break block0;
                        }
                        this.selectPreviousTabInRun(current);
                        break block0;
                    }
                    case 7: {
                        if (leftToRight) {
                            this.selectPreviousTabInRun(current);
                            break block0;
                        }
                        this.selectNextTabInRun(current);
                        break block0;
                    }
                }
            }
        }
    }

    protected void selectNextTabInRun(int current) {
        int tabCount = this.tabPane.getTabCount();
        int tabIndex = this.getNextTabIndexInRun(tabCount, current);
        while (tabIndex != current && !this.tabPane.isEnabledAt(tabIndex)) {
            tabIndex = this.getNextTabIndexInRun(tabCount, tabIndex);
        }
        this.navigateTo(tabIndex);
    }

    protected void selectPreviousTabInRun(int current) {
        int tabCount = this.tabPane.getTabCount();
        int tabIndex = this.getPreviousTabIndexInRun(tabCount, current);
        while (tabIndex != current && !this.tabPane.isEnabledAt(tabIndex)) {
            tabIndex = this.getPreviousTabIndexInRun(tabCount, tabIndex);
        }
        this.navigateTo(tabIndex);
    }

    protected void selectNextTab(int current) {
        int tabIndex = this.getNextTabIndex(current);
        while (tabIndex != current && !this.tabPane.isEnabledAt(tabIndex)) {
            tabIndex = this.getNextTabIndex(tabIndex);
        }
        this.navigateTo(tabIndex);
    }

    protected void selectPreviousTab(int current) {
        int tabIndex = this.getPreviousTabIndex(current);
        while (tabIndex != current && !this.tabPane.isEnabledAt(tabIndex)) {
            tabIndex = this.getPreviousTabIndex(tabIndex);
        }
        this.navigateTo(tabIndex);
    }

    protected void selectAdjacentRunTab(int tabPlacement, int tabIndex, int offset) {
        if (this.runCount < 2) {
            return;
        }
        Rectangle r = this.rects[tabIndex];
        int newIndex = switch (tabPlacement) {
            case 2, 4 -> this.tabForCoordinate(this.tabPane, r.x + r.width / 2 + offset, r.y + r.height / 2);
            default -> this.tabForCoordinate(this.tabPane, r.x + r.width / 2, r.y + r.height / 2 + offset);
        };
        if (newIndex != -1) {
            while (!this.tabPane.isEnabledAt(newIndex) && newIndex != tabIndex) {
                newIndex = this.getNextTabIndex(newIndex);
            }
            this.navigateTo(newIndex);
        }
    }

    private void navigateTo(int index) {
        if (DefaultLookup.getBoolean(this.tabPane, this, "TabbedPane.selectionFollowsFocus", true)) {
            this.tabPane.setSelectedIndex(index);
        } else {
            this.setFocusIndex(index, true);
        }
    }

    void setFocusIndex(int index, boolean repaint) {
        if (repaint && !this.isRunsDirty) {
            this.repaintTab(this.focusIndex);
            this.focusIndex = index;
            this.repaintTab(this.focusIndex);
        } else {
            this.focusIndex = index;
        }
    }

    private void repaintTab(int index) {
        if (!this.isRunsDirty && index >= 0 && index < this.tabPane.getTabCount()) {
            this.tabPane.repaint(this.getTabBounds(this.tabPane, index));
        }
    }

    private void validateFocusIndex() {
        if (this.focusIndex >= this.tabPane.getTabCount()) {
            this.setFocusIndex(this.tabPane.getSelectedIndex(), false);
        }
    }

    protected int getFocusIndex() {
        return this.focusIndex;
    }

    protected int getTabRunOffset(int tabPlacement, int tabCount, int tabIndex, boolean forward) {
        int run = this.getRunForTab(tabCount, tabIndex);
        return switch (tabPlacement) {
            case 2 -> {
                if (run == 0) {
                    yield forward ? -(this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth) - this.maxTabWidth) : -this.maxTabWidth;
                }
                if (run == this.runCount - 1) {
                    yield forward ? this.maxTabWidth : this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth) - this.maxTabWidth;
                }
                yield forward ? this.maxTabWidth : -this.maxTabWidth;
            }
            case 4 -> {
                if (run == 0) {
                    yield forward ? this.maxTabWidth : this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth) - this.maxTabWidth;
                }
                if (run == this.runCount - 1) {
                    yield forward ? -(this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth) - this.maxTabWidth) : -this.maxTabWidth;
                }
                yield forward ? this.maxTabWidth : -this.maxTabWidth;
            }
            case 3 -> {
                if (run == 0) {
                    yield forward ? this.maxTabHeight : this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight) - this.maxTabHeight;
                }
                if (run == this.runCount - 1) {
                    yield forward ? -(this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight) - this.maxTabHeight) : -this.maxTabHeight;
                }
                yield forward ? this.maxTabHeight : -this.maxTabHeight;
            }
            default -> run == 0 ? (forward ? -(this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight) - this.maxTabHeight) : -this.maxTabHeight) : (run == this.runCount - 1 ? (forward ? this.maxTabHeight : this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight) - this.maxTabHeight) : (forward ? this.maxTabHeight : -this.maxTabHeight));
        };
    }

    protected int getPreviousTabIndex(int base) {
        int tabIndex = base - 1 >= 0 ? base - 1 : this.tabPane.getTabCount() - 1;
        return tabIndex >= 0 ? tabIndex : 0;
    }

    protected int getNextTabIndex(int base) {
        return (base + 1) % this.tabPane.getTabCount();
    }

    protected int getNextTabIndexInRun(int tabCount, int base) {
        if (this.runCount < 2) {
            return this.getNextTabIndex(base);
        }
        int currentRun = this.getRunForTab(tabCount, base);
        int next = this.getNextTabIndex(base);
        if (next == this.tabRuns[this.getNextTabRun(currentRun)]) {
            return this.tabRuns[currentRun];
        }
        return next;
    }

    protected int getPreviousTabIndexInRun(int tabCount, int base) {
        if (this.runCount < 2) {
            return this.getPreviousTabIndex(base);
        }
        int currentRun = this.getRunForTab(tabCount, base);
        if (base == this.tabRuns[currentRun]) {
            int previous = this.tabRuns[this.getNextTabRun(currentRun)] - 1;
            return previous != -1 ? previous : tabCount - 1;
        }
        return this.getPreviousTabIndex(base);
    }

    protected int getPreviousTabRun(int baseRun) {
        int runIndex = baseRun - 1 >= 0 ? baseRun - 1 : this.runCount - 1;
        return runIndex >= 0 ? runIndex : 0;
    }

    protected int getNextTabRun(int baseRun) {
        return (baseRun + 1) % this.runCount;
    }

    protected static void rotateInsets(Insets topInsets, Insets targetInsets, int targetPlacement) {
        switch (targetPlacement) {
            case 2: {
                targetInsets.top = topInsets.left;
                targetInsets.left = topInsets.top;
                targetInsets.bottom = topInsets.right;
                targetInsets.right = topInsets.bottom;
                break;
            }
            case 3: {
                targetInsets.top = topInsets.bottom;
                targetInsets.left = topInsets.left;
                targetInsets.bottom = topInsets.top;
                targetInsets.right = topInsets.right;
                break;
            }
            case 4: {
                targetInsets.top = topInsets.left;
                targetInsets.left = topInsets.bottom;
                targetInsets.bottom = topInsets.right;
                targetInsets.right = topInsets.top;
                break;
            }
            default: {
                targetInsets.top = topInsets.top;
                targetInsets.left = topInsets.left;
                targetInsets.bottom = topInsets.bottom;
                targetInsets.right = topInsets.right;
            }
        }
    }

    boolean requestFocusForVisibleComponent() {
        return SwingUtilities2.tabbedPaneChangeFocusTo(this.getVisibleComponent());
    }

    private Vector<View> createHTMLVector() {
        Vector<View> htmlViews = new Vector<View>();
        int count = this.tabPane.getTabCount();
        if (count > 0) {
            for (int i = 0; i < count; ++i) {
                String title = this.tabPane.getTitleAt(i);
                if (BasicHTML.isHTMLString(title)) {
                    htmlViews.addElement(BasicHTML.createHTMLView(this.tabPane, title));
                    continue;
                }
                htmlViews.addElement(null);
            }
        }
        return htmlViews;
    }

    private static class Actions
    extends UIAction {
        static final String NEXT = "navigateNext";
        static final String PREVIOUS = "navigatePrevious";
        static final String RIGHT = "navigateRight";
        static final String LEFT = "navigateLeft";
        static final String UP = "navigateUp";
        static final String DOWN = "navigateDown";
        static final String PAGE_UP = "navigatePageUp";
        static final String PAGE_DOWN = "navigatePageDown";
        static final String REQUEST_FOCUS = "requestFocus";
        static final String REQUEST_FOCUS_FOR_VISIBLE = "requestFocusForVisibleComponent";
        static final String SET_SELECTED = "setSelectedIndex";
        static final String SELECT_FOCUSED = "selectTabWithFocus";
        static final String SCROLL_FORWARD = "scrollTabsForwardAction";
        static final String SCROLL_BACKWARD = "scrollTabsBackwardAction";

        Actions(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String key = this.getName();
            JTabbedPane pane = (JTabbedPane)e.getSource();
            BasicTabbedPaneUI ui = (BasicTabbedPaneUI)BasicLookAndFeel.getUIOfType(pane.getUI(), BasicTabbedPaneUI.class);
            if (ui == null) {
                return;
            }
            if (key == NEXT) {
                ui.navigateSelectedTab(12);
            } else if (key == PREVIOUS) {
                ui.navigateSelectedTab(13);
            } else if (key == RIGHT) {
                ui.navigateSelectedTab(3);
            } else if (key == LEFT) {
                ui.navigateSelectedTab(7);
            } else if (key == UP) {
                ui.navigateSelectedTab(1);
            } else if (key == DOWN) {
                ui.navigateSelectedTab(5);
            } else if (key == PAGE_UP) {
                int tabPlacement = pane.getTabPlacement();
                if (tabPlacement == 1 || tabPlacement == 3) {
                    ui.navigateSelectedTab(7);
                } else {
                    ui.navigateSelectedTab(1);
                }
            } else if (key == PAGE_DOWN) {
                int tabPlacement = pane.getTabPlacement();
                if (tabPlacement == 1 || tabPlacement == 3) {
                    ui.navigateSelectedTab(3);
                } else {
                    ui.navigateSelectedTab(5);
                }
            } else if (key == REQUEST_FOCUS) {
                pane.requestFocus();
            } else if (key == REQUEST_FOCUS_FOR_VISIBLE) {
                ui.requestFocusForVisibleComponent();
            } else if (key == SET_SELECTED) {
                String command = e.getActionCommand();
                if (command != null && command.length() > 0) {
                    Integer index;
                    int mnemonic = e.getActionCommand().charAt(0);
                    if (mnemonic >= 97 && mnemonic <= 122) {
                        mnemonic -= 32;
                    }
                    if ((index = ui.mnemonicToIndexMap.get(mnemonic)) != null && pane.isEnabledAt(index)) {
                        pane.setSelectedIndex(index);
                    }
                }
            } else if (key == SELECT_FOCUSED) {
                int focusIndex = ui.getFocusIndex();
                if (focusIndex != -1) {
                    pane.setSelectedIndex(focusIndex);
                }
            } else if (key == SCROLL_FORWARD) {
                if (ui.scrollableTabLayoutEnabled()) {
                    ui.tabScroller.scrollForward(pane.getTabPlacement());
                }
            } else if (key == SCROLL_BACKWARD && ui.scrollableTabLayoutEnabled()) {
                ui.tabScroller.scrollBackward(pane.getTabPlacement());
            }
        }
    }

    private class TabbedPaneScrollLayout
    extends TabbedPaneLayout {
        private TabbedPaneScrollLayout() {
        }

        @Override
        protected int preferredTabAreaHeight(int tabPlacement, int width) {
            return BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, 1, BasicTabbedPaneUI.this.calculateMaxTabHeight(tabPlacement));
        }

        @Override
        protected int preferredTabAreaWidth(int tabPlacement, int height) {
            return BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, 1, BasicTabbedPaneUI.this.calculateMaxTabWidth(tabPlacement));
        }

        @Override
        public void layoutContainer(Container parent) {
            BasicTabbedPaneUI.this.setRolloverTab(-1);
            int tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement();
            int tabCount = BasicTabbedPaneUI.this.tabPane.getTabCount();
            Insets insets = BasicTabbedPaneUI.this.tabPane.getInsets();
            int selectedIndex = BasicTabbedPaneUI.this.tabPane.getSelectedIndex();
            Component visibleComponent = BasicTabbedPaneUI.this.getVisibleComponent();
            this.calculateLayoutInfo();
            Component selectedComponent = null;
            if (selectedIndex < 0) {
                if (visibleComponent != null) {
                    BasicTabbedPaneUI.this.setVisibleComponent(null);
                }
            } else {
                selectedComponent = BasicTabbedPaneUI.this.tabPane.getComponentAt(selectedIndex);
            }
            if (BasicTabbedPaneUI.this.tabPane.getTabCount() == 0) {
                BasicTabbedPaneUI.this.tabScroller.croppedEdge.resetParams();
                BasicTabbedPaneUI.this.tabScroller.scrollForwardButton.setVisible(false);
                BasicTabbedPaneUI.this.tabScroller.scrollBackwardButton.setVisible(false);
                return;
            }
            boolean shouldChangeFocus = false;
            if (selectedComponent != null) {
                if (selectedComponent != visibleComponent && visibleComponent != null && SwingUtilities.findFocusOwner(visibleComponent) != null) {
                    shouldChangeFocus = true;
                }
                BasicTabbedPaneUI.this.setVisibleComponent(selectedComponent);
            }
            Insets contentInsets = BasicTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            Rectangle bounds = BasicTabbedPaneUI.this.tabPane.getBounds();
            int numChildren = BasicTabbedPaneUI.this.tabPane.getComponentCount();
            if (numChildren > 0) {
                int cw;
                int cy;
                int cx;
                int ty;
                int tx;
                int th;
                int tw;
                int ch = switch (tabPlacement) {
                    case 2 -> {
                        tw = BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabWidth);
                        th = bounds.height - insets.top - insets.bottom;
                        tx = insets.left;
                        ty = insets.top;
                        cx = tx + tw + contentInsets.left;
                        cy = ty + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - tw - contentInsets.left - contentInsets.right;
                        yield bounds.height - insets.top - insets.bottom - contentInsets.top - contentInsets.bottom;
                    }
                    case 4 -> {
                        tw = BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabWidth);
                        th = bounds.height - insets.top - insets.bottom;
                        tx = bounds.width - insets.right - tw;
                        ty = insets.top;
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - tw - contentInsets.left - contentInsets.right;
                        yield bounds.height - insets.top - insets.bottom - contentInsets.top - contentInsets.bottom;
                    }
                    case 3 -> {
                        tw = bounds.width - insets.left - insets.right;
                        th = BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabHeight);
                        tx = insets.left;
                        ty = bounds.height - insets.bottom - th;
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - contentInsets.left - contentInsets.right;
                        yield bounds.height - insets.top - insets.bottom - th - contentInsets.top - contentInsets.bottom;
                    }
                    default -> {
                        tw = bounds.width - insets.left - insets.right;
                        th = BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabHeight);
                        tx = insets.left;
                        ty = insets.top;
                        cx = tx + contentInsets.left;
                        cy = ty + th + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - contentInsets.left - contentInsets.right;
                        yield bounds.height - insets.top - insets.bottom - th - contentInsets.top - contentInsets.bottom;
                    }
                };
                for (int i = 0; i < numChildren; ++i) {
                    Component child = BasicTabbedPaneUI.this.tabPane.getComponent(i);
                    if (BasicTabbedPaneUI.this.tabScroller != null && child == BasicTabbedPaneUI.this.tabScroller.viewport) {
                        JViewport viewport = (JViewport)child;
                        Rectangle viewRect = viewport.getViewRect();
                        int vw = tw;
                        int vh = th;
                        Dimension butSize = BasicTabbedPaneUI.this.tabScroller.scrollForwardButton.getPreferredSize();
                        switch (tabPlacement) {
                            case 2: 
                            case 4: {
                                int totalTabHeight = BasicTabbedPaneUI.this.rects[tabCount - 1].y + BasicTabbedPaneUI.this.rects[tabCount - 1].height;
                                if (totalTabHeight <= th) break;
                                int n = vh = th > 2 * butSize.height ? th - 2 * butSize.height : 0;
                                if (totalTabHeight - viewRect.y > vh) break;
                                vh = totalTabHeight - viewRect.y;
                                break;
                            }
                            default: {
                                int totalTabWidth = BasicTabbedPaneUI.this.rects[tabCount - 1].x + BasicTabbedPaneUI.this.rects[tabCount - 1].width;
                                if (totalTabWidth <= tw) break;
                                int n = vw = tw > 2 * butSize.width ? tw - 2 * butSize.width : 0;
                                if (totalTabWidth - viewRect.x > vw) break;
                                vw = totalTabWidth - viewRect.x;
                            }
                        }
                        child.setBounds(tx, ty, vw, vh);
                        continue;
                    }
                    if (BasicTabbedPaneUI.this.tabScroller != null && (child == BasicTabbedPaneUI.this.tabScroller.scrollForwardButton || child == BasicTabbedPaneUI.this.tabScroller.scrollBackwardButton)) {
                        Component scrollbutton = child;
                        Dimension bsize = scrollbutton.getPreferredSize();
                        int bx = 0;
                        int by = 0;
                        int bw = bsize.width;
                        int bh = bsize.height;
                        boolean visible = false;
                        switch (tabPlacement) {
                            case 2: 
                            case 4: {
                                int totalTabHeight = BasicTabbedPaneUI.this.rects[tabCount - 1].y + BasicTabbedPaneUI.this.rects[tabCount - 1].height;
                                if (totalTabHeight <= th) break;
                                visible = true;
                                bx = tabPlacement == 2 ? tx + tw - bsize.width : tx;
                                by = child == BasicTabbedPaneUI.this.tabScroller.scrollForwardButton ? bounds.height - insets.bottom - bsize.height : bounds.height - insets.bottom - 2 * bsize.height;
                                break;
                            }
                            default: {
                                int totalTabWidth = BasicTabbedPaneUI.this.rects[tabCount - 1].x + BasicTabbedPaneUI.this.rects[tabCount - 1].width;
                                if (totalTabWidth <= tw) break;
                                visible = true;
                                bx = child == BasicTabbedPaneUI.this.tabScroller.scrollForwardButton ? bounds.width - insets.left - bsize.width : bounds.width - insets.left - 2 * bsize.width;
                                by = tabPlacement == 1 ? ty + th - bsize.height : ty;
                            }
                        }
                        child.setVisible(visible);
                        if (!visible) continue;
                        child.setBounds(bx, by, bw, bh);
                        continue;
                    }
                    child.setBounds(cx, cy, cw, ch);
                }
                super.layoutTabComponents();
                this.layoutCroppedEdge();
                if (shouldChangeFocus && !BasicTabbedPaneUI.this.requestFocusForVisibleComponent()) {
                    BasicTabbedPaneUI.this.tabPane.requestFocus();
                }
            }
        }

        private void layoutCroppedEdge() {
            BasicTabbedPaneUI.this.tabScroller.croppedEdge.resetParams();
            Rectangle viewRect = BasicTabbedPaneUI.this.tabScroller.viewport.getViewRect();
            block3: for (int i = 0; i < BasicTabbedPaneUI.this.rects.length; ++i) {
                Rectangle tabRect = BasicTabbedPaneUI.this.rects[i];
                switch (BasicTabbedPaneUI.this.tabPane.getTabPlacement()) {
                    case 2: 
                    case 4: {
                        int cropline = viewRect.y + viewRect.height;
                        if (tabRect.y >= cropline || tabRect.y + tabRect.height <= cropline) continue block3;
                        BasicTabbedPaneUI.this.tabScroller.croppedEdge.setParams(i, cropline - tabRect.y - 1, -BasicTabbedPaneUI.this.currentTabAreaInsets.left, 0);
                        continue block3;
                    }
                    default: {
                        int cropline = viewRect.x + viewRect.width;
                        if (tabRect.x >= cropline - 1 || tabRect.x + tabRect.width <= cropline) continue block3;
                        BasicTabbedPaneUI.this.tabScroller.croppedEdge.setParams(i, cropline - tabRect.x - 1, 0, -BasicTabbedPaneUI.this.currentTabAreaInsets.top);
                    }
                }
            }
        }

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            int i;
            FontMetrics metrics = BasicTabbedPaneUI.this.getFontMetrics();
            Dimension size = BasicTabbedPaneUI.this.tabPane.getSize();
            Insets insets = BasicTabbedPaneUI.this.tabPane.getInsets();
            Insets tabAreaInsets = BasicTabbedPaneUI.this.getTabAreaInsets(tabPlacement);
            int fontHeight = metrics.getHeight();
            int selectedIndex = BasicTabbedPaneUI.this.tabPane.getSelectedIndex();
            boolean verticalTabRuns = tabPlacement == 2 || tabPlacement == 4;
            boolean leftToRight = BasicGraphicsUtils.isLeftToRight(BasicTabbedPaneUI.this.tabPane);
            int x = tabAreaInsets.left;
            int y = tabAreaInsets.top;
            int totalWidth = 0;
            int totalHeight = 0;
            switch (tabPlacement) {
                case 2: 
                case 4: {
                    BasicTabbedPaneUI.this.maxTabWidth = BasicTabbedPaneUI.this.calculateMaxTabWidth(tabPlacement);
                    break;
                }
                default: {
                    BasicTabbedPaneUI.this.maxTabHeight = BasicTabbedPaneUI.this.calculateMaxTabHeight(tabPlacement);
                }
            }
            BasicTabbedPaneUI.this.runCount = 0;
            BasicTabbedPaneUI.this.selectedRun = -1;
            if (tabCount == 0) {
                return;
            }
            BasicTabbedPaneUI.this.selectedRun = 0;
            BasicTabbedPaneUI.this.runCount = 1;
            for (i = 0; i < tabCount; ++i) {
                Rectangle rect = BasicTabbedPaneUI.this.rects[i];
                if (!verticalTabRuns) {
                    if (i > 0) {
                        rect.x = BasicTabbedPaneUI.this.rects[i - 1].x + BasicTabbedPaneUI.this.rects[i - 1].width;
                    } else {
                        BasicTabbedPaneUI.this.tabRuns[0] = 0;
                        BasicTabbedPaneUI.this.maxTabWidth = 0;
                        totalHeight += BasicTabbedPaneUI.this.maxTabHeight;
                        rect.x = x;
                    }
                    rect.width = BasicTabbedPaneUI.this.calculateTabWidth(tabPlacement, i, metrics);
                    totalWidth = rect.x + rect.width;
                    BasicTabbedPaneUI.this.maxTabWidth = Math.max(BasicTabbedPaneUI.this.maxTabWidth, rect.width);
                    rect.y = y;
                    rect.height = BasicTabbedPaneUI.this.maxTabHeight;
                    continue;
                }
                if (i > 0) {
                    rect.y = BasicTabbedPaneUI.this.rects[i - 1].y + BasicTabbedPaneUI.this.rects[i - 1].height;
                } else {
                    BasicTabbedPaneUI.this.tabRuns[0] = 0;
                    BasicTabbedPaneUI.this.maxTabHeight = 0;
                    totalWidth = BasicTabbedPaneUI.this.maxTabWidth;
                    rect.y = y;
                }
                rect.height = BasicTabbedPaneUI.this.calculateTabHeight(tabPlacement, i, fontHeight);
                totalHeight = rect.y + rect.height;
                BasicTabbedPaneUI.this.maxTabHeight = Math.max(BasicTabbedPaneUI.this.maxTabHeight, rect.height);
                rect.x = x;
                rect.width = BasicTabbedPaneUI.this.maxTabWidth;
            }
            if (BasicTabbedPaneUI.this.tabsOverlapBorder) {
                this.padSelectedTab(tabPlacement, selectedIndex);
            }
            if (!leftToRight && !verticalTabRuns) {
                int rightMargin = size.width - (insets.right + tabAreaInsets.right);
                for (i = 0; i < tabCount; ++i) {
                    BasicTabbedPaneUI.this.rects[i].x = rightMargin - BasicTabbedPaneUI.this.rects[i].x - BasicTabbedPaneUI.this.rects[i].width;
                }
            }
            BasicTabbedPaneUI.this.tabScroller.tabPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
            BasicTabbedPaneUI.this.tabScroller.tabPanel.invalidate();
        }
    }

    public class TabbedPaneLayout
    implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return this.calculateSize(false);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return this.calculateSize(true);
        }

        protected Dimension calculateSize(boolean minimum) {
            int tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement();
            Insets insets = BasicTabbedPaneUI.this.tabPane.getInsets();
            Insets contentInsets = BasicTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            Insets tabAreaInsets = BasicTabbedPaneUI.this.getTabAreaInsets(tabPlacement);
            Dimension zeroSize = new Dimension(0, 0);
            int height = 0;
            int width = 0;
            int cWidth = 0;
            int cHeight = 0;
            for (int i = 0; i < BasicTabbedPaneUI.this.tabPane.getTabCount(); ++i) {
                Dimension size;
                Component component = BasicTabbedPaneUI.this.tabPane.getComponentAt(i);
                if (component == null) continue;
                Dimension dimension = size = minimum ? component.getMinimumSize() : component.getPreferredSize();
                if (size == null) continue;
                cHeight = Math.max(size.height, cHeight);
                cWidth = Math.max(size.width, cWidth);
            }
            width += cWidth;
            height += cHeight;
            switch (tabPlacement) {
                case 2: 
                case 4: {
                    height = Math.max(height, BasicTabbedPaneUI.this.calculateMaxTabHeight(tabPlacement));
                    int tabExtent = this.preferredTabAreaWidth(tabPlacement, height - tabAreaInsets.top - tabAreaInsets.bottom);
                    width += tabExtent;
                    break;
                }
                default: {
                    width = Math.max(width, BasicTabbedPaneUI.this.calculateMaxTabWidth(tabPlacement));
                    int tabExtent = this.preferredTabAreaHeight(tabPlacement, width - tabAreaInsets.left - tabAreaInsets.right);
                    height += tabExtent;
                }
            }
            return new Dimension(width + insets.left + insets.right + contentInsets.left + contentInsets.right, height + insets.bottom + insets.top + contentInsets.top + contentInsets.bottom);
        }

        protected int preferredTabAreaHeight(int tabPlacement, int width) {
            FontMetrics metrics = BasicTabbedPaneUI.this.getFontMetrics();
            int tabCount = BasicTabbedPaneUI.this.tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int rows = 1;
                int x = 0;
                int maxTabHeight = BasicTabbedPaneUI.this.calculateMaxTabHeight(tabPlacement);
                for (int i = 0; i < tabCount; ++i) {
                    int tabWidth = BasicTabbedPaneUI.this.calculateTabWidth(tabPlacement, i, metrics);
                    if (x != 0 && x + tabWidth > width) {
                        ++rows;
                        x = 0;
                    }
                    x += tabWidth;
                }
                total = BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, rows, maxTabHeight);
            }
            return total;
        }

        protected int preferredTabAreaWidth(int tabPlacement, int height) {
            FontMetrics metrics = BasicTabbedPaneUI.this.getFontMetrics();
            int tabCount = BasicTabbedPaneUI.this.tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int columns = 1;
                int y = 0;
                int fontHeight = metrics.getHeight();
                BasicTabbedPaneUI.this.maxTabWidth = BasicTabbedPaneUI.this.calculateMaxTabWidth(tabPlacement);
                for (int i = 0; i < tabCount; ++i) {
                    int tabHeight = BasicTabbedPaneUI.this.calculateTabHeight(tabPlacement, i, fontHeight);
                    if (y != 0 && y + tabHeight > height) {
                        ++columns;
                        y = 0;
                    }
                    y += tabHeight;
                }
                total = BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, columns, BasicTabbedPaneUI.this.maxTabWidth);
            }
            return total;
        }

        @Override
        public void layoutContainer(Container parent) {
            BasicTabbedPaneUI.this.setRolloverTab(-1);
            int tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement();
            Insets insets = BasicTabbedPaneUI.this.tabPane.getInsets();
            int selectedIndex = BasicTabbedPaneUI.this.tabPane.getSelectedIndex();
            Component visibleComponent = BasicTabbedPaneUI.this.getVisibleComponent();
            this.calculateLayoutInfo();
            Component selectedComponent = null;
            if (selectedIndex < 0) {
                if (visibleComponent != null) {
                    BasicTabbedPaneUI.this.setVisibleComponent(null);
                }
            } else {
                selectedComponent = BasicTabbedPaneUI.this.tabPane.getComponentAt(selectedIndex);
            }
            int totalTabWidth = 0;
            int totalTabHeight = 0;
            Insets contentInsets = BasicTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            boolean shouldChangeFocus = false;
            if (selectedComponent != null) {
                if (selectedComponent != visibleComponent && visibleComponent != null && SwingUtilities.findFocusOwner(visibleComponent) != null) {
                    shouldChangeFocus = true;
                }
                BasicTabbedPaneUI.this.setVisibleComponent(selectedComponent);
            }
            Rectangle bounds = BasicTabbedPaneUI.this.tabPane.getBounds();
            int numChildren = BasicTabbedPaneUI.this.tabPane.getComponentCount();
            if (numChildren > 0) {
                int cx;
                int cy = switch (tabPlacement) {
                    case 2 -> {
                        totalTabWidth = BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabWidth);
                        cx = insets.left + totalTabWidth + contentInsets.left;
                        yield insets.top + contentInsets.top;
                    }
                    case 4 -> {
                        totalTabWidth = BasicTabbedPaneUI.this.calculateTabAreaWidth(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabWidth);
                        cx = insets.left + contentInsets.left;
                        yield insets.top + contentInsets.top;
                    }
                    case 3 -> {
                        totalTabHeight = BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabHeight);
                        cx = insets.left + contentInsets.left;
                        yield insets.top + contentInsets.top;
                    }
                    default -> {
                        totalTabHeight = BasicTabbedPaneUI.this.calculateTabAreaHeight(tabPlacement, BasicTabbedPaneUI.this.runCount, BasicTabbedPaneUI.this.maxTabHeight);
                        cx = insets.left + contentInsets.left;
                        yield insets.top + totalTabHeight + contentInsets.top;
                    }
                };
                int cw = bounds.width - totalTabWidth - insets.left - insets.right - contentInsets.left - contentInsets.right;
                int ch = bounds.height - totalTabHeight - insets.top - insets.bottom - contentInsets.top - contentInsets.bottom;
                for (int i = 0; i < numChildren; ++i) {
                    Component child = BasicTabbedPaneUI.this.tabPane.getComponent(i);
                    if (child == BasicTabbedPaneUI.this.tabContainer) {
                        int tabContainerWidth = totalTabWidth == 0 ? bounds.width : totalTabWidth + insets.left + insets.right + contentInsets.left + contentInsets.right;
                        int tabContainerHeight = totalTabHeight == 0 ? bounds.height : totalTabHeight + insets.top + insets.bottom + contentInsets.top + contentInsets.bottom;
                        int tabContainerX = 0;
                        int tabContainerY = 0;
                        if (tabPlacement == 3) {
                            tabContainerY = bounds.height - tabContainerHeight;
                        } else if (tabPlacement == 4) {
                            tabContainerX = bounds.width - tabContainerWidth;
                        }
                        child.setBounds(tabContainerX, tabContainerY, tabContainerWidth, tabContainerHeight);
                        continue;
                    }
                    child.setBounds(cx, cy, cw, ch);
                }
            }
            this.layoutTabComponents();
            if (shouldChangeFocus && !BasicTabbedPaneUI.this.requestFocusForVisibleComponent()) {
                BasicTabbedPaneUI.this.tabPane.requestFocus();
            }
        }

        public void calculateLayoutInfo() {
            int tabCount = BasicTabbedPaneUI.this.tabPane.getTabCount();
            BasicTabbedPaneUI.this.assureRectsCreated(tabCount);
            this.calculateTabRects(BasicTabbedPaneUI.this.tabPane.getTabPlacement(), tabCount);
            BasicTabbedPaneUI.this.isRunsDirty = false;
        }

        private void layoutTabComponents() {
            if (BasicTabbedPaneUI.this.tabContainer == null) {
                return;
            }
            Rectangle rect = new Rectangle();
            Point delta = new Point(-BasicTabbedPaneUI.this.tabContainer.getX(), -BasicTabbedPaneUI.this.tabContainer.getY());
            if (BasicTabbedPaneUI.this.scrollableTabLayoutEnabled()) {
                BasicTabbedPaneUI.this.translatePointToTabPanel(0, 0, delta);
            }
            for (int i = 0; i < BasicTabbedPaneUI.this.tabPane.getTabCount(); ++i) {
                Component c = BasicTabbedPaneUI.this.tabPane.getTabComponentAt(i);
                if (c == null) continue;
                BasicTabbedPaneUI.this.getTabBounds(i, rect);
                Dimension preferredSize = c.getPreferredSize();
                Insets insets = BasicTabbedPaneUI.this.getTabInsets(BasicTabbedPaneUI.this.tabPane.getTabPlacement(), i);
                int outerX = rect.x + insets.left + delta.x;
                int outerY = rect.y + insets.top + delta.y;
                int outerWidth = rect.width - insets.left - insets.right;
                int outerHeight = rect.height - insets.top - insets.bottom;
                int x = outerX + (outerWidth - preferredSize.width) / 2;
                int y = outerY + (outerHeight - preferredSize.height) / 2;
                int tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement();
                boolean isSeleceted = i == BasicTabbedPaneUI.this.tabPane.getSelectedIndex();
                c.setBounds(x + BasicTabbedPaneUI.this.getTabLabelShiftX(tabPlacement, i, isSeleceted), y + BasicTabbedPaneUI.this.getTabLabelShiftY(tabPlacement, i, isSeleceted), preferredSize.width, preferredSize.height);
            }
        }

        /*
         * Exception decompiling
         */
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * java.lang.ClassCastException: class org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement cannot be cast to class org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement (org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement and org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement are in unnamed module of loader 'app')
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter$LValueSingleUsageCheckingRewriter.rewriteExpression(SwitchExpressionRewriter.java:96)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression.applyExpressionRewriter(LValueExpression.java:84)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation.applyExpressionRewriter(ArithmeticOperation.java:171)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter.rewriteExpression(AbstractExpressionRewriter.java:14)
             *     at org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple.rewriteExpressions(AssignmentSimple.java:167)
             *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFor.rewriteExpressions(StructuredFor.java:194)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:24)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
             *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.transformStructuredChildren(Block.java:421)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer.transform(ExpressionRewriterTransformer.java:25)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.transform(Op04StructuredStatement.java:680)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.rewriteBlockSwitches(SwitchExpressionRewriter.java:140)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchExpressionRewriter.transform(SwitchExpressionRewriter.java:71)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.switchExpression(Op04StructuredStatement.java:101)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:909)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        protected void rotateTabRuns(int tabPlacement, int selectedRun) {
            for (int i = 0; i < selectedRun; ++i) {
                int save = BasicTabbedPaneUI.this.tabRuns[0];
                for (int j = 1; j < BasicTabbedPaneUI.this.runCount; ++j) {
                    BasicTabbedPaneUI.this.tabRuns[j - 1] = BasicTabbedPaneUI.this.tabRuns[j];
                }
                BasicTabbedPaneUI.this.tabRuns[BasicTabbedPaneUI.this.runCount - 1] = save;
            }
        }

        protected void normalizeTabRuns(int tabPlacement, int tabCount, int start, int max) {
            boolean verticalTabRuns = tabPlacement == 2 || tabPlacement == 4;
            int run = BasicTabbedPaneUI.this.runCount - 1;
            boolean keepAdjusting = true;
            double weight = 1.25;
            while (keepAdjusting) {
                int prevLastLen;
                int end;
                int last = BasicTabbedPaneUI.this.lastTabInRun(tabCount, run);
                int prevLast = BasicTabbedPaneUI.this.lastTabInRun(tabCount, run - 1);
                if (!verticalTabRuns) {
                    end = BasicTabbedPaneUI.this.rects[last].x + BasicTabbedPaneUI.this.rects[last].width;
                    prevLastLen = (int)((double)BasicTabbedPaneUI.this.maxTabWidth * weight);
                } else {
                    end = BasicTabbedPaneUI.this.rects[last].y + BasicTabbedPaneUI.this.rects[last].height;
                    prevLastLen = (int)((double)BasicTabbedPaneUI.this.maxTabHeight * weight * 2.0);
                }
                if (max - end > prevLastLen) {
                    BasicTabbedPaneUI.this.tabRuns[run] = prevLast;
                    if (!verticalTabRuns) {
                        BasicTabbedPaneUI.this.rects[prevLast].x = start;
                    } else {
                        BasicTabbedPaneUI.this.rects[prevLast].y = start;
                    }
                    for (int i = prevLast + 1; i <= last; ++i) {
                        if (!verticalTabRuns) {
                            BasicTabbedPaneUI.this.rects[i].x = BasicTabbedPaneUI.this.rects[i - 1].x + BasicTabbedPaneUI.this.rects[i - 1].width;
                            continue;
                        }
                        BasicTabbedPaneUI.this.rects[i].y = BasicTabbedPaneUI.this.rects[i - 1].y + BasicTabbedPaneUI.this.rects[i - 1].height;
                    }
                } else if (run == BasicTabbedPaneUI.this.runCount - 1) {
                    keepAdjusting = false;
                }
                if (run - 1 > 0) {
                    --run;
                    continue;
                }
                run = BasicTabbedPaneUI.this.runCount - 1;
                weight += 0.25;
            }
        }

        protected void padTabRun(int tabPlacement, int start, int end, int max) {
            Rectangle lastRect = BasicTabbedPaneUI.this.rects[end];
            if (tabPlacement == 1 || tabPlacement == 3) {
                int runWidth = lastRect.x + lastRect.width - BasicTabbedPaneUI.this.rects[start].x;
                int deltaWidth = max - (lastRect.x + lastRect.width);
                float factor = (float)deltaWidth / (float)runWidth;
                for (int j = start; j <= end; ++j) {
                    Rectangle pastRect = BasicTabbedPaneUI.this.rects[j];
                    if (j > start) {
                        pastRect.x = BasicTabbedPaneUI.this.rects[j - 1].x + BasicTabbedPaneUI.this.rects[j - 1].width;
                    }
                    pastRect.width += Math.round((float)pastRect.width * factor);
                }
                lastRect.width = max - lastRect.x;
            } else {
                int runHeight = lastRect.y + lastRect.height - BasicTabbedPaneUI.this.rects[start].y;
                int deltaHeight = max - (lastRect.y + lastRect.height);
                float factor = (float)deltaHeight / (float)runHeight;
                for (int j = start; j <= end; ++j) {
                    Rectangle pastRect = BasicTabbedPaneUI.this.rects[j];
                    if (j > start) {
                        pastRect.y = BasicTabbedPaneUI.this.rects[j - 1].y + BasicTabbedPaneUI.this.rects[j - 1].height;
                    }
                    pastRect.height += Math.round((float)pastRect.height * factor);
                }
                lastRect.height = max - lastRect.y;
            }
        }

        protected void padSelectedTab(int tabPlacement, int selectedIndex) {
            if (selectedIndex >= 0) {
                Rectangle selRect = BasicTabbedPaneUI.this.rects[selectedIndex];
                Insets padInsets = BasicTabbedPaneUI.this.getSelectedTabPadInsets(tabPlacement);
                selRect.x -= padInsets.left;
                selRect.width += padInsets.left + padInsets.right;
                selRect.y -= padInsets.top;
                selRect.height += padInsets.top + padInsets.bottom;
                if (!BasicTabbedPaneUI.this.scrollableTabLayoutEnabled()) {
                    Dimension size = BasicTabbedPaneUI.this.tabPane.getSize();
                    Insets insets = BasicTabbedPaneUI.this.tabPane.getInsets();
                    if (tabPlacement == 2 || tabPlacement == 4) {
                        int bottom;
                        int top = insets.top - selRect.y;
                        if (top > 0) {
                            selRect.y += top;
                            selRect.height -= top;
                        }
                        if ((bottom = selRect.y + selRect.height + insets.bottom - size.height) > 0) {
                            selRect.height -= bottom;
                        }
                    } else {
                        int right;
                        int left = insets.left - selRect.x;
                        if (left > 0) {
                            selRect.x += left;
                            selRect.width -= left;
                        }
                        if ((right = selRect.x + selRect.width + insets.right - size.width) > 0) {
                            selRect.width -= right;
                        }
                    }
                }
            }
        }
    }

    private class ScrollableTabSupport
    implements ActionListener,
    ChangeListener {
        public ScrollableTabViewport viewport;
        public ScrollableTabPanel tabPanel;
        public JButton scrollForwardButton;
        public JButton scrollBackwardButton;
        public CroppedEdge croppedEdge;
        public int leadingTabIndex;
        private Point tabViewPosition = new Point(0, 0);

        ScrollableTabSupport(int tabPlacement) {
            this.viewport = new ScrollableTabViewport();
            this.tabPanel = new ScrollableTabPanel();
            this.viewport.setView(this.tabPanel);
            this.viewport.addChangeListener(this);
            this.croppedEdge = new CroppedEdge();
            this.createButtons();
        }

        void createButtons() {
            int tabPlacement;
            if (this.scrollForwardButton != null) {
                BasicTabbedPaneUI.this.tabPane.remove(this.scrollForwardButton);
                this.scrollForwardButton.removeActionListener(this);
                BasicTabbedPaneUI.this.tabPane.remove(this.scrollBackwardButton);
                this.scrollBackwardButton.removeActionListener(this);
            }
            if ((tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement()) == 1 || tabPlacement == 3) {
                this.scrollForwardButton = BasicTabbedPaneUI.this.createScrollButton(3);
                this.scrollBackwardButton = BasicTabbedPaneUI.this.createScrollButton(7);
            } else {
                this.scrollForwardButton = BasicTabbedPaneUI.this.createScrollButton(5);
                this.scrollBackwardButton = BasicTabbedPaneUI.this.createScrollButton(1);
            }
            this.scrollForwardButton.addActionListener(this);
            this.scrollBackwardButton.addActionListener(this);
            BasicTabbedPaneUI.this.tabPane.add(this.scrollForwardButton);
            BasicTabbedPaneUI.this.tabPane.add(this.scrollBackwardButton);
        }

        public void scrollForward(int tabPlacement) {
            Dimension viewSize = this.viewport.getViewSize();
            Rectangle viewRect = this.viewport.getViewRect();
            if (tabPlacement == 1 || tabPlacement == 3 ? viewRect.width >= viewSize.width - viewRect.x : viewRect.height >= viewSize.height - viewRect.y) {
                return;
            }
            this.setLeadingTabIndex(tabPlacement, this.leadingTabIndex + 1);
        }

        public void scrollBackward(int tabPlacement) {
            if (this.leadingTabIndex == 0) {
                return;
            }
            this.setLeadingTabIndex(tabPlacement, this.leadingTabIndex - 1);
        }

        public void setLeadingTabIndex(int tabPlacement, int index) {
            this.leadingTabIndex = index;
            Dimension viewSize = this.viewport.getViewSize();
            Rectangle viewRect = this.viewport.getViewRect();
            switch (tabPlacement) {
                case 1: 
                case 3: {
                    int n = this.tabViewPosition.x = this.leadingTabIndex == 0 ? 0 : BasicTabbedPaneUI.this.rects[this.leadingTabIndex].x;
                    if (viewSize.width - this.tabViewPosition.x >= viewRect.width) break;
                    Dimension extentSize = new Dimension(viewSize.width - this.tabViewPosition.x, viewRect.height);
                    this.viewport.setExtentSize(extentSize);
                    break;
                }
                case 2: 
                case 4: {
                    int n = this.tabViewPosition.y = this.leadingTabIndex == 0 ? 0 : BasicTabbedPaneUI.this.rects[this.leadingTabIndex].y;
                    if (viewSize.height - this.tabViewPosition.y >= viewRect.height) break;
                    Dimension extentSize = new Dimension(viewRect.width, viewSize.height - this.tabViewPosition.y);
                    this.viewport.setExtentSize(extentSize);
                }
            }
            this.viewport.setViewPosition(this.tabViewPosition);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            this.updateView();
        }

        private void updateView() {
            int tabPlacement = BasicTabbedPaneUI.this.tabPane.getTabPlacement();
            int tabCount = BasicTabbedPaneUI.this.tabPane.getTabCount();
            BasicTabbedPaneUI.this.assureRectsCreated(tabCount);
            Rectangle vpRect = this.viewport.getBounds();
            Dimension viewSize = this.viewport.getViewSize();
            Rectangle viewRect = this.viewport.getViewRect();
            this.leadingTabIndex = BasicTabbedPaneUI.this.getClosestTab(viewRect.x, viewRect.y);
            if (this.leadingTabIndex + 1 < tabCount) {
                switch (tabPlacement) {
                    case 1: 
                    case 3: {
                        if (BasicTabbedPaneUI.this.rects[this.leadingTabIndex].x >= viewRect.x) break;
                        ++this.leadingTabIndex;
                        break;
                    }
                    case 2: 
                    case 4: {
                        if (BasicTabbedPaneUI.this.rects[this.leadingTabIndex].y >= viewRect.y) break;
                        ++this.leadingTabIndex;
                    }
                }
            }
            Insets contentInsets = BasicTabbedPaneUI.this.getContentBorderInsets(tabPlacement);
            switch (tabPlacement) {
                case 2: {
                    BasicTabbedPaneUI.this.tabPane.repaint(vpRect.x + vpRect.width, vpRect.y, contentInsets.left, vpRect.height);
                    this.scrollBackwardButton.setEnabled(viewRect.y > 0 && this.leadingTabIndex > 0);
                    this.scrollForwardButton.setEnabled(this.leadingTabIndex < tabCount - 1 && viewSize.height - viewRect.y > viewRect.height);
                    break;
                }
                case 4: {
                    BasicTabbedPaneUI.this.tabPane.repaint(vpRect.x - contentInsets.right, vpRect.y, contentInsets.right, vpRect.height);
                    this.scrollBackwardButton.setEnabled(viewRect.y > 0 && this.leadingTabIndex > 0);
                    this.scrollForwardButton.setEnabled(this.leadingTabIndex < tabCount - 1 && viewSize.height - viewRect.y > viewRect.height);
                    break;
                }
                case 3: {
                    BasicTabbedPaneUI.this.tabPane.repaint(vpRect.x, vpRect.y - contentInsets.bottom, vpRect.width, contentInsets.bottom);
                    this.scrollBackwardButton.setEnabled(viewRect.x > 0 && this.leadingTabIndex > 0);
                    this.scrollForwardButton.setEnabled(this.leadingTabIndex < tabCount - 1 && viewSize.width - viewRect.x > viewRect.width);
                    break;
                }
                default: {
                    BasicTabbedPaneUI.this.tabPane.repaint(vpRect.x, vpRect.y + vpRect.height, vpRect.width, contentInsets.top);
                    this.scrollBackwardButton.setEnabled(viewRect.x > 0 && this.leadingTabIndex > 0);
                    this.scrollForwardButton.setEnabled(this.leadingTabIndex < tabCount - 1 && viewSize.width - viewRect.x > viewRect.width);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String actionKey;
            Action action;
            ActionMap map = BasicTabbedPaneUI.this.tabPane.getActionMap();
            if (map != null && (action = map.get(actionKey = e.getSource() == this.scrollForwardButton ? "scrollTabsForwardAction" : "scrollTabsBackwardAction")) != null && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(BasicTabbedPaneUI.this.tabPane, 1001, null, e.getWhen(), e.getModifiers()));
            }
        }

        public String toString() {
            return "viewport.viewSize=" + String.valueOf(this.viewport.getViewSize()) + "\nviewport.viewRectangle=" + String.valueOf(this.viewport.getViewRect()) + "\nleadingTabIndex=" + this.leadingTabIndex + "\ntabViewPosition=" + String.valueOf(this.tabViewPosition);
        }
    }

    private class ScrollableTabViewport
    extends JViewport
    implements UIResource {
        public ScrollableTabViewport() {
            this.setName("TabbedPane.scrollableViewport");
            this.setScrollMode(0);
            this.setOpaque(BasicTabbedPaneUI.this.tabPane.isOpaque());
            Color bgColor = UIManager.getColor("TabbedPane.tabAreaBackground");
            if (bgColor == null) {
                bgColor = BasicTabbedPaneUI.this.tabPane.getBackground();
            }
            this.setBackground(bgColor);
        }
    }

    private class TabContainer
    extends JPanel
    implements UIResource {
        private boolean notifyTabbedPane;

        public TabContainer() {
            super(null);
            this.notifyTabbedPane = true;
            this.setOpaque(false);
        }

        @Override
        public void remove(Component comp) {
            int index = BasicTabbedPaneUI.this.tabPane.indexOfTabComponent(comp);
            super.remove(comp);
            if (this.notifyTabbedPane && index != -1) {
                BasicTabbedPaneUI.this.tabPane.setTabComponentAt(index, null);
            }
        }

        private void removeUnusedTabComponents() {
            for (Component c : this.getComponents()) {
                int index;
                if (c instanceof UIResource || (index = BasicTabbedPaneUI.this.tabPane.indexOfTabComponent(c)) != -1) continue;
                super.remove(c);
            }
        }

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return BasicTabbedPaneUI.this.tabScroller != null && !BasicTabbedPaneUI.this.tabScroller.croppedEdge.isParamsSet();
        }

        @Override
        public void doLayout() {
            if (BasicTabbedPaneUI.this.scrollableTabLayoutEnabled()) {
                BasicTabbedPaneUI.this.tabScroller.tabPanel.repaint();
                BasicTabbedPaneUI.this.tabScroller.updateView();
            } else {
                BasicTabbedPaneUI.this.tabPane.repaint(this.getBounds());
            }
        }
    }

    private class ScrollableTabPanel
    extends JPanel
    implements UIResource {
        public ScrollableTabPanel() {
            super(null);
            this.setOpaque(BasicTabbedPaneUI.this.tabPane.isOpaque());
            Color background = BasicTabbedPaneUI.this.tabPane.getBackground();
            Color tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
            if (background instanceof UIResource && tabAreaBackground != null) {
                this.setBackground(tabAreaBackground);
            } else {
                this.setBackground(background);
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            BasicTabbedPaneUI.this.paintTabArea(g, BasicTabbedPaneUI.this.tabPane.getTabPlacement(), BasicTabbedPaneUI.this.tabPane.getSelectedIndex());
            if (BasicTabbedPaneUI.this.tabScroller.croppedEdge.isParamsSet() && BasicTabbedPaneUI.this.tabContainer == null) {
                Rectangle croppedRect = BasicTabbedPaneUI.this.rects[BasicTabbedPaneUI.this.tabScroller.croppedEdge.getTabIndex()];
                g.translate(croppedRect.x, croppedRect.y);
                BasicTabbedPaneUI.this.tabScroller.croppedEdge.paintComponent(g);
                g.translate(-croppedRect.x, -croppedRect.y);
            }
        }

        @Override
        public void doLayout() {
            if (this.getComponentCount() > 0) {
                Component child = this.getComponent(0);
                child.setBounds(0, 0, this.getWidth(), this.getHeight());
            }
        }
    }

    private static class ScrollableTabButton
    extends BasicArrowButton
    implements UIResource,
    SwingConstants {
        public ScrollableTabButton(int direction) {
            super(direction, UIManager.getColor("TabbedPane.selected"), UIManager.getColor("TabbedPane.shadow"), UIManager.getColor("TabbedPane.darkShadow"), UIManager.getColor("TabbedPane.highlight"));
        }
    }

    private class CroppedEdge
    extends JPanel
    implements UIResource {
        private Shape shape;
        private int tabIndex;
        private int cropline;
        private int cropx;
        private int cropy;

        public CroppedEdge() {
            this.setOpaque(false);
        }

        public void setParams(int tabIndex, int cropline, int cropx, int cropy) {
            this.tabIndex = tabIndex;
            this.cropline = cropline;
            this.cropx = cropx;
            this.cropy = cropy;
            Rectangle tabRect = BasicTabbedPaneUI.this.rects[tabIndex];
            this.setBounds(tabRect);
            this.shape = BasicTabbedPaneUI.createCroppedTabShape(BasicTabbedPaneUI.this.tabPane.getTabPlacement(), tabRect, cropline);
            if (this.getParent() == null && BasicTabbedPaneUI.this.tabContainer != null) {
                BasicTabbedPaneUI.this.tabContainer.add((Component)this, 0);
            }
        }

        public void resetParams() {
            this.shape = null;
            if (this.getParent() == BasicTabbedPaneUI.this.tabContainer && BasicTabbedPaneUI.this.tabContainer != null) {
                BasicTabbedPaneUI.this.tabContainer.remove(this);
            }
        }

        public boolean isParamsSet() {
            return this.shape != null;
        }

        public int getTabIndex() {
            return this.tabIndex;
        }

        public int getCropline() {
            return this.cropline;
        }

        public int getCroppedSideWidth() {
            return 3;
        }

        private Color getBgColor() {
            Color bg;
            Container parent = BasicTabbedPaneUI.this.tabPane.getParent();
            if (parent != null && (bg = parent.getBackground()) != null) {
                return bg;
            }
            return UIManager.getColor("control");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.isParamsSet() && g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D)g;
                g2.clipRect(0, 0, this.getWidth(), this.getHeight());
                g2.setColor(this.getBgColor());
                g2.translate(this.cropx, this.cropy);
                g2.fill(this.shape);
                BasicTabbedPaneUI.this.paintCroppedTabEdge(g);
                g2.translate(-this.cropx, -this.cropy);
            }
        }
    }

    private class Handler
    implements ChangeListener,
    ContainerListener,
    FocusListener,
    MouseListener,
    MouseMotionListener,
    PropertyChangeListener {
        private Handler() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            JTabbedPane pane = (JTabbedPane)e.getSource();
            String name = e.getPropertyName();
            boolean isScrollLayout = BasicTabbedPaneUI.this.scrollableTabLayoutEnabled();
            if (name == "mnemonicAt") {
                BasicTabbedPaneUI.this.updateMnemonics();
                pane.repaint();
            } else if (name == "displayedMnemonicIndexAt") {
                pane.repaint();
            } else if (name == "indexForTitle") {
                BasicTabbedPaneUI.this.calculatedBaseline = false;
                Integer index = (Integer)e.getNewValue();
                this.updateHtmlViews(index, false);
            } else if (name == "tabLayoutPolicy") {
                BasicTabbedPaneUI.this.uninstallUI(pane);
                BasicTabbedPaneUI.this.installUI(pane);
                BasicTabbedPaneUI.this.calculatedBaseline = false;
            } else if (name == "tabPlacement") {
                if (BasicTabbedPaneUI.this.scrollableTabLayoutEnabled()) {
                    BasicTabbedPaneUI.this.tabScroller.createButtons();
                }
                BasicTabbedPaneUI.this.calculatedBaseline = false;
            } else if (name == "opaque" && isScrollLayout) {
                boolean newVal = (Boolean)e.getNewValue();
                BasicTabbedPaneUI.this.tabScroller.tabPanel.setOpaque(newVal);
                BasicTabbedPaneUI.this.tabScroller.viewport.setOpaque(newVal);
            } else if (name == "background" && isScrollLayout) {
                Color newVal = (Color)e.getNewValue();
                BasicTabbedPaneUI.this.tabScroller.tabPanel.setBackground(newVal);
                BasicTabbedPaneUI.this.tabScroller.viewport.setBackground(newVal);
                Color newColor = BasicTabbedPaneUI.this.selectedColor == null ? newVal : BasicTabbedPaneUI.this.selectedColor;
                BasicTabbedPaneUI.this.tabScroller.scrollForwardButton.setBackground(newColor);
                BasicTabbedPaneUI.this.tabScroller.scrollBackwardButton.setBackground(newColor);
            } else if (name == "indexForTabComponent") {
                Component c;
                if (BasicTabbedPaneUI.this.tabContainer != null) {
                    BasicTabbedPaneUI.this.tabContainer.removeUnusedTabComponents();
                }
                if ((c = BasicTabbedPaneUI.this.tabPane.getTabComponentAt((Integer)e.getNewValue())) != null) {
                    if (BasicTabbedPaneUI.this.tabContainer == null) {
                        BasicTabbedPaneUI.this.installTabContainer();
                    } else {
                        BasicTabbedPaneUI.this.tabContainer.add(c);
                    }
                }
                BasicTabbedPaneUI.this.tabPane.revalidate();
                BasicTabbedPaneUI.this.tabPane.repaint();
                BasicTabbedPaneUI.this.calculatedBaseline = false;
            } else if (name == "indexForNullComponent") {
                BasicTabbedPaneUI.this.isRunsDirty = true;
                this.updateHtmlViews((Integer)e.getNewValue(), true);
            } else if (name == "font" || SwingUtilities2.isScaleChanged(e)) {
                BasicTabbedPaneUI.this.calculatedBaseline = false;
            }
        }

        private void updateHtmlViews(int index, boolean inserted) {
            String title = BasicTabbedPaneUI.this.tabPane.getTitleAt(index);
            Boolean htmlDisabled = (Boolean)BasicTabbedPaneUI.this.tabPane.getClientProperty("html.disable");
            boolean isHTML = BasicHTML.isHTMLString(title);
            if (isHTML && !Boolean.TRUE.equals(htmlDisabled)) {
                if (BasicTabbedPaneUI.this.htmlViews == null) {
                    BasicTabbedPaneUI.this.htmlViews = BasicTabbedPaneUI.this.createHTMLVector();
                } else {
                    View v = BasicHTML.createHTMLView(BasicTabbedPaneUI.this.tabPane, title);
                    this.setHtmlView(v, inserted, index);
                }
            } else if (BasicTabbedPaneUI.this.htmlViews != null) {
                this.setHtmlView(null, inserted, index);
            }
            BasicTabbedPaneUI.this.updateMnemonics();
        }

        private void setHtmlView(View v, boolean inserted, int index) {
            if (inserted || index >= BasicTabbedPaneUI.this.htmlViews.size()) {
                BasicTabbedPaneUI.this.htmlViews.insertElementAt(v, index);
            } else {
                BasicTabbedPaneUI.this.htmlViews.setElementAt(v, index);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabPane = (JTabbedPane)e.getSource();
            tabPane.revalidate();
            tabPane.repaint();
            BasicTabbedPaneUI.this.setFocusIndex(tabPane.getSelectedIndex(), false);
            if (tabPane.getLayout() instanceof TabbedPaneScrollLayout) {
                BasicTabbedPaneUI.this.ensureCurrentLayout();
                int index = tabPane.getSelectedIndex();
                if (index < BasicTabbedPaneUI.this.rects.length && index != -1) {
                    BasicTabbedPaneUI.this.tabScroller.tabPanel.scrollRectToVisible((Rectangle)BasicTabbedPaneUI.this.rects[index].clone());
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            BasicTabbedPaneUI.this.setRolloverTab(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            BasicTabbedPaneUI.this.setRolloverTab(-1);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!BasicTabbedPaneUI.this.tabPane.isEnabled()) {
                return;
            }
            int tabIndex = BasicTabbedPaneUI.this.tabForCoordinate(BasicTabbedPaneUI.this.tabPane, e.getX(), e.getY());
            if (tabIndex >= 0 && BasicTabbedPaneUI.this.tabPane.isEnabledAt(tabIndex)) {
                if (tabIndex != BasicTabbedPaneUI.this.tabPane.getSelectedIndex()) {
                    BasicTabbedPaneUI.this.tabPane.setSelectedIndex(tabIndex);
                } else if (BasicTabbedPaneUI.this.tabPane.isRequestFocusEnabled()) {
                    BasicTabbedPaneUI.this.tabPane.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            BasicTabbedPaneUI.this.setRolloverTab(e.getX(), e.getY());
        }

        @Override
        public void focusGained(FocusEvent e) {
            BasicTabbedPaneUI.this.setFocusIndex(BasicTabbedPaneUI.this.tabPane.getSelectedIndex(), true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicTabbedPaneUI.this.repaintTab(BasicTabbedPaneUI.this.focusIndex);
        }

        @Override
        public void componentAdded(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane)e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }
            BasicTabbedPaneUI.this.isRunsDirty = true;
            this.updateHtmlViews(tp.indexOfComponent(child), true);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane)e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }
            Integer indexObj = (Integer)tp.getClientProperty("__index_to_remove__");
            if (indexObj != null) {
                int index = indexObj;
                if (BasicTabbedPaneUI.this.htmlViews != null && BasicTabbedPaneUI.this.htmlViews.size() > index) {
                    BasicTabbedPaneUI.this.htmlViews.removeElementAt(index);
                }
                tp.putClientProperty("__index_to_remove__", null);
            }
            BasicTabbedPaneUI.this.isRunsDirty = true;
            BasicTabbedPaneUI.this.updateMnemonics();
            BasicTabbedPaneUI.this.validateFocusIndex();
        }
    }

    public class FocusHandler
    extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            BasicTabbedPaneUI.this.getHandler().focusGained(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicTabbedPaneUI.this.getHandler().focusLost(e);
        }
    }

    public class MouseHandler
    extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            BasicTabbedPaneUI.this.getHandler().mousePressed(e);
        }
    }

    public class TabSelectionHandler
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicTabbedPaneUI.this.getHandler().stateChanged(e);
        }
    }

    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicTabbedPaneUI.this.getHandler().propertyChange(e);
        }
    }
}

