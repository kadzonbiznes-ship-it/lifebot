/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.settings;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import lombok.Generated;
import org.pbrands.Startup;
import org.pbrands.logic.Application;
import org.pbrands.settings.Settings;
import org.pbrands.ui.AppUI;
import org.pbrands.util.FontUtil;
import org.pbrands.util.ImageIconInverter;

public class SettingsWindow
implements NativeKeyListener {
    private static final int INITIAL_FRAME_WIDTH = 450;
    private static final int INITIAL_FRAME_HEIGHT = 420;
    private static final int FRAME_CORNER_RADIUS = 20;
    private static final int LABEL_FONT_SIZE = 13;
    private static final String SETTINGS_TITLE = "USTAWIENIA";
    private static final int DEFAULT_PING = 50;
    private static final int DEFAULT_FISHING_BEGIN_DELAY = 1700;
    private static final int DEFAULT_FISHING_BEGIN_RANDOMIZER = 200;
    private static final int DEFAULT_LOOSE_DELAY = 115;
    private static final int DEFAULT_LOOSE_DELAY_RANDOMIZER = 100;
    private static final int DEFAULT_FISHING_START_DELAY = 750;
    private static final int DEFAULT_FISHING_START_DELAY_RANDOMIZER = 400;
    private static final String CONFIG_FILE_PATH = "config.json";
    private final JFrame frame;
    private JSlider fishingStartDelaySlider;
    private JSlider fishingStartDelayRandomizerSlider;
    private JLabel fishingStartDelayLabel;
    private JSlider delaySlider;
    private JLabel delayLabel;
    private JSlider randomizerSlider;
    private JSlider fishingBeginDelaySlider;
    private JSlider fishingBeginRandomizerSlider;
    private JLabel fishingBeginDelayLabel;
    private JTextField overlayKeyField;
    private JTextField fishingToggleKeyField;
    private boolean isOverlayKeyCapturing = false;
    private boolean isFishingToggleKeyCapturing = false;
    private final Application application;
    private boolean enabled;
    private Point initialClick;
    private volatile boolean configLoaded;

    public SettingsWindow(Application application) {
        this.application = application;
        this.frame = this.createFrame();
        JPanel contentPanel = this.createContentPanel();
        this.frame.add(contentPanel);
        JTabbedPane settingsPanel = this.createSettingsPanel();
        contentPanel.add((Component)settingsPanel, "Center");
        this.setupEventHandlers();
        this.frame.setAlwaysOnTop(true);
        this.frame.setFocusable(false);
        this.frame.pack();
        this.frame.setFocusableWindowState(false);
        this.updateFrameShape();
        try {
            this.loadSettings(CONFIG_FILE_PATH);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVisible(boolean visible) {
        this.frame.setSize(450, 420);
        this.frame.pack();
        JFrame main = this.application.getAppUI().getFrame();
        this.frame.setLocation(main.getLocation().x + main.getWidth() + 5, main.getLocation().y);
        this.frame.setVisible(visible);
    }

    public boolean isVisible() {
        return this.frame.isVisible();
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame(SETTINGS_TITLE);
        frame.setUndecorated(true);
        frame.setSize(450, 420);
        frame.setDefaultCloseOperation(2);
        return frame;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(43, 43, 43));
        contentPanel.setLayout(new BorderLayout());
        this.addDragFunctionalityToPanel(contentPanel);
        return contentPanel;
    }

    private JTabbedPane createSettingsPanel() {
        int iconWidth = 25;
        JTabbedPane settingsPanel = new JTabbedPane(2);
        settingsPanel.setOpaque(false);
        ImageIcon pickaxeIcon = new ImageIcon(this.getClass().getResource("/images/fishing-rod.png"));
        pickaxeIcon = ImageIconInverter.invertImageIcon(pickaxeIcon);
        int logoHeight = pickaxeIcon.getIconHeight() * iconWidth / pickaxeIcon.getIconWidth();
        Image scaledImage = pickaxeIcon.getImage().getScaledInstance(iconWidth, logoHeight, 4);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JPanel fishingPanel = this.createFishingSettingsPanel();
        settingsPanel.addTab(null, fishingPanel);
        settingsPanel.setTabComponentAt(0, this.createCustomTabHeader("", scaledIcon, new Color(16729344)));
        ImageIcon miscIcon = new ImageIcon(this.getClass().getResource("/images/misc.png"));
        miscIcon = ImageIconInverter.invertImageIcon(miscIcon);
        logoHeight = miscIcon.getIconHeight() * iconWidth / miscIcon.getIconWidth();
        Image miscImage = miscIcon.getImage().getScaledInstance(iconWidth, logoHeight, 4);
        ImageIcon scaledMiscIcon = new ImageIcon(miscImage);
        JPanel additionalPanel = this.createAdditionalFeaturesPanel();
        settingsPanel.addTab(null, additionalPanel);
        settingsPanel.setTabComponentAt(1, this.createCustomTabHeader("", scaledMiscIcon, new Color(16766720)));
        settingsPanel.setFont(FontUtil.NUNITO_BOLD);
        return settingsPanel;
    }

    private JPanel createCustomTabHeader(String title, Icon icon, Color borderColor) {
        JPanel tabHeader = new JPanel(new FlowLayout(0, 10, 5));
        tabHeader.setOpaque(false);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JLabel label = new JLabel(title, icon, 2);
        label.setFont(label.getFont().deriveFont(1, 14.0f));
        label.setForeground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        panel.add(label);
        tabHeader.add(panel);
        tabHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
        return tabHeader;
    }

    private JPanel createJPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createJPanel() {
        return this.createJPanel(new FlowLayout());
    }

    private JPanel createFishingSettingsPanel() {
        JPanel mainPanel = this.createJPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        GridBagConstraints gbc = this.createGridBagConstraints();
        this.fishingBeginDelayLabel = this.createLabel(this.getFishingBeginDelayLabelText(1700, 200), null, 13.0f);
        JPanel fishingBeginDelayLabelSetting = this.createLabeledSetting(this.fishingBeginDelayLabel, "");
        mainPanel.add((Component)fishingBeginDelayLabelSetting, gbc);
        JPanel fishingBeginRow1 = this.createJPanel(new FlowLayout());
        fishingBeginRow1.add(new JLabel("Bazowe op\u00f3\u017anienie: "));
        this.fishingBeginDelaySlider = this.createSlider(1650, 2000, 1700, e -> this.updateFishingBeginDelayLabel());
        fishingBeginRow1.add(this.fishingBeginDelaySlider);
        mainPanel.add((Component)fishingBeginRow1, gbc);
        JPanel fishingBeginRow3 = this.createJPanel(new FlowLayout());
        fishingBeginRow3.add(new JLabel("Losowo\u015b\u0107 op\u00f3\u017anienia: "));
        this.fishingBeginRandomizerSlider = this.createSlider(0, 300, 200, e -> this.updateFishingBeginDelayLabel());
        fishingBeginRow3.add(this.fishingBeginRandomizerSlider);
        mainPanel.add((Component)fishingBeginRow3, gbc);
        mainPanel.add((Component)this.createSeparator(), gbc);
        this.delayLabel = this.createLabel(this.getDelayLabelText(115, 100), null, 13.0f);
        JPanel delayLabelSetting = this.createLabeledSetting(this.delayLabel, "");
        mainPanel.add((Component)delayLabelSetting, gbc);
        JPanel delayPanel = this.createJPanel();
        delayPanel.setLayout(new FlowLayout());
        delayPanel.add(new JLabel("Bazowe op\u00f3\u017anienie: "));
        this.delaySlider = this.createSlider(50, 200, 115, e -> this.updateDelayLabel());
        delayPanel.add(this.delaySlider);
        mainPanel.add((Component)delayPanel, gbc);
        JPanel randomizerPanel = this.createJPanel();
        randomizerPanel.setLayout(new FlowLayout());
        randomizerPanel.add(new JLabel("Losowo\u015b\u0107 op\u00f3\u017anienia: "));
        this.randomizerSlider = this.createSlider(0, 200, 100, e -> this.updateDelayLabel());
        randomizerPanel.add(this.randomizerSlider);
        mainPanel.add((Component)randomizerPanel, gbc);
        mainPanel.add((Component)this.createSeparator(), gbc);
        this.fishingStartDelayLabel = this.createLabel(this.getFishingStartDelayLabelText(750, 400), null, 13.0f);
        JPanel fishingStartDelayLabelSetting = this.createLabeledSetting(this.fishingStartDelayLabel, "");
        mainPanel.add((Component)fishingStartDelayLabelSetting, gbc);
        JPanel fishingStartDelayPanel = this.createJPanel();
        fishingStartDelayPanel.setLayout(new FlowLayout());
        fishingStartDelayPanel.add(new JLabel("Bazowe op\u00f3\u017anienie: "));
        this.fishingStartDelaySlider = this.createSlider(500, 1500, 750, e -> this.updateDigStartDelayLabel());
        fishingStartDelayPanel.add(this.fishingStartDelaySlider);
        mainPanel.add((Component)fishingStartDelayPanel, gbc);
        JPanel digStartRandomizerPanel = this.createJPanel();
        digStartRandomizerPanel.setLayout(new FlowLayout());
        digStartRandomizerPanel.add(new JLabel("Losowo\u015b\u0107 op\u00f3\u017anienia: "));
        this.fishingStartDelayRandomizerSlider = this.createSlider(0, 1000, 115, e -> this.updateDigStartDelayLabel());
        digStartRandomizerPanel.add(this.fishingStartDelayRandomizerSlider);
        mainPanel.add((Component)digStartRandomizerPanel, gbc);
        mainPanel.setPreferredSize(new Dimension(450, 420));
        return mainPanel;
    }

    private JPanel createAdditionalFeaturesPanel() {
        JPanel mainPanel = this.createJPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        GridBagConstraints gbc = this.createGridBagConstraints();
        JPanel overlayKeyPanel = this.createJPanel(new FlowLayout(0));
        overlayKeyPanel.add(new JLabel("<html><b>Nak\u0142adka: </b></html>: "));
        this.overlayKeyField = this.createKeyBindingField("overlay");
        overlayKeyPanel.add(this.overlayKeyField);
        mainPanel.add((Component)overlayKeyPanel, gbc);
        JPanel digToggleKeyPanel = this.createJPanel(new FlowLayout(0));
        digToggleKeyPanel.add(new JLabel("<html><b>\u0141owienie: </b></html>: "));
        this.fishingToggleKeyField = this.createKeyBindingField("fishingToggle");
        digToggleKeyPanel.add(this.fishingToggleKeyField);
        mainPanel.add((Component)digToggleKeyPanel, gbc);
        mainPanel.setPreferredSize(new Dimension(450, 420));
        return mainPanel;
    }

    private void saveSettings() {
        if (!this.configLoaded) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                this.saveSettings(CONFIG_FILE_PATH);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (this.isOverlayKeyCapturing) {
            this.overlayKeyField.setText(NativeKeyEvent.getKeyText(e.getKeyCode()));
            this.isOverlayKeyCapturing = false;
            this.saveSettings();
        } else if (this.isFishingToggleKeyCapturing) {
            this.fishingToggleKeyField.setText(NativeKeyEvent.getKeyText(e.getKeyCode()));
            this.isFishingToggleKeyCapturing = false;
            this.saveSettings();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    private void setupEventHandlers() {
        Runnable saveSettings = () -> {
            if (!this.configLoaded) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    this.saveSettings(CONFIG_FILE_PATH);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        };
        ChangeListener saveSettingsListener = e -> saveSettings.run();
        this.fishingStartDelaySlider.addChangeListener(saveSettingsListener);
        this.fishingStartDelayRandomizerSlider.addChangeListener(saveSettingsListener);
        this.delaySlider.addChangeListener(saveSettingsListener);
        this.randomizerSlider.addChangeListener(saveSettingsListener);
        this.fishingBeginDelaySlider.addChangeListener(saveSettingsListener);
        this.fishingBeginRandomizerSlider.addChangeListener(saveSettingsListener);
    }

    private JSlider createSlider(int min, int max, int value, ChangeListener changeListener) {
        JSlider slider = new JSlider(0, min, max, value);
        slider.setBorder(BorderFactory.createEmptyBorder());
        slider.addChangeListener(changeListener);
        return slider;
    }

    private JTextField createKeyBindingField(final String fieldName) {
        final JTextField keyField = new JTextField(10);
        keyField.setEditable(false);
        keyField.setFont(new Font("Cascadia Code", 1, 13));
        keyField.setText("Click to set key");
        keyField.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                keyField.setText("Press any key...");
                if (fieldName.equals("overlay")) {
                    SettingsWindow.this.isOverlayKeyCapturing = true;
                } else if (fieldName.equals("fishingToggle")) {
                    SettingsWindow.this.isFishingToggleKeyCapturing = true;
                }
            }
        });
        return keyField;
    }

    private void addDragFunctionalityToPanel(JPanel panel) {
        panel.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                SettingsWindow.this.initialClick = e.getPoint();
                SettingsWindow.this.frame.getComponentAt(SettingsWindow.this.initialClick);
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
                AppUI appUI = SettingsWindow.this.application.getAppUI();
                JFrame appFrame = appUI.getFrame();
                int xMoved = e.getX() - SettingsWindow.this.initialClick.x;
                int yMoved = e.getY() - SettingsWindow.this.initialClick.y;
                int x = SettingsWindow.this.frame.getLocation().x + xMoved;
                int y = SettingsWindow.this.frame.getLocation().y + yMoved;
                SettingsWindow.this.frame.setLocation(x, y);
                appFrame.setLocation(x - SettingsWindow.this.application.getAppUI().getFrame().getWidth() - 5, y);
            }
        });
    }

    private void updateDelayLabel() {
        int delayValue = this.delaySlider.getValue();
        int randomizerValue = this.randomizerSlider.getValue();
        this.delayLabel.setText(this.getDelayLabelText(delayValue, randomizerValue));
    }

    private void updateDigStartDelayLabel() {
        int delayValue = this.fishingStartDelaySlider.getValue();
        int randomizerValue = this.fishingStartDelayRandomizerSlider.getValue();
        this.fishingStartDelayLabel.setText(this.getFishingStartDelayLabelText(delayValue, randomizerValue));
    }

    private void updateFrameShape() {
        this.frame.setShape(new RoundRectangle2D.Double(0.0, 0.0, this.frame.getWidth(), this.frame.getHeight(), 20.0, 20.0));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.frame.setVisible(enabled);
    }

    public int getFishingStartDelay() {
        return this.fishingStartDelaySlider.getValue();
    }

    public int getFishingStartRandomizerDelay() {
        return this.fishingStartDelayRandomizerSlider.getValue();
    }

    public int getLooseDelay() {
        return this.delaySlider.getValue();
    }

    public int getLooseDelayRandomizer() {
        return this.randomizerSlider.getValue();
    }

    private JLabel createLabel(String text, Border border, float fontSize) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(2);
        label.setFont(FontUtil.NUNITO_BLACK.deriveFont(fontSize));
        label.setBorder(border);
        return label;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = 17;
        gbc.weightx = 1.0;
        return gbc;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(0);
        separator.setOpaque(false);
        separator.setForeground(Color.GRAY);
        separator.setPreferredSize(new Dimension(350, 3));
        return separator;
    }

    private String getDelayLabelText(int delay, int randomizer) {
        String randomizerText = randomizer > 0 ? String.format(" <span style='color:gray;'>+ (0-%d)</span>", randomizer) : "";
        return String.format("<html>Anty-zerwanie \u017cy\u0142ki: <b>%d</b>%s ms</html>", delay, randomizerText);
    }

    private String getFishingStartDelayLabelText(int delay, int randomizer) {
        String randomizerText = randomizer > 0 ? String.format(" <span style='color:gray;'>+ (0-%d)</span>", randomizer) : "";
        return String.format("<html>Czas oczekiwania po \u0142owieniu: <b>%d</b>%s ms</html>", delay, randomizerText);
    }

    private String getFishingBeginDelayLabelText(int delay, int randomizer) {
        String randomizerText = randomizer > 0 ? String.format(" + <span style='color:gray;'>(0-%d)</span>", randomizer) : "";
        return String.format("<html>Rozpocz\u0119cie \u0142owienia: <b>%d</b>%s ms</html>", delay, randomizerText);
    }

    private void updateFishingBeginDelayLabel() {
        int delayValue = this.fishingBeginDelaySlider.getValue();
        int randomizerValue = this.fishingBeginRandomizerSlider.getValue();
        this.fishingBeginDelayLabel.setText(this.getFishingBeginDelayLabelText(delayValue, randomizerValue));
    }

    private JPanel createLabeledSetting(JLabel label, final String infoText) {
        JPanel panel = new JPanel(new FlowLayout(0));
        panel.setOpaque(false);
        ImageIcon infoIcon = new ImageIcon(this.getClass().getResource("/images/info.png"));
        final JLabel infoLabel = new JLabel(infoIcon);
        infoLabel.setCursor(Cursor.getPredefinedCursor(12));
        infoLabel.addMouseListener(new MouseAdapter(this){

            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                String htmlContent = "<html><head><style type='text/css'>body {  font-family: 'Segoe UI', Arial, sans-serif;  font-size: 10px;  margin: 0;  padding: 10px;}</style></head><body>" + infoText.replace("\n", "<br>") + "</body></html>";
                JLabel popupLabel = new JLabel(htmlContent);
                popup.add(popupLabel);
                popup.show(infoLabel, e.getX(), e.getY());
            }
        });
        panel.add(label);
        panel.add(infoLabel);
        return panel;
    }

    public int getOverlayKeyCode() {
        String keyText = this.overlayKeyField.getText();
        return this.getKeyCodeFromText(keyText);
    }

    private void setOverlayKeyCode(int keyCode) {
        this.overlayKeyField.setText(NativeKeyEvent.getKeyText(keyCode));
    }

    public int getFishingToggleKeyCode() {
        String keyText = this.fishingToggleKeyField.getText();
        return this.getKeyCodeFromText(keyText);
    }

    private void setDigToggleKeyCode(int keyCode) {
        this.fishingToggleKeyField.setText(NativeKeyEvent.getKeyText(keyCode));
    }

    private int getKeyCodeFromText(String keyText) {
        if (keyText == null || keyText.isEmpty() || keyText.equals("Press a key...") || keyText.equals("Click to set key") || keyText.equals("Press any key...")) {
            return -1;
        }
        for (int keyCode = 0; keyCode <= 65535; ++keyCode) {
            String text = NativeKeyEvent.getKeyText(keyCode);
            if (!keyText.equalsIgnoreCase(text)) continue;
            return keyCode;
        }
        return -1;
    }

    public Settings getSettings() {
        Settings settings = new Settings();
        settings.setFishingStartDelay(this.getFishingStartDelay());
        settings.setFishingStartDelayRandomizer(this.getFishingStartRandomizerDelay());
        settings.setDelay(this.getLooseDelay());
        settings.setRandomizer(this.getLooseDelayRandomizer());
        settings.setFishingBeginDelay(this.fishingBeginDelaySlider.getValue());
        settings.setFishingBeginRandomizer(this.fishingBeginRandomizerSlider.getValue());
        settings.setOverlayKeyCode(this.getOverlayKeyCode());
        settings.setToggleKeyCode(this.getFishingToggleKeyCode());
        return settings;
    }

    public void saveSettings(String filePath) throws IOException {
        Settings settings = this.getSettings();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File programDir = new File(Startup.DATA_FOLDER, this.application.getStartupParams().folderName);
        if (!programDir.exists()) {
            programDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(new File(programDir, filePath));){
            gson.toJson((Object)settings, (Appendable)writer);
        }
    }

    public void loadSettings(String filePath) throws IOException {
        File programDir = new File(Startup.DATA_FOLDER, this.application.getStartupParams().folderName);
        File configFile = new File(programDir, filePath);
        if (!configFile.exists()) {
            Settings defaultSettings = this.createDefaultSettings();
            this.saveSettings(filePath);
            this.setSettings(defaultSettings);
        } else {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile);){
                Settings settings = gson.fromJson((Reader)reader, Settings.class);
                this.setSettings(settings);
            }
        }
        this.configLoaded = true;
    }

    private void setSettings(Settings settings) {
        this.fishingStartDelaySlider.setValue(settings.getFishingStartDelay());
        this.fishingStartDelayRandomizerSlider.setValue(settings.getFishingStartDelayRandomizer());
        this.delaySlider.setValue(settings.getDelay());
        this.randomizerSlider.setValue(settings.getRandomizer());
        this.fishingBeginDelaySlider.setValue(settings.getFishingBeginDelay());
        this.fishingBeginRandomizerSlider.setValue(settings.getFishingBeginRandomizer());
        this.setOverlayKeyCode(settings.getOverlayKeyCode());
        this.setDigToggleKeyCode(settings.getToggleKeyCode());
    }

    private Settings createDefaultSettings() {
        Settings settings = new Settings();
        settings.setPing(50);
        settings.setFishingStartDelay(750);
        settings.setFishingStartDelayRandomizer(400);
        settings.setDelay(115);
        settings.setRandomizer(100);
        settings.setFishingBeginDelay(1700);
        settings.setFishingBeginRandomizer(200);
        settings.setOverlayKeyCode(3666);
        settings.setToggleKeyCode(3663);
        return settings;
    }

    @Generated
    public JFrame getFrame() {
        return this.frame;
    }

    @Generated
    public JSlider getFishingBeginDelaySlider() {
        return this.fishingBeginDelaySlider;
    }

    @Generated
    public JSlider getFishingBeginRandomizerSlider() {
        return this.fishingBeginRandomizerSlider;
    }

    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }
}

