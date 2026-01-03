/*
 * Decompiled with CFR 0.152.
 */
package java.lang.invoke;

import java.lang.invoke.BoundMethodHandle;
import java.lang.invoke.LambdaForm;
import java.lang.invoke.MethodType;
import jdk.internal.vm.annotation.Stable;

final class BoundMethodHandle$Species_LI
extends BoundMethodHandle {
    @Stable
    static BoundMethodHandle.SpeciesData BMH_SPECIES;
    final Object argL0;
    final int argI1;

    @Override
    final BoundMethodHandle.SpeciesData speciesData() {
        return BMH_SPECIES;
    }

    private BoundMethodHandle$Species_LI(MethodType methodType, LambdaForm lambdaForm, Object object, int n) {
        super(methodType, lambdaForm);
        this.argL0 = object;
        this.argI1 = n;
    }

    static BoundMethodHandle make(MethodType methodType, LambdaForm lambdaForm, Object object, int n) {
        return new BoundMethodHandle$Species_LI(methodType, lambdaForm, object, n);
    }

    @Override
    final BoundMethodHandle copyWithExtendL(MethodType methodType, LambdaForm lambdaForm, Object object) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(0).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1, object);
    }

    @Override
    final BoundMethodHandle copyWithExtendI(MethodType methodType, LambdaForm lambdaForm, int n) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(1).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1, n);
    }

    @Override
    final BoundMethodHandle copyWithExtendJ(MethodType methodType, LambdaForm lambdaForm, long l) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(2).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1, l);
    }

    @Override
    final BoundMethodHandle copyWithExtendF(MethodType methodType, LambdaForm lambdaForm, float f) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(3).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1, f);
    }

    @Override
    final BoundMethodHandle copyWithExtendD(MethodType methodType, LambdaForm lambdaForm, double d) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(4).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1, d);
    }

    @Override
    final BoundMethodHandle copyWith(MethodType methodType, LambdaForm lambdaForm) throws Throwable {
        return (BoundMethodHandle)BMH_SPECIES.transformHelper(5).invokeBasic(methodType, lambdaForm, this.argL0, this.argI1);
    }
}

