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

package rapaio.experiment.math;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;
import rapaio.printer.ImageTools;

public class TensorSandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {

        BufferedImage bi = ImageTools.readImage(
                new URI("https://img.freepik.com/free-photo/two-chicks-beach-ai-generated_268835-6148.jpg?w=1060&t=st=1706892885~exp=1706893485~hmac=1aa2364fe0170b12ec0004772ecb802c7eb596ff4a9b2cd780d151aaf8ab8eaf").toURL());
        System.out.println(bi.getData().getDataBuffer().getElem(1));

        int m = bi.getWidth();
        int n = bi.getHeight();

        var img = Tensors.zeros(Shape.of(n, m, 3));
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int clr = bi.getRGB(i, j);
                img.setDouble((clr & 0x00ff0000) >> 16, j, i, 0);
                img.setDouble((clr & 0x0000ff00) >> 8, j, i, 1);
                img.setDouble(clr & 0x000000ff, j, i, 2);
            }
        }
//        System.out.println(img);

//        WS.draw(gridLayer(3, 1)
//                .add(matrix(img.takesq(2,0), palette.redRGB()))
//                .add(matrix(img.takesq(2,1), palette.greenRGB()))
//                .add(matrix(img.takesq(2,2), palette.blueRGB()))
//        );

        var bw = img.mean(2);

//        var mean = bw.mean(0);
//        var std = bw.std(0);
//
//        var cbw = bw.bsub(0, mean).bdiv(0, std);
//        var eig = cbw.scatter().eig();
//
//        WS.draw(matrix(cbw.mm(eig.v()), GOptions.palette.bw(0, 255)));

//        PCA pca = PCA.newModel().fit(SolidFrame.matrix(bw.t()));
//        var tpca = pca.transform(SolidFrame.matrix(bw.t()), 100).dtNew();
//        WS.draw(matrix(tpca, palette.bw(0, 255)));


        var t = Tensors.stride(Shape.of(6, 5), 1, 2, -1, 4, 10, 3, -3, -3, 12, -15, 2, 1, -2, 4, 5,
                5, 1, -5, 10, 5, 2, 3, -3, 5, 12, 4, 0, -3, 16, 2);
        System.out.println(t);
    }
}
