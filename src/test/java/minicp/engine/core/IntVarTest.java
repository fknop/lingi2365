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

package minicp.engine.core;

import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static minicp.cp.Factory.*;


public class IntVarTest {

    public boolean propagateCalled = false;

    @Test
    public void testIntVar() {
        Solver cp  = makeSolver();

        IntVar x = makeIntVar(cp,10);
        IntVar y = makeIntVar(cp,10);

        cp.getTrail().push();


        try {

        assertFalse(x.isBound());
        x.remove(5);
        assertEquals(9,x.getSize());
        x.assign(7);
        assertEquals(1,x.getSize());
        assertTrue(x.isBound());
        assertEquals(7,x.getMin());
        assertEquals(7,x.getMax());

        } catch(InconsistencyException e) { fail("should not fail here");}

        try {
            x.assign(8);
            fail( "should have failed" );
        } catch (InconsistencyException expectedException) {}



        cp.getTrail().pop();
        cp.getTrail().push();

        assertFalse(x.isBound());
        assertEquals(10,x.getSize());

        for (int i = 0; i < 10; i++) {
            assertTrue(x.contains(i));
        }
        assertFalse(x.contains(-1));

    }

    @Test
    public void testIntVarView() {
        Solver cp  = makeSolver();

        IntVar x = plus(mul(plus(makeIntVar(cp,10),1),2),-1); // 2*(x+1)-1 = 2x+1 D(x)= {1,3,5,...,19}
        IntVar y = plus(minus(mul(plus(makeIntVar(cp,10),-1),2)),-1); // (-2*(x-1))-1 = -2x+1 D(x)= {-17,-15,..,1}

        cp.getTrail().push();


        try {

            assertFalse(x.isBound());
            assertEquals(x.getMin(),1);
            assertEquals(x.getMax(),19);
            assertFalse(x.contains(2));
            x.remove(5);
            assertFalse(x.contains(5));

            assertFalse(y.isBound());
            assertEquals(y.getMin(),-17);
            assertEquals(y.getMax(),1);
            assertFalse(x.contains(-2));
            x.remove(-5);
            assertFalse(x.contains(-5));

        } catch(InconsistencyException e) { fail("should not fail here");}



    }

