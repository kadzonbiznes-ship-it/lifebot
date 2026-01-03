/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import lombok.Generated;
import org.pbrands.Startup;
import org.pbrands.logic.Application;
import org.pbrands.model.BotStatus;
import org.pbrands.netty.ConnectionStatus;
import org.pbrands.settings.SettingsWindow;
import org.pbrands.ui.components.RoundedPanel;
import org.pbrands.ui.components.ShadowPanel;
import org.pbrands.util.FontUtil;
import org.pbrands.util.ResolutionScaler;

public class AppUI {
    private final Application application;
    private final JFrame frame;
    private final JLabel statusLabelPrefix;
    private final JLabel statusLabel;
    private final JLabel actionLabel;
    private final SettingsWindow settingsWindow;
    private Point initialClick;
    private final ResolutionScaler scaler;
    private final ResolutionScaler originScaler;
    private static final int FRAME_SIZE_X = 250;
    private static final int FRAME_SIZE_Y = 150;
    private static final int FRAME_LOCATION_Y = 420;

    public AppUI(Application application, ResolutionScaler scaler) {
        this.scaler = scaler;
        this.originScaler = new ResolutionScaler(2560, 1440, scaler.getWidth(), scaler.getHeight(), false);
        this.application = application;
        this.frame = new JFrame("Bot Controller (END toggle)");
        this.statusLabelPrefix = this.createShadowedLabel("Status: ", BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.statusLabel = this.createShadowedLabel("Zatrzymany", BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.updateStatusLabel(BotStatus.STOPPED);
        this.actionLabel = this.createShadowedLabel("Akcja: N/A");
        this.settingsWindow = new SettingsWindow(application);
        this.setupUI();
    }

    private void setupUI() {
        int leftOffset = 10;
        this.frame.setUndecorated(true);
        this.frame.setShape(new RoundRectangle2D.Double(0.0, 0.0, this.scaler.rescaleX(250) - leftOffset, this.scaler.rescaleY(150), this.scaler.rescaleSize(40), this.scaler.rescaleSize(40)));
        RoundedPanel statusPanel = new RoundedPanel(this.scaler.rescaleSize(40));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, leftOffset, 0, 0));
        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.fill = 2;
        gbc.insets = new Insets(this.scaler.rescaleSize(5), this.scaler.rescaleSize(10), this.scaler.rescaleSize(5), this.scaler.rescaleSize(10));
        gbc.anchor = 17;
        Color background = statusPanel.getBackground();
        this.addComponentToPanel(statusPanel, this.createStatusStatePanel(background), gbc);
        JPanel logoBackgroundPanel = new JPanel(null){

            @Override
            public Dimension getPreferredSize() {
                int width = AppUI.this.scaler.rescaleX(240);
                int height = AppUI.this.scaler.rescaleY(90);
                return new Dimension(width, height);
            }
        };
        logoBackgroundPanel.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                AppUI.this.initialClick = e.getPoint();
                AppUI.this.frame.getComponentAt(AppUI.this.initialClick);
            }
        });
        logoBackgroundPanel.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = AppUI.this.frame.getLocation().x;
                int thisY = AppUI.this.frame.getLocation().y;
                int xMoved = e.getX() - AppUI.this.initialClick.x;
                int yMoved = e.getY() - AppUI.this.initialClick.y;
                int x = thisX + xMoved;
                int y = thisY + yMoved;
                AppUI.this.frame.setLocation(x, y);
                AppUI.this.settingsWindow.getFrame().setLocation(x + AppUI.this.frame.getWidth() + 5, y);
            }
        });
        logoBackgroundPanel.setBackground(new Color(40, 40, 40));
        logoBackgroundPanel.setBounds(0, 0, this.scaler.rescaleX(250) - leftOffset, this.scaler.rescaleY(60) - leftOffset / 2);
        JLabel logoLabel = this.createLogoLabel(this.scaler.rescaleX(160), this.scaler.rescaleY(30));
        logoLabel.setBounds((logoBackgroundPanel.getPreferredSize().width - logoLabel.getPreferredSize().width) / 2, (logoBackgroundPanel.getPreferredSize().height - logoLabel.getPreferredSize().height) / 2, logoLabel.getPreferredSize().width, logoLabel.getPreferredSize().height);
        logoBackgroundPanel.add(logoLabel);
        JButton settingsButton = this.createSettingsButton();
        int padding = this.scaler.rescaleSize(5);
        settingsButton.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        int buttonWidth = this.scaler.rescaleX(24) + 2 * padding;
        int buttonHeight = this.scaler.rescaleY(24) + 2 * padding;
        settingsButton.setBounds(logoBackgroundPanel.getWidth() - buttonWidth - padding, padding, buttonWidth, buttonHeight);
        logoBackgroundPanel.add(settingsButton);
        RoundedPanel contentPanel = new RoundedPanel(this.scaler.rescaleSize(40));
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add((Component)logoBackgroundPanel, "North");
        contentPanel.add((Component)statusPanel, "Before");
        ShadowPanel shadowPanel = new ShadowPanel(this.scaler.rescaleX(250), this.scaler.rescaleY(150), this.scaler.rescaleSize(40), this.scaler.rescaleSize(10));
        shadowPanel.setLayout(new BorderLayout());
        shadowPanel.add((Component)contentPanel, "Before");
        this.frame.setContentPane(shadowPanel);
        this.frame.pack();
        this.frame.setSize(250, 150);
        this.frame.setLocation(0, this.originScaler.rescaleY(420));
        this.frame.setDefaultCloseOperation(3);
        this.frame.setAlwaysOnTop(true);
        this.frame.setFocusable(false);
        this.frame.setFocusableWindowState(false);
    }

    private JPanel createStatusStatePanel(Color backgroundColor) {
        JPanel statusStatePanel = new JPanel(new FlowLayout(0, 0, 0));
        statusStatePanel.setBorder(BorderFactory.createEmptyBorder());
        statusStatePanel.setBackground(backgroundColor);
        statusStatePanel.add(this.statusLabelPrefix);
        statusStatePanel.add(this.statusLabel);
        return statusStatePanel;
    }

    private JLabel createLogoLabel(final int scaledWidth, final int scaledHeight) {
        BufferedImage image = null;
        try {
            InputStream inputStream = Startup.class.getResourceAsStream("/images/logo.png");
            if (inputStream != null) {
                image = ImageIO.read(inputStream);
            } else {
                System.err.println("Resource not found: logo.png");
            }
        }
        catch (IOException inputStream) {
            // empty catch block
        }
        if (image != null) {
            final BufferedImage finalImage = image;
            JLabel label = new JLabel(new Icon(){

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.drawImage(finalImage, x, y, scaledWidth, scaledHeight, null);
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return scaledWidth;
                }

                @Override
                public int getIconHeight() {
                    return scaledHeight;
                }
            });
            label.setBorder(BorderFactory.createEmptyBorder(this.scaler.rescaleSize(30), 0, this.scaler.rescaleSize(30), 0));
            label.setHorizontalAlignment(0);
            return label;
        }
        return new JLabel("Logo not found");
    }

    private JLabel createShadowedLabel(String text, Border border) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(2);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(FontUtil.NUNITO_BLACK.deriveFont(14.0f));
        label.setBorder(border);
        return label;
    }

    private JLabel createShadowedLabel(String text) {
        int borderSize = 10;
        return this.createShadowedLabel(text, BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
    }

    private JButton createSettingsButton() {
        BufferedImage image = null;
        try {
            InputStream inputStream = Startup.class.getResourceAsStream("/images/gear.png");
            if (inputStream != null) {
                image = ImageIO.read(inputStream);
            } else {
                System.err.println("Resource not found: gear.png");
            }
        }
        catch (IOException inputStream) {
            // empty catch block
        }
        if (image != null) {
            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(1003), null);
            image = op.filter(image, null);
            int width = this.scaler.rescaleX(24);
            int height = this.scaler.rescaleY(24);
            Image scaledImage = image.getScaledInstance(width, height, 4);
            ImageIcon icon = new ImageIcon(scaledImage);
            JButton button = new JButton(icon);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                JFrame settingsFrame = this.settingsWindow.getFrame();
                this.settingsWindow.setEnabled(!this.settingsWindow.isEnabled());
                int x = this.frame.getLocation().x + this.frame.getWidth() + 5;
                int y = this.frame.getLocation().y;
                settingsFrame.setLocation(x, y);
            }));
            button.setToolTipText("Ustawienia");
            return button;
        }
        return new JButton("Ustawienia");
    }

    private void addComponentToPanel(JPanel panel, JComponent component, GridBagConstraints gbc) {
        component.setBorder(BorderFactory.createEmptyBorder());
        panel.add((Component)component, gbc);
    }

    public void setVisible(boolean visible) {
        this.frame.pack();
        this.frame.setSize(250, 150);
        this.frame.setLocation(0, this.originScaler.rescaleY(420));
        this.frame.setVisible(visible);
    }

    public boolean isVisible() {
        return this.frame.isVisible();
    }

    public void updateStatusLabel(BotStatus status) {
        SwingUtilities.invokeLater(() -> {
            ConnectionStatus connectionStatus = this.application.getNettyClient().getConnectionStatus();
            switch (connectionStatus) {
                case CONNECTED: {
                    switch (status) {
                        case RUNNING: {
                            this.statusLabel.setForeground(Color.GREEN);
                            this.statusLabel.setText("Uruchomiony");
                            break;
                        }
                        case STOPPING: {
                            this.statusLabel.setForeground(Color.ORANGE);
                            this.statusLabel.setText("Zatrzymywanie..");
                            break;
                        }
                        case STOPPED: {
                            this.statusLabel.setForeground(Color.RED);
                            this.statusLabel.setText("Zatrzymany");
                        }
                    }
                    break;
                }
                case DISCONNECTED: {
                    this.statusLabel.setForeground(Color.ORANGE);
                    this.statusLabel.setText("Brak po\u0142\u0105czenia");
                    break;
                }
                case RECONNECTING: {
                    this.statusLabel.setForeground(Color.ORANGE);
                    this.statusLabel.setText("Ponowne \u0142\u0105czenie...");
                }
            }
        });
    }

    public Consumer<String> getActionUpdater() {
        return text -> SwingUtilities.invokeLater(() -> this.actionLabel.setText("Akcja: " + text));
    }

    @Generated
    public JFrame getFrame() {
        return this.frame;
    }

    @Generated
    public SettingsWindow getSettingsWindow() {
        return this.settingsWindow;
    }
}

