/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventFilter;
import java.awt.Window;
import sun.awt.AppContext;
import sun.awt.ModalExclude;

abstract class ModalEventFilter
implements EventFilter {
    protected Dialog modalDialog;
    protected boolean disabled;

    protected ModalEventFilter(Dialog modalDialog) {
        this.modalDialog = modalDialog;
        this.disabled = false;
    }

    Dialog getModalDialog() {
        return this.modalDialog;
    }

    @Override
    public EventFilter.FilterAction acceptEvent(AWTEvent event) {
        Object o;
        if (this.disabled || !this.modalDialog.isVisible()) {
            return EventFilter.FilterAction.ACCEPT;
        }
        int eventID = event.getID();
        if ((eventID >= 500 && eventID <= 507 || eventID >= 1001 && eventID <= 1001 || eventID == 201) && !((o = event.getSource()) instanceof ModalExclude) && o instanceof Component) {
            Component c;
            for (c = (Component)o; c != null && !(c instanceof Window); c = c.getParent_NoClientCode()) {
            }
            if (c != null) {
                return this.acceptWindow((Window)c);
            }
        }
        return EventFilter.FilterAction.ACCEPT;
    }

    protected abstract EventFilter.FilterAction acceptWindow(Window var1);

    void disable() {
        this.disabled = true;
    }

    int compareTo(ModalEventFilter another) {
        Dialog blocker;
        Container c;
        Dialog anotherDialog = another.getModalDialog();
        for (c = this.modalDialog; c != null; c = c.getParent_NoClientCode()) {
            if (c != anotherDialog) continue;
            return 1;
        }
        for (c = anotherDialog; c != null; c = c.getParent_NoClientCode()) {
            if (c != this.modalDialog) continue;
            return -1;
        }
        for (blocker = this.modalDialog.getModalBlocker(); blocker != null; blocker = blocker.getModalBlocker()) {
            if (blocker != anotherDialog) continue;
            return -1;
        }
        for (blocker = anotherDialog.getModalBlocker(); blocker != null; blocker = blocker.getModalBlocker()) {
            if (blocker != this.modalDialog) continue;
            return 1;
        }
        return this.modalDialog.getModalityType().compareTo(anotherDialog.getModalityType());
    }

    static ModalEventFilter createFilterForDialog(Dialog modalDialog) {
        switch (modalDialog.getModalityType()) {
            case DOCUMENT_MODAL: {
                return new DocumentModalEventFilter(modalDialog);
            }
            case APPLICATION_MODAL: {
                return new ApplicationModalEventFilter(modalDialog);
            }
            case TOOLKIT_MODAL: {
                return new ToolkitModalEventFilter(modalDialog);
            }
        }
        return null;
    }

    private static class DocumentModalEventFilter
    extends ModalEventFilter {
        private Window documentRoot;

        DocumentModalEventFilter(Dialog modalDialog) {
            super(modalDialog);
            this.documentRoot = modalDialog.getDocumentRoot();
        }

        @Override
        protected EventFilter.FilterAction acceptWindow(Window w) {
            if (w.isModalExcluded(Dialog.ModalExclusionType.APPLICATION_EXCLUDE)) {
                for (Window w1 = this.modalDialog.getOwner(); w1 != null; w1 = w1.getOwner()) {
                    if (w1 != w) continue;
                    return EventFilter.FilterAction.REJECT;
                }
                return EventFilter.FilterAction.ACCEPT;
            }
            while (w != null) {
                if (w == this.modalDialog) {
                    return EventFilter.FilterAction.ACCEPT_IMMEDIATELY;
                }
                if (w == this.documentRoot) {
                    return EventFilter.FilterAction.REJECT;
                }
                w = w.getOwner();
            }
            return EventFilter.FilterAction.ACCEPT;
        }
    }

    private static class ApplicationModalEventFilter
    extends ModalEventFilter {
        private AppContext appContext;

        ApplicationModalEventFilter(Dialog modalDialog) {
            super(modalDialog);
            this.appContext = modalDialog.appContext;
        }

        @Override
        protected EventFilter.FilterAction acceptWindow(Window w) {
            if (w.isModalExcluded(Dialog.ModalExclusionType.APPLICATION_EXCLUDE)) {
                return EventFilter.FilterAction.ACCEPT;
            }
            if (w.appContext == this.appContext) {
                while (w != null) {
                    if (w == this.modalDialog) {
                        return EventFilter.FilterAction.ACCEPT_IMMEDIATELY;
                    }
                    w = w.getOwner();
                }
                return EventFilter.FilterAction.REJECT;
            }
            return EventFilter.FilterAction.ACCEPT;
        }
    }

    private static class ToolkitModalEventFilter
    extends ModalEventFilter {
        private AppContext appContext;

        ToolkitModalEventFilter(Dialog modalDialog) {
            super(modalDialog);
            this.appContext = modalDialog.appContext;
        }

        @Override
        protected EventFilter.FilterAction acceptWindow(Window w) {
            if (w.isModalExcluded(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE)) {
                return EventFilter.FilterAction.ACCEPT;
            }
            if (w.appContext != this.appContext) {
                return EventFilter.FilterAction.REJECT;
            }
            while (w != null) {
                if (w == this.modalDialog) {
                    return EventFilter.FilterAction.ACCEPT_IMMEDIATELY;
                }
                w = w.getOwner();
            }
            return EventFilter.FilterAction.REJECT;
        }
    }
}

