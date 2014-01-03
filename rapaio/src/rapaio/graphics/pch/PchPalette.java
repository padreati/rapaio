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
package rapaio.graphics.pch;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum PchPalette {

    STANDARD(new StandardPchPalette());
    private final Mapping mapping;

    private PchPalette(Mapping mapping) {
        this.mapping = mapping;
    }

    public void draw(Graphics2D g2d, int x, int y, double size, int pch) {
        mapping.draw(g2d, x, y, size, pch);
    }

    public static interface Mapping {
        void draw(Graphics2D g2d, int x, int y, double size, int pch);
    }
}
