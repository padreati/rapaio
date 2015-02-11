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

import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/26/15.
 */
public class SandboxComponent extends PlotComponent {

    @Override
    protected Range buildRange() {
        return new Range(0, 0, 100, 100);
    }

    @Override
    public void paint(Graphics2D g2d) {

        Path2D.Double path = new Path2D.Double();
        path.moveTo(parent.xScale(0), parent.yScale(0));
        path.lineTo(parent.xScale(0), parent.yScale(70));
        path.lineTo(parent.xScale(100), parent.yScale(30));
        path.lineTo(parent.xScale(100), parent.yScale(100));
        path.lineTo(parent.xScale(70), parent.yScale(100));
        path.lineTo(parent.xScale(20), parent.yScale(0));
        path.lineTo(parent.xScale(0), parent.yScale(0));

        g2d.setColor(Color.blue);
        g2d.fill(path);
    }
}
