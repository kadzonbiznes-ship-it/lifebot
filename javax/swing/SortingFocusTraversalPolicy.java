/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.InternalFrameFocusTraversalPolicy;
import javax.swing.JComponent;
import javax.swing.SwingContainerOrderFocusTraversalPolicy;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class SortingFocusTraversalPolicy
extends InternalFrameFocusTraversalPolicy {
    private Comparator<? super Component> comparator;
    private boolean implicitDownCycleTraversal = true;
    private PlatformLogger log = PlatformLogger.getLogger("javax.swing.SortingFocusTraversalPolicy");
    private transient Container cachedRoot;
    private transient List<Component> cachedCycle;
    private static final SwingContainerOrderFocusTraversalPolicy fitnessTestPolicy = new SwingContainerOrderFocusTraversalPolicy();
    private final int FORWARD_TRAVERSAL = 0;
    private final int BACKWARD_TRAVERSAL = 1;
    private static final boolean legacySortingFTPEnabled = "true".equals(AccessController.doPrivileged(new GetPropertyAction("swing.legacySortingFTPEnabled", "true")));

    protected SortingFocusTraversalPolicy() {
    }

    public SortingFocusTraversalPolicy(Comparator<? super Component> comparator) {
        this.comparator = comparator;
    }

    private List<Component> getFocusTraversalCycle(Container aContainer) {
        ArrayList<Component> cycle = new ArrayList<Component>();
        this.enumerateAndSortCycle(aContainer, cycle);
        return cycle;
    }

    private int getComponentIndex(List<Component> cycle, Component aComponent) {
        int index;
        try {
            index = Collections.binarySearch(cycle, aComponent, this.comparator);
        }
        catch (ClassCastException e) {
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### During the binary search for " + String.valueOf(aComponent) + " the exception occurred: ", e);
            }
            return -1;
        }
        if (index < 0) {
            index = cycle.indexOf(aComponent);
        }
        return index;
    }

    private void enumerateAndSortCycle(Container focusCycleRoot, List<Component> cycle) {
        if (focusCycleRoot.isShowing()) {
            this.enumerateCycle(focusCycleRoot, cycle);
            if (legacySortingFTPEnabled) {
                this.legacySort(cycle, this.comparator);
            } else {
                cycle.sort(this.comparator);
            }
        }
    }

    private void legacySort(List<Component> l, Comparator<? super Component> c) {
        if (c != null && l.size() > 1) {
            Component[] a = l.toArray(new Component[l.size()]);
            this.mergeSort((Component[])a.clone(), a, 0, a.length, 0, c);
            ListIterator<Component> i = l.listIterator();
            for (Component e : a) {
                i.next();
                i.set(e);
            }
        }
    }

    private void enumerateCycle(Container container, List<Component> cycle) {
        Component[] components;
        if (!container.isVisible() || !container.isDisplayable()) {
            return;
        }
        cycle.add(container);
        for (Component comp : components = container.getComponents()) {
            Container cont;
            if (!(!(comp instanceof Container) || (cont = (Container)comp).isFocusCycleRoot() || cont.isFocusTraversalPolicyProvider() || cont instanceof JComponent && ((JComponent)cont).isManagingFocus())) {
                this.enumerateCycle(cont, cycle);
                continue;
            }
            cycle.add(comp);
        }
    }

    Container getTopmostProvider(Container focusCycleRoot, Component aComponent) {
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
            if (retComp == null || !this.log.isLoggable(PlatformLogger.Level.FINE)) return retComp;
            this.log.fine("### Transferred focus down-cycle to " + String.valueOf(retComp) + " in the focus cycle root " + String.valueOf(cont));
            return retComp;
        } else {
            if (!cont.isFocusTraversalPolicyProvider()) return retComp;
            Component component = retComp = traversalDirection == 0 ? cont.getFocusTraversalPolicy().getDefaultComponent(cont) : cont.getFocusTraversalPolicy().getLastComponent(cont);
            if (retComp == null || !this.log.isLoggable(PlatformLogger.Level.FINE)) return retComp;
            this.log.fine("### Transferred focus to " + String.valueOf(retComp) + " in the FTP provider " + String.valueOf(cont));
        }
        return retComp;
    }

    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        int index;
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Searching in " + String.valueOf(aContainer) + " for component after " + String.valueOf(aComponent));
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
        Component comp = this.getComponentDownCycle(aComponent, 0);
        if (comp != null) {
            return comp;
        }
        Container provider = this.getTopmostProvider(aContainer, aComponent);
        if (provider != null) {
            FocusTraversalPolicy policy;
            Component afterComp;
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Asking FTP " + String.valueOf(provider) + " for component after " + String.valueOf(aComponent));
            }
            if ((afterComp = (policy = provider.getFocusTraversalPolicy()).getComponentAfter(provider, aComponent)) != null) {
                if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                    this.log.fine("### FTP returned " + String.valueOf(afterComp));
                }
                return afterComp;
            }
            aComponent = provider;
        }
        List<Component> cycle = this.getFocusTraversalCycle(aContainer);
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Cycle is " + String.valueOf(cycle) + ", component is " + String.valueOf(aComponent));
        }
        if ((index = this.getComponentIndex(cycle, aComponent)) < 0) {
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Didn't find component " + String.valueOf(aComponent) + " in a cycle " + String.valueOf(aContainer));
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
        return null;
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        Component comp;
        int index;
        if (aContainer == null || aComponent == null) {
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        if (!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()) {
            throw new IllegalArgumentException("aContainer should be focus cycle root or focus traversal policy provider");
        }
        if (aContainer.isFocusCycleRoot() && !aComponent.isFocusCycleRoot(aContainer)) {
            throw new IllegalArgumentException("aContainer is not a focus cycle root of aComponent");
        }
        Container provider = this.getTopmostProvider(aContainer, aComponent);
        if (provider != null) {
            FocusTraversalPolicy policy;
            Component beforeComp;
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Asking FTP " + String.valueOf(provider) + " for component after " + String.valueOf(aComponent));
            }
            if ((beforeComp = (policy = provider.getFocusTraversalPolicy()).getComponentBefore(provider, aComponent)) != null) {
                if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                    this.log.fine("### FTP returned " + String.valueOf(beforeComp));
                }
                return beforeComp;
            }
            aComponent = provider;
            if (this.accept(aComponent)) {
                return aComponent;
            }
        }
        List<Component> cycle = this.getFocusTraversalCycle(aContainer);
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Cycle is " + String.valueOf(cycle) + ", component is " + String.valueOf(aComponent));
        }
        if ((index = this.getComponentIndex(cycle, aComponent)) < 0) {
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Didn't find component " + String.valueOf(aComponent) + " in a cycle " + String.valueOf(aContainer));
            }
            return this.getLastComponent(aContainer);
        }
        --index;
        while (index >= 0) {
            Component tryComp;
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
        return null;
    }

    @Override
    public Component getFirstComponent(Container aContainer) {
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Getting first component in " + String.valueOf(aContainer));
        }
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        List<Component> cycle = this.cachedRoot == aContainer ? this.cachedCycle : this.getFocusTraversalCycle(aContainer);
        if (cycle.size() == 0) {
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Cycle is empty");
            }
            return null;
        }
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Cycle is " + String.valueOf(cycle));
        }
        for (Component comp : cycle) {
            if (this.accept(comp)) {
                return comp;
            }
            if (comp == aContainer || (comp = this.getComponentDownCycle(comp, 0)) == null) continue;
            return comp;
        }
        return null;
    }

    @Override
    public Component getLastComponent(Container aContainer) {
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Getting last component in " + String.valueOf(aContainer));
        }
        if (aContainer == null) {
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        List<Component> cycle = this.cachedRoot == aContainer ? this.cachedCycle : this.getFocusTraversalCycle(aContainer);
        if (cycle.size() == 0) {
            if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
                this.log.fine("### Cycle is empty");
            }
            return null;
        }
        if (this.log.isLoggable(PlatformLogger.Level.FINE)) {
            this.log.fine("### Cycle is " + String.valueOf(cycle));
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

    protected void setComparator(Comparator<? super Component> comparator) {
        this.comparator = comparator;
    }

    protected Comparator<? super Component> getComparator() {
        return this.comparator;
    }

    protected boolean accept(Component aComponent) {
        return fitnessTestPolicy.accept(aComponent);
    }

    private <T> void mergeSort(T[] src, T[] dest, int low, int high, int off, Comparator<? super T> c) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; ++i) {
                for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; --j) {
                    T t = dest[j];
                    dest[j] = dest[j - 1];
                    dest[j - 1] = t;
                }
            }
            return;
        }
        int destLow = low;
        int destHigh = high;
        int mid = (low += off) + (high += off) >>> 1;
        this.mergeSort(dest, src, low, mid, -off, c);
        this.mergeSort(dest, src, mid, high, -off, c);
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }
        int p = low;
        int q = mid;
        for (int i = destLow; i < destHigh; ++i) {
            dest[i] = q >= high || p < mid && c.compare(src[p], src[q]) <= 0 ? src[p++] : src[q++];
        }
    }
}

