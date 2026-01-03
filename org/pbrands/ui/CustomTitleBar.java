/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CustomTitleBar
extends JPanel {
    private JButton closeButton;
    private JButton minimizeButton;
    private JLabel logoLabel;
    private JPanel buttonPanel;
    private int mouseX;
    private int mouseY;
    private Runnable onDragStart;
    private Runnable onDragEnd;

    public CustomTitleBar(JFrame parentFrame, ImageIcon logoIcon, Runnable onClose) {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(parentFrame.getWidth(), 40));
        this.setOpaque(true);
        this.setBackground(Color.decode("#252526"));
        this.setBorder(new EmptyBorder(0, 10, 0, 0));
        this.logoLabel = new JLabel();
        this.logoLabel.setHorizontalAlignment(2);
        if (logoIcon == null || logoIcon.getIconWidth() == -1) {
            this.logoLabel.setText("LifeBot");
            this.logoLabel.setForeground(Color.WHITE);
            this.logoLabel.setFont(new Font("Segoe UI", 1, 14));
        } else {
            final int targetHeight = 24;
            final int targetWidth = logoIcon.getIconWidth() * targetHeight / logoIcon.getIconHeight();
            final Image scaledImg = logoIcon.getImage().getScaledInstance(targetWidth, targetHeight, 4);
            this.logoLabel.setIcon(new Icon(){

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.drawImage(scaledImg, x, y, null);
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return targetWidth;
                }

                @Override
                public int getIconHeight() {
                    return targetHeight;
                }
            });
        }
        this.add((Component)this.logoLabel, "West");
        this.buttonPanel = new JPanel(new FlowLayout(2, 0, 0));
        this.buttonPanel.setOpaque(false);
        this.minimizeButton = this.createStyledButton("\u2014");
        this.minimizeButton.addActionListener(e -> parentFrame.setState(1));
        this.buttonPanel.add(this.minimizeButton);
        this.closeButton = this.createStyledButton("X");
        this.closeButton.addActionListener(e -> onClose.run());
        this.buttonPanel.add(this.closeButton);
        this.add((Component)this.buttonPanel, "East");
        this.addDragFunctionality(parentFrame);
    }

    public void setDragListener(Runnable onDragStart, Runnable onDragEnd) {
        this.onDragStart = onDragStart;
        this.onDragEnd = onDragEnd;
    }

    private JButton createStyledButton(String text) {
        final boolean isCloseBtn = "X".equals(text);
        JButton button = new JButton(){
            private boolean hovered = false;
            {
                this.addMouseListener(new MouseAdapter(){

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        this.repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        this.repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this.hovered) {
                    g2.setColor(isCloseBtn ? new Color(232, 17, 35) : new Color(55, 55, 58));
                    g2.fillRect(0, 0, this.getWidth(), this.getHeight());
                }
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.0f));
                int cx = this.getWidth() / 2;
                int cy = this.getHeight() / 2;
                if (isCloseBtn) {
                    int size = 5;
                    g2.drawLine(cx - size, cy - size, cx + size, cy + size);
                    g2.drawLine(cx + size, cy - size, cx - size, cy + size);
                } else {
                    int width = 10;
                    int lineY = cy + 4;
                    g2.drawLine(cx - width / 2, lineY, cx + width / 2, lineY);
                }
                g2.dispose();
            }
        };
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(46, 40));
        return button;
    }

    private void addDragFunctionality(final JFrame parentFrame) {
        this.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                CustomTitleBar.this.mouseX = e.getX();
                CustomTitleBar.this.mouseY = e.getY();
                if (CustomTitleBar.this.onDragStart != null) {
                    CustomTitleBar.this.onDragStart.run();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (CustomTitleBar.this.onDragEnd != null) {
                    CustomTitleBar.this.onDragEnd.run();
                }
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getXOnScreen();
                int currentY = e.getYOnScreen();
                parentFrame.setLocation(currentX - CustomTitleBar.this.mouseX, currentY - CustomTitleBar.this.mouseY);
            }
        });
    }
}

