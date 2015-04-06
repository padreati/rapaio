/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class ImageChart extends HostFigure {

    private final BufferedImage image;

    public ImageChart(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Range buildRange() {
        return null;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        g2d.drawImage(image, null, 0, 0);
    }
}
