package minicp.engine.core;

import java.util.ArrayList;
import java.util.List;

public interface Var {
    List<Constraint> constraints = new ArrayList<>();

    default void register(Constraint constraint) {
        constraints.add(constraint);
    }

    default int getDegree() {
        return constraints.size();
    }

    default int getWeightedDegree() {
        int degree = 0;
        for (Constraint c: constraints) {
            degree += c.getFailureCount();
        }

        return degree;
    }
}
