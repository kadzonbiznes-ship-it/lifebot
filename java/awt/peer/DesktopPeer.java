/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.Desktop;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenURIHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.PrintFilesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitStrategy;
import java.awt.desktop.SystemEventListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.JMenuBar;

public interface DesktopPeer {
    public boolean isSupported(Desktop.Action var1);

    public void open(File var1) throws IOException;

    public void edit(File var1) throws IOException;

    public void print(File var1) throws IOException;

    public void mail(URI var1) throws IOException;

    public void browse(URI var1) throws IOException;

    default public void addAppEventListener(SystemEventListener listener) {
    }

    default public void removeAppEventListener(SystemEventListener listener) {
    }

    default public void setAboutHandler(AboutHandler aboutHandler) {
    }

    default public void setPreferencesHandler(PreferencesHandler preferencesHandler) {
    }

    default public void setOpenFileHandler(OpenFilesHandler openFileHandler) {
    }

    default public void setPrintFileHandler(PrintFilesHandler printFileHandler) {
    }

    default public void setOpenURIHandler(OpenURIHandler openURIHandler) {
    }

    default public void setQuitHandler(QuitHandler quitHandler) {
    }

    default public void setQuitStrategy(QuitStrategy strategy) {
    }

    default public void enableSuddenTermination() {
    }

    default public void disableSuddenTermination() {
    }

    default public void requestForeground(boolean allWindows) {
    }

    default public void openHelpViewer() {
    }

    default public void setDefaultMenuBar(JMenuBar menuBar) {
    }

    default public boolean browseFileDirectory(File file) {
        return false;
    }

    default public boolean moveToTrash(File file) {
        return false;
    }
}

