/*
 * The MIT License
 *
 * Copyright 2013 Alexander Alexeev.
 *
 */

package org.mumidol.sync;

import java.io.IOException;

/**
 * Class acts as a bridge between file abstraction and concrete file storage implementation.
 *
 * @author Alexander Alexeev
 */
public interface Serializer {
    /**
     * Returns {@link MetaFile} tree representing file tree from storage.
     *
     * @return MetaFile tree.
     * @throws IOException
     */
    MetaFile read() throws IOException;

    /**
     * Make changes to storage using provided {@link SyncPatch}.
     * @param patch with changes.
     * @throws IOException
     */
    void patch(SyncPatch patch) throws IOException;
}