    @Test
    public void onDomainChangeOnBind() {
        propagateCalled = false;
        Solver cp  = makeSolver();

        IntVar x = makeIntVar(cp,10);
        IntVar y = makeIntVar(cp,10);

        Constraint cons = new Constraint(cp) {

            @Override
            public void post() throws InconsistencyException {
                x.whenBind(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(8);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.assign(4);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            y.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            y.remove(9);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }

    @Test
    public void arbitraryRangeDomains() {

        try {

            Solver cp  = new Solver();

            IntVar x = makeIntVar(cp,-10,10);

            cp.push();


            try {

                assertFalse(x.isBound());
                x.remove(-9);
                x.remove(-10);


                assertEquals(19,x.getSize());
                x.assign(-4);
                assertEquals(1,x.getSize());
                assertTrue(x.isBound());
                assertEquals(-4,x.getMin());

            } catch(InconsistencyException e) { fail("should not fail here");}

            try {
                x.assign(8);
                fail( "should have failed" );
            } catch (InconsistencyException expectedException) {}


            cp.pop();

            assertEquals(21,x.getSize());

            for (int i = -10; i < 10; i++) {
                assertTrue(x.contains(i));
            }
            assertFalse(x.contains(-11));


        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }



    @Test
    public void arbitrarySetDomains() {

        try {

            Solver cp  = makeSolver();

            Set<Integer> dom = new HashSet<>(Arrays.asList(-7,-10,6,9,10,12));

            IntVar x = makeIntVar(cp,dom);

            cp.push();

            try {

                for (int i = -15; i < 15; i++) {
                    if (dom.contains(i)) assertTrue(x.contains(i));
                    else assertFalse(x.contains(i));
                }

                x.assign(-7);
            } catch(InconsistencyException e) { fail("should not fail here");}

            try {
                x.assign(-10);
                fail( "should have failed" );
            } catch (InconsistencyException expectedException) {}


            cp.pop();

            for (int i = -15; i < 15; i++) {
                if (dom.contains(i)) assertTrue(x.contains(i));
                else assertFalse(x.contains(i));
            }
            assertEquals(6,x.getSize());


        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void onBoundChange() {

        Solver cp = new Solver();

        IntVar x = makeIntVar(cp, 10);
        IntVar y = makeIntVar(cp, 10);

        Constraint cons = new Constraint(cp) {

            @Override
            public void post() throws InconsistencyException {
                x.whenBind(() -> propagateCalled  = true);
                y.whenBoundsChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(8);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.assign(4);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            assertFalse(y.contains(10));
            y.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            propagateCalled = false;
            y.remove(2);
            cp.fixPoint();
            assertFalse(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }



    @Test
    public void removeAbove() {

        try {

            Solver cp = new Solver();

            IntVar x = makeIntVar(cp, 10);

            Constraint cons = new Constraint(cp) {

                @Override
                public void post() throws InconsistencyException {
                    x.propagateOnBoundChange(this);
                }

                @Override
                public void propagate() throws InconsistencyException {
                    propagateCalled = true;
                }
            };

            try {
                cp.post(cons);
                x.remove(8);
                cp.fixPoint();
                assertFalse(propagateCalled);
                assertEquals(7,x.removeAbove(8));
                cp.fixPoint();
                assertTrue(propagateCalled);

            } catch (InconsistencyException inconsistency) {
                fail("should not fail");
            }

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void removeBelow() {

        try {

            Solver cp = new Solver();

            IntVar x = makeIntVar(cp, 10);

            Constraint cons = new Constraint(cp) {

                @Override
                public void post() throws InconsistencyException {
                    x.propagateOnBoundChange(this);
                }

                @Override
                public void propagate() throws InconsistencyException {
                    propagateCalled = true;
                }
            };

            try {
                cp.post(cons);
                x.remove(3);
                cp.fixPoint();
                assertFalse(propagateCalled);
                assertEquals(4,x.removeBelow(3));
                cp.fixPoint();
                assertTrue(propagateCalled);
                propagateCalled = false;

                assertEquals(5,x.removeBelow(5));
                cp.fixPoint();
                assertTrue(propagateCalled);
                propagateCalled = false;


            } catch (InconsistencyException inconsistency) {
                fail("should not fail");
            }

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testFillArray(){
        Solver cp = new Solver();

        IntVar x = makeIntVar(cp,-3,4);
        int[] domain = {-3,-2,-1,0,1,2,3,4};

        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        assertEquals(8, size);

        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
        }

    }

    @Test
    public void testFillArrayRemove(){
        Solver cp = new Solver();

        IntVar x = makeIntVar(cp,-3,4);
        int[] domain = {-3,-2,-1,4,3,2};
        try {
            x.remove(0);
            x.remove(1);
        } catch (InconsistencyException e) {
            fail("should not fail");
        }
        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
        }

    }



    @Test
    public void testFillArrayOffset(){
        Solver cp = new Solver();

        IntVar x = plus(makeIntVar(cp,-3,4),3);
        int[] domain = {0,1,2,3,4,5,6,7};

        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
        }
    }

    @Test
    public void testFillArrayOffsetRemove(){
        Solver cp = new Solver();

        IntVar x = plus(makeIntVar(cp,-3,4),3);
        int[] domain = {0,1,2,7,4,5};

        try {
            x.remove(3);
            x.remove(6);
        } catch (InconsistencyException e) {
            fail("should not fail");
        }

        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
        }
    }

    @Test
    public void testFillArrayMul(){
        Solver cp = new Solver();

        IntVar x = mul(mul(makeIntVar(cp,-3,4),-3),-1);
        int[] domain = {-9,-6,-3,0,3,6,9,12};


        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
    }

    }

    @Test
    public void testFillArrayMulRemove(){
        Solver cp = new Solver();

        IntVar x = mul(mul(makeIntVar(cp,-3,4),-3),-1);
        int[] domain = {-9,12,-3,0,9,6};

        try {
            x.remove(-6);
            x.remove(3);
        } catch (InconsistencyException e) {
            fail("should not fail");
        }

        int size = x.getSize();
        int[] a = new int[size];

        size = x.fillArray(a);
        for(int i = 0 ; i < size ; i++) {
            assertEquals(domain[i], a[i]);
        }

    }

    @Test
    public void fillArray() {

        try {
            Solver cp = new Solver();

            IntVar x = plus(mul(minus(makeIntVar(cp, 5)), 3), 5); // D(x)= {-7,-4,-1,2,5}
            int[] values = new int[10];
            int s = x.fillArray(values);
            HashSet<Integer> dom = new HashSet<Integer>();
            for (int i = 0; i < s; i++) {
                dom.add(values[i]);
            }
            System.out.println(x);
            System.out.println(dom);
            System.out.println(s);
            HashSet<Integer> expectedDom = new HashSet<Integer>();
            Collections.addAll(expectedDom, -7, -4, -1, 2, 5);
            assertEquals(expectedDom, dom);

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testDelta() {
        Solver solver = makeSolver();
        IntVar x = makeIntVar(solver, 10);
        try {
            x.remove(7);
            x.remove(3);

            int[] delta = x.delta(10);

            assertEquals(3, delta[0]);
            assertEquals(7, delta[1]);

        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }
}
