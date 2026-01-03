/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.lang.ref.SoftReference;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.security.cert.CertPathValidatorException;
import java.security.interfaces.ECKey;
import java.security.interfaces.XECKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.security.ssl.SSLScope;
import sun.security.util.AbstractAlgorithmConstraints;
import sun.security.util.AlgorithmDecomposer;
import sun.security.util.ConstraintsParameters;
import sun.security.util.CurveDB;
import sun.security.util.Debug;
import sun.security.util.KeyUtil;
import sun.security.util.NamedCurve;

public class DisabledAlgorithmConstraints
extends AbstractAlgorithmConstraints {
    private static final Debug debug = Debug.getInstance("certpath");
    public static final String PROPERTY_CERTPATH_DISABLED_ALGS = "jdk.certpath.disabledAlgorithms";
    public static final String PROPERTY_SECURITY_LEGACY_ALGS = "jdk.security.legacyAlgorithms";
    public static final String PROPERTY_TLS_DISABLED_ALGS = "jdk.tls.disabledAlgorithms";
    public static final String PROPERTY_JAR_DISABLED_ALGS = "jdk.jar.disabledAlgorithms";
    private static final String PROPERTY_DISABLED_EC_CURVES = "jdk.disabled.namedCurves";
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("include jdk.disabled.namedCurves", 2);
    private final Set<String> disabledAlgorithms;
    private final List<Pattern> disabledPatterns;
    private final Constraints algorithmConstraints;
    private volatile SoftReference<Map<String, Boolean>> cacheRef = new SoftReference<Object>(null);

    public static DisabledAlgorithmConstraints certPathConstraints() {
        return CertPathHolder.CONSTRAINTS;
    }

    public static DisabledAlgorithmConstraints jarConstraints() {
        return JarHolder.CONSTRAINTS;
    }

    public DisabledAlgorithmConstraints(String propertyName) {
        this(propertyName, new AlgorithmDecomposer());
    }

    public DisabledAlgorithmConstraints(String propertyName, AlgorithmDecomposer decomposer) {
        super(decomposer);
        this.disabledAlgorithms = DisabledAlgorithmConstraints.getAlgorithms(propertyName);
        this.disabledPatterns = PROPERTY_TLS_DISABLED_ALGS.equals(propertyName) ? this.getDisabledPatterns() : null;
        for (String s : this.disabledAlgorithms) {
            Matcher matcher = INCLUDE_PATTERN.matcher(s);
            if (!matcher.matches()) continue;
            this.disabledAlgorithms.remove(matcher.group());
            this.disabledAlgorithms.addAll(DisabledAlgorithmConstraints.getAlgorithms(PROPERTY_DISABLED_EC_CURVES));
            break;
        }
        this.algorithmConstraints = new Constraints(propertyName, this.disabledAlgorithms);
    }

    @Override
    public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters) {
        if (primitives == null || primitives.isEmpty()) {
            throw new IllegalArgumentException("The primitives cannot be null or empty.");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        if (!this.cachedCheckAlgorithm(algorithm)) {
            return false;
        }
        if (parameters != null) {
            return this.algorithmConstraints.permits(algorithm, parameters);
        }
        return true;
    }

    public boolean permits(String algorithm, Set<SSLScope> scopes) {
        List<Constraint> list = this.algorithmConstraints.getConstraints(algorithm);
        return list == null || list.stream().allMatch(c -> c.permits(scopes));
    }

    @Override
    public final boolean permits(Set<CryptoPrimitive> primitives, Key key) {
        return this.checkConstraints(primitives, "", key, null);
    }

    @Override
    public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        return this.checkConstraints(primitives, algorithm, key, parameters);
    }

    public final void permits(String algorithm, AlgorithmParameters ap, ConstraintsParameters cp, boolean checkKey) throws CertPathValidatorException {
        this.permits(algorithm, cp, checkKey);
        if (ap != null) {
            this.permits(ap, cp);
        }
    }

    public void permits(AlgorithmParameters ap, ConstraintsParameters cp) throws CertPathValidatorException {
        switch (ap.getAlgorithm().toUpperCase(Locale.ENGLISH)) {
            case "RSASSA-PSS": {
                this.permitsPSSParams(ap, cp);
                break;
            }
        }
    }

    private void permitsPSSParams(AlgorithmParameters ap, ConstraintsParameters cp) throws CertPathValidatorException {
        try {
            String mgfDigestAlg;
            PSSParameterSpec pssParams = ap.getParameterSpec(PSSParameterSpec.class);
            String digestAlg = pssParams.getDigestAlgorithm();
            this.permits(digestAlg, cp, false);
            AlgorithmParameterSpec mgfParams = pssParams.getMGFParameters();
            if (mgfParams instanceof MGF1ParameterSpec && !(mgfDigestAlg = ((MGF1ParameterSpec)mgfParams).getDigestAlgorithm()).equalsIgnoreCase(digestAlg)) {
                this.permits(mgfDigestAlg, cp, false);
            }
        }
        catch (InvalidParameterSpecException invalidParameterSpecException) {
            // empty catch block
        }
    }

    public final void permits(String algorithm, ConstraintsParameters cp, boolean checkKey) throws CertPathValidatorException {
        if (checkKey) {
            for (Key key : cp.getKeys()) {
                for (String curve : DisabledAlgorithmConstraints.getNamedCurveFromKey(key)) {
                    if (this.cachedCheckAlgorithm(curve)) continue;
                    throw new CertPathValidatorException("Algorithm constraints check failed on disabled algorithm: " + curve, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                }
            }
        }
        this.algorithmConstraints.permits(algorithm, cp, checkKey);
    }

    private static List<String> getNamedCurveFromKey(Key key) {
        if (key instanceof ECKey) {
            NamedCurve nc = CurveDB.lookup(((ECKey)((Object)key)).getParams());
            return nc == null ? List.of() : Arrays.asList(nc.getNameAndAliases());
        }
        if (key instanceof XECKey) {
            return List.of(((NamedParameterSpec)((XECKey)((Object)key)).getParams()).getName());
        }
        return List.of();
    }

    private boolean checkConstraints(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
        if (primitives == null || primitives.isEmpty()) {
            throw new IllegalArgumentException("The primitives cannot be null or empty.");
        }
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null");
        }
        if (algorithm != null && !algorithm.isEmpty() && !this.permits(primitives, algorithm, parameters)) {
            return false;
        }
        if (!this.permits(primitives, key.getAlgorithm(), null)) {
            return false;
        }
        for (String curve : DisabledAlgorithmConstraints.getNamedCurveFromKey(key)) {
            if (this.permits(primitives, curve, null)) continue;
            return false;
        }
        return this.algorithmConstraints.permits(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean cachedCheckAlgorithm(String algorithm) {
        Boolean result;
        Map<String, Boolean> cache = this.cacheRef.get();
        if (cache == null) {
            DisabledAlgorithmConstraints disabledAlgorithmConstraints = this;
            synchronized (disabledAlgorithmConstraints) {
                cache = this.cacheRef.get();
                if (cache == null) {
                    cache = new ConcurrentHashMap<String, Boolean>();
                    this.cacheRef = new SoftReference<Map<String, Boolean>>(cache);
                }
            }
        }
        if ((result = cache.get(algorithm)) != null) {
            return result;
        }
        result = DisabledAlgorithmConstraints.checkAlgorithm(this.disabledAlgorithms, algorithm, this.decomposer) && this.checkDisabledPatterns(algorithm);
        cache.put(algorithm, result);
        return result;
    }

    private boolean checkDisabledPatterns(String algorithm) {
        return this.disabledPatterns == null || this.disabledPatterns.stream().noneMatch(p -> p.matcher(algorithm).matches());
    }

    private List<Pattern> getDisabledPatterns() {
        ArrayList<Pattern> ret = null;
        ArrayList<String> patternStrings = new ArrayList<String>(4);
        for (String p : this.disabledAlgorithms) {
            if (!p.contains("*")) continue;
            if (!p.startsWith("TLS_")) {
                throw new IllegalArgumentException("Wildcard pattern must start with \"TLS_\"");
            }
            patternStrings.add(p);
        }
        if (!patternStrings.isEmpty()) {
            ret = new ArrayList<Pattern>(patternStrings.size());
            for (String p : patternStrings) {
                this.disabledAlgorithms.remove(p);
                ret.add(Pattern.compile("^\\Q" + p.replace("*", "\\E.*\\Q") + "\\E$"));
            }
        }
        return ret;
    }

    private static class CertPathHolder {
        static final DisabledAlgorithmConstraints CONSTRAINTS = new DisabledAlgorithmConstraints("jdk.certpath.disabledAlgorithms");

        private CertPathHolder() {
        }
    }

    private static class JarHolder {
        static final DisabledAlgorithmConstraints CONSTRAINTS = new DisabledAlgorithmConstraints("jdk.jar.disabledAlgorithms");

        private JarHolder() {
        }
    }

    private static class Constraints {
        private final Map<String, List<Constraint>> constraintsMap = new HashMap<String, List<Constraint>>();

        public Constraints(String propertyName, Set<String> constraintSet) {
            for (String constraintEntry : constraintSet) {
                int space;
                if (constraintEntry == null || constraintEntry.isEmpty()) continue;
                constraintEntry = constraintEntry.trim();
                if (debug != null) {
                    debug.println("Constraints: " + constraintEntry);
                }
                String algorithm = AlgorithmDecomposer.decomposeDigestName((space = constraintEntry.indexOf(32)) > 0 ? constraintEntry.substring(0, space) : constraintEntry);
                List constraintList = this.constraintsMap.getOrDefault(algorithm.toUpperCase(Locale.ENGLISH), new ArrayList(1));
                for (String alias : AlgorithmDecomposer.getAliases(algorithm)) {
                    this.constraintsMap.putIfAbsent(alias.toUpperCase(Locale.ENGLISH), constraintList);
                }
                if (space <= 0 || CurveDB.lookup(constraintEntry) != null) {
                    constraintList.add(new DisabledConstraint(algorithm));
                    continue;
                }
                String policy = constraintEntry.substring(space + 1);
                jdkCAConstraint lastConstraint = null;
                boolean jdkCALimit = false;
                boolean denyAfterLimit = false;
                for (String entry : policy.split("&")) {
                    Matcher matcher;
                    Constraint c;
                    if ((entry = entry.trim()).startsWith("keySize")) {
                        StringTokenizer tokens;
                        if (debug != null) {
                            debug.println("Constraints set to keySize: " + entry);
                        }
                        if (!"keySize".equals((tokens = new StringTokenizer(entry)).nextToken())) {
                            throw new IllegalArgumentException("Error in security property. Constraint unknown: " + entry);
                        }
                        c = new KeySizeConstraint(algorithm, Constraint.Operator.of(tokens.nextToken()), Integer.parseInt(tokens.nextToken()));
                    } else if (entry.equalsIgnoreCase("jdkCA")) {
                        if (debug != null) {
                            debug.println("Constraints set to jdkCA.");
                        }
                        if (jdkCALimit) {
                            throw new IllegalArgumentException("Only one jdkCA entry allowed in property. Constraint: " + constraintEntry);
                        }
                        c = new jdkCAConstraint(algorithm);
                        jdkCALimit = true;
                    } else if (entry.startsWith("denyAfter") && (matcher = Holder.DENY_AFTER_PATTERN.matcher(entry)).matches()) {
                        if (debug != null) {
                            debug.println("Constraints set to denyAfter");
                        }
                        if (denyAfterLimit) {
                            throw new IllegalArgumentException("Only one denyAfter entry allowed in property. Constraint: " + constraintEntry);
                        }
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2));
                        int day = Integer.parseInt(matcher.group(3));
                        c = new DenyAfterConstraint(algorithm, year, month, day);
                        denyAfterLimit = true;
                    } else if (entry.startsWith("usage")) {
                        String[] s = entry.substring(5).trim().split(" ");
                        c = new UsageConstraint(algorithm, s, propertyName);
                        if (debug != null) {
                            debug.println("Constraints usage length is " + s.length);
                        }
                    } else {
                        throw new IllegalArgumentException("Error in security property. Constraint unknown: " + entry);
                    }
                    if (lastConstraint == null) {
                        constraintList.add(c);
                    } else {
                        lastConstraint.nextConstraint = c;
                    }
                    lastConstraint = c;
                }
            }
        }

        private List<Constraint> getConstraints(String algorithm) {
            return this.constraintsMap.get(algorithm.toUpperCase(Locale.ENGLISH));
        }

        public boolean permits(Key key) {
            List<Constraint> list = this.getConstraints(key.getAlgorithm());
            if (list == null) {
                return true;
            }
            for (Constraint constraint : list) {
                if (constraint.permits(key)) continue;
                if (debug != null) {
                    debug.println("Constraints: failed key size constraint check " + KeyUtil.getKeySize(key));
                }
                return false;
            }
            return true;
        }

        public boolean permits(String algorithm, AlgorithmParameters aps) {
            List<Constraint> list = this.getConstraints(algorithm);
            if (list == null) {
                return true;
            }
            for (Constraint constraint : list) {
                if (constraint.permits(aps)) continue;
                if (debug != null) {
                    debug.println("Constraints: failed algorithm parameters constraint check " + aps);
                }
                return false;
            }
            return true;
        }

        public void permits(String algorithm, ConstraintsParameters cp, boolean checkKey) throws CertPathValidatorException {
            if (debug != null) {
                debug.println("Constraints.permits(): " + algorithm + ", " + cp.toString());
            }
            HashSet<String> algorithms = new HashSet<String>();
            if (algorithm != null) {
                algorithms.addAll(AlgorithmDecomposer.decomposeName(algorithm));
                algorithms.add(algorithm);
            }
            if (checkKey) {
                for (Key key : cp.getKeys()) {
                    algorithms.add(key.getAlgorithm());
                }
            }
            for (String alg : algorithms) {
                List<Constraint> list = this.getConstraints(alg);
                if (list == null) continue;
                for (Constraint constraint : list) {
                    if (!checkKey && constraint instanceof KeySizeConstraint) continue;
                    constraint.permits(cp);
                }
            }
        }

        private static class Holder {
            private static final Pattern DENY_AFTER_PATTERN = Pattern.compile("denyAfter\\s+(\\d{4})-(\\d{2})-(\\d{2})");

            private Holder() {
            }
        }
    }

    private static abstract class Constraint {
        String algorithm;
        Constraint nextConstraint = null;

        private Constraint() {
        }

        public boolean permits(Key key) {
            return true;
        }

        public boolean permits(AlgorithmParameters parameters) {
            return true;
        }

        public boolean permits(Set<SSLScope> scopes) {
            return true;
        }

        public abstract void permits(ConstraintsParameters var1) throws CertPathValidatorException;

        boolean next(ConstraintsParameters cp) throws CertPathValidatorException {
            if (this.nextConstraint != null) {
                this.nextConstraint.permits(cp);
                return true;
            }
            return false;
        }

        boolean next(Key key) {
            return this.nextConstraint != null && this.nextConstraint.permits(key);
        }

        static enum Operator {
            EQ,
            NE,
            LT,
            LE,
            GT,
            GE;


            static Operator of(String s) {
                switch (s) {
                    case "==": {
                        return EQ;
                    }
                    case "!=": {
                        return NE;
                    }
                    case "<": {
                        return LT;
                    }
                    case "<=": {
                        return LE;
                    }
                    case ">": {
                        return GT;
                    }
                    case ">=": {
                        return GE;
                    }
                }
                throw new IllegalArgumentException("Error in security property. " + s + " is not a legal Operator");
            }
        }
    }

    private static class DisabledConstraint
    extends Constraint {
        DisabledConstraint(String algo) {
            this.algorithm = algo;
        }

        @Override
        public void permits(ConstraintsParameters cp) throws CertPathValidatorException {
            throw new CertPathValidatorException("Algorithm constraints check failed on disabled algorithm: " + this.algorithm + cp.extendedExceptionMsg(), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
        }

        @Override
        public boolean permits(Key key) {
            return false;
        }
    }

    private static class KeySizeConstraint
    extends Constraint {
        private final int minSize;
        private final int maxSize;
        private int prohibitedSize = -1;

        public KeySizeConstraint(String algo, Constraint.Operator operator, int length) {
            this.algorithm = algo;
            switch (operator.ordinal()) {
                case 0: {
                    this.minSize = 0;
                    this.maxSize = Integer.MAX_VALUE;
                    this.prohibitedSize = length;
                    break;
                }
                case 1: {
                    this.minSize = length;
                    this.maxSize = length;
                    break;
                }
                case 2: {
                    this.minSize = length;
                    this.maxSize = Integer.MAX_VALUE;
                    break;
                }
                case 3: {
                    this.minSize = length + 1;
                    this.maxSize = Integer.MAX_VALUE;
                    break;
                }
                case 4: {
                    this.minSize = 0;
                    this.maxSize = length;
                    break;
                }
                case 5: {
                    this.minSize = 0;
                    this.maxSize = length > 1 ? length - 1 : 0;
                    break;
                }
                default: {
                    this.minSize = Integer.MAX_VALUE;
                    this.maxSize = -1;
                }
            }
        }

        @Override
        public void permits(ConstraintsParameters cp) throws CertPathValidatorException {
            for (Key key : cp.getKeys()) {
                if (this.permitsImpl(key)) continue;
                if (this.nextConstraint != null) {
                    this.nextConstraint.permits(cp);
                    continue;
                }
                throw new CertPathValidatorException("Algorithm constraints check failed on keysize limits: " + this.algorithm + " " + KeyUtil.getKeySize(key) + " bit key" + cp.extendedExceptionMsg(), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
        }

        @Override
        public boolean permits(Key key) {
            if (this.nextConstraint != null && this.nextConstraint.permits(key)) {
                return true;
            }
            if (debug != null) {
                debug.println("KeySizeConstraints.permits(): " + this.algorithm);
            }
            return this.permitsImpl(key);
        }

        @Override
        public boolean permits(AlgorithmParameters parameters) {
            Collection<String> aliases;
            String paramAlg = parameters.getAlgorithm();
            if (!this.algorithm.equalsIgnoreCase(parameters.getAlgorithm()) && !(aliases = AlgorithmDecomposer.getAliases(this.algorithm)).contains(paramAlg)) {
                return true;
            }
            int keySize = KeyUtil.getKeySize(parameters);
            if (keySize == 0) {
                return false;
            }
            if (keySize > 0) {
                return keySize >= this.minSize && keySize <= this.maxSize && this.prohibitedSize != keySize;
            }
            return true;
        }

        private boolean permitsImpl(Key key) {
            if (this.algorithm.compareToIgnoreCase(key.getAlgorithm()) != 0) {
                return true;
            }
            int size = KeyUtil.getKeySize(key);
            if (size == 0) {
                return false;
            }
            if (size > 0) {
                return size >= this.minSize && size <= this.maxSize && this.prohibitedSize != size;
            }
            return true;
        }
    }

    private static class UsageConstraint
    extends Constraint {
        String[] usages;
        Set<SSLScope> scopes;

        UsageConstraint(String algorithm, String[] usages, String propertyName) {
            this.algorithm = algorithm;
            if (DisabledAlgorithmConstraints.PROPERTY_TLS_DISABLED_ALGS.equals(propertyName)) {
                for (String usage : usages) {
                    SSLScope scope = SSLScope.nameOf(usage);
                    if (scope != null) {
                        if (this.scopes == null) {
                            this.scopes = new HashSet<SSLScope>(usages.length);
                        }
                        this.scopes.add(scope);
                        continue;
                    }
                    this.usages = usages;
                }
                if (this.scopes != null && this.usages != null) {
                    throw new IllegalArgumentException("Can't mix TLS protocol specific constraints with other usage constraints");
                }
            } else {
                this.usages = usages;
            }
        }

        @Override
        public boolean permits(Set<SSLScope> scopes) {
            if (this.scopes == null || scopes == null) {
                return true;
            }
            return Collections.disjoint(this.scopes, scopes);
        }

        @Override
        public void permits(ConstraintsParameters cp) throws CertPathValidatorException {
            String variant = cp.getVariant();
            for (String usage : this.usages) {
                boolean match = false;
                switch (usage.toLowerCase(Locale.ENGLISH)) {
                    case "tlsserver": {
                        match = variant.equals("tls server");
                        break;
                    }
                    case "tlsclient": {
                        match = variant.equals("tls client");
                        break;
                    }
                    case "signedjar": {
                        boolean bl = match = variant.equals("code signing") || variant.equals("tsa server");
                    }
                }
                if (debug != null) {
                    debug.println("Checking if usage constraint \"" + usage + "\" matches \"" + cp.getVariant() + "\"");
                    if (Debug.isVerbose()) {
                        new Exception().printStackTrace(debug.getPrintStream());
                    }
                }
                if (!match) continue;
                if (this.next(cp)) {
                    return;
                }
                throw new CertPathValidatorException("Usage constraint " + usage + " check failed: " + this.algorithm + cp.extendedExceptionMsg(), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
        }
    }

    private static class DenyAfterConstraint
    extends Constraint {
        private final ZonedDateTime zdt;
        private final Instant denyAfterDate;

        DenyAfterConstraint(String algo, int year, int month, int day) {
            this.algorithm = algo;
            if (debug != null) {
                debug.println("DenyAfterConstraint read in as: year " + year + ", month = " + month + ", day = " + day);
            }
            try {
                this.zdt = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.of("GMT"));
                this.denyAfterDate = this.zdt.toInstant();
            }
            catch (DateTimeException dte) {
                throw new IllegalArgumentException("Invalid denyAfter date", dte);
            }
            if (debug != null) {
                debug.println("DenyAfterConstraint date set to: " + this.zdt.toLocalDate());
            }
        }

        @Override
        public void permits(ConstraintsParameters cp) throws CertPathValidatorException {
            Instant currentDate = cp.getDate() != null ? cp.getDate().toInstant() : Instant.now();
            if (!this.denyAfterDate.isAfter(currentDate)) {
                if (this.next(cp)) {
                    return;
                }
                throw new CertPathValidatorException("denyAfter constraint check failed: " + this.algorithm + " used with Constraint date: " + this.zdt.toLocalDate() + "; params date: " + currentDate + cp.extendedExceptionMsg(), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
        }

        @Override
        public boolean permits(Key key) {
            if (this.next(key)) {
                return true;
            }
            if (debug != null) {
                debug.println("DenyAfterConstraints.permits(): " + this.algorithm);
            }
            return this.denyAfterDate.isAfter(Instant.now());
        }
    }

    private static class jdkCAConstraint
    extends Constraint {
        jdkCAConstraint(String algo) {
            this.algorithm = algo;
        }

        @Override
        public void permits(ConstraintsParameters cp) throws CertPathValidatorException {
            if (debug != null) {
                debug.println("jdkCAConstraints.permits(): " + this.algorithm);
            }
            if (cp.anchorIsJdkCA()) {
                if (this.next(cp)) {
                    return;
                }
                throw new CertPathValidatorException("Algorithm constraints check failed on certificate anchor limits. " + this.algorithm + cp.extendedExceptionMsg(), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
        }
    }
}

