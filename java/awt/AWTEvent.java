/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.List;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.EventObject;
import sun.awt.AWTAccessor;

public abstract class AWTEvent
extends EventObject {
    private byte[] bdata;
    protected int id;
    protected boolean consumed = false;
    private volatile transient AccessControlContext acc = AccessController.getContext();
    transient boolean focusManagerIsDispatching = false;
    transient boolean isPosted;
    private transient boolean isSystemGenerated;
    public static final long COMPONENT_EVENT_MASK = 1L;
    public static final long CONTAINER_EVENT_MASK = 2L;
    public static final long FOCUS_EVENT_MASK = 4L;
    public static final long KEY_EVENT_MASK = 8L;
    public static final long MOUSE_EVENT_MASK = 16L;
    public static final long MOUSE_MOTION_EVENT_MASK = 32L;
    public static final long WINDOW_EVENT_MASK = 64L;
    public static final long ACTION_EVENT_MASK = 128L;
    public static final long ADJUSTMENT_EVENT_MASK = 256L;
    public static final long ITEM_EVENT_MASK = 512L;
    public static final long TEXT_EVENT_MASK = 1024L;
    public static final long INPUT_METHOD_EVENT_MASK = 2048L;
    static final long INPUT_METHODS_ENABLED_MASK = 4096L;
    public static final long PAINT_EVENT_MASK = 8192L;
    public static final long INVOCATION_EVENT_MASK = 16384L;
    public static final long HIERARCHY_EVENT_MASK = 32768L;
    public static final long HIERARCHY_BOUNDS_EVENT_MASK = 65536L;
    public static final long MOUSE_WHEEL_EVENT_MASK = 131072L;
    public static final long WINDOW_STATE_EVENT_MASK = 262144L;
    public static final long WINDOW_FOCUS_EVENT_MASK = 524288L;
    public static final int RESERVED_ID_MAX = 1999;
    private static final long serialVersionUID = -1825314779160409405L;

    final AccessControlContext getAccessControlContext() {
        if (this.acc == null) {
            throw new SecurityException("AWTEvent is missing AccessControlContext");
        }
        return this.acc;
    }

    private static native void initIDs();

    @Deprecated(since="9")
    public AWTEvent(Event event) {
        this(event.target, event.id);
    }

    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        switch (id) {
            case 601: 
            case 701: 
            case 900: 
            case 1001: {
                this.consumed = true;
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setSource(Object newSource) {
        if (this.source == newSource) {
            return;
        }
        Component comp = null;
        if (newSource instanceof Component) {
            comp = (Component)newSource;
            while (comp != null && comp.peer instanceof LightweightPeer) {
                comp = comp.parent;
            }
        }
        AWTEvent aWTEvent = this;
        synchronized (aWTEvent) {
            ComponentPeer peer;
            this.source = newSource;
            if (comp != null && (peer = comp.peer) != null) {
                this.nativeSetSource(peer);
            }
        }
    }

    private native void nativeSetSource(ComponentPeer var1);

    public int getID() {
        return this.id;
    }

    @Override
    public String toString() {
        String srcName = null;
        if (this.source instanceof Component) {
            srcName = ((Component)this.source).getName();
        } else if (this.source instanceof MenuComponent) {
            srcName = ((MenuComponent)this.source).getName();
        }
        return this.getClass().getName() + "[" + this.paramString() + "] on " + String.valueOf(srcName != null ? srcName : this.source);
    }

    public String paramString() {
        return "";
    }

    protected void consume() {
        switch (this.id) {
            case 401: 
            case 402: 
            case 501: 
            case 502: 
            case 503: 
            case 504: 
            case 505: 
            case 506: 
            case 507: 
            case 1100: 
            case 1101: {
                this.consumed = true;
                break;
            }
        }
    }

    protected boolean isConsumed() {
        return this.consumed;
    }

    Event convertToOld() {
        Object src = this.getSource();
        int newid = this.id;
        switch (this.id) {
            case 401: 
            case 402: {
                int keyCode;
                KeyEvent ke = (KeyEvent)this;
                if (ke.isActionKey()) {
                    int n = newid = this.id == 401 ? 403 : 404;
                }
                if ((keyCode = ke.getKeyCode()) == 16 || keyCode == 17 || keyCode == 18) {
                    return null;
                }
                return new Event(src, ke.getWhen(), newid, 0, 0, Event.getOldEventKey(ke), ke.getModifiers() & 0xFFFFFFEF);
            }
            case 501: 
            case 502: 
            case 503: 
            case 504: 
            case 505: 
            case 506: {
                MouseEvent me = (MouseEvent)this;
                Event olde = new Event(src, me.getWhen(), newid, me.getX(), me.getY(), 0, me.getModifiers() & 0xFFFFFFEF);
                olde.clickCount = me.getClickCount();
                return olde;
            }
            case 1004: {
                return new Event(src, 1004, null);
            }
            case 1005: {
                return new Event(src, 1005, null);
            }
            case 201: 
            case 203: 
            case 204: {
                return new Event(src, newid, null);
            }
            case 100: {
                if (!(src instanceof Frame) && !(src instanceof Dialog)) break;
                Point p = ((Component)src).getLocation();
                return new Event(src, 0L, 205, p.x, p.y, 0, 0);
            }
            case 1001: {
                ActionEvent ae = (ActionEvent)this;
                String cmd = src instanceof Button ? ((Button)src).getLabel() : (src instanceof MenuItem ? ((MenuItem)src).getLabel() : ae.getActionCommand());
                return new Event(src, 0L, newid, 0, 0, 0, ae.getModifiers(), cmd);
            }
            case 701: {
                Object arg;
                ItemEvent ie = (ItemEvent)this;
                if (src instanceof List) {
                    newid = ie.getStateChange() == 1 ? 701 : 702;
                    arg = ie.getItem();
                } else {
                    newid = 1001;
                    arg = src instanceof Choice ? ie.getItem() : Boolean.valueOf(ie.getStateChange() == 1);
                }
                return new Event(src, newid, arg);
            }
            case 601: {
                AdjustmentEvent aje = (AdjustmentEvent)this;
                switch (aje.getAdjustmentType()) {
                    case 1: {
                        newid = 602;
                        break;
                    }
                    case 2: {
                        newid = 601;
                        break;
                    }
                    case 4: {
                        newid = 604;
                        break;
                    }
                    case 3: {
                        newid = 603;
                        break;
                    }
                    case 5: {
                        if (aje.getValueIsAdjusting()) {
                            newid = 605;
                            break;
                        }
                        newid = 607;
                        break;
                    }
                    default: {
                        return null;
                    }
                }
                return new Event(src, newid, aje.getValue());
            }
        }
        return null;
    }

    void copyPrivateDataInto(AWTEvent that) {
        that.bdata = this.bdata;
        if (this instanceof InputEvent && that instanceof InputEvent) {
            AWTAccessor.InputEventAccessor accessor = AWTAccessor.getInputEventAccessor();
            boolean b = accessor.canAccessSystemClipboard((InputEvent)this);
            accessor.setCanAccessSystemClipboard((InputEvent)that, b);
        }
        that.isSystemGenerated = this.isSystemGenerated;
    }

    void dispatched() {
        if (this instanceof InputEvent) {
            AWTAccessor.getInputEventAccessor().setCanAccessSystemClipboard((InputEvent)this, false);
        }
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            AWTEvent.initIDs();
        }
        AWTAccessor.setAWTEventAccessor(new AWTAccessor.AWTEventAccessor(){

            @Override
            public void setPosted(AWTEvent ev) {
                ev.isPosted = true;
            }

            @Override
            public void setSystemGenerated(AWTEvent ev) {
                ev.isSystemGenerated = true;
            }

            @Override
            public boolean isSystemGenerated(AWTEvent ev) {
                return ev.isSystemGenerated;
            }

            @Override
            public AccessControlContext getAccessControlContext(AWTEvent ev) {
                return ev.getAccessControlContext();
            }

            @Override
            public byte[] getBData(AWTEvent ev) {
                return ev.bdata;
            }

            @Override
            public void setBData(AWTEvent ev, byte[] bdata) {
                ev.bdata = bdata;
            }
        });
    }
}

