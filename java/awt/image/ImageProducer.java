/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ImageConsumer;

public interface ImageProducer {
    public void addConsumer(ImageConsumer var1);

    public boolean isConsumer(ImageConsumer var1);

    public void removeConsumer(ImageConsumer var1);

    public void startProduction(ImageConsumer var1);

    public void requestTopDownLeftRightResend(ImageConsumer var1);
}

