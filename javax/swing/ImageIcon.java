/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.beans.BeanProperty;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.Icon;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;

public class ImageIcon
implements Icon,
Serializable,
Accessible {
    private transient String filename;
    private transient URL location;
    transient Image image;
    transient int loadStatus = 0;
    ImageObserver imageObserver;
    String description = null;
    @Deprecated
    protected static final Component component = AccessController.doPrivileged(new PrivilegedAction<Component>(){

        @Override
        public Component run() {
            try {
                Component component = ImageIcon.createNoPermsComponent();
                AWTAccessor.getComponentAccessor().setAppContext(component, null);
                return component;
            }
            catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    });
    @Deprecated
    protected static final MediaTracker tracker = new MediaTracker(component);
    private static int mediaTrackerID;
    private static final Object TRACKER_KEY;
    int width = -1;
    int height = -1;
    private AccessibleImageIcon accessibleContext = null;

    private static Component createNoPermsComponent() {
        return AccessController.doPrivileged(new PrivilegedAction<Component>(){

            @Override
            public Component run() {
                return new Component(){};
            }
        }, new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, null)}));
    }

    public ImageIcon(String filename, String description) {
        this.image = Toolkit.getDefaultToolkit().getImage(filename);
        if (this.image == null) {
            return;
        }
        this.filename = filename;
        this.description = description;
        this.loadImage(this.image);
    }

    @ConstructorProperties(value={"description"})
    public ImageIcon(String filename) {
        this(filename, filename);
    }

    public ImageIcon(URL location, String description) {
        this.image = Toolkit.getDefaultToolkit().getImage(location);
        if (this.image == null) {
            return;
        }
        this.location = location;
        this.description = description;
        this.loadImage(this.image);
    }

    public ImageIcon(URL location) {
        this(location, location.toExternalForm());
    }

    public ImageIcon(Image image, String description) {
        this(image);
        this.description = description;
    }

    public ImageIcon(Image image) {
        this.image = image;
        Object o = image.getProperty("comment", this.imageObserver);
        if (o instanceof String) {
            this.description = (String)o;
        }
        this.loadImage(image);
    }

    public ImageIcon(byte[] imageData, String description) {
        this.image = Toolkit.getDefaultToolkit().createImage(imageData);
        if (this.image == null) {
            return;
        }
        this.description = description;
        this.loadImage(this.image);
    }

    public ImageIcon(byte[] imageData) {
        this.image = Toolkit.getDefaultToolkit().createImage(imageData);
        if (this.image == null) {
            return;
        }
        Object o = this.image.getProperty("comment", this.imageObserver);
        if (o instanceof String) {
            this.description = (String)o;
        }
        this.loadImage(this.image);
    }

    public ImageIcon() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void loadImage(Image image) {
        MediaTracker mTracker = this.getTracker();
        boolean interrupted = false;
        MediaTracker mediaTracker = mTracker;
        synchronized (mediaTracker) {
            int id = this.getNextID();
            mTracker.addImage(image, id);
            try {
                mTracker.waitForID(id, 0L);
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            this.loadStatus = mTracker.statusID(id, false);
            mTracker.removeImage(image, id);
            if (interrupted && (this.loadStatus & 1) != 0) {
                this.loadStatus = 2;
            }
            this.width = image.getWidth(this.imageObserver);
            this.height = image.getHeight(this.imageObserver);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getNextID() {
        MediaTracker mediaTracker = this.getTracker();
        synchronized (mediaTracker) {
            return ++mediaTrackerID;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private MediaTracker getTracker() {
        Object trackerObj;
        AppContext ac;
        AppContext appContext = ac = AppContext.getAppContext();
        synchronized (appContext) {
            trackerObj = ac.get(TRACKER_KEY);
            if (trackerObj == null) {
                Component comp = new Component(){};
                trackerObj = new MediaTracker(comp);
                ac.put(TRACKER_KEY, trackerObj);
            }
        }
        return (MediaTracker)trackerObj;
    }

    public int getImageLoadStatus() {
        return this.loadStatus;
    }

    @Transient
    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
        this.loadImage(image);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if (this.imageObserver == null) {
            g.drawImage(this.image, x, y, c);
        } else {
            g.drawImage(this.image, x, y, this.imageObserver);
        }
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    public void setImageObserver(ImageObserver observer) {
        this.imageObserver = observer;
    }

    @Transient
    public ImageObserver getImageObserver() {
        return this.imageObserver;
    }

    public String toString() {
        if (this.description != null) {
            return this.description;
        }
        return super.toString();
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        this.imageObserver = (ImageObserver)f.get("imageObserver", null);
        this.description = (String)f.get("description", null);
        this.width = f.get("width", -1);
        this.height = f.get("height", -1);
        this.accessibleContext = (AccessibleImageIcon)f.get("accessibleContext", null);
        int w = s.readInt();
        int h = s.readInt();
        int[] pixels = (int[])s.readObject();
        if (pixels == null && (w != -1 || h != -1)) {
            throw new IllegalStateException("Inconsistent width and height for null image [" + w + ", " + h + "]");
        }
        if (pixels != null && (w < 0 || h < 0)) {
            throw new IllegalStateException("Inconsistent width and height for image [" + w + ", " + h + "]");
        }
        if (w != this.getIconWidth() || h != this.getIconHeight()) {
            throw new IllegalStateException("Inconsistent width and height for image [" + w + ", " + h + "]");
        }
        if (pixels != null) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            ColorModel cm = ColorModel.getRGBdefault();
            this.image = tk.createImage(new MemoryImageSource(w, h, cm, pixels, 0, w));
            this.loadImage(this.image);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        int[] pixels;
        s.defaultWriteObject();
        int w = this.getIconWidth();
        int h = this.getIconHeight();
        int[] nArray = pixels = this.image != null ? new int[w * h] : null;
        if (this.image != null) {
            try {
                PixelGrabber pg = new PixelGrabber(this.image, 0, 0, w, h, pixels, 0, w);
                pg.grabPixels();
                if ((pg.getStatus() & 0x80) != 0) {
                    throw new IOException("failed to load image contents");
                }
            }
            catch (InterruptedException e) {
                throw new IOException("image load interrupted");
            }
        }
        s.writeInt(w);
        s.writeInt(h);
        s.writeObject(pixels);
    }

    @Override
    @BeanProperty(expert=true, description="The AccessibleContext associated with this ImageIcon.")
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleImageIcon();
        }
        return this.accessibleContext;
    }

    static {
        TRACKER_KEY = new StringBuilder("TRACKER_KEY");
    }

    protected class AccessibleImageIcon
    extends AccessibleContext
    implements AccessibleIcon,
    Serializable {
        protected AccessibleImageIcon() {
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.ICON;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            return null;
        }

        @Override
        public Accessible getAccessibleParent() {
            return null;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return -1;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public Locale getLocale() throws IllegalComponentStateException {
            return null;
        }

        @Override
        public String getAccessibleIconDescription() {
            return ImageIcon.this.getDescription();
        }

        @Override
        public void setAccessibleIconDescription(String description) {
            ImageIcon.this.setDescription(description);
        }

        @Override
        public int getAccessibleIconHeight() {
            return ImageIcon.this.height;
        }

        @Override
        public int getAccessibleIconWidth() {
            return ImageIcon.this.width;
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
        }
    }
}

