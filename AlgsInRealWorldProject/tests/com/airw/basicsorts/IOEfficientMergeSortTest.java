package com.airw.basicsorts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.airw.sorts.IOEfficientMergeSort;

public class IOEfficientMergeSortTest {
    private static int fileSize = 1001;
    private static int blockSize = 20;
    private static int numBlocksInCache = 20;

    public static void main(String[] args) throws IOException {
        File testFile = new File("testIOMergeSort.txt");
        FileWriter fw = new FileWriter(testFile);
        BufferedWriter bw = new BufferedWriter(fw);
        Random gen = new Random();
        // Copy first few lines of file.
        for (int i = 0; i < fileSize; i++) {
            bw.write("" + gen.nextInt(fileSize));
            bw.newLine();
        }
        bw.close();

        IOEfficientMergeSort ms = new IOEfficientMergeSort(testFile.getAbsolutePath(), blockSize,
                numBlocksInCache);

        ms.sort();
    }
}
