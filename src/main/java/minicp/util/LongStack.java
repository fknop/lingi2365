package minicp.util;

public class LongStack {
    private LongArrayList values;


    public LongStack() {
        values = new LongArrayList();
    }

    public LongStack(int capacity) {
        values = new LongArrayList(capacity);
    }

    public long peek() {
        return values.last();
    }

    public long pop() {
        return values.removeLast();
    }

    public void push(long value) {
        values.add(value);
    }

    public int size() {
        return values.size();
    }
}
