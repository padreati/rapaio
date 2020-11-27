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

package rapaio.graphics.opt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/17.
 */
public class GOptionWidths implements GOption<Sizes> {

    private static final long serialVersionUID = 6568267641815981670L;
    private final Sizes widths;

    public GOptionWidths(double... percentages) {
        this.widths = new Sizes(false, percentages, null);
    }

    public GOptionWidths(int... sizes) {
        this.widths = new Sizes(true, null, sizes);
    }

    @Override
    public void bind(GOptions opts) {
        opts.setWidths(this);
    }

    @Override
    public Sizes apply(GOptions opts) {
        return widths;
    }
}
