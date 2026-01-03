/*
 * Decompiled with CFR 0.152.
 */
package java.lang.reflect;

import java.io.IOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.ClassFileFormatVersion;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import sun.invoke.util.Wrapper;
import sun.security.action.GetBooleanAction;

final class ProxyGenerator
extends ClassWriter {
    private static final int CLASSFILE_VERSION = ClassFileFormatVersion.latest().major();
    private static final String JL_CLASS = "java/lang/Class";
    private static final String JL_OBJECT = "java/lang/Object";
    private static final String JL_THROWABLE = "java/lang/Throwable";
    private static final String JL_CLASS_NOT_FOUND_EX = "java/lang/ClassNotFoundException";
    private static final String JL_ILLEGAL_ACCESS_EX = "java/lang/IllegalAccessException";
    private static final String JL_NO_CLASS_DEF_FOUND_ERROR = "java/lang/NoClassDefFoundError";
    private static final String JL_NO_SUCH_METHOD_EX = "java/lang/NoSuchMethodException";
    private static final String JL_NO_SUCH_METHOD_ERROR = "java/lang/NoSuchMethodError";
    private static final String JLI_LOOKUP = "java/lang/invoke/MethodHandles$Lookup";
    private static final String JLI_METHODHANDLES = "java/lang/invoke/MethodHandles";
    private static final String JLR_INVOCATION_HANDLER = "java/lang/reflect/InvocationHandler";
    private static final String JLR_PROXY = "java/lang/reflect/Proxy";
    private static final String JLR_UNDECLARED_THROWABLE_EX = "java/lang/reflect/UndeclaredThrowableException";
    private static final String LJL_CLASS = "Ljava/lang/Class;";
    private static final String LJL_CLASSLOADER = "Ljava/lang/ClassLoader;";
    private static final String LJLR_METHOD = "Ljava/lang/reflect/Method;";
    private static final String LJLR_INVOCATION_HANDLER = "Ljava/lang/reflect/InvocationHandler;";
    private static final String MJLR_INVOCATIONHANDLER = "(Ljava/lang/reflect/InvocationHandler;)V";
    private static final String NAME_CTOR = "<init>";
    private static final String NAME_CLINIT = "<clinit>";
    private static final String NAME_LOOKUP_ACCESSOR = "proxyClassLookup";
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final String handlerFieldName = "h";
    private static final boolean saveGeneratedFiles = AccessController.doPrivileged(new GetBooleanAction("jdk.proxy.ProxyGenerator.saveGeneratedFiles"));
    private static final ProxyMethod hashCodeMethod;
    private static final ProxyMethod equalsMethod;
    private static final ProxyMethod toStringMethod;
    private final ClassLoader loader;
    private final String className;
    private final List<Class<?>> interfaces;
    private final int accessFlags;
    private final Map<String, List<ProxyMethod>> proxyMethods = new LinkedHashMap<String, List<ProxyMethod>>();
    private int proxyMethodCount = 3;

    private ProxyGenerator(ClassLoader loader, String className, List<Class<?>> interfaces, int accessFlags) {
        super(2);
        this.loader = loader;
        this.className = className;
        this.interfaces = interfaces;
        this.accessFlags = accessFlags;
    }

    static byte[] generateProxyClass(ClassLoader loader, final String name, List<Class<?>> interfaces, int accessFlags) {
        ProxyGenerator gen = new ProxyGenerator(loader, name, interfaces, accessFlags);
        final byte[] classFile = gen.generateClassFile();
        if (saveGeneratedFiles) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    try {
                        Path path;
                        int i = name.lastIndexOf(46);
                        if (i > 0) {
                            Path dir = Path.of(ProxyGenerator.dotToSlash(name.substring(0, i)), new String[0]);
                            Files.createDirectories(dir, new FileAttribute[0]);
                            path = dir.resolve(name.substring(i + 1) + ".class");
                        } else {
                            path = Path.of(name + ".class", new String[0]);
                        }
                        Files.write(path, classFile, new OpenOption[0]);
                        return null;
                    }
                    catch (IOException e) {
                        throw new InternalError("I/O exception saving generated file: " + e);
                    }
                }
            });
        }
        return classFile;
    }

    private static String[] typeNames(List<Class<?>> classes) {
        if (classes == null || classes.size() == 0) {
            return null;
        }
        int size = classes.size();
        String[] ifaces = new String[size];
        for (int i = 0; i < size; ++i) {
            ifaces[i] = ProxyGenerator.dotToSlash(classes.get(i).getName());
        }
        return ifaces;
    }

    private static void checkReturnTypes(List<ProxyMethod> methods) {
        if (methods.size() < 2) {
            return;
        }
        ArrayList uncoveredReturnTypes = new ArrayList(1);
        block0: for (ProxyMethod pm : methods) {
            Class<?> newReturnType = pm.returnType;
            if (newReturnType.isPrimitive()) {
                throw new IllegalArgumentException("methods with same signature " + pm.shortSignature + " but incompatible return types: " + newReturnType.getName() + " and others");
            }
            boolean added = false;
            ListIterator<Class<?>> liter = uncoveredReturnTypes.listIterator();
            while (liter.hasNext()) {
                Class uncoveredReturnType = (Class)liter.next();
                if (newReturnType.isAssignableFrom(uncoveredReturnType)) {
                    assert (!added);
                    continue block0;
                }
                if (!uncoveredReturnType.isAssignableFrom(newReturnType)) continue;
                if (!added) {
                    liter.set(newReturnType);
                    added = true;
                    continue;
                }
                liter.remove();
            }
            if (added) continue;
            uncoveredReturnTypes.add(newReturnType);
        }
        if (uncoveredReturnTypes.size() > 1) {
            ProxyMethod pm = methods.get(0);
            throw new IllegalArgumentException("methods with same signature " + pm.shortSignature + " but incompatible return types: " + uncoveredReturnTypes);
        }
    }

    private static List<Class<?>> computeUniqueCatchList(Class<?>[] exceptions) {
        ArrayList uniqueList = new ArrayList();
        uniqueList.add(Error.class);
        uniqueList.add(RuntimeException.class);
        block0: for (Class<Throwable> clazz : exceptions) {
            if (clazz.isAssignableFrom(Throwable.class)) {
                uniqueList.clear();
                break;
            }
            if (!Throwable.class.isAssignableFrom(clazz)) continue;
            int j = 0;
            while (j < uniqueList.size()) {
                Class ex2 = (Class)uniqueList.get(j);
                if (ex2.isAssignableFrom(clazz)) continue block0;
                if (clazz.isAssignableFrom(ex2)) {
                    uniqueList.remove(j);
                    continue;
                }
                ++j;
            }
            uniqueList.add(clazz);
        }
        return uniqueList;
    }

    private static String dotToSlash(String name) {
        return name.replace('.', '/');
    }

    private static int getWordsPerType(Class<?> type) {
        if (type == Long.TYPE || type == Double.TYPE) {
            return 2;
        }
        return 1;
    }

    private static void collectCompatibleTypes(Class<?>[] from, Class<?>[] with, List<Class<?>> list) {
        block0: for (Class<?> fc : from) {
            if (list.contains(fc)) continue;
            for (Class<?> wc : with) {
                if (!wc.isAssignableFrom(fc)) continue;
                list.add(fc);
                continue block0;
            }
        }
    }

    @Override
    protected ClassLoader getClassLoader() {
        return this.loader;
    }

    private byte[] generateClassFile() {
        this.visit(CLASSFILE_VERSION, this.accessFlags, ProxyGenerator.dotToSlash(this.className), null, JLR_PROXY, ProxyGenerator.typeNames(this.interfaces));
        this.addProxyMethod(hashCodeMethod);
        this.addProxyMethod(equalsMethod);
        this.addProxyMethod(toStringMethod);
        for (Class<?> clazz : this.interfaces) {
            for (Method m : clazz.getMethods()) {
                if (Modifier.isStatic(m.getModifiers())) continue;
                this.addProxyMethod(m, clazz);
            }
        }
        for (List list : this.proxyMethods.values()) {
            ProxyGenerator.checkReturnTypes(list);
        }
        this.generateConstructor();
        for (List list : this.proxyMethods.values()) {
            for (ProxyMethod pm : list) {
                this.visitField(26, pm.methodFieldName, LJLR_METHOD, null, null);
                pm.generateMethod(this, this.className);
            }
        }
        this.generateStaticInitializer();
        this.generateLookupAccessor();
        return this.toByteArray();
    }

    private void addProxyMethod(Method m, Class<?> fromClass) {
        Class<?> returnType = m.getReturnType();
        Class<?>[] exceptionTypes = m.getSharedExceptionTypes();
        String sig = m.toShortSignature();
        List sigmethods = this.proxyMethods.computeIfAbsent(sig, f -> new ArrayList(3));
        for (ProxyMethod pm : sigmethods) {
            if (returnType != pm.returnType) continue;
            ArrayList legalExceptions = new ArrayList();
            ProxyGenerator.collectCompatibleTypes(exceptionTypes, pm.exceptionTypes, legalExceptions);
            ProxyGenerator.collectCompatibleTypes(pm.exceptionTypes, exceptionTypes, legalExceptions);
            pm.exceptionTypes = legalExceptions.toArray(EMPTY_CLASS_ARRAY);
            return;
        }
        sigmethods.add(new ProxyMethod(m, sig, m.getSharedParameterTypes(), returnType, exceptionTypes, fromClass, "m" + this.proxyMethodCount++));
    }

    private void addProxyMethod(ProxyMethod pm) {
        String sig = pm.shortSignature;
        List sigmethods = this.proxyMethods.computeIfAbsent(sig, f -> new ArrayList(3));
        sigmethods.add(pm);
    }

    private void generateConstructor() {
        MethodVisitor ctor = this.visitMethod(1, NAME_CTOR, MJLR_INVOCATIONHANDLER, null, null);
        ctor.visitParameter(null, 0);
        ctor.visitCode();
        ctor.visitVarInsn(25, 0);
        ctor.visitVarInsn(25, 1);
        ctor.visitMethodInsn(183, JLR_PROXY, NAME_CTOR, MJLR_INVOCATIONHANDLER, false);
        ctor.visitInsn(177);
        ctor.visitMaxs(-1, -1);
        ctor.visitEnd();
    }

    private void generateStaticInitializer() {
        MethodVisitor mv = this.visitMethod(8, NAME_CLINIT, "()V", null, null);
        mv.visitCode();
        Label L_startBlock = new Label();
        Label L_endBlock = new Label();
        Label L_NoMethodHandler = new Label();
        Label L_NoClassHandler = new Label();
        mv.visitTryCatchBlock(L_startBlock, L_endBlock, L_NoMethodHandler, JL_NO_SUCH_METHOD_EX);
        mv.visitTryCatchBlock(L_startBlock, L_endBlock, L_NoClassHandler, JL_CLASS_NOT_FOUND_EX);
        mv.visitLdcInsn(Type.getObjectType(ProxyGenerator.dotToSlash(this.className)));
        mv.visitMethodInsn(182, JL_CLASS, "getClassLoader", "()Ljava/lang/ClassLoader;", false);
        mv.visitVarInsn(58, 0);
        mv.visitLabel(L_startBlock);
        for (List<ProxyMethod> sigmethods : this.proxyMethods.values()) {
            for (ProxyMethod pm : sigmethods) {
                pm.codeFieldInitialization(mv, this.className);
            }
        }
        mv.visitInsn(177);
        mv.visitLabel(L_endBlock);
        mv.visitLabel(L_NoMethodHandler);
        mv.visitVarInsn(58, 1);
        mv.visitTypeInsn(187, JL_NO_SUCH_METHOD_ERROR);
        mv.visitInsn(89);
        mv.visitVarInsn(25, 1);
        mv.visitMethodInsn(182, JL_THROWABLE, "getMessage", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(183, JL_NO_SUCH_METHOD_ERROR, NAME_CTOR, "(Ljava/lang/String;)V", false);
        mv.visitInsn(191);
        mv.visitLabel(L_NoClassHandler);
        mv.visitVarInsn(58, 1);
        mv.visitTypeInsn(187, JL_NO_CLASS_DEF_FOUND_ERROR);
        mv.visitInsn(89);
        mv.visitVarInsn(25, 1);
        mv.visitMethodInsn(182, JL_THROWABLE, "getMessage", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(183, JL_NO_CLASS_DEF_FOUND_ERROR, NAME_CTOR, "(Ljava/lang/String;)V", false);
        mv.visitInsn(191);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateLookupAccessor() {
        MethodVisitor mv = this.visitMethod(10, NAME_LOOKUP_ACCESSOR, "(Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/MethodHandles$Lookup;", null, new String[]{JL_ILLEGAL_ACCESS_EX});
        mv.visitCode();
        Label L_illegalAccess = new Label();
        mv.visitVarInsn(25, 0);
        mv.visitMethodInsn(182, JLI_LOOKUP, "lookupClass", "()Ljava/lang/Class;", false);
        mv.visitLdcInsn(Type.getType(Proxy.class));
        mv.visitJumpInsn(166, L_illegalAccess);
        mv.visitVarInsn(25, 0);
        mv.visitMethodInsn(182, JLI_LOOKUP, "hasFullPrivilegeAccess", "()Z", false);
        mv.visitJumpInsn(153, L_illegalAccess);
        mv.visitMethodInsn(184, JLI_METHODHANDLES, "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
        mv.visitInsn(176);
        mv.visitLabel(L_illegalAccess);
        mv.visitTypeInsn(187, JL_ILLEGAL_ACCESS_EX);
        mv.visitInsn(89);
        mv.visitVarInsn(25, 0);
        mv.visitMethodInsn(182, JLI_LOOKUP, "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(183, JL_ILLEGAL_ACCESS_EX, NAME_CTOR, "(Ljava/lang/String;)V", false);
        mv.visitInsn(191);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    static {
        try {
            hashCodeMethod = new ProxyMethod(Object.class.getMethod("hashCode", new Class[0]), "m0");
            equalsMethod = new ProxyMethod(Object.class.getMethod("equals", Object.class), "m1");
            toStringMethod = new ProxyMethod(Object.class.getMethod("toString", new Class[0]), "m2");
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    private static class ProxyMethod {
        private final Method method;
        private final String shortSignature;
        private final Class<?> fromClass;
        private final Class<?>[] parameterTypes;
        private final Class<?> returnType;
        private final String methodFieldName;
        private Class<?>[] exceptionTypes;

        private ProxyMethod(Method method, String sig, Class<?>[] parameterTypes, Class<?> returnType, Class<?>[] exceptionTypes, Class<?> fromClass, String methodFieldName) {
            this.method = method;
            this.shortSignature = sig;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.exceptionTypes = exceptionTypes;
            this.fromClass = fromClass;
            this.methodFieldName = methodFieldName;
        }

        private ProxyMethod(Method method, String methodFieldName) {
            this(method, method.toShortSignature(), method.getSharedParameterTypes(), method.getReturnType(), method.getSharedExceptionTypes(), method.getDeclaringClass(), methodFieldName);
        }

        private void generateMethod(ClassWriter cw, String className) {
            MethodType mt = MethodType.methodType(this.returnType, this.parameterTypes);
            String desc = mt.toMethodDescriptorString();
            int accessFlags = 17;
            if (this.method.isVarArgs()) {
                accessFlags |= 0x80;
            }
            MethodVisitor mv = cw.visitMethod(accessFlags, this.method.getName(), desc, null, ProxyGenerator.typeNames(Arrays.asList(this.exceptionTypes)));
            int[] parameterSlot = new int[this.parameterTypes.length];
            int nextSlot = 1;
            for (int i = 0; i < parameterSlot.length; ++i) {
                parameterSlot[i] = nextSlot;
                nextSlot += ProxyGenerator.getWordsPerType(this.parameterTypes[i]);
            }
            mv.visitCode();
            Label L_startBlock = new Label();
            Label L_endBlock = new Label();
            Label L_RuntimeHandler = new Label();
            Label L_ThrowableHandler = new Label();
            List<Class<?>> catchList = ProxyGenerator.computeUniqueCatchList(this.exceptionTypes);
            if (catchList.size() > 0) {
                for (Class<?> ex : catchList) {
                    mv.visitTryCatchBlock(L_startBlock, L_endBlock, L_RuntimeHandler, ProxyGenerator.dotToSlash(ex.getName()));
                }
                mv.visitTryCatchBlock(L_startBlock, L_endBlock, L_ThrowableHandler, ProxyGenerator.JL_THROWABLE);
            }
            mv.visitLabel(L_startBlock);
            mv.visitVarInsn(25, 0);
            mv.visitFieldInsn(180, ProxyGenerator.JLR_PROXY, ProxyGenerator.handlerFieldName, ProxyGenerator.LJLR_INVOCATION_HANDLER);
            mv.visitVarInsn(25, 0);
            mv.visitFieldInsn(178, ProxyGenerator.dotToSlash(className), this.methodFieldName, ProxyGenerator.LJLR_METHOD);
            if (this.parameterTypes.length > 0) {
                this.emitIconstInsn(mv, this.parameterTypes.length);
                mv.visitTypeInsn(189, ProxyGenerator.JL_OBJECT);
                for (int i = 0; i < this.parameterTypes.length; ++i) {
                    mv.visitInsn(89);
                    this.emitIconstInsn(mv, i);
                    this.codeWrapArgument(mv, this.parameterTypes[i], parameterSlot[i]);
                    mv.visitInsn(83);
                }
            } else {
                mv.visitInsn(1);
            }
            mv.visitMethodInsn(185, ProxyGenerator.JLR_INVOCATION_HANDLER, "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
            if (this.returnType == Void.TYPE) {
                mv.visitInsn(87);
                mv.visitInsn(177);
            } else {
                this.codeUnwrapReturnValue(mv, this.returnType);
            }
            mv.visitLabel(L_endBlock);
            mv.visitLabel(L_RuntimeHandler);
            mv.visitInsn(191);
            mv.visitLabel(L_ThrowableHandler);
            mv.visitVarInsn(58, 1);
            mv.visitTypeInsn(187, ProxyGenerator.JLR_UNDECLARED_THROWABLE_EX);
            mv.visitInsn(89);
            mv.visitVarInsn(25, 1);
            mv.visitMethodInsn(183, ProxyGenerator.JLR_UNDECLARED_THROWABLE_EX, ProxyGenerator.NAME_CTOR, "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(191);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        private void codeWrapArgument(MethodVisitor mv, Class<?> type, int slot) {
            if (type.isPrimitive()) {
                PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);
                mv.visitVarInsn(prim.loadOpcode, slot);
                mv.visitMethodInsn(184, prim.wrapperClassName, "valueOf", prim.wrapperValueOfDesc, false);
            } else {
                mv.visitVarInsn(25, slot);
            }
        }

        private void codeUnwrapReturnValue(MethodVisitor mv, Class<?> type) {
            if (type.isPrimitive()) {
                PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);
                mv.visitTypeInsn(192, prim.wrapperClassName);
                mv.visitMethodInsn(182, prim.wrapperClassName, prim.unwrapMethodName, prim.unwrapMethodDesc, false);
                mv.visitInsn(prim.returnOpcode);
            } else {
                mv.visitTypeInsn(192, ProxyGenerator.dotToSlash(type.getName()));
                mv.visitInsn(176);
            }
        }

        private void codeFieldInitialization(MethodVisitor mv, String className) {
            this.codeClassForName(mv, this.fromClass);
            mv.visitLdcInsn(this.method.getName());
            this.emitIconstInsn(mv, this.parameterTypes.length);
            mv.visitTypeInsn(189, ProxyGenerator.JL_CLASS);
            for (int i = 0; i < this.parameterTypes.length; ++i) {
                mv.visitInsn(89);
                this.emitIconstInsn(mv, i);
                if (this.parameterTypes[i].isPrimitive()) {
                    PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(this.parameterTypes[i]);
                    mv.visitFieldInsn(178, prim.wrapperClassName, "TYPE", ProxyGenerator.LJL_CLASS);
                } else {
                    this.codeClassForName(mv, this.parameterTypes[i]);
                }
                mv.visitInsn(83);
            }
            mv.visitMethodInsn(182, ProxyGenerator.JL_CLASS, "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            mv.visitFieldInsn(179, ProxyGenerator.dotToSlash(className), this.methodFieldName, ProxyGenerator.LJLR_METHOD);
        }

        private void codeClassForName(MethodVisitor mv, Class<?> cl) {
            mv.visitLdcInsn(cl.getName());
            mv.visitInsn(3);
            mv.visitVarInsn(25, 0);
            mv.visitMethodInsn(184, ProxyGenerator.JL_CLASS, "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false);
        }

        private void emitIconstInsn(MethodVisitor mv, int cst) {
            if (cst >= -1 && cst <= 5) {
                mv.visitInsn(3 + cst);
            } else if (cst >= -128 && cst <= 127) {
                mv.visitIntInsn(16, cst);
            } else if (cst >= Short.MIN_VALUE && cst <= Short.MAX_VALUE) {
                mv.visitIntInsn(17, cst);
            } else {
                mv.visitLdcInsn(cst);
            }
        }

        public String toString() {
            return this.method.toShortString();
        }
    }

    private static final class PrimitiveTypeInfo
    extends Enum<PrimitiveTypeInfo> {
        public static final /* enum */ PrimitiveTypeInfo BYTE = new PrimitiveTypeInfo(Byte.TYPE, 21, 172);
        public static final /* enum */ PrimitiveTypeInfo CHAR = new PrimitiveTypeInfo(Character.TYPE, 21, 172);
        public static final /* enum */ PrimitiveTypeInfo DOUBLE = new PrimitiveTypeInfo(Double.TYPE, 24, 175);
        public static final /* enum */ PrimitiveTypeInfo FLOAT = new PrimitiveTypeInfo(Float.TYPE, 23, 174);
        public static final /* enum */ PrimitiveTypeInfo INT = new PrimitiveTypeInfo(Integer.TYPE, 21, 172);
        public static final /* enum */ PrimitiveTypeInfo LONG = new PrimitiveTypeInfo(Long.TYPE, 22, 173);
        public static final /* enum */ PrimitiveTypeInfo SHORT = new PrimitiveTypeInfo(Short.TYPE, 21, 172);
        public static final /* enum */ PrimitiveTypeInfo BOOLEAN = new PrimitiveTypeInfo(Boolean.TYPE, 21, 172);
        private final String wrapperClassName;
        private final String wrapperValueOfDesc;
        private final String unwrapMethodName;
        private final String unwrapMethodDesc;
        private final int loadOpcode;
        private final int returnOpcode;
        private static final /* synthetic */ PrimitiveTypeInfo[] $VALUES;

        public static PrimitiveTypeInfo[] values() {
            return (PrimitiveTypeInfo[])$VALUES.clone();
        }

        public static PrimitiveTypeInfo valueOf(String name) {
            return Enum.valueOf(PrimitiveTypeInfo.class, name);
        }

        private PrimitiveTypeInfo(Class<?> primitiveClass, int loadOpcode, int returnOpcode) {
            assert (primitiveClass.isPrimitive());
            assert (returnOpcode - 172 == loadOpcode - 21);
            Wrapper wrapper = Wrapper.forPrimitiveType(primitiveClass);
            String baseTypeString = wrapper.basicTypeString();
            Class<?> wrapperType = wrapper.wrapperType();
            this.wrapperClassName = ProxyGenerator.dotToSlash(wrapperType.getName());
            this.wrapperValueOfDesc = "(" + baseTypeString + ")" + wrapperType.descriptorString();
            this.unwrapMethodName = primitiveClass.getName() + "Value";
            this.unwrapMethodDesc = "()" + baseTypeString;
            this.loadOpcode = loadOpcode;
            this.returnOpcode = returnOpcode;
        }

        public static PrimitiveTypeInfo get(Class<?> cl) {
            if (cl == Integer.TYPE) {
                return INT;
            }
            if (cl == Long.TYPE) {
                return LONG;
            }
            if (cl == Boolean.TYPE) {
                return BOOLEAN;
            }
            if (cl == Short.TYPE) {
                return SHORT;
            }
            if (cl == Byte.TYPE) {
                return BYTE;
            }
            if (cl == Character.TYPE) {
                return CHAR;
            }
            if (cl == Float.TYPE) {
                return FLOAT;
            }
            if (cl == Double.TYPE) {
                return DOUBLE;
            }
            throw new AssertionError(cl);
        }

        private static /* synthetic */ PrimitiveTypeInfo[] $values() {
            return new PrimitiveTypeInfo[]{BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, BOOLEAN};
        }

        static {
            $VALUES = PrimitiveTypeInfo.$values();
        }
    }
}

