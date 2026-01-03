/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import jdk.internal.reflect.ReflectionFactory;

final class PublicMethods {
    private final Map<Key, MethodList> map = new LinkedHashMap<Key, MethodList>();
    private int methodCount;

    PublicMethods() {
    }

    void merge(Method method) {
        Key key = new Key(method);
        MethodList existing = this.map.get(key);
        int xLen = existing == null ? 0 : existing.length();
        MethodList merged = MethodList.merge(existing, method);
        this.methodCount += merged.length() - xLen;
        if (merged != existing) {
            this.map.put(key, merged);
        }
    }

    Method[] toArray() {
        Method[] array = new Method[this.methodCount];
        int i = 0;
        for (MethodList ml : this.map.values()) {
            while (ml != null) {
                array[i++] = ml.method;
                ml = ml.next;
            }
        }
        return array;
    }

    private static final class Key {
        private static final ReflectionFactory reflectionFactory = AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
        private final String name;
        private final Class<?>[] ptypes;

        Key(Method method) {
            this.name = method.getName();
            this.ptypes = reflectionFactory.getExecutableSharedParameterTypes(method);
        }

        static boolean matches(Method method, String name, Class<?>[] ptypes) {
            return method.getName().equals(name) && Arrays.equals(reflectionFactory.getExecutableSharedParameterTypes(method), ptypes);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) return false;
            Key that = (Key)o;
            if (this.name != that.name) return false;
            if (!Arrays.equals(this.ptypes, that.ptypes)) return false;
            return true;
        }

        public int hashCode() {
            return System.identityHashCode(this.name) + 31 * Arrays.hashCode(this.ptypes);
        }
    }

    static final class MethodList {
        Method method;
        MethodList next;

        private MethodList(Method method) {
            this.method = method;
        }

        static MethodList filter(Method[] methods, String name, Class<?>[] ptypes, boolean includeStatic) {
            MethodList head = null;
            MethodList tail = null;
            for (Method method : methods) {
                if (!includeStatic && Modifier.isStatic(method.getModifiers()) || !Key.matches(method, name, ptypes)) continue;
                if (tail == null) {
                    head = tail = new MethodList(method);
                    continue;
                }
                tail = tail.next = new MethodList(method);
            }
            return head;
        }

        static MethodList merge(MethodList head, MethodList methodList) {
            MethodList ml = methodList;
            while (ml != null) {
                head = MethodList.merge(head, ml.method);
                ml = ml.next;
            }
            return head;
        }

        private static MethodList merge(MethodList head, Method method) {
            Class<?> dclass = method.getDeclaringClass();
            Class<?> rtype = method.getReturnType();
            MethodList prev = null;
            MethodList l = head;
            while (l != null) {
                Method xmethod = l.method;
                if (rtype == xmethod.getReturnType()) {
                    Class<?> xdclass = xmethod.getDeclaringClass();
                    if (dclass.isInterface() == xdclass.isInterface()) {
                        if (dclass.isAssignableFrom(xdclass)) {
                            return head;
                        }
                        if (xdclass.isAssignableFrom(dclass)) {
                            if (prev != null) {
                                prev.next = l.next;
                            } else {
                                head = l.next;
                            }
                        } else {
                            prev = l;
                        }
                    } else {
                        if (dclass.isInterface()) {
                            return head;
                        }
                        if (prev != null) {
                            prev.next = l.next;
                        } else {
                            head = l.next;
                        }
                    }
                } else {
                    prev = l;
                }
                l = l.next;
            }
            if (prev == null) {
                head = new MethodList(method);
            } else {
                prev.next = new MethodList(method);
            }
            return head;
        }

        private int length() {
            int len = 1;
            MethodList ml = this.next;
            while (ml != null) {
                ++len;
                ml = ml.next;
            }
            return len;
        }

        Method getMostSpecific() {
            Method m = this.method;
            Class<?> rt = m.getReturnType();
            MethodList ml = this.next;
            while (ml != null) {
                Method m2 = ml.method;
                Class<?> rt2 = m2.getReturnType();
                if (rt2 != rt && rt.isAssignableFrom(rt2)) {
                    m = m2;
                    rt = rt2;
                }
                ml = ml.next;
            }
            return m;
        }
    }
}

