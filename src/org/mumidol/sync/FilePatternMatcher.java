/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.util.regex.Pattern;

/**
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
        if (!file.isFile()) {
            for (MetaFile f : file.getFiles().values()) {
                if (accept(f)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
