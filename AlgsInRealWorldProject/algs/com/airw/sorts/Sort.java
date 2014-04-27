package com.airw.sorts;

import java.io.IOException;
import java.util.Comparator;

import com.airw.cache.CacheArray;
import com.airw.cache.CacheObject;


/**
 * The sort interface. The implementor must implement sortCore only.
 * @param <T>
 * 
 * @param <T>
 *            A comparable.
 */
public abstract class Sort<T extends CacheObject> {

    protected CacheArray<T> array;
    protected Comparator<T> comp;
    
    public Sort(CacheArray<T> array, Comparator<T> comp){
        this.array = array;
        this.comp = comp;
    }
    
    public abstract void sort() throws IOException;

}
