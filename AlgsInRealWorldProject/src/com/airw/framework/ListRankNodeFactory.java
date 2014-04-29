package com.airw.framework;

import java.util.Comparator;

public class ListRankNodeFactory implements CacheObjectFactory<ListRankNode> {

    @Override
    public ListRankNode createCacheObject(String s) {
        String[] comps = s.split(" ");
        int id = Integer.parseInt(comps[0]);
        int next = Integer.parseInt(comps[1]);
        int weight = Integer.parseInt(comps[2]);
        int rank = Integer.parseInt(comps[3]);
        int next_next = Integer.parseInt(comps[4]);
        boolean flag = Boolean.parseBoolean(comps[5]);
        return new ListRankNode(id, next, weight, rank, next_next, flag);
    }

    @Override
    public Comparator<ListRankNode> getBasicComparator() {
        return null;
    }

    public Comparator<ListRankNode> addressComparator() {
        return new Comparator<ListRankNode>() {
            public int compare(ListRankNode a, ListRankNode b) {
                return Integer.compare(a.id, b.id);
            }
        };
    }

    public Comparator<ListRankNode> nextAddressComparator() {
        return new Comparator<ListRankNode>() {
            public int compare(ListRankNode a, ListRankNode b) {
                return Integer.compare(Math.abs(a.next), Math.abs(b.next));
            }
        };
    }

    public Comparator<ListRankNode> nextNextAddressComparator() {
        return new Comparator<ListRankNode>() {
            public int compare(ListRankNode a, ListRankNode b) {
                return Integer.compare(Math.abs(a.next_next), Math.abs(b.next_next));
            }
        };
    }

    public Comparator<ListRankNode> rankComparator() {
        return new Comparator<ListRankNode>() {
            public int compare(ListRankNode a, ListRankNode b) {
                return Integer.compare(a.rank, b.rank);
            }
        };
    }

}
