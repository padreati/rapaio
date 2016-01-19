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

package rapaio.graphics.base;

import rapaio.graphics.opt.GOpts;

public abstract class BaseFigure implements Figure {

    private static final long serialVersionUID = 4161041064084793962L;
    protected GOpts options = new GOpts();
    private Range range;

    public GOpts getOptions() {
        return options;
    }

    protected abstract Range buildRange();

    public Range getRange() {
        if (range == null) {
            range = buildRange();
        }
        return range;
    }

    protected void setRange(Range range) {
        this.range = range;
    }
}
