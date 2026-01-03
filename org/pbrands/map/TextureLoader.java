/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.map;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.pbrands.Startup;

public class TextureLoader {
    public static int loadTexture(String path) {
        byte[] bytes = TextureLoader.loadFromResources(path);
        return TextureLoader.loadTextureFromBytes(bytes);
    }

    public static int loadTextureFromFile(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return TextureLoader.loadTextureFromBytes(bytes);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load texture from file: " + String.valueOf(filePath), e);
        }
    }

    public static int loadTextureFromBytes(byte[] bytes) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(false);
            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, x, y, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason());
            }
            int w = x.get(0);
            int h = y.get(0);
            int texID = GL11.glGenTextures();
            GL11.glBindTexture(3553, texID);
            GL11.glTexImage2D(3553, 0, 6408, w, h, 0, 6408, 5121, image);
            GL30.glGenerateMipmap(3553);
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
            GL11.glTexParameteri(3553, 10241, 9987);
            GL11.glTexParameteri(3553, 10240, 9729);
            if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                float maxAniso = GL11.glGetFloat(34047);
                GL11.glTexParameteri(3553, 34046, (int)maxAniso);
            }
            STBImage.stbi_image_free(image);
            int n = texID;
            return n;
        }
    }

    public static byte[] loadFromResources(String name) {
        byte[] byArray;
        block9: {
            InputStream is = Startup.class.getResourceAsStream(name);
            try {
                if (is == null) {
                    throw new RuntimeException("Resource not found: " + name);
                }
                byArray = is.readAllBytes();
                if (is == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            is.close();
        }
        return byArray;
    }
}

