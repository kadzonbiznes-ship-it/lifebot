/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.core.status.ErrorStatus
 */
package ch.qos.logback.core;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

public class OutputStreamAppender<E>
extends UnsynchronizedAppenderBase<E> {
    protected Encoder<E> encoder;
    protected final ReentrantLock streamWriteLock = new ReentrantLock(false);
    private OutputStream outputStream;
    boolean immediateFlush = true;

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void start() {
        int errors = 0;
        if (this.encoder == null) {
            this.addStatus((Status)new ErrorStatus("No encoder set for the appender named \"" + this.name + "\".", (Object)this));
            ++errors;
        }
        if (this.outputStream == null) {
            this.addStatus((Status)new ErrorStatus("No output stream set for the appender named \"" + this.name + "\".", (Object)this));
            ++errors;
        }
        if (errors == 0) {
            super.start();
        }
    }

    public void setLayout(Layout<E> layout) {
        this.addWarn("This appender no longer admits a layout as a sub-component, set an encoder instead.");
        this.addWarn("To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.");
        this.addWarn("See also http://logback.qos.ch/codes.html#layoutInsteadOfEncoder for details");
        LayoutWrappingEncoder<E> lwe = new LayoutWrappingEncoder<E>();
        lwe.setLayout(layout);
        lwe.setContext(this.context);
        this.encoder = lwe;
    }

    @Override
    protected void append(E eventObject) {
        if (!this.isStarted()) {
            return;
        }
        this.subAppend(eventObject);
    }

    @Override
    public void stop() {
        if (!this.isStarted()) {
            return;
        }
        this.streamWriteLock.lock();
        try {
            this.closeOutputStream();
            super.stop();
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    protected void closeOutputStream() {
        if (this.outputStream != null) {
            try {
                this.encoderClose();
                this.outputStream.close();
                this.outputStream = null;
            }
            catch (IOException e) {
                this.addStatus((Status)new ErrorStatus("Could not close output stream for OutputStreamAppender.", (Object)this, (Throwable)e));
            }
        }
    }

    void encoderClose() {
        if (this.encoder != null && this.outputStream != null) {
            try {
                byte[] footer = this.encoder.footerBytes();
                this.writeBytes(footer);
            }
            catch (IOException ioe) {
                this.started = false;
                this.addStatus((Status)new ErrorStatus("Failed to write footer for appender named [" + this.name + "].", (Object)this, (Throwable)ioe));
            }
        }
    }

    public void setOutputStream(OutputStream outputStream) {
        this.streamWriteLock.lock();
        try {
            this.closeOutputStream();
            this.outputStream = outputStream;
            if (this.encoder == null) {
                this.addWarn("Encoder has not been set. Cannot invoke its init method.");
                return;
            }
            this.encoderInit();
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    void encoderInit() {
        if (this.encoder != null && this.outputStream != null) {
            try {
                byte[] header = this.encoder.headerBytes();
                this.writeBytes(header);
            }
            catch (IOException ioe) {
                this.started = false;
                this.addStatus((Status)new ErrorStatus("Failed to initialize encoder for appender named [" + this.name + "].", (Object)this, (Throwable)ioe));
            }
        }
    }

    protected void writeOut(E event) throws IOException {
        byte[] byteArray = this.encoder.encode(event);
        this.writeBytes(byteArray);
    }

    private void writeBytes(byte[] byteArray) throws IOException {
        if (byteArray == null || byteArray.length == 0) {
            return;
        }
        this.streamWriteLock.lock();
        try {
            this.writeByteArrayToOutputStreamWithPossibleFlush(byteArray);
        }
        finally {
            this.streamWriteLock.unlock();
        }
    }

    protected final void writeByteArrayToOutputStreamWithPossibleFlush(byte[] byteArray) throws IOException {
        this.outputStream.write(byteArray);
        if (this.immediateFlush) {
            this.outputStream.flush();
        }
    }

    protected void subAppend(E event) {
        if (!this.isStarted()) {
            return;
        }
        try {
            if (event instanceof DeferredProcessingAware) {
                ((DeferredProcessingAware)event).prepareForDeferredProcessing();
            }
            this.writeOut(event);
        }
        catch (IOException ioe) {
            this.started = false;
            this.addStatus((Status)new ErrorStatus("IO failure in appender", (Object)this, (Throwable)ioe));
        }
    }

    public Encoder<E> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public boolean isImmediateFlush() {
        return this.immediateFlush;
    }

    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }
}

