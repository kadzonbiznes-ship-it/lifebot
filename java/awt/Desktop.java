/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTPermission;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenURIHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.PrintFilesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitStrategy;
import java.awt.desktop.SystemEventListener;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.Objects;
import javax.swing.JMenuBar;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class Desktop {
    private DesktopPeer peer;

    private Desktop() {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        if (defaultToolkit instanceof SunToolkit) {
            this.peer = ((SunToolkit)defaultToolkit).createDesktopPeer(this);
        }
    }

    private void checkEventsProcessingPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("canProcessApplicationEvents"));
        }
    }

    public static synchronized Desktop getDesktop() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop API is not supported on the current platform");
        }
        AppContext context = AppContext.getAppContext();
        Desktop desktop = (Desktop)context.get(Desktop.class);
        if (desktop == null) {
            desktop = new Desktop();
            context.put(Desktop.class, desktop);
        }
        return desktop;
    }

    public static boolean isDesktopSupported() {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        if (defaultToolkit instanceof SunToolkit) {
            return ((SunToolkit)defaultToolkit).isDesktopSupported();
        }
        return false;
    }

    public boolean isSupported(Action action) {
        return this.peer.isSupported(action);
    }

    private static void checkFileValidation(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("The file: " + file.getPath() + " doesn't exist.");
        }
    }

    private void checkActionSupport(Action actionType) {
        if (!this.isSupported(actionType)) {
            throw new UnsupportedOperationException("The " + actionType.name() + " action is not supported on the current platform!");
        }
    }

    private void checkAWTPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission("showWindowWithoutWarningBanner"));
        }
    }

    public void open(File file) throws IOException {
        file = new File(file.getPath());
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.OPEN);
        Desktop.checkFileValidation(file);
        this.peer.open(file);
    }

    public void edit(File file) throws IOException {
        file = new File(file.getPath());
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.EDIT);
        file.canWrite();
        Desktop.checkFileValidation(file);
        if (file.isDirectory()) {
            throw new IOException(file.getPath() + " is a directory");
        }
        this.peer.edit(file);
    }

    public void print(File file) throws IOException {
        file = new File(file.getPath());
        this.checkExec();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPrintJobAccess();
        }
        this.checkActionSupport(Action.PRINT);
        Desktop.checkFileValidation(file);
        if (file.isDirectory()) {
            throw new IOException(file.getPath() + " is a directory");
        }
        this.peer.print(file);
    }

    public void browse(URI uri) throws IOException {
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.BROWSE);
        Objects.requireNonNull(uri);
        this.peer.browse(uri);
    }

    public void mail() throws IOException {
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.MAIL);
        URI mailtoURI = null;
        try {
            mailtoURI = new URI("mailto:?");
            this.peer.mail(mailtoURI);
        }
        catch (URISyntaxException uRISyntaxException) {
            // empty catch block
        }
    }

    public void mail(URI mailtoURI) throws IOException {
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.MAIL);
        if (mailtoURI == null) {
            throw new NullPointerException();
        }
        if (!"mailto".equalsIgnoreCase(mailtoURI.getScheme())) {
            throw new IllegalArgumentException("URI scheme is not \"mailto\"");
        }
        this.peer.mail(mailtoURI);
    }

    private void checkExec() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new FilePermission("<<ALL FILES>>", "execute"));
        }
    }

    private void checkRead() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new FilePermission("<<ALL FILES>>", "read"));
        }
    }

    private void checkQuitPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkExit(0);
        }
    }

    public void addAppEventListener(SystemEventListener listener) {
        this.checkEventsProcessingPermission();
        this.peer.addAppEventListener(listener);
    }

    public void removeAppEventListener(SystemEventListener listener) {
        this.checkEventsProcessingPermission();
        this.peer.removeAppEventListener(listener);
    }

    public void setAboutHandler(AboutHandler aboutHandler) {
        this.checkEventsProcessingPermission();
        this.checkActionSupport(Action.APP_ABOUT);
        this.peer.setAboutHandler(aboutHandler);
    }

    public void setPreferencesHandler(PreferencesHandler preferencesHandler) {
        this.checkEventsProcessingPermission();
        this.checkActionSupport(Action.APP_PREFERENCES);
        this.peer.setPreferencesHandler(preferencesHandler);
    }

    public void setOpenFileHandler(OpenFilesHandler openFileHandler) {
        this.checkEventsProcessingPermission();
        this.checkExec();
        this.checkRead();
        this.checkActionSupport(Action.APP_OPEN_FILE);
        this.peer.setOpenFileHandler(openFileHandler);
    }

    public void setPrintFileHandler(PrintFilesHandler printFileHandler) {
        this.checkEventsProcessingPermission();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPrintJobAccess();
        }
        this.checkActionSupport(Action.APP_PRINT_FILE);
        this.peer.setPrintFileHandler(printFileHandler);
    }

    public void setOpenURIHandler(OpenURIHandler openURIHandler) {
        this.checkEventsProcessingPermission();
        this.checkExec();
        this.checkActionSupport(Action.APP_OPEN_URI);
        this.peer.setOpenURIHandler(openURIHandler);
    }

    public void setQuitHandler(QuitHandler quitHandler) {
        this.checkEventsProcessingPermission();
        this.checkQuitPermission();
        this.checkActionSupport(Action.APP_QUIT_HANDLER);
        this.peer.setQuitHandler(quitHandler);
    }

    public void setQuitStrategy(QuitStrategy strategy) {
        this.checkEventsProcessingPermission();
        this.checkQuitPermission();
        this.checkActionSupport(Action.APP_QUIT_STRATEGY);
        this.peer.setQuitStrategy(strategy);
    }

    public void enableSuddenTermination() {
        this.checkEventsProcessingPermission();
        this.checkQuitPermission();
        this.checkActionSupport(Action.APP_SUDDEN_TERMINATION);
        this.peer.enableSuddenTermination();
    }

    public void disableSuddenTermination() {
        this.checkEventsProcessingPermission();
        this.checkQuitPermission();
        this.checkActionSupport(Action.APP_SUDDEN_TERMINATION);
        this.peer.disableSuddenTermination();
    }

    public void requestForeground(boolean allWindows) {
        this.checkEventsProcessingPermission();
        this.checkActionSupport(Action.APP_REQUEST_FOREGROUND);
        this.peer.requestForeground(allWindows);
    }

    public void openHelpViewer() {
        this.checkAWTPermission();
        this.checkExec();
        this.checkEventsProcessingPermission();
        this.checkActionSupport(Action.APP_HELP_VIEWER);
        this.peer.openHelpViewer();
    }

    public void setDefaultMenuBar(JMenuBar menuBar) {
        Container parent;
        this.checkEventsProcessingPermission();
        this.checkActionSupport(Action.APP_MENU_BAR);
        if (menuBar != null && (parent = menuBar.getParent()) != null) {
            parent.remove(menuBar);
            menuBar.updateUI();
        }
        this.peer.setDefaultMenuBar(menuBar);
    }

    public void browseFileDirectory(File file) {
        file = new File(file.getPath());
        this.checkAWTPermission();
        this.checkExec();
        this.checkActionSupport(Action.BROWSE_FILE_DIR);
        Desktop.checkFileValidation(file);
        File parentFile = file.getParentFile();
        if (parentFile == null || !parentFile.exists()) {
            throw new IllegalArgumentException("Parent folder doesn't exist");
        }
        this.peer.browseFileDirectory(file);
    }

    public boolean moveToTrash(File file) {
        file = new File(file.getPath());
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkDelete(file.getPath());
        }
        this.checkActionSupport(Action.MOVE_TO_TRASH);
        File finalFile = file;
        AccessController.doPrivileged(() -> {
            Desktop.checkFileValidation(finalFile);
            return null;
        });
        return this.peer.moveToTrash(file);
    }

    public static enum Action {
        OPEN,
        EDIT,
        PRINT,
        MAIL,
        BROWSE,
        APP_EVENT_FOREGROUND,
        APP_EVENT_HIDDEN,
        APP_EVENT_REOPENED,
        APP_EVENT_SCREEN_SLEEP,
        APP_EVENT_SYSTEM_SLEEP,
        APP_EVENT_USER_SESSION,
        APP_ABOUT,
        APP_PREFERENCES,
        APP_OPEN_FILE,
        APP_PRINT_FILE,
        APP_OPEN_URI,
        APP_QUIT_HANDLER,
        APP_QUIT_STRATEGY,
        APP_SUDDEN_TERMINATION,
        APP_REQUEST_FOREGROUND,
        APP_HELP_VIEWER,
        APP_MENU_BAR,
        BROWSE_FILE_DIR,
        MOVE_TO_TRASH;

    }
}

