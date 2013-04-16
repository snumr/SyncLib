/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

/**
 * Signals that some conflict occurs during file's synchronization.
 *
 * @author Alexander Alexeev
 */
class SynchronizationException extends Exception {
    SynchronizationException(String message) {
        super(message);
    }
}
