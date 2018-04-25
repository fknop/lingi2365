package minicp.search.selector.value;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.InconsistencyException;

import static minicp.cp.Factory.equal;

public class IBS implements ValueSelector {

    private int boundTreshold;
    private int[] domain;
    private IntVar[] x;
    private IntVar objective;

    public IBS(IntVar objective, IntVar[] x) {
        this(objective, x, 10);
    }

    public IBS(IntVar objective, IntVar[] x, int threshold) {
        this.x = x;
        this.objective = objective;
        this.boundTreshold = threshold;
        this.domain = new int[boundTreshold];
    }

    @Override
    public int getValue(IntVar[] x, int index) {
        IntVar var = x[index];
        double bestImpact = Integer.MAX_VALUE;
        int bestValue = var.getMin();

        if (var.getSize() > boundTreshold) {
            double impact = impact(x, index, var.getMin());
            if (impact < bestImpact) {
                bestImpact = impact;
                bestValue = var.getMin();
            }

            impact = impact(x, index, var.getMax());
            if (impact < bestImpact) {
                bestValue = var.getMax();
            }
        }
        else {
            int size = var.fillArray(domain);

            for (int i = 0; i < size; ++i) {
                int v = domain[i];
                double impact = impact(x, index, v);
                if (impact < bestImpact) {
                    bestImpact = impact;
                    bestValue = v;
                }
            }
        }

        return bestValue;
    }

    private double impact(IntVar[] x, int index, int v) {

        IntVar var = x[index];
        Solver solver = var.getSolver();
        try {
            int before = objective.getMax();
            solver.push();
            equal(var, v);
            int after = objective.getMax();
            return ((double) after / (double) before);
        }
        catch (InconsistencyException e) {
            return Double.POSITIVE_INFINITY;
        }
        finally {
            solver.pop();
        }
    }
}
