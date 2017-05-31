package rapaio.printer;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;

public class NumbericTypeStrategy implements TypeStrategy {
	public void getString(Frame df, Var v,String[][] first, String[][] second, int th ){
		double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
        double[] perc = Quantiles.from(v, p).getValues();
        double mean = Mean.from(v).getValue();

        int nas = 0;
        for (int j = 0; j < df.getRowCount(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        first[th][0] = "Min.";
        first[th][1] = "1st Qu.";
        first[th][2] = "Median";
        first[th][3] = "Mean";
        first[th][4] = "2nd Qu.";
        first[th][5] = "Max.";

        second[th][0] = String.format("%.3f", perc[0]);
        second[th][1] = String.format("%.3f", perc[1]);
        second[th][2] = String.format("%.3f", perc[2]);
        second[th][3] = String.format("%.3f", mean);
        second[th][4] = String.format("%.3f", perc[3]);
        second[th][5] = String.format("%.3f", perc[4]);

        if (nas != 0) {
            first[th][6] = "NA's";
            second[th][6] = String.format("%d", nas);
        }
	}
}
