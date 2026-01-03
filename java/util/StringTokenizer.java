/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class StringTokenizer
implements Enumeration<Object> {
    private int currentPosition = 0;
    private int newPosition = -1;
    private int maxPosition;
    private String str;
    private String delimiters;
    private boolean retDelims;
    private boolean delimsChanged = false;
    private int maxDelimCodePoint;
    private boolean hasSurrogates = false;
    private int[] delimiterCodePoints;

    private void setMaxDelimCodePoint() {
        int i;
        int c;
        if (this.delimiters == null) {
            this.maxDelimCodePoint = 0;
            return;
        }
        int m = 0;
        int count = 0;
        for (i = 0; i < this.delimiters.length(); i += Character.charCount(c)) {
            c = this.delimiters.charAt(i);
            if (c >= 55296 && c <= 57343) {
                c = this.delimiters.codePointAt(i);
                this.hasSurrogates = true;
            }
            if (m < c) {
                m = c;
            }
            ++count;
        }
        this.maxDelimCodePoint = m;
        if (this.hasSurrogates) {
            this.delimiterCodePoints = new int[count];
            i = 0;
            int j = 0;
            while (i < count) {
                this.delimiterCodePoints[i] = c = this.delimiters.codePointAt(j);
                ++i;
                j += Character.charCount(c);
            }
        }
    }

    public StringTokenizer(String str, String delim, boolean returnDelims) {
        this.str = str;
        this.maxPosition = str.length();
        this.delimiters = delim;
        this.retDelims = returnDelims;
        this.setMaxDelimCodePoint();
    }

    public StringTokenizer(String str, String delim) {
        this(str, delim, false);
    }

    public StringTokenizer(String str) {
        this(str, " \t\n\r\f", false);
    }

    private int skipDelimiters(int startPos) {
        if (this.delimiters == null) {
            throw new NullPointerException();
        }
        int position = startPos;
        while (!this.retDelims && position < this.maxPosition) {
            int c;
            if (!this.hasSurrogates) {
                c = this.str.charAt(position);
                if (c > this.maxDelimCodePoint || this.delimiters.indexOf(c) < 0) break;
                ++position;
                continue;
            }
            c = this.str.codePointAt(position);
            if (c > this.maxDelimCodePoint || !this.isDelimiter(c)) break;
            position += Character.charCount(c);
        }
        return position;
    }

    private int scanToken(int startPos) {
        int c;
        int position = startPos;
        while (position < this.maxPosition) {
            if (!this.hasSurrogates) {
                c = this.str.charAt(position);
                if (c <= this.maxDelimCodePoint && this.delimiters.indexOf(c) >= 0) break;
                ++position;
                continue;
            }
            c = this.str.codePointAt(position);
            if (c <= this.maxDelimCodePoint && this.isDelimiter(c)) break;
            position += Character.charCount(c);
        }
        if (this.retDelims && startPos == position) {
            if (!this.hasSurrogates) {
                c = this.str.charAt(position);
                if (c <= this.maxDelimCodePoint && this.delimiters.indexOf(c) >= 0) {
                    ++position;
                }
            } else {
                c = this.str.codePointAt(position);
                if (c <= this.maxDelimCodePoint && this.isDelimiter(c)) {
                    position += Character.charCount(c);
                }
            }
        }
        return position;
    }

    private boolean isDelimiter(int codePoint) {
        for (int delimiterCodePoint : this.delimiterCodePoints) {
            if (delimiterCodePoint != codePoint) continue;
            return true;
        }
        return false;
    }

    public boolean hasMoreTokens() {
        this.newPosition = this.skipDelimiters(this.currentPosition);
        return this.newPosition < this.maxPosition;
    }

    public String nextToken() {
        this.currentPosition = this.newPosition >= 0 && !this.delimsChanged ? this.newPosition : this.skipDelimiters(this.currentPosition);
        this.delimsChanged = false;
        this.newPosition = -1;
        if (this.currentPosition >= this.maxPosition) {
            throw new NoSuchElementException();
        }
        int start = this.currentPosition;
        this.currentPosition = this.scanToken(this.currentPosition);
        return this.str.substring(start, this.currentPosition);
    }

    public String nextToken(String delim) {
        this.delimiters = delim;
        this.delimsChanged = true;
        this.setMaxDelimCodePoint();
        return this.nextToken();
    }

    @Override
    public boolean hasMoreElements() {
        return this.hasMoreTokens();
    }

    @Override
    public Object nextElement() {
        return this.nextToken();
    }

    public int countTokens() {
        int count = 0;
        int currpos = this.currentPosition;
        while (currpos < this.maxPosition && (currpos = this.skipDelimiters(currpos)) < this.maxPosition) {
            currpos = this.scanToken(currpos);
            ++count;
        }
        return count;
    }
}

