/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rapaio.blog.exploring;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.db.JavaDBUtil;
import rapaio.graphics.BarChart;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.sql.SQLException;

import static rapaio.workspace.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Olimpics {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
//        setPrinter(new HTMLPrinter("/home/ati/work/rapaio-blog/src/rapaio/blog/exploring/SummerOlympics.html", "Summer Olympics"));
        setPrinter(new LocalPrinter());
        new Olimpics().run();
    }

    public void run() throws IOException, SQLException {
        preparePrinter();
        Frame df = Datasets.loadOlympic();
//        Summary.summary(df);


        JavaDBUtil db = new JavaDBUtil();
        try {
            db.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.putFrame(df, "T");

//        Frame sel = db.getFrame("SELECT NOC, COUNT(*) AS CNT FROM T GROUP BY NOC");
//        Summary.summary(sel);
//        draw(new BarChart(sel.getCol("NOC"), null, sel.getCol("CNT"))
//                .useSortType(BarChart.SortType.DESC)
//                .useTop(20));

//        Frame rou = db.getFrame("SELECT * FROM T WHERE NOC = 'ROU'");
//        Summary.summary(rou);
//        draw(new Plot().add(new Histogram(rou.getCol("EDITION")).setBins(100)));

//        Frame sel = db.getFrame("SELECT SPORT, DISCIPLINE, COUNT(*) AS CNT "
//                + "FROM T "
//                + "WHERE NOC='ROU' "
//                + "GROUP BY SPORT, DISCIPLINE");
//        Summary.names(df);
//        Summary.summary(sel);
//        draw(new BarChart(sel.getCol("SPORT"), sel.getCol("DISCIPLINE"), sel.getCol("CNT"))
//                .useSortType(BarChart.SortType.DESC));
//        Summary.lines(-1, sel);

        Frame sel = db.getFrame("SELECT NOC, COUNT(*) AS CNT "
                + "FROM T "
                + "WHERE SPORT='Rowing' "
                + "GROUP BY NOC");
        Summary.names(df);
        Summary.summary(sel);
        draw(new BarChart(sel.getCol("NOC"), null, sel.getCol("CNT"))
                .useSortType(BarChart.SortType.DESC)
                .useTop(10));
        Summary.head(-1, sel);

//        Frame sel = db.getFrame("SELECT NOC, EDITION, MEDAL "
//                + "FROM T "
//                + "WHERE SPORT='Gymnastics' AND DISCIPLINE = 'Artistic G.' AND GENDER = 'Women' ");
//        Summary.names(df);
//        Summary.summary(sel);
//        draw(new BarChart(sel.getCol("NOC"), null, null)
//                .useSortType(BarChart.SortType.DESC)
//                .useTop(10));

//        draw(new Plot().add(new Histogram(sel.getCol("EDITION")).setProb(false)));
//        Summary.lines(-1, sel);


        closePrinter();
    }

}
