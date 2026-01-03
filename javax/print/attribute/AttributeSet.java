/*
 * Decompiled with CFR 0.152.
 */
package javax.print.attribute;

import javax.print.attribute.Attribute;

public interface AttributeSet {
    public Attribute get(Class<?> var1);

    public boolean add(Attribute var1);

    public boolean remove(Class<?> var1);

    public boolean remove(Attribute var1);

    public boolean containsKey(Class<?> var1);

    public boolean containsValue(Attribute var1);

    public boolean addAll(AttributeSet var1);

    public int size();

    public Attribute[] toArray();

    public void clear();

    public boolean isEmpty();

    public boolean equals(Object var1);

    public int hashCode();
}

