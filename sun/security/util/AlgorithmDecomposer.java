/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AlgorithmDecomposer {
    private static final Pattern PATTERN = Pattern.compile("with|and|(?<!padd)in", 2);
    private static final Map<String, String> DECOMPOSED_DIGEST_NAMES = Map.of("SHA-1", "SHA1", "SHA-224", "SHA224", "SHA-256", "SHA256", "SHA-384", "SHA384", "SHA-512", "SHA512", "SHA-512/224", "SHA512/224", "SHA-512/256", "SHA512/256");

    private static Set<String> decomposeImpl(String algorithm) {
        String[] transTokens;
        HashSet<String> elements = new HashSet<String>();
        for (String transToken : transTokens = algorithm.split("/")) {
            String[] tokens;
            if (transToken.isEmpty()) continue;
            for (String token : tokens = PATTERN.split(transToken)) {
                if (token.isEmpty()) continue;
                elements.add(token);
            }
        }
        return elements;
    }

    public Set<String> decompose(String algorithm) {
        if (algorithm == null || algorithm.isEmpty()) {
            return new HashSet<String>();
        }
        Set<String> elements = AlgorithmDecomposer.decomposeImpl(algorithm);
        if (!algorithm.contains("SHA")) {
            return elements;
        }
        for (Map.Entry<String, String> e : DECOMPOSED_DIGEST_NAMES.entrySet()) {
            if (elements.contains(e.getValue()) && !elements.contains(e.getKey())) {
                elements.add(e.getKey());
                continue;
            }
            if (!elements.contains(e.getKey()) || elements.contains(e.getValue())) continue;
            elements.add(e.getValue());
        }
        return elements;
    }

    public static Collection<String> getAliases(String algorithm) {
        String[] aliases = algorithm.equalsIgnoreCase("DH") || algorithm.equalsIgnoreCase("DiffieHellman") ? new String[]{"DH", "DiffieHellman"} : new String[]{algorithm};
        return Arrays.asList(aliases);
    }

    static Set<String> decomposeName(String algorithm) {
        if (algorithm == null || algorithm.isEmpty()) {
            return new HashSet<String>();
        }
        Set<String> elements = AlgorithmDecomposer.decomposeImpl(algorithm);
        if (!algorithm.contains("SHA")) {
            return elements;
        }
        for (Map.Entry<String, String> e : DECOMPOSED_DIGEST_NAMES.entrySet()) {
            if (!elements.contains(e.getKey())) continue;
            elements.add(e.getValue());
            elements.remove(e.getKey());
        }
        return elements;
    }

    static String decomposeDigestName(String algorithm) {
        return DECOMPOSED_DIGEST_NAMES.getOrDefault(algorithm, algorithm);
    }
}

