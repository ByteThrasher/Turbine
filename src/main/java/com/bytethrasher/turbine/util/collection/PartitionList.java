package com.bytethrasher.turbine.util.collection;

import java.util.AbstractList;
import java.util.List;

public class PartitionList<T> extends AbstractList<List<T>> {

    private final List<T> list;
    private final int size;

    public PartitionList(List<T> list, int size) {
        this.list = list;
        this.size = size;
    }

    @Override
    public List<T> get(int index) {
        int start = index * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    @Override
    public int size() {
        return Math.ceilDiv(list.size(), size);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
