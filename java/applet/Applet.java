/*
 * Decompiled with CFR 0.152.
 */
package java.applet;

import com.sun.media.sound.JavaSoundAudioClip;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.AWTPermission;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Panel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

@Deprecated(since="9", forRemoval=true)
public class Applet
extends Panel {
    private transient AppletStub stub;
    private static final long serialVersionUID = -5836846270535785031L;
    AccessibleContext accessibleContext = null;

    public Applet() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        s.defaultReadObject();
    }

    public final void setStub(AppletStub stub) {
        SecurityManager s;
        if (this.stub != null && (s = System.getSecurityManager()) != null) {
            s.checkPermission(new AWTPermission("setAppletStub"));
        }
        this.stub = stub;
    }

    public boolean isActive() {
        if (this.stub != null) {
            return this.stub.isActive();
        }
        return false;
    }

    public URL getDocumentBase() {
        return this.stub.getDocumentBase();
    }

    public URL getCodeBase() {
        return this.stub.getCodeBase();
    }

    public String getParameter(String name) {
        return this.stub.getParameter(name);
    }

    public AppletContext getAppletContext() {
        return this.stub.getAppletContext();
    }

    @Override
    public void resize(int width, int height) {
        Dimension d = this.size();
        if (d.width != width || d.height != height) {
            super.resize(width, height);
            if (this.stub != null) {
                this.stub.appletResize(width, height);
            }
        }
    }

    @Override
    public void resize(Dimension d) {
        this.resize(d.width, d.height);
    }

    @Override
    public boolean isValidateRoot() {
        return true;
    }

    public void showStatus(String msg) {
        this.getAppletContext().showStatus(msg);
    }

    public Image getImage(URL url) {
        return this.getAppletContext().getImage(url);
    }

    public Image getImage(URL url, String name) {
        try {
            URL u = new URL(url, name);
            return this.getImage(u);
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public static final AudioClip newAudioClip(URL url) {
        return JavaSoundAudioClip.create(url);
    }

    public AudioClip getAudioClip(URL url) {
        return this.getAppletContext().getAudioClip(url);
    }

    public AudioClip getAudioClip(URL url, String name) {
        try {
            URL u = new URL(url, name);
            return this.getAudioClip(u);
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public String getAppletInfo() {
        return null;
    }

    @Override
    public Locale getLocale() {
        Locale locale = super.getLocale();
        if (locale == null) {
            return Locale.getDefault();
        }
        return locale;
    }

    public String[][] getParameterInfo() {
        return null;
    }

    public void play(URL url) {
        AudioClip clip = this.getAudioClip(url);
        if (clip != null) {
            clip.play();
        }
    }

    public void play(URL url, String name) {
        AudioClip clip = this.getAudioClip(url, name);
        if (clip != null) {
            clip.play();
        }
    }

    public void init() {
    }

    public void start() {
    }

    public void stop() {
    }

    public void destroy() {
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleApplet();
        }
        return this.accessibleContext;
    }

    protected class AccessibleApplet
    extends Panel.AccessibleAWTPanel {
        private static final long serialVersionUID = 8127374778187708896L;

        protected AccessibleApplet() {
            super(Applet.this);
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FRAME;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.ACTIVE);
            return states;
        }
    }
}

