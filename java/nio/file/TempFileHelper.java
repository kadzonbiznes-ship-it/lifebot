/*
 * Decompiled with CFR 0.152.
 */
package java.nio.file;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;
import jdk.internal.util.StaticProperty;

class TempFileHelper {
    private static final Path tmpdir = Path.of(StaticProperty.javaIoTmpDir(), new String[0]);
    private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    private static final SecureRandom random = new SecureRandom();

    private TempFileHelper() {
    }

    private static Path generatePath(String prefix, String suffix, Path dir) {
        long n = random.nextLong();
        String s = prefix + Long.toUnsignedString(n) + suffix;
        Path name = dir.getFileSystem().getPath(s, new String[0]);
        if (name.getParent() != null) {
            throw new IllegalArgumentException("Invalid prefix or suffix");
        }
        return dir.resolve(name);
    }

    private static Path create(Path dir, String prefix, String suffix, boolean createDirectory, FileAttribute<?>[] attrs) throws IOException {
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            String string = suffix = createDirectory ? "" : ".tmp";
        }
        if (dir == null) {
            dir = tmpdir;
        }
        if (isPosix && dir.getFileSystem() == FileSystems.getDefault()) {
            if (attrs.length == 0) {
                attrs = new FileAttribute[]{createDirectory ? PosixPermissions.dirPermissions : PosixPermissions.filePermissions};
            } else {
                boolean hasPermissions = false;
                for (int i = 0; i < attrs.length; ++i) {
                    if (!attrs[i].name().equals("posix:permissions")) continue;
                    hasPermissions = true;
                    break;
                }
                if (!hasPermissions) {
                    FileAttribute[] copy = new FileAttribute[attrs.length + 1];
                    System.arraycopy(attrs, 0, copy, 0, attrs.length);
                    attrs = copy;
                    attrs[attrs.length - 1] = createDirectory ? PosixPermissions.dirPermissions : PosixPermissions.filePermissions;
                }
            }
        }
        SecurityManager sm = System.getSecurityManager();
        while (true) {
            Path f;
            try {
                f = TempFileHelper.generatePath(prefix, suffix, dir);
            }
            catch (InvalidPathException e) {
                if (sm != null) {
                    throw new IllegalArgumentException("Invalid prefix or suffix");
                }
                throw e;
            }
            try {
                if (createDirectory) {
                    return Files.createDirectory(f, attrs);
                }
                return Files.createFile(f, attrs);
            }
            catch (SecurityException e) {
                if (dir == tmpdir && sm != null) {
                    throw new SecurityException("Unable to create temporary file or directory");
                }
                throw e;
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
                continue;
            }
            break;
        }
    }

    static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>[] attrs) throws IOException {
        return TempFileHelper.create(dir, prefix, suffix, false, attrs);
    }

    static Path createTempDirectory(Path dir, String prefix, FileAttribute<?>[] attrs) throws IOException {
        return TempFileHelper.create(dir, prefix, null, true, attrs);
    }

    private static class PosixPermissions {
        static final FileAttribute<Set<PosixFilePermission>> filePermissions = PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        static final FileAttribute<Set<PosixFilePermission>> dirPermissions = PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));

        private PosixPermissions() {
        }
    }
}

