/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public interface Node {
    public static final short ELEMENT_NODE = 1;
    public static final short ATTRIBUTE_NODE = 2;
    public static final short TEXT_NODE = 3;
    public static final short CDATA_SECTION_NODE = 4;
    public static final short ENTITY_REFERENCE_NODE = 5;
    public static final short ENTITY_NODE = 6;
    public static final short PROCESSING_INSTRUCTION_NODE = 7;
    public static final short COMMENT_NODE = 8;
    public static final short DOCUMENT_NODE = 9;
    public static final short DOCUMENT_TYPE_NODE = 10;
    public static final short DOCUMENT_FRAGMENT_NODE = 11;
    public static final short NOTATION_NODE = 12;
    public static final short DOCUMENT_POSITION_DISCONNECTED = 1;
    public static final short DOCUMENT_POSITION_PRECEDING = 2;
    public static final short DOCUMENT_POSITION_FOLLOWING = 4;
    public static final short DOCUMENT_POSITION_CONTAINS = 8;
    public static final short DOCUMENT_POSITION_CONTAINED_BY = 16;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32;

    public String getNodeName();

    public String getNodeValue() throws DOMException;

    public void setNodeValue(String var1) throws DOMException;

    public short getNodeType();

    public Node getParentNode();

    public NodeList getChildNodes();

    public Node getFirstChild();

    public Node getLastChild();

    public Node getPreviousSibling();

    public Node getNextSibling();

    public NamedNodeMap getAttributes();

    public Document getOwnerDocument();

    public Node insertBefore(Node var1, Node var2) throws DOMException;

    public Node replaceChild(Node var1, Node var2) throws DOMException;

    public Node removeChild(Node var1) throws DOMException;

    public Node appendChild(Node var1) throws DOMException;

    public boolean hasChildNodes();

    public Node cloneNode(boolean var1);

    public void normalize();

    public boolean isSupported(String var1, String var2);

    public String getNamespaceURI();

    public String getPrefix();

    public void setPrefix(String var1) throws DOMException;

    public String getLocalName();

    public boolean hasAttributes();

    public String getBaseURI();

    public short compareDocumentPosition(Node var1) throws DOMException;

    public String getTextContent() throws DOMException;

    public void setTextContent(String var1) throws DOMException;

    public boolean isSameNode(Node var1);

    public String lookupPrefix(String var1);

    public boolean isDefaultNamespace(String var1);

    public String lookupNamespaceURI(String var1);

    public boolean isEqualNode(Node var1);

    public Object getFeature(String var1, String var2);

    public Object setUserData(String var1, Object var2, UserDataHandler var3);

    public Object getUserData(String var1);
}

