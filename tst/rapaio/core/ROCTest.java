/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core;

import org.junit.Test;
import rapaio.core.stat.ROC;
import rapaio.data.Frame;
import rapaio.io.Csv;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCTest {

    @Test
    public void testFawcett() throws URISyntaxException, IOException {
        Frame df = new Csv()
                .withNominalFields("class")
                .withNumericFields("score")
                .read(getClass(), "fawcett-roc.csv");

        final ROC roc = new ROC(df.col("score"), df.col("class"), "p");
        Summary.head(roc.getData().rowCount(), roc.getData());

//        Workspace.draw(new Plot()
//                .add(new ROCCurve(roc))
//                .add(new Lines(roc.getData().getCol("tpr"), roc.getData().getCol("acc")).setColorIndex(1))
//                .add(new Lines(roc.getData().getCol("tpr"), roc.getData().getCol("acc")).setColorIndex(1)));
    }
}
