/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides methods to synchronize {@link MetaFile}.
 * 
 * @author Alexander Alexeev
 */
public class Synchronizer {
    private Synchronizer() {
    }

    /**
     * Synchronizes two {@link MetaFile}. If <code>matcher</code> is not <code>null</code>
     * then <code>MetaFile</code> tree is sifted before synchronization using <code>matcher</code>.
     * 
     * @param first first <code>MetaFile</code>.
     * @param second second <code>MetaFile</code>.
     * @param matcher matcher to be used to filter files taking part in synchronization.
     * @return synchronization patch contained information about changes or <code>null</code>
     *          if there are no differences.
     * @throws NullPointerException if both <code>MetaFiles</code> are <code>null</code>.
     * @throws SynchronizationException 
     * @see SyncPatch
     */
    public static SyncPatch sync(MetaFile first, MetaFile second, FileMatcher matcher, String hashFunc)
            throws NullPointerException, SynchronizationException, IOException {
        if ((first == null) && (second == null)) {
            throw new NullPointerException();
        }

        if (matcher != null) {
            FileSieve sieve = new FileSieve(matcher);
            first = first != null ? sieve.sift(first) : null;
            second = second != null ? sieve.sift(second) : null;
        }

        if (first == null) {
            return new SyncPatch(second, null);
        }
        if (second == null) {
            return new SyncPatch(first, null);
        }
        if (first.isFile() && !second.isFile() || !first.isFile() && second.isFile()) {
            throw new SynchronizationException("Synchronization impossible between file and directory");
        }

        return recurSync(first, second, hashFunc);
    }

    /**
     * Synchronizes two {@link MetaFile}. Calling this method has the same effect as calling
     * <code>sync(first, second, null)</code>.
     * @param first first <code>MetaFile</code>.
     * @param second second <code>MetaFile</code>.
     * @return synchronization patch contained information about changes or <code>null</code>
     *          if there are no differences.
     * @throws NullPointerException if both <code>MetaFiles</code> are <code>null</code>.
     * @throws SynchronizationException 
     * @see SyncPatch
     */
    public static SyncPatch sync(MetaFile first, MetaFile second)
            throws NullPointerException, SynchronizationException, IOException {
        return sync(first, second, null, null);
    }

//  private stuff
//  =============================================================================================
    private static SyncPatch recurSync(MetaFile first, MetaFile second, String hashFunc)
            throws SynchronizationException, IOException {
        if (first.isFile()) {
            if (first.getSize() == second.getSize()) {
                InputStream fis = first.getInputStream();
                InputStream sis = second.getInputStream();
                if (hashFunc == null) {
                    if (FileUtils.isEqual(fis, sis)) {
                        return null;
                    }
                } else {
                    byte[] hash1 = first.getHash(hashFunc);
                    byte[] hash2 = second.getHash(hashFunc);
                    if ((hash1 == null) && (hash2 == null) ||
                        ((hash1 == null) || (hash2 == null)) &&
                                (HashManager.getHashManager().getCalculator(hashFunc) == null)) {
                        if (FileUtils.isEqual(fis, sis)) {
                            return null;
                        }
                    }
                    if (hash1 == null) {
                        hash1 = HashManager.getHashManager().getCalculator(hashFunc).calculate(fis);
                    }
                    if (hash2 == null) {
                        hash2 = HashManager.getHashManager().getCalculator(hashFunc).calculate(sis);
                    }
                    if (Arrays.equals(hash1, hash2)) {
                        return null;
                    }
                }
            }
            if (first.getTime() > second.getTime()) {
                return new SyncPatch(first, second.getName());
            } else if (first.getTime() < second.getTime()) {
                return new SyncPatch(second, first.getName());
            } else {
                throw new SynchronizationException("The same time for conflicted files");
            }
        } else { // directories
            Set<SyncPatch> syncs = new HashSet<>();
            boolean same = true;

            Set<String> remained = new HashSet<>(second.getFiles().keySet());
            for (String name : first.getFiles().keySet()) {
                if (remained.remove(name)) { // if file exist in second directory
                    MetaFile fc = first.getFiles().get(name);
                    MetaFile sc = second.getFiles().get(name);
                    // if the one is file and the other is directory
                    if (fc.isFile() && !sc.isFile() || !fc.isFile() && sc.isFile()) {
                        same = false;
                        syncs.add(new SyncPatch(getMaster(fc, sc), null));
                    } else { // both files or directories exist
                        SyncPatch si = recurSync(fc, sc, hashFunc);
                        if (si != null) {
                            syncs.add(si);
                        }
                    }
                } else { // file exist only in first directory
                    same = false;
                    syncs.add(diffSync(first, second, first.getFiles().get(name)));
                }
            }

            if (!remained.isEmpty()) {
                same = false;
                for (String name : remained) {
                    syncs.add(diffSync(second, first, second.getFiles().get(name)));
                }
            }

            if (same) {
                if (syncs.isEmpty()) {
                    return null;
                } else {
                    return new SyncPatch(first, second.getName(), syncs);
                }
            } else {
                if (first.getTime() >= second.getTime()) {
                    return new SyncPatch(first, second.getName(), syncs);
                } else {
                    return new SyncPatch(second, first.getName(), syncs);
                }
            }
        }
    }

    private static SyncPatch diffSync(MetaFile fileOwner, MetaFile other, MetaFile file)
            throws SynchronizationException {
        if (fileOwner.getTime() > other.getTime()) {
            return new SyncPatch(file, null);
        } else if (fileOwner.getTime() < other.getTime()) {
            return new SyncPatch(null, file.getName());
        } else {
            throw new SynchronizationException("The same time for conflicted directories");
        }
    }

    private static MetaFile getMaster(MetaFile first, MetaFile second) throws SynchronizationException {
        if (first.getTime() > second.getTime()) {
            return first;
        } else if (first.getTime() < second.getTime()) {
            return second;
        } else {
            throw new SynchronizationException("The same time for conflicted files");
        }
    }
}