/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.utils;

import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

public final class XMLLimitAnalyzer {
    private final int[] values = new int[XMLSecurityManager.Limit.values().length];
    private final String[] names;
    private final int[] totalValue = new int[XMLSecurityManager.Limit.values().length];
    private final Map<String, Integer>[] caches;
    private String entityStart;
    private String entityEnd;

    public XMLLimitAnalyzer() {
        this.names = new String[XMLSecurityManager.Limit.values().length];
        this.caches = new Map[XMLSecurityManager.Limit.values().length];
    }

    public void addValue(XMLSecurityManager.Limit limit, String entityName, int value) {
        this.addValue(limit.ordinal(), entityName, value);
    }

    public void addValue(int index, String entityName, int value) {
        Map<Object, Object> cache;
        if (index == XMLSecurityManager.Limit.ENTITY_EXPANSION_LIMIT.ordinal() || index == XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT.ordinal() || index == XMLSecurityManager.Limit.ELEMENT_ATTRIBUTE_LIMIT.ordinal() || index == XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal() || index == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT.ordinal()) {
            int n = index;
            this.totalValue[n] = this.totalValue[n] + value;
            return;
        }
        if (index == XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT.ordinal() || index == XMLSecurityManager.Limit.MAX_NAME_LIMIT.ordinal()) {
            this.values[index] = value;
            this.totalValue[index] = value;
            return;
        }
        if (this.caches[index] == null) {
            cache = new HashMap(10);
            this.caches[index] = cache;
        } else {
            cache = this.caches[index];
        }
        int accumulatedValue = value;
        if (cache.containsKey(entityName)) {
            cache.put(entityName, accumulatedValue += ((Integer)cache.get(entityName)).intValue());
        } else {
            cache.put(entityName, value);
        }
        if (accumulatedValue > this.values[index]) {
            this.values[index] = accumulatedValue;
            this.names[index] = entityName;
        }
        if (index == XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT.ordinal() || index == XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT.ordinal()) {
            int n = XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal();
            this.totalValue[n] = this.totalValue[n] + value;
        }
    }

    public int getValue(XMLSecurityManager.Limit limit) {
        return this.getValue(limit.ordinal());
    }

    public int getValue(int index) {
        if (index == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT.ordinal()) {
            return this.totalValue[index];
        }
        return this.values[index];
    }

    public int getTotalValue(XMLSecurityManager.Limit limit) {
        return this.totalValue[limit.ordinal()];
    }

    public int getTotalValue(int index) {
        return this.totalValue[index];
    }

    public int getValueByIndex(int index) {
        return this.values[index];
    }

    public void startEntity(String name) {
        this.entityStart = name;
    }

    public boolean isTracking(String name) {
        if (this.entityStart == null) {
            return false;
        }
        return this.entityStart.equals(name);
    }

    public void endEntity(XMLSecurityManager.Limit limit, String name) {
        this.entityStart = "";
        Map<String, Integer> cache = this.caches[limit.ordinal()];
        if (cache != null) {
            cache.remove(name);
        }
    }

    public void reset(XMLSecurityManager.Limit limit) {
        if (limit.ordinal() == XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal()) {
            this.totalValue[limit.ordinal()] = 0;
        } else if (limit.ordinal() == XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT.ordinal()) {
            this.names[limit.ordinal()] = null;
            this.values[limit.ordinal()] = 0;
            this.caches[limit.ordinal()] = null;
            this.totalValue[limit.ordinal()] = 0;
        }
    }

    public void debugPrint(XMLSecurityManager securityManager) {
        Formatter formatter = new Formatter();
        System.out.println(formatter.format("%30s %15s %15s %15s %30s", "Property", "Limit", "Total size", "Size", "Entity Name"));
        for (XMLSecurityManager.Limit limit : XMLSecurityManager.Limit.values()) {
            formatter = new Formatter();
            System.out.println(formatter.format("%30s %15d %15d %15d %30s", limit.name(), securityManager.getLimit(limit), this.totalValue[limit.ordinal()], this.values[limit.ordinal()], this.names[limit.ordinal()]));
        }
    }
}

