/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 */

package org.mumidol.sync;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 * Date: 11.05.13
 * Time: 16:26
 */
public interface HashCalculator {
    public byte[] calculate(InputStream is) throws IOException;
}
