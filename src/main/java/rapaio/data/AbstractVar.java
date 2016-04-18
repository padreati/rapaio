/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
        if (name == null)
            throw new IllegalArgumentException("variable name cannot be null");
        this.name = name;
        return this;
    }

    @Override
    public Var solidCopy() {

        // this implementation is useful for non-solid variables like bounded or mapped
        // all solid implementations have their own version of copy method

        switch (type()) {
            case NOMINAL:
                Nominal nom = Nominal.newEmpty(rowCount(), levels()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    nom.setLabel(i, label(i));
                }
                return nom;
            case ORDINAL:
                Ordinal ord = Ordinal.empty(rowCount(), levels()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    ord.setLabel(i, label(i));
                }
                return ord;
            case INDEX:
                Index idx = Index.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    idx.setIndex(i, index(i));
                }
                return idx;
            case STAMP:
                Stamp stamp = Stamp.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    stamp.setStamp(i, stamp(i));
                }
                return stamp;
            case NUMERIC:
                Numeric num = Numeric.newEmpty(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    num.setValue(i, value(i));
                }
                return num;
            case BINARY:
                Binary bin = Binary.newEmpty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    bin.setIndex(i, index(i));
                }
                return bin;
            default:
                throw new IllegalArgumentException("not implemented");
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
    }
}
