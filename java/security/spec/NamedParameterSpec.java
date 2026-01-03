/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

public class NamedParameterSpec
implements AlgorithmParameterSpec {
    public static final NamedParameterSpec X25519 = new NamedParameterSpec("X25519");
    public static final NamedParameterSpec X448 = new NamedParameterSpec("X448");
    public static final NamedParameterSpec ED25519 = new NamedParameterSpec("Ed25519");
    public static final NamedParameterSpec ED448 = new NamedParameterSpec("Ed448");
    private final String name;

    public NamedParameterSpec(String stdName) {
        Objects.requireNonNull(stdName, "stdName must not be null");
        this.name = stdName;
    }

    public String getName() {
        return this.name;
    }
}

