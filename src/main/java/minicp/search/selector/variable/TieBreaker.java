package minicp.search.selector.variable;

import minicp.util.IntArrayList;

public interface TieBreaker {
    int breakTies(IntArrayList values);
}
