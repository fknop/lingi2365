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
        int size = constraints.size();
        for (int i = 0; i < size; ++i) {
            degree += constraints.get(i).getFailureCount();
        }

        return degree;
    }
}
