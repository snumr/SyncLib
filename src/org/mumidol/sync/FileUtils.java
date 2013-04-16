/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class with useful file's methods.
 *
 * @author Alexander Alexeev
 */
public class FileUtils {
    /**
     * Copies from input to output stream without closing streams.
     *
     * @param is input stream
     * @param os output stream
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int i = is.read(buf);
        while (i != -1) {
            os.write(buf, 0, i);
            i = is.read(buf);
        }
    }
}
