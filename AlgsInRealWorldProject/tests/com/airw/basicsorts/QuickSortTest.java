package com.airw.basicsorts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.airw.cache.CacheArray;
import com.airw.cache.FileCacheArray;
import com.airw.cache.LRUCCache;
import com.airw.framework.CacheIntegerFactory;
import com.airw.sorts.QuickSort;
import com.airw.tools.IntegerFileObject;

public class QuickSortTest {

    private static int fileSize = 1001;
    private static int blockSize = 20;
    private static int numBlocksInCache = 20;

    public static void main(String[] args) throws IOException {

        File testFile = new File("testFileSortNew.txt");
        FileWriter fw = new FileWriter(testFile);
        BufferedWriter bw = new BufferedWriter(fw);
        Random gen = new Random();
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + gen.nextInt(fileSize));
            bw.newLine();
        }
        bw.close();
        
        LRUCCache lru = new LRUCCache(blockSize, numBlocksInCache, 5);
        CacheIntegerFactory cif = new CacheIntegerFactory();
        CacheArray<IntegerFileObject> array = new FileCacheArray<IntegerFileObject>(
                cif, testFile.getAbsolutePath(), lru);

        QuickSort<IntegerFileObject> qs = new QuickSort<IntegerFileObject>(array, cif.getBasicComparator());

        qs.sort();
        
        array.close();

    }
}
