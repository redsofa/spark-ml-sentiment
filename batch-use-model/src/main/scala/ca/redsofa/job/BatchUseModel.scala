//End to End Spark ML Example From the book :
//Big Data Analytics with Spark: A Practitioner’s Guide to Using Spark for Large-Scale Data Processing, Machine Learning, and Graph Analytics, and High-Velocity Data Stream Processing

package ca.redsofa.job

import org.apache.spark.sql.SparkSession

case class ReviewText(text:String)

object BatchUseModel {

  def main (args: Array[String]): Unit = {

    //Create Spark session
    val spark = SparkSession
      .builder
      .appName("batch-train-model")
      .getOrCreate()

    import spark.implicits._

    //Load the saved model
    val persistedModel = org.apache.spark.ml.tuning.CrossValidatorModel.load("/tmp/pipeline.mdl")
    val bestModel = persistedModel.bestModel


    val values = List(
      ReviewText("This is really bad"),
      ReviewText("This is great"),
      ReviewText("Burnt toast is better than this"),
      ReviewText("Awefull movie"),
      ReviewText("Super$!! Horrific"),
      ReviewText("Bonkers"),
      ReviewText("Not Great")
    )

    val df = values.toDF()

    val p = bestModel.transform(df)
    println("**************** New Data Predictions (START) ****************\n")
    p.select("text", "prediction", "probability").show(10, false)
    println("**************** New Data Predictions (END) ****************\n")


    //Close Spark session
    spark.stop()

  }
}