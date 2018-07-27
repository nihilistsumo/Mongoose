#!/bin/bash

#rlib=~/Softwares
rlib=/home/sk1105/sumanta

#trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
trecdir=/home/sk1105/sumanta/trec-methods/benchmarkY1/benchmarkY1-train
#cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
cvdir=/home/sk1105/sumanta/trec-methods/cv-results-passage/cv-results-new
#jardir=/home/sumanta/git/Mongoose/target
jardir=/home/sk1105/sumanta/trec-methods/Mongoose/target
jarfile=trec-car-methods-0.9-jar-with-dependencies.jar
class=edu.unh.cs.lucene.TrecCarLuceneQuery
qrelslevel=hierarchical

# Ranklib	
leavefold=$1

java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb $cvdir/runs/tmp-leave$leavefold $cvdir/runs/tmp-leave$leavefold/fet-file $cvdir/qrels/leave$leavefold.qrels
java -jar $rlib/RankLib-2.1-patched.jar -train $cvdir/runs/tmp-leave$leavefold/fet-file -ranker 4 -metric2t MAP -save $cvdir/models/fold$leavefold-rlib-model
rm $cvdir/runs/tmp-leave$leavefold/fet-file