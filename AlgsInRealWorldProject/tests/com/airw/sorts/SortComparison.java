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
import com.airw.framework.CacheInteger;
import com.airw.framework.CacheIntegerFactory;

public class SortComparison {

    private static int blockSize = 20;
    private static int numBlocksInCache = 20;
    private static int blocksInFile = 5;
    private static int iterations = 1;

    public static void main(String[] args) throws IOException {
    	int [] Ns = {10,50,100, 500, 1000, 2500, 5000};

        File recordFile = new File("SortSelectionSortMisses_BlockSize=" + blockSize
                + "_numBlocksInCache=" + numBlocksInCache + "_blocksInFile"
                + blocksInFile + ".txt");
        FileWriter recordfw = new FileWriter(recordFile);
        BufferedWriter recordbw = new BufferedWriter(recordfw);

        for (int n : Ns) {
            double totalMisses = 0.0;
            for (int c = 0; c < iterations; c++) {
                System.out.println(n + "_" + c);
                ArrayList<Integer> perm = new ArrayList<Integer>();
                for (int i = 0; i < n; i++) {
                    perm.add(i);
                }
                Collections.shuffle(perm);

                File testFile = new File("MergeSort_Test.txt");
                FileWriter fw = new FileWriter(testFile);
                BufferedWriter bw = new BufferedWriter(fw);

                // Copy first few lines of file.
                for (int i = 0; i < n; i++) {
                    bw.write("" + perm.get(i));
                    bw.newLine();
                }
                bw.close();

                LRUCache lru = new LRUCache(blockSize, numBlocksInCache,
                        blocksInFile);
                CacheIntegerFactory cif = new CacheIntegerFactory();
                CacheArray<CacheInteger> array = new FileCacheArray<CacheInteger>(
                        cif, testFile.getAbsolutePath(), lru);

                IOEfficientMergeSort<CacheInteger> qs = new IOEfficientMergeSort<CacheInteger>(array,
                        cif.getBasicComparator());

                qs.sort();

                array.close();
                totalMisses += lru.getMisses();

            }
            double avrg = totalMisses / iterations;
            recordbw.write(n + " " + avrg);
            recordbw.newLine();
        }
        recordbw.close();
    }
}
