package minicp.search.branching;

import minicp.engine.core.Variable;
import minicp.search.Choice;

public class DiscrepancyBranching<V extends Variable> implements Branching<V> {

    private Branching<V> branching;
    private int maxDiscrepancy;
    private int discrepancy = 0;

    public DiscrepancyBranching(Branching<V> branching, int maxDiscrepancy) {
        this.branching = branching;
        this.maxDiscrepancy = maxDiscrepancy;

        assert(maxDiscrepancy > 0);
    }

    @Override
    public Choice branch() {
        return () -> {

            Choice choice = branching.branch();
            Branch[] branches = choice.call();
            if (branches.length == 0) {
                return branches;
            }
            else {
                int k = Math.min(maxDiscrepancy - discrepancy + 1, branches.length);
                Branch[] mapped = new Branch[k];
                int i = 0;
                while (i < k) {
                    Branch branch = branches[i];
                    final int newDiscrepancy = discrepancy + i;
                    mapped[i] = () -> {
                        discrepancy = newDiscrepancy;
                        branch.call();
                    };

                    i += 1;
                }


                return mapped;
            }
        };
    }
}
