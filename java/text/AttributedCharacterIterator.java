/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface AttributedCharacterIterator
extends CharacterIterator {
    public int getRunStart();

    public int getRunStart(Attribute var1);

    public int getRunStart(Set<? extends Attribute> var1);

    public int getRunLimit();

    public int getRunLimit(Attribute var1);

    public int getRunLimit(Set<? extends Attribute> var1);

    public Map<Attribute, Object> getAttributes();

    public Object getAttribute(Attribute var1);

    public Set<Attribute> getAllAttributeKeys();

    public static class Attribute
    implements Serializable {
        private String name;
        private static final Map<String, Attribute> instanceMap = new HashMap<String, Attribute>(7);
        public static final Attribute LANGUAGE = new Attribute("language");
        public static final Attribute READING = new Attribute("reading");
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");
        private static final long serialVersionUID = -9142742483513960612L;

        protected Attribute(String name) {
            this.name = name;
            if (this.getClass() == Attribute.class) {
                instanceMap.put(name, this);
            }
        }

        public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        public final int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return this.getClass().getName() + "(" + this.name + ")";
        }

        protected String getName() {
            return this.name;
        }

        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Attribute.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Attribute instance = instanceMap.get(this.getName());
            if (instance != null) {
                return instance;
            }
            throw new InvalidObjectException("unknown attribute name");
        }
    }
}

