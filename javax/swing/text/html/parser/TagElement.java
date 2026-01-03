/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.Element;

public class TagElement {
    Element elem;
    HTML.Tag htmlTag;
    boolean insertedByErrorRecovery;

    public TagElement(Element elem) {
        this(elem, false);
    }

    public TagElement(Element elem, boolean fictional) {
        this.elem = elem;
        this.htmlTag = HTML.getTag(elem.getName());
        if (this.htmlTag == null) {
            this.htmlTag = new HTML.UnknownTag(elem.getName());
        }
        this.insertedByErrorRecovery = fictional;
    }

    public boolean breaksFlow() {
        return this.htmlTag.breaksFlow();
    }

    public boolean isPreformatted() {
        return this.htmlTag.isPreformatted();
    }

    public Element getElement() {
        return this.elem;
    }

    public HTML.Tag getHTMLTag() {
        return this.htmlTag;
    }

    public boolean fictional() {
        return this.insertedByErrorRecovery;
    }
}

