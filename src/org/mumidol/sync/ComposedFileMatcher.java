/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.Collections;
import java.util.List;

/**
 * Special matcher as composition of several matchers.
 *
 * @author Alexander Alexeev
 */
public class ComposedFileMatcher implements FileMatcher {
    private List<FileMatcher> matchers;

    /**
     * Creates matcher with list of matchers to be used for file's acceptance.
     * @param matchers list of matchers.
     */
    public ComposedFileMatcher(List<FileMatcher> matchers) {
        this.matchers = Collections.unmodifiableList(matchers);
    }

    /**
     * Returns true if all matchers return true.
     * @param file to accept
     * @return <code>true</code> if all matchers return <code>true</code>, <code>false</code> otherwise.
     * @see FileMatcher#accept(MetaFile)
     */
    @Override
    public boolean accept(MetaFile file) {
        for (FileMatcher m : matchers) {
            if (!m.accept(file)) {
                return false;
            }
        }
        return true;
    }
}
