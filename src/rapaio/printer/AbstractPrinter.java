/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.printer;

import rapaio.printer.opt.POption;
import rapaio.printer.opt.POpts;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractPrinter implements Printer {

    private int graphicWidth;
    private int graphicHeight;
    private POpts opts = new POpts(null);

    @Override
    public Printer withGraphicShape(int width, int height) {
        graphicWidth = width;
        graphicHeight = height;
        return this;
    }

    @Override
    public int graphicWidth() {
        return graphicWidth;
    }

    @Override
    public int graphicHeight() {
        return graphicHeight;
    }

    @Override
    public POpts getOptions() {
        return new POpts(opts);
    }

    @Override
    public Printer withOptions(POption... options) {
        for (POption option : options) {
            option.bind(opts);
        }
        return this;
    }
}
