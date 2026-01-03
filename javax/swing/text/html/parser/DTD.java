/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Entity;
import sun.awt.AppContext;

public class DTD
implements DTDConstants {
    public String name;
    public Vector<Element> elements = new Vector();
    public Hashtable<String, Element> elementHash = new Hashtable();
    public Hashtable<Object, Entity> entityHash = new Hashtable();
    public final Element pcdata = this.getElement("#pcdata");
    public final Element html = this.getElement("html");
    public final Element meta = this.getElement("meta");
    public final Element base = this.getElement("base");
    public final Element isindex = this.getElement("isindex");
    public final Element head = this.getElement("head");
    public final Element body = this.getElement("body");
    public final Element applet = this.getElement("applet");
    public final Element param = this.getElement("param");
    public final Element p = this.getElement("p");
    public final Element title = this.getElement("title");
    final Element style = this.getElement("style");
    final Element link = this.getElement("link");
    final Element script = this.getElement("script");
    public static final int FILE_VERSION = 1;
    private static final Object DTD_HASH_KEY = new Object();

    protected DTD(String name) {
        this.name = name;
        this.defEntity("#RE", 65536, 13);
        this.defEntity("#RS", 65536, 10);
        this.defEntity("#SPACE", 65536, 32);
        this.defineElement("unknown", 17, false, true, null, null, null, null);
    }

    public String getName() {
        return this.name;
    }

    public Entity getEntity(String name) {
        return this.entityHash.get(name);
    }

    public Entity getEntity(int ch) {
        return this.entityHash.get(ch);
    }

    boolean elementExists(String name) {
        return !"unknown".equals(name) && this.elementHash.get(name) != null;
    }

    public Element getElement(String name) {
        Element e = this.elementHash.get(name);
        if (e == null) {
            e = new Element(name, this.elements.size());
            this.elements.addElement(e);
            this.elementHash.put(name, e);
        }
        return e;
    }

    public Element getElement(int index) {
        return this.elements.elementAt(index);
    }

    public Entity defineEntity(String name, int type, char[] data) {
        Entity ent = this.entityHash.get(name);
        if (ent == null) {
            ent = new Entity(name, type, data);
            this.entityHash.put(name, ent);
            if ((type & 0x10000) != 0 && data.length == 1) {
                switch (type & 0xFFFEFFFF) {
                    case 1: 
                    case 11: {
                        this.entityHash.put(data[0], ent);
                    }
                }
            }
        }
        return ent;
    }

    public Element defineElement(String name, int type, boolean omitStart, boolean omitEnd, ContentModel content, BitSet exclusions, BitSet inclusions, AttributeList atts) {
        Element e = this.getElement(name);
        e.type = type;
        e.oStart = omitStart;
        e.oEnd = omitEnd;
        e.content = content;
        e.exclusions = exclusions;
        e.inclusions = inclusions;
        e.atts = atts;
        return e;
    }

    public void defineAttributes(String name, AttributeList atts) {
        Element e = this.getElement(name);
        e.atts = atts;
    }

    public Entity defEntity(String name, int type, int ch) {
        char[] data = new char[]{(char)ch};
        return this.defineEntity(name, type, data);
    }

    protected Entity defEntity(String name, int type, String str) {
        int len = str.length();
        char[] data = new char[len];
        str.getChars(0, len, data, 0);
        return this.defineEntity(name, type, data);
    }

    protected Element defElement(String name, int type, boolean omitStart, boolean omitEnd, ContentModel content, String[] exclusions, String[] inclusions, AttributeList atts) {
        BitSet excl = null;
        if (exclusions != null && exclusions.length > 0) {
            excl = new BitSet();
            for (String str : exclusions) {
                if (str.length() <= 0) continue;
                excl.set(this.getElement(str).getIndex());
            }
        }
        BitSet incl = null;
        if (inclusions != null && inclusions.length > 0) {
            incl = new BitSet();
            for (String str : inclusions) {
                if (str.length() <= 0) continue;
                incl.set(this.getElement(str).getIndex());
            }
        }
        return this.defineElement(name, type, omitStart, omitEnd, content, excl, incl, atts);
    }

    protected AttributeList defAttributeList(String name, int type, int modifier, String value, String values, AttributeList atts) {
        Vector<String> vals = null;
        if (values != null) {
            vals = new Vector<String>();
            StringTokenizer s = new StringTokenizer(values, "|");
            while (s.hasMoreTokens()) {
                String str = s.nextToken();
                if (str.length() <= 0) continue;
                vals.addElement(str);
            }
        }
        return new AttributeList(name, type, modifier, value, vals, atts);
    }

    protected ContentModel defContentModel(int type, Object obj, ContentModel next) {
        return new ContentModel(type, obj, next);
    }

    public String toString() {
        return this.name;
    }

    public static void putDTDHash(String name, DTD dtd) {
        DTD.getDtdHash().put(name, dtd);
    }

    public static DTD getDTD(String name) throws IOException {
        name = name.toLowerCase();
        DTD dtd = DTD.getDtdHash().get(name);
        if (dtd == null) {
            dtd = new DTD(name);
        }
        return dtd;
    }

    private static Hashtable<String, DTD> getDtdHash() {
        AppContext appContext = AppContext.getAppContext();
        Hashtable result = (Hashtable)appContext.get(DTD_HASH_KEY);
        if (result == null) {
            result = new Hashtable();
            appContext.put(DTD_HASH_KEY, result);
        }
        return result;
    }

    public void read(DataInputStream in) throws IOException {
        byte type;
        short nameId;
        int i;
        if (in.readInt() != 1) {
            // empty if block
        }
        String[] names = new String[in.readShort()];
        for (int i2 = 0; i2 < names.length; ++i2) {
            names[i2] = in.readUTF();
        }
        int num = in.readShort();
        for (i = 0; i < num; ++i) {
            nameId = in.readShort();
            type = in.readByte();
            String name = in.readUTF();
            this.defEntity(names[nameId], type | 0x10000, name);
        }
        num = in.readShort();
        for (i = 0; i < num; ++i) {
            nameId = in.readShort();
            type = in.readByte();
            byte flags = in.readByte();
            ContentModel m = this.readContentModel(in, names);
            String[] exclusions = this.readNameArray(in, names);
            String[] inclusions = this.readNameArray(in, names);
            AttributeList atts = this.readAttributeList(in, names);
            this.defElement(names[nameId], type, (flags & 1) != 0, (flags & 2) != 0, m, exclusions, inclusions, atts);
        }
    }

    private ContentModel readContentModel(DataInputStream in, String[] names) throws IOException {
        byte flag = in.readByte();
        switch (flag) {
            case 0: {
                return null;
            }
            case 1: {
                byte type = in.readByte();
                ContentModel m = this.readContentModel(in, names);
                ContentModel next = this.readContentModel(in, names);
                return this.defContentModel(type, m, next);
            }
            case 2: {
                byte type = in.readByte();
                Element el = this.getElement(names[in.readShort()]);
                ContentModel next = this.readContentModel(in, names);
                return this.defContentModel(type, el, next);
            }
        }
        throw new IOException("bad bdtd");
    }

    private String[] readNameArray(DataInputStream in, String[] names) throws IOException {
        int num = in.readShort();
        if (num == 0) {
            return null;
        }
        String[] result = new String[num];
        for (int i = 0; i < num; ++i) {
            result[i] = names[in.readShort()];
        }
        return result;
    }

    private AttributeList readAttributeList(DataInputStream in, String[] names) throws IOException {
        AttributeList result = null;
        for (int num = in.readByte(); num > 0; --num) {
            short nameId = in.readShort();
            byte type = in.readByte();
            byte modifier = in.readByte();
            short valueId = in.readShort();
            String value = valueId == -1 ? null : names[valueId];
            Vector<String> values = null;
            int numValues = in.readShort();
            if (numValues > 0) {
                values = new Vector<String>(numValues);
                for (int i = 0; i < numValues; ++i) {
                    values.addElement(names[in.readShort()]);
                }
            }
            result = new AttributeList(names[nameId], type, modifier, value, values, result);
        }
        return result;
    }
}

