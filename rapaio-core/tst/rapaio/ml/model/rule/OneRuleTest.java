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

package rapaio.ml.model.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.printer.opt.POpts.textWidth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.model.rule.onerule.HolteBinning;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRuleTest {

    private static final int SIZE = 6;

    private static final double TOLERANCE = 1e-12;

    private Var classVar;
    private Var heightVar;

    @BeforeEach
    void beforeEach() {
        classVar = VarNominal.empty(SIZE, "False", "True").name("class");
        classVar.setLabel(0, "True");
        classVar.setLabel(1, "True");
        classVar.setLabel(2, "True");
        classVar.setLabel(3, "False");
        classVar.setLabel(4, "False");
        classVar.setLabel(5, "False");

        heightVar = VarDouble.copy(0.1, 0.3, 0.5, 10, 10.3, 10.5).name("height");
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

        assertEquals("odor", mushroomsModel.getBestRuleSet().getVarName());
        assertEquals(0.985228951256, mushroomsModel.getBestRuleSet().getAccuracy(), TOLERANCE);

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

        VarNominal x = VarNominal.copy("a", "a", "a", "?", "?").name("x");
        VarNominal y = VarNominal.copy("x", "x", "x", "y", "y").name("y");

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

        assertEquals("""
                        OneRule model
                        ================

                        Description:
                        OneRule{}

                        Capabilities:
                        types inputs/targets: BINARY,INT,NOMINAL,DOUBLE,LONG/NOMINAL
                        counts inputs/targets: [1,1000000] / [1,1]
                        missing inputs/targets: true/false

                        Model fitted: true
                        input vars:\s
                        0. sepal-length : DOUBLE  |\s
                        1.  sepal-width : DOUBLE  |\s
                        2. petal-length : DOUBLE  |\s
                        3.  petal-width : DOUBLE  |\s

                        target vars:
                        > class : NOMINAL [?,setosa,versicolor,virginica]

                        BestRuleSet {var=petal-length, acc=0.9933333333333333}
                        > NumericRule {minValue=-Infinity,maxValue=2.45,class=setosa,errors=0,total=50,accuracy=1}
                        > NumericRule {minValue=2.45,maxValue=4.85,class=versicolor,errors=0,total=49,accuracy=1}
                        > NumericRule {minValue=4.85,maxValue=Infinity,class=virginica,errors=1,total=51,accuracy=0.9803921568627451}
                        """,
                irisModel.toSummary());

        Frame mushrooms = Datasets.loadMushrooms();

        OneRule modelMushrooms = OneRule.newModel();
        modelMushrooms.fit(mushrooms, "classes");

        assertEquals("""
                OneRule model
                ================

                Description:
                OneRule{}

                Capabilities:
                types inputs/targets: BINARY,INT,NOMINAL,DOUBLE,LONG/NOMINAL
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false

                Model fitted: true
                input vars:\s
                 0.                cap-shape : NOMINAL  | 11. stalk-surface-above-ring : NOMINAL  |\s
                 1.              cap-surface : NOMINAL  | 12. stalk-surface-below-ring : NOMINAL  |\s
                 2.                cap-color : NOMINAL  | 13.   stalk-color-above-ring : NOMINAL  |\s
                 3.                  bruises : NOMINAL  | 14.   stalk-color-below-ring : NOMINAL  |\s
                 4.                     odor : NOMINAL  | 15.                veil-type : NOMINAL  |\s
                 5.          gill-attachment : NOMINAL  | 16.               veil-color : NOMINAL  |\s
                 6.             gill-spacing : NOMINAL  | 17.              ring-number : NOMINAL  |\s
                 7.                gill-size : NOMINAL  | 18.                ring-type : NOMINAL  |\s
                 8.               gill-color : NOMINAL  | 19.        spore-print-color : NOMINAL  |\s
                 9.              stalk-shape : NOMINAL  | 20.               population : NOMINAL  |\s
                10.               stalk-root : NOMINAL  | 21.                  habitat : NOMINAL  |\s

                target vars:
                > classes : NOMINAL [?,p,e]

                BestRuleSet {var=odor, acc=0.9852289512555391}
                > NominalRule {value=?, class=p, errors=0, total=0, acc=0}
                > NominalRule {value=p, class=p, errors=0, total=256, acc=1}
                > NominalRule {value=a, class=e, errors=0, total=400, acc=1}
                > NominalRule {value=l, class=e, errors=0, total=400, acc=1}
                > NominalRule {value=n, class=e, errors=120, total=3,528, acc=0.9659863945578231}
                > NominalRule {value=f, class=p, errors=0, total=2,160, acc=1}
                > NominalRule {value=c, class=p, errors=0, total=192, acc=1}
                > NominalRule {value=y, class=p, errors=0, total=576, acc=1}
                > NominalRule {value=s, class=p, errors=0, total=576, acc=1}
                > NominalRule {value=m, class=p, errors=0, total=36, acc=1}
                """, modelMushrooms.toSummary(textWidth(100)));

        assertEquals(modelMushrooms.toContent(textWidth(100)), modelMushrooms.toSummary(textWidth(100)));
        assertEquals(modelMushrooms.toFullContent(textWidth(100)), modelMushrooms.toSummary(textWidth(100)));

        assertEquals("OneRule{}, fitted=true, rule set: RuleSet {var=odor, acc=0.9852289512555391}, " +
                        "NominalRule {value=?, class=p, errors=0, total=0, acc=0}, " +
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
