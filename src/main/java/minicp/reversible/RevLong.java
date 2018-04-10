package minicp.reversible;

public interface RevLong extends Reversible {
    long setValue(long v);
    long getValue();
    long increment();
    long decrement();
    @Override
    String toString();
}


