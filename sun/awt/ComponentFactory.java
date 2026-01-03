/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.Taskbar;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.LightweightPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.MouseInfoPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TaskbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
import sun.awt.LightweightPeerHolder;
import sun.awt.datatransfer.DataTransferer;

public interface ComponentFactory {
    default public LightweightPeer createComponent(Component target) {
        return LightweightPeerHolder.lightweightMarker;
    }

    default public DesktopPeer createDesktopPeer(Desktop target) {
        throw new HeadlessException();
    }

    default public TaskbarPeer createTaskbarPeer(Taskbar target) {
        throw new HeadlessException();
    }

    default public ButtonPeer createButton(Button target) {
        throw new HeadlessException();
    }

    default public TextFieldPeer createTextField(TextField target) {
        throw new HeadlessException();
    }

    default public LabelPeer createLabel(Label target) {
        throw new HeadlessException();
    }

    default public ListPeer createList(List target) {
        throw new HeadlessException();
    }

    default public CheckboxPeer createCheckbox(Checkbox target) {
        throw new HeadlessException();
    }

    default public ScrollbarPeer createScrollbar(Scrollbar target) {
        throw new HeadlessException();
    }

    default public ScrollPanePeer createScrollPane(ScrollPane target) {
        throw new HeadlessException();
    }

    default public TextAreaPeer createTextArea(TextArea target) {
        throw new HeadlessException();
    }

    default public ChoicePeer createChoice(Choice target) {
        throw new HeadlessException();
    }

    default public FramePeer createFrame(Frame target) {
        throw new HeadlessException();
    }

    default public CanvasPeer createCanvas(Canvas target) {
        return (CanvasPeer)((Object)this.createComponent(target));
    }

    default public PanelPeer createPanel(Panel target) {
        return (PanelPeer)((Object)this.createComponent(target));
    }

    default public WindowPeer createWindow(Window target) {
        throw new HeadlessException();
    }

    default public DialogPeer createDialog(Dialog target) {
        throw new HeadlessException();
    }

    default public MenuBarPeer createMenuBar(MenuBar target) {
        throw new HeadlessException();
    }

    default public MenuPeer createMenu(Menu target) {
        throw new HeadlessException();
    }

    default public PopupMenuPeer createPopupMenu(PopupMenu target) {
        throw new HeadlessException();
    }

    default public MenuItemPeer createMenuItem(MenuItem target) {
        throw new HeadlessException();
    }

    default public FileDialogPeer createFileDialog(FileDialog target) {
        throw new HeadlessException();
    }

    default public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        throw new HeadlessException();
    }

    default public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) {
        throw new InvalidDnDOperationException("Headless environment");
    }

    default public FontPeer getFontPeer(String name, int style) {
        return null;
    }

    default public RobotPeer createRobot(GraphicsDevice screen) throws AWTException {
        throw new AWTException(String.format("Unsupported device: %s", screen));
    }

    default public DataTransferer getDataTransferer() {
        return null;
    }

    default public MouseInfoPeer getMouseInfoPeer() {
        throw new UnsupportedOperationException("Not implemented");
    }
}

