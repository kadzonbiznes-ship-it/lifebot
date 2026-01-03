/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;

public class BasicProgressBarUI
extends ProgressBarUI {
    private int cachedPercent;
    private int cellLength;
    private int cellSpacing;
    private Color selectionForeground;
    private Color selectionBackground;
    private Animator animator;
    protected JProgressBar progressBar;
    protected ChangeListener changeListener;
    private Handler handler;
    private int animationIndex = 0;
    private int numFrames;
    private int repaintInterval;
    private int cycleTime;
    private static boolean ADJUSTTIMER = true;
    protected Rectangle boxRect;
    private Rectangle nextPaintRect;
    private Rectangle componentInnards;
    private Rectangle oldComponentInnards;
    private double delta = 0.0;
    private int maxPosition = 0;

    public static ComponentUI createUI(JComponent x) {
        return new BasicProgressBarUI();
    }

    @Override
    public void installUI(JComponent c) {
        this.progressBar = (JProgressBar)c;
        this.installDefaults();
        this.installListeners();
        if (this.progressBar.isIndeterminate()) {
            this.initIndeterminateValues();
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (this.progressBar.isIndeterminate()) {
            this.cleanUpIndeterminateValues();
        }
        this.uninstallDefaults();
        this.uninstallListeners();
        this.progressBar = null;
    }

    protected void installDefaults() {
        LookAndFeel.installProperty(this.progressBar, "opaque", Boolean.TRUE);
        LookAndFeel.installBorder(this.progressBar, "ProgressBar.border");
        LookAndFeel.installColorsAndFont(this.progressBar, "ProgressBar.background", "ProgressBar.foreground", "ProgressBar.font");
        this.cellLength = UIManager.getInt("ProgressBar.cellLength");
        if (this.cellLength == 0) {
            this.cellLength = 1;
        }
        this.cellSpacing = UIManager.getInt("ProgressBar.cellSpacing");
        this.selectionForeground = UIManager.getColor("ProgressBar.selectionForeground");
        this.selectionBackground = UIManager.getColor("ProgressBar.selectionBackground");
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(this.progressBar);
    }

    protected void installListeners() {
        this.changeListener = this.getHandler();
        this.progressBar.addChangeListener(this.changeListener);
        this.progressBar.addPropertyChangeListener(this.getHandler());
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void startAnimationTimer() {
        if (this.animator == null) {
            this.animator = new Animator();
        }
        this.animator.start(this.getRepaintInterval());
    }

    protected void stopAnimationTimer() {
        if (this.animator != null) {
            this.animator.stop();
        }
    }

    protected void uninstallListeners() {
        this.progressBar.removeChangeListener(this.changeListener);
        this.progressBar.removePropertyChangeListener(this.getHandler());
        this.handler = null;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        if (this.progressBar.isStringPainted() && this.progressBar.getOrientation() == 0) {
            FontMetrics metrics = this.progressBar.getFontMetrics(this.progressBar.getFont());
            Insets insets = this.progressBar.getInsets();
            int y = insets.top;
            height = height - insets.top - insets.bottom;
            return y + (height + metrics.getAscent() - metrics.getLeading() - metrics.getDescent()) / 2;
        }
        return -1;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        if (this.progressBar.isStringPainted() && this.progressBar.getOrientation() == 0) {
            return Component.BaselineResizeBehavior.CENTER_OFFSET;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    protected Dimension getPreferredInnerHorizontal() {
        Dimension horizDim = (Dimension)DefaultLookup.get(this.progressBar, this, "ProgressBar.horizontalSize");
        if (horizDim == null) {
            horizDim = new Dimension(146, 12);
        }
        return horizDim;
    }

    protected Dimension getPreferredInnerVertical() {
        Dimension vertDim = (Dimension)DefaultLookup.get(this.progressBar, this, "ProgressBar.verticalSize");
        if (vertDim == null) {
            vertDim = new Dimension(12, 146);
        }
        return vertDim;
    }

    protected Color getSelectionForeground() {
        return this.selectionForeground;
    }

    protected Color getSelectionBackground() {
        return this.selectionBackground;
    }

    private int getCachedPercent() {
        return this.cachedPercent;
    }

    private void setCachedPercent(int cachedPercent) {
        this.cachedPercent = cachedPercent;
    }

    protected int getCellLength() {
        if (this.progressBar.isStringPainted()) {
            return 1;
        }
        return this.cellLength;
    }

    protected void setCellLength(int cellLen) {
        this.cellLength = cellLen;
    }

    protected int getCellSpacing() {
        if (this.progressBar.isStringPainted()) {
            return 0;
        }
        return this.cellSpacing;
    }

    protected void setCellSpacing(int cellSpace) {
        this.cellSpacing = cellSpace;
    }

    protected int getAmountFull(Insets b, int width, int height) {
        int amountFull = 0;
        BoundedRangeModel model = this.progressBar.getModel();
        if (model.getMaximum() - model.getMinimum() != 0) {
            amountFull = this.progressBar.getOrientation() == 0 ? (int)Math.round((double)width * this.progressBar.getPercentComplete()) : (int)Math.round((double)height * this.progressBar.getPercentComplete());
        }
        return amountFull;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (this.progressBar.isIndeterminate()) {
            this.paintIndeterminate(g, c);
        } else {
            this.paintDeterminate(g, c);
        }
    }

    protected Rectangle getBox(Rectangle r) {
        int currentFrame = this.getAnimationIndex();
        int middleFrame = this.numFrames / 2;
        if (this.sizeChanged() || this.delta == 0.0 || (double)this.maxPosition == 0.0) {
            this.updateSizes();
        }
        if ((r = this.getGenericBox(r)) == null) {
            return null;
        }
        if (middleFrame <= 0) {
            return null;
        }
        if (this.progressBar.getOrientation() == 0) {
            r.x = currentFrame < middleFrame ? this.componentInnards.x + (int)Math.round(this.delta * (double)currentFrame) : this.maxPosition - (int)Math.round(this.delta * (double)(currentFrame - middleFrame));
        } else {
            r.y = currentFrame < middleFrame ? this.componentInnards.y + (int)Math.round(this.delta * (double)currentFrame) : this.maxPosition - (int)Math.round(this.delta * (double)(currentFrame - middleFrame));
        }
        return r;
    }

    private void updateSizes() {
        int length = 0;
        if (this.progressBar.getOrientation() == 0) {
            length = this.getBoxLength(this.componentInnards.width, this.componentInnards.height);
            this.maxPosition = this.componentInnards.x + this.componentInnards.width - length;
        } else {
            length = this.getBoxLength(this.componentInnards.height, this.componentInnards.width);
            this.maxPosition = this.componentInnards.y + this.componentInnards.height - length;
        }
        this.delta = 2.0 * (double)this.maxPosition / (double)this.numFrames;
    }

    private Rectangle getGenericBox(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        if (this.progressBar.getOrientation() == 0) {
            r.width = this.getBoxLength(this.componentInnards.width, this.componentInnards.height);
            if (r.width < 0) {
                r = null;
            } else {
                r.height = this.componentInnards.height;
                r.y = this.componentInnards.y;
            }
        } else {
            r.height = this.getBoxLength(this.componentInnards.height, this.componentInnards.width);
            if (r.height < 0) {
                r = null;
            } else {
                r.width = this.componentInnards.width;
                r.x = this.componentInnards.x;
            }
        }
        return r;
    }

    protected int getBoxLength(int availableLength, int otherDimension) {
        return (int)Math.round((double)availableLength / 6.0);
    }

    protected void paintIndeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Insets b = this.progressBar.getInsets();
        int barRectWidth = this.progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = this.progressBar.getHeight() - (b.top + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        this.boxRect = this.getBox(this.boxRect);
        if (this.boxRect != null) {
            g2.setColor(this.progressBar.getForeground());
            g2.fillRect(this.boxRect.x, this.boxRect.y, this.boxRect.width, this.boxRect.height);
        }
        if (this.progressBar.isStringPainted()) {
            if (this.progressBar.getOrientation() == 0) {
                this.paintString(g2, b.left, b.top, barRectWidth, barRectHeight, this.boxRect.x, this.boxRect.width, b);
            } else {
                this.paintString(g2, b.left, b.top, barRectWidth, barRectHeight, this.boxRect.y, this.boxRect.height, b);
            }
        }
    }

    protected void paintDeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Insets b = this.progressBar.getInsets();
        int barRectWidth = this.progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = this.progressBar.getHeight() - (b.top + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        int cellLength = this.getCellLength();
        int cellSpacing = this.getCellSpacing();
        int amountFull = this.getAmountFull(b, barRectWidth, barRectHeight);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(this.progressBar.getForeground());
        if (this.progressBar.getOrientation() == 0) {
            if (cellSpacing == 0 && amountFull > 0) {
                g2.setStroke(new BasicStroke(barRectHeight, 0, 2));
            } else {
                g2.setStroke(new BasicStroke(barRectHeight, 0, 2, 0.0f, new float[]{cellLength, cellSpacing}, 0.0f));
            }
            if (BasicGraphicsUtils.isLeftToRight(c)) {
                g2.drawLine(b.left, barRectHeight / 2 + b.top, amountFull + b.left, barRectHeight / 2 + b.top);
            } else {
                g2.drawLine(barRectWidth + b.left, barRectHeight / 2 + b.top, barRectWidth + b.left - amountFull, barRectHeight / 2 + b.top);
            }
        } else {
            if (cellSpacing == 0 && amountFull > 0) {
                g2.setStroke(new BasicStroke(barRectWidth, 0, 2));
            } else {
                g2.setStroke(new BasicStroke(barRectWidth, 0, 2, 0.0f, new float[]{cellLength, cellSpacing}, 0.0f));
            }
            g2.drawLine(barRectWidth / 2 + b.left, b.top + barRectHeight, barRectWidth / 2 + b.left, b.top + barRectHeight - amountFull);
        }
        if (this.progressBar.isStringPainted()) {
            this.paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
        }
    }

    protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
        if (this.progressBar.getOrientation() == 0) {
            if (BasicGraphicsUtils.isLeftToRight(this.progressBar)) {
                if (this.progressBar.isIndeterminate()) {
                    this.boxRect = this.getBox(this.boxRect);
                    this.paintString(g, x, y, width, height, this.boxRect.x, this.boxRect.width, b);
                } else {
                    this.paintString(g, x, y, width, height, x, amountFull, b);
                }
            } else {
                this.paintString(g, x, y, width, height, x + width - amountFull, amountFull, b);
            }
        } else if (this.progressBar.isIndeterminate()) {
            this.boxRect = this.getBox(this.boxRect);
            this.paintString(g, x, y, width, height, this.boxRect.y, this.boxRect.height, b);
        } else {
            this.paintString(g, x, y, width, height, y + height - amountFull, amountFull, b);
        }
    }

    private void paintString(Graphics g, int x, int y, int width, int height, int fillStart, int amountFull, Insets b) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        String progressString = this.progressBar.getString();
        g2.setFont(this.progressBar.getFont());
        Point renderLocation = this.getStringPlacement(g2, progressString, x, y, width, height);
        Rectangle oldClip = g2.getClipBounds();
        if (this.progressBar.getOrientation() == 0) {
            g2.setColor(this.getSelectionBackground());
            SwingUtilities2.drawString((JComponent)this.progressBar, (Graphics)g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(this.getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, height);
            SwingUtilities2.drawString((JComponent)this.progressBar, (Graphics)g2, progressString, renderLocation.x, renderLocation.y);
        } else {
            g2.setColor(this.getSelectionBackground());
            AffineTransform rotate = AffineTransform.getRotateInstance(1.5707963267948966);
            g2.setFont(this.progressBar.getFont().deriveFont(rotate));
            renderLocation = this.getStringPlacement(g2, progressString, x, y, width, height);
            SwingUtilities2.drawString((JComponent)this.progressBar, (Graphics)g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(this.getSelectionForeground());
            g2.clipRect(x, fillStart, width, amountFull);
            SwingUtilities2.drawString((JComponent)this.progressBar, (Graphics)g2, progressString, renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    protected Point getStringPlacement(Graphics g, String progressString, int x, int y, int width, int height) {
        FontMetrics fontSizer = SwingUtilities2.getFontMetrics(this.progressBar, g, this.progressBar.getFont());
        int stringWidth = SwingUtilities2.stringWidth(this.progressBar, fontSizer, progressString);
        if (this.progressBar.getOrientation() == 0) {
            return new Point(x + (int)Math.round((double)width / 2.0 - (double)stringWidth / 2.0), y + (height + fontSizer.getAscent() - fontSizer.getLeading() - fontSizer.getDescent()) / 2);
        }
        return new Point(x + (width - fontSizer.getAscent() + fontSizer.getLeading() + fontSizer.getDescent()) / 2, y + (int)Math.round((double)height / 2.0 - (double)stringWidth / 2.0));
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size;
        Insets border = this.progressBar.getInsets();
        FontMetrics fontSizer = this.progressBar.getFontMetrics(this.progressBar.getFont());
        if (this.progressBar.getOrientation() == 0) {
            size = new Dimension(this.getPreferredInnerHorizontal());
            if (this.progressBar.isStringPainted()) {
                int stringHeight;
                String progString = this.progressBar.getString();
                int stringWidth = SwingUtilities2.stringWidth(this.progressBar, fontSizer, progString);
                if (stringWidth > size.width) {
                    size.width = stringWidth;
                }
                if ((stringHeight = fontSizer.getHeight() + fontSizer.getDescent()) > size.height) {
                    size.height = stringHeight;
                }
            }
        } else {
            size = new Dimension(this.getPreferredInnerVertical());
            if (this.progressBar.isStringPainted()) {
                int stringWidth;
                String progString = this.progressBar.getString();
                int stringHeight = fontSizer.getHeight() + fontSizer.getDescent();
                if (stringHeight > size.width) {
                    size.width = stringHeight;
                }
                if ((stringWidth = SwingUtilities2.stringWidth(this.progressBar, fontSizer, progString)) > size.height) {
                    size.height = stringWidth;
                }
            }
        }
        size.width += border.left + border.right;
        size.height += border.top + border.bottom;
        return size;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension pref = this.getPreferredSize(this.progressBar);
        if (this.progressBar.getOrientation() == 0) {
            pref.width = 10;
        } else {
            pref.height = 10;
        }
        return pref;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension pref = this.getPreferredSize(this.progressBar);
        if (this.progressBar.getOrientation() == 0) {
            pref.width = Short.MAX_VALUE;
        } else {
            pref.height = Short.MAX_VALUE;
        }
        return pref;
    }

    protected int getAnimationIndex() {
        return this.animationIndex;
    }

    protected final int getFrameCount() {
        return this.numFrames;
    }

    protected void setAnimationIndex(int newValue) {
        if (this.animationIndex != newValue) {
            if (this.sizeChanged()) {
                this.animationIndex = newValue;
                this.maxPosition = 0;
                this.delta = 0.0;
                this.progressBar.repaint();
                return;
            }
            this.nextPaintRect = this.getBox(this.nextPaintRect);
            this.animationIndex = newValue;
            if (this.nextPaintRect != null) {
                this.boxRect = this.getBox(this.boxRect);
                if (this.boxRect != null) {
                    this.nextPaintRect.add(this.boxRect);
                }
            }
        } else {
            return;
        }
        if (this.nextPaintRect != null) {
            this.progressBar.repaint(this.nextPaintRect);
        } else {
            this.progressBar.repaint();
        }
    }

    private boolean sizeChanged() {
        if (this.oldComponentInnards == null || this.componentInnards == null) {
            return true;
        }
        this.oldComponentInnards.setRect(this.componentInnards);
        this.componentInnards = SwingUtilities.calculateInnerArea(this.progressBar, this.componentInnards);
        return !this.oldComponentInnards.equals(this.componentInnards);
    }

    protected void incrementAnimationIndex() {
        int newValue = this.getAnimationIndex() + 1;
        if (newValue < this.numFrames) {
            this.setAnimationIndex(newValue);
        } else {
            this.setAnimationIndex(0);
        }
    }

    private int getRepaintInterval() {
        return this.repaintInterval;
    }

    private int initRepaintInterval() {
        this.repaintInterval = DefaultLookup.getInt(this.progressBar, this, "ProgressBar.repaintInterval", 50);
        return this.repaintInterval;
    }

    private int getCycleTime() {
        return this.cycleTime;
    }

    private int initCycleTime() {
        this.cycleTime = DefaultLookup.getInt(this.progressBar, this, "ProgressBar.cycleTime", 3000);
        return this.cycleTime;
    }

    private void initIndeterminateDefaults() {
        this.initRepaintInterval();
        this.initCycleTime();
        if (this.repaintInterval <= 0) {
            this.repaintInterval = 100;
        }
        if (this.repaintInterval > this.cycleTime) {
            this.cycleTime = this.repaintInterval * 20;
        } else {
            int factor = (int)Math.ceil((double)this.cycleTime / ((double)this.repaintInterval * 2.0));
            this.cycleTime = this.repaintInterval * factor * 2;
        }
    }

    private void initIndeterminateValues() {
        this.initIndeterminateDefaults();
        this.numFrames = this.cycleTime / this.repaintInterval;
        this.initAnimationIndex();
        this.boxRect = new Rectangle();
        this.nextPaintRect = new Rectangle();
        this.componentInnards = new Rectangle();
        this.oldComponentInnards = new Rectangle();
        this.progressBar.addHierarchyListener(this.getHandler());
        if (this.progressBar.isDisplayable()) {
            this.startAnimationTimer();
        }
    }

    private void cleanUpIndeterminateValues() {
        if (this.progressBar.isDisplayable()) {
            this.stopAnimationTimer();
        }
        this.repaintInterval = 0;
        this.cycleTime = 0;
        this.animationIndex = 0;
        this.numFrames = 0;
        this.maxPosition = 0;
        this.delta = 0.0;
        this.nextPaintRect = null;
        this.boxRect = null;
        this.oldComponentInnards = null;
        this.componentInnards = null;
        this.progressBar.removeHierarchyListener(this.getHandler());
    }

    private void initAnimationIndex() {
        if (this.progressBar.getOrientation() == 0 && BasicGraphicsUtils.isLeftToRight(this.progressBar)) {
            this.setAnimationIndex(0);
        } else {
            this.setAnimationIndex(this.numFrames / 2);
        }
    }

    private class Handler
    implements ChangeListener,
    PropertyChangeListener,
    HierarchyListener {
        private Handler() {
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            int oldPercent;
            BoundedRangeModel model = BasicProgressBarUI.this.progressBar.getModel();
            int newRange = model.getMaximum() - model.getMinimum();
            int newPercent = newRange > 0 ? (int)(100L * (long)model.getValue() / (long)newRange) : 0;
            if (newPercent != (oldPercent = BasicProgressBarUI.this.getCachedPercent())) {
                BasicProgressBarUI.this.setCachedPercent(newPercent);
                BasicProgressBarUI.this.progressBar.repaint();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if ("indeterminate" == prop) {
                if (BasicProgressBarUI.this.progressBar.isIndeterminate()) {
                    BasicProgressBarUI.this.initIndeterminateValues();
                } else {
                    BasicProgressBarUI.this.cleanUpIndeterminateValues();
                }
                BasicProgressBarUI.this.progressBar.repaint();
            }
        }

        @Override
        public void hierarchyChanged(HierarchyEvent he) {
            if ((he.getChangeFlags() & 2L) != 0L && BasicProgressBarUI.this.progressBar.isIndeterminate()) {
                if (BasicProgressBarUI.this.progressBar.isDisplayable()) {
                    BasicProgressBarUI.this.startAnimationTimer();
                } else {
                    BasicProgressBarUI.this.stopAnimationTimer();
                }
            }
        }
    }

    private class Animator
    implements ActionListener {
        private Timer timer;
        private long previousDelay;
        private int interval;
        private long lastCall;
        private int MINIMUM_DELAY = 5;

        private Animator() {
        }

        private void start(int interval) {
            this.previousDelay = interval;
            this.lastCall = 0L;
            if (this.timer == null) {
                this.timer = new Timer(interval, this);
            } else {
                this.timer.setDelay(interval);
            }
            if (ADJUSTTIMER) {
                this.timer.setRepeats(false);
                this.timer.setCoalesce(false);
            }
            this.timer.start();
        }

        private void stop() {
            this.timer.stop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ADJUSTTIMER) {
                long time = System.currentTimeMillis();
                if (this.lastCall > 0L) {
                    int nextDelay = (int)(this.previousDelay - time + this.lastCall + (long)BasicProgressBarUI.this.getRepaintInterval());
                    if (nextDelay < this.MINIMUM_DELAY) {
                        nextDelay = this.MINIMUM_DELAY;
                    }
                    this.timer.setInitialDelay(nextDelay);
                    this.previousDelay = nextDelay;
                }
                this.timer.start();
                this.lastCall = time;
            }
            BasicProgressBarUI.this.incrementAnimationIndex();
        }
    }

    public class ChangeHandler
    implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            BasicProgressBarUI.this.getHandler().stateChanged(e);
        }
    }
}

