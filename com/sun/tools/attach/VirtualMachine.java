/*
 * Decompiled with CFR 0.152.
 */
package com.sun.tools.attach;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class VirtualMachine {
    private AttachProvider provider;
    private String id;
    private volatile int hash;

    protected VirtualMachine(AttachProvider provider, String id) {
        if (provider == null) {
            throw new NullPointerException("provider cannot be null");
        }
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        this.provider = provider;
        this.id = id;
    }

    public static List<VirtualMachineDescriptor> list() {
        ArrayList<VirtualMachineDescriptor> l = new ArrayList<VirtualMachineDescriptor>();
        List<AttachProvider> providers = AttachProvider.providers();
        for (AttachProvider provider : providers) {
            l.addAll(provider.listVirtualMachines());
        }
        return l;
    }

    public static VirtualMachine attach(String id) throws AttachNotSupportedException, IOException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        List<AttachProvider> providers = AttachProvider.providers();
        if (providers.size() == 0) {
            throw new AttachNotSupportedException("no providers installed");
        }
        AttachNotSupportedException lastExc = null;
        for (AttachProvider provider : providers) {
            try {
                return provider.attachVirtualMachine(id);
            }
            catch (AttachNotSupportedException x) {
                lastExc = x;
            }
        }
        throw lastExc;
    }

    public static VirtualMachine attach(VirtualMachineDescriptor vmd) throws AttachNotSupportedException, IOException {
        return vmd.provider().attachVirtualMachine(vmd);
    }

    public abstract void detach() throws IOException;

    public final AttachProvider provider() {
        return this.provider;
    }

    public final String id() {
        return this.id;
    }

    public abstract void loadAgentLibrary(String var1, String var2) throws AgentLoadException, AgentInitializationException, IOException;

    public void loadAgentLibrary(String agentLibrary) throws AgentLoadException, AgentInitializationException, IOException {
        this.loadAgentLibrary(agentLibrary, null);
    }

    public abstract void loadAgentPath(String var1, String var2) throws AgentLoadException, AgentInitializationException, IOException;

    public void loadAgentPath(String agentPath) throws AgentLoadException, AgentInitializationException, IOException {
        this.loadAgentPath(agentPath, null);
    }

    public abstract void loadAgent(String var1, String var2) throws AgentLoadException, AgentInitializationException, IOException;

    public void loadAgent(String agent) throws AgentLoadException, AgentInitializationException, IOException {
        this.loadAgent(agent, null);
    }

    public abstract Properties getSystemProperties() throws IOException;

    public abstract Properties getAgentProperties() throws IOException;

    public abstract void startManagementAgent(Properties var1) throws IOException;

    public abstract String startLocalManagementAgent() throws IOException;

    public int hashCode() {
        if (this.hash != 0) {
            return this.hash;
        }
        this.hash = this.provider.hashCode() * 127 + this.id.hashCode();
        return this.hash;
    }

    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (!(ob instanceof VirtualMachine)) {
            return false;
        }
        VirtualMachine other = (VirtualMachine)ob;
        if (other.provider() != this.provider()) {
            return false;
        }
        return other.id().equals(this.id());
    }

    public String toString() {
        return this.provider.toString() + ": " + this.id;
    }
}

