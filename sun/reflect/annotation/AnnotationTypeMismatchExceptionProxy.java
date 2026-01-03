/*
 * Decompiled with CFR 0.152.
 */
package sun.reflect.annotation;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;
import sun.reflect.annotation.ExceptionProxy;

class AnnotationTypeMismatchExceptionProxy
extends ExceptionProxy {
    private static final long serialVersionUID = 7844069490309503934L;
    private Method member;
    private final String foundType;

    AnnotationTypeMismatchExceptionProxy(String foundType) {
        this.foundType = foundType;
    }

    AnnotationTypeMismatchExceptionProxy setMember(Method member) {
        this.member = member;
        return this;
    }

    @Override
    protected RuntimeException generateException() {
        return new AnnotationTypeMismatchException(this.member, this.foundType);
    }

    public String toString() {
        return "/* Warning type mismatch! \"" + this.foundType + "\" */";
    }
}

