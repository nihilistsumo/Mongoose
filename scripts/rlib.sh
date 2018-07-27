#!/bin/bash

rlib=~/Softwares
#rlib=/home/sk1105/sumanta

trecdir=/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train
#trecdir=/home/sk1105/sumanta/trec-methods/benchmarkY1/benchmarkY1-train
cvdir=/home/sumanta/Documents/Mongoose-data/Mongoose-results/hier-runs-basic-sim-and-fixed/cv
#cvdir=/home/sk1105/sumanta/trec-methods/cv-results
jardir=/home/sumanta/git/Mongoose/target
#jardir=/home/sk1105/sumanta/trec-methods/Mongoose/target
jarfile=trec-car-methods-0.9-jar-with-dependencies.jar
class=edu.unh.cs.lucene.TrecCarLuceneQuery
qrelslevel=hierarchical

#indexes
paraindex=../paragraph-corpus-paragraph-index
pageindex=../paragraph-corpus-page-index
entindex=../paragraph-corpus-entity-index
aspindex=../paragraph-corpus-aspect-index

# Ranklib	
leavefold=0

java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmb $cvdir/runs/tmp-leave$leavefold $cvdir/runs/tmp-leave$leavefold/fet-file $cvdir/qrels/leave$leavefold.qrels
java -jar $rlib/RankLib-2.1-patched.jar -train $cvdir/runs/tmp-leave$leavefold/fet-file -ranker 4 -metric2t MAP -save $cvdir/models/fold$leavefold-rlib-model
rm $cvdir/runs/tmp-leave$leavefold/fet-file


# Combining runfiles using rlib models

#for leave in {0..4}
#do
#	mkdir $cvdir/tmp-runs$leave
#	cp $cvdir/runs/fold$leave-*-run $cvdir/tmp-runs$leave
#	java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmbrun $cvdir/tmp-runs$leave $cvdir/models/fold$leave-rlib-model $cvdir/comb-runs/fold-$leave-comb-run
#	rm -r $cvdir/tmp-runs$leave
#done

#cat $cvdir/comb-runs/* >> $cvdir/comb-runs/cv-comb-run
#rm $cvdir/comb-runs/fold*-run

#java -jar $jardir/Mongoose-0.0.1-SNAPSHOT-jar-with-dependencies.jar -cmboptw $cvdir/models

#echo "Cross validation complete"