package com.airw.sorts;

import java.io.IOException;

/**
 * Quick sort so taking into account cache efficiency.
 * 
 * @param <T>
 */
public class QuickSort extends Sort {

    public QuickSort(String fileName, int blockSize, int numBlocksInCache)
            throws IOException {
        super(fileName, blockSize, numBlocksInCache, 0.0);
    }

    @Override
    protected void sortCore() throws IOException {
        quickAux(0, numElems() - 1);
    }

    public void quickAux(long lowIndex, long highIndex) throws IOException {

        // at least one item must exist in the array
        if (lowIndex >= highIndex) {
            return;
        }

        long pivotIndex = getMedianIndexAsPivotIndex(lowIndex, highIndex);
        // 1) Choose pivot from the sublist
        Long pivot = cache.get(pivotIndex);
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
            // we dont have the protection as the previous loop.
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
        final Long tempItem = cache.get(firstItem);
        cache.set(firstItem, cache.get(secondItem));
        cache.set(secondItem, tempItem);
    }

    // Variation 1 - chose median as pivot
    private long getMedianIndexAsPivotIndex(long lowIndex, long highIndex) {
        return lowIndex + ((highIndex - lowIndex) / 2);
    }
}
