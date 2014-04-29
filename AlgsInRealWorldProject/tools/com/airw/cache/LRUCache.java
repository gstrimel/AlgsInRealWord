package com.airw.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LRUCache {

    private int blockSize; // Number of entries in a block.
    private int numBlocksInCache; // Number of blocks in the cache.
    private LRUMap<CacheKey, List<String>> cache;
    private List<CacheArray<?>> cacheArrays;
    private long hits;
    private long accesses;
    private int numBlocksPerSubFile;

    /**
     * Constructor for LRUCache.
     * 
     * @param blockSize
     *            Number of entries for each block in cache.
     * @param numBlocksInCache
     *            Number of blocks in the cache.
     * @param fileName
     *            Name of input file.
     * @throws IOException
     *             Thrown on any error opening and scanning file.
     */
    public LRUCache(int blockSize, final int numBlocksInCache,
            int numBlocksPerSubFile) throws IOException {

        this.blockSize = blockSize;
        this.numBlocksInCache = numBlocksInCache;
        this.numBlocksPerSubFile = Math.max(numBlocksPerSubFile, 1);

        cache = new LRUMap<CacheKey, List<String>>(numBlocksInCache, .75F, true);

        cacheArrays = new LinkedList<CacheArray<?>>();

        // Set the hits.
        hits = 0;
        accesses = 0;
    }

    public void addCacheArray(CacheArray<?> ca) {
        cacheArrays.add(ca);
    }

    public void removeCacheArray(CacheArray<?> ca) {
        cacheArrays.remove(ca);
    }

    /**
     * Gets an element at a given index.
     * 
     * @param index
     *            The element index to access.
     * @return The element at this index.
     * @throws IOException
     */
    public String get(long index, CacheArray<?> ca) throws IOException {
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        CacheKey key = new CacheKey(ca.getId(), blockNumber);
        accesses++;
        if (cache.containsKey(key)) {
            hits++;
            return cache.get(key).get(indexInBlock);
        } else {
            List<String> block = pullBlock(ca, blockNumber);
            cache.put(key, block);
            if (cache.getEldestEntry() != null) {
                CacheKey evicKey = cache.getEldestEntry().getKey();
                CacheArray<?> evicArray = getArrayMatch(evicKey);
                writeEntries(evicArray, evicKey.getBlockNumber(), cache
                        .getEldestEntry().getValue());
            }
            return block.get(indexInBlock);
        }
    }

    private CacheArray<?> getArrayMatch(CacheKey evicKey) {
        for (CacheArray<?> ca : cacheArrays) {
            if (ca.getId() == evicKey.getId()) {
                return ca;
            }
        }
        return null;
    }

    /**
     * Sets the value of the index to the given string.
     * 
     * @param index
     *            The index to modify.
     * @param s
     *            The string we are setting the index to.
     * @throws IOException
     */
    public void set(long index, String v, CacheArray<?> ca) throws IOException {
        accesses++;
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        CacheKey key = new CacheKey(ca.getId(), blockNumber);
        if (cache.containsKey(key)) {
            hits++;
            cache.get(key).set(indexInBlock, v);
        } else {
            List<String> block = pullBlock(ca, blockNumber);
            block.set(indexInBlock, v);
            cache.put(key, block);
            if (cache.getEldestEntry() != null) {
                CacheKey evicKey = cache.getEldestEntry().getKey();
                CacheArray<?> evicArray = getArrayMatch(evicKey);
                writeEntries(evicArray, evicKey.getBlockNumber(), cache
                        .getEldestEntry().getValue());
            }
        }
    }

    /**
     * Pulls a given block from the file.
     * 
     * @param blockNumber
     *            The index of the block to pull.
     * @return The block pulled from file.
     * @throws IOException
     */
    private List<String> pullBlock(CacheArray<?> ca, long blockNumber)
            throws IOException {
        int subFileNum = (int) (blockNumber / (numBlocksPerSubFile));
        int blockIndexInFile = (int) (blockNumber % numBlocksPerSubFile);
        String subFileName = ca.getBaseFileName() + "_" + subFileNum + ".txt";

        File file = new File(subFileName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        // Skip first few lines of file.
        for (int i = 0; i < blockIndexInFile * blockSize; i++) {
            br.readLine();
        }
        // Read block.
        List<String> block = new ArrayList<String>();
        String line;
        for (int i = 0; i < blockSize; i++) {
            line = br.readLine();
            if (line == null) {
                break;
            }
            block.add(line);
        }
        br.close();
        return block;
    }

    /**
     * Writes a block to file.
     * 
     * @param blockNumber
     *            The index of the block to write.
     * @param eldestEntries
     *            The entries to write.
     * @throws IOException
     */
    private void writeEntries(CacheArray<?> ca, long blockNumber,
            List<String> entries) throws IOException {
        int subFileNum = (int) (blockNumber / (numBlocksPerSubFile));
        int blockIndexInFile = (int) (blockNumber % numBlocksPerSubFile);
        String subFileName = ca.getBaseFileName() + "_" + subFileNum;

        String copyFileName = copyFile(subFileName);

        File copyFile = new File(copyFileName);
        BufferedReader br = new BufferedReader(new FileReader(copyFile));
        FileWriter fw = new FileWriter(subFileName + ".txt");
        BufferedWriter bw = new BufferedWriter(fw);
        // Copy first few lines of file.
        for (int i = 0; i < blockIndexInFile * blockSize; i++) {
            bw.write(br.readLine());
            bw.newLine();
        }
        // Write changes block.
        for (int i = 0; i < entries.size(); i++) {
            br.readLine();
            bw.write(entries.get(i));
            bw.newLine();
        }
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line);
            bw.newLine();
        }
        br.close();
        bw.close();

        copyFile.delete();
    }

    public void dump(CacheArray<?> ca) throws IOException {
        List<CacheKey> keysToRemove = new LinkedList<CacheKey>();
        for (Entry<CacheKey, List<String>> e : cache.entrySet()) {
            if (e.getKey().getId() == ca.getId()) {
                writeEntries(ca, e.getKey().getBlockNumber(), e.getValue());
                keysToRemove.add(e.getKey());
            }
        }
        for (CacheKey ck : keysToRemove) {
            cache.remove(ck);
        }
        // my need to wory about last entry
        if (keysToRemove.size() > 0) {
            cache.nullifyEldest();
        }
    }

    /**
     * Copy's the contents of a file to a new file of the same name but with
     * "copy" appended to the name. Returns this new file name.
     * 
     * @param subFileName
     *            The base file name.
     * @return The copy file name.
     * @throws IOException
     */
    private String copyFile(String subFileName) throws IOException {
        String copyFileName = subFileName + "_copy.txt";
        BufferedReader br = new BufferedReader(new FileReader(new File(
                subFileName + ".txt")));
        FileWriter fw = new FileWriter(copyFileName);
        BufferedWriter bw = new BufferedWriter(fw);
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line);
            bw.newLine();
        }
        br.close();
        bw.close();
        return copyFileName;
    }

    public int cacheSize() {
        return numBlocksInCache * blockSize;
    }

    public long getHits() {
        return hits;
    }

    public long getAccesses() {
        return accesses;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public long getNumBlocksInCache() {
        return numBlocksInCache;
    }

    public long numBlocksPerSubFile() {
        return numBlocksPerSubFile;
    }

    /**
     * Our LRU map.
     * 
     * @param <K>
     *            The key object type.
     * @param <V>
     *            The value object type.
     */
    public class LRUMap<K, V> extends LinkedHashMap<K, V> {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Map.Entry<K, V> eldest;

        public LRUMap(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
            this.eldest = null;
        }

        @Override
        public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            if (size() > numBlocksInCache) {
                this.eldest = eldest;
            }
            return size() > numBlocksInCache;
        }

        public Map.Entry<K, V> getEldestEntry() {
            return eldest;
        }

        public void nullifyEldest() {
            this.eldest = null;
        }
    }

    public List<CacheArray<?>> getCacheArrays() {
        return cacheArrays;
    }
}
