/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.pbrands.util.FontUtil;

public class LicenseInputPanel
extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton authenticateButton;
    private final JLabel statusLabel;
    private boolean isPasswordVisible = false;
    private final Timer loadingTimer;
    private final SpinnerIcon spinnerIcon;
    private static final Color BG_CARD = new Color(37, 37, 38);
    private static final Color BG_INPUT = new Color(30, 30, 30);
    private static final Color BORDER_DEFAULT = new Color(62, 62, 66);
    private static final Color BORDER_FOCUS = new Color(0, 122, 204);
    private static final Color ACCENT = new Color(0, 122, 204);
    private static final Color ACCENT_HOVER = new Color(0, 140, 230);
    private static final Color ACCENT_PRESSED = new Color(0, 100, 180);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private static final Color TEXT_PLACEHOLDER = new Color(100, 100, 100);

    public LicenseInputPanel(ActionListener authenticateListener) {
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);
        JPanel card = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight(), 24.0, 24.0));
                g2.setColor(BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, this.getWidth() - 1, this.getHeight() - 1, 24.0, 24.0));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, 1));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(48, 48, 40, 48));
        JLabel titleLabel = new JLabel("Zaloguj si\u0119");
        titleLabel.setFont(FontUtil.NUNITO_BLACK.deriveFont(26.0f));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(0.5f);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        JLabel subTitleLabel = new JLabel("Wprowad\u017a dane logowania");
        subTitleLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
        subTitleLabel.setForeground(TEXT_SECONDARY);
        subTitleLabel.setAlignmentX(0.5f);
        card.add(subTitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 36)));
        this.usernameField = this.createStyledTextField("Nazwa u\u017cytkownika");
        card.add(this.usernameField);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        this.passwordField = this.createStyledPasswordField("Has\u0142o");
        card.add(this.passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 28)));
        this.authenticateButton = this.createStyledButton("Zaloguj");
        this.authenticateButton.addActionListener(authenticateListener);
        card.add(this.authenticateButton);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        this.statusLabel = new JLabel(" ");
        this.statusLabel.setFont(FontUtil.NUNITO_REGULAR.deriveFont(13.0f));
        this.statusLabel.setForeground(TEXT_SECONDARY);
        this.statusLabel.setAlignmentX(0.5f);
        card.add(this.statusLabel);
        this.usernameField.addActionListener(authenticateListener);
        this.passwordField.addActionListener(authenticateListener);
        this.add(card);
        this.spinnerIcon = new SpinnerIcon(this);
        this.loadingTimer = new Timer(40, e -> {
            this.spinnerIcon.nextFrame();
            this.authenticateButton.repaint();
        });
    }

    private JTextField createStyledTextField(final String placeholder) {
        JTextField field = new JTextField(){
            private boolean focused = false;
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
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight(), 8.0, 8.0));
                g2.setColor(this.focused ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(this.focused ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, this.getWidth() - 1, this.getHeight() - 1, 8.0, 8.0));
                g2.dispose();
                super.paintComponent(g);
                if (this.getText().isEmpty() && !this.focused) {
                    Graphics2D g3 = (Graphics2D)g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_PLACEHOLDER);
                    g3.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
                    Insets insets = this.getInsets();
                    g3.drawString(placeholder, insets.left, this.getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 2);
                    g3.dispose();
                }
            }
        };
        field.setPreferredSize(new Dimension(300, 44));
        field.setMaximumSize(new Dimension(300, 44));
        field.setOpaque(false);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setAlignmentX(0.5f);
        return field;
    }

    private JPasswordField createStyledPasswordField(final String placeholder) {
        JPasswordField field = new JPasswordField(){
            private boolean focused = false;
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
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight(), 8.0, 8.0));
                g2.setColor(this.focused ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(this.focused ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, this.getWidth() - 1, this.getHeight() - 1, 8.0, 8.0));
                g2.dispose();
                super.paintComponent(g);
                if (this.getPassword().length == 0 && !this.focused) {
                    Graphics2D g3 = (Graphics2D)g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_PLACEHOLDER);
                    g3.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
                    Insets insets = this.getInsets();
                    g3.drawString(placeholder, insets.left, this.getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 2);
                    g3.dispose();
                }
            }
        };
        field.setPreferredSize(new Dimension(300, 44));
        field.setMaximumSize(new Dimension(300, 44));
        field.setOpaque(false);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(FontUtil.NUNITO_REGULAR.deriveFont(14.0f));
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setAlignmentX(0.5f);
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text){
            private boolean hovered;
            private boolean pressed;
            {
                this.hovered = false;
                this.pressed = false;
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

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        this.repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        this.repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Color bg = !this.isEnabled() ? new Color(60, 60, 60) : (this.pressed ? ACCENT_PRESSED : (this.hovered ? ACCENT_HOVER : ACCENT));
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight(), 8.0, 8.0));
                g2.setColor(this.isEnabled() ? Color.WHITE : TEXT_SECONDARY);
                g2.setFont(this.getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = this.getText();
                Icon icon = this.getIcon();
                int totalWidth = fm.stringWidth(txt);
                if (icon != null) {
                    totalWidth += icon.getIconWidth() + 8;
                }
                int x = (this.getWidth() - totalWidth) / 2;
                int y = (this.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (icon != null) {
                    icon.paintIcon(this, g2, x, (this.getHeight() - icon.getIconHeight()) / 2);
                    x += icon.getIconWidth() + 8;
                }
                g2.drawString(txt, x, y);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(300, 44));
        button.setMaximumSize(new Dimension(300, 44));
        button.setFont(FontUtil.NUNITO_BOLD.deriveFont(14.0f));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(12));
        button.setAlignmentX(0.5f);
        return button;
    }

    public String getUsername() {
        return this.usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(this.passwordField.getPassword());
    }

    public boolean areCredentialsValid() {
        return !this.getUsername().isEmpty() && !this.getPassword().isEmpty();
    }

    public void clearPassword() {
        this.passwordField.setText("");
    }

    public void showLoading() {
        this.authenticateButton.setText("Logowanie...");
        this.authenticateButton.setIcon(this.spinnerIcon);
        this.authenticateButton.setDisabledIcon(this.spinnerIcon);
        this.authenticateButton.setEnabled(false);
        this.usernameField.setEnabled(false);
        this.passwordField.setEnabled(false);
        this.statusLabel.setText("\u0141\u0105czenie z serwerem...");
        this.statusLabel.setForeground(TEXT_SECONDARY);
        this.loadingTimer.start();
    }

    public void hideLoading() {
        this.loadingTimer.stop();
        this.authenticateButton.setIcon(null);
        this.authenticateButton.setText("Zaloguj");
        this.authenticateButton.setEnabled(true);
        this.usernameField.setEnabled(true);
        this.passwordField.setEnabled(true);
        this.statusLabel.setText(" ");
    }

    public void enableAuthenticateButton() {
        this.authenticateButton.setEnabled(true);
    }

    public void showError(String message) {
        this.hideLoading();
        this.statusLabel.setText(message);
        this.statusLabel.setForeground(new Color(255, 100, 100));
    }

    private class SpinnerIcon
    implements Icon {
        private int angle = 0;

        private SpinnerIcon(LicenseInputPanel licenseInputPanel) {
        }

        public void nextFrame() {
            this.angle = (this.angle + 15) % 360;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(Color.WHITE);
            int w = this.getIconWidth();
            int h = this.getIconHeight();
            g2.drawArc(x, y, w - 2, h - 2, this.angle, 270);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }
}

