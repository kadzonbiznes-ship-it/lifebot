/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.util.HashMap;
import java.util.Map;

class VKCollection {
    Map<Integer, String> code2name = new HashMap<Integer, String>();
    Map<String, Integer> name2code = new HashMap<String, Integer>();

    public synchronized void put(String name, Integer code) {
        assert (name != null && code != null);
        assert (this.findName(code) == null);
        assert (this.findCode(name) == null);
        this.code2name.put(code, name);
        this.name2code.put(name, code);
    }

    public synchronized Integer findCode(String name) {
        assert (name != null);
        return this.name2code.get(name);
    }

    public synchronized String findName(Integer code) {
        assert (code != null);
        return this.code2name.get(code);
    }
}

