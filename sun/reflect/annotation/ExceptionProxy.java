/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.annotation;

import java.io.Serializable;

public abstract class ExceptionProxy
implements Serializable {
    private static final long serialVersionUID = 7241930048386631401L;

    protected abstract RuntimeException generateException();
}

