package rapaio.printer;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;

public class BinaryTypeStrategy implements TypeStrategy {
	public void getVarSummary(Frame df, Var v,String[][] first, String[][] second, int th ){
		first[th][0] = "0";
        first[th][1] = "1";
        first[th][2] = "NA's";

        int ones = 0;
        int zeros = 0;
        int missing = 0;
        for (int j = 0; j < v.getRowCount(); j++) {
            if (v.isMissing(j)) {
                missing++;
            } else {
                if (v.getBinary(j))
                    ones++;
                else
                    zeros++;
            }
        }
        second[th][0] = String.valueOf(zeros);
        second[th][1] = String.valueOf(ones);
        second[th][2] = String.valueOf(missing);
	}

	@Override
	public void getPrintSummary(Var v, String[] first, String[] second) {
		first[0] = "0";
        first[1] = "1";
        first[2] = "NA's";

        int ones = 0;
        int zeros = 0;
        int missing = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                missing++;
            } else {
                if (v.getBinary(i))
                    ones++;
                else
                    zeros++;
            }
        }
        second[0] = String.valueOf(zeros);
        second[1] = String.valueOf(ones);
        second[2] = String.valueOf(missing);
	}
	

}
