/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html.parser;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Entity;
import javax.swing.text.html.parser.TagElement;
import javax.swing.text.html.parser.TagStack;

public class Parser
implements DTDConstants {
    private char[] text = new char[1024];
    private int textpos = 0;
    private TagElement last;
    private boolean space;
    private char[] str = new char[128];
    private int strpos = 0;
    protected DTD dtd = null;
    private int ch;
    private int ln;
    private Reader in;
    private Element recent;
    private TagStack stack;
    private boolean skipTag = false;
    private TagElement lastFormSent = null;
    private SimpleAttributeSet attributes = new SimpleAttributeSet();
    private boolean seenHtml = false;
    private boolean seenHead = false;
    private boolean seenBody = false;
    private boolean ignoreSpace;
    protected boolean strict = false;
    private int crlfCount;
    private int crCount;
    private int lfCount;
    private int currentBlockStartPos;
    private int lastBlockStartPos;
    private static final char[] cp1252Map = new char[]{'\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u008d', '\u008e', '\u008f', '\u0090', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\u009d', '\u009e', '\u0178'};
    private static final String START_COMMENT = "<!--";
    private static final String END_COMMENT = "-->";
    private static final char[] SCRIPT_END_TAG = "</script>".toCharArray();
    private static final char[] SCRIPT_END_TAG_UPPER_CASE = "</SCRIPT>".toCharArray();
    private char[] buf = new char[1];
    private int pos;
    private int len;
    private int currentPosition;

    public Parser(DTD dtd) {
        this.dtd = dtd;
    }

    protected int getCurrentLine() {
        return this.ln;
    }

    int getBlockStartPosition() {
        return Math.max(0, this.lastBlockStartPos - 1);
    }

    protected TagElement makeTag(Element elem, boolean fictional) {
        return new TagElement(elem, fictional);
    }

    protected TagElement makeTag(Element elem) {
        return this.makeTag(elem, false);
    }

    protected SimpleAttributeSet getAttributes() {
        return this.attributes;
    }

    protected void flushAttributes() {
        this.attributes.removeAttributes(this.attributes);
    }

    protected void handleText(char[] text) {
    }

    protected void handleTitle(char[] text) {
        this.handleText(text);
    }

    protected void handleComment(char[] text) {
    }

    protected void handleEOFInComment() {
        int commentEndPos = this.strIndexOf('\n');
        if (commentEndPos >= 0) {
            this.handleComment(this.getChars(0, commentEndPos));
            try {
                this.in.close();
                this.in = new CharArrayReader(this.getChars(commentEndPos + 1));
                this.ch = 62;
            }
            catch (IOException e) {
                this.error("ioexception");
            }
            this.resetStrBuffer();
        } else {
            this.error("eof.comment");
        }
    }

    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
    }

    protected void handleStartTag(TagElement tag) {
    }

    protected void handleEndTag(TagElement tag) {
    }

    protected void handleError(int ln, String msg) {
    }

    void handleText(TagElement tag) {
        char[] newtext;
        if (tag.breaksFlow()) {
            this.space = false;
            if (!this.strict) {
                this.ignoreSpace = true;
            }
        }
        if (!(this.textpos != 0 || this.space && this.stack != null && !this.last.breaksFlow() && this.stack.advance(this.dtd.pcdata))) {
            this.last = tag;
            this.space = false;
            this.lastBlockStartPos = this.currentBlockStartPos;
            return;
        }
        if (this.space) {
            if (!this.ignoreSpace) {
                if (this.textpos + 1 > this.text.length) {
                    newtext = new char[this.text.length + 200];
                    System.arraycopy(this.text, 0, newtext, 0, this.text.length);
                    this.text = newtext;
                }
                this.text[this.textpos++] = 32;
                if (!this.strict && !tag.getElement().isEmpty()) {
                    this.ignoreSpace = true;
                }
            }
            this.space = false;
        }
        newtext = new char[this.textpos];
        System.arraycopy(this.text, 0, newtext, 0, this.textpos);
        if (tag.getElement().getName().equals("title")) {
            this.handleTitle(newtext);
        } else {
            this.handleText(newtext);
        }
        this.lastBlockStartPos = this.currentBlockStartPos;
        this.textpos = 0;
        this.last = tag;
        this.space = false;
    }

    protected void error(String err, String arg1, String arg2, String arg3) {
        this.handleError(this.ln, err + " " + arg1 + " " + arg2 + " " + arg3);
    }

    protected void error(String err, String arg1, String arg2) {
        this.error(err, arg1, arg2, "?");
    }

    protected void error(String err, String arg1) {
        this.error(err, arg1, "?", "?");
    }

    protected void error(String err) {
        this.error(err, "?", "?", "?");
    }

    protected void startTag(TagElement tag) throws ChangedCharSetException {
        Element elem = tag.getElement();
        if (!elem.isEmpty() || this.last != null && !this.last.breaksFlow() || this.textpos != 0) {
            this.handleText(tag);
        } else {
            this.last = tag;
            this.space = false;
        }
        this.lastBlockStartPos = this.currentBlockStartPos;
        AttributeList a = elem.atts;
        while (a != null) {
            if (a.modifier == 2 && (this.attributes.isEmpty() || !this.attributes.isDefined(a.name) && !this.attributes.isDefined(HTML.getAttributeKey(a.name)))) {
                this.error("req.att ", a.getName(), elem.getName());
            }
            a = a.next;
        }
        if (elem.isEmpty()) {
            this.handleEmptyTag(tag);
        } else {
            this.recent = elem;
            this.stack = new TagStack(tag, this.stack);
            this.handleStartTag(tag);
        }
    }

    protected void endTag(boolean omitted) {
        this.handleText(this.stack.tag);
        if (omitted && !this.stack.elem.omitEnd()) {
            this.error("end.missing", this.stack.elem.getName());
        } else if (!this.stack.terminate()) {
            this.error("end.unexpected", this.stack.elem.getName());
        }
        this.handleEndTag(this.stack.tag);
        this.stack = this.stack.next;
        this.recent = this.stack != null ? this.stack.elem : null;
    }

    boolean ignoreElement(Element elem) {
        String stackElement = this.stack.elem.getName();
        String elemName = elem.getName();
        if (elemName.equals("html") && this.seenHtml || elemName.equals("head") && this.seenHead || elemName.equals("body") && this.seenBody) {
            return true;
        }
        if (elemName.equals("dt") || elemName.equals("dd")) {
            TagStack s = this.stack;
            while (s != null && !s.elem.getName().equals("dl")) {
                s = s.next;
            }
            if (s == null) {
                return true;
            }
        }
        return stackElement.equals("table") && !elemName.equals("#pcdata") && !elemName.equals("input") || elemName.equals("font") && (stackElement.equals("ul") || stackElement.equals("ol")) || elemName.equals("meta") && this.stack != null || elemName.equals("style") && this.seenBody || stackElement.equals("table") && elemName.equals("a");
    }

    protected void markFirstTime(Element elem) {
        String elemName = elem.getName();
        if (elemName.equals("html")) {
            this.seenHtml = true;
        } else if (elemName.equals("head")) {
            this.seenHead = true;
        } else if (elemName.equals("body")) {
            if (this.buf.length == 1) {
                char[] newBuf = new char[256];
                newBuf[0] = this.buf[0];
                this.buf = newBuf;
            }
            this.seenBody = true;
        }
    }

    boolean legalElementContext(Element elem) throws ChangedCharSetException {
        Element next;
        if (this.stack == null) {
            if (elem != this.dtd.html) {
                this.startTag(this.makeTag(this.dtd.html, true));
                return this.legalElementContext(elem);
            }
            return true;
        }
        if (this.stack.advance(elem)) {
            this.markFirstTime(elem);
            return true;
        }
        boolean insertTag = false;
        String stackElemName = this.stack.elem.getName();
        String elemName = elem.getName();
        if (!this.strict && (stackElemName.equals("table") && elemName.equals("td") || stackElemName.equals("table") && elemName.equals("th") || stackElemName.equals("tr") && !elemName.equals("tr"))) {
            insertTag = true;
        }
        if (!this.strict && !insertTag && (this.stack.elem.getName() != elem.getName() || elem.getName().equals("body")) && (this.skipTag = this.ignoreElement(elem))) {
            this.error("tag.ignore", elem.getName());
            return this.skipTag;
        }
        if (!(this.strict || !stackElemName.equals("table") || elemName.equals("tr") || elemName.equals("td") || elemName.equals("th") || elemName.equals("caption"))) {
            Element e = this.dtd.getElement("tr");
            TagElement t = this.makeTag(e, true);
            this.legalTagContext(t);
            this.startTag(t);
            this.error("start.missing", elem.getName());
            return this.legalElementContext(elem);
        }
        if (!insertTag && this.stack.terminate() && (!this.strict || this.stack.elem.omitEnd())) {
            TagStack s = this.stack.next;
            while (s != null) {
                if (s.advance(elem)) {
                    while (this.stack != s) {
                        this.endTag(true);
                    }
                    return true;
                }
                if (!s.terminate() || this.strict && !s.elem.omitEnd()) break;
                if (s.terminate() && !s.elem.omitEnd()) {
                    return false;
                }
                s = s.next;
            }
        }
        if (!((next = this.stack.first()) == null || this.strict && !next.omitStart() || next == this.dtd.head && elem == this.dtd.pcdata)) {
            TagElement t = this.makeTag(next, true);
            this.legalTagContext(t);
            this.startTag(t);
            if (!next.omitStart()) {
                this.error("start.missing", elem.getName());
            }
            return this.legalElementContext(elem);
        }
        if (!this.strict) {
            ContentModel content = this.stack.contentModel();
            Vector<Element> elemVec = new Vector<Element>();
            if (content != null) {
                content.getElements(elemVec);
                for (Element e : elemVec) {
                    ContentModel m;
                    if (this.stack.excluded(e.getIndex())) continue;
                    boolean reqAtts = false;
                    AttributeList a = e.getAttributes();
                    while (a != null) {
                        if (a.modifier == 2) {
                            reqAtts = true;
                            break;
                        }
                        a = a.next;
                    }
                    if (reqAtts || (m = e.getContent()) == null || !m.first(elem)) continue;
                    TagElement t = this.makeTag(e, true);
                    this.legalTagContext(t);
                    this.startTag(t);
                    this.error("start.missing", e.getName());
                    return this.legalElementContext(elem);
                }
            }
        }
        if (this.stack.terminate() && this.stack.elem != this.dtd.body && (!this.strict || this.stack.elem.omitEnd())) {
            if (!this.stack.elem.omitEnd()) {
                this.error("end.missing", elem.getName());
            }
            this.endTag(true);
            return this.legalElementContext(elem);
        }
        return false;
    }

    void legalTagContext(TagElement tag) throws ChangedCharSetException {
        if (this.legalElementContext(tag.getElement())) {
            this.markFirstTime(tag.getElement());
            return;
        }
        if (tag.breaksFlow() && this.stack != null && !this.stack.tag.breaksFlow()) {
            this.endTag(true);
            this.legalTagContext(tag);
            return;
        }
        TagStack s = this.stack;
        while (s != null) {
            if (s.tag.getElement() == this.dtd.head) {
                while (this.stack != s) {
                    this.endTag(true);
                }
                this.endTag(true);
                this.legalTagContext(tag);
                return;
            }
            s = s.next;
        }
        this.error("tag.unexpected", tag.getElement().getName());
    }

    void errorContext() throws ChangedCharSetException {
        while (this.stack != null && this.stack.tag.getElement() != this.dtd.body) {
            this.handleEndTag(this.stack.tag);
            this.stack = this.stack.next;
        }
        if (this.stack == null) {
            this.legalElementContext(this.dtd.body);
            this.startTag(this.makeTag(this.dtd.body, true));
        }
    }

    void addString(int c) {
        if (this.strpos == this.str.length) {
            char[] newstr = new char[this.str.length + 128];
            System.arraycopy(this.str, 0, newstr, 0, this.str.length);
            this.str = newstr;
        }
        this.str[this.strpos++] = (char)c;
    }

    String getString(int pos) {
        char[] newStr = new char[this.strpos - pos];
        System.arraycopy(this.str, pos, newStr, 0, this.strpos - pos);
        this.strpos = pos;
        return new String(newStr);
    }

    char[] getChars(int pos) {
        char[] newStr = new char[this.strpos - pos];
        System.arraycopy(this.str, pos, newStr, 0, this.strpos - pos);
        this.strpos = pos;
        return newStr;
    }

    char[] getChars(int pos, int endPos) {
        char[] newStr = new char[endPos - pos];
        System.arraycopy(this.str, pos, newStr, 0, endPos - pos);
        return newStr;
    }

    void resetStrBuffer() {
        this.strpos = 0;
    }

    int strIndexOf(char target) {
        for (int i = 0; i < this.strpos; ++i) {
            if (this.str[i] != target) continue;
            return i;
        }
        return -1;
    }

    void skipSpace() throws IOException {
        block5: while (true) {
            switch (this.ch) {
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    continue block5;
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                        continue block5;
                    }
                    ++this.crCount;
                    continue block5;
                }
                case 9: 
                case 32: {
                    this.ch = this.readCh();
                    continue block5;
                }
            }
            break;
        }
    }

    boolean parseIdentifier(boolean lower) throws IOException {
        switch (this.ch) {
            case 65: 
            case 66: 
            case 67: 
            case 68: 
            case 69: 
            case 70: 
            case 71: 
            case 72: 
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: 
            case 78: 
            case 79: 
            case 80: 
            case 81: 
            case 82: 
            case 83: 
            case 84: 
            case 85: 
            case 86: 
            case 87: 
            case 88: 
            case 89: 
            case 90: {
                if (!lower) break;
                this.ch = 97 + (this.ch - 65);
                break;
            }
            case 97: 
            case 98: 
            case 99: 
            case 100: 
            case 101: 
            case 102: 
            case 103: 
            case 104: 
            case 105: 
            case 106: 
            case 107: 
            case 108: 
            case 109: 
            case 110: 
            case 111: 
            case 112: 
            case 113: 
            case 114: 
            case 115: 
            case 116: 
            case 117: 
            case 118: 
            case 119: 
            case 120: 
            case 121: 
            case 122: {
                break;
            }
            default: {
                return false;
            }
        }
        block8: while (true) {
            this.addString(this.ch);
            this.ch = this.readCh();
            switch (this.ch) {
                case 65: 
                case 66: 
                case 67: 
                case 68: 
                case 69: 
                case 70: 
                case 71: 
                case 72: 
                case 73: 
                case 74: 
                case 75: 
                case 76: 
                case 77: 
                case 78: 
                case 79: 
                case 80: 
                case 81: 
                case 82: 
                case 83: 
                case 84: 
                case 85: 
                case 86: 
                case 87: 
                case 88: 
                case 89: 
                case 90: {
                    if (!lower) continue block8;
                    this.ch = 97 + (this.ch - 65);
                    continue block8;
                }
                case 45: 
                case 46: 
                case 48: 
                case 49: 
                case 50: 
                case 51: 
                case 52: 
                case 53: 
                case 54: 
                case 55: 
                case 56: 
                case 57: 
                case 95: 
                case 97: 
                case 98: 
                case 99: 
                case 100: 
                case 101: 
                case 102: 
                case 103: 
                case 104: 
                case 105: 
                case 106: 
                case 107: 
                case 108: 
                case 109: 
                case 110: 
                case 111: 
                case 112: 
                case 113: 
                case 114: 
                case 115: 
                case 116: 
                case 117: 
                case 118: 
                case 119: 
                case 120: 
                case 121: 
                case 122: {
                    continue block8;
                }
            }
            break;
        }
        return true;
    }

    private char[] parseEntityReference() throws IOException {
        int pos = this.strpos;
        this.ch = this.readCh();
        if (this.ch == 35) {
            int n = 0;
            this.ch = this.readCh();
            if (this.ch >= 48 && this.ch <= 57 || this.ch == 120 || this.ch == 88) {
                if (this.ch >= 48 && this.ch <= 57) {
                    while (this.ch >= 48 && this.ch <= 57) {
                        n = n * 10 + this.ch - 48;
                        this.ch = this.readCh();
                    }
                } else {
                    this.ch = this.readCh();
                    char lch = (char)Character.toLowerCase(this.ch);
                    while (lch >= '0' && lch <= '9' || lch >= 'a' && lch <= 'f') {
                        n = lch >= '0' && lch <= '9' ? n * 16 + lch - 48 : n * 16 + lch - 97 + 10;
                        this.ch = this.readCh();
                        lch = (char)Character.toLowerCase(this.ch);
                    }
                }
                switch (this.ch) {
                    case 10: {
                        ++this.ln;
                        this.ch = this.readCh();
                        ++this.lfCount;
                        break;
                    }
                    case 13: {
                        ++this.ln;
                        this.ch = this.readCh();
                        if (this.ch == 10) {
                            this.ch = this.readCh();
                            ++this.crlfCount;
                            break;
                        }
                        ++this.crCount;
                        break;
                    }
                    case 59: {
                        this.ch = this.readCh();
                    }
                }
                char[] data = this.mapNumericReference(n);
                return data;
            }
            this.addString(35);
            if (!this.parseIdentifier(false)) {
                this.error("ident.expected");
                this.strpos = pos;
                char[] data = new char[]{'&', '#'};
                return data;
            }
        } else if (!this.parseIdentifier(false)) {
            char[] data = new char[]{'&'};
            return data;
        }
        boolean semicolon = false;
        switch (this.ch) {
            case 10: {
                ++this.ln;
                this.ch = this.readCh();
                ++this.lfCount;
                break;
            }
            case 13: {
                ++this.ln;
                this.ch = this.readCh();
                if (this.ch == 10) {
                    this.ch = this.readCh();
                    ++this.crlfCount;
                    break;
                }
                ++this.crCount;
                break;
            }
            case 59: {
                semicolon = true;
                this.ch = this.readCh();
            }
        }
        String nm = this.getString(pos);
        Entity ent = this.dtd.getEntity(nm);
        if (!this.strict && ent == null) {
            ent = this.dtd.getEntity(nm.toLowerCase());
        }
        if (ent == null || !ent.isGeneral()) {
            if (nm.length() == 0) {
                this.error("invalid.entref", nm);
                return new char[0];
            }
            String str = "&" + nm + (semicolon ? ";" : "");
            char[] b = new char[str.length()];
            str.getChars(0, b.length, b, 0);
            return b;
        }
        return ent.getData();
    }

    private char[] mapNumericReference(int c) {
        char[] data;
        if (c >= 65535) {
            try {
                data = Character.toChars(c);
            }
            catch (IllegalArgumentException e) {
                data = new char[]{};
            }
        } else {
            data = new char[]{c < 130 || c > 159 ? (char)c : cp1252Map[c - 130]};
        }
        return data;
    }

    void parseComment() throws IOException {
        block7: while (true) {
            int c = this.ch;
            switch (c) {
                case 45: {
                    if (!this.strict && this.strpos != 0 && this.str[this.strpos - 1] == '-') {
                        this.ch = this.readCh();
                        if (this.ch == 62) {
                            return;
                        }
                        if (this.ch != 33) break;
                        this.ch = this.readCh();
                        if (this.ch == 62) {
                            return;
                        }
                        this.addString(45);
                        this.addString(33);
                        continue block7;
                    }
                    this.ch = this.readCh();
                    if (this.ch != 45) break;
                    this.ch = this.readCh();
                    if (this.strict || this.ch == 62) {
                        return;
                    }
                    if (this.ch == 33) {
                        this.ch = this.readCh();
                        if (this.ch == 62) {
                            return;
                        }
                        this.addString(45);
                        this.addString(33);
                        continue block7;
                    }
                    this.addString(45);
                    break;
                }
                case -1: {
                    this.handleEOFInComment();
                    return;
                }
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    break;
                }
                case 62: {
                    this.ch = this.readCh();
                    break;
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                    } else {
                        ++this.crCount;
                    }
                    c = 10;
                    break;
                }
                default: {
                    this.ch = this.readCh();
                }
            }
            this.addString(c);
        }
    }

    void parseLiteral(boolean replace) throws IOException {
        block7: while (true) {
            int c = this.ch;
            switch (c) {
                case -1: {
                    this.error("eof.literal", this.stack.elem.getName());
                    this.endTag(true);
                    return;
                }
                case 62: {
                    this.ch = this.readCh();
                    int i = this.textpos - (this.stack.elem.name.length() + 2);
                    int j = 0;
                    if (i < 0 || this.text[i++] != '<' || this.text[i] != '/') break;
                    while (++i < this.textpos && Character.toLowerCase(this.text[i]) == this.stack.elem.name.charAt(j++)) {
                    }
                    if (i != this.textpos) break;
                    this.textpos -= this.stack.elem.name.length() + 2;
                    if (this.textpos > 0 && this.text[this.textpos - 1] == '\n') {
                        --this.textpos;
                    }
                    this.endTag(false);
                    return;
                }
                case 38: {
                    char[] data = this.parseEntityReference();
                    if (this.textpos + data.length > this.text.length) {
                        char[] newtext = new char[Math.max(this.textpos + data.length + 128, this.text.length * 2)];
                        System.arraycopy(this.text, 0, newtext, 0, this.text.length);
                        this.text = newtext;
                    }
                    System.arraycopy(data, 0, this.text, this.textpos, data.length);
                    this.textpos += data.length;
                    continue block7;
                }
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    break;
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                    } else {
                        ++this.crCount;
                    }
                    c = 10;
                    break;
                }
                default: {
                    this.ch = this.readCh();
                }
            }
            if (this.textpos == this.text.length) {
                char[] newtext = new char[this.text.length + 128];
                System.arraycopy(this.text, 0, newtext, 0, this.text.length);
                this.text = newtext;
            }
            this.text[this.textpos++] = (char)c;
        }
    }

    String parseAttributeValue(boolean lower) throws IOException {
        int delim = -1;
        switch (this.ch) {
            case 34: 
            case 39: {
                delim = this.ch;
                this.ch = this.readCh();
            }
        }
        block14: while (true) {
            int c = this.ch;
            switch (c) {
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    if (delim >= 0) break;
                    return this.getString(0);
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                    } else {
                        ++this.crCount;
                    }
                    if (delim >= 0) break;
                    return this.getString(0);
                }
                case 9: {
                    if (delim < 0) {
                        c = 32;
                    }
                }
                case 32: {
                    this.ch = this.readCh();
                    if (delim >= 0) break;
                    return this.getString(0);
                }
                case 60: 
                case 62: {
                    if (delim < 0) {
                        return this.getString(0);
                    }
                    this.ch = this.readCh();
                    break;
                }
                case 34: 
                case 39: {
                    this.ch = this.readCh();
                    if (c == delim) {
                        return this.getString(0);
                    }
                    if (delim != -1) break;
                    this.error("attvalerr");
                    if (!this.strict && this.ch != 32) continue block14;
                    return this.getString(0);
                }
                case 61: {
                    if (delim < 0) {
                        this.error("attvalerr");
                        if (this.strict) {
                            return this.getString(0);
                        }
                    }
                    this.ch = this.readCh();
                    break;
                }
                case 38: {
                    if (this.strict && delim < 0) {
                        this.ch = this.readCh();
                        break;
                    }
                    char[] data = this.parseEntityReference();
                    int i = 0;
                    while (true) {
                        if (i >= data.length) continue block14;
                        c = data[i];
                        this.addString(lower && c >= 65 && c <= 90 ? 97 + c - 65 : c);
                        ++i;
                    }
                }
                case -1: {
                    return this.getString(0);
                }
                default: {
                    if (lower && c >= 65 && c <= 90) {
                        c = 97 + c - 65;
                    }
                    this.ch = this.readCh();
                }
            }
            this.addString(c);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    void parseAttributeSpecificationList(Element elem) throws IOException {
        block4: while (true) {
            String attvalue;
            AttributeList att;
            String attname;
            block20: {
                char[] str;
                block21: {
                    block22: {
                        block23: {
                            block18: {
                                block19: {
                                    this.skipSpace();
                                    switch (this.ch) {
                                        case -1: 
                                        case 47: 
                                        case 60: 
                                        case 62: {
                                            return;
                                        }
                                        case 45: {
                                            this.ch = this.readCh();
                                            if (this.ch == 45) {
                                                this.ch = this.readCh();
                                                this.parseComment();
                                                this.strpos = 0;
                                                continue block4;
                                            }
                                            this.error("invalid.tagchar", "-", elem.getName());
                                            this.ch = this.readCh();
                                            continue block4;
                                        }
                                    }
                                    if (!this.parseIdentifier(true)) break block18;
                                    attname = this.getString(0);
                                    this.skipSpace();
                                    if (this.ch != 61) break block19;
                                    this.ch = this.readCh();
                                    this.skipSpace();
                                    att = elem.getAttribute(attname);
                                    attvalue = this.parseAttributeValue(att != null && att.type != 1 && att.type != 11 && att.type != 7);
                                    break block20;
                                }
                                attvalue = attname;
                                att = elem.getAttributeByValue(attvalue);
                                if (att != null) break block20;
                                att = elem.getAttribute(attname);
                                attvalue = att != null ? att.getValue() : null;
                                break block20;
                            }
                            if (!this.strict && this.ch == 44) {
                                this.ch = this.readCh();
                                continue;
                            }
                            if (this.strict || this.ch != 34) break block21;
                            this.ch = this.readCh();
                            this.skipSpace();
                            if (!this.parseIdentifier(true)) break block22;
                            attname = this.getString(0);
                            if (this.ch == 34) {
                                this.ch = this.readCh();
                            }
                            this.skipSpace();
                            if (this.ch != 61) break block23;
                            this.ch = this.readCh();
                            this.skipSpace();
                            att = elem.getAttribute(attname);
                            attvalue = this.parseAttributeValue(att != null && att.type != 1 && att.type != 11);
                            break block20;
                        }
                        attvalue = attname;
                        att = elem.getAttributeByValue(attvalue);
                        if (att != null || (att = elem.getAttribute(attname)) == null) break block20;
                        attvalue = att.getValue();
                        break block20;
                    }
                    str = new char[]{(char)this.ch};
                    this.error("invalid.tagchar", new String(str), elem.getName());
                    this.ch = this.readCh();
                    continue;
                }
                if (!this.strict && this.attributes.isEmpty() && this.ch == 61) {
                    this.ch = this.readCh();
                    this.skipSpace();
                    attname = elem.getName();
                    att = elem.getAttribute(attname);
                    attvalue = this.parseAttributeValue(att != null && att.type != 1 && att.type != 11);
                } else {
                    if (!this.strict && this.ch == 61) {
                        this.ch = this.readCh();
                        this.skipSpace();
                        attvalue = this.parseAttributeValue(true);
                        this.error("attvalerr");
                        return;
                    }
                    str = new char[]{(char)this.ch};
                    this.error("invalid.tagchar", new String(str), elem.getName());
                    if (this.strict) return;
                    this.ch = this.readCh();
                    continue;
                }
            }
            if (att != null) {
                attname = att.getName();
            } else {
                this.error("invalid.tagatt", attname, elem.getName());
            }
            if (this.attributes.isDefined(attname)) {
                this.error("multi.tagatt", attname, elem.getName());
            }
            if (attvalue == null) {
                attvalue = att != null && att.value != null ? att.value : "#DEFAULT";
            } else if (att != null && att.values != null && !att.values.contains(attvalue)) {
                this.error("invalid.tagattval", attname, elem.getName());
            }
            HTML.Attribute attkey = HTML.getAttributeKey(attname);
            if (attkey == null) {
                this.attributes.addAttribute(attname, attvalue);
                continue;
            }
            this.attributes.addAttribute(attkey, attvalue);
        }
    }

    public String parseDTDMarkup() throws IOException {
        StringBuilder strBuff = new StringBuilder();
        this.ch = this.readCh();
        block7: while (true) {
            switch (this.ch) {
                case 62: {
                    this.ch = this.readCh();
                    return strBuff.toString();
                }
                case -1: {
                    this.error("invalid.markup");
                    return strBuff.toString();
                }
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    continue block7;
                }
                case 34: {
                    this.ch = this.readCh();
                    continue block7;
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                        continue block7;
                    }
                    ++this.crCount;
                    continue block7;
                }
            }
            strBuff.append((char)(this.ch & 0xFF));
            this.ch = this.readCh();
        }
    }

    protected boolean parseMarkupDeclarations(StringBuffer strBuff) throws IOException {
        if (strBuff.length() == "DOCTYPE".length() && strBuff.toString().toUpperCase().equals("DOCTYPE")) {
            this.parseDTDMarkup();
            return true;
        }
        return false;
    }

    void parseInvalidTag() throws IOException {
        while (true) {
            this.skipSpace();
            switch (this.ch) {
                case -1: 
                case 62: {
                    this.ch = this.readCh();
                    return;
                }
                case 60: {
                    return;
                }
            }
            this.ch = this.readCh();
        }
    }

    void parseTag() throws IOException {
        Element elem;
        boolean net = false;
        boolean warned = false;
        boolean unknown = false;
        this.ch = this.readCh();
        switch (this.ch) {
            case 33: {
                this.ch = this.readCh();
                switch (this.ch) {
                    case 45: {
                        block36: while (true) {
                            if (this.ch == 45) {
                                if (!this.strict || (this.ch = this.readCh()) == 45) {
                                    this.ch = this.readCh();
                                    if (!this.strict && this.ch == 45) {
                                        this.ch = this.readCh();
                                    }
                                    if (this.textpos != 0) {
                                        char[] newtext = new char[this.textpos];
                                        System.arraycopy(this.text, 0, newtext, 0, this.textpos);
                                        this.handleText(newtext);
                                        this.lastBlockStartPos = this.currentBlockStartPos;
                                        this.textpos = 0;
                                    }
                                    this.parseComment();
                                    this.last = this.makeTag(this.dtd.getElement("comment"), true);
                                    this.handleComment(this.getChars(0));
                                    continue;
                                }
                                if (!warned) {
                                    warned = true;
                                    this.error("invalid.commentchar", "-");
                                }
                            }
                            this.skipSpace();
                            switch (this.ch) {
                                case 45: {
                                    continue block36;
                                }
                                case 62: {
                                    this.ch = this.readCh();
                                    return;
                                }
                                case -1: {
                                    return;
                                }
                            }
                            this.ch = this.readCh();
                            if (warned) continue;
                            warned = true;
                            this.error("invalid.commentchar", String.valueOf((char)this.ch));
                        }
                    }
                }
                StringBuffer strBuff = new StringBuffer();
                block37: while (true) {
                    strBuff.append((char)this.ch);
                    if (this.parseMarkupDeclarations(strBuff)) {
                        return;
                    }
                    switch (this.ch) {
                        case 62: {
                            this.ch = this.readCh();
                        }
                        case -1: {
                            this.error("invalid.markup");
                            return;
                        }
                        case 10: {
                            ++this.ln;
                            this.ch = this.readCh();
                            ++this.lfCount;
                            continue block37;
                        }
                        case 13: {
                            ++this.ln;
                            this.ch = this.readCh();
                            if (this.ch == 10) {
                                this.ch = this.readCh();
                                ++this.crlfCount;
                                continue block37;
                            }
                            ++this.crCount;
                            continue block37;
                        }
                    }
                    this.ch = this.readCh();
                }
            }
            case 47: {
                Element elem2;
                this.ch = this.readCh();
                switch (this.ch) {
                    case 62: {
                        this.ch = this.readCh();
                    }
                    case 60: {
                        if (this.recent == null) {
                            this.error("invalid.shortend");
                            return;
                        }
                        elem2 = this.recent;
                        break;
                    }
                    default: {
                        if (!this.parseIdentifier(true)) {
                            this.error("expected.endtagname");
                            return;
                        }
                        this.skipSpace();
                        switch (this.ch) {
                            case 62: {
                                this.ch = this.readCh();
                                break;
                            }
                            case 60: {
                                break;
                            }
                            default: {
                                this.error("expected", "'>'");
                                while (this.ch != -1 && this.ch != 10 && this.ch != 62) {
                                    this.ch = this.readCh();
                                }
                                if (this.ch != 62) break;
                                this.ch = this.readCh();
                            }
                        }
                        String elemStr = this.getString(0);
                        if (!this.dtd.elementExists(elemStr)) {
                            this.error("end.unrecognized", elemStr);
                            if (this.textpos > 0 && this.text[this.textpos - 1] == '\n') {
                                --this.textpos;
                            }
                            elem2 = this.dtd.getElement("unknown");
                            elem2.name = elemStr;
                            unknown = true;
                            break;
                        }
                        elem2 = this.dtd.getElement(elemStr);
                    }
                }
                if (this.stack == null) {
                    this.error("end.extra.tag", elem2.getName());
                    return;
                }
                if (this.textpos > 0 && this.text[this.textpos - 1] == '\n') {
                    if (this.stack.pre) {
                        if (this.textpos > 1 && this.text[this.textpos - 2] != '\n') {
                            --this.textpos;
                        }
                    } else {
                        --this.textpos;
                    }
                }
                if (unknown) {
                    TagElement t = this.makeTag(elem2);
                    this.handleText(t);
                    this.attributes.addAttribute(HTML.Attribute.ENDTAG, "true");
                    this.handleEmptyTag(this.makeTag(elem2));
                    unknown = false;
                    return;
                }
                if (!this.strict) {
                    String stackElem = this.stack.elem.getName();
                    if (stackElem.equals("table") && !elem2.getName().equals(stackElem)) {
                        this.error("tag.ignore", elem2.getName());
                        return;
                    }
                    if ((stackElem.equals("tr") || stackElem.equals("td")) && !elem2.getName().equals("table") && !elem2.getName().equals(stackElem)) {
                        this.error("tag.ignore", elem2.getName());
                        return;
                    }
                }
                TagStack sp = this.stack;
                while (sp != null && elem2 != sp.elem) {
                    sp = sp.next;
                }
                if (sp == null) {
                    this.error("unmatched.endtag", elem2.getName());
                    return;
                }
                String elemName = elem2.getName();
                if (this.stack != sp && (elemName.equals("font") || elemName.equals("center"))) {
                    if (elemName.equals("center")) {
                        while (this.stack.elem.omitEnd() && this.stack != sp) {
                            this.endTag(true);
                        }
                        if (this.stack.elem == elem2) {
                            this.endTag(false);
                        }
                    }
                    return;
                }
                while (this.stack != sp) {
                    this.endTag(true);
                }
                this.endTag(false);
                return;
            }
            case -1: {
                this.error("eof");
                return;
            }
        }
        if (!this.parseIdentifier(true)) {
            elem = this.recent;
            if (this.ch != 62 || elem == null) {
                this.error("expected.tagname");
                return;
            }
        } else {
            String elemStr = this.getString(0);
            if (elemStr.equals("image")) {
                elemStr = "img";
            }
            if (!this.dtd.elementExists(elemStr)) {
                this.error("tag.unrecognized ", elemStr);
                elem = this.dtd.getElement("unknown");
                elem.name = elemStr;
                unknown = true;
            } else {
                elem = this.dtd.getElement(elemStr);
            }
        }
        this.parseAttributeSpecificationList(elem);
        switch (this.ch) {
            case 47: {
                net = true;
            }
            case 62: {
                this.ch = this.readCh();
                if (this.ch == 62 && net) {
                    this.ch = this.readCh();
                }
            }
            case 60: {
                break;
            }
            default: {
                this.error("expected", "'>'");
            }
        }
        if (!this.strict && elem.getName().equals("script")) {
            this.error("javascript.unsupported");
        }
        if (!elem.isEmpty()) {
            if (this.ch == 10) {
                ++this.ln;
                ++this.lfCount;
                this.ch = this.readCh();
            } else if (this.ch == 13) {
                ++this.ln;
                this.ch = this.readCh();
                if (this.ch == 10) {
                    this.ch = this.readCh();
                    ++this.crlfCount;
                } else {
                    ++this.crCount;
                }
            }
        }
        TagElement tag = this.makeTag(elem, false);
        if (!unknown) {
            this.legalTagContext(tag);
            if (!this.strict && this.skipTag) {
                this.skipTag = false;
                return;
            }
        }
        this.startTag(tag);
        if (!elem.isEmpty()) {
            switch (elem.getType()) {
                case 1: {
                    this.parseLiteral(false);
                    break;
                }
                case 16: {
                    this.parseLiteral(true);
                    break;
                }
                default: {
                    if (this.stack == null) break;
                    this.stack.net = net;
                }
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    void parseScript() throws IOException {
        charsToAdd = new char[Parser.SCRIPT_END_TAG.length];
        insideComment = false;
        while (true) lbl-1000:
        // 6 sources

        {
            for (i = 0; !(insideComment || i >= Parser.SCRIPT_END_TAG.length || Parser.SCRIPT_END_TAG[i] != this.ch && Parser.SCRIPT_END_TAG_UPPER_CASE[i] != this.ch); ++i) {
                charsToAdd[i] = (char)this.ch;
                this.ch = this.readCh();
            }
            if (i == Parser.SCRIPT_END_TAG.length) {
                return;
            }
            if (!insideComment && i == 1 && charsToAdd[0] == "<!--".charAt(0)) {
                while (i < "<!--".length() && "<!--".charAt(i) == this.ch) {
                    charsToAdd[i] = (char)this.ch;
                    this.ch = this.readCh();
                    ++i;
                }
                if (i == "<!--".length()) {
                    insideComment = true;
                }
            }
            if (insideComment) {
                while (i < "-->".length() && "-->".charAt(i) == this.ch) {
                    charsToAdd[i] = (char)this.ch;
                    this.ch = this.readCh();
                    ++i;
                }
                if (i == "-->".length()) {
                    insideComment = false;
                }
            }
            if (i > 0) {
                j = 0;
                while (true) {
                    if (j >= i) ** continue;
                    this.addString(charsToAdd[j]);
                    ++j;
                }
            }
            switch (this.ch) {
                case -1: {
                    this.error("eof.script");
                    return;
                }
                case 10: {
                    ++this.ln;
                    this.ch = this.readCh();
                    ++this.lfCount;
                    this.addString(10);
                    ** continue;
                }
                case 13: {
                    ++this.ln;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                    } else {
                        ++this.crCount;
                    }
                    this.addString(10);
                    ** continue;
                }
            }
            this.addString(this.ch);
            this.ch = this.readCh();
        }
    }

    void parseContent() throws IOException {
        Thread curThread = Thread.currentThread();
        block9: while (true) {
            if (curThread.isInterrupted()) break;
            int c = this.ch;
            this.currentBlockStartPos = this.currentPosition;
            if (this.recent == this.dtd.script) {
                this.parseScript();
                this.last = this.makeTag(this.dtd.getElement("comment"), true);
                String str = new String(this.getChars(0)).trim();
                int minLength = START_COMMENT.length() + END_COMMENT.length();
                if (str.startsWith(START_COMMENT) && str.endsWith(END_COMMENT) && str.length() >= minLength) {
                    str = str.substring(START_COMMENT.length(), str.length() - END_COMMENT.length());
                }
                this.handleComment(str.toCharArray());
                this.endTag(false);
                this.lastBlockStartPos = this.currentPosition;
                continue;
            }
            switch (c) {
                case 60: {
                    this.parseTag();
                    this.lastBlockStartPos = this.currentPosition;
                    continue block9;
                }
                case 47: {
                    this.ch = this.readCh();
                    if (this.stack != null && this.stack.net) {
                        this.endTag(false);
                        continue block9;
                    }
                    if (this.textpos != 0) break;
                    if (!this.legalElementContext(this.dtd.pcdata)) {
                        this.error("unexpected.pcdata");
                    }
                    if (!this.last.breaksFlow()) break;
                    this.space = false;
                    break;
                }
                case -1: {
                    return;
                }
                case 38: {
                    char[] data;
                    if (this.textpos == 0) {
                        if (!this.legalElementContext(this.dtd.pcdata)) {
                            this.error("unexpected.pcdata");
                        }
                        if (this.last.breaksFlow()) {
                            this.space = false;
                        }
                    }
                    if (this.textpos + (data = this.parseEntityReference()).length + 1 > this.text.length) {
                        char[] newtext = new char[Math.max(this.textpos + data.length + 128, this.text.length * 2)];
                        System.arraycopy(this.text, 0, newtext, 0, this.text.length);
                        this.text = newtext;
                    }
                    if (this.space) {
                        this.space = false;
                        this.text[this.textpos++] = 32;
                    }
                    System.arraycopy(data, 0, this.text, this.textpos, data.length);
                    this.textpos += data.length;
                    this.ignoreSpace = false;
                    continue block9;
                }
                case 10: {
                    ++this.ln;
                    ++this.lfCount;
                    this.ch = this.readCh();
                    if (this.stack != null && this.stack.pre) break;
                    if (this.textpos == 0) {
                        this.lastBlockStartPos = this.currentPosition;
                    }
                    if (this.ignoreSpace) continue block9;
                    this.space = true;
                    continue block9;
                }
                case 13: {
                    ++this.ln;
                    c = 10;
                    this.ch = this.readCh();
                    if (this.ch == 10) {
                        this.ch = this.readCh();
                        ++this.crlfCount;
                    } else {
                        ++this.crCount;
                    }
                    if (this.stack != null && this.stack.pre) break;
                    if (this.textpos == 0) {
                        this.lastBlockStartPos = this.currentPosition;
                    }
                    if (this.ignoreSpace) continue block9;
                    this.space = true;
                    continue block9;
                }
                case 9: 
                case 32: {
                    this.ch = this.readCh();
                    if (this.stack != null && this.stack.pre) break;
                    if (this.textpos == 0) {
                        this.lastBlockStartPos = this.currentPosition;
                    }
                    if (this.ignoreSpace) continue block9;
                    this.space = true;
                    continue block9;
                }
                default: {
                    if (this.textpos == 0) {
                        if (!this.legalElementContext(this.dtd.pcdata)) {
                            this.error("unexpected.pcdata");
                        }
                        if (this.last.breaksFlow()) {
                            this.space = false;
                        }
                    }
                    this.ch = this.readCh();
                }
            }
            if (this.textpos + 2 > this.text.length) {
                char[] newtext = new char[this.text.length + 128];
                System.arraycopy(this.text, 0, newtext, 0, this.text.length);
                this.text = newtext;
            }
            if (this.space) {
                if (this.textpos == 0) {
                    --this.lastBlockStartPos;
                }
                this.text[this.textpos++] = 32;
                this.space = false;
            }
            this.text[this.textpos++] = (char)c;
            this.ignoreSpace = false;
        }
        curThread.interrupt();
    }

    String getEndOfLineString() {
        if (this.crlfCount >= this.crCount) {
            if (this.lfCount >= this.crlfCount) {
                return "\n";
            }
            return "\r\n";
        }
        if (this.crCount > this.lfCount) {
            return "\r";
        }
        return "\n";
    }

    public synchronized void parse(Reader in) throws IOException {
        this.in = in;
        this.ln = 1;
        this.seenHtml = false;
        this.seenHead = false;
        this.seenBody = false;
        this.crlfCount = 0;
        this.lfCount = 0;
        this.crCount = 0;
        try {
            this.ch = this.readCh();
            this.text = new char[1024];
            this.str = new char[128];
            this.parseContent();
            while (this.stack != null) {
                this.endTag(true);
            }
            in.close();
        }
        catch (IOException e) {
            this.errorContext();
            this.error("ioexception");
            throw e;
        }
        catch (Exception e) {
            this.errorContext();
            this.error("exception", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
        }
        finally {
            while (this.stack != null) {
                this.handleEndTag(this.stack.tag);
                this.stack = this.stack.next;
            }
            this.text = null;
            this.str = null;
        }
    }

    private int readCh() throws IOException {
        if (this.pos >= this.len) {
            this.len = this.in.read(this.buf);
            if (this.len <= 0) {
                return -1;
            }
            this.pos = 0;
        }
        ++this.currentPosition;
        return this.buf[this.pos++];
    }

    protected int getCurrentPos() {
        return this.currentPosition;
    }
}

