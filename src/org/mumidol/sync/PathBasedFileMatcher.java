/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

/**
 * Matcher that extends this file matcher bases acceptance on file path.
 *
 * @author Alexander Alexeev
 */
public abstract class PathBasedFileMatcher implements FileMatcher {

    /**
     * Returns file path, separated by /. Any path begins with root /.
     * Directory path ends with /.
     * For example: /src/org/, /src/org/mumidol/sync/MetaFile.java
     * @param file for which path will be returned
     * @return file path
     */
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
