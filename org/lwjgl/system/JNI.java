/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Library;

public final class JNI {
    private JNI() {
    }

    public static native byte invokePB(long var0, long var2);

    public static native short invokeC(long var0);

    public static native short invokeC(int var0, long var1);

    public static native short invokePC(long var0, long var2);

    public static native short invokeCC(int var0, short var1, long var2);

    public static native short invokeCC(short var0, boolean var1, long var2);

    public static native short invokePC(long var0, int var2, long var3);

    public static native short invokeJC(int var0, int var1, long var2, long var4);

    public static native short invokeCUC(short var0, byte var1, long var2);

    public static native short invokePCC(long var0, short var2, long var3);

    public static native short invokeCCC(short var0, short var1, boolean var2, long var3);

    public static native short invokePCC(int var0, long var1, short var3, long var4);

    public static native short invokePCC(long var0, int var2, short var3, long var4);

    public static native short invokeUPC(byte var0, long var1, boolean var3, long var4);

    public static native short invokeCJC(int var0, boolean var1, short var2, int var3, long var4, long var6);

    public static native short invokeCPCC(short var0, long var1, short var3, long var4);

    public static native short invokePPCC(long var0, long var2, short var4, long var5);

    public static native short invokeCCJC(short var0, short var1, int var2, long var3, long var5);

    public static native short invokePCCC(long var0, short var2, short var3, int var4, int var5, long var6);

    public static native short invokeCCCCC(short var0, short var1, short var2, short var3, long var4);

    public static native short invokePJUPC(long var0, long var2, byte var4, long var5, long var7);

    public static native short invokeCCJPC(short var0, boolean var1, short var2, int var3, long var4, long var6, long var8);

    public static native short invokePCCCCC(long var0, short var2, short var3, short var4, short var5, long var6);

    public static native short invokeCCCJPC(short var0, short var1, short var2, boolean var3, int var4, long var5, long var7, long var9);

    public static native short invokeCCCJPC(short var0, short var1, boolean var2, short var3, int var4, long var5, long var7, long var9);

    public static native double invokeD(long var0);

    public static native double invokeD(int var0, long var1);

    public static native double invokePD(long var0, long var2);

    public static native double invokePD(long var0, int var2, long var3);

    public static native double invokePD(long var0, int var2, int var3, long var4);

    public static native double invokePPD(long var0, long var2, long var4);

    public static native float invokeF(int var0, long var1);

    public static native float invokePF(long var0, long var2);

    public static native float invokePF(float var0, long var1, long var3);

    public static native float invokePF(long var0, int var2, long var3);

    public static native float invokePF(float var0, float var1, long var2, long var4);

    public static native float invokePF(long var0, float var2, float var3, long var4);

    public static native float invokePF(long var0, int var2, int var3, long var4);

    public static native float invokePPF(long var0, long var2, long var4);

    public static native float invokePPF(long var0, int var2, long var3, long var5);

    public static native float invokePPF(long var0, float var2, long var3, int var5, long var6);

    public static native int invokeI(long var0);

    public static native int invokeI(int var0, long var1);

    public static native int invokeI(boolean var0, long var1);

    public static native int invokeI(int var0, int var1, long var2);

    public static native int invokeI(int var0, boolean var1, long var2);

    public static native int invokeI(int var0, int var1, int var2, long var3);

    public static native int invokePI(long var0, long var2);

    public static native int invokeCI(int var0, short var1, long var2);

    public static native int invokePI(int var0, long var1, long var3);

    public static native int invokePI(long var0, int var2, long var3);

    public static native int invokePI(long var0, int var2, int var3, long var4);

    public static native int invokePI(long var0, int var2, boolean var3, long var4);

    public static native int invokePI(long var0, int var2, int var3, int var4, long var5);

    public static native int invokeCPI(short var0, long var1, long var3);

    public static native int invokePCI(long var0, short var2, long var3);

    public static native int invokePJI(long var0, long var2, long var4);

    public static native int invokePNI(long var0, long var2, long var4);

    public static native int invokePPI(long var0, long var2, long var4);

    public static native int invokePJI(long var0, long var2, int var4, long var5);

    public static native int invokePNI(long var0, int var2, long var3, long var5);

    public static native int invokePNI(long var0, long var2, int var4, long var5);

    public static native int invokePPI(int var0, long var1, long var3, long var5);

    public static native int invokePPI(long var0, int var2, long var3, long var5);

    public static native int invokePPI(long var0, long var2, float var4, long var5);

    public static native int invokePPI(long var0, long var2, int var4, long var5);

    public static native int invokePPI(long var0, long var2, boolean var4, long var5);

    public static native int invokePPI(long var0, boolean var2, long var3, long var5);

    public static native int invokePPI(long var0, int var2, int var3, long var4, long var6);

    public static native int invokePPI(long var0, int var2, long var3, int var5, long var6);

    public static native int invokePPI(long var0, int var2, long var3, boolean var5, long var6);

    public static native int invokePPI(long var0, int var2, boolean var3, long var4, long var6);

    public static native int invokePPI(long var0, long var2, int var4, int var5, long var6);

    public static native int invokePPI(long var0, long var2, boolean var4, boolean var5, long var6);

