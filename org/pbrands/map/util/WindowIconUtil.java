/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.map.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.Struct;

public class WindowIconUtil {
    private static final int[] ICON_SIZES = new int[]{16, 32, 48, 64, 128, 256};

    public static void setWindowIcon(long window, String path) {
        ArrayList<GLFWImage> icons = new ArrayList<GLFWImage>();
        try {
            InputStream is = WindowIconUtil.class.getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new RuntimeException("No ImageReaders found for " + path);
            }
            ImageReader reader = readers.next();
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            BufferedImage originalImage = reader.read(0, param);
            reader.dispose();
            iis.close();
            BufferedImage convertedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), 2);
            Graphics2D g = convertedImage.createGraphics();
            g.drawImage((Image)originalImage, 0, 0, null);
            g.dispose();
            List<BufferedImage> scaledImages = WindowIconUtil.generateScaledImages(convertedImage);
            for (BufferedImage image : scaledImages) {
                ByteBuffer buffer = WindowIconUtil.convertImageToByteBuffer(image);
                GLFWImage glfwImage = GLFWImage.malloc();
                glfwImage.width(image.getWidth());
                glfwImage.height(image.getHeight());
                glfwImage.pixels(buffer);
                icons.add(glfwImage);
            }
            GLFWImage.Buffer iconsBuffer = GLFWImage.malloc(icons.size());
            icons.forEach(iconsBuffer::put);
            iconsBuffer.flip();
            GLFW.glfwSetWindowIcon(window, iconsBuffer);
            icons.forEach(Struct::free);
            iconsBuffer.free();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to set window icon: " + path, e);
        }
    }

    private static List<BufferedImage> generateScaledImages(BufferedImage original) {
        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        images.add(original);
        for (int size : ICON_SIZES) {
            if (original.getWidth() == size && original.getHeight() == size) continue;
            images.add(WindowIconUtil.scaleImage(original, size, size));
        }
        return images;
    }

    private static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        Image scaled = original.getScaledInstance(width, height, 4);
        BufferedImage buffered = new BufferedImage(width, height, 2);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return buffered;
    }

    private static ByteBuffer convertImageToByteBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int pixel : pixels) {
            buffer.put((byte)(pixel >> 16 & 0xFF));
            buffer.put((byte)(pixel >> 8 & 0xFF));
            buffer.put((byte)(pixel & 0xFF));
            buffer.put((byte)(pixel >> 24 & 0xFF));
        }
        buffer.flip();
        return buffer;
    }
}

