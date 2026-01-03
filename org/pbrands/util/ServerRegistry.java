/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Generated;
import org.pbrands.model.ServerInfo;

public class ServerRegistry {
    private final List<ServerInfo> servers = new ArrayList<ServerInfo>();

    private void handleError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message, "Error", 0));
    }

    public ServerInfo getServer(int id) {
        return this.servers.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    @Generated
    public List<ServerInfo> getServers() {
        return this.servers;
    }
}

