package minicp.engine.core;

import minicp.util.Box;
import minicp.util.BoxInt;

import java.util.ArrayList;
import java.util.List;


public interface Var {
    List<Constraint> constraints = new ArrayList<>();

    BoxInt weightedDegree = new BoxInt(0);


    default void register(Constraint constraint) {
        constraints.add(constraint);
    }

    default int getDegree() {
        return constraints.size();
    }

    default void addFailure() {
        ++weightedDegree.value;
    }


    default int getWeightedDegree() {
        return weightedDegree.value;
    }

    boolean isBound();
}
