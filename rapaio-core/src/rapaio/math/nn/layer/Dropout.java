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

package rapaio.math.nn.layer;

import java.util.List;

import rapaio.math.nn.Net;
import rapaio.math.nn.Node;

public class Dropout extends Net {

    private final double p;
    private final boolean inplace;

    public Dropout(double p) {
        this(p, false);
    }

    public Dropout(double p, boolean inplace) {
        super(null);
        this.p = p;
        this.inplace = inplace;
    }

    @Override
    public List<Node> parameters() {
        return List.of();
    }

    @Override
    protected Node forward11(Node x) {
        if (train) {
            return x.dropout(p, random, inplace);
        }
        return x;
    }
}
