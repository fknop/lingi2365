package minicp.search.selector.variable;

import minicp.util.IntArrayList;

import java.util.Random;

public class FirstTieBreaker implements TieBreaker {


    @Override
    public int breakTies(IntArrayList values) {
        return values.get(0);
    }
}
