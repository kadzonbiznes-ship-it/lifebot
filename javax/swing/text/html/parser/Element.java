/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Hashtable;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTDConstants;
import sun.awt.AppContext;

public final class Element
implements DTDConstants,
Serializable {
    public int index;
    public String name;
    public boolean oStart;
    public boolean oEnd;
    public BitSet inclusions;
    public BitSet exclusions;
    public int type = 19;
    public ContentModel content;
    public AttributeList atts;
    public Object data;
    private static final Object MAX_INDEX_KEY = new Object();
    static Hashtable<String, Integer> contentTypes = new Hashtable();

    Element() {
    }

    Element(String name, int index) {
        this.name = name;
        this.index = index;
        if (index > Element.getMaxIndex()) {
            AppContext.getAppContext().put(MAX_INDEX_KEY, index);
        }
    }

    static int getMaxIndex() {
        Integer value = (Integer)AppContext.getAppContext().get(MAX_INDEX_KEY);
        return value != null ? value : 0;
    }

    public String getName() {
        return this.name;
    }

    public boolean omitStart() {
        return this.oStart;
    }

    public boolean omitEnd() {
        return this.oEnd;
    }

    public int getType() {
        return this.type;
    }

    public ContentModel getContent() {
        return this.content;
    }

    public AttributeList getAttributes() {
        return this.atts;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean isEmpty() {
        return this.type == 17;
    }

    public String toString() {
        return this.name;
    }

    public AttributeList getAttribute(String name) {
        AttributeList a = this.atts;
        while (a != null) {
            if (a.name.equals(name)) {
                return a;
            }
            a = a.next;
        }
        return null;
    }

    public AttributeList getAttributeByValue(String value) {
        AttributeList a = this.atts;
        while (a != null) {
            if (a.values != null && a.values.contains(value)) {
                return a;
            }
            a = a.next;
        }
        return null;
    }

    public static int name2type(String nm) {
        Integer val = contentTypes.get(nm);
        return val != null ? val : 0;
    }

    static {
        contentTypes.put("CDATA", 1);
        contentTypes.put("RCDATA", 16);
        contentTypes.put("EMPTY", 17);
        contentTypes.put("ANY", 19);
    }
}

