/*
 * Decompiled with CFR 0.152.
 */
package javax.accessibility;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import javax.accessibility.Accessible;

public interface AccessibleComponent {
    public Color getBackground();

    public void setBackground(Color var1);

    public Color getForeground();

    public void setForeground(Color var1);

    public Cursor getCursor();

    public void setCursor(Cursor var1);

    public Font getFont();

    public void setFont(Font var1);

    public FontMetrics getFontMetrics(Font var1);

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public boolean isVisible();

    public void setVisible(boolean var1);

    public boolean isShowing();

    public boolean contains(Point var1);

    public Point getLocationOnScreen();

    public Point getLocation();

    public void setLocation(Point var1);

    public Rectangle getBounds();

    public void setBounds(Rectangle var1);

    public Dimension getSize();

    public void setSize(Dimension var1);

    public Accessible getAccessibleAt(Point var1);

    public boolean isFocusTraversable();

    public void requestFocus();

    public void addFocusListener(FocusListener var1);

    public void removeFocusListener(FocusListener var1);
}

