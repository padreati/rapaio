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

package rapaio.graphics.plot.artist;

import static rapaio.sys.With.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serial;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

/**
 * Artist which displays an image onto a plot.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/21.
 */
public class ImageArtist extends Artist {

    @Serial
    private static final long serialVersionUID = -1903664389725485554L;
    private final BufferedImage image;

    public ImageArtist(BufferedImage image, GOption<?>... opts) {
        this.image = image;
        this.options.bind(position(0, image.getHeight(), image.getWidth(), image.getHeight()));
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        Rectangle2D pos = options.getPosition();
        union(pos.getX(), pos.getY());
        union(pos.getX() + pos.getWidth(), pos.getY() - pos.getHeight());
    }

    @Override
    public void paint(Graphics2D g2d) {
        Rectangle2D pos = options.getPosition();
        g2d.drawImage(image,
                (int) xScale(pos.getX()),
                (int) yScale(pos.getY()),
                (int) xScale(pos.getX() + pos.getWidth()) - (int) xScale(pos.getX()),
                (int) yScale(pos.getY() - pos.getHeight()) - (int) yScale(pos.getY()),
                null);
    }
}
