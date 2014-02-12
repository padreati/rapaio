/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/12/14
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */

import org.scalatest._
import rapaio.data.Value

class ValueSpec extends FlatSpec with Matchers{
  "A value" should "contain the same double values when used with a fill parameter" in {
    val value = new Value(10,10,100.12)
    for (index <- 0 until value.getRowCount-1){
      value(index) should be (100.12)
    }
  }
}
