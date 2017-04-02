#!/bin/bash

wget -P /tmp/ https://archive.ics.uci.edu/ml/machine-learning-databases/00331/sentiment\ labelled\ sentences.zip

unzip -o -j /tmp/sentiment\ labelled\ sentences.zip sentiment\ labelled\ sentences/imdb_labelled.txt -d /tmp/
unzip -o -j /tmp/sentiment\ labelled\ sentences.zip sentiment\ labelled\ sentences/amazon_cells_labelled.txt -d /tmp/
unzip -o -j /tmp/sentiment\ labelled\ sentences.zip sentiment\ labelled\ sentences/yelp_labelled.txt -d /tmp/
