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
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.util;

import org.junit.Assume;
import org.junit.Assert;

public class NotImplementedExceptionAssume {
    public static boolean failOnNPE = System.getenv().getOrDefault("MINICP_TEST_FAIL_ON_NPE", "0").equals("1");

    public static void fail(NotImplementedException e) {
        if (failOnNPE)
            Assert.fail("NotImplementedException");
        else
            Assume.assumeNoException(e);
    }
}
