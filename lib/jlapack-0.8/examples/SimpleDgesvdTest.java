import org.netlib.util.*;
import org.netlib.lapack.DGESVD;

public class SimpleDgesvdTest {

  public static void main(String[] args) {
    double[][] m = {
      {18.91, -1.59, -1.59},
      {14.91, -1.59, 1.59},
      {-6.15,  -2.25, 0},
      {-18.15, -1.59, 1.59},
      {27.5, -2.25, 0}};
    int M = m.length;
    int N = m[0].length;
    double[]s = new double[m.length];
    double[][]u = new double[M][M];
    double[][]vt = new double[N][N];
    double[]work = new double[Math.max(3*Math.min(M,N)+Math.max(M,N),5*Math.min(M,N))];
    org.netlib.util.intW info = new org.netlib.util.intW(2);

    DGESVD.DGESVD("A", "A", M, N, m, s, u, vt,
      work, work.length, info);

    System.out.println("info = " + info.val);
  }

}
