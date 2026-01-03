/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public interface Document
extends Node {
    public DocumentType getDoctype();

    public DOMImplementation getImplementation();

    public Element getDocumentElement();

    public Element createElement(String var1) throws DOMException;

    public DocumentFragment createDocumentFragment();

    public Text createTextNode(String var1);

    public Comment createComment(String var1);

    public CDATASection createCDATASection(String var1) throws DOMException;

    public ProcessingInstruction createProcessingInstruction(String var1, String var2) throws DOMException;

    public Attr createAttribute(String var1) throws DOMException;

    public EntityReference createEntityReference(String var1) throws DOMException;

    public NodeList getElementsByTagName(String var1);

    public Node importNode(Node var1, boolean var2) throws DOMException;

    public Element createElementNS(String var1, String var2) throws DOMException;

    public Attr createAttributeNS(String var1, String var2) throws DOMException;

    public NodeList getElementsByTagNameNS(String var1, String var2);

    public Element getElementById(String var1);

    public String getInputEncoding();

    public String getXmlEncoding();

    public boolean getXmlStandalone();

    public void setXmlStandalone(boolean var1) throws DOMException;

    public String getXmlVersion();

    public void setXmlVersion(String var1) throws DOMException;

    public boolean getStrictErrorChecking();

    public void setStrictErrorChecking(boolean var1);

    public String getDocumentURI();

    public void setDocumentURI(String var1);

    public Node adoptNode(Node var1) throws DOMException;

    public DOMConfiguration getDomConfig();

    public void normalizeDocument();

    public Node renameNode(Node var1, String var2, String var3) throws DOMException;
}

