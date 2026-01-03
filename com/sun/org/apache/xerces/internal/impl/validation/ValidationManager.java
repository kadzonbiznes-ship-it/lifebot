/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.validation;

import com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationState;
import java.util.ArrayList;
import java.util.List;

public class ValidationManager {
    protected final List<ValidationState> fVSs = new ArrayList<ValidationState>();
    protected boolean fGrammarFound = false;
    protected boolean fCachedDTD = false;

    public final void addValidationState(ValidationState vs) {
        this.fVSs.add(vs);
    }

    public final void setEntityState(EntityState state) {
        for (int i = this.fVSs.size() - 1; i >= 0; --i) {
            this.fVSs.get(i).setEntityState(state);
        }
    }

    public final void setGrammarFound(boolean grammar) {
        this.fGrammarFound = grammar;
    }

    public final boolean isGrammarFound() {
        return this.fGrammarFound;
    }

    public final void setCachedDTD(boolean cachedDTD) {
        this.fCachedDTD = cachedDTD;
    }

    public final boolean isCachedDTD() {
        return this.fCachedDTD;
    }

    public final void reset() {
        this.fVSs.clear();
        this.fGrammarFound = false;
        this.fCachedDTD = false;
    }
}

