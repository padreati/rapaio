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

package rapaio.graphics.opt;

import rapaio.data.IndexVar;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.util.func.SFunction;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Graphical aspect options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/31/15.
 */
public class GOpts implements Serializable {

    public static final GOpts DEFAULTS;
    private static final long serialVersionUID = -8407683729055712796L;

    static {
        DEFAULTS = new GOpts();
        DEFAULTS.palette = gOpts -> ColorPalette.STANDARD;
        DEFAULTS.color = gOpts -> new Color[]{Color.black};
        DEFAULTS.lwd = gOpts -> 1.0f;
        DEFAULTS.sz = gOpts -> NumericVar.scalar(3);
        DEFAULTS.pch = gOpts -> IndexVar.scalar(0);
        DEFAULTS.alpha = gOpts -> 1.0f;
        DEFAULTS.bins = gOpts -> -1;
        DEFAULTS.prob = gOpts -> false;
        DEFAULTS.points = gOpts -> 256;
        DEFAULTS.labels = gOpts -> new String[]{""};
    }

    GOpts parent;
    //
    SFunction<GOpts, ColorPalette> paletteDefault;
    SFunction<GOpts, Color[]> colorDefault;
    SFunction<GOpts, Float> lwdDefault;
    SFunction<GOpts, Var> szDefault;
    SFunction<GOpts, Var> pchDefault;
    SFunction<GOpts, Float> alphaDefault;
    SFunction<GOpts, Integer> binsDefault;
    SFunction<GOpts, Boolean> probDefault;
    SFunction<GOpts, Integer> pointsDefault;
    SFunction<GOpts, String[]> labelsDefault;

    SFunction<GOpts, ColorPalette> palette;
    SFunction<GOpts, Color[]> color;
    SFunction<GOpts, Float> lwd;
    SFunction<GOpts, Var> sz;
    SFunction<GOpts, Var> pch;
    SFunction<GOpts, Float> alpha;
    SFunction<GOpts, Integer> bins;
    SFunction<GOpts, Boolean> prob;
    SFunction<GOpts, Integer> points;
    SFunction<GOpts, String[]> labels;

    public GOpts apply(GOpt... options) {
        Arrays.stream(options).forEach(o -> o.apply(this));
        return this;
    }

    public GOpt[] toArray() {
        return new GOpt[]{
                opt -> opt.setPalette(palette),
                opt -> opt.setColor(color),
                opt -> opt.setLwd(lwd),
                opt -> opt.setSz(sz),
                opt -> opt.setPch(pch),
                opt -> opt.setAlpha(alpha),
                opt -> opt.setBins(bins),
                opt -> opt.setProb(prob),
                opt -> opt.setPoints(points),
                opt -> opt.setLabels(labels)
        };
    }

    // getters

    public GOpts getParent() {
        return parent;
    }

    public void setParent(GOpts parent) {
        this.parent = parent;
    }

    public ColorPalette getPalette() {
        if (palette == null) {
            return parent != null ? parent.getPalette() : DEFAULTS.palette.apply(this);
        }
        return palette.apply(this);
    }

    public void setPalette(SFunction<GOpts, ColorPalette> palette) {
        this.palette = palette;
    }

    public Color getColor(int row) {
        SFunction<GOpts, Color[]> c = getUpColor();
        if (c == null)
            c = getUpColorDefault();
        if (c == null)
            c = DEFAULTS.color;
        return c.apply(this)[row % c.apply(this).length];
    }

    private SFunction<GOpts, Color[]> getUpColor() {
        if (color != null)
            return color;
        if (parent != null)
            return parent.getUpColor();
        return null;
    }

    private SFunction<GOpts, Color[]> getUpColorDefault() {
        if (colorDefault != null)
            return colorDefault;
        if (parent != null)
            return parent.getUpColorDefault();
        return null;
    }

    public float getLwd() {
        SFunction<GOpts, Float> c = getUpLwd();
        if (c == null)
            c = getUpLwdDefault();
        if (c == null)
            c = DEFAULTS.lwd;
        return c.apply(this);
    }

    public void setLwd(SFunction<GOpts, Float> lwd) {
        this.lwd = lwd;
    }

    private SFunction<GOpts, Float> getUpLwd() {
        if (lwd != null)
            return lwd;
        if (parent != null)
            return parent.getUpLwd();
        return null;
    }

    private SFunction<GOpts, Float> getUpLwdDefault() {
        if (lwdDefault != null)
            return lwdDefault;
        if (parent != null)
            return parent.getUpLwdDefault();
        return null;
    }

    public double getSz(int row) {
        SFunction<GOpts, Var> c = getUpSz();
        if (c == null)
            c = getUpSzDefault();
        if (c == null)
            c = DEFAULTS.sz;
        return c.apply(this).getValue(row % c.apply(this).getRowCount());
    }

    private SFunction<GOpts, Var> getUpSz() {
        if (sz != null)
            return sz;
        if (parent != null)
            return parent.getUpSz();
        return null;
    }

    private SFunction<GOpts, Var> getUpSzDefault() {
        if (szDefault != null)
            return szDefault;
        if (parent != null)
            return parent.getUpSzDefault();
        return null;
    }

    public int getPch(int row) {
        SFunction<GOpts, Var> c = getUpPch();
        if (c == null)
            c = getUpPchDefault();
        if (c == null)
            c = DEFAULTS.pch;
        return c.apply(this).getIndex(row % c.apply(this).getRowCount());
    }

