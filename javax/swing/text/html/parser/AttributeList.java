/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.text.html.parser.DTDConstants;

public final class AttributeList
implements DTDConstants,
Serializable {
    public String name;
    public int type;
    public Vector<?> values;
    public int modifier;
    public String value;
    public AttributeList next;
    static Hashtable<Object, Object> attributeTypes = new Hashtable();

    AttributeList() {
    }

    public AttributeList(String name) {
        this.name = name;
    }

    public AttributeList(String name, int type, int modifier, String value, Vector<?> values, AttributeList next) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.value = value;
        this.values = values;
        this.next = next;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public int getModifier() {
        return this.modifier;
    }

    public Enumeration<?> getValues() {
        return this.values != null ? this.values.elements() : null;
    }

    public String getValue() {
        return this.value;
    }

    public AttributeList getNext() {
        return this.next;
    }

    public String toString() {
        return this.name;
    }

    static void defineAttributeType(String nm, int val) {
        Integer num = val;
        attributeTypes.put(nm, num);
        attributeTypes.put(num, nm);
    }

    public static int name2type(String nm) {
        Integer i = (Integer)attributeTypes.get(nm);
        return i == null ? 1 : i;
    }

    public static String type2name(int tp) {
        return (String)attributeTypes.get(tp);
    }

    static {
        AttributeList.defineAttributeType("CDATA", 1);
        AttributeList.defineAttributeType("ENTITY", 2);
        AttributeList.defineAttributeType("ENTITIES", 3);
        AttributeList.defineAttributeType("ID", 4);
        AttributeList.defineAttributeType("IDREF", 5);
        AttributeList.defineAttributeType("IDREFS", 6);
        AttributeList.defineAttributeType("NAME", 7);
        AttributeList.defineAttributeType("NAMES", 8);
        AttributeList.defineAttributeType("NMTOKEN", 9);
        AttributeList.defineAttributeType("NMTOKENS", 10);
        AttributeList.defineAttributeType("NOTATION", 11);
        AttributeList.defineAttributeType("NUMBER", 12);
        AttributeList.defineAttributeType("NUMBERS", 13);
        AttributeList.defineAttributeType("NUTOKEN", 14);
        AttributeList.defineAttributeType("NUTOKENS", 15);
        attributeTypes.put("fixed", 1);
        attributeTypes.put("required", 2);
        attributeTypes.put("current", 3);
        attributeTypes.put("conref", 4);
        attributeTypes.put("implied", 5);
    }
}

