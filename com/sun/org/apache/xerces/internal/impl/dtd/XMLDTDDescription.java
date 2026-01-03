/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import java.util.Iterator;
import java.util.List;

public class XMLDTDDescription
extends XMLResourceIdentifierImpl
implements com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription {
    protected String fRootName = null;
    protected List<String> fPossibleRoots = null;

    public XMLDTDDescription(XMLResourceIdentifier id, String rootName) {
        this.setValues(id.getPublicId(), id.getLiteralSystemId(), id.getBaseSystemId(), id.getExpandedSystemId());
        this.fRootName = rootName;
        this.fPossibleRoots = null;
    }

    public XMLDTDDescription(String publicId, String literalId, String baseId, String expandedId, String rootName) {
        this.setValues(publicId, literalId, baseId, expandedId);
        this.fRootName = rootName;
        this.fPossibleRoots = null;
    }

    public XMLDTDDescription(XMLInputSource source) {
        this.setValues(source.getPublicId(), null, source.getBaseSystemId(), source.getSystemId());
        this.fRootName = null;
        this.fPossibleRoots = null;
    }

    @Override
    public String getGrammarType() {
        return "http://www.w3.org/TR/REC-xml";
    }

    @Override
    public String getRootName() {
        return this.fRootName;
    }

    public void setRootName(String rootName) {
        this.fRootName = rootName;
        this.fPossibleRoots = null;
    }

    public void setPossibleRoots(List<String> possibleRoots) {
        this.fPossibleRoots = possibleRoots;
    }

    public boolean equals(Object desc) {
        if (!(desc instanceof XMLGrammarDescription)) {
            return false;
        }
        if (!this.getGrammarType().equals(((XMLGrammarDescription)desc).getGrammarType())) {
            return false;
        }
        XMLDTDDescription dtdDesc = (XMLDTDDescription)desc;
        if (this.fRootName != null) {
            if (dtdDesc.fRootName != null && !dtdDesc.fRootName.equals(this.fRootName)) {
                return false;
            }
            if (dtdDesc.fPossibleRoots != null && !dtdDesc.fPossibleRoots.contains(this.fRootName)) {
                return false;
            }
        } else if (this.fPossibleRoots != null) {
            if (dtdDesc.fRootName != null) {
                if (!this.fPossibleRoots.contains(dtdDesc.fRootName)) {
                    return false;
                }
            } else {
                String root;
                if (dtdDesc.fPossibleRoots == null) {
                    return false;
                }
                boolean found = false;
                Iterator<String> iterator = this.fPossibleRoots.iterator();
                while (iterator.hasNext() && !(found = dtdDesc.fPossibleRoots.contains(root = iterator.next()))) {
                }
                if (!found) {
                    return false;
                }
            }
        }
        if (this.fExpandedSystemId != null ? !this.fExpandedSystemId.equals(dtdDesc.fExpandedSystemId) : dtdDesc.fExpandedSystemId != null) {
            return false;
        }
        return !(this.fPublicId != null ? !this.fPublicId.equals(dtdDesc.fPublicId) : dtdDesc.fPublicId != null);
    }

    @Override
    public int hashCode() {
        if (this.fExpandedSystemId != null) {
            return this.fExpandedSystemId.hashCode();
        }
        if (this.fPublicId != null) {
            return this.fPublicId.hashCode();
        }
        return 0;
    }
}

