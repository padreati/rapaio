/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.regression.Regression;

/**
 * @author VHG6KOR
 *
 */
public class Test {

  public static void main(String[] args) {
    Regression lr=new RidgeRegression(2);
//    Regression lr=new LinearRegression();
    Frame frame=SolidFrame.matrix(10, "output", "input");
   double[][] data=new double[10][2];
 
     for(int j=0;j<10;j++){
       data[j][0]=j*j;
       System.out.println(data[j][0]);
     }
     for(int j=0;j<10;j++){
       data[j][1]=j;
       
     }
    RM rm=SolidRM.wrap(data);
    Frame matrix = SolidFrame.matrix(rm, "output", "input");
    lr.train(matrix, "output");
    lr.fit(matrix);
    System.out.println(lr.summary());
  }
}
