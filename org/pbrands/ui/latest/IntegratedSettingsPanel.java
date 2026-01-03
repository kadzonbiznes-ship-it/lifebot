/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.util.PanicUtil
 */
package org.pbrands.ui.latest;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.pbrands.hid.HIDDeviceType;
import org.pbrands.hid.uber.RazerDriverHook;
import org.pbrands.netty.NettyClient;
import org.pbrands.ui.ProgramSelectionPanel;
import org.pbrands.ui.main.LoaderWindow;
import org.pbrands.util.FontUtil;
import org.pbrands.util.LoaderUtil;
import org.pbrands.util.MacUtil;
import org.pbrands.util.PanicUtil;
import org.pbrands.util.RegistryUtil;

public class IntegratedSettingsPanel
extends JPanel {
    private final ProgramSelectionPanel selectionPanel;
    private final String token;
    private final Runnable onBack;
    private JLabel virtualHidStatusLabel;
    private JButton virtualHidActionBtn;
    private JProgressBar virtualHidProgressBar;
    private JLabel razerHidStatusLabel;
    private JButton razerHidActionBtn;
    private JComboBox<HIDDeviceType> activeDriverComboBox;
    private boolean isUpdatingDrivers = false;

    public IntegratedSettingsPanel(ProgramSelectionPanel selectionPanel, String token, Runnable onBack) {
        this.selectionPanel = selectionPanel;
        this.token = token;
        this.onBack = onBack;
        this.setLayout(new BorderLayout());
        this.setBackground(Color.decode("#1E1E1E"));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#1E1E1E"));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JButton backButton = new JButton(){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = this.getBackground();
                if (this.getModel().isPressed()) {
                    bg = bg.darker();
                } else if (this.getModel().isRollover()) {
                    bg = bg.brighter();
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 8, 8);
                g2.setColor(this.getForeground());
                g2.setStroke(new BasicStroke(2.0f, 1, 1));
                int centerY = this.getHeight() / 2;
                int arrowSize = 6;
                int startX = this.getWidth() / 2 + 3;
                g2.drawLine(startX, centerY, startX - 10, centerY);
                g2.drawLine(startX - 10, centerY, startX - 10 + arrowSize, centerY - arrowSize);
                g2.drawLine(startX - 10, centerY, startX - 10 + arrowSize, centerY + arrowSize);
                g2.dispose();
            }
        };
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#3E3E42"));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(Cursor.getPredefinedCursor(12));
        backButton.setPreferredSize(new Dimension(36, 30));
        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
        headerPanel.add((Component)backButton, "West");
        JLabel titleLabel = new JLabel("Ustawienia", 0);
        titleLabel.setFont(FontUtil.NUNITO_BLACK.deriveFont(24.0f));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add((Component)titleLabel, "Center");
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(80, 30));
        spacer.setOpaque(false);
        headerPanel.add((Component)spacer, "East");
        this.add((Component)headerPanel, "North");
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, 1));
        contentPanel.setBackground(Color.decode("#1E1E1E"));
        contentPanel.add(this.createHeaderLabel("Zarz\u0105dzanie Sterownikami"));
        contentPanel.add(Box.createVerticalStrut(5));
        JLabel infoLabel = new JLabel("Zainstaluj jeden z poni\u017cszych sterownik\u00f3w. Mo\u017cesz u\u017cywa\u0107 tylko jednego na raz.");
        infoLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(11.0f));
        infoLabel.setForeground(new Color(140, 140, 140));
        infoLabel.setAlignmentX(0.0f);
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(this.createDriversPanel());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(this.createHeaderLabel("Aktywny Sterownik"));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(this.createActiveDriverPanel());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(this.createFooterPanel());
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(31);
        this.add((Component)scrollPane, "Center");
        this.updateDriverStatuses();
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontUtil.NUNITO_BOLD.deriveFont(16.0f));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(0.0f);
        return label;
    }

    private JPanel createDriversPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(Color.decode("#1E1E1E"));
        panel.setAlignmentX(0.0f);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        panel.setPreferredSize(new Dimension(500, 185));
        JPanel razerPanel = this.createDriverCard("Razer HID", "Oficjalny sterownik Razer", true);
        this.razerHidStatusLabel = new JLabel("Sprawdzanie...");
        this.razerHidStatusLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(12.0f));
        this.razerHidActionBtn = this.createStyledButton("Zainstaluj");
        this.razerHidActionBtn.addActionListener(e -> this.downloadAndInstallRazerDriver());
        this.addComponentsToCard(razerPanel, this.razerHidStatusLabel, this.razerHidActionBtn, null);
        panel.add(razerPanel);
        JPanel virtualPanel = this.createDriverCard("Virtual HID", "Sterownik Interception", false);
        this.virtualHidStatusLabel = new JLabel("Sprawdzanie...");
        this.virtualHidStatusLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(12.0f));
        this.virtualHidActionBtn = this.createStyledButton("Zainstaluj");
        this.virtualHidProgressBar = new JProgressBar();
        this.virtualHidProgressBar.setIndeterminate(true);
        this.virtualHidProgressBar.setVisible(false);
        this.virtualHidProgressBar.setPreferredSize(new Dimension(100, 5));
        this.virtualHidProgressBar.setBackground(Color.decode("#252526"));
        this.virtualHidProgressBar.setForeground(Color.decode("#007ACC"));
        this.virtualHidActionBtn.addActionListener(e -> {
            if (this.virtualHidActionBtn.getText().equals("Zainstaluj")) {
                this.installHidDriver();
            } else {
                this.uninstallHidDriver();
            }
        });
        this.addComponentsToCard(virtualPanel, this.virtualHidStatusLabel, this.virtualHidActionBtn, this.virtualHidProgressBar);
        panel.add(virtualPanel);
        return panel;
    }

    private JPanel createDriverCard(String title, String subtitle, final boolean isRecommended) {
        JPanel card = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#252526"));
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                g2.setColor(isRecommended ? new Color(0, 122, 204) : Color.decode("#3E3E42"));
                g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, 1));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        if (isRecommended) {
            JLabel recommendedLabel = new JLabel("ZALECANE");
            recommendedLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(9.0f));
            recommendedLabel.setForeground(new Color(0, 122, 204));
            recommendedLabel.setAlignmentX(0.5f);
            card.add(recommendedLabel);
            card.add(Box.createVerticalStrut(4));
        }
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontUtil.NUNITO_BOLD.deriveFont(15.0f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(0.5f);
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(11.0f));
        subLabel.setForeground(new Color(150, 150, 150));
        subLabel.setAlignmentX(0.5f);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    private void addComponentsToCard(JPanel card, JLabel statusLabel, JButton actionBtn, JProgressBar progressBar) {
        statusLabel.setAlignmentX(0.5f);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));
        actionBtn.setAlignmentX(0.5f);
        actionBtn.setMaximumSize(new Dimension(140, 36));
        card.add(actionBtn);
        if (progressBar != null) {
            card.add(Box.createVerticalStrut(8));
            progressBar.setAlignmentX(0.5f);
            progressBar.setMaximumSize(new Dimension(120, 4));
            card.add(progressBar);
        }
    }

    private JPanel createActiveDriverPanel() {
        JPanel wrapper = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#252526"));
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
                g2.setColor(Color.decode("#3E3E42"));
                g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        wrapper.setLayout(new FlowLayout(0, 15, 12));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(0.0f);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel label = new JLabel("Wybierz sterownik:");
        label.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        label.setForeground(Color.WHITE);
        this.activeDriverComboBox = new JComboBox();
        this.activeDriverComboBox.setPreferredSize(new Dimension(200, 32));
        this.activeDriverComboBox.setBackground(Color.decode("#333337"));
        this.activeDriverComboBox.setForeground(Color.WHITE);
        this.activeDriverComboBox.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        this.activeDriverComboBox.setBorder(BorderFactory.createLineBorder(Color.decode("#3E3E42"), 1));
        this.activeDriverComboBox.addActionListener(e -> {
            if (this.isUpdatingDrivers) {
                return;
            }
            HIDDeviceType selected = (HIDDeviceType)((Object)((Object)this.activeDriverComboBox.getSelectedItem()));
            if (selected != null) {
                RegistryUtil.setHIDDeviceType(selected);
            }
        });
        wrapper.add(label);
        wrapper.add(this.activeDriverComboBox);
        return wrapper;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(0, 10, 0));
        panel.setBackground(Color.decode("#1E1E1E"));
        panel.setAlignmentX(0.0f);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JButton logsButton = this.createStyledButton("Otw\u00f3rz Logi");
        logsButton.addActionListener(e -> this.openLogs());
        JButton panicButton = this.createStyledButton("Odinstaluj (Panic)");
        panicButton.setBackground(new Color(180, 50, 50));
        panicButton.addActionListener(e -> PanicUtil.handlePanicAction());
        panel.add(logsButton);
        panel.add(panicButton);
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = this.getBackground();
                if (this.getModel().isPressed()) {
                    bg = bg.darker();
                } else if (this.getModel().isRollover()) {
                    bg = bg.brighter();
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 8, 8);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (this.getWidth() - fm.stringWidth(this.getText())) / 2;
                int textY = (this.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(this.getForeground());
                g2.setFont(this.getFont());
                g2.drawString(this.getText(), textX, textY);
                g2.dispose();
            }
        };
        button.setFocusPainted(false);
        button.setBackground(Color.decode("#3E3E42"));
        button.setForeground(Color.WHITE);
        button.setFont(FontUtil.NUNITO_BOLD.deriveFont(12.0f));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(12));
        return button;
    }

    private void updateDriverStatuses() {
        if (MacUtil.isMacOs()) {
            return;
        }
        boolean razerAvailable = RazerDriverHook.isDriverAvailable();
        if (razerAvailable) {
            this.razerHidStatusLabel.setText("Zainstalowany");
            this.razerHidStatusLabel.setForeground(new Color(80, 200, 120));
            this.razerHidActionBtn.setEnabled(true);
            this.razerHidActionBtn.setText("Przeinstaluj");
        } else {
            this.razerHidStatusLabel.setText("Brak sterownika");
            this.razerHidStatusLabel.setForeground(new Color(200, 80, 80));
            this.razerHidActionBtn.setEnabled(true);
            this.razerHidActionBtn.setText("Zainstaluj");
        }
        boolean virtualAvailable = LoaderUtil.KFC_INSTANCE.kfc_responsive().booleanValue();
        if (virtualAvailable) {
            this.virtualHidStatusLabel.setText("Zainstalowany");
            this.virtualHidStatusLabel.setForeground(new Color(80, 200, 120));
            this.virtualHidActionBtn.setText("Odinstaluj");
        } else {
            this.virtualHidStatusLabel.setText("Brak sterownika");
            this.virtualHidStatusLabel.setForeground(new Color(200, 80, 80));
            this.virtualHidActionBtn.setText("Zainstaluj");
        }
        this.updateActiveDriverComboBox(razerAvailable, virtualAvailable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateActiveDriverComboBox(boolean razerAvailable, boolean virtualAvailable) {
        this.isUpdatingDrivers = true;
        try {
            int i;
            HIDDeviceType saved = RegistryUtil.getHIDDeviceType();
            HIDDeviceType currentSelection = (HIDDeviceType)((Object)this.activeDriverComboBox.getSelectedItem());
            this.activeDriverComboBox.removeAllItems();
            if (virtualAvailable) {
                this.activeDriverComboBox.addItem(HIDDeviceType.KFC);
            }
            if (razerAvailable) {
                this.activeDriverComboBox.addItem(HIDDeviceType.UBER);
            }
            if (currentSelection != null) {
                for (i = 0; i < this.activeDriverComboBox.getItemCount(); ++i) {
                    if (this.activeDriverComboBox.getItemAt(i) != currentSelection) continue;
                    this.activeDriverComboBox.setSelectedItem((Object)currentSelection);
                    return;
                }
            }
            for (i = 0; i < this.activeDriverComboBox.getItemCount(); ++i) {
                if (this.activeDriverComboBox.getItemAt(i) != saved) continue;
                this.activeDriverComboBox.setSelectedItem((Object)saved);
                return;
            }
        }
        finally {
            this.isUpdatingDrivers = false;
        }
    }

    private void downloadAndInstallRazerDriver() {
        this.razerHidActionBtn.setEnabled(false);
        this.razerHidStatusLabel.setText("Pobieranie...");
        this.razerHidStatusLabel.setForeground(Color.YELLOW);
        new Thread(() -> {
            File installer = null;
            try {
                installer = this.downloadRazerSynapseInstaller();
                SwingUtilities.invokeLater(() -> this.razerHidStatusLabel.setText("Uruchamianie instalatora..."));
                ProcessBuilder pb = new ProcessBuilder(installer.getAbsolutePath());
                pb.start();
                SwingUtilities.invokeLater(() -> {
                    this.razerHidStatusLabel.setText("Instalator uruchomiony");
                    this.razerHidStatusLabel.setForeground(Color.ORANGE);
                    this.razerHidActionBtn.setText("Sprawd\u017a ponownie");
                    this.razerHidActionBtn.setEnabled(true);
                    for (ActionListener listener : this.razerHidActionBtn.getActionListeners()) {
                        this.razerHidActionBtn.removeActionListener(listener);
                    }
                    this.razerHidActionBtn.addActionListener(e -> {
                        this.updateDriverStatuses();
                        for (ActionListener l : this.razerHidActionBtn.getActionListeners()) {
                            this.razerHidActionBtn.removeActionListener(l);
                        }
                        this.razerHidActionBtn.addActionListener(ev -> this.downloadAndInstallRazerDriver());
                    });
                    JOptionPane.showMessageDialog(this, "Instalator Razer Synapse zosta\u0142 uruchomiony.\nPo zako\u0144czeniu instalacji kliknij 'Sprawd\u017a ponownie'.", "Instalacja", 1);
                });
            }
            catch (Exception ex) {
                ex.printStackTrace();
                File finalInstaller = installer;
                SwingUtilities.invokeLater(() -> {
                    this.razerHidStatusLabel.setText("B\u0142\u0105d pobierania");
                    this.razerHidStatusLabel.setForeground(Color.RED);
                    this.razerHidActionBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "B\u0142\u0105d: " + ex.getMessage(), "B\u0142\u0105d", 0);
                    if (finalInstaller != null) {
                        finalInstaller.delete();
                    }
                });
            }
        }).start();
    }

    private File downloadRazerSynapseInstaller() throws IOException {
        String downloadUrl = "https://cdn.razersynapse.com/16028372057849nNqJRazerNanoleafSetup_v3.5.1030.101616.exe";
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File installer = new File(tempDir, "RazerSynapseInstaller.exe");
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        int status = connection.getResponseCode();
        if (status == 302 || status == 301 || status == 303) {
            String newUrl = connection.getHeaderField("Location");
            connection = (HttpURLConnection)new URL(newUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        }
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(installer);){
            in.transferTo(out);
        }
        return installer;
    }

    private void installHidDriver() {
        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz zainstalowa\u0107 Interception?", "Potwierdzenie", 0);
        if (confirm != 0) {
            return;
        }
        this.virtualHidActionBtn.setEnabled(false);
        this.virtualHidProgressBar.setVisible(true);
        this.virtualHidStatusLabel.setText("Pobieranie...");
        new Thread(() -> {
            File installer = null;
            try {
                installer = this.downloadDriverInstallerViaSocket();
                SwingUtilities.invokeLater(() -> this.virtualHidStatusLabel.setText("Instalowanie..."));
                this.executeInstaller(installer, "/install");
                Thread.sleep(2000L);
                SwingUtilities.invokeLater(() -> {
                    this.updateDriverStatuses();
                    this.virtualHidActionBtn.setEnabled(true);
                    this.virtualHidProgressBar.setVisible(false);
                    JOptionPane.showMessageDialog(this, "Zainstalowano pomy\u015blnie. Zalecany restart.", "Sukces", 1);
                });
            }
            catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    this.virtualHidStatusLabel.setText("B\u0142\u0105d");
                    this.virtualHidActionBtn.setEnabled(true);
                    this.virtualHidProgressBar.setVisible(false);
                    JOptionPane.showMessageDialog(this, "B\u0142\u0105d: " + ex.getMessage(), "B\u0142\u0105d", 0);
                });
            }
            finally {
                if (installer != null) {
                    installer.delete();
                }
            }
        }).start();
    }

    private void uninstallHidDriver() {
        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz odinstalowa\u0107 Interception?", "Potwierdzenie", 0);
        if (confirm != 0) {
            return;
        }
        this.virtualHidActionBtn.setEnabled(false);
        this.virtualHidProgressBar.setVisible(true);
        this.virtualHidStatusLabel.setText("Usuwanie...");
        new Thread(() -> {
            File installer = null;
            try {
                installer = this.downloadDriverInstallerViaSocket();
                this.executeInstaller(installer, "/uninstall");
                Thread.sleep(2000L);
                SwingUtilities.invokeLater(() -> {
                    this.updateDriverStatuses();
                    this.virtualHidActionBtn.setEnabled(true);
                    this.virtualHidProgressBar.setVisible(false);
                    int restart = JOptionPane.showConfirmDialog(this, "Odinstalowano pomy\u015blnie. Wymagany jest restart komputera, aby doko\u0144czy\u0107 proces.\nCzy chcesz zrestartowa\u0107 komputer teraz?", "Wymagany Restart", 0, 2);
                    if (restart == 0) {
                        this.restartComputer();
                    }
                });
            }
            catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    this.virtualHidStatusLabel.setText("B\u0142\u0105d");
                    this.virtualHidActionBtn.setEnabled(true);
                    this.virtualHidProgressBar.setVisible(false);
                });
            }
            finally {
                if (installer != null) {
                    installer.delete();
                }
            }
        }).start();
    }

    private File downloadDriverInstallerViaSocket() throws Exception {
        NettyClient client = LoaderWindow.instance.getNettyClient();
        if (client == null || !client.isConnected()) {
            throw new IOException("Brak po\u0142\u0105czenia z serwerem (Netty)");
        }
        byte[] fileBytes = (byte[])client.requestInterceptionInstaller().get();
        File tempInstallerFile = File.createTempFile("virtualhid_installer_", ".exe");
        tempInstallerFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempInstallerFile);){
            fos.write(fileBytes);
        }
        return tempInstallerFile;
    }

    private void executeInstaller(File installer, String parameter) throws IOException, InterruptedException {
        ProcessBuilder pb;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String installerPath = installer.getAbsolutePath().replace("\"", "\\\"");
            String command = String.format("Start-Process \"%s\" -ArgumentList \"%s\" -Verb RunAs -WindowStyle Hidden", installerPath, parameter);
            pb = new ProcessBuilder("powershell.exe", "-WindowStyle", "Hidden", "-Command", command);
        } else {
            pb = new ProcessBuilder("sudo", installer.getAbsolutePath(), parameter);
        }
        pb.start().waitFor();
    }

    private void restartComputer() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("shutdown", "/r", "/t", "0").start();
            } else {
                new ProcessBuilder("sudo", "shutdown", "-r", "now").start();
            }
            System.exit(0);
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Nie uda\u0142o si\u0119 zrestartowa\u0107 komputera automatycznie.\nProsz\u0119 zrestartowa\u0107 r\u0119cznie.", "B\u0142\u0105d", 0);
        }
    }

    private void openLogs() {
        try {
            File logFile = RegistryUtil.getLogsDir().resolve("latest.log").toFile();
            if (logFile.exists()) {
                Desktop.getDesktop().open(logFile);
            } else {
                JOptionPane.showMessageDialog(this, "Brak log\u00f3w.", "Info", 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

