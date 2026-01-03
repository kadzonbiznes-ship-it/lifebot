/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.core.status.WarnStatus
 */
package ch.qos.logback.core;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ConsoleAppender<E>
extends OutputStreamAppender<E> {
    protected ConsoleTarget target = ConsoleTarget.SystemOut;
    protected boolean withJansi = false;
    private static final String AnsiConsole_CLASS_NAME = "org.fusesource.jansi.AnsiConsole";
    private static final String JANSI2_OUT_METHOD_NAME = "out";
    private static final String JANSI2_ERR_METHOD_NAME = "err";
    private static final String wrapSystemOut_METHOD_NAME = "wrapSystemOut";
    private static final String wrapSystemErr_METHOD_NAME = "wrapSystemErr";
    private static final Class<?>[] ARGUMENT_TYPES = new Class[]{PrintStream.class};

    public void setTarget(String value) {
        ConsoleTarget t = ConsoleTarget.findByName(value.trim());
        if (t == null) {
            this.targetWarn(value);
        } else {
            this.target = t;
        }
    }

    public String getTarget() {
        return this.target.getName();
    }

    private void targetWarn(String val) {
        WarnStatus status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString((Object[])ConsoleTarget.values()), (Object)this);
        status.add((Status)new WarnStatus("Using previously set target, System.out by default.", (Object)this));
        this.addStatus((Status)status);
    }

    @Override
    public void start() {
        OutputStream targetStream = this.target.getStream();
        if (this.withJansi) {
            targetStream = this.wrapWithJansi(targetStream);
        }
        this.setOutputStream(targetStream);
        super.start();
    }

    private OutputStream wrapWithJansi(OutputStream targetStream) {
        try {
            this.addInfo("Enabling JANSI AnsiPrintStream for the console.");
            ClassLoader classLoader = Loader.getClassLoaderOfObject(this.context);
            Class<?> classObj = classLoader.loadClass(AnsiConsole_CLASS_NAME);
            String methodNameJansi2 = this.target == ConsoleTarget.SystemOut ? JANSI2_OUT_METHOD_NAME : JANSI2_ERR_METHOD_NAME;
            Optional<Method> optOutMethod = Arrays.stream(classObj.getMethods()).filter(m -> m.getName().equals(methodNameJansi2)).filter(m -> m.getParameters().length == 0).filter(m -> Modifier.isStatic(m.getModifiers())).filter(m -> PrintStream.class.isAssignableFrom(m.getReturnType())).findAny();
            if (optOutMethod.isPresent()) {
                Method outMethod = optOutMethod.orElseThrow(() -> new NoSuchElementException("No value present"));
                return (PrintStream)outMethod.invoke(null, new Object[0]);
            }
            String methodName = this.target == ConsoleTarget.SystemOut ? wrapSystemOut_METHOD_NAME : wrapSystemErr_METHOD_NAME;
            Method method = classObj.getMethod(methodName, ARGUMENT_TYPES);
            return (OutputStream)method.invoke(null, new PrintStream(targetStream));
        }
        catch (Exception e) {
            this.addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", e);
            return targetStream;
        }
    }

    public boolean isWithJansi() {
        return this.withJansi;
    }

    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
    }
}

