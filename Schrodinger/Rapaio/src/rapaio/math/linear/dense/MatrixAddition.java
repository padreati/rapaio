/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

/**
 * @author VHG6KOR
 *
 */
public class MatrixAddition {

  public static RM add(RM A, RM B){
    if(A==null||B==null){
      throw new IllegalArgumentException("Matrix is null");
    }
    if(A.colCount()!=B.colCount()||A.rowCount()!=B.rowCount()){
      throw new IllegalArgumentException("Matrix inner dimensions must agree.");
    }
    RM X = SolidRM.empty(A.rowCount(), A.colCount());
    for(int i=0;i<A.rowCount();i++){
      for(int j=0;j<A.colCount();j++){
        X.set(i, j, A.get(i, j)+B.get(i, j));
      }
    }
    return X;
  }
}
