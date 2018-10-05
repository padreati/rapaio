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

    public Var withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Var solidCopy() {

        // this implementation is useful for non-solid variables like bounded or mapped
        // all solid implementations have their own version of copy method

        switch (type()) {
            case INT:
                VarInt idx = VarInt.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    idx.setInt(i, getInt(i));
                }
                return idx;
            case LONG:
                VarLong stamp = VarLong.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    if (isMissing(i)) {
                        stamp.setMissing(i);
                        continue;
                    }
                    stamp.setLong(i, getLong(i));
                }
                return stamp;
            case DOUBLE:
                VarDouble num = VarDouble.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    num.setDouble(i, getDouble(i));
                }
                return num;
            case BINARY:
                VarBinary bin = VarBinary.empty(rowCount()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    if (isMissing(i)) {
                        bin.setMissing(i);
                        continue;
                    }
                    bin.setInt(i, getInt(i));
                }
                return bin;
            default:
                VarNominal nom = VarNominal.empty(rowCount(), levels()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    if (isMissing(i)) {
                        nom.setMissing(i);
                        continue;
                    }
                    nom.setLabel(i, getLabel(i));
                }
                return nom;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
    }
}
