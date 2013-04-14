/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.Set;


/**
 * Synchronization path contains information about changes between two {@link MetaFile}.
 *
 * @author Alexander Alexeev
 */
public class SyncPatch {
    private MetaFile master;
    private String depName;
    private Set<SyncPatch> syncs;
    private boolean masterCopy;

    SyncPatch(MetaFile master, String depName) {
        this.master = master;
        this.depName = depName;
    }

    SyncPatch(MetaFile master, String depName, Set<SyncPatch> syncs) {
        this.master = master;
        this.depName = depName;
        this.syncs = syncs;
    }

    /**
     * Returns {@link MetaFile} which content to be used as master copy.
     * @return master file or <code>null</code> if file was deleted.
     */
    public MetaFile getMaster() {
        return master;
    }

    /**
     * Returns name of the dependent file which content to be replaced.
     * @return dependent file's name or <code>null</code> if file doesn't exist.
     */
    public String getDependentName() {
        return depName;
    }

    /**
     * Returns underlying <code>SyncPathes</code> if the master file is a directory and
     * there are some changes within.
     * @return set of underlying changes.
     */
    public Set<SyncPatch> getSyncs() {
        return syncs;
    }

    /**
     * Returns true if master files of all underlying changes are descendants of master file.
     * @return <code>true</code> if all changes are located in the master file hierarchy,
     * <code>false</code> otherwise.
     */
    public boolean isMasterCopy() {
        return masterCopy;
    }
}
