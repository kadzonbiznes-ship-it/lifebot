/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.namespace;

import java.util.Iterator;

public interface NamespaceContext {
    public String getNamespaceURI(String var1);

    public String getPrefix(String var1);

    public Iterator<String> getPrefixes(String var1);
}

