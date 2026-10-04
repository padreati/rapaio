/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.tools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the axis construction of the grid: a degenerate range must not loop forever,
 * and the generated sequence has exactly the requested number of intervals.
 */
public class Grid2DTest {

    private static final Duration LIMIT = Duration.ofSeconds(5);

    @Test
    void constantGridSequenceTerminatesWithASinglePoint() {
        // the range of values is the single point 0, the step would be 0 and the sequence would never end
        Grid2D grid = Grid2D.fromFunction((x, y) -> 0.0, 0, 1, 0, 1, 2);
        double[] seq = assertTimeoutPreemptively(LIMIT, () -> grid.seq(4));
        assertArrayEquals(new double[] {0.0}, seq, 0.0);
        assertArrayEquals(new double[] {0.0}, assertTimeoutPreemptively(LIMIT, () -> grid.seq(4, 0.5)), 0.0);
    }

    @Test
    void degenerateAxisRangeGivesASinglePointAxis() {
        Grid2D grid = assertTimeoutPreemptively(LIMIT, () -> Grid2D.fromFunction((x, y) -> x + y, 1, 1, 0, 1, 4));
        assertEquals(1, grid.x().size());
        assertEquals(5, grid.y().size());
        assertEquals(1.0, grid.x().getDouble(0), 0.0);
        assertEquals(1.0 + 0.25, grid.value(0, 1), 1e-12);
    }

    @Test
    void sequenceHasExactlyTheRequestedNumberOfIntervals() {
        // the backing array of the variable has spare capacity which used to be returned as trailing zeros
        Grid2D grid = Grid2D.fromFunction((x, y) -> x, 1, 3, 1, 3, 4);
        for (int intervals : new int[] {1, 2, 3, 4, 5, 7, 10, 33}) {
            double[] seq = grid.seq(intervals);
            assertEquals(intervals + 1, seq.length, "intervals=" + intervals);
            assertEquals(1.0, seq[0], 0.0);
            assertEquals(3.0, seq[seq.length - 1], 1e-12);
        }
        assertArrayEquals(new double[] {1.0, 2.0, 3.0}, grid.seq(2), 1e-12);
    }

    @Test
    void extensionEnlargesThePositiveRange() {
        Grid2D grid = Grid2D.fromFunction((x, y) -> x, 1, 3, 1, 3, 2);
        // start 1 - 1*0.5, end 3 + 3*0.5, in 2 equal intervals
        assertArrayEquals(new double[] {0.5, 2.5, 4.5}, grid.seq(2, 0.5), 1e-12);
    }

    @Test
    void extensionEnlargesTheNegativeRangeToo() {
        // the signed product used to move a negative bound inwards, giving [-0.5, 0.5, 1.5] here
        Grid2D grid = Grid2D.fromFunction((x, y) -> x, -1, 1, -1, 1, 2);
        assertArrayEquals(new double[] {-1.5, 0.0, 1.5}, grid.seq(2, 0.5), 1e-12);
    }

    @Test
    void invalidAxisParametersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Grid2D.fromFunction((x, y) -> 0.0, 0, 1, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> Grid2D.fromFunction((x, y) -> 0.0, 0, 1, 0, 1, -3));
        assertThrows(IllegalArgumentException.class, () -> Grid2D.fromFunction((x, y) -> 0.0, 1, 0, 0, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> Grid2D.fromFunction((x, y) -> 0.0, Double.NaN, 1, 0, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> Grid2D.fromFunction((x, y) -> 0.0, 0, Double.POSITIVE_INFINITY, 0, 1, 2));
        Grid2D grid = Grid2D.fromFunction((x, y) -> x, 1, 3, 1, 3, 2);
        assertThrows(IllegalArgumentException.class, () -> grid.seq(0));
    }
}
