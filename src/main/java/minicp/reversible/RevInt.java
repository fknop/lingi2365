package minicp.reversible;

/**
 * Created by ldm on 2/22/17.
 */
public interface RevInt extends Reversible {
    int setValue(int v);
    int getValue();
    int increment();
    int decrement();
    @Override
    String toString();
}


