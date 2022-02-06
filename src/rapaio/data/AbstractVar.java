/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.data.unique.UniqueLabel;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorVar;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractVar implements Var {

    @Serial
    private static final long serialVersionUID = 2607349261526552662L;
    private String name = "?";

    public String name() {
        return name;
    }

    public Var name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Var copy() {

        // this implementation is useful for non-solid variables like bounded or mapped
        // all solid implementations have their own version of copy method

        switch (type()) {
            case INT:
                return VarInt.from(size(), this::getInt).name(name());
            case LONG:
                VarLong stamp = VarLong.empty(size()).name(name());
                for (int i = 0; i < size(); i++) {
                    if (isMissing(i)) {
                        stamp.setMissing(i);
                        continue;
                    }
                    stamp.setLong(i, getLong(i));
                }
                return stamp;
            case DOUBLE:
                VarDouble num = VarDouble.empty(size()).name(name());
                for (int i = 0; i < size(); i++) {
                    num.setDouble(i, getDouble(i));
                }
                return num;
            case BINARY:
                VarBinary bin = VarBinary.empty(size()).name(name());
                for (int i = 0; i < size(); i++) {
                    if (isMissing(i)) {
                        bin.setMissing(i);
                        continue;
                    }
                    bin.setInt(i, getInt(i));
                }
                return bin;
            case NOMINAL:
                VarNominal nom = VarNominal.empty(size(), levels()).name(name());
                for (int i = 0; i < size(); i++) {
                    if (isMissing(i)) {
                        nom.setMissing(i);
                        continue;
                    }
                    nom.setLabel(i, getLabel(i));
                }
                return nom;
            case STRING:
                return VarString.from(size(), this::getLabel).name(name());
            case INSTANT:
                return VarInstant.from(size(), this::getInstant).name(name());
            default:
                throw new IllegalArgumentException("Variable type does not hav an implementation.");
        }
    }

    @Override
    public DVector dv(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] values = new double[size()];
            for (int i = 0; i < size(); i++) {
                values[i] = getDouble(i);
            }
            return new DVectorDense(0, size(), values);
        }
        return new DVectorVar<>(this);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException {
        name = in.readUTF();
    }

    protected abstract String toStringClassName();

    protected abstract int toStringDisplayValueCount();

    protected abstract void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(toStringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(size());
        sb.append(", values: ");

        int elements = toStringDisplayValueCount();
        if (size() <= elements) {
            for (int i = 0; i < size(); i++) {
                sb.append(getLabel(i));
                if (i < size() - 1) {
                    sb.append(", ");
                }
            }
        } else {
            for (int i = 0; i < elements - 2; i++) {
                sb.append(getLabel(i)).append(", ");
            }
            sb.append("..., ");
            sb.append(getLabel(size() - 2)).append(", ").append(getLabel(size() - 1));
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        StringBuilder sb = new StringBuilder();
        sb.append("> summary(name: ").append(name()).append(", type: ").append(type().name()).append(")\n");
        int complete = (int) stream().complete().count();
        sb.append("rows: ")
                .append(size())
                .append(", complete: ")
                .append(complete)
                .append(", missing: ")
                .append(size() - complete)
                .append("\n");

        TextTable tt = TextTable.empty(8, 2);

        tt.textRight(0, 0, name());
        tt.textLeft(0, 1, "[" + type().code() + "]");
        fillSummary(tt, 0, 1);
        sb.append(tt.getRawText()).append("\n");

        return sb.toString();
    }

    /**
     * Fills the 7-cells summary values for the given variable in the text table
     *
     * @param tt             text table which holds the summary
     * @param headerColIndex column index of the text table where to store header information
     * @param valueColIndex  column index of the text table where to store value information
     */
    void fillSummary(TextTable tt, int headerColIndex, int valueColIndex) {
        switch (type()) {
            case BINARY:
                fillSummaryBinary(this, tt, headerColIndex, valueColIndex);
                break;
            case NOMINAL:
            case STRING:
                fillSummaryLabel(this, tt, headerColIndex, valueColIndex);
                break;
            case DOUBLE:
            case INT:
            case LONG:
                fillSummaryDouble(this, tt, headerColIndex, valueColIndex);
                break;

            default:
        }
    }

    private void fillSummaryBinary(Var v, TextTable tt, int headerColIndex, int valueColIndex) {
        tt.textLeft(1, headerColIndex, "0 :");
        tt.textLeft(2, headerColIndex, "1 :");
        tt.textLeft(3, headerColIndex, "NAs :");

        int ones = 0;
        int zeros = 0;
        int missing = 0;
        for (int i = 0; i < v.size(); i++) {
            if (v.isMissing(i)) {
                missing++;
            } else {
                if (v.getInt(i) == 1) {
                    ones++;
                } else {
                    zeros++;
                }
            }
        }
        tt.textRight(1, valueColIndex, String.valueOf(zeros));
        tt.textRight(2, valueColIndex, String.valueOf(ones));
        tt.textRight(3, valueColIndex, String.valueOf(missing));
    }

    private void fillSummaryLabel(Var var, TextTable tt, int headerColIndex, int valueColIndex) {
        UniqueLabel unique = Unique.ofLabel(var.stream().complete().toMappedVar(), false);
        int[] ids = unique.countSortedIds().elements();
        IntArrays.reverse(ids, 0, unique.uniqueCount());

        int rowCount = var.size();
        int nans = (int) (var.size() - var.stream().complete().count());

        int len;
        if (nans == 0) {
            len = 6;
        } else {
            len = 5;
            tt.textRight(6, headerColIndex, "NAs :");
            tt.textRight(6, valueColIndex, String.valueOf(nans));
        }

        boolean others = false;
        if (unique.uniqueCount() <= len) {
            len = unique.uniqueCount();
        } else {
            len = len - 1;
            others = true;
        }

        int filled = 0;
        for (int i = 0; i < len; i++) {
            tt.textRight(i + 1, headerColIndex, unique.uniqueValue(ids[i]) + " :");
            int count = unique.rowList(ids[i]).size();
            tt.textRight(i + 1, valueColIndex, String.valueOf(count));
            filled += count;
        }
        if (others) {
            tt.textRight(len + 1, headerColIndex, "(Other) :");
            tt.textRight(len + 1, valueColIndex, String.valueOf(rowCount - filled - nans));
        }
    }

    private void fillSummaryDouble(Var v, TextTable tt, int headerColIndex, int valueColIndex) {
        double[] p = new double[] {0., 0.25, 0.50, 0.75, 1.00};
        double[] perc = Quantiles.of(v, p).values();
        double mean = Mean.of(v).value();

        int nas = 0;
        for (int j = 0; j < v.size(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        tt.textRight(1, headerColIndex, "Min. :");
        tt.textRight(2, headerColIndex, "1st Qu. :");
        tt.textRight(3, headerColIndex, "Median :");
        tt.textRight(4, headerColIndex, "Mean :");
        tt.textRight(5, headerColIndex, "2nd Qu. :");
        tt.textRight(6, headerColIndex, "Max. :");

        tt.floatMedium(1, valueColIndex, perc[0]);
        tt.floatMedium(2, valueColIndex, perc[1]);
        tt.floatMedium(3, valueColIndex, perc[2]);
        tt.floatMedium(4, valueColIndex, mean);
        tt.floatMedium(5, valueColIndex, perc[3]);
        tt.floatMedium(6, valueColIndex, perc[4]);

        if (nas != 0) {
            tt.textRight(7, headerColIndex, "NAs :");
            tt.floatMedium(7, valueColIndex, nas);
        }
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(size()).append("]\n");

        if (size() > 100) {
            TextTable tt = TextTable.empty(102, 2, 1, 1);
            tt.textCenter(0, 0, "row");
            tt.textCenter(0, 1, "value");

            for (int i = 0; i < 80; i++) {
                tt.intRow(i + 1, 0, i);
                textTablePutValue(tt, i + 1, 1, i, printer, options);
            }
            tt.textCenter(80, 0, "...");
            tt.textCenter(80, 1, "...");
            for (int i = size() - 20; i < size(); i++) {
                tt.intRow(i + 101 - size(), 0, i);
                textTablePutValue(tt, i + 101 - size(), 1, i, printer, options);
            }
            sb.append(tt.getDynamicText(printer, options));
        } else {
            fullTable(sb, printer, options);
        }
        return sb.toString();
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringClassName()).append(" [name:\"").append(name()).append("\", rowCount:").append(size()).append("]\n");
        fullTable(sb, printer, options);
        return sb.toString();
    }

    private void fullTable(StringBuilder sb, Printer printer, POption<?>... options) {
        TextTable tt = TextTable.empty(size() + 1, 2, 1, 1);
        tt.textCenter(0, 0, "row");
        tt.textCenter(0, 1, "value");
        for (int i = 0; i < size(); i++) {
            tt.intRow(i + 1, 0, i);
            textTablePutValue(tt, i + 1, 1, i, printer, options);
        }
        sb.append(tt.getDynamicText(printer, options));
    }
}
