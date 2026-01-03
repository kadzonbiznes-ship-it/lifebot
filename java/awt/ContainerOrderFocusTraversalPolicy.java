/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sun.util.logging.PlatformLogger;

public class ContainerOrderFocusTraversalPolicy
extends FocusTraversalPolicy
implements Serializable {
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.ContainerOrderFocusTraversalPolicy");
    private final int FORWARD_TRAVERSAL = 0;
    private final int BACKWARD_TRAVERSAL = 1;
    private static final long serialVersionUID = 486933713763926351L;
    private boolean implicitDownCycleTraversal = true;
    private transient Container cachedRoot;
    private transient List<Component> cachedCycle;

    private List<Component> getFocusTraversalCycle(Container aContainer) {
        ArrayList<Component> cycle = new ArrayList<Component>();
        this.enumerateCycle(aContainer, cycle);
        return cycle;
    }

    private int getComponentIndex(List<Component> cycle, Component aComponent) {
        return cycle.indexOf(aComponent);
    }

    private void enumerateCycle(Container container, List<Component> cycle) {
        if (!container.isVisible() || !container.isDisplayable()) {
            return;
        }
        cycle.add(container);
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; ++i) {
            Container cont;
            Component comp = components[i];
            if (comp instanceof Container && !(cont = (Container)comp).isFocusCycleRoot() && !cont.isFocusTraversalPolicyProvider()) {
                this.enumerateCycle(cont, cycle);
                continue;
            }
            cycle.add(comp);
        }
    }

    private Container getTopmostProvider(Container focusCycleRoot, Component aComponent) {
        Container aCont;
        Container ftp = null;
        for (aCont = aComponent.getParent(); aCont != focusCycleRoot && aCont != null; aCont = aCont.getParent()) {
            if (!aCont.isFocusTraversalPolicyProvider()) continue;
            ftp = aCont;
        }
        if (aCont == null) {
            return null;
        }
        return ftp;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private Component getComponentDownCycle(Component comp, int traversalDirection) {
        Component retComp = null;
        if (!(comp instanceof Container)) return retComp;
        Container cont = (Container)comp;
        if (cont.isFocusCycleRoot()) {
            if (!this.getImplicitDownCycleTraversal()) return null;
            retComp = cont.getFocusTraversalPolicy().getDefaultComponent(cont);
            if (retComp == null || !log.isLoggable(PlatformLogger.Level.FINE)) return retComp;
            log.fine("### Transferred focus down-cycle to " + String.valueOf(retComp) + " in the focus cycle root " + String.valueOf(cont));
            return retComp;
        } else {
            if (!cont.isFocusTraversalPolicyProvider()) return retComp;
            Component component = retComp = traversalDirection == 0 ? cont.getFocusTraversalPolicy().getDefaultComponent(cont) : cont.getFocusTraversalPolicy().getLastComponent(cont);
            if (retComp == null || !log.isLoggable(PlatformLogger.Level.FINE)) return retComp;
            log.fine("### Transferred focus to " + String.valueOf(retComp) + " in the FTP provider " + String.valueOf(cont));
        }
        return retComp;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### Searching in " + String.valueOf(aContainer) + " for component after " + String.valueOf(aComponent));
        }
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if (!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()) {
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");
        }
        if (aContainer.isFocusCycleRoot() && !aComponent.isFocusCycleRoot(aContainer)) {
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }
        Object object = aContainer.getTreeLock();
        synchronized (object) {
            int index;
            if (!aContainer.isVisible() || !aContainer.isDisplayable()) {
                return null;
            }
            Component comp = this.getComponentDownCycle(aComponent, 0);
            if (comp != null && comp != aComponent) {
                return comp;
            }
            Container provider = this.getTopmostProvider(aContainer, aComponent);
            if (provider != null) {
                FocusTraversalPolicy policy;
                Component afterComp;
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Asking FTP " + String.valueOf(provider) + " for component after " + String.valueOf(aComponent));
                }
                if ((afterComp = (policy = provider.getFocusTraversalPolicy()).getComponentAfter(provider, aComponent)) != null) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("### FTP returned " + String.valueOf(afterComp));
                    }
                    return afterComp;
                }
                aComponent = provider;
            }
            List<Component> cycle = this.getFocusTraversalCycle(aContainer);
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### Cycle is " + String.valueOf(cycle) + ", component is " + String.valueOf(aComponent));
            }
            if ((index = this.getComponentIndex(cycle, aComponent)) < 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Didn't find component " + String.valueOf(aComponent) + " in a cycle " + String.valueOf(aContainer));
                }
                return this.getFirstComponent(aContainer);
            }
            ++index;
            while (index < cycle.size()) {
                comp = cycle.get(index);
                if (this.accept(comp)) {
                    return comp;
                }
                if ((comp = this.getComponentDownCycle(comp, 0)) != null) {
                    return comp;
                }
                ++index;
            }
            if (aContainer.isFocusCycleRoot()) {
                this.cachedRoot = aContainer;
                this.cachedCycle = cycle;
                comp = this.getFirstComponent(aContainer);
                this.cachedRoot = null;
                this.cachedCycle = null;
                return comp;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if (!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()) {
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");
        }
        if (aContainer.isFocusCycleRoot() && !aComponent.isFocusCycleRoot(aContainer)) {
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }
        Object object = aContainer.getTreeLock();
        synchronized (object) {
            int index;
            if (!aContainer.isVisible() || !aContainer.isDisplayable()) {
                return null;
            }
            Container provider = this.getTopmostProvider(aContainer, aComponent);
            if (provider != null) {
                FocusTraversalPolicy policy;
                Component beforeComp;
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Asking FTP " + String.valueOf(provider) + " for component after " + String.valueOf(aComponent));
                }
                if ((beforeComp = (policy = provider.getFocusTraversalPolicy()).getComponentBefore(provider, aComponent)) != null) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("### FTP returned " + String.valueOf(beforeComp));
                    }
                    return beforeComp;
                }
                aComponent = provider;
                if (this.accept(aComponent)) {
                    return aComponent;
                }
            }
            List<Component> cycle = this.getFocusTraversalCycle(aContainer);
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### Cycle is " + String.valueOf(cycle) + ", component is " + String.valueOf(aComponent));
            }
            if ((index = this.getComponentIndex(cycle, aComponent)) < 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Didn't find component " + String.valueOf(aComponent) + " in a cycle " + String.valueOf(aContainer));
                }
                return this.getLastComponent(aContainer);
            }
            Component comp = null;
            Component tryComp = null;
            --index;
            while (index >= 0) {
                comp = cycle.get(index);
                if (comp != aContainer && (tryComp = this.getComponentDownCycle(comp, 1)) != null) {
                    return tryComp;
                }
                if (this.accept(comp)) {
                    return comp;
                }
                --index;
            }
            if (aContainer.isFocusCycleRoot()) {
                this.cachedRoot = aContainer;
                this.cachedCycle = cycle;
                comp = this.getLastComponent(aContainer);
                this.cachedRoot = null;
                this.cachedCycle = null;
                return comp;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component getFirstComponent(Container aContainer) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### Getting first component in " + String.valueOf(aContainer));
        }
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Object object = aContainer.getTreeLock();
        synchronized (object) {
            if (!aContainer.isVisible() || !aContainer.isDisplayable()) {
                return null;
            }
            List<Component> cycle = this.cachedRoot == aContainer ? this.cachedCycle : this.getFocusTraversalCycle(aContainer);
            if (cycle.size() == 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Cycle is empty");
                }
                return null;
            }
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### Cycle is " + String.valueOf(cycle));
            }
            for (Component comp : cycle) {
                if (this.accept(comp)) {
                    return comp;
                }
                if (comp == aContainer || (comp = this.getComponentDownCycle(comp, 0)) == null) continue;
                return comp;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Component getLastComponent(Container aContainer) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("### Getting last component in " + String.valueOf(aContainer));
        }
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Object object = aContainer.getTreeLock();
        synchronized (object) {
            if (!aContainer.isVisible() || !aContainer.isDisplayable()) {
                return null;
            }
            List<Component> cycle = this.cachedRoot == aContainer ? this.cachedCycle : this.getFocusTraversalCycle(aContainer);
            if (cycle.size() == 0) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("### Cycle is empty");
                }
                return null;
            }
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("### Cycle is " + String.valueOf(cycle));
            }
            for (int i = cycle.size() - 1; i >= 0; --i) {
                Component retComp;
                Container cont;
                Component comp = cycle.get(i);
                if (this.accept(comp)) {
                    return comp;
                }
                if (!(comp instanceof Container) || comp == aContainer || !(cont = (Container)comp).isFocusTraversalPolicyProvider() || (retComp = cont.getFocusTraversalPolicy().getLastComponent(cont)) == null) continue;
                return retComp;
            }
        }
        return null;
    }

    @Override
    public Component getDefaultComponent(Container aContainer) {
        return this.getFirstComponent(aContainer);
    }

    public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal) {
        this.implicitDownCycleTraversal = implicitDownCycleTraversal;
    }

    public boolean getImplicitDownCycleTraversal() {
        return this.implicitDownCycleTraversal;
    }

    protected boolean accept(Component aComponent) {
        if (!aComponent.canBeFocusOwner()) {
            return false;
        }
        if (!(aComponent instanceof Window)) {
            for (Container enableTest = aComponent.getParent(); enableTest != null; enableTest = enableTest.getParent()) {
                if (!enableTest.isEnabled() && !enableTest.isLightweight()) {
                    return false;
                }
                if (enableTest instanceof Window) break;
            }
        }
        return true;
    }
}

