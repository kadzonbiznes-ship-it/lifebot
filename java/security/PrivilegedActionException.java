/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import jdk.internal.access.SharedSecrets;

public class PrivilegedActionException
extends Exception {
    private static final long serialVersionUID = 4724086851538908602L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("exception", Exception.class)};

    public PrivilegedActionException(Exception exception) {
        super(null, exception);
    }

    public Exception getException() {
        return (Exception)super.getCause();
    }

    @Override
    public String toString() {
        String s = this.getClass().getName();
        Throwable cause = super.getCause();
        return cause != null ? s + ": " + cause.toString() : s;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        Exception exception = (Exception)fields.get("exception", null);
        if (exception != null) {
            SharedSecrets.getJavaLangAccess().setCause(this, exception);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("exception", super.getCause());
        out.writeFields();
    }
}

