/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.ml.eval.metric.Confusion;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Classification predict result.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassifierResult implements Printable {

    protected final ClassifierModel<?, ?, ?> model;
    protected final Frame df;
    protected final List<String> targetNames = new ArrayList<>();
    protected final boolean hasClasses;
    protected final boolean hasDensities;
    protected final Map<String, List<String>> dictionaries = new HashMap<>();
    protected final Map<String, Var> classes = new HashMap<>();
    protected final Map<String, Frame> densities = new HashMap<>();

    public static ClassifierResult build(ClassifierModel<?, ?, ?> model, Frame df, boolean withClasses, boolean withDensities) {
        return new ClassifierResult(model, df, withClasses, withDensities);
    }

    public static ClassifierResult copy(ClassifierModel<?, ?, ?> model, Frame df, boolean withClasses, boolean withDensities,
            ClassifierResult from) {
        ClassifierResult result = new ClassifierResult(model, df, withClasses, withDensities);
        for (String key : result.classes.keySet()) {
            result.classes.put(key, from.classes.get(key));
        }
        for (String key : result.densities.keySet()) {
            result.densities.put(key, from.densities.get(key));
        }
        return result;
    }

    private ClassifierResult(final ClassifierModel<?, ?, ?> model, final Frame df, final boolean hasClasses, final boolean hasDensities) {
        this.model = model;
        this.df = df;
        this.hasClasses = hasClasses;
        this.hasDensities = hasDensities;

        for (int i = 0; i < model.targetNames().length; i++) {
            String targetName = model.targetNames()[i];
            targetNames.add(targetName);
            List<String> targetLevels = new ArrayList<>(model.targetLevels(targetName));
            dictionaries.put(targetName, targetLevels);
            if (hasClasses) {
                switch (model.targetTypes()[i]) {
                    case NOMINAL -> classes.put(targetName, VarNominal.empty(df.rowCount(), targetLevels).name(targetName));
                    case BINARY -> classes.put(targetName, VarBinary.empty(df.rowCount()).name(targetName));
                    case INT -> classes.put(targetName, VarInt.empty(df.rowCount()).name(targetName));
                }

            }
            if (hasDensities) {
                densities.put(targetName, SolidFrame.matrix(df.rowCount(), targetLevels));
            }
        }
    }

    public ClassifierModel<?, ?, ?> getModel() {
        return model;
    }

    public boolean hasClasses() {
        return hasClasses;
    }

    public boolean hasDensities() {
        return hasDensities;
    }

    /**
     * Returns target variables built at learning time
     *
     * @return target variable names
     */
    public String[] targetNames() {
        return targetNames.toArray(new String[0]);
    }

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    public String firstTargetName() {
        return targetNames.get(0);
    }

    /**
     * Returns levels used at learning times for target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public Map<String, List<String>> dictionaries() {
        return dictionaries;
    }

    /**
     * Returns levels used at learning times for first target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public List<String> firstDictionary() {
        return dictionaries.get(firstTargetName());
    }

    /**
     * Returns levels used at learning time for the given target variable
     */
    public List<String> dictionary(String targetVar) {
        return dictionaries.get(targetVar);
    }

    /**
     * Returns predicted target classes for each target variable name
     *
     * @return map with nominal variables as predicted classes
     */
    public Map<String, Var> classes() {
        return classes;
    }

    /**
     * Returns predicted target classes for first target variable name
     *
     * @return nominal variable with predicted classes
     */
    public Var firstClasses() {
        return classes.get(firstTargetName());
    }

    /**
     * Returns predicted target classes for given target variable name
     *
     * @param targetVar given target variable name
     * @return nominal variable with predicted classes
     */
    public Var classes(String targetVar) {
        return classes.get(targetVar);
    }

    /**
     * Returns predicted class densities frame if is computed,
     * otherwise returns null.
     *
     * @return predicted class densities (frame with one
     * column for each target class, including missing value)
     */
    public Map<String, Frame> densities() {
        return densities;
    }

    /**
     * Returns predicted class density for the first target variable if is computed,
     * otherwise returns null.
     *
     * @return predicted class densities (frame with one
     * column for each target class, including missing value)
     */
    public Frame firstDensity() {
        return densities().get(firstTargetName());
    }

    /**
     * Returns predicted class density for the given variable if densities are computed,
     * otherwise returns null.
     *
     * @param targetVar given target variable name
     * @return map of frames for each target class, with frames having variables for each
     * classification label
     */
    public Frame density(final String targetVar) {
        return densities.get(targetVar);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();

        sb.append("Classification Result Summary").append("\n");
        sb.append("=============================\n");
        sb.append("\n");

        sb.append("Model type: ").append(model.name()).append("\n");
        sb.append("Model instance: ").append(model.fullName()).append("\n");
        sb.append("\n");

        sb.append("Predicted frame summary:\n");
        sb.append("> rows: ").append(df.rowCount()).append("\n");
        sb.append("> vars: ").append(df.varCount()).append("\n");
        sb.append("> targets: ").append(Arrays.deepToString(model.targetNames())).append("\n");
        sb.append("> inputs: ").append(Arrays.deepToString(model.inputNames())).append("\n");
        sb.append("\n");

        sb.append("Classification results:").append("\n");
        if (Arrays.asList(df.varNames()).contains(firstTargetName())) {
            sb.append(Confusion.from(df.rvar(model.firstTargetName()), firstClasses()).toSummary(printer, options));
        } else {
            sb.append("data frame does not contain target variable.");
        }
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }
}
