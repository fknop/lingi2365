package minicp.util;

import java.util.ArrayList;
import java.util.List;

public class Sequence {
    public static Iterable<Integer> sequence(int start, int end) {
        List<Integer> seq = new ArrayList<>();
        for (int i = 0; i < end; i++) {
            seq.add(i);
        }
        return seq;
    }
}
