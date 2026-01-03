/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.util.BitSet;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.ContentModelState;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.TagElement;

final class TagStack
implements DTDConstants {
    TagElement tag;
    Element elem;
    ContentModelState state;
    TagStack next;
    BitSet inclusions;
    BitSet exclusions;
    boolean net;
    boolean pre;

    TagStack(TagElement tag, TagStack next) {
        this.tag = tag;
        this.elem = tag.getElement();
        this.next = next;
        Element elem = tag.getElement();
        if (elem.getContent() != null) {
            this.state = new ContentModelState(elem.getContent());
        }
        if (next != null) {
            this.inclusions = next.inclusions;
            this.exclusions = next.exclusions;
            this.pre = next.pre;
        }
        if (tag.isPreformatted()) {
            this.pre = true;
        }
        if (elem.inclusions != null) {
            if (this.inclusions != null) {
                this.inclusions = (BitSet)this.inclusions.clone();
                this.inclusions.or(elem.inclusions);
            } else {
                this.inclusions = elem.inclusions;
            }
        }
        if (elem.exclusions != null) {
            if (this.exclusions != null) {
                this.exclusions = (BitSet)this.exclusions.clone();
                this.exclusions.or(elem.exclusions);
            } else {
                this.exclusions = elem.exclusions;
            }
        }
    }

    public Element first() {
        return this.state != null ? this.state.first() : null;
    }

    public ContentModel contentModel() {
        if (this.state == null) {
            return null;
        }
        return this.state.getModel();
    }

    boolean excluded(int elemIndex) {
        return this.exclusions != null && this.exclusions.get(this.elem.getIndex());
    }

    boolean advance(Element elem) {
        if (this.exclusions != null && this.exclusions.get(elem.getIndex())) {
            return false;
        }
        if (this.state != null) {
            ContentModelState newState = this.state.advance(elem);
            if (newState != null) {
                this.state = newState;
                return true;
            }
        } else if (this.elem.getType() == 19) {
            return true;
        }
        return this.inclusions != null && this.inclusions.get(elem.getIndex());
    }

    boolean terminate() {
        return this.state == null || this.state.terminate();
    }

    public String toString() {
        return this.next == null ? "<" + this.tag.getElement().getName() + ">" : String.valueOf(this.next) + " <" + this.tag.getElement().getName() + ">";
    }
}

