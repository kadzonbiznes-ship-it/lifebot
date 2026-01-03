/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;
import javax.swing.text.Position;
import sun.awt.AWTAccessor;
import sun.swing.SwingUtilities2;

@JavaBean(defaultProperty="UI", description="A component which allows for the selection of one or more objects from a list.")
@SwingContainer(value=false)
public class JList<E>
extends JComponent
implements Scrollable,
Accessible {
    private static final String uiClassID = "ListUI";
    public static final int VERTICAL = 0;
    public static final int VERTICAL_WRAP = 1;
    public static final int HORIZONTAL_WRAP = 2;
    private int fixedCellWidth = -1;
    private int fixedCellHeight = -1;
    private int horizontalScrollIncrement = -1;
    private E prototypeCellValue;
    private int visibleRowCount = 8;
    private Color selectionForeground;
    private Color selectionBackground;
    private boolean dragEnabled;
    private ListSelectionModel selectionModel;
    private ListModel<E> dataModel;
    private ListCellRenderer<? super E> cellRenderer;
    private ListSelectionListener selectionListener;
    private int layoutOrientation;
    private DropMode dropMode = DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    private transient boolean updateInProgress;

    public JList(ListModel<E> dataModel) {
        if (dataModel == null) {
            throw new IllegalArgumentException("dataModel must be non null");
        }
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        this.layoutOrientation = 0;
        this.dataModel = dataModel;
        this.selectionModel = this.createSelectionModel();
        this.setAutoscrolls(true);
        this.updateUI();
    }

    public JList(final E[] listData) {
        this(new AbstractListModel<E>(){

            @Override
            public int getSize() {
                return listData.length;
            }

            @Override
            public E getElementAt(int i) {
                return listData[i];
            }
        });
    }

    public JList(final Vector<? extends E> listData) {
        this(new AbstractListModel<E>(){

            @Override
            public int getSize() {
                return listData.size();
            }

            @Override
            public E getElementAt(int i) {
                return listData.elementAt(i);
            }
        });
    }

    public JList() {
        this(new AbstractListModel<E>(){

            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public E getElementAt(int i) {
                throw new IndexOutOfBoundsException("No Data Model");
            }
        });
    }

    @Override
    public ListUI getUI() {
        return (ListUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(ListUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        if (!this.updateInProgress) {
            this.updateInProgress = true;
            try {
                this.setUI((ListUI)UIManager.getUI(this));
                ListCellRenderer<E> renderer = this.getCellRenderer();
                if (renderer instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component)((Object)renderer));
                }
            }
            finally {
                this.updateInProgress = false;
            }
        }
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    private void updateFixedCellSize() {
        ListCellRenderer<E> cr = this.getCellRenderer();
        E value = this.getPrototypeCellValue();
        if (cr != null && value != null) {
            Component c = cr.getListCellRendererComponent(this, value, 0, false, false);
            Font f = c.getFont();
            c.setFont(this.getFont());
            Dimension d = c.getPreferredSize();
            this.fixedCellWidth = d.width;
            this.fixedCellHeight = d.height;
            c.setFont(f);
        }
    }

    public E getPrototypeCellValue() {
        return this.prototypeCellValue;
    }

    @BeanProperty(visualUpdate=true, description="The cell prototype value, used to compute cell width and height.")
    public void setPrototypeCellValue(E prototypeCellValue) {
        E oldValue = this.prototypeCellValue;
        this.prototypeCellValue = prototypeCellValue;
        if (prototypeCellValue != null && !prototypeCellValue.equals(oldValue)) {
            this.updateFixedCellSize();
        }
        this.firePropertyChange("prototypeCellValue", oldValue, prototypeCellValue);
    }

    public int getFixedCellWidth() {
        return this.fixedCellWidth;
    }

    @BeanProperty(visualUpdate=true, description="Defines a fixed cell width when greater than zero.")
    public void setFixedCellWidth(int width) {
        int oldValue = this.fixedCellWidth;
        this.fixedCellWidth = width;
        this.firePropertyChange("fixedCellWidth", oldValue, this.fixedCellWidth);
    }

    public int getFixedCellHeight() {
        return this.fixedCellHeight;
    }

    @BeanProperty(visualUpdate=true, description="Defines a fixed cell height when greater than zero.")
    public void setFixedCellHeight(int height) {
        int oldValue = this.fixedCellHeight;
        this.fixedCellHeight = height;
        this.firePropertyChange("fixedCellHeight", oldValue, this.fixedCellHeight);
    }

    @Transient
    public ListCellRenderer<? super E> getCellRenderer() {
        return this.cellRenderer;
    }

    @BeanProperty(visualUpdate=true, description="The component used to draw the cells.")
    public void setCellRenderer(ListCellRenderer<? super E> cellRenderer) {
        ListCellRenderer<? super E> oldValue = this.cellRenderer;
        this.cellRenderer = cellRenderer;
        if (cellRenderer != null && !cellRenderer.equals(oldValue)) {
            this.updateFixedCellSize();
        }
        this.firePropertyChange("cellRenderer", oldValue, cellRenderer);
    }

    public Color getSelectionForeground() {
        return this.selectionForeground;
    }

    @BeanProperty(visualUpdate=true, description="The foreground color of selected cells.")
    public void setSelectionForeground(Color selectionForeground) {
        Color oldValue = this.selectionForeground;
        this.selectionForeground = selectionForeground;
        this.firePropertyChange("selectionForeground", oldValue, selectionForeground);
    }

    public Color getSelectionBackground() {
        return this.selectionBackground;
    }

    @BeanProperty(visualUpdate=true, description="The background color of selected cells.")
    public void setSelectionBackground(Color selectionBackground) {
        Color oldValue = this.selectionBackground;
        this.selectionBackground = selectionBackground;
        this.firePropertyChange("selectionBackground", oldValue, selectionBackground);
    }

    public int getVisibleRowCount() {
        return this.visibleRowCount;
    }

    @BeanProperty(visualUpdate=true, description="The preferred number of rows to display without requiring scrolling")
    public void setVisibleRowCount(int visibleRowCount) {
        int oldValue = this.visibleRowCount;
        this.visibleRowCount = Math.max(0, visibleRowCount);
        this.firePropertyChange("visibleRowCount", oldValue, visibleRowCount);
    }

    public int getLayoutOrientation() {
        return this.layoutOrientation;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"JList.VERTICAL", "JList.HORIZONTAL_WRAP", "JList.VERTICAL_WRAP"}, description="Defines the way list cells are laid out.")
    public void setLayoutOrientation(int layoutOrientation) {
        int oldValue = this.layoutOrientation;
        switch (layoutOrientation) {
            case 0: 
            case 1: 
            case 2: {
                this.layoutOrientation = layoutOrientation;
                this.firePropertyChange("layoutOrientation", oldValue, layoutOrientation);
                break;
            }
            default: {
                throw new IllegalArgumentException("layoutOrientation must be one of: VERTICAL, HORIZONTAL_WRAP or VERTICAL_WRAP");
            }
        }
    }

    @BeanProperty(bound=false)
    public int getFirstVisibleIndex() {
        Rectangle bounds;
        Rectangle r = this.getVisibleRect();
        int first = this.getComponentOrientation().isLeftToRight() ? this.locationToIndex(r.getLocation()) : this.locationToIndex(new Point(r.x + r.width - 1, r.y));
        if (first != -1 && (bounds = this.getCellBounds(first, first)) != null) {
            SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height, bounds);
            if (bounds.width == 0 || bounds.height == 0) {
                first = -1;
            }
        }
        return first;
    }

    @BeanProperty(bound=false)
    public int getLastVisibleIndex() {
        Rectangle bounds;
        boolean leftToRight = this.getComponentOrientation().isLeftToRight();
        Rectangle r = this.getVisibleRect();
        Point lastPoint = leftToRight ? new Point(r.x + r.width - 1, r.y + r.height - 1) : new Point(r.x, r.y + r.height - 1);
        int location = this.locationToIndex(lastPoint);
        if (location != -1 && (bounds = this.getCellBounds(location, location)) != null) {
            SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height, bounds);
            if (bounds.width == 0 || bounds.height == 0) {
                int last;
                boolean isHorizontalWrap = this.getLayoutOrientation() == 2;
                Point visibleLocation = isHorizontalWrap ? new Point(lastPoint.x, r.y) : new Point(r.x, lastPoint.y);
                int visIndex = -1;
                int lIndex = location;
                location = -1;
                do {
                    last = visIndex;
                    visIndex = this.locationToIndex(visibleLocation);
                    if (visIndex == -1) continue;
                    bounds = this.getCellBounds(visIndex, visIndex);
                    if (visIndex != lIndex && bounds != null && bounds.contains(visibleLocation)) {
                        location = visIndex;
                        if (isHorizontalWrap) {
                            visibleLocation.y = bounds.y + bounds.height;
                            if (visibleLocation.y < lastPoint.y) continue;
                            last = visIndex;
                            continue;
                        }
                        visibleLocation.x = bounds.x + bounds.width;
                        if (visibleLocation.x < lastPoint.x) continue;
                        last = visIndex;
                        continue;
                    }
                    last = visIndex;
                } while (visIndex != -1 && last != visIndex);
            }
        }
        return location;
    }

    public void ensureIndexIsVisible(int index) {
        Rectangle cellBounds = this.getCellBounds(index, index);
        if (cellBounds != null) {
            this.scrollRectToVisible(cellBounds);
        }
    }

    @BeanProperty(bound=false, description="determines whether automatic drag handling is enabled")
    public void setDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        this.dragEnabled = b;
    }

    public boolean getDragEnabled() {
        return this.dragEnabled;
    }

    public final void setDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case USE_SELECTION: 
                case ON: 
                case INSERT: 
                case ON_OR_INSERT: {
                    this.dropMode = dropMode;
                    return;
                }
            }
        }
        throw new IllegalArgumentException(String.valueOf((Object)dropMode) + ": Unsupported drop mode for list");
    }

    public final DropMode getDropMode() {
        return this.dropMode;
    }

    @Override
    DropLocation dropLocationForPoint(Point p) {
        DropLocation location = null;
        Rectangle rect = null;
        int index = this.locationToIndex(p);
        if (index != -1) {
            rect = this.getCellBounds(index, index);
        }
        switch (this.dropMode) {
            case USE_SELECTION: 
            case ON: {
                location = new DropLocation(p, rect != null && rect.contains(p) ? index : -1, false);
                break;
            }
            case INSERT: {
                if (index == -1) {
                    location = new DropLocation(p, this.getModel().getSize(), true);
                    break;
                }
                if (this.layoutOrientation == 2) {
                    boolean ltr = this.getComponentOrientation().isLeftToRight();
                    if (SwingUtilities2.liesInHorizontal(rect, p, ltr, false) == SwingUtilities2.Section.TRAILING) {
                        ++index;
                    } else if (index == this.getModel().getSize() - 1 && p.y >= rect.y + rect.height) {
                        ++index;
                    }
                } else if (SwingUtilities2.liesInVertical(rect, p, false) == SwingUtilities2.Section.TRAILING) {
                    ++index;
                }
                location = new DropLocation(p, index, true);
                break;
            }
            case ON_OR_INSERT: {
                if (index == -1) {
                    location = new DropLocation(p, this.getModel().getSize(), true);
                    break;
                }
                boolean between = false;
                if (this.layoutOrientation == 2) {
                    boolean ltr = this.getComponentOrientation().isLeftToRight();
                    SwingUtilities2.Section section = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                    if (section == SwingUtilities2.Section.TRAILING) {
                        ++index;
                        between = true;
                    } else if (index == this.getModel().getSize() - 1 && p.y >= rect.y + rect.height) {
                        ++index;
                        between = true;
                    } else if (section == SwingUtilities2.Section.LEADING) {
                        between = true;
                    }
                } else {
                    SwingUtilities2.Section section = SwingUtilities2.liesInVertical(rect, p, true);
                    if (section == SwingUtilities2.Section.LEADING) {
                        between = true;
                    } else if (section == SwingUtilities2.Section.TRAILING) {
                        ++index;
                        between = true;
                    }
                }
                location = new DropLocation(p, index, between);
                break;
            }
            default: {
                assert (false) : "Unexpected drop mode";
                break;
            }
        }
        return location;
    }

    @Override
    Object setDropLocation(TransferHandler.DropLocation location, Object state, boolean forDrop) {
        Object retVal = null;
        DropLocation listLocation = (DropLocation)location;
        if (this.dropMode == DropMode.USE_SELECTION) {
            if (listLocation == null) {
                if (!forDrop && state != null) {
                    this.setSelectedIndices(((int[][])state)[0]);
                    int anchor = ((int[][])state)[1][0];
                    int lead = ((int[][])state)[1][1];
                    SwingUtilities2.setLeadAnchorWithoutSelection(this.getSelectionModel(), lead, anchor);
                }
            } else {
                if (this.dropLocation == null) {
                    int[] inds = this.getSelectedIndices();
                    retVal = new int[][]{inds, {this.getAnchorSelectionIndex(), this.getLeadSelectionIndex()}};
                } else {
                    retVal = state;
                }
                int index = listLocation.getIndex();
                if (index == -1) {
                    this.clearSelection();
                    this.getSelectionModel().setAnchorSelectionIndex(-1);
                    this.getSelectionModel().setLeadSelectionIndex(-1);
                } else {
                    this.setSelectionInterval(index, index);
                }
            }
        }
        DropLocation old = this.dropLocation;
        this.dropLocation = listLocation;
        this.firePropertyChange("dropLocation", old, this.dropLocation);
        return retVal;
    }

    @BeanProperty(bound=false)
    public final DropLocation getDropLocation() {
        return this.dropLocation;
    }

    public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
        ListModel<E> model = this.getModel();
        int max = model.getSize();
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (startIndex < 0 || startIndex >= max) {
            throw new IllegalArgumentException();
        }
        prefix = prefix.toUpperCase();
        int increment = bias == Position.Bias.Forward ? 1 : -1;
        int index = startIndex;
        do {
            String string;
            E element;
            if ((element = model.getElementAt(index)) == null) continue;
            if (element instanceof String) {
                string = ((String)element).toUpperCase();
            } else {
                string = element.toString();
                if (string != null) {
                    string = string.toUpperCase();
                }
            }
            if (string == null || !string.startsWith(prefix)) continue;
            return index;
        } while ((index = (index + increment + max) % max) != startIndex);
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            Rectangle cellBounds;
            Point p = event.getPoint();
            int index = this.locationToIndex(p);
            ListCellRenderer<E> r = this.getCellRenderer();
            if (index != -1 && r != null && (cellBounds = this.getCellBounds(index, index)) != null && cellBounds.contains(p.x, p.y)) {
                ListSelectionModel lsm = this.getSelectionModel();
                Component rComponent = r.getListCellRendererComponent(this, this.getModel().getElementAt(index), index, lsm.isSelectedIndex(index), this.hasFocus() && lsm.getLeadSelectionIndex() == index);
                if (rComponent instanceof JComponent) {
                    p.translate(-cellBounds.x, -cellBounds.y);
                    MouseEvent newEvent = new MouseEvent(rComponent, event.getID(), event.getWhen(), event.getModifiers(), p.x, p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), 0);
                    AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
                    meAccessor.setCausedByTouchEvent(newEvent, meAccessor.isCausedByTouchEvent(event));
                    String tip = ((JComponent)rComponent).getToolTipText(newEvent);
                    if (tip != null) {
                        return tip;
                    }
                }
            }
        }
        return super.getToolTipText();
    }

    public int locationToIndex(Point location) {
        ListUI ui = this.getUI();
        return ui != null ? ui.locationToIndex(this, location) : -1;
    }

    public Point indexToLocation(int index) {
        ListUI ui = this.getUI();
        return ui != null ? ui.indexToLocation(this, index) : null;
    }

    public Rectangle getCellBounds(int index0, int index1) {
        ListUI ui = this.getUI();
        return ui != null ? ui.getCellBounds(this, index0, index1) : null;
    }

    public ListModel<E> getModel() {
        return this.dataModel;
    }

    @BeanProperty(visualUpdate=true, description="The object that contains the data to be drawn by this JList.")
    public void setModel(ListModel<E> model) {
        if (model == null) {
            throw new IllegalArgumentException("model must be non null");
        }
        ListModel<E> oldValue = this.dataModel;
        this.dataModel = model;
        this.firePropertyChange("model", oldValue, this.dataModel);
        this.clearSelection();
    }

    public void setListData(final E[] listData) {
        this.setModel(new AbstractListModel<E>(this){
            final /* synthetic */ JList this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public int getSize() {
                return listData.length;
            }

            @Override
            public E getElementAt(int i) {
                return listData[i];
            }
        });
    }

    public void setListData(final Vector<? extends E> listData) {
        this.setModel(new AbstractListModel<E>(this){
            final /* synthetic */ JList this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public int getSize() {
                return listData.size();
            }

            @Override
            public E getElementAt(int i) {
                return listData.elementAt(i);
            }
        });
    }

    protected ListSelectionModel createSelectionModel() {
        return new DefaultListSelectionModel();
    }

    public ListSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    protected void fireSelectionValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
        Object[] listeners = this.listenerList.getListenerList();
        ListSelectionEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ListSelectionListener.class) continue;
            if (e == null) {
                e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
            }
            ((ListSelectionListener)listeners[i + 1]).valueChanged(e);
        }
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        if (this.selectionListener == null) {
            this.selectionListener = new ListSelectionHandler();
            this.getSelectionModel().addListSelectionListener(this.selectionListener);
        }
        this.listenerList.add(ListSelectionListener.class, listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        this.listenerList.remove(ListSelectionListener.class, listener);
    }

    @BeanProperty(bound=false)
    public ListSelectionListener[] getListSelectionListeners() {
        return (ListSelectionListener[])this.listenerList.getListeners(ListSelectionListener.class);
    }

    @BeanProperty(description="The selection model, recording which cells are selected.")
    public void setSelectionModel(ListSelectionModel selectionModel) {
        if (selectionModel == null) {
            throw new IllegalArgumentException("selectionModel must be non null");
        }
        if (this.selectionListener != null) {
            this.selectionModel.removeListSelectionListener(this.selectionListener);
            selectionModel.addListSelectionListener(this.selectionListener);
        }
        ListSelectionModel oldValue = this.selectionModel;
        this.selectionModel = selectionModel;
        this.firePropertyChange("selectionModel", oldValue, selectionModel);
    }

    @BeanProperty(bound=false, enumerationValues={"ListSelectionModel.SINGLE_SELECTION", "ListSelectionModel.SINGLE_INTERVAL_SELECTION", "ListSelectionModel.MULTIPLE_INTERVAL_SELECTION"}, description="The selection mode.")
    public void setSelectionMode(int selectionMode) {
        this.getSelectionModel().setSelectionMode(selectionMode);
    }

    public int getSelectionMode() {
        return this.getSelectionModel().getSelectionMode();
    }

    @BeanProperty(bound=false)
    public int getAnchorSelectionIndex() {
        return this.getSelectionModel().getAnchorSelectionIndex();
    }

    @BeanProperty(bound=false, description="The lead selection index.")
    public int getLeadSelectionIndex() {
        return this.getSelectionModel().getLeadSelectionIndex();
    }

    @BeanProperty(bound=false)
    public int getMinSelectionIndex() {
        return this.getSelectionModel().getMinSelectionIndex();
    }

    @BeanProperty(bound=false)
    public int getMaxSelectionIndex() {
        return this.getSelectionModel().getMaxSelectionIndex();
    }

    public boolean isSelectedIndex(int index) {
        return this.getSelectionModel().isSelectedIndex(index);
    }

    @BeanProperty(bound=false)
    public boolean isSelectionEmpty() {
        return this.getSelectionModel().isSelectionEmpty();
    }

    public void clearSelection() {
        this.getSelectionModel().clearSelection();
    }

    public void setSelectionInterval(int anchor, int lead) {
        this.getSelectionModel().setSelectionInterval(anchor, lead);
    }

    public void addSelectionInterval(int anchor, int lead) {
        this.getSelectionModel().addSelectionInterval(anchor, lead);
    }

    public void removeSelectionInterval(int index0, int index1) {
        this.getSelectionModel().removeSelectionInterval(index0, index1);
    }

    public void setValueIsAdjusting(boolean b) {
        this.getSelectionModel().setValueIsAdjusting(b);
    }

    public boolean getValueIsAdjusting() {
        return this.getSelectionModel().getValueIsAdjusting();
    }

    @Transient
    public int[] getSelectedIndices() {
        return this.getSelectionModel().getSelectedIndices();
    }

    @BeanProperty(bound=false, description="The index of the selected cell.")
    public void setSelectedIndex(int index) {
        if (index >= this.getModel().getSize()) {
            return;
        }
        this.getSelectionModel().setSelectionInterval(index, index);
    }

    public void setSelectedIndices(int[] indices) {
        ListSelectionModel sm = this.getSelectionModel();
        sm.clearSelection();
        int size = this.getModel().getSize();
        for (int i : indices) {
            if (i >= size) continue;
            sm.addSelectionInterval(i, i);
        }
    }

    @Deprecated
    @BeanProperty(bound=false)
    public Object[] getSelectedValues() {
        ListSelectionModel sm = this.getSelectionModel();
        ListModel<E> dm = this.getModel();
        int iMin = sm.getMinSelectionIndex();
        int iMax = sm.getMaxSelectionIndex();
        int size = dm.getSize();
        if (iMin < 0 || iMax < 0 || iMin >= size) {
            return new Object[0];
        }
        iMax = iMax < size ? iMax : size - 1;
        Object[] rvTmp = new Object[1 + (iMax - iMin)];
        int n = 0;
        for (int i = iMin; i <= iMax; ++i) {
            if (!sm.isSelectedIndex(i)) continue;
            rvTmp[n++] = dm.getElementAt(i);
        }
        Object[] rv = new Object[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }

    @BeanProperty(bound=false)
    public List<E> getSelectedValuesList() {
        ListModel<E> dm = this.getModel();
        int[] selectedIndices = this.getSelectedIndices();
        if (selectedIndices.length > 0) {
            int size = dm.getSize();
            if (selectedIndices[0] >= size) {
                return Collections.emptyList();
            }
            ArrayList<E> selectedItems = new ArrayList<E>();
            for (int i : selectedIndices) {
                if (i >= size) break;
                selectedItems.add(dm.getElementAt(i));
            }
            return selectedItems;
        }
        return Collections.emptyList();
    }

    public int getSelectedIndex() {
        return this.getMinSelectionIndex();
    }

    @BeanProperty(bound=false)
    public E getSelectedValue() {
        int i = this.getMinSelectionIndex();
        return i == -1 || i >= this.getModel().getSize() ? null : (E)this.getModel().getElementAt(i);
    }

    public void setSelectedValue(Object anObject, boolean shouldScroll) {
        if (anObject == null) {
            this.clearSelection();
        } else if (!anObject.equals(this.getSelectedValue())) {
            ListModel<E> dm = this.getModel();
            int c = dm.getSize();
            for (int i = 0; i < c; ++i) {
                if (!anObject.equals(dm.getElementAt(i))) continue;
                this.setSelectedIndex(i);
                if (shouldScroll) {
                    this.ensureIndexIsVisible(i);
                }
                this.repaint();
                return;
            }
            this.setSelectedIndex(-1);
        }
        this.repaint();
    }

    private void checkScrollableParameters(Rectangle visibleRect, int orientation) {
        if (visibleRect == null) {
            throw new IllegalArgumentException("visibleRect must be non-null");
        }
        switch (orientation) {
            case 0: 
            case 1: {
                break;
            }
            default: {
                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
            }
        }
    }

    @Override
    @BeanProperty(bound=false)
    public Dimension getPreferredScrollableViewportSize() {
        if (this.getLayoutOrientation() != 0) {
            return this.getPreferredSize();
        }
        Insets insets = this.getInsets();
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        int visibleRowCount = this.getVisibleRowCount();
        int fixedCellWidth = this.getFixedCellWidth();
        int fixedCellHeight = this.getFixedCellHeight();
        if (fixedCellWidth > 0 && fixedCellHeight > 0) {
            int width = fixedCellWidth + dx;
            int height = visibleRowCount * fixedCellHeight + dy;
            return new Dimension(width, height);
        }
        if (this.getModel().getSize() > 0) {
            int width = this.getPreferredSize().width;
            Rectangle r = this.getCellBounds(0, 0);
            int height = r != null ? visibleRowCount * r.height + dy : 1;
            return new Dimension(width, height);
        }
        fixedCellWidth = fixedCellWidth > 0 ? fixedCellWidth : 256;
        fixedCellHeight = fixedCellHeight > 0 ? fixedCellHeight : 16;
        return new Dimension(fixedCellWidth, fixedCellHeight * visibleRowCount);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        Rectangle cellBounds;
        boolean leftToRight;
        Point leadingPoint;
        int index;
        this.checkScrollableParameters(visibleRect, orientation);
        if (orientation == 1) {
            int row = this.locationToIndex(visibleRect.getLocation());
            if (row == -1) {
                return 0;
            }
            if (direction > 0) {
                Rectangle r = this.getCellBounds(row, row);
                return r == null ? 0 : r.height - (visibleRect.y - r.y);
            }
            Rectangle r = this.getCellBounds(row, row);
            if (r.y == visibleRect.y && row == 0) {
                return 0;
            }
            if (r.y == visibleRect.y) {
                Point loc = r.getLocation();
                --loc.y;
                int prevIndex = this.locationToIndex(loc);
                Rectangle prevR = this.getCellBounds(prevIndex, prevIndex);
                if (prevR == null || prevR.y >= r.y) {
                    return 0;
                }
                return prevR.height;
            }
            return visibleRect.y - r.y;
        }
        if (orientation == 0 && this.getLayoutOrientation() != 0 && (index = this.locationToIndex(leadingPoint = (leftToRight = this.getComponentOrientation().isLeftToRight()) ? visibleRect.getLocation() : new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y))) != -1 && (cellBounds = this.getCellBounds(index, index)) != null && cellBounds.contains(leadingPoint)) {
            int leadingCellEdge;
            int leadingVisibleEdge;
            if (leftToRight) {
                leadingVisibleEdge = visibleRect.x;
                leadingCellEdge = cellBounds.x;
            } else {
                leadingVisibleEdge = visibleRect.x + visibleRect.width;
                leadingCellEdge = cellBounds.x + cellBounds.width;
            }
            if (leadingCellEdge != leadingVisibleEdge) {
                if (direction < 0) {
                    return Math.abs(leadingVisibleEdge - leadingCellEdge);
                }
                if (leftToRight) {
                    return leadingCellEdge + cellBounds.width - leadingVisibleEdge;
                }
                return leadingVisibleEdge - cellBounds.x;
            }
            return cellBounds.width;
        }
        Font f = this.getFont();
        return f != null ? f.getSize() : 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        this.checkScrollableParameters(visibleRect, orientation);
        if (orientation == 1) {
            int inc = visibleRect.height;
            if (direction > 0) {
                Rectangle lastRect;
                int last = this.locationToIndex(new Point(visibleRect.x, visibleRect.y + visibleRect.height - 1));
                if (last != -1 && (lastRect = this.getCellBounds(last, last)) != null && (inc = lastRect.y - visibleRect.y) == 0 && last < this.getModel().getSize() - 1) {
                    inc = lastRect.height;
                }
            } else {
                int newFirst = this.locationToIndex(new Point(visibleRect.x, visibleRect.y - visibleRect.height));
                int first = this.getFirstVisibleIndex();
                if (newFirst != -1) {
                    if (first == -1) {
                        first = this.locationToIndex(visibleRect.getLocation());
                    }
                    Rectangle newFirstRect = this.getCellBounds(newFirst, newFirst);
                    Rectangle firstRect = this.getCellBounds(first, first);
                    if (newFirstRect != null && firstRect != null) {
                        while (newFirstRect.y + visibleRect.height < firstRect.y + firstRect.height && newFirstRect.y < firstRect.y) {
                            newFirstRect = this.getCellBounds(++newFirst, newFirst);
                        }
                        inc = visibleRect.y - newFirstRect.y;
                        if (inc <= 0 && newFirstRect.y > 0 && (newFirstRect = this.getCellBounds(--newFirst, newFirst)) != null) {
                            inc = visibleRect.y - newFirstRect.y;
                        }
                    }
                }
            }
            return inc;
        }
        if (orientation == 0 && this.getLayoutOrientation() != 0) {
            boolean leftToRight = this.getComponentOrientation().isLeftToRight();
            int inc = visibleRect.width;
            if (direction > 0) {
                Rectangle lastRect;
                int x = visibleRect.x + (leftToRight ? visibleRect.width - 1 : 0);
                int last = this.locationToIndex(new Point(x, visibleRect.y));
                if (last != -1 && (lastRect = this.getCellBounds(last, last)) != null) {
                    inc = leftToRight ? lastRect.x - visibleRect.x : visibleRect.x + visibleRect.width - (lastRect.x + lastRect.width);
                    if (inc < 0) {
                        inc += lastRect.width;
                    } else if (inc == 0 && last < this.getModel().getSize() - 1) {
                        inc = lastRect.width;
                    }
                }
            } else {
                Rectangle firstRect;
                int x = visibleRect.x + (leftToRight ? -visibleRect.width : visibleRect.width - 1 + visibleRect.width);
                int first = this.locationToIndex(new Point(x, visibleRect.y));
                if (first != -1 && (firstRect = this.getCellBounds(first, first)) != null) {
                    int visibleRight;
                    int firstRight = firstRect.x + firstRect.width;
                    inc = leftToRight ? (firstRect.x < visibleRect.x - visibleRect.width && firstRight < visibleRect.x ? visibleRect.x - firstRight : visibleRect.x - firstRect.x) : (firstRight > (visibleRight = visibleRect.x + visibleRect.width) + visibleRect.width && firstRect.x > visibleRight ? firstRect.x - visibleRight : firstRight - visibleRight);
                }
            }
            return inc;
        }
        return visibleRect.width;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportWidth() {
        if (this.getLayoutOrientation() == 2 && this.getVisibleRowCount() <= 0) {
            return true;
        }
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getWidth() > this.getPreferredSize().width;
        }
        return false;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportHeight() {
        if (this.getLayoutOrientation() == 1 && this.getVisibleRowCount() <= 0) {
            return true;
        }
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getHeight() > this.getPreferredSize().height;
        }
        return false;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            count = (byte)(count - 1);
            JComponent.setWriteObjCounter(this, count);
            if (count == 0 && this.ui != null) {
                this.ui.installUI(this);
            }
        }
    }

    @Override
    protected String paramString() {
        String selectionForegroundString = this.selectionForeground != null ? this.selectionForeground.toString() : "";
        String selectionBackgroundString = this.selectionBackground != null ? this.selectionBackground.toString() : "";
        return super.paramString() + ",fixedCellHeight=" + this.fixedCellHeight + ",fixedCellWidth=" + this.fixedCellWidth + ",horizontalScrollIncrement=" + this.horizontalScrollIncrement + ",selectionBackground=" + selectionBackgroundString + ",selectionForeground=" + selectionForegroundString + ",visibleRowCount=" + this.visibleRowCount + ",layoutOrientation=" + this.layoutOrientation;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJList();
        }
        return this.accessibleContext;
    }

    public static final class DropLocation
    extends TransferHandler.DropLocation {
        private final int index;
        private final boolean isInsert;

        private DropLocation(Point p, int index, boolean isInsert) {
            super(p);
            this.index = index;
            this.isInsert = isInsert;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isInsert() {
            return this.isInsert;
        }

        @Override
        public String toString() {
            return this.getClass().getName() + "[dropPoint=" + String.valueOf(this.getDropPoint()) + ",index=" + this.index + ",insert=" + this.isInsert + "]";
        }
    }

    private class ListSelectionHandler
    implements ListSelectionListener,
    Serializable {
        private ListSelectionHandler() {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            JList.this.fireSelectionValueChanged(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
        }
    }

    protected class AccessibleJList
    extends JComponent.AccessibleJComponent
    implements AccessibleSelection,
    PropertyChangeListener,
    ListSelectionListener,
    ListDataListener {
        int leadSelectionIndex;

        public AccessibleJList() {
            JList.this.addPropertyChangeListener(this);
            JList.this.getSelectionModel().addListSelectionListener(this);
            JList.this.getModel().addListDataListener(this);
            this.leadSelectionIndex = JList.this.getLeadSelectionIndex();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            Object oldValue = e.getOldValue();
            Object newValue = e.getNewValue();
            if (name.equals("model")) {
                if (oldValue instanceof ListModel) {
                    ListModel oldModel = (ListModel)oldValue;
                    oldModel.removeListDataListener(this);
                }
                if (newValue instanceof ListModel) {
                    ListModel newModel = (ListModel)newValue;
                    newModel.addListDataListener(this);
                }
            } else if (name.equals("selectionModel")) {
                if (oldValue instanceof ListSelectionModel) {
                    ListSelectionModel oldModel = (ListSelectionModel)oldValue;
                    oldModel.removeListSelectionListener(this);
                }
                if (newValue instanceof ListSelectionModel) {
                    ListSelectionModel newModel = (ListSelectionModel)newValue;
                    newModel.addListSelectionListener(this);
                }
                this.firePropertyChange("AccessibleSelection", false, true);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int oldLeadSelectionIndex = this.leadSelectionIndex;
            this.leadSelectionIndex = JList.this.getLeadSelectionIndex();
            if (oldLeadSelectionIndex != this.leadSelectionIndex) {
                Accessible oldLS = oldLeadSelectionIndex >= 0 ? this.getAccessibleChild(oldLeadSelectionIndex) : null;
                Accessible newLS = this.leadSelectionIndex >= 0 ? this.getAccessibleChild(this.leadSelectionIndex) : null;
                this.firePropertyChange("AccessibleActiveDescendant", oldLS, newLS);
            }
            this.firePropertyChange("AccessibleVisibleData", false, true);
            this.firePropertyChange("AccessibleSelection", false, true);
            AccessibleStateSet s = this.getAccessibleStateSet();
            ListSelectionModel lsm = JList.this.getSelectionModel();
            if (lsm.getSelectionMode() != 0) {
                if (!s.contains(AccessibleState.MULTISELECTABLE)) {
                    s.add(AccessibleState.MULTISELECTABLE);
                    this.firePropertyChange("AccessibleState", null, AccessibleState.MULTISELECTABLE);
                }
            } else if (s.contains(AccessibleState.MULTISELECTABLE)) {
                s.remove(AccessibleState.MULTISELECTABLE);
                this.firePropertyChange("AccessibleState", AccessibleState.MULTISELECTABLE, null);
            }
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            this.firePropertyChange("AccessibleVisibleData", false, true);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            this.firePropertyChange("AccessibleVisibleData", false, true);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            this.firePropertyChange("AccessibleVisibleData", false, true);
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JList.this.selectionModel.getSelectionMode() != 0) {
                states.add(AccessibleState.MULTISELECTABLE);
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LIST;
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            int i = JList.this.locationToIndex(p);
            if (i >= 0) {
                return new AccessibleJListChild(JList.this, i);
            }
            return null;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return JList.this.getModel().getSize();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (i >= JList.this.getModel().getSize()) {
                return null;
            }
            return new AccessibleJListChild(JList.this, i);
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public int getAccessibleSelectionCount() {
            return JList.this.getSelectedIndices().length;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            int len = this.getAccessibleSelectionCount();
            if (i < 0 || i >= len) {
                return null;
            }
            return this.getAccessibleChild(JList.this.getSelectedIndices()[i]);
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            return JList.this.isSelectedIndex(i);
        }

        @Override
        public void addAccessibleSelection(int i) {
            JList.this.addSelectionInterval(i, i);
        }

        @Override
        public void removeAccessibleSelection(int i) {
            JList.this.removeSelectionInterval(i, i);
        }

        @Override
        public void clearAccessibleSelection() {
            JList.this.clearSelection();
        }

        @Override
        public void selectAllAccessibleSelection() {
            JList.this.addSelectionInterval(0, this.getAccessibleChildrenCount() - 1);
        }

        protected class AccessibleJListChild
        extends AccessibleContext
        implements Accessible,
        AccessibleComponent,
        AccessibleAction {
            private JList<E> parent = null;
            int indexInParent;
            private Component component = null;
            private AccessibleContext accessibleContext = null;
            private ListModel<E> listModel;
            private ListCellRenderer<? super E> cellRenderer = null;

            public AccessibleJListChild(JList<E> parent, int indexInParent) {
                this.parent = parent;
                this.setAccessibleParent(parent);
                this.indexInParent = indexInParent;
                if (parent != null) {
                    this.listModel = parent.getModel();
                    this.cellRenderer = parent.getCellRenderer();
                }
            }

            private Component getCurrentComponent() {
                return this.getComponentAtIndex(this.indexInParent);
            }

            AccessibleContext getCurrentAccessibleContext() {
                Component c = this.getComponentAtIndex(this.indexInParent);
                if (c instanceof Accessible) {
                    return c.getAccessibleContext();
                }
                return null;
            }

            private Component getComponentAtIndex(int index) {
                if (index < 0 || index >= this.listModel.getSize()) {
                    return null;
                }
                if (this.parent != null && this.listModel != null && this.cellRenderer != null) {
                    Object value = this.listModel.getElementAt(index);
                    boolean isSelected = this.parent.isSelectedIndex(index);
                    boolean isFocussed = this.parent.isFocusOwner() && index == this.parent.getLeadSelectionIndex();
                    return this.cellRenderer.getListCellRendererComponent(this.parent, value, index, isSelected, isFocussed);
                }
                return null;
            }

            @Override
            public AccessibleContext getAccessibleContext() {
                return this;
            }

            @Override
            public String getAccessibleName() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleName();
                }
                return null;
            }

            @Override
            public void setAccessibleName(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                }
            }

            @Override
            public String getAccessibleDescription() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                }
                return null;
            }

            @Override
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                }
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                }
                return null;
            }

            @Override
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                AccessibleStateSet s = ac != null ? ac.getAccessibleStateSet() : new AccessibleStateSet();
                s.add(AccessibleState.SELECTABLE);
                if (this.parent.isFocusOwner() && this.indexInParent == this.parent.getLeadSelectionIndex()) {
                    s.add(AccessibleState.ACTIVE);
                }
                if (this.parent.isSelectedIndex(this.indexInParent)) {
                    s.add(AccessibleState.SELECTED);
                }
                if (this.isShowing()) {
                    s.add(AccessibleState.SHOWING);
                } else if (s.contains(AccessibleState.SHOWING)) {
                    s.remove(AccessibleState.SHOWING);
                }
                if (this.isVisible()) {
                    s.add(AccessibleState.VISIBLE);
                } else if (s.contains(AccessibleState.VISIBLE)) {
                    s.remove(AccessibleState.VISIBLE);
                }
                s.add(AccessibleState.TRANSIENT);
                return s;
            }

            @Override
            public int getAccessibleIndexInParent() {
                return this.indexInParent;
            }

            @Override
            public int getAccessibleChildrenCount() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleChildrenCount();
                }
                return 0;
            }

            @Override
            public Accessible getAccessibleChild(int i) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    Accessible accessibleChild = ac.getAccessibleChild(i);
                    ac.setAccessibleParent(this);
                    return accessibleChild;
                }
                return null;
            }

            @Override
            public Locale getLocale() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getLocale();
                }
                return null;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.addPropertyChangeListener(l);
                }
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                }
            }

            @Override
            public AccessibleComponent getAccessibleComponent() {
                return this;
            }

            @Override
            public AccessibleSelection getAccessibleSelection() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                return ac != null ? ac.getAccessibleSelection() : null;
            }

            @Override
            public AccessibleText getAccessibleText() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                return ac != null ? ac.getAccessibleText() : null;
            }

            @Override
            public AccessibleValue getAccessibleValue() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                return ac != null ? ac.getAccessibleValue() : null;
            }

            @Override
            public Color getBackground() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getBackground();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.getBackground();
                }
                return null;
            }

            @Override
            public void setBackground(Color c) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setBackground(c);
                } else {
                    Component cp = this.getCurrentComponent();
                    if (cp != null) {
                        cp.setBackground(c);
                    }
                }
            }

            @Override
            public Color getForeground() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getForeground();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.getForeground();
                }
                return null;
            }

            @Override
            public void setForeground(Color c) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setForeground(c);
                } else {
                    Component cp = this.getCurrentComponent();
                    if (cp != null) {
                        cp.setForeground(c);
                    }
                }
            }

            @Override
            public Cursor getCursor() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getCursor();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.getCursor();
                }
                Accessible ap = this.getAccessibleParent();
                if (ap instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ap)).getCursor();
                }
                return null;
            }

            @Override
            public void setCursor(Cursor c) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setCursor(c);
                } else {
                    Component cp = this.getCurrentComponent();
                    if (cp != null) {
                        cp.setCursor(c);
                    }
                }
            }

            @Override
            public Font getFont() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getFont();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.getFont();
                }
                return null;
            }

            @Override
            public void setFont(Font f) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setFont(f);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setFont(f);
                    }
                }
            }

            @Override
            public FontMetrics getFontMetrics(Font f) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getFontMetrics(f);
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.getFontMetrics(f);
                }
                return null;
            }

            @Override
            public boolean isEnabled() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).isEnabled();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isEnabled();
                }
                return false;
            }

            @Override
            public void setEnabled(boolean b) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setEnabled(b);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setEnabled(b);
                    }
                }
            }

            @Override
            public boolean isVisible() {
                int fi = this.parent.getFirstVisibleIndex();
                int li = this.parent.getLastVisibleIndex();
                if (li == -1) {
                    li = this.parent.getModel().getSize() - 1;
                }
                return this.indexInParent >= fi && this.indexInParent <= li;
            }

            @Override
            public void setVisible(boolean b) {
            }

            @Override
            public boolean isShowing() {
                return this.parent.isShowing() && this.isVisible();
            }

            @Override
            public boolean contains(Point p) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    Rectangle r = ((AccessibleComponent)((Object)ac)).getBounds();
                    return r.contains(p);
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    Rectangle r = c.getBounds();
                    return r.contains(p);
                }
                return this.getBounds().contains(p);
            }

            @Override
            public Point getLocationOnScreen() {
                if (this.parent != null) {
                    Point listLocation;
                    try {
                        listLocation = this.parent.getLocationOnScreen();
                    }
                    catch (IllegalComponentStateException e) {
                        return null;
                    }
                    Point componentLocation = this.parent.indexToLocation(this.indexInParent);
                    if (componentLocation != null) {
                        componentLocation.translate(listLocation.x, listLocation.y);
                        return componentLocation;
                    }
                    return null;
                }
                return null;
            }

            @Override
            public Point getLocation() {
                if (this.parent != null) {
                    return this.parent.indexToLocation(this.indexInParent);
                }
                return null;
            }

            @Override
            public void setLocation(Point p) {
                if (this.parent != null && this.parent.contains(p)) {
                    JList.this.ensureIndexIsVisible(this.indexInParent);
                }
            }

            @Override
            public Rectangle getBounds() {
                if (this.parent != null) {
                    return this.parent.getCellBounds(this.indexInParent, this.indexInParent);
                }
                return null;
            }

            @Override
            public void setBounds(Rectangle r) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setBounds(r);
                }
            }

            @Override
            public Dimension getSize() {
                Rectangle cellBounds = this.getBounds();
                if (cellBounds != null) {
                    return cellBounds.getSize();
                }
                return null;
            }

            @Override
            public void setSize(Dimension d) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setSize(d);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setSize(d);
                    }
                }
            }

            @Override
            public Accessible getAccessibleAt(Point p) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).getAccessibleAt(p);
                }
                return null;
            }

            @Override
            public boolean isFocusTraversable() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).isFocusTraversable();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isFocusTraversable();
                }
                return false;
            }

            @Override
            public void requestFocus() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).requestFocus();
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.requestFocus();
                    }
                }
            }

            @Override
            public void addFocusListener(FocusListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).addFocusListener(l);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.addFocusListener(l);
                    }
                }
            }

            @Override
            public void removeFocusListener(FocusListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).removeFocusListener(l);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.removeFocusListener(l);
                    }
                }
            }

            @Override
            public AccessibleIcon[] getAccessibleIcon() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleIcon();
                }
                return null;
            }

            @Override
            public AccessibleAction getAccessibleAction() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac == null) {
                    return null;
                }
                AccessibleAction aa = ac.getAccessibleAction();
                if (aa != null) {
                    return aa;
                }
                return this;
            }

            @Override
            public boolean doAccessibleAction(int i) {
                if (i == 0) {
                    JList.this.setSelectedIndex(this.indexInParent);
                    return true;
                }
                return false;
            }

            @Override
            public String getAccessibleActionDescription(int i) {
                if (i == 0) {
                    return UIManager.getString("AbstractButton.clickText");
                }
                return null;
            }

            @Override
            public int getAccessibleActionCount() {
                return 1;
            }
        }
    }
}

