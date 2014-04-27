package com.airw.tools;

import com.airw.cache.CacheObject;

public class IntegerFileObject extends CacheObject {

    private Integer n;

    public IntegerFileObject(Integer n) {
        this.n = n;
    }

    public int valueOf() {
        return n.intValue();
    }

    public static IntegerFileObject parseInt(String s) {
        return new IntegerFileObject(Integer.parseInt(s));
    }

    public String toString() {
        return n.toString();
    }

    @Override
    public String myToString() {
        return n.toString();
    }

}
