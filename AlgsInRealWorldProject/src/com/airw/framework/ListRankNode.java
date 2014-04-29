package com.airw.framework;

import com.airw.cache.CacheObject;

public class ListRankNode extends CacheObject {

    public int id;
    public int next;
    public int weight;
    public int rank;
    public int next_next;
    public boolean flag;
    public int address;

    public ListRankNode(int id, int next, int weight, int rank, int next_next, boolean flag) {
        this.id = id;
        this.next = next;
        this.weight = weight;
        this.rank = rank;
        this.next_next = next_next;
        this.flag = flag;
    }

    @Override
    public String myToString() {
        return id + " " + next + " " + weight + " " + rank + " " + next_next + " " + flag;
    }
}
