/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleExtendedTable;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleTable;
import javax.accessibility.AccessibleTableModelChange;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.Scrollable;
import javax.swing.SizeSequence;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.TablePrintable;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import sun.awt.AWTAccessor;
import sun.reflect.misc.ReflectUtil;
import sun.swing.PrintingStatus;
import sun.swing.SwingUtilities2;

@JavaBean(defaultProperty="UI", description="A component which displays data in a two dimensional grid.")
@SwingContainer(value=false)
public class JTable
extends JComponent
implements TableModelListener,
Scrollable,
TableColumnModelListener,
ListSelectionListener,
CellEditorListener,
Accessible,
RowSorterListener {
    private static final String uiClassID = "TableUI";
    public static final int AUTO_RESIZE_OFF = 0;
    public static final int AUTO_RESIZE_NEXT_COLUMN = 1;
    public static final int AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2;
    public static final int AUTO_RESIZE_LAST_COLUMN = 3;
    public static final int AUTO_RESIZE_ALL_COLUMNS = 4;
    protected TableModel dataModel;
    protected TableColumnModel columnModel;
    protected ListSelectionModel selectionModel;
    protected JTableHeader tableHeader;
    protected int rowHeight;
    protected int rowMargin;
    protected Color gridColor;
    protected boolean showHorizontalLines;
    protected boolean showVerticalLines;
    protected int autoResizeMode;
    protected boolean autoCreateColumnsFromModel;
    protected Dimension preferredViewportSize;
    protected boolean rowSelectionAllowed;
    protected boolean cellSelectionEnabled;
    protected transient Component editorComp;
    protected transient TableCellEditor cellEditor;
    protected transient int editingColumn;
    protected transient int editingRow;
    protected transient Hashtable<Object, Object> defaultRenderersByColumnClass;
    protected transient Hashtable<Object, Object> defaultEditorsByColumnClass;
    protected Color selectionForeground;
    protected Color selectionBackground;
    private SizeSequence rowModel;
    private boolean dragEnabled;
    private boolean surrendersFocusOnKeystroke;
    private PropertyChangeListener editorRemover = null;
    private boolean columnSelectionAdjusting;
    private boolean rowSelectionAdjusting;
    private Throwable printError;
    private boolean isRowHeightSet;
    private boolean updateSelectionOnSort;
    private transient SortManager sortManager;
    private boolean ignoreSortChange;
    private boolean sorterChanged;
    private boolean autoCreateRowSorter;
    private boolean fillsViewportHeight;
    private DropMode dropMode = DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    private transient boolean updateInProgress;

    public JTable() {
        this(null, null, null);
    }

    public JTable(TableModel dm) {
        this(dm, null, null);
    }

    public JTable(TableModel dm, TableColumnModel cm) {
        this(dm, cm, null);
    }

    public JTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        this.setLayout(null);
        this.setFocusTraversalKeys(0, JComponent.getManagingFocusForwardTraversalKeys());
        this.setFocusTraversalKeys(1, JComponent.getManagingFocusBackwardTraversalKeys());
        if (cm == null) {
            cm = this.createDefaultColumnModel();
            this.autoCreateColumnsFromModel = true;
        }
        this.setColumnModel(cm);
        if (sm == null) {
            sm = this.createDefaultSelectionModel();
        }
        this.setSelectionModel(sm);
        if (dm == null) {
            dm = this.createDefaultDataModel();
        }
        this.setModel(dm);
        this.initializeLocalVars();
        this.updateUI();
    }

    public JTable(int numRows, int numColumns) {
        this(new DefaultTableModel(numRows, numColumns));
    }

    public JTable(Vector<? extends Vector> rowData, Vector<?> columnNames) {
        this(new DefaultTableModel(rowData, columnNames));
    }

    public JTable(final Object[][] rowData, final Object[] columnNames) {
        this(new AbstractTableModel(){

            @Override
            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            @Override
            public int getRowCount() {
                return rowData.length;
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public Object getValueAt(int row, int col) {
                return rowData[row][col];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                rowData[row][col] = value;
                this.fireTableCellUpdated(row, col);
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.configureEnclosingScrollPane();
    }

    protected void configureEnclosingScrollPane() {
        JViewport port;
        Container gp;
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport && (gp = (port = (JViewport)parent).getParent()) instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)gp;
            JViewport viewport = scrollPane.getViewport();
            if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                return;
            }
            scrollPane.setColumnHeaderView(this.getTableHeader());
            this.configureEnclosingScrollPaneUI();
        }
    }

    private void configureEnclosingScrollPaneUI() {
        JViewport port;
        Container gp;
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport && (gp = (port = (JViewport)parent).getParent()) instanceof JScrollPane) {
            Component corner;
            Border scrollPaneBorder;
            JScrollPane scrollPane = (JScrollPane)gp;
            JViewport viewport = scrollPane.getViewport();
            if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                return;
            }
            Border border = scrollPane.getBorder();
            if ((border == null || border instanceof UIResource) && (scrollPaneBorder = UIManager.getBorder("Table.scrollPaneBorder")) != null) {
                scrollPane.setBorder(scrollPaneBorder);
            }
            if ((corner = scrollPane.getCorner("UPPER_TRAILING_CORNER")) == null || corner instanceof UIResource) {
                corner = null;
                try {
                    corner = (Component)UIManager.get("Table.scrollPaneCornerComponent");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                scrollPane.setCorner("UPPER_TRAILING_CORNER", corner);
            }
        }
    }

    @Override
    public void removeNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner", this.editorRemover);
        this.editorRemover = null;
        this.unconfigureEnclosingScrollPane();
        super.removeNotify();
    }

    protected void unconfigureEnclosingScrollPane() {
        JViewport port;
        Container gp;
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport && (gp = (port = (JViewport)parent).getParent()) instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)gp;
            JViewport viewport = scrollPane.getViewport();
            if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                return;
            }
            scrollPane.setColumnHeaderView(null);
            Component corner = scrollPane.getCorner("UPPER_TRAILING_CORNER");
            if (corner instanceof UIResource) {
                scrollPane.setCorner("UPPER_TRAILING_CORNER", null);
            }
        }
    }

    @Override
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == "rowHeight") {
            if (!this.isRowHeightSet) {
                this.setRowHeight(((Number)value).intValue());
                this.isRowHeightSet = false;
            }
            return;
        }
        super.setUIProperty(propertyName, value);
    }

    @Deprecated
    public static JScrollPane createScrollPaneForTable(JTable aTable) {
        return new JScrollPane(aTable);
    }

    @BeanProperty(description="The JTableHeader instance which renders the column headers.")
    public void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != tableHeader) {
            JTableHeader old = this.tableHeader;
            if (old != null) {
                old.setTable(null);
            }
            this.tableHeader = tableHeader;
            if (tableHeader != null) {
                tableHeader.setTable(this);
            }
            this.firePropertyChange("tableHeader", old, tableHeader);
        }
    }

    public JTableHeader getTableHeader() {
        return this.tableHeader;
    }

    @BeanProperty(description="The height of the specified row.")
    public void setRowHeight(int rowHeight) {
        if (rowHeight <= 0) {
            throw new IllegalArgumentException("New row height less than 1");
        }
        int old = this.rowHeight;
        this.rowHeight = rowHeight;
        this.rowModel = null;
        if (this.sortManager != null) {
            this.sortManager.modelRowSizes = null;
        }
        this.isRowHeightSet = true;
        this.resizeAndRepaint();
        this.firePropertyChange("rowHeight", old, rowHeight);
    }

    public int getRowHeight() {
        return this.rowHeight;
    }

    private SizeSequence getRowModel() {
        if (this.rowModel == null) {
            this.rowModel = new SizeSequence(this.getRowCount(), this.getRowHeight());
        }
        return this.rowModel;
    }

    @BeanProperty(description="The height in pixels of the cells in <code>row</code>")
    public void setRowHeight(int row, int rowHeight) {
        if (rowHeight <= 0) {
            throw new IllegalArgumentException("New row height less than 1");
        }
        this.getRowModel().setSize(row, rowHeight);
        if (this.sortManager != null) {
            this.sortManager.setViewRowHeight(row, rowHeight);
        }
        this.resizeAndRepaint();
    }

    public int getRowHeight(int row) {
        return this.rowModel == null ? this.getRowHeight() : this.rowModel.getSize(row);
    }

    @BeanProperty(description="The amount of space between cells.")
    public void setRowMargin(int rowMargin) {
        int old = this.rowMargin;
        this.rowMargin = rowMargin;
        this.resizeAndRepaint();
        this.firePropertyChange("rowMargin", old, rowMargin);
    }

    public int getRowMargin() {
        return this.rowMargin;
    }

    @BeanProperty(bound=false, description="The spacing between the cells, drawn in the background color of the JTable.")
    public void setIntercellSpacing(Dimension intercellSpacing) {
        this.setRowMargin(intercellSpacing.height);
        this.getColumnModel().setColumnMargin(intercellSpacing.width);
        this.resizeAndRepaint();
    }

    public Dimension getIntercellSpacing() {
        return new Dimension(this.getColumnModel().getColumnMargin(), this.rowMargin);
    }

    @BeanProperty(description="The grid color.")
    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("New color is null");
        }
        Color old = this.gridColor;
        this.gridColor = gridColor;
        this.firePropertyChange("gridColor", old, gridColor);
        this.repaint();
    }

    public Color getGridColor() {
        return this.gridColor;
    }

    @BeanProperty(description="The color used to draw the grid lines.")
    public void setShowGrid(boolean showGrid) {
        this.setShowHorizontalLines(showGrid);
        this.setShowVerticalLines(showGrid);
        this.repaint();
    }

    @BeanProperty(description="Whether horizontal lines should be drawn in between the cells.")
    public void setShowHorizontalLines(boolean showHorizontalLines) {
        boolean old = this.showHorizontalLines;
        this.showHorizontalLines = showHorizontalLines;
        this.firePropertyChange("showHorizontalLines", old, showHorizontalLines);
        this.repaint();
    }

    @BeanProperty(description="Whether vertical lines should be drawn in between the cells.")
    public void setShowVerticalLines(boolean showVerticalLines) {
        boolean old = this.showVerticalLines;
        this.showVerticalLines = showVerticalLines;
        this.firePropertyChange("showVerticalLines", old, showVerticalLines);
        this.repaint();
    }

    public boolean getShowHorizontalLines() {
        return this.showHorizontalLines;
    }

    public boolean getShowVerticalLines() {
        return this.showVerticalLines;
    }

    @BeanProperty(enumerationValues={"JTable.AUTO_RESIZE_OFF", "JTable.AUTO_RESIZE_NEXT_COLUMN", "JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS", "JTable.AUTO_RESIZE_LAST_COLUMN", "JTable.AUTO_RESIZE_ALL_COLUMNS"}, description="Whether the columns should adjust themselves automatically.")
    public void setAutoResizeMode(int mode) {
        if (JTable.isValidAutoResizeMode(mode)) {
            int old = this.autoResizeMode;
            this.autoResizeMode = mode;
            this.resizeAndRepaint();
            if (this.tableHeader != null) {
                this.tableHeader.resizeAndRepaint();
            }
            this.firePropertyChange("autoResizeMode", old, this.autoResizeMode);
        }
    }

    private static boolean isValidAutoResizeMode(int mode) {
        return mode == 0 || mode == 1 || mode == 2 || mode == 3 || mode == 4;
    }

    public int getAutoResizeMode() {
        return this.autoResizeMode;
    }

    @BeanProperty(description="Automatically populates the columnModel when a new TableModel is submitted.")
    public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
        if (this.autoCreateColumnsFromModel != autoCreateColumnsFromModel) {
            boolean old = this.autoCreateColumnsFromModel;
            this.autoCreateColumnsFromModel = autoCreateColumnsFromModel;
            if (autoCreateColumnsFromModel) {
                this.createDefaultColumnsFromModel();
            }
            this.firePropertyChange("autoCreateColumnsFromModel", old, autoCreateColumnsFromModel);
        }
    }

    public boolean getAutoCreateColumnsFromModel() {
        return this.autoCreateColumnsFromModel;
    }

    public void createDefaultColumnsFromModel() {
        TableModel m = this.getModel();
        if (m != null) {
            TableColumnModel cm = this.getColumnModel();
            while (cm.getColumnCount() > 0) {
                cm.removeColumn(cm.getColumn(0));
            }
            for (int i = 0; i < m.getColumnCount(); ++i) {
                TableColumn newColumn = new TableColumn(i);
                this.addColumn(newColumn);
            }
        }
    }

    public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {
        if (renderer != null) {
            this.defaultRenderersByColumnClass.put(columnClass, renderer);
        } else {
            this.defaultRenderersByColumnClass.remove(columnClass);
        }
    }

    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        Object renderer;
        if (columnClass == null) {
            return null;
        }
        if (this.defaultRenderersByColumnClass == null) {
            this.createDefaultRenderers();
        }
        if ((renderer = this.defaultRenderersByColumnClass.get(columnClass)) != null) {
            return (TableCellRenderer)renderer;
        }
        Class<Object> c = columnClass.getSuperclass();
        if (c == null && columnClass != Object.class) {
            c = Object.class;
        }
        return this.getDefaultRenderer(c);
    }

    public void setDefaultEditor(Class<?> columnClass, TableCellEditor editor) {
        if (editor != null) {
            this.defaultEditorsByColumnClass.put(columnClass, editor);
        } else {
            this.defaultEditorsByColumnClass.remove(columnClass);
        }
    }

    public TableCellEditor getDefaultEditor(Class<?> columnClass) {
        Object editor;
        if (columnClass == null) {
            return null;
        }
        if (this.defaultEditorsByColumnClass == null) {
            this.createDefaultEditors();
        }
        if ((editor = this.defaultEditorsByColumnClass.get(columnClass)) != null) {
            return (TableCellEditor)editor;
        }
        return this.getDefaultEditor(columnClass.getSuperclass());
    }

    @BeanProperty(bound=false, description="determines whether automatic drag handling is enabled")
    public void setDragEnabled(boolean b) {
        this.checkDragEnabled(b);
        this.dragEnabled = b;
    }

    private void checkDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    public boolean getDragEnabled() {
        return this.dragEnabled;
    }

    public final void setDropMode(DropMode dropMode) {
        JTable.checkDropMode(dropMode);
        this.dropMode = dropMode;
    }

    private static void checkDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case USE_SELECTION: 
                case ON: 
                case INSERT: 
                case INSERT_ROWS: 
                case INSERT_COLS: 
                case ON_OR_INSERT: 
                case ON_OR_INSERT_ROWS: 
                case ON_OR_INSERT_COLS: {
                    return;
                }
            }
        }
        throw new IllegalArgumentException(String.valueOf((Object)dropMode) + ": Unsupported drop mode for table");
    }

    public final DropMode getDropMode() {
        return this.dropMode;
    }

    @Override
    DropLocation dropLocationForPoint(Point p) {
        DropLocation location = null;
        int row = this.rowAtPoint(p);
        int col = this.columnAtPoint(p);
        boolean outside = Boolean.TRUE == this.getClientProperty("Table.isFileList") && SwingUtilities2.pointOutsidePrefSize(this, row, col, p);
        Rectangle rect = this.getCellRect(row, col, true);
        boolean between = false;
        boolean ltr = this.getComponentOrientation().isLeftToRight();
        switch (this.dropMode) {
            case USE_SELECTION: 
            case ON: {
                if (row == -1 || col == -1 || outside) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                location = new DropLocation(p, row, col, false, false);
                break;
            }
            case INSERT: {
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, 0, 0, true, true);
                    break;
                }
                SwingUtilities2.Section xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (row == -1) {
                    if (xSection == SwingUtilities2.Section.LEADING) {
                        location = new DropLocation(p, this.getRowCount(), col, true, true);
                        break;
                    }
                    if (xSection == SwingUtilities2.Section.TRAILING) {
                        location = new DropLocation(p, this.getRowCount(), col + 1, true, true);
                        break;
                    }
                    location = new DropLocation(p, this.getRowCount(), col, true, false);
                    break;
                }
                if (xSection == SwingUtilities2.Section.LEADING || xSection == SwingUtilities2.Section.TRAILING) {
                    SwingUtilities2.Section ySection = SwingUtilities2.liesInVertical(rect, p, true);
                    if (ySection == SwingUtilities2.Section.LEADING) {
                        between = true;
                    } else if (ySection == SwingUtilities2.Section.TRAILING) {
                        ++row;
                        between = true;
                    }
                    location = new DropLocation(p, row, xSection == SwingUtilities2.Section.TRAILING ? col + 1 : col, between, true);
                    break;
                }
                if (SwingUtilities2.liesInVertical(rect, p, false) == SwingUtilities2.Section.TRAILING) {
                    ++row;
                }
                location = new DropLocation(p, row, col, true, false);
                break;
            }
            case INSERT_ROWS: {
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (row == -1) {
                    location = new DropLocation(p, this.getRowCount(), col, true, false);
                    break;
                }
                if (SwingUtilities2.liesInVertical(rect, p, false) == SwingUtilities2.Section.TRAILING) {
                    ++row;
                }
                location = new DropLocation(p, row, col, true, false);
                break;
            }
            case ON_OR_INSERT_ROWS: {
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (row == -1) {
                    location = new DropLocation(p, this.getRowCount(), col, true, false);
                    break;
                }
                SwingUtilities2.Section ySection = SwingUtilities2.liesInVertical(rect, p, true);
                if (ySection == SwingUtilities2.Section.LEADING) {
                    between = true;
                } else if (ySection == SwingUtilities2.Section.TRAILING) {
                    ++row;
                    between = true;
                }
                location = new DropLocation(p, row, col, between, false);
                break;
            }
            case INSERT_COLS: {
                if (row == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (col == -1) {
                    location = new DropLocation(p, this.getColumnCount(), col, false, true);
                    break;
                }
                if (SwingUtilities2.liesInHorizontal(rect, p, ltr, false) == SwingUtilities2.Section.TRAILING) {
                    ++col;
                }
                location = new DropLocation(p, row, col, false, true);
                break;
            }
            case ON_OR_INSERT_COLS: {
                if (row == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (col == -1) {
                    location = new DropLocation(p, row, this.getColumnCount(), false, true);
                    break;
                }
                SwingUtilities2.Section xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (xSection == SwingUtilities2.Section.LEADING) {
                    between = true;
                } else if (xSection == SwingUtilities2.Section.TRAILING) {
                    ++col;
                    between = true;
                }
                location = new DropLocation(p, row, col, false, between);
                break;
            }
            case ON_OR_INSERT: {
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, 0, 0, true, true);
                    break;
                }
                SwingUtilities2.Section xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (row == -1) {
                    if (xSection == SwingUtilities2.Section.LEADING) {
                        location = new DropLocation(p, this.getRowCount(), col, true, true);
                        break;
                    }
                    if (xSection == SwingUtilities2.Section.TRAILING) {
                        location = new DropLocation(p, this.getRowCount(), col + 1, true, true);
                        break;
                    }
                    location = new DropLocation(p, this.getRowCount(), col, true, false);
                    break;
                }
                SwingUtilities2.Section ySection = SwingUtilities2.liesInVertical(rect, p, true);
                if (ySection == SwingUtilities2.Section.LEADING) {
                    between = true;
                } else if (ySection == SwingUtilities2.Section.TRAILING) {
                    ++row;
                    between = true;
                }
                location = new DropLocation(p, row, xSection == SwingUtilities2.Section.TRAILING ? col + 1 : col, between, xSection != SwingUtilities2.Section.MIDDLE);
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
        DropLocation tableLocation = (DropLocation)location;
        if (this.dropMode == DropMode.USE_SELECTION) {
            if (tableLocation == null) {
                if (!forDrop && state != null) {
                    this.clearSelection();
                    int[] rows = ((int[][])state)[0];
                    int[] cols = ((int[][])state)[1];
                    int[] anchleads = ((int[][])state)[2];
                    for (int row : rows) {
                        this.addRowSelectionInterval(row, row);
                    }
                    for (int col : cols) {
                        this.addColumnSelectionInterval(col, col);
                    }
                    SwingUtilities2.setLeadAnchorWithoutSelection(this.getSelectionModel(), anchleads[1], anchleads[0]);
                    SwingUtilities2.setLeadAnchorWithoutSelection(this.getColumnModel().getSelectionModel(), anchleads[3], anchleads[2]);
                }
            } else {
                retVal = this.dropLocation == null ? (Object)new int[][]{this.getSelectedRows(), this.getSelectedColumns(), {this.getAdjustedIndex(this.getSelectionModel().getAnchorSelectionIndex(), true), this.getAdjustedIndex(this.getSelectionModel().getLeadSelectionIndex(), true), this.getAdjustedIndex(this.getColumnModel().getSelectionModel().getAnchorSelectionIndex(), false), this.getAdjustedIndex(this.getColumnModel().getSelectionModel().getLeadSelectionIndex(), false)}} : state;
                if (tableLocation.getRow() == -1) {
                    this.clearSelectionAndLeadAnchor();
                } else {
                    this.setRowSelectionInterval(tableLocation.getRow(), tableLocation.getRow());
                    this.setColumnSelectionInterval(tableLocation.getColumn(), tableLocation.getColumn());
                }
            }
        }
        DropLocation old = this.dropLocation;
        this.dropLocation = tableLocation;
        this.firePropertyChange("dropLocation", old, this.dropLocation);
        return retVal;
    }

    @BeanProperty(bound=false)
    public final DropLocation getDropLocation() {
        return this.dropLocation;
    }

    @BeanProperty(preferred=true, description="Whether or not to turn on sorting by default.")
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        boolean oldValue = this.autoCreateRowSorter;
        this.autoCreateRowSorter = autoCreateRowSorter;
        if (autoCreateRowSorter) {
            this.setRowSorter(new TableRowSorter<TableModel>(this.getModel()));
        }
        this.firePropertyChange("autoCreateRowSorter", oldValue, autoCreateRowSorter);
    }

    public boolean getAutoCreateRowSorter() {
        return this.autoCreateRowSorter;
    }

    @BeanProperty(expert=true, description="Whether or not to update the selection on sorting")
    public void setUpdateSelectionOnSort(boolean update) {
        if (this.updateSelectionOnSort != update) {
            this.updateSelectionOnSort = update;
            this.firePropertyChange("updateSelectionOnSort", !update, update);
        }
    }

    public boolean getUpdateSelectionOnSort() {
        return this.updateSelectionOnSort;
    }

    @BeanProperty(description="The table's RowSorter")
    public void setRowSorter(RowSorter<? extends TableModel> sorter) {
        RowSorter<? extends TableModel> oldRowSorter = null;
        if (this.sortManager != null) {
            oldRowSorter = this.sortManager.sorter;
            this.sortManager.dispose();
            this.sortManager = null;
        }
        this.rowModel = null;
        this.clearSelectionAndLeadAnchor();
        if (sorter != null) {
            this.sortManager = new SortManager(sorter);
        }
        this.resizeAndRepaint();
        this.firePropertyChange("rowSorter", oldRowSorter, sorter);
        this.firePropertyChange("sorter", oldRowSorter, sorter);
    }

    public RowSorter<? extends TableModel> getRowSorter() {
        return this.sortManager != null ? this.sortManager.sorter : null;
    }

    @BeanProperty(enumerationValues={"ListSelectionModel.SINGLE_SELECTION", "ListSelectionModel.SINGLE_INTERVAL_SELECTION", "ListSelectionModel.MULTIPLE_INTERVAL_SELECTION"}, description="The selection mode used by the row and column selection models.")
    public void setSelectionMode(int selectionMode) {
        this.clearSelection();
        this.getSelectionModel().setSelectionMode(selectionMode);
        this.getColumnModel().getSelectionModel().setSelectionMode(selectionMode);
    }

    @BeanProperty(visualUpdate=true, description="If true, an entire row is selected for each selected cell.")
    public void setRowSelectionAllowed(boolean rowSelectionAllowed) {
        boolean old = this.rowSelectionAllowed;
        this.rowSelectionAllowed = rowSelectionAllowed;
        if (old != rowSelectionAllowed) {
            this.repaint();
        }
        this.firePropertyChange("rowSelectionAllowed", old, rowSelectionAllowed);
    }

    public boolean getRowSelectionAllowed() {
        return this.rowSelectionAllowed;
    }

    @BeanProperty(visualUpdate=true, description="If true, an entire column is selected for each selected cell.")
    public void setColumnSelectionAllowed(boolean columnSelectionAllowed) {
        boolean old = this.columnModel.getColumnSelectionAllowed();
        this.columnModel.setColumnSelectionAllowed(columnSelectionAllowed);
        if (old != columnSelectionAllowed) {
            this.repaint();
        }
        this.firePropertyChange("columnSelectionAllowed", old, columnSelectionAllowed);
    }

    public boolean getColumnSelectionAllowed() {
        return this.columnModel.getColumnSelectionAllowed();
    }

    @BeanProperty(visualUpdate=true, description="Select a rectangular region of cells rather than rows or columns.")
    public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
        this.setRowSelectionAllowed(cellSelectionEnabled);
        this.setColumnSelectionAllowed(cellSelectionEnabled);
        boolean old = this.cellSelectionEnabled;
        this.cellSelectionEnabled = cellSelectionEnabled;
        this.firePropertyChange("cellSelectionEnabled", old, cellSelectionEnabled);
    }

    public boolean getCellSelectionEnabled() {
        return this.getRowSelectionAllowed() && this.getColumnSelectionAllowed();
    }

    public void selectAll() {
        if (this.isEditing()) {
            this.removeEditor();
        }
        if (this.getRowCount() > 0 && this.getColumnCount() > 0) {
            ListSelectionModel selModel = this.selectionModel;
            selModel.setValueIsAdjusting(true);
            int oldLead = this.getAdjustedIndex(selModel.getLeadSelectionIndex(), true);
            int oldAnchor = this.getAdjustedIndex(selModel.getAnchorSelectionIndex(), true);
            this.setRowSelectionInterval(0, this.getRowCount() - 1);
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel, oldLead, oldAnchor);
            selModel.setValueIsAdjusting(false);
            selModel = this.columnModel.getSelectionModel();
            selModel.setValueIsAdjusting(true);
            oldLead = this.getAdjustedIndex(selModel.getLeadSelectionIndex(), false);
            oldAnchor = this.getAdjustedIndex(selModel.getAnchorSelectionIndex(), false);
            this.setColumnSelectionInterval(0, this.getColumnCount() - 1);
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel, oldLead, oldAnchor);
            selModel.setValueIsAdjusting(false);
        }
    }

    public void clearSelection() {
        this.selectionModel.clearSelection();
        this.columnModel.getSelectionModel().clearSelection();
    }

    private void clearSelectionAndLeadAnchor() {
        this.selectionModel.setValueIsAdjusting(true);
        this.columnModel.getSelectionModel().setValueIsAdjusting(true);
        this.clearSelection();
        this.selectionModel.setAnchorSelectionIndex(-1);
        this.selectionModel.setLeadSelectionIndex(-1);
        this.columnModel.getSelectionModel().setAnchorSelectionIndex(-1);
        this.columnModel.getSelectionModel().setLeadSelectionIndex(-1);
        this.selectionModel.setValueIsAdjusting(false);
        this.columnModel.getSelectionModel().setValueIsAdjusting(false);
    }

    private int getAdjustedIndex(int index, boolean row) {
        int compare = row ? this.getRowCount() : this.getColumnCount();
        return index < compare ? index : -1;
    }

    private int boundRow(int row) throws IllegalArgumentException {
        if (row < 0 || row >= this.getRowCount()) {
            throw new IllegalArgumentException("Row index out of range");
        }
        return row;
    }

    private int boundColumn(int col) {
        if (col < 0 || col >= this.getColumnCount()) {
            throw new IllegalArgumentException("Column index out of range");
        }
        return col;
    }

    public void setRowSelectionInterval(int index0, int index1) {
        this.selectionModel.setSelectionInterval(this.boundRow(index0), this.boundRow(index1));
    }

    public void setColumnSelectionInterval(int index0, int index1) {
        this.columnModel.getSelectionModel().setSelectionInterval(this.boundColumn(index0), this.boundColumn(index1));
    }

    public void addRowSelectionInterval(int index0, int index1) {
        this.selectionModel.addSelectionInterval(this.boundRow(index0), this.boundRow(index1));
    }

    public void addColumnSelectionInterval(int index0, int index1) {
        this.columnModel.getSelectionModel().addSelectionInterval(this.boundColumn(index0), this.boundColumn(index1));
    }

    public void removeRowSelectionInterval(int index0, int index1) {
        this.selectionModel.removeSelectionInterval(this.boundRow(index0), this.boundRow(index1));
    }

    public void removeColumnSelectionInterval(int index0, int index1) {
        this.columnModel.getSelectionModel().removeSelectionInterval(this.boundColumn(index0), this.boundColumn(index1));
    }

    @BeanProperty(bound=false)
    public int getSelectedRow() {
        return this.selectionModel.getMinSelectionIndex();
    }

    @BeanProperty(bound=false)
    public int getSelectedColumn() {
        return this.columnModel.getSelectionModel().getMinSelectionIndex();
    }

    @BeanProperty(bound=false)
    public int[] getSelectedRows() {
        return this.selectionModel.getSelectedIndices();
    }

    @BeanProperty(bound=false)
    public int[] getSelectedColumns() {
        return this.columnModel.getSelectedColumns();
    }

    @BeanProperty(bound=false)
    public int getSelectedRowCount() {
        return this.selectionModel.getSelectedItemsCount();
    }

    @BeanProperty(bound=false)
    public int getSelectedColumnCount() {
        return this.columnModel.getSelectedColumnCount();
    }

    public boolean isRowSelected(int row) {
        return this.selectionModel.isSelectedIndex(row);
    }

    public boolean isColumnSelected(int column) {
        return this.columnModel.getSelectionModel().isSelectedIndex(column);
    }

    public boolean isCellSelected(int row, int column) {
        if (!this.getRowSelectionAllowed() && !this.getColumnSelectionAllowed()) {
            return false;
        }
        return !(this.getRowSelectionAllowed() && !this.isRowSelected(row) || this.getColumnSelectionAllowed() && !this.isColumnSelected(column));
    }

    private void changeSelectionModel(ListSelectionModel sm, int index, boolean toggle, boolean extend, boolean selected, int anchor, boolean anchorSelected) {
        if (extend) {
            if (toggle) {
                if (anchorSelected) {
                    sm.addSelectionInterval(anchor, index);
                } else {
                    sm.removeSelectionInterval(anchor, index);
                    if (Boolean.TRUE == this.getClientProperty("Table.isFileList")) {
                        sm.addSelectionInterval(index, index);
                        sm.setAnchorSelectionIndex(anchor);
                    }
                }
            } else {
                sm.setSelectionInterval(anchor, index);
            }
        } else if (toggle) {
            if (selected) {
                sm.removeSelectionInterval(index, index);
            } else {
                sm.addSelectionInterval(index, index);
            }
        } else {
            sm.setSelectionInterval(index, index);
        }
    }

    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        Rectangle cellRect;
        ListSelectionModel rsm = this.getSelectionModel();
        ListSelectionModel csm = this.getColumnModel().getSelectionModel();
        int anchorRow = this.getAdjustedIndex(rsm.getAnchorSelectionIndex(), true);
        int anchorCol = this.getAdjustedIndex(csm.getAnchorSelectionIndex(), false);
        boolean anchorSelected = true;
        if (anchorRow == -1) {
            if (this.getRowCount() > 0) {
                anchorRow = 0;
            }
            anchorSelected = false;
        }
        if (anchorCol == -1) {
            if (this.getColumnCount() > 0) {
                anchorCol = 0;
            }
            anchorSelected = false;
        }
        boolean selected = this.isCellSelected(rowIndex, columnIndex);
        anchorSelected = anchorSelected && this.isCellSelected(anchorRow, anchorCol);
        this.changeSelectionModel(csm, columnIndex, toggle, extend, selected, anchorCol, anchorSelected);
        this.changeSelectionModel(rsm, rowIndex, toggle, extend, selected, anchorRow, anchorSelected);
        if (this.getAutoscrolls() && (cellRect = this.getCellRect(rowIndex, columnIndex, false)) != null) {
            this.scrollRectToVisible(cellRect);
        }
    }

    public Color getSelectionForeground() {
        return this.selectionForeground;
    }

    @BeanProperty(description="A default foreground color for selected cells.")
    public void setSelectionForeground(Color selectionForeground) {
        Color old = this.selectionForeground;
        this.selectionForeground = selectionForeground;
        this.firePropertyChange("selectionForeground", old, selectionForeground);
        this.repaint();
    }

    public Color getSelectionBackground() {
        return this.selectionBackground;
    }

    @BeanProperty(description="A default background color for selected cells.")
    public void setSelectionBackground(Color selectionBackground) {
        Color old = this.selectionBackground;
        this.selectionBackground = selectionBackground;
        this.firePropertyChange("selectionBackground", old, selectionBackground);
        this.repaint();
    }

    public TableColumn getColumn(Object identifier) {
        TableColumnModel cm = this.getColumnModel();
        int columnIndex = cm.getColumnIndex(identifier);
        return cm.getColumn(columnIndex);
    }

    public int convertColumnIndexToModel(int viewColumnIndex) {
        return SwingUtilities2.convertColumnIndexToModel(this.getColumnModel(), viewColumnIndex);
    }

    public int convertColumnIndexToView(int modelColumnIndex) {
        return SwingUtilities2.convertColumnIndexToView(this.getColumnModel(), modelColumnIndex);
    }

    public int convertRowIndexToView(int modelRowIndex) {
        RowSorter<? extends TableModel> sorter = this.getRowSorter();
        if (sorter != null) {
            return sorter.convertRowIndexToView(modelRowIndex);
        }
        return modelRowIndex;
    }

    public int convertRowIndexToModel(int viewRowIndex) {
        RowSorter<? extends TableModel> sorter = this.getRowSorter();
        if (sorter != null) {
            return sorter.convertRowIndexToModel(viewRowIndex);
        }
        return viewRowIndex;
    }

    @BeanProperty(bound=false)
    public int getRowCount() {
        RowSorter<? extends TableModel> sorter = this.getRowSorter();
        if (sorter != null) {
            return sorter.getViewRowCount();
        }
        return this.getModel().getRowCount();
    }

    @BeanProperty(bound=false)
    public int getColumnCount() {
        return this.getColumnModel().getColumnCount();
    }

    public String getColumnName(int column) {
        return this.getModel().getColumnName(this.convertColumnIndexToModel(column));
    }

    public Class<?> getColumnClass(int column) {
        return this.getModel().getColumnClass(this.convertColumnIndexToModel(column));
    }

    public Object getValueAt(int row, int column) {
        return this.getModel().getValueAt(this.convertRowIndexToModel(row), this.convertColumnIndexToModel(column));
    }

    public void setValueAt(Object aValue, int row, int column) {
        this.getModel().setValueAt(aValue, this.convertRowIndexToModel(row), this.convertColumnIndexToModel(column));
    }

    public boolean isCellEditable(int row, int column) {
        return this.getModel().isCellEditable(this.convertRowIndexToModel(row), this.convertColumnIndexToModel(column));
    }

    public void addColumn(TableColumn aColumn) {
        if (aColumn.getHeaderValue() == null) {
            int modelColumn = aColumn.getModelIndex();
            String columnName = this.getModel().getColumnName(modelColumn);
            aColumn.setHeaderValue(columnName);
        }
        this.getColumnModel().addColumn(aColumn);
    }

    public void removeColumn(TableColumn aColumn) {
        this.getColumnModel().removeColumn(aColumn);
    }

    public void moveColumn(int column, int targetColumn) {
        this.getColumnModel().moveColumn(column, targetColumn);
    }

    public int columnAtPoint(Point point) {
        int x = point.x;
        if (!this.getComponentOrientation().isLeftToRight()) {
            x = this.getWidth() - x - 1;
        }
        return this.getColumnModel().getColumnIndexAtX(x);
    }

    public int rowAtPoint(Point point) {
        int result;
        int y = point.y;
        int n = result = this.rowModel == null ? y / this.getRowHeight() : this.rowModel.getIndex(y);
        if (result < 0) {
            return -1;
        }
        if (result >= this.getRowCount()) {
            return -1;
        }
        return result;
    }

    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        Rectangle r = new Rectangle();
        boolean valid = true;
        if (row < 0) {
            valid = false;
        } else if (row >= this.getRowCount()) {
            r.y = this.getHeight();
            valid = false;
        } else {
            r.height = this.getRowHeight(row);
            int n = r.y = this.rowModel == null ? row * r.height : this.rowModel.getPosition(row);
        }
        if (column < 0) {
            if (!this.getComponentOrientation().isLeftToRight()) {
                r.x = this.getWidth();
            }
            valid = false;
        } else if (column >= this.getColumnCount()) {
            if (this.getComponentOrientation().isLeftToRight()) {
                r.x = this.getWidth();
            }
            valid = false;
        } else {
            TableColumnModel cm = this.getColumnModel();
            if (this.getComponentOrientation().isLeftToRight()) {
                for (i = 0; i < column; ++i) {
                    r.x += cm.getColumn(i).getWidth();
                }
            } else {
                for (i = cm.getColumnCount() - 1; i > column; --i) {
                    r.x += cm.getColumn(i).getWidth();
                }
            }
            r.width = cm.getColumn(column).getWidth();
        }
        if (valid && !includeSpacing) {
            int rm = Math.min(this.getRowMargin(), r.height);
            int cm = Math.min(this.getColumnModel().getColumnMargin(), r.width);
            r.setBounds(r.x + cm / 2, r.y + rm / 2, r.width - cm, r.height - rm);
        }
        return r;
    }

    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = this.getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); ++column) {
            if (cm.getColumn(column) != aColumn) continue;
            return column;
        }
        return -1;
    }

    @Override
    public void doLayout() {
        TableColumn resizingColumn = this.getResizingColumn();
        if (resizingColumn == null) {
            this.setWidthsFromPreferredWidths(false);
        } else {
            int columnIndex = this.viewIndexForColumn(resizingColumn);
            int delta = this.getWidth() - this.getColumnModel().getTotalColumnWidth();
            this.accommodateDelta(columnIndex, delta);
            delta = this.getWidth() - this.getColumnModel().getTotalColumnWidth();
            if (delta != 0) {
                resizingColumn.setWidth(resizingColumn.getWidth() + delta);
            }
            this.setWidthsFromPreferredWidths(true);
        }
        super.doLayout();
    }

    private TableColumn getResizingColumn() {
        return this.tableHeader == null ? null : this.tableHeader.getResizingColumn();
    }

    @Deprecated
    public void sizeColumnsToFit(boolean lastColumnOnly) {
        int oldAutoResizeMode = this.autoResizeMode;
        this.setAutoResizeMode(lastColumnOnly ? 3 : 4);
        this.sizeColumnsToFit(-1);
        this.setAutoResizeMode(oldAutoResizeMode);
    }

    public void sizeColumnsToFit(int resizingColumn) {
        if (resizingColumn == -1) {
            this.setWidthsFromPreferredWidths(false);
        } else if (this.autoResizeMode == 0) {
            TableColumn aColumn = this.getColumnModel().getColumn(resizingColumn);
            aColumn.setPreferredWidth(aColumn.getWidth());
        } else {
            int delta = this.getWidth() - this.getColumnModel().getTotalColumnWidth();
            this.accommodateDelta(resizingColumn, delta);
            this.setWidthsFromPreferredWidths(true);
        }
    }

    private void setWidthsFromPreferredWidths(final boolean inverse) {
        int totalWidth = this.getWidth();
        int totalPreferred = this.getPreferredSize().width;
        int target = !inverse ? totalWidth : totalPreferred;
        final TableColumnModel cm = this.columnModel;
        Resizable3 r = new Resizable3(){

            @Override
            public int getElementCount() {
                return cm.getColumnCount();
            }

            @Override
            public int getLowerBoundAt(int i) {
                return cm.getColumn(i).getMinWidth();
            }

            @Override
            public int getUpperBoundAt(int i) {
                return cm.getColumn(i).getMaxWidth();
            }

            @Override
            public int getMidPointAt(int i) {
                if (!inverse) {
                    return cm.getColumn(i).getPreferredWidth();
                }
                return cm.getColumn(i).getWidth();
            }

            @Override
            public void setSizeAt(int s, int i) {
                if (!inverse) {
                    cm.getColumn(i).setWidth(s);
                } else {
                    cm.getColumn(i).setPreferredWidth(s);
                }
            }
        };
        this.adjustSizes((long)target, r, inverse);
    }

    private void accommodateDelta(int resizingColumnIndex, int delta) {
        int to;
        int columnCount = this.getColumnCount();
        int from = resizingColumnIndex;
        switch (this.autoResizeMode) {
            case 1: {
                to = Math.min(++from + 1, columnCount);
                break;
            }
            case 2: {
                ++from;
                to = columnCount;
                break;
            }
            case 3: {
                from = columnCount - 1;
                to = from + 1;
                break;
            }
            case 4: {
                from = 0;
                to = columnCount;
                break;
            }
            default: {
                return;
            }
        }
        final int start = from;
        final int end = to;
        final TableColumnModel cm = this.columnModel;
        Resizable3 r = new Resizable3(){

            @Override
            public int getElementCount() {
                return end - start;
            }

            @Override
            public int getLowerBoundAt(int i) {
                return cm.getColumn(i + start).getMinWidth();
            }

            @Override
            public int getUpperBoundAt(int i) {
                return cm.getColumn(i + start).getMaxWidth();
            }

            @Override
            public int getMidPointAt(int i) {
                return cm.getColumn(i + start).getWidth();
            }

            @Override
            public void setSizeAt(int s, int i) {
                cm.getColumn(i + start).setWidth(s);
            }
        };
        int totalWidth = 0;
        for (int i = from; i < to; ++i) {
            TableColumn aColumn = this.columnModel.getColumn(i);
            int input = aColumn.getWidth();
            totalWidth += input;
        }
        this.adjustSizes((long)(totalWidth + delta), r, false);
    }

    private void adjustSizes(long target, final Resizable3 r, boolean inverse) {
        int N = r.getElementCount();
        long totalPreferred = 0L;
        for (int i = 0; i < N; ++i) {
            totalPreferred += (long)r.getMidPointAt(i);
        }
        Resizable2 s = target < totalPreferred == !inverse ? new Resizable2(){

            @Override
            public int getElementCount() {
                return r.getElementCount();
            }

            @Override
            public int getLowerBoundAt(int i) {
                return r.getLowerBoundAt(i);
            }

            @Override
            public int getUpperBoundAt(int i) {
                return r.getMidPointAt(i);
            }

            @Override
            public void setSizeAt(int newSize, int i) {
                r.setSizeAt(newSize, i);
            }
        } : new Resizable2(){

            @Override
            public int getElementCount() {
                return r.getElementCount();
            }

            @Override
            public int getLowerBoundAt(int i) {
                return r.getMidPointAt(i);
            }

            @Override
            public int getUpperBoundAt(int i) {
                return r.getUpperBoundAt(i);
            }

            @Override
            public void setSizeAt(int newSize, int i) {
                r.setSizeAt(newSize, i);
            }
        };
        this.adjustSizes(target, s, !inverse);
    }

    private void adjustSizes(long target, Resizable2 r, boolean limitToRange) {
        int i;
        long totalLowerBound = 0L;
        long totalUpperBound = 0L;
        for (i = 0; i < r.getElementCount(); ++i) {
            totalLowerBound += (long)r.getLowerBoundAt(i);
            totalUpperBound += (long)r.getUpperBoundAt(i);
        }
        if (limitToRange) {
            target = Math.min(Math.max(totalLowerBound, target), totalUpperBound);
        }
        for (i = 0; i < r.getElementCount(); ++i) {
            int newSize;
            int lowerBound = r.getLowerBoundAt(i);
            int upperBound = r.getUpperBoundAt(i);
            if (totalLowerBound == totalUpperBound) {
                newSize = lowerBound;
            } else {
                double f = (double)(target - totalLowerBound) / (double)(totalUpperBound - totalLowerBound);
                newSize = (int)Math.round((double)lowerBound + f * (double)(upperBound - lowerBound));
            }
            r.setSizeAt(newSize, i);
            target -= (long)newSize;
            totalLowerBound -= (long)lowerBound;
            totalUpperBound -= (long)upperBound;
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        TableCellRenderer renderer;
        Component component;
        String tip = null;
        Point p = event.getPoint();
        int hitColumnIndex = this.columnAtPoint(p);
        int hitRowIndex = this.rowAtPoint(p);
        if (hitColumnIndex != -1 && hitRowIndex != -1 && (component = this.prepareRenderer(renderer = this.getCellRenderer(hitRowIndex, hitColumnIndex), hitRowIndex, hitColumnIndex)) instanceof JComponent) {
            Rectangle cellRect = this.getCellRect(hitRowIndex, hitColumnIndex, false);
            p.translate(-cellRect.x, -cellRect.y);
            int modifiers = event.getModifiers();
            MouseEvent newEvent = new MouseEvent(component, event.getID(), event.getWhen(), modifiers, p.x, p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(), 0);
            AWTAccessor.MouseEventAccessor meAccessor = AWTAccessor.getMouseEventAccessor();
            meAccessor.setCausedByTouchEvent(newEvent, meAccessor.isCausedByTouchEvent(event));
            tip = ((JComponent)component).getToolTipText(newEvent);
        }
        if (tip == null) {
            tip = this.getToolTipText();
        }
        return tip;
    }

    public void setSurrendersFocusOnKeystroke(boolean surrendersFocusOnKeystroke) {
        this.surrendersFocusOnKeystroke = surrendersFocusOnKeystroke;
    }

    public boolean getSurrendersFocusOnKeystroke() {
        return this.surrendersFocusOnKeystroke;
    }

    public boolean editCellAt(int row, int column) {
        return this.editCellAt(row, column, null);
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        TableCellEditor editor;
        if (this.cellEditor != null && !this.cellEditor.stopCellEditing()) {
            return false;
        }
        if (row < 0 || row >= this.getRowCount() || column < 0 || column >= this.getColumnCount()) {
            return false;
        }
        if (!this.isCellEditable(row, column)) {
            return false;
        }
        if (this.editorRemover == null) {
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            this.editorRemover = new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner", this.editorRemover);
        }
        if ((editor = this.getCellEditor(row, column)) != null && editor.isCellEditable(e)) {
            this.editorComp = this.prepareEditor(editor, row, column);
            if (this.editorComp == null) {
                this.removeEditor();
                return false;
            }
            this.editorComp.setBounds(this.getCellRect(row, column, false));
            this.add(this.editorComp);
            this.editorComp.validate();
            this.editorComp.repaint();
            this.setCellEditor(editor);
            this.setEditingRow(row);
            this.setEditingColumn(column);
            editor.addCellEditorListener(this);
            return true;
        }
        return false;
    }

    @BeanProperty(bound=false)
    public boolean isEditing() {
        return this.cellEditor != null;
    }

    @BeanProperty(bound=false)
    public Component getEditorComponent() {
        return this.editorComp;
    }

    public int getEditingColumn() {
        return this.editingColumn;
    }

    public int getEditingRow() {
        return this.editingRow;
    }

    @Override
    public TableUI getUI() {
        return (TableUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(TableUI ui) {
        if (this.ui != ui) {
            super.setUI(ui);
            this.repaint();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateUI() {
        if (this.updateInProgress) {
            return;
        }
        this.updateInProgress = true;
        try {
            TableColumnModel cm = this.getColumnModel();
            for (int column = 0; column < cm.getColumnCount(); ++column) {
                TableColumn aColumn = cm.getColumn(column);
                SwingUtilities.updateRendererOrEditorUI(aColumn.getCellRenderer());
                SwingUtilities.updateRendererOrEditorUI(aColumn.getCellEditor());
                SwingUtilities.updateRendererOrEditorUI(aColumn.getHeaderRenderer());
            }
            Enumeration<Object> defaultRenderers = this.defaultRenderersByColumnClass.elements();
            while (defaultRenderers.hasMoreElements()) {
                SwingUtilities.updateRendererOrEditorUI(defaultRenderers.nextElement());
            }
            Enumeration<Object> defaultEditors = this.defaultEditorsByColumnClass.elements();
            while (defaultEditors.hasMoreElements()) {
                SwingUtilities.updateRendererOrEditorUI(defaultEditors.nextElement());
            }
            if (this.tableHeader != null && this.tableHeader.getParent() == null) {
                this.tableHeader.updateUI();
            }
            this.configureEnclosingScrollPaneUI();
            this.setUI((TableUI)UIManager.getUI(this));
        }
        finally {
            this.updateInProgress = false;
        }
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @BeanProperty(description="The model that is the source of the data for this view.")
    public void setModel(TableModel dataModel) {
        if (dataModel == null) {
            throw new IllegalArgumentException("Cannot set a null TableModel");
        }
        if (this.dataModel != dataModel) {
            TableModel old = this.dataModel;
            if (old != null) {
                old.removeTableModelListener(this);
            }
            this.dataModel = dataModel;
            dataModel.addTableModelListener(this);
            this.tableChanged(new TableModelEvent(dataModel, -1));
            this.firePropertyChange("model", old, dataModel);
            if (this.getAutoCreateRowSorter()) {
                this.setRowSorter(new TableRowSorter<TableModel>(dataModel));
            }
        }
    }

    public TableModel getModel() {
        return this.dataModel;
    }

    @BeanProperty(description="The object governing the way columns appear in the view.")
    public void setColumnModel(TableColumnModel columnModel) {
        if (columnModel == null) {
            throw new IllegalArgumentException("Cannot set a null ColumnModel");
        }
        TableColumnModel old = this.columnModel;
        if (columnModel != old) {
            if (old != null) {
                old.removeColumnModelListener(this);
            }
            this.columnModel = columnModel;
            columnModel.addColumnModelListener(this);
            if (this.tableHeader != null) {
                this.tableHeader.setColumnModel(columnModel);
            }
            this.firePropertyChange("columnModel", old, columnModel);
            this.resizeAndRepaint();
        }
    }

    public TableColumnModel getColumnModel() {
        return this.columnModel;
    }

    @BeanProperty(description="The selection model for rows.")
    public void setSelectionModel(ListSelectionModel selectionModel) {
        if (selectionModel == null) {
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }
        ListSelectionModel oldModel = this.selectionModel;
        if (selectionModel != oldModel) {
            if (oldModel != null) {
                oldModel.removeListSelectionListener(this);
            }
            this.selectionModel = selectionModel;
            selectionModel.addListSelectionListener(this);
            this.firePropertyChange("selectionModel", oldModel, selectionModel);
            this.repaint();
        }
    }

    public ListSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            JTableHeader header = this.getTableHeader();
            if (header != null) {
                header.repaint();
            }
        } else if (e.getType() == RowSorterEvent.Type.SORTED) {
            this.sorterChanged = true;
            if (!this.ignoreSortChange) {
                this.sortedTableChanged(e, null);
            }
        }
    }

    private void sortedTableChanged(RowSorterEvent sortedEvent, TableModelEvent e) {
        ModelChange change;
        int editingModelIndex = -1;
        ModelChange modelChange = change = e != null ? new ModelChange(this, e) : null;
        if (!(change != null && change.allRowsChanged || this.editingRow == -1)) {
            editingModelIndex = this.convertRowIndexToModel(sortedEvent, this.editingRow);
        }
        this.sortManager.prepareForChange(sortedEvent, change);
        if (e != null) {
            if (change.type == 0) {
                this.repaintSortedRows(change);
            }
            this.notifySorter(change);
            if (change.type != 0) {
                this.sorterChanged = true;
            }
        } else {
            this.sorterChanged = true;
        }
        this.sortManager.processChange(sortedEvent, change, this.sorterChanged);
        if (this.sorterChanged) {
            if (this.editingRow != -1) {
                int newIndex = editingModelIndex == -1 ? -1 : this.convertRowIndexToView(editingModelIndex, change);
                this.restoreSortingEditingRow(newIndex);
            }
            if (e == null || change.type != 0) {
                this.resizeAndRepaint();
            }
        }
        if (change != null && change.allRowsChanged) {
            this.clearSelectionAndLeadAnchor();
            this.resizeAndRepaint();
        }
    }

    private void repaintSortedRows(ModelChange change) {
        if (change.startModelIndex > change.endModelIndex || change.startModelIndex + 10 < change.endModelIndex) {
            this.repaint();
            return;
        }
        int eventColumn = change.event.getColumn();
        int columnViewIndex = eventColumn;
        if (columnViewIndex == -1) {
            columnViewIndex = 0;
        } else if ((columnViewIndex = this.convertColumnIndexToView(columnViewIndex)) == -1) {
            return;
        }
        int modelIndex = change.startModelIndex;
        while (modelIndex <= change.endModelIndex) {
            int viewIndex;
            if ((viewIndex = this.convertRowIndexToView(modelIndex++)) == -1) continue;
            Rectangle dirty = this.getCellRect(viewIndex, columnViewIndex, false);
            int x = dirty.x;
            int w = dirty.width;
            if (eventColumn == -1) {
                x = 0;
                w = this.getWidth();
            }
            this.repaint(x, dirty.y, w, dirty.height);
        }
    }

    private void restoreSortingSelection(int[] selection, int lead, ModelChange change) {
        int i;
        for (i = selection.length - 1; i >= 0; --i) {
            selection[i] = this.convertRowIndexToView(selection[i], change);
        }
        lead = this.convertRowIndexToView(lead, change);
        if (selection.length == 0 || selection.length == 1 && selection[0] == this.getSelectedRow()) {
            return;
        }
        this.selectionModel.setValueIsAdjusting(true);
        this.selectionModel.clearSelection();
        for (i = selection.length - 1; i >= 0; --i) {
            if (selection[i] == -1) continue;
            this.selectionModel.addSelectionInterval(selection[i], selection[i]);
        }
        SwingUtilities2.setLeadAnchorWithoutSelection(this.selectionModel, lead, lead);
        this.selectionModel.setValueIsAdjusting(false);
    }

    private void restoreSortingEditingRow(int editingRow) {
        if (editingRow == -1) {
            TableCellEditor editor = this.getCellEditor();
            if (editor != null) {
                editor.cancelCellEditing();
                if (this.getCellEditor() != null) {
                    this.removeEditor();
                }
            }
        } else {
            this.editingRow = editingRow;
            this.repaint();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void notifySorter(ModelChange change) {
        try {
            this.ignoreSortChange = true;
            this.sorterChanged = false;
            switch (change.type) {
                case 0: {
                    if (change.event.getLastRow() == Integer.MAX_VALUE) {
                        this.sortManager.sorter.allRowsChanged();
                        return;
                    }
                    if (change.event.getColumn() == -1) {
                        this.sortManager.sorter.rowsUpdated(change.startModelIndex, change.endModelIndex);
                        return;
                    }
                    this.sortManager.sorter.rowsUpdated(change.startModelIndex, change.endModelIndex, change.event.getColumn());
                    return;
                }
                case 1: {
                    this.sortManager.sorter.rowsInserted(change.startModelIndex, change.endModelIndex);
                    return;
                }
                case -1: {
                    this.sortManager.sorter.rowsDeleted(change.startModelIndex, change.endModelIndex);
                    return;
                }
            }
            return;
        }
        finally {
            this.ignoreSortChange = false;
        }
    }

    private int convertRowIndexToView(int modelIndex, ModelChange change) {
        if (modelIndex < 0) {
            return -1;
        }
        if (change != null && modelIndex >= change.startModelIndex) {
            if (change.type == 1) {
                if (modelIndex + change.length >= change.modelRowCount) {
                    return -1;
                }
                return this.sortManager.sorter.convertRowIndexToView(modelIndex + change.length);
            }
            if (change.type == -1) {
                if (modelIndex <= change.endModelIndex) {
                    return -1;
                }
                if (modelIndex - change.length >= change.modelRowCount) {
                    return -1;
                }
                return this.sortManager.sorter.convertRowIndexToView(modelIndex - change.length);
            }
        }
        if (modelIndex >= this.getModel().getRowCount()) {
            return -1;
        }
        return this.sortManager.sorter.convertRowIndexToView(modelIndex);
    }

    private int[] convertSelectionToModel(RowSorterEvent e) {
        int[] selection = this.getSelectedRows();
        for (int i = selection.length - 1; i >= 0; --i) {
            selection[i] = this.convertRowIndexToModel(e, selection[i]);
        }
        return selection;
    }

    private int convertRowIndexToModel(RowSorterEvent e, int viewIndex) {
        if (e != null) {
            if (e.getPreviousRowCount() == 0) {
                return viewIndex;
            }
            return e.convertPreviousRowIndexToModel(viewIndex);
        }
        if (viewIndex < 0 || viewIndex >= this.getRowCount()) {
            return -1;
        }
        return this.convertRowIndexToModel(viewIndex);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        Rectangle dirtyRegion;
        if (e == null || e.getFirstRow() == -1) {
            this.clearSelectionAndLeadAnchor();
            this.rowModel = null;
            if (this.sortManager != null) {
                try {
                    this.ignoreSortChange = true;
                    this.sortManager.sorter.modelStructureChanged();
                }
                finally {
                    this.ignoreSortChange = false;
                }
                this.sortManager.allChanged();
            }
            if (this.getAutoCreateColumnsFromModel()) {
                this.createDefaultColumnsFromModel();
                return;
            }
            this.resizeAndRepaint();
            return;
        }
        if (this.sortManager != null) {
            this.sortedTableChanged(null, e);
            return;
        }
        if (this.rowModel != null) {
            this.repaint();
        }
        if (e.getType() == 1) {
            this.tableRowsInserted(e);
            return;
        }
        if (e.getType() == -1) {
            this.tableRowsDeleted(e);
            return;
        }
        int modelColumn = e.getColumn();
        int start = e.getFirstRow();
        int end = e.getLastRow();
        if (modelColumn == -1) {
            dirtyRegion = new Rectangle(0, start * this.getRowHeight(), this.getColumnModel().getTotalColumnWidth(), 0);
        } else {
            int column = this.convertColumnIndexToView(modelColumn);
            dirtyRegion = this.getCellRect(start, column, false);
        }
        if (end != Integer.MAX_VALUE) {
            dirtyRegion.height = (end - start + 1) * this.getRowHeight();
            this.repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
        } else {
            this.clearSelectionAndLeadAnchor();
            this.resizeAndRepaint();
            this.rowModel = null;
        }
    }

    private void tableRowsInserted(TableModelEvent e) {
        int start = e.getFirstRow();
        int end = e.getLastRow();
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = this.getRowCount() - 1;
        }
        int length = end - start + 1;
        this.selectionModel.insertIndexInterval(start, length, true);
        if (this.rowModel != null) {
            this.rowModel.insertEntries(start, length, this.getRowHeight());
        }
        int rh = this.getRowHeight();
        Rectangle drawRect = new Rectangle(0, start * rh, this.getColumnModel().getTotalColumnWidth(), (this.getRowCount() - start) * rh);
        this.revalidate();
        this.repaint(drawRect);
    }

    private void tableRowsDeleted(TableModelEvent e) {
        int start = e.getFirstRow();
        int end = e.getLastRow();
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = this.getRowCount() - 1;
        }
        int deletedCount = end - start + 1;
        int previousRowCount = this.getRowCount() + deletedCount;
        this.selectionModel.removeIndexInterval(start, end);
        if (this.rowModel != null) {
            this.rowModel.removeEntries(start, deletedCount);
        }
        int rh = this.getRowHeight();
        Rectangle drawRect = new Rectangle(0, start * rh, this.getColumnModel().getTotalColumnWidth(), (previousRowCount - start) * rh);
        this.revalidate();
        this.repaint(drawRect);
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
        if (this.isEditing()) {
            this.removeEditor();
        }
        this.resizeAndRepaint();
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        if (this.isEditing()) {
            this.removeEditor();
        }
        this.resizeAndRepaint();
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        if (this.isEditing() && !this.getCellEditor().stopCellEditing()) {
            this.getCellEditor().cancelCellEditing();
        }
        this.repaint();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        TableColumn resizingColumn;
        if (this.isEditing() && !this.getCellEditor().stopCellEditing()) {
            this.getCellEditor().cancelCellEditing();
        }
        if ((resizingColumn = this.getResizingColumn()) != null && this.autoResizeMode == 0) {
            resizingColumn.setPreferredWidth(resizingColumn.getWidth());
        }
        this.resizeAndRepaint();
    }

    private int limit(int i, int a, int b) {
        return Math.min(b, Math.max(i, a));
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        boolean isAdjusting = e.getValueIsAdjusting();
        if (this.columnSelectionAdjusting && !isAdjusting) {
            this.columnSelectionAdjusting = false;
            return;
        }
        this.columnSelectionAdjusting = isAdjusting;
        if (this.getRowCount() <= 0 || this.getColumnCount() <= 0) {
            return;
        }
        int firstIndex = this.limit(e.getFirstIndex(), 0, this.getColumnCount() - 1);
        int lastIndex = this.limit(e.getLastIndex(), 0, this.getColumnCount() - 1);
        int minRow = 0;
        int maxRow = this.getRowCount() - 1;
        if (this.getRowSelectionAllowed()) {
            minRow = this.selectionModel.getMinSelectionIndex();
            maxRow = this.selectionModel.getMaxSelectionIndex();
            int leadRow = this.getAdjustedIndex(this.selectionModel.getLeadSelectionIndex(), true);
            if (minRow == -1 || maxRow == -1) {
                if (leadRow == -1) {
                    return;
                }
                minRow = maxRow = leadRow;
            } else if (leadRow != -1) {
                minRow = Math.min(minRow, leadRow);
                maxRow = Math.max(maxRow, leadRow);
            }
        }
        Rectangle firstColumnRect = this.getCellRect(minRow, firstIndex, false);
        Rectangle lastColumnRect = this.getCellRect(maxRow, lastIndex, false);
        Rectangle dirtyRegion = firstColumnRect.union(lastColumnRect);
        this.repaint(dirtyRegion);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (this.sortManager != null) {
            this.sortManager.viewSelectionChanged(e);
        }
        boolean isAdjusting = e.getValueIsAdjusting();
        if (this.rowSelectionAdjusting && !isAdjusting) {
            this.rowSelectionAdjusting = false;
            return;
        }
        this.rowSelectionAdjusting = isAdjusting;
        if (this.getRowCount() <= 0 || this.getColumnCount() <= 0) {
            return;
        }
        int firstIndex = this.limit(e.getFirstIndex(), 0, this.getRowCount() - 1);
        int lastIndex = this.limit(e.getLastIndex(), 0, this.getRowCount() - 1);
        Rectangle firstRowRect = this.getCellRect(firstIndex, 0, false);
        Rectangle lastRowRect = this.getCellRect(lastIndex, this.getColumnCount() - 1, false);
        Rectangle dirtyRegion = firstRowRect.union(lastRowRect);
        this.repaint(dirtyRegion);
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = this.getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();
            this.setValueAt(value, this.editingRow, this.editingColumn);
            this.removeEditor();
        }
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        this.removeEditor();
    }

    @BeanProperty(bound=false, description="The preferred size of the viewport.")
    public void setPreferredScrollableViewportSize(Dimension size) {
        this.preferredViewportSize = size;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.preferredViewportSize;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int leadingRow = this.getLeadingRow(visibleRect);
        int leadingCol = this.getLeadingCol(visibleRect);
        if (orientation == 1 && leadingRow < 0) {
            return this.getRowHeight();
        }
        if (orientation == 0 && leadingCol < 0) {
            return 100;
        }
        Rectangle leadingCellRect = this.getCellRect(leadingRow, leadingCol, true);
        int leadingVisibleEdge = this.leadingEdge(visibleRect, orientation);
        int leadingCellEdge = this.leadingEdge(leadingCellRect, orientation);
        int leadingCellSize = orientation == 1 ? leadingCellRect.height : leadingCellRect.width;
        if (leadingVisibleEdge == leadingCellEdge) {
            if (direction < 0) {
                int retVal = 0;
                if (orientation == 1) {
                    while (--leadingRow >= 0 && (retVal = this.getRowHeight(leadingRow)) == 0) {
                    }
                } else {
                    while (--leadingCol >= 0 && (retVal = this.getCellRect((int)leadingRow, (int)leadingCol, (boolean)true).width) == 0) {
                    }
                }
                return retVal;
            }
            return leadingCellSize;
        }
        int hiddenAmt = Math.abs(leadingVisibleEdge - leadingCellEdge);
        int visibleAmt = leadingCellSize - hiddenAmt;
        if (direction > 0) {
            return visibleAmt;
        }
        return hiddenAmt;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (this.getRowCount() == 0) {
            if (1 == orientation) {
                int rh = this.getRowHeight();
                return rh > 0 ? Math.max(rh, visibleRect.height / rh * rh) : visibleRect.height;
            }
            return visibleRect.width;
        }
        if (null == this.rowModel && 1 == orientation) {
            int row = this.rowAtPoint(visibleRect.getLocation());
            assert (row != -1);
            int col = this.columnAtPoint(visibleRect.getLocation());
            Rectangle cellRect = this.getCellRect(row, col, true);
            if (cellRect.y == visibleRect.y) {
                int rh = this.getRowHeight();
                assert (rh > 0);
                return Math.max(rh, visibleRect.height / rh * rh);
            }
        }
        if (direction < 0) {
            return this.getPreviousBlockIncrement(visibleRect, orientation);
        }
        return this.getNextBlockIncrement(visibleRect, orientation);
    }

    private int getPreviousBlockIncrement(Rectangle visibleRect, int orientation) {
        int newLeadingEdge;
        Point newCellLoc;
        int newEdge;
        int visibleLeadingEdge = this.leadingEdge(visibleRect, orientation);
        boolean leftToRight = this.getComponentOrientation().isLeftToRight();
        if (orientation == 1) {
            newEdge = visibleLeadingEdge - visibleRect.height;
            int x = visibleRect.x + (leftToRight ? 0 : visibleRect.width);
            newCellLoc = new Point(x, newEdge);
        } else if (leftToRight) {
            newEdge = visibleLeadingEdge - visibleRect.width;
            newCellLoc = new Point(newEdge, visibleRect.y);
        } else {
            newEdge = visibleLeadingEdge + visibleRect.width;
            newCellLoc = new Point(newEdge - 1, visibleRect.y);
        }
        int row = this.rowAtPoint(newCellLoc);
        int col = this.columnAtPoint(newCellLoc);
        if (orientation == 1 & row < 0) {
            newLeadingEdge = 0;
        } else if (orientation == 0 & col < 0) {
            newLeadingEdge = leftToRight ? 0 : this.getWidth();
        } else {
            Rectangle newCellRect = this.getCellRect(row, col, true);
            int newCellLeadingEdge = this.leadingEdge(newCellRect, orientation);
            int newCellTrailingEdge = this.trailingEdge(newCellRect, orientation);
            newLeadingEdge = (orientation == 1 || leftToRight) && newCellTrailingEdge >= visibleLeadingEdge ? newCellLeadingEdge : (orientation == 0 && !leftToRight && newCellTrailingEdge <= visibleLeadingEdge ? newCellLeadingEdge : (newEdge == newCellLeadingEdge ? newCellLeadingEdge : newCellTrailingEdge));
        }
        return Math.abs(visibleLeadingEdge - newLeadingEdge);
    }

    private int getNextBlockIncrement(Rectangle visibleRect, int orientation) {
        boolean cellFillsVis;
        int trailingRow = this.getTrailingRow(visibleRect);
        int trailingCol = this.getTrailingCol(visibleRect);
        int visibleLeadingEdge = this.leadingEdge(visibleRect, orientation);
        if (orientation == 1 && trailingRow < 0) {
            return visibleRect.height;
        }
        if (orientation == 0 && trailingCol < 0) {
            return visibleRect.width;
        }
        Rectangle cellRect = this.getCellRect(trailingRow, trailingCol, true);
        int cellLeadingEdge = this.leadingEdge(cellRect, orientation);
        int cellTrailingEdge = this.trailingEdge(cellRect, orientation);
        if (orientation == 1 || this.getComponentOrientation().isLeftToRight()) {
            cellFillsVis = cellLeadingEdge <= visibleLeadingEdge;
        } else {
            boolean bl = cellFillsVis = cellLeadingEdge >= visibleLeadingEdge;
        }
        int newLeadingEdge = cellFillsVis ? cellTrailingEdge : (cellTrailingEdge == this.trailingEdge(visibleRect, orientation) ? cellTrailingEdge : cellLeadingEdge);
        return Math.abs(newLeadingEdge - visibleLeadingEdge);
    }

    private int getLeadingRow(Rectangle visibleRect) {
        Point leadingPoint = this.getComponentOrientation().isLeftToRight() ? new Point(visibleRect.x, visibleRect.y) : new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y);
        return this.rowAtPoint(leadingPoint);
    }

    private int getLeadingCol(Rectangle visibleRect) {
        Point leadingPoint = this.getComponentOrientation().isLeftToRight() ? new Point(visibleRect.x, visibleRect.y) : new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y);
        return this.columnAtPoint(leadingPoint);
    }

    private int getTrailingRow(Rectangle visibleRect) {
        Point trailingPoint = this.getComponentOrientation().isLeftToRight() ? new Point(visibleRect.x, visibleRect.y + visibleRect.height - 1) : new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y + visibleRect.height - 1);
        return this.rowAtPoint(trailingPoint);
    }

    private int getTrailingCol(Rectangle visibleRect) {
        Point trailingPoint = this.getComponentOrientation().isLeftToRight() ? new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y) : new Point(visibleRect.x, visibleRect.y);
        return this.columnAtPoint(trailingPoint);
    }

    private int leadingEdge(Rectangle rect, int orientation) {
        if (orientation == 1) {
            return rect.y;
        }
        if (this.getComponentOrientation().isLeftToRight()) {
            return rect.x;
        }
        return rect.x + rect.width;
    }

    private int trailingEdge(Rectangle rect, int orientation) {
        if (orientation == 1) {
            return rect.y + rect.height;
        }
        if (this.getComponentOrientation().isLeftToRight()) {
            return rect.x + rect.width;
        }
        return rect.x;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportWidth() {
        return this.autoResizeMode != 0;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportHeight() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        return this.getFillsViewportHeight() && parent instanceof JViewport && parent.getHeight() > this.getPreferredSize().height;
    }

    @BeanProperty(description="Whether or not this table is always made large enough to fill the height of an enclosing viewport")
    public void setFillsViewportHeight(boolean fillsViewportHeight) {
        boolean old = this.fillsViewportHeight;
        this.fillsViewportHeight = fillsViewportHeight;
        this.resizeAndRepaint();
        this.firePropertyChange("fillsViewportHeight", old, fillsViewportHeight);
    }

    public boolean getFillsViewportHeight() {
        return this.fillsViewportHeight;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
        if (!retValue && condition == 1 && this.isFocusOwner() && !Boolean.FALSE.equals(this.getClientProperty("JTable.autoStartsEdit"))) {
            Component editorComponent = this.getEditorComponent();
            if (editorComponent == null) {
                if (e == null || e.getID() != 401) {
                    return false;
                }
                int code = e.getKeyCode();
                if (code == 16 || code == 17 || code == 18 || code == 157 || code == 65406) {
                    return false;
                }
                int leadRow = this.getSelectionModel().getLeadSelectionIndex();
                int leadColumn = this.getColumnModel().getSelectionModel().getLeadSelectionIndex();
                if (leadRow != -1 && leadColumn != -1 && !this.isEditing() && !this.editCellAt(leadRow, leadColumn, e)) {
                    return false;
                }
                editorComponent = this.getEditorComponent();
                if (editorComponent == null) {
                    return false;
                }
            }
            if (editorComponent instanceof JComponent) {
                retValue = ((JComponent)editorComponent).processKeyBinding(ks, e, 0, pressed);
                Object prop = this.getClientProperty("JTable.forceAutoStartsEdit");
                if (this.getSurrendersFocusOnKeystroke() || Boolean.TRUE.equals(prop)) {
                    editorComponent.requestFocus();
                }
            }
        }
        return retValue;
    }

    protected void createDefaultRenderers() {
        this.defaultRenderersByColumnClass = new UIDefaults(8, 0.75f);
        this.defaultRenderersByColumnClass.put(Object.class, t -> new DefaultTableCellRenderer.UIResource());
        this.defaultRenderersByColumnClass.put(Number.class, t -> new NumberRenderer());
        this.defaultRenderersByColumnClass.put(Float.class, t -> new DoubleRenderer());
        this.defaultRenderersByColumnClass.put(Double.class, t -> new DoubleRenderer());
        this.defaultRenderersByColumnClass.put(Date.class, t -> new DateRenderer());
        this.defaultRenderersByColumnClass.put(Icon.class, t -> new IconRenderer());
        this.defaultRenderersByColumnClass.put(ImageIcon.class, t -> new IconRenderer());
        this.defaultRenderersByColumnClass.put(Boolean.class, t -> new BooleanRenderer());
    }

    protected void createDefaultEditors() {
        this.defaultEditorsByColumnClass = new UIDefaults(3, 0.75f);
        this.defaultEditorsByColumnClass.put(Object.class, t -> new GenericEditor());
        this.defaultEditorsByColumnClass.put(Number.class, t -> new NumberEditor());
        this.defaultEditorsByColumnClass.put(Boolean.class, t -> new BooleanEditor());
    }

    protected void initializeLocalVars() {
        this.updateSelectionOnSort = true;
        this.createDefaultRenderers();
        this.createDefaultEditors();
        this.setTableHeader(this.createDefaultTableHeader());
        this.setShowGrid(true);
        this.setAutoResizeMode(2);
        this.setRowHeight(16);
        this.isRowHeightSet = false;
        this.setRowMargin(1);
        this.setRowSelectionAllowed(true);
        this.setCellEditor(null);
        this.setEditingColumn(-1);
        this.setEditingRow(-1);
        this.setSurrendersFocusOnKeystroke(false);
        this.setPreferredScrollableViewportSize(new Dimension(450, 400));
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        this.setAutoscrolls(true);
    }

    protected TableModel createDefaultDataModel() {
        return new DefaultTableModel();
    }

    protected TableColumnModel createDefaultColumnModel() {
        return new DefaultTableColumnModel();
    }

    protected ListSelectionModel createDefaultSelectionModel() {
        return new DefaultListSelectionModel();
    }

    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(this.columnModel);
    }

    protected void resizeAndRepaint() {
        this.revalidate();
        this.repaint();
    }

    public TableCellEditor getCellEditor() {
        return this.cellEditor;
    }

    @BeanProperty(description="The table's active cell editor.")
    public void setCellEditor(TableCellEditor anEditor) {
        TableCellEditor oldEditor = this.cellEditor;
        this.cellEditor = anEditor;
        this.firePropertyChange("tableCellEditor", oldEditor, anEditor);
    }

    public void setEditingColumn(int aColumn) {
        this.editingColumn = aColumn;
    }

    public void setEditingRow(int aRow) {
        this.editingRow = aRow;
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = this.getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            renderer = this.getDefaultRenderer(this.getColumnClass(column));
        }
        return renderer;
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Object value = this.getValueAt(row, column);
        boolean isSelected = false;
        boolean hasFocus = false;
        if (!this.isPaintingForPrint()) {
            isSelected = this.isCellSelected(row, column);
            boolean rowIsLead = this.selectionModel.getLeadSelectionIndex() == row;
            boolean colIsLead = this.columnModel.getSelectionModel().getLeadSelectionIndex() == column;
            hasFocus = rowIsLead && colIsLead && this.isFocusOwner();
        }
        return renderer.getTableCellRendererComponent(this, value, isSelected, hasFocus, row, column);
    }

    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = this.getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            editor = this.getDefaultEditor(this.getColumnClass(column));
        }
        return editor;
    }

    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        JComponent jComp;
        boolean isSelected;
        Object value = this.getValueAt(row, column);
        Component comp = editor.getTableCellEditorComponent(this, value, isSelected = this.isCellSelected(row, column), row, column);
        if (comp instanceof JComponent && (jComp = (JComponent)comp).getNextFocusableComponent() == null) {
            jComp.setNextFocusableComponent(this);
        }
        return comp;
    }

    public void removeEditor() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner", this.editorRemover);
        this.editorRemover = null;
        TableCellEditor editor = this.getCellEditor();
        if (editor != null) {
            editor.removeCellEditorListener(this);
            if (this.editorComp != null) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean isFocusOwnerInTheTable = focusOwner != null ? SwingUtilities.isDescendingFrom(focusOwner, this) : false;
                this.remove(this.editorComp);
                if (isFocusOwnerInTheTable) {
                    this.requestFocusInWindow();
                }
            }
            Rectangle cellRect = this.getCellRect(this.editingRow, this.editingColumn, false);
            this.setCellEditor(null);
            this.setEditingColumn(-1);
            this.setEditingRow(-1);
            this.editorComp = null;
            this.repaint(cellRect);
        }
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

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField f = s.readFields();
        TableModel newDataModel = (TableModel)f.get("dataModel", null);
        if (newDataModel == null) {
            throw new InvalidObjectException("Null dataModel");
        }
        this.dataModel = newDataModel;
        TableColumnModel newColumnModel = (TableColumnModel)f.get("columnModel", null);
        if (newColumnModel == null) {
            throw new InvalidObjectException("Null columnModel");
        }
        this.columnModel = newColumnModel;
        ListSelectionModel newSelectionModel = (ListSelectionModel)f.get("selectionModel", null);
        if (newSelectionModel == null) {
            throw new InvalidObjectException("Null selectionModel");
        }
        this.selectionModel = newSelectionModel;
        this.tableHeader = (JTableHeader)f.get("tableHeader", null);
        int newRowHeight = f.get("rowHeight", 0);
        if (newRowHeight <= 0) {
            throw new InvalidObjectException("Row height less than 1");
        }
        this.rowHeight = newRowHeight;
        this.rowMargin = f.get("rowMargin", 0);
        Color newGridColor = (Color)f.get("gridColor", null);
        if (newGridColor == null) {
            throw new InvalidObjectException("Null gridColor");
        }
        this.gridColor = newGridColor;
        this.showHorizontalLines = f.get("showHorizontalLines", false);
        this.showVerticalLines = f.get("showVerticalLines", false);
        int newAutoResizeMode = f.get("autoResizeMode", 0);
        if (!JTable.isValidAutoResizeMode(newAutoResizeMode)) {
            throw new InvalidObjectException("autoResizeMode is not valid");
        }
        this.autoResizeMode = newAutoResizeMode;
        this.autoCreateColumnsFromModel = f.get("autoCreateColumnsFromModel", false);
        this.preferredViewportSize = (Dimension)f.get("preferredViewportSize", null);
        this.rowSelectionAllowed = f.get("rowSelectionAllowed", false);
        this.cellSelectionEnabled = f.get("cellSelectionEnabled", false);
        this.selectionForeground = (Color)f.get("selectionForeground", null);
        this.selectionBackground = (Color)f.get("selectionBackground", null);
        this.rowModel = (SizeSequence)f.get("rowModel", null);
        boolean newDragEnabled = f.get("dragEnabled", false);
        this.checkDragEnabled(newDragEnabled);
        this.dragEnabled = newDragEnabled;
        this.surrendersFocusOnKeystroke = f.get("surrendersFocusOnKeystroke", false);
        this.editorRemover = (PropertyChangeListener)f.get("editorRemover", null);
        this.columnSelectionAdjusting = f.get("columnSelectionAdjusting", false);
        this.rowSelectionAdjusting = f.get("rowSelectionAdjusting", false);
        this.printError = (Throwable)f.get("printError", null);
        this.isRowHeightSet = f.get("isRowHeightSet", false);
        this.updateSelectionOnSort = f.get("updateSelectionOnSort", false);
        this.ignoreSortChange = f.get("ignoreSortChange", false);
        this.sorterChanged = f.get("sorterChanged", false);
        this.autoCreateRowSorter = f.get("autoCreateRowSorter", false);
        this.fillsViewportHeight = f.get("fillsViewportHeight", false);
        DropMode newDropMode = (DropMode)((Object)f.get("dropMode", (Object)DropMode.USE_SELECTION));
        JTable.checkDropMode(newDropMode);
        this.dropMode = newDropMode;
        if (this.ui != null && this.getUIClassID().equals(uiClassID)) {
            this.ui.installUI(this);
        }
        this.createDefaultRenderers();
        this.createDefaultEditors();
        if (this.getToolTipText() == null) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
    }

    @Override
    void compWriteObjectNotify() {
        super.compWriteObjectNotify();
        if (this.getToolTipText() == null) {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    @Override
    protected String paramString() {
        String showVerticalLinesString;
        String gridColorString = this.gridColor != null ? this.gridColor.toString() : "";
        String showHorizontalLinesString = this.showHorizontalLines ? "true" : "false";
        String string = showVerticalLinesString = this.showVerticalLines ? "true" : "false";
        String autoResizeModeString = this.autoResizeMode == 0 ? "AUTO_RESIZE_OFF" : (this.autoResizeMode == 1 ? "AUTO_RESIZE_NEXT_COLUMN" : (this.autoResizeMode == 2 ? "AUTO_RESIZE_SUBSEQUENT_COLUMNS" : (this.autoResizeMode == 3 ? "AUTO_RESIZE_LAST_COLUMN" : (this.autoResizeMode == 4 ? "AUTO_RESIZE_ALL_COLUMNS" : ""))));
        String autoCreateColumnsFromModelString = this.autoCreateColumnsFromModel ? "true" : "false";
        String preferredViewportSizeString = this.preferredViewportSize != null ? this.preferredViewportSize.toString() : "";
        String rowSelectionAllowedString = this.rowSelectionAllowed ? "true" : "false";
        String cellSelectionEnabledString = this.cellSelectionEnabled ? "true" : "false";
        String selectionForegroundString = this.selectionForeground != null ? this.selectionForeground.toString() : "";
        String selectionBackgroundString = this.selectionBackground != null ? this.selectionBackground.toString() : "";
        return super.paramString() + ",autoCreateColumnsFromModel=" + autoCreateColumnsFromModelString + ",autoResizeMode=" + autoResizeModeString + ",cellSelectionEnabled=" + cellSelectionEnabledString + ",editingColumn=" + this.editingColumn + ",editingRow=" + this.editingRow + ",gridColor=" + gridColorString + ",preferredViewportSize=" + preferredViewportSizeString + ",rowHeight=" + this.rowHeight + ",rowMargin=" + this.rowMargin + ",rowSelectionAllowed=" + rowSelectionAllowedString + ",selectionBackground=" + selectionBackgroundString + ",selectionForeground=" + selectionForegroundString + ",showHorizontalLines=" + showHorizontalLinesString + ",showVerticalLines=" + showVerticalLinesString;
    }

    public boolean print() throws PrinterException {
        return this.print(PrintMode.FIT_WIDTH);
    }

    public boolean print(PrintMode printMode) throws PrinterException {
        return this.print(printMode, null, null);
    }

    public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat) throws PrinterException {
        boolean showDialogs = !GraphicsEnvironment.isHeadless();
        return this.print(printMode, headerFormat, footerFormat, showDialogs, null, showDialogs);
    }

    public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat, boolean showPrintDialog, PrintRequestAttributeSet attr, boolean interactive) throws PrinterException, HeadlessException {
        return this.print(printMode, headerFormat, footerFormat, showPrintDialog, attr, interactive, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat, boolean showPrintDialog, PrintRequestAttributeSet attr, boolean interactive, PrintService service) throws PrinterException, HeadlessException {
        Throwable pe;
        PrintingStatus printingStatus;
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        if (isHeadless) {
            if (showPrintDialog) {
                throw new HeadlessException("Can't show print dialog.");
            }
            if (interactive) {
                throw new HeadlessException("Can't run interactively.");
            }
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        if (this.isEditing() && !this.getCellEditor().stopCellEditing()) {
            this.getCellEditor().cancelCellEditing();
        }
        if (attr == null) {
            attr = new HashPrintRequestAttributeSet();
        }
        Printable printable = this.getPrintable(printMode, headerFormat, footerFormat);
        if (interactive) {
            printable = new ThreadSafePrintable(printable);
            printingStatus = PrintingStatus.createPrintingStatus(this, job);
            printable = printingStatus.createNotificationPrintable(printable);
        } else {
            printingStatus = null;
        }
        job.setPrintable(printable);
        if (service != null) {
            job.setPrintService(service);
        }
        if (showPrintDialog && !job.printDialog(attr)) {
            return false;
        }
        if (!interactive) {
            job.print(attr);
            return true;
        }
        this.printError = null;
        Object lock = new Object();
        PrintRequestAttributeSet copyAttr = attr;
        Runnable runnable = () -> {
            try {
                job.print(copyAttr);
            }
            catch (Throwable t) {
                Object object = lock;
                synchronized (object) {
                    this.printError = t;
                }
            }
            finally {
                printingStatus.dispose();
            }
        };
        Thread th = new Thread(null, runnable, "JTablePrint", 0L, false);
        th.start();
        printingStatus.showModal(true);
        Object object = lock;
        synchronized (object) {
            pe = this.printError;
            this.printError = null;
        }
        if (pe != null) {
            if (pe instanceof PrinterAbortException) {
                return false;
            }
            if (pe instanceof PrinterException) {
                throw (PrinterException)pe;
            }
            if (pe instanceof RuntimeException) {
                throw (RuntimeException)pe;
            }
            if (pe instanceof Error) {
                throw (Error)pe;
            }
            throw new AssertionError((Object)pe);
        }
        return true;
    }

    public Printable getPrintable(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat) {
        return new TablePrintable(this, printMode, headerFormat, footerFormat);
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJTable();
        }
        return this.accessibleContext;
    }

    private final class SortManager {
        RowSorter<? extends TableModel> sorter;
        private ListSelectionModel modelSelection;
        private int modelLeadIndex;
        private boolean syncingSelection;
        private int[] lastModelSelection;
        private SizeSequence modelRowSizes;

        SortManager(RowSorter<? extends TableModel> sorter) {
            this.sorter = sorter;
            sorter.addRowSorterListener(JTable.this);
        }

        public void dispose() {
            if (this.sorter != null) {
                this.sorter.removeRowSorterListener(JTable.this);
            }
        }

        public void setViewRowHeight(int viewIndex, int rowHeight) {
            if (this.modelRowSizes == null) {
                this.modelRowSizes = new SizeSequence(JTable.this.getModel().getRowCount(), JTable.this.getRowHeight());
            }
            this.modelRowSizes.setSize(JTable.this.convertRowIndexToModel(viewIndex), rowHeight);
        }

        public void allChanged() {
            this.modelLeadIndex = -1;
            this.modelSelection = null;
            this.modelRowSizes = null;
        }

        public void viewSelectionChanged(ListSelectionEvent e) {
            if (!this.syncingSelection && this.modelSelection != null) {
                this.modelSelection = null;
            }
        }

        public void prepareForChange(RowSorterEvent sortEvent, ModelChange change) {
            if (JTable.this.getUpdateSelectionOnSort()) {
                this.cacheSelection(sortEvent, change);
            }
        }

        private void cacheSelection(RowSorterEvent sortEvent, ModelChange change) {
            if (sortEvent != null) {
                if (this.modelSelection == null && this.sorter.getViewRowCount() != JTable.this.getModel().getRowCount()) {
                    int modelIndex;
                    this.modelSelection = new DefaultListSelectionModel();
                    ListSelectionModel viewSelection = JTable.this.getSelectionModel();
                    int min = viewSelection.getMinSelectionIndex();
                    int max = viewSelection.getMaxSelectionIndex();
                    for (int viewIndex = min; viewIndex <= max; ++viewIndex) {
                        if (!viewSelection.isSelectedIndex(viewIndex) || (modelIndex = JTable.this.convertRowIndexToModel(sortEvent, viewIndex)) == -1) continue;
                        this.modelSelection.addSelectionInterval(modelIndex, modelIndex);
                    }
                    modelIndex = JTable.this.convertRowIndexToModel(sortEvent, viewSelection.getLeadSelectionIndex());
                    SwingUtilities2.setLeadAnchorWithoutSelection(this.modelSelection, modelIndex, modelIndex);
                } else if (this.modelSelection == null) {
                    this.cacheModelSelection(sortEvent);
                }
            } else if (change.allRowsChanged) {
                this.modelSelection = null;
            } else if (this.modelSelection != null) {
                switch (change.type) {
                    case -1: {
                        this.modelSelection.removeIndexInterval(change.startModelIndex, change.endModelIndex);
                        break;
                    }
                    case 1: {
                        this.modelSelection.insertIndexInterval(change.startModelIndex, change.length, true);
                        break;
                    }
                }
            } else {
                this.cacheModelSelection(null);
            }
        }

        private void cacheModelSelection(RowSorterEvent sortEvent) {
            this.lastModelSelection = JTable.this.convertSelectionToModel(sortEvent);
            this.modelLeadIndex = JTable.this.convertRowIndexToModel(sortEvent, JTable.this.selectionModel.getLeadSelectionIndex());
        }

        public void processChange(RowSorterEvent sortEvent, ModelChange change, boolean sorterChanged) {
            if (change != null) {
                if (change.allRowsChanged) {
                    this.modelRowSizes = null;
                    JTable.this.rowModel = null;
                } else if (this.modelRowSizes != null) {
                    if (change.type == 1) {
                        this.modelRowSizes.insertEntries(change.startModelIndex, change.endModelIndex - change.startModelIndex + 1, JTable.this.getRowHeight());
                    } else if (change.type == -1) {
                        this.modelRowSizes.removeEntries(change.startModelIndex, change.endModelIndex - change.startModelIndex + 1);
                    }
                }
            }
            if (sorterChanged) {
                this.setViewRowHeightsFromModel();
                this.restoreSelection(change);
            }
        }

        private void setViewRowHeightsFromModel() {
            if (this.modelRowSizes != null) {
                JTable.this.rowModel.setSizes(JTable.this.getRowCount(), JTable.this.getRowHeight());
                for (int viewIndex = JTable.this.getRowCount() - 1; viewIndex >= 0; --viewIndex) {
                    int modelIndex = JTable.this.convertRowIndexToModel(viewIndex);
                    JTable.this.rowModel.setSize(viewIndex, this.modelRowSizes.getSize(modelIndex));
                }
            }
        }

        private void restoreSelection(ModelChange change) {
            this.syncingSelection = true;
            if (this.lastModelSelection != null) {
                JTable.this.restoreSortingSelection(this.lastModelSelection, this.modelLeadIndex, change);
                this.lastModelSelection = null;
            } else if (this.modelSelection != null) {
                ListSelectionModel viewSelection = JTable.this.getSelectionModel();
                viewSelection.setValueIsAdjusting(true);
                viewSelection.clearSelection();
                int min = this.modelSelection.getMinSelectionIndex();
                int max = this.modelSelection.getMaxSelectionIndex();
                for (int modelIndex = min; modelIndex <= max; ++modelIndex) {
                    int viewIndex;
                    if (!this.modelSelection.isSelectedIndex(modelIndex) || (viewIndex = JTable.this.convertRowIndexToView(modelIndex)) == -1) continue;
                    viewSelection.addSelectionInterval(viewIndex, viewIndex);
                }
                int viewLeadIndex = this.modelSelection.getLeadSelectionIndex();
                if (viewLeadIndex != -1 && !this.modelSelection.isSelectionEmpty()) {
                    viewLeadIndex = JTable.this.convertRowIndexToView(viewLeadIndex);
                }
                SwingUtilities2.setLeadAnchorWithoutSelection(viewSelection, viewLeadIndex, viewLeadIndex);
                viewSelection.setValueIsAdjusting(false);
            }
            this.syncingSelection = false;
        }
    }

    public static final class DropLocation
    extends TransferHandler.DropLocation {
        private final int row;
        private final int col;
        private final boolean isInsertRow;
        private final boolean isInsertCol;

        private DropLocation(Point p, int row, int col, boolean isInsertRow, boolean isInsertCol) {
            super(p);
            this.row = row;
            this.col = col;
            this.isInsertRow = isInsertRow;
            this.isInsertCol = isInsertCol;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.col;
        }

        public boolean isInsertRow() {
            return this.isInsertRow;
        }

        public boolean isInsertColumn() {
            return this.isInsertCol;
        }

        @Override
        public String toString() {
            return this.getClass().getName() + "[dropPoint=" + String.valueOf(this.getDropPoint()) + ",row=" + this.row + ",column=" + this.col + ",insertRow=" + this.isInsertRow + ",insertColumn=" + this.isInsertCol + "]";
        }
    }

    private static interface Resizable3
    extends Resizable2 {
        public int getMidPointAt(int var1);
    }

    private static interface Resizable2 {
        public int getElementCount();

        public int getLowerBoundAt(int var1);

        public int getUpperBoundAt(int var1);

        public void setSizeAt(int var1, int var2);
    }

    class CellEditorRemover
    implements PropertyChangeListener {
        KeyboardFocusManager focusManager;

        public CellEditorRemover(KeyboardFocusManager fm) {
            this.focusManager = fm;
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (!JTable.this.isEditing() || JTable.this.getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
                return;
            }
            for (Component c = this.focusManager.getPermanentFocusOwner(); c != null; c = c.getParent()) {
                if (c == JTable.this) {
                    return;
                }
                if (!(c instanceof Window) && (!(c instanceof Applet) || c.getParent() != null)) continue;
                if (c != SwingUtilities.getRoot(JTable.this) || JTable.this.getCellEditor().stopCellEditing()) break;
                JTable.this.getCellEditor().cancelCellEditing();
                break;
            }
        }
    }

    private final class ModelChange {
        int startModelIndex;
        int endModelIndex;
        int type;
        int modelRowCount;
        TableModelEvent event;
        int length;
        boolean allRowsChanged;

        ModelChange(JTable jTable, TableModelEvent e) {
            this.startModelIndex = Math.max(0, e.getFirstRow());
            this.endModelIndex = e.getLastRow();
            this.modelRowCount = jTable.getModel().getRowCount();
            if (this.endModelIndex < 0) {
                this.endModelIndex = Math.max(0, this.modelRowCount - 1);
            }
            this.length = this.endModelIndex - this.startModelIndex + 1;
            this.type = e.getType();
            this.event = e;
            this.allRowsChanged = e.getLastRow() == Integer.MAX_VALUE;
        }
    }

    public static enum PrintMode {
        NORMAL,
        FIT_WIDTH;

    }

    private static class ThreadSafePrintable
    implements Printable {
        private Printable printDelegate;
        private int retVal;
        private Throwable retThrowable;

        public ThreadSafePrintable(Printable printDelegate) {
            this.printDelegate = printDelegate;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
            Runnable runnable;
            Runnable runnable2 = runnable = new Runnable(){
                final /* synthetic */ ThreadSafePrintable this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public synchronized void run() {
                    try {
                        this.this$0.retVal = this.this$0.printDelegate.print(graphics, pageFormat, pageIndex);
                    }
                    catch (Throwable throwable) {
                        this.this$0.retThrowable = throwable;
                    }
                    finally {
                        this.notifyAll();
                    }
                }
            };
            synchronized (runnable2) {
                this.retVal = -1;
                this.retThrowable = null;
                SwingUtilities.invokeLater(runnable);
                while (this.retVal == -1 && this.retThrowable == null) {
                    try {
                        runnable.wait();
                    }
                    catch (InterruptedException interruptedException) {}
                }
                if (this.retThrowable != null) {
                    if (this.retThrowable instanceof PrinterException) {
                        throw (PrinterException)this.retThrowable;
                    }
                    if (this.retThrowable instanceof RuntimeException) {
                        throw (RuntimeException)this.retThrowable;
                    }
                    if (this.retThrowable instanceof Error) {
                        throw (Error)this.retThrowable;
                    }
                    throw new AssertionError((Object)this.retThrowable);
                }
                return this.retVal;
            }
        }
    }

    protected class AccessibleJTable
    extends JComponent.AccessibleJComponent
    implements AccessibleSelection,
    ListSelectionListener,
    TableModelListener,
    TableColumnModelListener,
    CellEditorListener,
    PropertyChangeListener,
    AccessibleExtendedTable {
        int previousFocusedRow;
        int previousFocusedCol;
        private Accessible caption;
        private Accessible summary;
        private Accessible[] rowDescription;
        private Accessible[] columnDescription;

        protected AccessibleJTable() {
            JTable.this.putClientProperty("JTable.forceAutoStartsEdit", true);
            JTable.this.addPropertyChangeListener(this);
            JTable.this.getSelectionModel().addListSelectionListener(this);
            TableColumnModel tcm = JTable.this.getColumnModel();
            tcm.addColumnModelListener(this);
            tcm.getSelectionModel().addListSelectionListener(this);
            JTable.this.getModel().addTableModelListener(this);
            this.previousFocusedRow = JTable.this.getSelectionModel().getLeadSelectionIndex();
            this.previousFocusedCol = JTable.this.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            Object oldValue = e.getOldValue();
            Object newValue = e.getNewValue();
            if (name.equals("model")) {
                if (oldValue instanceof TableModel) {
                    TableModel oldModel = (TableModel)oldValue;
                    oldModel.removeTableModelListener(this);
                }
                if (newValue instanceof TableModel) {
                    TableModel newModel = (TableModel)newValue;
                    newModel.addTableModelListener(this);
                }
            } else if (name.equals("selectionModel")) {
                Object source = e.getSource();
                if (source == JTable.this) {
                    if (oldValue instanceof ListSelectionModel) {
                        ListSelectionModel oldModel = (ListSelectionModel)oldValue;
                        oldModel.removeListSelectionListener(this);
                    }
                    if (newValue instanceof ListSelectionModel) {
                        ListSelectionModel newModel = (ListSelectionModel)newValue;
                        newModel.addListSelectionListener(this);
                    }
                } else if (source == JTable.this.getColumnModel()) {
                    if (oldValue instanceof ListSelectionModel) {
                        ListSelectionModel oldModel = (ListSelectionModel)oldValue;
                        oldModel.removeListSelectionListener(this);
                    }
                    if (newValue instanceof ListSelectionModel) {
                        ListSelectionModel newModel = (ListSelectionModel)newValue;
                        newModel.addListSelectionListener(this);
                    }
                }
            } else if (name.equals("columnModel")) {
                TableColumnModel tcm;
                if (oldValue instanceof TableColumnModel) {
                    tcm = (TableColumnModel)oldValue;
                    tcm.removeColumnModelListener(this);
                    tcm.getSelectionModel().removeListSelectionListener(this);
                }
                if (newValue instanceof TableColumnModel) {
                    tcm = (TableColumnModel)newValue;
                    tcm.addColumnModelListener(this);
                    tcm.getSelectionModel().addListSelectionListener(this);
                }
            } else if (name.equals("tableCellEditor")) {
                if (oldValue instanceof TableCellEditor) {
                    TableCellEditor oldEditor = (TableCellEditor)oldValue;
                    oldEditor.removeCellEditorListener(this);
                }
                if (newValue instanceof TableCellEditor) {
                    TableCellEditor newEditor = (TableCellEditor)newValue;
                    newEditor.addCellEditorListener(this);
                }
            }
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            if (e != null) {
                int firstColumn = e.getColumn();
                int lastColumn = e.getColumn();
                if (firstColumn == -1) {
                    firstColumn = 0;
                    lastColumn = JTable.this.getColumnCount() - 1;
                }
                AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, e.getType(), e.getFirstRow(), e.getLastRow(), firstColumn, lastColumn);
                this.firePropertyChange("accessibleTableModelChanged", null, change);
            }
        }

        public void tableRowsInserted(TableModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            int firstColumn = e.getColumn();
            int lastColumn = e.getColumn();
            if (firstColumn == -1) {
                firstColumn = 0;
                lastColumn = JTable.this.getColumnCount() - 1;
            }
            AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, e.getType(), e.getFirstRow(), e.getLastRow(), firstColumn, lastColumn);
            this.firePropertyChange("accessibleTableModelChanged", null, change);
        }

        public void tableRowsDeleted(TableModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            int firstColumn = e.getColumn();
            int lastColumn = e.getColumn();
            if (firstColumn == -1) {
                firstColumn = 0;
                lastColumn = JTable.this.getColumnCount() - 1;
            }
            AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, e.getType(), e.getFirstRow(), e.getLastRow(), firstColumn, lastColumn);
            this.firePropertyChange("accessibleTableModelChanged", null, change);
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            int type = 1;
            AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, type, 0, 0, e.getFromIndex(), e.getToIndex());
            this.firePropertyChange("accessibleTableModelChanged", null, change);
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            int type = -1;
            AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, type, 0, 0, e.getFromIndex(), e.getToIndex());
            this.firePropertyChange("accessibleTableModelChanged", null, change);
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
            int type = -1;
            AccessibleJTableModelChange change = new AccessibleJTableModelChange(this, type, 0, 0, e.getFromIndex(), e.getFromIndex());
            this.firePropertyChange("accessibleTableModelChanged", null, change);
            int type2 = 1;
            AccessibleJTableModelChange change2 = new AccessibleJTableModelChange(this, type2, 0, 0, e.getToIndex(), e.getToIndex());
            this.firePropertyChange("accessibleTableModelChanged", null, change2);
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
        }

        @Override
        public void editingStopped(ChangeEvent e) {
            this.firePropertyChange("AccessibleVisibleData", null, null);
        }

        @Override
        public void editingCanceled(ChangeEvent e) {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            this.firePropertyChange("AccessibleSelection", false, true);
            int focusedRow = JTable.this.getSelectionModel().getLeadSelectionIndex();
            int focusedCol = JTable.this.getColumnModel().getSelectionModel().getLeadSelectionIndex();
            if (focusedRow != this.previousFocusedRow || focusedCol != this.previousFocusedCol) {
                Accessible oldA = this.getAccessibleAt(this.previousFocusedRow, this.previousFocusedCol);
                Accessible newA = this.getAccessibleAt(focusedRow, focusedCol);
                this.firePropertyChange("AccessibleActiveDescendant", oldA, newA);
                this.previousFocusedRow = focusedRow;
                this.previousFocusedCol = focusedCol;
            }
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TABLE;
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            int column = JTable.this.columnAtPoint(p);
            int row = JTable.this.rowAtPoint(p);
            if (column != -1 && row != -1) {
                Component editor;
                if (row == JTable.this.getEditingRow() && column == JTable.this.getEditingColumn() && (editor = JTable.this.getEditorComponent()) instanceof Accessible) {
                    return (Accessible)((Object)editor);
                }
                return new AccessibleJTableCell(JTable.this, row, column, this.getAccessibleIndexAt(row, column));
            }
            return null;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return JTable.this.getColumnCount() * JTable.this.getRowCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            Component editor;
            if (i < 0 || i >= this.getAccessibleChildrenCount()) {
                return null;
            }
            int column = this.getAccessibleColumnAtIndex(i);
            int row = this.getAccessibleRowAtIndex(i);
            if (row == JTable.this.getEditingRow() && column == JTable.this.getEditingColumn() && (editor = JTable.this.getEditorComponent()) instanceof Accessible) {
                return (Accessible)((Object)editor);
            }
            return new AccessibleJTableCell(JTable.this, row, column, this.getAccessibleIndexAt(row, column));
        }

        @Override
        public int getAccessibleSelectionCount() {
            int rowsSel = JTable.this.getSelectedRowCount();
            int colsSel = JTable.this.getSelectedColumnCount();
            if (JTable.this.cellSelectionEnabled) {
                return rowsSel * colsSel;
            }
            if (JTable.this.getRowSelectionAllowed() && JTable.this.getColumnSelectionAllowed()) {
                return rowsSel * JTable.this.getColumnCount() + colsSel * JTable.this.getRowCount() - rowsSel * colsSel;
            }
            if (JTable.this.getRowSelectionAllowed()) {
                return rowsSel * JTable.this.getColumnCount();
            }
            if (JTable.this.getColumnSelectionAllowed()) {
                return colsSel * JTable.this.getRowCount();
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            if (i < 0 || i > this.getAccessibleSelectionCount()) {
                return null;
            }
            int rowsSel = JTable.this.getSelectedRowCount();
            int colsSel = JTable.this.getSelectedColumnCount();
            int[] rowIndicies = JTable.this.getSelectedRows();
            int[] colIndicies = JTable.this.getSelectedColumns();
            int ttlCols = JTable.this.getColumnCount();
            int ttlRows = JTable.this.getRowCount();
            if (JTable.this.cellSelectionEnabled) {
                int r = rowIndicies[i / colsSel];
                int c = colIndicies[i % colsSel];
                return this.getAccessibleChild(r * ttlCols + c);
            }
            if (JTable.this.getRowSelectionAllowed() && JTable.this.getColumnSelectionAllowed()) {
                int curIndex = i;
                boolean IN_ROW = false;
                boolean NOT_IN_ROW = true;
                int state = rowIndicies[0] == 0 ? 0 : 1;
                int j = 0;
                int prevRow = -1;
                while (j < rowIndicies.length) {
                    switch (state) {
                        case 0: {
                            if (curIndex < ttlCols) {
                                int c = curIndex % ttlCols;
                                int r = rowIndicies[j];
                                return this.getAccessibleChild(r * ttlCols + c);
                            }
                            curIndex -= ttlCols;
                            if (j + 1 == rowIndicies.length || rowIndicies[j] != rowIndicies[j + 1] - 1) {
                                state = 1;
                                prevRow = rowIndicies[j];
                            }
                            ++j;
                            break;
                        }
                        case 1: {
                            if (curIndex < colsSel * (rowIndicies[j] - (prevRow == -1 ? 0 : prevRow + 1))) {
                                int c = colIndicies[curIndex % colsSel];
                                int r = (j > 0 ? rowIndicies[j - 1] + 1 : 0) + curIndex / colsSel;
                                return this.getAccessibleChild(r * ttlCols + c);
                            }
                            curIndex -= colsSel * (rowIndicies[j] - (prevRow == -1 ? 0 : prevRow + 1));
                            state = 0;
                        }
                    }
                }
                if (curIndex < colsSel * (ttlRows - (prevRow == -1 ? 0 : prevRow + 1))) {
                    int c = colIndicies[curIndex % colsSel];
                    int r = rowIndicies[j - 1] + curIndex / colsSel + 1;
                    return this.getAccessibleChild(r * ttlCols + c);
                }
            } else {
                if (JTable.this.getRowSelectionAllowed()) {
                    int c = i % ttlCols;
                    int r = rowIndicies[i / ttlCols];
                    return this.getAccessibleChild(r * ttlCols + c);
                }
                if (JTable.this.getColumnSelectionAllowed()) {
                    int c = colIndicies[i % colsSel];
                    int r = i / colsSel;
                    return this.getAccessibleChild(r * ttlCols + c);
                }
            }
            return null;
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            int column = this.getAccessibleColumnAtIndex(i);
            int row = this.getAccessibleRowAtIndex(i);
            return JTable.this.isCellSelected(row, column);
        }

        @Override
        public void addAccessibleSelection(int i) {
            int column = this.getAccessibleColumnAtIndex(i);
            int row = this.getAccessibleRowAtIndex(i);
            JTable.this.changeSelection(row, column, true, false);
        }

        @Override
        public void removeAccessibleSelection(int i) {
            if (JTable.this.cellSelectionEnabled) {
                int column = this.getAccessibleColumnAtIndex(i);
                int row = this.getAccessibleRowAtIndex(i);
                JTable.this.removeRowSelectionInterval(row, row);
                JTable.this.removeColumnSelectionInterval(column, column);
            }
        }

        @Override
        public void clearAccessibleSelection() {
            JTable.this.clearSelection();
        }

        @Override
        public void selectAllAccessibleSelection() {
            if (JTable.this.cellSelectionEnabled) {
                JTable.this.selectAll();
            }
        }

        @Override
        public int getAccessibleRow(int index) {
            return this.getAccessibleRowAtIndex(index);
        }

        @Override
        public int getAccessibleColumn(int index) {
            return this.getAccessibleColumnAtIndex(index);
        }

        @Override
        public int getAccessibleIndex(int r, int c) {
            return this.getAccessibleIndexAt(r, c);
        }

        @Override
        public AccessibleTable getAccessibleTable() {
            return this;
        }

        @Override
        public Accessible getAccessibleCaption() {
            return this.caption;
        }

        @Override
        public void setAccessibleCaption(Accessible a) {
            Accessible oldCaption = this.caption;
            this.caption = a;
            this.firePropertyChange("accessibleTableCaptionChanged", oldCaption, this.caption);
        }

        @Override
        public Accessible getAccessibleSummary() {
            return this.summary;
        }

        @Override
        public void setAccessibleSummary(Accessible a) {
            Accessible oldSummary = this.summary;
            this.summary = a;
            this.firePropertyChange("accessibleTableSummaryChanged", oldSummary, this.summary);
        }

        @Override
        public int getAccessibleRowCount() {
            return JTable.this.getRowCount();
        }

        @Override
        public int getAccessibleColumnCount() {
            return JTable.this.getColumnCount();
        }

        @Override
        public Accessible getAccessibleAt(int r, int c) {
            return this.getAccessibleChild(r * this.getAccessibleColumnCount() + c);
        }

        @Override
        public int getAccessibleRowExtentAt(int r, int c) {
            return 1;
        }

        @Override
        public int getAccessibleColumnExtentAt(int r, int c) {
            return 1;
        }

        @Override
        public AccessibleTable getAccessibleRowHeader() {
            return null;
        }

        @Override
        public void setAccessibleRowHeader(AccessibleTable a) {
        }

        @Override
        public AccessibleTable getAccessibleColumnHeader() {
            JTableHeader header = JTable.this.getTableHeader();
            return header == null ? null : new AccessibleTableHeader(header);
        }

        @Override
        public void setAccessibleColumnHeader(AccessibleTable a) {
        }

        @Override
        public Accessible getAccessibleRowDescription(int r) {
            if (r < 0 || r >= this.getAccessibleRowCount()) {
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if (this.rowDescription == null) {
                return null;
            }
            return this.rowDescription[r];
        }

        @Override
        public void setAccessibleRowDescription(int r, Accessible a) {
            if (r < 0 || r >= this.getAccessibleRowCount()) {
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if (this.rowDescription == null) {
                int numRows = this.getAccessibleRowCount();
                this.rowDescription = new Accessible[numRows];
            }
            this.rowDescription[r] = a;
        }

        @Override
        public Accessible getAccessibleColumnDescription(int c) {
            if (c < 0 || c >= this.getAccessibleColumnCount()) {
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if (this.columnDescription == null) {
                return null;
            }
            return this.columnDescription[c];
        }

        @Override
        public void setAccessibleColumnDescription(int c, Accessible a) {
            if (c < 0 || c >= this.getAccessibleColumnCount()) {
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if (this.columnDescription == null) {
                int numColumns = this.getAccessibleColumnCount();
                this.columnDescription = new Accessible[numColumns];
            }
            this.columnDescription[c] = a;
        }

        @Override
        public boolean isAccessibleSelected(int r, int c) {
            return JTable.this.isCellSelected(r, c);
        }

        @Override
        public boolean isAccessibleRowSelected(int r) {
            return JTable.this.isRowSelected(r);
        }

        @Override
        public boolean isAccessibleColumnSelected(int c) {
            return JTable.this.isColumnSelected(c);
        }

        @Override
        public int[] getSelectedAccessibleRows() {
            return JTable.this.getSelectedRows();
        }

        @Override
        public int[] getSelectedAccessibleColumns() {
            return JTable.this.getSelectedColumns();
        }

        public int getAccessibleRowAtIndex(int i) {
            int columnCount = this.getAccessibleColumnCount();
            if (columnCount == 0) {
                return -1;
            }
            return i / columnCount;
        }

        public int getAccessibleColumnAtIndex(int i) {
            int columnCount = this.getAccessibleColumnCount();
            if (columnCount == 0) {
                return -1;
            }
            return i % columnCount;
        }

        public int getAccessibleIndexAt(int r, int c) {
            return r * this.getAccessibleColumnCount() + c;
        }

        protected class AccessibleJTableModelChange
        implements AccessibleTableModelChange {
            protected int type;
            protected int firstRow;
            protected int lastRow;
            protected int firstColumn;
            protected int lastColumn;

            protected AccessibleJTableModelChange(AccessibleJTable this$1, int type, int firstRow, int lastRow, int firstColumn, int lastColumn) {
                this.type = type;
                this.firstRow = firstRow;
                this.lastRow = lastRow;
                this.firstColumn = firstColumn;
                this.lastColumn = lastColumn;
            }

            @Override
            public int getType() {
                return this.type;
            }

            @Override
            public int getFirstRow() {
                return this.firstRow;
            }

            @Override
            public int getLastRow() {
                return this.lastRow;
            }

            @Override
            public int getFirstColumn() {
                return this.firstColumn;
            }

            @Override
            public int getLastColumn() {
                return this.lastColumn;
            }
        }

        protected class AccessibleJTableCell
        extends AccessibleContext
        implements Accessible,
        AccessibleComponent {
            private JTable parent;
            private int row;
            private int column;
            private int index;

            public AccessibleJTableCell(JTable t, int r, int c, int i) {
                this.parent = t;
                this.row = r;
                this.column = c;
                this.index = i;
                this.setAccessibleParent(this.parent);
            }

            @Override
            public AccessibleContext getAccessibleContext() {
                return this;
            }

            protected AccessibleContext getCurrentAccessibleContext() {
                Component component;
                TableColumn aColumn = JTable.this.getColumnModel().getColumn(this.column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = JTable.this.getColumnClass(this.column);
                    renderer = JTable.this.getDefaultRenderer(columnClass);
                }
                if ((component = renderer.getTableCellRendererComponent(JTable.this, JTable.this.getValueAt(this.row, this.column), false, false, this.row, this.column)) instanceof Accessible) {
                    return component.getAccessibleContext();
                }
                return null;
            }

            protected Component getCurrentComponent() {
                TableColumn aColumn = JTable.this.getColumnModel().getColumn(this.column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = JTable.this.getColumnClass(this.column);
                    renderer = JTable.this.getDefaultRenderer(columnClass);
                }
                return renderer.getTableCellRendererComponent(JTable.this, null, false, false, this.row, this.column);
            }

            @Override
            public String getAccessibleName() {
                String name;
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null && (name = ac.getAccessibleName()) != null && name != "") {
                    return name;
                }
                if (this.accessibleName != null && this.accessibleName != "") {
                    return this.accessibleName;
                }
                return (String)JTable.this.getClientProperty("AccessibleName");
            }

            @Override
            public void setAccessibleName(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                } else {
                    super.setAccessibleName(s);
                }
            }

            @Override
            public String getAccessibleDescription() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                }
                return super.getAccessibleDescription();
            }

            @Override
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                } else {
                    super.setAccessibleDescription(s);
                }
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                }
                return AccessibleRole.UNKNOWN;
            }

            @Override
            public AccessibleStateSet getAccessibleStateSet() {
                Rectangle rcell;
                Rectangle rjt;
                AccessibleContext ac = this.getCurrentAccessibleContext();
                AccessibleStateSet as = null;
                if (ac != null) {
                    as = ac.getAccessibleStateSet();
                }
                if (as == null) {
                    as = new AccessibleStateSet();
                }
                if ((rjt = JTable.this.getVisibleRect()).intersects(rcell = JTable.this.getCellRect(this.row, this.column, false))) {
                    as.add(AccessibleState.SHOWING);
                } else if (as.contains(AccessibleState.SHOWING)) {
                    as.remove(AccessibleState.SHOWING);
                }
                if (this.parent.isCellSelected(this.row, this.column)) {
                    as.add(AccessibleState.SELECTED);
                } else if (as.contains(AccessibleState.SELECTED)) {
                    as.remove(AccessibleState.SELECTED);
                }
                if (this.row == JTable.this.getSelectedRow() && this.column == JTable.this.getSelectedColumn()) {
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }

            @Override
            public Accessible getAccessibleParent() {
                return this.parent;
            }

            @Override
            public int getAccessibleIndexInParent() {
                return this.index;
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
                } else {
                    super.addPropertyChangeListener(l);
                }
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                } else {
                    super.removePropertyChangeListener(l);
                }
            }

            @Override
            public AccessibleAction getAccessibleAction() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleAction();
                }
                return null;
            }

            @Override
            public AccessibleComponent getAccessibleComponent() {
                return this;
            }

            @Override
            public AccessibleSelection getAccessibleSelection() {
                return this.getCurrentAccessibleContext().getAccessibleSelection();
            }

            @Override
            public AccessibleText getAccessibleText() {
                return this.getCurrentAccessibleContext().getAccessibleText();
            }

            @Override
            public AccessibleValue getAccessibleValue() {
                return this.getCurrentAccessibleContext().getAccessibleValue();
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
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).isVisible();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isVisible();
                }
                return false;
            }

            @Override
            public void setVisible(boolean b) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setVisible(b);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setVisible(b);
                    }
                }
            }

            @Override
            public boolean isShowing() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    if (ac.getAccessibleParent() != null) {
                        return ((AccessibleComponent)((Object)ac)).isShowing();
                    }
                    return this.isVisible();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isShowing();
                }
                return false;
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
                if (this.parent != null && this.parent.isShowing()) {
                    Point parentLocation = this.parent.getLocationOnScreen();
                    Point componentLocation = this.getLocation();
                    componentLocation.translate(parentLocation.x, parentLocation.y);
                    return componentLocation;
                }
                return null;
            }

            @Override
            public Point getLocation() {
                Rectangle r;
                if (this.parent != null && (r = this.parent.getCellRect(this.row, this.column, false)) != null) {
                    return r.getLocation();
                }
                return null;
            }

            @Override
            public void setLocation(Point p) {
            }

            @Override
            public Rectangle getBounds() {
                if (this.parent != null) {
                    return this.parent.getCellRect(this.row, this.column, false);
                }
                return null;
            }

            @Override
            public void setBounds(Rectangle r) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setBounds(r);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setBounds(r);
                    }
                }
            }

            @Override
            public Dimension getSize() {
                Rectangle r;
                if (this.parent != null && (r = this.parent.getCellRect(this.row, this.column, false)) != null) {
                    return r.getSize();
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
        }

        private class AccessibleTableHeader
        implements AccessibleTable {
            private JTableHeader header;
            private TableColumnModel headerModel;

            AccessibleTableHeader(JTableHeader header) {
                this.header = header;
                this.headerModel = header.getColumnModel();
            }

            @Override
            public Accessible getAccessibleCaption() {
                return null;
            }

            @Override
            public void setAccessibleCaption(Accessible a) {
            }

            @Override
            public Accessible getAccessibleSummary() {
                return null;
            }

            @Override
            public void setAccessibleSummary(Accessible a) {
            }

            @Override
            public int getAccessibleRowCount() {
                return 1;
            }

            @Override
            public int getAccessibleColumnCount() {
                return this.headerModel.getColumnCount();
            }

            @Override
            public Accessible getAccessibleAt(int row, int column) {
                TableColumn aColumn = this.headerModel.getColumn(column);
                TableCellRenderer renderer = aColumn.getHeaderRenderer();
                if (renderer == null) {
                    renderer = this.header.getDefaultRenderer();
                }
                Component component = renderer.getTableCellRendererComponent(this.header.getTable(), aColumn.getHeaderValue(), false, false, -1, column);
                return new AccessibleJTableHeaderCell(row, column, JTable.this.getTableHeader(), component);
            }

            @Override
            public int getAccessibleRowExtentAt(int r, int c) {
                return 1;
            }

            @Override
            public int getAccessibleColumnExtentAt(int r, int c) {
                return 1;
            }

            @Override
            public AccessibleTable getAccessibleRowHeader() {
                return null;
            }

            @Override
            public void setAccessibleRowHeader(AccessibleTable table) {
            }

            @Override
            public AccessibleTable getAccessibleColumnHeader() {
                return null;
            }

            @Override
            public void setAccessibleColumnHeader(AccessibleTable table) {
            }

            @Override
            public Accessible getAccessibleRowDescription(int r) {
                return null;
            }

            @Override
            public void setAccessibleRowDescription(int r, Accessible a) {
            }

            @Override
            public Accessible getAccessibleColumnDescription(int c) {
                return null;
            }

            @Override
            public void setAccessibleColumnDescription(int c, Accessible a) {
            }

            @Override
            public boolean isAccessibleSelected(int r, int c) {
                return false;
            }

            @Override
            public boolean isAccessibleRowSelected(int r) {
                return false;
            }

            @Override
            public boolean isAccessibleColumnSelected(int c) {
                return false;
            }

            @Override
            public int[] getSelectedAccessibleRows() {
                return new int[0];
            }

            @Override
            public int[] getSelectedAccessibleColumns() {
                return new int[0];
            }
        }

        private class AccessibleJTableHeaderCell
        extends AccessibleContext
        implements Accessible,
        AccessibleComponent {
            private int row;
            private int column;
            private JTableHeader parent;
            private Component rendererComponent;

            public AccessibleJTableHeaderCell(int row, int column, JTableHeader parent, Component rendererComponent) {
                this.row = row;
                this.column = column;
                this.parent = parent;
                this.rendererComponent = rendererComponent;
                this.setAccessibleParent(parent);
            }

            @Override
            public AccessibleContext getAccessibleContext() {
                return this;
            }

            private AccessibleContext getCurrentAccessibleContext() {
                return this.rendererComponent.getAccessibleContext();
            }

            private Component getCurrentComponent() {
                return this.rendererComponent;
            }

            @Override
            public String getAccessibleName() {
                String name;
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null && (name = ac.getAccessibleName()) != null && name != "") {
                    return ac.getAccessibleName();
                }
                if (this.accessibleName != null && this.accessibleName != "") {
                    return this.accessibleName;
                }
                return null;
            }

            @Override
            public void setAccessibleName(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                } else {
                    super.setAccessibleName(s);
                }
            }

            @Override
            public String getAccessibleDescription() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                }
                return super.getAccessibleDescription();
            }

            @Override
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                } else {
                    super.setAccessibleDescription(s);
                }
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                }
                return AccessibleRole.UNKNOWN;
            }

            @Override
            public AccessibleStateSet getAccessibleStateSet() {
                Rectangle rcell;
                Rectangle rjt;
                AccessibleContext ac = this.getCurrentAccessibleContext();
                AccessibleStateSet as = null;
                if (ac != null) {
                    as = ac.getAccessibleStateSet();
                }
                if (as == null) {
                    as = new AccessibleStateSet();
                }
                if ((rjt = JTable.this.getVisibleRect()).intersects(rcell = JTable.this.getCellRect(this.row, this.column, false))) {
                    as.add(AccessibleState.SHOWING);
                } else if (as.contains(AccessibleState.SHOWING)) {
                    as.remove(AccessibleState.SHOWING);
                }
                if (JTable.this.isCellSelected(this.row, this.column)) {
                    as.add(AccessibleState.SELECTED);
                } else if (as.contains(AccessibleState.SELECTED)) {
                    as.remove(AccessibleState.SELECTED);
                }
                if (this.row == JTable.this.getSelectedRow() && this.column == JTable.this.getSelectedColumn()) {
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }

            @Override
            public Accessible getAccessibleParent() {
                return this.parent;
            }

            @Override
            public int getAccessibleIndexInParent() {
                return this.column;
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
                } else {
                    super.addPropertyChangeListener(l);
                }
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                } else {
                    super.removePropertyChangeListener(l);
                }
            }

            @Override
            public AccessibleAction getAccessibleAction() {
                return this.getCurrentAccessibleContext().getAccessibleAction();
            }

            @Override
            public AccessibleComponent getAccessibleComponent() {
                return this;
            }

            @Override
            public AccessibleSelection getAccessibleSelection() {
                return this.getCurrentAccessibleContext().getAccessibleSelection();
            }

            @Override
            public AccessibleText getAccessibleText() {
                return this.getCurrentAccessibleContext().getAccessibleText();
            }

            @Override
            public AccessibleValue getAccessibleValue() {
                return this.getCurrentAccessibleContext().getAccessibleValue();
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
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent)((Object)ac)).isVisible();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isVisible();
                }
                return false;
            }

            @Override
            public void setVisible(boolean b) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setVisible(b);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setVisible(b);
                    }
                }
            }

            @Override
            public boolean isShowing() {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    if (ac.getAccessibleParent() != null) {
                        return ((AccessibleComponent)((Object)ac)).isShowing();
                    }
                    return this.isVisible();
                }
                Component c = this.getCurrentComponent();
                if (c != null) {
                    return c.isShowing();
                }
                return false;
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
                if (this.parent != null && this.parent.isShowing()) {
                    Point parentLocation = this.parent.getLocationOnScreen();
                    Point componentLocation = this.getLocation();
                    componentLocation.translate(parentLocation.x, parentLocation.y);
                    return componentLocation;
                }
                return null;
            }

            @Override
            public Point getLocation() {
                Rectangle r;
                if (this.parent != null && (r = this.parent.getHeaderRect(this.column)) != null) {
                    return r.getLocation();
                }
                return null;
            }

            @Override
            public void setLocation(Point p) {
            }

            @Override
            public Rectangle getBounds() {
                if (this.parent != null) {
                    return this.parent.getHeaderRect(this.column);
                }
                return null;
            }

            @Override
            public void setBounds(Rectangle r) {
                AccessibleContext ac = this.getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent)((Object)ac)).setBounds(r);
                } else {
                    Component c = this.getCurrentComponent();
                    if (c != null) {
                        c.setBounds(r);
                    }
                }
            }

            @Override
            public Dimension getSize() {
                Rectangle r;
                if (this.parent != null && (r = this.parent.getHeaderRect(this.column)) != null) {
                    return r.getSize();
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
        }
    }

    static class BooleanEditor
    extends DefaultCellEditor {
        public BooleanEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox)this.getComponent();
            checkBox.setHorizontalAlignment(0);
        }
    }

    static class NumberEditor
    extends GenericEditor {
        public NumberEditor() {
            ((JTextField)this.getComponent()).setHorizontalAlignment(4);
        }
    }

    static class GenericEditor
    extends DefaultCellEditor {
        Class<?>[] argTypes = new Class[]{String.class};
        Constructor<?> constructor;
        Object value;

        public GenericEditor() {
            super(new JTextField());
            this.getComponent().setName("Table.editor");
        }

        @Override
        public boolean stopCellEditing() {
            String s = (String)super.getCellEditorValue();
            try {
                if ("".equals(s)) {
                    if (this.constructor.getDeclaringClass() == String.class) {
                        this.value = s;
                    }
                    return super.stopCellEditing();
                }
                SwingUtilities2.checkAccess(this.constructor.getModifiers());
                this.value = this.constructor.newInstance(s);
            }
            catch (Exception e) {
                ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.value = null;
            ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.black));
            try {
                Class<Object> type = table.getColumnClass(column);
                if (type == Object.class) {
                    type = String.class;
                }
                ReflectUtil.checkPackageAccess(type);
                SwingUtilities2.checkAccess(type.getModifiers());
                this.constructor = type.getConstructor(this.argTypes);
            }
            catch (Exception e) {
                return null;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return this.value;
        }
    }

    static class BooleanRenderer
    extends JCheckBox
    implements TableCellRenderer,
    UIResource {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public BooleanRenderer() {
            this.setHorizontalAlignment(0);
            this.setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                this.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                this.setForeground(table.getForeground());
                this.setBackground(table.getBackground());
            }
            this.setSelected(value != null && (Boolean)value != false);
            if (hasFocus) {
                this.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                this.setBorder(noFocusBorder);
            }
            return this;
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (this.accessibleContext == null) {
                this.accessibleContext = new AccessibleBooleanRenderer();
            }
            return this.accessibleContext;
        }

        class AccessibleBooleanRenderer
        extends JCheckBox.AccessibleJCheckBox {
            AccessibleBooleanRenderer() {
                super(BooleanRenderer.this);
            }

            @Override
            public AccessibleAction getAccessibleAction() {
                return null;
            }
        }
    }

    static class IconRenderer
    extends DefaultTableCellRenderer.UIResource {
        public IconRenderer() {
            this.setHorizontalAlignment(0);
        }

        @Override
        public void setValue(Object value) {
            this.setIcon(value instanceof Icon ? (Icon)value : null);
        }
    }

    static class DateRenderer
    extends DefaultTableCellRenderer.UIResource {
        DateFormat formatter;

        @Override
        public void setValue(Object value) {
            if (this.formatter == null) {
                this.formatter = DateFormat.getDateInstance();
            }
            this.setText(value == null ? "" : this.formatter.format(value));
        }
    }

    static class DoubleRenderer
    extends NumberRenderer {
        NumberFormat formatter;

        @Override
        public void setValue(Object value) {
            if (this.formatter == null) {
                this.formatter = NumberFormat.getInstance();
            }
            this.setText(value == null ? "" : this.formatter.format(value));
        }
    }

    static class NumberRenderer
    extends DefaultTableCellRenderer.UIResource {
        public NumberRenderer() {
            this.setHorizontalAlignment(4);
        }
    }
}

