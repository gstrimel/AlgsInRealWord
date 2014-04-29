package com.airw.framework;

import com.airw.cache.CacheObject;

public class CacheInteger extends CacheObject {

    private Integer n;

    public CacheInteger(Integer n) {
        this.n = n;
    }

    public int valueOf() {
        return n.intValue();
    }

    public static CacheInteger parseInt(String s) {
        return new CacheInteger(Integer.parseInt(s));
    }

    public String toString() {
        return n.toString();
    }

    @Override
    public String myToString() {
        return n.toString();
    }

}
