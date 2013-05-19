/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * File abstraction. File can be a directory then it contains set of files (set can be empty) or
 * can be an ordinary file then it contains some content.
 * <p>
 * <code>MetaFile</code> instance can be obtained from {@link Serializer#read()} method call.
 * 
 * @author Alexander Alexeev
 */
public interface MetaFile {
    /**
     * Returns the parent file.
     * @return parent file or <code>null</code> if there is no parent.
     */
    MetaFile getParent();

    /**
     * Returns the name of this file.
     * 
     * @return name of this file
     */
    String getName();

    /**
     * Returns <code>true</code> if file is an ordinary file.
     * @return <code>true</code> if file is an ordinary file, <code>false</code> otherwise.
     */
    boolean isFile();

    /**
     * Returns files in this directory.
     * @return files in this directory or <code>null</code> if this is not directory.
     */
    Map<String, ? extends MetaFile> getFiles();

    /**
     * Returns hash sum of this file content.
     * @param hashFunc type of hash function to be used
     * @return hash sum or <tt>null</tt> if file is a directory or hash function isn't supported
     */
    byte[] getHash(String hashFunc);

    /**
     * Returns size of this file.
     * @return size of this file or 0 if file is a directory.
     */
    long getSize();

    /**
     * Returns last modification time.
     * @return last modification time.
     */
    long getTime();

    /**
     * Returns input stream to read file content from.
     * @return content input stream or <code>null</code> if file is a directory.
     * @throws IOException 
     */
    InputStream getInputStream() throws IOException;
}
