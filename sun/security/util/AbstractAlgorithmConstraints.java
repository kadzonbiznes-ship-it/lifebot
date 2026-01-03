/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import sun.security.util.AlgorithmDecomposer;

public abstract class AbstractAlgorithmConstraints
implements AlgorithmConstraints {
    protected final AlgorithmDecomposer decomposer;

    protected AbstractAlgorithmConstraints(AlgorithmDecomposer decomposer) {
        this.decomposer = decomposer;
    }

    static Set<String> getAlgorithms(final String propertyName) {
        String property = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return Security.getProperty(propertyName);
            }
        });
        String[] algorithmsInProperty = null;
        if (property != null && !property.isEmpty()) {
            if (property.length() >= 2 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
                property = property.substring(1, property.length() - 1);
            }
            algorithmsInProperty = property.split(",");
            for (int i = 0; i < algorithmsInProperty.length; ++i) {
                algorithmsInProperty[i] = algorithmsInProperty[i].trim();
            }
        }
        if (algorithmsInProperty == null) {
            return Collections.emptySet();
        }
        TreeSet<String> algorithmsInPropertySet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        algorithmsInPropertySet.addAll(Arrays.asList(algorithmsInProperty));
        return algorithmsInPropertySet;
    }

    static boolean checkAlgorithm(Set<String> algorithms, String algorithm, AlgorithmDecomposer decomposer) {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        if (algorithms.contains(algorithm)) {
            return false;
        }
        Set<String> elements = decomposer.decompose(algorithm);
        for (String element : elements) {
            if (!algorithms.contains(element)) continue;
            return false;
        }
        return true;
    }
}

