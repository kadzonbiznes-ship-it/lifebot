/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.dnd.SunDropTargetEvent;
import sun.util.logging.PlatformLogger;

class LightweightDispatcher
implements Serializable,
AWTEventListener {
    private static final long serialVersionUID = 5184291520170872969L;
    private static final int LWD_MOUSE_DRAGGED_OVER = 1500;
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.LightweightDispatcher");
    private static final int BUTTONS_DOWN_MASK;
    private Container nativeContainer;
    private Component focus;
    private transient WeakReference<Component> mouseEventTarget;
    private transient WeakReference<Component> targetLastEntered;
    private transient WeakReference<Component> targetLastEnteredDT;
    private transient boolean isMouseInNativeContainer = false;
    private transient boolean isMouseDTInNativeContainer = false;
    private Cursor nativeCursor;
    private long eventMask;
    private static final long PROXY_EVENT_MASK = 131132L;
    private static final long MOUSE_MASK = 131120L;

    LightweightDispatcher(Container nativeContainer) {
        this.nativeContainer = nativeContainer;
        this.mouseEventTarget = new WeakReference<Object>(null);
        this.targetLastEntered = new WeakReference<Object>(null);
        this.targetLastEnteredDT = new WeakReference<Object>(null);
        this.eventMask = 0L;
    }

    void dispose() {
        this.stopListeningForOtherDrags();
        this.mouseEventTarget.clear();
        this.targetLastEntered.clear();
        this.targetLastEnteredDT.clear();
    }

    void enableEvents(long events) {
        this.eventMask |= events;
    }

    boolean dispatchEvent(AWTEvent e) {
        boolean ret = false;
        if (e instanceof SunDropTargetEvent) {
            SunDropTargetEvent sdde = (SunDropTargetEvent)e;
            ret = this.processDropTargetEvent(sdde);
        } else {
            if (e instanceof MouseEvent && (this.eventMask & 0x20030L) != 0L) {
                MouseEvent me = (MouseEvent)e;
                ret = this.processMouseEvent(me);
            }
            if (e.getID() == 503) {
                this.nativeContainer.updateCursorImmediately();
            }
        }
        return ret;
    }

    private boolean isMouseGrab(MouseEvent e) {
        int modifiers = e.getModifiersEx();
        if (e.getID() == 501 || e.getID() == 502) {
            modifiers ^= InputEvent.getMaskForButton(e.getButton());
        }
        return (modifiers & BUTTONS_DOWN_MASK) != 0;
    }

    private boolean processMouseEvent(MouseEvent e) {
        int id = e.getID();
        Component mouseOver = this.nativeContainer.getMouseEventTarget(e.getX(), e.getY(), true);
        this.trackMouseEnterExit(mouseOver, e);
        Component met = (Component)this.mouseEventTarget.get();
        if (!this.isMouseGrab(e) && id != 500) {
            met = mouseOver != this.nativeContainer ? mouseOver : null;
            this.mouseEventTarget = new WeakReference<Component>(met);
        }
        if (met != null) {
            switch (id) {
                case 504: 
                case 505: {
                    break;
                }
                case 501: {
                    this.retargetMouseEvent(met, id, e);
                    break;
                }
                case 502: {
                    this.retargetMouseEvent(met, id, e);
                    break;
                }
                case 500: {
                    if (mouseOver != met) break;
                    this.retargetMouseEvent(mouseOver, id, e);
                    break;
                }
                case 503: {
                    this.retargetMouseEvent(met, id, e);
                    break;
                }
                case 506: {
                    if (!this.isMouseGrab(e)) break;
                    this.retargetMouseEvent(met, id, e);
                    break;
                }
                case 507: {
                    if (eventLog.isLoggable(PlatformLogger.Level.FINEST) && mouseOver != null) {
                        eventLog.finest("retargeting mouse wheel to " + mouseOver.getName() + ", " + String.valueOf(mouseOver.getClass()));
                    }
                    this.retargetMouseEvent(mouseOver, id, e);
                }
            }
            if (id != 507) {
                e.consume();
            }
        }
        return e.isConsumed();
    }

    private boolean processDropTargetEvent(SunDropTargetEvent e) {
        int y;
        int id = e.getID();
        int x = e.getX();
        if (!this.nativeContainer.contains(x, y = e.getY())) {
            Dimension d = this.nativeContainer.getSize();
            if (d.width <= x) {
                x = d.width - 1;
            } else if (x < 0) {
                x = 0;
            }
            if (d.height <= y) {
                y = d.height - 1;
            } else if (y < 0) {
                y = 0;
            }
        }
        Component mouseOver = this.nativeContainer.getDropTargetEventTarget(x, y, true);
        this.trackMouseEnterExit(mouseOver, e);
        if (mouseOver != this.nativeContainer && mouseOver != null) {
            switch (id) {
                case 504: 
                case 505: {
                    break;
                }
                default: {
                    this.retargetMouseEvent(mouseOver, id, e);
                    e.consume();
                }
            }
        }
        return e.isConsumed();
    }

    private void trackDropTargetEnterExit(Component targetOver, MouseEvent e) {
        int id = e.getID();
        if (id == 504 && this.isMouseDTInNativeContainer) {
            this.targetLastEnteredDT.clear();
        } else if (id == 504) {
            this.isMouseDTInNativeContainer = true;
        } else if (id == 505) {
            this.isMouseDTInNativeContainer = false;
        }
        Component tle = this.retargetMouseEnterExit(targetOver, e, (Component)this.targetLastEnteredDT.get(), this.isMouseDTInNativeContainer);
        this.targetLastEnteredDT = new WeakReference<Component>(tle);
    }

    private void trackMouseEnterExit(Component targetOver, MouseEvent e) {
        if (e instanceof SunDropTargetEvent) {
            this.trackDropTargetEnterExit(targetOver, e);
            return;
        }
        int id = e.getID();
        if (id != 505 && id != 506 && id != 1500 && !this.isMouseInNativeContainer) {
            this.isMouseInNativeContainer = true;
            this.startListeningForOtherDrags();
        } else if (id == 505) {
            this.isMouseInNativeContainer = false;
            this.stopListeningForOtherDrags();
        }
        Component tle = this.retargetMouseEnterExit(targetOver, e, (Component)this.targetLastEntered.get(), this.isMouseInNativeContainer);
        this.targetLastEntered = new WeakReference<Component>(tle);
    }

    private Component retargetMouseEnterExit(Component targetOver, MouseEvent e, Component lastEntered, boolean inNativeContainer) {
        Component targetEnter;
        int id = e.getID();
        Component component = targetEnter = inNativeContainer ? targetOver : null;
        if (lastEntered != targetEnter) {
            if (lastEntered != null) {
                this.retargetMouseEvent(lastEntered, 505, e);
            }
            if (id == 505) {
                e.consume();
            }
            if (targetEnter != null) {
                this.retargetMouseEvent(targetEnter, 504, e);
            }
            if (id == 504) {
                e.consume();
            }
        }
        return targetEnter;
    }

    private void startListeningForOtherDrags() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                LightweightDispatcher.this.nativeContainer.getToolkit().addAWTEventListener(LightweightDispatcher.this, 48L);
                return null;
            }
        });
    }

    private void stopListeningForOtherDrags() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                LightweightDispatcher.this.nativeContainer.getToolkit().removeAWTEventListener(LightweightDispatcher.this);
                return null;
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void eventDispatched(AWTEvent e) {
        MouseEvent me;
        boolean isForeignDrag;
        boolean bl = isForeignDrag = e instanceof MouseEvent && !(e instanceof SunDropTargetEvent) && e.id == 506 && e.getSource() != this.nativeContainer;
        if (!isForeignDrag) {
            return;
        }
        MouseEvent srcEvent = (MouseEvent)e;
        Object object = this.nativeContainer.getTreeLock();
        synchronized (object) {
            Container c;
            Component srcComponent = srcEvent.getComponent();
            if (!srcComponent.isShowing()) {
                return;
            }
            for (c = this.nativeContainer; c != null && !(c instanceof Window); c = c.getParent_NoClientCode()) {
            }
            if (c == null || ((Window)c).isModalBlocked()) {
                return;
            }
            me = new MouseEvent(this.nativeContainer, 1500, srcEvent.getWhen(), srcEvent.getModifiersEx() | srcEvent.getModifiers(), srcEvent.getX(), srcEvent.getY(), srcEvent.getXOnScreen(), srcEvent.getYOnScreen(), srcEvent.getClickCount(), srcEvent.isPopupTrigger(), srcEvent.getButton());
            AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
            meAccessor.setCausedByTouchEvent(me, meAccessor.isCausedByTouchEvent(srcEvent));
            srcEvent.copyPrivateDataInto(me);
            final Point ptSrcOrigin = srcComponent.getLocationOnScreen();
            if (AppContext.getAppContext() != this.nativeContainer.appContext) {
                final MouseEvent mouseEvent = me;
                Runnable r = new Runnable(){
                    final /* synthetic */ LightweightDispatcher this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        if (!this.this$0.nativeContainer.isShowing()) {
                            return;
                        }
                        Point ptDstOrigin = this.this$0.nativeContainer.getLocationOnScreen();
                        mouseEvent.translatePoint(ptSrcOrigin.x - ptDstOrigin.x, ptSrcOrigin.y - ptDstOrigin.y);
                        Component targetOver = this.this$0.nativeContainer.getMouseEventTarget(mouseEvent.getX(), mouseEvent.getY(), true);
                        this.this$0.trackMouseEnterExit(targetOver, mouseEvent);
                    }
                };
                SunToolkit.executeOnEventHandlerThread(this.nativeContainer, r);
                return;
            }
            if (!this.nativeContainer.isShowing()) {
                return;
            }
            Point ptDstOrigin = this.nativeContainer.getLocationOnScreen();
            me.translatePoint(ptSrcOrigin.x - ptDstOrigin.x, ptSrcOrigin.y - ptDstOrigin.y);
        }
        Component targetOver = this.nativeContainer.getMouseEventTarget(me.getX(), me.getY(), true);
        this.trackMouseEnterExit(targetOver, me);
    }

    void retargetMouseEvent(Component target, int id, MouseEvent e) {
        Component component;
        if (target == null) {
            return;
        }
        int x = e.getX();
        int y = e.getY();
        for (component = target; component != null && component != this.nativeContainer; component = component.getParent()) {
            x -= component.x;
            y -= component.y;
        }
        if (component != null) {
            MouseEvent retargeted;
            if (e instanceof SunDropTargetEvent) {
                retargeted = new SunDropTargetEvent(target, id, x, y, ((SunDropTargetEvent)e).getDispatcher());
            } else if (id == 507) {
                retargeted = new MouseWheelEvent(target, id, e.getWhen(), e.getModifiersEx() | e.getModifiers(), x, y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), ((MouseWheelEvent)e).getScrollType(), ((MouseWheelEvent)e).getScrollAmount(), ((MouseWheelEvent)e).getWheelRotation(), ((MouseWheelEvent)e).getPreciseWheelRotation());
            } else {
                retargeted = new MouseEvent(target, id, e.getWhen(), e.getModifiersEx() | e.getModifiers(), x, y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
                AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                meAccessor.setCausedByTouchEvent(retargeted, meAccessor.isCausedByTouchEvent(e));
            }
            e.copyPrivateDataInto(retargeted);
            if (target == this.nativeContainer) {
                ((Container)target).dispatchEventToSelf(retargeted);
            } else {
                assert (AppContext.getAppContext() == target.appContext);
                if (this.nativeContainer.modalComp != null) {
                    if (((Container)this.nativeContainer.modalComp).isAncestorOf(target)) {
                        target.dispatchEvent(retargeted);
                    } else {
                        e.consume();
                    }
                } else {
                    target.dispatchEvent(retargeted);
                }
            }
            if (id == 507 && retargeted.isConsumed()) {
                e.consume();
            }
        }
    }

    static {
        int[] buttonsDownMask = AWTAccessor.getInputEventAccessor().getButtonDownMasks();
        int mask = 0;
        for (int buttonDownMask : buttonsDownMask) {
            mask |= buttonDownMask;
        }
        BUTTONS_DOWN_MASK = mask;
    }
}

