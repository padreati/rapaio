/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.Regression;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * @author VHG6KOR
 *
 */
public class RidgeRegression extends AbstractRegression {

  
  /**
   * 
   */
  private static final long serialVersionUID = -6014222985456365210L;
  
  private double alpha=0;
  /**
   * @param alpha Regularization strength; must be a positive float. Regularization improves the conditioning of the problem and reduces the variance of the estimates. Larger values specify stronger regularization
   * 
   */
  public RidgeRegression(double alpha) {
      this.alpha=alpha;
  }
 
  
  public static RidgeRegression newRr(double alpha) {
    return new RidgeRegression(alpha);
}


protected RM beta;

@Override
public Regression newInstance() {
    return new RidgeRegression( alpha);
}

@Override
public String name() {
    return "RidgeRegression";
}

@Override
public String fullName() {
    StringBuilder sb = new StringBuilder();
    sb.append(name());
    return sb.toString();
}

@Override
public RidgeRegression withInputFilters(FFilter... filters) {
    return (RidgeRegression) super.withInputFilters(filters);
}

@Override
public Capabilities capabilities() {
    return new Capabilities()
            .withInputTypes(VarType.NUMERIC, VarType.INDEX, VarType.BINARY, VarType.ORDINAL)
            .withTargetTypes(VarType.NUMERIC)
            .withInputCount(1, 1_000_000)
            .withTargetCount(1, 1_000_000)
            .withAllowMissingInputValues(false)
            .withAllowMissingTargetValues(false);
}

public RV firstCoeff() {
    return beta.mapCol(0);
}

public RV coefficients(int targetIndex) {
    return beta.mapCol(targetIndex);
}

public RM allCoefficients() {
    return beta;
}

@Override
protected boolean coreTrain(Frame df, Var weights) {
    if (targetNames().length == 0) {
        throw new IllegalArgumentException("OLS must specify at least one target variable name");
    }
    if(alpha<0){
       throw new IllegalArgumentException("Alpha- Regularization strength cannot be negative");
    }
    RM X = SolidRM.copy(df.mapVars(inputNames()));
    RM Y = SolidRM.copy(df.mapVars(targetNames()));
    
    //Create an input matrix of size X.rowCount()+X.colCount() in order to accomodate lamdaI (ridge regression factor)
    double[][] xArray=new double[X.rowCount()+X.colCount()][X.colCount()];
    for (int i = 0; i < X.rowCount(); i++) {
        System.arraycopy(X.getRow(i), 0, xArray[i], 0, xArray[i].length);
    }
    SolidRM identity = SolidRM.identity(X.colCount());
    RM lambdaI=MatrixMultiplication.mul(identity, Math.sqrt(this.alpha));
    
    //fill the remaining rows with the identity matrix 
    for(int i = 0; i < lambdaI.colCount(); i++){
      xArray[X.rowCount()]=lambdaI.getRow(i);
    }
    double[][] yArray=new double[Y.rowCount()+Y.colCount()][Y.colCount()];
    
  //Create an out matrix of size Y.rowCount()+Y.colCount()
    for (int i = 0; i < Y.rowCount(); i++) {
      System.arraycopy(Y.getRow(i), 0, yArray[i], 0, yArray[i].length);
  }
    
    //fill the remaining rows zeros
    for(int i = 0; i < lambdaI.colCount(); i++){
      yArray[X.rowCount()]=new double[lambdaI.colCount()];
    }
    RM Xnew= SolidRM.wrap(xArray);
    RM Ynew= SolidRM.wrap(yArray);
    beta = QRDecomposition.from(Xnew).solve(Ynew);
    return true;
}

@Override
public LinearRFit fit(Frame df) {
    return (LinearRFit) super.fit(df);
}

@Override
public LinearRFit fit(Frame df, boolean withResiduals) {
    return (LinearRFit) super.fit(df, withResiduals);
}

@Override
protected LinearRFit coreFit(Frame df, boolean withResiduals) {
    LinearRFit rp = new LinearRFit(this, df, withResiduals);
    for (int i = 0; i < targetNames().length; i++) {
        String target = targetName(i);
        for (int j = 0; j < rp.fit(target).rowCount(); j++) {
            double fit = 0.0;
            for (int k = 0; k < inputNames().length; k++) {
                fit += beta.get(k, i) * df.value(j, inputName(k));
            }
            rp.fit(target).setValue(j, fit);
        }
    }

    rp.buildComplete();
    return rp;
}

@Override
public String summary() {
    StringBuilder sb = new StringBuilder();
    sb.append(headerSummary());
    sb.append("\n");

    if (!hasLearned) {
        return sb.toString();
    }

    for (int i = 0; i < targetNames.length; i++) {
        String targetName = targetNames[i];
        sb.append("Target <<< ").append(targetName).append(" >>>\n\n");
        sb.append("> Coefficients: \n");
        RV coeff = beta.mapCol(i);

        TextTable tt = TextTable
                .newEmpty(coeff.count() + 1, 2)
                .withHeaderRows(1);
        tt.set(0, 0, "Name", 0);
        tt.set(0, 1, "Estimate", 0);
        for (int j = 0; j < coeff.count(); j++) {
            tt.set(j + 1, 0, inputNames[j], -1);
            tt.set(j + 1, 1, WS.formatMedium(coeff.get(j)), 1);
        }
        sb.append(tt.summary());
        sb.append("\n");
    }
    return sb.toString();
}


}
