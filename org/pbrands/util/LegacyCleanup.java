/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyCleanup {
    private static final Logger logger = LoggerFactory.getLogger(LegacyCleanup.class);
    private static final Path LEGACY_DIR = Path.of(System.getProperty("user.home"), ".lifebot");
    private static final Path LEGACY_MARKER = LEGACY_DIR.resolve("loader.properties");

    public static void checkAndPromptCleanup() {
        if (!LegacyCleanup.hasLegacyFiles()) {
            logger.info("No legacy LifeBot files found");
            return;
        }
        logger.info("Legacy LifeBot files detected at: {}", (Object)LEGACY_DIR);
        try {
            SwingUtilities.invokeAndWait(LegacyCleanup::showCleanupDialog);
        }
        catch (Exception e) {
            logger.error("Error showing cleanup dialog", e);
        }
    }

    public static boolean hasLegacyFiles() {
        return Files.exists(LEGACY_MARKER, new LinkOption[0]) || Files.exists(LEGACY_DIR, new LinkOption[0]);
    }

    private static void showCleanupDialog() {
        AtomicInteger result = new AtomicInteger(-1);
        JDialog dialog = new JDialog((Frame)null, "LifeBot - Czyszczenie starych plik\u00f3w", true);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(2);
        dialog.setResizable(false);
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(40, 44, 52));
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        iconLabel.setVerticalAlignment(1);
        mainPanel.add((Component)iconLabel, "West");
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, 1));
        contentPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Znaleziono stare pliki LifeBot");
        titleLabel.setFont(new Font("Segoe UI", 1, 16));
        titleLabel.setForeground(new Color(255, 193, 7));
        titleLabel.setAlignmentX(0.0f);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(12));
        JLabel msgLabel = new JLabel("<html><div style='width: 300px; color: #E0E0E0;'>Wykryto pliki z poprzedniej wersji LifeBot w lokalizacji:<br><br><code style='color: #81D4FA; background: #263238; padding: 4px;'>" + String.valueOf(LEGACY_DIR) + "</code><br><br>Te pliki nie s\u0105 ju\u017c u\u017cywane przez now\u0105 wersj\u0119 programu i zajmuj\u0105 miejsce na dysku.<br><br><b>Zalecamy ich usuni\u0119cie.</b></div></html>");
        msgLabel.setFont(new Font("Segoe UI", 0, 13));
        msgLabel.setAlignmentX(0.0f);
        contentPanel.add(msgLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        try {
            long totalSize = Files.walk(LEGACY_DIR, new FileVisitOption[0]).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).mapToLong(p -> {
                try {
                    return Files.size(p);
                }
                catch (Exception e) {
                    return 0L;
                }
            }).sum();
            long fileCount = Files.walk(LEGACY_DIR, new FileVisitOption[0]).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).count();
            String sizeStr = LegacyCleanup.formatSize(totalSize);
            JLabel sizeLabel = new JLabel(String.format("Plik\u00f3w: %d, \u0142\u0105cznie %s", fileCount, sizeStr));
            sizeLabel.setFont(new Font("Segoe UI", 2, 12));
            sizeLabel.setForeground(new Color(158, 158, 158));
            sizeLabel.setAlignmentX(0.0f);
            contentPanel.add(sizeLabel);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        catch (Exception totalSize) {
            // empty catch block
        }
        mainPanel.add((Component)contentPanel, "Center");
        JPanel buttonPanel = new JPanel(new FlowLayout(2, 10, 0));
        buttonPanel.setOpaque(false);
        JButton noButton = LegacyCleanup.createStyledButton("Zostaw", new Color(97, 97, 97), Color.WHITE);
        noButton.addActionListener(e -> {
            result.set(1);
            dialog.dispose();
        });
        JButton yesButton = LegacyCleanup.createStyledButton("Usu\u0144 pliki", new Color(76, 175, 80), Color.WHITE);
        yesButton.addActionListener(e -> {
            result.set(0);
            dialog.dispose();
        });
        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);
        mainPanel.add((Component)buttonPanel, "South");
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.addWindowListener((WindowListener)new /* Unavailable Anonymous Inner Class!! */);
        dialog.addWindowListener((WindowListener)new /* Unavailable Anonymous Inner Class!! */);
        dialog.setVisible(true);
        if (result.get() == 0) {
            LegacyCleanup.deleteLegacyFiles();
        } else {
            logger.info("User chose to keep legacy files");
        }
    }

    private static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", 1, 12));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bgColor.darker(), 1), BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(12));
        button.addMouseListener((MouseListener)new /* Unavailable Anonymous Inner Class!! */);
        return button;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        if (bytes < 0x100000L) {
            return String.format("%.1f KB", (double)bytes / 1024.0);
        }
        if (bytes < 0x40000000L) {
            return String.format("%.1f MB", (double)bytes / 1048576.0);
        }
        return String.format("%.1f GB", (double)bytes / 1.073741824E9);
    }

    private static void deleteLegacyFiles() {
        try {
            if (!Files.exists(LEGACY_DIR, new LinkOption[0])) {
                logger.info("Legacy directory already removed");
                return;
            }
            long fileCount = Files.walk(LEGACY_DIR, new FileVisitOption[0]).count();
            logger.info("Deleting {} files/directories from legacy location", (Object)fileCount);
            Files.walk(LEGACY_DIR, new FileVisitOption[0]).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            if (!Files.exists(LEGACY_DIR, new LinkOption[0])) {
                logger.info("Successfully deleted legacy LifeBot directory");
                LegacyCleanup.showSuccessDialog("Stare pliki zosta\u0142y pomy\u015blnie usuni\u0119te.");
            } else {
                logger.warn("Some legacy files could not be deleted");
                LegacyCleanup.showWarningDialog("Niekt\u00f3re pliki nie mog\u0142y zosta\u0107 usuni\u0119te.\n\nMo\u017cesz je usun\u0105\u0107 r\u0119cznie:\n" + String.valueOf(LEGACY_DIR));
            }
        }
        catch (Exception e) {
            logger.error("Error deleting legacy files", e);
            LegacyCleanup.showErrorDialog("B\u0142\u0105d podczas usuwania plik\u00f3w:\n" + e.getMessage());
        }
    }

    private static void showSuccessDialog(String message) {
        LegacyCleanup.showInfoDialog(message, "Sukces", new Color(76, 175, 80));
    }

    private static void showWarningDialog(String message) {
        LegacyCleanup.showInfoDialog(message, "Uwaga", new Color(255, 193, 7));
    }

    private static void showErrorDialog(String message) {
        LegacyCleanup.showInfoDialog(message, "B\u0142\u0105d", new Color(244, 67, 54));
    }

    private static void showInfoDialog(String message, String title, Color accentColor) {
        JDialog dialog = new JDialog((Frame)null, title, true);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(2);
        dialog.setResizable(false);
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(new Color(40, 44, 52));
        JLabel msgLabel = new JLabel("<html><div style='width: 280px; color: #E0E0E0;'>" + message.replace("\n", "<br>") + "</div></html>");
        msgLabel.setFont(new Font("Segoe UI", 0, 13));
        panel.add((Component)msgLabel, "Center");
        JButton okButton = LegacyCleanup.createStyledButton("OK", accentColor, Color.WHITE);
        okButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(1));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        panel.add((Component)buttonPanel, "South");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}

