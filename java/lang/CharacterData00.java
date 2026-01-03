/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

class CharacterData00
extends CharacterData {
    static final CharacterData00 instance = new CharacterData00();
    static final char[][][] charMap;
    static final char[] X;
    static final char[] Y;
    static final int[] A;
    static final String A_DATA = "\u4800\u100f\u4800\u100f\u4800\u100f\u5800\u400f\u5000\u400f\u5800\u400f\u6000\u400f\u5000\u400f\u5000\u400f\u5000\u400f\u6000\u400c\u6800\u0018\u6800\u0018\u2800\u0018\u2800\u601a\u2800\u0018\u6800\u0018\u6800\u0018\ue800\u0015\ue800\u0016\u6800\u0018\u2000\u0019\u3800\u0018\u2000\u0014\u3800\u0018\u3800\u0018\u1800\u3609\u1800\u3609\u3800\u0018\u6800\u0018\ue800\u0019\u6800\u0019\ue800\u0019\u6800\u0018\u6800\u0018\u0082\u7fe1\u0082\u7fe1\u0082\u7fe1\u0082\u7fe1\ue800\u0015\u6800\u0018\ue800\u0016\u6800\u001b\u6800\u5017\u6800\u001b\u0081\u7fe2\u0081\u7fe2\u0081\u7fe2\u0081\u7fe2\ue800\u0015\u6800\u0019\ue800\u0016\u6800\u0019\u4800\u100f\u4800\u100f\u5000\u100f\u3800\f\u6800\u0018\u2800\u601a\u2800\u601a\u6800\u001c\u6800\u0018\u6800\u001b\u6800\u001c\u0000\u7005\ue800\u001d\u6800\u0019\u4800\u1010\u6800\u001c\u6800\u001b\u2800\u001c\u2800\u0019\u1800\u060b\u1800\u060b\u6800\u001b\u07fd\u7002\u6800\u0018\u6800\u0018\u6800\u001b\u1800\u050b\u0000\u7005\ue800\u001e\u6800\u080b\u6800\u080b\u6800\u080b\u6800\u0018\u0082\u7001\u0082\u7001\u0082\u7001\u6800\u0019\u0082\u7001\u07fd\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u6800\u0019\u0081\u7002\u061d\u7002\u0006\u7001\u0005\u7002\u07ff\uf001\u03a1\u7002\u0000\u7002\u0006\u7001\u0005\u7002\u0006\u7001\u0005\u7002\u07fd\u7002\u061e\u7001\u0006\u7001\u04f5\u7002\u034a\u7001\u033a\u7001\u0006\u7001\u0005\u7002\u0336\u7001\u0336\u7001\u0006\u7001\u0005\u7002\u0000\u7002\u013e\u7001\u032a\u7001\u032e\u7001\u0006\u7001\u033e\u7001\u067d\u7002\u034e\u7001\u0346\u7001\u0575\u7002\u0000\u7002\u034e\u7001\u0356\u7001\u05f9\u7002\u035a\u7001\u036a\u7001\u0006\u7001\u0005\u7002\u036a\u7001\u0000\u7002\u0000\u7002\u0005\u7002\u0366\u7001\u0366\u7001\u0006\u7001\u0005\u7002\u036e\u7001\u0000\u7002\u0000\u7005\u0000\u7002\u0721\u7002\u0000\u7005\u0000\u7005\n\uf001\u0007\uf003\t\uf002\n\uf001\u0007\uf003\t\uf002\t\uf002\u0006\u7001\u0005\u7002\u013d\u7002\u07fd\u7002\n\uf001\u067e\u7001\u0722\u7001\u05fa\u7001\u0000\u7002\u07fe\u7001\u0006\u7001\u0005\u7002\u0576\u7001\u07fe\u7001\u07fd\u7002\u07fd\u7002\u0006\u7001\u0005\u7002\u04f6\u7001\u0116\u7001\u011e\u7001\u07fd\u7002\u07fd\u7002\u07fd\u7002\u0349\u7002\u0339\u7002\u0000\u7002\u0335\u7002\u0335\u7002\u0000\u7002\u0329\u7002\u0000\u7002\u032d\u7002\u07fd\u7002\u0000\u7002\u0335\u7002\u07fd\u7002\u0000\u7002\u033d\u7002\u0000\u7002\u07fd\u7002\u0345\u7002\u034d\u7002\u0000\u7002\u034d\u7002\u0355\u7002\u0000\u7002\u0000\u7002\u0359\u7002\u0369\u7002\u0000\u7002\u07fd\u7002\u0369\u7002\u0369\u7002\u0115\u7002\u0365\u7002\u0365\u7002\u011d\u7002\u0000\u7002\u036d\u7002\u0000\u7002\u0000\u7005\u0000\u7002\u0000\u7004\u0000\u7004\u0000\u7004\u6800\u7004\u6800\u7004\u0000\u7004\u0000\u7004\u0000\u7004\u6800\u001b\u6800\u001b\u6800\u7004\u6800\u7004\u0000\u7004\u6800\u001b\u6800\u7004\u6800\u001b\u0000\u7004\u6800\u001b\u4000\u3006\u4000\u3006\u4000\u3006\u46b1\u3006\u7800\u0000\u7800\u0000\u0000\u7004\u05f9\u7002\u05f9\u7002\u05f9\u7002\u6800\u0018\u01d2\u7001\u009a\u7001\u6800\u0018\u0096\u7001\u0096\u7001\u0096\u7001\u7800\u0000\u0102\u7001\u7800\u0000\u00fe\u7001\u00fe\u7001\u07fd\u7002\u0082\u7001\u7800\u0000\u0082\u7001\u0099\u7002\u0095\u7002\u0095\u7002\u0095\u7002\u07fd\u7002\u0081\u7002}\u7002\u0081\u7002\u0101\u7002\u00fd\u7002\u00fd\u7002\"\u7001\u00f9\u7002\u00e5\u7002\u0000\u7001\u0000\u7001\u0000\u7001\u00bd\u7002\u00d9\u7002!\u7002\u0159\u7002\u0141\u7002\u07e5\u7002\u01d1\u7002\u0712\u7001\u0181\u7002\u6800\u0019\u0006\u7001\u0005\u7002\u07e6\u7001\u0000\u7002\u05fa\u7001\u05fa\u7001\u05fa\u7001\u0142\u7001\u0142\u7001\u0141\u7002\u0141\u7002\u0000\u001c\u4000\u3006\u4000\u0007\u4000\u0007>\u7001\u0006\u7001\u0005\u7002=\u7002\u7800\u0000\u00c2\u7001\u00c2\u7001\u00c2\u7001\u00c2\u7001\u7800\u0000\u7800\u0000\u0000\u7004\u0000\u0018\u0000\u0018\u0000\u7002\u00c1\u7002\u00c1\u7002\u00c1\u7002\u00c1\u7002\u07fd\u7002\u0000\u7002\u0000\u0018\u6800\u0014\u7800\u0000\u7800\u0000\u6800\u001c\u6800\u001c\u2800\u601a\u7800\u0000\u4000\u3006\u4000\u3006\u4000\u3006\u0800\u0014\u4000\u3006\u0800\u0018\u4000\u3006\u4000\u3006\u0800\u0018\u0800\u7005\u0800\u7005\u0800\u7005\u7800\u0000\u7800\u0000\u0800\u7005\u0800\u7005\u0800\u0018\u0800\u0018\u7800\u0000\u3000\u1010\u3000\u1010\u6800\u0019\u6800\u0019\u1000\u0019\u2800\u0018\u2800\u0018\u1000\u601a\u3800\u0018\u1000\u0018\u6800\u001c\u6800\u001c\u4000\u3006\u1000\u0018\u1000\u1010\u1000\u0018\u1000\u0018\u1000\u0018\u1000\u7005\u1000\u7005\u1000\u7004\u1000\u7005\u1000\u7005\u4000\u3006\u4000\u3006\u4000\u3006\u3000\u3409\u3000\u3409\u2800\u0018\u3000\u0018\u3000\u0018\u1000\u0018\u4000\u3006\u1000\u7005\u1000\u0018\u1000\u7005\u4000\u3006\u3000\u1010\u6800\u001c\u4000\u3006\u4000\u3006\u1000\u7004\u1000\u7004\u4000\u3006\u4000\u3006\u6800\u001c\u1800\u3609\u1800\u3609\u1000\u7005\u1000\u001c\u1000\u001c\u1000\u7005\u7800\u0000\u1000\u1010\u4000\u3006\u7800\u0000\u7800\u0000\u1000\u7005\u0800\u3409\u0800\u3409\u0800\u7005\u4000\u3006\u0800\u7004\u0800\u7004\u0800\u7004\u7800\u0000\u0800\u601a\u0800\u601a\u0800\u7004\u4000\u3006\u4000\u3006\u4000\u3006\u0800\u0018\u0800\u0018\u1000\u7005\u7800\u0000\u1000\u001b\u1000\u7005\u1000\u7005\u1000\u7004\u3000\u1010\u4000\u3006\u4000\u3006\u0000\u3008\u4000\u3006\u0000\u7005\u0000\u3008\u0000\u3008\u0000\u3008\u4000\u3006\u0000\u3008\u4000\u3006\u0000\u7005\u4000\u3006\u0000\u3749\u0000\u3749\u0000\u0018\u0000\u7004\u0000\u7005\u4000\u3006\u7800\u0000\u0000\u7005\u0000\u7005\u7800\u0000\u4000\u3006\u7800\u0000\u7800\u0000\u0000\u3008\u0000\u3008\u7800\u0000\u0000\u080b\u0000\u080b\u0000\u080b\u0000\u06eb\u0000\u001c\u2800\u601a\u0000\u7005\u0000\u0018\u7800\u0000\u4000\u3006\u0000\u0018\u7800\u0000\u0000\u0018\u2800\u601a\u0000\u001c\u0000\u7005\u4000\u3006\u0000\u7005\u0000\u074b\u0000\u080b\u0000\u080b\u6800\u001c\u6800\u001c\u7800\u0000\u7800\u0000\u0000\u0018\u6800\u050b\u6800\u050b\u6800\u04ab\u6800\u04ab\u6800\u04ab\u0000\u001c\u0000\u0018\u0000\u7005\u0000\u3008\u0000\u3006\u0000\u3006\u0000\u3008\u0000\u7005\u0000\u3008\u0000\u7005\u0000\u001c\u0000\u080b\u0000\u7005\u0000\u080b\u0000\u001c\u7800\u0000\u2800\u601a\u0000\u7004\u4000\u3006\u4000\u3006\u0000\u0018\u0000\u3609\u0000\u3609\u0000\u7004\u7800\u0000\u0000\u001c\u0000\u001c\u0000\u0018\u0000\u001c\u0000\u3409\u0000\u3409\u0000\u3008\u0000\u3008\u4000\u3006\u0000\u001c\u0000\u001c\u7800\u0000\u0000\u001c\u0000\u0018\u4000\u3006\u0000\u3008\u0000\u3008\u0000\u7005\u07fe\u7001\u07fe\u7001\u7800\u0000\u07fe\u7001\u07fd\uf002\u07fd\uf002\u07fd\uf002\u0000\u0018\u0000\u7004\u07fd\uf002\u0000\u0018\u0000\u070b\u0000\u070b\u0000\u070b\u0000\u070b\u0000\u042b\u0000\u054b\u0000\u080b\u0000\u080b\u7800\u0000\"\u7001\"\u7001!\u7002!\u7002\u6800\u0014\u0000\u7005\u6000\u400c\u0000\u7005\u0000\u7005\ue800\u0015\ue800\u0016\u7800\u0000\u0000\u746a\u0000\u746a\u0000\u746a\u0000\u7005\u4000\u3006\u0000\u3008\u0000\u3008\u0000\u0018\u6800\u060b\u6800\u060b\u6800\u0014\u6800\u0018\u6800\u0018\u4000\u3006\u4800\u1010\u4000\u3006\u0000\u7005\u0000\u7004\u0000\u7005\u4000\u3006\u4000\u3006\u0000\u7005\u0000\u04eb\u7800\u0000\u4000\u0007\u4000\u3006\u0000\u3008\u0000\u7005\u0000\u3008\u4000\u3006\u07fd\u7002\u7800\u0000\u07fe\u7001\u7800\u0000\u0000\u7005\u0000\u3008\u0000\u7004\u0000\u7002\u0000\u7004\u07fd\u7002\u0000\u7002\u0000\u7004\u07fd\u7002\u00ed\u7002\u07fe\u7001\u0000\u7002\u07e1\u7002\u07e1\u7002\u07e2\u7001\u07e2\u7001\u07fd\u7002\u07e1\u7002\u7800\u0000\u07e2\u7001\u06d9\u7002\u06d9\u7002\u06a9\u7002\u06a9\u7002\u0671\u7002\u0671\u7002\u0601\u7002\u0601\u7002\u0641\u7002\u0641\u7002\u0609\u7002\u0609\u7002\u07ff\uf003\u07ff\uf003\u06da\u7001\u06da\u7001\u07ff\uf003\u6800\u001b\u07fd\u7002\u6800\u001b\u06aa\u7001\u06aa\u7001\u0672\u7001\u0672\u7001\u7800\u0000\u6800\u001b\u07fd\u7002\u07e5\u7002\u0642\u7001\u0642\u7001\u07e6\u7001\u6800\u001b\u0602\u7001\u0602\u7001\u060a\u7001\u060a\u7001\u6800\u001b\u7800\u0000\u6000\u400c\u6000\u400c\u6000\u400c\u6000\f\u6000\u400c\u4800\u1010\u4800\u1010\u4800\u1010\u0000\u1010\u0800\u1010\u6800\u0014\u6800\u0014\u6800\u001d\u6800\u001e\u6800\u0015\u6800\u001d\u6000\u400d\u5000\u400e\u7800\u1010\u7800\u1010\u7800\u1010\u3800\f\u2800\u0018\u2800\u0018\u2800\u0018\u6800\u0018\u6800\u0018\ue800\u001d\ue800\u001e\u6800\u0018\u6800\u0018\u6800\u0018\u6800\u0018\u6800\u5017\u6800\u5017\u6800\u0018\u3800\u0019\ue800\u0015\ue800\u0016\u6800\u0018\u6800\u0018\u6800\u0018\u6800\u0019\u6800\u0018\u6800\u0018\u6000\u400c\u4800\u1010\u4800\u1010\u4800\u1010\u7800\u0000\u1800\u060b\u0000\u7004\u2000\u0019\u2000\u0019\u6800\u0019\ue800\u0015\ue800\u0016\u0000\u7004\u1800\u040b\u1800\u040b\u0000\u7004\u7800\u0000\u2800\u601a\u7800\u0000\u4000\u3006\u4000\u0007\u4000\u0007\u4000\u3006\u4000\u0007\u4000\u0007\u0000\u7001\u6800\u001c\u6800\u001c\u0000\u7001\u0000\u7002\u0000\u7001\u0000\u7001\u0000\u7002\u6800\u0019\u0000\u7001\u6800\u001c\u6800\u001c\u07fe\u7001\u6800\u001c\u2800\u001c\u0000\u7002r\u7001\u0000\u7001\u0000\u7005\u0000\u7002\u6800\u0019\u0000\u7001\u6800\u001c\u6800\u0019q\u7002\u0000\u001cB\u742aB\u742aB\u780aB\u780aA\u762aA\u762aA\u780aA\u780a\u0000\u780a\u0000\u780a\u0000\u780a\u0006\u7001\u0005\u7002\u0000\u742a\u0000\u780a\u6800\u06eb\u6800\u0019\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u0019\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u0019\ue800\u0019\ue800\u0019\ue800\u0019\u2000\u0019\u2800\u0019\u6800\u001c\u6800\u001c\u6800\u001c\ue800\u0015\ue800\u0016\u6800\u001c\u0000\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u0000\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u042b\u6800\u042b\u6800\u05ab\u6800\u05ab\u1800\u072b\u1800\u072bj\u001cj\u001cj\u001cj\u001ci\u001ci\u001c\u6800\u06cb\u6800\u040b\u6800\u040b\u6800\u040b\u6800\u040b\u6800\u058b\u6800\u058b\u6800\u058b\u6800\u058b\u6800\u042b\u6800\u001c\u6800\u0019\u6800\u0019\u6800\u0019\u6800\u0019\u6800\u0019\u6800\u0019\u6800\u0019\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u0019\u0000\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u056b\u6800\u056b\u6800\u06eb\u6800\u06eb\ue800\u0019\ue800\u0015\ue800\u0016\u6800\u0019\u6800\u0019\u6800\u0019\ue800\u0016\ue800\u0015\ue800\u001c\u6800\u001c\u0005\u7002\u07fe\u7001\u0000\u7002\u6800\u001c\u6800\u001c\u0006\u7001\u0005\u7002\u4000\u3006\u7800\u0000\u6800\u0018\u6800\u0018\u6800\u080b\u7800\u0000\u07fd\u7002\ue800\u001d\ue800\u001e\u6800\u0018\u6800\u0014\u6800\u0018\u6800\u7004\u6800\u0015\u6800\u0018\u6800\u0018\ue800\u0015\ue800\u0016\u6800\u0014\u6800\u001c\u0000\u7004\u0000\u7005\u0000\u772a\u6800\u0014\u6800\u0015\u6800\u0016\u6800\u0016\u6800\u001c\u0000\u740a\u0000\u740a\u0000\u740a\u6800\u0014\u0000\u7004\u0000\u764a\u0000\u776a\u0000\u748a\u0000\u7004\u0000\u7005\u6800\u0018\u4000\u3006\u6800\u001b\u6800\u001b\u0000\u7004\u0000\u7004\u0000\u7005\u0000\u7005\u6800\u0018\u0000\u05eb\u0000\u05eb\u0000\u042b\u0000\u042b\u0000\u044b\u0000\u056b\u0000\u068b\u0000\u080b\u6800\u001c\u6800\u048b\u6800\u048b\u6800\u048b\u0000\u001c\u0000\u001c\u0000\u001c\u6800\u080b\u0000\u7005\u0000\u7005\u0000\u7004\u6800\u0018\u4000\u0007\u6800\u0018\u6800\u0018\u6800\u7004\u0000\u776a\u0000\u776a\u0000\u776a\u0000\u762a\u6800\u001b\u6800\u7004\u6800\u7004\u0000\u001b\u0000\u001b\u0006\u7001\u0741\u7002\u0000\u7002\u0742\u7001\u07fe\u7001\u0005\u7002\u7800\u0000\u7800\u0000\u0000\u7002\u0000\u7004\u0006\u7001\u0005\u7002\u0000\u7005\u2800\u601a\u2800\u001c\u0000\u3008\u0000\u3008\u0000\u7004\u0000\u3008\u0000\u7002\u0000\u001b\u0000\u3008\u0000\u0018\u0000\u0013\u0000\u0013\u0000\u0012\u0000\u0012\u0000\u7005\u0000\u7705\u0000\u7005\u0000\u76e5\u0000\u7545\u0000\u7005\u0000\u75c5\u0000\u7005\u0000\u7005\u0000\u76a5\u0000\u7005\u0000\u7665\u0000\u7005\u0000\u75a5\u4000\u3006\u0800\u7005\u0800\u7005\u2000\u0019\u1000\u001b\u1000\u001b\u1000\u001b\u7800\u0000\u6800\u0016\u6800\u0015\u1000\u601a\u6800\u001c\u4000\u3006\u4000\u3006\u6800\u0018\u6800\u0015\u6800\u0016\u6800\u0018\u6800\u0014\u6800\u5017\u6800\u5017\u6800\u0015\u6800\u5017\u6800\u5017\u3800\u0018\u7800\u0000\u6800\u0018\u3800\u0018\u6800\u0014\ue800\u0015\ue800\u0016\u2800\u0018\u2000\u0019\u2000\u0014\u6800\u0019\u7800\u0000\u6800\u0018\u2800\u601a\u7800\u0000\u4800\u1010\u6800\u0018\u2800\u0018\u6800\u0018\u2000\u0019\u6800\u0019\u6800\u001b\u7800\u0000\u6800\u1010\u6800\u1010\u6800\u1010";
    static final char[] B;

    @Override
    int getProperties(int ch) {
        char offset = (char)ch;
        int props = A[Y[X[offset >> 5] | offset >> 1 & 0xF] | offset & '\u0001'];
        return props;
    }

    int getPropertiesEx(int ch) {
        char offset = (char)ch;
        char props = B[Y[X[offset >> 5] | offset >> 1 & 0xF] | offset & '\u0001'];
        return props;
    }

    @Override
    int getType(int ch) {
        int props = this.getProperties(ch);
        return props & 0x1F;
    }

    @Override
    boolean isOtherAlphabetic(int ch) {
        int props = this.getPropertiesEx(ch);
        return (props & 4) != 0;
    }

    @Override
    boolean isIdeographic(int ch) {
        int props = this.getPropertiesEx(ch);
        return (props & 8) != 0;
    }

    @Override
    boolean isJavaIdentifierStart(int ch) {
        int props = this.getProperties(ch);
        return (props & 0x7000) >= 20480;
    }

    @Override
    boolean isJavaIdentifierPart(int ch) {
        int props = this.getProperties(ch);
        return (props & 0x3000) != 0;
    }

    @Override
    boolean isUnicodeIdentifierStart(int ch) {
        return (this.getPropertiesEx(ch) & 0x10) != 0 || ch == 11823;
    }

    @Override
    boolean isUnicodeIdentifierPart(int ch) {
        return (this.getPropertiesEx(ch) & 0x20) != 0 || this.isIdentifierIgnorable(ch) || ch == 11823;
    }

    @Override
    boolean isIdentifierIgnorable(int ch) {
        int props = this.getProperties(ch);
        return (props & 0x7000) == 4096;
    }

    @Override
    boolean isEmoji(int ch) {
        return (this.getPropertiesEx(ch) & 0x40) != 0;
    }

    @Override
    boolean isEmojiPresentation(int ch) {
        return (this.getPropertiesEx(ch) & 0x80) != 0;
    }

    @Override
    boolean isEmojiModifier(int ch) {
        return (this.getPropertiesEx(ch) & 0x100) != 0;
    }

    @Override
    boolean isEmojiModifierBase(int ch) {
        return (this.getPropertiesEx(ch) & 0x200) != 0;
    }

    @Override
    boolean isEmojiComponent(int ch) {
        return (this.getPropertiesEx(ch) & 0x400) != 0;
    }

    @Override
    boolean isExtendedPictographic(int ch) {
        return (this.getPropertiesEx(ch) & 0x800) != 0;
    }

    @Override
    int toLowerCase(int ch) {
        int mapChar = ch;
        int val = this.getProperties(ch);
        if ((val & 0x20000) != 0) {
            if ((val & 0x7FC0000) == 133955584) {
                switch (ch) {
                    case 304: {
                        mapChar = 105;
                        break;
                    }
                    case 570: {
                        mapChar = 11365;
                        break;
                    }
                    case 574: {
                        mapChar = 11366;
                        break;
                    }
                    case 1042: {
                        mapChar = 7296;
                        break;
                    }
                    case 1044: {
                        mapChar = 7297;
                        break;
                    }
                    case 1054: {
                        mapChar = 7298;
                        break;
                    }
                    case 1057: {
                        mapChar = 7299;
                        break;
                    }
                    case 1066: {
                        mapChar = 7302;
                        break;
                    }
                    case 1122: {
                        mapChar = 7303;
                        break;
                    }
                    case 4256: {
                        mapChar = 11520;
                        break;
                    }
                    case 4257: {
                        mapChar = 11521;
                        break;
                    }
                    case 4258: {
                        mapChar = 11522;
                        break;
                    }
                    case 4259: {
                        mapChar = 11523;
                        break;
                    }
                    case 4260: {
                        mapChar = 11524;
                        break;
                    }
                    case 4261: {
                        mapChar = 11525;
                        break;
                    }
                    case 4262: {
                        mapChar = 11526;
                        break;
                    }
                    case 4263: {
                        mapChar = 11527;
                        break;
                    }
                    case 4264: {
                        mapChar = 11528;
                        break;
                    }
                    case 4265: {
                        mapChar = 11529;
                        break;
                    }
                    case 4266: {
                        mapChar = 11530;
                        break;
                    }
                    case 4267: {
                        mapChar = 11531;
                        break;
                    }
                    case 4268: {
                        mapChar = 11532;
                        break;
                    }
                    case 4269: {
                        mapChar = 11533;
                        break;
                    }
                    case 4270: {
                        mapChar = 11534;
                        break;
                    }
                    case 4271: {
                        mapChar = 11535;
                        break;
                    }
                    case 4272: {
                        mapChar = 11536;
                        break;
                    }
                    case 4273: {
                        mapChar = 11537;
                        break;
                    }
                    case 4274: {
                        mapChar = 11538;
                        break;
                    }
                    case 4275: {
                        mapChar = 11539;
                        break;
                    }
                    case 4276: {
                        mapChar = 11540;
                        break;
                    }
                    case 4277: {
                        mapChar = 11541;
                        break;
                    }
                    case 4278: {
                        mapChar = 11542;
                        break;
                    }
                    case 4279: {
                        mapChar = 11543;
                        break;
                    }
                    case 4280: {
                        mapChar = 11544;
                        break;
                    }
                    case 4281: {
                        mapChar = 11545;
                        break;
                    }
                    case 4282: {
                        mapChar = 11546;
                        break;
                    }
                    case 4283: {
                        mapChar = 11547;
                        break;
                    }
                    case 4284: {
                        mapChar = 11548;
                        break;
                    }
                    case 4285: {
                        mapChar = 11549;
                        break;
                    }
                    case 4286: {
                        mapChar = 11550;
                        break;
                    }
                    case 4287: {
                        mapChar = 11551;
                        break;
                    }
                    case 4288: {
                        mapChar = 11552;
                        break;
                    }
                    case 4289: {
                        mapChar = 11553;
                        break;
                    }
                    case 4290: {
                        mapChar = 11554;
                        break;
                    }
                    case 4291: {
                        mapChar = 11555;
                        break;
                    }
                    case 4292: {
                        mapChar = 11556;
                        break;
                    }
                    case 4293: {
                        mapChar = 11557;
                        break;
                    }
                    case 4295: {
                        mapChar = 11559;
                        break;
                    }
                    case 4301: {
                        mapChar = 11565;
                        break;
                    }
                    case 5024: {
                        mapChar = 43888;
                        break;
                    }
                    case 5025: {
                        mapChar = 43889;
                        break;
                    }
                    case 5026: {
                        mapChar = 43890;
                        break;
                    }
                    case 5027: {
                        mapChar = 43891;
                        break;
                    }
                    case 5028: {
                        mapChar = 43892;
                        break;
                    }
                    case 5029: {
                        mapChar = 43893;
                        break;
                    }
                    case 5030: {
                        mapChar = 43894;
                        break;
                    }
                    case 5031: {
                        mapChar = 43895;
                        break;
                    }
                    case 5032: {
                        mapChar = 43896;
                        break;
                    }
                    case 5033: {
                        mapChar = 43897;
                        break;
                    }
                    case 5034: {
                        mapChar = 43898;
                        break;
                    }
                    case 5035: {
                        mapChar = 43899;
                        break;
                    }
                    case 5036: {
                        mapChar = 43900;
                        break;
                    }
                    case 5037: {
                        mapChar = 43901;
                        break;
                    }
                    case 5038: {
                        mapChar = 43902;
                        break;
                    }
                    case 5039: {
                        mapChar = 43903;
                        break;
                    }
                    case 5040: {
                        mapChar = 43904;
                        break;
                    }
                    case 5041: {
                        mapChar = 43905;
                        break;
                    }
                    case 5042: {
                        mapChar = 43906;
                        break;
                    }
                    case 5043: {
                        mapChar = 43907;
                        break;
                    }
                    case 5044: {
                        mapChar = 43908;
                        break;
                    }
                    case 5045: {
                        mapChar = 43909;
                        break;
                    }
                    case 5046: {
                        mapChar = 43910;
                        break;
                    }
                    case 5047: {
                        mapChar = 43911;
                        break;
                    }
                    case 5048: {
                        mapChar = 43912;
                        break;
                    }
                    case 5049: {
                        mapChar = 43913;
                        break;
                    }
                    case 5050: {
                        mapChar = 43914;
                        break;
                    }
                    case 5051: {
                        mapChar = 43915;
                        break;
                    }
                    case 5052: {
                        mapChar = 43916;
                        break;
                    }
                    case 5053: {
                        mapChar = 43917;
                        break;
                    }
                    case 5054: {
                        mapChar = 43918;
                        break;
                    }
                    case 5055: {
                        mapChar = 43919;
                        break;
                    }
                    case 5056: {
                        mapChar = 43920;
                        break;
                    }
                    case 5057: {
                        mapChar = 43921;
                        break;
                    }
                    case 5058: {
                        mapChar = 43922;
                        break;
                    }
                    case 5059: {
                        mapChar = 43923;
                        break;
                    }
                    case 5060: {
                        mapChar = 43924;
                        break;
                    }
                    case 5061: {
                        mapChar = 43925;
                        break;
                    }
                    case 5062: {
                        mapChar = 43926;
                        break;
                    }
                    case 5063: {
                        mapChar = 43927;
                        break;
                    }
                    case 5064: {
                        mapChar = 43928;
                        break;
                    }
                    case 5065: {
                        mapChar = 43929;
                        break;
                    }
                    case 5066: {
                        mapChar = 43930;
                        break;
                    }
                    case 5067: {
                        mapChar = 43931;
                        break;
                    }
                    case 5068: {
                        mapChar = 43932;
                        break;
                    }
                    case 5069: {
                        mapChar = 43933;
                        break;
                    }
                    case 5070: {
                        mapChar = 43934;
                        break;
                    }
                    case 5071: {
                        mapChar = 43935;
                        break;
                    }
                    case 5072: {
                        mapChar = 43936;
                        break;
                    }
                    case 5073: {
                        mapChar = 43937;
                        break;
                    }
                    case 5074: {
                        mapChar = 43938;
                        break;
                    }
                    case 5075: {
                        mapChar = 43939;
                        break;
                    }
                    case 5076: {
                        mapChar = 43940;
                        break;
                    }
                    case 5077: {
                        mapChar = 43941;
                        break;
                    }
                    case 5078: {
                        mapChar = 43942;
                        break;
                    }
                    case 5079: {
                        mapChar = 43943;
                        break;
                    }
                    case 5080: {
                        mapChar = 43944;
                        break;
                    }
                    case 5081: {
                        mapChar = 43945;
                        break;
                    }
                    case 5082: {
                        mapChar = 43946;
                        break;
                    }
                    case 5083: {
                        mapChar = 43947;
                        break;
                    }
                    case 5084: {
                        mapChar = 43948;
                        break;
                    }
                    case 5085: {
                        mapChar = 43949;
                        break;
                    }
                    case 5086: {
                        mapChar = 43950;
                        break;
                    }
                    case 5087: {
                        mapChar = 43951;
                        break;
                    }
                    case 5088: {
                        mapChar = 43952;
                        break;
                    }
                    case 5089: {
                        mapChar = 43953;
                        break;
                    }
                    case 5090: {
                        mapChar = 43954;
                        break;
                    }
                    case 5091: {
                        mapChar = 43955;
                        break;
                    }
                    case 5092: {
                        mapChar = 43956;
                        break;
                    }
                    case 5093: {
                        mapChar = 43957;
                        break;
                    }
                    case 5094: {
                        mapChar = 43958;
                        break;
                    }
                    case 5095: {
                        mapChar = 43959;
                        break;
                    }
                    case 5096: {
                        mapChar = 43960;
                        break;
                    }
                    case 5097: {
                        mapChar = 43961;
                        break;
                    }
                    case 5098: {
                        mapChar = 43962;
                        break;
                    }
                    case 5099: {
                        mapChar = 43963;
                        break;
                    }
                    case 5100: {
                        mapChar = 43964;
                        break;
                    }
                    case 5101: {
                        mapChar = 43965;
                        break;
                    }
                    case 5102: {
                        mapChar = 43966;
                        break;
                    }
                    case 5103: {
                        mapChar = 43967;
                        break;
                    }
                    case 7312: {
                        mapChar = 4304;
                        break;
                    }
                    case 7313: {
                        mapChar = 4305;
                        break;
                    }
                    case 7314: {
                        mapChar = 4306;
                        break;
                    }
                    case 7315: {
                        mapChar = 4307;
                        break;
                    }
                    case 7316: {
                        mapChar = 4308;
                        break;
                    }
                    case 7317: {
                        mapChar = 4309;
                        break;
                    }
                    case 7318: {
                        mapChar = 4310;
                        break;
                    }
                    case 7319: {
                        mapChar = 4311;
                        break;
                    }
                    case 7320: {
                        mapChar = 4312;
                        break;
                    }
                    case 7321: {
                        mapChar = 4313;
                        break;
                    }
                    case 7322: {
                        mapChar = 4314;
                        break;
                    }
                    case 7323: {
                        mapChar = 4315;
                        break;
                    }
                    case 7324: {
                        mapChar = 4316;
                        break;
                    }
                    case 7325: {
                        mapChar = 4317;
                        break;
                    }
                    case 7326: {
                        mapChar = 4318;
                        break;
                    }
                    case 7327: {
                        mapChar = 4319;
                        break;
                    }
                    case 7328: {
                        mapChar = 4320;
                        break;
                    }
                    case 7329: {
                        mapChar = 4321;
                        break;
                    }
                    case 7330: {
                        mapChar = 4322;
                        break;
                    }
                    case 7331: {
                        mapChar = 4323;
                        break;
                    }
                    case 7332: {
                        mapChar = 4324;
                        break;
                    }
                    case 7333: {
                        mapChar = 4325;
                        break;
                    }
                    case 7334: {
                        mapChar = 4326;
                        break;
                    }
                    case 7335: {
                        mapChar = 4327;
                        break;
                    }
                    case 7336: {
                        mapChar = 4328;
                        break;
                    }
                    case 7337: {
                        mapChar = 4329;
                        break;
                    }
                    case 7338: {
                        mapChar = 4330;
                        break;
                    }
                    case 7339: {
                        mapChar = 4331;
                        break;
                    }
                    case 7340: {
                        mapChar = 4332;
                        break;
                    }
                    case 7341: {
                        mapChar = 4333;
                        break;
                    }
                    case 7342: {
                        mapChar = 4334;
                        break;
                    }
                    case 7343: {
                        mapChar = 4335;
                        break;
                    }
                    case 7344: {
                        mapChar = 4336;
                        break;
                    }
                    case 7345: {
                        mapChar = 4337;
                        break;
                    }
                    case 7346: {
                        mapChar = 4338;
                        break;
                    }
                    case 7347: {
                        mapChar = 4339;
                        break;
                    }
                    case 7348: {
                        mapChar = 4340;
                        break;
                    }
                    case 7349: {
                        mapChar = 4341;
                        break;
                    }
                    case 7350: {
                        mapChar = 4342;
                        break;
                    }
                    case 7351: {
                        mapChar = 4343;
                        break;
                    }
                    case 7352: {
                        mapChar = 4344;
                        break;
                    }
                    case 7353: {
                        mapChar = 4345;
                        break;
                    }
                    case 7354: {
                        mapChar = 4346;
                        break;
                    }
                    case 7355: {
                        mapChar = 4347;
                        break;
                    }
                    case 7356: {
                        mapChar = 4348;
                        break;
                    }
                    case 7357: {
                        mapChar = 4349;
                        break;
                    }
                    case 7358: {
                        mapChar = 4350;
                        break;
                    }
                    case 7359: {
                        mapChar = 4351;
                        break;
                    }
                    case 7838: {
                        mapChar = 223;
                        break;
                    }
                    case 8072: {
                        mapChar = 8064;
                        break;
                    }
                    case 8073: {
                        mapChar = 8065;
                        break;
                    }
                    case 8074: {
                        mapChar = 8066;
                        break;
                    }
                    case 8075: {
                        mapChar = 8067;
                        break;
                    }
                    case 8076: {
                        mapChar = 8068;
                        break;
                    }
                    case 8077: {
                        mapChar = 8069;
                        break;
                    }
                    case 8078: {
                        mapChar = 8070;
                        break;
                    }
                    case 8079: {
                        mapChar = 8071;
                        break;
                    }
                    case 8088: {
                        mapChar = 8080;
                        break;
                    }
                    case 8089: {
                        mapChar = 8081;
                        break;
                    }
                    case 8090: {
                        mapChar = 8082;
                        break;
                    }
                    case 8091: {
                        mapChar = 8083;
                        break;
                    }
                    case 8092: {
                        mapChar = 8084;
                        break;
                    }
                    case 8093: {
                        mapChar = 8085;
                        break;
                    }
                    case 8094: {
                        mapChar = 8086;
                        break;
                    }
                    case 8095: {
                        mapChar = 8087;
                        break;
                    }
                    case 8104: {
                        mapChar = 8096;
                        break;
                    }
                    case 8105: {
                        mapChar = 8097;
                        break;
                    }
                    case 8106: {
                        mapChar = 8098;
                        break;
                    }
                    case 8107: {
                        mapChar = 8099;
                        break;
                    }
                    case 8108: {
                        mapChar = 8100;
                        break;
                    }
                    case 8109: {
                        mapChar = 8101;
                        break;
                    }
                    case 8110: {
                        mapChar = 8102;
                        break;
                    }
                    case 8111: {
                        mapChar = 8103;
                        break;
                    }
                    case 8124: {
                        mapChar = 8115;
                        break;
                    }
                    case 8140: {
                        mapChar = 8131;
                        break;
                    }
                    case 8188: {
                        mapChar = 8179;
                        break;
                    }
                    case 8486: {
                        mapChar = 969;
                        break;
                    }
                    case 8490: {
                        mapChar = 107;
                        break;
                    }
                    case 8491: {
                        mapChar = 229;
                        break;
                    }
                    case 11362: {
                        mapChar = 619;
                        break;
                    }
                    case 11363: {
                        mapChar = 7549;
                        break;
                    }
                    case 11364: {
                        mapChar = 637;
                        break;
                    }
                    case 11373: {
                        mapChar = 593;
                        break;
                    }
                    case 11374: {
                        mapChar = 625;
                        break;
                    }
                    case 11375: {
                        mapChar = 592;
                        break;
                    }
                    case 11376: {
                        mapChar = 594;
                        break;
                    }
                    case 11390: {
                        mapChar = 575;
                        break;
                    }
                    case 11391: {
                        mapChar = 576;
                        break;
                    }
                    case 42570: {
                        mapChar = 7304;
                        break;
                    }
                    case 42877: {
                        mapChar = 7545;
                        break;
                    }
                    case 42893: {
                        mapChar = 613;
                        break;
                    }
                    case 42922: {
                        mapChar = 614;
                        break;
                    }
                    case 42923: {
                        mapChar = 604;
                        break;
                    }
                    case 42924: {
                        mapChar = 609;
                        break;
                    }
                    case 42925: {
                        mapChar = 620;
                        break;
                    }
                    case 42926: {
                        mapChar = 618;
                        break;
                    }
                    case 42928: {
                        mapChar = 670;
                        break;
                    }
                    case 42929: {
                        mapChar = 647;
                        break;
                    }
                    case 42930: {
                        mapChar = 669;
                        break;
                    }
                    case 42931: {
                        mapChar = 43859;
                        break;
                    }
                    case 42949: {
                        mapChar = 642;
                        break;
                    }
                    case 42950: {
                        mapChar = 7566;
                    }
                }
            } else {
                int offset = val << 5 >> 23;
                mapChar = ch + offset;
            }
        }
        return mapChar;
    }

    @Override
    int toUpperCase(int ch) {
        int mapChar = ch;
        int val = this.getProperties(ch);
        if ((val & 0x10000) != 0) {
            if ((val & 0x7FC0000) == 133955584) {
                switch (ch) {
                    case 383: {
                        mapChar = 83;
                        break;
                    }
                    case 575: {
                        mapChar = 11390;
                        break;
                    }
                    case 576: {
                        mapChar = 11391;
                        break;
                    }
                    case 592: {
                        mapChar = 11375;
                        break;
                    }
                    case 593: {
                        mapChar = 11373;
                        break;
                    }
                    case 594: {
                        mapChar = 11376;
                        break;
                    }
                    case 604: {
                        mapChar = 42923;
                        break;
                    }
                    case 609: {
                        mapChar = 42924;
                        break;
                    }
                    case 613: {
                        mapChar = 42893;
                        break;
                    }
                    case 614: {
                        mapChar = 42922;
                        break;
                    }
                    case 618: {
                        mapChar = 42926;
                        break;
                    }
                    case 619: {
                        mapChar = 11362;
                        break;
                    }
                    case 620: {
                        mapChar = 42925;
                        break;
                    }
                    case 625: {
                        mapChar = 11374;
                        break;
                    }
                    case 637: {
                        mapChar = 11364;
                        break;
                    }
                    case 642: {
                        mapChar = 42949;
                        break;
                    }
                    case 647: {
                        mapChar = 42929;
                        break;
                    }
                    case 669: {
                        mapChar = 42930;
                        break;
                    }
                    case 670: {
                        mapChar = 42928;
                        break;
                    }
                    case 4304: {
                        mapChar = 7312;
                        break;
                    }
                    case 4305: {
                        mapChar = 7313;
                        break;
                    }
                    case 4306: {
                        mapChar = 7314;
                        break;
                    }
                    case 4307: {
                        mapChar = 7315;
                        break;
                    }
                    case 4308: {
                        mapChar = 7316;
                        break;
                    }
                    case 4309: {
                        mapChar = 7317;
                        break;
                    }
                    case 4310: {
                        mapChar = 7318;
                        break;
                    }
                    case 4311: {
                        mapChar = 7319;
                        break;
                    }
                    case 4312: {
                        mapChar = 7320;
                        break;
                    }
                    case 4313: {
                        mapChar = 7321;
                        break;
                    }
                    case 4314: {
                        mapChar = 7322;
                        break;
                    }
                    case 4315: {
                        mapChar = 7323;
                        break;
                    }
                    case 4316: {
                        mapChar = 7324;
                        break;
                    }
                    case 4317: {
                        mapChar = 7325;
                        break;
                    }
                    case 4318: {
                        mapChar = 7326;
                        break;
                    }
                    case 4319: {
                        mapChar = 7327;
                        break;
                    }
                    case 4320: {
                        mapChar = 7328;
                        break;
                    }
                    case 4321: {
                        mapChar = 7329;
                        break;
                    }
                    case 4322: {
                        mapChar = 7330;
                        break;
                    }
                    case 4323: {
                        mapChar = 7331;
                        break;
                    }
                    case 4324: {
                        mapChar = 7332;
                        break;
                    }
                    case 4325: {
                        mapChar = 7333;
                        break;
                    }
                    case 4326: {
                        mapChar = 7334;
                        break;
                    }
                    case 4327: {
                        mapChar = 7335;
                        break;
                    }
                    case 4328: {
                        mapChar = 7336;
                        break;
                    }
                    case 4329: {
                        mapChar = 7337;
                        break;
                    }
                    case 4330: {
                        mapChar = 7338;
                        break;
                    }
                    case 4331: {
                        mapChar = 7339;
                        break;
                    }
                    case 4332: {
                        mapChar = 7340;
                        break;
                    }
                    case 4333: {
                        mapChar = 7341;
                        break;
                    }
                    case 4334: {
                        mapChar = 7342;
                        break;
                    }
                    case 4335: {
                        mapChar = 7343;
                        break;
                    }
                    case 4336: {
                        mapChar = 7344;
                        break;
                    }
                    case 4337: {
                        mapChar = 7345;
                        break;
                    }
                    case 4338: {
                        mapChar = 7346;
                        break;
                    }
                    case 4339: {
                        mapChar = 7347;
                        break;
                    }
                    case 4340: {
                        mapChar = 7348;
                        break;
                    }
                    case 4341: {
                        mapChar = 7349;
                        break;
                    }
                    case 4342: {
                        mapChar = 7350;
                        break;
                    }
                    case 4343: {
                        mapChar = 7351;
                        break;
                    }
                    case 4344: {
                        mapChar = 7352;
                        break;
                    }
                    case 4345: {
                        mapChar = 7353;
                        break;
                    }
                    case 4346: {
                        mapChar = 7354;
                        break;
                    }
                    case 4349: {
                        mapChar = 7357;
                        break;
                    }
                    case 4350: {
                        mapChar = 7358;
                        break;
                    }
                    case 4351: {
                        mapChar = 7359;
                        break;
                    }
                    case 7296: {
                        mapChar = 1042;
                        break;
                    }
                    case 7297: {
                        mapChar = 1044;
                        break;
                    }
                    case 7298: {
                        mapChar = 1054;
                        break;
                    }
                    case 7299: {
                        mapChar = 1057;
                        break;
                    }
                    case 7300: {
                        mapChar = 1058;
                        break;
                    }
                    case 7301: {
                        mapChar = 1058;
                        break;
                    }
                    case 7302: {
                        mapChar = 1066;
                        break;
                    }
                    case 7303: {
                        mapChar = 1122;
                        break;
                    }
                    case 7304: {
                        mapChar = 42570;
                        break;
                    }
                    case 7545: {
                        mapChar = 42877;
                        break;
                    }
                    case 7549: {
                        mapChar = 11363;
                        break;
                    }
                    case 7566: {
                        mapChar = 42950;
                        break;
                    }
                    case 8064: {
                        mapChar = 8072;
                        break;
                    }
                    case 8065: {
                        mapChar = 8073;
                        break;
                    }
                    case 8066: {
                        mapChar = 8074;
                        break;
                    }
                    case 8067: {
                        mapChar = 8075;
                        break;
                    }
                    case 8068: {
                        mapChar = 8076;
                        break;
                    }
                    case 8069: {
                        mapChar = 8077;
                        break;
                    }
                    case 8070: {
                        mapChar = 8078;
                        break;
                    }
                    case 8071: {
                        mapChar = 8079;
                        break;
                    }
                    case 8080: {
                        mapChar = 8088;
                        break;
                    }
                    case 8081: {
                        mapChar = 8089;
                        break;
                    }
                    case 8082: {
                        mapChar = 8090;
                        break;
                    }
                    case 8083: {
                        mapChar = 8091;
                        break;
                    }
                    case 8084: {
                        mapChar = 8092;
                        break;
                    }
                    case 8085: {
                        mapChar = 8093;
                        break;
                    }
                    case 8086: {
                        mapChar = 8094;
                        break;
                    }
                    case 8087: {
                        mapChar = 8095;
                        break;
                    }
                    case 8096: {
                        mapChar = 8104;
                        break;
                    }
                    case 8097: {
                        mapChar = 8105;
                        break;
                    }
                    case 8098: {
                        mapChar = 8106;
                        break;
                    }
                    case 8099: {
                        mapChar = 8107;
                        break;
                    }
                    case 8100: {
                        mapChar = 8108;
                        break;
                    }
                    case 8101: {
                        mapChar = 8109;
                        break;
                    }
                    case 8102: {
                        mapChar = 8110;
                        break;
                    }
                    case 8103: {
                        mapChar = 8111;
                        break;
                    }
                    case 8115: {
                        mapChar = 8124;
                        break;
                    }
                    case 8126: {
                        mapChar = 921;
                        break;
                    }
                    case 8131: {
                        mapChar = 8140;
                        break;
                    }
                    case 8179: {
                        mapChar = 8188;
                        break;
                    }
                    case 11365: {
                        mapChar = 570;
                        break;
                    }
                    case 11366: {
                        mapChar = 574;
                        break;
                    }
                    case 11520: {
                        mapChar = 4256;
                        break;
                    }
                    case 11521: {
                        mapChar = 4257;
                        break;
                    }
                    case 11522: {
                        mapChar = 4258;
                        break;
                    }
                    case 11523: {
                        mapChar = 4259;
                        break;
                    }
                    case 11524: {
                        mapChar = 4260;
                        break;
                    }
                    case 11525: {
                        mapChar = 4261;
                        break;
                    }
                    case 11526: {
                        mapChar = 4262;
                        break;
                    }
                    case 11527: {
                        mapChar = 4263;
                        break;
                    }
                    case 11528: {
                        mapChar = 4264;
                        break;
                    }
                    case 11529: {
                        mapChar = 4265;
                        break;
                    }
                    case 11530: {
                        mapChar = 4266;
                        break;
                    }
                    case 11531: {
                        mapChar = 4267;
                        break;
                    }
                    case 11532: {
                        mapChar = 4268;
                        break;
                    }
                    case 11533: {
                        mapChar = 4269;
                        break;
                    }
                    case 11534: {
                        mapChar = 4270;
                        break;
                    }
                    case 11535: {
                        mapChar = 4271;
                        break;
                    }
                    case 11536: {
                        mapChar = 4272;
                        break;
                    }
                    case 11537: {
                        mapChar = 4273;
                        break;
                    }
                    case 11538: {
                        mapChar = 4274;
                        break;
                    }
                    case 11539: {
                        mapChar = 4275;
                        break;
                    }
                    case 11540: {
                        mapChar = 4276;
                        break;
                    }
                    case 11541: {
                        mapChar = 4277;
                        break;
                    }
                    case 11542: {
                        mapChar = 4278;
                        break;
                    }
                    case 11543: {
                        mapChar = 4279;
                        break;
                    }
                    case 11544: {
                        mapChar = 4280;
                        break;
                    }
                    case 11545: {
                        mapChar = 4281;
                        break;
                    }
                    case 11546: {
                        mapChar = 4282;
                        break;
                    }
                    case 11547: {
                        mapChar = 4283;
                        break;
                    }
                    case 11548: {
                        mapChar = 4284;
                        break;
                    }
                    case 11549: {
                        mapChar = 4285;
                        break;
                    }
                    case 11550: {
                        mapChar = 4286;
                        break;
                    }
                    case 11551: {
                        mapChar = 4287;
                        break;
                    }
                    case 11552: {
                        mapChar = 4288;
                        break;
                    }
                    case 11553: {
                        mapChar = 4289;
                        break;
                    }
                    case 11554: {
                        mapChar = 4290;
                        break;
                    }
                    case 11555: {
                        mapChar = 4291;
                        break;
                    }
                    case 11556: {
                        mapChar = 4292;
                        break;
                    }
                    case 11557: {
                        mapChar = 4293;
                        break;
                    }
                    case 11559: {
                        mapChar = 4295;
                        break;
                    }
                    case 11565: {
                        mapChar = 4301;
                        break;
                    }
                    case 43859: {
                        mapChar = 42931;
                        break;
                    }
                    case 43888: {
                        mapChar = 5024;
                        break;
                    }
                    case 43889: {
                        mapChar = 5025;
                        break;
                    }
                    case 43890: {
                        mapChar = 5026;
                        break;
                    }
                    case 43891: {
                        mapChar = 5027;
                        break;
                    }
                    case 43892: {
                        mapChar = 5028;
                        break;
                    }
                    case 43893: {
                        mapChar = 5029;
                        break;
                    }
                    case 43894: {
                        mapChar = 5030;
                        break;
                    }
                    case 43895: {
                        mapChar = 5031;
                        break;
                    }
                    case 43896: {
                        mapChar = 5032;
                        break;
                    }
                    case 43897: {
                        mapChar = 5033;
                        break;
                    }
                    case 43898: {
                        mapChar = 5034;
                        break;
                    }
                    case 43899: {
                        mapChar = 5035;
                        break;
                    }
                    case 43900: {
                        mapChar = 5036;
                        break;
                    }
                    case 43901: {
                        mapChar = 5037;
                        break;
                    }
                    case 43902: {
                        mapChar = 5038;
                        break;
                    }
                    case 43903: {
                        mapChar = 5039;
                        break;
                    }
                    case 43904: {
                        mapChar = 5040;
                        break;
                    }
                    case 43905: {
                        mapChar = 5041;
                        break;
                    }
                    case 43906: {
                        mapChar = 5042;
                        break;
                    }
                    case 43907: {
                        mapChar = 5043;
                        break;
                    }
                    case 43908: {
                        mapChar = 5044;
                        break;
                    }
                    case 43909: {
                        mapChar = 5045;
                        break;
                    }
                    case 43910: {
                        mapChar = 5046;
                        break;
                    }
                    case 43911: {
                        mapChar = 5047;
                        break;
                    }
                    case 43912: {
                        mapChar = 5048;
                        break;
                    }
                    case 43913: {
                        mapChar = 5049;
                        break;
                    }
                    case 43914: {
                        mapChar = 5050;
                        break;
                    }
                    case 43915: {
                        mapChar = 5051;
                        break;
                    }
                    case 43916: {
                        mapChar = 5052;
                        break;
                    }
                    case 43917: {
                        mapChar = 5053;
                        break;
                    }
                    case 43918: {
                        mapChar = 5054;
                        break;
                    }
                    case 43919: {
                        mapChar = 5055;
                        break;
                    }
                    case 43920: {
                        mapChar = 5056;
                        break;
                    }
                    case 43921: {
                        mapChar = 5057;
                        break;
                    }
                    case 43922: {
                        mapChar = 5058;
                        break;
                    }
                    case 43923: {
                        mapChar = 5059;
                        break;
                    }
                    case 43924: {
                        mapChar = 5060;
                        break;
                    }
                    case 43925: {
                        mapChar = 5061;
                        break;
                    }
                    case 43926: {
                        mapChar = 5062;
                        break;
                    }
                    case 43927: {
                        mapChar = 5063;
                        break;
                    }
                    case 43928: {
                        mapChar = 5064;
                        break;
                    }
                    case 43929: {
                        mapChar = 5065;
                        break;
                    }
                    case 43930: {
                        mapChar = 5066;
                        break;
                    }
                    case 43931: {
                        mapChar = 5067;
                        break;
                    }
                    case 43932: {
                        mapChar = 5068;
                        break;
                    }
                    case 43933: {
                        mapChar = 5069;
                        break;
                    }
                    case 43934: {
                        mapChar = 5070;
                        break;
                    }
                    case 43935: {
                        mapChar = 5071;
                        break;
                    }
                    case 43936: {
                        mapChar = 5072;
                        break;
                    }
                    case 43937: {
                        mapChar = 5073;
                        break;
                    }
                    case 43938: {
                        mapChar = 5074;
                        break;
                    }
                    case 43939: {
                        mapChar = 5075;
                        break;
                    }
                    case 43940: {
                        mapChar = 5076;
                        break;
                    }
                    case 43941: {
                        mapChar = 5077;
                        break;
                    }
                    case 43942: {
                        mapChar = 5078;
                        break;
                    }
                    case 43943: {
                        mapChar = 5079;
                        break;
                    }
                    case 43944: {
                        mapChar = 5080;
                        break;
                    }
                    case 43945: {
                        mapChar = 5081;
                        break;
                    }
                    case 43946: {
                        mapChar = 5082;
                        break;
                    }
                    case 43947: {
                        mapChar = 5083;
                        break;
                    }
                    case 43948: {
                        mapChar = 5084;
                        break;
                    }
                    case 43949: {
                        mapChar = 5085;
                        break;
                    }
                    case 43950: {
                        mapChar = 5086;
                        break;
                    }
                    case 43951: {
                        mapChar = 5087;
                        break;
                    }
                    case 43952: {
                        mapChar = 5088;
                        break;
                    }
                    case 43953: {
                        mapChar = 5089;
                        break;
                    }
                    case 43954: {
                        mapChar = 5090;
                        break;
                    }
                    case 43955: {
                        mapChar = 5091;
                        break;
                    }
                    case 43956: {
                        mapChar = 5092;
                        break;
                    }
                    case 43957: {
                        mapChar = 5093;
                        break;
                    }
                    case 43958: {
                        mapChar = 5094;
                        break;
                    }
                    case 43959: {
                        mapChar = 5095;
                        break;
                    }
                    case 43960: {
                        mapChar = 5096;
                        break;
                    }
                    case 43961: {
                        mapChar = 5097;
                        break;
                    }
                    case 43962: {
                        mapChar = 5098;
                        break;
                    }
                    case 43963: {
                        mapChar = 5099;
                        break;
                    }
                    case 43964: {
                        mapChar = 5100;
                        break;
                    }
                    case 43965: {
                        mapChar = 5101;
                        break;
                    }
                    case 43966: {
                        mapChar = 5102;
                        break;
                    }
                    case 43967: {
                        mapChar = 5103;
                    }
                }
            } else {
                int offset = val << 5 >> 23;
                mapChar = ch - offset;
            }
        }
        return mapChar;
    }

    @Override
    int toTitleCase(int ch) {
        int mapChar = ch;
        int val = this.getProperties(ch);
        if ((val & 0x8000) != 0) {
            if ((val & 0x10000) == 0) {
                mapChar = ch + 1;
            } else if ((val & 0x20000) == 0) {
                mapChar = ch >= 4304 && ch <= 4351 ? ch : ch - 1;
            }
        } else if ((val & 0x10000) != 0) {
            mapChar = this.toUpperCase(ch);
        }
        return mapChar;
    }

    @Override
    int digit(int ch, int radix) {
        int value = -1;
        if (radix >= 2 && radix <= 36) {
            int val = this.getProperties(ch);
            int kind = val & 0x1F;
            if (kind == 9) {
                value = ch + ((val & 0x3E0) >> 5) & 0x1F;
            } else if ((val & 0xC00) == 3072) {
                value = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            }
        }
        return value < radix ? value : -1;
    }

    @Override
    int getNumericValue(int ch) {
        int val = this.getProperties(ch);
        int retval = -1;
        block0 : switch (val & 0xC00) {
            default: {
                retval = -1;
                break;
            }
            case 1024: {
                retval = ch + ((val & 0x3E0) >> 5) & 0x1F;
                break;
            }
            case 2048: {
                switch (ch) {
                    case 3057: {
                        retval = 100;
                        break block0;
                    }
                    case 3058: {
                        retval = 1000;
                        break block0;
                    }
                    case 3441: {
                        retval = 100;
                        break block0;
                    }
                    case 3442: {
                        retval = 1000;
                        break block0;
                    }
                    case 4981: {
                        retval = 40;
                        break block0;
                    }
                    case 4982: {
                        retval = 50;
                        break block0;
                    }
                    case 4983: {
                        retval = 60;
                        break block0;
                    }
                    case 4984: {
                        retval = 70;
                        break block0;
                    }
                    case 4985: {
                        retval = 80;
                        break block0;
                    }
                    case 4986: {
                        retval = 90;
                        break block0;
                    }
                    case 4987: {
                        retval = 100;
                        break block0;
                    }
                    case 4988: {
                        retval = 10000;
                        break block0;
                    }
                    case 8543: {
                        retval = 1;
                        break block0;
                    }
                    case 8556: {
                        retval = 50;
                        break block0;
                    }
                    case 8557: {
                        retval = 100;
                        break block0;
                    }
                    case 8558: {
                        retval = 500;
                        break block0;
                    }
                    case 8559: {
                        retval = 1000;
                        break block0;
                    }
                    case 8572: {
                        retval = 50;
                        break block0;
                    }
                    case 8573: {
                        retval = 100;
                        break block0;
                    }
                    case 8574: {
                        retval = 500;
                        break block0;
                    }
                    case 8575: {
                        retval = 1000;
                        break block0;
                    }
                    case 8576: {
                        retval = 1000;
                        break block0;
                    }
                    case 8577: {
                        retval = 5000;
                        break block0;
                    }
                    case 8578: {
                        retval = 10000;
                        break block0;
                    }
                    case 8582: {
                        retval = 50;
                        break block0;
                    }
                    case 8583: {
                        retval = 50000;
                        break block0;
                    }
                    case 8584: {
                        retval = 100000;
                        break block0;
                    }
                    case 12875: {
                        retval = 40;
                        break block0;
                    }
                    case 12876: {
                        retval = 50;
                        break block0;
                    }
                    case 12877: {
                        retval = 60;
                        break block0;
                    }
                    case 12878: {
                        retval = 70;
                        break block0;
                    }
                    case 12879: {
                        retval = 80;
                        break block0;
                    }
                    case 12892: {
                        retval = 32;
                        break block0;
                    }
                    case 12893: {
                        retval = 33;
                        break block0;
                    }
                    case 12894: {
                        retval = 34;
                        break block0;
                    }
                    case 12895: {
                        retval = 35;
                        break block0;
                    }
                    case 12977: {
                        retval = 36;
                        break block0;
                    }
                    case 12978: {
                        retval = 37;
                        break block0;
                    }
                    case 12979: {
                        retval = 38;
                        break block0;
                    }
                    case 12980: {
                        retval = 39;
                        break block0;
                    }
                    case 12981: {
                        retval = 40;
                        break block0;
                    }
                    case 12982: {
                        retval = 41;
                        break block0;
                    }
                    case 12983: {
                        retval = 42;
                        break block0;
                    }
                    case 12984: {
                        retval = 43;
                        break block0;
                    }
                    case 12985: {
                        retval = 44;
                        break block0;
                    }
                    case 12986: {
                        retval = 45;
                        break block0;
                    }
                    case 12987: {
                        retval = 46;
                        break block0;
                    }
                    case 12988: {
                        retval = 47;
                        break block0;
                    }
                    case 12989: {
                        retval = 48;
                        break block0;
                    }
                    case 12990: {
                        retval = 49;
                        break block0;
                    }
                    case 12991: {
                        retval = 50;
                        break block0;
                    }
                }
                retval = -2;
                break;
            }
            case 3072: {
                retval = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            }
        }
        return retval;
    }

    @Override
    boolean isDigit(int ch) {
        int props = this.getProperties(ch);
        return (props & 0x1F) == 9;
    }

    @Override
    boolean isLowerCase(int ch) {
        return (this.getPropertiesEx(ch) & 1) != 0;
    }

    @Override
    boolean isUpperCase(int ch) {
        return (this.getPropertiesEx(ch) & 2) != 0;
    }

    @Override
    boolean isWhitespace(int ch) {
        int props = this.getProperties(ch);
        return (props & 0x7000) == 16384;
    }

    @Override
    byte getDirectionality(int ch) {
        int val = this.getProperties(ch);
        int directionality = (val & 0x78000000) >> 27;
        if (directionality == 15) {
            switch (ch) {
                case 8234: {
                    directionality = 14;
                    break;
                }
                case 8235: {
                    directionality = 16;
                    break;
                }
                case 8236: {
                    directionality = 18;
                    break;
                }
                case 8237: {
                    directionality = 15;
                    break;
                }
                case 8238: {
                    directionality = 17;
                    break;
                }
                case 8294: {
                    directionality = 19;
                    break;
                }
                case 8295: {
                    directionality = 20;
                    break;
                }
                case 8296: {
                    directionality = 21;
                    break;
                }
                case 8297: {
                    directionality = 22;
                    break;
                }
                default: {
                    directionality = -1;
                }
            }
        }
        return (byte)directionality;
    }

    @Override
    boolean isMirrored(int ch) {
        int props = this.getProperties(ch);
        return (props & Integer.MIN_VALUE) != 0;
    }

    @Override
    int toUpperCaseEx(int ch) {
        int mapChar = ch;
        int val = this.getProperties(ch);
        if ((val & 0x10000) != 0) {
            if ((val & 0x7FC0000) != 133955584) {
                int offset = val << 5 >> 23;
                mapChar = ch - offset;
            } else {
                switch (ch) {
                    case 383: {
                        mapChar = 83;
                        break;
                    }
                    case 575: {
                        mapChar = 11390;
                        break;
                    }
                    case 576: {
                        mapChar = 11391;
                        break;
                    }
                    case 592: {
                        mapChar = 11375;
                        break;
                    }
                    case 593: {
                        mapChar = 11373;
                        break;
                    }
                    case 594: {
                        mapChar = 11376;
                        break;
                    }
                    case 604: {
                        mapChar = 42923;
                        break;
                    }
                    case 609: {
                        mapChar = 42924;
                        break;
                    }
                    case 613: {
                        mapChar = 42893;
                        break;
                    }
                    case 614: {
                        mapChar = 42922;
                        break;
                    }
                    case 618: {
                        mapChar = 42926;
                        break;
                    }
                    case 619: {
                        mapChar = 11362;
                        break;
                    }
                    case 620: {
                        mapChar = 42925;
                        break;
                    }
                    case 625: {
                        mapChar = 11374;
                        break;
                    }
                    case 637: {
                        mapChar = 11364;
                        break;
                    }
                    case 642: {
                        mapChar = 42949;
                        break;
                    }
                    case 647: {
                        mapChar = 42929;
                        break;
                    }
                    case 669: {
                        mapChar = 42930;
                        break;
                    }
                    case 670: {
                        mapChar = 42928;
                        break;
                    }
                    case 4304: {
                        mapChar = 7312;
                        break;
                    }
                    case 4305: {
                        mapChar = 7313;
                        break;
                    }
                    case 4306: {
                        mapChar = 7314;
                        break;
                    }
                    case 4307: {
                        mapChar = 7315;
                        break;
                    }
                    case 4308: {
                        mapChar = 7316;
                        break;
                    }
                    case 4309: {
                        mapChar = 7317;
                        break;
                    }
                    case 4310: {
                        mapChar = 7318;
                        break;
                    }
                    case 4311: {
                        mapChar = 7319;
                        break;
                    }
                    case 4312: {
                        mapChar = 7320;
                        break;
                    }
                    case 4313: {
                        mapChar = 7321;
                        break;
                    }
                    case 4314: {
                        mapChar = 7322;
                        break;
                    }
                    case 4315: {
                        mapChar = 7323;
                        break;
                    }
                    case 4316: {
                        mapChar = 7324;
                        break;
                    }
                    case 4317: {
                        mapChar = 7325;
                        break;
                    }
                    case 4318: {
                        mapChar = 7326;
                        break;
                    }
                    case 4319: {
                        mapChar = 7327;
                        break;
                    }
                    case 4320: {
                        mapChar = 7328;
                        break;
                    }
                    case 4321: {
                        mapChar = 7329;
                        break;
                    }
                    case 4322: {
                        mapChar = 7330;
                        break;
                    }
                    case 4323: {
                        mapChar = 7331;
                        break;
                    }
                    case 4324: {
                        mapChar = 7332;
                        break;
                    }
                    case 4325: {
                        mapChar = 7333;
                        break;
                    }
                    case 4326: {
                        mapChar = 7334;
                        break;
                    }
                    case 4327: {
                        mapChar = 7335;
                        break;
                    }
                    case 4328: {
                        mapChar = 7336;
                        break;
                    }
                    case 4329: {
                        mapChar = 7337;
                        break;
                    }
                    case 4330: {
                        mapChar = 7338;
                        break;
                    }
                    case 4331: {
                        mapChar = 7339;
                        break;
                    }
                    case 4332: {
                        mapChar = 7340;
                        break;
                    }
                    case 4333: {
                        mapChar = 7341;
                        break;
                    }
                    case 4334: {
                        mapChar = 7342;
                        break;
                    }
                    case 4335: {
                        mapChar = 7343;
                        break;
                    }
                    case 4336: {
                        mapChar = 7344;
                        break;
                    }
                    case 4337: {
                        mapChar = 7345;
                        break;
                    }
                    case 4338: {
                        mapChar = 7346;
                        break;
                    }
                    case 4339: {
                        mapChar = 7347;
                        break;
                    }
                    case 4340: {
                        mapChar = 7348;
                        break;
                    }
                    case 4341: {
                        mapChar = 7349;
                        break;
                    }
                    case 4342: {
                        mapChar = 7350;
                        break;
                    }
                    case 4343: {
                        mapChar = 7351;
                        break;
                    }
                    case 4344: {
                        mapChar = 7352;
                        break;
                    }
                    case 4345: {
                        mapChar = 7353;
                        break;
                    }
                    case 4346: {
                        mapChar = 7354;
                        break;
                    }
                    case 4349: {
                        mapChar = 7357;
                        break;
                    }
                    case 4350: {
                        mapChar = 7358;
                        break;
                    }
                    case 4351: {
                        mapChar = 7359;
                        break;
                    }
                    case 7296: {
                        mapChar = 1042;
                        break;
                    }
                    case 7297: {
                        mapChar = 1044;
                        break;
                    }
                    case 7298: {
                        mapChar = 1054;
                        break;
                    }
                    case 7299: {
                        mapChar = 1057;
                        break;
                    }
                    case 7300: {
                        mapChar = 1058;
                        break;
                    }
                    case 7301: {
                        mapChar = 1058;
                        break;
                    }
                    case 7302: {
                        mapChar = 1066;
                        break;
                    }
                    case 7303: {
                        mapChar = 1122;
                        break;
                    }
                    case 7304: {
                        mapChar = 42570;
                        break;
                    }
                    case 7545: {
                        mapChar = 42877;
                        break;
                    }
                    case 7549: {
                        mapChar = 11363;
                        break;
                    }
                    case 7566: {
                        mapChar = 42950;
                        break;
                    }
                    case 8126: {
                        mapChar = 921;
                        break;
                    }
                    case 11365: {
                        mapChar = 570;
                        break;
                    }
                    case 11366: {
                        mapChar = 574;
                        break;
                    }
                    case 11520: {
                        mapChar = 4256;
                        break;
                    }
                    case 11521: {
                        mapChar = 4257;
                        break;
                    }
                    case 11522: {
                        mapChar = 4258;
                        break;
                    }
                    case 11523: {
                        mapChar = 4259;
                        break;
                    }
                    case 11524: {
                        mapChar = 4260;
                        break;
                    }
                    case 11525: {
                        mapChar = 4261;
                        break;
                    }
                    case 11526: {
                        mapChar = 4262;
                        break;
                    }
                    case 11527: {
                        mapChar = 4263;
                        break;
                    }
                    case 11528: {
                        mapChar = 4264;
                        break;
                    }
                    case 11529: {
                        mapChar = 4265;
                        break;
                    }
                    case 11530: {
                        mapChar = 4266;
                        break;
                    }
                    case 11531: {
                        mapChar = 4267;
                        break;
                    }
                    case 11532: {
                        mapChar = 4268;
                        break;
                    }
                    case 11533: {
                        mapChar = 4269;
                        break;
                    }
                    case 11534: {
                        mapChar = 4270;
                        break;
                    }
                    case 11535: {
                        mapChar = 4271;
                        break;
                    }
                    case 11536: {
                        mapChar = 4272;
                        break;
                    }
                    case 11537: {
                        mapChar = 4273;
                        break;
                    }
                    case 11538: {
                        mapChar = 4274;
                        break;
                    }
                    case 11539: {
                        mapChar = 4275;
                        break;
                    }
                    case 11540: {
                        mapChar = 4276;
                        break;
                    }
                    case 11541: {
                        mapChar = 4277;
                        break;
                    }
                    case 11542: {
                        mapChar = 4278;
                        break;
                    }
                    case 11543: {
                        mapChar = 4279;
                        break;
                    }
                    case 11544: {
                        mapChar = 4280;
                        break;
                    }
                    case 11545: {
                        mapChar = 4281;
                        break;
                    }
                    case 11546: {
                        mapChar = 4282;
                        break;
                    }
                    case 11547: {
                        mapChar = 4283;
                        break;
                    }
                    case 11548: {
                        mapChar = 4284;
                        break;
                    }
                    case 11549: {
                        mapChar = 4285;
                        break;
                    }
                    case 11550: {
                        mapChar = 4286;
                        break;
                    }
                    case 11551: {
                        mapChar = 4287;
                        break;
                    }
                    case 11552: {
                        mapChar = 4288;
                        break;
                    }
                    case 11553: {
                        mapChar = 4289;
                        break;
                    }
                    case 11554: {
                        mapChar = 4290;
                        break;
                    }
                    case 11555: {
                        mapChar = 4291;
                        break;
                    }
                    case 11556: {
                        mapChar = 4292;
                        break;
                    }
                    case 11557: {
                        mapChar = 4293;
                        break;
                    }
                    case 11559: {
                        mapChar = 4295;
                        break;
                    }
                    case 11565: {
                        mapChar = 4301;
                        break;
                    }
                    case 43859: {
                        mapChar = 42931;
                        break;
                    }
                    case 43888: {
                        mapChar = 5024;
                        break;
                    }
                    case 43889: {
                        mapChar = 5025;
                        break;
                    }
                    case 43890: {
                        mapChar = 5026;
                        break;
                    }
                    case 43891: {
                        mapChar = 5027;
                        break;
                    }
                    case 43892: {
                        mapChar = 5028;
                        break;
                    }
                    case 43893: {
                        mapChar = 5029;
                        break;
                    }
                    case 43894: {
                        mapChar = 5030;
                        break;
                    }
                    case 43895: {
                        mapChar = 5031;
                        break;
                    }
                    case 43896: {
                        mapChar = 5032;
                        break;
                    }
                    case 43897: {
                        mapChar = 5033;
                        break;
                    }
                    case 43898: {
                        mapChar = 5034;
                        break;
                    }
                    case 43899: {
                        mapChar = 5035;
                        break;
                    }
                    case 43900: {
                        mapChar = 5036;
                        break;
                    }
                    case 43901: {
                        mapChar = 5037;
                        break;
                    }
                    case 43902: {
                        mapChar = 5038;
                        break;
                    }
                    case 43903: {
                        mapChar = 5039;
                        break;
                    }
                    case 43904: {
                        mapChar = 5040;
                        break;
                    }
                    case 43905: {
                        mapChar = 5041;
                        break;
                    }
                    case 43906: {
                        mapChar = 5042;
                        break;
                    }
                    case 43907: {
                        mapChar = 5043;
                        break;
                    }
                    case 43908: {
                        mapChar = 5044;
                        break;
                    }
                    case 43909: {
                        mapChar = 5045;
                        break;
                    }
                    case 43910: {
                        mapChar = 5046;
                        break;
                    }
                    case 43911: {
                        mapChar = 5047;
                        break;
                    }
                    case 43912: {
                        mapChar = 5048;
                        break;
                    }
                    case 43913: {
                        mapChar = 5049;
                        break;
                    }
                    case 43914: {
                        mapChar = 5050;
                        break;
                    }
                    case 43915: {
                        mapChar = 5051;
                        break;
                    }
                    case 43916: {
                        mapChar = 5052;
                        break;
                    }
                    case 43917: {
                        mapChar = 5053;
                        break;
                    }
                    case 43918: {
                        mapChar = 5054;
                        break;
                    }
                    case 43919: {
                        mapChar = 5055;
                        break;
                    }
                    case 43920: {
                        mapChar = 5056;
                        break;
                    }
                    case 43921: {
                        mapChar = 5057;
                        break;
                    }
                    case 43922: {
                        mapChar = 5058;
                        break;
                    }
                    case 43923: {
                        mapChar = 5059;
                        break;
                    }
                    case 43924: {
                        mapChar = 5060;
                        break;
                    }
                    case 43925: {
                        mapChar = 5061;
                        break;
                    }
                    case 43926: {
                        mapChar = 5062;
                        break;
                    }
                    case 43927: {
                        mapChar = 5063;
                        break;
                    }
                    case 43928: {
                        mapChar = 5064;
                        break;
                    }
                    case 43929: {
                        mapChar = 5065;
                        break;
                    }
                    case 43930: {
                        mapChar = 5066;
                        break;
                    }
                    case 43931: {
                        mapChar = 5067;
                        break;
                    }
                    case 43932: {
                        mapChar = 5068;
                        break;
                    }
                    case 43933: {
                        mapChar = 5069;
                        break;
                    }
                    case 43934: {
                        mapChar = 5070;
                        break;
                    }
                    case 43935: {
                        mapChar = 5071;
                        break;
                    }
                    case 43936: {
                        mapChar = 5072;
                        break;
                    }
                    case 43937: {
                        mapChar = 5073;
                        break;
                    }
                    case 43938: {
                        mapChar = 5074;
                        break;
                    }
                    case 43939: {
                        mapChar = 5075;
                        break;
                    }
                    case 43940: {
                        mapChar = 5076;
                        break;
                    }
                    case 43941: {
                        mapChar = 5077;
                        break;
                    }
                    case 43942: {
                        mapChar = 5078;
                        break;
                    }
                    case 43943: {
                        mapChar = 5079;
                        break;
                    }
                    case 43944: {
                        mapChar = 5080;
                        break;
                    }
                    case 43945: {
                        mapChar = 5081;
                        break;
                    }
                    case 43946: {
                        mapChar = 5082;
                        break;
                    }
                    case 43947: {
                        mapChar = 5083;
                        break;
                    }
                    case 43948: {
                        mapChar = 5084;
                        break;
                    }
                    case 43949: {
                        mapChar = 5085;
                        break;
                    }
                    case 43950: {
                        mapChar = 5086;
                        break;
                    }
                    case 43951: {
                        mapChar = 5087;
                        break;
                    }
                    case 43952: {
                        mapChar = 5088;
                        break;
                    }
                    case 43953: {
                        mapChar = 5089;
                        break;
                    }
                    case 43954: {
                        mapChar = 5090;
                        break;
                    }
                    case 43955: {
                        mapChar = 5091;
                        break;
                    }
                    case 43956: {
                        mapChar = 5092;
                        break;
                    }
                    case 43957: {
                        mapChar = 5093;
                        break;
                    }
                    case 43958: {
                        mapChar = 5094;
                        break;
                    }
                    case 43959: {
                        mapChar = 5095;
                        break;
                    }
                    case 43960: {
                        mapChar = 5096;
                        break;
                    }
                    case 43961: {
                        mapChar = 5097;
                        break;
                    }
                    case 43962: {
                        mapChar = 5098;
                        break;
                    }
                    case 43963: {
                        mapChar = 5099;
                        break;
                    }
                    case 43964: {
                        mapChar = 5100;
                        break;
                    }
                    case 43965: {
                        mapChar = 5101;
                        break;
                    }
                    case 43966: {
                        mapChar = 5102;
                        break;
                    }
                    case 43967: {
                        mapChar = 5103;
                        break;
                    }
                    default: {
                        mapChar = -1;
                    }
                }
            }
        }
        return mapChar;
    }

    @Override
    char[] toUpperCaseCharArray(int ch) {
        char[] upperMap = new char[]{(char)ch};
        int location = this.findInCharMap(ch);
        if (location != -1) {
            upperMap = charMap[location][1];
        }
        return upperMap;
    }

    int findInCharMap(int ch) {
        if (charMap == null || charMap.length == 0) {
            return -1;
        }
        int bottom = 0;
        int top = charMap.length;
        int current = top / 2;
        while (top - bottom > 1) {
            if (ch >= charMap[current][0][0]) {
                bottom = current;
            } else {
                top = current;
            }
            current = (top + bottom) / 2;
        }
        if (ch == charMap[current][0][0]) {
            return current;
        }
        return -1;
    }

    private CharacterData00() {
    }

    static {
        X = "\u0000\u0010 0@P`p\u0080\u0090\u00a0\u00b0\u00c0\u00d0\u00e0\u00f0\u0080\u0100\u0110\u0120\u0130\u0140\u0150\u0160\u0170\u0170\u0180\u0190\u01a0\u01b0\u01c0\u01d0\u01e0\u01f0\u0200\u0080\u0210\u0080\u0220\u0080\u0080\u0230\u0240\u0250\u0260\u0270\u0280\u0290\u02a0\u02b0\u02c0\u02d0\u02b0\u02b0\u02e0\u02f0\u0300\u0310\u0320\u02b0\u02b0\u0330\u0340\u0350\u0360\u0370\u0380\u0390\u03a0\u02b0\u03b0\u03c0\u03d0\u03e0\u03f0\u0400\u0410\u0420\u0430\u0440\u0450\u0460\u0470\u0480\u0490\u04a0\u04b0\u04c0\u04d0\u04e0\u04f0\u0500\u0510\u0520\u0530\u0540\u0550\u0560\u0570\u0580\u0590\u05a0\u05b0\u05c0\u05d0\u05e0\u05f0\u0600\u0610\u0620\u0630\u0640\u0650\u0660\u0670\u0680\u0690\u06a0\u06b0\u0680\u06c0\u06d0\u06e0\u06f0\u0700\u0710\u0720\u0680\u0730\u0740\u0750\u0760\u0770\u0780\u0790\u07a0\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u07b0\u0730\u07c0\u07d0\u07e0\u0730\u07f0\u0730\u0800\u0810\u0820\u0780\u0780\u0830\u0840\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0850\u0860\u0730\u0730\u0870\u0880\u0890\u08a0\u08b0\u0730\u08c0\u08d0\u08e0\u08f0\u0730\u0900\u0910\u0920\u0930\u0730\u0940\u0950\u0960\u0970\u0980\u0730\u0990\u09a0\u09b0\u09c0\u0730\u09d0\u09e0\u09f0\u0a00\u0a10\u0680\u0a20\u0a30\u0a40\u0a50\u0a60\u0a70\u0730\u0a80\u0730\u0a90\u0aa0\u0ab0\u0ac0\u0ad0\u0ae0\u0af0\u0b00\u0b10\u0b20\u0b30\u0b40\u0b20\u0170\u0b50\u0080\u0080\u0080\u0080\u0b60\u0080\u0080\u0080\u0b70\u0b80\u0b90\u0ba0\u0bb0\u0bc0\u0bd0\u0be0\u0bf0\u0c00\u0c10\u0c20\u0c30\u0c40\u0c50\u0c60\u0c70\u0c80\u0c90\u0ca0\u0cb0\u0cc0\u0cd0\u0ce0\u0cf0\u0d00\u0d10\u0d20\u0d30\u0d40\u0d50\u0d60\u0d70\u0d80\u0d90\u0da0\u0db0\u0dc0\u0dd0\u0de0\u09b0\u0df0\u0e00\u0e10\u0e20\u0e30\u0e40\u0e50\u09b0\u09b0\u09b0\u09b0\u09b0\u0e60\u0e70\u0e80\u0e90\u0ea0\u0eb0\u0ec0\u0ed0\u0ee0\u0ef0\u0f00\u0f10\u0f20\u0f30\u0f40\u0f50\u0f60\u0f70\u0f80\u0d90\u0d90\u0d90\u0d90\u0d90\u0d90\u0d90\u0d90\u0f90\u0fa0\u0f90\u0f90\u0fb0\u0fc0\u0fd0\u0fe0\u0ff0\u1000\u1010\u1020\u1030\u1040\u1050\u1060\u1070\u1080\u1090\u10a0\u10b0\u09b0\u09b0\u10c0\u10d0\u10e0\u10f0\u1100\u0080\u0080\u0080\u1110\u1120\u1130\u0730\u1140\u1150\u1160\u1160\u1170\u1180\u1190\u11a0\u0680\u11b0\u09b0\u09b0\u11c0\u09b0\u09b0\u09b0\u09b0\u09b0\u09b0\u11d0\u11e0\u11f0\u1200\u0650\u0730\u1210\u0840\u0730\u1220\u1230\u1240\u0730\u0730\u1250\u0730\u09b0\u1260\u1270\u1280\u1290\u12a0\u12b0\u12c0\u12d0\u0d90\u0d90\u0d90\u0d90\u12e0\u0d90\u0d90\u12f0\u1300\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u09b0\u09b0\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1310\u1320\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u1330\u09b0\u1340\u0ab0\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u1350\u1360\u0080\u1370\u1380\u0730\u0730\u1390\u13a0\u13b0\u0080\u13c0\u13d0\u13e0\u13f0\u1400\u1410\u1420\u0730\u1430\u1440\u1450\u1460\u1470\u1480\u1490\u14a0\u14b0\u03d0\u14c0\u14d0\u14e0\u0730\u14f0\u1500\u1510\u0730\u1520\u1530\u1540\u1550\u1560\u1570\u1580\u1120\u1120\u0730\u1590\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u0730\u15a0\u15b0\u15c0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15d0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u15e0\u1310\u1310\u1310\u15f0\u1310\u1600\u1610\u1620\u1310\u1310\u1310\u1630\u1310\u1310\u1640\u0680\u1650\u1660\u1670\u02b0\u02b0\u1680\u1690\u02b0\u02b0\u02b0\u02b0\u02b0\u02b0\u02b0\u02b0\u02b0\u02b0\u16a0\u16b0\u02b0\u16c0\u02b0\u16d0\u16e0\u16f0\u1700\u1710\u1720\u02b0\u02b0\u02b0\u1730\u1740 \u1750\u1760\u1770\u0950\u1780\u1790".toCharArray();
        Y = "\u0000\u0000\u0000\u0000\u0002\u0004\u0006\u0000\u0000\u0000\u0000\u0000\u0000\u0000\b\u0004\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001a\u001a\u001a\u001a\u001c\u001e \"$$$$$$$$$$$$&(*,............024\u0000\u00006\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00008::<>@BDFHJLNPRTVVVVVVVVVVVXVVVZ\\\\\\\\\\\\\\\\\\\\\\^\\\\\\`bbbbbbbbbbbbbbbbbbbbbbbbdbbbfhhhhhhhjbbbbbbbbbbbbbbbbbbbbbbblhhjnbbprtvxzr|~b\u0080\u0082\u0084bbb\u0086\u0088\u008ab\u0086\u008c\u008eh\u0090b\u0092b\u0094\u0096\u0096\u0098\u009a\u009c\u0098\u009ehhhhhhh\u00a0bbbbbbbbb\u00a2\u009cb\u00a4bbbb\u00a6bbbbbbbbb\u008a\u008a\u008a\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2bbbbb\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u008a\u00c2\u00c4\u00c6\u00c0\u00c8\u00b4\u00c0\u00ca\u00c6\u00cc\u00ce\u008a\u008a\u008a\u00c6\u008a\u00d0\u00d2\u008a\u00c6\u00d4\u00d6\u00d8\u008a\u008a\u00da\u00dc\u008a\u008a\u008a\u00c6\u00c0\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u00de\u00de\u00de\u00de\u00e0\u00e2\u00e4\u00e4\u00de\u00e6\u00e6\u00e8\u00e8\u00e8\u00e8\u00e8\u00e4\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00de\u00de\u00ea\u00e6\u00e6\u00e6\u00ec\u00ee\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f2\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0bb\u00ecb\u00f4\u00f6\u00f8\u00fa\u00f4\u00f4\u00e6\u00fc\u00fe\u0100\u0102\u0104\u0106VVVVVVVV\u0108VVVV\u010a\u010c\u010e\\\\\\\\\\\\\\\\\u0110\\\\\\\\\u0112\u0114\u0116\u0118\u011a\u011cbbbbbbbbbbbb\u011e\u0120\u0122\u0124\u0126b\u0128\u012a\u012c\u012c\u012c\u012c\u012c\u012c\u012c\u012cVVVVVVVVVVVVVVVV\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\u012e\u012e\u012e\u012e\u012e\u012e\u012e\u012eb\u0130\u00f0\u00f0\u0132bbbbbbbbbbb\u0134hhhhhh\u0136bbbbbbbbbbbbbbbb\u0138\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013c\u013e\u0140\u0140\u0140\u0142\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0146\u0148\u014a\u014c\u014e\u0150\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0154\u0156\u0158\u0152\u0156\u00f4\u00f4\u00f4\u00f4\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015c\u00f4\u015e\u015a\u0160\u0162\u00f4\u00f4\u00f4\u00f4\u00f4\u0164\u0164\u0164\u0166\u0168\u016a\u016c\u016e\u0152\u0152\u0152\u0152\u0152\u0170\u0172\u0174\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0178\u0176\u0176\u0176\u0176\u017a\u0152\u0152\u0152\u0152\u0152\u0152\u017c\u0152\u0152\u0152\u017e\u017e\u017e\u017e\u017e\u0180\u0182\u0176\u0184\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0186\u0152\u0152\u0152\u0188\u018a\u017c\u0152\u018c\u018e\u0190\u00f0\u017c\u0176\u0192\u0192\u0192\u0192\u0192\u0176\u0194\u0196\u0174\u0174\u0174\u0174\u0174\u0174\u0174\u0198\u017a\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u00f0\u00f0\u00f0\u00f0\u00f0\u019a\u019c\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0152\u0152\u0152\u0152\u0152\u0184\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u019e\u019e\u019e\u019e\u019e\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u01a0\u00f0\u00f0\u00f0\u00f0\u01a2<\u0010\u01a4\u0150\u01a6\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u0152\u00f0\u01a8\u0152\u0152\u0152\u0152\u01a8\u0152\u01a8\u0152\u01aa\u00f4\u01ac\u01ac\u01ac\u01ac\u01ac\u01ac\u01ac\u0162\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u015a\u01a0\u00f0\u00f4\u0162\u0176\u0176\u0176\u0176\u0176\u01ae\u00f4\u00f4\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u01b0\u0176\u0176\u01ae\u0164\u00f4\u00f4\u00f4\u00f0\u00f0\u00f0\u00f0\u0176\u0176\u0176\u0176\u01b2\u00f0\u00f0\u00f0\u00f0\u00f0\u0152\u0152\u0152\u0152\u0152\u0152\u00f0\u01b4\u0152\u0152\u0152\u00f0\u00f0\u00f0\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u01b6\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01b6\u01b8\u01ba\u01bc\u0152\u0152\u0152\u01b6\u01ba\u01be\u01ba\u01c0\u00f0\u017c\u0152\u0096\u0096\u0096\u0096\u0096\u0152\u0140\u01c2\u01c2\u01c2\u01c2\u01c2\u01c4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c6\u01ba\u01c8\u0096\u0096\u0096\u01ca\u01c8\u01ca\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u01ca\u00f4\u0096\u0096\u00f4\u01b8\u01ba\u01bc\u0152\u01cc\u01ce\u01d0\u01ce\u01be\u01ca\u00f4\u00f4\u00f4\u01ce\u00f4\u00f4\u0096\u01c8\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u0096:\u01d2\u01d2\u01d4\u01d6\u01d8\u019a\u01da\u01b6\u01c8\u0096\u0096\u01ca\u00f4\u01c8\u01ca\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0096\u01c8\u01ca\u0096\u00f4\u019a\u01ba\u01bc\u01cc\u00f4\u01da\u01cc\u01da\u01aa\u00f4\u01da\u00f4\u00f4\u00f4\u01c8\u0096\u01ca\u01ca\u00f4\u00f4\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u0152\u0096\u01c6\u01dc\u00f4\u00f4\u00f4\u00f4\u01da\u01b6\u01c8\u0096\u0096\u0096\u0096\u01c8\u0096\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0096\u01c8\u0096\u0096\u00f4\u01b8\u01ba\u01bc\u0152\u0152\u01da\u01b6\u01ce\u01be\u00f4\u01ca\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u01de\u00f4\u00f4\u00f4\u01c8\u0152\u01aa\u00f0\u01da\u01ba\u01c8\u0096\u0096\u0096\u01ca\u01c8\u01ca\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0096\u01c8\u0096\u0096\u00f4\u01b8\u01bc\u01bc\u0152\u01cc\u01ce\u01d0\u01ce\u01be\u00f4\u00f4\u00f4\u0150\u01b6\u00f4\u00f4\u0096\u01c8\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u01e0\u01d2\u01d2\u01d2\u00f4\u00f4\u00f4\u00f4\u00f4\u01e2\u01c8\u0096\u0096\u01ca\u00f4\u0096\u01ca\u0096\u0096\u00f4\u01c8\u01ca\u01ca\u0096\u00f4\u01c8\u01ca\u00f4\u0096\u01ca\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u01ba\u01b6\u01d0\u00f4\u01ba\u01d0\u01ba\u01be\u00f4\u01ca\u00f4\u00f4\u01ce\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u01e4\u01e6\u016e\u016e\u014e\u01e8\u00f4\u00f4\u01b6\u01ba\u01e2\u0096\u0096\u0096\u01ca\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u01b8\u0152\u01b6\u01ba\u01d0\u0152\u01cc\u0152\u01aa\u00f4\u00f4\u00f4\u01da\u01cc\u0096\u01ca\u01c8\u00f4\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u00f4\u00f4\u00f4\u01ea\u01ec\u01ec\u01ee\u01f0\u01c6\u01ba\u01f2\u0096\u0096\u0096\u01ca\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u01c8\u0096\u0096\u00f4\u01b8\u01f4\u01ba\u01ba\u01d0\u01f6\u01d0\u01ba\u01aa\u00f4\u00f4\u00f4\u01ce\u01d0\u00f4\u00f4\u01c8\u01ca\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u01c8\u01f8\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0152\u01ba\u0096\u0096\u0096\u0096\u01ca\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c0\u01b8\u01ba\u01bc\u0152\u01cc\u01ba\u01d0\u01ba\u01be\u01fa\u00f4\u00f4\u0096\u01f8\u01d2\u01d2\u01d2\u01fc\u0096\u0152\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u01e4\u01d2\u01d2\u01d2\u01fe\u0096\u0096\u0096\u01da\u01ba\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c8\u0096\u0096\u0096\u0096\u01c8\u00f4\u0096\u0096\u0096\u01ca\u00f4\u019a\u00f4\u01ce\u01ba\u0152\u01cc\u01cc\u01ba\u01ba\u01ba\u01ba\u00f4\u00f4\u00f4\u01c2\u01c2\u01c2\u01c2\u01c2\u00f4\u01ba\u01dc\u00f4\u00f4\u00f4\u00f4\u00f4\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c6\u0096\u0152\u0152\u0152\u01cc\u00f4\u0200\u0096\u0096\u0096\u0202\u00f0\u00f0\u017c\u0204\u0206\u0206\u0206\u0206\u0206\u0140\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u01c8\u01ca\u01ca\u0096\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c8\u01c8\u0096\u0096\u0096\u0096\u01c6\u0096\u0152\u0152\u0152\u017c\u01e2\u00f4\u0096\u0096\u01ca\u0208\u00f0\u00f0\u017c\u019a\u0206\u0206\u0206\u0206\u0206\u00f4\u0096\u0096\u01fa\u020a\u0140\u0140\u0140\u0140\u0140\u0140\u0140\u020c\u020c\u020a\u00f0\u020a\u020a\u020a\u020e\u020e\u020e\u020e\u020e\u01d2\u01d2\u01d2\u01d2\u01d2\u0130\u0130\u0130\u0012\u0012\u0210\u0096\u0096\u0096\u0096\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u01da\u0152\u0152\u0152\u0152\u0152\u0152\u01b6\u0152\u0152\u0204\u00f0\u0096\u0096\u01c6\u0152\u0152\u0152\u0152\u0152\u01da\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u01cc\u020a\u020a\u020a\u020a\u0212\u020a\u020a\u0214\u020a\u0140\u0140\u020c\u020a\u0216\u01dc\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01f8\u01bc\u0152\u01b6\u0152\u0152\u01aa\u01be\u0218\u01bc\u01e2\u020e\u020e\u020e\u020e\u020e\u0140\u0140\u0140\u0096\u0096\u0096\u01ba\u0152\u0096\u0096\u0152\u01e2\u01ba\u021a\u01f8\u01ba\u01ba\u01ba\u0096\u01c6\u0152\u01e2\u0096\u0096\u0096\u0096\u0096\u0096\u01b6\u01bc\u01b6\u01ba\u01ba\u01bc\u01f8\u0206\u0206\u0206\u0206\u0206\u01ba\u01bc\u020a\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021e\u00f4\u00f4\u021e\u00f4\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0220\u0222\u0224\u0220\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u01ca\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u01ca\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u0096\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0150\u00f0\u0140\u0140\u0140\u0140\u0226\u0228\u0228\u0228\u0228\u022a\u022c\u01d2\u01d2\u01d2\u022e\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u016e\u016e\u016e\u016e\u016e\u00f4\u00f4\u00f4\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u0230\u0230\u0230\u00f4\u0232\u0232\u0232\u00f4\u0234\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01fa\u01f2\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0236\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0238\u023a\u00f4\u0096\u0096\u0096\u0096\u0096\u01d8\u0140\u023c\u023e\u0096\u0096\u0096\u01ca\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0152\u0240\u00f4\u00f4\u00f4\u00f4\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0152\u0242\u01dc\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0152\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0096\u01ca\u0152\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f0\u01bc\u0152\u0152\u0152\u01ba\u01ba\u01ba\u01ba\u01b6\u01be\u00f0\u00f0\u00f0\u00f0\u00f0\u0140\u01c4\u0140\u01de\u01c0\u00f4\u020e\u020e\u020e\u020e\u020e\u00f4\u00f4\u00f4\u0244\u0244\u0244\u0244\u0244\u00f4\u00f4\u00f4\u0010\u0010\u0010\u0246\u0010\u0248\u00f0\u024a\u0206\u0206\u0206\u0206\u0206\u00f4\u00f4\u00f4\u0096\u024c\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u00f4\u00f4\u0096\u0096\u024e\u0250\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c6\u01ca\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0152\u01b6\u01ba\u01bc\u01b6\u01ba\u00f4\u00f4\u01ba\u01b6\u01ba\u01ba\u01be\u00f0\u00f4\u00f4\u01e8\u00f4\u0010\u01c2\u01c2\u01c2\u01c2\u01c2\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u0096\u0096\u01ca\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u00f4\u0206\u0206\u0206\u0206\u0206\u0252\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c6\u01b6\u01bc\u00f4\u0140\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01f8\u01b6\u0152\u0152\u0152\u01cc\u0218\u01b6\u01bc\u0152\u0152\u0152\u01b6\u01ba\u01ba\u01bc\u01aa\u00f0\u00f0\u00f0\u019a\u0150\u020e\u020e\u020e\u020e\u020e\u00f4\u00f4\u00f4\u0206\u0206\u0206\u0206\u0206\u00f4\u00f4\u00f4\u0140\u0140\u0140\u01c4\u0140\u0140\u0140\u00f4\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u0254\u01aa\u00f0\u00f0\u00f0\u00f0\u00f0\u0152\u01cc\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0152\u0152\u021a\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0218\u0152\u0152\u01b6\u01b6\u01ba\u01ba\u01b6\u0256\u0096\u0096\u0096\u01ca\u00f4\u0206\u0206\u0206\u0206\u0206\u0140\u0140\u0140\u020c\u020a\u020a\u020a\u020a\u0130\u00f0\u00f0\u00f0\u00f0\u020a\u020a\u020a\u020a\u0216\u01dc\u0152\u021a\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01f8\u0152\u0152\u01ba\u0152\u0258\u0152\u0096\u0206\u0206\u0206\u0206\u0206\u0096\u0096\u0096\u0096\u0096\u0096\u0218\u0152\u01ba\u01bc\u01bc\u0152\u0210\u00f4\u00f4\u00f4\u00f4\u0140\u0140\u0096\u0096\u01ba\u01ba\u01ba\u01ba\u0152\u0152\u0152\u0152\u01ba\u01aa\u00f4\u01ea\u0140\u0140\u020e\u020e\u020e\u020e\u020e\u00f4\u01c8\u0096\u0206\u0206\u0206\u0206\u0206\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00e4\u00e4\u00e4\u0140\u00b4\u00b4\u00b4\u00b4\u025a\u00f4\u00f4\u00f4\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u021c\u025c\u021e\u021c\u0140\u0140\u0140\u0140\u00f4\u00f4\u00f4\u00f4\u00f0\u0204\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u0240\u00f0\u00f0\u00f0\u01b8\u0096\u01c0\u0096\u0096\u0096\u01b8\u025e\u00f0\u01ca\u00f4\u00f4\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u00de\u0260\u008a\u008a\u008a\u008a\u008a\u008a\u0262\u008a\u00c6\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u00c0\u008a\u008a\u008a\u008a\u008a\u0264\u00de\u00de\u00f0\u00f0\u00f0\u017c\u0152\u0152\u0152\u0152\u0152\u0152\u01aa\u00f0\u00f0\u00f0\u00f0\u00f0bbbbbbbbbbb\u00b4\u00b4\u0266\u008a\u0268\u026a\u026a\u026a\u026a\u026c\u026c\u026c\u026c\u026a\u026a\u026a\u00f4\u026c\u026c\u026c\u00f4\u026a\u026a\u026a\u026a\u026c\u026c\u026c\u026c\u026a\u026a\u026a\u026a\u026c\u026c\u026c\u026c\u026a\u026a\u026a\u00f4\u026c\u026c\u026c\u00f4\u026e\u026e\u026e\u026e\u0270\u0270\u0270\u0270\u026a\u026a\u026a\u026a\u026c\u026c\u026c\u026c\u0272\u0274\u0274\u0276\u0278\u027a\u027c\u00f4\u00b4\u00b4\u00b4\u00b4\u027e\u027e\u027e\u027e\u00b4\u00b4\u00b4\u00b4\u027e\u027e\u027e\u027e\u00b4\u00b4\u00b4\u00b4\u027e\u027e\u027e\u027e\u026a\u00b4\u025a\u00b4\u026c\u0280\u0282\u0284\u00e6\u00b4\u025a\u00b4\u0286\u0286\u0282\u00e6\u026a\u00b4\u00f4\u00b4\u026c\u0288\u028a\u00e6\u026a\u00b4\u028c\u00b4\u026c\u028e\u0290\u00e6\u00f4\u00b4\u025a\u00b4\u0292\u0294\u0282\u0296\u0298\u0298\u0298\u029a\u0298\u029c\u029e\u02a0\u02a2\u02a2\u02a2\u0010\u02a4\u02a6\u02a4\u02a6\u0010\u0010\u0010\u0010\u02a8\u02aa\u02aa\u02ac\u02ae\u02ae\u02b0\u0010\u02b2\u02b4\u02b6\u02b8\u02ba\u0010\u02bc\u02be\u02c0\u0010\u0010\u0010\u0010\u02c2\u02ba\u0010\u0010\u0010\u0010\u02c4\u02c6\u02c6\u02c8\u02aa\u02aa\u02c6\u02c6\u02c6\u02ca\u00f4HHH\u02cc\u02ce\u02d0\u02d2\u02d2\u02d2\u02d2\u02d2\u02cc\u02ce\u023a\u00de\u00de\u00de\u00de\u00de\u00de\u02d4\u00f4::::::::::::::::\u02d6\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u02d8\u0132\u02da\u02dc\u02da\u00f0\u00f0\u00f0\u00f0\u00f0\u019a\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u016e\u02de\u016e\u02e0\u016e\u02e2\u0118\u008a\u0118\u02e4\u02e0\u016e\u02e6\u0118\u0118\u016e\u016e\u02e8\u02de\u02ea\u02de\u021c\u0118\u02ec\u0118\u02ee\u0092\u0096\u02f0\u016e\u008a\u0118\u001e\u0166\u02f2\u008a\u008a\u02f4\u016e\u02f6RRRRRRRR\u02f8\u02f8\u02f8\u02f8\u02f8\u02f8\u02fa\u02fa\u02fc\u02fc\u02fc\u02fc\u02fc\u02fc\u02fe\u02fe\u0300\u0302\u0304\u0300\u0306\u016e\u00f4\u00f4\u0166\u0166\u0308\u030a\u030a\u0166\u016e\u016e\u030c\u02f4\u016e\u030c\u030e\u02e8\u016e\u030c\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0166\u016e\u030c\u030c\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0166\u0166\u0166\u0166\u0166\u0166\u0310\u0312\u001e\u0166\u0312\u0312\u0312\u0166\u0310\u0314\u0310\u001e\u0166\u0312\u0312\u0310\u0312\u001e\u001e\u001e\u0166\u0310\u0312\u0312\u0312\u0312\u0166\u0166\u0310\u0310\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u001e\u0166\u0166\u0312\u0312\u0166\u0166\u0166\u0166\u0310\u001e\u001e\u0312\u0312\u0312\u0312\u0166\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u001e\u0310\u0312\u001e\u0166\u0166\u001e\u0166\u0166\u0166\u0166\u0312\u0166\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u001e\u0166\u0166\u0312\u0166\u0166\u0166\u0166\u0310\u0312\u0312\u0166\u0312\u0166\u0166\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0166\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u016e\u016e\u016e\u016e\u0012\u0012\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0316\u016e\u016e\u0312\u016e\u016e\u016e\u0318\u031a\u016e\u016e\u016e\u016e\u016e\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u031c\u030c\u016e\u016e\u016e\u016e\u016e\u031e\u016e\u016e\u016e\u016e\u016e\u0320\u016e\u016e\u02f4\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u030e\u016e\u016e\u016e\u016e\u016e\u016e\u0166\u0166\u0166\u016e\u016e\u016e\u0322\u0316\u0324\u030a\u0324\u0326\u016e\u016e\u030a\u02e8\u016e\u016e\u016e\u016e\u016e\u01e8\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u016e\u016e\u016e\u016e\u016e\u01e8\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0328\u0328\u0328\u0328\u0328\u0328\u0328\u0328\u0328\u0328\u032a\u032a\u032a\u032a\u032a\u032a\u032a\u032a\u032a\u032a\u032c\u032c\u032c\u032c\u032c\u032c\u032c\u032c\u032c\u032c\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u032e\u032e\u032e\u032e\u032e\u032e\u0330\u032e\u032e\u032e\u032e\u032e\u032e\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0332\u0334\u0336\u0336\u0336\u0336\u0338\u033a\u033a\u033a\u033a\u033c\u016e\u016e\u016e\u016e\u016e\u030a\u016e\u016e\u016e\u016e\u016e\u033e\u016e\u016e\u016e\u016e\u033e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0166\u0340\u0342\u0344\u030a\u030a\u0346\u0348\u034a\u034a\u034a\u0346\u034c\u031e\u0316\u034a\u0346\u034a\u034e\u034a\u0346\u030a\u034a\u0346\u034a\u0346\u034a\u030a\u034a\u034a\u034a\u034a\u030a\u0346\u034a\u034a\u0346\u0346\u034a\u034a\u0316\u0316\u0316\u0316\u0316\u0316\u034a\u034a\u034a\u034a\u034a\u034c\u0346\u034c\u034c\u0346\u0346\u034a\u034a\u0350\u034a\u034a\u034a\u034a\u034a\u034c\u034a\u0326\u034a\u034a\u034a\u016e\u016e\u016e\u016e\u016e\u034a\u0326\u030a\u030a\u034c\u034c\u0346\u034a\u0326\u034a\u034a\u034c\u034a\u0316\u0352\u034a\u030a\u034a\u034a\u034a\u034a\u034a\u0354\u0356\u034a\u034a\u0316\u034a\u0346\u034a\u034a\u0324\u034c\u034c\u0356\u034a\u034a\u034a\u034a\u034a\u034a\u034a\u034a\u034a\u034c\u0356\u034a\u034a\u030a\u0316\u0326\u034c\u0358\u0356\u0354\u034a\u034a\u0346\u0354\u016e\u030a\u035a\u035c\u034c\u034a\u02e8\u02e8\u02e8\u016e\u016e\u030e\u016e\u030e\u016e\u016e\u016e\u035e\u016e\u016e\u016e\u016e\u030e\u02e8\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u02e8\u030e\u016e\u016e\u035e\u035e\u016e\u0322\u0316\u0322\u016e\u016e\u016e\u016e\u016e\u030e\u0346\u034a\u0012\u0012\u0012\u0012\u0012\u0012\u0012\u0360\u0360\u0360\u0360\u0360\u0328\u0328\u0328\u0328\u0328\u0362\u0362\u0362\u0362\u0362\u0322\u0316\u016e\u016e\u016e\u016e\u030e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u035e\u016e\u016e\u016e\u016e\u016e\u016e\u0322\u001e\u0310\u0364\u0366\u0312\u0310\u0312\u0166\u0166\u0310\u0312\u001e\u0166\u0166\u0312\u001e\u0166\u0312\u0312\u0012\u0012\u0012\u0012\u0012\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0368\u0166\u0166\u0166\u0166\u0166\u0166\u02ce\u036a\u036a\u036a\u036a\u036a\u036a\u036a\u036a\u036a\u036a\u0366\u0310\u0312\u0312\u001e\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0166\u0166\u0166\u0166\u001e\u0166\u0166\u0166\u0312\u0312\u0312\u0166\u0310\u0166\u0166\u0312\u0312\u001e\u0312\u0166\u0012\u0012\u001e\u0166\u0310\u0310\u0312\u0166\u0312\u0166\u0166\u0166\u0166\u0166\u0312\u0312\u0312\u0166\u0012\u0166\u0166\u0166\u0166\u0166\u0166\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u001e\u0312\u0312\u0166\u001e\u001e\u0310\u0310\u0312\u001e\u0166\u0166\u0312\u0166\u0166\u0166\u0312\u001e\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0310\u001e\u0166\u0166\u0166\u0166\u0166\u0312\u0166\u0166\u0312\u0312\u0310\u001e\u0310\u001e\u0166\u0310\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0166\u0312\u0312\u0312\u0312\u0310\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u0312\u001e\u0166\u0166\u001e\u001e\u0166\u0312\u0312\u001e\u0166\u0166\u0312\u001e\u0166\u0310\u0166\u0310\u0312\u0312\u0310\u0166\u016e\u016e\u030e\u030a\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0322\u035e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u0166\u030c\u02f4\u0166\u0166\u030c\u016e\u035e\u016e\u0322\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u014c\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u036c\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u013a\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144\u0144b\u021c\u00ac\u00aehh\u036e\u021c\u0268bfv\u008a\u008a\u00de\u021cbb\u0370\u016e\u016e\u0372h\u0374\u00f0b\u00f4\u00f4\u0376\u0010\u0378\u0010\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u037a\u00f4\u00f4\u037a\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u00f4\u013e\u01dc\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0150\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0152\u0010\u037c\u037c\u0010\u02b2\u02b4\u037c\u0010\u0010\u0010\u0010\u037e\u0010\u0246\u037c\u0010\u037c\u0012\u0012\u0012\u0012\u0010\u0010\u0380\u0010\u0010\u0010\u0010\u0010\u02a2\u0010\u0010\u0246\u0382\u0010\u0010\u0010\u0010\u0010\u0010\u016e\u0010\u0384\u036a\u036a\u036a\u0386\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u014c\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u00f4\u00f4\n\u0010\u0388\u038a\u0012\u0012\u0012\u0012\u0012\u016e\u0012\u0012\u0012\u0012\u038c\u038e\u0390\u0392\u0392\u0392\u0392\u00f0\u00f0\u0210\u0394\u00e4\u00e4\u016e\u0396\u0398\u039a\u016e\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u0150\u039c\u039e\u03a0\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u03a2\u00e4\u03a0\u00f4\u00f4\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u020a\u03a4\u03a4\u020a\u020a\u020a\u020a\u020a\u016e\u016e\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u031c\u01e8\u03a6\u03a6\u03a6\u03a6\u03a6\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u03a8\u03aa\u01d2\u01d2\u03ac\u03ae\u03ae\u03ae\u03ae\u03aeRR\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u016e\u0320\u03a6\u03a6\u03a6\u03a6\u03a6\u020a\u020a\u020a\u020a\u020a\u020a\u03b0\u03b0\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u03b2RRRRRRR\u020a\u020a\u020a\u020a\u020a\u020a\u016e\u016e\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u031c\u016e\u0320\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u016e\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u020a\u031c\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u024c\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u01e8\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u03b6\u0010\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u020e\u020e\u020e\u020e\u020e\u0096\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4bbbbbbb\u01c0\u0132\u03b8\u0152\u0152\u0152\u0152\u00f0\u03babbbbbbbbbbbbbb\u00de\u0152\u0096\u0096\u0096\u03bc\u03bc\u03bc\u03bc\u03be\u00f0\u0140\u0140\u0140\u00f4\u00f4\u00f4\u00f4\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u00e6\u03c0\u00e8\u00e8\u00e8\u00e8\u00e6bbbbbbb\u008abbbbbbbbbbbbbbb\u0260\u008a\u008a\u008afh\u036ebbbbb\u03c2\u03c4\u036e\u0092bb\u03c6bbbbbbbbbb\u021c\u021c\u0268\u021c\u021cbbbbbbbb\u03c8\u00a8h\u03ca\u00f4\u00f4b\u03cc\u03ccbb\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00de\u03ce\u03d0\u00de\u0092\u0096\u0096\u0096\u01e2\u0096\u01b8\u0096\u01c6\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01f8\u01bc\u01b6\u016e\u016e\u019a\u00f4\u01d2\u01d2\u01d2\u020a\u03d2\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0010\u0010\u00f4\u00f4\u00f4\u00f4\u01ba\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ba\u01ba\u01ba\u01ba\u01ba\u01ba\u01ba\u01ba\u017c\u00f4\u00f4\u00f4\u00f4\u0140\u0206\u0206\u0206\u0206\u0206\u00f4\u00f4\u00f4\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u0096\u0096\u0096\u0140\u01f2\u01f2\u01c6\u020e\u020e\u020e\u020e\u020e\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0152\u0152\u01aa\u00f0\u0140\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c6\u0152\u0152\u0152\u0152\u0152\u03d4\u00f4\u00f4\u00f4\u00f4\u00f4\u01ea\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01c0\u01ba\u0152\u0152\u01ba\u0152\u01ba\u0242\u0140\u0140\u0140\u0140\u0140\u0140\u013e\u0206\u0206\u0206\u0206\u0206\u00f4\u00f4\u0140\u0096\u0096\u01c6\u03a0\u0096\u0096\u0096\u0096\u0206\u0206\u0206\u0206\u0206\u0096\u0096\u01ca\u0096\u0096\u0096\u0096\u01c6\u0152\u0152\u01b6\u01bc\u01b6\u01bc\u01cc\u00f4\u00f4\u00f4\u00f4\u0096\u01c6\u0096\u0096\u0096\u0096\u01b6\u00f4\u0206\u0206\u0206\u0206\u0206\u00f4\u0140\u0140\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u03a0\u0096\u0096\u01fa\u020a\u01f8\u01b6\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01e2\u0152\u01e2\u01c6\u01e2\u0096\u0096\u01aa\u01c0\u01ca\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u01c8\u024c\u0140\u0096\u0096\u0096\u0096\u0096\u01f8\u0152\u01ba\u0140\u024c\u03d6\u019a\u00f4\u00f4\u00f4\u00f4\u01c8\u0096\u0096\u01ca\u01c8\u0096\u0096\u01ca\u01c8\u0096\u0096\u01ca\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u01ca\u0096\u0096\u0096\u01ca\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u008a\u00c6\u008a\u008a\u008a\u03d8\u00de\u00de\u008a\u008a\u008a\u008a\u0264\u00e6\u00f4\u00f4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u00b4\u0096\u01f8\u01bc\u01ba\u01b6\u03da\u0258\u00f4\u0206\u0206\u0206\u0206\u0206\u00f4\u00f4\u00f4\u0096\u0096\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u01ca\u00f4\u01c8\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00f4\u00f4\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03dc\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03de\u03b4\u03b4\u03b4\u03b4\u03b4\u03e0\u03b4\u03b4\u03b4\u03e2\u03b4\u03b4\u03e4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03e6\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03e8\u03ea\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03ec\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u00f4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u03b4\u00f4\u00f4\u00f4\u00b4\u00b4\u00b4\u025a\u00f4\u00f4\u00f4\u00f4\u00f4\u037a\u00b4\u00b4\u00f4\u00f4\u015e\u03ee\u015a\u015a\u015a\u015a\u03f0\u015a\u015a\u015a\u015a\u015a\u015a\u015c\u015a\u015a\u015c\u015c\u015a\u015e\u015c\u015a\u015a\u015a\u015a\u015a\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u03f2\u03f2\u03f2\u03f2\u03f2\u03f2\u03f2\u03f2\u03f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u019c\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u03f6\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u016e\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u00f4\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u00f4\u00f4\u00f4\u014c\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u00f4\u0176\u0176\u0176\u0176\u0176\u0176\u03f8\u016e\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u03fa\u0010\u0010\u0010\u03fc\u03fe\u00f4\u00f4\u00f4\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u00f0\u037e\u0400\u0402\u03f6\u03f6\u03f6\u03f6\u03f6\u03f6\u03f6\u03fe\u03fc\u03fe\u0010\u02b8\u0404\u001c\u0406\u0408\u0010\u040a\u036a\u036a\u040c\u0010\u040e\u0312\u0410\u0412\u02b0\u00f4\u00f4\u0176\u0176\u01ae\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u0176\u01ae\u0414\u0376\u0416\u000e\u0010\u0012\u0418\u0016\u0018\u0192\u0192\u0192\u0192\u0192\u001c\u001e ,............02\u02ce\u02be\u0012\u0010\u0096\u0096\u0096\u0096\u0096\u03a0\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u0096\u00e4\u00f4\u0096\u0096\u0096\u00f4\u0096\u0096\u0096\u00f4\u0096\u0096\u0096\u00f4\u0096\u01ca\u00f4:\u041a\u014e\u02d6\u02f4\u0166\u030c\u01e8\u00f4\u00f4\u00f4\u00f4\u041c\u041e\u016e\u00f4".toCharArray();
        A = new int[1056];
        B = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0440\u0000\u0000\u0000\u0000\u0000\u0000\u0440\u0000\u0000\u0000\u0000\u0000\u0460\u0460\u0000\u0000\u0000\u0000\u0000\u0000\u00002222\u0000\u0000\u0000\u0000 \u00001111\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u08401\u0000\u0000\u0000\u0840\u0000\u0000\u0000\u0000\u0000\u00001\u0000 \u0000\u00001\u0000\u0000\u0000\u0000\u0000222\u000021111\u000011212112121122122212221122222122112212221211122212101100201201121112222122122112122211111111111111111111111111111111111111110111100000\u0000\u0000001\u00000\u00000\u0000   %\u0000\u00001111\u000022 222\u00002\u00002212\u0000211111111111211222111111121\u000021212222211\u0000 \u0000\u00002211\u00002222\u0000\u00000\u0000\u00001111111\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 $$\u0000$\u0000$$\u0000000\u0000\u000000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000$\u0000\u0000\u0000\u0000\u000000000$ $  \u0000\u0000\u0000\u0000$0\u00000$\u0000\u0000 $00$$\u0000  0\u0000\u00000\u0000\u0000 \u0000\u00000  0 000\u0000\u0000\u00000$$ \u0000\u00000\u0000\u0000000\u0000$$$ 0$$$$$ 0   \u000000$\u000000\u0000$\u0000\u0000$$\u0000\u0000\u0000\u0000\u0000\u0000\u00000\u0000\u0000$\u0000\u0000\u0000\u0000\u00000$0\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00000$$$$0$0\u0000\u00000\u0000\u0000\u0000\u00000  \u0000  0\u0000\u0000\u0000\u0000\u0000     \u0000\u0000\u0000\u0000\u0000 $$022\u00002111\u000011\u0000   \u0000\u0000\u0000\u0000\u0000\u00002211\u00000\u000000\u0000\u0000\u00000000   \u0000\u0000\u0000\u0000\u0000\u0000 \u0000 000440 \u0000\u0000$ 0  1\u00002\u00000 1111111121112211\u0000211111111111100220\u00001\u00002222\u0000\u000011222\u00002222\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0400\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0840\u0000\u0000  \u0000\u0000\u0000\u0000\u0000\u0000\u0840\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00001\u0000\u0000\u0000\u0000\u00001\u0000\u00001\u0000\u0000\u0000 \u0000\u0000 \u0000\u04002\u0000\u00002122102\u0840\u00002\u000001220\u0871\u00002\u0000\u00001\u0000222211110002100\u0000\u0840\u0840\u0840\u0840\u0000\u0000\u0000\u0840\u0000\u0000\u0000\u0000\u0000\u0000\u08c0\u08c0\u0840\u0000\u0000\u0000\u0000\u0000\u0800\u0000\u0000\u0000\u0000\u08c0\u08c0\u0840\u0840\u08c0\u0000\u0000\u0000\u0000\u0000\u0000\u0006\u0006\u0846\u0006\u0005\u0005\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0840\u0000\u0000\u0840\u0840\u08c0\u08c0\u0000\u0840\u0800\u0000\u0800\u0800\u0800\u0800\u0840\u0800\u0a40\u0800\u0800\u0800\u0800\u0800\u08c0\u08c0\u0800\u0840\u0a40\u0ac0\u0ac0\u0a40\u0a40\u08c0\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0840\u0840\u0000\u0000\u0000\u0000121\u0000\u000021 \u0000\u0000\u0000\u0000\u00001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000088\u0000\u0000\u0000\u0000\u0000888\u0840088800\u0840 000000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0840\u0000\u0000880\u0000\u0000\u0000\u000000000\u000000\u0000\u0000211221\u0000\u000011210\u0000\u0000$ 0$1\u0000$\u0000\u0000\u0000\u0000\u000088888888888888$00\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 \u0420\u0000\u0000\u0000\u0000\u0000  \u0000  \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".toCharArray();
        charMap = new char[][][]{new char[][]{{'\u00df'}, {'S', 'S'}}, new char[][]{{'\u0130'}, {'\u0130'}}, new char[][]{{'\u0149'}, {'\u02bc', 'N'}}, new char[][]{{'\u01f0'}, {'J', '\u030c'}}, new char[][]{{'\u0390'}, {'\u0399', '\u0308', '\u0301'}}, new char[][]{{'\u03b0'}, {'\u03a5', '\u0308', '\u0301'}}, new char[][]{{'\u0587'}, {'\u0535', '\u0552'}}, new char[][]{{'\u1e96'}, {'H', '\u0331'}}, new char[][]{{'\u1e97'}, {'T', '\u0308'}}, new char[][]{{'\u1e98'}, {'W', '\u030a'}}, new char[][]{{'\u1e99'}, {'Y', '\u030a'}}, new char[][]{{'\u1e9a'}, {'A', '\u02be'}}, new char[][]{{'\u1f50'}, {'\u03a5', '\u0313'}}, new char[][]{{'\u1f52'}, {'\u03a5', '\u0313', '\u0300'}}, new char[][]{{'\u1f54'}, {'\u03a5', '\u0313', '\u0301'}}, new char[][]{{'\u1f56'}, {'\u03a5', '\u0313', '\u0342'}}, new char[][]{{'\u1f80'}, {'\u1f08', '\u0399'}}, new char[][]{{'\u1f81'}, {'\u1f09', '\u0399'}}, new char[][]{{'\u1f82'}, {'\u1f0a', '\u0399'}}, new char[][]{{'\u1f83'}, {'\u1f0b', '\u0399'}}, new char[][]{{'\u1f84'}, {'\u1f0c', '\u0399'}}, new char[][]{{'\u1f85'}, {'\u1f0d', '\u0399'}}, new char[][]{{'\u1f86'}, {'\u1f0e', '\u0399'}}, new char[][]{{'\u1f87'}, {'\u1f0f', '\u0399'}}, new char[][]{{'\u1f88'}, {'\u1f08', '\u0399'}}, new char[][]{{'\u1f89'}, {'\u1f09', '\u0399'}}, new char[][]{{'\u1f8a'}, {'\u1f0a', '\u0399'}}, new char[][]{{'\u1f8b'}, {'\u1f0b', '\u0399'}}, new char[][]{{'\u1f8c'}, {'\u1f0c', '\u0399'}}, new char[][]{{'\u1f8d'}, {'\u1f0d', '\u0399'}}, new char[][]{{'\u1f8e'}, {'\u1f0e', '\u0399'}}, new char[][]{{'\u1f8f'}, {'\u1f0f', '\u0399'}}, new char[][]{{'\u1f90'}, {'\u1f28', '\u0399'}}, new char[][]{{'\u1f91'}, {'\u1f29', '\u0399'}}, new char[][]{{'\u1f92'}, {'\u1f2a', '\u0399'}}, new char[][]{{'\u1f93'}, {'\u1f2b', '\u0399'}}, new char[][]{{'\u1f94'}, {'\u1f2c', '\u0399'}}, new char[][]{{'\u1f95'}, {'\u1f2d', '\u0399'}}, new char[][]{{'\u1f96'}, {'\u1f2e', '\u0399'}}, new char[][]{{'\u1f97'}, {'\u1f2f', '\u0399'}}, new char[][]{{'\u1f98'}, {'\u1f28', '\u0399'}}, new char[][]{{'\u1f99'}, {'\u1f29', '\u0399'}}, new char[][]{{'\u1f9a'}, {'\u1f2a', '\u0399'}}, new char[][]{{'\u1f9b'}, {'\u1f2b', '\u0399'}}, new char[][]{{'\u1f9c'}, {'\u1f2c', '\u0399'}}, new char[][]{{'\u1f9d'}, {'\u1f2d', '\u0399'}}, new char[][]{{'\u1f9e'}, {'\u1f2e', '\u0399'}}, new char[][]{{'\u1f9f'}, {'\u1f2f', '\u0399'}}, new char[][]{{'\u1fa0'}, {'\u1f68', '\u0399'}}, new char[][]{{'\u1fa1'}, {'\u1f69', '\u0399'}}, new char[][]{{'\u1fa2'}, {'\u1f6a', '\u0399'}}, new char[][]{{'\u1fa3'}, {'\u1f6b', '\u0399'}}, new char[][]{{'\u1fa4'}, {'\u1f6c', '\u0399'}}, new char[][]{{'\u1fa5'}, {'\u1f6d', '\u0399'}}, new char[][]{{'\u1fa6'}, {'\u1f6e', '\u0399'}}, new char[][]{{'\u1fa7'}, {'\u1f6f', '\u0399'}}, new char[][]{{'\u1fa8'}, {'\u1f68', '\u0399'}}, new char[][]{{'\u1fa9'}, {'\u1f69', '\u0399'}}, new char[][]{{'\u1faa'}, {'\u1f6a', '\u0399'}}, new char[][]{{'\u1fab'}, {'\u1f6b', '\u0399'}}, new char[][]{{'\u1fac'}, {'\u1f6c', '\u0399'}}, new char[][]{{'\u1fad'}, {'\u1f6d', '\u0399'}}, new char[][]{{'\u1fae'}, {'\u1f6e', '\u0399'}}, new char[][]{{'\u1faf'}, {'\u1f6f', '\u0399'}}, new char[][]{{'\u1fb2'}, {'\u1fba', '\u0399'}}, new char[][]{{'\u1fb3'}, {'\u0391', '\u0399'}}, new char[][]{{'\u1fb4'}, {'\u0386', '\u0399'}}, new char[][]{{'\u1fb6'}, {'\u0391', '\u0342'}}, new char[][]{{'\u1fb7'}, {'\u0391', '\u0342', '\u0399'}}, new char[][]{{'\u1fbc'}, {'\u0391', '\u0399'}}, new char[][]{{'\u1fc2'}, {'\u1fca', '\u0399'}}, new char[][]{{'\u1fc3'}, {'\u0397', '\u0399'}}, new char[][]{{'\u1fc4'}, {'\u0389', '\u0399'}}, new char[][]{{'\u1fc6'}, {'\u0397', '\u0342'}}, new char[][]{{'\u1fc7'}, {'\u0397', '\u0342', '\u0399'}}, new char[][]{{'\u1fcc'}, {'\u0397', '\u0399'}}, new char[][]{{'\u1fd2'}, {'\u0399', '\u0308', '\u0300'}}, new char[][]{{'\u1fd3'}, {'\u0399', '\u0308', '\u0301'}}, new char[][]{{'\u1fd6'}, {'\u0399', '\u0342'}}, new char[][]{{'\u1fd7'}, {'\u0399', '\u0308', '\u0342'}}, new char[][]{{'\u1fe2'}, {'\u03a5', '\u0308', '\u0300'}}, new char[][]{{'\u1fe3'}, {'\u03a5', '\u0308', '\u0301'}}, new char[][]{{'\u1fe4'}, {'\u03a1', '\u0313'}}, new char[][]{{'\u1fe6'}, {'\u03a5', '\u0342'}}, new char[][]{{'\u1fe7'}, {'\u03a5', '\u0308', '\u0342'}}, new char[][]{{'\u1ff2'}, {'\u1ffa', '\u0399'}}, new char[][]{{'\u1ff3'}, {'\u03a9', '\u0399'}}, new char[][]{{'\u1ff4'}, {'\u038f', '\u0399'}}, new char[][]{{'\u1ff6'}, {'\u03a9', '\u0342'}}, new char[][]{{'\u1ff7'}, {'\u03a9', '\u0342', '\u0399'}}, new char[][]{{'\u1ffc'}, {'\u03a9', '\u0399'}}, new char[][]{{'\ufb00'}, {'F', 'F'}}, new char[][]{{'\ufb01'}, {'F', 'I'}}, new char[][]{{'\ufb02'}, {'F', 'L'}}, new char[][]{{'\ufb03'}, {'F', 'F', 'I'}}, new char[][]{{'\ufb04'}, {'F', 'F', 'L'}}, new char[][]{{'\ufb05'}, {'S', 'T'}}, new char[][]{{'\ufb06'}, {'S', 'T'}}, new char[][]{{'\ufb13'}, {'\u0544', '\u0546'}}, new char[][]{{'\ufb14'}, {'\u0544', '\u0535'}}, new char[][]{{'\ufb15'}, {'\u0544', '\u053b'}}, new char[][]{{'\ufb16'}, {'\u054e', '\u0546'}}, new char[][]{{'\ufb17'}, {'\u0544', '\u053d'}}};
        char[] data = A_DATA.toCharArray();
        assert (data.length == 2112);
        int i = 0;
        int j = 0;
        while (i < 2112) {
            int entry = data[i++] << 16;
            CharacterData00.A[j++] = entry | data[i++];
        }
    }
}

