/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.metal;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalFontDesktopProperty;
import javax.swing.plaf.metal.MetalHighContrastTheme;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.MetalUtils;
import javax.swing.plaf.metal.OceanTheme;
import sun.awt.AppContext;
import sun.awt.OSInfo;
import sun.security.action.GetPropertyAction;
import sun.swing.DefaultLayoutStyle;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;

public class MetalLookAndFeel
extends BasicLookAndFeel {
    private static boolean METAL_LOOK_AND_FEEL_INITED = false;
    private static boolean checkedWindows;
    private static boolean isWindows;
    private static boolean checkedSystemFontSettings;
    private static boolean useSystemFonts;
    static ReferenceQueue<LookAndFeel> queue;

    static boolean isWindows() {
        if (!checkedWindows) {
            if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
                isWindows = true;
                String systemFonts = AccessController.doPrivileged(new GetPropertyAction("swing.useSystemFontSettings"));
                useSystemFonts = Boolean.parseBoolean(systemFonts);
            }
            checkedWindows = true;
        }
        return isWindows;
    }

    static boolean useSystemFonts() {
        if (MetalLookAndFeel.isWindows() && useSystemFonts) {
            if (METAL_LOOK_AND_FEEL_INITED) {
                Object value = UIManager.get("Application.useSystemFontSettings");
                return value == null || Boolean.TRUE.equals(value);
            }
            return true;
        }
        return false;
    }

    private static boolean useHighContrastTheme() {
        if (MetalLookAndFeel.isWindows() && MetalLookAndFeel.useSystemFonts()) {
            Boolean highContrast = (Boolean)Toolkit.getDefaultToolkit().getDesktopProperty("win.highContrast.on");
            return highContrast == null ? false : highContrast;
        }
        return false;
    }

    static boolean usingOcean() {
        return MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme;
    }

    @Override
    public String getName() {
        return "Metal";
    }

    @Override
    public String getID() {
        return "Metal";
    }

    @Override
    public String getDescription() {
        return "The Java(tm) Look and Feel";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    @Override
    public boolean getSupportsWindowDecorations() {
        return true;
    }

    @Override
    protected void initClassDefaults(UIDefaults table) {
        super.initClassDefaults(table);
        String metalPackageName = "javax.swing.plaf.metal.";
        Object[] uiDefaults = new Object[]{"ButtonUI", "javax.swing.plaf.metal.MetalButtonUI", "CheckBoxUI", "javax.swing.plaf.metal.MetalCheckBoxUI", "ComboBoxUI", "javax.swing.plaf.metal.MetalComboBoxUI", "DesktopIconUI", "javax.swing.plaf.metal.MetalDesktopIconUI", "FileChooserUI", "javax.swing.plaf.metal.MetalFileChooserUI", "InternalFrameUI", "javax.swing.plaf.metal.MetalInternalFrameUI", "LabelUI", "javax.swing.plaf.metal.MetalLabelUI", "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI", "PopupMenuSeparatorUI", "javax.swing.plaf.metal.MetalPopupMenuSeparatorUI", "ProgressBarUI", "javax.swing.plaf.metal.MetalProgressBarUI", "RadioButtonUI", "javax.swing.plaf.metal.MetalRadioButtonUI", "ScrollBarUI", "javax.swing.plaf.metal.MetalScrollBarUI", "ScrollPaneUI", "javax.swing.plaf.metal.MetalScrollPaneUI", "SeparatorUI", "javax.swing.plaf.metal.MetalSeparatorUI", "SliderUI", "javax.swing.plaf.metal.MetalSliderUI", "SplitPaneUI", "javax.swing.plaf.metal.MetalSplitPaneUI", "TabbedPaneUI", "javax.swing.plaf.metal.MetalTabbedPaneUI", "TextFieldUI", "javax.swing.plaf.metal.MetalTextFieldUI", "ToggleButtonUI", "javax.swing.plaf.metal.MetalToggleButtonUI", "ToolBarUI", "javax.swing.plaf.metal.MetalToolBarUI", "ToolTipUI", "javax.swing.plaf.metal.MetalToolTipUI", "TreeUI", "javax.swing.plaf.metal.MetalTreeUI", "RootPaneUI", "javax.swing.plaf.metal.MetalRootPaneUI"};
        table.putDefaults(uiDefaults);
    }

    @Override
    protected void initSystemColorDefaults(UIDefaults table) {
        MetalTheme theme = MetalLookAndFeel.getCurrentTheme();
        ColorUIResource control = theme.getControl();
        Object[] systemColors = new Object[]{"desktop", theme.getDesktopColor(), "activeCaption", theme.getWindowTitleBackground(), "activeCaptionText", theme.getWindowTitleForeground(), "activeCaptionBorder", theme.getPrimaryControlShadow(), "inactiveCaption", theme.getWindowTitleInactiveBackground(), "inactiveCaptionText", theme.getWindowTitleInactiveForeground(), "inactiveCaptionBorder", theme.getControlShadow(), "window", theme.getWindowBackground(), "windowBorder", control, "windowText", theme.getUserTextColor(), "menu", theme.getMenuBackground(), "menuText", theme.getMenuForeground(), "text", theme.getWindowBackground(), "textText", theme.getUserTextColor(), "textHighlight", theme.getTextHighlightColor(), "textHighlightText", theme.getHighlightedTextColor(), "textInactiveText", theme.getInactiveSystemTextColor(), "control", control, "controlText", theme.getControlTextColor(), "controlHighlight", theme.getControlHighlight(), "controlLtHighlight", theme.getControlHighlight(), "controlShadow", theme.getControlShadow(), "controlDkShadow", theme.getControlDarkShadow(), "scrollbar", control, "info", theme.getPrimaryControl(), "infoText", theme.getPrimaryControlInfo()};
        table.putDefaults(systemColors);
    }

    private void initResourceBundle(UIDefaults table) {
        SwingAccessor.getUIDefaultsAccessor().addInternalBundle(table, "com.sun.swing.internal.plaf.metal.resources.metal");
    }

    @Override
    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        this.initResourceBundle(table);
        ColorUIResource acceleratorForeground = MetalLookAndFeel.getAcceleratorForeground();
        ColorUIResource acceleratorSelectedForeground = MetalLookAndFeel.getAcceleratorSelectedForeground();
        ColorUIResource control = MetalLookAndFeel.getControl();
        ColorUIResource controlHighlight = MetalLookAndFeel.getControlHighlight();
        ColorUIResource controlShadow = MetalLookAndFeel.getControlShadow();
        ColorUIResource controlDarkShadow = MetalLookAndFeel.getControlDarkShadow();
        ColorUIResource controlTextColor = MetalLookAndFeel.getControlTextColor();
        ColorUIResource focusColor = MetalLookAndFeel.getFocusColor();
        ColorUIResource inactiveControlTextColor = MetalLookAndFeel.getInactiveControlTextColor();
        ColorUIResource menuBackground = MetalLookAndFeel.getMenuBackground();
        ColorUIResource menuSelectedBackground = MetalLookAndFeel.getMenuSelectedBackground();
        ColorUIResource menuDisabledForeground = MetalLookAndFeel.getMenuDisabledForeground();
        ColorUIResource menuSelectedForeground = MetalLookAndFeel.getMenuSelectedForeground();
        ColorUIResource primaryControl = MetalLookAndFeel.getPrimaryControl();
        ColorUIResource primaryControlDarkShadow = MetalLookAndFeel.getPrimaryControlDarkShadow();
        ColorUIResource primaryControlShadow = MetalLookAndFeel.getPrimaryControlShadow();
        ColorUIResource systemTextColor = MetalLookAndFeel.getSystemTextColor();
        InsetsUIResource zeroInsets = new InsetsUIResource(0, 0, 0, 0);
        Integer zero = 0;
        UIDefaults.LazyValue textFieldBorder = t -> MetalBorders.getTextFieldBorder();
        UIDefaults.LazyValue dialogBorder = t -> new MetalBorders.DialogBorder();
        UIDefaults.LazyValue questionDialogBorder = t -> new MetalBorders.QuestionDialogBorder();
        UIDefaults.LazyInputMap fieldInputMap = new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy-to-clipboard", "ctrl V", "paste-from-clipboard", "ctrl X", "cut-to-clipboard", "COPY", "copy-to-clipboard", "PASTE", "paste-from-clipboard", "CUT", "cut-to-clipboard", "control INSERT", "copy-to-clipboard", "shift INSERT", "paste-from-clipboard", "shift DELETE", "cut-to-clipboard", "shift LEFT", "selection-backward", "shift KP_LEFT", "selection-backward", "shift RIGHT", "selection-forward", "shift KP_RIGHT", "selection-forward", "ctrl LEFT", "caret-previous-word", "ctrl KP_LEFT", "caret-previous-word", "ctrl RIGHT", "caret-next-word", "ctrl KP_RIGHT", "caret-next-word", "ctrl shift LEFT", "selection-previous-word", "ctrl shift KP_LEFT", "selection-previous-word", "ctrl shift RIGHT", "selection-next-word", "ctrl shift KP_RIGHT", "selection-next-word", "ctrl A", "select-all", "HOME", "caret-begin-line", "END", "caret-end-line", "shift HOME", "selection-begin-line", "shift END", "selection-end-line", "BACK_SPACE", "delete-previous", "shift BACK_SPACE", "delete-previous", "ctrl H", "delete-previous", "DELETE", "delete-next", "ctrl DELETE", "delete-next-word", "ctrl BACK_SPACE", "delete-previous-word", "RIGHT", "caret-forward", "LEFT", "caret-backward", "KP_RIGHT", "caret-forward", "KP_LEFT", "caret-backward", "ENTER", "notify-field-accept", "ctrl BACK_SLASH", "unselect", "control shift O", "toggle-componentOrientation"});
        UIDefaults.LazyInputMap passwordInputMap = new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy-to-clipboard", "ctrl V", "paste-from-clipboard", "ctrl X", "cut-to-clipboard", "COPY", "copy-to-clipboard", "PASTE", "paste-from-clipboard", "CUT", "cut-to-clipboard", "control INSERT", "copy-to-clipboard", "shift INSERT", "paste-from-clipboard", "shift DELETE", "cut-to-clipboard", "shift LEFT", "selection-backward", "shift KP_LEFT", "selection-backward", "shift RIGHT", "selection-forward", "shift KP_RIGHT", "selection-forward", "ctrl LEFT", "caret-begin-line", "ctrl KP_LEFT", "caret-begin-line", "ctrl RIGHT", "caret-end-line", "ctrl KP_RIGHT", "caret-end-line", "ctrl shift LEFT", "selection-begin-line", "ctrl shift KP_LEFT", "selection-begin-line", "ctrl shift RIGHT", "selection-end-line", "ctrl shift KP_RIGHT", "selection-end-line", "ctrl A", "select-all", "HOME", "caret-begin-line", "END", "caret-end-line", "shift HOME", "selection-begin-line", "shift END", "selection-end-line", "BACK_SPACE", "delete-previous", "shift BACK_SPACE", "delete-previous", "ctrl H", "delete-previous", "DELETE", "delete-next", "RIGHT", "caret-forward", "LEFT", "caret-backward", "KP_RIGHT", "caret-forward", "KP_LEFT", "caret-backward", "ENTER", "notify-field-accept", "ctrl BACK_SLASH", "unselect", "control shift O", "toggle-componentOrientation"});
        UIDefaults.LazyInputMap multilineInputMap = new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy-to-clipboard", "ctrl V", "paste-from-clipboard", "ctrl X", "cut-to-clipboard", "COPY", "copy-to-clipboard", "PASTE", "paste-from-clipboard", "CUT", "cut-to-clipboard", "control INSERT", "copy-to-clipboard", "shift INSERT", "paste-from-clipboard", "shift DELETE", "cut-to-clipboard", "shift LEFT", "selection-backward", "shift KP_LEFT", "selection-backward", "shift RIGHT", "selection-forward", "shift KP_RIGHT", "selection-forward", "ctrl LEFT", "caret-previous-word", "ctrl KP_LEFT", "caret-previous-word", "ctrl RIGHT", "caret-next-word", "ctrl KP_RIGHT", "caret-next-word", "ctrl shift LEFT", "selection-previous-word", "ctrl shift KP_LEFT", "selection-previous-word", "ctrl shift RIGHT", "selection-next-word", "ctrl shift KP_RIGHT", "selection-next-word", "ctrl A", "select-all", "HOME", "caret-begin-line", "END", "caret-end-line", "shift HOME", "selection-begin-line", "shift END", "selection-end-line", "UP", "caret-up", "KP_UP", "caret-up", "DOWN", "caret-down", "KP_DOWN", "caret-down", "PAGE_UP", "page-up", "PAGE_DOWN", "page-down", "shift PAGE_UP", "selection-page-up", "shift PAGE_DOWN", "selection-page-down", "ctrl shift PAGE_UP", "selection-page-left", "ctrl shift PAGE_DOWN", "selection-page-right", "shift UP", "selection-up", "shift KP_UP", "selection-up", "shift DOWN", "selection-down", "shift KP_DOWN", "selection-down", "ENTER", "insert-break", "BACK_SPACE", "delete-previous", "shift BACK_SPACE", "delete-previous", "ctrl H", "delete-previous", "DELETE", "delete-next", "ctrl DELETE", "delete-next-word", "ctrl BACK_SPACE", "delete-previous-word", "RIGHT", "caret-forward", "LEFT", "caret-backward", "KP_RIGHT", "caret-forward", "KP_LEFT", "caret-backward", "TAB", "insert-tab", "ctrl BACK_SLASH", "unselect", "ctrl HOME", "caret-begin", "ctrl END", "caret-end", "ctrl shift HOME", "selection-begin", "ctrl shift END", "selection-end", "ctrl T", "next-link-action", "ctrl shift T", "previous-link-action", "ctrl SPACE", "activate-link-action", "control shift O", "toggle-componentOrientation"});
        UIDefaults.LazyValue scrollPaneBorder = t -> new MetalBorders.ScrollPaneBorder();
        UIDefaults.LazyValue buttonBorder = t -> MetalBorders.getButtonBorder();
        UIDefaults.LazyValue toggleButtonBorder = t -> MetalBorders.getToggleButtonBorder();
        UIDefaults.LazyValue titledBorderBorder = t -> new BorderUIResource.LineBorderUIResource(controlShadow);
        UIDefaults.LazyValue desktopIconBorder = t -> MetalBorders.getDesktopIconBorder();
        UIDefaults.LazyValue menuBarBorder = t -> new MetalBorders.MenuBarBorder();
        UIDefaults.LazyValue popupMenuBorder = t -> new MetalBorders.PopupMenuBorder();
        UIDefaults.LazyValue menuItemBorder = t -> new MetalBorders.MenuItemBorder();
        String menuItemAcceleratorDelimiter = "-";
        UIDefaults.LazyValue toolBarBorder = t -> new MetalBorders.ToolBarBorder();
        UIDefaults.LazyValue progressBarBorder = t -> new BorderUIResource.LineBorderUIResource(controlDarkShadow, 1);
        UIDefaults.LazyValue toolTipBorder = t -> new BorderUIResource.LineBorderUIResource(primaryControlDarkShadow);
        UIDefaults.LazyValue toolTipBorderInactive = t -> new BorderUIResource.LineBorderUIResource(controlDarkShadow);
        UIDefaults.LazyValue focusCellHighlightBorder = t -> new BorderUIResource.LineBorderUIResource(focusColor);
        InsetsUIResource tabbedPaneTabAreaInsets = new InsetsUIResource(4, 2, 0, 6);
        InsetsUIResource tabbedPaneTabInsets = new InsetsUIResource(0, 9, 1, 9);
        int internalFrameIconSize = 16;
        Object[] defaultCueList = new Object[]{"OptionPane.errorSound", "OptionPane.informationSound", "OptionPane.questionSound", "OptionPane.warningSound"};
        MetalTheme theme = MetalLookAndFeel.getCurrentTheme();
        FontActiveValue menuTextValue = new FontActiveValue(theme, 3);
        FontActiveValue controlTextValue = new FontActiveValue(theme, 0);
        FontActiveValue userTextValue = new FontActiveValue(theme, 2);
        FontActiveValue windowTitleValue = new FontActiveValue(theme, 4);
        FontActiveValue subTextValue = new FontActiveValue(theme, 5);
        FontActiveValue systemTextValue = new FontActiveValue(theme, 1);
        Object[] defaults = new Object[]{"AuditoryCues.defaultCueList", defaultCueList, "AuditoryCues.playList", null, "TextField.border", textFieldBorder, "TextField.font", userTextValue, "PasswordField.border", textFieldBorder, "PasswordField.font", userTextValue, "PasswordField.echoChar", Character.valueOf('\u2022'), "TextArea.font", userTextValue, "TextPane.background", table.get("window"), "TextPane.font", userTextValue, "EditorPane.background", table.get("window"), "EditorPane.font", userTextValue, "TextField.focusInputMap", fieldInputMap, "PasswordField.focusInputMap", passwordInputMap, "TextArea.focusInputMap", multilineInputMap, "TextPane.focusInputMap", multilineInputMap, "EditorPane.focusInputMap", multilineInputMap, "FormattedTextField.border", textFieldBorder, "FormattedTextField.font", userTextValue, "FormattedTextField.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy-to-clipboard", "ctrl V", "paste-from-clipboard", "ctrl X", "cut-to-clipboard", "COPY", "copy-to-clipboard", "PASTE", "paste-from-clipboard", "CUT", "cut-to-clipboard", "control INSERT", "copy-to-clipboard", "shift INSERT", "paste-from-clipboard", "shift DELETE", "cut-to-clipboard", "shift LEFT", "selection-backward", "shift KP_LEFT", "selection-backward", "shift RIGHT", "selection-forward", "shift KP_RIGHT", "selection-forward", "ctrl LEFT", "caret-previous-word", "ctrl KP_LEFT", "caret-previous-word", "ctrl RIGHT", "caret-next-word", "ctrl KP_RIGHT", "caret-next-word", "ctrl shift LEFT", "selection-previous-word", "ctrl shift KP_LEFT", "selection-previous-word", "ctrl shift RIGHT", "selection-next-word", "ctrl shift KP_RIGHT", "selection-next-word", "ctrl A", "select-all", "HOME", "caret-begin-line", "END", "caret-end-line", "shift HOME", "selection-begin-line", "shift END", "selection-end-line", "BACK_SPACE", "delete-previous", "shift BACK_SPACE", "delete-previous", "ctrl H", "delete-previous", "DELETE", "delete-next", "ctrl DELETE", "delete-next-word", "ctrl BACK_SPACE", "delete-previous-word", "RIGHT", "caret-forward", "LEFT", "caret-backward", "KP_RIGHT", "caret-forward", "KP_LEFT", "caret-backward", "ENTER", "notify-field-accept", "ctrl BACK_SLASH", "unselect", "control shift O", "toggle-componentOrientation", "ESCAPE", "reset-field-edit", "UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "Button.defaultButtonFollowsFocus", Boolean.FALSE, "Button.disabledText", inactiveControlTextColor, "Button.select", controlShadow, "Button.border", buttonBorder, "Button.font", controlTextValue, "Button.focus", focusColor, "Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "CheckBox.disabledText", inactiveControlTextColor, "Checkbox.select", controlShadow, "CheckBox.font", controlTextValue, "CheckBox.focus", focusColor, "CheckBox.icon", t -> MetalIconFactory.getCheckBoxIcon(), "CheckBox.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "CheckBox.totalInsets", new Insets(4, 4, 4, 4), "RadioButton.disabledText", inactiveControlTextColor, "RadioButton.select", controlShadow, "RadioButton.icon", t -> MetalIconFactory.getRadioButtonIcon(), "RadioButton.font", controlTextValue, "RadioButton.focus", focusColor, "RadioButton.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "RadioButton.totalInsets", new Insets(4, 4, 4, 4), "ToggleButton.select", controlShadow, "ToggleButton.disabledText", inactiveControlTextColor, "ToggleButton.focus", focusColor, "ToggleButton.border", toggleButtonBorder, "ToggleButton.font", controlTextValue, "ToggleButton.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "FileView.directoryIcon", t -> MetalIconFactory.getTreeFolderIcon(), "FileView.fileIcon", t -> MetalIconFactory.getTreeLeafIcon(), "FileView.computerIcon", t -> MetalIconFactory.getTreeComputerIcon(), "FileView.hardDriveIcon", t -> MetalIconFactory.getTreeHardDriveIcon(), "FileView.floppyDriveIcon", t -> MetalIconFactory.getTreeFloppyDriveIcon(), "FileChooser.detailsViewIcon", t -> MetalIconFactory.getFileChooserDetailViewIcon(), "FileChooser.homeFolderIcon", t -> MetalIconFactory.getFileChooserHomeFolderIcon(), "FileChooser.listViewIcon", t -> MetalIconFactory.getFileChooserListViewIcon(), "FileChooser.newFolderIcon", t -> MetalIconFactory.getFileChooserNewFolderIcon(), "FileChooser.upFolderIcon", t -> MetalIconFactory.getFileChooserUpFolderIcon(), "FileChooser.usesSingleFilePane", Boolean.TRUE, "FileChooser.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "cancelSelection", "F2", "editFileName", "F5", "refresh", "BACK_SPACE", "Go Up"}), "ToolTip.font", systemTextValue, "ToolTip.border", toolTipBorder, "ToolTip.borderInactive", toolTipBorderInactive, "ToolTip.backgroundInactive", control, "ToolTip.foregroundInactive", controlDarkShadow, "ToolTip.hideAccelerator", Boolean.FALSE, "ToolTipManager.enableToolTipMode", "activeApplication", "Slider.font", controlTextValue, "Slider.border", null, "Slider.foreground", primaryControlShadow, "Slider.focus", focusColor, "Slider.focusInsets", zeroInsets, "Slider.trackWidth", 7, "Slider.majorTickLength", 6, "Slider.horizontalThumbIcon", t -> MetalIconFactory.getHorizontalSliderThumbIcon(), "Slider.verticalThumbIcon", t -> MetalIconFactory.getVerticalSliderThumbIcon(), "Slider.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "negativeUnitIncrement", "KP_DOWN", "negativeUnitIncrement", "PAGE_DOWN", "negativeBlockIncrement", "ctrl PAGE_DOWN", "negativeBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "positiveUnitIncrement", "KP_UP", "positiveUnitIncrement", "PAGE_UP", "positiveBlockIncrement", "ctrl PAGE_UP", "positiveBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "ProgressBar.font", controlTextValue, "ProgressBar.foreground", primaryControlShadow, "ProgressBar.selectionBackground", primaryControlDarkShadow, "ProgressBar.border", progressBarBorder, "ProgressBar.cellSpacing", zero, "ProgressBar.cellLength", 1, "ComboBox.background", control, "ComboBox.foreground", controlTextColor, "ComboBox.selectionBackground", primaryControlShadow, "ComboBox.selectionForeground", controlTextColor, "ComboBox.font", controlTextValue, "ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "hidePopup", "PAGE_UP", "pageUpPassThrough", "PAGE_DOWN", "pageDownPassThrough", "HOME", "homePassThrough", "END", "endPassThrough", "DOWN", "selectNext", "KP_DOWN", "selectNext", "alt DOWN", "togglePopup", "alt KP_DOWN", "togglePopup", "alt UP", "togglePopup", "alt KP_UP", "togglePopup", "SPACE", "spacePopup", "ENTER", "enterPressed", "UP", "selectPrevious", "KP_UP", "selectPrevious"}), "InternalFrame.icon", t -> MetalIconFactory.getInternalFrameDefaultMenuIcon(), "InternalFrame.border", t -> new MetalBorders.InternalFrameBorder(), "InternalFrame.optionDialogBorder", t -> new MetalBorders.OptionDialogBorder(), "InternalFrame.paletteBorder", t -> new MetalBorders.PaletteBorder(), "InternalFrame.paletteTitleHeight", 11, "InternalFrame.paletteCloseIcon", t -> new MetalIconFactory.PaletteCloseIcon(), "InternalFrame.closeIcon", t -> MetalIconFactory.getInternalFrameCloseIcon(internalFrameIconSize), "InternalFrame.maximizeIcon", t -> MetalIconFactory.getInternalFrameMaximizeIcon(internalFrameIconSize), "InternalFrame.iconifyIcon", t -> MetalIconFactory.getInternalFrameMinimizeIcon(internalFrameIconSize), "InternalFrame.minimizeIcon", t -> MetalIconFactory.getInternalFrameAltMaximizeIcon(internalFrameIconSize), "InternalFrame.titleFont", windowTitleValue, "InternalFrame.windowBindings", null, "InternalFrame.closeSound", "sounds/FrameClose.wav", "InternalFrame.maximizeSound", "sounds/FrameMaximize.wav", "InternalFrame.minimizeSound", "sounds/FrameMinimize.wav", "InternalFrame.restoreDownSound", "sounds/FrameRestoreDown.wav", "InternalFrame.restoreUpSound", "sounds/FrameRestoreUp.wav", "DesktopIcon.border", desktopIconBorder, "DesktopIcon.font", controlTextValue, "DesktopIcon.foreground", controlTextColor, "DesktopIcon.background", control, "DesktopIcon.width", 160, "Desktop.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl F5", "restore", "ctrl F4", "close", "ctrl F7", "move", "ctrl F8", "resize", "RIGHT", "right", "KP_RIGHT", "right", "shift RIGHT", "shrinkRight", "shift KP_RIGHT", "shrinkRight", "LEFT", "left", "KP_LEFT", "left", "shift LEFT", "shrinkLeft", "shift KP_LEFT", "shrinkLeft", "UP", "up", "KP_UP", "up", "shift UP", "shrinkUp", "shift KP_UP", "shrinkUp", "DOWN", "down", "KP_DOWN", "down", "shift DOWN", "shrinkDown", "shift KP_DOWN", "shrinkDown", "ESCAPE", "escape", "ctrl F9", "minimize", "ctrl F10", "maximize", "ctrl F6", "selectNextFrame", "ctrl TAB", "selectNextFrame", "ctrl alt F6", "selectNextFrame", "shift ctrl alt F6", "selectPreviousFrame", "ctrl F12", "navigateNext", "shift ctrl F12", "navigatePrevious"}), "TitledBorder.font", controlTextValue, "TitledBorder.titleColor", systemTextColor, "TitledBorder.border", titledBorderBorder, "Label.font", controlTextValue, "Label.foreground", systemTextColor, "Label.disabledForeground", MetalLookAndFeel.getInactiveSystemTextColor(), "List.font", controlTextValue, "List.focusCellHighlightBorder", focusCellHighlightBorder, "List.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "HOME", "selectFirstRow", "shift HOME", "selectFirstRowExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRowChangeLead", "END", "selectLastRow", "shift END", "selectLastRowExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRowChangeLead", "PAGE_UP", "scrollUp", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDown", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "ScrollBar.background", control, "ScrollBar.highlight", controlHighlight, "ScrollBar.shadow", controlShadow, "ScrollBar.darkShadow", controlDarkShadow, "ScrollBar.thumb", primaryControlShadow, "ScrollBar.thumbShadow", primaryControlDarkShadow, "ScrollBar.thumbHighlight", primaryControl, "ScrollBar.width", 17, "ScrollBar.allowsAbsolutePositioning", Boolean.TRUE, "ScrollBar.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "positiveUnitIncrement", "KP_DOWN", "positiveUnitIncrement", "PAGE_DOWN", "positiveBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "negativeUnitIncrement", "KP_UP", "negativeUnitIncrement", "PAGE_UP", "negativeBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "ScrollPane.border", scrollPaneBorder, "ScrollPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "unitScrollRight", "KP_RIGHT", "unitScrollRight", "DOWN", "unitScrollDown", "KP_DOWN", "unitScrollDown", "LEFT", "unitScrollLeft", "KP_LEFT", "unitScrollLeft", "UP", "unitScrollUp", "KP_UP", "unitScrollUp", "PAGE_UP", "scrollUp", "PAGE_DOWN", "scrollDown", "ctrl PAGE_UP", "scrollLeft", "ctrl PAGE_DOWN", "scrollRight", "ctrl HOME", "scrollHome", "ctrl END", "scrollEnd"}), "TabbedPane.font", controlTextValue, "TabbedPane.tabAreaBackground", control, "TabbedPane.background", controlShadow, "TabbedPane.light", control, "TabbedPane.focus", primaryControlDarkShadow, "TabbedPane.selected", control, "TabbedPane.selectHighlight", controlHighlight, "TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets, "TabbedPane.tabInsets", tabbedPaneTabInsets, "TabbedPane.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"RIGHT", "navigateRight", "KP_RIGHT", "navigateRight", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "ctrl DOWN", "requestFocusForVisibleComponent", "ctrl KP_DOWN", "requestFocusForVisibleComponent"}), "TabbedPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl PAGE_DOWN", "navigatePageDown", "ctrl PAGE_UP", "navigatePageUp", "ctrl UP", "requestFocus", "ctrl KP_UP", "requestFocus"}), "Table.font", userTextValue, "Table.focusCellHighlightBorder", focusCellHighlightBorder, "Table.scrollPaneBorder", scrollPaneBorder, "Table.dropLineColor", focusColor, "Table.dropLineShortColor", primaryControlDarkShadow, "Table.gridColor", controlShadow, "Table.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "HOME", "selectFirstColumn", "shift HOME", "selectFirstColumnExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRow", "END", "selectLastColumn", "shift END", "selectLastColumnExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRow", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollLeftExtendSelection", "ctrl PAGE_UP", "scrollLeftChangeSelection", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollRightExtendSelection", "ctrl PAGE_DOWN", "scrollRightChangeSelection", "TAB", "selectNextColumnCell", "shift TAB", "selectPreviousColumnCell", "ENTER", "selectNextRowCell", "shift ENTER", "selectPreviousRowCell", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ESCAPE", "cancel", "F2", "startEditing", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo", "F8", "focusHeader"}), "Table.ascendingSortIcon", SwingUtilities2.makeIcon(this.getClass(), MetalLookAndFeel.class, "icons/sortUp.png"), "Table.descendingSortIcon", SwingUtilities2.makeIcon(this.getClass(), MetalLookAndFeel.class, "icons/sortDown.png"), "TableHeader.font", userTextValue, "TableHeader.cellBorder", t -> new MetalBorders.TableHeaderBorder(), "MenuBar.border", menuBarBorder, "MenuBar.font", menuTextValue, "MenuBar.windowBindings", new Object[]{"F10", "takeFocus"}, "Menu.border", menuItemBorder, "Menu.borderPainted", Boolean.TRUE, "Menu.menuPopupOffsetX", zero, "Menu.menuPopupOffsetY", zero, "Menu.submenuPopupOffsetX", -4, "Menu.submenuPopupOffsetY", -3, "Menu.font", menuTextValue, "Menu.selectionForeground", menuSelectedForeground, "Menu.selectionBackground", menuSelectedBackground, "Menu.disabledForeground", menuDisabledForeground, "Menu.acceleratorFont", subTextValue, "Menu.acceleratorForeground", acceleratorForeground, "Menu.acceleratorSelectionForeground", acceleratorSelectedForeground, "Menu.checkIcon", t -> MetalIconFactory.getMenuItemCheckIcon(), "Menu.arrowIcon", t -> MetalIconFactory.getMenuArrowIcon(), "MenuItem.border", menuItemBorder, "MenuItem.borderPainted", Boolean.TRUE, "MenuItem.font", menuTextValue, "MenuItem.selectionForeground", menuSelectedForeground, "MenuItem.selectionBackground", menuSelectedBackground, "MenuItem.disabledForeground", menuDisabledForeground, "MenuItem.acceleratorFont", subTextValue, "MenuItem.acceleratorForeground", acceleratorForeground, "MenuItem.acceleratorSelectionForeground", acceleratorSelectedForeground, "MenuItem.acceleratorDelimiter", menuItemAcceleratorDelimiter, "MenuItem.checkIcon", t -> MetalIconFactory.getMenuItemCheckIcon(), "MenuItem.arrowIcon", t -> MetalIconFactory.getMenuItemArrowIcon(), "MenuItem.commandSound", "sounds/MenuItemCommand.wav", "OptionPane.windowBindings", new Object[]{"ESCAPE", "close"}, "OptionPane.informationSound", "sounds/OptionPaneInformation.wav", "OptionPane.warningSound", "sounds/OptionPaneWarning.wav", "OptionPane.errorSound", "sounds/OptionPaneError.wav", "OptionPane.questionSound", "sounds/OptionPaneQuestion.wav", "OptionPane.errorDialog.border.background", new ColorUIResource(153, 51, 51), "OptionPane.errorDialog.titlePane.foreground", new ColorUIResource(51, 0, 0), "OptionPane.errorDialog.titlePane.background", new ColorUIResource(255, 153, 153), "OptionPane.errorDialog.titlePane.shadow", new ColorUIResource(204, 102, 102), "OptionPane.questionDialog.border.background", new ColorUIResource(51, 102, 51), "OptionPane.questionDialog.titlePane.foreground", new ColorUIResource(0, 51, 0), "OptionPane.questionDialog.titlePane.background", new ColorUIResource(153, 204, 153), "OptionPane.questionDialog.titlePane.shadow", new ColorUIResource(102, 153, 102), "OptionPane.warningDialog.border.background", new ColorUIResource(153, 102, 51), "OptionPane.warningDialog.titlePane.foreground", new ColorUIResource(102, 51, 0), "OptionPane.warningDialog.titlePane.background", new ColorUIResource(255, 204, 153), "OptionPane.warningDialog.titlePane.shadow", new ColorUIResource(204, 153, 102), "Separator.background", MetalLookAndFeel.getSeparatorBackground(), "Separator.foreground", MetalLookAndFeel.getSeparatorForeground(), "PopupMenu.border", popupMenuBorder, "PopupMenu.popupSound", "sounds/PopupMenuPopup.wav", "PopupMenu.font", menuTextValue, "CheckBoxMenuItem.border", menuItemBorder, "CheckBoxMenuItem.borderPainted", Boolean.TRUE, "CheckBoxMenuItem.font", menuTextValue, "CheckBoxMenuItem.selectionForeground", menuSelectedForeground, "CheckBoxMenuItem.selectionBackground", menuSelectedBackground, "CheckBoxMenuItem.disabledForeground", menuDisabledForeground, "CheckBoxMenuItem.acceleratorFont", subTextValue, "CheckBoxMenuItem.acceleratorForeground", acceleratorForeground, "CheckBoxMenuItem.acceleratorSelectionForeground", acceleratorSelectedForeground, "CheckBoxMenuItem.checkIcon", t -> MetalIconFactory.getCheckBoxMenuItemIcon(), "CheckBoxMenuItem.arrowIcon", t -> MetalIconFactory.getMenuItemArrowIcon(), "CheckBoxMenuItem.commandSound", "sounds/MenuItemCommand.wav", "RadioButtonMenuItem.border", menuItemBorder, "RadioButtonMenuItem.borderPainted", Boolean.TRUE, "RadioButtonMenuItem.font", menuTextValue, "RadioButtonMenuItem.selectionForeground", menuSelectedForeground, "RadioButtonMenuItem.selectionBackground", menuSelectedBackground, "RadioButtonMenuItem.disabledForeground", menuDisabledForeground, "RadioButtonMenuItem.acceleratorFont", subTextValue, "RadioButtonMenuItem.acceleratorForeground", acceleratorForeground, "RadioButtonMenuItem.acceleratorSelectionForeground", acceleratorSelectedForeground, "RadioButtonMenuItem.checkIcon", t -> MetalIconFactory.getRadioButtonMenuItemIcon(), "RadioButtonMenuItem.arrowIcon", t -> MetalIconFactory.getMenuItemArrowIcon(), "RadioButtonMenuItem.commandSound", "sounds/MenuItemCommand.wav", "Spinner.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "Spinner.arrowButtonInsets", zeroInsets, "Spinner.border", textFieldBorder, "Spinner.arrowButtonBorder", buttonBorder, "Spinner.font", controlTextValue, "SplitPane.dividerSize", 10, "SplitPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "negativeIncrement", "DOWN", "positiveIncrement", "LEFT", "negativeIncrement", "RIGHT", "positiveIncrement", "KP_UP", "negativeIncrement", "KP_DOWN", "positiveIncrement", "KP_LEFT", "negativeIncrement", "KP_RIGHT", "positiveIncrement", "HOME", "selectMin", "END", "selectMax", "F8", "startResize", "F6", "toggleFocus", "ctrl TAB", "focusOutForward", "ctrl shift TAB", "focusOutBackward"}), "SplitPane.centerOneTouchButtons", Boolean.FALSE, "SplitPane.dividerFocusColor", primaryControl, "Tree.font", userTextValue, "Tree.textBackground", MetalLookAndFeel.getWindowBackground(), "Tree.selectionBorderColor", focusColor, "Tree.openIcon", t -> MetalIconFactory.getTreeFolderIcon(), "Tree.closedIcon", t -> MetalIconFactory.getTreeFolderIcon(), "Tree.leafIcon", t -> MetalIconFactory.getTreeLeafIcon(), "Tree.expandedIcon", t -> MetalIconFactory.getTreeControlIcon(false), "Tree.collapsedIcon", t -> MetalIconFactory.getTreeControlIcon(true), "Tree.line", primaryControl, "Tree.hash", primaryControl, "Tree.rowHeight", zero, "Tree.focusInputMap", new UIDefaults.LazyInputMap(new Object[]{"ADD", "expand", "SUBTRACT", "collapse", "ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "control INSERT", "copy", "shift INSERT", "paste", "shift DELETE", "cut", "UP", "selectPrevious", "KP_UP", "selectPrevious", "shift UP", "selectPreviousExtendSelection", "shift KP_UP", "selectPreviousExtendSelection", "ctrl shift UP", "selectPreviousExtendSelection", "ctrl shift KP_UP", "selectPreviousExtendSelection", "ctrl UP", "selectPreviousChangeLead", "ctrl KP_UP", "selectPreviousChangeLead", "DOWN", "selectNext", "KP_DOWN", "selectNext", "shift DOWN", "selectNextExtendSelection", "shift KP_DOWN", "selectNextExtendSelection", "ctrl shift DOWN", "selectNextExtendSelection", "ctrl shift KP_DOWN", "selectNextExtendSelection", "ctrl DOWN", "selectNextChangeLead", "ctrl KP_DOWN", "selectNextChangeLead", "RIGHT", "selectChild", "KP_RIGHT", "selectChild", "LEFT", "selectParent", "KP_LEFT", "selectParent", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "HOME", "selectFirst", "shift HOME", "selectFirstExtendSelection", "ctrl shift HOME", "selectFirstExtendSelection", "ctrl HOME", "selectFirstChangeLead", "END", "selectLast", "shift END", "selectLastExtendSelection", "ctrl shift END", "selectLastExtendSelection", "ctrl END", "selectLastChangeLead", "F2", "startEditing", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ctrl LEFT", "scrollLeft", "ctrl KP_LEFT", "scrollLeft", "ctrl RIGHT", "scrollRight", "ctrl KP_RIGHT", "scrollRight", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "Tree.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"ESCAPE", "cancel"}), "ToolBar.border", toolBarBorder, "ToolBar.background", menuBackground, "ToolBar.foreground", MetalLookAndFeel.getMenuForeground(), "ToolBar.font", menuTextValue, "ToolBar.dockingBackground", menuBackground, "ToolBar.floatingBackground", menuBackground, "ToolBar.dockingForeground", primaryControlDarkShadow, "ToolBar.floatingForeground", primaryControl, "ToolBar.rolloverBorder", t -> MetalBorders.getToolBarRolloverBorder(), "ToolBar.nonrolloverBorder", t -> MetalBorders.getToolBarNonrolloverBorder(), "ToolBar.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{"UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "RIGHT", "navigateRight", "KP_RIGHT", "navigateRight"}), "RootPane.frameBorder", t -> new MetalBorders.FrameBorder(), "RootPane.plainDialogBorder", dialogBorder, "RootPane.informationDialogBorder", dialogBorder, "RootPane.errorDialogBorder", t -> new MetalBorders.ErrorDialogBorder(), "RootPane.colorChooserDialogBorder", questionDialogBorder, "RootPane.fileChooserDialogBorder", questionDialogBorder, "RootPane.questionDialogBorder", questionDialogBorder, "RootPane.warningDialogBorder", t -> new MetalBorders.WarningDialogBorder(), "RootPane.defaultButtonWindowKeyBindings", new Object[]{"ENTER", "press", "released ENTER", "release", "ctrl ENTER", "press", "ctrl released ENTER", "release"}};
        table.putDefaults(defaults);
        if (MetalLookAndFeel.isWindows() && MetalLookAndFeel.useSystemFonts() && theme.isSystemTheme()) {
            MetalFontDesktopProperty messageFont = new MetalFontDesktopProperty("win.messagebox.font.height", 0);
            defaults = new Object[]{"OptionPane.messageFont", messageFont, "OptionPane.buttonFont", messageFont};
            table.putDefaults(defaults);
        }
        MetalLookAndFeel.flushUnreferenced();
        boolean lafCond = SwingUtilities2.isLocalDisplay();
        SwingUtilities2.putAATextInfo(lafCond, table);
        new AATextListener(this);
    }

    protected void createDefaultTheme() {
        MetalLookAndFeel.getCurrentTheme();
    }

    @Override
    public UIDefaults getDefaults() {
        METAL_LOOK_AND_FEEL_INITED = true;
        this.createDefaultTheme();
        UIDefaults table = super.getDefaults();
        MetalTheme currentTheme = MetalLookAndFeel.getCurrentTheme();
        currentTheme.addCustomEntriesToTable(table);
        currentTheme.install();
        return table;
    }

    @Override
    public void provideErrorFeedback(Component component) {
        super.provideErrorFeedback(component);
    }

    public static void setCurrentTheme(MetalTheme theme) {
        if (theme == null) {
            throw new NullPointerException("Can't have null theme");
        }
        AppContext.getAppContext().put("currentMetalTheme", theme);
    }

    public static MetalTheme getCurrentTheme() {
        AppContext context = AppContext.getAppContext();
        MetalTheme currentTheme = (MetalTheme)context.get("currentMetalTheme");
        if (currentTheme == null) {
            String theme;
            currentTheme = MetalLookAndFeel.useHighContrastTheme() ? new MetalHighContrastTheme() : ("steel".equals(theme = AccessController.doPrivileged(new GetPropertyAction("swing.metalTheme"))) ? new DefaultMetalTheme() : new OceanTheme());
            MetalLookAndFeel.setCurrentTheme(currentTheme);
        }
        return currentTheme;
    }

    @Override
    public Icon getDisabledIcon(JComponent component, Icon icon) {
        if (icon instanceof ImageIcon && MetalLookAndFeel.usingOcean()) {
            return MetalUtils.getOceanDisabledButtonIcon(((ImageIcon)icon).getImage());
        }
        return super.getDisabledIcon(component, icon);
    }

    @Override
    public Icon getDisabledSelectedIcon(JComponent component, Icon icon) {
        if (icon instanceof ImageIcon && MetalLookAndFeel.usingOcean()) {
            return MetalUtils.getOceanDisabledButtonIcon(((ImageIcon)icon).getImage());
        }
        return super.getDisabledSelectedIcon(component, icon);
    }

    public static FontUIResource getControlTextFont() {
        return MetalLookAndFeel.getCurrentTheme().getControlTextFont();
    }

    public static FontUIResource getSystemTextFont() {
        return MetalLookAndFeel.getCurrentTheme().getSystemTextFont();
    }

    public static FontUIResource getUserTextFont() {
        return MetalLookAndFeel.getCurrentTheme().getUserTextFont();
    }

    public static FontUIResource getMenuTextFont() {
        return MetalLookAndFeel.getCurrentTheme().getMenuTextFont();
    }

    public static FontUIResource getWindowTitleFont() {
        return MetalLookAndFeel.getCurrentTheme().getWindowTitleFont();
    }

    public static FontUIResource getSubTextFont() {
        return MetalLookAndFeel.getCurrentTheme().getSubTextFont();
    }

    public static ColorUIResource getDesktopColor() {
        return MetalLookAndFeel.getCurrentTheme().getDesktopColor();
    }

    public static ColorUIResource getFocusColor() {
        return MetalLookAndFeel.getCurrentTheme().getFocusColor();
    }

    public static ColorUIResource getWhite() {
        return MetalLookAndFeel.getCurrentTheme().getWhite();
    }

    public static ColorUIResource getBlack() {
        return MetalLookAndFeel.getCurrentTheme().getBlack();
    }

    public static ColorUIResource getControl() {
        return MetalLookAndFeel.getCurrentTheme().getControl();
    }

    public static ColorUIResource getControlShadow() {
        return MetalLookAndFeel.getCurrentTheme().getControlShadow();
    }

    public static ColorUIResource getControlDarkShadow() {
        return MetalLookAndFeel.getCurrentTheme().getControlDarkShadow();
    }

    public static ColorUIResource getControlInfo() {
        return MetalLookAndFeel.getCurrentTheme().getControlInfo();
    }

    public static ColorUIResource getControlHighlight() {
        return MetalLookAndFeel.getCurrentTheme().getControlHighlight();
    }

    public static ColorUIResource getControlDisabled() {
        return MetalLookAndFeel.getCurrentTheme().getControlDisabled();
    }

    public static ColorUIResource getPrimaryControl() {
        return MetalLookAndFeel.getCurrentTheme().getPrimaryControl();
    }

    public static ColorUIResource getPrimaryControlShadow() {
        return MetalLookAndFeel.getCurrentTheme().getPrimaryControlShadow();
    }

    public static ColorUIResource getPrimaryControlDarkShadow() {
        return MetalLookAndFeel.getCurrentTheme().getPrimaryControlDarkShadow();
    }

    public static ColorUIResource getPrimaryControlInfo() {
        return MetalLookAndFeel.getCurrentTheme().getPrimaryControlInfo();
    }

    public static ColorUIResource getPrimaryControlHighlight() {
        return MetalLookAndFeel.getCurrentTheme().getPrimaryControlHighlight();
    }

    public static ColorUIResource getSystemTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getSystemTextColor();
    }

    public static ColorUIResource getControlTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getControlTextColor();
    }

    public static ColorUIResource getInactiveControlTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getInactiveControlTextColor();
    }

    public static ColorUIResource getInactiveSystemTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getInactiveSystemTextColor();
    }

    public static ColorUIResource getUserTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getUserTextColor();
    }

    public static ColorUIResource getTextHighlightColor() {
        return MetalLookAndFeel.getCurrentTheme().getTextHighlightColor();
    }

    public static ColorUIResource getHighlightedTextColor() {
        return MetalLookAndFeel.getCurrentTheme().getHighlightedTextColor();
    }

    public static ColorUIResource getWindowBackground() {
        return MetalLookAndFeel.getCurrentTheme().getWindowBackground();
    }

    public static ColorUIResource getWindowTitleBackground() {
        return MetalLookAndFeel.getCurrentTheme().getWindowTitleBackground();
    }

    public static ColorUIResource getWindowTitleForeground() {
        return MetalLookAndFeel.getCurrentTheme().getWindowTitleForeground();
    }

    public static ColorUIResource getWindowTitleInactiveBackground() {
        return MetalLookAndFeel.getCurrentTheme().getWindowTitleInactiveBackground();
    }

    public static ColorUIResource getWindowTitleInactiveForeground() {
        return MetalLookAndFeel.getCurrentTheme().getWindowTitleInactiveForeground();
    }

    public static ColorUIResource getMenuBackground() {
        return MetalLookAndFeel.getCurrentTheme().getMenuBackground();
    }

    public static ColorUIResource getMenuForeground() {
        return MetalLookAndFeel.getCurrentTheme().getMenuForeground();
    }

    public static ColorUIResource getMenuSelectedBackground() {
        return MetalLookAndFeel.getCurrentTheme().getMenuSelectedBackground();
    }

    public static ColorUIResource getMenuSelectedForeground() {
        return MetalLookAndFeel.getCurrentTheme().getMenuSelectedForeground();
    }

    public static ColorUIResource getMenuDisabledForeground() {
        return MetalLookAndFeel.getCurrentTheme().getMenuDisabledForeground();
    }

    public static ColorUIResource getSeparatorBackground() {
        return MetalLookAndFeel.getCurrentTheme().getSeparatorBackground();
    }

    public static ColorUIResource getSeparatorForeground() {
        return MetalLookAndFeel.getCurrentTheme().getSeparatorForeground();
    }

    public static ColorUIResource getAcceleratorForeground() {
        return MetalLookAndFeel.getCurrentTheme().getAcceleratorForeground();
    }

    public static ColorUIResource getAcceleratorSelectedForeground() {
        return MetalLookAndFeel.getCurrentTheme().getAcceleratorSelectedForeground();
    }

    @Override
    public LayoutStyle getLayoutStyle() {
        return MetalLayoutStyle.INSTANCE;
    }

    static void flushUnreferenced() {
        AATextListener aatl;
        while ((aatl = (AATextListener)queue.poll()) != null) {
            aatl.dispose();
        }
    }

    static {
        queue = new ReferenceQueue();
    }

    private static class FontActiveValue
    implements UIDefaults.ActiveValue {
        private int type;
        private MetalTheme theme;

        FontActiveValue(MetalTheme theme, int type) {
            this.theme = theme;
            this.type = type;
        }

        @Override
        public Object createValue(UIDefaults table) {
            FontUIResource value = null;
            switch (this.type) {
                case 0: {
                    value = this.theme.getControlTextFont();
                    break;
                }
                case 1: {
                    value = this.theme.getSystemTextFont();
                    break;
                }
                case 2: {
                    value = this.theme.getUserTextFont();
                    break;
                }
                case 3: {
                    value = this.theme.getMenuTextFont();
                    break;
                }
                case 4: {
                    value = this.theme.getWindowTitleFont();
                    break;
                }
                case 5: {
                    value = this.theme.getSubTextFont();
                }
            }
            return value;
        }
    }

    static class AATextListener
    extends WeakReference<LookAndFeel>
    implements PropertyChangeListener {
        private String key = "awt.font.desktophints";
        private static boolean updatePending;

        AATextListener(LookAndFeel laf) {
            super(laf, queue);
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.addPropertyChangeListener(this.key, this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            LookAndFeel laf = (LookAndFeel)this.get();
            if (laf == null || laf != UIManager.getLookAndFeel()) {
                this.dispose();
                return;
            }
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            boolean lafCond = SwingUtilities2.isLocalDisplay();
            SwingUtilities2.putAATextInfo(lafCond, defaults);
            this.updateUI();
        }

        void dispose() {
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.removePropertyChangeListener(this.key, this);
        }

        private static void updateWindowUI(Window window) {
            Window[] ownedWins;
            SwingUtilities.updateComponentTreeUI(window);
            for (Window w : ownedWins = window.getOwnedWindows()) {
                AATextListener.updateWindowUI(w);
            }
        }

        private static void updateAllUIs() {
            Frame[] appFrames;
            for (Frame frame : appFrames = Frame.getFrames()) {
                AATextListener.updateWindowUI(frame);
            }
        }

        private static synchronized void setUpdatePending(boolean update) {
            updatePending = update;
        }

        private static synchronized boolean isUpdatePending() {
            return updatePending;
        }

        protected void updateUI() {
            if (!AATextListener.isUpdatePending()) {
                AATextListener.setUpdatePending(true);
                Runnable uiUpdater = new Runnable(this){

                    @Override
                    public void run() {
                        AATextListener.updateAllUIs();
                        AATextListener.setUpdatePending(false);
                    }
                };
                SwingUtilities.invokeLater(uiUpdater);
            }
        }
    }

    private static class MetalLayoutStyle
    extends DefaultLayoutStyle {
        private static MetalLayoutStyle INSTANCE = new MetalLayoutStyle();

        private MetalLayoutStyle() {
        }

        @Override
        public int getPreferredGap(JComponent component1, JComponent component2, LayoutStyle.ComponentPlacement type, int position, Container parent) {
            super.getPreferredGap(component1, component2, type, position, parent);
            int offset = 0;
            switch (type) {
                case INDENT: {
                    if (position == 3 || position == 7) {
                        int indent = this.getIndent(component1, position);
                        if (indent > 0) {
                            return indent;
                        }
                        return 12;
                    }
                }
                case RELATED: {
                    if (component1.getUIClassID() == "ToggleButtonUI" && component2.getUIClassID() == "ToggleButtonUI") {
                        ButtonModel sourceModel = ((JToggleButton)component1).getModel();
                        ButtonModel targetModel = ((JToggleButton)component2).getModel();
                        if (sourceModel instanceof DefaultButtonModel && targetModel instanceof DefaultButtonModel && ((DefaultButtonModel)sourceModel).getGroup() == ((DefaultButtonModel)targetModel).getGroup() && ((DefaultButtonModel)sourceModel).getGroup() != null) {
                            return 2;
                        }
                        if (MetalLookAndFeel.usingOcean()) {
                            return 6;
                        }
                        return 5;
                    }
                    offset = 6;
                    break;
                }
                case UNRELATED: {
                    offset = 12;
                }
            }
            if (this.isLabelAndNonlabel(component1, component2, position)) {
                return this.getButtonGap(component1, component2, position, offset + 6);
            }
            return this.getButtonGap(component1, component2, position, offset);
        }

        @Override
        public int getContainerGap(JComponent component, int position, Container parent) {
            super.getContainerGap(component, position, parent);
            return this.getButtonGap(component, position, 12 - this.getButtonAdjustment(component, position));
        }

        @Override
        protected int getButtonGap(JComponent source, JComponent target, int position, int offset) {
            if ((offset = super.getButtonGap(source, target, position, offset)) > 0) {
                int buttonAdjustment = this.getButtonAdjustment(source, position);
                if (buttonAdjustment == 0) {
                    buttonAdjustment = this.getButtonAdjustment(target, this.flipDirection(position));
                }
                offset -= buttonAdjustment;
            }
            if (offset < 0) {
                return 0;
            }
            return offset;
        }

        private int getButtonAdjustment(JComponent source, int edge) {
            String classID = source.getUIClassID();
            if (classID == "ButtonUI" || classID == "ToggleButtonUI" ? !MetalLookAndFeel.usingOcean() && (edge == 3 || edge == 5) && source.getBorder() instanceof UIResource : edge == 5 && (classID == "RadioButtonUI" || classID == "CheckBoxUI") && !MetalLookAndFeel.usingOcean()) {
                return 1;
            }
            return 0;
        }
    }
}

