import org.netlib.util.*;
import org.netlib.lapack.Dgesvd;

public class DgesvdTest {

  public static void main(String[] args) {
    int M = 5;
    int N = 3;
    double[]m = {18.91, 14.91, -6.15, -18.15, 27.5, -1.59, -1.59,  -2.25,  -1.59, -2.25, -1.59, 1.59, 0.0, 1.59, 0.0};

    double[]s = new double[m.length];
    double[]u = new double[M*M];
    double[]vt = new double[N*N];
    double[]work = new double[Math.max(3*Math.min(M,N)+Math.max(M,N),5*Math.min(M,N))];
    org.netlib.util.intW info = new org.netlib.util.intW(2);

    Dgesvd.dgesvd("A","A",M,N,m, 0,M,s, 0,u, 0,M,vt,
      0,N,work, 0,work.length,info);

    System.out.println("info = " + info.val);
  }
}
