#!/bin/bash
spark-submit \
	--class ca.redsofa.job.BatchTrainModel \
	--master local[*] \
  	./target/batch-train-model-0.1-SNAPSHOT.jar
