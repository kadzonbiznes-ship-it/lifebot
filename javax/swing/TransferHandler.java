/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import jdk.internal.access.JavaSecurityAccess;
import jdk.internal.access.SharedSecrets;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.reflect.misc.MethodUtil;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

public class TransferHandler
implements Serializable {
    public static final int NONE = 0;
    public static final int COPY = 1;
    public static final int MOVE = 2;
    public static final int COPY_OR_MOVE = 3;
    public static final int LINK = 0x40000000;
    private Image dragImage;
    private Point dragImageOffset;
    private String propertyName;
    private static SwingDragGestureRecognizer recognizer = null;
    static final Action cutAction = new TransferAction("cut");
    static final Action copyAction = new TransferAction("copy");
    static final Action pasteAction = new TransferAction("paste");

    public static Action getCutAction() {
        return cutAction;
    }

    public static Action getCopyAction() {
        return copyAction;
    }

    public static Action getPasteAction() {
        return pasteAction;
    }

    public TransferHandler(String property) {
        this.propertyName = property;
    }

    protected TransferHandler() {
        this(null);
    }

    public void setDragImage(Image img) {
        this.dragImage = img;
    }

    public Image getDragImage() {
        return this.dragImage;
    }

    public void setDragImageOffset(Point p) {
        this.dragImageOffset = new Point(p);
    }

    public Point getDragImageOffset() {
        if (this.dragImageOffset == null) {
            return new Point(0, 0);
        }
        return new Point(this.dragImageOffset);
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        int srcActions = this.getSourceActions(comp);
        if (!(e instanceof MouseEvent) || action != 1 && action != 2 && action != 0x40000000 || (srcActions & action) == 0) {
            action = 0;
        }
        if (action != 0 && !GraphicsEnvironment.isHeadless()) {
            if (recognizer == null) {
                recognizer = new SwingDragGestureRecognizer(new DragHandler());
            }
            recognizer.gestured(comp, (MouseEvent)e, srcActions, action);
        } else {
            this.exportDone(comp, null, 0);
        }
    }

    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
        Transferable t;
        if ((action == 1 || action == 2) && (this.getSourceActions(comp) & action) != 0 && (t = this.createTransferable(comp)) != null) {
            try {
                clip.setContents(t, null);
                this.exportDone(comp, t, action);
                return;
            }
            catch (IllegalStateException ise) {
                this.exportDone(comp, t, 0);
                throw ise;
            }
        }
        this.exportDone(comp, null, 0);
    }

    public boolean importData(TransferSupport support) {
        return support.getComponent() instanceof JComponent ? this.importData((JComponent)support.getComponent(), support.getTransferable()) : false;
    }

    public boolean importData(JComponent comp, Transferable t) {
        PropertyDescriptor prop = this.getPropertyDescriptor(comp);
        if (prop != null) {
            Method writer = prop.getWriteMethod();
            if (writer == null) {
                return false;
            }
            Class<?>[] params = writer.getParameterTypes();
            if (params.length != 1) {
                return false;
            }
            DataFlavor flavor = this.getPropertyDataFlavor(params[0], t.getTransferDataFlavors());
            if (flavor != null) {
                try {
                    Object value = t.getTransferData(flavor);
                    Object[] args = new Object[]{value};
                    MethodUtil.invoke(writer, comp, args);
                    return true;
                }
                catch (Exception ex) {
                    System.err.println("Invocation failed");
                }
            }
        }
        return false;
    }

    public boolean canImport(TransferSupport support) {
        return support.getComponent() instanceof JComponent ? this.canImport((JComponent)support.getComponent(), support.getDataFlavors()) : false;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        PropertyDescriptor prop = this.getPropertyDescriptor(comp);
        if (prop != null) {
            Method writer = prop.getWriteMethod();
            if (writer == null) {
                return false;
            }
            Class<?>[] params = writer.getParameterTypes();
            if (params.length != 1) {
                return false;
            }
            DataFlavor flavor = this.getPropertyDataFlavor(params[0], transferFlavors);
            if (flavor != null) {
                return true;
            }
        }
        return false;
    }

    public int getSourceActions(JComponent c) {
        PropertyDescriptor prop = this.getPropertyDescriptor(c);
        if (prop != null) {
            return 1;
        }
        return 0;
    }

    public Icon getVisualRepresentation(Transferable t) {
        return null;
    }

    protected Transferable createTransferable(JComponent c) {
        PropertyDescriptor property = this.getPropertyDescriptor(c);
        if (property != null) {
            return new PropertyTransferable(property, c);
        }
        return null;
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    private PropertyDescriptor getPropertyDescriptor(JComponent comp) {
        BeanInfo bi;
        if (this.propertyName == null) {
            return null;
        }
        Class<?> k = comp.getClass();
        try {
            bi = Introspector.getBeanInfo(k);
        }
        catch (IntrospectionException ex) {
            return null;
        }
        PropertyDescriptor[] props = bi.getPropertyDescriptors();
        for (int i = 0; i < props.length; ++i) {
            Class<?>[] params;
            Method reader;
            if (!this.propertyName.equals(props[i].getName()) || (reader = props[i].getReadMethod()) == null || (params = reader.getParameterTypes()) != null && params.length != 0) continue;
            return props[i];
        }
        return null;
    }

    private DataFlavor getPropertyDataFlavor(Class<?> k, DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; ++i) {
            DataFlavor flavor = flavors[i];
            if (!"application".equals(flavor.getPrimaryType()) || !"x-java-jvm-local-objectref".equals(flavor.getSubType()) || !k.isAssignableFrom(flavor.getRepresentationClass())) continue;
            return flavor;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DropTargetListener getDropTargetListener() {
        Class<DropHandler> clazz = DropHandler.class;
        synchronized (DropHandler.class) {
            DropHandler handler = (DropHandler)AppContext.getAppContext().get(DropHandler.class);
            if (handler == null) {
                handler = new DropHandler();
                AppContext.getAppContext().put(DropHandler.class, handler);
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return handler;
        }
    }

    private static class SwingDragGestureRecognizer
    extends DragGestureRecognizer {
        SwingDragGestureRecognizer(DragGestureListener dgl) {
            super(DragSource.getDefaultDragSource(), null, 0, dgl);
        }

        void gestured(JComponent c, MouseEvent e, int srcActions, int action) {
            this.setComponent(c);
            this.setSourceActions(srcActions);
            this.appendEvent(e);
            this.fireDragGestureRecognized(action, e.getPoint());
        }

        @Override
        protected void registerListeners() {
        }

        @Override
        protected void unregisterListeners() {
        }
    }

    private static class DragHandler
    implements DragGestureListener,
    DragSourceListener {
        private boolean scrolls;

        private DragHandler() {
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            JComponent c = (JComponent)dge.getComponent();
            TransferHandler th = c.getTransferHandler();
            Transferable t = th.createTransferable(c);
            if (t != null) {
                this.scrolls = c.getAutoscrolls();
                c.setAutoscrolls(false);
                try {
                    Image im = th.getDragImage();
                    if (im == null) {
                        dge.startDrag(null, t, this);
                    } else {
                        dge.startDrag(null, im, th.getDragImageOffset(), t, this);
                    }
                    return;
                }
                catch (RuntimeException re) {
                    c.setAutoscrolls(this.scrolls);
                }
            }
            th.exportDone(c, t, 0);
        }

        @Override
        public void dragEnter(DragSourceDragEvent dsde) {
        }

        @Override
        public void dragOver(DragSourceDragEvent dsde) {
        }

        @Override
        public void dragExit(DragSourceEvent dsde) {
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            DragSourceContext dsc = dsde.getDragSourceContext();
            JComponent c = (JComponent)dsc.getComponent();
            if (dsde.getDropSuccess()) {
                c.getTransferHandler().exportDone(c, dsc.getTransferable(), dsde.getDropAction());
            } else {
                c.getTransferHandler().exportDone(c, dsc.getTransferable(), 0);
            }
            c.setAutoscrolls(this.scrolls);
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) {
        }
    }

    public static final class TransferSupport {
        private boolean isDrop;
        private Component component;
        private boolean showDropLocationIsSet;
        private boolean showDropLocation;
        private int dropAction = -1;
        private Object source;
        private DropLocation dropLocation;

        private TransferSupport(Component component, DropTargetEvent event) {
            this.isDrop = true;
            this.setDNDVariables(component, event);
        }

        public TransferSupport(Component component, Transferable transferable) {
            if (component == null) {
                throw new NullPointerException("component is null");
            }
            if (transferable == null) {
                throw new NullPointerException("transferable is null");
            }
            this.isDrop = false;
            this.component = component;
            this.source = transferable;
        }

        private void setDNDVariables(Component component, DropTargetEvent event) {
            Point p;
            assert (this.isDrop);
            this.component = component;
            this.source = event;
            this.dropLocation = null;
            this.dropAction = -1;
            this.showDropLocationIsSet = false;
            if (this.source == null) {
                return;
            }
            assert (this.source instanceof DropTargetDragEvent || this.source instanceof DropTargetDropEvent);
            Point point = p = this.source instanceof DropTargetDragEvent ? ((DropTargetDragEvent)this.source).getLocation() : ((DropTargetDropEvent)this.source).getLocation();
            if (SunToolkit.isInstanceOf(component, "javax.swing.text.JTextComponent")) {
                this.dropLocation = SwingAccessor.getJTextComponentAccessor().dropLocationForPoint((JTextComponent)component, p);
            } else if (component instanceof JComponent) {
                this.dropLocation = ((JComponent)component).dropLocationForPoint(p);
            }
        }

        public boolean isDrop() {
            return this.isDrop;
        }

        public Component getComponent() {
            return this.component;
        }

        private void assureIsDrop() {
            if (!this.isDrop) {
                throw new IllegalStateException("Not a drop");
            }
        }

        public DropLocation getDropLocation() {
            this.assureIsDrop();
            if (this.dropLocation == null) {
                Point p = this.source instanceof DropTargetDragEvent ? ((DropTargetDragEvent)this.source).getLocation() : ((DropTargetDropEvent)this.source).getLocation();
                this.dropLocation = new DropLocation(p);
            }
            return this.dropLocation;
        }

        public void setShowDropLocation(boolean showDropLocation) {
            this.assureIsDrop();
            this.showDropLocation = showDropLocation;
            this.showDropLocationIsSet = true;
        }

        public void setDropAction(int dropAction) {
            this.assureIsDrop();
            int action = dropAction & this.getSourceDropActions();
            if (action != 1 && action != 2 && action != 0x40000000) {
                throw new IllegalArgumentException("unsupported drop action: " + dropAction);
            }
            this.dropAction = dropAction;
        }

        public int getDropAction() {
            return this.dropAction == -1 ? this.getUserDropAction() : this.dropAction;
        }

        public int getUserDropAction() {
            this.assureIsDrop();
            return this.source instanceof DropTargetDragEvent ? ((DropTargetDragEvent)this.source).getDropAction() : ((DropTargetDropEvent)this.source).getDropAction();
        }

        public int getSourceDropActions() {
            this.assureIsDrop();
            return this.source instanceof DropTargetDragEvent ? ((DropTargetDragEvent)this.source).getSourceActions() : ((DropTargetDropEvent)this.source).getSourceActions();
        }

        public DataFlavor[] getDataFlavors() {
            if (this.isDrop) {
                if (this.source instanceof DropTargetDragEvent) {
                    return ((DropTargetDragEvent)this.source).getCurrentDataFlavors();
                }
                return ((DropTargetDropEvent)this.source).getCurrentDataFlavors();
            }
            return ((Transferable)this.source).getTransferDataFlavors();
        }

        public boolean isDataFlavorSupported(DataFlavor df) {
            if (this.isDrop) {
                if (this.source instanceof DropTargetDragEvent) {
                    return ((DropTargetDragEvent)this.source).isDataFlavorSupported(df);
                }
                return ((DropTargetDropEvent)this.source).isDataFlavorSupported(df);
            }
            return ((Transferable)this.source).isDataFlavorSupported(df);
        }

        public Transferable getTransferable() {
            if (this.isDrop) {
                if (this.source instanceof DropTargetDragEvent) {
                    return ((DropTargetDragEvent)this.source).getTransferable();
                }
                return ((DropTargetDropEvent)this.source).getTransferable();
            }
            return (Transferable)this.source;
        }
    }

    static class PropertyTransferable
    implements Transferable {
        JComponent component;
        PropertyDescriptor property;

        PropertyTransferable(PropertyDescriptor p, JComponent c) {
            this.property = p;
            this.component = c;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            Class<?> propertyType = this.property.getPropertyType();
            String mimeType = "application/x-java-jvm-local-objectref;class=" + propertyType.getName();
            try {
                flavors[0] = new DataFlavor(mimeType);
            }
            catch (ClassNotFoundException cnfe) {
                flavors = new DataFlavor[]{};
            }
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            Class<?> propertyType = this.property.getPropertyType();
            return "application".equals(flavor.getPrimaryType()) && "x-java-jvm-local-objectref".equals(flavor.getSubType()) && flavor.getRepresentationClass().isAssignableFrom(propertyType);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!this.isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            Method reader = this.property.getReadMethod();
            Object value = null;
            try {
                value = MethodUtil.invoke(reader, this.component, null);
            }
            catch (Exception ex) {
                throw new IOException("Property read failed: " + this.property.getName());
            }
            return value;
        }
    }

    private static class DropHandler
    implements DropTargetListener,
    Serializable,
    ActionListener {
        private Timer timer;
        private Point lastPosition;
        private Rectangle outer = new Rectangle();
        private Rectangle inner = new Rectangle();
        private int hysteresis = 10;
        private Component component;
        private Object state;
        private TransferSupport support = new TransferSupport(null, (DropTargetEvent)null);
        private static final int AUTOSCROLL_INSET = 10;

        private DropHandler() {
        }

        private void updateAutoscrollRegion(JComponent c) {
            Rectangle visible = c.getVisibleRect();
            this.outer.setBounds(visible.x, visible.y, visible.width, visible.height);
            Insets i = new Insets(0, 0, 0, 0);
            if (c instanceof Scrollable) {
                int minSize = 20;
                if (visible.width >= minSize) {
                    i.right = 10;
                    i.left = 10;
                }
                if (visible.height >= minSize) {
                    i.bottom = 10;
                    i.top = 10;
                }
            }
            this.inner.setBounds(visible.x + i.left, visible.y + i.top, visible.width - (i.left + i.right), visible.height - (i.top + i.bottom));
        }

        private void autoscroll(JComponent c, Point pos) {
            if (c instanceof Scrollable) {
                Rectangle r;
                Scrollable s = (Scrollable)((Object)c);
                if (pos.y < this.inner.y) {
                    dy = s.getScrollableUnitIncrement(this.outer, 1, -1);
                    r = new Rectangle(this.inner.x, this.outer.y - dy, this.inner.width, dy);
                    c.scrollRectToVisible(r);
                } else if (pos.y > this.inner.y + this.inner.height) {
                    dy = s.getScrollableUnitIncrement(this.outer, 1, 1);
                    r = new Rectangle(this.inner.x, this.outer.y + this.outer.height, this.inner.width, dy);
                    c.scrollRectToVisible(r);
                }
                if (pos.x < this.inner.x) {
                    dx = s.getScrollableUnitIncrement(this.outer, 0, -1);
                    r = new Rectangle(this.outer.x - dx, this.inner.y, dx, this.inner.height);
                    c.scrollRectToVisible(r);
                } else if (pos.x > this.inner.x + this.inner.width) {
                    dx = s.getScrollableUnitIncrement(this.outer, 0, 1);
                    r = new Rectangle(this.outer.x + this.outer.width, this.inner.y, dx, this.inner.height);
                    c.scrollRectToVisible(r);
                }
            }
        }

        private void initPropertiesIfNecessary() {
            if (this.timer == null) {
                Toolkit t = Toolkit.getDefaultToolkit();
                Integer prop = (Integer)t.getDesktopProperty("DnD.Autoscroll.interval");
                this.timer = new Timer(prop == null ? 100 : prop, this);
                prop = (Integer)t.getDesktopProperty("DnD.Autoscroll.initialDelay");
                this.timer.setInitialDelay(prop == null ? 100 : prop);
                prop = (Integer)t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis");
                if (prop != null) {
                    this.hysteresis = prop;
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.updateAutoscrollRegion((JComponent)this.component);
            if (this.outer.contains(this.lastPosition) && !this.inner.contains(this.lastPosition)) {
                this.autoscroll((JComponent)this.component, this.lastPosition);
            }
        }

        private void setComponentDropLocation(TransferSupport support, boolean forDrop) {
            DropLocation dropLocation;
            DropLocation dropLocation2 = dropLocation = support == null ? null : support.getDropLocation();
            if (SunToolkit.isInstanceOf(this.component, "javax.swing.text.JTextComponent")) {
                this.state = SwingAccessor.getJTextComponentAccessor().setDropLocation((JTextComponent)this.component, dropLocation, this.state, forDrop);
            } else if (this.component instanceof JComponent) {
                this.state = ((JComponent)this.component).setDropLocation(dropLocation, this.state, forDrop);
            }
        }

        private void handleDrag(DropTargetDragEvent e) {
            TransferHandler importer = ((HasGetTransferHandler)((Object)this.component)).getTransferHandler();
            if (importer == null) {
                e.rejectDrag();
                this.setComponentDropLocation(null, false);
                return;
            }
            this.support.setDNDVariables(this.component, e);
            boolean canImport = importer.canImport(this.support);
            if (canImport) {
                e.acceptDrag(this.support.getDropAction());
            } else {
                e.rejectDrag();
            }
            boolean showLocation = this.support.showDropLocationIsSet ? this.support.showDropLocation : canImport;
            this.setComponentDropLocation(showLocation ? this.support : null, false);
        }

        @Override
        public void dragEnter(DropTargetDragEvent e) {
            this.state = null;
            this.component = e.getDropTargetContext().getComponent();
            this.handleDrag(e);
            if (this.component instanceof JComponent) {
                this.lastPosition = e.getLocation();
                this.updateAutoscrollRegion((JComponent)this.component);
                this.initPropertiesIfNecessary();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
            this.handleDrag(e);
            if (!(this.component instanceof JComponent)) {
                return;
            }
            Point p = e.getLocation();
            if (Math.abs(p.x - this.lastPosition.x) > this.hysteresis || Math.abs(p.y - this.lastPosition.y) > this.hysteresis) {
                if (this.timer.isRunning()) {
                    this.timer.stop();
                }
            } else if (!this.timer.isRunning()) {
                this.timer.start();
            }
            this.lastPosition = p;
        }

        @Override
        public void dragExit(DropTargetEvent e) {
            this.cleanup(false);
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            TransferHandler importer = ((HasGetTransferHandler)((Object)this.component)).getTransferHandler();
            if (importer == null) {
                e.rejectDrop();
                this.cleanup(false);
                return;
            }
            this.support.setDNDVariables(this.component, e);
            boolean canImport = importer.canImport(this.support);
            if (canImport) {
                boolean success;
                e.acceptDrop(this.support.getDropAction());
                boolean showLocation = this.support.showDropLocationIsSet ? this.support.showDropLocation : canImport;
                this.setComponentDropLocation(showLocation ? this.support : null, false);
                try {
                    success = importer.importData(this.support);
                }
                catch (RuntimeException re) {
                    success = false;
                }
                e.dropComplete(success);
                this.cleanup(success);
            } else {
                e.rejectDrop();
                this.cleanup(false);
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
            if (this.component == null) {
                return;
            }
            this.handleDrag(e);
        }

        private void cleanup(boolean forDrop) {
            this.setComponentDropLocation(null, forDrop);
            if (this.component instanceof JComponent) {
                ((JComponent)this.component).dndDone();
            }
            if (this.timer != null) {
                this.timer.stop();
            }
            this.state = null;
            this.component = null;
            this.lastPosition = null;
        }
    }

    static class TransferAction
    extends UIAction
    implements UIResource {
        private static final JavaSecurityAccess javaSecurityAccess = SharedSecrets.getJavaSecurityAccess();
        private static Object SandboxClipboardKey = new Object();

        TransferAction(String name) {
            super(name);
        }

        @Override
        public boolean accept(Object sender) {
            return !(sender instanceof JComponent) || ((JComponent)sender).getTransferHandler() != null;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Object src = e.getSource();
            final PrivilegedAction<Void> action = new PrivilegedAction<Void>(){
                final /* synthetic */ TransferAction this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Void run() {
                    this.this$0.actionPerformedImpl(e);
                    return null;
                }
            };
            AccessControlContext stack = AccessController.getContext();
            AccessControlContext srcAcc = AWTAccessor.getComponentAccessor().getAccessControlContext((Component)src);
            final AccessControlContext eventAcc = AWTAccessor.getAWTEventAccessor().getAccessControlContext(e);
            if (srcAcc == null) {
                javaSecurityAccess.doIntersectionPrivilege(action, stack, eventAcc);
            } else {
                javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){

                    @Override
                    public Void run() {
                        javaSecurityAccess.doIntersectionPrivilege(action, eventAcc);
                        return null;
                    }
                }, stack, srcAcc);
            }
        }

        private void actionPerformedImpl(ActionEvent e) {
            Object src = e.getSource();
            if (src instanceof JComponent) {
                JComponent c = (JComponent)src;
                TransferHandler th = c.getTransferHandler();
                Clipboard clipboard = this.getClipboard(c);
                String name = (String)this.getValue("Name");
                Transferable trans = null;
                try {
                    if (clipboard != null && th != null && name != null) {
                        if ("cut".equals(name)) {
                            th.exportToClipboard(c, clipboard, 2);
                        } else if ("copy".equals(name)) {
                            th.exportToClipboard(c, clipboard, 1);
                        } else if ("paste".equals(name)) {
                            trans = clipboard.getContents(null);
                        }
                    }
                }
                catch (IllegalStateException ise) {
                    UIManager.getLookAndFeel().provideErrorFeedback(c);
                    return;
                }
                if (trans != null) {
                    th.importData(new TransferSupport((Component)c, trans));
                }
            }
        }

        private Clipboard getClipboard(JComponent c) {
            if (SwingUtilities2.canAccessSystemClipboard()) {
                return c.getToolkit().getSystemClipboard();
            }
            Clipboard clipboard = (Clipboard)AppContext.getAppContext().get(SandboxClipboardKey);
            if (clipboard == null) {
                clipboard = new Clipboard("Sandboxed Component Clipboard");
                AppContext.getAppContext().put(SandboxClipboardKey, clipboard);
            }
            return clipboard;
        }
    }

    static class SwingDropTarget
    extends DropTarget
    implements UIResource {
        private EventListenerList listenerList;

        SwingDropTarget(Component c) {
            super(c, 0x40000003, null);
            try {
                super.addDropTargetListener(TransferHandler.getDropTargetListener());
            }
            catch (TooManyListenersException tooManyListenersException) {
                // empty catch block
            }
        }

        @Override
        public void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
            if (this.listenerList == null) {
                this.listenerList = new EventListenerList();
            }
            this.listenerList.add(DropTargetListener.class, dtl);
        }

        @Override
        public void removeDropTargetListener(DropTargetListener dtl) {
            if (this.listenerList != null) {
                this.listenerList.remove(DropTargetListener.class, dtl);
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent e) {
            super.dragEnter(e);
            if (this.listenerList != null) {
                Object[] listeners = this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != DropTargetListener.class) continue;
                    ((DropTargetListener)listeners[i + 1]).dragEnter(e);
                }
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
            super.dragOver(e);
            if (this.listenerList != null) {
                Object[] listeners = this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != DropTargetListener.class) continue;
                    ((DropTargetListener)listeners[i + 1]).dragOver(e);
                }
            }
        }

        @Override
        public void dragExit(DropTargetEvent e) {
            DropTargetListener dtListener;
            super.dragExit(e);
            if (this.listenerList != null) {
                Object[] listeners = this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != DropTargetListener.class) continue;
                    ((DropTargetListener)listeners[i + 1]).dragExit(e);
                }
            }
            if (!this.isActive() && (dtListener = TransferHandler.getDropTargetListener()) instanceof DropHandler) {
                DropHandler dropHandler = (DropHandler)dtListener;
                dropHandler.cleanup(false);
            }
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            super.drop(e);
            if (this.listenerList != null) {
                Object[] listeners = this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != DropTargetListener.class) continue;
                    ((DropTargetListener)listeners[i + 1]).drop(e);
                }
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
            super.dropActionChanged(e);
            if (this.listenerList != null) {
                Object[] listeners = this.listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != DropTargetListener.class) continue;
                    ((DropTargetListener)listeners[i + 1]).dropActionChanged(e);
                }
            }
        }
    }

    public static class DropLocation {
        private final Point dropPoint;

        protected DropLocation(Point dropPoint) {
            if (dropPoint == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            this.dropPoint = new Point(dropPoint);
        }

        public final Point getDropPoint() {
            return new Point(this.dropPoint);
        }

        public String toString() {
            return this.getClass().getName() + "[dropPoint=" + String.valueOf(this.dropPoint) + "]";
        }
    }

    static interface HasGetTransferHandler {
        public TransferHandler getTransferHandler();
    }
}

