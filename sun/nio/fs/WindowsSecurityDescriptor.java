/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import jdk.internal.misc.Unsafe;
import sun.nio.fs.NativeBuffer;
import sun.nio.fs.NativeBuffers;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsUserPrincipals;

class WindowsSecurityDescriptor {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final short SIZEOF_ACL = 8;
    private static final short SIZEOF_ACCESS_ALLOWED_ACE = 12;
    private static final short SIZEOF_ACCESS_DENIED_ACE = 12;
    private static final short SIZEOF_SECURITY_DESCRIPTOR = 20;
    private static final short OFFSETOF_TYPE = 0;
    private static final short OFFSETOF_FLAGS = 1;
    private static final short OFFSETOF_ACCESS_MASK = 4;
    private static final short OFFSETOF_SID = 8;
    private static final WindowsSecurityDescriptor NULL_DESCRIPTOR = new WindowsSecurityDescriptor();
    private final List<Long> sidList;
    private final NativeBuffer aclBuffer;
    private final NativeBuffer sdBuffer;

    private WindowsSecurityDescriptor() {
        this.sidList = null;
        this.aclBuffer = null;
        this.sdBuffer = null;
    }

    private WindowsSecurityDescriptor(List<AclEntry> acl) throws IOException {
        boolean initialized = false;
        acl = new ArrayList<AclEntry>(acl);
        this.sidList = new ArrayList<Long>(acl.size());
        try {
            int size = 8;
            for (AclEntry entry : acl) {
                UserPrincipal user = entry.principal();
                if (!(user instanceof WindowsUserPrincipals.User)) {
                    throw new ProviderMismatchException();
                }
                String sidString = ((WindowsUserPrincipals.User)user).sidString();
                try {
                    long pSid = WindowsNativeDispatcher.ConvertStringSidToSid(sidString);
                    this.sidList.add(pSid);
                    size += WindowsNativeDispatcher.GetLengthSid(pSid) + Math.max(12, 12);
                }
                catch (WindowsException x) {
                    throw new IOException("Failed to get SID for " + user.getName() + ": " + x.errorString());
                }
            }
            this.aclBuffer = NativeBuffers.getNativeBuffer(size);
            this.sdBuffer = NativeBuffers.getNativeBuffer(20);
            WindowsNativeDispatcher.InitializeAcl(this.aclBuffer.address(), size);
            for (int i = 0; i < acl.size(); ++i) {
                AclEntry entry;
                entry = acl.get(i);
                long pSid = this.sidList.get(i);
                try {
                    WindowsSecurityDescriptor.encode(entry, pSid, this.aclBuffer.address());
                    continue;
                }
                catch (WindowsException x) {
                    throw new IOException("Failed to encode ACE: " + x.errorString());
                }
            }
            WindowsNativeDispatcher.InitializeSecurityDescriptor(this.sdBuffer.address());
            WindowsNativeDispatcher.SetSecurityDescriptorDacl(this.sdBuffer.address(), this.aclBuffer.address());
            initialized = true;
        }
        catch (WindowsException x) {
            throw new IOException(x.getMessage());
        }
        finally {
            if (!initialized) {
                this.release();
            }
        }
    }

    void release() {
        if (this.sdBuffer != null) {
            this.sdBuffer.release();
        }
        if (this.aclBuffer != null) {
            this.aclBuffer.release();
        }
        if (this.sidList != null) {
            for (Long sid : this.sidList) {
                WindowsNativeDispatcher.LocalFree(sid);
            }
        }
    }

    long address() {
        return this.sdBuffer == null ? 0L : this.sdBuffer.address();
    }

    private static AclEntry decode(long aceAddress) throws IOException {
        byte aceType = unsafe.getByte(aceAddress + 0L);
        if (aceType != 0 && aceType != 1) {
            return null;
        }
        AclEntryType type = aceType == 0 ? AclEntryType.ALLOW : AclEntryType.DENY;
        byte aceFlags = unsafe.getByte(aceAddress + 1L);
        EnumSet<AclEntryFlag> flags = EnumSet.noneOf(AclEntryFlag.class);
        if ((aceFlags & 1) != 0) {
            flags.add(AclEntryFlag.FILE_INHERIT);
        }
        if ((aceFlags & 2) != 0) {
            flags.add(AclEntryFlag.DIRECTORY_INHERIT);
        }
        if ((aceFlags & 4) != 0) {
            flags.add(AclEntryFlag.NO_PROPAGATE_INHERIT);
        }
        if ((aceFlags & 8) != 0) {
            flags.add(AclEntryFlag.INHERIT_ONLY);
        }
        int mask = unsafe.getInt(aceAddress + 4L);
        EnumSet<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        if ((mask & 1) > 0) {
            perms.add(AclEntryPermission.READ_DATA);
        }
        if ((mask & 2) > 0) {
            perms.add(AclEntryPermission.WRITE_DATA);
        }
        if ((mask & 4) > 0) {
            perms.add(AclEntryPermission.APPEND_DATA);
        }
        if ((mask & 8) > 0) {
            perms.add(AclEntryPermission.READ_NAMED_ATTRS);
        }
        if ((mask & 0x10) > 0) {
            perms.add(AclEntryPermission.WRITE_NAMED_ATTRS);
        }
        if ((mask & 0x20) > 0) {
            perms.add(AclEntryPermission.EXECUTE);
        }
        if ((mask & 0x40) > 0) {
            perms.add(AclEntryPermission.DELETE_CHILD);
        }
        if ((mask & 0x80) > 0) {
            perms.add(AclEntryPermission.READ_ATTRIBUTES);
        }
        if ((mask & 0x100) > 0) {
            perms.add(AclEntryPermission.WRITE_ATTRIBUTES);
        }
        if ((mask & 0x10000) > 0) {
            perms.add(AclEntryPermission.DELETE);
        }
        if ((mask & 0x20000) > 0) {
            perms.add(AclEntryPermission.READ_ACL);
        }
        if ((mask & 0x40000) > 0) {
            perms.add(AclEntryPermission.WRITE_ACL);
        }
        if ((mask & 0x80000) > 0) {
            perms.add(AclEntryPermission.WRITE_OWNER);
        }
        if ((mask & 0x100000) > 0) {
            perms.add(AclEntryPermission.SYNCHRONIZE);
        }
        long sidAddress = aceAddress + 8L;
        UserPrincipal user = WindowsUserPrincipals.fromSid(sidAddress);
        return AclEntry.newBuilder().setType(type).setPrincipal(user).setFlags(flags).setPermissions(perms).build();
    }

