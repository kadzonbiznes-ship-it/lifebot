/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.LocalizedMessage;
import sun.security.util.PropertyExpander;

public class PolicyParser {
    private final Vector<GrantEntry> grantEntries = new Vector();
    private Map<String, DomainEntry> domainEntries;
    private static final Debug debug = Debug.getInstance("parser", "\t[Policy Parser]");
    private StreamTokenizer st;
    private int lookahead;
    private boolean expandProp = false;
    private String keyStoreUrlString = null;
    private String keyStoreType = null;
    private String keyStoreProvider = null;
    private String storePassURL = null;

    private String expand(String value) throws PropertyExpander.ExpandException {
        return this.expand(value, false);
    }

    private String expand(String value, boolean encodeURL) throws PropertyExpander.ExpandException {
        if (!this.expandProp) {
            return value;
        }
        return PropertyExpander.expand(value, encodeURL);
    }

    public PolicyParser() {
    }

    public PolicyParser(boolean expandProp) {
        this();
        this.expandProp = expandProp;
    }

    public void read(Reader policy) throws ParsingException, IOException {
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
        this.lookahead = this.st.nextToken();
        GrantEntry ge = null;
        while (this.lookahead != -1) {
            if (this.peek("grant")) {
                ge = this.parseGrantEntry();
                if (ge != null) {
                    this.add(ge);
                }
            } else if (this.peek("keystore") && this.keyStoreUrlString == null) {
                this.parseKeyStoreEntry();
            } else if (this.peek("keystorePasswordURL") && this.storePassURL == null) {
                this.parseStorePassURL();
            } else if (ge == null && this.keyStoreUrlString == null && this.storePassURL == null && this.peek("domain")) {
                DomainEntry de;
                String domainName;
                if (this.domainEntries == null) {
                    this.domainEntries = new TreeMap<String, DomainEntry>();
                }
                if (this.domainEntries.putIfAbsent(domainName = (de = this.parseDomainEntry()).getName(), de) != null) {
                    LocalizedMessage localizedMsg = new LocalizedMessage("duplicate.keystore.domain.name");
                    Object[] source = new Object[]{domainName};
                    String msg = "duplicate keystore domain name: " + domainName;
                    throw new ParsingException(msg, localizedMsg, source);
                }
            }
            this.match(";");
        }
        if (this.keyStoreUrlString == null && this.storePassURL != null) {
            throw new ParsingException(LocalizedMessage.getNonlocalized("keystorePasswordURL.can.not.be.specified.without.also.specifying.keystore", new Object[0]));
        }
    }

    public void add(GrantEntry ge) {
        this.grantEntries.addElement(ge);
    }

    public void replace(GrantEntry origGe, GrantEntry newGe) {
        this.grantEntries.setElementAt(newGe, this.grantEntries.indexOf(origGe));
    }

    public boolean remove(GrantEntry ge) {
        return this.grantEntries.removeElement(ge);
    }

    public String getKeyStoreUrl() {
        try {
            if (this.keyStoreUrlString != null && this.keyStoreUrlString.length() != 0) {
                return this.expand(this.keyStoreUrlString, true).replace(File.separatorChar, '/');
            }
        }
        catch (PropertyExpander.ExpandException peee) {
            if (debug != null) {
                debug.println(peee.toString());
            }
            return null;
        }
        return null;
    }

    public void setKeyStoreUrl(String url) {
        this.keyStoreUrlString = url;
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }

    public void setKeyStoreType(String type) {
        this.keyStoreType = type;
    }

    public String getKeyStoreProvider() {
        return this.keyStoreProvider;
    }

    public void setKeyStoreProvider(String provider) {
        this.keyStoreProvider = provider;
    }

    public String getStorePassURL() {
        try {
            if (this.storePassURL != null && this.storePassURL.length() != 0) {
                return this.expand(this.storePassURL, true).replace(File.separatorChar, '/');
            }
        }
        catch (PropertyExpander.ExpandException peee) {
            if (debug != null) {
                debug.println(peee.toString());
            }
            return null;
        }
        return null;
    }

    public void setStorePassURL(String storePassURL) {
        this.storePassURL = storePassURL;
    }

    public Enumeration<GrantEntry> grantElements() {
        return this.grantEntries.elements();
    }

