/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.PageAttributes;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.AWTEventListener;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.FontPeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TrayIconPeer;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import sun.awt.ComponentFactory;
import sun.awt.GlobalCursorManager;
import sun.awt.KeyboardFocusManagerPeerProvider;
import sun.awt.SunToolkit;

public final class HeadlessToolkit
extends Toolkit
implements ComponentFactory,
KeyboardFocusManagerPeerProvider {
    private static final KeyboardFocusManagerPeer kfmPeer = new KeyboardFocusManagerPeer(){

        @Override
        public void setCurrentFocusedWindow(Window win) {
        }

        @Override
        public Window getCurrentFocusedWindow() {
            return null;
        }

        @Override
        public void setCurrentFocusOwner(Component comp) {
        }

        @Override
        public Component getCurrentFocusOwner() {
            return null;
        }

        @Override
        public void clearGlobalFocusOwner(Window activeWindow) {
        }
    };
    private final Toolkit tk;
    private ComponentFactory componentFactory;

    public HeadlessToolkit(Toolkit tk) {
        this.tk = tk;
        if (tk instanceof ComponentFactory) {
            this.componentFactory = (ComponentFactory)((Object)tk);
        }
    }

    public Toolkit getUnderlyingToolkit() {
        return this.tk;
    }

    @Override
    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() {
        return kfmPeer;
    }

    public TrayIconPeer createTrayIcon(TrayIcon target) throws HeadlessException {
        throw new HeadlessException();
    }

    public SystemTrayPeer createSystemTray(SystemTray target) throws HeadlessException {
        throw new HeadlessException();
    }

    public boolean isTraySupported() {
        return false;
    }

    public GlobalCursorManager getGlobalCursorManager() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    protected void loadSystemColors(int[] systemColors) throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public int getScreenResolution() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    @Deprecated(since="10")
    public int getMenuShortcutKeyMask() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public int getMenuShortcutKeyMaskEx() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
        throw new HeadlessException();
    }

    @Override
    public void setLockingKeyState(int keyCode, boolean on) throws UnsupportedOperationException {
        throw new HeadlessException();
    }

    @Override
    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public int getMaximumCursorColors() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        return null;
    }

    @Override
    public Dimension getScreenSize() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public void setDynamicLayout(boolean dynamic) throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    protected boolean isDynamicLayoutSet() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public boolean isDynamicLayoutActive() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public Clipboard getSystemClipboard() throws HeadlessException {
        throw new HeadlessException();
    }

    @Override
    public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
        if (frame != null) {
            throw new HeadlessException();
        }
        throw new NullPointerException("frame must not be null");
    }

    @Override
    public PrintJob getPrintJob(Frame frame, String doctitle, Properties props) {
        if (frame != null) {
            throw new HeadlessException();
        }
        throw new NullPointerException("frame must not be null");
    }

    @Override
    public void sync() {
    }

    @Override
    public void beep() {
        System.out.write(7);
    }

    @Override
    public EventQueue getSystemEventQueueImpl() {
        return SunToolkit.getSystemEventQueueImplPP();
    }

    @Override
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        return this.tk.checkImage(img, w, h, o);
    }

    @Override
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return this.tk.prepareImage(img, w, h, o);
    }

    @Override
    public Image getImage(String filename) {
        return this.tk.getImage(filename);
    }

    @Override
    public Image getImage(URL url) {
        return this.tk.getImage(url);
    }

    @Override
    public Image createImage(String filename) {
        return this.tk.createImage(filename);
    }

    @Override
    public Image createImage(URL url) {
        return this.tk.createImage(url);
    }

    @Override
    public Image createImage(byte[] data, int offset, int length) {
        return this.tk.createImage(data, offset, length);
    }

    @Override
    public Image createImage(ImageProducer producer) {
        return this.tk.createImage(producer);
    }

    @Override
    public Image createImage(byte[] imagedata) {
        return this.tk.createImage(imagedata);
    }

    @Override
    public FontPeer getFontPeer(String name, int style) {
        if (this.componentFactory != null) {
            return this.componentFactory.getFontPeer(name, style);
        }
        return null;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return this.tk.getFontMetrics(font);
    }

    @Override
    public String[] getFontList() {
        return this.tk.getFontList();
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        this.tk.addPropertyChangeListener(name, pcl);
    }

    @Override
    public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
        this.tk.removePropertyChangeListener(name, pcl);
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return false;
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return false;
    }

    @Override
    public boolean isAlwaysOnTopSupported() {
        return false;
    }

    @Override
    public void addAWTEventListener(AWTEventListener listener, long eventMask) {
        this.tk.addAWTEventListener(listener, eventMask);
    }

    @Override
    public void removeAWTEventListener(AWTEventListener listener) {
        this.tk.removeAWTEventListener(listener);
    }

    @Override
    public AWTEventListener[] getAWTEventListeners() {
        return this.tk.getAWTEventListeners();
    }

    @Override
    public AWTEventListener[] getAWTEventListeners(long eventMask) {
        return this.tk.getAWTEventListeners(eventMask);
    }

    public boolean isDesktopSupported() {
        return false;
    }

    @Override
    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        throw new HeadlessException();
    }
}

