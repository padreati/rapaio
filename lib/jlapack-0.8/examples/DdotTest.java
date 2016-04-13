import org.netlib.util.*;
import org.netlib.blas.Ddot;

/**
 * DdotTest - example of calling the Java version of Ddot.
 *
 * To compile and run:
 *
 * bambam> javac -classpath .:f2jutil.jar:blas.jar DdotTest.java
 * bambam> java -classpath .:f2jutil.jar:blas.jar DdotTest
 * Answer = 36.3
 *
 **/

public class DdotTest {
  public static void main(String [] args) {
    double [] dx = {1.1, 2.2, 3.3, 4.4};
    double [] dy = {1.1, 2.2, 3.3, 4.4};
    int incx = 1;
    int incy = 1;
    int n = dx.length;
    double answer;

    answer = Ddot.ddot(n,dx,0,incx,dy,0,incy);

    System.out.println("Answer = " + answer);
  } 
}
