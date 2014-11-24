/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.refactor.ml_experiment.cluster.algorithm;

import rapaio.data.Frame;
import rapaio.ml.refactor.ml_experiment.cluster.distance.Distance;
import rapaio.util.Pair;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:53 PM
 */
@Deprecated
public class KMeans {
    private int clusterNumber;
    private Distance distance;
    private double[] centroids;
    private double[] meanDistances;
    private int maxIterations;
    public static final int MAX_ITERATIONS = 100;

    public KMeans(int clusterNumber, Distance distance, int maxIterations) {
        Random rand = new Random();
        this.maxIterations = maxIterations;
        this.clusterNumber = clusterNumber;
        this.distance = distance;
        this.centroids = new double[clusterNumber];
        this.meanDistances = new double[clusterNumber];
        for (int i = 0; i < clusterNumber; i++) {
            centroids[i] = rand.nextDouble();
            meanDistances[i] = Double.NaN;
        }
    }

    public KMeans(int clusterNumber, Distance distance) {
        this(clusterNumber, distance, MAX_ITERATIONS);
    }

    public List<Pair<Integer, Integer>> learn(Frame frame) {
        boolean isStable = false;
        int iterations = 0;
        while (!isStable || iterations < maxIterations) {

        }
        return null;
    }

    private void distributeValues(Frame frame) {

    }

    public int classify(Frame frame, int rowNumber) {
        return 0;
    }
}
