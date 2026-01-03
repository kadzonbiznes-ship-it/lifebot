/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Locale;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;
import sun.swing.icon.SortArrowIcon;

public abstract class BasicLookAndFeel
extends LookAndFeel
implements Serializable {
    static boolean needsEventHelper;
    private transient Object audioLock = new Object();
    private Clip clipPlaying;
    AWTEventHelper invocator = null;
    private PropertyChangeListener disposer = null;

    protected BasicLookAndFeel() {
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults table = new UIDefaults(610, 0.75f);
        this.initClassDefaults(table);
        this.initSystemColorDefaults(table);
        this.initComponentDefaults(table);
        return table;
    }

    @Override
    public void initialize() {
        if (needsEventHelper) {
            this.installAWTEventListener();
        }
    }

    void installAWTEventListener() {
        if (this.invocator == null) {
            this.invocator = new AWTEventHelper();
            needsEventHelper = true;
            this.disposer = new PropertyChangeListener(){

                @Override
                public void propertyChange(PropertyChangeEvent prpChg) {
                    BasicLookAndFeel.this.uninitialize();
                }
            };
            AppContext.getAppContext().addPropertyChangeListener("guidisposed", this.disposer);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void uninitialize() {
        AppContext context = AppContext.getAppContext();
        StringBuilder stringBuilder = BasicPopupMenuUI.MOUSE_GRABBER_KEY;
        synchronized (stringBuilder) {
            Object grabber = context.get(BasicPopupMenuUI.MOUSE_GRABBER_KEY);
            if (grabber != null) {
                ((BasicPopupMenuUI.MouseGrabber)grabber).uninstall();
            }
        }
        stringBuilder = BasicPopupMenuUI.MENU_KEYBOARD_HELPER_KEY;
        synchronized (stringBuilder) {
            Object helper = context.get(BasicPopupMenuUI.MENU_KEYBOARD_HELPER_KEY);
            if (helper != null) {
                ((BasicPopupMenuUI.MenuKeyboardHelper)helper).uninstall();
            }
        }
        if (this.invocator != null) {
            AccessController.doPrivileged(this.invocator);
            this.invocator = null;
        }
        if (this.disposer != null) {
            context.removePropertyChangeListener("guidisposed", this.disposer);
            this.disposer = null;
        }
    }

    protected void initClassDefaults(UIDefaults table) {
        String basicPackageName = "javax.swing.plaf.basic.";
        Object[] uiDefaults = new Object[]{"ButtonUI", "javax.swing.plaf.basic.BasicButtonUI", "CheckBoxUI", "javax.swing.plaf.basic.BasicCheckBoxUI", "ColorChooserUI", "javax.swing.plaf.basic.BasicColorChooserUI", "FormattedTextFieldUI", "javax.swing.plaf.basic.BasicFormattedTextFieldUI", "MenuBarUI", "javax.swing.plaf.basic.BasicMenuBarUI", "MenuUI", "javax.swing.plaf.basic.BasicMenuUI", "MenuItemUI", "javax.swing.plaf.basic.BasicMenuItemUI", "CheckBoxMenuItemUI", "javax.swing.plaf.basic.BasicCheckBoxMenuItemUI", "RadioButtonMenuItemUI", "javax.swing.plaf.basic.BasicRadioButtonMenuItemUI", "RadioButtonUI", "javax.swing.plaf.basic.BasicRadioButtonUI", "ToggleButtonUI", "javax.swing.plaf.basic.BasicToggleButtonUI", "PopupMenuUI", "javax.swing.plaf.basic.BasicPopupMenuUI", "ProgressBarUI", "javax.swing.plaf.basic.BasicProgressBarUI", "ScrollBarUI", "javax.swing.plaf.basic.BasicScrollBarUI", "ScrollPaneUI", "javax.swing.plaf.basic.BasicScrollPaneUI", "SplitPaneUI", "javax.swing.plaf.basic.BasicSplitPaneUI", "SliderUI", "javax.swing.plaf.basic.BasicSliderUI", "SeparatorUI", "javax.swing.plaf.basic.BasicSeparatorUI", "SpinnerUI", "javax.swing.plaf.basic.BasicSpinnerUI", "ToolBarSeparatorUI", "javax.swing.plaf.basic.BasicToolBarSeparatorUI", "PopupMenuSeparatorUI", "javax.swing.plaf.basic.BasicPopupMenuSeparatorUI", "TabbedPaneUI", "javax.swing.plaf.basic.BasicTabbedPaneUI", "TextAreaUI", "javax.swing.plaf.basic.BasicTextAreaUI", "TextFieldUI", "javax.swing.plaf.basic.BasicTextFieldUI", "PasswordFieldUI", "javax.swing.plaf.basic.BasicPasswordFieldUI", "TextPaneUI", "javax.swing.plaf.basic.BasicTextPaneUI", "EditorPaneUI", "javax.swing.plaf.basic.BasicEditorPaneUI", "TreeUI", "javax.swing.plaf.basic.BasicTreeUI", "LabelUI", "javax.swing.plaf.basic.BasicLabelUI", "ListUI", "javax.swing.plaf.basic.BasicListUI", "ToolBarUI", "javax.swing.plaf.basic.BasicToolBarUI", "ToolTipUI", "javax.swing.plaf.basic.BasicToolTipUI", "ComboBoxUI", "javax.swing.plaf.basic.BasicComboBoxUI", "TableUI", "javax.swing.plaf.basic.BasicTableUI", "TableHeaderUI", "javax.swing.plaf.basic.BasicTableHeaderUI", "InternalFrameUI", "javax.swing.plaf.basic.BasicInternalFrameUI", "DesktopPaneUI", "javax.swing.plaf.basic.BasicDesktopPaneUI", "DesktopIconUI", "javax.swing.plaf.basic.BasicDesktopIconUI", "FileChooserUI", "javax.swing.plaf.basic.BasicFileChooserUI", "OptionPaneUI", "javax.swing.plaf.basic.BasicOptionPaneUI", "PanelUI", "javax.swing.plaf.basic.BasicPanelUI", "ViewportUI", "javax.swing.plaf.basic.BasicViewportUI", "RootPaneUI", "javax.swing.plaf.basic.BasicRootPaneUI"};
        table.putDefaults(uiDefaults);
    }

    protected void initSystemColorDefaults(UIDefaults table) {
        String[] defaultSystemColors = new String[]{"desktop", "#005C5C", "activeCaption", "#000080", "activeCaptionText", "#FFFFFF", "activeCaptionBorder", "#C0C0C0", "inactiveCaption", "#808080", "inactiveCaptionText", "#C0C0C0", "inactiveCaptionBorder", "#C0C0C0", "window", "#FFFFFF", "windowBorder", "#000000", "windowText", "#000000", "menu", "#C0C0C0", "menuText", "#000000", "text", "#C0C0C0", "textText", "#000000", "textHighlight", "#000080", "textHighlightText", "#FFFFFF", "textInactiveText", "#808080", "control", "#C0C0C0", "controlText", "#000000", "controlHighlight", "#C0C0C0", "controlLtHighlight", "#FFFFFF", "controlShadow", "#808080", "controlDkShadow", "#000000", "scrollbar", "#E0E0E0", "info", "#FFFFE1", "infoText", "#000000"};
        this.loadSystemColors(table, defaultSystemColors, this.isNativeLookAndFeel());
    }

    protected void loadSystemColors(UIDefaults table, String[] systemColors, boolean useNative) {
        if (useNative) {
            for (int i = 0; i < systemColors.length; i += 2) {
                Color color = Color.black;
                try {
                    String name = systemColors[i];
                    color = (Color)SystemColor.class.getField(name).get(null);
                }
                catch (Exception name) {
                    // empty catch block
                }
                table.put(systemColors[i], new ColorUIResource(color));
            }
        } else {
            for (int i = 0; i < systemColors.length; i += 2) {
                Color color = Color.black;
                try {
                    color = Color.decode(systemColors[i + 1]);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                table.put(systemColors[i], new ColorUIResource(color));
            }
        }
    }

    private void initResourceBundle(UIDefaults table) {
        table.setDefaultLocale(Locale.getDefault());
        SwingAccessor.getUIDefaultsAccessor().addInternalBundle(table, "com.sun.swing.internal.plaf.basic.resources.basic");
    }

    protected void initComponentDefaults(UIDefaults table) {
        this.initResourceBundle(table);
        Integer fiveHundred = 500;
        Long oneThousand = 1000L;
        UIDefaults.LazyValue dialogPlain12 = t -> new FontUIResource("Dialog", 0, 12);
        UIDefaults.LazyValue serifPlain12 = t -> new FontUIResource("Serif", 0, 12);
        UIDefaults.LazyValue sansSerifPlain12 = t -> new FontUIResource("SansSerif", 0, 12);
        UIDefaults.LazyValue monospacedPlain12 = t -> new FontUIResource("Monospaced", 0, 12);
        UIDefaults.LazyValue dialogBold12 = t -> new FontUIResource("Dialog", 1, 12);
        ColorUIResource red = new ColorUIResource(Color.red);
        ColorUIResource black = new ColorUIResource(Color.black);
        ColorUIResource white = new ColorUIResource(Color.white);
        ColorUIResource yellow = new ColorUIResource(Color.yellow);
        ColorUIResource gray = new ColorUIResource(Color.gray);
        ColorUIResource lightGray = new ColorUIResource(Color.lightGray);
        ColorUIResource darkGray = new ColorUIResource(Color.darkGray);
        ColorUIResource scrollBarTrack = new ColorUIResource(224, 224, 224);
        Color control = table.getColor("control");
        Color controlDkShadow = table.getColor("controlDkShadow");
        Color controlHighlight = table.getColor("controlHighlight");
        Color controlLtHighlight = table.getColor("controlLtHighlight");
        Color controlShadow = table.getColor("controlShadow");
        Color controlText = table.getColor("controlText");
        Color menu = table.getColor("menu");
        Color menuText = table.getColor("menuText");
        Color textHighlight = table.getColor("textHighlight");
        Color textHighlightText = table.getColor("textHighlightText");
        Color textInactiveText = table.getColor("textInactiveText");
        Color textText = table.getColor("textText");
        Color window = table.getColor("window");
        InsetsUIResource zeroInsets = new InsetsUIResource(0, 0, 0, 0);
        InsetsUIResource twoInsets = new InsetsUIResource(2, 2, 2, 2);
        InsetsUIResource threeInsets = new InsetsUIResource(3, 3, 3, 3);
        UIDefaults.LazyValue marginBorder = t -> new BasicBorders.MarginBorder();
        UIDefaults.LazyValue etchedBorder = t -> BorderUIResource.getEtchedBorderUIResource();
        UIDefaults.LazyValue loweredBevelBorder = t -> BorderUIResource.getLoweredBevelBorderUIResource();
        UIDefaults.LazyValue popupMenuBorder = t -> BasicBorders.getInternalFrameBorder();
        UIDefaults.LazyValue blackLineBorder = t -> BorderUIResource.getBlackLineBorderUIResource();
        UIDefaults.LazyValue focusCellHighlightBorder = t -> new BorderUIResource.LineBorderUIResource(yellow);
        BorderUIResource.EmptyBorderUIResource noFocusBorder = new BorderUIResource.EmptyBorderUIResource(1, 1, 1, 1);
        UIDefaults.LazyValue tableHeaderBorder = t -> new BorderUIResource.BevelBorderUIResource(0, controlLtHighlight, control, controlDkShadow, controlShadow);
        UIDefaults.LazyValue buttonBorder = t -> BasicBorders.getButtonBorder();
        UIDefaults.LazyValue buttonToggleBorder = t -> BasicBorders.getToggleButtonBorder();
        UIDefaults.LazyValue radioButtonBorder = t -> BasicBorders.getRadioButtonBorder();
        Object newFolderIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/NewFolder.gif");
        Object upFolderIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/UpFolder.gif");
        Object homeFolderIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/HomeFolder.gif");
        Object detailsViewIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/DetailsView.gif");
        Object listViewIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/ListView.gif");
        Object directoryIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Directory.gif");
        Object fileIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/File.gif");
        Object computerIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Computer.gif");
        Object hardDriveIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/HardDrive.gif");
        Object floppyDriveIcon = SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/FloppyDrive.gif");
        UIDefaults.LazyValue internalFrameBorder = t -> BasicBorders.getInternalFrameBorder();
        UIDefaults.ActiveValue listCellRendererActiveValue = new UIDefaults.ActiveValue(this){

            @Override
            public Object createValue(UIDefaults table) {
                return new DefaultListCellRenderer.UIResource();
            }
        };
        UIDefaults.LazyValue menuBarBorder = t -> BasicBorders.getMenuBarBorder();
        UIDefaults.LazyValue menuItemCheckIcon = t -> BasicIconFactory.getMenuItemCheckIcon();
        UIDefaults.LazyValue menuItemArrowIcon = t -> BasicIconFactory.getMenuItemArrowIcon();
        UIDefaults.LazyValue menuArrowIcon = t -> BasicIconFactory.getMenuArrowIcon();
        UIDefaults.LazyValue checkBoxIcon = t -> BasicIconFactory.getCheckBoxIcon();
        UIDefaults.LazyValue radioButtonIcon = t -> BasicIconFactory.getRadioButtonIcon();
        UIDefaults.LazyValue checkBoxMenuItemIcon = t -> BasicIconFactory.getCheckBoxMenuItemIcon();
        UIDefaults.LazyValue radioButtonMenuItemIcon = t -> BasicIconFactory.getRadioButtonMenuItemIcon();
        String menuItemAcceleratorDelimiter = "+";
        DimensionUIResource optionPaneMinimumSize = new DimensionUIResource(262, 90);
        int zero = 0;
        UIDefaults.LazyValue zeroBorder = t -> new BorderUIResource.EmptyBorderUIResource(zero, zero, zero, zero);
        int ten = 10;
        UIDefaults.LazyValue optionPaneBorder = t -> new BorderUIResource.EmptyBorderUIResource(ten, ten, 12, ten);
        UIDefaults.LazyValue optionPaneButtonAreaBorder = t -> new BorderUIResource.EmptyBorderUIResource(6, zero, zero, zero);
        UIDefaults.LazyValue progressBarBorder = t -> BasicBorders.getProgressBarBorder();
        DimensionUIResource minimumThumbSize = new DimensionUIResource(8, 8);
        DimensionUIResource maximumThumbSize = new DimensionUIResource(4096, 4096);
        InsetsUIResource sliderFocusInsets = twoInsets;
        DimensionUIResource toolBarSeparatorSize = new DimensionUIResource(10, 10);
        UIDefaults.LazyValue splitPaneBorder = t -> BasicBorders.getSplitPaneBorder();
        UIDefaults.LazyValue splitPaneDividerBorder = t -> BasicBorders.getSplitPaneDividerBorder();
        InsetsUIResource tabbedPaneTabInsets = new InsetsUIResource(0, 4, 1, 4);
        InsetsUIResource tabbedPaneTabPadInsets = new InsetsUIResource(2, 2, 2, 1);
        InsetsUIResource tabbedPaneTabAreaInsets = new InsetsUIResource(3, 2, 0, 2);
        InsetsUIResource tabbedPaneContentBorderInsets = new InsetsUIResource(2, 2, 3, 3);
        UIDefaults.LazyValue textFieldBorder = t -> BasicBorders.getTextFieldBorder();
        InsetsUIResource editorMargin = threeInsets;
        Integer caretBlinkRate = fiveHundred;
        Object[] allAuditoryCues = new Object[]{"CheckBoxMenuItem.commandSound", "InternalFrame.closeSound", "InternalFrame.maximizeSound", "InternalFrame.minimizeSound", "InternalFrame.restoreDownSound", "InternalFrame.restoreUpSound", "MenuItem.commandSound", "OptionPane.errorSound", "OptionPane.informationSound", "OptionPane.questionSound", "OptionPane.warningSound", "PopupMenu.popupSound", "RadioButtonMenuItem.commandSound"};
        Object[] noAuditoryCues = new Object[]{"mute"};
        Object[] defaults = new Object[]{"AuditoryCues.cueList", allAuditoryCues, "AuditoryCues.allAuditoryCues", allAuditoryCues, "AuditoryCues.noAuditoryCues", noAuditoryCues, "AuditoryCues.playList", null, "Button.defaultButtonFollowsFocus", Boolean.TRUE, "Button.font", dialogPlain12, "Button.background", control, "Button.foreground", controlText, "Button.shadow", controlShadow, "Button.darkShadow", controlDkShadow, "Button.light", controlHighlight, "Button.highlight", controlLtHighlight, "Button.border", buttonBorder, "Button.margin", new InsetsUIResource(2, 14, 2, 14), "Button.textIconGap", 4, "Button.textShiftOffset", zero, "Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released", "ENTER", "pressed", "released ENTER", "released"}), "ToggleButton.font", dialogPlain12, "ToggleButton.background", control, "ToggleButton.foreground", controlText, "ToggleButton.shadow", controlShadow, "ToggleButton.darkShadow", controlDkShadow, "ToggleButton.light", controlHighlight, "ToggleButton.highlight", controlLtHighlight, "ToggleButton.border", buttonToggleBorder, "ToggleButton.margin", new InsetsUIResource(2, 14, 2, 14), "ToggleButton.textIconGap", 4, "ToggleButton.textShiftOffset", zero, "ToggleButton.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "RadioButton.font", dialogPlain12, "RadioButton.background", control, "RadioButton.foreground", controlText, "RadioButton.shadow", controlShadow, "RadioButton.darkShadow", controlDkShadow, "RadioButton.light", controlHighlight, "RadioButton.highlight", controlLtHighlight, "RadioButton.border", radioButtonBorder, "RadioButton.margin", twoInsets, "RadioButton.textIconGap", 4, "RadioButton.textShiftOffset", zero, "RadioButton.icon", radioButtonIcon, "RadioButton.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released", "RETURN", "pressed"}), "CheckBox.font", dialogPlain12, "CheckBox.background", control, "CheckBox.foreground", controlText, "CheckBox.border", radioButtonBorder, "CheckBox.margin", twoInsets, "CheckBox.textIconGap", 4, "CheckBox.textShiftOffset", zero, "CheckBox.icon", checkBoxIcon, "CheckBox.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "FileChooser.useSystemExtensionHiding", Boolean.FALSE, "ColorChooser.font", dialogPlain12, "ColorChooser.background", control, "ColorChooser.foreground", controlText, "ColorChooser.swatchesSwatchSize", new Dimension(10, 10), "ColorChooser.swatchesRecentSwatchSize", new Dimension(10, 10), "ColorChooser.swatchesDefaultRecentColor", control, "ComboBox.font", sansSerifPlain12, "ComboBox.background", window, "ComboBox.foreground", textText, "ComboBox.buttonBackground", control, "ComboBox.buttonShadow", controlShadow, "ComboBox.buttonDarkShadow", controlDkShadow, "ComboBox.buttonHighlight", controlLtHighlight, "ComboBox.selectionBackground", textHighlight, "ComboBox.selectionForeground", textHighlightText, "ComboBox.disabledBackground", control, "ComboBox.disabledForeground", textInactiveText, "ComboBox.timeFactor", oneThousand, "ComboBox.isEnterSelectablePopup", Boolean.FALSE, "ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "hidePopup", "PAGE_UP", "pageUpPassThrough", "PAGE_DOWN", "pageDownPassThrough", "HOME", "homePassThrough", "END", "endPassThrough", "ENTER", "enterPressed"}), "ComboBox.noActionOnKeyNavigation", Boolean.FALSE, "FileChooser.newFolderIcon", newFolderIcon, "FileChooser.upFolderIcon", upFolderIcon, "FileChooser.homeFolderIcon", homeFolderIcon, "FileChooser.detailsViewIcon", detailsViewIcon, "FileChooser.listViewIcon", listViewIcon, "FileChooser.readOnly", Boolean.FALSE, "FileChooser.usesSingleFilePane", Boolean.FALSE, "FileChooser.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "cancelSelection", "F5", "refresh"}), "FileView.directoryIcon", directoryIcon, "FileView.fileIcon", fileIcon, "FileView.computerIcon", computerIcon, "FileView.hardDriveIcon", hardDriveIcon, "FileView.floppyDriveIcon", floppyDriveIcon, "InternalFrame.titleFont", dialogBold12, "InternalFrame.borderColor", control, "InternalFrame.borderShadow", controlShadow, "InternalFrame.borderDarkShadow", controlDkShadow, "InternalFrame.borderHighlight", controlLtHighlight, "InternalFrame.borderLight", controlHighlight, "InternalFrame.border", internalFrameBorder, "InternalFrame.icon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/JavaCup16.png"), "InternalFrame.maximizeIcon", t -> BasicIconFactory.createEmptyFrameIcon(), "InternalFrame.minimizeIcon", t -> BasicIconFactory.createEmptyFrameIcon(), "InternalFrame.iconifyIcon", t -> BasicIconFactory.createEmptyFrameIcon(), "InternalFrame.closeIcon", t -> BasicIconFactory.createEmptyFrameIcon(), "InternalFrame.closeSound", null, "InternalFrame.maximizeSound", null, "InternalFrame.minimizeSound", null, "InternalFrame.restoreDownSound", null, "InternalFrame.restoreUpSound", null, "InternalFrame.activeTitleBackground", table.get("activeCaption"), "InternalFrame.activeTitleForeground", table.get("activeCaptionText"), "InternalFrame.inactiveTitleBackground", table.get("inactiveCaption"), "InternalFrame.inactiveTitleForeground", table.get("inactiveCaptionText"), "InternalFrame.windowBindings", new Object[]{"shift ESCAPE", "showSystemMenu", "ctrl SPACE", "showSystemMenu", "ESCAPE", "hideSystemMenu"}, "InternalFrameTitlePane.iconifyButtonOpacity", Boolean.TRUE, "InternalFrameTitlePane.maximizeButtonOpacity", Boolean.TRUE, "InternalFrameTitlePane.closeButtonOpacity", Boolean.TRUE, "DesktopIcon.border", internalFrameBorder, "Desktop.minOnScreenInsets", threeInsets, "Desktop.background", table.get("desktop"), "Desktop.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl F5", "restore", "ctrl F4", "close", "ctrl F7", "move", "ctrl F8", "resize", "RIGHT", "right", "KP_RIGHT", "right", "shift RIGHT", "shrinkRight", "shift KP_RIGHT", "shrinkRight", "LEFT", "left", "KP_LEFT", "left", "shift LEFT", "shrinkLeft", "shift KP_LEFT", "shrinkLeft", "UP", "up", "KP_UP", "up", "shift UP", "shrinkUp", "shift KP_UP", "shrinkUp", "DOWN", "down", "KP_DOWN", "down", "shift DOWN", "shrinkDown", "shift KP_DOWN", "shrinkDown", "ESCAPE", "escape", "ctrl F9", "minimize", "ctrl F10", "maximize", "ctrl F6", "selectNextFrame", "ctrl TAB", "selectNextFrame", "ctrl alt F6", "selectNextFrame", "shift ctrl alt F6", "selectPreviousFrame", "ctrl F12", "navigateNext", "shift ctrl F12", "navigatePrevious"}), "Label.font", dialogPlain12, "Label.background", control, "Label.foreground", controlText, "Label.disabledForeground", white, "Label.disabledShadow", controlShadow, "Label.border", null, "List.font", dialogPlain12, "List.background", window, "List.foreground", textText, "List.selectionBackground", textHighlight, "List.selectionForeground", textHighlightText, "List.noFocusBorder", noFocusBorder, "List.focusCellHighlightBorder", focusCellHighlightBorder, "List.dropLineColor", controlShadow, "List.border", null, "List.cellRenderer", listCellRendererActiveValue, "List.timeFactor", oneThousand, "List.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "HOME", "selectFirstRow", "shift HOME", "selectFirstRowExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRowChangeLead", "END", "selectLastRow", "shift END", "selectLastRowExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRowChangeLead", "PAGE_UP", "scrollUp", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDown", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "List.focusInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"LEFT", "selectNextColumn", "KP_LEFT", "selectNextColumn", "shift LEFT", "selectNextColumnExtendSelection", "shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl shift LEFT", "selectNextColumnExtendSelection", "ctrl shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl LEFT", "selectNextColumnChangeLead", "ctrl KP_LEFT", "selectNextColumnChangeLead", "RIGHT", "selectPreviousColumn", "KP_RIGHT", "selectPreviousColumn", "shift RIGHT", "selectPreviousColumnExtendSelection", "shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl RIGHT", "selectPreviousColumnChangeLead", "ctrl KP_RIGHT", "selectPreviousColumnChangeLead"}), "MenuBar.font", dialogPlain12, "MenuBar.background", menu, "MenuBar.foreground", menuText, "MenuBar.shadow", controlShadow, "MenuBar.highlight", controlLtHighlight, "MenuBar.border", menuBarBorder, "MenuBar.windowBindings", new Object[]{"F10", "takeFocus"}, "MenuItem.font", dialogPlain12, "MenuItem.acceleratorFont", dialogPlain12, "MenuItem.background", menu, "MenuItem.foreground", menuText, "MenuItem.selectionForeground", textHighlightText, "MenuItem.selectionBackground", textHighlight, "MenuItem.disabledForeground", null, "MenuItem.acceleratorForeground", menuText, "MenuItem.acceleratorSelectionForeground", textHighlightText, "MenuItem.acceleratorDelimiter", menuItemAcceleratorDelimiter, "MenuItem.border", marginBorder, "MenuItem.borderPainted", Boolean.FALSE, "MenuItem.margin", twoInsets, "MenuItem.checkIcon", menuItemCheckIcon, "MenuItem.arrowIcon", menuItemArrowIcon, "MenuItem.commandSound", null, "RadioButtonMenuItem.font", dialogPlain12, "RadioButtonMenuItem.acceleratorFont", dialogPlain12, "RadioButtonMenuItem.background", menu, "RadioButtonMenuItem.foreground", menuText, "RadioButtonMenuItem.selectionForeground", textHighlightText, "RadioButtonMenuItem.selectionBackground", textHighlight, "RadioButtonMenuItem.disabledForeground", null, "RadioButtonMenuItem.acceleratorForeground", menuText, "RadioButtonMenuItem.acceleratorSelectionForeground", textHighlightText, "RadioButtonMenuItem.border", marginBorder, "RadioButtonMenuItem.borderPainted", Boolean.FALSE, "RadioButtonMenuItem.margin", twoInsets, "RadioButtonMenuItem.checkIcon", radioButtonMenuItemIcon, "RadioButtonMenuItem.arrowIcon", menuItemArrowIcon, "RadioButtonMenuItem.commandSound", null, "CheckBoxMenuItem.font", dialogPlain12, "CheckBoxMenuItem.acceleratorFont", dialogPlain12, "CheckBoxMenuItem.background", menu, "CheckBoxMenuItem.foreground", menuText, "CheckBoxMenuItem.selectionForeground", textHighlightText, "CheckBoxMenuItem.selectionBackground", textHighlight, "CheckBoxMenuItem.disabledForeground", null, "CheckBoxMenuItem.acceleratorForeground", menuText, "CheckBoxMenuItem.acceleratorSelectionForeground", textHighlightText, "CheckBoxMenuItem.border", marginBorder, "CheckBoxMenuItem.borderPainted", Boolean.FALSE, "CheckBoxMenuItem.margin", twoInsets, "CheckBoxMenuItem.checkIcon", checkBoxMenuItemIcon, "CheckBoxMenuItem.arrowIcon", menuItemArrowIcon, "CheckBoxMenuItem.commandSound", null, "Menu.font", dialogPlain12, "Menu.acceleratorFont", dialogPlain12, "Menu.background", menu, "Menu.foreground", menuText, "Menu.selectionForeground", textHighlightText, "Menu.selectionBackground", textHighlight, "Menu.disabledForeground", null, "Menu.acceleratorForeground", menuText, "Menu.acceleratorSelectionForeground", textHighlightText, "Menu.border", marginBorder, "Menu.borderPainted", Boolean.FALSE, "Menu.margin", twoInsets, "Menu.checkIcon", menuItemCheckIcon, "Menu.arrowIcon", menuArrowIcon, "Menu.menuPopupOffsetX", 0, "Menu.menuPopupOffsetY", 0, "Menu.submenuPopupOffsetX", 0, "Menu.submenuPopupOffsetY", 0, "Menu.shortcutKeys", new int[]{SwingUtilities2.getSystemMnemonicKeyMask(), SwingUtilities2.setAltGraphMask(SwingUtilities2.getSystemMnemonicKeyMask())}, "Menu.crossMenuMnemonic", Boolean.TRUE, "Menu.cancelMode", "hideLastSubmenu", "Menu.preserveTopLevelSelection", Boolean.FALSE, "PopupMenu.font", dialogPlain12, "PopupMenu.background", menu, "PopupMenu.foreground", menuText, "PopupMenu.border", popupMenuBorder, "PopupMenu.popupSound", null, "PopupMenu.selectedWindowInputMapBindings", new Object[]{"ESCAPE", "cancel", "DOWN", "selectNext", "KP_DOWN", "selectNext", "UP", "selectPrevious", "KP_UP", "selectPrevious", "LEFT", "selectParent", "KP_LEFT", "selectParent", "RIGHT", "selectChild", "KP_RIGHT", "selectChild", "ENTER", "return", "ctrl ENTER", "return", "SPACE", "return"}, "PopupMenu.selectedWindowInputMapBindings.RightToLeft", new Object[]{"LEFT", "selectChild", "KP_LEFT", "selectChild", "RIGHT", "selectParent", "KP_RIGHT", "selectParent"}, "PopupMenu.consumeEventOnClose", Boolean.FALSE, "OptionPane.font", dialogPlain12, "OptionPane.background", control, "OptionPane.foreground", controlText, "OptionPane.messageForeground", controlText, "OptionPane.border", optionPaneBorder, "OptionPane.messageAreaBorder", zeroBorder, "OptionPane.buttonAreaBorder", optionPaneButtonAreaBorder, "OptionPane.minimumSize", optionPaneMinimumSize, "OptionPane.errorIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Error.gif"), "OptionPane.informationIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Inform.gif"), "OptionPane.warningIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Warn.gif"), "OptionPane.questionIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/Question.gif"), "OptionPane.windowBindings", new Object[]{"ESCAPE", "close"}, "OptionPane.errorSound", null, "OptionPane.informationSound", null, "OptionPane.questionSound", null, "OptionPane.warningSound", null, "OptionPane.buttonClickThreshhold", fiveHundred, "Panel.font", dialogPlain12, "Panel.background", control, "Panel.foreground", textText, "ProgressBar.font", dialogPlain12, "ProgressBar.foreground", textHighlight, "ProgressBar.background", control, "ProgressBar.selectionForeground", control, "ProgressBar.selectionBackground", textHighlight, "ProgressBar.border", progressBarBorder, "ProgressBar.cellLength", 1, "ProgressBar.cellSpacing", zero, "ProgressBar.repaintInterval", 50, "ProgressBar.cycleTime", 3000, "ProgressBar.horizontalSize", new DimensionUIResource(146, 12), "ProgressBar.verticalSize", new DimensionUIResource(12, 146), "Separator.shadow", controlShadow, "Separator.highlight", controlLtHighlight, "Separator.background", controlLtHighlight, "Separator.foreground", controlShadow, "ScrollBar.background", scrollBarTrack, "ScrollBar.foreground", control, "ScrollBar.track", table.get("scrollbar"), "ScrollBar.trackHighlight", controlDkShadow, "ScrollBar.thumb", control, "ScrollBar.thumbHighlight", controlLtHighlight, "ScrollBar.thumbDarkShadow", controlDkShadow, "ScrollBar.thumbShadow", controlShadow, "ScrollBar.border", null, "ScrollBar.minimumThumbSize", minimumThumbSize, "ScrollBar.maximumThumbSize", maximumThumbSize, "ScrollBar.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "positiveUnitIncrement", "KP_DOWN", "positiveUnitIncrement", "PAGE_DOWN", "positiveBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "negativeUnitIncrement", "KP_UP", "negativeUnitIncrement", "PAGE_UP", "negativeBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "ScrollBar.ancestorInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "negativeUnitIncrement", "KP_RIGHT", "negativeUnitIncrement", "LEFT", "positiveUnitIncrement", "KP_LEFT", "positiveUnitIncrement"}), "ScrollBar.width", 16, "ScrollPane.font", dialogPlain12, "ScrollPane.background", control, "ScrollPane.foreground", controlText, "ScrollPane.border", textFieldBorder, "ScrollPane.viewportBorder", null, "ScrollPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "unitScrollRight", "KP_RIGHT", "unitScrollRight", "DOWN", "unitScrollDown", "KP_DOWN", "unitScrollDown", "LEFT", "unitScrollLeft", "KP_LEFT", "unitScrollLeft", "UP", "unitScrollUp", "KP_UP", "unitScrollUp", "PAGE_UP", "scrollUp", "PAGE_DOWN", "scrollDown", "ctrl PAGE_UP", "scrollLeft", "ctrl PAGE_DOWN", "scrollRight", "ctrl HOME", "scrollHome", "ctrl END", "scrollEnd"}), "ScrollPane.ancestorInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"ctrl PAGE_UP", "scrollRight", "ctrl PAGE_DOWN", "scrollLeft"}), "Viewport.font", dialogPlain12, "Viewport.background", control, "Viewport.foreground", textText, "Slider.font", dialogPlain12, "Slider.foreground", control, "Slider.background", control, "Slider.highlight", controlLtHighlight, "Slider.tickColor", Color.black, "Slider.shadow", controlShadow, "Slider.focus", controlDkShadow, "Slider.border", null, "Slider.horizontalSize", new Dimension(200, 21), "Slider.verticalSize", new Dimension(21, 200), "Slider.minimumHorizontalSize", new Dimension(36, 21), "Slider.minimumVerticalSize", new Dimension(21, 36), "Slider.focusInsets", sliderFocusInsets, "Slider.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "negativeUnitIncrement", "KP_DOWN", "negativeUnitIncrement", "PAGE_DOWN", "negativeBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "positiveUnitIncrement", "KP_UP", "positiveUnitIncrement", "PAGE_UP", "positiveBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "Slider.focusInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "negativeUnitIncrement", "KP_RIGHT", "negativeUnitIncrement", "LEFT", "positiveUnitIncrement", "KP_LEFT", "positiveUnitIncrement"}), "Slider.onlyLeftMouseButtonDrag", Boolean.TRUE, "Spinner.font", monospacedPlain12, "Spinner.background", control, "Spinner.foreground", control, "Spinner.border", textFieldBorder, "Spinner.arrowButtonBorder", null, "Spinner.arrowButtonInsets", null, "Spinner.arrowButtonSize", new Dimension(16, 5), "Spinner.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "Spinner.editorBorderPainted", Boolean.FALSE, "Spinner.editorAlignment", 11, "SplitPane.background", control, "SplitPane.highlight", controlLtHighlight, "SplitPane.shadow", controlShadow, "SplitPane.darkShadow", controlDkShadow, "SplitPane.border", splitPaneBorder, "SplitPane.dividerSize", 7, "SplitPaneDivider.border", splitPaneDividerBorder, "SplitPaneDivider.draggingColor", darkGray, "SplitPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "negativeIncrement", "DOWN", "positiveIncrement", "LEFT", "negativeIncrement", "RIGHT", "positiveIncrement", "KP_UP", "negativeIncrement", "KP_DOWN", "positiveIncrement", "KP_LEFT", "negativeIncrement", "KP_RIGHT", "positiveIncrement", "HOME", "selectMin", "END", "selectMax", "F8", "startResize", "F6", "toggleFocus", "ctrl TAB", "focusOutForward", "ctrl shift TAB", "focusOutBackward"}), "TabbedPane.font", dialogPlain12, "TabbedPane.background", control, "TabbedPane.foreground", controlText, "TabbedPane.highlight", controlLtHighlight, "TabbedPane.light", controlHighlight, "TabbedPane.shadow", controlShadow, "TabbedPane.darkShadow", controlDkShadow, "TabbedPane.selected", null, "TabbedPane.focus", controlText, "TabbedPane.textIconGap", 4, "TabbedPane.tabsOverlapBorder", Boolean.FALSE, "TabbedPane.selectionFollowsFocus", Boolean.TRUE, "TabbedPane.labelShift", 1, "TabbedPane.selectedLabelShift", -1, "TabbedPane.tabInsets", tabbedPaneTabInsets, "TabbedPane.selectedTabPadInsets", tabbedPaneTabPadInsets, "TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets, "TabbedPane.contentBorderInsets", tabbedPaneContentBorderInsets, "TabbedPane.tabRunOverlay", 2, "TabbedPane.tabsOpaque", Boolean.TRUE, "TabbedPane.contentOpaque", Boolean.TRUE, "TabbedPane.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "navigateRight", "KP_RIGHT", "navigateRight", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "ctrl DOWN", "requestFocusForVisibleComponent", "ctrl KP_DOWN", "requestFocusForVisibleComponent"}), "TabbedPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl PAGE_DOWN", "navigatePageDown", "ctrl PAGE_UP", "navigatePageUp", "ctrl UP", "requestFocus", "ctrl KP_UP", "requestFocus"}), "Table.font", dialogPlain12, "Table.foreground", controlText, "Table.background", window, "Table.selectionForeground", textHighlightText, "Table.selectionBackground", textHighlight, "Table.dropLineColor", controlShadow, "Table.dropLineShortColor", black, "Table.gridColor", gray, "Table.focusCellBackground", window, "Table.focusCellForeground", controlText, "Table.focusCellHighlightBorder", focusCellHighlightBorder, "Table.scrollPaneBorder", loweredBevelBorder, "Table.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "HOME", "selectFirstColumn", "shift HOME", "selectFirstColumnExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRow", "END", "selectLastColumn", "shift END", "selectLastColumnExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRow", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollLeftExtendSelection", "ctrl PAGE_UP", "scrollLeftChangeSelection", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollRightExtendSelection", "ctrl PAGE_DOWN", "scrollRightChangeSelection", "TAB", "selectNextColumnCell", "shift TAB", "selectPreviousColumnCell", "ENTER", "selectNextRowCell", "shift ENTER", "selectPreviousRowCell", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ESCAPE", "cancel", "F2", "startEditing", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo", "F8", "focusHeader"}), "Table.ancestorInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "selectPreviousColumn", "KP_RIGHT", "selectPreviousColumn", "shift RIGHT", "selectPreviousColumnExtendSelection", "shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl RIGHT", "selectPreviousColumnChangeLead", "ctrl KP_RIGHT", "selectPreviousColumnChangeLead", "LEFT", "selectNextColumn", "KP_LEFT", "selectNextColumn", "shift LEFT", "selectNextColumnExtendSelection", "shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl shift LEFT", "selectNextColumnExtendSelection", "ctrl shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl LEFT", "selectNextColumnChangeLead", "ctrl KP_LEFT", "selectNextColumnChangeLead", "ctrl PAGE_UP", "scrollRightChangeSelection", "ctrl PAGE_DOWN", "scrollLeftChangeSelection", "ctrl shift PAGE_UP", "scrollRightExtendSelection", "ctrl shift PAGE_DOWN", "scrollLeftExtendSelection"}), "Table.ascendingSortIcon", t -> new SortArrowIcon(true, "Table.sortIconColor"), "Table.descendingSortIcon", t -> new SortArrowIcon(false, "Table.sortIconColor"), "Table.sortIconColor", controlShadow, "TableHeader.font", dialogPlain12, "TableHeader.foreground", controlText, "TableHeader.background", control, "TableHeader.cellBorder", tableHeaderBorder, "TableHeader.focusCellBackground", table.getColor("text"), "TableHeader.focusCellForeground", null, "TableHeader.focusCellBorder", null, "TableHeader.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "toggleSortOrder", "LEFT", "selectColumnToLeft", "KP_LEFT", "selectColumnToLeft", "RIGHT", "selectColumnToRight", "KP_RIGHT", "selectColumnToRight", "alt LEFT", "moveColumnLeft", "alt KP_LEFT", "moveColumnLeft", "alt RIGHT", "moveColumnRight", "alt KP_RIGHT", "moveColumnRight", "alt shift LEFT", "resizeLeft", "alt shift KP_LEFT", "resizeLeft", "alt shift RIGHT", "resizeRight", "alt shift KP_RIGHT", "resizeRight", "ESCAPE", "focusTable"}), "TextField.font", sansSerifPlain12, "TextField.background", window, "TextField.foreground", textText, "TextField.shadow", controlShadow, "TextField.darkShadow", controlDkShadow, "TextField.light", controlHighlight, "TextField.highlight", controlLtHighlight, "TextField.inactiveForeground", textInactiveText, "TextField.inactiveBackground", control, "TextField.selectionBackground", textHighlight, "TextField.selectionForeground", textHighlightText, "TextField.caretForeground", textText, "TextField.caretBlinkRate", caretBlinkRate, "TextField.border", textFieldBorder, "TextField.margin", zeroInsets, "FormattedTextField.font", sansSerifPlain12, "FormattedTextField.background", window, "FormattedTextField.foreground", textText, "FormattedTextField.inactiveForeground", textInactiveText, "FormattedTextField.inactiveBackground", control, "FormattedTextField.selectionBackground", textHighlight, "FormattedTextField.selectionForeground", textHighlightText, "FormattedTextField.caretForeground", textText, "FormattedTextField.caretBlinkRate", caretBlinkRate, "FormattedTextField.border", textFieldBorder, "FormattedTextField.margin", zeroInsets, "FormattedTextField.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy-to-clipboard", "ctrl V", "paste-from-clipboard", "ctrl X", "cut-to-clipboard", "COPY", "copy-to-clipboard", "PASTE", "paste-from-clipboard", "CUT", "cut-to-clipboard", "control INSERT", "copy-to-clipboard", "shift INSERT", "paste-from-clipboard", "shift DELETE", "cut-to-clipboard", "shift LEFT", "selection-backward", "shift KP_LEFT", "selection-backward", "shift RIGHT", "selection-forward", "shift KP_RIGHT", "selection-forward", "ctrl LEFT", "caret-previous-word", "ctrl KP_LEFT", "caret-previous-word", "ctrl RIGHT", "caret-next-word", "ctrl KP_RIGHT", "caret-next-word", "ctrl shift LEFT", "selection-previous-word", "ctrl shift KP_LEFT", "selection-previous-word", "ctrl shift RIGHT", "selection-next-word", "ctrl shift KP_RIGHT", "selection-next-word", "ctrl A", "select-all", "HOME", "caret-begin-line", "END", "caret-end-line", "shift HOME", "selection-begin-line", "shift END", "selection-end-line", "BACK_SPACE", "delete-previous", "shift BACK_SPACE", "delete-previous", "ctrl H", "delete-previous", "DELETE", "delete-next", "ctrl DELETE", "delete-next-word", "ctrl BACK_SPACE", "delete-previous-word", "RIGHT", "caret-forward", "LEFT", "caret-backward", "KP_RIGHT", "caret-forward", "KP_LEFT", "caret-backward", "ENTER", "notify-field-accept", "ctrl BACK_SLASH", "unselect", "control shift O", "toggle-componentOrientation", "ESCAPE", "reset-field-edit", "UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "PasswordField.font", monospacedPlain12, "PasswordField.background", window, "PasswordField.foreground", textText, "PasswordField.inactiveForeground", textInactiveText, "PasswordField.inactiveBackground", control, "PasswordField.selectionBackground", textHighlight, "PasswordField.selectionForeground", textHighlightText, "PasswordField.caretForeground", textText, "PasswordField.caretBlinkRate", caretBlinkRate, "PasswordField.border", textFieldBorder, "PasswordField.margin", zeroInsets, "PasswordField.echoChar", Character.valueOf('*'), "TextArea.font", monospacedPlain12, "TextArea.background", window, "TextArea.foreground", textText, "TextArea.inactiveForeground", textInactiveText, "TextArea.selectionBackground", textHighlight, "TextArea.selectionForeground", textHighlightText, "TextArea.caretForeground", textText, "TextArea.caretBlinkRate", caretBlinkRate, "TextArea.border", marginBorder, "TextArea.margin", zeroInsets, "TextPane.font", serifPlain12, "TextPane.background", white, "TextPane.foreground", textText, "TextPane.selectionBackground", textHighlight, "TextPane.selectionForeground", textHighlightText, "TextPane.caretForeground", textText, "TextPane.caretBlinkRate", caretBlinkRate, "TextPane.inactiveForeground", textInactiveText, "TextPane.border", marginBorder, "TextPane.margin", editorMargin, "EditorPane.font", serifPlain12, "EditorPane.background", white, "EditorPane.foreground", textText, "EditorPane.selectionBackground", textHighlight, "EditorPane.selectionForeground", textHighlightText, "EditorPane.caretForeground", textText, "EditorPane.caretBlinkRate", caretBlinkRate, "EditorPane.inactiveForeground", textInactiveText, "EditorPane.border", marginBorder, "EditorPane.margin", editorMargin, "html.pendingImage", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/image-delayed.png"), "html.missingImage", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/image-failed.png"), "TitledBorder.font", dialogPlain12, "TitledBorder.titleColor", controlText, "TitledBorder.border", etchedBorder, "ToolBar.font", dialogPlain12, "ToolBar.background", control, "ToolBar.foreground", controlText, "ToolBar.shadow", controlShadow, "ToolBar.darkShadow", controlDkShadow, "ToolBar.light", controlHighlight, "ToolBar.highlight", controlLtHighlight, "ToolBar.dockingBackground", control, "ToolBar.dockingForeground", red, "ToolBar.floatingBackground", control, "ToolBar.floatingForeground", darkGray, "ToolBar.border", etchedBorder, "ToolBar.separatorSize", toolBarSeparatorSize, "ToolBar.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "RIGHT", "navigateRight", "KP_RIGHT", "navigateRight"}), "ToolTip.font", sansSerifPlain12, "ToolTip.background", table.get("info"), "ToolTip.foreground", table.get("infoText"), "ToolTip.border", blackLineBorder, "ToolTipManager.enableToolTipMode", "allWindows", "Tree.paintLines", Boolean.TRUE, "Tree.lineTypeDashed", Boolean.FALSE, "Tree.font", dialogPlain12, "Tree.background", window, "Tree.foreground", textText, "Tree.hash", gray, "Tree.textForeground", textText, "Tree.textBackground", table.get("text"), "Tree.selectionForeground", textHighlightText, "Tree.selectionBackground", textHighlight, "Tree.selectionBorderColor", black, "Tree.dropLineColor", controlShadow, "Tree.editorBorder", blackLineBorder, "Tree.leftChildIndent", 7, "Tree.rightChildIndent", 13, "Tree.rowHeight", 16, "Tree.scrollsOnExpand", Boolean.TRUE, "Tree.openIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/TreeOpen.gif"), "Tree.closedIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/TreeClosed.gif"), "Tree.leafIcon", SwingUtilities2.makeIcon(this.getClass(), BasicLookAndFeel.class, "icons/TreeLeaf.gif"), "Tree.expandedIcon", null, "Tree.collapsedIcon", null, "Tree.changeSelectionWithFocus", Boolean.TRUE, "Tree.drawsFocusBorderAroundIcon", Boolean.FALSE, "Tree.timeFactor", oneThousand, "Tree.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "UP", "selectPrevious", "KP_UP", "selectPrevious", "shift UP", "selectPreviousExtendSelection", "shift KP_UP", "selectPreviousExtendSelection", "ctrl shift UP", "selectPreviousExtendSelection", "ctrl shift KP_UP", "selectPreviousExtendSelection", "ctrl UP", "selectPreviousChangeLead", "ctrl KP_UP", "selectPreviousChangeLead", "DOWN", "selectNext", "KP_DOWN", "selectNext", "shift DOWN", "selectNextExtendSelection", "shift KP_DOWN", "selectNextExtendSelection", "ctrl shift DOWN", "selectNextExtendSelection", "ctrl shift KP_DOWN", "selectNextExtendSelection", "ctrl DOWN", "selectNextChangeLead", "ctrl KP_DOWN", "selectNextChangeLead", "RIGHT", "selectChild", "KP_RIGHT", "selectChild", "LEFT", "selectParent", "KP_LEFT", "selectParent", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "HOME", "selectFirst", "shift HOME", "selectFirstExtendSelection", "ctrl shift HOME", "selectFirstExtendSelection", "ctrl HOME", "selectFirstChangeLead", "END", "selectLast", "shift END", "selectLastExtendSelection", "ctrl shift END", "selectLastExtendSelection", "ctrl END", "selectLastChangeLead", "F2", "startEditing", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ctrl LEFT", "scrollLeft", "ctrl KP_LEFT", "scrollLeft", "ctrl RIGHT", "scrollRight", "ctrl KP_RIGHT", "scrollRight", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "Tree.focusInputMap.RightToLeft", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "selectParent", "KP_RIGHT", "selectParent", "LEFT", "selectChild", "KP_LEFT", "selectChild"}), "Tree.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "cancel"}), "RootPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"shift F10", "postPopup", "CONTEXT_MENU", "postPopup"}), "RootPane.defaultButtonWindowKeyBindings", new Object[]{"ENTER", "press", "released ENTER", "release", "ctrl ENTER", "press", "ctrl released ENTER", "release"}};
        table.putDefaults(defaults);
    }

    static int getFocusAcceleratorKeyMask() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            return ((SunToolkit)tk).getFocusAcceleratorKeyMask();
        }
        return 8;
    }

    static Object getUIOfType(ComponentUI ui, Class<?> klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }

    protected ActionMap getAudioActionMap() {
        ActionMap audioActionMap = (ActionMap)UIManager.get("AuditoryCues.actionMap");
        if (audioActionMap == null) {
            Object[] acList = (Object[])UIManager.get("AuditoryCues.cueList");
            if (acList != null) {
                audioActionMap = new ActionMapUIResource();
                for (int counter = acList.length - 1; counter >= 0; --counter) {
                    audioActionMap.put(acList[counter], this.createAudioAction(acList[counter]));
                }
            }
            UIManager.getLookAndFeelDefaults().put("AuditoryCues.actionMap", audioActionMap);
        }
        return audioActionMap;
    }

    protected Action createAudioAction(Object key) {
        if (key != null) {
            String audioKey = (String)key;
            String audioValue = (String)UIManager.get(key);
            return new AudioAction(audioKey, audioValue);
        }
        return null;
    }

    private byte[] loadAudioData(final String soundFile) {
        if (soundFile == null) {
            return null;
        }
        byte[] buffer = AccessController.doPrivileged(new PrivilegedAction<byte[]>(){
            final /* synthetic */ BasicLookAndFeel this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public byte[] run() {
                byte[] byArray;
                InputStream resource = this.this$0.getClass().getResourceAsStream(soundFile);
                if (resource == null) {
                    return null;
                }
                BufferedInputStream in = new BufferedInputStream(resource);
                try {
                    byArray = in.readAllBytes();
                }
                catch (Throwable throwable) {
                    try {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    catch (IOException ioe) {
                        System.err.println(ioe.toString());
                        return null;
                    }
                }
                in.close();
                return byArray;
            }
        });
        if (buffer == null) {
            System.err.println(this.getClass().getName() + "/" + soundFile + " not found.");
            return null;
        }
        if (buffer.length == 0) {
            System.err.println("warning: " + soundFile + " is zero-length");
            return null;
        }
        return buffer;
    }

    protected void playSound(Action audioAction) {
        Object[] audioStrings;
        if (audioAction != null && (audioStrings = (Object[])UIManager.get("AuditoryCues.playList")) != null) {
            HashSet<Object> audioCues = new HashSet<Object>();
            for (Object audioString : audioStrings) {
                audioCues.add(audioString);
            }
            String actionName = (String)audioAction.getValue("Name");
            if (audioCues.contains(actionName)) {
                audioAction.actionPerformed(new ActionEvent(this, 1001, actionName));
            }
        }
    }

    static void installAudioActionMap(ActionMap map) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof BasicLookAndFeel) {
            map.setParent(((BasicLookAndFeel)laf).getAudioActionMap());
        }
    }

    static void playSound(JComponent c, Object actionKey) {
        Action audioAction;
        ActionMap map;
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof BasicLookAndFeel && (map = c.getActionMap()) != null && (audioAction = map.get(actionKey)) != null) {
            ((BasicLookAndFeel)laf).playSound(audioAction);
        }
    }

    class AWTEventHelper
    implements AWTEventListener,
    PrivilegedAction<Object> {
        AWTEventHelper() {
            AccessController.doPrivileged(this);
        }

        @Override
        public Object run() {
            Toolkit tk = Toolkit.getDefaultToolkit();
            if (BasicLookAndFeel.this.invocator == null) {
                tk.addAWTEventListener(this, 16L);
            } else {
                tk.removeAWTEventListener(BasicLookAndFeel.this.invocator);
            }
            return null;
        }

        @Override
        public void eventDispatched(AWTEvent ev) {
            MenuElement[] elems;
            MouseEvent me;
            int eventID = ev.getID();
            if (((long)eventID & 0x10L) != 0L && (me = (MouseEvent)ev).isPopupTrigger()) {
                JPopupMenu componentPopupMenu;
                elems = MenuSelectionManager.defaultManager().getSelectedPath();
                if (elems != null && elems.length != 0) {
                    return;
                }
                Object c = me.getSource();
                JComponent src = null;
                if (c instanceof JComponent) {
                    src = (JComponent)c;
                } else if (c instanceof BasicSplitPaneDivider) {
                    src = (JComponent)((BasicSplitPaneDivider)c).getParent();
                }
                if (src != null && (componentPopupMenu = src.getComponentPopupMenu()) != null) {
                    Point pt = src.getPopupLocation(me);
                    if (pt == null) {
                        pt = me.getPoint();
                        pt = SwingUtilities.convertPoint((Component)c, pt, src);
                    }
                    componentPopupMenu.show(src, pt.x, pt.y);
                    me.consume();
                }
            }
            if (eventID == 501 && (elems = ev.getSource()) instanceof Component) {
                for (Component parent = (Component)elems; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
                    if (!(parent instanceof JInternalFrame)) continue;
                    JInternalFrame internalFrame = (JInternalFrame)parent;
                    try {
                        internalFrame.setSelected(true);
                        continue;
                    }
                    catch (PropertyVetoException propertyVetoException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    private class AudioAction
    extends AbstractAction
    implements LineListener {
        private String audioResource;
        private byte[] audioBuffer;

        public AudioAction(String name, String resource) {
            super(name);
            this.audioResource = resource;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.audioBuffer == null) {
                this.audioBuffer = BasicLookAndFeel.this.loadAudioData(this.audioResource);
            }
            if (this.audioBuffer != null) {
                this.cancelCurrentSound(null);
                try {
                    AudioInputStream soundStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(this.audioBuffer));
                    DataLine.Info info = new DataLine.Info(Clip.class, soundStream.getFormat());
                    Clip clip = (Clip)AudioSystem.getLine(info);
                    clip.open(soundStream);
                    clip.addLineListener(this);
                    Object object = BasicLookAndFeel.this.audioLock;
                    synchronized (object) {
                        BasicLookAndFeel.this.clipPlaying = clip;
                    }
                    clip.start();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        @Override
        public void update(LineEvent event) {
            if (event.getType() == LineEvent.Type.STOP) {
                this.cancelCurrentSound((Clip)event.getLine());
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void cancelCurrentSound(Clip clip) {
            Clip lastClip = null;
            Object object = BasicLookAndFeel.this.audioLock;
            synchronized (object) {
                if (clip == null || clip == BasicLookAndFeel.this.clipPlaying) {
                    lastClip = BasicLookAndFeel.this.clipPlaying;
                    BasicLookAndFeel.this.clipPlaying = null;
                }
            }
            if (lastClip != null) {
                lastClip.removeLineListener(this);
                lastClip.close();
            }
        }
    }
}

