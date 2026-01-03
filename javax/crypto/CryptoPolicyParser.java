/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.crypto.CryptoAllPermission;
import javax.crypto.CryptoPermission;

final class CryptoPolicyParser {
    private final Vector<GrantEntry> grantEntries = new Vector();
    private StreamTokenizer st;
    private int lookahead;
    private boolean allPermEntryFound = false;

    CryptoPolicyParser() {
    }

    void read(Reader policy) throws ParsingException, IOException {
        if (!(policy instanceof BufferedReader)) {
            policy = new BufferedReader(policy);
        }
        this.st = new StreamTokenizer(policy);
        this.st.resetSyntax();
        this.st.wordChars(97, 122);
        this.st.wordChars(65, 90);
        this.st.wordChars(46, 46);
        this.st.wordChars(48, 57);
        this.st.wordChars(95, 95);
        this.st.wordChars(36, 36);
        this.st.wordChars(160, 255);
        this.st.whitespaceChars(0, 32);
        this.st.commentChar(47);
        this.st.quoteChar(39);
        this.st.quoteChar(34);
        this.st.lowerCaseMode(false);
        this.st.ordinaryChar(47);
        this.st.slashSlashComments(true);
        this.st.slashStarComments(true);
        this.st.parseNumbers();
        Hashtable<String, Vector<String>> processedPermissions = new Hashtable<String, Vector<String>>();
        this.lookahead = this.st.nextToken();
        while (this.lookahead != -1) {
            if (!this.peek("grant")) {
                throw new ParsingException(this.st.lineno(), "expected grant statement");
            }
            GrantEntry ge = this.parseGrantEntry(processedPermissions);
            this.grantEntries.addElement(ge);
            this.match(";");
        }
    }

    private GrantEntry parseGrantEntry(Hashtable<String, Vector<String>> processedPermissions) throws ParsingException, IOException {
        GrantEntry e = new GrantEntry();
        this.match("grant");
        this.match("{");
        while (!this.peek("}")) {
            if (this.peek("Permission")) {
                CryptoPermissionEntry pe = this.parsePermissionEntry(processedPermissions);
                e.add(pe);
                this.match(";");
                continue;
            }
            throw new ParsingException(this.st.lineno(), "expected permission entry");
        }
        this.match("}");
        return e;
    }

    private CryptoPermissionEntry parsePermissionEntry(Hashtable<String, Vector<String>> processedPermissions) throws ParsingException, IOException {
        CryptoPermissionEntry e = new CryptoPermissionEntry();
        this.match("Permission");
        e.cryptoPermission = this.match("permission type");
        if (e.cryptoPermission.equals("javax.crypto.CryptoAllPermission")) {
            if (!processedPermissions.isEmpty()) {
                throw new ParsingException(this.st.lineno(), "Inconsistent policy");
            }
            this.allPermEntryFound = true;
            e.alg = "CryptoAllPermission";
            e.maxKeySize = Integer.MAX_VALUE;
            return e;
        }
        if (this.peek("\"")) {
            e.alg = this.match("quoted string").toUpperCase(Locale.ENGLISH);
        } else if (this.peek("*")) {
            this.match("*");
            e.alg = "*";
        } else {
            throw new ParsingException(this.st.lineno(), "Missing the algorithm name");
        }
        this.peekAndMatch(",");
        if (this.peek("\"")) {
            e.exemptionMechanism = this.match("quoted string").toUpperCase(Locale.ENGLISH);
        }
        this.peekAndMatch(",");
        if (!this.isConsistent(e.alg, e.exemptionMechanism, processedPermissions)) {
            throw new ParsingException(this.st.lineno(), "Inconsistent policy");
        }
        if (this.peek("number")) {
            e.maxKeySize = this.match();
        } else if (this.peek("*")) {
            this.match("*");
            e.maxKeySize = Integer.MAX_VALUE;
        } else {
            if (!this.peek(";")) {
                throw new ParsingException(this.st.lineno(), "Missing the maximum allowable key size");
            }
            e.maxKeySize = Integer.MAX_VALUE;
        }
        this.peekAndMatch(",");
        if (this.peek("\"")) {
            String algParamSpecClassName = this.match("quoted string");
            ArrayList<Integer> paramsV = new ArrayList<Integer>(1);
            while (this.peek(",")) {
                this.match(",");
                if (this.peek("number")) {
                    paramsV.add(this.match());
                    continue;
                }
                if (this.peek("*")) {
                    this.match("*");
                    paramsV.add(Integer.MAX_VALUE);
                    continue;
                }
                throw new ParsingException(this.st.lineno(), "Expecting an integer");
            }
            Integer[] params = paramsV.toArray(new Integer[0]);
            e.checkParam = true;
            e.algParamSpec = CryptoPolicyParser.getInstance(algParamSpecClassName, params);
        }
        return e;
    }

