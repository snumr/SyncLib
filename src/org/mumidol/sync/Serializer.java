/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;

/**
 *
 * @author Alexander Alexeev
 */
public interface Serializer {
    MetaFile read() throws IOException;
    void patch(SyncPatch patch) throws IOException;
}
