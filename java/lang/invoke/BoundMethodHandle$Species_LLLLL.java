/*
 * Decompiled with CFR 0.152.
 */
package java.lang.invoke;

import java.lang.invoke.BoundMethodHandle;
import java.lang.invoke.LambdaForm;
import java.lang.invoke.MethodType;
import jdk.internal.vm.annotation.Stable;

final class BoundMethodHandle$Species_LLLLL
extends BoundMethodHandle {
    @Stable
    static BoundMethodHandle.SpeciesData BMH_SPECIES;
    final Object argL0;
    final Object argL1;
    final Object argL2;
    final Object argL3;
    final Object argL4;

    @Override
    final BoundMethodHandle.SpeciesData speciesData() {
        return BMH_SPECIES;
    }

    private BoundMethodHandle$Species_LLLLL(MethodType methodType, LambdaForm lambdaForm, Object object, Object object2, Object object3, Object object4, Object object5) {
        super(methodType, lambdaForm);
        this.argL0 = object;
        this.argL1 = object2;
        this.argL2 = object3;
        this.argL3 = object4;
        this.argL4 = object5;
    }

    static BoundMethodHandle make(MethodType methodType, LambdaForm lambdaForm, Object object, Object object2, Object object3, Object object4, Object object5) {
        return new BoundMethodHandle$Species_LLLLL(methodType, lambdaForm, object, object2, object3, object4, object5);
    }

    @Override
    final BoundMethodHandle copyWithExtendL(MethodType methodType, LambdaForm lambdaForm, Object object) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(0).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4, object);
    }

    @Override
    final BoundMethodHandle copyWithExtendI(MethodType methodType, LambdaForm lambdaForm, int n) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(1).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4, n);
    }

    @Override
    final BoundMethodHandle copyWithExtendJ(MethodType methodType, LambdaForm lambdaForm, long l) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(2).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4, l);
    }

    @Override
    final BoundMethodHandle copyWithExtendF(MethodType methodType, LambdaForm lambdaForm, float f) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(3).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4, f);
    }

    @Override
    final BoundMethodHandle copyWithExtendD(MethodType methodType, LambdaForm lambdaForm, double d) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(4).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4, d);
    }

    @Override
    final BoundMethodHandle copyWith(MethodType methodType, LambdaForm lambdaForm) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(5).invokeBasic(methodType, lambdaForm, this.argL0, this.argL1, this.argL2, this.argL3, this.argL4);
    }
}

