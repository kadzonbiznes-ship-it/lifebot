/*
 * Decompiled with CFR 0.152.
 */
package javax.security.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.DomainCombiner;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import javax.security.auth.AuthPermission;
import javax.security.auth.PrivateCredentialPermission;
import javax.security.auth.SubjectDomainCombiner;
import sun.security.util.ResourcesMgr;

public final class Subject
implements Serializable {
    private static final long serialVersionUID = -8308522755600156056L;
    Set<Principal> principals;
    transient Set<Object> pubCredentials;
    transient Set<Object> privCredentials;
    private volatile boolean readOnly;
    private static final int PRINCIPAL_SET = 1;
    private static final int PUB_CREDENTIAL_SET = 2;
    private static final int PRIV_CREDENTIAL_SET = 3;
    private static final ProtectionDomain[] NULL_PD_ARRAY = new ProtectionDomain[0];

    public Subject() {
        this.principals = Collections.synchronizedSet(new SecureSet(this, 1));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3));
    }

    public Subject(boolean readOnly, Set<? extends Principal> principals, Set<?> pubCredentials, Set<?> privCredentials) {
        LinkedList<? extends Principal> principalList = Subject.collectionNullClean(principals);
        LinkedList<?> pubCredsList = Subject.collectionNullClean(pubCredentials);
        LinkedList<?> privCredsList = Subject.collectionNullClean(privCredentials);
        this.principals = Collections.synchronizedSet(new SecureSet<Principal>(this, 1, principalList));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2, pubCredsList));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3, privCredsList));
        this.readOnly = readOnly;
    }

    public void setReadOnly() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.SET_READ_ONLY_PERMISSION);
        }
        this.readOnly = true;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Deprecated(since="17", forRemoval=true)
    public static Subject getSubject(final AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.GET_SUBJECT_PERMISSION);
        }
        Objects.requireNonNull(acc, ResourcesMgr.getString("invalid.null.AccessControlContext.provided"));
        return AccessController.doPrivileged(new PrivilegedAction<Subject>(){

            @Override
            public Subject run() {
                DomainCombiner dc = acc.getDomainCombiner();
                if (!(dc instanceof SubjectDomainCombiner)) {
                    return null;
                }
                SubjectDomainCombiner sdc = (SubjectDomainCombiner)dc;
                return sdc.getSubject();
            }
        });
    }

    public static Subject current() {
        return Subject.getSubject(AccessController.getContext());
    }

    public static <T> T callAs(Subject subject, Callable<T> action) throws CompletionException {
        Objects.requireNonNull(action);
        try {
            PrivilegedExceptionAction<Object> pa = () -> action.call();
            Object result = Subject.doAs(subject, pa);
            return (T)result;
        }
        catch (PrivilegedActionException e) {
            throw new CompletionException(e.getCause());
        }
        catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    @Deprecated(since="18", forRemoval=true)
    public static <T> T doAs(Subject subject, PrivilegedAction<T> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        Objects.requireNonNull(action, ResourcesMgr.getString("invalid.null.action.provided"));
        AccessControlContext currentAcc = AccessController.getContext();
        return AccessController.doPrivileged(action, Subject.createContext(subject, currentAcc));
    }

    @Deprecated(since="18", forRemoval=true)
    public static <T> T doAs(Subject subject, PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        Objects.requireNonNull(action, ResourcesMgr.getString("invalid.null.action.provided"));
        AccessControlContext currentAcc = AccessController.getContext();
        return AccessController.doPrivileged(action, Subject.createContext(subject, currentAcc));
    }

    @Deprecated(since="17", forRemoval=true)
    public static <T> T doAsPrivileged(Subject subject, PrivilegedAction<T> action, AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        Objects.requireNonNull(action, ResourcesMgr.getString("invalid.null.action.provided"));
        AccessControlContext callerAcc = acc == null ? new AccessControlContext(NULL_PD_ARRAY) : acc;
        return AccessController.doPrivileged(action, Subject.createContext(subject, callerAcc));
    }

    @Deprecated(since="17", forRemoval=true)
    public static <T> T doAsPrivileged(Subject subject, PrivilegedExceptionAction<T> action, AccessControlContext acc) throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        Objects.requireNonNull(action, ResourcesMgr.getString("invalid.null.action.provided"));
        AccessControlContext callerAcc = acc == null ? new AccessControlContext(NULL_PD_ARRAY) : acc;
        return AccessController.doPrivileged(action, Subject.createContext(subject, callerAcc));
    }

    private static AccessControlContext createContext(final Subject subject, final AccessControlContext acc) {
        return AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>(){

            @Override
            public AccessControlContext run() {
                if (subject == null) {
                    return new AccessControlContext(acc, null);
                }
                return new AccessControlContext(acc, new SubjectDomainCombiner(subject));
            }
        });
    }

    public Set<Principal> getPrincipals() {
        return this.principals;
    }

    public <T extends Principal> Set<T> getPrincipals(Class<T> c) {
        Objects.requireNonNull(c, ResourcesMgr.getString("invalid.null.Class.provided"));
        return new ClassSet<T>(1, c);
    }

    public Set<Object> getPublicCredentials() {
        return this.pubCredentials;
    }

    public Set<Object> getPrivateCredentials() {
        return this.privCredentials;
    }

    public <T> Set<T> getPublicCredentials(Class<T> c) {
        Objects.requireNonNull(c, ResourcesMgr.getString("invalid.null.Class.provided"));
        return new ClassSet<T>(2, c);
    }

    public <T> Set<T> getPrivateCredentials(Class<T> c) {
        Objects.requireNonNull(c, ResourcesMgr.getString("invalid.null.Class.provided"));
        return new ClassSet<T>(3, c);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof Subject) {
            HashSet<Object> thatPrivCredentials;
            HashSet<Object> thatPubCredentials;
            HashSet<Principal> thatPrincipals;
            Subject that = (Subject)o;
            Set<Principal> set = that.principals;
            synchronized (set) {
                thatPrincipals = new HashSet<Principal>(that.principals);
            }
            if (!this.principals.equals(thatPrincipals)) {
                return false;
            }
            Set<Object> set2 = that.pubCredentials;
            synchronized (set2) {
                thatPubCredentials = new HashSet<Object>(that.pubCredentials);
            }
            if (!this.pubCredentials.equals(thatPubCredentials)) {
                return false;
            }
            Set<Object> set3 = that.privCredentials;
            synchronized (set3) {
                thatPrivCredentials = new HashSet<Object>(that.privCredentials);
            }
            return this.privCredentials.equals(thatPrivCredentials);
        }
        return false;
    }

    public String toString() {
        return this.toString(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    String toString(boolean includePrivateCredentials) {
        String s = ResourcesMgr.getString("Subject.");
        String suffix = "";
        Set<Object> set = this.principals;
        synchronized (set) {
            for (Principal p : this.principals) {
                suffix = suffix + ResourcesMgr.getString(".Principal.") + p.toString() + ResourcesMgr.getString("NEWLINE");
            }
        }
        set = this.pubCredentials;
        synchronized (set) {
            for (Object o : this.pubCredentials) {
                suffix = suffix + ResourcesMgr.getString(".Public.Credential.") + o.toString() + ResourcesMgr.getString("NEWLINE");
            }
        }
        if (includePrivateCredentials) {
            set = this.privCredentials;
            synchronized (set) {
                Iterator<Object> pI = this.privCredentials.iterator();
                while (pI.hasNext()) {
                    try {
                        Object o;
                        o = pI.next();
                        suffix = suffix + ResourcesMgr.getString(".Private.Credential.") + o.toString() + ResourcesMgr.getString("NEWLINE");
                    }
                    catch (SecurityException se) {
                        suffix = suffix + ResourcesMgr.getString(".Private.Credential.inaccessible.");
                        break;
                    }
                }
            }
        }
        return s + suffix;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int hashCode() {
        int hashCode = 0;
        Set<Object> set = this.principals;
        synchronized (set) {
            for (Principal p : this.principals) {
                hashCode ^= p.hashCode();
            }
        }
        set = this.pubCredentials;
        synchronized (set) {
            for (Object pubCredential : this.pubCredentials) {
                hashCode ^= this.getCredHashCode(pubCredential);
            }
        }
        return hashCode;
    }

    private int getCredHashCode(Object o) {
        try {
            return o.hashCode();
        }
        catch (IllegalStateException ise) {
            return o.getClass().toString().hashCode();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        Set<Principal> set = this.principals;
        synchronized (set) {
            oos.defaultWriteObject();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = s.readFields();
        this.readOnly = gf.get("readOnly", false);
        Set inputPrincs = (Set)gf.get("principals", null);
        Objects.requireNonNull(inputPrincs, ResourcesMgr.getString("invalid.null.input.s."));
        try {
            LinkedList principalList = Subject.collectionNullClean(inputPrincs);
            this.principals = Collections.synchronizedSet(new SecureSet(this, 1, principalList));
        }
        catch (NullPointerException npe) {
            this.principals = Collections.synchronizedSet(new SecureSet(this, 1));
        }
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3));
    }

    private static <E> LinkedList<E> collectionNullClean(Collection<? extends E> coll) {
        Objects.requireNonNull(coll, ResourcesMgr.getString("invalid.null.input.s."));
        LinkedList<E> output = new LinkedList<E>();
        for (E e : coll) {
            output.add(Objects.requireNonNull(e, ResourcesMgr.getString("invalid.null.input.s.")));
        }
        return output;
    }

    private static class SecureSet<E>
    implements Set<E>,
    Serializable {
        private static final long serialVersionUID = 7911754171111800359L;
        private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("this$0", Subject.class), new ObjectStreamField("elements", LinkedList.class), new ObjectStreamField("which", Integer.TYPE)};
        Subject subject;
        LinkedList<E> elements;
        private int which;

        SecureSet(Subject subject, int which) {
            this.subject = subject;
            this.which = which;
            this.elements = new LinkedList();
        }

        SecureSet(Subject subject, int which, LinkedList<E> list) {
            this.subject = subject;
            this.which = which;
            this.elements = list;
        }

        @Override
        public int size() {
            return this.elements.size();
        }

        @Override
        public Iterator<E> iterator() {
            final LinkedList<E> list = this.elements;
            return new Iterator<E>(this){
                final ListIterator<E> i;
                final /* synthetic */ SecureSet this$0;
                {
                    this.this$0 = this$0;
                    this.i = list.listIterator(0);
                }

                @Override
                public boolean hasNext() {
                    return this.i.hasNext();
                }

                @Override
                public E next() {
                    if (this.this$0.which != 3) {
                        return this.i.next();
                    }
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        try {
                            sm.checkPermission(new PrivateCredentialPermission(list.get(this.i.nextIndex()).getClass().getName(), this.this$0.subject.getPrincipals()));
                        }
                        catch (SecurityException se) {
                            this.i.next();
                            throw se;
                        }
                    }
                    return this.i.next();
                }

                @Override
                public void remove() {
                    if (this.this$0.subject.isReadOnly()) {
                        throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
                    }
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        switch (this.this$0.which) {
                            case 1: {
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                                break;
                            }
                            case 2: {
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                                break;
                            }
                            default: {
                                sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                            }
                        }
                    }
                    this.i.remove();
                }
            };
        }

        @Override
        public boolean add(E o) {
            Objects.requireNonNull(o, ResourcesMgr.getString("invalid.null.input.s."));
            if (this.subject.isReadOnly()) {
                throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
            }
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                switch (this.which) {
                    case 1: {
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                        break;
                    }
                    case 2: {
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                        break;
                    }
                    default: {
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                    }
                }
            }
            switch (this.which) {
                case 1: {
                    if (o instanceof Principal) break;
                    throw new SecurityException(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set"));
                }
            }
            if (!this.elements.contains(o)) {
                return this.elements.add(o);
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            Objects.requireNonNull(o, ResourcesMgr.getString("invalid.null.input.s."));
            final Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                Object next = this.which != 3 ? e.next() : AccessController.doPrivileged(new PrivilegedAction<E>(){

                    @Override
                    public E run() {
                        return e.next();
                    }
                });
                if (!next.equals(o)) continue;
                e.remove();
                return true;
            }
            return false;
        }

        @Override
        public boolean contains(Object o) {
            Objects.requireNonNull(o, ResourcesMgr.getString("invalid.null.input.s."));
            final Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                Object next;
                if (this.which != 3) {
                    next = e.next();
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new PrivateCredentialPermission(o.getClass().getName(), this.subject.getPrincipals()));
                    }
                    next = AccessController.doPrivileged(new PrivilegedAction<E>(){

                        @Override
                        public E run() {
                            return e.next();
                        }
                    });
                }
                if (!next.equals(o)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            boolean result = false;
            c = Subject.collectionNullClean(c);
            for (E item : c) {
                result |= this.add(item);
            }
            return result;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            c = Subject.collectionNullClean(c);
            boolean modified = false;
            final Iterator<E> e = this.iterator();
            block0: while (e.hasNext()) {
                Object next = this.which != 3 ? e.next() : AccessController.doPrivileged(new PrivilegedAction<E>(){

                    @Override
                    public E run() {
                        return e.next();
                    }
                });
                for (Object o : c) {
                    if (!next.equals(o)) continue;
                    e.remove();
                    modified = true;
                    continue block0;
                }
            }
            return modified;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            c = Subject.collectionNullClean(c);
            for (Object item : c) {
                if (this.contains(item)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            c = Subject.collectionNullClean(c);
            boolean modified = false;
            final Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                Object next = this.which != 3 ? e.next() : AccessController.doPrivileged(new PrivilegedAction<E>(){

                    @Override
                    public E run() {
                        return e.next();
                    }
                });
                if (c.contains(next)) continue;
                e.remove();
                modified = true;
            }
            return modified;
        }

        @Override
        public void clear() {
            final Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                Object next = this.which != 3 ? e.next() : AccessController.doPrivileged(new PrivilegedAction<E>(){

                    @Override
                    public E run() {
                        return e.next();
                    }
                });
                e.remove();
            }
        }

        @Override
        public boolean isEmpty() {
            return this.elements.isEmpty();
        }

        @Override
        public Object[] toArray() {
            Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                e.next();
            }
            return this.elements.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            Iterator<E> e = this.iterator();
            while (e.hasNext()) {
                e.next();
            }
            return this.elements.toArray(a);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Collection c = (Collection)o;
            if (c.size() != this.size()) {
                return false;
            }
            try {
                return this.containsAll(c);
            }
            catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (E obj : this) {
                h += Objects.hashCode(obj);
            }
            return h;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            if (this.which == 3) {
                Iterator<E> i = this.iterator();
                while (i.hasNext()) {
                    i.next();
                }
            }
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("this$0", this.subject);
            fields.put("elements", this.elements);
            fields.put("which", this.which);
            oos.writeFields();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField fields = ois.readFields();
            this.subject = (Subject)fields.get("this$0", null);
            this.which = fields.get("which", 0);
            LinkedList tmp = (LinkedList)fields.get("elements", null);
            this.elements = Subject.collectionNullClean(tmp);
        }
    }

    static final class AuthPermissionHolder {
        static final AuthPermission DO_AS_PERMISSION = new AuthPermission("doAs");
        static final AuthPermission DO_AS_PRIVILEGED_PERMISSION = new AuthPermission("doAsPrivileged");
        static final AuthPermission SET_READ_ONLY_PERMISSION = new AuthPermission("setReadOnly");
        static final AuthPermission GET_SUBJECT_PERMISSION = new AuthPermission("getSubject");
        static final AuthPermission MODIFY_PRINCIPALS_PERMISSION = new AuthPermission("modifyPrincipals");
        static final AuthPermission MODIFY_PUBLIC_CREDENTIALS_PERMISSION = new AuthPermission("modifyPublicCredentials");
        static final AuthPermission MODIFY_PRIVATE_CREDENTIALS_PERMISSION = new AuthPermission("modifyPrivateCredentials");

        AuthPermissionHolder() {
        }
    }

    private class ClassSet<T>
    extends AbstractSet<T> {
        private final int which;
        private final Class<T> c;
        private final Set<T> set;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ClassSet(int which, Class<T> c) {
            this.which = which;
            this.c = c;
            this.set = new HashSet<T>();
            switch (which) {
                case 1: {
                    Set<Principal> set = Subject.this.principals;
                    synchronized (set) {
                        this.populateSet();
                        break;
                    }
                }
                case 2: {
                    Set<Object> set = Subject.this.pubCredentials;
                    synchronized (set) {
                        this.populateSet();
                        break;
                    }
                }
                default: {
                    Set<Object> set = Subject.this.privCredentials;
                    synchronized (set) {
                        this.populateSet();
                        break;
                    }
                }
            }
        }

        private void populateSet() {
            final Iterator<Object> iterator = switch (this.which) {
                case 1 -> Subject.this.principals.iterator();
                case 2 -> Subject.this.pubCredentials.iterator();
                default -> Subject.this.privCredentials.iterator();
            };
            while (iterator.hasNext()) {
                Object next = this.which == 3 ? AccessController.doPrivileged(new PrivilegedAction<Object>(this){

                    @Override
                    public Object run() {
                        return iterator.next();
                    }
                }) : iterator.next();
                if (!this.c.isAssignableFrom(next.getClass())) continue;
                if (this.which != 3) {
                    this.set.add(next);
                    continue;
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(new PrivateCredentialPermission(next.getClass().getName(), Subject.this.getPrincipals()));
                }
                this.set.add(next);
            }
        }

        @Override
        public int size() {
            return this.set.size();
        }

        @Override
        public Iterator<T> iterator() {
            return this.set.iterator();
        }

        @Override
        public boolean add(T o) {
            if (!this.c.isAssignableFrom(o.getClass())) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.class"));
                Object[] source = new Object[]{this.c.toString()};
                throw new SecurityException(form.format(source));
            }
            return this.set.add(o);
        }
    }
}

