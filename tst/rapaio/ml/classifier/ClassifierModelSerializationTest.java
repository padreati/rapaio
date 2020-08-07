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

package rapaio.ml.classifier;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.io.JavaIO;
import rapaio.ml.classifier.bayes.NaiveBayes;
import rapaio.ml.classifier.bayes.nb.Estimator;
import rapaio.ml.classifier.bayes.nb.KernelEstimator;
import rapaio.ml.classifier.rule.OneRule;
import rapaio.ml.eval.metric.Confusion;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassifierModelSerializationTest {

    @Test
    void testOneRuleIris() throws IOException, ClassNotFoundException {

        Var varModel = VarNominal.empty();
        Var varData = VarNominal.empty();
        Var varAcc = VarDouble.empty();

        Frame iris = Datasets.loadIrisDataset();
        testModel(OneRule.newModel(), iris, "class", "iris", varModel, varData, varAcc);
        testModel(NaiveBayes.newModel().estimators.add(KernelEstimator.forType(iris, VType.DOUBLE).toArray(new Estimator[0])), iris, "class", "iris", varModel, varData, varAcc);
        testModel(CTree.newC45(), iris, "class", "iris", varModel, varData, varAcc);
        testModel(CTree.newCART(), iris, "class", "iris", varModel, varData, varAcc);

        SolidFrame.byVars(varData, varModel, varAcc).printHead();
    }

    private <T extends ClassifierModel> void testModel(T model, Frame df, String target, String dataName, Var varModel, Var varData, Var varAcc) throws IOException, ClassNotFoundException {
        model.fit(df, target);
        model.printSummary();

        File tmp = File.createTempFile("model-", "ser");
        JavaIO.storeToFile(model, tmp);

        T shaddow = (T) JavaIO.restoreFromFile(tmp);

        var modelFit = model.predict(df);
        var shaddowFit = shaddow.predict(df);

        modelFit.printSummary();
        assertEquals(modelFit.toSummary(), shaddowFit.toSummary());

        varData.addLabel(dataName);
        varModel.addLabel(model.name());
        varAcc.addDouble(Confusion.from(df.rvar(target), modelFit.firstClasses()).accuracy());
    }
}
