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

package rapaio.ml.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.io.JavaIO;
import rapaio.ml.eval.metric.Confusion;
import rapaio.ml.model.bayes.NaiveBayes;
import rapaio.ml.model.bayes.nb.KernelEstimator;
import rapaio.ml.model.rule.OneRule;
import rapaio.ml.model.tree.CTree;

public class ClassifierModelSerializationTest {

    @Test
    void testOneRuleIris() throws IOException, ClassNotFoundException {

        Var varModel = VarNominal.empty();
        Var varData = VarNominal.empty();
        Var varAcc = VarDouble.empty();

        Frame iris = Datasets.loadIrisDataset();
        testModel(OneRule.newModel(), iris, varModel, varData, varAcc);
        testModel(NaiveBayes.newModel().estimators.set(KernelEstimator.forType(iris, VarType.DOUBLE)), iris, varModel, varData, varAcc);
        testModel(CTree.newC45(), iris, varModel, varData, varAcc);
        testModel(CTree.newCART(), iris, varModel, varData, varAcc);
    }

    @SuppressWarnings("unchecked")
    private <T extends ClassifierModel> void testModel(T model, Frame df, Var varModel, Var varData, Var varAcc) throws IOException, ClassNotFoundException {
        model.fit(df, "class");

        File tmp = File.createTempFile("model-", "ser");
        JavaIO.storeToFile(model, tmp);

        T shaddow = (T) JavaIO.restoreFromFile(tmp);

        var modelFit = model.predict(df);
        var shaddowFit = shaddow.predict(df);

        assertEquals(modelFit.toSummary(), shaddowFit.toSummary());

        varData.addLabel("iris");
        varModel.addLabel(model.name());
        varAcc.addDouble(Confusion.from(df.rvar("class"), modelFit.firstClasses()).accuracy());
    }
}
