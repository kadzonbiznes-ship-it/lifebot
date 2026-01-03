/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dv.dtd;

import com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.ENTITYDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.IDDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.IDREFDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.ListDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.NOTATIONDatatypeValidator;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.StringDatatypeValidator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DTDDVFactoryImpl
extends DTDDVFactory {
    static final Map<String, DatatypeValidator> fBuiltInTypes;

    @Override
    public DatatypeValidator getBuiltInDV(String name) {
        return fBuiltInTypes.get(name);
    }

    @Override
    public Map<String, DatatypeValidator> getBuiltInTypes() {
        return new HashMap<String, DatatypeValidator>(fBuiltInTypes);
    }

    static {
        HashMap<String, DatatypeValidator> builtInTypes = new HashMap<String, DatatypeValidator>();
        builtInTypes.put("string", new StringDatatypeValidator());
        builtInTypes.put("ID", new IDDatatypeValidator());
        DatatypeValidator dvTemp = new IDREFDatatypeValidator();
        builtInTypes.put("IDREF", dvTemp);
        builtInTypes.put("IDREFS", new ListDatatypeValidator(dvTemp));
        dvTemp = new ENTITYDatatypeValidator();
        builtInTypes.put("ENTITY", new ENTITYDatatypeValidator());
        builtInTypes.put("ENTITIES", new ListDatatypeValidator(dvTemp));
        builtInTypes.put("NOTATION", new NOTATIONDatatypeValidator());
        dvTemp = new NMTOKENDatatypeValidator();
        builtInTypes.put("NMTOKEN", dvTemp);
        builtInTypes.put("NMTOKENS", new ListDatatypeValidator(dvTemp));
        fBuiltInTypes = Collections.unmodifiableMap(builtInTypes);
    }
}

