package com.airw.listranking;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.airw.arrays.FileCacheArray;
import com.airw.cache.CacheArray;
import com.airw.cache.LRUCache;
import com.airw.framework.ListRankNode;
import com.airw.framework.ListRankNodeFactory;
import com.airw.listranking.ListRanking;


public class ListRankingTest {

    private static int listsize = 1000;
    private static int blockSize = 20;
    private static int numBlocksInCache = 20;

    public static void main(String[] args) throws IOException {
        Random gen = new Random();
        
        File testFileEffic = new File("listRankTestFileEfficient.txt");
        File testFileNaive = new File("listRankTestFileNaive.txt");
        FileWriter fwEffic = new FileWriter(testFileEffic);
        FileWriter fwNaive = new FileWriter(testFileNaive);
        BufferedWriter bwEffic = new BufferedWriter(fwEffic);
        BufferedWriter bwNaive = new BufferedWriter(fwNaive);
       
        ArrayList<Integer> perm = new ArrayList<Integer>();
        for(int i = 1; i < listsize; i++){
            perm.add(i);
        }
        Collections.shuffle(perm);
        
        
        Map<Integer, Integer> idNextMap = new HashMap<Integer, Integer>();
        int cur = 0;
        int next;
        while(perm.size() > 0){
            next = perm.remove(0);
            idNextMap.put(cur, next);
            cur = next;
        }
        idNextMap.put(cur, 0);
        
        int end = Math.max(gen.nextInt(listsize), 1);
        
        ArrayList<ListRankNode> thelist = new ArrayList<ListRankNode>();
        for(int i = 0; i < listsize; i++){
            next = idNextMap.get(i);
            int next_next = idNextMap.get(next);
            if(i == end)
                next = -next;
            thelist.add(new ListRankNode(i, next, 1, Integer.MAX_VALUE, next_next, false));
        }
        
        
        // Copy first few lines of file.
        for (int i = 0; i < listsize; i++) {
            bwEffic.write(thelist.get(i).toString());
            bwEffic.newLine();
            bwNaive.write(thelist.get(i).toString());
            bwNaive.newLine();
        }
        bwEffic.close();
        bwNaive.close();
        
        LRUCache lru = new LRUCache(blockSize, numBlocksInCache, 5);
        ListRankNodeFactory cif = new ListRankNodeFactory();
        CacheArray<ListRankNode> arrayEffic = new FileCacheArray<ListRankNode>(
                cif, testFileEffic.getAbsolutePath(), lru);
        
        CacheArray<ListRankNode> arrayNaive = new FileCacheArray<ListRankNode>(
                cif, testFileNaive.getAbsolutePath(), lru);

        ListRanking.rankListNaive(arrayNaive);
        arrayNaive.close();
        System.out.println("Naive done");
        ListRanking.rankList(arrayEffic);
        
        arrayEffic.close();

    }
    
}
