import org.netlib.util.*;
import org.netlib.lapack.Dsygv;

/**
 * DsygvTest - example of calling the Java version of Dsygv (from LAPACK).
 *
 * To compile and run:
 *
 * # javac -classpath .:f2jutil.jar:blas.jar:lapack.jar DsygvTest.java
 * # java -classpath .:f2jutil.jar:blas.jar:lapack.jar:xerbla.jar DsygvTest
 * on return info = 0
 *
 **/

public class DsygvTest {

  public static void main(String[] args) {
    int itype = 1;
    String jobz = new String("V");
    String uplo = new String("U");
    int n = 3;
    double []a = {1.0, 2.0, 4.0, 0.0, 3.0, 5.0, 0.0, 0.0, 6.0};
    int lda = 3;
    double []b = {2.5298, 0.6405, 0.2091, 0.3798, 2.7833, 0.6808, 0.4611, 0.5678, 2.7942};
    int ldb = 3;
    double []w = new double[n];
    int lwork = 9;
    double []work = new double[lwork];
    org.netlib.util.intW info = new org.netlib.util.intW(0);

    Dsygv.dsygv(itype, jobz, uplo, n, a, 0, lda, b, 0, 
       ldb, w, 0, work, 0, lwork, info);

    System.out.println("on return info = " + info.val);
  }
}
