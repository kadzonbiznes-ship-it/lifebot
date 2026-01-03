/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Conditional;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventDispatchThread;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.SentEvent;
import java.awt.SequencedEvent;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.TimedWindowEvent;
import sun.util.logging.PlatformLogger;

public class DefaultKeyboardFocusManager
extends KeyboardFocusManager {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.DefaultKeyboardFocusManager");
    private static final WeakReference<Window> NULL_WINDOW_WR = new WeakReference<Object>(null);
    private static final WeakReference<Component> NULL_COMPONENT_WR = new WeakReference<Object>(null);
    private WeakReference<Window> realOppositeWindowWR = NULL_WINDOW_WR;
    private WeakReference<Component> realOppositeComponentWR = NULL_COMPONENT_WR;
    private int inSendMessage;
    private LinkedList<KeyEvent> enqueuedKeyEvents = new LinkedList();
    private LinkedList<TypeAheadMarker> typeAheadMarkers = new LinkedList();
    private boolean consumeNextKeyTyped;
    private Component restoreFocusTo;
    private static boolean fxAppThreadIsDispatchThread;

    private static void initStatic() {
        AWTAccessor.setDefaultKeyboardFocusManagerAccessor(new AWTAccessor.DefaultKeyboardFocusManagerAccessor(){

            @Override
            public void consumeNextKeyTyped(DefaultKeyboardFocusManager dkfm, KeyEvent e) {
                dkfm.consumeNextKeyTyped(e);
            }
        });
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                fxAppThreadIsDispatchThread = "true".equals(System.getProperty("javafx.embed.singleThread"));
                return null;
            }
        });
    }

    private Window getOwningFrameDialog(Window window) {
        while (window != null && !(window instanceof Frame) && !(window instanceof Dialog)) {
            window = (Window)window.getParent();
        }
        return window;
    }

    private void restoreFocus(FocusEvent fe, Window newFocusedWindow) {
        Component realOppositeComponent = (Component)this.realOppositeComponentWR.get();
        Component vetoedComponent = fe.getComponent();
        if (!(newFocusedWindow != null && this.restoreFocus(newFocusedWindow, vetoedComponent, false) || realOppositeComponent != null && this.doRestoreFocus(realOppositeComponent, vetoedComponent, false) || fe.getOppositeComponent() != null && this.doRestoreFocus(fe.getOppositeComponent(), vetoedComponent, false))) {
            this.clearGlobalFocusOwnerPriv();
        }
    }

    private void restoreFocus(WindowEvent we) {
        Window realOppositeWindow = (Window)this.realOppositeWindowWR.get();
        if (!(realOppositeWindow != null && this.restoreFocus(realOppositeWindow, null, false) || we.getOppositeWindow() != null && this.restoreFocus(we.getOppositeWindow(), null, false))) {
            this.clearGlobalFocusOwnerPriv();
        }
    }

    private boolean restoreFocus(Window aWindow, Component vetoedComponent, boolean clearOnFailure) {
        this.restoreFocusTo = null;
        Component toFocus = KeyboardFocusManager.getMostRecentFocusOwner(aWindow);
        if (toFocus != null && toFocus != vetoedComponent) {
            if (DefaultKeyboardFocusManager.getHeavyweight(aWindow) != this.getNativeFocusOwner()) {
                if (!toFocus.isShowing() || !toFocus.canBeFocusOwner()) {
                    toFocus = toFocus.getNextFocusCandidate();
                }
                if (toFocus != null && toFocus != vetoedComponent) {
                    if (!toFocus.requestFocus(false, FocusEvent.Cause.ROLLBACK)) {
                        this.restoreFocusTo = toFocus;
                    }
                    return true;
                }
            } else if (this.doRestoreFocus(toFocus, vetoedComponent, false)) {
                return true;
            }
        }
        if (clearOnFailure) {
            this.clearGlobalFocusOwnerPriv();
            return true;
        }
        return false;
    }

    private boolean restoreFocus(Component toFocus, boolean clearOnFailure) {
        return this.doRestoreFocus(toFocus, null, clearOnFailure);
    }

    private boolean doRestoreFocus(Component toFocus, Component vetoedComponent, boolean clearOnFailure) {
        boolean success = true;
        if (toFocus != vetoedComponent && toFocus.isShowing() && toFocus.canBeFocusOwner() && (success = toFocus.requestFocus(false, FocusEvent.Cause.ROLLBACK))) {
            return true;
        }
        if (!success && this.getGlobalFocusedWindow() != SunToolkit.getContainingWindow(toFocus)) {
            this.restoreFocusTo = toFocus;
            return true;
        }
        Component nextFocus = toFocus.getNextFocusCandidate();
        if (nextFocus != null && nextFocus != vetoedComponent && nextFocus.requestFocusInWindow(FocusEvent.Cause.ROLLBACK)) {
            return true;
        }
        if (clearOnFailure) {
            this.clearGlobalFocusOwnerPriv();
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean sendMessage(Component target, AWTEvent e) {
        e.isPosted = true;
        AppContext myAppContext = AppContext.getAppContext();
        final AppContext targetAppContext = target.appContext;
        final DefaultKeyboardFocusManagerSentEvent se = new DefaultKeyboardFocusManagerSentEvent(e, myAppContext);
        if (myAppContext == targetAppContext) {
            ((SentEvent)se).dispatch();
        } else {
            if (targetAppContext.isDisposed()) {
                return false;
            }
            SunToolkit.postEvent(targetAppContext, se);
            if (EventQueue.isDispatchThread()) {
                if (Thread.currentThread() instanceof EventDispatchThread) {
                    EventDispatchThread edt = (EventDispatchThread)Thread.currentThread();
                    edt.pumpEvents(1007, new Conditional(){

                        @Override
                        public boolean evaluate() {
                            return !se.dispatched && !targetAppContext.isDisposed();
                        }
                    });
                } else if (fxAppThreadIsDispatchThread) {
                    Thread fxCheckDispatchThread = new Thread(){

                        @Override
                        public void run() {
                            while (!se.dispatched && !targetAppContext.isDisposed()) {
                                try {
                                    Thread.sleep(100L);
                                }
                                catch (InterruptedException e) {
                                    break;
                                }
                            }
                        }
                    };
                    fxCheckDispatchThread.start();
                    try {
                        fxCheckDispatchThread.join(500L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
            } else {
                DefaultKeyboardFocusManagerSentEvent defaultKeyboardFocusManagerSentEvent = se;
                synchronized (defaultKeyboardFocusManagerSentEvent) {
                    while (!se.dispatched && !targetAppContext.isDisposed()) {
                        try {
                            se.wait(1000L);
                        }
                        catch (InterruptedException ie) {
                            // empty catch block
                            break;
                        }
                    }
                }
            }
        }
        return se.dispatched;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean repostIfFollowsKeyEvents(WindowEvent e) {
        if (!(e instanceof TimedWindowEvent)) {
            return false;
        }
        TimedWindowEvent we = (TimedWindowEvent)e;
        long time = we.getWhen();
        DefaultKeyboardFocusManager defaultKeyboardFocusManager = this;
        synchronized (defaultKeyboardFocusManager) {
            KeyEvent ke;
            KeyEvent keyEvent = ke = this.enqueuedKeyEvents.isEmpty() ? null : this.enqueuedKeyEvents.getFirst();
            if (ke != null && time >= ke.getWhen()) {
                Window toplevel;
                TypeAheadMarker marker;
                TypeAheadMarker typeAheadMarker = marker = this.typeAheadMarkers.isEmpty() ? null : this.typeAheadMarkers.getFirst();
                if (marker != null && (toplevel = marker.untilFocused.getContainingWindow()) != null && toplevel.isFocused()) {
                    SunToolkit.postEvent(AppContext.getAppContext(), new SequencedEvent(e));
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public boolean dispatchEvent(AWTEvent e) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINE) && (e instanceof WindowEvent || e instanceof FocusEvent)) {
            focusLog.fine(String.valueOf(e));
        }
        switch (e.getID()) {
            case 207: {
                Window currentActiveWindow;
                Window newActiveWindow;
                boolean isEventDispatched;
                if (this.repostIfFollowsKeyEvents((WindowEvent)e)) {
                    return true;
                }
                WindowEvent we = (WindowEvent)e;
                Window oldFocusedWindow = this.getGlobalFocusedWindow();
                Window newFocusedWindow = we.getWindow();
                if (newFocusedWindow == oldFocusedWindow) {
                    return true;
                }
                if (!(newFocusedWindow.isFocusableWindow() && newFocusedWindow.isVisible() && newFocusedWindow.isDisplayable())) {
                    this.restoreFocus(we);
                    return true;
                }
                if (oldFocusedWindow != null && !(isEventDispatched = DefaultKeyboardFocusManager.sendMessage(oldFocusedWindow, new WindowEvent(oldFocusedWindow, 208, newFocusedWindow)))) {
                    this.setGlobalFocusOwner(null);
                    this.setGlobalFocusedWindow(null);
                }
                if ((newActiveWindow = this.getOwningFrameDialog(newFocusedWindow)) != (currentActiveWindow = this.getGlobalActiveWindow())) {
                    DefaultKeyboardFocusManager.sendMessage(newActiveWindow, new WindowEvent(newActiveWindow, 205, currentActiveWindow));
                    if (newActiveWindow != this.getGlobalActiveWindow()) {
                        this.restoreFocus(we);
                        return true;
                    }
                }
                this.setGlobalFocusedWindow(newFocusedWindow);
                if (newFocusedWindow != this.getGlobalFocusedWindow()) {
                    this.restoreFocus(we);
                    return true;
                }
                if (this.inSendMessage == 0) {
                    boolean isFocusRestore;
                    Component toFocus = KeyboardFocusManager.getMostRecentFocusOwner(newFocusedWindow);
                    boolean bl = isFocusRestore = this.restoreFocusTo != null && toFocus == this.restoreFocusTo;
                    if (toFocus == null && newFocusedWindow.isFocusableWindow()) {
                        toFocus = newFocusedWindow.getFocusTraversalPolicy().getInitialComponent(newFocusedWindow);
                    }
                    Component tempLost = null;
                    Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
                    // MONITORENTER : java.awt.KeyboardFocusManager.class
                    tempLost = newFocusedWindow.setTemporaryLostComponent(null);
                    // MONITOREXIT : clazz
                    if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                        focusLog.finer("tempLost {0}, toFocus {1}", tempLost, toFocus);
                    }
                    if (tempLost != null) {
                        tempLost.requestFocusInWindow(isFocusRestore && tempLost == toFocus ? FocusEvent.Cause.ROLLBACK : FocusEvent.Cause.ACTIVATION);
                    }
                    if (toFocus != null && toFocus != tempLost) {
                        toFocus.requestFocusInWindow(FocusEvent.Cause.ACTIVATION);
                    }
                }
                this.restoreFocusTo = null;
                Window realOppositeWindow = (Window)this.realOppositeWindowWR.get();
                if (realOppositeWindow == we.getOppositeWindow()) return this.typeAheadAssertions(newFocusedWindow, we);
                we = new WindowEvent(newFocusedWindow, 207, realOppositeWindow);
                return this.typeAheadAssertions(newFocusedWindow, we);
            }
            case 205: {
                WindowEvent we = (WindowEvent)e;
                Window oldActiveWindow = this.getGlobalActiveWindow();
                Window newActiveWindow = we.getWindow();
                if (oldActiveWindow == newActiveWindow) {
                    return true;
                }
                if (oldActiveWindow != null) {
                    boolean isEventDispatched = DefaultKeyboardFocusManager.sendMessage(oldActiveWindow, new WindowEvent(oldActiveWindow, 206, newActiveWindow));
                    if (!isEventDispatched) {
                        this.setGlobalActiveWindow(null);
                    }
                    if (this.getGlobalActiveWindow() != null) {
                        return true;
                    }
                }
                this.setGlobalActiveWindow(newActiveWindow);
                if (newActiveWindow == this.getGlobalActiveWindow()) return this.typeAheadAssertions(newActiveWindow, we);
                return true;
            }
            case 1004: {
                boolean isEventDispatched;
                this.restoreFocusTo = null;
                FocusEvent fe = (FocusEvent)e;
                Component oldFocusOwner = this.getGlobalFocusOwner();
                Component newFocusOwner = fe.getComponent();
                if (oldFocusOwner == newFocusOwner) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINE)) {
                        focusLog.fine("Skipping {0} because focus owner is the same", e);
                    }
                    this.dequeueKeyEvents(-1L, newFocusOwner);
                    return true;
                }
                if (oldFocusOwner != null && !(isEventDispatched = DefaultKeyboardFocusManager.sendMessage(oldFocusOwner, new FocusEvent(oldFocusOwner, 1005, fe.isTemporary(), newFocusOwner, fe.getCause())))) {
                    this.setGlobalFocusOwner(null);
                    if (!fe.isTemporary()) {
                        this.setGlobalPermanentFocusOwner(null);
                    }
                }
                Window newFocusedWindow = SunToolkit.getContainingWindow(newFocusOwner);
                Window currentFocusedWindow = this.getGlobalFocusedWindow();
                if (newFocusedWindow != null && newFocusedWindow != currentFocusedWindow) {
                    DefaultKeyboardFocusManager.sendMessage(newFocusedWindow, new WindowEvent(newFocusedWindow, 207, currentFocusedWindow));
                    if (newFocusedWindow != this.getGlobalFocusedWindow()) {
                        this.dequeueKeyEvents(-1L, newFocusOwner);
                        return true;
                    }
                }
                if (!newFocusOwner.isFocusable() || !newFocusOwner.isShowing() || !newFocusOwner.isEnabled() && !fe.getCause().equals((Object)FocusEvent.Cause.UNKNOWN)) {
                    this.dequeueKeyEvents(-1L, newFocusOwner);
                    if (!KeyboardFocusManager.isAutoFocusTransferEnabled()) return true;
                    if (newFocusedWindow == null) {
                        this.restoreFocus(fe, currentFocusedWindow);
                    } else {
                        this.restoreFocus(fe, newFocusedWindow);
                    }
                    DefaultKeyboardFocusManager.setMostRecentFocusOwner(newFocusedWindow, null);
                    return true;
                }
                this.setGlobalFocusOwner(newFocusOwner);
                if (newFocusOwner != this.getGlobalFocusOwner()) {
                    this.dequeueKeyEvents(-1L, newFocusOwner);
                    if (!KeyboardFocusManager.isAutoFocusTransferEnabled()) return true;
                    this.restoreFocus(fe, newFocusedWindow);
                    return true;
                }
                if (!fe.isTemporary()) {
                    this.setGlobalPermanentFocusOwner(newFocusOwner);
                    if (newFocusOwner != this.getGlobalPermanentFocusOwner()) {
                        this.dequeueKeyEvents(-1L, newFocusOwner);
                        if (!KeyboardFocusManager.isAutoFocusTransferEnabled()) return true;
                        this.restoreFocus(fe, newFocusedWindow);
                        return true;
                    }
                }
                this.setNativeFocusOwner(DefaultKeyboardFocusManager.getHeavyweight(newFocusOwner));
                Component realOppositeComponent = (Component)this.realOppositeComponentWR.get();
                if (realOppositeComponent == null) return this.typeAheadAssertions(newFocusOwner, fe);
                if (realOppositeComponent == fe.getOppositeComponent()) return this.typeAheadAssertions(newFocusOwner, fe);
                fe = new FocusEvent(newFocusOwner, 1004, fe.isTemporary(), realOppositeComponent, fe.getCause());
                fe.isPosted = true;
                return this.typeAheadAssertions(newFocusOwner, fe);
            }
            case 1005: {
                FocusEvent fe = (FocusEvent)e;
                Component currentFocusOwner = this.getGlobalFocusOwner();
                if (currentFocusOwner == null) {
                    if (!focusLog.isLoggable(PlatformLogger.Level.FINE)) return true;
                    focusLog.fine("Skipping {0} because focus owner is null", e);
                    return true;
                }
                if (currentFocusOwner == fe.getOppositeComponent()) {
                    if (!focusLog.isLoggable(PlatformLogger.Level.FINE)) return true;
                    focusLog.fine("Skipping {0} because current focus owner is equal to opposite", e);
                    return true;
                }
                this.setGlobalFocusOwner(null);
                if (this.getGlobalFocusOwner() != null) {
                    this.restoreFocus(currentFocusOwner, true);
                    return true;
                }
                if (!fe.isTemporary()) {
                    this.setGlobalPermanentFocusOwner(null);
                    if (this.getGlobalPermanentFocusOwner() != null) {
                        this.restoreFocus(currentFocusOwner, true);
                        return true;
                    }
                } else {
                    Window owningWindow = currentFocusOwner.getContainingWindow();
                    if (owningWindow != null) {
                        owningWindow.setTemporaryLostComponent(currentFocusOwner);
                    }
                }
                this.setNativeFocusOwner(null);
                fe.setSource(currentFocusOwner);
                this.realOppositeComponentWR = fe.getOppositeComponent() != null ? new WeakReference<Component>(currentFocusOwner) : NULL_COMPONENT_WR;
                return this.typeAheadAssertions(currentFocusOwner, fe);
            }
            case 206: {
                WindowEvent we = (WindowEvent)e;
                Window currentActiveWindow = this.getGlobalActiveWindow();
                if (currentActiveWindow == null) {
                    return true;
                }
                if (currentActiveWindow != e.getSource()) {
                    return true;
                }
                this.setGlobalActiveWindow(null);
                if (this.getGlobalActiveWindow() != null) {
                    return true;
                }
                we.setSource(currentActiveWindow);
                return this.typeAheadAssertions(currentActiveWindow, we);
            }
            case 208: {
                if (this.repostIfFollowsKeyEvents((WindowEvent)e)) {
                    return true;
                }
                WindowEvent we = (WindowEvent)e;
                Window currentFocusedWindow = this.getGlobalFocusedWindow();
                Window losingFocusWindow = we.getWindow();
                Window activeWindow = this.getGlobalActiveWindow();
                Window oppositeWindow = we.getOppositeWindow();
                if (focusLog.isLoggable(PlatformLogger.Level.FINE)) {
                    focusLog.fine("Active {0}, Current focused {1}, losing focus {2} opposite {3}", activeWindow, currentFocusedWindow, losingFocusWindow, oppositeWindow);
                }
                if (currentFocusedWindow == null) {
                    return true;
                }
                if (this.inSendMessage == 0 && losingFocusWindow == activeWindow && oppositeWindow == currentFocusedWindow) {
                    return true;
                }
                Component currentFocusOwner = this.getGlobalFocusOwner();
                if (currentFocusOwner != null) {
                    Component oppositeComp = null;
                    if (oppositeWindow != null && (oppositeComp = oppositeWindow.getTemporaryLostComponent()) == null) {
                        oppositeComp = oppositeWindow.getMostRecentFocusOwner();
                    }
                    if (oppositeComp == null) {
                        oppositeComp = oppositeWindow;
                    }
                    DefaultKeyboardFocusManager.sendMessage(currentFocusOwner, new FocusEvent(currentFocusOwner, 1005, true, oppositeComp, FocusEvent.Cause.ACTIVATION));
                }
                this.setGlobalFocusedWindow(null);
                if (this.getGlobalFocusedWindow() != null) {
                    this.restoreFocus(currentFocusedWindow, null, true);
                    return true;
                }
                we.setSource(currentFocusedWindow);
                this.realOppositeWindowWR = oppositeWindow != null ? new WeakReference<Window>(currentFocusedWindow) : NULL_WINDOW_WR;
                this.typeAheadAssertions(currentFocusedWindow, we);
                if (oppositeWindow != null) return true;
                if (activeWindow == null) return true;
                DefaultKeyboardFocusManager.sendMessage(activeWindow, new WindowEvent(activeWindow, 206, null));
                if (this.getGlobalActiveWindow() == null) return true;
                this.restoreFocus(currentFocusedWindow, null, true);
                return true;
            }
            case 400: 
            case 401: 
            case 402: {
                return this.typeAheadAssertions(null, e);
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        Container target;
        Component comp;
        Component focusOwner;
        Component component = focusOwner = e.isPosted ? this.getFocusOwner() : e.getComponent();
        if (focusOwner != null && focusOwner.isShowing() && focusOwner.canBeFocusOwner() && !e.isConsumed() && (comp = e.getComponent()) != null && comp.isEnabled()) {
            this.redispatchEvent(comp, e);
        }
        boolean stopPostProcessing = false;
        List<KeyEventPostProcessor> processors = this.getKeyEventPostProcessors();
        if (processors != null) {
            KeyEventPostProcessor processor;
            Iterator<KeyEventPostProcessor> iterator = processors.iterator();
            while (iterator.hasNext() && !(stopPostProcessing = (processor = iterator.next()).postProcessKeyEvent(e))) {
            }
        }
        if (!stopPostProcessing) {
            this.postProcessKeyEvent(e);
        }
        Component source = e.getComponent();
        ComponentPeer peer = source.peer;
        if ((peer == null || peer instanceof LightweightPeer) && (target = source.getNativeContainer()) != null) {
            peer = target.peer;
        }
        if (peer != null) {
            peer.handleEvent(e);
        }
        return true;
    }

    @Override
    public boolean postProcessKeyEvent(KeyEvent e) {
        Component target;
        Container p;
        if (!e.isConsumed() && (p = (Container)((target = e.getComponent()) instanceof Container ? target : target.getParent())) != null) {
            p.postProcessKeyEvent(e);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void pumpApprovedKeyEvents() {
        KeyEvent ke;
        do {
            ke = null;
            DefaultKeyboardFocusManager defaultKeyboardFocusManager = this;
            synchronized (defaultKeyboardFocusManager) {
                if (this.enqueuedKeyEvents.size() != 0) {
                    ke = this.enqueuedKeyEvents.getFirst();
                    if (this.typeAheadMarkers.size() != 0) {
                        TypeAheadMarker marker = this.typeAheadMarkers.getFirst();
                        if (ke.getWhen() > marker.after) {
                            ke = null;
                        }
                    }
                    if (ke != null) {
                        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                            focusLog.finer("Pumping approved event {0}", ke);
                        }
                        this.enqueuedKeyEvents.removeFirst();
                    }
                }
            }
            if (ke == null) continue;
            this.preDispatchKeyEvent(ke);
        } while (ke != null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void dumpMarkers() {
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest(">>> Markers dump, time: {0}", System.currentTimeMillis());
            DefaultKeyboardFocusManager defaultKeyboardFocusManager = this;
            synchronized (defaultKeyboardFocusManager) {
                if (this.typeAheadMarkers.size() != 0) {
                    for (TypeAheadMarker marker : this.typeAheadMarkers) {
                        focusLog.finest("    {0}", marker);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private boolean typeAheadAssertions(Component target, AWTEvent e) {
        this.pumpApprovedKeyEvents();
        switch (e.getID()) {
            case 400: 
            case 401: 
            case 402: {
                ke = (KeyEvent)e;
                var4_5 = this;
                synchronized (var4_5) {
                    if (e.isPosted && this.typeAheadMarkers.size() != 0) {
                        marker = this.typeAheadMarkers.getFirst();
                        if (ke.getWhen() > marker.after) {
                            if (DefaultKeyboardFocusManager.focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                                DefaultKeyboardFocusManager.focusLog.finer("Storing event {0} because of marker {1}", new Object[]{ke, marker});
                            }
                            this.enqueuedKeyEvents.addLast(ke);
                            return true;
                        }
                    }
                }
                return this.preDispatchKeyEvent(ke);
            }
            case 1004: {
                if (DefaultKeyboardFocusManager.focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    DefaultKeyboardFocusManager.focusLog.finest("Markers before FOCUS_GAINED on {0}", new Object[]{target});
                }
                this.dumpMarkers();
                var3_4 = this;
                synchronized (var3_4) {
                    found = false;
                    if (!this.hasMarker(target)) ** GOTO lbl38
                    iter = this.typeAheadMarkers.iterator();
                    while (iter.hasNext()) {
                        if (((TypeAheadMarker)iter.next()).untilFocused != target) ** GOTO lbl33
                        found = true;
                        ** GOTO lbl34
lbl33:
                        // 1 sources

                        if (found) ** GOTO lbl43
lbl34:
                        // 2 sources

                        iter.remove();
                    }
                    ** GOTO lbl43
lbl38:
                    // 1 sources

                    if (DefaultKeyboardFocusManager.focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                        DefaultKeyboardFocusManager.focusLog.finer("Event without marker {0}", new Object[]{e});
                    }
                }
lbl43:
                // 3 sources

                DefaultKeyboardFocusManager.focusLog.finest("Markers after FOCUS_GAINED");
                this.dumpMarkers();
                this.redispatchEvent(target, e);
                this.pumpApprovedKeyEvents();
                return true;
            }
        }
        this.redispatchEvent(target, e);
        return true;
    }

    private boolean hasMarker(Component comp) {
        for (TypeAheadMarker typeAheadMarker : this.typeAheadMarkers) {
            if (typeAheadMarker.untilFocused != comp) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void clearMarkers() {
        DefaultKeyboardFocusManager defaultKeyboardFocusManager = this;
        synchronized (defaultKeyboardFocusManager) {
            this.typeAheadMarkers.clear();
        }
    }

    private boolean preDispatchKeyEvent(KeyEvent ke) {
        if (ke.isPosted) {
            Component focusOwner = this.getFocusOwner();
            ke.setSource(focusOwner != null ? focusOwner : this.getFocusedWindow());
        }
        if (ke.getSource() == null) {
            return true;
        }
        EventQueue.setCurrentEventAndMostRecentTime(ke);
        if (KeyboardFocusManager.isProxyActive(ke)) {
            ComponentPeer peer;
            Component source = (Component)ke.getSource();
            Container target = source.getNativeContainer();
            if (target != null && (peer = target.peer) != null) {
                peer.handleEvent(ke);
                ke.consume();
            }
            return true;
        }
        List<KeyEventDispatcher> dispatchers = this.getKeyEventDispatchers();
        if (dispatchers != null) {
            for (KeyEventDispatcher dispatcher : dispatchers) {
                if (!dispatcher.dispatchKeyEvent(ke)) continue;
                return true;
            }
        }
        return this.dispatchKeyEvent(ke);
    }

    private void consumeNextKeyTyped(KeyEvent e) {
        this.consumeNextKeyTyped = true;
    }

    private void consumeTraversalKey(KeyEvent e) {
        e.consume();
        this.consumeNextKeyTyped = e.getID() == 401 && !e.isActionKey();
    }

    private boolean consumeProcessedKeyEvent(KeyEvent e) {
        if (e.getID() == 400 && this.consumeNextKeyTyped) {
            e.consume();
            this.consumeNextKeyTyped = false;
            return true;
        }
        return false;
    }

    @Override
    public void processKeyEvent(Component focusedComponent, KeyEvent e) {
        if (this.consumeProcessedKeyEvent(e)) {
            return;
        }
        if (e.getID() == 400) {
            return;
        }
        if (focusedComponent.getFocusTraversalKeysEnabled() && !e.isConsumed()) {
            AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
            AWTKeyStroke oppStroke = AWTKeyStroke.getAWTKeyStroke(stroke.getKeyCode(), stroke.getModifiers(), !stroke.isOnKeyRelease());
            Set<AWTKeyStroke> toTest = focusedComponent.getFocusTraversalKeys(0);
            boolean contains = toTest.contains(stroke);
            boolean containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                this.consumeTraversalKey(e);
                if (contains) {
                    this.focusNextComponent(focusedComponent);
                }
                return;
            }
            if (e.getID() == 401) {
                this.consumeNextKeyTyped = false;
            }
            toTest = focusedComponent.getFocusTraversalKeys(1);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                this.consumeTraversalKey(e);
                if (contains) {
                    this.focusPreviousComponent(focusedComponent);
                }
                return;
            }
            toTest = focusedComponent.getFocusTraversalKeys(2);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                this.consumeTraversalKey(e);
                if (contains) {
                    this.upFocusCycle(focusedComponent);
                }
                return;
            }
            if (!(focusedComponent instanceof Container) || !((Container)focusedComponent).isFocusCycleRoot()) {
                return;
            }
            toTest = focusedComponent.getFocusTraversalKeys(3);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);
            if (contains || containsOpp) {
                this.consumeTraversalKey(e);
                if (contains) {
                    this.downFocusCycle((Container)focusedComponent);
                }
            }
        }
    }

    @Override
    protected synchronized void enqueueKeyEvents(long after, Component untilFocused) {
        int i;
        if (untilFocused == null) {
            return;
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Enqueue at {0} for {1}", after, untilFocused);
        }
        int insertionIndex = 0;
        ListIterator<TypeAheadMarker> iter = this.typeAheadMarkers.listIterator(i);
        for (i = this.typeAheadMarkers.size(); i > 0; --i) {
            TypeAheadMarker marker = iter.previous();
            if (marker.after > after) continue;
            insertionIndex = i;
            break;
        }
        this.typeAheadMarkers.add(insertionIndex, new TypeAheadMarker(after, untilFocused));
    }

    @Override
    protected synchronized void dequeueKeyEvents(long after, Component untilFocused) {
        if (untilFocused == null) {
            return;
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Dequeue at {0} for {1}", after, untilFocused);
        }
        ListIterator<TypeAheadMarker> iter = this.typeAheadMarkers.listIterator(after >= 0L ? this.typeAheadMarkers.size() : 0);
        if (after < 0L) {
            while (iter.hasNext()) {
                TypeAheadMarker marker = iter.next();
                if (marker.untilFocused != untilFocused) continue;
                iter.remove();
                return;
            }
        } else {
            while (iter.hasPrevious()) {
                TypeAheadMarker marker = iter.previous();
                if (marker.untilFocused != untilFocused || marker.after != after) continue;
                iter.remove();
                return;
            }
        }
    }

    @Override
    protected synchronized void discardKeyEvents(Component comp) {
        if (comp == null) {
            return;
        }
        long start = -1L;
        Iterator iter = this.typeAheadMarkers.iterator();
        while (iter.hasNext()) {
            boolean match;
            TypeAheadMarker marker = (TypeAheadMarker)iter.next();
            Component toTest = marker.untilFocused;
            boolean bl = match = toTest == comp;
            while (!match && toTest != null && !(toTest instanceof Window)) {
                match = (toTest = toTest.getParent()) == comp;
            }
            if (match) {
                if (start < 0L) {
                    start = marker.after;
                }
                iter.remove();
                continue;
            }
            if (start < 0L) continue;
            this.purgeStampedEvents(start, marker.after);
            start = -1L;
        }
        this.purgeStampedEvents(start, -1L);
    }

    private void purgeStampedEvents(long start, long end) {
        if (start < 0L) {
            return;
        }
        Iterator iter = this.enqueuedKeyEvents.iterator();
        while (iter.hasNext()) {
            KeyEvent ke = (KeyEvent)iter.next();
            long time = ke.getWhen();
            if (start < time && (end < 0L || time <= end)) {
                iter.remove();
            }
            if (end < 0L || time <= end) continue;
            break;
        }
    }

    @Override
    public void focusPreviousComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusBackward();
        }
    }

    @Override
    public void focusNextComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocus();
        }
    }

    @Override
    public void upFocusCycle(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusUpCycle();
        }
    }

    @Override
    public void downFocusCycle(Container aContainer) {
        if (aContainer != null && aContainer.isFocusCycleRoot()) {
            aContainer.transferFocusDownCycle();
        }
    }

    static {
        DefaultKeyboardFocusManager.initStatic();
    }

    private static class DefaultKeyboardFocusManagerSentEvent
    extends SentEvent {
        private static final long serialVersionUID = -2924743257508701758L;

        public DefaultKeyboardFocusManagerSentEvent(AWTEvent nested, AppContext toNotify) {
            super(nested, toNotify);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public final void dispatch() {
            DefaultKeyboardFocusManager defaultKeyboardFocusManager;
            DefaultKeyboardFocusManager defaultManager;
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            DefaultKeyboardFocusManager defaultKeyboardFocusManager2 = defaultManager = manager instanceof DefaultKeyboardFocusManager ? (DefaultKeyboardFocusManager)manager : null;
            if (defaultManager != null) {
                defaultKeyboardFocusManager = defaultManager;
                synchronized (defaultKeyboardFocusManager) {
                    ++defaultManager.inSendMessage;
                }
            }
            super.dispatch();
            if (defaultManager != null) {
                defaultKeyboardFocusManager = defaultManager;
                synchronized (defaultKeyboardFocusManager) {
                    --defaultManager.inSendMessage;
                }
            }
        }
    }

    private static class TypeAheadMarker {
        long after;
        Component untilFocused;

        TypeAheadMarker(long after, Component untilFocused) {
            this.after = after;
            this.untilFocused = untilFocused;
        }

        public String toString() {
            return ">>> Marker after " + this.after + " on " + String.valueOf(this.untilFocused);
        }
    }
}

