/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.io.File;

public class CoreConstants {
    public static final File LIFEBOT_FOLDER = new File(System.getenv("LOCALAPPDATA"), "LifeBot");
    public static final File DATA_FOLDER = new File(LIFEBOT_FOLDER, "data");
    public static final File NATIVE_FOLDER = new File(LIFEBOT_FOLDER, "native");
    public static final File LOGS_FOLDER = new File(LIFEBOT_FOLDER, "logs");
    public static final File SCREENSHOTS_FOLDER = new File(LIFEBOT_FOLDER, "screenshots");
}

