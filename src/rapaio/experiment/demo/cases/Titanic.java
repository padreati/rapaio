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

package rapaio.experiment.demo.cases;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.FrameTransform;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.FCopy;
import rapaio.data.filter.FFilter;
import rapaio.data.filter.FImputeClassifier;
import rapaio.data.filter.FImputeRegression;
import rapaio.data.filter.FOneHotEncoding;
import rapaio.data.filter.FRemoveVars;
import rapaio.data.filter.FStandardize;
import rapaio.io.Csv;
import rapaio.ml.common.kernel.PolyKernel;
import rapaio.ml.eval.ClassifierEvaluation;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ensemble.CForest;
import rapaio.ml.model.ensemble.RForest;
import rapaio.ml.model.svm.SvmClassifier;
import rapaio.sys.WS;
import rapaio.sys.With;

public class Titanic {

    public static void main(String[] args) {
        new Titanic().run();
    }

    private void run() {

        WS.getPrinter().withOptions(With.textWidth(300));

        String urlTrain = "https://raw.githubusercontent.com/padreati/rapaio-notebooks/master/data/titanic/train.csv";
        String urlTest = "https://raw.githubusercontent.com/padreati/rapaio-notebooks/master/data/titanic/test.csv";
        Frame train = Csv.instance().naValues.add("").types.add(VarType.NOMINAL, "Survived", "Pclass").readUrl(urlTrain);
        Frame test = Csv.instance().naValues.add("").template.set(train).readUrl(urlTest);

        RandomSource.setSeed(123);
//        Frame tr = train.mapVars("Survived,Sex,Pclass,Embarked,Age,Fare,SibSp,Parch");
//        CForest rf = CForest.newModel().runs.set(500).poolSize.set(3);
//        ClassifierEvaluation.cv(tr, "Survived", rf, 10, Accuracy.newMetric(true)).run().printContent();

//        train.printSummary();

        var transform = FrameTransform.newTransform()
                .add(FCopy.filter())
                .add(new CustomFilter())
                .add(FImputeRegression.of(RForest.newRF().runs.set(100), VarRange.of("Age,Pclass,Embarked,Sex,Fare,Title"), "Age"))
                .add(FImputeClassifier.of(CForest.newModel().runs.set(100), VarRange.of("Embarked,Age,Pclass,Sex,Title"), "Embarked"))
                .add(FImputeClassifier.of(CForest.newModel().runs.set(100), VarRange.of("Age,Pclass,Embarked,Sex,Fare,Ticket"),"Ticket"))
                .add(FImputeClassifier.of(CForest.newModel().runs.set(100), VarRange.of("Age,Pclass,Embarked,Sex,Fare,Cabin"), "Cabin"))
                .add(FOneHotEncoding.on("Title"))
                .add(FOneHotEncoding.on(false, false, "Embarked,Sex,Ticket,Cabin,Pclass"))
                .add(FStandardize.on(VarRange.onlyTypes(VarType.DOUBLE)))
                .add(FRemoveVars.remove(VarRange.of("PassengerId,Name,SibSp,Parch")));

        ClassifierModel<?,?,?> model = new SvmClassifier()
                .c.set(.00000007)
                .kernel.set(new PolyKernel(5, 1, 1))
                .probability.set(true)
                .shrinking.set(true);

        var tr = transform.fapply(train);
        tr.printSummary();

        ClassifierEvaluation.cv(tr, "Survived", model, 10, Accuracy.newMetric()).run().printContent();
    }
}

class CustomFilter implements FFilter {

    private final Map<String, String[]> replaceMap = Map.of(
            "Mrs", new String[] {"Mrs", "Mme", "Lady", "Countess"},
            "Mr", new String[] {"Mr", "Sir", "Don", "Ms"},
            "Miss", new String[] {"Miss", "Mlle"},
            "Master", new String[] {"Master"},
            "Dr", new String[] {"Dr"},
            "Military", new String[] {"Col", "Major", "Jonkheer", "Capt"},
            "Rev", new String[] {"Rev"}
    );
    private final Function<String, String> titleFun = txt -> {
        for (var e : replaceMap.entrySet()) {
            for (int i = 0; i < e.getValue().length; i++) {
                if (txt.contains(" " + e.getValue()[i] + ". ")) {
                    return e.getKey();
                }
            }
        }
        return "?";
    };

    public void fit(Frame df) {
    }

    public Frame apply(Frame df) {
        VarNominal title = VarNominal.empty(0, new ArrayList<>(replaceMap.keySet())).name("Title");
        df.rvar("Name").stream().mapToString().forEach(name -> title.addLabel(titleFun.apply(name)));

        Var famSize = VarDouble.from(df.rowCount(), r -> 1.0 + df.getInt(r, "SibSp") + df.getInt(r, "Parch")).name("FamilySize");
        Var ticket = VarNominal.from(df.rowCount(), r -> df.getLabel(r, "Ticket").substring(0, 1).toUpperCase()).name("Ticket");
        Var cabin = VarNominal.from(df.rowCount(), r -> df.getLabel(r, "Cabin").substring(0, 1).toUpperCase()).name("Cabin");

        return df.removeVars("Ticket,Cabin").bindVars(famSize, ticket, cabin, title).copy();
    }

    public CustomFilter newInstance() {
        return new CustomFilter();
    }

    public String[] varNames() {
        return new String[0];
    }
}
