/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

public final class SwingPropertyChangeSupport
extends PropertyChangeSupport {
    private static final long serialVersionUID = 7162625831330845068L;
    private final boolean notifyOnEDT;

    public SwingPropertyChangeSupport(Object sourceBean) {
        this(sourceBean, false);
    }

    public SwingPropertyChangeSupport(Object sourceBean, boolean notifyOnEDT) {
        super(sourceBean);
        this.notifyOnEDT = notifyOnEDT;
    }

    @Override
    public void firePropertyChange(final PropertyChangeEvent evt) {
        if (evt == null) {
            throw new NullPointerException();
        }
        if (!this.isNotifyOnEDT() || SwingUtilities.isEventDispatchThread()) {
            super.firePropertyChange(evt);
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                final /* synthetic */ SwingPropertyChangeSupport this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void run() {
                    this.this$0.firePropertyChange(evt);
                }
            });
        }
    }

    public boolean isNotifyOnEDT() {
        return this.notifyOnEDT;
    }
}

