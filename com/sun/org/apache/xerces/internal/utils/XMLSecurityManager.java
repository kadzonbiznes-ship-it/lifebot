/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.utils;

import com.sun.org.apache.xerces.internal.util.SecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import java.util.concurrent.CopyOnWriteArrayList;
import jdk.xml.internal.JdkProperty;
import jdk.xml.internal.SecuritySupport;
import org.xml.sax.SAXException;

public final class XMLSecurityManager {
    private static final int NO_LIMIT = 0;
    private final int[] values = new int[Limit.values().length];
    private JdkProperty.State[] states = new JdkProperty.State[Limit.values().length];
    boolean secureProcessing;
    private boolean[] isSet = new boolean[Limit.values().length];
    private final int indexEntityCountInfo = 10000;
    private String printEntityCountInfo = "";
    private static final CopyOnWriteArrayList<String> printedWarnings = new CopyOnWriteArrayList();

    public XMLSecurityManager() {
        this(false);
    }

    public XMLSecurityManager(boolean secureProcessing) {
        this.secureProcessing = secureProcessing;
        for (Limit limit : Limit.values()) {
            if (secureProcessing) {
                this.values[limit.ordinal()] = limit.secureValue;
                this.states[limit.ordinal()] = JdkProperty.State.FSP;
                continue;
            }
            this.values[limit.ordinal()] = limit.defaultValue();
            this.states[limit.ordinal()] = JdkProperty.State.DEFAULT;
        }
        this.readSystemProperties();
    }

    public void setSecureProcessing(boolean secure) {
        this.secureProcessing = secure;
        for (Limit limit : Limit.values()) {
            if (secure) {
                this.setLimit(limit.ordinal(), JdkProperty.State.FSP, limit.secureValue());
                continue;
            }
            this.setLimit(limit.ordinal(), JdkProperty.State.FSP, limit.defaultValue());
        }
    }

    public boolean isSecureProcessing() {
        return this.secureProcessing;
    }

    public String find(String propertyName) {
        for (Limit limit : Limit.values()) {
            if (!limit.is(propertyName)) continue;
            return limit.systemProperty();
        }
        if (JdkProperty.ImplPropMap.ENTITYCOUNT.is(propertyName)) {
            return JdkProperty.ImplPropMap.ENTITYCOUNT.qName();
        }
        return null;
    }

    public boolean setLimit(String propertyName, JdkProperty.State state, Object value) {
        int index = this.getIndex(propertyName);
        if (index > -1) {
            JdkProperty.State pState = state;
            if (index != 10000 && state == JdkProperty.State.APIPROPERTY) {
                pState = Limit.values()[index].getState(propertyName);
            }
            this.setLimit(index, pState, value);
            return true;
        }
        return false;
    }

    public void setLimit(Limit limit, JdkProperty.State state, int value) {
        this.setLimit(limit.ordinal(), state, value);
    }

    public void setLimit(int index, JdkProperty.State state, Object value) {
        if (index == 10000) {
            this.printEntityCountInfo = (String)value;
        } else {
            int temp;
            if (value instanceof Integer) {
                temp = (Integer)value;
            } else {
                temp = Integer.parseInt((String)value);
                if (temp < 0) {
                    temp = 0;
                }
            }
            this.setLimit(index, state, temp);
        }
    }

    public void setLimit(int index, JdkProperty.State state, int value) {
        if (index == 10000) {
            this.printEntityCountInfo = "yes";
        } else if (state.compareTo(this.states[index]) >= 0) {
            this.values[index] = value;
            this.states[index] = state;
            this.isSet[index] = true;
        }
    }

    public String getLimitAsString(String propertyName) {
        int index = this.getIndex(propertyName);
        if (index > -1) {
            return this.getLimitValueByIndex(index);
        }
        return null;
    }

    public int getLimit(Limit limit) {
        return this.values[limit.ordinal()];
    }

    public String getLimitValueAsString(Limit limit) {
        return Integer.toString(this.values[limit.ordinal()]);
    }

    public String getLimitValueByIndex(int index) {
        if (index == 10000) {
            return this.printEntityCountInfo;
        }
        return Integer.toString(this.values[index]);
    }

    public JdkProperty.State getState(Limit limit) {
        return this.states[limit.ordinal()];
    }

    public String getStateLiteral(Limit limit) {
        return this.states[limit.ordinal()].literal();
    }

