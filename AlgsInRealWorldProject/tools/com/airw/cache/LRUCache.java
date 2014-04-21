package com.airw.cache;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LRUCache {

    private int blockSize; // Number of entries in a block.
    private int numBlocksInCache; // Number of blocks in the cache.
    private int numEntriesInFile;  
    private int lineSizeInBytes;
    private RandomAccessFile file;
    private Map<Long, List<String>> cache;
    private long hits;
    private long accesses;

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
    public LRUCache(int blockSize, final int numBlocksInCache, String fileName)
            throws IOException {
        this.blockSize = blockSize;
        this.numBlocksInCache = numBlocksInCache;

        // Count the number of lines in the file.
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(
                fileName)));
        lnr.skip(Long.MAX_VALUE);
        this.numEntriesInFile = lnr.getLineNumber();
        lnr.close();

        // Count the number of bytes in the first line.
        FileInputStream fstream = new FileInputStream("textfile.txt");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = br.readLine() + "\n";
        lineSizeInBytes = strLine.length();
        br.close();

        // Open ultimate file to read and write from.
        file = new RandomAccessFile(fileName, "rw");

        cache = new LinkedHashMap<Long, List<String>>(numBlocksInCache + 1,
                .75F, true) {
            private static final long serialVersionUID = 1L;

            // This method is called just after a new entry has been added.
            public boolean removeEldestEntry(
                    Map.Entry<Long, List<String>> eldest) {
                return size() > numBlocksInCache;
            }
        };

        // Set the hits.
        hits = 0;
        accesses = 0;
    }

    /**
     * Gets an element at a given index.
     * 
     * @param index
     *            The element index to access.
     * @return The element at this index.
     * @throws IOException
     */
    public String get(long index) throws IOException {
        if (index > numEntriesInFile) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }
        accesses++;
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        if (cache.containsKey(blockNumber)) {
            hits++;
            return cache.get(blockNumber).get(indexInBlock);
        } else {
            if (cache.size() == numBlocksInCache) {
                // Record entry changes because its getting kicked out.
                Long key = getEldestKey();
                List<String> eldestEntries = cache.get(key);
                writeEntries(blockNumber, eldestEntries);
            }
            List<String> block = pullBlock(blockNumber);
            cache.put(blockNumber, block);
            return block.get(indexInBlock);
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
    public void set(long index, String s) throws IOException {
        if (index > numEntriesInFile) {
            throw new IndexOutOfBoundsException(
                    "Attempted to access element out of bounds.");
        }
        accesses++;
        long blockNumber = index / blockSize;
        int indexInBlock = (int) (index % blockSize);
        if (cache.containsKey(blockNumber)) {
            hits++;
            cache.get(blockNumber).set(indexInBlock, s);
        } else {
            if (cache.size() == numBlocksInCache) {
                // Record entry changes because its getting kicked out.
                Long key = getEldestKey();
                List<String> eldestEntries = cache.get(key);
                writeEntries(blockNumber, eldestEntries);
            }
            List<String> block = pullBlock(blockNumber);
            block.set(indexInBlock, s);
            cache.put(blockNumber, block);
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
        long beginIndex = blockNumber * blockSize;
        List<String> block = new ArrayList<String>();
        for (long i = beginIndex; i < beginIndex + blockSize
                && i < numEntriesInFile; i++) {
            block.add(readFromFile(i));
        }
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
        long elemIndexNum = blockNumber * blockSize;
        for (String entry : entries) {
            writeToFile(entry, elemIndexNum);
        }

    }

    /**
     * Gets the key of the eldest element in the list.
     * 
     * @return The key of the eldest element in the list.
     */
    private Long getEldestKey() {
        final Set<Entry<Long, List<String>>> mapValues = cache.entrySet();
        final int maplength = mapValues.size();
        @SuppressWarnings("unchecked")
        final Entry<Long, List<String>>[] ray = new Entry[maplength];
        mapValues.toArray(ray);
        return ray[ray.length - 1].getKey();
    }

    /**
     * Reads an entry from the file given its index.
     * 
     * @param index
     *            The index of the element to read.
     * @return The string of the index that was read.
     * @throws IOException
     */
    private String readFromFile(long index) throws IOException {
        long position = index * lineSizeInBytes;
        file.seek(position);
        byte[] bytes = new byte[lineSizeInBytes - 1]; // Ignore the "\n" at the
                                                      // end of the line.
        file.read(bytes);
        String data = bytes.toString();
        while (data.charAt(data.length() - 1) != ' ') { // Strip off the
                                                        // remaining padding.
            data = data.substring(data.length() - 2, data.length() - 1);
        }
        return data;
    }

    /**
     * Reads an entry from the file given its index.
     * 
     * @param data
     *            The data to be written.
     * @param index
     *            The index of the element to read.
     * @throws IOException
     */
    private void writeToFile(String data, long index) throws IOException {
        if (data.length() > lineSizeInBytes - 1) {
            throw new IOException(
                    "Tried to write a string which was too large.");
        }
        String line = data;
        while (line.length() < lineSizeInBytes - 1) {
            line += " ";
        }
        line += "\n";
        long position = index * lineSizeInBytes;
        file.seek(position);
        file.write(line.getBytes());
    }

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

}
