#!/bin/bash
spark-submit \
	--class ca.redsofa.job.BatchUseModel \
	--master local[*] \
  	./target/batch-use-model-0.1-SNAPSHOT.jar
