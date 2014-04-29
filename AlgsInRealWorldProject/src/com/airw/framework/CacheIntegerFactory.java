package com.airw.framework;

import java.util.Comparator;

public class CacheIntegerFactory implements
        CacheObjectFactory<CacheInteger> {

    @Override
    public CacheInteger createCacheObject(String s) {
        return CacheInteger.parseInt(s);
    }

    @Override
    public Comparator<CacheInteger> getBasicComparator() {
        return new Comparator<CacheInteger>() {
            public int compare(CacheInteger a, CacheInteger b) {
                return Integer.compare(a.valueOf(), b.valueOf());
            }
        };
    }
}
