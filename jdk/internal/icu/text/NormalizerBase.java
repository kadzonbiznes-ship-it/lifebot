/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.text;

import java.text.CharacterIterator;
import java.text.Normalizer;
import jdk.internal.icu.impl.Norm2AllModes;
import jdk.internal.icu.text.FilteredNormalizer2;
import jdk.internal.icu.text.Normalizer2;
import jdk.internal.icu.text.UCharacterIterator;
import jdk.internal.icu.text.UnicodeSet;

public final class NormalizerBase
implements Cloneable {
    private UCharacterIterator text;
    private Normalizer2 norm2;
    private Mode mode;
    private int options;
    private int currentIndex;
    private int nextIndex;
    private StringBuilder buffer;
    private int bufferPos;
    public static final int UNICODE_3_2 = 32;
    public static final int UNICODE_3_2_0_ORIGINAL = 32;
    public static final int UNICODE_LATEST = 0;
    public static final int DONE = -1;
    public static final Mode NONE = new NONEMode();
    public static final Mode NFD = new NFDMode();
    public static final Mode NFKD = new NFKDMode();
    public static final Mode NFC = new NFCMode();
    public static final Mode NFKC = new NFKCMode();

    private static Mode toMode(Normalizer.Form form) {
        switch (form) {
            case NFC: {
                return NFC;
            }
            case NFD: {
                return NFD;
            }
            case NFKC: {
                return NFKC;
            }
            case NFKD: {
                return NFKD;
            }
        }
        throw new IllegalArgumentException("Unexpected normalization form: " + (Object)((Object)form));
    }

    public NormalizerBase(String str, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    public NormalizerBase(String str, Mode mode) {
        this(str, mode, 0);
    }

    public NormalizerBase(CharacterIterator iter, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance((CharacterIterator)iter.clone());
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    public NormalizerBase(CharacterIterator iter, Mode mode) {
        this(iter, mode, 0);
    }

    public Object clone() {
        try {
            NormalizerBase copy = (NormalizerBase)super.clone();
            copy.text = (UCharacterIterator)this.text.clone();
            copy.mode = this.mode;
            copy.options = this.options;
            copy.norm2 = this.norm2;
            copy.buffer = new StringBuilder(this.buffer);
            copy.bufferPos = this.bufferPos;
            copy.currentIndex = this.currentIndex;
            copy.nextIndex = this.nextIndex;
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public static String normalize(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).normalize(str);
    }

    public static String normalize(String str, Normalizer.Form form) {
        return NormalizerBase.normalize(str, NormalizerBase.toMode(form), 0);
    }

    public static String normalize(String str, Normalizer.Form form, int options) {
        return NormalizerBase.normalize(str, NormalizerBase.toMode(form), options);
    }

    public static boolean isNormalized(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(str);
    }

    public static boolean isNormalized(String str, Normalizer.Form form) {
        return NormalizerBase.isNormalized(str, NormalizerBase.toMode(form), 0);
    }

    public static boolean isNormalized(String str, Normalizer.Form form, int options) {
        return NormalizerBase.isNormalized(str, NormalizerBase.toMode(form), options);
    }

    public int current() {
        if (this.bufferPos < this.buffer.length() || this.nextNormalize()) {
            return this.buffer.codePointAt(this.bufferPos);
        }
        return -1;
    }

    public int next() {
        if (this.bufferPos < this.buffer.length() || this.nextNormalize()) {
            int c = this.buffer.codePointAt(this.bufferPos);
            this.bufferPos += Character.charCount(c);
            return c;
        }
        return -1;
    }

    public int previous() {
        if (this.bufferPos > 0 || this.previousNormalize()) {
            int c = this.buffer.codePointBefore(this.bufferPos);
            this.bufferPos -= Character.charCount(c);
            return c;
        }
        return -1;
    }

    public void reset() {
        this.text.setIndex(0);
        this.nextIndex = 0;
        this.currentIndex = 0;
        this.clearBuffer();
    }

    public void setIndexOnly(int index) {
        this.text.setIndex(index);
        this.currentIndex = this.nextIndex = index;
        this.clearBuffer();
    }

    public int setIndex(int index) {
        this.setIndexOnly(index);
        return this.current();
    }

    @Deprecated
    public int getBeginIndex() {
        return 0;
    }

    @Deprecated
    public int getEndIndex() {
        return this.endIndex();
    }

    public int getIndex() {
        if (this.bufferPos < this.buffer.length()) {
            return this.currentIndex;
        }
        return this.nextIndex;
    }

    public int endIndex() {
        return this.text.getLength();
    }

    public void setMode(Mode newMode) {
        this.mode = newMode;
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setText(String newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.reset();
    }

    public void setText(CharacterIterator newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.nextIndex = 0;
        this.currentIndex = 0;
        this.clearBuffer();
    }

    private void clearBuffer() {
        this.buffer.setLength(0);
        this.bufferPos = 0;
    }

    private boolean nextNormalize() {
        this.clearBuffer();
        this.currentIndex = this.nextIndex;
        this.text.setIndex(this.nextIndex);
        int c = this.text.nextCodePoint();
        if (c < 0) {
            return false;
        }
        StringBuilder segment = new StringBuilder().appendCodePoint(c);
        while ((c = this.text.nextCodePoint()) >= 0) {
            if (this.norm2.hasBoundaryBefore(c)) {
                this.text.moveCodePointIndex(-1);
                break;
            }
            segment.appendCodePoint(c);
        }
        this.nextIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence)segment, this.buffer);
        return this.buffer.length() != 0;
    }

    private boolean previousNormalize() {
        int c;
        this.clearBuffer();
        this.nextIndex = this.currentIndex;
        this.text.setIndex(this.currentIndex);
        StringBuilder segment = new StringBuilder();
        while ((c = this.text.previousCodePoint()) >= 0) {
            if (c <= 65535) {
                segment.insert(0, (char)c);
            } else {
                segment.insert(0, Character.toChars(c));
            }
            if (!this.norm2.hasBoundaryBefore(c)) continue;
        }
        this.currentIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence)segment, this.buffer);
        this.bufferPos = this.buffer.length();
        return this.buffer.length() != 0;
    }

    public static abstract class Mode {
        @Deprecated
        protected Mode() {
        }

        @Deprecated
        protected abstract Normalizer2 getNormalizer2(int var1);
    }

    private static final class NONEMode
    extends Mode {
        private NONEMode() {
        }

        @Override
        protected Normalizer2 getNormalizer2(int options) {
            return Norm2AllModes.NOOP_NORMALIZER2;
        }
    }

    private static final class NFDMode
    extends Mode {
        private NFDMode() {
        }

        @Override
        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFD32ModeImpl.INSTANCE.normalizer2 : NFDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKDMode
    extends Mode {
        private NFKDMode() {
        }

        @Override
        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFKD32ModeImpl.INSTANCE.normalizer2 : NFKDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFCMode
    extends Mode {
        private NFCMode() {
        }

        @Override
        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFC32ModeImpl.INSTANCE.normalizer2 : NFCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKCMode
    extends Mode {
        private NFKCMode() {
        }

        @Override
        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFKC32ModeImpl.INSTANCE.normalizer2 : NFKCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKCInstance(), Unicode32.INSTANCE));

        private NFKC32ModeImpl() {
        }
    }

    private static final class NFC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFCInstance(), Unicode32.INSTANCE));

        private NFC32ModeImpl() {
        }
    }

    private static final class NFKD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKDInstance(), Unicode32.INSTANCE));

        private NFKD32ModeImpl() {
        }
    }

    private static final class NFD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFDInstance(), Unicode32.INSTANCE));

        private NFD32ModeImpl() {
        }
    }

    private static final class Unicode32 {
        private static final UnicodeSet INSTANCE = new UnicodeSet("[:age=3.2:]").freeze();

        private Unicode32() {
        }
    }

    private static final class NFKCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKCInstance());

        private NFKCModeImpl() {
        }
    }

    private static final class NFCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFCInstance());

        private NFCModeImpl() {
        }
    }

    private static final class NFKDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKDInstance());

        private NFKDModeImpl() {
        }
    }

    private static final class NFDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFDInstance());

        private NFDModeImpl() {
        }
    }

    private static final class ModeImpl {
        private final Normalizer2 normalizer2;

        private ModeImpl(Normalizer2 n2) {
            this.normalizer2 = n2;
        }
    }
}

