/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class ParametersMap<T> {
    private Map<Integer, T> sizeMap = new HashMap<Integer, T>();
    private Map<ObjectIdentifier, T> oidMap = new HashMap<ObjectIdentifier, T>();
    private Map<String, T> nameMap = new HashMap<String, T>();

    public void fix() {
        this.sizeMap = Collections.unmodifiableMap(this.sizeMap);
        this.oidMap = Collections.unmodifiableMap(this.oidMap);
        this.nameMap = Collections.unmodifiableMap(this.nameMap);
    }

    public void put(String name, ObjectIdentifier oid, int size, T params) {
        this.nameMap.put(name.toLowerCase(), params);
        this.oidMap.put(oid, params);
        this.sizeMap.put(size, params);
    }

    public Optional<T> getByOid(ObjectIdentifier id) {
        return Optional.ofNullable(this.oidMap.get(id));
    }

    public Optional<T> getBySize(int size) {
        return Optional.ofNullable(this.sizeMap.get(size));
    }

    public Optional<T> getByName(String name) {
        return Optional.ofNullable(this.nameMap.get(name.toLowerCase()));
    }

    private static <A, B> Supplier<B> apply(final Function<A, B> func, final A a) {
        return new Supplier<B>(){

            @Override
            public B get() {
                return func.apply(a);
            }
        };
    }

    public <E extends Throwable> T getBySize(Function<String, E> exception, int size) throws E {
        Optional<T> paramsOpt = this.getBySize(size);
        return paramsOpt.orElseThrow(ParametersMap.apply(exception, "Unsupported size: " + size));
    }

    public <E extends Throwable> T get(Function<String, E> exception, AlgorithmId algId) throws E {
        Optional<T> paramsOpt = this.getByOid(algId.getOID());
        return paramsOpt.orElseThrow(ParametersMap.apply(exception, "Unsupported OID: " + String.valueOf(algId.getOID())));
    }

    public <E extends Throwable> T get(Function<String, E> exception, AlgorithmParameterSpec params) throws E {
        if (params instanceof NamedParameterSpec) {
            NamedParameterSpec namedParams = (NamedParameterSpec)params;
            Optional<T> paramsOpt = this.getByName(namedParams.getName());
            return paramsOpt.orElseThrow(ParametersMap.apply(exception, "Unsupported name: " + namedParams.getName()));
        }
        throw (Throwable)exception.apply("Only NamedParameterSpec is supported.");
    }
}

