package com.airw.sorts;

import java.io.IOException;

import com.airw.cache.LRUCache;
import com.airw.framework.CacheObjectFactory;

/**
 * The sort interface. The implementor must implement sortCore only.
 * 
 * @param <T>
 *            A comparable.
 */
public abstract class Sort<T extends Comparable<T>> {

    protected LRUCache<T> cache;

    public Sort(String fileName, CacheObjectFactory<T> cof, int blockSize,
            final int numBlocksInCache, double extraSpaceMultiplier)
            throws IOException {
        cache = new LRUCache<T>(fileName, cof, blockSize, numBlocksInCache,
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
