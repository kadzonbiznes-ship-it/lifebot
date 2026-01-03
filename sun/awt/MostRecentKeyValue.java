/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

final class MostRecentKeyValue {
    Object key;
    Object value;

    MostRecentKeyValue(Object k, Object v) {
        this.key = k;
        this.value = v;
    }

    void setPair(Object k, Object v) {
        this.key = k;
        this.value = v;
    }
}

