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
 *
 */

package rapaio.printer;

import rapaio.graphics.base.Figure;

import java.text.DecimalFormat;

/**
 * @author tutuianu
 */
@Deprecated
public interface Printer {

    DecimalFormat formatDecShort = new DecimalFormat() {{
        setMinimumIntegerDigits(1);
        setMinimumFractionDigits(3);
        setMaximumFractionDigits(3);
    }};

    DecimalFormat formatDecLong = new DecimalFormat() {{
        setMinimumFractionDigits(30);
        setMaximumFractionDigits(30);
        setMinimumIntegerDigits(1);
    }};

    DecimalFormat formatDecFlex = new DecimalFormat() {{
        setMinimumFractionDigits(0);
        setMaximumFractionDigits(7);
        setMinimumIntegerDigits(1);
    }};

    int getTextWidth();

    void setTextWidth(int chars);

    int getGraphicWidth();

    void setGraphicWidth(int width);

    int getGraphicHeight();

    void setGraphicHeight(int height);

    void print(String message);

    void println();

    void error(String message, Throwable throwable);

    void preparePrinter();

    void closePrinter();

    void heading(int h, String lines);

    void code(String lines);

    void p(String lines);

    void eqn(String equation);

    void draw(Figure figure, int width, int height);

    void draw(Figure figure);
}
