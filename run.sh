#!/bin/bash
#This script is meant for c01 compute server
echo "Running Mongoose"
echo "Using candidate set from /home/mong/cand-sets/"$pagerunfilename"-page-run"
pagerunfilename=comb-top200-laura-cand-train
#java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ../../cand-sets/$pagerunfilename-page-run mongoose-results/simpara-$pagerunfilename

#Clustering
echo "Starting clustering..."
echo "Mode: Hierarchical Agglomerative with wordnet similarity"
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacsim /home/mong/cand-sets/$pagerunfilename-page-run /home/mong/simpara-data/simpara-$pagerunfilename /home/mong/rlib/simpara-$pagerunfilename-rlib-fet-model mongoose-results/cluster-out-$pagerunfilename
echo "Clustering finished. The clusters are saved in mongoose-results/cluster-out-"$pagerunfilename
echo "Evaluating clusters..."
#Measure clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm mongoose-results/cluster-out-$pagerunfilename
echo "Mapping clusters to hierarchical sections and generating runfile"
#Map clusters to sections
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm mongoose-results/cluster-out-$pagerunfilename mongoose-results/hacsim-$pagerunfilename-run
echo "All sections mapped. Runfile saved in mongoose-results/hacsim-"$pagerunfilename"-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/hacsim-$pagerunfilename-run