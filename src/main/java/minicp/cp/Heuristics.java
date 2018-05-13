/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.cp;


import minicp.engine.core.IntVar;
import minicp.search.Choice;
import minicp.search.ChoiceCombinator;
import minicp.search.branching.Branching;
import minicp.search.branching.FirstFailBranching;
import minicp.search.selector.value.MinValue;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.Dom;
import minicp.search.selector.variable.FirstTieBreaker;
import minicp.search.selector.variable.VariableSelector;


public class Heuristics {



    public static Choice firstFail(IntVar... x) {
        Branching<IntVar> branching = new FirstFailBranching(x, new Dom(), new MinValue());
        return branching.branch();
    }

    public static Choice firstFailNoTieBreaker(IntVar... x) {
        Dom dom = new Dom();
        dom.setTieBreaker(new FirstTieBreaker());
        Branching<IntVar> branching = new FirstFailBranching(x, dom, new MinValue());
        return branching.branch();
    }

    public static Choice firstFail(IntVar[] x, VariableSelector<IntVar> varSelector, ValueSelector valSelector) {
        Branching<IntVar> branching = new FirstFailBranching(x, varSelector, valSelector);
        return branching.branch();
    }


//    public static Choice firstFail(IntVar... x) {
//        return buildHeuristic(x,
//                domSizeHeuristic,
//                branchHeuristic
//        );
//    }

//    public static Choice buildHeuristic(IntVar[] x, Selector.ValueFun<IntVar> value, Selector.BranchOn<IntVar> branch) {
//        return selectMin(x,
//            filterUnbound,
//            value,
//            branch
//        );
//    }
//
//    public static Choice lastSuccess(IntVar[] x, Selector.ValueFunIndexed<IntVar> value) {
//        final int[] lastValues = new int[x.length];
//        for (int i = 0; i < x.length; ++i) {
//            lastValues[i] = x[i].getMin();
//        }
//
//        return selectMinIndexed(x,
//                filterUnbound,
//                value,
//                (IntVar xi, int i) -> {
//                    int last = lastValues[i];
//                    if (!x[i].contains(last)) {
//                        last = x[i].getMin(); // fallback
//                    }
//
//                    final int v = last;
//                    return branch(
//                        () -> {
//                            equal(xi, v);
//                            lastValues[i] = v;
//                        },
//                        () -> {
//                            notEqual(xi, v);
//                        }
//                    );
//                });
//    }
//
//    public static Choice lastSuccessConflict(IntVar[] x) {
//        final int[] lastValues = new int[x.length];
//
//        final Box<IntVar> lastConflict = new Box<>(null);
//
//        for (int i = 0; i < x.length; ++i) {
//            lastValues[i] = x[i].getMin();
//        }
//
//        return selectMinIndexed(x,
//                filterUnbound,
//                (IntVar xi, int i) -> {
//                    if (lastConflict.get() == xi) {
//                        return Float.NEGATIVE_INFINITY;
//                    }
//                    else {
//                        return domDivDegreeHeuristic.call(xi);
//                    }
//                },
//                (IntVar xi, int i) -> {
//                    int last = lastValues[i];
//                    if (!x[i].contains(last)) {
//                        last = x[i].getMin(); // fallback
//                    }
//
//                    final int v = last;
//                    return branch(
//                            () -> {
//                                IntVar tmp = lastConflict.get();
//                                lastConflict.set(xi);
//                                equal(xi, v);
//                                lastConflict.set(tmp);
//                                lastValues[i] = v;
//                            },
//                            () -> {
//                                notEqual(xi, v);
//                            }
//                    );
//                });
//    }
//
//
//    public static Selector.ValueFun<IntVar> domDivDegreeHeuristic = (IntVar xi) -> (float) xi.getSize() / (float) xi.getDegree();
//    public static Selector.ValueFun<IntVar> domPlusDegreeHeuristic = (IntVar xi) -> xi.getSize() + xi.getDegree();
//    public static Selector.ValueFunIndexed<IntVar> domPlusDegreeHeuristicIndexed = (IntVar xi, int i) -> xi.getSize() + xi.getDegree();
//    public static Selector.ValueFun<IntVar> domPlusWeightedDegreeHeuristic = (IntVar xi) -> xi.getSize() + xi.getWeightedDegree();
//    public static Selector.ValueFun<IntVar> wdegHeuristic = (IntVar xi) -> {
//        int wdeg = xi.getWeightedDegree();
//        if (wdeg == 0) {
//            return 100_000_000f;
//        }
//        else {
//            return (float) xi.getSize() / (float) wdeg;
//        }
////    };
//
//    public static Selector.ValueFun<IntVar> domSizeHeuristic = IntVar::getSize;
//
//    public static Selector.Filter<IntVar> filterUnbound = (IntVar xi) -> xi.getSize() > 1;
//
//    public static Selector.BranchOn<IntVar> branchHeuristic = (IntVar xi) -> {
//        int v = xi.getMin();
//        return branch(
//                () -> equal(xi,v),
//                () -> notEqual(xi,v)
//        );
//    };


    /**
     *
     * @param choices
     * @return A choice that is only returns an empty alternative when all the choices return empty, otherwise it
     *         return the alternatives generated by the first non empty one.
     */
    public static Choice and(Choice ... choices) {
        return new ChoiceCombinator(choices);
    }


}
