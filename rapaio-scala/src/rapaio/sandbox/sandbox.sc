import rapaio.io._
import scala.reflect.io.File

val df = CSV.read(new File("/home/ati/work/rapaio/rapaio/src/rapaio/datasets/titanic-train.csv"), true)
//Workspace.drawPlugin(new Plot().hist(x))


