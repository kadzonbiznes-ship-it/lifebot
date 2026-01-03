/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.IOException;
import jdk.internal.icu.impl.NormalizerImpl;
import jdk.internal.icu.text.Normalizer2;

public final class Norm2AllModes {
    public final NormalizerImpl impl;
    public final ComposeNormalizer2 comp;
    public final DecomposeNormalizer2 decomp;
    public static final NoopNormalizer2 NOOP_NORMALIZER2 = new NoopNormalizer2();

    private Norm2AllModes(NormalizerImpl ni) {
        this.impl = ni;
        this.comp = new ComposeNormalizer2(ni, false);
        this.decomp = new DecomposeNormalizer2(ni);
    }

    private static Norm2AllModes getInstanceFromSingleton(Norm2AllModesSingleton singleton) {
        if (singleton.exception != null) {
            throw singleton.exception;
        }
        return singleton.allModes;
    }

    public static Norm2AllModes getNFCInstance() {
        return Norm2AllModes.getInstanceFromSingleton(NFCSingleton.INSTANCE);
    }

    public static Norm2AllModes getNFKCInstance() {
        return Norm2AllModes.getInstanceFromSingleton(NFKCSingleton.INSTANCE);
    }

    public static final class ComposeNormalizer2
    extends Normalizer2WithImpl {
        private final boolean onlyContiguous;

        public ComposeNormalizer2(NormalizerImpl ni, boolean fcc) {
            super(ni);
            this.onlyContiguous = fcc;
        }

        @Override
        protected void normalize(CharSequence src, NormalizerImpl.ReorderingBuffer buffer) {
            this.impl.compose(src, 0, src.length(), this.onlyContiguous, true, buffer);
        }

        @Override
        protected void normalizeAndAppend(CharSequence src, boolean doNormalize, NormalizerImpl.ReorderingBuffer buffer) {
            this.impl.composeAndAppend(src, doNormalize, this.onlyContiguous, buffer);
        }

        @Override
        public boolean isNormalized(CharSequence s) {
            return this.impl.compose(s, 0, s.length(), this.onlyContiguous, false, new NormalizerImpl.ReorderingBuffer(this.impl, new StringBuilder(), 5));
        }

        @Override
        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.composeQuickCheck(s, 0, s.length(), this.onlyContiguous, true) >>> 1;
        }

        @Override
        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasCompBoundaryBefore(c);
        }
    }

    public static final class DecomposeNormalizer2
    extends Normalizer2WithImpl {
        public DecomposeNormalizer2(NormalizerImpl ni) {
            super(ni);
        }

        @Override
        protected void normalize(CharSequence src, NormalizerImpl.ReorderingBuffer buffer) {
            this.impl.decompose(src, 0, src.length(), buffer);
        }

        @Override
        protected void normalizeAndAppend(CharSequence src, boolean doNormalize, NormalizerImpl.ReorderingBuffer buffer) {
            this.impl.decomposeAndAppend(src, doNormalize, buffer);
        }

        @Override
        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.decompose(s, 0, s.length(), null);
        }

        @Override
        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasDecompBoundaryBefore(c);
        }
    }

    private static final class Norm2AllModesSingleton {
        private Norm2AllModes allModes;
        private RuntimeException exception;

        private Norm2AllModesSingleton(String name) {
            try {
                String DATA_FILE_NAME = "/jdk/internal/icu/impl/data/icudt72b/" + name + ".nrm";
                NormalizerImpl impl = new NormalizerImpl().load(DATA_FILE_NAME);
                this.allModes = new Norm2AllModes(impl);
            }
            catch (RuntimeException e) {
                this.exception = e;
            }
        }
    }

    private static final class NFCSingleton {
        private static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfc");

        private NFCSingleton() {
        }
    }

    private static final class NFKCSingleton {
        private static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfkc");

        private NFKCSingleton() {
        }
    }

    public static final class NoopNormalizer2
    extends Normalizer2 {
        @Override
        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest != src) {
                dest.setLength(0);
                return dest.append(src);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest != src) {
                try {
                    return dest.append(src);
                }
                catch (IOException e) {
                    throw new InternalError(e.toString(), e);
                }
            }
            throw new IllegalArgumentException();
        }

        @Override
        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            if (first != second) {
                return first.append(second);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public StringBuilder append(StringBuilder first, CharSequence second) {
            if (first != second) {
                return first.append(second);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getDecomposition(int c) {
            return null;
        }

        @Override
        public boolean isNormalized(CharSequence s) {
            return true;
        }

        @Override
        public int spanQuickCheckYes(CharSequence s) {
            return s.length();
        }

        @Override
        public boolean hasBoundaryBefore(int c) {
            return true;
        }
    }

    public static abstract class Normalizer2WithImpl
    extends Normalizer2 {
        public final NormalizerImpl impl;

        public Normalizer2WithImpl(NormalizerImpl ni) {
            this.impl = ni;
        }

        @Override
        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest == src) {
                throw new IllegalArgumentException();
            }
            dest.setLength(0);
            this.normalize(src, new NormalizerImpl.ReorderingBuffer(this.impl, dest, src.length()));
            return dest;
        }

        @Override
        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest == src) {
                throw new IllegalArgumentException();
            }
            NormalizerImpl.ReorderingBuffer buffer = new NormalizerImpl.ReorderingBuffer(this.impl, dest, src.length());
            this.normalize(src, buffer);
            buffer.flush();
            return dest;
        }

        protected abstract void normalize(CharSequence var1, NormalizerImpl.ReorderingBuffer var2);

        @Override
        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            return this.normalizeSecondAndAppend(first, second, true);
        }

        @Override
        public StringBuilder append(StringBuilder first, CharSequence second) {
            return this.normalizeSecondAndAppend(first, second, false);
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second, boolean doNormalize) {
            if (first == second) {
                throw new IllegalArgumentException();
            }
            this.normalizeAndAppend(second, doNormalize, new NormalizerImpl.ReorderingBuffer(this.impl, first, first.length() + second.length()));
            return first;
        }

        protected abstract void normalizeAndAppend(CharSequence var1, boolean var2, NormalizerImpl.ReorderingBuffer var3);

        @Override
        public String getDecomposition(int c) {
            return this.impl.getDecomposition(c);
        }

        @Override
        public int getCombiningClass(int c) {
            return this.impl.getCC(this.impl.getNorm16(c));
        }

        @Override
        public boolean isNormalized(CharSequence s) {
            return s.length() == this.spanQuickCheckYes(s);
        }
    }
}

