package com.airw.listranking;

import java.io.IOException;
import java.util.Random;

import com.airw.arrays.EmptyCacheArray;
import com.airw.cache.CacheArray;
import com.airw.framework.ListRankNode;
import com.airw.framework.ListRankNodeFactory;
import com.airw.sorts.QuickSort;

public class ListRanking {

    /**
     * A simple function from computing the list ranking using the naive method.
     * 
     * @param thelist
     *            The input list. Expects to be sorted in id order and id's
     *            correspond to the index in the array.
     * @throws IOException
     */
    public static void rankListNaive(CacheArray<ListRankNode> thelist)
            throws IOException {
        long startIndex = findStartIndex(thelist);
        ListRankNode cur = thelist.get(startIndex);
        cur.rank = 0;
        thelist.set(startIndex, cur);
        while (cur.next >= 0) {
            int curank = cur.rank;
            int w = cur.weight;
            long indx = findIndex(thelist, cur.next);
            cur = thelist.get(indx);
            cur.rank = curank + w;
            thelist.set(indx, cur);
        }
    }

    /**
     * The cache efficient method for computing the list ranking using the
     * method presented in lecture.
     * 
     * @param thelist
     *            The input list. Expects to be sorted in id order and id's
     *            correspond to the index in the array. All ranks initially set
     *            to Integer.maxValue().
     * @throws IOException
     */
    public static void rankList(CacheArray<ListRankNode> thelist)
            throws IOException {
        recursiveListRank(thelist);
    }

    /**
     * The recursive cache efficient solution.
     * 
     * @param thelist
     *            The list to sort.
     * @throws IOException
     */
    public static void recursiveListRank(CacheArray<ListRankNode> thelist)
            throws IOException {

        // The list fits in cache so everything is free.
        if (thelist.size() <= thelist.getCache().getBlockSize()) {
            long startIndex = findStartIndex(thelist);
            ListRankNode cur = thelist.get(startIndex);
            cur.rank = 0;
            thelist.set(startIndex, cur);
            while (cur.next >= 0) {
                int curank = cur.rank;
                int w = cur.weight;
                long indx = findIndex(thelist, cur.next);
                cur = thelist.get(indx);
                cur.rank = curank + w;
                thelist.set(indx, cur);
            }
        } else {
            // label independent set
            labelIndependentSet(thelist);

            // bride out
            CacheArray<ListRankNode> subproblem = bridgeOut(thelist);

            // recurse
            recursiveListRank(subproblem);

            mergeArray(thelist, subproblem);

            subproblem.close();
        }
    }

