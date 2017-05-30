package rapaio.printer;

import rapaio.data.Frame;
import rapaio.data.Var;

public interface TypeStrategy {
	public void getString(Frame df, Var v,String[][] first, String[][] second, int th );
}
