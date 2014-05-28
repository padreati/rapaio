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

package rapaio.ml.ml_experiment.cluster.distance;

import rapaio.core.ColRange;
import rapaio.data.Frame;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:58 PM
 */
public class EuclideanDistance implements Distance {

    private ColRange range;

    public EuclideanDistance(ColRange range) {
        this.range = range;
    }

    @Override
    public double getDistance(Frame from, int fromRow, Frame targetFrame, int targetRow) {
        List<Integer> sourceFields = range.parseColumnIndexes(from);
        List<Integer> targetFields = range.parseColumnIndexes(targetFrame);
        if (sourceFields.size() != targetFields.size()) {
            throw new IllegalArgumentException("Source frame and target frame have a different number of columns !");
        }
        double distance = 0;
        for (int i = 0; i < sourceFields.size(); i++) {
            distance += (Math.pow(from.col(sourceFields.get(i)).value(fromRow), 2) -
                    Math.pow(targetFrame.col(targetFields.get(i)).value(targetRow), 2));
        }
        return Math.sqrt(distance);
    }
}
