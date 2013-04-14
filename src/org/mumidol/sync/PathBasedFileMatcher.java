/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

/**
 *
 * @author Alexander Alexeev
 */
public abstract class PathBasedFileMatcher implements FileMatcher {

    protected static String getPath(MetaFile file) {
        StringBuilder sb = getPath(new StringBuilder(), file);
        if (!file.isFile()) {
            sb.append('/');
        }
        return sb.toString();
    }

    private static StringBuilder getPath(StringBuilder sb, MetaFile file) {
        if (file.getParent() != null) {
            getPath(sb, file.getParent()).append('/').append(file.getName());
        }
        return sb;
    }
}
