/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class FileWriter
extends OutputStreamWriter {
    public FileWriter(String fileName) throws IOException {
        super(new FileOutputStream(fileName));
    }

    public FileWriter(String fileName, boolean append) throws IOException {
        super(new FileOutputStream(fileName, append));
    }

    public FileWriter(File file) throws IOException {
        super(new FileOutputStream(file));
    }

    public FileWriter(File file, boolean append) throws IOException {
        super(new FileOutputStream(file, append));
    }

    public FileWriter(FileDescriptor fd) {
        super(new FileOutputStream(fd));
    }

    public FileWriter(String fileName, Charset charset) throws IOException {
        super((OutputStream)new FileOutputStream(fileName), charset);
    }

    public FileWriter(String fileName, Charset charset, boolean append) throws IOException {
        super((OutputStream)new FileOutputStream(fileName, append), charset);
    }

    public FileWriter(File file, Charset charset) throws IOException {
        super((OutputStream)new FileOutputStream(file), charset);
    }

    public FileWriter(File file, Charset charset, boolean append) throws IOException {
        super((OutputStream)new FileOutputStream(file, append), charset);
    }
}

