package minicp.reversible;

public interface RevLong {
    long setValue(long v);
    long getValue();
    long increment();
    long decrement();
    @Override
    String toString();
}


