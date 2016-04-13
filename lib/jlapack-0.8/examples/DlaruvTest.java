import org.netlib.util.*;
import org.netlib.lapack.Dlaruv;


/**
 * DlaruvTest - example of calling the Java version of Dlaruv (from LAPACK).
 *
 * To compile and run:
 *
 * bambam> javac -classpath .:f2jutil.jar:blas.jar:lapack.jar DlaruvTest.java
 * bambam> java -classpath .:f2jutil.jar:blas.jar:lapack.jar:xerbla.jar DlaruvTest
 * Answer = 
 * 0.5806943866373508 0.7878030027693832 0.22090042246633246 0.7438538655551419 0.2937111564915149 0.19260597967192794 0.46939556457146026 0.903349054003403 0.852466982480923 0.3357901748424048 
 *
 **/

public class DlaruvTest {
  public static void main(String [] args) {
    int [] iseed = {1998, 1999, 2000, 2001};
    double []x = new double [10];
    int n = x.length;

    Dlaruv.dlaruv(iseed,0,n,x,0);

    System.out.println("Answer = ");
    for(int i = 0; i < x.length; i++)
      System.out.print(x[i] + " ");
    System.out.println();
  } 
}
