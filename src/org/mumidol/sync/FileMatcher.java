/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

/**
 * Interface to be used by {@link FileSieve} to accept or decline file.
 *
 * @author Alexander Alexeev
 */
public interface FileMatcher {

    /**
     * Returns true if file is accepted.
     * @param file to accept
     * @return <code>true</code> if file is accepted, <code>false</code> otherwise.
     */
    boolean accept(MetaFile file);
    
}
