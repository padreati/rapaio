package rapaio.ts;

import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.printer.Printable;

 abstract class Correlation implements Printable {

     protected Var ts;
     protected VarInt lags;

     Correlation(Var ts, VarInt lags) {
         this.ts = ts;
         this.lags = lags;
     }
 }
