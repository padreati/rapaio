/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package unlinkedlist;

import static rapaio.graphics.opt.GOptions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import rapaio.graphics.Plotter;
import rapaio.graphics.opt.Palette;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.decomposition.DoubleSVDecomposition;
import rapaio.sys.WS;

public class Sandbox {

    public static void main(String[] args) throws IOException {
        BufferedImage img = ImageTools.readImage(new File("/home/ati/data/images/dataset-lda.png"));
        byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
        DMatrix m = DMatrix.empty(img.getWidth(), img.getHeight());
        int pos = 0;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                byte b = data[pos++];
                m.set(i, j, ((int) b & 0xff));
            }
        }

        int n = 250;
        DoubleSVDecomposition svd = m.svd();
        var uf = svd.u().rangeCols(0, n);
        var sf = svd.s().rangeCols(0, n).rangeRows(0, n);
        var vf = svd.v().rangeCols(0, n);

        DMatrix reducedFirst = uf.dot(sf).dot(vf.t());

        var ul = svd.u().rangeCols(n, img.getWidth());
        var sl = svd.s().rangeCols(n, img.getWidth()).rangeRows(n, img.getHeight());
        var vl = svd.v().rangeCols(n, img.getWidth());

        DMatrix reducedLast = ul.dot(sl).dot(vl.t());

        WS.draw(GridLayer.of(1, 3)
                .add(Plotter.matrix(m, palette(Palette.bicolor(Color.WHITE, Color.BLACK, 0, 255))))
                .add(Plotter.matrix(reducedFirst, palette(Palette.bicolor(Color.WHITE, Color.BLACK, 0, 255))))
                .add(Plotter.matrix(reducedLast, palette(Palette.bicolor(Color.WHITE, Color.BLACK, 0, 255))))
        );
    }
}
