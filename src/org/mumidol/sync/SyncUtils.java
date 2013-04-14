/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class allows to save/load patches.
 * 
 * @author Alexander Alexeev
 */
public class SyncUtils {
    /**
     * Saves {@link SyncPatch} to the OutputStream.
     * 
     * @param patch changes to be saved.
     * @param os - output stream changes to be save to.
     * @throws IOException 
     * @see SyncPatch
     */
    public static void save(SyncPatch patch, OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        try {
            writeSync(patch, dos);
        } finally {
            dos.close();
        }
    }

    /**
     * Loads {@link SyncPatch} from the InputStream.
     * @param is - input stream changes to be loaded from.
     * @return loaded patch.
     * @throws IOException 
     * @see SyncPatch
     */
    public static SyncPatch load(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        try {
            return readSync(dis);
        } finally {
            dis.close();
        }
    }

//  private stuff 
//  ==========================================================================================
    private static void writeSync(SyncPatch sync, DataOutputStream dos) throws IOException {
        dos.writeBoolean(sync.getDependentName() != null);
        if (sync.getDependentName() != null) {
            dos.writeUTF(sync.getDependentName());
            SyncMetaFile.writeMasterFile(sync.getMaster(), dos, false);
        } else {
            SyncMetaFile.writeMasterFile(sync.getMaster(), dos, true);
        }
        writeSyncs(sync.getSyncs(), dos);
    }

    private static void writeSyncs(Set<SyncPatch> syncs, DataOutputStream dos) throws IOException {
        if (syncs != null) {
            dos.writeInt(syncs.size());
            for (SyncPatch i : syncs) {
                writeSync(i, dos);
            }
        } else {
            dos.writeInt(0);
        }
    }

    private static SyncPatch readSync(DataInputStream dis) throws IOException {
        MetaFile master;
        String depName = null;
        if (dis.readBoolean()) {
            depName = dis.readUTF();
            master = SyncMetaFile.readMasterFile(dis, false);
        } else {
            master = SyncMetaFile.readMasterFile(dis, true);
        }

        Set<SyncPatch> syncs = null;
        int count = dis.readInt();
        if (count != 0) {
            syncs = readSyncs(count, dis);
        }
        
        return new SyncPatch(master, depName, syncs);
    }
    
    private static Set<SyncPatch> readSyncs(int count, DataInputStream dis) throws IOException {
        Set<SyncPatch> syncs = new HashSet<SyncPatch>(count);
        for (int i = 0; i < count; i++) {
            syncs.add(readSync(dis));
        }
        return syncs;
    }
}

class SyncMetaFile implements MetaFile {
    private String name;
    private boolean isFile;
    private Map<String, SyncMetaFile> files;
    private long size;
    private long time;
    private byte[] content;

    private SyncMetaFile(String name, boolean isFile, Map<String,
            SyncMetaFile> files, long size, long time, byte[] content) {
        this.name = name;
        this.isFile = isFile;
        this.files = files;
        this.size = size;
        this.time = time;
        this.content = content;
    }

    @Override
    public MetaFile getParent() {
        throw new UnsupportedOperationException();
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
    public Map<String, SyncMetaFile> getFiles() {
        return files;
    }

    @Override
    public long getCrc() {
        throw new UnsupportedOperationException();
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
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    static void writeMasterFile(MetaFile master, DataOutputStream dos, boolean tree)
            throws IOException {
        dos.writeBoolean(master != null);
        if (master != null) {
            dos.writeUTF(master.getName());
            dos.writeBoolean(master.isFile());
            dos.writeLong(master.getTime());
            if (master.isFile()) {
                dos.writeLong(master.getSize());
                FileUtils.copy(master.getInputStream(), dos);
            } else if (tree) {
                dos.writeInt(master.getFiles().size());
                for (MetaFile f : master.getFiles().values()) {
                    writeMasterFile(f, dos, tree);
                }
            }
        }
    }

    static SyncMetaFile readMasterFile(DataInputStream dis, boolean tree)
            throws IOException {
        if (dis.readBoolean()) {
            String name = dis.readUTF();
            boolean isFile = dis.readBoolean();
            long time = dis.readLong();
            long size = 0;
            byte[] content = null;
            Map<String, SyncMetaFile> files = null;
            if (isFile) {
                size = dis.readLong();
                content = new byte[(int)size];
                dis.read(content);
            } else if (tree) {
                int count = dis.readInt();
                files = new HashMap<String, SyncMetaFile>(count);
                for (int i = 0; i < count; i++) {
                    SyncMetaFile f = readMasterFile(dis, tree);
                    files.put(f.getName(), f);
                }
            }
            return new SyncMetaFile(name, isFile, files, size, time, content);
        } else {
            return null;
        }
    }    
}
