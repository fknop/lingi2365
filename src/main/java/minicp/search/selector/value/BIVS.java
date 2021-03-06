package minicp.search.selector.value;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.InconsistencyException;

import static minicp.cp.Factory.equal;

public class BIVS implements ValueSelector {

    private int boundTreshold;
    private int[] domain;
    private IntVar objective;


    public BIVS(IntVar objective) {
        this(objective, 10);
    }

    public BIVS(IntVar objective, int threshold) {
        this.objective = objective;
        this.boundTreshold = threshold;
        this.domain = new int[boundTreshold];
    }

    @Override
    public int getValue(IntVar[] x, int index) {
        IntVar var = x[index];
        int bestBound = Integer.MAX_VALUE;
        int bestValue = var.getMin();

        if (var.getSize() > boundTreshold) {
            int bound = bound(var, var.getMin());
            if (bound < bestBound) {
                bestBound = bound;
                bestValue = var.getMin();
            }

            bound = bound(var, var.getMax());
            if (bound < bestBound) {
                bestValue = var.getMax();
            }
        }
        else {
            int size = var.fillArray(domain);

            for (int i = 0; i < size; ++i) {
                int v = domain[i];
                int bound = bound(var, v);
                if (bound < bestBound) {
                    bestBound = bound;
                    bestValue = v;
                }
            }
        }

        return bestValue;
    }

    private int bound(IntVar var, int v) {
        Solver solver = var.getSolver();
        try {
            solver.push();
            equal(var, v);
            return objective.getMax();
        }
        catch (InconsistencyException e) {
            return Integer.MAX_VALUE;
        }
        finally {
            solver.pop();
        }
    }
}
