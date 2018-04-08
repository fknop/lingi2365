package minicp.reversible;

import minicp.engine.core.IntVar;
import minicp.engine.core.delta.DeltaInt;

public class ReversibleDeltaInt implements DeltaInt {


    class DeltaTrailEntry implements TrailEntry {

        private int oldMin;
        private int oldMax;
        private int oldSize;

        DeltaTrailEntry(int oldMin, int oldMax, int oldSize) {
            this.oldMin = oldMin;
            this.oldMax = oldMax;
            this.oldSize = oldSize;
        }

        @Override
        public void restore() {
            ReversibleDeltaInt.this.oldMin = this.oldMin;
            ReversibleDeltaInt.this.oldMax = this.oldMax;
            ReversibleDeltaInt.this.oldSize = this.oldSize;
        }
    }


    private Trail trail;
    private Long lastMagic = -1L;


    private IntVar x;
    private int oldMin;
    private int oldMax;
    private int oldSize;

    public ReversibleDeltaInt(Trail trail, IntVar x) {
        this.trail = trail;
        lastMagic = trail.magic;

        this.x = x;
        this.oldMin = x.getMin();
        this.oldMax = x.getMax();
        this.oldSize = x.getSize();
    }

    @Override
    public void update() {
        if (changed()) {
            trail();
        }

        oldMin = x.getMin();
        oldMax = x.getMax();
        oldSize = x.getSize();
    }

    private void trail() {
        long trailMagic = trail.magic;
        if (lastMagic != trailMagic) {
            lastMagic = trailMagic;
            trail.pushOnTrail(new DeltaTrailEntry(oldMin, oldMax, oldSize));
        }
    }

    public boolean changed() {
        return oldSize != x.getSize();
    }

    public int deltaSize() {
        return oldSize - x.getSize();
    }

    public boolean minChanged() {
        return oldMin != x.getMin();
    }

    public boolean maxChanged() {
        return oldMax != x.getMax();
    }

    public int[] values() {
        return x.delta(oldSize);
    }

    public int fillArray(int[] values) {
        return x.fillDelta(values, oldSize);
    }


}
