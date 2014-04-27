package com.airw.cache;

public class ArrayIdMaker {
    
    static int id = -1;

    public static synchronized int nextID() {
        id++;
        return id;
    }

}
