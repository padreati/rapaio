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

package rapaio.core.tests;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.StudentT;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class KSTestTest {

    @Test
    public void testPearson() throws IOException, URISyntaxException {
        Frame df = Datasets.loadPearsonHeightDataset();
        KSTest test = new KSTest("2-sample pearson", df.var("Son"), df.var("Father"));
        test.summary();

        Assert.assertEquals(0.150278, test.d(), 10e-5);
        Assert.assertEquals(0.0000000000411316, test.pValue(), 10e-10);
    }

    @Test
    public void testNormal() {
        Normal d = new Normal(0, 1);
        Numeric sample = d.sample(1000);
        KSTest test = new KSTest("normal sample", sample, d);
        test.summary();
        Assert.assertTrue(test.d() < 0.4);
        Assert.assertTrue(test.pValue() > 0.08);
    }

    @Test
    public void testUniform() {
        Numeric sample = new Uniform(0, 1).sample(1_000);
        KSTest test = new KSTest("uniform sample", sample, new Normal(0, 1));
        test.summary();
        Assert.assertTrue(test.d() > 0.4);
        Assert.assertTrue(test.pValue() < 0.001);
    }

    @Test
    public void testStudentT() {
        StudentT d = new StudentT(3, 0, 1);
        Numeric sample = d.sample(1000);
        KSTest test = new KSTest("studentT sample", sample, new Normal(0, 1));
        test.summary();
        Assert.assertTrue(test.d() > 0.04);
        Assert.assertTrue(test.pValue() < 0.05);
    }
}
