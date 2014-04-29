package com.airw.sorts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.airw.sorts.IOEfficientMergeSort;
import com.airw.sorts.MergeSort;

public class SortComparison {
    private static int fileSize = 10000;
    private static int blockSize = 10;
    private static int numBlocksInCache = 100;

    public static void main(String[] args) throws IOException {
        
        
        File testFile = new File("testFileSort.txt");
        FileWriter fw = new FileWriter(testFile);
        BufferedWriter bw = new BufferedWriter(fw);
        Random gen = new Random();
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + gen.nextInt(fileSize));
            bw.newLine();
        }
        bw.close();

        MergeSort qs = new MergeSort(testFile.getAbsolutePath(), blockSize,
                numBlocksInCache);

        qs.sort();
        System.out.println("Reg merge sort done");
        
        testFile = new File("testFileSort.txt");
        fw = new FileWriter(testFile);
        bw = new BufferedWriter(fw);
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + gen.nextInt(fileSize));
            bw.newLine();
        }
        bw.close();
        
        IOEfficientMergeSort ms = new IOEfficientMergeSort(testFile.getAbsolutePath(), blockSize,
                numBlocksInCache);

        ms.sort();
        System.out.println("Efficient merge sort done");
        System.out.println(((double) ms.getMisses()) / qs.getMisses());

    }

}
