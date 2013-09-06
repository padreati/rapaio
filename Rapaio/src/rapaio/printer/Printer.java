package rapaio.printer;

import rapaio.graphics.base.Figure;

/**
 * @author tutuianu
 */
public interface Printer {

    int getTextWidth();

    void setTextWidth(int chars);

    int getGraphicWidth();

    void setGraphicWidth(int width);

    int getGraphicHeight();

    void setGraphicHeight(int height);

    void print(String message);

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
