/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.utils;

import jdk.xml.internal.SecuritySupport;

public final class XMLSecurityPropertyManager {
    private final String[] values;
    private State[] states = new State[]{State.DEFAULT, State.DEFAULT};

    public XMLSecurityPropertyManager() {
        this.values = new String[Property.values().length];
        for (Property property : Property.values()) {
            this.values[property.ordinal()] = property.defaultValue();
        }
        this.readSystemProperties();
    }

    public String find(String propertyName) {
        for (Property property : Property.values()) {
            if (!property.equalsName(propertyName)) continue;
            return property.propertyName();
        }
        return null;
    }

    public boolean setValue(String propertyName, State state, Object value) {
        int index = this.getIndex(propertyName);
        if (index > -1) {
            this.setValue(index, state, (String)value);
            return true;
        }
        return false;
    }

    public void setValue(Property property, State state, String value) {
        if (state.compareTo(this.states[property.ordinal()]) >= 0) {
            this.values[property.ordinal()] = value;
            this.states[property.ordinal()] = state;
        }
    }

    public void setValue(int index, State state, String value) {
        if (state.compareTo(this.states[index]) >= 0) {
            this.values[index] = value;
            this.states[index] = state;
        }
    }

    public String getValue(String propertyName) {
        int index = this.getIndex(propertyName);
        if (index > -1) {
            return this.getValueByIndex(index);
        }
        return null;
    }

    public String getValue(Property property) {
        return this.values[property.ordinal()];
    }

    public String getValueByIndex(int index) {
        return this.values[index];
    }

    public int getIndex(String propertyName) {
        for (Property property : Property.values()) {
            if (!property.equalsName(propertyName)) continue;
            return property.ordinal();
        }
        return -1;
    }

    private void readSystemProperties() {
        this.getSystemProperty(Property.ACCESS_EXTERNAL_DTD, "javax.xml.accessExternalDTD");
        this.getSystemProperty(Property.ACCESS_EXTERNAL_SCHEMA, "javax.xml.accessExternalSchema");
    }

    private void getSystemProperty(Property property, String systemProperty) {
        try {
            String value = SecuritySupport.getSystemProperty(systemProperty);
            if (value != null) {
                this.values[property.ordinal()] = value;
                this.states[property.ordinal()] = State.SYSTEMPROPERTY;
                return;
            }
            value = SecuritySupport.readConfig(systemProperty);
            if (value != null) {
                this.values[property.ordinal()] = value;
                this.states[property.ordinal()] = State.JAXPDOTPROPERTIES;
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    public static enum State {
        DEFAULT,
        FSP,
        JAXPDOTPROPERTIES,
        SYSTEMPROPERTY,
        APIPROPERTY;

    }

    public static enum Property {
        ACCESS_EXTERNAL_DTD("http://javax.xml.XMLConstants/property/accessExternalDTD", "all"),
        ACCESS_EXTERNAL_SCHEMA("http://javax.xml.XMLConstants/property/accessExternalSchema", "all");

        final String name;
        final String defaultValue;

        private Property(String name, String value) {
            this.name = name;
            this.defaultValue = value;
        }

        public boolean equalsName(String propertyName) {
            return propertyName == null ? false : this.name.equals(propertyName);
        }

        public String propertyName() {
            return this.name;
        }

        String defaultValue() {
            return this.defaultValue;
        }
    }
}

