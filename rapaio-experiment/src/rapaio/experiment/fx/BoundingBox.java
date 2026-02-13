/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.experiment.fx;

import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;

public class BoundingBox extends Box {

    public BoundingBox(double size, double pstar) {
        super(size, size, size);

        PhongMaterial marginMaterial = new PhongMaterial();
        marginMaterial.setDiffuseMap(backImage(1024, 1024, pstar));

        this.setCullFace(CullFace.FRONT);
        this.setMaterial(marginMaterial);
    }

    private Image backImage(int width, int height, double pstar) {

        double opacity = 1;
        double red = 0;
        double green = 0;
        double blue = 0;

        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();

        // Should really verify 0.0 <= red, green, blue, opacity <= 1.0
        int alpha = (int) (opacity * 255);
        int r = (int) (red * 255);
        int g = (int) (green * 255);
        int b = (int) (blue * 255);

        int pixel = (alpha << 24) | (r << 16) | (g << 8) | b;
        int[] pixels = new int[width * height];

        Random random = new Random();
        for (int i = 0; i < width * height; i++) {
            if (random.nextDouble() > pstar) {
                pixels[i] = pixel;
            } else {
                pixels[i] = 0xFF000000 | random.nextInt(0x00FFFFFF);
            }
        }

        pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return img;
    }


}
