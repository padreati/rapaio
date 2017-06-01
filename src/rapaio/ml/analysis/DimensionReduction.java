package rapaio.ml.analysis;

import java.util.function.BiFunction;

import rapaio.data.Frame;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

public abstract class DimensionReduction {
	protected RV eigenValues;
    protected RM eigenVectors;
    
    protected String[] inputNames;
    protected RV mean;
    protected RV sd;
    
    protected boolean scaling = true;
	
    public RV getEigenValues() {
        return eigenValues;
    }
    
    public RM getEigenVectors() {
        return eigenVectors;
    }
    
    abstract public DimensionReduction withMaxRuns(int maxRuns);
    abstract public DimensionReduction withTol(double tol);
    
    abstract public String getSummary();
}
