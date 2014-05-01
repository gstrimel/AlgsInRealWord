package com.airw.sorts;

import java.io.IOException;
import java.util.Comparator;

import com.airw.cache.CacheArray;
import com.airw.cache.CacheObject;

public class SelectionSort<T extends CacheObject> extends Sort<T> {

    public SelectionSort(CacheArray<T> array, Comparator<T> comp) {
        super(array, comp);
    }

    @Override
    public void sort() throws IOException {
        for (long i = 0; i < array.size(); i++) {
            T ithObj = array.get(i);
            T minObj = array.get(i);
            long minIndx = i;
            for (long j = i + 1; j < array.size(); j++) {
                T pot = array.get(j);
                if (comp.compare(minObj, pot) > 0) {
                    minObj = pot;
                    minIndx = j;
                }
            }
            array.set(minIndx, ithObj);
            array.set(i, minObj);
        }
    }

}
