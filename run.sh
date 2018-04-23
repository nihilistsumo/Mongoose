#!/bin/bash
#This script is meant for c01 compute server
pagerunfilename=comb-top200-laura-cand-train
echo "Running Mongoose"
echo "Using candidate set from /home/mong/cand-sets/$pagerunfilename-page-run"
#java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ../../cand-sets/$pagerunfilename-page-run mongoose-results/simpara-$pagerunfilename

#Clustering
echo -e "\n\n1. Mode: Hierarchical Agglomerative with wordnet similarity"
echo "Starting clustering..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacsim /home/mong/cand-sets/$pagerunfilename-page-run /home/mong/simpara-data/simpara-$pagerunfilename /home/mong/rlib/simpara-$pagerunfilename-rlib-fet-model mongoose-results/hacsim-cluster-out-$pagerunfilename
echo "Clustering finished. The clusters are saved in mongoose-results/hacsim-cluster-out-$pagerunfilename"
echo "Evaluating clusters..."
#Measure clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm mongoose-results/hacsim-cluster-out-$pagerunfilename
echo "Mapping clusters to hierarchical sections and generating runfile"
#Map clusters to sections
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm mongoose-results/hacsim-cluster-out-$pagerunfilename mongoose-results/hacsim-$pagerunfilename-run
echo "All sections mapped. Runfile saved in mongoose-results/hacsim-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/hacsim-$pagerunfilename-run

#Clustering
echo -e "\n\n2. Mode: Hierarchical Agglomerative with word2vec similarity"
echo "Starting clustering..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -hacwv /home/mong/cand-sets/$pagerunfilename-page-run mongoose-results/hacwv-cluster-out-$pagerunfilename
echo "Clustering finished. The clusters are saved in mongoose-results/hacwv-cluster-out-$pagerunfilename"
echo "Evaluating clusters..."
#Measure clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm mongoose-results/hacwv-cluster-out-$pagerunfilename
echo "Mapping clusters to hierarchical sections and generating runfile"
#Map clusters to sections
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm mongoose-results/hacwv-cluster-out-$pagerunfilename mongoose-results/hacwv-$pagerunfilename-run
echo "All sections mapped. Runfile saved in mongoose-results/hacwv-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/hacwv-$pagerunfilename-run

#Clustering
echo -e "\n\n3. Mode: KMeans with word2vec similarity"
echo "Starting clustering..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -kmwv /home/mong/cand-sets/$pagerunfilename-page-run mongoose-results/kmwv-cluster-out-$pagerunfilename
echo "Clustering finished. The clusters are saved in mongoose-results/kmwv-cluster-out-$pagerunfilename"
echo "Evaluating clusters..."
#Measure clustering
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cm mongoose-results/kmwv-cluster-out-$pagerunfilename
echo "Mapping clusters to hierarchical sections and generating runfile"
#Map clusters to sections
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -pm mongoose-results/kmwv-cluster-out-$pagerunfilename mongoose-results/kmwv-$pagerunfilename-run
echo "All sections mapped. Runfile saved in mongoose-results/kmwv-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/kmwv-$pagerunfilename-run

#SummaryMapper
echo -e "\n\n4. Mode: Summarization"
echo  "Starting SummaryMapper..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -sm /home/mong/cand-sets/$pagerunfilename-page-run mongoose-results/summary-out-$pagerunfilename-run
echo "All sections mapped. Runfile saved in mongoose-results/summary-out-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/summary-out-$pagerunfilename-run

#LDATopicModel Mapper
echo -e "\n\n5. Mode: LDA Topic Model"
echo  "Starting LDA Topic Model mapper..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -tm /home/mong/cand-sets/$pagerunfilename-page-run mongoose-results/lda-topic-out-$pagerunfilename-run 0
echo "All sections mapped. Runfile saved in mongoose-results/lda-topic-out-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/lda-topic-out-$pagerunfilename-run

#LDATopicModel Mapper
echo -e "\n\n6. Mode: LDA Topic Model with section headings expanded using wordnet synonyms"
echo  "Starting LDA Topic Model mapper..."
java -jar target/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -tm /home/mong/cand-sets/$pagerunfilename-page-run mongoose-results/lda-topic-expanded-out-$pagerunfilename-run 1
echo "All sections mapped. Runfile saved in mongoose-results/lda-topic-expanded-out-$pagerunfilename-run"
echo "Evaluating runfile using treceval..."
#Evaluate run file
/trec_data/trec_eval -c /trec_data/benchmarkY1-train/train.pages.cbor-hierarchical.qrels mongoose-results/lda-topic-expanded-out-$pagerunfilename-run