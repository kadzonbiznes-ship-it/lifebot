/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import sun.nio.ch.FileKey;

class FileLockTable {
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap = new ConcurrentHashMap();
    private static ReferenceQueue<FileLock> queue = new ReferenceQueue();
    private final Channel channel;
    private final FileKey fileKey;
    private final Set<FileLock> locks;

    FileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
        this.locks = new HashSet<FileLock>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list = lockMap.get(this.fileKey);
        while (true) {
            if (list == null) {
                List<FileLockReference> prev;
                List<FileLockReference> list2 = list = new ArrayList<FileLockReference>(2);
                synchronized (list2) {
                    prev = lockMap.putIfAbsent(this.fileKey, list);
                    if (prev == null) {
                        list.add(new FileLockReference(fl, queue, this.fileKey));
                        this.locks.add(fl);
                        break;
                    }
                }
                list = prev;
            }
            List<FileLockReference> list3 = list;
            synchronized (list3) {
                List<FileLockReference> current = lockMap.get(this.fileKey);
                if (list == current) {
                    this.checkList(list, fl.position(), fl.size());
                    list.add(new FileLockReference(fl, queue, this.fileKey));
                    this.locks.add(fl);
                    break;
                }
                list = current;
            }
        }
        this.removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        assert (Thread.holdsLock(list));
        assert (lockMap.get(fk) == list);
        if (list.isEmpty()) {
            lockMap.remove(fk);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void remove(FileLock fl) {
        assert (fl != null);
        List<FileLockReference> list = lockMap.get(this.fileKey);
        if (list == null) {
            return;
        }
        List<FileLockReference> list2 = list;
        synchronized (list2) {
            for (int index = 0; index < list.size(); ++index) {
                FileLockReference ref = list.get(index);
                FileLock lock = (FileLock)ref.get();
                if (lock != fl) continue;
                assert (lock != null && lock.acquiredBy() == this.channel);
                ref.clear();
                list.remove(index);
                this.locks.remove(fl);
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    List<FileLock> removeAll() {
        ArrayList<FileLock> result = new ArrayList<FileLock>();
        List<FileLockReference> list = lockMap.get(this.fileKey);
        if (list != null) {
            List<FileLockReference> list2 = list;
            synchronized (list2) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = list.get(index);
                    FileLock lock = (FileLock)ref.get();
                    if (lock != null && lock.acquiredBy() == this.channel) {
                        ref.clear();
                        list.remove(index);
                        result.add(lock);
                        continue;
                    }
                    ++index;
                }
                this.removeKeyIfEmpty(this.fileKey, list);
                this.locks.clear();
            }
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = lockMap.get(this.fileKey);
        assert (list != null);
        List<FileLockReference> list2 = list;
        synchronized (list2) {
            for (int index = 0; index < list.size(); ++index) {
                FileLockReference ref = list.get(index);
                FileLock lock = (FileLock)ref.get();
                if (lock != fromLock) continue;
                ref.clear();
                list.set(index, new FileLockReference(toLock, queue, this.fileKey));
                this.locks.remove(fromLock);
                this.locks.add(toLock);
                break;
            }
        }
    }

    private void checkList(List<FileLockReference> list, long position, long size) throws OverlappingFileLockException {
        assert (Thread.holdsLock(list));
        for (FileLockReference ref : list) {
            FileLock fl = (FileLock)ref.get();
            if (fl == null || !fl.overlaps(position, size)) continue;
            throw new OverlappingFileLockException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeStaleEntries() {
        FileLockReference ref;
        while ((ref = (FileLockReference)queue.poll()) != null) {
            FileKey fk = ref.fileKey();
            List<FileLockReference> list = lockMap.get(fk);
            if (list == null) continue;
            List<FileLockReference> list2 = list;
            synchronized (list2) {
                list.remove(ref);
                this.removeKeyIfEmpty(fk, list);
            }
        }
    }

    private static class FileLockReference
    extends WeakReference<FileLock> {
        private FileKey fileKey;

        FileLockReference(FileLock referent, ReferenceQueue<FileLock> queue, FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }

        FileKey fileKey() {
            return this.fileKey;
        }
    }
}

