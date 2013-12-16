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
package rapaio.graphics.plot;


import java.awt.*;
import rapaio.data.Vector;
import rapaio.graphics.base.AbstractFigure;

/**
 * @author Aurelian Tutuianu
 */
public abstract class PlotComponent extends AbstractFigure {

    public abstract void paint(Graphics2D g2d);

    @Override
    public PlotComponent setLwd(float lwd) {
        super.setLwd(lwd);
        return this;
    }

    @Override
    public PlotComponent setSizeIndex(Vector sizeIndex) {
        super.setSizeIndex(sizeIndex);
        return this;
    }

    @Override
    public PlotComponent setSizeIndex(double size) {
        super.setSizeIndex(size);
        return this;
    }

    @Override
    public PlotComponent setColorIndex(Vector colorIndex) {
        super.setColorIndex(colorIndex);
        return this;
    }

    @Override
    public PlotComponent setColorIndex(int colorIndex) {
        super.setColorIndex(colorIndex);
        return this;
    }
    @Override
    public PlotComponent setPchIndex(Vector pchIndex) {
        super.setPchIndex(pchIndex);
        return this;
    }

    @Override
    public PlotComponent setPchIndex(int pch) {
        super.setPchIndex(pch);
        return this;
    }
    @Override
    public PlotComponent setXRange(double start, double end) {
        super.setXRange(start, end);
        return this;
    }

    @Override
    public PlotComponent setYRange(double start, double end) {
        super.setYRange(start, end);
        return this;
    }
}
