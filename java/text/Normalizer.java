/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import jdk.internal.icu.text.NormalizerBase;

public final class Normalizer {
    private Normalizer() {
    }

    public static String normalize(CharSequence src, Form form) {
        return NormalizerBase.normalize(src.toString(), form);
    }

    public static boolean isNormalized(CharSequence src, Form form) {
        return NormalizerBase.isNormalized(src.toString(), form);
    }

    public static enum Form {
        NFD,
        NFC,
        NFKD,
        NFKC;

    }
}