    /**
     * Find the index with the matching id.
     * 
     * @param thelist
     * @param id
     * @return
     * @throws IOException
     */
    private static long findIndex(CacheArray<ListRankNode> thelist, int id)
            throws IOException {
        for (long i = 0; i < thelist.size(); i++) {
            ListRankNode cur = thelist.get(i);
            if (cur.id == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the start index of a list
     * 
     * @param thelist
     * @return
     * @throws IOException
     */
    private static long findStartIndex(CacheArray<ListRankNode> thelist)
            throws IOException {
        int id = -1;
        for (long i = 0; i < thelist.size(); i++) {
            ListRankNode cur = thelist.get(i);
            if (cur.next < 0) {
                id = -cur.next;
                break;
            }
        }
        for (long i = 0; i < thelist.size(); i++) {
            ListRankNode cur = thelist.get(i);
            if (cur.id == id) {
                return i;
            }
        }
        return -1;

    }

    /**
     * Labels the independent set using flags.
     * 
     * @param list
     * @throws IOException
     */
    private static int labelIndependentSet(CacheArray<ListRankNode> list)
            throws IOException {

        Random gen = new Random();
        ListRankNodeFactory lrnf = new ListRankNodeFactory();
        // Copy the array and set some flags
        CacheArray<ListRankNode> listCopy = new EmptyCacheArray<ListRankNode>(
                lrnf, list.size(), list.getCache());
        for (long i = 0; i < list.size(); i++) {
            ListRankNode cur = list.get(i);
            cur.flag = gen.nextBoolean();
            ListRankNode cop = new ListRankNode(cur.id, cur.next, cur.weight,
                    cur.rank, cur.next_next, cur.flag);
            list.set(i, cur);
            listCopy.set(i, cop);
        }

        QuickSort<ListRankNode> qsCopy = new QuickSort<ListRankNode>(listCopy,
                lrnf.nextAddressComparator());
        qsCopy.sort();

        int c = 0;
        for (long i = 0; i < list.size(); i++) {
            ListRankNode top = list.get(i);
            ListRankNode bot = listCopy.get(i);

            if (top.flag && !bot.flag && bot.next >= 0) {
                top.flag = true;
                c++;
            } else {
                top.flag = false;
            }
            list.set(i, top);
        }
        listCopy.close();
        return c;
    }

    /**
     * Bride out the nodes that are flag.
     * 
     * @param labeledArray
     * @return
     * @throws IOException
     */
    private static CacheArray<ListRankNode> bridgeOut(
            CacheArray<ListRankNode> labeledArray) throws IOException {
        ListRankNodeFactory lrnf = new ListRankNodeFactory();

        // Copy the list twice
        CacheArray<ListRankNode> midCopy = new EmptyCacheArray<ListRankNode>(
                lrnf, labeledArray.size(), labeledArray.getCache());
        CacheArray<ListRankNode> botCopy = new EmptyCacheArray<ListRankNode>(
                lrnf, labeledArray.size(), labeledArray.getCache());
        long indepSize = 0;
        for (long i = 0; i < labeledArray.size(); i++) {
            ListRankNode cur = labeledArray.get(i);
            if (cur.flag) {
                indepSize++;
            }
            ListRankNode cop = new ListRankNode(cur.id, cur.next, cur.weight,
                    cur.rank, cur.next_next, cur.flag);
            ListRankNode cop2 = new ListRankNode(cur.id, cur.next, cur.weight,
                    cur.rank, cur.next_next, cur.flag);
            midCopy.set(i, cop);
            botCopy.set(i, cop2);
        }

        QuickSort<ListRankNode> qsMid = new QuickSort<ListRankNode>(midCopy,
                lrnf.nextAddressComparator());
        qsMid.sort();
        QuickSort<ListRankNode> qsBot = new QuickSort<ListRankNode>(botCopy,
                lrnf.nextNextAddressComparator());
        qsBot.sort();

        for (long i = 0; i < labeledArray.size(); i++) {
            ListRankNode top = labeledArray.get(i);
            ListRankNode mid = midCopy.get(i);
            ListRankNode bot = botCopy.get(i);

            if (!top.flag && mid.flag && !bot.flag) {
                int x = bot.weight;
                int y = mid.weight;
                bot.next = mid.next;
                bot.weight = x + y;
                botCopy.set(i, bot);
            }
        }

        midCopy.close();

        // identify next_nexts
        CacheArray<ListRankNode> filtered = new EmptyCacheArray<ListRankNode>(
                lrnf, labeledArray.size() - indepSize, labeledArray.getCache());
        CacheArray<ListRankNode> filteredCopy = new EmptyCacheArray<ListRankNode>(
                lrnf, labeledArray.size() - indepSize, labeledArray.getCache());
        long c = 0;
        for (long i = 0; i < labeledArray.size(); i++) {
            ListRankNode cur = botCopy.get(i);
            if (!cur.flag) {
                ListRankNode cop = new ListRankNode(cur.id, cur.next,
                        cur.weight, cur.rank, cur.next_next, cur.flag);
                ListRankNode cop2 = new ListRankNode(cur.id, cur.next,
                        cur.weight, cur.rank, cur.next_next, cur.flag);
                filtered.set(c, cop);
                filteredCopy.set(c, cop2);
                c++;
            }
        }

        botCopy.close();

        QuickSort<ListRankNode> filtSort = new QuickSort<ListRankNode>(
                filtered, lrnf.addressComparator());
        filtSort.sort();
        QuickSort<ListRankNode> filtCopySort = new QuickSort<ListRankNode>(
                filteredCopy, lrnf.nextAddressComparator());
        filtCopySort.sort();
        for (long i = 0; i < filtered.size(); i++) {
            ListRankNode cur = filteredCopy.get(i);
            cur.next_next = filtered.get(i).next;
            filteredCopy.set(i, cur);
        }
        filtered.close();

        filtCopySort = new QuickSort<ListRankNode>(filteredCopy,
                lrnf.addressComparator());
        filtCopySort.sort();
        return filteredCopy;
    }

    /**
     * Updates the original array to have ranks given a partial solution.
     * 
     * @param original
     * @param alreadyRanked
     * @throws IOException
     */
    public static void mergeArray(CacheArray<ListRankNode> original,
            CacheArray<ListRankNode> alreadyRanked) throws IOException {

        long arpointer = 0;
        for (long i = 0; i < original.size()
                && arpointer < alreadyRanked.size(); i++) {
            ListRankNode curOrig = original.get(i);
            ListRankNode curAldRanked = alreadyRanked.get(arpointer);
            if (curOrig.id == curAldRanked.id) {
                curOrig.rank = curAldRanked.rank;
                original.set(i, curOrig);
                arpointer++;
            }
        }

        // Sort by rank
        ListRankNodeFactory lrnf = new ListRankNodeFactory();
        QuickSort<ListRankNode> rankSort = new QuickSort<ListRankNode>(
                original, lrnf.rankComparator());
        rankSort.sort();

        // fill in the remaining
        for (long i = 0; i < original.size(); i++) {
            ListRankNode orig = original.get(i);
            if (orig.next >= 0) {
                // will likely be in cache.
                long indx = findIndex(original, orig.next);
                ListRankNode nextNode = original.get(indx);
                if (nextNode.rank == Integer.MAX_VALUE) {
                    nextNode.rank = orig.rank + orig.weight;
                    original.set(indx, nextNode);
                }
            }
        }

        // Sort by address again
        QuickSort<ListRankNode> addrSort = new QuickSort<ListRankNode>(
                original, lrnf.addressComparator());
        addrSort.sort();
    }

}
