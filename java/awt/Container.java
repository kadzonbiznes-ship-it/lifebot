/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventDispatchThread;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsCallback;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.LightweightDispatcher;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.SequencedEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LightweightPeer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.OptionalDataException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.swing.JInternalFrame;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;
import sun.java2d.pipe.Region;
import sun.security.action.GetBooleanAction;
import sun.util.logging.PlatformLogger;

public class Container
extends Component {
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Container");
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.Container");
    private static final Component[] EMPTY_ARRAY = new Component[0];
    private List<Component> component = new ArrayList<Component>();
    LayoutManager layoutMgr;
    private LightweightDispatcher dispatcher;
    private transient FocusTraversalPolicy focusTraversalPolicy;
    private boolean focusCycleRoot = false;
    private boolean focusTraversalPolicyProvider;
    private transient Set<Thread> printingThreads;
    private transient boolean printing = false;
    transient ContainerListener containerListener;
    transient int listeningChildren;
    transient int listeningBoundsChildren;
    transient int descendantsCount;
    transient Color preserveBackgroundColor = null;
    private static final long serialVersionUID = 4613797578919906343L;
    static final boolean INCLUDE_SELF = true;
    static final boolean SEARCH_HEAVYWEIGHTS = true;
    private transient int numOfHWComponents = 0;
    private transient int numOfLWComponents = 0;
    private static final PlatformLogger mixingLog = PlatformLogger.getLogger("java.awt.mixing.Container");
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("ncomponents", Integer.TYPE), new ObjectStreamField("component", Component[].class), new ObjectStreamField("layoutMgr", LayoutManager.class), new ObjectStreamField("dispatcher", LightweightDispatcher.class), new ObjectStreamField("maxSize", Dimension.class), new ObjectStreamField("focusCycleRoot", Boolean.TYPE), new ObjectStreamField("containerSerializedDataVersion", Integer.TYPE), new ObjectStreamField("focusTraversalPolicyProvider", Boolean.TYPE)};
    private static final boolean isJavaAwtSmartInvalidate;
    private static boolean descendUnconditionallyWhenValidating;
    transient Component modalComp;
    transient AppContext modalAppContext;
    private int containerSerializedDataVersion = 1;

    private static native void initIDs();

    @Override
    void initializeFocusTraversalKeys() {
        this.focusTraversalKeys = new Set[4];
    }

    public int getComponentCount() {
        return this.countComponents();
    }

    @Deprecated
    public int countComponents() {
        return this.component.size();
    }

    public Component getComponent(int n) {
        try {
            return this.component.get(n);
        }
        catch (IndexOutOfBoundsException z) {
            throw new ArrayIndexOutOfBoundsException("No such child: " + n);
        }
    }

    public Component[] getComponents() {
        return this.getComponents_NoClientCode();
    }

    final Component[] getComponents_NoClientCode() {
        return this.component.toArray(EMPTY_ARRAY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Component[] getComponentsSync() {
        Object object = this.getTreeLock();
        synchronized (object) {
            return this.getComponents();
        }
    }

    public Insets getInsets() {
        return this.insets();
    }

    @Deprecated
    public Insets insets() {
        ComponentPeer peer = this.peer;
        if (peer instanceof ContainerPeer) {
            ContainerPeer cpeer = (ContainerPeer)peer;
            return (Insets)cpeer.getInsets().clone();
        }
        return new Insets(0, 0, 0, 0);
    }

    public Component add(Component comp) {
        this.addImpl(comp, null, -1);
        return comp;
    }

    public Component add(String name, Component comp) {
        this.addImpl(comp, name, -1);
        return comp;
    }

    public Component add(Component comp, int index) {
        this.addImpl(comp, null, index);
        return comp;
    }

    private void checkAddToSelf(Component comp) {
        if (comp instanceof Container) {
            Container cn = this;
            while (cn != null) {
                if (cn == comp) {
                    throw new IllegalArgumentException("adding container's parent to itself");
                }
                cn = cn.parent;
            }
        }
    }

    private void checkNotAWindow(Component comp) {
        if (comp instanceof Window) {
            throw new IllegalArgumentException("adding a window to a container");
        }
    }

    private void checkAdding(Component comp, int index) {
        this.checkTreeLock();
        GraphicsConfiguration thisGC = this.getGraphicsConfiguration();
        if (index > this.component.size() || index < 0) {
            throw new IllegalArgumentException("illegal component position");
        }
        if (comp.parent == this && index == this.component.size()) {
            throw new IllegalArgumentException("illegal component position " + index + " should be less than " + this.component.size());
        }
        this.checkAddToSelf(comp);
        this.checkNotAWindow(comp);
        Window thisTopLevel = this.getContainingWindow();
        Window compTopLevel = comp.getContainingWindow();
        if (thisTopLevel != compTopLevel) {
            throw new IllegalArgumentException("component and container should be in the same top-level window");
        }
        if (thisGC != null) {
            comp.checkGD(thisGC.getDevice().getIDstring());
        }
    }

    private boolean removeDelicately(Component comp, Container newParent, int newIndex) {
        this.checkTreeLock();
        int index = this.getComponentZOrder(comp);
        boolean needRemoveNotify = Container.isRemoveNotifyNeeded(comp, this, newParent);
        if (needRemoveNotify) {
            comp.removeNotify();
        }
        if (newParent != this) {
            if (this.layoutMgr != null) {
                this.layoutMgr.removeLayoutComponent(comp);
            }
            this.adjustListeningChildren(32768L, -comp.numListening(32768L));
            this.adjustListeningChildren(65536L, -comp.numListening(65536L));
            this.adjustDescendants(-comp.countHierarchyMembers());
            comp.parent = null;
            if (needRemoveNotify) {
                comp.setGraphicsConfiguration(null);
            }
            this.component.remove(index);
            this.invalidateIfValid();
        } else {
            this.component.remove(index);
            this.component.add(newIndex, comp);
        }
        if (comp.parent == null) {
            if (this.containerListener != null || (this.eventMask & 2L) != 0L || Toolkit.enabledOnToolkit(2L)) {
                ContainerEvent e = new ContainerEvent(this, 301, comp);
                this.dispatchEvent(e);
            }
            comp.createHierarchyEvents(1400, comp, this, 1L, Toolkit.enabledOnToolkit(32768L));
            if (this.peer != null && this.layoutMgr == null && this.isVisible()) {
                this.updateCursorImmediately();
            }
        }
        return needRemoveNotify;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean canContainFocusOwner(Component focusOwnerCandidate) {
        FocusTraversalPolicy policy;
        if (!(this.isEnabled() && this.isDisplayable() && this.isVisible() && this.isFocusable())) {
            return false;
        }
        if (this.isFocusCycleRoot() && (policy = this.getFocusTraversalPolicy()) instanceof DefaultFocusTraversalPolicy && !((DefaultFocusTraversalPolicy)policy).accept(focusOwnerCandidate)) {
            return false;
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.parent != null) {
                return this.parent.canContainFocusOwner(focusOwnerCandidate);
            }
        }
        return true;
    }

    final boolean hasHeavyweightDescendants() {
        this.checkTreeLock();
        return this.numOfHWComponents > 0;
    }

    final boolean hasLightweightDescendants() {
        this.checkTreeLock();
        return this.numOfLWComponents > 0;
    }

    Container getHeavyweightContainer() {
        this.checkTreeLock();
        if (this.peer != null && !(this.peer instanceof LightweightPeer)) {
            return this;
        }
        return this.getNativeContainer();
    }

    private static boolean isRemoveNotifyNeeded(Component comp, Container oldContainer, Container newContainer) {
        Container oldNativeContainer;
        boolean isContainer;
        if (oldContainer == null) {
            return false;
        }
        if (comp.peer == null) {
            return false;
        }
        if (newContainer.peer == null) {
            return true;
        }
        if (comp.isLightweight() && (!(isContainer = comp instanceof Container) || isContainer && !((Container)comp).hasHeavyweightDescendants())) {
            return false;
        }
        Container newNativeContainer = oldContainer.getHeavyweightContainer();
        if (newNativeContainer != (oldNativeContainer = newContainer.getHeavyweightContainer())) {
            return !comp.peer.isReparentSupported();
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setComponentZOrder(Component comp, int index) {
        Object object = this.getTreeLock();
        synchronized (object) {
            Container curParent = comp.parent;
            int oldZindex = this.getComponentZOrder(comp);
            if (curParent == this && index == oldZindex) {
                return;
            }
            this.checkAdding(comp, index);
            boolean peerRecreated = curParent != null ? curParent.removeDelicately(comp, this, index) : false;
            this.addDelicately(comp, curParent, index);
            if (!peerRecreated && oldZindex != -1) {
                comp.mixOnZOrderChanging(oldZindex, index);
            }
        }
    }

    private void reparentTraverse(ContainerPeer parentPeer, Container child) {
        this.checkTreeLock();
        for (int i = 0; i < child.getComponentCount(); ++i) {
            Component comp = child.getComponent(i);
            if (comp.isLightweight()) {
                if (!(comp instanceof Container)) continue;
                this.reparentTraverse(parentPeer, (Container)comp);
                continue;
            }
            comp.peer.reparent(parentPeer);
        }
    }

    private void reparentChild(Component comp) {
        this.checkTreeLock();
        if (comp == null) {
            return;
        }
        if (comp.isLightweight()) {
            if (comp instanceof Container) {
                this.reparentTraverse((ContainerPeer)this.peer, (Container)comp);
            }
        } else {
            comp.peer.reparent((ContainerPeer)this.peer);
        }
    }

    private void addDelicately(Component comp, Container curParent, int index) {
        this.checkTreeLock();
        if (curParent != this) {
            if (index == -1) {
                this.component.add(comp);
            } else {
                this.component.add(index, comp);
            }
            comp.parent = this;
            comp.setGraphicsConfiguration(this.getGraphicsConfiguration());
            this.adjustListeningChildren(32768L, comp.numListening(32768L));
            this.adjustListeningChildren(65536L, comp.numListening(65536L));
            this.adjustDescendants(comp.countHierarchyMembers());
        } else if (index < this.component.size()) {
            this.component.set(index, comp);
        }
        this.invalidateIfValid();
        if (this.peer != null) {
            if (comp.peer == null) {
                comp.addNotify();
            } else {
                Container newNativeContainer = this.getHeavyweightContainer();
                Container oldNativeContainer = curParent.getHeavyweightContainer();
                if (oldNativeContainer != newNativeContainer) {
                    newNativeContainer.reparentChild(comp);
                }
                comp.updateZOrder();
                if (!comp.isLightweight() && this.isLightweight()) {
                    comp.relocateComponent();
                }
            }
        }
        if (curParent != this) {
            Component focusOwner;
            if (this.layoutMgr != null) {
                if (this.layoutMgr instanceof LayoutManager2) {
                    ((LayoutManager2)this.layoutMgr).addLayoutComponent(comp, null);
                } else {
                    this.layoutMgr.addLayoutComponent(null, comp);
                }
            }
            if (this.containerListener != null || (this.eventMask & 2L) != 0L || Toolkit.enabledOnToolkit(2L)) {
                ContainerEvent e = new ContainerEvent(this, 300, comp);
                this.dispatchEvent(e);
            }
            comp.createHierarchyEvents(1400, comp, this, 1L, Toolkit.enabledOnToolkit(32768L));
            if (comp.isFocusOwner() && !comp.canBeFocusOwnerRecursively()) {
                comp.transferFocus();
            } else if (comp instanceof Container && (focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) != null && this.isParentOf(focusOwner) && !focusOwner.canBeFocusOwnerRecursively()) {
                focusOwner.transferFocus();
            }
        } else {
            comp.createHierarchyEvents(1400, comp, this, 1400L, Toolkit.enabledOnToolkit(32768L));
        }
        if (this.peer != null && this.layoutMgr == null && this.isVisible()) {
            this.updateCursorImmediately();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getComponentZOrder(Component comp) {
        if (comp == null) {
            return -1;
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            if (comp.parent != this) {
                return -1;
            }
            return this.component.indexOf(comp);
        }
    }

    public void add(Component comp, Object constraints) {
        this.addImpl(comp, constraints, -1);
    }

    public void add(Component comp, Object constraints, int index) {
        this.addImpl(comp, constraints, index);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        Object object = this.getTreeLock();
        synchronized (object) {
            GraphicsConfiguration thisGC = this.getGraphicsConfiguration();
            if (index > this.component.size() || index < 0 && index != -1) {
                throw new IllegalArgumentException("illegal component position");
            }
            this.checkAddToSelf(comp);
            this.checkNotAWindow(comp);
            if (comp.parent != null) {
                comp.parent.remove(comp);
                if (index > this.component.size()) {
                    throw new IllegalArgumentException("illegal component position");
                }
            }
            if (thisGC != null) {
                comp.checkGD(thisGC.getDevice().getIDstring());
            }
            if (index == -1) {
                this.component.add(comp);
            } else {
                this.component.add(index, comp);
            }
            comp.parent = this;
            comp.setGraphicsConfiguration(thisGC);
            this.adjustListeningChildren(32768L, comp.numListening(32768L));
            this.adjustListeningChildren(65536L, comp.numListening(65536L));
            this.adjustDescendants(comp.countHierarchyMembers());
            this.invalidateIfValid();
            if (this.peer != null) {
                comp.addNotify();
            }
            if (this.layoutMgr != null) {
                if (this.layoutMgr instanceof LayoutManager2) {
                    ((LayoutManager2)this.layoutMgr).addLayoutComponent(comp, constraints);
                } else if (constraints instanceof String) {
                    this.layoutMgr.addLayoutComponent((String)constraints, comp);
                }
            }
            if (this.containerListener != null || (this.eventMask & 2L) != 0L || Toolkit.enabledOnToolkit(2L)) {
                ContainerEvent e = new ContainerEvent(this, 300, comp);
                this.dispatchEvent(e);
            }
            comp.createHierarchyEvents(1400, comp, this, 1L, Toolkit.enabledOnToolkit(32768L));
            if (this.peer != null && this.layoutMgr == null && this.isVisible()) {
                this.updateCursorImmediately();
            }
        }
    }

    @Override
    final boolean updateChildGraphicsData(GraphicsConfiguration gc) {
        this.checkTreeLock();
        boolean ret = false;
        for (Component comp : this.component) {
            if (comp == null) continue;
            ret |= comp.updateGraphicsData(gc);
        }
        return ret;
    }

    @Override
    void checkGD(String stringID) {
        for (Component comp : this.component) {
            if (comp == null) continue;
            comp.checkGD(stringID);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove(int index) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (index < 0 || index >= this.component.size()) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            Component comp = this.component.get(index);
            if (this.peer != null) {
                comp.removeNotify();
            }
            if (this.layoutMgr != null) {
                this.layoutMgr.removeLayoutComponent(comp);
            }
            this.adjustListeningChildren(32768L, -comp.numListening(32768L));
            this.adjustListeningChildren(65536L, -comp.numListening(65536L));
            this.adjustDescendants(-comp.countHierarchyMembers());
            comp.parent = null;
            this.component.remove(index);
            comp.setGraphicsConfiguration(null);
            this.invalidateIfValid();
            if (this.containerListener != null || (this.eventMask & 2L) != 0L || Toolkit.enabledOnToolkit(2L)) {
                ContainerEvent e = new ContainerEvent(this, 301, comp);
                this.dispatchEvent(e);
            }
            comp.createHierarchyEvents(1400, comp, this, 1L, Toolkit.enabledOnToolkit(32768L));
            if (this.peer != null && this.layoutMgr == null && this.isVisible()) {
                this.updateCursorImmediately();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove(Component comp) {
        Object object = this.getTreeLock();
        synchronized (object) {
            int index;
            if (comp.parent == this && (index = this.component.indexOf(comp)) >= 0) {
                this.remove(index);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAll() {
        Object object = this.getTreeLock();
        synchronized (object) {
            this.adjustListeningChildren(32768L, -this.listeningChildren);
            this.adjustListeningChildren(65536L, -this.listeningBoundsChildren);
            this.adjustDescendants(-this.descendantsCount);
            while (!this.component.isEmpty()) {
                Component comp = this.component.remove(this.component.size() - 1);
                if (this.peer != null) {
                    comp.removeNotify();
                }
                if (this.layoutMgr != null) {
                    this.layoutMgr.removeLayoutComponent(comp);
                }
                comp.parent = null;
                comp.setGraphicsConfiguration(null);
                if (this.containerListener != null || (this.eventMask & 2L) != 0L || Toolkit.enabledOnToolkit(2L)) {
                    ContainerEvent e = new ContainerEvent(this, 301, comp);
                    this.dispatchEvent(e);
                }
                comp.createHierarchyEvents(1400, comp, this, 1L, Toolkit.enabledOnToolkit(32768L));
            }
            if (this.peer != null && this.layoutMgr == null && this.isVisible()) {
                this.updateCursorImmediately();
            }
            this.invalidateIfValid();
        }
    }

    @Override
    int numListening(long mask) {
        int superListening = super.numListening(mask);
        if (mask == 32768L) {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                int sum = 0;
                for (Component comp : this.component) {
                    sum += comp.numListening(mask);
                }
                if (this.listeningChildren != sum) {
                    eventLog.fine("Assertion (listeningChildren == sum) failed");
                }
            }
            return this.listeningChildren + superListening;
        }
        if (mask == 65536L) {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                int sum = 0;
                for (Component comp : this.component) {
                    sum += comp.numListening(mask);
                }
                if (this.listeningBoundsChildren != sum) {
                    eventLog.fine("Assertion (listeningBoundsChildren == sum) failed");
                }
            }
            return this.listeningBoundsChildren + superListening;
        }
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("This code must never be reached");
        }
        return superListening;
    }

    void adjustListeningChildren(long mask, int num) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            boolean toAssert;
            boolean bl = toAssert = mask == 32768L || mask == 65536L || mask == 98304L;
            if (!toAssert) {
                eventLog.fine("Assertion failed");
            }
        }
        if (num == 0) {
            return;
        }
        if ((mask & 0x8000L) != 0L) {
            this.listeningChildren += num;
        }
        if ((mask & 0x10000L) != 0L) {
            this.listeningBoundsChildren += num;
        }
        this.adjustListeningChildrenOnParent(mask, num);
    }

    void adjustDescendants(int num) {
        if (num == 0) {
            return;
        }
        this.descendantsCount += num;
        this.adjustDescendantsOnParent(num);
    }

    void adjustDescendantsOnParent(int num) {
        if (this.parent != null) {
            this.parent.adjustDescendants(num);
        }
    }

    @Override
    int countHierarchyMembers() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            int sum = 0;
            for (Component comp : this.component) {
                sum += comp.countHierarchyMembers();
            }
            if (this.descendantsCount != sum) {
                log.fine("Assertion (descendantsCount == sum) failed");
            }
        }
        return this.descendantsCount + 1;
    }

    private int getListenersCount(int id, boolean enabledOnToolkit) {
        this.checkTreeLock();
        if (enabledOnToolkit) {
            return this.descendantsCount;
        }
        switch (id) {
            case 1400: {
                return this.listeningChildren;
            }
            case 1401: 
            case 1402: {
                return this.listeningBoundsChildren;
            }
        }
        return 0;
    }

    @Override
    final int createHierarchyEvents(int id, Component changed, Container changedParent, long changeFlags, boolean enabledOnToolkit) {
        int listeners;
        this.checkTreeLock();
        int count = listeners = this.getListenersCount(id, enabledOnToolkit);
        int i = 0;
        while (count > 0) {
            count -= this.component.get(i).createHierarchyEvents(id, changed, changedParent, changeFlags, enabledOnToolkit);
            ++i;
        }
        return listeners + super.createHierarchyEvents(id, changed, changedParent, changeFlags, enabledOnToolkit);
    }

    final void createChildHierarchyEvents(int id, long changeFlags, boolean enabledOnToolkit) {
        int listeners;
        this.checkTreeLock();
        if (this.component.isEmpty()) {
            return;
        }
        int count = listeners = this.getListenersCount(id, enabledOnToolkit);
        int i = 0;
        while (count > 0) {
            count -= this.component.get(i).createHierarchyEvents(id, this, this.parent, changeFlags, enabledOnToolkit);
            ++i;
        }
    }

    public LayoutManager getLayout() {
        return this.layoutMgr;
    }

    public void setLayout(LayoutManager mgr) {
        this.layoutMgr = mgr;
        this.invalidateIfValid();
    }

    @Override
    public void doLayout() {
        this.layout();
    }

    @Override
    @Deprecated
    public void layout() {
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr != null) {
            layoutMgr.layoutContainer(this);
        }
    }

    public boolean isValidateRoot() {
        return false;
    }

    @Override
    void invalidateParent() {
        if (!isJavaAwtSmartInvalidate || !this.isValidateRoot()) {
            super.invalidateParent();
        }
    }

    @Override
    public void invalidate() {
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr instanceof LayoutManager2) {
            LayoutManager2 lm = (LayoutManager2)layoutMgr;
            lm.invalidateLayout(this);
        }
        super.invalidate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void validate() {
        boolean updateCur = false;
        Object object = this.getTreeLock();
        synchronized (object) {
            if ((!this.isValid() || descendUnconditionallyWhenValidating) && this.peer != null) {
                ContainerPeer p = null;
                if (this.peer instanceof ContainerPeer) {
                    p = (ContainerPeer)this.peer;
                }
                if (p != null) {
                    p.beginValidate();
                }
                this.validateTree();
                if (p != null) {
                    p.endValidate();
                    if (!descendUnconditionallyWhenValidating) {
                        updateCur = this.isVisible();
                    }
                }
            }
        }
        if (updateCur) {
            this.updateCursorImmediately();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void validateUnconditionally() {
        boolean updateCur = false;
        Object object = this.getTreeLock();
        synchronized (object) {
            descendUnconditionallyWhenValidating = true;
            this.validate();
            if (this.peer instanceof ContainerPeer) {
                updateCur = this.isVisible();
            }
            descendUnconditionallyWhenValidating = false;
        }
        if (updateCur) {
            this.updateCursorImmediately();
        }
    }

    protected void validateTree() {
        this.checkTreeLock();
        if (!this.isValid() || descendUnconditionallyWhenValidating) {
            if (this.peer instanceof ContainerPeer) {
                ((ContainerPeer)this.peer).beginLayout();
            }
            if (!this.isValid()) {
                this.doLayout();
            }
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                if (comp instanceof Container && !(comp instanceof Window) && (!comp.isValid() || descendUnconditionallyWhenValidating)) {
                    ((Container)comp).validateTree();
                    continue;
                }
                comp.validate();
            }
            if (this.peer instanceof ContainerPeer) {
                ((ContainerPeer)this.peer).endLayout();
            }
        }
        super.validate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void invalidateTree() {
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                if (comp instanceof Container) {
                    ((Container)comp).invalidateTree();
                    continue;
                }
                comp.invalidateIfValid();
            }
            this.invalidateIfValid();
        }
    }

    @Override
    public void setFont(Font f) {
        boolean shouldinvalidate = false;
        Font oldfont = this.getFont();
        super.setFont(f);
        Font newfont = this.getFont();
        if (!(newfont == oldfont || oldfont != null && oldfont.equals(newfont))) {
            this.invalidateTree();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public Dimension preferredSize() {
        Dimension dim = this.prefSize;
        if (dim == null || !this.isPreferredSizeSet() && !this.isValid()) {
            Object object = this.getTreeLock();
            synchronized (object) {
                dim = this.prefSize = this.layoutMgr != null ? this.layoutMgr.preferredLayoutSize(this) : super.preferredSize();
            }
        }
        if (dim != null) {
            return new Dimension(dim);
        }
        return dim;
    }

    @Override
    public Dimension getMinimumSize() {
        return this.minimumSize();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public Dimension minimumSize() {
        Dimension dim = this.minSize;
        if (dim == null || !this.isMinimumSizeSet() && !this.isValid()) {
            Object object = this.getTreeLock();
            synchronized (object) {
                dim = this.minSize = this.layoutMgr != null ? this.layoutMgr.minimumLayoutSize(this) : super.minimumSize();
            }
        }
        if (dim != null) {
            return new Dimension(dim);
        }
        return dim;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension getMaximumSize() {
        Dimension dim = this.maxSize;
        if (dim == null || !this.isMaximumSizeSet() && !this.isValid()) {
            Object object = this.getTreeLock();
            synchronized (object) {
                if (this.layoutMgr instanceof LayoutManager2) {
                    LayoutManager2 lm = (LayoutManager2)this.layoutMgr;
                    this.maxSize = lm.maximumLayoutSize(this);
                } else {
                    this.maxSize = super.getMaximumSize();
                }
                dim = this.maxSize;
            }
        }
        if (dim != null) {
            return new Dimension(dim);
        }
        return dim;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float getAlignmentX() {
        float xAlign;
        if (this.layoutMgr instanceof LayoutManager2) {
            Object object = this.getTreeLock();
            synchronized (object) {
                LayoutManager2 lm = (LayoutManager2)this.layoutMgr;
                xAlign = lm.getLayoutAlignmentX(this);
            }
        } else {
            xAlign = super.getAlignmentX();
        }
        return xAlign;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float getAlignmentY() {
        float yAlign;
        if (this.layoutMgr instanceof LayoutManager2) {
            Object object = this.getTreeLock();
            synchronized (object) {
                LayoutManager2 lm = (LayoutManager2)this.layoutMgr;
                yAlign = lm.getLayoutAlignmentY(this);
            }
        } else {
            yAlign = super.getAlignmentY();
        }
        return yAlign;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paint(Graphics g) {
        if (this.isShowing()) {
            Object object = this.getObjectLock();
            synchronized (object) {
                if (this.printing && this.printingThreads.contains(Thread.currentThread())) {
                    return;
                }
            }
            GraphicsCallback.PaintCallback.getInstance().runComponents(this.getComponentsSync(), g, 2);
        }
    }

    @Override
    public void update(Graphics g) {
        if (this.isShowing()) {
            if (!(this.peer instanceof LightweightPeer)) {
                g.clearRect(0, 0, this.width, this.height);
            }
            this.paint(g);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void print(Graphics g) {
        if (this.isShowing()) {
            Object object;
            Thread t = Thread.currentThread();
            try {
                object = this.getObjectLock();
                synchronized (object) {
                    if (this.printingThreads == null) {
                        this.printingThreads = new HashSet<Thread>();
                    }
                    this.printingThreads.add(t);
                    this.printing = true;
                }
                super.print(g);
            }
            catch (Throwable throwable) {
                Object object2 = this.getObjectLock();
                synchronized (object2) {
                    this.printingThreads.remove(t);
                    this.printing = !this.printingThreads.isEmpty();
                }
                throw throwable;
            }
            object = this.getObjectLock();
            synchronized (object) {
                this.printingThreads.remove(t);
                this.printing = !this.printingThreads.isEmpty();
            }
            GraphicsCallback.PrintCallback.getInstance().runComponents(this.getComponentsSync(), g, 2);
        }
    }

    public void paintComponents(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PaintAllCallback.getInstance().runComponents(this.getComponentsSync(), g, 4);
        }
    }

    @Override
    void lightweightPaint(Graphics g) {
        super.lightweightPaint(g);
        this.paintHeavyweightComponents(g);
    }

    @Override
    void paintHeavyweightComponents(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().runComponents(this.getComponentsSync(), g, 3);
        }
    }

    public void printComponents(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PrintAllCallback.getInstance().runComponents(this.getComponentsSync(), g, 4);
        }
    }

    @Override
    void lightweightPrint(Graphics g) {
        super.lightweightPrint(g);
        this.printHeavyweightComponents(g);
    }

    @Override
    void printHeavyweightComponents(Graphics g) {
        if (this.isShowing()) {
            GraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().runComponents(this.getComponentsSync(), g, 3);
        }
    }

    public synchronized void addContainerListener(ContainerListener l) {
        if (l == null) {
            return;
        }
        this.containerListener = AWTEventMulticaster.add(this.containerListener, l);
        this.newEventsOnly = true;
    }

    public synchronized void removeContainerListener(ContainerListener l) {
        if (l == null) {
            return;
        }
        this.containerListener = AWTEventMulticaster.remove(this.containerListener, l);
    }

    public synchronized ContainerListener[] getContainerListeners() {
        return (ContainerListener[])this.getListeners(ContainerListener.class);
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        ContainerListener l = null;
        if (listenerType != ContainerListener.class) {
            return super.getListeners(listenerType);
        }
        l = this.containerListener;
        return AWTEventMulticaster.getListeners((EventListener)l, listenerType);
    }

    @Override
    boolean eventEnabled(AWTEvent e) {
        int id = e.getID();
        if (id == 300 || id == 301) {
            return (this.eventMask & 2L) != 0L || this.containerListener != null;
        }
        return super.eventEnabled(e);
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e instanceof ContainerEvent) {
            this.processContainerEvent((ContainerEvent)e);
            return;
        }
        super.processEvent(e);
    }

    protected void processContainerEvent(ContainerEvent e) {
        ContainerListener listener = this.containerListener;
        if (listener != null) {
            switch (e.getID()) {
                case 300: {
                    listener.componentAdded(e);
                    break;
                }
                case 301: {
                    listener.componentRemoved(e);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void dispatchEventImpl(AWTEvent e) {
        if (this.dispatcher != null && this.dispatcher.dispatchEvent(e)) {
            e.consume();
            if (this.peer != null) {
                this.peer.handleEvent(e);
            }
            return;
        }
        super.dispatchEventImpl(e);
        Object object = this.getTreeLock();
        synchronized (object) {
            switch (e.getID()) {
                case 101: {
                    this.createChildHierarchyEvents(1402, 0L, Toolkit.enabledOnToolkit(65536L));
                    break;
                }
                case 100: {
                    this.createChildHierarchyEvents(1401, 0L, Toolkit.enabledOnToolkit(65536L));
                    break;
                }
            }
        }
    }

    void dispatchEventToSelf(AWTEvent e) {
        super.dispatchEventImpl(e);
    }

    Component getMouseEventTarget(int x, int y, boolean includeSelf) {
        return this.getMouseEventTarget(x, y, includeSelf, MouseEventTargetFilter.FILTER, false);
    }

    Component getDropTargetEventTarget(int x, int y, boolean includeSelf) {
        return this.getMouseEventTarget(x, y, includeSelf, DropTargetEventTargetFilter.FILTER, true);
    }

    private Component getMouseEventTarget(int x, int y, boolean includeSelf, EventTargetFilter filter, boolean searchHeavyweights) {
        Component comp = null;
        if (searchHeavyweights) {
            comp = this.getMouseEventTargetImpl(x, y, includeSelf, filter, true, searchHeavyweights);
        }
        if (comp == null || comp == this) {
            comp = this.getMouseEventTargetImpl(x, y, includeSelf, filter, false, searchHeavyweights);
        }
        return comp;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Component getMouseEventTargetImpl(int x, int y, boolean includeSelf, EventTargetFilter filter, boolean searchHeavyweightChildren, boolean searchHeavyweightDescendants) {
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                if (comp == null || !comp.visible || (searchHeavyweightChildren || !(comp.peer instanceof LightweightPeer)) && (!searchHeavyweightChildren || comp.peer instanceof LightweightPeer) || !comp.contains(x - comp.x, y - comp.y)) continue;
                if (comp instanceof Container) {
                    Container child = (Container)comp;
                    Component deeper = child.getMouseEventTarget(x - child.x, y - child.y, includeSelf, filter, searchHeavyweightDescendants);
                    if (deeper == null) continue;
                    return deeper;
                }
                if (!filter.accept(comp)) continue;
                return comp;
            }
            boolean isPeerOK = this.peer instanceof LightweightPeer || includeSelf;
            boolean isMouseOverMe = this.contains(x, y);
            if (isMouseOverMe && isPeerOK && filter.accept(this)) {
                return this;
            }
            return null;
        }
    }

    void proxyEnableEvents(long events) {
        if (this.peer instanceof LightweightPeer) {
            if (this.parent != null) {
                this.parent.proxyEnableEvents(events);
            }
        } else if (this.dispatcher != null) {
            this.dispatcher.enableEvents(events);
        }
    }

    @Override
    @Deprecated
    public void deliverEvent(Event e) {
        Component comp = this.getComponentAt(e.x, e.y);
        if (comp != null && comp != this) {
            e.translate(-comp.x, -comp.y);
            comp.deliverEvent(e);
        } else {
            this.postEvent(e);
        }
    }

    @Override
    public Component getComponentAt(int x, int y) {
        return this.locate(x, y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    @Deprecated
    public Component locate(int x, int y) {
        if (!this.contains(x, y)) {
            return null;
        }
        Component lightweight = null;
        Object object = this.getTreeLock();
        synchronized (object) {
            for (Component comp : this.component) {
                if (!comp.contains(x - comp.x, y - comp.y)) continue;
                if (!comp.isLightweight()) {
                    return comp;
                }
                if (lightweight != null) continue;
                lightweight = comp;
            }
        }
        return lightweight != null ? lightweight : this;
    }

    @Override
    public Component getComponentAt(Point p) {
        return this.getComponentAt(p.x, p.y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Point getMousePosition(boolean allowChildren) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        PointerInfo pi = AccessController.doPrivileged(new PrivilegedAction<PointerInfo>(this){

            @Override
            public PointerInfo run() {
                return MouseInfo.getPointerInfo();
            }
        });
        Object object = this.getTreeLock();
        synchronized (object) {
            Component inTheSameWindow = this.findUnderMouseInWindow(pi);
            if (this.isSameOrAncestorOf(inTheSameWindow, allowChildren)) {
                return this.pointRelativeToComponent(pi.getLocation());
            }
            return null;
        }
    }

    @Override
    boolean isSameOrAncestorOf(Component comp, boolean allowChildren) {
        return this == comp || allowChildren && this.isParentOf(comp);
    }

    public Component findComponentAt(int x, int y) {
        return this.findComponentAt(x, y, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final Component findComponentAt(int x, int y, boolean ignoreEnabled) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.isRecursivelyVisible()) {
                return this.findComponentAtImpl(x, y, ignoreEnabled);
            }
        }
        return null;
    }

    final Component findComponentAtImpl(int x, int y, boolean ignoreEnabled) {
        if (!this.contains(x, y) || !this.visible || !ignoreEnabled && !this.enabled) {
            return null;
        }
        Component lightweight = null;
        for (Component comp : this.component) {
            int y1;
            int x1;
            if (!comp.contains(x1 = x - comp.x, y1 = y - comp.y)) continue;
            if (!comp.isLightweight()) {
                Component child = Container.getChildAt(comp, x1, y1, ignoreEnabled);
                if (child == null) continue;
                return child;
            }
            if (lightweight != null) continue;
            lightweight = Container.getChildAt(comp, x1, y1, ignoreEnabled);
        }
        return lightweight != null ? lightweight : this;
    }

    private static Component getChildAt(Component comp, int x, int y, boolean ignoreEnabled) {
        if ((comp = comp instanceof Container ? ((Container)comp).findComponentAtImpl(x, y, ignoreEnabled) : comp.getComponentAt(x, y)) != null && comp.visible && (ignoreEnabled || comp.enabled)) {
            return comp;
        }
        return null;
    }

    public Component findComponentAt(Point p) {
        return this.findComponentAt(p.x, p.y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            super.addNotify();
            if (!(this.peer instanceof LightweightPeer)) {
                this.dispatcher = new LightweightDispatcher(this);
            }
            for (int i = 0; i < this.component.size(); ++i) {
                this.component.get(i).addNotify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeNotify() {
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = this.component.size() - 1; i >= 0; --i) {
                Component comp = this.component.get(i);
                if (comp == null) continue;
                comp.setAutoFocusTransferOnDisposal(false);
                comp.removeNotify();
                comp.setAutoFocusTransferOnDisposal(true);
            }
            if (this.containsFocus() && KeyboardFocusManager.isAutoFocusTransferEnabledFor(this) && !this.transferFocus(false)) {
                this.transferFocusBackward(true);
            }
            if (this.dispatcher != null) {
                this.dispatcher.dispose();
                this.dispatcher = null;
            }
            super.removeNotify();
        }
    }

    public boolean isAncestorOf(Component c) {
        Container p;
        if (c == null || (p = c.getParent()) == null) {
            return false;
        }
        while (p != null) {
            if (p == this) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void startLWModal() {
        Container nativeContainer;
        Component predictedFocusOwner;
        this.modalAppContext = AppContext.getAppContext();
        long time = Toolkit.getEventQueue().getMostRecentKeyEventTime();
        Component component = predictedFocusOwner = Component.isInstanceOf(this, "javax.swing.JInternalFrame") ? ((JInternalFrame)this).getMostRecentFocusOwner() : null;
        if (predictedFocusOwner != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().enqueueKeyEvents(time, predictedFocusOwner);
        }
        Object object = this.getTreeLock();
        synchronized (object) {
            nativeContainer = this.getHeavyweightContainer();
            if (nativeContainer.modalComp != null) {
                this.modalComp = nativeContainer.modalComp;
                nativeContainer.modalComp = this;
                return;
            }
            nativeContainer.modalComp = this;
        }
        Runnable pumpEventsForHierarchy = () -> {
            EventDispatchThread dispatchThread = (EventDispatchThread)Thread.currentThread();
            dispatchThread.pumpEventsForHierarchy(() -> nativeContainer.modalComp != null, this);
        };
        if (EventQueue.isDispatchThread()) {
            SequencedEvent currentSequencedEvent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getCurrentSequencedEvent();
            if (currentSequencedEvent != null) {
                currentSequencedEvent.dispose();
            }
            pumpEventsForHierarchy.run();
        } else {
            Object object2 = this.getTreeLock();
            synchronized (object2) {
                Toolkit.getEventQueue().postEvent(new PeerEvent(this, pumpEventsForHierarchy, 1L));
                while (nativeContainer.modalComp != null) {
                    try {
                        this.getTreeLock().wait();
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                        break;
                    }
                }
            }
        }
        if (predictedFocusOwner != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().dequeueKeyEvents(time, predictedFocusOwner);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void stopLWModal() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this.modalAppContext != null) {
                Container nativeContainer = this.getHeavyweightContainer();
                if (nativeContainer != null) {
                    if (this.modalComp != null) {
                        nativeContainer.modalComp = this.modalComp;
                        this.modalComp = null;
                        return;
                    }
                    nativeContainer.modalComp = null;
                }
                SunToolkit.postEvent(this.modalAppContext, new PeerEvent(this, new WakingRunnable(), 1L));
            }
            EventQueue.invokeLater(new WakingRunnable());
            this.getTreeLock().notifyAll();
        }
    }

    @Override
    protected String paramString() {
        Object str = super.paramString();
        LayoutManager layoutMgr = this.layoutMgr;
        if (layoutMgr != null) {
            str = (String)str + ",layout=" + layoutMgr.getClass().getName();
        }
        return str;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void list(PrintStream out, int indent) {
        super.list(out, indent);
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                if (comp == null) continue;
                comp.list(out, indent + 1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void list(PrintWriter out, int indent) {
        super.list(out, indent);
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                if (comp == null) continue;
                comp.list(out, indent + 1);
            }
        }
    }

    @Override
    public void setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        this.setFocusTraversalKeys_NoIDCheck(id, keystrokes);
    }

    @Override
    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return this.getFocusTraversalKeys_NoIDCheck(id);
    }

    @Override
    public boolean areFocusTraversalKeysSet(int id) {
        if (id < 0 || id >= 4) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        return this.focusTraversalKeys != null && this.focusTraversalKeys[id] != null;
    }

    @Override
    public boolean isFocusCycleRoot(Container container) {
        if (this.isFocusCycleRoot() && container == this) {
            return true;
        }
        return super.isFocusCycleRoot(container);
    }

    private Container findTraversalRoot() {
        Container root;
        Container currentFocusCycleRoot = KeyboardFocusManager.getCurrentKeyboardFocusManager().getCurrentFocusCycleRoot();
        if (currentFocusCycleRoot == this) {
            root = this;
        } else {
            root = this.getFocusCycleRootAncestor();
            if (root == null) {
                root = this;
            }
        }
        if (root != currentFocusCycleRoot) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalCurrentFocusCycleRootPriv(root);
        }
        return root;
    }

    @Override
    final boolean containsFocus() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return this.isParentOf(focusOwner);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean isParentOf(Component comp) {
        Object object = this.getTreeLock();
        synchronized (object) {
            while (comp != null && comp != this && !(comp instanceof Window)) {
                comp = comp.getParent();
            }
            return comp == this;
        }
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
    void clearMostRecentFocusOwnerOnHide() {
        boolean reset = false;
        Window window = null;
        Object object = this.getTreeLock();
        // MONITORENTER : object
        window = this.getContainingWindow();
        if (window != null) {
            Component comp = KeyboardFocusManager.getMostRecentFocusOwner(window);
            reset = comp == this || this.isParentOf(comp);
            Class<KeyboardFocusManager> clazz = KeyboardFocusManager.class;
            // MONITORENTER : java.awt.KeyboardFocusManager.class
            Component storedComp = window.getTemporaryLostComponent();
            if (this.isParentOf(storedComp) || storedComp == this) {
                window.setTemporaryLostComponent(null);
            }
            // MONITOREXIT : clazz
        }
        // MONITOREXIT : object
        if (!reset) return;
        KeyboardFocusManager.setMostRecentFocusOwner(window, null);
    }

    @Override
    void clearCurrentFocusCycleRootOnHide() {
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Container cont = kfm.getCurrentFocusCycleRoot();
        if (cont == this || this.isParentOf(cont)) {
            kfm.setGlobalCurrentFocusCycleRootPriv(null);
        }
    }

    @Override
    final Container getTraversalRoot() {
        if (this.isFocusCycleRoot()) {
            return this.findTraversalRoot();
        }
        return super.getTraversalRoot();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFocusTraversalPolicy(FocusTraversalPolicy policy) {
        FocusTraversalPolicy oldPolicy;
        Container container = this;
        synchronized (container) {
            oldPolicy = this.focusTraversalPolicy;
            this.focusTraversalPolicy = policy;
        }
        this.firePropertyChange("focusTraversalPolicy", oldPolicy, policy);
    }

    public FocusTraversalPolicy getFocusTraversalPolicy() {
        if (!this.isFocusTraversalPolicyProvider() && !this.isFocusCycleRoot()) {
            return null;
        }
        FocusTraversalPolicy policy = this.focusTraversalPolicy;
        if (policy != null) {
            return policy;
        }
        Container rootAncestor = this.getFocusCycleRootAncestor();
        if (rootAncestor != null) {
            return rootAncestor.getFocusTraversalPolicy();
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
    }

    public boolean isFocusTraversalPolicySet() {
        return this.focusTraversalPolicy != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setFocusCycleRoot(boolean focusCycleRoot) {
        boolean oldFocusCycleRoot;
        Container container = this;
        synchronized (container) {
            oldFocusCycleRoot = this.focusCycleRoot;
            this.focusCycleRoot = focusCycleRoot;
        }
        this.firePropertyChange("focusCycleRoot", oldFocusCycleRoot, focusCycleRoot);
    }

    public boolean isFocusCycleRoot() {
        return this.focusCycleRoot;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setFocusTraversalPolicyProvider(boolean provider) {
        boolean oldProvider;
        Container container = this;
        synchronized (container) {
            oldProvider = this.focusTraversalPolicyProvider;
            this.focusTraversalPolicyProvider = provider;
        }
        this.firePropertyChange("focusTraversalPolicyProvider", oldProvider, provider);
    }

    public final boolean isFocusTraversalPolicyProvider() {
        return this.focusTraversalPolicyProvider;
    }

    public void transferFocusDownCycle() {
        if (this.isFocusCycleRoot()) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalCurrentFocusCycleRootPriv(this);
            Component toFocus = this.getFocusTraversalPolicy().getDefaultComponent(this);
            if (toFocus != null) {
                toFocus.requestFocus(FocusEvent.Cause.TRAVERSAL_DOWN);
            }
        }
    }

    void preProcessKeyEvent(KeyEvent e) {
        Container parent = this.parent;
        if (parent != null) {
            parent.preProcessKeyEvent(e);
        }
    }

    void postProcessKeyEvent(KeyEvent e) {
        Container parent = this.parent;
        if (parent != null) {
            parent.postProcessKeyEvent(e);
        }
    }

    @Override
    boolean postsOldMouseEvents() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        Object object = this.getTreeLock();
        synchronized (object) {
            for (int i = 0; i < this.component.size(); ++i) {
                Component comp = this.component.get(i);
                comp.applyComponentOrientation(o);
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField f = s.putFields();
        f.put("ncomponents", this.component.size());
        f.put("component", this.component.toArray(EMPTY_ARRAY));
        f.put("layoutMgr", this.layoutMgr);
        f.put("dispatcher", this.dispatcher);
        f.put("maxSize", this.maxSize);
        f.put("focusCycleRoot", this.focusCycleRoot);
        f.put("containerSerializedDataVersion", this.containerSerializedDataVersion);
        f.put("focusTraversalPolicyProvider", this.focusTraversalPolicyProvider);
        s.writeFields();
        AWTEventMulticaster.save(s, "containerL", this.containerListener);
        s.writeObject(null);
        if (this.focusTraversalPolicy instanceof Serializable) {
            s.writeObject(this.focusTraversalPolicy);
        } else {
            s.writeObject(null);
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        block10: {
            Object keyOrNull;
            int ncomponents;
            ObjectInputStream.GetField f = s.readFields();
            Component[] tmpComponent = (Component[])f.get("component", null);
            if (tmpComponent == null) {
                tmpComponent = EMPTY_ARRAY;
            }
            if ((ncomponents = Integer.valueOf(f.get("ncomponents", 0)).intValue()) < 0 || ncomponents > tmpComponent.length) {
                throw new InvalidObjectException("Incorrect number of components");
            }
            this.component = new ArrayList<Component>(ncomponents);
            for (int i = 0; i < ncomponents; ++i) {
                this.component.add(tmpComponent[i]);
            }
            this.layoutMgr = (LayoutManager)f.get("layoutMgr", null);
            this.dispatcher = (LightweightDispatcher)f.get("dispatcher", null);
            if (this.maxSize == null) {
                this.maxSize = (Dimension)f.get("maxSize", null);
            }
            this.focusCycleRoot = f.get("focusCycleRoot", false);
            this.containerSerializedDataVersion = f.get("containerSerializedDataVersion", 1);
            this.focusTraversalPolicyProvider = f.get("focusTraversalPolicyProvider", false);
            List<Component> component = this.component;
            for (Component comp : component) {
                comp.parent = this;
                this.adjustListeningChildren(32768L, comp.numListening(32768L));
                this.adjustListeningChildren(65536L, comp.numListening(65536L));
                this.adjustDescendants(comp.countHierarchyMembers());
            }
            while (null != (keyOrNull = s.readObject())) {
                String key = ((String)keyOrNull).intern();
                if ("containerL" == key) {
                    this.addContainerListener((ContainerListener)s.readObject());
                    continue;
                }
                s.readObject();
            }
            try {
                Object policy = s.readObject();
                if (policy instanceof FocusTraversalPolicy) {
                    this.focusTraversalPolicy = (FocusTraversalPolicy)policy;
                }
            }
            catch (OptionalDataException e) {
                if (e.eof) break block10;
                throw e;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Accessible getAccessibleAt(Point p) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (this instanceof Accessible) {
                Accessible a = (Accessible)((Object)this);
                AccessibleContext ac = a.getAccessibleContext();
                if (ac != null) {
                    int nchildren = ac.getAccessibleChildrenCount();
                    for (int i = 0; i < nchildren; ++i) {
                        AccessibleComponent acmp;
                        a = ac.getAccessibleChild(i);
                        if (a == null || (ac = a.getAccessibleContext()) == null || (acmp = ac.getAccessibleComponent()) == null || !acmp.isShowing()) continue;
                        Point location = acmp.getLocation();
                        Point np = new Point(p.x - location.x, p.y - location.y);
                        if (!acmp.contains(np)) continue;
                        return a;
                    }
                }
                return (Accessible)((Object)this);
            }
            Component ret = this;
            if (!this.contains(p.x, p.y)) {
                ret = null;
            } else {
                int ncomponents = this.getComponentCount();
                for (int i = 0; i < ncomponents; ++i) {
                    Component comp = this.getComponent(i);
                    if (comp == null || !comp.isShowing()) continue;
                    Point location = comp.getLocation();
                    if (!comp.contains(p.x - location.x, p.y - location.y)) continue;
                    ret = comp;
                }
            }
            if (ret instanceof Accessible) {
                return (Accessible)((Object)ret);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int getAccessibleChildrenCount() {
        Object object = this.getTreeLock();
        synchronized (object) {
            int count = 0;
            Component[] children = this.getComponents();
            for (int i = 0; i < children.length; ++i) {
                if (!(children[i] instanceof Accessible)) continue;
                ++count;
            }
            return count;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Accessible getAccessibleChild(int i) {
        Object object = this.getTreeLock();
        synchronized (object) {
            Component[] children = this.getComponents();
            int count = 0;
            for (int j = 0; j < children.length; ++j) {
                if (!(children[j] instanceof Accessible)) continue;
                if (count == i) {
                    return (Accessible)((Object)children[j]);
                }
                ++count;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void increaseComponentCount(Component c) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (!c.isDisplayable()) {
                throw new IllegalStateException("Peer does not exist while invoking the increaseComponentCount() method");
            }
            int addHW = 0;
            int addLW = 0;
            if (c instanceof Container) {
                addLW = ((Container)c).numOfLWComponents;
                addHW = ((Container)c).numOfHWComponents;
            }
            if (c.isLightweight()) {
                ++addLW;
            } else {
                ++addHW;
            }
            for (Container cont = this; cont != null; cont = cont.getContainer()) {
                cont.numOfLWComponents += addLW;
                cont.numOfHWComponents += addHW;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void decreaseComponentCount(Component c) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (!c.isDisplayable()) {
                throw new IllegalStateException("Peer does not exist while invoking the decreaseComponentCount() method");
            }
            int subHW = 0;
            int subLW = 0;
            if (c instanceof Container) {
                subLW = ((Container)c).numOfLWComponents;
                subHW = ((Container)c).numOfHWComponents;
            }
            if (c.isLightweight()) {
                ++subLW;
            } else {
                ++subHW;
            }
            for (Container cont = this; cont != null; cont = cont.getContainer()) {
                cont.numOfLWComponents -= subLW;
                cont.numOfHWComponents -= subHW;
            }
        }
    }

    private int getTopmostComponentIndex() {
        this.checkTreeLock();
        if (this.getComponentCount() > 0) {
            return 0;
        }
        return -1;
    }

    private int getBottommostComponentIndex() {
        this.checkTreeLock();
        if (this.getComponentCount() > 0) {
            return this.getComponentCount() - 1;
        }
        return -1;
    }

    @Override
    final Region getOpaqueShape() {
        this.checkTreeLock();
        if (this.isLightweight() && this.isNonOpaqueForMixing() && this.hasLightweightDescendants()) {
            Region s = Region.EMPTY_REGION;
            for (int index = 0; index < this.getComponentCount(); ++index) {
                Component c = this.getComponent(index);
                if (!c.isLightweight() || !c.isShowing()) continue;
                s = s.getUnion(c.getOpaqueShape());
            }
            return s.getIntersection(this.getNormalShape());
        }
        return super.getOpaqueShape();
    }

    final void recursiveSubtractAndApplyShape(Region shape) {
        this.recursiveSubtractAndApplyShape(shape, this.getTopmostComponentIndex(), this.getBottommostComponentIndex());
    }

    final void recursiveSubtractAndApplyShape(Region shape, int fromZorder) {
        this.recursiveSubtractAndApplyShape(shape, fromZorder, this.getBottommostComponentIndex());
    }

    final void recursiveSubtractAndApplyShape(Region shape, int fromZorder, int toZorder) {
        this.checkTreeLock();
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + String.valueOf(this) + "; shape=" + String.valueOf(shape) + "; fromZ=" + fromZorder + "; toZ=" + toZorder);
        }
        if (fromZorder == -1) {
            return;
        }
        if (shape.isEmpty()) {
            return;
        }
        if (this.getLayout() != null && !this.isValid()) {
            return;
        }
        for (int index = fromZorder; index <= toZorder; ++index) {
            Component comp = this.getComponent(index);
            if (!comp.isLightweight()) {
                comp.subtractAndApplyShape(shape);
                continue;
            }
            if (!(comp instanceof Container) || !((Container)comp).hasHeavyweightDescendants() || !comp.isShowing()) continue;
            ((Container)comp).recursiveSubtractAndApplyShape(shape);
        }
    }

    final void recursiveApplyCurrentShape() {
        this.recursiveApplyCurrentShape(this.getTopmostComponentIndex(), this.getBottommostComponentIndex());
    }

    final void recursiveApplyCurrentShape(int fromZorder) {
        this.recursiveApplyCurrentShape(fromZorder, this.getBottommostComponentIndex());
    }

    final void recursiveApplyCurrentShape(int fromZorder, int toZorder) {
        this.checkTreeLock();
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + String.valueOf(this) + "; fromZ=" + fromZorder + "; toZ=" + toZorder);
        }
        if (fromZorder == -1) {
            return;
        }
        if (this.getLayout() != null && !this.isValid()) {
            return;
        }
        for (int index = fromZorder; index <= toZorder; ++index) {
            Component comp = this.getComponent(index);
            if (!comp.isLightweight()) {
                comp.applyCurrentShape();
            }
            if (!(comp instanceof Container) || !((Container)comp).hasHeavyweightDescendants()) continue;
            ((Container)comp).recursiveApplyCurrentShape();
        }
    }

    private void recursiveShowHeavyweightChildren() {
        if (!this.hasHeavyweightDescendants() || !this.isVisible()) {
            return;
        }
        for (int index = 0; index < this.getComponentCount(); ++index) {
            ComponentPeer peer;
            Component comp = this.getComponent(index);
            if (comp.isLightweight()) {
                if (!(comp instanceof Container)) continue;
                ((Container)comp).recursiveShowHeavyweightChildren();
                continue;
            }
            if (!comp.isVisible() || (peer = comp.peer) == null) continue;
            peer.setVisible(true);
        }
    }

    private void recursiveHideHeavyweightChildren() {
        if (!this.hasHeavyweightDescendants()) {
            return;
        }
        for (int index = 0; index < this.getComponentCount(); ++index) {
            ComponentPeer peer;
            Component comp = this.getComponent(index);
            if (comp.isLightweight()) {
                if (!(comp instanceof Container)) continue;
                ((Container)comp).recursiveHideHeavyweightChildren();
                continue;
            }
            if (!comp.isVisible() || (peer = comp.peer) == null) continue;
            peer.setVisible(false);
        }
    }

    private void recursiveRelocateHeavyweightChildren(Point origin) {
        for (int index = 0; index < this.getComponentCount(); ++index) {
            Component comp = this.getComponent(index);
            if (comp.isLightweight()) {
                if (!(comp instanceof Container) || !((Container)comp).hasHeavyweightDescendants()) continue;
                Point newOrigin = new Point(origin);
                newOrigin.translate(comp.getX(), comp.getY());
                ((Container)comp).recursiveRelocateHeavyweightChildren(newOrigin);
                continue;
            }
            ComponentPeer peer = comp.peer;
            if (peer == null) continue;
            peer.setBounds(origin.x + comp.getX(), origin.y + comp.getY(), comp.getWidth(), comp.getHeight(), 1);
        }
    }

    final boolean isRecursivelyVisibleUpToHeavyweightContainer() {
        if (!this.isLightweight()) {
            return true;
        }
        for (Container cont = this; cont != null && cont.isLightweight(); cont = cont.getContainer()) {
            if (cont.isVisible()) continue;
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void mixOnShowing() {
        Object object = this.getTreeLock();
        synchronized (object) {
            boolean isLightweight;
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this));
            }
            if ((isLightweight = this.isLightweight()) && this.isRecursivelyVisibleUpToHeavyweightContainer()) {
                this.recursiveShowHeavyweightChildren();
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (!isLightweight || isLightweight && this.hasHeavyweightDescendants()) {
                this.recursiveApplyCurrentShape();
            }
            super.mixOnShowing();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void mixOnHiding(boolean isLightweight) {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; isLightweight=" + isLightweight);
            }
            if (isLightweight) {
                this.recursiveHideHeavyweightChildren();
            }
            super.mixOnHiding(isLightweight);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void mixOnReshaping() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this));
            }
            boolean isMixingNeeded = this.isMixingNeeded();
            if (this.isLightweight() && this.hasHeavyweightDescendants()) {
                Point origin = new Point(this.getX(), this.getY());
                for (Container cont = this.getContainer(); cont != null && cont.isLightweight(); cont = cont.getContainer()) {
                    origin.translate(cont.getX(), cont.getY());
                }
                this.recursiveRelocateHeavyweightChildren(origin);
                if (!isMixingNeeded) {
                    return;
                }
                this.recursiveApplyCurrentShape();
            }
            if (!isMixingNeeded) {
                return;
            }
            super.mixOnReshaping();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void mixOnZOrderChanging(int oldZorder, int newZorder) {
        Object object = this.getTreeLock();
        synchronized (object) {
            boolean becameHigher;
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this) + "; oldZ=" + oldZorder + "; newZ=" + newZorder);
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            boolean bl = becameHigher = newZorder < oldZorder;
            if (becameHigher && this.isLightweight() && this.hasHeavyweightDescendants()) {
                this.recursiveApplyCurrentShape();
            }
            super.mixOnZOrderChanging(oldZorder, newZorder);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void mixOnValidating() {
        Object object = this.getTreeLock();
        synchronized (object) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + String.valueOf(this));
            }
            if (!this.isMixingNeeded()) {
                return;
            }
            if (this.hasHeavyweightDescendants()) {
                this.recursiveApplyCurrentShape();
            }
            if (this.isLightweight() && this.isNonOpaqueForMixing()) {
                this.subtractAndApplyShapeBelowMe();
            }
            super.mixOnValidating();
        }
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Container.initIDs();
        }
        AWTAccessor.setContainerAccessor(new AWTAccessor.ContainerAccessor(){

            @Override
            public void validateUnconditionally(Container cont) {
                cont.validateUnconditionally();
            }

            @Override
            public Component findComponentAt(Container cont, int x, int y, boolean ignoreEnabled) {
                return cont.findComponentAt(x, y, ignoreEnabled);
            }

            @Override
            public void startLWModal(Container cont) {
                cont.startLWModal();
            }

            @Override
            public void stopLWModal(Container cont) {
                cont.stopLWModal();
            }
        });
        isJavaAwtSmartInvalidate = AccessController.doPrivileged(new GetBooleanAction("java.awt.smartInvalidate"));
        descendUnconditionallyWhenValidating = false;
    }

    static class MouseEventTargetFilter
    implements EventTargetFilter {
        static final EventTargetFilter FILTER = new MouseEventTargetFilter();

        private MouseEventTargetFilter() {
        }

        @Override
        public boolean accept(Component comp) {
            return (comp.eventMask & 0x20L) != 0L || (comp.eventMask & 0x10L) != 0L || (comp.eventMask & 0x20000L) != 0L || comp.mouseListener != null || comp.mouseMotionListener != null || comp.mouseWheelListener != null;
        }
    }

    static interface EventTargetFilter {
        public boolean accept(Component var1);
    }

    static class DropTargetEventTargetFilter
    implements EventTargetFilter {
        static final EventTargetFilter FILTER = new DropTargetEventTargetFilter();

        private DropTargetEventTargetFilter() {
        }

        @Override
        public boolean accept(Component comp) {
            DropTarget dt = comp.getDropTarget();
            return dt != null && dt.isActive();
        }
    }

    static final class WakingRunnable
    implements Runnable {
        WakingRunnable() {
        }

        @Override
        public void run() {
        }
    }

    protected class AccessibleAWTContainer
    extends Component.AccessibleAWTComponent {
        private static final long serialVersionUID = 5081320404842566097L;
        private volatile transient int propertyListenersCount;
        protected ContainerListener accessibleContainerHandler = null;

        protected AccessibleAWTContainer() {
        }

        @Override
        public int getAccessibleChildrenCount() {
            return Container.this.getAccessibleChildrenCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return Container.this.getAccessibleChild(i);
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            return Container.this.getAccessibleAt(p);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (this.accessibleContainerHandler == null) {
                this.accessibleContainerHandler = new AccessibleContainerHandler();
            }
            if (this.propertyListenersCount++ == 0) {
                Container.this.addContainerListener(this.accessibleContainerHandler);
            }
            super.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (--this.propertyListenersCount == 0) {
                Container.this.removeContainerListener(this.accessibleContainerHandler);
            }
            super.removePropertyChangeListener(listener);
        }

        protected class AccessibleContainerHandler
        implements ContainerListener,
        Serializable {
            private static final long serialVersionUID = -480855353991814677L;

            protected AccessibleContainerHandler() {
            }

            @Override
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
                if (c instanceof Accessible) {
                    Accessible accessible = (Accessible)((Object)c);
                    AccessibleAWTContainer.this.firePropertyChange("AccessibleChild", null, accessible.getAccessibleContext());
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                Component c = e.getChild();
                if (c instanceof Accessible) {
                    Accessible accessible = (Accessible)((Object)c);
                    AccessibleAWTContainer.this.firePropertyChange("AccessibleChild", accessible.getAccessibleContext(), null);
                }
            }
        }
    }
}

