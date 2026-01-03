/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StateInvariantError;
import sun.swing.SwingUtilities2;

public class DefaultCaret
extends Rectangle
implements Caret,
FocusListener,
MouseListener,
MouseMotionListener {
    public static final int UPDATE_WHEN_ON_EDT = 0;
    public static final int NEVER_UPDATE = 1;
    public static final int ALWAYS_UPDATE = 2;
    private int savedBlinkRate = 0;
    private boolean isBlinkRateSaved = false;
    protected EventListenerList listenerList = new EventListenerList();
    protected transient ChangeEvent changeEvent = null;
    JTextComponent component;
    int updatePolicy = 0;
    boolean visible;
    boolean active;
    int dot;
    int mark;
    Object selectionTag;
    boolean selectionVisible;
    Timer flasher;
    Point magicCaretPosition;
    transient Position.Bias dotBias;
    transient Position.Bias markBias;
    boolean dotLTR;
    boolean markLTR;
    transient Handler handler = new Handler();
    private transient int[] flagXPoints = new int[3];
    private transient int[] flagYPoints = new int[3];
    private transient NavigationFilter.FilterBypass filterBypass;
    private static transient Action selectWord = null;
    private static transient Action selectLine = null;
    private boolean ownsSelection;
    private boolean forceCaretPositionChange;
    private transient boolean shouldHandleRelease;
    private transient MouseEvent selectedWordEvent = null;
    private int caretWidth = -1;
    private float aspectRatio = -1.0f;

    public void setUpdatePolicy(int policy) {
        this.updatePolicy = policy;
    }

    public int getUpdatePolicy() {
        return this.updatePolicy;
    }

    protected final JTextComponent getComponent() {
        return this.component;
    }

    protected final synchronized void repaint() {
        if (this.component != null) {
            this.component.repaint(this.x, this.y, this.width, this.height);
        }
    }

    protected synchronized void damage(Rectangle r) {
        if (r != null) {
            int damageWidth = this.getCaretWidth(r.height);
            this.x = r.x - 4 - (damageWidth >> 1);
            this.y = r.y;
            this.width = 9 + damageWidth;
            this.height = r.height;
            this.repaint();
        }
    }

    protected void adjustVisibility(Rectangle nloc) {
        if (this.component == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            this.component.scrollRectToVisible(nloc);
        } else {
            SwingUtilities.invokeLater(new SafeScroller(nloc));
        }
    }

    protected Highlighter.HighlightPainter getSelectionPainter() {
        return DefaultHighlighter.DefaultPainter;
    }

    protected void positionCaret(MouseEvent e) {
        Point pt = new Point(e.getX(), e.getY());
        Position.Bias[] biasRet = new Position.Bias[1];
        int pos = this.component.getUI().viewToModel(this.component, pt, biasRet);
        if (biasRet[0] == null) {
            biasRet[0] = Position.Bias.Forward;
        }
        if (pos >= 0) {
            this.setDot(pos, biasRet[0]);
        }
    }

    protected void moveCaret(MouseEvent e) {
        Point pt = new Point(e.getX(), e.getY());
        Position.Bias[] biasRet = new Position.Bias[1];
        int pos = this.component.getUI().viewToModel(this.component, pt, biasRet);
        if (biasRet[0] == null) {
            biasRet[0] = Position.Bias.Forward;
        }
        if (pos >= 0) {
            this.moveDot(pos, biasRet[0]);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.component.isEnabled()) {
            if (this.component.isEditable()) {
                if (this.isBlinkRateSaved) {
                    this.setBlinkRate(this.savedBlinkRate);
                    this.savedBlinkRate = 0;
                    this.isBlinkRateSaved = false;
                }
            } else if (this.getBlinkRate() != 0) {
                if (!this.isBlinkRateSaved) {
                    this.savedBlinkRate = this.getBlinkRate();
                    this.isBlinkRateSaved = true;
                }
                this.setBlinkRate(0);
            }
            this.setVisible(true);
            this.setSelectionVisible(true);
            this.updateSystemSelection();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        this.setVisible(false);
        this.setSelectionVisible(!(e.getCause() != FocusEvent.Cause.ACTIVATION && !(e.getOppositeComponent() instanceof JRootPane) || !this.ownsSelection && !e.isTemporary()));
    }

    private void selectWord(MouseEvent e) {
        if (this.selectedWordEvent != null && this.selectedWordEvent.getX() == e.getX() && this.selectedWordEvent.getY() == e.getY()) {
            return;
        }
        Action a = null;
        ActionMap map = this.getComponent().getActionMap();
        if (map != null) {
            a = map.get("select-word");
        }
        if (a == null) {
            if (selectWord == null) {
                selectWord = new DefaultEditorKit.SelectWordAction();
            }
            a = selectWord;
        }
        a.actionPerformed(new ActionEvent(this.getComponent(), 1001, null, e.getWhen(), e.getModifiers()));
        this.selectedWordEvent = e;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        block19: {
            if (this.getComponent() == null) {
                return;
            }
            int nclicks = SwingUtilities2.getAdjustedClickCount(this.getComponent(), e);
            if (!e.isConsumed()) {
                JTextComponent c;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (nclicks == 1) {
                        this.selectedWordEvent = null;
                    } else if (nclicks == 2 && SwingUtilities2.canEventAccessSystemClipboard(e)) {
                        this.selectWord(e);
                        this.selectedWordEvent = null;
                    } else if (nclicks == 3 && SwingUtilities2.canEventAccessSystemClipboard(e)) {
                        Action a = null;
                        ActionMap map = this.getComponent().getActionMap();
                        if (map != null) {
                            a = map.get("select-line");
                        }
                        if (a == null) {
                            if (selectLine == null) {
                                selectLine = new DefaultEditorKit.SelectLineAction();
                            }
                            a = selectLine;
                        }
                        a.actionPerformed(new ActionEvent(this.getComponent(), 1001, null, e.getWhen(), e.getModifiers()));
                    }
                } else if (SwingUtilities.isMiddleMouseButton(e) && nclicks == 1 && this.component.isEditable() && this.component.isEnabled() && SwingUtilities2.canEventAccessSystemClipboard(e) && (c = (JTextComponent)e.getSource()) != null) {
                    try {
                        Toolkit tk = c.getToolkit();
                        Clipboard buffer = tk.getSystemSelection();
                        if (buffer == null) break block19;
                        this.adjustCaret(e);
                        TransferHandler th = c.getTransferHandler();
                        if (th != null) {
                            Transferable trans = null;
                            try {
                                trans = buffer.getContents(null);
                            }
                            catch (IllegalStateException ise) {
                                UIManager.getLookAndFeel().provideErrorFeedback(c);
                            }
                            if (trans != null) {
                                th.importData(c, trans);
                            }
                        }
                        this.adjustFocus(true);
                    }
                    catch (HeadlessException headlessException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int nclicks = SwingUtilities2.getAdjustedClickCount(this.getComponent(), e);
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isConsumed()) {
                this.shouldHandleRelease = true;
            } else {
                this.shouldHandleRelease = false;
                this.adjustCaretAndFocus(e);
                if (nclicks == 2 && SwingUtilities2.canEventAccessSystemClipboard(e)) {
                    this.selectWord(e);
                }
            }
        }
    }

    void adjustCaretAndFocus(MouseEvent e) {
        this.adjustCaret(e);
        this.adjustFocus(false);
    }

    private void adjustCaret(MouseEvent e) {
        if ((e.getModifiers() & 1) != 0 && this.getDot() != -1) {
            this.moveCaret(e);
        } else if (!e.isPopupTrigger()) {
            this.positionCaret(e);
        }
    }

    private void adjustFocus(boolean inWindow) {
        if (this.component != null && this.component.isEnabled() && this.component.isRequestFocusEnabled()) {
            if (inWindow) {
                this.component.requestFocusInWindow(FocusEvent.Cause.MOUSE_EVENT);
            } else {
                this.component.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!e.isConsumed() && this.shouldHandleRelease && SwingUtilities.isLeftMouseButton(e)) {
            this.adjustCaretAndFocus(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!e.isConsumed() && SwingUtilities.isLeftMouseButton(e)) {
            this.moveCaret(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void paint(Graphics g) {
        if (this.isVisible()) {
            try {
                Element bidi;
                TextUI mapper = this.component.getUI();
                Rectangle r = mapper.modelToView(this.component, this.dot, this.dotBias);
                if (r == null || r.width == 0 && r.height == 0) {
                    return;
                }
                if (this.width > 0 && this.height > 0 && !this._contains(r.x, r.y, r.width, r.height)) {
                    Rectangle clip = g.getClipBounds();
                    if (clip != null && !clip.contains(this)) {
                        this.repaint();
                    }
                    this.damage(r);
                }
                g.setColor(this.component.getCaretColor());
                int paintWidth = this.getCaretWidth(r.height);
                r.x -= paintWidth >> 1;
                g.fillRect(r.x, r.y, paintWidth, r.height);
                Document doc = this.component.getDocument();
                if (doc instanceof AbstractDocument && (bidi = ((AbstractDocument)doc).getBidiRootElement()) != null && bidi.getElementCount() > 1) {
                    this.flagXPoints[0] = r.x + (this.dotLTR ? paintWidth : 0);
                    this.flagYPoints[0] = r.y;
                    this.flagXPoints[1] = this.flagXPoints[0];
                    this.flagYPoints[1] = this.flagYPoints[0] + 4;
                    this.flagXPoints[2] = this.flagXPoints[0] + (this.dotLTR ? 4 : -4);
                    this.flagYPoints[2] = this.flagYPoints[0];
                    g.fillPolygon(this.flagXPoints, this.flagYPoints, 3);
                }
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }
    }

    @Override
    public void install(JTextComponent c) {
        Number ratio;
        this.component = c;
        Document doc = c.getDocument();
        this.mark = 0;
        this.dot = 0;
        this.markLTR = true;
        this.dotLTR = true;
        this.dotBias = this.markBias = Position.Bias.Forward;
        if (doc != null) {
            doc.addDocumentListener(this.handler);
        }
        c.addPropertyChangeListener(this.handler);
        c.addFocusListener(this);
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
        if (this.component.hasFocus()) {
            this.focusGained(null);
        }
        this.aspectRatio = (ratio = (Number)c.getClientProperty("caretAspectRatio")) != null ? ratio.floatValue() : -1.0f;
        Integer width = (Integer)c.getClientProperty("caretWidth");
        this.caretWidth = width != null ? width : -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deinstall(JTextComponent c) {
        c.removeMouseListener(this);
        c.removeMouseMotionListener(this);
        c.removeFocusListener(this);
        c.removePropertyChangeListener(this.handler);
        Document doc = c.getDocument();
        if (doc != null) {
            doc.removeDocumentListener(this.handler);
        }
        DefaultCaret defaultCaret = this;
        synchronized (defaultCaret) {
            this.component = null;
        }
        if (this.flasher != null) {
            this.flasher.stop();
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }

    @Override
    public void setSelectionVisible(boolean vis) {
        if (vis != this.selectionVisible) {
            this.selectionVisible = vis;
            if (this.selectionVisible) {
                Highlighter h = this.component.getHighlighter();
                if (this.dot != this.mark && h != null && this.selectionTag == null) {
                    int p0 = Math.min(this.dot, this.mark);
                    int p1 = Math.max(this.dot, this.mark);
                    Highlighter.HighlightPainter p = this.getSelectionPainter();
                    try {
                        this.selectionTag = h.addHighlight(p0, p1, p);
                    }
                    catch (BadLocationException bl) {
                        this.selectionTag = null;
                    }
                }
            } else if (this.selectionTag != null) {
                Highlighter h = this.component.getHighlighter();
                h.removeHighlight(this.selectionTag);
                this.selectionTag = null;
            }
        }
    }

    @Override
    public boolean isSelectionVisible() {
        return this.selectionVisible;
    }

    public boolean isActive() {
        return this.active;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean e) {
        this.active = e;
        if (this.component != null) {
            TextUI mapper = this.component.getUI();
            if (this.visible != e) {
                this.visible = e;
                try {
                    Rectangle loc = mapper.modelToView(this.component, this.dot, this.dotBias);
                    this.damage(loc);
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }
        if (this.flasher != null) {
            if (this.visible) {
                this.flasher.start();
            } else {
                this.flasher.stop();
            }
        }
    }

    @Override
    public void setBlinkRate(int rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Invalid blink rate: " + rate);
        }
        if (rate != 0) {
            if (this.component != null && this.component.isEditable()) {
                if (this.flasher == null) {
                    this.flasher = new Timer(rate, this.handler);
                }
                this.flasher.setDelay(rate);
            } else {
                this.savedBlinkRate = rate;
                this.isBlinkRateSaved = true;
            }
        } else {
            if (this.flasher != null) {
                this.flasher.stop();
                this.flasher.removeActionListener(this.handler);
                this.flasher = null;
            }
            if ((this.component == null || this.component.isEditable()) && this.isBlinkRateSaved) {
                this.savedBlinkRate = 0;
                this.isBlinkRateSaved = false;
            }
        }
    }

    @Override
    public int getBlinkRate() {
        if (this.isBlinkRateSaved) {
            return this.savedBlinkRate;
        }
        return this.flasher == null ? 0 : this.flasher.getDelay();
    }

    @Override
    public int getDot() {
        return this.dot;
    }

    @Override
    public int getMark() {
        return this.mark;
    }

    @Override
    public void setDot(int dot) {
        this.setDot(dot, Position.Bias.Forward);
    }

    @Override
    public void moveDot(int dot) {
        this.moveDot(dot, Position.Bias.Forward);
    }

    public void moveDot(int dot, Position.Bias dotBias) {
        if (dotBias == null) {
            throw new IllegalArgumentException("null bias");
        }
        if (!this.component.isEnabled()) {
            this.setDot(dot, dotBias);
            return;
        }
        if (dot != this.dot) {
            NavigationFilter filter = this.component.getNavigationFilter();
            if (filter != null) {
                filter.moveDot(this.getFilterBypass(), dot, dotBias);
            } else {
                this.handleMoveDot(dot, dotBias);
            }
        }
    }

    void handleMoveDot(int dot, Position.Bias dotBias) {
        Highlighter h;
        this.changeCaretPosition(dot, dotBias);
        if (this.selectionVisible && (h = this.component.getHighlighter()) != null) {
            int p1;
            int p0 = Math.min(dot, this.mark);
            if (p0 == (p1 = Math.max(dot, this.mark))) {
                if (this.selectionTag != null) {
                    h.removeHighlight(this.selectionTag);
                    this.selectionTag = null;
                }
            } else {
                try {
                    if (this.selectionTag != null) {
                        h.changeHighlight(this.selectionTag, p0, p1);
                    } else {
                        Highlighter.HighlightPainter p = this.getSelectionPainter();
                        this.selectionTag = h.addHighlight(p0, p1, p);
                    }
                }
                catch (BadLocationException e) {
                    throw new StateInvariantError("Bad caret position");
                }
            }
        }
    }

    public void setDot(int dot, Position.Bias dotBias) {
        if (dotBias == null) {
            throw new IllegalArgumentException("null bias");
        }
        NavigationFilter filter = this.component.getNavigationFilter();
        if (filter != null) {
            filter.setDot(this.getFilterBypass(), dot, dotBias);
        } else {
            this.handleSetDot(dot, dotBias);
        }
    }

    void handleSetDot(int dot, Position.Bias dotBias) {
        Document doc = this.component.getDocument();
        if (doc != null) {
            dot = Math.min(dot, doc.getLength());
        }
        if ((dot = Math.max(dot, 0)) == 0) {
            dotBias = Position.Bias.Forward;
        }
        this.mark = dot;
        if (this.dot != dot || this.dotBias != dotBias || this.selectionTag != null || this.forceCaretPositionChange) {
            this.changeCaretPosition(dot, dotBias);
        }
        this.markBias = this.dotBias;
        this.markLTR = this.dotLTR;
        Highlighter h = this.component.getHighlighter();
        if (h != null && this.selectionTag != null) {
            h.removeHighlight(this.selectionTag);
            this.selectionTag = null;
        }
    }

    public Position.Bias getDotBias() {
        return this.dotBias;
    }

    public Position.Bias getMarkBias() {
        return this.markBias;
    }

    boolean isDotLeftToRight() {
        return this.dotLTR;
    }

    boolean isMarkLeftToRight() {
        return this.markLTR;
    }

    boolean isPositionLTR(int position, Position.Bias bias) {
        Document doc = this.component.getDocument();
        if (bias == Position.Bias.Backward && --position < 0) {
            position = 0;
        }
        return AbstractDocument.isLeftToRight(doc, position, position);
    }

    Position.Bias guessBiasForOffset(int offset, Position.Bias lastBias, boolean lastLTR) {
        if (lastLTR != this.isPositionLTR(offset, lastBias)) {
            lastBias = Position.Bias.Backward;
        } else if (lastBias != Position.Bias.Backward && lastLTR != this.isPositionLTR(offset, Position.Bias.Backward)) {
            lastBias = Position.Bias.Backward;
        }
        if (lastBias == Position.Bias.Backward && offset > 0) {
            try {
                Segment s = new Segment();
                this.component.getDocument().getText(offset - 1, 1, s);
                if (s.count > 0 && s.array[s.offset] == '\n') {
                    lastBias = Position.Bias.Forward;
                }
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }
        return lastBias;
    }

    void changeCaretPosition(int dot, Position.Bias dotBias) {
        this.repaint();
        if (this.flasher != null && this.flasher.isRunning()) {
            this.visible = true;
            this.flasher.restart();
        }
        this.dot = dot;
        this.dotBias = dotBias;
        this.dotLTR = this.isPositionLTR(dot, dotBias);
        this.fireStateChanged();
        this.updateSystemSelection();
        this.setMagicCaretPosition(null);
        Runnable callRepaintNewCaret = new Runnable(){

            @Override
            public void run() {
                DefaultCaret.this.repaintNewCaret();
            }
        };
        SwingUtilities.invokeLater(callRepaintNewCaret);
    }

    void repaintNewCaret() {
        if (this.component != null) {
            TextUI mapper = this.component.getUI();
            Document doc = this.component.getDocument();
            if (mapper != null && doc != null) {
                Rectangle newLoc;
                try {
                    newLoc = mapper.modelToView(this.component, this.dot, this.dotBias);
                }
                catch (BadLocationException e) {
                    newLoc = null;
                }
                if (newLoc != null) {
                    this.adjustVisibility(newLoc);
                    if (this.getMagicCaretPosition() == null) {
                        this.setMagicCaretPosition(new Point(newLoc.x, newLoc.y));
                    }
                }
                this.damage(newLoc);
            }
        }
    }

    private void updateSystemSelection() {
        Clipboard clip;
        if (!SwingUtilities2.canCurrentEventAccessSystemClipboard()) {
            return;
        }
        if (this.dot != this.mark && this.component != null && this.component.hasFocus() && (clip = this.getSystemSelection()) != null) {
            String selectedText;
            if (this.component instanceof JPasswordField && this.component.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
                StringBuilder txt = null;
                char echoChar = ((JPasswordField)this.component).getEchoChar();
                int p0 = Math.min(this.getDot(), this.getMark());
                int p1 = Math.max(this.getDot(), this.getMark());
                for (int i = p0; i < p1; ++i) {
                    if (txt == null) {
                        txt = new StringBuilder();
                    }
                    txt.append(echoChar);
                }
                selectedText = txt != null ? txt.toString() : null;
            } else {
                selectedText = this.component.getSelectedText();
            }
            try {
                clip.setContents(new StringSelection(selectedText), this.getClipboardOwner());
                this.ownsSelection = true;
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    private Clipboard getSystemSelection() {
        try {
            return this.component.getToolkit().getSystemSelection();
        }
        catch (HeadlessException headlessException) {
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        return null;
    }

    private ClipboardOwner getClipboardOwner() {
        return this.handler;
    }

    private void ensureValidPosition() {
        int length = this.component.getDocument().getLength();
        if (this.dot > length || this.mark > length) {
            this.handleSetDot(length, Position.Bias.Forward);
        }
    }

    @Override
    public void setMagicCaretPosition(Point p) {
        this.magicCaretPosition = p;
    }

    @Override
    public Point getMagicCaretPosition() {
        return this.magicCaretPosition;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        String s = "Dot=(" + this.dot + ", " + String.valueOf(this.dotBias) + ")";
        s = s + " Mark=(" + this.mark + ", " + String.valueOf(this.markBias) + ")";
        return s;
    }

    private NavigationFilter.FilterBypass getFilterBypass() {
        if (this.filterBypass == null) {
            this.filterBypass = new DefaultFilterBypass();
        }
        return this.filterBypass;
    }

    private boolean _contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            return false;
        }
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        if (W > 0) {
            w += x;
            if ((W += X) <= X ? w >= x || W > w : w >= x && W > w) {
                return false;
            }
        } else if (x + w < X) {
            return false;
        }
        if (H > 0) {
            h += y;
            if ((H += Y) <= Y ? h >= y || H > h : h >= y && H > h) {
                return false;
            }
        } else if (y + h < Y) {
            return false;
        }
        return true;
    }

    int getCaretWidth(int height) {
        if (this.aspectRatio > -1.0f) {
            return (int)(this.aspectRatio * (float)height) + 1;
        }
        if (this.caretWidth > -1) {
            return this.caretWidth;
        }
        Object property = UIManager.get("Caret.width");
        if (property instanceof Integer) {
            return (Integer)property;
        }
        return 1;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        EventListenerList newListenerList = (EventListenerList)f.get("listenerList", null);
        if (newListenerList == null) {
            throw new InvalidObjectException("Null listenerList");
        }
        this.listenerList = newListenerList;
        this.component = (JTextComponent)f.get("component", null);
        this.updatePolicy = f.get("updatePolicy", 0);
        this.visible = f.get("visible", false);
        this.active = f.get("active", false);
        this.dot = f.get("dot", 0);
        this.mark = f.get("mark", 0);
        this.selectionTag = f.get("selectionTag", null);
        this.selectionVisible = f.get("selectionVisible", false);
        this.flasher = (Timer)f.get("flasher", null);
        this.magicCaretPosition = (Point)f.get("magicCaretPosition", null);
        this.dotLTR = f.get("dotLTR", false);
        this.markLTR = f.get("markLTR", false);
        this.ownsSelection = f.get("ownsSelection", false);
        this.forceCaretPositionChange = f.get("forceCaretPositionChange", false);
        this.caretWidth = f.get("caretWidth", 0);
        this.aspectRatio = f.get("aspectRatio", 0.0f);
        this.handler = new Handler();
        this.dotBias = !s.readBoolean() ? Position.Bias.Forward : Position.Bias.Backward;
        this.markBias = !s.readBoolean() ? Position.Bias.Forward : Position.Bias.Backward;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeBoolean(this.dotBias == Position.Bias.Backward);
        s.writeBoolean(this.markBias == Position.Bias.Backward);
    }

    class Handler
    implements PropertyChangeListener,
    DocumentListener,
    ActionListener,
    ClipboardOwner {
        Handler() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ((DefaultCaret.this.width == 0 || DefaultCaret.this.height == 0) && DefaultCaret.this.component != null) {
                TextUI mapper = DefaultCaret.this.component.getUI();
                try {
                    Rectangle r = mapper.modelToView(DefaultCaret.this.component, DefaultCaret.this.dot, DefaultCaret.this.dotBias);
                    if (r != null && r.width != 0 && r.height != 0) {
                        DefaultCaret.this.damage(r);
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            DefaultCaret.this.visible = !DefaultCaret.this.visible;
            DefaultCaret.this.repaint();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            int newMark;
            if (DefaultCaret.this.getUpdatePolicy() == 1 || DefaultCaret.this.getUpdatePolicy() == 0 && !SwingUtilities.isEventDispatchThread()) {
                if ((e.getOffset() <= DefaultCaret.this.dot || e.getOffset() <= DefaultCaret.this.mark) && DefaultCaret.this.selectionTag != null) {
                    try {
                        DefaultCaret.this.component.getHighlighter().changeHighlight(DefaultCaret.this.selectionTag, Math.min(DefaultCaret.this.dot, DefaultCaret.this.mark), Math.max(DefaultCaret.this.dot, DefaultCaret.this.mark));
                    }
                    catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
                return;
            }
            int offset = e.getOffset();
            int length = e.getLength();
            int newDot = DefaultCaret.this.dot;
            short changed = 0;
            if (e instanceof AbstractDocument.UndoRedoDocumentEvent) {
                DefaultCaret.this.setDot(offset + length);
                return;
            }
            if (newDot >= offset) {
                newDot += length;
                changed = (short)(changed | 1);
            }
            if ((newMark = DefaultCaret.this.mark) >= offset) {
                newMark += length;
                changed = (short)(changed | 2);
            }
            if (changed != 0) {
                Position.Bias dotBias = DefaultCaret.this.dotBias;
                if (DefaultCaret.this.dot == offset) {
                    boolean isNewline;
                    Document doc = DefaultCaret.this.component.getDocument();
                    try {
                        Segment s = new Segment();
                        doc.getText(newDot - 1, 1, s);
                        isNewline = s.count > 0 && s.array[s.offset] == '\n';
                    }
                    catch (BadLocationException ble) {
                        isNewline = false;
                    }
                    dotBias = isNewline ? Position.Bias.Forward : Position.Bias.Backward;
                }
                if (newMark == newDot) {
                    DefaultCaret.this.setDot(newDot, dotBias);
                    DefaultCaret.this.ensureValidPosition();
                } else {
                    DefaultCaret.this.setDot(newMark, DefaultCaret.this.markBias);
                    if (DefaultCaret.this.getDot() == newMark) {
                        DefaultCaret.this.moveDot(newDot, dotBias);
                    }
                    DefaultCaret.this.ensureValidPosition();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            if (DefaultCaret.this.getUpdatePolicy() == 1 || DefaultCaret.this.getUpdatePolicy() == 0 && !SwingUtilities.isEventDispatchThread()) {
                int length = DefaultCaret.this.component.getDocument().getLength();
                DefaultCaret.this.dot = Math.min(DefaultCaret.this.dot, length);
                DefaultCaret.this.mark = Math.min(DefaultCaret.this.mark, length);
                if ((e.getOffset() < DefaultCaret.this.dot || e.getOffset() < DefaultCaret.this.mark) && DefaultCaret.this.selectionTag != null) {
                    try {
                        DefaultCaret.this.component.getHighlighter().changeHighlight(DefaultCaret.this.selectionTag, Math.min(DefaultCaret.this.dot, DefaultCaret.this.mark), Math.max(DefaultCaret.this.dot, DefaultCaret.this.mark));
                    }
                    catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
                return;
            }
            int offs0 = e.getOffset();
            int offs1 = offs0 + e.getLength();
            int newDot = DefaultCaret.this.dot;
            boolean adjustDotBias = false;
            int newMark = DefaultCaret.this.mark;
            boolean adjustMarkBias = false;
            if (e instanceof AbstractDocument.UndoRedoDocumentEvent) {
                DefaultCaret.this.setDot(offs0);
                return;
            }
            if (newDot >= offs1) {
                if ((newDot -= offs1 - offs0) == offs1) {
                    adjustDotBias = true;
                }
            } else if (newDot >= offs0) {
                newDot = offs0;
                adjustDotBias = true;
            }
            if (newMark >= offs1) {
                if ((newMark -= offs1 - offs0) == offs1) {
                    adjustMarkBias = true;
                }
            } else if (newMark >= offs0) {
                newMark = offs0;
                adjustMarkBias = true;
            }
            if (newMark == newDot) {
                DefaultCaret.this.forceCaretPositionChange = true;
                try {
                    DefaultCaret.this.setDot(newDot, DefaultCaret.this.guessBiasForOffset(newDot, DefaultCaret.this.dotBias, DefaultCaret.this.dotLTR));
                }
                finally {
                    DefaultCaret.this.forceCaretPositionChange = false;
                }
                DefaultCaret.this.ensureValidPosition();
            } else {
                Position.Bias dotBias = DefaultCaret.this.dotBias;
                Position.Bias markBias = DefaultCaret.this.markBias;
                if (adjustDotBias) {
                    dotBias = DefaultCaret.this.guessBiasForOffset(newDot, dotBias, DefaultCaret.this.dotLTR);
                }
                if (adjustMarkBias) {
                    markBias = DefaultCaret.this.guessBiasForOffset(DefaultCaret.this.mark, markBias, DefaultCaret.this.markLTR);
                }
                DefaultCaret.this.setDot(newMark, markBias);
                if (DefaultCaret.this.getDot() == newMark) {
                    DefaultCaret.this.moveDot(newDot, dotBias);
                }
                DefaultCaret.this.ensureValidPosition();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (DefaultCaret.this.getUpdatePolicy() == 1 || DefaultCaret.this.getUpdatePolicy() == 0 && !SwingUtilities.isEventDispatchThread()) {
                return;
            }
            if (e instanceof AbstractDocument.UndoRedoDocumentEvent) {
                DefaultCaret.this.setDot(e.getOffset() + e.getLength());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            if (oldValue instanceof Document || newValue instanceof Document) {
                DefaultCaret.this.setDot(0);
                if (oldValue != null) {
                    ((Document)oldValue).removeDocumentListener(this);
                }
                if (newValue != null) {
                    ((Document)newValue).addDocumentListener(this);
                }
            } else if ("enabled".equals(evt.getPropertyName())) {
                Boolean enabled = (Boolean)evt.getNewValue();
                if (DefaultCaret.this.component.isFocusOwner()) {
                    if (enabled == Boolean.TRUE) {
                        if (DefaultCaret.this.component.isEditable()) {
                            DefaultCaret.this.setVisible(true);
                        }
                        DefaultCaret.this.setSelectionVisible(true);
                    } else {
                        DefaultCaret.this.setVisible(false);
                        DefaultCaret.this.setSelectionVisible(false);
                    }
                }
            } else if ("caretWidth".equals(evt.getPropertyName())) {
                Integer newWidth = (Integer)evt.getNewValue();
                DefaultCaret.this.caretWidth = newWidth != null ? newWidth : -1;
                DefaultCaret.this.repaint();
            } else if ("caretAspectRatio".equals(evt.getPropertyName())) {
                Number newRatio = (Number)evt.getNewValue();
                DefaultCaret.this.aspectRatio = newRatio != null ? newRatio.floatValue() : -1.0f;
                DefaultCaret.this.repaint();
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            if (DefaultCaret.this.ownsSelection) {
                DefaultCaret.this.ownsSelection = false;
                if (DefaultCaret.this.component != null && !DefaultCaret.this.component.hasFocus()) {
                    DefaultCaret.this.setSelectionVisible(false);
                }
            }
        }
    }

    class SafeScroller
    implements Runnable {
        Rectangle r;

        SafeScroller(Rectangle r) {
            this.r = r;
        }

        @Override
        public void run() {
            if (DefaultCaret.this.component != null) {
                DefaultCaret.this.component.scrollRectToVisible(this.r);
            }
        }
    }

    private class DefaultFilterBypass
    extends NavigationFilter.FilterBypass {
        private DefaultFilterBypass() {
        }

        @Override
        public Caret getCaret() {
            return DefaultCaret.this;
        }

        @Override
        public void setDot(int dot, Position.Bias bias) {
            DefaultCaret.this.handleSetDot(dot, bias);
        }

        @Override
        public void moveDot(int dot, Position.Bias bias) {
            DefaultCaret.this.handleMoveDot(dot, bias);
        }
    }
}

