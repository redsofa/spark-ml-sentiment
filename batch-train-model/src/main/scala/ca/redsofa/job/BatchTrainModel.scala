//End to End Spark ML Example From the book :
//Big Data Analytics with Spark: A Practitioner’s Guide to Using Spark for Large-Scale Data Processing, Machine Learning, and Graph Analytics, and High-Velocity Data Stream Processing

package ca.redsofa.job

import ca.redsofa.{LowerTransformer, PunctuationRemover}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, StopWordsRemover, Tokenizer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.tuning.CrossValidator

//Used to define DataFrame schema
case class Review(text: String, label: Double)

object BatchTrainModel {

  def main (args: Array[String]): Unit = {

    //Create Spark session
    val spark = SparkSession
      .builder
      .appName("batch-train-model")
      .getOrCreate()

    import spark.implicits._


    //Read IMDB, Yelp and Amazon reviews
    val imdb = spark.sparkContext.textFile("/tmp/imdb_labelled.txt")
    val yelp = spark.sparkContext.textFile("/tmp/yelp_labelled.txt")
    val amazon = spark.sparkContext.textFile("/tmp/amazon_cells_labelled.txt")

    //Combine these reviews into one RDD
    val rawData = imdb.union(yelp).union(amazon)

    //Cache the RDD as it gets referred to often when training the model. The persist() call is lazy
    rawData.persist()
    //Trigger an action to actually persist it
    rawData.count()

    //Split raw data RDD text and label based on the TAB character
    val columns = rawData.map{_.split("\\t")}


    println("**************** Print Columns RDD Contents (START) ****************\n")
    //Columns.take(10) returns an array of string arrays.
    columns.take(10).foreach(s => {
      val e = s.deep.mkString("")
      println(e + "\n")
    })
    println("**************** Print Columns RDD Contents (END) ****************\n")

    //Create an RDD of Reviews and convert to a DataFrame. Spark ML works with DataFrames
    val reviews = columns.map{
      a => Review(a(0), a(1).toDouble)
    }.toDF()


    println("**************** Print reviews DataFrame Contents (START) ****************\n")
    reviews.show(5, false)
    println("**************** Print reviews DataFrame Contents (END) ****************\n")


    println("**************** Print reviews Schema (START) ****************\n")
    reviews.printSchema()
    println("**************** Print reviews Schema (End) ****************\n")


    //A label of 0.0 is a negative review.
    //A label of 1.0 is a positive review.
    println("**************** Print review count per label type (START) ****************\n")
    reviews.groupBy("label").count.show(true)
    println("**************** Print review count per label type (End) ****************\n")




    // -------------- TRANSFORMERS ------------------

    //Instantiate custom transformers (LowerTransformer and PunctuationRemover). Set input and output columns.
    //Also instantiate Tokenizer and StopWordsRemover transformers.
    //Note that the custom transformers are included in the transformers module
    val lowerTransformer = new LowerTransformer
    val noPunctTransformer = new PunctuationRemover
    val tokenizer = new Tokenizer()
    val stopWordsRemover = new StopWordsRemover()

    //These transformers get chained together in the
    //ML pipeline below so input and output columns follow a chaining path.
    //The output of the previous transformer serves as input for the current one.
    //That chaining path looks like :

    // lowerTransformer -> noPunctTransformer -> tokenizer -> stopWordsRemover -> hashingTF

    lowerTransformer.setInputCol("text").setOutputCol("lower")
    noPunctTransformer.setInputCol(lowerTransformer.getOutputCol).setOutputCol("nopunct")
    tokenizer.setInputCol(noPunctTransformer.getOutputCol).setOutputCol("tokens")
    stopWordsRemover.setInputCol(tokenizer.getOutputCol).setOutputCol("words")

    //Instantiate a HashingTF Transformer. This Transformer converts words to sparse vectors using a Hashing Trick.
    //The input col is set by asking the stopWordsRemover what its output column is
    val hashingTF = new HashingTF()
      .setNumFeatures(5000)
      .setInputCol(stopWordsRemover.getOutputCol)
      .setOutputCol("features")


    //The transformers add a column to the input DataFrames
    //Here's an example of how the DataFrame will expand in the ML Pipeline
    //(This next piece of code is only for illustration):
    //The label and features DataFrame fields are used to feed the model
    var b = lowerTransformer.transform(reviews)
    b = noPunctTransformer.transform(b)
    b = tokenizer.transform(b)
    b = stopWordsRemover.transform(b)
    b = hashingTF.transform(b)

    println("**************** Print Transformation Results (START) ****************\n")
    b.show(10, true)
    println("**************** Print Transformation Results (END) ****************\n")




    // -------------- ESTIMATORS ------------------

    //We use a LogisticRegression Estimator for predictions
    val lr = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.01)






    // -------------- PIPELINE -----------------------

    //Now we’re ready to construct the pipeline.
    //Pipeline stages are : lowerTransformer, noPunctTransformer, tokenizer, stopWordsRemover, hashingTF, lr

    val pipeline = new Pipeline().setStages(Array(lowerTransformer, noPunctTransformer, tokenizer, stopWordsRemover, hashingTF, lr))


    //Split the training and testing data randomly (80% for training and 20% for testing)
    val Array(trainingData, testData) = reviews.randomSplit(Array(0.8, 0.2))

    println("**************** Print training and test data row counts (START) ****************\n")
    println(trainingData.count)
    println(testData.count)
    println("**************** Print training and test data row counts (END) ****************\n")







    // -------------- TUNING -----------------------

    println("**************** Fit CrossValidator (START) ****************\n")
    val paramGrid = new ParamGridBuilder()
      .addGrid(hashingTF.numFeatures, Array(10000, 100000))
      .addGrid(lr.regParam, Array(0.01, 0.1, 1.0))
      .addGrid(lr.maxIter, Array(20, 30))
      .build()

    val evaluator = new BinaryClassificationEvaluator()

    val crossValidator = new CrossValidator()
      .setEstimator(pipeline)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(10)
      .setEvaluator(evaluator)

    val crossValidatorModel = crossValidator.fit(trainingData)

    println("**************** Fit CrossValidator (END) ****************\n")


    // -------------- EVALUATION -----------------------

    //Let’s now evaluate our model using Area under ROC as a metric.
    import org.apache.spark.ml.param.ParamMap
    val evaluatorParamMap = ParamMap(evaluator.metricName -> "areaUnderROC")


    //Save the model
    crossValidatorModel.write.overwrite().save("/tmp/pipeline.mdl")

    //Load the saved model
    val persistedModel = org.apache.spark.ml.tuning.CrossValidatorModel.load("/tmp/pipeline.mdl")
    val bestModel = persistedModel.bestModel


    //Evaluate predictions AUROC >=.8 is good
    val pred = bestModel.transform(testData)
    val AUCTest = evaluator.evaluate(pred, evaluatorParamMap)
    println("**************** AUC Test (START) ****************\n")
    println(AUCTest)
    println("**************** AUC Test (END) ****************\n")


    println("**************** testData Predictions (START) ****************\n")
    pred.select("text", "label", "prediction").show(10, true)
    println("**************** testData Predictions (END) ****************\n")

    //Close Spark session
    spark.stop()

  }
}