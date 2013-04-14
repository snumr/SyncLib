/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Alexander Alexeev
 */
public class ZipSerializer implements Serializer {
    private File zip;
    private ZipFile zipFile;
    private ZipMetaFile root;

    public ZipSerializer(String zip) {
        this.zip = new File(zip);
    }

    @Override
    public MetaFile read() throws IOException {
        if (zip.exists()) {
            return constructTree(getZipFile().entries());
        } else {
            return null;
        }
    }
    
    @Override
    public void patch(SyncPatch sync) throws IOException {
        if ((sync == null) || (sync.getMaster() == root) && sync.isMasterCopy()) {
            return;
        }

        if (root == null) {
            root = new ZipMetaFile();
        }

        File tempFile = File.createTempFile("sync", ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
        write0(root, sync, "", out);
        out.finish();
        out.close();

        if (zip.exists()) {
            zipFile.close();
            if (!zip.delete()) {
                throw new IOException("Couldn't delete zip file: " + zip);
            }
        }
        if (!tempFile.renameTo(zip)) {
            throw new IOException("Couldn't rename temp file to: " + zip);
        }
    }

    private ZipFile getZipFile() throws IOException {
        if (zipFile == null) {
            zipFile = new ZipFile(zip);
        }
        return zipFile;
    }

    private MetaFile constructTree(Enumeration<? extends ZipEntry> entries) {
        root = new ZipMetaFile();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            constructNode(entry);
        }

        return root;
    }

    private void constructNode(ZipEntry entry) {
        String path = entry.getName();
        ZipMetaFile node = root;

        if (path.isEmpty()) {
            return;
        }

        int i = 0;
        int j = getNextInd(i, path);
        while (j < path.length() - 1) {
            String name = path.substring(i, j);
            if (node.getFiles().get(name) == null) {
                node.add(new ZipMetaFile(node, path.substring(0, j + 1)));
            }
            node = node.getFiles().get(name);
            i = j + 1;
            j = getNextInd(i, path);
        }
        node.add(new ZipMetaFile(node, entry));
    }

    private static int getNextInd(int s, String path) {
        int i = path.indexOf('/', s);
        if (i == -1) {
            i = path.length();
        }
        return i;
    }

    private static String getPath(String prefix, String name) {
        return prefix.isEmpty() ? name : prefix + "/" + name;
    }

    private static void write0(MetaFile file, SyncPatch sync, String path, ZipOutputStream out)
            throws IOException {
        if (sync.getMaster() == null) {
            // skips deleted files
        } else if (sync.getDependentName() == null) {
            // adds new files even if master
            addFile(sync.getMaster(), path, out);
        } else if (sync.getMaster().isFile()) {
            addFile(sync.getMaster(), path, out);
        } else {
            Set<SyncPatch> syncs = new HashSet(sync.getSyncs());
            for (MetaFile f : file.getFiles().values()) {
                boolean found = false;
                for (Iterator<SyncPatch> i = syncs.iterator(); i.hasNext(); ) {
                    SyncPatch s = i.next();
                    if ((s.getMaster() != null) && f.getName().equals(s.getMaster().getName()) ||
                            f.getName().equals(s.getDependentName())) {
                        write0(f, s, getPath(path, f.getName()), out);
                        i.remove();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addFile(f, getPath(path, f.getName()), out);
                }
            }
            for (SyncPatch s : syncs) {
                write0(null, s, getPath(path, s.getMaster().getName()), out);
            }
        }
    }

    private static void addFile(MetaFile file, String path, ZipOutputStream out) throws IOException {
        if (file.isFile()) {
            ZipEntry entry = new ZipEntry(path);
            entry.setTime(file.getTime());
            out.putNextEntry(entry);
            InputStream is = file.getInputStream();
            try {
                FileUtils.copy(is, out);
            } finally {
                is.close();
            }
        } else {
            if (file.getFiles().isEmpty()) {
                ZipEntry entry = new ZipEntry(path + "/");
                entry.setTime(file.getTime());
                out.putNextEntry(entry);
            }
            for (MetaFile f : file.getFiles().values()) {
                addFile(f, getPath(path, f.getName()), out);
            }
        }
    }

    class ZipMetaFile implements MetaFile {
        private ZipMetaFile parent;
        private String path;
        private String name;
        private boolean isDir;
        private Map<String, ZipMetaFile> files;
        private long crc;
        private long size;
        private long time;

        ZipMetaFile() {
            this.path = "";
            this.name = "";
            this.isDir = true;
            files = new HashMap<String, ZipMetaFile>();
        }

        ZipMetaFile(ZipMetaFile parent, String path) {
            this.parent = parent;
            this.path = path;
            extractName(path);
            this.isDir = true;//path.endsWith("/");
            if (isDir) {
                files = new HashMap<String, ZipMetaFile>();
            }
        }

        ZipMetaFile(ZipMetaFile parent, ZipEntry entry) {
            this.parent = parent;
            this.path = entry.getName();
            extractName(path);
            this.isDir = entry.isDirectory();
            this.crc = entry.getCrc();
            this.size = entry.getSize();
            this.time = entry.getTime();
            if (isDir) {
                files = new HashMap<String, ZipMetaFile>();
            }
        }

        private void extractName(String path) {
            int j = path.endsWith("/") ? path.length() - 1 : path.length();
            int i = path.lastIndexOf('/', j - 1);
            this.name = path.substring(i + 1, j);
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
            return !isDir;
        }

        @Override
        public Map<String, ZipMetaFile> getFiles() {
            return files;
        }

        @Override
        public long getCrc() {
            return crc;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public long getTime() {
            if (time > 0) {
                return time;
            } else {
                return zip.lastModified();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return getZipFile().getInputStream(getZipFile().getEntry(path));
        }

        void add(ZipMetaFile file) {
            files.put(file.getName(), file);
        }
    }
}
