package minicp.engine.core;

import minicp.util.Box;

import java.util.ArrayList;
import java.util.List;

public interface Var {
    List<Constraint> constraints = new ArrayList<>();

    Box<Integer> weightedDegree = new Box<>(0);


    default void register(Constraint constraint) {
        constraints.add(constraint);
    }

    default int getDegree() {
        return constraints.size();
    }

    default void updateWeight(int old, int n) {
        weightedDegree.set(weightedDegree.get() - old + n);
    }


    default int getWeightedDegree() {
        return weightedDegree.get() == 0 ? 1 : weightedDegree.get();
    }
}