    public int getIndex(String propertyName) {
        for (Limit limit : Limit.values()) {
            if (!limit.is(propertyName)) continue;
            return limit.ordinal();
        }
        if (JdkProperty.ImplPropMap.ENTITYCOUNT.is(propertyName)) {
            return 10000;
        }
        return -1;
    }

    public boolean isNoLimit(int limit) {
        return limit == 0;
    }

    public boolean isOverLimit(Limit limit, String entityName, int size, XMLLimitAnalyzer limitAnalyzer) {
        return this.isOverLimit(limit.ordinal(), entityName, size, limitAnalyzer);
    }

    public boolean isOverLimit(int index, String entityName, int size, XMLLimitAnalyzer limitAnalyzer) {
        if (this.values[index] == 0) {
            return false;
        }
        if (size > this.values[index]) {
            limitAnalyzer.addValue(index, entityName, size);
            return true;
        }
        return false;
    }

    public boolean isOverLimit(Limit limit, XMLLimitAnalyzer limitAnalyzer) {
        return this.isOverLimit(limit.ordinal(), limitAnalyzer);
    }

    public boolean isOverLimit(int index, XMLLimitAnalyzer limitAnalyzer) {
        if (this.values[index] == 0) {
            return false;
        }
        if (index == Limit.ELEMENT_ATTRIBUTE_LIMIT.ordinal() || index == Limit.ENTITY_EXPANSION_LIMIT.ordinal() || index == Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal() || index == Limit.ENTITY_REPLACEMENT_LIMIT.ordinal() || index == Limit.MAX_ELEMENT_DEPTH_LIMIT.ordinal() || index == Limit.MAX_NAME_LIMIT.ordinal()) {
            return limitAnalyzer.getTotalValue(index) > this.values[index];
        }
        return limitAnalyzer.getValue(index) > this.values[index];
    }

    public void debugPrint(XMLLimitAnalyzer limitAnalyzer) {
        if (this.printEntityCountInfo.equals("yes")) {
            limitAnalyzer.debugPrint(this);
        }
    }

    public boolean isSet(int index) {
        return this.isSet[index];
    }

    public boolean printEntityCountInfo() {
        return this.printEntityCountInfo.equals("yes");
    }

    private void readSystemProperties() {
        for (Limit limit : Limit.values()) {
            if (this.getSystemProperty(limit, limit.systemProperty()) || this.getOldSystemProperty(limit)) continue;
            this.getPropertyConfig(limit, limit.systemProperty());
        }
    }

    public static void printWarning(String parserClassName, String propertyName, SAXException exception) {
        String key = parserClassName + ":" + propertyName;
        if (printedWarnings.addIfAbsent(key)) {
            System.err.println("Warning: " + parserClassName + ": " + exception.getMessage());
        }
    }

