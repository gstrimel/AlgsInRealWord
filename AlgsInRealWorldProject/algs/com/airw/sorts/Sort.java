package com.airw.sorts;

import java.io.IOException;

import com.airw.cache.LRUCache;

/**
 * The sort interface. The implementor must implement sortCore only.
 * 
 * @param <T>
 *            A comparable.
 */
public abstract class Sort {

    protected LRUCache cache;

    public Sort(String fileName, int blockSize, final int numBlocksInCache,
            double extraSpaceMultiplier) throws IOException {
        cache = new LRUCache(fileName, blockSize, numBlocksInCache,
                extraSpaceMultiplier);
    }

    protected abstract void sortCore() throws IOException;

    public void sort() throws IOException {
        sortCore();
        close();
    }

    private void close() throws IOException {
        cache.close();
    }

    public long numElems() {
        return cache.fileSize();
    }

}