    private static void encode(AclEntry ace, long sidAddress, long aclAddress) throws WindowsException {
        if (ace.type() != AclEntryType.ALLOW && ace.type() != AclEntryType.DENY) {
            return;
        }
        boolean allow = ace.type() == AclEntryType.ALLOW;
        Set<AclEntryPermission> aceMask = ace.permissions();
        int mask = 0;
        if (aceMask.contains((Object)AclEntryPermission.READ_DATA)) {
            mask |= 1;
        }
        if (aceMask.contains((Object)AclEntryPermission.WRITE_DATA)) {
            mask |= 2;
        }
        if (aceMask.contains((Object)AclEntryPermission.APPEND_DATA)) {
            mask |= 4;
        }
        if (aceMask.contains((Object)AclEntryPermission.READ_NAMED_ATTRS)) {
            mask |= 8;
        }
        if (aceMask.contains((Object)AclEntryPermission.WRITE_NAMED_ATTRS)) {
            mask |= 0x10;
        }
        if (aceMask.contains((Object)AclEntryPermission.EXECUTE)) {
            mask |= 0x20;
        }
        if (aceMask.contains((Object)AclEntryPermission.DELETE_CHILD)) {
            mask |= 0x40;
        }
        if (aceMask.contains((Object)AclEntryPermission.READ_ATTRIBUTES)) {
            mask |= 0x80;
        }
        if (aceMask.contains((Object)AclEntryPermission.WRITE_ATTRIBUTES)) {
            mask |= 0x100;
        }
        if (aceMask.contains((Object)AclEntryPermission.DELETE)) {
            mask |= 0x10000;
        }
        if (aceMask.contains((Object)AclEntryPermission.READ_ACL)) {
            mask |= 0x20000;
        }
        if (aceMask.contains((Object)AclEntryPermission.WRITE_ACL)) {
            mask |= 0x40000;
        }
        if (aceMask.contains((Object)AclEntryPermission.WRITE_OWNER)) {
            mask |= 0x80000;
        }
        if (aceMask.contains((Object)AclEntryPermission.SYNCHRONIZE)) {
            mask |= 0x100000;
        }
        Set<AclEntryFlag> aceFlags = ace.flags();
        byte flags = 0;
        if (aceFlags.contains((Object)AclEntryFlag.FILE_INHERIT)) {
            flags = (byte)(flags | 1);
        }
        if (aceFlags.contains((Object)AclEntryFlag.DIRECTORY_INHERIT)) {
            flags = (byte)(flags | 2);
        }
        if (aceFlags.contains((Object)AclEntryFlag.NO_PROPAGATE_INHERIT)) {
            flags = (byte)(flags | 4);
        }
        if (aceFlags.contains((Object)AclEntryFlag.INHERIT_ONLY)) {
            flags = (byte)(flags | 8);
        }
        if (allow) {
            WindowsNativeDispatcher.AddAccessAllowedAceEx(aclAddress, flags, mask, sidAddress);
        } else {
            WindowsNativeDispatcher.AddAccessDeniedAceEx(aclAddress, flags, mask, sidAddress);
        }
    }

    static WindowsSecurityDescriptor create(List<AclEntry> acl) throws IOException {
        return new WindowsSecurityDescriptor(acl);
    }

    static WindowsSecurityDescriptor fromAttribute(FileAttribute<?> ... attrs) throws IOException {
        WindowsSecurityDescriptor sd = NULL_DESCRIPTOR;
        for (FileAttribute<?> attr : attrs) {
            if (sd != NULL_DESCRIPTOR) {
                sd.release();
            }
            if (attr == null) {
                throw new NullPointerException();
            }
            if (!attr.name().equals("acl:acl")) {
                throw new UnsupportedOperationException("'" + attr.name() + "' not supported as initial attribute");
            }
            List acl = (List)attr.value();
            sd = new WindowsSecurityDescriptor(acl);
        }
        return sd;
    }

    static List<AclEntry> getAcl(long pSecurityDescriptor) throws IOException {
        long aclAddress = WindowsNativeDispatcher.GetSecurityDescriptorDacl(pSecurityDescriptor);
        int aceCount = 0;
        if (aclAddress == 0L) {
            aceCount = 0;
        } else {
            WindowsNativeDispatcher.AclInformation aclInfo = WindowsNativeDispatcher.GetAclInformation(aclAddress);
            aceCount = aclInfo.aceCount();
        }
        ArrayList<AclEntry> result = new ArrayList<AclEntry>(aceCount);
        for (int i = 0; i < aceCount; ++i) {
            long aceAddress = WindowsNativeDispatcher.GetAce(aclAddress, i);
            AclEntry entry = WindowsSecurityDescriptor.decode(aceAddress);
            if (entry == null) continue;
            result.add(entry);
        }
        return result;
    }
}