    private boolean getSystemProperty(Limit limit, String sysPropertyName) {
        try {
            String value = SecuritySupport.getSystemProperty(sysPropertyName);
            if (value != null && !value.equals("")) {
                this.values[limit.ordinal()] = Integer.parseInt(value);
                this.states[limit.ordinal()] = JdkProperty.State.SYSTEMPROPERTY;
                return true;
            }
        }
        catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid setting for system property: " + limit.systemProperty());
        }
        return false;
    }

    private boolean getOldSystemProperty(Limit limit) {
        boolean found = false;
        for (NameMap nameMap : NameMap.values()) {
            String oldName = nameMap.getOldName(limit.systemProperty());
            if (oldName == null || !this.getSystemProperty(limit, oldName)) continue;
            found = true;
            break;
        }
        return found;
    }

    private boolean getPropertyConfig(Limit limit, String sysPropertyName) {
        try {
            String value = SecuritySupport.readConfig(sysPropertyName);
            if (value != null && !value.equals("")) {
                this.values[limit.ordinal()] = Integer.parseInt(value);
                this.states[limit.ordinal()] = JdkProperty.State.JAXPDOTPROPERTIES;
                return true;
            }
        }
        catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid setting for system property: " + limit.systemProperty());
        }
        return false;
    }

    public static XMLSecurityManager convert(Object value, XMLSecurityManager securityManager) {
        if (value == null) {
            if (securityManager == null) {
                securityManager = new XMLSecurityManager(true);
            }
            return securityManager;
        }
        if (value instanceof XMLSecurityManager) {
            return (XMLSecurityManager)value;
        }
        if (securityManager == null) {
            securityManager = new XMLSecurityManager(true);
        }
        if (value instanceof SecurityManager) {
            SecurityManager origSM = (SecurityManager)value;
            securityManager.setLimit(Limit.MAX_OCCUR_NODE_LIMIT, JdkProperty.State.APIPROPERTY, origSM.getMaxOccurNodeLimit());
            securityManager.setLimit(Limit.ENTITY_EXPANSION_LIMIT, JdkProperty.State.APIPROPERTY, origSM.getEntityExpansionLimit());
            securityManager.setLimit(Limit.ELEMENT_ATTRIBUTE_LIMIT, JdkProperty.State.APIPROPERTY, origSM.getElementAttrLimit());
        }
        return securityManager;
    }

    public static enum Limit {
        ENTITY_EXPANSION_LIMIT("EntityExpansionLimit", "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", "jdk.xml.entityExpansionLimit", 0, 64000),
        MAX_OCCUR_NODE_LIMIT("MaxOccurLimit", "http://www.oracle.com/xml/jaxp/properties/maxOccurLimit", "jdk.xml.maxOccurLimit", 0, 5000),
        ELEMENT_ATTRIBUTE_LIMIT("ElementAttributeLimit", "http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit", "jdk.xml.elementAttributeLimit", 0, 10000),
        TOTAL_ENTITY_SIZE_LIMIT("TotalEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit", "jdk.xml.totalEntitySizeLimit", 0, 50000000),
        GENERAL_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/maxGeneralEntitySizeLimit", "jdk.xml.maxGeneralEntitySizeLimit", 0, 0),
        PARAMETER_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/maxParameterEntitySizeLimit", "jdk.xml.maxParameterEntitySizeLimit", 0, 1000000),
        MAX_ELEMENT_DEPTH_LIMIT("MaxElementDepthLimit", "http://www.oracle.com/xml/jaxp/properties/maxElementDepth", "jdk.xml.maxElementDepth", 0, 0),
        MAX_NAME_LIMIT("MaxXMLNameLimit", "http://www.oracle.com/xml/jaxp/properties/maxXMLNameLimit", "jdk.xml.maxXMLNameLimit", 1000, 1000),
        ENTITY_REPLACEMENT_LIMIT("EntityReplacementLimit", "http://www.oracle.com/xml/jaxp/properties/entityReplacementLimit", "jdk.xml.entityReplacementLimit", 0, 3000000);

        final String key;
        final String apiProperty;
        final String systemProperty;
        final int defaultValue;
        final int secureValue;

        private Limit(String key, String apiProperty, String systemProperty, int value, int secureValue) {
            this.key = key;
            this.apiProperty = apiProperty;
            this.systemProperty = systemProperty;
            this.defaultValue = value;
            this.secureValue = secureValue;
        }

        public boolean is(String name) {
            return this.systemProperty != null && this.systemProperty.equals(name) || this.apiProperty.equals(name);
        }

        public JdkProperty.State getState(String name) {
            if (this.systemProperty != null && this.systemProperty.equals(name)) {
                return JdkProperty.State.APIPROPERTY;
            }
            if (this.apiProperty.equals(name)) {
                return JdkProperty.State.LEGACY_APIPROPERTY;
            }
            return null;
        }

        public String key() {
            return this.key;
        }

        public String apiProperty() {
            return this.apiProperty;
        }

        public String systemProperty() {
            return this.systemProperty;
        }

        public int defaultValue() {
            return this.defaultValue;
        }

        int secureValue() {
            return this.secureValue;
        }
    }

    public static enum NameMap {
        ENTITY_EXPANSION_LIMIT("jdk.xml.entityExpansionLimit", "entityExpansionLimit"),
        MAX_OCCUR_NODE_LIMIT("jdk.xml.maxOccurLimit", "maxOccurLimit"),
        ELEMENT_ATTRIBUTE_LIMIT("jdk.xml.elementAttributeLimit", "elementAttributeLimit");

        final String newName;
        final String oldName;

        private NameMap(String newName, String oldName) {
            this.newName = newName;
            this.oldName = oldName;
        }

        String getOldName(String newName) {
            if (newName.equals(this.newName)) {
                return this.oldName;
            }
            return null;
        }
    }
}

