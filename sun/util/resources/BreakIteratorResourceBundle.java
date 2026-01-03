/*
 * Decompiled with CFR 0.152.
 */
package sun.util.resources;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Set;

public abstract class BreakIteratorResourceBundle
extends ResourceBundle {
    private static final Set<String> NON_DATA_KEYS = Set.of("BreakIteratorClasses");
    private volatile Set<String> keys;

    protected abstract ResourceBundle getBreakIteratorInfo();

    @Override
    protected Object handleGetObject(String key) {
        byte[] data;
        if (NON_DATA_KEYS.contains(key)) {
            return null;
        }
        ResourceBundle info = this.getBreakIteratorInfo();
        if (!info.containsKey(key)) {
            return null;
        }
        String path = this.getClass().getPackageName().replace('.', '/') + '/' + info.getString(key);
        try (InputStream is = this.getResourceAsStream(path);){
            data = is.readAllBytes();
        }
        catch (Exception e) {
            throw new InternalError("Can't load " + path, e);
        }
        return data;
    }

    private InputStream getResourceAsStream(String path) throws Exception {
        InputStream is;
        PrivilegedExceptionAction<InputStream> pa = () -> this.getClass().getModule().getResourceAsStream(path);
        try {
            is = AccessController.doPrivileged(pa);
        }
        catch (PrivilegedActionException e) {
            throw e.getException();
        }
        return is;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.keySet());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Set<String> handleKeySet() {
        if (this.keys == null) {
            ResourceBundle info = this.getBreakIteratorInfo();
            Set<String> k = info.keySet();
            k.removeAll(NON_DATA_KEYS);
            BreakIteratorResourceBundle breakIteratorResourceBundle = this;
            synchronized (breakIteratorResourceBundle) {
                if (this.keys == null) {
                    this.keys = k;
                }
            }
        }
        return this.keys;
    }
}

