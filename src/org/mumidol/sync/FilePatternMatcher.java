/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.regex.Pattern;

/**
 * Pattern based matcher. Patterns are used for the inclusion and exclusion of files and are the same as
 * Patterns in ANT. See <a href=http://ant.apache.org/manual/dirtasks.html#patterns>
 *     http://ant.apache.org/manual/dirtasks.html#patterns</a> for more information.
 *
 * @author Alexander Alexeev
 */
public class FilePatternMatcher extends PathBasedFileMatcher {
    private Pattern pattern;
    private boolean include;

    public FilePatternMatcher(String mask, boolean include) {
        if (!mask.startsWith("/")) {
            mask = "/" + mask;
        }
        if (mask.endsWith("/")) {
            mask = mask + "**";
        }
        String regexp = mask.
                    replace(".", "\\.").
                    replace("?", ".").
                    replace("/**/", "(/|/.+/)").
                    replace("**", "++"). // temporary change to + to avoid mixing with single *
                    replace("*", "[^/]*").
                    replace("++", ".*");
        this.pattern = Pattern.compile(regexp);
        this.include = include;
    }

    @Override
    public boolean accept(MetaFile file) {
        if (pattern.matcher(getPath(file)).matches()) {
            return include;
        }
        if (!include) {   // always accept file if doesn't match exclude pattern
            return true;
        }
        if (!file.isFile()) { // if one of sub-files matches then accept file
            for (MetaFile f : file.getFiles().values()) {
                if (accept(f)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
