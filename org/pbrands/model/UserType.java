/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.model;

import java.awt.Color;

public enum UserType {
    STANDARD(1, "User", new Color(0, 92, 152), "ROLE_USER"),
    BETA(2, "Beta", new Color(133, 0, 172), "ROLE_BETA_USER"),
    ADMIN(9999, "Admin", new Color(163, 0, 0), "ROLE_ADMIN");

    private final int permissionLevel;
    private final String displayName;
    private final Color color;
    private final String apiRoleName;

    private UserType(int permissionLevel, String displayName, Color color, String apiRoleName) {
        this.permissionLevel = permissionLevel;
        this.displayName = displayName;
        this.color = color;
        this.apiRoleName = apiRoleName;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Color getColor() {
        return this.color;
    }

    public String getApiRoleName() {
        return this.apiRoleName;
    }

    public static UserType getUserType(String apiRoleName) {
        if (apiRoleName == null) {
            return null;
        }
        String normalized = apiRoleName.trim();
        for (UserType userType : UserType.values()) {
            if (!userType.apiRoleName.equalsIgnoreCase(normalized) && !userType.name().equalsIgnoreCase(normalized) && !userType.displayName.equalsIgnoreCase(normalized)) continue;
            return userType;
        }
        return null;
    }
}

