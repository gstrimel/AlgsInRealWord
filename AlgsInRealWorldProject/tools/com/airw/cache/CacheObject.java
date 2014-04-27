package com.airw.cache;


public abstract class CacheObject {

    public abstract String myToString();

    @Override
    public String toString() {
        return myToString();
    }

}
