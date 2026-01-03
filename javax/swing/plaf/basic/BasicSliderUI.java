/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.LazyActionMap;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicSliderUI
extends SliderUI {
    private static final Actions SHARED_ACTION = new Actions();
    public static final int POSITIVE_SCROLL = 1;
    public static final int NEGATIVE_SCROLL = -1;
    public static final int MIN_SCROLL = -2;
    public static final int MAX_SCROLL = 2;
    protected Timer scrollTimer;
    protected JSlider slider;
    protected Insets focusInsets = null;
    protected Insets insetCache = null;
    protected boolean leftToRightCache = true;
    protected Rectangle focusRect = null;
    protected Rectangle contentRect = null;
    protected Rectangle labelRect = null;
    protected Rectangle tickRect = null;
    protected Rectangle trackRect = null;
    protected Rectangle thumbRect = null;
    protected int trackBuffer = 0;
    private transient boolean isDragging;
    protected TrackListener trackListener;
    protected ChangeListener changeListener;
    protected ComponentListener componentListener;
    protected FocusListener focusListener;
    protected ScrollListener scrollListener;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;
    private int lastValue;
    private Color shadowColor;
    private Color highlightColor;
    private Color focusColor;
    private boolean checkedLabelBaselines;
    private boolean sameLabelBaselines;
    private static Rectangle unionRect = new Rectangle();

    public BasicSliderUI() {
    }

    protected Color getShadowColor() {
        return this.shadowColor;
    }

    protected Color getHighlightColor() {
        return this.highlightColor;
    }

    protected Color getFocusColor() {
        return this.focusColor;
    }

    protected boolean isDragging() {
        return this.isDragging;
    }

    public static ComponentUI createUI(JComponent b) {
        return new BasicSliderUI((JSlider)b);
    }

    public BasicSliderUI(JSlider b) {
    }

    @Override
    public void installUI(JComponent c) {
        this.slider = (JSlider)c;
        this.checkedLabelBaselines = false;
        this.slider.setEnabled(this.slider.isEnabled());
        LookAndFeel.installProperty(this.slider, "opaque", Boolean.TRUE);
        this.isDragging = false;
        this.trackListener = this.createTrackListener(this.slider);
        this.changeListener = this.createChangeListener(this.slider);
        this.componentListener = this.createComponentListener(this.slider);
        this.focusListener = this.createFocusListener(this.slider);
        this.scrollListener = this.createScrollListener(this.slider);
        this.propertyChangeListener = this.createPropertyChangeListener(this.slider);
        this.installDefaults(this.slider);
        this.installListeners(this.slider);
        this.installKeyboardActions(this.slider);
        this.scrollTimer = new Timer(100, this.scrollListener);
        this.scrollTimer.setInitialDelay(300);
        this.insetCache = this.slider.getInsets();
        this.leftToRightCache = BasicGraphicsUtils.isLeftToRight(this.slider);
        this.focusRect = new Rectangle();
        this.contentRect = new Rectangle();
        this.labelRect = new Rectangle();
        this.tickRect = new Rectangle();
        this.trackRect = new Rectangle();
        this.thumbRect = new Rectangle();
        this.lastValue = this.slider.getValue();
        this.calculateGeometry();
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (c != this.slider) {
            throw new IllegalComponentStateException(String.valueOf(this) + " was asked to deinstall() " + String.valueOf(c) + " when it only knows about " + String.valueOf(this.slider) + ".");
        }
        this.scrollTimer.stop();
        this.scrollTimer = null;
        this.uninstallDefaults(this.slider);
        this.uninstallListeners(this.slider);
        this.uninstallKeyboardActions(this.slider);
        this.insetCache = null;
        this.leftToRightCache = true;
        this.focusRect = null;
        this.contentRect = null;
        this.labelRect = null;
        this.tickRect = null;
        this.trackRect = null;
        this.thumbRect = null;
        this.trackListener = null;
        this.changeListener = null;
        this.componentListener = null;
        this.focusListener = null;
        this.scrollListener = null;
        this.propertyChangeListener = null;
        this.slider = null;
    }

    protected void installDefaults(JSlider slider) {
        LookAndFeel.installBorder(slider, "Slider.border");
        LookAndFeel.installColorsAndFont(slider, "Slider.background", "Slider.foreground", "Slider.font");
        this.highlightColor = UIManager.getColor("Slider.highlight");
        this.shadowColor = UIManager.getColor("Slider.shadow");
        this.focusColor = UIManager.getColor("Slider.focus");
        this.focusInsets = (Insets)UIManager.get("Slider.focusInsets");
        if (this.focusInsets == null) {
            this.focusInsets = new InsetsUIResource(2, 2, 2, 2);
        }
    }

    protected void uninstallDefaults(JSlider slider) {
        LookAndFeel.uninstallBorder(slider);
        this.focusInsets = null;
    }

    protected TrackListener createTrackListener(JSlider slider) {
        return new TrackListener();
    }

    protected ChangeListener createChangeListener(JSlider slider) {
        return this.getHandler();
    }

    protected ComponentListener createComponentListener(JSlider slider) {
        return this.getHandler();
    }

    protected FocusListener createFocusListener(JSlider slider) {
        return this.getHandler();
    }

    protected ScrollListener createScrollListener(JSlider slider) {
        return new ScrollListener();
    }

    protected PropertyChangeListener createPropertyChangeListener(JSlider slider) {
        return this.getHandler();
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void installListeners(JSlider slider) {
        slider.addMouseListener(this.trackListener);
        slider.addMouseMotionListener(this.trackListener);
        slider.addFocusListener(this.focusListener);
        slider.addComponentListener(this.componentListener);
        slider.addPropertyChangeListener(this.propertyChangeListener);
        slider.getModel().addChangeListener(this.changeListener);
    }

    protected void uninstallListeners(JSlider slider) {
        slider.removeMouseListener(this.trackListener);
        slider.removeMouseMotionListener(this.trackListener);
        slider.removeFocusListener(this.focusListener);
        slider.removeComponentListener(this.componentListener);
        slider.removePropertyChangeListener(this.propertyChangeListener);
        slider.getModel().removeChangeListener(this.changeListener);
        this.handler = null;
    }

    protected void installKeyboardActions(JSlider slider) {
        InputMap km = this.getInputMap(0, slider);
        SwingUtilities.replaceUIInputMap(slider, 0, km);
        LazyActionMap.installLazyActionMap(slider, BasicSliderUI.class, "Slider.actionMap");
    }

    InputMap getInputMap(int condition, JSlider slider) {
        if (condition == 0) {
            InputMap rtlKeyMap;
            InputMap keyMap = (InputMap)DefaultLookup.get(slider, this, "Slider.focusInputMap");
            if (slider.getComponentOrientation().isLeftToRight() || (rtlKeyMap = (InputMap)DefaultLookup.get(slider, this, "Slider.focusInputMap.RightToLeft")) == null) {
                return keyMap;
            }
            rtlKeyMap.setParent(keyMap);
            return rtlKeyMap;
        }
        return null;
    }

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("positiveUnitIncrement"));
        map.put(new Actions("positiveBlockIncrement"));
        map.put(new Actions("negativeUnitIncrement"));
        map.put(new Actions("negativeBlockIncrement"));
        map.put(new Actions("minScroll"));
        map.put(new Actions("maxScroll"));
    }

    protected void uninstallKeyboardActions(JSlider slider) {
        SwingUtilities.replaceUIActionMap(slider, null);
        SwingUtilities.replaceUIInputMap(slider, 0, null);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        if (this.slider.getPaintLabels() && this.labelsHaveSameBaselines()) {
            Integer value;
            FontMetrics metrics = this.slider.getFontMetrics(this.slider.getFont());
            Insets insets = this.slider.getInsets();
            Dimension thumbSize = this.getThumbSize();
            if (this.slider.getOrientation() == 0) {
                int thumbHeight;
                int tickLength = this.getTickLength();
                int contentHeight = height - insets.top - insets.bottom - this.focusInsets.top - this.focusInsets.bottom;
                int centerSpacing = thumbHeight = thumbSize.height;
                if (this.slider.getPaintTicks()) {
                    centerSpacing += tickLength;
                }
                int trackY = insets.top + this.focusInsets.top + (contentHeight - (centerSpacing += this.getHeightOfTallestLabel()) - 1) / 2;
                int trackHeight = thumbHeight;
                int tickY = trackY + trackHeight;
                int tickHeight = tickLength;
                if (!this.slider.getPaintTicks()) {
                    tickHeight = 0;
                }
                int labelY = tickY + tickHeight;
                return labelY + metrics.getAscent();
            }
            boolean inverted = this.slider.getInverted();
            Integer n = value = inverted ? this.getLowestValue() : this.getHighestValue();
            if (value != null) {
                int thumbHeight = thumbSize.height;
                int trackBuffer = Math.max(metrics.getHeight() / 2, thumbHeight / 2);
                int contentY = this.focusInsets.top + insets.top;
                int trackY = contentY + trackBuffer;
                int trackHeight = height - this.focusInsets.top - this.focusInsets.bottom - insets.top - insets.bottom - trackBuffer - trackBuffer;
                int yPosition = this.yPositionForValue(value, trackY, trackHeight);
                return yPosition - metrics.getHeight() / 2 + metrics.getAscent();
            }
        }
        return 0;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.OTHER;
    }

    protected boolean labelsHaveSameBaselines() {
        if (!this.checkedLabelBaselines) {
            this.checkedLabelBaselines = true;
            Dictionary dictionary = this.slider.getLabelTable();
            if (dictionary != null) {
                this.sameLabelBaselines = true;
                Enumeration elements = dictionary.elements();
                int baseline = -1;
                while (elements.hasMoreElements()) {
                    JComponent label = (JComponent)elements.nextElement();
                    Dimension pref = label.getPreferredSize();
                    int labelBaseline = label.getBaseline(pref.width, pref.height);
                    if (labelBaseline >= 0) {
                        if (baseline == -1) {
                            baseline = labelBaseline;
                            continue;
                        }
                        if (baseline == labelBaseline) continue;
                        this.sameLabelBaselines = false;
                    } else {
                        this.sameLabelBaselines = false;
                    }
                    break;
                }
            } else {
                this.sameLabelBaselines = false;
            }
        }
        return this.sameLabelBaselines;
    }

    public Dimension getPreferredHorizontalSize() {
        Dimension horizDim = (Dimension)DefaultLookup.get(this.slider, this, "Slider.horizontalSize");
        if (horizDim == null) {
            horizDim = new Dimension(200, 21);
        }
        return horizDim;
    }

    public Dimension getPreferredVerticalSize() {
        Dimension vertDim = (Dimension)DefaultLookup.get(this.slider, this, "Slider.verticalSize");
        if (vertDim == null) {
            vertDim = new Dimension(21, 200);
        }
        return vertDim;
    }

    public Dimension getMinimumHorizontalSize() {
        Dimension minHorizDim = (Dimension)DefaultLookup.get(this.slider, this, "Slider.minimumHorizontalSize");
        if (minHorizDim == null) {
            minHorizDim = new Dimension(36, 21);
        }
        return minHorizDim;
    }

    public Dimension getMinimumVerticalSize() {
        Dimension minVertDim = (Dimension)DefaultLookup.get(this.slider, this, "Slider.minimumVerticalSize");
        if (minVertDim == null) {
            minVertDim = new Dimension(21, 36);
        }
        return minVertDim;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d;
        this.recalculateIfInsetsChanged();
        if (this.slider.getOrientation() == 1) {
            d = new Dimension(this.getPreferredVerticalSize());
            d.width = this.insetCache.left + this.insetCache.right;
            d.width += this.focusInsets.left + this.focusInsets.right;
            d.width += this.trackRect.width + this.tickRect.width + this.labelRect.width;
        } else {
            d = new Dimension(this.getPreferredHorizontalSize());
            d.height = this.insetCache.top + this.insetCache.bottom;
            d.height += this.focusInsets.top + this.focusInsets.bottom;
            d.height += this.trackRect.height + this.tickRect.height + this.labelRect.height;
        }
        return d;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d;
        this.recalculateIfInsetsChanged();
        if (this.slider.getOrientation() == 1) {
            d = new Dimension(this.getMinimumVerticalSize());
            d.width = this.insetCache.left + this.insetCache.right;
            d.width += this.focusInsets.left + this.focusInsets.right;
            d.width += this.trackRect.width + this.tickRect.width + this.labelRect.width;
        } else {
            d = new Dimension(this.getMinimumHorizontalSize());
            d.height = this.insetCache.top + this.insetCache.bottom;
            d.height += this.focusInsets.top + this.focusInsets.bottom;
            d.height += this.trackRect.height + this.tickRect.height + this.labelRect.height;
        }
        return d;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = this.getPreferredSize(c);
        if (this.slider.getOrientation() == 1) {
            d.height = Short.MAX_VALUE;
        } else {
            d.width = Short.MAX_VALUE;
        }
        return d;
    }

    protected void calculateGeometry() {
        this.calculateFocusRect();
        this.calculateContentRect();
        this.calculateThumbSize();
        this.calculateTrackBuffer();
        this.calculateTrackRect();
        this.calculateTickRect();
        this.calculateLabelRect();
        this.calculateThumbLocation();
    }

    protected void calculateFocusRect() {
        this.focusRect.x = this.insetCache.left;
        this.focusRect.y = this.insetCache.top;
        this.focusRect.width = this.slider.getWidth() - (this.insetCache.left + this.insetCache.right);
        this.focusRect.height = this.slider.getHeight() - (this.insetCache.top + this.insetCache.bottom);
    }

    protected void calculateThumbSize() {
        Dimension size = this.getThumbSize();
        this.thumbRect.setSize(size.width, size.height);
    }

    protected void calculateContentRect() {
        this.contentRect.x = this.focusRect.x + this.focusInsets.left;
        this.contentRect.y = this.focusRect.y + this.focusInsets.top;
        this.contentRect.width = this.focusRect.width - (this.focusInsets.left + this.focusInsets.right);
        this.contentRect.height = this.focusRect.height - (this.focusInsets.top + this.focusInsets.bottom);
    }

    private int getTickSpacing() {
        int majorTickSpacing = this.slider.getMajorTickSpacing();
        int minorTickSpacing = this.slider.getMinorTickSpacing();
        int result = minorTickSpacing > 0 ? minorTickSpacing : (majorTickSpacing > 0 ? majorTickSpacing : 0);
        return result;
    }

    protected void calculateThumbLocation() {
        if (this.slider.getSnapToTicks()) {
            int sliderValue;
            int snappedValue = sliderValue = this.slider.getValue();
            int tickSpacing = this.getTickSpacing();
            if (tickSpacing != 0) {
                if ((sliderValue - this.slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float)(sliderValue - this.slider.getMinimum()) / (float)tickSpacing;
                    int whichTick = Math.round(temp);
                    if ((double)(temp - (float)((int)temp)) == 0.5 && sliderValue < this.lastValue) {
                        --whichTick;
                    }
                    snappedValue = this.slider.getMinimum() + whichTick * tickSpacing;
                }
                if (snappedValue != sliderValue) {
                    this.slider.setValue(snappedValue);
                }
            }
        }
        if (this.slider.getOrientation() == 0) {
            valuePosition = this.xPositionForValue(this.slider.getValue());
            this.thumbRect.x = valuePosition - this.thumbRect.width / 2;
            this.thumbRect.y = this.trackRect.y;
        } else {
            valuePosition = this.yPositionForValue(this.slider.getValue());
            this.thumbRect.x = this.trackRect.x;
            this.thumbRect.y = valuePosition - this.thumbRect.height / 2;
        }
    }

    protected void calculateTrackBuffer() {
        if (this.slider.getPaintLabels() && this.slider.getLabelTable() != null) {
            Component highLabel = this.getHighestValueLabel();
            Component lowLabel = this.getLowestValueLabel();
            if (this.slider.getOrientation() == 0) {
                this.trackBuffer = Math.max(highLabel.getBounds().width, lowLabel.getBounds().width) / 2;
                this.trackBuffer = Math.max(this.trackBuffer, this.thumbRect.width / 2);
            } else {
                this.trackBuffer = Math.max(highLabel.getBounds().height, lowLabel.getBounds().height) / 2;
                this.trackBuffer = Math.max(this.trackBuffer, this.thumbRect.height / 2);
            }
        } else {
            this.trackBuffer = this.slider.getOrientation() == 0 ? this.thumbRect.width / 2 : this.thumbRect.height / 2;
        }
    }

    protected void calculateTrackRect() {
        if (this.slider.getOrientation() == 0) {
            int centerSpacing = this.thumbRect.height;
            if (this.slider.getPaintTicks()) {
                centerSpacing += this.getTickLength();
            }
            if (this.slider.getPaintLabels()) {
                centerSpacing += this.getHeightOfTallestLabel();
            }
            this.trackRect.x = this.contentRect.x + this.trackBuffer;
            this.trackRect.y = this.contentRect.y + (this.contentRect.height - centerSpacing - 1) / 2;
            this.trackRect.width = this.contentRect.width - this.trackBuffer * 2;
            this.trackRect.height = this.thumbRect.height;
        } else {
            int centerSpacing = this.thumbRect.width;
            if (BasicGraphicsUtils.isLeftToRight(this.slider)) {
                if (this.slider.getPaintTicks()) {
                    centerSpacing += this.getTickLength();
                }
                if (this.slider.getPaintLabels()) {
                    centerSpacing += this.getWidthOfWidestLabel();
                }
            } else {
                if (this.slider.getPaintTicks()) {
                    centerSpacing -= this.getTickLength();
                }
                if (this.slider.getPaintLabels()) {
                    centerSpacing -= this.getWidthOfWidestLabel();
                }
            }
            this.trackRect.x = this.contentRect.x + (this.contentRect.width - centerSpacing - 1) / 2;
            this.trackRect.y = this.contentRect.y + this.trackBuffer;
            this.trackRect.width = this.thumbRect.width;
            this.trackRect.height = this.contentRect.height - this.trackBuffer * 2;
        }
    }

    protected int getTickLength() {
        return 8;
    }

    protected void calculateTickRect() {
        if (this.slider.getOrientation() == 0) {
            this.tickRect.x = this.trackRect.x;
            this.tickRect.y = this.trackRect.y + this.trackRect.height;
            this.tickRect.width = this.trackRect.width;
            this.tickRect.height = this.slider.getPaintTicks() ? this.getTickLength() : 0;
        } else {
            this.tickRect.width = this.slider.getPaintTicks() ? this.getTickLength() : 0;
            this.tickRect.x = BasicGraphicsUtils.isLeftToRight(this.slider) ? this.trackRect.x + this.trackRect.width : this.trackRect.x - this.tickRect.width;
            this.tickRect.y = this.trackRect.y;
            this.tickRect.height = this.trackRect.height;
        }
    }

    protected void calculateLabelRect() {
        if (this.slider.getPaintLabels()) {
            if (this.slider.getOrientation() == 0) {
                this.labelRect.x = this.tickRect.x - this.trackBuffer;
                this.labelRect.y = this.tickRect.y + this.tickRect.height;
                this.labelRect.width = this.tickRect.width + this.trackBuffer * 2;
                this.labelRect.height = this.getHeightOfTallestLabel();
            } else {
                if (BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    this.labelRect.x = this.tickRect.x + this.tickRect.width;
                    this.labelRect.width = this.getWidthOfWidestLabel();
                } else {
                    this.labelRect.width = this.getWidthOfWidestLabel();
                    this.labelRect.x = this.tickRect.x - this.labelRect.width;
                }
                this.labelRect.y = this.tickRect.y - this.trackBuffer;
                this.labelRect.height = this.tickRect.height + this.trackBuffer * 2;
            }
        } else if (this.slider.getOrientation() == 0) {
            this.labelRect.x = this.tickRect.x;
            this.labelRect.y = this.tickRect.y + this.tickRect.height;
            this.labelRect.width = this.tickRect.width;
            this.labelRect.height = 0;
        } else {
            this.labelRect.x = BasicGraphicsUtils.isLeftToRight(this.slider) ? this.tickRect.x + this.tickRect.width : this.tickRect.x;
            this.labelRect.y = this.tickRect.y;
            this.labelRect.width = 0;
            this.labelRect.height = this.tickRect.height;
        }
    }

    protected Dimension getThumbSize() {
        Dimension size = new Dimension();
        if (this.slider.getOrientation() == 1) {
            size.width = 20;
            size.height = 11;
        } else {
            size.width = 11;
            size.height = 20;
        }
        return size;
    }

    protected int getWidthOfWidestLabel() {
        Dictionary dictionary = this.slider.getLabelTable();
        int widest = 0;
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                JComponent label = (JComponent)dictionary.get(keys.nextElement());
                widest = Math.max(label.getPreferredSize().width, widest);
            }
        }
        return widest;
    }

    protected int getHeightOfTallestLabel() {
        Dictionary dictionary = this.slider.getLabelTable();
        int tallest = 0;
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                JComponent label = (JComponent)dictionary.get(keys.nextElement());
                tallest = Math.max(label.getPreferredSize().height, tallest);
            }
        }
        return tallest;
    }

    protected int getWidthOfHighValueLabel() {
        Component label = this.getHighestValueLabel();
        int width = 0;
        if (label != null) {
            width = label.getPreferredSize().width;
        }
        return width;
    }

    protected int getWidthOfLowValueLabel() {
        Component label = this.getLowestValueLabel();
        int width = 0;
        if (label != null) {
            width = label.getPreferredSize().width;
        }
        return width;
    }

    protected int getHeightOfHighValueLabel() {
        Component label = this.getHighestValueLabel();
        int height = 0;
        if (label != null) {
            height = label.getPreferredSize().height;
        }
        return height;
    }

    protected int getHeightOfLowValueLabel() {
        Component label = this.getLowestValueLabel();
        int height = 0;
        if (label != null) {
            height = label.getPreferredSize().height;
        }
        return height;
    }

    protected boolean drawInverted() {
        if (this.slider.getOrientation() == 0) {
            if (BasicGraphicsUtils.isLeftToRight(this.slider)) {
                return this.slider.getInverted();
            }
            return !this.slider.getInverted();
        }
        return this.slider.getInverted();
    }

    protected Integer getHighestValue() {
        Dictionary dictionary = this.slider.getLabelTable();
        if (dictionary == null) {
            return null;
        }
        Enumeration keys = dictionary.keys();
        Integer max = null;
        while (keys.hasMoreElements()) {
            Integer i = (Integer)keys.nextElement();
            if (max != null && i <= max) continue;
            max = i;
        }
        return max;
    }

    protected Integer getLowestValue() {
        Dictionary dictionary = this.slider.getLabelTable();
        if (dictionary == null) {
            return null;
        }
        Enumeration keys = dictionary.keys();
        Integer min = null;
        while (keys.hasMoreElements()) {
            Integer i = (Integer)keys.nextElement();
            if (min != null && i >= min) continue;
            min = i;
        }
        return min;
    }

    protected Component getLowestValueLabel() {
        Integer min = this.getLowestValue();
        if (min != null) {
            return (Component)this.slider.getLabelTable().get(min);
        }
        return null;
    }

    protected Component getHighestValueLabel() {
        Integer max = this.getHighestValue();
        if (max != null) {
            return (Component)this.slider.getLabelTable().get(max);
        }
        return null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        this.recalculateIfInsetsChanged();
        this.recalculateIfOrientationChanged();
        Rectangle clip = g.getClipBounds();
        if (!clip.intersects(this.trackRect) && this.slider.getPaintTrack()) {
            this.calculateGeometry();
        }
        if (this.slider.getPaintTrack() && clip.intersects(this.trackRect)) {
            this.paintTrack(g);
        }
        if (this.slider.getPaintTicks() && clip.intersects(this.tickRect)) {
            this.paintTicks(g);
        }
        if (this.slider.getPaintLabels() && clip.intersects(this.labelRect)) {
            this.paintLabels(g);
        }
        if (this.slider.hasFocus() && clip.intersects(this.focusRect)) {
            this.paintFocus(g);
        }
        if (clip.intersects(this.thumbRect)) {
            this.paintThumb(g);
        }
    }

    protected void recalculateIfInsetsChanged() {
        Insets newInsets = this.slider.getInsets();
        if (!newInsets.equals(this.insetCache)) {
            this.insetCache = newInsets;
            this.calculateGeometry();
        }
    }

    protected void recalculateIfOrientationChanged() {
        boolean ltr = BasicGraphicsUtils.isLeftToRight(this.slider);
        if (ltr != this.leftToRightCache) {
            this.leftToRightCache = ltr;
            this.calculateGeometry();
        }
    }

    public void paintFocus(Graphics g) {
        g.setColor(this.getFocusColor());
        BasicGraphicsUtils.drawDashedRect(g, this.focusRect.x, this.focusRect.y, this.focusRect.width, this.focusRect.height);
    }

    public void paintTrack(Graphics g) {
        Rectangle trackBounds = this.trackRect;
        if (this.slider.getOrientation() == 0) {
            int cy = trackBounds.height / 2 - 2;
            int cw = trackBounds.width;
            g.translate(trackBounds.x, trackBounds.y + cy);
            g.setColor(this.getShadowColor());
            g.drawLine(0, 0, cw - 1, 0);
            g.drawLine(0, 1, 0, 2);
            g.setColor(this.getHighlightColor());
            g.drawLine(0, 3, cw, 3);
            g.drawLine(cw, 0, cw, 3);
            g.setColor(Color.black);
            g.drawLine(1, 1, cw - 2, 1);
            g.translate(-trackBounds.x, -(trackBounds.y + cy));
        } else {
            int cx = trackBounds.width / 2 - 2;
            int ch = trackBounds.height;
            g.translate(trackBounds.x + cx, trackBounds.y);
            g.setColor(this.getShadowColor());
            g.drawLine(0, 0, 0, ch - 1);
            g.drawLine(1, 0, 2, 0);
            g.setColor(this.getHighlightColor());
            g.drawLine(3, 0, 3, ch);
            g.drawLine(0, ch, 3, ch);
            g.setColor(Color.black);
            g.drawLine(1, 1, 1, ch - 2);
            g.translate(-(trackBounds.x + cx), -trackBounds.y);
        }
    }

    public void paintTicks(Graphics g) {
        Rectangle tickBounds = this.tickRect;
        g.setColor(DefaultLookup.getColor(this.slider, this, "Slider.tickColor", Color.black));
        if (this.slider.getOrientation() == 0) {
            int xPos;
            int value;
            g.translate(0, tickBounds.y);
            if (this.slider.getMinorTickSpacing() > 0) {
                for (value = this.slider.getMinimum(); value <= this.slider.getMaximum(); value += this.slider.getMinorTickSpacing()) {
                    xPos = this.xPositionForValue(value);
                    this.paintMinorTickForHorizSlider(g, tickBounds, xPos);
                    if (Integer.MAX_VALUE - this.slider.getMinorTickSpacing() < value) break;
                }
            }
            if (this.slider.getMajorTickSpacing() > 0) {
                for (value = this.slider.getMinimum(); value <= this.slider.getMaximum(); value += this.slider.getMajorTickSpacing()) {
                    xPos = this.xPositionForValue(value);
                    this.paintMajorTickForHorizSlider(g, tickBounds, xPos);
                    if (Integer.MAX_VALUE - this.slider.getMajorTickSpacing() < value) break;
                }
            }
            g.translate(0, -tickBounds.y);
        } else {
            g.translate(tickBounds.x, 0);
            if (this.slider.getMinorTickSpacing() > 0) {
                int offset = 0;
                if (!BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    offset = tickBounds.width - tickBounds.width / 2;
                    g.translate(offset, 0);
                }
                for (int value = this.slider.getMinimum(); value <= this.slider.getMaximum(); value += this.slider.getMinorTickSpacing()) {
                    int yPos = this.yPositionForValue(value);
                    this.paintMinorTickForVertSlider(g, tickBounds, yPos);
                    if (Integer.MAX_VALUE - this.slider.getMinorTickSpacing() < value) break;
                }
                if (!BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    g.translate(-offset, 0);
                }
            }
            if (this.slider.getMajorTickSpacing() > 0) {
                if (!BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    g.translate(2, 0);
                }
                for (int value = this.slider.getMinimum(); value <= this.slider.getMaximum(); value += this.slider.getMajorTickSpacing()) {
                    int yPos = this.yPositionForValue(value);
                    this.paintMajorTickForVertSlider(g, tickBounds, yPos);
                    if (Integer.MAX_VALUE - this.slider.getMajorTickSpacing() < value) break;
                }
                if (!BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    g.translate(-2, 0);
                }
            }
            g.translate(-tickBounds.x, 0);
        }
    }

    protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
        g.drawLine(x, 0, x, tickBounds.height / 2 - 1);
    }

    protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
        g.drawLine(x, 0, x, tickBounds.height - 2);
    }

    protected void paintMinorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
        g.drawLine(0, y, tickBounds.width / 2 - 1, y);
    }

    protected void paintMajorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
        g.drawLine(0, y, tickBounds.width - 2, y);
    }

    public void paintLabels(Graphics g) {
        Rectangle labelBounds = this.labelRect;
        Dictionary dictionary = this.slider.getLabelTable();
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            int minValue = this.slider.getMinimum();
            int maxValue = this.slider.getMaximum();
            boolean enabled = this.slider.isEnabled();
            while (keys.hasMoreElements()) {
                Integer key = (Integer)keys.nextElement();
                int value = key;
                if (value < minValue || value > maxValue) continue;
                JComponent label = (JComponent)dictionary.get(key);
                label.setEnabled(enabled);
                if (label instanceof JLabel) {
                    Icon icon;
                    Icon icon2 = icon = label.isEnabled() ? ((JLabel)label).getIcon() : ((JLabel)label).getDisabledIcon();
                    if (icon instanceof ImageIcon) {
                        Toolkit.getDefaultToolkit().checkImage(((ImageIcon)icon).getImage(), -1, -1, this.slider);
                    }
                }
                if (this.slider.getOrientation() == 0) {
                    g.translate(0, labelBounds.y);
                    this.paintHorizontalLabel(g, value, label);
                    g.translate(0, -labelBounds.y);
                    continue;
                }
                int offset = 0;
                if (!BasicGraphicsUtils.isLeftToRight(this.slider)) {
                    offset = labelBounds.width - label.getPreferredSize().width;
                }
                g.translate(labelBounds.x + offset, 0);
                this.paintVerticalLabel(g, value, label);
                g.translate(-labelBounds.x - offset, 0);
            }
        }
    }

    protected void paintHorizontalLabel(Graphics g, int value, Component label) {
        int labelCenter = this.xPositionForValue(value);
        int labelLeft = labelCenter - label.getPreferredSize().width / 2;
        g.translate(labelLeft, 0);
        label.paint(g);
        g.translate(-labelLeft, 0);
    }

    protected void paintVerticalLabel(Graphics g, int value, Component label) {
        int labelCenter = this.yPositionForValue(value);
        int labelTop = labelCenter - label.getPreferredSize().height / 2;
        g.translate(0, labelTop);
        label.paint(g);
        g.translate(0, -labelTop);
    }

    public void paintThumb(Graphics g) {
        Rectangle knobBounds = this.thumbRect;
        int w = knobBounds.width;
        int h = knobBounds.height;
        g.translate(knobBounds.x, knobBounds.y);
        Rectangle clip = g.getClipBounds();
        g.clipRect(0, 0, w, h);
        if (this.slider.isEnabled()) {
            g.setColor(this.slider.getBackground());
        } else {
            g.setColor(this.slider.getBackground().darker());
        }
        Boolean paintThumbArrowShape = (Boolean)this.slider.getClientProperty("Slider.paintThumbArrowShape");
        if (!this.slider.getPaintTicks() && paintThumbArrowShape == null || paintThumbArrowShape == Boolean.FALSE) {
            g.fillRect(0, 0, w, h);
            g.setColor(Color.black);
            g.drawLine(0, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 0, w - 1, h - 1);
            g.setColor(this.highlightColor);
            g.drawLine(0, 0, 0, h - 2);
            g.drawLine(1, 0, w - 2, 0);
            g.setColor(this.shadowColor);
            g.drawLine(1, h - 2, w - 2, h - 2);
            g.drawLine(w - 2, 1, w - 2, h - 3);
        } else if (this.slider.getOrientation() == 0) {
            int cw = w / 2;
            g.fillRect(1, 1, w - 3, h - 1 - cw);
            Polygon p = new Polygon();
            p.addPoint(1, h - cw);
            p.addPoint(cw - 1, h - 1);
            p.addPoint(w - 2, h - 1 - cw);
            g.fillPolygon(p);
            g.setColor(this.highlightColor);
            g.drawLine(0, 0, w - 2, 0);
            g.drawLine(0, 1, 0, h - 1 - cw);
            g.drawLine(0, h - cw, cw - 1, h - 1);
            g.setColor(Color.black);
            g.drawLine(w - 1, 0, w - 1, h - 2 - cw);
            g.drawLine(w - 1, h - 1 - cw, w - 1 - cw, h - 1);
            g.setColor(this.shadowColor);
            g.drawLine(w - 2, 1, w - 2, h - 2 - cw);
            g.drawLine(w - 2, h - 1 - cw, w - 1 - cw, h - 2);
        } else {
            int cw = h / 2;
            if (BasicGraphicsUtils.isLeftToRight(this.slider)) {
                g.fillRect(1, 1, w - 1 - cw, h - 3);
                Polygon p = new Polygon();
                p.addPoint(w - cw - 1, 0);
                p.addPoint(w - 1, cw);
                p.addPoint(w - 1 - cw, h - 2);
                g.fillPolygon(p);
                g.setColor(this.highlightColor);
                g.drawLine(0, 0, 0, h - 2);
                g.drawLine(1, 0, w - 1 - cw, 0);
                g.drawLine(w - cw - 1, 0, w - 1, cw);
                g.setColor(Color.black);
                g.drawLine(0, h - 1, w - 2 - cw, h - 1);
                g.drawLine(w - 1 - cw, h - 1, w - 1, h - 1 - cw);
                g.setColor(this.shadowColor);
                g.drawLine(1, h - 2, w - 2 - cw, h - 2);
                g.drawLine(w - 1 - cw, h - 2, w - 2, h - cw - 1);
            } else {
                g.fillRect(5, 1, w - 1 - cw, h - 3);
                Polygon p = new Polygon();
                p.addPoint(cw, 0);
                p.addPoint(0, cw);
                p.addPoint(cw, h - 2);
                g.fillPolygon(p);
                g.setColor(this.highlightColor);
                g.drawLine(cw - 1, 0, w - 2, 0);
                g.drawLine(0, cw, cw, 0);
                g.setColor(Color.black);
                g.drawLine(0, h - 1 - cw, cw, h - 1);
                g.drawLine(cw, h - 1, w - 1, h - 1);
                g.setColor(this.shadowColor);
                g.drawLine(cw, h - 2, w - 2, h - 2);
                g.drawLine(w - 1, 1, w - 1, h - 2);
            }
        }
        g.setClip(clip);
        g.translate(-knobBounds.x, -knobBounds.y);
    }

    public void setThumbLocation(int x, int y) {
        unionRect.setBounds(this.thumbRect);
        this.thumbRect.setLocation(x, y);
        SwingUtilities.computeUnion(this.thumbRect.x, this.thumbRect.y, this.thumbRect.width, this.thumbRect.height, unionRect);
        this.slider.repaint(BasicSliderUI.unionRect.x, BasicSliderUI.unionRect.y, BasicSliderUI.unionRect.width, BasicSliderUI.unionRect.height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void scrollByBlock(int direction) {
        JSlider jSlider = this.slider;
        synchronized (jSlider) {
            int blockIncrement = (this.slider.getMaximum() - this.slider.getMinimum()) / 10;
            if (blockIncrement == 0) {
                blockIncrement = 1;
            }
            int tickSpacing = this.getTickSpacing();
            if (this.slider.getSnapToTicks()) {
                if (blockIncrement < tickSpacing) {
                    blockIncrement = tickSpacing;
                }
            } else if (tickSpacing > 0) {
                blockIncrement = tickSpacing;
            }
            int delta = blockIncrement * (direction > 0 ? 1 : -1);
            this.slider.setValue(this.slider.getValue() + delta);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void scrollByUnit(int direction) {
        JSlider jSlider = this.slider;
        synchronized (jSlider) {
            int delta;
            int n = delta = direction > 0 ? 1 : -1;
            if (this.slider.getSnapToTicks()) {
                delta *= this.getTickSpacing();
            }
            this.slider.setValue(this.slider.getValue() + delta);
        }
    }

    protected void scrollDueToClickInTrack(int dir) {
        this.scrollByBlock(dir);
    }

    protected int xPositionForValue(int value) {
        int xPosition;
        int min = this.slider.getMinimum();
        int max = this.slider.getMaximum();
        int trackLength = this.trackRect.width;
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackLength / valueRange;
        int trackLeft = this.trackRect.x;
        int trackRight = this.trackRect.x + (this.trackRect.width - 1);
        if (!this.drawInverted()) {
            xPosition = trackLeft;
            xPosition = (int)((long)xPosition + Math.round(pixelsPerValue * ((double)value - (double)min)));
        } else {
            xPosition = trackRight;
            xPosition = (int)((long)xPosition - Math.round(pixelsPerValue * ((double)value - (double)min)));
        }
        xPosition = Math.max(trackLeft, xPosition);
        xPosition = Math.min(trackRight, xPosition);
        return xPosition;
    }

    protected int yPositionForValue(int value) {
        return this.yPositionForValue(value, this.trackRect.y, this.trackRect.height);
    }

    protected int yPositionForValue(int value, int trackY, int trackHeight) {
        int yPosition;
        int min = this.slider.getMinimum();
        int max = this.slider.getMaximum();
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackHeight / valueRange;
        int trackBottom = trackY + (trackHeight - 1);
        if (!this.drawInverted()) {
            yPosition = trackY;
            yPosition = (int)((long)yPosition + Math.round(pixelsPerValue * ((double)max - (double)value)));
        } else {
            yPosition = trackY;
            yPosition = (int)((long)yPosition + Math.round(pixelsPerValue * ((double)value - (double)min)));
        }
        yPosition = Math.max(trackY, yPosition);
        yPosition = Math.min(trackBottom, yPosition);
        return yPosition;
    }

    public int valueForYPosition(int yPos) {
        int value;
        int minValue = this.slider.getMinimum();
        int maxValue = this.slider.getMaximum();
        int trackLength = this.trackRect.height;
        int trackTop = this.trackRect.y;
        int trackBottom = this.trackRect.y + (this.trackRect.height - 1);
        if (yPos <= trackTop) {
            value = this.drawInverted() ? minValue : maxValue;
        } else if (yPos >= trackBottom) {
            value = this.drawInverted() ? maxValue : minValue;
        } else {
            int distanceFromTrackTop = yPos - trackTop;
            double valueRange = (double)maxValue - (double)minValue;
            double valuePerPixel = valueRange / (double)trackLength;
            int valueFromTrackTop = (int)Math.round((double)distanceFromTrackTop * valuePerPixel);
            value = this.drawInverted() ? minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
        }
        return value;
    }

    public int valueForXPosition(int xPos) {
        int value;
        int minValue = this.slider.getMinimum();
        int maxValue = this.slider.getMaximum();
        int trackLength = this.trackRect.width;
        int trackLeft = this.trackRect.x;
        int trackRight = this.trackRect.x + (this.trackRect.width - 1);
        if (xPos <= trackLeft) {
            value = this.drawInverted() ? maxValue : minValue;
        } else if (xPos >= trackRight) {
            value = this.drawInverted() ? minValue : maxValue;
        } else {
            int distanceFromTrackLeft = xPos - trackLeft;
            double valueRange = (double)maxValue - (double)minValue;
            double valuePerPixel = valueRange / (double)trackLength;
            int valueFromTrackLeft = (int)Math.round((double)distanceFromTrackLeft * valuePerPixel);
            value = this.drawInverted() ? maxValue - valueFromTrackLeft : minValue + valueFromTrackLeft;
        }
        return value;
    }

    public class TrackListener
    extends MouseInputAdapter {
        protected transient int offset;
        protected transient int currentMouseX;
        protected transient int currentMouseY;

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!BasicSliderUI.this.slider.isEnabled()) {
                return;
            }
            this.offset = 0;
            BasicSliderUI.this.scrollTimer.stop();
            BasicSliderUI.this.isDragging = false;
            BasicSliderUI.this.slider.setValueIsAdjusting(false);
            BasicSliderUI.this.slider.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!BasicSliderUI.this.slider.isEnabled()) {
                return;
            }
            BasicSliderUI.this.calculateGeometry();
            this.currentMouseX = e.getX();
            this.currentMouseY = e.getY();
            if (BasicSliderUI.this.slider.isRequestFocusEnabled()) {
                BasicSliderUI.this.slider.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
            if (BasicSliderUI.this.thumbRect.contains(this.currentMouseX, this.currentMouseY)) {
                if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                switch (BasicSliderUI.this.slider.getOrientation()) {
                    case 1: {
                        this.offset = this.currentMouseY - BasicSliderUI.this.thumbRect.y;
                        break;
                    }
                    case 0: {
                        this.offset = this.currentMouseX - BasicSliderUI.this.thumbRect.x;
                    }
                }
                BasicSliderUI.this.isDragging = true;
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            BasicSliderUI.this.isDragging = false;
            BasicSliderUI.this.slider.setValueIsAdjusting(true);
            Dimension sbSize = BasicSliderUI.this.slider.getSize();
            int direction = 1;
            switch (BasicSliderUI.this.slider.getOrientation()) {
                case 1: {
                    if (BasicSliderUI.this.thumbRect.isEmpty()) {
                        int scrollbarCenter = sbSize.height / 2;
                        if (!BasicSliderUI.this.drawInverted()) {
                            direction = this.currentMouseY < scrollbarCenter ? 1 : -1;
                            break;
                        }
                        direction = this.currentMouseY < scrollbarCenter ? -1 : 1;
                        break;
                    }
                    int thumbY = BasicSliderUI.this.thumbRect.y;
                    if (!BasicSliderUI.this.drawInverted()) {
                        direction = this.currentMouseY < thumbY ? 1 : -1;
                        break;
                    }
                    direction = this.currentMouseY < thumbY ? -1 : 1;
                    break;
                }
                case 0: {
                    if (BasicSliderUI.this.thumbRect.isEmpty()) {
                        int scrollbarCenter = sbSize.width / 2;
                        if (!BasicSliderUI.this.drawInverted()) {
                            direction = this.currentMouseX < scrollbarCenter ? -1 : 1;
                            break;
                        }
                        direction = this.currentMouseX < scrollbarCenter ? 1 : -1;
                        break;
                    }
                    int thumbX = BasicSliderUI.this.thumbRect.x;
                    if (!BasicSliderUI.this.drawInverted()) {
                        direction = this.currentMouseX < thumbX ? -1 : 1;
                        break;
                    }
                    int n = direction = this.currentMouseX < thumbX ? 1 : -1;
                }
            }
            if (this.shouldScroll(direction)) {
                BasicSliderUI.this.scrollDueToClickInTrack(direction);
            }
            if (this.shouldScroll(direction)) {
                BasicSliderUI.this.scrollTimer.stop();
                BasicSliderUI.this.scrollListener.setDirection(direction);
                BasicSliderUI.this.scrollTimer.start();
            }
        }

        public boolean shouldScroll(int direction) {
            Rectangle r = BasicSliderUI.this.thumbRect;
            if (BasicSliderUI.this.slider.getOrientation() == 1 ? ((BasicSliderUI.this.drawInverted() ? direction < 0 : direction > 0) ? r.y <= this.currentMouseY : r.y + r.height >= this.currentMouseY) : ((BasicSliderUI.this.drawInverted() ? direction < 0 : direction > 0) ? r.x + r.width >= this.currentMouseX : r.x <= this.currentMouseX)) {
                return false;
            }
            if (direction > 0 && BasicSliderUI.this.slider.getValue() + BasicSliderUI.this.slider.getExtent() >= BasicSliderUI.this.slider.getMaximum()) {
                return false;
            }
            return direction >= 0 || BasicSliderUI.this.slider.getValue() > BasicSliderUI.this.slider.getMinimum();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!BasicSliderUI.this.slider.isEnabled()) {
                return;
            }
            this.currentMouseX = e.getX();
            this.currentMouseY = e.getY();
            if (!BasicSliderUI.this.isDragging) {
                return;
            }
            BasicSliderUI.this.slider.setValueIsAdjusting(true);
            switch (BasicSliderUI.this.slider.getOrientation()) {
                case 1: {
                    int halfThumbHeight = BasicSliderUI.this.thumbRect.height / 2;
                    int thumbTop = e.getY() - this.offset;
                    int trackTop = BasicSliderUI.this.trackRect.y;
                    int trackBottom = BasicSliderUI.this.trackRect.y + (BasicSliderUI.this.trackRect.height - 1);
                    int vMax = BasicSliderUI.this.yPositionForValue(BasicSliderUI.this.slider.getMaximum() - BasicSliderUI.this.slider.getExtent());
                    if (BasicSliderUI.this.drawInverted()) {
                        trackBottom = vMax;
                    } else {
                        trackTop = vMax;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);
                    BasicSliderUI.this.setThumbLocation(BasicSliderUI.this.thumbRect.x, thumbTop);
                    int thumbMiddle = thumbTop + halfThumbHeight;
                    BasicSliderUI.this.slider.setValue(BasicSliderUI.this.valueForYPosition(thumbMiddle));
                    break;
                }
                case 0: {
                    int halfThumbWidth = BasicSliderUI.this.thumbRect.width / 2;
                    int thumbLeft = e.getX() - this.offset;
                    int trackLeft = BasicSliderUI.this.trackRect.x;
                    int trackRight = BasicSliderUI.this.trackRect.x + (BasicSliderUI.this.trackRect.width - 1);
                    int hMax = BasicSliderUI.this.xPositionForValue(BasicSliderUI.this.slider.getMaximum() - BasicSliderUI.this.slider.getExtent());
                    if (BasicSliderUI.this.drawInverted()) {
                        trackLeft = hMax;
                    } else {
                        trackRight = hMax;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
                    BasicSliderUI.this.setThumbLocation(thumbLeft, BasicSliderUI.this.thumbRect.y);
                    int thumbMiddle = thumbLeft + halfThumbWidth;
                    BasicSliderUI.this.slider.setValue(BasicSliderUI.this.valueForXPosition(thumbMiddle));
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    public class ScrollListener
    implements ActionListener {
        int direction = 1;
        boolean useBlockIncrement;

        public ScrollListener() {
            this.direction = 1;
            this.useBlockIncrement = true;
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
                BasicSliderUI.this.scrollByBlock(this.direction);
            } else {
                BasicSliderUI.this.scrollByUnit(this.direction);
            }
            if (!BasicSliderUI.this.trackListener.shouldScroll(this.direction)) {
                ((Timer)e.getSource()).stop();
            }
        }
    }

    private class Handler
    implements ChangeListener,
    ComponentListener,
    FocusListener,
    PropertyChangeListener {
        private Handler() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!BasicSliderUI.this.isDragging) {
                BasicSliderUI.this.calculateThumbLocation();
                BasicSliderUI.this.slider.repaint();
            }
            BasicSliderUI.this.lastValue = BasicSliderUI.this.slider.getValue();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            BasicSliderUI.this.calculateGeometry();
            BasicSliderUI.this.slider.repaint();
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void focusGained(FocusEvent e) {
            BasicSliderUI.this.slider.repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicSliderUI.this.slider.repaint();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "orientation" || propertyName == "inverted" || propertyName == "labelTable" || propertyName == "majorTickSpacing" || propertyName == "minorTickSpacing" || propertyName == "paintTicks" || propertyName == "paintTrack" || propertyName == "font" || SwingUtilities2.isScaleChanged(e) || propertyName == "paintLabels" || propertyName == "Slider.paintThumbArrowShape") {
                BasicSliderUI.this.checkedLabelBaselines = false;
                BasicSliderUI.this.calculateGeometry();
                BasicSliderUI.this.slider.repaint();
            } else if (propertyName == "componentOrientation") {
                BasicSliderUI.this.calculateGeometry();
                BasicSliderUI.this.slider.repaint();
                InputMap km = BasicSliderUI.this.getInputMap(0, BasicSliderUI.this.slider);
                SwingUtilities.replaceUIInputMap(BasicSliderUI.this.slider, 0, km);
            } else if (propertyName == "model") {
                ((BoundedRangeModel)e.getOldValue()).removeChangeListener(BasicSliderUI.this.changeListener);
                ((BoundedRangeModel)e.getNewValue()).addChangeListener(BasicSliderUI.this.changeListener);
                BasicSliderUI.this.calculateThumbLocation();
                BasicSliderUI.this.slider.repaint();
            }
        }
    }

    private static class Actions
    extends UIAction {
        public static final String POSITIVE_UNIT_INCREMENT = "positiveUnitIncrement";
        public static final String POSITIVE_BLOCK_INCREMENT = "positiveBlockIncrement";
        public static final String NEGATIVE_UNIT_INCREMENT = "negativeUnitIncrement";
        public static final String NEGATIVE_BLOCK_INCREMENT = "negativeBlockIncrement";
        public static final String MIN_SCROLL_INCREMENT = "minScroll";
        public static final String MAX_SCROLL_INCREMENT = "maxScroll";

        Actions() {
            super(null);
        }

        public Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            JSlider slider = (JSlider)evt.getSource();
            BasicSliderUI ui = (BasicSliderUI)BasicLookAndFeel.getUIOfType(slider.getUI(), BasicSliderUI.class);
            String name = this.getName();
            if (ui == null) {
                return;
            }
            if (POSITIVE_UNIT_INCREMENT == name) {
                this.scroll(slider, ui, 1, false);
            } else if (NEGATIVE_UNIT_INCREMENT == name) {
                this.scroll(slider, ui, -1, false);
            } else if (POSITIVE_BLOCK_INCREMENT == name) {
                this.scroll(slider, ui, 1, true);
            } else if (NEGATIVE_BLOCK_INCREMENT == name) {
                this.scroll(slider, ui, -1, true);
            } else if (MIN_SCROLL_INCREMENT == name) {
                this.scroll(slider, ui, -2, false);
            } else if (MAX_SCROLL_INCREMENT == name) {
                this.scroll(slider, ui, 2, false);
            }
        }

        private void scroll(JSlider slider, BasicSliderUI ui, int direction, boolean isBlock) {
            boolean invert = slider.getInverted();
            if (direction == -1 || direction == 1) {
                if (invert) {
                    int n = direction = direction == 1 ? -1 : 1;
                }
                if (isBlock) {
                    ui.scrollByBlock(direction);
                } else {
                    ui.scrollByUnit(direction);
                }
            } else {
                if (invert) {
                    direction = direction == -2 ? 2 : -2;
                }
                slider.setValue(direction == -2 ? slider.getMinimum() : slider.getMaximum());
            }
        }
    }

    static class SharedActionScroller
    extends AbstractAction {
        int dir;
        boolean block;

        public SharedActionScroller(int dir, boolean block) {
            this.dir = dir;
            this.block = block;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            JSlider slider = (JSlider)evt.getSource();
            BasicSliderUI ui = (BasicSliderUI)BasicLookAndFeel.getUIOfType(slider.getUI(), BasicSliderUI.class);
            if (ui == null) {
                return;
            }
            SHARED_ACTION.scroll(slider, ui, this.dir, this.block);
        }
    }

    public class ActionScroller
    extends AbstractAction {
        int dir;
        boolean block;
        JSlider slider;

        public ActionScroller(JSlider slider, int dir, boolean block) {
            this.dir = dir;
            this.block = block;
            this.slider = slider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SHARED_ACTION.scroll(this.slider, BasicSliderUI.this, this.dir, this.block);
        }

        @Override
        public boolean isEnabled() {
            boolean b = true;
            if (this.slider != null) {
                b = this.slider.isEnabled();
            }
            return b;
        }
    }

    public class FocusHandler
    implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            BasicSliderUI.this.getHandler().focusGained(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicSliderUI.this.getHandler().focusLost(e);
        }
    }

    public class ComponentHandler
    extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            BasicSliderUI.this.getHandler().componentResized(e);
        }
    }

    public class ChangeHandler
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicSliderUI.this.getHandler().stateChanged(e);
        }
    }

    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicSliderUI.this.getHandler().propertyChange(e);
        }
    }
}

