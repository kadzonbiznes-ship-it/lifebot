/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.Vector;
import javax.swing.text.html.parser.Element;

public final class ContentModel
implements Serializable {
    public int type;
    public Object content;
    public ContentModel next;
    private boolean[] valSet;
    private boolean[] val;

    public ContentModel() {
    }

    public ContentModel(Element content) {
        this(0, content, null);
    }

    public ContentModel(int type, ContentModel content) {
        this(type, content, null);
    }

    public ContentModel(int type, Object content, ContentModel next) {
        this.type = type;
        this.content = content;
        this.next = next;
    }

    public boolean empty() {
        switch (this.type) {
            case 42: 
            case 63: {
                return true;
            }
            case 43: 
            case 124: {
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    if (m.empty()) {
                        return true;
                    }
                    m = m.next;
                }
                return false;
            }
            case 38: 
            case 44: {
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    if (!m.empty()) {
                        return false;
                    }
                    m = m.next;
                }
                return true;
            }
        }
        return false;
    }

    public void getElements(Vector<Element> elemVec) {
        switch (this.type) {
            case 42: 
            case 43: 
            case 63: {
                ((ContentModel)this.content).getElements(elemVec);
                break;
            }
            case 38: 
            case 44: 
            case 124: {
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    m.getElements(elemVec);
                    m = m.next;
                }
                break;
            }
            default: {
                elemVec.addElement((Element)this.content);
            }
        }
    }

    public boolean first(Object token) {
        switch (this.type) {
            case 42: 
            case 43: 
            case 63: {
                return ((ContentModel)this.content).first(token);
            }
            case 44: {
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    if (m.first(token)) {
                        return true;
                    }
                    if (!m.empty()) {
                        return false;
                    }
                    m = m.next;
                }
                return false;
            }
            case 38: 
            case 124: {
                Element e = (Element)token;
                if (this.valSet == null || this.valSet.length <= Element.getMaxIndex()) {
                    this.valSet = new boolean[Element.getMaxIndex() + 1];
                    this.val = new boolean[this.valSet.length];
                }
                if (this.valSet[e.index]) {
                    return this.val[e.index];
                }
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    if (m.first(token)) {
                        this.val[e.index] = true;
                        break;
                    }
                    m = m.next;
                }
                this.valSet[e.index] = true;
                return this.val[e.index];
            }
        }
        return this.content == token;
    }

    public Element first() {
        switch (this.type) {
            case 38: 
            case 42: 
            case 63: 
            case 124: {
                return null;
            }
            case 43: 
            case 44: {
                return ((ContentModel)this.content).first();
            }
        }
        return (Element)this.content;
    }

    public String toString() {
        switch (this.type) {
            case 42: {
                return String.valueOf(this.content) + "*";
            }
            case 63: {
                return String.valueOf(this.content) + "?";
            }
            case 43: {
                return String.valueOf(this.content) + "+";
            }
            case 38: 
            case 44: 
            case 124: {
                char[] data = new char[]{' ', (char)this.type, ' '};
                Object str = "";
                ContentModel m = (ContentModel)this.content;
                while (m != null) {
                    str = (String)str + String.valueOf(m);
                    if (m.next != null) {
                        str = (String)str + new String(data);
                    }
                    m = m.next;
                }
                return "(" + (String)str + ")";
            }
        }
        return this.content.toString();
    }
}

