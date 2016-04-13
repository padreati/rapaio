import java.lang.*;
import org.netlib.util.*;
import org.netlib.lapack.Dstevr;

/**
 * DstevrTest - example of calling DSTEVR (from LAPACK).
 *
 * To compile and run:
 *
 * # javac -classpath .:f2jutil.jar:blas.jar:lapack.jar DstevrTest.java 
 * # java -classpath .:f2jutil.jar:blas.jar:lapack.jar DstevrTest
 * Selected eigenvalues
 * 3.547002474892091 8.657766989006001 
 * Selected eigenvectors
 * 0.33875494698229236 0.049369992446919496 
 * 0.8628096883458374 0.3780638984074957 
 * -0.36480280022104183 0.8557817766452163 
 * 0.08788313002203395 -0.3496681903300259 
 **/

public class DstevrTest {

  public static void main (String [] args)  {

    double abstol= 0.0d;
    double vl= 0.0d;
    double vu= 0.0d;
    int i= 0;
    int ifail= 0;
    int il= 0;
    intW info= new intW(0);
    int iu= 0;
    int j= 0;
    int liwopt= 0;
    int lwopt= 0;
    intW m= new intW(0);
    int n= 0;
    double [] d= new double[10];
    double [] e= new double[10-1];
    double [] w= new double[10];
    double [] work= new double[200];
    double [] z= new double[10 * 5];
    int [] isuppz= new int[2*10];
    int [] iwork= new int[100];

    n = 4;
    il = 2;
    iu = 3;

    d[0] = 1.0;
    d[1] = 4.0;
    d[2] = 9.0;
    d[3] = 16.0;

    e[0] = 1.0;
    e[1] = 2.0;
    e[2] = 3.0;

    abstol = 0.0;

    Dstevr.dstevr("Vectors", "Indices", n, d, 0, e, 0, 
      vl, vu, il, iu, abstol, m, w, 0, z, 0, 10, isuppz, 0, 
      work, 0, 200, iwork, 0, 100, info);

    lwopt = (int)(work[0]);
    liwopt = iwork[0];

    if(info.val == 0)  {
      System.out.println("Selected eigenvalues");
      System.out.print("");
      for(j = 0; j < m.val; j++)
        System.out.print(w[j] + " ");

      System.out.println();

      System.out.println("Selected eigenvectors");
      for(i = 0; i < n; i++) {
        for(j = 0; j < m.val; j++)
          System.out.print(z[j*10+i] + " ");
        System.out.println();
      }

      ifail = 0;
    }
    else
      System.out.println("Failure in DSTEVR. INFO = " + info.val);

    if(200 < lwopt)  {
      System.out.println();
      System.out.println("Real workspace required = " + lwopt);
      System.out.println("Real workspace provided = " + 200);
    }

    if (100 < liwopt)  {
      System.out.println();
      System.out.println("Integer workspace required = " + liwopt);
      System.out.println("Integer workspace provided = " + 100);
    }

    return;
  }
}
