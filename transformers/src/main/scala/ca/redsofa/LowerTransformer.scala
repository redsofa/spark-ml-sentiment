package ca.redsofa

//Source : https://github.com/jaceklaskowski/mastering-apache-spark-book/blob/master/spark-mllib/spark-mllib-transformers.adoc#custom-transformer
import org.apache.spark.ml._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._

class LowerTransformer(override val uid: String)
    extends UnaryTransformer[String, String, LowerTransformer] with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("lower"))

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType)
  }

  protected def createTransformFunc: String => String = {
    _.toLowerCase
  }

  protected def outputDataType: DataType = StringType
}

object LowerTransformer extends DefaultParamsReadable[LowerTransformer]
