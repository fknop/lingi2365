package minicp.engine.core.delta;

public interface DeltaInt extends Delta {
    boolean changed();
    int deltaSize();
    boolean minChanged();
    boolean maxChanged();
    int[] values();
    int fillArray(int[] values);
}
