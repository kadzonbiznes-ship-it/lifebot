/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.im.InputContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.BasicTransferable;
import javax.swing.plaf.basic.DragRecognitionSupport;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import sun.awt.AppContext;
import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;

public abstract class BasicTextUI
extends TextUI
implements ViewFactory {
    private static final int DEFAULT_CARET_MARGIN = 1;
    private static BasicCursor textCursor = new BasicCursor(2);
    private static final EditorKit defaultKit = new DefaultEditorKit();
    transient JTextComponent editor;
    transient boolean painted = false;
    transient RootView rootView = new RootView();
    transient UpdateHandler updateHandler = new UpdateHandler();
    private static final TransferHandler defaultTransferHandler = new TextTransferHandler();
    private final DragListener dragListener = BasicTextUI.getDragListener();
    private static final Position.Bias[] discardBias = new Position.Bias[1];
    private DefaultCaret dropCaret;
    private int caretMargin;

    protected Caret createCaret() {
        return new BasicCaret();
    }

    protected Highlighter createHighlighter() {
        return new BasicHighlighter();
    }

    protected String getKeymapName() {
        String nm = this.getClass().getName();
        int index = nm.lastIndexOf(46);
        if (index >= 0) {
            nm = nm.substring(index + 1);
        }
        return nm;
    }

    protected Keymap createKeymap() {
        String nm = this.getKeymapName();
        Keymap map = JTextComponent.getKeymap(nm);
        if (map == null) {
            Keymap parent = JTextComponent.getKeymap("default");
            map = JTextComponent.addKeymap(nm, parent);
            String prefix = this.getPropertyPrefix();
            Object o = DefaultLookup.get(this.editor, this, prefix + ".keyBindings");
            if (o instanceof JTextComponent.KeyBinding[]) {
                JTextComponent.KeyBinding[] bindings = (JTextComponent.KeyBinding[])o;
                JTextComponent.loadKeymap(map, bindings, this.getComponent().getActions());
            }
        }
        return map;
    }

    protected void propertyChange(PropertyChangeEvent evt) {
        int width;
        Object value;
        if (evt.getPropertyName().equals("editable") || evt.getPropertyName().equals("enabled")) {
            this.updateBackground((JTextComponent)evt.getSource());
        } else if (evt.getPropertyName().equals("caretWidth") && (value = evt.getNewValue()) instanceof Number && (width = ((Number)value).intValue()) >= 0) {
            this.caretMargin = width;
        }
    }

    private void updateBackground(JTextComponent c) {
        if (this instanceof SynthUI || c instanceof JTextArea) {
            return;
        }
        Color background = c.getBackground();
        if (background instanceof UIResource) {
            String prefix = this.getPropertyPrefix();
            Color disabledBG = DefaultLookup.getColor(c, this, prefix + ".disabledBackground", null);
            Color inactiveBG = DefaultLookup.getColor(c, this, prefix + ".inactiveBackground", null);
            Color bg = DefaultLookup.getColor(c, this, prefix + ".background", null);
            if ((c instanceof JTextArea || c instanceof JEditorPane) && background != disabledBG && background != inactiveBG && background != bg) {
                return;
            }
            Color newColor = null;
            if (!c.isEnabled()) {
                newColor = disabledBG;
            }
            if (newColor == null && !c.isEditable()) {
                newColor = inactiveBG;
            }
            if (newColor == null) {
                newColor = bg;
            }
            if (newColor != null && newColor != background) {
                c.setBackground(newColor);
            }
        }
    }

    protected abstract String getPropertyPrefix();

    protected void installDefaults() {
        Insets margin;
        Border b;
        Color dfg;
        Color sfg;
        Color s;
        Color color;
        Color fg;
        Color bg;
        String prefix = this.getPropertyPrefix();
        Font f = this.editor.getFont();
        if (f == null || f instanceof UIResource) {
            this.editor.setFont(UIManager.getFont(prefix + ".font"));
        }
        if ((bg = this.editor.getBackground()) == null || bg instanceof UIResource) {
            this.editor.setBackground(UIManager.getColor(prefix + ".background"));
        }
        if ((fg = this.editor.getForeground()) == null || fg instanceof UIResource) {
            this.editor.setForeground(UIManager.getColor(prefix + ".foreground"));
        }
        if ((color = this.editor.getCaretColor()) == null || color instanceof UIResource) {
            this.editor.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
        }
        if ((s = this.editor.getSelectionColor()) == null || s instanceof UIResource) {
            this.editor.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
        }
        if ((sfg = this.editor.getSelectedTextColor()) == null || sfg instanceof UIResource) {
            this.editor.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
        }
        if ((dfg = this.editor.getDisabledTextColor()) == null || dfg instanceof UIResource) {
            this.editor.setDisabledTextColor(UIManager.getColor(prefix + ".inactiveForeground"));
        }
        if ((b = this.editor.getBorder()) == null || b instanceof UIResource) {
            this.editor.setBorder(UIManager.getBorder(prefix + ".border"));
        }
        if ((margin = this.editor.getMargin()) == null || margin instanceof UIResource) {
            this.editor.setMargin(UIManager.getInsets(prefix + ".margin"));
        }
        this.updateCursor();
    }

    private void installDefaults2() {
        TransferHandler th;
        Highlighter highlighter;
        this.editor.addMouseListener(this.dragListener);
        this.editor.addMouseMotionListener(this.dragListener);
        String prefix = this.getPropertyPrefix();
        Caret caret = this.editor.getCaret();
        if (caret == null || caret instanceof UIResource) {
            caret = this.createCaret();
            this.editor.setCaret(caret);
            int rate = DefaultLookup.getInt(this.getComponent(), this, prefix + ".caretBlinkRate", 500);
            caret.setBlinkRate(rate);
        }
        if ((highlighter = this.editor.getHighlighter()) == null || highlighter instanceof UIResource) {
            this.editor.setHighlighter(this.createHighlighter());
        }
        if ((th = this.editor.getTransferHandler()) == null || th instanceof UIResource) {
            this.editor.setTransferHandler(this.getTransferHandler());
        }
    }

    protected void uninstallDefaults() {
        this.editor.removeMouseListener(this.dragListener);
        this.editor.removeMouseMotionListener(this.dragListener);
        if (this.editor.getCaretColor() instanceof UIResource) {
            this.editor.setCaretColor(null);
        }
        if (this.editor.getSelectionColor() instanceof UIResource) {
            this.editor.setSelectionColor(null);
        }
        if (this.editor.getDisabledTextColor() instanceof UIResource) {
            this.editor.setDisabledTextColor(null);
        }
        if (this.editor.getSelectedTextColor() instanceof UIResource) {
            this.editor.setSelectedTextColor(null);
        }
        if (this.editor.getBorder() instanceof UIResource) {
            this.editor.setBorder(null);
        }
        if (this.editor.getMargin() instanceof UIResource) {
            this.editor.setMargin(null);
        }
        if (this.editor.getCaret() instanceof UIResource) {
            this.editor.setCaret(null);
        }
        if (this.editor.getHighlighter() instanceof UIResource) {
            this.editor.setHighlighter(null);
        }
        if (this.editor.getTransferHandler() instanceof UIResource) {
            this.editor.setTransferHandler(null);
        }
        if (this.editor.getCursor() instanceof UIResource) {
            this.editor.setCursor(null);
        }
    }

    protected void installListeners() {
    }

    protected void uninstallListeners() {
    }

    protected void installKeyboardActions() {
        ActionMap map;
        this.editor.setKeymap(this.createKeymap());
        InputMap km = this.getInputMap();
        if (km != null) {
            SwingUtilities.replaceUIInputMap(this.editor, 0, km);
        }
        if ((map = this.getActionMap()) != null) {
            SwingUtilities.replaceUIActionMap(this.editor, map);
        }
        this.updateFocusAcceleratorBinding(false);
    }

    InputMap getInputMap() {
        InputMapUIResource map = new InputMapUIResource();
        InputMap shared = (InputMap)DefaultLookup.get(this.editor, this, this.getPropertyPrefix() + ".focusInputMap");
        if (shared != null) {
            map.setParent(shared);
        }
        return map;
    }

    void updateFocusAcceleratorBinding(boolean changed) {
        char accelerator = this.editor.getFocusAccelerator();
        if (changed || accelerator != '\u0000') {
            InputMap km = SwingUtilities.getUIInputMap(this.editor, 2);
            if (km == null && accelerator != '\u0000') {
                km = new ComponentInputMapUIResource(this.editor);
                SwingUtilities.replaceUIInputMap(this.editor, 2, km);
                ActionMap am = this.getActionMap();
                SwingUtilities.replaceUIActionMap(this.editor, am);
            }
            if (km != null) {
                km.clear();
                if (accelerator != '\u0000') {
                    km.put(KeyStroke.getKeyStroke((int)accelerator, BasicLookAndFeel.getFocusAcceleratorKeyMask()), "requestFocus");
                    km.put(KeyStroke.getKeyStroke((int)accelerator, SwingUtilities2.setAltGraphMask(BasicLookAndFeel.getFocusAcceleratorKeyMask())), "requestFocus");
                }
            }
        }
    }

    void updateFocusTraversalKeys() {
        EditorKit editorKit = this.getEditorKit(this.editor);
        if (editorKit instanceof DefaultEditorKit) {
            Set<AWTKeyStroke> storedForwardTraversalKeys = this.editor.getFocusTraversalKeys(0);
            Set<AWTKeyStroke> storedBackwardTraversalKeys = this.editor.getFocusTraversalKeys(1);
            HashSet<AWTKeyStroke> forwardTraversalKeys = new HashSet<AWTKeyStroke>(storedForwardTraversalKeys);
            HashSet<AWTKeyStroke> backwardTraversalKeys = new HashSet<AWTKeyStroke>(storedBackwardTraversalKeys);
            if (this.editor.isEditable()) {
                forwardTraversalKeys.remove(KeyStroke.getKeyStroke(9, 0));
                backwardTraversalKeys.remove(KeyStroke.getKeyStroke(9, 1));
            } else {
                forwardTraversalKeys.add(KeyStroke.getKeyStroke(9, 0));
                backwardTraversalKeys.add(KeyStroke.getKeyStroke(9, 1));
            }
            LookAndFeel.installProperty(this.editor, "focusTraversalKeysForward", forwardTraversalKeys);
            LookAndFeel.installProperty(this.editor, "focusTraversalKeysBackward", backwardTraversalKeys);
        }
    }

    private void updateCursor() {
        if (!this.editor.isCursorSet() || this.editor.getCursor() instanceof UIResource) {
            BasicCursor cursor = this.editor.isEditable() ? textCursor : null;
            this.editor.setCursor(cursor);
        }
    }

    TransferHandler getTransferHandler() {
        return defaultTransferHandler;
    }

    ActionMap getActionMap() {
        Action obj;
        String mapName = this.getPropertyPrefix() + ".actionMap";
        ActionMap map = (ActionMap)UIManager.get(mapName);
        if (map == null && (map = this.createActionMap()) != null) {
            UIManager.getLookAndFeelDefaults().put(mapName, map);
        }
        ActionMapUIResource componentMap = new ActionMapUIResource();
        componentMap.put("requestFocus", new FocusAction());
        if (this.getEditorKit(this.editor) instanceof DefaultEditorKit && map != null && (obj = map.get("insert-break")) instanceof DefaultEditorKit.InsertBreakAction) {
            DefaultEditorKit.InsertBreakAction breakAction = (DefaultEditorKit.InsertBreakAction)obj;
            TextActionWrapper action = new TextActionWrapper(breakAction);
            componentMap.put(action.getValue("Name"), action);
        }
        if (map != null) {
            componentMap.setParent(map);
        }
        return componentMap;
    }

    ActionMap createActionMap() {
        ActionMapUIResource map = new ActionMapUIResource();
        for (Action a : this.editor.getActions()) {
            map.put(a.getValue("Name"), a);
        }
        map.put(TransferHandler.getCutAction().getValue("Name"), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue("Name"), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue("Name"), TransferHandler.getPasteAction());
        return map;
    }

    protected void uninstallKeyboardActions() {
        this.editor.setKeymap(null);
        SwingUtilities.replaceUIInputMap(this.editor, 2, null);
        SwingUtilities.replaceUIActionMap(this.editor, null);
    }

    protected void paintBackground(Graphics g) {
        g.setColor(this.editor.getBackground());
        g.fillRect(0, 0, this.editor.getWidth(), this.editor.getHeight());
    }

    protected final JTextComponent getComponent() {
        return this.editor;
    }

    protected void modelChanged() {
        ViewFactory f = this.rootView.getViewFactory();
        Document doc = this.editor.getDocument();
        Element elem = doc.getDefaultRootElement();
        this.setView(f.create(elem));
    }

    protected final void setView(View v) {
        this.rootView.setView(v);
        this.painted = false;
        this.editor.revalidate();
        this.editor.repaint();
    }

    protected void paintSafely(Graphics g) {
        Rectangle alloc;
        this.painted = true;
        Highlighter highlighter = this.editor.getHighlighter();
        Caret caret = this.editor.getCaret();
        if (this.editor.isOpaque()) {
            this.paintBackground(g);
        }
        if (highlighter != null) {
            highlighter.paint(g);
        }
        if ((alloc = this.getVisibleEditorRect()) != null) {
            this.rootView.paint(g, alloc);
        }
        if (caret != null) {
            caret.paint(g);
        }
        if (this.dropCaret != null) {
            this.dropCaret.paint(g);
        }
    }

    @Override
    public void installUI(JComponent c) {
        if (c instanceof JTextComponent) {
            this.editor = (JTextComponent)c;
            LookAndFeel.installProperty(this.editor, "opaque", Boolean.TRUE);
            LookAndFeel.installProperty(this.editor, "autoscrolls", Boolean.TRUE);
            this.installDefaults();
            this.installDefaults2();
            this.caretMargin = -1;
            Object property = UIManager.get("Caret.width");
            if (property instanceof Number) {
                this.caretMargin = ((Number)property).intValue();
            }
            if ((property = c.getClientProperty("caretWidth")) instanceof Number) {
                this.caretMargin = ((Number)property).intValue();
            }
            if (this.caretMargin < 0) {
                this.caretMargin = 1;
            }
            this.editor.addPropertyChangeListener(this.updateHandler);
            Document doc = this.editor.getDocument();
            if (doc == null) {
                this.editor.setDocument(this.getEditorKit(this.editor).createDefaultDocument());
            } else {
                doc.addDocumentListener(this.updateHandler);
                this.modelChanged();
            }
            this.installListeners();
            this.installKeyboardActions();
            LayoutManager oldLayout = this.editor.getLayout();
            if (oldLayout == null || oldLayout instanceof UIResource) {
                this.editor.setLayout(this.updateHandler);
            }
        } else {
            throw new Error("TextUI needs JTextComponent");
        }
        this.updateBackground(this.editor);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.editor.removePropertyChangeListener(this.updateHandler);
        this.editor.getDocument().removeDocumentListener(this.updateHandler);
        this.painted = false;
        this.uninstallDefaults();
        this.rootView.setView(null);
        c.removeAll();
        LayoutManager lm = c.getLayout();
        if (lm instanceof UIResource) {
            c.setLayout(null);
        }
        this.uninstallKeyboardActions();
        this.uninstallListeners();
        this.editor = null;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        this.paint(g, c);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void paint(Graphics g, JComponent c) {
        if (this.rootView.getViewCount() > 0 && this.rootView.getView(0) != null) {
            Document doc = this.editor.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                this.paintSafely(g);
            }
            finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        Document doc = this.editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = c.getSize();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            if (d.width > i.left + i.right + this.caretMargin && d.height > i.top + i.bottom) {
                this.rootView.setSize(d.width - i.left - i.right - this.caretMargin, d.height - i.top - i.bottom);
            } else if (d.width == 0 && d.height == 0) {
                this.rootView.setSize(2.1474836E9f, 2.1474836E9f);
            }
            d.width = (int)Math.min((long)this.rootView.getPreferredSpan(0) + (long)i.left + (long)i.right + (long)this.caretMargin, Integer.MAX_VALUE);
            d.height = (int)Math.min((long)this.rootView.getPreferredSpan(1) + (long)i.top + (long)i.bottom, Integer.MAX_VALUE);
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        Document doc = this.editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            d.width = (int)this.rootView.getMinimumSpan(0) + i.left + i.right + this.caretMargin;
            d.height = (int)this.rootView.getMinimumSpan(1) + i.top + i.bottom;
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Dimension getMaximumSize(JComponent c) {
        Document doc = this.editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            d.width = (int)Math.min((long)this.rootView.getMaximumSpan(0) + (long)i.left + (long)i.right + (long)this.caretMargin, Integer.MAX_VALUE);
            d.height = (int)Math.min((long)this.rootView.getMaximumSpan(1) + (long)i.top + (long)i.bottom, Integer.MAX_VALUE);
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }

    protected Rectangle getVisibleEditorRect() {
        Rectangle alloc = this.editor.getBounds();
        if (alloc.width > 0 && alloc.height > 0) {
            alloc.y = 0;
            alloc.x = 0;
            Insets insets = this.editor.getInsets();
            alloc.x += insets.left;
            alloc.y += insets.top;
            alloc.width -= insets.left + insets.right + this.caretMargin;
            alloc.height -= insets.top + insets.bottom;
            return alloc;
        }
        return null;
    }

    @Override
    @Deprecated(since="9")
    public Rectangle modelToView(JTextComponent tc, int pos) throws BadLocationException {
        return this.modelToView(tc, pos, Position.Bias.Forward);
    }

    @Override
    @Deprecated(since="9")
    public Rectangle modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
        return (Rectangle)this.modelToView(tc, pos, bias, false);
    }

    @Override
    public Rectangle2D modelToView2D(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
        return this.modelToView(tc, pos, bias, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Rectangle2D modelToView(JTextComponent tc, int pos, Position.Bias bias, boolean useFPAPI) throws BadLocationException {
        Document doc = this.editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            Rectangle alloc = this.getVisibleEditorRect();
            if (alloc != null) {
                this.rootView.setSize(alloc.width, alloc.height);
                Shape s = this.rootView.modelToView(pos, alloc, bias);
                if (s != null) {
                    Rectangle2D rectangle2D = useFPAPI ? s.getBounds2D() : s.getBounds();
                    return rectangle2D;
                }
            }
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return null;
    }

    @Override
    @Deprecated(since="9")
    public int viewToModel(JTextComponent tc, Point pt) {
        return this.viewToModel(tc, pt, discardBias);
    }

    @Override
    @Deprecated(since="9")
    public int viewToModel(JTextComponent tc, Point pt, Position.Bias[] biasReturn) {
        return this.viewToModel(tc, pt.x, pt.y, biasReturn);
    }

    @Override
    public int viewToModel2D(JTextComponent tc, Point2D pt, Position.Bias[] biasReturn) {
        return this.viewToModel(tc, (float)pt.getX(), (float)pt.getY(), biasReturn);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int viewToModel(JTextComponent tc, float x, float y, Position.Bias[] biasReturn) {
        int offs = -1;
        Document doc = this.editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            Rectangle alloc = this.getVisibleEditorRect();
            if (alloc != null) {
                this.rootView.setSize(alloc.width, alloc.height);
                offs = this.rootView.viewToModel(x, y, alloc, biasReturn);
            }
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return offs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getNextVisualPositionFrom(JTextComponent t, int pos, Position.Bias b, int direction, Position.Bias[] biasRet) throws BadLocationException {
        Document doc = this.editor.getDocument();
        if (pos < -1 || pos > doc.getLength()) {
            throw new BadLocationException("Invalid position", pos);
        }
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            if (this.painted) {
                Rectangle alloc = this.getVisibleEditorRect();
                if (alloc != null) {
                    this.rootView.setSize(alloc.width, alloc.height);
                }
                int n = this.rootView.getNextVisualPositionFrom(pos, b, alloc, direction, biasRet);
                return n;
            }
        }
        finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return -1;
    }

    @Override
    public void damageRange(JTextComponent tc, int p0, int p1) {
        this.damageRange(tc, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void damageRange(JTextComponent t, int p0, int p1, Position.Bias p0Bias, Position.Bias p1Bias) {
        Rectangle alloc;
        if (this.painted && (alloc = this.getVisibleEditorRect()) != null) {
            Document doc = t.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                this.rootView.setSize(alloc.width, alloc.height);
                Shape toDamage = this.rootView.modelToView(p0, p0Bias, p1, p1Bias, alloc);
                Rectangle rect = toDamage instanceof Rectangle ? (Rectangle)toDamage : toDamage.getBounds();
                this.editor.repaint(rect.x, rect.y, rect.width, rect.height);
            }
            catch (BadLocationException badLocationException) {
            }
            finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
    }

    @Override
    public EditorKit getEditorKit(JTextComponent tc) {
        return defaultKit;
    }

    @Override
    public View getRootView(JTextComponent tc) {
        return this.rootView;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getToolTipText(JTextComponent t, Point pt) {
        if (!this.painted) {
            return null;
        }
        Document doc = this.editor.getDocument();
        String tt = null;
        Rectangle alloc = this.getVisibleEditorRect();
        if (alloc != null) {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                tt = this.rootView.getToolTipText(pt.x, pt.y, alloc);
            }
            finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
        return tt;
    }

    @Override
    public View create(Element elem) {
        return null;
    }

    public View create(Element elem, int p0, int p1) {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DragListener getDragListener() {
        Class<DragListener> clazz = DragListener.class;
        synchronized (DragListener.class) {
            DragListener listener = (DragListener)AppContext.getAppContext().get(DragListener.class);
            if (listener == null) {
                listener = new DragListener();
                AppContext.getAppContext().put(DragListener.class, listener);
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return listener;
        }
    }

    class RootView
    extends View {
        private View view;

        RootView() {
            super(null);
        }

        void setView(View v) {
            View oldView = this.view;
            this.view = null;
            if (oldView != null) {
                oldView.setParent(null);
            }
            if (v != null) {
                v.setParent(this);
            }
            this.view = v;
        }

        @Override
        public AttributeSet getAttributes() {
            return null;
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (this.view != null) {
                return this.view.getPreferredSpan(axis);
            }
            return 10.0f;
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (this.view != null) {
                return this.view.getMinimumSpan(axis);
            }
            return 10.0f;
        }

        @Override
        public float getMaximumSpan(int axis) {
            return 2.1474836E9f;
        }

        @Override
        public void preferenceChanged(View child, boolean width, boolean height) {
            BasicTextUI.this.editor.revalidate();
        }

        @Override
        public float getAlignment(int axis) {
            if (this.view != null) {
                return this.view.getAlignment(axis);
            }
            return 0.0f;
        }

        @Override
        public void paint(Graphics g, Shape allocation) {
            if (this.view != null) {
                Rectangle alloc = allocation instanceof Rectangle ? (Rectangle)allocation : allocation.getBounds();
                this.setSize(alloc.width, alloc.height);
                this.view.paint(g, allocation);
            }
        }

        @Override
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }

        @Override
        public int getViewCount() {
            return 1;
        }

        @Override
        public View getView(int n) {
            return this.view;
        }

        @Override
        public int getViewIndex(int pos, Position.Bias b) {
            return 0;
        }

        @Override
        public Shape getChildAllocation(int index, Shape a) {
            return a;
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (this.view != null) {
                return this.view.modelToView(pos, a, b);
            }
            return null;
        }

        @Override
        public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
            if (this.view != null) {
                return this.view.modelToView(p0, b0, p1, b1, a);
            }
            return null;
        }

        @Override
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (this.view != null) {
                int retValue = this.view.viewToModel(x, y, a, bias);
                return retValue;
            }
            return -1;
        }

        @Override
        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
            if (pos < -1 || pos > this.getDocument().getLength()) {
                throw new BadLocationException("invalid position", pos);
            }
            if (this.view != null) {
                int nextPos = this.view.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
                if (nextPos != -1) {
                    pos = nextPos;
                } else {
                    biasRet[0] = b;
                }
            }
            return pos;
        }

        @Override
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (this.view != null) {
                this.view.insertUpdate(e, a, f);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (this.view != null) {
                this.view.removeUpdate(e, a, f);
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (this.view != null) {
                this.view.changedUpdate(e, a, f);
            }
        }

        @Override
        public Document getDocument() {
            return BasicTextUI.this.editor.getDocument();
        }

        @Override
        public int getStartOffset() {
            if (this.view != null) {
                return this.view.getStartOffset();
            }
            return this.getElement().getStartOffset();
        }

        @Override
        public int getEndOffset() {
            if (this.view != null) {
                return this.view.getEndOffset();
            }
            return this.getElement().getEndOffset();
        }

        @Override
        public Element getElement() {
            if (this.view != null) {
                return this.view.getElement();
            }
            return BasicTextUI.this.editor.getDocument().getDefaultRootElement();
        }

        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view");
        }

        @Override
        public int getResizeWeight(int axis) {
            if (this.view != null) {
                return this.view.getResizeWeight(axis);
            }
            return 0;
        }

        @Override
        public void setSize(float width, float height) {
            if (this.view != null) {
                this.view.setSize(width, height);
            }
        }

        @Override
        public Container getContainer() {
            return BasicTextUI.this.editor;
        }

        @Override
        public ViewFactory getViewFactory() {
            EditorKit kit = BasicTextUI.this.getEditorKit(BasicTextUI.this.editor);
            ViewFactory f = kit.getViewFactory();
            if (f != null) {
                return f;
            }
            return BasicTextUI.this;
        }
    }

    class UpdateHandler
    implements PropertyChangeListener,
    DocumentListener,
    LayoutManager2,
    UIResource {
        private Hashtable<Component, Object> constraints;
        private boolean i18nView = false;

        UpdateHandler() {
        }

        @Override
        public final void propertyChange(PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            String propertyName = evt.getPropertyName();
            if (oldValue instanceof Document || newValue instanceof Document) {
                if (oldValue != null) {
                    ((Document)oldValue).removeDocumentListener(this);
                    this.i18nView = false;
                }
                if (newValue != null) {
                    ((Document)newValue).addDocumentListener(this);
                    if ("document" == propertyName) {
                        BasicTextUI.this.setView(null);
                        BasicTextUI.this.propertyChange(evt);
                        BasicTextUI.this.modelChanged();
                        return;
                    }
                }
                BasicTextUI.this.modelChanged();
            }
            if ("focusAccelerator" == propertyName) {
                BasicTextUI.this.updateFocusAcceleratorBinding(true);
            } else if ("componentOrientation" == propertyName) {
                Document document = BasicTextUI.this.editor.getDocument();
                String I18NProperty = "i18n";
                if (ComponentOrientation.RIGHT_TO_LEFT == newValue && !Boolean.TRUE.equals(document.getProperty("i18n"))) {
                    document.putProperty("i18n", Boolean.TRUE);
                }
                BasicTextUI.this.modelChanged();
            } else if ("font" == propertyName) {
                BasicTextUI.this.modelChanged();
            } else if ("dropLocation" == propertyName) {
                this.dropIndexChanged();
            } else if ("editable" == propertyName) {
                BasicTextUI.this.updateCursor();
                BasicTextUI.this.modelChanged();
            }
            BasicTextUI.this.propertyChange(evt);
        }

        private void dropIndexChanged() {
            if (BasicTextUI.this.editor.getDropMode() == DropMode.USE_SELECTION) {
                return;
            }
            JTextComponent.DropLocation dropLocation = BasicTextUI.this.editor.getDropLocation();
            if (dropLocation == null) {
                if (BasicTextUI.this.dropCaret != null) {
                    BasicTextUI.this.dropCaret.deinstall(BasicTextUI.this.editor);
                    BasicTextUI.this.editor.repaint(BasicTextUI.this.dropCaret);
                    BasicTextUI.this.dropCaret = null;
                }
            } else {
                if (BasicTextUI.this.dropCaret == null) {
                    BasicTextUI.this.dropCaret = new BasicCaret();
                    BasicTextUI.this.dropCaret.install(BasicTextUI.this.editor);
                    BasicTextUI.this.dropCaret.setVisible(true);
                }
                BasicTextUI.this.dropCaret.setDot(dropLocation.getIndex(), dropLocation.getBias());
            }
        }

        @Override
        public final void insertUpdate(DocumentEvent e) {
            Boolean i18nFlag;
            Document doc = e.getDocument();
            Object o = doc.getProperty("i18n");
            if (o instanceof Boolean && (i18nFlag = (Boolean)o) != this.i18nView) {
                this.i18nView = i18nFlag;
                BasicTextUI.this.modelChanged();
                return;
            }
            Rectangle alloc = BasicTextUI.this.painted ? BasicTextUI.this.getVisibleEditorRect() : null;
            BasicTextUI.this.rootView.insertUpdate(e, alloc, BasicTextUI.this.rootView.getViewFactory());
        }

        @Override
        public final void removeUpdate(DocumentEvent e) {
            Rectangle alloc = BasicTextUI.this.painted ? BasicTextUI.this.getVisibleEditorRect() : null;
            BasicTextUI.this.rootView.removeUpdate(e, alloc, BasicTextUI.this.rootView.getViewFactory());
        }

        @Override
        public final void changedUpdate(DocumentEvent e) {
            Rectangle alloc = BasicTextUI.this.painted ? BasicTextUI.this.getVisibleEditorRect() : null;
            BasicTextUI.this.rootView.changedUpdate(e, alloc, BasicTextUI.this.rootView.getViewFactory());
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            if (this.constraints != null) {
                this.constraints.remove(comp);
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return null;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void layoutContainer(Container parent) {
            Rectangle alloc;
            if (this.constraints != null && !this.constraints.isEmpty() && (alloc = BasicTextUI.this.getVisibleEditorRect()) != null) {
                Document doc = BasicTextUI.this.editor.getDocument();
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readLock();
                }
                try {
                    BasicTextUI.this.rootView.setSize(alloc.width, alloc.height);
                    Enumeration<Component> components = this.constraints.keys();
                    while (components.hasMoreElements()) {
                        Component comp = components.nextElement();
                        View v = (View)this.constraints.get(comp);
                        Shape ca = this.calculateViewPosition(alloc, v);
                        if (ca == null) continue;
                        Rectangle compAlloc = ca instanceof Rectangle ? (Rectangle)ca : ca.getBounds();
                        comp.setBounds(compAlloc);
                    }
                }
                finally {
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument)doc).readUnlock();
                    }
                }
            }
        }

        Shape calculateViewPosition(Shape alloc, View v) {
            int pos = v.getStartOffset();
            View child = null;
            View parent = BasicTextUI.this.rootView;
            while (parent != null && parent != v) {
                int index = ((View)parent).getViewIndex(pos, Position.Bias.Forward);
                alloc = ((View)parent).getChildAllocation(index, alloc);
                child = ((View)parent).getView(index);
                parent = child;
            }
            return child != null ? alloc : null;
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraint) {
            if (constraint instanceof View) {
                if (this.constraints == null) {
                    this.constraints = new Hashtable(7);
                }
                this.constraints.put(comp, constraint);
            }
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return null;
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.5f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }
    }

    static class DragListener
    extends MouseInputAdapter
    implements DragRecognitionSupport.BeforeDrag {
        private boolean dragStarted;

        DragListener() {
        }

        @Override
        public void dragStarting(MouseEvent me) {
            this.dragStarted = true;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled()) {
                this.dragStarted = false;
                if (this.isDragPossible(e) && DragRecognitionSupport.mousePressed(e)) {
                    e.consume();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled()) {
                if (this.dragStarted) {
                    e.consume();
                }
                DragRecognitionSupport.mouseReleased(e);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled() && (this.dragStarted || DragRecognitionSupport.mouseDragged(e, this))) {
                e.consume();
            }
        }

        protected boolean isDragPossible(MouseEvent e) {
            int mark;
            Caret caret;
            int dot;
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.isEnabled() && (dot = (caret = c.getCaret()).getDot()) != (mark = caret.getMark())) {
                Point p = new Point(e.getX(), e.getY());
                int pos = c.viewToModel(p);
                int p0 = Math.min(dot, mark);
                int p1 = Math.max(dot, mark);
                if (pos >= p0 && pos < p1) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class BasicCaret
    extends DefaultCaret
    implements UIResource {
    }

    public static class BasicHighlighter
    extends DefaultHighlighter
    implements UIResource {
    }

    static class BasicCursor
    extends Cursor
    implements UIResource {
        BasicCursor(int type) {
            super(type);
        }

        BasicCursor(String name) {
            super(name);
        }
    }

    class FocusAction
    extends AbstractAction {
        FocusAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BasicTextUI.this.editor.requestFocus();
        }

        @Override
        public boolean isEnabled() {
            return BasicTextUI.this.editor.isEditable();
        }
    }

    class TextActionWrapper
    extends TextAction {
        TextAction action;

        public TextActionWrapper(TextAction action) {
            super((String)action.getValue("Name"));
            this.action = null;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.action.actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return BasicTextUI.this.editor == null || BasicTextUI.this.editor.isEditable() ? this.action.isEnabled() : false;
        }
    }

    static class TextTransferHandler
    extends TransferHandler
    implements UIResource {
        private JTextComponent exportComp;
        private boolean shouldRemove;
        private int p0;
        private int p1;
        private boolean modeBetween = false;
        private boolean isDrop = false;
        private int dropAction = 2;
        private Position.Bias dropBias;

        TextTransferHandler() {
        }

        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
            DataFlavor plainFlavor = null;
            DataFlavor refFlavor = null;
            DataFlavor stringFlavor = null;
            if (c instanceof JEditorPane) {
                for (int i = 0; i < flavors.length; ++i) {
                    String mime = flavors[i].getMimeType();
                    if (mime.startsWith(((JEditorPane)c).getEditorKit().getContentType())) {
                        return flavors[i];
                    }
                    if (plainFlavor == null && mime.startsWith("text/plain")) {
                        plainFlavor = flavors[i];
                        continue;
                    }
                    if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") && flavors[i].getRepresentationClass() == String.class) {
                        refFlavor = flavors[i];
                        continue;
                    }
                    if (stringFlavor != null || !flavors[i].equals(DataFlavor.stringFlavor)) continue;
                    stringFlavor = flavors[i];
                }
                if (plainFlavor != null) {
                    return plainFlavor;
                }
                if (refFlavor != null) {
                    return refFlavor;
                }
                if (stringFlavor != null) {
                    return stringFlavor;
                }
                return null;
            }
            for (int i = 0; i < flavors.length; ++i) {
                String mime = flavors[i].getMimeType();
                if (mime.startsWith("text/plain")) {
                    return flavors[i];
                }
                if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") && flavors[i].getRepresentationClass() == String.class) {
                    refFlavor = flavors[i];
                    continue;
                }
                if (stringFlavor != null || !flavors[i].equals(DataFlavor.stringFlavor)) continue;
                stringFlavor = flavors[i];
            }
            if (refFlavor != null) {
                return refFlavor;
            }
            if (stringFlavor != null) {
                return stringFlavor;
            }
            return null;
        }

        protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead) throws BadLocationException, IOException {
            if (useRead) {
                int startPosition = c.getSelectionStart();
                int endPosition = c.getSelectionEnd();
                int length = endPosition - startPosition;
                EditorKit kit = c.getUI().getEditorKit(c);
                Document doc = c.getDocument();
                if (length > 0) {
                    doc.remove(startPosition, length);
                }
                kit.read(in, doc, startPosition);
            } else {
                int nch;
                char[] buff = new char[1024];
                boolean lastWasCR = false;
                StringBuilder sbuff = null;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    if (sbuff == null) {
                        sbuff = new StringBuilder(nch);
                    }
                    int last = 0;
                    block5: for (int counter = 0; counter < nch; ++counter) {
                        switch (buff[counter]) {
                            case '\r': {
                                if (lastWasCR) {
                                    if (counter == 0) {
                                        sbuff.append('\n');
                                        continue block5;
                                    }
                                    buff[counter - 1] = 10;
                                    continue block5;
                                }
                                lastWasCR = true;
                                continue block5;
                            }
                            case '\n': {
                                if (!lastWasCR) continue block5;
                                if (counter > last + 1) {
                                    sbuff.append(buff, last, counter - last - 1);
                                }
                                lastWasCR = false;
                                last = counter;
                                continue block5;
                            }
                            default: {
                                if (!lastWasCR) continue block5;
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = 10;
                                }
                                lastWasCR = false;
                            }
                        }
                    }
                    if (last >= nch) continue;
                    if (lastWasCR) {
                        if (last >= nch - 1) continue;
                        sbuff.append(buff, last, nch - last - 1);
                        continue;
                    }
                    sbuff.append(buff, last, nch - last);
                }
                if (lastWasCR) {
                    sbuff.append('\n');
                }
                c.replaceSelection(sbuff != null ? sbuff.toString() : "");
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            if (c instanceof JPasswordField && c.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
                return 0;
            }
            return ((JTextComponent)c).isEditable() ? 3 : 1;
        }

        @Override
        protected Transferable createTransferable(JComponent comp) {
            this.exportComp = (JTextComponent)comp;
            this.shouldRemove = true;
            this.p0 = this.exportComp.getSelectionStart();
            this.p1 = this.exportComp.getSelectionEnd();
            return this.p0 != this.p1 ? new TextTransferable(this.exportComp, this.p0, this.p1) : null;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (this.shouldRemove && action == 2) {
                TextTransferable t = (TextTransferable)data;
                t.removeText();
            }
            this.exportComp = null;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            this.isDrop = support.isDrop();
            if (this.isDrop) {
                this.modeBetween = ((JTextComponent)support.getComponent()).getDropMode() == DropMode.INSERT;
                this.dropBias = ((JTextComponent.DropLocation)support.getDropLocation()).getBias();
                this.dropAction = support.getDropAction();
            }
            try {
                boolean bl = super.importData(support);
                return bl;
            }
            finally {
                this.isDrop = false;
                this.modeBetween = false;
                this.dropBias = null;
                this.dropAction = 2;
            }
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            int pos;
            JTextComponent c = (JTextComponent)comp;
            int n = pos = this.modeBetween ? c.getDropLocation().getIndex() : c.getCaretPosition();
            if (this.dropAction == 2 && c == this.exportComp && pos >= this.p0 && pos <= this.p1) {
                this.shouldRemove = false;
                return true;
            }
            boolean imported = false;
            DataFlavor importFlavor = this.getImportFlavor(t.getTransferDataFlavors(), c);
            if (importFlavor != null) {
                try {
                    Caret caret;
                    InputContext ic;
                    JEditorPane ep;
                    boolean useRead = false;
                    if (comp instanceof JEditorPane && !(ep = (JEditorPane)comp).getContentType().startsWith("text/plain") && importFlavor.getMimeType().startsWith(ep.getContentType())) {
                        useRead = true;
                    }
                    if ((ic = c.getInputContext()) != null) {
                        ic.endComposition();
                    }
                    Reader r = importFlavor.getReaderForText(t);
                    if (this.modeBetween) {
                        caret = c.getCaret();
                        if (caret instanceof DefaultCaret) {
                            ((DefaultCaret)caret).setDot(pos, this.dropBias);
                        } else {
                            c.setCaretPosition(pos);
                        }
                    }
                    this.handleReaderImport(r, c, useRead);
                    if (this.isDrop) {
                        c.requestFocus();
                        caret = c.getCaret();
                        if (caret instanceof DefaultCaret) {
                            int newPos = caret.getDot();
                            Position.Bias newBias = ((DefaultCaret)caret).getDotBias();
                            ((DefaultCaret)caret).setDot(pos, this.dropBias);
                            ((DefaultCaret)caret).moveDot(newPos, newBias);
                        } else {
                            c.select(pos, c.getCaretPosition());
                        }
                    }
                    imported = true;
                }
                catch (UnsupportedFlavorException | IOException | BadLocationException exception) {
                    // empty catch block
                }
            }
            return imported;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            JTextComponent c = (JTextComponent)comp;
            if (!c.isEditable() || !c.isEnabled()) {
                return false;
            }
            return this.getImportFlavor(flavors, c) != null;
        }

        static class TextTransferable
        extends BasicTransferable {
            Position p0;
            Position p1;
            String mimeType;
            String richText;
            JTextComponent c;

            TextTransferable(JTextComponent c, int start, int end) {
                super(null, null);
                this.c = c;
                Document doc = c.getDocument();
                try {
                    this.p0 = doc.createPosition(start);
                    this.p1 = doc.createPosition(end);
                    this.plainData = c.getSelectedText();
                    if (c instanceof JEditorPane) {
                        JEditorPane ep = (JEditorPane)c;
                        this.mimeType = ep.getContentType();
                        if (this.mimeType.startsWith("text/plain")) {
                            return;
                        }
                        StringWriter sw = new StringWriter(this.p1.getOffset() - this.p0.getOffset());
                        ep.getEditorKit().write(sw, doc, this.p0.getOffset(), this.p1.getOffset() - this.p0.getOffset());
                        if (this.mimeType.startsWith("text/html")) {
                            this.htmlData = sw.toString();
                        } else {
                            this.richText = sw.toString();
                        }
                    }
                }
                catch (IOException | BadLocationException exception) {
                    // empty catch block
                }
            }

            void removeText() {
                if (this.p0 != null && this.p1 != null && this.p0.getOffset() != this.p1.getOffset()) {
                    try {
                        Document doc = this.c.getDocument();
                        doc.remove(this.p0.getOffset(), this.p1.getOffset() - this.p0.getOffset());
                    }
                    catch (BadLocationException badLocationException) {
                        // empty catch block
                    }
                }
            }

            @Override
            protected DataFlavor[] getRicherFlavors() {
                if (this.richText == null) {
                    return null;
                }
                try {
                    DataFlavor[] flavors = new DataFlavor[]{new DataFlavor(this.mimeType + ";class=java.lang.String"), new DataFlavor(this.mimeType + ";class=java.io.Reader"), new DataFlavor(this.mimeType + ";class=java.io.InputStream;charset=unicode")};
                    return flavors;
                }
                catch (ClassNotFoundException classNotFoundException) {
                    return null;
                }
            }

            @Override
            protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (this.richText == null) {
                    return null;
                }
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return this.richText;
                }
                if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(this.richText);
                }
                if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new StringBufferInputStream(this.richText);
                }
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
}

