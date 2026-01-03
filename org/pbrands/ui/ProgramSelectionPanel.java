/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.util.VirtualHIDUtil
 */
package org.pbrands.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Generated;
import org.pbrands.hid.HIDDeviceType;
import org.pbrands.hid.uber.RazerDriverHook;
import org.pbrands.model.Product;
import org.pbrands.model.UserType;
import org.pbrands.ui.latest.RoundedAvatarLabel;
import org.pbrands.ui.main.LoaderWindow;
import org.pbrands.util.FontUtil;
import org.pbrands.util.RegistryUtil;
import org.pbrands.util.ServerRegistry;
import org.pbrands.util.VirtualHIDUtil;

public class ProgramSelectionPanel
extends JPanel {
    private final JList<Product> programList;
    private final JLabel nameLabel;
    private final JEditorPane descriptionArea;
    private final JLabel lastUpdateLabel;
    private final JLabel arduinoRequiredLabel;
    private final JButton loadButton;
    private final UserType userType;
    private final Runnable onSettings;
    private final Runnable onLogout;
    private final ServerRegistry registry;
    private static final Icon ICON_HID;

    public ProgramSelectionPanel(ServerRegistry serverRegistry, final List<Product> products, ActionListener runListener, UserType userType, Runnable onSettings, Runnable onLogout, String username, long subscriptionExpiry, byte[] pfpBytes) {
        super(new BorderLayout(0, 0));
        if (products.isEmpty()) {
            throw new IllegalArgumentException("No products available");
        }
        this.registry = serverRegistry;
        this.userType = userType;
        this.onSettings = onSettings;
        this.onLogout = onLogout;
        this.setOpaque(false);
        JPanel topBar = this.createTopBar(products, username, subscriptionExpiry, pfpBytes);
        this.add((Component)topBar, "North");
        JPanel mainContainer = new JPanel(new BorderLayout(20, 0));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(10, 20, 20, 20));
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(280, 0));
        final JTextField searchField = new JTextField(){
            private boolean focused = false;
            private final String placeholder = "Szukaj programu...";
            {
                this.addFocusListener(new FocusAdapter(){

                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        this.repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        this.repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 8, 8);
                g2.setColor(this.focused ? new Color(0, 122, 204) : new Color(62, 62, 66));
                g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
                if (this.getText().isEmpty() && !this.focused) {
                    Graphics2D g3 = (Graphics2D)g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(new Color(100, 100, 100));
                    g3.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
                    g3.drawString("Szukaj programu...", 12, this.getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 2);
                    g3.dispose();
                }
            }
        };
        searchField.setPreferredSize(new Dimension(0, 38));
        searchField.setOpaque(false);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        searchField.setBorder(new EmptyBorder(8, 12, 8, 12));
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        searchWrapper.add((Component)searchField, "Center");
        sidebarPanel.add((Component)searchWrapper, "North");
        final DefaultListModel listModel = new DefaultListModel();
        products.forEach(listModel::addElement);
        this.programList = new JList(listModel);
        this.programList.setCellRenderer(new ModernProductRenderer());
        this.programList.setSelectionMode(0);
        this.programList.setBackground(new Color(30, 30, 30, 0));
        this.programList.setOpaque(false);
        JScrollPane listScroll = new JScrollPane(this.programList);
        listScroll.setBorder(null);
        listScroll.getViewport().setOpaque(false);
        listScroll.setOpaque(false);
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarPanel.add((Component)listScroll, "Center");
        mainContainer.add((Component)sidebarPanel, "West");
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        JPanel detailsCard = new JPanel(new BorderLayout()){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 37, 38, 230));
                g2.fillRect(0, 0, this.getWidth(), this.getHeight());
                g2.dispose();
            }
        };
        detailsCard.setOpaque(false);
        detailsCard.setBorder(new EmptyBorder(30, 40, 30, 40));
        JPanel headerInfo = new JPanel();
        headerInfo.setLayout(new BoxLayout(headerInfo, 1));
        headerInfo.setOpaque(false);
        this.nameLabel = new JLabel("Nazwa Programu");
        this.nameLabel.setFont(FontUtil.NUNITO_BLACK.deriveFont(28.0f));
        this.nameLabel.setForeground(Color.WHITE);
        this.nameLabel.setAlignmentX(0.0f);
        headerInfo.add(this.nameLabel);
        headerInfo.add(Box.createVerticalStrut(5));
        JPanel metaRow = new JPanel(new FlowLayout(0, 0, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(0.0f);
        this.lastUpdateLabel = new JLabel("Updated: -");
        this.lastUpdateLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(12.0f));
        this.lastUpdateLabel.setForeground(new Color(160, 160, 160));
        metaRow.add(this.lastUpdateLabel);
        JLabel spacer = new JLabel("   \u2022   ");
        spacer.setFont(FontUtil.NUNITO_REGULAR.deriveFont(12.0f));
        spacer.setForeground(new Color(80, 80, 80));
        metaRow.add(spacer);
        this.arduinoRequiredLabel = new JLabel("HID: -");
        this.arduinoRequiredLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(12.0f));
        this.arduinoRequiredLabel.setForeground(new Color(160, 160, 160));
        this.arduinoRequiredLabel.setIcon(ICON_HID);
        metaRow.add(this.arduinoRequiredLabel);
        headerInfo.add(metaRow);
        detailsCard.add((Component)headerInfo, "North");
        this.descriptionArea = new JEditorPane("text/html", "");
        this.descriptionArea.setEditable(false);
        this.descriptionArea.setOpaque(false);
        this.descriptionArea.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);
        this.descriptionArea.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
        this.descriptionArea.setForeground(new Color(200, 200, 200));
        JScrollPane descScroll = new JScrollPane(this.descriptionArea);
        descScroll.setBorder(null);
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setBorder(new EmptyBorder(20, 0, 20, 0));
        detailsCard.add((Component)descScroll, "Center");
        JPanel footerPanel = new JPanel(new FlowLayout(2));
        footerPanel.setOpaque(false);
        this.loadButton = new JButton("Uruchom"){

            @Override
            protected void paintComponent(Graphics g) {
                Color bg;
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int width = this.getWidth();
                int height = this.getHeight();
                Color color = bg = this.isEnabled() ? this.getBackground() : new Color(60, 60, 60);
                if (this.isEnabled() && this.getModel().isRollover()) {
                    bg = new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20), Math.min(255, bg.getBlue() + 20));
                } else if (this.isEnabled() && this.getModel().isPressed()) {
                    bg = bg.darker();
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, width, height, 8, 8);
                g2.setFont(this.getFont());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
                int textX = (width - stringBounds.width) / 2;
                int textY = (height - stringBounds.height) / 2 + fm.getAscent() - 2;
                g2.setColor(this.isEnabled() ? this.getForeground() : new Color(140, 140, 140));
                g2.drawString(this.getText(), textX, textY);
                g2.dispose();
            }
        };
        this.loadButton.setPreferredSize(new Dimension(180, 42));
        this.loadButton.setBackground(new Color(0, 122, 204));
        this.loadButton.setForeground(Color.WHITE);
        this.loadButton.setFont(FontUtil.NUNITO_BOLD.deriveFont(14.0f));
        this.loadButton.setFocusPainted(false);
        this.loadButton.setBorderPainted(false);
        this.loadButton.setContentAreaFilled(false);
        this.loadButton.setCursor(Cursor.getPredefinedCursor(12));
        this.loadButton.addActionListener(e -> {
            if ("Skonfiguruj HID".equals(this.loadButton.getText())) {
                Container parent;
                for (parent = this.getParent(); parent != null && !(parent instanceof LoaderWindow); parent = parent.getParent()) {
                }
                if (parent != null && onSettings != null) {
                    onSettings.run();
                }
            } else {
                runListener.actionPerformed(e);
            }
        });
        footerPanel.add(this.loadButton);
        detailsCard.add((Component)footerPanel, "South");
        detailsPanel.add((Component)detailsCard, "Center");
        mainContainer.add((Component)detailsPanel, "Center");
        this.add((Component)mainContainer, "Center");
        searchField.getDocument().addDocumentListener(new DocumentListener(){

            private void update() {
                String text = searchField.getText().trim().toLowerCase();
                listModel.clear();
                products.stream().filter(p -> p.getName().toLowerCase().contains(text)).forEach(listModel::addElement);
                if (!listModel.isEmpty()) {
                    ProgramSelectionPanel.this.restoreLastSelectedProgram();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.update();
            }
        });
        this.programList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Product selected = this.programList.getSelectedValue();
                if (selected != null) {
                    RegistryUtil.setLastSelectedProgramId(selected.getId());
                }
                this.updateDetails();
                this.updateRunButton();
            }
        });
        this.restoreLastSelectedProgram();
    }

    private JPanel createTopBar(List<Product> products, String username, long subscriptionExpiry, byte[] pfpBytes) {
        LocalDateTime dt;
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 20, 5, 20));
        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = 10;
        gbc.insets = new Insets(0, 0, 0, 15);
        RoundedAvatarLabel avatarLabel = new RoundedAvatarLabel(this.userType);
        try {
            if (pfpBytes != null && pfpBytes.length > 0) {
                avatarIcon = new ImageIcon(pfpBytes);
                avatarLabel.setAvatarImage(avatarIcon.getImage());
            } else {
                avatarIcon = new ImageIcon(this.getClass().getResource("/images/no-profile-picture.png"));
                avatarLabel.setAvatarImage(avatarIcon.getImage());
            }
        }
        catch (Exception e2) {
            avatarLabel.setText(username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "A");
        }
        userPanel.add((Component)avatarLabel, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel textInfo = new JPanel(new GridLayout(2, 1));
        textInfo.setOpaque(false);
        String displayUsername = username != null ? username : "User";
        JLabel userLabel = new JLabel("Witaj, " + displayUsername);
        userLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(15.0f));
        userLabel.setForeground(Color.WHITE);
        Object subText = subscriptionExpiry <= 0L ? "Brak aktywnej subskrypcji" : ((dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(subscriptionExpiry), ZoneId.systemDefault())).getYear() > 2100 ? "Subskrypcja: LIFETIME" : "Wygasa: " + dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        JLabel subLabel = new JLabel((String)subText);
        subLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(11.0f));
        subLabel.setForeground(new Color(140, 140, 140));
        textInfo.add(userLabel);
        textInfo.add(subLabel);
        userPanel.add((Component)textInfo, gbc);
        topBar.add((Component)userPanel, "West");
        JButton settingsButton = new JButton("Ustawienia"){

            @Override
            protected void paintComponent(Graphics g) {
                Color bg;
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = bg = this.getModel().isRollover() ? new Color(70, 70, 74) : new Color(62, 62, 66);
                if (this.getModel().isPressed()) {
                    bg = new Color(50, 50, 54);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        settingsButton.setPreferredSize(new Dimension(110, 34));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setCursor(Cursor.getPredefinedCursor(12));
        settingsButton.addActionListener(e -> {
            if (this.onSettings != null) {
                this.onSettings.run();
            }
        });
        JButton logoutButton = new JButton("Wyloguj"){

            @Override
            protected void paintComponent(Graphics g) {
                Color bg;
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = bg = this.getModel().isRollover() ? new Color(220, 60, 60) : new Color(200, 50, 50);
                if (this.getModel().isPressed()) {
                    bg = new Color(170, 40, 40);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutButton.setPreferredSize(new Dimension(90, 34));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(12));
        logoutButton.addActionListener(e -> {
            if (this.onLogout != null) {
                this.onLogout.run();
            }
        });
        JPanel rightPanel = new JPanel(new FlowLayout(2));
        rightPanel.setOpaque(false);
        rightPanel.add(settingsButton);
        rightPanel.add(logoutButton);
        topBar.add((Component)rightPanel, "East");
        return topBar;
    }

    private void restoreLastSelectedProgram() {
        int lastSelectedId = RegistryUtil.getLastSelectedProgramId();
        DefaultListModel model = (DefaultListModel)this.programList.getModel();
        for (int i = 0; i < model.size(); ++i) {
            if (((Product)model.getElementAt(i)).getId() != lastSelectedId) continue;
            this.programList.setSelectedIndex(i);
            this.programList.ensureIndexIsVisible(i);
            return;
        }
        if (model.getSize() > 0) {
            this.programList.setSelectedIndex(0);
        }
    }

    private void updateDetails() {
        Product p = this.programList.getSelectedValue();
        if (p != null) {
            this.nameLabel.setText(p.getName());
            String htmlDesc = "<html><body style='font-family: Nunito; color: #C8C8C8;'>" + p.getDescription().replace("\n", "<br>") + "</body></html>";
            this.descriptionArea.setText(htmlDesc);
            this.lastUpdateLabel.setText("Ostatnia aktualizacja: " + p.getLastUpdate());
            this.arduinoRequiredLabel.setText("Wymagane HID: " + (p.isHidRequired() ? "Tak" : "Nie"));
        } else {
            this.nameLabel.setText("Wybierz program");
            this.descriptionArea.setText("");
            this.lastUpdateLabel.setText("");
            this.arduinoRequiredLabel.setText("");
        }
    }

    public void updateRunButton() {
        Product p = this.programList.getSelectedValue();
        if (p != null && p.isAuthorized()) {
            if (p.isHidRequired()) {
                HIDDeviceType type = RegistryUtil.getHIDDeviceType();
                boolean enabled = type == HIDDeviceType.KFC ? VirtualHIDUtil.isVirtualHidInstalled() : (type == HIDDeviceType.UBER ? RazerDriverHook.isDriverAvailable() : false);
                this.loadButton.setText(enabled ? "Uruchom" : "Skonfiguruj HID");
                this.loadButton.setEnabled(true);
                this.loadButton.setBackground(enabled ? new Color(0, 122, 204) : new Color(200, 50, 50));
            } else {
                this.loadButton.setText("Uruchom");
                this.loadButton.setEnabled(true);
                this.loadButton.setBackground(new Color(0, 122, 204));
            }
        } else if (p != null && p.isBeta()) {
            this.loadButton.setText("Tylko BETA");
            this.loadButton.setEnabled(false);
            this.loadButton.setBackground(new Color(62, 62, 66));
        } else {
            this.loadButton.setText("Brak dost\u0119pu");
            this.loadButton.setEnabled(false);
            this.loadButton.setBackground(new Color(62, 62, 66));
        }
        this.loadButton.repaint();
    }

    public Product getSelectedProgram() {
        return this.programList.getSelectedValue();
    }

    public void updateProducts(List<Product> newProducts) {
        DefaultListModel model = (DefaultListModel)this.programList.getModel();
        model.clear();
        newProducts.forEach(model::addElement);
        this.restoreLastSelectedProgram();
    }

    @Generated
    public ServerRegistry getRegistry() {
        return this.registry;
    }

    static {
        try (InputStream hidStream = ProgramSelectionPanel.class.getResourceAsStream("/images/usb.svg");){
            ICON_HID = new FlatSVGIcon(hidStream).derive(16, 16);
            ((FlatSVGIcon)ICON_HID).setColorFilter(new FlatSVGIcon.ColorFilter(color -> color.equals(Color.BLACK) ? Color.WHITE : color));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ModernProductRenderer
    extends JPanel
    implements ListCellRenderer<Product> {
        private final JLabel nameLabel;
        private final JLabel statusLabel;
        private final JPanel indicator;
        private boolean isSelected;

        public ModernProductRenderer() {
            this.setLayout(new BorderLayout(10, 0));
            this.setOpaque(false);
            this.setBorder(new EmptyBorder(12, 12, 12, 12));
            this.indicator = new JPanel();
            this.indicator.setPreferredSize(new Dimension(3, 0));
            this.indicator.setOpaque(false);
            this.add((Component)this.indicator, "West");
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            this.nameLabel = new JLabel();
            this.nameLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(14.0f));
            textPanel.add(this.nameLabel);
            this.statusLabel = new JLabel();
            this.statusLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(11.0f));
            textPanel.add(this.statusLabel);
            this.add((Component)textPanel, "Center");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Product> list, Product value, int index, boolean isSelected, boolean cellHasFocus) {
            this.isSelected = isSelected;
            this.nameLabel.setText(value.getName());
            if (value.isAuthorized()) {
                if (value.isBeta()) {
                    this.statusLabel.setText("BETA");
                    this.statusLabel.setForeground(new Color(255, 180, 50));
                } else {
                    this.statusLabel.setText("Dost\u0119pny");
                    this.statusLabel.setForeground(new Color(80, 200, 120));
                }
            } else if (value.isBeta()) {
                this.statusLabel.setText("BETA (Brak dost\u0119pu)");
                this.statusLabel.setForeground(new Color(120, 120, 120));
            } else {
                this.statusLabel.setText("Brak dost\u0119pu");
                this.statusLabel.setForeground(new Color(120, 120, 120));
            }
            if (isSelected) {
                this.nameLabel.setForeground(Color.WHITE);
                this.indicator.setOpaque(true);
                this.indicator.setBackground(new Color(0, 122, 204));
            } else {
                this.nameLabel.setForeground(new Color(200, 200, 200));
                this.indicator.setOpaque(false);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (this.isSelected) {
                g2.setColor(new Color(50, 50, 54));
                g2.fillRoundRect(4, 2, this.getWidth() - 8, this.getHeight() - 4, 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

