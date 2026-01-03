/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CanIgnoreReturnValue
 *  com.google.gson.internal.LinkedTreeMap
 */
package com.google.gson;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import java.util.Map;
import java.util.Set;

public final class JsonObject
extends JsonElement {
    private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap(false);

    @Override
    public JsonObject deepCopy() {
        JsonObject result = new JsonObject();
        for (Map.Entry entry : this.members.entrySet()) {
            result.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
        }
        return result;
    }

    public void add(String property, JsonElement value) {
        this.members.put((Object)property, (Object)(value == null ? JsonNull.INSTANCE : value));
    }

    @CanIgnoreReturnValue
    public JsonElement remove(String property) {
        return (JsonElement)this.members.remove((Object)property);
    }

    public void addProperty(String property, String value) {
        this.add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    public void addProperty(String property, Number value) {
        this.add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    public void addProperty(String property, Boolean value) {
        this.add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    public void addProperty(String property, Character value) {
        this.add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return this.members.entrySet();
    }

    public Set<String> keySet() {
        return this.members.keySet();
    }

    public int size() {
        return this.members.size();
    }

    public boolean isEmpty() {
        return this.members.size() == 0;
    }

    public boolean has(String memberName) {
        return this.members.containsKey((Object)memberName);
    }

    public JsonElement get(String memberName) {
        return (JsonElement)this.members.get((Object)memberName);
    }

    public JsonPrimitive getAsJsonPrimitive(String memberName) {
        return (JsonPrimitive)this.members.get((Object)memberName);
    }

    public JsonArray getAsJsonArray(String memberName) {
        return (JsonArray)this.members.get((Object)memberName);
    }

    public JsonObject getAsJsonObject(String memberName) {
        return (JsonObject)this.members.get((Object)memberName);
    }

    public Map<String, JsonElement> asMap() {
        return this.members;
    }

    public boolean equals(Object o) {
        return o == this || o instanceof JsonObject && ((JsonObject)o).members.equals(this.members);
    }

    public int hashCode() {
        return this.members.hashCode();
    }
}

