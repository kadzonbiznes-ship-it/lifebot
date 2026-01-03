/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale;

public class StringTokenIterator {
    private String text;
    private String dlms;
    private char delimiterChar;
    private String token;
    private int start;
    private int end;
    private boolean done;

    public StringTokenIterator(String text, String dlms) {
        this.text = text;
        if (dlms.length() == 1) {
            this.delimiterChar = dlms.charAt(0);
        } else {
            this.dlms = dlms;
        }
        this.setStart(0);
    }

    public String first() {
        this.setStart(0);
        return this.token;
    }

    public String current() {
        return this.token;
    }

    public int currentStart() {
        return this.start;
    }

    public int currentEnd() {
        return this.end;
    }

    public boolean isDone() {
        return this.done;
    }

    public String next() {
        if (this.hasNext()) {
            this.start = this.end + 1;
            this.end = this.nextDelimiter(this.start);
            this.token = this.text.substring(this.start, this.end);
        } else {
            this.start = this.end;
            this.token = null;
            this.done = true;
        }
        return this.token;
    }

    public boolean hasNext() {
        return this.end < this.text.length();
    }

    public StringTokenIterator setStart(int offset) {
        if (offset > this.text.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.start = offset;
        this.end = this.nextDelimiter(this.start);
        this.token = this.text.substring(this.start, this.end);
        this.done = false;
        return this;
    }

    public StringTokenIterator setText(String text) {
        this.text = text;
        this.setStart(0);
        return this;
    }

    private int nextDelimiter(int start) {
        int textlen = this.text.length();
        if (this.dlms == null) {
            for (int idx = start; idx < textlen; ++idx) {
                if (this.text.charAt(idx) != this.delimiterChar) continue;
                return idx;
            }
        } else {
            int dlmslen = this.dlms.length();
            for (int idx = start; idx < textlen; ++idx) {
                char c = this.text.charAt(idx);
                for (int i = 0; i < dlmslen; ++i) {
                    if (c != this.dlms.charAt(i)) continue;
                    return idx;
                }
            }
        }
        return textlen;
    }
}

