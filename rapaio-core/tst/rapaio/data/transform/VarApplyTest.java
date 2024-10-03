/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

public class VarApplyTest {

    private static final double TOL = 1e-20;

    @Test
    void testApply() {

        VarTransform vf = VarApply.onSpot(spot -> {
            if (spot.isMissing())
                spot.setDouble(0);
            if (spot.getDouble() > 0)
                spot.setDouble(spot.getDouble() * spot.getDouble());
            else
                spot.setDouble(-spot.getDouble() * spot.getDouble());
        });

        Var x = VarDouble.wrap(0, Double.NaN, 1, Double.NaN, -12, 3.1);

        Var y = x.copy().fapply(vf);
        assertEquals(0, y.getDouble(0), 1e-20);
        assertEquals(0, y.getDouble(1), 1e-20);
        assertEquals(1, y.getDouble(2), 1e-20);
        assertEquals(0, y.getDouble(3), 1e-20);
        assertEquals(-144, y.getDouble(4), 1e-20);
        assertEquals(3.1 * 3.1, y.getDouble(5), 1e-20);

        Var l1 = VarNominal.copy("ana", "?", "are", "?", "mere");
        Var l2 = l1.fapply(VarApply.onSpot(s -> {
            if (s.isMissing()) {
                s.setLabel("missing");
                return;
            }

            char[] msg = s.getLabel().toCharArray();
            for (int i = 0; i < msg.length / 2; i++) {
                char tmp = msg[i];
                msg[i] = msg[msg.length - i - 1];
                msg[msg.length - i - 1] = tmp;
            }
            s.setLabel(String.copyValueOf(msg));
        }));

        assertEquals("ana", l2.getLabel(0));
        assertEquals("missing", l2.getLabel(1));
        assertEquals("era", l2.getLabel(2));
        assertEquals("missing", l2.getLabel(3));
        assertEquals("erem", l2.getLabel(4));
    }

    @Test
    void testApplyDouble() {
        VarTransform vf = VarApply.onDouble(x -> {
            if (Double.isNaN(x))
                return 0.0;
            return (x > 0) ? (x * x) : (-x * x);
        });

        Var x = VarDouble.wrap(0, Double.NaN, 1, Double.NaN, -12, 3.1);

        double[] a1 = new double[]{0, 0, 1, 0, -144, 3.1 * 3.1};
        double[] a2 = x.copy().fapply(vf).stream().mapToDouble().toArray();
        double[] a3 = x.stream().mapToDouble().toArray();
        double[] a4 = x.fapply(vf.newInstance()).stream().mapToDouble().toArray();

        assertArrayEquals(a1, a2, TOL);
        assertEquals(Double.NaN, a3[1], TOL);
        assertArrayEquals(a1, a4, TOL);
    }

    @Test
    void testApplyInt() {
        VarTransform vf = VarApply.onInt(x -> {
            if (x == Integer.MIN_VALUE) return 0;
            return (x > 0) ? (x * x) : (-x * x);
        });

        Var x = VarDouble.wrap(0, Double.NaN, 1, Double.NaN, -12, 3);

        Var y = x.copy().fapply(vf);
        assertEquals(0, y.getDouble(0), 1e-20);
        assertEquals(0, y.getDouble(1), 1e-20);
        assertEquals(1, y.getDouble(2), 1e-20);
        assertEquals(0, y.getDouble(3), 1e-20);
        assertEquals(-144, y.getDouble(4), 1e-20);
        assertEquals(9, y.getDouble(5), 1e-20);
    }

    @Test
    void testApplyLabel() {
        Var l1 = VarNominal.copy("ana", "?", "are", "?", "mere");
        Var l2 = l1.fapply(VarApply.onLabel(l -> {
            if (l.equals("?")) {
                return "missing";
            }

            char[] msg = l.toCharArray();
            for (int i = 0; i < msg.length / 2; i++) {
                char tmp = msg[i];
                msg[i] = msg[msg.length - i - 1];
                msg[msg.length - i - 1] = tmp;
            }
            return String.copyValueOf(msg);
        }));

        assertEquals("ana", l2.getLabel(0));
        assertEquals("missing", l2.getLabel(1));
        assertEquals("era", l2.getLabel(2));
        assertEquals("missing", l2.getLabel(3));
        assertEquals("erem", l2.getLabel(4));

    }

}
