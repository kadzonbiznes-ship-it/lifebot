/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  com.google.gson.internal.bind.TreeTypeAdapter$GsonContextImpl
 *  com.google.gson.internal.bind.TreeTypeAdapter$SingleTypeFactory
 */
package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.SerializationDelegatingTypeAdapter;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public final class TreeTypeAdapter<T>
extends SerializationDelegatingTypeAdapter<T> {
    private final JsonSerializer<T> serializer;
    private final JsonDeserializer<T> deserializer;
    final Gson gson;
    private final TypeToken<T> typeToken;
    private final TypeAdapterFactory skipPastForGetDelegateAdapter;
    private final GsonContextImpl context = new GsonContextImpl(this, null);
    private final boolean nullSafe;
    private volatile TypeAdapter<T> delegate;

    public TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer, Gson gson, TypeToken<T> typeToken, TypeAdapterFactory skipPast, boolean nullSafe) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.gson = gson;
        this.typeToken = typeToken;
        this.skipPastForGetDelegateAdapter = skipPast;
        this.nullSafe = nullSafe;
    }

    public TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer, Gson gson, TypeToken<T> typeToken, TypeAdapterFactory skipPast) {
        this(serializer, deserializer, gson, typeToken, skipPast, true);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (this.deserializer == null) {
            return this.delegate().read(in);
        }
        JsonElement value = Streams.parse(in);
        if (this.nullSafe && value.isJsonNull()) {
            return null;
        }
        return (T)this.deserializer.deserialize(value, this.typeToken.getType(), (JsonDeserializationContext)this.context);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (this.serializer == null) {
            this.delegate().write(out, value);
            return;
        }
        if (this.nullSafe && value == null) {
            out.nullValue();
            return;
        }
        JsonElement tree = this.serializer.serialize(value, this.typeToken.getType(), (JsonSerializationContext)this.context);
        Streams.write(tree, out);
    }

    private TypeAdapter<T> delegate() {
        TypeAdapter<T> d = this.delegate;
        return d != null ? d : (this.delegate = this.gson.getDelegateAdapter(this.skipPastForGetDelegateAdapter, this.typeToken));
    }

    @Override
    public TypeAdapter<T> getSerializationDelegate() {
        return this.serializer != null ? this : this.delegate();
    }

    public static TypeAdapterFactory newFactory(TypeToken<?> exactType, Object typeAdapter) {
        return new SingleTypeFactory(typeAdapter, exactType, false, null);
    }

    public static TypeAdapterFactory newFactoryWithMatchRawType(TypeToken<?> exactType, Object typeAdapter) {
        boolean matchRawType = exactType.getType() == exactType.getRawType();
        return new SingleTypeFactory(typeAdapter, exactType, matchRawType, null);
    }

    public static TypeAdapterFactory newTypeHierarchyFactory(Class<?> hierarchyType, Object typeAdapter) {
        return new SingleTypeFactory(typeAdapter, null, false, hierarchyType);
    }
}

