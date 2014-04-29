package com.airw.sorts;

import java.io.IOException;
import java.util.Comparator;

import com.airw.arrays.EmptyCacheArray;
import com.airw.cache.CacheArray;
import com.airw.cache.CacheObject;
import com.airw.cache.LRUCache;
import com.airw.framework.CacheIntegerFactory;
import com.airw.framework.CacheInteger;

public class IOEfficientMergeSort<T extends CacheObject> extends Sort<T> {


	public IOEfficientMergeSort(CacheArray<T> array, Comparator<T> comp) {
        super(array, comp);
    }
    
	public void sort() throws IOException {
		mergeAux(0, array.size());
	}	
	
    // Merges elements from lowIndex to highIndex - 1.
    public void mergeAux(long lowIndex, long highIndex) throws IOException {
        long numElems = highIndex - lowIndex;
        
        if(numElems == 1) {
            return;
        }
       
        LRUCache cache = array.getCache();
        long K = (long) Math.ceil((double) cache.cacheSize() / (2*cache.getBlockSize()));
        
        /*
         * All sub-arrays may not be the same size. Consider K=5, numElems = 4:
         * maxSubArraySize = ceil(5/4) = 2
         * 
         * So our sub-arrays have size [2,1,1,1]
         */
        long maxSubArraySize = (long) Math.ceil((double) numElems / K);
        
        /*
         * Calculate the first index for which the sub-array size equals maxSubArraySize-1
         * 
         * Invariant: \forall i < firstLowerIndex, size(subarray[i]) == maxSubArraySize
         *            \forall i >= firstLowerIndex, size(subarray[i]) == maxSubArraySize-1
         */
        long firstLowerIndex = K;
        for(int i = 0; i < K; i++) {
        	long elemsLeft = numElems - maxSubArraySize*i;
        	
        	if(Math.ceil((double) elemsLeft / (K-i)) < maxSubArraySize) {
        		firstLowerIndex = i;
        		break;
        	}
        }
        
        // Sort each of the sub-arrays

        long startIndex = lowIndex;
        int curSubArrayIndex = 0;
            
        while(startIndex < highIndex) {
        	long curSubArraySize;
        	if(curSubArrayIndex < firstLowerIndex) {
        		curSubArraySize = maxSubArraySize;
        	} else {
        		curSubArraySize = maxSubArraySize - 1;
        	}
            long endIndex = Math.min(startIndex + curSubArraySize, highIndex);
            
            mergeAux(startIndex, endIndex);
            
            startIndex = endIndex;      
        	curSubArrayIndex += 1;
        }

        // k-way merge
        
        // Temp storage for merging
        EmptyCacheArray<T> mergedArray = new EmptyCacheArray<T>(array.getFactory(), numElems, cache);
        
        CacheIntegerFactory cif = new CacheIntegerFactory();
        
        // The pointer position for each of the K sub-arrays
        EmptyCacheArray<CacheInteger> curPosition = new EmptyCacheArray<CacheInteger>(cif, K, cache);

        // Start curPos pointer for each sub-array at location 0
        for (int i = 0; i < K; i++) {
            curPosition.set(i, new CacheInteger(0));
        }
        
        long curElem = 0;
        while(curElem < numElems) {
            T min = null;
            long minSubArray = -1;
            
            // Search for the min element
            for (int i = 0; i < K; i++) {
                int curPos = curPosition.get(i).valueOf();
                
                // If we haven't run off the end of the sub-array
                if((i < firstLowerIndex && curPos < maxSubArraySize) ||
                	i >= firstLowerIndex && curPos < maxSubArraySize - 1) {
                	
                	// What's the index of the current element in the main array?
                	long index;
                	
                	if(i < firstLowerIndex) {
                		index = lowIndex + (i * maxSubArraySize) + curPos;
                	} else {
                		index = lowIndex + (firstLowerIndex * maxSubArraySize) + (i-firstLowerIndex) * (maxSubArraySize - 1) + curPos;
                	}

                    if(index < array.size() && (min == null || comp.compare(array.get(index),min) < 0)) {
                        min = array.get(index);
                        minSubArray = i;
                    }
                }   
            }
            
            mergedArray.set(curElem, min);         
            curPosition.set(minSubArray, new CacheInteger(curPosition.get(minSubArray).valueOf() + 1));
            curElem += 1;
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