    public static native int invokePPI(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native int invokePPI(long var0, int var2, int var3, long var4, int var6, long var7);

    public static native int invokePPI(long var0, int var2, long var3, int var5, int var6, long var7);

    public static native int invokePPI(int var0, long var1, int var3, long var4, int var6, boolean var7, long var8);

    public static native int invokePPI(long var0, int var2, int var3, int var4, int var5, long var6, long var8);

    public static native int invokePPI(long var0, int var2, int var3, int var4, int var5, long var6, int var8, long var9);

    public static native int invokeCPUI(short var0, long var1, byte var3, long var4);

    public static native int invokePCPI(long var0, short var2, long var3, long var5);

    public static native int invokePNNI(long var0, long var2, long var4, long var6);

    public static native int invokePNPI(long var0, long var2, long var4, long var6);

    public static native int invokePPCI(long var0, long var2, short var4, long var5);

    public static native int invokePPJI(long var0, long var2, long var4, long var6);

    public static native int invokePPNI(long var0, long var2, long var4, long var6);

    public static native int invokePPPI(long var0, long var2, long var4, long var6);

    public static native int invokePNPI(long var0, long var2, int var4, long var5, long var7);

    public static native int invokePNPI(long var0, long var2, long var4, int var6, long var7);

    public static native int invokePPNI(long var0, int var2, long var3, long var5, long var7);

    public static native int invokePPPI(long var0, int var2, long var3, long var5, long var7);

    public static native int invokePPPI(long var0, long var2, int var4, long var5, long var7);

    public static native int invokePPPI(long var0, long var2, long var4, int var6, long var7);

    public static native int invokePNNI(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native int invokePPPI(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native int invokePPPI(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native int invokePPPI(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native int invokePPPI(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native int invokePPPI(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native int invokePPPI(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native int invokePPPI(long var0, int var2, int var3, int var4, long var5, long var7, long var9);

    public static native int invokePPPI(long var0, int var2, long var3, int var5, long var6, int var8, long var9);

    public static native int invokePPPI(long var0, long var2, int var4, long var5, int var7, int var8, long var9);

    public static native int invokePPPI(long var0, long var2, long var4, int var6, boolean var7, float var8, long var9);

    public static native int invokePPPI(long var0, int var2, int var3, int var4, int var5, long var6, long var8, long var10);

    public static native int invokePPPI(long var0, int var2, int var3, int var4, long var5, long var7, int var9, long var10);

    public static native int invokePPPI(long var0, int var2, int var3, long var4, int var6, long var7, int var9, int var10, long var11);

    public static native int invokePNPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int invokePPNNI(long var0, long var2, long var4, long var6, long var8);

    public static native int invokePPNPI(long var0, long var2, long var4, long var6, long var8);

    public static native int invokePPPNI(long var0, long var2, long var4, long var6, long var8);

    public static native int invokePPPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int invokePNNPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int invokePPPNI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int invokePPPPI(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native int invokePPPPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int invokePPPPI(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native int invokePPPPI(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native int invokePPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native int invokePPPPI(long var0, long var2, int var4, int var5, long var6, long var8, long var10);

    public static native int invokePPPPI(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native int invokePPPPI(long var0, long var2, int var4, long var5, long var7, int var9, long var10);

    public static native int invokePPPPI(long var0, long var2, long var4, int var6, int var7, long var8, long var10);

    public static native int invokePPPPI(long var0, long var2, long var4, long var6, int var8, int var9, long var10);

    public static native int invokePPPPI(long var0, int var2, int var3, int var4, long var5, long var7, long var9, long var11);

    public static native int invokePPPPI(long var0, int var2, int var3, long var4, long var6, long var8, int var10, long var11);

    public static native int invokePPPPI(long var0, long var2, long var4, int var6, int var7, int var8, long var9, int var11, long var12);

    public static native int invokePPPPI(long var0, long var2, long var4, long var6, int var8, int var9, int var10, int var11, long var12);

    public static native int invokePPPPI(long var0, int var2, int var3, long var4, int var6, long var7, long var9, int var11, int var12, long var13);

    public static native int invokePNNPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int invokePPNNPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int invokePPPNNI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int invokePPPPNI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int invokePPPPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int invokePJPPNI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPNPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPNPPI(long var0, long var2, long var4, int var6, long var7, long var9, long var11);

    public static native int invokePPPNJI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPPNNI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPPNPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPPPNI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPPPNI(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native int invokePPPPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int invokePPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native int invokePPPPPI(long var0, long var2, long var4, int var6, long var7, long var9, long var11);

    public static native int invokePPPPPI(long var0, long var2, long var4, long var6, long var8, int var10, long var11);

    public static native int invokePNPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12);

    public static native int invokePPPPPI(long var0, long var2, int var4, int var5, long var6, long var8, long var10, long var12);

    public static native int invokePPPPPI(long var0, long var2, int var4, long var5, int var7, long var8, int var10, long var11, long var13);

    public static native int invokePPPPPI(long var0, long var2, long var4, int var6, long var7, int var9, int var10, long var11, long var13);

    public static native int invokePPPPPI(long var0, long var2, long var4, int var6, int var7, int var8, float var9, long var10, long var12, long var14);

    public static native int invokePPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int invokePJJJJPI(long var0, long var2, long var4, long var6, long var8, int var10, long var11, long var13);

    public static native int invokePPNPPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11, long var13);

    public static native int invokePPPPPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11, long var13);

    public static native int invokePPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, int var12, long var13);

    public static native int invokePNNPPPI(long var0, long var2, long var4, int var6, int var7, long var8, long var10, long var12, long var14);

    public static native int invokePPPPPPI(int var0, int var1, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native int invokePPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native int invokePPPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native int invokePPPPPPPI(long var0, long var2, long var4, int var6, long var7, float var9, float var10, long var11, long var13, long var15, long var17);

    public static native int invokePPPPPPPI(long var0, long var2, long var4, int var6, int var7, long var8, long var10, int var12, long var13, int var15, long var16, int var18, long var19);

    public static native int invokePPPPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18);

    public static native long invokeJ(long var0);

    public static native long invokePJ(long var0, long var2);

    public static native long invokePJ(long var0, int var2, long var3);

    public static native long invokePJ(long var0, int var2, int var3, long var4);

    public static native long invokePJJ(long var0, long var2, long var4);

    public static native long invokePPJ(long var0, long var2, long var4);

    public static native long invokePPJ(long var0, long var2, int var4, long var5);

    public static native long invokeNN(long var0, long var2);

    public static native long invokePN(long var0, long var2);

    public static native long invokePN(long var0, int var2, long var3);

    public static native long invokeNNN(long var0, long var2, long var4);

    public static native long invokePPN(long var0, long var2, long var4);

    public static native long invokeNNNN(long var0, long var2, long var4, long var6);

    public static native long invokePNPN(long var0, long var2, long var4, long var6);

    public static native long invokePNPN(long var0, long var2, long var4, int var6, long var7);

    public static native long invokePPNN(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native long invokePNPNN(long var0, long var2, long var4, long var6, long var8);

    public static native long invokePNPNPN(long var0, long var2, int var4, int var5, int var6, int var7, int var8, int var9, int var10, long var11, long var13, long var15, long var17);

    public static native long invokeP(long var0);

    public static native long invokeP(int var0, long var1);

    public static native long invokeP(boolean var0, long var1);

    public static native long invokeP(int var0, int var1, long var2);

    public static native long invokeCP(short var0, long var1);

    public static native long invokePP(long var0, long var2);

    public static native long invokePP(int var0, long var1, long var3);

    public static native long invokePP(long var0, double var2, long var4);

    public static native long invokePP(long var0, int var2, long var3);

    public static native long invokePP(int var0, int var1, long var2, long var4);

    public static native long invokePP(long var0, int var2, int var3, long var4);

    public static native long invokePP(int var0, int var1, int var2, long var3, long var5);

    public static native long invokePP(long var0, int var2, int var3, int var4, long var5);

    public static native long invokePP(long var0, float var2, int var3, float var4, int var5, long var6);

    public static native long invokePP(long var0, int var2, int var3, int var4, int var5, int var6, long var7);

    public static native long invokeCPP(short var0, long var1, long var3);

    public static native long invokePJP(long var0, long var2, long var4);

    public static native long invokePNP(long var0, long var2, long var4);

    public static native long invokePPP(long var0, long var2, long var4);

    public static native long invokePUP(long var0, byte var2, long var3);

    public static native long invokeCPP(int var0, short var1, long var2, long var4);

    public static native long invokePCP(long var0, short var2, boolean var3, long var4);

    public static native long invokePJP(long var0, int var2, long var3, long var5);

    public static native long invokePJP(long var0, long var2, int var4, long var5);

    public static native long invokePPP(int var0, long var1, long var3, long var5);

    public static native long invokePPP(long var0, int var2, long var3, long var5);

    public static native long invokePPP(long var0, long var2, int var4, long var5);

    public static native long invokePPP(long var0, int var2, int var3, long var4, long var6);

    public static native long invokePPP(long var0, int var2, long var3, int var5, long var6);

    public static native long invokePPP(long var0, long var2, int var4, int var5, long var6);

    public static native long invokePPP(long var0, long var2, boolean var4, boolean var5, long var6);

    public static native long invokePPP(long var0, boolean var2, boolean var3, long var4, long var6);

    public static native long invokePPP(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native long invokePPP(int var0, int var1, int var2, int var3, long var4, long var6, long var8);

    public static native long invokePUP(long var0, int var2, byte var3, int var4, boolean var5, boolean var6, long var7);

    public static native long invokePPP(int var0, int var1, int var2, int var3, long var4, int var6, long var7, long var9);

    public static native long invokePJJP(long var0, long var2, long var4, long var6);

    public static native long invokePPJP(long var0, long var2, long var4, long var6);

    public static native long invokePPPP(long var0, long var2, long var4, long var6);

    public static native long invokePPUP(long var0, long var2, byte var4, long var5);

    public static native long invokePPPP(int var0, long var1, long var3, long var5, long var7);

    public static native long invokePPPP(long var0, int var2, long var3, long var5, long var7);

    public static native long invokePPPP(long var0, long var2, int var4, long var5, long var7);

    public static native long invokePPPP(long var0, long var2, long var4, int var6, long var7);

    public static native long invokePPUP(long var0, long var2, int var4, byte var5, long var6);

    public static native long invokePPPP(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native long invokePPPP(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native long invokePPPP(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native long invokePPPP(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native long invokePPPP(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native long invokeJPPP(int var0, int var1, int var2, long var3, long var5, long var7, long var9);

    public static native long invokePPPP(long var0, int var2, long var3, long var5, int var7, int var8, long var9);

    public static native long invokePPPP(long var0, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native long invokePPPP(long var0, long var2, int var4, int var5, long var6, int var8, long var9);

    public static native long invokePPPP(int var0, int var1, int var2, long var3, long var5, int var7, long var8, long var10);

    public static native long invokePPPP(long var0, long var2, int var4, int var5, long var6, int var8, int var9, long var10);

    public static native long invokePBPPP(long var0, byte var2, long var3, long var5, long var7);

    public static native long invokePNNPP(long var0, long var2, long var4, long var6, long var8);

    public static native long invokePPJPP(long var0, long var2, long var4, long var6, long var8);

    public static native long invokePPNNP(long var0, long var2, long var4, long var6, long var8);

    public static native long invokePPPPP(long var0, long var2, long var4, long var6, long var8);

    public static native long invokePPPJP(int var0, long var1, long var3, long var5, long var7, long var9);

    public static native long invokePPPJP(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native long invokePPPPP(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native long invokePPPPP(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native long invokePPPPP(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native long invokePPPPP(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native long invokePJPPP(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native long invokePJPPP(long var0, long var2, int var4, long var5, long var7, int var9, long var10);

    public static native long invokePPPJP(long var0, long var2, long var4, long var6, int var8, int var9, long var10);

    public static native long invokePPPPP(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native long invokePPPPP(long var0, long var2, long var4, int var6, long var7, int var9, long var10);

    public static native long invokePPPPP(long var0, long var2, long var4, int var6, int var7, int var8, long var9, long var11);

    public static native long invokePPPPP(long var0, long var2, long var4, int var6, long var7, int var9, int var10, long var11);

    public static native long invokePPPPP(long var0, long var2, long var4, long var6, int var8, int var9, int var10, long var11);

    public static native long invokePPPPP(int var0, int var1, int var2, long var3, long var5, long var7, int var9, long var10, long var12);

    public static native long invokePJPJPP(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native long invokePNNNPP(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native long invokePPBPPP(long var0, long var2, byte var4, long var5, long var7, long var9);

    public static native long invokePPPPPP(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native long invokeCCCUJP(short var0, short var1, short var2, byte var3, int var4, long var5, long var7);

    public static native long invokePPPPNP(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native long invokePPPPPP(long var0, long var2, long var4, int var6, long var7, long var9, long var11);

    public static native long invokePPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11);

    public static native long invokePPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11);

    public static native long invokePPJPPP(long var0, long var2, long var4, int var6, int var7, long var8, long var10, long var12);

    public static native long invokePPPPPP(long var0, long var2, long var4, int var6, long var7, int var9, long var10, long var12);

    public static native long invokePPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, int var11, long var12);

    public static native long invokePPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, int var11, int var12, int var13, long var14);

    public static native long invokePPJJPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long invokePSSCCPP(long var0, short var2, short var3, short var4, short var5, long var6, long var8);

    public static native long invokePPPPPPP(long var0, long var2, long var4, int var6, long var7, long var9, long var11, long var13);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, int var12, long var13);

    public static native long invokePPPPPPP(long var0, long var2, int var4, int var5, long var6, long var8, long var10, long var12, long var14);

    public static native long invokePPPPPPP(long var0, long var2, long var4, int var6, long var7, int var9, long var10, long var12, long var14);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, int var11, long var12, long var14);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, int var11, long var12, int var14, int var15, long var16);

    public static native long invokePPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, int var14, int var15, long var16);

    public static native long invokePPPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, long var15);

    public static native long invokePPPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, int var15, long var16);

    public static native long invokePPPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14, int var16, long var17);

    public static native long invokePPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native long invokePPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15, long var17);

    public static native long invokePPPPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, int var15, long var16, long var18);

    public static native long invokePPPPJJPPP(long var0, long var2, long var4, long var6, int var8, long var9, int var11, long var12, int var14, long var15, long var17, long var19);

    public static native long invokePPPPPJJPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14, int var16, long var17, long var19);

    public static native long invokePPPPPJPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14, int var16, long var17, long var19);

    public static native long invokePPPPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14, int var16, long var17, long var19);

    public static native long invokePPPPPJPPP(long var0, int var2, long var3, long var5, long var7, long var9, int var11, int var12, long var13, int var15, long var16, long var18, long var20);

    public static native long invokePPPPPJPPP(long var0, int var2, long var3, long var5, long var7, long var9, int var11, int var12, long var13, int var15, int var16, long var17, long var19, long var21);

    public static native long invokePPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15, int var17, int var18, int var19, int var20, int var21, long var22);

    public static native long invokePPPPPJJJPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, long var13, long var15, int var17, long var18, long var20);

    public static native long invokePPPPPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, long var15, int var17, long var18, long var20);

    public static native long invokePPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15, int var17, long var18, int var20, long var21);

    public static native long invokePPPPPJPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, int var14, long var15, int var17, int var18, long var19, long var21, long var23);

    public static native long invokePPPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20);

    public static native long invokePPPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15, int var17, long var18, long var20, int var22, long var23);

    public static native long invokePPPPPJPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, int var14, long var15, long var17, int var19, int var20, long var21, long var23, long var25, long var27);

    public static native long invokePPPPPPPPPPPPP(long var0, int var2, long var3, long var5, long var7, int var9, long var10, long var12, int var14, long var15, long var17, int var19, int var20, int var21, int var22, long var23, long var25, long var27, long var29, long var31);

    public static native long invokePPPPPJJPPPPPPP(long var0, long var2, long var4, long var6, long var8, int var10, long var11, int var13, long var14, int var16, long var17, long var19, int var21, long var22, long var24, long var26, long var28, long var30);

    public static native byte invokeU(int var0, long var1);

    public static native byte invokeUPU(byte var0, long var1, long var3);

    public static native void invokeV(long var0);

    public static native void invokeV(double var0, long var2);

    public static native void invokeV(float var0, long var1);

    public static native void invokeV(int var0, long var1);

    public static native void invokeV(int var0, float var1, long var2);

    public static native void invokeV(int var0, int var1, long var2);

    public static native void invokeV(int var0, int var1, double var2, long var4);

    public static native void invokeV(int var0, int var1, float var2, long var3);

    public static native void invokeV(int var0, int var1, int var2, long var3);

    public static native void invokeV(int var0, float var1, float var2, float var3, long var4);

    public static native void invokeV(int var0, int var1, int var2, int var3, long var4);

    public static native void invokeV(int var0, int var1, double var2, double var4, double var6, long var8);

    public static native void invokeV(int var0, int var1, float var2, float var3, float var4, long var5);

    public static native void invokeV(int var0, int var1, int var2, int var3, int var4, long var5);

    public static native void invokeCV(short var0, long var1);

    public static native void invokePV(long var0, long var2);

    public static native void invokeUV(byte var0, long var1);

    public static native void invokeCV(int var0, short var1, long var2);

    public static native void invokeCV(short var0, int var1, long var2);

    public static native void invokeCV(short var0, boolean var1, long var2);

    public static native void invokeJV(int var0, long var1, long var3);

    public static native void invokeJV(long var0, int var2, long var3);

    public static native void invokePV(int var0, long var1, long var3);

    public static native void invokePV(long var0, float var2, long var3);

    public static native void invokePV(long var0, int var2, long var3);

    public static native void invokePV(long var0, boolean var2, long var3);

    public static native void invokeUV(byte var0, int var1, long var2);

    public static native void invokeUV(byte var0, boolean var1, long var2);

    public static native void invokeCV(short var0, int var1, int var2, long var3);

    public static native void invokeJV(int var0, int var1, long var2, long var4);

    public static native void invokePV(int var0, int var1, long var2, long var4);

    public static native void invokePV(int var0, long var1, boolean var3, long var4);

    public static native void invokePV(long var0, double var2, double var4, long var6);

    public static native void invokePV(long var0, float var2, float var3, long var4);

    public static native void invokePV(long var0, int var2, double var3, long var5);

    public static native void invokePV(long var0, int var2, float var3, long var4);

    public static native void invokePV(long var0, int var2, int var3, long var4);

    public static native void invokePV(long var0, int var2, boolean var3, long var4);

    public static native void invokePV(int var0, int var1, int var2, long var3, long var5);

    public static native void invokePV(long var0, float var2, float var3, float var4, long var5);

    public static native void invokePV(long var0, float var2, float var3, int var4, long var5);

    public static native void invokePV(long var0, int var2, int var3, double var4, long var6);

    public static native void invokePV(long var0, int var2, int var3, float var4, long var5);

    public static native void invokePV(long var0, int var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, long var2, int var4, int var5, long var6);

    public static native void invokePV(long var0, int var2, float var3, float var4, float var5, long var6);

    public static native void invokePV(long var0, int var2, int var3, int var4, int var5, long var6);

    public static native void invokeUV(byte var0, float var1, float var2, float var3, float var4, long var5);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, long var5, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, long var4, boolean var6, long var7);

    public static native void invokePV(long var0, int var2, int var3, double var4, double var6, double var8, long var10);

    public static native void invokePV(long var0, int var2, int var3, float var4, float var5, float var6, long var7);

    public static native void invokePV(long var0, int var2, int var3, int var4, int var5, int var6, long var7);

    public static native void invokePV(long var0, int var2, int var3, int var4, int var5, boolean var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, long var6, long var8);

    public static native void invokeCCV(short var0, short var1, long var2);

    public static native void invokeCPV(short var0, long var1, long var3);

    public static native void invokePCV(long var0, short var2, long var3);

    public static native void invokePJV(long var0, long var2, long var4);

    public static native void invokePNV(long var0, long var2, long var4);

    public static native void invokePPV(long var0, long var2, long var4);

    public static native void invokePUV(long var0, byte var2, long var3);

    public static native void invokeUPV(byte var0, long var1, long var3);

    public static native void invokeCPV(short var0, int var1, long var2, long var4);

    public static native void invokeCPV(short var0, long var1, int var3, long var4);

    public static native void invokePCV(long var0, int var2, short var3, long var4);

    public static native void invokePCV(long var0, short var2, boolean var3, long var4);

    public static native void invokePJV(int var0, long var1, long var3, long var5);

    public static native void invokePJV(long var0, int var2, long var3, long var5);

    public static native void invokePJV(long var0, long var2, int var4, long var5);

    public static native void invokePPV(int var0, long var1, long var3, long var5);

    public static native void invokePPV(long var0, int var2, long var3, long var5);

    public static native void invokePPV(long var0, long var2, float var4, long var5);

    public static native void invokePPV(long var0, long var2, int var4, long var5);

    public static native void invokePPV(long var0, long var2, boolean var4, long var5);

    public static native void invokeUCV(byte var0, short var1, int var2, long var3);

    public static native void invokePBV(long var0, int var2, int var3, byte var4, long var5);

    public static native void invokePCV(long var0, int var2, int var3, short var4, long var5);

    public static native void invokePCV(long var0, short var2, int var3, int var4, long var5);

    public static native void invokePJV(long var0, int var2, int var3, long var4, long var6);

    public static native void invokePPV(int var0, int var1, long var2, long var4, long var6);

    public static native void invokePPV(long var0, int var2, int var3, long var4, long var6);

    public static native void invokePPV(long var0, int var2, long var3, int var5, long var6);

    public static native void invokePPV(long var0, int var2, long var3, boolean var5, long var6);

    public static native void invokePPV(long var0, long var2, int var4, int var5, long var6);

    public static native void invokePSV(long var0, int var2, int var3, short var4, long var5);

    public static native void invokePUV(long var0, int var2, int var3, byte var4, long var5);

    public static native void invokeUCV(byte var0, short var1, int var2, int var3, long var4);

    public static native void invokeUPV(byte var0, long var1, int var3, int var4, long var5);

    public static native void invokePCV(long var0, short var2, int var3, int var4, int var5, long var6);

    public static native void invokePPV(int var0, int var1, int var2, long var3, long var5, long var7);

    public static native void invokePPV(long var0, float var2, long var3, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native void invokePPV(long var0, long var2, int var4, int var5, int var6, long var7);

    public static native void invokePPV(int var0, int var1, int var2, int var3, long var4, long var6, long var8);

    public static native void invokePPV(long var0, int var2, int var3, int var4, int var5, long var6, long var8);

    public static native void invokePPV(long var0, int var2, int var3, long var4, int var6, int var7, long var8);

    public static native void invokePPV(long var0, long var2, float var4, float var5, float var6, float var7, long var8);

    public static native void invokePPV(int var0, int var1, int var2, int var3, int var4, long var5, long var7, long var9);

    public static native void invokePPV(long var0, int var2, int var3, int var4, int var5, int var6, long var7, long var9);

    public static native void invokePPV(long var0, int var2, int var3, int var4, int var5, long var6, boolean var8, long var9);

    public static native void invokePPV(long var0, long var2, int var4, int var5, int var6, int var7, int var8, long var9);

    public static native void invokePPV(long var0, long var2, float var4, float var5, float var6, float var7, float var8, float var9, long var10);

    public static native void invokeCCPV(short var0, short var1, long var2, long var4);

    public static native void invokeCPCV(short var0, long var1, short var3, long var4);

    public static native void invokeCPPV(short var0, long var1, long var3, long var5);

    public static native void invokePNNV(long var0, long var2, long var4, long var6);

    public static native void invokePNPV(long var0, long var2, long var4, long var6);

    public static native void invokePPNV(long var0, long var2, long var4, long var6);

    public static native void invokePPPV(long var0, long var2, long var4, long var6);

    public static native void invokeCCCV(short var0, short var1, short var2, int var3, long var4);

    public static native void invokeCCUV(short var0, short var1, int var2, byte var3, long var4);

    public static native void invokePJPV(long var0, long var2, long var4, int var6, long var7);

    public static native void invokePPJV(long var0, int var2, long var3, long var5, long var7);

    public static native void invokePPPV(int var0, long var1, long var3, long var5, long var7);

    public static native void invokePPPV(long var0, int var2, long var3, long var5, long var7);

    public static native void invokePPPV(long var0, long var2, int var4, long var5, long var7);

    public static native void invokePPPV(long var0, long var2, long var4, float var6, long var7);

    public static native void invokePPPV(long var0, long var2, long var4, int var6, long var7);

    public static native void invokePUCV(long var0, byte var2, short var3, int var4, long var5);

    public static native void invokeUCCV(byte var0, short var1, short var2, int var3, long var4);

    public static native void invokeCCUV(short var0, short var1, int var2, float var3, byte var4, long var5);

    public static native void invokeJJJV(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native void invokePNNV(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native void invokePPPV(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native void invokePPPV(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native void invokePPPV(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native void invokePPPV(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native void invokePPPV(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native void invokePPPV(long var0, long var2, long var4, float var6, float var7, long var8);

    public static native void invokePPPV(long var0, long var2, long var4, boolean var6, boolean var7, long var8);

    public static native void invokePUCV(long var0, byte var2, short var3, int var4, int var5, long var6);

    public static native void invokePUPV(long var0, byte var2, long var3, int var5, int var6, long var7);

    public static native void invokeUCCV(byte var0, short var1, int var2, int var3, short var4, long var5);

    public static native void invokeUCUV(byte var0, short var1, byte var2, int var3, int var4, long var5);

    public static native void invokeUPCV(byte var0, long var1, int var3, int var4, short var5, long var6);

    public static native void invokeCCUV(short var0, short var1, int var2, int var3, int var4, byte var5, long var6);

    public static native void invokePPPV(int var0, int var1, long var2, int var4, long var5, long var7, long var9);

    public static native void invokePPPV(long var0, int var2, int var3, int var4, long var5, long var7, long var9);

    public static native void invokePPPV(long var0, int var2, long var3, int var5, long var6, int var8, long var9);

    public static native void invokePPPV(long var0, int var2, long var3, long var5, int var7, int var8, long var9);

    public static native void invokePPPV(long var0, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native void invokePPPV(long var0, long var2, long var4, int var6, boolean var7, boolean var8, long var9);

    public static native void invokePPPV(long var0, boolean var2, int var3, long var4, long var6, int var8, long var9);

    public static native void invokePPPV(long var0, int var2, int var3, long var4, int var6, long var7, int var9, long var10);

    public static native void invokePPPV(long var0, int var2, long var3, int var5, long var6, int var8, int var9, long var10);

    public static native void invokePPPV(long var0, long var2, float var4, float var5, float var6, float var7, long var8, long var10);

    public static native void invokePPPV(long var0, long var2, long var4, float var6, float var7, float var8, float var9, long var10);

    public static native void invokePPPV(long var0, long var2, int var4, int var5, int var6, long var7, int var9, boolean var10, long var11);

    public static native void invokePPPV(long var0, long var2, float var4, float var5, float var6, float var7, float var8, float var9, long var10, long var12);

    public static native void invokePPPV(long var0, long var2, long var4, float var6, float var7, float var8, float var9, float var10, float var11, long var12);

    public static native void invokeCCUPV(short var0, short var1, byte var2, long var3, long var5);

    public static native void invokePCPCV(long var0, short var2, long var3, short var5, long var6);

    public static native void invokePNPPV(long var0, long var2, long var4, long var6, long var8);

    public static native void invokePPPPV(long var0, long var2, long var4, long var6, long var8);

    public static native void invokeCCCUV(short var0, short var1, short var2, int var3, byte var4, long var5);

    public static native void invokePCCUV(long var0, short var2, short var3, int var4, byte var5, long var6);

    public static native void invokePJJPV(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native void invokePPCPV(long var0, long var2, short var4, int var5, long var6, long var8);

    public static native void invokePPPCV(long var0, long var2, int var4, long var5, short var7, long var8);

    public static native void invokePPPPV(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native void invokePPPPV(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native void invokePPPPV(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native void invokePPPPV(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native void invokePUCCV(long var0, byte var2, short var3, short var4, int var5, long var6);

    public static native void invokeCCCUV(short var0, short var1, short var2, int var3, int var4, byte var5, long var6);

    public static native void invokePJJJV(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native void invokePPPPV(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native void invokePPPPV(long var0, long var2, long var4, float var6, float var7, long var8, long var10);

    public static native void invokePUCCV(long var0, byte var2, short var3, int var4, int var5, short var6, long var7);

    public static native void invokePUCUV(long var0, byte var2, short var3, byte var4, int var5, int var6, long var7);

    public static native void invokePUPCV(long var0, byte var2, long var3, int var5, int var6, short var7, long var8);

    public static native void invokeCCCUV(short var0, short var1, short var2, int var3, int var4, int var5, byte var6, long var7);

    public static native void invokePCCUV(long var0, short var2, short var3, int var4, int var5, int var6, byte var7, long var8);

    public static native void invokePPPPV(long var0, long var2, long var4, float var6, float var7, float var8, float var9, long var10, long var12);

    public static native void invokePPPPV(long var0, long var2, long var4, int var6, int var7, int var8, float var9, long var10, long var12);

    public static native void invokePPPPV(long var0, long var2, long var4, float var6, float var7, float var8, float var9, float var10, float var11, long var12, long var14);

    public static native void invokeCCCCCV(short var0, short var1, short var2, short var3, short var4, long var5);

    public static native void invokeCCUPPV(short var0, short var1, byte var2, long var3, long var5, long var7);

    public static native void invokePPCPPV(long var0, long var2, short var4, long var5, long var7, long var9);

    public static native void invokePPPPPV(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native void invokePCCCUV(long var0, short var2, short var3, short var4, int var5, byte var6, long var7);

    public static native void invokePPPPPV(int var0, long var1, long var3, long var5, long var7, long var9, long var11);

    public static native void invokePPPPPV(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native void invokePPPPPV(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native void invokePCCCUV(long var0, short var2, short var3, short var4, int var5, int var6, byte var7, long var8);

    public static native void invokePPPPPV(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12);

    public static native void invokePCCCCV(long var0, short var2, short var3, short var4, boolean var5, boolean var6, short var7, int var8, long var9);

    public static native void invokePCCCUV(long var0, short var2, short var3, short var4, int var5, int var6, int var7, byte var8, long var9);

    public static native void invokePPPPPV(long var0, long var2, int var4, long var5, int var7, long var8, int var10, long var11, long var13);

    public static native void invokePPPPPV(long var0, long var2, int var4, long var5, long var7, int var9, int var10, long var11, long var13);

    public static native void invokeCCCCUV(short var0, short var1, short var2, int var3, short var4, int var5, int var6, int var7, byte var8, long var9);

    public static native void invokePPPPPV(int var0, long var1, int var3, long var4, long var6, long var8, int var10, long var11, int var13, boolean var14, long var15);

    public static native void invokeCCCCPCV(short var0, short var1, short var2, short var3, long var4, short var6, long var7);

    public static native void invokePPPPPPV(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native void invokePCCCCUV(long var0, short var2, int var3, short var4, short var5, short var6, byte var7, long var8);

    public static native void invokePCCCCUV(long var0, short var2, short var3, short var4, int var5, short var6, int var7, int var8, int var9, byte var10, long var11);

    public static native void invokePPPPPPPV(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native void invokePPPPPPPV(long var0, int var2, long var3, long var5, long var7, long var9, long var11, long var13, long var15);

    public static native void invokeCCUCCCCPCV(short var0, short var1, byte var2, short var3, short var4, short var5, short var6, long var7, short var9, long var10);

    public static native void invokeCUCCCCCCPV(short var0, byte var1, short var2, short var3, short var4, short var5, short var6, short var7, long var8, long var10);

    public static native void invokeCCUUCCCCPCV(short var0, short var1, byte var2, byte var3, short var4, short var5, short var6, short var7, long var8, short var10, long var11);

    public static native void invokeCCUUUUUUUUUV(short var0, short var1, float var2, byte var3, byte var4, byte var5, byte var6, byte var7, byte var8, byte var9, byte var10, byte var11, long var12);

    public static native void invokeCCUCCCCUCCCCCCV(short var0, short var1, byte var2, short var3, short var4, short var5, short var6, byte var7, short var8, short var9, short var10, short var11, short var12, short var13, long var14);

    public static native void invokePCCUCCCCUCCCCCCV(long var0, short var2, short var3, byte var4, short var5, short var6, short var7, short var8, byte var9, short var10, short var11, short var12, short var13, short var14, short var15, long var16);

    public static native boolean invokeZ(long var0);

    public static native boolean invokeZ(int var0, long var1);

    public static native boolean invokePZ(long var0, long var2);

    public static native boolean invokePZ(long var0, int var2, long var3);

    public static native boolean invokePZ(long var0, boolean var2, long var3);

    public static native boolean invokePZ(int var0, long var1, boolean var3, long var4);

    public static native boolean invokePZ(long var0, int var2, int var3, long var4);

    public static native boolean invokePZ(long var0, int var2, int var3, int var4, long var5);

    public static native boolean invokePPZ(long var0, long var2, long var4);

    public static native boolean invokeUPZ(byte var0, long var1, long var3);

    public static native boolean invokePPZ(long var0, int var2, long var3, long var5);

    public static native boolean invokePPZ(long var0, int var2, int var3, long var4, long var6);

    public static native boolean invokePPPZ(long var0, long var2, long var4, long var6);

    public static native boolean invokePJPZ(long var0, long var2, long var4, int var6, long var7);

    public static native boolean invokePPPZ(long var0, long var2, long var4, int var6, long var7);

    public static native boolean invokeCCJZ(short var0, boolean var1, short var2, int var3, long var4, long var6);

    public static native boolean invokePPPZ(long var0, long var2, long var4, boolean var6, int var7, long var8);

    public static native boolean invokePPPZ(long var0, long var2, int var4, long var5, int var7, boolean var8, long var9);

    public static native boolean invokePPPPZ(long var0, long var2, long var4, long var6, long var8);

    public static native boolean invokePPPPZ(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native boolean invokePPPUPZ(long var0, long var2, long var4, byte var6, long var7, long var9);

    public static native boolean invokePPPPPZ(long var0, long var2, long var4, long var6, long var8, int var10, long var11);

    public static native boolean invokePPPPPZ(long var0, long var2, long var4, long var6, long var8, boolean var10, int var11, long var12);

    public static native short callC(int var0, long var1);

    public static native float callF(long var0);

    public static native float callF(int var0, int var1, int var2, long var3);

    public static native float callPF(long var0, float var2, long var3);

    public static native float callPF(int var0, int var1, long var2, long var4);

    public static native float callPPPF(long var0, long var2, long var4, long var6);

    public static native int callI(long var0);

    public static native int callI(int var0, long var1);

    public static native int callI(int var0, float var1, long var2);

    public static native int callI(int var0, int var1, long var2);

    public static native int callI(int var0, int var1, int var2, long var3);

    public static native int callJI(long var0, long var2);

    public static native int callPI(long var0, long var2);

    public static native int callJI(long var0, float var2, long var3);

    public static native int callJI(long var0, int var2, long var3);

    public static native int callPI(int var0, long var1, long var3);

    public static native int callPI(long var0, float var2, long var3);

    public static native int callPI(long var0, int var2, long var3);

    public static native int callPI(long var0, boolean var2, long var3);

    public static native int callJI(long var0, int var2, boolean var3, long var4);

    public static native int callPI(int var0, int var1, long var2, long var4);

    public static native int callPI(int var0, long var1, int var3, long var4);

    public static native int callPI(long var0, float var2, float var3, long var4);

    public static native int callPI(long var0, float var2, int var3, long var4);

    public static native int callPI(long var0, int var2, float var3, long var4);

    public static native int callPI(long var0, int var2, int var3, long var4);

    public static native int callJI(int var0, long var1, int var3, int var4, long var5);

    public static native int callJI(long var0, float var2, float var3, float var4, long var5);

    public static native int callPI(int var0, int var1, int var2, long var3, long var5);

    public static native int callPI(int var0, int var1, long var2, int var4, long var5);

    public static native int callPI(int var0, long var1, int var3, int var4, long var5);

    public static native int callPI(long var0, float var2, float var3, float var4, long var5);

    public static native int callPI(long var0, int var2, float var3, float var4, long var5);

    public static native int callPI(long var0, int var2, int var3, int var4, long var5);

    public static native int callPI(long var0, int var2, int var3, boolean var4, long var5);

    public static native int callPI(int var0, int var1, int var2, int var3, long var4, long var6);

    public static native int callPI(long var0, int var2, float var3, float var4, int var5, long var6);

    public static native int callPI(long var0, int var2, int var3, int var4, int var5, long var6);

    public static native int callPI(int var0, int var1, int var2, float var3, float var4, long var5, long var7);

    public static native int callPI(int var0, int var1, int var2, int var3, int var4, long var5, long var7);

    public static native int callPI(int var0, long var1, int var3, int var4, float var5, int var6, long var7);

    public static native int callPI(long var0, int var2, int var3, int var4, int var5, int var6, long var7);

    public static native int callPI(long var0, int var2, int var3, int var4, int var5, int var6, int var7, long var8);

    public static native int callPI(int var0, int var1, long var2, int var4, int var5, int var6, int var7, float var8, long var9);

    public static native int callPI(long var0, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, long var10);

    public static native int callJJI(long var0, long var2, long var4);

    public static native int callJPI(long var0, long var2, long var4);

    public static native int callPJI(long var0, long var2, long var4);

    public static native int callPPI(long var0, long var2, long var4);

    public static native int callJPI(long var0, int var2, long var3, long var5);

    public static native int callJPI(long var0, long var2, int var4, long var5);

    public static native int callPJI(long var0, int var2, long var3, long var5);

    public static native int callPJI(long var0, long var2, float var4, long var5);

    public static native int callPJI(long var0, long var2, int var4, long var5);

    public static native int callPPI(int var0, long var1, long var3, long var5);

    public static native int callPPI(long var0, float var2, long var3, long var5);

    public static native int callPPI(long var0, int var2, long var3, long var5);

    public static native int callPPI(long var0, long var2, float var4, long var5);

    public static native int callPPI(long var0, long var2, int var4, long var5);

    public static native int callJPI(long var0, int var2, long var3, int var5, long var6);

    public static native int callJPI(long var0, long var2, int var4, int var5, long var6);

    public static native int callPJI(long var0, int var2, int var3, long var4, long var6);

    public static native int callPPI(int var0, int var1, long var2, long var4, long var6);

    public static native int callPPI(int var0, long var1, long var3, int var5, long var6);

    public static native int callPPI(long var0, int var2, float var3, long var4, long var6);

    public static native int callPPI(long var0, int var2, int var3, long var4, long var6);

    public static native int callPPI(long var0, int var2, long var3, int var5, long var6);

    public static native int callPPI(long var0, long var2, float var4, float var5, long var6);

    public static native int callPPI(long var0, long var2, float var4, int var5, long var6);

    public static native int callPPI(long var0, long var2, int var4, int var5, long var6);

    public static native int callJPI(long var0, int var2, int var3, long var4, int var6, long var7);

    public static native int callJPI(long var0, long var2, int var4, int var5, int var6, long var7);

    public static native int callPPI(int var0, int var1, int var2, long var3, long var5, long var7);

    public static native int callPPI(int var0, int var1, long var2, int var4, long var5, long var7);

    public static native int callPPI(int var0, long var1, int var3, long var4, int var6, long var7);

    public static native int callPPI(long var0, int var2, float var3, float var4, long var5, long var7);

    public static native int callPPI(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native int callPPI(long var0, int var2, int var3, long var4, int var6, long var7);

    public static native int callPPI(long var0, long var2, int var4, int var5, int var6, long var7);

    public static native int callPPI(long var0, long var2, int var4, int var5, boolean var6, long var7);

    public static native int callJJI(long var0, float var2, float var3, float var4, float var5, long var6, long var8);

    public static native int callPPI(int var0, int var1, int var2, long var3, int var5, long var6, long var8);

    public static native int callPPI(int var0, long var1, int var3, int var4, float var5, long var6, long var8);

    public static native int callPPI(long var0, int var2, float var3, float var4, float var5, long var6, long var8);

    public static native int callPPI(long var0, long var2, int var4, int var5, int var6, int var7, long var8);

    public static native int callPPI(long var0, int var2, float var3, float var4, float var5, int var6, long var7, long var9);

    public static native int callPPI(long var0, int var2, int var3, int var4, int var5, int var6, long var7, long var9);

    public static native int callPPI(long var0, int var2, float var3, float var4, float var5, float var6, int var7, long var8, long var10);

    public static native int callPPI(long var0, int var2, int var3, int var4, int var5, int var6, int var7, long var8, long var10);

    public static native int callPPI(int var0, int var1, long var2, long var4, int var6, int var7, int var8, int var9, float var10, long var11);

    public static native int callPPI(long var0, int var2, float var3, float var4, float var5, float var6, float var7, int var8, long var9, long var11);

    public static native int callPPI(long var0, int var2, int var3, float var4, float var5, float var6, float var7, float var8, int var9, long var10, int var12, long var13);

    public static native int callPPI(long var0, int var2, int var3, int var4, int var5, int var6, int var7, long var8, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18, long var19);

    public static native int callJPPI(long var0, long var2, long var4, long var6);

    public static native int callPCPI(long var0, short var2, long var3, long var5);

    public static native int callPJJI(long var0, long var2, long var4, long var6);

    public static native int callPJPI(long var0, long var2, long var4, long var6);

    public static native int callPPJI(long var0, long var2, long var4, long var6);

    public static native int callPPPI(long var0, long var2, long var4, long var6);

    public static native int callPUPI(long var0, byte var2, long var3, long var5);

    public static native int callJJPI(long var0, long var2, long var4, int var6, long var7);

    public static native int callJPJI(long var0, long var2, int var4, long var5, long var7);

    public static native int callJPPI(long var0, int var2, long var3, long var5, long var7);

    public static native int callJPPI(long var0, long var2, int var4, long var5, long var7);

    public static native int callJPPI(long var0, long var2, long var4, int var6, long var7);

    public static native int callPJJI(long var0, long var2, int var4, long var5, long var7);

    public static native int callPJJI(long var0, long var2, long var4, float var6, long var7);

    public static native int callPJJI(long var0, long var2, long var4, int var6, long var7);

    public static native int callPJJI(long var0, long var2, long var4, boolean var6, long var7);

    public static native int callPJPI(long var0, int var2, long var3, long var5, long var7);

    public static native int callPJPI(long var0, long var2, int var4, long var5, long var7);

    public static native int callPPJI(long var0, int var2, long var3, long var5, long var7);

    public static native int callPPJI(long var0, long var2, int var4, long var5, long var7);

    public static native int callPPJI(long var0, long var2, long var4, int var6, long var7);

    public static native int callPPNI(long var0, int var2, long var3, long var5, long var7);

    public static native int callPPPI(int var0, long var1, long var3, long var5, long var7);

    public static native int callPPPI(long var0, int var2, long var3, long var5, long var7);

    public static native int callPPPI(long var0, long var2, int var4, long var5, long var7);

    public static native int callPPPI(long var0, long var2, long var4, int var6, long var7);

    public static native int callJPJI(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native int callJPPI(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native int callJPPI(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native int callJPPI(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native int callJPPI(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native int callPJJI(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native int callPJPI(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native int callPPJI(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native int callPPPI(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native int callPPPI(int var0, long var1, int var3, long var4, long var6, long var8);

    public static native int callPPPI(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native int callPPPI(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native int callPPPI(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native int callPPPI(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native int callPPPI(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native int callJPJI(long var0, int var2, float var3, long var4, int var6, long var7, long var9);

    public static native int callJPPI(long var0, int var2, long var3, int var5, long var6, int var8, long var9);

    public static native int callPPPI(long var0, int var2, int var3, int var4, long var5, long var7, long var9);

    public static native int callPPPI(long var0, int var2, int var3, long var4, int var6, long var7, long var9);

    public static native int callPPPI(long var0, int var2, long var3, long var5, int var7, int var8, long var9);

    public static native int callPPPI(long var0, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native int callPPPI(long var0, long var2, long var4, int var6, int var7, int var8, long var9);

    public static native int callPPPI(long var0, float var2, float var3, int var4, int var5, long var6, long var8, long var10);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int var5, long var6, long var8, long var10);

    public static native int callPPPI(long var0, int var2, long var3, long var5, int var7, int var8, int var9, long var10);

    public static native int callPPPI(long var0, long var2, int var4, int var5, int var6, int var7, long var8, long var10);

    public static native int callJPPI(int var0, int var1, int var2, int var3, int var4, long var5, long var7, long var9, long var11);

    public static native int callPPJI(int var0, int var1, int var2, long var3, int var5, long var6, boolean var8, long var9, long var11);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int var5, int var6, int var7, int var8, long var9, long var11, long var13);

    public static native int callJJPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callJPPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPCPPI(long var0, short var2, long var3, long var5, long var7);

    public static native int callPJJJI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPJJPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPJPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPPJPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPPNPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPPPJI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPPPPI(long var0, long var2, long var4, long var6, long var8);

    public static native int callPUPPI(long var0, byte var2, long var3, long var5, long var7);

    public static native int callJPPPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int callPJJJI(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native int callPJPPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int callPPJPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int callPPPJI(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native int callPPPPI(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native int callPPPPI(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native int callPPPPI(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native int callJPPPI(long var0, int var2, long var3, long var5, long var7, int var9, long var10);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, long var6, long var8, long var10);

    public static native int callPPPPI(int var0, long var1, long var3, int var5, long var6, long var8, long var10);

    public static native int callPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native int callPPPPI(long var0, int var2, long var3, int var5, long var6, long var8, long var10);

    public static native int callPPPPI(long var0, int var2, long var3, long var5, int var7, long var8, long var10);

    public static native int callPPPPI(long var0, int var2, long var3, long var5, long var7, int var9, long var10);

    public static native int callPPPPI(long var0, long var2, int var4, int var5, long var6, long var8, long var10);

    public static native int callPPPPI(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, int var7, long var8, long var10);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, long var7, int var9, long var10);

    public static native int callPPPPI(long var0, long var2, long var4, long var6, int var8, int var9, long var10);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, int var6, long var7, long var9, long var11);

    public static native int callPPPPI(long var0, int var2, int var3, int var4, long var5, long var7, long var9, long var11);

    public static native int callPPPPI(long var0, int var2, long var3, int var5, int var6, long var7, long var9, long var11);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, int var7, int var8, long var9, long var11);

    public static native int callJPPJI(long var0, int var2, int var3, int var4, long var5, int var7, long var8, boolean var10, long var11, long var13);

    public static native int callPPPPI(long var0, int var2, int var3, int var4, int var5, int var6, int var7, int var8, long var9, long var11, long var13, long var15);

    public static native int callPPPPI(int var0, long var1, long var3, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20, int var21, int var22, long var23, long var25, long var27);

    public static native int callJPPPJI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPJJPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPJPPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPPCPPI(long var0, long var2, short var4, long var5, long var7, long var9);

    public static native int callPPJPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPPPJJI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPPPPJI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native int callPPUPPI(long var0, long var2, byte var4, long var5, long var7, long var9);

    public static native int callPJJJPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11);

    public static native int callPJPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, long var2, long var4, int var6, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11);

    public static native int callPPPPPI(long var0, long var2, long var4, long var6, long var8, int var10, long var11);

    public static native int callJJPPPI(long var0, long var2, int var4, long var5, int var7, long var8, long var10, long var12);

    public static native int callPPJPPI(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12);

    public static native int callPPJPPI(long var0, long var2, int var4, long var5, int var7, long var8, long var10, long var12);

    public static native int callPPPPPI(int var0, int var1, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int callPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12);

    public static native int callPPPPPI(long var0, int var2, long var3, int var5, long var6, long var8, long var10, long var12);

    public static native int callPPPPPI(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12);

    public static native int callPPPPPI(long var0, long var2, long var4, long var6, long var8, int var10, int var11, long var12);

    public static native int callJPPPPI(int var0, int var1, long var2, long var4, int var6, long var7, long var9, long var11, long var13);

    public static native int callPJPPJI(long var0, long var2, int var4, int var5, long var6, long var8, long var10, int var12, long var13);

    public static native int callPPPPPI(long var0, int var2, long var3, int var5, long var6, int var8, long var9, long var11, long var13);

    public static native int callJPJPPJI(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int callPJJJJPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int callPPCPPPI(long var0, long var2, short var4, long var5, long var7, long var9, long var11);

    public static native int callPPPPJPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int callPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native int callPPUPPPI(long var0, long var2, byte var4, long var5, long var7, long var9, long var11);

    public static native int callPJJPPPI(long var0, long var2, long var4, int var6, long var7, long var9, long var11, long var13);

    public static native int callPJPPPPI(long var0, int var2, long var3, long var5, long var7, long var9, long var11, long var13);

    public static native int callPPPJPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13);

    public static native int callPJPPPPI(long var0, int var2, long var3, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPJPPI(long var0, int var2, long var3, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPI(int var0, int var1, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native int callPPPPPPI(long var0, int var2, long var3, int var5, long var6, long var8, long var10, long var12, long var14);

    public static native int callPPPPPPI(long var0, int var2, long var3, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11, int var13, long var14);

    public static native int callPPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12, int var14, int var15, int var16, int var17, long var18);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, long var8, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15);

    public static native int callPPPPPPPI(long var0, int var2, long var3, int var5, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native int callPPPPPPPI(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12, long var14, long var16);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, int var11, long var12, long var14, long var16);

    public static native int callPPPPPPPI(long var0, long var2, long var4, int var6, long var7, long var9, int var11, long var12, long var14, long var16);

    public static native int callPPPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12, int var14, int var15, long var16, int var18, int var19, int var20, int var21, long var22);

    public static native int callPPPPPJPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native int callPPJPPPPPI(long var0, long var2, long var4, long var6, long var8, int var10, long var11, long var13, long var15, long var17);

    public static native int callPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, int var12, long var13, long var15, long var17);

    public static native int callPPPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11, int var13, long var14, long var16, long var18);

    public static native int callPPPPPPPPI(long var0, int var2, int var3, long var4, long var6, long var8, long var10, int var12, long var13, long var15, long var17, long var19);

    public static native int callJPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18);

    public static native int callPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, int var12, long var13, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, int var15, long var16, long var18, long var20);

    public static native int callPPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, long var15, long var17, long var19, long var21);

    public static native int callPPPPPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, int var15, long var16, long var18, long var20, long var22);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, long var19, int var21, long var22, long var24, long var26);

    public static native int callPPPPPPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20, int var22, long var23, long var25, long var27, long var29);

    public static native long callJ(long var0);

    public static native long callJ(int var0, long var1);

    public static native long callJ(int var0, int var1, long var2);

    public static native long callJ(int var0, int var1, boolean var2, int var3, int var4, long var5);

    public static native long callJJ(long var0, long var2);

    public static native long callPJ(long var0, long var2);

    public static native long callPJ(int var0, int var1, long var2, long var4);

    public static native long callPPJ(long var0, long var2, long var4);

    public static native long callPPJ(long var0, int var2, long var3, long var5);

    public static native long callPJJ(long var0, long var2, int var4, int var5, long var6);

    public static native long callPN(long var0, long var2);

    public static native long callP(long var0);

    public static native long callP(int var0, long var1);

    public static native long callP(int var0, int var1, long var2);

    public static native long callP(int var0, float var1, float var2, float var3, long var4);

    public static native long callJP(long var0, long var2);

    public static native long callPP(long var0, long var2);

    public static native long callPP(int var0, long var1, long var3);

    public static native long callPP(long var0, int var2, long var3);

    public static native long callPP(int var0, int var1, long var2, long var4);

    public static native long callPP(int var0, long var1, int var3, long var4);

    public static native long callPP(long var0, int var2, int var3, long var4);

    public static native long callPP(int var0, int var1, long var2, int var4, long var5);

    public static native long callJJP(long var0, long var2, long var4);

    public static native long callPNP(long var0, long var2, long var4);

    public static native long callPPP(long var0, long var2, long var4);

    public static native long callPPP(int var0, long var1, long var3, long var5);

    public static native long callPPP(long var0, int var2, long var3, long var5);

    public static native long callPPP(long var0, long var2, int var4, long var5);

    public static native long callPPP(int var0, long var1, long var3, int var5, long var6);

    public static native long callPPP(long var0, int var2, int var3, long var4, long var6);

    public static native long callPPP(int var0, int var1, int var2, long var3, long var5, long var7);

    public static native long callPPP(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native long callPPP(long var0, long var2, int var4, int var5, int var6, long var7);

    public static native long callPPNP(long var0, long var2, long var4, long var6);

    public static native long callPPPP(long var0, long var2, long var4, long var6);

    public static native long callPJPP(long var0, long var2, int var4, long var5, long var7);

    public static native long callPJPP(long var0, long var2, long var4, int var6, long var7);

    public static native long callPPPP(int var0, long var1, long var3, long var5, long var7);

    public static native long callPPPP(long var0, int var2, long var3, long var5, long var7);

    public static native long callPPPP(long var0, long var2, int var4, long var5, long var7);

    public static native long callPPPP(long var0, long var2, long var4, int var6, long var7);

    public static native long callPPPP(long var0, long var2, int var4, int var5, long var6, long var8);

    public static native long callPPPP(long var0, long var2, int var4, long var5, int var7, long var8);

    public static native long callPJPP(long var0, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native long callJJPPP(long var0, long var2, long var4, long var6, long var8);

    public static native long callPPJPP(long var0, long var2, long var4, long var6, long var8);

    public static native long callPPNPP(long var0, long var2, long var4, long var6, long var8);

    public static native long callPPPPP(long var0, long var2, long var4, long var6, long var8);

    public static native long callPJPPP(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native long callPJPPP(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native long callPPPPP(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native long callPPPPP(long var0, long var2, int var4, long var5, long var7, long var9);

    public static native long callPPPPP(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native long callPPPPP(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native long callPJPPP(long var0, long var2, int var4, int var5, long var6, long var8, long var10);

    public static native long callPJPPPP(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native long callPPPJPP(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native long callPPPPPP(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native long callPPPPPP(long var0, long var2, long var4, long var6, int var8, long var9, long var11);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long callPPJPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long callPPPJPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native long callPPPPPPP(long var0, int var2, long var3, long var5, long var7, long var9, long var11, long var13);

    public static native long callPPJPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native long callPPPPJPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14);

    public static native long callPPPPPPPP(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12, long var14, long var16);

    public static native long callPPPPPPPP(int var0, long var1, long var3, int var5, int var6, int var7, int var8, long var9, long var11, long var13, int var15, long var16, long var18, long var20);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native long callPPJPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, int var11, long var12, long var14, long var16, long var18);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20);

    public static native long callPPJPPPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, int var15, long var16, long var18, long var20, long var22);

    public static native short callS(int var0, long var1);

    public static native short callPS(long var0, long var2);

    public static native short callPCS(long var0, short var2, long var3);

    public static native short callPPS(long var0, long var2, long var4);

    public static native short callPSS(long var0, short var2, long var3);

    public static native short callSPS(short var0, long var1, long var3);

    public static native short callPPS(long var0, long var2, int var4, long var5);

    public static native short callPPS(long var0, int var2, long var3, int var5, long var6);

    public static native short callPCPS(long var0, short var2, long var3, long var5);

    public static native short callPPCS(long var0, long var2, short var4, long var5);

    public static native short callPPPS(long var0, long var2, long var4, long var6);

    public static native short callPPSS(long var0, long var2, short var4, long var5);

    public static native short callPSPS(long var0, short var2, long var3, long var5);

    public static native short callSPPS(short var0, long var1, long var3, long var5);

    public static native short callSPSS(short var0, long var1, short var3, long var4);

    public static native short callPPPS(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native short callPJCCS(long var0, long var2, short var4, short var5, long var6);

    public static native short callPPSPS(long var0, long var2, short var4, long var5, long var7);

    public static native short callPSSPS(long var0, short var2, short var3, long var4, int var6, long var7);

    public static native short callPPPPS(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native short callPCPPPS(long var0, short var2, long var3, long var5, long var7, long var9);

    public static native short callPCPSPS(long var0, short var2, long var3, short var5, long var6, long var8);

    public static native short callPSSPPS(long var0, short var2, short var3, long var4, int var6, long var7, long var9);

    public static native short callPCPPPPS(long var0, short var2, long var3, long var5, long var7, long var9, long var11);

    public static native short callPCSPPPS(long var0, short var2, short var3, long var4, long var6, long var8, long var10);

    public static native short callPPSPSPS(long var0, long var2, short var4, long var5, short var7, long var8, long var10);

    public static native short callPCCPSPPS(long var0, short var2, short var3, long var4, short var6, long var7, long var9, long var11);

    public static native short callPPSPSPSS(long var0, long var2, short var4, long var5, short var7, long var8, short var10, long var11);

    public static native short callSPSSPSPS(short var0, long var1, short var3, short var4, long var5, short var7, long var8, long var10);

    public static native short callPCPSPPSPS(long var0, short var2, long var3, short var5, long var6, long var8, short var10, long var11, long var13);

    public static native short callPPPSPSPCS(long var0, long var2, long var4, short var6, long var7, short var9, long var10, short var12, long var13);

    public static native short callSPSPPPSPS(short var0, long var1, short var3, long var4, long var6, long var8, short var10, long var11, long var13);

    public static native short callPCPSPPPPPS(long var0, short var2, long var3, short var5, long var6, long var8, long var10, long var12, long var14, long var16);

    public static native short callPPSPSPSCCS(long var0, long var2, short var4, long var5, short var7, long var8, short var10, short var11, short var12, long var13);

    public static native short callPPSPSPSPSS(long var0, long var2, short var4, long var5, short var7, long var8, short var10, long var11, short var13, long var14);

    public static native short callPCPSPSPSCCS(long var0, short var2, long var3, short var5, long var6, short var8, long var9, short var11, short var12, short var13, long var14);

    public static native short callPCSSSPSPPPS(long var0, short var2, short var3, short var4, short var5, long var6, short var8, long var9, long var11, long var13, long var15);

    public static native short callPSSSPSSPPPS(long var0, short var2, short var3, short var4, long var5, short var7, short var8, long var9, long var11, long var13, long var15);

    public static native short callPSPSPPPPPPPS(long var0, short var2, long var3, short var5, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20);

    public static native short callPPSPSPSPSPSPSS(long var0, long var2, short var4, long var5, short var7, long var8, short var10, long var11, short var13, long var14, short var16, long var17, short var19, long var20);

    public static native void callV(long var0);

    public static native void callV(double var0, long var2);

    public static native void callV(float var0, long var1);

    public static native void callV(int var0, long var1);

    public static native void callV(boolean var0, long var1);

    public static native void callV(double var0, double var2, long var4);

    public static native void callV(float var0, float var1, long var2);

    public static native void callV(float var0, boolean var1, long var2);

    public static native void callV(int var0, double var1, long var3);

    public static native void callV(int var0, float var1, long var2);

    public static native void callV(int var0, int var1, long var2);

    public static native void callV(int var0, boolean var1, long var2);

    public static native void callV(double var0, double var2, double var4, long var6);

    public static native void callV(float var0, float var1, float var2, long var3);

    public static native void callV(int var0, double var1, double var3, long var5);

    public static native void callV(int var0, float var1, float var2, long var3);

    public static native void callV(int var0, int var1, double var2, long var4);

    public static native void callV(int var0, int var1, float var2, long var3);

    public static native void callV(int var0, int var1, int var2, long var3);

    public static native void callV(int var0, int var1, boolean var2, long var3);

    public static native void callV(double var0, double var2, double var4, double var6, long var8);

    public static native void callV(float var0, float var1, float var2, float var3, long var4);

    public static native void callV(int var0, double var1, double var3, double var5, long var7);

    public static native void callV(int var0, float var1, float var2, float var3, long var4);

    public static native void callV(int var0, int var1, double var2, double var4, long var6);

    public static native void callV(int var0, int var1, float var2, float var3, long var4);

    public static native void callV(int var0, int var1, float var2, int var3, long var4);

    public static native void callV(int var0, int var1, int var2, double var3, long var5);

    public static native void callV(int var0, int var1, int var2, float var3, long var4);

    public static native void callV(int var0, int var1, int var2, int var3, long var4);

    public static native void callV(int var0, int var1, int var2, boolean var3, long var4);

    public static native void callV(int var0, int var1, boolean var2, int var3, long var4);

    public static native void callV(boolean var0, boolean var1, boolean var2, boolean var3, long var4);

    public static native void callV(int var0, double var1, double var3, double var5, double var7, long var9);

    public static native void callV(int var0, float var1, float var2, float var3, float var4, long var5);

    public static native void callV(int var0, int var1, double var2, double var4, double var6, long var8);

    public static native void callV(int var0, int var1, float var2, float var3, float var4, long var5);

    public static native void callV(int var0, int var1, int var2, float var3, int var4, long var5);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, long var5);

    public static native void callV(int var0, int var1, int var2, boolean var3, int var4, long var5);

    public static native void callV(int var0, boolean var1, boolean var2, boolean var3, boolean var4, long var5);

    public static native void callV(double var0, double var2, double var4, double var6, double var8, double var10, long var12);

    public static native void callV(float var0, float var1, float var2, float var3, float var4, boolean var5, long var6);

    public static native void callV(int var0, double var1, double var3, int var5, double var6, double var8, long var10);

    public static native void callV(int var0, float var1, float var2, int var3, float var4, float var5, long var6);

    public static native void callV(int var0, int var1, double var2, double var4, double var6, double var8, long var10);

    public static native void callV(int var0, int var1, float var2, float var3, float var4, float var5, long var6);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, long var6);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, boolean var5, long var6);

    public static native void callV(int var0, int var1, int var2, int var3, boolean var4, int var5, long var6);

    public static native void callV(int var0, double var1, double var3, double var5, double var7, double var9, double var11, long var13);

    public static native void callV(int var0, int var1, int var2, double var3, double var5, double var7, double var9, long var11);

    public static native void callV(int var0, int var1, int var2, float var3, float var4, float var5, float var6, long var7);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, long var7);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, boolean var6, long var7);

    public static native void callV(int var0, int var1, int var2, boolean var3, int var4, int var5, int var6, long var7);

    public static native void callV(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, long var8);

    public static native void callV(int var0, int var1, int var2, float var3, float var4, float var5, float var6, float var7, long var8);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, long var8);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7, long var8);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, long var9);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, long var9);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, long var10);

    public static native void callV(int var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, long var11);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, long var12);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, long var15);

    public static native void callV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, long var17);

    public static native void callJV(long var0, long var2);

    public static native void callPV(long var0, long var2);

    public static native void callSV(short var0, long var1);

    public static native void callUV(byte var0, long var1);

    public static native void callCV(int var0, short var1, long var2);

    public static native void callJV(int var0, long var1, long var3);

    public static native void callJV(long var0, int var2, long var3);

    public static native void callPV(int var0, long var1, long var3);

    public static native void callPV(long var0, float var2, long var3);

    public static native void callPV(long var0, int var2, long var3);

    public static native void callSV(int var0, short var1, long var2);

    public static native void callCV(int var0, int var1, short var2, long var3);

    public static native void callJV(int var0, int var1, long var2, long var4);

    public static native void callPV(int var0, int var1, long var2, long var4);

    public static native void callPV(int var0, long var1, int var3, long var4);

    public static native void callPV(long var0, float var2, float var3, long var4);

    public static native void callPV(long var0, int var2, int var3, long var4);

    public static native void callJV(int var0, long var1, int var3, int var4, long var5);

    public static native void callNV(long var0, int var2, int var3, int var4, long var5);

    public static native void callPV(int var0, float var1, long var2, int var4, long var5);

    public static native void callPV(int var0, int var1, int var2, long var3, long var5);

    public static native void callPV(int var0, int var1, long var2, int var4, long var5);

    public static native void callPV(int var0, int var1, boolean var2, long var3, long var5);

    public static native void callPV(int var0, long var1, int var3, int var4, long var5);

    public static native void callPV(long var0, float var2, float var3, float var4, long var5);

    public static native void callPV(long var0, int var2, int var3, int var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, long var4, long var6);

    public static native void callPV(int var0, int var1, int var2, long var3, int var5, long var6);

    public static native void callPV(int var0, int var1, int var2, long var3, boolean var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, long var4, long var6);

    public static native void callPV(int var0, int var1, long var2, int var4, int var5, long var6);

    public static native void callPV(int var0, long var1, int var3, int var4, int var5, long var6);

    public static native void callPV(int var0, boolean var1, int var2, int var3, long var4, long var6);

    public static native void callPV(long var0, int var2, int var3, int var4, int var5, long var6);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, long var5, long var7);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, long var7, long var9);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, long var5, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, long var5, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, long var4, boolean var6, long var7);

    public static native void callPV(int var0, int var1, int var2, long var3, int var5, int var6, long var7);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, long var5, long var7);

    public static native void callPV(int var0, int var1, long var2, int var4, int var5, int var6, long var7);

    public static native void callPV(int var0, boolean var1, int var2, int var3, int var4, long var5, long var7);

    public static native void callPV(long var0, int var2, int var3, int var4, int var5, int var6, long var7);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, int var5, long var6, long var8);

    public static native void callPV(int var0, int var1, float var2, float var3, float var4, float var5, long var6, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, long var6, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, long var5, int var7, long var8);

    public static native void callPV(int var0, int var1, int var2, long var3, int var5, int var6, int var7, long var8);

    public static native void callPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, long var8);

    public static native void callPV(long var0, int var2, int var3, int var4, int var5, int var6, int var7, long var8);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, long var7, long var9);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, boolean var5, int var6, long var7, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, long var7, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, boolean var5, int var6, long var7, long var9);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, int var5, boolean var6, int var7, long var8, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, long var8, long var10);

    public static native void callPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, int var8, float var9, long var10);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, double var7, double var9, int var11, int var12, long var13, long var15);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, float var5, float var6, int var7, int var8, long var9, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, long var9, long var11);

    public static native void callJV(long var0, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, long var10, long var12);

    public static native void callPV(long var0, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, long var12);

    public static native void callJV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, long var10, boolean var12, long var13);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, long var11, long var13);

    public static native void callJPV(long var0, long var2, long var4);

    public static native void callPJV(long var0, long var2, long var4);

    public static native void callPPV(long var0, long var2, long var4);

    public static native void callSSV(short var0, short var1, long var2);

    public static native void callJJV(int var0, long var1, long var3, long var5);

    public static native void callPCV(long var0, int var2, short var3, long var4);

    public static native void callPJV(long var0, int var2, long var3, long var5);

    public static native void callPJV(long var0, long var2, float var4, long var5);

    public static native void callPJV(long var0, long var2, int var4, long var5);

    public static native void callPPV(int var0, long var1, long var3, long var5);

    public static native void callPPV(long var0, int var2, long var3, long var5);

    public static native void callPPV(long var0, long var2, int var4, long var5);

    public static native void callSSV(int var0, short var1, short var2, long var3);

    public static native void callJJV(int var0, int var1, long var2, long var4, long var6);

    public static native void callJPV(int var0, int var1, long var2, long var4, long var6);

    public static native void callJPV(int var0, long var1, int var3, long var4, long var6);

    public static native void callPJV(int var0, long var1, int var3, long var4, long var6);

    public static native void callPJV(long var0, int var2, long var3, int var5, long var6);

    public static native void callPJV(long var0, long var2, int var4, int var5, long var6);

    public static native void callPPV(int var0, int var1, long var2, long var4, long var6);

    public static native void callPPV(int var0, long var1, int var3, long var4, long var6);

    public static native void callPPV(int var0, long var1, long var3, int var5, long var6);

    public static native void callPPV(int var0, long var1, long var3, boolean var5, long var6);

    public static native void callPPV(long var0, int var2, float var3, long var4, long var6);

    public static native void callPPV(long var0, int var2, int var3, long var4, long var6);

    public static native void callPPV(long var0, long var2, int var4, int var5, long var6);

    public static native void callPJV(long var0, long var2, int var4, int var5, int var6, long var7);

    public static native void callPPV(int var0, int var1, int var2, long var3, long var5, long var7);

    public static native void callPPV(int var0, int var1, long var2, int var4, long var5, long var7);

    public static native void callPPV(int var0, int var1, long var2, long var4, int var6, long var7);

    public static native void callPPV(int var0, long var1, int var3, long var4, int var6, long var7);

    public static native void callPPV(int var0, long var1, long var3, int var5, int var6, long var7);

    public static native void callPPV(long var0, int var2, int var3, int var4, long var5, long var7);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, long var6, long var8);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, long var6, long var8);

    public static native void callPPV(int var0, int var1, long var2, long var4, int var6, int var7, long var8);

    public static native void callPPV(int var0, long var1, long var3, int var5, int var6, int var7, long var8);

    public static native void callPPV(long var0, int var2, long var3, int var5, int var6, int var7, long var8);

    public static native void callPPV(int var0, int var1, int var2, int var3, int var4, long var5, long var7, long var9);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, int var6, long var7, long var9);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native void callPPV(int var0, int var1, long var2, long var4, int var6, int var7, int var8, long var9);

    public static native void callPPV(int var0, int var1, int var2, int var3, int var4, int var5, long var6, long var8, long var10);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, int var6, int var7, long var8, long var10);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, long var8, long var10);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, float var6, float var7, int var8, long var9, long var11);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, int var8, long var9, long var11);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, long var7, int var9, int var10, float var11, long var12);

    public static native void callBBBV(byte var0, byte var1, byte var2, long var3);

    public static native void callCCCV(short var0, short var1, short var2, long var3);

    public static native void callPJJV(long var0, long var2, long var4, long var6);

    public static native void callPJPV(long var0, long var2, long var4, long var6);

    public static native void callPPNV(long var0, long var2, long var4, long var6);

    public static native void callPPPV(long var0, long var2, long var4, long var6);

    public static native void callSSSV(short var0, short var1, short var2, long var3);

    public static native void callUUUV(byte var0, byte var1, byte var2, long var3);

    public static native void callJJJV(int var0, long var1, long var3, long var5, long var7);

    public static native void callPJJV(long var0, long var2, long var4, int var6, long var7);

    public static native void callPJPV(long var0, long var2, int var4, long var5, long var7);

    public static native void callPPPV(int var0, long var1, long var3, long var5, long var7);

    public static native void callPPPV(long var0, int var2, long var3, long var5, long var7);

    public static native void callPPPV(long var0, long var2, float var4, long var5, long var7);

    public static native void callPPPV(long var0, long var2, int var4, long var5, long var7);

    public static native void callPPPV(long var0, long var2, long var4, int var6, long var7);

    public static native void callPPPV(long var0, long var2, boolean var4, long var5, long var7);

    public static native void callSSSV(int var0, short var1, short var2, short var3, long var4);

    public static native void callJJJV(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native void callPJJV(long var0, int var2, long var3, long var5, int var7, long var8);

    public static native void callPJJV(long var0, long var2, long var4, int var6, int var7, long var8);

    public static native void callPPPV(int var0, int var1, long var2, long var4, long var6, long var8);

    public static native void callPPPV(int var0, long var1, int var3, long var4, long var6, long var8);

    public static native void callPPPV(int var0, long var1, long var3, int var5, long var6, long var8);

    public static native void callPPPV(int var0, long var1, long var3, long var5, int var7, long var8);

    public static native void callPPPV(long var0, int var2, int var3, long var4, long var6, long var8);

    public static native void callPPPV(long var0, int var2, long var3, int var5, long var6, long var8);

    public static native void callPJPV(long var0, int var2, long var3, int var5, int var6, long var7, long var9);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, long var7, long var9);

    public static native void callPPJV(int var0, long var1, long var3, int var5, long var6, boolean var8, long var9);

    public static native void callPPJV(long var0, int var2, long var3, int var5, long var6, int var8, long var9);

    public static native void callPPPV(int var0, int var1, int var2, long var3, long var5, long var7, long var9);

    public static native void callPPPV(int var0, int var1, long var2, int var4, long var5, long var7, long var9);

    public static native void callPPPV(int var0, long var1, int var3, long var4, int var6, long var7, long var9);

    public static native void callPJJV(long var0, int var2, int var3, long var4, long var6, int var8, int var9, long var10);

    public static native void callPPPV(int var0, int var1, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, long var8, long var10);

    public static native void callPPPV(long var0, int var2, long var3, int var5, int var6, int var7, long var8, long var10);

    public static native void callPPPV(int var0, int var1, int var2, int var3, long var4, int var6, long var7, long var9, long var11);

    public static native void callPPPV(long var0, int var2, int var3, int var4, int var5, int var6, long var7, long var9, long var11);

    public static native void callPPPV(long var0, long var2, int var4, int var5, int var6, int var7, int var8, int var9, long var10, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20, long var21);

    public static native void callBBBBV(byte var0, byte var1, byte var2, byte var3, long var4);

    public static native void callCCCCV(short var0, short var1, short var2, short var3, long var4);

    public static native void callPJJJV(long var0, long var2, long var4, long var6, long var8);

    public static native void callPJJPV(long var0, long var2, long var4, long var6, long var8);

    public static native void callPJPPV(long var0, long var2, long var4, long var6, long var8);

    public static native void callPPPNV(long var0, long var2, long var4, long var6, long var8);

    public static native void callPPPPV(long var0, long var2, long var4, long var6, long var8);

    public static native void callSSSSV(short var0, short var1, short var2, short var3, long var4);

    public static native void callUUUUV(byte var0, byte var1, byte var2, byte var3, long var4);

    public static native void callJJJJV(int var0, long var1, long var3, long var5, long var7, long var9);

    public static native void callPJJJV(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native void callPJJPV(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native void callPJJPV(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native void callPPPPV(int var0, long var1, long var3, long var5, long var7, long var9);

    public static native void callPPPPV(long var0, int var2, long var3, long var5, long var7, long var9);

    public static native void callPPPPV(long var0, long var2, long var4, int var6, long var7, long var9);

    public static native void callPPPPV(long var0, long var2, long var4, long var6, int var8, long var9);

    public static native void callSSSSV(int var0, short var1, short var2, short var3, short var4, long var5);

    public static native void callUUUUV(int var0, byte var1, byte var2, byte var3, byte var4, long var5);

    public static native void callJJJJV(int var0, int var1, long var2, long var4, long var6, long var8, long var10);

    public static native void callPJJPV(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native void callPJJPV(long var0, long var2, long var4, int var6, int var7, long var8, long var10);

    public static native void callPJPPV(long var0, long var2, int var4, long var5, int var7, long var8, long var10);

    public static native void callPPPPV(int var0, long var1, int var3, long var4, long var6, long var8, long var10);

    public static native void callPPPPV(int var0, long var1, long var3, long var5, long var7, int var9, long var10);

    public static native void callPPPPV(long var0, int var2, int var3, long var4, long var6, long var8, long var10);

    public static native void callPJJPV(long var0, long var2, int var4, int var5, long var6, int var8, long var9, long var11);

    public static native void callPJJPV(long var0, long var2, int var4, long var5, int var7, int var8, long var9, long var11);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, long var7, long var9, long var11);

    public static native void callPPPPV(int var0, int var1, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native void callPPPPV(int var0, int var1, long var2, long var4, long var6, long var8, int var10, long var11);

    public static native void callPJJPV(long var0, long var2, int var4, long var5, int var7, int var8, long var9, int var11, long var12);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, long var7, int var9, long var10, long var12);

    public static native void callPPPPV(long var0, int var2, int var3, int var4, int var5, long var6, int var8, long var9, int var11, long var12, long var14);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native void callPPPPPV(long var0, int var2, long var3, long var5, long var7, long var9, long var11);

    public static native void callPJJJJV(long var0, long var2, long var4, long var6, long var8, int var10, int var11, long var12);

    public static native void callPPPPPV(int var0, int var1, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native void callPPPPPV(long var0, int var2, int var3, long var4, long var6, long var8, long var10, long var12);

    public static native void callPJJJJV(long var0, long var2, int var4, int var5, long var6, long var8, long var10, int var12, long var13);

    public static native void callPJPPPV(long var0, int var2, int var3, long var4, long var6, int var8, long var9, long var11, long var13);

    public static native void callPPPPPV(long var0, long var2, long var4, long var6, long var8, int var10, int var11, int var12, long var13);

    public static native void callPPPPPV(long var0, int var2, long var3, int var5, int var6, int var7, long var8, int var10, long var11, int var13, long var14, long var16);

    public static native void callPPPPPJV(long var0, long var2, long var4, long var6, long var8, long var10, long var12);

    public static native void callPPPPPPV(long var0, long var2, long var4, int var6, int var7, long var8, long var10, long var12, long var14);

    public static native void callPPPPPPPV(int var0, int var1, int var2, long var3, int var5, long var6, long var8, long var10, long var12, long var14, long var16, long var18);

    public static native void callPPJJJJJJV(long var0, long var2, long var4, long var6, int var8, long var9, long var11, long var13, long var15, long var17);

    public static native void callPJJJJJJJJJJJV(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20, long var22, int var24, int var25, int var26, long var27);

    public static native boolean callZ(long var0);

    public static native boolean callZ(int var0, long var1);

    public static native boolean callZ(boolean var0, long var1);

    public static native boolean callZ(int var0, int var1, long var2);

    public static native boolean callZ(int var0, float var1, float var2, long var3);

    public static native boolean callZ(int var0, int var1, float var2, float var3, long var4);

    public static native boolean callJZ(long var0, long var2);

    public static native boolean callPZ(long var0, long var2);

    public static native boolean callJZ(int var0, long var1, long var3);

    public static native boolean callPZ(int var0, long var1, long var3);

    public static native boolean callPZ(long var0, int var2, long var3);

    public static native boolean callJZ(int var0, long var1, int var3, long var4);

    public static native boolean callPZ(int var0, int var1, long var2, long var4);

    public static native boolean callPZ(int var0, long var1, int var3, long var4);

    public static native boolean callPZ(int var0, float var1, float var2, long var3, long var5);

    public static native boolean callPPZ(long var0, long var2, long var4);

    public static native boolean callJPZ(long var0, long var2, int var4, long var5);

    public static native boolean callPPZ(int var0, long var1, long var3, long var5);

    public static native boolean callPPZ(long var0, int var2, long var3, long var5);

    public static native boolean callPPZ(long var0, long var2, int var4, long var5);

    public static native boolean callPPZ(int var0, long var1, int var3, long var4, long var6);

    public static native boolean callPPZ(int var0, int var1, long var2, int var4, long var5, long var7);

    public static native boolean callJPPZ(long var0, long var2, long var4, long var6);

    public static native boolean callPPPZ(long var0, long var2, long var4, long var6);

    public static native boolean callPPPPZ(int var0, int var1, int var2, float var3, long var4, long var6, long var8, long var10, long var12);

    public static native boolean callPPJPPZ(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native boolean callPPPPPZ(long var0, long var2, long var4, long var6, long var8, long var10);

    public static native short invokeUPC(byte var0, short @Nullable [] var1, boolean var2, long var3);

    public static native short invokeCPCC(short var0, short @Nullable [] var1, short var2, long var3);

    public static native int invokeCPI(short var0, int @Nullable [] var1, long var2);

    public static native int invokePCI(float @Nullable [] var0, short var1, long var2);

    public static native int invokePPI(int var0, long var1, int @Nullable [] var3, long var4);

    public static native int invokePPI(int var0, int @Nullable [] var1, int var2, int @Nullable [] var3, int var4, boolean var5, long var6);

    public static native int invokePPI(int var0, short @Nullable [] var1, int var2, short @Nullable [] var3, int var4, boolean var5, long var6);

    public static native int invokeCPUI(short var0, float @Nullable [] var1, byte var2, long var3);

    public static native int invokeCPUI(short var0, int @Nullable [] var1, byte var2, long var3);

    public static native int invokeCPUI(short var0, short @Nullable [] var1, byte var2, long var3);

    public static native int invokePPCI(long var0, float @Nullable [] var2, short var3, long var4);

    public static native int invokePPPI(int @Nullable [] var0, long var1, long var3, int var5, boolean var6, float var7, long var8);

    public static native int invokePPPI(short @Nullable [] var0, long var1, long var3, int var5, boolean var6, float var7, long var8);

    public static native int invokePPPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int invokePPPPI(long var0, long var2, int var4, int var5, float @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int invokePPPPI(long var0, long var2, int var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int invokePPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5, long var7);

    public static native int invokePNNPPPI(long var0, long var2, long var4, int var6, int var7, int @Nullable [] var8, int @Nullable [] var9, long var10, long var12);

    public static native int invokePPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native int invokePPPPPPPI(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int @Nullable [] var7, int @Nullable [] var8, long var9, long var11);

    public static native int invokePPPPPPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, int @Nullable [] var7, float @Nullable [] var8, int @Nullable [] var9, int @Nullable [] var10, int @Nullable [] var11, long var12);

    public static native long invokePP(double @Nullable [] var0, int var1, long var2);

    public static native long invokePP(float @Nullable [] var0, int var1, long var2);

    public static native long invokePP(int @Nullable [] var0, int var1, long var2);

    public static native long invokePP(long @Nullable [] var0, int var1, long var2);

    public static native long invokePP(short @Nullable [] var0, int var1, long var2);

    public static native long invokePPP(long var0, int @Nullable [] var2, long var3);

    public static native byte invokeUPU(byte var0, int @Nullable [] var1, long var2);

    public static native void invokePV(int var0, double @Nullable [] var1, long var2);

    public static native void invokePV(int var0, float @Nullable [] var1, long var2);

    public static native void invokePV(int var0, int @Nullable [] var1, long var2);

    public static native void invokePV(int var0, int var1, double @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, float @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, int @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, long @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int @Nullable [] var1, boolean var2, long var3);

    public static native void invokePV(int var0, int var1, float @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, int @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, short @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, double @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, short @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int @Nullable [] var4, boolean var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, double @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, int @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void invokeUPV(byte var0, float @Nullable [] var1, long var2);

    public static native void invokePJV(int var0, int @Nullable [] var1, long var2, long var4);

    public static native void invokePPV(long var0, int var2, double @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, int var2, float @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, int var2, int @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, float @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, int @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, short @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, int var2, int var3, double @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, float @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, long @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int @Nullable [] var3, boolean var4, long var5);

    public static native void invokePPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void invokePPV(long var0, int var2, int var3, float @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, int @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, short @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, int var4, int var5, int @Nullable [] var6, boolean var7, long var8);

    public static native void invokeCCPV(short var0, short var1, short @Nullable [] var2, long var3);

    public static native void invokeCPCV(short var0, double @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, float @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, int @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, long @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, short @Nullable [] var1, short var2, long var3);

    public static native void invokeCPPV(short var0, float @Nullable [] var1, float @Nullable [] var2, long var3);

    public static native void invokePNPV(long var0, long var2, short @Nullable [] var4, long var5);

    public static native void invokePPPV(long var0, double @Nullable [] var2, double @Nullable [] var3, long var4);

    public static native void invokePPPV(long var0, float @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void invokePPPV(long var0, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void invokePPPV(int @Nullable [] var0, int @Nullable [] var1, int @Nullable [] var2, long var3);

    public static native void invokePPJV(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void invokePPPV(int var0, float @Nullable [] var1, float @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void invokePPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void invokePPPV(int var0, int var1, double @Nullable [] var2, double @Nullable [] var3, double @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, float @Nullable [] var2, float @Nullable [] var3, float @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, long @Nullable [] var2, long @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native void invokePPPV(float @Nullable [] var0, int var1, long var2, long var4, int var6, long var7);

    public static native void invokePPPV(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6, long var8);

    public static native void invokePPPV(float @Nullable [] var0, boolean var1, int var2, long var3, long var5, int var7, long var8);

    public static native void invokePCPCV(long var0, short var2, double @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, float @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, int @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, long @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, short @Nullable [] var3, short var4, long var5);

    public static native void invokePNPPV(long var0, long var2, long var4, short @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, float @Nullable [] var3, float @Nullable [] var4, float @Nullable [] var5, long var6);

    public static native void invokePPPPV(long var0, int var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void invokePPPPV(long var0, int var2, int var3, double @Nullable [] var4, double @Nullable [] var5, double @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, float @Nullable [] var4, float @Nullable [] var5, float @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native void invokePPPPPV(long var0, long var2, long var4, float @Nullable [] var6, long var7, long var9);

    public static native void invokePPPPPV(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void invokePPPPPV(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8, long var10);

    public static native void invokePPPPPV(int var0, long var1, int var3, float @Nullable [] var4, float @Nullable [] var5, long var6, int var8, long var9, int var11, boolean var12, long var13);

    public static native void invokePPPPPV(int var0, int @Nullable [] var1, int var2, float @Nullable [] var3, float @Nullable [] var4, long var5, int var7, int @Nullable [] var8, int var9, boolean var10, long var11);

    public static native void invokePPPPPV(int var0, short @Nullable [] var1, int var2, float @Nullable [] var3, float @Nullable [] var4, long var5, int var7, short @Nullable [] var8, int var9, boolean var10, long var11);

    public static native boolean invokePZ(int var0, int @Nullable [] var1, boolean var2, long var3);

    public static native boolean invokePPZ(long var0, int @Nullable [] var2, long var3);

    public static native boolean invokePPPZ(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPI(int @Nullable [] var0, long var1);

    public static native int callPI(int var0, int @Nullable [] var1, long var2);

    public static native int callPI(int @Nullable [] var0, int var1, long var2);

    public static native int callPI(int var0, int var1, int @Nullable [] var2, long var3);

    public static native int callPI(int var0, int @Nullable [] var1, int var2, long var3);

    public static native int callPI(int var0, int var1, int var2, int @Nullable [] var3, long var4);

    public static native int callPI(int var0, int var1, int var2, int var3, float @Nullable [] var4, long var5);

    public static native int callPI(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native int callPPI(long var0, int @Nullable [] var2, long var3);

    public static native int callPPI(long var0, long @Nullable [] var2, long var3);

    public static native int callPPI(int @Nullable [] var0, long var1, long var3);

    public static native int callPPI(int var0, long var1, int @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, double @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, float @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, int @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, long @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, short @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native int callPPI(long var0, int var2, int var3, long @Nullable [] var4, long var5);

    public static native int callPPI(int var0, long var1, int var3, int var4, float var5, int @Nullable [] var6, long var7);

    public static native int callPJPI(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPJPI(long var0, long var2, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, long var2, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, int @Nullable [] var2, long var3, long var5);

    public static native int callPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native int callPPPI(long var0, long @Nullable [] var2, long @Nullable [] var3, long var4);

    public static native int callPPPI(int @Nullable [] var0, long var1, int @Nullable [] var3, long var4);

    public static native int callPJPI(long var0, int var2, long var3, int @Nullable [] var5, long var6);

    public static native int callPJPI(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, double @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, float @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, int @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, long @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, short @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, int var2, int @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, long var2, int @Nullable [] var4, int var5, long var6);

    public static native int callPPJI(long var0, int var2, long @Nullable [] var3, int var4, long var5, long var7);

    public static native int callPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, long var7);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int @Nullable [] var5, float @Nullable [] var6, long var7);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, long @Nullable [] var5, long var6);

    public static native int callPPNPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native int callPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native int callPJPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long var5, long @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, int var2, int @Nullable [] var3, long var4, long var6, long var8);

    public static native int callPPPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, long var2, int var4, long @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int @Nullable [] var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long @Nullable [] var2, int var3, long var4, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPI(long var0, int var2, int var3, long var4, long @Nullable [] var6, long var7, long var9);

    public static native int callPPPPI(long var0, int var2, int var3, long @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, int var6, long var7, int @Nullable [] var9, long var10);

    public static native int callPPPPI(long var0, int var2, long var3, int var5, int var6, long var7, int @Nullable [] var9, long var10);

    public static native int callPPPPI(int var0, int @Nullable [] var1, long @Nullable [] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20, int @Nullable [] var21, long @Nullable [] var22, long var23);

    public static native int callPJPPPI(long var0, long var2, long var4, long var6, long @Nullable [] var8, long var9);

    public static native int callPPJPPI(long var0, long var2, long var4, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native int callPPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5, long var7);

    public static native int callPJPPPI(long var0, long var2, int var4, long var5, long var7, long @Nullable [] var9, long var10);

    public static native int callPPPPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8, long var10);

    public static native int callPPPPPI(long var0, long var2, int var4, long var5, long @Nullable [] var7, long var8, long var10);

    public static native int callPPPPPI(long var0, long var2, int var4, int @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPPI(long var0, long var2, long var4, int @Nullable [] var6, int var7, int @Nullable [] var8, long var9);

    public static native int callPPPPPI(long var0, int @Nullable [] var2, float @Nullable [] var3, int var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6, long var8);

    public static native int callPPPPPI(long var0, int var2, long var3, long @Nullable [] var5, int var6, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, int var2, long @Nullable [] var3, int var4, long var5, long var7, long var9, long var11);

    public static native int callPJPPJI(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9, int var11, long var12);

    public static native int callPJPPJI(long var0, long var2, int var4, int var5, long var6, long @Nullable [] var8, long var9, int var11, long var12);

    public static native int callPJJJJPI(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native int callPPPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJJPPPI(long var0, long var2, long var4, int var6, long var7, long var9, long @Nullable [] var11, long var12);

    public static native int callPPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, int @Nullable [] var11, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, float @Nullable [] var4, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, double @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, float @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, int @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, short @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPJPPPPPI(long var0, long @Nullable [] var2, long var3, long var5, long var7, int var9, long var10, long var12, long var14, long var16);

    public static native int callPPJPPPPPI(long var0, long @Nullable [] var2, long var3, long var5, long var7, int var9, long var10, int @Nullable [] var12, long var13, long var15);

    public static native int callPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, int var12, int @Nullable [] var13, int @Nullable [] var14, long var15, long var17);

    public static native int callPPPPPPPPPI(long var0, long var2, long var4, float @Nullable [] var6, long var7, long var9, int var11, int @Nullable [] var12, int @Nullable [] var13, long var14, long var16);

    public static native int callPPPPPPPPPI(long var0, long var2, long var4, int @Nullable [] var6, long var7, long var9, int var11, int @Nullable [] var12, int @Nullable [] var13, long var14, long var16);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, double @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, float @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, int @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, short @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int var14, int @Nullable [] var15, int @Nullable [] var16, long var17, long var19);

    public static native int callPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, int var7, long var8, long var10, long var12, int var14, int @Nullable [] var15, int @Nullable [] var16, long var17, long var19);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, double @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, float @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, int @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, short @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPPPI(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20, int var22, int @Nullable [] var23, int @Nullable [] var24, long var25, long var27);

    public static native long callPP(int @Nullable [] var0, long var1);

    public static native long callPPP(long var0, int @Nullable [] var2, long var3);

    public static native long callPPP(int var0, long var1, int @Nullable [] var3, long var4);

    public static native long callPPP(long var0, int var2, int @Nullable [] var3, long var4);

    public static native long callPPP(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native long callPPP(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(long var0, long var2, int @Nullable [] var4, long var5);

    public static native long callPPPP(long var0, long @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native long callPJPP(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(int var0, long var1, long @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native long callPPPP(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native long callPPPP(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(long var0, long var2, int var4, int var5, int @Nullable [] var6, long var7);

    public static native long callPJPP(long var0, long var2, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native long callPPJPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPNPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, long var2, long @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native long callPJPPP(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native long callPJPPP(long var0, long var2, int @Nullable [] var4, int var5, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, int var2, long var3, long var5, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long var2, long var4, int var6, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long @Nullable [] var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native long callPJPPP(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9);

    public static native long callPJPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, long var9);

    public static native long callPJPPPP(long var0, long var2, long var4, double @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, float @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, short @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPPPPPP(long var0, int var2, long var3, long var5, long var7, int @Nullable [] var9, long var10);

    public static native long callPPPPPP(long var0, long var2, long @Nullable [] var4, long var5, int var7, int @Nullable [] var8, long var9);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, double @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, float @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, short @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, float @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, short @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, int @Nullable [] var9, long var10);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, double @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, float @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, int @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, short @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPPJPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPPPPPPP(long var0, int var2, long var3, long var5, long var7, int @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, long var9, int @Nullable [] var11, long var12);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, float @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, int @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, short @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPPPPPPP(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12, int @Nullable [] var14, long var15);

    public static native long callPPPPPPPP(int var0, int @Nullable [] var1, long @Nullable [] var2, int var3, int var4, int var5, int var6, long var7, long var9, long var11, int var13, int @Nullable [] var14, long @Nullable [] var15, long var16);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int @Nullable [] var14, long var15);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, float @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, int @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, short @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPPJPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, int var11, long var12, long var14, int @Nullable [] var16, long var17);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, int @Nullable [] var18, long var19);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, float @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, int @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, short @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPPJPPPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, int var15, long var16, long var18, int @Nullable [] var20, long var21);

    public static native void callPV(double @Nullable [] var0, long var1);

    public static native void callPV(float @Nullable [] var0, long var1);

    public static native void callPV(int @Nullable [] var0, long var1);

    public static native void callPV(short @Nullable [] var0, long var1);

    public static native void callPV(int var0, double @Nullable [] var1, long var2);

    public static native void callPV(int var0, float @Nullable [] var1, long var2);

    public static native void callPV(int var0, int @Nullable [] var1, long var2);

    public static native void callPV(int var0, long @Nullable [] var1, long var2);

    public static native void callPV(int var0, short @Nullable [] var1, long var2);

    public static native void callPV(int var0, int var1, double @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, float @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, long @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, short @Nullable [] var2, long var3);

    public static native void callPV(int var0, int @Nullable [] var1, int var2, long var3);

    public static native void callPV(int var0, int var1, int var2, double @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, float @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, int @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, long @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, short @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, double @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, float @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, int @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPV(int var0, int @Nullable [] var1, int var2, int var3, long var4);

    public static native void callPV(int var0, int var1, int var2, int var3, double @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, float @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, long @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, short @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, boolean var3, double @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, boolean var3, float @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int @Nullable [] var3, boolean var4, long var5);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, int var4, long var5);

    public static native void callPV(int var0, boolean var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, double @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, short @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, boolean var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, short @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, double @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, int var4, int var5, int var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, short @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, double @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, double var7, double var9, int var11, int var12, double @Nullable [] var13, long var14);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, float var5, float var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, double @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, short @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, double @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, float @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, short @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, double @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, float @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, short @Nullable [] var11, long var12);

    public static native void callPPV(long var0, float @Nullable [] var2, long var3);

    public static native void callPPV(long var0, int @Nullable [] var2, long var3);

    public static native void callPPV(double @Nullable [] var0, double @Nullable [] var1, long var2);

    public static native void callPPV(float @Nullable [] var0, float @Nullable [] var1, long var2);

    public static native void callPPV(int @Nullable [] var0, int @Nullable [] var1, long var2);

    public static native void callPPV(short @Nullable [] var0, short @Nullable [] var1, long var2);

    public static native void callPPV(int var0, long var1, int @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int @Nullable [] var1, float @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int @Nullable [] var1, long @Nullable [] var2, long var3);

    public static native void callPPV(long var0, int var2, float @Nullable [] var3, long var4);

    public static native void callPPV(long var0, int var2, int @Nullable [] var3, long var4);

    public static native void callPPV(int @Nullable [] var0, int var1, int @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int var1, long var2, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long var3, long var5);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long @Nullable [] var3, long var4);

    public static native void callPPV(int var0, long var1, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, long var1, double @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, float @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, int @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, long @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, short @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPPV(int var0, long @Nullable [] var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPPV(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, int var2, long var3, int @Nullable [] var5, long var6);

    public static native void callPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPV(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, long var2, int @Nullable [] var4, int var5, long var6);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, int var2, long var3, int var5, long var6);

    public static native void callPPV(int var0, int @Nullable [] var1, int var2, int @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, long var2, int var4, int var5, long var6);

    public static native void callPPV(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5, long var7);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long var3, int var5, int var6, long var7);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, float var6, float var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPJPV(long var0, long var2, long @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, long var2, int @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int @Nullable [] var2, long var3, long var5);

    public static native void callPJPV(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, double @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, float @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, long @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, short @Nullable [] var5, long var6);

    public static native void callPPPV(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(long var0, int var2, int @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int var2, long @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int var1, long var2, long var4, double @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, float @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, short @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, long var3, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(int var0, long var1, int var3, int @Nullable [] var4, long var5, long var7);

    public static native void callPPPV(int var0, long var1, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long var6);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, long @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, short @Nullable [] var7, long var8);

    public static native void callPPJV(long var0, int var2, long @Nullable [] var3, int var4, long var5, int var7, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int @Nullable [] var1, int var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPPPV(long var0, int var2, long var3, int var5, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, int var5, int @Nullable [] var6, float @Nullable [] var7, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native void callPPPV(long var0, int var2, int var3, int var4, int var5, int var6, int @Nullable [] var7, long var8, long var10);

    public static native void callPJPPV(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native void callPPPPV(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native void callPJJPV(long var0, int var2, long var3, long var5, long @Nullable [] var7, long var8);

    public static native void callPPPPV(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8);

    public static native void callPPPPV(long @Nullable [] var0, int @Nullable [] var1, int @Nullable [] var2, int @Nullable [] var3, int var4, long var5);

    public static native void callPPPPV(int var0, long var1, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int var6, long var7);

    public static native void callPPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, int @Nullable [] var7, long @Nullable [] var8, long var9);

    public static native void callPPPPV(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6, long var8);

    public static native void callPPPPV(int var0, int var1, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int var7, long var8);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, long @Nullable [] var7, int var8, int @Nullable [] var9, long var10);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, double @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, float @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, int @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, long @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, short @Nullable [] var8, long var9);

    public static native void callPPPPPV(long var0, int var2, long var3, long @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native void callPPPPPV(int var0, int var1, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native void callPPPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long @Nullable [] var7, long var8);

    public static native void callPPPPPV(long var0, int var2, long @Nullable [] var3, int var4, int var5, int var6, long var7, int var9, long var10, int var12, long var13, long var15);

    public static native void callPPPPPPPV(int var0, int var1, int var2, long var3, int var5, long var6, int @Nullable [] var8, int @Nullable [] var9, int @Nullable [] var10, int @Nullable [] var11, long @Nullable [] var12, long var13);

    public static native boolean callPPZ(int var0, int @Nullable [] var1, long var2, long var4);

    public static native boolean callPPPPZ(int var0, int var1, int var2, float var3, float @Nullable [] var4, float @Nullable [] var5, float @Nullable [] var6, float @Nullable [] var7, long var8);

    static {
        Library.initialize();
    }
}

