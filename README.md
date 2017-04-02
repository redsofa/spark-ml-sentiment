# What's this Project About ?

End to End Spark ML Example From the book : Big Data Analytics with Spark: A Practitioner’s Guide to 
Using Spark for Large-Scale Data Processing, Machine Learning, and Graph Analytics, and High-Velocity Data Stream Processing
URL : https://www.amazon.com/Big-Data-Analytics-Spark-Practitioners/dp/1484209656#reader_1484209656


# Requirements

* *nix
* Java 1.8
* Scala 2.11.8
* Spark 2.1.0
* Maven 3.3.9
* wget


# Projects 

* transformers -> Custom Spark ML Pipeline transformers 
* batch-train-model -> Spark job that trains a model and persists it for later use. [Output](batch-train-model/program_output.txt)
* batch-use-model -> Spark job that uses persisted model for predictions. [Output](batch-use-model/program_output.txt)

# Fetching the Labeled Data

* run `sh download_data.sh` from the project root directoy (uses wget)


# Building Artifacts 

* Run `mvn install` from the the project root directory
* You'll need to generate the model and save it before using it.


# Generating the model

* Run `sh submit_job.sh' from inside the  batch-train-model directory
* Note that master points to local[*] in the spark-submit command


# Using the model

* Run `sh submit_job.sh' from inside the  batch-use-model directory
* Note that master points to local[*] in the spark-submit command
