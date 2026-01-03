/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.ImageObserver;
import java.beans.BeanProperty;
import java.beans.Transient;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.ViewportLayout;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ViewportUI;
import sun.awt.AWTAccessor;
import sun.swing.SwingUtilities2;

public class JViewport
extends JComponent
implements Accessible {
    private static final String uiClassID = "ViewportUI";
    static final Object EnableWindowBlit = "EnableWindowBlit";
    protected boolean isViewSizeSet = false;
    protected Point lastPaintPosition = null;
    @Deprecated
    protected boolean backingStore = false;
    protected transient Image backingStoreImage = null;
    protected boolean scrollUnderway = false;
    private ComponentListener viewListener = null;
    private transient ChangeEvent changeEvent = null;
    public static final int BLIT_SCROLL_MODE = 1;
    public static final int BACKINGSTORE_SCROLL_MODE = 2;
    public static final int SIMPLE_SCROLL_MODE = 0;
    private int scrollMode = 1;
    private transient boolean repaintAll;
    private transient boolean waitingForRepaint;
    private transient Timer repaintTimer;
    private transient boolean inBlitPaint;
    private boolean hasHadValidView;
    private boolean viewChanged;

    public JViewport() {
        this.setLayout(this.createLayoutManager());
        this.updateUI();
        this.setInheritsPopupMenu(true);
    }

    @Override
    public ViewportUI getUI() {
        return (ViewportUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(ViewportUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((ViewportUI)UIManager.getUI(this));
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    protected void addImpl(Component child, Object constraints, int index) {
        this.setView(child);
    }

    @Override
    public void remove(Component child) {
        child.removeComponentListener(this.viewListener);
        super.remove(child);
    }

    @Override
    public void scrollRectToVisible(Rectangle contentRect) {
        Component view = this.getView();
        if (view == null) {
            return;
        }
        if (!view.isValid()) {
            this.validateView();
        }
        int dx = this.positionAdjustment(this.getWidth(), contentRect.width, contentRect.x);
        int dy = this.positionAdjustment(this.getHeight(), contentRect.height, contentRect.y);
        if (dx != 0 || dy != 0) {
            Point viewPosition = this.getViewPosition();
            Dimension viewSize = view.getSize();
            int startX = viewPosition.x;
            int startY = viewPosition.y;
            Dimension extent = this.getExtentSize();
            viewPosition.x -= dx;
            viewPosition.y -= dy;
            if (view.isValid()) {
                if (this.getParent().getComponentOrientation().isLeftToRight()) {
                    if (viewPosition.x + extent.width > viewSize.width) {
                        viewPosition.x = Math.max(0, viewSize.width - extent.width);
                    } else if (viewPosition.x < 0) {
                        viewPosition.x = 0;
                    }
                } else {
                    viewPosition.x = extent.width > viewSize.width ? viewSize.width - extent.width : Math.max(0, Math.min(viewSize.width - extent.width, viewPosition.x));
                }
                if (viewPosition.y + extent.height > viewSize.height) {
                    viewPosition.y = Math.max(0, viewSize.height - extent.height);
                } else if (viewPosition.y < 0) {
                    viewPosition.y = 0;
                }
            }
            if (viewPosition.x != startX || viewPosition.y != startY) {
                this.setViewPosition(viewPosition);
                this.scrollUnderway = false;
            }
        }
    }

    private void validateView() {
        Container validateRoot = SwingUtilities.getValidateRoot(this, false);
        if (validateRoot == null) {
            return;
        }
        ((Component)validateRoot).validate();
        RepaintManager rm = RepaintManager.currentManager(this);
        if (rm != null) {
            rm.removeInvalidComponent((JComponent)validateRoot);
        }
    }

    private int positionAdjustment(int parentWidth, int childWidth, int childAt) {
        if (childAt >= 0 && childWidth + childAt <= parentWidth) {
            return 0;
        }
        if (childAt <= 0 && childWidth + childAt >= parentWidth) {
            return 0;
        }
        if (childAt > 0 && childWidth <= parentWidth) {
            return -childAt + parentWidth - childWidth;
        }
        if (childAt >= 0 && childWidth >= parentWidth) {
            return -childAt;
        }
        if (childAt <= 0 && childWidth <= parentWidth) {
            return -childAt;
        }
        if (childAt < 0 && childWidth >= parentWidth) {
            return -childAt + parentWidth - childWidth;
        }
        return 0;
    }

    @Override
    public final void setBorder(Border border) {
        if (border != null) {
            throw new IllegalArgumentException("JViewport.setBorder() not supported");
        }
    }

    @Override
    public final Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    @BeanProperty(expert=true)
    public final Insets getInsets(Insets insets) {
        insets.bottom = 0;
        insets.right = 0;
        insets.top = 0;
        insets.left = 0;
        return insets;
    }

    private Graphics getBackingStoreGraphics(Graphics g) {
        Graphics bsg = this.backingStoreImage.getGraphics();
        bsg.setColor(g.getColor());
        bsg.setFont(g.getFont());
        bsg.setClip(g.getClipBounds());
        return bsg;
    }

    private void paintViaBackingStore(Graphics g) {
        Graphics bsg = this.getBackingStoreGraphics(g);
        try {
            super.paint(bsg);
            g.drawImage(this.backingStoreImage, 0, 0, this);
        }
        finally {
            bsg.dispose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void paintViaBackingStore(Graphics g, Rectangle oClip) {
        Graphics bsg = this.getBackingStoreGraphics(g);
        try {
            super.paint(bsg);
            g.setClip(oClip);
            g.drawImage(this.backingStoreImage, 0, 0, this);
        }
        finally {
            bsg.dispose();
        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    protected boolean isPaintingOrigin() {
        return this.scrollMode == 2;
    }

    private Point getViewLocation() {
        Component view = this.getView();
        if (view != null) {
            return view.getLocation();
        }
        return new Point(0, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paint(Graphics g) {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (this.inBlitPaint) {
            super.paint(g);
            return;
        }
        if (this.repaintAll) {
            this.repaintAll = false;
            clipB = g.getClipBounds();
            if (clipB.width < this.getWidth() || clipB.height < this.getHeight()) {
                this.waitingForRepaint = true;
                if (this.repaintTimer == null) {
                    this.repaintTimer = this.createRepaintTimer();
                }
                this.repaintTimer.stop();
                this.repaintTimer.start();
            } else {
                if (this.repaintTimer != null) {
                    this.repaintTimer.stop();
                }
                this.waitingForRepaint = false;
            }
        } else if (this.waitingForRepaint) {
            clipB = g.getClipBounds();
            if (clipB.width >= this.getWidth() && clipB.height >= this.getHeight()) {
                this.waitingForRepaint = false;
                this.repaintTimer.stop();
            }
        }
        if (!this.backingStore || this.isBlitting() || this.getView() == null) {
            super.paint(g);
            this.lastPaintPosition = this.getViewLocation();
            return;
        }
        Rectangle viewBounds = this.getView().getBounds();
        if (!this.isOpaque()) {
            g.clipRect(0, 0, viewBounds.width, viewBounds.height);
        }
        boolean recreateBackingStoreImage = this.backingStoreImage == null;
        int scaledWidth = width;
        int scaledHeight = height;
        if (g instanceof Graphics2D) {
            double sw = width;
            double sh = height;
            Graphics2D g2d = (Graphics2D)g;
            AffineTransform tx = g2d.getTransform();
            int type = tx.getType();
            if ((type & 0xFFFFFFBE) != 0) {
                if ((type & 0xFFFFFFB8) == 0) {
                    sw = Math.abs((double)width * tx.getScaleX());
                    sh = Math.abs((double)height * tx.getScaleY());
                } else {
                    sw = Math.abs((double)width * Math.hypot(tx.getScaleX(), tx.getShearY()));
                    sh = Math.abs((double)height * Math.hypot(tx.getShearX(), tx.getScaleY()));
                }
            }
            scaledWidth = (int)Math.ceil(sw);
            scaledHeight = (int)Math.ceil(sh);
            if (!recreateBackingStoreImage) {
                if (this.backingStoreImage instanceof BackingStoreMultiResolutionImage) {
                    BackingStoreMultiResolutionImage mrImage = (BackingStoreMultiResolutionImage)this.backingStoreImage;
                    recreateBackingStoreImage = mrImage.scaledWidth != scaledWidth || mrImage.scaledHeight != scaledHeight;
                } else {
                    boolean bl = recreateBackingStoreImage = width != scaledWidth || height != scaledHeight;
                }
            }
        }
        if (recreateBackingStoreImage) {
            this.backingStoreImage = this.createScaledImage(width, height, scaledWidth, scaledHeight);
            Rectangle clip = g.getClipBounds();
            if (clip.width != width || clip.height != height) {
                if (!this.isOpaque()) {
                    g.setClip(0, 0, Math.min(viewBounds.width, width), Math.min(viewBounds.height, height));
                } else {
                    g.setClip(0, 0, width, height);
                }
                this.paintViaBackingStore(g, clip);
            } else {
                this.paintViaBackingStore(g);
            }
        } else if (!this.scrollUnderway || this.lastPaintPosition.equals(this.getViewLocation())) {
            this.paintViaBackingStore(g);
        } else {
            Point blitFrom = new Point();
            Point blitTo = new Point();
            Dimension blitSize = new Dimension();
            Rectangle blitPaint = new Rectangle();
            Point newLocation = this.getViewLocation();
            int dx = newLocation.x - this.lastPaintPosition.x;
            int dy = newLocation.y - this.lastPaintPosition.y;
            boolean canBlit = this.computeBlit(dx, dy, blitFrom, blitTo, blitSize, blitPaint);
            if (!canBlit) {
                this.paintViaBackingStore(g);
            } else {
                int bdx = blitTo.x - blitFrom.x;
                int bdy = blitTo.y - blitFrom.y;
                Rectangle clip = g.getClipBounds();
                g.setClip(0, 0, width, height);
                Graphics bsg = this.getBackingStoreGraphics(g);
                try {
                    bsg.copyArea(blitFrom.x, blitFrom.y, blitSize.width, blitSize.height, bdx, bdy);
                    g.setClip(clip.x, clip.y, clip.width, clip.height);
                    Rectangle r = viewBounds.intersection(blitPaint);
                    bsg.setClip(r);
                    super.paint(bsg);
                    g.drawImage(this.backingStoreImage, 0, 0, this);
                }
                finally {
                    bsg.dispose();
                }
            }
        }
        this.lastPaintPosition = this.getViewLocation();
        this.scrollUnderway = false;
    }

    private Image createScaledImage(int width, int height, int scaledWidth, int scaledHeight) {
        if (scaledWidth == width && scaledHeight == height) {
            return this.createImage(width, height);
        }
        Image rvImage = this.createImage(scaledWidth, scaledHeight);
        return new BackingStoreMultiResolutionImage(width, height, scaledWidth, scaledHeight, rvImage);
    }

    @Override
    public void reshape(int x, int y, int w, int h) {
        boolean sizeChanged;
        boolean bl = sizeChanged = this.getWidth() != w || this.getHeight() != h;
        if (sizeChanged) {
            this.backingStoreImage = null;
        }
        super.reshape(x, y, w, h);
        if (sizeChanged || this.viewChanged) {
            this.viewChanged = false;
            this.fireStateChanged();
        }
    }

    @BeanProperty(bound=false, enumerationValues={"JViewport.BLIT_SCROLL_MODE", "JViewport.BACKINGSTORE_SCROLL_MODE", "JViewport.SIMPLE_SCROLL_MODE"}, description="Method of moving contents for incremental scrolls.")
    public void setScrollMode(int mode) {
        this.scrollMode = mode;
        this.backingStore = mode == 2;
    }

    public int getScrollMode() {
        return this.scrollMode;
    }

    @Deprecated
    public boolean isBackingStoreEnabled() {
        return this.scrollMode == 2;
    }

    @Deprecated
    public void setBackingStoreEnabled(boolean enabled) {
        if (enabled) {
            this.setScrollMode(2);
        } else {
            this.setScrollMode(1);
        }
    }

    private boolean isBlitting() {
        Component view = this.getView();
        return this.scrollMode == 1 && view instanceof JComponent && view.isOpaque() && !this.isFPScale();
    }

    private boolean isFPScale() {
        GraphicsConfiguration gc = this.getGraphicsConfiguration();
        if (gc != null) {
            return SwingUtilities2.isFloatingPointScale(gc.getDefaultTransform());
        }
        return false;
    }

    public Component getView() {
        return this.getComponentCount() > 0 ? this.getComponent(0) : null;
    }

    public void setView(Component view) {
        int n = this.getComponentCount();
        for (int i = n - 1; i >= 0; --i) {
            this.remove(this.getComponent(i));
        }
        this.isViewSizeSet = false;
        if (view != null) {
            super.addImpl(view, null, -1);
            this.viewListener = this.createViewListener();
            view.addComponentListener(this.viewListener);
        }
        if (this.hasHadValidView) {
            this.fireStateChanged();
        } else if (view != null) {
            this.hasHadValidView = true;
        }
        this.viewChanged = true;
        this.revalidate();
        this.repaint();
    }

    public Dimension getViewSize() {
        Component view = this.getView();
        if (view == null) {
            return new Dimension(0, 0);
        }
        if (this.isViewSizeSet) {
            return view.getSize();
        }
        return view.getPreferredSize();
    }

    public void setViewSize(Dimension newSize) {
        Dimension oldSize;
        Component view = this.getView();
        if (view != null && !newSize.equals(oldSize = view.getSize())) {
            this.scrollUnderway = false;
            view.setSize(newSize);
            this.isViewSizeSet = true;
            this.fireStateChanged();
        }
    }

    public Point getViewPosition() {
        Component view = this.getView();
        if (view != null) {
            Point p = view.getLocation();
            p.x = -p.x;
            p.y = -p.y;
            return p;
        }
        return new Point(0, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setViewPosition(Point p) {
        int oldY;
        int oldX;
        Component view = this.getView();
        if (view == null) {
            return;
        }
        int x = p.x;
        int y = p.y;
        if (view instanceof JComponent) {
            JComponent c = (JComponent)view;
            oldX = c.getX();
            oldY = c.getY();
        } else {
            Rectangle r = view.getBounds();
            oldX = r.x;
            oldY = r.y;
        }
        int newX = -x;
        int newY = -y;
        if (oldX != newX || oldY != newY) {
            if (!this.waitingForRepaint && this.isBlitting() && this.canUseWindowBlitter()) {
                JComponent jview;
                RepaintManager rm = RepaintManager.currentManager(this);
                Rectangle dirty = rm.getDirtyRegion(jview = (JComponent)view);
                if (dirty == null || !dirty.contains(jview.getVisibleRect())) {
                    rm.beginPaint();
                    try {
                        Graphics g = JComponent.safelyGetGraphics(this);
                        this.flushViewDirtyRegion(g, dirty);
                        view.setLocation(newX, newY);
                        Rectangle r = new Rectangle(0, 0, this.getWidth(), Math.min(this.getHeight(), jview.getHeight()));
                        g.setClip(r);
                        this.repaintAll = this.windowBlitPaint(g) && this.needsRepaintAfterBlit();
                        g.dispose();
                        rm.notifyRepaintPerformed(this, r.x, r.y, r.width, r.height);
                        rm.markCompletelyClean((JComponent)this.getParent());
                        rm.markCompletelyClean(this);
                        rm.markCompletelyClean(jview);
                    }
                    finally {
                        rm.endPaint();
                    }
                } else {
                    view.setLocation(newX, newY);
                    this.repaintAll = false;
                }
            } else {
                this.scrollUnderway = true;
                view.setLocation(newX, newY);
                this.repaintAll = false;
            }
            this.revalidate();
            this.fireStateChanged();
        }
    }

    public Rectangle getViewRect() {
        return new Rectangle(this.getViewPosition(), this.getExtentSize());
    }

    protected boolean computeBlit(int dx, int dy, Point blitFrom, Point blitTo, Dimension blitSize, Rectangle blitPaint) {
        int dxAbs = Math.abs(dx);
        int dyAbs = Math.abs(dy);
        Dimension extentSize = this.getExtentSize();
        if (dx == 0 && dy != 0 && dyAbs < extentSize.height) {
            if (dy < 0) {
                blitFrom.y = -dy;
                blitTo.y = 0;
                blitPaint.y = extentSize.height + dy;
            } else {
                blitFrom.y = 0;
                blitTo.y = dy;
                blitPaint.y = 0;
            }
            blitTo.x = 0;
            blitFrom.x = 0;
            blitPaint.x = 0;
            blitSize.width = extentSize.width;
            blitSize.height = extentSize.height - dyAbs;
            blitPaint.width = extentSize.width;
            blitPaint.height = dyAbs;
            return true;
        }
        if (dy == 0 && dx != 0 && dxAbs < extentSize.width) {
            if (dx < 0) {
                blitFrom.x = -dx;
                blitTo.x = 0;
                blitPaint.x = extentSize.width + dx;
            } else {
                blitFrom.x = 0;
                blitTo.x = dx;
                blitPaint.x = 0;
            }
            blitTo.y = 0;
            blitFrom.y = 0;
            blitPaint.y = 0;
            blitSize.width = extentSize.width - dxAbs;
            blitSize.height = extentSize.height;
            blitPaint.width = dxAbs;
            blitPaint.height = extentSize.height;
            return true;
        }
        return false;
    }

    @Transient
    public Dimension getExtentSize() {
        return this.getSize();
    }

    public Dimension toViewCoordinates(Dimension size) {
        return new Dimension(size);
    }

    public Point toViewCoordinates(Point p) {
        return new Point(p);
    }

    public void setExtentSize(Dimension newExtent) {
        Dimension oldExtent = this.getExtentSize();
        if (!newExtent.equals(oldExtent)) {
            this.setSize(newExtent);
            this.fireStateChanged();
        }
    }

    protected ViewListener createViewListener() {
        return new ViewListener();
    }

    protected LayoutManager createLayoutManager() {
        return ViewportLayout.SHARED_INSTANCE;
    }

    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    @Override
    public void repaint(long tm, int x, int y, int w, int h) {
        Container parent = this.getParent();
        if (parent != null) {
            parent.repaint(tm, x + this.getX(), y + this.getY(), w, h);
        } else {
            super.repaint(tm, x, y, w, h);
        }
    }

    @Override
    protected String paramString() {
        String isViewSizeSetString = this.isViewSizeSet ? "true" : "false";
        String lastPaintPositionString = this.lastPaintPosition != null ? this.lastPaintPosition.toString() : "";
        String scrollUnderwayString = this.scrollUnderway ? "true" : "false";
        return super.paramString() + ",isViewSizeSet=" + isViewSizeSetString + ",lastPaintPosition=" + lastPaintPositionString + ",scrollUnderway=" + scrollUnderwayString;
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
        if (propertyName.equals(EnableWindowBlit)) {
            if (newValue != null) {
                this.setScrollMode(1);
            } else {
                this.setScrollMode(0);
            }
        }
    }

    private boolean needsRepaintAfterBlit() {
        Object peer;
        Container heavyParent;
        for (heavyParent = this.getParent(); heavyParent != null && heavyParent.isLightweight(); heavyParent = heavyParent.getParent()) {
        }
        return heavyParent == null || (peer = AWTAccessor.getComponentAccessor().getPeer(heavyParent)) == null || !peer.canDetermineObscurity() || peer.isObscured();
    }

    private Timer createRepaintTimer() {
        Timer timer = new Timer(300, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (JViewport.this.waitingForRepaint) {
                    JViewport.this.repaint();
                }
            }
        });
        timer.setRepeats(false);
        return timer;
    }

    private void flushViewDirtyRegion(Graphics g, Rectangle dirty) {
        JComponent view = (JComponent)this.getView();
        if (dirty != null && dirty.width > 0 && dirty.height > 0) {
            dirty.x += view.getX();
            dirty.y += view.getY();
            Rectangle clip = g.getClipBounds();
            if (clip == null) {
                g.setClip(0, 0, this.getWidth(), this.getHeight());
            }
            g.clipRect(dirty.x, dirty.y, dirty.width, dirty.height);
            clip = g.getClipBounds();
            if (clip.width > 0 && clip.height > 0) {
                this.paintView(g);
            }
        }
    }

    private boolean windowBlitPaint(Graphics g) {
        boolean retValue;
        int width = this.getWidth();
        int height = this.getHeight();
        if (width == 0 || height == 0) {
            return false;
        }
        RepaintManager rm = RepaintManager.currentManager(this);
        JComponent view = (JComponent)this.getView();
        if (this.lastPaintPosition == null || this.lastPaintPosition.equals(this.getViewLocation())) {
            this.paintView(g);
            retValue = false;
        } else {
            Point blitFrom = new Point();
            Point blitTo = new Point();
            Dimension blitSize = new Dimension();
            Rectangle blitPaint = new Rectangle();
            Point newLocation = this.getViewLocation();
            int dx = newLocation.x - this.lastPaintPosition.x;
            int dy = newLocation.y - this.lastPaintPosition.y;
            boolean canBlit = this.computeBlit(dx, dy, blitFrom, blitTo, blitSize, blitPaint);
            if (!canBlit) {
                this.paintView(g);
                retValue = false;
            } else {
                Rectangle r = view.getBounds().intersection(blitPaint);
                r.x -= view.getX();
                r.y -= view.getY();
                this.blitDoubleBuffered(view, g, r.x, r.y, r.width, r.height, blitFrom.x, blitFrom.y, blitTo.x, blitTo.y, blitSize.width, blitSize.height);
                retValue = true;
            }
        }
        this.lastPaintPosition = this.getViewLocation();
        return retValue;
    }

    private void blitDoubleBuffered(JComponent view, Graphics g, int clipX, int clipY, int clipW, int clipH, int blitFromX, int blitFromY, int blitToX, int blitToY, int blitW, int blitH) {
        RepaintManager rm = RepaintManager.currentManager(this);
        int bdx = blitToX - blitFromX;
        int bdy = blitToY - blitFromY;
        Composite oldComposite = null;
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D)g;
            oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.Src);
        }
        rm.copyArea(this, g, blitFromX, blitFromY, blitW, blitH, bdx, bdy, false);
        if (oldComposite != null) {
            ((Graphics2D)g).setComposite(oldComposite);
        }
        int x = view.getX();
        int y = view.getY();
        g.translate(x, y);
        g.setClip(clipX, clipY, clipW, clipH);
        view.paintForceDoubleBuffered(g);
        g.translate(-x, -y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void paintView(Graphics g) {
        Rectangle clip = g.getClipBounds();
        JComponent view = (JComponent)this.getView();
        if (view.getWidth() >= this.getWidth()) {
            int x = view.getX();
            int y = view.getY();
            g.translate(x, y);
            g.setClip(clip.x - x, clip.y - y, clip.width, clip.height);
            view.paintForceDoubleBuffered(g);
            g.translate(-x, -y);
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        } else {
            try {
                this.inBlitPaint = true;
                this.paintForceDoubleBuffered(g);
            }
            finally {
                this.inBlitPaint = false;
            }
        }
    }

    private boolean canUseWindowBlitter() {
        Container parent;
        if (!this.isShowing() || !(this.getParent() instanceof JComponent) && !(this.getView() instanceof JComponent)) {
            return false;
        }
        if (this.isPainting()) {
            return false;
        }
        Rectangle dirtyRegion = RepaintManager.currentManager(this).getDirtyRegion((JComponent)this.getParent());
        if (dirtyRegion != null && dirtyRegion.width > 0 && dirtyRegion.height > 0) {
            return false;
        }
        Rectangle clip = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        Rectangle oldClip = new Rectangle();
        Rectangle tmp2 = null;
        JViewport lastParent = null;
        for (parent = this; parent != null && JViewport.isLightweightComponent(parent); parent = parent.getParent()) {
            int x = ((Component)parent).getX();
            int y = ((Component)parent).getY();
            int w = ((Component)parent).getWidth();
            int h = ((Component)parent).getHeight();
            oldClip.setBounds(clip);
            SwingUtilities.computeIntersection(0, 0, w, h, clip);
            if (!clip.equals(oldClip)) {
                return false;
            }
            if (lastParent != null && parent instanceof JComponent && !((JComponent)parent).isOptimizedDrawingEnabled()) {
                Component[] comps = parent.getComponents();
                int index = 0;
                for (int i = comps.length - 1; i >= 0; --i) {
                    if (comps[i] != lastParent) continue;
                    index = i - 1;
                    break;
                }
                while (index >= 0) {
                    if ((tmp2 = comps[index].getBounds(tmp2)).intersects(clip)) {
                        return false;
                    }
                    --index;
                }
            }
            clip.x += x;
            clip.y += y;
            lastParent = parent;
        }
        return parent != null;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJViewport();
        }
        return this.accessibleContext;
    }

    static class BackingStoreMultiResolutionImage
    extends AbstractMultiResolutionImage {
        private final int width;
        private final int height;
        private final int scaledWidth;
        private final int scaledHeight;
        private final Image rvImage;

        public BackingStoreMultiResolutionImage(int width, int height, int scaledWidth, int scaledHeight, Image rvImage) {
            this.width = width;
            this.height = height;
            this.scaledWidth = scaledWidth;
            this.scaledHeight = scaledHeight;
            this.rvImage = rvImage;
        }

        @Override
        public int getWidth(ImageObserver observer) {
            return this.width;
        }

        @Override
        public int getHeight(ImageObserver observer) {
            return this.height;
        }

        @Override
        protected Image getBaseImage() {
            return this.rvImage;
        }

        @Override
        public Graphics getGraphics() {
            Graphics graphics = this.rvImage.getGraphics();
            if (graphics instanceof Graphics2D) {
                double sx = (double)this.scaledWidth / (double)this.width;
                double sy = (double)this.scaledHeight / (double)this.height;
                ((Graphics2D)graphics).scale(sx, sy);
            }
            return graphics;
        }

        @Override
        public Image getResolutionVariant(double w, double h) {
            return this.rvImage;
        }

        @Override
        public List<Image> getResolutionVariants() {
            return Collections.unmodifiableList(Arrays.asList(this.rvImage));
        }
    }

    protected class ViewListener
    extends ComponentAdapter
    implements Serializable {
        protected ViewListener() {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            JViewport.this.fireStateChanged();
            JViewport.this.revalidate();
        }
    }

    protected class AccessibleJViewport
    extends JComponent.AccessibleJComponent {
        protected AccessibleJViewport() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.VIEWPORT;
        }
    }
}

