package rapaio.studio;

import rapaio.graphics.base.Figure;
import rapaio.printer.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AgregatePrinter implements Printer {

    private final List<Printer> printers = new ArrayList<Printer>();

    public void addPrinter(Printer printer) {
        printers.add(printer);
    }

    public void removePrinter(Printer printer) {
        printers.remove(printer);
    }

    @Override
    public int getTextWidth() {
        int max = 0;
        for (Printer printer : printers) {
            if (max < printer.getTextWidth()) {
                max = printer.getTextWidth();
            }
        }
        return max;
    }

    @Override
    public void setTextWidth(int textWidth) {
    }

    @Override
    public int getGraphicWidth() {
        int max = 0;
        for (Printer printer : printers) {
            if (max < printer.getGraphicWidth()) {
                max = printer.getGraphicWidth();
            }
        }
        return max;
    }

    @Override
    public void setGraphicWidth(int graphicWidth) {
    }

    @Override
    public int getGraphicHeight() {
        int max = 0;
        for (Printer printer : printers) {
            if (max < printer.getGraphicHeight()) {
                max = printer.getGraphicHeight();
            }
        }
        return max;
    }

    @Override
    public void setGraphicHeight(int graphicHeight) {
    }

    @Override
    public void print(String string) {
        for (Printer printer : printers) {
            printer.print(string);
        }
    }

    @Override
    public void println() {
        for (Printer printer : printers) {
            printer.println();
        }
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        for (Printer printer : printers) {
            printer.error(string, thrwbl);
        }
    }

    @Override
    public void preparePrinter() {
        for (Printer printer : printers) {
            printer.preparePrinter();
        }
    }

    @Override
    public void closePrinter() {
        for (Printer printer : printers) {
            printer.closePrinter();
        }
    }

    @Override
    public void heading(int i, String string) {
        for (Printer printer : printers) {
            printer.heading(i, string);
        }
    }

    @Override
    public void code(String string) {
        for (Printer printer : printers) {
            printer.code(string);
        }
    }

    @Override
    public void p(String string) {
        for (Printer printer : printers) {
            printer.p(string);
        }
    }

    @Override
    public void eqn(String string) {
        for (Printer printer : printers) {
            printer.eqn(string);
        }
    }

    @Override
    public void draw(Figure figure, int i, int i1) {
        for (Printer printer : printers) {
            printer.draw(figure, i, i1);
        }
    }

    @Override
    public void draw(Figure figure) {
        for (Printer printer : printers) {
            printer.draw(figure);
        }
    }
}
