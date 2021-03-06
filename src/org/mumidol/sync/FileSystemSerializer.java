/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;


/**
 * Serializer to read/write from/to underlying file system.
 * @see Serializer
 * @author Alexander Alexeev
 */
public class FileSystemSerializer implements Serializer {
    private File root;

    public FileSystemSerializer(String root) {
        this.root = new File(root);
    }

    @Override
    public void patch(SyncPatch sync) throws IOException {
        recursWrite(sync, root);
    }

    @Override
    public MetaFile read() throws IOException {
        if (root.exists()) {
            return readMetaFile(null, root);
        } else {
            return null;
        }
    }

    private void recursWrite(SyncPatch sync, File path) throws IOException {
        if (sync == null) {
            return;
        }
        if (sync.getMaster() == null) {
            if (path.exists()) {
                recursDelete(path);
            }
        } else if (sync.getDependentName() == null) {
            if (path.exists() &&
                    (path.isFile() && !sync.getMaster().isFile() ||
                    !path.isFile() && sync.getMaster().isFile())) { // delete file with conflicted name
                recursDelete(path);
            }
            if (!path.exists()) {
                recursCopy(sync.getMaster(), path);
            }
        // file
        } else if (sync.getMaster().isFile()) {
            if (!isOwnFile(sync.getMaster())) {
                copy(sync.getMaster().getInputStream(), path, sync.getMaster().getTime());
            }
        // directory
        } else {
            if (isOwnFile(sync.getMaster()) && sync.isMasterCopy()) {
                return;
            }
            // recursive calls
            for (SyncPatch i : sync.getSyncs()) {
                recursWrite(i, new File(path, i.getMaster() != null ?
                        i.getMaster().getName() : i.getDependentName()));
            }
            path.setLastModified(sync.getMaster().getTime());
        }
    }

    private boolean isOwnFile(MetaFile file) {
        return ((file instanceof FSMetaFile) && (((FSMetaFile) file).getSerializer() == this));
    }

    private static void copy(InputStream is, File file, long time) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            FileUtils.copy(is, fos);
        } finally {
            is.close();
        }
        file.setLastModified(time);
    }

    private static void recursCopy(MetaFile master, File path) throws IOException {
        if (master.isFile()) {
            copy(master.getInputStream(), path, master.getTime());
        } else {
            path.mkdir();
            for (MetaFile f : master.getFiles().values()) {
                recursCopy(f, new File(path, f.getName()));
            }
            path.setLastModified(master.getTime());
        }
    }

    private static void recursDelete(File file) {
        File[] list = file.listFiles();
        if ((list == null) || (list.length == 0)) {
            file.delete();
        } else {
            for (File f : list) {
                recursDelete(f);
            }
        }
    }

    private FSMetaFile readMetaFile(FSMetaFile parent, File path) throws IOException {
        if (path.isFile()) {
            return new FSMetaFile(parent, path.getName(), path.length(), path.lastModified(), path.getAbsolutePath());
        } else {
            Map<String, FSMetaFile> files = new HashMap<String, FSMetaFile>();
            FSMetaFile file = new FSMetaFile(parent, path.getName(), path.lastModified(),
                    path.getAbsolutePath(), files);
            for (File cf : path.listFiles()) {
                files.put(cf.getName(), readMetaFile(file, cf));
            }
            return file;
        }
    }

    class FSMetaFile implements MetaFile {
        private FSMetaFile parent;
        private String name;
        private boolean isFile;
        private Map<String, FSMetaFile> files;
        private long size;
        private long time;
        private String path;

        FSMetaFile(FSMetaFile parent, String name, long size, long time, String path) {
            this.parent = parent;
            this.name = name;
            this.isFile = true;
            this.size = size;
            this.time = time;
            this.path = path;
        }

        FSMetaFile(FSMetaFile parent, String name, long time, String path,
                Map<String, FSMetaFile> files) {
            this.parent = parent;
            this.name = name;
            this.isFile = false;
            this.files = Collections.unmodifiableMap(files);
            this.time = time;
            this.path = path;
        }

        @Override
        public MetaFile getParent() {
            return parent;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isFile() {
            return isFile;
        }

        @Override
        public Map<String, FSMetaFile> getFiles() {
            return files;
        }

        @Override
        public byte[] getHash(String hashFunc) {
            return null;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(path);
        }

        FileSystemSerializer getSerializer() {
            return FileSystemSerializer.this;
        }
    }
}
