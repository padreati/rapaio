///*
// * Apache License
// * Version 2.0, January 2004
// * http://www.apache.org/licenses/
// *
// *    Copyright 2013 Aurelian Tutuianu
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package rapaio.ml.classifier.mi;
//
//import rapaio.data.Frame;
//import rapaio.data.Nominal;
//import rapaio.data.Numeric;
//import rapaio.ml.classifier.AbstractClassifier;
//import rapaio.ml.classifier.Classifier;
//import rapaio.ml.classifier.bayes.NaiveBayesClassifier;
//import rapaio.ml.classifier.colselect.VarSelector;
//import rapaio.ml.classifier.tools.DensityVector;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
// */
//@Deprecated
//public class MIClassifier extends AbstractClassifier {
//
//    String groupCol = "";
//    Classifier c = new NaiveBayesClassifier();
//
//    @Override
//    public VarSelector getVarSelector() {
//        return super.getVarSelector();
//    }
//
//    @Override
//    public MIClassifier withVarSelector(VarSelector varSelector) {
//        return (MIClassifier) super.withVarSelector(varSelector);
//    }
//
//    public String getGroupCol() {
//        return groupCol;
//    }
//
//    public MIClassifier withGroupCol(String groupCol) {
//        this.groupCol = groupCol;
//        return this;
//    }
//
//    public Classifier getClassifier() {
//        return c;
//    }
//
//    public MIClassifier withClassifier(Classifier c) {
//        this.c = c;
//        return this;
//    }
//
//    @Override
//    public Classifier newInstance() {
//        return new MIClassifier()
//                .withVarSelector(varSelector)
//                .withGroupCol(groupCol)
//                .withClassifier(c.newInstance());
//    }
//
//    @Override
//    public String name() {
//        return "MIClassifier";
//    }
//
//    @Override
//    public String fullName() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("MIClassifier(");
//        sb.append("colSelector=").append(varSelector.name()).append(",");
//        sb.append("groupCol=").append(groupCol).append(",");
//        sb.append("c=").append(c.fullName()).append(",");
//        sb.append(")");
//        return sb.toString();
//    }
//
//    @Override
//    public void learn(Frame df, Numeric weights, String targetVars) {
//        this.targetVars = targetVars;
//        this.dict = df.var(targetVars).dictionary();
//
//        if (!groupCol.isEmpty()) {
//            df = df.removeVars(groupCol);
//        }
//        c.learn(df, weights, targetVars);
//    }
//
//    @Override
//    public void predict(Frame df) {
//        String[] groups = df.var(groupCol).dictionary();
//        Map<String, DensityVector> dvs = new HashMap<>();
//        Arrays.stream(groups).forEach(groupLabel -> dvs.put(groupLabel, new DensityVector(dict)));
//
//        c.predict(df);
//        c.classes().stream().forEach(s -> {
//            String groupLabel = df.label(s.row(), groupCol);
//            DensityVector dv = dvs.get(groupLabel);
//            int i = s.index();
//            dv.update(i, 1);
//        });
//
//        Map<String, String> predictions = new HashMap<>();
//        dvs.forEach((groupLabel, dv) -> {
//            predictions.put(groupLabel, dict[dv.findBestIndex()]);
//        });
//
//        classes = Nominal.newEmpty(df.rowCount(), dict);
//
//        c.classes().stream().forEach(s -> classes.setLabel(s.row(), predictions.get(df.label(s.row(), groupCol))));
//        densities = c.densities();
//    }
//
//    @Override
//    public void buildSummary(StringBuilder sb) {
//
//    }
//}
