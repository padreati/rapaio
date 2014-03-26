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

import rapaio.data.Vector;
import rapaio.graphics.Plot;
import rapaio.graphics.base.AbstractFigure;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public abstract class PlotComponent extends AbstractFigure {

    protected Plot parent;

    public void initialize() {
    }

    public void setParent(Plot parent) {
        this.parent = parent;
    }

    public Plot getParent() {
        return parent;
    }


    public abstract void paint(Graphics2D g2d);

    @Override
    public Color getColor(int row) {
        if (parent != null && colors == null) {
            return parent.getColor(row);
        }
        return super.getColor(row);
    }

    @Override
    public int getPch(int row) {
        if (parent != null && pchIndex == null) {
            return parent.getPch(row);
        }
        return super.getPch(row);
    }

    @Override
    public double getSize(int row) {
        if (parent != null && sizeIndex == null) {
            return parent.getSize(row);
        }
        return super.getSize(row);
    }

    @Override
    public float getLwd() {
        if (parent != null && lwd == null) {
            return parent.getLwd();
        }
        return super.getLwd();
    }

    @Override
    public PlotComponent setColor(int index) {
        super.setColor(index);
        return this;
    }

    @Override
    public PlotComponent setColor(Color color) {
        super.setColor(color);
        return this;
    }

    @Override
    public PlotComponent setColor(Vector color) {
        super.setColor(color);
        return this;
    }

    @Override
    public PlotComponent setLwd(float lwd) {
        super.setLwd(lwd);
        return this;
    }

    @Override
    public PlotComponent setSize(Vector sizeIndex) {
        super.setSize(sizeIndex);
        return this;
    }

    @Override
    public PlotComponent setSize(double size) {
        super.setSize(size);
        return this;
    }

    @Override
    public PlotComponent setPch(Vector pchIndex) {
        super.setPch(pchIndex);
        return this;
    }

    @Override
    public PlotComponent setPch(int pch) {
        super.setPch(pch);
        return this;
    }
}
