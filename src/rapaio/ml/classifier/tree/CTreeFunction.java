/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree;

import rapaio.core.tools.DTable;
import rapaio.util.Tag;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeFunction extends Serializable {

    double compute(DTable dt);

    Tag<CTreeFunction> Entropy = Tag.valueOf("Entropy", (DTable dt) -> dt.getSplitEntropy(false));
    Tag<CTreeFunction> InfoGain = Tag.valueOf("InfoGain", (DTable dt) -> -dt.getInfoGain(false));
    Tag<CTreeFunction> GainRatio = Tag.valueOf("GainRatio", (DTable dt) -> -dt.getGainRatio(false));
    Tag<CTreeFunction> GiniGain = Tag.valueOf("GiniGain", (DTable dt) -> -dt.getGiniIndex());
}

