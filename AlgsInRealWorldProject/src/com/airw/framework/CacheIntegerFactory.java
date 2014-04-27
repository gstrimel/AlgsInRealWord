package com.airw.framework;

import java.util.Comparator;

import com.airw.tools.IntegerFileObject;

public class CacheIntegerFactory implements
        CacheObjectFactory<IntegerFileObject> {

    @Override
    public IntegerFileObject createCacheObject(String s) {
        return IntegerFileObject.parseInt(s);
    }

    @Override
    public Comparator<IntegerFileObject> getBasicComparator() {
        return new Comparator<IntegerFileObject>() {
            public int compare(IntegerFileObject a, IntegerFileObject b) {
                return Integer.compare(a.valueOf(), b.valueOf());
            }
        };
    }
}
