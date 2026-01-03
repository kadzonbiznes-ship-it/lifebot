/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.validation;

import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ValidationState
implements ValidationContext {
    private boolean fExtraChecking = true;
    private boolean fFacetChecking = true;
    private boolean fNormalize = true;
    private boolean fNamespaces = true;
    private EntityState fEntityState = null;
    private NamespaceContext fNamespaceContext = null;
    private SymbolTable fSymbolTable = null;
    private Locale fLocale = null;
    private HashSet<String> fIds;
    private List<String> fIdRefList;

    public void setExtraChecking(boolean newValue) {
        this.fExtraChecking = newValue;
    }

    public void setFacetChecking(boolean newValue) {
        this.fFacetChecking = newValue;
    }

    public void setNormalizationRequired(boolean newValue) {
        this.fNormalize = newValue;
    }

    public void setUsingNamespaces(boolean newValue) {
        this.fNamespaces = newValue;
    }

    public void setEntityState(EntityState state) {
        this.fEntityState = state;
    }

    public void setNamespaceSupport(NamespaceContext namespace) {
        this.fNamespaceContext = namespace;
    }

    public void setSymbolTable(SymbolTable sTable) {
        this.fSymbolTable = sTable;
    }

    public Iterator<String> checkIDRefID() {
        HashSet<String> missingIDs = null;
        if (this.fIdRefList != null) {
            for (int i = 0; i < this.fIdRefList.size(); ++i) {
                String key = this.fIdRefList.get(i);
                if (this.fIds != null && this.fIds.contains(key)) continue;
                if (missingIDs == null) {
                    missingIDs = new HashSet<String>();
                }
                missingIDs.add(key);
            }
        }
        return missingIDs != null ? missingIDs.iterator() : null;
    }

    public void reset() {
        this.fExtraChecking = true;
        this.fFacetChecking = true;
        this.fNamespaces = true;
        this.fIds = null;
        this.fIdRefList = null;
        this.fEntityState = null;
        this.fNamespaceContext = null;
        this.fSymbolTable = null;
    }

    public void resetIDTables() {
        this.fIds = null;
        this.fIdRefList = null;
    }

    @Override
    public boolean needExtraChecking() {
        return this.fExtraChecking;
    }

    @Override
    public boolean needFacetChecking() {
        return this.fFacetChecking;
    }

    @Override
    public boolean needToNormalize() {
        return this.fNormalize;
    }

    @Override
    public boolean useNamespaces() {
        return this.fNamespaces;
    }

    @Override
    public boolean isEntityDeclared(String name) {
        if (this.fEntityState != null) {
            return this.fEntityState.isEntityDeclared(this.getSymbol(name));
        }
        return false;
    }

    @Override
    public boolean isEntityUnparsed(String name) {
        if (this.fEntityState != null) {
            return this.fEntityState.isEntityUnparsed(this.getSymbol(name));
        }
        return false;
    }

    @Override
    public boolean isIdDeclared(String name) {
        return this.fIds != null && this.fIds.contains(name);
    }

    @Override
    public void addId(String name) {
        if (this.fIds == null) {
            this.fIds = new HashSet();
        }
        this.fIds.add(name);
    }

    @Override
    public void addIdRef(String name) {
        if (this.fIdRefList == null) {
            this.fIdRefList = new ArrayList<String>();
        }
        this.fIdRefList.add(name);
    }

    @Override
    public String getSymbol(String symbol) {
        if (this.fSymbolTable != null) {
            return this.fSymbolTable.addSymbol(symbol);
        }
        return symbol.intern();
    }

    @Override
    public String getURI(String prefix) {
        if (this.fNamespaceContext != null) {
            return this.fNamespaceContext.getURI(prefix);
        }
        return null;
    }

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    @Override
    public Locale getLocale() {
        return this.fLocale;
    }
}

