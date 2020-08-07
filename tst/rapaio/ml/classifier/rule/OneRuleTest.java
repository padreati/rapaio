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

package rapaio.ml.classifier.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.rule.onerule.HolteBinning;
import rapaio.sys.WS;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.printer.Printer.textWidth;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class OneRuleTest {

    private static final int SIZE = 6;

    private static final double TOLERANCE = 1e-12;

    private Var classVar;
    private Var heightVar;

    @BeforeEach
    void beforeEach() {
        classVar = VarNominal.empty(SIZE, "False", "True").withName("class");
        classVar.setLabel(0, "True");
        classVar.setLabel(1, "True");
        classVar.setLabel(2, "True");
        classVar.setLabel(3, "False");
        classVar.setLabel(4, "False");
        classVar.setLabel(5, "False");

        heightVar = VarDouble.copy(0.1, 0.3, 0.5, 10, 10.3, 10.5).withName("height");

        RandomSource.setSeed(1);
    }

    @Test
    void testNaming() {
        OneRule model = OneRule.newModel().binning.set(new HolteBinning(1));
        assertEquals("OneRule", model.name());
        assertEquals("OneRule{binning=HolteBinning(minCount=1)}", model.fullName());
        assertEquals("OneRule{binning=HolteBinning(minCount=1)}", model.newInstance().fullName());
    }

    @Test
    public void testNumeric() {
        Frame df = SolidFrame.byVars(SIZE, heightVar, classVar);

        String[] labels;
        OneRule oneRule = OneRule.newModel();

        oneRule = oneRule.binning.set(new HolteBinning(1));
        oneRule.fit(df, "class");
        var pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertEquals(labels[i], pred.firstClasses().getLabel(i));
        }

        oneRule.binning.set(new HolteBinning(2));
        oneRule.fit(df, "class");
        pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "TrueFalse", "TrueFalse", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertTrue(labels[i].contains(pred.firstClasses().getLabel(i)));
        }

        oneRule.binning.set(new HolteBinning(3));
        oneRule.fit(df, "class");
        pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertEquals(labels[i], pred.firstClasses().getLabel(i));
        }

        oneRule.binning.set(new HolteBinning(4));
        oneRule.fit(df, "class");
        pred = oneRule.predict(df);
        for (int i = 1; i < SIZE; i++) {
            assertEquals(pred.firstClasses().getLabel(i), pred.firstClasses().getLabel(0));
        }
    }

    @Test
    public void testFit() {

        Frame mushrooms = Datasets.loadMushrooms();
        var mushroomsModel = OneRule.newModel().fit(mushrooms, "classes");
        var predictionMushrooms = mushroomsModel.predict(mushrooms, true, true);

        assertEquals("odor", mushroomsModel.getBestRuleSet().getVarName());
        assertEquals(0.985228951256, mushroomsModel.getBestRuleSet().getAccuracy(), TOLERANCE);


        Frame iris = Datasets.loadIrisDataset();
        var irisModel = OneRule.newModel().fit(iris, "class");
        var predictionIris = irisModel.predict(iris, true, true);

        assertEquals("odor", mushroomsModel.getBestRuleSet().getVarName());
        assertEquals(0.985228951256, mushroomsModel.getBestRuleSet().getAccuracy(), TOLERANCE);

    }

    @Test
    void testNotTrained() {
        var ex = assertThrows(IllegalStateException.class, () -> OneRule.newModel().predict(Datasets.loadIrisDataset(), true, true));
        assertEquals("Best rule not found. Either the classifier was not trained, either something went wrong.", ex.getMessage());
    }

    @Test
    void predictMissing() {

        VarNominal x = VarNominal.copy("a", "a", "a", "?", "?").withName("x");
        VarNominal y = VarNominal.copy("x", "x", "x", "y", "y").withName("y");

        Frame df = SolidFrame.byVars(x, y);
        OneRule model = OneRule.newModel().fit(df, "y");

        df.setMissing(0, 0);

        model.missingHandler.set(OneRule.MissingHandler.MAJORITY);
        var resultMajority = model.predict(df);
        assertEquals("x", resultMajority.firstClasses().getLabel(0));

        model.missingHandler.set(OneRule.MissingHandler.CATEGORY);
        var resultCategory = model.predict(df);
        assertEquals("y", resultCategory.firstClasses().getLabel(0));
    }

    @Test
    public void testSummary() {
        Frame iris = Datasets.loadIrisDataset();
        OneRule irisModel = OneRule.newModel();
        irisModel.fit(iris, "class");

        assertEquals("OneRule{}, fitted=true, rule set: RuleSet {" +
                        "var=petal-length, acc=0.9933333333333333}, " +
                        "NumericRule {minValue=-Infinity,maxValue=2.45,class=setosa,errors=0,total=50,accuracy=1}, " +
                        "NumericRule {minValue=2.45,maxValue=4.85,class=versicolor,errors=0,total=49,accuracy=1}, " +
                        "NumericRule {minValue=4.85,maxValue=Infinity,class=virginica,errors=1,total=51,accuracy=0.9803921568627451}",
                irisModel.toString());

        assertEquals("OneRule model\n" +
                        "================\n" +
                        "\n" +
                        "Description:\n" +
                        "OneRule{}\n" +
                        "\n" +
                        "Capabilities:\n" +
                        "types inputs/targets: BINARY,INT,NOMINAL,DOUBLE,LONG/NOMINAL\n" +
                        "counts inputs/targets: [1,1000000] / [1,1]\n" +
                        "missing inputs/targets: true/false\n" +
                        "\n" +
                        "Model fitted: true\n" +
                        "input vars: \n" +
                        "0. sepal-length : DOUBLE  | \n" +
                        "1.  sepal-width : DOUBLE  | \n" +
                        "2. petal-length : DOUBLE  | \n" +
                        "3.  petal-width : DOUBLE  | \n" +
                        "\n" +
                        "target vars:\n" +
                        "> class : NOMINAL [?,setosa,versicolor,virginica]\n" +
                        "\n" +
                        "BestRuleSet {var=petal-length, acc=0.9933333333333333}\n" +
                        "> NumericRule {minValue=-Infinity,maxValue=2.45,class=setosa,errors=0,total=50,accuracy=1}\n" +
                        "> NumericRule {minValue=2.45,maxValue=4.85,class=versicolor,errors=0,total=49,accuracy=1}\n" +
                        "> NumericRule {minValue=4.85,maxValue=Infinity,class=virginica,errors=1,total=51,accuracy=0.9803921568627451}\n",
                irisModel.toSummary());

        Frame mushrooms = Datasets.loadMushrooms();

        OneRule modelMushrooms = OneRule.newModel();
        modelMushrooms.fit(mushrooms, "classes");

        int oldTextWidth = WS.getPrinter().getOptions().textWidth();
        WS.getPrinter().withOptions(textWidth(100));
        assertEquals("OneRule model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "OneRule{}\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: BINARY,INT,NOMINAL,DOUBLE,LONG/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model fitted: true\n" +
                "input vars: \n" +
                " 0.                cap-shape : NOMINAL  | 11. stalk-surface-above-ring : NOMINAL  | \n" +
                " 1.              cap-surface : NOMINAL  | 12. stalk-surface-below-ring : NOMINAL  | \n" +
                " 2.                cap-color : NOMINAL  | 13.   stalk-color-above-ring : NOMINAL  | \n" +
                " 3.                  bruises : NOMINAL  | 14.   stalk-color-below-ring : NOMINAL  | \n" +
                " 4.                     odor : NOMINAL  | 15.                veil-type : NOMINAL  | \n" +
                " 5.          gill-attachment : NOMINAL  | 16.               veil-color : NOMINAL  | \n" +
                " 6.             gill-spacing : NOMINAL  | 17.              ring-number : NOMINAL  | \n" +
                " 7.                gill-size : NOMINAL  | 18.                ring-type : NOMINAL  | \n" +
                " 8.               gill-color : NOMINAL  | 19.        spore-print-color : NOMINAL  | \n" +
                " 9.              stalk-shape : NOMINAL  | 20.               population : NOMINAL  | \n" +
                "10.               stalk-root : NOMINAL  | 21.                  habitat : NOMINAL  | \n" +
                "\n" +
                "target vars:\n" +
                "> classes : NOMINAL [?,p,e]\n" +
                "\n" +
                "BestRuleSet {var=odor, acc=0.9852289512555391}\n" +
                "> NominalRule {value=?, class=e, errors=0, total=0, acc=0}\n" +
                "> NominalRule {value=p, class=p, errors=0, total=256, acc=1}\n" +
                "> NominalRule {value=a, class=e, errors=0, total=400, acc=1}\n" +
                "> NominalRule {value=l, class=e, errors=0, total=400, acc=1}\n" +
                "> NominalRule {value=n, class=e, errors=120, total=3,528, acc=0.9659863945578231}\n" +
                "> NominalRule {value=f, class=p, errors=0, total=2,160, acc=1}\n" +
                "> NominalRule {value=c, class=p, errors=0, total=192, acc=1}\n" +
                "> NominalRule {value=y, class=p, errors=0, total=576, acc=1}\n" +
                "> NominalRule {value=s, class=p, errors=0, total=576, acc=1}\n" +
                "> NominalRule {value=m, class=p, errors=0, total=36, acc=1}\n", modelMushrooms.toSummary());
        WS.getPrinter().withOptions(textWidth(oldTextWidth));

        assertEquals(modelMushrooms.toContent(), modelMushrooms.toSummary());
        assertEquals(modelMushrooms.toFullContent(), modelMushrooms.toSummary());

        assertEquals("OneRule{}, fitted=true, rule set: RuleSet {var=odor, acc=0.9852289512555391}, " +
                        "NominalRule {value=?, class=e, errors=0, total=0, acc=0}, " +
                        "NominalRule {value=p, class=p, errors=0, total=256, acc=1}, " +
                        "NominalRule {value=a, class=e, errors=0, total=400, acc=1}, " +
                        "NominalRule {value=l, class=e, errors=0, total=400, acc=1}, " +
                        "NominalRule {value=n, class=e, errors=120, total=3,528, acc=0.9659863945578231}, " +
                        "NominalRule {value=f, class=p, errors=0, total=2,160, acc=1}, " +
                        "NominalRule {value=c, class=p, errors=0, total=192, acc=1}, " +
                        "NominalRule {value=y, class=p, errors=0, total=576, acc=1}, " +
                        "NominalRule {value=s, class=p, errors=0, total=576, acc=1}, " +
                        "NominalRule {value=m, class=p, errors=0, total=36, acc=1}",
                modelMushrooms.toString());

    }

}
