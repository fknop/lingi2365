package minicp.util;

public class IntStack {
    private IntArrayList values;


    public IntStack() {
        values = new IntArrayList();
    }

    public IntStack(int capacity) {
        values = new IntArrayList(capacity);
    }

    public int peek() {
        return values.last();
    }

    public int pop() {
        return values.removeLast();
    }

    public void push(int value) {
        values.add(value);
    }

    public int size() {
        return values.size();
    }
}
