package com.airw.sorts;

import java.io.IOException;
import java.util.Comparator;

import com.airw.cache.CacheArray;
import com.airw.cache.CacheObject;
import com.airw.cache.EmptyCacheArray;
import com.airw.cache.LRUCCache;
import com.airw.framework.CacheIntegerFactory;
import com.airw.tools.IntegerFileObject;

public class IOEfficientMergeSort<T extends CacheObject> extends Sort<T> {


	public IOEfficientMergeSort(CacheArray<T> array, Comparator<T> comp) {
        super(array, comp);
    }
    
	@Override
	public void sort() throws IOException {
		mergeAux(0, array.size());
	}	
	
    // Merges elements from lowIndex to highIndex - 1.
    public void mergeAux(long lowIndex, long highIndex) throws IOException {
        long numElems = highIndex - lowIndex;
        
        // Just run a quick sort if data can fit in a cache block
        if(numElems == 1) {
            return;
        }
        
        //long subArraySize = 2*cache.getBlockSize();
        // Round k up
        LRUCCache cache = array.getCache();
        long K = (cache.cacheSize() + 2*cache.getBlockSize() - 1) / (2*cache.getBlockSize());
        long subArraySize = (long) Math.ceil((double) numElems / K);
        
        // Sort each of the subarrays
        long startIndex = lowIndex;
        while(startIndex < highIndex) {
            long endIndex = Math.min(startIndex + subArraySize, highIndex);
            mergeAux(startIndex, endIndex);
            startIndex = endIndex;      
        }

        // k-way merge

        
        EmptyCacheArray<T> mergedArray = new EmptyCacheArray<T>(array.getFactory(), numElems, array.getCache());
        CacheIntegerFactory cif = new CacheIntegerFactory();
        EmptyCacheArray<IntegerFileObject> curPosition = new EmptyCacheArray<IntegerFileObject>(cif, K, array.getCache());

        // Start curPos pointer for each sub-array at location 0
        for (int i = 0; i < K; i++) {
            curPosition.set(i, new IntegerFileObject(0));
        }
        
        long lastSubArraySize = numElems - subArraySize*(K-1);
        
        long p = 0;
        while(p < numElems) {
            T min = null; //array.get(lowIndex);
            long minSubArray = -1;
            
            // Search for the min element
            for (int i = 0; i < K; i++) {
                int curPos = curPosition.get(i).valueOf();
                
                // If we haven't run off the end of the subarray
                if((i < K-1 && curPos < subArraySize) ||
                    (i == K-1 && curPos < lastSubArraySize)) {
                    
                    long index = lowIndex + (i * subArraySize) + curPos;
                    // System.out.printf("lowIndex = %d, i = %d, subArraySize = %d, curPos = %d, K = %d, numElems = %d, index = %d\n", lowIndex, i, subArraySize, curPos, K, numElems, index);
                    
                    if(index < array.size() && (min == null || comp.compare(array.get(index),min) < 0)) {
                        min = array.get(index);
                        minSubArray = i;
                    }
                }   
            }
            
            mergedArray.set(p, min);         
            curPosition.set(minSubArray, new IntegerFileObject(curPosition.get(minSubArray).valueOf() + 1));
            p += 1;
        }
        
        //Copy sorted array back to original location
        for(int i = 0; i < numElems; i++) {
            array.set(lowIndex + i, mergedArray.get(i));
        }
        
        mergedArray.close();
        curPosition.close();
    }
    
    public void quickAux(long lowIndex, long highIndex) throws IOException {

        // at least one item must exist in the array
        if (lowIndex >= highIndex) {
            return;
        }

        long pivotIndex = getMedianIndexAsPivotIndex(lowIndex, highIndex);
        // 1) Choose pivot from the sublist
        T pivot = array.get(pivotIndex);
        // 2) Swap the pivot to the last item in the array
        swapItemsWithIndices(pivotIndex, highIndex);

        /*
         * Get the border indices sandwiching the unsorted items alone (ignore
         * pivot (now, in the highIndex)) set 'i' to point to the item before
         * the first Index set 'j' to point to the item before pivot
         * 
         * Notice that this way, the following invariant gets maintained all
         * through the sorting procedure
         * 
         * a. All items left of Index 'i' have a value <=pivot b. All items
         * right of Index 'j' have a value >=pivot
         */

        long i = lowIndex - 1;
        long j = highIndex;

        do { // Notice the <j (pivot item is ignored). We stop when both the
             // counters cross

            // compareTo will return 0 when it reaches the pivot - will exit
            // loop
            do {
                i++;
            } while (comp.compare(array.get(i), pivot) < 0);
            // we dont have the protection as the previous loop.
            // So, add extra condition to prevent 'j' from overflowing outside
            // the current sub array
            do {
                j--;
            } while (comp.compare(array.get(j), pivot) > 0 && (j > lowIndex));

            if (i < j) {
                swapItemsWithIndices(i, j);
            }
        } while (i < j);

        swapItemsWithIndices(highIndex, i);// bring pivot to i's position

        // the big subarray is partially sorted (agrees to invariant). Let's
        // recurse and bring in more hands

        quickAux(lowIndex, i - 1); // sort subarray between low index and one
                                   // before the pivot
        quickAux(i + 1, highIndex); // sort subarray between low index and one
                                    // before the pivot
    }

    // ... since swapping with array is the easiest way to swap two objects
    private void swapItemsWithIndices(long firstItem, long secondItem)
            throws IOException {
        final T tempItem = array.get(firstItem);
        array.set(firstItem, array.get(secondItem));
        array.set(secondItem, tempItem);
    }

    // Variation 1 - chose median as pivot
    private long getMedianIndexAsPivotIndex(long lowIndex, long highIndex) {
        return lowIndex + ((highIndex - lowIndex) / 2);
    }

}