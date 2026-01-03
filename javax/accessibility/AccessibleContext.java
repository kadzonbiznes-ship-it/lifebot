/*
 * Decompiled with CFR 0.152.
 */
package javax.accessibility;

import java.awt.IllegalComponentStateException;
import java.beans.BeanProperty;
import java.beans.JavaBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleEditableText;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleTable;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;

@JavaBean(description="Minimal information that all accessible objects return")
public abstract class AccessibleContext {
    private volatile AppContext targetAppContext;
    public static final String ACCESSIBLE_NAME_PROPERTY = "AccessibleName";
    public static final String ACCESSIBLE_DESCRIPTION_PROPERTY = "AccessibleDescription";
    public static final String ACCESSIBLE_STATE_PROPERTY = "AccessibleState";
    public static final String ACCESSIBLE_VALUE_PROPERTY = "AccessibleValue";
    public static final String ACCESSIBLE_SELECTION_PROPERTY = "AccessibleSelection";
    public static final String ACCESSIBLE_CARET_PROPERTY = "AccessibleCaret";
    public static final String ACCESSIBLE_VISIBLE_DATA_PROPERTY = "AccessibleVisibleData";
    public static final String ACCESSIBLE_CHILD_PROPERTY = "AccessibleChild";
    public static final String ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY = "AccessibleActiveDescendant";
    public static final String ACCESSIBLE_TABLE_CAPTION_CHANGED = "accessibleTableCaptionChanged";
    public static final String ACCESSIBLE_TABLE_SUMMARY_CHANGED = "accessibleTableSummaryChanged";
    public static final String ACCESSIBLE_TABLE_MODEL_CHANGED = "accessibleTableModelChanged";
    public static final String ACCESSIBLE_TABLE_ROW_HEADER_CHANGED = "accessibleTableRowHeaderChanged";
    public static final String ACCESSIBLE_TABLE_ROW_DESCRIPTION_CHANGED = "accessibleTableRowDescriptionChanged";
    public static final String ACCESSIBLE_TABLE_COLUMN_HEADER_CHANGED = "accessibleTableColumnHeaderChanged";
    public static final String ACCESSIBLE_TABLE_COLUMN_DESCRIPTION_CHANGED = "accessibleTableColumnDescriptionChanged";
    public static final String ACCESSIBLE_ACTION_PROPERTY = "accessibleActionProperty";
    public static final String ACCESSIBLE_HYPERTEXT_OFFSET = "AccessibleHypertextOffset";
    public static final String ACCESSIBLE_TEXT_PROPERTY = "AccessibleText";
    public static final String ACCESSIBLE_INVALIDATE_CHILDREN = "accessibleInvalidateChildren";
    public static final String ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED = "accessibleTextAttributesChanged";
    public static final String ACCESSIBLE_COMPONENT_BOUNDS_CHANGED = "accessibleComponentBoundsChanged";
    protected Accessible accessibleParent = null;
    protected String accessibleName = null;
    protected String accessibleDescription = null;
    private PropertyChangeSupport accessibleChangeSupport = null;
    private AccessibleRelationSet relationSet = new AccessibleRelationSet();
    private Object nativeAXResource;

    protected AccessibleContext() {
    }

    public String getAccessibleName() {
        return this.accessibleName;
    }

    @BeanProperty(preferred=true, description="Sets the accessible name for the component.")
    public void setAccessibleName(String s) {
        String oldName = this.accessibleName;
        this.accessibleName = s;
        this.firePropertyChange(ACCESSIBLE_NAME_PROPERTY, oldName, this.accessibleName);
    }

    public String getAccessibleDescription() {
        return this.accessibleDescription;
    }

    @BeanProperty(preferred=true, description="Sets the accessible description for the component.")
    public void setAccessibleDescription(String s) {
        String oldDescription = this.accessibleDescription;
        this.accessibleDescription = s;
        this.firePropertyChange(ACCESSIBLE_DESCRIPTION_PROPERTY, oldDescription, this.accessibleDescription);
    }

    public abstract AccessibleRole getAccessibleRole();

    public abstract AccessibleStateSet getAccessibleStateSet();

    public Accessible getAccessibleParent() {
        return this.accessibleParent;
    }

    public void setAccessibleParent(Accessible a) {
        this.accessibleParent = a;
    }

    public abstract int getAccessibleIndexInParent();

    public abstract int getAccessibleChildrenCount();

    public abstract Accessible getAccessibleChild(int var1);

    public abstract Locale getLocale() throws IllegalComponentStateException;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.accessibleChangeSupport == null) {
            this.accessibleChangeSupport = new PropertyChangeSupport(this);
        }
        this.accessibleChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.accessibleChangeSupport != null) {
            this.accessibleChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public AccessibleAction getAccessibleAction() {
        return null;
    }

    public AccessibleComponent getAccessibleComponent() {
        return null;
    }

    public AccessibleSelection getAccessibleSelection() {
        return null;
    }

    public AccessibleText getAccessibleText() {
        return null;
    }

    public AccessibleEditableText getAccessibleEditableText() {
        return null;
    }

    public AccessibleValue getAccessibleValue() {
        return null;
    }

    public AccessibleIcon[] getAccessibleIcon() {
        return null;
    }

    public AccessibleRelationSet getAccessibleRelationSet() {
        return this.relationSet;
    }

    public AccessibleTable getAccessibleTable() {
        return null;
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.accessibleChangeSupport != null) {
            if (newValue instanceof PropertyChangeEvent) {
                PropertyChangeEvent pce = (PropertyChangeEvent)newValue;
                this.accessibleChangeSupport.firePropertyChange(pce);
            } else {
                this.accessibleChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    }

    static {
        AWTAccessor.setAccessibleContextAccessor(new AWTAccessor.AccessibleContextAccessor(){

            @Override
            public void setAppContext(AccessibleContext accessibleContext, AppContext appContext) {
                accessibleContext.targetAppContext = appContext;
            }

            @Override
            public AppContext getAppContext(AccessibleContext accessibleContext) {
                return accessibleContext.targetAppContext;
            }

            @Override
            public Object getNativeAXResource(AccessibleContext accessibleContext) {
                return accessibleContext.nativeAXResource;
            }

            @Override
            public void setNativeAXResource(AccessibleContext accessibleContext, Object value) {
                accessibleContext.nativeAXResource = value;
            }
        });
    }
}

