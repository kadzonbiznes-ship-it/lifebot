/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import sun.swing.SwingUtilities2;

public class BasicEditorPaneUI
extends BasicTextUI {
    private static final String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";

    public static ComponentUI createUI(JComponent c) {
        return new BasicEditorPaneUI();
    }

    @Override
    protected String getPropertyPrefix() {
        return "EditorPane";
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.updateDisplayProperties(c.getFont(), c.getForeground());
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.cleanDisplayProperties();
        super.uninstallUI(c);
    }

    @Override
    public EditorKit getEditorKit(JTextComponent tc) {
        JEditorPane pane = (JEditorPane)this.getComponent();
        return pane.getEditorKit();
    }

    @Override
    ActionMap getActionMap() {
        Action[] actions;
        ActionMapUIResource am = new ActionMapUIResource();
        am.put("requestFocus", new BasicTextUI.FocusAction(this));
        EditorKit editorKit = this.getEditorKit(this.getComponent());
        if (editorKit != null && (actions = editorKit.getActions()) != null) {
            this.addActions(am, actions);
        }
        am.put(TransferHandler.getCutAction().getValue("Name"), TransferHandler.getCutAction());
        am.put(TransferHandler.getCopyAction().getValue("Name"), TransferHandler.getCopyAction());
        am.put(TransferHandler.getPasteAction().getValue("Name"), TransferHandler.getPasteAction());
        return am;
    }

    @Override
    protected void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String name = evt.getPropertyName();
        if ("editorKit".equals(name)) {
            ActionMap map = SwingUtilities.getUIActionMap(this.getComponent());
            if (map != null) {
                Action[] actions;
                Object newValue;
                Action[] actions2;
                Object oldValue = evt.getOldValue();
                if (oldValue instanceof EditorKit && (actions2 = ((EditorKit)oldValue).getActions()) != null) {
                    this.removeActions(map, actions2);
                }
                if ((newValue = evt.getNewValue()) instanceof EditorKit && (actions = ((EditorKit)newValue).getActions()) != null) {
                    this.addActions(map, actions);
                }
            }
            this.updateFocusTraversalKeys();
        } else if ("editable".equals(name)) {
            this.updateFocusTraversalKeys();
        } else if ("foreground".equals(name) || "font".equals(name) || "document".equals(name) || "JEditorPane.w3cLengthUnits".equals(name) || "JEditorPane.honorDisplayProperties".equals(name)) {
            JTextComponent c = this.getComponent();
            this.updateDisplayProperties(c.getFont(), c.getForeground());
            if ("JEditorPane.w3cLengthUnits".equals(name) || "JEditorPane.honorDisplayProperties".equals(name)) {
                this.modelChanged();
            }
            if ("foreground".equals(name)) {
                Object honorDisplayPropertiesObject = c.getClientProperty("JEditorPane.honorDisplayProperties");
                boolean honorDisplayProperties = false;
                if (honorDisplayPropertiesObject instanceof Boolean) {
                    honorDisplayProperties = (Boolean)honorDisplayPropertiesObject;
                }
                if (honorDisplayProperties) {
                    this.modelChanged();
                }
            }
        }
    }

    void removeActions(ActionMap map, Action[] actions) {
        for (Action a : actions) {
            map.remove(a.getValue("Name"));
        }
    }

    void addActions(ActionMap map, Action[] actions) {
        for (Action a : actions) {
            map.put(a.getValue("Name"), a);
        }
    }

    void updateDisplayProperties(Font font, Color fg) {
        Document doc;
        JTextComponent c = this.getComponent();
        Object honorDisplayPropertiesObject = c.getClientProperty("JEditorPane.honorDisplayProperties");
        boolean honorDisplayProperties = false;
        Object w3cLengthUnitsObject = c.getClientProperty("JEditorPane.w3cLengthUnits");
        boolean w3cLengthUnits = false;
        if (honorDisplayPropertiesObject instanceof Boolean) {
            honorDisplayProperties = (Boolean)honorDisplayPropertiesObject;
        }
        if (w3cLengthUnitsObject instanceof Boolean) {
            w3cLengthUnits = (Boolean)w3cLengthUnitsObject;
        }
        if (this instanceof BasicTextPaneUI || honorDisplayProperties) {
            doc = this.getComponent().getDocument();
            if (doc instanceof StyledDocument) {
                if (doc instanceof HTMLDocument && honorDisplayProperties) {
                    this.updateCSS(font, fg);
                } else {
                    this.updateStyle(font, fg);
                }
            }
        } else {
            this.cleanDisplayProperties();
        }
        if (w3cLengthUnits) {
            doc = this.getComponent().getDocument();
            if (doc instanceof HTMLDocument) {
                StyleSheet documentStyleSheet = ((HTMLDocument)doc).getStyleSheet();
                documentStyleSheet.addRule("W3C_LENGTH_UNITS_ENABLE");
            }
        } else {
            doc = this.getComponent().getDocument();
            if (doc instanceof HTMLDocument) {
                StyleSheet documentStyleSheet = ((HTMLDocument)doc).getStyleSheet();
                documentStyleSheet.addRule("W3C_LENGTH_UNITS_DISABLE");
            }
        }
    }

    void cleanDisplayProperties() {
        Document document = this.getComponent().getDocument();
        if (document instanceof HTMLDocument) {
            Style style;
            StyleSheet documentStyleSheet = ((HTMLDocument)document).getStyleSheet();
            StyleSheet[] styleSheets = documentStyleSheet.getStyleSheets();
            if (styleSheets != null) {
                for (StyleSheet s : styleSheets) {
                    if (!(s instanceof StyleSheetUIResource)) continue;
                    documentStyleSheet.removeStyleSheet(s);
                    documentStyleSheet.addRule("BASE_SIZE_DISABLE");
                    break;
                }
            }
            if ((style = ((StyledDocument)document).getStyle("default")).getAttribute(FONT_ATTRIBUTE_KEY) != null) {
                style.removeAttribute(FONT_ATTRIBUTE_KEY);
            }
        }
    }

    private void updateCSS(Font font, Color fg) {
        JTextComponent component = this.getComponent();
        Document document = component.getDocument();
        if (document instanceof HTMLDocument) {
            StyleSheetUIResource styleSheet = new StyleSheetUIResource();
            StyleSheet documentStyleSheet = ((HTMLDocument)document).getStyleSheet();
            StyleSheet[] styleSheets = documentStyleSheet.getStyleSheets();
            if (styleSheets != null) {
                for (StyleSheet s : styleSheets) {
                    if (!(s instanceof StyleSheetUIResource)) continue;
                    documentStyleSheet.removeStyleSheet(s);
                }
            }
            String cssRule = SwingUtilities2.displayPropertiesToCSS(font, fg);
            styleSheet.addRule(cssRule);
            documentStyleSheet.addStyleSheet(styleSheet);
            documentStyleSheet.addRule("BASE_SIZE " + component.getFont().getSize());
            Style style = ((StyledDocument)document).getStyle("default");
            if (!font.equals(style.getAttribute(FONT_ATTRIBUTE_KEY))) {
                style.addAttribute(FONT_ATTRIBUTE_KEY, font);
            }
        }
    }

    private void updateStyle(Font font, Color fg) {
        this.updateFont(font);
        this.updateForeground(fg);
    }

    private void updateForeground(Color color) {
        StyledDocument doc = (StyledDocument)this.getComponent().getDocument();
        Style style = doc.getStyle("default");
        if (style == null) {
            return;
        }
        if (color == null) {
            if (style.getAttribute(StyleConstants.Foreground) != null) {
                style.removeAttribute(StyleConstants.Foreground);
            }
        } else if (!color.equals(StyleConstants.getForeground(style))) {
            StyleConstants.setForeground(style, color);
        }
    }

    private void updateFont(Font font) {
        StyledDocument doc = (StyledDocument)this.getComponent().getDocument();
        Style style = doc.getStyle("default");
        if (style == null) {
            return;
        }
        String fontFamily = (String)style.getAttribute(StyleConstants.FontFamily);
        Integer fontSize = (Integer)style.getAttribute(StyleConstants.FontSize);
        Boolean isBold = (Boolean)style.getAttribute(StyleConstants.Bold);
        Boolean isItalic = (Boolean)style.getAttribute(StyleConstants.Italic);
        Font fontAttribute = (Font)style.getAttribute(FONT_ATTRIBUTE_KEY);
        if (font == null) {
            if (fontFamily != null) {
                style.removeAttribute(StyleConstants.FontFamily);
            }
            if (fontSize != null) {
                style.removeAttribute(StyleConstants.FontSize);
            }
            if (isBold != null) {
                style.removeAttribute(StyleConstants.Bold);
            }
            if (isItalic != null) {
                style.removeAttribute(StyleConstants.Italic);
            }
            if (fontAttribute != null) {
                style.removeAttribute(FONT_ATTRIBUTE_KEY);
            }
        } else {
            if (!font.getName().equals(fontFamily)) {
                StyleConstants.setFontFamily(style, font.getName());
            }
            if (fontSize == null || fontSize.intValue() != font.getSize()) {
                StyleConstants.setFontSize(style, font.getSize());
            }
            if (isBold == null || isBold.booleanValue() != font.isBold()) {
                StyleConstants.setBold(style, font.isBold());
            }
            if (isItalic == null || isItalic.booleanValue() != font.isItalic()) {
                StyleConstants.setItalic(style, font.isItalic());
            }
            if (!font.equals(fontAttribute)) {
                style.addAttribute(FONT_ATTRIBUTE_KEY, font);
            }
        }
    }

    static class StyleSheetUIResource
    extends StyleSheet
    implements UIResource {
        StyleSheetUIResource() {
        }
    }
}

