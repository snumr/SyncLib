/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Sieve sifts {@link MetaFile} to return <code>MetaFile</code> tree with files accepted by 
 * {@link FileMatcher}.
 * 
 * @author Alexander Alexeev
 */
class FileSieve {
    private FileMatcher matcher;

    /**
     * Creates file sieve that will use provided <code>FileMatcher</code> to sift files.
     * @param matcher to be used to accept files.
     */
    public FileSieve(FileMatcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Sifts <code>MetaFile</code> tree.
     * @param file to be sifted
     * @return sifted tree.
     */
    public MetaFile sift(MetaFile file) {
        return new SieveMetaFile(file);
    }

    class SieveMetaFile implements MetaFile {
        SieveMetaFile parent;
        MetaFile original;
        Map<String, SieveMetaFile> files;

        SieveMetaFile(MetaFile root) {
            this.original = root;
        }

        private SieveMetaFile(SieveMetaFile parent, MetaFile file) {
            this.parent = parent;
            this.original = file;
        }

        @Override
        public MetaFile getParent() {
            return parent;
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public boolean isFile() {
            return original.isFile();
        }

        @Override
        public Map<String, ? extends MetaFile> getFiles() {
            if (files == null) {
                if (original.getFiles() != null) {
                    files = new HashMap<>();
                    for (MetaFile f : original.getFiles().values()) {
                        if (matcher.accept(f)) {
                            files.put(f.getName(), new SieveMetaFile(this, f));
                        }
                    }
                }
            }
            return files;
        }

        @Override
        public long getCrc() {
            return original.getCrc();
        }

        @Override
        public long getSize() {
            return original.getSize();
        }

        @Override
        public long getTime() {
            return original.getTime();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return original.getInputStream();
        }

    }
}
