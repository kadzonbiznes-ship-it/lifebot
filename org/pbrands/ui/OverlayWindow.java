/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.logic.DebugPixel
 */
package org.pbrands.ui;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import lombok.Generated;
import org.pbrands.logic.DebugPixel;

public class OverlayWindow
extends JWindow {
    private final JLabel statusLabel;
    private final List<DebugPixel> debugPoints = new CopyOnWriteArrayList<DebugPixel>();
    private boolean enabled = false;

    public OverlayWindow() {
        this.setAlwaysOnTop(true);
        this.setFocusableWindowState(false);
        this.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                g2d.setComposite(AlphaComposite.SrcOver);
                for (DebugPixel dp : OverlayWindow.this.debugPoints) {
                    g2d.setColor(dp.color);
                    g2d.fillOval(dp.point.x - 3, dp.point.y + 9, 6, 6);
                }
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        this.statusLabel = new JLabel("Initializing...");
        this.statusLabel.setForeground(Color.WHITE);
        this.statusLabel.setFont(new Font("Arial", 1, 14));
        this.statusLabel.setOpaque(false);
        panel.add((Component)this.statusLabel, new GridBagConstraints());
        this.add(panel);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize);
        this.setLocationRelativeTo(null);
        this.setVisible(this.enabled);
        if (this.enabled) {
            this.makeWindowTransparentToMouse();
        }
    }

    public void updateStatus(String text) {
        SwingUtilities.invokeLater(() -> this.statusLabel.setText(text));
    }

    public void updateDebugPoints(List<DebugPixel> points) {
        SwingUtilities.invokeLater(() -> {
            this.debugPoints.clear();
            this.debugPoints.addAll(points);
            this.repaint();
        });
    }

    private void makeWindowTransparentToMouse() {
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(this));
        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, -20);
        exStyle = exStyle | 0x20 | 0x80000;
        User32.INSTANCE.SetWindowLong(hwnd, -20, exStyle);
    }

    @Override
    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }
}

