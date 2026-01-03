/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.im;

import java.awt.AWTException;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

final class InputMethodLocator {
    private InputMethodDescriptor descriptor;
    private ClassLoader loader;
    private Locale locale;

    InputMethodLocator(InputMethodDescriptor descriptor, ClassLoader loader, Locale locale) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor can't be null");
        }
        this.descriptor = descriptor;
        this.loader = loader;
        this.locale = locale;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        InputMethodLocator otherLocator = (InputMethodLocator)other;
        if (!this.descriptor.getClass().equals(otherLocator.descriptor.getClass())) {
            return false;
        }
        if (this.loader == null && otherLocator.loader != null || this.loader != null && !this.loader.equals(otherLocator.loader)) {
            return false;
        }
        return (this.locale != null || otherLocator.locale == null) && (this.locale == null || this.locale.equals(otherLocator.locale));
    }

    public int hashCode() {
        int result = this.descriptor.hashCode();
        if (this.loader != null) {
            result |= this.loader.hashCode() << 10;
        }
        if (this.locale != null) {
            result |= this.locale.hashCode() << 20;
        }
        return result;
    }

    InputMethodDescriptor getDescriptor() {
        return this.descriptor;
    }

    ClassLoader getClassLoader() {
        return this.loader;
    }

    Locale getLocale() {
        return this.locale;
    }

    boolean isLocaleAvailable(Locale locale) {
        try {
            Locale[] locales = this.descriptor.getAvailableLocales();
            for (int i = 0; i < locales.length; ++i) {
                if (!locales[i].equals(locale)) continue;
                return true;
            }
        }
        catch (AWTException aWTException) {
            // empty catch block
        }
        return false;
    }

    InputMethodLocator deriveLocator(Locale forLocale) {
        if (forLocale == this.locale) {
            return this;
        }
        return new InputMethodLocator(this.descriptor, this.loader, forLocale);
    }

    boolean sameInputMethod(InputMethodLocator other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!this.descriptor.getClass().equals(other.descriptor.getClass())) {
            return false;
        }
        return (this.loader != null || other.loader == null) && (this.loader == null || this.loader.equals(other.loader));
    }

    String getActionCommandString() {
        String inputMethodString = this.descriptor.getClass().getName();
        if (this.locale == null) {
            return inputMethodString;
        }
        return inputMethodString + "\n" + this.locale.toString();
    }
}

