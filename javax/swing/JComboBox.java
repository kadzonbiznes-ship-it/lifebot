/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.swing.JComboBox$AccessibleJComboBox.EditorAccessibleContext
 */
package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleEditableText;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleTable;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionPropertyChangeListener;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

@JavaBean(defaultProperty="UI", description="A combination of a text field and a drop-down list.")
@SwingContainer(value=false)
public class JComboBox<E>
extends JComponent
implements ItemSelectable,
ListDataListener,
ActionListener,
Accessible {
    private static final String uiClassID = "ComboBoxUI";
    protected ComboBoxModel<E> dataModel;
    protected ListCellRenderer<? super E> renderer;
    protected ComboBoxEditor editor;
    protected int maximumRowCount = 8;
    protected boolean isEditable = false;
    protected KeySelectionManager keySelectionManager = null;
    protected String actionCommand = "comboBoxChanged";
    protected boolean lightWeightPopupEnabled = JPopupMenu.getDefaultLightWeightPopupEnabled();
    protected Object selectedItemReminder = null;
    private E prototypeDisplayValue;
    private boolean firingActionEvent = false;
    private boolean selectingItem = false;
    private transient boolean updateInProgress;
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;

    public JComboBox(ComboBoxModel<E> aModel) {
        this.setModel(aModel);
        this.init();
    }

    public JComboBox(E[] items) {
        this.setModel(new DefaultComboBoxModel<E>(items));
        this.init();
    }

    public JComboBox(Vector<E> items) {
        this.setModel(new DefaultComboBoxModel<E>(items));
        this.init();
    }

    public JComboBox() {
        this.setModel(new DefaultComboBoxModel());
        this.init();
    }

    private void init() {
        this.installAncestorListener();
        this.setUIProperty("opaque", true);
        this.updateUI();
    }

    protected void installAncestorListener() {
        this.addAncestorListener(new AncestorListener(){

            @Override
            public void ancestorAdded(AncestorEvent event) {
                JComboBox.this.hidePopup();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                JComboBox.this.hidePopup();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                if (event.getSource() != JComboBox.this) {
                    JComboBox.this.hidePopup();
                }
            }
        });
    }

    @BeanProperty(hidden=true, visualUpdate=true, description="The UI object that implements the Component's LookAndFeel.")
    public void setUI(ComboBoxUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        if (!this.updateInProgress) {
            this.updateInProgress = true;
            try {
                this.setUI((ComboBoxUI)UIManager.getUI(this));
                ListCellRenderer<E> renderer = this.getRenderer();
                if (renderer instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component)((Object)renderer));
                }
            }
            finally {
                this.updateInProgress = false;
            }
        }
    }

    @Override
    @BeanProperty(bound=false)
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public ComboBoxUI getUI() {
        return (ComboBoxUI)this.ui;
    }

    @BeanProperty(description="Model that the combo box uses to get data to display.")
    public void setModel(ComboBoxModel<E> aModel) {
        ComboBoxModel<E> oldModel = this.dataModel;
        if (oldModel != null) {
            oldModel.removeListDataListener(this);
        }
        this.dataModel = aModel;
        this.dataModel.addListDataListener(this);
        this.selectedItemReminder = this.dataModel.getSelectedItem();
        this.firePropertyChange("model", oldModel, this.dataModel);
    }

    public ComboBoxModel<E> getModel() {
        return this.dataModel;
    }

    @BeanProperty(expert=true, description="Set to <code>false</code> to require heavyweight popups.")
    public void setLightWeightPopupEnabled(boolean aFlag) {
        boolean oldFlag = this.lightWeightPopupEnabled;
        this.lightWeightPopupEnabled = aFlag;
        this.firePropertyChange("lightWeightPopupEnabled", oldFlag, this.lightWeightPopupEnabled);
    }

    public boolean isLightWeightPopupEnabled() {
        return this.lightWeightPopupEnabled;
    }

    @BeanProperty(preferred=true, description="If true, the user can type a new value in the combo box.")
    public void setEditable(boolean aFlag) {
        boolean oldFlag = this.isEditable;
        this.isEditable = aFlag;
        this.firePropertyChange("editable", oldFlag, this.isEditable);
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    @BeanProperty(preferred=true, description="The maximum number of rows the popup should have")
    public void setMaximumRowCount(int count) {
        int oldCount = this.maximumRowCount;
        this.maximumRowCount = count;
        this.firePropertyChange("maximumRowCount", oldCount, this.maximumRowCount);
    }

    public int getMaximumRowCount() {
        return this.maximumRowCount;
    }

    @BeanProperty(expert=true, description="The renderer that paints the item selected in the list.")
    public void setRenderer(ListCellRenderer<? super E> aRenderer) {
        ListCellRenderer<? super E> oldRenderer = this.renderer;
        this.renderer = aRenderer;
        this.firePropertyChange("renderer", oldRenderer, this.renderer);
        this.invalidate();
    }

    public ListCellRenderer<? super E> getRenderer() {
        return this.renderer;
    }

    @BeanProperty(expert=true, description="The editor that combo box uses to edit the current value")
    public void setEditor(ComboBoxEditor anEditor) {
        ComboBoxEditor oldEditor = this.editor;
        if (this.editor != null) {
            this.editor.removeActionListener(this);
        }
        this.editor = anEditor;
        if (this.editor != null) {
            this.editor.addActionListener(this);
        }
        this.firePropertyChange("editor", oldEditor, this.editor);
    }

    public ComboBoxEditor getEditor() {
        return this.editor;
    }

    @BeanProperty(bound=false, preferred=true, description="Sets the selected item in the JComboBox.")
    public void setSelectedItem(Object anObject) {
        Object oldSelection = this.selectedItemReminder;
        Object objectToSelect = anObject;
        if (oldSelection == null || !oldSelection.equals(anObject)) {
            if (anObject != null && !this.isEditable()) {
                boolean found = false;
                for (int i = 0; i < this.dataModel.getSize(); ++i) {
                    Object element = this.dataModel.getElementAt(i);
                    if (!anObject.equals(element)) continue;
                    found = true;
                    objectToSelect = element;
                    break;
                }
                if (!found) {
                    return;
                }
                this.getEditor().setItem(anObject);
            }
            this.selectingItem = true;
            this.dataModel.setSelectedItem(objectToSelect);
            this.selectingItem = false;
            if (this.selectedItemReminder != this.dataModel.getSelectedItem()) {
                this.selectedItemChanged();
            }
        }
        this.fireActionEvent();
    }

    public Object getSelectedItem() {
        return this.dataModel.getSelectedItem();
    }

    @BeanProperty(bound=false, preferred=true, description="The item at index is selected.")
    public void setSelectedIndex(int anIndex) {
        int size = this.dataModel.getSize();
        if (anIndex == -1) {
            this.setSelectedItem(null);
        } else {
            if (anIndex < -1 || anIndex >= size) {
                throw new IllegalArgumentException("setSelectedIndex: " + anIndex + " out of bounds");
            }
            this.setSelectedItem(this.dataModel.getElementAt(anIndex));
        }
    }

    @Transient
    public int getSelectedIndex() {
        Object sObject = this.dataModel.getSelectedItem();
        int c = this.dataModel.getSize();
        for (int i = 0; i < c; ++i) {
            Object obj = this.dataModel.getElementAt(i);
            if (obj == null || !obj.equals(sObject)) continue;
            return i;
        }
        return -1;
    }

    public E getPrototypeDisplayValue() {
        return this.prototypeDisplayValue;
    }

    @BeanProperty(visualUpdate=true, description="The display prototype value, used to compute display width and height.")
    public void setPrototypeDisplayValue(E prototypeDisplayValue) {
        E oldValue = this.prototypeDisplayValue;
        this.prototypeDisplayValue = prototypeDisplayValue;
        this.firePropertyChange("prototypeDisplayValue", oldValue, prototypeDisplayValue);
    }

    public void addItem(E item) {
        this.checkMutableComboBoxModel();
        ((MutableComboBoxModel)this.dataModel).addElement(item);
    }

    public void insertItemAt(E item, int index) {
        this.checkMutableComboBoxModel();
        ((MutableComboBoxModel)this.dataModel).insertElementAt(item, index);
    }

    public void removeItem(Object anObject) {
        this.checkMutableComboBoxModel();
        ((MutableComboBoxModel)this.dataModel).removeElement(anObject);
    }

    public void removeItemAt(int anIndex) {
        this.checkMutableComboBoxModel();
        ((MutableComboBoxModel)this.dataModel).removeElementAt(anIndex);
    }

    public void removeAllItems() {
        this.checkMutableComboBoxModel();
        MutableComboBoxModel model = (MutableComboBoxModel)this.dataModel;
        int size = model.getSize();
        if (model instanceof DefaultComboBoxModel) {
            ((DefaultComboBoxModel)model).removeAllElements();
        } else {
            for (int i = 0; i < size; ++i) {
                Object element = model.getElementAt(0);
                model.removeElement(element);
            }
        }
        this.selectedItemReminder = null;
        if (this.isEditable()) {
            this.editor.setItem(null);
        }
    }

    void checkMutableComboBoxModel() {
        if (!(this.dataModel instanceof MutableComboBoxModel)) {
            throw new RuntimeException("Cannot use this method with a non-Mutable data model.");
        }
    }

    public void showPopup() {
        this.setPopupVisible(true);
    }

    public void hidePopup() {
        this.setPopupVisible(false);
    }

    public void setPopupVisible(boolean v) {
        this.getUI().setPopupVisible(this, v);
    }

    public boolean isPopupVisible() {
        return this.getUI().isPopupVisible(this);
    }

    @Override
    public void addItemListener(ItemListener aListener) {
        this.listenerList.add(ItemListener.class, aListener);
    }

    @Override
    public void removeItemListener(ItemListener aListener) {
        this.listenerList.remove(ItemListener.class, aListener);
    }

    @BeanProperty(bound=false)
    public ItemListener[] getItemListeners() {
        return (ItemListener[])this.listenerList.getListeners(ItemListener.class);
    }

    public void addActionListener(ActionListener l) {
        this.listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        if (l != null && this.getAction() == l) {
            this.setAction(null);
        } else {
            this.listenerList.remove(ActionListener.class, l);
        }
    }

    @BeanProperty(bound=false)
    public ActionListener[] getActionListeners() {
        return (ActionListener[])this.listenerList.getListeners(ActionListener.class);
    }

    public void addPopupMenuListener(PopupMenuListener l) {
        this.listenerList.add(PopupMenuListener.class, l);
    }

    public void removePopupMenuListener(PopupMenuListener l) {
        this.listenerList.remove(PopupMenuListener.class, l);
    }

    @BeanProperty(bound=false)
    public PopupMenuListener[] getPopupMenuListeners() {
        return (PopupMenuListener[])this.listenerList.getListeners(PopupMenuListener.class);
    }

    public void firePopupMenuWillBecomeVisible() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuWillBecomeVisible(e);
        }
    }

    public void firePopupMenuWillBecomeInvisible() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuWillBecomeInvisible(e);
        }
    }

    public void firePopupMenuCanceled() {
        Object[] listeners = this.listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != PopupMenuListener.class) continue;
            if (e == null) {
                e = new PopupMenuEvent(this);
            }
            ((PopupMenuListener)listeners[i + 1]).popupMenuCanceled(e);
        }
    }

    public void setActionCommand(String aCommand) {
        this.actionCommand = aCommand;
    }

    public String getActionCommand() {
        return this.actionCommand;
    }

    @BeanProperty(visualUpdate=true, description="the Action instance connected with this ActionEvent source")
    public void setAction(Action a) {
        Action oldValue = this.getAction();
        if (this.action == null || !this.action.equals(a)) {
            this.action = a;
            if (oldValue != null) {
                this.removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(this.actionPropertyChangeListener);
                this.actionPropertyChangeListener = null;
            }
            this.configurePropertiesFromAction(this.action);
            if (this.action != null) {
                if (!this.isListener(ActionListener.class, this.action)) {
                    this.addActionListener(this.action);
                }
                this.actionPropertyChangeListener = this.createActionPropertyChangeListener(this.action);
                this.action.addPropertyChangeListener(this.actionPropertyChangeListener);
            }
            this.firePropertyChange("action", oldValue, this.action);
        }
    }

    private boolean isListener(Class<?> c, ActionListener a) {
        boolean isListener = false;
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != c || listeners[i + 1] != a) continue;
            isListener = true;
        }
        return isListener;
    }

    public Action getAction() {
        return this.action;
    }

    protected void configurePropertiesFromAction(Action a) {
        AbstractAction.setEnabledFromAction(this, a);
        AbstractAction.setToolTipTextFromAction(this, a);
        this.setActionCommandFromAction(a);
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new ComboBoxActionPropertyChangeListener(this, a);
    }

    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == "ActionCommandKey") {
            this.setActionCommandFromAction(action);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if ("ShortDescription" == propertyName) {
            AbstractAction.setToolTipTextFromAction(this, action);
        }
    }

    private void setActionCommandFromAction(Action a) {
        this.setActionCommand(a != null ? (String)a.getValue("ActionCommandKey") : null);
    }

    protected void fireItemStateChanged(ItemEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ItemListener.class) continue;
            ((ItemListener)listeners[i + 1]).itemStateChanged(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fireActionEvent() {
        if (!this.firingActionEvent) {
            this.firingActionEvent = true;
            ActionEvent e = null;
            Object[] listeners = this.listenerList.getListenerList();
            long mostRecentEventTime = EventQueue.getMostRecentEventTime();
            int modifiers = 0;
            AWTEvent currentEvent = EventQueue.getCurrentEvent();
            if (currentEvent instanceof InputEvent) {
                modifiers = ((InputEvent)currentEvent).getModifiers();
            } else if (currentEvent instanceof ActionEvent) {
                modifiers = ((ActionEvent)currentEvent).getModifiers();
            }
            try {
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] != ActionListener.class) continue;
                    if (e == null) {
                        e = new ActionEvent(this, 1001, this.getActionCommand(), mostRecentEventTime, modifiers);
                    }
                    ((ActionListener)listeners[i + 1]).actionPerformed(e);
                }
            }
            finally {
                this.firingActionEvent = false;
            }
        }
    }

    protected void selectedItemChanged() {
        if (this.selectedItemReminder != null) {
            this.fireItemStateChanged(new ItemEvent(this, 701, this.selectedItemReminder, 2));
        }
        this.selectedItemReminder = this.dataModel.getSelectedItem();
        if (this.selectedItemReminder != null) {
            this.fireItemStateChanged(new ItemEvent(this, 701, this.selectedItemReminder, 1));
        }
    }

    @Override
    @BeanProperty(bound=false)
    public Object[] getSelectedObjects() {
        Object selectedObject = this.getSelectedItem();
        if (selectedObject == null) {
            return new Object[0];
        }
        Object[] result = new Object[]{selectedObject};
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.setPopupVisible(false);
        this.getModel().setSelectedItem(this.getEditor().getItem());
        String oldCommand = this.getActionCommand();
        this.setActionCommand("comboBoxEdited");
        this.fireActionEvent();
        this.setActionCommand(oldCommand);
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        Object oldSelection = this.selectedItemReminder;
        Object newSelection = this.dataModel.getSelectedItem();
        if (oldSelection == null || !oldSelection.equals(newSelection)) {
            this.selectedItemChanged();
            if (!this.selectingItem) {
                this.fireActionEvent();
            }
        }
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        if (this.selectedItemReminder != this.dataModel.getSelectedItem()) {
            this.selectedItemChanged();
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        this.contentsChanged(e);
    }

    public boolean selectWithKeyChar(char keyChar) {
        int index;
        if (this.keySelectionManager == null) {
            this.keySelectionManager = this.createDefaultKeySelectionManager();
        }
        if ((index = this.keySelectionManager.selectionForKey(keyChar, this.getModel())) != -1) {
            this.setSelectedIndex(index);
            return true;
        }
        return false;
    }

    @Override
    @BeanProperty(preferred=true, description="The enabled state of the component.")
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        this.firePropertyChange("enabled", !this.isEnabled(), this.isEnabled());
    }

    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        anEditor.setItem(anItem);
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == 9) {
            this.hidePopup();
        }
        super.processKeyEvent(e);
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (super.processKeyBinding(ks, e, condition, pressed)) {
            return true;
        }
        if (!this.isEditable() || condition != 0 || this.getEditor() == null || !Boolean.TRUE.equals(this.getClientProperty("JComboBox.isTableCellEditor"))) {
            return false;
        }
        Component editorComponent = this.getEditor().getEditorComponent();
        if (editorComponent instanceof JComponent) {
            JComponent component = (JComponent)editorComponent;
            return component.processKeyBinding(ks, e, 0, pressed);
        }
        return false;
    }

    @BeanProperty(bound=false, expert=true, description="The objects that changes the selection when a key is pressed.")
    public void setKeySelectionManager(KeySelectionManager aManager) {
        this.keySelectionManager = aManager;
    }

    public KeySelectionManager getKeySelectionManager() {
        return this.keySelectionManager;
    }

    @BeanProperty(bound=false)
    public int getItemCount() {
        return this.dataModel.getSize();
    }

    public E getItemAt(int index) {
        return this.dataModel.getElementAt(index);
    }

    protected KeySelectionManager createDefaultKeySelectionManager() {
        return new DefaultKeySelectionManager();
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
        String selectedItemReminderString = this.selectedItemReminder != null ? this.selectedItemReminder.toString() : "";
        String isEditableString = this.isEditable ? "true" : "false";
        String lightWeightPopupEnabledString = this.lightWeightPopupEnabled ? "true" : "false";
        return super.paramString() + ",isEditable=" + isEditableString + ",lightWeightPopupEnabled=" + lightWeightPopupEnabledString + ",maximumRowCount=" + this.maximumRowCount + ",selectedItemReminder=" + selectedItemReminderString;
    }

    @Override
    @BeanProperty(bound=false)
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleJComboBox();
        }
        return this.accessibleContext;
    }

    public static interface KeySelectionManager {
        public int selectionForKey(char var1, ComboBoxModel<?> var2);
    }

    private static class ComboBoxActionPropertyChangeListener
    extends ActionPropertyChangeListener<JComboBox<?>> {
        ComboBoxActionPropertyChangeListener(JComboBox<?> b, Action a) {
            super(b, a);
        }

        @Override
        protected void actionPropertyChanged(JComboBox<?> cb, Action action, PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                cb.configurePropertiesFromAction(action);
            } else {
                cb.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }

    static class DefaultKeySelectionManager
    implements KeySelectionManager,
    Serializable {
        DefaultKeySelectionManager() {
        }

        @Override
        public int selectionForKey(char aKey, ComboBoxModel<?> aModel) {
            String v;
            Object elem;
            int i;
            int c;
            int currentSelection = -1;
            Object selectedItem = aModel.getSelectedItem();
            if (selectedItem != null) {
                c = aModel.getSize();
                for (i = 0; i < c; ++i) {
                    if (selectedItem != aModel.getElementAt(i)) continue;
                    currentSelection = i;
                    break;
                }
            }
            String pattern = ("" + aKey).toLowerCase();
            aKey = pattern.charAt(0);
            c = aModel.getSize();
            for (i = ++currentSelection; i < c; ++i) {
                elem = aModel.getElementAt(i);
                if (elem == null || elem.toString() == null || (v = elem.toString().toLowerCase()).length() <= 0 || v.charAt(0) != aKey) continue;
                return i;
            }
            for (i = 0; i < currentSelection; ++i) {
                elem = aModel.getElementAt(i);
                if (elem == null || elem.toString() == null || (v = elem.toString().toLowerCase()).length() <= 0 || v.charAt(0) != aKey) continue;
                return i;
            }
            return -1;
        }
    }

    protected class AccessibleJComboBox
    extends JComponent.AccessibleJComponent
    implements AccessibleAction,
    AccessibleSelection {
        private JList<?> popupList;
        private Accessible previousSelectedAccessible;
        private javax.swing.JComboBox$AccessibleJComboBox.EditorAccessibleContext editorAccessibleContext;

        public AccessibleJComboBox() {
            super(JComboBox.this);
            this.previousSelectedAccessible = null;
            this.editorAccessibleContext = null;
            JComboBox.this.addPropertyChangeListener(new AccessibleJComboBoxPropertyChangeListener());
            this.setEditorNameAndDescription();
            Accessible a = JComboBox.this.getUI().getAccessibleChild(JComboBox.this, 0);
            if (a instanceof ComboPopup) {
                this.popupList = ((ComboPopup)((Object)a)).getList();
                this.popupList.addListSelectionListener(new AccessibleJComboBoxListSelectionListener());
            }
            JComboBox.this.addPopupMenuListener(new AccessibleJComboBoxPopupMenuListener());
        }

        private void setEditorNameAndDescription() {
            AccessibleContext ac;
            Component comp;
            ComboBoxEditor editor = JComboBox.this.getEditor();
            if (editor != null && (comp = editor.getEditorComponent()) instanceof Accessible && (ac = comp.getAccessibleContext()) != null) {
                ac.setAccessibleName(this.getAccessibleName());
                ac.setAccessibleDescription(this.getAccessibleDescription());
            }
        }

        @Override
        public int getAccessibleChildrenCount() {
            if (JComboBox.this.ui != null) {
                return JComboBox.this.ui.getAccessibleChildrenCount(JComboBox.this);
            }
            return super.getAccessibleChildrenCount();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            if (JComboBox.this.ui != null) {
                return JComboBox.this.ui.getAccessibleChild(JComboBox.this, i);
            }
            return super.getAccessibleChild(i);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COMBO_BOX;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet ass = super.getAccessibleStateSet();
            if (ass == null) {
                ass = new AccessibleStateSet();
            }
            if (JComboBox.this.isPopupVisible()) {
                ass.add(AccessibleState.EXPANDED);
            } else {
                ass.add(AccessibleState.COLLAPSED);
            }
            return ass;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return UIManager.getString("ComboBox.togglePopupText");
            }
            return null;
        }

        @Override
        public int getAccessibleActionCount() {
            return 1;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                JComboBox.this.setPopupVisible(!JComboBox.this.isPopupVisible());
                return true;
            }
            return false;
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public int getAccessibleSelectionCount() {
            Object o = JComboBox.this.getSelectedItem();
            if (o != null) {
                return 1;
            }
            return 0;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            AccessibleSelection as;
            ComboPopup popup;
            JList<Object> list;
            AccessibleContext ac;
            Accessible a = JComboBox.this.getUI().getAccessibleChild(JComboBox.this, 0);
            if (a instanceof ComboPopup && (ac = (list = (popup = (ComboPopup)((Object)a)).getList()).getAccessibleContext()) != null && (as = ac.getAccessibleSelection()) != null) {
                return as.getAccessibleSelection(i);
            }
            return null;
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            return JComboBox.this.getSelectedIndex() == i;
        }

        @Override
        public void addAccessibleSelection(int i) {
            this.clearAccessibleSelection();
            JComboBox.this.setSelectedIndex(i);
        }

        @Override
        public void removeAccessibleSelection(int i) {
            if (JComboBox.this.getSelectedIndex() == i) {
                this.clearAccessibleSelection();
            }
        }

        @Override
        public void clearAccessibleSelection() {
            JComboBox.this.setSelectedIndex(-1);
        }

        @Override
        public void selectAllAccessibleSelection() {
        }

        private class EditorAccessibleContext
        extends AccessibleContext {
            private AccessibleContext ac;

            private EditorAccessibleContext() {
            }

            EditorAccessibleContext(Accessible a) {
                this.ac = a.getAccessibleContext();
            }

            @Override
            public String getAccessibleName() {
                return this.ac.getAccessibleName();
            }

            @Override
            @BeanProperty(preferred=true, description="Sets the accessible name for the component.")
            public void setAccessibleName(String s) {
                this.ac.setAccessibleName(s);
            }

            @Override
            public String getAccessibleDescription() {
                return this.ac.getAccessibleDescription();
            }

            @Override
            @BeanProperty(preferred=true, description="Sets the accessible description for the component.")
            public void setAccessibleDescription(String s) {
                this.ac.setAccessibleDescription(s);
            }

            @Override
            public AccessibleRole getAccessibleRole() {
                return this.ac.getAccessibleRole();
            }

            @Override
            public AccessibleStateSet getAccessibleStateSet() {
                return this.ac.getAccessibleStateSet();
            }

            @Override
            public Accessible getAccessibleParent() {
                return this.ac.getAccessibleParent();
            }

            @Override
            public void setAccessibleParent(Accessible a) {
                this.ac.setAccessibleParent(a);
            }

            @Override
            public int getAccessibleIndexInParent() {
                return JComboBox.this.getSelectedIndex();
            }

            @Override
            public int getAccessibleChildrenCount() {
                return this.ac.getAccessibleChildrenCount();
            }

            @Override
            public Accessible getAccessibleChild(int i) {
                return this.ac.getAccessibleChild(i);
            }

            @Override
            public Locale getLocale() throws IllegalComponentStateException {
                return this.ac.getLocale();
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                this.ac.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                this.ac.removePropertyChangeListener(listener);
            }

            @Override
            public AccessibleAction getAccessibleAction() {
                return this.ac.getAccessibleAction();
            }

            @Override
            public AccessibleComponent getAccessibleComponent() {
                return this.ac.getAccessibleComponent();
            }

            @Override
            public AccessibleSelection getAccessibleSelection() {
                return this.ac.getAccessibleSelection();
            }

            @Override
            public AccessibleText getAccessibleText() {
                return this.ac.getAccessibleText();
            }

            @Override
            public AccessibleEditableText getAccessibleEditableText() {
                return this.ac.getAccessibleEditableText();
            }

            @Override
            public AccessibleValue getAccessibleValue() {
                return this.ac.getAccessibleValue();
            }

            @Override
            public AccessibleIcon[] getAccessibleIcon() {
                return this.ac.getAccessibleIcon();
            }

            @Override
            public AccessibleRelationSet getAccessibleRelationSet() {
                return this.ac.getAccessibleRelationSet();
            }

            @Override
            public AccessibleTable getAccessibleTable() {
                return this.ac.getAccessibleTable();
            }

            @Override
            public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                this.ac.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        private class AccessibleJComboBoxPropertyChangeListener
        implements PropertyChangeListener {
            private AccessibleJComboBoxPropertyChangeListener() {
            }

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() == "editor") {
                    AccessibleJComboBox.this.setEditorNameAndDescription();
                }
            }
        }

        private class AccessibleJComboBoxListSelectionListener
        implements ListSelectionListener {
            private AccessibleJComboBoxListSelectionListener() {
            }

            @Override
            public void valueChanged(ListSelectionEvent e) {
                PropertyChangeEvent pce;
                if (AccessibleJComboBox.this.popupList == null) {
                    return;
                }
                int selectedIndex = AccessibleJComboBox.this.popupList.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                Accessible selectedAccessible = AccessibleJComboBox.this.popupList.getAccessibleContext().getAccessibleChild(selectedIndex);
                if (selectedAccessible == null) {
                    return;
                }
                if (AccessibleJComboBox.this.previousSelectedAccessible != null) {
                    pce = new PropertyChangeEvent(AccessibleJComboBox.this.previousSelectedAccessible, "AccessibleState", AccessibleState.FOCUSED, null);
                    AccessibleJComboBox.this.firePropertyChange("AccessibleState", null, pce);
                }
                pce = new PropertyChangeEvent(selectedAccessible, "AccessibleState", null, AccessibleState.FOCUSED);
                AccessibleJComboBox.this.firePropertyChange("AccessibleState", null, pce);
                AccessibleJComboBox.this.firePropertyChange("AccessibleActiveDescendant", AccessibleJComboBox.this.previousSelectedAccessible, selectedAccessible);
                AccessibleJComboBox.this.previousSelectedAccessible = selectedAccessible;
            }
        }

        private class AccessibleJComboBoxPopupMenuListener
        implements PopupMenuListener {
            private AccessibleJComboBoxPopupMenuListener() {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (AccessibleJComboBox.this.popupList == null) {
                    return;
                }
                int selectedIndex = AccessibleJComboBox.this.popupList.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                AccessibleJComboBox.this.previousSelectedAccessible = AccessibleJComboBox.this.popupList.getAccessibleContext().getAccessibleChild(selectedIndex);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        }

        private class AccessibleEditor
        implements Accessible {
            private AccessibleEditor() {
            }

            @Override
            public AccessibleContext getAccessibleContext() {
                Component c;
                if (AccessibleJComboBox.this.editorAccessibleContext == null && (c = JComboBox.this.getEditor().getEditorComponent()) instanceof Accessible) {
                    AccessibleJComboBox.this.editorAccessibleContext = new EditorAccessibleContext((Accessible)((Object)c));
                }
                return AccessibleJComboBox.this.editorAccessibleContext;
            }
        }
    }
}

