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
public interface Gradient {

    Color getColor(double value);

    Gradient forRange(double rangeStart, double rangeEnd);

    static Gradient newBiColorGradient(Color start, Color end) {
        return new BiColorGradient(start, end, Double.NaN, Double.NaN);
    }

    static Gradient newBiColorGradient(Color start, Color end, double rangeStart, double rangeEnd) {
        return new BiColorGradient(start, end, rangeStart, rangeEnd);
    }

    static Gradient newHueGradient() {
        return newHueGradient(0, 256);
    }

    static Gradient newHueGradient(int from, int to) {
        return newHueGradient(from, to, Double.NaN, Double.NaN);
    }

    static Gradient newHueGradient(int from, int to, double rangeFrom, double rangeTo) {
        return new HueGradient(from, to, rangeFrom, rangeTo);
    }

    static Gradient newMonoHueGradient(float hue, float minSat, float maxSat, float brightness, double rangeStart, double rangeEnd) {
        return new MonoHueGradient(hue, minSat, maxSat, brightness, rangeStart, rangeEnd);
    }

    class BiColorGradient implements Gradient {

        private final Color start;
        private final Color end;
        private final double rangeStart;
        private final double rangeEnd;

        BiColorGradient(Color start, Color end, double rangeStart, double rangeEnd) {
            this.start = start;
            this.end = end;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        @Override
        public Color getColor(double value) {
            value = Math.max(rangeStart, Math.min(rangeEnd, value));
            double p = (value - rangeStart) / (rangeEnd - rangeStart);
            int r = (int) (start.getRed() * p + end.getRed() * (1 - p));
            int g = (int) (start.getGreen() * p + end.getGreen() * (1 - p));
            int b = (int) (start.getBlue() * p + end.getBlue() * (1 - p));
            return new Color(r, g, b, 255);
        }

        @Override
        public Gradient forRange(double rangeStart, double rangeEnd) {
            return new BiColorGradient(start, end, rangeStart, rangeEnd);
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/10/15.
     */
    class HueGradient implements Gradient {

        private final int from;
        private final int to;
        private final double rangeStart;
        private final double rangeEnd;

        HueGradient(int from, int to, double rangeStart, double rangeEnd) {
            this.from = from;
            this.to = to;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        @Override
        public Color getColor(double value) {
            value = Math.max(rangeStart, Math.min(rangeEnd, value));
            double p = (value - rangeStart) / (rangeEnd - rangeStart);
            if (from < to) {
                return new Color(Color.HSBtoRGB(
                        (float) ((from + p * Math.abs(to - from)) / 360.0), 1f, 1f));
            } else {
                return new Color(Color.HSBtoRGB(
                        (float) ((from - p * Math.abs(to - from)) / 360.0), 1f, 1f));
            }
        }

        @Override
        public Gradient forRange(double rangeStart, double rangeEnd) {
            return new HueGradient(from, to, rangeStart, rangeEnd);
        }
    }

    class MonoHueGradient implements Gradient {

        private final float hue;
        private final float minSat;
        private final float maxSat;
        private final float brightness;
        private final double rangeStart;
        private final double rangeEnd;

        public MonoHueGradient(float hue, float minSat, float maxSat, float brightness, double rangeStart, double rangeEnd) {
            this.hue = hue;
            this.minSat = minSat;
            this.maxSat = maxSat;
            this.brightness = brightness;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        @Override
        public Color getColor(double value) {
            value = Math.max(rangeStart, Math.min(rangeEnd, value));
            double p = (value - rangeStart) / (rangeEnd - rangeStart);
            float sat = (float) (minSat + (maxSat - minSat) * p);
            return new Color(Color.HSBtoRGB(hue, sat, brightness));
        }

        @Override
        public Gradient forRange(double rangeStart, double rangeEnd) {
            return new MonoHueGradient(hue, minSat, maxSat, brightness, rangeStart, rangeEnd);
        }
    }
}
