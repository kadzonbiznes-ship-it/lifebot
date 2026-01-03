/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.peer.CanvasPeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LightweightPeer;
import java.awt.peer.PanelPeer;
import sun.java2d.pipe.Region;

public class NullComponentPeer
implements LightweightPeer,
CanvasPeer,
PanelPeer {
    @Override
    public boolean isObscured() {
        return false;
    }

    @Override
    public boolean canDetermineObscurity() {
        return false;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void setVisible(boolean b) {
    }

    public void show() {
    }

    public void hide() {
    }

    @Override
    public void setEnabled(boolean b) {
    }

    public void enable() {
    }

    public void disable() {
    }

    @Override
    public void paint(Graphics g) {
    }

    public void repaint(long tm, int x, int y, int width, int height) {
    }

    @Override
    public void print(Graphics g) {
    }

    @Override
    public void setBounds(int x, int y, int width, int height, int op) {
    }

    public void reshape(int x, int y, int width, int height) {
    }

    @Override
    public void coalescePaintEvent(PaintEvent e) {
    }

    public boolean handleEvent(Event e) {
        return false;
    }

    @Override
    public void handleEvent(AWTEvent arg0) {
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1, 1);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(1, 1);
    }

    @Override
    public ColorModel getColorModel() {
        return null;
    }

    @Override
    public Graphics getGraphics() {
        return null;
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        return null;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setForeground(Color c) {
    }

    @Override
    public void setBackground(Color c) {
    }

    @Override
    public void setFont(Font f) {
    }

    @Override
    public void updateCursorImmediately() {
    }

    public void setCursor(Cursor cursor) {
    }

    @Override
    public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEvent.Cause cause) {
        return false;
    }

    @Override
    public Image createImage(int width, int height) {
        return null;
    }

    public Dimension preferredSize() {
        return this.getPreferredSize();
    }

    public Dimension minimumSize() {
        return this.getMinimumSize();
    }

    @Override
    public Point getLocationOnScreen() {
        return new Point(0, 0);
    }

    @Override
    public Insets getInsets() {
        return this.insets();
    }

    @Override
    public void beginValidate() {
    }

    @Override
    public void endValidate() {
    }

    public Insets insets() {
        return new Insets(0, 0, 0, 0);
    }

    public boolean isPaintPending() {
        return false;
    }

    @Override
    public boolean handlesWheelScrolling() {
        return false;
    }

    @Override
    public VolatileImage createVolatileImage(int width, int height) {
        return null;
    }

    @Override
    public void beginLayout() {
    }

    @Override
    public void endLayout() {
    }

    @Override
    public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
        throw new AWTException("Page-flipping is not allowed on a lightweight component");
    }

    @Override
    public Image getBackBuffer() {
        throw new IllegalStateException("Page-flipping is not allowed on a lightweight component");
    }

    @Override
    public void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {
        throw new IllegalStateException("Page-flipping is not allowed on a lightweight component");
    }

    @Override
    public void destroyBuffers() {
    }

    @Override
    public boolean isReparentSupported() {
        return false;
    }

    @Override
    public void reparent(ContainerPeer newNativeParent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void layout() {
    }

    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public void applyShape(Region shape) {
    }

    @Override
    public void setZOrder(ComponentPeer above) {
    }

    @Override
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        return false;
    }

    @Override
    public GraphicsConfiguration getAppropriateGraphicsConfiguration(GraphicsConfiguration gc) {
        return gc;
    }
}

