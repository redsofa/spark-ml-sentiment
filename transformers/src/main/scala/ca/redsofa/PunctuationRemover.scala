package ca.redsofa

//Source : https://github.com/jaceklaskowski/mastering-apache-spark-book/blob/master/spark-mllib/spark-mllib-transformers.adoc#custom-transformer
import org.apache.spark.ml._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._

class PunctuationRemover(override val uid: String)
    extends UnaryTransformer[String, String, PunctuationRemover] with DefaultParamsWritable{

  def this() = this(Identifiable.randomUID("punctuation"))

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType)
  }

  protected def createTransformFunc: String => String = {
    _.replaceAll("[^a-zA-Z0-9\\s]", "")
  }

  protected def outputDataType: DataType = StringType
}

object PunctuationRemover extends DefaultParamsReadable[PunctuationRemover]
