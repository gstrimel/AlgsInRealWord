package com.airw.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.airw.cache.LRUCache;

/**
 * Simple class to test the caching system. Negates a file with numbers.
 * 
 */
public class TestCache {

    private static int fileSize = 1001;
    private static int blockSize = 10;
    private static int numBlocksInCache = 10;

    public static void main(String[] args) throws IOException {

        File testFile = new File("testFile.txt");
        FileWriter fw = new FileWriter(testFile);
        BufferedWriter bw = new BufferedWriter(fw);
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + i);
            bw.newLine();
        }
        bw.close();

        LRUCache lru = new LRUCache(testFile.getAbsolutePath(), blockSize,
                numBlocksInCache, 0.0);

        List<Integer> accesses = new LinkedList<Integer>();
        for (int i = 0; i < 10 * fileSize; i++) {
            accesses.add(i);
        }
        Collections.shuffle(accesses);

        for (Integer i : accesses) {
            if (i % 3 == 0) {
                Long s = (long) (-(i % fileSize));
                lru.set(i % fileSize, s);
            } else {
                lru.get(i % fileSize);
            }
        }
        lru.close();

    }

}
