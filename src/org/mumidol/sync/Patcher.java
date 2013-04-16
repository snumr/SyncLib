/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import com.sun.javaws.CacheUpdateHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Program allows to create and apply patches to the directory.
 * 
 * @author Alexander Alexeev
 */
public class Patcher {
    private static boolean create;
    private static boolean apply;
    private static Serializer src;
    private static Serializer backup;
    private static File patch;
    private static List<String> includes;
    private static List<String> excludes;

    private Patcher() {}

    public static void main(String[] args) throws IOException, SynchronizationException {
        parseArgs(args);

        FileMatcher matcher = null;
        if (includes != null) {
            List<FileMatcher> matchers = new ArrayList<>(includes.size());
            for (String include : includes) {
                matchers.add(new FilePatternMatcher(include, true));
            }
            matcher = new ComposedFileMatcher(matchers);
        }
        if (excludes != null) {
            List<FileMatcher> matchers = new ArrayList<>(excludes.size());
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

        if (create) {
            createPatch(src, backup, patch, matcher);
        } else if (apply) {
            applyPatch(src, backup, patch, matcher);
        }
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length - 1; ) {
            switch (args[i]) {
                case "-c":
                    create = true;
                    i += 1;
                    break;
                case "-a":
                    apply = true;
                    i += 1;
                    break;
                case "-b":
                    backup = createSerializer(args[i + 1]);
                    i += 2;
                    break;
                case "-p":
                    patch = new File(args[i + 1]);
                    i += 2;
                    break;
                case "-i":
                    includes = new ArrayList<>();
                    i = fillList(args, i + 1, includes);
                    break;
                case "-e":
                    excludes = new ArrayList<>();
                    i = fillList(args, i + 1, excludes);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parameter: " + args[i]);
            }
        }
        if (create && apply) {
            throw new IllegalArgumentException("Contradicted parameters: -c and -a");
        }
        if (!create && !apply) {
            throw new IllegalArgumentException("Provide -a or -c parameter");
        }
        src = createSerializer(args[args.length - 1]);
        if (backup == null) {
            backup = createSerializer(args[args.length - 1] + ".backup");
        }
        if (patch == null) {
            patch = new File(args[args.length - 1] + ".patch");
        }
    }

    private static int fillList(String[] args, int i, List<String> list) {
        while (!args[i].startsWith("-") && (i < args.length - 1)) {
            list.add(args[i++]);
        }
        return i;
    }

    private static Serializer createSerializer(String path) {
        if (path.endsWith(".zip") && new File(path).isFile()) {
            return new ZipSerializer(path);
        } else {
            return new FileSystemSerializer(path);
        }
    }

    private static void createPatch(Serializer src, Serializer backup, File patch, FileMatcher matcher)
            throws IOException, SynchronizationException {
        SyncPatch sync = Synchronizer.sync(src.read(), backup.read(), matcher);
        if (sync != null) {
            SyncPatch.save(sync, new FileOutputStream(patch));
        } else {
            System.out.println("There are no differences");
        }
    }

    private static void applyPatch(Serializer src, Serializer backup, File patch, FileMatcher matcher)
            throws IOException, SynchronizationException {
        backup.patch(SyncPatch.load(new FileInputStream(patch)));

        src.patch(Synchronizer.sync(src.read(), backup.read(), matcher));
    }
}