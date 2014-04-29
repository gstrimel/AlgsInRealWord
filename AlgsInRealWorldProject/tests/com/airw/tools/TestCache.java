package com.airw.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.airw.arrays.EmptyCacheArray;
import com.airw.arrays.FileCacheArray;
import com.airw.cache.CacheArray;
import com.airw.cache.LRUCache;
import com.airw.framework.CacheIntegerFactory;
import com.airw.framework.CacheInteger;

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

        LRUCache lru = new LRUCache(blockSize, numBlocksInCache, 20);
        CacheIntegerFactory cif = new CacheIntegerFactory();
        CacheArray<CacheInteger> array = new FileCacheArray<CacheInteger>(
                cif, testFile.getAbsolutePath(), lru);
        EmptyCacheArray<CacheInteger> array2 = new EmptyCacheArray<CacheInteger>(
                cif, fileSize, lru);

        for (int i = 0; i < fileSize; i++) {
            array2.set(i, new CacheInteger(-i));
        }
        
        List<Integer> accesses = new LinkedList<Integer>();
        for (int i = 0; i < 10 * fileSize; i++) {
            accesses.add(i);
        }
        Collections.shuffle(accesses);

        for (Integer i : accesses) {
            if (i % 3 == 0) {
                array.set(i % fileSize, array2.get(i % fileSize));
                array2.set(i % fileSize, array2.get(i % fileSize));
            } else {
                array.get(i % fileSize);
                array2.get(i % fileSize);
            }
        }
        array.close();
        array2.close();

    }

}
