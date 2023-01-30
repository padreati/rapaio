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

package rapaio.data.preprocessing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.ml.model.rule.OneRule;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/28/19.
 */
public class ImputeClassifierTest {

    private static final String[] xd = new String[] {"a", "b", "c", "d", "?"};
    private static final String[] yd = new String[] {"x", "y", "?"};

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(123);
    }

    @Test
    void testBasic() {

        VarNominal x = VarNominal.from(100, row -> xd[random.nextInt(xd.length)]).name("x");
        VarNominal y = VarNominal.from(100, row -> yd[random.nextInt(yd.length)]).name("y");

        var model = OneRule.newModel();

        ImputeClassifier xfilter = ImputeClassifier.of(model, VarRange.of("x,y"), "x").newInstance();
        ImputeClassifier yfilter = ImputeClassifier.of(model, VarRange.of("x,y"), "y");

        Frame df = SolidFrame.byVars(x, y);

        Frame copy = df.copy().fapply(xfilter, yfilter);

        for (int i = 0; i < 100; i++) {
            assertFalse(copy.isMissing(i, "x"));
            assertFalse(copy.isMissing(i, "y"));
        }
    }
}
