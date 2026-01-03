/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.peer.ContainerPeer;
import sun.java2d.pipe.Region;

public interface ComponentPeer {
    public static final int SET_LOCATION = 1;
    public static final int SET_SIZE = 2;
    public static final int SET_BOUNDS = 3;
    public static final int SET_CLIENT_SIZE = 4;
    public static final int RESET_OPERATION = 5;
    public static final int NO_EMBEDDED_CHECK = 16384;
    public static final int DEFAULT_OPERATION = 3;

    public boolean isObscured();

    public boolean canDetermineObscurity();

    public void setVisible(boolean var1);

    public void setEnabled(boolean var1);

    public void paint(Graphics var1);

    public void print(Graphics var1);

    public void setBounds(int var1, int var2, int var3, int var4, int var5);

    public void handleEvent(AWTEvent var1);

    public void coalescePaintEvent(PaintEvent var1);

    public Point getLocationOnScreen();

    public Dimension getPreferredSize();

    public Dimension getMinimumSize();

    public ColorModel getColorModel();

    public Graphics getGraphics();

    public FontMetrics getFontMetrics(Font var1);

    public void dispose();

    public void setForeground(Color var1);

    public void setBackground(Color var1);

    public void setFont(Font var1);

    public void updateCursorImmediately();

    public boolean requestFocus(Component var1, boolean var2, boolean var3, long var4, FocusEvent.Cause var6);

    public boolean isFocusable();

    public Image createImage(int var1, int var2);

    public VolatileImage createVolatileImage(int var1, int var2);

    public GraphicsConfiguration getGraphicsConfiguration();

    public boolean handlesWheelScrolling();

    public void createBuffers(int var1, BufferCapabilities var2) throws AWTException;

    public Image getBackBuffer();

    public void flip(int var1, int var2, int var3, int var4, BufferCapabilities.FlipContents var5);

    public void destroyBuffers();

    public void reparent(ContainerPeer var1);

    public boolean isReparentSupported();

    public void layout();

    public void applyShape(Region var1);

    public void setZOrder(ComponentPeer var1);

    public boolean updateGraphicsData(GraphicsConfiguration var1);
}

