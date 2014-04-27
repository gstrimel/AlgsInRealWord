package com.airw.framework;

import java.util.Comparator;

import com.airw.cache.CacheObject;

/**
 * Interface for cache objects. 
 *
 * @param <T>
 *          A comparable type.
 */
public interface CacheObjectFactory<T extends CacheObject> {
    
    public T createCacheObject(String s); 
    
    public Comparator<T> getBasicComparator();

}