    private SFunction<GOpts, Var> getUpPch() {
        if (pch != null)
            return pch;
        if (parent != null)
            return parent.getUpPch();
        return null;
    }

    private SFunction<GOpts, Var> getUpPchDefault() {
        if (pchDefault != null)
            return pchDefault;
        if (parent != null)
            return parent.getUpPchDefault();
        return null;
    }

    public float getAlpha() {
        SFunction<GOpts, Float> c = getUpAlpha();
        if (c == null)
            c = getUpAlphaDefault();
        if (c == null)
            c = DEFAULTS.alpha;
        return c.apply(this);
    }

    public void setAlpha(SFunction<GOpts, Float> alpha) {
        this.alpha = alpha;
    }

    protected SFunction<GOpts, Float> getUpAlpha() {
        if (alpha != null)
            return alpha;
        if (parent != null)
            return parent.getUpAlpha();
        return null;
    }

    protected SFunction<GOpts, Float> getUpAlphaDefault() {
        if (alphaDefault != null)
            return alphaDefault;
        if (parent != null)
            return parent.getUpAlphaDefault();
        return null;
    }

    public int getBins() {
        SFunction<GOpts, Integer> c = getUpBins();
        if (c == null)
            c = getUpBinsDefault();
        if (c == null)
            c = DEFAULTS.bins;
        return c.apply(this);
    }

    public void setBins(SFunction<GOpts, Integer> bins) {
        this.bins = bins;
    }

    protected SFunction<GOpts, Integer> getUpBins() {
        if (bins != null)
            return bins;
        if (parent != null)
            return parent.getUpBins();
        return null;
    }

    protected SFunction<GOpts, Integer> getUpBinsDefault() {
        if (binsDefault != null)
            return binsDefault;
        if (parent != null)
            return parent.getUpBinsDefault();
        return null;
    }

    public boolean getProb() {
        SFunction<GOpts, Boolean> c = getUpProb();
        if (c == null)
            c = getUpProbDefault();
        if (c == null)
            c = DEFAULTS.prob;
        return c.apply(this);
    }

    public void setProb(SFunction<GOpts, Boolean> prob) {
        this.prob = prob;
    }

    protected SFunction<GOpts, Boolean> getUpProb() {
        if (prob != null)
            return prob;
        if (parent != null)
            return parent.getUpProb();
        return null;
    }

    protected SFunction<GOpts, Boolean> getUpProbDefault() {
        if (probDefault != null)
            return probDefault;
        if (parent != null)
            return parent.getUpProbDefault();
        return null;
    }

    public int getPoints() {
        SFunction<GOpts, Integer> c = getUpPoints();
        if (c == null)
            c = getUpPointsDefault();
        if (c == null)
            c = DEFAULTS.points;
        return c.apply(this);
    }

    public void setPoints(SFunction<GOpts, Integer> points) {
        this.points = points;
    }

    protected SFunction<GOpts, Integer> getUpPoints() {
        if (points != null)
            return points;
        if (parent != null)
            return parent.getUpPoints();
        return null;
    }

    protected SFunction<GOpts, Integer> getUpPointsDefault() {
        if (pointsDefault != null)
            return pointsDefault;
        if (parent != null)
            return parent.getUpPointsDefault();
        return null;
    }

    public String[] getLabels() {
        SFunction<GOpts, String[]> c = getUpLabels();
        if (c == null)
            c = getUpLabelsDefault();
        if (c == null)
            c = DEFAULTS.labels;
        return c.apply(this);
    }

    public void setLabels(SFunction<GOpts, String[]> labels) {
        this.labels = labels;
    }

    protected SFunction<GOpts, String[]> getUpLabels() {
        if (labels != null)
            return labels;
        if (parent != null)
            return parent.getUpLabels();
        return null;
    }

    protected SFunction<GOpts, String[]> getUpLabelsDefault() {
        if (labelsDefault != null)
            return labelsDefault;
        if (parent != null)
            return parent.getUpLabelsDefault();
        return null;
    }

    public void setColor(SFunction<GOpts, Color[]> color) {
        this.color = color;
    }

    public void setSz(SFunction<GOpts, Var> sz) {
        this.sz = sz;
    }

    public void setPch(SFunction<GOpts, Var> pch) {
        this.pch = pch;
    }

    public void setPaletteDefault(SFunction<GOpts, ColorPalette> paletteDefault) {
        this.paletteDefault = paletteDefault;
    }

    public void setColorDefault(SFunction<GOpts, Color[]> colorDefault) {
        this.colorDefault = colorDefault;
    }

    public void setLwdDefault(SFunction<GOpts, Float> lwdDefault) {
        this.lwdDefault = lwdDefault;
    }

    public void setSzDefault(SFunction<GOpts, Var> szDefault) {
        this.szDefault = szDefault;
    }

    public void setPchDefault(SFunction<GOpts, Var> pchDefault) {
        this.pchDefault = pchDefault;
    }

    public void setAlphaDefault(SFunction<GOpts, Float> alphaDefault) {
        this.alphaDefault = alphaDefault;
    }

    public void setBinsDefault(SFunction<GOpts, Integer> binsDefault) {
        this.binsDefault = binsDefault;
    }

    public void setProbDefault(SFunction<GOpts, Boolean> probDefault) {
        this.probDefault = probDefault;
    }

    public void setPointsDefault(SFunction<GOpts, Integer> pointsDefault) {
        this.pointsDefault = pointsDefault;
    }

    public void setLabelsDefault(SFunction<GOpts, String[]> labels) {
        this.labels = labelsDefault;
    }
}
