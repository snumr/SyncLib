/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Program allows to create and apply patches to the directory.
 * 
 * @author Alexander Alexeev
 */
public class Patcher {

    public static void main(String[] args) throws IOException, SynchronizationException {
        applyPatch(args[0]);
    }

    private static void createPatch(String path) throws IOException, SynchronizationException {
        Serializer src = new FileSystemSerializer(path);
        Serializer backup = new ZipSerializer(path + ".backup.zip");
        File patch = new File(path + ".patch");

        SyncUtils.save(Synchronizer.sync(src.read(), backup.read()), new FileOutputStream(patch));
    }

    private static void applyPatch(String path)
            throws IOException, SynchronizationException {
        Serializer src = new FileSystemSerializer(path);

        applyPatch(src, new File(path + ".patch"));

        Serializer backup = new ZipSerializer(path + ".backup.zip");
        backup.patch(Synchronizer.sync(src.read(), backup.read()));
    }

    private static void applyPatch(Serializer dst, File patch) throws IOException {
        dst.patch(SyncUtils.load(new FileInputStream(patch)));
    }
}