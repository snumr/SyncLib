/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

/**
 *
 * @author Alexander Alexeev
 */
public interface FileMatcher {

    boolean accept(MetaFile file);
    
}
