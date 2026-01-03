/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.text.Annotation;
import java.text.AttributeEntry;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class AttributedString {
    String text;
    private static final int INITIAL_CAPACITY = 10;
    int runCount;
    int[] runStarts;
    Vector<AttributedCharacterIterator.Attribute>[] runAttributes;
    Vector<Object>[] runAttributeValues;

    AttributedString(AttributedCharacterIterator[] iterators) {
        if (iterators == null) {
            throw new NullPointerException("Iterators must not be null");
        }
        if (iterators.length == 0) {
            this.text = "";
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int counter = 0; counter < iterators.length; ++counter) {
                this.appendContents(buffer, iterators[counter]);
            }
            this.text = buffer.toString();
            if (!this.text.isEmpty()) {
                int offset = 0;
                Map<AttributedCharacterIterator.Attribute, Object> last = null;
                for (int counter = 0; counter < iterators.length; ++counter) {
                    AttributedCharacterIterator iterator = iterators[counter];
                    int start = iterator.getBeginIndex();
                    int end = iterator.getEndIndex();
                    int index = start;
                    while (index < end) {
                        iterator.setIndex(index);
                        Map<AttributedCharacterIterator.Attribute, Object> attrs = iterator.getAttributes();
                        if (AttributedString.mapsDiffer(last, attrs)) {
                            this.setAttributes(attrs, index - start + offset);
                        }
                        last = attrs;
                        index = iterator.getRunLimit();
                    }
                    offset += end - start;
                }
            }
        }
    }

    public AttributedString(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
    }

    public AttributedString(String text, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        if (text == null || attributes == null) {
            throw new NullPointerException();
        }
        this.text = text;
        if (text.isEmpty()) {
            if (attributes.isEmpty()) {
                return;
            }
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        int attributeCount = attributes.size();
        if (attributeCount > 0) {
            this.createRunAttributeDataVectors();
            Vector<AttributedCharacterIterator.Attribute> newRunAttributes = new Vector<AttributedCharacterIterator.Attribute>(attributeCount);
            Vector newRunAttributeValues = new Vector(attributeCount);
            this.runAttributes[0] = newRunAttributes;
            this.runAttributeValues[0] = newRunAttributeValues;
            for (Map.Entry<AttributedCharacterIterator.Attribute, ?> entry : attributes.entrySet()) {
                newRunAttributes.addElement(entry.getKey());
                newRunAttributeValues.addElement(entry.getValue());
            }
        }
    }

    public AttributedString(AttributedCharacterIterator text) {
        this(text, text.getBeginIndex(), text.getEndIndex(), null);
    }

    public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex) {
        this(text, beginIndex, endIndex, null);
    }

    public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        if (text == null) {
            throw new NullPointerException();
        }
        int textBeginIndex = text.getBeginIndex();
        int textEndIndex = text.getEndIndex();
        if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        StringBuilder textBuilder = new StringBuilder();
        text.setIndex(beginIndex);
        char c = text.current();
        while (text.getIndex() < endIndex) {
            textBuilder.append(c);
            c = text.next();
        }
        this.text = textBuilder.toString();
        if (beginIndex == endIndex) {
            return;
        }
        HashSet<AttributedCharacterIterator.Attribute> keys = new HashSet<AttributedCharacterIterator.Attribute>();
        if (attributes == null) {
            keys.addAll(text.getAllAttributeKeys());
        } else {
            for (int i = 0; i < attributes.length; ++i) {
                keys.add(attributes[i]);
            }
            keys.retainAll(text.getAllAttributeKeys());
        }
        if (keys.isEmpty()) {
            return;
        }
        block2: for (AttributedCharacterIterator.Attribute attributeKey : keys) {
            text.setIndex(textBeginIndex);
            while (text.getIndex() < endIndex) {
                int start = text.getRunStart(attributeKey);
                int limit = text.getRunLimit(attributeKey);
                Object value = text.getAttribute(attributeKey);
                if (value != null) {
                    if (value instanceof Annotation) {
                        if (start >= beginIndex && limit <= endIndex) {
                            this.addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                        } else if (limit > endIndex) {
                            continue block2;
                        }
                    } else {
                        if (start >= endIndex) continue block2;
                        if (limit > beginIndex) {
                            if (start < beginIndex) {
                                start = beginIndex;
                            }
                            if (limit > endIndex) {
                                limit = endIndex;
                            }
                            if (start != limit) {
                                this.addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                            }
                        }
                    }
                }
                text.setIndex(limit);
            }
        }
    }

    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        int len = this.length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        this.addAttributeImpl(attribute, value, 0, len);
    }

    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        if (beginIndex < 0 || endIndex > this.length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        this.addAttributeImpl(attribute, value, beginIndex, endIndex);
    }

    public void addAttributes(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes, int beginIndex, int endIndex) {
        if (attributes == null) {
            throw new NullPointerException();
        }
        if (beginIndex < 0 || endIndex > this.length() || beginIndex > endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        if (beginIndex == endIndex) {
            if (attributes.isEmpty()) {
                return;
            }
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        if (this.runCount == 0) {
            this.createRunAttributeDataVectors();
        }
        int beginRunIndex = this.ensureRunBreak(beginIndex);
        int endRunIndex = this.ensureRunBreak(endIndex);
        for (Map.Entry<AttributedCharacterIterator.Attribute, ?> entry : attributes.entrySet()) {
            this.addAttributeRunData(entry.getKey(), entry.getValue(), beginRunIndex, endRunIndex);
        }
    }

    private synchronized void addAttributeImpl(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (this.runCount == 0) {
            this.createRunAttributeDataVectors();
        }
        int beginRunIndex = this.ensureRunBreak(beginIndex);
        int endRunIndex = this.ensureRunBreak(endIndex);
        this.addAttributeRunData(attribute, value, beginRunIndex, endRunIndex);
    }

    private final void createRunAttributeDataVectors() {
        int[] newRunStarts = new int[10];
        Vector[] newRunAttributes = new Vector[10];
        Vector[] newRunAttributeValues = new Vector[10];
        this.runStarts = newRunStarts;
        this.runAttributes = newRunAttributes;
        this.runAttributeValues = newRunAttributeValues;
        this.runCount = 1;
    }

    private final int ensureRunBreak(int offset) {
        return this.ensureRunBreak(offset, true);
    }

    private final int ensureRunBreak(int offset, boolean copyAttrs) {
        int runIndex;
        if (offset == this.length()) {
            return this.runCount;
        }
        for (runIndex = 0; runIndex < this.runCount && this.runStarts[runIndex] < offset; ++runIndex) {
        }
        if (runIndex < this.runCount && this.runStarts[runIndex] == offset) {
            return runIndex;
        }
        int currentCapacity = this.runStarts.length;
        if (this.runCount == currentCapacity) {
            int newCapacity = currentCapacity + (currentCapacity >> 2);
            int[] newRunStarts = Arrays.copyOf(this.runStarts, newCapacity);
            Vector<AttributedCharacterIterator.Attribute>[] newRunAttributes = Arrays.copyOf(this.runAttributes, newCapacity);
            Vector<Object>[] newRunAttributeValues = Arrays.copyOf(this.runAttributeValues, newCapacity);
            this.runStarts = newRunStarts;
            this.runAttributes = newRunAttributes;
            this.runAttributeValues = newRunAttributeValues;
        }
        Vector<AttributedCharacterIterator.Attribute> newRunAttributes = null;
        Vector<Object> newRunAttributeValues = null;
        if (copyAttrs) {
            Vector<AttributedCharacterIterator.Attribute> oldRunAttributes = this.runAttributes[runIndex - 1];
            Vector<Object> oldRunAttributeValues = this.runAttributeValues[runIndex - 1];
            if (oldRunAttributes != null) {
                newRunAttributes = new Vector<AttributedCharacterIterator.Attribute>(oldRunAttributes);
            }
            if (oldRunAttributeValues != null) {
                newRunAttributeValues = new Vector<Object>(oldRunAttributeValues);
            }
        }
        ++this.runCount;
        for (int i = this.runCount - 1; i > runIndex; --i) {
            this.runStarts[i] = this.runStarts[i - 1];
            this.runAttributes[i] = this.runAttributes[i - 1];
            this.runAttributeValues[i] = this.runAttributeValues[i - 1];
        }
        this.runStarts[runIndex] = offset;
        this.runAttributes[runIndex] = newRunAttributes;
        this.runAttributeValues[runIndex] = newRunAttributeValues;
        return runIndex;
    }

    private void addAttributeRunData(AttributedCharacterIterator.Attribute attribute, Object value, int beginRunIndex, int endRunIndex) {
        for (int i = beginRunIndex; i < endRunIndex; ++i) {
            int keyValueIndex = -1;
            if (this.runAttributes[i] == null) {
                Vector newRunAttributes = new Vector();
                Vector newRunAttributeValues = new Vector();
                this.runAttributes[i] = newRunAttributes;
                this.runAttributeValues[i] = newRunAttributeValues;
            } else {
                keyValueIndex = this.runAttributes[i].indexOf(attribute);
            }
            if (keyValueIndex == -1) {
                int oldSize = this.runAttributes[i].size();
                this.runAttributes[i].addElement(attribute);
                try {
                    this.runAttributeValues[i].addElement(value);
                }
                catch (Exception e) {
                    this.runAttributes[i].setSize(oldSize);
                    this.runAttributeValues[i].setSize(oldSize);
                }
                continue;
            }
            this.runAttributeValues[i].set(keyValueIndex, value);
        }
    }

    public AttributedCharacterIterator getIterator() {
        return this.getIterator(null, 0, this.length());
    }

    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes) {
        return this.getIterator(attributes, 0, this.length());
    }

    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(attributes, beginIndex, endIndex);
    }

    int length() {
        return this.text.length();
    }

    private char charAt(int index) {
        return this.text.charAt(index);
    }

    private synchronized Object getAttribute(AttributedCharacterIterator.Attribute attribute, int runIndex) {
        Vector<AttributedCharacterIterator.Attribute> currentRunAttributes = this.runAttributes[runIndex];
        Vector<Object> currentRunAttributeValues = this.runAttributeValues[runIndex];
        if (currentRunAttributes == null) {
            return null;
        }
        int attributeIndex = currentRunAttributes.indexOf(attribute);
        if (attributeIndex != -1) {
            return currentRunAttributeValues.elementAt(attributeIndex);
        }
        return null;
    }

    private Object getAttributeCheckRange(AttributedCharacterIterator.Attribute attribute, int runIndex, int beginIndex, int endIndex) {
        Object value = this.getAttribute(attribute, runIndex);
        if (value instanceof Annotation) {
            int textLength;
            if (beginIndex > 0) {
                int currIndex = runIndex;
                int runStart = this.runStarts[currIndex];
                while (runStart >= beginIndex && AttributedString.valuesMatch(value, this.getAttribute(attribute, currIndex - 1))) {
                    runStart = this.runStarts[--currIndex];
                }
                if (runStart < beginIndex) {
                    return null;
                }
            }
            if (endIndex < (textLength = this.length())) {
                int runLimit;
                int currIndex = runIndex;
                int n = runLimit = currIndex < this.runCount - 1 ? this.runStarts[currIndex + 1] : textLength;
                while (runLimit <= endIndex && AttributedString.valuesMatch(value, this.getAttribute(attribute, currIndex + 1))) {
                    runLimit = ++currIndex < this.runCount - 1 ? this.runStarts[currIndex + 1] : textLength;
                }
                if (runLimit > endIndex) {
                    return null;
                }
            }
        }
        return value;
    }

    private boolean attributeValuesMatch(Set<? extends AttributedCharacterIterator.Attribute> attributes, int runIndex1, int runIndex2) {
        for (AttributedCharacterIterator.Attribute attribute : attributes) {
            if (AttributedString.valuesMatch(this.getAttribute(attribute, runIndex1), this.getAttribute(attribute, runIndex2))) continue;
            return false;
        }
        return true;
    }

    private static final boolean valuesMatch(Object value1, Object value2) {
        if (value1 == null) {
            return value2 == null;
        }
        return value1.equals(value2);
    }

    private final void appendContents(StringBuilder buf, CharacterIterator iterator) {
        int index = iterator.getBeginIndex();
        int end = iterator.getEndIndex();
        while (index < end) {
            iterator.setIndex(index++);
            buf.append(iterator.current());
        }
    }

    private void setAttributes(Map<AttributedCharacterIterator.Attribute, Object> attrs, int offset) {
        int size;
        if (this.runCount == 0) {
            this.createRunAttributeDataVectors();
        }
        int index = this.ensureRunBreak(offset, false);
        if (attrs != null && (size = attrs.size()) > 0) {
            Vector<AttributedCharacterIterator.Attribute> runAttrs = new Vector<AttributedCharacterIterator.Attribute>(size);
            Vector<Object> runValues = new Vector<Object>(size);
            for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : attrs.entrySet()) {
                runAttrs.add(entry.getKey());
                runValues.add(entry.getValue());
            }
            this.runAttributes[index] = runAttrs;
            this.runAttributeValues[index] = runValues;
        }
    }

    private static <K, V> boolean mapsDiffer(Map<K, V> last, Map<K, V> attrs) {
        if (last == null) {
            return attrs != null && attrs.size() > 0;
        }
        return !last.equals(attrs);
    }

    private final class AttributedStringIterator
    implements AttributedCharacterIterator {
        private int beginIndex;
        private int endIndex;
        private AttributedCharacterIterator.Attribute[] relevantAttributes;
        private int currentIndex;
        private int currentRunIndex;
        private int currentRunStart;
        private int currentRunLimit;

        AttributedStringIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex, int endIndex) {
            if (beginIndex < 0 || beginIndex > endIndex || endIndex > AttributedString.this.length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.currentIndex = beginIndex;
            this.updateRunInfo();
            if (attributes != null) {
                this.relevantAttributes = (AttributedCharacterIterator.Attribute[])attributes.clone();
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AttributedStringIterator)) {
                return false;
            }
            AttributedStringIterator that = (AttributedStringIterator)obj;
            if (AttributedString.this != that.getString()) {
                return false;
            }
            return this.currentIndex == that.currentIndex && this.beginIndex == that.beginIndex && this.endIndex == that.endIndex;
        }

        public int hashCode() {
            return AttributedString.this.text.hashCode() ^ this.currentIndex ^ this.beginIndex ^ this.endIndex;
        }

        @Override
        public Object clone() {
            try {
                AttributedStringIterator other = (AttributedStringIterator)super.clone();
                return other;
            }
            catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }

        @Override
        public char first() {
            return this.internalSetIndex(this.beginIndex);
        }

        @Override
        public char last() {
            if (this.endIndex == this.beginIndex) {
                return this.internalSetIndex(this.endIndex);
            }
            return this.internalSetIndex(this.endIndex - 1);
        }

        @Override
        public char current() {
            if (this.currentIndex == this.endIndex) {
                return '\uffff';
            }
            return AttributedString.this.charAt(this.currentIndex);
        }

        @Override
        public char next() {
            if (this.currentIndex < this.endIndex) {
                return this.internalSetIndex(this.currentIndex + 1);
            }
            return '\uffff';
        }

        @Override
        public char previous() {
            if (this.currentIndex > this.beginIndex) {
                return this.internalSetIndex(this.currentIndex - 1);
            }
            return '\uffff';
        }

        @Override
        public char setIndex(int position) {
            if (position < this.beginIndex || position > this.endIndex) {
                throw new IllegalArgumentException("Invalid index");
            }
            return this.internalSetIndex(position);
        }

        @Override
        public int getBeginIndex() {
            return this.beginIndex;
        }

        @Override
        public int getEndIndex() {
            return this.endIndex;
        }

        @Override
        public int getIndex() {
            return this.currentIndex;
        }

        @Override
        public int getRunStart() {
            return this.currentRunStart;
        }

        @Override
        public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
            if (this.currentRunStart == this.beginIndex || this.currentRunIndex == -1) {
                return this.currentRunStart;
            }
            Object value = this.getAttribute(attribute);
            int runStart = this.currentRunStart;
            int runIndex = this.currentRunIndex;
            while (runStart > this.beginIndex && AttributedString.valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex - 1))) {
                runStart = AttributedString.this.runStarts[--runIndex];
            }
            if (runStart < this.beginIndex) {
                runStart = this.beginIndex;
            }
            return runStart;
        }

        @Override
        public int getRunStart(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
            if (this.currentRunStart == this.beginIndex || this.currentRunIndex == -1) {
                return this.currentRunStart;
            }
            int runStart = this.currentRunStart;
            int runIndex = this.currentRunIndex;
            while (runStart > this.beginIndex && AttributedString.this.attributeValuesMatch(attributes, this.currentRunIndex, runIndex - 1)) {
                runStart = AttributedString.this.runStarts[--runIndex];
            }
            if (runStart < this.beginIndex) {
                runStart = this.beginIndex;
            }
            return runStart;
        }

        @Override
        public int getRunLimit() {
            return this.currentRunLimit;
        }

        @Override
        public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
            if (this.currentRunLimit == this.endIndex || this.currentRunIndex == -1) {
                return this.currentRunLimit;
            }
            Object value = this.getAttribute(attribute);
            int runLimit = this.currentRunLimit;
            int runIndex = this.currentRunIndex;
            while (runLimit < this.endIndex && AttributedString.valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex + 1))) {
                runLimit = ++runIndex < AttributedString.this.runCount - 1 ? AttributedString.this.runStarts[runIndex + 1] : this.endIndex;
            }
            if (runLimit > this.endIndex) {
                runLimit = this.endIndex;
            }
            return runLimit;
        }

        @Override
        public int getRunLimit(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
            if (this.currentRunLimit == this.endIndex || this.currentRunIndex == -1) {
                return this.currentRunLimit;
            }
            int runLimit = this.currentRunLimit;
            int runIndex = this.currentRunIndex;
            while (runLimit < this.endIndex && AttributedString.this.attributeValuesMatch(attributes, this.currentRunIndex, runIndex + 1)) {
                runLimit = ++runIndex < AttributedString.this.runCount - 1 ? AttributedString.this.runStarts[runIndex + 1] : this.endIndex;
            }
            if (runLimit > this.endIndex) {
                runLimit = this.endIndex;
            }
            return runLimit;
        }

        @Override
        public Map<AttributedCharacterIterator.Attribute, Object> getAttributes() {
            if (AttributedString.this.runAttributes == null || this.currentRunIndex == -1 || AttributedString.this.runAttributes[this.currentRunIndex] == null) {
                return new Hashtable<AttributedCharacterIterator.Attribute, Object>();
            }
            return new AttributeMap(this.currentRunIndex, this.beginIndex, this.endIndex);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Set<AttributedCharacterIterator.Attribute> getAllAttributeKeys() {
            if (AttributedString.this.runAttributes == null) {
                return new HashSet<AttributedCharacterIterator.Attribute>();
            }
            AttributedString attributedString = AttributedString.this;
            synchronized (attributedString) {
                HashSet<AttributedCharacterIterator.Attribute> keys = new HashSet<AttributedCharacterIterator.Attribute>();
                for (int i = 0; i < AttributedString.this.runCount; ++i) {
                    Vector<AttributedCharacterIterator.Attribute> currentRunAttributes;
                    if (AttributedString.this.runStarts[i] >= this.endIndex || i != AttributedString.this.runCount - 1 && AttributedString.this.runStarts[i + 1] <= this.beginIndex || (currentRunAttributes = AttributedString.this.runAttributes[i]) == null) continue;
                    int j = currentRunAttributes.size();
                    while (j-- > 0) {
                        keys.add(currentRunAttributes.get(j));
                    }
                }
                return keys;
            }
        }

        @Override
        public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
            int runIndex = this.currentRunIndex;
            if (runIndex < 0) {
                return null;
            }
            return AttributedString.this.getAttributeCheckRange(attribute, runIndex, this.beginIndex, this.endIndex);
        }

        private AttributedString getString() {
            return AttributedString.this;
        }

        private char internalSetIndex(int position) {
            this.currentIndex = position;
            if (position < this.currentRunStart || position >= this.currentRunLimit) {
                this.updateRunInfo();
            }
            if (this.currentIndex == this.endIndex) {
                return '\uffff';
            }
            return AttributedString.this.charAt(position);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void updateRunInfo() {
            if (this.currentIndex == this.endIndex) {
                this.currentRunStart = this.currentRunLimit = this.endIndex;
                this.currentRunIndex = -1;
            } else {
                AttributedString attributedString = AttributedString.this;
                synchronized (attributedString) {
                    int runIndex;
                    for (runIndex = -1; runIndex < AttributedString.this.runCount - 1 && AttributedString.this.runStarts[runIndex + 1] <= this.currentIndex; ++runIndex) {
                    }
                    this.currentRunIndex = runIndex;
                    if (runIndex >= 0) {
                        this.currentRunStart = AttributedString.this.runStarts[runIndex];
                        if (this.currentRunStart < this.beginIndex) {
                            this.currentRunStart = this.beginIndex;
                        }
                    } else {
                        this.currentRunStart = this.beginIndex;
                    }
                    if (runIndex < AttributedString.this.runCount - 1) {
                        this.currentRunLimit = AttributedString.this.runStarts[runIndex + 1];
                        if (this.currentRunLimit > this.endIndex) {
                            this.currentRunLimit = this.endIndex;
                        }
                    } else {
                        this.currentRunLimit = this.endIndex;
                    }
                }
            }
        }
    }

    private final class AttributeMap
    extends AbstractMap<AttributedCharacterIterator.Attribute, Object> {
        int runIndex;
        int beginIndex;
        int endIndex;

        AttributeMap(int runIndex, int beginIndex, int endIndex) {
            this.runIndex = runIndex;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Set<Map.Entry<AttributedCharacterIterator.Attribute, Object>> entrySet() {
            HashSet<Map.Entry<AttributedCharacterIterator.Attribute, Object>> set = new HashSet<Map.Entry<AttributedCharacterIterator.Attribute, Object>>();
            AttributedString attributedString = AttributedString.this;
            synchronized (attributedString) {
                int size = AttributedString.this.runAttributes[this.runIndex].size();
                for (int i = 0; i < size; ++i) {
                    AttributedCharacterIterator.Attribute key = AttributedString.this.runAttributes[this.runIndex].get(i);
                    Object value = AttributedString.this.runAttributeValues[this.runIndex].get(i);
                    if (value instanceof Annotation && (value = AttributedString.this.getAttributeCheckRange(key, this.runIndex, this.beginIndex, this.endIndex)) == null) continue;
                    AttributeEntry entry = new AttributeEntry(key, value);
                    set.add(entry);
                }
            }
            return set;
        }

        @Override
        public Object get(Object key) {
            return AttributedString.this.getAttributeCheckRange((AttributedCharacterIterator.Attribute)key, this.runIndex, this.beginIndex, this.endIndex);
        }
    }
}

