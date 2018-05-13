package minicp.search.selector.variable;

import minicp.util.IntArrayList;

import java.util.Random;

public class RandomTieBreaker implements TieBreaker {

    Random rand;

    public RandomTieBreaker(long seed) {
        rand = new Random(seed);
    }

    public RandomTieBreaker() {
        this(0L);
    }


    @Override
    public int breakTies(IntArrayList values) {
        return values.get(rand.nextInt(values.size()));
    }
}
