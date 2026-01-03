/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import com.sun.beans.util.Cache;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleEditableText;
import javax.accessibility.AccessibleExtendedText;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleTextSequence;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Keymap;
import javax.swing.text.NavigationFilter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import sun.awt.AppContext;
import sun.swing.PrintingStatus;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;
import sun.swing.text.TextComponentPrintable;

@JavaBean(defaultProperty="UI")
@SwingContainer(value=false)
public abstract class JTextComponent
extends JComponent
implements Scrollable,
Accessible {
    public static final String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey";
    private Document model;
    private transient Caret caret;
    private NavigationFilter navigationFilter;
    private transient Highlighter highlighter;
    private transient Keymap keymap;
    private transient MutableCaretEvent caretEvent;
    private Color caretColor;
    private Color selectionColor;
    private Color selectedTextColor;
    private Color disabledTextColor;
    private boolean editable;
    private Insets margin;
    private char focusAccelerator;
    private boolean dragEnabled;
    private DropMode dropMode = DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    private static DefaultTransferHandler defaultTransferHandler;
    private static Cache<Class<?>, Boolean> METHOD_OVERRIDDEN;
    private static final Object KEYMAP_TABLE;
    private transient InputMethodRequests inputMethodRequestsHandler;
    private SimpleAttributeSet composedTextAttribute;
    private String composedTextContent;
    private Position composedTextStart;
    private Position composedTextEnd;
    private Position latestCommittedTextStart;
    private Position latestCommittedTextEnd;
    private ComposedTextCaret composedTextCaret;
    private transient Caret originalCaret;
    private boolean checkedInputOverride;
    private boolean needToSendKeyTypedEvent;
    private static final Object FOCUSED_COMPONENT;
    public static final String DEFAULT_KEYMAP = "default";

    public JTextComponent() {
        this.enableEvents(2056L);
        this.caretEvent = new MutableCaretEvent(this);
        this.addMouseListener(this.caretEvent);
        this.addFocusListener(this.caretEvent);
        this.setEditable(true);
        this.setDragEnabled(false);
        this.setLayout(null);
        this.updateUI();
    }

    @Override
    public TextUI getUI() {
        return (TextUI)this.ui;
    }

    public void setUI(TextUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        this.setUI((TextUI)UIManager.getUI(this));
        this.invalidate();
    }

    public void addCaretListener(CaretListener listener) {
        this.listenerList.add(CaretListener.class, listener);
    }

    public void removeCaretListener(CaretListener listener) {
        this.listenerList.remove(CaretListener.class, listener);
    }

    @BeanProperty(bound=false)
    public CaretListener[] getCaretListeners() {
        return (CaretListener[])this.listenerList.getListeners(CaretListener.class);
    }

    protected void fireCaretUpdate(CaretEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != CaretListener.class) continue;
            ((CaretListener)listeners[i + 1]).caretUpdate(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @BeanProperty(expert=true, description="the text document model")
    public void setDocument(Document doc) {
        Document old = this.model;
        try {
            Boolean runDir;
            if (old instanceof AbstractDocument) {
                ((AbstractDocument)old).readLock();
            }
            if (this.accessibleContext != null) {
                this.model.removeDocumentListener((AccessibleJTextComponent)this.accessibleContext);
            }
            if (this.inputMethodRequestsHandler != null) {
                this.model.removeDocumentListener((DocumentListener)((Object)this.inputMethodRequestsHandler));
            }
            this.model = doc;
            Boolean bl = runDir = this.getComponentOrientation().isLeftToRight() ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL;
            if (runDir != doc.getProperty(TextAttribute.RUN_DIRECTION)) {
                doc.putProperty(TextAttribute.RUN_DIRECTION, runDir);
            }
            this.firePropertyChange("document", old, doc);
        }
        finally {
            if (old instanceof AbstractDocument) {
                ((AbstractDocument)old).readUnlock();
            }
        }
        this.revalidate();
        this.repaint();
        if (this.accessibleContext != null) {
            this.model.addDocumentListener((AccessibleJTextComponent)this.accessibleContext);
        }
        if (this.inputMethodRequestsHandler != null) {
            this.model.addDocumentListener((DocumentListener)((Object)this.inputMethodRequestsHandler));
        }
    }

    public Document getDocument() {
        return this.model;
    }

    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        Document doc = this.getDocument();
        if (doc != null) {
            Boolean runDir = o.isLeftToRight() ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL;
            doc.putProperty(TextAttribute.RUN_DIRECTION, runDir);
        }
        super.setComponentOrientation(o);
    }

    @BeanProperty(bound=false)
    public Action[] getActions() {
        return this.getUI().getEditorKit(this).getActions();
    }

    @BeanProperty(description="desired space between the border and text area")
    public void setMargin(Insets m) {
        Insets old = this.margin;
        this.margin = m;
        this.firePropertyChange("margin", old, m);
        this.invalidate();
    }

    public Insets getMargin() {
        return this.margin;
    }

    public void setNavigationFilter(NavigationFilter filter) {
        this.navigationFilter = filter;
    }

    public NavigationFilter getNavigationFilter() {
        return this.navigationFilter;
    }

    @Transient
    public Caret getCaret() {
        return this.caret;
    }

    @BeanProperty(expert=true, description="the caret used to select/navigate")
    public void setCaret(Caret c) {
        if (this.caret != null) {
            this.caret.removeChangeListener(this.caretEvent);
            this.caret.deinstall(this);
        }
        Caret old = this.caret;
        this.caret = c;
        if (this.caret != null) {
            this.caret.install(this);
            this.caret.addChangeListener(this.caretEvent);
        }
        this.firePropertyChange("caret", old, this.caret);
    }

    public Highlighter getHighlighter() {
        return this.highlighter;
    }

    @BeanProperty(expert=true, description="object responsible for background highlights")
    public void setHighlighter(Highlighter h) {
        if (this.highlighter != null) {
            this.highlighter.deinstall(this);
        }
        Highlighter old = this.highlighter;
        this.highlighter = h;
        if (this.highlighter != null) {
            this.highlighter.install(this);
        }
        this.firePropertyChange("highlighter", old, h);
    }

    @BeanProperty(description="set of key event to action bindings to use")
    public void setKeymap(Keymap map) {
        Keymap old = this.keymap;
        this.keymap = map;
        this.firePropertyChange("keymap", old, this.keymap);
        this.updateInputMap(old, map);
    }

    @BeanProperty(bound=false, description="determines whether automatic drag handling is enabled")
    public void setDragEnabled(boolean b) {
        JTextComponent.checkDragEnabled(b);
        this.dragEnabled = b;
    }

    private static void checkDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    public boolean getDragEnabled() {
        return this.dragEnabled;
    }

    public final void setDropMode(DropMode dropMode) {
        JTextComponent.checkDropMode(dropMode);
        this.dropMode = dropMode;
    }

    private static void checkDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case USE_SELECTION: 
                case INSERT: {
                    return;
                }
            }
        }
        throw new IllegalArgumentException(String.valueOf((Object)dropMode) + ": Unsupported drop mode for text");
    }

    public final DropMode getDropMode() {
        return this.dropMode;
    }

    DropLocation dropLocationForPoint(Point p) {
        Position.Bias[] bias = new Position.Bias[1];
        int index = this.getUI().viewToModel(this, p, bias);
        if (bias[0] == null) {
            bias[0] = Position.Bias.Forward;
        }
        return new DropLocation(p, index, bias[0]);
    }

    Object setDropLocation(TransferHandler.DropLocation location, Object state, boolean forDrop) {
        Object[] retVal = null;
        DropLocation textLocation = (DropLocation)location;
        if (this.dropMode == DropMode.USE_SELECTION) {
            if (textLocation == null) {
                if (state != null) {
                    Object[] vals = state;
                    if (!forDrop) {
                        if (this.caret instanceof DefaultCaret) {
                            ((DefaultCaret)this.caret).setDot((Integer)vals[0], (Position.Bias)vals[3]);
                            ((DefaultCaret)this.caret).moveDot((Integer)vals[1], (Position.Bias)vals[4]);
                        } else {
                            this.caret.setDot((Integer)vals[0]);
                            this.caret.moveDot((Integer)vals[1]);
                        }
                    }
                    this.caret.setVisible((Boolean)vals[2]);
                }
            } else {
                if (this.dropLocation == null) {
                    if (this.caret instanceof DefaultCaret) {
                        DefaultCaret dc = (DefaultCaret)this.caret;
                        visible = dc.isActive();
                        retVal = new Object[]{dc.getMark(), dc.getDot(), visible, dc.getMarkBias(), dc.getDotBias()};
                    } else {
                        visible = this.caret.isVisible();
                        retVal = new Object[]{this.caret.getMark(), this.caret.getDot(), visible};
                    }
                    this.caret.setVisible(true);
                } else {
                    retVal = state;
                }
                if (this.caret instanceof DefaultCaret) {
                    ((DefaultCaret)this.caret).setDot(textLocation.getIndex(), textLocation.getBias());
                } else {
                    this.caret.setDot(textLocation.getIndex());
                }
            }
        } else if (textLocation == null) {
            if (state != null) {
                this.caret.setVisible((Boolean)state);
            }
        } else if (this.dropLocation == null) {
            boolean visible = this.caret instanceof DefaultCaret ? ((DefaultCaret)this.caret).isActive() : this.caret.isVisible();
            retVal = visible;
            this.caret.setVisible(false);
        } else {
            retVal = state;
        }
        DropLocation old = this.dropLocation;
        this.dropLocation = textLocation;
        this.firePropertyChange("dropLocation", old, this.dropLocation);
        return retVal;
    }

    @BeanProperty(bound=false)
    public final DropLocation getDropLocation() {
        return this.dropLocation;
    }

    void updateInputMap(Keymap oldKm, Keymap newKm) {
        ActionMap am;
        InputMap km;
        InputMap last = km = this.getInputMap(0);
        while (km != null && !(km instanceof KeymapWrapper)) {
            last = km;
            km = km.getParent();
        }
        if (km != null) {
            if (newKm == null) {
                if (last != km) {
                    last.setParent(km.getParent());
                } else {
                    last.setParent(null);
                }
            } else {
                newKM = new KeymapWrapper(newKm);
                last.setParent(newKM);
                if (last != km) {
                    newKM.setParent(km.getParent());
                }
            }
        } else if (newKm != null && (km = this.getInputMap(0)) != null) {
            newKM = new KeymapWrapper(newKm);
            newKM.setParent(km.getParent());
            km.setParent(newKM);
        }
        ActionMap lastAM = am = this.getActionMap();
        while (am != null && !(am instanceof KeymapActionMap)) {
            lastAM = am;
            am = am.getParent();
        }
        if (am != null) {
            if (newKm == null) {
                if (lastAM != am) {
                    lastAM.setParent(am.getParent());
                } else {
                    lastAM.setParent(null);
                }
            } else {
                KeymapActionMap newAM = new KeymapActionMap(newKm);
                lastAM.setParent(newAM);
                if (lastAM != am) {
                    newAM.setParent(am.getParent());
                }
            }
        } else if (newKm != null && (am = this.getActionMap()) != null) {
            KeymapActionMap newAM = new KeymapActionMap(newKm);
            newAM.setParent(am.getParent());
            am.setParent(newAM);
        }
    }

    public Keymap getKeymap() {
        return this.keymap;
    }

    public static Keymap addKeymap(String nm, Keymap parent) {
        DefaultKeymap map = new DefaultKeymap(nm, parent);
        if (nm != null) {
            JTextComponent.getKeymapTable().put(nm, map);
        }
        return map;
    }

    public static Keymap removeKeymap(String nm) {
        return JTextComponent.getKeymapTable().remove(nm);
    }

    public static Keymap getKeymap(String nm) {
        return JTextComponent.getKeymapTable().get(nm);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static HashMap<String, Keymap> getKeymapTable() {
        Object object = KEYMAP_TABLE;
        synchronized (object) {
            AppContext appContext = AppContext.getAppContext();
            HashMap keymapTable = (HashMap)appContext.get(KEYMAP_TABLE);
            if (keymapTable == null) {
                keymapTable = new HashMap(17);
                appContext.put(KEYMAP_TABLE, keymapTable);
                Keymap binding = JTextComponent.addKeymap(DEFAULT_KEYMAP, null);
                binding.setDefaultAction(new DefaultEditorKit.DefaultKeyTypedAction());
            }
            return keymapTable;
        }
    }

    public static void loadKeymap(Keymap map, KeyBinding[] bindings, Action[] actions) {
        Hashtable<String, Action> h = new Hashtable<String, Action>();
        for (Action a : actions) {
            String value = (String)a.getValue("Name");
            h.put(value != null ? value : "", a);
        }
        for (KeyBinding binding : bindings) {
            Action a = (Action)h.get(binding.actionName);
            if (a == null) continue;
            map.addActionForKeyStroke(binding.key, a);
        }
    }

    public Color getCaretColor() {
        return this.caretColor;
    }

    @BeanProperty(preferred=true, description="the color used to render the caret")
    public void setCaretColor(Color c) {
        Color old = this.caretColor;
        this.caretColor = c;
        this.firePropertyChange("caretColor", old, this.caretColor);
    }

    public Color getSelectionColor() {
        return this.selectionColor;
    }

    @BeanProperty(preferred=true, description="color used to render selection background")
    public void setSelectionColor(Color c) {
        Color old = this.selectionColor;
        this.selectionColor = c;
        this.firePropertyChange("selectionColor", old, this.selectionColor);
    }

    public Color getSelectedTextColor() {
        return this.selectedTextColor;
    }

    @BeanProperty(preferred=true, description="color used to render selected text")
    public void setSelectedTextColor(Color c) {
        Color old = this.selectedTextColor;
        this.selectedTextColor = c;
        this.firePropertyChange("selectedTextColor", old, this.selectedTextColor);
    }

    public Color getDisabledTextColor() {
        return this.disabledTextColor;
    }

    @BeanProperty(preferred=true, description="color used to render disabled text")
    public void setDisabledTextColor(Color c) {
        Color old = this.disabledTextColor;
        this.disabledTextColor = c;
        this.firePropertyChange("disabledTextColor", old, this.disabledTextColor);
    }

    public void replaceSelection(String content) {
        Document doc = this.getDocument();
        if (doc != null) {
            try {
                boolean composedTextSaved = this.saveComposedText(this.caret.getDot());
                int p0 = Math.min(this.caret.getDot(), this.caret.getMark());
                int p1 = Math.max(this.caret.getDot(), this.caret.getMark());
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).replace(p0, p1 - p0, content, null);
                } else {
                    if (p0 != p1) {
                        doc.remove(p0, p1 - p0);
                    }
                    if (content != null && content.length() > 0) {
                        doc.insertString(p0, content, null);
                    }
                }
                if (composedTextSaved) {
                    this.restoreComposedText();
                }
            }
            catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
            }
        }
    }

    public String getText(int offs, int len) throws BadLocationException {
        return this.getDocument().getText(offs, len);
    }

    @Deprecated(since="9")
    public Rectangle modelToView(int pos) throws BadLocationException {
        return this.getUI().modelToView(this, pos);
    }

    public Rectangle2D modelToView2D(int pos) throws BadLocationException {
        return this.getUI().modelToView2D(this, pos, Position.Bias.Forward);
    }

    @Deprecated(since="9")
    public int viewToModel(Point pt) {
        return this.getUI().viewToModel(this, pt);
    }

    public int viewToModel2D(Point2D pt) {
        return this.getUI().viewToModel2D(this, pt, new Position.Bias[1]);
    }

    public void cut() {
        if (this.isEditable() && this.isEnabled()) {
            this.invokeAction("cut", TransferHandler.getCutAction());
        }
    }

    public void copy() {
        this.invokeAction("copy", TransferHandler.getCopyAction());
    }

    public void paste() {
        if (this.isEditable() && this.isEnabled()) {
            this.invokeAction("paste", TransferHandler.getPasteAction());
        }
    }

    private void invokeAction(String name, Action altAction) {
        ActionMap map = this.getActionMap();
        Action action = null;
        if (map != null) {
            action = map.get(name);
        }
        if (action == null) {
            this.installDefaultTransferHandlerIfNecessary();
            action = altAction;
        }
        action.actionPerformed(new ActionEvent(this, 1001, (String)action.getValue("Name"), EventQueue.getMostRecentEventTime(), this.getCurrentEventModifiers()));
    }

    private void installDefaultTransferHandlerIfNecessary() {
        if (this.getTransferHandler() == null) {
            if (defaultTransferHandler == null) {
                defaultTransferHandler = new DefaultTransferHandler();
            }
            this.setTransferHandler(defaultTransferHandler);
        }
    }

    public void moveCaretPosition(int pos) {
        Document doc = this.getDocument();
        if (doc != null) {
            if (pos > doc.getLength() || pos < 0) {
                throw new IllegalArgumentException("bad position: " + pos);
            }
            this.caret.moveDot(pos);
        }
    }

    @BeanProperty(description="accelerator character used to grab focus")
    public void setFocusAccelerator(char aKey) {
        aKey = Character.toUpperCase(aKey);
        char old = this.focusAccelerator;
        this.focusAccelerator = aKey;
        this.firePropertyChange(FOCUS_ACCELERATOR_KEY, old, this.focusAccelerator);
        this.firePropertyChange("focusAccelerator", old, this.focusAccelerator);
    }

    public char getFocusAccelerator() {
        return this.focusAccelerator;
    }

    public void read(Reader in, Object desc) throws IOException {
        EditorKit kit = this.getUI().getEditorKit(this);
        Document doc = kit.createDefaultDocument();
        if (desc != null) {
            doc.putProperty("stream", desc);
        }
        try {
            kit.read(in, doc, 0);
            this.setDocument(doc);
        }
        catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void write(Writer out) throws IOException {
        Document doc = this.getDocument();
        try {
            this.getUI().getEditorKit(this).write(out, doc, 0, doc.getLength());
        }
        catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (JTextComponent.getFocusedComponent() == this) {
            AppContext.getAppContext().remove(FOCUSED_COMPONENT);
        }
    }

    @BeanProperty(bound=false, description="the caret position")
    public void setCaretPosition(int position) {
        Document doc = this.getDocument();
        if (doc != null) {
            if (position > doc.getLength() || position < 0) {
                throw new IllegalArgumentException("bad position: " + position);
            }
            this.caret.setDot(position);
        }
    }

    @Transient
    public int getCaretPosition() {
        return this.caret.getDot();
    }

    @BeanProperty(bound=false, description="the text of this component")
    public void setText(String t) {
        try {
            Document doc = this.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).replace(0, doc.getLength(), t, null);
            } else {
                doc.remove(0, doc.getLength());
                doc.insertString(0, t, null);
            }
        }
        catch (BadLocationException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
    }

    public String getText() {
        String txt;
        Document doc = this.getDocument();
        try {
            txt = doc.getText(0, doc.getLength());
        }
        catch (BadLocationException e) {
            txt = null;
        }
        return txt;
    }

    @BeanProperty(bound=false)
    public String getSelectedText() {
        int p1;
        String txt = null;
        int p0 = Math.min(this.caret.getDot(), this.caret.getMark());
        if (p0 != (p1 = Math.max(this.caret.getDot(), this.caret.getMark()))) {
            try {
                Document doc = this.getDocument();
                txt = doc.getText(p0, p1 - p0);
            }
            catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return txt;
    }

    public boolean isEditable() {
        return this.editable;
    }

    @BeanProperty(description="specifies if the text can be edited")
    public void setEditable(boolean b) {
        if (b != this.editable) {
            boolean oldVal = this.editable;
            this.editable = b;
            this.enableInputMethods(this.editable);
            this.firePropertyChange("editable", (Object)oldVal, (Object)this.editable);
            this.repaint();
        }
    }

    @Transient
    public int getSelectionStart() {
        int start = Math.min(this.caret.getDot(), this.caret.getMark());
        return start;
    }

    @BeanProperty(bound=false, description="starting location of the selection.")
    public void setSelectionStart(int selectionStart) {
        this.select(selectionStart, this.getSelectionEnd());
    }

    @Transient
    public int getSelectionEnd() {
        int end = Math.max(this.caret.getDot(), this.caret.getMark());
        return end;
    }

    @BeanProperty(bound=false, description="ending location of the selection.")
    public void setSelectionEnd(int selectionEnd) {
        this.select(this.getSelectionStart(), selectionEnd);
    }

    public void select(int selectionStart, int selectionEnd) {
        int docLength = this.getDocument().getLength();
        if (selectionStart < 0) {
            selectionStart = 0;
        }
        if (selectionStart > docLength) {
            selectionStart = docLength;
        }
        if (selectionEnd > docLength) {
            selectionEnd = docLength;
        }
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
        }
        this.setCaretPosition(selectionStart);
        this.moveCaretPosition(selectionEnd);
    }

    public void selectAll() {
        Document doc = this.getDocument();
        if (doc != null) {
            this.setCaretPosition(0);
            this.moveCaretPosition(doc.getLength());
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        TextUI ui;
        String retValue = super.getToolTipText(event);
        if (retValue == null && (ui = this.getUI()) != null) {
            retValue = ui.getToolTipText(this, new Point(event.getX(), event.getY()));
        }
        return retValue;
    }

    @Override
    @BeanProperty(bound=false)
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
            case 1: {
                return visibleRect.height / 10;
            }
            case 0: {
                return visibleRect.width / 10;
            }
        }
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
            case 1: {
                return visibleRect.height;
            }
            case 0: {
                return visibleRect.width;
            }
        }
        throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportWidth() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getWidth() > this.getPreferredSize().width;
        }
        return false;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportHeight() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getHeight() > this.getPreferredSize().height;
        }
        return false;
    }

    public boolean print() throws PrinterException {
        return this.print(null, null, true, null, null, true);
    }

    public boolean print(MessageFormat headerFormat, MessageFormat footerFormat) throws PrinterException {
        return this.print(headerFormat, footerFormat, true, null, null, true);
    }

    public boolean print(MessageFormat headerFormat, MessageFormat footerFormat, boolean showPrintDialog, PrintService service, PrintRequestAttributeSet attributes, boolean interactive) throws PrinterException {
        PrintRequestAttributeSet attr;
        Printable printable;
        PrintingStatus printingStatus;
        final PrinterJob job = PrinterJob.getPrinterJob();
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        final boolean isEventDispatchThread = SwingUtilities.isEventDispatchThread();
        Printable textPrintable = this.getPrintable(headerFormat, footerFormat);
        if (interactive && !isHeadless) {
            printingStatus = PrintingStatus.createPrintingStatus(this, job);
            printable = printingStatus.createNotificationPrintable(textPrintable);
        } else {
            printingStatus = null;
            printable = textPrintable;
        }
        if (service != null) {
            job.setPrintService(service);
        }
        job.setPrintable(printable);
        PrintRequestAttributeSet printRequestAttributeSet = attr = attributes == null ? new HashPrintRequestAttributeSet() : attributes;
        if (showPrintDialog && !isHeadless && !job.printDialog(attr)) {
            return false;
        }
        Callable<Object> doPrint = new Callable<Object>(){

            @Override
            public Object call() throws Exception {
                try {
                    job.print(attr);
                }
                finally {
                    if (printingStatus != null) {
                        printingStatus.dispose();
                    }
                }
                return null;
            }
        };
        final FutureTask<Object> futurePrinting = new FutureTask<Object>(doPrint);
        Runnable runnablePrinting = new Runnable(){
            final /* synthetic */ JTextComponent this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public void run() {
                boolean wasEnabled = false;
                if (isEventDispatchThread) {
                    if (this.this$0.isEnabled()) {
                        wasEnabled = true;
                        this.this$0.setEnabled(false);
                    }
                } else {
                    try {
                        wasEnabled = SwingUtilities2.submit(new Callable<Boolean>(){

                            @Override
                            public Boolean call() throws Exception {
                                boolean rv = this$0.isEnabled();
                                if (rv) {
                                    this$0.setEnabled(false);
                                }
                                return rv;
                            }
                        }).get();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof Error) {
                            throw (Error)cause;
                        }
                        if (cause instanceof RuntimeException) {
                            throw (RuntimeException)cause;
                        }
                        throw new AssertionError((Object)cause);
                    }
                }
                this.this$0.getDocument().render(futurePrinting);
                if (wasEnabled) {
                    if (isEventDispatchThread) {
                        this.this$0.setEnabled(true);
                    } else {
                        try {
                            SwingUtilities2.submit(new Runnable(){

                                @Override
                                public void run() {
                                    this$0.setEnabled(true);
                                }
                            }, null).get();
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        catch (ExecutionException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof Error) {
                                throw (Error)cause;
                            }
                            if (cause instanceof RuntimeException) {
                                throw (RuntimeException)cause;
                            }
                            throw new AssertionError((Object)cause);
                        }
                    }
                }
            }
        };
        if (!interactive || isHeadless) {
            runnablePrinting.run();
        } else if (isEventDispatchThread) {
            new Thread(null, runnablePrinting, "JTextComponentPrint", 0L, false).start();
            printingStatus.showModal(true);
        } else {
            printingStatus.showModal(false);
            runnablePrinting.run();
        }
        try {
            futurePrinting.get();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof PrinterAbortException) {
                if (printingStatus != null && printingStatus.isAborted()) {
                    return false;
                }
                throw (PrinterAbortException)cause;
            }
            if (cause instanceof PrinterException) {
                throw (PrinterException)cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            throw new AssertionError((Object)cause);
        }
        return true;
    }

    public Printable getPrintable(MessageFormat headerFormat, MessageFormat footerFormat) {
        return TextComponentPrintable.getPrintable(this, headerFormat, footerFormat);
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJTextComponent();
        }
        return this.accessibleContext;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField f = s.readFields();
        this.model = (Document)f.get("model", null);
        this.navigationFilter = (NavigationFilter)f.get("navigationFilter", null);
        this.caretColor = (Color)f.get("caretColor", null);
        this.selectionColor = (Color)f.get("selectionColor", null);
        this.selectedTextColor = (Color)f.get("selectedTextColor", null);
        this.disabledTextColor = (Color)f.get("disabledTextColor", null);
        this.editable = f.get("editable", false);
        this.margin = (Insets)f.get("margin", null);
        this.focusAccelerator = f.get("focusAccelerator", '\u0000');
        boolean newDragEnabled = f.get("dragEnabled", false);
        JTextComponent.checkDragEnabled(newDragEnabled);
        this.dragEnabled = newDragEnabled;
        DropMode newDropMode = (DropMode)((Object)f.get("dropMode", (Object)DropMode.USE_SELECTION));
        JTextComponent.checkDropMode(newDropMode);
        this.dropMode = newDropMode;
        this.composedTextAttribute = (SimpleAttributeSet)f.get("composedTextAttribute", null);
        this.composedTextContent = (String)f.get("composedTextContent", null);
        this.composedTextStart = (Position)f.get("composedTextStart", null);
        this.composedTextEnd = (Position)f.get("composedTextEnd", null);
        this.latestCommittedTextStart = (Position)f.get("latestCommittedTextStart", null);
        this.latestCommittedTextEnd = (Position)f.get("latestCommittedTextEnd", null);
        this.composedTextCaret = (ComposedTextCaret)f.get("composedTextCaret", null);
        this.checkedInputOverride = f.get("checkedInputOverride", false);
        this.needToSendKeyTypedEvent = f.get("needToSendKeyTypedEvent", false);
        this.caretEvent = new MutableCaretEvent(this);
        this.addMouseListener(this.caretEvent);
        this.addFocusListener(this.caretEvent);
    }

    @Override
    protected String paramString() {
        String editableString = this.editable ? "true" : "false";
        String caretColorString = this.caretColor != null ? this.caretColor.toString() : "";
        String selectionColorString = this.selectionColor != null ? this.selectionColor.toString() : "";
        String selectedTextColorString = this.selectedTextColor != null ? this.selectedTextColor.toString() : "";
        String disabledTextColorString = this.disabledTextColor != null ? this.disabledTextColor.toString() : "";
        String marginString = this.margin != null ? this.margin.toString() : "";
        return super.paramString() + ",caretColor=" + caretColorString + ",disabledTextColor=" + disabledTextColorString + ",editable=" + editableString + ",margin=" + marginString + ",selectedTextColor=" + selectedTextColorString + ",selectionColor=" + selectionColorString;
    }

    static final JTextComponent getFocusedComponent() {
        return (JTextComponent)AppContext.getAppContext().get(FOCUSED_COMPONENT);
    }

    private int getCurrentEventModifiers() {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent)currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent)currentEvent).getModifiers();
        }
        return modifiers;
    }

    @Override
    protected void processInputMethodEvent(InputMethodEvent e) {
        super.processInputMethodEvent(e);
        if (!e.isConsumed()) {
            if (!this.isEditable()) {
                return;
            }
            switch (e.getID()) {
                case 1100: {
                    this.replaceInputMethodText(e);
                }
                case 1101: {
                    this.setInputMethodCaretPosition(e);
                }
            }
            e.consume();
        }
    }

    @Override
    @BeanProperty(bound=false)
    public InputMethodRequests getInputMethodRequests() {
        if (this.inputMethodRequestsHandler == null) {
            this.inputMethodRequestsHandler = new InputMethodRequestsHandler();
            Document doc = this.getDocument();
            if (doc != null) {
                doc.addDocumentListener((DocumentListener)((Object)this.inputMethodRequestsHandler));
            }
        }
        return this.inputMethodRequestsHandler;
    }

    @Override
    public void addInputMethodListener(InputMethodListener l) {
        super.addInputMethodListener(l);
        if (l != null) {
            this.needToSendKeyTypedEvent = false;
            this.checkedInputOverride = true;
        }
    }

    private void replaceInputMethodText(InputMethodEvent e) {
        int commitCount = e.getCommittedCharacterCount();
        AttributedCharacterIterator text = e.getText();
        Document doc = this.getDocument();
        if (this.composedTextExists()) {
            try {
                doc.remove(this.composedTextStart.getOffset(), this.composedTextEnd.getOffset() - this.composedTextStart.getOffset());
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
            this.composedTextEnd = null;
            this.composedTextStart = null;
            this.composedTextAttribute = null;
            this.composedTextContent = null;
        }
        if (text != null) {
            int composedTextIndex;
            text.first();
            int committedTextStartIndex = 0;
            int committedTextEndIndex = 0;
            if (commitCount > 0) {
                committedTextStartIndex = this.caret.getDot();
                if (this.shouldSynthensizeKeyEvents()) {
                    char c = text.current();
                    while (commitCount > 0) {
                        KeyEvent ke = new KeyEvent(this, 400, EventQueue.getMostRecentEventTime(), 0, 0, c);
                        this.processKeyEvent(ke);
                        c = text.next();
                        --commitCount;
                    }
                } else {
                    StringBuilder strBuf = new StringBuilder();
                    char c = text.current();
                    while (commitCount > 0) {
                        strBuf.append(c);
                        c = text.next();
                        --commitCount;
                    }
                    this.mapCommittedTextToAction(strBuf.toString());
                }
                committedTextEndIndex = this.caret.getDot();
            }
            if ((composedTextIndex = text.getIndex()) < text.getEndIndex()) {
                this.createComposedTextAttribute(composedTextIndex, text);
                try {
                    this.replaceSelection(null);
                    doc.insertString(this.caret.getDot(), this.composedTextContent, this.composedTextAttribute);
                    this.composedTextStart = doc.createPosition(this.caret.getDot() - this.composedTextContent.length());
                    this.composedTextEnd = doc.createPosition(this.caret.getDot());
                }
                catch (BadLocationException ble) {
                    this.composedTextEnd = null;
                    this.composedTextStart = null;
                    this.composedTextAttribute = null;
                    this.composedTextContent = null;
                }
            }
            if (committedTextStartIndex != committedTextEndIndex) {
                try {
                    this.latestCommittedTextStart = doc.createPosition(committedTextStartIndex);
                    this.latestCommittedTextEnd = doc.createPosition(committedTextEndIndex);
                }
                catch (BadLocationException ble) {
                    this.latestCommittedTextEnd = null;
                    this.latestCommittedTextStart = null;
                }
            } else {
                this.latestCommittedTextEnd = null;
                this.latestCommittedTextStart = null;
            }
        }
    }

    private void createComposedTextAttribute(int composedIndex, AttributedCharacterIterator text) {
        Document doc = this.getDocument();
        StringBuilder strBuf = new StringBuilder();
        char c = text.setIndex(composedIndex);
        while (c != '\uffff') {
            strBuf.append(c);
            c = text.next();
        }
        this.composedTextContent = strBuf.toString();
        this.composedTextAttribute = new SimpleAttributeSet();
        this.composedTextAttribute.addAttribute(StyleConstants.ComposedTextAttribute, new AttributedString(text, composedIndex, text.getEndIndex()));
    }

    protected boolean saveComposedText(int pos) {
        if (this.composedTextExists()) {
            int start = this.composedTextStart.getOffset();
            int len = this.composedTextEnd.getOffset() - this.composedTextStart.getOffset();
            if (pos >= start && pos <= start + len) {
                try {
                    this.getDocument().remove(start, len);
                    return true;
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }
        return false;
    }

    protected void restoreComposedText() {
        Document doc = this.getDocument();
        try {
            doc.insertString(this.caret.getDot(), this.composedTextContent, this.composedTextAttribute);
            this.composedTextStart = doc.createPosition(this.caret.getDot() - this.composedTextContent.length());
            this.composedTextEnd = doc.createPosition(this.caret.getDot());
        }
        catch (BadLocationException badLocationException) {
            // empty catch block
        }
    }

    private void mapCommittedTextToAction(String committedText) {
        Keymap binding = this.getKeymap();
        if (binding != null) {
            Action a = null;
            if (committedText.length() == 1) {
                KeyStroke k = KeyStroke.getKeyStroke(committedText.charAt(0));
                a = binding.getAction(k);
            }
            if (a == null) {
                a = binding.getDefaultAction();
            }
            if (a != null) {
                ActionEvent ae = new ActionEvent(this, 1001, committedText, EventQueue.getMostRecentEventTime(), this.getCurrentEventModifiers());
                a.actionPerformed(ae);
            }
        }
    }

    private void setInputMethodCaretPosition(InputMethodEvent e) {
        if (this.composedTextExists()) {
            TextHitInfo caretPos;
            int dot = this.composedTextStart.getOffset();
            if (!(this.caret instanceof ComposedTextCaret)) {
                if (this.composedTextCaret == null) {
                    this.composedTextCaret = new ComposedTextCaret();
                }
                this.originalCaret = this.caret;
                this.exchangeCaret(this.originalCaret, this.composedTextCaret);
            }
            if ((caretPos = e.getCaret()) != null) {
                int index = caretPos.getInsertionIndex();
                dot += index;
                if (index == 0) {
                    try {
                        Rectangle d = this.modelToView(dot);
                        Rectangle end = this.modelToView(this.composedTextEnd.getOffset());
                        Rectangle b = this.getBounds();
                        d.x += Math.min(end.x - d.x, b.width);
                        this.scrollRectToVisible(d);
                    }
                    catch (BadLocationException badLocationException) {
                        // empty catch block
                    }
                }
            }
            this.caret.setDot(dot);
        } else if (this.caret instanceof ComposedTextCaret) {
            int dot = this.caret.getDot();
            this.exchangeCaret(this.caret, this.originalCaret);
            this.caret.setDot(dot);
        }
    }

    private void exchangeCaret(Caret oldCaret, Caret newCaret) {
        int blinkRate = oldCaret.getBlinkRate();
        this.setCaret(newCaret);
        this.caret.setBlinkRate(blinkRate);
        this.caret.setVisible(this.hasFocus());
    }

    private boolean shouldSynthensizeKeyEvents() {
        if (!this.checkedInputOverride) {
            this.needToSendKeyTypedEvent = METHOD_OVERRIDDEN.get(this.getClass()) == false;
            this.checkedInputOverride = true;
        }
        return this.needToSendKeyTypedEvent;
    }

    boolean composedTextExists() {
        return this.composedTextStart != null;
    }

    static {
        SwingAccessor.setJTextComponentAccessor(new SwingAccessor.JTextComponentAccessor(){

            @Override
            public TransferHandler.DropLocation dropLocationForPoint(JTextComponent textComp, Point p) {
                return textComp.dropLocationForPoint(p);
            }

            @Override
            public Object setDropLocation(JTextComponent textComp, TransferHandler.DropLocation location, Object state, boolean forDrop) {
                return textComp.setDropLocation(location, state, forDrop);
            }
        });
        METHOD_OVERRIDDEN = new Cache<Class<?>, Boolean>(Cache.Kind.WEAK, Cache.Kind.STRONG){

            @Override
            public Boolean create(final Class<?> type) {
                if (JTextComponent.class == type) {
                    return Boolean.FALSE;
                }
                if (((Boolean)this.get(type.getSuperclass())).booleanValue()) {
                    return Boolean.TRUE;
                }
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

                    @Override
                    public Boolean run() {
                        try {
                            type.getDeclaredMethod("processInputMethodEvent", InputMethodEvent.class);
                            return Boolean.TRUE;
                        }
                        catch (NoSuchMethodException exception) {
                            return Boolean.FALSE;
                        }
                    }
                });
            }
        };
        KEYMAP_TABLE = new StringBuilder("JTextComponent_KeymapTable");
        FOCUSED_COMPONENT = new StringBuilder("JTextComponent_FocusedComponent");
    }

    static class MutableCaretEvent
    extends CaretEvent
    implements ChangeListener,
    FocusListener,
    MouseListener {
        private boolean dragActive;
        private int dot;
        private int mark;

        MutableCaretEvent(JTextComponent c) {
            super(c);
        }

        final void fire() {
            JTextComponent c = (JTextComponent)this.getSource();
            if (c != null) {
                Caret caret = c.getCaret();
                this.dot = caret.getDot();
                this.mark = caret.getMark();
                c.fireCaretUpdate(this);
            }
        }

        @Override
        public final String toString() {
            return "dot=" + this.dot + ",mark=" + this.mark;
        }

        @Override
        public final int getDot() {
            return this.dot;
        }

        @Override
        public final int getMark() {
            return this.mark;
        }

        @Override
        public final void stateChanged(ChangeEvent e) {
            if (!this.dragActive) {
                this.fire();
            }
        }

        @Override
        public void focusGained(FocusEvent fe) {
            AppContext.getAppContext().put(FOCUSED_COMPONENT, fe.getSource());
        }

        @Override
        public void focusLost(FocusEvent fe) {
        }

        @Override
        public final void mousePressed(MouseEvent e) {
            this.dragActive = true;
        }

        @Override
        public final void mouseReleased(MouseEvent e) {
            this.dragActive = false;
            this.fire();
        }

        @Override
        public final void mouseClicked(MouseEvent e) {
        }

        @Override
        public final void mouseEntered(MouseEvent e) {
        }

        @Override
        public final void mouseExited(MouseEvent e) {
        }
    }

    public class AccessibleJTextComponent
    extends JComponent.AccessibleJComponent
    implements AccessibleText,
    CaretListener,
    DocumentListener,
    AccessibleAction,
    AccessibleEditableText,
    AccessibleExtendedText {
        int caretPos;
        Point oldLocationOnScreen;

        public AccessibleJTextComponent() {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null) {
                doc.addDocumentListener(this);
            }
            JTextComponent.this.addCaretListener(this);
            this.caretPos = this.getCaretPosition();
            try {
                this.oldLocationOnScreen = this.getLocationOnScreen();
            }
            catch (IllegalComponentStateException illegalComponentStateException) {
                // empty catch block
            }
            JTextComponent.this.addComponentListener(new ComponentAdapter(this){
                final /* synthetic */ AccessibleJTextComponent this$1;
                {
                    this.this$1 = this$1;
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    try {
                        Point newLocationOnScreen = this.this$1.getLocationOnScreen();
                        this.this$1.firePropertyChange("AccessibleVisibleData", this.this$1.oldLocationOnScreen, newLocationOnScreen);
                        this.this$1.oldLocationOnScreen = newLocationOnScreen;
                    }
                    catch (IllegalComponentStateException illegalComponentStateException) {
                        // empty catch block
                    }
                }
            });
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            int dot = e.getDot();
            int mark = e.getMark();
            if (this.caretPos != dot) {
                this.firePropertyChange("AccessibleCaret", this.caretPos, dot);
                this.caretPos = dot;
                try {
                    this.oldLocationOnScreen = this.getLocationOnScreen();
                }
                catch (IllegalComponentStateException illegalComponentStateException) {
                    // empty catch block
                }
            }
            if (mark != dot) {
                this.firePropertyChange("AccessibleSelection", null, this.getSelectedText());
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            final Integer pos = e.getOffset();
            if (SwingUtilities.isEventDispatchThread()) {
                this.firePropertyChange("AccessibleText", null, pos);
            } else {
                Runnable doFire = new Runnable(){
                    final /* synthetic */ AccessibleJTextComponent this$1;
                    {
                        this.this$1 = this$1;
                    }

                    @Override
                    public void run() {
                        this.this$1.firePropertyChange("AccessibleText", null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            final Integer pos = e.getOffset();
            if (SwingUtilities.isEventDispatchThread()) {
                this.firePropertyChange("AccessibleText", null, pos);
            } else {
                Runnable doFire = new Runnable(){
                    final /* synthetic */ AccessibleJTextComponent this$1;
                    {
                        this.this$1 = this$1;
                    }

                    @Override
                    public void run() {
                        this.this$1.firePropertyChange("AccessibleText", null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            final Integer pos = e.getOffset();
            if (SwingUtilities.isEventDispatchThread()) {
                this.firePropertyChange("AccessibleText", null, pos);
            } else {
                Runnable doFire = new Runnable(){
                    final /* synthetic */ AccessibleJTextComponent this$1;
                    {
                        this.this$1 = this$1;
                    }

                    @Override
                    public void run() {
                        this.this$1.firePropertyChange("AccessibleText", null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JTextComponent.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleText getAccessibleText() {
            return this;
        }

        @Override
        public int getIndexAtPoint(Point p) {
            if (p == null) {
                return -1;
            }
            return JTextComponent.this.viewToModel(p);
        }

        Rectangle getRootEditorRect() {
            Rectangle alloc = JTextComponent.this.getBounds();
            if (alloc.width > 0 && alloc.height > 0) {
                alloc.y = 0;
                alloc.x = 0;
                Insets insets = JTextComponent.this.getInsets();
                alloc.x += insets.left;
                alloc.y += insets.top;
                alloc.width -= insets.left + insets.right;
                alloc.height -= insets.top + insets.bottom;
                return alloc;
            }
            return null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Rectangle getCharacterBounds(int i) {
            if (i < 0 || i > JTextComponent.this.model.getLength() - 1) {
                return null;
            }
            TextUI ui = JTextComponent.this.getUI();
            if (ui == null) {
                return null;
            }
            Rectangle rect = null;
            Rectangle alloc = this.getRootEditorRect();
            if (alloc == null) {
                return null;
            }
            if (JTextComponent.this.model instanceof AbstractDocument) {
                ((AbstractDocument)JTextComponent.this.model).readLock();
            }
            try {
                View rootView = ui.getRootView(JTextComponent.this);
                if (rootView != null) {
                    rootView.setSize(alloc.width, alloc.height);
                    Shape bounds = rootView.modelToView(i, Position.Bias.Forward, i + 1, Position.Bias.Backward, alloc);
                    rect = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
                }
            }
            catch (BadLocationException badLocationException) {
            }
            finally {
                if (JTextComponent.this.model instanceof AbstractDocument) {
                    ((AbstractDocument)JTextComponent.this.model).readUnlock();
                }
            }
            return rect;
        }

        @Override
        public int getCharCount() {
            return JTextComponent.this.model.getLength();
        }

        @Override
        public int getCaretPosition() {
            return JTextComponent.this.getCaretPosition();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public AttributeSet getCharacterAttribute(int i) {
            Element e = null;
            if (JTextComponent.this.model instanceof AbstractDocument) {
                ((AbstractDocument)JTextComponent.this.model).readLock();
            }
            try {
                e = JTextComponent.this.model.getDefaultRootElement();
                while (!e.isLeaf()) {
                    int index = e.getElementIndex(i);
                    e = e.getElement(index);
                }
            }
            finally {
                if (JTextComponent.this.model instanceof AbstractDocument) {
                    ((AbstractDocument)JTextComponent.this.model).readUnlock();
                }
            }
            return e.getAttributes();
        }

        @Override
        public int getSelectionStart() {
            return JTextComponent.this.getSelectionStart();
        }

        @Override
        public int getSelectionEnd() {
            return JTextComponent.this.getSelectionEnd();
        }

        @Override
        public String getSelectedText() {
            return JTextComponent.this.getSelectedText();
        }

        @Override
        public String getAtIndex(int part, int index) {
            return this.getAtIndex(part, index, 0);
        }

        @Override
        public String getAfterIndex(int part, int index) {
            return this.getAtIndex(part, index, 1);
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            return this.getAtIndex(part, index, -1);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        private String getAtIndex(int part, int index, int direction) {
            if (JTextComponent.this.model instanceof AbstractDocument) {
                ((AbstractDocument)JTextComponent.this.model).readLock();
            }
            try {
                if (index < 0 || index >= JTextComponent.this.model.getLength()) {
                    String string = null;
                    return string;
                }
                switch (part) {
                    case 1: {
                        if (index + direction >= JTextComponent.this.model.getLength()) return null;
                        if (index + direction < 0) return null;
                        String string = JTextComponent.this.model.getText(index + direction, 1);
                        return string;
                    }
                    case 2: 
                    case 3: {
                        IndexedSegment seg = this.getSegmentAt(part, index);
                        if (seg == null) return null;
                        if (direction != 0) {
                            int next = direction < 0 ? seg.modelOffset - 1 : seg.modelOffset + direction * seg.count;
                            seg = next >= 0 && next <= JTextComponent.this.model.getLength() ? this.getSegmentAt(part, next) : null;
                        }
                        if (seg == null) return null;
                        String string = new String(seg.array, seg.offset, seg.count);
                        return string;
                    }
                }
                return null;
            }
            catch (BadLocationException badLocationException) {
                return null;
            }
            finally {
                if (JTextComponent.this.model instanceof AbstractDocument) {
                    ((AbstractDocument)JTextComponent.this.model).readUnlock();
                }
            }
        }

        private Element getParagraphElement(int index) {
            if (JTextComponent.this.model instanceof PlainDocument) {
                PlainDocument sdoc = (PlainDocument)JTextComponent.this.model;
                return sdoc.getParagraphElement(index);
            }
            if (JTextComponent.this.model instanceof StyledDocument) {
                StyledDocument sdoc = (StyledDocument)JTextComponent.this.model;
                return sdoc.getParagraphElement(index);
            }
            Element para = JTextComponent.this.model.getDefaultRootElement();
            while (!para.isLeaf()) {
                int pos = para.getElementIndex(index);
                para = para.getElement(pos);
            }
            if (para == null) {
                return null;
            }
            return para.getParentElement();
        }

        private IndexedSegment getParagraphElementText(int index) throws BadLocationException {
            Element para = this.getParagraphElement(index);
            if (para != null) {
                IndexedSegment segment = new IndexedSegment(this);
                try {
                    int length = para.getEndOffset() - para.getStartOffset();
                    JTextComponent.this.model.getText(para.getStartOffset(), length, segment);
                }
                catch (BadLocationException e) {
                    return null;
                }
                segment.modelOffset = para.getStartOffset();
                return segment;
            }
            return null;
        }

        private IndexedSegment getSegmentAt(int part, int index) throws BadLocationException {
            BreakIterator iterator;
            IndexedSegment seg = this.getParagraphElementText(index);
            if (seg == null) {
                return null;
            }
            switch (part) {
                case 2: {
                    iterator = BreakIterator.getWordInstance(this.getLocale());
                    break;
                }
                case 3: {
                    iterator = BreakIterator.getSentenceInstance(this.getLocale());
                    break;
                }
                default: {
                    return null;
                }
            }
            seg.first();
            iterator.setText(seg);
            int end = iterator.following(index - seg.modelOffset + seg.offset);
            if (end == -1) {
                return null;
            }
            if (end > seg.offset + seg.count) {
                return null;
            }
            int begin = iterator.previous();
            if (begin == -1 || begin >= seg.offset + seg.count) {
                return null;
            }
            seg.modelOffset = seg.modelOffset + begin - seg.offset;
            seg.offset = begin;
            seg.count = end - begin;
            return seg;
        }

        @Override
        public AccessibleEditableText getAccessibleEditableText() {
            return this;
        }

        @Override
        public void setTextContents(String s) {
            JTextComponent.this.setText(s);
        }

        @Override
        public void insertTextAtIndex(int index, String s) {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null) {
                try {
                    if (s != null && s.length() > 0) {
                        boolean composedTextSaved = JTextComponent.this.saveComposedText(index);
                        doc.insertString(index, s, null);
                        if (composedTextSaved) {
                            JTextComponent.this.restoreComposedText();
                        }
                    }
                }
                catch (BadLocationException e) {
                    UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
                }
            }
        }

        @Override
        public String getTextRange(int startIndex, int endIndex) {
            int p1;
            String txt = null;
            int p0 = Math.min(startIndex, endIndex);
            if (p0 != (p1 = Math.max(startIndex, endIndex))) {
                try {
                    Document doc = JTextComponent.this.getDocument();
                    txt = doc.getText(p0, p1 - p0);
                }
                catch (BadLocationException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return txt;
        }

        @Override
        public void delete(int startIndex, int endIndex) {
            if (JTextComponent.this.isEditable() && this.isEnabled()) {
                try {
                    int p0 = Math.min(startIndex, endIndex);
                    int p1 = Math.max(startIndex, endIndex);
                    if (p0 != p1) {
                        Document doc = JTextComponent.this.getDocument();
                        doc.remove(p0, p1 - p0);
                    }
                }
                catch (BadLocationException badLocationException) {}
            } else {
                UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
            }
        }

        @Override
        public void cut(int startIndex, int endIndex) {
            this.selectText(startIndex, endIndex);
            JTextComponent.this.cut();
        }

        @Override
        public void paste(int startIndex) {
            JTextComponent.this.setCaretPosition(startIndex);
            JTextComponent.this.paste();
        }

        @Override
        public void replaceText(int startIndex, int endIndex, String s) {
            this.selectText(startIndex, endIndex);
            JTextComponent.this.replaceSelection(s);
        }

        @Override
        public void selectText(int startIndex, int endIndex) {
            JTextComponent.this.select(startIndex, endIndex);
        }

        @Override
        public void setAttributes(int startIndex, int endIndex, AttributeSet as) {
            Document doc = JTextComponent.this.getDocument();
            if (doc instanceof StyledDocument) {
                StyledDocument sDoc = (StyledDocument)doc;
                int offset = startIndex;
                int length = endIndex - startIndex;
                sDoc.setCharacterAttributes(offset, length, as, true);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private AccessibleTextSequence getSequenceAtIndex(int part, int index, int direction) {
            if (index < 0 || index >= JTextComponent.this.model.getLength()) {
                return null;
            }
            if (direction < -1 || direction > 1) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (JTextComponent.this.model instanceof AbstractDocument) {
                        ((AbstractDocument)JTextComponent.this.model).readLock();
                    }
                    AccessibleTextSequence charSequence = null;
                    try {
                        if (index + direction < JTextComponent.this.model.getLength() && index + direction >= 0) {
                            charSequence = new AccessibleTextSequence(index + direction, index + direction + 1, JTextComponent.this.model.getText(index + direction, 1));
                        }
                    }
                    catch (BadLocationException badLocationException) {
                    }
                    finally {
                        if (JTextComponent.this.model instanceof AbstractDocument) {
                            ((AbstractDocument)JTextComponent.this.model).readUnlock();
                        }
                    }
                    return charSequence;
                }
                case 2: 
                case 3: {
                    if (JTextComponent.this.model instanceof AbstractDocument) {
                        ((AbstractDocument)JTextComponent.this.model).readLock();
                    }
                    AccessibleTextSequence rangeSequence = null;
                    try {
                        IndexedSegment seg = this.getSegmentAt(part, index);
                        if (seg != null) {
                            if (direction != 0) {
                                int next = direction < 0 ? seg.modelOffset - 1 : seg.modelOffset + seg.count;
                                seg = next >= 0 && next <= JTextComponent.this.model.getLength() ? this.getSegmentAt(part, next) : null;
                            }
                            if (seg != null && seg.offset + seg.count <= JTextComponent.this.model.getLength()) {
                                rangeSequence = new AccessibleTextSequence(seg.offset, seg.offset + seg.count, new String(seg.array, seg.offset, seg.count));
                            }
                        }
                    }
                    catch (BadLocationException seg) {
                    }
                    finally {
                        if (JTextComponent.this.model instanceof AbstractDocument) {
                            ((AbstractDocument)JTextComponent.this.model).readUnlock();
                        }
                    }
                    return rangeSequence;
                }
                case 4: {
                    AccessibleTextSequence lineSequence = null;
                    if (JTextComponent.this.model instanceof AbstractDocument) {
                        ((AbstractDocument)JTextComponent.this.model).readLock();
                    }
                    try {
                        int startIndex = Utilities.getRowStart(JTextComponent.this, index);
                        int endIndex = Utilities.getRowEnd(JTextComponent.this, index);
                        if (startIndex >= 0 && endIndex >= startIndex) {
                            if (direction == 0) {
                                lineSequence = new AccessibleTextSequence(startIndex, endIndex, JTextComponent.this.model.getText(startIndex, endIndex - startIndex + 1));
                            } else if (direction == -1 && startIndex > 0) {
                                endIndex = Utilities.getRowEnd(JTextComponent.this, startIndex - 1);
                                if ((startIndex = Utilities.getRowStart(JTextComponent.this, startIndex - 1)) >= 0 && endIndex >= startIndex) {
                                    lineSequence = new AccessibleTextSequence(startIndex, endIndex, JTextComponent.this.model.getText(startIndex, endIndex - startIndex + 1));
                                }
                            } else if (direction == 1 && endIndex < JTextComponent.this.model.getLength()) {
                                startIndex = Utilities.getRowStart(JTextComponent.this, endIndex + 1);
                                endIndex = Utilities.getRowEnd(JTextComponent.this, endIndex + 1);
                                if (startIndex >= 0 && endIndex >= startIndex) {
                                    lineSequence = new AccessibleTextSequence(startIndex, endIndex, JTextComponent.this.model.getText(startIndex, endIndex - startIndex + 1));
                                }
                            }
                        }
                    }
                    catch (BadLocationException startIndex) {
                    }
                    finally {
                        if (JTextComponent.this.model instanceof AbstractDocument) {
                            ((AbstractDocument)JTextComponent.this.model).readUnlock();
                        }
                    }
                    return lineSequence;
                }
                case 5: {
                    int attributeRunStartIndex;
                    int attributeRunEndIndex;
                    String runText = null;
                    if (JTextComponent.this.model instanceof AbstractDocument) {
                        ((AbstractDocument)JTextComponent.this.model).readLock();
                    }
                    try {
                        attributeRunEndIndex = Integer.MIN_VALUE;
                        attributeRunStartIndex = Integer.MIN_VALUE;
                        int tempIndex = index;
                        switch (direction) {
                            case -1: {
                                attributeRunEndIndex = this.getRunEdge(index, direction);
                                tempIndex = attributeRunEndIndex - 1;
                                break;
                            }
                            case 1: {
                                tempIndex = attributeRunStartIndex = this.getRunEdge(index, direction);
                                break;
                            }
                            case 0: {
                                break;
                            }
                            default: {
                                throw new AssertionError(direction);
                            }
                        }
                        attributeRunStartIndex = attributeRunStartIndex != Integer.MIN_VALUE ? attributeRunStartIndex : this.getRunEdge(tempIndex, -1);
                        attributeRunEndIndex = attributeRunEndIndex != Integer.MIN_VALUE ? attributeRunEndIndex : this.getRunEdge(tempIndex, 1);
                        runText = JTextComponent.this.model.getText(attributeRunStartIndex, attributeRunEndIndex - attributeRunStartIndex);
                    }
                    catch (BadLocationException e) {
                        AccessibleTextSequence accessibleTextSequence = null;
                        return accessibleTextSequence;
                    }
                    finally {
                        if (JTextComponent.this.model instanceof AbstractDocument) {
                            ((AbstractDocument)JTextComponent.this.model).readUnlock();
                        }
                    }
                    return new AccessibleTextSequence(attributeRunStartIndex, attributeRunEndIndex, runText);
                }
            }
            return null;
        }

        private int getRunEdge(int index, int direction) throws BadLocationException {
            if (index < 0 || index >= JTextComponent.this.model.getLength()) {
                throw new BadLocationException("Location out of bounds", index);
            }
            int elementIndex = -1;
            Element indexElement = JTextComponent.this.model.getDefaultRootElement();
            while (!indexElement.isLeaf()) {
                elementIndex = indexElement.getElementIndex(index);
                indexElement = indexElement.getElement(elementIndex);
            }
            if (elementIndex == -1) {
                throw new AssertionError(index);
            }
            AttributeSet indexAS = indexElement.getAttributes();
            Element parent = indexElement.getParentElement();
            Element edgeElement = switch (direction) {
                case -1, 1 -> {
                    int edgeElementIndex = elementIndex;
                    int elementCount = parent.getElementCount();
                    while (edgeElementIndex + direction > 0 && edgeElementIndex + direction < elementCount && parent.getElement(edgeElementIndex + direction).getAttributes().isEqual(indexAS)) {
                        edgeElementIndex += direction;
                    }
                    yield parent.getElement(edgeElementIndex);
                }
                default -> throw new AssertionError(direction);
            };
            switch (direction) {
                case -1: {
                    return edgeElement.getStartOffset();
                }
                case 1: {
                    return edgeElement.getEndOffset();
                }
            }
            return Integer.MIN_VALUE;
        }

        @Override
        public AccessibleTextSequence getTextSequenceAt(int part, int index) {
            return this.getSequenceAtIndex(part, index, 0);
        }

        @Override
        public AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            return this.getSequenceAtIndex(part, index, 1);
        }

        @Override
        public AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            return this.getSequenceAtIndex(part, index, -1);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Rectangle getTextBounds(int startIndex, int endIndex) {
            if (startIndex < 0 || startIndex > JTextComponent.this.model.getLength() - 1 || endIndex < 0 || endIndex > JTextComponent.this.model.getLength() - 1 || startIndex > endIndex) {
                return null;
            }
            TextUI ui = JTextComponent.this.getUI();
            if (ui == null) {
                return null;
            }
            Rectangle rect = null;
            Rectangle alloc = this.getRootEditorRect();
            if (alloc == null) {
                return null;
            }
            if (JTextComponent.this.model instanceof AbstractDocument) {
                ((AbstractDocument)JTextComponent.this.model).readLock();
            }
            try {
                View rootView = ui.getRootView(JTextComponent.this);
                if (rootView != null) {
                    Shape bounds = rootView.modelToView(startIndex, Position.Bias.Forward, endIndex, Position.Bias.Backward, alloc);
                    rect = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
                }
            }
            catch (BadLocationException badLocationException) {
            }
            finally {
                if (JTextComponent.this.model instanceof AbstractDocument) {
                    ((AbstractDocument)JTextComponent.this.model).readUnlock();
                }
            }
            return rect;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            Action[] actions = JTextComponent.this.getActions();
            return actions.length;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            Action[] actions = JTextComponent.this.getActions();
            if (i < 0 || i >= actions.length) {
                return null;
            }
            return (String)actions[i].getValue("Name");
        }

        @Override
        public boolean doAccessibleAction(int i) {
            Action[] actions = JTextComponent.this.getActions();
            if (i < 0 || i >= actions.length) {
                return false;
            }
            ActionEvent ae = new ActionEvent(JTextComponent.this, 1001, null, EventQueue.getMostRecentEventTime(), JTextComponent.this.getCurrentEventModifiers());
            actions[i].actionPerformed(ae);
            return true;
        }

        private class IndexedSegment
        extends Segment {
            public int modelOffset;

            private IndexedSegment(AccessibleJTextComponent accessibleJTextComponent) {
            }
        }
    }

    public static final class DropLocation
    extends TransferHandler.DropLocation {
        private final int index;
        private final Position.Bias bias;

        private DropLocation(Point p, int index, Position.Bias bias) {
            super(p);
            this.index = index;
            this.bias = bias;
        }

        public int getIndex() {
            return this.index;
        }

        public Position.Bias getBias() {
            return this.bias;
        }

        @Override
        public String toString() {
            return this.getClass().getName() + "[dropPoint=" + String.valueOf(this.getDropPoint()) + ",index=" + this.index + ",bias=" + String.valueOf(this.bias) + "]";
        }
    }

    static class KeymapWrapper
    extends InputMap {
        static final Object DefaultActionKey = new Object();
        private Keymap keymap;

        KeymapWrapper(Keymap keymap) {
            this.keymap = keymap;
        }

        @Override
        public KeyStroke[] keys() {
            int keymapCount;
            KeyStroke[] sKeys = super.keys();
            KeyStroke[] keymapKeys = this.keymap.getBoundKeyStrokes();
            int sCount = sKeys == null ? 0 : sKeys.length;
            int n = keymapCount = keymapKeys == null ? 0 : keymapKeys.length;
            if (sCount == 0) {
                return keymapKeys;
            }
            if (keymapCount == 0) {
                return sKeys;
            }
            KeyStroke[] retValue = new KeyStroke[sCount + keymapCount];
            System.arraycopy(sKeys, 0, retValue, 0, sCount);
            System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);
            return retValue;
        }

        @Override
        public int size() {
            KeyStroke[] keymapStrokes = this.keymap.getBoundKeyStrokes();
            int keymapCount = keymapStrokes == null ? 0 : keymapStrokes.length;
            return super.size() + keymapCount;
        }

        @Override
        public Object get(KeyStroke keyStroke) {
            Object retValue = this.keymap.getAction(keyStroke);
            if (retValue == null && (retValue = super.get(keyStroke)) == null && keyStroke.getKeyChar() != '\uffff' && this.keymap.getDefaultAction() != null) {
                retValue = DefaultActionKey;
            }
            return retValue;
        }
    }

    static class KeymapActionMap
    extends ActionMap {
        private Keymap keymap;

        KeymapActionMap(Keymap keymap) {
            this.keymap = keymap;
        }

        @Override
        public Object[] keys() {
            boolean hasDefault;
            Object[] sKeys = super.keys();
            Object[] keymapKeys = this.keymap.getBoundActions();
            int sCount = sKeys == null ? 0 : sKeys.length;
            int keymapCount = keymapKeys == null ? 0 : keymapKeys.length;
            boolean bl = hasDefault = this.keymap.getDefaultAction() != null;
            if (hasDefault) {
                ++keymapCount;
            }
            if (sCount == 0) {
                if (hasDefault) {
                    Object[] retValue = new Object[keymapCount];
                    if (keymapCount > 1) {
                        System.arraycopy(keymapKeys, 0, retValue, 0, keymapCount - 1);
                    }
                    retValue[keymapCount - 1] = KeymapWrapper.DefaultActionKey;
                    return retValue;
                }
                return keymapKeys;
            }
            if (keymapCount == 0) {
                return sKeys;
            }
            Object[] retValue = new Object[sCount + keymapCount];
            System.arraycopy(sKeys, 0, retValue, 0, sCount);
            if (hasDefault) {
                if (keymapCount > 1) {
                    System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount - 1);
                }
                retValue[sCount + keymapCount - 1] = KeymapWrapper.DefaultActionKey;
            } else {
                System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);
            }
            return retValue;
        }

        @Override
        public int size() {
            int keymapCount;
            Action[] actions = this.keymap.getBoundActions();
            int n = keymapCount = actions == null ? 0 : actions.length;
            if (this.keymap.getDefaultAction() != null) {
                ++keymapCount;
            }
            return super.size() + keymapCount;
        }

        @Override
        public Action get(Object key) {
            Action retValue = super.get(key);
            if (retValue == null) {
                if (key == KeymapWrapper.DefaultActionKey) {
                    retValue = this.keymap.getDefaultAction();
                } else if (key instanceof Action) {
                    retValue = (Action)key;
                }
            }
            return retValue;
        }
    }

    static class DefaultKeymap
    implements Keymap {
        String nm;
        Keymap parent;
        Hashtable<KeyStroke, Action> bindings;
        Action defaultAction;

        DefaultKeymap(String nm, Keymap parent) {
            this.nm = nm;
            this.parent = parent;
            this.bindings = new Hashtable();
        }

        @Override
        public Action getDefaultAction() {
            if (this.defaultAction != null) {
                return this.defaultAction;
            }
            return this.parent != null ? this.parent.getDefaultAction() : null;
        }

        @Override
        public void setDefaultAction(Action a) {
            this.defaultAction = a;
        }

        @Override
        public String getName() {
            return this.nm;
        }

        @Override
        public Action getAction(KeyStroke key) {
            Action a = this.bindings.get(key);
            if (a == null && this.parent != null) {
                a = this.parent.getAction(key);
            }
            return a;
        }

        @Override
        public KeyStroke[] getBoundKeyStrokes() {
            KeyStroke[] keys = new KeyStroke[this.bindings.size()];
            int i = 0;
            Enumeration<KeyStroke> e = this.bindings.keys();
            while (e.hasMoreElements()) {
                keys[i++] = e.nextElement();
            }
            return keys;
        }

        @Override
        public Action[] getBoundActions() {
            Action[] actions = new Action[this.bindings.size()];
            int i = 0;
            Enumeration<Action> e = this.bindings.elements();
            while (e.hasMoreElements()) {
                actions[i++] = e.nextElement();
            }
            return actions;
        }

        @Override
        public KeyStroke[] getKeyStrokesForAction(Action a) {
            KeyStroke[] pStrokes;
            if (a == null) {
                return null;
            }
            KeyStroke[] retValue = null;
            ArrayList<KeyStroke> keyStrokes = null;
            Enumeration<KeyStroke> keys = this.bindings.keys();
            while (keys.hasMoreElements()) {
                KeyStroke key = keys.nextElement();
                if (this.bindings.get(key) != a) continue;
                if (keyStrokes == null) {
                    keyStrokes = new ArrayList<KeyStroke>();
                }
                keyStrokes.add(key);
            }
            if (this.parent != null && (pStrokes = this.parent.getKeyStrokesForAction(a)) != null) {
                int counter;
                int rCount = 0;
                for (counter = pStrokes.length - 1; counter >= 0; --counter) {
                    if (!this.isLocallyDefined(pStrokes[counter])) continue;
                    pStrokes[counter] = null;
                    ++rCount;
                }
                if (rCount > 0 && rCount < pStrokes.length) {
                    if (keyStrokes == null) {
                        keyStrokes = new ArrayList();
                    }
                    for (counter = pStrokes.length - 1; counter >= 0; --counter) {
                        if (pStrokes[counter] == null) continue;
                        keyStrokes.add(pStrokes[counter]);
                    }
                } else if (rCount == 0) {
                    if (keyStrokes == null) {
                        retValue = pStrokes;
                    } else {
                        retValue = new KeyStroke[keyStrokes.size() + pStrokes.length];
                        keyStrokes.toArray(retValue);
                        System.arraycopy(pStrokes, 0, retValue, keyStrokes.size(), pStrokes.length);
                        keyStrokes = null;
                    }
                }
            }
            if (keyStrokes != null) {
                retValue = keyStrokes.toArray(new KeyStroke[0]);
            }
            return retValue;
        }

        @Override
        public boolean isLocallyDefined(KeyStroke key) {
            return this.bindings.containsKey(key);
        }

        @Override
        public void addActionForKeyStroke(KeyStroke key, Action a) {
            this.bindings.put(key, a);
        }

        @Override
        public void removeKeyStrokeBinding(KeyStroke key) {
            this.bindings.remove(key);
        }

        @Override
        public void removeBindings() {
            this.bindings.clear();
        }

        @Override
        public Keymap getResolveParent() {
            return this.parent;
        }

        @Override
        public void setResolveParent(Keymap parent) {
            this.parent = parent;
        }

        public String toString() {
            return "Keymap[" + this.nm + "]" + String.valueOf(this.bindings);
        }
    }

    public static class KeyBinding {
        public KeyStroke key;
        public String actionName;

        public KeyBinding(KeyStroke key, String actionName) {
            this.key = key;
            this.actionName = actionName;
        }
    }

    static class DefaultTransferHandler
    extends TransferHandler
    implements UIResource {
        DefaultTransferHandler() {
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clipboard, int action) throws IllegalStateException {
            int p1;
            JTextComponent text;
            int p0;
            if (comp instanceof JTextComponent && (p0 = (text = (JTextComponent)comp).getSelectionStart()) != (p1 = text.getSelectionEnd())) {
                try {
                    Document doc = text.getDocument();
                    String srcData = doc.getText(p0, p1 - p0);
                    StringSelection contents = new StringSelection(srcData);
                    clipboard.setContents(contents, null);
                    if (action == 2) {
                        doc.remove(p0, p1 - p0);
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            DataFlavor flavor;
            if (comp instanceof JTextComponent && (flavor = this.getFlavor(t.getTransferDataFlavors())) != null) {
                InputContext ic = comp.getInputContext();
                if (ic != null) {
                    ic.endComposition();
                }
                try {
                    String data = (String)t.getTransferData(flavor);
                    ((JTextComponent)comp).replaceSelection(data);
                    return true;
                }
                catch (UnsupportedFlavorException | IOException exception) {
                    // empty catch block
                }
            }
            return false;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            JTextComponent c = (JTextComponent)comp;
            if (!c.isEditable() || !c.isEnabled()) {
                return false;
            }
            return this.getFlavor(transferFlavors) != null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return 0;
        }

        private DataFlavor getFlavor(DataFlavor[] flavors) {
            if (flavors != null) {
                for (DataFlavor flavor : flavors) {
                    if (!flavor.equals(DataFlavor.stringFlavor)) continue;
                    return flavor;
                }
            }
            return null;
        }
    }

    class ComposedTextCaret
    extends DefaultCaret
    implements Serializable {
        Color bg;

        ComposedTextCaret() {
        }

        @Override
        public void install(JTextComponent c) {
            super.install(c);
            Document doc = c.getDocument();
            if (doc instanceof StyledDocument) {
                StyledDocument sDoc = (StyledDocument)doc;
                Element elem = sDoc.getCharacterElement(c.composedTextStart.getOffset());
                AttributeSet attr = elem.getAttributes();
                this.bg = sDoc.getBackground(attr);
            }
            if (this.bg == null) {
                this.bg = c.getBackground();
            }
        }

        @Override
        public void paint(Graphics g) {
            if (this.isVisible()) {
                try {
                    Rectangle r = this.component.modelToView(this.getDot());
                    g.setXORMode(this.bg);
                    g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                    g.setPaintMode();
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }

        @Override
        protected void positionCaret(MouseEvent me) {
            int composedStartIndex;
            JTextComponent host = this.component;
            Point pt = new Point(me.getX(), me.getY());
            int offset = host.viewToModel(pt);
            if (offset < (composedStartIndex = host.composedTextStart.getOffset()) || offset > JTextComponent.this.composedTextEnd.getOffset()) {
                try {
                    Position newPos = host.getDocument().createPosition(offset);
                    host.getInputContext().endComposition();
                    EventQueue.invokeLater(new DoSetCaretPosition(host, newPos));
                }
                catch (BadLocationException ble) {
                    System.err.println(ble);
                }
            } else {
                super.positionCaret(me);
            }
        }
    }

    class InputMethodRequestsHandler
    implements InputMethodRequests,
    DocumentListener {
        InputMethodRequestsHandler() {
        }

        @Override
        public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null && JTextComponent.this.latestCommittedTextStart != null && !JTextComponent.this.latestCommittedTextStart.equals(JTextComponent.this.latestCommittedTextEnd)) {
                try {
                    int startIndex = JTextComponent.this.latestCommittedTextStart.getOffset();
                    int endIndex = JTextComponent.this.latestCommittedTextEnd.getOffset();
                    String latestCommittedText = doc.getText(startIndex, endIndex - startIndex);
                    doc.remove(startIndex, endIndex - startIndex);
                    return new AttributedString(latestCommittedText).getIterator();
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            return null;
        }

        @Override
        public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
            Object committed;
            int composedStartIndex = 0;
            int composedEndIndex = 0;
            if (JTextComponent.this.composedTextExists()) {
                composedStartIndex = JTextComponent.this.composedTextStart.getOffset();
                composedEndIndex = JTextComponent.this.composedTextEnd.getOffset();
            }
            try {
                if (beginIndex < composedStartIndex) {
                    if (endIndex <= composedStartIndex) {
                        committed = JTextComponent.this.getText(beginIndex, endIndex - beginIndex);
                    } else {
                        int firstPartLength = composedStartIndex - beginIndex;
                        committed = JTextComponent.this.getText(beginIndex, firstPartLength) + JTextComponent.this.getText(composedEndIndex, endIndex - beginIndex - firstPartLength);
                    }
                } else {
                    committed = JTextComponent.this.getText(beginIndex + (composedEndIndex - composedStartIndex), endIndex - beginIndex);
                }
            }
            catch (BadLocationException ble) {
                throw new IllegalArgumentException("Invalid range");
            }
            return new AttributedString((String)committed).getIterator();
        }

        @Override
        public int getCommittedTextLength() {
            Document doc = JTextComponent.this.getDocument();
            int length = 0;
            if (doc != null) {
                length = doc.getLength();
                if (JTextComponent.this.composedTextContent != null) {
                    length = JTextComponent.this.composedTextEnd == null || JTextComponent.this.composedTextStart == null ? (length -= JTextComponent.this.composedTextContent.length()) : (length -= JTextComponent.this.composedTextEnd.getOffset() - JTextComponent.this.composedTextStart.getOffset());
                }
            }
            return length;
        }

        @Override
        public int getInsertPositionOffset() {
            int caretIndex;
            int composedStartIndex = 0;
            int composedEndIndex = 0;
            if (JTextComponent.this.composedTextExists()) {
                composedStartIndex = JTextComponent.this.composedTextStart.getOffset();
                composedEndIndex = JTextComponent.this.composedTextEnd.getOffset();
            }
            if ((caretIndex = JTextComponent.this.getCaretPosition()) < composedStartIndex) {
                return caretIndex;
            }
            if (caretIndex < composedEndIndex) {
                return composedStartIndex;
            }
            return caretIndex - (composedEndIndex - composedStartIndex);
        }

        @Override
        public TextHitInfo getLocationOffset(int x, int y) {
            if (JTextComponent.this.composedTextAttribute == null) {
                return null;
            }
            Point p = JTextComponent.this.getLocationOnScreen();
            p.x = x - p.x;
            p.y = y - p.y;
            int pos = JTextComponent.this.viewToModel(p);
            if (pos >= JTextComponent.this.composedTextStart.getOffset() && pos <= JTextComponent.this.composedTextEnd.getOffset()) {
                return TextHitInfo.leading(pos - JTextComponent.this.composedTextStart.getOffset());
            }
            return null;
        }

        @Override
        public Rectangle getTextLocation(TextHitInfo offset) {
            Rectangle r;
            try {
                r = JTextComponent.this.modelToView(JTextComponent.this.getCaretPosition());
                if (r != null) {
                    Point p = JTextComponent.this.getLocationOnScreen();
                    r.translate(p.x, p.y);
                }
            }
            catch (BadLocationException ble) {
                r = null;
            }
            if (r == null) {
                r = new Rectangle();
            }
            return r;
        }

        @Override
        public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
            String selection = JTextComponent.this.getSelectedText();
            if (selection != null) {
                return new AttributedString(selection).getIterator();
            }
            return null;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            JTextComponent.this.latestCommittedTextEnd = null;
            JTextComponent.this.latestCommittedTextStart = null;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            JTextComponent.this.latestCommittedTextEnd = null;
            JTextComponent.this.latestCommittedTextStart = null;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            JTextComponent.this.latestCommittedTextEnd = null;
            JTextComponent.this.latestCommittedTextStart = null;
        }
    }

    private static class DoSetCaretPosition
    implements Runnable {
        JTextComponent host;
        Position newPos;

        DoSetCaretPosition(JTextComponent host, Position newPos) {
            this.host = host;
            this.newPos = newPos;
        }

        @Override
        public void run() {
            this.host.setCaretPosition(this.newPos.getOffset());
        }
    }
}

