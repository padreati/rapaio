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

package rapaio.data;

import rapaio.core.VarRange;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoundFrame extends AbstractFrame {

    private int rowCount;
    private int varCount;


    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int varCount() {
        return varCount;
    }

    @Override
    public String[] varNames() {
        throw new NotImplementedException();
    }

    @Override
    public int varIndex(String name) {
        throw new NotImplementedException();
    }

    @Override
    public Var var(int pos) {
        throw new NotImplementedException();
    }

    @Override
    public Var var(String name) {
        throw new NotImplementedException();
    }

    @Override
    public Frame bindVars(Var... vars) {
        throw new NotImplementedException();
    }

    @Override
    public Frame bindVars(Frame df) {
        throw new NotImplementedException();
    }

    @Override
    public Frame mapVars(VarRange range) {
        throw new NotImplementedException();
    }

    @Override
    public Frame bindRows(Frame df) {
        throw new NotImplementedException();
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        throw new NotImplementedException();
    }
}
