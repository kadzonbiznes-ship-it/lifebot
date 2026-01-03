/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.lang.ref.SoftReference;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.text.spi.BreakIteratorProvider;
import java.util.Locale;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

public abstract class BreakIterator
implements Cloneable {
    public static final int DONE = -1;
    private static final int CHARACTER_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int SENTENCE_INDEX = 3;
    private static final SoftReference<BreakIteratorCache>[] iterCache = new SoftReference[4];

    protected BreakIterator() {
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public abstract int first();

    public abstract int last();

    public abstract int next(int var1);

    public abstract int next();

    public abstract int previous();

    public abstract int following(int var1);

    public int preceding(int offset) {
        int pos = this.following(offset);
        while (pos >= offset && pos != -1) {
            pos = this.previous();
        }
        return pos;
    }

    public boolean isBoundary(int offset) {
        if (offset == 0) {
            return true;
        }
        int boundary = this.following(offset - 1);
        if (boundary == -1) {
            throw new IllegalArgumentException();
        }
        return boundary == offset;
    }

    public abstract int current();

    public abstract CharacterIterator getText();

    public void setText(String newText) {
        this.setText(new StringCharacterIterator(newText));
    }

    public abstract void setText(CharacterIterator var1);

    public static BreakIterator getWordInstance() {
        return BreakIterator.getWordInstance(Locale.getDefault());
    }

    public static BreakIterator getWordInstance(Locale locale) {
        return BreakIterator.getBreakInstance(locale, 1);
    }

    public static BreakIterator getLineInstance() {
        return BreakIterator.getLineInstance(Locale.getDefault());
    }

    public static BreakIterator getLineInstance(Locale locale) {
        return BreakIterator.getBreakInstance(locale, 2);
    }

    public static BreakIterator getCharacterInstance() {
        return BreakIterator.getCharacterInstance(Locale.getDefault());
    }

    public static BreakIterator getCharacterInstance(Locale locale) {
        return BreakIterator.getBreakInstance(locale, 0);
    }

    public static BreakIterator getSentenceInstance() {
        return BreakIterator.getSentenceInstance(Locale.getDefault());
    }

    public static BreakIterator getSentenceInstance(Locale locale) {
        return BreakIterator.getBreakInstance(locale, 3);
    }

    private static BreakIterator getBreakInstance(Locale locale, int type) {
        BreakIteratorCache cache;
        if (iterCache[type] != null && (cache = iterCache[type].get()) != null && cache.getLocale().equals(locale)) {
            return cache.createBreakInstance();
        }
        BreakIterator result = BreakIterator.createBreakInstance(locale, type);
        BreakIteratorCache cache2 = new BreakIteratorCache(locale, result);
        BreakIterator.iterCache[type] = new SoftReference<BreakIteratorCache>(cache2);
        return result;
    }

    private static BreakIterator createBreakInstance(Locale locale, int type) {
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(BreakIteratorProvider.class, locale);
        BreakIterator iterator = BreakIterator.createBreakInstance(adapter, locale, type);
        if (iterator == null) {
            iterator = BreakIterator.createBreakInstance(LocaleProviderAdapter.forJRE(), locale, type);
        }
        return iterator;
    }

    private static BreakIterator createBreakInstance(LocaleProviderAdapter adapter, Locale locale, int type) {
        BreakIteratorProvider breakIteratorProvider = adapter.getBreakIteratorProvider();
        return switch (type) {
            case 0 -> breakIteratorProvider.getCharacterInstance(locale);
            case 1 -> breakIteratorProvider.getWordInstance(locale);
            case 2 -> breakIteratorProvider.getLineInstance(locale);
            case 3 -> breakIteratorProvider.getSentenceInstance(locale);
            default -> null;
        };
    }

    public static synchronized Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(BreakIteratorProvider.class);
        return pool.getAvailableLocales();
    }

    private static final class BreakIteratorCache {
        private BreakIterator iter;
        private Locale locale;

        BreakIteratorCache(Locale locale, BreakIterator iter) {
            this.locale = locale;
            this.iter = (BreakIterator)iter.clone();
        }

        Locale getLocale() {
            return this.locale;
        }

        BreakIterator createBreakInstance() {
            return (BreakIterator)this.iter.clone();
        }
    }
}

