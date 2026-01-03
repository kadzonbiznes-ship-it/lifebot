/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileReader
extends InputStreamReader {
    public FileReader(String fileName) throws FileNotFoundException {
        super(new FileInputStream(fileName));
    }

    public FileReader(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
    }

    public FileReader(FileDescriptor fd) {
        super(new FileInputStream(fd));
    }

    public FileReader(String fileName, Charset charset) throws IOException {
        super((InputStream)new FileInputStream(fileName), charset);
    }

    public FileReader(File file, Charset charset) throws IOException {
        super((InputStream)new FileInputStream(file), charset);
    }
}

