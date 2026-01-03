/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

public abstract class Format
implements Serializable,
Cloneable {
    private static final long serialVersionUID = -299282585814624189L;

    protected Format() {
    }

    public final String format(Object obj) {
        return this.format(obj, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public abstract StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3);

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return this.createAttributedCharacterIterator(this.format(obj));
    }

    public abstract Object parseObject(String var1, ParsePosition var2);

    public Object parseObject(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object result = this.parseObject(source, pos);
        if (pos.index == 0) {
            throw new ParseException("Format.parseObject(String) failed", pos.errorIndex);
        }
        return result;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    AttributedCharacterIterator createAttributedCharacterIterator(String s) {
        AttributedString as = new AttributedString(s);
        return as.getIterator();
    }

    AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator[] iterators) {
        AttributedString as = new AttributedString(iterators);
        return as.getIterator();
    }

    AttributedCharacterIterator createAttributedCharacterIterator(String string, AttributedCharacterIterator.Attribute key, Object value) {
        AttributedString as = new AttributedString(string);
        as.addAttribute(key, value);
        return as.getIterator();
    }

    AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator iterator, AttributedCharacterIterator.Attribute key, Object value) {
        AttributedString as = new AttributedString(iterator);
        as.addAttribute(key, value);
        return as.getIterator();
    }

    static interface FieldDelegate {
        public void formatted(Field var1, Object var2, int var3, int var4, StringBuffer var5);

        public void formatted(int var1, Field var2, Object var3, int var4, int var5, StringBuffer var6);
    }

    public static class Field
    extends AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = 276966692217360283L;

        protected Field(String name) {
            super(name);
        }
    }
}

