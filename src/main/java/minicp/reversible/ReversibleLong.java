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

package minicp.reversible;


import minicp.util.LongStack;

public class ReversibleLong implements RevLong {

    class TrailEntryLong implements TrailEntry {
        private final long v;
        public TrailEntryLong(long v) {
            this.v = v;
        }
        public void restore()       { ReversibleLong.this.v = v;}
    }


    private Trail trail;
    private long v;
    private long lastMagic = -1L;

    LongStack trailEntries;

    public ReversibleLong(Trail trail, long initial) {
        this.trail = trail;
        v = initial;
        lastMagic = trail.magic;
        trailEntries = new LongStack();
    }

    private void trail() {
        long trailMagic = trail.magic;
        if (lastMagic != trailMagic) {
            lastMagic = trailMagic;
            trailEntries.push(v);
            trail.pushOnTrail(this);
        }
    }

    @Override
    public void restore() {
        this.v = trailEntries.pop();
    }

    public long setValue(long v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }

        return this.v;
    }

    public long increment() { return setValue(getValue()+1);}
    public long decrement() { return setValue(getValue()-1);}
    public long getValue()  { return this.v; }

    public boolean isZero() {
        return this.v == 0L;
    }

    @Override
    public String toString() {
        return ""+v;
    }
}
