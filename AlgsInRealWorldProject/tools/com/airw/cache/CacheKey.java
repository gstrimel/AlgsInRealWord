package com.airw.cache;

public class CacheKey {

    private int id;
    private long blockNumber;

    public CacheKey(int arrayId, long blockNumber) {
        id = arrayId;
        this.blockNumber = blockNumber;
    }

    public int getId() {
        return id;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CacheKey) {
            return ((id == ((CacheKey) o).getId()) && (blockNumber == ((CacheKey) o)
                    .getBlockNumber()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        String comp = id + "," + blockNumber;
        return comp.hashCode();
    }

}
