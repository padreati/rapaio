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

package rapaio.data.preprocessing;

import java.io.Serial;

import rapaio.data.Var;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VarSort extends AbstractVarTransform {

    public static VarSort ascending() {
        return new VarSort(true);
    }

    public static VarSort descending() {
        return new VarSort(false);
    }

    @Serial
    private static final long serialVersionUID = -6260151471065618233L;
    private final boolean asc;

    private VarSort(boolean asc) {
        this.asc = asc;
    }

    @Override
    public VarTransform newInstance() {
        return new VarSort(asc);
    }

    @Override
    public Var coreApply(Var var) {
        return VarRefSort.from(var.refComparator(asc)).fapply(var);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toString();
    }

    @Override
    public String toString() {
        return "VarSort(" + (asc ? "asc" : "desc") + ")";
    }
}
