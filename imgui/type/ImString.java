/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ImString
implements Cloneable,
Comparable<ImString> {
    public static final short DEFAULT_LENGTH = 100;
    public static final short CARET_LEN = 1;
    public final InputData inputData = new InputData();
    private byte[] data;
    private String text = "";

    public ImString() {
        this(100);
    }

    public ImString(ImString imString) {
        this(imString.text, imString.data.length);
        this.inputData.allowedChars = imString.inputData.allowedChars;
        this.inputData.isResizable = imString.inputData.isResizable;
        this.inputData.resizeFactor = imString.inputData.resizeFactor;
        this.inputData.size = imString.inputData.size;
        this.inputData.isDirty = imString.inputData.isDirty;
        this.inputData.isResized = imString.inputData.isResized;
    }

    public ImString(int length) {
        this.data = new byte[length + 1];
    }

    public ImString(String text) {
        this.set(text, true, 0);
    }

    public ImString(String text, int length) {
        this(length);
        this.set(text);
    }

    public String get() {
        if (this.inputData.isDirty) {
            this.inputData.isDirty = false;
            this.text = new String(this.data, 0, this.inputData.size, StandardCharsets.UTF_8);
        }
        return this.text;
    }

    public byte[] getData() {
        return this.data;
    }

    public void set(Object object) {
        this.set(String.valueOf(object));
    }

    public void set(ImString value) {
        this.set(value.get(), true);
    }

    public void set(ImString value, boolean resize) {
        this.set(value.get(), resize);
    }

    public void set(String value) {
        this.set(value, this.inputData.isResizable, this.inputData.resizeFactor);
    }

    public void set(String value, boolean resize) {
        this.set(value, resize, this.inputData.resizeFactor);
    }

    public void set(String value, boolean resize, int resizeValue) {
        byte[] valueBuff = (value == null ? "null" : value).getBytes(StandardCharsets.UTF_8);
        int currentLen = this.data == null ? 0 : this.data.length;
        byte[] newBuff = null;
        if (resize && currentLen - 1 < valueBuff.length) {
            newBuff = new byte[valueBuff.length + resizeValue + 1];
            this.inputData.size = valueBuff.length;
        }
        if (newBuff == null) {
            newBuff = new byte[currentLen];
            this.inputData.size = Math.max(0, Math.min(valueBuff.length, currentLen - 1));
        }
        System.arraycopy(valueBuff, 0, newBuff, 0, Math.min(valueBuff.length, newBuff.length - 1));
        this.data = newBuff;
        this.inputData.isDirty = true;
    }

    public void resize(int newSize) {
        if (newSize < this.data.length) {
            throw new IllegalArgumentException("New size should be greater than current size of the buffer");
        }
        int size = newSize + 1;
        byte[] newBuffer = new byte[size];
        System.arraycopy(this.data, 0, newBuffer, 0, this.data.length);
        this.data = newBuffer;
    }

    byte[] resizeInternal(int newSize) {
        this.resize(newSize + this.inputData.resizeFactor);
        return this.data;
    }

    public int getLength() {
        return this.get().length();
    }

    public int getBufferSize() {
        return this.data.length;
    }

    public boolean isEmpty() {
        return this.getLength() == 0;
    }

    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public void clear() {
        this.set("");
    }

    public String toString() {
        return this.get();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImString imString = (ImString)o;
        return Objects.equals(this.text, imString.text);
    }

    public int hashCode() {
        return this.text.hashCode();
    }

    public ImString clone() {
        return new ImString(this);
    }

    @Override
    public int compareTo(ImString o) {
        return this.get().compareTo(o.get());
    }

    public static final class InputData {
        private static final short DEFAULT_RESIZE_FACTOR = 10;
        public String allowedChars = "";
        public boolean isResizable;
        public int resizeFactor = 10;
        int size;
        boolean isDirty;
        boolean isResized = false;

        private InputData() {
        }
    }
}

