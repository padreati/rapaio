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

package rapaio.data.filter.frame;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/3/16.
 */
public class FFRandomProjectionTest {

    @Test
    public void gausianSDTest() throws IOException, URISyntaxException {

        RandomSource.setSeed(1);
        FFRandomProjection rp = FFRandomProjection.newGaussianSd(3, VRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fitApply(rp);
        df.printSummary();

        Assert.assertEquals(3, df.getVarCount());
        Assert.assertEquals("RP_1", df.getVar(0).getName());
        Assert.assertEquals("RP_2", df.getVar(1).getName());
        Assert.assertEquals("RP_3", df.getVar(2).getName());


        double corr = CoreTools.corrPearson(df).singleValue();
        Assert.assertEquals(0.40856545876413647, corr, 1e-20);
    }

    @Test
    public void achioptasTest() throws IOException, URISyntaxException {

        RandomSource.setSeed(1);
        FFRandomProjection rp = FFRandomProjection.newAchlioptas(3, VRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fitApply(rp);
        df.printSummary();

        Assert.assertEquals(3, df.getVarCount());
        Assert.assertEquals("RP_1", df.getVar(0).getName());
        Assert.assertEquals("RP_2", df.getVar(1).getName());
        Assert.assertEquals("RP_3", df.getVar(2).getName());


        double corr = CoreTools.corrPearson(df).singleValue();
        Assert.assertEquals(-0.5035565970961097, corr, 1e-20);
    }

    @Test
    public void achioptas5Test() throws IOException, URISyntaxException {

        RandomSource.setSeed(1);
        FFRandomProjection rp = FFRandomProjection.newAchlioptas(3, 5, VRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fitApply(rp);
        df.printSummary();

        Assert.assertEquals(3, df.getVarCount());
        Assert.assertEquals("RP_1", df.getVar(0).getName());
        Assert.assertEquals("RP_2", df.getVar(1).getName());
        Assert.assertEquals("RP_3", df.getVar(2).getName());


        double corr = CoreTools.corrPearson(df).singleValue();
        Assert.assertEquals(-0.5195786390214067, corr, 1e-20);
    }
}
