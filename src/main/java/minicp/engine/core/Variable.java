package minicp.engine.core;

import minicp.util.Box;
import minicp.util.BoxInt;

import java.util.ArrayList;
import java.util.List;


public interface Variable {
    List<Constraint> constraints = new ArrayList<>();

    default List<Constraint> getConstraints() {
        return constraints;
    }

    default void register(Constraint constraint) {
        constraints.add(constraint);
    }

    default int getDegree() {
        return constraints.size();
    }

//    default double getFailures() {
//        double w = 1;
//        int size = constraints.size();
//        for (int i = 0; i < size; ++i) {
//            w += constraints.get(i).getWeight();
//        }
//
//        return w;
//    }

    void whenBind(ConstraintClosure.Filtering c);
    boolean isBound();
}
