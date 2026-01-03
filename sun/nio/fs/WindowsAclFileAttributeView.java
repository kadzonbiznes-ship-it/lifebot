/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import sun.nio.fs.AbstractAclFileAttributeView;
import sun.nio.fs.NativeBuffer;
import sun.nio.fs.NativeBuffers;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsLinkSupport;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsPath;
import sun.nio.fs.WindowsSecurity;
import sun.nio.fs.WindowsSecurityDescriptor;
import sun.nio.fs.WindowsUserPrincipals;

class WindowsAclFileAttributeView
extends AbstractAclFileAttributeView {
    private static final short SIZEOF_SECURITY_DESCRIPTOR = 20;
    private final WindowsPath file;
    private final boolean followLinks;

    WindowsAclFileAttributeView(WindowsPath file, boolean followLinks) {
        this.file = file;
        this.followLinks = followLinks;
    }

    private void checkAccess(WindowsPath file, boolean checkRead, boolean checkWrite) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (checkRead) {
                sm.checkRead(file.getPathForPermissionCheck());
            }
            if (checkWrite) {
                sm.checkWrite(file.getPathForPermissionCheck());
            }
            sm.checkPermission(new RuntimePermission("accessUserInformation"));
        }
    }

    static NativeBuffer getFileSecurity(String path, int request) throws IOException {
        int size = 0;
        try {
            size = WindowsNativeDispatcher.GetFileSecurity(path, request, 0L, 0);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(path);
        }
        assert (size > 0);
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            while (true) {
                int newSize;
                if ((newSize = WindowsNativeDispatcher.GetFileSecurity(path, request, buffer.address(), size)) <= size) {
                    return buffer;
                }
                buffer.release();
                buffer = NativeBuffers.getNativeBuffer(newSize);
                size = newSize;
            }
        }
        catch (WindowsException x) {
            buffer.release();
            x.rethrowAsIOException(path);
            return null;
        }
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        this.checkAccess(this.file, true, false);
        String path = WindowsLinkSupport.getFinalPath(this.file, this.followLinks);
        NativeBuffer buffer = WindowsAclFileAttributeView.getFileSecurity(path, 1);
        try {
            long sidAddress = WindowsNativeDispatcher.GetSecurityDescriptorOwner(buffer.address());
            if (sidAddress == 0L) {
                throw new IOException("no owner");
            }
            UserPrincipal userPrincipal = WindowsUserPrincipals.fromSid(sidAddress);
            if (buffer != null) {
                buffer.close();
            }
            return userPrincipal;
        }
        catch (Throwable throwable) {
            try {
                if (buffer != null) {
                    try {
                        buffer.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (WindowsException x) {
                x.rethrowAsIOException(this.file);
                return null;
            }
        }
    }

    @Override
    public List<AclEntry> getAcl() throws IOException {
        this.checkAccess(this.file, true, false);
        String path = WindowsLinkSupport.getFinalPath(this.file, this.followLinks);
        try (NativeBuffer buffer = WindowsAclFileAttributeView.getFileSecurity(path, 4);){
            List<AclEntry> list = WindowsSecurityDescriptor.getAcl(buffer.address());
            return list;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOwner(UserPrincipal obj) throws IOException {
        long pOwner;
        if (obj == null) {
            throw new NullPointerException("'owner' is null");
        }
        if (!(obj instanceof WindowsUserPrincipals.User)) {
            throw new ProviderMismatchException();
        }
        WindowsUserPrincipals.User owner = (WindowsUserPrincipals.User)obj;
        this.checkAccess(this.file, false, true);
        String path = WindowsLinkSupport.getFinalPath(this.file, this.followLinks);
        try {
            pOwner = WindowsNativeDispatcher.ConvertStringSidToSid(owner.sidString());
        }
        catch (WindowsException x) {
            throw new IOException("Failed to get SID for " + owner.getName() + ": " + x.errorString());
        }
        try (NativeBuffer buffer = NativeBuffers.getNativeBuffer(20);){
            WindowsNativeDispatcher.InitializeSecurityDescriptor(buffer.address());
            WindowsNativeDispatcher.SetSecurityDescriptorOwner(buffer.address(), pOwner);
            WindowsSecurity.Privilege priv = WindowsSecurity.enablePrivilege("SeRestorePrivilege");
            try {
                WindowsNativeDispatcher.SetFileSecurity(path, 1, buffer.address());
            }
            finally {
                priv.drop();
            }
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(this.file);
        }
        finally {
            WindowsNativeDispatcher.LocalFree(pOwner);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAcl(List<AclEntry> acl) throws IOException {
        this.checkAccess(this.file, false, true);
        String path = WindowsLinkSupport.getFinalPath(this.file, this.followLinks);
        WindowsSecurityDescriptor sd = WindowsSecurityDescriptor.create(acl);
        try {
            WindowsNativeDispatcher.SetFileSecurity(path, 4, sd.address());
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(this.file);
        }
        finally {
            sd.release();
        }
    }
}

