import org.netlib.util.*;
import org.netlib.blas.DDOT;

/**
 * SimpleDdotTest - example of calling the simplified version of Ddot.
 *
 * To compile and run:
 *
 * bambam> javac -classpath .:f2jutil.jar:blas.jar:blas_simple.jar SimpleDdotTest.java
 * bambam> java -classpath .:f2jutil.jar:blas.jar:blas_simple.jar SimpleDdotTest
 * Answer = 36.3
 *
 **/

public class SimpleDdotTest {
  public static void main(String [] args) {
    double [] dx = {1.1, 2.2, 3.3, 4.4};
    double [] dy = {1.1, 2.2, 3.3, 4.4};
    int incx = 1;
    int incy = 1;
    int n = dx.length;
    double answer;

    answer = DDOT.DDOT(n,dx,incx,dy,incy);

    System.out.println("Answer = " + answer);
  } 
}
