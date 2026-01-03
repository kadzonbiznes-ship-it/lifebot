/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;

public class StreamTokenizer {
    private Reader reader = null;
    private InputStream input = null;
    private char[] buf = new char[20];
    private int peekc = Integer.MAX_VALUE;
    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = 0x7FFFFFFE;
    private boolean pushedBack;
    private boolean forceLower;
    private int LINENO = 1;
    private boolean eolIsSignificantP = false;
    private boolean slashSlashCommentsP = false;
    private boolean slashStarCommentsP = false;
    private final byte[] ctype = new byte[256];
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;
    public int ttype = -4;
    public static final int TT_EOF = -1;
    public static final int TT_EOL = 10;
    public static final int TT_NUMBER = -2;
    public static final int TT_WORD = -3;
    private static final int TT_NOTHING = -4;
    public String sval;
    public double nval;

    private StreamTokenizer() {
        this.wordChars(97, 122);
        this.wordChars(65, 90);
        this.wordChars(160, 255);
        this.whitespaceChars(0, 32);
        this.commentChar(47);
        this.quoteChar(34);
        this.quoteChar(39);
        this.parseNumbers();
    }

    @Deprecated
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        this.input = is;
    }

    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        this.reader = r;
    }

    public void resetSyntax() {
        int i = this.ctype.length;
        while (--i >= 0) {
            this.ctype[i] = 0;
        }
    }

    public void wordChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            int n = low++;
            this.ctype[n] = (byte)(this.ctype[n] | 4);
        }
    }

    public void whitespaceChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            this.ctype[low++] = 1;
        }
    }

    public void ordinaryChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            this.ctype[low++] = 0;
        }
    }

    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 0;
        }
    }

    public void commentChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 16;
        }
    }

    public void quoteChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 8;
        }
    }

    public void parseNumbers() {
        int i = 48;
        while (i <= 57) {
            int n = i++;
            this.ctype[n] = (byte)(this.ctype[n] | 2);
        }
        this.ctype[46] = (byte)(this.ctype[46] | 2);
        this.ctype[45] = (byte)(this.ctype[45] | 2);
    }

    public void eolIsSignificant(boolean flag) {
        this.eolIsSignificantP = flag;
    }

    public void slashStarComments(boolean flag) {
        this.slashStarCommentsP = flag;
    }

    public void slashSlashComments(boolean flag) {
        this.slashSlashCommentsP = flag;
    }

    public void lowerCaseMode(boolean fl) {
        this.forceLower = fl;
    }

    private int read() throws IOException {
        if (this.reader != null) {
            return this.reader.read();
        }
        if (this.input != null) {
            return this.input.read();
        }
        throw new IllegalStateException();
    }

    public int nextToken() throws IOException {
        int ctype;
        if (this.pushedBack) {
            this.pushedBack = false;
            return this.ttype;
        }
        byte[] ct = this.ctype;
        this.sval = null;
        int c = this.peekc;
        if (c < 0) {
            c = Integer.MAX_VALUE;
        }
        if (c == 0x7FFFFFFE) {
            c = this.read();
            if (c < 0) {
                this.ttype = -1;
                return -1;
            }
            if (c == 10) {
                c = Integer.MAX_VALUE;
            }
        }
        if (c == Integer.MAX_VALUE && (c = this.read()) < 0) {
            this.ttype = -1;
            return -1;
        }
        this.ttype = c;
        this.peekc = Integer.MAX_VALUE;
        int n = ctype = c < 256 ? ct[c] : 4;
        while ((ctype & 1) != 0) {
            if (c == 13) {
                ++this.LINENO;
                if (this.eolIsSignificantP) {
                    this.peekc = 0x7FFFFFFE;
                    this.ttype = 10;
                    return 10;
                }
                c = this.read();
                if (c == 10) {
                    c = this.read();
                }
            } else {
                if (c == 10) {
                    ++this.LINENO;
                    if (this.eolIsSignificantP) {
                        this.ttype = 10;
                        return 10;
                    }
                }
                c = this.read();
            }
            if (c < 0) {
                this.ttype = -1;
                return -1;
            }
            ctype = c < 256 ? ct[c] : 4;
        }
        if ((ctype & 2) != 0) {
            boolean neg = false;
            if (c == 45) {
                c = this.read();
                if (c != 46 && (c < 48 || c > 57)) {
                    this.peekc = c;
                    this.ttype = 45;
                    return 45;
                }
                neg = true;
            }
            double v = 0.0;
            int decexp = 0;
            int seendot = 0;
            while (true) {
                if (c == 46 && seendot == 0) {
                    seendot = 1;
                } else {
                    if (48 > c || c > 57) break;
                    v = v * 10.0 + (double)(c - 48);
                    decexp += seendot;
                }
                c = this.read();
            }
            this.peekc = c;
            if (decexp != 0) {
                double denom = 10.0;
                --decexp;
                while (decexp > 0) {
                    denom *= 10.0;
                    --decexp;
                }
                v /= denom;
            }
            this.nval = neg ? -v : v;
            this.ttype = -2;
            return -2;
        }
        if ((ctype & 4) != 0) {
            int i = 0;
            do {
                if (i >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                this.buf[i++] = (char)c;
            } while (((ctype = (c = this.read()) < 0 ? 1 : (c < 256 ? ct[c] : 4)) & 6) != 0);
            this.peekc = c;
            this.sval = String.copyValueOf(this.buf, 0, i);
            if (this.forceLower) {
                this.sval = this.sval.toLowerCase();
            }
            this.ttype = -3;
            return -3;
        }
        if ((ctype & 8) != 0) {
            this.ttype = c;
            int i = 0;
            int d = this.read();
            while (d >= 0 && d != this.ttype && d != 10 && d != 13) {
                if (d == 92) {
                    int first = c = this.read();
                    if (c >= 48 && c <= 55) {
                        c -= 48;
                        int c2 = this.read();
                        if (48 <= c2 && c2 <= 55) {
                            c = (c << 3) + (c2 - 48);
                            c2 = this.read();
                            if (48 <= c2 && c2 <= 55 && first <= 51) {
                                c = (c << 3) + (c2 - 48);
                                d = this.read();
                            } else {
                                d = c2;
                            }
                        } else {
                            d = c2;
                        }
                    } else {
                        c = switch (c) {
                            case 97 -> 7;
                            case 98 -> 8;
                            case 102 -> 12;
                            case 110 -> 10;
                            case 114 -> 13;
                            case 116 -> 9;
                            case 118 -> 11;
                            default -> c;
                        };
                        d = this.read();
                    }
                } else {
                    c = d;
                    d = this.read();
                }
                if (i >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                this.buf[i++] = (char)c;
            }
            this.peekc = d == this.ttype ? Integer.MAX_VALUE : d;
            this.sval = String.copyValueOf(this.buf, 0, i);
            return this.ttype;
        }
        if (c == 47 && (this.slashSlashCommentsP || this.slashStarCommentsP)) {
            c = this.read();
            if (c == 42 && this.slashStarCommentsP) {
                int prevc = 0;
                while ((c = this.read()) != 47 || prevc != 42) {
                    if (c == 13) {
                        ++this.LINENO;
                        c = this.read();
                        if (c == 10) {
                            c = this.read();
                        }
                    } else if (c == 10) {
                        ++this.LINENO;
                        c = this.read();
                    }
                    if (c < 0) {
                        this.ttype = -1;
                        return -1;
                    }
                    prevc = c;
                }
                return this.nextToken();
            }
            if (c == 47 && this.slashSlashCommentsP) {
                while ((c = this.read()) != 10 && c != 13 && c >= 0) {
                }
                this.peekc = c;
                return this.nextToken();
            }
            if ((ct[47] & 0x10) != 0) {
                while ((c = this.read()) != 10 && c != 13 && c >= 0) {
                }
                this.peekc = c;
                return this.nextToken();
            }
            this.peekc = c;
            this.ttype = 47;
            return 47;
        }
        if ((ctype & 0x10) != 0) {
            while ((c = this.read()) != 10 && c != 13 && c >= 0) {
            }
            this.peekc = c;
            return this.nextToken();
        }
        this.ttype = c;
        return this.ttype;
    }

    public void pushBack() {
        if (this.ttype != -4) {
            this.pushedBack = true;
        }
    }

    public int lineno() {
        return this.LINENO;
    }

    public String toString() {
        String ret = switch (this.ttype) {
            case -1 -> "EOF";
            case 10 -> "EOL";
            case -3 -> this.sval;
            case -2 -> "n=" + this.nval;
            case -4 -> "NOTHING";
            default -> {
                if (this.ttype < 256 && (this.ctype[this.ttype] & 8) != 0) {
                    yield this.sval;
                }
                char[] s = new char[3];
                s[2] = 39;
                s[0] = 39;
                s[1] = (char)this.ttype;
                yield new String(s);
            }
        };
        return "Token[" + ret + "], line " + this.LINENO;
    }
}

