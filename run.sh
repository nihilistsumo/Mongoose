#!/bin/bash
#This script is meant for c01 compute server
echo "Running Mongoose"
pagerunfilename=comb-top200-laura-cand-train
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ../../cand-sets/$pagerunfilename-page-run mongoose-results/simpara-$pagerunfilename
#Clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacsim /home/mong/cand-sets/$pagerunfilename-page-run /home/mong/simpara-data/simpara-$pagerunfilename /home/mong/rlib/simpara-$pagerunfilename-rlib-fet-model mongoose-results/cluster-out-$pagerunfilename
#Measure clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm mongoose-results/cluster-out-$pagerunfilename
#Map clusters to sections
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm mongoose-results/cluster-out-$pagerunfilename /mongoose-results/hacsim-$pagerunfilename-run
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels /mongoose-results/hacsim-$pagerunfilename-run