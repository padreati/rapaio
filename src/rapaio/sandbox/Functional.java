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

package rapaio.sandbox;

import rapaio.core.MathBase;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.workspace.Summary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Functional {

    public static void main(String[] args) throws Exception {
        new Functional().run();
    }

    public void run() throws Exception {

        Frame df = Datasets.loadCarMpgDataset();
        List<Double> weights = new ArrayList<>();
        for (int i = 0; i < df.rowCount(); i++) {
            weights.add((double) i);
        }
        Summary.summary(
                df.stream().toMappedFrame()
//                        .filter(spot -> spot.getValue("origin") >= 2)
//                        .filter(spot -> spot.getValue("cylinders") > 5)
//                        .toMappedFrame()
        );

//        Frame filtered = new FSpots(spots).toMappedFrame();
//        Summary.summary(filtered);
//        List<Double> filteredWeights = new FSpots(spots).filterByRowId(weights);
//
//        for (int i = 0; i < filtered.rowCount(); i++) {
//            System.out.println(String.format("row:%d, weight::%f", filtered.rowId(i), filteredWeights.get(i)));
//        }

        System.out.println(MathBase.log2(200));

        System.out.println(new BigDecimal("0.0001").multiply(new BigDecimal("2").pow(8))
                .toPlainString());
    }

    public static <T> T time(Supplier<T> f) {
        long start = System.currentTimeMillis();
        T obj = f.get();
        long stop = System.currentTimeMillis();
        System.out.println("time for " + obj + " took " + (stop - start) + " millis");
        return obj;
    }
}
