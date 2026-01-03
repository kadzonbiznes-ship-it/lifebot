/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dv;

import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public interface DatatypeValidator {
    public void validate(String var1, ValidationContext var2) throws InvalidDatatypeValueException;
}

