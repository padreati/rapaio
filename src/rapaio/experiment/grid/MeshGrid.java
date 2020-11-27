/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.experiment.grid;

import rapaio.data.Var;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/27/15.
 */
@Deprecated
public interface MeshGrid extends Serializable {

    Var x();

    Var y();

    /**
     * Computes the side of the mesh grid point.
     *
     * @param i index of the x coordinate
     * @param j index of the y coordinate
     * @return 0 if below isoBand, 1 if inside isoBand, 2 if above isoBand
     */
    int side(int i, int j);

    /**
     * Computes x coordinate of the low threshold between
     * grid points with indexes (i,j) and (i+1,j)
     *
     * @param i index of the x coordinate of the starting point
     * @param j index of the y coordinate of the starting point
     * @return x coordinate value of the low threshold
     */
    double xLow(int i, int j);

    /**
     * Computes x coordinate of the high threshold between
     * grid points with indexes (i, j) and (i+1,j)
     *
     * @param i index of the x coordinate of the starting point
     * @param j index of the y coordinate of the starting point
     * @return x coordinate value of the high threshold
     */
    double xHigh(int i, int j);

    /**
     * Computes y coordinate of the low threshold between
     * grid points with indexes (i,j) and (i,j+1)
     *
     * @param i index of the x coordinate of the starting point
     * @param j index of the y coordinate of the starting point
     * @return y coordinate value of the low threshold
     */
    double yLow(int i, int j);

    /**
     * Computes y coordinate of the high threshold between
     * grid points with indexes (i,j) and (i,j+1)
     *
     * @param i index of the x coordinate of the starting point
     * @param j index of the y coordinate of the starting point
     * @return y coordinate value of the high threshold
     */
    double yHigh(int i, int j);
}
