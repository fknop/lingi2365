//package minicp.search.heuristic;
//
//import minicp.engine.core.IntVar;
//import minicp.search.Choice;
//import minicp.search.branching.Branching;
//import minicp.search.selector.value.ValueSelector;
//import minicp.search.selector.variable.Dom;
//import minicp.search.selector.variable.VariableEvaluator;
//import minicp.search.selector.variable.VariableSelector;
//
//import static minicp.cp.Factory.equal;
//import static minicp.cp.Factory.notEqual;
//
//public class LastSuccessAndConflict implements Heuristic {
//
//    private VariableEvaluator<IntVar> fallback;
//    private ValueSelector valueSelector = null;
//    private IntVar lastConflict = null;
//    private boolean useLastSuccess = true;
//    private int[] lastValues;
//
//    public LastSuccessAndConflict() {
//        this(new Dom());
//    }
//
//    public LastSuccessAndConflict(VariableEvaluator<IntVar> fallback) {
//        this.fallback = fallback;
//    }
//
//    public LastSuccessAndConflict(VariableEvaluator<IntVar> fallback, ValueSelector valueSelector) {
//        this.fallback = fallback;
//        this.valueSelector = valueSelector;
//    }
//
//    public LastSuccessAndConflict(VariableEvaluator<IntVar> fallback, ValueSelector valueSelector, boolean useLastSuccess) {
//        this.fallback = fallback;
//        this.valueSelector = valueSelector;
//        this.useLastSuccess = useLastSuccess;
//    }
//
//
//    @Override
//    public int getVariable(IntVar[] variables) {
//        return VariableSelector.selectMinVariable(variables, this, this);
//    }
//
//    @Override
//    public double evaluate(IntVar[] x, int i) {
//        if (lastConflict == x[i]) {
//            return Double.NEGATIVE_INFINITY;
//        }
//        else {
//            return fallback.evaluate(x, i);
//        }
//    }
//
//    @Override
//    public Choice branch(IntVar[] x, VariableSelector<IntVar> variableSelector, ValueSelector valueSelector) {
//        lastValues = new int[x.length];
//        for (int i = 0; i < x.length; ++i) {
//            lastValues[i] = x[i].getMin();
//        }
//
//        return () -> {
//            final int index = variableSelector.getVariable(x);
//            if (index == -1) {
//                return LEAF;
//            }
//
//            IntVar var = x[index];
//
//            int value;
//            if (useLastSuccess) {
//                value = lastValues[index];
//                if (!x[index].contains(value)) {
//                    value = this.getValue(var);
//                }
//            }
//            else {
//                value = this.getValue(var);
//            }
//
//
//            int v = value;
//            return Branching.branch(() -> {
//                IntVar tmp = var;
//                lastConflict = var;
//                equal(var, v);
//                lastConflict = tmp;
//                lastValues[index] = v;
//            },
//            () -> {
//                notEqual(var, v);
//            });
//        };
//    }
//
//    @Override
//    public boolean take(IntVar var) {
//        return !var.isBound();
//    }
//
//
//    @Override
//    public int getValue(IntVar var) {
//        if (valueSelector == null) {
//            return var.getMin();
//        }
//        else {
//            return valueSelector.getValue(var);
//        }
//    }
//}
