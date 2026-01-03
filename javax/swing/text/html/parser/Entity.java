/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.util.Hashtable;
import javax.swing.text.html.parser.DTDConstants;

public final class Entity
implements DTDConstants {
    public String name;
    public int type;
    public char[] data;
    static Hashtable<String, Integer> entityTypes = new Hashtable();

    public Entity(String name, int type, char[] data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type & 0xFFFF;
    }

    public boolean isParameter() {
        return (this.type & 0x40000) != 0;
    }

    public boolean isGeneral() {
        return (this.type & 0x10000) != 0;
    }

    public char[] getData() {
        return this.data;
    }

    public String getString() {
        return new String(this.data);
    }

    public static int name2type(String nm) {
        Integer i = entityTypes.get(nm);
        return i == null ? 1 : i;
    }

    static {
        entityTypes.put("PUBLIC", 10);
        entityTypes.put("CDATA", 1);
        entityTypes.put("SDATA", 11);
        entityTypes.put("PI", 12);
        entityTypes.put("STARTTAG", 13);
        entityTypes.put("ENDTAG", 14);
        entityTypes.put("MS", 15);
        entityTypes.put("MD", 16);
        entityTypes.put("SYSTEM", 17);
    }
}

