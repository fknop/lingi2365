package minicp.util;

import jdk.nashorn.internal.runtime.arrays.ArrayIndex;

import java.util.Arrays;
import java.util.Iterator;

public class IntArrayList {

    final static int DEFAULT_CAPACITY = 10;

    private int[] data;
    private int end = 0;

    public IntArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public IntArrayList(int capacity) {
        this.data = new int[capacity];
    }

    public int size() {
        return end;
    }

    public boolean add(int value) {
        ensureCapacity(end + 1);
        data[end++] = value;
        return true;
    }

    public int get(int pos) {
        if (pos >= end) {
            throw new ArrayIndexOutOfBoundsException(pos);
        }

        return data[pos];
    }

    public void clear() {
        end = 0;
    }


    public int removeLast() {
        return data[--end];
    }

    public boolean isEmpty() {
        return end == 0;
    }

    public int first() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException(0);
        }

        return data[0];
    }

    public int last() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException(0);
        }

        return data[end - 1];
    }

    private void ensureCapacity(int capacity) {
        if (capacity > data.length) {
            int newCapacity = Math.max(data.length << 1, capacity);
            int[] tmp = new int[newCapacity];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
    }


}
