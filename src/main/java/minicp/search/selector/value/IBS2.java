package minicp.search.selector.value;

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.InconsistencyException;

import java.util.Arrays;

import static minicp.cp.Factory.equal;
import static minicp.cp.Factory.greater;

public class IBS2 implements ValueSelector {

    private int boundTreshold;
    private int[] domain;
    private IntVar[] x;
    private IntVar objective;

    private double bestImpact;
    private int bestValue;

    public IBS2(IntVar objective, IntVar[] x) {
        this(objective, x, 10);
    }

    public IBS2(IntVar objective, IntVar[] x, int threshold) {
        this.x = x;
        this.objective = objective;
        this.boundTreshold = threshold;
        this.domain = new int[boundTreshold];
    }

    @Override
    public int getValue(IntVar[] x, int index) {
        IntVar var = x[index];
        bestImpact = Double.MAX_VALUE;
        bestValue = var.getMin();

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
        } else {
            int size = var.fillArray(domain);
            Arrays.sort(domain, 0, size);
            assert (domain[0] <= domain[1]);

            binarySearch(domain, index, 0, size - 1);

        }

        return bestValue;
    }

    private void binarySearch(int[] domain, int index, int min, int max) {
        int i = (min + max) / 2;

        double impact = impact(x, index, domain[i]);

        if (min >= max) {
            if (impact < bestImpact) {
                bestImpact = impact;
                bestValue = domain[i];
            }

            return;
        }


        if (impact < bestImpact) {
            bestImpact = impact;
            bestValue = domain[i];
        }

        double impactLess = impactLess(x, index, domain[i]);
        if (impactLess < bestImpact) {
            bestImpact = impactLess;
            binarySearch(domain, index, min, i - 1);
        }

        double impactGreater = impactGreater(x, index, domain[i]);
        if (impactGreater < bestImpact) {
            bestImpact = impactGreater;
            binarySearch(domain, index, i + 1, max);
        }
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

    private double impactLess(IntVar[] x, int index, int v) {

        IntVar var = x[index];
        Solver solver = var.getSolver();
        try {
            int before = objective.getMax();
            solver.push();
            Factory.less(var, v);
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

    private double impactGreater(IntVar[] x, int index, int v) {

        IntVar var = x[index];
        Solver solver = var.getSolver();
        try {
            int before = objective.getMax();
            solver.push();
            greater(var, v);
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
