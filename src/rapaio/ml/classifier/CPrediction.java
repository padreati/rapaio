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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;
import rapaio.ml.eval.Confusion;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classification predict result.
 * <p>
 * This object holds the result of a classification fitting.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CPrediction implements Printable {

    private final Classifier model;
    private final Frame df;
    private final List<String> targetNames = new ArrayList<>();
    private final boolean hasClasses;
    private final boolean hasDensities;
    private final Map<String, List<String>> dictionaries = new HashMap<>();
    private final Map<String, VarNominal> classes = new HashMap<>();
    private final Map<String, Frame> densities = new HashMap<>();

    // builder

    public static CPrediction build(
            final Classifier model,
            final Frame df,
            final boolean withClasses,
            final boolean withDensities) {
        return new CPrediction(model, df, withClasses, withDensities);
    }

    // private constructor

    private CPrediction(final Classifier model, final Frame df, final boolean hasClasses, final boolean hasDensities) {
        this.model = model;
        this.df = df;
        this.hasClasses = hasClasses;
        this.hasDensities = hasDensities;

        for (String target : model.targetNames()) {
            targetNames.add(target);
            dictionaries.put(target, model.targetLevels(target));
            if (hasClasses) {
                classes.put(target, VarNominal.empty(df.rowCount(), model.targetLevels(target)).withName(target));
            }
            if (hasDensities) {
                densities.put(target, SolidFrame.matrix(df.rowCount(), model.targetLevels(target)));
            }
        }
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
        return targetNames.toArray(new String[targetNames.size()]);
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
    public Map<String, VarNominal> classes() {
        return classes;
    }

    /**
     * Returns predicted target classes for first target variable name
     *
     * @return nominal variable with predicted classes
     */
    public VarNominal firstClasses() {
        return classes.get(firstTargetName());
    }

    /**
     * Returns predicted target classes for given target variable name
     *
     * @param targetVar given target variable name
     * @return nominal variable with predicted classes
     */
    public VarNominal classes(String targetVar) {
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
    public String summary() {
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
            sb.append(new Confusion(df.rvar(model.firstTargetName()), firstClasses()).summary());
        } else {
            sb.append("data frame does not contain target variable.");
        }
        return sb.toString();
    }
}
