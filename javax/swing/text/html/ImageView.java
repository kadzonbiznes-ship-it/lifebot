/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.StyleSheet;

public class ImageView
extends View {
    private static boolean sIsInc = false;
    private static int sIncRate = 100;
    private static final String PENDING_IMAGE = "html.pendingImage";
    private static final String MISSING_IMAGE = "html.missingImage";
    private static final String IMAGE_CACHE_PROPERTY = "imageCache";
    private static final int DEFAULT_WIDTH = 38;
    private static final int DEFAULT_HEIGHT = 38;
    private static final int DEFAULT_BORDER = 2;
    private static final int LOADING_FLAG = 1;
    private static final int LINK_FLAG = 2;
    private static final int WIDTH_FLAG = 4;
    private static final int HEIGHT_FLAG = 8;
    private static final int RELOAD_FLAG = 16;
    private static final int RELOAD_IMAGE_FLAG = 32;
    private static final int SYNC_LOAD_FLAG = 64;
    private AttributeSet attr;
    private Image image;
    private Image disabledImage;
    private int width;
    private int height;
    private int state = 48;
    private Container container;
    private Rectangle fBounds = new Rectangle();
    private Color borderColor;
    private short borderSize;
    private short leftInset;
    private short rightInset;
    private short topInset;
    private short bottomInset;
    private ImageObserver imageObserver = new ImageHandler();
    private View altView;
    private float vAlign;

    public ImageView(Element elem) {
        super(elem);
    }

    public String getAltText() {
        return (String)this.getElement().getAttributes().getAttribute(HTML.Attribute.ALT);
    }

    public URL getImageURL() {
        String src = (String)this.getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
        if (src == null) {
            return null;
        }
        URL reference = ((HTMLDocument)this.getDocument()).getBase();
        try {
            URL u = new URL(reference, src);
            return u;
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public Icon getNoImageIcon() {
        return (Icon)UIManager.getLookAndFeelDefaults().get(MISSING_IMAGE);
    }

    public Icon getLoadingImageIcon() {
        return (Icon)UIManager.getLookAndFeelDefaults().get(PENDING_IMAGE);
    }

    public Image getImage() {
        this.sync();
        return this.image;
    }

    private Image getImage(boolean enabled) {
        Image img = this.getImage();
        if (!enabled) {
            if (this.disabledImage == null) {
                this.disabledImage = GrayFilter.createDisabledImage(img);
            }
            img = this.disabledImage;
        }
        return img;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLoadsSynchronously(boolean newValue) {
        ImageView imageView = this;
        synchronized (imageView) {
            this.state = newValue ? (this.state |= 0x40) : (this.state | 0x40) ^ 0x40;
        }
    }

    public boolean getLoadsSynchronously() {
        return (this.state & 0x40) != 0;
    }

    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument)this.getDocument();
        return doc.getStyleSheet();
    }

    @Override
    public AttributeSet getAttributes() {
        this.sync();
        return this.attr;
    }

    @Override
    public String getToolTipText(float x, float y, Shape allocation) {
        return this.getAltText();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setPropertiesFromAttributes() {
        AttributeSet anchorAttr;
        StyleSheet sheet = this.getStyleSheet();
        this.attr = sheet.getViewAttributes(this);
        this.borderSize = (short)this.getIntAttr(HTML.Attribute.BORDER, this.isLink() ? 2 : 0);
        this.leftInset = this.rightInset = (short)(this.getIntAttr(HTML.Attribute.HSPACE, 0) + this.borderSize);
        this.topInset = this.bottomInset = (short)(this.getIntAttr(HTML.Attribute.VSPACE, 0) + this.borderSize);
        this.borderColor = ((StyledDocument)this.getDocument()).getForeground(this.getAttributes());
        AttributeSet attr = this.getElement().getAttributes();
        Object alignment = attr.getAttribute(HTML.Attribute.ALIGN);
        this.vAlign = 1.0f;
        if (alignment != null) {
            if ("top".equals(alignment = alignment.toString())) {
                this.vAlign = 0.0f;
            } else if ("middle".equals(alignment)) {
                this.vAlign = 0.5f;
            }
        }
        if ((anchorAttr = (AttributeSet)attr.getAttribute(HTML.Tag.A)) != null && anchorAttr.isDefined(HTML.Attribute.HREF)) {
            ImageView imageView = this;
            synchronized (imageView) {
                this.state |= 2;
            }
        }
        ImageView imageView = this;
        synchronized (imageView) {
            this.state = (this.state | 2) ^ 2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setParent(View parent) {
        View oldParent = this.getParent();
        super.setParent(parent);
        Container container = this.container = parent != null ? this.getContainer() : null;
        if (oldParent != parent) {
            ImageView imageView = this;
            synchronized (imageView) {
                this.state |= 0x10;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        ImageView imageView = this;
        synchronized (imageView) {
            this.state |= 0x30;
        }
        this.preferenceChanged(null, true, true);
    }

    @Override
    public void paint(Graphics g, Shape a) {
        Container host;
        Image img;
        this.sync();
        Rectangle rect = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        Rectangle clip = g.getClipBounds();
        this.fBounds.setBounds(rect);
        this.paintHighlights(g, a);
        this.paintBorder(g, rect);
        if (clip != null) {
            g.clipRect(rect.x + this.leftInset, rect.y + this.topInset, rect.width - this.leftInset - this.rightInset, rect.height - this.topInset - this.bottomInset);
        }
        if ((img = this.getImage((host = this.getContainer()) == null || host.isEnabled())) != null) {
            if (!this.hasPixels(img)) {
                Icon icon = this.getLoadingImageIcon();
                if (icon != null) {
                    icon.paintIcon(host, g, rect.x + this.leftInset, rect.y + this.topInset);
                }
            } else {
                g.drawImage(img, rect.x + this.leftInset, rect.y + this.topInset, this.width, this.height, this.imageObserver);
            }
        } else {
            View view;
            Icon icon = this.getNoImageIcon();
            if (icon != null) {
                icon.paintIcon(host, g, rect.x + this.leftInset, rect.y + this.topInset);
            }
            if ((view = this.getAltView()) != null && ((this.state & 4) == 0 || this.width > 38)) {
                Rectangle altRect = new Rectangle(rect.x + this.leftInset + 38, rect.y + this.topInset, rect.width - this.leftInset - this.rightInset - 38, rect.height - this.topInset - this.bottomInset);
                view.paint(g, altRect);
            }
        }
        if (clip != null) {
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }
    }

    private void paintHighlights(Graphics g, Shape shape) {
        JTextComponent tc;
        Highlighter h;
        if (this.container instanceof JTextComponent && (h = (tc = (JTextComponent)this.container).getHighlighter()) instanceof LayeredHighlighter) {
            ((LayeredHighlighter)h).paintLayeredHighlights(g, this.getStartOffset(), this.getEndOffset(), shape, tc, this);
        }
    }

    private void paintBorder(Graphics g, Rectangle rect) {
        Color color = this.borderColor;
        if ((this.borderSize > 0 || this.image == null) && color != null) {
            int xOffset = this.leftInset - this.borderSize;
            int yOffset = this.topInset - this.borderSize;
            g.setColor(color);
            int n = this.image == null ? 1 : this.borderSize;
            for (int counter = 0; counter < n; ++counter) {
                g.drawRect(rect.x + xOffset + counter, rect.y + yOffset + counter, rect.width - counter - counter - xOffset - xOffset - 1, rect.height - counter - counter - yOffset - yOffset - 1);
            }
        }
    }

    @Override
    public float getPreferredSpan(int axis) {
        this.sync();
        if (axis == 0 && (this.state & 4) == 4) {
            this.getPreferredSpanFromAltView(axis);
            return this.width + this.leftInset + this.rightInset;
        }
        if (axis == 1 && (this.state & 8) == 8) {
            this.getPreferredSpanFromAltView(axis);
            return this.height + this.topInset + this.bottomInset;
        }
        Image image = this.getImage();
        if (image != null) {
            switch (axis) {
                case 0: {
                    return this.width + this.leftInset + this.rightInset;
                }
                case 1: {
                    return this.height + this.topInset + this.bottomInset;
                }
            }
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        View view = this.getAltView();
        float retValue = 0.0f;
        if (view != null) {
            retValue = view.getPreferredSpan(axis);
        }
        switch (axis) {
            case 0: {
                return retValue + (float)(this.width + this.leftInset + this.rightInset);
            }
            case 1: {
                return retValue + (float)(this.height + this.topInset + this.bottomInset);
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public float getAlignment(int axis) {
        switch (axis) {
            case 1: {
                return this.vAlign;
            }
        }
        return super.getAlignment(axis);
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int p0 = this.getStartOffset();
        int p1 = this.getEndOffset();
        if (pos >= p0 && pos <= p1) {
            Rectangle r = a.getBounds();
            if (pos == p1) {
                r.x += r.width;
            }
            r.width = 0;
            return r;
        }
        return null;
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        Rectangle alloc = (Rectangle)a;
        if (x < (float)(alloc.x + alloc.width)) {
            bias[0] = Position.Bias.Forward;
            return this.getStartOffset();
        }
        bias[0] = Position.Bias.Backward;
        return this.getEndOffset();
    }

    @Override
    public void setSize(float width, float height) {
        View view;
        this.sync();
        if (this.getImage() == null && (view = this.getAltView()) != null) {
            view.setSize(Math.max(0.0f, width - (float)(38 + this.leftInset + this.rightInset)), Math.max(0.0f, height - (float)(this.topInset + this.bottomInset)));
        }
    }

    private boolean isLink() {
        return (this.state & 2) == 2;
    }

    private boolean hasPixels(Image image) {
        return image != null && image.getHeight(this.imageObserver) > 0 && image.getWidth(this.imageObserver) > 0;
    }

    private float getPreferredSpanFromAltView(int axis) {
        View view;
        if (this.getImage() == null && (view = this.getAltView()) != null) {
            return view.getPreferredSpan(axis);
        }
        return 0.0f;
    }

    private void repaint(long delay) {
        if (this.container != null && this.fBounds != null) {
            this.container.repaint(delay, this.fBounds.x, this.fBounds.y, this.fBounds.width, this.fBounds.height);
        }
    }

    private int getIntAttr(HTML.Attribute name, int deflt) {
        AttributeSet attr = this.getElement().getAttributes();
        if (attr.isDefined(name)) {
            int i;
            String val = (String)attr.getAttribute(name);
            if (val == null) {
                i = deflt;
            } else {
                try {
                    i = Math.max(0, Integer.parseInt(val));
                }
                catch (NumberFormatException x) {
                    i = deflt;
                }
            }
            return i;
        }
        return deflt;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sync() {
        int s = this.state;
        if ((s & 0x20) != 0) {
            this.refreshImage();
        }
        if (((s = this.state) & 0x10) != 0) {
            ImageView imageView = this;
            synchronized (imageView) {
                this.state = (this.state | 0x10) ^ 0x10;
            }
            this.setPropertiesFromAttributes();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void refreshImage() {
        ImageView imageView = this;
        synchronized (imageView) {
            this.state = (this.state | 1 | 0x20 | 4 | 8) ^ 0x2C;
            this.image = null;
            this.height = 0;
            this.width = 0;
        }
        try {
            this.loadImage();
            this.updateImageSize();
        }
        finally {
            imageView = this;
            synchronized (imageView) {
                this.state = (this.state | 1) ^ 1;
            }
        }
    }

    private void loadImage() {
        URL src = this.getImageURL();
        Image newImage = null;
        if (src != null) {
            Dictionary cache = (Dictionary)this.getDocument().getProperty(IMAGE_CACHE_PROPERTY);
            if (cache != null) {
                newImage = (Image)cache.get(src);
            } else {
                newImage = Toolkit.getDefaultToolkit().createImage(src);
                if (newImage != null && this.getLoadsSynchronously()) {
                    ImageIcon ii = new ImageIcon();
                    ii.setImage(newImage);
                }
            }
        }
        this.image = newImage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateImageSize() {
        int newWidth = 0;
        int newHeight = 0;
        int newState = 0;
        Image newImage = this.getImage();
        if (newImage != null) {
            Image img;
            Element elem = this.getElement();
            AttributeSet attr = elem.getAttributes();
            newWidth = this.getIntAttr(HTML.Attribute.WIDTH, -1);
            newHeight = this.getIntAttr(HTML.Attribute.HEIGHT, -1);
            if (newWidth > 0) {
                newState |= 4;
            }
            if (newHeight > 0) {
                newState |= 8;
            }
            ImageView imageView = this;
            synchronized (imageView) {
                img = this.image;
            }
            if (newWidth <= 0 && (newWidth = img.getWidth(this.imageObserver)) <= 0) {
                newWidth = 38;
            }
            if (newHeight <= 0 && (newHeight = img.getHeight(this.imageObserver)) <= 0) {
                newHeight = 38;
            }
            if (this.getLoadsSynchronously()) {
                Dimension d = this.adjustWidthHeight(newWidth, newHeight);
                newWidth = d.width;
                newHeight = d.height;
                newState |= 0xC;
            }
            if ((newState & 0xC) != 0) {
                Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth, newHeight, this.imageObserver);
            } else {
                Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1, this.imageObserver);
            }
            boolean createText = false;
            ImageView imageView2 = this;
            synchronized (imageView2) {
                if (this.image != null) {
                    if ((newState & 4) == 4 || this.width == 0) {
                        this.width = newWidth;
                    }
                    if ((newState & 8) == 8 || this.height == 0) {
                        this.height = newHeight;
                    }
                } else {
                    createText = true;
                    if ((newState & 4) == 4) {
                        this.width = newWidth;
                    }
                    if ((newState & 8) == 8) {
                        this.height = newHeight;
                    }
                }
                this.state |= newState;
                this.state = (this.state | 1) ^ 1;
            }
            if (createText) {
                this.updateAltTextView();
            }
        } else {
            this.height = 38;
            this.width = 38;
            this.updateAltTextView();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateAltTextView() {
        String text = this.getAltText();
        if (text != null) {
            ImageLabelView newView = new ImageLabelView(this.getElement(), text);
            ImageView imageView = this;
            synchronized (imageView) {
                this.altView = newView;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private View getAltView() {
        View view;
        ImageView imageView = this;
        synchronized (imageView) {
            view = this.altView;
        }
        if (view != null && view.getParent() == null) {
            view.setParent(this.getParent());
        }
        return view;
    }

    private void safePreferenceChanged() {
        if (SwingUtilities.isEventDispatchThread()) {
            Document doc = this.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            this.preferenceChanged(null, true, true);
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        } else {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    ImageView.this.safePreferenceChanged();
                }
            });
        }
    }

    private Dimension adjustWidthHeight(int newWidth, int newHeight) {
        Dimension d = new Dimension();
        double proportion = 0.0;
        int specifiedWidth = this.getIntAttr(HTML.Attribute.WIDTH, -1);
        int specifiedHeight = this.getIntAttr(HTML.Attribute.HEIGHT, -1);
        if (specifiedWidth != -1 && specifiedHeight != -1) {
            newWidth = specifiedWidth;
            newHeight = specifiedHeight;
        } else if (specifiedWidth != -1 ^ specifiedHeight != -1) {
            if (specifiedWidth <= 0) {
                proportion = (double)specifiedHeight / (double)newHeight;
                newWidth = (int)(proportion * (double)newWidth);
                newHeight = specifiedHeight;
            }
            if (specifiedHeight <= 0) {
                proportion = (double)specifiedWidth / (double)newWidth;
                newHeight = (int)(proportion * (double)newHeight);
                newWidth = specifiedWidth;
            }
        }
        d.width = newWidth;
        d.height = newHeight;
        return d;
    }

    private class ImageHandler
    implements ImageObserver {
        private ImageHandler() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean imageUpdate(Image img, int flags, int x, int y, int newWidth, int newHeight) {
            if (img != ImageView.this.image && img != ImageView.this.disabledImage || ImageView.this.image == null || ImageView.this.getParent() == null) {
                return false;
            }
            if ((flags & 0xC0) != 0) {
                ImageView.this.repaint(0L);
                ImageView imageView = ImageView.this;
                synchronized (imageView) {
                    if (ImageView.this.image == img) {
                        ImageView.this.image = null;
                        if ((ImageView.this.state & 4) != 4) {
                            ImageView.this.width = 38;
                        }
                        if ((ImageView.this.state & 8) != 8) {
                            ImageView.this.height = 38;
                        }
                    } else {
                        ImageView.this.disabledImage = null;
                    }
                    if ((ImageView.this.state & 1) == 1) {
                        return false;
                    }
                }
                ImageView.this.updateAltTextView();
                ImageView.this.safePreferenceChanged();
                return false;
            }
            if (ImageView.this.image == img) {
                short changed = 0;
                if ((flags & 2) != 0 && !ImageView.this.getElement().getAttributes().isDefined(HTML.Attribute.HEIGHT)) {
                    changed = (short)(changed | 1);
                }
                if ((flags & 1) != 0 && !ImageView.this.getElement().getAttributes().isDefined(HTML.Attribute.WIDTH)) {
                    changed = (short)(changed | 2);
                }
                if ((flags & 2) != 0 && (flags & 1) != 0) {
                    Dimension d = ImageView.this.adjustWidthHeight(newWidth, newHeight);
                    newWidth = d.width;
                    newHeight = d.height;
                    changed = (short)(changed | 3);
                }
                ImageView imageView = ImageView.this;
                synchronized (imageView) {
                    if ((changed & 1) == 1 && (ImageView.this.state & 8) == 0) {
                        ImageView.this.height = newHeight;
                    }
                    if ((changed & 2) == 2 && (ImageView.this.state & 4) == 0) {
                        ImageView.this.width = newWidth;
                    }
                    if ((ImageView.this.state & 1) == 1) {
                        return true;
                    }
                }
                if (changed != 0) {
                    ImageView.this.safePreferenceChanged();
                    return true;
                }
            }
            if ((flags & 0x30) != 0) {
                ImageView.this.repaint(0L);
            } else if ((flags & 8) != 0 && sIsInc) {
                ImageView.this.repaint(sIncRate);
            }
            return (flags & 0x20) == 0;
        }
    }

    private static class ImageLabelView
    extends InlineView {
        private Segment segment;
        private Color fg;

        ImageLabelView(Element e, String text) {
            super(e);
            this.reset(text);
        }

        public void reset(String text) {
            this.segment = new Segment(text.toCharArray(), 0, text.length());
        }

        @Override
        public void paint(Graphics g, Shape a) {
            GlyphView.GlyphPainter painter = this.getGlyphPainter();
            if (painter != null) {
                g.setColor(this.getForeground());
                painter.paint(this, g, a, this.getStartOffset(), this.getEndOffset());
            }
        }

        @Override
        public Segment getText(int p0, int p1) {
            if (p0 < 0 || p1 > this.segment.array.length) {
                throw new RuntimeException("ImageLabelView: Stale view");
            }
            this.segment.offset = p0;
            this.segment.count = p1 - p0;
            return this.segment;
        }

        @Override
        public int getStartOffset() {
            return 0;
        }

        @Override
        public int getEndOffset() {
            return this.segment.array.length;
        }

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            return this;
        }

        @Override
        public Color getForeground() {
            View parent;
            if (this.fg == null && (parent = this.getParent()) != null) {
                Document doc = this.getDocument();
                AttributeSet attr = parent.getAttributes();
                if (attr != null && doc instanceof StyledDocument) {
                    this.fg = ((StyledDocument)doc).getForeground(attr);
                }
            }
            return this.fg;
        }
    }
}

