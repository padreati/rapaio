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

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import rapaio.printer.format.*;

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

    ///////////////////// BEGIN VARIOUS OPERATIONS ///////////////////////////////////////

    @Override
    public AbstractVar apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rowCount(); i++) {
            if (!isMissing(i)) {
                setDouble(i, fun.applyAsDouble(getDouble(i)));
            }
        }
        return this;
    }

    @Override
    public VarDouble capply(Double2DoubleFunction fun) {
        double[] data = new double[rowCount()];
        for (int i = 0; i < rowCount(); i++) {
            if (isMissing(i)) {
                data[i] = Double.NaN;
            } else {
                data[i] = fun.applyAsDouble(getDouble(i));
            }
        }
        return VarDouble.wrap(data).withName(name());
    }

    @Override
    public double sum() {
        double sum = 0.0;
        for (int i = 0; i < rowCount(); i++) {
            if (isMissing(i)) {
                continue;
            }
            sum += getDouble(i);
        }
        return sum;
    }

    @Override
    public AbstractVar plus(double a) {
        for (int i = 0; i < rowCount(); i++) {
            setDouble(i, getDouble(i) + a);
        }
        return this;
    }

    @Override
    public AbstractVar plus(Var x) {
        for (int i = 0; i < rowCount(); i++) {
            setDouble(i, getDouble(i) + x.getDouble(i));
        }
        return this;
    }

    @Override
    public AbstractVar mult(double a) {
        for (int i = 0; i < rowCount(); i++) {
            setDouble(i, getDouble(i) * a);
        }
        return this;
    }

    @Override
    public int[] sortedCompleteRows(boolean asc) {
        int[] rows = new int[rowCount()];
        int len = 0;
        for (int i = 0; i < rowCount(); i++) {
            if(isMissing(i)) {
                continue;
            }
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, refComparator(asc));
        return IntArrays.copy(rows, 0, len);
    }

    ///////////////////// END VARIOUS OPERATIONS ///////////////////////////////////////

    @Override
    public Var copy() {

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
            case NOMINAL:
            case TEXT:
                VarNominal nom = VarNominal.empty(rowCount(), levels()).withName(name());
                for (int i = 0; i < rowCount(); i++) {
                    if (isMissing(i)) {
                        nom.setMissing(i);
                        continue;
                    }
                    nom.setLabel(i, getLabel(i));
                }
                return nom;
            default:
                throw new IllegalArgumentException("Variable type does not hav an implementation.");
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        name = in.readUTF();
    }

    abstract String stringClassName();

    abstract int stringPrefix();

    void stringPutValue(TextTable tt, int i, int j, int row) {
        tt.textCenter(i, j, getLabel(row));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(stringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(rowCount());
        sb.append(", values: ");

        int prefix = stringPrefix();
        if (rowCount() <= prefix + 2) {
            for (int i = 0; i < rowCount(); i++) {
                sb.append(getLabel(i));
                if (i < rowCount() - 1) {
                    sb.append(", ");
                }
            }
        } else {
            for (int i = 0; i < prefix; i++) {
                sb.append(getLabel(i)).append(", ");
            }
            sb.append("..., ");
            sb.append(getLabel(rowCount() - 2)).append(", ").append(getLabel(rowCount() - 1));
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    public String content() {
        StringBuilder sb = new StringBuilder();
        sb.append(stringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(rowCount()).append("]\n");

        if (rowCount() > 100) {
            TextTable tt = TextTable.empty(102, 2, 1, 1);
            tt.textCenter(0, 0, "row");
            tt.textCenter(0, 1, "value");

            for (int i = 0; i < 80; i++) {
                tt.intRow(i + 1, 0, i);
                stringPutValue(tt, i + 1, 1, i);
            }
            tt.textCenter(80, 0, "...");
            tt.textCenter(80, 1, "...");
            for (int i = rowCount() - 20; i < rowCount(); i++) {
                tt.intRow(i + 101 - rowCount(), 0, i);
                stringPutValue(tt, i + 101 - rowCount(), 1, i);
            }
            sb.append(tt.getDefaultText());
        } else {
            fullTable(sb);
        }
        return sb.toString();
    }

    private void fullTable(StringBuilder sb) {
        TextTable tt = TextTable.empty(rowCount() + 1, 2, 1, 1);
        tt.textCenter(0, 0, "row");
        tt.textCenter(0, 1, "value");
        for (int i = 0; i < rowCount(); i++) {
            tt.intRow(i + 1, 0, i);
            stringPutValue(tt, i + 1, 1, i);
        }
        sb.append(tt.getDefaultText());
    }

    @Override
    public String fullContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(stringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(rowCount()).append("]\n");
        fullTable(sb);
        return sb.toString();
    }
}
