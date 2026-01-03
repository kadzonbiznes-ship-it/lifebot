/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import javax.xml.stream.events.Attribute;

public interface Namespace
extends Attribute {
    public String getPrefix();

    public String getNamespaceURI();

    public boolean isDefaultNamespaceDeclaration();
}

