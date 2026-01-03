/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.CryptoPermission;
import javax.crypto.CryptoPolicyParser;
import javax.crypto.PermissionsEnumerator;

final class CryptoPermissions
extends PermissionCollection
implements Serializable {
    private static final long serialVersionUID = 4946547168093391015L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("perms", Hashtable.class)};
    private transient ConcurrentHashMap<String, PermissionCollection> perms = new ConcurrentHashMap(7);

    CryptoPermissions() {
    }

    void load(InputStream in) throws IOException, CryptoPolicyParser.ParsingException {
        CryptoPolicyParser parser = new CryptoPolicyParser();
        parser.read(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        CryptoPermission[] parsingResult = parser.getPermissions();
        for (int i = 0; i < parsingResult.length; ++i) {
            this.add(parsingResult[i]);
        }
    }

    boolean isEmpty() {
        return this.perms.isEmpty();
    }

    @Override
    public void add(Permission permission) {
        if (this.isReadOnly()) {
            throw new SecurityException("Attempt to add a Permission to a readonly CryptoPermissions object");
        }
        if (!(permission instanceof CryptoPermission)) {
            return;
        }
        CryptoPermission cryptoPerm = (CryptoPermission)permission;
        PermissionCollection pc = this.getPermissionCollection(cryptoPerm);
        pc.add(cryptoPerm);
        String alg = cryptoPerm.getAlgorithm();
        this.perms.putIfAbsent(alg, pc);
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof CryptoPermission)) {
            return false;
        }
        CryptoPermission cryptoPerm = (CryptoPermission)permission;
        PermissionCollection pc = this.getPermissionCollection(cryptoPerm.getAlgorithm());
        if (pc != null) {
            return pc.implies(cryptoPerm);
        }
        return false;
    }

    @Override
    public Enumeration<Permission> elements() {
        return new PermissionsEnumerator(this.perms.elements());
    }

    CryptoPermissions getMinimum(CryptoPermissions other) {
        int i;
        CryptoPermission[] partialResult;
        PermissionCollection thatPc;
        if (other == null) {
            return null;
        }
        if (this.perms.containsKey("CryptoAllPermission")) {
            return other;
        }
        if (other.perms.containsKey("CryptoAllPermission")) {
            return this;
        }
        CryptoPermissions ret = new CryptoPermissions();
        PermissionCollection thatWildcard = other.perms.get("*");
        int maxKeySize = 0;
        if (thatWildcard != null) {
            maxKeySize = ((CryptoPermission)thatWildcard.elements().nextElement()).getMaxKeySize();
        }
        for (String alg : this.perms.keySet()) {
            PermissionCollection thisPc = this.perms.get(alg);
            thatPc = other.perms.get(alg);
            if (thatPc == null) {
                if (thatWildcard == null) continue;
                partialResult = this.getMinimum(maxKeySize, thisPc);
            } else {
                partialResult = this.getMinimum(thisPc, thatPc);
            }
            for (i = 0; i < partialResult.length; ++i) {
                ret.add(partialResult[i]);
            }
        }
        PermissionCollection thisWildcard = this.perms.get("*");
        if (thisWildcard == null) {
            return ret;
        }
        maxKeySize = ((CryptoPermission)thisWildcard.elements().nextElement()).getMaxKeySize();
        for (String alg : other.perms.keySet()) {
            if (this.perms.containsKey(alg)) continue;
            thatPc = other.perms.get(alg);
            partialResult = this.getMinimum(maxKeySize, thatPc);
            for (i = 0; i < partialResult.length; ++i) {
                ret.add(partialResult[i]);
            }
        }
        return ret;
    }

    private CryptoPermission[] getMinimum(PermissionCollection thisPc, PermissionCollection thatPc) {
        ArrayList<CryptoPermission> permList = new ArrayList<CryptoPermission>(2);
        Enumeration<Permission> thisPcPermissions = thisPc.elements();
        block0: while (thisPcPermissions.hasMoreElements()) {
            CryptoPermission thisCp = (CryptoPermission)thisPcPermissions.nextElement();
            Enumeration<Permission> thatPcPermissions = thatPc.elements();
            while (thatPcPermissions.hasMoreElements()) {
                CryptoPermission thatCp = (CryptoPermission)thatPcPermissions.nextElement();
                if (thatCp.implies(thisCp)) {
                    permList.add(thisCp);
                    continue block0;
                }
                if (!thisCp.implies(thatCp)) continue;
                permList.add(thatCp);
            }
        }
        return permList.toArray(new CryptoPermission[0]);
    }

    private CryptoPermission[] getMinimum(int maxKeySize, PermissionCollection pc) {
        ArrayList<CryptoPermission> permList = new ArrayList<CryptoPermission>(1);
        Enumeration<Permission> enum_ = pc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp = (CryptoPermission)enum_.nextElement();
            if (cp.getMaxKeySize() <= maxKeySize) {
                permList.add(cp);
                continue;
            }
            if (cp.getCheckParam()) {
                permList.add(new CryptoPermission(cp.getAlgorithm(), maxKeySize, cp.getAlgorithmParameterSpec(), cp.getExemptionMechanism()));
                continue;
            }
            permList.add(new CryptoPermission(cp.getAlgorithm(), maxKeySize, cp.getExemptionMechanism()));
        }
        return permList.toArray(new CryptoPermission[0]);
    }

    PermissionCollection getPermissionCollection(String alg) {
        PermissionCollection pc = this.perms.get("CryptoAllPermission");
        if (pc == null && (pc = this.perms.get(alg)) == null) {
            pc = this.perms.get("*");
        }
        return pc;
    }

    private PermissionCollection getPermissionCollection(CryptoPermission cryptoPerm) {
        String alg = cryptoPerm.getAlgorithm();
        PermissionCollection pc = this.perms.get(alg);
        if (pc == null) {
            pc = cryptoPerm.newPermissionCollection();
        }
        return pc;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        Hashtable permTable = (Hashtable)fields.get("perms", null);
        this.perms = permTable != null ? new ConcurrentHashMap(permTable) : new ConcurrentHashMap();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Hashtable<String, PermissionCollection> permTable = new Hashtable<String, PermissionCollection>(this.perms);
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("perms", permTable);
        s.writeFields();
    }
}

