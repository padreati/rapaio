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

package rapaio.graphics.opt;

import java.awt.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public class BiColorGradient {

    private final Color start;
    private final Color end;
    private final double[] p;
    private Color[] colors;

    public BiColorGradient(Color start, Color end, double[] p) {
        this.start = start;
        this.end = end;
        this.p = p;

        buildColors();
    }

    private void buildColors() {
        this.colors = new Color[p.length];
        for (int i = 0; i < p.length; i++) {
            double pp = p[i];
            int r = (int) (start.getRed() * pp + end.getRed() * (1 - pp));
            int g = (int) (start.getGreen() * pp + end.getGreen() * (1 - pp));
            int b = (int) (start.getBlue() * pp + end.getBlue() * (1 - pp));
            int a = (int) (start.getAlpha() * pp + end.getAlpha() * (1 - pp));
            colors[i] = new Color(r, g, b, a);
        }
    }

    public Color[] getColors() {
        return colors;
    }


    public Color getColor(int i) {
        return colors[i];
    }
}
