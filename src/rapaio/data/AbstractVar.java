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

package rapaio.data;

import rapaio.data.stream.VSpot;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractVar implements Var {

    private static final long serialVersionUID = 2607349261526552662L;
    private String name = "?";

    public String name() {
        return name;
    }

    public AbstractVar withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Var solidCopy() {

        // this implementation is useful for non-solid variables like bounded or mapped
        // all solid implementations have their own version of solidCopy method

        switch (type()) {
            case NOMINAL:
                return stream().map(VSpot::label).collect(Nominal.collector());
            case ORDINAL:
                Ordinal ord = Ordinal.newEmpty(rowCount(), dictionary()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    ord.setLabel(i, label(i));
                }
                return ord;
            case INDEX:
                Index idx = Index.newEmpty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    idx.setIndex(i, index(i));
                }
                return idx;
            case STAMP:
                Stamp stamp = Stamp.newEmpty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    stamp.setStamp(i, stamp(i));
                }
                return stamp;
            case NUMERIC:
                return stream().map(VSpot::value).collect(Numeric.collector());
            case BINARY:
                Binary bin = Binary.newEmpty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    bin.setIndex(i, index(i));
                }
                return bin;
            default:
                throw new NotImplementedException();
        }
    }
}
