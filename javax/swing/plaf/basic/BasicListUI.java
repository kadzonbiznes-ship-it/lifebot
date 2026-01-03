/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.BasicTransferable;
import javax.swing.plaf.basic.DragRecognitionSupport;
import javax.swing.plaf.basic.LazyActionMap;
import javax.swing.text.Position;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class BasicListUI
extends ListUI {
    private static final StringBuilder BASELINE_COMPONENT_KEY = new StringBuilder("List.baselineComponent");
    protected JList<Object> list = null;
    protected CellRendererPane rendererPane;
    protected FocusListener focusListener;
    protected MouseInputListener mouseInputListener;
    protected ListSelectionListener listSelectionListener;
    protected ListDataListener listDataListener;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;
    protected int[] cellHeights = null;
    protected int cellHeight = -1;
    protected int cellWidth = -1;
    protected int updateLayoutStateNeeded = 1;
    private int listHeight;
    private int listWidth;
    private int layoutOrientation;
    private int columnCount;
    private int preferredHeight;
    private int rowsPerColumn;
    private long timeFactor = 1000L;
    private boolean isFileList = false;
    private boolean isLeftToRight = true;
    protected static final int modelChanged = 1;
    protected static final int selectionModelChanged = 2;
    protected static final int fontChanged = 4;
    protected static final int fixedCellWidthChanged = 8;
    protected static final int fixedCellHeightChanged = 16;
    protected static final int prototypeCellValueChanged = 32;
    protected static final int cellRendererChanged = 64;
    private static final int layoutOrientationChanged = 128;
    private static final int heightChanged = 256;
    private static final int widthChanged = 512;
    private static final int componentOrientationChanged = 1024;
    private static final int DROP_LINE_THICKNESS = 2;
    private static final int CHANGE_LEAD = 0;
    private static final int CHANGE_SELECTION = 1;
    private static final int EXTEND_SELECTION = 2;
    private static final TransferHandler defaultTransferHandler = new ListTransferHandler();

    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions("selectPreviousColumn"));
        map.put(new Actions("selectPreviousColumnExtendSelection"));
        map.put(new Actions("selectPreviousColumnChangeLead"));
        map.put(new Actions("selectNextColumn"));
        map.put(new Actions("selectNextColumnExtendSelection"));
        map.put(new Actions("selectNextColumnChangeLead"));
        map.put(new Actions("selectPreviousRow"));
        map.put(new Actions("selectPreviousRowExtendSelection"));
        map.put(new Actions("selectPreviousRowChangeLead"));
        map.put(new Actions("selectNextRow"));
        map.put(new Actions("selectNextRowExtendSelection"));
        map.put(new Actions("selectNextRowChangeLead"));
        map.put(new Actions("selectFirstRow"));
        map.put(new Actions("selectFirstRowExtendSelection"));
        map.put(new Actions("selectFirstRowChangeLead"));
        map.put(new Actions("selectLastRow"));
        map.put(new Actions("selectLastRowExtendSelection"));
        map.put(new Actions("selectLastRowChangeLead"));
        map.put(new Actions("scrollUp"));
        map.put(new Actions("scrollUpExtendSelection"));
        map.put(new Actions("scrollUpChangeLead"));
        map.put(new Actions("scrollDown"));
        map.put(new Actions("scrollDownExtendSelection"));
        map.put(new Actions("scrollDownChangeLead"));
        map.put(new Actions("selectAll"));
        map.put(new Actions("clearSelection"));
        map.put(new Actions("addToSelection"));
        map.put(new Actions("toggleAndAnchor"));
        map.put(new Actions("extendTo"));
        map.put(new Actions("moveSelectionTo"));
        map.put(TransferHandler.getCutAction().getValue("Name"), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue("Name"), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue("Name"), TransferHandler.getPasteAction());
    }

    protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer<Object> cellRenderer, ListModel<Object> dataModel, ListSelectionModel selModel, int leadIndex) {
        Object value = dataModel.getElementAt(row);
        boolean cellHasFocus = this.list.hasFocus() && row == leadIndex;
        boolean isSelected = selModel.isSelectedIndex(row);
        Component rendererComponent = cellRenderer.getListCellRendererComponent(this.list, value, row, isSelected, cellHasFocus);
        int cx = rowBounds.x;
        int cy = rowBounds.y;
        int cw = rowBounds.width;
        int ch = rowBounds.height;
        if (this.isFileList) {
            int w = Math.min(cw, rendererComponent.getPreferredSize().width + 4);
            if (!this.isLeftToRight) {
                cx += cw - w;
            }
            cw = w;
        }
        this.rendererPane.paintComponent(g, rendererComponent, this.list, cx, cy, cw, ch, true);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Shape clip = g.getClip();
        this.paintImpl(g, c);
        g.setClip(clip);
        this.paintDropLine(g);
    }

    private void paintImpl(Graphics g, JComponent c) {
        int endColumn;
        int size;
        switch (this.layoutOrientation) {
            case 1: {
                if (this.list.getHeight() == this.listHeight) break;
                this.updateLayoutStateNeeded |= 0x100;
                this.redrawList();
                break;
            }
            case 2: {
                if (this.list.getWidth() == this.listWidth) break;
                this.updateLayoutStateNeeded |= 0x200;
                this.redrawList();
                break;
            }
        }
        this.maybeUpdateLayoutState();
        ListCellRenderer<Object> renderer = this.list.getCellRenderer();
        ListModel<Object> dataModel = this.list.getModel();
        ListSelectionModel selModel = this.list.getSelectionModel();
        if (renderer == null || (size = dataModel.getSize()) == 0) {
            return;
        }
        Rectangle paintBounds = g.getClipBounds();
        if (c.getComponentOrientation().isLeftToRight()) {
            startColumn = this.convertLocationToColumn(paintBounds.x, paintBounds.y);
            endColumn = this.convertLocationToColumn(paintBounds.x + paintBounds.width, paintBounds.y);
        } else {
            startColumn = this.convertLocationToColumn(paintBounds.x + paintBounds.width, paintBounds.y);
            endColumn = this.convertLocationToColumn(paintBounds.x, paintBounds.y);
        }
        int maxY = paintBounds.y + paintBounds.height;
        int leadIndex = BasicListUI.adjustIndex(this.list.getLeadSelectionIndex(), this.list);
        int rowIncrement = this.layoutOrientation == 2 ? this.columnCount : 1;
        for (int colCounter = startColumn; colCounter <= endColumn; ++colCounter) {
            int row;
            int rowCount = this.getRowCount(colCounter);
            int index = this.getModelIndex(colCounter, row);
            Rectangle rowBounds = this.getCellBounds(this.list, index, index);
            if (rowBounds == null) {
                return;
            }
            for (row = this.convertLocationToRowInColumn(paintBounds.y, colCounter); row < rowCount && rowBounds.y < maxY && index < size; index += rowIncrement, ++row) {
                rowBounds.height = this.getHeight(colCounter, row);
                g.setClip(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
                g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);
                this.paintCell(g, index, rowBounds, renderer, dataModel, selModel, leadIndex);
                rowBounds.y += rowBounds.height;
            }
        }
        this.rendererPane.removeAll();
    }

    private void paintDropLine(Graphics g) {
        JList.DropLocation loc = this.list.getDropLocation();
        if (loc == null || !loc.isInsert()) {
            return;
        }
        Color c = DefaultLookup.getColor(this.list, this, "List.dropLineColor", null);
        if (c != null) {
            g.setColor(c);
            Rectangle rect = this.getDropLineRect(loc);
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    private Rectangle getDropLineRect(JList.DropLocation loc) {
        int size = this.list.getModel().getSize();
        if (size == 0) {
            Insets insets = this.list.getInsets();
            if (this.layoutOrientation == 2) {
                if (this.isLeftToRight) {
                    return new Rectangle(insets.left, insets.top, 2, 20);
                }
                return new Rectangle(this.list.getWidth() - 2 - insets.right, insets.top, 2, 20);
            }
            return new Rectangle(insets.left, insets.top, this.list.getWidth() - insets.left - insets.right, 2);
        }
        Rectangle rect = null;
        int index = loc.getIndex();
        boolean decr = false;
        if (this.layoutOrientation == 2) {
            if (index == size) {
                decr = true;
            } else if (index != 0 && this.convertModelToRow(index) != this.convertModelToRow(index - 1)) {
                Rectangle prev = this.getCellBounds(this.list, index - 1);
                Rectangle me = this.getCellBounds(this.list, index);
                Point p = loc.getDropPoint();
                if (this.isLeftToRight) {
                    decr = Point2D.distance(prev.x + prev.width, prev.y + (int)((double)prev.height / 2.0), p.x, p.y) < Point2D.distance(me.x, me.y + (int)((double)me.height / 2.0), p.x, p.y);
                } else {
                    boolean bl = decr = Point2D.distance(prev.x, prev.y + (int)((double)prev.height / 2.0), p.x, p.y) < Point2D.distance(me.x + me.width, me.y + (int)((double)prev.height / 2.0), p.x, p.y);
                }
            }
            if (decr) {
                rect = this.getCellBounds(this.list, --index);
                rect.x = this.isLeftToRight ? (rect.x += rect.width) : (rect.x -= 2);
            } else {
                rect = this.getCellBounds(this.list, index);
                if (!this.isLeftToRight) {
                    rect.x += rect.width - 2;
                }
            }
            if (rect.x >= this.list.getWidth()) {
                rect.x = this.list.getWidth() - 2;
            } else if (rect.x < 0) {
                rect.x = 0;
            }
            rect.width = 2;
        } else if (this.layoutOrientation == 1) {
            if (index == size) {
                rect = this.getCellBounds(this.list, --index);
                rect.y += rect.height;
            } else if (index != 0 && this.convertModelToColumn(index) != this.convertModelToColumn(index - 1)) {
                Rectangle prev = this.getCellBounds(this.list, index - 1);
                Rectangle me = this.getCellBounds(this.list, index);
                Point p = loc.getDropPoint();
                if (Point2D.distance(prev.x + (int)((double)prev.width / 2.0), prev.y + prev.height, p.x, p.y) < Point2D.distance(me.x + (int)((double)me.width / 2.0), me.y, p.x, p.y)) {
                    rect = this.getCellBounds(this.list, --index);
                    rect.y += rect.height;
                } else {
                    rect = this.getCellBounds(this.list, index);
                }
            } else {
                rect = this.getCellBounds(this.list, index);
            }
            if (rect.y >= this.list.getHeight()) {
                rect.y = this.list.getHeight() - 2;
            }
            rect.height = 2;
        } else {
            if (index == size) {
                rect = this.getCellBounds(this.list, --index);
                rect.y += rect.height;
            } else {
                rect = this.getCellBounds(this.list, index);
            }
            if (rect.y >= this.list.getHeight()) {
                rect.y = this.list.getHeight() - 2;
            }
            rect.height = 2;
        }
        return rect;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int rowHeight = this.list.getFixedCellHeight();
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        Component renderer = (Component)lafDefaults.get(BASELINE_COMPONENT_KEY);
        if (renderer == null) {
            ListCellRenderer lcr = (ListCellRenderer)UIManager.get("List.cellRenderer");
            if (lcr == null) {
                lcr = new DefaultListCellRenderer();
            }
            renderer = lcr.getListCellRendererComponent(this.list, "a", -1, false, false);
            lafDefaults.put(BASELINE_COMPONENT_KEY, renderer);
        }
        renderer.setFont(this.list.getFont());
        if (rowHeight == -1) {
            rowHeight = renderer.getPreferredSize().height;
        }
        return renderer.getBaseline(Integer.MAX_VALUE, rowHeight) + this.list.getInsets().top;
    }

    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Rectangle bounds;
        this.maybeUpdateLayoutState();
        int lastRow = this.list.getModel().getSize() - 1;
        if (lastRow < 0) {
            return new Dimension(0, 0);
        }
        Insets insets = this.list.getInsets();
        int width = this.cellWidth * this.columnCount + insets.left + insets.right;
        int height = this.layoutOrientation != 0 ? this.preferredHeight : ((bounds = this.getCellBounds(this.list, lastRow)) != null ? bounds.y + bounds.height + insets.bottom : 0);
        return new Dimension(width, height);
    }

    protected void selectPreviousIndex() {
        int s = this.list.getSelectedIndex();
        if (s > 0) {
            this.list.setSelectedIndex(--s);
            this.list.ensureIndexIsVisible(s);
        }
    }

    protected void selectNextIndex() {
        int s = this.list.getSelectedIndex();
        if (s + 1 < this.list.getModel().getSize()) {
            this.list.setSelectedIndex(++s);
            this.list.ensureIndexIsVisible(s);
        }
    }

    protected void installKeyboardActions() {
        InputMap inputMap = this.getInputMap(0);
        SwingUtilities.replaceUIInputMap(this.list, 0, inputMap);
        LazyActionMap.installLazyActionMap(this.list, BasicListUI.class, "List.actionMap");
    }

    InputMap getInputMap(int condition) {
        if (condition == 0) {
            InputMap rtlKeyMap;
            InputMap keyMap = (InputMap)DefaultLookup.get(this.list, this, "List.focusInputMap");
            if (this.isLeftToRight || (rtlKeyMap = (InputMap)DefaultLookup.get(this.list, this, "List.focusInputMap.RightToLeft")) == null) {
                return keyMap;
            }
            rtlKeyMap.setParent(keyMap);
            return rtlKeyMap;
        }
        return null;
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(this.list, null);
        SwingUtilities.replaceUIInputMap(this.list, 0, null);
    }

    protected void installListeners() {
        ListSelectionModel selectionModel;
        TransferHandler th = this.list.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            this.list.setTransferHandler(defaultTransferHandler);
            if (this.list.getDropTarget() instanceof UIResource) {
                this.list.setDropTarget(null);
            }
        }
        this.focusListener = this.createFocusListener();
        this.mouseInputListener = this.createMouseInputListener();
        this.propertyChangeListener = this.createPropertyChangeListener();
        this.listSelectionListener = this.createListSelectionListener();
        this.listDataListener = this.createListDataListener();
        this.list.addFocusListener(this.focusListener);
        this.list.addMouseListener(this.mouseInputListener);
        this.list.addMouseMotionListener(this.mouseInputListener);
        this.list.addPropertyChangeListener(this.propertyChangeListener);
        this.list.addKeyListener(this.getHandler());
        ListModel<Object> model = this.list.getModel();
        if (model != null) {
            model.addListDataListener(this.listDataListener);
        }
        if ((selectionModel = this.list.getSelectionModel()) != null) {
            selectionModel.addListSelectionListener(this.listSelectionListener);
        }
    }

    protected void uninstallListeners() {
        ListSelectionModel selectionModel;
        this.list.removeFocusListener(this.focusListener);
        this.list.removeMouseListener(this.mouseInputListener);
        this.list.removeMouseMotionListener(this.mouseInputListener);
        this.list.removePropertyChangeListener(this.propertyChangeListener);
        this.list.removeKeyListener(this.getHandler());
        ListModel<Object> model = this.list.getModel();
        if (model != null) {
            model.removeListDataListener(this.listDataListener);
        }
        if ((selectionModel = this.list.getSelectionModel()) != null) {
            selectionModel.removeListSelectionListener(this.listSelectionListener);
        }
        this.focusListener = null;
        this.mouseInputListener = null;
        this.listSelectionListener = null;
        this.listDataListener = null;
        this.propertyChangeListener = null;
        this.handler = null;
    }

    protected void installDefaults() {
        Long l;
        Color sfg;
        Color sbg;
        this.list.setLayout(null);
        LookAndFeel.installBorder(this.list, "List.border");
        LookAndFeel.installColorsAndFont(this.list, "List.background", "List.foreground", "List.font");
        LookAndFeel.installProperty(this.list, "opaque", Boolean.TRUE);
        if (this.list.getCellRenderer() == null) {
            ListCellRenderer tmp = (ListCellRenderer)UIManager.get("List.cellRenderer");
            this.list.setCellRenderer(tmp);
        }
        if ((sbg = this.list.getSelectionBackground()) == null || sbg instanceof UIResource) {
            this.list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        }
        if ((sfg = this.list.getSelectionForeground()) == null || sfg instanceof UIResource) {
            this.list.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
        }
        this.timeFactor = (l = (Long)UIManager.get("List.timeFactor")) != null ? l : 1000L;
        this.updateIsFileList();
    }

    private void updateIsFileList() {
        boolean b = Boolean.TRUE.equals(this.list.getClientProperty("List.isFileList"));
        if (b != this.isFileList) {
            Font newFont;
            this.isFileList = b;
            Font oldFont = this.list.getFont();
            if ((oldFont == null || oldFont instanceof UIResource) && (newFont = UIManager.getFont(b ? "FileChooser.listFont" : "List.font")) != null && newFont != oldFont) {
                this.list.setFont(newFont);
            }
        }
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(this.list);
        if (this.list.getFont() instanceof UIResource) {
            this.list.setFont(null);
        }
        if (this.list.getForeground() instanceof UIResource) {
            this.list.setForeground(null);
        }
        if (this.list.getBackground() instanceof UIResource) {
            this.list.setBackground(null);
        }
        if (this.list.getSelectionBackground() instanceof UIResource) {
            this.list.setSelectionBackground(null);
        }
        if (this.list.getSelectionForeground() instanceof UIResource) {
            this.list.setSelectionForeground(null);
        }
        if (this.list.getCellRenderer() instanceof UIResource) {
            this.list.setCellRenderer(null);
        }
        if (this.list.getTransferHandler() instanceof UIResource) {
            this.list.setTransferHandler(null);
        }
    }

    @Override
    public void installUI(JComponent c) {
        JList tmp;
        this.list = tmp = (JList)c;
        this.layoutOrientation = this.list.getLayoutOrientation();
        this.rendererPane = new CellRendererPane();
        this.list.add(this.rendererPane);
        this.columnCount = 1;
        this.updateLayoutStateNeeded = 1;
        this.isLeftToRight = this.list.getComponentOrientation().isLeftToRight();
        this.installDefaults();
        this.installListeners();
        this.installKeyboardActions();
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.uninstallListeners();
        this.uninstallDefaults();
        this.uninstallKeyboardActions();
        this.cellHeight = -1;
        this.cellWidth = -1;
        this.cellHeights = null;
        this.listHeight = -1;
        this.listWidth = -1;
        this.list.remove(this.rendererPane);
        this.rendererPane = null;
        this.list = null;
    }

    public static ComponentUI createUI(JComponent list) {
        return new BasicListUI();
    }

    @Override
    public int locationToIndex(JList<?> list, Point location) {
        this.maybeUpdateLayoutState();
        return this.convertLocationToModel(location.x, location.y);
    }

    @Override
    public Point indexToLocation(JList<?> list, int index) {
        this.maybeUpdateLayoutState();
        Rectangle rect = this.getCellBounds(list, index, index);
        if (rect != null) {
            return new Point(rect.x, rect.y);
        }
        return null;
    }

    @Override
    public Rectangle getCellBounds(JList<?> list, int index1, int index2) {
        this.maybeUpdateLayoutState();
        int minIndex = Math.min(index1, index2);
        int maxIndex = Math.max(index1, index2);
        if (minIndex >= list.getModel().getSize()) {
            return null;
        }
        Rectangle minBounds = this.getCellBounds(list, minIndex);
        if (minBounds == null) {
            return null;
        }
        if (minIndex == maxIndex) {
            return minBounds;
        }
        Rectangle maxBounds = this.getCellBounds(list, maxIndex);
        if (maxBounds != null) {
            if (this.layoutOrientation == 2) {
                int maxRow;
                int minRow = this.convertModelToRow(minIndex);
                if (minRow != (maxRow = this.convertModelToRow(maxIndex))) {
                    minBounds.x = 0;
                    minBounds.width = list.getWidth();
                }
            } else if (minBounds.x != maxBounds.x) {
                minBounds.y = 0;
                minBounds.height = list.getHeight();
            }
            minBounds.add(maxBounds);
        }
        return minBounds;
    }

    private Rectangle getCellBounds(JList<?> list, int index) {
        int x;
        this.maybeUpdateLayoutState();
        int row = this.convertModelToRow(index);
        int column = this.convertModelToColumn(index);
        if (row == -1 || column == -1) {
            return null;
        }
        Insets insets = list.getInsets();
        int w = this.cellWidth;
        int y = insets.top;
        return new Rectangle(x, y, w, switch (this.layoutOrientation) {
            case 1, 2 -> {
                x = this.isLeftToRight ? insets.left + column * this.cellWidth : list.getWidth() - insets.right - (column + 1) * this.cellWidth;
                y += this.cellHeight * row;
                yield this.cellHeight;
            }
            default -> {
                x = insets.left;
                if (this.cellHeights == null) {
                    y += this.cellHeight * row;
                } else if (row >= this.cellHeights.length) {
                    y = 0;
                } else {
                    for (int i = 0; i < row; ++i) {
                        y += this.cellHeights[i];
                    }
                }
                w = list.getWidth() - (insets.left + insets.right);
                yield this.getRowHeight(index);
            }
        });
    }

    protected int getRowHeight(int row) {
        return this.getHeight(0, row);
    }

    protected int convertYToRow(int y0) {
        return this.convertLocationToRow(0, y0, false);
    }

    protected int convertRowToY(int row) {
        if (row >= this.getRowCount(0) || row < 0) {
            return -1;
        }
        Rectangle bounds = this.getCellBounds(this.list, row, row);
        return bounds.y;
    }

    private int getHeight(int column, int row) {
        if (column < 0 || column > this.columnCount || row < 0) {
            return -1;
        }
        if (this.layoutOrientation != 0) {
            return this.cellHeight;
        }
        if (row >= this.list.getModel().getSize()) {
            return -1;
        }
        return this.cellHeights == null ? this.cellHeight : (row < this.cellHeights.length ? this.cellHeights[row] : -1);
    }

    private int convertLocationToRow(int x, int y0, boolean closest) {
        int i;
        int size = this.list.getModel().getSize();
        if (size <= 0) {
            return -1;
        }
        Insets insets = this.list.getInsets();
        if (this.cellHeights == null) {
            int row;
            int n = row = this.cellHeight == 0 ? 0 : (y0 - insets.top) / this.cellHeight;
            if (closest) {
                if (row < 0) {
                    row = 0;
                } else if (row >= size) {
                    row = size - 1;
                }
            }
            return row;
        }
        if (size > this.cellHeights.length) {
            return -1;
        }
        int y = insets.top;
        int row = 0;
        if (closest && y0 < y) {
            return 0;
        }
        for (i = 0; i < size; ++i) {
            if (y0 >= y && y0 < y + this.cellHeights[i]) {
                return row;
            }
            y += this.cellHeights[i];
            ++row;
        }
        return i - 1;
    }

    private int convertLocationToRowInColumn(int y, int column) {
        int x = 0;
        if (this.layoutOrientation != 0) {
            x = this.isLeftToRight ? column * this.cellWidth : this.list.getWidth() - (column + 1) * this.cellWidth - this.list.getInsets().right;
        }
        return this.convertLocationToRow(x, y, true);
    }

    private int convertLocationToModel(int x, int y) {
        int row = this.convertLocationToRow(x, y, true);
        int column = this.convertLocationToColumn(x, y);
        if (row >= 0 && column >= 0) {
            return this.getModelIndex(column, row);
        }
        return -1;
    }

    private int getRowCount(int column) {
        if (column < 0 || column >= this.columnCount) {
            return -1;
        }
        if (this.layoutOrientation == 0 || column == 0 && this.columnCount == 1) {
            return this.list.getModel().getSize();
        }
        if (column >= this.columnCount) {
            return -1;
        }
        if (this.layoutOrientation == 1) {
            if (column < this.columnCount - 1) {
                return this.rowsPerColumn;
            }
            return this.list.getModel().getSize() - (this.columnCount - 1) * this.rowsPerColumn;
        }
        int diff = this.columnCount - (this.columnCount * this.rowsPerColumn - this.list.getModel().getSize());
        if (column >= diff) {
            return Math.max(0, this.rowsPerColumn - 1);
        }
        return this.rowsPerColumn;
    }

    private int getModelIndex(int column, int row) {
        switch (this.layoutOrientation) {
            case 1: {
                return Math.min(this.list.getModel().getSize() - 1, this.rowsPerColumn * column + Math.min(row, this.rowsPerColumn - 1));
            }
            case 2: {
                return Math.min(this.list.getModel().getSize() - 1, row * this.columnCount + column);
            }
        }
        return row;
    }

    private int convertLocationToColumn(int x, int y) {
        if (this.cellWidth > 0) {
            if (this.layoutOrientation == 0) {
                return 0;
            }
            Insets insets = this.list.getInsets();
            int col = this.isLeftToRight ? (x - insets.left) / this.cellWidth : (this.list.getWidth() - x - insets.right - 1) / this.cellWidth;
            if (col < 0) {
                return 0;
            }
            if (col >= this.columnCount) {
                return this.columnCount - 1;
            }
            return col;
        }
        return 0;
    }

    private int convertModelToRow(int index) {
        int size = this.list.getModel().getSize();
        if (index < 0 || index >= size) {
            return -1;
        }
        if (this.layoutOrientation != 0 && this.columnCount > 1 && this.rowsPerColumn > 0) {
            if (this.layoutOrientation == 1) {
                return index % this.rowsPerColumn;
            }
            return index / this.columnCount;
        }
        return index;
    }

    private int convertModelToColumn(int index) {
        int size = this.list.getModel().getSize();
        if (index < 0 || index >= size) {
            return -1;
        }
        if (this.layoutOrientation != 0 && this.rowsPerColumn > 0 && this.columnCount > 1) {
            if (this.layoutOrientation == 1) {
                return index / this.rowsPerColumn;
            }
            return index % this.columnCount;
        }
        return 0;
    }

    protected void maybeUpdateLayoutState() {
        if (this.updateLayoutStateNeeded != 0) {
            this.updateLayoutState();
            this.updateLayoutStateNeeded = 0;
        }
    }

    protected void updateLayoutState() {
        int fixedCellHeight = this.list.getFixedCellHeight();
        int fixedCellWidth = this.list.getFixedCellWidth();
        int n = this.cellWidth = fixedCellWidth != -1 ? fixedCellWidth : -1;
        if (fixedCellHeight != -1) {
            this.cellHeight = fixedCellHeight;
            this.cellHeights = null;
        } else {
            this.cellHeight = -1;
            this.cellHeights = new int[this.list.getModel().getSize()];
        }
        if (fixedCellWidth == -1 || fixedCellHeight == -1) {
            ListModel<Object> dataModel = this.list.getModel();
            int dataModelSize = dataModel.getSize();
            ListCellRenderer<Object> renderer = this.list.getCellRenderer();
            if (renderer != null) {
                for (int index = 0; index < dataModelSize; ++index) {
                    Object value = dataModel.getElementAt(index);
                    Component c = renderer.getListCellRendererComponent(this.list, value, index, false, false);
                    this.rendererPane.add(c);
                    Dimension cellSize = c.getPreferredSize();
                    if (fixedCellWidth == -1) {
                        this.cellWidth = Math.max(cellSize.width, this.cellWidth);
                    }
                    if (fixedCellHeight != -1) continue;
                    this.cellHeights[index] = cellSize.height;
                }
            } else {
                if (this.cellWidth == -1) {
                    this.cellWidth = 0;
                }
                if (this.cellHeights == null) {
                    this.cellHeights = new int[dataModelSize];
                }
                for (int index = 0; index < dataModelSize; ++index) {
                    this.cellHeights[index] = 0;
                }
            }
        }
        this.columnCount = 1;
        if (this.layoutOrientation != 0) {
            this.updateHorizontalLayoutState(fixedCellWidth, fixedCellHeight);
        }
    }

    private void updateHorizontalLayoutState(int fixedCellWidth, int fixedCellHeight) {
        int height;
        int visRows = this.list.getVisibleRowCount();
        int dataModelSize = this.list.getModel().getSize();
        Insets insets = this.list.getInsets();
        this.listHeight = this.list.getHeight();
        this.listWidth = this.list.getWidth();
        if (dataModelSize == 0) {
            this.columnCount = 0;
            this.rowsPerColumn = 0;
            this.preferredHeight = insets.top + insets.bottom;
            return;
        }
        if (fixedCellHeight != -1) {
            height = fixedCellHeight;
        } else {
            int maxHeight = 0;
            if (this.cellHeights.length > 0) {
                maxHeight = this.cellHeights[this.cellHeights.length - 1];
                for (int counter = this.cellHeights.length - 2; counter >= 0; --counter) {
                    maxHeight = Math.max(maxHeight, this.cellHeights[counter]);
                }
            }
            height = this.cellHeight = maxHeight;
            this.cellHeights = null;
        }
        this.rowsPerColumn = dataModelSize;
        if (visRows > 0) {
            this.rowsPerColumn = visRows;
            this.columnCount = Math.max(1, dataModelSize / this.rowsPerColumn);
            if (dataModelSize > 0 && dataModelSize > this.rowsPerColumn && dataModelSize % this.rowsPerColumn != 0) {
                ++this.columnCount;
            }
            if (this.layoutOrientation == 2) {
                this.rowsPerColumn = dataModelSize / this.columnCount;
                if (dataModelSize % this.columnCount > 0) {
                    ++this.rowsPerColumn;
                }
            }
        } else if (this.layoutOrientation == 1 && height != 0) {
            this.rowsPerColumn = Math.max(1, (this.listHeight - insets.top - insets.bottom) / height);
            this.columnCount = Math.max(1, dataModelSize / this.rowsPerColumn);
            if (dataModelSize > 0 && dataModelSize > this.rowsPerColumn && dataModelSize % this.rowsPerColumn != 0) {
                ++this.columnCount;
            }
        } else if (this.layoutOrientation == 2 && this.cellWidth > 0 && this.listWidth > 0) {
            this.columnCount = Math.max(1, (this.listWidth - insets.left - insets.right) / this.cellWidth);
            this.rowsPerColumn = dataModelSize / this.columnCount;
            if (dataModelSize % this.columnCount > 0) {
                ++this.rowsPerColumn;
            }
        }
        this.preferredHeight = this.rowsPerColumn * this.cellHeight + insets.top + insets.bottom;
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected MouseInputListener createMouseInputListener() {
        return this.getHandler();
    }

    protected FocusListener createFocusListener() {
        return this.getHandler();
    }

    protected ListSelectionListener createListSelectionListener() {
        return this.getHandler();
    }

    private void redrawList() {
        this.list.revalidate();
        this.list.repaint();
    }

    protected ListDataListener createListDataListener() {
        return this.getHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return this.getHandler();
    }

    private static int adjustIndex(int index, JList<?> list) {
        return index < list.getModel().getSize() ? index : -1;
    }

    private static class Actions
    extends UIAction {
        private static final String SELECT_PREVIOUS_COLUMN = "selectPreviousColumn";
        private static final String SELECT_PREVIOUS_COLUMN_EXTEND = "selectPreviousColumnExtendSelection";
        private static final String SELECT_PREVIOUS_COLUMN_CHANGE_LEAD = "selectPreviousColumnChangeLead";
        private static final String SELECT_NEXT_COLUMN = "selectNextColumn";
        private static final String SELECT_NEXT_COLUMN_EXTEND = "selectNextColumnExtendSelection";
        private static final String SELECT_NEXT_COLUMN_CHANGE_LEAD = "selectNextColumnChangeLead";
        private static final String SELECT_PREVIOUS_ROW = "selectPreviousRow";
        private static final String SELECT_PREVIOUS_ROW_EXTEND = "selectPreviousRowExtendSelection";
        private static final String SELECT_PREVIOUS_ROW_CHANGE_LEAD = "selectPreviousRowChangeLead";
        private static final String SELECT_NEXT_ROW = "selectNextRow";
        private static final String SELECT_NEXT_ROW_EXTEND = "selectNextRowExtendSelection";
        private static final String SELECT_NEXT_ROW_CHANGE_LEAD = "selectNextRowChangeLead";
        private static final String SELECT_FIRST_ROW = "selectFirstRow";
        private static final String SELECT_FIRST_ROW_EXTEND = "selectFirstRowExtendSelection";
        private static final String SELECT_FIRST_ROW_CHANGE_LEAD = "selectFirstRowChangeLead";
        private static final String SELECT_LAST_ROW = "selectLastRow";
        private static final String SELECT_LAST_ROW_EXTEND = "selectLastRowExtendSelection";
        private static final String SELECT_LAST_ROW_CHANGE_LEAD = "selectLastRowChangeLead";
        private static final String SCROLL_UP = "scrollUp";
        private static final String SCROLL_UP_EXTEND = "scrollUpExtendSelection";
        private static final String SCROLL_UP_CHANGE_LEAD = "scrollUpChangeLead";
        private static final String SCROLL_DOWN = "scrollDown";
        private static final String SCROLL_DOWN_EXTEND = "scrollDownExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_LEAD = "scrollDownChangeLead";
        private static final String SELECT_ALL = "selectAll";
        private static final String CLEAR_SELECTION = "clearSelection";
        private static final String ADD_TO_SELECTION = "addToSelection";
        private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";
        private static final String EXTEND_TO = "extendTo";
        private static final String MOVE_SELECTION_TO = "moveSelectionTo";

        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = this.getName();
            JList list = (JList)e.getSource();
            BasicListUI ui = (BasicListUI)BasicLookAndFeel.getUIOfType(list.getUI(), BasicListUI.class);
            if (name == SELECT_PREVIOUS_COLUMN) {
                this.changeSelection(list, 1, this.getNextColumnIndex(list, ui, -1), -1);
            } else if (name == SELECT_PREVIOUS_COLUMN_EXTEND) {
                this.changeSelection(list, 2, this.getNextColumnIndex(list, ui, -1), -1);
            } else if (name == SELECT_PREVIOUS_COLUMN_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextColumnIndex(list, ui, -1), -1);
            } else if (name == SELECT_NEXT_COLUMN) {
                this.changeSelection(list, 1, this.getNextColumnIndex(list, ui, 1), 1);
            } else if (name == SELECT_NEXT_COLUMN_EXTEND) {
                this.changeSelection(list, 2, this.getNextColumnIndex(list, ui, 1), 1);
            } else if (name == SELECT_NEXT_COLUMN_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextColumnIndex(list, ui, 1), 1);
            } else if (name == SELECT_PREVIOUS_ROW) {
                this.changeSelection(list, 1, this.getNextIndex(list, ui, -1), -1);
            } else if (name == SELECT_PREVIOUS_ROW_EXTEND) {
                this.changeSelection(list, 2, this.getNextIndex(list, ui, -1), -1);
            } else if (name == SELECT_PREVIOUS_ROW_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextIndex(list, ui, -1), -1);
            } else if (name == SELECT_NEXT_ROW) {
                this.changeSelection(list, 1, this.getNextIndex(list, ui, 1), 1);
            } else if (name == SELECT_NEXT_ROW_EXTEND) {
                this.changeSelection(list, 2, this.getNextIndex(list, ui, 1), 1);
            } else if (name == SELECT_NEXT_ROW_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextIndex(list, ui, 1), 1);
            } else if (name == SELECT_FIRST_ROW) {
                this.changeSelection(list, 1, 0, -1);
            } else if (name == SELECT_FIRST_ROW_EXTEND) {
                this.changeSelection(list, 2, 0, -1);
            } else if (name == SELECT_FIRST_ROW_CHANGE_LEAD) {
                this.changeSelection(list, 0, 0, -1);
            } else if (name == SELECT_LAST_ROW) {
                this.changeSelection(list, 1, list.getModel().getSize() - 1, 1);
            } else if (name == SELECT_LAST_ROW_EXTEND) {
                this.changeSelection(list, 2, list.getModel().getSize() - 1, 1);
            } else if (name == SELECT_LAST_ROW_CHANGE_LEAD) {
                this.changeSelection(list, 0, list.getModel().getSize() - 1, 1);
            } else if (name == SCROLL_UP) {
                this.changeSelection(list, 1, this.getNextPageIndex(list, -1), -1);
            } else if (name == SCROLL_UP_EXTEND) {
                this.changeSelection(list, 2, this.getNextPageIndex(list, -1), -1);
            } else if (name == SCROLL_UP_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextPageIndex(list, -1), -1);
            } else if (name == SCROLL_DOWN) {
                this.changeSelection(list, 1, this.getNextPageIndex(list, 1), 1);
            } else if (name == SCROLL_DOWN_EXTEND) {
                this.changeSelection(list, 2, this.getNextPageIndex(list, 1), 1);
            } else if (name == SCROLL_DOWN_CHANGE_LEAD) {
                this.changeSelection(list, 0, this.getNextPageIndex(list, 1), 1);
            } else if (name == SELECT_ALL) {
                this.selectAll(list);
            } else if (name == CLEAR_SELECTION) {
                this.clearSelection(list);
            } else if (name == ADD_TO_SELECTION) {
                int index = BasicListUI.adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list);
                if (!list.isSelectedIndex(index)) {
                    int oldAnchor = list.getSelectionModel().getAnchorSelectionIndex();
                    list.setValueIsAdjusting(true);
                    list.addSelectionInterval(index, index);
                    list.getSelectionModel().setAnchorSelectionIndex(oldAnchor);
                    list.setValueIsAdjusting(false);
                }
            } else if (name == TOGGLE_AND_ANCHOR) {
                int index = BasicListUI.adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list);
                if (list.isSelectedIndex(index)) {
                    list.removeSelectionInterval(index, index);
                } else {
                    list.addSelectionInterval(index, index);
                }
            } else if (name == EXTEND_TO) {
                this.changeSelection(list, 2, BasicListUI.adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list), 0);
            } else if (name == MOVE_SELECTION_TO) {
                this.changeSelection(list, 1, BasicListUI.adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list), 0);
            }
        }

        @Override
        public boolean accept(Object c) {
            String name = this.getName();
            if (name == SELECT_PREVIOUS_COLUMN_CHANGE_LEAD || name == SELECT_NEXT_COLUMN_CHANGE_LEAD || name == SELECT_PREVIOUS_ROW_CHANGE_LEAD || name == SELECT_NEXT_ROW_CHANGE_LEAD || name == SELECT_FIRST_ROW_CHANGE_LEAD || name == SELECT_LAST_ROW_CHANGE_LEAD || name == SCROLL_UP_CHANGE_LEAD || name == SCROLL_DOWN_CHANGE_LEAD) {
                return c != null && ((JList)c).getSelectionModel() instanceof DefaultListSelectionModel;
            }
            return true;
        }

        private void clearSelection(JList<?> list) {
            list.clearSelection();
        }

        private void selectAll(JList<?> list) {
            int size = list.getModel().getSize();
            if (size > 0) {
                ListSelectionModel lsm = list.getSelectionModel();
                int lead = BasicListUI.adjustIndex(lsm.getLeadSelectionIndex(), list);
                if (lsm.getSelectionMode() == 0) {
                    if (lead == -1) {
                        int min = BasicListUI.adjustIndex(list.getMinSelectionIndex(), list);
                        lead = min == -1 ? 0 : min;
                    }
                    list.setSelectionInterval(lead, lead);
                    list.ensureIndexIsVisible(lead);
                } else {
                    list.setValueIsAdjusting(true);
                    int anchor = BasicListUI.adjustIndex(lsm.getAnchorSelectionIndex(), list);
                    list.setSelectionInterval(0, size - 1);
                    SwingUtilities2.setLeadAnchorWithoutSelection(lsm, anchor, lead);
                    list.setValueIsAdjusting(false);
                }
            }
        }

        private int getNextPageIndex(JList<?> list, int direction) {
            Rectangle leadRect;
            if (list.getModel().getSize() == 0) {
                return -1;
            }
            int index = -1;
            Rectangle visRect = list.getVisibleRect();
            ListSelectionModel lsm = list.getSelectionModel();
            int lead = BasicListUI.adjustIndex(lsm.getLeadSelectionIndex(), list);
            Rectangle rectangle = leadRect = lead == -1 ? new Rectangle() : list.getCellBounds(lead, lead);
            if (leadRect == null) {
                return index;
            }
            if (list.getLayoutOrientation() == 1 && list.getVisibleRowCount() <= 0) {
                if (!list.getComponentOrientation().isLeftToRight()) {
                    direction = -direction;
                }
                if (direction < 0) {
                    visRect.x = leadRect.x + leadRect.width - visRect.width;
                    Point p = new Point(visRect.x - 1, leadRect.y);
                    index = list.locationToIndex(p);
                    if (index == -1) {
                        return index;
                    }
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && visRect.intersects(cellBounds)) {
                        p.x = cellBounds.x - 1;
                        index = list.locationToIndex(p);
                        if (index == -1) {
                            return index;
                        }
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds != null && cellBounds.y != leadRect.y) {
                        p.x = cellBounds.x + cellBounds.width;
                        index = list.locationToIndex(p);
                    }
                } else {
                    visRect.x = leadRect.x;
                    Point p = new Point(visRect.x + visRect.width, leadRect.y);
                    index = list.locationToIndex(p);
                    if (index == -1) {
                        return index;
                    }
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && visRect.intersects(cellBounds)) {
                        p.x = cellBounds.x + cellBounds.width;
                        index = list.locationToIndex(p);
                        if (index == -1) {
                            return index;
                        }
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds != null && cellBounds.y != leadRect.y) {
                        p.x = cellBounds.x - 1;
                        index = list.locationToIndex(p);
                    }
                }
            } else if (direction < 0) {
                Point p = new Point(leadRect.x, visRect.y);
                index = list.locationToIndex(p);
                if (lead <= index) {
                    p.y = visRect.y = leadRect.y + leadRect.height - visRect.height;
                    index = list.locationToIndex(p);
                    if (index == -1) {
                        return index;
                    }
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && cellBounds.y < visRect.y) {
                        p.y = cellBounds.y + cellBounds.height;
                        index = list.locationToIndex(p);
                        if (index == -1) {
                            return index;
                        }
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds != null && cellBounds.y >= leadRect.y) {
                        p.y = leadRect.y - 1;
                        index = list.locationToIndex(p);
                    }
                }
            } else {
                Point p = new Point(leadRect.x, visRect.y + visRect.height - 1);
                index = list.locationToIndex(p);
                if (index == -1) {
                    return index;
                }
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds != null && cellBounds.y + cellBounds.height > visRect.y + visRect.height) {
                    p.y = cellBounds.y - 1;
                    index = list.locationToIndex(p);
                    if (index == -1) {
                        return index;
                    }
                    cellBounds = list.getCellBounds(index, index);
                    index = Math.max(index, lead);
                }
                if (lead >= index) {
                    visRect.y = leadRect.y;
                    p.y = visRect.y + visRect.height - 1;
                    index = list.locationToIndex(p);
                    if (index == -1) {
                        return index;
                    }
                    cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && cellBounds.y + cellBounds.height > visRect.y + visRect.height) {
                        p.y = cellBounds.y - 1;
                        index = list.locationToIndex(p);
                        if (index == -1) {
                            return index;
                        }
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds != null && cellBounds.y <= leadRect.y) {
                        p.y = leadRect.y + leadRect.height;
                        index = list.locationToIndex(p);
                    }
                }
            }
            return index;
        }

        private void changeSelection(JList<?> list, int type, int index, int direction) {
            if (index >= 0 && index < list.getModel().getSize()) {
                ListSelectionModel lsm = list.getSelectionModel();
                if (type == 0 && list.getSelectionMode() != 2) {
                    type = 1;
                }
                this.adjustScrollPositionIfNecessary(list, index, direction);
                if (type == 2) {
                    int anchor = BasicListUI.adjustIndex(lsm.getAnchorSelectionIndex(), list);
                    if (anchor == -1) {
                        anchor = 0;
                    }
                    list.setSelectionInterval(anchor, index);
                } else if (type == 1) {
                    list.setSelectedIndex(index);
                } else {
                    ((DefaultListSelectionModel)lsm).moveLeadSelectionIndex(index);
                }
            }
        }

        private void adjustScrollPositionIfNecessary(JList<?> list, int index, int direction) {
            if (direction == 0) {
                return;
            }
            Rectangle cellBounds = list.getCellBounds(index, index);
            Rectangle visRect = list.getVisibleRect();
            if (cellBounds != null && !visRect.contains(cellBounds)) {
                if (list.getLayoutOrientation() == 1 && list.getVisibleRowCount() <= 0) {
                    if (list.getComponentOrientation().isLeftToRight()) {
                        if (direction > 0) {
                            int x = Math.max(0, cellBounds.x + cellBounds.width - visRect.width);
                            int startIndex = list.locationToIndex(new Point(x, cellBounds.y));
                            if (startIndex == -1) {
                                return;
                            }
                            Rectangle startRect = list.getCellBounds(startIndex, startIndex);
                            if (startRect != null && startRect.x < x && startRect.x < cellBounds.x) {
                                startRect.x += startRect.width;
                                startIndex = list.locationToIndex(startRect.getLocation());
                                if (startIndex == -1) {
                                    return;
                                }
                                startRect = list.getCellBounds(startIndex, startIndex);
                            }
                            cellBounds = startRect;
                        }
                        if (cellBounds != null) {
                            cellBounds.width = visRect.width;
                        }
                    } else if (direction > 0) {
                        int x = cellBounds.x + visRect.width;
                        int rightIndex = list.locationToIndex(new Point(x, cellBounds.y));
                        if (rightIndex == -1) {
                            return;
                        }
                        Rectangle rightRect = list.getCellBounds(rightIndex, rightIndex);
                        if (rightRect != null) {
                            if (rightRect.x + rightRect.width > x && rightRect.x > cellBounds.x) {
                                rightRect.width = 0;
                            }
                            cellBounds.x = Math.max(0, rightRect.x + rightRect.width - visRect.width);
                            cellBounds.width = visRect.width;
                        }
                    } else {
                        cellBounds.x += Math.max(0, cellBounds.width - visRect.width);
                        cellBounds.width = Math.min(cellBounds.width, visRect.width);
                    }
                } else if (direction > 0 && (cellBounds.y < visRect.y || cellBounds.y + cellBounds.height > visRect.y + visRect.height)) {
                    int y = Math.max(0, cellBounds.y + cellBounds.height - visRect.height);
                    int startIndex = list.locationToIndex(new Point(cellBounds.x, y));
                    if (startIndex == -1) {
                        return;
                    }
                    Rectangle startRect = list.getCellBounds(startIndex, startIndex);
                    if (startRect != null && startRect.y < y && startRect.y < cellBounds.y) {
                        startRect.y += startRect.height;
                        startIndex = list.locationToIndex(startRect.getLocation());
                        if (startIndex == -1) {
                            return;
                        }
                        startRect = list.getCellBounds(startIndex, startIndex);
                    }
                    if ((cellBounds = startRect) != null) {
                        cellBounds.height = visRect.height;
                    }
                } else {
                    cellBounds.height = Math.min(cellBounds.height, visRect.height);
                }
                if (cellBounds != null) {
                    list.scrollRectToVisible(cellBounds);
                }
            }
        }

        private int getNextColumnIndex(JList<?> list, BasicListUI ui, int amount) {
            if (list.getLayoutOrientation() != 0) {
                int index = BasicListUI.adjustIndex(list.getLeadSelectionIndex(), list);
                int size = list.getModel().getSize();
                if (index == -1) {
                    return 0;
                }
                if (size == 1) {
                    return 0;
                }
                if (ui == null || ui.columnCount <= 1) {
                    return -1;
                }
                int column = ui.convertModelToColumn(index);
                int row = ui.convertModelToRow(index);
                if ((column += amount) >= ui.columnCount || column < 0) {
                    return -1;
                }
                int maxRowCount = ui.getRowCount(column);
                if (row >= maxRowCount) {
                    return -1;
                }
                return ui.getModelIndex(column, row);
            }
            return -1;
        }

        private int getNextIndex(JList<?> list, BasicListUI ui, int amount) {
            int index = BasicListUI.adjustIndex(list.getLeadSelectionIndex(), list);
            int size = list.getModel().getSize();
            if (index == -1) {
                if (size > 0) {
                    index = amount > 0 ? 0 : size - 1;
                }
            } else if (size == 1) {
                index = 0;
            } else if (list.getLayoutOrientation() == 2) {
                if (ui != null) {
                    index += ui.columnCount * amount;
                }
            } else {
                index += amount;
            }
            return index;
        }
    }

    private class Handler
    implements FocusListener,
    KeyListener,
    ListDataListener,
    ListSelectionListener,
    MouseInputListener,
    PropertyChangeListener,
    DragRecognitionSupport.BeforeDrag {
        private String prefix = "";
        private String typedString = "";
        private long lastTime = 0L;
        private boolean dragPressDidSelection;

        private Handler() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int index;
            JList src = (JList)e.getSource();
            ListModel model = src.getModel();
            if (model.getSize() == 0 || e.isAltDown() || BasicGraphicsUtils.isMenuShortcutKeyDown(e) || this.isNavigationKey(e)) {
                return;
            }
            boolean startingFromSelection = true;
            char c = e.getKeyChar();
            long time = e.getWhen();
            int startIndex = BasicListUI.adjustIndex(src.getLeadSelectionIndex(), BasicListUI.this.list);
            if (time - this.lastTime < BasicListUI.this.timeFactor) {
                this.typedString = this.typedString + c;
                if (this.prefix.length() == 1 && c == this.prefix.charAt(0)) {
                    ++startIndex;
                } else {
                    this.prefix = this.typedString;
                }
            } else {
                ++startIndex;
                this.typedString = "" + c;
                this.prefix = this.typedString;
            }
            this.lastTime = time;
            if (startIndex < 0 || startIndex >= model.getSize()) {
                startingFromSelection = false;
                startIndex = 0;
            }
            if ((index = src.getNextMatch(this.prefix, startIndex, Position.Bias.Forward)) >= 0) {
                src.setSelectedIndex(index);
                src.ensureIndexIsVisible(index);
            } else if (startingFromSelection && (index = src.getNextMatch(this.prefix, 0, Position.Bias.Forward)) >= 0) {
                src.setSelectedIndex(index);
                src.ensureIndexIsVisible(index);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (this.isNavigationKey(e)) {
                this.prefix = "";
                this.typedString = "";
                this.lastTime = 0L;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        private boolean isNavigationKey(KeyEvent event) {
            InputMap inputMap = BasicListUI.this.list.getInputMap(1);
            KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
            return inputMap != null && inputMap.get(key) != null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "model") {
                ListModel oldModel = (ListModel)e.getOldValue();
                ListModel newModel = (ListModel)e.getNewValue();
                if (oldModel != null) {
                    oldModel.removeListDataListener(BasicListUI.this.listDataListener);
                }
                if (newModel != null) {
                    newModel.addListDataListener(BasicListUI.this.listDataListener);
                }
                BasicListUI.this.updateLayoutStateNeeded |= 1;
                BasicListUI.this.redrawList();
            } else if (propertyName == "selectionModel") {
                ListSelectionModel oldModel = (ListSelectionModel)e.getOldValue();
                ListSelectionModel newModel = (ListSelectionModel)e.getNewValue();
                if (oldModel != null) {
                    oldModel.removeListSelectionListener(BasicListUI.this.listSelectionListener);
                }
                if (newModel != null) {
                    newModel.addListSelectionListener(BasicListUI.this.listSelectionListener);
                }
                BasicListUI.this.updateLayoutStateNeeded |= 1;
                BasicListUI.this.redrawList();
            } else if (propertyName == "cellRenderer") {
                BasicListUI.this.updateLayoutStateNeeded |= 0x40;
                BasicListUI.this.redrawList();
            } else if (propertyName == "font" || SwingUtilities2.isScaleChanged(e)) {
                BasicListUI.this.updateLayoutStateNeeded |= 4;
                BasicListUI.this.redrawList();
            } else if (propertyName == "prototypeCellValue") {
                BasicListUI.this.updateLayoutStateNeeded |= 0x20;
                BasicListUI.this.redrawList();
            } else if (propertyName == "fixedCellHeight") {
                BasicListUI.this.updateLayoutStateNeeded |= 0x10;
                BasicListUI.this.redrawList();
            } else if (propertyName == "fixedCellWidth") {
                BasicListUI.this.updateLayoutStateNeeded |= 8;
                BasicListUI.this.redrawList();
            } else if (propertyName == "selectionForeground") {
                BasicListUI.this.list.repaint();
            } else if (propertyName == "selectionBackground") {
                BasicListUI.this.list.repaint();
            } else if ("layoutOrientation" == propertyName) {
                BasicListUI.this.updateLayoutStateNeeded |= 0x80;
                BasicListUI.this.layoutOrientation = BasicListUI.this.list.getLayoutOrientation();
                BasicListUI.this.redrawList();
            } else if ("visibleRowCount" == propertyName) {
                if (BasicListUI.this.layoutOrientation != 0) {
                    BasicListUI.this.updateLayoutStateNeeded |= 0x80;
                    BasicListUI.this.redrawList();
                }
            } else if ("componentOrientation" == propertyName) {
                BasicListUI.this.isLeftToRight = BasicListUI.this.list.getComponentOrientation().isLeftToRight();
                BasicListUI.this.updateLayoutStateNeeded |= 0x400;
                BasicListUI.this.redrawList();
                InputMap inputMap = BasicListUI.this.getInputMap(0);
                SwingUtilities.replaceUIInputMap(BasicListUI.this.list, 0, inputMap);
            } else if ("List.isFileList" == propertyName) {
                BasicListUI.this.updateIsFileList();
                BasicListUI.this.redrawList();
            } else if ("dropLocation" == propertyName) {
                JList.DropLocation oldValue = (JList.DropLocation)e.getOldValue();
                this.repaintDropLocation(oldValue);
                this.repaintDropLocation(BasicListUI.this.list.getDropLocation());
            }
        }

        private void repaintDropLocation(JList.DropLocation loc) {
            if (loc == null) {
                return;
            }
            Rectangle r = loc.isInsert() ? BasicListUI.this.getDropLineRect(loc) : BasicListUI.this.getCellBounds(BasicListUI.this.list, loc.getIndex());
            if (r != null) {
                BasicListUI.this.list.repaint(r);
            }
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            BasicListUI.this.updateLayoutStateNeeded = 1;
            int minIndex = Math.min(e.getIndex0(), e.getIndex1());
            int maxIndex = Math.max(e.getIndex0(), e.getIndex1());
            ListSelectionModel sm = BasicListUI.this.list.getSelectionModel();
            if (sm != null) {
                sm.insertIndexInterval(minIndex, maxIndex - minIndex + 1, true);
            }
            BasicListUI.this.redrawList();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            BasicListUI.this.updateLayoutStateNeeded = 1;
            ListSelectionModel sm = BasicListUI.this.list.getSelectionModel();
            if (sm != null) {
                sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
            }
            BasicListUI.this.redrawList();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            BasicListUI.this.updateLayoutStateNeeded = 1;
            BasicListUI.this.redrawList();
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            BasicListUI.this.maybeUpdateLayoutState();
            int size = BasicListUI.this.list.getModel().getSize();
            int firstIndex = Math.min(size - 1, Math.max(e.getFirstIndex(), 0));
            int lastIndex = Math.min(size - 1, Math.max(e.getLastIndex(), 0));
            Rectangle bounds = BasicListUI.this.getCellBounds(BasicListUI.this.list, firstIndex, lastIndex);
            if (bounds != null) {
                BasicListUI.this.list.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, BasicListUI.this.list)) {
                return;
            }
            boolean dragEnabled = BasicListUI.this.list.getDragEnabled();
            boolean grabFocus = true;
            if (dragEnabled) {
                int row = SwingUtilities2.loc2IndexFileList(BasicListUI.this.list, e.getPoint());
                if (row != -1 && DragRecognitionSupport.mousePressed(e)) {
                    this.dragPressDidSelection = false;
                    if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                        return;
                    }
                    if (!e.isShiftDown() && BasicListUI.this.list.isSelectedIndex(row)) {
                        BasicListUI.this.list.addSelectionInterval(row, row);
                        return;
                    }
                    grabFocus = false;
                    this.dragPressDidSelection = true;
                }
            } else {
                BasicListUI.this.list.setValueIsAdjusting(true);
            }
            if (grabFocus) {
                SwingUtilities2.adjustFocus(BasicListUI.this.list);
            }
            this.adjustSelection(e);
        }

        private void adjustSelection(MouseEvent e) {
            int row = SwingUtilities2.loc2IndexFileList(BasicListUI.this.list, e.getPoint());
            if (row < 0) {
                if (BasicListUI.this.isFileList && e.getID() == 501 && (!e.isShiftDown() || BasicListUI.this.list.getSelectionMode() == 0)) {
                    BasicListUI.this.list.clearSelection();
                }
            } else {
                boolean anchorSelected;
                int anchorIndex = BasicListUI.adjustIndex(BasicListUI.this.list.getAnchorSelectionIndex(), BasicListUI.this.list);
                if (anchorIndex == -1) {
                    anchorIndex = 0;
                    anchorSelected = false;
                } else {
                    anchorSelected = BasicListUI.this.list.isSelectedIndex(anchorIndex);
                }
                if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                    if (e.isShiftDown()) {
                        if (anchorSelected) {
                            BasicListUI.this.list.addSelectionInterval(anchorIndex, row);
                        } else {
                            BasicListUI.this.list.removeSelectionInterval(anchorIndex, row);
                            if (BasicListUI.this.isFileList) {
                                BasicListUI.this.list.addSelectionInterval(row, row);
                                BasicListUI.this.list.getSelectionModel().setAnchorSelectionIndex(anchorIndex);
                            }
                        }
                    } else if (BasicListUI.this.list.isSelectedIndex(row)) {
                        BasicListUI.this.list.removeSelectionInterval(row, row);
                    } else {
                        BasicListUI.this.list.addSelectionInterval(row, row);
                    }
                } else if (e.isShiftDown()) {
                    BasicListUI.this.list.setSelectionInterval(anchorIndex, row);
                } else {
                    BasicListUI.this.list.setSelectionInterval(row, row);
                }
            }
        }

        @Override
        public void dragStarting(MouseEvent me) {
            if (BasicGraphicsUtils.isMenuShortcutKeyDown(me)) {
                int row = SwingUtilities2.loc2IndexFileList(BasicListUI.this.list, me.getPoint());
                BasicListUI.this.list.addSelectionInterval(row, row);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, BasicListUI.this.list)) {
                return;
            }
            if (BasicListUI.this.list.getDragEnabled()) {
                DragRecognitionSupport.mouseDragged(e, this);
                return;
            }
            if (e.isShiftDown() || BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                return;
            }
            int row = BasicListUI.this.locationToIndex(BasicListUI.this.list, e.getPoint());
            if (row != -1) {
                if (BasicListUI.this.isFileList) {
                    return;
                }
                Rectangle cellBounds = BasicListUI.this.getCellBounds(BasicListUI.this.list, row, row);
                if (cellBounds != null) {
                    BasicListUI.this.list.scrollRectToVisible(cellBounds);
                    BasicListUI.this.list.setSelectionInterval(row, row);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, BasicListUI.this.list)) {
                return;
            }
            if (BasicListUI.this.list.getDragEnabled()) {
                MouseEvent me = DragRecognitionSupport.mouseReleased(e);
                if (me != null) {
                    SwingUtilities2.adjustFocus(BasicListUI.this.list);
                    if (!this.dragPressDidSelection) {
                        this.adjustSelection(me);
                    }
                }
            } else {
                BasicListUI.this.list.setValueIsAdjusting(false);
            }
        }

        protected void repaintCellFocus() {
            Rectangle r;
            int leadIndex = BasicListUI.adjustIndex(BasicListUI.this.list.getLeadSelectionIndex(), BasicListUI.this.list);
            if (leadIndex != -1 && (r = BasicListUI.this.getCellBounds(BasicListUI.this.list, leadIndex, leadIndex)) != null) {
                BasicListUI.this.list.repaint(r.x, r.y, r.width, r.height);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            this.repaintCellFocus();
        }

        @Override
        public void focusLost(FocusEvent e) {
            this.repaintCellFocus();
        }
    }

    static class ListTransferHandler
    extends TransferHandler
    implements UIResource {
        ListTransferHandler() {
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JList) {
                JList list = (JList)c;
                Object[] values = list.getSelectedValues();
                if (values == null || values.length == 0) {
                    return null;
                }
                StringBuilder plainStr = new StringBuilder();
                StringBuilder htmlStr = new StringBuilder();
                htmlStr.append("<html>\n<body>\n<ul>\n");
                for (int i = 0; i < values.length; ++i) {
                    Object obj = values[i];
                    String val = obj == null ? "" : obj.toString();
                    plainStr.append(val).append('\n');
                    htmlStr.append("  <li>").append(val).append('\n');
                }
                plainStr.deleteCharAt(plainStr.length() - 1);
                htmlStr.append("</ul>\n</body>\n</html>");
                return new BasicTransferable(plainStr.toString(), htmlStr.toString());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return 1;
        }
    }

    public class PropertyChangeHandler
    implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            BasicListUI.this.getHandler().propertyChange(e);
        }
    }

    public class ListDataHandler
    implements ListDataListener {
        @Override
        public void intervalAdded(ListDataEvent e) {
            BasicListUI.this.getHandler().intervalAdded(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            BasicListUI.this.getHandler().intervalRemoved(e);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            BasicListUI.this.getHandler().contentsChanged(e);
        }
    }

    public class ListSelectionHandler
    implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            BasicListUI.this.getHandler().valueChanged(e);
        }
    }

    public class FocusHandler
    implements FocusListener {
        protected void repaintCellFocus() {
            BasicListUI.this.getHandler().repaintCellFocus();
        }

        @Override
        public void focusGained(FocusEvent e) {
            BasicListUI.this.getHandler().focusGained(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            BasicListUI.this.getHandler().focusLost(e);
        }
    }

    public class MouseInputHandler
    implements MouseInputListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            BasicListUI.this.getHandler().mouseClicked(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            BasicListUI.this.getHandler().mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            BasicListUI.this.getHandler().mouseExited(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            BasicListUI.this.getHandler().mousePressed(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            BasicListUI.this.getHandler().mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            BasicListUI.this.getHandler().mouseMoved(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            BasicListUI.this.getHandler().mouseReleased(e);
        }
    }
}

