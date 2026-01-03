/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleHyperlink;
import javax.accessibility.AccessibleHypertext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.LookAndFeel;
import javax.swing.SizeRequirements;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.CompositeView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.GlyphView;
import javax.swing.text.JTextComponent;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import sun.reflect.misc.ReflectUtil;

@JavaBean(defaultProperty="UIClassID", description="A text component to edit various types of content.")
@SwingContainer(value=false)
public class JEditorPane
extends JTextComponent {
    private SwingWorker<URL, Object> pageLoader;
    private EditorKit kit;
    private boolean isUserSetEditorKit;
    private Hashtable<String, Object> pageProperties;
    static final String PostDataProperty = "javax.swing.JEditorPane.postdata";
    private Hashtable<String, EditorKit> typeHandlers;
    private static final Object kitRegistryKey = new StringBuffer("JEditorPane.kitRegistry");
    private static final Object kitTypeRegistryKey = new StringBuffer("JEditorPane.kitTypeRegistry");
    private static final Object kitLoaderRegistryKey = new StringBuffer("JEditorPane.kitLoaderRegistry");
    private static final String uiClassID = "EditorPaneUI";
    public static final String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits";
    public static final String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties";
    static final Map<String, String> defaultEditorKitMap = new HashMap<String, String>(0);

    public JEditorPane() {
        this.setFocusCycleRoot(true);
        this.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy(){

            @Override
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
                if (focusCycleRoot != JEditorPane.this || !JEditorPane.this.isEditable() && JEditorPane.this.getComponentCount() > 0) {
                    return super.getComponentAfter(focusCycleRoot, aComponent);
                }
                Container rootAncestor = JEditorPane.this.getFocusCycleRootAncestor();
                return rootAncestor != null ? rootAncestor.getFocusTraversalPolicy().getComponentAfter(rootAncestor, JEditorPane.this) : null;
            }

            @Override
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                if (focusCycleRoot != JEditorPane.this || !JEditorPane.this.isEditable() && JEditorPane.this.getComponentCount() > 0) {
                    return super.getComponentBefore(focusCycleRoot, aComponent);
                }
                Container rootAncestor = JEditorPane.this.getFocusCycleRootAncestor();
                return rootAncestor != null ? rootAncestor.getFocusTraversalPolicy().getComponentBefore(rootAncestor, JEditorPane.this) : null;
            }

            @Override
            public Component getDefaultComponent(Container focusCycleRoot) {
                return focusCycleRoot != JEditorPane.this || !JEditorPane.this.isEditable() && JEditorPane.this.getComponentCount() > 0 ? super.getDefaultComponent(focusCycleRoot) : null;
            }

            @Override
            protected boolean accept(Component aComponent) {
                return aComponent != JEditorPane.this ? super.accept(aComponent) : false;
            }
        });
        LookAndFeel.installProperty(this, "focusTraversalKeysForward", JComponent.getManagingFocusForwardTraversalKeys());
        LookAndFeel.installProperty(this, "focusTraversalKeysBackward", JComponent.getManagingFocusBackwardTraversalKeys());
    }

    public JEditorPane(URL initialPage) throws IOException {
        this();
        this.setPage(initialPage);
    }

    public JEditorPane(String url) throws IOException {
        this();
        this.setPage(url);
    }

    public JEditorPane(String type, String text) {
        this();
        this.setContentType(type);
        this.setText(text);
    }

    public synchronized void addHyperlinkListener(HyperlinkListener listener) {
        this.listenerList.add(HyperlinkListener.class, listener);
    }

    public synchronized void removeHyperlinkListener(HyperlinkListener listener) {
        this.listenerList.remove(HyperlinkListener.class, listener);
    }

    @BeanProperty(bound=false)
    public synchronized HyperlinkListener[] getHyperlinkListeners() {
        return (HyperlinkListener[])this.listenerList.getListeners(HyperlinkListener.class);
    }

    public void fireHyperlinkUpdate(HyperlinkEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != HyperlinkListener.class) continue;
            ((HyperlinkListener)listeners[i + 1]).hyperlinkUpdate(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @BeanProperty(expert=true, description="the URL used to set content")
    public void setPage(URL page) throws IOException {
        String reference;
        if (page == null) {
            throw new IOException("invalid url");
        }
        URL loaded = this.getPage();
        if (!page.equals(loaded) && page.getRef() == null) {
            this.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }
        boolean reloaded = false;
        Object postData = this.getPostData();
        if (loaded == null || !loaded.sameFile(page) || postData != null) {
            int p = this.getAsynchronousLoadPriority(this.getDocument());
            if (p < 0) {
                InputStream in = this.getStream(page);
                if (this.kit != null) {
                    Document doc = this.initializeModel(this.kit, page);
                    p = this.getAsynchronousLoadPriority(doc);
                    if (p >= 0) {
                        this.setDocument(doc);
                        JEditorPane jEditorPane = this;
                        synchronized (jEditorPane) {
                            this.pageLoader = new PageLoader(doc, in, loaded, page);
                            this.pageLoader.execute();
                        }
                        return;
                    }
                    this.read(in, doc);
                    this.setDocument(doc);
                    reloaded = true;
                }
            } else {
                if (this.pageLoader != null) {
                    this.pageLoader.cancel(true);
                }
                this.pageLoader = new PageLoader(null, null, loaded, page);
                this.pageLoader.execute();
                return;
            }
        }
        if ((reference = page.getRef()) != null) {
            if (!reloaded) {
                this.scrollToReference(reference);
            } else {
                SwingUtilities.invokeLater(new Runnable(){
                    final /* synthetic */ JEditorPane this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        this.this$0.scrollToReference(reference);
                    }
                });
            }
            this.getDocument().putProperty("stream", page);
        }
        this.firePropertyChange("page", loaded, page);
    }

    private Document initializeModel(EditorKit kit, URL page) {
        Document doc = kit.createDefaultDocument();
        if (this.pageProperties != null) {
            Enumeration<String> e = this.pageProperties.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                doc.putProperty(key, this.pageProperties.get(key));
            }
            this.pageProperties.clear();
        }
        if (doc.getProperty("stream") == null) {
            doc.putProperty("stream", page);
        }
        return doc;
    }

    private int getAsynchronousLoadPriority(Document doc) {
        return doc instanceof AbstractDocument ? ((AbstractDocument)doc).getAsynchronousLoadPriority() : -1;
    }

    public void read(InputStream in, Object desc) throws IOException {
        if (desc instanceof HTMLDocument && this.kit instanceof HTMLEditorKit) {
            HTMLDocument hdoc = (HTMLDocument)desc;
            this.setDocument(hdoc);
            this.read(in, hdoc);
        } else {
            String charset = (String)this.getClientProperty("charset");
            InputStreamReader r = charset != null ? new InputStreamReader(in, charset) : new InputStreamReader(in);
            super.read(r, desc);
        }
    }

    void read(InputStream in, Document doc) throws IOException {
        String charset;
        if (!Boolean.TRUE.equals(doc.getProperty("IgnoreCharsetDirective"))) {
            int READ_LIMIT = 10240;
            in = new BufferedInputStream(in, 10240);
            in.mark(10240);
        }
        try (InputStreamReader r = (charset = (String)this.getClientProperty("charset")) != null ? new InputStreamReader(in, charset) : new InputStreamReader(in);){
            try {
                this.kit.read(r, doc, 0);
            }
            catch (BadLocationException e) {
                throw new IOException(e.getMessage());
            }
            catch (ChangedCharSetException changedCharSetException) {
                block18: {
                    String charSetSpec = changedCharSetException.getCharSetSpec();
                    if (changedCharSetException.keyEqualsCharSet()) {
                        this.putClientProperty("charset", charSetSpec);
                    } else {
                        this.setCharsetFromContentTypeParameters(charSetSpec);
                    }
                    try {
                        in.reset();
                    }
                    catch (IOException exception) {
                        in.close();
                        URL url = (URL)doc.getProperty("stream");
                        if (url != null) {
                            URLConnection conn = url.openConnection();
                            in = conn.getInputStream();
                            break block18;
                        }
                        throw changedCharSetException;
                    }
                }
                try {
                    doc.remove(0, doc.getLength());
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
                doc.putProperty("IgnoreCharsetDirective", true);
                this.read(in, doc);
            }
        }
    }

    protected InputStream getStream(URL page) throws IOException {
        final URLConnection conn = page.openConnection();
        if (conn instanceof HttpURLConnection) {
            int response;
            boolean redirect;
            HttpURLConnection hconn = (HttpURLConnection)conn;
            hconn.setInstanceFollowRedirects(false);
            Object postData = this.getPostData();
            if (postData != null) {
                this.handlePostData(hconn, postData);
            }
            boolean bl = redirect = (response = hconn.getResponseCode()) >= 300 && response <= 399;
            if (redirect) {
                String loc = conn.getHeaderField("Location");
                if (loc.startsWith("http", 0)) {
                    URL uRL = page = new URL(loc);
                } else {
                    URL uRL = page = new URL(page, loc);
                }
                return this.getStream(page);
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            this.handleConnectionProperties(conn);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable(){
                    final /* synthetic */ JEditorPane this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        this.this$0.handleConnectionProperties(conn);
                    }
                });
            }
            catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return conn.getInputStream();
    }

    private void handleConnectionProperties(URLConnection conn) {
        String type;
        if (this.pageProperties == null) {
            this.pageProperties = new Hashtable();
        }
        if ((type = conn.getContentType()) != null) {
            this.setContentType(type);
            this.pageProperties.put("content-type", type);
        }
        this.pageProperties.put("stream", conn.getURL());
        String enc = conn.getContentEncoding();
        if (enc != null) {
            this.pageProperties.put("content-encoding", enc);
        }
    }

    private Object getPostData() {
        return this.getDocument().getProperty(PostDataProperty);
    }

    private void handlePostData(HttpURLConnection conn, Object postData) throws IOException {
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = conn.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);){
            dos.writeBytes((String)postData);
        }
    }

    public void scrollToReference(String reference) {
        Document d = this.getDocument();
        if (d instanceof HTMLDocument) {
            HTMLDocument doc = (HTMLDocument)d;
            HTMLDocument.Iterator iter = doc.getIterator(HTML.Tag.A);
            while (iter.isValid()) {
                AttributeSet a = iter.getAttributes();
                String nm = (String)a.getAttribute(HTML.Attribute.NAME);
                if (nm != null && nm.equals(reference)) {
                    try {
                        int pos = iter.getStartOffset();
                        Rectangle r = this.modelToView(pos);
                        if (r != null) {
                            Rectangle vis = this.getVisibleRect();
                            r.height = vis.height;
                            this.scrollRectToVisible(r);
                            this.setCaretPosition(pos);
                        }
                    }
                    catch (BadLocationException ble) {
                        UIManager.getLookAndFeel().provideErrorFeedback(this);
                    }
                }
                iter.next();
            }
        }
    }

    public URL getPage() {
        return (URL)this.getDocument().getProperty("stream");
    }

    public void setPage(String url) throws IOException {
        if (url == null) {
            throw new IOException("invalid url");
        }
        URL page = new URL(url);
        this.setPage(page);
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    protected EditorKit createDefaultEditorKit() {
        return new PlainEditorKit();
    }

    public EditorKit getEditorKit() {
        if (this.kit == null) {
            this.kit = this.createDefaultEditorKit();
            this.isUserSetEditorKit = false;
        }
        return this.kit;
    }

    public final String getContentType() {
        return this.kit != null ? this.kit.getContentType() : null;
    }

    @BeanProperty(bound=false, description="the type of content")
    public final void setContentType(String type) {
        EditorKit k;
        int parm = type.indexOf(59);
        if (parm > -1) {
            String paramList = type.substring(parm);
            if ((type = type.substring(0, parm).trim()).toLowerCase().startsWith("text/")) {
                this.setCharsetFromContentTypeParameters(paramList);
            }
        }
        if (!(this.kit != null && type.equals(this.kit.getContentType()) && this.isUserSetEditorKit || (k = this.getEditorKitForContentType(type)) == null || k == this.kit)) {
            this.setEditorKit(k);
            this.isUserSetEditorKit = false;
        }
    }

    private void setCharsetFromContentTypeParameters(String paramlist) {
        try {
            HeaderParser hdrParser;
            String charset;
            int semi = paramlist.indexOf(59);
            if (semi > -1 && semi < paramlist.length() - 1) {
                paramlist = paramlist.substring(semi + 1);
            }
            if (paramlist.length() > 0 && (charset = (hdrParser = new HeaderParser(paramlist)).findValue("charset")) != null) {
                this.putClientProperty("charset", charset);
            }
        }
        catch (IndexOutOfBoundsException | NullPointerException semi) {
        }
        catch (Exception e) {
            System.err.println("JEditorPane.getCharsetFromContentTypeParameters failed on: " + paramlist);
            e.printStackTrace();
        }
    }

    @BeanProperty(expert=true, description="the currently installed kit for handling content")
    public void setEditorKit(EditorKit kit) {
        EditorKit old = this.kit;
        this.isUserSetEditorKit = true;
        if (old != null) {
            old.deinstall(this);
        }
        this.kit = kit;
        if (this.kit != null) {
            this.kit.install(this);
            this.setDocument(this.kit.createDefaultDocument());
        }
        this.firePropertyChange("editorKit", old, kit);
    }

    public EditorKit getEditorKitForContentType(String type) {
        EditorKit k;
        if (this.typeHandlers == null) {
            this.typeHandlers = new Hashtable(3);
        }
        if ((k = this.typeHandlers.get(type)) == null && (k = JEditorPane.createEditorKitForContentType(type)) != null) {
            this.setEditorKitForContentType(type, k);
        }
        if (k == null) {
            k = this.createDefaultEditorKit();
        }
        return k;
    }

    public void setEditorKitForContentType(String type, EditorKit k) {
        if (this.typeHandlers == null) {
            this.typeHandlers = new Hashtable(3);
        }
        this.typeHandlers.put(type, k);
    }

    @Override
    public void replaceSelection(String content) {
        if (!this.isEditable()) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
            return;
        }
        EditorKit kit = this.getEditorKit();
        if (kit instanceof StyledEditorKit) {
            try {
                Document doc = this.getDocument();
                Caret caret = this.getCaret();
                boolean composedTextSaved = this.saveComposedText(caret.getDot());
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).replace(p0, p1 - p0, content, ((StyledEditorKit)kit).getInputAttributes());
                } else {
                    if (p0 != p1) {
                        doc.remove(p0, p1 - p0);
                    }
                    if (content != null && content.length() > 0) {
                        doc.insertString(p0, content, ((StyledEditorKit)kit).getInputAttributes());
                    }
                }
                if (composedTextSaved) {
                    this.restoreComposedText();
                }
            }
            catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
            }
        } else {
            super.replaceSelection(content);
        }
    }

    public static EditorKit createEditorKitForContentType(String type) {
        Hashtable<String, EditorKit> kitRegistry = JEditorPane.getKitRegisty();
        EditorKit k = kitRegistry.get(type);
        if (k == null) {
            String classname = JEditorPane.getKitTypeRegistry().get(type);
            ClassLoader loader = JEditorPane.getKitLoaderRegistry().get(type);
            try {
                Class<?> c;
                if (loader != null) {
                    ReflectUtil.checkPackageAccess(classname);
                    c = loader.loadClass(classname);
                } else {
                    c = SwingUtilities.loadSystemClass(classname);
                }
                k = (EditorKit)c.newInstance();
                kitRegistry.put(type, k);
            }
            catch (Throwable e) {
                k = null;
            }
        }
        if (k != null) {
            return (EditorKit)k.clone();
        }
        return null;
    }

    public static void registerEditorKitForContentType(String type, String classname) {
        JEditorPane.registerEditorKitForContentType(type, classname, Thread.currentThread().getContextClassLoader());
    }

    public static void registerEditorKitForContentType(String type, String classname, ClassLoader loader) {
        JEditorPane.getKitTypeRegistry().put(type, classname);
        if (loader != null) {
            JEditorPane.getKitLoaderRegistry().put(type, loader);
        } else {
            JEditorPane.getKitLoaderRegistry().remove(type);
        }
        JEditorPane.getKitRegisty().remove(type);
    }

    public static String getEditorKitClassNameForContentType(String type) {
        return JEditorPane.getKitTypeRegistry().get(type);
    }

    private static Hashtable<String, String> getKitTypeRegistry() {
        JEditorPane.loadDefaultKitsIfNecessary();
        Hashtable tmp = (Hashtable)SwingUtilities.appContextGet(kitTypeRegistryKey);
        return tmp;
    }

    private static Hashtable<String, ClassLoader> getKitLoaderRegistry() {
        JEditorPane.loadDefaultKitsIfNecessary();
        Hashtable tmp = (Hashtable)SwingUtilities.appContextGet(kitLoaderRegistryKey);
        return tmp;
    }

    private static Hashtable<String, EditorKit> getKitRegisty() {
        Hashtable ht = (Hashtable)SwingUtilities.appContextGet(kitRegistryKey);
        if (ht == null) {
            ht = new Hashtable(3);
            SwingUtilities.appContextPut(kitRegistryKey, ht);
        }
        return ht;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadDefaultKitsIfNecessary() {
        if (SwingUtilities.appContextGet(kitTypeRegistryKey) == null) {
            Map<String, String> map = defaultEditorKitMap;
            synchronized (map) {
                if (defaultEditorKitMap.size() == 0) {
                    defaultEditorKitMap.put("text/plain", "javax.swing.JEditorPane$PlainEditorKit");
                    defaultEditorKitMap.put("text/html", "javax.swing.text.html.HTMLEditorKit");
                    defaultEditorKitMap.put("text/rtf", "javax.swing.text.rtf.RTFEditorKit");
                    defaultEditorKitMap.put("application/rtf", "javax.swing.text.rtf.RTFEditorKit");
                }
            }
            Hashtable ht = new Hashtable();
            SwingUtilities.appContextPut(kitTypeRegistryKey, ht);
            ht = new Hashtable();
            SwingUtilities.appContextPut(kitLoaderRegistryKey, ht);
            for (String key : defaultEditorKitMap.keySet()) {
                JEditorPane.registerEditorKitForContentType(key, defaultEditorKitMap.get(key));
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            Dimension min;
            JViewport port = (JViewport)parent;
            TextUI ui = this.getUI();
            int prefWidth = d.width;
            int prefHeight = d.height;
            if (!this.getScrollableTracksViewportWidth()) {
                int w = port.getWidth();
                min = ui.getMinimumSize(this);
                if (w != 0 && w < min.width) {
                    prefWidth = min.width;
                }
            }
            if (!this.getScrollableTracksViewportHeight()) {
                int h = port.getHeight();
                min = ui.getMinimumSize(this);
                if (h != 0 && h < min.height) {
                    prefHeight = min.height;
                }
            }
            if (prefWidth != d.width || prefHeight != d.height) {
                d = new Dimension(prefWidth, prefHeight);
            }
        }
        return d;
    }

    @Override
    @BeanProperty(bound=false, description="the text of this component")
    public void setText(String t) {
        try {
            Document doc = this.getDocument();
            doc.remove(0, doc.getLength());
            if (t == null || t.isEmpty()) {
                return;
            }
            StringReader r = new StringReader(t);
            EditorKit kit = this.getEditorKit();
            kit.read(r, doc, 0);
        }
        catch (IOException | BadLocationException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
    }

    @Override
    public String getText() {
        String txt;
        try {
            StringWriter buf = new StringWriter();
            this.write(buf);
            txt = buf.toString();
        }
        catch (IOException ioe) {
            txt = null;
        }
        return txt;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportWidth() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport)parent;
            TextUI ui = this.getUI();
            int w = port.getWidth();
            Dimension min = ui.getMinimumSize(this);
            Dimension max = ui.getMaximumSize(this);
            if (w >= min.width && w <= max.width) {
                return true;
            }
        }
        return false;
    }

    @Override
    @BeanProperty(bound=false)
    public boolean getScrollableTracksViewportHeight() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport)parent;
            TextUI ui = this.getUI();
            int h = port.getHeight();
            Dimension min = ui.getMinimumSize(this);
            if (h >= min.height) {
                Dimension max = ui.getMaximumSize(this);
                if (h <= max.height) {
                    return true;
                }
            }
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
        String kitString = this.kit != null ? this.kit.toString() : "";
        String typeHandlersString = this.typeHandlers != null ? this.typeHandlers.toString() : "";
        return super.paramString() + ",kit=" + kitString + ",typeHandlers=" + typeHandlersString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.getEditorKit() instanceof HTMLEditorKit) {
            if (this.accessibleContext == null || this.accessibleContext.getClass() != AccessibleJEditorPaneHTML.class) {
                this.accessibleContext = new AccessibleJEditorPaneHTML();
            }
        } else if (this.accessibleContext == null || this.accessibleContext.getClass() != AccessibleJEditorPane.class) {
            this.accessibleContext = new AccessibleJEditorPane();
        }
        return this.accessibleContext;
    }

    class PageLoader
    extends SwingWorker<URL, Object> {
        InputStream in;
        URL old;
        URL page;
        Document doc;

        PageLoader(Document doc, InputStream in, URL old, URL page) {
            this.in = in;
            this.old = old;
            this.page = page;
            this.doc = doc;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        protected URL doInBackground() {
            boolean pageLoaded;
            block10: {
                block9: {
                    pageLoaded = false;
                    if (this.in != null) break block9;
                    this.in = JEditorPane.this.getStream(this.page);
                    if (JEditorPane.this.kit != null) break block9;
                    UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
                    URL uRL = this.old;
                    if (!pageLoaded) return uRL;
                    SwingUtilities.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            JEditorPane.this.firePropertyChange("page", PageLoader.this.old, PageLoader.this.page);
                        }
                    });
                    return uRL;
                }
                if (this.doc != null) break block10;
                try {
                    SwingUtilities.invokeAndWait(new Runnable(){

                        @Override
                        public void run() {
                            PageLoader.this.doc = JEditorPane.this.initializeModel(JEditorPane.this.kit, PageLoader.this.page);
                            JEditorPane.this.setDocument(PageLoader.this.doc);
                        }
                    });
                }
                catch (InterruptedException | InvocationTargetException ex) {
                    UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
                    URL uRL = this.old;
                    if (!pageLoaded) return uRL;
                    SwingUtilities.invokeLater(new /* invalid duplicate definition of identical inner class */);
                    return uRL;
                }
            }
            try {
                JEditorPane.this.read(this.in, this.doc);
                URL page = (URL)this.doc.getProperty("stream");
                String reference = page.getRef();
                if (reference != null) {
                    Runnable callScrollToReference = new Runnable(){

                        @Override
                        public void run() {
                            URL u = (URL)JEditorPane.this.getDocument().getProperty("stream");
                            String ref = u.getRef();
                            JEditorPane.this.scrollToReference(ref);
                        }
                    };
                    SwingUtilities.invokeLater(callScrollToReference);
                }
                if (!(pageLoaded = true)) return pageLoaded ? this.page : this.old;
            }
            catch (IOException ioe) {
                try {
                    UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
                    if (!pageLoaded) return pageLoaded ? this.page : this.old;
                }
                catch (Throwable throwable) {
                    if (!pageLoaded) throw throwable;
                    SwingUtilities.invokeLater(new /* invalid duplicate definition of identical inner class */);
                    throw throwable;
                }
                SwingUtilities.invokeLater(new /* invalid duplicate definition of identical inner class */);
                return pageLoaded ? this.page : this.old;
            }
            SwingUtilities.invokeLater(new /* invalid duplicate definition of identical inner class */);
            return pageLoaded ? this.page : this.old;
        }
    }

    static class PlainEditorKit
    extends DefaultEditorKit
    implements ViewFactory {
        PlainEditorKit() {
        }

        @Override
        public ViewFactory getViewFactory() {
            return this;
        }

        @Override
        public View create(Element elem) {
            Document doc = elem.getDocument();
            Object i18nFlag = doc.getProperty("i18n");
            if (i18nFlag != null && i18nFlag.equals(Boolean.TRUE)) {
                return this.createI18N(elem);
            }
            return new WrappedPlainView(elem);
        }

        View createI18N(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals("content")) {
                    return new PlainParagraph(elem);
                }
                if (kind.equals("paragraph")) {
                    return new BoxView(elem, 1);
                }
            }
            return null;
        }

        static class PlainParagraph
        extends ParagraphView {
            PlainParagraph(Element elem) {
                super(elem);
                this.layoutPool = new LogicalView(elem);
                this.layoutPool.setParent(this);
            }

            @Override
            protected void setPropertiesFromAttributes() {
                Container c = this.getContainer();
                if (c != null && !c.getComponentOrientation().isLeftToRight()) {
                    this.setJustification(2);
                } else {
                    this.setJustification(0);
                }
            }

            @Override
            public int getFlowSpan(int index) {
                JTextArea area;
                Container c = this.getContainer();
                if (c instanceof JTextArea && !(area = (JTextArea)c).getLineWrap()) {
                    return Integer.MAX_VALUE;
                }
                return super.getFlowSpan(index);
            }

            @Override
            protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
                JTextArea area;
                SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
                Container c = this.getContainer();
                if (c instanceof JTextArea && !(area = (JTextArea)c).getLineWrap()) {
                    req.minimum = req.preferred;
                }
                return req;
            }

            static class LogicalView
            extends CompositeView {
                LogicalView(Element elem) {
                    super(elem);
                }

                @Override
                protected int getViewIndexAtPosition(int pos) {
                    Element elem = this.getElement();
                    if (elem.getElementCount() > 0) {
                        return elem.getElementIndex(pos);
                    }
                    return 0;
                }

                @Override
                protected boolean updateChildren(DocumentEvent.ElementChange ec, DocumentEvent e, ViewFactory f) {
                    return false;
                }

                @Override
                protected void loadChildren(ViewFactory f) {
                    Element elem = this.getElement();
                    if (elem.getElementCount() > 0) {
                        super.loadChildren(f);
                    } else {
                        GlyphView v = new GlyphView(elem);
                        this.append(v);
                    }
                }

                @Override
                public float getPreferredSpan(int axis) {
                    if (this.getViewCount() != 1) {
                        throw new Error("One child view is assumed.");
                    }
                    View v = this.getView(0);
                    return v.getPreferredSpan(axis);
                }

                @Override
                protected void forwardUpdateToView(View v, DocumentEvent e, Shape a, ViewFactory f) {
                    v.setParent(this);
                    super.forwardUpdateToView(v, e, a, f);
                }

                @Override
                public void paint(Graphics g, Shape allocation) {
                }

                @Override
                protected boolean isBefore(int x, int y, Rectangle alloc) {
                    return false;
                }

                @Override
                protected boolean isAfter(int x, int y, Rectangle alloc) {
                    return false;
                }

                @Override
                protected View getViewAtPoint(int x, int y, Rectangle alloc) {
                    return null;
                }

                @Override
                protected void childAllocation(int index, Rectangle a) {
                }
            }
        }
    }

    static class HeaderParser {
        String raw;
        String[][] tab;

        public HeaderParser(String raw) {
            this.raw = raw;
            this.tab = new String[10][2];
            this.parse();
        }

        private void parse() {
            if (this.raw != null) {
                this.raw = this.raw.trim();
                char[] ca = this.raw.toCharArray();
                int beg = 0;
                int end = 0;
                int i = 0;
                boolean inKey = true;
                boolean inQuote = false;
                int len = ca.length;
                while (end < len) {
                    char c = ca[end];
                    if (c == '=') {
                        this.tab[i][0] = new String(ca, beg, end - beg).toLowerCase();
                        inKey = false;
                        beg = ++end;
                        continue;
                    }
                    if (c == '\"') {
                        if (inQuote) {
                            this.tab[i++][1] = new String(ca, beg, end - beg);
                            inQuote = false;
                            while (++end < len && (ca[end] == ' ' || ca[end] == ',')) {
                            }
                            inKey = true;
                            beg = end;
                            continue;
                        }
                        inQuote = true;
                        beg = ++end;
                        continue;
                    }
                    if (c == ' ' || c == ',') {
                        if (inQuote) {
                            ++end;
                            continue;
                        }
                        if (inKey) {
                            this.tab[i++][0] = new String(ca, beg, end - beg).toLowerCase();
                        } else {
                            this.tab[i++][1] = new String(ca, beg, end - beg);
                        }
                        while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
                            ++end;
                        }
                        inKey = true;
                        beg = end;
                        continue;
                    }
                    ++end;
                }
                if (--end > beg) {
                    if (!inKey) {
                        this.tab[i++][1] = ca[end] == '\"' ? new String(ca, beg, end - beg) : new String(ca, beg, end - beg + 1);
                    } else {
                        this.tab[i][0] = new String(ca, beg, end - beg + 1).toLowerCase();
                    }
                } else if (end == beg) {
                    if (!inKey) {
                        this.tab[i++][1] = ca[end] == '\"' ? String.valueOf(ca[end - 1]) : String.valueOf(ca[end]);
                    } else {
                        this.tab[i][0] = String.valueOf(ca[end]).toLowerCase();
                    }
                }
            }
        }

        public String findKey(int i) {
            if (i < 0 || i > 10) {
                return null;
            }
            return this.tab[i][0];
        }

        public String findValue(int i) {
            if (i < 0 || i > 10) {
                return null;
            }
            return this.tab[i][1];
        }

        public String findValue(String key) {
            return this.findValue(key, null);
        }

        public String findValue(String k, String Default2) {
            if (k == null) {
                return Default2;
            }
            k = k.toLowerCase();
            for (int i = 0; i < 10; ++i) {
                if (this.tab[i][0] == null) {
                    return Default2;
                }
                if (!k.equals(this.tab[i][0])) continue;
                return this.tab[i][1];
            }
            return Default2;
        }

        public int findInt(String k, int Default2) {
            try {
                return Integer.parseInt(this.findValue(k, String.valueOf(Default2)));
            }
            catch (Throwable t) {
                return Default2;
            }
        }
    }

    protected class AccessibleJEditorPaneHTML
    extends AccessibleJEditorPane {
        private AccessibleContext accessibleContext;

        @Override
        public AccessibleText getAccessibleText() {
            return new JEditorPaneAccessibleHypertextSupport();
        }

        protected AccessibleJEditorPaneHTML() {
            HTMLEditorKit kit = (HTMLEditorKit)JEditorPane.this.getEditorKit();
            this.accessibleContext = kit.getAccessibleContext();
        }

        @Override
        public int getAccessibleChildrenCount() {
            if (this.accessibleContext != null) {
                return this.accessibleContext.getAccessibleChildrenCount();
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (this.accessibleContext != null) {
                return this.accessibleContext.getAccessibleChild(i);
            }
            return null;
        }

        @Override
        public Accessible getAccessibleAt(Point p) {
            if (this.accessibleContext != null && p != null) {
                try {
                    AccessibleComponent acomp = this.accessibleContext.getAccessibleComponent();
                    if (acomp != null) {
                        return acomp.getAccessibleAt(p);
                    }
                    return null;
                }
                catch (IllegalComponentStateException e) {
                    return null;
                }
            }
            return null;
        }
    }

    protected class AccessibleJEditorPane
    extends JTextComponent.AccessibleJTextComponent {
        protected AccessibleJEditorPane() {
            super(JEditorPane.this);
        }

        @Override
        public String getAccessibleDescription() {
            String description = this.accessibleDescription;
            if (description == null) {
                description = (String)JEditorPane.this.getClientProperty("AccessibleDescription");
            }
            if (description == null) {
                description = JEditorPane.this.getContentType();
            }
            return description;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }

    protected class JEditorPaneAccessibleHypertextSupport
    extends AccessibleJEditorPane
    implements AccessibleHypertext {
        LinkVector hyperlinks = new LinkVector();
        boolean linksValid = false;

        private void buildLinkTable() {
            this.hyperlinks.removeAllElements();
            Document d = JEditorPane.this.getDocument();
            if (d != null) {
                Element e;
                ElementIterator ei = new ElementIterator(d);
                while ((e = ei.next()) != null) {
                    AttributeSet as;
                    AttributeSet anchor;
                    String href;
                    if (!e.isLeaf() || (href = (anchor = (AttributeSet)(as = e.getAttributes()).getAttribute(HTML.Tag.A)) != null ? (String)anchor.getAttribute(HTML.Attribute.HREF) : null) == null) continue;
                    this.hyperlinks.addElement(new HTMLLink(e));
                }
            }
            this.linksValid = true;
        }

        public JEditorPaneAccessibleHypertextSupport() {
            Document d = JEditorPane.this.getDocument();
            if (d != null) {
                d.addDocumentListener(new DocumentListener(){
                    final /* synthetic */ JEditorPaneAccessibleHypertextSupport this$1;
                    {
                        this.this$1 = this$1;
                    }

                    @Override
                    public void changedUpdate(DocumentEvent theEvent) {
                        this.this$1.linksValid = false;
                    }

                    @Override
                    public void insertUpdate(DocumentEvent theEvent) {
                        this.this$1.linksValid = false;
                    }

                    @Override
                    public void removeUpdate(DocumentEvent theEvent) {
                        this.this$1.linksValid = false;
                    }
                });
            }
        }

        @Override
        public int getLinkCount() {
            if (!this.linksValid) {
                this.buildLinkTable();
            }
            return this.hyperlinks.size();
        }

        @Override
        public int getLinkIndex(int charIndex) {
            if (!this.linksValid) {
                this.buildLinkTable();
            }
            Element e = null;
            Document doc = JEditorPane.this.getDocument();
            if (doc != null) {
                e = doc.getDefaultRootElement();
                while (!e.isLeaf()) {
                    int index = e.getElementIndex(charIndex);
                    e = e.getElement(index);
                }
            }
            return this.hyperlinks.baseElementIndex(e);
        }

        @Override
        public AccessibleHyperlink getLink(int linkIndex) {
            if (!this.linksValid) {
                this.buildLinkTable();
            }
            if (linkIndex >= 0 && linkIndex < this.hyperlinks.size()) {
                return (AccessibleHyperlink)this.hyperlinks.elementAt(linkIndex);
            }
            return null;
        }

        public String getLinkText(int linkIndex) {
            Document d;
            Element e;
            if (!this.linksValid) {
                this.buildLinkTable();
            }
            if ((e = (Element)this.hyperlinks.elementAt(linkIndex)) != null && (d = JEditorPane.this.getDocument()) != null) {
                try {
                    return d.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
                }
                catch (BadLocationException exception) {
                    return null;
                }
            }
            return null;
        }

        private class LinkVector
        extends Vector<HTMLLink> {
            private LinkVector() {
            }

            public int baseElementIndex(Element e) {
                for (int i = 0; i < this.elementCount; ++i) {
                    HTMLLink l = (HTMLLink)this.elementAt(i);
                    if (l.element != e) continue;
                    return i;
                }
                return -1;
            }
        }

        public class HTMLLink
        extends AccessibleHyperlink {
            Element element;

            public HTMLLink(Element e) {
                this.element = e;
            }

            @Override
            public boolean isValid() {
                return JEditorPaneAccessibleHypertextSupport.this.linksValid;
            }

            @Override
            public int getAccessibleActionCount() {
                return 1;
            }

            @Override
            public boolean doAccessibleAction(int i) {
                URL u;
                if (i == 0 && this.isValid() && (u = (URL)this.getAccessibleActionObject(i)) != null) {
                    HyperlinkEvent linkEvent = new HyperlinkEvent(JEditorPane.this, HyperlinkEvent.EventType.ACTIVATED, u);
                    JEditorPane.this.fireHyperlinkUpdate(linkEvent);
                    return true;
                }
                return false;
            }

            @Override
            public String getAccessibleActionDescription(int i) {
                Document d;
                if (i == 0 && this.isValid() && (d = JEditorPane.this.getDocument()) != null) {
                    try {
                        return d.getText(this.getStartIndex(), this.getEndIndex() - this.getStartIndex());
                    }
                    catch (BadLocationException exception) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public Object getAccessibleActionObject(int i) {
                if (i == 0 && this.isValid()) {
                    String href;
                    AttributeSet as = this.element.getAttributes();
                    AttributeSet anchor = (AttributeSet)as.getAttribute(HTML.Tag.A);
                    String string = href = anchor != null ? (String)anchor.getAttribute(HTML.Attribute.HREF) : null;
                    if (href != null) {
                        URL u;
                        try {
                            URL uRL = u = new URL(JEditorPane.this.getPage(), href);
                        }
                        catch (MalformedURLException m) {
                            u = null;
                        }
                        return u;
                    }
                }
                return null;
            }

            @Override
            public Object getAccessibleActionAnchor(int i) {
                return this.getAccessibleActionDescription(i);
            }

            @Override
            public int getStartIndex() {
                return this.element.getStartOffset();
            }

            @Override
            public int getEndIndex() {
                return this.element.getEndOffset();
            }
        }
    }
}

