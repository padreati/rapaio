/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.tree.rtree;

/**
 * Class used to pass information computed by search function to the method used to evaluate split
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/28/15.
 */
public class SearchPayload {

    public final int splits;
    public double totalVar;
    public double totalWeight;
    public final double[] splitWeight;
    public final double[] splitVar;

    public SearchPayload(int splits) {
        this.splits = splits;
        this.splitVar = new double[splits];
        this.splitWeight = new double[splits];
    }
}
