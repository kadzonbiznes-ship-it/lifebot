/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.io.IOException;
import java.io.Reader;

class CSSParser {
    private static final int IDENTIFIER = 1;
    private static final int BRACKET_OPEN = 2;
    private static final int BRACKET_CLOSE = 3;
    private static final int BRACE_OPEN = 4;
    private static final int BRACE_CLOSE = 5;
    private static final int PAREN_OPEN = 6;
    private static final int PAREN_CLOSE = 7;
    private static final int END = -1;
    private static final char[] charMapping = new char[]{'\u0000', '\u0000', '[', ']', '{', '}', '(', ')', '\u0000'};
    private boolean didPushChar;
    private int pushedChar;
    private StringBuffer unitBuffer;
    private int[] unitStack = new int[2];
    private int stackCount;
    private Reader reader;
    private boolean encounteredRuleSet;
    private CSSParserCallback callback;
    private char[] tokenBuffer = new char[80];
    private int tokenBufferLength;
    private boolean readWS;

    CSSParser() {
        this.unitBuffer = new StringBuffer();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void parse(Reader reader, CSSParserCallback callback, boolean inRule) throws IOException {
        this.callback = callback;
        this.tokenBufferLength = 0;
        this.stackCount = 0;
        this.reader = reader;
        this.encounteredRuleSet = false;
        try {
            if (inRule) {
                this.parseDeclarationBlock();
            } else {
                while (this.getNextStatement()) {
                }
            }
        }
        finally {
            callback = null;
            reader = null;
        }
    }

    private boolean getNextStatement() throws IOException {
        this.unitBuffer.setLength(0);
        int token = this.nextToken('\u0000');
        switch (token) {
            case 1: {
                if (this.tokenBufferLength > 0) {
                    if (this.tokenBuffer[0] == '@') {
                        this.parseAtRule();
                    } else {
                        this.encounteredRuleSet = true;
                        this.parseRuleSet();
                    }
                }
                return true;
            }
            case 2: 
            case 4: 
            case 6: {
                this.parseTillClosed(token);
                return true;
            }
            case 3: 
            case 5: 
            case 7: {
                throw new RuntimeException("Unexpected top level block close");
            }
            case -1: {
                return false;
            }
        }
        return true;
    }

    private void parseAtRule() throws IOException {
        boolean done = false;
        boolean isImport = this.tokenBufferLength == 7 && this.tokenBuffer[0] == '@' && this.tokenBuffer[1] == 'i' && this.tokenBuffer[2] == 'm' && this.tokenBuffer[3] == 'p' && this.tokenBuffer[4] == 'o' && this.tokenBuffer[5] == 'r' && this.tokenBuffer[6] == 't';
        this.unitBuffer.setLength(0);
        block7: while (!done) {
            int nextToken = this.nextToken(';');
            switch (nextToken) {
                case 1: {
                    if (this.tokenBufferLength > 0 && this.tokenBuffer[this.tokenBufferLength - 1] == ';') {
                        --this.tokenBufferLength;
                        done = true;
                    }
                    if (this.tokenBufferLength <= 0) break;
                    if (this.unitBuffer.length() > 0 && this.readWS) {
                        this.unitBuffer.append(' ');
                    }
                    this.unitBuffer.append(this.tokenBuffer, 0, this.tokenBufferLength);
                    break;
                }
                case 4: {
                    if (this.unitBuffer.length() > 0 && this.readWS) {
                        this.unitBuffer.append(' ');
                    }
                    this.unitBuffer.append(charMapping[nextToken]);
                    this.parseTillClosed(nextToken);
                    done = true;
                    int nextChar = this.readWS();
                    if (nextChar == -1 || nextChar == 59) continue block7;
                    this.pushChar(nextChar);
                    break;
                }
                case 2: 
                case 6: {
                    this.unitBuffer.append(charMapping[nextToken]);
                    this.parseTillClosed(nextToken);
                    break;
                }
                case 3: 
                case 5: 
                case 7: {
                    throw new RuntimeException("Unexpected close in @ rule");
                }
                case -1: {
                    done = true;
                }
            }
        }
        if (isImport && !this.encounteredRuleSet) {
            this.callback.handleImport(this.unitBuffer.toString());
        }
    }

    private void parseRuleSet() throws IOException {
        if (this.parseSelectors()) {
            this.callback.startRule();
            this.parseDeclarationBlock();
            this.callback.endRule();
        }
    }

    private boolean parseSelectors() throws IOException {
        if (this.tokenBufferLength > 0) {
            this.callback.handleSelector(new String(this.tokenBuffer, 0, this.tokenBufferLength));
        }
        this.unitBuffer.setLength(0);
        while (true) {
            int nextToken;
            if ((nextToken = this.nextToken('\u0000')) == 1) {
                if (this.tokenBufferLength <= 0) continue;
                this.callback.handleSelector(new String(this.tokenBuffer, 0, this.tokenBufferLength));
                continue;
            }
            switch (nextToken) {
                case 4: {
                    return true;
                }
                case 2: 
                case 6: {
                    this.parseTillClosed(nextToken);
                    this.unitBuffer.setLength(0);
                    break;
                }
                case 3: 
                case 5: 
                case 7: {
                    throw new RuntimeException("Unexpected block close in selector");
                }
                case -1: {
                    return false;
                }
            }
        }
    }

    private void parseDeclarationBlock() throws IOException {
        while (true) {
            int token = this.parseDeclaration();
            switch (token) {
                case -1: 
                case 5: {
                    return;
                }
                case 3: 
                case 7: {
                    throw new RuntimeException("Unexpected close in declaration block");
                }
            }
        }
    }

    private int parseDeclaration() throws IOException {
        int token = this.parseIdentifiers(':', false);
        if (token != 1) {
            return token;
        }
        for (int counter = this.unitBuffer.length() - 1; counter >= 0; --counter) {
            this.unitBuffer.setCharAt(counter, Character.toLowerCase(this.unitBuffer.charAt(counter)));
        }
        this.callback.handleProperty(this.unitBuffer.toString());
        token = this.parseIdentifiers(';', true);
        this.callback.handleValue(this.unitBuffer.toString());
        return token;
    }

    private int parseIdentifiers(char extraChar, boolean wantsBlocks) throws IOException {
        this.unitBuffer.setLength(0);
        while (true) {
            int nextToken = this.nextToken(extraChar);
            switch (nextToken) {
                case 1: {
                    if (this.tokenBufferLength <= 0) break;
                    if (this.tokenBuffer[this.tokenBufferLength - 1] == extraChar) {
                        if (--this.tokenBufferLength > 0) {
                            if (this.readWS && this.unitBuffer.length() > 0) {
                                this.unitBuffer.append(' ');
                            }
                            this.unitBuffer.append(this.tokenBuffer, 0, this.tokenBufferLength);
                        }
                        return 1;
                    }
                    if (this.readWS && this.unitBuffer.length() > 0) {
                        this.unitBuffer.append(' ');
                    }
                    this.unitBuffer.append(this.tokenBuffer, 0, this.tokenBufferLength);
                    break;
                }
                case 2: 
                case 4: 
                case 6: {
                    int ubl = this.unitBuffer.length();
                    if (wantsBlocks) {
                        this.unitBuffer.append(charMapping[nextToken]);
                    }
                    this.parseTillClosed(nextToken);
                    if (wantsBlocks) break;
                    this.unitBuffer.setLength(ubl);
                    break;
                }
                case -1: 
                case 3: 
                case 5: 
                case 7: {
                    return nextToken;
                }
            }
        }
    }

    private void parseTillClosed(int openToken) throws IOException {
        boolean done = false;
        this.startBlock(openToken);
        while (!done) {
            int nextToken = this.nextToken('\u0000');
            switch (nextToken) {
                case 1: {
                    if (this.unitBuffer.length() > 0 && this.readWS) {
                        this.unitBuffer.append(' ');
                    }
                    if (this.tokenBufferLength <= 0) break;
                    this.unitBuffer.append(this.tokenBuffer, 0, this.tokenBufferLength);
                    break;
                }
                case 2: 
                case 4: 
                case 6: {
                    if (this.unitBuffer.length() > 0 && this.readWS) {
                        this.unitBuffer.append(' ');
                    }
                    this.unitBuffer.append(charMapping[nextToken]);
                    this.startBlock(nextToken);
                    break;
                }
                case 3: 
                case 5: 
                case 7: {
                    if (this.unitBuffer.length() > 0 && this.readWS) {
                        this.unitBuffer.append(' ');
                    }
                    this.unitBuffer.append(charMapping[nextToken]);
                    this.endBlock(nextToken);
                    if (this.inBlock()) break;
                    done = true;
                    break;
                }
                case -1: {
                    throw new RuntimeException("Unclosed block");
                }
            }
        }
    }

    private int nextToken(char idChar) throws IOException {
        this.readWS = false;
        int nextChar = this.readWS();
        switch (nextChar) {
            case 39: {
                this.readTill('\'');
                if (this.tokenBufferLength > 0) {
                    --this.tokenBufferLength;
                }
                return 1;
            }
            case 34: {
                this.readTill('\"');
                if (this.tokenBufferLength > 0) {
                    --this.tokenBufferLength;
                }
                return 1;
            }
            case 91: {
                return 2;
            }
            case 93: {
                return 3;
            }
            case 123: {
                return 4;
            }
            case 125: {
                return 5;
            }
            case 40: {
                return 6;
            }
            case 41: {
                return 7;
            }
            case -1: {
                return -1;
            }
        }
        this.pushChar(nextChar);
        this.getIdentifier(idChar);
        return 1;
    }

    private boolean getIdentifier(char stopChar) throws IOException {
        boolean lastWasEscape = false;
        boolean done = false;
        int escapeCount = 0;
        int escapeChar = 0;
        char intStopChar = stopChar;
        int escapeOffset = 0;
        this.tokenBufferLength = 0;
        while (!done) {
            int type;
            int nextChar = this.readChar();
            switch (nextChar) {
                case 92: {
                    type = 1;
                    break;
                }
                case 48: 
                case 49: 
                case 50: 
                case 51: 
                case 52: 
                case 53: 
                case 54: 
                case 55: 
                case 56: 
                case 57: {
                    type = 2;
                    escapeOffset = nextChar - 48;
                    break;
                }
                case 97: 
                case 98: 
                case 99: 
                case 100: 
                case 101: 
                case 102: {
                    type = 2;
                    escapeOffset = nextChar - 97 + 10;
                    break;
                }
                case 65: 
                case 66: 
                case 67: 
                case 68: 
                case 69: 
                case 70: {
                    type = 2;
                    escapeOffset = nextChar - 65 + 10;
                    break;
                }
                case 9: 
                case 10: 
                case 13: 
                case 32: 
                case 34: 
                case 39: 
                case 40: 
                case 41: 
                case 91: 
                case 93: 
                case 123: 
                case 125: {
                    type = 3;
                    break;
                }
                case 47: {
                    type = 4;
                    break;
                }
                case -1: {
                    done = true;
                    type = 0;
                    break;
                }
                default: {
                    type = 0;
                }
            }
            if (lastWasEscape) {
                if (type == 2) {
                    escapeChar = escapeChar * 16 + escapeOffset;
                    if (++escapeCount != 4) continue;
                    lastWasEscape = false;
                    this.append((char)escapeChar);
                    continue;
                }
                lastWasEscape = false;
                if (escapeCount > 0) {
                    this.append((char)escapeChar);
                    this.pushChar(nextChar);
                    continue;
                }
                if (done) continue;
                this.append((char)nextChar);
                continue;
            }
            if (done) continue;
            if (type == 1) {
                lastWasEscape = true;
                escapeCount = 0;
                escapeChar = 0;
                continue;
            }
            if (type == 3) {
                done = true;
                this.pushChar(nextChar);
                continue;
            }
            if (type == 4) {
                nextChar = this.readChar();
                if (nextChar == 42) {
                    done = true;
                    this.readComment();
                    this.readWS = true;
                    continue;
                }
                this.append('/');
                if (nextChar == -1) {
                    done = true;
                    continue;
                }
                this.pushChar(nextChar);
                continue;
            }
            this.append((char)nextChar);
            if (nextChar != intStopChar) continue;
            done = true;
        }
        return this.tokenBufferLength > 0;
    }

    private void readTill(char stopChar) throws IOException {
        boolean lastWasEscape = false;
        int escapeCount = 0;
        int escapeChar = 0;
        boolean done = false;
        char intStopChar = stopChar;
        int escapeOffset = 0;
        this.tokenBufferLength = 0;
        while (!done) {
            int type;
            int nextChar = this.readChar();
            switch (nextChar) {
                case 92: {
                    type = 1;
                    break;
                }
                case 48: 
                case 49: 
                case 50: 
                case 51: 
                case 52: 
                case 53: 
                case 54: 
                case 55: 
                case 56: 
                case 57: {
                    type = 2;
                    escapeOffset = nextChar - 48;
                    break;
                }
                case 97: 
                case 98: 
                case 99: 
                case 100: 
                case 101: 
                case 102: {
                    type = 2;
                    escapeOffset = nextChar - 97 + 10;
                    break;
                }
                case 65: 
                case 66: 
                case 67: 
                case 68: 
                case 69: 
                case 70: {
                    type = 2;
                    escapeOffset = nextChar - 65 + 10;
                    break;
                }
                case -1: {
                    throw new RuntimeException("Unclosed " + stopChar);
                }
                default: {
                    type = 0;
                }
            }
            if (lastWasEscape) {
                if (type == 2) {
                    escapeChar = escapeChar * 16 + escapeOffset;
                    if (++escapeCount != 4) continue;
                    lastWasEscape = false;
                    this.append((char)escapeChar);
                    continue;
                }
                if (escapeCount > 0) {
                    this.append((char)escapeChar);
                    if (type == 1) {
                        lastWasEscape = true;
                        escapeCount = 0;
                        escapeChar = 0;
                        continue;
                    }
                    if (nextChar == intStopChar) {
                        done = true;
                    }
                    this.append((char)nextChar);
                    lastWasEscape = false;
                    continue;
                }
                this.append((char)nextChar);
                lastWasEscape = false;
                continue;
            }
            if (type == 1) {
                lastWasEscape = true;
                escapeCount = 0;
                escapeChar = 0;
                continue;
            }
            if (nextChar == intStopChar) {
                done = true;
            }
            this.append((char)nextChar);
        }
    }

    private void append(char character) {
        if (this.tokenBufferLength == this.tokenBuffer.length) {
            char[] newBuffer = new char[this.tokenBuffer.length * 2];
            System.arraycopy(this.tokenBuffer, 0, newBuffer, 0, this.tokenBuffer.length);
            this.tokenBuffer = newBuffer;
        }
        this.tokenBuffer[this.tokenBufferLength++] = character;
    }

    private void readComment() throws IOException {
        block4: while (true) {
            int nextChar = this.readChar();
            switch (nextChar) {
                case -1: {
                    throw new RuntimeException("Unclosed comment");
                }
                case 42: {
                    nextChar = this.readChar();
                    if (nextChar == 47) {
                        return;
                    }
                    if (nextChar == -1) {
                        throw new RuntimeException("Unclosed comment");
                    }
                    this.pushChar(nextChar);
                    continue block4;
                }
            }
        }
    }

    private void startBlock(int startToken) {
        if (this.stackCount == this.unitStack.length) {
            int[] newUS = new int[this.stackCount * 2];
            System.arraycopy(this.unitStack, 0, newUS, 0, this.stackCount);
            this.unitStack = newUS;
        }
        this.unitStack[this.stackCount++] = startToken;
    }

    private void endBlock(int endToken) {
        if (this.stackCount > 0 && this.unitStack[this.stackCount - 1] == (switch (endToken) {
            case 3 -> 2;
            case 5 -> 4;
            case 7 -> 6;
            default -> -1;
        })) {
            --this.stackCount;
        } else {
            throw new RuntimeException("Unmatched block");
        }
    }

    private boolean inBlock() {
        return this.stackCount > 0;
    }

    private int readWS() throws IOException {
        int nextChar;
        while ((nextChar = this.readChar()) != -1 && Character.isWhitespace((char)nextChar)) {
            this.readWS = true;
        }
        return nextChar;
    }

    private int readChar() throws IOException {
        if (this.didPushChar) {
            this.didPushChar = false;
            return this.pushedChar;
        }
        return this.reader.read();
    }

    private void pushChar(int tempChar) {
        if (this.didPushChar) {
            throw new RuntimeException("Can not handle look ahead of more than one character");
        }
        this.didPushChar = true;
        this.pushedChar = tempChar;
    }

    static interface CSSParserCallback {
        public void handleImport(String var1);

        public void handleSelector(String var1);

        public void startRule();

        public void handleProperty(String var1);

        public void handleValue(String var1);

        public void endRule();
    }
}

