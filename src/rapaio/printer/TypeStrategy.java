package rapaio.printer;

import rapaio.data.Frame;
import rapaio.data.Var;

public interface TypeStrategy {
	public void getVarSummary(Frame df, Var v,String[][] first, String[][] second, int th );

	public void getPrintSummary(Var v, String[] first, String[] second);
	
}
