/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.List;

/**
 *
 * @author Alexander Alexeev
 */
public class ComposedFileMatcher implements FileMatcher {
    private List<FileMatcher> matchers;

    public ComposedFileMatcher(List<FileMatcher> matchers) {
        this.matchers = matchers;
    }

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
