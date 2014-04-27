package com.airw.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.airw.framework.CacheObjectFactory;

public class EmptyCacheArray<T extends CacheObject> extends CacheArray<T> {

    private String baseFileName;
    private long size;

    public EmptyCacheArray(CacheObjectFactory<T> objectFact, long size,
            LRUCCache cache) throws IOException {
        super(objectFact, cache);
        baseFileName = "EMPTY_" + getId();
        this.size = size;
        createAllSubFiles();
    }

    @Override
    protected void createAllSubFiles() throws IOException {
        long numBlocksPerSubFile = cache.numBlocksPerSubFile();
        long blockSize = cache.getBlockSize();
        for (long i = 0; i < size; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = baseFileName + "_"
                     + (i / (numBlocksPerSubFile * blockSize)) + ".txt";
            FileWriter fw = new FileWriter(subFileName);
            BufferedWriter bw = new BufferedWriter(fw);
            for (long j = i; j < Math.min(size, i
                    + (numBlocksPerSubFile * blockSize)); j++) {
                bw.write("0");
                bw.newLine();
            }
            bw.close();
        }
    }

    @Override
    public void close() throws IOException {
        cache.dump(this);
        long numBlocksPerSubFile = cache.numBlocksPerSubFile();
        long blockSize = cache.getBlockSize();
        for (long i = 0; i < size; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = baseFileName + "_"
                    + (i / (numBlocksPerSubFile * blockSize)) + ".txt";
            File f = new File(subFileName);
            f.delete();
        }
        cache.removeCacheArray(this);
    }

    @Override
    public String getBaseFileName() {
        return baseFileName;
    }

    @Override
    public long size() {
        return size;
    }

}