    private static AlgorithmParameterSpec getInstance(String type, Integer[] params) throws ParsingException {
        AlgorithmParameterSpec ret;
        try {
            Class<?> apsClass = Class.forName(type);
            Class[] paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; ++i) {
                paramClasses[i] = Integer.TYPE;
            }
            Constructor<?> c = apsClass.getConstructor(paramClasses);
            ret = (AlgorithmParameterSpec)c.newInstance(params);
        }
        catch (Exception e) {
            throw new ParsingException("Cannot call the constructor of " + type + e);
        }
        return ret;
    }

    private boolean peekAndMatch(String expect) throws ParsingException, IOException {
        if (this.peek(expect)) {
            this.match(expect);
            return true;
        }
        return false;
    }

    private boolean peek(String expect) {
        boolean found = false;
        switch (this.lookahead) {
            case -3: {
                if (!expect.equalsIgnoreCase(this.st.sval)) break;
                found = true;
                break;
            }
            case -2: {
                if (!expect.equalsIgnoreCase("number")) break;
                found = true;
                break;
            }
            case 44: {
                if (!expect.equals(",")) break;
                found = true;
                break;
            }
            case 123: {
                if (!expect.equals("{")) break;
                found = true;
                break;
            }
            case 125: {
                if (!expect.equals("}")) break;
                found = true;
                break;
            }
            case 34: {
                if (!expect.equals("\"")) break;
                found = true;
                break;
            }
            case 42: {
                if (!expect.equals("*")) break;
                found = true;
                break;
            }
            case 59: {
                if (!expect.equals(";")) break;
                found = true;
                break;
            }
        }
        return found;
    }

    private int match() throws ParsingException, IOException {
        int value = -1;
        int lineno = this.st.lineno();
        String sValue = null;
        switch (this.lookahead) {
            case -2: {
                value = (int)this.st.nval;
                if (value < 0) {
                    sValue = String.valueOf(this.st.nval);
                }
                this.lookahead = this.st.nextToken();
                break;
            }
            default: {
                sValue = this.st.sval;
            }
        }
        if (value <= 0) {
            throw new ParsingException(lineno, "a non-negative number", sValue);
        }
        return value;
    }

    private String match(String expect) throws ParsingException, IOException {
        String value = null;
        switch (this.lookahead) {
            case -2: {
                throw new ParsingException(this.st.lineno(), expect, "number " + this.st.nval);
            }
            case -1: {
                throw new ParsingException("expected " + expect + ", read end of file");
            }
            case -3: {
                if (expect.equalsIgnoreCase(this.st.sval)) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                if (expect.equalsIgnoreCase("permission type")) {
                    value = this.st.sval;
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, this.st.sval);
            }
            case 34: {
                if (expect.equalsIgnoreCase("quoted string")) {
                    value = this.st.sval;
                    this.lookahead = this.st.nextToken();
                    break;
                }
                if (expect.equalsIgnoreCase("permission type")) {
                    value = this.st.sval;
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, this.st.sval);
            }
            case 44: {
                if (expect.equals(",")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, ",");
            }
            case 123: {
                if (expect.equals("{")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "{");
            }
            case 125: {
                if (expect.equals("}")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "}");
            }
            case 59: {
                if (expect.equals(";")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, ";");
            }
            case 42: {
                if (expect.equals("*")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "*");
            }
            default: {
                throw new ParsingException(this.st.lineno(), expect, String.valueOf((char)this.lookahead));
            }
        }
        return value;
    }

    CryptoPermission[] getPermissions() {
        ArrayList<CryptoPermission> result = new ArrayList<CryptoPermission>();
        for (GrantEntry ge : this.grantEntries) {
            for (CryptoPermissionEntry pe : ge.permissionEntries) {
                if (pe.cryptoPermission.equals("javax.crypto.CryptoAllPermission")) {
                    result.add(CryptoAllPermission.INSTANCE);
                    continue;
                }
                if (pe.checkParam) {
                    result.add(new CryptoPermission(pe.alg, pe.maxKeySize, pe.algParamSpec, pe.exemptionMechanism));
                    continue;
                }
                result.add(new CryptoPermission(pe.alg, pe.maxKeySize, pe.exemptionMechanism));
            }
        }
        return result.toArray(new CryptoPermission[0]);
    }

    private boolean isConsistent(String alg, String exemptionMechanism, Hashtable<String, Vector<String>> processedPermissions) {
        Vector<String> exemptionMechanisms;
        String thisExemptionMechanism;
        String string = thisExemptionMechanism = exemptionMechanism == null ? "none" : exemptionMechanism;
        if (this.allPermEntryFound) {
            return false;
        }
        if (processedPermissions.isEmpty()) {
            Vector<String> exemptionMechanisms2 = new Vector<String>(1);
            exemptionMechanisms2.addElement(thisExemptionMechanism);
            processedPermissions.put(alg, exemptionMechanisms2);
            return true;
        }
        if (processedPermissions.containsKey(alg)) {
            exemptionMechanisms = processedPermissions.get(alg);
            if (exemptionMechanisms.contains(thisExemptionMechanism)) {
                return false;
            }
        } else {
            exemptionMechanisms = new Vector(1);
        }
        exemptionMechanisms.addElement(thisExemptionMechanism);
        processedPermissions.put(alg, exemptionMechanisms);
        return true;
    }

    private static class GrantEntry {
        private final Vector<CryptoPermissionEntry> permissionEntries = new Vector();

        GrantEntry() {
        }

        void add(CryptoPermissionEntry pe) {
            this.permissionEntries.addElement(pe);
        }
    }

    static final class ParsingException
    extends GeneralSecurityException {
        private static final long serialVersionUID = 7147241245566588374L;

        ParsingException(String msg) {
            super(msg);
        }

        ParsingException(int line, String msg) {
            super("line " + line + ": " + msg);
        }

        ParsingException(int line, String expect, String actual) {
            super("line " + line + ": expected '" + expect + "', found '" + actual + "'");
        }
    }

    private static class CryptoPermissionEntry {
        String cryptoPermission;
        String alg = null;
        String exemptionMechanism = null;
        int maxKeySize = 0;
        boolean checkParam = false;
        AlgorithmParameterSpec algParamSpec = null;

        CryptoPermissionEntry() {
        }

        public int hashCode() {
            int retval = this.cryptoPermission.hashCode();
            if (this.alg != null) {
                retval ^= this.alg.hashCode();
            }
            if (this.exemptionMechanism != null) {
                retval ^= this.exemptionMechanism.hashCode();
            }
            retval ^= this.maxKeySize;
            if (this.checkParam) {
                retval ^= 0x64;
            }
            if (this.algParamSpec != null) {
                retval ^= this.algParamSpec.hashCode();
            }
            return retval;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CryptoPermissionEntry)) {
                return false;
            }
            CryptoPermissionEntry that = (CryptoPermissionEntry)obj;
            if (this.cryptoPermission == null ? that.cryptoPermission != null : !this.cryptoPermission.equals(that.cryptoPermission)) {
                return false;
            }
            if (this.alg == null ? that.alg != null : !this.alg.equalsIgnoreCase(that.alg)) {
                return false;
            }
            if (this.maxKeySize != that.maxKeySize) {
                return false;
            }
            if (this.checkParam != that.checkParam) {
                return false;
            }
            if (this.algParamSpec == null) {
                return that.algParamSpec == null;
            }
            return this.algParamSpec.equals(that.algParamSpec);
        }
    }
}

