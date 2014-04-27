package com.airw.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

import com.airw.framework.CacheObjectFactory;

public class FileCacheArray<T extends CacheObject> extends CacheArray<T> {

    private String baseFileName;
    private long size;

    public FileCacheArray(CacheObjectFactory<T> objectFact, String fileName,
            LRUCCache cache) throws IOException {
        super(objectFact, cache);
        baseFileName = fileName.replace(".txt", "");

        // Count the number of lines in the file.
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(
                fileName)));
        lnr.skip(Long.MAX_VALUE);
        size = lnr.getLineNumber();
        lnr.close();

        createAllSubFiles();
    }

    @Override
    protected void createAllSubFiles() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(
                baseFileName + ".txt")));
        long numBlocksPerSubFile = cache.numBlocksPerSubFile();
        long blockSize = cache.getBlockSize();
        for (long i = 0; i < size; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = baseFileName + "_"
                    + +(i / (numBlocksPerSubFile * blockSize)) + ".txt";
            FileWriter fw = new FileWriter(subFileName);
            BufferedWriter bw = new BufferedWriter(fw);
            for (long j = i; j < Math.min(size, i
                    + (numBlocksPerSubFile * blockSize)); j++) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
        br.close();
    }

    @Override
    public void close() throws IOException {
        cache.dump(this);
        mergeAllSubFiles();
        cache.removeCacheArray(this);
    }

    private void mergeAllSubFiles() throws IOException {

        long numBlocksPerSubFile = cache.numBlocksPerSubFile();
        long blockSize = cache.getBlockSize();

        File oldFile = new File(baseFileName + ".txt");
        oldFile.delete();

        FileWriter fw = new FileWriter(oldFile.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(fw);

        for (long i = 0; i < size; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = baseFileName + "_"
                    + +(i / (numBlocksPerSubFile * blockSize)) + ".txt";
            File subFile = new File(subFileName);
            BufferedReader br = new BufferedReader(new FileReader(subFile));
            for (long j = i; j < Math.min(size, i
                    + (numBlocksPerSubFile * blockSize)); j++) {
                bw.write(br.readLine());
                bw.newLine();
            }
            br.close();
            subFile.delete();
        }
        bw.close();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getBaseFileName() {
        return baseFileName;
    }

}
