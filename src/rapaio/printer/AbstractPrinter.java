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

package rapaio.printer;

import rapaio.graphics.base.Figure;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractPrinter implements Printer {

    private int textWidth;
    private int graphicWidth;
    private int graphicHeight;

    @Override
    public int textWidth() {
        return textWidth;
    }

    @Override
    public Printer withTextWidth(int chars) {
        textWidth = chars;
        return this;
    }

    @Override
    public int graphicWidth() {
        return graphicWidth;
    }

    @Override
    public void withGraphicWidth(int width) {
        graphicWidth = width;
    }

    @Override
    public int graphicHeight() {
        return graphicHeight;
    }

    @Override
    public void withGraphicHeight(int height) {
        graphicHeight = height;
    }

    @Override
    public void openPrinter() {
        // do nothing in standard setup
    }

    @Override
    public void closePrinter() {
        // do nothing in standard setup
    }

    @Override
    public void head(int h, String lines) {
        print("*" + lines + "*\n");
    }

    @Override
    public void code(String lines) {
        print(lines);
    }

    @Override
    public void p(String lines) {
        print(lines);
    }

    @Override
    public void draw(Figure figure) {
        draw(figure, graphicWidth(), graphicHeight());
    }
}
