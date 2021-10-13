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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serial;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.Position;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

/**
 * Artist which displays an image.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/21.
 */
public class ImageArtist extends Artist {

    @Serial
    private static final long serialVersionUID = -1903664389725485554L;
    private final BufferedImage image;

    public ImageArtist(BufferedImage image, GOption<?>... opts) {
        this.image = image;
        this.options.bind(position(0, 0, image.getWidth(), image.getHeight()));
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        Position pos = options.getPosition();
        union(pos.x(), pos.y());
        union(pos.x() + pos.width(), pos.y() + pos.height());
    }

    @Override
    public void paint(Graphics2D g2d) {
        Position pos = options.getPosition();
        g2d.drawImage(image,
                (int) xScale(pos.x()),
                (int) yScale(pos.height()),
                (int) xScale(pos.width()) - (int) xScale(pos.x()),
                -(int) yScale(pos.height()) + (int) yScale(pos.y()),
                null);
    }
}
