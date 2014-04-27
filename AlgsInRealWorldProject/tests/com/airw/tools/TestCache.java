package com.airw.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.airw.cache.CacheArray;
import com.airw.cache.EmptyCacheArray;
import com.airw.cache.FileCacheArray;
import com.airw.cache.LRUCCache;
import com.airw.framework.CacheIntegerFactory;

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

        LRUCCache lru = new LRUCCache(blockSize, numBlocksInCache, 20);
        CacheIntegerFactory cif = new CacheIntegerFactory();
        CacheArray<IntegerFileObject> array = new FileCacheArray<IntegerFileObject>(
                cif, testFile.getAbsolutePath(), lru);
        EmptyCacheArray<IntegerFileObject> array2 = new EmptyCacheArray<IntegerFileObject>(
                cif, fileSize, lru);

        List<Integer> accesses = new LinkedList<Integer>();
        for (int i = 0; i < 10 * fileSize; i++) {
            accesses.add(i);
        }
        Collections.shuffle(accesses);

        for (Integer i : accesses) {
            if (i % 3 == 0) {
                IntegerFileObject s = new IntegerFileObject(-(i % fileSize));
                array.set(i % fileSize, s);
                array2.set(i % fileSize, s);
            } else {
                array.get(i % fileSize);
                array2.get(i % fileSize);
            }
        }
        array.close();
        array2.close();

    }

}
