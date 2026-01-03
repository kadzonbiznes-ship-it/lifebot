/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.spi.BreakIteratorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import jdk.internal.util.regex.Grapheme;
import sun.text.DictionaryBasedBreakIterator;
import sun.text.RuleBasedBreakIterator;
import sun.util.locale.provider.AvailableLanguageTags;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

public class BreakIteratorProviderImpl
extends BreakIteratorProvider
implements AvailableLanguageTags {
    private static final int WORD_INDEX = 0;
    private static final int LINE_INDEX = 1;
    private static final int SENTENCE_INDEX = 2;
    private final LocaleProviderAdapter.Type type;
    private final Set<String> langtags;

    public BreakIteratorProviderImpl(LocaleProviderAdapter.Type type, Set<String> langtags) {
        this.type = type;
        this.langtags = langtags;
    }

    @Override
    public Locale[] getAvailableLocales() {
        return LocaleProviderAdapter.toLocaleArray(this.langtags);
    }

    @Override
    public BreakIterator getWordInstance(Locale locale) {
        return this.getBreakInstance(locale, 0, "WordData", "WordDictionary");
    }

    @Override
    public BreakIterator getLineInstance(Locale locale) {
        return this.getBreakInstance(locale, 1, "LineData", "LineDictionary");
    }

    @Override
    public BreakIterator getCharacterInstance(Locale locale) {
        return new GraphemeBreakIterator();
    }

    @Override
    public BreakIterator getSentenceInstance(Locale locale) {
        return this.getBreakInstance(locale, 2, "SentenceData", "SentenceDictionary");
    }

    private BreakIterator getBreakInstance(Locale locale, int type, String ruleName, String dictionaryName) {
        Objects.requireNonNull(locale);
        LocaleResources lr = LocaleProviderAdapter.forJRE().getLocaleResources(locale);
        String[] classNames = (String[])lr.getBreakIteratorInfo("BreakIteratorClasses");
        String ruleFile = (String)lr.getBreakIteratorInfo(ruleName);
        byte[] ruleData = lr.getBreakIteratorResources(ruleName);
        try {
            switch (classNames[type]) {
                case "RuleBasedBreakIterator": {
                    return new RuleBasedBreakIterator(ruleFile, ruleData);
                }
                case "DictionaryBasedBreakIterator": {
                    String dictionaryFile = (String)lr.getBreakIteratorInfo(dictionaryName);
                    byte[] dictionaryData = lr.getBreakIteratorResources(dictionaryName);
                    return new DictionaryBasedBreakIterator(ruleFile, ruleData, dictionaryFile, dictionaryData);
                }
            }
            throw new IllegalArgumentException("Invalid break iterator class \"" + classNames[type] + "\"");
        }
        catch (IllegalArgumentException | MissingResourceException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    @Override
    public Set<String> getAvailableLanguageTags() {
        return this.langtags;
    }

    @Override
    public boolean isSupportedLocale(Locale locale) {
        return LocaleProviderAdapter.forType(this.type).isSupportedProviderLocale(locale, this.langtags);
    }

    static final class GraphemeBreakIterator
    extends BreakIterator {
        CharacterIterator ci;
        int offset;
        List<Integer> boundaries;
        int boundaryIndex;

        GraphemeBreakIterator() {
            this.setText("");
        }

        @Override
        public int first() {
            this.boundaryIndex = 0;
            return this.current();
        }

        @Override
        public int last() {
            this.boundaryIndex = this.boundaries.size() - 1;
            return this.current();
        }

        @Override
        public int next(int n) {
            if (n == 0) {
                return this.offset;
            }
            this.boundaryIndex += n;
            if (this.boundaryIndex < 0) {
                this.boundaryIndex = 0;
                this.current();
                return -1;
            }
            if (this.boundaryIndex >= this.boundaries.size()) {
                this.boundaryIndex = this.boundaries.size() - 1;
                this.current();
                return -1;
            }
            return this.current();
        }

        @Override
        public int next() {
            return this.next(1);
        }

        @Override
        public int previous() {
            return this.next(-1);
        }

        @Override
        public int following(int offset) {
            Integer lastBoundary = this.boundaries.get(this.boundaries.size() - 1);
            if (offset < this.boundaries.get(0) || offset > lastBoundary) {
                throw new IllegalArgumentException("offset is out of bounds: " + offset);
            }
            if (offset == this.offset && this.offset == lastBoundary) {
                return -1;
            }
            this.boundaryIndex = Collections.binarySearch(this.boundaries, Math.min(offset + 1, lastBoundary));
            if (this.boundaryIndex < 0) {
                this.boundaryIndex = -this.boundaryIndex - 1;
            }
            return this.current();
        }

        @Override
        public int current() {
            this.offset = this.boundaries.get(this.boundaryIndex);
            return this.offset;
        }

        @Override
        public CharacterIterator getText() {
            return this.ci;
        }

        @Override
        public void setText(CharacterIterator newText) {
            this.ci = newText;
            CharacterIteratorCharSequence text = new CharacterIteratorCharSequence(this.ci);
            int end = this.ci.getEndIndex();
            this.boundaries = new ArrayList<Integer>();
            int b = this.ci.getBeginIndex();
            while (b < end) {
                this.boundaries.add(b);
                b = Grapheme.nextBoundary(text, b, end);
            }
            this.boundaries.add(end);
            this.boundaryIndex = 0;
            this.offset = this.ci.getIndex();
        }

        @Override
        public boolean isBoundary(int offset) {
            if (offset < this.boundaries.get(0) || offset > this.boundaries.get(this.boundaries.size() - 1)) {
                throw new IllegalArgumentException("offset is out of bounds: " + offset);
            }
            return Collections.binarySearch(this.boundaries, offset) >= 0;
        }

        public int hashCode() {
            return Objects.hash(this.ci, this.offset, this.boundaries, this.boundaryIndex);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (!(o instanceof GraphemeBreakIterator)) return false;
            GraphemeBreakIterator that = (GraphemeBreakIterator)o;
            if (!this.ci.equals(that.ci)) return false;
            if (this.offset != that.offset) return false;
            if (!this.boundaries.equals(that.boundaries)) return false;
            if (this.boundaryIndex != that.boundaryIndex) return false;
            return true;
        }
    }

    static final class CharacterIteratorCharSequence
    implements CharSequence {
        CharacterIterator src;

        CharacterIteratorCharSequence(CharacterIterator ci) {
            this.src = ci;
        }

        @Override
        public int length() {
            return this.src.getEndIndex();
        }

        @Override
        public char charAt(int index) {
            this.src.setIndex(index);
            return this.src.current();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
    }
}

