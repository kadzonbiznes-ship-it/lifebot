/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import jdk.internal.access.SharedSecrets;

public class UndeclaredThrowableException
extends RuntimeException {
    static final long serialVersionUID = 330127114055056639L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("undeclaredThrowable", Throwable.class)};

    public UndeclaredThrowableException(Throwable undeclaredThrowable) {
        super(null, undeclaredThrowable);
    }

    public UndeclaredThrowableException(Throwable undeclaredThrowable, String s) {
        super(s, undeclaredThrowable);
    }

    public Throwable getUndeclaredThrowable() {
        return super.getCause();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        Throwable exception = (Throwable)fields.get("undeclaredThrowable", null);
        if (exception != null) {
            SharedSecrets.getJavaLangAccess().setCause(this, exception);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("undeclaredThrowable", super.getCause());
        out.writeFields();
    }
}

