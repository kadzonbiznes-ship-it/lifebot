/*
 * Decompiled with CFR 0.152.
 */
package java.util.regex;

import java.util.Map;
import java.util.Objects;

public interface MatchResult {
    public int start();

    public int start(int var1);

    default public int start(String name) {
        return this.start(this.groupNumber(name));
    }

    public int end();

    public int end(int var1);

    default public int end(String name) {
        return this.end(this.groupNumber(name));
    }

    public String group();

    public String group(int var1);

    default public String group(String name) {
        return this.group(this.groupNumber(name));
    }

    public int groupCount();

    default public Map<String, Integer> namedGroups() {
        throw new UnsupportedOperationException("namedGroups()");
    }

    private int groupNumber(String name) {
        Objects.requireNonNull(name, "Group name");
        Integer number = this.namedGroups().get(name);
        if (number != null) {
            return number;
        }
        throw new IllegalArgumentException("No group with name <" + name + ">");
    }

    default public boolean hasMatch() {
        throw new UnsupportedOperationException("hasMatch()");
    }
}

