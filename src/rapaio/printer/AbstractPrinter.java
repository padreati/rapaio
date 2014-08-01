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

package rapaio.printer;

import rapaio.graphics.base.Figure;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public abstract class AbstractPrinter implements Printer {

    private int textWidth;
    private int graphicWidth;
    private int graphicHeight;

    @Override
    public int getTextWidth() {
        return textWidth;
    }

    @Override
    public void setTextWidth(int chars) {
        textWidth = chars;
    }

    @Override
    public int getGraphicWidth() {
        return graphicWidth;
    }

    @Override
    public void setGraphicWidth(int width) {
        graphicWidth = width;
    }

    @Override
    public int getGraphicHeight() {
        return graphicHeight;
    }

    @Override
    public void setGraphicHeight(int height) {
        graphicHeight = height;
    }

    @Override
    public void preparePrinter() {
        // do nothing in standard setup
    }

    @Override
    public void closePrinter() {
        // do nothing in standard setup
    }

    @Override
    public void heading(int h, String lines) {
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
    public void eqn(String equation) {
        print("<latex>");
        print("</latex>");
    }

    @Override
    public void draw(Figure figure) {
        draw(figure, getGraphicWidth(), getGraphicHeight());
    }
}
