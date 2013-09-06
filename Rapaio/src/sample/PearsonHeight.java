package sample;

import rapaio.data.Frame;
import rapaio.data.IndexOneVector;
import rapaio.datasets.Datasets;
import rapaio.distributions.Distribution;
import rapaio.distributions.Normal;
import rapaio.explore.Summary;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.QQPlot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.Points;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;

import static rapaio.core.BaseMath.sqrt;
import static rapaio.core.BaseStat.*;
import static rapaio.core.Correlation.pearsonRho;
import static rapaio.explore.Summary.summary;
import static rapaio.explore.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonHeight {

    public static void main(String[] args) throws IOException {
        setPrinter(new HTMLPrinter("pearsonheight.html", "Pearson's Height Dataset Analysis"));
        preparePrinter();

        heading(1, "Analysis of Pearson's Height dataset");

        Frame df = Datasets.loadPearsonHeightDataset();

        p("This exploratory analysis is provided as a sample of analysis produced with Rapaio system.");
        p("The studied dataset contains " + df.getRowCount() + " observations and has " + df.getColCount() + " columns.");

        Summary.summary(df);

        p("First we take a look at the histograms for the two dimensions");

        for (int i = 0; i < df.getColCount(); i++) {
            Histogram hist = new Histogram(df.getCol(i), 50, true);
            hist.setBottomLabel(df.getColNames()[i]);
            hist.getOp().setXRange(57, 80);
            hist.getOp().setYRange(0, 0.15);

            Normal normal = new Normal(mean(df.getCol(i)).value(), sqrt(variance(df.getCol(i)).value()));
            FunctionLine nline = new FunctionLine(hist, normal.getPdfFunction());
            nline.opt().setColorIndex(new IndexOneVector(2));
            hist.add(nline);

            draw(hist, 700, 300);
        }

        heading(2, "About normality");

        p("Looking at both produced histograms we are interested to understand "
                + "if the data contained in both variables resemble a normal "
                + "curve. Basically we are interested if the the values of "
                + "those dimensions are normally distributed.");

        p("An ususal graphical tools which can give us insights about that fact "
                + "is the quantile-quantile plot. ");

        for (int i = 0; i < df.getColCount(); i++) {

            QQPlot qqplot = new QQPlot();

            double mu = mean(df.getCol(i)).value();

            Distribution normal = new Normal();
            qqplot.add(df.getCol(i), normal);
            qqplot.setLeftLabel(df.getColNames()[i]);

            qqplot.add(new ABLine(qqplot, mu, true));
            qqplot.add(new ABLine(qqplot, 0, false));

            draw(qqplot, 500, 300);
        }

        summary(mean(df.getCol("Father")));
        summary(variance(df.getCol("Father")));

        summary(mean(df.getCol("Son")));
        summary(variance(df.getCol("Son")));

        summary(pearsonRho(df.getCol("Father"), df.getCol("Son")));

        double[] perc = new double[11];
        for (int i = 0; i < perc.length; i++) {
            perc[i] = i / (10.);
        }
        QuantilesResult fatherQuantiles = quantiles(df.getCol("Father"), perc);
        QuantilesResult sonQuantiles = quantiles(df.getCol("Son"), perc);
        summary(fatherQuantiles);
        summary(sonQuantiles);

        Plot plot = new Plot();
        plot.add(new Points(plot, df.getCol("Father"), df.getCol("Son")));
        plot.getOp().setXRange(55, 80);
        plot.getOp().setYRange(55, 80);

        for (int i = 0; i < fatherQuantiles.value().length; i++) {
            ABLine line = new ABLine(plot, fatherQuantiles.value()[i], false);
            line.opt().setColorIndex(new IndexOneVector(30));
            plot.add(line);
        }

        for (int i = 0; i < sonQuantiles.value().length; i++) {
            ABLine line = new ABLine(plot, sonQuantiles.value()[i], true);
            line.opt().setColorIndex(new IndexOneVector(30));
            plot.add(line);
        }
        draw(plot, 600, 600);

        closePrinter();
    }
}
