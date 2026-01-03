/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.Element;

class ContentModelState {
    ContentModel model;
    long value;
    ContentModelState next;

    public ContentModelState(ContentModel model) {
        this(model, null, 0L);
    }

    ContentModelState(Object content, ContentModelState next) {
        this(content, next, 0L);
    }

    ContentModelState(Object content, ContentModelState next, long value) {
        this.model = (ContentModel)content;
        this.next = next;
        this.value = value;
    }

    public ContentModel getModel() {
        ContentModel m = this.model;
        int i = 0;
        while ((long)i < this.value) {
            if (m.next == null) {
                return null;
            }
            m = m.next;
            ++i;
        }
        return m;
    }

    public boolean terminate() {
        switch (this.model.type) {
            case 43: {
                if (this.value == 0L && !this.model.empty()) {
                    return false;
                }
            }
            case 42: 
            case 63: {
                return this.next == null || this.next.terminate();
            }
            case 124: {
                ContentModel m = (ContentModel)this.model.content;
                while (m != null) {
                    if (m.empty()) {
                        return this.next == null || this.next.terminate();
                    }
                    m = m.next;
                }
                return false;
            }
            case 38: {
                ContentModel m = (ContentModel)this.model.content;
                int i = 0;
                while (m != null) {
                    if ((this.value & 1L << i) == 0L && !m.empty()) {
                        return false;
                    }
                    ++i;
                    m = m.next;
                }
                return this.next == null || this.next.terminate();
            }
            case 44: {
                ContentModel m = (ContentModel)this.model.content;
                int i = 0;
                while ((long)i < this.value) {
                    ++i;
                    m = m.next;
                }
                while (m != null && m.empty()) {
                    m = m.next;
                }
                if (m != null) {
                    return false;
                }
                return this.next == null || this.next.terminate();
            }
        }
        return false;
    }

    public Element first() {
        switch (this.model.type) {
            case 38: 
            case 42: 
            case 63: 
            case 124: {
                return null;
            }
            case 43: {
                return this.model.first();
            }
            case 44: {
                ContentModel m = (ContentModel)this.model.content;
                int i = 0;
                while ((long)i < this.value) {
                    ++i;
                    m = m.next;
                }
                return m.first();
            }
        }
        return this.model.first();
    }

    public ContentModelState advance(Object token) {
        switch (this.model.type) {
            case 43: {
                if (this.model.first(token)) {
                    return new ContentModelState(this.model.content, new ContentModelState(this.model, this.next, this.value + 1L)).advance(token);
                }
                if (this.value == 0L) break;
                if (this.next != null) {
                    return this.next.advance(token);
                }
                return null;
            }
            case 42: {
                if (this.model.first(token)) {
                    return new ContentModelState(this.model.content, this).advance(token);
                }
                if (this.next != null) {
                    return this.next.advance(token);
                }
                return null;
            }
            case 63: {
                if (this.model.first(token)) {
                    return new ContentModelState(this.model.content, this.next).advance(token);
                }
                if (this.next != null) {
                    return this.next.advance(token);
                }
                return null;
            }
            case 124: {
                ContentModel m = (ContentModel)this.model.content;
                while (m != null) {
                    if (m.first(token)) {
                        return new ContentModelState(m, this.next).advance(token);
                    }
                    m = m.next;
                }
                break;
            }
            case 44: {
                ContentModel m = (ContentModel)this.model.content;
                int i = 0;
                while ((long)i < this.value) {
                    ++i;
                    m = m.next;
                }
                if (!m.first(token) && !m.empty()) break;
                if (m.next == null) {
                    return new ContentModelState(m, this.next).advance(token);
                }
                return new ContentModelState(m, new ContentModelState(this.model, this.next, this.value + 1L)).advance(token);
            }
            case 38: {
                ContentModel m = (ContentModel)this.model.content;
                boolean complete = true;
                int i = 0;
                while (m != null) {
                    if ((this.value & 1L << i) == 0L) {
                        if (m.first(token)) {
                            return new ContentModelState(m, new ContentModelState(this.model, this.next, this.value | 1L << i)).advance(token);
                        }
                        if (!m.empty()) {
                            complete = false;
                        }
                    }
                    ++i;
                    m = m.next;
                }
                if (!complete) break;
                if (this.next != null) {
                    return this.next.advance(token);
                }
                return null;
            }
            default: {
                if (this.model.content != token) break;
                if (this.next == null && token instanceof Element && ((Element)token).content != null) {
                    return new ContentModelState(((Element)token).content);
                }
                return this.next;
            }
        }
        return null;
    }
}

