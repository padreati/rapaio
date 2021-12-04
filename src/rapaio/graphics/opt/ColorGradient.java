/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.opt;

import java.awt.Color;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/10/15.
 */
public interface ColorGradient {

    Color[] getColors();

    default Color getColor(int i) {
        return getColors()[i];
    }

    static ColorGradient newBiColorGradient(Color start, Color end, double... p) {
        return new BiColorGradient(start, end, p);
    }

    static ColorGradient newHueGradient(double[] p) {
        return new HueGradient(0, 256, p);
    }

    static ColorGradient newMonoHueGradient(float hue, float minSat, float maxSat, float brightness, double... p) {
        return new MonoHueGradient(hue, minSat, maxSat, brightness, p);
    }

    static ColorGradient newHueGradient(int from, int to, double[] p) {
        return new HueGradient(from, to, p);
    }

    class BiColorGradient implements ColorGradient {

        private final Color[] colors;

        BiColorGradient(Color start, Color end, double... p) {
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

        @Override
        public Color[] getColors() {
            return colors;
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/10/15.
     */
    class HueGradient implements ColorGradient {

        private final Color[] colors;

        HueGradient(int from, int to, double[] p) {
            boolean reverse = from > to;
            colors = new Color[p.length];
            for (int i = 0; i < p.length; i++) {
                if (!reverse) {
                    colors[i] = new Color(Color.HSBtoRGB(
                            (float) ((from + p[i] * Math.abs(to - from)) / 360.0), 1f, 1f));
                } else {
                    colors[i] = new Color(Color.HSBtoRGB(
                            (float) ((from - p[i] * Math.abs(to - from)) / 360.0), 1f, 1f));
                }
            }
        }

        @Override
        public Color[] getColors() {
            return colors;
        }
    }

    class MonoHueGradient implements ColorGradient {

        private final Color[] colors;

        public MonoHueGradient(float hue, float minSat, float maxSat, float brightness, double... p) {
            colors = new Color[p.length];
            for (int i = 0; i < p.length; i++) {
                float sat = (float) (minSat + (maxSat - minSat) * p[i]);
                colors[i] = new Color(Color.HSBtoRGB(hue, sat, brightness));
            }
        }

        @Override
        public Color[] getColors() {
            return colors;
        }
    }
}
