package com.airw.framework;

public class CacheIntegerFactory implements CacheObjectFactory<Integer> {

    @Override
    public Integer createCacheObject(String s) {
        return Integer.parseInt(s);
    }

}
