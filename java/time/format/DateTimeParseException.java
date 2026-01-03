/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

import java.time.DateTimeException;

public class DateTimeParseException
extends DateTimeException {
    private static final long serialVersionUID = 4304633501674722597L;
    private final String parsedString;
    private final int errorIndex;

    public DateTimeParseException(String message, CharSequence parsedData, int errorIndex) {
        super(message);
        this.parsedString = parsedData.toString();
        this.errorIndex = errorIndex;
    }

    public DateTimeParseException(String message, CharSequence parsedData, int errorIndex, Throwable cause) {
        super(message, cause);
        this.parsedString = parsedData.toString();
        this.errorIndex = errorIndex;
    }

    public String getParsedString() {
        return this.parsedString;
    }

    public int getErrorIndex() {
        return this.errorIndex;
    }
}

