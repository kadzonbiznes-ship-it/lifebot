/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import sun.nio.cs.ISO_8859_1;
import sun.security.action.GetPropertyAction;
import sun.util.PropertyResourceBundleCharset;
import sun.util.ResourceBundleEnumeration;

public class PropertyResourceBundle
extends ResourceBundle {
    private static final String encoding = GetPropertyAction.privilegedGetProperty("java.util.PropertyResourceBundle.encoding", "").toUpperCase(Locale.ROOT);
    private final Map<String, Object> lookup;

    public PropertyResourceBundle(InputStream stream) throws IOException {
        this(new InputStreamReader(stream, "ISO-8859-1".equals(encoding) ? ISO_8859_1.INSTANCE.newDecoder() : new PropertyResourceBundleCharset("UTF-8".equals(encoding)).newDecoder()));
    }

    public PropertyResourceBundle(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        this.lookup = new HashMap<Object, Object>(properties);
    }

    @Override
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return this.lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(this.lookup.keySet(), parent != null ? parent.getKeys() : null);
    }

    @Override
    protected Set<String> handleKeySet() {
        return this.lookup.keySet();
    }
}

