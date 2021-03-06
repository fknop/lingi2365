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


import minicp.util.IntStack;

public class ReversibleBool implements Reversible {

//    final TrailEntry restoreTrue = new TrailEntry() {
//        @Override
//        public void restore() {
//            v = true;
//        }
//    };
//
//    final TrailEntry restoreFalse = new TrailEntry() {
//        @Override
//        public void restore() {
//            v = false;
//        }
//    };

    private boolean v;
    private Trail context;
    private long lastMagic;

    private IntStack trailEntries;

    public ReversibleBool(Trail context, boolean initial) {
        this.context = context;
        v = initial;
        lastMagic = context.magic;
        trailEntries = new IntStack();
    }

    private void trail() {
        long contextMagic = context.magic;
        if (lastMagic != contextMagic) {
            lastMagic = contextMagic;
            trailEntries.push(v ? 1 : 0);
            context.pushOnTrail(this);

//            if (v) context.pushOnTrail(restoreTrue);
//            else context.pushOnTrail(restoreFalse);
        }
    }

    public void setValue(boolean v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }
    }

    public boolean getValue() { return this.v; }

    public void restore() {
        v = trailEntries.pop() == 1;
//        v = !v;
    }
}
