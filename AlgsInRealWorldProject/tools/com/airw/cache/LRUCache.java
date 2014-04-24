package com.airw.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.airw.framework.CacheObjectFactory;

public class LRUCache<T extends Comparable<T>> {

    private int blockSize; // Number of entries in a block.
    private int numBlocksInCache; // Number of blocks in the cache.
    private int numEntriesInFile;
    private LRUMap<Long, List<String>> cache;
    private long hits;
    private long accesses;
    private String fileName;
    private int numBlocksPerSubFile;
    private CacheObjectFactory<T> cacheObjectFactory;
    private long numEntriesWithExta;

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
    public LRUCache(String fileName, CacheObjectFactory<T> cacheObjectFactory,
            int blockSize, final int numBlocksInCache,
            double extraSpaceMultiplier) throws IOException {
        if (extraSpaceMultiplier < 0.0) {
            throw new IllegalArgumentException(
                    "Extra space must be nonnegative.");
        }

        this.blockSize = blockSize;
        this.numBlocksInCache = numBlocksInCache;

        this.fileName = fileName.replace(".txt", "");

        this.cacheObjectFactory = cacheObjectFactory;

        // Count the number of lines in the file.
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(
                fileName)));
        lnr.skip(Long.MAX_VALUE);
        this.numEntriesInFile = lnr.getLineNumber();
        lnr.close();

        // Calculate extra space needed.
        this.numEntriesWithExta = numEntriesInFile
                + (long) Math.ceil(numEntriesInFile * extraSpaceMultiplier);

        numBlocksPerSubFile = (int) Math.ceil(Math
                .sqrt((double) numEntriesInFile) / blockSize);
        numBlocksPerSubFile = Math.max(numBlocksPerSubFile, 1);

        cache = new LRUMap<Long, List<String>>(numBlocksInCache, .75F, true);

        createAllSubFiles();

        // Set the hits.
        hits = 0;
        accesses = 0;
    }

    /**
     * Cleans up the process. Flushes the cache and deletes all temporary files.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        for (Map.Entry<Long, List<String>> entry : cache.entrySet()) {
            writeEntries(entry.getKey(), entry.getValue());
        }
        mergeAllSubFiles();
    }

    /**
     * Gets an element at a given index.
     * 
     * @param index
     *            The element index to access.
     * @return The element at this index.
     * @throws IOException
     */
    public T get(long index) throws IOException {
        if (index >= numEntriesWithExta) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }
        accesses++;
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        if (cache.containsKey(blockNumber)) {
            hits++;
            return cacheObjectFactory.createCacheObject(cache.get(blockNumber)
                    .get(indexInBlock));
        } else {
            List<String> block = pullBlock(blockNumber);
            cache.put(blockNumber, block);
            if (cache.getEldestEntry() != null) {
                writeEntries(cache.getEldestEntry().getKey(), cache
                        .getEldestEntry().getValue());
            }
            return cacheObjectFactory
                    .createCacheObject(block.get(indexInBlock));
        }
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
    public void set(long index, T s) throws IOException {
        if (index >= numEntriesWithExta) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }
        accesses++;
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        if (cache.containsKey(blockNumber)) {
            hits++;
            cache.get(blockNumber).set(indexInBlock, s.toString());
        } else {
            List<String> block = pullBlock(blockNumber);
            block.set(indexInBlock, s.toString());
            cache.put(blockNumber, block);
            if (cache.getEldestEntry() != null) {
                writeEntries(cache.getEldestEntry().getKey(), cache
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
    private List<String> pullBlock(long blockNumber) throws IOException {
        int subFileNum = (int) (blockNumber / (numBlocksPerSubFile));
        int blockIndexInFile = (int) (blockNumber % numBlocksPerSubFile);
        String subFileName = fileName + subFileNum + ".txt";

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
    private void writeEntries(long blockNumber, List<String> entries)
            throws IOException {
        int subFileNum = (int) (blockNumber / (numBlocksPerSubFile));
        int blockIndexInFile = (int) (blockNumber % numBlocksPerSubFile);
        String subFileName = fileName + subFileNum;
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
        String copyFileName = subFileName + "copy.txt";
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

    /**
     * Creates all necessary sub files for the process.
     * 
     * @throws IOException
     */
    private void createAllSubFiles() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName
                + ".txt")));
        boolean endOfFileReached = false;
        for (long i = 0; i < numEntriesWithExta; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = fileName
                    + (i / (numBlocksPerSubFile * blockSize)) + ".txt";
            FileWriter fw = new FileWriter(subFileName);
            BufferedWriter bw = new BufferedWriter(fw);
            for (long j = i; j < Math.min(numEntriesWithExta, i
                    + (numBlocksPerSubFile * blockSize)); j++) {
                String line;
                if (endOfFileReached) {
                    line = "";
                } else {
                    line = br.readLine();
                    if (line == null) {
                        endOfFileReached = true;
                        line = "";
                    }
                }
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
        br.close();
    }

    /**
     * Takes the results of all the subfiles and merges them back into the
     * original file. Called on cleanup.
     * 
     * @throws IOException
     */
    private void mergeAllSubFiles() throws IOException {
        File oldFile = new File(fileName + ".txt");
        oldFile.delete();

        FileWriter fw = new FileWriter(oldFile.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(fw);

        for (long i = 0; i < numEntriesWithExta; i += (numBlocksPerSubFile * blockSize)) {
            String subFileName = fileName
                    + (i / (numBlocksPerSubFile * blockSize)) + ".txt";
            File subFile = new File(subFileName);
            if (i < numEntriesInFile) {
                BufferedReader br = new BufferedReader(new FileReader(subFile));
                for (long j = i; j < Math.min(numEntriesInFile, i
                        + (numBlocksPerSubFile * blockSize)); j++) {
                    bw.write(br.readLine());
                    bw.newLine();
                }
                br.close();
            }
            subFile.delete();
        }
        bw.close();
    }

    /**
     * Gets the key of the eldest element in the list.
     * 
     * @return The key of the eldest element in the list.
     */
    public int fileSize() {
        return numEntriesInFile;
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

    public long totalAccessableSize() {
        return numEntriesWithExta;
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
    }

}
