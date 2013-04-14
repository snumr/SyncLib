/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple program to sync files between directories/zip archives.
 * Accepts following parameters:
 *  -c - copies from source to destination without source modification
 *  -s - synchronizes source and destination directories
 *  -i - followed by list of files to be included to synchronization
 *  -e - followed by list of files to be excluded from synchronization
 *
 * @author Alexander Alexeev
 */
public class Sync {
    private static boolean syncSource;
    private static Serializer src;
    private static Serializer dst;
    private static List<String> includes;
    private static List<String> excludes;

    public static void main(String[] args) throws IOException, SynchronizationException {
        parseArgs(args);

        FileMatcher matcher = null;
        if (includes != null) {
            List<FileMatcher> matchers = new ArrayList<FileMatcher>(includes.size());
            for (String include : includes) {
                matchers.add(new FilePatternMatcher(include, true));
            }
            matcher = new ComposedFileMatcher(matchers);
        }
        if (excludes != null) {
            List<FileMatcher> matchers = new ArrayList<FileMatcher>(excludes.size());
            for (String exclude : excludes) {
                matchers.add(new FilePatternMatcher(exclude, false));
            }
            if (matcher != null) {
                matcher = new ComposedFileMatcher(
                        Arrays.asList(matcher, new ComposedFileMatcher(matchers)));
            } else {
                matcher = new ComposedFileMatcher(matchers);
            }
        }
        SyncPatch sync = Synchronizer.sync(src.read(), dst.read(), matcher);
        if (syncSource) {
            src.patch(sync);
        }
        dst.patch(sync);
    }

    private static void parseArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Source and destination must be provided");
        }
        for (int i = 0; i < args.length - 2; ) {
            if ("-c".equals(args[i])) {
                syncSource = false;
                i += 1;
            } else if ("-s".equals(args[i])) {
                syncSource = true;
                i += 1;
            } else if ("-i".equals(args[i])) {
                includes = new ArrayList<String>();
                i = fillList(args, i + 1, includes);
            } else if ("-e".equals(args[i])) {
                excludes = new ArrayList<String>();
                i = fillList(args, i + 1, excludes);
            } else {
                throw new IllegalArgumentException("Unknown parameter: " + args[i]);
            }
        }
        src = createSerializer(args[args.length - 2]);
        dst = createSerializer(args[args.length - 1]);
    }

    private static Serializer createSerializer(String path) {
        if (path.endsWith(".zip") && new File(path).isFile()) {
            return new ZipSerializer(path);
        } else {
            return new FileSystemSerializer(path);
        }
    }

    private static int fillList(String[] args, int i, List<String> list) {
        while (!args[i].startsWith("-") && (i < args.length - 2)) {
            list.add(args[i++]);
        }
        return i;
    }
}
