/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.pbrands.Startup;
import org.pbrands.util.FontUtil;

public class ProgressWindow
extends JFrame {
    private final JLabel statusLabel;
    private final JProgressBar spinner;

    public ProgressWindow() {
        this(true);
    }

    public ProgressWindow(boolean indeterminate) {
        this.setTitle("Startup");
        this.setSize(300, 80);
        this.setUndecorated(true);
        this.setDefaultCloseOperation(3);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.setFocusable(false);
        this.setFocusableWindowState(false);
        this.setAlwaysOnTop(true);
        this.setShape(new RoundRectangle2D.Double(0.0, 0.0, 300.0, 80.0, 20.0, 20.0));
        ImageIcon icon = new ImageIcon(Startup.class.getResource("/images/favicon.png"));
        this.setIconImage(icon.getImage());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, 1));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        this.add((Component)mainPanel, "Center");
        this.statusLabel = new JLabel("Inicializacja...", 0);
        this.statusLabel.setForeground(Color.WHITE);
        this.statusLabel.setFont(FontUtil.NUNITO_BLACK);
        this.statusLabel.setAlignmentX(0.5f);
        mainPanel.add(this.statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.spinner = new JProgressBar();
        this.spinner.setIndeterminate(indeterminate);
        if (!indeterminate) {
            this.spinner.setStringPainted(true);
            this.spinner.setMinimum(0);
            this.spinner.setMaximum(100);
        }
        this.spinner.setPreferredSize(new Dimension(50, 50));
        this.spinner.setAlignmentX(0.5f);
        this.spinner.setForeground(new Color(70, 130, 180));
        this.spinner.setBackground(new Color(50, 50, 50));
        this.spinner.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(this.spinner);
    }

    public void updateProgress(int value) {
        this.spinner.setValue(value);
    }

    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> this.statusLabel.setText(message));
    }

    public void showSpinner() {
        SwingUtilities.invokeLater(() -> this.spinner.setVisible(true));
    }

    public void hideSpinner() {
        SwingUtilities.invokeLater(() -> this.spinner.setVisible(false));
    }
}

