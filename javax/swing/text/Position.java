/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

public interface Position {
    public int getOffset();

    public static final class Bias {
        public static final Bias Forward = new Bias("Forward");
        public static final Bias Backward = new Bias("Backward");
        private String name;

        public String toString() {
            return this.name;
        }

        private Bias(String name) {
            this.name = name;
        }
    }
}

