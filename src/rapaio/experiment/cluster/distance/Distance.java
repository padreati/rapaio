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

package rapaio.experiment.cluster.distance;

import rapaio.data.Frame;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public interface Distance {
    public double getDistance(Frame from, int fromRow, Frame targetFrame, int targetRow);
}
