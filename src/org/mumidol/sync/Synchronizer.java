/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides methods to synchronize {@link MetaFile}.
 * 
 * @author Alexander Alexeev
 */
public class Synchronizer {

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
    public static SyncPatch sync(MetaFile first, MetaFile second, FileMatcher matcher)
            throws NullPointerException, SynchronizationException {
        if ((first == null) && (second == null)) {
            throw new NullPointerException();
        }
        if (first == null) {
            return new SyncPatch(second, null);
        }
        if (second == null) {
            return new SyncPatch(first, null);
        }

        FileSieve sieve = new FileSieve(matcher);
        if (matcher != null) {
            first = sieve.sift(first);
            second = sieve.sift(second);
        }
        return recurSync(first, second);
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
            throws NullPointerException, SynchronizationException {
        return sync(first, second, null);
    }

//  private stuff
//  =============================================================================================
    private static SyncPatch recurSync(MetaFile first, MetaFile second)
            throws SynchronizationException {
        if (first.isFile()) {
            if ((first.getSize() != second.getSize()) || (first.getCrc() != second.getCrc())) {
                if (first.getTime() > second.getTime()) {
                    return new SyncPatch(first, second.getName());
                } else if (first.getTime() < second.getTime()) {
                    return new SyncPatch(second, first.getName());
                } else {
                    throw new SynchronizationException("The same time for conflicted files");
                }
            } else {
                return null;
            }
        } else {
            Set<SyncPatch> syncs = new HashSet<SyncPatch>();

            Set<String> same = new HashSet<String>(first.getFiles().keySet());
            same.retainAll(second.getFiles().keySet());
            for (String name : same) {
                SyncPatch si = recurSync(first.getFiles().get(name), second.getFiles().get(name));
                if (si != null) {
                    syncs.add(si);
                }
            }
            // if the directories' file's lists are the same
            if ((first.getFiles().size() == second.getFiles().size()) && 
                    (same.size() == first.getFiles().size())) {
                if (syncs.isEmpty()) {
                    return null;
                } else {
                    return new SyncPatch(first, second.getName(), syncs);
                }
            } else {
                if (first.getTime() > second.getTime()) {
                    syncs.addAll(syncDiff(first, second));
                    return new SyncPatch(first, second.getName(), syncs);
                } else if (first.getTime() < second.getTime()) {
                    syncs.addAll(syncDiff(second, first));
                    return new SyncPatch(second, first.getName(), syncs);
                } else {
                    throw new SynchronizationException("The same time for conflicted directories");
                }
            }
        }
    }

    private static Set<SyncPatch> syncDiff(MetaFile master, MetaFile copy) {
        Set<SyncPatch> syncs = new HashSet<SyncPatch>();

        Set<String> added = new HashSet<String>(master.getFiles().keySet());
        added.removeAll(copy.getFiles().keySet());
        for (String f : added) {
            syncs.add(new SyncPatch(master.getFiles().get(f), null));
        }

        Set<String> removed = new HashSet<String>(copy.getFiles().keySet());
        removed.removeAll(master.getFiles().keySet());
        for (String f : removed) {
            syncs.add(new SyncPatch(null, f));
        }

        return syncs;
    }
}