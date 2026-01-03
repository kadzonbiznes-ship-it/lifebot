/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import jdk.internal.access.JavaIOPrintWriterAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.InternalLock;

public class PrintWriter
extends Writer {
    protected Writer out;
    private final boolean autoFlush;
    private boolean trouble;
    private Formatter formatter;
    private PrintStream psOut;

    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException unused) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    public PrintWriter(Writer out) {
        this(out, false);
    }

    public PrintWriter(Writer out, boolean autoFlush) {
        super(out);
        this.trouble = false;
        this.psOut = null;
        this.out = out;
        this.autoFlush = autoFlush;
    }

    public PrintWriter(OutputStream out) {
        this(out, false);
    }

    public PrintWriter(OutputStream out, boolean autoFlush) {
        Charset charset;
        if (out instanceof PrintStream) {
            PrintStream ps = (PrintStream)out;
            charset = ps.charset();
        } else {
            charset = Charset.defaultCharset();
        }
        this(out, autoFlush, charset);
    }

    public PrintWriter(OutputStream out, boolean autoFlush, Charset charset) {
        this((Writer)new BufferedWriter(new OutputStreamWriter(out, charset)), autoFlush);
        if (out instanceof PrintStream) {
            this.psOut = (PrintStream)out;
        }
    }

    public PrintWriter(String fileName) throws FileNotFoundException {
        this((Writer)new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))), false);
    }

    PrintWriter(Writer out, Object lock) {
        super(lock);
        this.trouble = false;
        this.psOut = null;
        this.out = out;
        this.autoFlush = false;
    }

    private PrintWriter(Charset charset, File file) throws FileNotFoundException {
        this((Writer)new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), charset)), false);
    }

    public PrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(PrintWriter.toCharset(csn), new File(fileName));
    }

    public PrintWriter(String fileName, Charset charset) throws IOException {
        this(Objects.requireNonNull(charset, "charset"), new File(fileName));
    }

    public PrintWriter(File file) throws FileNotFoundException {
        this((Writer)new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))), false);
    }

    public PrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(PrintWriter.toCharset(csn), file);
    }

    public PrintWriter(File file, Charset charset) throws IOException {
        this(Objects.requireNonNull(charset, "charset"), file);
    }

    private void ensureOpen() throws IOException {
        if (this.out == null) {
            throw new IOException("Stream closed");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flush() {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implFlush();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implFlush();
        }
    }

    private void implFlush() {
        try {
            this.ensureOpen();
            this.out.flush();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implClose();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implClose();
        }
    }

    private void implClose() {
        try {
            if (this.out != null) {
                this.out.close();
                this.out = null;
            }
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    public boolean checkError() {
        Writer writer;
        if (this.out != null) {
            this.flush();
        }
        if ((writer = this.out) instanceof PrintWriter) {
            PrintWriter pw = (PrintWriter)writer;
            return pw.checkError();
        }
        if (this.psOut != null) {
            return this.psOut.checkError();
        }
        return this.trouble;
    }

    protected void setError() {
        this.trouble = true;
    }

    protected void clearError() {
        this.trouble = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(int c) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implWrite(c);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implWrite(c);
        }
    }

    private void implWrite(int c) {
        try {
            this.ensureOpen();
            this.out.write(c);
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(char[] buf, int off, int len) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implWrite(buf, off, len);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implWrite(buf, off, len);
        }
    }

    private void implWrite(char[] buf, int off, int len) {
        try {
            this.ensureOpen();
            this.out.write(buf, off, len);
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    @Override
    public void write(char[] buf) {
        this.write(buf, 0, buf.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(String s, int off, int len) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implWrite(s, off, len);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implWrite(s, off, len);
        }
    }

    private void implWrite(String s, int off, int len) {
        try {
            this.ensureOpen();
            this.out.write(s, off, len);
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    @Override
    public void write(String s) {
        this.write(s, 0, s.length());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void newLine() {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implNewLine();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implNewLine();
        }
    }

    private void implNewLine() {
        try {
            this.ensureOpen();
            this.out.write(System.lineSeparator());
            if (this.autoFlush) {
                this.out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    public void print(boolean b) {
        this.write(String.valueOf(b));
    }

    public void print(char c) {
        this.write(c);
    }

    public void print(int i) {
        this.write(String.valueOf(i));
    }

    public void print(long l) {
        this.write(String.valueOf(l));
    }

    public void print(float f) {
        this.write(String.valueOf(f));
    }

    public void print(double d) {
        this.write(String.valueOf(d));
    }

    public void print(char[] s) {
        this.write(s);
    }

    public void print(String s) {
        this.write(String.valueOf(s));
    }

    public void print(Object obj) {
        this.write(String.valueOf(obj));
    }

    public void println() {
        this.newLine();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(boolean x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(char x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(int x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(long x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(float x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(double x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(char[] x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(String x) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(x);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(x);
            this.println();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void println(Object x) {
        String s = String.valueOf(x);
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.print(s);
                this.println();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.print(s);
            this.println();
        }
    }

    public PrintWriter printf(String format, Object ... args) {
        return this.format(format, args);
    }

    public PrintWriter printf(Locale l, String format, Object ... args) {
        return this.format(l, format, args);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PrintWriter format(String format, Object ... args) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implFormat(format, args);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implFormat(format, args);
        }
        return this;
    }

    private void implFormat(String format, Object ... args) {
        try {
            this.ensureOpen();
            if (this.formatter == null || this.formatter.locale() != Locale.getDefault()) {
                this.formatter = new Formatter(this);
            }
            this.formatter.format(Locale.getDefault(), format, args);
            if (this.autoFlush) {
                this.out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PrintWriter format(Locale l, String format, Object ... args) {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implFormat(l, format, args);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implFormat(l, format, args);
        }
        return this;
    }

    private void implFormat(Locale l, String format, Object ... args) {
        try {
            this.ensureOpen();
            if (this.formatter == null || this.formatter.locale() != l) {
                this.formatter = new Formatter(this, l);
            }
            this.formatter.format(l, format, args);
            if (this.autoFlush) {
                this.out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            this.trouble = true;
        }
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        this.write(String.valueOf(csq));
        return this;
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public PrintWriter append(char c) {
        this.write(c);
        return this;
    }

    static {
        SharedSecrets.setJavaIOCPrintWriterAccess(new JavaIOPrintWriterAccess(){

            @Override
            public Object lock(PrintWriter pw) {
                return pw.lock;
            }
        });
    }
}

