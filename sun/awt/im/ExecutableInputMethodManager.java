/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.im.spi.InputMethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import sun.awt.AppContext;
import sun.awt.InputMethodSupport;
import sun.awt.SunToolkit;
import sun.awt.im.InputContext;
import sun.awt.im.InputMethodLocator;
import sun.awt.im.InputMethodManager;
import sun.awt.im.InputMethodPopupMenu;

class ExecutableInputMethodManager
extends InputMethodManager
implements Runnable {
    private InputContext currentInputContext;
    private String triggerMenuString;
    private InputMethodPopupMenu selectionMenu;
    private static String selectInputMethodMenuTitle;
    private InputMethodLocator hostAdapterLocator;
    private int javaInputMethodCount;
    private Vector<InputMethodLocator> javaInputMethodLocatorList;
    private Component requestComponent;
    private InputContext requestInputContext;
    private static final String preferredIMNode = "/sun/awt/im/preferredInputMethod";
    private static final String descriptorKey = "descriptor";
    private Hashtable<String, InputMethodLocator> preferredLocatorCache = new Hashtable();
    private Preferences userRoot;

    ExecutableInputMethodManager() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            InputMethodDescriptor hostAdapterDescriptor;
            if (toolkit instanceof InputMethodSupport && (hostAdapterDescriptor = ((InputMethodSupport)((Object)toolkit)).getInputMethodAdapterDescriptor()) != null) {
                this.hostAdapterLocator = new InputMethodLocator(hostAdapterDescriptor, null, null);
            }
        }
        catch (AWTException aWTException) {
            // empty catch block
        }
        this.javaInputMethodLocatorList = new Vector();
        this.initializeInputMethodLocatorList();
    }

    synchronized void initialize() {
        this.triggerMenuString = selectInputMethodMenuTitle = Toolkit.getProperty("AWT.InputMethodSelectionMenu", "Select Input Method");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (!this.hasMultipleInputMethods()) {
            try {
                ExecutableInputMethodManager executableInputMethodManager = this;
                synchronized (executableInputMethodManager) {
                    this.wait();
                }
            }
            catch (InterruptedException interruptedException) {
            }
        }
        while (true) {
            this.waitForChangeRequest();
            this.initializeInputMethodLocatorList();
            try {
                if (this.requestComponent != null) {
                    this.showInputMethodMenuOnRequesterEDT(this.requestComponent);
                    continue;
                }
                EventQueue.invokeAndWait(new Runnable(){

                    @Override
                    public void run() {
                        ExecutableInputMethodManager.this.showInputMethodMenu();
                    }
                });
            }
            catch (InterruptedException interruptedException) {
            }
            catch (InvocationTargetException invocationTargetException) {
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void showInputMethodMenuOnRequesterEDT(Component requester) throws InterruptedException, InvocationTargetException {
        if (requester == null) {
            return;
        }
        class AWTInvocationLock {
            AWTInvocationLock(ExecutableInputMethodManager this$0) {
            }
        }
        AWTInvocationLock lock = new AWTInvocationLock(this);
        InvocationEvent event = new InvocationEvent((Object)requester, new Runnable(){

            @Override
            public void run() {
                ExecutableInputMethodManager.this.showInputMethodMenu();
            }
        }, lock, true);
        AppContext requesterAppContext = SunToolkit.targetToAppContext(requester);
        AWTInvocationLock aWTInvocationLock = lock;
        synchronized (aWTInvocationLock) {
            SunToolkit.postEvent(requesterAppContext, event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }

    @Override
    void setInputContext(InputContext inputContext) {
        if (this.currentInputContext == null || inputContext != null) {
            // empty if block
        }
        this.currentInputContext = inputContext;
    }

    @Override
    public synchronized void notifyChangeRequest(Component comp) {
        if (!(comp instanceof Frame) && !(comp instanceof Dialog)) {
            return;
        }
        if (this.requestComponent != null) {
            return;
        }
        this.requestComponent = comp;
        this.notify();
    }

    @Override
    public synchronized void notifyChangeRequestByHotKey(Component comp) {
        while (!(comp instanceof Frame) && !(comp instanceof Dialog)) {
            if (comp == null) {
                return;
            }
            comp = comp.getParent();
        }
        this.notifyChangeRequest(comp);
    }

    @Override
    public String getTriggerMenuString() {
        return this.triggerMenuString;
    }

    @Override
    boolean hasMultipleInputMethods() {
        return this.hostAdapterLocator != null && this.javaInputMethodCount > 0 || this.javaInputMethodCount > 1;
    }

    private synchronized void waitForChangeRequest() {
        try {
            while (this.requestComponent == null) {
                this.wait();
            }
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void initializeInputMethodLocatorList() {
        Vector<InputMethodLocator> vector = this.javaInputMethodLocatorList;
        synchronized (vector) {
            this.javaInputMethodLocatorList.clear();
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                    @Override
                    public Object run() {
                        for (InputMethodDescriptor descriptor : ServiceLoader.load(InputMethodDescriptor.class, ClassLoader.getSystemClassLoader())) {
                            ClassLoader cl = descriptor.getClass().getClassLoader();
                            ExecutableInputMethodManager.this.javaInputMethodLocatorList.add(new InputMethodLocator(descriptor, cl, null));
                        }
                        return null;
                    }
                });
            }
            catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
            this.javaInputMethodCount = this.javaInputMethodLocatorList.size();
        }
        if (this.hasMultipleInputMethods()) {
            if (this.userRoot == null) {
                this.userRoot = this.getUserRoot();
            }
        } else {
            this.triggerMenuString = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void showInputMethodMenu() {
        if (!this.hasMultipleInputMethods()) {
            this.requestComponent = null;
            return;
        }
        this.selectionMenu = InputMethodPopupMenu.getInstance(this.requestComponent, selectInputMethodMenuTitle);
        this.selectionMenu.removeAll();
        String currentSelection = this.getCurrentSelection();
        if (this.hostAdapterLocator != null) {
            this.selectionMenu.addOneInputMethodToMenu(this.hostAdapterLocator, currentSelection);
            this.selectionMenu.addSeparator();
        }
        for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i) {
            InputMethodLocator locator = this.javaInputMethodLocatorList.get(i);
            this.selectionMenu.addOneInputMethodToMenu(locator, currentSelection);
        }
        ExecutableInputMethodManager executableInputMethodManager = this;
        synchronized (executableInputMethodManager) {
            this.selectionMenu.addToComponent(this.requestComponent);
            this.requestInputContext = this.currentInputContext;
            this.selectionMenu.show(this.requestComponent, 60, 80);
            this.requestComponent = null;
        }
    }

    private String getCurrentSelection() {
        InputMethodLocator locator;
        InputContext inputContext = this.currentInputContext;
        if (inputContext != null && (locator = inputContext.getInputMethodLocator()) != null) {
            return locator.getActionCommandString();
        }
        return null;
    }

    synchronized void changeInputMethod(String choice) {
        InputMethodLocator locator = null;
        String inputMethodName = choice;
        String localeString = null;
        int index = choice.indexOf(10);
        if (index != -1) {
            localeString = choice.substring(index + 1);
            inputMethodName = choice.substring(0, index);
        }
        if (this.hostAdapterLocator.getActionCommandString().equals(inputMethodName)) {
            locator = this.hostAdapterLocator;
        } else {
            for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i) {
                InputMethodLocator candidate = this.javaInputMethodLocatorList.get(i);
                String name = candidate.getActionCommandString();
                if (!name.equals(inputMethodName)) continue;
                locator = candidate;
                break;
            }
        }
        if (locator != null && localeString != null) {
            String language = "";
            String country = "";
            String variant = "";
            int postIndex = localeString.indexOf(95);
            if (postIndex == -1) {
                language = localeString;
            } else {
                language = localeString.substring(0, postIndex);
                int preIndex = postIndex + 1;
                if ((postIndex = localeString.indexOf(95, preIndex)) == -1) {
                    country = localeString.substring(preIndex);
                } else {
                    country = localeString.substring(preIndex, postIndex);
                    variant = localeString.substring(postIndex + 1);
                }
            }
            Locale locale = Locale.of(language, country, variant);
            locator = locator.deriveLocator(locale);
        }
        if (locator == null) {
            return;
        }
        if (this.requestInputContext != null) {
            this.requestInputContext.changeInputMethod(locator);
            this.requestInputContext = null;
            this.putPreferredInputMethod(locator);
        }
    }

    @Override
    InputMethodLocator findInputMethod(Locale locale) {
        InputMethodLocator locator = this.getPreferredInputMethod(locale);
        if (locator != null) {
            return locator;
        }
        if (this.hostAdapterLocator != null && this.hostAdapterLocator.isLocaleAvailable(locale)) {
            return this.hostAdapterLocator.deriveLocator(locale);
        }
        this.initializeInputMethodLocatorList();
        for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i) {
            InputMethodLocator candidate = this.javaInputMethodLocatorList.get(i);
            if (!candidate.isLocaleAvailable(locale)) continue;
            return candidate.deriveLocator(locale);
        }
        return null;
    }

    @Override
    Locale getDefaultKeyboardLocale() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof InputMethodSupport) {
            return ((InputMethodSupport)((Object)toolkit)).getDefaultKeyboardLocale();
        }
        return Locale.getDefault();
    }

    private synchronized InputMethodLocator getPreferredInputMethod(Locale locale) {
        InputMethodLocator preferredLocator = null;
        if (!this.hasMultipleInputMethods()) {
            return null;
        }
        preferredLocator = this.preferredLocatorCache.get(locale.toString().intern());
        if (preferredLocator != null) {
            return preferredLocator;
        }
        String nodePath = this.findPreferredInputMethodNode(locale);
        String descriptorName = this.readPreferredInputMethod(nodePath);
        if (descriptorName != null) {
            if (this.hostAdapterLocator != null && this.hostAdapterLocator.getDescriptor().getClass().getName().equals(descriptorName)) {
                Locale advertised = this.getAdvertisedLocale(this.hostAdapterLocator, locale);
                if (advertised != null) {
                    preferredLocator = this.hostAdapterLocator.deriveLocator(advertised);
                    this.preferredLocatorCache.put(locale.toString().intern(), preferredLocator);
                }
                return preferredLocator;
            }
            for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i) {
                InputMethodLocator locator = this.javaInputMethodLocatorList.get(i);
                InputMethodDescriptor descriptor = locator.getDescriptor();
                if (!descriptor.getClass().getName().equals(descriptorName)) continue;
                Locale advertised = this.getAdvertisedLocale(locator, locale);
                if (advertised != null) {
                    preferredLocator = locator.deriveLocator(advertised);
                    this.preferredLocatorCache.put(locale.toString().intern(), preferredLocator);
                }
                return preferredLocator;
            }
            this.writePreferredInputMethod(nodePath, null);
        }
        return null;
    }

    private String findPreferredInputMethodNode(Locale locale) {
        if (this.userRoot == null) {
            return null;
        }
        Object nodePath = "/sun/awt/im/preferredInputMethod/" + this.createLocalePath(locale);
        while (!((String)nodePath).equals(preferredIMNode)) {
            try {
                if (this.userRoot.nodeExists((String)nodePath) && this.readPreferredInputMethod((String)nodePath) != null) {
                    return nodePath;
                }
            }
            catch (BackingStoreException backingStoreException) {
                // empty catch block
            }
            nodePath = ((String)nodePath).substring(0, ((String)nodePath).lastIndexOf(47));
        }
        return null;
    }

    private String readPreferredInputMethod(String nodePath) {
        if (this.userRoot == null || nodePath == null) {
            return null;
        }
        return this.userRoot.node(nodePath).get(descriptorKey, null);
    }

    private synchronized void putPreferredInputMethod(InputMethodLocator locator) {
        InputMethodDescriptor descriptor = locator.getDescriptor();
        Locale preferredLocale = locator.getLocale();
        if (preferredLocale == null) {
            try {
                Locale[] availableLocales = descriptor.getAvailableLocales();
                if (availableLocales.length != 1) {
                    return;
                }
                preferredLocale = availableLocales[0];
            }
            catch (AWTException ae) {
                return;
            }
        }
        if (preferredLocale.equals(Locale.JAPAN)) {
            preferredLocale = Locale.JAPANESE;
        }
        if (preferredLocale.equals(Locale.KOREA)) {
            preferredLocale = Locale.KOREAN;
        }
        if (preferredLocale.equals(Locale.of("th", "TH"))) {
            preferredLocale = Locale.of("th");
        }
        String path = "/sun/awt/im/preferredInputMethod/" + this.createLocalePath(preferredLocale);
        this.writePreferredInputMethod(path, descriptor.getClass().getName());
        this.preferredLocatorCache.put(preferredLocale.toString().intern(), locator.deriveLocator(preferredLocale));
    }

    private String createLocalePath(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        String localePath = null;
        localePath = !variant.isEmpty() ? "_" + language + "/_" + country + "/_" + variant : (!country.isEmpty() ? "_" + language + "/_" + country : "_" + language);
        return localePath;
    }

    private void writePreferredInputMethod(String path, String descriptorName) {
        if (this.userRoot != null) {
            Preferences node = this.userRoot.node(path);
            if (descriptorName != null) {
                node.put(descriptorKey, descriptorName);
            } else {
                node.remove(descriptorKey);
            }
        }
    }

    private Preferences getUserRoot() {
        return AccessController.doPrivileged(new PrivilegedAction<Preferences>(this){

            @Override
            public Preferences run() {
                return Preferences.userRoot();
            }
        });
    }

    private Locale getAdvertisedLocale(InputMethodLocator locator, Locale locale) {
        Locale advertised = null;
        if (locator.isLocaleAvailable(locale)) {
            advertised = locale;
        } else if (locale.getLanguage().equals("ja")) {
            if (locator.isLocaleAvailable(Locale.JAPAN)) {
                advertised = Locale.JAPAN;
            } else if (locator.isLocaleAvailable(Locale.JAPANESE)) {
                advertised = Locale.JAPANESE;
            }
        } else if (locale.getLanguage().equals("ko")) {
            if (locator.isLocaleAvailable(Locale.KOREA)) {
                advertised = Locale.KOREA;
            } else if (locator.isLocaleAvailable(Locale.KOREAN)) {
                advertised = Locale.KOREAN;
            }
        } else if (locale.getLanguage().equals("th")) {
            if (locator.isLocaleAvailable(Locale.of("th", "TH"))) {
                advertised = Locale.of("th", "TH");
            } else if (locator.isLocaleAvailable(Locale.of("th"))) {
                advertised = Locale.of("th");
            }
        }
        return advertised;
    }
}

