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

import rapaio.data.Var;
import rapaio.graphics.Plot;
import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.opt.ColorPalette;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public abstract class PlotComponent extends BaseFigure {

    protected Plot parent;

    public void initialize(Plot parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent plot reference is null");
        }
        this.parent = parent;
    }

    public abstract void paint(Graphics2D g2d);

    @Override
    public PlotComponent color(int index) {
        super.color(index);
        return this;
    }

    @Override
    public PlotComponent color(Color color) {
        super.color(color);
        return this;
    }

    @Override
    public PlotComponent color(Var color) {
        super.color(color);
        return this;
    }

    @Override
    public PlotComponent lwd(float lwd) {
        super.lwd(lwd);
        return this;
    }

    @Override
    public PlotComponent sz(Var sizeIndex) {
        super.sz(sizeIndex);
        return this;
    }

    @Override
    public PlotComponent sz(double size) {
        super.sz(size);
        return this;
    }

    @Override
    public PlotComponent pch(Var pchIndex) {
        super.pch(pchIndex);
        return this;
    }

    @Override
    public PlotComponent pch(int pch) {
        super.pch(pch);
        return this;
    }

    @Override
    public PlotComponent alpha(float alpha) {
        return (PlotComponent)super.alpha(alpha);
    }

    @Override
    public float getAlpha() {
        return super.getAlpha();
    }

    @Override
    public PlotComponent colorPalette(ColorPalette colorPalette) {
        return (PlotComponent) super.colorPalette(colorPalette);
    }
}
