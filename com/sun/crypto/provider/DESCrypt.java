/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.DESConstants;
import com.sun.crypto.provider.SymmetricCipher;
import java.security.InvalidKeyException;

class DESCrypt
extends SymmetricCipher
implements DESConstants {
    private static final int[] s0p = new int[]{0x410100, 65536, 0x40400000, 0x40410100, 0x400000, 0x40010100, 0x40010000, 0x40400000, 0x40010100, 0x410100, 0x410000, 0x40000100, 0x40400100, 0x400000, 0, 0x40010000, 65536, 0x40000000, 0x400100, 65792, 0x40410100, 0x410000, 0x40000100, 0x400100, 0x40000000, 256, 65792, 0x40410000, 256, 0x40400100, 0x40410000, 0, 0, 0x40410100, 0x400100, 0x40010000, 0x410100, 65536, 0x40000100, 0x400100, 0x40410000, 256, 65792, 0x40400000, 0x40010100, 0x40000000, 0x40400000, 0x410000, 0x40410100, 65792, 0x410000, 0x40400100, 0x400000, 0x40000100, 0x40010000, 0, 65536, 0x400000, 0x40400100, 0x410100, 0x40000000, 0x40410000, 256, 0x40010100};
    private static final int[] s1p = new int[]{134352898, 0, 135168, 0x8020000, 0x8000002, 4098, 0x8001000, 135168, 4096, 0x8020002, 2, 0x8001000, 131074, 134352896, 0x8020000, 2, 131072, 134221826, 0x8020002, 4096, 135170, 0x8000000, 0, 131074, 134221826, 135170, 134352896, 0x8000002, 0x8000000, 131072, 4098, 134352898, 131074, 134352896, 0x8001000, 135170, 134352898, 131074, 0x8000002, 0, 0x8000000, 4098, 131072, 0x8020002, 4096, 0x8000000, 135170, 134221826, 134352896, 4096, 0, 0x8000002, 2, 134352898, 135168, 0x8020000, 0x8020002, 131072, 4098, 0x8001000, 134221826, 2, 0x8020000, 135168};
    private static final int[] s2p = new int[]{0x20800000, 0x808020, 32, 0x20800020, 0x20008000, 0x800000, 0x20800020, 32800, 0x800020, 32768, 0x808000, 0x20000000, 0x20808020, 0x20000020, 0x20000000, 0x20808000, 0, 0x20008000, 0x808020, 32, 0x20000020, 0x20808020, 32768, 0x20800000, 0x20808000, 0x800020, 0x20008020, 0x808000, 32800, 0, 0x800000, 0x20008020, 0x808020, 32, 0x20000000, 32768, 0x20000020, 0x20008000, 0x808000, 0x20800020, 0, 0x808020, 32800, 0x20808000, 0x20008000, 0x800000, 0x20808020, 0x20000000, 0x20008020, 0x20800000, 0x800000, 0x20808020, 32768, 0x800020, 0x20800020, 32800, 0x800020, 0, 0x20808000, 0x20000020, 0x20800000, 0x20008020, 32, 0x808000};
    private static final int[] s3p = new int[]{524801, 0x2000200, 1, 34079233, 0, 0x2080000, 0x2000201, 524289, 0x2080200, 0x2000001, 0x2000000, 513, 0x2000001, 524801, 524288, 0x2000000, 34078721, 524800, 512, 1, 524800, 0x2000201, 0x2080000, 512, 513, 0, 524289, 0x2080200, 0x2000200, 34078721, 34079233, 524288, 34078721, 513, 524288, 0x2000001, 524800, 0x2000200, 1, 0x2080000, 0x2000201, 0, 512, 524289, 0, 34078721, 0x2080200, 512, 0x2000000, 34079233, 524801, 524288, 34079233, 1, 0x2000200, 524801, 524289, 524800, 0x2080000, 0x2000201, 513, 0x2000000, 0x2000001, 0x2080200};
    private static final int[] s4p = new int[]{0x1000000, 8192, 128, 16785540, 16785412, 0x1000080, 8324, 0x1002000, 8192, 4, 0x1000004, 8320, 16777348, 16785412, 16785536, 0, 8320, 0x1000000, 8196, 132, 0x1000080, 8324, 0, 0x1000004, 4, 16777348, 16785540, 8196, 0x1002000, 128, 132, 16785536, 16785536, 16777348, 8196, 0x1002000, 8192, 4, 0x1000004, 0x1000080, 0x1000000, 8320, 16785540, 0, 8324, 0x1000000, 128, 8196, 16777348, 128, 0, 16785540, 16785412, 16785536, 132, 8192, 8320, 16785412, 0x1000080, 132, 4, 8324, 0x1002000, 0x1000004};
    private static final int[] s5p = new int[]{0x10000008, 262152, 0, 0x10040400, 262152, 1024, 268436488, 262144, 1032, 268698632, 263168, 0x10000000, 0x10000400, 0x10000008, 0x10040000, 263176, 262144, 268436488, 268697608, 0, 1024, 8, 0x10040400, 268697608, 268698632, 0x10040000, 0x10000000, 1032, 8, 263168, 263176, 0x10000400, 1032, 0x10000000, 0x10000400, 263176, 0x10040400, 262152, 0, 0x10000400, 0x10000000, 1024, 268697608, 262144, 262152, 268698632, 263168, 8, 268698632, 263168, 262144, 268436488, 0x10000008, 0x10040000, 263176, 0, 1024, 0x10000008, 268436488, 0x10040400, 0x10040000, 1032, 8, 268697608};
    private static final int[] s6p = new int[]{2048, 64, 0x200040, -2145386496, -2145384384, -2147481600, 2112, 0, 0x200000, -2145386432, -2147483584, 0x200800, Integer.MIN_VALUE, 2099264, 0x200800, -2147483584, -2145386432, 2048, -2147481600, -2145384384, 0, 0x200040, -2145386496, 2112, -2145384448, -2147481536, 2099264, Integer.MIN_VALUE, -2147481536, -2145384448, 64, 0x200000, -2147481536, 0x200800, -2145384448, -2147483584, 2048, 64, 0x200000, -2145384448, -2145386432, -2147481536, 2112, 0, 64, -2145386496, Integer.MIN_VALUE, 0x200040, 0, -2145386432, 0x200040, 2112, -2147483584, 2048, -2145384384, 0x200000, 2099264, Integer.MIN_VALUE, -2147481600, -2145384384, -2145386496, 2099264, 0x200800, -2147481600};
    private static final int[] s7p = new int[]{0x4100010, 0x4104000, 16400, 0, 0x4004000, 0x100010, 0x4100000, 0x4104010, 16, 0x4000000, 0x104000, 16400, 0x104010, 0x4004010, 0x4000010, 0x4100000, 16384, 0x104010, 0x100010, 0x4004000, 0x4104010, 0x4000010, 0, 0x104000, 0x4000000, 0x100000, 0x4004010, 0x4100010, 0x100000, 16384, 0x4104000, 16, 0x100000, 16384, 0x4000010, 0x4104010, 16400, 0x4000000, 0, 0x104000, 0x4100010, 0x4004010, 0x4004000, 0x100010, 0x4104000, 16, 0x100010, 0x4004000, 0x4104010, 0x100000, 0x4100000, 0x4000010, 0x104000, 16400, 0x4004010, 0x4100000, 16, 0x4104000, 0x104010, 0, 0x4000000, 0x4100010, 16384, 0x104010};
    private static final int[] permRight0 = new int[]{0, 0x40000000, 0x400000, 0x40400000, 16384, 0x40004000, 0x404000, 0x40404000, 64, 0x40000040, 0x400040, 0x40400040, 16448, 0x40004040, 0x404040, 0x40404040};
    private static final int[] permLeft1 = new int[]{0, 0x40000000, 0x400000, 0x40400000, 16384, 0x40004000, 0x404000, 0x40404000, 64, 0x40000040, 0x400040, 0x40400040, 16448, 0x40004040, 0x404040, 0x40404040};
    private static final int[] permRight2 = new int[]{0, 0x10000000, 0x100000, 0x10100000, 4096, 0x10001000, 0x101000, 0x10101000, 16, 0x10000010, 0x100010, 0x10100010, 4112, 0x10001010, 0x101010, 0x10101010};
    private static final int[] permLeft3 = new int[]{0, 0x10000000, 0x100000, 0x10100000, 4096, 0x10001000, 0x101000, 0x10101000, 16, 0x10000010, 0x100010, 0x10100010, 4112, 0x10001010, 0x101010, 0x10101010};
    private static final int[] permRight4 = new int[]{0, 0x4000000, 262144, 0x4040000, 1024, 0x4000400, 263168, 0x4040400, 4, 0x4000004, 262148, 0x4040004, 1028, 0x4000404, 263172, 0x4040404};
    private static final int[] permLeft5 = new int[]{0, 0x4000000, 262144, 0x4040000, 1024, 0x4000400, 263168, 0x4040400, 4, 0x4000004, 262148, 0x4040004, 1028, 0x4000404, 263172, 0x4040404};
    private static final int[] permRight6 = new int[]{0, 0x1000000, 65536, 0x1010000, 256, 0x1000100, 65792, 0x1010100, 1, 0x1000001, 65537, 0x1010001, 257, 0x1000101, 65793, 0x1010101};
    private static final int[] permLeft7 = new int[]{0, 0x1000000, 65536, 0x1010000, 256, 0x1000100, 65792, 0x1010100, 1, 0x1000001, 65537, 0x1010001, 257, 0x1000101, 65793, 0x1010101};
    private static final int[] permRight8 = new int[]{0, Integer.MIN_VALUE, 0x800000, -2139095040, 32768, -2147450880, 0x808000, -2139062272, 128, -2147483520, 0x800080, -2139094912, 32896, -2147450752, 0x808080, -2139062144};
    private static final int[] permLeft9 = new int[]{0, Integer.MIN_VALUE, 0x800000, -2139095040, 32768, -2147450880, 0x808000, -2139062272, 128, -2147483520, 0x800080, -2139094912, 32896, -2147450752, 0x808080, -2139062144};
    private static final int[] permRightA = new int[]{0, 0x20000000, 0x200000, 0x20200000, 8192, 0x20002000, 0x202000, 0x20202000, 32, 0x20000020, 0x200020, 0x20200020, 8224, 0x20002020, 0x202020, 0x20202020};
    private static final int[] permLeftB = new int[]{0, 0x20000000, 0x200000, 0x20200000, 8192, 0x20002000, 0x202000, 0x20202000, 32, 0x20000020, 0x200020, 0x20200020, 8224, 0x20002020, 0x202020, 0x20202020};
    private static final int[] permRightC = new int[]{0, 0x8000000, 524288, 0x8080000, 2048, 0x8000800, 526336, 0x8080800, 8, 0x8000008, 524296, 0x8080008, 2056, 0x8000808, 526344, 0x8080808};
    private static final int[] permLeftD = new int[]{0, 0x8000000, 524288, 0x8080000, 2048, 0x8000800, 526336, 0x8080800, 8, 0x8000008, 524296, 0x8080008, 2056, 0x8000808, 526344, 0x8080808};
    private static final int[] permRightE = new int[]{0, 0x2000000, 131072, 0x2020000, 512, 0x2000200, 131584, 0x2020200, 2, 0x2000002, 131074, 0x2020002, 514, 0x2000202, 131586, 0x2020202};
    private static final int[] permLeftF = new int[]{0, 0x2000000, 131072, 0x2020000, 512, 0x2000200, 131584, 0x2020200, 2, 0x2000002, 131074, 0x2020002, 514, 0x2000202, 131586, 0x2020202};
    private static final int[] initPermLeft0 = new int[]{0, 32768, 0, 32768, 128, 32896, 128, 32896, 0, 32768, 0, 32768, 128, 32896, 128, 32896};
    private static final int[] initPermRight0 = new int[]{0, 0, 32768, 32768, 0, 0, 32768, 32768, 128, 128, 32896, 32896, 128, 128, 32896, 32896};
    private static final int[] initPermLeft1 = new int[]{0, Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 0x800000, -2139095040, 0x800000, -2139095040, 0, Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 0x800000, -2139095040, 0x800000, -2139095040};
    private static final int[] initPermRight1 = new int[]{0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0x800000, 0x800000, -2139095040, -2139095040, 0x800000, 0x800000, -2139095040, -2139095040};
    private static final int[] initPermLeft2 = new int[]{0, 16384, 0, 16384, 64, 16448, 64, 16448, 0, 16384, 0, 16384, 64, 16448, 64, 16448};
    private static final int[] initPermRight2 = new int[]{0, 0, 16384, 16384, 0, 0, 16384, 16384, 64, 64, 16448, 16448, 64, 64, 16448, 16448};
    private static final int[] initPermLeft3 = new int[]{0, 0x40000000, 0, 0x40000000, 0x400000, 0x40400000, 0x400000, 0x40400000, 0, 0x40000000, 0, 0x40000000, 0x400000, 0x40400000, 0x400000, 0x40400000};
    private static final int[] initPermRight3 = new int[]{0, 0, 0x40000000, 0x40000000, 0, 0, 0x40000000, 0x40000000, 0x400000, 0x400000, 0x40400000, 0x40400000, 0x400000, 0x400000, 0x40400000, 0x40400000};
    private static final int[] initPermLeft4 = new int[]{0, 8192, 0, 8192, 32, 8224, 32, 8224, 0, 8192, 0, 8192, 32, 8224, 32, 8224};
    private static final int[] initPermRight4 = new int[]{0, 0, 8192, 8192, 0, 0, 8192, 8192, 32, 32, 8224, 8224, 32, 32, 8224, 8224};
    private static final int[] initPermLeft5 = new int[]{0, 0x20000000, 0, 0x20000000, 0x200000, 0x20200000, 0x200000, 0x20200000, 0, 0x20000000, 0, 0x20000000, 0x200000, 0x20200000, 0x200000, 0x20200000};
    private static final int[] initPermRight5 = new int[]{0, 0, 0x20000000, 0x20000000, 0, 0, 0x20000000, 0x20000000, 0x200000, 0x200000, 0x20200000, 0x20200000, 0x200000, 0x200000, 0x20200000, 0x20200000};
    private static final int[] initPermLeft6 = new int[]{0, 4096, 0, 4096, 16, 4112, 16, 4112, 0, 4096, 0, 4096, 16, 4112, 16, 4112};
    private static final int[] initPermRight6 = new int[]{0, 0, 4096, 4096, 0, 0, 4096, 4096, 16, 16, 4112, 4112, 16, 16, 4112, 4112};
    private static final int[] initPermLeft7 = new int[]{0, 0x10000000, 0, 0x10000000, 0x100000, 0x10100000, 0x100000, 0x10100000, 0, 0x10000000, 0, 0x10000000, 0x100000, 0x10100000, 0x100000, 0x10100000};
    private static final int[] initPermRight7 = new int[]{0, 0, 0x10000000, 0x10000000, 0, 0, 0x10000000, 0x10000000, 0x100000, 0x100000, 0x10100000, 0x10100000, 0x100000, 0x100000, 0x10100000, 0x10100000};
    private static final int[] initPermLeft8 = new int[]{0, 2048, 0, 2048, 8, 2056, 8, 2056, 0, 2048, 0, 2048, 8, 2056, 8, 2056};
    private static final int[] initPermRight8 = new int[]{0, 0, 2048, 2048, 0, 0, 2048, 2048, 8, 8, 2056, 2056, 8, 8, 2056, 2056};
    private static final int[] initPermLeft9 = new int[]{0, 0x8000000, 0, 0x8000000, 524288, 0x8080000, 524288, 0x8080000, 0, 0x8000000, 0, 0x8000000, 524288, 0x8080000, 524288, 0x8080000};
    private static final int[] initPermRight9 = new int[]{0, 0, 0x8000000, 0x8000000, 0, 0, 0x8000000, 0x8000000, 524288, 524288, 0x8080000, 0x8080000, 524288, 524288, 0x8080000, 0x8080000};
    private static final int[] initPermLeftA = new int[]{0, 1024, 0, 1024, 4, 1028, 4, 1028, 0, 1024, 0, 1024, 4, 1028, 4, 1028};
    private static final int[] initPermRightA = new int[]{0, 0, 1024, 1024, 0, 0, 1024, 1024, 4, 4, 1028, 1028, 4, 4, 1028, 1028};
    private static final int[] initPermLeftB = new int[]{0, 0x4000000, 0, 0x4000000, 262144, 0x4040000, 262144, 0x4040000, 0, 0x4000000, 0, 0x4000000, 262144, 0x4040000, 262144, 0x4040000};
    private static final int[] initPermRightB = new int[]{0, 0, 0x4000000, 0x4000000, 0, 0, 0x4000000, 0x4000000, 262144, 262144, 0x4040000, 0x4040000, 262144, 262144, 0x4040000, 0x4040000};
    private static final int[] initPermLeftC = new int[]{0, 512, 0, 512, 2, 514, 2, 514, 0, 512, 0, 512, 2, 514, 2, 514};
    private static final int[] initPermRightC = new int[]{0, 0, 512, 512, 0, 0, 512, 512, 2, 2, 514, 514, 2, 2, 514, 514};
    private static final int[] initPermLeftD = new int[]{0, 0x2000000, 0, 0x2000000, 131072, 0x2020000, 131072, 0x2020000, 0, 0x2000000, 0, 0x2000000, 131072, 0x2020000, 131072, 0x2020000};
    private static final int[] initPermRightD = new int[]{0, 0, 0x2000000, 0x2000000, 0, 0, 0x2000000, 0x2000000, 131072, 131072, 0x2020000, 0x2020000, 131072, 131072, 0x2020000, 0x2020000};
    private static final int[] initPermLeftE = new int[]{0, 256, 0, 256, 1, 257, 1, 257, 0, 256, 0, 256, 1, 257, 1, 257};
    private static final int[] initPermRightE = new int[]{0, 0, 256, 256, 0, 0, 256, 256, 1, 1, 257, 257, 1, 1, 257, 257};
    private static final int[] initPermLeftF = new int[]{0, 0x1000000, 0, 0x1000000, 65536, 0x1010000, 65536, 0x1010000, 0, 0x1000000, 0, 0x1000000, 65536, 0x1010000, 65536, 0x1010000};
    private static final int[] initPermRightF = new int[]{0, 0, 0x1000000, 0x1000000, 0, 0, 0x1000000, 0x1000000, 65536, 65536, 0x1010000, 0x1010000, 65536, 65536, 0x1010000, 0x1010000};
    byte[] expandedKey = null;
    boolean decrypting = false;

    DESCrypt() {
    }

    @Override
    int getBlockSize() {
        return 8;
    }

    @Override
    void init(boolean decrypting, String algorithm, byte[] rawKey) throws InvalidKeyException {
        this.decrypting = decrypting;
        if (!algorithm.equalsIgnoreCase("DES")) {
            throw new InvalidKeyException("Wrong algorithm: DES required");
        }
        if (rawKey.length != 8) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.expandKey(rawKey);
    }

    @Override
    void encryptBlock(byte[] plain, int plainOffset, byte[] cipher, int cipherOffset) {
        this.cipherBlock(plain, plainOffset, cipher, cipherOffset);
    }

    @Override
    void decryptBlock(byte[] cipher, int cipherOffset, byte[] plain, int plainOffset) {
        this.cipherBlock(cipher, cipherOffset, plain, plainOffset);
    }

    void cipherBlock(byte[] in, int inOffset, byte[] out, int outOffset) {
        int temp;
        int j;
        int offset;
        int left = DESCrypt.initialPermutationLeft(in, inOffset);
        int right = DESCrypt.initialPermutationRight(in, inOffset);
        byte[] key = this.expandedKey;
        if (this.decrypting) {
            offset = 8;
            j = 120;
        } else {
            offset = -8;
            j = 0;
        }
        for (int i = 0; i < 16; ++i) {
            temp = right << 1 | right >> 31 & 1;
            left ^= s0p[temp & 0x3F ^ key[j + 0]] ^ s1p[temp >> 4 & 0x3F ^ key[j + 1]] ^ s2p[temp >> 8 & 0x3F ^ key[j + 2]] ^ s3p[temp >> 12 & 0x3F ^ key[j + 3]] ^ s4p[temp >> 16 & 0x3F ^ key[j + 4]] ^ s5p[temp >> 20 & 0x3F ^ key[j + 5]] ^ s6p[temp >> 24 & 0x3F ^ key[j + 6]];
            temp = (right & 1) << 5 | right >> 27 & 0x1F;
            left ^= s7p[temp ^ key[j + 7]];
            temp = left;
            left = right;
            right = temp;
            j -= offset;
        }
        temp = left;
        left = right;
        right = temp;
        DESCrypt.perm(left, right, out, outOffset);
    }

    private static void perm(int left, int right, byte[] out, int offset) {
        int temp = left;
        int high = permRight0[temp & 0xF];
        int low = permLeft1[(temp >>= 4) & 0xF];
        high |= permRight2[(temp >>= 4) & 0xF];
        low |= permLeft3[(temp >>= 4) & 0xF];
        high |= permRight4[(temp >>= 4) & 0xF];
        low |= permLeft5[(temp >>= 4) & 0xF];
        high |= permRight6[(temp >>= 4) & 0xF];
        low |= permLeft7[(temp >>= 4) & 0xF];
        temp = right;
        high |= permRight8[temp & 0xF];
        low |= permLeft9[(temp >>= 4) & 0xF];
        high |= permRightA[(temp >>= 4) & 0xF];
        low |= permLeftB[(temp >>= 4) & 0xF];
        high |= permRightC[(temp >>= 4) & 0xF];
        low |= permLeftD[(temp >>= 4) & 0xF];
        out[offset + 0] = (byte)(low |= permLeftF[(temp >>= 4) & 0xF]);
        out[offset + 1] = (byte)(low >> 8);
        out[offset + 2] = (byte)(low >> 16);
        out[offset + 3] = (byte)(low >> 24);
        out[offset + 4] = (byte)(high |= permRightE[(temp >>= 4) & 0xF]);
        out[offset + 5] = (byte)(high >> 8);
        out[offset + 6] = (byte)(high >> 16);
        out[offset + 7] = (byte)(high >> 24);
    }

    private static int initialPermutationLeft(byte[] block, int offset) {
        int l = initPermLeft1[block[offset] & 0xF];
        l |= initPermLeft0[block[offset] >> 4 & 0xF];
        l |= initPermLeft3[block[offset + 1] & 0xF];
        l |= initPermLeft2[block[offset + 1] >> 4 & 0xF];
        l |= initPermLeft5[block[offset + 2] & 0xF];
        l |= initPermLeft4[block[offset + 2] >> 4 & 0xF];
        l |= initPermLeft7[block[offset + 3] & 0xF];
        l |= initPermLeft6[block[offset + 3] >> 4 & 0xF];
        l |= initPermLeft9[block[offset + 4] & 0xF];
        l |= initPermLeft8[block[offset + 4] >> 4 & 0xF];
        l |= initPermLeftB[block[offset + 5] & 0xF];
        l |= initPermLeftA[block[offset + 5] >> 4 & 0xF];
        l |= initPermLeftD[block[offset + 6] & 0xF];
        l |= initPermLeftC[block[offset + 6] >> 4 & 0xF];
        l |= initPermLeftF[block[offset + 7] & 0xF];
        return l |= initPermLeftE[block[offset + 7] >> 4 & 0xF];
    }

    private static int initialPermutationRight(byte[] block, int offset) {
        int l = initPermRight1[block[offset] & 0xF];
        l |= initPermRight0[block[offset] >> 4 & 0xF];
        l |= initPermRight3[block[offset + 1] & 0xF];
        l |= initPermRight2[block[offset + 1] >> 4 & 0xF];
        l |= initPermRight5[block[offset + 2] & 0xF];
        l |= initPermRight4[block[offset + 2] >> 4 & 0xF];
        l |= initPermRight7[block[offset + 3] & 0xF];
        l |= initPermRight6[block[offset + 3] >> 4 & 0xF];
        l |= initPermRight9[block[offset + 4] & 0xF];
        l |= initPermRight8[block[offset + 4] >> 4 & 0xF];
        l |= initPermRightB[block[offset + 5] & 0xF];
        l |= initPermRightA[block[offset + 5] >> 4 & 0xF];
        l |= initPermRightD[block[offset + 6] & 0xF];
        l |= initPermRightC[block[offset + 6] >> 4 & 0xF];
        l |= initPermRightF[block[offset + 7] & 0xF];
        return l |= initPermRightE[block[offset + 7] >> 4 & 0xF];
    }

    void expandKey(byte[] key) {
        byte[] ek = new byte[128];
        byte octet = key[0];
        if ((octet & 0x80) != 0) {
            ek[3] = (byte)(ek[3] | 2);
            ek[9] = (byte)(ek[9] | 8);
            ek[18] = (byte)(ek[18] | 8);
            ek[27] = (byte)(ek[27] | 0x20);
            ek[33] = (byte)(ek[33] | 2);
            ek[42] = (byte)(ek[42] | 0x10);
            ek[48] = (byte)(ek[48] | 8);
            ek[65] = (byte)(ek[65] | 0x10);
            ek[74] = (byte)(ek[74] | 2);
            ek[80] = (byte)(ek[80] | 2);
            ek[89] = (byte)(ek[89] | 4);
            ek[99] = (byte)(ek[99] | 0x10);
            ek[104] = (byte)(ek[104] | 4);
            ek[122] = (byte)(ek[122] | 0x20);
        }
        if ((octet & 0x40) != 0) {
            ek[1] = (byte)(ek[1] | 4);
            ek[8] = (byte)(ek[8] | 1);
            ek[18] = (byte)(ek[18] | 4);
            ek[25] = (byte)(ek[25] | 0x20);
            ek[34] = (byte)(ek[34] | 0x20);
            ek[41] = (byte)(ek[41] | 8);
            ek[50] = (byte)(ek[50] | 8);
            ek[59] = (byte)(ek[59] | 0x20);
            ek[64] = (byte)(ek[64] | 0x10);
            ek[75] = (byte)(ek[75] | 4);
            ek[90] = (byte)(ek[90] | 1);
            ek[97] = (byte)(ek[97] | 0x10);
            ek[106] = (byte)(ek[106] | 2);
            ek[112] = (byte)(ek[112] | 2);
            ek[123] = (byte)(ek[123] | 1);
        }
        if ((octet & 0x20) != 0) {
            ek[2] = (byte)(ek[2] | 1);
            ek[19] = (byte)(ek[19] | 8);
            ek[35] = (byte)(ek[35] | 1);
            ek[40] = (byte)(ek[40] | 1);
            ek[50] = (byte)(ek[50] | 4);
            ek[57] = (byte)(ek[57] | 0x20);
            ek[75] = (byte)(ek[75] | 2);
            ek[80] = (byte)(ek[80] | 0x20);
            ek[89] = (byte)(ek[89] | 1);
            ek[96] = (byte)(ek[96] | 0x10);
            ek[107] = (byte)(ek[107] | 4);
            ek[120] = (byte)(ek[120] | 8);
        }
        if ((octet & 0x10) != 0) {
            ek[4] = (byte)(ek[4] | 0x20);
            ek[20] = (byte)(ek[20] | 2);
            ek[31] = (byte)(ek[31] | 4);
            ek[37] = (byte)(ek[37] | 0x20);
            ek[47] = (byte)(ek[47] | 1);
            ek[54] = (byte)(ek[54] | 1);
            ek[63] = (byte)(ek[63] | 2);
            ek[68] = (byte)(ek[68] | 1);
            ek[78] = (byte)(ek[78] | 4);
            ek[84] = (byte)(ek[84] | 8);
            ek[101] = (byte)(ek[101] | 0x10);
            ek[108] = (byte)(ek[108] | 4);
            ek[119] = (byte)(ek[119] | 0x10);
            ek[126] = (byte)(ek[126] | 8);
        }
        if ((octet & 8) != 0) {
            ek[5] = (byte)(ek[5] | 4);
            ek[15] = (byte)(ek[15] | 4);
            ek[21] = (byte)(ek[21] | 0x20);
            ek[31] = (byte)(ek[31] | 1);
            ek[38] = (byte)(ek[38] | 1);
            ek[47] = (byte)(ek[47] | 2);
            ek[53] = (byte)(ek[53] | 2);
            ek[68] = (byte)(ek[68] | 8);
            ek[85] = (byte)(ek[85] | 0x10);
            ek[92] = (byte)(ek[92] | 4);
            ek[103] = (byte)(ek[103] | 0x10);
            ek[108] = (byte)(ek[108] | 0x20);
            ek[118] = (byte)(ek[118] | 0x20);
            ek[124] = (byte)(ek[124] | 2);
        }
        if ((octet & 4) != 0) {
            ek[15] = (byte)(ek[15] | 2);
            ek[21] = (byte)(ek[21] | 2);
            ek[39] = (byte)(ek[39] | 8);
            ek[46] = (byte)(ek[46] | 0x10);
            ek[55] = (byte)(ek[55] | 0x20);
            ek[61] = (byte)(ek[61] | 1);
            ek[71] = (byte)(ek[71] | 0x10);
            ek[76] = (byte)(ek[76] | 0x20);
            ek[86] = (byte)(ek[86] | 0x20);
            ek[93] = (byte)(ek[93] | 4);
            ek[102] = (byte)(ek[102] | 2);
            ek[108] = (byte)(ek[108] | 0x10);
            ek[117] = (byte)(ek[117] | 8);
            ek[126] = (byte)(ek[126] | 1);
        }
        if ((octet & 2) != 0) {
            ek[14] = (byte)(ek[14] | 0x10);
            ek[23] = (byte)(ek[23] | 0x20);
            ek[29] = (byte)(ek[29] | 1);
            ek[38] = (byte)(ek[38] | 8);
            ek[52] = (byte)(ek[52] | 2);
            ek[63] = (byte)(ek[63] | 4);
            ek[70] = (byte)(ek[70] | 2);
            ek[76] = (byte)(ek[76] | 0x10);
            ek[85] = (byte)(ek[85] | 8);
            ek[100] = (byte)(ek[100] | 1);
            ek[110] = (byte)(ek[110] | 4);
            ek[116] = (byte)(ek[116] | 8);
            ek[127] = (byte)(ek[127] | 8);
        }
        if (((octet = key[1]) & 0x80) != 0) {
            ek[1] = (byte)(ek[1] | 8);
            ek[8] = (byte)(ek[8] | 0x20);
            ek[17] = (byte)(ek[17] | 1);
            ek[24] = (byte)(ek[24] | 0x10);
            ek[35] = (byte)(ek[35] | 4);
            ek[50] = (byte)(ek[50] | 1);
            ek[57] = (byte)(ek[57] | 0x10);
            ek[67] = (byte)(ek[67] | 8);
            ek[83] = (byte)(ek[83] | 1);
            ek[88] = (byte)(ek[88] | 1);
            ek[98] = (byte)(ek[98] | 4);
            ek[105] = (byte)(ek[105] | 0x20);
            ek[114] = (byte)(ek[114] | 0x20);
            ek[123] = (byte)(ek[123] | 2);
        }
        if ((octet & 0x40) != 0) {
            ek[0] = (byte)(ek[0] | 1);
            ek[11] = (byte)(ek[11] | 0x10);
            ek[16] = (byte)(ek[16] | 4);
            ek[35] = (byte)(ek[35] | 2);
            ek[40] = (byte)(ek[40] | 0x20);
            ek[49] = (byte)(ek[49] | 1);
            ek[56] = (byte)(ek[56] | 0x10);
            ek[65] = (byte)(ek[65] | 2);
            ek[74] = (byte)(ek[74] | 0x10);
            ek[80] = (byte)(ek[80] | 8);
            ek[99] = (byte)(ek[99] | 8);
            ek[115] = (byte)(ek[115] | 1);
            ek[121] = (byte)(ek[121] | 4);
        }
        if ((octet & 0x20) != 0) {
            ek[9] = (byte)(ek[9] | 0x10);
            ek[18] = (byte)(ek[18] | 2);
            ek[24] = (byte)(ek[24] | 2);
            ek[33] = (byte)(ek[33] | 4);
            ek[43] = (byte)(ek[43] | 0x10);
            ek[48] = (byte)(ek[48] | 4);
            ek[66] = (byte)(ek[66] | 0x20);
            ek[73] = (byte)(ek[73] | 8);
            ek[82] = (byte)(ek[82] | 8);
            ek[91] = (byte)(ek[91] | 0x20);
            ek[97] = (byte)(ek[97] | 2);
            ek[106] = (byte)(ek[106] | 0x10);
            ek[112] = (byte)(ek[112] | 8);
            ek[122] = (byte)(ek[122] | 1);
        }
        if ((octet & 0x10) != 0) {
            ek[14] = (byte)(ek[14] | 0x20);
            ek[21] = (byte)(ek[21] | 4);
            ek[30] = (byte)(ek[30] | 2);
            ek[36] = (byte)(ek[36] | 0x10);
            ek[45] = (byte)(ek[45] | 8);
            ek[60] = (byte)(ek[60] | 1);
            ek[69] = (byte)(ek[69] | 2);
            ek[87] = (byte)(ek[87] | 8);
            ek[94] = (byte)(ek[94] | 0x10);
            ek[103] = (byte)(ek[103] | 0x20);
            ek[109] = (byte)(ek[109] | 1);
            ek[118] = (byte)(ek[118] | 8);
            ek[124] = (byte)(ek[124] | 0x20);
        }
        if ((octet & 8) != 0) {
            ek[7] = (byte)(ek[7] | 4);
            ek[14] = (byte)(ek[14] | 2);
            ek[20] = (byte)(ek[20] | 0x10);
            ek[29] = (byte)(ek[29] | 8);
            ek[44] = (byte)(ek[44] | 1);
            ek[54] = (byte)(ek[54] | 4);
            ek[60] = (byte)(ek[60] | 8);
            ek[71] = (byte)(ek[71] | 8);
            ek[78] = (byte)(ek[78] | 0x10);
            ek[87] = (byte)(ek[87] | 0x20);
            ek[93] = (byte)(ek[93] | 1);
            ek[102] = (byte)(ek[102] | 8);
            ek[116] = (byte)(ek[116] | 2);
            ek[125] = (byte)(ek[125] | 4);
        }
        if ((octet & 4) != 0) {
            ek[7] = (byte)(ek[7] | 2);
            ek[12] = (byte)(ek[12] | 1);
            ek[22] = (byte)(ek[22] | 4);
            ek[28] = (byte)(ek[28] | 8);
            ek[45] = (byte)(ek[45] | 0x10);
            ek[52] = (byte)(ek[52] | 4);
            ek[63] = (byte)(ek[63] | 0x10);
            ek[70] = (byte)(ek[70] | 8);
            ek[84] = (byte)(ek[84] | 2);
            ek[95] = (byte)(ek[95] | 4);
            ek[101] = (byte)(ek[101] | 0x20);
            ek[111] = (byte)(ek[111] | 1);
            ek[118] = (byte)(ek[118] | 1);
        }
        if ((octet & 2) != 0) {
            ek[6] = (byte)(ek[6] | 0x10);
            ek[13] = (byte)(ek[13] | 0x10);
            ek[20] = (byte)(ek[20] | 4);
            ek[31] = (byte)(ek[31] | 0x10);
            ek[36] = (byte)(ek[36] | 0x20);
            ek[46] = (byte)(ek[46] | 0x20);
            ek[53] = (byte)(ek[53] | 4);
            ek[62] = (byte)(ek[62] | 2);
            ek[69] = (byte)(ek[69] | 0x20);
            ek[79] = (byte)(ek[79] | 1);
            ek[86] = (byte)(ek[86] | 1);
            ek[95] = (byte)(ek[95] | 2);
            ek[101] = (byte)(ek[101] | 2);
            ek[119] = (byte)(ek[119] | 8);
        }
        if (((octet = key[2]) & 0x80) != 0) {
            ek[0] = (byte)(ek[0] | 0x20);
            ek[10] = (byte)(ek[10] | 8);
            ek[19] = (byte)(ek[19] | 0x20);
            ek[25] = (byte)(ek[25] | 2);
            ek[34] = (byte)(ek[34] | 0x10);
            ek[40] = (byte)(ek[40] | 8);
            ek[59] = (byte)(ek[59] | 8);
            ek[66] = (byte)(ek[66] | 2);
            ek[72] = (byte)(ek[72] | 2);
            ek[81] = (byte)(ek[81] | 4);
            ek[91] = (byte)(ek[91] | 0x10);
            ek[96] = (byte)(ek[96] | 4);
            ek[115] = (byte)(ek[115] | 2);
            ek[121] = (byte)(ek[121] | 8);
        }
        if ((octet & 0x40) != 0) {
            ek[3] = (byte)(ek[3] | 0x10);
            ek[10] = (byte)(ek[10] | 4);
            ek[17] = (byte)(ek[17] | 0x20);
            ek[26] = (byte)(ek[26] | 0x20);
            ek[33] = (byte)(ek[33] | 8);
            ek[42] = (byte)(ek[42] | 8);
            ek[51] = (byte)(ek[51] | 0x20);
            ek[57] = (byte)(ek[57] | 2);
            ek[67] = (byte)(ek[67] | 4);
            ek[82] = (byte)(ek[82] | 1);
            ek[89] = (byte)(ek[89] | 0x10);
            ek[98] = (byte)(ek[98] | 2);
            ek[104] = (byte)(ek[104] | 2);
            ek[113] = (byte)(ek[113] | 4);
            ek[120] = (byte)(ek[120] | 1);
        }
        if ((octet & 0x20) != 0) {
            ek[1] = (byte)(ek[1] | 0x10);
            ek[11] = (byte)(ek[11] | 8);
            ek[27] = (byte)(ek[27] | 1);
            ek[32] = (byte)(ek[32] | 1);
            ek[42] = (byte)(ek[42] | 4);
            ek[49] = (byte)(ek[49] | 0x20);
            ek[58] = (byte)(ek[58] | 0x20);
            ek[67] = (byte)(ek[67] | 2);
            ek[72] = (byte)(ek[72] | 0x20);
            ek[81] = (byte)(ek[81] | 1);
            ek[88] = (byte)(ek[88] | 0x10);
            ek[99] = (byte)(ek[99] | 4);
            ek[114] = (byte)(ek[114] | 1);
        }
        if ((octet & 0x10) != 0) {
            ek[6] = (byte)(ek[6] | 0x20);
            ek[12] = (byte)(ek[12] | 2);
            ek[23] = (byte)(ek[23] | 4);
            ek[29] = (byte)(ek[29] | 0x20);
            ek[39] = (byte)(ek[39] | 1);
            ek[46] = (byte)(ek[46] | 1);
            ek[55] = (byte)(ek[55] | 2);
            ek[61] = (byte)(ek[61] | 2);
            ek[70] = (byte)(ek[70] | 4);
            ek[76] = (byte)(ek[76] | 8);
            ek[93] = (byte)(ek[93] | 0x10);
            ek[100] = (byte)(ek[100] | 4);
            ek[111] = (byte)(ek[111] | 0x10);
            ek[116] = (byte)(ek[116] | 0x20);
        }
        if ((octet & 8) != 0) {
            ek[6] = (byte)(ek[6] | 2);
            ek[13] = (byte)(ek[13] | 0x20);
            ek[23] = (byte)(ek[23] | 1);
            ek[30] = (byte)(ek[30] | 1);
            ek[39] = (byte)(ek[39] | 2);
            ek[45] = (byte)(ek[45] | 2);
            ek[63] = (byte)(ek[63] | 8);
            ek[77] = (byte)(ek[77] | 0x10);
            ek[84] = (byte)(ek[84] | 4);
            ek[95] = (byte)(ek[95] | 0x10);
            ek[100] = (byte)(ek[100] | 0x20);
            ek[110] = (byte)(ek[110] | 0x20);
            ek[117] = (byte)(ek[117] | 4);
            ek[127] = (byte)(ek[127] | 4);
        }
        if ((octet & 4) != 0) {
            ek[4] = (byte)(ek[4] | 1);
            ek[13] = (byte)(ek[13] | 2);
            ek[31] = (byte)(ek[31] | 8);
            ek[38] = (byte)(ek[38] | 0x10);
            ek[47] = (byte)(ek[47] | 0x20);
            ek[53] = (byte)(ek[53] | 1);
            ek[62] = (byte)(ek[62] | 8);
            ek[68] = (byte)(ek[68] | 0x20);
            ek[78] = (byte)(ek[78] | 0x20);
            ek[85] = (byte)(ek[85] | 4);
            ek[94] = (byte)(ek[94] | 2);
            ek[100] = (byte)(ek[100] | 0x10);
            ek[109] = (byte)(ek[109] | 8);
            ek[127] = (byte)(ek[127] | 2);
        }
        if ((octet & 2) != 0) {
            ek[5] = (byte)(ek[5] | 0x10);
            ek[15] = (byte)(ek[15] | 0x20);
            ek[21] = (byte)(ek[21] | 1);
            ek[30] = (byte)(ek[30] | 8);
            ek[44] = (byte)(ek[44] | 2);
            ek[55] = (byte)(ek[55] | 4);
            ek[61] = (byte)(ek[61] | 0x20);
            ek[68] = (byte)(ek[68] | 0x10);
            ek[77] = (byte)(ek[77] | 8);
            ek[92] = (byte)(ek[92] | 1);
            ek[102] = (byte)(ek[102] | 4);
            ek[108] = (byte)(ek[108] | 8);
            ek[126] = (byte)(ek[126] | 0x10);
        }
        if (((octet = key[3]) & 0x80) != 0) {
            ek[2] = (byte)(ek[2] | 8);
            ek[9] = (byte)(ek[9] | 1);
            ek[16] = (byte)(ek[16] | 0x10);
            ek[27] = (byte)(ek[27] | 4);
            ek[42] = (byte)(ek[42] | 1);
            ek[49] = (byte)(ek[49] | 0x10);
            ek[58] = (byte)(ek[58] | 2);
            ek[75] = (byte)(ek[75] | 1);
            ek[80] = (byte)(ek[80] | 1);
            ek[90] = (byte)(ek[90] | 4);
            ek[97] = (byte)(ek[97] | 0x20);
            ek[106] = (byte)(ek[106] | 0x20);
            ek[113] = (byte)(ek[113] | 8);
            ek[120] = (byte)(ek[120] | 0x20);
        }
        if ((octet & 0x40) != 0) {
            ek[2] = (byte)(ek[2] | 4);
            ek[8] = (byte)(ek[8] | 4);
            ek[27] = (byte)(ek[27] | 2);
            ek[32] = (byte)(ek[32] | 0x20);
            ek[41] = (byte)(ek[41] | 1);
            ek[48] = (byte)(ek[48] | 0x10);
            ek[59] = (byte)(ek[59] | 4);
            ek[66] = (byte)(ek[66] | 0x10);
            ek[72] = (byte)(ek[72] | 8);
            ek[91] = (byte)(ek[91] | 8);
            ek[107] = (byte)(ek[107] | 1);
            ek[112] = (byte)(ek[112] | 1);
            ek[123] = (byte)(ek[123] | 0x10);
        }
        if ((octet & 0x20) != 0) {
            ek[3] = (byte)(ek[3] | 8);
            ek[10] = (byte)(ek[10] | 2);
            ek[16] = (byte)(ek[16] | 2);
            ek[25] = (byte)(ek[25] | 4);
            ek[35] = (byte)(ek[35] | 0x10);
            ek[40] = (byte)(ek[40] | 4);
            ek[59] = (byte)(ek[59] | 2);
            ek[65] = (byte)(ek[65] | 8);
            ek[74] = (byte)(ek[74] | 8);
            ek[83] = (byte)(ek[83] | 0x20);
            ek[89] = (byte)(ek[89] | 2);
            ek[98] = (byte)(ek[98] | 0x10);
            ek[104] = (byte)(ek[104] | 8);
            ek[121] = (byte)(ek[121] | 0x10);
        }
        if ((octet & 0x10) != 0) {
            ek[4] = (byte)(ek[4] | 2);
            ek[13] = (byte)(ek[13] | 4);
            ek[22] = (byte)(ek[22] | 2);
            ek[28] = (byte)(ek[28] | 0x10);
            ek[37] = (byte)(ek[37] | 8);
            ek[52] = (byte)(ek[52] | 1);
            ek[62] = (byte)(ek[62] | 4);
            ek[79] = (byte)(ek[79] | 8);
            ek[86] = (byte)(ek[86] | 0x10);
            ek[95] = (byte)(ek[95] | 0x20);
            ek[101] = (byte)(ek[101] | 1);
            ek[110] = (byte)(ek[110] | 8);
            ek[126] = (byte)(ek[126] | 0x20);
        }
        if ((octet & 8) != 0) {
            ek[5] = (byte)(ek[5] | 0x20);
            ek[12] = (byte)(ek[12] | 0x10);
            ek[21] = (byte)(ek[21] | 8);
            ek[36] = (byte)(ek[36] | 1);
            ek[46] = (byte)(ek[46] | 4);
            ek[52] = (byte)(ek[52] | 8);
            ek[70] = (byte)(ek[70] | 0x10);
            ek[79] = (byte)(ek[79] | 0x20);
            ek[85] = (byte)(ek[85] | 1);
            ek[94] = (byte)(ek[94] | 8);
            ek[108] = (byte)(ek[108] | 2);
            ek[119] = (byte)(ek[119] | 4);
            ek[126] = (byte)(ek[126] | 2);
        }
        if ((octet & 4) != 0) {
            ek[5] = (byte)(ek[5] | 2);
            ek[14] = (byte)(ek[14] | 4);
            ek[20] = (byte)(ek[20] | 8);
            ek[37] = (byte)(ek[37] | 0x10);
            ek[44] = (byte)(ek[44] | 4);
            ek[55] = (byte)(ek[55] | 0x10);
            ek[60] = (byte)(ek[60] | 0x20);
            ek[76] = (byte)(ek[76] | 2);
            ek[87] = (byte)(ek[87] | 4);
            ek[93] = (byte)(ek[93] | 0x20);
            ek[103] = (byte)(ek[103] | 1);
            ek[110] = (byte)(ek[110] | 1);
            ek[119] = (byte)(ek[119] | 2);
            ek[124] = (byte)(ek[124] | 1);
        }
        if ((octet & 2) != 0) {
            ek[7] = (byte)(ek[7] | 0x20);
            ek[12] = (byte)(ek[12] | 4);
            ek[23] = (byte)(ek[23] | 0x10);
            ek[28] = (byte)(ek[28] | 0x20);
            ek[38] = (byte)(ek[38] | 0x20);
            ek[45] = (byte)(ek[45] | 4);
            ek[54] = (byte)(ek[54] | 2);
            ek[60] = (byte)(ek[60] | 0x10);
            ek[71] = (byte)(ek[71] | 1);
            ek[78] = (byte)(ek[78] | 1);
            ek[87] = (byte)(ek[87] | 2);
            ek[93] = (byte)(ek[93] | 2);
            ek[111] = (byte)(ek[111] | 8);
            ek[118] = (byte)(ek[118] | 0x10);
            ek[125] = (byte)(ek[125] | 0x10);
        }
        if (((octet = key[4]) & 0x80) != 0) {
            ek[1] = (byte)(ek[1] | 1);
            ek[11] = (byte)(ek[11] | 0x20);
            ek[17] = (byte)(ek[17] | 2);
            ek[26] = (byte)(ek[26] | 0x10);
            ek[32] = (byte)(ek[32] | 8);
            ek[51] = (byte)(ek[51] | 8);
            ek[64] = (byte)(ek[64] | 2);
            ek[73] = (byte)(ek[73] | 4);
            ek[83] = (byte)(ek[83] | 0x10);
            ek[88] = (byte)(ek[88] | 4);
            ek[107] = (byte)(ek[107] | 2);
            ek[112] = (byte)(ek[112] | 0x20);
            ek[122] = (byte)(ek[122] | 8);
        }
        if ((octet & 0x40) != 0) {
            ek[0] = (byte)(ek[0] | 4);
            ek[9] = (byte)(ek[9] | 0x20);
            ek[18] = (byte)(ek[18] | 0x20);
            ek[25] = (byte)(ek[25] | 8);
            ek[34] = (byte)(ek[34] | 8);
            ek[43] = (byte)(ek[43] | 0x20);
            ek[49] = (byte)(ek[49] | 2);
            ek[58] = (byte)(ek[58] | 0x10);
            ek[74] = (byte)(ek[74] | 1);
            ek[81] = (byte)(ek[81] | 0x10);
            ek[90] = (byte)(ek[90] | 2);
            ek[96] = (byte)(ek[96] | 2);
            ek[105] = (byte)(ek[105] | 4);
            ek[115] = (byte)(ek[115] | 0x10);
            ek[122] = (byte)(ek[122] | 4);
        }
        if ((octet & 0x20) != 0) {
            ek[2] = (byte)(ek[2] | 2);
            ek[19] = (byte)(ek[19] | 1);
            ek[24] = (byte)(ek[24] | 1);
            ek[34] = (byte)(ek[34] | 4);
            ek[41] = (byte)(ek[41] | 0x20);
            ek[50] = (byte)(ek[50] | 0x20);
            ek[57] = (byte)(ek[57] | 8);
            ek[64] = (byte)(ek[64] | 0x20);
            ek[73] = (byte)(ek[73] | 1);
            ek[80] = (byte)(ek[80] | 0x10);
            ek[91] = (byte)(ek[91] | 4);
            ek[106] = (byte)(ek[106] | 1);
            ek[113] = (byte)(ek[113] | 0x10);
            ek[123] = (byte)(ek[123] | 8);
        }
        if ((octet & 0x10) != 0) {
            ek[3] = (byte)(ek[3] | 4);
            ek[10] = (byte)(ek[10] | 0x10);
            ek[16] = (byte)(ek[16] | 8);
            ek[35] = (byte)(ek[35] | 8);
            ek[51] = (byte)(ek[51] | 1);
            ek[56] = (byte)(ek[56] | 1);
            ek[67] = (byte)(ek[67] | 0x10);
            ek[72] = (byte)(ek[72] | 4);
            ek[91] = (byte)(ek[91] | 2);
            ek[96] = (byte)(ek[96] | 0x20);
            ek[105] = (byte)(ek[105] | 1);
            ek[112] = (byte)(ek[112] | 0x10);
            ek[121] = (byte)(ek[121] | 2);
        }
        if ((octet & 8) != 0) {
            ek[4] = (byte)(ek[4] | 0x10);
            ek[15] = (byte)(ek[15] | 1);
            ek[22] = (byte)(ek[22] | 1);
            ek[31] = (byte)(ek[31] | 2);
            ek[37] = (byte)(ek[37] | 2);
            ek[55] = (byte)(ek[55] | 8);
            ek[62] = (byte)(ek[62] | 0x10);
            ek[69] = (byte)(ek[69] | 0x10);
            ek[76] = (byte)(ek[76] | 4);
            ek[87] = (byte)(ek[87] | 0x10);
            ek[92] = (byte)(ek[92] | 0x20);
            ek[102] = (byte)(ek[102] | 0x20);
            ek[109] = (byte)(ek[109] | 4);
            ek[118] = (byte)(ek[118] | 2);
            ek[125] = (byte)(ek[125] | 0x20);
        }
        if ((octet & 4) != 0) {
            ek[6] = (byte)(ek[6] | 4);
            ek[23] = (byte)(ek[23] | 8);
            ek[30] = (byte)(ek[30] | 0x10);
            ek[39] = (byte)(ek[39] | 0x20);
            ek[45] = (byte)(ek[45] | 1);
            ek[54] = (byte)(ek[54] | 8);
            ek[70] = (byte)(ek[70] | 0x20);
            ek[77] = (byte)(ek[77] | 4);
            ek[86] = (byte)(ek[86] | 2);
            ek[92] = (byte)(ek[92] | 0x10);
            ek[101] = (byte)(ek[101] | 8);
            ek[116] = (byte)(ek[116] | 1);
            ek[125] = (byte)(ek[125] | 2);
        }
        if ((octet & 2) != 0) {
            ek[4] = (byte)(ek[4] | 4);
            ek[13] = (byte)(ek[13] | 1);
            ek[22] = (byte)(ek[22] | 8);
            ek[36] = (byte)(ek[36] | 2);
            ek[47] = (byte)(ek[47] | 4);
            ek[53] = (byte)(ek[53] | 0x20);
            ek[63] = (byte)(ek[63] | 1);
            ek[69] = (byte)(ek[69] | 8);
            ek[84] = (byte)(ek[84] | 1);
            ek[94] = (byte)(ek[94] | 4);
            ek[100] = (byte)(ek[100] | 8);
            ek[117] = (byte)(ek[117] | 0x10);
            ek[127] = (byte)(ek[127] | 0x20);
        }
        if (((octet = key[5]) & 0x80) != 0) {
            ek[3] = (byte)(ek[3] | 0x20);
            ek[8] = (byte)(ek[8] | 0x10);
            ek[19] = (byte)(ek[19] | 4);
            ek[34] = (byte)(ek[34] | 1);
            ek[41] = (byte)(ek[41] | 0x10);
            ek[50] = (byte)(ek[50] | 2);
            ek[56] = (byte)(ek[56] | 2);
            ek[67] = (byte)(ek[67] | 1);
            ek[72] = (byte)(ek[72] | 1);
            ek[82] = (byte)(ek[82] | 4);
            ek[89] = (byte)(ek[89] | 0x20);
            ek[98] = (byte)(ek[98] | 0x20);
            ek[105] = (byte)(ek[105] | 8);
            ek[114] = (byte)(ek[114] | 8);
            ek[121] = (byte)(ek[121] | 1);
        }
        if ((octet & 0x40) != 0) {
            ek[1] = (byte)(ek[1] | 0x20);
            ek[19] = (byte)(ek[19] | 2);
            ek[24] = (byte)(ek[24] | 0x20);
            ek[33] = (byte)(ek[33] | 1);
            ek[40] = (byte)(ek[40] | 0x10);
            ek[51] = (byte)(ek[51] | 4);
            ek[64] = (byte)(ek[64] | 8);
            ek[83] = (byte)(ek[83] | 8);
            ek[99] = (byte)(ek[99] | 1);
            ek[104] = (byte)(ek[104] | 1);
            ek[114] = (byte)(ek[114] | 4);
            ek[120] = (byte)(ek[120] | 4);
        }
        if ((octet & 0x20) != 0) {
            ek[8] = (byte)(ek[8] | 2);
            ek[17] = (byte)(ek[17] | 4);
            ek[27] = (byte)(ek[27] | 0x10);
            ek[32] = (byte)(ek[32] | 4);
            ek[51] = (byte)(ek[51] | 2);
            ek[56] = (byte)(ek[56] | 0x20);
            ek[66] = (byte)(ek[66] | 8);
            ek[75] = (byte)(ek[75] | 0x20);
            ek[81] = (byte)(ek[81] | 2);
            ek[90] = (byte)(ek[90] | 0x10);
            ek[96] = (byte)(ek[96] | 8);
            ek[115] = (byte)(ek[115] | 8);
            ek[122] = (byte)(ek[122] | 2);
        }
        if ((octet & 0x10) != 0) {
            ek[2] = (byte)(ek[2] | 0x10);
            ek[18] = (byte)(ek[18] | 1);
            ek[25] = (byte)(ek[25] | 0x10);
            ek[34] = (byte)(ek[34] | 2);
            ek[40] = (byte)(ek[40] | 2);
            ek[49] = (byte)(ek[49] | 4);
            ek[59] = (byte)(ek[59] | 0x10);
            ek[66] = (byte)(ek[66] | 4);
            ek[73] = (byte)(ek[73] | 0x20);
            ek[82] = (byte)(ek[82] | 0x20);
            ek[89] = (byte)(ek[89] | 8);
            ek[98] = (byte)(ek[98] | 8);
            ek[107] = (byte)(ek[107] | 0x20);
            ek[113] = (byte)(ek[113] | 2);
            ek[123] = (byte)(ek[123] | 4);
        }
        if ((octet & 8) != 0) {
            ek[7] = (byte)(ek[7] | 1);
            ek[13] = (byte)(ek[13] | 8);
            ek[28] = (byte)(ek[28] | 1);
            ek[38] = (byte)(ek[38] | 4);
            ek[44] = (byte)(ek[44] | 8);
            ek[61] = (byte)(ek[61] | 0x10);
            ek[71] = (byte)(ek[71] | 0x20);
            ek[77] = (byte)(ek[77] | 1);
            ek[86] = (byte)(ek[86] | 8);
            ek[100] = (byte)(ek[100] | 2);
            ek[111] = (byte)(ek[111] | 4);
            ek[117] = (byte)(ek[117] | 0x20);
            ek[124] = (byte)(ek[124] | 0x10);
        }
        if ((octet & 4) != 0) {
            ek[12] = (byte)(ek[12] | 8);
            ek[29] = (byte)(ek[29] | 0x10);
            ek[36] = (byte)(ek[36] | 4);
            ek[47] = (byte)(ek[47] | 0x10);
            ek[52] = (byte)(ek[52] | 0x20);
            ek[62] = (byte)(ek[62] | 0x20);
            ek[68] = (byte)(ek[68] | 2);
            ek[79] = (byte)(ek[79] | 4);
            ek[85] = (byte)(ek[85] | 0x20);
            ek[95] = (byte)(ek[95] | 1);
            ek[102] = (byte)(ek[102] | 1);
            ek[111] = (byte)(ek[111] | 2);
            ek[117] = (byte)(ek[117] | 2);
            ek[126] = (byte)(ek[126] | 4);
        }
        if ((octet & 2) != 0) {
            ek[5] = (byte)(ek[5] | 1);
            ek[15] = (byte)(ek[15] | 0x10);
            ek[20] = (byte)(ek[20] | 0x20);
            ek[30] = (byte)(ek[30] | 0x20);
            ek[37] = (byte)(ek[37] | 4);
            ek[46] = (byte)(ek[46] | 2);
            ek[52] = (byte)(ek[52] | 0x10);
            ek[61] = (byte)(ek[61] | 8);
            ek[70] = (byte)(ek[70] | 1);
            ek[79] = (byte)(ek[79] | 2);
            ek[85] = (byte)(ek[85] | 2);
            ek[103] = (byte)(ek[103] | 8);
            ek[110] = (byte)(ek[110] | 0x10);
            ek[119] = (byte)(ek[119] | 0x20);
            ek[124] = (byte)(ek[124] | 4);
        }
        if (((octet = key[6]) & 0x80) != 0) {
            ek[0] = (byte)(ek[0] | 0x10);
            ek[9] = (byte)(ek[9] | 2);
            ek[18] = (byte)(ek[18] | 0x10);
            ek[24] = (byte)(ek[24] | 8);
            ek[43] = (byte)(ek[43] | 8);
            ek[59] = (byte)(ek[59] | 1);
            ek[65] = (byte)(ek[65] | 4);
            ek[75] = (byte)(ek[75] | 0x10);
            ek[80] = (byte)(ek[80] | 4);
            ek[99] = (byte)(ek[99] | 2);
            ek[104] = (byte)(ek[104] | 0x20);
            ek[113] = (byte)(ek[113] | 1);
            ek[123] = (byte)(ek[123] | 0x20);
        }
        if ((octet & 0x40) != 0) {
            ek[10] = (byte)(ek[10] | 0x20);
            ek[17] = (byte)(ek[17] | 8);
            ek[26] = (byte)(ek[26] | 8);
            ek[35] = (byte)(ek[35] | 0x20);
            ek[41] = (byte)(ek[41] | 2);
            ek[50] = (byte)(ek[50] | 0x10);
            ek[56] = (byte)(ek[56] | 8);
            ek[66] = (byte)(ek[66] | 1);
            ek[73] = (byte)(ek[73] | 0x10);
            ek[82] = (byte)(ek[82] | 2);
            ek[88] = (byte)(ek[88] | 2);
            ek[97] = (byte)(ek[97] | 4);
            ek[107] = (byte)(ek[107] | 0x10);
            ek[112] = (byte)(ek[112] | 4);
            ek[121] = (byte)(ek[121] | 0x20);
        }
        if ((octet & 0x20) != 0) {
            ek[0] = (byte)(ek[0] | 2);
            ek[11] = (byte)(ek[11] | 1);
            ek[16] = (byte)(ek[16] | 1);
            ek[26] = (byte)(ek[26] | 4);
            ek[33] = (byte)(ek[33] | 0x20);
            ek[42] = (byte)(ek[42] | 0x20);
            ek[49] = (byte)(ek[49] | 8);
            ek[58] = (byte)(ek[58] | 8);
            ek[65] = (byte)(ek[65] | 1);
            ek[72] = (byte)(ek[72] | 0x10);
            ek[83] = (byte)(ek[83] | 4);
            ek[98] = (byte)(ek[98] | 1);
            ek[105] = (byte)(ek[105] | 0x10);
            ek[114] = (byte)(ek[114] | 2);
        }
        if ((octet & 0x10) != 0) {
            ek[8] = (byte)(ek[8] | 8);
            ek[27] = (byte)(ek[27] | 8);
            ek[43] = (byte)(ek[43] | 1);
            ek[48] = (byte)(ek[48] | 1);
            ek[58] = (byte)(ek[58] | 4);
            ek[64] = (byte)(ek[64] | 4);
            ek[83] = (byte)(ek[83] | 2);
            ek[88] = (byte)(ek[88] | 0x20);
            ek[97] = (byte)(ek[97] | 1);
            ek[104] = (byte)(ek[104] | 0x10);
            ek[115] = (byte)(ek[115] | 4);
            ek[122] = (byte)(ek[122] | 0x10);
        }
        if ((octet & 8) != 0) {
            ek[5] = (byte)(ek[5] | 8);
            ek[14] = (byte)(ek[14] | 1);
            ek[23] = (byte)(ek[23] | 2);
            ek[29] = (byte)(ek[29] | 2);
            ek[47] = (byte)(ek[47] | 8);
            ek[54] = (byte)(ek[54] | 0x10);
            ek[63] = (byte)(ek[63] | 0x20);
            ek[68] = (byte)(ek[68] | 4);
            ek[79] = (byte)(ek[79] | 0x10);
            ek[84] = (byte)(ek[84] | 0x20);
            ek[94] = (byte)(ek[94] | 0x20);
            ek[101] = (byte)(ek[101] | 4);
            ek[110] = (byte)(ek[110] | 2);
            ek[116] = (byte)(ek[116] | 0x10);
            ek[127] = (byte)(ek[127] | 1);
        }
        if ((octet & 4) != 0) {
            ek[4] = (byte)(ek[4] | 8);
            ek[15] = (byte)(ek[15] | 8);
            ek[22] = (byte)(ek[22] | 0x10);
            ek[31] = (byte)(ek[31] | 0x20);
            ek[37] = (byte)(ek[37] | 1);
            ek[46] = (byte)(ek[46] | 8);
            ek[60] = (byte)(ek[60] | 2);
            ek[69] = (byte)(ek[69] | 4);
            ek[78] = (byte)(ek[78] | 2);
            ek[84] = (byte)(ek[84] | 0x10);
            ek[93] = (byte)(ek[93] | 8);
            ek[108] = (byte)(ek[108] | 1);
            ek[118] = (byte)(ek[118] | 4);
        }
        if ((octet & 2) != 0) {
            ek[7] = (byte)(ek[7] | 0x10);
            ek[14] = (byte)(ek[14] | 8);
            ek[28] = (byte)(ek[28] | 2);
            ek[39] = (byte)(ek[39] | 4);
            ek[45] = (byte)(ek[45] | 0x20);
            ek[55] = (byte)(ek[55] | 1);
            ek[62] = (byte)(ek[62] | 1);
            ek[76] = (byte)(ek[76] | 1);
            ek[86] = (byte)(ek[86] | 4);
            ek[92] = (byte)(ek[92] | 8);
            ek[109] = (byte)(ek[109] | 0x10);
            ek[116] = (byte)(ek[116] | 4);
            ek[125] = (byte)(ek[125] | 1);
        }
        if (((octet = key[7]) & 0x80) != 0) {
            ek[1] = (byte)(ek[1] | 2);
            ek[11] = (byte)(ek[11] | 4);
            ek[26] = (byte)(ek[26] | 1);
            ek[33] = (byte)(ek[33] | 0x10);
            ek[42] = (byte)(ek[42] | 2);
            ek[48] = (byte)(ek[48] | 2);
            ek[57] = (byte)(ek[57] | 4);
            ek[64] = (byte)(ek[64] | 1);
            ek[74] = (byte)(ek[74] | 4);
            ek[81] = (byte)(ek[81] | 0x20);
            ek[90] = (byte)(ek[90] | 0x20);
            ek[97] = (byte)(ek[97] | 8);
            ek[106] = (byte)(ek[106] | 8);
            ek[115] = (byte)(ek[115] | 0x20);
            ek[120] = (byte)(ek[120] | 0x10);
        }
        if ((octet & 0x40) != 0) {
            ek[2] = (byte)(ek[2] | 0x20);
            ek[11] = (byte)(ek[11] | 2);
            ek[16] = (byte)(ek[16] | 0x20);
            ek[25] = (byte)(ek[25] | 1);
            ek[32] = (byte)(ek[32] | 0x10);
            ek[43] = (byte)(ek[43] | 4);
            ek[58] = (byte)(ek[58] | 1);
            ek[75] = (byte)(ek[75] | 8);
            ek[91] = (byte)(ek[91] | 1);
            ek[96] = (byte)(ek[96] | 1);
            ek[106] = (byte)(ek[106] | 4);
            ek[113] = (byte)(ek[113] | 0x20);
        }
        if ((octet & 0x20) != 0) {
            ek[3] = (byte)(ek[3] | 1);
            ek[9] = (byte)(ek[9] | 4);
            ek[19] = (byte)(ek[19] | 0x10);
            ek[24] = (byte)(ek[24] | 4);
            ek[43] = (byte)(ek[43] | 2);
            ek[48] = (byte)(ek[48] | 0x20);
            ek[57] = (byte)(ek[57] | 1);
            ek[67] = (byte)(ek[67] | 0x20);
            ek[73] = (byte)(ek[73] | 2);
            ek[82] = (byte)(ek[82] | 0x10);
            ek[88] = (byte)(ek[88] | 8);
            ek[107] = (byte)(ek[107] | 8);
            ek[120] = (byte)(ek[120] | 2);
        }
        if ((octet & 0x10) != 0) {
            ek[0] = (byte)(ek[0] | 8);
            ek[10] = (byte)(ek[10] | 1);
            ek[17] = (byte)(ek[17] | 0x10);
            ek[26] = (byte)(ek[26] | 2);
            ek[32] = (byte)(ek[32] | 2);
            ek[41] = (byte)(ek[41] | 4);
            ek[51] = (byte)(ek[51] | 0x10);
            ek[56] = (byte)(ek[56] | 4);
            ek[65] = (byte)(ek[65] | 0x20);
            ek[74] = (byte)(ek[74] | 0x20);
            ek[81] = (byte)(ek[81] | 8);
            ek[90] = (byte)(ek[90] | 8);
            ek[99] = (byte)(ek[99] | 0x20);
            ek[105] = (byte)(ek[105] | 2);
            ek[114] = (byte)(ek[114] | 0x10);
        }
        if ((octet & 8) != 0) {
            ek[6] = (byte)(ek[6] | 1);
            ek[20] = (byte)(ek[20] | 1);
            ek[30] = (byte)(ek[30] | 4);
            ek[36] = (byte)(ek[36] | 8);
            ek[53] = (byte)(ek[53] | 0x10);
            ek[60] = (byte)(ek[60] | 4);
            ek[69] = (byte)(ek[69] | 1);
            ek[78] = (byte)(ek[78] | 8);
            ek[92] = (byte)(ek[92] | 2);
            ek[103] = (byte)(ek[103] | 4);
            ek[109] = (byte)(ek[109] | 0x20);
            ek[119] = (byte)(ek[119] | 1);
            ek[125] = (byte)(ek[125] | 8);
        }
        if ((octet & 4) != 0) {
            ek[7] = (byte)(ek[7] | 8);
            ek[21] = (byte)(ek[21] | 0x10);
            ek[28] = (byte)(ek[28] | 4);
            ek[39] = (byte)(ek[39] | 0x10);
            ek[44] = (byte)(ek[44] | 0x20);
            ek[54] = (byte)(ek[54] | 0x20);
            ek[61] = (byte)(ek[61] | 4);
            ek[71] = (byte)(ek[71] | 4);
            ek[77] = (byte)(ek[77] | 0x20);
            ek[87] = (byte)(ek[87] | 1);
            ek[94] = (byte)(ek[94] | 1);
            ek[103] = (byte)(ek[103] | 2);
            ek[109] = (byte)(ek[109] | 2);
            ek[124] = (byte)(ek[124] | 8);
        }
        if ((octet & 2) != 0) {
            ek[6] = (byte)(ek[6] | 8);
            ek[12] = (byte)(ek[12] | 0x20);
            ek[22] = (byte)(ek[22] | 0x20);
            ek[29] = (byte)(ek[29] | 4);
            ek[38] = (byte)(ek[38] | 2);
            ek[44] = (byte)(ek[44] | 0x10);
            ek[53] = (byte)(ek[53] | 8);
            ek[71] = (byte)(ek[71] | 2);
            ek[77] = (byte)(ek[77] | 2);
            ek[95] = (byte)(ek[95] | 8);
            ek[102] = (byte)(ek[102] | 0x10);
            ek[111] = (byte)(ek[111] | 0x20);
            ek[117] = (byte)(ek[117] | 1);
            ek[127] = (byte)(ek[127] | 0x10);
        }
        this.expandedKey = ek;
    }
}

