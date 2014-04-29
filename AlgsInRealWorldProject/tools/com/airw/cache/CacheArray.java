package com.airw.cache;

import java.io.IOException;

import com.airw.arrays.ArrayIdMaker;
import com.airw.framework.CacheObjectFactory;

public abstract class CacheArray<T extends CacheObject> {

    private CacheObjectFactory<T> objectFact;
    protected LRUCache cache;
    private int arrayId;

    public CacheArray(CacheObjectFactory<T> objectFact, LRUCache cache)
            throws IOException {
        this.objectFact = objectFact;
        this.cache = cache;
        this.arrayId = ArrayIdMaker.nextID();
        cache.addCacheArray(this);
    }

    protected abstract void createAllSubFiles() throws IOException;

    public abstract void close() throws IOException;

    /**
     * Gets an element at a given index.
     * 
     * @param index
     *            The element index to access.
     * @return The element at this index.
     * @throws IOException
     */
    public T get(long index) throws IOException {
        if (index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }

        return objectFact.createCacheObject(cache.get(index, this));
    }

    public void set(long index, T v) throws IOException {
        if (index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }

        cache.set(index, v.toString(), this);
    }

    public int getId() {
        return arrayId;
    }
    
    public CacheObjectFactory<T> getFactory() {
    	return objectFact;
    }

    public abstract long size();

    public abstract String getBaseFileName();

    public LRUCache getCache() {
        return cache;
    }
}
