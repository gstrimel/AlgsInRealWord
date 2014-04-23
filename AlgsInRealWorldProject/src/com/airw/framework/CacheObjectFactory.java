package com.airw.framework;

/**
 * Interface for cache objects. 
 *
 * @param <T>
 *          A comparable type.
 */
public interface CacheObjectFactory<T extends Comparable<T>> {
    
    public T createCacheObject(String s); 

}
