/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.MetalUtils;
import sun.swing.PrintColorUIResource;
import sun.swing.SwingUtilities2;

public class OceanTheme
extends DefaultMetalTheme {
    private static final ColorUIResource PRIMARY1 = new ColorUIResource(6521535);
    private static final ColorUIResource PRIMARY2 = new ColorUIResource(10729676);
    private static final ColorUIResource PRIMARY3 = new ColorUIResource(12111845);
    private static final ColorUIResource SECONDARY1 = new ColorUIResource(8030873);
    private static final ColorUIResource SECONDARY2 = new ColorUIResource(12111845);
    private static final ColorUIResource SECONDARY3 = new ColorUIResource(0xEEEEEE);
    private static final ColorUIResource CONTROL_TEXT_COLOR = new PrintColorUIResource(0x333333, Color.BLACK);
    private static final ColorUIResource INACTIVE_CONTROL_TEXT_COLOR = new ColorUIResource(0x999999);
    private static final ColorUIResource MENU_DISABLED_FOREGROUND = new ColorUIResource(0x999999);
    private static final ColorUIResource OCEAN_BLACK = new PrintColorUIResource(0x333333, Color.BLACK);
    private static final ColorUIResource OCEAN_DROP = new ColorUIResource(13822463);

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        UIDefaults.LazyValue focusBorder = t -> new BorderUIResource.LineBorderUIResource(this.getPrimary1());
        List<Object> buttonGradient = Arrays.asList(Float.valueOf(0.3f), Float.valueOf(0.0f), new ColorUIResource(14543091), this.getWhite(), this.getSecondary2());
        ColorUIResource cccccc = new ColorUIResource(0xCCCCCC);
        ColorUIResource dadada = new ColorUIResource(0xDADADA);
        ColorUIResource c8ddf2 = new ColorUIResource(13164018);
        Object directoryIcon = this.getIconResource("icons/ocean/directory.gif");
        Object fileIcon = this.getIconResource("icons/ocean/file.gif");
        List<Object> sliderGradient = Arrays.asList(Float.valueOf(0.3f), Float.valueOf(0.2f), c8ddf2, this.getWhite(), new ColorUIResource(SECONDARY2));
        Object[] defaults = new Object[]{"Button.gradient", buttonGradient, "Button.rollover", Boolean.TRUE, "Button.toolBarBorderBackground", INACTIVE_CONTROL_TEXT_COLOR, "Button.disabledToolBarBorderBackground", cccccc, "Button.rolloverIconType", "ocean", "CheckBox.rollover", Boolean.TRUE, "CheckBox.gradient", buttonGradient, "CheckBoxMenuItem.gradient", buttonGradient, "FileChooser.homeFolderIcon", this.getIconResource("icons/ocean/homeFolder.gif"), "FileChooser.newFolderIcon", this.getIconResource("icons/ocean/newFolder.gif"), "FileChooser.upFolderIcon", this.getIconResource("icons/ocean/upFolder.gif"), "FileView.computerIcon", this.getIconResource("icons/ocean/computer.gif"), "FileView.directoryIcon", directoryIcon, "FileView.hardDriveIcon", this.getIconResource("icons/ocean/hardDrive.gif"), "FileView.fileIcon", fileIcon, "FileView.floppyDriveIcon", this.getIconResource("icons/ocean/floppy.gif"), "Label.disabledForeground", this.getInactiveControlTextColor(), "Menu.opaque", Boolean.FALSE, "MenuBar.gradient", Arrays.asList(Float.valueOf(1.0f), Float.valueOf(0.0f), this.getWhite(), dadada, new ColorUIResource(dadada)), "MenuBar.borderColor", cccccc, "InternalFrame.activeTitleGradient", buttonGradient, "InternalFrame.closeIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new IFIcon(OceanTheme.this.getHastenedIcon("icons/ocean/close.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/close-pressed.gif", table));
            }
        }, "InternalFrame.iconifyIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new IFIcon(OceanTheme.this.getHastenedIcon("icons/ocean/iconify.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/iconify-pressed.gif", table));
            }
        }, "InternalFrame.minimizeIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new IFIcon(OceanTheme.this.getHastenedIcon("icons/ocean/minimize.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/minimize-pressed.gif", table));
            }
        }, "InternalFrame.icon", this.getIconResource("icons/ocean/menu.gif"), "InternalFrame.maximizeIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new IFIcon(OceanTheme.this.getHastenedIcon("icons/ocean/maximize.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/maximize-pressed.gif", table));
            }
        }, "InternalFrame.paletteCloseIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new IFIcon(OceanTheme.this.getHastenedIcon("icons/ocean/paletteClose.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/paletteClose-pressed.gif", table));
            }
        }, "List.focusCellHighlightBorder", focusBorder, "OptionPane.errorIcon", this.getIconResource("icons/ocean/error.png"), "OptionPane.informationIcon", this.getIconResource("icons/ocean/info.png"), "OptionPane.questionIcon", this.getIconResource("icons/ocean/question.png"), "OptionPane.warningIcon", this.getIconResource("icons/ocean/warning.png"), "RadioButton.gradient", buttonGradient, "RadioButton.rollover", Boolean.TRUE, "RadioButtonMenuItem.gradient", buttonGradient, "ScrollBar.gradient", buttonGradient, "Slider.altTrackColor", new ColorUIResource(13820655), "Slider.gradient", sliderGradient, "Slider.focusGradient", sliderGradient, "SplitPane.oneTouchButtonsOpaque", Boolean.FALSE, "SplitPane.dividerFocusColor", c8ddf2, "TabbedPane.borderHightlightColor", this.getPrimary1(), "TabbedPane.contentAreaColor", c8ddf2, "TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 3), "TabbedPane.selected", c8ddf2, "TabbedPane.tabAreaBackground", dadada, "TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 6), "TabbedPane.unselectedBackground", SECONDARY3, "Table.focusCellHighlightBorder", focusBorder, "Table.gridColor", SECONDARY1, "TableHeader.focusCellBackground", c8ddf2, "ToggleButton.gradient", buttonGradient, "ToolBar.borderColor", cccccc, "ToolBar.isRollover", Boolean.TRUE, "Tree.closedIcon", directoryIcon, "Tree.collapsedIcon", new UIDefaults.LazyValue(){

            @Override
            public Object createValue(UIDefaults table) {
                return new COIcon(OceanTheme.this.getHastenedIcon("icons/ocean/collapsed.gif", table), OceanTheme.this.getHastenedIcon("icons/ocean/collapsed-rtl.gif", table));
            }
        }, "Tree.expandedIcon", this.getIconResource("icons/ocean/expanded.gif"), "Tree.leafIcon", fileIcon, "Tree.openIcon", directoryIcon, "Tree.selectionBorderColor", this.getPrimary1(), "Tree.dropLineColor", this.getPrimary1(), "Table.dropLineColor", this.getPrimary1(), "Table.dropLineShortColor", OCEAN_BLACK, "Table.dropCellBackground", OCEAN_DROP, "Tree.dropCellBackground", OCEAN_DROP, "List.dropCellBackground", OCEAN_DROP, "List.dropLineColor", this.getPrimary1()};
        table.putDefaults(defaults);
    }

    @Override
    boolean isSystemTheme() {
        return true;
    }

    @Override
    public String getName() {
        return "Ocean";
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return SECONDARY1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return SECONDARY2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return SECONDARY3;
    }

    @Override
    protected ColorUIResource getBlack() {
        return OCEAN_BLACK;
    }

    @Override
    public ColorUIResource getDesktopColor() {
        return MetalTheme.white;
    }

    @Override
    public ColorUIResource getInactiveControlTextColor() {
        return INACTIVE_CONTROL_TEXT_COLOR;
    }

    @Override
    public ColorUIResource getControlTextColor() {
        return CONTROL_TEXT_COLOR;
    }

    @Override
    public ColorUIResource getMenuDisabledForeground() {
        return MENU_DISABLED_FOREGROUND;
    }

    private Object getIconResource(String iconID) {
        return SwingUtilities2.makeIcon(this.getClass(), OceanTheme.class, iconID);
    }

    private Icon getHastenedIcon(String iconID, UIDefaults table) {
        Object res = this.getIconResource(iconID);
        return (Icon)((UIDefaults.LazyValue)res).createValue(table);
    }

    private static class IFIcon
    extends IconUIResource {
        private Icon pressed;

        public IFIcon(Icon normal, Icon pressed) {
            super(normal);
            this.pressed = pressed;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            ButtonModel model = ((AbstractButton)c).getModel();
            if (model.isPressed() && model.isArmed()) {
                this.pressed.paintIcon(c, g, x, y);
            } else {
                super.paintIcon(c, g, x, y);
            }
        }
    }

    private static class COIcon
    extends IconUIResource {
        private Icon rtl;

        public COIcon(Icon ltr, Icon rtl) {
            super(ltr);
            this.rtl = rtl;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (MetalUtils.isLeftToRight(c)) {
                super.paintIcon(c, g, x, y);
            } else {
                this.rtl.paintIcon(c, g, x, y);
            }
        }
    }
}

