/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream;

public interface Location {
    public int getLineNumber();

    public int getColumnNumber();

    public int getCharacterOffset();

    public String getPublicId();

    public String getSystemId();
}

