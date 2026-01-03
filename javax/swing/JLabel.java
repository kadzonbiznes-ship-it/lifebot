/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.BreakIterator;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleExtendedComponent;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleKeyBinding;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.LabelUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

@JavaBean(defaultProperty="UI", description="A component that displays a short string and an icon.")
@SwingContainer(value=false)
public class JLabel
extends JComponent
implements SwingConstants,
Accessible {
    private static final String uiClassID = "LabelUI";
    private int mnemonic = 0;
    private int mnemonicIndex = -1;
    private String text = "";
    private Icon defaultIcon = null;
    private Icon disabledIcon = null;
    private boolean disabledIconSet = false;
    private int verticalAlignment = 0;
    private int horizontalAlignment = 10;
    private int verticalTextPosition = 0;
    private int horizontalTextPosition = 11;
    private int iconTextGap = 4;
    protected Component labelFor = null;
    static final String LABELED_BY_PROPERTY = "labeledBy";

    public JLabel(String text, Icon icon, int horizontalAlignment) {
        this.setText(text);
        this.setIcon(icon);
        this.setHorizontalAlignment(horizontalAlignment);
        this.updateUI();
        this.setAlignmentX(0.0f);
    }

    public JLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public JLabel(String text) {
        this(text, null, 10);
    }

    public JLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
    }

    public JLabel(Icon image) {
        this(null, image, 0);
    }

    public JLabel() {
        this("", null, 10);
    }

    @Override
    public LabelUI getUI() {
        return (LabelUI)this.ui;
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(LabelUI ui) {
        super.setUI(ui);
        if (!this.disabledIconSet && this.disabledIcon != null) {
            this.setDisabledIcon(null);
        }
    }

    @Override
    public void updateUI() {
        this.setUI((LabelUI)UIManager.getUI(this));
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    public String getText() {
        return this.text;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="Defines the single line of text this component will display.")
    public void setText(String text) {
        String oldAccessibleName = null;
        if (this.accessibleContext != null) {
            oldAccessibleName = this.accessibleContext.getAccessibleName();
        }
        String oldValue = this.text;
        this.text = text;
        this.firePropertyChange("text", oldValue, text);
        this.setDisplayedMnemonicIndex(SwingUtilities.findDisplayedMnemonicIndex(text, this.getDisplayedMnemonic()));
        if (this.accessibleContext != null && this.accessibleContext.getAccessibleName() != oldAccessibleName) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldAccessibleName, this.accessibleContext.getAccessibleName());
        }
        if (text == null || oldValue == null || !text.equals(oldValue)) {
            this.revalidate();
            this.repaint();
        }
    }

    public Icon getIcon() {
        return this.defaultIcon;
    }

    @BeanProperty(preferred=true, visualUpdate=true, description="The icon this component will display.")
    public void setIcon(Icon icon) {
        Icon oldValue = this.defaultIcon;
        this.defaultIcon = icon;
        if (this.defaultIcon != oldValue && !this.disabledIconSet) {
            this.disabledIcon = null;
        }
        this.firePropertyChange("icon", oldValue, this.defaultIcon);
        if (this.accessibleContext != null && oldValue != this.defaultIcon) {
            this.accessibleContext.firePropertyChange("AccessibleVisibleData", oldValue, this.defaultIcon);
        }
        if (this.defaultIcon != oldValue) {
            if (this.defaultIcon == null || oldValue == null || this.defaultIcon.getIconWidth() != oldValue.getIconWidth() || this.defaultIcon.getIconHeight() != oldValue.getIconHeight()) {
                this.revalidate();
            }
            this.repaint();
        }
    }

    @Transient
    public Icon getDisabledIcon() {
        if (!this.disabledIconSet && this.disabledIcon == null && this.defaultIcon != null) {
            this.disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, this.defaultIcon);
            if (this.disabledIcon != null) {
                this.firePropertyChange("disabledIcon", null, this.disabledIcon);
            }
        }
        return this.disabledIcon;
    }

    @BeanProperty(visualUpdate=true, description="The icon to display if the label is disabled.")
    public void setDisabledIcon(Icon disabledIcon) {
        Icon oldValue = this.disabledIcon;
        this.disabledIcon = disabledIcon;
        this.disabledIconSet = disabledIcon != null;
        this.firePropertyChange("disabledIcon", oldValue, disabledIcon);
        if (disabledIcon != oldValue) {
            if (disabledIcon == null || oldValue == null || disabledIcon.getIconWidth() != oldValue.getIconWidth() || disabledIcon.getIconHeight() != oldValue.getIconHeight()) {
                this.revalidate();
            }
            if (!this.isEnabled()) {
                this.repaint();
            }
        }
    }

    @BeanProperty(visualUpdate=true, description="The mnemonic keycode.")
    public void setDisplayedMnemonic(int key) {
        int oldKey = this.mnemonic;
        this.mnemonic = key;
        this.firePropertyChange("displayedMnemonic", oldKey, this.mnemonic);
        this.setDisplayedMnemonicIndex(SwingUtilities.findDisplayedMnemonicIndex(this.getText(), this.mnemonic));
        if (key != oldKey) {
            this.revalidate();
            this.repaint();
        }
    }

    public void setDisplayedMnemonic(char aChar) {
        int vk = KeyEvent.getExtendedKeyCodeForChar(aChar);
        if (vk != 0) {
            this.setDisplayedMnemonic(vk);
        }
    }

    public int getDisplayedMnemonic() {
        return this.mnemonic;
    }

    @BeanProperty(visualUpdate=true, description="the index into the String to draw the keyboard character mnemonic at")
    public void setDisplayedMnemonicIndex(int index) throws IllegalArgumentException {
        int oldValue = this.mnemonicIndex;
        if (index == -1) {
            this.mnemonicIndex = -1;
        } else {
            int textLength;
            String text = this.getText();
            int n = textLength = text == null ? 0 : text.length();
            if (index < -1 || index >= textLength) {
                throw new IllegalArgumentException("index == " + index);
            }
        }
        this.mnemonicIndex = index;
        this.firePropertyChange("displayedMnemonicIndex", oldValue, index);
        if (index != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    public int getDisplayedMnemonicIndex() {
        return this.mnemonicIndex;
    }

    protected int checkHorizontalKey(int key, String message) {
        if (key == 2 || key == 0 || key == 4 || key == 10 || key == 11) {
            return key;
        }
        throw new IllegalArgumentException(message);
    }

    protected int checkVerticalKey(int key, String message) {
        if (key == 1 || key == 0 || key == 3) {
            return key;
        }
        throw new IllegalArgumentException(message);
    }

    public int getIconTextGap() {
        return this.iconTextGap;
    }

    @BeanProperty(visualUpdate=true, description="If both the icon and text properties are set, this property defines the space between them.")
    public void setIconTextGap(int iconTextGap) {
        int oldValue = this.iconTextGap;
        this.iconTextGap = iconTextGap;
        this.firePropertyChange("iconTextGap", oldValue, iconTextGap);
        if (iconTextGap != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.TOP", "SwingConstants.CENTER", "SwingConstants.BOTTOM"}, description="The alignment of the label's contents along the Y axis.")
    public void setVerticalAlignment(int alignment) {
        if (alignment == this.verticalAlignment) {
            return;
        }
        int oldValue = this.verticalAlignment;
        this.verticalAlignment = this.checkVerticalKey(alignment, "verticalAlignment");
        this.firePropertyChange("verticalAlignment", oldValue, this.verticalAlignment);
        this.repaint();
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    @BeanProperty(visualUpdate=true, enumerationValues={"SwingConstants.LEFT", "SwingConstants.CENTER", "SwingConstants.RIGHT", "SwingConstants.LEADING", "SwingConstants.TRAILING"}, description="The alignment of the label's content along the X axis.")
    public void setHorizontalAlignment(int alignment) {
        if (alignment == this.horizontalAlignment) {
            return;
        }
        int oldValue = this.horizontalAlignment;
        this.horizontalAlignment = this.checkHorizontalKey(alignment, "horizontalAlignment");
        this.firePropertyChange("horizontalAlignment", oldValue, this.horizontalAlignment);
        this.repaint();
    }

    public int getVerticalTextPosition() {
        return this.verticalTextPosition;
    }

    @BeanProperty(expert=true, visualUpdate=true, enumerationValues={"SwingConstants.TOP", "SwingConstants.CENTER", "SwingConstants.BOTTOM"}, description="The vertical position of the text relative to it's image.")
    public void setVerticalTextPosition(int textPosition) {
        if (textPosition == this.verticalTextPosition) {
            return;
        }
        int old = this.verticalTextPosition;
        this.verticalTextPosition = this.checkVerticalKey(textPosition, "verticalTextPosition");
        this.firePropertyChange("verticalTextPosition", old, this.verticalTextPosition);
        this.revalidate();
        this.repaint();
    }

    public int getHorizontalTextPosition() {
        return this.horizontalTextPosition;
    }

    @BeanProperty(expert=true, visualUpdate=true, enumerationValues={"SwingConstants.LEFT", "SwingConstants.CENTER", "SwingConstants.RIGHT", "SwingConstants.LEADING", "SwingConstants.TRAILING"}, description="The horizontal position of the label's text, relative to its image.")
    public void setHorizontalTextPosition(int textPosition) {
        int old = this.horizontalTextPosition;
        this.horizontalTextPosition = this.checkHorizontalKey(textPosition, "horizontalTextPosition");
        this.firePropertyChange("horizontalTextPosition", old, this.horizontalTextPosition);
        this.revalidate();
        this.repaint();
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (!this.isShowing() || !SwingUtilities.doesIconReferenceImage(this.getIcon(), img) && !SwingUtilities.doesIconReferenceImage(this.disabledIcon, img)) {
            return false;
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
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
        String labelForString;
        String textString = this.text != null ? this.text : "";
        String defaultIconString = this.defaultIcon != null && this.defaultIcon != this ? this.defaultIcon.toString() : "";
        String disabledIconString = this.disabledIcon != null && this.disabledIcon != this ? this.disabledIcon.toString() : "";
        String string = labelForString = this.labelFor != null ? this.labelFor.toString() : "";
        String verticalAlignmentString = this.verticalAlignment == 1 ? "TOP" : (this.verticalAlignment == 0 ? "CENTER" : (this.verticalAlignment == 3 ? "BOTTOM" : ""));
        String horizontalAlignmentString = this.horizontalAlignment == 2 ? "LEFT" : (this.horizontalAlignment == 0 ? "CENTER" : (this.horizontalAlignment == 4 ? "RIGHT" : (this.horizontalAlignment == 10 ? "LEADING" : (this.horizontalAlignment == 11 ? "TRAILING" : ""))));
        String verticalTextPositionString = this.verticalTextPosition == 1 ? "TOP" : (this.verticalTextPosition == 0 ? "CENTER" : (this.verticalTextPosition == 3 ? "BOTTOM" : ""));
        String horizontalTextPositionString = this.horizontalTextPosition == 2 ? "LEFT" : (this.horizontalTextPosition == 0 ? "CENTER" : (this.horizontalTextPosition == 4 ? "RIGHT" : (this.horizontalTextPosition == 10 ? "LEADING" : (this.horizontalTextPosition == 11 ? "TRAILING" : ""))));
        return super.paramString() + ",defaultIcon=" + defaultIconString + ",disabledIcon=" + disabledIconString + ",horizontalAlignment=" + horizontalAlignmentString + ",horizontalTextPosition=" + horizontalTextPositionString + ",iconTextGap=" + this.iconTextGap + ",labelFor=" + labelForString + ",text=" + textString + ",verticalAlignment=" + verticalAlignmentString + ",verticalTextPosition=" + verticalTextPositionString;
    }

    public Component getLabelFor() {
        return this.labelFor;
    }

    @BeanProperty(description="The component this is labelling.")
    public void setLabelFor(Component c) {
        Component oldC = this.labelFor;
        this.labelFor = c;
        this.firePropertyChange("labelFor", oldC, c);
        if (oldC instanceof JComponent) {
            ((JComponent)oldC).putClientProperty(LABELED_BY_PROPERTY, null);
        }
        if (c instanceof JComponent) {
            ((JComponent)c).putClientProperty(LABELED_BY_PROPERTY, this);
        }
    }

    @Override
    @BeanProperty(bound=false, expert=true, description="The AccessibleContext associated with this Label.")
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJLabel();
        }
        return this.accessibleContext;
    }

    protected class AccessibleJLabel
    extends JComponent.AccessibleJComponent
    implements AccessibleText,
    AccessibleExtendedComponent {
        protected AccessibleJLabel() {
        }

        @Override
        public String getAccessibleName() {
            return this.getAccessibleNameCheckIcon(this.getAccessibleNameImpl());
        }

        private String getAccessibleNameImpl() {
            String name = this.accessibleName;
            if (name == null) {
                name = (String)JLabel.this.getClientProperty("AccessibleName");
            }
            if (name == null) {
                name = JLabel.this.getText();
            }
            if (name == null) {
                name = super.getAccessibleName();
            }
            return name;
        }

        private String getAccessibleNameCheckIcon(String name) {
            AccessibleContext ac;
            if ((name == null || name.isEmpty()) && JLabel.this.getIcon() != null && JLabel.this.getIcon() instanceof Accessible && (ac = ((Accessible)((Object)JLabel.this.getIcon())).getAccessibleContext()) != null) {
                name = ac.getAccessibleName();
            }
            return name;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            String name = this.getAccessibleNameImpl();
            if ((name == null || name.isEmpty()) && JLabel.this.getIcon() != null) {
                return AccessibleRole.ICON;
            }
            return AccessibleRole.LABEL;
        }

        @Override
        public AccessibleIcon[] getAccessibleIcon() {
            AccessibleContext ac;
            Icon icon = JLabel.this.getIcon();
            if (icon instanceof Accessible && (ac = ((Accessible)((Object)icon)).getAccessibleContext()) instanceof AccessibleIcon) {
                AccessibleIcon ai = (AccessibleIcon)((Object)ac);
                return new AccessibleIcon[]{ai};
            }
            return null;
        }

        @Override
        public AccessibleRelationSet getAccessibleRelationSet() {
            Component c;
            AccessibleRelationSet relationSet = super.getAccessibleRelationSet();
            if (!relationSet.contains(AccessibleRelation.LABEL_FOR) && (c = JLabel.this.getLabelFor()) != null) {
                AccessibleRelation relation = new AccessibleRelation(AccessibleRelation.LABEL_FOR);
                relation.setTarget(c);
                relationSet.add(relation);
            }
            return relationSet;
        }

        @Override
        public AccessibleText getAccessibleText() {
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null) {
                return this;
            }
            return null;
        }

        @Override
        public int getIndexAtPoint(Point p) {
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = this.getTextRectangle();
                if (r == null) {
                    return -1;
                }
                Rectangle2D.Float shape = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                Position.Bias[] bias = new Position.Bias[1];
                return view.viewToModel(p.x, p.y, shape, bias);
            }
            return -1;
        }

        @Override
        public Rectangle getCharacterBounds(int i) {
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null) {
                Rectangle r = this.getTextRectangle();
                if (r == null) {
                    return null;
                }
                Rectangle2D.Float shape = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
                try {
                    Shape charShape = view.modelToView(i, shape, Position.Bias.Forward);
                    return charShape.getBounds();
                }
                catch (BadLocationException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public int getCharCount() {
            Document d;
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)d;
                return doc.getLength();
            }
            return JLabel.this.accessibleContext.getAccessibleName().length();
        }

        @Override
        public int getCaretPosition() {
            return -1;
        }

        @Override
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= this.getCharCount()) {
                return null;
            }
            switch (part) {
                case 1: {
                    try {
                        return this.getText(index, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int end = words.following(index);
                        return s.substring(words.previous(), end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int end = sentence.following(index);
                        return s.substring(sentence.previous(), end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= this.getCharCount()) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index + 1 >= this.getCharCount()) {
                        return null;
                    }
                    try {
                        return this.getText(index + 1, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int start = words.following(index);
                        if (start == -1 || start >= s.length()) {
                            return null;
                        }
                        int end = words.following(start);
                        if (end == -1 || end >= s.length()) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int start = sentence.following(index);
                        if (start == -1 || start > s.length()) {
                            return null;
                        }
                        int end = sentence.following(start);
                        if (end == -1 || end > s.length()) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > this.getCharCount() - 1) {
                return null;
            }
            switch (part) {
                case 1: {
                    if (index == 0) {
                        return null;
                    }
                    try {
                        return this.getText(index - 1, 1);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 2: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator words = BreakIterator.getWordInstance(this.getLocale());
                        words.setText(s);
                        int end = words.following(index);
                        end = words.previous();
                        int start = words.previous();
                        if (start == -1) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
                case 3: {
                    try {
                        String s = this.getText(0, this.getCharCount());
                        BreakIterator sentence = BreakIterator.getSentenceInstance(this.getLocale());
                        sentence.setText(s);
                        int end = sentence.following(index);
                        end = sentence.previous();
                        int start = sentence.previous();
                        if (start == -1) {
                            return null;
                        }
                        return s.substring(start, end);
                    }
                    catch (BadLocationException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public AttributeSet getCharacterAttribute(int i) {
            StyledDocument doc;
            Element elem;
            Document d;
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument && (elem = (doc = (StyledDocument)d).getCharacterElement(i)) != null) {
                return elem.getAttributes();
            }
            return null;
        }

        @Override
        public int getSelectionStart() {
            return -1;
        }

        @Override
        public int getSelectionEnd() {
            return -1;
        }

        @Override
        public String getSelectedText() {
            return null;
        }

        private String getText(int offset, int length) throws BadLocationException {
            Document d;
            View view = (View)JLabel.this.getClientProperty("html");
            if (view != null && (d = view.getDocument()) instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)d;
                return doc.getText(offset, length);
            }
            return null;
        }

        private Rectangle getTextRectangle() {
            Icon icon;
            String text = JLabel.this.getText();
            Icon icon2 = icon = JLabel.this.isEnabled() ? JLabel.this.getIcon() : JLabel.this.getDisabledIcon();
            if (icon == null && text == null) {
                return null;
            }
            Rectangle paintIconR = new Rectangle();
            Rectangle paintTextR = new Rectangle();
            Rectangle paintViewR = new Rectangle();
            Insets paintViewInsets = new Insets(0, 0, 0, 0);
            paintViewInsets = JLabel.this.getInsets(paintViewInsets);
            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;
            paintViewR.width = JLabel.this.getWidth() - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.height = JLabel.this.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
            String clippedText = SwingUtilities.layoutCompoundLabel(JLabel.this, this.getFontMetrics(this.getFont()), text, icon, JLabel.this.getVerticalAlignment(), JLabel.this.getHorizontalAlignment(), JLabel.this.getVerticalTextPosition(), JLabel.this.getHorizontalTextPosition(), paintViewR, paintIconR, paintTextR, JLabel.this.getIconTextGap());
            return paintTextR;
        }

        @Override
        AccessibleExtendedComponent getAccessibleExtendedComponent() {
            return this;
        }

        @Override
        public String getToolTipText() {
            return JLabel.this.getToolTipText();
        }

        @Override
        public String getTitledBorderText() {
            return super.getTitledBorderText();
        }

        @Override
        public AccessibleKeyBinding getAccessibleKeyBinding() {
            int mnemonic = JLabel.this.getDisplayedMnemonic();
            if (mnemonic == 0) {
                return null;
            }
            return new LabelKeyBinding(this, mnemonic);
        }

        class LabelKeyBinding
        implements AccessibleKeyBinding {
            int mnemonic;

            LabelKeyBinding(AccessibleJLabel this$1, int mnemonic) {
                this.mnemonic = mnemonic;
            }

            @Override
            public int getAccessibleKeyBindingCount() {
                return 1;
            }

            @Override
            public Object getAccessibleKeyBinding(int i) {
                if (i != 0) {
                    throw new IllegalArgumentException();
                }
                return KeyStroke.getKeyStroke(this.mnemonic, 0);
            }
        }
    }
}

