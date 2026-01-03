/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Parser;
import javax.swing.text.html.parser.TagElement;

public class DocumentParser
extends Parser {
    private int inbody;
    private int intitle;
    private int inhead;
    private int instyle;
    private int inscript;
    private boolean seentitle;
    private HTMLEditorKit.ParserCallback callback = null;
    private boolean ignoreCharSet = false;
    private static final boolean debugFlag = false;

    public DocumentParser(DTD dtd) {
        super(dtd);
    }

    public void parse(Reader in, HTMLEditorKit.ParserCallback callback, boolean ignoreCharSet) throws IOException {
        this.ignoreCharSet = ignoreCharSet;
        this.callback = callback;
        this.parse(in);
        callback.handleEndOfLineString(this.getEndOfLineString());
    }

    @Override
    protected void handleStartTag(TagElement tag) {
        Element elem = tag.getElement();
        if (elem == this.dtd.body) {
            ++this.inbody;
        } else if (elem != this.dtd.html) {
            if (elem == this.dtd.head) {
                ++this.inhead;
            } else if (elem == this.dtd.title) {
                ++this.intitle;
            } else if (elem == this.dtd.style) {
                ++this.instyle;
            } else if (elem == this.dtd.script) {
                ++this.inscript;
            }
        }
        if (tag.fictional()) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(HTMLEditorKit.ParserCallback.IMPLIED, Boolean.TRUE);
            this.callback.handleStartTag(tag.getHTMLTag(), attrs, this.getBlockStartPosition());
        } else {
            this.callback.handleStartTag(tag.getHTMLTag(), this.getAttributes(), this.getBlockStartPosition());
            this.flushAttributes();
        }
    }

    @Override
    protected void handleComment(char[] text) {
        this.callback.handleComment(text, this.getBlockStartPosition());
    }

    @Override
    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        String content;
        SimpleAttributeSet atts;
        Element elem = tag.getElement();
        if (elem == this.dtd.meta && !this.ignoreCharSet && (atts = this.getAttributes()) != null && (content = (String)atts.getAttribute(HTML.Attribute.CONTENT)) != null) {
            if ("content-type".equalsIgnoreCase((String)atts.getAttribute(HTML.Attribute.HTTPEQUIV))) {
                if (!content.equalsIgnoreCase("text/html") && !content.equalsIgnoreCase("text/plain")) {
                    throw new ChangedCharSetException(content, false);
                }
            } else if ("charset".equalsIgnoreCase((String)atts.getAttribute(HTML.Attribute.HTTPEQUIV))) {
                throw new ChangedCharSetException(content, true);
            }
        }
        if (this.inbody != 0 || elem == this.dtd.meta || elem == this.dtd.base || elem == this.dtd.isindex || elem == this.dtd.style || elem == this.dtd.link) {
            if (tag.fictional()) {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                attrs.addAttribute(HTMLEditorKit.ParserCallback.IMPLIED, Boolean.TRUE);
                this.callback.handleSimpleTag(tag.getHTMLTag(), attrs, this.getBlockStartPosition());
            } else {
                this.callback.handleSimpleTag(tag.getHTMLTag(), this.getAttributes(), this.getBlockStartPosition());
                this.flushAttributes();
            }
        }
    }

    @Override
    protected void handleEndTag(TagElement tag) {
        Element elem = tag.getElement();
        if (elem == this.dtd.body) {
            --this.inbody;
        } else if (elem == this.dtd.title) {
            --this.intitle;
            this.seentitle = true;
        } else if (elem == this.dtd.head) {
            --this.inhead;
        } else if (elem == this.dtd.style) {
            --this.instyle;
        } else if (elem == this.dtd.script) {
            --this.inscript;
        }
        this.callback.handleEndTag(tag.getHTMLTag(), this.getBlockStartPosition());
    }

    @Override
    protected void handleText(char[] data) {
        if (data != null) {
            if (this.inscript != 0) {
                this.callback.handleComment(data, this.getBlockStartPosition());
                return;
            }
            if (this.inbody != 0 || this.instyle != 0 || this.intitle != 0 && !this.seentitle) {
                this.callback.handleText(data, this.getBlockStartPosition());
            }
        }
    }

    @Override
    protected void handleError(int ln, String errorMsg) {
        this.callback.handleError(errorMsg, this.getCurrentPos());
    }

    private void debug(String msg) {
        System.out.println(msg);
    }
}

