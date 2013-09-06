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
        print("*" + lines + "*");
    }

    @Override
    public void code(String lines) {
        print("$");
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