    public Collection<DomainEntry> getDomainEntries() {
        return this.domainEntries.values();
    }

    public void write(Writer policy) {
        PrintWriter out = new PrintWriter(new BufferedWriter(policy));
        out.println("/* AUTOMATICALLY GENERATED ON " + new Date() + "*/");
        out.println("/* DO NOT EDIT */");
        out.println();
        if (this.keyStoreUrlString != null) {
            this.writeKeyStoreEntry(out);
        }
        if (this.storePassURL != null) {
            this.writeStorePassURL(out);
        }
        for (GrantEntry ge : this.grantEntries) {
            ge.write(out);
            out.println();
        }
        out.flush();
    }

    private void parseKeyStoreEntry() throws ParsingException, IOException {
        this.match("keystore");
        this.keyStoreUrlString = this.match("quoted string");
        if (!this.peek(",")) {
            return;
        }
        this.match(",");
        if (!this.peek("\"")) {
            throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("expected.keystore.type", new Object[0]));
        }
        this.keyStoreType = this.match("quoted string");
        if (!this.peek(",")) {
            return;
        }
        this.match(",");
        if (!this.peek("\"")) {
            throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("expected.keystore.provider", new Object[0]));
        }
        this.keyStoreProvider = this.match("quoted string");
    }

    private void parseStorePassURL() throws ParsingException, IOException {
        this.match("keyStorePasswordURL");
        this.storePassURL = this.match("quoted string");
    }

    private void writeKeyStoreEntry(PrintWriter out) {
        out.print("keystore \"");
        out.print(this.keyStoreUrlString);
        out.print('\"');
        if (this.keyStoreType != null && !this.keyStoreType.isEmpty()) {
            out.print(", \"" + this.keyStoreType + "\"");
        }
        if (this.keyStoreProvider != null && !this.keyStoreProvider.isEmpty()) {
            out.print(", \"" + this.keyStoreProvider + "\"");
        }
        out.println(";");
        out.println();
    }

    private void writeStorePassURL(PrintWriter out) {
        out.print("keystorePasswordURL \"");
        out.print(this.storePassURL);
        out.print('\"');
        out.println(";");
        out.println();
    }

    private GrantEntry parseGrantEntry() throws ParsingException, IOException {
        GrantEntry e = new GrantEntry();
        LinkedList<PrincipalEntry> principals = null;
        boolean ignoreEntry = false;
        this.match("grant");
        while (!this.peek("{")) {
            if (this.peekAndMatch("Codebase")) {
                if (e.codeBase != null) {
                    throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("multiple.Codebase.expressions", new Object[0]));
                }
                e.codeBase = this.match("quoted string");
                this.peekAndMatch(",");
                continue;
            }
            if (this.peekAndMatch("SignedBy")) {
                if (e.signedBy != null) {
                    throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("multiple.SignedBy.expressions", new Object[0]));
                }
                e.signedBy = this.match("quoted string");
                StringTokenizer aliases = new StringTokenizer(e.signedBy, ",", true);
                int actr = 0;
                int cctr = 0;
                while (aliases.hasMoreTokens()) {
                    String alias = aliases.nextToken().trim();
                    if (alias.equals(",")) {
                        ++cctr;
                        continue;
                    }
                    if (alias.isEmpty()) continue;
                    ++actr;
                }
                if (actr <= cctr) {
                    throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("SignedBy.has.empty.alias", new Object[0]));
                }
                this.peekAndMatch(",");
                continue;
            }
            if (this.peekAndMatch("Principal")) {
                String principalName;
                String principalClass;
                if (principals == null) {
                    principals = new LinkedList<PrincipalEntry>();
                }
                if (this.peek("\"")) {
                    principalClass = "PolicyParser.REPLACE_NAME";
                    principalName = this.match("principal type");
                } else {
                    if (this.peek("*")) {
                        this.match("*");
                        principalClass = "WILDCARD_PRINCIPAL_CLASS";
                    } else {
                        principalClass = this.match("principal type");
                    }
                    if (this.peek("*")) {
                        this.match("*");
                        principalName = "WILDCARD_PRINCIPAL_NAME";
                    } else {
                        principalName = this.match("quoted string");
                    }
                    if (principalClass.equals("WILDCARD_PRINCIPAL_CLASS") && !principalName.equals("WILDCARD_PRINCIPAL_NAME")) {
                        if (debug != null) {
                            debug.println("disallowing principal that has WILDCARD class but no WILDCARD name");
                        }
                        throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("can.not.specify.Principal.with.a.wildcard.class.without.a.wildcard.name", new Object[0]));
                    }
                }
                try {
                    principalName = this.expand(principalName);
                    if (principalClass.equals("javax.security.auth.x500.X500Principal") && !principalName.equals("WILDCARD_PRINCIPAL_NAME")) {
                        X500Principal p = new X500Principal(new X500Principal(principalName).toString());
                        principalName = p.getName();
                    }
                    principals.add(new PrincipalEntry(principalClass, principalName));
                }
                catch (PropertyExpander.ExpandException peee) {
                    if (debug != null) {
                        debug.println("principal name expansion failed: " + principalName);
                    }
                    ignoreEntry = true;
                }
                this.peekAndMatch(",");
                continue;
            }
            throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("expected.codeBase.or.SignedBy.or.Principal", new Object[0]));
        }
        if (principals != null) {
            e.principals = principals;
        }
        this.match("{");
        while (!this.peek("}")) {
            if (this.peek("Permission")) {
                try {
                    PermissionEntry pe = this.parsePermissionEntry();
                    e.add(pe);
                }
                catch (PropertyExpander.ExpandException peee) {
                    if (debug != null) {
                        debug.println(peee.toString());
                    }
                    this.skipEntry();
                }
                this.match(";");
                continue;
            }
            throw new ParsingException(this.st.lineno(), LocalizedMessage.getNonlocalized("expected.permission.entry", new Object[0]));
        }
        this.match("}");
        try {
            if (e.signedBy != null) {
                e.signedBy = this.expand(e.signedBy);
            }
            if (e.codeBase != null) {
                e.codeBase = this.expand(e.codeBase, true).replace(File.separatorChar, '/');
            }
        }
        catch (PropertyExpander.ExpandException peee) {
            if (debug != null) {
                debug.println(peee.toString());
            }
            return null;
        }
        return ignoreEntry ? null : e;
    }

    private PermissionEntry parsePermissionEntry() throws ParsingException, IOException, PropertyExpander.ExpandException {
        PermissionEntry e = new PermissionEntry();
        this.match("Permission");
        e.permission = this.match("permission type");
        if (this.peek("\"")) {
            e.name = this.expand(this.match("quoted string"));
        }
        if (!this.peek(",")) {
            return e;
        }
        this.match(",");
        if (this.peek("\"")) {
            e.action = this.expand(this.match("quoted string"));
            if (!this.peek(",")) {
                return e;
            }
            this.match(",");
        }
        if (this.peekAndMatch("SignedBy")) {
            e.signedBy = this.expand(this.match("quoted string"));
        }
        return e;
    }

    private DomainEntry parseDomainEntry() throws ParsingException, IOException {
        HashMap<String, String> properties = new HashMap();
        this.match("domain");
        String name = this.match("domain name");
        while (!this.peek("{")) {
            properties = this.parseProperties("{");
        }
        this.match("{");
        DomainEntry domainEntry = new DomainEntry(name, properties);
        while (!this.peek("}")) {
            this.match("keystore");
            name = this.match("keystore name");
            if (!this.peek("}")) {
                properties = this.parseProperties(";");
            }
            this.match(";");
            domainEntry.add(new KeyStoreEntry(name, properties));
        }
        this.match("}");
        return domainEntry;
    }

    private Map<String, String> parseProperties(String terminator) throws ParsingException, IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        while (!this.peek(terminator)) {
            String value;
            String key = this.match("property name");
            this.match("=");
            try {
                value = this.expand(this.match("quoted string"));
            }
            catch (PropertyExpander.ExpandException peee) {
                throw new IOException(peee.getLocalizedMessage());
            }
            properties.put(key.toLowerCase(Locale.ENGLISH), value);
        }
        return properties;
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
            case 44: {
                if (!expect.equalsIgnoreCase(",")) break;
                found = true;
                break;
            }
            case 123: {
                if (!expect.equalsIgnoreCase("{")) break;
                found = true;
                break;
            }
            case 125: {
                if (!expect.equalsIgnoreCase("}")) break;
                found = true;
                break;
            }
            case 34: {
                if (!expect.equalsIgnoreCase("\"")) break;
                found = true;
                break;
            }
            case 42: {
                if (!expect.equalsIgnoreCase("*")) break;
                found = true;
                break;
            }
            case 59: {
                if (!expect.equalsIgnoreCase(";")) break;
                found = true;
                break;
            }
        }
        return found;
    }

    private String match(String expect) throws ParsingException, IOException {
        String value = null;
        switch (this.lookahead) {
            case -2: {
                throw new ParsingException(this.st.lineno(), expect, LocalizedMessage.getNonlocalized("number.", new Object[0]) + this.st.nval);
            }
            case -1: {
                LocalizedMessage localizedMsg = new LocalizedMessage("expected.expect.read.end.of.file.");
                Object[] source = new Object[]{expect};
                String msg = "expected [" + expect + "], read [end of file]";
                throw new ParsingException(msg, localizedMsg, source);
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
                if (expect.equalsIgnoreCase("principal type")) {
                    value = this.st.sval;
                    this.lookahead = this.st.nextToken();
                    break;
                }
                if (expect.equalsIgnoreCase("domain name") || expect.equalsIgnoreCase("keystore name") || expect.equalsIgnoreCase("property name")) {
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
                if (expect.equalsIgnoreCase("principal type")) {
                    value = this.st.sval;
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, this.st.sval);
            }
            case 44: {
                if (expect.equalsIgnoreCase(",")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, ",");
            }
            case 123: {
                if (expect.equalsIgnoreCase("{")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "{");
            }
            case 125: {
                if (expect.equalsIgnoreCase("}")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "}");
            }
            case 59: {
                if (expect.equalsIgnoreCase(";")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, ";");
            }
            case 42: {
                if (expect.equalsIgnoreCase("*")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "*");
            }
            case 61: {
                if (expect.equalsIgnoreCase("=")) {
                    this.lookahead = this.st.nextToken();
                    break;
                }
                throw new ParsingException(this.st.lineno(), expect, "=");
            }
            default: {
                throw new ParsingException(this.st.lineno(), expect, String.valueOf((char)this.lookahead));
            }
        }
        return value;
    }

    private void skipEntry() throws ParsingException, IOException {
        while (this.lookahead != 59) {
            switch (this.lookahead) {
                case -2: {
                    throw new ParsingException(this.st.lineno(), ";", LocalizedMessage.getNonlocalized("number.", new Object[0]) + this.st.nval);
                }
                case -1: {
                    throw new ParsingException(LocalizedMessage.getNonlocalized("expected.read.end.of.file.", new Object[0]));
                }
            }
            this.lookahead = this.st.nextToken();
        }
    }

    public static void main(String[] arg) throws Exception {
        try (FileReader fr = new FileReader(arg[0]);
             FileWriter fw = new FileWriter(arg[1]);){
            PolicyParser pp = new PolicyParser(true);
            pp.read(fr);
            pp.write(fw);
        }
    }

    public static class GrantEntry {
        public String signedBy;
        public String codeBase;
        public LinkedList<PrincipalEntry> principals;
        public Vector<PermissionEntry> permissionEntries;

        public GrantEntry() {
            this.principals = new LinkedList();
            this.permissionEntries = new Vector();
        }

        public GrantEntry(String signedBy, String codeBase) {
            this.codeBase = codeBase;
            this.signedBy = signedBy;
            this.principals = new LinkedList();
            this.permissionEntries = new Vector();
        }

        public void add(PermissionEntry pe) {
            this.permissionEntries.addElement(pe);
        }

        public boolean remove(PrincipalEntry pe) {
            return this.principals.remove(pe);
        }

        public boolean remove(PermissionEntry pe) {
            return this.permissionEntries.removeElement(pe);
        }

        public boolean contains(PrincipalEntry pe) {
            return this.principals.contains(pe);
        }

        public boolean contains(PermissionEntry pe) {
            return this.permissionEntries.contains(pe);
        }

        public Enumeration<PermissionEntry> permissionElements() {
            return this.permissionEntries.elements();
        }

        public void write(PrintWriter out) {
            out.print("grant");
            if (this.signedBy != null) {
                out.print(" signedBy \"");
                out.print(this.signedBy);
                out.print('\"');
                if (this.codeBase != null) {
                    out.print(", ");
                }
            }
            if (this.codeBase != null) {
                out.print(" codeBase \"");
                out.print(this.codeBase);
                out.print('\"');
                if (this.principals != null && this.principals.size() > 0) {
                    out.print(",\n");
                }
            }
            if (this.principals != null && this.principals.size() > 0) {
                Iterator pli = this.principals.iterator();
                while (pli.hasNext()) {
                    out.print("      ");
                    PrincipalEntry principalEntry = (PrincipalEntry)pli.next();
                    principalEntry.write(out);
                    if (!pli.hasNext()) continue;
                    out.print(",\n");
                }
            }
            out.println(" {");
            for (PermissionEntry permissionEntry : this.permissionEntries) {
                out.write("  ");
                permissionEntry.write(out);
            }
            out.println("};");
        }

        public Object clone() {
            GrantEntry ge = new GrantEntry();
            ge.codeBase = this.codeBase;
            ge.signedBy = this.signedBy;
            ge.principals = new LinkedList<PrincipalEntry>(this.principals);
            ge.permissionEntries = new Vector<PermissionEntry>(this.permissionEntries);
            return ge;
        }
    }

    static class DomainEntry {
        private final String name;
        private final Map<String, String> properties;
        private final Map<String, KeyStoreEntry> entries;

        DomainEntry(String name, Map<String, String> properties) {
            this.name = name;
            this.properties = properties;
            this.entries = new HashMap<String, KeyStoreEntry>();
        }

        String getName() {
            return this.name;
        }

        Map<String, String> getProperties() {
            return this.properties;
        }

        Collection<KeyStoreEntry> getEntries() {
            return this.entries.values();
        }

        void add(KeyStoreEntry entry) throws ParsingException {
            String keystoreName = entry.getName();
            if (this.entries.containsKey(keystoreName)) {
                LocalizedMessage localizedMsg = new LocalizedMessage("duplicate.keystore.name");
                Object[] source = new Object[]{keystoreName};
                String msg = "duplicate keystore name: " + keystoreName;
                throw new ParsingException(msg, localizedMsg, source);
            }
            this.entries.put(keystoreName, entry);
        }

        public String toString() {
            StringBuilder s = new StringBuilder("\ndomain ").append(this.name);
            if (this.properties != null) {
                for (Map.Entry<String, String> property : this.properties.entrySet()) {
                    s.append("\n        ").append(property.getKey()).append('=').append(property.getValue());
                }
            }
            s.append(" {\n");
            for (KeyStoreEntry entry : this.entries.values()) {
                s.append(entry).append("\n");
            }
            s.append("}");
            return s.toString();
        }
    }

    public static class ParsingException
    extends GeneralSecurityException {
        private static final long serialVersionUID = -4330692689482574072L;
        private String i18nMessage;
        private LocalizedMessage localizedMsg;
        private Object[] source;

        public ParsingException(String msg) {
            super(msg);
            this.i18nMessage = msg;
        }

        public ParsingException(String msg, LocalizedMessage localizedMsg, Object[] source) {
            super(msg);
            this.localizedMsg = localizedMsg;
            this.source = source;
        }

        public ParsingException(int line, String msg) {
            super("line " + line + ": " + msg);
            this.localizedMsg = new LocalizedMessage("line.number.msg");
            this.source = new Object[]{line, msg};
        }

        public ParsingException(int line, String expect, String actual) {
            super("line " + line + ": expected [" + expect + "], found [" + actual + "]");
            this.localizedMsg = new LocalizedMessage("line.number.expected.expect.found.actual.");
            this.source = new Object[]{line, expect, actual};
        }

        public String getNonlocalizedMessage() {
            return this.i18nMessage != null ? this.i18nMessage : this.localizedMsg.formatNonlocalized(this.source);
        }
    }

    public static class PrincipalEntry
    implements Principal {
        public static final String WILDCARD_CLASS = "WILDCARD_PRINCIPAL_CLASS";
        public static final String WILDCARD_NAME = "WILDCARD_PRINCIPAL_NAME";
        public static final String REPLACE_NAME = "PolicyParser.REPLACE_NAME";
        String principalClass;
        String principalName;

        public PrincipalEntry(String principalClass, String principalName) {
            if (principalClass == null || principalName == null) {
                throw new NullPointerException(LocalizedMessage.getNonlocalized("null.principalClass.or.principalName", new Object[0]));
            }
            this.principalClass = principalClass;
            this.principalName = principalName;
        }

        boolean isWildcardName() {
            return this.principalName.equals(WILDCARD_NAME);
        }

        boolean isWildcardClass() {
            return this.principalClass.equals(WILDCARD_CLASS);
        }

        boolean isReplaceName() {
            return this.principalClass.equals(REPLACE_NAME);
        }

        public String getPrincipalClass() {
            return this.principalClass;
        }

        public String getPrincipalName() {
            return this.principalName;
        }

        public String getDisplayClass() {
            if (this.isWildcardClass()) {
                return "*";
            }
            if (this.isReplaceName()) {
                return "";
            }
            return this.principalClass;
        }

        public String getDisplayName() {
            return this.getDisplayName(false);
        }

        public String getDisplayName(boolean addQuote) {
            if (this.isWildcardName()) {
                return "*";
            }
            if (addQuote) {
                return "\"" + this.principalName + "\"";
            }
            return this.principalName;
        }

        @Override
        public String getName() {
            return this.principalName;
        }

        @Override
        public String toString() {
            if (!this.isReplaceName()) {
                return this.getDisplayClass() + "/" + this.getDisplayName();
            }
            return this.getDisplayName();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PrincipalEntry)) {
                return false;
            }
            PrincipalEntry that = (PrincipalEntry)obj;
            return this.principalClass.equals(that.principalClass) && this.principalName.equals(that.principalName);
        }

        @Override
        public int hashCode() {
            return this.principalClass.hashCode();
        }

        public void write(PrintWriter out) {
            out.print("principal " + this.getDisplayClass() + " " + this.getDisplayName(true));
        }
    }

    public static class PermissionEntry {
        public String permission;
        public String name;
        public String action;
        public String signedBy;

        public PermissionEntry() {
        }

        public PermissionEntry(String permission, String name, String action) {
            this.permission = permission;
            this.name = name;
            this.action = action;
        }

        public int hashCode() {
            int retval = this.permission.hashCode();
            if (this.name != null) {
                retval ^= this.name.hashCode();
            }
            if (this.action != null) {
                retval ^= this.action.hashCode();
            }
            return retval;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof PermissionEntry)) {
                return false;
            }
            PermissionEntry that = (PermissionEntry)obj;
            if (this.permission == null ? that.permission != null : !this.permission.equals(that.permission)) {
                return false;
            }
            if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
                return false;
            }
            if (this.action == null ? that.action != null : !this.action.equals(that.action)) {
                return false;
            }
            if (this.signedBy == null) {
                return that.signedBy == null;
            }
            return this.signedBy.equals(that.signedBy);
        }

        public void write(PrintWriter out) {
            out.print("permission ");
            out.print(this.permission);
            if (this.name != null) {
                out.print(" \"");
                out.print(this.name.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\\\""));
                out.print('\"');
            }
            if (this.action != null) {
                out.print(", \"");
                out.print(this.action);
                out.print('\"');
            }
            if (this.signedBy != null) {
                out.print(", signedBy \"");
                out.print(this.signedBy);
                out.print('\"');
            }
            out.println(";");
        }
    }

    static class KeyStoreEntry {
        private final String name;
        private final Map<String, String> properties;

        KeyStoreEntry(String name, Map<String, String> properties) {
            this.name = name;
            this.properties = properties;
        }

        String getName() {
            return this.name;
        }

        Map<String, String> getProperties() {
            return this.properties;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("\n    keystore ").append(this.name);
            if (this.properties != null) {
                for (Map.Entry<String, String> property : this.properties.entrySet()) {
                    s.append("\n        ").append(property.getKey()).append('=').append(property.getValue());
                }
            }
            s.append(";");
            return s.toString();
        }
    }
}

