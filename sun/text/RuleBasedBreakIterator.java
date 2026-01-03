/*
 * Decompiled with CFR 0.152.
 */
package sun.text;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.MissingResourceException;
import sun.text.CompactByteArray;
import sun.text.SupplementaryCharacterData;

public class RuleBasedBreakIterator
extends BreakIterator {
    protected static final byte IGNORE = -1;
    private static final short START_STATE = 1;
    private static final short STOP_STATE = 0;
    static final byte[] LABEL = new byte[]{66, 73, 100, 97, 116, 97, 0};
    static final int LABEL_LENGTH = LABEL.length;
    static final byte supportedVersion = 1;
    private static final int BMP_INDICES_LENGTH = 512;
    private CompactByteArray charCategoryTable = null;
    private SupplementaryCharacterData supplementaryCharCategoryTable = null;
    private short[] stateTable = null;
    private short[] backwardsStateTable = null;
    private boolean[] endStates = null;
    private boolean[] lookaheadStates = null;
    private byte[] additionalData = null;
    private int numCategories;
    private CharacterIterator text = null;
    private long checksum;
    private int cachedLastKnownBreak = -1;

    public RuleBasedBreakIterator(String ruleFile, byte[] ruleData) {
        ByteBuffer bb = ByteBuffer.wrap(ruleData);
        try {
            this.validateRuleData(ruleFile, bb);
            this.setupTables(ruleFile, bb);
        }
        catch (BufferUnderflowException bue) {
            MissingResourceException e = new MissingResourceException("Corrupted rule data file", ruleFile, "");
            e.initCause(bue);
            throw e;
        }
    }

    private void setupTables(String ruleFile, ByteBuffer bb) {
        int i;
        int stateTableLength = bb.getInt();
        int backwardsStateTableLength = bb.getInt();
        int endStatesLength = bb.getInt();
        int lookaheadStatesLength = bb.getInt();
        int BMPdataLength = bb.getInt();
        int nonBMPdataLength = bb.getInt();
        int additionalDataLength = bb.getInt();
        this.checksum = bb.getLong();
        this.stateTable = new short[stateTableLength];
        for (i = 0; i < stateTableLength; ++i) {
            this.stateTable[i] = bb.getShort();
        }
        this.backwardsStateTable = new short[backwardsStateTableLength];
        for (i = 0; i < backwardsStateTableLength; ++i) {
            this.backwardsStateTable[i] = bb.getShort();
        }
        this.endStates = new boolean[endStatesLength];
        for (i = 0; i < endStatesLength; ++i) {
            this.endStates[i] = bb.get() == 1;
        }
        this.lookaheadStates = new boolean[lookaheadStatesLength];
        for (i = 0; i < lookaheadStatesLength; ++i) {
            this.lookaheadStates[i] = bb.get() == 1;
        }
        short[] temp1 = new short[512];
        for (int i2 = 0; i2 < 512; ++i2) {
            temp1[i2] = bb.getShort();
        }
        byte[] temp2 = new byte[BMPdataLength];
        bb.get(temp2);
        this.charCategoryTable = new CompactByteArray(temp1, temp2);
        int[] temp3 = new int[nonBMPdataLength];
        for (int i3 = 0; i3 < nonBMPdataLength; ++i3) {
            temp3[i3] = bb.getInt();
        }
        this.supplementaryCharCategoryTable = new SupplementaryCharacterData(temp3);
        if (additionalDataLength > 0) {
            this.additionalData = new byte[additionalDataLength];
            bb.get(this.additionalData);
        }
        assert (bb.position() == bb.limit());
        this.numCategories = this.stateTable.length / this.endStates.length;
    }

    void validateRuleData(String ruleFile, ByteBuffer bb) {
        for (int i = 0; i < LABEL_LENGTH; ++i) {
            if (bb.get() == LABEL[i]) continue;
            throw new MissingResourceException("Wrong magic number", ruleFile, "");
        }
        byte version = bb.get();
        if (version != 1) {
            throw new MissingResourceException("Unsupported version(" + version + ")", ruleFile, "");
        }
        int len = bb.getInt();
        if (bb.position() + len != bb.limit()) {
            throw new MissingResourceException("Wrong data length", ruleFile, "");
        }
    }

    byte[] getAdditionalData() {
        return this.additionalData;
    }

    void setAdditionalData(byte[] b) {
        this.additionalData = b;
    }

    @Override
    public Object clone() {
        RuleBasedBreakIterator result = (RuleBasedBreakIterator)super.clone();
        if (this.text != null) {
            result.text = (CharacterIterator)this.text.clone();
        }
        return result;
    }

    public boolean equals(Object that) {
        try {
            if (that == null) {
                return false;
            }
            RuleBasedBreakIterator other = (RuleBasedBreakIterator)that;
            if (this.checksum != other.checksum) {
                return false;
            }
            if (this.text == null) {
                return other.text == null;
            }
            return this.text.equals(other.text);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "[checksum=0x" + Long.toHexString(this.checksum) + ']';
    }

    public int hashCode() {
        return (int)this.checksum;
    }

    @Override
    public int first() {
        CharacterIterator t = this.getText();
        t.first();
        return t.getIndex();
    }

    @Override
    public int last() {
        CharacterIterator t = this.getText();
        t.setIndex(t.getEndIndex());
        return t.getIndex();
    }

    @Override
    public int next(int n) {
        int result = this.current();
        while (n > 0) {
            result = this.handleNext();
            --n;
        }
        while (n < 0) {
            result = this.previous();
            ++n;
        }
        return result;
    }

    @Override
    public int next() {
        return this.handleNext();
    }

    @Override
    public int previous() {
        CharacterIterator text = this.getText();
        if (this.current() == text.getBeginIndex()) {
            return -1;
        }
        int lastResult = this.cachedLastKnownBreak;
        int start = this.current();
        if (lastResult >= start || lastResult <= -1) {
            this.getPrevious();
            lastResult = this.handlePrevious();
        } else {
            text.setIndex(lastResult);
        }
        int result = lastResult;
        while (result != -1 && result < start) {
            lastResult = result;
            result = this.handleNext();
        }
        text.setIndex(lastResult);
        this.cachedLastKnownBreak = lastResult;
        return lastResult;
    }

    private int getPrevious() {
        char c2 = this.text.previous();
        if (Character.isLowSurrogate(c2) && this.text.getIndex() > this.text.getBeginIndex()) {
            char c1 = this.text.previous();
            if (Character.isHighSurrogate(c1)) {
                return Character.toCodePoint(c1, c2);
            }
            this.text.next();
        }
        return c2;
    }

    int getCurrent() {
        char c1 = this.text.current();
        if (Character.isHighSurrogate(c1) && this.text.getIndex() < this.text.getEndIndex()) {
            char c2 = this.text.next();
            this.text.previous();
            if (Character.isLowSurrogate(c2)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    private int getCurrentCodePointCount() {
        char c1 = this.text.current();
        if (Character.isHighSurrogate(c1) && this.text.getIndex() < this.text.getEndIndex()) {
            char c2 = this.text.next();
            this.text.previous();
            if (Character.isLowSurrogate(c2)) {
                return 2;
            }
        }
        return 1;
    }

    int getNext() {
        int endIndex;
        int index = this.text.getIndex();
        if (index == (endIndex = this.text.getEndIndex()) || (index += this.getCurrentCodePointCount()) >= endIndex) {
            return 65535;
        }
        this.text.setIndex(index);
        return this.getCurrent();
    }

    private int getNextIndex() {
        int endIndex;
        int index = this.text.getIndex() + this.getCurrentCodePointCount();
        if (index > (endIndex = this.text.getEndIndex())) {
            return endIndex;
        }
        return index;
    }

    protected static final void checkOffset(int offset, CharacterIterator text) {
        if (offset < text.getBeginIndex() || offset > text.getEndIndex()) {
            throw new IllegalArgumentException("offset out of bounds");
        }
    }

    @Override
    public int following(int offset) {
        CharacterIterator text = this.getText();
        RuleBasedBreakIterator.checkOffset(offset, text);
        text.setIndex(offset);
        if (offset == text.getBeginIndex()) {
            this.cachedLastKnownBreak = this.handleNext();
            return this.cachedLastKnownBreak;
        }
        int result = this.cachedLastKnownBreak;
        if (result >= offset || result <= -1) {
            result = this.handlePrevious();
        } else {
            text.setIndex(result);
        }
        while (result != -1 && result <= offset) {
            result = this.handleNext();
        }
        this.cachedLastKnownBreak = result;
        return result;
    }

    @Override
    public int preceding(int offset) {
        CharacterIterator text = this.getText();
        RuleBasedBreakIterator.checkOffset(offset, text);
        text.setIndex(offset);
        return this.previous();
    }

    @Override
    public boolean isBoundary(int offset) {
        CharacterIterator text = this.getText();
        RuleBasedBreakIterator.checkOffset(offset, text);
        if (offset == text.getBeginIndex()) {
            return true;
        }
        return this.following(offset - 1) == offset;
    }

    @Override
    public int current() {
        return this.getText().getIndex();
    }

    @Override
    public CharacterIterator getText() {
        if (this.text == null) {
            this.text = new StringCharacterIterator("");
        }
        return this.text;
    }

    @Override
    public void setText(CharacterIterator newText) {
        boolean goodIterator;
        int end = newText.getEndIndex();
        try {
            newText.setIndex(end);
            goodIterator = newText.getIndex() == end;
        }
        catch (IllegalArgumentException e) {
            goodIterator = false;
        }
        this.text = goodIterator ? newText : new SafeCharIterator(newText);
        this.text.first();
        this.cachedLastKnownBreak = -1;
    }

    protected int handleNext() {
        CharacterIterator text = this.getText();
        if (text.getIndex() == text.getEndIndex()) {
            return -1;
        }
        int result = this.getNextIndex();
        int lookaheadResult = 0;
        int state = 1;
        int c = this.getCurrent();
        while (c != 65535 && state != 0) {
            int category = this.lookupCategory(c);
            if (category != -1) {
                state = this.lookupState(state, category);
            }
            if (this.lookaheadStates[state]) {
                if (this.endStates[state]) {
                    result = lookaheadResult;
                } else {
                    lookaheadResult = this.getNextIndex();
                }
            } else if (this.endStates[state]) {
                result = this.getNextIndex();
            }
            c = this.getNext();
        }
        if (c == 65535 && lookaheadResult == text.getEndIndex()) {
            result = lookaheadResult;
        }
        text.setIndex(result);
        return result;
    }

    protected int handlePrevious() {
        CharacterIterator text = this.getText();
        int state = 1;
        int category = 0;
        int lastCategory = 0;
        int c = this.getCurrent();
        while (c != 65535 && state != 0) {
            lastCategory = category;
            category = this.lookupCategory(c);
            if (category != -1) {
                state = this.lookupBackwardState(state, category);
            }
            c = this.getPrevious();
        }
        if (c != 65535) {
            if (lastCategory != -1) {
                this.getNext();
                this.getNext();
            } else {
                this.getNext();
            }
        }
        return text.getIndex();
    }

    protected int lookupCategory(int c) {
        if (c < 65536) {
            return this.charCategoryTable.elementAt((char)c);
        }
        return this.supplementaryCharCategoryTable.getValue(c);
    }

    protected int lookupState(int state, int category) {
        return this.stateTable[state * this.numCategories + category];
    }

    protected int lookupBackwardState(int state, int category) {
        return this.backwardsStateTable[state * this.numCategories + category];
    }

    private static final class SafeCharIterator
    implements CharacterIterator,
    Cloneable {
        private CharacterIterator base;
        private int rangeStart;
        private int rangeLimit;
        private int currentIndex;

        SafeCharIterator(CharacterIterator base) {
            this.base = base;
            this.rangeStart = base.getBeginIndex();
            this.rangeLimit = base.getEndIndex();
            this.currentIndex = base.getIndex();
        }

        @Override
        public char first() {
            return this.setIndex(this.rangeStart);
        }

        @Override
        public char last() {
            return this.setIndex(this.rangeLimit - 1);
        }

        @Override
        public char current() {
            if (this.currentIndex < this.rangeStart || this.currentIndex >= this.rangeLimit) {
                return '\uffff';
            }
            return this.base.setIndex(this.currentIndex);
        }

        @Override
        public char next() {
            ++this.currentIndex;
            if (this.currentIndex >= this.rangeLimit) {
                this.currentIndex = this.rangeLimit;
                return '\uffff';
            }
            return this.base.setIndex(this.currentIndex);
        }

        @Override
        public char previous() {
            --this.currentIndex;
            if (this.currentIndex < this.rangeStart) {
                this.currentIndex = this.rangeStart;
                return '\uffff';
            }
            return this.base.setIndex(this.currentIndex);
        }

        @Override
        public char setIndex(int i) {
            if (i < this.rangeStart || i > this.rangeLimit) {
                throw new IllegalArgumentException("Invalid position");
            }
            this.currentIndex = i;
            return this.current();
        }

        @Override
        public int getBeginIndex() {
            return this.rangeStart;
        }

        @Override
        public int getEndIndex() {
            return this.rangeLimit;
        }

        @Override
        public int getIndex() {
            return this.currentIndex;
        }

        @Override
        public Object clone() {
            CharacterIterator copyOfBase;
            SafeCharIterator copy = null;
            try {
                copy = (SafeCharIterator)super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new Error("Clone not supported: " + e);
            }
            copy.base = copyOfBase = (CharacterIterator)this.base.clone();
            return copy;
        }
    }
}

