package com.airw.sorts;

import java.io.IOException;

public class IOEfficientMergeSort extends Sort {

	public IOEfficientMergeSort(String fileName,
			int blockSize, int numBlocksInCache) throws IOException {
		super(fileName, blockSize, numBlocksInCache, 2.0);
	}

	@Override
	protected void sortCore() throws IOException {
		mergeAux(0, numElems());
	}
	
	// Merges elements from lowIndex to highIndex - 1.
	public void mergeAux(long lowIndex, long highIndex) throws IOException {
		long numElems = highIndex - lowIndex;
		
		// Just run a quick sort if data can fit in a cache block
		if(numElems < cache.getBlockSize()) {
			quickAux(lowIndex, highIndex);
			return;
		}
		
		//long subArraySize = 2*cache.getBlockSize();
		// Round k up
		long K = cache.getNumBlocksInCache() / 2; //(cache.cacheSize() + 2*cache.getBlockSize() - 1) / (2*cache.getBlockSize());
		long subArraySize = (long) Math.ceil((double) numElems / K);
		
		// Sort each of the subarrays
		long startIndex = lowIndex;
		while(startIndex < highIndex) {
			long endIndex = Math.min(startIndex + subArraySize, highIndex);
			mergeAux(startIndex, endIndex);
			startIndex = endIndex;		
		}

		// k-way merge
		
		// Start curPos pointer for each sub-array at location 0
		for (int i = 0; i < K; i++) {
			curPosSet(i, 0);
		}
		
		long lastSubArraySize = numElems - subArraySize*(K-1);
				
		long p = 0;
		while(p < numElems) {
			long min = Long.MAX_VALUE;
			long minSubArray = -1;
			
			// Search for the min element
			for (int i = 0; i < K; i++) {
				long curPos = curPosGet(i);
				
				// If we haven't run off the end of the subarray
				if((i < K-1 && curPos < subArraySize) ||
					(i == K-1 && curPos < lastSubArraySize)) {
					
					long index = lowIndex + (i * subArraySize) + curPos;
					if(cache.get(index) < min) {
						min = cache.get(index);
						minSubArray = i;
					}
				}	
			}
			
			mergedArraySet(p, min);			
			curPosSet(minSubArray, curPosGet(minSubArray) + 1);
			p += 1;
		}
		
		//Copy sorted array back to original location
		for(int i = 0; i < numElems; i++) {
			cache.set(lowIndex + i, mergedArrayGet(i));
		}
	}
	
	private void mergedArraySet(long index, long s) throws IOException {
		cache.set(numElems() + index, s);
	}
	
	private long mergedArrayGet(long index) throws IOException {
		return cache.get(numElems() + index);
	}
	
	private void curPosSet(long index, long s) throws IOException {
		cache.set(2*numElems() + index, s);
	}
	
	private long curPosGet(long index) throws IOException {
		return cache.get(2*numElems() + index);
	}	

	public void quickAux(long lowIndex, long highIndex) throws IOException {

		// at least one item must exist in the array
		if (lowIndex >= highIndex) {
			return;
		}

		long pivotIndex = getMedianIndexAsPivotIndex(lowIndex, highIndex);
		// 1) Choose pivot from the sublist
		long pivot = cache.get(pivotIndex);
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
			} while (cache.get(i).compareTo(pivot) < 0);
			// we don't have the protection as the previous loop.
			// So, add extra condition to prevent 'j' from overflowing outside
			// the current sub array
			do {
				j--;
			} while (cache.get(j).compareTo(pivot) > 0 && (j > lowIndex));

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
		final long tempItem = cache.get(firstItem);
		cache.set(firstItem, cache.get(secondItem));
		cache.set(secondItem, tempItem);
	}

	// Variation 1 - chose median as pivot
	private long getMedianIndexAsPivotIndex(long lowIndex, long highIndex) {
		return lowIndex + ((highIndex - lowIndex) / 2);
	}
}
