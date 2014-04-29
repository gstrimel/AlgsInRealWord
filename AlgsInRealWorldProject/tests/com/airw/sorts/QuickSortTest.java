package com.airw.sorts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.airw.arrays.FileCacheArray;
import com.airw.cache.CacheArray;
import com.airw.cache.LRUCache;
import com.airw.framework.CacheIntegerFactory;
import com.airw.framework.CacheInteger;
import com.airw.sorts.QuickSort;

public class QuickSortTest {

    private static int fileSize = 1001;
    private static int blockSize = 20;
    private static int numBlocksInCache = 20;

    public static void main(String[] args) throws IOException {

        ArrayList<Integer> perm = new ArrayList<Integer>();
        for (int i = 0; i < fileSize; i++) {
            perm.add(i);
        }
        Collections.shuffle(perm);
        
        File testFile = new File("testFileSortNew.txt");
        FileWriter fw = new FileWriter(testFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + perm.get(i));
            bw.newLine();
        }
        bw.close();
        
        LRUCache lru = new LRUCache(blockSize, numBlocksInCache, 5);
        CacheIntegerFactory cif = new CacheIntegerFactory();
        CacheArray<CacheInteger> array = new FileCacheArray<CacheInteger>(
                cif, testFile.getAbsolutePath(), lru);

        QuickSort<CacheInteger> qs = new QuickSort<CacheInteger>(array, cif.getBasicComparator());

        qs.sort();
        
        array.close();

    }
}
