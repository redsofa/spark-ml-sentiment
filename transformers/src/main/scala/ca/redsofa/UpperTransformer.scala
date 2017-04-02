package ca.redsofa

//Source : https://github.com/jaceklaskowski/mastering-apache-spark-book/blob/master/spark-mllib/spark-mllib-transformers.adoc#custom-transformer
import org.apache.spark.ml._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._

class UpperTransformer(override val uid: String)
    extends UnaryTransformer[String, String, UpperTransformer] with DefaultParamsWritable{

  def this() = this(Identifiable.randomUID("upper"))

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType)
  }

  protected def createTransformFunc: String => String = {
    _.toUpperCase
  }

  protected def outputDataType: DataType = StringType
}

object UpperTransformer extends DefaultParamsReadable[UpperTransformer]
