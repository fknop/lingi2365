package minicp.util;

public class LongArrayList {

    final static int DEFAULT_CAPACITY = 10;

    private long[] data;
    private int end = 0;

    public LongArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public LongArrayList(int capacity) {
        this.data = new long[capacity];
    }

    public int size() {
        return end;
    }

    public boolean add(long value) {
        ensureCapacity(end + 1);
        data[end++] = value;
        return true;
    }

    public long get(int pos) {
        if (pos >= end) {
            throw new ArrayIndexOutOfBoundsException(pos);
        }

        return data[pos];
    }

    public void clear() {
        end = 0;
    }


    public long removeLast() {
        return data[--end];
    }

    public boolean isEmpty() {
        return end == 0;
    }

    public long first() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException(0);
        }

        return data[0];
    }

    public long last() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException(0);
        }

        return data[end - 1];
    }

    private void ensureCapacity(int capacity) {
        if (capacity > data.length) {
            int newCapacity = Math.max(data.length << 1, capacity);
            long[] tmp = new long[newCapacity];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
    }


}
