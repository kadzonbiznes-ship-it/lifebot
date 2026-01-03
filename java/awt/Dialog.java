/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventDispatchThread;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.ModalEventFilter;
import java.awt.SecondaryLoop;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.InvocationEvent;
import java.awt.peer.DialogPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import sun.awt.AWTPermissions;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.util.IdentityArrayList;

public class Dialog
extends Window {
    boolean resizable = true;
    boolean undecorated = false;
    private transient boolean initialized = false;
    public static final ModalityType DEFAULT_MODALITY_TYPE;
    boolean modal;
    ModalityType modalityType;
    static transient IdentityArrayList<Dialog> modalDialogs;
    transient IdentityArrayList<Window> blockedWindows = new IdentityArrayList();
    String title;
    private transient ModalEventFilter modalFilter;
    private volatile transient SecondaryLoop secondaryLoop;
    volatile transient boolean isInHide;
    volatile transient boolean isInDispose;
    private static final String base = "dialog";
    private static int nameCounter;
    private static final long serialVersionUID = 5920926903803293709L;

    public Dialog(Frame owner) {
        this(owner, "", false);
    }

    public Dialog(Frame owner, boolean modal) {
        this(owner, "", modal);
    }

    public Dialog(Frame owner, String title) {
        this(owner, title, false);
    }

    public Dialog(Frame owner, String title, boolean modal) {
        this((Window)owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    public Dialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        this((Window)owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS, gc);
    }

    public Dialog(Dialog owner) {
        this(owner, "", false);
    }

    public Dialog(Dialog owner, String title) {
        this(owner, title, false);
    }

    public Dialog(Dialog owner, String title, boolean modal) {
        this((Window)owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    public Dialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        this((Window)owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS, gc);
    }

    public Dialog(Window owner) {
        this(owner, "", ModalityType.MODELESS);
    }

    public Dialog(Window owner, String title) {
        this(owner, title, ModalityType.MODELESS);
    }

    public Dialog(Window owner, ModalityType modalityType) {
        this(owner, "", modalityType);
    }

    public Dialog(Window owner, String title, ModalityType modalityType) {
        super(owner);
        if (owner != null && !(owner instanceof Frame) && !(owner instanceof Dialog)) {
            throw new IllegalArgumentException("Wrong parent window");
        }
        this.title = title;
        this.setModalityType(modalityType);
        SunToolkit.checkAndSetPolicy(this);
        this.initialized = true;
    }

    public Dialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, gc);
        if (owner != null && !(owner instanceof Frame) && !(owner instanceof Dialog)) {
            throw new IllegalArgumentException("wrong owner window");
        }
        this.title = title;
        this.setModalityType(modalityType);
        SunToolkit.checkAndSetPolicy(this);
        this.initialized = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    String constructComponentName() {
        Class<Dialog> clazz = Dialog.class;
        synchronized (Dialog.class) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return base + nameCounter++;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.parent != null && this.parent.peer == null) {
                this.parent.addNotify();
            }
            if (this.peer == null) {
                this.peer = this.getComponentFactory().createDialog(this);
            }
            super.addNotify();
        }
    }

    public boolean isModal() {
        return this.isModal_NoClientCode();
    }

    final boolean isModal_NoClientCode() {
        return this.modalityType != ModalityType.MODELESS;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
        this.setModalityType(modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    public ModalityType getModalityType() {
        return this.modalityType;
    }

    public void setModalityType(ModalityType type) {
        if (type == null) {
            type = ModalityType.MODELESS;
        }
        if (!Toolkit.getDefaultToolkit().isModalityTypeSupported(type)) {
            type = ModalityType.MODELESS;
        }
        if (this.modalityType == type) {
            return;
        }
        this.checkModalityPermission(type);
        this.modalityType = type;
        this.modal = this.modalityType != ModalityType.MODELESS;
    }

    public String getTitle() {
        return this.title;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTitle(String title) {
        String oldTitle = this.title;
        Dialog dialog = this;
        synchronized (dialog) {
            this.title = title;
            DialogPeer peer = (DialogPeer)this.peer;
            if (peer != null) {
                peer.setTitle(title);
            }
        }
        this.firePropertyChange("title", oldTitle, title);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean conditionalShow(Component toFocus, AtomicLong time) {
        boolean retval;
        this.closeSplashScreen();
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.peer == null) {
                this.addNotify();
            }
            this.validateUnconditionally();
            if (this.visible) {
                this.toFront();
                retval = false;
            } else {
                retval = true;
                this.visible = true;
                if (!this.isModal()) {
                    Dialog.checkShouldBeBlocked(this);
                } else {
                    modalDialogs.add(this);
                    this.modalShow();
                }
                if (toFocus != null && time != null && this.isFocusable() && this.isEnabled() && !this.isModalBlocked()) {
                    time.set(Toolkit.getEventQueue().getMostRecentKeyEventTime());
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().enqueueKeyEvents(time.get(), toFocus);
                }
                this.mixOnShowing();
                this.peer.setVisible(true);
                if (this.isModalBlocked()) {
                    this.modalBlocker.toFront();
                }
                this.setLocationByPlatform(false);
                for (int i = 0; i < this.ownedWindowList.size(); ++i) {
                    Window child = (Window)((WeakReference)this.ownedWindowList.elementAt(i)).get();
                    if (child == null || !child.showWithParent) continue;
                    child.show();
                    child.showWithParent = false;
                }
                Window.updateChildFocusableWindowState(this);
                this.createHierarchyEvents(1400, this, this.parent, 4L, Toolkit.enabledOnToolkit(32768L));
                if (this.componentListener != null || (this.eventMask & 1L) != 0L || Toolkit.enabledOnToolkit(1L)) {
                    ComponentEvent e = new ComponentEvent(this, 102);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
        }
        if (retval && (this.state & 1) == 0) {
            this.postWindowEvent(200);
            this.state |= 1;
        }
        return retval;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public void show() {
        block15: {
            if (!this.initialized) {
                throw new IllegalStateException("The dialog component has not been initialized properly");
            }
            this.beforeFirstShow = false;
            if (!this.isModal()) {
                this.conditionalShow(null, null);
            } else {
                AppContext showAppContext = AppContext.getAppContext();
                AtomicLong time = new AtomicLong();
                Component predictedFocusOwner = null;
                try {
                    EventDispatchThread edt;
                    EventQueue eventQueue;
                    predictedFocusOwner = this.getMostRecentFocusOwner();
                    if (!this.conditionalShow(predictedFocusOwner, time)) break block15;
                    this.modalFilter = ModalEventFilter.createFilterForDialog(this);
                    if (this.modalityType == ModalityType.TOOLKIT_MODAL) {
                        for (AppContext appContext : AppContext.getAppContexts()) {
                            if (appContext == showAppContext) continue;
                            eventQueue = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
                            eventQueue.postEvent(new InvocationEvent((Object)this, () -> {}));
                            edt = eventQueue.getDispatchThread();
                            edt.addEventFilter(this.modalFilter);
                        }
                    }
                    this.modalityPushed();
                    try {
                        EventQueue eventQueue2 = AccessController.doPrivileged(Toolkit.getDefaultToolkit()::getSystemEventQueue);
                        this.secondaryLoop = eventQueue2.createSecondaryLoop(() -> true, this.modalFilter, 0L);
                        if (!this.secondaryLoop.enter()) {
                            this.secondaryLoop = null;
                        }
                    }
                    finally {
                        this.modalityPopped();
                    }
                    if (this.modalityType == ModalityType.TOOLKIT_MODAL) {
                        for (AppContext appContext : AppContext.getAppContexts()) {
                            if (appContext == showAppContext) continue;
                            eventQueue = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
                            edt = eventQueue.getDispatchThread();
                            edt.removeEventFilter(this.modalFilter);
                        }
                    }
                }
                finally {
                    if (predictedFocusOwner != null) {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().dequeueKeyEvents(time.get(), predictedFocusOwner);
                    }
                }
            }
        }
    }

    final void modalityPushed() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            SunToolkit stk = (SunToolkit)tk;
            stk.notifyModalityPushed(this);
        }
    }

    final void modalityPopped() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            SunToolkit stk = (SunToolkit)tk;
            stk.notifyModalityPopped(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void hideAndDisposePreHandler() {
        this.isInHide = true;
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.secondaryLoop != null) {
                this.modalHide();
                if (this.modalFilter != null) {
                    this.modalFilter.disable();
                }
                modalDialogs.remove(this);
            }
        }
    }

    private void hideAndDisposeHandler() {
        if (this.secondaryLoop != null) {
            this.secondaryLoop.exit();
            this.secondaryLoop = null;
        }
        this.isInHide = false;
    }

    @Override
    @Deprecated
    public void hide() {
        this.hideAndDisposePreHandler();
        super.hide();
        if (!this.isInDispose) {
            this.hideAndDisposeHandler();
        }
    }

    @Override
    void doDispose() {
        this.isInDispose = true;
        super.doDispose();
        this.hideAndDisposeHandler();
        this.isInDispose = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void toBack() {
        super.toBack();
        if (this.visible) {
            Object object = this.getTreeLock();
            synchronized (object) {
                for (Window w : this.blockedWindows) {
                    w.toBack_NoClientCode();
                }
            }
        }
    }

    public boolean isResizable() {
        return this.resizable;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setResizable(boolean resizable) {
        boolean testvalid = false;
        Dialog dialog = this;
        synchronized (dialog) {
            this.resizable = resizable;
            DialogPeer peer = (DialogPeer)this.peer;
            if (peer != null) {
                peer.setResizable(resizable);
                testvalid = true;
            }
        }
        if (testvalid) {
            this.invalidateIfValid();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setUndecorated(boolean undecorated) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.isDisplayable()) {
                throw new IllegalComponentStateException("The dialog is displayable.");
            }
            if (!undecorated) {
                if (this.getOpacity() < 1.0f) {
                    throw new IllegalComponentStateException("The dialog is not opaque");
                }
                if (this.getShape() != null) {
                    throw new IllegalComponentStateException("The dialog does not have a default shape");
                }
                Color bg = this.getBackground();
                if (bg != null && bg.getAlpha() < 255) {
                    throw new IllegalComponentStateException("The dialog background color is not opaque");
                }
            }
            this.undecorated = undecorated;
        }
    }

    public boolean isUndecorated() {
        return this.undecorated;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOpacity(float opacity) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (opacity < 1.0f && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The dialog is decorated");
            }
            super.setOpacity(opacity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setShape(Shape shape) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (shape != null && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The dialog is decorated");
            }
            super.setShape(shape);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBackground(Color bgColor) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (bgColor != null && bgColor.getAlpha() < 255 && !this.isUndecorated()) {
                throw new IllegalComponentStateException("The dialog is decorated");
            }
            super.setBackground(bgColor);
        }
    }

    @Override
    protected String paramString() {
        String str = super.paramString() + "," + String.valueOf((Object)this.modalityType);
        if (this.title != null) {
            str = str + ",title=" + this.title;
        }
        return str;
    }

    private static native void initIDs();

    void modalShow() {
        Window w;
        IdentityArrayList<Dialog> blockers = new IdentityArrayList<Dialog>();
        for (Dialog d : modalDialogs) {
            if (!d.shouldBlock(this)) continue;
            for (w = d; w != null && w != this; w = w.getOwner_NoClientCode()) {
            }
            if (w != this && this.shouldBlock(d) && this.modalityType.compareTo(d.getModalityType()) >= 0) continue;
            blockers.add(d);
        }
        for (int i = 0; i < blockers.size(); ++i) {
            Dialog blockerBlocker;
            Dialog blocker = (Dialog)blockers.get(i);
            if (!blocker.isModalBlocked() || blockers.contains(blockerBlocker = blocker.getModalBlocker())) continue;
            blockers.add(i + 1, blockerBlocker);
        }
        if (blockers.size() > 0) {
            ((Dialog)blockers.get(0)).blockWindow(this);
        }
        IdentityArrayList<Window> blockersHierarchies = new IdentityArrayList<Window>(blockers);
        for (int k = 0; k < blockersHierarchies.size(); ++k) {
            Window[] ownedWindows;
            w = (Window)blockersHierarchies.get(k);
            for (Window win : ownedWindows = w.getOwnedWindows_NoClientCode()) {
                blockersHierarchies.add(win);
            }
        }
        IdentityArrayList<Window> toBlock = new IdentityArrayList<Window>();
        IdentityArrayList<Window> unblockedWindows = Window.getAllUnblockedWindows();
        for (Window w2 : unblockedWindows) {
            Dialog wd;
            if (!this.shouldBlock(w2) || blockersHierarchies.contains(w2) || w2 instanceof Dialog && ((Dialog)w2).isModal_NoClientCode() && (wd = (Dialog)w2).shouldBlock(this) && modalDialogs.indexOf(wd) > modalDialogs.indexOf(this)) continue;
            toBlock.add(w2);
        }
        this.blockWindows(toBlock);
        if (!this.isModalBlocked()) {
            this.updateChildrenBlocking();
        }
    }

    void modalHide() {
        Window w;
        int i;
        IdentityArrayList<Window> save = new IdentityArrayList<Window>();
        int blockedWindowsCount = this.blockedWindows.size();
        for (i = 0; i < blockedWindowsCount; ++i) {
            w = this.blockedWindows.get(0);
            save.add(w);
            this.unblockWindow(w);
        }
        for (i = 0; i < blockedWindowsCount; ++i) {
            w = (Window)save.get(i);
            if (w instanceof Dialog && ((Dialog)w).isModal_NoClientCode()) {
                Dialog d = (Dialog)w;
                d.modalShow();
                continue;
            }
            Dialog.checkShouldBeBlocked(w);
        }
    }

    boolean shouldBlock(Window w) {
        Container c;
        if (!this.isVisible_NoClientCode() || !w.isVisible_NoClientCode() && !w.isInShow || this.isInHide || w == this || !this.isModal_NoClientCode()) {
            return false;
        }
        if (w instanceof Dialog && ((Dialog)w).isInHide) {
            return false;
        }
        for (Dialog blockerToCheck = this; blockerToCheck != null; blockerToCheck = blockerToCheck.getModalBlocker()) {
            for (c = w; c != null && c != blockerToCheck; c = c.getParent_NoClientCode()) {
            }
            if (c != blockerToCheck) continue;
            return false;
        }
        switch (this.modalityType.ordinal()) {
            case 0: {
                return false;
            }
            case 1: {
                if (w.isModalExcluded(ModalExclusionType.APPLICATION_EXCLUDE)) {
                    for (c = this; c != null && c != w; c = c.getParent_NoClientCode()) {
                    }
                    return c == w;
                }
                return this.getDocumentRoot() == w.getDocumentRoot();
            }
            case 2: {
                return !w.isModalExcluded(ModalExclusionType.APPLICATION_EXCLUDE) && this.appContext == w.appContext;
            }
            case 3: {
                return !w.isModalExcluded(ModalExclusionType.TOOLKIT_EXCLUDE);
            }
        }
        return false;
    }

    void blockWindow(Window w) {
        if (!w.isModalBlocked()) {
            w.setModalBlocked(this, true, true);
            this.blockedWindows.add(w);
        }
    }

    void blockWindows(List<Window> toBlock) {
        DialogPeer dpeer = (DialogPeer)this.peer;
        if (dpeer == null) {
            return;
        }
        Iterator<Window> it = toBlock.iterator();
        while (it.hasNext()) {
            Window w = it.next();
            if (!w.isModalBlocked()) {
                w.setModalBlocked(this, true, false);
                continue;
            }
            it.remove();
        }
        dpeer.blockWindows(toBlock);
        this.blockedWindows.addAll(toBlock);
    }

    void unblockWindow(Window w) {
        if (w.isModalBlocked() && this.blockedWindows.contains(w)) {
            this.blockedWindows.remove(w);
            w.setModalBlocked(this, false, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void checkShouldBeBlocked(Window w) {
        Object object = w.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < modalDialogs.size(); ++i) {
                Dialog modalDialog = modalDialogs.get(i);
                if (!modalDialog.shouldBlock(w)) continue;
                modalDialog.blockWindow(w);
                break;
            }
        }
    }

    private void checkModalityPermission(ModalityType mt) {
        SecurityManager sm;
        if (mt == ModalityType.TOOLKIT_MODAL && (sm = System.getSecurityManager()) != null) {
            sm.checkPermission(AWTPermissions.TOOLKIT_MODALITY_PERMISSION);
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        ObjectInputStream.GetField fields = s.readFields();
        ModalityType localModalityType = (ModalityType)((Object)fields.get("modalityType", null));
        try {
            this.checkModalityPermission(localModalityType);
        }
        catch (AccessControlException ace) {
            localModalityType = DEFAULT_MODALITY_TYPE;
        }
        if (localModalityType == null) {
            this.modal = fields.get("modal", false);
            this.setModal(this.modal);
        } else {
            this.modalityType = localModalityType;
        }
        this.resizable = fields.get("resizable", true);
        this.undecorated = fields.get("undecorated", false);
        this.title = (String)fields.get("title", "");
        this.blockedWindows = new IdentityArrayList();
        SunToolkit.checkAndSetPolicy(this);
        this.initialized = true;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleAWTDialog();
        }
        return this.accessibleContext;
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Dialog.initIDs();
        }
        DEFAULT_MODALITY_TYPE = ModalityType.APPLICATION_MODAL;
        modalDialogs = new IdentityArrayList();
        nameCounter = 0;
    }

    public static enum ModalityType {
        MODELESS,
        DOCUMENT_MODAL,
        APPLICATION_MODAL,
        TOOLKIT_MODAL;

    }

    public static enum ModalExclusionType {
        NO_EXCLUDE,
        APPLICATION_EXCLUDE,
        TOOLKIT_EXCLUDE;

    }

    protected class AccessibleAWTDialog
    extends Window.AccessibleAWTWindow {
        private static final long serialVersionUID = 4837230331833941201L;

        protected AccessibleAWTDialog() {
            super(Dialog.this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.DIALOG;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (Dialog.this.getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            if (Dialog.this.isModal()) {
                states.add(AccessibleState.MODAL);
            }
            if (Dialog.this.isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            return states;
        }
    }
}

