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

import javax.swing.*;
import java.awt.*;

/**
 * @author tutuianu
 */
public final class StandardPrinter extends AbstractPrinter {

    public StandardPrinter() {
        setTextWidth(120);
        setGraphicWidth(800);
        setGraphicHeight(600);
    }

    private int textWidth = 120;
    private int graphicWidth = 800;
    private int graphicHeight = 600;

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
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println() {
        System.out.println();
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (message != null) {
            System.out.println(message);
        }
        if (throwable != null) {
            System.out.println(throwable);
        }
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        FigurePanel figurePanel = new FigurePanel(figure);
        JDialog frame = new JDialog();
        frame.setContentPane(figurePanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setAutoRequestFocus(true);
        frame.setSize(width, height);
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
            if (!frame.isVisible()) {
                break;
            }
        }
    }
}
