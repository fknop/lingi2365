package minicp.search.selector.value;

import minicp.engine.core.IntVar;

import java.util.Random;

public class RandomValue implements ValueSelector {

    private Random rand = new Random(0);
    private int domain[] = null;

    private void setupDomain(IntVar x) {
        if (domain == null || x.getSize() > domain.length) {
            domain = new int[x.getSize()];
        }
    }

    @Override
    public int getValue(IntVar[] x, int index) {

        setupDomain(x[index]);

        int size = x[index].fillArray(domain);
        int i = rand.nextInt(size);
        return domain[i];
    }
}
