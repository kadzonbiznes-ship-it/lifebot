/*
 * Decompiled with CFR 0.152.
 */
package sun.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class ResourceBundleEnumeration
implements Enumeration<String> {
    Set<String> set;
    Iterator<String> iterator;
    Enumeration<String> enumeration;
    String next = null;

    public ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
        this.set = set;
        this.iterator = set.iterator();
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasMoreElements() {
        if (this.next == null) {
            if (this.iterator.hasNext()) {
                this.next = this.iterator.next();
            } else if (this.enumeration != null) {
                while (this.next == null && this.enumeration.hasMoreElements()) {
                    this.next = this.enumeration.nextElement();
                    if (!this.set.contains(this.next)) continue;
                    this.next = null;
                }
            }
        }
        return this.next != null;
    }

    @Override
    public String nextElement() {
        if (this.hasMoreElements()) {
            String result = this.next;
            this.next = null;
            return result;
        }
        throw new NoSuchElementException();
    }
}

