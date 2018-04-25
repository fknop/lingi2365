package minicp.search.heuristic;

import minicp.engine.core.IntVar;
import minicp.search.Choice;
import minicp.search.branching.Branching;
import minicp.search.selector.value.ValueSelector;
import minicp.search.selector.variable.VariableEvaluator;
import minicp.search.selector.variable.VariableFilter;
import minicp.search.selector.variable.VariableSelector;

//public interface Heuristic extends VariableFilter<IntVar>, VariableSelector<IntVar>, VariableEvaluator<IntVar>, ValueSelector, Branching<IntVar> {
//    default Choice branch(IntVar[] x) {
//        return branch(x, this, this);
//    }
//}
