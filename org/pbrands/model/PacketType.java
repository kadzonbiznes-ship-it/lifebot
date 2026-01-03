/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.model;

import lombok.Generated;

public enum PacketType {
    AUTH(0),
    PRODUCT(1),
    PING(2),
    HARDWARE_INFO(3),
    DLL(4),
    DLL_KEY(5),
    DLL_UBER(6),
    DLL_UBER_KEY(7),
    LOGIN(8),
    FETCH_PRODUCTS(9),
    LOADER_DLL(10),
    DOWNLOAD_PRODUCT(11),
    DOWNLOAD_INTERCEPTION(12),
    RESET_HWID(13),
    MAPPER(20),
    DRIVER(21),
    SPOOF_START(22),
    IMAGE_RECOGNITION(30),
    RECOGNITION_RESULT(31),
    RECOGNITION_INFO(32),
    DIG_LEVEL(33),
    COAL_PRICE(34),
    CHAT_RECOGNITION(35),
    OCR_DIGIT(36),
    SANTORI_RECOGNITION_INFO(40),
    ADD_MARKER(50),
    REMOVE_MARKER(51),
    MARKER_INFO(52),
    MAP_SELECTION(53),
    MARKER_TYPE(54),
    MAP_LIST(55),
    DECOMPILE(60),
    CLIENT_LOG(70),
    DOWNLOAD_NATIVE(71),
    DOWNLOAD_ENCRYPTED_PRODUCT(72),
    ENCRYPTED_PRODUCT_KEY(73),
    DOWNLOAD_MAP_TILE(80),
    MAP_TILE_DATA(81);

    private final byte opcode;

    private PacketType(int opcode) {
        this.opcode = (byte)opcode;
    }

    public static PacketType fromOpcode(int opcode) {
        for (PacketType type : PacketType.values()) {
            if (type.opcode != opcode) continue;
            return type;
        }
        return null;
    }

    @Generated
    public byte getOpcode() {
        return this.opcode;
    }
}

